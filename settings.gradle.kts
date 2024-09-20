rootProject.name = "Lavalink-Parent"

include(":Lavalink-Server")
include(":protocol")
include(":plugin-api")

project(":Lavalink-Server").projectDir = file("$rootDir/LavalinkServer")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            spring()
            voice()
            metrics()
            common()
            other()
        }
    }
}

fun VersionCatalogBuilder.spring() {
    version("spring-boot", "3.3.0")

    library("spring-websocket", "org.springframework", "spring-websocket").version("6.1.9")

    library("spring-boot",          "org.springframework.boot", "spring-boot").versionRef("spring-boot")
    library("spring-boot-web",      "org.springframework.boot", "spring-boot-starter-web").versionRef("spring-boot")
    library("spring-boot-undertow", "org.springframework.boot", "spring-boot-starter-undertow") .versionRef("spring-boot")
    library("spring-boot-test",     "org.springframework.boot", "spring-boot-starter-test") .versionRef("spring-boot")

    bundle("spring", listOf("spring-websocket", "spring-boot-web", "spring-boot-undertow"))
}

fun VersionCatalogBuilder.voice() {
    version("lavaplayer", "2.2.2")
    version("koe", "2.0.3-rc2")

    library("lavaplayer",            "dev.arbjerg", "lavaplayer").versionRef("lavaplayer")
    library("lavaplayer-ip-rotator", "dev.arbjerg", "lavaplayer-ext-youtube-rotator").versionRef("lavaplayer")
    library("lavadsp",               "dev.arbjerg", "lavadsp").version("0.7.8")

    library("koe",          "moe.kyokobot.koe", "core").versionRef("koe")
    library("koe-udpqueue", "moe.kyokobot.koe", "ext-udpqueue").versionRef("koe")

    version("udpqueue", "0.2.7")
    val platforms = listOf("linux-x86-64", "linux-x86", "linux-aarch64", "linux-arm", "linux-musl-x86-64", "linux-musl-aarch64", "win-x86-64", "win-x86", "darwin")
    platforms.forEach {
        library("udpqueue-native-$it", "club.minnced", "udpqueue-native-$it").versionRef("udpqueue")
    }

    bundle("udpqueue-natives", platforms.map { "udpqueue-native-$it" })
}

fun VersionCatalogBuilder.metrics() {
    version("prometheus", "0.16.0")

    library("metrics",         "io.prometheus", "simpleclient").versionRef("prometheus")
    library("metrics-hotspot", "io.prometheus", "simpleclient_hotspot").versionRef("prometheus")
    library("metrics-logback", "io.prometheus", "simpleclient_logback").versionRef("prometheus")
    library("metrics-servlet", "io.prometheus", "simpleclient_servlet").versionRef("prometheus")

    bundle("metrics", listOf("metrics", "metrics-hotspot", "metrics-logback", "metrics-servlet"))
}

fun VersionCatalogBuilder.common() {
    version("kotlin", "2.0.0")

    library("kotlin-reflect",     "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
    library("kotlin-stdlib-jdk8", "org.jetbrains.kotlin", "kotlin-stdlib-jdk8").versionRef("kotlin")

    library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").version("1.7.0")
    library("kotlinx-datetime", "org.jetbrains.kotlinx", "kotlinx-datetime").version("0.6.0")

    library("logback",        "ch.qos.logback",       "logback-classic").version("1.5.6")
    library("sentry-logback", "io.sentry",            "sentry-logback").version("7.10.0")
    library("oshi",           "com.github.oshi",      "oshi-core").version("6.4.11")
}

fun VersionCatalogBuilder.other() {
    val mavenPublishPlugin = version("maven-publish-plugin", "0.28.0")

    plugin("maven-publish", "com.vanniktech.maven.publish").versionRef(mavenPublishPlugin)
    plugin("maven-publish-base", "com.vanniktech.maven.publish.base").versionRef(mavenPublishPlugin)
}
