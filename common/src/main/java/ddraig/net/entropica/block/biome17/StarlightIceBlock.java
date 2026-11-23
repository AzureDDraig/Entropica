package ddraig.net.entropica.block.biome17;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.item.biome16.SanguineIceLancetItem;
import ddraig.net.entropica.item.biome17.StarlightTuningChiselItem;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Starlight Ice — translucent celestial-blue pack ice of 2.17 Astral Skerries infused with
 * Astral and Glacial Materia. Emits steady Light 5, never melts from block light or sunlight,
 * never floods into water when broken, twinkles with starlight motes under open night skies,
 * and harvests cleanly with Silk Touch, a Starlight Tuning Chisel, or a Sanguine Ice-Lancet.
 */
public class StarlightIceBlock extends HalfTransparentBlock {
    public static final MapCodec<StarlightIceBlock> CODEC = simpleCodec(StarlightIceBlock::new);

    public StarlightIceBlock(Properties properties) {
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
        boolean specialCutter = (ModItems.STARLIGHT_TUNING_CHISEL.isBound() && tool.is(ModItems.STARLIGHT_TUNING_CHISEL.get()))
                || tool.getItem() instanceof StarlightTuningChiselItem
                || (ModItems.SANGUINE_ICE_LANCET.isBound() && tool.is(ModItems.SANGUINE_ICE_LANCET.get()))
                || tool.getItem() instanceof SanguineIceLancetItem;

        if (!silkTouch && specialCutter) {
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
            if (level instanceof ServerLevel serverLevel) {
                popResource(serverLevel, pos, new ItemStack(this));
            }
            return;
        }

        // Standard Block.playerDestroy evaluates blocks/starlight_ice.json (Silk Touch -> starlight_ice, else 35% stardust_fragment)
        // and never places water.
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    public static boolean hasSilkTouch(Level level, ItemStack tool) {
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

    public static boolean isNightSky(Level level) {
        long dayTime = Math.floorMod(level.getDayTime(), 24000L);
        return dayTime >= 12500L && dayTime <= 23500L;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(18) != 0) {
            return;
        }
        BlockPos above = pos.above();
        if (!level.getBlockState(above).isAir()) {
            return;
        }
        if (!isNightSky(level) || !level.canSeeSky(above)) {
            return;
        }
        double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
        double y = pos.getY() + 1.04D + random.nextDouble() * 0.12D;
        double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
        level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.012D, 0.0D);
    }
}
