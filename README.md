# IMPORTANT LICENSE NOTICE

By using this project in any form, you hereby give your "express assent" for the terms of the license of this
project (see [License](#license)), and acknowledge that I (the author of this project) have fulfilled my obligation
under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to
the terms of this License".

# Nolijium

A collection of various QoL enhancements with (optional) Embeddium support and integration, written by Nolij.

# FAQ

#### Q: Some of these features seem familiar...

A: Inspiration was taken from a few sources, but 100% of the code in this mod was written by me. The only parts of 
this mod that were not entirely written by me are the parts of [Zume](https://github.com/Nolij/Zume)'s buildscript 
contributed by @rhysdh540, which I used for the buildscript of this mod, and the icon, which 100% of the credit for 
goes to the amazing @CelestialAbyss, who also made the equally amazing icons for [Embeddium](https://github.
com/embeddedt/embeddium) and [TauMC](https://github.com/TauMC).

#### Q: Where is the config?

A: You'll find the config at `.minecraft/global/nolijium.json5` (note that this is the default `.minecraft` folder, not
the instance `.minecraft`). You can modify the file while the game is running, and the config will be automatically
reloaded. The config button in the NeoForge mods screen will open this text file for you in your system's default 
text editor. Nolijium also has optional Embeddium options screen integration, which is probably the easiest way to 
modify this mod's config.

#### Q: discord where
A: https://discord.gg/6ZjX4mvCMR

#### Q: What version is this for?

A: Currently this mod only supports 21.x NeoForge. It is built off of [Zume](https://github.com/Nolij/Zume)'s 
buildscript though, so it should be feasible to add support for Fabric and/or older Minecraft versions. PRs which 
extend platform support are welcome.

#### Q: What kind of weird license is this?

A: OSL-3.0 is the closest equivalent to a LAGPL I could find. AGPL and GPL are incompatible with Minecraft, and LGPL
doesn't protect network use. OSL-3.0 protects network use and is compatible with Minecraft.

#### Q: Why though? It's so strict!!!!

A: This is, and will remain, free, copyleft software. Any requests to change the license other than to make it even
stronger will be denied immediately (unfortunately GPL and AGPL aren't compatible with Minecraft due to linking
restrictions, as much as I'd like to use them). Even in situations where I use parts of other projects with more
"permissive" licenses, I will treat them as copyleft, free software.

## License

This project is licensed under OSL-3.0. For more information, see [LICENSE](LICENSE).
