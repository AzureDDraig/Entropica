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
import ddraig.net.entropica.api.materia.MateriaStack;
import ddraig.net.entropica.api.materia.MateriaSublimataStack;
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

    private MateriaStack createMateriaStackForTier(int tier, EssenceType type, int amount) {
        return switch (tier) {
            case 2 -> new MateriaFumusStack(type, amount);
            case 3 -> new MateriaSublimataStack(type, amount);
            case 4 -> new MateriaLiquidaStack(type, amount);
            case 5 -> new MateriaVolatilisStack(type, amount);
            case 6 -> new MateriaCoagulataStack(type, amount);
            case 7 -> new MateriaIchorStack(type, amount);
            case 8 -> new MateriaTransmutataStack(type, amount);
            case 9 -> new MateriaPerfectaStack(type, amount);
            case 10 -> new MateriaLiminaliaStack(type, amount);
            default -> null;
        };
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        if (level.getGameTime() % EntropicaConfig.MATERIA_FUMUS_TICK_RATE.get() != 0) return;

        int amountToPush = EntropicaConfig.MATERIA_FUMUS_TRANSFER_RATE.get();

        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) continue;

            int tier = DecompressionCouplerBlockEntity.getMateriaTier(neighbor);
            if (tier == 0 && neighbor instanceof IVaporHandler) {
                tier = 2; // Fallback for other vapor handlers (vessels, chambers, etc.)
            }

            if (tier >= 2 && tier <= 10) {
                int currentAmount = 0;
                int limit = 0;

                if (neighbor instanceof VaporPneumaticPipeBlockEntity pipe) {
                    currentAmount = pipe.getMateriaInTank().getAmount();
                    limit = pipe.getSafeCapacity();
                } else if (neighbor instanceof HydraulicPipelineBlockEntity pipeline) {
                    currentAmount = pipeline.getLiquidInTank().getAmount();
                    limit = pipeline.getCapacity();
                } else if (neighbor instanceof VoltaicConduitBlockEntity conduit) {
                    currentAmount = conduit.getVolatilisInTank().getAmount();
                    limit = conduit.getCapacity();
                } else if (neighbor instanceof ViscousAgitatorBlockEntity agitator) {
                    currentAmount = agitator.getCoagulataInTank().getAmount();
                    limit = agitator.getCapacity();
                } else if (neighbor instanceof SanguineConduitBlockEntity conduit) {
                    currentAmount = conduit.getIchorInTank().getAmount();
                    limit = conduit.getCapacity();
                } else if (neighbor instanceof EquilibriumConduitBlockEntity conduit) {
                    currentAmount = conduit.getTransmutataInTank().getAmount();
                    limit = conduit.getCapacity();
                } else if (neighbor instanceof PristineConduitBlockEntity conduit) {
                    currentAmount = conduit.getPerfectaInTank().getAmount();
                    limit = conduit.getCapacity();
                } else if (neighbor instanceof AthanorConduitBlockEntity conduit) {
                    currentAmount = conduit.getLiminaliaInTank().getAmount();
                    limit = conduit.getCapacity();
                } else if (neighbor instanceof IVaporHandler handler) {
                    currentAmount = handler.getMateriaInTank().getAmount();
                    limit = handler.getSafeCapacity();
                }

                if (limit > 0 && currentAmount < limit) {
                    int spaceToFill = limit - currentAmount;
                    int pushAmount = Math.min(spaceToFill, amountToPush);

                    if (pushAmount > 0) {
                        MateriaStack pushStack = createMateriaStackForTier(tier, this.currentType, pushAmount);
                        if (pushStack != null) {
                            if (neighbor instanceof IVaporHandler handler) {
                                handler.fill(pushStack, false);
                            } else {
                                DecompressionCouplerBlockEntity.fillMateria(neighbor, pushStack, false);
                            }
                        }
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