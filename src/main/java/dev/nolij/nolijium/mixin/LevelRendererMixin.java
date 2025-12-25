package dev.nolij.nolijium.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.nolij.libnolij.util.ColourUtil;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	
	@WrapOperation(
		method = "renderHitOutline", 
		at = @At(
			value = "INVOKE", 
			target = "Lnet/minecraft/client/renderer/LevelRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDFFFF)V"
		)
	)
	public void nolijium$renderHitOutline$renderShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha, Operation<Void> original) {
		final var colour = Nolijium.transformBlockOutlineColour(System.nanoTime() * 1E-9D, ColourUtil.getARGB(alpha, red, green, blue));
		
		alpha = (float) ColourUtil.getAlphaD(colour);
		red = (float) ColourUtil.getRedD(colour);
		green = (float) ColourUtil.getGreenD(colour);
		blue = (float) ColourUtil.getBlueD(colour);
		
		if (Nolijium.blockChromaProvider != null && (
			Nolijium.config.blockShapeOverlayOverride > 0 || 
			Nolijium.config.chromaBlockShapeOverlay > 0)) {
			Nolijium.focusedBlockPosition = new Vec3(x, y, z);
			Nolijium.focusedBlockShape = shape;
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
