package ddraig.net.entropica.item.biome15;

import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Dust-Core Shard — dense, vortex-fused lithic shard dropped by Dust-Core Golems and sifted from Grey Sand.
 * Right-clicking loose Sand, Red Sand, or Grey Sand lithifies up to a 3x3 horizontal patch into solid stone,
 * and brews with Awkward Potions into Potions of Zephyr Weight.
 */
public class DustCoreShardItem extends Item {
    public static final int COOLDOWN_TICKS = 10;

    public DustCoreShardItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static BlockState getCompactedState(BlockState state) {
        if (state.is(Blocks.SAND)) {
            return Blocks.SANDSTONE.defaultBlockState();
        }
        if (state.is(Blocks.RED_SAND)) {
            return Blocks.RED_SANDSTONE.defaultBlockState();
        }
        if (ModBlocks.GREY_SAND.isBound() && state.is(ModBlocks.GREY_SAND.get())) {
            return Blocks.TUFF.defaultBlockState();
        }
        return null;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos clickedPos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();

        if (player != null && player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (getCompactedState(level.getBlockState(clickedPos)) == null) {
            return super.useOn(ctx);
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            int compactedCount = 0;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos targetPos = clickedPos.offset(dx, 0, dz);
                    BlockState compacted = getCompactedState(serverLevel.getBlockState(targetPos));
                    if (compacted != null) {
                        serverLevel.setBlock(targetPos, compacted, 3);
                        serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, targetPos, GameEvent.Context.of(player, compacted));
                        serverLevel.sendParticles(ParticleTypes.ASH,
                                targetPos.getX() + 0.5D, targetPos.getY() + 1.05D, targetPos.getZ() + 0.5D,
                                5, 0.28D, 0.08D, 0.28D, 0.02D);
                        serverLevel.sendParticles(ParticleTypes.CRIT,
                                targetPos.getX() + 0.5D, targetPos.getY() + 1.05D, targetPos.getZ() + 0.5D,
                                3, 0.25D, 0.08D, 0.25D, 0.03D);
                        compactedCount++;
                    }
                }
            }

            if (compactedCount > 0) {
                serverLevel.playSound(null, clickedPos, SoundEvents.BASALT_PLACE, SoundSource.BLOCKS, 1.0F, 0.9F);
                serverLevel.playSound(null, clickedPos, SoundEvents.SAND_BREAK, SoundSource.BLOCKS, 0.85F, 1.15F);
                if (player != null) {
                    player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                } else {
                    stack.shrink(1);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Use on Sand/Silt: Compacts a 3x3 patch of Sand, Red Sand, or Grey Sand into Sandstone or Tuff.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Brewing: Brew with Awkward Potions to craft Potions of Zephyr Weight.")
                .withStyle(ChatFormatting.AQUA));
    }
}
