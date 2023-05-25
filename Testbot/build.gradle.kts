plugins {
    application

    kotlin("jvm")
}

group = "dev.arbjerg.lavalink"
version = "1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

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
