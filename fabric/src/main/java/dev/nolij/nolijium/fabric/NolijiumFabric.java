package dev.nolij.nolijium.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.nolij.nolijium.common.INolijiumSubImplementation;
import dev.nolij.nolijium.common.NolijiumCommon;
import dev.nolij.nolijium.common.NolijiumLightOverlayRenderer;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.mixin.fabric.GuiAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.PlainTextContents;

public class NolijiumFabric implements ClientModInitializer, INolijiumSubImplementation {
	
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
			return;
		
		new NolijiumCommon(this, FabricLoader.getInstance().getConfigDir());
		
		for (final NolijiumFabricKeyBind keyBind : NolijiumFabricKeyBind.values()) {
			KeyBindingHelper.registerKeyBinding(keyBind.value);
		}
		
		ClientLifecycleEvents.CLIENT_STARTED.register(minecraft -> 
			((GuiAccessor) minecraft.gui).getLayers().add(new NolijiumHUDRenderLayer()));
		
		WorldRenderEvents.BEFORE_ENTITIES.register(context ->
			NolijiumLightOverlayRenderer.render(context.camera(), context.positionMatrix(), RenderType.cutout()));
		
		WorldRenderEvents.AFTER_TRANSLUCENT.register(context ->
			NolijiumCommon.renderAfterTranslucentBlocks(context.matrixStack()));
		
		ClientChunkEvents.CHUNK_UNLOAD.register((minecraft, chunk) ->
			NolijiumLightOverlayRenderer.invalidateChunk(chunk.getLevel(), chunk.getPos()));
		
		ClientTickEvents.START_CLIENT_TICK.register(minecraft -> {
			if (NolijiumFabricKeyBind.TOGGLE_LIGHT_LEVEL_OVERLAY.wasPressed()) {
				NolijiumFabricKeyBind.TOGGLE_LIGHT_LEVEL_OVERLAY.flush();
				Nolijium.config.modify(config -> config.enableLightLevelOverlay = !config.enableLightLevelOverlay);
			}
		});
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
		return true;
	}
	
	@Override
	public void addLineVertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, int color, float nx, float ny, float nz) {
		consumer.addVertex(pose, x, y, z).setColor(color).setNormal(pose, nx, ny, nz);
	}
	
}
