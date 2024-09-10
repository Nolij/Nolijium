operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
	version("neoforge21_minecraft_version"())
	
	runs.off = true
	
	neoForge {
		loader("neoforge21_neoforge_version"())
		mixinConfig("nolijium-common.mixins.json")
		accessTransformer(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))
	}

	source {
		sourceGenerator.jvmArgs = listOf("-Xmx4G")
	}

	mappings {
		mojmap()
		parchment(mcVersion = "1.21", version = "neoforge21_parchment_version"())
	}
}

repositories {
	maven("https://maven.blamejared.com")
}

dependencies {
	compileOnly(project(":stubs"))
}