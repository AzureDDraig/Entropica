package ddraig.net.entropica.block.biome14;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Amber Resin Block — translucent golden resin block condensed from Oasis Floes amber sap.
 * Cushions fall damage by half and lets entities slide smoothly down its walls without restricting jump height.
 */
public class AmberResinBlock extends HalfTransparentBlock {
    public static final MapCodec<AmberResinBlock> CODEC = simpleCodec(AmberResinBlock::new);
    protected static final VoxelShape COLLISION_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    public AmberResinBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HalfTransparentBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.playSound(SoundEvents.HONEY_BLOCK_FALL, 1.0F, 1.0F);
        super.fallOn(level, state, pos, entity, fallDistance * 0.5D);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (!entity.onGround() && entity.getY() <= pos.getY() + 0.9375D) {
            Vec3 motion = entity.getDeltaMovement();
            if (motion.y < -0.13D) {
                double factor = -0.05D / motion.y;
                entity.setDeltaMovement(new Vec3(motion.x * factor, -0.13D, motion.z * factor));
                entity.resetFallDistance();
                if (level.getRandom().nextInt(5) == 0) {
                    entity.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 0.8F, 1.0F);
                }
            } else if (motion.y < -0.08D) {
                entity.resetFallDistance();
            }
        }
    }
}
