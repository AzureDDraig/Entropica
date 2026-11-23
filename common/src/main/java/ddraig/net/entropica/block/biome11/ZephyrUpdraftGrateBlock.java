package ddraig.net.entropica.block.biome11;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.item.biome11.StormKiteGliderItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Zephyr Updraft Grate — ducted Silver-Pine and Cut-Copper wind-lift shaft block.
 * Projects a continuous vertical column of buoyant wind (14 blocks unpowered, 22 blocks powered)
 * that lifts entities smoothly, boosts Storm-Kite Glider riders, and relays through stacked grates.
 */
public class ZephyrUpdraftGrateBlock extends Block {
    public static final MapCodec<ZephyrUpdraftGrateBlock> CODEC = simpleCodec(ZephyrUpdraftGrateBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ZephyrUpdraftGrateBlock(Properties properties) {
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
        return Shapes.block();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return this.defaultBlockState().setValue(POWERED, powered);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            boolean powered = level.hasNeighborSignal(pos);
            if (state.getValue(POWERED) != powered) {
                level.setBlock(pos, state.setValue(POWERED, powered), 3);
            }
            if (!level.getBlockTicks().willTickThisTick(pos, this)) {
                level.scheduleTick(pos, this, 2);
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   @Nullable Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, isMoving);
        if (!level.isClientSide()) {
            boolean powered = level.hasNeighborSignal(pos);
            if (state.getValue(POWERED) != powered) {
                level.setBlock(pos, state.setValue(POWERED, powered), 3);
            }
            if (!level.getBlockTicks().willTickThisTick(pos, this)) {
                level.scheduleTick(pos, this, 2);
            }
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        if (!entity.isShiftKeyDown()) {
            entity.resetFallDistance();
        } else {
            super.fallOn(level, state, pos, entity, fallDistance);
        }
        if (!level.isClientSide() && !level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!entity.isShiftKeyDown()) {
            entity.resetFallDistance();
        }
        if (!level.isClientSide() && !level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean powered = state.getValue(POWERED);
        int maxReach = powered ? 22 : 14;

        int clearHeight = 0;
        for (int dy = 1; dy <= maxReach; dy++) {
            BlockPos checkPos = pos.above(dy);
            BlockState checkState = level.getBlockState(checkPos);
            if (!(checkState.getBlock() instanceof ZephyrUpdraftGrateBlock)
                    && !checkState.getCollisionShape(level, checkPos).isEmpty()) {
                break;
            }
            clearHeight = dy;
        }

        if (clearHeight > 0) {
            AABB column = new AABB(
                    pos.getX() - 0.15D, pos.getY() + 1.0D, pos.getZ() - 0.15D,
                    pos.getX() + 1.15D, pos.getY() + 1.0D + clearHeight, pos.getZ() + 1.15D
            );
            List<Entity> entities = level.getEntities((Entity) null, column,
                    e -> e.isAlive() && !e.isSpectator() && !e.isShiftKeyDown());

            for (Entity entity : entities) {
                boolean holdingGlider = entity instanceof LivingEntity le && StormKiteGliderItem.isHoldingGlider(le);
                double targetVy = holdingGlider
                        ? (powered ? 0.68D : 0.52D)
                        : (powered ? 0.46D : 0.36D);

                Vec3 cur = entity.getDeltaMovement();
                double blendedVy = Math.min(targetVy, Math.max(cur.y + 0.12D, targetVy * 0.65D));
                double vy = Math.max(cur.y, blendedVy);
                if (vy > targetVy && cur.y <= targetVy) {
                    vy = targetVy;
                }

                entity.setDeltaMovement(cur.x, vy, cur.z);
                entity.hurtMarked = true;
                entity.resetFallDistance();

                if (entity instanceof LivingEntity le) {
                    le.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, true, false, false));
                }
            }

            if (level.getGameTime() % 4L == 0L) {
                double py = pos.getY() + 1.05D + random.nextDouble() * Math.min(clearHeight, 6);
                level.sendParticles(ParticleTypes.CLOUD,
                        pos.getX() + 0.5D, py, pos.getZ() + 0.5D,
                        1, 0.18D, 0.25D, 0.18D, 0.04D);
            }
        }

        if (level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 28.0D, false) != null
                && !level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        BlockPos above = pos.above();
        if (!level.getBlockState(above).getCollisionShape(level, above).isEmpty()
                && !(level.getBlockState(above).getBlock() instanceof ZephyrUpdraftGrateBlock)) {
            return;
        }

        boolean powered = state.getValue(POWERED);
        if (random.nextInt(powered ? 2 : 3) == 0) {
            double x = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
            double y = pos.getY() + 0.92D;
            double z = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;
            double vy = powered ? (0.14D + random.nextDouble() * 0.08D) : (0.09D + random.nextDouble() * 0.05D);
            level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, vy, 0.0D);
        }
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
            double y = pos.getY() + 0.95D + random.nextDouble() * 0.4D;
            double z = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;
            level.addParticle(ParticleTypes.WHITE_ASH, x, y, z, 0.0D, 0.08D, 0.0D);
        }
    }
}
