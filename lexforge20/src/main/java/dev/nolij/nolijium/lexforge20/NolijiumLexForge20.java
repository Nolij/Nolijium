package dev.nolij.nolijium.lexforge20;

import dev.nolij.nolijium.common.INolijiumSubImplementation;
import dev.nolij.nolijium.common.NolijiumCommon;
import dev.nolij.nolijium.impl.INolijiumImplementation;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import dev.nolij.nolijium.impl.util.MethodHandleHelper;
import dev.nolij.nolijium.impl.util.RGBHelper;
import dev.nolij.nolijium.lexforge20.integration.embeddium.NolijiumEmbeddiumConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ToastAddEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import static dev.nolij.nolijium.impl.NolijiumConstants.*;

@Mod(MOD_ID)
public class NolijiumLexForge20 implements INolijiumSubImplementation {
	
	public NolijiumLexForge20() {
		if (!FMLEnvironment.dist.isClient())
			return;
		
		new NolijiumCommon(this, FMLPaths.CONFIGDIR.get());
		
		ModLoadingContext.get().registerExtensionPoint(
			ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new Screen(Component.nullToEmpty(null)) {
				@Override
				public void tick() {
					NolijiumConfigImpl.openConfigFile();
					Minecraft.getInstance().setScreen(parent);
				}
			}));
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterGuiOverlays);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onAddToast);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onRenderTooltip);
		
		if (MethodHandleHelper.PUBLIC.getClassOrNull("org.embeddedt.embeddium.api.OptionGUIConstructionEvent") != null)
			new NolijiumEmbeddiumConfigScreen();
	}
	
	private void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
		event.registerAboveAll("hud", new NolijiumHUDOverlay());
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
	
	@Override
	public String getClickActionName(ClickEvent.Action action) {
		return action.getName();
	}
	
	@Override
	public ComponentContents getEmptyComponentContents() {
		return ComponentContents.EMPTY;
	}
	
}
