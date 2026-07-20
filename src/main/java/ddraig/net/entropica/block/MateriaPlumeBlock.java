package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.MateriaPlumeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MateriaPlumeBlock extends BaseEntityBlock {
    public static final MapCodec<MateriaPlumeBlock> CODEC = simpleCodec(MateriaPlumeBlock::new);

    // Combined VoxelShape generated perfectly from your Blockbench elements
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 3, 16),   // Base layer
            Block.box(2, 3, 2, 14, 5, 14),   // Second layer
            Block.box(4, 5, 4, 12, 7, 12),   // Third layer
            Block.box(6, 7, 6, 10, 10, 10),  // Stem
            Block.box(5, 10, 5, 11, 16, 11)  // Top crystal/plume
    );

    public MateriaPlumeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // Applies the custom hitbox
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MateriaPlumeBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof MateriaPlumeBlockEntity plume && plume.isCoreActive()) {
            int color = plume.getParticleColor();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;

            for (int i = 0; i < 3; i++) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
                double y = pos.getY() + 0.8;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;

                level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, r, g, b),
                        x, y, z,
                        (random.nextDouble() - 0.5) * 0.05,
                        random.nextDouble() * 0.1,
                        (random.nextDouble() - 0.5) * 0.05);
            }
        }
    }
}