package dev.nolij.nolijium.mixin.common;

import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PotionItem.class)
public class PotionItemMixin extends Item {
	
	public PotionItemMixin(Properties p_41383_) {
		super(p_41383_);
	}
	
	@Override
	public boolean isFoil(@NotNull ItemStack p_41453_) {
		if (Nolijium.config.revertPotions)
			return true;
		
		return super.isFoil(p_41453_);
	}
	
}
