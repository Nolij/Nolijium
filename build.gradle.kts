import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import net.neoforged.moddevgradle.dsl.ModDevExtension
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.neoforged.moddevgradle.legacyforge.dsl.MixinExtension
import org.taumc.gradle.minecraft.MinecraftVersion
import org.taumc.gradle.minecraft.ModEnvironment
import org.taumc.gradle.minecraft.ModLoader
import org.taumc.gradle.publishing.api.artifact.Relation
import org.taumc.gradle.publishing.publishing

plugins {
	id("com.github.gmazzo.buildconfig")
	id("org.taumc.gradle.versioning")
	id("org.taumc.gradle.publishing")
	id("net.neoforged.moddev") apply(false)
	id("net.neoforged.moddev.legacyforge") apply(false)
	id("xyz.wagyourtail.jvmdowngrader")
	`maven-publish`
}

operator fun String.invoke(): String = project.properties[this] as? String ?: error("Property $this not found")

val mcVersion = MinecraftVersion.get("minecraft_version"()) ?: error("Invalid `minecraft_version`!")
val modLoader = ModLoader.get("mod_loader"()) ?: error("Invalid `mod_loader`!")
val javaVersion = JavaVersion.valueOf("java_version"())

project.group = "maven_group"()
project.version = tau.versioning.subVersion("mc.${mcVersion.conciseName}")
println("Nolijium Version: ${tau.versioning.version}")

base {
	archivesName = "mod_id"()
}

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

tasks.named<ProcessResources>("processResources") {
	inputs.file(rootDir.resolve("gradle.properties"))
	inputs.property("version", project.version.toString())

	filteringCharset = "UTF-8"

	val props = mutableMapOf<String, String>()
	props.putAll(project.properties
		.filterValues { value -> value is String }
		.mapValues { entry -> entry.value as String })
	props["mod_version"] = project.version.toString()
	
	exclude(when (modLoader) {
		ModLoader.LEXFORGE -> "META-INF/neoforge.mods.toml"
		ModLoader.NEOFORGE -> "META-INF/mods.toml"
		else -> throw AssertionError()
	})
	
	filesMatching(listOf("META-INF/neoforge.mods.toml", "META-INF/mods.toml")) {
		expand(props)
	}
}

val disableRecomp = System.getenv("CI") == "true"

val modDevExtension: ModDevExtension = when (modLoader) {
	ModLoader.LEXFORGE -> {
		apply(plugin = "net.neoforged.moddev.legacyforge")
		val legacyForge = project.extensions.getByName("legacyForge") as LegacyForgeExtension
		legacyForge.enable {
			forgeVersion = mcVersion.mojangName + "-" + "mod_loader_version"()
			isDisableRecompilation = disableRecomp
		}
		legacyForge
	}
	ModLoader.NEOFORGE -> {
		apply(plugin = "net.neoforged.moddev")
		val neoForge = project.extensions.getByName("neoForge") as NeoForgeExtension
		neoForge.enable {
			version = "mod_loader_version"()
			isDisableRecompilation = disableRecomp
		}
		neoForge
	}
	else -> throw AssertionError()
}

modDevExtension.apply {
	project.properties["parchment_version"]?.toString().let { parchmentVer ->
		parchment {
			minecraftVersion = mcVersion.mojangName
			mappingsVersion = parchmentVer
		}
	}
	
	runs {
		create("client") {
			client()
			jvmArgument("-Dnolijium.configPathOverride=${rootProject.file("nolijium.json5").absolutePath}")
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

val lib: Configuration by configurations.creating {
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
			@Suppress("UnstableApiUsage")
			includeGroupAndSubgroups("dev.nolij")
		}
	}
}

dependencies {
	compileOnly("systems.manifold:manifold-rt:${"manifold_version"()}")
	annotationProcessor("systems.manifold:manifold-exceptions:${"manifold_version"()}")

	testCompileOnly("systems.manifold:manifold-rt:${"manifold_version"()}")
	testAnnotationProcessor("systems.manifold:manifold-exceptions:${"manifold_version"()}")
	
	val jarJar by configurations.getting
	
	listOf("dev.nolij:zson:${"zson_version"()}", "dev.nolij:libnolij:${"libnolij_version"()}").forEach { 
		lib(it)
		jarJar(it)
	}
	
	if (modLoader == ModLoader.LEXFORGE) {
		annotationProcessor("org.spongepowered:mixin:${"mixin_version"()}:processor")
		compileOnly("io.github.llamalad7:mixinextras-common:${"mixinextras_version"()}")
		annotationProcessor("io.github.llamalad7:mixinextras-common:${"mixinextras_version"()}")
		implementation("io.github.llamalad7:mixinextras-forge:${"mixinextras_version"()}")
		jarJar("io.github.llamalad7:mixinextras-forge:${"mixinextras_version"()}")
	}
	
	val embeddiumConfig = configurations.named(if (modLoader == ModLoader.LEXFORGE) { "compileOnly" } else { "implementation" })

	embeddiumConfig("org.embeddedt:embeddium-${mcVersion.mojangName}:${"embeddium_version"()}") {
		isTransitive = false
	}
}

if (modLoader == ModLoader.LEXFORGE) {
	val mixin = project.extensions.getByType(MixinExtension::class)
	mixin.add(sourceSets.main.get(), "nolijium-refmap.json")
	mixin.config("nolijium.mixins.json")
}

jvmdg.defaultShadeTask {
	enabled = false
}

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	includeEmptyDirs = false
}

val outputJar = if (javaVersion < JavaVersion.current()) {
	tasks.jar {
		archiveClassifier = "reobf"
	}
	
	jvmdg.defaultTask {
		dependsOn(tasks.jar)
		
		inputFile = provider { tasks.jar.get().archiveFile.get() }
		downgradeTo = javaVersion
		
		archiveClassifier = ""
	}
	
	jvmdg.defaultTask
} else {
	tasks.jar
}

outputJar {
	from(rootProject.file("LICENSE")) {
		rename { "${it}_${"mod_id"()}" }
	}
}

val sourcesJar by tasks.registering(Jar::class) {
	group = "build"

	archiveClassifier = "sources"

	from(rootProject.file("LICENSE")) {
		rename { "${it}_${"mod_id"()}" }
	}
	
	project.sourceSets.forEach { 
		from(it.allSource) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	}
}

tasks.assemble {
	dependsOn(outputJar, sourcesJar)
}

stonecutter {
	replacements.string(current.parsed >= "21.1") {
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

publishing {
	repositories {
		if (!System.getenv("local_maven_url").isNullOrEmpty())
			maven(System.getenv("local_maven_url"))
	}

	publications {
		create<MavenPublication>("mod_id"()) {
			artifact(outputJar)
			artifact(sourcesJar)
		}
	}
}

rootProject.tau.publishing.modArtifact("${modLoader.displayName} ${mcVersion.conciseName}") {
	files(provider { outputJar.get().archiveFile }, provider { sourcesJar.get().archiveFile })

	version = tau.versioning.version

	minecraftVersionRange = mcVersion.mojangName
	javaVersions.add(javaVersion)

	environment = ModEnvironment.CLIENT_ONLY
	modLoaders.add(modLoader)

	relations.add(Relation(id = "zume", type = Relation.Type.INTEGRATES_WITH))
	relations.add(Relation(id = "embeddium", type = Relation.Type.INTEGRATES_WITH))
}