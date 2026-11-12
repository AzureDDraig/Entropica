package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class OpticalBoosterAmplifierBlockEntity extends BlockEntity {

    private boolean isBoosting = false;
    private int boostMultiplier = 2;

    public OpticalBoosterAmplifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OPTICAL_BOOSTER_AMPLIFIER_BE.get(), pos, state);
    }

    public boolean isBoosting() {
        return isBoosting;
    }

    public int getBoostMultiplier() {
        return boostMultiplier;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OpticalBoosterAmplifierBlockEntity be) {
        // Ticks signal amplification
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("IsBoosting", Codec.BOOL, this.isBoosting);
        output.store("BoostMultiplier", Codec.INT, this.boostMultiplier);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("IsBoosting", Codec.BOOL).ifPresent(b -> this.isBoosting = b);
        input.read("BoostMultiplier", Codec.INT).ifPresent(m -> this.boostMultiplier = m);
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
