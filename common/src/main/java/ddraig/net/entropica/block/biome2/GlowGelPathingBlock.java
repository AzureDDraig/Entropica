package ddraig.net.entropica.block.biome2;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Glow-Gel Pathing — bouncy bioluminescent road block compounded from Auroral Gelatin
 * and Coarse Gravel. Illuminates underfoot and grants a swift movement speed boost.
 */
public class GlowGelPathingBlock extends HalfTransparentBlock {
    public static final MapCodec<GlowGelPathingBlock> CODEC = simpleCodec(GlowGelPathingBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final int RESET_DELAY_TICKS = 40;
    private static final int SPEED_DURATION_TICKS = 60;

    public GlowGelPathingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends HalfTransparentBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (entity instanceof LivingEntity living) {
            if (!level.isClientSide()) {
                if (!state.getValue(LIT)) {
                    level.setBlock(pos, state.setValue(LIT, true), 3);
                }
                level.scheduleTick(pos, this, RESET_DELAY_TICKS);

                // Speed II (+40% movement speed) for 60 ticks
                living.addEffect(new MobEffectInstance(
                        MobEffects.SPEED,
                        SPEED_DURATION_TICKS,
                        1,
                        false,
                        false,
                        true
                ));
            } else {
                Vec3 motion = living.getDeltaMovement();
                if (motion.x * motion.x + motion.z * motion.z > 0.001D && level.random.nextFloat() < 0.4F) {
                    double px = living.getX() + (level.random.nextDouble() - 0.5D) * 0.4D;
                    double py = pos.getY() + 1.02D;
                    double pz = living.getZ() + (level.random.nextDouble() - 0.5D) * 0.4D;
                    int color = level.random.nextBoolean() ? 0x22D3EE : 0x4ADE80;
                    level.addParticle(new DustParticleOptions(color, 0.75F), px, py, pz, 0.0D, 0.015D, 0.0D);
                }
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        AABB topBox = new AABB(
                pos.getX(), pos.getY() + 0.95D, pos.getZ(),
                pos.getX() + 1.0D, pos.getY() + 1.3D, pos.getZ() + 1.0D
        );
        List<LivingEntity> standing = level.getEntitiesOfClass(LivingEntity.class, topBox, Entity::isAlive);
        if (!standing.isEmpty()) {
            level.scheduleTick(pos, this, RESET_DELAY_TICKS);
        } else {
            level.setBlock(pos, state.setValue(LIT, false), 3);
        }
    }
}
