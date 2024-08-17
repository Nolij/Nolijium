package dev.nolij.nolijium.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.nolij.nolijium.common.NolijiumCommon;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.util.RGBHelper;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelRenderer.class, priority = 1100)
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
		
		if (NolijiumCommon.blockedParticleTypeIDs.isEmpty())
			return;
		
		var key = BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType());
		if (key == null)
			return;
		
		if (NolijiumCommon.blockedParticleTypeIDs.contains(key))
			cir.setReturnValue(null);
	}
	
	@WrapOperation(
		method = "renderHitOutline", 
		at = @At(
			value = "INVOKE", 
			target = "Lnet/minecraft/client/renderer/LevelRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDFFFF)V"
		)
	)
	public void nolijium$renderHitOutline$renderShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha, Operation<Void> original) {
		if (Nolijium.config.enableChromaBlockOutlines) {
			final double timestamp = System.nanoTime() * 1E-9D;
			
			red = (float) RGBHelper.chromaRed(timestamp, Nolijium.config.chromaSpeed, 0);
			green = (float) RGBHelper.chromaGreen(timestamp, Nolijium.config.chromaSpeed, 0);
			blue = (float) RGBHelper.chromaBlue(timestamp, Nolijium.config.chromaSpeed, 0);
			alpha = 1F;
		} else if (Nolijium.config.enableOpaqueBlockOutlines) {
			alpha = 1F;
		}
		
		original.call(poseStack, vertexConsumer, shape, x, y, z, red, green, blue, alpha);
	}
}
