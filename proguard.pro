-ignorewarnings
-dontnote
-optimizationpasses 10
-optimizations !class/merging/*,!method/marking/private,!method/marking/static,!*/specialization/*,!method/removal/parameter
-allowaccessmodification
#noinspection ShrinkerInvalidFlags
-optimizeaggressively
-repackageclasses nolijium
-keepattributes Runtime*Annotations,AnnotationDefault

-keepclassmembers class dev.nolij.nolijium.impl.config.NolijiumConfigImpl { # dont rename config fields
	@dev.nolij.nolijium.zson.ZsonField <fields>;
}
-keepclassmembers,allowoptimization class dev.nolij.nolijium.NolijiumMixinPlugin {
    public *;
}
-keep @org.spongepowered.asm.mixin.Mixin class * {
	@org.spongepowered.asm.mixin.Overwrite *;
	@org.spongepowered.asm.mixin.Shadow *;
}
-keepclassmembers,allowobfuscation @org.spongepowered.asm.mixin.Mixin class * { *; }

# Forge entrypoints
-keep,allowobfuscation @*.*.fml.common.Mod class dev.nolij.nolijium.** {
	public <init>(...);
}

-adaptclassstrings
-adaptresourcefilecontents fabric.mod.json

# screens
-keepclassmembers class dev.nolij.nolijium.** extends net.minecraft.class_437,
												  net.minecraft.client.gui.screens.Screen {
	public *;
}

# Fabric entrypoints
-keep,allowoptimization,allowobfuscation class dev.nolij.nolijium.fabric.NolijiumFabric
-keep,allowoptimization,allowobfuscation class dev.nolij.nolijium.fabric.integration.modmenu.NolijiumModMenuIntegration

# Shaded MixinExtras
-keep class nolijium.mixinextras.** {
	*;
}

# ZumeGradle
-keep @dev.nolij.zumegradle.proguard.ProGuardKeep class * { *; }
-keepclassmembers class * { @dev.nolij.zumegradle.proguard.ProGuardKeep *; }

-keep,allowobfuscation @dev.nolij.zumegradle.proguard.ProGuardKeep$WithObfuscation class * { *; }
-keepclassmembers,allowobfuscation class * { @dev.nolij.zumegradle.proguard.ProGuardKeep$WithObfuscation *; }

-keepclassmembers @dev.nolij.zumegradle.proguard.ProGuardKeep$Enum enum * {
	**[] values();
	** valueOf(...);
}