package dev.nolij.nolijium.mixin.neoforge;

import dev.nolij.nolijium.neoforge.NolijiumLightOverlayRenderer;
import net.minecraft.client.renderer.LevelRenderer;
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
}
