package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.starlight.IWirelessFluxReceiver;
import ddraig.net.entropica.block.IlluminatedBalustradeBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class IlluminatedBalustradeBlockEntity extends BlockEntity implements IWirelessFluxReceiver {

    private String activeStarName = "Astral";
    private EssenceType activeEssence = EssenceType.ASTRAL;
    private int pulseTicksRemaining = 0;

    public IlluminatedBalustradeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ILLUMINATED_BALUSTRADE_BE.get(), pos, state);
    }

    @Override
    public void receiveFluxPulse(String starName, EssenceType essence) {
        this.activeStarName = starName != null ? starName : "Astral";
        this.activeEssence = essence != null ? essence : EssenceType.ASTRAL;
        this.pulseTicksRemaining = 35;
        setChanged();

        if (level != null && !level.isClientSide()) {
            BlockState current = getBlockState();
            if (current.hasProperty(IlluminatedBalustradeBlock.LIT) && !current.getValue(IlluminatedBalustradeBlock.LIT)) {
                level.setBlock(worldPosition, current.setValue(IlluminatedBalustradeBlock.LIT, true), 3);
            }

            // Propagate to adjacent balustrades (horizontal and along stairs up/down)
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos[] targets = new BlockPos[]{
                    worldPosition.relative(dir),
                    worldPosition.relative(dir).above(),
                    worldPosition.relative(dir).below()
                };
                for (BlockPos neighbor : targets) {
                    BlockState nState = level.getBlockState(neighbor);
                    if (nState.is(current.getBlock())) {
                        BlockEntity nBe = level.getBlockEntity(neighbor);
                        if (nBe instanceof IlluminatedBalustradeBlockEntity other && !other.isFluxPowered()) {
                            other.receiveFluxPulse(starName, essence);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isFluxPowered() {
        if (pulseTicksRemaining > 0) return true;
        BlockState state = getBlockState();
        return state.hasProperty(IlluminatedBalustradeBlock.LIT) && state.getValue(IlluminatedBalustradeBlock.LIT);
    }

    @Override
    public int getPulseTicksRemaining() {
        return pulseTicksRemaining;
    }

    @Override
    public BlockPos getReceiverPos() {
        return worldPosition;
    }

    public String getActiveStarName() {
        return activeStarName;
    }

    public EssenceType getActiveEssence() {
        return activeEssence;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, IlluminatedBalustradeBlockEntity be) {
        if (be.pulseTicksRemaining > 0) {
            be.pulseTicksRemaining--;

            if (be.pulseTicksRemaining <= 0 && !level.isClientSide()) {
                if (state.hasProperty(IlluminatedBalustradeBlock.LIT) && state.getValue(IlluminatedBalustradeBlock.LIT)) {
                    level.setBlock(pos, state.setValue(IlluminatedBalustradeBlock.LIT, false), 3);
                }
                be.setChanged();
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("ActiveStarName", Codec.STRING, this.activeStarName);
        output.store("ActiveEssence", Codec.STRING, this.activeEssence.name());
        output.store("PulseTicksRemaining", Codec.INT, this.pulseTicksRemaining);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.activeStarName = input.read("ActiveStarName", Codec.STRING).orElse("Astral");
        String essStr = input.read("ActiveEssence", Codec.STRING).orElse("ASTRAL");
        try {
            this.activeEssence = EssenceType.valueOf(essStr);
        } catch (Exception e) {
            this.activeEssence = EssenceType.ASTRAL;
        }
        this.pulseTicksRemaining = input.read("PulseTicksRemaining", Codec.INT).orElse(0);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("ActiveStarName", activeStarName);
        tag.putString("ActiveEssence", activeEssence.name());
        tag.putInt("PulseTicksRemaining", pulseTicksRemaining);
        return tag;
    }
}
