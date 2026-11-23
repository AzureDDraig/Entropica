package ddraig.net.entropica.item.biome15;

import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * Copper Silt-Sieve — copper-framed Tough-Fiber Rope panning mesh (128 durability).
 * Right-click Grey Sand, Sand, Red Sand, Gravel, or Coarse Gravel to sift out mineral nuggets,
 * quartz, Dust-Core Shards, and rare emeralds (with richer mineral yields on Grey Sand!).
 */
public class CopperSiltSieveItem extends Item {
    public static final int COOLDOWN_TICKS = 8;

    public CopperSiltSieveItem(Properties properties) {
        super(properties.stacksTo(1).durability(128));
    }

    public static boolean isSiftable(BlockState state) {
        return (ModBlocks.GREY_SAND.isBound() && state.is(ModBlocks.GREY_SAND.get()))
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.GRAVEL)
                || (ModBlocks.COARSE_GRAVEL.isBound() && state.is(ModBlocks.COARSE_GRAVEL.get()));
    }

    public static Item rollSiftedMineral(RandomSource random, boolean isGreySand) {
        int roll = random.nextInt(100);
        if (isGreySand && roll < 44 && random.nextBoolean()) {
            // Grey Sand carries richer glacial-basalt metallic silt
            roll = random.nextInt(100);
        }
        if (roll < 24) {
            return Items.FLINT;
        } else if (roll < 44) {
            return Items.CLAY_BALL;
        } else if (roll < 62) {
            return Items.IRON_NUGGET;
        } else if (roll < 76) {
            return Items.GOLD_NUGGET;
        } else if (roll < 86) {
            return Items.RAW_COPPER;
        } else if (roll < 94) {
            return Items.QUARTZ;
        } else if (roll < 98) {
            return ModItems.DUST_CORE_SHARD.isBound() ? ModItems.DUST_CORE_SHARD.get() : Items.QUARTZ;
        } else {
            return Items.EMERALD;
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!isSiftable(state)) {
            return super.useOn(ctx);
        }

        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        if (player != null && player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            boolean isGreySand = ModBlocks.GREY_SAND.isBound() && state.is(ModBlocks.GREY_SAND.get());

            serverLevel.levelEvent(2001, pos, Block.getId(state));
            serverLevel.removeBlock(pos, false);

            serverLevel.playSound(null, pos, SoundEvents.SAND_STEP, SoundSource.BLOCKS, 0.85F, 1.0F);
            serverLevel.playSound(null, pos, SoundEvents.BRUSH_SAND_COMPLETED, SoundSource.BLOCKS, 0.9F, 1.1F);
            serverLevel.sendParticles(ParticleTypes.ASH,
                    pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                    8, 0.25D, 0.2D, 0.25D, 0.02D);

            if (player != null) {
                player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
                stack.hurtAndBreak(1, player, ctx.getHand());
            }

            int drops = serverLevel.random.nextFloat() < (isGreySand ? 0.65F : 0.35F) ? 2 : 1;
            for (int i = 0; i < drops; i++) {
                Item reward = rollSiftedMineral(serverLevel.random, isGreySand);
                ItemEntity itemEntity = new ItemEntity(
                        serverLevel,
                        pos.getX() + 0.5D,
                        pos.getY() + 0.65D,
                        pos.getZ() + 0.5D,
                        new ItemStack(reward)
                );
                itemEntity.setDeltaMovement(
                        (serverLevel.random.nextDouble() - 0.5D) * 0.12D,
                        0.18D + serverLevel.random.nextDouble() * 0.08D,
                        (serverLevel.random.nextDouble() - 0.5D) * 0.12D
                );
                itemEntity.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(itemEntity);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Use on Sand/Gravel: Sifts 1-2 mineral drops from Sand, Gravel, or Grey Sand.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Rich Silt: Grey Sand yields higher chances of metals, Quartz, Dust-Core Shards, and Emeralds.")
                .withStyle(ChatFormatting.AQUA));
    }
}
