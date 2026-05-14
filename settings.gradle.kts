pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins { kotlin("multiplatform") version "2.3.21" }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0" }

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "quote-kotlin"

val procMacro2Local = file("../proc-macro2-kotlin")
if (procMacro2Local.exists()) {
    includeBuild(procMacro2Local) {
        dependencySubstitution {
            substitute(module("io.github.kotlinmania:proc-macro2-kotlin")).using(project(":"))
        }
    }
}
