package com.lucybeyondme.lucyfix.mixin;

import com.lucybeyondme.lucyfix.screen.LapisAnvil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends ForgingScreen<AnvilScreenHandler> {

    @Unique
    private static final Identifier LUCYFIX$ANVIL_TEXTURE =
        new Identifier("textures/gui/container/anvil.png");
    @Unique
    private static final int LUCYFIX$HAMMER_SOURCE_X = 17;
    @Unique
    private static final int LUCYFIX$HAMMER_SOURCE_Y = 5;
    @Unique
    private static final int LUCYFIX$HAMMER_WIDTH = 31;
    @Unique
    private static final int LUCYFIX$HAMMER_HEIGHT = 33;
    @Unique
    private static final int LUCYFIX$HAMMER_DESTINATION_X = 7;

    protected AnvilScreenMixin(
        AnvilScreenHandler handler,
        PlayerInventory inventory,
        Text title,
        Identifier texture
    ) {
        super(handler, inventory, title, texture);
    }

    @Inject(method = "drawBackground", at = @At("RETURN"))
    private void lucyfix$drawLapisSlot(
        DrawContext context,
        float delta,
        int mouseX,
        int mouseY,
        CallbackInfo ci
    ) {
        // Clear the vanilla hammer, then redraw it ten pixels farther left so
        // its right edge no longer occupies the dedicated lapis slot.
        context.fill(
            this.x + LUCYFIX$HAMMER_SOURCE_X,
            this.y + LUCYFIX$HAMMER_SOURCE_Y,
            this.x + LUCYFIX$HAMMER_SOURCE_X + LUCYFIX$HAMMER_WIDTH,
            this.y + LUCYFIX$HAMMER_SOURCE_Y + LUCYFIX$HAMMER_HEIGHT,
            0xFFC6C6C6
        );
        // Draw the hammer from its original atlas region at the shifted destination.
        context.drawTexture(
            LUCYFIX$ANVIL_TEXTURE,
            this.x + LUCYFIX$HAMMER_DESTINATION_X,
            this.y + LUCYFIX$HAMMER_SOURCE_Y,
            LUCYFIX$HAMMER_SOURCE_X,
            LUCYFIX$HAMMER_SOURCE_Y,
            LUCYFIX$HAMMER_WIDTH,
            LUCYFIX$HAMMER_HEIGHT
        );

        // Reuse the vanilla slot texture so the added inventory slot matches the screen.
        context.drawTexture(
            LUCYFIX$ANVIL_TEXTURE,
            this.x + LapisAnvil.LAPIS_SLOT_X - 1,
            this.y + LapisAnvil.LAPIS_SLOT_Y - 1,
            26,
            46,
            18,
            18
        );
    }

    @Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    private void lucyfix$drawLapisCost(
        DrawContext context,
        int mouseX,
        int mouseY,
        CallbackInfo ci
    ) {
        // Draw vanilla labels first, then replace the XP cost area with lapis status.
        super.drawForeground(context, mouseX, mouseY);

        LapisAnvil lapisAnvil = (LapisAnvil) this.handler;
        int cost = lapisAnvil.lucyfix$getLapisCost();
        if (cost > 0 && this.handler.getSlot(2).hasStack()) {
            Text text = Text.translatable("lucyfix.container.anvil.lapis_cost", cost);
            boolean affordable = this.client.player.getAbilities().creativeMode
                || lapisAnvil.lucyfix$getLapisCount() >= cost;
            int color = affordable ? 0x80FF20 : 0xFF6060;
            int textX = this.backgroundWidth - 10 - this.textRenderer.getWidth(text);
            context.fill(textX - 2, 67, this.backgroundWidth - 8, 79, 0x4F000000);
            context.drawTextWithShadow(this.textRenderer, text, textX, 69, color);
        }

        // Prevent the original foreground pass from drawing the vanilla XP cost afterward.
        ci.cancel();
    }
}
