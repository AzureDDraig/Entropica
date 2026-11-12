package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.IVaporHandler;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.api.materia.MateriaSublimataStack;
import ddraig.net.entropica.api.materia.MateriaStack;
import ddraig.net.entropica.block.VaporPneumaticOneWayValveBlock;
import ddraig.net.entropica.block.VaporPneumaticPipeBlock;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.registry.ModAttachments;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import ddraig.net.entropica.api.pressure.IPressureHandler;
import ddraig.net.entropica.api.pressure.PressureNetworkManager;
import ddraig.net.entropica.api.pressure.PressureNetworkHelper;

public class VaporPneumaticPipeBlockEntity extends BlockEntity implements IVaporHandler {

    protected MateriaStack storedMateria = MateriaFumusStack.EMPTY;
    private int lastSyncedAmount = -1;
    private EssenceType lastSyncedType = null;
    private boolean isFlushing = false;
    private boolean firstTick = true;

    private boolean managedByGraph = false;
    private Direction activeBoostDirection = null;
    private float activeBoostStrength = 0.0f;

    public enum PipeTier {
        COPPER, IRON, GOLD, ARCANITE, DIAMOND, VISCANITE, RESONITE, CHARGED_ARCANITE, CHARGED_VISCANITE, CHARGED_RESONITE,
        CAPUTITE, CHARGED_CAPUTITE, SANGUINITE, CHARGED_SANGUINITE, MERCURITE, CHARGED_MERCURITE, EUCLIDITE, CHARGED_EUCLIDITE, ATHANORITE, CHARGED_ATHANORITE,
        DEFAULT;

        public int getCapacity() {
            return switch (this) {
                case COPPER -> EntropicaConfig.COPPER_PIPE_CAPACITY.get();
                case IRON -> EntropicaConfig.IRON_PIPE_CAPACITY.get();
                case GOLD -> EntropicaConfig.GOLD_PIPE_CAPACITY.get();
                case DIAMOND -> EntropicaConfig.DIAMOND_PIPE_CAPACITY.get();
                case ARCANITE -> EntropicaConfig.ARCANITE_PIPE_CAPACITY.get();
                case RESONITE -> EntropicaConfig.RESONITE_PIPE_CAPACITY.get();
                case VISCANITE -> EntropicaConfig.VISCANITE_PIPE_CAPACITY.get();
                case CHARGED_ARCANITE -> EntropicaConfig.CHARGED_ARCANITE_PIPE_CAPACITY.get();
                case CHARGED_VISCANITE -> EntropicaConfig.CHARGED_VISCANITE_PIPE_CAPACITY.get();
                case CHARGED_RESONITE -> EntropicaConfig.CHARGED_RESONITE_PIPE_CAPACITY.get();
                case CAPUTITE -> EntropicaConfig.getCaputitePipeCapacity();
                case CHARGED_CAPUTITE -> EntropicaConfig.getChargedCaputitePipeCapacity();
                case SANGUINITE -> EntropicaConfig.getSanguinitePipeCapacity();
                case CHARGED_SANGUINITE -> EntropicaConfig.getChargedSanguinitePipeCapacity();
                case MERCURITE -> EntropicaConfig.getMercuritePipeCapacity();
                case CHARGED_MERCURITE -> EntropicaConfig.getChargedMercuritePipeCapacity();
                case EUCLIDITE -> EntropicaConfig.getEucliditePipeCapacity();
                case CHARGED_EUCLIDITE -> EntropicaConfig.getChargedEucliditePipeCapacity();
                case ATHANORITE -> EntropicaConfig.getAthanoritePipeCapacity();
                case CHARGED_ATHANORITE -> EntropicaConfig.getChargedAthanoritePipeCapacity();
                default -> 200; // Fallback
            };
        }

