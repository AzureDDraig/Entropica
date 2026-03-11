package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.fumes.IFumeHandler;
import ddraig.net.entropica.api.fumes.VisFumeStack;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.item.VisFumeAmpouleItem;
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

public class EntropicCoreBlockEntity extends BlockEntity implements IFumeHandler {

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

    // Overload System Variables
    private boolean isOverloaded = false;
    private int overloadTicks = 0;
    private int maxOverloadTicks = 2400; // Default 120 seconds

    private BlockPos masterPos = null;
    private BlockPos renderHatchPos = null;
    private String lastValidationError = "Structure has not been validated yet.";

    private final List<BlockPos> connectedPlumes = new ArrayList<>();
    private final List<BlockPos> connectedCores = new ArrayList<>();

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

    // Accessors for the renderer to read Overload State from the master core
    public boolean isOverloaded() { return getMaster().isOverloaded; }
    public int getOverloadTicks() { return getMaster().overloadTicks; }
    public int getMaxOverloadTicks() { return getMaster().maxOverloadTicks; }

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

        if (master.isActive) {
            if (master.getTotalEssence() < 10 || (!EntropicaConfig.ENABLE_CORE_OVERLOAD.get() && master.getWeightedTotalMana() >= master.getMaxMana())) {
                master.isActive = false;
            }
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
        return Math.max(0, (int) Math.floor(((double) master.getWeightedTotalMana() / maxMana) * 15.0));
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

                // --- OVERLOAD LOGIC ---
                if (EntropicaConfig.ENABLE_CORE_OVERLOAD.get()) {
                    if (this.getWeightedTotalMana() >= this.getMaxMana()) {
                        if (!this.isOverloaded) {
                            this.isOverloaded = true;
                            this.recalculateOverloadTime();
                        }

                        this.overloadTicks++;

                        if (this.overloadTicks >= this.maxOverloadTicks) {
                            this.triggerExplosion();
                        }

                        // Sync to client frequently for the rendering UI timer
                        if (this.overloadTicks % 10 == 0) {
                            this.setChanged();
                            level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                        }
                    } else if (this.isOverloaded) {
                        this.isOverloaded = false;
                        this.overloadTicks = 0;
                        this.setChanged();
                        level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                    }
                } else {
                    // Safe behavior fallback: shut off immediately if full, clear overload states if toggled off in config
                    if (this.isOverloaded) {
                        this.isOverloaded = false;
                        this.overloadTicks = 0;
                        this.setChanged();
                        level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                    }
                    if (this.getWeightedTotalMana() >= this.getMaxMana()) {
                        this.isActive = false;
                        this.setChanged();
                        level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                    }
                }
                // ----------------------

                // Proceed with processing only if still active
                if (this.isActive) {
                    this.tickCounter++;
                    if (this.tickCounter >= EntropicaConfig.CORE_PROCESS_TICK_RATE.get()) {
                        this.tickCounter = 0;
                        processProcessing(level, pos, state);
                    }
                }
            } else if (this.isOverloaded) {
                // If the player deactivates the core in time, reset the overload
                this.isOverloaded = false;
                this.overloadTicks = 0;
                this.setChanged();
                level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }

