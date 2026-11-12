package ddraig.net.entropica.block;

import ddraig.net.entropica.entity.SporeProjectileEntity;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SporeCannonPuffballBlock extends EntropicaFlowerBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public SporeCannonPuffballBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        return this.defaultBlockState().setValue(FACING, face);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.UP;
        BlockPos supportPos = pos.relative(facing.getOpposite());
        BlockState supportState = level.getBlockState(supportPos);
        return !supportState.isAir();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        AABB scanBox = new AABB(pos).inflate(8.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, scanBox, e -> (e instanceof Player p && !p.isCreative() && !p.isSpectator()) || (e instanceof Enemy));
        if (!targets.isEmpty()) {
            LivingEntity target = targets.get(random.nextInt(targets.size()));
            SporeProjectileEntity spore = new SporeProjectileEntity(level, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5);
            Vec3 dir = target.getEyePosition().subtract(spore.position()).normalize();
            spore.shoot(dir.x, dir.y + 0.1, dir.z, 0.95f, 2.0f);
            level.addFreshEntity(spore);
            level.playSound(null, pos, SoundEvents.SLIME_ATTACK, SoundSource.BLOCKS, 0.8f, 1.4f);
        }
        level.scheduleTick(pos, this, 35);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double y = pos.getY() + 0.6 + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 0.75f, 0.15f, 0.85f);
        }
    }
}
