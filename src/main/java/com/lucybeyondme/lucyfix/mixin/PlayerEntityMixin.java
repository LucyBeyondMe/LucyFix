package com.lucybeyondme.lucyfix.mixin;

import com.lucybeyondme.lucyfix.RemovedContent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * - Prevents players from gaining experience levels.
 * - Cleans removed Mending enchantments and Netherite Upgrade templates from
 *   legacy player inventories.
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

    @Inject(method = "tick", at = @At("TAIL"))
    private void lucyfix$cleanRemovedContent(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        // Run once per second on the server to avoid scanning inventories every tick.
        if (player.getWorld().isClient || player.age % 20 != 0) {
            return;
        }

        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack original = player.getInventory().getStack(slot);
            ItemStack sanitized = RemovedContent.sanitize(original);
            if (sanitized != original) {
                player.getInventory().setStack(slot, sanitized);
            }
        }
    }
}
