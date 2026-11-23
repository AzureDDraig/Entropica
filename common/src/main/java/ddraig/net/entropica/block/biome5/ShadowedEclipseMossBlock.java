package ddraig.net.entropica.block.biome5;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Flora 36 — Shadowed Eclipse-Moss.
 * Dark Eclipse Materia moss carpet clinging to shadowy basalt cliffs in 2.5 Silent Fjord.
 * Spreads slowly in deep gloom (light <= 4), grants Night Vision to entities treading on it,
 * and applies a gentle Shadow Siphon food exhaustion cost to non-creative players.
 */
public class ShadowedEclipseMossBlock extends Block {
    public static final MapCodec<ShadowedEclipseMossBlock> CODEC = simpleCodec(ShadowedEclipseMossBlock::new);
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public ShadowedEclipseMossBlock(Properties properties) {
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
        BlockPos belowPos = pos.below();
        if (level.isEmptyBlock(belowPos)) {
            return false;
        }
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.isFaceSturdy(level, belowPos, Direction.UP)
                || belowState.isCollisionShapeFullBlock(level, belowPos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos) > 4 || random.nextInt(12) != 0) {
            return;
        }
        if (!ModBlocks.SHADOWED_ECLIPSE_MOSS.isBound()) {
            return;
        }

        for (int attempt = 0; attempt < 4; attempt++) {
            BlockPos targetPos = pos.offset(
                    random.nextInt(3) - 1,
                    random.nextInt(3) - 1,
                    random.nextInt(3) - 1
            );
            if (targetPos.equals(pos) || !level.getBlockState(targetPos).isAir()) {
                continue;
            }
            if (level.getMaxLocalRawBrightness(targetPos) > 4) {
                continue;
            }
            BlockState belowTarget = level.getBlockState(targetPos.below());
            if (isValidEclipseSpreadSubstrate(belowTarget)) {
                BlockState mossState = ModBlocks.SHADOWED_ECLIPSE_MOSS.get().defaultBlockState();
                if (mossState.canSurvive(level, targetPos)) {
                    level.setBlock(targetPos, mossState, 2);
                    break;
                }
            }
        }
    }

    private static boolean isValidEclipseSpreadSubstrate(BlockState state) {
        return state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT)
                || state.is(Blocks.POLISHED_BASALT)
                || (ModBlocks.BASALT_COLUMN.isBound() && state.is(ModBlocks.BASALT_COLUMN.get()))
                || state.is(Blocks.OBSIDIAN)
                || state.is(Blocks.RED_SANDSTONE);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (level instanceof ServerLevel && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, true));
            if (entity instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                player.causeFoodExhaustion(0.02F);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        // Rhythmic 4-second (80-tick) Eclipse breathing pulse
        double phase = ((level.getGameTime() % 80L) / 80.0D) * (Math.PI * 2.0D);
        double breath = 0.5D + 0.5D * Math.sin(phase);
        if (random.nextDouble() < (0.10D + 0.35D * breath)) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.08D + random.nextDouble() * 0.06D;
            double z = pos.getZ() + random.nextDouble();
            if (breath > 0.75D && random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.PORTAL, x, y + 0.05D, z,
                        (random.nextDouble() - 0.5D) * 0.04D,
                        0.02D,
                        (random.nextDouble() - 0.5D) * 0.04D);
            } else if (random.nextBoolean()) {
                level.addParticle(ParticleTypes.MYCELIUM, x, y, z, 0.0D, 0.008D, 0.0D);
            } else {
                level.addParticle(ParticleTypes.ASH, x, y + 0.04D, z, 0.0D, 0.005D, 0.0D);
            }
        }
    }
}
