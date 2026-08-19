package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AstralCollectorBlockEntity extends BlockEntity {

    public static final int MAX_CAPACITY = 2000;

    private int storedMateria = 0;
    private EssenceType storedEssence = EssenceType.ASTRAL;
    private boolean isIrradiated = false;
    private int irradiationTicksLeft = 0;

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

    public void receiveStarlightBeam(String starName, EssenceType incomingEssence) {
        if (incomingEssence == null) {
            incomingEssence = EssenceType.ASTRAL;
        }

        this.isIrradiated = true;
        this.irradiationTicksLeft = 10;

        if (this.storedMateria == 0) {
            this.storedEssence = incomingEssence;
        }

        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AstralCollectorBlockEntity be) {
        if (be.irradiationTicksLeft > 0) {
            be.irradiationTicksLeft--;
            be.isIrradiated = true;
        } else {
            be.isIrradiated = false;
        }

        if (level.isClientSide()) {
            return;
        }

        boolean changed = false;

        // Focused Optical Beam Collection: configurable rate (default 8 Materia per second, i.e. 2 Materia every 5 ticks)
        if (be.isIrradiated) {
            if (be.storedMateria < MAX_CAPACITY) {
                int ratePerSec = Math.max(1, ddraig.net.entropica.config.EntropicaConfig.ASTRAL_COLLECTOR_TRANSFER_RATE.get());
                int gainPerInterval = Math.max(1, ratePerSec / 4);
                if (level.getGameTime() % 5 == 0) {
                    int prevMateria = be.storedMateria;
                    be.storedMateria = Math.min(MAX_CAPACITY, be.storedMateria + gainPerInterval);
                    changed = true;
                    if (prevMateria < MAX_CAPACITY && be.storedMateria >= MAX_CAPACITY) {
                        // Full Capacity Resonant Harmonic Chime & Particle Burst
                        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.8f);
                        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 2.0f);
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.85, pos.getZ() + 0.5, 24, 0.35, 0.2, 0.35, 0.06);
                        }
                    }
                }
            }

            if (level instanceof ServerLevel serverLevel && level.getGameTime() % 8 == 0) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.85, pos.getZ() + 0.5, 1, 0.1, 0.05, 0.1, 0.01);
            }
        }

        if (changed || level.getGameTime() % 20 == 0) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
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
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) {
        CompoundTag tag = super.getUpdateTag(p);
        tag.putInt("StoredMateria", this.storedMateria);
        tag.putString("StoredEssence", this.storedEssence != null ? this.storedEssence.name() : "ASTRAL");
        tag.putBoolean("IsIrradiated", this.isIrradiated);
        tag.putInt("IrradiationTicksLeft", this.irradiationTicksLeft);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.storedMateria = tag.getInt("StoredMateria").orElse(0);
            String essStr = tag.getString("StoredEssence").orElse("ASTRAL");
            try {
                this.storedEssence = EssenceType.valueOf(essStr);
            } catch (Exception ignored) {
                this.storedEssence = EssenceType.ASTRAL;
            }
            this.isIrradiated = tag.getBoolean("IsIrradiated").orElse(false);
            this.irradiationTicksLeft = tag.getInt("IrradiationTicksLeft").orElse(0);
        }
    }
}
