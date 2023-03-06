import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.ajoberstar.grgit.Grgit

buildscript {
    repositories {
        mavenLocal()
        maven("https://plugins.gradle.org/m2/")
        maven("https://repo.spring.io/plugins-release")
        maven("https://jitpack.io")
        maven("https://m2.dv8tion.net/releases")
    }

    dependencies {
        classpath("gradle.plugin.com.gorylenko.gradle-git-properties:gradle-git-properties:1.5.2")
        classpath("org.springframework.boot:spring-boot-gradle-plugin:2.6.6")
        classpath("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:2.6.2")
        classpath("com.adarshr:gradle-test-logger-plugin:1.6.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
        classpath("org.jetbrains.kotlin:kotlin-allopen:1.8.10")
    }
}

allprojects {
    group = "lavalink"
    version = versionFromTag()

    repositories {
        mavenCentral() // main maven repo
        mavenLocal()   // useful for developing
        maven("https://m2.dv8tion.net/releases")
        jcenter()
        maven("https://jitpack.io") // build projects directly from GitHub
    }
}

subprojects {
    if (project.hasProperty("includeAnalysis")) {
        project.logger.lifecycle("applying analysis plugins")
        apply(from = "../analysis.gradle")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
}

@SuppressWarnings("GrMethodMayBeStatic")
fun versionFromTag(): String {
    if (System.getenv("CI") == null) return "local-build"
    Grgit.open(mapOf("currentDir" to project.rootDir)).use { git ->
        val headTag = git.tag
            .list()
            .find { it.commit.id == git.head().id }

        val clean = git.status().isClean
        if (!clean) {
            println("Git state is dirty, setting version as snapshot.")
        }

        return if (headTag != null && clean) headTag.name else "${git.head().id}-SNAPSHOT"
    }
}
