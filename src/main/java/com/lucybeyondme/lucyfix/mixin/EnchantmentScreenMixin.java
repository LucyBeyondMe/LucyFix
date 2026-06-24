package com.lucybeyondme.lucyfix.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Overdraws lapis cost text on the enchanting screen.
 * Vanilla charges slot_index + 1 lapis (1, 2, 3).
 */
@Mixin(EnchantmentScreen.class)
public class EnchantmentScreenMixin {

    @Inject(
        method = "drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V",
        at = @At("RETURN")
    )
    private void overdrawLapisCost(DrawContext context, float delta, int mouseX, int mouseY,
                                    CallbackInfo ci) {
        EnchantmentScreen self = (EnchantmentScreen)(Object)this;
        EnchantmentScreenHandler handler = self.getScreenHandler();

        if (handler.getSlot(0).getStack().isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int guiWidth = 176;
        int guiHeight = 166;
        int screenX = (client.getWindow().getScaledWidth() - guiWidth) / 2;
        int screenY = (client.getWindow().getScaledHeight() - guiHeight) / 2;

        for (int i = 0; i < 3; i++) {
            if (handler.enchantmentPower[i] > 0) {
                // Vanilla charges (i + 1) lapis per slot
                int lapisCost = i + 1;
                String lapisText = lapisCost + " Lapis";
                context.drawText(
                    client.textRenderer,
                    lapisText,
                    screenX + 60,
                    screenY + 16 + (i * 19),
                    0xAA44FF,
                    true
                );
            }
        }
    }
}
