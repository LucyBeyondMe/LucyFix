package com.lucybeyondme.lucyfix.mixin;

import com.lucybeyondme.lucyfix.RemovedContent;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantEntity.class)
public class MerchantEntityMixin {

    @Inject(method = "getOffers", at = @At("RETURN"))
    private void lucyfix$removeLegacyMendingTrades(
        CallbackInfoReturnable<TradeOfferList> cir
    ) {
        // Existing villagers cache offers, so sanitize the returned list as well.
        cir.getReturnValue().removeIf(offer ->
            RemovedContent.containsMending(offer.getSellItem())
        );
    }
}
