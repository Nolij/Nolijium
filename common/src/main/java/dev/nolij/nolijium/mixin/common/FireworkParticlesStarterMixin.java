package dev.nolij.nolijium.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkParticles.Starter.class)
public class FireworkParticlesStarterMixin {
	@WrapWithCondition(method="tick",at= @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;setColor(FFF)V"))
	private boolean preventdontSetColorIfNull(Particle instance, float particleRed, float particleGreen, float particleBlue) {
		return instance != null;
	}
	@ModifyExpressionValue(method="createParticle", at= @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;"))
	private Particle captureParticleCreation(Particle original, @Share("particleCreated") LocalBooleanRef particleHidden) {
		particleHidden.set(original == null);
		return original;
	}
	@Inject(method="createParticle", at= @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/particle/ParticleEngine;createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;"), cancellable = true)
	private void cancelParticleCreationIfNull(double p_106768_, double p_106769_, double p_106770_, double p_106771_, double p_106772_, double p_106773_, IntList p_330262_, IntList p_330897_, boolean p_106776_, boolean p_106777_, CallbackInfo ci, @Share("particleCreated")LocalBooleanRef particleHidden) {
		if (particleHidden.get()) {
			ci.cancel();
		}
	}
}