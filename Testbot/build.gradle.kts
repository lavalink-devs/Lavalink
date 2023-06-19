plugins {
    application

    kotlin("jvm")
}

group = "dev.arbjerg.lavalink"
version = "1.0"

application {
    mainClass.set("lavalink.testbot.TestbotKt")
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(libs.lavalink.client)
    compileOnly(libs.logback)
    compileOnly(libs.kotlin.stdlib.jdk8)
    compileOnly(libs.jda) {
        exclude(module = "opus-java")
    }
}
