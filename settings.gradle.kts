rootProject.name = "Lavalink-Parent"

include(":Lavalink-Server")
include(":protocol")
include(":Testbot")
include(":plugin-api")
include("plugin-api")

project(":Lavalink-Server").projectDir = file("$rootDir/LavalinkServer")

enableFeaturePreview("VERSION_CATALOGS")
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
    version("spring-boot", "2.6.6")

    library("spring-websocket", "org.springframework", "spring-websocket").version("5.3.17")

    library("spring-boot",          "org.springframework.boot", "spring-boot").versionRef("spring-boot")
    library("spring-boot-web",      "org.springframework.boot", "spring-boot-starter-web").versionRef("spring-boot")
    library("spring-boot-undertow", "org.springframework.boot", "spring-boot-starter-undertow") .versionRef("spring-boot")
    library("spring-boot-test",     "org.springframework.boot", "spring-boot-starter-test") .versionRef("spring-boot")
    library("jackson-module-kotlin", "com.fasterxml.jackson.module", "jackson-module-kotlin").version("2.13.2")

    bundle("spring", listOf("spring-websocket", "spring-boot-web", "spring-boot-undertow"))
}

fun VersionCatalogBuilder.voice() {
    version("lavaplayer", "1.5.4")

    library("lavaplayer",            "dev.arbjerg", "lavaplayer").versionRef("lavaplayer")
    library("lavaplayer-ip-rotator", "dev.arbjerg", "lavaplayer-ext-youtube-rotator").versionRef("lavaplayer")
    library("lavadsp",               "dev.arbjerg", "lavadsp").version("0.7.8")

    library("koe",          "moe.kyokobot.koe", "core").version("2.0.2")
    library("koe-udpqueue", "moe.kyokobot.koe", "ext-udpqueue").version("2.0.2")

    version("udpqueue", "0.2.7")
    val platforms = listOf("linux-x86-64", "linux-x86", "linux-aarch64", "linux-arm", "linux-musl-x86-64", "linux-musl-aarch64", "win-x86-64", "win-x86", "darwin")
    platforms.forEach {
        library("udpqueue-native-$it", "club.minnced", "udpqueue-native-$it").versionRef("udpqueue")
    }

    bundle("udpqueue-natives", platforms.map { "udpqueue-native-$it" })
}

fun VersionCatalogBuilder.metrics() {
    version("prometheus", "0.5.0")

    library("metrics",         "io.prometheus", "simpleclient").versionRef("prometheus")
    library("metrics-hotspot", "io.prometheus", "simpleclient_hotspot").versionRef("prometheus")
    library("metrics-logback", "io.prometheus", "simpleclient_logback").versionRef("prometheus")
    library("metrics-servlet", "io.prometheus", "simpleclient_servlet").versionRef("prometheus")

    bundle("metrics", listOf("metrics", "metrics-hotspot", "metrics-logback", "metrics-servlet"))
}

fun VersionCatalogBuilder.common() {
    version("kotlin", "1.7.20")

    library("kotlin-reflect",     "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
    library("kotlin-stdlib-jdk8", "org.jetbrains.kotlin", "kotlin-stdlib-jdk8").versionRef("kotlin")

    library("logback",        "ch.qos.logback",       "logback-classic").version("1.2.3")
    library("sentry-logback", "io.sentry",            "sentry-logback").version("1.7.2")
    library("oshi",           "com.github.oshi",      "oshi-core").version("6.4.8")
    library("json",           "org.json",             "json").version("20180813")

    library("spotbugs", "com.github.spotbugs", "spotbugs-annotations").version("3.1.6")
}


fun VersionCatalogBuilder.other() {
    library("jda",             "net.dv8tion",         "JDA").version("4.1.1_135")
    library("lavalink-client", "com.github.FredBoat", "Lavalink-Client").version("8d9b660")
}
