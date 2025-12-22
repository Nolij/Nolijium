package dev.nolij.nolijium.impl.integration.embeddium;

import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import org.embeddedt.embeddium.api.options.structure.OptionStorage;

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
