plugins {
    id("dev.kikugie.stonecutter")
	id("org.taumc.gradle.versioning")
	id("org.taumc.gradle.publishing")
}
stonecutter active "21.1"

operator fun String.invoke(): String = project.properties[this] as? String ?: error("Property $this not found")

project.group = "maven_group"()
project.version = tau.versioning.version("mod_version"(), project.properties["release_channel"])