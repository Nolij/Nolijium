package dev.nolij.nolijium.mixin;

import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	
	@Inject(method = "handleContainerClose", at = @At("HEAD"), cancellable = true)
	public void nolijium$handleContainerClose$HEAD(CallbackInfo ci) {
		if (Nolijium.config.keepChatBarOpen &&
			Minecraft.getInstance().screen instanceof ChatScreen)
			ci.cancel();
	}
	
}
