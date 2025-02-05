import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    alias(libs.plugins.maven.publish.base)
    alias(libs.plugins.kotlinx.binary.compatibilty.validator)
}

val archivesBaseName = "plugin-api"
group = "dev.arbjerg.lavalink"

dependencies {
    api(projects.protocol)
    api(libs.spring.boot)
    api(libs.spring.boot.web)
    api(libs.lavaplayer)
    api(libs.kotlinx.serialization.json)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

mavenPublishing {
    configure(KotlinJvm(JavadocJar.Dokka("dokkaJavadoc")))
    pom {
        name = "Lavalink Plugin API"
        description = "API for Lavalink plugin development"
    }
}
