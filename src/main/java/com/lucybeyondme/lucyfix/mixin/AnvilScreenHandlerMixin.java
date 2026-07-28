package com.lucybeyondme.lucyfix.mixin;

import com.lucybeyondme.lucyfix.screen.LapisAnvil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler implements LapisAnvil {

    @Shadow
    private Property levelCost;

    @Unique
    private final SimpleInventory lucyfix$lapisInventory = new SimpleInventory(1);

    @Unique
    private int lucyfix$lapisCost;

    protected AnvilScreenHandlerMixin() {
        super(null, 0, null, null);
    }

    @Inject(
        method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
        at = @At("TAIL")
    )
    private void lucyfix$addLapisSlot(CallbackInfo ci) {
        // Append the lapis slot after vanilla constructs its 39 standard slots.
        this.addSlot(new Slot(this.lucyfix$lapisInventory, 0, LAPIS_SLOT_X, LAPIS_SLOT_Y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.LAPIS_LAZULI);
            }
        });
    }

    /**
     * Vanilla removes anvil output at 40 levels. LucyFix uses lapis instead,
     * so the vanilla XP ceiling must not invalidate an otherwise valid result.
     */
    @Redirect(
        method = "updateResult",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/player/PlayerAbilities;creativeMode:Z"
        )
    )
    private boolean lucyfix$ignoreTooExpensiveLimit(net.minecraft.entity.player.PlayerAbilities abilities) {
        return true;
    }

    @Inject(method = "updateResult", at = @At("RETURN"))
    private void lucyfix$replaceExperienceCost(CallbackInfo ci) {
        ItemStack secondInput = this.getSlot(1).getStack();
        ItemStack output = this.getSlot(2).getStack();

        // Only enchanted-book combinations pay lapis; repairs and renames stay free.
        if (!output.isEmpty() && secondInput.isOf(Items.ENCHANTED_BOOK)) {
            this.lucyfix$lapisCost = Math.max(1, lucyfix$totalStoredLevels(secondInput) * 3);
            this.levelCost.set(this.lucyfix$lapisCost);
        } else {
            this.lucyfix$lapisCost = 0;
            this.levelCost.set(0);
        }
    }

    @Inject(
        method = "canTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Z)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void lucyfix$checkLapis(
        PlayerEntity player,
        boolean outputPresent,
        CallbackInfoReturnable<Boolean> cir
    ) {
        // Mirror vanilla output validation before applying the replacement currency check.
        if (!outputPresent || this.getSlot(2).getStack().isEmpty()) {
            cir.setReturnValue(false);
            return;
        }

        if (this.lucyfix$lapisCost == 0 || player.getAbilities().creativeMode) {
            cir.setReturnValue(true);
            return;
        }

        cir.setReturnValue(this.lucyfix$lapisInventory.getStack(0).getCount() >= this.lucyfix$lapisCost);
    }

    @Inject(method = "onTakeOutput", at = @At("HEAD"))
    private void lucyfix$consumeLapis(PlayerEntity player, ItemStack output, CallbackInfo ci) {
        // Payment happens when output is taken so previewing a result never consumes lapis.
        if (this.lucyfix$lapisCost > 0 && !player.getAbilities().creativeMode) {
            this.lucyfix$lapisInventory.removeStack(0, this.lucyfix$lapisCost);
            this.lucyfix$lapisInventory.markDirty();
        }
    }

    @Override
    public int lucyfix$getLapisCost() {
        return this.lucyfix$lapisCost;
    }

    @Override
    public int lucyfix$getLapisCount() {
        return this.lucyfix$lapisInventory.getStack(0).getCount();
    }

    @Unique
    private static int lucyfix$totalStoredLevels(ItemStack stack) {
        // Sum stored levels before multiplying by the configured three-lapis rate.
        NbtList enchantments = EnchantedBookItem.getEnchantmentNbt(stack);
        int total = 0;
        for (int index = 0; index < enchantments.size(); index++) {
            total += Math.max(0, enchantments.getCompound(index).getShort("lvl"));
        }
        return total;
    }
}
