package com.lucybeyondme.lucyfix.mixin;

import net.minecraft.client.gui.screen.ingame.CyclingSlotIcon;
import net.minecraft.client.gui.screen.ingame.SmithingScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SmithingScreen.class)
public abstract class SmithingScreenMixin {

    @Shadow
    @Final
    private static Identifier EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM_TEXTURE;

    @Shadow
    @Final
    private CyclingSlotIcon templateSlotIcon;

    @Inject(method = "handledScreenTick", at = @At("TAIL"))
    private void lucyfix$showArmorTrimTemplateOutline(CallbackInfo ci) {
        this.templateSlotIcon.updateTexture(List.of(EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM_TEXTURE));
    }
}
