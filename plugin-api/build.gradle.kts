plugins {
    java
    signing
    `java-library`
    `maven-publish`
}

val archivesBaseName = "plugin-api"
group = "dev.arbjerg.lavalink"
version = "3.6.1"

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
