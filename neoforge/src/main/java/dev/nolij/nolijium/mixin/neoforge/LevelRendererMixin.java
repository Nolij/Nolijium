package dev.nolij.nolijium.mixin.neoforge;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.common.NolijiumLightOverlayRenderer;
import dev.nolij.nolijium.neoforge.ChromaMultiBufferSource;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelRenderer.class, priority = 1100)
public class LevelRendererMixin {
	
	@Inject(method = "setSectionDirty(IIIZ)V", at = @At("RETURN"))
	private void nolijium$triggerLightOverlayUpdate(int x, int y, int z, boolean important, CallbackInfo ci) {
		NolijiumLightOverlayRenderer.invalidateSection(x, y, z);
		// Invalidate section below, in case it now needs to show light levels on its topmost blocks
		NolijiumLightOverlayRenderer.invalidateSection(x, y - 1, z);
	}
	
	@WrapWithCondition(
		method = "renderLevel",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V")
	)
	public boolean nolijium$renderLevel$renderSky(LevelRenderer instance, Matrix4f matrix4f1, Matrix4f matrix4f2, float v, Camera camera, boolean b, Runnable runnable) {
		return !Nolijium.config.disableSky;
	}
	
	// fix crash if star count is too low
	@Inject(
		method = "drawStars",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;buildOrThrow()Lcom/mojang/blaze3d/vertex/MeshData;"
		)
	)
	public void nolijium$drawStars$buildOrThrow(Tesselator p_350542_, CallbackInfoReturnable<MeshData> cir, @Local BufferBuilder bufferBuilder) {
		for (int i = 0; i < 4; i++)
			bufferBuilder.addVertex(0, 0, 0);
	}
	
	@ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;onDrawHighlight(Lnet/minecraft/client/renderer/LevelRenderer;Lnet/minecraft/client/Camera;Lnet/minecraft/world/phys/HitResult;Lnet/minecraft/client/DeltaTracker;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)Z"), index = 5)
	private MultiBufferSource wrapBuffersWithColor(MultiBufferSource original, @Local(ordinal = 0, argsOnly = true) DeltaTracker deltaTracker) {
		if (Nolijium.config.enableChromaBlockOutlines) {
			return new ChromaMultiBufferSource(original);
		} else {
			return original;
		}
	}
}
