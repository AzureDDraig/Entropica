package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.ScribedChalkBlock;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.item.OrbisCellItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

import java.util.List;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.mojang.serialization.Codec;

public class ScribedChalkBlockEntity extends BlockEntity {
    private int color = 0xFFCCCCCC;
    private EssenceType activeAffinity = EssenceType.REGULAR;
    private EssenceType filterType = null;
    private ItemStack storedOrbisCell = ItemStack.EMPTY;
    private ItemStack storedRune = ItemStack.EMPTY;
    private ItemStack storedItem = ItemStack.EMPTY;
    private int scanCooldown = 0;
    private int dyeTicks = 0;
    private boolean isInActiveCircle = false;
    private int essenceLevel = 0;
    private int propagationStrength = 0;
    private Direction facing = Direction.NORTH;

    private ItemStack activeRecipeOutput = ItemStack.EMPTY;
    private int processingProgress = 0;
    private int processingTimeTotal = 0;
    private int overclockTicks = 0;
    private boolean doubleYield = false;
    private boolean isWard = false;
    private int wardTicks = 0;
    private int activeCircleTier = 0;
    private int amplifierCount = 0;
    private int capacitorCount = 0;

    public int getAmplifierCount() { return this.amplifierCount; }
    public void setAmplifierCount(int count) { this.amplifierCount = count; this.setChanged(); }
    public int getCapacitorCount() { return this.capacitorCount; }
    public void setCapacitorCount(int count) { this.capacitorCount = count; this.setChanged(); }

    public enum Direction8 {
        NORTH("north", 0, -1),
        NORTH_EAST("north_east", 1, -1),
        EAST("east", 1, 0),
        SOUTH_EAST("south_east", 1, 1),
        SOUTH("south", 0, 1),
        SOUTH_WEST("south_west", -1, 1),
        WEST("west", -1, 0),
        NORTH_WEST("north_west", -1, -1);

        private final String name;
        private final int xOffset;
        private final int zOffset;

        Direction8(String name, int xOffset, int zOffset) {
            this.name = name;
            this.xOffset = xOffset;
            this.zOffset = zOffset;
        }

        public String getName() { return this.name; }
        public int getXOffset() { return this.xOffset; }
        public int getZOffset() { return this.zOffset; }

        public Direction8 getOpposite() {
            return switch (this) {
                case NORTH -> SOUTH;
                case NORTH_EAST -> SOUTH_WEST;
                case EAST -> WEST;
                case SOUTH_EAST -> NORTH_WEST;
                case SOUTH -> NORTH;
                case SOUTH_WEST -> NORTH_EAST;
                case WEST -> EAST;
                case NORTH_WEST -> SOUTH_EAST;
            };
        }
    }

    private final int[] connectionOverrides = new int[8];

    public int getConnectionOverride(Direction8 dir) {
        return this.connectionOverrides[dir.ordinal()];
    }

