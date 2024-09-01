package dev.nolij.nolijium.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.nolij.nolijium.impl.INolijiumImplementation;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentContents;

public interface INolijiumSubImplementation extends INolijiumImplementation {
	
	String getClickActionName(ClickEvent.Action action);
	
	ComponentContents getEmptyComponentContents();
	
	boolean supportsLightLevelOverlay();
	
	void addLineVertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, int color, float nx, float ny, float nz);
}
