package dev.nolij.nolijium.mixin.common;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.renderer.LightTexture;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
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
		)
	)
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
	
	@WrapWithCondition(
		method = "tick", 
		at = @At(
			value = "FIELD", 
			opcode = Opcodes.PUTFIELD, 
			target = "Lnet/minecraft/client/renderer/LightTexture;blockLightRedFlicker:F"
		)
	)
	public boolean nolijium$tick$blockLightRedFlicker(LightTexture instance, float value) {
		return !(Nolijium.config.disableBlockLightFlicker || Nolijium.config.enableGamma);
	}
	
	@WrapWithCondition(
		method = "tick", 
		at = @At(
			value = "FIELD", 
			opcode = Opcodes.PUTFIELD, 
			target = "Lnet/minecraft/client/renderer/LightTexture;updateLightTexture:Z"
		)
	)
	public boolean nolijium$tick$updateLightTexture(LightTexture instance, boolean value) {
		return !Nolijium.config.enableGamma;
	}
	
	@ModifyConstant(method = "updateLightTexture", constant = @Constant(floatValue = 0.04F))
	public float nolijium$updateLightTexture$0_04F(float value, @Local(ordinal = 2, print = true) float f1, @Local(ordinal = 7) float f5) {
		if (f1 != 1F && f5 == 0F && Nolijium.config.enablePureDarkness)
			return 0F;
		
		return value;
	}
	
}