        public int getTransferRate() {
            return switch (this) {
                case COPPER -> EntropicaConfig.COPPER_PIPE_TRANSFER_RATE.get();
                case IRON -> EntropicaConfig.IRON_PIPE_TRANSFER_RATE.get();
                case GOLD -> EntropicaConfig.GOLD_PIPE_TRANSFER_RATE.get();
                case DIAMOND -> EntropicaConfig.DIAMOND_PIPE_TRANSFER_RATE.get();
                case ARCANITE -> EntropicaConfig.ARCANITE_PIPE_TRANSFER_RATE.get();
                case RESONITE -> EntropicaConfig.RESONITE_PIPE_TRANSFER_RATE.get();
                case VISCANITE -> EntropicaConfig.VISCANITE_PIPE_TRANSFER_RATE.get();
                case CHARGED_ARCANITE -> EntropicaConfig.CHARGED_ARCANITE_PIPE_TRANSFER_RATE.get();
                case CHARGED_VISCANITE -> EntropicaConfig.CHARGED_VISCANITE_PIPE_TRANSFER_RATE.get();
                case CHARGED_RESONITE -> EntropicaConfig.CHARGED_RESONITE_PIPE_TRANSFER_RATE.get();
                case CAPUTITE -> EntropicaConfig.getCaputitePipeTransferRate();
                case CHARGED_CAPUTITE -> EntropicaConfig.getChargedCaputitePipeTransferRate();
                case SANGUINITE -> EntropicaConfig.getSanguinitePipeTransferRate();
                case CHARGED_SANGUINITE -> EntropicaConfig.getChargedSanguinitePipeTransferRate();
                case MERCURITE -> EntropicaConfig.getMercuritePipeTransferRate();
                case CHARGED_MERCURITE -> EntropicaConfig.getChargedMercuritePipeTransferRate();
                case EUCLIDITE -> EntropicaConfig.getEucliditePipeTransferRate();
                case CHARGED_EUCLIDITE -> EntropicaConfig.getChargedEucliditePipeTransferRate();
                case ATHANORITE -> EntropicaConfig.getAthanoritePipeTransferRate();
                case CHARGED_ATHANORITE -> EntropicaConfig.getChargedAthanoritePipeTransferRate();
                default -> 20; // Fallback
            };
        }

        public float getResistance() {
            return switch (this) {
                case COPPER -> 0.05f;
                case IRON -> 0.03f;
                case GOLD -> 0.02f;
                case DIAMOND -> 0.015f;
                case ARCANITE -> 0.01f;
                case VISCANITE, RESONITE -> 0.005f;
                case CAPUTITE, SANGUINITE, MERCURITE, EUCLIDITE, ATHANORITE -> 0.002f;
                case CHARGED_ARCANITE, CHARGED_VISCANITE, CHARGED_RESONITE,
                     CHARGED_CAPUTITE, CHARGED_SANGUINITE, CHARGED_MERCURITE, CHARGED_EUCLIDITE, CHARGED_ATHANORITE -> 0.0f;
                default -> 0.02f;
            };
        }
    }

    @Override
    public float getResistance() {
        return getTier().getResistance();
    }

    @Override
    public boolean isManagedByGraph() {
        return this.managedByGraph;
    }

    @Override
    public void setManagedByGraph(boolean managed) {
        this.managedByGraph = managed;
    }

    @Override
    public void applyActiveBoost(Direction direction, float strength) {
        this.activeBoostDirection = direction;
        this.activeBoostStrength = strength;
    }

    @Override
    public Direction getActiveBoostDirection() {
        return this.activeBoostDirection;
    }

    @Override
    public float getActiveBoostStrength() {
        return this.activeBoostStrength;
    }

    @Override
    public void clearActiveBoost() {
        this.activeBoostDirection = null;
        this.activeBoostStrength = 0.0f;
    }

