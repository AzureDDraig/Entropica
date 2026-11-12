package ddraig.net.entropica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public class VoidStalkerOrchidBlock extends EntropicaFlowerBlock {
    public VoidStalkerOrchidBlock(Properties properties) {
        super(properties);
    }


    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.END_STONE)
            || state.is(Blocks.OBSIDIAN)
            || state.is(Blocks.CRYING_OBSIDIAN)
            || state.is(Blocks.SCULK)
            || state.is(Blocks.SOUL_SAND)
            || state.is(Blocks.SOUL_SOIL)
            || state.is(Blocks.NETHERRACK);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide() && entity instanceof LivingEntity living && level.random.nextInt(20) == 0) {
            double targetX = pos.getX() + (level.random.nextDouble() - 0.5) * 8.0;
            double targetY = pos.getY() + level.random.nextInt(3) - 1;
            double targetZ = pos.getZ() + (level.random.nextDouble() - 0.5) * 8.0;

            BlockPos targetPos = BlockPos.containing(targetX, targetY, targetZ);
            if (level.getBlockState(targetPos).canBeReplaced() && level.getBlockState(targetPos.above()).canBeReplaced()) {
                living.teleportTo(targetX, targetY, targetZ);
                level.playSound(null, targetPos, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        popResource(level, pos, new ItemStack(this.asItem()));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;

            level.addParticle(ParticleTypes.PORTAL, x, y, z, (random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.2);
            if (random.nextBoolean()) {
                level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0.0, 0.03, 0.0);
            }
        }
    }
}
