package ddraig.net.entropica.block.biome16;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.EntropicaFloraGroundHelper;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Crimson Tide Lantern — sculpted Deepslate Tile, Cut Copper, Blood Glass, and Sanguine Heart-Gel
 * beacon of 2.16 Blood-Ice Shallows. Emits full Light 15 without melting Sanguine Ice, thaws
 * frostbite and purges Weakness & Mining Fatigue within 16 blocks, and radiates Regeneration I to
 * swimmers in water or travelers standing upon Sanguine Ice.
 */
public class CrimsonTideLanternBlock extends LanternBlock {
    public static final MapCodec<CrimsonTideLanternBlock> CODEC = simpleCodec(CrimsonTideLanternBlock::new);
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final double AURA_RADIUS = 16.0D;

    protected static final VoxelShape STANDING_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 15.0D, 13.0D);
    protected static final VoxelShape HANGING_SHAPE = Block.box(3.0D, 1.0D, 3.0D, 13.0D, 16.0D, 13.0D);

    public CrimsonTideLanternBlock(Properties properties) {
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

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (super.canSurvive(state, level, pos)) {
            return true;
        }
        if (!state.getValue(HANGING)) {
            BlockState below = level.getBlockState(pos.below());
            return EntropicaFloraGroundHelper.isAnyIce(below)
                    || (ModBlocks.SANGUINE_ICE.isBound() && below.is(ModBlocks.SANGUINE_ICE.get()));
        }
        BlockState above = level.getBlockState(pos.above());
        return EntropicaFloraGroundHelper.isAnyIce(above)
                || (ModBlocks.SANGUINE_ICE.isBound() && above.is(ModBlocks.SANGUINE_ICE.get()));
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
            pulseSanguineTideAura(serverLevel, pos);
            serverLevel.scheduleTick(pos, this, 40);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        pulseSanguineTideAura(level, pos);
        level.scheduleTick(pos, this, 40);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.is(this)) {
            return;
        }
        pulseSanguineTideAura(level, pos);
        level.scheduleTick(pos, this, 40);
    }

    public static void pulseSanguineTideAura(ServerLevel level, BlockPos pos) {
        AABB box = new AABB(pos).inflate(AURA_RADIUS);
        List<Player> players = level.getEntitiesOfClass(Player.class, box,
                p -> p.isAlive() && !p.isSpectator());

        for (Player player : players) {
            if (player.getTicksFrozen() > 0) {
                player.setTicksFrozen(0);
            }
            if (player.hasEffect(MobEffects.WEAKNESS)) {
                player.removeEffect(MobEffects.WEAKNESS);
            }
            if (player.hasEffect(MobEffects.MINING_FATIGUE)) {
                player.removeEffect(MobEffects.MINING_FATIGUE);
            }

            if (player.isInWater() || isStandingOnSanguineIce(level, player)) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, false, true));
            }
        }
    }

    private static boolean isStandingOnSanguineIce(ServerLevel level, Player player) {
        BlockState onState = level.getBlockState(player.getOnPos());
        if (isSanguineIceState(onState)) {
            return true;
        }
        BlockState belowState = level.getBlockState(player.blockPosition().below());
        return isSanguineIceState(belowState);
    }

    private static boolean isSanguineIceState(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (ModBlocks.SANGUINE_ICE.isBound() && state.is(ModBlocks.SANGUINE_ICE.get())) {
            return true;
        }
        if (state.getBlock() instanceof SanguineIceBlock) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return key != null && "entropica".equals(key.getNamespace()) && "sanguine_ice".equals(key.getPath());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            pulseSanguineTideAura(serverLevel, pos);
            if (!serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
                serverLevel.scheduleTick(pos, this, 40);
            }
            serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.85F, 1.25F);
            serverLevel.playSound(null, pos, SoundEvents.LANTERN_HIT, SoundSource.BLOCKS, 0.65F, 1.1F);
            serverLevel.sendParticles(new DustParticleOptions(0xD42A48, 0.9F),
                    pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                    8, 0.25D, 0.25D, 0.25D, 0.02D);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(7) == 0) {
            double yBase = state.getValue(HANGING) ? 0.55D : 0.48D;
            double x = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
            double y = pos.getY() + yBase + (random.nextDouble() - 0.5D) * 0.25D;
            double z = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
            if (random.nextInt(4) == 0) {
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
            } else {
                level.addParticle(new DustParticleOptions(0xD42A48, 0.75F), x, y, z, 0.0D, 0.012D, 0.0D);
            }
        }
    }
}
