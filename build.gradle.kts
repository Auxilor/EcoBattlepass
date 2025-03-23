plugins {
    java
    `java-library`
    `maven-publish`
    kotlin("jvm") version "2.1.10"
    id("com.gradleup.shadow") version "8.3.5"
    id("com.willfp.libreforge-gradle-plugin") version "1.0.2"
}

group = "ru.oftendev"
version = findProperty("version")!!
val libreforgeVersion = findProperty("libreforge-version")

base {
    archivesName.set(project.name)
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }

        maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        }
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
        compileOnly (fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
        compileOnly ("com.willfp:eco:6.75.2")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.0.5")
        implementation("com.willfp:ecomponent:1.4.1")
    }

    java {
        withSourcesJar()
    }

    tasks {
        shadowJar {
            exclude("kotlin/**")
            exclude("kotlinx/**")
            relocate("com.willfp.ecomponent", "com.willfp.ecoskills.ecomponent")
        }
        compileJava {
            options.isDeprecation = true
            options.encoding = "UTF-8"

            dependsOn(clean)
        }

        processResources {
            filesMatching(listOf("**plugin.yml", "**eco.yml")) {
                expand(
                    "version" to project.version,
                    "libreforgeVersion" to libreforgeVersion,
                    "pluginName" to rootProject.name
                )
            }
        }

        build {
            dependsOn(shadowJar)
        }
    }
}