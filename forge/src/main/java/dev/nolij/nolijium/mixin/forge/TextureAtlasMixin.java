package dev.nolij.nolijium.mixin.forge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.Tickable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {
	
	@WrapOperation(method = "cycleAnimationFrames", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;animatedTextures:Ljava/util/List;"))
	public List<Tickable> nolijium$cycleAnimationFrames$animatedTextures(TextureAtlas instance,
	                                                                     Operation<List<Tickable>> original) {
		if (Nolijium.config.disableTextureAnimations)
			return List.of();
		
		return original.call(instance);
	}
	
}
