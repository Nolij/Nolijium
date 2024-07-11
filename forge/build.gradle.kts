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
	version("minecraft_version"())
	
	minecraftForge {
		loader("forge_version"())
	}

	source {
		sourceGenerator.jvmArgs = listOf("-Xmx4G")
	}

	mappings {
		mojmap()
		parchment(mcVersion = "minecraft_version"(), version = "parchment_version"())
	}

	mods {
		remap(modCompileOnly)
		remap(modRuntimeOnly)
		remap(mod)
	}
}

repositories {
	mavenLocal()
	maven("https://maven.blamejared.com")
}

dependencies {
	compileOnly(project(":stubs"))
	annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.0")?.let { compileOnly(it) }
	// TODO: JARJAR PROPERLY
	implementation("io.github.llamalad7:mixinextras-forge:0.4.0")
	
	minecraftLibraries("dev.nolij:zson:${"zson_version"()}:downgraded-17")
	
	mod("org.embeddedt:embeddium-1.18.2:${"embeddium_neoforge_version"()}") {
		isTransitive = false
	}

	modRuntimeOnly("dev.nolij:zume:${"zume_version"()}")
}