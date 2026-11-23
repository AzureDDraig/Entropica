package ddraig.net.entropica.mixin;

import ddraig.net.entropica.registry.ModPotions;
import ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public class PotionBrewingMixin {

    @Inject(method = "addVanillaMixes", at = @At("TAIL"))
    private static void entropica$registerCustomBrewingMixes(PotionBrewing.Builder builder, CallbackInfo ci) {
        ModPotions.registerBrewingMixes(builder);
    }

    @Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
    private void entropica$allowOmegaHorizonIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ApexEschatonSingularityHelper.isOmegaHorizonBrewingIngredient(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private void entropica$hasOmegaHorizonStasisMix(ItemStack container, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (ApexEschatonSingularityHelper.isOmegaHorizonBrewingIngredient(ingredient)
                && ApexEschatonSingularityHelper.canBrewGravitationalStasisPotion(container)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private void entropica$brewOmegaHorizonStasisElixir(ItemStack ingredient, ItemStack container, CallbackInfoReturnable<ItemStack> cir) {
        if (ApexEschatonSingularityHelper.isOmegaHorizonBrewingIngredient(ingredient)
                && ApexEschatonSingularityHelper.canBrewGravitationalStasisPotion(container)) {
            cir.setReturnValue(ApexEschatonSingularityHelper.createGravitationalStasisElixir());
        }
    }
}
