package ddraig.net.entropica.block.biome7;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Static Conductive Mat — low-profile 2px copper-and-quill grounding pad.
 * Discharges accumulated Static Charge when stepped on and outputs a full Level 15
 * Redstone signal for 20 ticks while occupied.
 */
public class StaticConductiveMatBlock extends Block {
    public static final MapCodec<StaticConductiveMatBlock> CODEC = simpleCodec(StaticConductiveMatBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    private static final int RESET_TICKS = 20;

    public StaticConductiveMatBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        activateGroundingPad(level, pos, state, entity);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
        activateGroundingPad(level, pos, state, entity);
    }

    private void activateGroundingPad(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity == null || entity.isSpectator()) {
            return;
        }
        StaticChargeHelper.clearStaticCharge(entity);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (!state.getValue(POWERED)) {
                serverLevel.setBlock(pos, state.setValue(POWERED, true), 3);
                updateNeighbours(serverLevel, pos);
                serverLevel.playSound(null, pos, SoundEvents.COPPER_STEP, SoundSource.BLOCKS, 0.6F, 1.35F);
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.getX() + 0.5D, pos.getY() + 0.16D, pos.getZ() + 0.5D,
                        6, 0.25D, 0.05D, 0.25D, 0.04D);
            }
            if (!serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
                serverLevel.scheduleTick(pos, this, RESET_TICKS);
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        AABB padCheckBox = new AABB(
                pos.getX() + 0.0625D, pos.getY(), pos.getZ() + 0.0625D,
                pos.getX() + 0.9375D, pos.getY() + 0.5D, pos.getZ() + 0.9375D
        );
        List<Entity> standing = level.getEntitiesOfClass(Entity.class, padCheckBox,
                e -> e.isAlive() && !e.isSpectator() && !e.isIgnoringBlockTriggers());

        if (!standing.isEmpty()) {
            for (Entity e : standing) {
                StaticChargeHelper.clearStaticCharge(e);
            }
            level.scheduleTick(pos, this, RESET_TICKS);
        } else if (state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, false), 3);
            updateNeighbours(level, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!isMoving && state.getValue(POWERED)) {
            updateNeighbours(level, pos);
        }
    }

    private void updateNeighbours(Level level, BlockPos pos) {
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.below(), this);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return (state.getValue(POWERED) && direction == Direction.UP) ? 15 : 0;
    }
}
