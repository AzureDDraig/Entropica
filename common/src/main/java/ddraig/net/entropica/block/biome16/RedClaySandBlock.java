package ddraig.net.entropica.block.biome16;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.ColoredFallingBlock;

/**
 * Red Clay-Sand — iron-rich sanguine tidal sediment settled along the shallow basins and shoals
 * of 2.16 Blood-Ice Shallows. Presses into Sanguine Clay Bricks and smelts into Blood Glass.
 */
public class RedClaySandBlock extends ColoredFallingBlock {
    public static final MapCodec<RedClaySandBlock> CODEC = simpleCodec(RedClaySandBlock::new);
    public static final ColorRGBA DUST_COLOR = new ColorRGBA(0x6E2028);

    public RedClaySandBlock(Properties properties) {
        super(DUST_COLOR, properties);
    }

    @Override
    public MapCodec<? extends ColoredFallingBlock> codec() {
        return CODEC;
    }
}
