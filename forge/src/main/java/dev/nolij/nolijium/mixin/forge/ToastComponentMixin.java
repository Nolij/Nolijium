package dev.nolij.nolijium.mixin.forge;

import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastComponent.class)
public class ToastComponentMixin {
	
	@Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
	public void nojillium$addToast$HEAD(Toast toast, CallbackInfo ci) {
		if (Nolijium.config.hideAllToasts) {
			ci.cancel();
			return;
		}
		
		// Yes, can be compacted to avoid use cancel many times, but doing that makes code unreadable
		if (toast instanceof AdvancementToast && Nolijium.config.hideAdvancementToasts) {
			ci.cancel();
		} else if (toast instanceof RecipeToast && Nolijium.config.hideRecipeToasts) {
			ci.cancel();
		} else if (toast instanceof SystemToast && Nolijium.config.hideSystemToasts) {
			ci.cancel();
		} else if (toast instanceof TutorialToast && Nolijium.config.hideTutorialToasts) {
			ci.cancel();
		}
	}
}
