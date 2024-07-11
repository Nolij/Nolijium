@file:Suppress("UnstableApiUsage")
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.nolij.zumegradle.DeflateAlgorithm
import dev.nolij.zumegradle.JsonShrinkingType
import dev.nolij.zumegradle.MixinConfigMergingTransformer
import dev.nolij.zumegradle.CompressJarTask
import kotlinx.serialization.encodeToString
import me.modmuss50.mpp.HttpUtils
import me.modmuss50.mpp.PublishModTask
import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.discord.DiscordAPI
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.internal.immutableListOf
import org.ajoberstar.grgit.Tag
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import xyz.wagyourtail.unimined.api.unimined
import java.nio.file.Files
import java.time.ZonedDateTime

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow")
    id("me.modmuss50.mod-publish-plugin")
    id("xyz.wagyourtail.unimined")
    id("org.ajoberstar.grgit")
}

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

enum class ReleaseChannel(
    val suffix: String? = null,
    val releaseType: ReleaseType? = null,
    val deflation: DeflateAlgorithm = DeflateAlgorithm.ZOPFLI,
    val json: JsonShrinkingType = JsonShrinkingType.MINIFY,
    val proguard: Boolean = true,
) {
    DEV_BUILD(
        suffix = "dev",
        deflation = DeflateAlgorithm.SEVENZIP,
        json = JsonShrinkingType.PRETTY_PRINT
    ),
    PRE_RELEASE("pre"),
    RELEASE_CANDIDATE("rc"),
    RELEASE(releaseType = ReleaseType.STABLE),
}

//region Git Versioning

val headDateTime: ZonedDateTime? = grgit.head()?.dateTime

val branchName = grgit.branch.current().name!!
val releaseTagPrefix = "release/"

val releaseTags = grgit.tag.list()
    .filter { tag -> tag.name.startsWith(releaseTagPrefix) }
    .sortedWith { tag1, tag2 ->
        if (tag1.commit.dateTime == tag2.commit.dateTime)
            if (tag1.name.length != tag2.name.length)
                return@sortedWith tag1.name.length.compareTo(tag2.name.length)
            else
                return@sortedWith tag1.name.compareTo(tag2.name)
        else
            return@sortedWith tag2.commit.dateTime.compareTo(tag1.commit.dateTime)
    }
    .dropWhile { tag -> headDateTime != null && tag.commit.dateTime > headDateTime }

val isExternalCI = (rootProject.properties["external_publish"] as String?).toBoolean()
val isRelease = rootProject.hasProperty("release_channel") || isExternalCI
val releaseIncrement = if (isExternalCI) 0 else 1
val releaseChannel: ReleaseChannel =
    if (isExternalCI) {
        val tagName = releaseTags.first().name
        val suffix = """\-(\w+)\.\d+$""".toRegex().find(tagName)?.groupValues?.get(1)
        if (suffix != null)
            ReleaseChannel.values().find { channel -> channel.suffix == suffix }!!
        else
            ReleaseChannel.RELEASE
    } else {
        if (isRelease)
            ReleaseChannel.valueOf("release_channel"())
        else
            ReleaseChannel.DEV_BUILD
    }

println("Release Channel: $releaseChannel")

val minorVersion = "mod_version"()
val minorTagPrefix = "${releaseTagPrefix}${minorVersion}."

val patchHistory = releaseTags
    .map { tag -> tag.name }
    .filter { name -> name.startsWith(minorTagPrefix) }
    .map { name -> name.substring(minorTagPrefix.length) }

val maxPatch = patchHistory.maxOfOrNull { it.substringBefore('-').toInt() }
val patch =
    maxPatch?.plus(
        if (patchHistory.contains(maxPatch.toString()))
            releaseIncrement
        else
            0
    ) ?: 0
var patchAndSuffix = patch.toString()

