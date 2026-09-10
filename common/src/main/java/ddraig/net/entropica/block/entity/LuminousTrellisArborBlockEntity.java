package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.starlight.IWirelessFluxReceiver;
import ddraig.net.entropica.block.LuminousTrellisArborBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class LuminousTrellisArborBlockEntity extends BlockEntity implements IWirelessFluxReceiver {

    private String activeStarName = "Astral";
    private EssenceType activeEssence = EssenceType.ASTRAL;
    private int pulseTicksRemaining = 0;

    public LuminousTrellisArborBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LUMINOUS_TRELLIS_ARBOR_BE.get(), pos, state);
    }

    @Override
    public void receiveFluxPulse(String starName, EssenceType essence) {
        this.activeStarName = starName != null ? starName : "Astral";
        this.activeEssence = essence != null ? essence : EssenceType.ASTRAL;
        this.pulseTicksRemaining = 35;
        setChanged();

        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            if (state.hasProperty(LuminousTrellisArborBlock.LIT) && !state.getValue(LuminousTrellisArborBlock.LIT)) {
                level.setBlock(worldPosition, state.setValue(LuminousTrellisArborBlock.LIT, true), 3);
            }
            // Propagate to other half if double block
            if (state.hasProperty(LuminousTrellisArborBlock.HALF)) {
                BlockPos otherPos = state.getValue(LuminousTrellisArborBlock.HALF) == DoubleBlockHalf.LOWER ? worldPosition.above() : worldPosition.below();
                BlockState otherState = level.getBlockState(otherPos);
                if (otherState.is(state.getBlock()) && otherState.hasProperty(LuminousTrellisArborBlock.LIT) && !otherState.getValue(LuminousTrellisArborBlock.LIT)) {
                    level.setBlock(otherPos, otherState.setValue(LuminousTrellisArborBlock.LIT, true), 3);
                }
            }
        }
    }

    @Override
    public boolean isFluxPowered() {
        if (pulseTicksRemaining > 0) return true;
        BlockState state = getBlockState();
        return state.hasProperty(LuminousTrellisArborBlock.LIT) && state.getValue(LuminousTrellisArborBlock.LIT);
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

    public static void tick(Level level, BlockPos pos, BlockState state, LuminousTrellisArborBlockEntity be) {
        if (be.pulseTicksRemaining > 0) {
            be.pulseTicksRemaining--;

            if (level.isClientSide()) {
                if (level.random.nextInt(6) == 0) {
                    double px = pos.getX() + 0.1 + level.random.nextDouble() * 0.8;
                    double py = pos.getY() + 0.5 + level.random.nextDouble() * 0.5;
                    double pz = pos.getZ() + 0.1 + level.random.nextDouble() * 0.8;
                    level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), px, py, pz, 0, -0.01, 0);
                }
            }

            if (be.pulseTicksRemaining <= 0 && !level.isClientSide()) {
                if (state.hasProperty(LuminousTrellisArborBlock.LIT) && state.getValue(LuminousTrellisArborBlock.LIT)) {
                    level.setBlock(pos, state.setValue(LuminousTrellisArborBlock.LIT, false), 3);
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
