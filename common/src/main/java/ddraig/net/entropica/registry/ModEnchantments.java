package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantments {

    // This key tells our Java code exactly where to look for the Essence Reap JSON file
    public static final ResourceKey<Enchantment> ESSENCE_REAP = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "essence_reap")
    );

    // This key tells our Java code exactly where to look for the Augment Slot Expansion JSON file
    public static final ResourceKey<Enchantment> AUGMENT_SLOT_EXPANSION = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "augment_slot_expansion")
    );

}