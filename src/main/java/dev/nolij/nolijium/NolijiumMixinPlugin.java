package dev.nolij.nolijium;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import dev.nolij.nolijium.impl.util.MethodHandleHelper;
import dev.nolij.zumegradle.proguard.ProGuardKeep;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

@ProGuardKeep.WithObfuscation
public final class NolijiumMixinPlugin implements IMixinConfigPlugin {
	
	static final String NEOFORGE = "neoforge";
	static final String LEXFORGE20 = "lexforge20";
	
	static final String NOLIJIUM_VARIANT;
	private static final String implementationMixinPackage;
	
	static {
		if (MethodHandleHelper.PUBLIC.getClassOrNull("net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion") != null) {
			NOLIJIUM_VARIANT = NEOFORGE;
		} else {
			String lexForgeVersion = null;
			try {
				//noinspection DataFlowIssue
				lexForgeVersion = (String) MethodHandleHelper.PUBLIC.getMethodOrNull(
					MethodHandleHelper.PUBLIC.getClassOrNull("net.minecraftforge.versions.forge.ForgeVersion"),
					"getVersion"
				).invokeExact();
			} catch (Throwable ignored) { }
			
			if (lexForgeVersion != null) {
				final int major = Integer.parseInt(lexForgeVersion.substring(0, lexForgeVersion.indexOf('.')));
				if (major == 47)
					NOLIJIUM_VARIANT = LEXFORGE20;
				else
					NOLIJIUM_VARIANT = null;
			} else {
				NOLIJIUM_VARIANT = null;
			}
		}
		
		if (NOLIJIUM_VARIANT != null)
			implementationMixinPackage = "dev.nolij.nolijium.mixin." + NOLIJIUM_VARIANT + ".";
		else
			implementationMixinPackage = null;
	}
	
	@Override
	public void onLoad(String mixinPackage) {
		MixinExtrasBootstrap.init();
	}
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (implementationMixinPackage == null)
			return false;
		
		return mixinClassName.startsWith(implementationMixinPackage);
	}
	
	@Override
	public String getRefMapperConfig() {
		return null;
	}
	
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	
	@Override
	public List<String> getMixins() {
		return null;
	}
	
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	
}
