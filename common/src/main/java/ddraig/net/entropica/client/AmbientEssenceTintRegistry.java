package ddraig.net.entropica.client;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.AestheticGlassRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

public class AmbientEssenceTintRegistry {

    public static int getAmbientEssenceColor(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
        long time = net.minecraft.Util.getMillis();

        if (level != null && pos != null) {
            if (level instanceof Level world) {
                Holder<Biome> biomeHolder = world.getBiome(pos);
                if (biomeHolder.isBound()) {
                    ResourceLocation biomeKey = biomeHolder.unwrapKey().map(net.minecraft.resources.ResourceKey::location).orElse(null);
                    if (biomeKey != null) {
                        String path = biomeKey.getPath();
                        if (path.contains("nether") || path.contains("basalt") || path.contains("crimson") || path.contains("warped")) {
                            int[] rgb = EssenceType.NETHER.getCurrentRGB(time / 50.0);
                            return (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                        }
                        if (path.contains("end_") || path.contains("the_end")) {
                            int[] rgb = EssenceType.VOID.getCurrentRGB(time / 50.0);
                            return (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                        }
                        if (path.contains("desert") || path.contains("badlands") || path.contains("savanna")) {
                            int[] rgb = EssenceType.ARID.getCurrentRGB(time / 50.0);
                            return (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                        }
                        if (path.contains("snow") || path.contains("frozen") || path.contains("ice")) {
                            int[] rgb = EssenceType.FROZEN.getCurrentRGB(time / 50.0);
                            return (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                        }
                        if (path.contains("ocean") || path.contains("river") || path.contains("beach")) {
                            int[] rgb = EssenceType.WATER.getCurrentRGB(time / 50.0);
                            return (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                        }
                        if (path.contains("swamp") || path.contains("mangrove")) {
                            int[] rgb = EssenceType.UNDEAD.getCurrentRGB(time / 50.0);
                            return (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                        }
                        if (path.contains("slopes") || path.contains("peaks") || path.contains("windswept") || path.contains("jagged")) {
                            int[] rgb = EssenceType.AIR.getCurrentRGB(time / 50.0);
                            return (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                        }
                    }
                }
            }
        }

        // Dynamic ambient shimmer cycle (rainbow pastel shimmer wave)
        float hue = (float) ((time % 6000L) / 6000.0);
        return AestheticGlassRegistry.hsbToRgb(hue, 0.65f, 1.0f);
    }
}
