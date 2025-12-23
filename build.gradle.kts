import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import net.neoforged.moddevgradle.dsl.ModDevExtension
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.neoforged.moddevgradle.legacyforge.dsl.MixinExtension
import kotlin.text.compareTo

plugins {
	id("com.github.gmazzo.buildconfig") version("5.6.7")
	id("net.neoforged.moddev") version("2.0.134") apply false
	id("net.neoforged.moddev.legacyforge") version("2.0.134") apply false
}

operator fun String.invoke(): String = project.properties[this] as? String ?: error("Property $this not found")

version = "0.3.0"

buildConfig {
	className("NolijiumConstants")
	packageName("dev.nolij.nolijium.impl")

	useJavaOutput()

	buildConfigField("MOD_ID", "mod_id"())
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

tasks.withType<JavaCompile> {
	options.compilerArgs.addAll(arrayOf("-Xplugin:Manifold no-bootstrap"))
}

val isLegacyForge = sc.current.parsed <= "1.20.1"

tasks.named<ProcessResources>("processResources") {
	inputs.file(rootDir.resolve("gradle.properties"))
	inputs.property("version", project.version.toString())

	filteringCharset = "UTF-8"

	val props = mutableMapOf<String, String>()
	props.putAll(project.properties
		.filterValues { value -> value is String }
		.mapValues { entry -> entry.value as String })
	props["mod_version"] = project.version.toString()
	
	if (isLegacyForge) {
		exclude("META-INF/neoforge.mods.toml")
	} else {
		exclude("META-INF/mods.toml")
	}
	
	filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "META-INF/mods.toml")) {
		expand(props)
	}
}

val disableRecomp = System.getenv("CI") == "true"

val modDevExtension: ModDevExtension = if (isLegacyForge) {
	apply(plugin = "net.neoforged.moddev.legacyforge")
	val legacyForge = project.extensions.getByName("legacyForge") as LegacyForgeExtension
	legacyForge.enable {
		forgeVersion = "minecraft_version"() + "-" + "lexforge_version"()
		isDisableRecompilation = disableRecomp
	}
	legacyForge
} else {
	apply(plugin = "net.neoforged.moddev")
	val neoForge = project.extensions.getByName("neoForge") as NeoForgeExtension
	neoForge.enable {
		version = "neoforge_version"()
		isDisableRecompilation = disableRecomp
	}
	neoForge
}

modDevExtension.apply {
	project.properties["parchment_version"]?.toString().let { parchmentVer ->
		parchment {
			minecraftVersion = "minecraft_version"()
			mappingsVersion = parchmentVer
		}
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

// Required for MDG to work correctly with Stonecutter
tasks.named("createMinecraftArtifacts") {
	dependsOn("stonecutterGenerate")
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

	annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
	
	shade("dev.nolij:zson:${"zson_version"()}")
	shade("dev.nolij:libnolij:${"libnolij_version"()}")
	
	if (isLegacyForge) {
		compileOnly("io.github.llamalad7:mixinextras-common:${"mixinextras_version"()}")
		annotationProcessor("io.github.llamalad7:mixinextras-common:${"mixinextras_version"()}")
		implementation("io.github.llamalad7:mixinextras-forge:${"mixinextras_version"()}")
		"jarJar"("io.github.llamalad7:mixinextras-forge:${"mixinextras_version"()}")
	}
	
	val embeddiumConfig = configurations.named(if (isLegacyForge) { "compileOnly" } else { "implementation" })

	embeddiumConfig("org.embeddedt:embeddium-${"minecraft_version"()}:${"embeddium_version"()}") {
		isTransitive = false
	}
	//shade("io.github.llamalad7:mixinextras-common:${"mixinextras_version"()}")
}

if (isLegacyForge) {
	val mixin = project.extensions.getByType(MixinExtension::class)
	mixin.add(sourceSets.main.get(), "nolijium-refmap.json")
	mixin.config("nolijium.mixins.json")
}

stonecutter {
	replacements.string(current.parsed >= "1.21") {
		replace("net.minecraftforge.client", "net.neoforged.neoforge.client")
		replace("net.minecraftforge.eventbus", "net.neoforged.bus")
		replace("net.minecraftforge.fml", "net.neoforged.fml")
		replace("net.minecraftforge.event", "net.neoforged.neoforge.event")
		replace("net.minecraftforge.api.distmarker", "net.neoforged.api.distmarker")
		replace("me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage", "org.embeddedt.embeddium.api.options.structure.OptionStorage")
		replace("org.embeddedt.embeddium.client.gui.options.OptionIdentifier", "org.embeddedt.embeddium.api.options.OptionIdentifier")
		replace("me.jellysquid.mods.sodium.client.gui.options.binding.", "org.embeddedt.embeddium.api.options.binding.")
		replace("me.jellysquid.mods.sodium.client.gui.options.control.", "org.embeddedt.embeddium.api.options.control.")
		replace("me.jellysquid.mods.sodium.client.gui.options.", "org.embeddedt.embeddium.api.options.structure.")
	}
}
