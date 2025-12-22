package dev.nolij.nolijium.mixin;

import dev.nolij.libnolij.util.ColourUtil;
import dev.nolij.nolijium.common.NolijiumCommon;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	
	@ModifyArg(
		method = "tickEffects", 
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), 
		index = 0
	)
	private ParticleOptions nolijium$tickEffects$addParticle(ParticleOptions option) {
		if (Nolijium.config.revertPotions && option instanceof ColorParticleOption colourOption) {
			final int colour = ColourUtil.getRGB(
				colourOption.getRed(),
				colourOption.getGreen(),
				colourOption.getBlue());
			
			// Avoid allocation + possible overwrite of custom ColorParticleOption subclass if color doesn't match
			if (!NolijiumCommon.oldPotionColours.containsKey(colour)) {
				return option;
			}
			
			return ColorParticleOption.create(
				colourOption.getType(),
				NolijiumCommon.oldPotionColours.getOrDefault(colour, colour) | ((int) (colourOption.getAlpha() * 255)) << 24
			);
		} else {
			return option;
		}
	}
	
}
