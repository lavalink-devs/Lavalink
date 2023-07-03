import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

plugins {
    signing
    `maven-publish`
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

apply(from = "../repositories.gradle")

val archivesBaseName = "protocol"
group = "dev.arbjerg.lavalink"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
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
    }

    targets {
        all {
            mavenPublication {
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
    }
}

if (findProperty("signing.gnupg.keyName") != null) {
    signing {
        sign(
            publishing.publications["js"],
            publishing.publications["jvm"],
            publishing.publications["kotlinMultiplatform"]
        )
        useGpgCmd()
    }
}


tasks {
    withType<KotlinJvmTest> {
        useJUnitPlatform()
    }
}

// Use system Node.Js on NixOS
if (System.getenv("NIX_PROFILES") != null) {
    rootProject.plugins.withType<NodeJsRootPlugin> {
        rootProject.the<NodeJsRootExtension>().download = false
    }
}
