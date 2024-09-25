package dev.nolij.nolijium.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public enum NolijiumFabricKeyBind {
	
	TOGGLE_LIGHT_LEVEL_OVERLAY("nolijium.toggle_light_level_overlay", GLFW.GLFW_KEY_F7),
	
	;
	
	public final KeyMapping value;
	
	public boolean isPressed() {
		return value.isDown();
	}
	
	public boolean wasPressed() {
		return value.consumeClick();
	}
	
	public void flush() {
		//noinspection StatementWithEmptyBody
		while (value.consumeClick());
	}
	
	NolijiumFabricKeyBind(String translationKey, int code) {
		this.value = new KeyMapping(translationKey, InputConstants.Type.KEYSYM, code, "nolijium");
	}
	
}
