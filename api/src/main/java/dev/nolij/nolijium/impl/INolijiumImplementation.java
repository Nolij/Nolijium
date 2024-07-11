package dev.nolij.nolijium.impl;

import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;

public interface INolijiumImplementation {
	
	default void onConfigReload(NolijiumConfigImpl config) {}
	
}
