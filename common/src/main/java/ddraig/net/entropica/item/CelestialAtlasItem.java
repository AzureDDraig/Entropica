package ddraig.net.entropica.item;

import ddraig.net.entropica.astral.Constellation;
import ddraig.net.entropica.client.gui.CelestialAtlasScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class CelestialAtlasItem extends Item {

    public static final String TAG_CHARTS = "DiscoveredCharts";

    public CelestialAtlasItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            // Shift-right-click: scan player inventory and absorb all completed star charts!
            Set<String> discovered = getDiscoveredConstellationIds(stack);
            int added = 0;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (invStack.getItem() instanceof CompletedStarChartItem) {
                    Constellation c = CompletedStarChartItem.getConstellation(invStack);
                    if (c != null && !discovered.contains(c.getId().toString())) {
                        discovered.add(c.getId().toString());
                        added++;
                        if (!player.isCreative()) {
                            invStack.shrink(1);
                        }
                    }
                }
            }

            if (added > 0) {
                saveDiscoveredConstellationIds(stack, discovered);
                if (!level.isClientSide()) {
                    level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.2f);
                    player.displayClientMessage(Component.literal("§6[Celestial Atlas] §7Absorbed §b" + added + " §7new Star Charts! Total: §f" + discovered.size() + " / 24"), true);
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.literal("§7[Celestial Atlas] No new Star Charts found in inventory. (Current: " + discovered.size() + " / 24)"), true);
                }
                return InteractionResult.SUCCESS;
            }
        }

        // Normal right-click: open Celestial Atlas Screen
        if (level.isClientSide()) {
            CelestialAtlasScreen.open(stack);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        Set<String> discovered = new HashSet<>(getDiscoveredConstellationIds(stack));
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.player != null) {
                Set<ResourceLocation> playerDiscovered = ddraig.net.entropica.astral.PlayerAstralProgress.getDiscovered(mc.player);
                for (ResourceLocation rl : playerDiscovered) {
                    discovered.add(rl.toString());
                }
            }
        } catch (Throwable ignored) {}

        int totalCount = ddraig.net.entropica.astral.ModConstellations.getAllConstellations().size();
        tooltipComponents.accept(Component.literal("§6✦ Discovered Constellations: §f" + discovered.size() + " / " + totalCount));
        tooltipComponents.accept(Component.literal("§8Right-click: Browse Celestial Atlas"));
        tooltipComponents.accept(Component.literal("§8Shift+Right-click: Store Star Charts from Inventory"));
    }

    public static Set<String> getDiscoveredConstellationIds(ItemStack stack) {
        Set<String> set = new HashSet<>();
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = cd.copyTag();
        ListTag list = tag.getList(TAG_CHARTS).orElse(new ListTag());
        for (int i = 0; i < list.size(); i++) {
            list.getString(i).ifPresent(set::add);
        }
        return set;
    }

    public static void saveDiscoveredConstellationIds(ItemStack stack, Set<String> set) {
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = cd.copyTag();
        ListTag list = new ListTag();
        for (String id : set) {
            list.add(StringTag.valueOf(id));
        }
        tag.put(TAG_CHARTS, list);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
