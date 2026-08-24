package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.AstralCrystalItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;

public class AstralCollectorBlockEntity extends BlockEntity {

    public static final int MAX_CAPACITY = 2000;
    public static final int MAX_LINK_RANGE = 48;

    private int storedMateria = 0;
    private EssenceType storedEssence = EssenceType.ASTRAL;
    private boolean isIrradiated = false;
    private int irradiationTicksLeft = 0;

    // Multiblock, Crystal & Beam States
    private boolean structureValid = false;
    private ItemStack socketedCrystal = ItemStack.EMPTY;
    private BlockPos targetPos = null;
    private boolean isBeaming = false;
    private int generatedFlux = 0;
    private int incomingCannibalizedFlux = 0;
    private int cannibalizedTimeout = 0;

    // Client-side visual rotation & animation
    public float clientRotation = 0.0f;

    public AstralCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASTRAL_COLLECTOR_BE.get(), pos, state);
    }

    public int getStoredMateria() {
        return storedMateria;
    }

    public EssenceType getStoredEssence() {
        return storedEssence;
    }

    public boolean isIrradiated() {
        return isIrradiated;
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public ItemStack getSocketedCrystal() {
        return socketedCrystal;
    }

    public void setSocketedCrystal(ItemStack stack) {
        this.socketedCrystal = stack.copy();
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    public void setCustomTargetPos(BlockPos target) {
        if (target != null && canLinkTo(target)) {
            this.targetPos = target;
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public boolean isBeaming() {
        return isBeaming;
    }

    public int getTotalOutputFlux() {
        return generatedFlux + incomingCannibalizedFlux;
    }

    /**
     * Prevents infinite loop cycles when collection altars cannibalize / chain into each other.
     */
    public boolean canLinkTo(BlockPos target) {
        if (level == null || target == null || target.equals(worldPosition)) return false;
        if (target.distSqr(worldPosition) > MAX_LINK_RANGE * MAX_LINK_RANGE) return false;

        Set<BlockPos> visited = new HashSet<>();
        visited.add(worldPosition);

        BlockPos current = target;
        while (current != null) {
            if (visited.contains(current)) {
                return false; // Cycle detected!
            }
            visited.add(current);

            if (!level.hasChunkAt(current)) break;
            BlockEntity be = level.getBlockEntity(current);
            if (be instanceof AstralCollectorBlockEntity nextCollector) {
                current = nextCollector.getTargetPos();
            } else {
                break;
            }
        }
        return true;
    }

    public void receiveStarlightBeam(String starName, EssenceType incomingEssence) {
        if (incomingEssence == null) incomingEssence = EssenceType.ASTRAL;
        this.isIrradiated = true;
        this.irradiationTicksLeft = 10;
        if (this.storedMateria == 0) {
            this.storedEssence = incomingEssence;
        }
        setChanged();
    }

    public void receiveCannibalizedBeam(BlockPos fromPos, int flux, EssenceType incomingEssence) {
        this.incomingCannibalizedFlux += flux;
        this.cannibalizedTimeout = 15;
        this.isIrradiated = true;
        this.irradiationTicksLeft = 15;
        if (this.storedMateria == 0 && incomingEssence != null) {
            this.storedEssence = incomingEssence;
        }
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AstralCollectorBlockEntity be) {
        if (level.isClientSide()) {
            be.clientRotation += 0.03f;
            if (be.isBeaming && be.targetPos != null && !be.socketedCrystal.isEmpty()) {
                // Client-side ambient starburst particle halo
                if (level.random.nextFloat() < 0.35f) {
                    double cx = pos.getX() + 0.5;
                    double cy = pos.getY() + 1.25;
                    double cz = pos.getZ() + 0.5;
                    level.addParticle(ParticleTypes.END_ROD, cx + (level.random.nextDouble() - 0.5) * 0.4, cy + (level.random.nextDouble() - 0.5) * 0.4, cz + (level.random.nextDouble() - 0.5) * 0.4, (level.random.nextDouble() - 0.5) * 0.02, 0.03, (level.random.nextDouble() - 0.5) * 0.02);
                }
            }
            return;
        }

        if (be.irradiationTicksLeft > 0) {
            be.irradiationTicksLeft--;
            be.isIrradiated = true;
        } else {
            be.isIrradiated = false;
        }

        if (be.cannibalizedTimeout > 0) {
            be.cannibalizedTimeout--;
        } else {
            be.incomingCannibalizedFlux = 0;
        }

        boolean changed = false;

        // 1. Structure & Smart Link Check every 20 ticks
        if (level.getGameTime() % 20 == 0) {
            boolean prevStructure = be.structureValid;
            be.structureValid = be.checkCollectionAltarStructure();
            if (prevStructure != be.structureValid) changed = true;

            // Smart Auto-Link if target is null or invalid
            if (be.structureValid && (be.targetPos == null || !level.hasChunkAt(be.targetPos))) {
                be.findNearestTarget();
            }
        }

        // 2. Starlight Energy Generation & Beam Routing
        boolean hasSky = level.canSeeSky(pos.above());
        long timeOfDay = level.getDayTime() % 24000;
        boolean isNight = timeOfDay >= 13000 && timeOfDay <= 23000;

        if (be.structureValid && !be.socketedCrystal.isEmpty() && hasSky) {
            int baseGain = isNight ? 25 : 8;
            if (be.socketedCrystal.getItem() instanceof AstralCrystalItem) {
                int size = AstralCrystalItem.getSize(be.socketedCrystal);
                int purity = AstralCrystalItem.getPurity(be.socketedCrystal);
                baseGain += (size * 4) + (purity / 10);
            }
            be.generatedFlux = baseGain;
        } else {
            be.generatedFlux = 0;
        }

        int totalFlux = be.getTotalOutputFlux();
        boolean wasBeaming = be.isBeaming;
        be.isBeaming = totalFlux > 0 && be.targetPos != null && level.hasChunkAt(be.targetPos);

        if (wasBeaming != be.isBeaming) {
            changed = true;
        }

        // 3. Project Beam to Target (Altar, Lens, or Cannibalized Collector)
        if (be.isBeaming && be.targetPos != null) {
            BlockEntity targetBE = level.getBlockEntity(be.targetPos);
            if (targetBE instanceof AstralAltarCoreBlockEntity altarBE) {
                altarBE.receiveCollectorBeam(pos, totalFlux, be.storedEssence);
            } else if (targetBE instanceof AstralCollectorBlockEntity collectorBE) {
                collectorBE.receiveCannibalizedBeam(pos, totalFlux, be.storedEssence);
            } else if (targetBE instanceof RefractiveAstralLensBlockEntity lensBE) {
                lensBE.receiveRelayBeam(pos, "Collector");
            }

            // Fill internal buffer
            if (be.storedMateria < MAX_CAPACITY && level.getGameTime() % 10 == 0) {
                be.storedMateria = Math.min(MAX_CAPACITY, be.storedMateria + Math.max(1, totalFlux / 10));
                changed = true;
            }
        }

        if (changed || level.getGameTime() % 40 == 0) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    /**
     * 5x5x3 Multiblock Collection Altar validation.
     */
    public boolean checkCollectionAltarStructure() {
        if (level == null) return false;

        // Layer Y = -1 (Center plinth shaft + 4 cornerstones at ±2, ±2)
        BlockPos base1 = worldPosition.below();
        BlockPos shaftPos = base1;
        if (!level.hasChunkAt(shaftPos)) return false;
        BlockState shaftBs = level.getBlockState(shaftPos);
        if (!shaftBs.is(ModBlocks.STARLIGHT_PILLAR.get()) && !shaftBs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) {
            return false;
        }

        BlockPos[] corners1 = {
                base1.offset(2, 0, 2),
                base1.offset(2, 0, -2),
                base1.offset(-2, 0, 2),
                base1.offset(-2, 0, -2)
        };
        for (BlockPos cp : corners1) {
            if (!level.hasChunkAt(cp)) return false;
            BlockState cbs = level.getBlockState(cp);
            if (!cbs.is(ModBlocks.CHISELED_ASTRAL_MARBLE.get()) && !cbs.is(ModBlocks.STARLIGHT_PILLAR.get()) && !cbs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) {
                return false;
            }
        }

        // Layer Y = -2 (5x5 Foundation Dais with Sunken Starlight Pool)
        BlockPos base2 = worldPosition.below(2);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos checkPos = base2.offset(dx, 0, dz);
                if (!level.hasChunkAt(checkPos)) return false;
                BlockState bs = level.getBlockState(checkPos);
                int absX = Math.abs(dx);
                int absZ = Math.abs(dz);

                if (absX == 2 && absZ == 2) {
                    if (!bs.is(ModBlocks.CHISELED_ASTRAL_MARBLE.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) return false;
                } else if (absX == 2 || absZ == 2) {
                    if (!bs.is(ModBlocks.ENGRAVED_ASTRAL_SLATE.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) return false;
                } else {
                    // Center 3x3 sunken pool
                    if (!bs.is(ModBlocks.ASTRAL_MIRROR_BLOCK.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE.get()) && !bs.is(ModBlocks.ASTRAL_MARBLE_BRICKS.get())) return false;
                }
            }
        }

        return true;
    }

    private void findNearestTarget() {
        if (level == null) return;
        BlockPos bestPos = null;
        double bestDistSq = MAX_LINK_RANGE * MAX_LINK_RANGE;

        // 1. Search for nearest Astral Altar Core
        for (int dx = -MAX_LINK_RANGE; dx <= MAX_LINK_RANGE; dx += 4) {
            for (int dz = -MAX_LINK_RANGE; dz <= MAX_LINK_RANGE; dz += 4) {
                for (int dy = -16; dy <= 24; dy += 4) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    if (!level.hasChunkAt(p)) continue;
                    BlockEntity be = level.getBlockEntity(p);
                    if (be instanceof AstralAltarCoreBlockEntity) {
                        double d = worldPosition.distSqr(p);
                        if (d < bestDistSq) {
                            bestDistSq = d;
                            bestPos = p;
                        }
                    }
                }
            }
        }

        // 2. If no altar found, search for nearest Collector (cannibalization)
        if (bestPos == null) {
            for (int dx = -MAX_LINK_RANGE; dx <= MAX_LINK_RANGE; dx += 4) {
                for (int dz = -MAX_LINK_RANGE; dz <= MAX_LINK_RANGE; dz += 4) {
                    for (int dy = -16; dy <= 24; dy += 4) {
                        BlockPos p = worldPosition.offset(dx, dy, dz);
                        if (p.equals(worldPosition) || !level.hasChunkAt(p)) continue;
                        BlockEntity be = level.getBlockEntity(p);
                        if (be instanceof AstralCollectorBlockEntity && canLinkTo(p)) {
                            double d = worldPosition.distSqr(p);
                            if (d < bestDistSq) {
                                bestDistSq = d;
                                bestPos = p;
                            }
                        }
                    }
                }
            }
        }

        if (bestPos != null) {
            this.targetPos = bestPos;
            setChanged();
        }
    }

    public void displayCollectorStatus(Player player) {
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6=== Astral Collection Altar Status ==="), false);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§7Structure Integrity: " + (structureValid ? "§a✓ 5x5x3 Plinth Aligned" : "§c✗ Incomplete (Check Astrolabe)")), false);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§7Socketed Crystal: " + (!socketedCrystal.isEmpty() ? "§b✦ " + socketedCrystal.getHoverName().getString() : "§8○ None (Right-click with Crystal)")), false);
        if (targetPos != null) {
            int dist = (int) Math.sqrt(worldPosition.distSqr(targetPos));
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§7Linked Target: §e(" + targetPos.toShortString() + ") §8[" + dist + " blocks away]"), false);
        } else {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§7Linked Target: §8None (Use Astral Linking Wand)"), false);
        }
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§7Generated Starlight Flux: §f" + generatedFlux + " §7| Cannibalized Input: §e+" + incomingCannibalizedFlux), false);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§7Total Beam Output: §a" + getTotalOutputFlux() + " flux/s §7[" + (isBeaming ? "§bBEAMING" : "§8IDLE") + "§7]"), false);
    }

    public int drainMateria(int maxDrain) {
        int drained = Math.min(storedMateria, maxDrain);
        this.storedMateria -= drained;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return drained;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("StoredMateria", Codec.INT, this.storedMateria);
        output.store("StoredEssence", Codec.STRING, this.storedEssence.name());
        output.store("IsIrradiated", Codec.BOOL, this.isIrradiated);
        output.store("IrradiationTicksLeft", Codec.INT, this.irradiationTicksLeft);
        output.store("StructureValid", Codec.BOOL, this.structureValid);
        output.store("IsBeaming", Codec.BOOL, this.isBeaming);
        output.store("GeneratedFlux", Codec.INT, this.generatedFlux);
        output.store("IncomingCannibalizedFlux", Codec.INT, this.incomingCannibalizedFlux);
        output.store("SocketedCrystal", ItemStack.OPTIONAL_CODEC, this.socketedCrystal);

        if (targetPos != null) {
            output.store("TargetPos", BlockPos.CODEC, this.targetPos);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("StoredMateria", Codec.INT).ifPresent(m -> this.storedMateria = m);
        input.read("StoredEssence", Codec.STRING).ifPresent(s -> {
            try {
                this.storedEssence = EssenceType.valueOf(s);
            } catch (Exception ignored) {
                this.storedEssence = EssenceType.ASTRAL;
            }
        });
        input.read("IsIrradiated", Codec.BOOL).ifPresent(i -> this.isIrradiated = i);
        input.read("IrradiationTicksLeft", Codec.INT).ifPresent(t -> this.irradiationTicksLeft = t);
        input.read("StructureValid", Codec.BOOL).ifPresent(v -> this.structureValid = v);
        input.read("IsBeaming", Codec.BOOL).ifPresent(b -> this.isBeaming = b);
        input.read("GeneratedFlux", Codec.INT).ifPresent(g -> this.generatedFlux = g);
        input.read("IncomingCannibalizedFlux", Codec.INT).ifPresent(c -> this.incomingCannibalizedFlux = c);
        input.read("SocketedCrystal", ItemStack.OPTIONAL_CODEC).ifPresent(stack -> this.socketedCrystal = stack);
        input.read("TargetPos", BlockPos.CODEC).ifPresent(p -> this.targetPos = p);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) {
        return this.saveWithoutMetadata(p);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
