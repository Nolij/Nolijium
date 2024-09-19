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
	
	version("lexforge20_minecraft_version"())
	
	runs.config("client") {
		javaVersion = JavaVersion.VERSION_21
	}
	
	minecraftForge {
		loader("lexforge20_lexforge_version"())
		mixinConfig("nolijium-lexforge20.mixins.json")
		accessTransformer(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))
	}

	mappings {
		mojmap()
		parchment(mcVersion = "lexforge20_minecraft_version"(), version = "lexforge20_parchment_version"())
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
	compileOnly("io.github.llamalad7:mixinextras-common:${"mixinextras_version"()}")
	runtimeOnly("io.github.llamalad7:mixinextras-forge:${"mixinextras_version"()}")

	minecraftLibraries("dev.nolij:zson:${"zson_version"()}")
	
	mod("org.embeddedt:embeddium-1.20.1:${"lexforge20_embeddium_version"()}")
	
	modRuntimeOnly("dev.nolij:zume:${"zume_version"()}")
}