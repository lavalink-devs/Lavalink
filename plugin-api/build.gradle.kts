plugins {
    java
    signing
    `java-library`
    `maven-publish`
}

apply(from = "../repositories.gradle")

val archivesBaseName = "plugin-api"
group = "dev.arbjerg.lavalink"

dependencies {
    api(libs.spring.boot)
    api(libs.spring.boot.web)
    api(libs.lavaplayer)
    api(libs.json)
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8

    withJavadocJar()
    withSourcesJar()
}

tasks.withType<Javadoc> {
    if (JavaVersion.current().isJava9Compatible) {
        /* https://stackoverflow.com/a/52850306 */
        val opts = options as StandardJavadocDocletOptions
        opts.addBooleanOption("html5", true)
    }
}

val isGpgKeyDefined = findProperty("signing.gnupg.keyName") != null

publishing {
    publications {
        create<MavenPublication>("PluginApi") {
            from(project.components["java"])

            pom {
                name.set("Lavalink Plugin API")
                description.set("API for Lavalink plugin development")
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
}

if (isGpgKeyDefined) {
    signing {
        sign(publishing.publications["PluginApi"])
        useGpgCmd()
    }
}
