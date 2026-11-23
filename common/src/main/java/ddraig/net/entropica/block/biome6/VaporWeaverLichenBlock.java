package ddraig.net.entropica.block.biome6;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vapor-Weaver Lichen — thin 1px multi-face geothermal lichen clinging to damp basalt and ice.
 * Spreads near steam vents, spring-stone, or water, and releases a defensive
 * hot-steam burst when right-clicked with an empty hand (15s cooldown).
 */
public class VaporWeaverLichenBlock extends MultifaceSpreadeableBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<VaporWeaverLichenBlock> CODEC = simpleCodec(VaporWeaverLichenBlock::new);

    private static final long STEAM_BURST_COOLDOWN_TICKS = 300L; // 15 seconds
    private static final double BURST_RADIUS = 3.0D;

    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> POS_COOLDOWNS = new ConcurrentHashMap<>();

    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    public VaporWeaverLichenBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<VaporWeaverLichenBlock> codec() {
        return CODEC;
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return this.spreader;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return state.getFluidState().isEmpty();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) {
            return;
        }
        if (isNearMoistureOrGeothermalHeat(level, pos, 2)) {
            this.spreader.spreadFromRandomFaceTowardRandomDirection(state, level, pos, random);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        long now = level.getGameTime();
        Map<BlockPos, Long> dimMap = POS_COOLDOWNS.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
        long readyAt = dimMap.getOrDefault(pos, 0L);
        if (now < readyAt) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            dimMap.put(pos.immutable(), now + STEAM_BURST_COOLDOWN_TICKS);

            Vec3 center = Vec3.atCenterOf(pos);
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    center.x, center.y, center.z,
                    20, 0.45D, 0.45D, 0.45D, 0.08D);
            serverLevel.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.9F, 1.1F);

            Vec3 look = player.getLookAngle();
            AABB box = new AABB(pos).inflate(BURST_RADIUS);
            double maxDistSq = (BURST_RADIUS + 0.5D) * (BURST_RADIUS + 0.5D);

            for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player && e.isAlive() && !e.isSpectator() && e.position().distanceToSqr(center) <= maxDistSq)) {
                Vec3 away = target.position().subtract(center);
                Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                if (horiz.lengthSqr() < 1.0E-4D) {
                    horiz = new Vec3(look.x, 0.0D, look.z);
                }
                if (horiz.lengthSqr() > 1.0E-4D) {
                    horiz = horiz.normalize();
                } else {
                    horiz = new Vec3(0.0D, 0.0D, 1.0D);
                }

                Vec3 curVel = target.getDeltaMovement();
                target.setDeltaMovement(
                        curVel.x * 0.3D + horiz.x * 0.85D,
                        Math.max(curVel.y, 0.32D),
                        curVel.z * 0.3D + horiz.z * 0.85D
                );
                target.hurtMarked = true;

                // Blindness (3s = 60t) on hostile monsters in front of the player / burst
                if (target instanceof Enemy || target instanceof Monster) {
                    Vec3 fromPlayer = target.position().subtract(player.position());
                    if (fromPlayer.lengthSqr() < 1.0E-4D || fromPlayer.normalize().dot(look) >= -0.25D) {
                        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        Map<BlockPos, Long> dimMap = POS_COOLDOWNS.get(level.dimension());
        if (dimMap != null) {
            dimMap.remove(pos);
        }
    }

    private static boolean isNearMoistureOrGeothermalHeat(ServerLevel level, BlockPos center, int radius) {
        for (BlockPos check : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            BlockState st = level.getBlockState(check);
            if (st.is(Blocks.WATER) || level.getFluidState(check).is(FluidTags.WATER)) {
                return true;
            }
            if (ModBlocks.STEAM_VENT.isBound() && st.is(ModBlocks.STEAM_VENT.get())) {
                return true;
            }
            if (ModBlocks.STEAMING_SPRING_STONE.isBound() && st.is(ModBlocks.STEAMING_SPRING_STONE.get())) {
                return true;
            }
        }
        return false;
    }
}
