package ddraig.net.entropica.block;

import ddraig.net.entropica.Entropica;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * Shared ground-substrate helper ensuring terrestrial Entropica plants grow on soil, grass,
 * gravel, stone, or solid snow blocks (replacing thin snow layers) rather than on bare ice
 * or stacked atop snow layers.
 */
public final class EntropicaFloraGroundHelper {

    private static final Set<String> ENTROPICA_ICE_PATHS = Set.of(
            "runic_blue_ice",
            "glimmer_ice",
            "rotten_ice",
            "blighted_packed_ice",
            "steaming_ice",
            "fulgurite_ice",
            "radiant_glaze_ice",
            "zephyr_ice",
            "spore_glazed_ice",
            "mirage_ice",
            "dust_crusted_ice",
            "sanguine_ice"
    );

    private static final Set<String> ENTROPICA_MINERAL_SAND_OR_CLAY_PATHS = Set.of(
            "glass_sand",
            "grey_sand",
            "red_clay_sand"
    );

    private EntropicaFloraGroundHelper() {
    }

    public static boolean isAnyIce(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.FROSTED_ICE)) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (key != null
                && Entropica.MODID.equals(key.getNamespace())
                && ENTROPICA_ICE_PATHS.contains(key.getPath())) {
            return true;
        }
        try {
            return state.is(BlockTags.ICE);
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    public static boolean isIceOrSnowLayer(BlockState state) {
        if (state == null) {
            return false;
        }
        return state.is(Blocks.SNOW) || isAnyIce(state);
    }

    public static boolean isSolidSnowBlock(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return key != null
                && Entropica.MODID.equals(key.getNamespace())
                && "radiant_snow_block".equals(key.getPath());
    }

    public static boolean isEntropicaMineralSandOrClay(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return key != null
                && Entropica.MODID.equals(key.getNamespace())
                && ENTROPICA_MINERAL_SAND_OR_CLAY_PATHS.contains(key.getPath());
    }
}
