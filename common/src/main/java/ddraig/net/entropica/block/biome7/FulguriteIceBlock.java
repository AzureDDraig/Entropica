package ddraig.net.entropica.block.biome7;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fulgurite Ice — translucent lightning-veined glacial ice of 2.7 Static Ice-Plains.
 * Slippery (0.98f), glows at Light Level 4, emits constant weak Redstone power (Level 4),
 * and arcs subtle cyan static sparks toward nearby ice blocks.
 */
public class FulguriteIceBlock extends TransparentBlock {
    public static final MapCodec<FulguriteIceBlock> CODEC = simpleCodec(FulguriteIceBlock::new);

    public FulguriteIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends TransparentBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 4;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(5) != 0) {
            return;
        }
        BlockPos above = pos.above();
        if (level.getBlockState(above).isCollisionShapeFullBlock(level, above)) {
            return;
        }

        BlockPos targetIce = findNearbyIceTarget(level, pos, random);
        double sx = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
        double sy = pos.getY() + 1.03D;
        double sz = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;

        double vx = (random.nextDouble() - 0.5D) * 0.04D;
        double vy = 0.02D + random.nextDouble() * 0.025D;
        double vz = (random.nextDouble() - 0.5D) * 0.04D;

        if (targetIce != null) {
            vx = (targetIce.getX() + 0.5D - sx) * 0.08D;
            vy = 0.035D;
            vz = (targetIce.getZ() + 0.5D - sz) * 0.08D;
        }

        if (random.nextBoolean() || !ModParticles.SPECTRUM_SPARKLE.isBound()) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, sx, sy, sz, vx, vy, vz);
        } else {
            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), sx, sy, sz, 0.13F, 0.83F, 0.93F);
        }
    }

    private BlockPos findNearbyIceTarget(Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 4; i++) {
            int dx = random.nextInt(5) - 2;
            int dy = random.nextInt(3) - 1;
            int dz = random.nextInt(5) - 2;
            if (dx == 0 && dy == 0 && dz == 0) {
                continue;
            }
            BlockPos check = pos.offset(dx, dy, dz);
            BlockState st = level.getBlockState(check);
            if (st.is(this) || st.is(Blocks.ICE) || st.is(Blocks.PACKED_ICE) || st.is(Blocks.BLUE_ICE)) {
                return check;
            }
        }
        return null;
    }
}
