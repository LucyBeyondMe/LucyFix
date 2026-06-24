package com.lucybeyondme.lucyfix.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the XP bar from the HUD entirely.
 * Confirmed method signature for 1.20.1: renderExperienceBar(DrawContext, int)
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(
        method = "renderExperienceBar(Lnet/minecraft/client/gui/DrawContext;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hideXpBar(DrawContext context, int x, CallbackInfo ci) {
        ci.cancel();
    }
}
