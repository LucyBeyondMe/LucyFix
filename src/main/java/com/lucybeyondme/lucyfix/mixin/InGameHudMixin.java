package com.lucybeyondme.lucyfix.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow
    private int scaledHeight;

    @Inject(
        method = "renderExperienceBar(Lnet/minecraft/client/gui/DrawContext;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void lucyfix$hideExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        // The level number is rendered with this method, so cancelling removes both pieces.
        ci.cancel();
    }

    /**
     * Places armor on the upper row and health plus hunger on the former XP row.
     * In this texture overload, V=9 identifies armor and V=27 identifies food.
     */
    @ModifyArgs(
        method = "renderStatusBars",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
        )
    )
    private void lucyfix$positionArmorAndHungerRows(Args args) {
        // Texture V distinguishes armor and hunger calls without touching other HUD icons.
        int textureV = args.get(4);
        if (textureV == 9) {
            args.set(2, this.scaledHeight - 43);
        } else if (textureV == 27) {
            int originalY = args.get(2);
            args.set(2, originalY + 6);
        }
    }

    @ModifyArgs(
        method = "renderStatusBars",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V"
        )
    )
    private void lucyfix$moveHealthToFormerArmorRow(Args args) {
        int originalY = args.get(3);
        args.set(3, originalY + 6);
    }
}
