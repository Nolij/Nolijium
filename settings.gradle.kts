pluginManagement {
    repositories {
        gradlePluginPortal()
	    maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
	id("dev.kikugie.stonecutter") version "0.8"
}

stonecutter {
	create(rootProject) {
		versions("1.20.1", "1.21.1")
		vcsVersion = "1.21.1"
	}
}

rootProject.name = "nolijium"