package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.IVaporHandler;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.api.materia.MateriaStack;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EssenceRepulsionWardBlockEntity extends BlockEntity implements IVaporHandler {

    private EssenceType storedType = null;
    private int storedAmount = 0;
    private int cooldownTicks = 0;

    public EssenceRepulsionWardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENCE_REPULSION_WARD_BE.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) {
            if (this.storedAmount >= EntropicaConfig.WARD_CONSUMPTION_AMOUNT.get()) {
                if (level.random.nextInt(4) == 0) {
                    double px = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 1.5;
                    double py = pos.getY() + 1.1;
                    double pz = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 1.5;
                    level.addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.02, 0);
                }
            }
            return;
        }

        // Server-side
        if (this.storedAmount >= EntropicaConfig.WARD_CONSUMPTION_AMOUNT.get()) {
            // Repel monsters
            if (level.getGameTime() % 5 == 0) {
                double radius = EntropicaConfig.WARD_BLOCK_RADIUS.get();
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

            // Consume Fumus Stack over time
            if (this.cooldownTicks <= 0) {
                this.cooldownTicks = EntropicaConfig.WARD_CONSUMPTION_INTERVAL_SECONDS.get() * 20;
            }
            this.cooldownTicks--;

            if (this.cooldownTicks <= 0) {
                this.storedAmount -= EntropicaConfig.WARD_CONSUMPTION_AMOUNT.get();
                if (this.storedAmount <= 0) {
                    this.storedAmount = 0;
                    this.storedType = null;
                }
                this.cooldownTicks = EntropicaConfig.WARD_CONSUMPTION_INTERVAL_SECONDS.get() * 20;
                this.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        } else {
            this.cooldownTicks = 0;
        }
    }

    // --- IVaporHandler ---
    @Override
    public int fill(MateriaStack resource, boolean simulate) {
        if (resource.isEmpty() || !(resource instanceof MateriaFumusStack)) return 0;
        if (this.storedType != null && this.storedType != resource.getType()) return 0;

        int space = getSafeCapacity() - this.storedAmount;
        if (space <= 0) return 0;

        int toFill = Math.min(resource.getAmount(), space);
        if (!simulate && toFill > 0) {
            this.storedType = resource.getType();
            this.storedAmount += toFill;
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return toFill;
    }

    @NotNull
    @Override
    public MateriaStack drain(int maxDrain, boolean simulate) {
        if (this.storedAmount <= 0 || this.storedType == null) return MateriaFumusStack.EMPTY;
        int toDrain = Math.min(this.storedAmount, maxDrain);
        MateriaStack result = new MateriaFumusStack(this.storedType, toDrain);
        if (!simulate && toDrain > 0) {
            this.storedAmount -= toDrain;
            if (this.storedAmount <= 0) {
                this.storedType = null;
            }
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return result;
    }

    @NotNull
    @Override
    public MateriaStack getMateriaInTank() {
        if (this.storedType == null || this.storedAmount <= 0) return MateriaFumusStack.EMPTY;
        return new MateriaFumusStack(this.storedType, this.storedAmount);
    }

    @Override
    public int getSafeCapacity() {
        return 100;
    }

    @Override
    public int getAbsoluteCapacity() {
        return 300;
    }

    // --- NBT and Synchronization ---
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.storedType != null) {
            output.putInt("StoredType", this.storedType.ordinal());
        }
        output.putInt("StoredAmount", this.storedAmount);
        output.putInt("CooldownTicks", this.cooldownTicks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (input.getInt("StoredType").isPresent()) {
            int ord = input.getInt("StoredType").get();
            this.storedType = EssenceType.values()[ord % EssenceType.values().length];
        } else {
            this.storedType = null;
        }
        this.storedAmount = input.getIntOr("StoredAmount", 0);
        this.cooldownTicks = input.getIntOr("CooldownTicks", 0);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) {
        return this.saveWithoutMetadata(p);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
