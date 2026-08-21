package ddraig.net.entropica.item;

import ddraig.net.entropica.astral.Constellation;
import ddraig.net.entropica.astral.ModConstellations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class AstralCrystalItem extends Item {

    public static final String KEY_SIZE = "Size";
    public static final String KEY_PURITY = "Purity";
    public static final String KEY_CUT = "Cut";
    public static final String KEY_RITUAL = "Ritual";
    public static final String KEY_MATERIA = "MateriaStored";

    public AstralCrystalItem(Properties properties) {
        super(properties);
    }

    public static int getSize(ItemStack stack) {
        if (stack.isEmpty()) return 1;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return 1;
        return Math.max(1, Math.min(5, cd.copyTag().getInt(KEY_SIZE).orElse(1)));
    }

    public static void setSize(ItemStack stack, int size) {
        int clamped = Math.max(1, Math.min(5, size));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(KEY_SIZE, clamped));
    }

    public static int getPurity(ItemStack stack) {
        if (stack.isEmpty()) return 100;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return 100;
        return Math.max(1, Math.min(100, cd.copyTag().getInt(KEY_PURITY).orElse(100)));
    }

    public static void setPurity(ItemStack stack, int purity) {
        int clamped = Math.max(1, Math.min(100, purity));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(KEY_PURITY, clamped));
    }

    public static int getCut(ItemStack stack) {
        if (stack.isEmpty()) return 100;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return 100;
        return Math.max(1, Math.min(100, cd.copyTag().getInt(KEY_CUT).orElse(100)));
    }

    public static void setCut(ItemStack stack, int cut) {
        int clamped = Math.max(1, Math.min(100, cut));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(KEY_CUT, clamped));
    }

    public static Constellation getRitual(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return null;
        String idStr = cd.copyTag().getString(KEY_RITUAL).orElse("");
        if (idStr.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(idStr);
        if (id == null) return null;
        return ModConstellations.getById(id).orElse(null);
    }

    public static void setRitual(ItemStack stack, ResourceLocation constellationId) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (constellationId != null) {
                tag.putString(KEY_RITUAL, constellationId.toString());
            } else {
                tag.remove(KEY_RITUAL);
            }
        });
    }

    public static int getStoredMateria(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return 0;
        return Math.max(0, cd.copyTag().getInt(KEY_MATERIA).orElse(0));
    }

    public static void setStoredMateria(ItemStack stack, int materia) {
        int max = getMaxMateria(stack);
        int clamped = Math.max(0, Math.min(max, materia));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(KEY_MATERIA, clamped));
    }

    public static int getMaxMateria(ItemStack stack) {
        return getSize(stack) * 1000;
    }

    public static String toRomanNumeral(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(number);
        };
    }

    @Override
    public Component getName(ItemStack stack) {
        Constellation ritual = getRitual(stack);
        int size = getSize(stack);
        if (ritual != null) {
            String ritualName = Component.translatable(ritual.getUnlocalizedName()).getString();
            return Component.literal("§dInscribed Astral Crystal §7(" + ritualName + " - " + toRomanNumeral(size) + ")");
        } else if (size >= 5) {
            return Component.literal("§bFlawless Astral Crystal (Size V)");
        } else if (size > 1) {
            return Component.literal("§bAstral Crystal (Size " + toRomanNumeral(size) + ")");
        }
        return super.getName(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getRitual(stack) != null || getSize(stack) == 5;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        int size = getSize(stack);
        int purity = getPurity(stack);
        int cut = getCut(stack);
        Constellation ritual = getRitual(stack);
        int stored = getStoredMateria(stack);
        int max = getMaxMateria(stack);

        tooltipComponents.accept(Component.literal("§6◆ Crystal Properties:"));
        tooltipComponents.accept(Component.literal("§7  Size Tier: §e" + toRomanNumeral(size) + " §8(" + size + "/5)"));
        tooltipComponents.accept(Component.literal("§7  Purity: §a" + purity + "%"));
        tooltipComponents.accept(Component.literal("§7  Cut Facet: §b" + cut + "%"));

        if (ritual != null) {
            String title = Component.translatable(ritual.getUnlocalizedName()).getString();
            String color = ritual.getEssenceType().getColorCode();
            tooltipComponents.accept(Component.literal("§d✦ Inscribed Ritual: " + color + title));
            tooltipComponents.accept(Component.literal("§7  Sanctuary Effect: §a" + ritual.getRitualEffect()));
            tooltipComponents.accept(Component.literal("§7  Materia Charge: §3" + stored + " §7/ §b" + max + " mB"));
            tooltipComponents.accept(Component.literal("§8Mount on Attunement Pedestal to radiate field."));
        } else {
            tooltipComponents.accept(Component.literal("§8Uninscribed - Irradiate on Infusion Pedestal"));
            tooltipComponents.accept(Component.literal("§8Place on Astral Altar at night to grow Size"));
        }
    }
}
