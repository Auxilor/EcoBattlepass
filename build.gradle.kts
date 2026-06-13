import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.0"
    id("java")
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.3.1"
    id("com.willfp.libreforge-gradle-plugin") version "2.1.0"
}

group = "io.auxilor"
version = findProperty("version")!!
// useGradleVersions=true (set by release workflows) pins dependencies to the
// versions in gradle.properties; otherwise dev builds track the latest master snapshot.
val useGradleVersions = findProperty("useGradleVersions") == "true"
val libreforgeVersion = if (useGradleVersions) findProperty("libreforge-version") else "dev-SNAPSHOT"
val ecoVersion = if (useGradleVersions) findProperty("eco-version") else "dev-SNAPSHOT"

base {
    archivesName.set(project.name)
}

dependencies {
    project.project(project(":eco-core").path).subprojects {
        implementation(this)
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenCentral()
        mavenLocal {
            content {
                excludeGroup("com.willfp")
                excludeGroup("com.auxilor")
                excludeGroup("io.auxilor")
            }
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.codemc.io/repository/maven-releases/")
        maven("https://repo.codemc.io/repository/maven-snapshots/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

        compileOnly("com.willfp:eco:$ecoVersion")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.3")

        implementation("com.willfp:ecomponent:1.5.0")

        compileOnly (fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
    }

    java {
        withSourcesJar()
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks {
        shadowJar {
            exclude("META-INF/**")
            relocate("com.willfp.ecomponent", "io.auxilor.ecobattlepass.ecomponent")
            relocate("com.willfp.libreforge.loader", "io.auxilor.ecobattlepass.libreforge.loader")
            relocate("kotlin", "com.willfp.eco.libs.kotlin")
            relocate("kotlin.jvm", "com.willfp.eco.libs.kotlin.jvm")
            relocate("kotlin.coroutines", "com.willfp.eco.libs.kotlin.coroutines")
            relocate("kotlin.reflect", "com.willfp.eco.libs.kotlin.reflect")
        }

        compileKotlin {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
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
                    "libreforgeVersion" to libreforgeVersion!!,
                    "pluginName" to rootProject.name
                )
            }
        }

        build {
            dependsOn(shadowJar)
        }
    }
}