    public VaporPneumaticPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VAPOR_PNEUMATIC_PIPE_BE.get(), pos, state);
    }

    public VaporPneumaticPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public PipeTier getTier() {
        if (this.getBlockState() == null) return PipeTier.DEFAULT;
        String path = BuiltInRegistries.BLOCK.getKey(this.getBlockState().getBlock()).getPath();

        // Check longest/most-specific matches first to avoid false positives
        if (path.contains("charged_viscanite")) return PipeTier.CHARGED_VISCANITE;
        if (path.contains("charged_arcanite")) return PipeTier.CHARGED_ARCANITE;
        if (path.contains("charged_resonite")) return PipeTier.CHARGED_RESONITE;
        if (path.contains("charged_athanorite")) return PipeTier.CHARGED_ATHANORITE;
        if (path.contains("charged_euclidite")) return PipeTier.CHARGED_EUCLIDITE;
        if (path.contains("charged_mercurite")) return PipeTier.CHARGED_MERCURITE;
        if (path.contains("charged_sanguinite")) return PipeTier.CHARGED_SANGUINITE;
        if (path.contains("charged_caputite")) return PipeTier.CHARGED_CAPUTITE;
        if (path.contains("viscanite")) return PipeTier.VISCANITE;
        if (path.contains("resonite")) return PipeTier.RESONITE;
        if (path.contains("arcanite")) return PipeTier.ARCANITE;
        if (path.contains("athanorite")) return PipeTier.ATHANORITE;
        if (path.contains("euclidite")) return PipeTier.EUCLIDITE;
        if (path.contains("mercurite")) return PipeTier.MERCURITE;
        if (path.contains("sanguinite")) return PipeTier.SANGUINITE;
        if (path.contains("caputite")) return PipeTier.CAPUTITE;
        if (path.contains("diamond")) return PipeTier.DIAMOND;
        if (path.contains("gold")) return PipeTier.GOLD;
        if (path.contains("iron")) return PipeTier.IRON;
        if (path.contains("copper")) return PipeTier.COPPER;
        return PipeTier.DEFAULT;
    }

    public int getTransferRate() {
        return getTier().getTransferRate();
    }

    @Override
    public int getSafeCapacity() {
        return getTier().getCapacity();
    }

    @Override
    public int getAbsoluteCapacity() {
        return getSafeCapacity() * 3;
    }

    protected List<Direction> getActiveConnections(BlockState state) {
        List<Direction> active = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if (state.hasProperty(VaporPneumaticPipeBlock.getDirectionProperty(dir)) &&
                    state.getValue(VaporPneumaticPipeBlock.getDirectionProperty(dir))) {
                active.add(dir);
            }
        }
        return active;
    }

    // ==========================================
    // PURGE SYSTEM LOGIC
    // ==========================================

    public Optional<Direction> findNearestExit(Level level, BlockPos startPos, int maxRange) {
        Queue<PathNode> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        visited.add(startPos);
        for (Direction dir : getActiveConnections(level.getBlockState(startPos))) {
            queue.add(new PathNode(startPos.relative(dir), dir, 1));
        }
        while (!queue.isEmpty()) {
            PathNode node = queue.poll();
            if (node.distance > maxRange || visited.contains(node.pos)) continue;
            visited.add(node.pos);
            BlockEntity be = level.getBlockEntity(node.pos);
            if (be instanceof IVaporHandler handler) {
                boolean isGenerator = be instanceof CreativeMateriaGeneratorBlockEntity;
                boolean isPipe = be instanceof VaporPneumaticPipeBlockEntity;

                if ((!isPipe && !isGenerator) || (isPipe && isVentingPipe(level, node.pos, (VaporPneumaticPipeBlockEntity)be))) {
                    return Optional.of(node.initialDir);
                }

                if (isPipe) {
                    VaporPneumaticPipeBlockEntity pipe = (VaporPneumaticPipeBlockEntity) be;
                    for (Direction nextDir : pipe.getActiveConnections(level.getBlockState(node.pos))) {
                        queue.add(new PathNode(node.pos.relative(nextDir), node.initialDir, node.distance + 1));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private boolean isVentingPipe(Level level, BlockPos pos, VaporPneumaticPipeBlockEntity pipe) {
        List<Direction> connections = pipe.getActiveConnections(level.getBlockState(pos));
        if (connections.size() == 1) {
            Direction ventDir = connections.get(0).getOpposite();
            return level.getBlockState(pos.relative(ventDir)).isAir();
        }
        return false;
    }

    private record PathNode(BlockPos pos, Direction initialDir, int distance) {}

    public boolean isBeingOverpowered() {
        if (this.level == null) return false;

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        visited.add(this.worldPosition);
        toCheck.add(this.worldPosition);

        int depth = 0;
        while (!toCheck.isEmpty() && depth < 32) {
            BlockPos currentPos = toCheck.poll();
            BlockEntity currentBE = level.getBlockEntity(currentPos);

            List<Direction> connections = new ArrayList<>();
            if (currentBE instanceof VaporPneumaticPipeBlockEntity pipe) {
                connections = pipe.getActiveConnections(level.getBlockState(currentPos));
            }

            for (Direction dir : connections) {
                BlockPos neighborPos = currentPos.relative(dir);
                if (visited.contains(neighborPos)) continue;
                visited.add(neighborPos);

                BlockEntity be = level.getBlockEntity(neighborPos);
                if (be instanceof CreativeMateriaGeneratorBlockEntity gen) {
                    if (this.storedMateria.isEmpty() || gen.getCurrentType() != this.storedMateria.getType()) return true;
                } else if (be instanceof VaporPneumaticPipeBlockEntity pipe) {
                    MateriaStack nStack = pipe.getMateriaInTank();
                    if (!nStack.isEmpty() && (!nStack.is(this.storedMateria.getType()) && nStack.getAmount() > this.storedMateria.getAmount())) {
                        return true;
                    }
                    toCheck.add(neighborPos);
                }
            }
            depth++;
        }
        return false;
    }

    @Override
    public void setRemoved() {
        PressureNetworkManager.onBlockEntityRemoved(this);
        super.setRemoved();
    }

    protected void syncIfNeeded(Level level, BlockPos pos, BlockState state) {
        int currentAmount = storedMateria.isEmpty() ? 0 : storedMateria.getAmount();
        EssenceType currentType = storedMateria.isEmpty() ? null : storedMateria.getType();

        if (currentAmount != lastSyncedAmount || currentType != lastSyncedType) {
            lastSyncedAmount = currentAmount;
            lastSyncedType = currentType;
            this.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        if (firstTick) {
            firstTick = false;
            PressureNetworkManager.onBlockAdded(level, pos, this);
        }

        syncIfNeeded(level, pos, state);

        PressureNetworkManager.tick(level);

        // Open-end venting
        if (!this.storedMateria.isEmpty() && level.getGameTime() % EntropicaConfig.MATERIA_FUMUS_TICK_RATE.get() == 0) {
            List<Direction> active = getActiveConnections(state);
            if (active.size() == 1) {
                Direction ventDir = active.get(0).getOpposite();
                BlockPos ventPos = pos.relative(ventDir);
                if (level.getBlockState(ventPos).isAir()) {
                    if (level instanceof ServerLevel serverLevel) {
                        int ventAmount = Math.min(this.storedMateria.getAmount(), getTransferRate());
                        if (ventAmount > 0) {
                            ventGasIntoAir(serverLevel, pos, ventDir, this.storedMateria, ventAmount);
                            applyVisToxicity(serverLevel, pos);
                            this.storedMateria.shrink(ventAmount);
                            this.setChanged();
                            level.sendBlockUpdated(pos, state, state, 3);
                        }
                    }
                }
            }
        }

        if (!isManagedByGraph()) {
            PressureNetworkHelper.tickLocalEqualization(level, pos, this);
        }
    }

    @Override
    public int fill(MateriaStack resource, boolean simulate) {
        if (resource.isEmpty()) return 0;
        if (!(resource instanceof MateriaFumusStack) && !(resource instanceof MateriaSublimataStack)) return 0;

        // Flushing Mechanics (If a different type of gas forcefully enters)
        if (!this.storedMateria.isEmpty() && !this.storedMateria.is(resource.getType())) {
            if (resource.getAmount() > this.storedMateria.getAmount()) {
                if (simulate || this.isFlushing || this.level == null || this.level.isClientSide()) {
                    return 0;
                }

                this.isFlushing = true;
                try {
                    List<Direction> active = getActiveConnections(getBlockState());
                    for (Direction dir : active) {
                        if (this.storedMateria.isEmpty()) break;

                        BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));

                        if (neighbor instanceof VaporPneumaticOneWayValveBlockEntity oneWay) {
                            if (oneWay.getBlockState().hasProperty(VaporPneumaticOneWayValveBlock.FACING) &&
                                    oneWay.getBlockState().getValue(VaporPneumaticOneWayValveBlock.FACING) == dir.getOpposite()) {
                                continue;
                            }
                        }

                        if (neighbor instanceof IVaporHandler handler) {
                            int accepted = handler.fill(this.storedMateria, false);
                            this.storedMateria.shrink(accepted);
                        }
                    }

                    if (!this.storedMateria.isEmpty() && active.size() == 1) {
                        Direction ventDir = active.get(0).getOpposite();
                        if (level.getBlockState(worldPosition.relative(ventDir)).isAir()) {
                            if (level instanceof ServerLevel serverLevel) {
                                ventGasIntoAir(serverLevel, worldPosition, ventDir, this.storedMateria, this.storedMateria.getAmount());
                                serverLevel.playSound(null, worldPosition, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 0.6F, 1.8F);
                            }
                            this.storedMateria = MateriaFumusStack.EMPTY;
                        }
                    }
                } finally {
                    this.isFlushing = false;
                }
            }

            if (!this.storedMateria.isEmpty()) return 0;
        }

        int currentAmount = this.storedMateria.getAmount();
        int space = Math.max(0, getAbsoluteCapacity() - currentAmount);
        int accepted = Math.min(space, resource.getAmount());

        if (!simulate && accepted > 0) {
            if (this.storedMateria.isEmpty()) {
                this.storedMateria = resource.copy();
                this.storedMateria.setAmount(accepted);
            } else {
                this.storedMateria.grow(accepted);
            }
            this.setChanged();
            if (level != null && !this.level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return accepted;
    }

    @Override
    public @NotNull MateriaStack getMateriaInTank() { return this.storedMateria; }

    @Override
    public @NotNull MateriaStack drain(int maxDrain, boolean simulate) {
        if (this.storedMateria.isEmpty() || maxDrain <= 0) return MateriaFumusStack.EMPTY;
        int drained = Math.min(this.storedMateria.getAmount(), maxDrain);
        MateriaStack result = this.storedMateria.copy();
        result.setAmount(drained);
        if (!simulate) {
            this.storedMateria.shrink(drained);
            this.setChanged();
            if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    protected void ventGasIntoAir(ServerLevel level, BlockPos pos, Direction dir, MateriaStack stack, int amount) {
        double pX = pos.getX() + 0.5 + (dir.getStepX() * 0.6);
        double pY = pos.getY() + 0.5 + (dir.getStepY() * 0.6);
        double pZ = pos.getZ() + 0.5 + (dir.getStepZ() * 0.6);
        float pitch = 0.8F + (float)((double) amount / getTransferRate() * 0.8F);
        if (level.random.nextInt(3) == 0) level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.3F, pitch);
        DustParticleOptions dust = new DustParticleOptions(stack.getType().getColorInt(), 1.5F);
        level.sendParticles(dust, pX, pY, pZ, 10, dir.getStepX() * 0.2, dir.getStepY() * 0.2, dir.getStepZ() * 0.2, 0.1);
    }

    private void applyVisToxicity(ServerLevel level, BlockPos pos) {
        AABB aabb = new AABB(pos).inflate(3.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, aabb);

        // Extract the literal EssenceType from the pipe's internal storage
        String toxicitySource = this.storedMateria.isEmpty() ? "UNKNOWN" : this.storedMateria.getType().name();

        for (Player player : players) {
            player.addEffect(new MobEffectInstance(ModEffects.MATERIA_TOXICITY, 200, 0));
            ModAttachments.setToxicitySource(player, toxicitySource);
        }
    }

    protected void exhaustBreakBlock(ServerLevel level, BlockPos pos) {
        level.destroyBlock(pos, true);
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.7F, 0.8F + level.random.nextFloat() * 0.4F);
        int color = this.storedMateria.isEmpty() ? 0xFFFFFF : this.storedMateria.getType().getColorInt();
        level.sendParticles(new DustParticleOptions(color, 2.0F), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.3, 0.3, 0.3, 0.2);
        applyVisToxicity(level, pos); // Blast the room with toxicity when it explodes
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("FumeAmount", this.storedMateria.getAmount());
        output.putInt("FumeType", this.storedMateria.isEmpty() ? -1 : this.storedMateria.getType().ordinal());
        output.putBoolean("IsSublimated", this.storedMateria instanceof MateriaSublimataStack);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int amount = input.getIntOr("FumeAmount", 0);
        int typeOrd = input.getIntOr("FumeType", -1);
        boolean isSublimated = input.getBooleanOr("IsSublimated", false);
        if (amount <= 0 || typeOrd < 0 || typeOrd >= EssenceType.values().length) {
            this.storedMateria = MateriaFumusStack.EMPTY;
        } else {
            if (isSublimated) {
                this.storedMateria = new MateriaSublimataStack(EssenceType.values()[typeOrd], amount);
            } else {
                this.storedMateria = new MateriaFumusStack(EssenceType.values()[typeOrd], amount);
            }
        }
        this.lastSyncedAmount = -1;
        this.lastSyncedType = null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
