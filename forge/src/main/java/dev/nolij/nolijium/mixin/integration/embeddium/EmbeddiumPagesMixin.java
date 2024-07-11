package dev.nolij.nolijium.mixin.integration.embeddium;

import dev.nolij.nolijium.integration.embeddium.NolijiumEmbeddiumConfigScreen;
import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(SodiumOptionsGUI.class)
public class EmbeddiumPagesMixin {
	@Shadow(remap = false)
	@Final
	private List<OptionPage> pages;
	
	@Inject(method = "<init>", at = @At("RETURN"), remap = false)
	public void nojillium$constructor$return(CallbackInfo ci) {
		this.pages.addAll(Arrays.asList(NolijiumEmbeddiumConfigScreen.getPages()));
	}
}
