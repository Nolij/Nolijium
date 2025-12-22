package dev.nolij.nolijium.mixin.common;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Font.class)
public class FontMixin {
	
	@WrapMethod(method = "drawInternal(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)I")
	public int nolijium$drawInternal(String p_273658_, float p_273086_, float p_272883_, int p_273547_, boolean p_272778_, Matrix4f p_272662_, MultiBufferSource p_273012_, Font.DisplayMode p_273381_, int p_272855_, int p_272745_, boolean p_272785_, Operation<Integer> original) {
		return original.call(
			p_273658_, 
			p_273086_, 
			p_272883_, 
			p_273547_, 
			p_272778_ && !Nolijium.config.disableFontShadows, 
			p_272662_, 
			p_273012_, 
			p_273381_, 
			p_272855_, 
			p_272745_, 
			p_272785_);
	}
	
}
