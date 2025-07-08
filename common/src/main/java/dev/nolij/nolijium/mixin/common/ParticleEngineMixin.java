package dev.nolij.nolijium.mixin.common;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.nolij.nolijium.common.NolijiumCommon;
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
	
	@WrapWithCondition(method="createParticle", at=@At(value = "INVOKE", target="Lnet/minecraft/client/particle/ParticleEngine;add(Lnet/minecraft/client/particle/Particle;)V"))
	public boolean nolijium$preventAddingParticle(ParticleEngine instance, Particle effect, @Share(value="mustCreateAndHidden") LocalBooleanRef mustCreateAndHidden) {
		return !mustCreateAndHidden.get();
	}

	@WrapMethod(method = "createParticle")
	public Particle nolijium$createParticle(ParticleOptions particleOptions, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, Operation<Particle> original, @Share("mustCreateAndHidden") LocalBooleanRef mustCreateAndHidden) {
		final var key = BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType());
		final boolean hidden = Nolijium.config.hideParticles || (key != null && NolijiumCommon.blockedParticleTypeIDs.contains(key));
		final boolean mustReturnNonNull = key != null && NolijiumCommon.mustBeCreatedParticleTypeIDs.contains(key);
		if (hidden && !mustReturnNonNull)  
			return null;

		mustCreateAndHidden.set(hidden);
		Particle p = original.call(particleOptions, x, y, z, xSpeed, ySpeed, zSpeed);
		if (hidden) p.remove();
		mustCreateAndHidden.set(false);
		return p;
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
