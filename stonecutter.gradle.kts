import org.taumc.gradle.publishing.api.PublishChannel

plugins {
    id("dev.kikugie.stonecutter")
	id("org.taumc.gradle.versioning")
	id("org.taumc.gradle.publishing")
}
stonecutter active "21.1"

operator fun String.invoke(): String = project.properties[this] as? String ?: error("Property $this not found")

project.group = "maven_group"()
project.version = tau.versioning.version("mod_version"(), project.properties["release_channel"])

tau.publishing.publish {
	useTauGradleVersioning()
	changelog = rootProject.file("CHANGELOG.md").readText()

	github {
		supportAllChannels()

		accessToken = providers.environmentVariable("GITHUB_TOKEN")
		repository = "Nolij/Nolijium"
		tagName = tau.versioning.releaseTag
	}

	curseforge {
		supportChannels(PublishChannel.RELEASE)

		accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
		projectID = 969602
		projectSlug = "nolijium"
	}

	modrinth {
		supportChannels(PublishChannel.RELEASE)

		accessToken = providers.environmentVariable("MODRINTH_TOKEN")
		projectID = "KstN3eSL"
		projectSlug = "nolijium"
	}

	val iconURL = "https://github.com/Nolij/Nolijium/raw/master/api/src/main/resources/icon.png"

	discord {
		supportAllChannelsExcluding(PublishChannel.RELEASE)

		webhookURL = providers.environmentVariable("DISCORD_WEBHOOK")
		avatarURL = iconURL

		testBuildPreset(modName = "Nolijium", repoURL = "https://github.com/Nolij/Nolijium")
	}

	discord {
		supportChannels(PublishChannel.RELEASE)

		webhookURL = providers.environmentVariable("DISCORD_WEBHOOK")
		avatarURL = iconURL

		releasePreset(modName = "Nolijium")
	}
}