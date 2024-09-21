operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

val modCompileOnly: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
}
val modRuntimeOnly: Configuration by configurations.creating {
	configurations.runtimeClasspath.get().extendsFrom(this)
}
val mod: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
	configurations.runtimeClasspath.get().extendsFrom(this)
}

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

	mods {
		remap(modCompileOnly)
		remap(modRuntimeOnly)
		remap(mod)
	}
}

repositories {
	maven("https://maven.blamejared.com")
}

dependencies {
	compileOnly(project(":stubs"))
}