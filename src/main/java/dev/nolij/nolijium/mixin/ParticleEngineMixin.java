package dev.nolij.nolijium.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.nolij.nolijium.impl.common.NolijiumCommon;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
	
	@WrapMethod(method = "add")
	public void nolijium$add(Particle particle, Operation<Void> original) {
		if (Nolijium.config.hideParticles)
			return;

		original.call(particle);
	}
	
	@WrapMethod(method = "createParticle")
	public Particle nolijium$createParticle(ParticleOptions particleOptions, double p_107372_, double p_107373_, double p_107374_, double p_107375_, double p_107376_, double p_107377_, Operation<Particle> original) {
		if (Nolijium.config.hideParticles)
			return null;
		
		if (!NolijiumCommon.blockedParticleTypeIDs.isEmpty()) {
			final var key = BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType());
			if (key != null && NolijiumCommon.blockedParticleTypeIDs.contains(key))
				return null;
		}
		
		return original.call(particleOptions, p_107372_, p_107373_, p_107374_, p_107375_, p_107376_, p_107377_);
	}
	
	@Unique
	private static final ResourceLocation nolijium$blockKey = BuiltInRegistries.PARTICLE_TYPE.getKey(ParticleTypes.BLOCK);
	
	@Inject(method = {"crack", "destroy"}, at = @At("HEAD"), cancellable = true)
	public void nolijium$crack_destroy$HEAD(CallbackInfo ci) {
		if (Nolijium.config.hideParticles ||
			NolijiumCommon.blockedParticleTypeIDs.contains(nolijium$blockKey))
			ci.cancel();
	}
	
}
