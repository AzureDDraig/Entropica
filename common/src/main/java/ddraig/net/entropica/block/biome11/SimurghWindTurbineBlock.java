package ddraig.net.entropica.block.biome11;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Simurgh Wind-Turbine — vertical Savonius feather wind-catcher tuned to Storm Materia.
 * Outputs a continuous Redstone signal (0..15) scaling with sky exposure, altitude, weather,
 * and Storm-Swept Icebergs gale winds.
 */
public class SimurghWindTurbineBlock extends Block {
    public static final MapCodec<SimurghWindTurbineBlock> CODEC = simpleCodec(SimurghWindTurbineBlock::new);
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    private static final int UPDATE_INTERVAL_TICKS = 40;

    public SimurghWindTurbineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 8));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(POWER);
    }

    public static int computeWindPower(Level level, BlockPos pos) {
        if (!level.canSeeSky(pos.above())) {
            return Math.max(2, Math.min(6, (pos.getY() - 50) / 16));
        }
        int y = pos.getY();
        int p = (y >= 126) ? 13 : (y >= 96) ? 11 : (y >= 70) ? 8 : 5;
        if (level.isThundering()) {
            p = 15;
        } else if (level.isRaining()) {
            p = Math.min(15, p + 3);
        }
        if (level.getBiome(pos).unwrapKey()
                .map(k -> "storm_swept_icebergs".equals(k.location().getPath()))
                .orElse(false)) {
            p = Math.min(15, p + 2);
        }
        return Math.max(0, Math.min(15, p));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int power = computeWindPower(context.getLevel(), context.getClickedPos());
        return this.defaultBlockState().setValue(POWER, power);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            int newPower = computeWindPower(level, pos);
            if (newPower != state.getValue(POWER)) {
                level.setBlock(pos, state.setValue(POWER, newPower), 3);
            }
            level.updateNeighborsAt(pos, this);
            if (!level.getBlockTicks().willTickThisTick(pos, this)) {
                level.scheduleTick(pos, this, UPDATE_INTERVAL_TICKS);
            }
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        refreshPowerAndReschedule(state, level, pos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        refreshPowerAndReschedule(state, level, pos);
    }

    private void refreshPowerAndReschedule(BlockState state, ServerLevel level, BlockPos pos) {
        int oldPower = state.getValue(POWER);
        int newPower = computeWindPower(level, pos);
        if (newPower != oldPower) {
            level.setBlock(pos, state.setValue(POWER, newPower), 3);
            level.updateNeighborsAt(pos, this);
        }
        if (level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 48.0D, false) != null
                && !level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, UPDATE_INTERVAL_TICKS);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!isMoving) {
            level.updateNeighborsAt(pos, this);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        int power = state.getValue(POWER);
        if (power <= 0) {
            return;
        }

        int chance = Math.max(1, 6 - (power / 3));
        if (random.nextInt(chance) == 0) {
            double angle = (level.getGameTime() * 0.25D) + random.nextDouble() * Math.PI * 2.0D;
            double radius = 0.34D + random.nextDouble() * 0.1D;
            double x = pos.getX() + 0.5D + Math.cos(angle) * radius;
            double y = pos.getY() + 0.35D + random.nextDouble() * 0.5D;
            double z = pos.getZ() + 0.5D + Math.sin(angle) * radius;
            double speed = 0.015D + (power / 15.0D) * 0.035D;
            double vx = -Math.sin(angle) * speed;
            double vz = Math.cos(angle) * speed;
            level.addParticle(ParticleTypes.CLOUD, x, y, z, vx, 0.01D, vz);

            if (power >= 12 && random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y + 0.1D, z, vx * 1.4D, 0.02D, vz * 1.4D);
            }
        }
    }
}
