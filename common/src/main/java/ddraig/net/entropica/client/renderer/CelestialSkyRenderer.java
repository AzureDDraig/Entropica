package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.astral.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CelestialSkyRenderer {

    public static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    public static final ResourceLocation STAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/star.png");
    public static final ResourceLocation COMET_HEAD_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/comet_head.png");
    public static final ResourceLocation METEOR_TRAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/meteor_trail.png");
    public static final ResourceLocation NEBULA_PUFF_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/nebula_puff.png");
    public static final ResourceLocation SUPERNOVA_RING_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/supernova_ring.png");

    public record NebulaPuff(float dAzim, float dAlt, float size, float r, float g, float b, float a, float rotSpeed, float phase) {
        public NebulaPuff(float dAzim, float dAlt, float size, float r, float g, float b, float a) {
            this(dAzim, dAlt, size, r, g, b, a, 1.0f, (dAzim * 3.1f + dAlt * 7.3f));
        }
    }

    public static class NebulaComplex {
        public final float baseAzim;
        public final float baseAlt;
        public final String name;
        public final List<NebulaPuff> puffs;

        public NebulaComplex(float baseAzim, float baseAlt, String name, List<NebulaPuff> puffs) {
            this.baseAzim = baseAzim;
            this.baseAlt = baseAlt;
            this.name = name;
            this.puffs = puffs;
        }
    }

    public static final List<NebulaComplex> NEBULA_COMPLEXES = new ArrayList<>();
    static {
        // 1. The Great Galactic Spine / Milky Way Ribbon (Sweeping 360° celestial arc across both sides of the sky)
        List<NebulaPuff> spine = new ArrayList<>();
        // Luminous Galactic Core & Ribbons
        spine.add(new NebulaPuff(0.0f, 0.0f, 62.0f, 0.45f, 0.35f, 0.85f, 0.28f, 0.4f, 0.0f));
        spine.add(new NebulaPuff(-18.0f, -8.0f, 54.0f, 0.15f, 0.65f, 0.95f, 0.25f, -0.3f, 1.2f));
        spine.add(new NebulaPuff(22.0f, 10.0f, 56.0f, 0.75f, 0.30f, 0.80f, 0.26f, 0.5f, 2.5f));
        spine.add(new NebulaPuff(-38.0f, -14.0f, 50.0f, 0.20f, 0.75f, 0.85f, 0.22f, -0.4f, 3.8f));
        spine.add(new NebulaPuff(42.0f, 18.0f, 52.0f, 0.60f, 0.25f, 0.90f, 0.24f, 0.3f, 4.1f));
        spine.add(new NebulaPuff(-58.0f, -18.0f, 48.0f, 0.35f, 0.45f, 0.95f, 0.20f, -0.5f, 5.0f));
        spine.add(new NebulaPuff(64.0f, 22.0f, 50.0f, 0.85f, 0.45f, 0.65f, 0.22f, 0.4f, 0.8f));
        spine.add(new NebulaPuff(-80.0f, -22.0f, 46.0f, 0.10f, 0.80f, 0.90f, 0.18f, -0.3f, 2.1f));
        spine.add(new NebulaPuff(86.0f, 24.0f, 48.0f, 0.50f, 0.30f, 0.85f, 0.20f, 0.5f, 3.4f));
        spine.add(new NebulaPuff(110.0f, 20.0f, 48.0f, 0.30f, 0.50f, 0.90f, 0.22f, -0.4f, 1.7f));
        spine.add(new NebulaPuff(-105.0f, -20.0f, 46.0f, 0.65f, 0.35f, 0.80f, 0.20f, 0.3f, 2.9f));
        // Embedded Interstellar Dark Dust Clouds (Coal-Sack Rifts) in Galactic Spine
        spine.add(new NebulaPuff(-10.0f, -3.0f, 34.0f, 0.03f, 0.02f, 0.06f, 0.42f, 0.2f, 0.5f));
        spine.add(new NebulaPuff(14.0f, 6.0f, 32.0f, 0.04f, 0.02f, 0.07f, 0.40f, -0.2f, 1.8f));
        spine.add(new NebulaPuff(-46.0f, -12.0f, 30.0f, 0.03f, 0.02f, 0.05f, 0.38f, 0.3f, 3.1f));
        spine.add(new NebulaPuff(52.0f, 16.0f, 28.0f, 0.04f, 0.03f, 0.08f, 0.36f, -0.3f, 4.4f));
        NEBULA_COMPLEXES.add(new NebulaComplex(90.0f, 45.0f, "Galactic Spine", spine));

        // 2. The Great Azure Lagoon Veil (Side A - Cyan, Teal, Sapphire Reflection Nebulae)
        List<NebulaPuff> lagoon = new ArrayList<>();
        lagoon.add(new NebulaPuff(0.0f, 0.0f, 52.0f, 0.0f, 0.70f, 1.0f, 0.34f, 0.5f, 0.0f));
        lagoon.add(new NebulaPuff(-10.0f, 5.0f, 38.0f, 0.1f, 0.90f, 0.95f, 0.30f, -0.4f, 1.1f));
        lagoon.add(new NebulaPuff(11.0f, -6.0f, 42.0f, 0.0f, 0.50f, 0.98f, 0.28f, 0.6f, 2.2f));
        lagoon.add(new NebulaPuff(-6.0f, -7.0f, 32.0f, 0.2f, 0.98f, 1.0f, 0.25f, -0.5f, 3.3f));
        lagoon.add(new NebulaPuff(7.0f, 8.0f, 34.0f, 0.05f, 0.60f, 0.90f, 0.26f, 0.4f, 4.4f));
        lagoon.add(new NebulaPuff(-14.0f, -3.0f, 26.0f, 0.15f, 0.80f, 1.0f, 0.20f, -0.3f, 5.5f));
        lagoon.add(new NebulaPuff(15.0f, 4.0f, 28.0f, 0.0f, 0.45f, 0.85f, 0.22f, 0.5f, 0.7f));
        lagoon.add(new NebulaPuff(0.0f, -12.0f, 30.0f, 0.05f, 0.75f, 0.80f, 0.18f, -0.4f, 1.9f));
        NEBULA_COMPLEXES.add(new NebulaComplex(45.0f, 25.0f, "Lagoon Veil", lagoon));

        // 3. The Andromeda Spiral Halo (Side A - Soft Silver-Cyan & Violet Oval Glow)
        List<NebulaPuff> andromeda = new ArrayList<>();
        andromeda.add(new NebulaPuff(0.0f, 0.0f, 58.0f, 0.75f, 0.85f, 1.0f, 0.32f, 0.3f, 0.0f));
        andromeda.add(new NebulaPuff(-14.0f, 6.0f, 44.0f, 0.45f, 0.65f, 0.95f, 0.26f, -0.4f, 1.4f));
        andromeda.add(new NebulaPuff(15.0f, -6.0f, 46.0f, 0.85f, 0.70f, 0.98f, 0.28f, 0.4f, 2.7f));
        andromeda.add(new NebulaPuff(-8.0f, -7.0f, 32.0f, 0.30f, 0.50f, 0.88f, 0.22f, -0.3f, 4.0f));
        andromeda.add(new NebulaPuff(8.0f, 7.0f, 34.0f, 0.95f, 0.80f, 1.0f, 0.38f, 0.5f, 5.2f));
        NEBULA_COMPLEXES.add(new NebulaComplex(15.0f, 55.0f, "Andromeda Spiral Halo", andromeda));

        // 4. The Abyssal Void Rift & Ethereal Corona (Side A - Dark Silhouette Rift with Violet Border)
        List<NebulaPuff> voidRift = new ArrayList<>();
        voidRift.add(new NebulaPuff(0.0f, 0.0f, 48.0f, 0.02f, 0.01f, 0.04f, 0.48f, 0.2f, 0.0f));
        voidRift.add(new NebulaPuff(-10.0f, 4.0f, 40.0f, 0.03f, 0.02f, 0.05f, 0.42f, -0.3f, 1.6f));
        voidRift.add(new NebulaPuff(11.0f, -5.0f, 42.0f, 0.02f, 0.01f, 0.03f, 0.44f, 0.3f, 3.2f));
        voidRift.add(new NebulaPuff(-4.0f, -8.0f, 32.0f, 0.60f, 0.15f, 0.90f, 0.24f, -0.5f, 4.5f));
        voidRift.add(new NebulaPuff(5.0f, 8.0f, 34.0f, 0.45f, 0.10f, 0.85f, 0.25f, 0.4f, 0.8f));
        NEBULA_COMPLEXES.add(new NebulaComplex(60.0f, 42.0f, "Abyssal Void Rift", voidRift));

        // 5. The Tarantula Web Nebula (Side A - Teal, Viridian, Neon Violet Tendrils)
        List<NebulaPuff> tarantula = new ArrayList<>();
        tarantula.add(new NebulaPuff(0.0f, 0.0f, 46.0f, 0.10f, 0.85f, 0.80f, 0.32f, 0.4f, 0.0f));
        tarantula.add(new NebulaPuff(-9.0f, -6.0f, 36.0f, 0.70f, 0.20f, 0.90f, 0.26f, -0.5f, 1.3f));
        tarantula.add(new NebulaPuff(10.0f, 7.0f, 38.0f, 0.05f, 0.95f, 0.70f, 0.28f, 0.6f, 2.7f));
        tarantula.add(new NebulaPuff(-7.0f, 8.0f, 28.0f, 0.80f, 0.30f, 0.85f, 0.22f, -0.4f, 4.1f));
        tarantula.add(new NebulaPuff(8.0f, -8.0f, 30.0f, 0.15f, 0.75f, 0.90f, 0.24f, 0.3f, 5.4f));
        NEBULA_COMPLEXES.add(new NebulaComplex(80.0f, 22.0f, "Tarantula Web Nebula", tarantula));

        // 6. The Cygnus Swan Veil (Side A - Lilac, Silver, Aether Cyan Filaments)
        List<NebulaPuff> cygnus = new ArrayList<>();
        cygnus.add(new NebulaPuff(0.0f, 0.0f, 50.0f, 0.40f, 0.70f, 1.0f, 0.30f, 0.3f, 0.0f));
        cygnus.add(new NebulaPuff(-12.0f, 5.0f, 38.0f, 0.75f, 0.50f, 0.95f, 0.25f, -0.4f, 1.8f));
        cygnus.add(new NebulaPuff(13.0f, -5.0f, 40.0f, 0.30f, 0.85f, 0.95f, 0.26f, 0.5f, 3.4f));
        cygnus.add(new NebulaPuff(0.0f, 10.0f, 30.0f, 0.85f, 0.60f, 0.90f, 0.22f, -0.3f, 4.9f));
        NEBULA_COMPLEXES.add(new NebulaComplex(110.0f, 60.0f, "Cygnus Swan Veil", cygnus));

        // 7. The Witchhead Reflection Shroud (Side A - Bioluminescent Electric Cyan & Royal Purple)
        List<NebulaPuff> witchhead = new ArrayList<>();
        witchhead.add(new NebulaPuff(0.0f, 0.0f, 52.0f, 0.10f, 0.75f, 0.95f, 0.32f, 0.5f, 0.0f));
        witchhead.add(new NebulaPuff(-11.0f, -7.0f, 40.0f, 0.55f, 0.15f, 0.90f, 0.28f, -0.4f, 1.5f));
        witchhead.add(new NebulaPuff(12.0f, 8.0f, 42.0f, 0.05f, 0.85f, 0.98f, 0.26f, 0.6f, 3.0f));
        witchhead.add(new NebulaPuff(-6.0f, 9.0f, 30.0f, 0.65f, 0.25f, 0.85f, 0.22f, -0.5f, 4.5f));
        NEBULA_COMPLEXES.add(new NebulaComplex(120.0f, 52.0f, "Witchhead Reflection Shroud", witchhead));

        // 8. The Orion Archon Complex (Side A - Violet, Neon Rose, Magenta, Electric Amethyst)
        List<NebulaPuff> amethyst = new ArrayList<>();
        amethyst.add(new NebulaPuff(0.0f, 0.0f, 54.0f, 0.70f, 0.15f, 1.0f, 0.35f, 0.4f, 0.0f));
        amethyst.add(new NebulaPuff(-12.0f, 7.0f, 40.0f, 0.90f, 0.20f, 0.80f, 0.30f, -0.5f, 1.3f));
        amethyst.add(new NebulaPuff(13.0f, -8.0f, 44.0f, 0.50f, 0.10f, 0.95f, 0.28f, 0.6f, 2.6f));
        amethyst.add(new NebulaPuff(-7.0f, -9.0f, 34.0f, 1.0f, 0.28f, 0.70f, 0.24f, -0.4f, 3.9f));
        amethyst.add(new NebulaPuff(9.0f, 9.0f, 36.0f, 0.60f, 0.12f, 0.90f, 0.25f, 0.5f, 5.2f));
        amethyst.add(new NebulaPuff(16.0f, 3.0f, 30.0f, 0.80f, 0.22f, 0.85f, 0.20f, -0.3f, 0.4f));
        amethyst.add(new NebulaPuff(-17.0f, -4.0f, 28.0f, 0.45f, 0.06f, 0.75f, 0.18f, 0.4f, 1.7f));
        amethyst.add(new NebulaPuff(2.0f, 1.0f, 24.0f, 1.0f, 0.60f, 0.95f, 0.42f, -0.6f, 2.9f));
        NEBULA_COMPLEXES.add(new NebulaComplex(145.0f, 38.0f, "Orion Archon Remnant", amethyst));

        // 9. The Southern Cross Dark Horse Nebula (Side A - Dense Dark Interstellar Dust Lanes)
        List<NebulaPuff> darkHorse = new ArrayList<>();
        darkHorse.add(new NebulaPuff(0.0f, 0.0f, 48.0f, 0.03f, 0.02f, 0.05f, 0.44f, 0.2f, 0.0f));
        darkHorse.add(new NebulaPuff(-10.0f, 6.0f, 38.0f, 0.04f, 0.02f, 0.06f, 0.38f, -0.3f, 1.7f));
        darkHorse.add(new NebulaPuff(11.0f, -7.0f, 40.0f, 0.02f, 0.01f, 0.04f, 0.40f, 0.3f, 3.3f));
        darkHorse.add(new NebulaPuff(-5.0f, -8.0f, 30.0f, 0.04f, 0.03f, 0.07f, 0.34f, -0.4f, 4.9f));
        NEBULA_COMPLEXES.add(new NebulaComplex(170.0f, 68.0f, "Dark Horse Interstellar Rift", darkHorse));

        // 10. The Crimson Carina Superbubble (Side B - Deep Crimson, Coral, Rose Quartz, Pyre Embers)
        List<NebulaPuff> carina = new ArrayList<>();
        carina.add(new NebulaPuff(0.0f, 0.0f, 56.0f, 0.95f, 0.15f, 0.30f, 0.35f, 0.4f, 0.0f));
        carina.add(new NebulaPuff(-13.0f, -7.0f, 44.0f, 0.85f, 0.25f, 0.45f, 0.30f, -0.5f, 1.4f));
        carina.add(new NebulaPuff(14.0f, 8.0f, 46.0f, 1.0f, 0.10f, 0.20f, 0.28f, 0.5f, 2.8f));
        carina.add(new NebulaPuff(-8.0f, 9.0f, 34.0f, 0.90f, 0.40f, 0.55f, 0.24f, -0.4f, 4.2f));
        carina.add(new NebulaPuff(9.0f, -9.0f, 36.0f, 0.75f, 0.10f, 0.15f, 0.26f, 0.3f, 5.6f));
        carina.add(new NebulaPuff(0.0f, 0.0f, 26.0f, 1.0f, 0.50f, 0.35f, 0.42f, -0.6f, 1.0f));
        NEBULA_COMPLEXES.add(new NebulaComplex(185.0f, 32.0f, "Crimson Carina Superbubble", carina));

        // 11. The Great Coal-Sack Dust Rifts (Side B - Dark Absorption Interstellar Clouds)
        List<NebulaPuff> dustRifts = new ArrayList<>();
        dustRifts.add(new NebulaPuff(0.0f, 0.0f, 46.0f, 0.03f, 0.02f, 0.05f, 0.44f, 0.2f, 0.0f));
        dustRifts.add(new NebulaPuff(-8.0f, 5.0f, 36.0f, 0.04f, 0.02f, 0.06f, 0.40f, -0.2f, 1.5f));
        dustRifts.add(new NebulaPuff(9.0f, -6.0f, 38.0f, 0.02f, 0.01f, 0.04f, 0.38f, 0.3f, 3.0f));
        dustRifts.add(new NebulaPuff(-14.0f, -4.0f, 30.0f, 0.04f, 0.03f, 0.07f, 0.34f, -0.3f, 4.5f));
        dustRifts.add(new NebulaPuff(15.0f, 4.0f, 32.0f, 0.03f, 0.02f, 0.05f, 0.36f, 0.2f, 0.8f));
        NEBULA_COMPLEXES.add(new NebulaComplex(215.0f, 50.0f, "Coal-Sack Dust Rift", dustRifts));

        // 12. The Sapphire Helix Filament (Side B - Deep Sapphire, Cobalt Blue, Ultraviolet Ribbons)
        List<NebulaPuff> sapphire = new ArrayList<>();
        sapphire.add(new NebulaPuff(0.0f, 0.0f, 52.0f, 0.15f, 0.35f, 1.0f, 0.34f, 0.4f, 0.0f));
        sapphire.add(new NebulaPuff(-12.0f, -8.0f, 40.0f, 0.35f, 0.15f, 0.95f, 0.28f, -0.5f, 1.6f));
        sapphire.add(new NebulaPuff(13.0f, 9.0f, 42.0f, 0.05f, 0.55f, 1.0f, 0.30f, 0.6f, 3.1f));
        sapphire.add(new NebulaPuff(-7.0f, 9.0f, 32.0f, 0.45f, 0.25f, 0.90f, 0.24f, -0.4f, 4.6f));
        sapphire.add(new NebulaPuff(8.0f, -9.0f, 34.0f, 0.10f, 0.40f, 0.85f, 0.22f, 0.3f, 0.9f));
        NEBULA_COMPLEXES.add(new NebulaComplex(240.0f, 35.0f, "Sapphire Helix Filament", sapphire));

        // 13. The Amber Solar Nursery & Flare (Side B - Golden Amber, Solar Orange, Warm Peach)
        List<NebulaPuff> amber = new ArrayList<>();
        amber.add(new NebulaPuff(0.0f, 0.0f, 54.0f, 1.0f, 0.68f, 0.12f, 0.35f, 0.5f, 0.0f));
        amber.add(new NebulaPuff(-11.0f, -6.0f, 42.0f, 1.0f, 0.42f, 0.06f, 0.30f, -0.4f, 1.4f));
        amber.add(new NebulaPuff(10.0f, 7.0f, 44.0f, 1.0f, 0.88f, 0.22f, 0.28f, 0.6f, 2.8f));
        amber.add(new NebulaPuff(-7.0f, 8.0f, 32.0f, 0.98f, 0.58f, 0.12f, 0.24f, -0.5f, 4.2f));
        amber.add(new NebulaPuff(8.0f, -8.0f, 34.0f, 1.0f, 0.32f, 0.02f, 0.26f, 0.3f, 5.6f));
        amber.add(new NebulaPuff(15.0f, -3.0f, 28.0f, 0.92f, 0.78f, 0.18f, 0.20f, -0.3f, 0.9f));
        amber.add(new NebulaPuff(0.0f, 12.0f, 30.0f, 1.0f, 0.50f, 0.10f, 0.22f, 0.4f, 2.3f));
        amber.add(new NebulaPuff(-1.0f, -1.0f, 22.0f, 1.0f, 0.95f, 0.65f, 0.45f, -0.7f, 3.7f));
        NEBULA_COMPLEXES.add(new NebulaComplex(265.0f, 28.0f, "Amber Nursery", amber));

        // 14. The Antares Blood Veil (Side B - Deep Ruby, Garnet, Amber Reflection Nebulae)
        List<NebulaPuff> antares = new ArrayList<>();
        antares.add(new NebulaPuff(0.0f, 0.0f, 50.0f, 0.90f, 0.12f, 0.18f, 0.34f, 0.4f, 0.0f));
        antares.add(new NebulaPuff(-11.0f, 6.0f, 38.0f, 0.95f, 0.35f, 0.10f, 0.28f, -0.5f, 1.7f));
        antares.add(new NebulaPuff(12.0f, -6.0f, 40.0f, 0.75f, 0.08f, 0.22f, 0.30f, 0.5f, 3.2f));
        antares.add(new NebulaPuff(-6.0f, -7.0f, 30.0f, 0.85f, 0.20f, 0.15f, 0.24f, -0.4f, 4.8f));
        antares.add(new NebulaPuff(7.0f, 7.0f, 32.0f, 1.0f, 0.50f, 0.20f, 0.38f, 0.6f, 0.5f));
        NEBULA_COMPLEXES.add(new NebulaComplex(285.0f, 22.0f, "Antares Blood Veil", antares));

        // 15. The Golden Phoenix Supernova Remnant (Side B - Incandescent Gold & Fiery Orange Shockwaves)
        List<NebulaPuff> phoenix = new ArrayList<>();
        phoenix.add(new NebulaPuff(0.0f, 0.0f, 56.0f, 1.0f, 0.80f, 0.20f, 0.35f, 0.4f, 0.0f));
        phoenix.add(new NebulaPuff(-14.0f, -7.0f, 42.0f, 1.0f, 0.45f, 0.10f, 0.30f, -0.4f, 1.6f));
        phoenix.add(new NebulaPuff(15.0f, 8.0f, 44.0f, 0.95f, 0.90f, 0.35f, 0.28f, 0.5f, 3.1f));
        phoenix.add(new NebulaPuff(-7.0f, 9.0f, 32.0f, 1.0f, 0.35f, 0.05f, 0.25f, -0.5f, 4.7f));
        phoenix.add(new NebulaPuff(8.0f, -9.0f, 34.0f, 1.0f, 0.65f, 0.15f, 0.27f, 0.3f, 0.8f));
        NEBULA_COMPLEXES.add(new NebulaComplex(300.0f, 62.0f, "Golden Phoenix Remnant", phoenix));

        // 16. The Obsidian Emerald & Sylvan Filament (Side B - Deep Emerald, Viridian, Celestial Jade)
        List<NebulaPuff> emerald = new ArrayList<>();
        emerald.add(new NebulaPuff(0.0f, 0.0f, 50.0f, 0.06f, 0.88f, 0.58f, 0.32f, 0.4f, 0.0f));
        emerald.add(new NebulaPuff(-9.0f, 6.0f, 38.0f, 0.12f, 0.72f, 0.78f, 0.26f, -0.5f, 1.5f));
        emerald.add(new NebulaPuff(10.0f, -7.0f, 40.0f, 0.02f, 0.98f, 0.48f, 0.24f, 0.5f, 3.0f));
        emerald.add(new NebulaPuff(-8.0f, -7.0f, 30.0f, 0.18f, 0.58f, 0.88f, 0.21f, -0.4f, 4.5f));
        emerald.add(new NebulaPuff(8.0f, 8.0f, 32.0f, 0.08f, 0.84f, 0.64f, 0.22f, 0.3f, 0.2f));
        emerald.add(new NebulaPuff(-14.0f, 2.0f, 26.0f, 0.04f, 0.65f, 0.50f, 0.18f, -0.3f, 1.7f));
        emerald.add(new NebulaPuff(13.0f, 3.0f, 28.0f, 0.15f, 0.92f, 0.70f, 0.19f, 0.4f, 3.2f));
        NEBULA_COMPLEXES.add(new NebulaComplex(325.0f, 45.0f, "Emerald Filament", emerald));

        // 17. The Prismatic Stellar Cradle (Side B - Multi-Hued Iridescent Chromatic Cloudlets)
        List<NebulaPuff> prismatic = new ArrayList<>();
        prismatic.add(new NebulaPuff(0.0f, 0.0f, 52.0f, 0.90f, 0.55f, 0.95f, 0.30f, 0.4f, 0.0f));
        prismatic.add(new NebulaPuff(-11.0f, 6.0f, 40.0f, 0.40f, 0.90f, 0.85f, 0.26f, -0.4f, 1.6f));
        prismatic.add(new NebulaPuff(12.0f, -6.0f, 42.0f, 0.95f, 0.85f, 0.40f, 0.28f, 0.5f, 3.2f));
        prismatic.add(new NebulaPuff(-6.0f, -7.0f, 32.0f, 0.55f, 0.40f, 0.98f, 0.22f, -0.5f, 4.8f));
        prismatic.add(new NebulaPuff(7.0f, 7.0f, 34.0f, 0.98f, 0.60f, 0.50f, 0.24f, 0.3f, 0.7f));
        NEBULA_COMPLEXES.add(new NebulaComplex(350.0f, 48.0f, "Prismatic Stellar Cradle", prismatic));

        // 18. The Polar Auroral Crown (Zenith / Circumpolar High Latitude Shimmer)
        List<NebulaPuff> aurora = new ArrayList<>();
        aurora.add(new NebulaPuff(0.0f, 0.0f, 58.0f, 0.20f, 0.95f, 0.80f, 0.26f, 0.3f, 0.0f));
        aurora.add(new NebulaPuff(-25.0f, -4.0f, 48.0f, 0.60f, 0.25f, 0.95f, 0.22f, -0.3f, 1.8f));
        aurora.add(new NebulaPuff(30.0f, 3.0f, 50.0f, 0.10f, 0.85f, 0.90f, 0.24f, 0.4f, 3.6f));
        aurora.add(new NebulaPuff(-55.0f, -6.0f, 45.0f, 0.75f, 0.35f, 0.85f, 0.20f, -0.4f, 5.4f));
        aurora.add(new NebulaPuff(60.0f, 4.0f, 46.0f, 0.30f, 0.98f, 0.65f, 0.22f, 0.3f, 1.2f));
        aurora.add(new NebulaPuff(0.0f, 6.0f, 40.0f, 0.90f, 0.80f, 0.98f, 0.25f, -0.5f, 3.0f));
        NEBULA_COMPLEXES.add(new NebulaComplex(0.0f, 82.0f, "Polar Auroral Crown", aurora));

        // 20. The Phoenix Stellar Nursery (Deep Fiery Amber & Rose Quartz Emission Nursery)
        List<NebulaPuff> phoenixNursery = new ArrayList<>();
        phoenixNursery.add(new NebulaPuff(0.0f, 0.0f, 54.0f, 1.0f, 0.45f, 0.15f, 0.32f, 0.4f, 0.0f));
        phoenixNursery.add(new NebulaPuff(-12.0f, 8.0f, 42.0f, 0.95f, 0.20f, 0.35f, 0.28f, -0.3f, 1.4f));
        phoenixNursery.add(new NebulaPuff(14.0f, -6.0f, 44.0f, 1.0f, 0.70f, 0.20f, 0.30f, 0.5f, 2.8f));
        phoenixNursery.add(new NebulaPuff(0.0f, -10.0f, 36.0f, 0.85f, 0.15f, 0.10f, 0.25f, -0.4f, 4.2f));
        NEBULA_COMPLEXES.add(new NebulaComplex(195.0f, 35.0f, "Phoenix Stellar Nursery", phoenixNursery));

        // 21. The Emerald Aurora Veil (Luminescent Jade, Viridian & Turquoise Polar Tendrils)
        List<NebulaPuff> emeraldVeil = new ArrayList<>();
        emeraldVeil.add(new NebulaPuff(0.0f, 0.0f, 52.0f, 0.10f, 0.95f, 0.65f, 0.30f, 0.3f, 0.0f));
        emeraldVeil.add(new NebulaPuff(-14.0f, -6.0f, 40.0f, 0.05f, 0.85f, 0.80f, 0.26f, -0.4f, 1.6f));
        emeraldVeil.add(new NebulaPuff(15.0f, 7.0f, 42.0f, 0.20f, 1.0f, 0.50f, 0.28f, 0.5f, 3.2f));
        emeraldVeil.add(new NebulaPuff(-6.0f, 10.0f, 32.0f, 0.15f, 0.90f, 0.90f, 0.22f, -0.3f, 4.8f));
        NEBULA_COMPLEXES.add(new NebulaComplex(330.0f, 65.0f, "Emerald Aurora Veil", emeraldVeil));

        // 22. The Void Blossom Nebula (Ethereal Indigo & Violet Cosmic Flower)
        List<NebulaPuff> voidBlossom = new ArrayList<>();
        voidBlossom.add(new NebulaPuff(0.0f, 0.0f, 56.0f, 0.55f, 0.10f, 0.90f, 0.34f, 0.4f, 0.0f));
        voidBlossom.add(new NebulaPuff(-11.0f, 8.0f, 42.0f, 0.80f, 0.20f, 0.75f, 0.28f, -0.5f, 1.3f));
        voidBlossom.add(new NebulaPuff(12.0f, -8.0f, 44.0f, 0.35f, 0.05f, 0.85f, 0.26f, 0.6f, 2.7f));
        voidBlossom.add(new NebulaPuff(8.0f, 9.0f, 34.0f, 0.90f, 0.30f, 0.65f, 0.24f, -0.3f, 4.1f));
        NEBULA_COMPLEXES.add(new NebulaComplex(270.0f, 50.0f, "Void Blossom Nebula", voidBlossom));

        // 23. The Singularity Gravitational Halo (Cyan Accretion Halo & Relativistic Lensing)
        List<NebulaPuff> singularityHalo = new ArrayList<>();
        singularityHalo.add(new NebulaPuff(0.0f, 0.0f, 48.0f, 0.02f, 0.01f, 0.03f, 0.50f, 0.2f, 0.0f));
        singularityHalo.add(new NebulaPuff(-8.0f, 0.0f, 38.0f, 0.0f, 0.80f, 1.0f, 0.30f, -0.6f, 1.5f));
        singularityHalo.add(new NebulaPuff(8.0f, 0.0f, 38.0f, 0.2f, 0.95f, 1.0f, 0.30f, 0.6f, 3.0f));
        singularityHalo.add(new NebulaPuff(0.0f, -7.0f, 32.0f, 0.1f, 0.70f, 0.95f, 0.25f, -0.4f, 4.5f));
        singularityHalo.add(new NebulaPuff(0.0f, 7.0f, 32.0f, 0.3f, 0.90f, 1.0f, 0.25f, 0.4f, 0.8f));
        NEBULA_COMPLEXES.add(new NebulaComplex(50.0f, 75.0f, "Singularity Gravitational Halo", singularityHalo));

        // 20. The Southern Carina & Volans Veil (Deep Southern Horizon - Rose, Magenta, Cyan Puffs down to -15°)
        List<NebulaPuff> southCarina = new ArrayList<>();
        southCarina.add(new NebulaPuff(0.0f, 0.0f, 54.0f, 0.90f, 0.25f, 0.70f, 0.32f, 0.4f, 0.0f));
        southCarina.add(new NebulaPuff(-12.0f, -5.0f, 44.0f, 0.30f, 0.75f, 1.0f, 0.28f, -0.5f, 1.4f));
        southCarina.add(new NebulaPuff(14.0f, 6.0f, 46.0f, 0.95f, 0.40f, 0.60f, 0.26f, 0.5f, 2.7f));
        southCarina.add(new NebulaPuff(-6.0f, -8.0f, 36.0f, 0.20f, 0.85f, 0.90f, 0.24f, -0.4f, 4.1f));
        southCarina.add(new NebulaPuff(8.0f, -6.0f, 34.0f, 0.70f, 0.20f, 0.95f, 0.22f, 0.3f, 5.3f));
        NEBULA_COMPLEXES.add(new NebulaComplex(195.0f, -6.0f, "Southern Carina Veil", southCarina));

        // 21. The Eridanus Southern River Rift (Deep South Horizon - Dark Dust & Amber Gold down to -15°)
        List<NebulaPuff> eridanus = new ArrayList<>();
        eridanus.add(new NebulaPuff(0.0f, 0.0f, 52.0f, 0.85f, 0.55f, 0.15f, 0.30f, 0.3f, 0.0f));
        eridanus.add(new NebulaPuff(-10.0f, -6.0f, 42.0f, 0.03f, 0.02f, 0.05f, 0.45f, -0.4f, 1.8f));
        eridanus.add(new NebulaPuff(12.0f, 4.0f, 40.0f, 0.95f, 0.70f, 0.25f, 0.26f, 0.5f, 3.2f));
        eridanus.add(new NebulaPuff(-5.0f, -7.0f, 32.0f, 0.02f, 0.01f, 0.04f, 0.42f, -0.3f, 4.6f));
        NEBULA_COMPLEXES.add(new NebulaComplex(275.0f, -8.0f, "Eridanus Southern Rift", eridanus));

        // 22. The Fornax-Sculptor Void Shroud (Deep South Horizon - Abyssal Violet down to -15°)
        List<NebulaPuff> fornax = new ArrayList<>();
        fornax.add(new NebulaPuff(0.0f, 0.0f, 50.0f, 0.50f, 0.20f, 0.90f, 0.30f, 0.4f, 0.0f));
        fornax.add(new NebulaPuff(-11.0f, -4.0f, 38.0f, 0.25f, 0.50f, 0.95f, 0.26f, -0.5f, 1.6f));
        fornax.add(new NebulaPuff(13.0f, 5.0f, 42.0f, 0.80f, 0.30f, 0.75f, 0.25f, 0.6f, 3.1f));
        fornax.add(new NebulaPuff(0.0f, -5.0f, 30.0f, 0.03f, 0.02f, 0.06f, 0.40f, -0.3f, 4.7f));
        NEBULA_COMPLEXES.add(new NebulaComplex(330.0f, -10.0f, "Fornax-Sculptor Void Shroud", fornax));

        // All-Sky Background Interstellar Dust Puffs (18 faint clouds distributed across both sky hemispheres including low declinations)
        List<NebulaPuff> allSkyDust = new ArrayList<>();
        float[][] dustCoords = {
                {30.0f, 30.0f, 0.30f, 0.20f, 0.50f},
                {50.0f, 65.0f, 0.20f, 0.50f, 0.40f},
                {70.0f, 15.0f, 0.50f, 0.20f, 0.30f},
                {95.0f, 75.0f, 0.20f, 0.30f, 0.60f},
                {115.0f, 40.0f, 0.60f, 0.30f, 0.20f},
                {135.0f, 8.0f, 0.15f, 0.55f, 0.45f},
                {150.0f, -8.0f, 0.40f, 0.20f, 0.55f},
                {160.0f, 70.0f, 0.45f, 0.20f, 0.40f},
                {180.0f, 25.0f, 0.60f, 0.15f, 0.30f},
                {200.0f, -12.0f, 0.35f, 0.65f, 0.85f},
                {220.0f, 60.0f, 0.30f, 0.20f, 0.70f},
                {240.0f, 62.0f, 0.50f, 0.30f, 0.25f},
                {255.0f, -10.0f, 0.75f, 0.35f, 0.20f},
                {260.0f, 32.0f, 0.70f, 0.45f, 0.15f},
                {280.0f, 72.0f, 0.80f, 0.25f, 0.35f},
                {295.0f, -14.0f, 0.40f, 0.25f, 0.65f},
                {315.0f, 50.0f, 0.10f, 0.60f, 0.50f},
                {340.0f, 68.0f, 0.20f, 0.45f, 0.55f}
        };
        for (int d = 0; d < dustCoords.length; d++) {
            float[] dc = dustCoords[d];
            allSkyDust.add(new NebulaPuff(dc[0], dc[1], 46.0f, dc[2], dc[3], dc[4], 0.16f, (d % 2 == 0 ? 0.3f : -0.3f), d * 1.5f));
        }
        NEBULA_COMPLEXES.add(new NebulaComplex(0.0f, 0.0f, "Interstellar Cosmic Dust", allSkyDust));
    }

    public static void renderSky(PoseStack poseStack, Matrix4f projectionMatrix, Camera camera, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player player = mc.player;
        if (level == null || player == null) return;

        // 1. Dimensional Checks
        boolean isEnd = level.dimension().equals(Level.END);
        boolean isNether = level.dimension().equals(Level.NETHER);
        if (isNether) return;

        float starBrightness;
        if (isEnd) {
            starBrightness = 1.0f;
        } else {
            if (!level.dimensionType().hasSkyLight()) return;
            starBrightness = level.getStarBrightness(partialTick);
            if (starBrightness <= 0.01f) return;

            float rain = level.getRainLevel(partialTick);
            if (rain > 0.01f && player.getY() < 192.0) {
                starBrightness *= (1.0f - rain * 0.85f);
                if (starBrightness <= 0.01f) return;
            }
        }

        Collection<Constellation> visibleConstellations = ModConstellations.getAllConstellations();

        int playerOpticTier = PlayerAstralProgress.getInstrumentOpticTier(player);

        long gameTime = level.getGameTime();
        float timeAnim = (gameTime + partialTick) * 0.05f;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        RenderType starRenderType = RenderType.entityTranslucentEmissive(STAR_TEXTURE);
        RenderType nebulaRenderType = RenderType.entityTranslucentEmissive(NEBULA_PUFF_TEXTURE);
        RenderType lineRenderType = RenderType.entityTranslucentEmissive(WHITE_TEXTURE);

        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();

        float celestialAngle = isEnd ? ((gameTime + partialTick) * 0.02f) : (level.getTimeOfDay(partialTick) * 360.0f);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(celestialAngle + 180.0F));

        Matrix4f matrix = poseStack.last().pose();
        float skyRadius = 100.0f;

        // ----------------------------------------------------
        // PASS 1: Organic Nebulae Clouds & Dark Dust Rifts (Using NEBULA_PUFF_TEXTURE)
        // ----------------------------------------------------
        VertexConsumer nebulaConsumer = bufferSource.getBuffer(nebulaRenderType);
        for (NebulaComplex complex : NEBULA_COMPLEXES) {
            for (NebulaPuff puff : complex.puffs) {
                float azimDeg = complex.baseAzim + puff.dAzim();
                float altDeg = complex.baseAlt + puff.dAlt();
                float azim = (float) Math.toRadians(azimDeg);
                float alt = (float) Math.toRadians(altDeg);

                float x = skyRadius * 0.94f * Mth.cos(alt) * Mth.sin(azim);
                float y = skyRadius * 0.94f * Mth.sin(alt);
                float z = skyRadius * 0.94f * Mth.cos(alt) * Mth.cos(azim);

                // Subtle organic sinusoidal breathing & rotational drift
                float breathing = 0.80f + 0.20f * Mth.sin(timeAnim * 0.25f + puff.phase());
                float a = starBrightness * puff.a() * breathing;
                float rotRad = timeAnim * 0.04f * puff.rotSpeed() + puff.phase();

                renderSphericalBillboardQuad(nebulaConsumer, matrix, x, y, z, puff.size(), rotRad, puff.r(), puff.g(), puff.b(), a, light, overlay);
            }
        }

        // Pass 1.5: Supernova Figure-8 Hourglass Nebulae & Shockwave Rings
        for (SupernovaEvent se : SupernovaManager.getAllEvents()) {
            if (se.phase() == SupernovaPhase.EXPANDING_NEBULA || se.phase() == SupernovaPhase.REMNANT || se.phase() == SupernovaPhase.FLASH) {
                List<SupernovaManager.FigureEightPuff> puffs = SupernovaManager.getFigureEightPuffs(se);
                for (SupernovaManager.FigureEightPuff puff : puffs) {
                    float azimDeg = se.azimuthDeg() + puff.dAzim();
                    float altDeg = se.altitudeDeg() + puff.dAlt();
                    float azim = (float) Math.toRadians(azimDeg);
                    float alt = (float) Math.toRadians(altDeg);

                    float x = skyRadius * 0.94f * Mth.cos(alt) * Mth.sin(azim);
                    float y = skyRadius * 0.94f * Mth.sin(alt);
                    float z = skyRadius * 0.94f * Mth.cos(alt) * Mth.cos(azim);

                    float breathing = 0.82f + 0.18f * Mth.sin(timeAnim * 0.35f + puff.phase());
                    float a = starBrightness * puff.a() * breathing;
                    float rotRad = timeAnim * 0.05f * puff.rotSpeed() + puff.phase();

                    renderSphericalBillboardQuad(nebulaConsumer, matrix, x, y, z, puff.size(), rotRad, puff.r(), puff.g(), puff.b(), a, light, overlay);
                }
            }
        }

        bufferSource.endBatch(nebulaRenderType);

        // ----------------------------------------------------
        // PASS 2: Ambient Stars, Landmarks & Constellation Nodes (Using STAR_TEXTURE with Very Slow Shifting RGB)
        // ----------------------------------------------------
        VertexConsumer starConsumer = bufferSource.getBuffer(starRenderType);

        // A. Render All Ambient Stars in Skybox with Per-Tier Natural Dimming
        for (int i = 0; i < CelestialStarHelper.AMBIENT_STARS.size(); i++) {
            CelestialStarHelper.AmbientStar s = CelestialStarHelper.AMBIENT_STARS.get(i);

            float theta = (float) Math.toRadians(s.azimuth());
            float phi = (float) Math.toRadians(s.altitude());
            float sSize = s.size() * 0.25f;

            float x = skyRadius * Mth.cos(phi) * Mth.sin(theta);
            float y = skyRadius * Mth.sin(phi);
            float z = skyRadius * Mth.cos(phi) * Mth.cos(theta);

            SupernovaManager.OverriddenStar ov = SupernovaManager.getOverriddenStar("a:" + i);
            if (ov != null) {
                float[] rgb = CelestialStarHelper.getShiftingStarRGB(ov.spectralClass(), ov.essenceType(), timeAnim, ov.azimuth());
                if (ov.phase() == SupernovaPhase.PRECURSOR) {
                    float pulse = 0.6f + 0.4f * Mth.sin(timeAnim * 8.0f);
                    float pSize = ov.size() * 0.25f * (0.8f + 0.5f * pulse);
                    renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, pSize, timeAnim * 2.0f, rgb[0], rgb[1] * pulse, rgb[2], starBrightness * 0.95f, light, overlay);
                } else if (ov.phase() == SupernovaPhase.FLASH) {
                    float flashPulse = 0.8f + 0.2f * Mth.sin(timeAnim * 12.0f);
                    renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, ov.size() * 0.35f * flashPulse, timeAnim * 4.0f, 1.0f, 1.0f, 1.0f, starBrightness, light, overlay);
                    renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, ov.size() * 0.55f * flashPulse, 0.0f, rgb[0], rgb[1], rgb[2], starBrightness * 0.7f, light, overlay);
                } else if (ov.phase() == SupernovaPhase.EXPANDING_NEBULA || ov.phase() == SupernovaPhase.REMNANT) {
                    if (ov.remnantType() == StellarRemnantType.BLACK_HOLE) {
                        // Accretion disk (radiant spinning golden/violet ring)
                        renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, ov.size() * 0.35f, timeAnim * 1.2f, rgb[0] * 1.5f, rgb[1] * 1.2f, rgb[2] * 1.8f, starBrightness * 0.95f, light, overlay);
                        // Pitch-black event horizon core
                        renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, ov.size() * 0.16f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, light, overlay);
                    } else if (ov.remnantType() == StellarRemnantType.PULSAR) {
                        float strobe = 0.5f + 0.5f * Mth.sin(timeAnim * 24.0f);
                        renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, ov.size() * 0.28f * (0.7f + 0.6f * strobe), timeAnim * 15.0f, rgb[0], rgb[1], rgb[2], starBrightness * (0.6f + 0.4f * strobe), light, overlay);
                    } else if (ov.remnantType() == StellarRemnantType.MAGNETAR) {
                        float flare = 0.7f + 0.3f * Mth.sin(timeAnim * 5.0f);
                        renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, ov.size() * 0.28f * flare, timeAnim * 3.0f, rgb[0] * 1.2f, rgb[1] * 0.9f, rgb[2] * 1.5f, starBrightness * 0.98f, light, overlay);
                    } else {
                        renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, ov.size() * 0.28f, timeAnim * 0.5f, rgb[0], rgb[1], rgb[2], starBrightness * 0.95f, light, overlay);
                    }
                }
                continue;
            }

            float ambTierDimmer = switch (s.minTier()) {
                case 1 -> 1.0f;
                case 2 -> 0.80f;
                case 3 -> 0.60f;
                case 4 -> 0.40f;
                default -> 1.0f;
            };

            float[] rgb = CelestialStarHelper.getShiftingStarRGB(s.spectralClass(), s.essenceType(), timeAnim, s.azimuth());
            float twinkle = 0.75f + 0.25f * Mth.sin(timeAnim + s.azimuth());
            float a = starBrightness * twinkle * 0.85f * ambTierDimmer;

            renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, sSize, timeAnim * 0.3f + s.azimuth(), rgb[0], rgb[1], rgb[2], a, light, overlay);
        }

        // B. Render Landmark Guide Stars
        for (CelestialStarHelper.LandmarkStar ls : CelestialStarHelper.LANDMARK_STARS) {
            float theta = (float) Math.toRadians(ls.azimuth());
            float phi = (float) Math.toRadians(ls.altitude());
            float sSize = Math.max(2.4f, ls.size() * 0.22f);

            float x = skyRadius * Mth.cos(phi) * Mth.sin(theta);
            float y = skyRadius * Mth.sin(phi);
            float z = skyRadius * Mth.cos(phi) * Mth.cos(theta);

            SupernovaManager.OverriddenStar ov = SupernovaManager.getOverriddenStar("l:" + ls.name());
            if (ov != null) {
                float[] rgb = CelestialStarHelper.getShiftingStarRGB(ov.spectralClass(), ov.essenceType(), timeAnim, ov.azimuth());
                if (ov.phase() == SupernovaPhase.PRECURSOR) {
                    float pulse = 0.6f + 0.4f * Mth.sin(timeAnim * 8.0f);
                    float pSize = sSize * (0.8f + 0.5f * pulse);
                    renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, pSize, timeAnim * 2.0f, rgb[0], rgb[1] * pulse, rgb[2], starBrightness * 0.95f, light, overlay);
                } else if (ov.phase() == SupernovaPhase.FLASH) {
                    float flashPulse = 0.8f + 0.2f * Mth.sin(timeAnim * 12.0f);
                    renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, sSize * 2.5f * flashPulse, timeAnim * 4.0f, 1.0f, 1.0f, 1.0f, starBrightness, light, overlay);
                } else {
                    if (ov.remnantType() == StellarRemnantType.BLACK_HOLE) {
                        renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, sSize * 2.0f, timeAnim * 1.2f, rgb[0] * 1.5f, rgb[1] * 1.2f, rgb[2] * 1.8f, starBrightness * 0.95f, light, overlay);
                        renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, sSize * 0.9f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, light, overlay);
                    } else {
                        renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, sSize * 1.5f, timeAnim * 0.5f, rgb[0], rgb[1], rgb[2], starBrightness * 0.95f, light, overlay);
                    }
                }
                continue;
            }

            float[] rgb = CelestialStarHelper.getShiftingStarRGB(ls.spectralClass(), ls.essenceType(), timeAnim, ls.azimuth());
            float twinkle = 0.85f + 0.15f * Mth.sin(timeAnim * 1.5f + ls.azimuth());
            float a = starBrightness * twinkle;

            renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, sSize, timeAnim * 0.2f + ls.azimuth(), rgb[0], rgb[1], rgb[2], a, light, overlay);
        }

        // C. Render All Constellation Star Vertices (All Tiers Visible, Slightly Dimmer Per Tier)
        for (Constellation constellation : visibleConstellations) {
            List<ConstellationStar> stars = constellation.getStars();

            float tierDimmer = switch (constellation.getTier()) {
                case FUNDAMENTAL -> 1.0f;
                case ADVANCED -> 0.82f;
                case MASTER -> 0.65f;
                case MYTHIC -> 0.48f;
                case TRANSCENDENT -> 0.35f;
            };

            for (int s = 0; s < stars.size(); s++) {
                ConstellationStar star = stars.get(s);
                float starAzimuth = constellation.getStarSphereAzimuth(s);
                float starAltitude = constellation.getStarSphereAltitude(s);

                float x = skyRadius * Mth.cos(starAltitude) * Mth.sin(starAzimuth);
                float y = skyRadius * Mth.sin(starAltitude);
                float z = skyRadius * Mth.cos(starAltitude) * Mth.cos(starAzimuth);

                float[] rgb = CelestialStarHelper.getShiftingStarRGB(star.spectralClass(), constellation.getEssenceType(), timeAnim, (float) (star.x() + star.y()));

                float starSize = Math.max(2.0f, star.brightness() * 2.4f);
                float twinkle = 0.85f + 0.15f * Mth.sin(timeAnim * 2.0f + s * 1.5f);
                float alpha = starBrightness * twinkle * tierDimmer;

                renderSphericalBillboardQuad(starConsumer, matrix, x, y, z, starSize, timeAnim * 0.25f + s, rgb[0], rgb[1], rgb[2], alpha, light, overlay);
            }
        }

        // D. Active Supernova Ignition Cores
        List<CelestialEventHelper.ActiveSupernovaState> activeSupernovae = CelestialEventHelper.getActiveSupernovae(gameTime, partialTick);
        for (CelestialEventHelper.ActiveSupernovaState sn : activeSupernovae) {
            float azimRad = (float) Math.toRadians(sn.event().azim());
            float altRad = (float) Math.toRadians(sn.event().alt());
            float cx = skyRadius * Mth.cos(altRad) * Mth.sin(azimRad);
            float cy = skyRadius * Mth.sin(altRad);
            float cz = skyRadius * Mth.cos(altRad) * Mth.cos(azimRad);
            float sz = 16.0f * sn.currentBrightness();
            renderSphericalBillboardQuad(starConsumer, matrix, cx, cy, cz, sz, timeAnim * 0.5f, sn.event().r(), sn.event().g(), sn.event().b(), starBrightness * sn.coreTwinkle(), light, overlay);
        }

        bufferSource.endBatch(starRenderType);

        // ----------------------------------------------------
        // PASS 2.2: The Wandering Spheres (Archon Planets & Dwarf Worlds)
        // ----------------------------------------------------
        for (CelestialEventHelper.PlanetDefinition planet : CelestialEventHelper.PLANETS) {
            float planetTierDimmer = switch (planet.minTier()) {
                case 1 -> 1.0f;
                case 2 -> 0.82f;
                case 3 -> 0.65f;
                case 4 -> 0.48f;
                case 5 -> 0.35f;
                default -> 1.0f;
            };

            float[] skyPos = CelestialEventHelper.getPlanetSkyPos(planet, gameTime, partialTick);
            float azimRad = (float) Math.toRadians(skyPos[0]);
            float altRad = (float) Math.toRadians(skyPos[1]);
            float cx = skyRadius * Mth.cos(altRad) * Mth.sin(azimRad);
            float cy = skyRadius * Mth.sin(altRad);
            float cz = skyRadius * Mth.cos(altRad) * Mth.cos(azimRad);

            RenderType planetType = RenderType.entityTranslucentEmissive(planet.texture());
            VertexConsumer planetConsumer = bufferSource.getBuffer(planetType);
            renderSphericalBillboardQuad(planetConsumer, matrix, cx, cy, cz, planet.angularSize() * 0.55f, 0.0f, 1.0f, 1.0f, 1.0f, starBrightness * 0.95f * planetTierDimmer, light, overlay);
            bufferSource.endBatch(planetType);
        }

        // ----------------------------------------------------
        // PASS 2.3: Supernova Expanding Shockwave Nebula Rings (Using SUPERNOVA_RING_TEXTURE)
        // ----------------------------------------------------
        if (!activeSupernovae.isEmpty()) {
            RenderType supernovaType = RenderType.entityTranslucentEmissive(SUPERNOVA_RING_TEXTURE);
            VertexConsumer snConsumer = bufferSource.getBuffer(supernovaType);
            for (CelestialEventHelper.ActiveSupernovaState sn : activeSupernovae) {
                float azimRad = (float) Math.toRadians(sn.event().azim());
                float altRad = (float) Math.toRadians(sn.event().alt());
                float cx = skyRadius * Mth.cos(altRad) * Mth.sin(azimRad);
                float cy = skyRadius * Mth.sin(altRad);
                float cz = skyRadius * Mth.cos(altRad) * Mth.cos(azimRad);
                float ringSz = sn.expandingRadiusDeg() * 10.0f;
                renderSphericalBillboardQuad(snConsumer, matrix, cx, cy, cz, ringSz, timeAnim * 0.05f, sn.event().r(), sn.event().g(), sn.event().b(), starBrightness * sn.currentBrightness() * 0.75f, light, overlay);
            }
            bufferSource.endBatch(supernovaType);
        }

        // ----------------------------------------------------
        // PASS 2.5: Comet Nuclei & Active Meteor Heads (Using COMET_HEAD_TEXTURE)
        // ----------------------------------------------------
        RenderType cometHeadRenderType = RenderType.entityTranslucentEmissive(COMET_HEAD_TEXTURE);
        VertexConsumer cometHeadConsumer = bufferSource.getBuffer(cometHeadRenderType);

        // A. Comet Nuclei & Glowing Comas
        for (CelestialEventHelper.CometDefinition comet : CelestialEventHelper.COMETS) {
            if (comet.minTier() > playerOpticTier) continue;

            float azim = (comet.baseAzim() + (gameTime + partialTick) * comet.orbitalSpeed() * 0.001f * 360.0f) % 360.0f;
            float alt = comet.baseAlt();
            float azimRad = (float) Math.toRadians(azim);
            float altRad = (float) Math.toRadians(alt);

            float cx = skyRadius * Mth.cos(altRad) * Mth.sin(azimRad);
            float cy = skyRadius * Mth.sin(altRad);
            float cz = skyRadius * Mth.cos(altRad) * Mth.cos(azimRad);

            float comaSz = comet.comaSize();
            float breathing = 0.88f + 0.12f * Mth.sin(timeAnim * 2.5f);
            float alpha = starBrightness * breathing * 0.98f;

            renderSphericalBillboardQuad(cometHeadConsumer, matrix, cx, cy, cz, comaSz, timeAnim * 0.1f, comet.r(), comet.g(), comet.b(), alpha, light, overlay);
        }

        // B. Active Meteor Heads
        List<CelestialEventHelper.ActiveMeteor> meteors = CelestialEventHelper.getActiveMeteors(gameTime, partialTick);
        for (CelestialEventHelper.ActiveMeteor m : meteors) {
            org.joml.Vector3f headPos = CelestialEventHelper.getMeteorPosAt(m, m.progress());
            float hx = headPos.x * skyRadius;
            float hy = headPos.y * skyRadius;
            float hz = headPos.z * skyRadius;

            float mAlpha = starBrightness * m.intensity() * 0.98f;
            renderSphericalBillboardQuad(cometHeadConsumer, matrix, hx, hy, hz, m.size() * 1.5f, timeAnim * 3.0f, m.r(), m.g(), m.b(), mAlpha, light, overlay);
        }

        bufferSource.endBatch(cometHeadRenderType);

        // ----------------------------------------------------
        // PASS 3: Render Comet Curved Ion & Dust Tails & Meteor Streaks (Using METEOR_TRAIL_TEXTURE Ribbon)
        // ----------------------------------------------------
        RenderType trailRenderType = RenderType.entityTranslucentEmissive(METEOR_TRAIL_TEXTURE);
        VertexConsumer trailConsumer = bufferSource.getBuffer(trailRenderType);

        // A. Comet Curved Ion & Dust Tails
        for (CelestialEventHelper.CometDefinition comet : CelestialEventHelper.COMETS) {
            if (comet.minTier() > playerOpticTier) continue;

            float azim = (comet.baseAzim() + (gameTime + partialTick) * comet.orbitalSpeed() * 0.001f * 360.0f) % 360.0f;
            float alt = comet.baseAlt();

            int segments = 24;
            List<org.joml.Vector3f> points = new ArrayList<>();
            List<Float> widths = new ArrayList<>();

            float dir = (comet.orbitalSpeed() >= 0.0f) ? 1.0f : -1.0f;
            for (int seg = 0; seg <= segments; seg++) {
                float segT = seg / (float) segments; // 0.0 at head -> 1.0 at tail end
                // Tail extends backwards behind the orbital direction of motion
                float tailAzim = azim - dir * (segT * comet.tailLengthDeg()) + dir * Mth.sin(segT * (float) Math.PI) * comet.tailCurvature();
                float tailAlt = Mth.clamp(alt - segT * (comet.tailLengthDeg() * 0.25f), 5.0f, 88.0f);

                float tAzimRad = (float) Math.toRadians(tailAzim);
                float tAltRad = (float) Math.toRadians(tailAlt);

                float sx = skyRadius * Mth.cos(tAltRad) * Mth.sin(tAzimRad);
                float sy = skyRadius * Mth.sin(tAltRad);
                float sz = skyRadius * Mth.cos(tAltRad) * Mth.cos(tAzimRad);

                points.add(new org.joml.Vector3f(sx, sy, sz));
                float w = Math.max(0.1f, (1.0f - segT * 0.75f) * (comet.comaSize() * 0.22f));
                widths.add(w);
            }

            renderSphericalRibbon(trailConsumer, matrix, points, widths, comet.tailR(), comet.tailG(), comet.tailB(), starBrightness * 0.85f, light, overlay);
        }

        // B. Active Meteor Curved Trail Streaks
        for (CelestialEventHelper.ActiveMeteor m : meteors) {
            int segments = 32;
            float trailLen = 0.45f; // Trail spans up to 45% backwards along the trajectory
            float tHead = m.progress();
            float tTail = Math.max(0.0f, tHead - trailLen);

            List<org.joml.Vector3f> points = new ArrayList<>();
            List<Float> widths = new ArrayList<>();

            for (int seg = 0; seg <= segments; seg++) {
                float segT = seg / (float) segments;
                float segParam = tTail + (tHead - tTail) * segT; // 0 at tail end -> 1 at head
                org.joml.Vector3f p = CelestialEventHelper.getMeteorPosAt(m, segParam);
                float sx = p.x * skyRadius;
                float sy = p.y * skyRadius;
                float sz = p.z * skyRadius;

                points.add(new org.joml.Vector3f(sx, sy, sz));
                float w = (0.15f + 1.25f * segT * segT) * (m.size() * 0.22f);
                widths.add(w);
            }

            renderSphericalRibbon(trailConsumer, matrix, points, widths, m.r(), m.g(), m.b(), starBrightness * m.intensity() * 0.95f, light, overlay);
        }

        bufferSource.endBatch(trailRenderType);

        // ----------------------------------------------------
        // PASS 4: Render ALL Charted Constellation Connection Lines (Using WHITE_TEXTURE)
        // ----------------------------------------------------
        VertexConsumer lineConsumer = bufferSource.getBuffer(lineRenderType);
        Set<String> chartedEdges = PlayerAstralProgress.getChartedConnections(player);

        for (String edge : chartedEdges) {
            try {
                String[] parts = edge.split("---");
                if (parts.length == 2) {
                    CelestialStarHelper.StarSkyPos p1 = CelestialStarHelper.getStarSkyPositionAndColor(parts[0]);
                    CelestialStarHelper.StarSkyPos p2 = CelestialStarHelper.getStarSkyPositionAndColor(parts[1]);

                    if (p1 != null && p2 != null) {
                        float x1 = skyRadius * Mth.cos(p1.altitudeRad()) * Mth.sin(p1.azimuthRad());
                        float y1 = skyRadius * Mth.sin(p1.altitudeRad());
                        float z1 = skyRadius * Mth.cos(p1.altitudeRad()) * Mth.cos(p1.azimuthRad());

                        float x2 = skyRadius * Mth.cos(p2.altitudeRad()) * Mth.sin(p2.azimuthRad());
                        float y2 = skyRadius * Mth.sin(p2.altitudeRad());
                        float z2 = skyRadius * Mth.cos(p2.altitudeRad()) * Mth.cos(p2.azimuthRad());

                        float r = (p1.r() + p2.r()) * 0.5f;
                        float g = (p1.g() + p2.g()) * 0.5f;
                        float b = (p1.b() + p2.b()) * 0.5f;

                        renderSphericalLineSegment(lineConsumer, matrix, x1, y1, z1, x2, y2, z2, 0.55f, r, g, b, starBrightness * 0.85f, light, overlay);
                    }
                }
            } catch (Exception ignored) {}
        }

        bufferSource.endBatch(lineRenderType);

        poseStack.popPose();
    }

    /**
     * Renders a continuous, seamless spherical ribbon strip with zero inter-segment gaps or angle elbows.
     */
    private static void renderSphericalRibbon(VertexConsumer builder, Matrix4f matrix, List<org.joml.Vector3f> points, List<Float> widths, float r, float g, float b, float a, int light, int overlay) {
        if (points.size() < 2 || a <= 0.001f) return;
        int count = points.size();

        org.joml.Vector3f[] lefts = new org.joml.Vector3f[count];
        org.joml.Vector3f[] rights = new org.joml.Vector3f[count];

        for (int i = 0; i < count; i++) {
            org.joml.Vector3f p = points.get(i);
            org.joml.Vector3f tangent = new org.joml.Vector3f();
            if (i == 0) {
                tangent.set(points.get(1)).sub(p);
            } else if (i == count - 1) {
                tangent.set(p).sub(points.get(count - 2));
            } else {
                tangent.set(points.get(i + 1)).sub(points.get(i - 1));
            }

            org.joml.Vector3f side = new org.joml.Vector3f(tangent).cross(p);
            float len = side.length();
            if (len > 1e-5f) {
                side.div(len);
            } else {
                side.set(1, 0, 0);
            }

            float hw = widths.get(i) * 0.5f;
            lefts[i] = new org.joml.Vector3f(p).sub(new org.joml.Vector3f(side).mul(hw));
            rights[i] = new org.joml.Vector3f(p).add(new org.joml.Vector3f(side).mul(hw));
        }

        for (int i = 0; i < count - 1; i++) {
            float u0 = i / (float) (count - 1);
            float u1 = (i + 1) / (float) (count - 1);

            org.joml.Vector3f l0 = lefts[i];
            org.joml.Vector3f r0 = rights[i];
            org.joml.Vector3f l1 = lefts[i + 1];
            org.joml.Vector3f r1 = rights[i + 1];

            builder.addVertex(matrix, l0.x, l0.y, l0.z).setColor(r, g, b, a).setUv(u0, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            builder.addVertex(matrix, l1.x, l1.y, l1.z).setColor(r, g, b, a).setUv(u1, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            builder.addVertex(matrix, r1.x, r1.y, r1.z).setColor(r, g, b, a).setUv(u1, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            builder.addVertex(matrix, r0.x, r0.y, r0.z).setColor(r, g, b, a).setUv(u0, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        }
    }

    private static void renderSphericalBillboardQuad(VertexConsumer builder, Matrix4f matrix, float x, float y, float z, float size, float rotRad, float r, float g, float b, float a, int light, int overlay) {
        float len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len < 1e-4f) return;
        float nx = x / len;
        float ny = y / len;
        float nz = z / len;

        float upX = 0, upY = 1, upZ = 0;
        if (Math.abs(ny) > 0.99f) {
            upX = 1; upY = 0; upZ = 0;
        }

        float ux = upY * nz - upZ * ny;
        float uy = upZ * nx - upX * nz;
        float uz = upX * ny - upY * nx;
        float uLen = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        if (uLen > 1e-4f) {
            ux /= uLen; uy /= uLen; uz /= uLen;
        } else {
            ux = 1; uy = 0; uz = 0;
        }

        float vx = ny * uz - nz * uy;
        float vy = nz * ux - nx * uz;
        float vz = nx * uy - ny * ux;

        float cosR = (float) Math.cos(rotRad);
        float sinR = (float) Math.sin(rotRad);

        float rUx = cosR * ux + sinR * vx;
        float rUy = cosR * uy + sinR * vy;
        float rUz = cosR * uz + sinR * vz;

        float rVx = -sinR * ux + cosR * vx;
        float rVy = -sinR * uy + cosR * vy;
        float rVz = -sinR * uz + cosR * vz;

        float hs = size * 0.5f;

        float p0x = x - hs * rUx - hs * rVx;
        float p0y = y - hs * rUy - hs * rVy;
        float p0z = z - hs * rUz - hs * rVz;

        float p1x = x + hs * rUx - hs * rVx;
        float p1y = y + hs * rUy - hs * rVy;
        float p1z = z + hs * rUz - hs * rVz;

        float p2x = x + hs * rUx + hs * rVx;
        float p2y = y + hs * rUy + hs * rVy;
        float p2z = z + hs * rUz + hs * rVz;

        float p3x = x - hs * rUx + hs * rVx;
        float p3y = y - hs * rUy + hs * rVy;
        float p3z = z - hs * rUz + hs * rVz;

        builder.addVertex(matrix, p0x, p0y, p0z).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, p1x, p1y, p1z).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, p2x, p2y, p2z).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, p3x, p3y, p3z).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }

    private static void renderSphericalLineSegment(VertexConsumer builder, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float width, float r, float g, float b, float a, int light, int overlay) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;

        float mx = (x1 + x2) * 0.5f;
        float my = (y1 + y2) * 0.5f;
        float mz = (z1 + z2) * 0.5f;

        // Cross product of line vector D and midpoint normal M yields the spherical tangent vector
        float wx = dy * mz - dz * my;
        float wy = dz * mx - dx * mz;
        float wz = dx * my - dy * mx;
        float wLen = (float) Math.sqrt(wx * wx + wy * wy + wz * wz);
        if (wLen > 1e-4f) {
            wx /= wLen;
            wy /= wLen;
            wz /= wLen;
        } else {
            wx = 0; wy = 1; wz = 0;
        }

        float hw = width * 0.5f;
        float offX = wx * hw;
        float offY = wy * hw;
        float offZ = wz * hw;

        builder.addVertex(matrix, x1 - offX, y1 - offY, z1 - offZ).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, x2 - offX, y2 - offY, z2 - offZ).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, x2 + offX, y2 + offY, z2 + offZ).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, x1 + offX, y1 + offY, z1 + offZ).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }
}
