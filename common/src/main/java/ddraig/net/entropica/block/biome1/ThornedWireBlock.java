package ddraig.net.entropica.block.biome1;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Craftable defensive wire woven from Shielding Thorn Fiber and iron nuggets.
 * Applies brief Slowness II and deals 1.5F damage to entities moving through it.
 */
public class ThornedWireBlock extends Block {
    public static final MapCodec<ThornedWireBlock> CODEC = simpleCodec(ThornedWireBlock::new);
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public ThornedWireBlock(Properties properties) {
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
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        entity.makeStuckInBlock(state, new Vec3(0.55D, 0.50D, 0.55D));

        if (level instanceof ServerLevel serverLevel && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, true, true));

            Vec3 movement = entity.isClientAuthoritative()
                    ? entity.getKnownMovement()
                    : entity.oldPosition().subtract(entity.position());
            if (Math.abs(movement.x()) >= 0.003D || Math.abs(movement.y()) >= 0.003D || Math.abs(movement.z()) >= 0.003D) {
                living.hurtServer(serverLevel, serverLevel.damageSources().cactus(), 1.5F);
            }
        }
    }
}
