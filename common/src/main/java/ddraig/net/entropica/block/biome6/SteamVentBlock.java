package ddraig.net.entropica.block.biome6;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Steam Vent — active geothermal basalt fissure of the Steaming Skerries.
 * Emits rising steam plumes, clears frost buildup on friendly travelers, singes
 * non-sneaking hostile monsters for 1.0 hot-steam damage, and feeds adjacent Vapor Condensers.
 */
public class SteamVentBlock extends Block {
    public static final MapCodec<SteamVentBlock> CODEC = simpleCodec(SteamVentBlock::new);

    public SteamVentBlock(Properties properties) {
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
        if (!level.isClientSide()
                && level instanceof ServerLevel serverLevel
                && entity instanceof LivingEntity living
                && entity instanceof Enemy
                && !entity.isSteppingCarefully()) {
            living.hurtServer(serverLevel, serverLevel.damageSources().hotFloor(), 1.0F);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!ModBlocks.VAPOR_CONDENSER.isBound()) {
            return;
        }
        Block condenserBlock = ModBlocks.VAPOR_CONDENSER.get();
        for (Direction dir : Direction.values()) {
            BlockPos adjPos = pos.relative(dir);
            if (level.getBlockState(adjPos).is(condenserBlock)) {
                VaporCondenserBlock.tryCondenseSteamIntoWater(level, adjPos);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        BlockPos above = pos.above();
        if (level.getBlockState(above).isCollisionShapeFullBlock(level, above)) {
            return;
        }

        double x = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
        double y = pos.getY() + 1.05D;
        double z = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;

        level.addParticle(ParticleTypes.CLOUD,
                x, y, z,
                (random.nextDouble() - 0.5D) * 0.015D,
                0.08D + random.nextDouble() * 0.05D,
                (random.nextDouble() - 0.5D) * 0.015D);

        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x, y + 0.1D, z,
                    0.0D, 0.07D, 0.0D);
        }

        if (random.nextInt(24) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                    0.15F, 1.2F + random.nextFloat() * 0.3F, false);
        }
    }
}
