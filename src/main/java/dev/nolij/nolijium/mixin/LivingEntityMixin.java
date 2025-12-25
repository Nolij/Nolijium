package dev.nolij.nolijium.mixin;

//? if >=21.1 {
import net.minecraft.core.particles.ColorParticleOption;
import org.spongepowered.asm.mixin.injection.ModifyArg;
//?} else {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.Level;
*///?}
import dev.nolij.libnolij.util.ColourUtil;
import dev.nolij.nolijium.impl.common.NolijiumCommon;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	
	//? if >=21.1 {
	@ModifyArg(
		method = "tickEffects", 
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), 
		index = 0
	)
	private ParticleOptions nolijium$tickEffects$addParticle(ParticleOptions option) {
		if (Nolijium.config.revertPotions && option instanceof ColorParticleOption colourOption) {
			Vector3d colorVec = new Vector3d(colourOption.getRed(), colourOption.getGreen(), colourOption.getBlue());
			if (nolijium$modifyColor(colorVec)) {
				return ColorParticleOption.create(
					colourOption.getType(),
					ColourUtil.getRGB(colorVec.x(), colorVec.y(), colorVec.z()) | ((int) (colourOption.getAlpha() * 255)) << 24);
			}
		}
		return option;
	}
	//? } else {
	/*@WrapOperation(
		method = "tickEffects",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
	)
	private void nolijium$tickEffects$addParticle(Level instance, ParticleOptions particleData, double x, double y, double z, double red, double green, double blue, Operation<Void> original) {
		if (Nolijium.config.revertPotions) {
			Vector3d colorVec = new Vector3d(red, green, blue);
			if (nolijium$modifyColor(colorVec)) {
				red = colorVec.x();
				green = colorVec.y();
				blue = colorVec.z();
			}
		}
		
		original.call(instance, particleData, x, y, z, red, green, blue);
	}
	*///? }
	
	@Unique
	private static boolean nolijium$modifyColor(Vector3d color) {
		final int colour = ColourUtil.getRGB(
			color.x(),
			color.y(),
			color.z());
		
		// Avoid allocation + possible overwrite of custom ColorParticleOption subclass if color hasn't changed
		int newColor = NolijiumCommon.oldPotionColours.getOrDefault(colour, colour);
		
		if (colour == newColor) {
			return false;
		}
		
		color.set(ColourUtil.getRedD(newColor), ColourUtil.getGreenD(newColor), ColourUtil.getBlueD(newColor));
		
		return true;
	}
	
}
