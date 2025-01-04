package dev.nolij.nolijium.mixin.lexforge20;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.lexforge20.ChromaMultiBufferSource;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	
	@WrapWithCondition(
		method = "renderLevel",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V")
	)
	public boolean nolijium$renderLevel$renderSky(LevelRenderer instance, PoseStack f8, Matrix4f f9, float j, Camera f3, boolean f4, Runnable f5) {
		return !Nolijium.config.disableSky;
	}
	
	@ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;onDrawHighlight(Lnet/minecraft/client/renderer/LevelRenderer;Lnet/minecraft/client/Camera;Lnet/minecraft/world/phys/HitResult;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)Z"), index = 5)
	private MultiBufferSource wrapBuffersWithColor(MultiBufferSource original) {
		if (Nolijium.config.enableChromaBlockOutlines) {
			return new ChromaMultiBufferSource(original);
		} else {
			return original;
		}
	}
	
}
