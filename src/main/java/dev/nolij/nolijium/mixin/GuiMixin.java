package dev.nolij.nolijium.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class GuiMixin {
	
	@WrapWithCondition(method = "onDisconnected", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;clearMessages(Z)V"))
	public boolean nolijium$onDisconnected$clearMessages(ChatComponent instance, boolean clearSentMsgHistory) {
		return !Nolijium.config.keepChatHistory;
	}
	
}
