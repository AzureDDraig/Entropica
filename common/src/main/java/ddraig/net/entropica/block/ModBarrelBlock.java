package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.ModBarrelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ModBarrelBlock extends BarrelBlock {
    public ModBarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModBarrelBlockEntity(pos, state);
    }
}