    public void setConnectionOverride(Direction8 dir, int value) {
        this.connectionOverrides[dir.ordinal()] = value;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public ScribedChalkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SCRIBED_CHALK_BE.get(), pos, state);
    }

    public boolean isInActiveCircle() {
        return this.isInActiveCircle;
    }

    public void setIsInActiveCircle(boolean inCircle) {
        if (this.isInActiveCircle != inCircle) {
            this.isInActiveCircle = inCircle;
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    public Direction getFacing() {
        return this.facing;
    }

    public void setFacing(Direction facing) {
        if (this.facing != facing) {
            this.facing = facing;
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    public int getColor() {
        if (this.activeAffinity == EssenceType.REGULAR) {
            BlockState state = getBlockState();
            if (state.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) && 
                state.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.RUNE &&
                this.filterType != null &&
                !this.storedRune.isEmpty() && 
                this.storedRune.getItem() == ddraig.net.entropica.registry.ModItems.RUNE_FEHU.get() &&
                this.level != null) {
                
                long time = this.level.getGameTime();
                if (this.filterType.isDynamic()) {
                    float speed = 0.05f;
                    float cycle = time * speed;
                    int r = (int) ((Math.sin(cycle) * 0.5f + 0.5f) * 255.0f);
                    int g = (int) ((Math.sin(cycle + 2.094f) * 0.5f + 0.5f) * 255.0f);
                    int b = (int) ((Math.sin(cycle + 4.188f) * 0.5f + 0.5f) * 255.0f);
                    return (r << 16) | (g << 8) | b;
                } else {
                    int[] rgb = this.filterType.getCurrentRGB(time);
                    return (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                }
            }
            if (state.hasProperty(ScribedChalkBlock.TIER)) {
                int tier = state.getValue(ScribedChalkBlock.TIER);
                return switch (tier) {
                    case 2 -> 0xFFD87040; // Conductive (copper)
                    case 3 -> 0xFFB040B0; // Resonant (purple)
                    case 4 -> 0xFF40B0E0; // Eidolic (cyan)
                    default -> 0xFFCCCCCC; // Dull/Normal
                };
            }
        }
        return this.color;
    }

    public EssenceType getActiveAffinity() {
        return this.activeAffinity;
    }

    public ItemStack getStoredOrbisCell() {
        return this.storedOrbisCell;
    }

    public ItemStack getStoredRune() {
        return this.storedRune;
    }

    public ItemStack getStoredItem() {
        return this.storedItem;
    }

    public int getDyeTicks() {
        return this.dyeTicks;
    }

    public void setColor(int color) {
        this.color = color;
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setActiveAffinity(EssenceType affinity) {
        if (this.activeAffinity != affinity) {
            this.dyeTicks = 0;
        }
        this.activeAffinity = affinity;
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public EssenceType getFilterType() {
        return this.filterType;
    }

    public void setFilterType(EssenceType filterType) {
        this.filterType = filterType;
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setStoredOrbisCell(ItemStack stack) {
        this.storedOrbisCell = stack.copy();
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setStoredRune(ItemStack stack) {
        this.storedRune = stack.copy();
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setStoredItem(ItemStack stack) {
        this.storedItem = stack.copy();
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public ItemStack getActiveRecipeOutput() {
        return this.activeRecipeOutput;
    }

    public void setActiveRecipeOutput(ItemStack stack) {
        this.activeRecipeOutput = stack.copy();
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getEssenceLevel() {
        return this.essenceLevel;
    }

    public void setEssenceLevel(int level) {
        this.essenceLevel = level;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public int getPropagationStrength() {
        return this.propagationStrength;
    }

    public void setPropagationStrength(int strength) {
        this.propagationStrength = strength;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public ScribedChalkBlockEntity findConnectedSource(Level level, BlockPos startPos, EssenceType affinity) {
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();
        queue.add(startPos);
        visited.add(startPos);
        
        BlockState startState = level.getBlockState(startPos);
        if (!startState.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get())) {
            return null;
        }
        boolean circuit = startState.getValue(ScribedChalkBlock.CIRCUIT);
        
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockEntity be = level.getBlockEntity(current);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                BlockState state = chalkBE.getBlockState();
                if (state.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.SOURCE &&
                    chalkBE.getActiveAffinity() == affinity &&
                    (chalkBE.getEssenceLevel() > 0 || !chalkBE.getStoredOrbisCell().isEmpty())) {
                    return chalkBE;
                }
                
                // Scan neighbors
                for (Direction8 dir8 : Direction8.values()) {
                    if (chalkBE.connectsToNeighbor8(level, current, dir8, circuit)) {
                        BlockPos neighbor = current.offset(dir8.getXOffset(), 0, dir8.getZOffset());
                        if (visited.add(neighbor)) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean connectsToNeighbor8(Level level, BlockPos pos, Direction8 dir8, boolean circuit) {
        int override = getConnectionOverride(dir8);
        if (override == 1) return true;
        if (override == 2) return false;

        BlockState state = level.getBlockState(pos);
        if (!state.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get())) {
            return false;
        }

        BlockPos neighborPos = pos.offset(dir8.getXOffset(), 0, dir8.getZOffset());
        BlockState neighborState = level.getBlockState(neighborPos);
        if (!neighborState.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get())) {
            return false;
        }

        Direction dir = switch (dir8) {
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case EAST -> Direction.EAST;
            case WEST -> Direction.WEST;
            default -> null;
        };

        if (dir == null) {
            // Diagonal connection
            if (state.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.DIODE) {
                return false;
            }
            ScribedChalkBlock.NodeType neighborType = neighborState.getValue(ScribedChalkBlock.NODE_TYPE);
            if (neighborType == ScribedChalkBlock.NodeType.DIODE || 
                neighborType == ScribedChalkBlock.NodeType.AND_GATE ||
                neighborType == ScribedChalkBlock.NodeType.OR_GATE ||
                neighborType == ScribedChalkBlock.NodeType.NOT_GATE) {
                return false;
            }
            return shouldConnectSmart(level, pos, neighborPos, null, circuit);
        }

        if (state.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.DIODE) {
            if (dir != this.facing && dir != this.facing.getOpposite()) {
                return false;
            }
        }

        ScribedChalkBlock.NodeType neighborType = neighborState.getValue(ScribedChalkBlock.NODE_TYPE);
        if (neighborType == ScribedChalkBlock.NodeType.DIODE) {
            BlockEntity neighborBE = level.getBlockEntity(neighborPos);
            if (neighborBE instanceof ScribedChalkBlockEntity neighborChalk) {
                if (dir.getOpposite() != neighborChalk.getFacing() && dir.getOpposite() != neighborChalk.getFacing().getOpposite()) {
                    return false;
                }
            }
        } else if (neighborType == ScribedChalkBlock.NodeType.AND_GATE ||
                   neighborType == ScribedChalkBlock.NodeType.OR_GATE ||
                   neighborType == ScribedChalkBlock.NodeType.NOT_GATE) {
            BlockEntity neighborBE = level.getBlockEntity(neighborPos);
            if (neighborBE instanceof ScribedChalkBlockEntity neighborChalk) {
                if (dir.getOpposite() != neighborChalk.getFacing()) {
                    return false;
                }
            }
        }

        return shouldConnectSmart(level, pos, neighborPos, dir, circuit);
    }

    private boolean shouldConnectSmart(Level level, BlockPos pos, BlockPos neighborPos, Direction dir, boolean circuit) {
        if (!circuit) {
            return true;
        }
        BlockState neighborState = level.getBlockState(neighborPos);
        if (!neighborState.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) || 
            neighborState.getValue(ScribedChalkBlock.CIRCUIT) != circuit) {
            return false;
        }

        // Check for parallel lines running side-by-side in the same direction
        if (dir == Direction.NORTH || dir == Direction.SOUTH) {
            boolean selfHasEast = hasNeighborInDirection(level, pos, Direction.EAST, circuit);
            boolean neighborHasEast = hasNeighborInDirection(level, neighborPos, Direction.EAST, circuit);
            if (selfHasEast && neighborHasEast) {
                return false;
            }
            boolean selfHasWest = hasNeighborInDirection(level, pos, Direction.WEST, circuit);
            boolean neighborHasWest = hasNeighborInDirection(level, neighborPos, Direction.WEST, circuit);
            if (selfHasWest && neighborHasWest) {
                return false;
            }
        } else if (dir == Direction.EAST || dir == Direction.WEST) {
            boolean selfHasNorth = hasNeighborInDirection(level, pos, Direction.NORTH, circuit);
            boolean neighborHasNorth = hasNeighborInDirection(level, neighborPos, Direction.NORTH, circuit);
            if (selfHasNorth && neighborHasNorth) {
                return false;
            }
            boolean selfHasSouth = hasNeighborInDirection(level, pos, Direction.SOUTH, circuit);
            boolean neighborHasSouth = hasNeighborInDirection(level, neighborPos, Direction.SOUTH, circuit);
            if (selfHasSouth && neighborHasSouth) {
                return false;
            }
        }

        return true;
    }

    private boolean hasNeighborInDirection(Level level, BlockPos pos, Direction dir, boolean circuit) {
        BlockState state = level.getBlockState(pos.relative(dir));
        return state.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) && state.getValue(ScribedChalkBlock.CIRCUIT) == circuit;
    }

    public int getDefaultColor(BlockState state) {
        if (state.hasProperty(ScribedChalkBlock.TIER)) {
            int tier = state.getValue(ScribedChalkBlock.TIER);
            return switch (tier) {
                case 2 -> 0xFFD87040;
                case 3 -> 0xFFB040B0;
                case 4 -> 0xFF40B0E0;
                default -> 0xFFCCCCCC;
            };
        }
        return 0xFFCCCCCC;
    }

    private EssenceType getEssenceTypeForBiome(Level level, BlockPos pos) {
        Holder<Biome> biomeHolder = level.getBiome(pos);
        if (biomeHolder.isBound()) {
            ResourceLocation biomeKey = biomeHolder.unwrapKey().map(net.minecraft.resources.ResourceKey::location).orElse(null);
            if (biomeKey != null) {
                String path = biomeKey.getPath();
                String namespace = biomeKey.getNamespace();
                
                if (namespace.equals("minecraft")) {
                    if (path.contains("nether") || path.contains("basalt") || path.contains("crimson") || path.contains("warped")) {
                        return EssenceType.NETHER;
                    }
                    if (path.contains("the_end") || path.contains("end_")) {
                        return EssenceType.VOID;
                    }
                    if (path.contains("desert") || path.contains("badlands") || path.contains("savanna")) {
                        return EssenceType.ARID;
                    }
                    if (path.contains("snowy") || path.contains("frozen") || path.contains("ice")) {
                        return EssenceType.FROZEN;
                    }
                    if (path.contains("ocean") || path.contains("river") || path.contains("beach")) {
                        return EssenceType.WATER;
                    }
                    if (path.contains("forest") || path.contains("jungle") || path.contains("plains") || path.contains("meadow") || path.contains("cherry")) {
                        return EssenceType.NATURE;
                    }
                    if (path.contains("swamp") || path.contains("mangrove")) {
                        return EssenceType.UNDEAD;
                    }
                    if (path.contains("slopes") || path.contains("peaks") || path.contains("windswept") || path.contains("jagged")) {
                        return EssenceType.AIR;
                    }
                } else {
                    if (path.contains("nether") || path.contains("hell")) return EssenceType.NETHER;
                    if (path.contains("end") || path.contains("void")) return EssenceType.VOID;
                    if (path.contains("desert") || path.contains("dune") || path.contains("sandy")) return EssenceType.ARID;
                    if (path.contains("snow") || path.contains("freeze") || path.contains("cold") || path.contains("frost")) return EssenceType.FROZEN;
                    if (path.contains("water") || path.contains("lake") || path.contains("sea") || path.contains("ocean") || path.contains("river")) return EssenceType.WATER;
                    if (path.contains("forest") || path.contains("jungle") || path.contains("green") || path.contains("wood") || path.contains("plains")) return EssenceType.NATURE;
                    if (path.contains("swamp") || path.contains("marsh") || path.contains("bog")) return EssenceType.UNDEAD;
                    if (path.contains("mountain") || path.contains("hill") || path.contains("peak") || path.contains("cliff") || path.contains("alpine")) return EssenceType.AIR;
                }
            }
        }
        return EssenceType.REGULAR;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ScribedChalkBlockEntity blockEntity) {
        if (!state.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get())) {
            return;
        }
        // 1. Determine active circle status for OUTPUT center block
        if (state.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.OUTPUT) {
            int activeTier = 0;
            boolean circuit = state.getValue(ScribedChalkBlock.CIRCUIT);
            for (int tier = 4; tier >= 1; tier--) {
                if (ScribedChalkBlock.checkPatternStatic(level, pos, circuit, tier) &&
                    ScribedChalkBlock.validateNodeCountsStatic(level, pos, tier)) {
                    activeTier = tier;
                    break;
                }
            }
            
            blockEntity.activeCircleTier = activeTier;

            if (activeTier > 0) {
                // Mark center block
                blockEntity.setIsInActiveCircle(true);
                // Mark all blocks inside the circle's square footprint that match the circuit type
                for (int dx = -activeTier; dx <= activeTier; dx++) {
                    for (int dz = -activeTier; dz <= activeTier; dz++) {
                        BlockPos p = pos.offset(dx, 0, dz);
                        BlockEntity be = level.getBlockEntity(p);
                        if (be instanceof ScribedChalkBlockEntity chalkBE &&
                            chalkBE.getBlockState().getValue(ScribedChalkBlock.CIRCUIT) == circuit) {
                            chalkBE.setIsInActiveCircle(true);
                        }
                    }
                }
            } else {
                // Clear center block
                blockEntity.setIsInActiveCircle(false);
                // Clear all blocks in 9x9 area (radius 4) that match the circuit type
                for (int dx = -4; dx <= 4; dx++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        BlockPos p = pos.offset(dx, 0, dz);
                        BlockEntity be = level.getBlockEntity(p);
                        if (be instanceof ScribedChalkBlockEntity chalkBE &&
                            chalkBE.getBlockState().getValue(ScribedChalkBlock.CIRCUIT) == circuit) {
                            chalkBE.setIsInActiveCircle(false);
                        }
                    }
                }
            }
        }

        // 2. Perform dyeTicks ticking and ritual progress ticking
        if (level.isClientSide()) {
            // Client-side local interpolation of dyeTicks
            if (state.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.OUTPUT) {
                boolean hasCircleWithDye = false;
                boolean circuit = state.getValue(ScribedChalkBlock.CIRCUIT);
                for (int tier = 4; tier >= 1; tier--) {
                    if (ScribedChalkBlock.checkPatternStatic(level, pos, circuit, tier) &&
                        ScribedChalkBlock.validateNodeCountsStatic(level, pos, tier)) {
                        for (int t = 1; t <= tier; t++) {
                            int[][] offsets = ScribedChalkBlock.getOffsetsForTier(t);
                            for (int[] offset : offsets) {
                                BlockPos p = pos.offset(offset[0], 0, offset[1]);
                                BlockEntity be = level.getBlockEntity(p);
                                if (be instanceof ScribedChalkBlockEntity neighborChalk) {
                                    if (neighborChalk.getActiveAffinity() != EssenceType.REGULAR) {
                                        hasCircleWithDye = true;
                                        break;
                                    }
                                }
                            }
                            if (hasCircleWithDye) break;
                        }
                        break;
                    }
                }
                if (hasCircleWithDye) {
                    blockEntity.dyeTicks++;
                } else {
                    blockEntity.dyeTicks = 0;
                }
            } else {
                if (blockEntity.activeAffinity != EssenceType.REGULAR) {
                    blockEntity.dyeTicks++;
                } else {
                    blockEntity.dyeTicks = 0;
                }
            }

            // Client-side ambient ritual particles
            if (blockEntity.processingTimeTotal > 0 && level.random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.WITCH, 
                    pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, 
                    0, 0.02, 0);
            }
            // Client-side ambient ward particles
            if (blockEntity.isWard && blockEntity.wardTicks > 0 && level.random.nextInt(3) == 0) {
                double angle = level.random.nextDouble() * 2 * Math.PI;
                double r = 1.5 + level.random.nextDouble() * (blockEntity.activeCircleTier * 1.2);
                double px = pos.getX() + 0.5 + Math.cos(angle) * r;
                double pz = pos.getZ() + 0.5 + Math.sin(angle) * r;
                level.addParticle(ParticleTypes.END_ROD, px, pos.getY() + 0.15, pz, 0, 0.01, 0);
            }
            // Client-side ambient logic gate particles
            ScribedChalkBlock.NodeType type = state.getValue(ScribedChalkBlock.NODE_TYPE);
            if (type == ScribedChalkBlock.NodeType.AND_GATE ||
                type == ScribedChalkBlock.NodeType.OR_GATE ||
                type == ScribedChalkBlock.NodeType.NOT_GATE) {
                if (blockEntity.essenceLevel > 0) {
                    if (level.random.nextInt(4) == 0) {
                        double px = pos.getX() + 0.3 + level.random.nextDouble() * 0.4;
                        double py = pos.getY() + 0.15;
                        double pz = pos.getZ() + 0.3 + level.random.nextDouble() * 0.4;
                        level.addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, 0.01, 0);
                    }
                } else {
                    if (level.random.nextInt(10) == 0) {
                        double px = pos.getX() + 0.4 + level.random.nextDouble() * 0.2;
                        double py = pos.getY() + 0.1;
                        double pz = pos.getZ() + 0.4 + level.random.nextDouble() * 0.2;
                        level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0.005, 0);
                    }
                }
            }
            // Client-side ambient Fehu specialized filter particles
            if (type == ScribedChalkBlock.NodeType.RUNE) {
                ItemStack rune = blockEntity.getStoredRune();
                if (!rune.isEmpty() && rune.getItem() == ddraig.net.entropica.registry.ModItems.RUNE_FEHU.get()) {
                    EssenceType filter = blockEntity.getFilterType();
                    if (filter != null && level.random.nextInt(6) == 0) {
                        double px = pos.getX() + 0.3 + level.random.nextDouble() * 0.4;
                        double py = pos.getY() + 0.15;
                        double pz = pos.getZ() + 0.3 + level.random.nextDouble() * 0.4;
                        level.addParticle(new net.minecraft.core.particles.DustParticleOptions(filter.getColorInt(), 1.0F), px, py, pz, 0, 0.01, 0);
                    }
                }
            }
            return;
        }

        // Server-side Ward ticking
        if (blockEntity.isWard && blockEntity.wardTicks > 0) {
            blockEntity.wardTicks--;
            blockEntity.setChanged();

            if (!blockEntity.isInActiveCircle()) {
                blockEntity.isWard = false;
                blockEntity.wardTicks = 0;
                level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 0.8F);
                level.sendBlockUpdated(pos, state, state, 3);
            } else {
                if (blockEntity.wardTicks <= 0) {
                    blockEntity.isWard = false;
                    level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 0.8F);
                    level.sendBlockUpdated(pos, state, state, 3);
                } else if (level.getGameTime() % 5 == 0) {
                    double radius = EntropicaConfig.WARD_CIRCLE_RADIUS_BASE.get() + (blockEntity.activeCircleTier * 4.0);
                    List<net.minecraft.world.entity.monster.Monster> monsters = level.getEntitiesOfClass(
                        net.minecraft.world.entity.monster.Monster.class,
                        new net.minecraft.world.phys.AABB(pos).inflate(radius)
                    );
                    if (!monsters.isEmpty()) {
                        level.playSound(null, pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 0.35F, 0.5F);
                        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            for (int i = 0; i < 12; i++) {
                                double angle = (i * 2.0 * Math.PI) / 12.0;
                                double vx = Math.cos(angle) * 0.15;
                                double vz = Math.sin(angle) * 0.15;
                                serverLevel.sendParticles(ParticleTypes.CLOUD, 
                                    pos.getX() + 0.5, pos.getY() + 0.15, pos.getZ() + 0.5, 
                                    1, vx, 0.0, vz, 0.1);
                            }
                        }
                        for (net.minecraft.world.entity.monster.Monster monster : monsters) {
                            double dx = monster.getX() - (pos.getX() + 0.5);
                            double dz = monster.getZ() - (pos.getZ() + 0.5);
                            double distSq = dx * dx + dz * dz;
                            if (distSq > 0.01) {
                                double dist = Math.sqrt(distSq);
                                double force = (1.0 - (dist / radius)) * 0.35;
                                monster.push((dx / dist) * force, 0.08, (dz / dist) * force);
                                monster.hurtMarked = true;
                            }
                        }
                    }
                }
            }
        }

        // Server-side overclocking and active ritual ticking
        if (blockEntity.overclockTicks > 0) {
            blockEntity.overclockTicks--;
            blockEntity.setChanged();
        }

        if (blockEntity.processingTimeTotal > 0) {
            int progressIncrement = (blockEntity.overclockTicks > 0) ? 2 : 1;
            blockEntity.processingProgress += progressIncrement;
            
            // Spawn ritual particles
            if (level instanceof ServerLevel serverLevel && level.random.nextInt(3) == 0) {
                serverLevel.sendParticles(ParticleTypes.WITCH, 
                    pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, 
                    5, 0.3, 0.1, 0.3, 0.05);
            }

            if (blockEntity.processingProgress >= blockEntity.processingTimeTotal) {
                // Complete active recipe!
                ItemStack result = blockEntity.activeRecipeOutput.copy();
                int count = result.getCount();
                if (blockEntity.doubleYield) {
                    count *= 2;
                }
                if (blockEntity.amplifierCount > 0) {
                    count = Math.max(1, (int) Math.round(count * (1.0 + blockEntity.amplifierCount * 0.5)));
                }
                result.setCount(count);
                
                ItemEntity resultEntity = new ItemEntity(level, 
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 
                    result);
                level.addFreshEntity(resultEntity);
                
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.0F);
                
                // Clear ritual state
                blockEntity.activeRecipeOutput = ItemStack.EMPTY;
                blockEntity.processingProgress = 0;
                blockEntity.processingTimeTotal = 0;
                blockEntity.doubleYield = false;
                blockEntity.amplifierCount = 0;
                blockEntity.capacitorCount = 0;
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            } else {
                blockEntity.setChanged();
            }
        }

        ScribedChalkBlock.NodeType nodeType = state.getValue(ScribedChalkBlock.NODE_TYPE);
        
        // Passive collection node ticking
        if (nodeType == ScribedChalkBlock.NodeType.COLLECTION) {
            blockEntity.scanCooldown++;
            if (blockEntity.scanCooldown >= 120) {
                blockEntity.scanCooldown = 0;
                EssenceType biomeEssence = blockEntity.getEssenceTypeForBiome(level, pos);
                if (biomeEssence != EssenceType.REGULAR) {
                    blockEntity.dyeTicks = 0;
                    blockEntity.activeAffinity = biomeEssence;
                    blockEntity.color = biomeEssence.getColorInt();
                    blockEntity.essenceLevel = 32;
                    blockEntity.propagationStrength = 32;
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            } else {
                if (blockEntity.essenceLevel > 0 || blockEntity.propagationStrength > 0) {
                    blockEntity.essenceLevel = 0;
                    blockEntity.propagationStrength = 0;
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        }

        // Deactivate SOURCE node if out of essence
        if (nodeType == ScribedChalkBlock.NodeType.SOURCE) {
            if (blockEntity.getStoredOrbisCell().isEmpty() && blockEntity.essenceLevel <= 0 && blockEntity.activeAffinity != EssenceType.REGULAR) {
                blockEntity.activeAffinity = EssenceType.REGULAR;
                blockEntity.color = 0xFFCCCCCC;
                blockEntity.propagationStrength = 0;
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        // Extraction node ticking: accumulates and extracts essence
        if (nodeType == ScribedChalkBlock.NodeType.EXTRACTION) {
            if (blockEntity.propagationStrength >= 16 && blockEntity.activeAffinity != EssenceType.REGULAR) {
                BlockEntity aboveBE = level.getBlockEntity(pos.above());
                boolean hasPressureAbove = aboveBE instanceof ddraig.net.entropica.api.pressure.IPressureHandler;
                
                int maxCooldown = hasPressureAbove ? 15 : 40;
                
                blockEntity.scanCooldown++;
                if (blockEntity.scanCooldown >= maxCooldown) {
                    blockEntity.scanCooldown = 0;
                    if (!level.isClientSide()) {
                        boolean pushedSuccessfully = false;
                        
                        ScribedChalkBlockEntity sourceBE = blockEntity.findConnectedSource(level, pos, blockEntity.activeAffinity);
                        if (sourceBE != null) {
                            if (hasPressureAbove) {
                                ddraig.net.entropica.api.pressure.IPressureHandler targetPressureBE = (ddraig.net.entropica.api.pressure.IPressureHandler) aboveBE;
                                int conversionRate = EntropicaConfig.MATERIA_PER_ESSENCE.get();
                                int amountToFill = 1 * conversionRate;
                                ddraig.net.entropica.api.materia.MateriaFumusStack fumus = new ddraig.net.entropica.api.materia.MateriaFumusStack(blockEntity.activeAffinity, amountToFill);
                                int filled = targetPressureBE.fill(fumus, false);
                                
                                if (filled > 0) {
                                    // Consume 1 essence or Orbis Cell materia
                                    if (sourceBE.getStoredOrbisCell().isEmpty()) {
                                        sourceBE.setEssenceLevel(Math.max(0, sourceBE.getEssenceLevel() - 1));
                                        if (sourceBE.getEssenceLevel() <= 0) {
                                            sourceBE.activeAffinity = EssenceType.REGULAR;
                                            sourceBE.propagationStrength = 0;
                                            sourceBE.color = 0xFFCCCCCC;
                                        }
                                        sourceBE.setChanged();
                                        level.sendBlockUpdated(sourceBE.getBlockPos(), sourceBE.getBlockState(), sourceBE.getBlockState(), 3);
                                    } else {
                                        ItemStack cell = sourceBE.getStoredOrbisCell();
                                        int currentVis = ddraig.net.entropica.item.OrbisCellItem.getStoredVisAmount(cell);
                                        int connectedAmps = sourceBE.countConnectedAmplifiers(level, sourceBE.getBlockPos());
                                        int drainRate = 50 + connectedAmps * 20;
                                        int newVis = Math.max(0, currentVis - drainRate);
                                        ddraig.net.entropica.item.OrbisCellItem.setStoredVisAmount(cell, newVis);
                                        sourceBE.setStoredOrbisCell(cell);
                                        if (newVis <= 0) {
                                            sourceBE.activeAffinity = EssenceType.REGULAR;
                                            sourceBE.propagationStrength = 0;
                                            sourceBE.color = 0xFFCCCCCC;
                                            sourceBE.setChanged();
                                            level.sendBlockUpdated(sourceBE.getBlockPos(), sourceBE.getBlockState(), sourceBE.getBlockState(), 3);
                                            level.playSound(null, sourceBE.getBlockPos(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 0.8F);
                                        }
                                    }
                                    pushedSuccessfully = true;
                                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.4F, 1.2F);
                                    
                                    if (level instanceof ServerLevel serverLevel) {
                                        for (int yStep = 0; yStep < 5; yStep++) {
                                            double py = pos.getY() + 0.15 + (yStep * 0.17);
                                            serverLevel.sendParticles(ParticleTypes.WITCH, 
                                                pos.getX() + 0.5, py, pos.getZ() + 0.5, 
                                                1, 0.0, 0.01, 0.0, 0.0);
                                        }
                                    }
                                }
                            } else {
                                Direction facing = blockEntity.getFacing();
                                BlockPos targetPos = pos.relative(facing);
                                BlockEntity targetBE = level.getBlockEntity(targetPos);
                                
                                if (targetBE instanceof ScribedChalkBlockEntity inputBE && 
                                    inputBE.getBlockState().is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) &&
                                    inputBE.getBlockState().getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.INPUT) {
                                    
                                    ItemStack cellStack = inputBE.getStoredItem();
                                    if (!cellStack.isEmpty() && blockEntity.tryPushVis(cellStack, blockEntity.activeAffinity)) {
                                        inputBE.setStoredItem(cellStack);
                                        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.5f, 1.0f);
                                        
                                        if (sourceBE.getStoredOrbisCell().isEmpty()) {
                                            sourceBE.setEssenceLevel(Math.max(0, sourceBE.getEssenceLevel() - 1));
                                            if (sourceBE.getEssenceLevel() <= 0) {
                                                sourceBE.activeAffinity = EssenceType.REGULAR;
                                                sourceBE.propagationStrength = 0;
                                                sourceBE.color = 0xFFCCCCCC;
                                            }
                                            sourceBE.setChanged();
                                            level.sendBlockUpdated(sourceBE.getBlockPos(), sourceBE.getBlockState(), sourceBE.getBlockState(), 3);
                                        } else {
                                            ItemStack cell = sourceBE.getStoredOrbisCell();
                                            int currentVis = ddraig.net.entropica.item.OrbisCellItem.getStoredVisAmount(cell);
                                            int connectedAmps = sourceBE.countConnectedAmplifiers(level, sourceBE.getBlockPos());
                                            int drainRate = 50 + connectedAmps * 20;
                                            int newVis = Math.max(0, currentVis - drainRate);
                                            ddraig.net.entropica.item.OrbisCellItem.setStoredVisAmount(cell, newVis);
                                            sourceBE.setStoredOrbisCell(cell);
                                            if (newVis <= 0) {
                                                sourceBE.activeAffinity = EssenceType.REGULAR;
                                                sourceBE.propagationStrength = 0;
                                                sourceBE.color = 0xFFCCCCCC;
                                                sourceBE.setChanged();
                                                level.sendBlockUpdated(sourceBE.getBlockPos(), sourceBE.getBlockState(), sourceBE.getBlockState(), 3);
                                                level.playSound(null, sourceBE.getBlockPos(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 0.8F);
                                            }
                                        }
                                        pushedSuccessfully = true;
                                    }
                                }
                            }
                            
                            if (!pushedSuccessfully) {
                                ItemStack orbStack = new ItemStack(ddraig.net.entropica.registry.ModItems.WEAK_ESSENCE.get());
                                ddraig.net.entropica.item.EssenceItem.setEssenceType(orbStack, blockEntity.activeAffinity);
                                ddraig.net.entropica.entity.EssenceOrbEntity orb = new ddraig.net.entropica.entity.EssenceOrbEntity(level, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, orbStack);
                                level.addFreshEntity(orb);
                                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.5f, 1.2f);
                                
                                if (sourceBE.getStoredOrbisCell().isEmpty()) {
                                    sourceBE.setEssenceLevel(Math.max(0, sourceBE.getEssenceLevel() - orb.getChargeAmount()));
                                    if (sourceBE.getEssenceLevel() <= 0) {
                                        sourceBE.activeAffinity = EssenceType.REGULAR;
                                        sourceBE.propagationStrength = 0;
                                        sourceBE.color = 0xFFCCCCCC;
                                    }
                                    sourceBE.setChanged();
                                    level.sendBlockUpdated(sourceBE.getBlockPos(), sourceBE.getBlockState(), sourceBE.getBlockState(), 3);
                                } else {
                                    ItemStack cell = sourceBE.getStoredOrbisCell();
                                    int currentVis = ddraig.net.entropica.item.OrbisCellItem.getStoredVisAmount(cell);
                                    int connectedAmps = sourceBE.countConnectedAmplifiers(level, sourceBE.getBlockPos());
                                    int drainRate = (50 + connectedAmps * 20) * orb.getChargeAmount();
                                    int newVis = Math.max(0, currentVis - drainRate);
                                    ddraig.net.entropica.item.OrbisCellItem.setStoredVisAmount(cell, newVis);
                                    sourceBE.setStoredOrbisCell(cell);
                                    if (newVis <= 0) {
                                        sourceBE.activeAffinity = EssenceType.REGULAR;
                                        sourceBE.propagationStrength = 0;
                                        sourceBE.color = 0xFFCCCCCC;
                                        sourceBE.setChanged();
                                        level.sendBlockUpdated(sourceBE.getBlockPos(), sourceBE.getBlockState(), sourceBE.getBlockState(), 3);
                                        level.playSound(null, sourceBE.getBlockPos(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 0.8F);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                blockEntity.scanCooldown = 0;
            }
        }

        // Check if we are a direct source
        boolean isDirectSource = false;
        if (!blockEntity.getStoredOrbisCell().isEmpty()) {
            int currentVis = ddraig.net.entropica.item.OrbisCellItem.getStoredVisAmount(blockEntity.getStoredOrbisCell());
            if (currentVis > 0) {
                isDirectSource = true;
            }
        } else if (nodeType == ScribedChalkBlock.NodeType.SOURCE) {
            if (blockEntity.activeAffinity != EssenceType.REGULAR && blockEntity.essenceLevel > 0) {
                isDirectSource = true;
            }
        }

        int newPropagation = 0;
        EssenceType newAffinity = EssenceType.REGULAR;
        int newColor = blockEntity.getDefaultColor(state);

        if (isDirectSource) {
            newPropagation = 32;
            newAffinity = blockEntity.activeAffinity;
            newColor = blockEntity.color;
        } else {
            boolean circuit = state.getValue(ScribedChalkBlock.CIRCUIT);

            // Handle Logic Gates (AND, OR, NOT)
            if (nodeType == ScribedChalkBlock.NodeType.AND_GATE ||
                nodeType == ScribedChalkBlock.NodeType.OR_GATE ||
                nodeType == ScribedChalkBlock.NodeType.NOT_GATE) {

                Direction facingDir = blockEntity.getFacing();
                Direction back = facingDir.getOpposite();
                Direction left = facingDir.getCounterClockWise();
                Direction right = facingDir.getClockWise();

                int activeInputs = 0;
                int maxInputLevel = 0;
                EssenceType bestAffinity = EssenceType.REGULAR;
                int bestColor = 0xFFCCCCCC;

                int controlLevel = 0;
                EssenceType controlAffinity = EssenceType.REGULAR;
                int controlColor = 0xFFCCCCCC;

                int maxSideLevel = 0;
                EssenceType sideAffinity = EssenceType.REGULAR;
                int sideColor = 0xFFCCCCCC;

                for (Direction dir : List.of(back, left, right)) {
                    Direction8 dir8 = Direction8.valueOf(dir.name());

                    if (blockEntity.connectsToNeighbor8(level, pos, dir8, circuit)) {
                        BlockPos neighborPos = pos.offset(dir8.getXOffset(), 0, dir8.getZOffset());
                        BlockEntity neighborBE = level.getBlockEntity(neighborPos);
                        if (neighborBE instanceof ScribedChalkBlockEntity neighborChalk) {
                            if (neighborChalk.getBlockState().is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) &&
                                neighborChalk.getBlockState().getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.EXTRACTION) {
                                continue;
                            }
                            int lvl = neighborChalk.getPropagationStrength();
                            EssenceType aff = neighborChalk.getActiveAffinity();
                            int col = neighborChalk.getColor();

                            if (lvl > 0 && aff != EssenceType.REGULAR) {
                                if (dir == back) {
                                    controlLevel = lvl;
                                    controlAffinity = aff;
                                    controlColor = col;
                                } else {
                                    if (lvl > maxSideLevel) {
                                        maxSideLevel = lvl;
                                        sideAffinity = aff;
                                        sideColor = col;
                                    }
                                }

                                activeInputs++;
                                if (lvl > maxInputLevel) {
                                    maxInputLevel = lvl;
                                    bestAffinity = aff;
                                    bestColor = col;
                                }
                            }
                        }
                    }
                }

                if (nodeType == ScribedChalkBlock.NodeType.AND_GATE) {
                    if (activeInputs >= 2) {
                        newPropagation = maxInputLevel > 1 ? maxInputLevel - 1 : 0;
                        newAffinity = newPropagation > 0 ? bestAffinity : EssenceType.REGULAR;
                        newColor = newPropagation > 0 ? bestColor : blockEntity.getDefaultColor(state);
                    }
                } else if (nodeType == ScribedChalkBlock.NodeType.OR_GATE) {
                    if (activeInputs >= 1) {
                        newPropagation = maxInputLevel > 1 ? maxInputLevel - 1 : 0;
                        newAffinity = newPropagation > 0 ? bestAffinity : EssenceType.REGULAR;
                        newColor = newPropagation > 0 ? bestColor : blockEntity.getDefaultColor(state);
                    }
                } else if (nodeType == ScribedChalkBlock.NodeType.NOT_GATE) {
                    boolean hasControl = (controlLevel > 0 && controlAffinity != EssenceType.REGULAR);
                    if (!hasControl && maxSideLevel > 0) {
                        newPropagation = maxSideLevel > 1 ? maxSideLevel - 1 : 0;
                        newAffinity = newPropagation > 0 ? sideAffinity : EssenceType.REGULAR;
                        newColor = newPropagation > 0 ? sideColor : blockEntity.getDefaultColor(state);
                    }
                }

            } else {
                // Handle Normal Chalk Paths / Runes
                int maxNeighborLevel = 0;
                EssenceType bestAffinity = EssenceType.REGULAR;
                int bestColor = 0xFFCCCCCC;

                ItemStack runeStack = blockEntity.getStoredRune();
                boolean isThurisaz = (!runeStack.isEmpty() && runeStack.getItem() == ddraig.net.entropica.registry.ModItems.RUNE_THURISAZ.get());
                boolean isUruz = (!runeStack.isEmpty() && runeStack.getItem() == ddraig.net.entropica.registry.ModItems.RUNE_URUZ.get());
                boolean isIsa = (!runeStack.isEmpty() && runeStack.getItem() == ddraig.net.entropica.registry.ModItems.RUNE_ISA.get());
                boolean isFehu = (!runeStack.isEmpty() && runeStack.getItem() == ddraig.net.entropica.registry.ModItems.RUNE_FEHU.get());

                if (isIsa) {
                    maxNeighborLevel = 0;
                } else {
                    // 1. Scan normal physical neighbors
                    for (Direction8 dir8 : Direction8.values()) {
                        Direction dir = switch (dir8) {
                            case NORTH -> Direction.NORTH;
                            case SOUTH -> Direction.SOUTH;
                            case EAST -> Direction.EAST;
                            case WEST -> Direction.WEST;
                            default -> null;
                        };

                        if (nodeType == ScribedChalkBlock.NodeType.DIODE) {
                            if (dir == null || dir != blockEntity.getFacing().getOpposite()) {
                                continue;
                            }
                        }

                        if (blockEntity.connectsToNeighbor8(level, pos, dir8, circuit)) {
                            BlockPos neighborPos = pos.offset(dir8.getXOffset(), 0, dir8.getZOffset());
                            BlockEntity neighborBE = level.getBlockEntity(neighborPos);
                            if (neighborBE instanceof ScribedChalkBlockEntity neighborChalk) {
                                BlockState neighborState = neighborChalk.getBlockState();
                                if (neighborState.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) && 
                                    neighborState.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.EXTRACTION) {
                                    continue;
                                }
                                if (neighborState.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) && 
                                    neighborState.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.DIODE) {
                                    if (dir == null || neighborChalk.getFacing() != dir.getOpposite()) {
                                        continue;
                                    }
                                }

                                int neighborLevel = neighborChalk.getPropagationStrength();
                                if (neighborLevel > maxNeighborLevel && neighborChalk.getActiveAffinity() != EssenceType.REGULAR) {
                                    maxNeighborLevel = neighborLevel;
                                    bestAffinity = neighborChalk.getActiveAffinity();
                                    bestColor = neighborChalk.getColor();
                                }
                            }
                        }
                    }

                    // 2. If Thurisaz, search within 32 blocks for another Thurisaz node
                    if (isThurisaz) {
                        for (int dx = -32; dx <= 32; dx++) {
                            for (int dz = -32; dz <= 32; dz++) {
                                if (dx == 0 && dz == 0) continue;
                                BlockPos otherPos = pos.offset(dx, 0, dz);
                                BlockEntity otherBE = level.getBlockEntity(otherPos);
                                if (otherBE instanceof ScribedChalkBlockEntity otherChalk) {
                                    ItemStack otherRune = otherChalk.getStoredRune();
                                    if (!otherRune.isEmpty() && otherRune.getItem() == ddraig.net.entropica.registry.ModItems.RUNE_THURISAZ.get()) {
                                        BlockState otherState = otherChalk.getBlockState();
                                        if (otherState.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get())) {
                                            int otherInputLevel = otherChalk.getWireInputLevel(level, otherPos, otherState.getValue(ScribedChalkBlock.NODE_TYPE));
                                            if (otherInputLevel > maxNeighborLevel) {
                                                maxNeighborLevel = otherInputLevel;
                                                bestAffinity = otherChalk.getActiveAffinity();
                                                bestColor = otherChalk.getColor();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. If RESONATOR, search within 16 blocks for another RESONATOR node
                    if (nodeType == ScribedChalkBlock.NodeType.RESONATOR) {
                        for (int dx = -16; dx <= 16; dx++) {
                            for (int dz = -16; dz <= 16; dz++) {
                                if (dx == 0 && dz == 0) continue;
                                BlockPos otherPos = pos.offset(dx, 0, dz);
                                BlockEntity otherBE = level.getBlockEntity(otherPos);
                                if (otherBE instanceof ScribedChalkBlockEntity otherChalk) {
                                    BlockState otherState = otherChalk.getBlockState();
                                    if (otherState.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) && 
                                        otherState.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.RESONATOR) {
                                        int otherLevel = otherChalk.getPropagationStrength();
                                        if (otherLevel > maxNeighborLevel && otherChalk.getActiveAffinity() != EssenceType.REGULAR) {
                                            maxNeighborLevel = otherLevel;
                                            bestAffinity = otherChalk.getActiveAffinity();
                                            bestColor = otherChalk.getColor();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Calculate next level
                if (maxNeighborLevel > 0) {
                    if (isUruz || nodeType == ScribedChalkBlock.NodeType.AMPLIFIER) {
                        newPropagation = 32;
                    } else {
                        newPropagation = maxNeighborLevel > 1 ? maxNeighborLevel - 1 : 0;
                    }

                    newAffinity = newPropagation > 0 ? bestAffinity : EssenceType.REGULAR;
                    newColor = newPropagation > 0 ? bestColor : blockEntity.getDefaultColor(state);

                    if (isFehu) {
                        EssenceType filter = blockEntity.getFilterType();
                        if (filter != null) {
                            if (newAffinity != filter) {
                                newPropagation = 0;
                                newColor = blockEntity.getDefaultColor(state);
                            }
                        } else {
                            if (newAffinity == EssenceType.REGULAR) {
                                newPropagation = 0;
                                newColor = blockEntity.getDefaultColor(state);
                            }
                        }
                    }
                }
            }
        }

        if (blockEntity.propagationStrength != newPropagation || blockEntity.activeAffinity != newAffinity || blockEntity.color != newColor) {
            if (blockEntity.activeAffinity != newAffinity) {
                blockEntity.dyeTicks = 0;
            }
            blockEntity.propagationStrength = newPropagation;
            blockEntity.activeAffinity = newAffinity;
            blockEntity.color = newColor;
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        // Ticking dyeTicks
        if (state.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.OUTPUT) {
            boolean hasCircleWithDye = false;
            boolean circuit = state.getValue(ScribedChalkBlock.CIRCUIT);
            for (int tier = 4; tier >= 1; tier--) {
                if (ScribedChalkBlock.checkPatternStatic(level, pos, circuit, tier) &&
                    ScribedChalkBlock.validateNodeCountsStatic(level, pos, tier)) {
                    for (int t = 1; t <= tier; t++) {
                        int[][] offsets = ScribedChalkBlock.getOffsetsForTier(t);
                        for (int[] offset : offsets) {
                            BlockPos p = pos.offset(offset[0], 0, offset[1]);
                            BlockEntity be = level.getBlockEntity(p);
                            if (be instanceof ScribedChalkBlockEntity neighborChalk) {
                                if (neighborChalk.getActiveAffinity() != EssenceType.REGULAR) {
                                    hasCircleWithDye = true;
                                    break;
                                }
                            }
                        }
                        if (hasCircleWithDye) break;
                    }
                    break;
                }
            }
            if (hasCircleWithDye) {
                blockEntity.dyeTicks++;
                blockEntity.setChanged();
            } else if (blockEntity.dyeTicks != 0) {
                blockEntity.dyeTicks = 0;
                blockEntity.setChanged();
            }
        } else {
            if (blockEntity.activeAffinity != EssenceType.REGULAR) {
                blockEntity.dyeTicks++;
                blockEntity.setChanged();
            } else if (blockEntity.dyeTicks != 0) {
                blockEntity.dyeTicks = 0;
                blockEntity.setChanged();
            }
        }

        // Scan for items at INPUT node
        if (nodeType == ScribedChalkBlock.NodeType.INPUT) {
            blockEntity.scanCooldown++;
            if (blockEntity.scanCooldown >= 10) {
                blockEntity.scanCooldown = 0;
                blockEntity.scanForItems(level, pos);
            }
        }
    }

    private void scanForItems(Level level, BlockPos pos) {
        if (!this.storedOrbisCell.isEmpty()) return;

        AABB area = new AABB(pos).inflate(0.2, 0.5, 0.2);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);

        for (ItemEntity itemEntity : items) {
            if (itemEntity.isRemoved()) continue;
            ItemStack stack = itemEntity.getItem();

            if (stack.getItem() instanceof EssenceItem) {
                EssenceType type = EssenceItem.getEssenceType(stack);
                if (type != null) {
                    if (this.activeAffinity != type) {
                        this.dyeTicks = 0;
                    }
                    this.activeAffinity = type;
                    this.color = type.getColorInt();
                    
                    // Consume 1 item from the stack
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        itemEntity.discard();
                    } else {
                        itemEntity.setItem(stack);
                    }

                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.2f);
                    this.setChanged();
                    level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
                    break;
                }
            } else if (stack.getItem() instanceof OrbisCellItem) {
                EssenceType type = OrbisCellItem.getStoredVisType(stack);
                if (type != null) {
                    if (this.activeAffinity != type) {
                        this.dyeTicks = 0;
                    }
                    this.activeAffinity = type;
                    this.color = type.getColorInt();
                    
                    ItemStack cellToStore = stack.copy();
                    cellToStore.setCount(1);
                    this.storedOrbisCell = cellToStore;

                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        itemEntity.discard();
                    } else {
                        itemEntity.setItem(stack);
                    }

                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 0.8f);
                    this.setChanged();
                    level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
                    break;
                }
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.color = input.read("ChalkColor", Codec.INT).orElse(0xFFCCCCCC);
        String affinityStr = input.read("ActiveAffinity", Codec.STRING).orElse("REGULAR");
        try {
            this.activeAffinity = EssenceType.valueOf(affinityStr);
        } catch (IllegalArgumentException e) {
            this.activeAffinity = EssenceType.REGULAR;
        }
        this.storedOrbisCell = input.read("StoredOrbisCell", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.storedRune = input.read("StoredRune", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.storedItem = input.read("StoredItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.dyeTicks = input.read("DyeTicks", Codec.INT).orElse(0);
        this.isInActiveCircle = input.read("IsInActiveCircle", Codec.BOOL).orElse(false);
        this.essenceLevel = input.read("EssenceLevel", Codec.INT).orElse(0);
        this.propagationStrength = input.read("PropagationStrength", Codec.INT).orElse(0);
        this.facing = Direction.byName(input.read("Facing", Codec.STRING).orElse("north"));
        if (this.facing == null) this.facing = Direction.NORTH;
        for (Direction8 dir : Direction8.values()) {
            this.connectionOverrides[dir.ordinal()] = input.read("override_" + dir.getName(), Codec.INT).orElse(0);
        }
        this.activeRecipeOutput = input.read("ActiveRecipeOutput", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.processingProgress = input.read("ProcessingProgress", Codec.INT).orElse(0);
        this.processingTimeTotal = input.read("ProcessingTimeTotal", Codec.INT).orElse(0);
        this.overclockTicks = input.read("OverclockTicks", Codec.INT).orElse(0);
        this.doubleYield = input.read("DoubleYield", Codec.BOOL).orElse(false);
        this.isWard = input.read("IsWard", Codec.BOOL).orElse(false);
        this.wardTicks = input.read("WardTicks", Codec.INT).orElse(0);
        this.activeCircleTier = input.read("ActiveCircleTier", Codec.INT).orElse(0);
        this.amplifierCount = input.read("AmplifierCount", Codec.INT).orElse(0);
        this.capacitorCount = input.read("CapacitorCount", Codec.INT).orElse(0);
        String filterTypeStr = input.read("FilterType", Codec.STRING).orElse("");
        if (!filterTypeStr.isEmpty()) {
            try {
                this.filterType = EssenceType.valueOf(filterTypeStr);
            } catch (Exception e) {
                this.filterType = null;
            }
        } else {
            this.filterType = null;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("ChalkColor", Codec.INT, this.color);
        output.store("ActiveAffinity", Codec.STRING, this.activeAffinity.name());
        output.store("StoredOrbisCell", ItemStack.OPTIONAL_CODEC, this.storedOrbisCell);
        output.store("StoredRune", ItemStack.OPTIONAL_CODEC, this.storedRune);
        output.store("StoredItem", ItemStack.OPTIONAL_CODEC, this.storedItem);
        output.store("DyeTicks", Codec.INT, this.dyeTicks);
        output.store("IsInActiveCircle", Codec.BOOL, this.isInActiveCircle);
        output.store("EssenceLevel", Codec.INT, this.essenceLevel);
        output.store("PropagationStrength", Codec.INT, this.propagationStrength);
        output.store("Facing", Codec.STRING, this.facing.getName());
        for (Direction8 dir : Direction8.values()) {
            output.store("override_" + dir.getName(), Codec.INT, this.connectionOverrides[dir.ordinal()]);
        }
        output.store("ActiveRecipeOutput", ItemStack.OPTIONAL_CODEC, this.activeRecipeOutput);
        output.store("ProcessingProgress", Codec.INT, this.processingProgress);
        output.store("ProcessingTimeTotal", Codec.INT, this.processingTimeTotal);
        output.store("OverclockTicks", Codec.INT, this.overclockTicks);
        output.store("DoubleYield", Codec.BOOL, this.doubleYield);
        output.store("IsWard", Codec.BOOL, this.isWard);
        output.store("WardTicks", Codec.INT, this.wardTicks);
        output.store("ActiveCircleTier", Codec.INT, this.activeCircleTier);
        output.store("AmplifierCount", Codec.INT, this.amplifierCount);
        output.store("CapacitorCount", Codec.INT, this.capacitorCount);
        if (this.filterType != null) {
            output.store("FilterType", Codec.STRING, this.filterType.name());
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public void startRitual(ItemStack output, int duration) {
        this.activeRecipeOutput = output.copy();
        this.processingTimeTotal = duration;
        this.processingProgress = 0;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean isProcessing() {
        return this.processingTimeTotal > 0;
    }

    public int getOverclockTicks() {
        return this.overclockTicks;
    }

    public void setOverclockTicks(int ticks) {
        this.overclockTicks = ticks;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean isDoubleYield() {
        return this.doubleYield;
    }

    public void setDoubleYield(boolean doubleYield) {
        this.doubleYield = doubleYield;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    private int getWireInputLevel(Level level, BlockPos pos, ScribedChalkBlock.NodeType nodeType) {
        int maxNeighborLevel = 0;
        BlockState state = getBlockState();
        if (!state.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get())) {
            return 0;
        }
        boolean circuit = state.getValue(ScribedChalkBlock.CIRCUIT);
        for (Direction8 dir8 : Direction8.values()) {
            Direction dir = switch (dir8) {
                case NORTH -> Direction.NORTH;
                case SOUTH -> Direction.SOUTH;
                case EAST -> Direction.EAST;
                case WEST -> Direction.WEST;
                default -> null;
            };
            if (dir == null) continue;
            
            if (connectsToNeighbor8(level, pos, dir8, circuit)) {
                BlockPos neighborPos = pos.offset(dir8.getXOffset(), 0, dir8.getZOffset());
                BlockEntity neighborBE = level.getBlockEntity(neighborPos);
                if (neighborBE instanceof ScribedChalkBlockEntity neighborChalk) {
                    if (neighborChalk.getBlockState().is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) &&
                        neighborChalk.getBlockState().getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.EXTRACTION) {
                        continue;
                    }
                    ItemStack neighborRune = neighborChalk.getStoredRune();
                    if (!neighborRune.isEmpty() && neighborRune.getItem() == ddraig.net.entropica.registry.ModItems.RUNE_THURISAZ.get()) {
                        continue;
                    }
                    int neighborLevel = neighborChalk.getPropagationStrength();
                    if (neighborLevel > maxNeighborLevel && neighborChalk.getActiveAffinity() != EssenceType.REGULAR) {
                        maxNeighborLevel = neighborLevel;
                    }
                }
            }
        }
        return maxNeighborLevel;
    }

    public boolean isWard() {
        return this.isWard;
    }

    public void setWard(boolean isWard) {
        this.isWard = isWard;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public int getWardTicks() {
        return this.wardTicks;
    }

    public void setWardTicks(int ticks) {
        this.wardTicks = ticks;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public int getActiveCircleTier() {
        return this.activeCircleTier;
    }

    public void setActiveCircleTier(int tier) {
        this.activeCircleTier = tier;
    }

    private int countConnectedAmplifiers(Level level, BlockPos start) {
        int count = 0;
        java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty() && visited.size() < 64) {
            BlockPos current = queue.poll();
            BlockState state = level.getBlockState(current);
            if (state.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get())) {
                if (state.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.AMPLIFIER) {
                    count++;
                }
                boolean circuit = state.getValue(ScribedChalkBlock.CIRCUIT);
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor)) {
                        BlockState neighborState = level.getBlockState(neighbor);
                        if (neighborState.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) &&
                            neighborState.getValue(ScribedChalkBlock.CIRCUIT) == circuit) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        return count;
    }

    private boolean tryPushVis(ItemStack cellStack, EssenceType type) {
        if (cellStack.isEmpty()) return false;
        
        // 1. Orbis Cell Item (Tier 2)
        if (cellStack.getItem() instanceof ddraig.net.entropica.item.OrbisCellItem) {
            EssenceType storedType = ddraig.net.entropica.item.OrbisCellItem.getStoredVisType(cellStack);
            if (storedType != null && storedType != type) {
                return false; // Type mismatch
            }
            int maxVis = ((ddraig.net.entropica.item.OrbisCellItem) cellStack.getItem()).getMaxVis();
            int currentVis = ddraig.net.entropica.item.OrbisCellItem.getStoredVisAmount(cellStack);
            if (currentVis < maxVis) {
                if (storedType == null) {
                    CompoundTag tag = cellStack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                    tag.putString("MateriaType", type.name());
                    cellStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
                }
                int newVis = Math.min(maxVis, currentVis + 100);
                ddraig.net.entropica.item.OrbisCellItem.setStoredVisAmount(cellStack, newVis);
                return true;
            }
            return false;
        }
        
        // 2. Generic BlockItem or other items that store StoredMateria<Tier> / MateriaType
        net.minecraft.world.item.component.CustomData customData = cellStack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        net.minecraft.world.item.Item item = cellStack.getItem();
        if (item instanceof net.minecraft.world.item.BlockItem blockItem) {
            net.minecraft.world.level.block.Block block = blockItem.getBlock();
            int tier = 0;
            if (block instanceof ddraig.net.entropica.block.OrbisCellBlock) tier = 2;
            else if (block instanceof ddraig.net.entropica.block.SublimatedOrbisCellBlock) tier = 3;
            else if (block instanceof ddraig.net.entropica.block.PneumaticCalixBlock) tier = 4;
            else if (block instanceof ddraig.net.entropica.block.VoltaicCalixBlock) tier = 5;
            else if (block instanceof ddraig.net.entropica.block.MatrixCalixBlock) tier = 6;
            else if (block instanceof ddraig.net.entropica.block.ThecaCellBlock) tier = 7;
            else if (block instanceof ddraig.net.entropica.block.VasCellBlock) tier = 8;
            else if (block instanceof ddraig.net.entropica.block.MonadCoreBlock) tier = 9;
            else if (block instanceof ddraig.net.entropica.block.AthanorCoreBlock) tier = 10;
            
            if (tier > 0) {
                String materiaKey = "StoredMateria" + tier;
                String typeStr = tag.getString("MateriaType").orElse("");
                if (!typeStr.isEmpty()) {
                    try {
                        EssenceType storedType = EssenceType.valueOf(typeStr);
                        if (storedType != type) {
                            return false; // Type mismatch
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
                
                int storedMana = tag.getInt(materiaKey).orElse(0);
                int maxMana = 10000; // default for cells
                if (block instanceof ddraig.net.entropica.block.SublimatedOrbisCellBlock) {
                    maxMana = 100000; // Sublimated is high tier!
                }
                
                if (storedMana < maxMana) {
                    tag.putString("MateriaType", type.name());
                    tag.putInt(materiaKey, Math.min(maxMana, storedMana + 100));
                    cellStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
                    return true;
                }
            }
        }
        
        return false;
    }
}
