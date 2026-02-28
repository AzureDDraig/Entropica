package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.fumes.IFumeHandler;
import ddraig.net.entropica.api.fumes.VisFumeStack;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.item.ManaAmpouleItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModRecipes;
import ddraig.net.entropica.recipe.FusionRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents the Entropic Core block entity that manages complex processing,
 * mana and essence pooling, multiblock structure validation, and burn mode toggling.
 * The Entropic Core acts as the central component of the multiblock structure
 * for advanced crafting and resource management.
 *
 * Fields:
 * - manaPool: Tracks the mana stored in the core by essence type.
 * - essencePool: Tracks the raw essence stored in the core by essence type.
 * - sharedReceptacleBuffer: Holds temporary inventory for sharing items between receptacles.
 * - isActive: Indicates whether the core is currently active.
 * - isFormed: Indicates whether the multiblock structure is correctly formed.
 * - currentMode: Defines the current burn mode (e.g., Regular, Elemental, or Fusion).
 * - tickCounter: Tracks the number of ticks elapsed in the core's lifecycle.
 * - validationTimer: Countdown timer for periodically validating the multiblock structure.
 * - receptacleCount: Number of receptacles connected to the core.
 * - catalystCount: Number of catalysts in attached receptacles.
 * - coreCount: Number of cores within the multiblock structure (should be one for the master block).
 * - masterPos: Position of the master core within the multiblock structure.
 * - renderHatchPos: Position of the active render hatch for interacting with players.
 * - lastValidationError: Holds the last error message if the multiblock validation failed.
 * - connectedPlumes: Tracks connected plumes for mana and essence transfer.
 */
public class EntropicCoreBlockEntity extends BlockEntity {

    public enum BurnMode {
        REGULAR("Regular Mana Production", "§d"),
        ELEMENTAL("Elemental Mana Production", "§b"),
        FUSION("Elemental Fusion", "§5");

        private final String displayName;
        private final String color;

        BurnMode(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        public BurnMode next() {
            return values()[(this.ordinal() + 1) % values().length];
        }

        public String getFormattedName() {
            return this.color + this.displayName;
        }
    }

    private final Map<EssenceType, Integer> manaPool = new EnumMap<>(EssenceType.class);
    private final Map<EssenceType, Integer> essencePool = new EnumMap<>(EssenceType.class);

    public final SimpleContainer sharedReceptacleBuffer = new SimpleContainer(54) {
        @Override
        public void setChanged() {
            super.setChanged();
            EntropicCoreBlockEntity.this.setChanged();
        }
    };

    private boolean isActive = false;
    private boolean isFormed = false;
    private BurnMode currentMode = BurnMode.ELEMENTAL;
    private int tickCounter = 0;
    private int validationTimer = 0;
    private int receptacleCount = 0;
    private int catalystCount = 0;
    private int coreCount = 0;

    private BlockPos masterPos = null;
    private BlockPos renderHatchPos = null;
    private String lastValidationError = "Structure has not been validated yet.";

    // --- NEW: Plume Location Cache ---
    private final List<BlockPos> connectedPlumes = new ArrayList<>();

