package ddraig.net.entropica.block.biome11;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.EntropicaFloraGroundHelper;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Zephyr Glass-Grass — wind-spun crystalline sedge of 2.11 Storm-Swept Icebergs.
 * Rooting into Zephyr Ice, solid snow blocks, or dirt (never thin snow layers or ordinary ice),
 * its springy glass blades gently cushion downward fall speed.
 */
public class ZephyrGlassGrassBlock extends BushBlock {
    public static final MapCodec<ZephyrGlassGrassBlock> CODEC = simpleCodec(ZephyrGlassGrassBlock::new);
    public static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D);

    public ZephyrGlassGrassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return (ModBlocks.ZEPHYR_ICE.isBound() && state.is(ModBlocks.ZEPHYR_ICE.get()))
                || EntropicaFloraGroundHelper.isSolidSnowBlock(state)
                || (!EntropicaFloraGroundHelper.isIceOrSnowLayer(state) && state.is(BlockTags.DIRT));
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier applier, boolean isInside) {
        super.entityInside(state, level, pos, entity, applier, isInside);
        if (entity instanceof LivingEntity living && living.getDeltaMovement().y < -0.22D) {
            Vec3 v = living.getDeltaMovement();
            living.setDeltaMovement(v.x, -0.22D, v.z);
            living.resetFallDistance();
        }
    }
}
