package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModBlocks;
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
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

public class EntropicaFlowerBlock extends BushBlock implements BonemealableBlock {
    public EntropicaFlowerBlock(Properties properties) {
        super(properties);
    }

    private boolean isAegisSpireOrchid() {
        return ModBlocks.AEGIS_SPIRE_ORCHID.isBound() && this == ModBlocks.AEGIS_SPIRE_ORCHID.get();
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        if (EntropicaFloraGroundHelper.isIceOrSnowLayer(state)) {
            return false;
        }
        if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT)
                || state.is(net.minecraft.tags.BlockTags.DIRT)
                || state.is(Blocks.CLAY) || state.is(Blocks.MUD) || state.is(Blocks.MUDDY_MANGROVE_ROOTS)) {
            return true;
        }
        if (isAegisSpireOrchid()) {
            return state.is(Blocks.STONE)
                    || state.is(Blocks.DEEPSLATE)
                    || state.is(Blocks.CALCITE)
                    || state.is(Blocks.SMOOTH_BASALT)
                    || EntropicaFloraGroundHelper.isSolidSnowBlock(state)
                    || (ModBlocks.AEGIS_QUARTZ_BLOCK.isBound() && state.is(ModBlocks.AEGIS_QUARTZ_BLOCK.get()));
        }
        return false;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isAegisSpireOrchid()) {
            AegisSpireOrchidBlock.applyResistanceAura(level, pos);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (!level.isClientSide() && isAegisSpireOrchid()) {
            if (entity instanceof LivingEntity living && living.isAlive() && !(living instanceof Enemy)) {
                living.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 0, true, true, true));
            }
            AegisSpireOrchidBlock.applyResistanceAura(level, pos);
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
        // If this 1-tall flower has a 2-tall variant and space above is clear, grow it into the 2-tall variant!
        if (isAegisSpireOrchid()) {
            BlockPos posAbove = pos.above();
            if (level.getBlockState(posAbove).canBeReplaced()) {
                DoublePlantBlock.placeAt(level, ModBlocks.TALL_AEGIS_SPIRE_ORCHID.get().defaultBlockState(), pos, 3);
                return;
            }
        }

        // Default behavior for flowers/mushrooms without a 2-tall variant or blocked space above: drop 1x item in world!
        popResource(level, pos, new ItemStack(this.asItem()));
    }

    @Override
    protected net.minecraft.world.InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (ddraig.net.entropica.util.FloraHarvestHelper.tryShearHarvest(level, pos, state, player, hand)) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}

