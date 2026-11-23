package ddraig.net.entropica.block.biome8;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Barrow Sentinel Totem — 3-tier carved hound-skull sentinel totem.
 * Gently pushes minor Undead (max HP <= 30) away within a 12-block radius and breaks their aggro near the totem.
 */
public class BarrowSentinelTotemBlock extends Block {
    public static final MapCodec<BarrowSentinelTotemBlock> CODEC = simpleCodec(BarrowSentinelTotemBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
            Block.box(4.0D, 3.0D, 4.0D, 12.0D, 10.0D, 12.0D),
            Block.box(3.0D, 10.0D, 3.0D, 13.0D, 16.0D, 13.0D)
    );

    public BarrowSentinelTotemBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (!serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
                serverLevel.scheduleTick(pos, this, 20);
            }
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        repelMinorUndead(level, pos);
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.is(this)) {
            repelMinorUndead(level, pos);
            level.scheduleTick(pos, this, 20);
        }
    }

    private static void repelMinorUndead(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        double maxDistSq = 12.0D * 12.0D;
        AABB scanBox = new AABB(pos).inflate(12.0D);
        List<Monster> minorUndead = level.getEntitiesOfClass(Monster.class, scanBox,
                m -> m.isAlive()
                        && m.getType().is(EntityTypeTags.UNDEAD)
                        && m.getMaxHealth() <= 30.0F
                        && m.position().distanceToSqr(center) <= maxDistSq);

        if (minorUndead.isEmpty()) {
            return;
        }

        for (Monster undead : minorUndead) {
            double dx = undead.getX() - center.x;
            double dz = undead.getZ() - center.z;
            double horizLen = Math.sqrt(dx * dx + dz * dz);
            double nx = horizLen > 1.0E-4D ? (dx / horizLen) : (level.random.nextDouble() - 0.5D);
            double nz = horizLen > 1.0E-4D ? (dz / horizLen) : (level.random.nextDouble() - 0.5D);
            undead.push(nx * 0.28D, 0.05D, nz * 0.28D);
            undead.hurtMarked = true;

            LivingEntity target = undead.getTarget();
            if (target != null && target.blockPosition().distSqr(pos) <= 64.0D) {
                undead.setTarget(null);
                undead.getNavigation().stop();
            }
        }

        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.45F, 0.85F);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                pos.getX() + 0.5D, pos.getY() + 0.85D, pos.getZ() + 0.5D,
                6, 0.25D, 0.25D, 0.25D, 0.02D);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(5) == 0) {
            double x = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
            double y = pos.getY() + 0.72D + random.nextDouble() * 0.22D;
            double z = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
            level.addParticle(new DustParticleOptions(0x22D3EE, 0.6F), x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }
}
