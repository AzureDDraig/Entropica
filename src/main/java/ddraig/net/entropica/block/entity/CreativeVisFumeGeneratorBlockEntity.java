package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.fumes.IFumeHandler;
import ddraig.net.entropica.api.fumes.VisFumeStack;
import ddraig.net.entropica.config.EntropicaConfig;
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

/**
 * Represents a special block entity for the Creative Vis Fume Generator.
 * This block entity is responsible for generating and distributing vis fumes
 * of a certain essence type in the Minecraft world. The essence type can be
 * cycled through various predefined types.
 *
 * The Creative Vis Fume Generator allows interaction with other blocks
 * or entities via a fume handling interface and supports saving and syncing
 * its state both on the client and server side.
 */
public class CreativeVisFumeGeneratorBlockEntity extends BlockEntity {

    private EssenceType currentType = EssenceType.REGULAR;

    public CreativeVisFumeGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_VIS_FUME_GENERATOR_BE.get(), pos, state);
    }

    public EssenceType getCurrentType() {
        return this.currentType;
    }

    public void cycleType() {
        EssenceType[] types = EssenceType.values();
        int nextOrdinal = (this.currentType.ordinal() + 1) % types.length;
        this.currentType = types[nextOrdinal];

        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        // Keep it synchronized with the rest of the Vis Fume network ticks
        if (level.getGameTime() % EntropicaConfig.VIS_FUME_TICK_RATE.get() != 0) return;

        int amountToPush = EntropicaConfig.VIS_FUME_TRANSFER_RATE.get();

        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));

            if (neighbor instanceof IFumeHandler handler) {
                int currentAmount = handler.getFumeInTank().getAmount();

                // Only push if the neighbor is safely under the 200 explosion limit
                if (currentAmount < 200) {
                    // Calculate exactly how much space is left up to 200
                    int spaceToFill = 200 - currentAmount;

                    // Push the smaller value: either the normal transfer rate, or the exact space left
                    int pushAmount = Math.min(spaceToFill, amountToPush);

                    if (pushAmount > 0) {
                        VisFumeStack pushStack = new VisFumeStack(this.currentType, pushAmount);
                        handler.fill(pushStack, false);
                    }
                }
            }
        }
    }

    // --- SAVING & SYNCING ---
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("GeneratorEssenceType", this.currentType.ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int ord = input.getIntOr("GeneratorEssenceType", 0);
        this.currentType = EssenceType.values()[ord % EssenceType.values().length];
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