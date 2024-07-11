package dev.nolij.nolijium.mixin.neoforge;

import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
	
	@ModifyConstant(method = {
			"addMessageToDisplayQueue", 
			"addMessageToQueue",
			"addRecentChat",
		}, constant = @Constant(intValue = 100))
	public int nolijium$MAX_CHAT_HISTORY(int constant) {
		return Nolijium.config.maxChatHistory > 0 
		       ? Nolijium.config.maxChatHistory 
		       : Integer.MAX_VALUE;
	}
	
}
