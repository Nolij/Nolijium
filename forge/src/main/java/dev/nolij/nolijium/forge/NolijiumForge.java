package dev.nolij.nolijium.forge;

import dev.nolij.nolijium.impl.INolijiumImplementation;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import dev.nolij.nolijium.impl.util.MethodHandleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.nolij.nolijium.impl.NolijiumConstants.*;

@Mod(value = MOD_ID)
public class NolijiumForge implements INolijiumImplementation {
	
	private static final MethodHandleHelper METHOD_HANDLE_HELPER =
		new MethodHandleHelper(NolijiumForge.class.getClassLoader(), MethodHandles.lookup());
	
	public NolijiumForge() {
		Nolijium.LOGGER.info("Loading Nolijium...");
		
		ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> 
			new ConfigGuiHandler.ConfigGuiFactory((minecraft, parent) -> new Screen(Component.nullToEmpty("")) {
				@Override
				protected void init() {
					NolijiumConfigImpl.openConfigFile();
					Minecraft.getInstance().setScreen(parent);
				}
			}));
		
		Nolijium.registerImplementation(this, FMLPaths.CONFIGDIR.get());
	}
	
	public static Set<ResourceLocation> blockedParticleTypeIDs = Set.of();
	
	@Override
	public void onConfigReload(NolijiumConfigImpl config) {
		blockedParticleTypeIDs = config.hideParticlesByID
			.stream()
			.map(ResourceLocation::new)
			.collect(Collectors.toUnmodifiableSet());
	}
}
