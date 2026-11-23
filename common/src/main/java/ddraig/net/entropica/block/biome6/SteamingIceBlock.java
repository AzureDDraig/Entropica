package ddraig.net.entropica.block.biome6;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Steaming Ice — translucent geothermal shoreline ice of the Steaming Skerries.
 * Emits rising steam wisps when bordering open water, steam vents, or hot spring stone.
 */
public class SteamingIceBlock extends TransparentBlock {
    public static final MapCodec<SteamingIceBlock> CODEC = simpleCodec(SteamingIceBlock::new);

    public SteamingIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends TransparentBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) != 0) {
            return;
        }
        if (!hasAdjacentThermalOrWaterSource(level, pos)) {
            return;
        }
        BlockPos above = pos.above();
        if (level.getBlockState(above).isCollisionShapeFullBlock(level, above)) {
            return;
        }

        double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
        double y = pos.getY() + 1.02D;
        double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
        double vy = 0.025D + random.nextDouble() * 0.02D;

        if (random.nextBoolean()) {
            level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, vy, 0.0D);
        } else {
            level.addParticle(ParticleTypes.WHITE_SMOKE, x, y, z, 0.0D, vy, 0.0D);
        }
    }

    private static boolean hasAdjacentThermalOrWaterSource(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos adjPos = pos.relative(dir);
            BlockState adjState = level.getBlockState(adjPos);
            if (adjState.is(Blocks.WATER) || level.getFluidState(adjPos).is(FluidTags.WATER)) {
                return true;
            }
            if (ModBlocks.STEAM_VENT.isBound() && adjState.is(ModBlocks.STEAM_VENT.get())) {
                return true;
            }
            if (ModBlocks.STEAMING_SPRING_STONE.isBound() && adjState.is(ModBlocks.STEAMING_SPRING_STONE.get())) {
                return true;
            }
        }
        return false;
    }
}
