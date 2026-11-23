package ddraig.net.entropica.block.biome6;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Vapor Condenser — stepped 3D geothermal alembic condenser.
 * - Redstone Pulse / Powered: releases a 6-block dense vapor cloud, extinguishes fire blocks
 *   within 4 blocks, clears burning on nearby entities, and blinds nearby Monsters (80 ticks).
 * - Passive Condensation: when bordering a Steam Vent, Steaming Spring-Stone, or Steaming Ice,
 *   condenses clean water into adjacent Cauldrons or held empty Buckets.
 */
public class VaporCondenserBlock extends Block {
    public static final MapCodec<VaporCondenserBlock> CODEC = simpleCodec(VaporCondenserBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 4.0D, 15.0D),
            Block.box(3.0D, 4.0D, 3.0D, 13.0D, 12.0D, 13.0D),
            Block.box(2.0D, 12.0D, 2.0D, 14.0D, 16.0D, 14.0D)
    );

    private static final double CLOUD_RADIUS = 6.0D;
    private static final int FIRE_EXTINGUISH_RADIUS = 4;
    private static final int POWERED_PULSE_INTERVAL = 40;

    public VaporCondenserBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            if (hasSignal != state.getValue(POWERED)) {
                state = state.setValue(POWERED, hasSignal);
                level.setBlock(pos, state, 3);
            }
            if (state.getValue(POWERED)) {
                releaseDenseVaporCloud(serverLevel, pos);
                serverLevel.scheduleTick(pos, this, POWERED_PULSE_INTERVAL);
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            boolean wasPowered = state.getValue(POWERED);
            if (hasSignal != wasPowered) {
                serverLevel.setBlock(pos, state.setValue(POWERED, hasSignal), 3);
                if (hasSignal) {
                    releaseDenseVaporCloud(serverLevel, pos);
                    serverLevel.scheduleTick(pos, this, POWERED_PULSE_INTERVAL);
                }
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean hasSignal = level.hasNeighborSignal(pos);
        if (!hasSignal) {
            if (state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, false), 3);
            }
            return;
        }
        if (!state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, true), 3);
        }
        releaseDenseVaporCloud(level, pos);
        level.scheduleTick(pos, this, POWERED_PULSE_INTERVAL);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        tryCondenseSteamIntoWater(level, pos);
        if (state.getValue(POWERED)) {
            releaseDenseVaporCloud(level, pos);
            if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
                level.scheduleTick(pos, this, POWERED_PULSE_INTERVAL);
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.BUCKET) && hasAdjacentSteamSource(level, pos)) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                ItemStack filledBucket = ItemUtils.createFilledResult(stack, player, new ItemStack(Items.WATER_BUCKET));
                player.setItemInHand(hand, filledBucket);
                serverLevel.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.9F, 1.1F);
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                        8, 0.25D, 0.2D, 0.25D, 0.03D);
                serverLevel.sendParticles(ParticleTypes.SPLASH,
                        pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                        10, 0.25D, 0.15D, 0.25D, 0.05D);
                tryCondenseSteamIntoWater(serverLevel, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    public static void releaseDenseVaporCloud(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        level.sendParticles(ParticleTypes.CLOUD,
                center.x, center.y + 0.3D, center.z,
                45, 2.4D, 1.1D, 2.4D, 0.06D);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.85F, 0.95F);

        // Extinguish fire blocks within 4 blocks
        for (BlockPos check : BlockPos.betweenClosed(
                pos.offset(-FIRE_EXTINGUISH_RADIUS, -FIRE_EXTINGUISH_RADIUS, -FIRE_EXTINGUISH_RADIUS),
                pos.offset(FIRE_EXTINGUISH_RADIUS, FIRE_EXTINGUISH_RADIUS, FIRE_EXTINGUISH_RADIUS))) {
            BlockState fireCheck = level.getBlockState(check);
            if (fireCheck.is(Blocks.FIRE) || fireCheck.is(Blocks.SOUL_FIRE)) {
                level.removeBlock(check, false);
            }
        }

        // Clear burning on nearby entities & blind nearby Monsters within 6 blocks
        double maxDistSq = (CLOUD_RADIUS + 0.5D) * (CLOUD_RADIUS + 0.5D);
        AABB box = new AABB(pos).inflate(CLOUD_RADIUS);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, box,
                e -> e.isAlive() && !e.isSpectator() && e.position().distanceToSqr(center) <= maxDistSq)) {
            if (entity.isOnFire() || entity.getRemainingFireTicks() > 0) {
                entity.clearFire();
            }
            if (entity.getTicksFrozen() > 0) {
                entity.setTicksFrozen(0);
            }
            if (entity instanceof Monster monster) {
                monster.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            }
        }
    }

    /**
     * Condenses steam from any adjacent geothermal block into an adjacent Cauldron,
     * filling it into a full 3-level {@link Blocks#WATER_CAULDRON}.
     */
    public static boolean tryCondenseSteamIntoWater(ServerLevel level, BlockPos condenserPos) {
        if (!hasAdjacentSteamSource(level, condenserPos)) {
            return false;
        }
        boolean condensed = false;
        for (Direction dir : Direction.values()) {
            BlockPos cauldronPos = condenserPos.relative(dir);
            BlockState cauldronState = level.getBlockState(cauldronPos);
            boolean canFill = cauldronState.is(Blocks.CAULDRON)
                    || (cauldronState.is(Blocks.WATER_CAULDRON)
                    && cauldronState.getValue(LayeredCauldronBlock.LEVEL) < LayeredCauldronBlock.MAX_FILL_LEVEL);
            if (canFill) {
                BlockState fullWaterCauldron = Blocks.WATER_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL);
                level.setBlock(cauldronPos, fullWaterCauldron, 3);
                level.playSound(null, cauldronPos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.6F, 1.2F);
                level.sendParticles(ParticleTypes.SPLASH,
                        cauldronPos.getX() + 0.5D, cauldronPos.getY() + 0.9D, cauldronPos.getZ() + 0.5D,
                        8, 0.2D, 0.1D, 0.2D, 0.04D);
                condensed = true;
            }
        }
        return condensed;
    }

    public static boolean hasAdjacentSteamSource(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockState adj = level.getBlockState(pos.relative(dir));
            if (ModBlocks.STEAM_VENT.isBound() && adj.is(ModBlocks.STEAM_VENT.get())) {
                return true;
            }
            if (ModBlocks.STEAMING_SPRING_STONE.isBound() && adj.is(ModBlocks.STEAMING_SPRING_STONE.get())) {
                return true;
            }
            if (ModBlocks.STEAMING_ICE.isBound() && adj.is(ModBlocks.STEAMING_ICE.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (state.getValue(POWERED) && random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.CLOUD,
                    pos.getX() + 0.3D + random.nextDouble() * 0.4D,
                    pos.getY() + 1.05D,
                    pos.getZ() + 0.3D + random.nextDouble() * 0.4D,
                    0.0D, 0.06D, 0.0D);
        } else if (random.nextInt(5) == 0 && hasAdjacentSteamSource(level, pos)) {
            level.addParticle(ParticleTypes.DRIPPING_WATER,
                    pos.getX() + 0.3D + random.nextDouble() * 0.4D,
                    pos.getY() + 0.75D,
                    pos.getZ() + 0.3D + random.nextDouble() * 0.4D,
                    0.0D, 0.0D, 0.0D);
        }
    }
}
