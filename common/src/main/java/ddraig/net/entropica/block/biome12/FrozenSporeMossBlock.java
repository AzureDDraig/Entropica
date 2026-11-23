package ddraig.net.entropica.block.biome12;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.EntropicaFloraGroundHelper;
import ddraig.net.entropica.effect.SporeClarityEffect;
import ddraig.net.entropica.item.biome12.SporeFilterMaskItem;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * Frozen Spore Moss — frost-crusted golden mycelial turf of 2.12 Spore-Glazed Pack.
 * Spreads across snow and soil with Bone Meal, and puffs disorienting spores when stepped on unprotected.
 */
public class FrozenSporeMossBlock extends Block implements BonemealableBlock {
    public static final MapCodec<FrozenSporeMossBlock> CODEC = simpleCodec(FrozenSporeMossBlock::new);

    private static final Set<String> SPORE_NATIVE_FAUNA = Set.of(
            "mycelial_spore_weaver",
            "spore_drifter",
            "overgrowth_ovis",
            "grot"
    );

    public FrozenSporeMossBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide()
                && level instanceof ServerLevel serverLevel
                && entity.tickCount % 20 == 0
                && !entity.isSteppingCarefully()
                && entity instanceof LivingEntity living
                && !living.isSpectator()) {
            ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(living.getType());
            if (entityKey != null && SPORE_NATIVE_FAUNA.contains(entityKey.getPath())) {
                return;
            }
            if (SporeFilterMaskItem.hasSporeFilterMask(living)
                    || SporeClarityEffect.hasSporeClarity(living)
                    || CryoSporeCenserBlock.isProtectedByCenser(level, pos)) {
                return;
            }

            living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0, false, true, true));
            serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                    6, 0.3D, 0.15D, 0.3D, 0.02D);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        BlockState above = level.getBlockState(pos.above());
        return above.isAir() || above.is(Blocks.SNOW);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public Type getType() {
        return Type.NEIGHBOR_SPREADER;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        boolean mossBound = ModBlocks.FROZEN_SPORE_MOSS.isBound();
        boolean carpetBound = ModBlocks.FROZEN_SPORE_MOSS_CARPET.isBound();
        boolean puffballBound = ModBlocks.SPORE_BURST_PUFFBALL.isBound();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos targetPos = pos.offset(dx, dy, dz);
                    BlockPos abovePos = targetPos.above();
                    BlockState aboveState = level.getBlockState(abovePos);
                    if (!aboveState.isAir() && !aboveState.is(Blocks.SNOW)) {
                        continue;
                    }

                    BlockState targetState = level.getBlockState(targetPos);
                    if (EntropicaFloraGroundHelper.isAnyIce(targetState)) {
                        continue;
                    }

                    boolean converted = false;
                    if (!targetPos.equals(pos) && mossBound && isConvertibleSubstrate(targetState)) {
                        int manhattan = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                        if (manhattan == 1 || random.nextFloat() < 0.75F) {
                            targetState = ModBlocks.FROZEN_SPORE_MOSS.get().defaultBlockState();
                            level.setBlock(targetPos, targetState, 3);
                            converted = true;
                        }
                    }

                    boolean canHostCover = converted
                            || (mossBound && targetState.is(ModBlocks.FROZEN_SPORE_MOSS.get()))
                            || (!EntropicaFloraGroundHelper.isIceOrSnowLayer(targetState)
                            && (targetState.isFaceSturdy(level, targetPos, Direction.UP)
                            || EntropicaFloraGroundHelper.isSolidSnowBlock(targetState)));

                    if (canHostCover && (!targetPos.equals(pos) || aboveState.is(Blocks.SNOW)) && random.nextFloat() < 0.60F) {
                        BlockState coverState = null;
                        if (puffballBound && random.nextFloat() < 0.18F) {
                            BlockState candidate = ModBlocks.SPORE_BURST_PUFFBALL.get().defaultBlockState();
                            if (candidate.canSurvive(level, abovePos)) {
                                coverState = candidate;
                            }
                        }
                        if (coverState == null && carpetBound) {
                            BlockState candidate = ModBlocks.FROZEN_SPORE_MOSS_CARPET.get().defaultBlockState();
                            if (candidate.canSurvive(level, abovePos)) {
                                coverState = candidate;
                            }
                        }
                        if (coverState != null) {
                            level.setBlock(abovePos, coverState, 3);
                        } else if (converted && aboveState.is(Blocks.SNOW)) {
                            level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    private static boolean isConvertibleSubstrate(BlockState state) {
        return state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MUD)
                || state.is(Blocks.MYCELIUM);
    }
}
