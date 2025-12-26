pluginManagement {
    repositories {
	    gradlePluginPortal {
		    content {
			    excludeGroup("org.apache.logging.log4j")
		    }
	    }
	    mavenCentral()
	    maven("https://maven.taumc.org/releases")
	    maven("https://maven.kikugie.dev/releases")
    }
	
	plugins {
		fun property(name: String): String = extra[name] as? String ?: error("Property ${name} not found")
		
		id("org.gradle.toolchains.foojay-resolver-convention") version(property("foojay_resolver_convention_version"))
		id("dev.kikugie.stonecutter") version(property("stonecutter_version"))
		id("org.taumc.gradle.versioning") version(property("taugradle_version"))
		id("org.taumc.gradle.publishing") version(property("taugradle_version"))
		id("com.github.gmazzo.buildconfig") version(property("buildconfig_version"))
		id("net.neoforged.moddev") version(property("neogradle_version"))
		id("net.neoforged.moddev.legacyforge") version(property("neogradle_version"))
		id("xyz.wagyourtail.jvmdowngrader") version(property("jvmdg_version"))
	}
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
	id("dev.kikugie.stonecutter")
}

stonecutter {
	create(rootProject) {
		versions("20.1", "21.1")
		vcsVersion = "21.1"
	}
}

rootProject.name = "nolijium"