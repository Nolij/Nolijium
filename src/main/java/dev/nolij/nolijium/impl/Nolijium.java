package dev.nolij.nolijium.impl;

//? if >=21.1 {
import dev.nolij.nolijium.impl.render.NolijiumHUDRenderLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
//?} else {
/*import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import dev.nolij.nolijium.impl.render.NolijiumHUDOverlay;
*///?}
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.nolij.libnolij.chroma.IChromaProvider;
import dev.nolij.libnolij.refraction.Refraction;
import dev.nolij.libnolij.util.ColourUtil;
import dev.nolij.libnolij.util.MathUtil;
import dev.nolij.nolijium.impl.render.ChromaShapeRenderer;
import dev.nolij.nolijium.impl.render.NolijiumLightOverlayRenderer;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import dev.nolij.nolijium.impl.integration.embeddium.NolijiumEmbeddiumConfigScreen;
import dev.nolij.nolijium.impl.util.PlatformUtil;
import dev.nolij.nolijium.mixin.LevelRendererAccessor;
import dev.nolij.nolijium.mixin.LightTextureAccessor;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import static dev.nolij.nolijium.impl.NolijiumConstants.*;

@Mod(value = MOD_ID/*? if >=21.1 { */, dist = Dist.CLIENT/*?}*/)
public class Nolijium {
	
	//region Constants
	public static final Refraction REFRACTION = Refraction.safe(MethodHandles.lookup());
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final String CONFIG_FILE_NAME = MOD_ID + ".json5";
	//endregion
	
	public static volatile NolijiumConfigImpl config = new NolijiumConfigImpl();
	
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
		
