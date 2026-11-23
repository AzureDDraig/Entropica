package ddraig.net.entropica.block.biome2;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Mist-Veil Fern — silver-fronded geothermal fern with a warm peach heart,
 * thriving amid the steam-shrouded gravel islets of the Auroral Skerries.
 */
public class MistVeilFernBlock extends VegetationBlock {
    public static final MapCodec<MistVeilFernBlock> CODEC = simpleCodec(MistVeilFernBlock::new);
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public MistVeilFernBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        if (ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(state)) {
            return false;
        }
        return state.is(BlockTags.DIRT)
                || ddraig.net.entropica.block.EntropicaFloraGroundHelper.isSolidSnowBlock(state)
                || state.is(Blocks.GRAVEL)
                || (ModBlocks.COARSE_GRAVEL.isBound() && state.is(ModBlocks.COARSE_GRAVEL.get()))
                || (ModBlocks.STEAMING_SPRING_STONE.isBound() && state.is(ModBlocks.STEAMING_SPRING_STONE.get()))
                || (ModBlocks.VITAE_SPRING_STONE.isBound() && state.is(ModBlocks.VITAE_SPRING_STONE.get()))
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.STONE)
                || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT)
                || state.is(Blocks.POLISHED_BASALT);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(7) == 0) {
            double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
            double y = pos.getY() + 0.3D + random.nextDouble() * 0.45D;
            double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
            } else {
                level.addParticle(ParticleTypes.CLOUD, x, y, z,
                        (random.nextDouble() - 0.5D) * 0.01D, 0.015D, (random.nextDouble() - 0.5D) * 0.01D);
            }
        }
    }
}
