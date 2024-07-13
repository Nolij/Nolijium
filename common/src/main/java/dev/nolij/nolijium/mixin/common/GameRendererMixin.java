package dev.nolij.nolijium.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	
	@WrapOperation(method = "bobHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getHurtDir()F"))
	public float nolijium$bobHurt$getHurtDir(LivingEntity instance, Operation<Float> original) {
		if (Nolijium.config.revertDamageCameraTilt)
			return 0F;
		
		return original.call(instance);
	}
	
}
