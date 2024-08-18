package dev.nolij.nolijium.mixin.common;

import dev.nolij.nolijium.common.INolijiumSubImplementation;
import dev.nolij.nolijium.common.NolijiumCommon;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(LevelLightEngine.class)
public class LevelLightEngineMixin {
	
	@Unique
	private static final INolijiumSubImplementation NOLIJIUM_IMPL = Objects.requireNonNull(NolijiumCommon.getImplementation()); 
	
	@Shadow @Final protected LevelHeightAccessor levelHeightAccessor;
	
	@Inject(method = "checkBlock", at = @At("HEAD"), cancellable = true)
	public void nolijium$checkBlock$HEAD(BlockPos blockPos, CallbackInfo ci) {
		if (Nolijium.config.enableGamma && !(NOLIJIUM_IMPL.supportsLightLevelOverlay() && Nolijium.config.enableLightLevelOverlay) && levelHeightAccessor instanceof ClientLevel)
			ci.cancel();
	}
	
	@Inject(method = "runLightUpdates", at = @At("HEAD"), cancellable = true)
	public void nolijium$runLightUpdates$HEAD(CallbackInfoReturnable<Integer> cir) {
		if (Nolijium.config.enableGamma && !(NOLIJIUM_IMPL.supportsLightLevelOverlay() && Nolijium.config.enableLightLevelOverlay) && levelHeightAccessor instanceof ClientLevel)
			cir.setReturnValue(0);
	}
	
}
