package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AmberNectarBlossomBlock extends FlowerBlock {
        public AmberNectarBlossomBlock(Properties properties) {
        super(net.minecraft.world.effect.MobEffects.REGENERATION, 5, properties);
    }
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.GLASS_BOTTLE)) {
            if (!level.isClientSide()) {
                stack.shrink(1);
                ItemStack nectar = new ItemStack(ModItems.AMBER_NECTAR_BOTTLE.get());
                if (!player.getInventory().add(nectar)) {
                    player.drop(nectar, false);
                }
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
            double y = pos.getY() + 0.4 + random.nextDouble() * 0.4;
            double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
            level.addParticle(new TimedTintableParticleOption(ModParticles.TINTABLE_DRIP.get(), 0.96f, 0.62f, 0.04f, 35, 1.1f, 0f, 0f, TimedTintableParticleOption.MODE_LINEAR, 0f, 0f, 0f), x, y, z, 0.0, -0.01, 0.0);
        }
    }
}
