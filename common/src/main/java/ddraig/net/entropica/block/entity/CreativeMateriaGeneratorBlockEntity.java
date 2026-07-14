package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.IVaporHandler;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.api.materia.MateriaLiquidaStack;
import ddraig.net.entropica.api.materia.MateriaVolatilisStack;
import ddraig.net.entropica.api.materia.MateriaCoagulataStack;
import ddraig.net.entropica.api.materia.MateriaIchorStack;
import ddraig.net.entropica.api.materia.MateriaTransmutataStack;
import ddraig.net.entropica.api.materia.MateriaPerfectaStack;
import ddraig.net.entropica.api.materia.MateriaLiminaliaStack;
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

public class CreativeMateriaGeneratorBlockEntity extends BlockEntity {

    // Safely default to an existing Essence to prevent null crashes
    private EssenceType currentType = EssenceType.values()[0];

    public CreativeMateriaGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_MATERIA_GENERATOR_BE.get(), pos, state);
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

        if (level.getGameTime() % EntropicaConfig.MATERIA_FUMUS_TICK_RATE.get() != 0) return;

        int amountToPush = EntropicaConfig.MATERIA_FUMUS_TRANSFER_RATE.get();

        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));

            if (neighbor instanceof IVaporHandler handler) {
                int currentAmount = handler.getMateriaInTank().getAmount();
                int safeLimit = handler.getSafeCapacity();

                if (currentAmount < safeLimit) {
                    int spaceToFill = safeLimit - currentAmount;
                    int pushAmount = Math.min(spaceToFill, amountToPush);

                    if (pushAmount > 0) {
                        MateriaFumusStack pushStack = new MateriaFumusStack(this.currentType, pushAmount);
                        handler.fill(pushStack, false);
                    }
                }
            } else if (neighbor instanceof HydraulicPipelineBlockEntity pipeline) {
                int currentAmount = pipeline.getLiquidInTank().getAmount();
                int limit = pipeline.getCapacity();

                if (currentAmount < limit) {
                    int spaceToFill = limit - currentAmount;
                    int pushAmount = Math.min(spaceToFill, amountToPush);

                    if (pushAmount > 0) {
                        MateriaLiquidaStack pushStack = new MateriaLiquidaStack(this.currentType, pushAmount);
                        pipeline.fill(pushStack, false);
                    }
                }
            } else if (neighbor instanceof VoltaicConduitBlockEntity conduit) {
                int currentAmount = conduit.getVolatilisInTank().getAmount();
                int limit = conduit.getCapacity();

                if (currentAmount < limit) {
                    int spaceToFill = limit - currentAmount;
                    int pushAmount = Math.min(spaceToFill, amountToPush);

                    if (pushAmount > 0) {
                        MateriaVolatilisStack pushStack = new MateriaVolatilisStack(this.currentType, pushAmount);
                        conduit.fill(pushStack, false);
                    }
                }
            } else if (neighbor instanceof ViscousAgitatorBlockEntity agitator) {
                int currentAmount = agitator.getCoagulataInTank().getAmount();
                int limit = agitator.getCapacity();

                if (currentAmount < limit) {
                    int spaceToFill = limit - currentAmount;
                    int pushAmount = Math.min(spaceToFill, amountToPush);

                    if (pushAmount > 0) {
                        MateriaCoagulataStack pushStack = new MateriaCoagulataStack(this.currentType, pushAmount);
                        agitator.fill(pushStack, false);
                    }
                }
            } else if (neighbor instanceof SanguineConduitBlockEntity conduit) {
                int currentAmount = conduit.getIchorInTank().getAmount();
                int limit = conduit.getCapacity();

                if (currentAmount < limit) {
                    int spaceToFill = limit - currentAmount;
                    int pushAmount = Math.min(spaceToFill, amountToPush);

                    if (pushAmount > 0) {
                        MateriaIchorStack pushStack = new MateriaIchorStack(this.currentType, pushAmount);
                        conduit.fill(pushStack, false);
                    }
                }
            } else if (neighbor instanceof EquilibriumConduitBlockEntity conduit) {
                int currentAmount = conduit.getTransmutataInTank().getAmount();
                int limit = conduit.getCapacity();

                if (currentAmount < limit) {
                    int spaceToFill = limit - currentAmount;
                    int pushAmount = Math.min(spaceToFill, amountToPush);

                    if (pushAmount > 0) {
                        MateriaTransmutataStack pushStack = new MateriaTransmutataStack(this.currentType, pushAmount);
                        conduit.fill(pushStack, false);
                    }
                }
            } else if (neighbor instanceof PristineConduitBlockEntity conduit) {
                int currentAmount = conduit.getPerfectaInTank().getAmount();
                int limit = conduit.getCapacity();

                if (currentAmount < limit) {
                    int spaceToFill = limit - currentAmount;
                    int pushAmount = Math.min(spaceToFill, amountToPush);

                    if (pushAmount > 0) {
                        MateriaPerfectaStack pushStack = new MateriaPerfectaStack(this.currentType, pushAmount);
                        conduit.fill(pushStack, false);
                    }
                }
            } else if (neighbor instanceof AthanorConduitBlockEntity conduit) {
                int currentAmount = conduit.getLiminaliaInTank().getAmount();
                int limit = conduit.getCapacity();

                if (currentAmount < limit) {
                    int spaceToFill = limit - currentAmount;
                    int pushAmount = Math.min(spaceToFill, amountToPush);

                    if (pushAmount > 0) {
                        MateriaLiminaliaStack pushStack = new MateriaLiminaliaStack(this.currentType, pushAmount);
                        conduit.fill(pushStack, false);
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