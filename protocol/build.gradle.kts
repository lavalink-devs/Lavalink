import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

plugins {
    `maven-publish`
    signing
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.8.10"
}

val archivesBaseName = "protocol"
group = "dev.arbjerg.lavalink"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    js(IR) {
        nodejs()
        browser()
        compilations.all {
            packageJson {
                //language=RegExp
                // npm doesn't support our versioning :(
                val validVersion = """\d+\.\d+\.\d+""".toRegex()
                if (!validVersion.matches(project.version.toString())) {
                    version = "4.0.0"
                }
            }
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }

        commonMain {
            dependencies {
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.datetime)
            }
        }

        commonTest {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
            }
        }

        getByName("jsTest") {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        getByName("jvmTest") {
            dependencies {
                implementation(kotlin("test-junit5"))
            }
        }

        getByName("jvmMain") {
            dependencies {
                compileOnly(libs.lavaplayer)
                implementation(libs.jackson.module.kotlin)
            }
        }
    }

    targets {
        all {
            mavenPublication {
                pom {
                    name.set("Lavalink Protocol")
                    description.set("Protocol for Lavalink Client development")
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
}

tasks {
    withType<KotlinJvmTest> {
        useJUnitPlatform()
    }
}

publishing {
    if (findProperty("signing.gnupg.keyName") != null) {
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

        signing {
            publications.withType<MavenPublication> {
                sign(this)
            }
            useGpgCmd()
        }
    } else {
        println("Not capable of publishing to OSSRH because of missing GPG key")
    }
}
