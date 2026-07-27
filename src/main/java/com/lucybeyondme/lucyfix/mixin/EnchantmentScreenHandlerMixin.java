package com.lucybeyondme.lucyfix.mixin;

import java.util.List;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin {

    @Shadow
    @Final
    private Inventory inventory;

    @Shadow
    @Final
    private ScreenHandlerContext context;

    @Shadow
    @Final
    private Property seed;

    @Shadow
    private List<EnchantmentLevelEntry> generateEnchantments(ItemStack stack, int slot, int level) {
        throw new AssertionError();
    }

    @Shadow
    public abstract void onContentChanged(Inventory inventory);

    /**
     * Enchants using lapis alone. Calling applyEnchantmentCosts with zero keeps
     * vanilla's per-player enchantment seed progression without altering XP.
     *
     * @author LucyFix
     * @reason Replace XP validation/payment with lapis-only enchanting.
     */
    @Overwrite
    public boolean onButtonClick(PlayerEntity player, int id) {
        EnchantmentScreenHandler self = (EnchantmentScreenHandler) (Object) this;
        // Treat the client button ID as untrusted input on the logical server.
        if (id < 0 || id >= self.enchantmentPower.length) {
            Util.error(player.getName() + " pressed invalid button id: " + id);
            return false;
        }

        ItemStack target = this.inventory.getStack(0);
        ItemStack lapis = this.inventory.getStack(1);
        int lapisCost = id + 1;
        boolean creative = player.getAbilities().creativeMode;

        if (!creative && (lapis.isEmpty() || lapis.getCount() < lapisCost)) {
            return false;
        }
        if (self.enchantmentPower[id] <= 0 || target.isEmpty()) {
            return false;
        }

        // ScreenHandlerContext keeps world access safe for both client and server handlers.
        this.context.run((world, pos) -> {
            ItemStack enchantedStack = target;
            List<EnchantmentLevelEntry> enchantments =
                this.generateEnchantments(enchantedStack, id, self.enchantmentPower[id]);

            if (enchantments.isEmpty()) {
                return;
            }

            // Passing zero advances the vanilla enchantment seed without charging XP.
            player.applyEnchantmentCosts(enchantedStack, 0);
            boolean isBook = enchantedStack.isOf(Items.BOOK);
            if (isBook) {
                enchantedStack = new ItemStack(Items.ENCHANTED_BOOK);
                NbtCompound nbt = target.getNbt();
                if (nbt != null) {
                    enchantedStack.setNbt(nbt.copy());
                }
                this.inventory.setStack(0, enchantedStack);
            }

            for (EnchantmentLevelEntry enchantment : enchantments) {
                if (isBook) {
                    EnchantedBookItem.addEnchantment(enchantedStack, enchantment);
                } else {
                    enchantedStack.addEnchantment(enchantment.enchantment, enchantment.level);
                }
            }

            if (!creative) {
                lapis.decrement(lapisCost);
                if (lapis.isEmpty()) {
                    this.inventory.setStack(1, ItemStack.EMPTY);
                }
            }

            player.incrementStat(Stats.ENCHANT_ITEM);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                Criteria.ENCHANTED_ITEM.trigger(serverPlayer, enchantedStack, lapisCost);
            }

            this.inventory.markDirty();
            this.seed.set(player.getEnchantmentTableSeed());
            this.onContentChanged(this.inventory);
            world.playSound(
                null,
                pos,
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.BLOCKS,
                1.0F,
                world.random.nextFloat() * 0.1F + 0.9F
            );
        });
        return true;
    }
}
