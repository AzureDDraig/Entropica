package ddraig.net.entropica.mixin;

import ddraig.net.entropica.config.EntropicaBiome;
import ddraig.net.entropica.worldgen.biome.AstralSkerriesFishingHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingHook.class)
public abstract class FishingHookAstralMixin {

    @Redirect(
            method = "retrieve",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"
            )
    )
    private ObjectArrayList<ItemStack> entropica$replaceAstralSkerriesFishingLoot(
            LootTable lootTable,
            LootParams lootParams
    ) {
        ObjectArrayList<ItemStack> items = lootTable.getRandomItems(lootParams);
        FishingHook self = (FishingHook) (Object) this;
        if (self.level().getBiome(self.blockPosition()).is(EntropicaBiome.ASTRAL_SKERRIES.resourceKey())) {
            AstralSkerriesFishingHelper.replaceAstralSkerriesCatches(self, lootParams, items);
        }
        return items;
    }
}
