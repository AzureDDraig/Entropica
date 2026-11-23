package ddraig.net.entropica.block.biome13;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.effect.DoubleVisionEffect;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 * Mirage Prism Lantern — sculpted Glass Sandstone, copper, gold, and Mirage Glass beacon.
 * Stabilizes refractive distortions in a 16-block radius, purges Double Vision, Blindness,
 * and Darkness from nearby players, and cloaks sneaking players within 8 blocks in Invisibility.
 */
public class MiragePrismLanternBlock extends LanternBlock {
    public static final MapCodec<MiragePrismLanternBlock> CODEC = simpleCodec(MiragePrismLanternBlock::new);
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected static final VoxelShape STANDING_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 15.0D, 13.0D);
    protected static final VoxelShape HANGING_SHAPE = Block.box(3.0D, 1.0D, 3.0D, 13.0D, 16.0D, 13.0D);

    public MiragePrismLanternBlock(Properties properties) {
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
            pulsePrismClarity(serverLevel, pos);
            if (!serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
                serverLevel.scheduleTick(pos, this, 40);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            pulsePrismClarity(serverLevel, pos);
            serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, 1.35F);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                    10, 0.28D, 0.28D, 0.28D, 0.02D);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        pulsePrismClarity(level, pos);
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 40);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.is(this)) {
            pulsePrismClarity(level, pos);
            if (level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 24.0D, false) != null
                    && !level.getBlockTicks().hasScheduledTick(pos, this)) {
                level.scheduleTick(pos, this, 40);
            }
        }
    }

    public static void pulsePrismClarity(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        double purgeDistSq = 16.0D * 16.0D;
        double cloakDistSq = 8.0D * 8.0D;
        AABB box = new AABB(pos).inflate(16.0D);
        List<Player> players = level.getEntitiesOfClass(Player.class, box,
                p -> p.isAlive() && !p.isSpectator() && p.position().distanceToSqr(center) <= purgeDistSq);

        for (Player player : players) {
            DoubleVisionEffect.removeDoubleVision(player);
            if (player.hasEffect(MobEffects.BLINDNESS)) {
                player.removeEffect(MobEffects.BLINDNESS);
            }
            if (player.hasEffect(MobEffects.DARKNESS)) {
                player.removeEffect(MobEffects.DARKNESS);
            }
            if ((player.isCrouching() || player.isShiftKeyDown() || player.isSteppingCarefully())
                    && player.position().distanceToSqr(center) <= cloakDistSq) {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, true, false, true));
            }
        }
    }

    public static boolean isProtectedByLantern(LevelReader level, BlockPos center) {
        if (level == null || center == null || !ModBlocks.MIRAGE_PRISM_LANTERN.isBound()) {
            return false;
        }
        Block lantern = ModBlocks.MIRAGE_PRISM_LANTERN.get();
        int minX = center.getX() - 16;
        int maxX = center.getX() + 16;
        int minY = Math.max(level.getMinY(), center.getY() - 8);
        int maxY = Math.min(level.getMaxY() - 1, center.getY() + 8);
        int minZ = center.getZ() - 16;
        int maxZ = center.getZ() + 16;

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
                            if (level.getBlockState(cursor).is(lantern)) {
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
            double x = pos.getX() + 0.30D + random.nextDouble() * 0.40D;
            double y = pos.getY() + (state.getValue(HANGING) ? 0.36D : 0.30D) + random.nextDouble() * 0.36D;
            double z = pos.getZ() + 0.30D + random.nextDouble() * 0.40D;
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }
}
