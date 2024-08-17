package dev.nolij.nolijium.mixin.common;

import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightTexture.class)
public interface LightTextureAccessor {
	
	@Accessor("updateLightTexture")
	void setUpdateLightTexture(boolean updateLightTexture);
	
}
