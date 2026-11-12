package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.registry.ModPotions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class PyreSproutBlock extends EntropicaFlowerBlock {
    public static final BooleanProperty HAS_NECTAR = BooleanProperty.create("has_nectar");

    public PyreSproutBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_NECTAR, true));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.NETHERRACK) || state.is(Blocks.BASALT) || state.is(Blocks.POLISHED_BASALT) || state.is(Blocks.BLACKSTONE)
            || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL) || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.GRAVEL)
            || state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.WARPED_NYLIUM) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_NECTAR);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.GLASS_BOTTLE) && state.getValue(HAS_NECTAR)) {
            if (!level.isClientSide()) {
                stack.shrink(1);
                ItemStack nectarBottle = PotionContents.createItemStack(Items.POTION, BuiltInRegistries.POTION.wrapAsHolder(ModPotions.PYRE_NECTAR.get()));
                if (!player.getInventory().add(nectarBottle)) {
                    player.drop(nectarBottle, false);
                }
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.setBlock(pos, state.setValue(HAS_NECTAR, false), 3);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (entity instanceof net.minecraft.world.entity.LivingEntity living && !living.fireImmune()) {
            living.igniteForSeconds(2);
            living.hurt(level.damageSources().inFire(), 0.5f);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.4 + random.nextDouble() * 0.4;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 1.0f, 0.27f, 0.0f);
        }
    }
}
