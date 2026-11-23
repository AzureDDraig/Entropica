package ddraig.net.entropica.block.biome2;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Thermal Heat Lamp — copper-framed geothermal lamp powered by a Warm Aurora Petal.
 * Passively and on right-click thermal pulses melts snow layers and surface ice
 * within a 10-block radius.
 */
public class ThermalHeatLampBlock extends Block {
    public static final MapCodec<ThermalHeatLampBlock> CODEC = simpleCodec(ThermalHeatLampBlock::new);
    public static final int MELT_RADIUS = 10;
    private static final int VERTICAL_MELT_RANGE = 6;
    private static final int FOLLOW_UP_TICKS = 20;

    public ThermalHeatLampBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, FOLLOW_UP_TICKS);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            performThermalPulse(serverLevel, pos, true);
            serverLevel.scheduleTick(pos, this, FOLLOW_UP_TICKS);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        performThermalPulse(level, pos, false);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        performThermalPulse(level, pos, false);
    }

    private static void performThermalPulse(ServerLevel level, BlockPos center, boolean manualPulse) {
        int rSq = MELT_RADIUS * MELT_RADIUS;
        boolean meltedAny = false;

        for (int dy = -VERTICAL_MELT_RANGE; dy <= VERTICAL_MELT_RANGE; dy++) {
            for (int dx = -MELT_RADIUS; dx <= MELT_RADIUS; dx++) {
                for (int dz = -MELT_RADIUS; dz <= MELT_RADIUS; dz++) {
                    if (dx * dx + dy * dy + dz * dz > rSq) {
                        continue;
                    }
                    BlockPos targetPos = center.offset(dx, dy, dz);
                    if (!level.isLoaded(targetPos)) {
                        continue;
                    }
                    BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.is(Blocks.SNOW)) {
                        level.removeBlock(targetPos, false);
                        meltedAny = true;
                    } else if (targetState.is(Blocks.ICE) || targetState.is(Blocks.FROSTED_ICE)) {
                        if (level.dimensionType().ultraWarm()) {
                            level.removeBlock(targetPos, false);
                        } else {
                            level.setBlockAndUpdate(targetPos, Blocks.WATER.defaultBlockState());
                            level.neighborChanged(targetPos, Blocks.WATER, null);
                        }
                        meltedAny = true;
                    }
                }
            }
        }

        // Warm nearby players within 10 blocks by clearing freezing buildup
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, new AABB(center).inflate(MELT_RADIUS));
        for (Player p : nearbyPlayers) {
            if (p.getTicksFrozen() > 0) {
                p.setTicksFrozen(0);
            }
        }

        if (manualPulse) {
            level.playSound(null, center, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.65F, 1.25F);
            level.playSound(null, center, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 0.9F);
            level.sendParticles(ParticleTypes.CLOUD,
                    center.getX() + 0.5D, center.getY() + 1.05D, center.getZ() + 0.5D,
                    14, 0.45D, 0.25D, 0.45D, 0.03D);
            level.sendParticles(ParticleTypes.FLAME,
                    center.getX() + 0.5D, center.getY() + 0.65D, center.getZ() + 0.5D,
                    8, 0.3D, 0.25D, 0.3D, 0.015D);
        } else if (meltedAny) {
            level.playSound(null, center, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.3F, 1.6F);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(5) == 0) {
            double x = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
            double y = pos.getY() + 1.02D;
            double z = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;
            level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, 0.02D, 0.0D);
            if (random.nextBoolean()) {
                level.addParticle(ParticleTypes.GLOW, x, pos.getY() + 0.5D, z, 0.0D, 0.01D, 0.0D);
            }
        }
    }
}
