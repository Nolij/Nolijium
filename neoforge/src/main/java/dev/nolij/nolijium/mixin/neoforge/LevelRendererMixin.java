package dev.nolij.nolijium.mixin.neoforge;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.neoforge.NolijiumLightOverlayRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V"
		)
	)
	public boolean nolijium$renderLevel$renderSky(LevelRenderer instance, Matrix4f matrix4f1, Matrix4f matrix4f2, float v, Camera camera, boolean b, Runnable runnable) {
		return !Nolijium.config.disableSkyRender;
	}
	
}
