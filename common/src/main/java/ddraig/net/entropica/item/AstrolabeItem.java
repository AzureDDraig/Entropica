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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

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

        if (player.isShiftKeyDown()) {
            boolean hasAnchor = tag.getBoolean(TAG_HAS_ANCHOR).orElse(false);

            // Creative mode instant auto-build assist!
            if (player.isCreative() && hasAnchor && bpIdx > 0) {
                BlockPos anchorPos = new BlockPos(
                        tag.getInt(TAG_ANCHOR_X).orElse(0),
                        tag.getInt(TAG_ANCHOR_Y).orElse(0),
                        tag.getInt(TAG_ANCHOR_Z).orElse(0)
                );

                java.util.List<ddraig.net.entropica.compat.jei.AstralMultiblockRecipe> recipes = ddraig.net.entropica.compat.jei.AstralMultiblockRecipes.createRecipes();
                if (bpIdx <= recipes.size()) {
                    ddraig.net.entropica.compat.jei.AstralMultiblockRecipe recipe = recipes.get(bpIdx - 1);
                    java.util.List<java.util.List<String>> layers = recipe.layerBlueprints();
                    java.util.Map<Character, ItemStack> legend = recipe.symbolLegend();

                    int depth = layers.get(0).size();
                    int width = layers.get(0).get(0).length();
                    int halfW = width / 2;
                    int halfD = depth / 2;

                    int placedCount = 0;
                    for (int l = 0; l < layers.size(); l++) {
                        java.util.List<String> grid = layers.get(l);
                        for (int z = 0; z < grid.size(); z++) {
                            String row = grid.get(z);
                            for (int x = 0; x < row.length(); x++) {
                                char ch = row.charAt(x);
                                if (ch == '.' || ch == ' ') continue;

                                ItemStack reqStack = legend.get(ch);
                                if (reqStack != null && reqStack.getItem() instanceof net.minecraft.world.item.BlockItem bi) {
                                    BlockPos targetPos = anchorPos.offset(x - halfW, l, z - halfD);
                                    net.minecraft.world.level.block.state.BlockState requiredState = bi.getBlock().defaultBlockState();
                                    if (!level.getBlockState(targetPos).is(bi.getBlock())) {
                                        level.setBlock(targetPos, requiredState, 3);
                                        placedCount++;
                                    }
                                }
                            }
                        }
                    }

                    if (!level.isClientSide()) {
                        level.playSound(null, clickedPos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);
                        player.displayClientMessage(Component.literal("§a[Astrolabe Auto-Build] §7Constructed §b" + placedCount + " §7blocks in-world!"), true);
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            // Otherwise, shift-clicking clears anchor for current slot
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
        }

        if (bpIdx == 0) {
            // Default to blueprint 1 (Astral Observatory) when clicking a block
            bpIdx = 1;
            tag.putInt(TAG_BLUEPRINT_INDEX, 1);
        }

        boolean hasAnchor = tag.getBoolean(TAG_HAS_ANCHOR).orElse(false);
        BlockPos anchorPos = clickedPos.above();

        if (hasAnchor) {
            BlockPos currentAnchor = new BlockPos(
                    tag.getInt(TAG_ANCHOR_X).orElse(0),
                    tag.getInt(TAG_ANCHOR_Y).orElse(0),
                    tag.getInt(TAG_ANCHOR_Z).orElse(0)
            );
            if (currentAnchor.equals(anchorPos)) {
                // Clicked same anchor -> clear anchor
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
            }
        }

        // Set new anchor for current slot
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

        // Shift + Right-click in air: Switch Memory Slot ([Slot 1] -> [Slot 2] -> [Slot 3])
        if (player.isShiftKeyDown()) {
            saveCurrentSlotToNbt(tag, activeSlot);
            int nextSlot = (activeSlot + 1) % 3;
            loadSlotFromNbt(tag, nextSlot);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            int nextBp = tag.getInt(TAG_BLUEPRINT_INDEX).orElse(0);
            boolean nextHasAnchor = tag.getBoolean(TAG_HAS_ANCHOR).orElse(false);
            BlockPos anchorPos = nextHasAnchor ? new BlockPos(tag.getInt(TAG_ANCHOR_X).orElse(0), tag.getInt(TAG_ANCHOR_Y).orElse(0), tag.getInt(TAG_ANCHOR_Z).orElse(0)) : null;
            int dist = (nextHasAnchor && player != null) ? (int) Math.sqrt(player.blockPosition().distSqr(anchorPos)) : 0;

            if (!level.isClientSide()) {
                level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8f, 1.4f);
                String status = nextHasAnchor ? (" §a(" + anchorPos.toShortString() + " - " + dist + "m)") : " §8(Unanchored)";
                player.displayClientMessage(Component.literal("§6[Astrolabe Memory] §7Active Slot: §b[" + (nextSlot + 1) + "/3] §7- Blueprint: §f" + BLUEPRINT_NAMES[nextBp] + status), true);
            }
            return InteractionResult.SUCCESS;
        }

        // If no blueprint selected, right-click opens the Sky Looking Glass directly!
        if (bpIdx == 0) {
            if (level.isClientSide()) {
                SkyLookingGlassScreen.openForInstrument(SkyLookingGlassScreen.InstrumentType.ASTROLABE);
            }
            return InteractionResult.SUCCESS;
        }

        // If anchored, right-click cycles layer slices (All -> Layer 1 -> Layer 2 -> ...)
        if (hasAnchor) {
            java.util.List<ddraig.net.entropica.compat.jei.AstralMultiblockRecipe> recipes = ddraig.net.entropica.compat.jei.AstralMultiblockRecipes.createRecipes();
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
        } else {
            // Not anchored -> cycle blueprint for current slot
            int nextIdx = (bpIdx + 1) % BLUEPRINT_NAMES.length;
            tag.putInt(TAG_BLUEPRINT_INDEX, nextIdx);
            saveCurrentSlotToNbt(tag, activeSlot);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            if (!level.isClientSide()) {
                level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.7f, 1.4f);
                player.displayClientMessage(Component.literal("§6[Astrolabe Slot " + (activeSlot + 1) + "] §7Active Blueprint: §b" + BLUEPRINT_NAMES[nextIdx]), true);
            }
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = cd.copyTag();

        int activeSlot = getActiveSlot(tag);
        int bpIdx = tag.getInt(TAG_BLUEPRINT_INDEX).orElse(0);
        boolean hasAnchor = tag.getBoolean(TAG_HAS_ANCHOR).orElse(false);
        int activeLayer = tag.getInt(TAG_ACTIVE_LAYER).orElse(0);

        tooltipComponents.accept(Component.literal("§6◆ Memory Slot: §b[" + (activeSlot + 1) + "/3]"));
        tooltipComponents.accept(Component.literal("§7  Blueprint: §f" + BLUEPRINT_NAMES[bpIdx]));
        if (hasAnchor) {
            BlockPos anchor = new BlockPos(
                    tag.getInt(TAG_ANCHOR_X).orElse(0),
                    tag.getInt(TAG_ANCHOR_Y).orElse(0),
                    tag.getInt(TAG_ANCHOR_Z).orElse(0)
            );
            tooltipComponents.accept(Component.literal("§7  Anchor: §a(" + anchor.toShortString() + ")"));
            tooltipComponents.accept(Component.literal("§7  Active Slice: §e" + (activeLayer == 0 ? "All Layers" : "Layer " + activeLayer)));
        }

        tooltipComponents.accept(Component.literal("§8Right-click: Open Sky / Cycle Slices / Anchor"));
        tooltipComponents.accept(Component.literal("§8Shift+Right-click: Cycle Memory Slot [1..3] / Clear Anchor"));
    }

    public static boolean isScoping(Player player) {
        return player.isUsingItem() && (player.getUseItem().getItem() instanceof LookingGlassItem || player.getUseItem().getItem() instanceof AstrolabeItem);
    }
}
