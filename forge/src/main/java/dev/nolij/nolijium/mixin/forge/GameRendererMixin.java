package dev.nolij.nolijium.mixin.forge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	
	// @WrapOperation(method = "bobHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getHurtDir()F"))
	// TODO: should we fix it instead using LivingKnockBackEvent?
	public float nolijium$bobHurt$getHurtDir(LivingEntity instance, Operation<Float> original) {
		if (Nolijium.config.revertDamageCameraTilt)
			return 0F;
		
		return original.call(instance);
	}
	
}
