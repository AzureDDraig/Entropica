package ddraig.net.entropica.block.biome10;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Sun-Glazed Lantern — sculpted warm golden winter sanctuary lantern.
 * Radiates full Light Level 15 with soft golden-amber Materia motes.
 */
public class SunGlazedLanternBlock extends Block {
    public static final MapCodec<SunGlazedLanternBlock> CODEC = simpleCodec(SunGlazedLanternBlock::new);

    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(4, 0, 4, 12, 2, 12),
            Block.box(3, 2, 3, 13, 11, 13),
            Block.box(2, 11, 2, 14, 13, 14),
            Block.box(5, 13, 5, 11, 15, 11),
            Block.box(7, 15, 7, 9, 16, 9)
    );

    public SunGlazedLanternBlock(Properties properties) {
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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(5) == 0) {
            double x = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
            double y = pos.getY() + 0.35D + random.nextDouble() * 0.45D;
            double z = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
            if (ModParticles.SPECTRUM_SPARKLE.isBound() && random.nextBoolean()) {
                level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 1.0F, 0.85F, 0.30F);
            } else {
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
            }
        }
    }
}
