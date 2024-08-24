package dev.nolij.nolijium.mixin.lexforge20;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	
	@WrapWithCondition(
		method = "renderLevel",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V"
		)
	)
	public boolean nolijium$renderLevel$renderSky(LevelRenderer instance, PoseStack f8, Matrix4f f9, float j, Camera f3, boolean f4, Runnable f5) {
		return !Nolijium.config.disableSky;
	}
	
}
