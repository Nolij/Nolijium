operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
	version("neoforge21_minecraft_version"())
	
	runs.off = true
	
	neoForge {
		loader("neoforge21_neoforge_version"())
		mixinConfig("nolijium-common.mixins.json")
	}

	source {
		sourceGenerator.jvmArgs = listOf("-Xmx4G")
	}

	mappings {
		mojmap()
		parchment(mcVersion = "neoforge21_minecraft_version"(), version = "neoforge21_parchment_version"())
	}
}

repositories {
	maven("https://maven.blamejared.com")
}

dependencies {
	compileOnly(project(":stubs"))
}