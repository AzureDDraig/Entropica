package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AstralMirrorBlockEntity extends BlockEntity {
    private int clientTicks = 0;

    public AstralMirrorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ASTRAL_MIRROR_BE.get(), pos, blockState);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AstralMirrorBlockEntity entity) {
        entity.clientTicks++;
    }

    public int getClientTicks() {
        return clientTicks;
    }
}
