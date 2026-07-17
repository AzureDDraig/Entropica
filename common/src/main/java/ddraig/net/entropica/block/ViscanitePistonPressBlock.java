package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.ViscanitePistonPressBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ViscanitePistonPressBlock extends BaseEntityBlock {
    public static final com.mojang.serialization.MapCodec<ViscanitePistonPressBlock> CODEC = simpleCodec(ViscanitePistonPressBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public ViscanitePistonPressBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ViscanitePistonPressBlockEntity(pos, state);
    }
}
