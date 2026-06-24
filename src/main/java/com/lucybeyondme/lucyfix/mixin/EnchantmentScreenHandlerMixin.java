package com.lucybeyondme.lucyfix.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes enchanting cost only lapis — no XP.
 * Vanilla already handles lapis consumption from the UI slot.
 * We just boost levels so vanilla's level check passes, then wipe them after.
 */
@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {

    @Inject(
        method = "onButtonClick(Lnet/minecraft/entity/player/PlayerEntity;I)Z",
        at = @At("HEAD")
    )
    private void boostLevelBefore(PlayerEntity player, int id,
                                   CallbackInfoReturnable<Boolean> cir) {
        player.experienceLevel = 100;
        player.totalExperience = 1000;
    }

    @Inject(
        method = "onButtonClick(Lnet/minecraft/entity/player/PlayerEntity;I)Z",
        at = @At("RETURN")
    )
    private void resetLevelAfter(PlayerEntity player, int id,
                                  CallbackInfoReturnable<Boolean> cir) {
        player.experienceLevel = 0;
        player.totalExperience = 0;
        player.experienceProgress = 0;
    }
}
