package dev.nolij.nolijium.integration.embeddium;

import com.google.common.collect.ImmutableList;
import dev.nolij.nolijium.impl.util.Alignment;
import dev.nolij.nolijium.impl.util.DetailLevel;
import me.jellysquid.mods.sodium.client.gui.options.Option;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.embeddedt.embeddium.api.OptionGUIConstructionEvent;
import org.embeddedt.embeddium.api.eventbus.EventHandlerRegistrar;
import org.embeddedt.embeddium.client.gui.options.OptionIdentifier;

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
		
		utilitiesPage.add(OptionGroup.createBuilder()
			.setId(id("utilities"))
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(id("enable_gamma", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.enableGamma = value,
					config -> config.enableGamma
				)
				.build())
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
					new SliderControl(option, 0, 20, 5, 
						v -> Component.translatable("nolijium.pixels", v)))
				.setBinding(
					(config, value) -> config.hudMarginX = value,
					config -> config.hudMarginX)
				.setEnabledPredicate(hudEnabledOption::getValue)
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(id("hud_margin_y", int.class))
				.setControl(option -> 
					new SliderControl(option, 0, 20, 5, 
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
				.setId(id("disable_block_animations", boolean.class))
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.disableTextureAnimations = value,
					config -> config.disableTextureAnimations)
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
			BuiltInRegistries.PARTICLE_TYPE.keySet().stream().sorted().toArray(ResourceLocation[]::new)) {
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
	}
	
}
