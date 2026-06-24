package com.lucybeyondme.lucyfix.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * - Prevents players from gaining experience levels.
 * - Disables Mending repair (Mending enchant can still exist on items
 *   but will do nothing, since XP orbs never spawn anyway).
 *
 * The XP bar is hidden client-side via the InGameHudMixin (client mixin).
 * On the server side we simply zero out any incoming experience.
 */
@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    /**
     * Cancel addExperience so no experience can accumulate.
     */
    @Inject(
        method = "addExperience(I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void cancelAddExperience(int experience, CallbackInfo ci) {
        ci.cancel();
    }

    /**
     * Cancel addExperienceLevels so level-granting calls are also suppressed.
     */
    @Inject(
        method = "addExperienceLevels(I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void cancelAddExperienceLevels(int levels, CallbackInfo ci) {
        ci.cancel();
    }
}
