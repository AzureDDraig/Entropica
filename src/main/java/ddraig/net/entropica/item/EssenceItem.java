package ddraig.net.entropica.item;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.EssenceOrbEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EssenceItem extends Item {
    private final int tier;

    public EssenceItem(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    public static void setEssenceType(ItemStack stack, EssenceType type) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString("EssenceType", type.name());
        });
    }

    public static EssenceType getEssenceType(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.copyTag().contains("EssenceType")) {
            try {
                return EssenceType.valueOf(data.copyTag().getString("EssenceType").orElse(""));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        EssenceType type = getEssenceType(stack);
        if (type != null) {
            tooltipComponents.add(Component.literal("Type: " + type.getDisplayName())
                    .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(type.getTextColorInt())));
        }
    }

    public Component getName(ItemStack stack) {
        EssenceType type = getEssenceType(stack);
        if (type != null) {
            if (this.tier == 0) {
                return Component.literal("Materia Fragment: " + type.getDisplayName())
                        .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(type.getTextColorInt()));
            } else {
                String tierPrefix = tier == 1 ? "Weak " : tier == 2 ? "Average " : "Strong ";
                return Component.literal(tierPrefix + type.getDisplayName() + " Essence")
                        .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(type.getTextColorInt()));
            }
        }
        return super.getName(stack);
    }

    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        if (location instanceof ItemEntity vanillaEntity) {
            EssenceOrbEntity orb = new EssenceOrbEntity(level, vanillaEntity.getX(), vanillaEntity.getY(), vanillaEntity.getZ(), stack);

            orb.setDeltaMovement(vanillaEntity.getDeltaMovement());
            orb.setPickUpDelay(40);

            if (vanillaEntity.getOwner() instanceof Player) {
                orb.markThrownByPlayer();
            }

            return orb;
        }
        return null;
    }
}