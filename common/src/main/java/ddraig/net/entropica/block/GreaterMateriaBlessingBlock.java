package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Greater Materia Blessing — 3×3×4 multiblock (36 blocks).
 * A larger, more powerful natural crystalline formation with enhanced Materia effects.
 * Drops 8-12 Materia Blessing Shards when broken.
 */
public class GreaterMateriaBlessingBlock extends AbstractMateriaBlessingBlock {

    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 35);
    public static final MapCodec<GreaterMateriaBlessingBlock> CODEC = simpleCodec(GreaterMateriaBlessingBlock::new);

    public GreaterMateriaBlessingBlock(Properties properties) {
        super(properties.lightLevel(state -> 12));
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
        return 3;
    }

    @Override
    public int getHeight() {
        return 4;
    }

    @Override
    public int getDepth() {
        return 3;
    }

    @Override
    public int getMinShardDrop() {
        return 6;
    }

    @Override
    public int getShardDropRange() {
        return 2; // 6-8 shards
    }
}
