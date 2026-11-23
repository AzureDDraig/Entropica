package ddraig.net.entropica.block.biome15;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.effect.ZephyrWeightEffect;
import ddraig.net.entropica.item.biome12.SporeFilterMaskItem;
import ddraig.net.entropica.item.biome13.MirageVeilGogglesItem;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wind-Baffle Lantern — sculpted Tuff Brick, Cut Copper, Gold wrap-louver, and glowing Dust-Core storm beacon.
 * Calms abrasive dust gales in a 20-block radius, granting Zephyr Weight and cleansing Blindness and Darkness.
 */
public class WindBaffleLanternBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<WindBaffleLanternBlock> CODEC = simpleCodec(WindBaffleLanternBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final int BAFFLE_RADIUS = 20;

    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 3.0D, 13.0D),
            Block.box(3.8D, 3.0D, 3.8D, 12.2D, 11.5D, 12.2D),
            Block.box(2.5D, 11.5D, 2.5D, 13.5D, 16.0D, 13.5D)
    );

    private static final Set<GlobalPos> ACTIVE_LANTERNS = ConcurrentHashMap.newKeySet();

    public WindBaffleLanternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        ACTIVE_LANTERNS.add(GlobalPos.of(level.dimension(), pos.immutable()));
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            pulseCalmAura(serverLevel, pos);
            if (!serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
                serverLevel.scheduleTick(pos, this, 40);
            }
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!level.getBlockState(pos).is(this)) {
            ACTIVE_LANTERNS.remove(GlobalPos.of(level.dimension(), pos));
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        ACTIVE_LANTERNS.add(GlobalPos.of(level.dimension(), pos.immutable()));
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            pulseCalmAura(serverLevel, pos);
            serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, 1.2F);
            serverLevel.playSound(null, pos, SoundEvents.LANTERN_HIT, SoundSource.BLOCKS, 0.7F, 1.1F);
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                    8, 0.28D, 0.2D, 0.28D, 0.015D);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                    8, 0.25D, 0.25D, 0.25D, 0.02D);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        pulseCalmAura(level, pos);
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 40);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.is(this)) {
            pulseCalmAura(level, pos);
            if (level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 28.0D, false) != null
                    && !level.getBlockTicks().hasScheduledTick(pos, this)) {
                level.scheduleTick(pos, this, 40);
            }
        }
    }

    public static void pulseCalmAura(ServerLevel level, BlockPos pos) {
        ACTIVE_LANTERNS.add(GlobalPos.of(level.dimension(), pos.immutable()));

        Vec3 center = Vec3.atCenterOf(pos);
        double radiusSq = (double) BAFFLE_RADIUS * (double) BAFFLE_RADIUS;
        AABB box = new AABB(pos).inflate(BAFFLE_RADIUS);
        List<Player> players = level.getEntitiesOfClass(Player.class, box,
                p -> p.isAlive() && !p.isSpectator() && p.position().distanceToSqr(center) <= radiusSq);

        Holder<MobEffect> zephyrHolder = resolveZephyrWeightHolder();
        for (Player player : players) {
            if (player.hasEffect(MobEffects.BLINDNESS)) {
                player.removeEffect(MobEffects.BLINDNESS);
            }
            if (player.hasEffect(MobEffects.DARKNESS)) {
                player.removeEffect(MobEffects.DARKNESS);
            }
            if (zephyrHolder != null) {
                player.addEffect(new MobEffectInstance(zephyrHolder, 260, 0, true, false, true));
            }
        }
    }

    private static Holder<MobEffect> resolveZephyrWeightHolder() {
        if (ModEffects.ZEPHYR_WEIGHT.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.ZEPHYR_WEIGHT.get());
        }
        MobEffect fallback = BuiltInRegistries.MOB_EFFECT
                .getOptional(ResourceLocation.fromNamespaceAndPath("entropica", "zephyr_weight"))
                .orElse(null);
        return fallback != null ? BuiltInRegistries.MOB_EFFECT.wrapAsHolder(fallback) : null;
    }

    public static boolean isNearWindBaffleLantern(Level level, BlockPos center, int radius) {
        if (level == null || center == null || !ModBlocks.WIND_BAFFLE_LANTERN.isBound()) {
            return false;
        }
        Block lantern = ModBlocks.WIND_BAFFLE_LANTERN.get();
        double maxDistSq = (double) radius * (double) radius;

        for (GlobalPos gp : ACTIVE_LANTERNS) {
            if (!gp.dimension().equals(level.dimension())) {
                continue;
            }
            BlockPos p = gp.pos();
            if (p.distSqr(center) > maxDistSq) {
                continue;
            }
            if (!level.hasChunkAt(p)) {
                continue;
            }
            if (level.getBlockState(p).is(lantern)) {
                return true;
            } else {
                ACTIVE_LANTERNS.remove(gp);
            }
        }

        // Compact fallback check for newly generated worldgen lanterns before their first tick
        int scanR = Math.min(radius, 8);
        int minX = center.getX() - scanR;
        int maxX = center.getX() + scanR;
        int minY = Math.max(level.getMinY(), center.getY() - 5);
        int maxY = Math.min(level.getMaxY() - 1, center.getY() + 5);
        int minZ = center.getZ() - scanR;
        int maxZ = center.getZ() + scanR;

        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                cursor.set(cx << 4, center.getY(), cz << 4);
                if (!level.hasChunkAt(cursor)) {
                    continue;
                }
                int x0 = Math.max(minX, cx << 4);
                int x1 = Math.min(maxX, (cx << 4) + 15);
                int z0 = Math.max(minZ, cz << 4);
                int z1 = Math.min(maxZ, (cz << 4) + 15);
                for (int y = minY; y <= maxY; y++) {
                    for (int x = x0; x <= x1; x++) {
                        for (int z = z0; z <= z1; z++) {
                            cursor.set(x, y, z);
                            if (cursor.distSqr(center) <= maxDistSq && level.getBlockState(cursor).is(lantern)) {
                                ACTIVE_LANTERNS.add(GlobalPos.of(level.dimension(), cursor.immutable()));
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isProtectedFromDust(Level level, LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (ZephyrWeightEffect.hasZephyrWeight(entity)) {
            return true;
        }
        if (MirageVeilGogglesItem.hasMirageVeilGoggles(entity)) {
            return true;
        }
        if (SporeFilterMaskItem.hasSporeFilterMask(entity)) {
            return true;
        }
        return level != null && isNearWindBaffleLantern(level, entity.blockPosition(), BAFFLE_RADIUS);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        ACTIVE_LANTERNS.add(GlobalPos.of(level.dimension(), pos.immutable()));
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + 0.30D + random.nextDouble() * 0.40D;
            double y = pos.getY() + 0.32D + random.nextDouble() * 0.38D;
            double z = pos.getZ() + 0.30D + random.nextDouble() * 0.40D;
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }
}
