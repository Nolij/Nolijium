pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
}

rootProject.name = "nolijium"

include("stubs")
include("api")
include("common")
include("neoforge")
include("lexforge20")
include("fabric")