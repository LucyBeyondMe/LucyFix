package com.lucybeyondme.lucyfix.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    protected AnvilScreenHandlerMixin() {
        super(null, 0, null, null);
    }

    @Shadow private Property levelCost;

    private boolean minecraftfix_needsLapis = false;
    private int minecraftfix_lapisCost = 0;
    // Track last time canTakeOutput returned true - if updateResult fires
    // right after with an empty output slot, item was taken
    private long minecraftfix_lastApproveTime = 0;
    private PlayerEntity minecraftfix_lastApprovePlayer = null;

    private static int getTotalEnchantLevels(ItemStack stack) {
        NbtList enchants = null;
        if (stack.isOf(Items.ENCHANTED_BOOK)) {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null) enchants = nbt.getList("StoredEnchantments", 10);
        } else {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null) enchants = nbt.getList("Enchantments", 10);
        }
        if (enchants == null || enchants.isEmpty()) return 0;
        int total = 0;
        for (int i = 0; i < enchants.size(); i++) {
            total += enchants.getCompound(i).getShort("lvl");
        }
        return total;
    }

    @Inject(method = "updateResult()V", at = @At("HEAD"))
    private void checkLapisTrigger(CallbackInfo ci) {
        // If a lapis payment was recently approved and output is now empty, consume lapis
        if (!minecraftfix_needsLapis) return;
        if (minecraftfix_lastApprovePlayer == null) return;

        long now = System.currentTimeMillis();
        // Only fire within 500ms of the approval (same tick as take)
        if (now - minecraftfix_lastApproveTime > 500) {
            minecraftfix_lastApprovePlayer = null;
            return;
        }

        AnvilScreenHandler self = (AnvilScreenHandler)(Object)this;
        if (self.getSlot(2).getStack().isEmpty()) {
            // Output just disappeared — item was taken, consume lapis now
            PlayerEntity player = minecraftfix_lastApprovePlayer;
            player.getInventory().remove(
                s -> s.isOf(Items.LAPIS_LAZULI),
                minecraftfix_lapisCost,
                player.getInventory()
            );
            player.experienceLevel = 0;
            player.totalExperience = 0;
            player.experienceProgress = 0;
            minecraftfix_lastApprovePlayer = null;
        }
    }

    @Inject(method = "updateResult()V", at = @At("RETURN"))
    private void reworkAnvilCost(CallbackInfo ci) {
        AnvilScreenHandler self = (AnvilScreenHandler)(Object)this;
        ItemStack input1 = self.getSlot(0).getStack();
        ItemStack input2 = self.getSlot(1).getStack();
        ItemStack output = self.getSlot(2).getStack();

        if (output.isEmpty()) {
            minecraftfix_needsLapis = false;
            minecraftfix_lapisCost = 0;
            return;
        }

        boolean isEnchantBook = !input2.isEmpty() && input2.isOf(Items.ENCHANTED_BOOK);
        boolean isSameItemWithEnchants = !input1.isEmpty() && !input2.isEmpty()
            && input1.getItem() == input2.getItem()
            && getTotalEnchantLevels(input2) > 0;

        if (isEnchantBook || isSameItemWithEnchants) {
            int totalLevels = getTotalEnchantLevels(input2);
            minecraftfix_needsLapis = true;
            minecraftfix_lapisCost = Math.max(1, totalLevels * 3);
            this.levelCost.set(minecraftfix_lapisCost);
        } else {
            minecraftfix_needsLapis = false;
            minecraftfix_lapisCost = 0;
            this.levelCost.set(0);
        }
    }

    @Inject(
        method = "canTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Z)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void lapisAnvilCheck(PlayerEntity player, boolean present,
                                  CallbackInfoReturnable<Boolean> cir) {
        if (!present) return;
        AnvilScreenHandler self = (AnvilScreenHandler)(Object)this;
        if (self.getSlot(2).getStack().isEmpty()) return;

        if (minecraftfix_needsLapis) {
            int lapisCount = player.getInventory().count(Items.LAPIS_LAZULI);
            if (lapisCount >= minecraftfix_lapisCost) {
                // Approve and record time + player for consumption in updateResult
                player.experienceLevel = 100;
                minecraftfix_lastApproveTime = System.currentTimeMillis();
                minecraftfix_lastApprovePlayer = player;
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        } else {
            cir.setReturnValue(true);
        }
    }
}
