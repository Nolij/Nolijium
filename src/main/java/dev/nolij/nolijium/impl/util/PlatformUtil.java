package dev.nolij.nolijium.impl.util;

//? if >=21.1 {
import net.minecraft.network.chat.contents.PlainTextContents;
//?}
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentContents;

public final class PlatformUtil {
	
	private PlatformUtil() {}
	
	public static String getClickActionName(ClickEvent.Action action) {
		//? if >=21.1 {
		return action.getSerializedName();
		//? } else {
		/*return action.getName();
		 *///? }
	}
	
	public static ComponentContents getEmptyComponentContents() {
		//? if >=21.1 {
		return PlainTextContents.LiteralContents.EMPTY;
		//? } else {
		/*return ComponentContents.EMPTY;
		 *///? }
	}
	
	public static void addLineVertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, int color, float nx, float ny, float nz) {
		//? if >=21.1 {
		consumer.addVertex(pose, x, y, z).setColor(color).setNormal(pose, nx, ny, nz);
		//? } else {
		/*consumer.vertex(pose.pose(), x, y, z).color(color).normal(pose.normal(), nx, ny, nz).endVertex();
		 *///? }
	}
	
	public static void addLineVertex(VertexConsumer consumer, float x, float y, float z, int color, float nx, float ny, float nz) {
		//? if >=21.1 {
		consumer.addVertex(x, y, z).setColor(color).setNormal(nx, ny, nz);
		//? } else {
		/*consumer.vertex(x, y, z).color(color).normal(nx, ny, nz).endVertex();
		 *///? }
	}
	
}
