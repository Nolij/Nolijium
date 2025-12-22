plugins {
	id("com.github.gmazzo.buildconfig") version("5.6.7")
	id("net.neoforged.moddev") version("2.0.134")
}

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

version = "0.3.0"

buildConfig {
	className("NolijiumConstants")
	packageName("dev.nolij.nolijium.impl")

	useJavaOutput()

	buildConfigField("MOD_ID", "mod_id"())
}

tasks.withType<JavaCompile> {
	options.compilerArgs.addAll(arrayOf("-Xplugin:Manifold no-bootstrap"))
}

tasks.named<ProcessResources>("processResources") {
	inputs.file(rootDir.resolve("gradle.properties"))
	inputs.property("version", project.version.toString())

	filteringCharset = "UTF-8"

	val props = mutableMapOf<String, String>()
	props.putAll(rootProject.properties
		.filterValues { value -> value is String }
		.mapValues { entry -> entry.value as String })
	props["mod_version"] = project.version.toString()

	filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "META-INF/mods.toml")) {
		expand(props)
	}
}

neoForge {
	enable {
		version = "21.1.217"
		isDisableRecompilation = System.getenv("CI") == "true"
	}

	runs {
		create("client") {
			client()
		}
	}
	
	mods {
		create("nolijium") {
			sourceSet(sourceSets.main.get())
		}
	}
}

val shade: Configuration by configurations.creating {
	listOf("compileClasspath", "runtimeClasspath", "additionalRuntimeClasspath")
		.map { configurations.named(it).get() }.forEach { it.extendsFrom(this) }
}

repositories {
	maven("https://maven.blamejared.com")
	exclusiveContent { 
		forRepository {
			maven("https://maven.taumc.org/releases")
		}
		filter {
			includeGroupAndSubgroups("dev.nolij")
		}
	}
}

dependencies {
	compileOnly("systems.manifold:manifold-rt:${"manifold_version"()}")
	annotationProcessor("systems.manifold:manifold-exceptions:${"manifold_version"()}")

	testCompileOnly("systems.manifold:manifold-rt:${"manifold_version"()}")
	testAnnotationProcessor("systems.manifold:manifold-exceptions:${"manifold_version"()}")
	
	shade("dev.nolij:zson:${"zson_version"()}")
	shade("dev.nolij:libnolij:${"libnolij_version"()}")

	implementation("org.embeddedt:embeddium-1.21.1:${"neoforge21_embeddium_version"()}") {
		isTransitive = false
	}
	//shade("io.github.llamalad7:mixinextras-common:${"mixinextras_version"()}")
}