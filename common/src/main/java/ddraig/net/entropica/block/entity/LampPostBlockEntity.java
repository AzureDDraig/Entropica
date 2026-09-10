package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.block.CagedOpticBulbBlock;
import ddraig.net.entropica.block.LampPostBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class LampPostBlockEntity extends BlockEntity {
    private String activeStarName = "";
    private boolean isLit = false;
    private int litTicksRemaining = 0;

    public LampPostBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAMP_POST_BE.get(), pos, state);
    }

    public void receiveFiberPulse(String starName) {
        String newStar = (starName != null && !starName.isEmpty()) ? starName : "Astral";
        boolean starChanged = !this.activeStarName.equals(newStar);
        boolean litChanged = !this.isLit;

        this.activeStarName = newStar;
        this.litTicksRemaining = 25; // 1.25s pulse decay buffer
        this.isLit = true;

        if (level != null && !level.isClientSide()) {
            BlockState current = getBlockState();
            boolean stateUpdated = false;
            if (current.hasProperty(LampPostBlock.LIT) && !current.getValue(LampPostBlock.LIT)) {
                level.setBlock(worldPosition, current.setValue(LampPostBlock.LIT, true), 3);
                stateUpdated = true;
            }
            if (litChanged || starChanged || stateUpdated) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LampPostBlockEntity be) {
        if (level.isClientSide()) {
            boolean blockLit = state.hasProperty(LampPostBlock.LIT) && state.getValue(LampPostBlock.LIT);
            if (blockLit) {
                be.isLit = true;
            }
            return;
        }

        if (be.litTicksRemaining > 0) {
            be.litTicksRemaining--;
            if (be.litTicksRemaining == 0 && be.isLit) {
                be.isLit = false;
                if (state.hasProperty(LampPostBlock.LIT) && state.getValue(LampPostBlock.LIT)) {
                    level.setBlock(pos, state.setValue(LampPostBlock.LIT, false), 3);
                }
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }

    public boolean isLit() {
        return isLit;
    }

    public String getActiveStarName() {
        return (activeStarName != null && !activeStarName.isEmpty()) ? activeStarName : "Astral";
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("ActiveStarName", Codec.STRING, this.activeStarName);
        output.store("IsLit", Codec.BOOL, this.isLit);
        output.store("LitTicksRemaining", Codec.INT, this.litTicksRemaining);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("ActiveStarName", Codec.STRING).ifPresent(s -> this.activeStarName = s);
        input.read("IsLit", Codec.BOOL).ifPresent(b -> this.isLit = b);
        input.read("LitTicksRemaining", Codec.INT).ifPresent(t -> this.litTicksRemaining = t);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
