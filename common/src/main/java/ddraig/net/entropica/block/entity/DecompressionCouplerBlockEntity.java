package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.*;
import ddraig.net.entropica.block.*;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DecompressionCouplerBlockEntity extends BlockEntity {

    public DecompressionCouplerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECOMPRESSION_COUPLER_BE.get(), pos, state);
    }

    public static int getMateriaTier(BlockEntity be) {
        if (be instanceof VaporPneumaticPipeBlockEntity pipe) {
            MateriaStack stack = pipe.getMateriaInTank();
            if (stack instanceof MateriaSublimataStack) return 3;
            return 2; // Default to Fumus (T2)
        }
        if (be instanceof HydraulicPipelineBlockEntity) return 4;
        if (be instanceof VoltaicConduitBlockEntity) return 5;
        if (be instanceof ViscousAgitatorBlockEntity) return 6;
        if (be instanceof SanguineConduitBlockEntity) return 7;
        if (be instanceof EquilibriumConduitBlockEntity) return 8;
        if (be instanceof PristineConduitBlockEntity) return 9;
        if (be instanceof AthanorConduitBlockEntity) return 10;
        
        // Orbis Cells and Calixes
        if (be instanceof OrbisCellBlockEntity) return 2;
        if (be instanceof SublimatedOrbisCellBlockEntity) return 3;
        if (be instanceof PneumaticCalixBlockEntity) return 4;
        if (be instanceof VoltaicCalixBlockEntity) return 5;
        if (be instanceof MatrixCalixBlockEntity) return 6;
        if (be instanceof ThecaCellBlockEntity) return 7;
        if (be instanceof VasCellBlockEntity) return 8;
        if (be instanceof MonadCoreBlockEntity) return 9;
        if (be instanceof AthanorCoreBlockEntity) return 10;

        return 0;
    }

    private static MateriaStack drainMateria(BlockEntity be, int amount, boolean simulate) {
        if (be instanceof VaporPneumaticPipeBlockEntity pipe) {
            return pipe.drain(amount, simulate);
        }
        if (be instanceof HydraulicPipelineBlockEntity pipeline) {
            return pipeline.drainLiquid(amount, simulate);
        }
        if (be instanceof VoltaicConduitBlockEntity conduit) {
            return conduit.drain(amount, simulate);
        }
        if (be instanceof ViscousAgitatorBlockEntity agitator) {
            return agitator.drain(amount, simulate);
        }
        if (be instanceof SanguineConduitBlockEntity conduit) {
            return conduit.drain(amount, simulate);
        }
        if (be instanceof EquilibriumConduitBlockEntity conduit) {
            return conduit.drain(amount, simulate);
        }
        if (be instanceof PristineConduitBlockEntity conduit) {
            return conduit.drain(amount, simulate);
        }
        if (be instanceof AthanorConduitBlockEntity conduit) {
            return conduit.drain(amount, simulate);
        }
        return MateriaFumusStack.EMPTY;
    }

    public static int fillMateria(BlockEntity be, MateriaStack stack, boolean simulate) {
        if (be instanceof VaporPneumaticPipeBlockEntity pipe) {
            return pipe.fill(stack, simulate);
        }
        if (be instanceof HydraulicPipelineBlockEntity pipeline) {
            if (stack instanceof MateriaLiquidaStack liq) {
                return pipeline.fill(liq, simulate);
            }
        }
        if (be instanceof VoltaicConduitBlockEntity conduit) {
            if (stack instanceof MateriaVolatilisStack vol) {
                return conduit.fill(vol, simulate);
            }
        }
        if (be instanceof ViscousAgitatorBlockEntity agitator) {
            if (stack instanceof MateriaCoagulataStack coag) {
                return agitator.fill(coag, simulate);
            }
        }
        if (be instanceof SanguineConduitBlockEntity conduit) {
            if (stack instanceof MateriaIchorStack ich) {
                return conduit.fill(ich, simulate);
            }
        }
        if (be instanceof EquilibriumConduitBlockEntity conduit) {
            if (stack instanceof MateriaTransmutataStack trans) {
                return conduit.fill(trans, simulate);
            }
        }
        if (be instanceof PristineConduitBlockEntity conduit) {
            if (stack instanceof MateriaPerfectaStack perf) {
                return conduit.fill(perf, simulate);
            }
        }
        if (be instanceof AthanorConduitBlockEntity conduit) {
            if (stack instanceof MateriaLiminaliaStack lim) {
                return conduit.fill(lim, simulate);
            }
        }
        return 0;
    }

    private static MateriaStack convertToLowerTier(MateriaStack input, int highTier) {
        if (input.isEmpty()) return input;
        EssenceType type = input.getType();
        int amount = input.getAmount();

        return switch (highTier) {
            case 10 -> new MateriaPerfectaStack(type, amount * 5);
            case 9 -> new MateriaTransmutataStack(type, amount * 5);
            case 8 -> new MateriaIchorStack(type, amount * 5);
            case 7 -> new MateriaCoagulataStack(type, amount * 4);
            case 6 -> new MateriaVolatilisStack(type, amount * 16);
            case 5 -> new MateriaLiquidaStack(type, amount * 2);
            case 4 -> new MateriaSublimataStack(type, amount * 6);
            case 3 -> new MateriaFumusStack(type, amount * 360);
            default -> input;
        };
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        Direction dir1 = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        Direction dir2 = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);

        BlockEntity be1 = level.getBlockEntity(pos.relative(dir1));
        BlockEntity be2 = level.getBlockEntity(pos.relative(dir2));

        if (be1 == null || be2 == null) return;

        int tier1 = getMateriaTier(be1);
        int tier2 = getMateriaTier(be2);

        // If one of the endpoints is an empty VaporPneumaticPipeBlockEntity, it can act as either Tier 2 or Tier 3
        // depending on what the other side is, so we can start the flow!
        if (be1 instanceof VaporPneumaticPipeBlockEntity pipe1 && pipe1.getMateriaInTank().isEmpty()) {
            if (Math.abs(3 - tier2) == 1) {
                tier1 = 3;
            } else if (Math.abs(2 - tier2) == 1) {
                tier1 = 2;
            }
        }
        if (be2 instanceof VaporPneumaticPipeBlockEntity pipe2 && pipe2.getMateriaInTank().isEmpty()) {
            if (Math.abs(tier1 - 3) == 1) {
                tier2 = 3;
            } else if (Math.abs(tier1 - 2) == 1) {
                tier2 = 2;
            }
        }

        if (tier1 <= 0 || tier2 <= 0) return;
        if (Math.abs(tier1 - tier2) != 1) return;

        BlockEntity source = tier1 > tier2 ? be1 : be2;
        BlockEntity target = tier1 > tier2 ? be2 : be1;
        int highTier = Math.max(tier1, tier2);

        // Try to drain 1 unit of high-tier Materia (simulate first)
        MateriaStack drainedSim = drainMateria(source, 1, true);
        if (drainedSim.isEmpty() || drainedSim.getAmount() < 1) return;

        // Convert the 1 unit to the lower tier
        MateriaStack converted = convertToLowerTier(drainedSim, highTier);

        // Check if the target can accept the converted amount
        int accepted = fillMateria(target, converted, true);
        if (accepted < converted.getAmount()) return; // Only convert if the target has enough space!

        // Perform the actual transfer
        MateriaStack drainedActual = drainMateria(source, 1, false);
        if (!drainedActual.isEmpty() && drainedActual.getAmount() >= 1) {
            MateriaStack convertedActual = convertToLowerTier(drainedActual, highTier);
            fillMateria(target, convertedActual, false);
        }
    }
}
