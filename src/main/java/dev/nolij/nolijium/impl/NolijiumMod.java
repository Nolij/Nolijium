package dev.nolij.nolijium.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.nolij.libnolij.refraction.Refraction;
import dev.nolij.libnolij.util.MathUtil;
import dev.nolij.nolijium.impl.common.INolijiumSubImplementation;
import dev.nolij.nolijium.impl.common.NolijiumCommon;
import dev.nolij.nolijium.impl.common.NolijiumLightOverlayRenderer;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import dev.nolij.nolijium.impl.integration.embeddium.NolijiumEmbeddiumConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.lang.invoke.MethodHandles;

import static dev.nolij.nolijium.impl.NolijiumConstants.*;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class NolijiumMod implements INolijiumSubImplementation {
	
	private static final Refraction METHOD_HANDLE_HELPER =
		new Refraction(MethodHandles.lookup());
	
	public NolijiumMod(IEventBus modEventBus, ModContainer modContainer) {
		new NolijiumCommon(this, FMLPaths.CONFIGDIR.get());
		
		modContainer.registerExtensionPoint(
			IConfigScreenFactory.class, 
			(minecraft, parent) -> 
				new Screen(Component.empty()) {
					@Override
					protected void init() {
						NolijiumConfigImpl.openConfigFile();
						Minecraft.getInstance().setScreen(parent);
					}
				});
		
		modEventBus.addListener(this::onRegisterGuiLayers);
		modEventBus.addListener(this::onRegisterKeyMappings);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onAddToast);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onRenderTooltip);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::renderFog);
		NeoForge.EVENT_BUS.addListener(this::renderLevelStage);
		NeoForge.EVENT_BUS.addListener(this::onChunkUnload);
		NeoForge.EVENT_BUS.addListener(this::onTick);
		
		if (METHOD_HANDLE_HELPER.getClassOrNull("org.embeddedt.embeddium.api.OptionGUIConstructionEvent") != null)
			new NolijiumEmbeddiumConfigScreen();
	}
	
	private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		for (var keyBind : NolijiumNeoForgeKeyBind.values()) {
			event.register(keyBind.value);
		}
	}
	
	private void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
		event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MOD_ID, "hud"), new NolijiumHUDRenderLayer());
	}
	
	private void onAddToast(ToastAddEvent event) {
		if (event.isCanceled())
			return;
		
		if (NolijiumCommon.shouldHideToast(event.getToast()))
			event.setCanceled(true);
	}
	
	private void onRenderTooltip(RenderTooltipEvent.Color event) {
		if (Nolijium.config.tooltipColourOverride) {
			event.setBorderStart(Nolijium.config.tooltipBorderStart);
			event.setBorderEnd(Nolijium.config.tooltipBorderEnd);
			event.setBackgroundStart(Nolijium.config.tooltipBackgroundStart);
			event.setBackgroundEnd(Nolijium.config.tooltipBackgroundEnd);
			
			return;
		}
		
		final double timestamp = System.nanoTime() * 1E-9D;
		
		if (Nolijium.tooltipBorderChromaProvider != null) {
			event.setBorderStart(Nolijium.tooltipBorderChromaProvider.chroma(timestamp, 0) | 0xFF000000);
			event.setBorderEnd(Nolijium.tooltipBorderChromaProvider.chroma(timestamp, 0.2D) | 0xFF000000);
		}
		if (Nolijium.tooltipBackgroundChromaProvider != null) {
			event.setBackgroundStart(Nolijium.tooltipBackgroundChromaProvider.chroma(timestamp, 0, 0.25D) | 0xFF000000);
			event.setBackgroundEnd(Nolijium.tooltipBackgroundChromaProvider.chroma(timestamp, 0.2D, 0.25D) | 0xFF000000);
		}
	}
	
	private void renderFog(ViewportEvent.RenderFog event) {
		if (event.getType() != FogType.NONE)
			return;
		
		if (Nolijium.config.disableFog) {
			if (event.getMode() == FogRenderer.FogMode.FOG_SKY)
				return;
			
			event.setCanceled(true);
			
			event.setNearPlaneDistance(Float.MAX_VALUE);
			event.setFarPlaneDistance(Float.MAX_VALUE);
			return;
		}
		
		if (Nolijium.config.fogOverride != 0) {
			event.setCanceled(true);
			final float distance = Nolijium.config.fogOverride * 16;
			
			if (event.getMode() != FogRenderer.FogMode.FOG_SKY)
				event.setNearPlaneDistance(distance - (float) MathUtil.clamp(distance * 0.1D, 4D, 64D));
			event.setFarPlaneDistance(distance);
		} else if (Nolijium.config.fogMultiplier != 1F) {
			event.setCanceled(true);
			
			event.scaleNearPlaneDistance(Nolijium.config.fogMultiplier);
			event.scaleFarPlaneDistance(Nolijium.config.fogMultiplier);
		}
		
		if (event.getMode() != FogRenderer.FogMode.FOG_SKY && 
			Nolijium.config.fogStartMultiplier != 1F) {
			event.setCanceled(true);
			
			event.scaleNearPlaneDistance(Nolijium.config.fogStartMultiplier);
		}
	}
	
	private void renderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
			NolijiumLightOverlayRenderer.render(event.getCamera(), event.getModelViewMatrix(), RenderType.cutout());
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
			NolijiumCommon.renderAfterTranslucentBlocks(event.getPoseStack());
		}
	}
	
	private void onChunkUnload(ChunkEvent.Unload event) {
		if (event.getLevel().isClientSide()) {
			NolijiumLightOverlayRenderer.invalidateChunk(event.getChunk().getLevel(), event.getChunk().getPos());
		}
	}
	
	private void onTick(ClientTickEvent.Pre event) {
		if (NolijiumNeoForgeKeyBind.TOGGLE_LIGHT_LEVEL_OVERLAY.wasPressed()) {
			NolijiumNeoForgeKeyBind.TOGGLE_LIGHT_LEVEL_OVERLAY.flush();
			Nolijium.config.modify(config -> config.enableLightLevelOverlay = !config.enableLightLevelOverlay);
		}
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
