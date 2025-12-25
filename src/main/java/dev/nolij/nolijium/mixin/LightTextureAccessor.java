package dev.nolij.nolijium.mixin;

import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightTexture.class)
public interface LightTextureAccessor {
	
	@Accessor("updateLightTexture")
	void nolijium$setUpdateLightTexture(boolean updateLightTexture);
	
}
