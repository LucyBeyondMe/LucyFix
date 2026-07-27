package com.lucybeyondme.lucyfix.mixin;

import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LootTable.class)
public class LootTableMixin {

    @ModifyArg(
        method = "generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/loot/LootTable;processStacks(Lnet/minecraft/server/world/ServerWorld;Ljava/util/function/Consumer;)Ljava/util/function/Consumer;"
        ),
        index = 1
    )
    private Consumer<ItemStack> lucyfix$removeNetheriteUpgradeLoot(
        Consumer<ItemStack> originalConsumer
    ) {
        // Wrap the output consumer so existing loot tables need no per-table edits.
        return stack -> {
            if (!stack.isOf(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)) {
                originalConsumer.accept(stack);
            }
        };
    }
}
