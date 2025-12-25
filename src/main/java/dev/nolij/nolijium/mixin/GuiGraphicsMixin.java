package dev.nolij.nolijium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nolij.nolijium.impl.common.NolijiumCommon;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
	
	@WrapOperation(
		method = "renderComponentHoverEffect", 
		at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Style;getHoverEvent()Lnet/minecraft/network/chat/HoverEvent;")
	)
	public HoverEvent nolijium$renderComponentHoverEffect$getHoverEvent(Style instance, Operation<HoverEvent> original) {
		if (Nolijium.config.enableToolTipInfo)
			return NolijiumCommon.getHoverEvent(instance);
		
		return original.call(instance);
	}
	
}
