package dev.nolij.nolijium.integration.embeddium;

import com.google.common.collect.ImmutableList;
import dev.nolij.nolijium.impl.util.Alignment;
import dev.nolij.nolijium.impl.util.DetailLevel;
import me.jellysquid.mods.sodium.client.gui.options.*;
import me.jellysquid.mods.sodium.client.gui.options.control.*;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class NolijiumEmbeddiumConfigScreen {
	
	
	public static OptionPage[] getPages() {
		final NolijiumOptionsStorage storage = new NolijiumOptionsStorage();
		
		final List<OptionGroup> utilitiesPage = new ArrayList<>();
		final List<OptionGroup> togglesPage = new ArrayList<>();
		final List<OptionGroup> particlesPage = new ArrayList<>();
		
		utilitiesPage.add(OptionGroup.createBuilder()
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.enableGamma = value,
					config -> config.enableGamma
				)
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setControl(option -> new SliderControl(option, 0, 2000, 100,
					v -> v > 0 
					     ? new TranslatableComponent("nolijium.messages", v).getString()
					     : new TranslatableComponent("nolijium.unlimited").getString()))
				.setBinding(
					(config, value) -> config.maxChatHistory = value,
					config -> config.maxChatHistory)
				.build())
			.build());
		
		final Option<Boolean> hudEnabledOption;
		final Option<DetailLevel> hudShowFPSOption;
		utilitiesPage.add(OptionGroup.createBuilder()
			.add(hudEnabledOption = OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hudEnabled = value,
					config -> config.hudEnabled)
				.build())
			.add(OptionImpl.createBuilder(Alignment.X.class, storage)
				.setControl(option -> new CyclingControl<>(option, Alignment.X.class))
				.setBinding(
					(config, value) -> config.hudAlignmentX = value,
					config -> config.hudAlignmentX)
				.setEnabled(hudEnabledOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(Alignment.Y.class, storage)
				.setControl(option -> new CyclingControl<>(option, Alignment.Y.class))
				.setBinding(
					(config, value) -> config.hudAlignmentY = value,
					config -> config.hudAlignmentY)
				.setEnabled(hudEnabledOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setControl(option -> 
					new SliderControl(option, 0, 20, 5, 
						v -> new TranslatableComponent("nolijium.pixels", v).getString()))
				.setBinding(
					(config, value) -> config.hudMarginX = value,
					config -> config.hudMarginX)
				.setEnabled(hudEnabledOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setControl(option -> 
					new SliderControl(option, 0, 20, 5, 
						v -> new TranslatableComponent("nolijium.pixels", v).getString()))
				.setBinding(
					(config, value) -> config.hudMarginY = value,
					config -> config.hudMarginY)
				.setEnabled(hudEnabledOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hudBackground = value,
					config -> config.hudBackground)
				.setEnabled(hudEnabledOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hudShadow = value,
					config -> config.hudShadow)
				.setEnabled(hudEnabledOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setControl(option ->
					new SliderControl(option, 0, 20, 1, 
						v -> v != 0 
						     ? new TranslatableComponent("nolijium.ticks", v).getString()
						     : new TranslatableComponent("nolijium.every_frame").getString()))
				.setBinding(
					(config, value) -> config.hudRefreshRateTicks = value,
					config -> config.hudRefreshRateTicks)
				.setEnabled(hudEnabledOption.getValue())
				.build())
			.add(hudShowFPSOption = OptionImpl.createBuilder(DetailLevel.class, storage)
				.setControl(option -> new CyclingControl<>(option, DetailLevel.class))
				.setBinding(
					(config, value) -> config.hudShowFPS = value,
					config -> config.hudShowFPS)
				.setEnabled(hudEnabledOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setControl(option ->
					new SliderControl(option, 0, 30, 5, 
						v -> new TranslatableComponent("nolijium.seconds", Math.max(1, v)).getString()))
				.setBinding(
					(config, value) -> config.hudFrameTimeBufferSize = Math.max(1, value),
					config -> (int) config.hudFrameTimeBufferSize)
				.setEnabled(hudEnabledOption.getValue() && hudShowFPSOption.getValue() == DetailLevel.EXTENDED)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hudShowCPU = value,
					config -> config.hudShowCPU)
				.setEnabled(hudEnabledOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hudShowMemory = value,
					config -> config.hudShowMemory)
				.setEnabled(hudEnabledOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hudShowCoordinates = value,
					config -> config.hudShowCoordinates)
				.setEnabled(hudEnabledOption.getValue())
				.build())
			.build());
		
		togglesPage.add(OptionGroup.createBuilder()
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.revertDamageCameraTilt = value,
					config -> config.revertDamageCameraTilt)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.disableTextureAnimations = value,
					config -> config.disableTextureAnimations)
				.build())
			.build());
		
		final Option<Boolean> disableAllToastsOption;
		togglesPage.add(OptionGroup.createBuilder()
			.add(disableAllToastsOption = OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hideAllToasts = value,
					config -> config.hideAllToasts)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hideAdvancementToasts = value,
					config -> config.hideAdvancementToasts)
				.setEnabled(!disableAllToastsOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hideRecipeToasts = value,
					config -> config.hideRecipeToasts)
				.setEnabled(!disableAllToastsOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hideSystemToasts = value,
					config -> config.hideSystemToasts)
				.setEnabled(!disableAllToastsOption.getValue())
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hideTutorialToasts = value,
					config -> config.hideTutorialToasts)
				.setEnabled(!disableAllToastsOption.getValue())
				.build())
			.build());
		
		final Option<Boolean> hideAllParticlesOption;
		
		particlesPage.add(OptionGroup.createBuilder()
			.add(hideAllParticlesOption = OptionImpl.createBuilder(boolean.class, storage)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.hideParticles = value,
					config -> config.hideParticles)
				.build())
			.build());
		
		final OptionGroup.Builder particlesByIdBuilder = OptionGroup.createBuilder();
		
		for (final ResourceLocation particleTypeId :
			Registry.PARTICLE_TYPE.keySet().stream().sorted().toArray(ResourceLocation[]::new)) {
			final String name = particleTypeId.toString();
			particlesByIdBuilder.add(OptionImpl.createBuilder(boolean.class, storage)
				.setName(new TextComponent(name))
				.setTooltip(new TranslatableComponent("nolijium.particle_id", name))
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
				.setEnabled(!hideAllParticlesOption.getValue())
				.build());
		}
		
		particlesPage.add(particlesByIdBuilder.build());
		
		return new OptionPage[]{
			new OptionPage(
				new TranslatableComponent("nolijium.utilities"),
				ImmutableList.copyOf(utilitiesPage)
			),
			new OptionPage(
				new TranslatableComponent("nolijium.toggles"),
				ImmutableList.copyOf(togglesPage)
			),
			new OptionPage(
				new TranslatableComponent("nolijium.particles"),
				ImmutableList.copyOf(particlesPage)
			)
		};
	}
}
