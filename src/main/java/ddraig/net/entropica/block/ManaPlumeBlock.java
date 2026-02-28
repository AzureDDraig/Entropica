package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.ManaPlumeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ManaPlumeBlock extends BaseEntityBlock {
    public static final MapCodec<ManaPlumeBlock> CODEC = simpleCodec(ManaPlumeBlock::new);

    public ManaPlumeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaPlumeBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof ManaPlumeBlockEntity plume && plume.isCoreActive()) {
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