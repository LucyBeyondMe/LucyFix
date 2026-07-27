package com.lucybeyondme.lucyfix.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @Inject(method = "isAvailableForEnchantedBookOffer", at = @At("HEAD"), cancellable = true)
    private void lucyfix$removeMendingTrades(CallbackInfoReturnable<Boolean> cir) {
        // Block Mending at both vanilla selection entry points.
        if ((Object) this == Enchantments.MENDING) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isAvailableForRandomSelection", at = @At("HEAD"), cancellable = true)
    private void lucyfix$removeMendingFromRandomLoot(CallbackInfoReturnable<Boolean> cir) {
        // Block Mending at both vanilla selection entry points.
        if ((Object) this == Enchantments.MENDING) {
            cir.setReturnValue(false);
        }
    }
}
