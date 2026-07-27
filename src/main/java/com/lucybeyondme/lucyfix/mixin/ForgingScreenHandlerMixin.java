package com.lucybeyondme.lucyfix.mixin;

import com.lucybeyondme.lucyfix.screen.LapisAnvil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgingScreenHandler.class)
public abstract class ForgingScreenHandlerMixin extends ScreenHandler {

    protected ForgingScreenHandlerMixin() {
        super(null, 0);
    }

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void lucyfix$quickMoveAnvilLapis(
        PlayerEntity player,
        int slotIndex,
        CallbackInfoReturnable<ItemStack> cir
    ) {
        // ForgingScreenHandler also backs smithing tables, so limit this path to anvils.
        if (!((Object) this instanceof AnvilScreenHandler)
            || slotIndex < 0
            || slotIndex >= this.slots.size()) {
            return;
        }

        Slot sourceSlot = this.slots.get(slotIndex);
        if (!sourceSlot.hasStack()) {
            return;
        }

        ItemStack sourceStack = sourceSlot.getStack();
        // Slot 39 is the appended lapis slot; player inventory occupies slots 3 through 38.
        boolean fromLapisSlot = slotIndex == LapisAnvil.LAPIS_SLOT_ID;
        boolean lapisFromPlayerInventory =
            slotIndex >= LapisAnvil.PLAYER_INVENTORY_START
                && slotIndex < LapisAnvil.LAPIS_SLOT_ID
                && sourceStack.isOf(Items.LAPIS_LAZULI);

        if (!fromLapisSlot && !lapisFromPlayerInventory) {
            return;
        }

        ItemStack original = sourceStack.copy();
        boolean moved = fromLapisSlot
            ? this.insertItem(
                sourceStack,
                LapisAnvil.PLAYER_INVENTORY_START,
                LapisAnvil.LAPIS_SLOT_ID,
                true
            )
            : this.insertItem(
                sourceStack,
                LapisAnvil.LAPIS_SLOT_ID,
                LapisAnvil.LAPIS_SLOT_ID + 1,
                false
            );

        // Returning an empty stack tells vanilla that shift-clicking made no transfer.
        if (!moved) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.setStack(ItemStack.EMPTY);
        } else {
            sourceSlot.markDirty();
        }
        sourceSlot.onTakeItem(player, sourceStack);
        cir.setReturnValue(original);
    }

    @Inject(method = "onClosed", at = @At("RETURN"))
    private void lucyfix$returnUnusedAnvilLapis(PlayerEntity player, CallbackInfo ci) {
        // Return unused payment items when the menu closes, matching vanilla input handling.
        if ((Object) this instanceof AnvilScreenHandler
            && !player.getWorld().isClient
            && this.slots.size() > LapisAnvil.LAPIS_SLOT_ID) {
            ItemStack lapis = this.getSlot(LapisAnvil.LAPIS_SLOT_ID).takeStack(Integer.MAX_VALUE);
            if (!lapis.isEmpty()) {
                player.getInventory().offerOrDrop(lapis);
            }
        }
    }
}
