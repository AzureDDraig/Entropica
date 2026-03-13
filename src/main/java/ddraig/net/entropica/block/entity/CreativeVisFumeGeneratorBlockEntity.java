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

public class CreativeVisFumeGeneratorBlockEntity extends BlockEntity {

    // Safely default to an existing Essence to prevent null crashes
    private EssenceType currentType = EssenceType.values()[0];

    public CreativeVisFumeGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_VIS_FUME_GENERATOR_BE.get(), pos, state);
    }

    public EssenceType getCurrentType() {
        return this.currentType;
    }

    public void cycleType() {
        cycleType(1);
    }

    public void cycleType(int delta) {
        EssenceType[] types = EssenceType.values();
        int nextOrdinal = (this.currentType.ordinal() + delta) % types.length;

        if (nextOrdinal < 0) {
            nextOrdinal += types.length;
        }

        this.currentType = types[nextOrdinal];

        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        if (level.getGameTime() % EntropicaConfig.VIS_FUME_TICK_RATE.get() != 0) return;

        int amountToPush = EntropicaConfig.VIS_FUME_TRANSFER_RATE.get();

        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));

            if (neighbor instanceof IFumeHandler handler) {
                int currentAmount = handler.getFumeInTank().getAmount();
                int safeLimit = handler.getSafeCapacity();

                if (currentAmount < safeLimit) {
                    int spaceToFill = safeLimit - currentAmount;
                    int pushAmount = Math.min(spaceToFill, amountToPush);

                    if (pushAmount > 0) {
                        VisFumeStack pushStack = new VisFumeStack(this.currentType, pushAmount);
                        handler.fill(pushStack, false);
                    }
                }
            }
        }
    }

    // --- RESTORED FLAWLESS VALUE INPUT/OUTPUT LOGIC ---
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