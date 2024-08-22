package dev.nolij.nolijium.neoforge;

import dev.nolij.nolijium.common.INolijiumSubImplementation;
import dev.nolij.nolijium.common.NolijiumCommon;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import dev.nolij.nolijium.impl.util.MethodHandleHelper;
import dev.nolij.nolijium.impl.util.RGBHelper;
import dev.nolij.nolijium.neoforge.integration.embeddium.NolijiumEmbeddiumConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
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
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.lang.invoke.MethodHandles;

import static dev.nolij.nolijium.impl.NolijiumConstants.*;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class NolijiumNeoForge implements INolijiumSubImplementation {
	
	private static final MethodHandleHelper METHOD_HANDLE_HELPER =
		new MethodHandleHelper(NolijiumNeoForge.class.getClassLoader(), MethodHandles.lookup());
	
	public NolijiumNeoForge(IEventBus modEventBus, ModContainer modContainer) {
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
		
		if (Nolijium.config.hideAllToasts) {
			event.setCanceled(true);
			return;
		}
		
		final Toast toast = event.getToast();
		
		//noinspection IfCanBeSwitch
		if (toast instanceof AdvancementToast)
			event.setCanceled(Nolijium.config.hideAdvancementToasts);
		else if (toast instanceof RecipeToast)
			event.setCanceled(Nolijium.config.hideRecipeToasts);
		else if (toast instanceof SystemToast)
			event.setCanceled(Nolijium.config.hideSystemToasts);
		else if (toast instanceof TutorialToast)
			event.setCanceled(Nolijium.config.hideTutorialToasts);
	}
	
	private void onRenderTooltip(RenderTooltipEvent.Color event) {
		if (!Nolijium.config.enableChromaToolTips)
			return;
		
		final double timestamp = System.nanoTime() * 1E-9D;
		
		event.setBorderStart(RGBHelper.chroma(timestamp, Nolijium.config.chromaSpeed, 0));
		event.setBorderEnd(RGBHelper.chroma(timestamp, Nolijium.config.chromaSpeed, -2));
		event.setBackgroundStart(RGBHelper.chroma(timestamp, Nolijium.config.chromaSpeed, 0, 0.25D));
		event.setBackgroundEnd(RGBHelper.chroma(timestamp, Nolijium.config.chromaSpeed, -2, 0.25D));
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
	
	private void renderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
			NolijiumLightOverlayRenderer.render(event.getCamera(), event.getModelViewMatrix(), RenderType.cutout());
		}
	}
	
	private void onChunkUnload(ChunkEvent.Unload event) {
		if (event.getLevel().isClientSide()) {
			NolijiumLightOverlayRenderer.invalidateChunk(event.getChunk().getLevel(), event.getChunk().getPos());
		}
	}
	
}
