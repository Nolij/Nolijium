package dev.nolij.nolijium.lexforge20;

import dev.nolij.nolijium.common.NolijiumHUD;
import dev.nolij.nolijium.impl.Nolijium;
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
		if (isHidden())
			return;
		
		this.onFrame(guiGraphics);
		
		//noinspection deprecation
		guiGraphics.drawManaged(() -> {
			var linePosY = posY;
			for (var line : lines) {
				if (!line.text.isEmpty()) {
					if (background)
						guiGraphics.fill(
							line.posX - 2, linePosY,
							line.posX + line.width + (Nolijium.config.hudShadow ? 2 : 1), linePosY + LINE_HEIGHT,
							BACKGROUND_COLOUR);
					
					guiGraphics.drawString(
						FONT, line.text,
						line.posX, linePosY + 2,
						TEXT_COLOUR, Nolijium.config.hudShadow);
				}
				
				linePosY += LINE_HEIGHT;
			}
		});
	}
}
