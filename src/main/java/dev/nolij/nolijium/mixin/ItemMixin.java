package dev.nolij.nolijium.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemMixin {
	
	// blame ProGuard
	@Unique
	private static boolean nolijium$isPotionItem(Object object) {
		return object instanceof PotionItem;
	}
	
	@ModifyReturnValue(method = "isFoil", at = @At("RETURN"))
	private boolean nolijium$isFoil$RETURN(boolean original) {
		if (Nolijium.config.revertPotions && nolijium$isPotionItem(this))
			return true;
		
		return original;
	}
	
}
