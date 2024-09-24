package dev.nolij.nolijium.mixin.fabric;

import dev.nolij.nolijium.common.NolijiumCommon;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastComponent.class)
public class ToastComponentMixin {
	
	@Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
	public void nolijium$addToast$HEAD(Toast toast, CallbackInfo ci) {
		if (NolijiumCommon.shouldHideToast(toast))
			ci.cancel();
	}
	
}