    public EntropicCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENTROPIC_CORE_BE.get(), pos, state);
        for (EssenceType type : EssenceType.values()) {
            manaPool.put(type, 0);
            essencePool.put(type, 0);
        }
    }

    public boolean isMaster() {
        return masterPos == null || masterPos.equals(this.worldPosition);
    }

    public EntropicCoreBlockEntity getMaster() {
        if (isMaster()) return this;
        if (level != null && level.getBlockEntity(masterPos) instanceof EntropicCoreBlockEntity master) {
            return master;
        }
        return this;
    }

    public void updateLastInteractedHatch(BlockPos pos) {
        EntropicCoreBlockEntity master = getMaster();
        if (level != null && level.getBlockState(pos).is(ModBlocks.FURNACE_HATCH.get())) {
            master.renderHatchPos = pos;
            master.setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(master.worldPosition, master.getBlockState(), master.getBlockState(), 3);
            }
        }
    }

    public void toggleFurnaceFromRedstone() {
        EntropicCoreBlockEntity master = getMaster();
        if (!master.isFormed) return;
        master.isActive = !master.isActive;
        if (master.isActive && (master.getTotalEssence() < 10 || master.getTotalMana() >= master.getMaxMana())) {
            master.isActive = false;
        }
        master.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(master.worldPosition, master.getBlockState(), master.getBlockState(), 3);
        }
    }

    public int getComparatorOutput() {
        EntropicCoreBlockEntity master = getMaster();
        if (!master.isFormed) return 0;
        int maxMana = master.getMaxMana();
        if (maxMana == 0) return 0;
        return Math.max(0, (int) Math.floor(((double) master.getTotalMana() / maxMana) * 15.0));
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        this.validationTimer++;
        if (this.validationTimer >= 20) {
            this.validationTimer = 0;
            checkMultiblock();
        }

        if (isMaster() && this.isFormed) {
            processSharedBuffer();

            if (this.isActive) {
                this.tickCounter++;
                if (this.tickCounter >= EntropicaConfig.CORE_PROCESS_TICK_RATE.get()) {
                    this.tickCounter = 0;
                    processProcessing(level, pos, state);
                }
            }

            // --- NEW: Auto-push Gas into Pipes ---
            if (level.getGameTime() % EntropicaConfig.VIS_FUME_TICK_RATE.get() == 0) {
                pushManaToPipes(level);
            }
        }
    }

    // --- NEW: Gas Extractor Logic ---
    private void pushManaToPipes(Level level) {
        EssenceType typeToPush = null;
        int maxMana = 0;

        for (Map.Entry<EssenceType, Integer> entry : this.manaPool.entrySet()) {
            if (entry.getValue() > maxMana) {
                maxMana = entry.getValue();
                typeToPush = entry.getKey();
            }
        }

        if (typeToPush == null || maxMana <= 0) return;

        int amountLeftToPush = Math.min(maxMana, EntropicaConfig.VIS_FUME_TRANSFER_RATE.get());

        for (BlockPos plumePos : this.connectedPlumes) {
            if (amountLeftToPush <= 0) break;

            if (level.getBlockState(plumePos).is(ModBlocks.MANA_PLUME.get())) {
                for (Direction dir : Direction.values()) {
                    if (amountLeftToPush <= 0) break;

                    BlockPos targetPos = plumePos.relative(dir);
                    BlockEntity targetBE = level.getBlockEntity(targetPos);

                    if (targetBE instanceof IFumeHandler handler) {
                        VisFumeStack pushStack = new VisFumeStack(typeToPush, amountLeftToPush);

                        int accepted = handler.fill(pushStack, false);

                        if (accepted > 0) {
                            this.extractMana(typeToPush, accepted);
                            amountLeftToPush -= accepted;
                            this.setChanged();
                        }
                    }
                }
            }
        }
    }

    private void processSharedBuffer() {
        int maxCapacity = getMaxEssencePerType();
        boolean changed = false;

        for (int i = 0; i < sharedReceptacleBuffer.getContainerSize(); i++) {
            ItemStack stack = sharedReceptacleBuffer.getItem(i);
            if (!stack.isEmpty()) {
                EssenceType type = getEssenceTypeFromItem(stack);
                if (type != null) {
                    int val = getEssenceValue(stack);
                    int current = essencePool.getOrDefault(type, 0);

                    if (current + val <= maxCapacity) {
                        int space = maxCapacity - current;
                        int itemsToConsume = Math.min(stack.getCount(), space / val);

                        if (itemsToConsume > 0) {
                            sharedReceptacleBuffer.removeItem(i, itemsToConsume);
                            essencePool.put(type, current + (itemsToConsume * val));
                            changed = true;
                        }
                    }
                }
            }
        }

        if (changed) {
            this.setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public ItemStack insertIntoSharedBuffer(ItemStack stack) {
        ItemStack remainder = stack.copy();
        for (int i = 0; i < sharedReceptacleBuffer.getContainerSize(); i++) {
            if (remainder.isEmpty()) break;
            ItemStack slot = sharedReceptacleBuffer.getItem(i);

            if (slot.isEmpty()) {
                sharedReceptacleBuffer.setItem(i, remainder.copy());
                remainder.setCount(0);
                break;
            } else if (ItemStack.isSameItemSameComponents(slot, remainder)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                int transfer = Math.min(space, remainder.getCount());
                if (transfer > 0) {
                    slot.grow(transfer);
                    remainder.shrink(transfer);
                }
            }
        }
        return remainder;
    }

    private void processProcessing(Level level, BlockPos pos, BlockState state) {
        boolean stateChanged = false;
        int maxMana = getMaxMana();
        int operations = this.coreCount;

        for (int i = 0; i < operations; i++) {
            int activeTypes = getActiveEssenceTypes();
            if (activeTypes == 0) {
                if (i == 0) this.isActive = false;
                break;
            }

            int generated = Math.round(EntropicaConfig.MANA_PER_ESSENCE.get() * (1.0f + (0.20f * (activeTypes - 1))));

            if (this.currentMode == BurnMode.REGULAR || this.currentMode == BurnMode.ELEMENTAL) {
                boolean performedOp = false;
                for (EssenceType type : EssenceType.values()) {
                    int essenceAmount = this.essencePool.getOrDefault(type, 0);
                    if (essenceAmount > 0) {
                        EssenceType targetType = (this.currentMode == BurnMode.REGULAR) ? EssenceType.REGULAR : type;

                        if (getTotalMana() + generated <= maxMana) {
                            this.essencePool.put(type, essenceAmount - 1);
                            this.manaPool.put(targetType, this.manaPool.getOrDefault(targetType, 0) + generated);
                            stateChanged = true;
                            performedOp = true;
                        } else {
                            this.isActive = false;
                        }
                    }
                }
                if (!performedOp) break;
            } else if (this.currentMode == BurnMode.FUSION) {
                if (activeTypes < 2) {
                    this.currentMode = BurnMode.ELEMENTAL;
                    stateChanged = true;
                    break;
                }
                boolean fusionSuccess = false;
                if (level instanceof ServerLevel serverLevel && ModRecipes.FUSION_TYPE.isBound()) {
                    Collection<RecipeHolder<FusionRecipe>> recipes = serverLevel.recipeAccess().recipeMap().byType(ModRecipes.FUSION_TYPE.get());
                    for (RecipeHolder<FusionRecipe> holder : recipes) {
                        FusionRecipe recipe = holder.value();
                        if (recipe.requiresCatalyst() && !hasCatalystInReceptacle("prismatic")) continue;
                        boolean hasIng = recipe.inputs().entrySet().stream().allMatch(e -> this.essencePool.getOrDefault(e.getKey(), 0) >= e.getValue());
                        if (hasIng) {
                            EssenceType out = (level.random.nextFloat() < recipe.chance()) ? recipe.success() : recipe.failure();
                            if (out == null) out = EssenceType.REGULAR;

                            if (getTotalMana() + recipe.yield() <= maxMana) {
                                recipe.inputs().forEach((t, amt) -> this.essencePool.put(t, this.essencePool.get(t) - amt));
                                this.manaPool.put(out, this.manaPool.getOrDefault(out, 0) + recipe.yield());
                                stateChanged = true;
                                fusionSuccess = true;
                                break;
                            } else {
                                this.isActive = false;
                            }
                        }
                    }
                }
                if (!fusionSuccess) break;
            }
        }
        if (stateChanged) {
            this.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    private boolean checkMultiblock() {
        if (this.level == null) return false;
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toVisit = new LinkedList<>();
        toVisit.add(this.worldPosition);
        visited.add(this.worldPosition);

        List<BlockPos> foundCores = new ArrayList<>();
        List<BlockPos> foundHatches = new ArrayList<>();
        List<BlockPos> foundPlumes = new ArrayList<>();
        List<BlockPos> foundReceptacles = new ArrayList<>();
        int catalysts = 0;

        while (!toVisit.isEmpty()) {
            BlockPos current = toVisit.poll();
            Block block = level.getBlockState(current).getBlock();
            if (block == ModBlocks.ENTROPIC_CORE.get()) foundCores.add(current);
            else if (block == ModBlocks.MANA_PLUME.get()) foundPlumes.add(current);
            else if (block == ModBlocks.FURNACE_HATCH.get()) foundHatches.add(current);
            else if (block == ModBlocks.ESSENCE_RECEPTACLE.get()) foundReceptacles.add(current);
            else if (block == ModBlocks.CATALYST_RECEPTACLE.get()) catalysts++;

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor) && isFurnaceComponent(level.getBlockState(neighbor).getBlock())) {
                    visited.add(neighbor);
                    toVisit.add(neighbor);
                }
            }
            if (visited.size() > 128) break;
        }

        foundCores.sort(Comparator.comparingLong(BlockPos::asLong));
        BlockPos newMasterPos = foundCores.isEmpty() ? null : foundCores.get(0);

        String error = "Structure is valid.";
        boolean formed = true;
        if (foundCores.isEmpty()) {
            error = "No Entropic Cores found.";
            formed = false;
        } else if (foundPlumes.size() < foundCores.size()) {
            error = "Missing Mana Plumes (Need " + foundCores.size() + ")";
            formed = false;
        } else if (foundHatches.size() < foundCores.size()) {
            error = "Missing Furnace Hatches";
            formed = false;
        } else if (foundReceptacles.size() < foundCores.size()) {
            error = "Missing Essence Receptacles";
            formed = false;
        }

        for (BlockPos corePos : foundCores) {
            if (level.getBlockEntity(corePos) instanceof EntropicCoreBlockEntity core) {
                core.masterPos = newMasterPos;
                core.isFormed = formed;
                core.lastValidationError = error;
                if (core.isMaster()) {
                    core.coreCount = foundCores.size();
                    core.receptacleCount = foundReceptacles.size();
                    core.catalystCount = catalysts;
                    if (core.renderHatchPos == null && !foundHatches.isEmpty()) core.renderHatchPos = foundHatches.get(0);

                    // --- NEW: Plume Cache Storage ---
                    core.connectedPlumes.clear();
                    core.connectedPlumes.addAll(foundPlumes);
                }
                core.setChanged();
                level.sendBlockUpdated(corePos, core.getBlockState(), core.getBlockState(), 3);
            }
        }

        for (BlockPos plumePos : foundPlumes) {
            if (level.getBlockEntity(plumePos) instanceof ManaPlumeBlockEntity plumeBE) {
                plumeBE.setMasterPos(newMasterPos);
                plumeBE.setChanged();
            }
        }

        for (BlockPos recPos : foundReceptacles) {
            if (level.getBlockEntity(recPos) instanceof EssenceReceptacleBlockEntity recBE) {
                recBE.setMasterPos(newMasterPos);
                recBE.setChanged();
            }
        }

        return formed;
    }

    private boolean isFurnaceComponent(Block block) {
        return block == ModBlocks.ENTROPIC_CORE.get() || block == ModBlocks.MANA_PLUME.get() ||
                block == ModBlocks.FURNACE_HATCH.get() || block == ModBlocks.ESSENCE_RECEPTACLE.get() ||
                block == ModBlocks.CATALYST_RECEPTACLE.get() || block == ModBlocks.ARCANE_BRICK.get() ||
                block == ModBlocks.ARCANE_PLATING.get() || block == ModBlocks.MANA_READOUT.get() ||
                block == ModBlocks.ESSENCE_READOUT.get() || block == ModBlocks.MANA_FILTER.get();
    }

    public int extractMana(EssenceType type, int maxExtract) {
        EntropicCoreBlockEntity master = getMaster();
        int current = master.manaPool.getOrDefault(type, 0);
        int ext = Math.min(current, maxExtract);
        if (ext > 0) {
            master.manaPool.put(type, current - ext);
            master.setChanged();
            if (level != null && !level.isClientSide()) level.sendBlockUpdated(master.worldPosition, master.getBlockState(), master.getBlockState(), 3);
        }
        return ext;
    }

    public boolean interactWithAmpoule(Player player, ItemStack handStack, InteractionHand hand) {
        EntropicCoreBlockEntity master = getMaster();
        if (master == null || !master.isFormed()) return false;

        if (handStack.getItem() == ModItems.SMALL_AMPOULE_BASE.get() ||
                handStack.getItem() == ModItems.MEDIUM_AMPOULE_BASE.get() ||
                handStack.getItem() == ModItems.LARGE_AMPOULE_BASE.get()) {

            int capacity = (handStack.getItem() == ModItems.SMALL_AMPOULE_BASE.get()) ? 4 :
                    (handStack.getItem() == ModItems.MEDIUM_AMPOULE_BASE.get()) ? 16 : 64;

            ManaAmpouleItem targetFilledItem = (ManaAmpouleItem) (
                    (capacity == 4) ? ModItems.SMALL_MANA_AMPOULE.get() :
                            (capacity == 16) ? ModItems.MEDIUM_MANA_AMPOULE.get() : ModItems.LARGE_MANA_AMPOULE.get());

            EssenceType bestType = null;
            int maxFound = -1;

            for (Map.Entry<EssenceType, Integer> entry : master.manaPool.entrySet()) {
                if (entry.getValue() >= capacity && entry.getValue() > maxFound) {
                    maxFound = entry.getValue();
                    bestType = entry.getKey();
                }
            }

            if (bestType != null) {
                master.manaPool.put(bestType, master.manaPool.get(bestType) - capacity);
                master.setChanged();

                ItemStack filled = new ItemStack(targetFilledItem);
                ManaAmpouleItem.setEssenceType(filled, bestType);

                handStack.shrink(1);
                if (handStack.isEmpty()) {
                    player.setItemInHand(hand, filled);
                } else if (!player.getInventory().add(filled)) {
                    player.drop(filled, false);
                }

                player.displayClientMessage(Component.literal("§aFilled ampoule with " + capacity + " " + bestType.getFormattedName() + " mana."), true);
                if (level != null && !level.isClientSide()) level.sendBlockUpdated(master.worldPosition, master.getBlockState(), master.getBlockState(), 3);
                return true;
            } else {
                player.displayClientMessage(Component.literal("§cNot enough mana of a single type to fill ampoule size (" + capacity + ")."), true);
                return true;
            }
        }

        if (handStack.getItem() instanceof ManaAmpouleItem ampoule) {
            EssenceType type = ManaAmpouleItem.getEssenceType(handStack);
            if (type == null) return false;

            int capacity = ampoule.getCapacity();
            if (master.getTotalMana() + capacity <= master.getMaxMana()) {
                master.manaPool.put(type, master.manaPool.getOrDefault(type, 0) + capacity);
                master.setChanged();

                ItemStack empty = new ItemStack(ampoule.getBaseItem());
                handStack.shrink(1);
                if (handStack.isEmpty()) {
                    player.setItemInHand(hand, empty);
                } else if (!player.getInventory().add(empty)) {
                    player.drop(empty, false);
                }

                player.displayClientMessage(Component.literal("§aInserted " + capacity + " " + type.getFormattedName() + " mana."), true);
                if (level != null && !level.isClientSide()) level.sendBlockUpdated(master.worldPosition, master.getBlockState(), master.getBlockState(), 3);
                return true;
            } else {
                player.displayClientMessage(Component.literal("§cCore mana capacity is full."), true);
                return true;
            }
        }

        return false;
    }

    public void toggleFurnace(Player player, BlockPos interactPos) {
        EntropicCoreBlockEntity master = getMaster();
        if (!master.isFormed) {
            player.displayClientMessage(Component.literal("§c" + master.lastValidationError), true);
            return;
        }
        updateLastInteractedHatch(interactPos);

        if (!master.isActive) {
            if (master.getTotalMana() >= master.getMaxMana()) {
                player.displayClientMessage(Component.literal("§cCannot activate: Mana capacity is full."), true);
                return;
            }
            if (master.getTotalEssence() < 10) {
                player.displayClientMessage(Component.literal("§cInsufficient Essence to start."), true);
                return;
            }
            master.isActive = true;
            player.displayClientMessage(Component.literal("§aCore Activated"), true);
        } else {
            master.isActive = false;
            player.displayClientMessage(Component.literal("§7Core Deactivated"), true);
        }

        master.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(master.worldPosition, master.getBlockState(), master.getBlockState(), 3);
        }
    }

    public void toggleBurnMode(Player player) {
        EntropicCoreBlockEntity master = getMaster();
        if (!master.isFormed) {
            player.displayClientMessage(Component.literal("§c" + master.lastValidationError), true);
            return;
        }
        master.currentMode = master.currentMode.next();
        player.displayClientMessage(Component.literal("Mode: " + master.currentMode.getFormattedName()), true);
        master.setChanged();
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(master.worldPosition, master.getBlockState(), master.getBlockState(), 3);
    }

    @Nullable public EssenceType getEssenceTypeFromItem(ItemStack stack) {
        String name = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        for (EssenceType type : EssenceType.values()) if (name.contains(type.name().toLowerCase())) return type;
        return null;
    }

    private int getEssenceValue(ItemStack stack) {
        String name = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (name.contains("strong") || name.contains("large")) return 16;
        if (name.contains("average") || name.contains("medium")) return 4;
        return 1;
    }

    public boolean isActive() { return getMaster().isActive; }
    public int getMaxMana() { return EntropicaConfig.ENTROPIC_CORE_MAX_MANA.get() * getMaster().coreCount; }
    public int getMaxEssencePerType() { return EntropicaConfig.RECEPTACLE_MAX_ESSENCE.get() * getMaster().receptacleCount; }

    public int getTotalMana() { return getMaster().manaPool.values().stream().mapToInt(Integer::intValue).sum(); }

    private int getTotalEssence() { return getEssencePool().values().stream().mapToInt(Integer::intValue).sum(); }
    private int getActiveEssenceTypes() { return (int) getEssencePool().values().stream().filter(v -> v > 0).count(); }
    public Map<EssenceType, Integer> getManaPool() { return getMaster().manaPool; }
    public Map<EssenceType, Integer> getEssencePool() { return getMaster().essencePool; }
    public boolean isFormed() { return getMaster().isFormed; }
    public String getLastValidationError() { return getMaster().lastValidationError; }
    public BlockPos getActiveRenderPos() { return getMaster().renderHatchPos; }
    public BurnMode getCurrentMode() { return getMaster().currentMode; }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (masterPos != null) output.putLong("MasterPos", masterPos.asLong());
        if (renderHatchPos != null) output.putLong("RenderHatchPos", renderHatchPos.asLong());
        output.putBoolean("CoreActive", this.isActive);
        output.putBoolean("CoreFormed", this.isFormed);
        output.putInt("BurnMode", this.currentMode.ordinal());
        output.putInt("CoreCount", this.coreCount);
        output.putInt("ReceptacleCount", this.receptacleCount);
        output.putInt("CatalystCount", this.catalystCount);
        output.putString("LastError", this.lastValidationError);
        for (EssenceType type : EssenceType.values()) {
            output.putInt(type.name() + "_Mana", this.manaPool.getOrDefault(type, 0));
            output.putInt(type.name() + "_Essence", this.essencePool.getOrDefault(type, 0));
        }
        for (int i = 0; i < sharedReceptacleBuffer.getContainerSize(); i++) {
            ItemStack stack = sharedReceptacleBuffer.getItem(i);
            output.putString("BufItem_" + i, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            output.putInt("BufCount_" + i, stack.getCount());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getLong("MasterPos").ifPresent(l -> this.masterPos = BlockPos.of(l));
        input.getLong("RenderHatchPos").ifPresent(l -> this.renderHatchPos = BlockPos.of(l));
        this.isActive = input.getBooleanOr("CoreActive", false);
        this.isFormed = input.getBooleanOr("CoreFormed", false);
        this.currentMode = BurnMode.values()[input.getIntOr("BurnMode", 0) % BurnMode.values().length];
        this.coreCount = input.getIntOr("CoreCount", 0);
        this.receptacleCount = input.getIntOr("ReceptacleCount", 0);
        this.catalystCount = input.getIntOr("CatalystCount", 0);
        this.lastValidationError = input.getStringOr("LastError", "No error.");
        for (EssenceType type : EssenceType.values()) {
            this.manaPool.put(type, input.getIntOr(type.name() + "_Mana", 0));
            this.essencePool.put(type, input.getIntOr(type.name() + "_Essence", 0));
        }
        for (int i = 0; i < sharedReceptacleBuffer.getContainerSize(); i++) {
            String itemStr = input.getStringOr("BufItem_" + i, "minecraft:air");
            int count = input.getIntOr("BufCount_" + i, 0);
            if (!itemStr.equals("minecraft:air") && count > 0) {
                net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(itemStr);
                if (rl != null) {
                    final int index = i;
                    BuiltInRegistries.ITEM.getOptional(rl).ifPresent(holder ->
                            sharedReceptacleBuffer.setItem(index, new ItemStack(holder, count))
                    );
                }
            }
        }
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    private boolean hasCatalystInReceptacle(String namePart) {
        if (this.level == null) return false;

        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockEntity be = level.getBlockEntity(this.worldPosition.offset(x, y, z));
                    if (be instanceof CatalystReceptacleBlockEntity cat) {
                        if (cat.getCatalyst().getItem().toString().contains(namePart)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}