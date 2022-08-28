import org.ajoberstar.grgit.Grgit
import org.apache.tools.ant.filters.ReplaceTokens
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    application
    `maven-publish`
}

apply(plugin = "org.springframework.boot")
apply(plugin = "com.gorylenko.gradle-git-properties")
apply(plugin = "org.ajoberstar.grgit")
apply(plugin = "com.adarshr.test-logger")
apply(plugin = "kotlin")
apply(plugin = "kotlin-spring")

description = "Play audio to discord voice channels"
version = versionFromTag()

application {
    mainClass.set("lavalink.server.Launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

configurations {
    compileOnly {
        extendsFrom(annotationProcessor.get())
    }
}

dependencies {
    implementation(projects.pluginApi)

    implementation(libs.bundles.metrics)
    implementation(libs.bundles.spring) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }

    implementation(libs.koe) {
        // This version of SLF4J does not recognise Logback 1.2.3
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(libs.koe.udpqueue) {
        exclude(module="udp-queue")
    }
    implementation(libs.bundles.udpqueue.natives)

    implementation(libs.lavaplayer)
    implementation(libs.lavaplayer.ip.rotator)

    implementation(libs.lavadsp)
    implementation(libs.kotlin.reflect)
    implementation(libs.logback)
    implementation(libs.sentry.logback)
    implementation(libs.oshi)
    implementation(libs.json)
    implementation(libs.gson)

    compileOnly(libs.spotbugs)

    testImplementation(libs.spring.boot.test)
}

tasks {
    build {
        doLast {
            println("Version: $version")
        }
    }

    processResources {
        val tokens = mapOf(
            "project.version"    to project.version,
            "project.groupId"    to project.group,
            "project.artifactId" to "Lavalink-Server",
            "env.BUILD_TIME"     to System.currentTimeMillis().toString()
        )

        filter(ReplaceTokens::class, mapOf("tokens" to tokens))
    }

    // https://stackoverflow.com/questions/41444916/multiple-artifacts-issue-with-deploying-zip-to-nexus
    named<AbstractArchiveTask>("bootDistTar") {
        archiveClassifier.set("bootTar")
    }

    named<AbstractArchiveTask>("bootDistZip") {
        archiveClassifier.set("bootZip")
    }

    named<Test>("test") {
        useJUnitPlatform()
    }

    withType<BootJar> {
        archiveFileName.set("Lavalink.jar")
    }

    withType<BootRun> {
        dependsOn("compileTestJava")

        //pass in custom jvm args
        // source: https://stackoverflow.com/a/25079415
        // example: ./gradlew bootRun -PjvmArgs="--illegal-access=debug -Dwhatever=value"
        if (project.hasProperty("jvmArgs")) {
            val args = project.property("jvmArgs")
                .toString()
                .split("\\s".toPattern())

            jvmArgs?.addAll(args)
        }
    }
}

@SuppressWarnings("GrMethodMayBeStatic")
fun versionFromTag(): String = Grgit.open(mapOf("currentDir" to project.rootDir)).use { git ->
    val headTag = git.tag
        .list()
        .find { it.commit.id == git.head().id }

    val clean = git.status().isClean || System.getenv("CI") != null
    if (!clean) {
        println("Git state is dirty, setting version as snapshot.")
    }

    return if (headTag != null && clean) headTag.name else "${git.head().id}-SNAPSHOT"
}
