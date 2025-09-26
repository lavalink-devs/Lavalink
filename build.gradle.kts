import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.ajoberstar.grgit.Grgit
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.gradle.git.properties)
    alias(libs.plugins.grgit)
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.maven.publish.base) apply false
}

val (gitVersion, release) = versionFromGit()
logger.lifecycle("Version: $gitVersion (release: $release)")

allprojects {
    group = "lavalink"
    version = gitVersion

    repositories {
        mavenCentral() // main maven repo
        mavenLocal()   // useful for developing
        maven("https://m2.dv8tion.net/releases")
        maven("https://maven.lavalink.dev/releases")
        maven("https://maven.lavalink.dev/snapshots")
        maven("https://jitpack.io") // build projects directly from GitHub
    }
}

subprojects {
    if (project.hasProperty("includeAnalysis")) {
        project.logger.lifecycle("applying analysis plugins")
        apply(from = "../analysis.gradle")
    }

    tasks.withType<KotlinCompile> {
        compilerOptions.jvmTarget = JvmTarget.JVM_17
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }

    afterEvaluate {
        plugins.withId(libs.plugins.maven.publish.base.get().pluginId) {
            configure<PublishingExtension> {
                val mavenUsername = findProperty("MAVEN_USERNAME") as String?
                val mavenPassword = findProperty("MAVEN_PASSWORD") as String?
                if (!mavenUsername.isNullOrEmpty() && !mavenPassword.isNullOrEmpty()) {
                    repositories {
                        val snapshots = "https://maven.lavalink.dev/snapshots"
                        val releases = "https://maven.lavalink.dev/releases"

                        maven(if (release) releases else snapshots) {
                            credentials {
                                username = mavenUsername
                                password = mavenPassword
                            }
                        }
                    }
                } else {
                    logger.lifecycle("Not publishing to maven.lavalink.dev because credentials are not set")
                }
            }

            configure<MavenPublishBaseExtension> {
                coordinates(group.toString(), project.the<BasePluginExtension>().archivesName.get(), version.toString())
                val mavenCentralUsername = findProperty("mavenCentralUsername") as String?
                val mavenCentralPassword = findProperty("mavenCentralPassword") as String?
                if (!mavenCentralUsername.isNullOrEmpty() && !mavenCentralPassword.isNullOrEmpty()) {
                    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, false)
                    if (release) {
                        signAllPublications()
                    }
                } else {
                    logger.lifecycle("Not publishing to OSSRH due to missing credentials")
                }

                pom {
                    url = "https://github.com/lavalink-devs/Lavalink"

                    licenses {
                        license {
                            name = "MIT License"
                            url = "https://github.com/lavalink-devs/Lavalink/blob/main/LICENSE"
                        }
                    }

                    developers {
                        developer {
                            id = "freyacodes"
                            name = "Freya Arbjerg"
                            url = "https://www.arbjerg.dev"
                        }
                    }

                    scm {
                        url = "https://github.com/lavalink-devs/Lavalink/"
                        connection = "scm:git:git://github.com/lavalink-devs/Lavalink.git"
                        developerConnection = "scm:git:ssh://git@github.com/lavalink-devs/Lavalink.git"
                    }
                }
            }
        }
    }
}

fun versionFromGit(): Pair<String, Boolean> {
    Grgit.open(mapOf("currentDir" to project.rootDir)).use { git ->
        val headTag = git.tag
            .list()
            .find { it.commit.id == git.head().id }

        val clean = git.status().isClean || System.getenv("CI") != null
        if (!clean) {
            logger.lifecycle("Git state is dirty, version is a snapshot.")
        }

        return if (headTag != null && clean) headTag.name to true else "${git.head().id}-SNAPSHOT" to false
    }
}
