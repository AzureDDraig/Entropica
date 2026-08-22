package ddraig.net.entropica.item;

import ddraig.net.entropica.client.gui.SkyLookingGlassScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public class AstrolabeItem extends Item {

    public static final String TAG_ACTIVE_SLOT = "ActiveSlot";
    public static final String TAG_BLUEPRINT_INDEX = "BlueprintIndex";
    public static final String TAG_HAS_ANCHOR = "HasAnchor";
    public static final String TAG_ANCHOR_X = "AnchorX";
    public static final String TAG_ANCHOR_Y = "AnchorY";
    public static final String TAG_ANCHOR_Z = "AnchorZ";
    public static final String TAG_ACTIVE_LAYER = "ActiveLayer";

    public static final String[] BLUEPRINT_NAMES = new String[]{
            "None",
            "Astral Observatory (7x6x7)",
            "Celestial Beacon (3x3x3)",
            "Modular Astral Altar (5x3x5)"
    };

    public AstrolabeItem(Properties properties) {
        super(properties);
    }

    public static boolean isScoping(Player player) {
        if (player == null) return false;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return (main.getItem() instanceof AstrolabeItem || main.getItem() instanceof LookingGlassItem ||
                off.getItem() instanceof AstrolabeItem || off.getItem() instanceof LookingGlassItem) && player.isUsingItem();
    }

    public static int getActiveSlot(CompoundTag tag) {
        return Math.max(0, tag.getInt(TAG_ACTIVE_SLOT).orElse(0) % 3);
    }

    public static void saveCurrentSlotToNbt(CompoundTag tag, int slot) {
        String prefix = "Slot_" + slot + "_";
        tag.putInt(prefix + TAG_BLUEPRINT_INDEX, tag.getInt(TAG_BLUEPRINT_INDEX).orElse(0));
        tag.putBoolean(prefix + TAG_HAS_ANCHOR, tag.getBoolean(TAG_HAS_ANCHOR).orElse(false));
        tag.putInt(prefix + TAG_ANCHOR_X, tag.getInt(TAG_ANCHOR_X).orElse(0));
        tag.putInt(prefix + TAG_ANCHOR_Y, tag.getInt(TAG_ANCHOR_Y).orElse(0));
        tag.putInt(prefix + TAG_ANCHOR_Z, tag.getInt(TAG_ANCHOR_Z).orElse(0));
        tag.putInt(prefix + TAG_ACTIVE_LAYER, tag.getInt(TAG_ACTIVE_LAYER).orElse(0));
    }

    public static void loadSlotFromNbt(CompoundTag tag, int slot) {
        String prefix = "Slot_" + slot + "_";
        tag.putInt(TAG_ACTIVE_SLOT, slot);
        tag.putInt(TAG_BLUEPRINT_INDEX, tag.getInt(prefix + TAG_BLUEPRINT_INDEX).orElse(0));
        tag.putBoolean(TAG_HAS_ANCHOR, tag.getBoolean(prefix + TAG_HAS_ANCHOR).orElse(false));
        tag.putInt(TAG_ANCHOR_X, tag.getInt(prefix + TAG_ANCHOR_X).orElse(0));
        tag.putInt(TAG_ANCHOR_Y, tag.getInt(prefix + TAG_ANCHOR_Y).orElse(0));
        tag.putInt(TAG_ANCHOR_Z, tag.getInt(prefix + TAG_ANCHOR_Z).orElse(0));
        tag.putInt(TAG_ACTIVE_LAYER, tag.getInt(prefix + TAG_ACTIVE_LAYER).orElse(0));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) {
            return InteractionResult.PASS;
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        int activeSlot = getActiveSlot(tag);
        int bpIdx = tag.getInt(TAG_BLUEPRINT_INDEX).orElse(0);
        boolean hasAnchor = tag.getBoolean(TAG_HAS_ANCHOR).orElse(false);

        // 1. Shift + Right-Click:
        if (player.isShiftKeyDown()) {
            if (hasAnchor) {
                // If anchored, Shift + Right-Click clears the anchor for the active slot
                tag.remove(TAG_HAS_ANCHOR);
                tag.remove(TAG_ANCHOR_X);
                tag.remove(TAG_ANCHOR_Y);
                tag.remove(TAG_ANCHOR_Z);
                tag.remove(TAG_ACTIVE_LAYER);
                saveCurrentSlotToNbt(tag, activeSlot);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.literal("§e[Astrolabe Slot " + (activeSlot + 1) + "] §7Cleared blueprint anchor."), true);
                }
                return InteractionResult.SUCCESS;
            } else {
                // If NOT anchored, Shift + Right-Click cycles the blueprint type
                int nextIdx = (bpIdx + 1) % BLUEPRINT_NAMES.length;
                tag.putInt(TAG_BLUEPRINT_INDEX, nextIdx);
                saveCurrentSlotToNbt(tag, activeSlot);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                if (!level.isClientSide()) {
                    level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8f, 1.2f);
                    player.displayClientMessage(Component.literal("§6[Astrolabe Slot " + (activeSlot + 1) + "] §7Selected Blueprint: §b" + BLUEPRINT_NAMES[nextIdx]), true);
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 2. Normal Right-Click on a Block:
        if (bpIdx == 0) {
            // Default to blueprint 1 (Astral Observatory) when clicking a block
            bpIdx = 1;
            tag.putInt(TAG_BLUEPRINT_INDEX, 1);
        }

        BlockPos anchorPos = clickedPos;

        if (hasAnchor) {
            BlockPos currentAnchor = new BlockPos(
                    tag.getInt(TAG_ANCHOR_X).orElse(0),
                    tag.getInt(TAG_ANCHOR_Y).orElse(0),
                    tag.getInt(TAG_ANCHOR_Z).orElse(0)
            );
            if (currentAnchor.equals(anchorPos)) {
                // Clicked existing anchor -> cycle layer slices
                return cycleLayerSlice(level, player, stack, tag, activeSlot, bpIdx);
            }
        }

        // Anchor at clicked block pos
        tag.putBoolean(TAG_HAS_ANCHOR, true);
        tag.putInt(TAG_ANCHOR_X, anchorPos.getX());
        tag.putInt(TAG_ANCHOR_Y, anchorPos.getY());
        tag.putInt(TAG_ANCHOR_Z, anchorPos.getZ());
        tag.putInt(TAG_ACTIVE_LAYER, 0);
        saveCurrentSlotToNbt(tag, activeSlot);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (!level.isClientSide()) {
            level.playSound(null, clickedPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.3f);
            player.displayClientMessage(Component.literal("§6[Astrolabe Slot " + (activeSlot + 1) + "] §7Anchored §b" + BLUEPRINT_NAMES[bpIdx] + " §7at §f(" + anchorPos.toShortString() + ")§7."), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        int activeSlot = getActiveSlot(tag);
        int bpIdx = tag.getInt(TAG_BLUEPRINT_INDEX).orElse(0);
        boolean hasAnchor = tag.getBoolean(TAG_HAS_ANCHOR).orElse(false);

        // 1. Shift + Right-click in air: Cycle Blueprint Type
        if (player.isShiftKeyDown()) {
            int nextIdx = (bpIdx + 1) % BLUEPRINT_NAMES.length;
            tag.putInt(TAG_BLUEPRINT_INDEX, nextIdx);
            saveCurrentSlotToNbt(tag, activeSlot);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            if (!level.isClientSide()) {
                level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8f, 1.2f);
                player.displayClientMessage(Component.literal("§6[Astrolabe Slot " + (activeSlot + 1) + "] §7Selected Blueprint: §b" + BLUEPRINT_NAMES[nextIdx]), true);
            }
            return InteractionResult.SUCCESS;
        }

        // 2. Normal Right-click in air:
        if (bpIdx == 0) {
            // No blueprint selected -> open Sky Looking Glass interface!
            if (level.isClientSide()) {
                SkyLookingGlassScreen.openForInstrument(SkyLookingGlassScreen.InstrumentType.ASTROLABE);
            }
            return InteractionResult.SUCCESS;
        }

        if (hasAnchor) {
            // Anchored -> cycle layer slices
            return cycleLayerSlice(level, player, stack, tag, activeSlot, bpIdx);
        } else {
            // Not anchored -> inform player to aim at ground
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("§7[Astrolabe] Right-click a ground block to anchor §b" + BLUEPRINT_NAMES[bpIdx] + "§7. Shift + Right-Click to change blueprint."), true);
            }
            return InteractionResult.SUCCESS;
        }
    }

    private InteractionResult cycleLayerSlice(Level level, Player player, ItemStack stack, CompoundTag tag, int activeSlot, int bpIdx) {
        List<ddraig.net.entropica.compat.jei.AstralMultiblockRecipe> recipes = ddraig.net.entropica.compat.jei.AstralMultiblockRecipes.createRecipes();
        int totalLayers = 6;
        if (bpIdx <= recipes.size()) {
            totalLayers = recipes.get(bpIdx - 1).layerBlueprints().size();
        }

        int curLayer = tag.getInt(TAG_ACTIVE_LAYER).orElse(0);
        int nextLayer = (curLayer + 1) % (totalLayers + 1);
        tag.putInt(TAG_ACTIVE_LAYER, nextLayer);
        saveCurrentSlotToNbt(tag, activeSlot);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.6f, 1.6f);
            String layerMsg = nextLayer == 0 ? "§aAll Layers (Full Structure)" : "§eLayer " + nextLayer + " / " + totalLayers;
            player.displayClientMessage(Component.literal("§6[Astrolabe View] §7Active Slice: " + layerMsg), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        int activeSlot = getActiveSlot(tag);
        int bpIdx = tag.getInt(TAG_BLUEPRINT_INDEX).orElse(0);
        boolean hasAnchor = tag.getBoolean(TAG_HAS_ANCHOR).orElse(false);

        tooltipComponents.accept(Component.literal("§6◆ Precision Brass Astrolabe §8(1x - 8x Zoom)"));
        tooltipComponents.accept(Component.literal("§e  Active Memory Slot: §f[" + (activeSlot + 1) + " / 3]"));
        tooltipComponents.accept(Component.literal("§b  Blueprint: §f" + BLUEPRINT_NAMES[bpIdx]));

        if (hasAnchor) {
            BlockPos anchorPos = new BlockPos(
                    tag.getInt(TAG_ANCHOR_X).orElse(0),
                    tag.getInt(TAG_ANCHOR_Y).orElse(0),
                    tag.getInt(TAG_ANCHOR_Z).orElse(0)
            );
            int curLayer = tag.getInt(TAG_ACTIVE_LAYER).orElse(0);
            String layerStr = curLayer == 0 ? "All" : ("Layer " + curLayer);
            tooltipComponents.accept(Component.literal("§a  Anchored at: §f(" + anchorPos.toShortString() + ") §8[Slice: " + layerStr + "]"));
        } else {
            tooltipComponents.accept(Component.literal("§8  Status: Unanchored"));
        }

        tooltipComponents.accept(Component.literal("§7  • Sneak + Scroll: Switch memory slots (1-3)"));
        tooltipComponents.accept(Component.literal("§7  • Sneak + Right-Click: Change blueprint"));
        tooltipComponents.accept(Component.literal("§7  • Right-Click Block: Anchor blueprint in-world"));
        tooltipComponents.accept(Component.literal("§7  • Right-Click Air: Cycle layer slices"));
    }
}
