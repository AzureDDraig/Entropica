package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.GravitonBouncepadBlockEntity;
import ddraig.net.entropica.block.entity.GravityCenterBlockEntity;
import ddraig.net.entropica.gravity.GravityApi;
import ddraig.net.entropica.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Graviton Bouncepad Block.
 * Launches entities "upward" based on their currently experienced downward gravity.
 * Works seamlessly with normal gravity, inverted gravity (launches downward from ceiling),
 * Gravity Centers (launches radially outward away from planet core), and wall-running.
 */
public class GravitonBouncepadBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected static final VoxelShape SHAPE_UP = Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);
    protected static final VoxelShape SHAPE_DOWN = Block.box(0.0, 10.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 10.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 6.0);
    protected static final VoxelShape SHAPE_WEST = Block.box(10.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SHAPE_EAST = Block.box(0.0, 0.0, 0.0, 6.0, 16.0, 16.0);

    public GravitonBouncepadBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return this.defaultBlockState().setValue(FACING, clicked).setValue(POWERED, powered);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean isMoving) {
        if (!level.isClientSide()) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            if (state.getValue(POWERED) != hasSignal) {
                level.setBlock(pos, state.setValue(POWERED, hasSignal), 2);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_UP;
        };
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        launchEntity(level, pos, state, entity);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        if (entity.isSuppressingBounce()) {
            super.fallOn(level, state, pos, entity, fallDistance);
        } else {
            entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
            launchEntity(level, pos, state, entity);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        launchEntity(level, pos, state, entity);
    }

    /**
     * Propels entity upwards relative to their currently experienced downward gravity,
     * or horizontally/normal if wall-mounted or in zero-g.
     */
    public static void launchEntity(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isAlive()) return;

        // Debounce check: prevent rapid multi-triggering across stepOn/fallOn/entityInside and physics pinning
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GravitonBouncepadBlockEntity bouncepadBE) {
            if (!bouncepadBE.canBounce()) {
                return;
            }
            bouncepadBE.triggerBounce();
        }

        Direction facing = state.getValue(FACING);
        Vec3 padNormal = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());

        Vec3 launchDir;
        if (facing.getAxis().isHorizontal()) {
            // Wall-mounted bouncepad launches horizontally along facing normal
            launchDir = padNormal;
        } else {
            // Calculate currently experienced downward gravity vector
            Vec3 downGravity = new Vec3(0, -1, 0); // Default vanilla gravity
            boolean hasGravity = true;

            if (entity instanceof LivingEntity living) {
                if (GravityApi.isZeroG(living)) {
                    hasGravity = false;
                } else {
                    // Check for Gravity Center point gravity
                    GravityCenterBlockEntity center = GravityCenterBlockEntity.getAffectingCenter(level, entity.position());
                    if (center != null) {
                        Vec3 toCore = center.getBlockPos().getCenter().subtract(entity.position());
                        if (toCore.lengthSqr() > 1e-4) {
                            downGravity = toCore.normalize();
                        }
                    } else if (GravityApi.isInverted(living)) {
                        // Inverted gravity: ceiling is floor, down is +Y
                        downGravity = new Vec3(0, 1, 0);
                    }
                }
            }

            // Upwards relative to gravity = -downGravity, or pad normal in Zero-G
            launchDir = hasGravity ? downGravity.scale(-1.0).normalize() : padNormal;
        }

        // Analogue redstone power scaling: v_launch = 1.35 + 1.15 * (power / 15.0)
        int redstonePower = Math.min(15, Math.max(0, level.getBestNeighborSignal(pos)));
        double launchSpeed = 1.35 + 1.15 * (redstonePower / 15.0);

        // Combine launch vector: replace component along launchDir, keep perpendicular momentum
        Vec3 currentV = entity.getDeltaMovement();
        double currentAlongUp = currentV.dot(launchDir);

        Vec3 newV;
        if (currentAlongUp < launchSpeed) {
            Vec3 perpV = currentV.subtract(launchDir.scale(currentAlongUp));
            newV = perpV.add(launchDir.scale(launchSpeed));
        } else {
            newV = currentV.add(launchDir.scale(0.3));
        }

        entity.setDeltaMovement(newV);
        entity.resetFallDistance();
        entity.hasImpulse = true;
        entity.addTag("entropica:bouncepad_immune");

        // Sound and particle chimes
        float pitch = 1.0F + (redstonePower / 15.0F) * 0.35F;
        level.playSound(
                null,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                ModSounds.BOUNCEPAD_LAUNCH.get(),
                SoundSource.BLOCKS,
                1.0F,
                pitch
        );

        if (level instanceof ServerLevel serverLevel) {
            Vec3 centerPos = pos.getCenter().add(launchDir.scale(0.4));
            int count = 12 + (int) (redstonePower * 0.8F);
            serverLevel.sendParticles(
                    ParticleTypes.GLOW,
                    centerPos.x, centerPos.y, centerPos.z,
                    count,
                    launchDir.x * 0.2, launchDir.y * 0.2, launchDir.z * 0.2,
                    0.05
            );
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GravitonBouncepadBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof GravitonBouncepadBlockEntity pad) {
                GravitonBouncepadBlockEntity.tick(lvl, pos, st, pad);
            }
        };
    }
}
