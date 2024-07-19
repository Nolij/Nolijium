package dev.nolij.nolijium.common;

import dev.nolij.nolijium.impl.INolijiumImplementation;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Mod("nolijium_common")
public class NolijiumCommon implements INolijiumImplementation {
	
	private final INolijiumImplementation platformImplementation;
	
	public NolijiumCommon() {
		this.platformImplementation = null;
	}
	
	public NolijiumCommon(INolijiumImplementation platformImplementation, Path configPath) {
		Nolijium.LOGGER.info("Loading Nolijium...");
		
		this.platformImplementation = platformImplementation;
		
		Nolijium.registerImplementation(this, configPath);
	}
	
	public static Set<ResourceLocation> blockedParticleTypeIDs = Set.of();
	
	@Override
	public void onConfigReload(NolijiumConfigImpl config) {
		//noinspection DataFlowIssue
		blockedParticleTypeIDs = config.hideParticlesByID
			.stream()
			.map(ResourceLocation::tryParse)
			.collect(Collectors.toUnmodifiableSet());
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
	}
	
	public static class ResourceLocationComparator implements Comparator<ResourceLocation> {
		
		public static final ResourceLocationComparator INSTANCE = new ResourceLocationComparator();
		
		private static boolean isVanillaLocation(ResourceLocation location) {
			return location.getNamespace().equals("minecraft");
		}
		
		@Override
		public int compare(ResourceLocation left, ResourceLocation right) {
			final boolean leftIsVanilla = isVanillaLocation(left);
			final boolean rightIsVanilla = isVanillaLocation(right);
			if (leftIsVanilla ^ rightIsVanilla) {
				if (leftIsVanilla)
					return -1;
				
				return 1;
			}
			
			return left.compareNamespaced(right);
		}
		
	}
	
}
