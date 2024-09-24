package dev.nolij.nolijium.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.nolij.nolijium.common.INolijiumSubImplementation;
import dev.nolij.nolijium.common.NolijiumCommon;
import dev.nolij.nolijium.mixin.fabric.GuiAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.PlainTextContents;

public class NolijiumFabric implements ClientModInitializer, INolijiumSubImplementation {
	
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
			return;
		
		new NolijiumCommon(this, FabricLoader.getInstance().getConfigDir());
		
		ClientLifecycleEvents.CLIENT_STARTED.register(minecraft -> 
			((GuiAccessor) minecraft.gui).getLayers().add(new NolijiumHUDRenderLayer()));
		
		WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> 
			NolijiumCommon.renderAfterTranslucentBlocks(context.matrixStack()));
	}
	
	@Override
	public String getClickActionName(ClickEvent.Action action) {
		return action.getSerializedName();
	}
	
	@Override
	public ComponentContents getEmptyComponentContents() {
		return PlainTextContents.LiteralContents.EMPTY;
	}
	
	@Override
	public boolean supportsLightLevelOverlay() {
		return false;
	}
	
	@Override
	public void addLineVertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, int color, float nx, float ny, float nz) {
		consumer.addVertex(pose, x, y, z).setColor(color).setNormal(pose, nx, ny, nz);
	}
	
}
