package ddraig.net.entropica.block.biome10;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Qilin Light-Refractor — sculpted jade-and-gold optical prism tuned to Radiant Materia.
 * Emits a Redstone signal (0..15) proportional to open sky brightness and periodically reveals
 * invisible or hostile creatures within 12 blocks with Glowing while sunlit.
 */
public class QilinLightRefractorBlock extends Block {
    public static final MapCodec<QilinLightRefractorBlock> CODEC = simpleCodec(QilinLightRefractorBlock::new);

    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 3, 15),
            Block.box(3, 3, 3, 13, 12, 13),
            Block.box(2, 12, 2, 14, 14, 14),
            Block.box(5, 14, 5, 11, 16, 11)
    );

    private static final int PULSE_INTERVAL_TICKS = 40;
    private static final double REVEAL_RADIUS = 12.0D;

    public QilinLightRefractorBlock(Properties properties) {
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
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level instanceof Level realLevel) {
            return computeSkySignal(realLevel, pos);
        }
        return 0;
    }

    public static int computeSkySignal(Level level, BlockPos pos) {
        int raw = level.getBrightness(LightLayer.SKY, pos) - level.getSkyDarken();
        return Mth.clamp(raw, 0, 15);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            level.updateNeighborsAt(pos, this);
            if (!level.getBlockTicks().willTickThisTick(pos, this)) {
                level.scheduleTick(pos, this, PULSE_INTERVAL_TICKS);
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   @Nullable Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, isMoving);
        if (!level.isClientSide() && !level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, PULSE_INTERVAL_TICKS);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.updateNeighborsAt(pos, this);

        int signal = computeSkySignal(level, pos);
        if (signal >= 8) {
            AABB searchBox = new AABB(pos).inflate(REVEAL_RADIUS);
            double maxDistSq = (REVEAL_RADIUS + 0.5D) * (REVEAL_RADIUS + 0.5D);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                    entity -> entity.isAlive()
                            && !entity.isSpectator()
                            && entity.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= maxDistSq
                            && (entity instanceof Monster
                                || (entity.isInvisible() && !(entity instanceof Player p && p.isCreative()))));
            for (LivingEntity target : targets) {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, true, true, true));
            }
        }

        if (!level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, PULSE_INTERVAL_TICKS);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!isMoving) {
            level.updateNeighborsAt(pos, this);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
            double y = pos.getY() + 0.55D + random.nextDouble() * 0.55D;
            double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
            if (ModParticles.SPECTRUM_SPARKLE.isBound() && random.nextBoolean()) {
                level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 0.35F, 0.95F, 0.68F);
            } else {
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.015D, 0.0D);
            }
        }
    }
}
