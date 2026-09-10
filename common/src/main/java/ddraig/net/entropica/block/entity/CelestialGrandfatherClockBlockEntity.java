package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.starlight.IWirelessFluxReceiver;
import ddraig.net.entropica.block.CelestialGrandfatherClockBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CelestialGrandfatherClockBlockEntity extends BlockEntity implements IWirelessFluxReceiver {

    private String activeStarName = "Astral";
    private EssenceType activeEssence = EssenceType.ASTRAL;
    private int pulseTicksRemaining = 0;
    private int animationTicks = 0;

    public CelestialGrandfatherClockBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CELESTIAL_GRANDFATHER_CLOCK_BE.get(), pos, state);
    }

    @Override
    public void receiveFluxPulse(String starName, EssenceType essence) {
        this.activeStarName = starName != null ? starName : "Astral";
        this.activeEssence = essence != null ? essence : EssenceType.ASTRAL;
        this.pulseTicksRemaining = 35;
        setChanged();

        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            if (state.hasProperty(CelestialGrandfatherClockBlock.LIT) && !state.getValue(CelestialGrandfatherClockBlock.LIT)) {
                level.setBlock(worldPosition, state.setValue(CelestialGrandfatherClockBlock.LIT, true), 3);
            }
            if (state.hasProperty(CelestialGrandfatherClockBlock.HALF)) {
                BlockPos otherPos = state.getValue(CelestialGrandfatherClockBlock.HALF) == DoubleBlockHalf.LOWER ? worldPosition.above() : worldPosition.below();
                BlockState otherState = level.getBlockState(otherPos);
                if (otherState.is(state.getBlock()) && otherState.hasProperty(CelestialGrandfatherClockBlock.LIT) && !otherState.getValue(CelestialGrandfatherClockBlock.LIT)) {
                    level.setBlock(otherPos, otherState.setValue(CelestialGrandfatherClockBlock.LIT, true), 3);
                }
            }
        }
    }

    @Override
    public boolean isFluxPowered() {
        if (pulseTicksRemaining > 0) return true;
        BlockState state = getBlockState();
        return state.hasProperty(CelestialGrandfatherClockBlock.LIT) && state.getValue(CelestialGrandfatherClockBlock.LIT);
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

    public int getAnimationTicks() {
        return animationTicks;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CelestialGrandfatherClockBlockEntity be) {
        be.animationTicks++;

        if (be.pulseTicksRemaining > 0) {
            be.pulseTicksRemaining--;

            if (be.pulseTicksRemaining <= 0 && !level.isClientSide()) {
                if (state.hasProperty(CelestialGrandfatherClockBlock.LIT) && state.getValue(CelestialGrandfatherClockBlock.LIT)) {
                    level.setBlock(pos, state.setValue(CelestialGrandfatherClockBlock.LIT, false), 3);
                }
                be.setChanged();
            }
        }

        // Ticking sounds & sunrise/sunset chimes
        if (state.hasProperty(CelestialGrandfatherClockBlock.HALF) && state.getValue(CelestialGrandfatherClockBlock.HALF) == DoubleBlockHalf.LOWER) {
            if (level.isClientSide()) {
                if (level.getGameTime() % 20 == 0) {
                    level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                            SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.25f, 1.6f, false);
                }
            } else {
                long dayTime = level.getDayTime() % 24000;
                if (dayTime == 0 || dayTime == 13000) {
                    level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 0.8f, 1.4f);
                }
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