		for (final int newColour : oldPotionColours.keySet()) {
			oldPotionColours.put(oldPotionColours.get(newColour), newColour);
		}
	}
	
	public Nolijium(/*? if >=21.1 { */IEventBus modEventBus, ModContainer modContainer/*?}*/) {
		LOGGER.info("Loading Nolijium...");
		NolijiumConfigImpl.init(FMLPaths.CONFIGDIR.get(), CONFIG_FILE_NAME, Nolijium::onConfigReload);
		
		//? if >=21.1 {
		modContainer.registerExtensionPoint(IConfigScreenFactory.class, Nolijium::configScreenFactory);
		var gameBus = NeoForge.EVENT_BUS;
		//? } else {
		/*var modContainer = ModList.get().getModContainerById(MOD_ID).orElseThrow();
		@SuppressWarnings("removal") var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		var gameBus = MinecraftForge.EVENT_BUS;
		modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(Nolijium::configScreenFactory));
		*///? }
		
		modEventBus.addListener(this::onRegisterGuiLayers);
		modEventBus.addListener(this::onRegisterKeyMappings);
		gameBus.addListener(EventPriority.HIGHEST, this::onAddToast);
		gameBus.addListener(EventPriority.HIGHEST, this::onRenderTooltip);
		gameBus.addListener(EventPriority.HIGHEST, this::renderFog);
		gameBus.addListener(this::renderLevelStage);
		gameBus.addListener(this::onChunkUnload);
		gameBus.addListener(this::onTick);
		
		if (REFRACTION.getClassOrNull("org.embeddedt.embeddium.api.OptionGUIConstructionEvent") != null)
			new NolijiumEmbeddiumConfigScreen();
	}
	
	public static @Nullable IChromaProvider blockChromaProvider = null;
	public static @Nullable IChromaProvider tooltipBorderChromaProvider = null;
	public static @Nullable IChromaProvider tooltipBackgroundChromaProvider = null;
	public static @Nullable IChromaProvider hudChromaProvider = null;
	
	public static Set<ResourceLocation> blockedParticleTypeIDs = Set.of();
	
	private static void onConfigReload(NolijiumConfigImpl config) {
		Nolijium.config = config;
		
		blockChromaProvider = config.blockChromaConfig.getChromaProvider();
		tooltipBorderChromaProvider = config.tooltipChromaConfig.getChromaProvider();
		tooltipBackgroundChromaProvider = tooltipBorderChromaProvider; // might eventually split
		hudChromaProvider = config.hudChromaConfig.getChromaProvider();
		
		//noinspection DataFlowIssue
		blockedParticleTypeIDs = config.hideParticlesByID
			.stream()
			.map(ResourceLocation::tryParse)
			.collect(Collectors.toUnmodifiableSet());
		
		//noinspection ConstantValue
		if (config.enableGamma && Minecraft.getInstance().gameRenderer != null)
			((LightTextureAccessor) Minecraft.getInstance().gameRenderer.lightTexture()).nolijium$setUpdateLightTexture(true);
		
		//noinspection ConstantValue
		if (Minecraft.getInstance().levelRenderer != null)
			RenderSystem.recordRenderCall(() ->
				((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).nolijium$createStars());
	}
	
	public static int transformBlockOutlineColour(double timestamp, int colour) {
		if (blockChromaProvider != null)
			return blockChromaProvider.chroma(timestamp, 0) | 0xFF000000;
			
		if (config.enableOpaqueBlockOutlines)
			return colour | 0xFF000000;
		
		return colour;
	}
	
	public static boolean shouldHideToast(Toast toast) {
		if (Nolijium.config.hideAllToasts)
			return true;
		
		//noinspection IfCanBeSwitch
		if (toast instanceof AdvancementToast)
			return Nolijium.config.hideAdvancementToasts;
		else if (toast instanceof RecipeToast)
			return Nolijium.config.hideRecipeToasts;
		else if (toast instanceof SystemToast)
			return Nolijium.config.hideSystemToasts;
		else if (toast instanceof TutorialToast)
			return Nolijium.config.hideTutorialToasts;
		
		return false;
	}
	
	private static Component getTooltipInfo(ClickEvent clickEvent) {
		return Component.translatable(
				"nolijium.tooltip_info",
				PlatformUtil.getClickActionName(clickEvent.getAction()).toUpperCase(),
				clickEvent.getValue())
			.withStyle(ChatFormatting.DARK_GRAY);
	}
	
	private static final Map<Style, HoverEvent> hoverEventCache = Collections.synchronizedMap(new WeakHashMap<>());
	
	public static HoverEvent getHoverEvent(Style _style) {
		return hoverEventCache.computeIfAbsent(_style, style -> {
			final HoverEvent hoverEvent = style.getHoverEvent();
			final ClickEvent clickEvent = style.getClickEvent();
			
			if (clickEvent != null) {
				final Component tooltipInfo = getTooltipInfo(clickEvent);
				final MutableComponent newTooltip = MutableComponent.create(PlatformUtil.getEmptyComponentContents());
				
				if (hoverEvent == null) {
					newTooltip.append(tooltipInfo);
				} else {
					final Component text = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
					
					if (text != null) {
						newTooltip.append(text).append("\n");
					} else {
						final HoverEvent.EntityTooltipInfo entityInfo = hoverEvent.getValue(HoverEvent.Action.SHOW_ENTITY);
						
						if (entityInfo != null) {
							for (var line : entityInfo.getTooltipLines()) {
								newTooltip.append(line).append("\n");
							}
						} else {
							final HoverEvent.ItemStackInfo itemStackInfo = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
							
							if (itemStackInfo != null) {
								for (var line : Screen.getTooltipFromItem(Minecraft.getInstance(), itemStackInfo.getItemStack())) {
									newTooltip.append(line).append("\n");
								}
							} else {
								return hoverEvent;
							}
						}
					}
					
					newTooltip.append("\n").append(tooltipInfo);
				}
				
				return new HoverEvent(HoverEvent.Action.SHOW_TEXT, newTooltip);
			}
			
			return hoverEvent;
		});
	}
	
	public static volatile Vec3 focusedBlockPosition = null;
	public static volatile VoxelShape focusedBlockShape = null;
	
	public static void renderAfterTranslucentBlocks(PoseStack poseStack) {
		if (focusedBlockPosition == null || focusedBlockShape == null || Nolijium.blockChromaProvider == null)
			return;
		
		final var timestamp = System.nanoTime() * 1E-9D;
		final var colour = Nolijium.blockChromaProvider.chroma(timestamp, 0);
		
		ChromaShapeRenderer.render(
			poseStack,
			focusedBlockShape,
			focusedBlockPosition.x,
			focusedBlockPosition.y,
			focusedBlockPosition.z,
			(float) ColourUtil.getRedD(colour),
			(float) ColourUtil.getGreenD(colour),
			(float) ColourUtil.getBlueD(colour),
			Nolijium.config.blockShapeOverlayOverride > 0
			? Nolijium.config.blockShapeOverlayOverride
			: Nolijium.config.chromaBlockShapeOverlay);
		
		focusedBlockPosition = null;
		focusedBlockShape = null;
	}
	
	private static Screen configScreenFactory(Object ignored, Screen parent) {
		return new Screen(Component.empty()) {
			@Override
			protected void init() {
				NolijiumConfigImpl.openConfigFile();
				Minecraft.getInstance().setScreen(parent);
			}
		};
	}
	
	private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		for (var keyBind : NolijiumKeyBind.values()) {
			event.register(keyBind.value);
		}
	}
	
	//? if >=21.1 {
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
		
		if (shouldHideToast(event.getToast()))
			event.setCanceled(true);
	}
	
	private void onRenderTooltip(RenderTooltipEvent.Color event) {
		if (config.tooltipColourOverride) {
			event.setBorderStart(config.tooltipBorderStart);
			event.setBorderEnd(config.tooltipBorderEnd);
			event.setBackgroundStart(config.tooltipBackgroundStart);
			event.setBackgroundEnd(config.tooltipBackgroundEnd);
			
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
			NolijiumLightOverlayRenderer.render(
				event.getCamera(),
				//? if >=21.1 {
				event.getModelViewMatrix(),
				//? } else
				//event.getPoseStack(), 
				RenderType.cutout());
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
			renderAfterTranslucentBlocks(event.getPoseStack());
		}
	}
	
	private void onChunkUnload(ChunkEvent.Unload event) {
		if (event.getLevel().isClientSide()) {
			NolijiumLightOverlayRenderer.invalidateChunk(event.getLevel(), event.getChunk().getPos());
		}
	}
	
	//? if >=21.1 {
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
		if (NolijiumKeyBind.TOGGLE_LIGHT_LEVEL_OVERLAY.wasPressed()) {
			NolijiumKeyBind.TOGGLE_LIGHT_LEVEL_OVERLAY.flush();
			Nolijium.config.modify(config -> config.enableLightLevelOverlay = !config.enableLightLevelOverlay);
		}
	}
	
}
