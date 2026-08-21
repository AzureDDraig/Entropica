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

public class CompletedStarChartItem extends Item {

    public static final String KEY_CONSTELLATION = "Constellation";

    public CompletedStarChartItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        Constellation constellation = getConstellation(stack);
        if (constellation != null) {
            String title = Component.translatable(constellation.getUnlocalizedName()).getString();
            String essCode = constellation.getEssenceType().getColorCode();
            tooltipComponents.accept(Component.literal("§6✦ Constellation: " + essCode + title));
            tooltipComponents.accept(Component.literal("§eTier: §f" + constellation.getTier().getDisplayName()));
            tooltipComponents.accept(Component.literal("§bSpectral Class: §f" + constellation.getPrimarySpectralClass().getTitle()));
            tooltipComponents.accept(Component.literal("§7Ritual: §a" + constellation.getRitualEffect()));
        } else {
            tooltipComponents.accept(Component.literal("§7Unattuned Star Chart"));
        }
        tooltipComponents.accept(Component.literal("§8Right-click: Pin to night sky for 10 minutes"));
    }

    @Override
    public net.minecraft.world.InteractionResult use(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Constellation constellation = getConstellation(stack);
        if (constellation != null) {
            if (level.isClientSide()) {
                ddraig.net.entropica.astral.PlayerAstralProgress.pinConstellation(constellation.getId(), 12000L, level.getGameTime());
            }
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.6f);
            String title = Component.translatable(constellation.getUnlocalizedName()).getString();
            player.displayClientMessage(Component.literal("§d✦ [Stargazing Pin] §fPinned §b" + title + " §fto the night sky for 10 minutes!"), true);
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }

    public static Constellation getConstellation(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            String idStr = customData.copyTag().getString(KEY_CONSTELLATION).orElse("");
            if (!idStr.isEmpty()) {
                ResourceLocation id = ResourceLocation.tryParse(idStr);
                if (id != null) {
                    return ModConstellations.getById(id).orElse(null);
                }
            }
        }
        return null;
    }

    public static ItemStack createFor(Item item, Constellation constellation) {
        ItemStack stack = new ItemStack(item);
        if (constellation != null) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                tag.putString(KEY_CONSTELLATION, constellation.getId().toString());
            });
        }
        return stack;
    }
}
