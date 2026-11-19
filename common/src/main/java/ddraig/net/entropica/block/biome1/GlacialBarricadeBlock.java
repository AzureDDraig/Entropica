package ddraig.net.entropica.block.biome1;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Defensive ice barricade reinforced with Glacial Aegis Plates.
 * Absorbs up to 3 projectile impacts before shattering in a freezing blast that slows
 * and damages nearby hostile mobs.
 */
public class GlacialBarricadeBlock extends TransparentBlock {
    public static final MapCodec<GlacialBarricadeBlock> CODEC = simpleCodec(GlacialBarricadeBlock::new);
    public static final IntegerProperty CHARGES = IntegerProperty.create("charges", 1, 3);

    public GlacialBarricadeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CHARGES, 3));
    }

    @Override
    protected MapCodec<? extends TransparentBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGES);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        super.onProjectileHit(level, state, hit, projectile);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos pos = hit.getBlockPos();
        if (!(projectile instanceof ThrownTrident)) {
            projectile.discard();
        } else {
            projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-0.2D));
        }

        int charges = state.getValue(CHARGES);
        if (charges > 1) {
            serverLevel.setBlock(pos, state.setValue(CHARGES, charges - 1), 3);
            serverLevel.playSound(null, pos, SoundEvents.GLASS_HIT, SoundSource.BLOCKS, 1.0F, 0.85F + 0.15F * charges);
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    12, 0.35D, 0.35D, 0.35D, 0.04D);
        } else {
            // Final hit: shatter into ice shards & unleash cold burst against nearby hostiles
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    28, 0.4D, 0.4D, 0.4D, 0.12D);
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    24, 0.6D, 0.6D, 0.6D, 0.08D);
            serverLevel.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.2F, 0.8F);
            serverLevel.removeBlock(pos, false);

            Vec3 center = Vec3.atCenterOf(pos);
            AABB burstBox = new AABB(pos).inflate(3.5D);
            List<LivingEntity> hostiles = serverLevel.getEntitiesOfClass(LivingEntity.class, burstBox,
                    e -> e.isAlive() && e instanceof Enemy && e.position().distanceToSqr(center) <= (3.5D * 3.5D + 2.0D));
            for (LivingEntity mob : hostiles) {
                mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                mob.hurtServer(serverLevel, serverLevel.damageSources().freeze(), 4.0F);
            }
        }
    }
}
