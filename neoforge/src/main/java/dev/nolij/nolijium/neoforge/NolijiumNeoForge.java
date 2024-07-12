package dev.nolij.nolijium.neoforge;

import dev.nolij.nolijium.impl.INolijiumImplementation;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import dev.nolij.nolijium.impl.util.MethodHandleHelper;
import dev.nolij.nolijium.impl.util.RGBHelper;
import dev.nolij.nolijium.integration.embeddium.NolijiumEmbeddiumConfigScreen;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ColorParticleOption;
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
import net.neoforged.neoforge.event.entity.living.EffectParticleModificationEvent;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.nolij.nolijium.impl.NolijiumConstants.*;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class NolijiumNeoForge implements INolijiumImplementation {
	
	private static final MethodHandleHelper METHOD_HANDLE_HELPER =
		new MethodHandleHelper(NolijiumNeoForge.class.getClassLoader(), MethodHandles.lookup());
	
	public NolijiumNeoForge(IEventBus modEventBus, ModContainer modContainer) {
		Nolijium.LOGGER.info("Loading Nolijium...");
		
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
		
		Nolijium.registerImplementation(this, FMLPaths.CONFIGDIR.get());
		
		modEventBus.addListener(this::onRegisterGuiLayers);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onAddToast);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onRenderTooltip);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onEffectParticle);
		
		if (METHOD_HANDLE_HELPER.getClassOrNull("org.embeddedt.embeddium.api.OptionGUIConstructionEvent") != null)
			new NolijiumEmbeddiumConfigScreen();
	}
	
	public static Set<ResourceLocation> blockedParticleTypeIDs = Set.of();
	
	@Override
	public void onConfigReload(NolijiumConfigImpl config) {
		blockedParticleTypeIDs = config.hideParticlesByID
			.stream()
			.map(ResourceLocation::parse)
			.collect(Collectors.toUnmodifiableSet());
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
		
		switch (toast) {
			case AdvancementToast advancementToast -> event.setCanceled(Nolijium.config.hideAdvancementToasts);
			case RecipeToast recipeToast -> event.setCanceled(Nolijium.config.hideRecipeToasts);
			case SystemToast systemToast -> event.setCanceled(Nolijium.config.hideSystemToasts);
			case TutorialToast tutorialToast -> event.setCanceled(Nolijium.config.hideTutorialToasts);
			default -> {}
		}
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
	
	public static final Int2IntMap oldPotionColours = new Int2IntOpenHashMap();
	static {
		oldPotionColours.put(0x33EBFF, 0x7CAFC6); // speed
		oldPotionColours.put(0x8BAFE0, 0x5A6C81); // slowness
		oldPotionColours.put(0xFFC700, 0x932423); // strength
		oldPotionColours.put(0xA9656A, 0x430A09); // instant_damage
		oldPotionColours.put(0xFDFF84, 0x22FF4C); // jump_boost
		oldPotionColours.put(0x9146F0, 0x99453A); // resistance
		oldPotionColours.put(0xFF9900, 0xE49A3A); // fire_resistance
		oldPotionColours.put(0x98DAC0, 0x2E5299); // water_breathing
		oldPotionColours.put(0xF6F6F6, 0x7F8392); // invisibility
		oldPotionColours.put(0xC2FF66, 0x1F1FA1); // night_vision
		oldPotionColours.put(0x87A363, 0x4E9331); // poison
		oldPotionColours.put(0x736156, 0x352A27); // wither
		oldPotionColours.put(0x59C106, 0x339900); // luck
		oldPotionColours.put(0xF3CFB9, 0xFFEFD1); // slow_falling
	}
	
	private void onEffectParticle(EffectParticleModificationEvent event) {
		if (Nolijium.config.revertPotions && 
			event.getParticleOptions() instanceof ColorParticleOption colourOption) {
			final int colour = RGBHelper.getColour(
				0, 
				colourOption.getRed(), 
				colourOption.getGreen(), 
				colourOption.getBlue());
			
			event.setParticleOptions(ColorParticleOption.create(
				colourOption.getType(), 
				oldPotionColours.getOrDefault(colour, colour) | 0xFF << 24
			));
		}
	}
	
}
