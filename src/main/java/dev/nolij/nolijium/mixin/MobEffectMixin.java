package dev.nolij.nolijium.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEffect.class)
public class MobEffectMixin {
	
	@ModifyReturnValue(method = "getColor", at = @At("RETURN"))
	public int nolijium$getColor$RETURN(int original) {
		if (Nolijium.config.revertPotions)
			return Nolijium.oldPotionColours.getOrDefault(original, original);
		
		return original;
	}
	
}