            if (level.getGameTime() % EntropicaConfig.VIS_FUME_TICK_RATE.get() == 0) {
                pushManaToPipes(level);
            }
        }
    }

    private void recalculateOverloadTime() {
        int baseSeconds = 120 * Math.max(1, this.coreCount);

        int activeManaTypes = (int) this.manaPool.values().stream().filter(v -> v > 0).count();
        int activeEssenceTypes = (int) this.essencePool.values().stream().filter(v -> v > 0).count();

        // Halves per extra type above 1 (Mana) and 2 (Essence)
        int extraManaTypes = Math.max(0, activeManaTypes - 1);
        int extraEssenceTypes = Math.max(0, activeEssenceTypes - 2);

        double manaDivisor = Math.pow(2, extraManaTypes);
        double essenceDivisor = Math.pow(2, extraEssenceTypes);
        double totalDivisor = manaDivisor * essenceDivisor;

        int explosionSeconds = (int) Math.ceil(baseSeconds / totalDivisor);
        this.maxOverloadTicks = explosionSeconds * 20; // Convert back to ticks
    }

    private void triggerExplosion() {
        if (this.level instanceof ServerLevel serverLevel) {
            // Base radius 4, caps at 8 based on how many cores exist in the multiblock
            float explosionRadius = Math.min(8.0f, 4.0f + (this.coreCount - 1));

            for (BlockPos corePos : this.connectedCores) {
                serverLevel.explode(
                        null,
                        corePos.getX() + 0.5D, corePos.getY() + 0.5D, corePos.getZ() + 0.5D,
                        explosionRadius,
                        Level.ExplosionInteraction.BLOCK
                );
            }
        }

        // Destroy 90% of the stored essence in the UI
        for (Map.Entry<EssenceType, Integer> entry : this.essencePool.entrySet()) {
            int current = entry.getValue();
            int remaining = (int) Math.floor(current * 0.10f);
            entry.setValue(remaining);
        }

        // Clear the mana entirely so it doesn't instantly re-blow up if rebuilt
        for (Map.Entry<EssenceType, Integer> entry : this.manaPool.entrySet()) {
            entry.setValue(0);
        }

        this.isOverloaded = false;
        this.overloadTicks = 0;
        this.isActive = false;

        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    private void pushManaToPipes(Level level) {
        EssenceType typeToPush = null;
        int maxMana = 0;

        // Find the most abundant mana type
        for (Map.Entry<EssenceType, Integer> entry : this.manaPool.entrySet()) {
            if (entry.getValue() > maxMana) {
                maxMana = entry.getValue();
                typeToPush = entry.getKey();
            }
        }

        if (typeToPush == null || maxMana <= 0) return;

        int amountLeftToPush = Math.min(maxMana, EntropicaConfig.VIS_FUME_TRANSFER_RATE.get());

        // Gather all UNIQUE fume handlers attached to the top of all plumes
        Set<IFumeHandler> handlersSet = new HashSet<>();
        for (BlockPos plumePos : this.connectedPlumes) {
            if (level.getBlockState(plumePos).is(ModBlocks.MANA_PLUME.get())) {
                BlockPos targetPos = plumePos.above(); // Only check directly above
                BlockEntity targetBE = level.getBlockEntity(targetPos);

                if (targetBE instanceof IFumeHandler handler && !(targetBE instanceof ManaPlumeBlockEntity) && !(targetBE instanceof EntropicCoreBlockEntity)) {
                    handlersSet.add(handler);
                }
            }
        }

        List<IFumeHandler> validHandlers = new ArrayList<>(handlersSet);

        // Iteratively distribute the mana evenly across all valid handlers
        boolean pushedAny;
        do {
            pushedAny = false;
            if (validHandlers.isEmpty() || amountLeftToPush <= 0) break;

            int splitAmount = amountLeftToPush / validHandlers.size();
            int remainder = amountLeftToPush % validHandlers.size();

            Iterator<IFumeHandler> it = validHandlers.iterator();
            while (it.hasNext()) {
                IFumeHandler handler = it.next();

                int amountToTry = splitAmount + (remainder > 0 ? 1 : 0);
                if (amountToTry == 0) amountToTry = 1;

                amountToTry = Math.min(amountToTry, amountLeftToPush);

                VisFumeStack pushStack = new VisFumeStack(typeToPush, amountToTry);
                int accepted = handler.fill(pushStack, false);

                if (accepted > 0) {
                    this.extractMana(typeToPush, accepted);
                    amountLeftToPush -= accepted;
                    if (remainder > 0) remainder--;
                    pushedAny = true;
                } else {
                    it.remove();
                }

                if (amountLeftToPush <= 0) break;
            }
        } while (pushedAny && amountLeftToPush > 0);
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
        boolean overloadEnabled = EntropicaConfig.ENABLE_CORE_OVERLOAD.get();

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

                        int weight = getEssenceWeight(targetType);
                        int spaceWeighted = maxMana - getWeightedTotalMana();
                        int spaceRaw = Math.max(0, spaceWeighted / weight);

                        if (overloadEnabled || spaceRaw >= generated) {
                            this.essencePool.put(type, essenceAmount - 1);
                            int actualAdded = Math.min(generated, spaceRaw);
                            this.manaPool.put(targetType, this.manaPool.getOrDefault(targetType, 0) + actualAdded);
                            stateChanged = true;
                            performedOp = true;
                        } else {
                            this.isActive = false;
                            stateChanged = true;
                            break;
                        }
                    }
                }
                if (!performedOp || !this.isActive) break;
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

                            int weight = getEssenceWeight(out);
                            int spaceWeighted = maxMana - getWeightedTotalMana();
                            int spaceRaw = Math.max(0, spaceWeighted / weight);

                            if (overloadEnabled || spaceRaw >= recipe.yield()) {
                                recipe.inputs().forEach((t, amt) -> this.essencePool.put(t, this.essencePool.get(t) - amt));
                                int actualAdded = Math.min(recipe.yield(), spaceRaw);
                                this.manaPool.put(out, this.manaPool.getOrDefault(out, 0) + actualAdded);
                                stateChanged = true;
                                fusionSuccess = true;
                                break;
                            } else {
                                this.isActive = false;
                                stateChanged = true;
                                break;
                            }
                        }
                    }
                }
                if (!fusionSuccess || !this.isActive) break;
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
            if (visited.size() > 256) break;
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
        } else if (foundHatches.isEmpty()) {
            error = "Missing Furnace Hatch (At least 1 required)";
            formed = false;
        } else if (foundReceptacles.isEmpty()) {
            error = "Missing Essence Receptacle (At least 1 required)";
            formed = false;
        }

        if (formed) {
            coreValidationLoop:
            for (BlockPos corePos : foundCores) {
                if (!isFurnaceComponent(level.getBlockState(corePos.above()).getBlock())) {
                    error = "Missing valid furnace component directly above the Entropic Core.";
                    formed = false;
                    break;
                }

                Direction[] surroundingDirs = {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
                for (Direction dir : surroundingDirs) {
                    Block neighbor = level.getBlockState(corePos.relative(dir)).getBlock();
                    if (neighbor != ModBlocks.ARCANITE_PLATING.get() &&
                            neighbor != ModBlocks.ARCANE_BRICK.get() &&
                            neighbor != ModBlocks.FURNACE_HATCH.get()) {
                        error = "Core must be directly surrounded by Arcane Plating, Bricks, or Hatches.";
                        formed = false;
                        break coreValidationLoop;
                    }
                }

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (level.getBlockState(corePos.offset(dx, 0, dz)).isAir()) {
                            error = "Layer 2 (Core layer) cannot contain air directly around the core.";
                            formed = false;
                            break coreValidationLoop;
                        }
                        if (level.getBlockState(corePos.offset(dx, -1, dz)).isAir()) {
                            error = "Layer 1 (Bottom layer) cannot contain air directly below the core.";
                            formed = false;
                            break coreValidationLoop;
                        }
                    }
                }
            }
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

                    core.connectedPlumes.clear();
                    core.connectedPlumes.addAll(foundPlumes);

                    core.connectedCores.clear();
                    core.connectedCores.addAll(foundCores);
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
                block == ModBlocks.ARCANITE_PLATING.get() || block == ModBlocks.VIS_READOUT.get() ||
                block == ModBlocks.ESSENCE_READOUT.get() || block == ModBlocks.MANA_FILTER.get();
    }

    // ==========================================
    // ESSENCE WEIGHTING & COMPRESSION MATH
    // ==========================================

    public static int getEssenceWeight(EssenceType type) {
        String name = type.name();
        if (Set.of("EMPYREAN", "CATACLYSM", "TERMINUS", "ESCHATON", "APOTHEOSIS", "ENTROPICA").contains(name)) return 32;
        if (Set.of("AETHER", "ENTROPIC", "CELESTIAL", "PRISMATIC").contains(name)) return 16;
        if (Set.of("PYRE", "PENUMBRA", "RIME", "SPRING", "STATIC", "SYLVAN", "MIASMA", "GENESIS", "OBLIVION").contains(name)) return 8;
        if (Set.of("LIGHTNING", "STORM", "DUST", "BLOOD", "ASTRAL", "OASIS", "MIRAGE", "AURA", "AMBER", "WRAITH", "BARROW").contains(name)) return 4;
        if (Set.of("MAGMA", "GLACIAL", "OVERGROWTH", "VITAE", "ECLIPSE", "BLIGHT", "SOULFIRE", "VAPOR", "NULL_R", "NULL_U", "SPORE", "TAIGA", "DAWN", "ABYSS", "AEGIS", "AURORA").contains(name)) return 2;

        // Default Tier 1 (Base Elements + Regular)
        return 1;
    }

    public int getWeightedTotalMana() {
        return getMaster().manaPool.entrySet().stream()
                .mapToInt(e -> e.getValue() * getEssenceWeight(e.getKey()))
                .sum();
    }


    // ==========================================
    // FUME HANDLER IMPLEMENTATION
    // ==========================================

    @Override
    public int fill(VisFumeStack resource, boolean simulate) {
        EntropicCoreBlockEntity master = getMaster();
        if (!master.isFormed || resource.isEmpty()) return 0;

        int weight = getEssenceWeight(resource.getType());
        int currentWeighted = master.getWeightedTotalMana();
        int spaceWeighted = master.getMaxMana() - currentWeighted;

        if (spaceWeighted <= 0) return 0;

        int spaceRaw = spaceWeighted / weight;
        int amountToFill = Math.min(resource.getAmount(), spaceRaw);

        if (!simulate && amountToFill > 0) {
            master.manaPool.put(resource.getType(), master.manaPool.getOrDefault(resource.getType(), 0) + amountToFill);
            master.setChanged();
            if (master.level != null && !master.level.isClientSide()) {
                master.level.sendBlockUpdated(master.worldPosition, master.getBlockState(), master.getBlockState(), 3);
            }
        }
        return amountToFill;
    }

    @Override
    public VisFumeStack drain(int maxDrain, boolean simulate) {
        EntropicCoreBlockEntity master = getMaster();
        if (!master.isFormed || maxDrain <= 0) return VisFumeStack.EMPTY;

        EssenceType bestType = null;
        int maxFound = 0;

        for (Map.Entry<EssenceType, Integer> entry : master.manaPool.entrySet()) {
            if (entry.getValue() > maxFound) {
                maxFound = entry.getValue();
                bestType = entry.getKey();
            }
        }

        if (bestType == null || maxFound <= 0) return VisFumeStack.EMPTY;

        int amountToDrain = Math.min(maxFound, maxDrain);

        if (!simulate && amountToDrain > 0) {
            master.manaPool.put(bestType, maxFound - amountToDrain);
            master.setChanged();
            if (master.level != null && !master.level.isClientSide()) {
                master.level.sendBlockUpdated(master.worldPosition, master.getBlockState(), master.getBlockState(), 3);
            }
        }

        return new VisFumeStack(bestType, amountToDrain);
    }

    @Override
    public VisFumeStack getFumeInTank() {
        EntropicCoreBlockEntity master = getMaster();
        EssenceType bestType = null;
        int maxFound = 0;

        for (Map.Entry<EssenceType, Integer> entry : master.manaPool.entrySet()) {
            if (entry.getValue() > maxFound) {
                maxFound = entry.getValue();
                bestType = entry.getKey();
            }
        }

        if (bestType == null || maxFound <= 0) return VisFumeStack.EMPTY;
        return new VisFumeStack(bestType, maxFound);
    }

    @Override
    public int getCapacity() {
        return getMaster().getMaxMana();
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

            VisFumeAmpouleItem targetFilledItem = (VisFumeAmpouleItem) (
                    (capacity == 4) ? ModItems.SMALL_VIS_FUME_AMPOULE.get() :
                            (capacity == 16) ? ModItems.MEDIUM_VIS_FUME_AMPOULE.get() : ModItems.LARGE_VIS_FUME_AMPOULE.get());

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
                VisFumeAmpouleItem.setEssenceType(filled, bestType);

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

        if (handStack.getItem() instanceof VisFumeAmpouleItem ampoule) {
            EssenceType type = VisFumeAmpouleItem.getEssenceType(handStack);
            if (type == null) return false;

            int capacity = ampoule.getCapacity();
            int weight = getEssenceWeight(type);

            if (master.getWeightedTotalMana() + (capacity * weight) <= master.getMaxMana()) {
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
                player.displayClientMessage(Component.literal("§cCore mana capacity is full. Too much density."), true);
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
            // Respect Config for toggling activation
            if (!EntropicaConfig.ENABLE_CORE_OVERLOAD.get() && master.getWeightedTotalMana() >= master.getMaxMana()) {
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

    // ==========================================
    // NBT DATA EXTRACTION (Updated for generic items)
    // ==========================================

    @Nullable public EssenceType getEssenceTypeFromItem(ItemStack stack) {
        if (stack.getItem() instanceof ddraig.net.entropica.item.EssenceItem) {
            return ddraig.net.entropica.item.EssenceItem.getEssenceType(stack);
        } else if (stack.getItem() instanceof ddraig.net.entropica.item.EssenceAmpouleItem) {
            return ddraig.net.entropica.item.EssenceAmpouleItem.getEssenceType(stack);
        }
        return null;
    }

    private int getEssenceValue(ItemStack stack) {
        if (stack.getItem() instanceof ddraig.net.entropica.item.EssenceItem essenceItem) {
            return essenceItem.getTier() == 3 ? 16 : (essenceItem.getTier() == 2 ? 4 : 1);
        } else if (stack.getItem() instanceof ddraig.net.entropica.item.EssenceAmpouleItem ampouleItem) {
            return ampouleItem.getTier() == 3 ? 16 : (ampouleItem.getTier() == 2 ? 4 : 1);
        }
        return 0;
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

        // Save Overload States
        output.putBoolean("IsOverloaded", this.isOverloaded);
        output.putInt("OverloadTicks", this.overloadTicks);
        output.putInt("MaxOverloadTicks", this.maxOverloadTicks);

        // Save Connected Cores for explosions
        output.putInt("ConnectedCoreCount", this.connectedCores.size());
        for (int i = 0; i < this.connectedCores.size(); i++) {
            output.putLong("ConnectedCore_" + i, this.connectedCores.get(i).asLong());
        }

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

        this.isOverloaded = input.getBooleanOr("IsOverloaded", false);
        this.overloadTicks = input.getIntOr("OverloadTicks", 0);
        this.maxOverloadTicks = input.getIntOr("MaxOverloadTicks", 2400);

        this.connectedCores.clear();
        int coreCt = input.getIntOr("ConnectedCoreCount", 0);
        for (int i = 0; i < coreCt; i++) {
            long cPos = input.getLongOr("ConnectedCore_" + i, -1L);
            if (cPos != -1L) this.connectedCores.add(BlockPos.of(cPos));
        }

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
            } else {
                sharedReceptacleBuffer.setItem(i, ItemStack.EMPTY);
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