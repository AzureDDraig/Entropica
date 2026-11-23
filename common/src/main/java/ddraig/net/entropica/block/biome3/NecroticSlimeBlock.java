package ddraig.net.entropica.block.biome3;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.item.biome3.CryoInsulatedArmorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Necrotic Slime Block — congealed Blight Materia slime cube.
 * Bounces falling entities like vanilla Slime Blocks while inflicting Poison I and Slowness II
 * on contact unless protected by Cryo-Insulated Armor.
 */
public class NecroticSlimeBlock extends HalfTransparentBlock {
    public static final MapCodec<NecroticSlimeBlock> CODEC = simpleCodec(NecroticSlimeBlock::new);

    public NecroticSlimeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HalfTransparentBlock> codec() {
        return CODEC;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        if (entity.isSuppressingBounce()) {
            super.fallOn(level, state, pos, entity, fallDistance);
        } else {
            entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
        }
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityMovementAfterFallOn(level, entity);
        } else {
            Vec3 delta = entity.getDeltaMovement();
            if (delta.y < 0.0D) {
                double factor = entity instanceof LivingEntity ? 1.0D : 0.8D;
                entity.setDeltaMovement(delta.x, -delta.y * factor, delta.z);
            }
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        double absY = Math.abs(entity.getDeltaMovement().y);
        if (absY < 0.1D && !entity.isSteppingCarefully()) {
            double drag = 0.4D + absY * 0.2D;
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(drag, 1.0D, drag));
        }
        applyNecroticContactEffects(level, entity);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        applyNecroticContactEffects(level, entity);
    }

    private static void applyNecroticContactEffects(Level level, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living && !CryoInsulatedArmorItem.hasAnyCryoInsulation(living)) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
        }
    }
}
