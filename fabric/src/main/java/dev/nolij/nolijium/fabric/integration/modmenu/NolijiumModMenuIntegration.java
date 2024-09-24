package dev.nolij.nolijium.fabric.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NolijiumModMenuIntegration implements ModMenuApi {
	
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> new Screen(Component.literal("")) {
			@Override
			protected void init() {
				NolijiumConfigImpl.openConfigFile();
				Minecraft.getInstance().setScreen(parent);
			}
		};
	}
	
}
