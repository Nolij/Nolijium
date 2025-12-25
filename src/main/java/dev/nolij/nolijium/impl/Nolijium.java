package dev.nolij.nolijium.impl;

import dev.nolij.libnolij.chroma.IChromaProvider;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Set;

import static dev.nolij.nolijium.impl.NolijiumConstants.*;

public class Nolijium {
	
	//region Constants
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final String CONFIG_FILE_NAME = MOD_ID + ".json5";
	//endregion
	
	public static volatile NolijiumConfigImpl config = new NolijiumConfigImpl();
	private static INolijiumImplementation implementation;
	
	public static void registerImplementation(final INolijiumImplementation implementation, final Path instanceConfigPath) {
		if (Nolijium.implementation != null)
			throw new AssertionError("Nolijium already initialized!");
		
		Nolijium.implementation = implementation;
		
		NolijiumConfigImpl.init(instanceConfigPath, CONFIG_FILE_NAME, config -> {
			Nolijium.config = config;
			Nolijium.implementation.onConfigReload(config);
			
			blockChromaProvider = config.blockChromaConfig.getChromaProvider();
			tooltipBorderChromaProvider = config.tooltipChromaConfig.getChromaProvider();
			tooltipBackgroundChromaProvider = tooltipBorderChromaProvider; // might eventually split
			hudChromaProvider = config.hudChromaConfig.getChromaProvider();
		});
	}
	
	public static @Nullable IChromaProvider blockChromaProvider = null;
	public static @Nullable IChromaProvider tooltipBorderChromaProvider = null;
	public static @Nullable IChromaProvider tooltipBackgroundChromaProvider = null;
	public static @Nullable IChromaProvider hudChromaProvider = null;
	
	public static Set<ResourceLocation> blockedParticleTypeIDs = Set.of();
	
	public static int transformBlockOutlineColour(double timestamp, int colour) {
		if (blockChromaProvider != null)
			return blockChromaProvider.chroma(timestamp, 0) | 0xFF000000;
			
		if (config.enableOpaqueBlockOutlines)
			return colour | 0xFF000000;
		
		return colour;
	}
	
}
