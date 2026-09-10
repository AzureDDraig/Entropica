package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.AstralCrystalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * AstralCrystalClusterBlock — AmethystClusterBlock variant that delegates in-world rendering
 * to the procedural AstralCrystalRenderer via RenderShape.INVISIBLE.
 */
public class AstralCrystalClusterBlock extends AmethystClusterBlock implements EntityBlock {

    private final int stage;

    public AstralCrystalClusterBlock(int stage, int height, int xzOffset, Properties properties) {
        super(height, xzOffset, properties);
        this.stage = stage;
    }

    public int getStage() {
        return this.stage;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AstralCrystalBlockEntity(pos, state, this.stage);
    }
}
