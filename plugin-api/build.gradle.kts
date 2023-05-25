import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    signing
    `maven-publish`
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

val archivesBaseName = "plugin-api"
group = "dev.arbjerg.lavalink"

dependencies {
    api(projects.protocol)
    api(libs.spring.boot)
    api(libs.spring.boot.web)
    api(libs.lavaplayer)
    api(libs.kotlinx.serialization.json)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

val dokkaJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Javadoc with Dokka"
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

val isGpgKeyDefined = findProperty("signing.gnupg.keyName") != null

publishing {
    publications {
        create<MavenPublication>("PluginApi") {
            from(project.components["java"])
            artifact(tasks.kotlinSourcesJar)
            artifact(dokkaJar)

            pom {
                name.set("Lavalink Plugin API")
                description.set("API for Lavalink plugin development")
                url.set("https://github.com/freyacodes/lavalink")

                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://github.com/lavalink-devs/Lavalink/blob/master/LICENSE")
                    }
                }

                developers {
                    developer {
                        id.set("freyacodes")
                        name.set("Freya Arbjerg")
                        url.set("https://www.arbjerg.dev")
                    }
                }

                scm {
                    connection.set("scm:git:ssh://github.com/freyacodes/lavalink.git")
                    developerConnection.set("scm:git:ssh://github.com/freyacodes/lavalink.git")
                    url.set("https://github.com/freyacodes/lavalink")
                }
            }
        }
    }

    if (isGpgKeyDefined) {
        repositories {
            val snapshots = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            val releases = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"

            maven(if ((version as String).endsWith("SNAPSHOT")) snapshots else releases) {
                credentials {
                    password = findProperty("ossrhPassword") as? String
                    username = findProperty("ossrhUsername") as? String
                }
            }
        }
    } else {
        println("Not capable of publishing to OSSRH because of missing GPG key")
    }
}

if (isGpgKeyDefined) {
    signing {
        sign(publishing.publications["PluginApi"])
        useGpgCmd()
    }
}
