package com.lucybeyondme.lucyfix;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public final class RemovedContent {
    private static final String MENDING_ID = "minecraft:mending";

    private RemovedContent() {
    }

    public static boolean containsMending(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        // Enchanted books store entries under a different NBT key than equipment.
        String key = stack.isOf(Items.ENCHANTED_BOOK) ? "StoredEnchantments" : "Enchantments";
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            return false;
        }
        NbtList enchantments = nbt.getList(key, NbtCompound.COMPOUND_TYPE);
        for (int index = 0; index < enchantments.size(); index++) {
            if (MENDING_ID.equals(enchantments.getCompound(index).getString("id"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes legacy Mending data and the removed Netherite Upgrade template.
     * If an enchanted book only contained Mending, it becomes a normal book.
     */
    public static ItemStack sanitize(ItemStack stack) {
        if (stack.isEmpty()) {
            return stack;
        }
        if (stack.isOf(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)) {
            return ItemStack.EMPTY;
        }
        if (!containsMending(stack)) {
            return stack;
        }

        boolean enchantedBook = stack.isOf(Items.ENCHANTED_BOOK);
        String key = enchantedBook ? "StoredEnchantments" : "Enchantments";
        NbtCompound originalNbt = stack.getNbt();
        if (originalNbt == null) {
            return stack;
        }

        NbtCompound cleanedNbt = originalNbt.copy();
        NbtList enchantments = cleanedNbt.getList(key, NbtCompound.COMPOUND_TYPE);
        enchantments.removeIf(element ->
            element instanceof NbtCompound enchantment
                && MENDING_ID.equals(enchantment.getString("id"))
        );

        if (enchantedBook && enchantments.isEmpty()) {
            ItemStack book = new ItemStack(Items.BOOK, stack.getCount());
            cleanedNbt.remove(key);
            if (!cleanedNbt.isEmpty()) {
                book.setNbt(cleanedNbt);
            }
            return book;
        }

        cleanedNbt.put(key, enchantments);
        stack.setNbt(cleanedNbt);
        return stack;
    }
}
