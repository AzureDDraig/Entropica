package ddraig.net.entropica.item.biome8;

import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bleached Bone Meal — frost-bleached Barrow fertilizer ground from weathered crypt bones.
 * Mosses Barrow Stone Bricks directly and sprouts Barrow flora across cold soils and snow.
 */
public class BleachedBoneMealItem extends Item {

    public BleachedBoneMealItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        // 1. Direct conversion: Barrow Stone Bricks -> Mossy Barrow Bricks
        if (ModBlocks.BARROW_STONE_BRICKS.isBound()
                && ModBlocks.MOSSY_BARROW_BRICKS.isBound()
                && state.is(ModBlocks.BARROW_STONE_BRICKS.get())) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                serverLevel.setBlock(pos, ModBlocks.MOSSY_BARROW_BRICKS.get().defaultBlockState(), 3);
                emitBleachedGrowthEffects(serverLevel, pos.above());
                if (player == null || !player.isCreative()) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // 2. Sprout Barrow flora in a 5x5 area on valid Barrow/tundra surfaces
        if (isBarrowSproutSurface(state)) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                sproutBarrowFlora(serverLevel, pos, serverLevel.random);
                emitBleachedGrowthEffects(serverLevel, pos.above());
                if (player == null || !player.isCreative()) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // 3. Fallback: fertilize any standard BonemealableBlock
        if (state.getBlock() instanceof BonemealableBlock) {
            if (BoneMealItem.growCrop(stack, level, pos)) {
                if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                    emitBleachedGrowthEffects(serverLevel, pos);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    private static boolean isBarrowSproutSurface(BlockState state) {
        return state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || (ModBlocks.NECROTIC_SOIL.isBound() && state.is(ModBlocks.NECROTIC_SOIL.get()))
                || (ModBlocks.MOSSY_BARROW_BRICKS.isBound() && state.is(ModBlocks.MOSSY_BARROW_BRICKS.get()))
                || (ModBlocks.BARROW_MOSS.isBound() && state.is(ModBlocks.BARROW_MOSS.get()));
    }

    private static void sproutBarrowFlora(ServerLevel level, BlockPos center, RandomSource random) {
        List<BlockState> floraPool = new ArrayList<>();
        if (ModBlocks.GRAVE_MOSS.isBound()) {
            floraPool.add(ModBlocks.GRAVE_MOSS.get().defaultBlockState());
            floraPool.add(ModBlocks.GRAVE_MOSS.get().defaultBlockState());
        }
        if (ModBlocks.RIME_CAP_MUSHROOM.isBound()) {
            floraPool.add(ModBlocks.RIME_CAP_MUSHROOM.get().defaultBlockState());
        }
        if (ModBlocks.CRYPT_GRAVE_ROOT.isBound()) {
            floraPool.add(ModBlocks.CRYPT_GRAVE_ROOT.get().defaultBlockState());
        }
        if (ModBlocks.BARROW_MOSS_CARPET.isBound()) {
            floraPool.add(ModBlocks.BARROW_MOSS_CARPET.get().defaultBlockState());
        }
        if (floraPool.isEmpty()) {
            return;
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean isCenter = (dx == 0 && dz == 0);
                if (!isCenter && random.nextFloat() > 0.45F) {
                    continue;
                }
                for (int dy = 1; dy >= -1; dy--) {
                    BlockPos groundPos = center.offset(dx, dy, dz);
                    BlockState groundState = level.getBlockState(groundPos);
                    if (!isBarrowSproutSurface(groundState)
                            && !(ModBlocks.BARROW_STONE_BRICKS.isBound() && groundState.is(ModBlocks.BARROW_STONE_BRICKS.get()))) {
                        continue;
                    }
                    BlockPos placePos = groundPos.above();
                    BlockState existing = level.getBlockState(placePos);
                    boolean replaceable = existing.isAir()
                            || (existing.is(Blocks.SNOW) && existing.getValue(SnowLayerBlock.LAYERS) <= 1);
                    if (!replaceable) {
                        break;
                    }
                    BlockState candidate = floraPool.get(random.nextInt(floraPool.size()));
                    if (candidate.canSurvive(level, placePos)) {
                        level.setBlock(placePos, candidate, 3);
                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                placePos.getX() + 0.5D, placePos.getY() + 0.25D, placePos.getZ() + 0.5D,
                                4, 0.25D, 0.15D, 0.25D, 0.02D);
                    }
                    break;
                }
            }
        }
    }

    private static void emitBleachedGrowthEffects(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 0.95F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D,
                12, 0.35D, 0.25D, 0.35D, 0.04D);
        level.sendParticles(ParticleTypes.SNOWFLAKE,
                pos.getX() + 0.5D, pos.getY() + 0.35D, pos.getZ() + 0.5D,
                8, 0.35D, 0.25D, 0.35D, 0.02D);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Use: Sprouts Grave-Moss, Rime-Cap Mushrooms, and Crypt-Grave Roots on cold soil or snow.")
                .withStyle(ChatFormatting.AQUA));
        tooltipComponents.accept(Component.literal("Masonry: Turns Barrow Stone Bricks into Mossy Barrow Bricks.")
                .withStyle(ChatFormatting.GRAY));
    }
}
