package dev.nolij.nolijium.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {
	
	@WrapOperation(method = "cycleAnimationFrames", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;animatedTextures:Ljava/util/List;"))
	public List<TextureAtlasSprite.Ticker> nolijium$cycleAnimationFrames$animatedTextures(TextureAtlas instance, Operation<List<TextureAtlasSprite.Ticker>> original) {
		if (Nolijium.config.disableTextureAnimations)
			return List.of();
		
		return original.call(instance);
	}
	
}
