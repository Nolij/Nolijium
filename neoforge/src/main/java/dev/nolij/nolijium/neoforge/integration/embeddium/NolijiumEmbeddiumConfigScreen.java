package dev.nolij.nolijium.neoforge.integration.embeddium;

import com.google.common.collect.ImmutableList;
import dev.nolij.nolijium.common.ResourceLocationComparator;
import dev.nolij.nolijium.impl.util.Alignment;
import dev.nolij.nolijium.impl.util.DetailLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.embeddedt.embeddium.api.OptionGUIConstructionEvent;
import org.embeddedt.embeddium.api.eventbus.EventHandlerRegistrar;
import org.embeddedt.embeddium.api.options.OptionIdentifier;
import org.embeddedt.embeddium.api.options.control.ControlValueFormatter;
import org.embeddedt.embeddium.api.options.control.CyclingControl;
import org.embeddedt.embeddium.api.options.control.SliderControl;
import org.embeddedt.embeddium.api.options.control.TickBoxControl;
import org.embeddedt.embeddium.api.options.structure.Option;
import org.embeddedt.embeddium.api.options.structure.OptionGroup;
import org.embeddedt.embeddium.api.options.structure.OptionImpl;
import org.embeddedt.embeddium.api.options.structure.OptionPage;

import java.util.ArrayList;
import java.util.List;

import static dev.nolij.nolijium.impl.NolijiumConstants.*;

public class NolijiumEmbeddiumConfigScreen implements EventHandlerRegistrar.Handler<OptionGUIConstructionEvent> {
	
	public NolijiumEmbeddiumConfigScreen() {
		OptionGUIConstructionEvent.BUS.addListener(this);
	}
	
	private static OptionIdentifier<Void> id(String path) {
		return id(path, void.class);
	}
	
	private static <T> OptionIdentifier<T> id(String path, Class<T> type) {
		return OptionIdentifier.create(MOD_ID, path, type);
	}
	
	@Override
	public void acceptEvent(OptionGUIConstructionEvent event) {
		final NolijiumOptionsStorage storage = new NolijiumOptionsStorage();
		
		final List<OptionGroup> utilitiesPage = new ArrayList<>();
		final List<OptionGroup> togglesPage = new ArrayList<>();
		final List<OptionGroup> particlesPage = new ArrayList<>();
		final List<OptionGroup> chromaPage = new ArrayList<>();
		
		utilitiesPage.add(OptionGroup.createBuilder()
			.setId(id("utilities"))
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(id("max_chat_history", int.class))
				.setControl(option -> new SliderControl(option, 0, 2000, 100,
					v -> v > 0 
					     ? Component.translatable("nolijium.messages", v)
					     : Component.translatable("nolijium.unlimited")))
				.setBinding(
					(config, value) -> config.maxChatHistory = value,
					config -> config.maxChatHistory)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("enable_tooltip_info", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.enableToolTipInfo = value,
					config -> config.enableToolTipInfo)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("enable_light_level_overlay", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.enableLightLevelOverlay = value,
					config -> config.enableLightLevelOverlay)
				.build())
			.build());
		
