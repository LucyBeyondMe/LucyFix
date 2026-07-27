package com.lucybeyondme.lucyfix.mixin;

import com.lucybeyondme.lucyfix.RemovedContent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {

    @Shadow
    @Final
    private int index;

    @Shadow
    @Final
    public Inventory inventory;

    @Inject(method = "getStack", at = @At("RETURN"), cancellable = true)
    private void lucyfix$removeLegacyContentFromContainers(
        CallbackInfoReturnable<ItemStack> cir
    ) {
        ItemStack original = cir.getReturnValue();
        // Lazy cleanup catches removed content in containers saved by older versions.
        ItemStack sanitized = RemovedContent.sanitize(original);
        if (sanitized != original) {
            this.inventory.setStack(this.index, sanitized);
            cir.setReturnValue(sanitized);
        }
    }
}
