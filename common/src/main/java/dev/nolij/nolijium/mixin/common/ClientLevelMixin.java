package dev.nolij.nolijium.mixin.common;

import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	
	@ModifyConstant(method = "getStarBrightness", constant = @Constant(floatValue = 0.5F))
	public float nolijium$getStarBrightness$0_5F(float constant) {
		return Nolijium.config.starBrightness;
	}
	
}