		utilitiesPage.add(OptionGroup.createBuilder()
			.setId(id("stars"))
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(id("star_count", int.class))
				.setControl(option -> new SliderControl(option, 0, 50000, 500, ControlValueFormatter.number()))
				.setBinding(
					(config, value) -> config.starCount = value,
					config -> config.starCount)
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(id("star_scale", int.class))
				.setControl(option -> new SliderControl(option, 0, 100, 5, ControlValueFormatter.percentage()))
				.setBinding(
					(config, value) -> config.starScale = value * 0.01F,
					config -> Math.round(config.starScale * 100))
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(id("star_brightness", int.class))
				.setControl(option -> new SliderControl(option, 0, 100, 5, ControlValueFormatter.percentage()))
				.setBinding(
					(config, value) -> config.starBrightness = value * 0.01F,
					config -> Math.round(config.starBrightness * 100))
				.build())
			.build());
		
		final Option<Boolean> disableFogOption;
		final Option<Integer> fogOverrideOption;
		utilitiesPage.add(OptionGroup.createBuilder()
			.setId(id("fog"))
			.add(disableFogOption = OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("disable_fog", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.disableFog = value,
					config -> config.disableFog)
				.build())
			.add(fogOverrideOption = OptionImpl.createBuilder(int.class, storage)
				.setId(id("fog_override", int.class))
				.setControl(option -> new SliderControl(option, 0, 32, 1,
					v -> v == 0 ? Component.translatable("nolijium.none") : Component.translatable("nolijium.chunks", v)))
				.setBinding(
					(config, value) -> config.fogOverride = value,
					config -> config.fogOverride)
				.setEnabledPredicate(() -> !disableFogOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(id("fog_multiplier", int.class))
				.setControl(option -> new SliderControl(option, 10, 200, 10,
					v -> v == 100 ? Component.translatable("nolijium.none") :  Component.translatable("nolijium.percentage", v)))
				.setBinding(
					(config, value) -> config.fogMultiplier = value / 100F,
					config -> Math.round(config.fogMultiplier * 100))
				.setEnabledPredicate(() -> !disableFogOption.getValue() && fogOverrideOption.getValue() == 0)
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(id("fog_start_multiplier", int.class))
				.setControl(option -> new SliderControl(option, 0, 100, 10,
					v -> v == 100 ? Component.translatable("nolijium.none") :  Component.translatable("nolijium.percentage", v)))
				.setBinding(
					(config, value) -> config.fogStartMultiplier = value / 100F,
					config -> Math.round(config.fogStartMultiplier * 100))
				.setEnabledPredicate(() -> !disableFogOption.getValue())
				.build())
			.build());
		
		final Option<Boolean> hudEnabledOption;
		final Option<DetailLevel> hudShowFPSOption;
		utilitiesPage.add(OptionGroup.createBuilder()
			.setId(id("hud"))
			.add(hudEnabledOption = OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("hud_enabled", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hudEnabled = value,
					config -> config.hudEnabled)
				.build())
			.add(OptionImpl.createBuilder(Alignment.X.class, storage)
				.setId(id("hud_alignment_x", Alignment.X.class))
				.setControl(option -> new CyclingControl<>(option, Alignment.X.class))
				.setBinding(
					(config, value) -> config.hudAlignmentX = value,
					config -> config.hudAlignmentX)
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.add(OptionImpl.createBuilder(Alignment.Y.class, storage)
				.setId(id("hud_alignment_y", Alignment.Y.class))
				.setControl(option -> new CyclingControl<>(option, Alignment.Y.class))
				.setBinding(
					(config, value) -> config.hudAlignmentY = value,
					config -> config.hudAlignmentY)
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(id("hud_margin_x", int.class))
				.setControl(option -> 
					new SliderControl(option, 0, 100, 5, 
						v -> Component.translatable("nolijium.pixels", v)))
				.setBinding(
					(config, value) -> config.hudMarginX = value,
					config -> config.hudMarginX)
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(id("hud_margin_y", int.class))
				.setControl(option -> 
					new SliderControl(option, 0, 100, 5, 
						v -> Component.translatable("nolijium.pixels", v)))
				.setBinding(
					(config, value) -> config.hudMarginY = value,
					config -> config.hudMarginY)
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("hud_background", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hudBackground = value,
					config -> config.hudBackground)
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("hud_shadow", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hudShadow = value,
					config -> config.hudShadow)
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(id("hud_refresh_rate_ticks", int.class))
				.setControl(option ->
					new SliderControl(option, 0, 20, 1, 
						v -> v != 0 
						     ? Component.translatable("nolijium.ticks", v)
						     : Component.translatable("nolijium.every_frame")))
				.setBinding(
					(config, value) -> config.hudRefreshRateTicks = value,
					config -> config.hudRefreshRateTicks)
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.add(hudShowFPSOption = OptionImpl.createBuilder(DetailLevel.class, storage)
				.setId(id("hud_show_fps", DetailLevel.class))
				.setControl(option -> new CyclingControl<>(option, DetailLevel.class))
				.setBinding(
					(config, value) -> config.hudShowFPS = value,
					config -> config.hudShowFPS)
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(id("hud_frame_time_buffer_size", int.class))
				.setControl(option ->
					new SliderControl(option, 0, 30, 5, 
						v -> Component.translatable("nolijium.seconds", Math.max(1, v))))
				.setBinding(
					(config, value) -> config.hudFrameTimeBufferSize = Math.max(1, value),
					config -> (int) config.hudFrameTimeBufferSize)
				.setEnabledPredicate(() -> hudEnabledOption.getValue() && hudShowFPSOption.getValue() == DetailLevel.EXTENDED)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("hud_show_cpu", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hudShowCPU = value,
					config -> config.hudShowCPU)
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("hud_show_memory", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hudShowMemory = value,
					config -> config.hudShowMemory)
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("hud_show_coordinates", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hudShowCoordinates = value,
					config -> config.hudShowCoordinates)
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.build());
		
		togglesPage.add(OptionGroup.createBuilder()
			.setId(id("toggles"))
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("revert_damage_camera_tilt", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.revertDamageCameraTilt = value,
					config -> config.revertDamageCameraTilt)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("revert_potions", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.revertPotions = value,
					config -> config.revertPotions)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("enable_opaque_block_outlines", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.enableOpaqueBlockOutlines = value,
					config -> config.enableOpaqueBlockOutlines)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("disable_block_light_flicker", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.disableBlockLightFlicker = value,
					config -> config.disableBlockLightFlicker)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("disable_block_animations", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.disableTextureAnimations = value,
					config -> config.disableTextureAnimations)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("disable_sky", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.disableSky = value,
					config -> config.disableSky)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("disable_weather_rendering", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.disableWeatherRendering = value,
					config -> config.disableWeatherRendering)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("disable_weather_ticking", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.disableWeatherTicking = value,
					config -> config.disableWeatherTicking)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("disable_font_shadows", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.disableFontShadows = value,
					config -> config.disableFontShadows)
				.build())
			.build());
		
		final Option<Boolean> disableAllToastsOption;
		togglesPage.add(OptionGroup.createBuilder()
			.setId(id("toasts"))
			.add(disableAllToastsOption = OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("disable_all_toasts", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hideAllToasts = value,
					config -> config.hideAllToasts)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("disable_advancement_toasts", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hideAdvancementToasts = value,
					config -> config.hideAdvancementToasts)
				.setEnabledPredicate(() -> !disableAllToastsOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("disable_recipe_toasts", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hideRecipeToasts = value,
					config -> config.hideRecipeToasts)
				.setEnabledPredicate(() -> !disableAllToastsOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("disable_system_toasts", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hideSystemToasts = value,
					config -> config.hideSystemToasts)
				.setEnabledPredicate(() -> !disableAllToastsOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("disable_tutorial_toasts", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hideTutorialToasts = value,
					config -> config.hideTutorialToasts)
				.setEnabledPredicate(() -> !disableAllToastsOption.getValue())
				.build())
			.build());
		
		final Option<Boolean> hideAllParticlesOption;
		
		particlesPage.add(OptionGroup.createBuilder()
			.setId(id("particles"))
			.add(hideAllParticlesOption = OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("disable_particles", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hideParticles = value,
					config -> config.hideParticles)
				.build())
			.build());
		
		final OptionGroup.Builder particlesByIdBuilder = OptionGroup.createBuilder().setId(id("particles_by_id"));
		
		for (final ResourceLocation particleTypeId :
			BuiltInRegistries.PARTICLE_TYPE.keySet()
				.stream()
				.sorted(ResourceLocationComparator.INSTANCE)
				.toArray(ResourceLocation[]::new)) {
			final String name = particleTypeId.toString();
			particlesByIdBuilder.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("enable_particle/" + name.replace(':', '/'), boolean.class))
				.setName(Component.literal(name))
				.setTooltip(Component.translatable("nolijium.particle_id", name))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> {
						final boolean present = config.hideParticlesByID.contains(name);
						if (!value && !present)
							config.hideParticlesByID.add(name);
						else if (value && present)
							config.hideParticlesByID.remove(name);
					},
					config -> !config.hideParticlesByID.contains(name))
				.setEnabledPredicate(() -> !hideAllParticlesOption.getValue())
				.build());
		}
		
		particlesPage.add(particlesByIdBuilder.build());
		
		chromaPage.add(OptionGroup.createBuilder()
			.setId(id("chroma"))
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(id("chroma_speed", int.class))
				.setControl(option -> 
					new SliderControl(option, 1, 30, 1, v -> Component.literal("%.1f".formatted(v * 0.1D))))
				.setBinding(
					(config, value) -> config.chromaSpeed = value * 0.1D,
					config -> (int) (config.chromaSpeed * 10))
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("enable_chroma_block_outlines", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.enableChromaBlockOutlines = value,
					config -> config.enableChromaBlockOutlines)
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(id("chroma_block_shape_overlay", int.class))
				.setControl(option -> new SliderControl(option, 0, 80, 10,
					v -> v == 0
					     ? Component.translatable("nolijium.disabled")
					     : Component.translatable("nolijium.percentage", v)))
				.setBinding(
					(config, value) -> config.chromaBlockShapeOverlay = value * 0.01F,
					config -> Math.round(config.chromaBlockShapeOverlay * 100F))
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("enable_chroma_tooltips", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.enableChromaToolTips = value,
					config -> config.enableChromaToolTips)
				.setEnabledPredicate(() -> !storage.getData().tooltipColourOverride)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("enable_chroma_hud", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.enableChromaHUD = value,
					config -> config.enableChromaHUD)
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.build());
		
		event.getPages().add(new OptionPage(
			id("utilities"),
			Component.translatable("nolijium.utilities"),
			ImmutableList.copyOf(utilitiesPage)
		));
		event.getPages().add(new OptionPage(
			id("toggles"),
			Component.translatable("nolijium.toggles"),
			ImmutableList.copyOf(togglesPage)
		));
		event.getPages().add(new OptionPage(
			id("particles"),
			Component.translatable("nolijium.particles"),
			ImmutableList.copyOf(particlesPage)
		));
		event.getPages().add(new OptionPage(
			id("chroma"),
			Component.translatable("nolijium.chroma"),
			ImmutableList.copyOf(chromaPage)
		));
	}
	
}
