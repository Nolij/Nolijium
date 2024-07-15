package dev.nolij.nolijium.neoforge;

import dev.nolij.nolijium.common.NolijiumCommon;
import dev.nolij.nolijium.impl.INolijiumImplementation;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.invoke.MethodHandles;

import static dev.nolij.nolijium.impl.NolijiumConstants.*;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class NolijiumNeoForge implements INolijiumImplementation {
	
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
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onAddToast);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onRenderTooltip);
		
		if (METHOD_HANDLE_HELPER.getClassOrNull("org.embeddedt.embeddium.api.OptionGUIConstructionEvent") != null)
			new NolijiumEmbeddiumConfigScreen();
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
		
		event.setBorderStart(RGBHelper.chroma(timestamp, Nolijium.config.chromaSpeed, 2));
		event.setBorderEnd(RGBHelper.chroma(timestamp, Nolijium.config.chromaSpeed, 1));
		event.setBackgroundStart(RGBHelper.chroma(timestamp, Nolijium.config.chromaSpeed, 2, 0.25D));
		event.setBackgroundEnd(RGBHelper.chroma(timestamp, Nolijium.config.chromaSpeed, 1, 0.25D));
	}
}
