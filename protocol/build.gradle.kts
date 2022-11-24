plugins {
    java
    `java-library`
    `maven-publish`
    kotlin("jvm")
}

val archivesBaseName = "protocol"
group = "dev.arbjerg.lavalink"
version = "3.7.0"

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

publishing {
    publications {
        create<MavenPublication>("Protocol") {
            from(project.components["java"])

            pom {
                name.set("Lavalink Plugin API")
                description.set("API for Lavalink plugin development")
                url.set("https://github.com/freyacodes/lavalink")

                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://github.com/freyacodes/Lavalink/blob/master/LICENSE")
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
}
