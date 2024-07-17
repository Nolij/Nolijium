package dev.nolij.nolijium.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemMixin {
	
	@ModifyReturnValue(method = "isFoil", at = @At("RETURN"))
	private boolean makePotionsEnchantable(boolean original) {
		//noinspection ConstantValue
		return original || (Nolijium.config.revertPotions && (Object) this instanceof PotionItem);
	}
	
}
