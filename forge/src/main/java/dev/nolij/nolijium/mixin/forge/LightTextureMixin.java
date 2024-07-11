package dev.nolij.nolijium.mixin.forge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightTexture.class)
public class LightTextureMixin {
	
	@WrapOperation(
		method = "updateLightTexture", 
		at = @At(
			value = "FIELD", 
			target = "Lnet/minecraft/client/Options;gamma:D", 
			remap = false,
			ordinal = 0
		))
	public double nolijium$updateLightTexture$floatValue(Options instance, Operation<Double> original) {
		if (Nolijium.config.enableGamma)
			return 1E7F;
		
		return original.call(instance);
	}
	
	@Inject(method = "notGamma", at = @At("HEAD"), cancellable = true)
	public void nolijium$calculateDarknessScale$HEAD(CallbackInfoReturnable<Float> cir) {
		if (Nolijium.config.enableGamma)
			cir.setReturnValue(0F);
	}
	
}
