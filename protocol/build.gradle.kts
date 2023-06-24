plugins {
    java
    signing
    `java-library`
    `maven-publish`
    kotlin("jvm")
}

val archivesBaseName = "protocol"
group = "dev.arbjerg.lavalink"

java {
    targetCompatibility = JavaVersion.VERSION_11
    sourceCompatibility = JavaVersion.VERSION_11

    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnly(libs.lavaplayer)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.jackson.module.kotlin)
}

val isGpgKeyDefined = findProperty("signing.gnupg.keyName") != null

publishing {
    publications {
        create<MavenPublication>("Protocol") {
            from(project.components["java"])

            pom {
                name.set("Lavalink Protocol")
                description.set("Protocol for Lavalink Client development")
                url.set("https://github.com/lavalink-devs/lavalink")

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
                    connection.set("scm:git:ssh://github.com/lavalink-devs/lavalink.git")
                    developerConnection.set("scm:git:ssh://github.com/lavalink-devs/lavalink.git")
                    url.set("https://github.com/lavalink-devs/lavalink")
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

    if (findProperty("MAVEN_USERNAME") != null && findProperty("MAVEN_PASSWORD") != null) {
        println("Publishing to Maven Repo")
        repositories {
            val snapshots = "https://maven.arbjerg.dev/snapshots"
            val releases = "https://maven.arbjerg.dev/releases"

            maven(if ((version as String).endsWith("SNAPSHOT")) snapshots else releases) {
                credentials {
                    password = findProperty("MAVEN_PASSWORD") as? String
                    username = findProperty("MAVEN_USERNAME") as? String
                }
            }
        }
    } else {
        println("Maven credentials not found, not publishing to Maven Repo")
    }
}

if (isGpgKeyDefined) {
    signing {
        sign(publishing.publications["Protocol"])
        useGpgCmd()
    }
}
