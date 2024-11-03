package dev.nolij.nolijium.mixin.lexforge20;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nolij.libnolij.util.ColourUtil;
import dev.nolij.nolijium.common.NolijiumCommon;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	
	@WrapOperation(
		method = "tickEffects", 
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
	)
	private void nolijium$tickEffects$addParticle(Level instance, ParticleOptions particleData, double x, double y, double z, double red, double green, double blue, Operation<Void> original) {
		if (Nolijium.config.revertPotions) {
			final int colour = ColourUtil.getColour(
				0,
				red,
				green,
				blue);
			
			if (NolijiumCommon.oldPotionColours.containsKey(colour)) {
				final int revertedColour = NolijiumCommon.oldPotionColours.get(colour);
				
				red = (revertedColour >> 16 & 0xFF) / 255D;
				green = (revertedColour >> 8 & 0xFF) / 255D;
				blue = (revertedColour & 0xFF) / 255D;
			}
		}
		
		original.call(instance, particleData, x, y, z, red, green, blue);
	}
	
}
