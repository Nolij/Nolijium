package dev.nolij.nolijium.mixin.common;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	
	@Invoker("createStars")
	void nolijium$createStars();
	
}
