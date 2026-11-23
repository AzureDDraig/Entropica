package ddraig.net.entropica.block.biome17;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Stardust Crystal Block — luminous Astral Materia crystal block of 2.17 Astral Skerries.
 * Radiates Light 9 and sings with crystalline amethyst chimes when stepped across or struck by projectiles.
 */
public class StardustCrystalBlock extends AmethystBlock {
    public static final MapCodec<StardustCrystalBlock> CODEC = simpleCodec(StardustCrystalBlock::new);

    public StardustCrystalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends AmethystBlock> codec() {
        return CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide()
                && entity instanceof LivingEntity
                && !entity.isSteppingCarefully()
                && level.getGameTime() % 12L == 0L) {
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS,
                    0.55F, 1.0F + level.random.nextFloat() * 0.25F);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,
                    0.45F, 0.9F + level.random.nextFloat() * 0.4F);
        }
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        super.onProjectileHit(level, state, hit, projectile);
        if (!level.isClientSide()) {
            BlockPos pos = hit.getBlockPos();
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,
                    1.0F, 0.85F + level.random.nextFloat() * 0.45F);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS,
                    0.65F, 1.15F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                        6, 0.3D, 0.3D, 0.3D, 0.025D);
            }
        }
    }
}
