package ddraig.net.entropica.block.biome17;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.block.EntropicaFloraGroundHelper;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Celestial Orrery Lantern — sculpted Engraved Astral Slate, gilded gimbal ring, Astral Glass,
 * and Stardust-Nacre beacon of 2.17 Astral Skerries. Emits full Light 15, pulses a continuous
 * 18-block aura every 40 ticks granting Starlight Grace, thawing freeze, purging Darkness & Blindness,
 * exposing invisible creatures with Glowing, and nurturing nearby budding Astral crystals at night.
 */
public class CelestialOrreryLanternBlock extends LanternBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<CelestialOrreryLanternBlock> CODEC = simpleCodec(CelestialOrreryLanternBlock::new);
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final double AURA_RADIUS = 18.0D;
    public static final int CRYSTAL_RESONANCE_RADIUS = 6;

    protected static final VoxelShape STANDING_SHAPE = Shapes.or(
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 2.5D, 13.0D),
            Block.box(2.5D, 2.5D, 2.5D, 13.5D, 11.5D, 13.5D),
            Block.box(4.0D, 11.5D, 4.0D, 12.0D, 15.0D, 12.0D)
    );

    protected static final VoxelShape HANGING_SHAPE = Shapes.or(
            Block.box(3.0D, 1.0D, 3.0D, 13.0D, 3.0D, 13.0D),
            Block.box(2.5D, 3.0D, 2.5D, 13.5D, 11.75D, 13.5D),
            Block.box(4.0D, 11.75D, 4.0D, 12.0D, 16.0D, 12.0D)
    );

    public CelestialOrreryLanternBlock(Properties properties) {
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
                    || (ModBlocks.STARLIGHT_ICE.isBound() && below.is(ModBlocks.STARLIGHT_ICE.get()));
        }
        BlockState above = level.getBlockState(pos.above());
        return EntropicaFloraGroundHelper.isAnyIce(above)
                || (ModBlocks.STARLIGHT_ICE.isBound() && above.is(ModBlocks.STARLIGHT_ICE.get()));
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
            pulseCelestialOrreryAura(serverLevel, pos, serverLevel.random);
            serverLevel.scheduleTick(pos, this, 40);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        pulseCelestialOrreryAura(level, pos, random);
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 40);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.is(this)) {
            return;
        }
        pulseCelestialOrreryAura(level, pos, random);
        level.scheduleTick(pos, this, 40);
    }

    public static void pulseCelestialOrreryAura(ServerLevel level, BlockPos pos, RandomSource random) {
        AABB box = new AABB(pos).inflate(AURA_RADIUS);

        Holder<MobEffect> graceHolder = resolveStarlightGraceHolder();
        List<Player> players = level.getEntitiesOfClass(Player.class, box,
                p -> p.isAlive() && !p.isSpectator());
        for (Player player : players) {
            if (player.hasEffect(MobEffects.DARKNESS)) {
                player.removeEffect(MobEffects.DARKNESS);
            }
            if (player.hasEffect(MobEffects.BLINDNESS)) {
                player.removeEffect(MobEffects.BLINDNESS);
            }
            if (player.getTicksFrozen() > 0) {
                player.setTicksFrozen(0);
            }
            if (graceHolder != null) {
                player.addEffect(new MobEffectInstance(graceHolder, 240, 0, true, false, true));
            }
        }

        List<LivingEntity> invisibleEntities = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> !(e instanceof Player) && e.isAlive() && !e.isSpectator() && e.isInvisible());
        for (LivingEntity entity : invisibleEntities) {
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
        }

        if (StarlightIceBlock.isNightSky(level)) {
            int span = CRYSTAL_RESONANCE_RADIUS * 2 + 1;
            for (int i = 0; i < 2; i++) {
                BlockPos targetPos = pos.offset(
                        random.nextInt(span) - CRYSTAL_RESONANCE_RADIUS,
                        random.nextInt(span) - CRYSTAL_RESONANCE_RADIUS,
                        random.nextInt(span) - CRYSTAL_RESONANCE_RADIUS
                );
                if (!level.hasChunkAt(targetPos)) {
                    continue;
                }
                BlockState targetState = level.getBlockState(targetPos);
                ResourceLocation key = BuiltInRegistries.BLOCK.getKey(targetState.getBlock());
                if (key != null && (key.getPath().contains("budding_astral") || key.getPath().contains("astral_crystal"))) {
                    targetState.randomTick(level, targetPos, random);
                    level.sendParticles(ParticleTypes.END_ROD,
                            targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D,
                            3, 0.25D, 0.25D, 0.25D, 0.015D);
                }
            }
        }
    }

    private static Holder<MobEffect> resolveStarlightGraceHolder() {
        if (ModEffects.STARLIGHT_GRACE.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.STARLIGHT_GRACE.get());
        }
        MobEffect fallback = BuiltInRegistries.MOB_EFFECT
                .getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_grace"))
                .orElse(null);
        return fallback != null ? BuiltInRegistries.MOB_EFFECT.wrapAsHolder(fallback) : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            pulseCelestialOrreryAura(serverLevel, pos, serverLevel.random);
            if (!serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
                serverLevel.scheduleTick(pos, this, 40);
            }
            serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, 1.35F);
            serverLevel.playSound(null, pos, SoundEvents.LANTERN_HIT, SoundSource.BLOCKS, 0.65F, 1.2F);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                    8, 0.25D, 0.25D, 0.25D, 0.02D);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(6) == 0) {
            double yBase = state.getValue(HANGING) ? 0.58D : 0.50D;
            double x = pos.getX() + 0.28D + random.nextDouble() * 0.44D;
            double y = pos.getY() + yBase + (random.nextDouble() - 0.5D) * 0.28D;
            double z = pos.getZ() + 0.28D + random.nextDouble() * 0.44D;
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }
}
