package ddraig.net.entropica.block.biome17;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Rime-Star Tussock — hardy, frost-glinting Astral sedge of 2.17 Astral Skerries.
 * Anchors directly onto Starlight Ice, glacial ice, Astral Impactite, gravel, and bare overworld stone,
 * gently warms passing travelers by easing freeze buildup, and outlines invisible entities with starlight motes.
 */
public class RimeStarTussockBlock extends VegetationBlock {
    public static final MapCodec<RimeStarTussockBlock> CODEC = simpleCodec(RimeStarTussockBlock::new);
    public static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public RimeStarTussockBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter level, BlockPos pos) {
        if (floor == null || floor.isAir()) {
            return false;
        }
        if ((ModBlocks.STARLIGHT_ICE.isBound() && floor.is(ModBlocks.STARLIGHT_ICE.get()))
                || floor.is(Blocks.BLUE_ICE)
                || floor.is(Blocks.PACKED_ICE)
                || floor.is(Blocks.ICE)
                || (ModBlocks.ASTRAL_IMPACTITE.isBound() && floor.is(ModBlocks.ASTRAL_IMPACTITE.get()))
                || (ModBlocks.BUDDING_ASTRAL_IMPACTITE.isBound() && floor.is(ModBlocks.BUDDING_ASTRAL_IMPACTITE.get()))
                || (ModBlocks.STARDUST_CRYSTAL_BLOCK.isBound() && floor.is(ModBlocks.STARDUST_CRYSTAL_BLOCK.get()))
                || (ModBlocks.COARSE_GRAVEL.isBound() && floor.is(ModBlocks.COARSE_GRAVEL.get()))
                || floor.is(Blocks.GRAVEL)
                || floor.is(Blocks.STONE)
                || floor.is(Blocks.ANDESITE)
                || floor.is(Blocks.DEEPSLATE)
                || floor.is(Blocks.TUFF)) {
            return true;
        }

        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(floor.getBlock());
        if (key != null && Entropica.MODID.equals(key.getNamespace())) {
            String path = key.getPath();
            if ("starlight_ice".equals(path)
                    || "astral_impactite".equals(path)
                    || "budding_astral_impactite".equals(path)
                    || "stardust_crystal_block".equals(path)
                    || "coarse_gravel".equals(path)) {
                return true;
            }
        }

        try {
            return floor.is(BlockTags.DIRT) || floor.is(BlockTags.BASE_STONE_OVERWORLD);
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (entity instanceof Player player && player.getTicksFrozen() > 0) {
                player.setTicksFrozen(Math.max(0, player.getTicksFrozen() - 2));
            }
            if (entity instanceof LivingEntity le && le.isAlive() && !le.isSpectator() && le.isInvisible()) {
                le.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        pos.getX() + 0.5D, pos.getY() + 0.45D, pos.getZ() + 0.5D,
                        6, 0.25D, 0.2D, 0.25D, 0.02D);
            }
        }
    }
}
