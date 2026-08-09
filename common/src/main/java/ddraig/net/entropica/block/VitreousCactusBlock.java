package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VitreousCactusBlock extends EntropicaFlowerBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;

    // Full Cuboid Bounding Box
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public VitreousCactusBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(this) 
            || super.mayPlaceOn(state, level, pos) 
            || state.is(Blocks.SAND) 
            || state.is(Blocks.RED_SAND) 
            || state.is(Blocks.TERRACOTTA) 
            || state.is(Blocks.GRAVEL) 
            || state.is(Blocks.CACTUS);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // 1. Cannot have solid blocks directly adjacent horizontally (North, South, East, West)
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState neighborState = level.getBlockState(pos.relative(direction));
            if (neighborState.isSolid() || level.getFluidState(pos.relative(direction)).is(FluidTags.LAVA)) {
                return false;
            }
        }
        // 2. Substrate check below
        BlockState belowState = level.getBlockState(pos.below());
        return belowState.is(this) || this.mayPlaceOn(belowState, level, pos.below());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelReader level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            if (level instanceof ScheduledTickAccess tickAccess) {
                tickAccess.scheduleTick(pos, this, 1);
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos abovePos = pos.above();
        if (level.isEmptyBlock(abovePos)) {
            // Count height of Vitreous Cactus blocks below
            int height = 1;
            while (level.getBlockState(pos.below(height)).is(this)) {
                height++;
            }

            // Grow up to a maximum height of 5 blocks
            if (height < 5) {
                int age = state.getValue(AGE);
                if (age == 15) {
                    level.setBlockAndUpdate(abovePos, this.defaultBlockState());
                    level.setBlock(pos, state.setValue(AGE, 0), 4);
                } else {
                    level.setBlock(pos, state.setValue(AGE, age + 1), 4);
                }
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        
        // 1. Cactus Puncture Damage
        entity.hurt(level.damageSources().cactus(), 2.0F);

        // 2. Special Bleeding Effect (4-second Wither/Bleed debuff)
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0, false, true, true));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.7;
            double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.7;

            // Electric Light Cyan Glass Sparkles (#67E8F9)
            float r = 0.40f;
            float g = 0.91f;
            float b = 0.98f;

            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
        }
    }
}
