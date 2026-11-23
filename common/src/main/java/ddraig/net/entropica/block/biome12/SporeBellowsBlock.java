package ddraig.net.entropica.block.biome12;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Spore Bellows — sculpted Spore-Wood, fungal accordion sac, and copper-nozzled spore jet device.
 * Right-clicking or pulsing with Redstone fires a 5-block golden spore jet that pushes back
 * hostile monsters with Slowness II + Weakness I and fertilizes crops, saplings, moss, and snow.
 */
public class SporeBellowsBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SporeBellowsBlock> CODEC = simpleCodec(SporeBellowsBlock::new);
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

    protected static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(2.0D, 0.0D, 2.5D, 14.0D, 12.0D, 16.0D),
            Block.box(5.0D, 4.25D, 0.0D, 11.0D, 8.75D, 4.5D)
    );
    protected static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(2.0D, 0.0D, 0.0D, 14.0D, 12.0D, 13.5D),
            Block.box(5.0D, 4.25D, 11.5D, 11.0D, 8.75D, 16.0D)
    );
    protected static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(2.5D, 0.0D, 2.0D, 16.0D, 12.0D, 14.0D),
            Block.box(0.0D, 4.25D, 5.0D, 4.5D, 8.75D, 11.0D)
    );
    protected static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(0.0D, 0.0D, 2.0D, 13.5D, 12.0D, 14.0D),
            Block.box(11.5D, 4.25D, 5.0D, 16.0D, 8.75D, 11.0D)
    );

    public SporeBellowsBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TRIGGERED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction dir = context.getHorizontalDirection().getOpposite();
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, dir)
                .setValue(TRIGGERED, powered);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (!state.getValue(TRIGGERED)) {
                serverLevel.setBlock(pos, state.setValue(TRIGGERED, true), 3);
                puffSporeJet(serverLevel, pos, state.getValue(FACING));
                serverLevel.scheduleTick(pos, this, 10);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   @Nullable Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, isMoving);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            boolean hasSignal = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
            boolean triggered = state.getValue(TRIGGERED);
            if (hasSignal && !triggered) {
                serverLevel.setBlock(pos, state.setValue(TRIGGERED, true), 3);
                puffSporeJet(serverLevel, pos, state.getValue(FACING));
                serverLevel.scheduleTick(pos, this, 10);
            } else if (!hasSignal && triggered && !serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
                serverLevel.setBlock(pos, state.setValue(TRIGGERED, false), 3);
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(TRIGGERED) && !level.hasNeighborSignal(pos) && !level.hasNeighborSignal(pos.above())) {
            level.setBlock(pos, state.setValue(TRIGGERED, false), 3);
        }
    }

    public static void puffSporeJet(ServerLevel level, BlockPos pos, Direction dir) {
        level.playSound(null, pos, SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.BLOCKS, 0.85F, 1.15F);
        level.playSound(null, pos, SoundEvents.HORSE_BREATHE, SoundSource.BLOCKS, 0.9F, 0.85F);

        for (int i = 1; i <= 5; i++) {
            BlockPos stepPos = pos.relative(dir, i);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    stepPos.getX() + 0.5D, stepPos.getY() + 0.45D, stepPos.getZ() + 0.5D,
                    6, 0.28D, 0.22D, 0.28D, 0.03D);
            level.sendParticles(ParticleTypes.CLOUD,
                    stepPos.getX() + 0.5D, stepPos.getY() + 0.45D, stepPos.getZ() + 0.5D,
                    2, 0.2D, 0.15D, 0.2D, 0.04D);

            for (BlockPos checkPos : new BlockPos[]{stepPos, stepPos.below()}) {
                BlockState checkState = level.getBlockState(checkPos);
                if (checkState.getBlock() instanceof BonemealableBlock bonemealable) {
                    if (bonemealable.isValidBonemealTarget(level, checkPos, checkState)
                            && level.random.nextFloat() < 0.35F
                            && bonemealable.isBonemealSuccess(level, level.random, checkPos, checkState)) {
                        bonemealable.performBonemeal(level, level.random, checkPos, checkState);
                    }
                } else if ((checkState.is(Blocks.SNOW_BLOCK) || checkState.is(Blocks.DIRT))
                        && ModBlocks.FROZEN_SPORE_MOSS.isBound()) {
                    BlockPos aboveCheck = checkPos.above();
                    if (!level.getBlockState(aboveCheck).isCollisionShapeFullBlock(level, aboveCheck)
                            && level.random.nextFloat() < 0.15F) {
                        level.setBlock(checkPos, ModBlocks.FROZEN_SPORE_MOSS.get().defaultBlockState(), 3);
                    }
                }
            }
        }

        BlockPos startPos = pos.relative(dir, 1);
        BlockPos endPos = pos.relative(dir, 5);
        double expandX = dir.getAxis() == Direction.Axis.X ? 0.0D : 0.75D;
        double expandZ = dir.getAxis() == Direction.Axis.Z ? 0.0D : 0.75D;
        AABB corridor = new AABB(
                Math.min(startPos.getX(), endPos.getX()),
                startPos.getY(),
                Math.min(startPos.getZ(), endPos.getZ()),
                Math.max(startPos.getX(), endPos.getX()) + 1.0D,
                endPos.getY() + 1.0D,
                Math.max(startPos.getZ(), endPos.getZ()) + 1.0D
        ).inflate(expandX, 0.5D, expandZ);

        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, corridor, Entity::isAlive);
        for (Monster monster : monsters) {
            Vec3 cur = monster.getDeltaMovement();
            monster.setDeltaMovement(
                    cur.x + dir.getStepX() * 0.7D,
                    Math.max(cur.y, 0.0D) + 0.2D,
                    cur.z + dir.getStepZ() * 0.7D
            );
            monster.hurtMarked = true;
            monster.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
            monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
        }
    }
}
