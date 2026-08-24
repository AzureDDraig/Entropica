package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.IVaporHandler;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
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

public class VaporPneumaticPipeBlockEntity extends BlockEntity implements IVaporHandler {

    protected MateriaFumusStack storedMateria = MateriaFumusStack.EMPTY;
    private int lastSyncedAmount = -1;
    private EssenceType lastSyncedType = null;
    private boolean isFlushing = false;

    public enum PipeTier {
        COPPER, IRON, GOLD, ARCANITE, DIAMOND, VISCANITE, RESONITE, CHARGED_ARCANITE, CHARGED_VISCANITE, CHARGED_RESONITE, DEFAULT;

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
                default -> 20; // Fallback
            };
        }
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
        if (path.contains("viscanite")) return PipeTier.VISCANITE;
        if (path.contains("resonite")) return PipeTier.RESONITE;
        if (path.contains("arcanite")) return PipeTier.ARCANITE;
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
                    MateriaFumusStack nStack = pipe.getMateriaInTank();
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

    // ==========================================

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

        syncIfNeeded(level, pos, state);

        int myAmount = this.storedMateria.getAmount();
        int safeCap = getSafeCapacity();
        int absoluteCap = getAbsoluteCapacity();

        // 1. CATASTROPHIC FAILURE
        if (myAmount > absoluteCap) {
            exhaustBreakBlock((ServerLevel) level, pos);
            return;
        }

        // 2. OVERPRESSURE LEAKING (Materia Toxicity)
        if (myAmount > safeCap) {
            float overpressureRatio = (float)(myAmount - safeCap) / (absoluteCap - safeCap);
            // Up to 15% chance to vent every single tick depending on severity
            if (level.random.nextFloat() < overpressureRatio * 0.15f) {
                int leakAmount = Math.max(1, (int)(getTransferRate() * 0.2f));
                ventGasIntoAir((ServerLevel) level, pos, Direction.UP, this.storedMateria, leakAmount);
                applyVisToxicity((ServerLevel) level, pos);
                this.storedMateria.shrink(leakAmount);
                myAmount = this.storedMateria.getAmount();
                this.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        if (storedMateria.isEmpty() || level.getGameTime() % EntropicaConfig.MATERIA_FUMUS_TICK_RATE.get() != 0) {
            return;
        }

        List<Direction> activeConnections = getActiveConnections(state);
        if (activeConnections.isEmpty()) return;

        boolean changed = false;
        EssenceType type = this.storedMateria.getType();
        float myPressure = this.getPressure();

        // PURGE LOGIC INTEGRATION
        boolean beingPurged = isBeingOverpowered();
        Direction priorityDir = null;
        if (beingPurged && activeConnections.size() > 1) {
            priorityDir = findNearestExit(level, pos, 32).orElse(null);
        }

        // 3. OPEN END VENTING
        if (activeConnections.size() == 1 && myAmount > 0) {
            Direction ventDir = activeConnections.get(0).getOpposite();

            if (priorityDir == null || priorityDir == ventDir) {
                BlockPos airPos = pos.relative(ventDir);
                if (level.getBlockState(airPos).isAir()) {
                    int ventAmount = Math.min(myAmount, getTransferRate());
                    ventGasIntoAir((ServerLevel) level, pos, ventDir, this.storedMateria, ventAmount);
                    this.storedMateria.shrink(ventAmount);
                    myAmount -= ventAmount;
                    myPressure = this.getPressure();
                    changed = true;
                }
            }
        }

        if (myAmount <= 0) {
            if (changed) { this.setChanged(); level.sendBlockUpdated(pos, state, state, 3); }
            return;
        }

        // Sort connections by neighbor's pressure (lowest first) to flow into emptiest pipes first
        activeConnections.sort((d1, d2) -> {
            BlockEntity be1 = level.getBlockEntity(pos.relative(d1));
            BlockEntity be2 = level.getBlockEntity(pos.relative(d2));
            float p1 = (be1 instanceof IVaporHandler h1) ? h1.getPressure() : 1.0f;
            float p2 = (be2 instanceof IVaporHandler h2) ? h2.getPressure() : 1.0f;
            return Float.compare(p1, p2);
        });

        // 4. TRANSFER
        for (Direction dir : activeConnections) {
            if (priorityDir != null && dir != priorityDir) continue; // Respect purge target direction

            BlockEntity be = level.getBlockEntity(pos.relative(dir));

            // Anti-Backflow check for One-Way Valves
            if (be instanceof VaporPneumaticOneWayValveBlockEntity oneWay) {
                if (oneWay.getBlockState().hasProperty(VaporPneumaticOneWayValveBlock.FACING) &&
                        oneWay.getBlockState().getValue(VaporPneumaticOneWayValveBlock.FACING) == dir.getOpposite()) {
                    continue;
                }
            }

            if (be instanceof IVaporHandler neighbor) {
                MateriaFumusStack neighborMateria = neighbor.getMateriaInTank();

                if (neighborMateria.isEmpty() || neighborMateria.getType() == type) {
                    float nPressure = neighbor.getPressure();

                    // OVERRIDE: If being purged, ignore pressure rules and violently force gas out
                    if (beingPurged) {
                        int toTransfer = Math.min(myAmount, getTransferRate());
                        int accepted = neighbor.fill(new MateriaFumusStack(type, toTransfer), false);
                        if (accepted > 0) {
                            this.storedMateria.shrink(accepted);
                            myAmount -= accepted;
                            myPressure = this.getPressure();
                            changed = true;
                        }
                    }
                    // STANDARD: Smooth Pressure Gradient
                    else if (myPressure > nPressure) {
                        int nSafeCap = neighbor.getSafeCapacity();

                        // Calculate perfect equilibrium target
                        float targetPressure = (float)(myAmount + neighborMateria.getAmount()) / (safeCap + nSafeCap);
                        int desiredNeighborAmount = (int)(targetPressure * nSafeCap);
                        int toTransfer = desiredNeighborAmount - neighborMateria.getAmount();

                        toTransfer = Math.min(toTransfer, getTransferRate());
                        toTransfer = Math.max(1, toTransfer); // Always move at least 1 to resolve rounding stalls

                        if (toTransfer > 0) {
                            int accepted = neighbor.fill(new MateriaFumusStack(type, toTransfer), false);
                            if (accepted > 0) {
                                this.storedMateria.shrink(accepted);
                                myAmount -= accepted;
                                myPressure = this.getPressure(); // Recalculate my pressure for the next iteration
                                changed = true;
                            }
                        }
                    }
                }
            }
            if (myAmount <= 0) break;
        }

        if (changed) {
            this.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override
    public int fill(MateriaFumusStack resource, boolean simulate) {
        if (resource.isEmpty()) return 0;

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
                this.storedMateria = new MateriaFumusStack(resource.getType(), accepted);
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
    public @NotNull MateriaFumusStack getMateriaInTank() { return this.storedMateria; }

    @Override
    public @NotNull MateriaFumusStack drain(int maxDrain, boolean simulate) {
        if (this.storedMateria.isEmpty() || maxDrain <= 0) return MateriaFumusStack.EMPTY;
        int drained = Math.min(this.storedMateria.getAmount(), maxDrain);
        MateriaFumusStack result = new MateriaFumusStack(this.storedMateria.getType(), drained);
        if (!simulate) {
            this.storedMateria.shrink(drained);
            this.setChanged();
            if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    protected void ventGasIntoAir(ServerLevel level, BlockPos pos, Direction dir, MateriaFumusStack stack, int amount) {
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
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int amount = input.getIntOr("FumeAmount", 0);
        int typeOrd = input.getIntOr("FumeType", -1);
        if (amount <= 0 || typeOrd < 0 || typeOrd >= EssenceType.values().length) {
            this.storedMateria = MateriaFumusStack.EMPTY;
        } else {
            this.storedMateria = new MateriaFumusStack(EssenceType.values()[typeOrd], amount);
        }
        this.lastSyncedAmount = -1;
        this.lastSyncedType = null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
