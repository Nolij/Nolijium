package dev.nolij.nolijium.neoforge;

import dev.nolij.nolijium.impl.INolijiumImplementation;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import dev.nolij.nolijium.impl.util.MethodHandleHelper;
import dev.nolij.nolijium.integration.embeddium.NolijiumEmbeddiumConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ToastAddEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.nolij.nolijium.impl.NolijiumConstants.*;

@Mod(value = MOD_ID)
public class NolijiumNeoForge implements INolijiumImplementation {
	
	private static final MethodHandleHelper METHOD_HANDLE_HELPER =
		new MethodHandleHelper(NolijiumNeoForge.class.getClassLoader(), MethodHandles.lookup());
	
	public NolijiumNeoForge() {
		if(FMLLoader.getDist() != Dist.CLIENT) {
			return;
		}
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModContainer modContainer = ModList.get().getModContainerById(MOD_ID).orElseThrow();
		Nolijium.LOGGER.info("Loading Nolijium...");
		
		modContainer.registerExtensionPoint(
			ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> 
				new Screen(Component.empty()) {
					@Override
					protected void init() {
						NolijiumConfigImpl.openConfigFile();
						Minecraft.getInstance().setScreen(parent);
					}
				}));
		
		Nolijium.registerImplementation(this, FMLPaths.CONFIGDIR.get());
		
		modEventBus.addListener(this::onRegisterGuiLayers);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onAddToast);
		
		if (METHOD_HANDLE_HELPER.getClassOrNull("org.embeddedt.embeddium.api.OptionGUIConstructionEvent") != null)
			new NolijiumEmbeddiumConfigScreen();
	}
	
	public static Set<ResourceLocation> blockedParticleTypeIDs = Set.of();
	
	@Override
	public void onConfigReload(NolijiumConfigImpl config) {
		blockedParticleTypeIDs = config.hideParticlesByID
			.stream()
			.map(ResourceLocation::new)
			.collect(Collectors.toUnmodifiableSet());
	}
	
	private void onRegisterGuiLayers(RegisterGuiOverlaysEvent event) {
		event.registerAboveAll("hud", new NolijiumHUDRenderLayer());
	}
	
	private void onAddToast(ToastAddEvent event) {
		if (event.isCanceled())
			return;
		
		if (Nolijium.config.hideAllToasts) {
			event.setCanceled(true);
			return;
		}
		
		final Toast toast = event.getToast();
		
		if(toast instanceof AdvancementToast) {
			event.setCanceled(Nolijium.config.hideAdvancementToasts);
		} else if(toast instanceof RecipeToast) {
			event.setCanceled(Nolijium.config.hideRecipeToasts);
		} else if(toast instanceof SystemToast) {
			event.setCanceled(Nolijium.config.hideSystemToasts);
		} else if(toast instanceof TutorialToast) {
			event.setCanceled(Nolijium.config.hideTutorialToasts);
		}
	}
	
}
