package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.block.CreativeGravityCenterBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeGravityCenterBlockEntity extends GravityCenterBlockEntity {

    public CreativeGravityCenterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_GRAVITY_CENTER_BE.get(), pos, state);
        if (state.hasProperty(CreativeGravityCenterBlock.RADIUS_LEVEL)) {
            this.radius = CreativeGravityCenterBlock.getRadiusForLevel(state.getValue(CreativeGravityCenterBlock.RADIUS_LEVEL));
        }
    }

    @Override
    public boolean isCreative() {
        return true;
    }
}
