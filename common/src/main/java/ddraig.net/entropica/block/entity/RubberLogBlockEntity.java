package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Random;

public class RubberLogBlockEntity extends BlockEntity {

    private float spotOffsetX = 0.5f;
    private float spotOffsetY = 0.5f;

    public RubberLogBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RUBBER_LOG_BE.get(), pos, state);
        this.generateRandomSpotOffset();
    }

    public void generateRandomSpotOffset() {
        // Deterministic position seed based on world position
        long seed = this.worldPosition != null ? this.worldPosition.asLong() : 12345L;
        Random rand = new Random(seed);
        this.spotOffsetX = 0.15f + rand.nextFloat() * 0.70f;
        this.spotOffsetY = 0.15f + rand.nextFloat() * 0.70f;
        this.setChanged();
    }

    public float getSpotOffsetX() {
        return spotOffsetX;
    }

    public float getSpotOffsetY() {
        return spotOffsetY;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("SpotX", this.spotOffsetX);
        output.putFloat("SpotY", this.spotOffsetY);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.spotOffsetX = input.getFloatOr("SpotX", 0.5f);
        this.spotOffsetY = input.getFloatOr("SpotY", 0.5f);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
