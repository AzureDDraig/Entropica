package ddraig.net.entropica.block.biome8;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.effect.BarrowWardEffect;
import ddraig.net.entropica.item.biome8.BarrowSlateGreavesItem;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Crypt-Grave Root — grasping necrotic floor root pad found around barrow entrances.
 * Snags non-undead trespassers with Slowness IV and Wither I unless protected by Barrow gear or wards.
 */
public class CryptGraveRootBlock extends Block {
    public static final MapCodec<CryptGraveRootBlock> CODEC = simpleCodec(CryptGraveRootBlock::new);
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public CryptGraveRootBlock(Properties properties) {
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
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (belowState.isAir()
                || ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(belowState)
                || belowState.is(BlockTags.LEAVES)) {
            return false;
        }
        return belowState.isFaceSturdy(level, belowPos, Direction.UP)
                || belowState.is(Blocks.SNOW_BLOCK)
                || belowState.is(BlockTags.DIRT)
                || (ModBlocks.NECROTIC_SOIL.isBound() && belowState.is(ModBlocks.NECROTIC_SOIL.get()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (!(level instanceof ServerLevel) || !(entity instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }
        if (living.getType().is(EntityTypeTags.UNDEAD)) {
            return;
        }
        if (living instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }
        if (BarrowSlateGreavesItem.hasBarrowSlateGreaves(living)) {
            return;
        }

        living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
        if (!BarrowWardEffect.hasBarrowWard(living)) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
        }
    }

    public static boolean isStandingOnCryptGraveRoot(Level level, LivingEntity entity) {
        if (level == null || entity == null || !ModBlocks.CRYPT_GRAVE_ROOT.isBound()) {
            return false;
        }
        Block root = ModBlocks.CRYPT_GRAVE_ROOT.get();
        BlockPos feetPos = entity.blockPosition();
        if (level.getBlockState(feetPos).is(root) || level.getBlockState(feetPos.above()).is(root)) {
            return true;
        }
        BlockPos onPos = entity.getOnPos();
        return level.getBlockState(onPos).is(root) || level.getBlockState(onPos.above()).is(root);
    }
}
