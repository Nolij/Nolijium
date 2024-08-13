package dev.nolij.nolijium.common;

import dev.nolij.nolijium.impl.INolijiumImplementation;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentContents;

public interface INolijiumSubImplementation extends INolijiumImplementation {
	
	String getClickActionName(ClickEvent.Action action);
	
	ComponentContents getEmptyComponentContents();
	
}
