package ddraig.net.entropica.block.biome15;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Wind-Blown Dust-Scrub (Flora 37) — wiry, sand-laden tumbleweed scrub rooted into Grey Sand
 * and Dust-Crusted Ice across 2.15 Dust-Crusted Bergs (never bare glacial ice).
 * Kicks up blinding silt puffs when brushed through unprotected, and rolls downwind during storms!
 */
public class WindBlownDustScrubBlock extends VegetationBlock {
    public static final MapCodec<WindBlownDustScrubBlock> CODEC = simpleCodec(WindBlownDustScrubBlock::new);
    public static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public WindBlownDustScrubBlock(Properties properties) {
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
        if (floor.is(Blocks.ICE)
                || floor.is(Blocks.PACKED_ICE)
                || floor.is(Blocks.BLUE_ICE)
                || floor.is(Blocks.FROSTED_ICE)
                || floor.is(Blocks.SNOW)) {
            return false;
        }
        if ((ModBlocks.GREY_SAND.isBound() && floor.is(ModBlocks.GREY_SAND.get()))
                || (ModBlocks.DUST_CRUSTED_ICE.isBound() && floor.is(ModBlocks.DUST_CRUSTED_ICE.get()))
                || floor.is(Blocks.SAND)
                || floor.is(Blocks.RED_SAND)
                || floor.is(Blocks.COARSE_DIRT)
                || floor.is(Blocks.TERRACOTTA)) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(floor.getBlock());
        if (key != null && "entropica".equals(key.getNamespace())) {
            String path = key.getPath();
            if ("grey_sand".equals(path) || "dust_crusted_ice".equals(path)) {
                return true;
            }
        }
        try {
            return floor.is(BlockTags.SAND);
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (!(entity instanceof LivingEntity living) || !living.isAlive() || living.isSpectator()) {
            return;
        }

        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(living.getType());
        if (entityKey != null && entityKey.getPath().contains("golem")) {
            return;
        }

        entity.makeStuckInBlock(state, new Vec3(0.78D, 0.95D, 0.78D));

        double dx = living.getX() - living.xOld;
        double dz = living.getZ() - living.zOld;
        boolean movingHorizontally = living.getDeltaMovement().horizontalDistanceSqr() > 0.0015D
                || (dx * dx + dz * dz) > 0.0015D;

        if (movingHorizontally
                && !level.isClientSide()
                && level instanceof ServerLevel serverLevel
                && !living.isShiftKeyDown()
                && !living.isSteppingCarefully()) {

            if (serverLevel.getGameTime() % 15L == 0L) {
                serverLevel.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 0.45F, 0.85F);
                serverLevel.playSound(null, pos, SoundEvents.AZALEA_LEAVES_STEP, SoundSource.BLOCKS, 0.6F, 0.9F);

                BlockState dustState = ModBlocks.GREY_SAND.isBound()
                        ? ModBlocks.GREY_SAND.get().defaultBlockState()
                        : Blocks.SAND.defaultBlockState();
                serverLevel.sendParticles(ParticleTypes.ASH,
                        pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                        8, 0.3D, 0.25D, 0.3D, 0.02D);
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        pos.getX() + 0.5D, pos.getY() + 0.45D, pos.getZ() + 0.5D,
                        2, 0.22D, 0.15D, 0.22D, 0.008D);
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, dustState),
                        pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D,
                        4, 0.25D, 0.2D, 0.25D, 0.01D);
            }

            if (!WindBaffleLanternBlock.isProtectedFromDust(serverLevel, living)) {
                living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            }
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if ((level.isRaining() || level.isThundering()) && level.canSeeSky(pos) && random.nextInt(12) == 0) {
            if (WindBaffleLanternBlock.isNearWindBaffleLantern(level, pos, WindBaffleLanternBlock.BAFFLE_RADIUS)) {
                return;
            }

            level.removeBlock(pos, false);

            int dx = 2 + random.nextInt(4); // 2..5 downwind
            int dz = random.nextInt(5) - 2; // -2..2 crosswind

            for (int step = 0; step <= 4; step++) {
                double t = step / 4.0D;
                double px = pos.getX() + 0.5D + dx * t;
                double py = pos.getY() + 0.45D + Math.sin(t * Math.PI) * 0.4D;
                double pz = pos.getZ() + 0.5D + dz * t;
                level.sendParticles(ParticleTypes.ASH, px, py, pz, 4, 0.2D, 0.18D, 0.2D, 0.02D);
                level.sendParticles(ParticleTypes.CLOUD, px, py, pz, 1, 0.15D, 0.12D, 0.15D, 0.015D);
            }

            for (int dy = 2; dy >= -2; dy--) {
                BlockPos target = pos.offset(dx, dy, dz);
                if (target.getY() <= level.getMinY() || target.getY() >= level.getMaxY() - 1) {
                    continue;
                }
                if (!level.hasChunkAt(target)) {
                    continue;
                }
                BlockState targetState = level.getBlockState(target);
                if ((targetState.isAir() || targetState.canBeReplaced())
                        && targetState.getFluidState().isEmpty()
                        && this.mayPlaceOn(level.getBlockState(target.below()), level, target.below())) {
                    level.setBlock(target, this.defaultBlockState(), 3);
                    level.playSound(null, target, SoundEvents.AZALEA_LEAVES_STEP, SoundSource.BLOCKS, 0.55F, 0.85F);
                    break;
                }
            }
        }
    }
}
