package dev.nolij.nolijium.integration.embeddium;

import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;

public class NolijiumOptionsStorage implements OptionStorage<NolijiumConfigImpl> {
	
	private final NolijiumConfigImpl storage = Nolijium.config.clone();
	
	@Override
	public NolijiumConfigImpl getData() {
		return storage;
	}
	
	@Override
	public void save() {
		NolijiumConfigImpl.replace(storage);
	}
	
}