if (releaseChannel.suffix != null) {
    patchAndSuffix += "-${releaseChannel.suffix}"

    if (isRelease) {
        patchAndSuffix += "."

        val maxBuild = patchHistory
            .mapNotNull { it.removePrefix(patchAndSuffix).toIntOrNull() }
            .maxOrNull()

        val build = (maxBuild?.plus(releaseIncrement)) ?: 1
        patchAndSuffix += build.toString()
    }
}

//endregion

ZumeGradle.version = "${minorVersion}.${patchAndSuffix}"
println("Zume Version: ${ZumeGradle.version}")

rootProject.group = "maven_group"()
rootProject.version = ZumeGradle.version

base {
    archivesName = "mod_id"()
}

fun arrayOfProjects(vararg projectNames: String): Array<String> {
    return listOf(*projectNames).filter { p -> findProject(p) != null }.toTypedArray()
}
val uniminedImpls = arrayOfProjects(
    "forge",
)

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral {
            content {
                excludeGroup("ca.weblite")
            }
        }
        maven("https://repo.spongepowered.org/maven")
        maven("https://jitpack.io/")
        exclusiveContent {
            forRepository { maven("https://api.modrinth.com/maven") }
            filter {
                includeGroup("maven.modrinth")
            }
        }
        maven("https://maven.blamejared.com")
    }

    tasks.withType<JavaCompile> {
        if (name !in arrayOf("compileMcLauncherJava", "compilePatchedMcJava")) {
            options.encoding = "UTF-8"
            sourceCompatibility = "17"
            options.release = 17
            javaCompiler = javaToolchains.compilerFor {
                languageVersion = JavaLanguageVersion.of(17)
            }
            options.compilerArgs.addAll(arrayOf("-Xplugin:Manifold no-bootstrap"))
        }
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:${"jetbrains_annotations_version"()}")

        compileOnly("systems.manifold:manifold-rt:${"manifold_version"()}")
        annotationProcessor("systems.manifold:manifold-exceptions:${"manifold_version"()}")

        testCompileOnly("systems.manifold:manifold-rt:${"manifold_version"()}")
        testAnnotationProcessor("systems.manifold:manifold-exceptions:${"manifold_version"()}")
    }

    tasks.processResources {
        inputs.file(rootDir.resolve("gradle.properties"))
        inputs.property("version", ZumeGradle.version)

        filteringCharset = "UTF-8"

        val props = mutableMapOf<String, String>()
        props.putAll(rootProject.properties
            .filterValues { value -> value is String }
            .mapValues { entry -> entry.value as String })
        props["mod_version"] = ZumeGradle.version

        filesMatching(immutableListOf("fabric.mod.json", "META-INF/mods.toml")) {
            expand(props)
        }
    }
}

subprojects {
    val subProject = this
    val implName = subProject.name

    group = "maven_group"()
    version = ZumeGradle.version

    base {
        archivesName = "${"mod_id"()}-${subProject.name}"
    }

    tasks.withType<GenerateModuleMetadata> {
        enabled = false
    }

    dependencies {
        implementation("dev.nolij:zson:${"zson_version"()}:downgraded-17")
    }

    if (implName in uniminedImpls) {
        apply(plugin = "xyz.wagyourtail.unimined")
        apply(plugin = "com.github.johnrengelman.shadow")

        unimined.minecraft(sourceSets["main"], lateApply = true) {
            combineWith(project(":api").sourceSets.main.get())

            if (implName != "primitive") {
                runs.config("server") {
                    disabled = true
                }

                runs.config("client") {
                    jvmArgs += "-Dnolijium.configPathOverride=${rootProject.file("nolijium.json5").absolutePath}"
                }
            }

            defaultRemapJar = true
        }

        val outputJar = tasks.register<ShadowJar>("outputJar") {
            group = "build"

            val remapJarTasks = tasks.withType<RemapJarTask>()
            dependsOn(remapJarTasks)
            mustRunAfter(remapJarTasks)
            remapJarTasks.forEach { remapJar ->
                remapJar.archiveFile.also { archiveFile ->
                    from(zipTree(archiveFile))
                    inputs.file(archiveFile)
                }
            }

            configurations = emptyList()
            archiveClassifier = "output"
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }

        tasks.assemble {
            dependsOn(outputJar)
        }
    }
}

