# IMPORTANT LICENSE NOTICE

By using this project in any form, you hereby give your "express assent" for the terms of the license of this project (see [License](#license)), and acknowledge that I (the author of this project) have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License".

# Nolijium

A collection of various QoL enhancements with (optional) Embeddium support and integration, written by Nolij.

# Features

- Maximum chat history customization
- Advanced tooltip info toggle
- Light level overlay toggle (NeoForge 21+ exclusive)
- Star renderer customization options
- Fog customization options
- HUD with accurate & efficient FPS tracking including 0.1% lows
- [MC-26678](https://bugs.mojang.com/browse/MC-26678) revert
- Potion revert (adds back potion glint and reverts colours)
- Opaque block outlines toggle
- Texture animation toggle
- Sky rendering toggle
- Weather toggles
- Font shadow toggle
- Toast toggles
- Particle toggles
- Chroma block outlines, tooltips, & HUD
- Global config file (config is synced between instances on the same machine)
- Optional Embeddium integration

# Credits (in no particular order)

- [embeddedt](https://github.com/embeddedt): Light Level Overlay, Embeddium (which inspired the name of this mod, and also allowed me to yet again avoid writing a config screen), much behind-the-scenes guidance, help debugging weird errors, and education on Mixin, the JVM, and modding in general
- [rdh](https://github.com/rhysdh540): Many contributions to the buildscript
- [CelestialAbyss](https://github.com/CelestialAbyss): The excellent icon (also made the equally amazing icons for [Embeddium](https://github.com/embeddedt/embeddium) and [TauMC](https://github.com/TauMC))
- [LlamaLad7](https://github.com/LlamaLad7): MixinExtras, which is heavily used by Nolijium and bundled to support monojar
- [AlexSanech](https://github.com/Alexander317): Russian translation
- [OrzMiku](https://github.com/OrzMiku): Simplified Chinese translation
- [cutiegin](https://github.com/cutiegin): Ukrainian translation

# FAQ

#### Q: Some of these features seem familiar...

A: Inspiration was taken from a few sources, but 100% of the code in this mod is either original to this mod (ie written specifically for this mod), or was used with explicit permission from code authors (the only "exception" to this might be bundled MixinExtras, but the README for MixinExtras literally provides instructions for bundling, so I consider it reasonable. LlamaLad7 is of course welcome to request I stop bundling MixinExtras).

#### Q: Where is the config?

A: You'll find the config at `.minecraft/global/nolijium.json5` (note that this is the default `.minecraft` folder, not the instance `.minecraft`). You can modify the file while the game is running, and the config will be automatically reloaded. The config button in the NeoForge mods screen will open this text file for you in your system's default text editor. Nolijium also has optional Embeddium options screen integration, which is probably the easiest way to modify this mod's config.

#### Q: discord where
A: https://discord.gg/6ZjX4mvCMR

#### Q: What version is this for?

A: This mod supports LexForge 20.1 and NeoForge 21+. It is built off of [Zume](https://github.com/Nolij/Zume)'s buildscript though, so it should be feasible to add support for Fabric and/or older Minecraft versions. PRs which extend platform support and maintain monojar support are welcome.

#### Q: What's a monojar?

A: Using a lot of buildscript magic, one need not have a different JAR for each supported version. A monojar is a single JAR which can be used on all supported platforms. I started using this concept in [Zume](https://github.com/Nolij/Zume) (which is why 90% of Nolijium's buildscript is a copy of [Zume](https://github.com/Nolij/Zume)'s buildscript). Please note that this is not very feasible for most mods, so I would strongly recommend against asking other mod authors to support monojars. While I'm sure many other mod developers are _capable_ of supporting monojar, it's entirely unreasonable to expect this of those mod developers, as it introduces numerous headaches just working on a small mod. Monojar-ifying large mods (especially content mods) would take far more effort than could be considered even remotely reasonable. That being said, I welcome other mod developers who do want to give it a try to reach out to me if they have any questions.

#### Q: What kind of weird license is this?

A: OSL-3.0 is the closest equivalent to a LAGPL I could find. AGPL and GPL are incompatible with Minecraft, and LGPL doesn't protect network use. OSL-3.0 protects network use and is compatible with Minecraft.

#### Q: Why though? It's so strict!!!!

A: This is, and will remain, free, copyleft software. Any requests to change the license other than to make it even stronger will be denied immediately (unfortunately GPL and AGPL aren't compatible with Minecraft due to linking restrictions, as much as I'd like to use them). Even in situations where I use parts of other projects with more "permissive" licenses, I will treat them as copyleft, free software.

# License

This project is licensed under OSL-3.0. For more information, see [LICENSE](LICENSE).
