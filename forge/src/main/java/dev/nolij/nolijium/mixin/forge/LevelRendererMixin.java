package dev.nolij.nolijium.mixin.forge;

import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.forge.NolijiumForge;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	
	@Inject(
		method = "addParticleInternal(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)Lnet/minecraft/client/particle/Particle;",
		at = @At("HEAD"),
		cancellable = true
	)
	public void nolijium$addParticleInternal$HEAD(ParticleOptions particleOptions, boolean p_109806_, boolean p_109807_, double p_109808_, double p_109809_, double p_109810_, double p_109811_, double p_109812_, double p_109813_, CallbackInfoReturnable<Particle> cir) {
		if (Nolijium.config.hideParticles) {
			cir.setReturnValue(null);
			return;
		}
		
		if (NolijiumForge.blockedParticleTypeIDs.isEmpty())
			return;
		
		var key = Registry.PARTICLE_TYPE.getKey(particleOptions.getType());
		if (key == null)
			return;
		
		if (NolijiumForge.blockedParticleTypeIDs.contains(key))
			cir.setReturnValue(null);
	}
	
}
