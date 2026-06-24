package com.lucybeyondme.lucyfix.mixin;

import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.inventory.Inventory;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Removes the netherite upgrade template requirement.
 * 
 * Instead of overriding matches(), we inject into the template ingredient
 * check specifically - if the addition is a netherite ingot, we skip
 * the template slot check entirely.
 */
@Mixin(SmithingTransformRecipe.class)
public class SmithingTransformRecipeMixin {

    @Shadow private net.minecraft.recipe.Ingredient template;
    @Shadow private net.minecraft.recipe.Ingredient base;
    @Shadow private net.minecraft.recipe.Ingredient addition;

    @Inject(
        method = "matches(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void ignoreTemplateForNetherite(Inventory inventory, World world,
                                             CallbackInfoReturnable<Boolean> cir) {
        ItemStack additionStack = inventory.getStack(2);

        // Only intercept netherite upgrade recipes
        if (!additionStack.isOf(Items.NETHERITE_INGOT)) return;

        ItemStack baseStack = inventory.getStack(1);

        // Check base item matches this recipe's base ingredient
        // but ignore the template slot entirely
        if (this.base.test(baseStack) && this.addition.test(additionStack)) {
            cir.setReturnValue(true);
        }
        // If base doesn't match, don't return anything - let vanilla handle it
        // This prevents ALL smithing recipes from matching
    }
}
