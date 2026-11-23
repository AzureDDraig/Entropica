package ddraig.net.entropica.block.biome1;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Flora 7 — Glacial Heart-Root.
 * Ceiling-hanging crystalline root found beneath Blue-Glacier Shelf overhangs,
 * Silent Fjord basalt sea-caves, and Frost-Veiled Glacial Runic Spire archways.
 * Chills entities brushing through its tendrils, drips freezing rime onto water below,
 * and lashes out with a shattering frost recoil if broken barehanded without proper tools.
 */
public class GlacialHeartRootBlock extends Block {
    public static final MapCodec<GlacialHeartRootBlock> CODEC = simpleCodec(GlacialHeartRootBlock::new);
    protected static final VoxelShape SHAPE = Block.box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public GlacialHeartRootBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        return aboveState.isFaceSturdy(level, abovePos, Direction.DOWN)
                || aboveState.is(BlockTags.ICE)
                || aboveState.is(Blocks.ICE)
                || aboveState.is(Blocks.PACKED_ICE)
                || aboveState.is(Blocks.BLUE_ICE)
                || aboveState.is(Blocks.BASALT)
                || aboveState.is(Blocks.SMOOTH_BASALT)
                || aboveState.is(Blocks.POLISHED_BASALT)
                || (ModBlocks.BASALT_COLUMN.isBound() && aboveState.is(ModBlocks.BASALT_COLUMN.get()))
                || (ModBlocks.RUNIC_BLUE_ICE.isBound() && aboveState.is(ModBlocks.RUNIC_BLUE_ICE.get()))
                || (ModBlocks.RUNED_BASALT_PILLAR.isBound() && aboveState.is(ModBlocks.RUNED_BASALT_PILLAR.get()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction == Direction.UP && !this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (level instanceof ServerLevel serverLevel && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
            int maxFreeze = living.getTicksRequiredToFreeze() + 40;
            living.setTicksFrozen(Math.min(maxFreeze, living.getTicksFrozen() + 20));
            if ((serverLevel.getGameTime() + living.getId()) % 20L == 0L) {
                living.hurtServer(serverLevel, serverLevel.damageSources().freeze(), 1.0F);
            }
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        for (int dy = 1; dy <= 8; dy++) {
            BlockPos checkPos = pos.below(dy);
            BlockState checkState = level.getBlockState(checkPos);
            if (checkState.isAir() || checkState.getBlock() instanceof GlacialHeartRootBlock) {
                continue;
            }
            if (checkState.is(Blocks.WATER)
                    && checkState.getFluidState().isSource()
                    && level.getBlockState(checkPos.above()).isAir()) {
                level.setBlockAndUpdate(checkPos, Blocks.FROSTED_ICE.defaultBlockState());
            }
            break;
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (level instanceof ServerLevel serverLevel && !player.isCreative()) {
            boolean safeHarvest = tool.is(Items.SHEARS)
                    || tool.is(ItemTags.AXES)
                    || hasSilkTouch(serverLevel, tool);
            if (!safeHarvest) {
                player.hurtServer(serverLevel, serverLevel.damageSources().freeze(), 6.0F);
                serverLevel.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.9F, 1.15F);
                serverLevel.playSound(null, pos, SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.8F, 1.05F);
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        12, 0.25D, 0.25D, 0.25D, 0.05D);
            }
        }
    }

    private static boolean hasSilkTouch(ServerLevel level, ItemStack tool) {
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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(16) == 0) {
            double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
            double y = pos.getY() + 0.35D;
            double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0D, -0.015D, 0.0D);
        }
    }
}
