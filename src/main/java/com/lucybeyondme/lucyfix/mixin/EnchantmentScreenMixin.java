package com.lucybeyondme.lucyfix.mixin;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantingPhrases;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends HandledScreen<EnchantmentScreenHandler> {

    protected EnchantmentScreenMixin(
        EnchantmentScreenHandler handler,
        PlayerInventory inventory,
        Text title
    ) {
        super(handler, inventory, title);
    }

    /**
     * The enchanting table is lapis-only, so XP must not disable valid offers.
     */
    @Redirect(
        method = "drawBackground",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;experienceLevel:I"
        )
    )
    private int lucyfix$treatOffersAsAffordable(ClientPlayerEntity player) {
        return Integer.MAX_VALUE;
    }

    /**
     * Replaces the numeric XP requirement with that offer's lapis payment,
     * aligned to the left edge of the native offer row.
     */
    @Redirect(
        method = "drawBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"
        )
    )
    private int lucyfix$drawLapisCostInNativePosition(
        DrawContext context,
        TextRenderer textRenderer,
        String text,
        int x,
        int y,
        int color
    ) {
        // Vanilla draws the three cost labels 19 pixels apart starting at y + 23.
        int slot = (y - this.y - 23) / 19;
        if (slot < 0 || slot >= 3) {
            return 0;
        }

        int lapisCost = slot + 1;
        String lapisText = lapisCost + " Lapis";
        int lapisX = this.x + 64;
        boolean affordable = this.client.player.getAbilities().creativeMode
            || this.handler.getLapisCount() >= lapisCost;
        int lapisColor = affordable ? 0xAA44FF : 0xFF6060;
        return context.drawTextWithShadow(
            textRenderer,
            lapisText,
            lapisX,
            y,
            lapisColor
        );
    }

    /**
     * Keeps the native row backgrounds but removes the vanilla tier glyphs.
     */
    @Redirect(
        method = "drawBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
        )
    )
    private void lucyfix$hideVanillaTierGlyphs(
        DrawContext context,
        Identifier texture,
        int x,
        int y,
        int textureX,
        int textureY,
        int width,
        int height
    ) {
        // Match only the two 16x16 tier-glyph atlas rows; keep all other textures.
        boolean tierGlyph = width == 16
            && height == 16
            && (textureY == 223 || textureY == 239);
        if (!tierGlyph) {
            context.drawTexture(
                texture,
                x,
                y,
                textureX,
                textureY,
                width,
                height
            );
        }
    }

    /**
     * The hover tooltip still exposes the enchantment clue, so replace the
     * generated Standard Galactic Alphabet phrase with empty text. Redirecting
     * phrase generation is stable in production mappings; redirecting
     * DrawContext.drawTextWrapped is not mapped consistently in 1.20.1.
     */
    @Redirect(
        method = "drawBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/EnchantingPhrases;generatePhrase(Lnet/minecraft/client/font/TextRenderer;I)Lnet/minecraft/text/StringVisitable;"
        )
    )
    private StringVisitable lucyfix$hideVanillaRunicText(
        EnchantingPhrases phrases,
        TextRenderer textRenderer,
        int width
    ) {
        // Empty text preserves layout code while suppressing the obsolete phrase.
        return Text.empty();
    }

    /**
     * Replaces vanilla's tooltip, which includes both a level requirement and
     * a level payment line, with a lapis-only tooltip.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void lucyfix$renderLapisOnlyTooltip(
        DrawContext context,
        int mouseX,
        int mouseY,
        float delta,
        CallbackInfo ci
    ) {
        delta = this.client.getTickDelta();
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        boolean creative = this.client.player.getAbilities().creativeMode;
        int lapisCount = this.handler.getLapisCount();
        // Rebuild only the hovered offer tooltip and omit every XP-related line.
        for (int slot = 0; slot < 3; slot++) {
            int power = this.handler.enchantmentPower[slot];
            Enchantment enchantment = Enchantment.byRawId(this.handler.enchantmentId[slot]);
            int enchantmentLevel = this.handler.enchantmentLevel[slot];
            int lapisCost = slot + 1;

            if (!this.isPointWithinBounds(60, 14 + 19 * slot, 108, 17, mouseX, mouseY)
                || power <= 0
                || enchantmentLevel < 0
                || enchantment == null) {
                continue;
            }

            ArrayList<Text> tooltip = Lists.newArrayList();
            tooltip.add(
                Text.translatable("container.enchant.clue", enchantment.getName(enchantmentLevel))
                    .formatted(Formatting.WHITE)
            );
            if (!creative) {
                tooltip.add(Text.empty());
                MutableText lapisText = lapisCost == 1
                    ? Text.translatable("container.enchant.lapis.one")
                    : Text.translatable("container.enchant.lapis.many", lapisCost);
                tooltip.add(lapisText.formatted(
                    lapisCount >= lapisCost ? Formatting.GRAY : Formatting.RED
                ));
            }
            context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
            break;
        }

        ci.cancel();
    }
}
