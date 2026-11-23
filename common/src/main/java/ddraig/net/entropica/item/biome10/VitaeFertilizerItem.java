package ddraig.net.entropica.item.biome10;

import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * Vitae Fertilizer — enriched golden botanical compost blended with Golden Lichen and Weeping Amber Tears.
 * Accelerates crop, sapling, and flora growth across a 3x3 horizontal grid in a single application.
 */
public class VitaeFertilizerItem extends Item {

    private static final int[] VERTICAL_SEARCH_ORDER = {0, 1, -1};

    public VitaeFertilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos targetPos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        boolean grewAny = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy : VERTICAL_SEARCH_ORDER) {
                    BlockPos checkPos = targetPos.offset(dx, dy, dz);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (!(checkState.getBlock() instanceof BonemealableBlock)) {
                        continue;
                    }
                    ItemStack probe = stack.copyWithCount(64);
                    if (BoneMealItem.growCrop(probe, level, checkPos)) {
                        grewAny = true;
                        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                            emitColumnSparkle(serverLevel, checkPos);
                        }
                        break;
                    }
                }
            }
        }

        if (!grewAny) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, targetPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.1F);
            serverLevel.playSound(null, targetPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.55F, 1.35F);
            if (player == null || !player.isCreative()) {
                stack.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static void emitColumnSparkle(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5D;
        double cy = pos.getY() + 0.45D;
        double cz = pos.getZ() + 0.5D;
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, cx, cy, cz, 5, 0.3D, 0.25D, 0.3D, 0.03D);
        if (ModParticles.SPECTRUM_SPARKLE.isBound()) {
            level.sendParticles(ModParticles.SPECTRUM_SPARKLE.get(), cx, cy + 0.15D, cz, 3, 0.25D, 0.2D, 0.25D, 0.02D);
        } else {
            level.sendParticles(ParticleTypes.END_ROD, cx, cy + 0.15D, cz, 2, 0.25D, 0.2D, 0.25D, 0.02D);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Use: Fertilizes crops, saplings, and flora across a 3x3 area.")
                .withStyle(ChatFormatting.GOLD));
    }
}
