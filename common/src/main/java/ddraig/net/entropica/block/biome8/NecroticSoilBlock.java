package ddraig.net.entropica.block.biome8;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Necrotic Soil — bone-flecked Barrow Materia permafrost soil native to 2.8 Barrow-Mound Tundra.
 * Witheringly rejects mundane crops and saplings while nourishing Barrow flora when bone-mealed.
 */
public class NecroticSoilBlock extends Block implements BonemealableBlock {
    public static final MapCodec<NecroticSoilBlock> CODEC = simpleCodec(NecroticSoilBlock::new);

    public NecroticSoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public static boolean isAllowedNecroticFlora(Block block) {
        if (block == null || block == Blocks.AIR) {
            return false;
        }
        if (block == Blocks.WITHER_ROSE || block == Blocks.DEAD_BUSH) {
            return true;
        }
        if ((ModBlocks.GRAVE_MOSS.isBound() && block == ModBlocks.GRAVE_MOSS.get())
                || (ModBlocks.RIME_CAP_MUSHROOM.isBound() && block == ModBlocks.RIME_CAP_MUSHROOM.get())
                || (ModBlocks.CRYPT_GRAVE_ROOT.isBound() && block == ModBlocks.CRYPT_GRAVE_ROOT.get())
                || (ModBlocks.BARROW_MOSS.isBound() && block == ModBlocks.BARROW_MOSS.get())
                || (ModBlocks.BARROW_MOSS_CARPET.isBound() && block == ModBlocks.BARROW_MOSS_CARPET.get())
                || (ModBlocks.SOUL_FLAME_ORCHID.isBound() && block == ModBlocks.SOUL_FLAME_ORCHID.get())
                || (ModBlocks.TALL_SOUL_FLAME_ORCHID.isBound() && block == ModBlocks.TALL_SOUL_FLAME_ORCHID.get())
                || (ModBlocks.SPECTRAL_LANTERN_FLOWER.isBound() && block == ModBlocks.SPECTRAL_LANTERN_FLOWER.get())
                || (ModBlocks.NECROTIC_ROSE_OF_JERICHO.isBound() && block == ModBlocks.NECROTIC_ROSE_OF_JERICHO.get())
                || (ModBlocks.TALL_NECROTIC_ROSE_OF_JERICHO.isBound() && block == ModBlocks.TALL_NECROTIC_ROSE_OF_JERICHO.get())
                || (ModBlocks.BARROW_FUNGAL_SHELF_CAP.isBound() && block == ModBlocks.BARROW_FUNGAL_SHELF_CAP.get())) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return "grave_moss".equals(path)
                || "rime_cap_mushroom".equals(path)
                || "crypt_grave_root".equals(path)
                || "barrow_moss".equals(path)
                || "barrow_moss_carpet".equals(path)
                || "soul_flame_orchid".equals(path)
                || "spectral_lantern_flower".equals(path);
    }

    public static boolean isRejectedMundanePlant(Block block) {
        if (block == null || isAllowedNecroticFlora(block)) {
            return false;
        }
        return block instanceof CropBlock
                || block instanceof SaplingBlock
                || block instanceof StemBlock
                || block instanceof AttachedStemBlock
                || block instanceof SweetBerryBushBlock
                || block instanceof SugarCaneBlock
                || block instanceof BambooStalkBlock
                || block instanceof BambooSaplingBlock;
    }

    public static boolean isRejectedMundanePlant(BlockState state) {
        return state != null && !state.isAir() && isRejectedMundanePlant(state.getBlock());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof BlockItem blockItem && isRejectedMundanePlant(blockItem.getBlock())) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, pos.above(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.45F, 1.35F);
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                        6, 0.25D, 0.08D, 0.25D, 0.02D);
            }
            return InteractionResult.FAIL;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   @Nullable Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, isMoving);
        if (!level.isClientSide()) {
            BlockPos above = pos.above();
            if (isRejectedMundanePlant(level.getBlockState(above))) {
                level.destroyBlock(above, true);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            above.getX() + 0.5D, above.getY() + 0.25D, above.getZ() + 0.5D,
                            6, 0.25D, 0.1D, 0.25D, 0.02D);
                }
            }
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        if (isRejectedMundanePlant(level.getBlockState(above))) {
            level.destroyBlock(above, true);
            level.sendParticles(ParticleTypes.SMOKE,
                    above.getX() + 0.5D, above.getY() + 0.25D, above.getZ() + 0.5D,
                    6, 0.25D, 0.1D, 0.25D, 0.02D);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        BlockState above = level.getBlockState(pos.above());
        return above.isAir() || (above.is(Blocks.SNOW) && above.getValue(SnowLayerBlock.LAYERS) <= 1);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        List<BlockState> candidates = new ArrayList<>();
        if (ModBlocks.GRAVE_MOSS.isBound()) {
            candidates.add(ModBlocks.GRAVE_MOSS.get().defaultBlockState());
            candidates.add(ModBlocks.GRAVE_MOSS.get().defaultBlockState());
        }
        if (ModBlocks.RIME_CAP_MUSHROOM.isBound()) {
            candidates.add(ModBlocks.RIME_CAP_MUSHROOM.get().defaultBlockState());
        }
        if (ModBlocks.CRYPT_GRAVE_ROOT.isBound()) {
            candidates.add(ModBlocks.CRYPT_GRAVE_ROOT.get().defaultBlockState());
        }
        if (candidates.isEmpty()) {
            return;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean isCenter = (dx == 0 && dz == 0);
                if (!isCenter && random.nextFloat() > 0.65F) {
                    continue;
                }
                for (int dy = 1; dy >= -1; dy--) {
                    BlockPos groundPos = pos.offset(dx, dy, dz);
                    BlockState groundState = level.getBlockState(groundPos);
                    boolean validSoil = groundState.is(this) || groundState.is(Blocks.SNOW_BLOCK);
                    if (!validSoil) {
                        continue;
                    }
                    BlockPos targetPos = groundPos.above();
                    BlockState existing = level.getBlockState(targetPos);
                    boolean replaceable = existing.isAir()
                            || (existing.is(Blocks.SNOW) && existing.getValue(SnowLayerBlock.LAYERS) <= 1);
                    if (!replaceable) {
                        break;
                    }
                    BlockState chosen = candidates.get(random.nextInt(candidates.size()));
                    if (chosen.canSurvive(level, targetPos)) {
                        level.setBlock(targetPos, chosen, 3);
                    }
                    break;
                }
            }
        }
    }
}
