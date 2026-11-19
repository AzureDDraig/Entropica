package ddraig.net.entropica.worldgen.biome;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * External weak-keyed surface rule registry for {@link NoiseGeneratorSettings}.
 *
 * <p>Because {@code NoiseGeneratorSettings} is a Java {@code Record}, non-static fields
 * cannot be added to it via Mixin without violating the JVM's Record attribute verifier.
 * Storing custom rules here keeps {@code NoiseGeneratorSettingsMixin} 100% field-free
 * while caching the combined {@link SurfaceRules.RuleSource} with zero per-chunk allocations.</p>
 */
public final class EntropicaSurfaceRuleStore {
    private static final class Entry {
        final SurfaceRules.RuleSource customRule;
        volatile SurfaceRules.RuleSource lastBaseRule;
        volatile SurfaceRules.RuleSource cachedSequence;

        Entry(SurfaceRules.RuleSource customRule) {
            this.customRule = customRule;
        }
    }

    private static final Map<NoiseGeneratorSettings, Entry> RULES =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static volatile boolean hasAnyActiveRules = false;

    private EntropicaSurfaceRuleStore() {}

    public static void setCustomSurfaceRule(NoiseGeneratorSettings settings, SurfaceRules.RuleSource rule) {
        if (settings == null) {
            return;
        }
        if (rule == null) {
            RULES.remove(settings);
            hasAnyActiveRules = !RULES.isEmpty();
        } else {
            RULES.put(settings, new Entry(rule));
            hasAnyActiveRules = true;
        }
    }

    public static SurfaceRules.RuleSource resolve(NoiseGeneratorSettings settings, SurfaceRules.RuleSource baseRule) {
        if (!hasAnyActiveRules || settings == null) {
            return null;
        }
        Entry entry = RULES.get(settings);
        if (entry == null || entry.customRule == null) {
            return null;
        }
        if (baseRule == null) {
            return entry.customRule;
        }
        if (entry.lastBaseRule == baseRule && entry.cachedSequence != null) {
            return entry.cachedSequence;
        }
        SurfaceRules.RuleSource combined = SurfaceRules.sequence(entry.customRule, baseRule);
        entry.lastBaseRule = baseRule;
        entry.cachedSequence = combined;
        return combined;
    }
}
