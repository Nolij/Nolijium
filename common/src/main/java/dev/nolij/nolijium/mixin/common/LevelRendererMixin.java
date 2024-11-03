package dev.nolij.nolijium.mixin.common;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.nolij.libnolij.util.ColourUtil;
import dev.nolij.nolijium.common.NolijiumCommon;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
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
		if (Nolijium.config.blockShapeOverlayOverride != 0 || Nolijium.config.chromaBlockShapeOverlay > 0) {
			NolijiumCommon.focusedBlockPosition = new Vec3(x, y, z);
			NolijiumCommon.focusedBlockShape = shape;
			alpha = 1F;
		} else if (Nolijium.config.enableOpaqueBlockOutlines || Nolijium.config.enableChromaBlockOutlines) {
			alpha = 1F;
		}
		
		if (Nolijium.config.blockShapeOverlayOverride != 0) {
			red = (float) ColourUtil.getRed(Nolijium.config.blockShapeOverlayOverride);
			green = (float) ColourUtil.getGreen(Nolijium.config.blockShapeOverlayOverride);
			blue = (float) ColourUtil.getBlue(Nolijium.config.blockShapeOverlayOverride);
		} else if (Nolijium.config.enableChromaBlockOutlines || Nolijium.config.chromaBlockShapeOverlay > 0) {
			final double timestamp = System.nanoTime() * 1E-9D;
			
			red = (float) ColourUtil.chromaRed(timestamp, Nolijium.config.chromaSpeed, 0);
			green = (float) ColourUtil.chromaGreen(timestamp, Nolijium.config.chromaSpeed, 0);
			blue = (float) ColourUtil.chromaBlue(timestamp, Nolijium.config.chromaSpeed, 0);
		}
		
		original.call(poseStack, vertexConsumer, shape, x, y, z, red, green, blue, alpha);
	}
	
	@WrapWithCondition(
		method = "renderLevel",
		at = @At(
			value = "INVOKE", 
			target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V"
		)
	)
	public boolean nolijium$renderLevel$renderSnowAndRain(LevelRenderer instance, LightTexture f2, float d2, double d4, double f3, double f4) {
		return !Nolijium.config.disableWeatherRendering;
	}
	
	@ModifyConstant(method = "drawStars", constant = @Constant(intValue = 1500))
	public int nolijium$drawStars$1500(int constant) {
		return Nolijium.config.starCount;
	}
	
	@ModifyConstant(method = "drawStars", constant = @Constant(floatValue = 0.1F))
	public float nolijium$drawStars$0_1F(float constant) {
		return Nolijium.config.starScale;
	}
	
}
