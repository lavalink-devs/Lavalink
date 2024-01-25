import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.ajoberstar.grgit.Grgit
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.dokka") version "1.8.20" apply false
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    id("org.ajoberstar.grgit") version "5.2.0"
    id("org.springframework.boot") version "3.1.0" apply false
    id("org.sonarqube") version "4.2.0.3129"
    id("com.adarshr.test-logger") version "3.2.0"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.22" apply false
    alias(libs.plugins.maven.publish.base) apply false
}

allprojects {
    group = "lavalink"
    version = versionFromTag()

    repositories {
        mavenCentral() // main maven repo
        mavenLocal()   // useful for developing
        maven("https://m2.dv8tion.net/releases")
        maven("https://maven.lavalink.dev/releases")
        jcenter()
        maven("https://jitpack.io") // build projects directly from GitHub
    }
}

subprojects {
    if (project.hasProperty("includeAnalysis")) {
        project.logger.lifecycle("applying analysis plugins")
        apply(from = "../analysis.gradle")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }

    afterEvaluate {
        plugins.withId(libs.plugins.maven.publish.base.get().pluginId) {
            configure<PublishingExtension> {
                if (findProperty("MAVEN_PASSWORD") != null && findProperty("MAVEN_USERNAME") != null) {
                    repositories {
                        val snapshots = "https://maven.lavalink.dev/snapshots"
                        val releases = "https://maven.lavalink.dev/releases"

                        maven(if ((version as String).endsWith("-SNAPSHOT")) snapshots else releases) {
                            credentials {
                                password = findProperty("MAVEN_PASSWORD") as String?
                                username = findProperty("MAVEN_USERNAME") as String?
                            }
                        }
                    }
                } else {
                    logger.lifecycle("Not publishing to maven.lavalink.dev because credentials are not set")
                }
            }
            configure<MavenPublishBaseExtension> {
                coordinates(group.toString(), project.the<BasePluginExtension>().archivesName.get(), version.toString())

                if (findProperty("mavenCentralUsername") != null && findProperty("mavenCentralPassword") != null) {
                    publishToMavenCentral(SonatypeHost.S01, false)
                    if (!(version as String).endsWith("-SNAPSHOT")) {
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

@SuppressWarnings("GrMethodMayBeStatic")
fun versionFromTag(): String {
    Grgit.open(mapOf("currentDir" to project.rootDir)).use { git ->
        val headTag = git.tag
            .list()
            .find { it.commit.id == git.head().id }

        val clean = git.status().isClean || System.getenv("CI") != null
        if (!clean) {
            println("Git state is dirty, setting version as snapshot.")
        }

        return if (headTag != null && clean) headTag.name else "${git.head().id}-SNAPSHOT"
    }
}
