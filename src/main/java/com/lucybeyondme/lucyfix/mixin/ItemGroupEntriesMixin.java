package com.lucybeyondme.lucyfix.mixin;

import com.lucybeyondme.lucyfix.RemovedContent;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.item.ItemGroup$EntriesImpl")
public class ItemGroupEntriesMixin {

    @Inject(
        method = "add(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemGroup$StackVisibility;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void lucyfix$hideRemovedContent(
        ItemStack stack,
        ItemGroup.StackVisibility visibility,
        CallbackInfo ci
    ) {
        // Filtering during tab population prevents removed items from being obtainable here.
        if (stack.isOf(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
            || RemovedContent.containsMending(stack)) {
            ci.cancel();
        }
    }
}
