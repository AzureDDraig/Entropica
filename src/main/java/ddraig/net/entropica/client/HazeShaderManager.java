package ddraig.net.entropica.client;

import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Method;

public class HazeShaderManager {
    private static final ResourceLocation HAZE_SHADER = ResourceLocation.fromNamespaceAndPath("entropica", "haze");
    private static boolean hasPrintedDiagnostic = false;
    private static boolean wasActive = false;

    public static void clientTick(Minecraft mc) {
        // Disabled in favor of HazeOverlayRenderer to prevent shader load failures
        if (wasActive && mc.gameRenderer != null) {
            shutdownHazeShader(mc.gameRenderer);
            wasActive = false;
        }
    }

    private static void loadHazeShader(net.minecraft.client.renderer.GameRenderer renderer) {
        ddraig.net.entropica.Entropica.LOGGER.info("HAZE SHADER: loadHazeShader invoked with {}", HAZE_SHADER);
        try {
            Method loadMethod = null;
            try {
                loadMethod = net.minecraft.client.renderer.GameRenderer.class.getDeclaredMethod("setPostEffect", ResourceLocation.class);
            } catch (NoSuchMethodException e) {
                try {
                    loadMethod = net.minecraft.client.renderer.GameRenderer.class.getDeclaredMethod("loadEffect", ResourceLocation.class);
                } catch (NoSuchMethodException ex) {
                    for (Method m : net.minecraft.client.renderer.GameRenderer.class.getDeclaredMethods()) {
                        if ((m.getName().equals("setPostEffect") || m.getName().equals("loadEffect") || m.getName().equals("loadSecondaryEffect") || m.getName().equals("m_109121_")) 
                                && m.getParameterCount() == 1 
                                && m.getParameterTypes()[0] == ResourceLocation.class) {
                            loadMethod = m;
                            break;
                        }
                    }
                }
            }
            ddraig.net.entropica.Entropica.LOGGER.info("HAZE SHADER: loadMethod found = {}", loadMethod);
            if (loadMethod != null) {
                loadMethod.setAccessible(true);
                loadMethod.invoke(renderer, HAZE_SHADER);
                ddraig.net.entropica.Entropica.LOGGER.info("HAZE SHADER: loadMethod successfully invoked!");
            } else {
                ddraig.net.entropica.Entropica.LOGGER.error("HAZE SHADER: loadMethod is NULL! Could not load the shader.");
            }
        } catch (Exception e) {
            ddraig.net.entropica.Entropica.LOGGER.error("HAZE SHADER: Exception loading shader:", e);
        }
    }

    private static void shutdownHazeShader(net.minecraft.client.renderer.GameRenderer renderer) {
        ddraig.net.entropica.Entropica.LOGGER.info("HAZE SHADER: shutdownHazeShader invoked");
        try {
            Method shutdownMethod = null;
            try {
                shutdownMethod = net.minecraft.client.renderer.GameRenderer.class.getDeclaredMethod("clearPostEffect");
            } catch (NoSuchMethodException e) {
                try {
                    shutdownMethod = net.minecraft.client.renderer.GameRenderer.class.getDeclaredMethod("shutdownEffect");
                } catch (NoSuchMethodException ex) {
                    for (Method m : net.minecraft.client.renderer.GameRenderer.class.getDeclaredMethods()) {
                        if ((m.getName().equals("clearPostEffect") || m.getName().equals("shutdownEffect") || m.getName().equals("clearSecondaryEffect") || m.getName().equals("m_109140_")) 
                                && m.getParameterCount() == 0) {
                            shutdownMethod = m;
                            break;
                        }
                    }
                }
            }
            ddraig.net.entropica.Entropica.LOGGER.info("HAZE SHADER: shutdownMethod found = {}", shutdownMethod);
            if (shutdownMethod != null) {
                shutdownMethod.setAccessible(true);
                shutdownMethod.invoke(renderer);
                ddraig.net.entropica.Entropica.LOGGER.info("HAZE SHADER: shutdownMethod successfully invoked!");
            } else {
                ddraig.net.entropica.Entropica.LOGGER.error("HAZE SHADER: shutdownMethod is NULL!");
            }
        } catch (Exception e) {
            ddraig.net.entropica.Entropica.LOGGER.error("HAZE SHADER: Exception shutting down shader:", e);
        }
    }
}
