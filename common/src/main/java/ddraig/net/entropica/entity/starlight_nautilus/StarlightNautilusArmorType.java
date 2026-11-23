package ddraig.net.entropica.entity.starlight_nautilus;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * Mount armor tiers wearable by tamed adult {@link StarlightNautilusEntity} mounts,
 * supporting vanilla Horse/Nautilus armor tiers plus Biome #17's Astral Nautilus Armor.
 */
public enum StarlightNautilusArmorType {
    NONE(0, 0.0D, 0.0D, null),
    LEATHER(3, 0.0D, 0.0D, "leather"),
    COPPER(4, 0.0D, 0.0D, "copper"),
    IRON(5, 0.0D, 0.0D, "iron"),
    GOLD(7, 0.0D, 0.0D, "gold"),
    DIAMOND(11, 2.0D, 0.0D, "diamond"),
    NETHERITE(13, 3.0D, 0.20D, "netherite"),
    ASTRAL_SLATE(12, 3.5D, 0.35D, "astral");

    private final int armorBonus;
    private final double toughnessBonus;
    private final double knockbackResistanceBonus;
    private final @Nullable String textureSuffix;
    private final @Nullable ResourceLocation textureLocation;

    StarlightNautilusArmorType(
            int armorBonus,
            double toughnessBonus,
            double knockbackResistanceBonus,
            @Nullable String textureSuffix
    ) {
        this.armorBonus = armorBonus;
        this.toughnessBonus = toughnessBonus;
        this.knockbackResistanceBonus = knockbackResistanceBonus;
        this.textureSuffix = textureSuffix;
        this.textureLocation = textureSuffix == null
                ? null
                : ResourceLocation.fromNamespaceAndPath(
                        Entropica.MODID,
                        "textures/entity/starlight_nautilus/starlight_nautilus_armor_" + textureSuffix + ".png"
                );
    }

    public int getArmorBonus() {
        return this.armorBonus;
    }

    public double getToughnessBonus() {
        return this.toughnessBonus;
    }

    public double getKnockbackResistanceBonus() {
        return this.knockbackResistanceBonus;
    }

    public @Nullable String getTextureSuffix() {
        return this.textureSuffix;
    }

    public @Nullable ResourceLocation getTextureLocation() {
        return this.textureLocation;
    }

    public static StarlightNautilusArmorType fromItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return NONE;
        }
        if (ModItems.ASTRAL_NAUTILUS_ARMOR.isPresent() && stack.is(ModItems.ASTRAL_NAUTILUS_ARMOR.get())) {
            return ASTRAL_SLATE;
        }
        if (stack.is(Items.LEATHER_HORSE_ARMOR)) {
            return LEATHER;
        }
        if (stack.is(Items.IRON_HORSE_ARMOR)) {
            return IRON;
        }
        if (stack.is(Items.GOLDEN_HORSE_ARMOR)) {
            return GOLD;
        }
        if (stack.is(Items.DIAMOND_HORSE_ARMOR)) {
            return DIAMOND;
        }

        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return switch (path) {
            case "astral_nautilus_armor" -> ASTRAL_SLATE;
            case "copper_horse_armor", "copper_nautilus_armor" -> COPPER;
            case "netherite_horse_armor", "netherite_nautilus_armor" -> NETHERITE;
            case "leather_nautilus_armor" -> LEATHER;
            case "iron_nautilus_armor" -> IRON;
            case "gold_nautilus_armor", "golden_nautilus_armor" -> GOLD;
            case "diamond_nautilus_armor" -> DIAMOND;
            default -> NONE;
        };
    }
}
