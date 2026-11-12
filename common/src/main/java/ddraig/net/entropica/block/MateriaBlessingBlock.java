package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Lesser Materia Blessing — 2×2×3 multiblock (12 blocks).
 * A naturally occurring crystalline formation that resonates with ambient Materia currents.
 * Drops 4-6 Materia Blessing Shards when broken.
 */
public class MateriaBlessingBlock extends AbstractMateriaBlessingBlock {

    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 11);
    public static final MapCodec<MateriaBlessingBlock> CODEC = simpleCodec(MateriaBlessingBlock::new);

    public MateriaBlessingBlock(Properties properties) {
        super(properties.lightLevel(state -> 8));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public IntegerProperty getPartProperty() {
        return PART;
    }

    @Override
    public int getWidth() {
        return 2;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public int getDepth() {
        return 2;
    }

    @Override
    public int getMinShardDrop() {
        return 3;
    }

    @Override
    public int getShardDropRange() {
        return 2; // 3-5 shards
    }
}
