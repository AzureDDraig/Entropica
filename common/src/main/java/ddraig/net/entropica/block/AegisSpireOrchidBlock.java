package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class AegisSpireOrchidBlock extends DoublePlantBlock implements BonemealableBlock {
    private static final double AURA_RADIUS = 4.0D;

    public AegisSpireOrchidBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected net.minecraft.world.InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (ddraig.net.entropica.util.FloraHarvestHelper.tryShearHarvest(level, pos, state, player, hand)) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        if (EntropicaFloraGroundHelper.isIceOrSnowLayer(state)) {
            return false;
        }
        return state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE)
                || state.is(Blocks.ANDESITE) || state.is(Blocks.TUFF) || state.is(Blocks.CALCITE) || state.is(Blocks.SMOOTH_BASALT)
                || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.PODZOL)
                || state.is(net.minecraft.tags.BlockTags.DIRT)
                || EntropicaFloraGroundHelper.isSolidSnowBlock(state)
                || (ModBlocks.AEGIS_QUARTZ_BLOCK.isBound() && state.is(ModBlocks.AEGIS_QUARTZ_BLOCK.get()));
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        applyResistanceAura(level, pos);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (!level.isClientSide()) {
            if (entity instanceof LivingEntity living && living.isAlive() && !(living instanceof Enemy)) {
                living.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 0, true, true, true));
            }
            applyResistanceAura(level, pos);
        }
    }

    static void applyResistanceAura(Level level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(AURA_RADIUS);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && !e.isSpectator() && !(e instanceof Enemy))) {
            entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 0, true, true, true));
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        popResource(level, pos, new ItemStack(this.asItem()));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 0.11f, 0.31f, 0.85f);
        }
    }
}