unimined.minecraft {
    version("minecraft_version"())

    runs.off = true

    minecraftForge {
        loader("forge_version"())
    }

    mappings {
        mojmap()
	    parchment(mcVersion = "minecraft_version"(), version = "parchment_version"())
    }

    defaultRemapJar = false
}

val shade: Configuration by configurations.creating {
    configurations.compileClasspath.get().extendsFrom(this)
    configurations.runtimeClasspath.get().extendsFrom(this)
}

dependencies {
    shade("dev.nolij:zson:${"zson_version"()}:downgraded-17")

    compileOnly("org.apache.logging.log4j:log4j-core:${"log4j_version"()}")

    compileOnly(project(":stubs"))

    implementation(project(":api"))

    uniminedImpls.forEach {
        implementation(project(":${it}")) { isTransitive = false }
    }
}

tasks.jar {
    enabled = false
	manifest {
		attributes["MixinConfigs"] = "nolijium-forge.mixins.json" 
	}
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    group = "build"

    archiveClassifier = "sources"
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    from("LICENSE") {
        rename { "${it}_${"mod_id"()}" }
    }

	if (releaseChannel.proguard) {
		dependsOn(compressJar)
		from(compressJar.get().mappingsFile!!) {
			rename { "mappings.txt" }
		}
	}

    listOf(
        sourceSets,
        project(":api").sourceSets,
        uniminedImpls.flatMap { project(":${it}").sourceSets }
    ).flatten().forEach {
        from(it.allSource) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
    }
}

tasks.shadowJar {
    transform(MixinConfigMergingTransformer::class.java) {
        modId = "mod_id"()
        packageName = "dev.nolij.nolijium.mixin"
    }

    from("LICENSE") {
        rename { "${it}_${"mod_id"()}" }
    }

    exclude("*.xcf")
    exclude("LICENSE_zson")

    configurations = immutableListOf(shade)
    archiveClassifier = null
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    val apiJar = project(":api").tasks.jar
    dependsOn(apiJar)
    from(zipTree(apiJar.get().archiveFile.get())) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

    uniminedImpls.map { project(it).tasks.named<ShadowJar>("outputJar").get() }.forEach { implJarTask ->
        dependsOn(implJarTask)
        from(zipTree(implJarTask.archiveFile.get())) {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            exclude("fabric.mod.json", "pack.mcmeta")
        }
    }

    relocate("dev.nolij.zson", "dev.nolij.nolijium.zson")
    if (releaseChannel.proguard) {
        relocate("dev.nolij.nolijium.mixin", "nolijium.mixin")
    }
}

val compressJar = tasks.register<CompressJarTask>("compressJar") {
    dependsOn(tasks.shadowJar)
    group = "build"

    val shadowJar = tasks.shadowJar.get()
    inputJar = shadowJar.archiveFile.get().asFile

    deflateAlgorithm = releaseChannel.deflation
    jsonShrinkingType = releaseChannel.json
    if (releaseChannel.proguard) {
        useProguard(uniminedImpls.flatMap { implName -> project(":$implName").unimined.minecrafts.values })
    }
}

tasks.assemble {
    dependsOn(compressJar, sourcesJar)
}

