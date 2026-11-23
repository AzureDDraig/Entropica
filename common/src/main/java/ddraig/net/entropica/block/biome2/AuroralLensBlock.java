package ddraig.net.entropica.block.biome2;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Auroral Lens Block — prismatic crystal optic crowning the Auroral Wellspring core.
 * Refracts Aurora and Vapor Materia into shimmering rays; right-clicking chimes
 * and projects an upward aurora beam.
 */
public class AuroralLensBlock extends HalfTransparentBlock {
    public static final MapCodec<AuroralLensBlock> CODEC = simpleCodec(AuroralLensBlock::new);

    private static final int[] PRISM_COLORS = new int[]{
            0x34D399, // Emerald
            0x22D3EE, // Cyan
            0xE879F9, // Magenta-violet
            0xFDE047  // Auroral gold
    };

    public AuroralLensBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HalfTransparentBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            emitAuroraBeam(serverLevel, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        super.onProjectileHit(level, state, hit, projectile);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            emitAuroraBeam(serverLevel, hit.getBlockPos());
        }
    }

    private static void emitAuroraBeam(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 1.1F);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.9F, 1.25F);

        double cx = pos.getX() + 0.5D;
        double cz = pos.getZ() + 0.5D;
        for (int step = 0; step < 20; step++) {
            double y = pos.getY() + 1.05D + step * 0.45D;
            int color = PRISM_COLORS[step % PRISM_COLORS.length];
            level.sendParticles(new DustParticleOptions(color, 1.1F),
                    cx, y, cz, 2, 0.08D, 0.1D, 0.08D, 0.01D);
            if (step % 2 == 0) {
                level.sendParticles(ParticleTypes.END_ROD,
                        cx, y, cz, 1, 0.05D, 0.05D, 0.05D, 0.015D);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
            double y = pos.getY() + 0.2D + random.nextDouble() * 0.85D;
            double z = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;
            int color = PRISM_COLORS[random.nextInt(PRISM_COLORS.length)];
            level.addParticle(new DustParticleOptions(color, 0.8F), x, y, z, 0.0D, 0.02D, 0.0D);
            if (random.nextInt(4) == 0) {
                level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D,
                        (random.nextDouble() - 0.5D) * 0.02D, 0.03D, (random.nextDouble() - 0.5D) * 0.02D);
            }
        }
    }
}
