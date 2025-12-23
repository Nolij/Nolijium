package dev.nolij.nolijium.impl;

//? if <1.21.1 {
/*import dev.nolij.nolijium.impl.common.NolijiumHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class NolijiumHUDOverlay extends NolijiumHUD implements IGuiOverlay {
	@Override
	protected boolean isDebugScreenOpen() {
		return Minecraft.getInstance().options.renderDebug;
	}
	
	@Override
	public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float f, int i, int j) {
		this.render(guiGraphics);
	}
}
*///? }