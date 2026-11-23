package ddraig.net.entropica.block.biome16;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.item.biome16.SanguineIceLancetItem;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Sanguine Ice — translucent crimson-veined pack ice of 2.16 Blood-Ice Shallows infused with
 * Blood and Glacial Materia. Emits soft Light 3, never melts from lantern or torch light,
 * resonates gently beneath wounded travelers, and requires Silk Touch or a Sanguine Ice-Lancet
 * to harvest intact.
 */
public class SanguineIceBlock extends HalfTransparentBlock {
    public static final MapCodec<SanguineIceBlock> CODEC = simpleCodec(SanguineIceBlock::new);

    public SanguineIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HalfTransparentBlock> codec() {
        return CODEC;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        boolean silkTouch = hasSilkTouch(level, tool);
        boolean lancet = (ModItems.SANGUINE_ICE_LANCET.isBound() && tool.is(ModItems.SANGUINE_ICE_LANCET.get()))
                || tool.getItem() instanceof SanguineIceLancetItem;

        if (silkTouch) {
            super.playerDestroy(level, player, pos, state, blockEntity, tool);
            return;
        }
        if (lancet) {
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
            if (level instanceof ServerLevel serverLevel) {
                popResource(serverLevel, pos, new ItemStack(this));
            }
            return;
        }

        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        if (level.dimensionType().ultraWarm()) {
            level.removeBlock(pos, false);
        } else {
            BlockState below = level.getBlockState(pos.below());
            if (below.blocksMotion() || !below.getFluidState().isEmpty()) {
                level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            }
        }
    }

    private static boolean hasSilkTouch(Level level, ItemStack tool) {
        if (tool == null || tool.isEmpty()) {
            return false;
        }
        if (EnchantmentHelper.hasTag(tool, EnchantmentTags.PREVENTS_ICE_MELTING)) {
            return true;
        }
        return level.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(reg -> reg.get(Enchantments.SILK_TOUCH))
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, tool) > 0)
                .orElse(false);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (entity instanceof LivingEntity living && living.getHealth() < living.getMaxHealth()) {
            if (level.isClientSide()) {
                DustParticleOptions crimsonMote = new DustParticleOptions(0xC81E38, 0.9F);
                for (int i = 0; i < 2; i++) {
                    double px = pos.getX() + 0.2D + level.random.nextDouble() * 0.6D;
                    double py = pos.getY() + 1.03D;
                    double pz = pos.getZ() + 0.2D + level.random.nextDouble() * 0.6D;
                    level.addParticle(crimsonMote, px, py, pz, 0.0D, 0.02D, 0.0D);
                }
            } else if (level.getGameTime() % 40L == 0L) {
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.3F, 1.35F);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(28) != 0) {
            return;
        }
        if (!level.getBlockState(pos.above()).isAir()) {
            return;
        }
        double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
        double y = pos.getY() + 1.03D;
        double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
        level.addParticle(new DustParticleOptions(0xB8223C, 0.85F), x, y, z, 0.0D, 0.015D, 0.0D);
    }
}
