package ddraig.net.entropica.client.worldgen;

import ddraig.net.entropica.config.EntropicaConfig;
import dev.architectury.platform.Platform;

import java.lang.reflect.Method;

/**
 * Zero-dependency client compatibility bridge for Sodium-family chunk renderers
 * (Sodium, Embeddium, Rubidium) and Iris-family shader pipelines (Iris, Oculus).
 *
 * <p>Uses pure reflection and uninitialized class probing so that {@code common}
 * has zero compile-time mod dependencies and executes safely inside headless JVM
 * verification suites.</p>
 */
public final class ShaderModCompatBridge {
    private static final long SHADERPACK_CACHE_WINDOW_NANOS = 250_000_000L; // ~250 ms

    private static final String[] SODIUM_FAMILY_CLASSES = {
            "net.caffeinemc.mods.sodium.client.SodiumClientMod",
            "me.jellysquid.mods.sodium.client.SodiumClientMod",
            "org.embeddedt.embeddium.impl.Embeddium"
    };
    private static final String[] SODIUM_FAMILY_MOD_IDS = {
            "sodium",
            "embeddium",
            "rubidium"
    };

    private static final String[] IRIS_API_CLASSES = {
            "net.irisshaders.iris.api.v0.IrisApi",
            "net.coderbot.iris.api.v0.IrisApi"
    };
    private static final String[] SHADER_MOD_IDS = {
            "iris",
            "oculus"
    };

    private static volatile Boolean cachedSodiumFamilyLoaded;
    private static volatile Boolean cachedShaderModLoaded;

    private static volatile boolean irisReflectionResolved;
    private static volatile Method irisGetInstanceMethod;
    private static volatile Method irisIsShaderPackInUseMethod;

    private static volatile long lastShaderPackCheckNanos = Long.MIN_VALUE;
    private static volatile boolean cachedShaderPackActive;

    private ShaderModCompatBridge() {}

    /**
     * Detects whether Sodium, Embeddium, or Rubidium is present on the runtime classpath.
     */
    public static boolean isSodiumFamilyLoaded() {
        Boolean cached = cachedSodiumFamilyLoaded;
        if (cached != null) {
            return cached;
        }
        boolean detected = hasAnyClass(SODIUM_FAMILY_CLASSES) || hasAnyModId(SODIUM_FAMILY_MOD_IDS);
        cachedSodiumFamilyLoaded = detected;
        return detected;
    }

    /**
     * Detects whether Iris or Oculus is present on the runtime classpath.
     */
    public static boolean isShaderModLoaded() {
        Boolean cached = cachedShaderModLoaded;
        if (cached != null) {
            return cached;
        }
        boolean detected = hasAnyClass(IRIS_API_CLASSES) || hasAnyModId(SHADER_MOD_IDS);
        cachedShaderModLoaded = detected;
        return detected;
    }

    /**
     * Queries {@code IrisApi.getInstance().isShaderPackInUse()} via cached reflection
     * (re-evaluated at most once every ~250ms). Safely returns {@code false} when no
     * shader mod is installed, in headless tests, or if any {@link Throwable} occurs.
     */
    public static boolean isShaderPackActive() {
        if (!isShaderModLoaded()) {
            return false;
        }
        long now = System.nanoTime();
        long last = lastShaderPackCheckNanos;
        if (last != Long.MIN_VALUE && (now - last) < SHADERPACK_CACHE_WINDOW_NANOS) {
            return cachedShaderPackActive;
        }

        boolean active = queryIrisShaderPackInUse();
        cachedShaderPackActive = active;
        lastShaderPackCheckNanos = now;
        return active;
    }

    /**
     * Returns {@code 0.55F} when shaderpack adaptive attenuation is enabled and an active
     * Iris/Oculus shaderpack is in use (preventing over-dense bloom/fog stacking), else {@code 1.0F}.
     */
    public static float getVolumetricMistShaderScale() {
        return (isAdaptiveAttenuationEnabled() && isShaderPackActive()) ? 0.55F : 1.0F;
    }

    /**
     * Returns {@code 0.22F} when shaderpack adaptive attenuation is enabled and an active
     * Iris/Oculus shaderpack is in use (preventing double-brightening over shader SSR water reflections),
     * else {@code 1.0F}.
     */
    public static float getWaterReflectionShaderScale() {
        return (isAdaptiveAttenuationEnabled() && isShaderPackActive()) ? 0.22F : 1.0F;
    }

    /**
     * Returns {@code 0.72F} when shaderpack adaptive attenuation is enabled and an active
     * Iris/Oculus shaderpack is in use (HDR-bloom-safe sub-surface starlight parallax),
     * else {@code 1.0F}.
     */
    public static float getWaterParallaxShaderScale() {
        return (isAdaptiveAttenuationEnabled() && isShaderPackActive()) ? 0.72F : 1.0F;
    }

    private static boolean isAdaptiveAttenuationEnabled() {
        try {
            Boolean enabled = EntropicaConfig.ENABLE_SHADERPACK_ADAPTIVE_ATTENUATION.get();
            return enabled == null || enabled;
        } catch (Throwable ignored) {
            // Safe fallback in standalone :common headless unit tests where @ExpectPlatform is untransformed
            return true;
        }
    }

    private static boolean queryIrisShaderPackInUse() {
        try {
            if (!irisReflectionResolved) {
                synchronized (ShaderModCompatBridge.class) {
                    if (!irisReflectionResolved) {
                        ClassLoader loader = ShaderModCompatBridge.class.getClassLoader();
                        for (String apiClassName : IRIS_API_CLASSES) {
                            try {
                                Class<?> apiClass = Class.forName(apiClassName, false, loader);
                                Method getInstance = apiClass.getMethod("getInstance");
                                Method isInUse = apiClass.getMethod("isShaderPackInUse");
                                irisGetInstanceMethod = getInstance;
                                irisIsShaderPackInUseMethod = isInUse;
                                break;
                            } catch (Throwable ignored) {
                                // Try fallback package name
                            }
                        }
                        irisReflectionResolved = true;
                    }
                }
            }
            Method getInstance = irisGetInstanceMethod;
            Method isInUse = irisIsShaderPackInUseMethod;
            if (getInstance == null || isInUse == null) {
                return false;
            }
            Object instance = getInstance.invoke(null);
            if (instance == null) {
                return false;
            }
            Object result = isInUse.invoke(instance);
            return result instanceof Boolean b && b;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean hasAnyClass(String[] classNames) {
        ClassLoader loader = ShaderModCompatBridge.class.getClassLoader();
        for (String name : classNames) {
            try {
                Class.forName(name, false, loader);
                return true;
            } catch (Throwable ignored) {
                // Not present
            }
        }
        return false;
    }

    private static boolean hasAnyModId(String[] modIds) {
        for (String modId : modIds) {
            try {
                if (Platform.isModLoaded(modId)) {
                    return true;
                }
            } catch (Throwable ignored) {
                // Safe fallback when Architectury Platform is not bootstrapped (headless JVM tests)
                return false;
            }
        }
        return false;
    }
}
