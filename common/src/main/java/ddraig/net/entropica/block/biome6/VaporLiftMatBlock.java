package ddraig.net.entropica.block.biome6;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vapor-Lift Mat — low-profile 4px tall pneumatic steam cushion pad.
 * Negates fall damage and launches stepping or landing entities 5–6 blocks upward
 * on a warm cushion of pressurized steam.
 */
public class VaporLiftMatBlock extends Block {
    public static final MapCodec<VaporLiftMatBlock> CODEC = simpleCodec(VaporLiftMatBlock::new);

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
    private static final Map<UUID, Long> LAST_LIFT_TICK = new ConcurrentHashMap<>();

    public VaporLiftMatBlock(Properties properties) {
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
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.resetFallDistance();
        launchOnSteamCushion(level, pos, entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        launchOnSteamCushion(level, pos, entity);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
        launchOnSteamCushion(level, pos, entity);
    }

    public static void launchOnSteamCushion(Level level, BlockPos pos, Entity entity) {
        if (entity == null || entity.isSpectator()) {
            return;
        }
        entity.resetFallDistance();
        if (entity.getTicksFrozen() > 0) {
            entity.setTicksFrozen(0);
        }

        long now = level.getGameTime();
        Long last = LAST_LIFT_TICK.get(entity.getUUID());
        if (!level.isClientSide() && last != null && now - last >= 0L && now - last < 6L) {
            return;
        }

        Vec3 vel = entity.getDeltaMovement();
        if (vel.y >= 0.85D) {
            return;
        }

        entity.setDeltaMovement(vel.x, 0.98D, vel.z);
        entity.hurtMarked = true;

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            LAST_LIFT_TICK.put(entity.getUUID(), now);
            entity.addTag("entropica:bouncepad_immune");
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D,
                    12, 0.3D, 0.1D, 0.3D, 0.05D);
            serverLevel.playSound(null, pos, SoundEvents.BREEZE_JUMP, SoundSource.BLOCKS, 0.8F, 1.15F);
            serverLevel.playSound(null, pos, SoundEvents.WOOL_FALL, SoundSource.BLOCKS, 0.75F, 1.0F);
        }
    }
}
