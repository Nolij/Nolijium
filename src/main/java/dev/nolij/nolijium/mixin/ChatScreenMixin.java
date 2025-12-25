package dev.nolij.nolijium.mixin;

import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
	
	@Unique private static final String nolijium$initialMessage = "";
	@Unique private static final String nolijium$initialCommand = "/";
	
	@Unique private static String nolijium$rememberedMessage = null;
	
	@Shadow private String initial;
	
	@ModifyArg(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setValue(Ljava/lang/String;)V"))
	public String nolijium$init$setValue$arg0(String text) {
		String result = text;
		
		if (nolijium$rememberedMessage != null && 
			Nolijium.config.rememberChatBarContents != NolijiumConfigImpl.RememberChatBarContents.NEVER &&
			(initial.equals(nolijium$initialMessage) || initial.equals(nolijium$initialCommand))) {
			result = nolijium$rememberedMessage;
		}
		
		return result;
	}
	
	@Inject(method = "keyPressed", at = @At("HEAD"))
	public void nolijium$keyPressed$HEAD(int keyCode, int p_95592_, int p_95593_, CallbackInfoReturnable<Boolean> cir) {
		if (keyCode == 256 &&
			Nolijium.config.rememberChatBarContents == NolijiumConfigImpl.RememberChatBarContents.UNTIL_USER_CLOSED) {
			nolijium$rememberedMessage = null;
		}
	}
	
	@Inject(method = "onEdited", at = @At("HEAD"))
	public void nolijium$onEdited$HEAD(String value, CallbackInfo ci) {
		if (Nolijium.config.rememberChatBarContents != NolijiumConfigImpl.RememberChatBarContents.NEVER) {
			nolijium$rememberedMessage = value;
		}
	}
	
	//? if >=21.1 {
	@Inject(method = "handleChatInput(Ljava/lang/String;Z)V", at = @At("HEAD"))
	public void nolijium$handleChatInput$HEAD(CallbackInfo ci) {
		nolijium$rememberedMessage = null;
	}
	//? } else {
	/*@Inject(method = "handleChatInput", at = @At("HEAD"))
	private void nolijium$handleChatInput$HEAD(CallbackInfoReturnable<Boolean> cir) {
		nolijium$rememberedMessage = null;
	}
	*///? }
	
}
