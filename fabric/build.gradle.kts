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
	combineWith(project(":common").sourceSets.main.get())
	
	version("fabric_minecraft_version"())
	
	fabric {
		loader("fabric_loader_version"())
	}

	source {
		sourceGenerator.jvmArgs = listOf("-Xmx4G")
	}

	mappings {
		intermediary()
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
	maven("https://maven.terraformersmc.com/releases/")
	maven("https://maven.blamejared.com")
}

dependencies {
	compileOnly(project(":stubs"))
	
	minecraftLibraries("dev.nolij:zson:${"zson_version"()}")

	modCompileOnly(fabricApi.fabricModule("fabric-lifecycle-events-v1", "fabric_api_version"()))
	modCompileOnly(fabricApi.fabricModule("fabric-rendering-v1", "fabric_api_version"()))
	modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:${"fabric_api_version"()}")
	
	mod("com.terraformersmc:modmenu:${"modmenu_version"()}")

//	modRuntimeOnly("dev.nolij:zume:${"zume_version"()}")
}