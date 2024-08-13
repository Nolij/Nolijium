package dev.nolij.nolijium.common;


import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;

public final class ResourceLocationComparator implements Comparator<ResourceLocation> {
	
	public static final ResourceLocationComparator INSTANCE = new ResourceLocationComparator();
	
	private static boolean isVanillaNamespace(ResourceLocation location) {
		return location.getNamespace().equals("minecraft");
	}
	
	@Override
	public int compare(ResourceLocation left, ResourceLocation right) {
		final boolean leftIsVanilla = isVanillaNamespace(left);
		final boolean rightIsVanilla = isVanillaNamespace(right);
		if (leftIsVanilla ^ rightIsVanilla) {
			if (leftIsVanilla)
				return -1;
			
			return 1;
		}
		
		return left.compareNamespaced(right);
	}
	
}
