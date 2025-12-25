package dev.nolij.nolijium.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	
	@ModifyReturnValue(method = "getStarBrightness", at = @At("RETURN"))
	public float nolijium$getStarBrightness(float brightness) {
		return brightness / 0.5F * Nolijium.config.starBrightness;
	}
	
	@ModifyReturnValue(method = "getSkyDarken(F)F", at = @At("RETURN"))
	public float nolijium$getSkyDarken$RETURN(float original) {
		if (Nolijium.config.minimumSkyLightLevel != 0.2F)
			return (original - 0.2F / 0.8F) * (1F - Nolijium.config.minimumSkyLightLevel) + Nolijium.config.minimumSkyLightLevel;
		
		return original;
	}
	
}
