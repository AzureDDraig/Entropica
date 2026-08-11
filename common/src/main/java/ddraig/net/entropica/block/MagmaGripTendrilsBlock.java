package ddraig.net.entropica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MagmaGripTendrilsBlock extends GlowLichenBlock {
    public MagmaGripTendrilsBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        super.randomTick(state, level, pos, random);
        // Spreads like lichen across solid faces and crawls like vines onto adjacent walls / down into air
        if (random.nextInt(4) == 0) {
            this.getSpreader().spreadFromRandomFaceTowardRandomDirection(state, level, pos, random);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        applyTendrilEffect(level, entity);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        applyTendrilEffect(level, entity);
    }

    private void applyTendrilEffect(Level level, Entity entity) {
        if (entity instanceof LivingEntity living && !level.isClientSide()) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 3, false, true, true));
            if (!living.fireImmune()) {
                living.igniteForSeconds(5);
                living.hurt(level.damageSources().inFire(), 2.0f);
            }
        }
    }
}
