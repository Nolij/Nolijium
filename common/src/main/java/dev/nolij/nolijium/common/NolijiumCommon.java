package dev.nolij.nolijium.common;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.nolij.libnolij.util.ColourUtil;
import dev.nolij.nolijium.impl.INolijiumImplementation;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import dev.nolij.nolijium.mixin.common.LevelRendererAccessor;
import dev.nolij.nolijium.mixin.common.LightTextureAccessor;
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
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

@Mod("nolijium_common")
public class NolijiumCommon implements INolijiumImplementation {
	
	private static NolijiumCommon instance = null;
	
	public static NolijiumCommon getInstance() {
		return instance;
	}
	
	private final INolijiumSubImplementation platformImplementation;
	
	public static INolijiumSubImplementation getImplementation() {
		if (instance == null)
			return null;
		
		return instance.platformImplementation;
	}
	
	@SuppressWarnings("unused")
	public NolijiumCommon() {
		this.platformImplementation = null;
	}
	
	public NolijiumCommon(INolijiumSubImplementation platformImplementation, Path configPath) {
		if (instance != null)
			throw new IllegalStateException("Nolijium is already initialized!");
		
		Nolijium.LOGGER.info("Loading Nolijium...");
		instance = this;
		
		this.platformImplementation = platformImplementation;
		
		Nolijium.registerImplementation(this, configPath);
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
	
	public static Set<ResourceLocation> blockedParticleTypeIDs = Set.of();
	
	@Override
	public void onConfigReload(NolijiumConfigImpl config) {
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
		
		platformImplementation.onConfigReload(config);
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
		
		for (final int newColour : oldPotionColours.keySet()) {
			oldPotionColours.put(oldPotionColours.get(newColour), newColour);
		}
	}
	
	private static Component getTooltipInfo(ClickEvent clickEvent) {
		return Component.translatable(
			"nolijium.tooltip_info",
			instance.platformImplementation.getClickActionName(clickEvent.getAction()).toUpperCase(),
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
				final MutableComponent newTooltip = MutableComponent.create(instance.platformImplementation.getEmptyComponentContents());
				
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
		if (focusedBlockPosition == null || focusedBlockShape == null)
			return;
		
		if (Nolijium.config.blockShapeOverlayOverride != 0) {
			ChromaShapeRenderer.render(
				poseStack,
				focusedBlockShape,
				focusedBlockPosition.x,
				focusedBlockPosition.y,
				focusedBlockPosition.z,
				(float) ColourUtil.getRed(Nolijium.config.blockShapeOverlayOverride),
				(float) ColourUtil.getGreen(Nolijium.config.blockShapeOverlayOverride),
				(float) ColourUtil.getBlue(Nolijium.config.blockShapeOverlayOverride),
				(float) ColourUtil.getAlpha(Nolijium.config.blockShapeOverlayOverride));
		} else {
			final double timestamp = System.nanoTime() * 1E-9D;
			
			ChromaShapeRenderer.render(
				poseStack,
				focusedBlockShape,
				focusedBlockPosition.x,
				focusedBlockPosition.y,
				focusedBlockPosition.z,
				(float) ColourUtil.chromaRed(timestamp, Nolijium.config.chromaSpeed, 0),
				(float) ColourUtil.chromaGreen(timestamp, Nolijium.config.chromaSpeed, 0),
				(float) ColourUtil.chromaBlue(timestamp, Nolijium.config.chromaSpeed, 0),
				Nolijium.config.chromaBlockShapeOverlay);
		}
		
		focusedBlockPosition = null;
		focusedBlockShape = null;
	}
	
}
