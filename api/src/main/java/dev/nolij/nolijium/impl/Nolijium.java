package dev.nolij.nolijium.impl;

import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

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
		});
	}
	
}
