package ddraig.net.entropica.worldgen.biome;

import ddraig.net.entropica.Entropica;
import dev.architectury.platform.Platform;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Zero-dependency compatibility bridge for TerraBlender.
 *
 * <ul>
 *   <li>Auto-detects whether {@code terrablender} is installed at runtime.</li>
 *   <li>Registers Entropica's {@code SurfaceRules.RuleSource}s directly with TerraBlender's
 *       {@code SurfaceRuleManager} via reflection when present so TerraBlender's
 *       {@code NamespacedSurfaceRuleSource} handles {@code entropica:*} biomes natively,
 *       while tracking which dimensions were accepted so our native {@code NoiseGeneratorSettings}
 *       hook never double-wraps them.</li>
 *   <li>Detects whether an external TerraBlender region or datapack has already injected
 *       {@code entropica:*} biomes into a base {@code MultiNoiseBiomeSource}.</li>
 * </ul>
 */
public final class TerraBlenderCompatBridge {
    public static final String TERRABLENDER_MOD_ID = "terrablender";

    private static final Set<ResourceKey<LevelStem>> SURFACE_RULES_DELEGATED_TO_TERRABLENDER =
            Collections.synchronizedSet(new HashSet<>());

    private TerraBlenderCompatBridge() {}

    public static boolean isTerraBlenderLoaded() {
        try {
            return Platform.isModLoaded(TERRABLENDER_MOD_ID);
        } catch (Throwable ignored) {
            // Safe fallback when running standalone unit verification outside Architectury bootstrap
            return false;
        }
    }

    /**
     * Checks whether the base BiomeSource (prior to Entropica's layer) already contains any
     * {@code entropica:*} biome (for example via a datapack or external TerraBlender Region addon).
     */
    public static boolean containsEntropicaBiomes(Collection<Holder<Biome>> existingBiomes) {
        if (existingBiomes == null || existingBiomes.isEmpty()) {
            return false;
        }
        for (Holder<Biome> holder : existingBiomes) {
            if (holder == null) continue;
            Optional<ResourceKey<Biome>> key = holder.unwrapKey();
            if (key.isPresent() && Entropica.MODID.equals(key.get().location().getNamespace())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSurfaceRuleDelegatedToTerraBlender(ResourceKey<LevelStem> stemKey) {
        return SURFACE_RULES_DELEGATED_TO_TERRABLENDER.contains(stemKey);
    }

    /**
     * Attempts to register Entropica's Overworld & Nether surface rules with TerraBlender's
     * {@code SurfaceRuleManager} during mod initialization when TerraBlender is present.
     */
    public static void tryRegisterSurfaceRulesWithTerraBlender() {
        if (!isTerraBlenderLoaded()) {
            return;
        }

        SurfaceRules.RuleSource overworldRule = EntropicaBiomePlacements.buildCombinedSurfaceRule(LevelStem.OVERWORLD, false);
        SurfaceRules.RuleSource netherRule = EntropicaBiomePlacements.buildCombinedSurfaceRule(LevelStem.NETHER, false);

        if (overworldRule == null && netherRule == null) {
            return;
        }

        try {
            Class<?> managerClass = Class.forName("terrablender.api.SurfaceRuleManager");
            Class<?> categoryClass = Class.forName("terrablender.api.SurfaceRuleManager$RuleCategory");
            Method addSurfaceRules = managerClass.getMethod(
                    "addSurfaceRules", categoryClass, String.class, SurfaceRules.RuleSource.class
            );

            if (overworldRule != null) {
                Object overworldCat = Enum.valueOf(categoryClass.asSubclass(Enum.class), "OVERWORLD");
                addSurfaceRules.invoke(null, overworldCat, Entropica.MODID, overworldRule);
                SURFACE_RULES_DELEGATED_TO_TERRABLENDER.add(LevelStem.OVERWORLD);
            }
            if (netherRule != null) {
                Object netherCat = Enum.valueOf(categoryClass.asSubclass(Enum.class), "NETHER");
                addSurfaceRules.invoke(null, netherCat, Entropica.MODID, netherRule);
                SURFACE_RULES_DELEGATED_TO_TERRABLENDER.add(LevelStem.NETHER);
            }

            Entropica.LOGGER.info(
                    "[Entropica Worldgen] TerraBlender detected — bridged Entropica surface rules to TerraBlender SurfaceRuleManager (mode: {}).",
                    TerraBlenderCompatMode.current()
            );
        } catch (Throwable t) {
            SURFACE_RULES_DELEGATED_TO_TERRABLENDER.clear();
            Entropica.LOGGER.warn(
                    "[Entropica Worldgen] TerraBlender detected, but SurfaceRuleManager reflection did not match ({}). Falling back cleanly to native NoiseGeneratorSettings surface rule injection.",
                    t.getMessage()
            );
        }
    }
}
