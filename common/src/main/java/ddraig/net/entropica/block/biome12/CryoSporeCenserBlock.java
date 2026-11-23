package ddraig.net.entropica.block.biome12;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Cryo-Spore Censer — sculpted copper, Spore-Wood, and Spore-Glazed Ice air-purifying lantern.
 * Protects a 12-block radius against Frozen Spore Moss disorientation, purges Nausea and Poison
 * from nearby players and allies, and grants Spore Clarity.
 */
public class CryoSporeCenserBlock extends LanternBlock {
    public static final MapCodec<CryoSporeCenserBlock> CODEC = simpleCodec(CryoSporeCenserBlock::new);
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected static final VoxelShape STANDING_SHAPE = Block.box(3.25D, 0.0D, 3.25D, 12.75D, 14.75D, 12.75D);
    protected static final VoxelShape HANGING_SHAPE = Block.box(3.25D, 1.0D, 3.25D, 12.75D, 16.0D, 12.75D);

    public CryoSporeCenserBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends LanternBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? HANGING_SHAPE : STANDING_SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        boolean waterlogged = fluidState.getType() == Fluids.WATER;
        Direction clickedFace = context.getClickedFace();

        if (clickedFace == Direction.DOWN) {
            BlockState hangingState = this.defaultBlockState()
                    .setValue(HANGING, true)
                    .setValue(WATERLOGGED, waterlogged);
            if (hangingState.canSurvive(context.getLevel(), context.getClickedPos())) {
                return hangingState;
            }
        } else if (clickedFace == Direction.UP) {
            BlockState standingState = this.defaultBlockState()
                    .setValue(HANGING, false)
                    .setValue(WATERLOGGED, waterlogged);
            if (standingState.canSurvive(context.getLevel(), context.getClickedPos())) {
                return standingState;
            }
        }

        return super.getStateForPlacement(context);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            purifyNearbyAir(serverLevel, pos);
            if (!serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
                serverLevel.scheduleTick(pos, this, 40);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            purifyNearbyAir(serverLevel, pos);
            serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.85F, 1.25F);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                    8, 0.25D, 0.25D, 0.25D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                    6, 0.3D, 0.25D, 0.3D, 0.01D);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        purifyNearbyAir(level, pos);
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 40);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.is(this)) {
            purifyNearbyAir(level, pos);
            if (level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 20.0D, false) != null
                    && !level.getBlockTicks().hasScheduledTick(pos, this)) {
                level.scheduleTick(pos, this, 40);
            }
        }
    }

    public static void purifyNearbyAir(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        double maxDistSq = 12.0D * 12.0D;
        AABB box = new AABB(pos).inflate(12.0D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive()
                        && !e.isSpectator()
                        && (e instanceof Player || !(e instanceof Enemy))
                        && e.position().distanceToSqr(center) <= maxDistSq);

        Holder<MobEffect> clarityHolder = ModEffects.SPORE_CLARITY.isBound()
                ? BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.SPORE_CLARITY.get())
                : null;

        for (LivingEntity target : targets) {
            if (target.hasEffect(MobEffects.NAUSEA)) {
                target.removeEffect(MobEffects.NAUSEA);
            }
            if (target.hasEffect(MobEffects.POISON)) {
                target.removeEffect(MobEffects.POISON);
            }
            if (clarityHolder != null) {
                target.addEffect(new MobEffectInstance(clarityHolder, 120, 0, true, false, true));
            }
        }
    }

    public static boolean isProtectedByCenser(LevelReader level, BlockPos center) {
        if (level == null || center == null || !ModBlocks.CRYO_SPORE_CENSER.isBound()) {
            return false;
        }
        Block censer = ModBlocks.CRYO_SPORE_CENSER.get();
        int minX = center.getX() - 12;
        int maxX = center.getX() + 12;
        int minY = Math.max(level.getMinY(), center.getY() - 6);
        int maxY = Math.min(level.getMaxY() - 1, center.getY() + 6);
        int minZ = center.getZ() - 12;
        int maxZ = center.getZ() + 12;

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
                            if (level.getBlockState(cursor).is(censer)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + 0.32D + random.nextDouble() * 0.36D;
            double y = pos.getY() + (state.getValue(HANGING) ? 0.38D : 0.32D) + random.nextDouble() * 0.34D;
            double z = pos.getZ() + 0.32D + random.nextDouble() * 0.36D;
            if (random.nextBoolean()) {
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
            } else {
                level.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, x, y, z, 0.0D, 0.008D, 0.0D);
            }
        }
    }
}
