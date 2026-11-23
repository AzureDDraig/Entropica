package ddraig.net.entropica.item.biome3;

import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Cryo-Insulated Armor — 4-piece expedition suit stitched from Cured Frost-Veined Hide.
 * - Any piece worn clears freeze buildup in inventoryTick.
 * - Boots prevent Rotten Ice collapse and powder snow freezing.
 * - Full 4-piece suit cleanses Frost-Bite Floes water Slowness and Rotten Ice Poison,
 *   granting complete hypothermia and frostbite immunity.
 */
public class CryoInsulatedArmorItem extends Item {

    private static final ResourceKey<Biome> FROST_BITE_FLOES_KEY = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "frost_bite_floes")
    );

    private static final EquipmentSlot[] HUMANOID_ARMOR_SLOTS = new EquipmentSlot[]{
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private final EquipmentSlot slot;

    public CryoInsulatedArmorItem(EquipmentSlot slot, int durability, double armor, double toughness, Properties properties) {
        super(applyArmorProperties(slot, durability, armor, toughness, properties));
        this.slot = slot;
    }

    private static Properties applyArmorProperties(EquipmentSlot slot, int durability, double armor, double toughness, Properties properties) {
        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(
                "entropica",
                "cryo_insulated_" + slot.getName()
        );
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(slot);
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(modifierId, armor, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, toughness, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .build();

        return properties
                .stacksTo(1)
                .durability(durability)
                .equippable(slot)
                .attributes(modifiers);
    }

    public EquipmentSlot getSlot() {
        return this.slot;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        boolean isWorn = slot == this.slot || living.getItemBySlot(this.slot) == stack;
        if (!isWorn) {
            return;
        }

        // At least 1 piece worn clears freeze buildup (including powder snow chill)
        if (living.getTicksFrozen() > 0) {
            living.setTicksFrozen(0);
        }

        // Full 4-piece set cleanses Frost-Bite Floes water Slowness & Rotten Ice Poison
        if (hasFullCryoInsulation(living)) {
            boolean inFloes = level.getBiome(living.blockPosition()).is(FROST_BITE_FLOES_KEY);
            if ((living.isInWater() || inFloes) && living.hasEffect(MobEffects.SLOWNESS)) {
                living.removeEffect(MobEffects.SLOWNESS);
            }
            if ((inFloes || isNearBlightIceOrSlime(level, living)) && living.hasEffect(MobEffects.POISON)) {
                living.removeEffect(MobEffects.POISON);
            }
        }
    }

    private static boolean isNearBlightIceOrSlime(ServerLevel level, LivingEntity entity) {
        BlockPos onPos = entity.getOnPos();
        BlockState below = level.getBlockState(onPos);
        BlockState inside = level.getBlockState(entity.blockPosition());
        return (ModBlocks.ROTTEN_ICE.isBound() && (below.is(ModBlocks.ROTTEN_ICE.get()) || inside.is(ModBlocks.ROTTEN_ICE.get())))
                || (ModBlocks.NECROTIC_SLIME_BLOCK.isBound() && (below.is(ModBlocks.NECROTIC_SLIME_BLOCK.get()) || inside.is(ModBlocks.NECROTIC_SLIME_BLOCK.get())))
                || (ModBlocks.NECROTIC_SLIME_PATCH.isBound() && (below.is(ModBlocks.NECROTIC_SLIME_PATCH.get()) || inside.is(ModBlocks.NECROTIC_SLIME_PATCH.get())));
    }

    /**
     * Returns true if {@code entity} is wearing at least one piece of Cryo-Insulated Armor.
     */
    public static boolean hasAnyCryoInsulation(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        for (EquipmentSlot armorSlot : HUMANOID_ARMOR_SLOTS) {
            if (isCryoPieceForSlot(entity.getItemBySlot(armorSlot), armorSlot)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if {@code entity} is wearing Cryo-Insulated Boots in {@link EquipmentSlot#FEET}.
     */
    public static boolean hasCryoBoots(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        return isCryoPieceForSlot(entity.getItemBySlot(EquipmentSlot.FEET), EquipmentSlot.FEET);
    }

    /**
     * Returns true if {@code entity} is wearing all four pieces of Cryo-Insulated Armor.
     */
    public static boolean hasFullCryoInsulation(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        for (EquipmentSlot armorSlot : HUMANOID_ARMOR_SLOTS) {
            if (!isCryoPieceForSlot(entity.getItemBySlot(armorSlot), armorSlot)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isCryoPieceForSlot(ItemStack stack, EquipmentSlot expectedSlot) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof CryoInsulatedArmorItem cryoItem) {
            return cryoItem.slot == expectedSlot;
        }
        return switch (expectedSlot) {
            case HEAD -> ModItems.CRYO_INSULATED_HELMET.isBound() && stack.is(ModItems.CRYO_INSULATED_HELMET.get());
            case CHEST -> ModItems.CRYO_INSULATED_CHESTPLATE.isBound() && stack.is(ModItems.CRYO_INSULATED_CHESTPLATE.get());
            case LEGS -> ModItems.CRYO_INSULATED_LEGGINGS.isBound() && stack.is(ModItems.CRYO_INSULATED_LEGGINGS.get());
            case FEET -> ModItems.CRYO_INSULATED_BOOTS.isBound() && stack.is(ModItems.CRYO_INSULATED_BOOTS.get());
            default -> false;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Worn: Clears frostbite chill from Glacial Materia exposure.")
                .withStyle(ChatFormatting.AQUA));
        if (this.slot == EquipmentSlot.FEET) {
            tooltipComponents.accept(Component.literal("Boots: Treads safely across Rotten Ice without triggering collapse.")
                    .withStyle(ChatFormatting.GREEN));
        }
        tooltipComponents.accept(Component.literal("Full Set: Immunity to Frost-Bite Floes hypothermia and Rotten Ice poison.")
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
