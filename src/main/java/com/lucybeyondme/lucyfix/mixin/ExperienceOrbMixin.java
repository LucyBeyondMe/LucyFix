package com.lucybeyondme.lucyfix.mixin;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents XP orbs from being spawned into the world.
 * Since XP orbs are the sole source of player experience,
 * this effectively removes the XP system entirely.
 */
@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbMixin {

    /**
     * Cancel the spawn method that places XP orbs in the world.
     * This covers all sources: mob kills, smelting, mining, fishing, etc.
     */
    @Inject(
        method = "spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void cancelXpSpawn(CallbackInfo ci) {
        // Cancel at the shared static spawn helper to cover every normal XP source.
        ci.cancel();
    }
}
