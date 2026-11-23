package ddraig.net.entropica.block.biome13;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.item.biome13.MirageVeilGogglesItem;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Mirage Ice — refractive Glacial & Mirage Materia ice sheet with seamless connected faces.
 * Temporarily phases intangible underfoot when walked across without sneaking or Mirage-Veil Goggles,
 * responds to Redstone power, deals immediate suffocation damage when solid around an entity's head,
 * and inflicts Double Vision unless protected by Mirage-Veil Goggles or a Mirage Prism Lantern.
 */
public class MirageIceBlock extends HalfTransparentBlock {
    public static final MapCodec<MirageIceBlock> CODEC = simpleCodec(MirageIceBlock::new);
    public static final BooleanProperty PHASED = BooleanProperty.create("phased");

    public MirageIceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PHASED, false));
    }

    @Override
    protected MapCodec<? extends HalfTransparentBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASED);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(PHASED) ? Shapes.empty() : Shapes.block();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return this.defaultBlockState().setValue(PHASED, powered);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        boolean powered = serverLevel.hasNeighborSignal(pos);
        if (powered) {
            if (!state.getValue(PHASED)) {
                serverLevel.setBlock(pos, state.setValue(PHASED, true), 3);
            }
        } else if (state.getValue(PHASED) && !serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
            serverLevel.scheduleTick(pos, this, 4);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!state.getValue(PHASED)
                && !serverLevel.hasNeighborSignal(pos)
                && entity instanceof LivingEntity living
                && !(living instanceof ddraig.net.entropica.entity.silica_glass_scarab.SilicaGlassScarabEntity)
                && !entity.isSteppingCarefully()
                && !MirageVeilGogglesItem.hasMirageVeilGoggles(living)
                && !ddraig.net.entropica.effect.MiragePhasingEffect.hasMiragePhasing(living)) {
            if (!serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
                serverLevel.scheduleTick(pos, this, 12);
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.is(this)) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (!state.getValue(PHASED)) {
            if (!powered) {
                level.setBlock(pos, state.setValue(PHASED, true), 3);
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.7F, 1.35F);
                level.sendParticles(ParticleTypes.END_ROD,
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        10, 0.35D, 0.35D, 0.35D, 0.02D);
                level.scheduleTick(pos, this, 60);
            } else {
                level.setBlock(pos, state.setValue(PHASED, true), 3);
            }
        } else if (!powered) {
            level.setBlock(pos, state.setValue(PHASED, false), 3);
            level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 0.75F, 1.15F);
            List<LivingEntity> enclosed = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(pos).inflate(0.1D),
                    LivingEntity::isAlive
            );
            for (LivingEntity living : enclosed) {
                applyHeadInIceEffects(level, living, false);
            }
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (!level.isClientSide()
                && level instanceof ServerLevel serverLevel
                && entity instanceof LivingEntity living
                && living.isAlive()) {
            applyHeadInIceEffects(serverLevel, living, state.getValue(PHASED));
        }
    }

    public static void applyHeadInIceEffects(ServerLevel level, LivingEntity living, boolean isPhased) {
        if (level == null || living == null || !living.isAlive() || living.isSpectator()) {
            return;
        }
        BlockPos eyePos = BlockPos.containing(living.getX(), living.getEyeY(), living.getZ());
        BlockState eyeState = level.getBlockState(eyePos);
        if (!(eyeState.getBlock() instanceof MirageIceBlock)) {
            return;
        }

        boolean effectivePhased = isPhased || eyeState.getValue(PHASED);
        if (!effectivePhased) {
            living.hurtServer(level, level.damageSources().inWall(), 2.0F);
        }

        if (!MirageVeilGogglesItem.hasMirageVeilGoggles(living)
                && !MiragePrismLanternBlock.isProtectedByLantern(level, living.blockPosition())
                && ModEffects.DOUBLE_VISION.isBound()) {
            living.addEffect(new MobEffectInstance(
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.DOUBLE_VISION.get()),
                    200, 0, false, true, true
            ));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        boolean phased = state.getValue(PHASED);
        if (random.nextInt(phased ? 6 : 20) != 0) {
            return;
        }
        if (!phased && !level.getBlockState(pos.above()).isAir()) {
            return;
        }
        double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
        double y = pos.getY() + (phased ? 0.2D + random.nextDouble() * 0.6D : 1.03D);
        double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
        level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.008D, 0.0D);
    }
}
