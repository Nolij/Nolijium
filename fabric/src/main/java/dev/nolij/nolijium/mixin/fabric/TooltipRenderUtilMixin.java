package dev.nolij.nolijium.mixin.fabric;

import dev.nolij.libnolij.util.ColourUtil;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TooltipRenderUtil.class)
public class TooltipRenderUtilMixin {
	
	@ModifyConstant(method = "renderTooltipBackground", constant = @Constant(intValue = 0xF0100010))
	private static int nolijium$renderTooltipBackground$0xF0100010(int constant) {
		if (Nolijium.config.tooltipColourOverride)
			return Nolijium.config.tooltipBackgroundStart;
		else if (Nolijium.config.enableChromaToolTips)
			return ColourUtil.chroma(System.nanoTime() * 1E-9D, Nolijium.config.chromaSpeed, 0, 0.25D);
		
		return constant;
	}
	
	@ModifyConstant(method = "renderTooltipBackground", constant = @Constant(intValue = 0x505000FF))
	private static int nolijium$renderTooltipBackground$0x505000FF(int constant) {
		if (Nolijium.config.tooltipColourOverride)
			return Nolijium.config.tooltipBorderStart;
		else if (Nolijium.config.enableChromaToolTips)
			return ColourUtil.chroma(System.nanoTime() * 1E-9D, Nolijium.config.chromaSpeed, 0);
		
		return constant;
	}
	
	@ModifyConstant(method = "renderTooltipBackground", constant = @Constant(intValue = 0x5028007F))
	private static int nolijium$renderTooltipBackground$0x5028007F(int constant) {
		if (Nolijium.config.tooltipColourOverride)
			return Nolijium.config.tooltipBorderEnd;
		else if (Nolijium.config.enableChromaToolTips)
			return ColourUtil.chroma(System.nanoTime() * 1E-9D, Nolijium.config.chromaSpeed, -2);
		
		return constant;
	}
	
}