afterEvaluate {
    publishing {
        repositories {
            if (!System.getenv("local_maven_url").isNullOrEmpty())
                maven(System.getenv("local_maven_url"))
        }

        publications {
            create<MavenPublication>("mod_id"()) {
                artifact(compressJar.get().outputJar)
                artifact(sourcesJar)
            }
        }
    }

    tasks.withType<AbstractPublishToMaven> {
        dependsOn(compressJar, sourcesJar)
    }

    fun getChangelog(): String {
        return file("CHANGELOG.md").readText()
    }

    publishMods {
        file = compressJar.get().outputJar
        additionalFiles.from(sourcesJar.get().archiveFile)
        type = releaseChannel.releaseType ?: ALPHA
        displayName = ZumeGradle.version
        version = ZumeGradle.version
        changelog = getChangelog()

        modLoaders.addAll("forge")
        dryRun = !isRelease

        github {
            accessToken = providers.environmentVariable("GITHUB_TOKEN")
            repository = "Nolij/Nolijium"
            commitish = branchName
            tagName = releaseTagPrefix + ZumeGradle.version
        }

        if (dryRun.get() || releaseChannel.releaseType != null) {
            modrinth {
                accessToken = providers.environmentVariable("MODRINTH_TOKEN")
                projectId = "KstN3eSL"

                minecraftVersions.add("1.18.2")
            }

            curseforge {
                val cfAccessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
                accessToken = cfAccessToken
                projectId = "969602"
                projectSlug = "nolijium"

	            minecraftVersions.add("1.18.2")
            }

            discord {
                webhookUrl = providers.environmentVariable("DISCORD_WEBHOOK").orElse("")

                username = "Nolijium Releases"

                avatarUrl = "https://github.com/Nolij/Nolijium/raw/master/api/src/main/resources/icon.png"

                content = changelog.map { changelog ->
                    "# Nolijium ${ZumeGradle.version} has been released!\nChangelog: ```md\n${changelog}\n```"
                }

                setPlatforms(platforms["modrinth"], platforms["github"], platforms["curseforge"])
            }
        }
    }

    tasks.withType<PublishModTask> {
        dependsOn(compressJar, sourcesJar)
    }

    tasks.publishMods {
        if (!publishMods.dryRun.get() && releaseChannel.releaseType == null) {
            doLast {
                val http = HttpUtils()

                val currentTag: Tag? = releaseTags.getOrNull(0)
                val buildChangeLog =
                    grgit.log {
                        if (currentTag != null)
                            excludes = listOf(currentTag.name)
                        includes = listOf("HEAD")
                    }.joinToString("\n") { commit ->
                        val id = commit.abbreviatedId
                        val message = commit.fullMessage.substringBefore('\n').trim()
                        val author = commit.author.name
                        "- [${id}] $message (${author})"
                    }

                val compareStart = currentTag?.name ?: grgit.log().minBy { it.dateTime }.id
                val compareEnd = releaseTagPrefix + ZumeGradle.version
                val compareLink = "https://github.com/Nolij/Nolijium/compare/${compareStart}...${compareEnd}"

                val webhookUrl = providers.environmentVariable("DISCORD_WEBHOOK")
                val releaseChangeLog = getChangelog()
                val file = publishMods.file.asFile.get()

                var content = "# [Nolijium Test Build ${ZumeGradle.version}]" +
                        "(<https://github.com/Nolij/Nolijium/releases/tag/${releaseTagPrefix}${ZumeGradle.version}>) has been released!\n" +
                        "Changes since last build: <${compareLink}>"

                if (buildChangeLog.isNotBlank())
                    content += " ```md\n${buildChangeLog}\n```"
                content += "\nChanges since last release: ```md\n${releaseChangeLog}\n```"

                val webhook = DiscordAPI.Webhook(
                    content,
                    "Nolijium Test Builds",
	                "https://github.com/Nolij/Nolijium/raw/master/api/src/main/resources/icon.png"
                )

                val bodyBuilder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("payload_json", http.json.encodeToString(webhook))
                    .addFormDataPart("files[0]", file.name, file.asRequestBody("application/java-archive".toMediaTypeOrNull()))

                var fileIndex = 1
                for (additionalFile in publishMods.additionalFiles) {
                    bodyBuilder.addFormDataPart(
                        "files[${fileIndex++}]",
                        additionalFile.name,
                        additionalFile.asRequestBody(Files.probeContentType(additionalFile.toPath()).toMediaTypeOrNull())
                    )
                }

                val requestBuilder = Request.Builder()
                    .url(webhookUrl.get())
                    .post(bodyBuilder.build())
                    .header("Content-Type", "multipart/form-data")

                http.httpClient.newCall(requestBuilder.build()).execute().close()
            }
        }
    }
}