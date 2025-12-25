
//? if >=21.1 {
package dev.nolij.nolijium.impl;

import dev.nolij.nolijium.impl.common.NolijiumHUD;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import org.jetbrains.annotations.NotNull;

public class NolijiumHUDRenderLayer extends NolijiumHUD implements LayeredDraw.Layer {
	
	@Override
	protected boolean isDebugScreenOpen() {
		return Minecraft.getInstance().getDebugOverlay().showDebugScreen();
	}
	
	@Override
	public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
		this.render(guiGraphics);
	}
	
}
//? }