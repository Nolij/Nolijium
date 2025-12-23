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
//? if >=1.21.1
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLPaths;
//? if >=1.21.1 {
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
//? }
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
//? if >=1.21.1 {
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
//? }
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.lang.invoke.MethodHandles;

import static dev.nolij.nolijium.impl.NolijiumConstants.*;

@Mod(value = MOD_ID/*? if >=1.21.1 { */, dist = Dist.CLIENT/*?}*/)
public class NolijiumMod implements INolijiumSubImplementation {
	
	private static final Refraction METHOD_HANDLE_HELPER =
		new Refraction(MethodHandles.lookup());
	
	public NolijiumMod(/*? if >=1.21.1 { */IEventBus modEventBus, ModContainer modContainer/*?}*/) {
		new NolijiumCommon(this, FMLPaths.CONFIGDIR.get());
		
		//? if >=1.21.1 {
		modContainer.registerExtensionPoint(IConfigScreenFactory.class, this::configScreenFactory);
		var gameBus = NeoForge.EVENT_BUS;
		//? } else {
		/*var modContainer = ModList.get().getModContainerById(MOD_ID).orElseThrow();
		var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		var gameBus = MinecraftForge.EVENT_BUS;
		modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(this::configScreenFactory));
		*///? }
		
		modEventBus.addListener(this::onRegisterGuiLayers);
		modEventBus.addListener(this::onRegisterKeyMappings);
		gameBus.addListener(EventPriority.HIGHEST, this::onAddToast);
		gameBus.addListener(EventPriority.HIGHEST, this::onRenderTooltip);
		gameBus.addListener(EventPriority.HIGHEST, this::renderFog);
		gameBus.addListener(this::renderLevelStage);
		gameBus.addListener(this::onChunkUnload);
		gameBus.addListener(this::onTick);
		
		if (METHOD_HANDLE_HELPER.getClassOrNull("org.embeddedt.embeddium.api.OptionGUIConstructionEvent") != null)
			new NolijiumEmbeddiumConfigScreen();
	}
	
	private Screen configScreenFactory(Minecraft minecraft, Screen parent) {
		return new Screen(Component.empty()) {
			@Override
			protected void init() {
				NolijiumConfigImpl.openConfigFile();
				Minecraft.getInstance().setScreen(parent);
			}
		};
	}
	
	private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		for (var keyBind : NolijiumNeoForgeKeyBind.values()) {
			event.register(keyBind.value);
		}
	}
	
	//? if >=1.21.1 {
	private void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
		event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MOD_ID, "hud"), new NolijiumHUDRenderLayer());
	}
	//? } else {
	/*private void onRegisterGuiLayers(RegisterGuiOverlaysEvent event) {
		event.registerAboveAll("hud", new NolijiumHUDOverlay());
	}
	*///? }
	
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
			NolijiumLightOverlayRenderer.render(event.getCamera(), event.getPoseStack(), RenderType.cutout());
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
			NolijiumCommon.renderAfterTranslucentBlocks(event.getPoseStack());
		}
	}
	
	private void onChunkUnload(ChunkEvent.Unload event) {
		if (event.getLevel().isClientSide()) {
			NolijiumLightOverlayRenderer.invalidateChunk(event.getLevel(), event.getChunk().getPos());
		}
	}
	
	//? if >=1.21.1 {
	private void onTick(ClientTickEvent.Pre event) {
		onClientStartTick();
	}
	//? } else {
	/*private void onTick(TickEvent.ClientTickEvent e) {
		if (e.phase == TickEvent.Phase.START) {
			onClientStartTick();
		}
	}
	*///? }
	
	private void onClientStartTick() {
		if (NolijiumNeoForgeKeyBind.TOGGLE_LIGHT_LEVEL_OVERLAY.wasPressed()) {
			NolijiumNeoForgeKeyBind.TOGGLE_LIGHT_LEVEL_OVERLAY.flush();
			Nolijium.config.modify(config -> config.enableLightLevelOverlay = !config.enableLightLevelOverlay);
		}
	}
	
	@Override
	public String getClickActionName(ClickEvent.Action action) {
		//? if >=1.21.1 {
		return action.getSerializedName();
		//? } else {
		/*return action.getName();
		*///? }
	}
	
	@Override
	public ComponentContents getEmptyComponentContents() {
		//? if >=1.21.1 {
		return PlainTextContents.LiteralContents.EMPTY;
		//? } else {
		/*return ComponentContents.EMPTY;
		*///? }
	}
	
	@Override
	public boolean supportsLightLevelOverlay() {
		return true;
	}
	
	@Override
	public void addLineVertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, int color, float nx, float ny, float nz) {
		//? if >=1.21.1 {
		consumer.addVertex(pose, x, y, z).setColor(color).setNormal(pose, nx, ny, nz);
		//? } else {
		/*consumer.vertex(pose.pose(), x, y, z).color(color).normal(pose.normal(), nx, ny, nz).endVertex();
		*///? }
	}
	
	@Override
	public void addLineVertex(VertexConsumer consumer, float x, float y, float z, int color, float nx, float ny, float nz) {
		//? if >=1.21.1 {
		consumer.addVertex(x, y, z).setColor(color).setNormal(nx, ny, nz);
		 //? } else {
		/*consumer.vertex(x, y, z).color(color).normal(nx, ny, nz).endVertex();
		*///? }
	}
}
