package dev.nolij.nolijium.mixin.neoforge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nolij.nolijium.impl.Nolijium;
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
			value = "INVOKE", 
			target = "Ljava/lang/Double;floatValue()F", 
			remap = false,
			ordinal = 1
		))
	public float nolijium$updateLightTexture$floatValue(Double instance, Operation<Float> original) {
		if (Nolijium.config.enableGamma)
			return 1E7F;
		
		return original.call(instance);
	}
	
	@Inject(method = "calculateDarknessScale", at = @At("HEAD"), cancellable = true)
	public void nolijium$calculateDarknessScale$HEAD(CallbackInfoReturnable<Float> cir) {
		if (Nolijium.config.enableGamma)
			cir.setReturnValue(0F);
	}
	
}
