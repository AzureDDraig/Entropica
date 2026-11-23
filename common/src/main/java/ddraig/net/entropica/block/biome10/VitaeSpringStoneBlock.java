package ddraig.net.entropica.block.biome10;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Vitae Spring-Stone — warm travertine basin rock saturated with Vitae Materia.
 * Keeps sanctuary hot springs thawed by melting nearby ice and emits rising golden bubbles under water.
 */
public class VitaeSpringStoneBlock extends Block {
    public static final MapCodec<VitaeSpringStoneBlock> CODEC = simpleCodec(VitaeSpringStoneBlock::new);

    public VitaeSpringStoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (entity.getTicksFrozen() > 0) {
            entity.setTicksFrozen(0);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.is(Blocks.ICE) || checkState.is(Blocks.FROSTED_ICE)) {
                        level.setBlockAndUpdate(checkPos, Blocks.WATER.defaultBlockState());
                    }
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        BlockPos above = pos.above();
        if (level.getBlockState(above).is(Blocks.WATER)) {
            if (random.nextInt(3) == 0) {
                double x = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
                double y = pos.getY() + 1.05D;
                double z = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;
                level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, x, y, z, 0.0D, 0.06D, 0.0D);
                if (random.nextInt(3) == 0) {
                    level.addParticle(ParticleTypes.HAPPY_VILLAGER, x, y + 0.25D, z, 0.0D, 0.03D, 0.0D);
                } else if (random.nextInt(3) == 0) {
                    level.addParticle(ParticleTypes.END_ROD, x, y + 0.2D, z, 0.0D, 0.025D, 0.0D);
                }
            }
        } else if (random.nextInt(6) == 0 && !level.getBlockState(above).isCollisionShapeFullBlock(level, above)) {
            double x = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
            double y = pos.getY() + 1.03D;
            double z = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.015D, 0.0D);
        }
    }
}
