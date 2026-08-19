package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.astral.CelestialEventHelper;
import ddraig.net.entropica.astral.CelestialStarHelper;
import ddraig.net.entropica.astral.Constellation;
import ddraig.net.entropica.astral.ConstellationStar;
import ddraig.net.entropica.astral.ModConstellations;
import ddraig.net.entropica.astral.PlayerAstralProgress;
import ddraig.net.entropica.block.AstralMirrorBlock;
import ddraig.net.entropica.block.entity.AstralMirrorBlockEntity;
import ddraig.net.entropica.config.EntropicaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class AstralMirrorRenderer implements BlockEntityRenderer<AstralMirrorBlockEntity, AstralMirrorRenderer.MirrorRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final ResourceLocation STAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/star.png");
    private static final ResourceLocation COMET_HEAD_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/comet_head.png");
    private static final ResourceLocation METEOR_TRAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/meteor_trail.png");
    private static final ResourceLocation NEBULA_PUFF_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/nebula_puff.png");
    private static final ResourceLocation SUPERNOVA_RING_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/supernova_ring.png");

    public AstralMirrorRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static class MirrorRenderState extends BlockEntityRenderState {
        public BlockPos pos = BlockPos.ZERO;
        public boolean north;
        public boolean east;
        public boolean south;
        public boolean west;
        public int ticks;
        public float partialTick;
        public double camX, camY, camZ;
    }

    @Override
    public MirrorRenderState createRenderState() {
        return new MirrorRenderState();
    }

    @Override
    public void extractRenderState(AstralMirrorBlockEntity be, MirrorRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.pos = be.getBlockPos();
        state.ticks = be.getClientTicks();
        state.partialTick = partialTick;
        state.camX = cameraPos.x;
        state.camY = cameraPos.y;
        state.camZ = cameraPos.z;

        BlockState bs = be.getBlockState();
        if (bs.hasProperty(AstralMirrorBlock.NORTH)) {
            state.north = bs.getValue(AstralMirrorBlock.NORTH);
            state.east = bs.getValue(AstralMirrorBlock.EAST);
            state.south = bs.getValue(AstralMirrorBlock.SOUTH);
            state.west = bs.getValue(AstralMirrorBlock.WEST);
        } else {
            state.north = state.east = state.south = state.west = false;
        }
    }

    @Override
    public void submit(MirrorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        if (!EntropicaConfig.ENABLE_ASTRAL_MIRROR_SKY_REFLECTION.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player player = mc.player;
        if (level == null || player == null) return;

        float starBrightness = level.getStarBrightness(state.partialTick);
        if (starBrightness <= 0.02f) {
            return; // Daytime - mirror reflects dark starlight base texture naturally
        }

        // Feature Toggles from Config
        boolean enableLiquidRefraction = EntropicaConfig.ENABLE_ASTRAL_MIRROR_LIQUID_REFRACTION.get();
        boolean enableParallaxDepth = EntropicaConfig.ENABLE_ASTRAL_MIRROR_PARALLAX_DEPTH.get();
        boolean enable3DBillboard = EntropicaConfig.ENABLE_ASTRAL_MIRROR_3D_BILLBOARD_STARS.get();
        boolean enableNebulae = EntropicaConfig.ENABLE_ASTRAL_MIRROR_NEBULAE.get();
        boolean enableConstellationLines = EntropicaConfig.ENABLE_ASTRAL_MIRROR_CONSTELLATION_LINES.get();

        // Inset pool quad bounds (0.125 on disconnected outer borders, 0.0 on connected interior edges)
        float minX = state.west  ? 0.0f : 0.125f;
        float maxX = state.east  ? 1.0f : 0.875f;
        float minZ = state.north ? 0.0f : 0.125f;
        float maxZ = state.south ? 1.0f : 0.875f;
        float y = 1.002f;

        double blockWorldX = state.pos.getX();
        double blockWorldY = state.pos.getY() + 1.0;
        double blockWorldZ = state.pos.getZ();

        double camRelY = state.camY - blockWorldY;
        if (camRelY <= 0.02) return; // Only render when camera is above the mirror plane

        long gameTime = level.getGameTime();
        float celestialAngle = level.getTimeOfDay(state.partialTick) * 360.0f;
        float animTime = (gameTime + state.partialTick) * 0.04f;

        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        // Transformation helper: Matching CelestialSkyRenderer: R_Y(-90) * R_X(celestialAngle + 180)
        float radX = (float) Math.toRadians(celestialAngle + 180.0f);
        float cosX = Mth.cos(radX);
        float sinX = Mth.sin(radX);

        // ----------------------------------------------------
        // PASS 0: Continuous World-Space Liquid Ether Surface Caustic Sheen
        // ----------------------------------------------------
        if (enableLiquidRefraction) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                Matrix4f mat = pose.pose();
                float c00 = computeCaustic(blockWorldX + minX, blockWorldZ + minZ, animTime, starBrightness);
                float c01 = computeCaustic(blockWorldX + minX, blockWorldZ + maxZ, animTime, starBrightness);
                float c11 = computeCaustic(blockWorldX + maxX, blockWorldZ + maxZ, animTime, starBrightness);
                float c10 = computeCaustic(blockWorldX + maxX, blockWorldZ + minZ, animTime, starBrightness);

                consumer.addVertex(mat, minX, y + 0.0002f, minZ).setColor(0.25f, 0.55f, 0.95f, c00).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                consumer.addVertex(mat, minX, y + 0.0002f, maxZ).setColor(0.20f, 0.45f, 0.85f, c01).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                consumer.addVertex(mat, maxX, y + 0.0002f, maxZ).setColor(0.30f, 0.60f, 1.00f, c11).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                consumer.addVertex(mat, maxX, y + 0.0002f, minZ).setColor(0.22f, 0.50f, 0.90f, c10).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            });
        }

        // ----------------------------------------------------
        // PASS 1: Subtle Cosmic Nebulae Veils (Deep Layer: depth = 1.20)
        // ----------------------------------------------------
        if (enableNebulae) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(NEBULA_PUFF_TEXTURE), (pose, consumer) -> {
                Matrix4f mat = pose.pose();

                for (CelestialSkyRenderer.NebulaComplex complex : CelestialSkyRenderer.NEBULA_COMPLEXES) {
                    for (CelestialSkyRenderer.NebulaPuff puff : complex.puffs) {
                        float azimDeg = complex.baseAzim + puff.dAzim();
                        float altDeg = complex.baseAlt + puff.dAlt();
                        float azim = (float) Math.toRadians(azimDeg);
                        float alt = (float) Math.toRadians(altDeg);

                        // Celestial sphere unrotated
                        float cx = Mth.cos(alt) * Mth.sin(azim);
                        float cy = Mth.sin(alt);
                        float cz = Mth.cos(alt) * Mth.cos(azim);

                        // Apply R_Y(-90) * R_X(celestialAngle + 180)
                        Vector3f app = toDiurnalWorldApparent(cx, cy, cz, cosX, sinX);

                        if (app.y > 0.05f) {
                            double t = camRelY / app.y;
                            double surfWorldX = state.camX + app.x * t;
                            double surfWorldZ = state.camZ + app.z * t;

                            double depth = enableParallaxDepth ? 1.20 : 0.0;
                            double slopeX = (surfWorldX - state.camX) / camRelY;
                            double slopeZ = (surfWorldZ - state.camZ) / camRelY;
                            double depthX = -slopeX * depth;
                            double depthZ = -slopeZ * depth;

                            float waveX = enableLiquidRefraction ? computeFluidWaveX(surfWorldX, surfWorldZ, animTime) : 0.0f;
                            float waveZ = enableLiquidRefraction ? computeFluidWaveZ(surfWorldX, surfWorldZ, animTime) : 0.0f;

                            float localX = (float) (surfWorldX + depthX + waveX - blockWorldX);
                            float localZ = (float) (surfWorldZ + depthZ + waveZ - blockWorldZ);

                            float sz = puff.size() * 0.0085f;
                            float breathing = 0.80f + 0.20f * Mth.sin(animTime * 1.5f + puff.phase());
                            float alpha = starBrightness * puff.a() * breathing * 0.32f;

                            renderClippedQuad(consumer, mat, localX, y + 0.0005f, localZ, sz * 2.0f, minX, maxX, minZ, maxZ, puff.r(), puff.g(), puff.b(), alpha, light, overlay);
                        }
                    }
                }
            });
        }

        // ----------------------------------------------------
        // PASS 2: Twinkling Stars & Constellation Nodes (Using STAR_TEXTURE)
        // ----------------------------------------------------
        Collection<Constellation> visibleConstellations = ModConstellations.getAllConstellations();
        List<CelestialEventHelper.ActiveMeteor> activeMeteors = CelestialEventHelper.getActiveMeteors(gameTime, state.partialTick);

        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(STAR_TEXTURE), (pose, consumer) -> {
            Matrix4f mat = pose.pose();

            // A. Deep Ambient Stars (Depth = 0.90)
            for (int i = 0; i < CelestialStarHelper.AMBIENT_STARS.size(); i++) {
                CelestialStarHelper.AmbientStar s = CelestialStarHelper.AMBIENT_STARS.get(i);
                float theta = (float) Math.toRadians(s.azimuth());
                float phi = (float) Math.toRadians(s.altitude());

                float cx = Mth.cos(phi) * Mth.sin(theta);
                float cy = Mth.sin(phi);
                float cz = Mth.cos(phi) * Mth.cos(theta);

                Vector3f app = toDiurnalWorldApparent(cx, cy, cz, cosX, sinX);

                if (app.y > 0.02f) {
                    double t = camRelY / app.y;
                    double surfWorldX = state.camX + app.x * t;
                    double surfWorldZ = state.camZ + app.z * t;

                    double depth = enableParallaxDepth ? 0.90 : 0.0;
                    double slopeX = (surfWorldX - state.camX) / camRelY;
                    double slopeZ = (surfWorldZ - state.camZ) / camRelY;
                    double depthX = -slopeX * depth;
                    double depthZ = -slopeZ * depth;

                    float waveX = enableLiquidRefraction ? computeFluidWaveX(surfWorldX, surfWorldZ, animTime) : 0.0f;
                    float waveZ = enableLiquidRefraction ? computeFluidWaveZ(surfWorldX, surfWorldZ, animTime) : 0.0f;

                    float localX = (float) (surfWorldX + depthX + waveX - blockWorldX);
                    float localZ = (float) (surfWorldZ + depthZ + waveZ - blockWorldZ);

                    float sz = 0.038f + s.size() * 0.008f;
                    float[] rgb = CelestialStarHelper.getShiftingStarRGB(s.spectralClass(), s.essenceType(), animTime * 2.0f, s.azimuth());
                    float twinkle = 0.75f + 0.25f * Mth.sin(animTime * 4.0f + s.azimuth());
                    float a = starBrightness * twinkle;

                    if (enable3DBillboard) {
                        render3DBillboardStar(consumer, mat, localX, y + 0.0010f, localZ, sz, minX, maxX, minZ, maxZ, state.camX - (blockWorldX + localX), camRelY, state.camZ - (blockWorldZ + localZ), rgb[0], rgb[1], rgb[2], a, light, overlay);
                    } else {
                        renderClippedQuad(consumer, mat, localX, y + 0.0010f, localZ, sz, minX, maxX, minZ, maxZ, rgb[0], rgb[1], rgb[2], a, light, overlay);
                    }
                }
            }

            // B. Mid-Depth Landmark Guide Stars (Depth = 0.50)
            for (CelestialStarHelper.LandmarkStar ls : CelestialStarHelper.LANDMARK_STARS) {
                float theta = (float) Math.toRadians(ls.azimuth());
                float phi = (float) Math.toRadians(ls.altitude());

                float cx = Mth.cos(phi) * Mth.sin(theta);
                float cy = Mth.sin(phi);
                float cz = Mth.cos(phi) * Mth.cos(theta);

                Vector3f app = toDiurnalWorldApparent(cx, cy, cz, cosX, sinX);

                if (app.y > 0.02f) {
                    double t = camRelY / app.y;
                    double surfWorldX = state.camX + app.x * t;
                    double surfWorldZ = state.camZ + app.z * t;

                    double depth = enableParallaxDepth ? 0.50 : 0.0;
                    double slopeX = (surfWorldX - state.camX) / camRelY;
                    double slopeZ = (surfWorldZ - state.camZ) / camRelY;
                    double depthX = -slopeX * depth;
                    double depthZ = -slopeZ * depth;

                    float waveX = enableLiquidRefraction ? computeFluidWaveX(surfWorldX, surfWorldZ, animTime) : 0.0f;
                    float waveZ = enableLiquidRefraction ? computeFluidWaveZ(surfWorldX, surfWorldZ, animTime) : 0.0f;

                    float localX = (float) (surfWorldX + depthX + waveX - blockWorldX);
                    float localZ = (float) (surfWorldZ + depthZ + waveZ - blockWorldZ);

                    float sz = 0.055f + ls.size() * 0.010f;
                    float[] rgb = CelestialStarHelper.getShiftingStarRGB(ls.spectralClass(), ls.essenceType(), animTime * 2.0f, ls.azimuth());
                    float twinkle = 0.85f + 0.15f * Mth.sin(animTime * 3.0f + ls.azimuth());
                    float a = starBrightness * twinkle;

                    if (enable3DBillboard) {
                        render3DBillboardStar(consumer, mat, localX, y + 0.0012f, localZ, sz, minX, maxX, minZ, maxZ, state.camX - (blockWorldX + localX), camRelY, state.camZ - (blockWorldZ + localZ), rgb[0], rgb[1], rgb[2], a, light, overlay);
                    } else {
                        renderClippedQuad(consumer, mat, localX, y + 0.0012f, localZ, sz, minX, maxX, minZ, maxZ, rgb[0], rgb[1], rgb[2], a, light, overlay);
                    }
                }
            }

            // C. Constellation Stars (Depth = 0.25)
            for (Constellation constellation : visibleConstellations) {
                for (int sIdx = 0; sIdx < constellation.getStars().size(); sIdx++) {
                    ConstellationStar star = constellation.getStars().get(sIdx);
                    float starAzimuth = constellation.getStarSphereAzimuth(sIdx);
                    float starAltitude = constellation.getStarSphereAltitude(sIdx);

                    float cx = Mth.cos(starAltitude) * Mth.sin(starAzimuth);
                    float cy = Mth.sin(starAltitude);
                    float cz = Mth.cos(starAltitude) * Mth.cos(starAzimuth);

                    Vector3f app = toDiurnalWorldApparent(cx, cy, cz, cosX, sinX);

                    if (app.y > 0.02f) {
                        double t = camRelY / app.y;
                        double surfWorldX = state.camX + app.x * t;
                        double surfWorldZ = state.camZ + app.z * t;

                        double depth = enableParallaxDepth ? 0.25 : 0.0;
                        double slopeX = (surfWorldX - state.camX) / camRelY;
                        double slopeZ = (surfWorldZ - state.camZ) / camRelY;
                        double depthX = -slopeX * depth;
                        double depthZ = -slopeZ * depth;

                        float waveX = enableLiquidRefraction ? computeFluidWaveX(surfWorldX, surfWorldZ, animTime) : 0.0f;
                        float waveZ = enableLiquidRefraction ? computeFluidWaveZ(surfWorldX, surfWorldZ, animTime) : 0.0f;

                        float localX = (float) (surfWorldX + depthX + waveX - blockWorldX);
                        float localZ = (float) (surfWorldZ + depthZ + waveZ - blockWorldZ);

                        float sz = 0.050f + star.brightness() * 0.022f;
                        float[] rgb = CelestialStarHelper.getShiftingStarRGB(star.spectralClass(), constellation.getEssenceType(), animTime * 2.0f, (float) (star.x() + star.y()));
                        float twinkle = 0.85f + 0.15f * Mth.sin(animTime * 5.0f + sIdx);
                        float tierDimmer = switch (constellation.getTier()) {
                            case FUNDAMENTAL -> 1.0f;
                            case ADVANCED -> 0.82f;
                            case MASTER -> 0.65f;
                            case MYTHIC -> 0.48f;
                            case TRANSCENDENT -> 0.35f;
                        };
                        float a = starBrightness * twinkle * tierDimmer;

                        if (enable3DBillboard) {
                            render3DBillboardStar(consumer, mat, localX, y + 0.0014f, localZ, sz, minX, maxX, minZ, maxZ, state.camX - (blockWorldX + localX), camRelY, state.camZ - (blockWorldZ + localZ), rgb[0], rgb[1], rgb[2], a, light, overlay);
                        } else {
                            renderClippedQuad(consumer, mat, localX, y + 0.0014f, localZ, sz, minX, maxX, minZ, maxZ, rgb[0], rgb[1], rgb[2], a, light, overlay);
                        }
                    }
                }
            }

            // D. Active Supernova Ignition Cores
            List<CelestialEventHelper.ActiveSupernovaState> activeSupernovae = CelestialEventHelper.getActiveSupernovae(gameTime, state.partialTick);
            for (CelestialEventHelper.ActiveSupernovaState sn : activeSupernovae) {
                float theta = (float) Math.toRadians(sn.event().azim());
                float phi = (float) Math.toRadians(sn.event().alt());
                float cx = Mth.cos(phi) * Mth.sin(theta);
                float cy = Mth.sin(phi);
                float cz = Mth.cos(phi) * Mth.cos(theta);

                Vector3f app = toDiurnalWorldApparent(cx, cy, cz, cosX, sinX);
                if (app.y > 0.02f) {
                    double t = camRelY / app.y;
                    double surfWorldX = state.camX + app.x * t;
                    double surfWorldZ = state.camZ + app.z * t;

                    double depth = enableParallaxDepth ? 0.50 : 0.0;
                    double slopeX = (surfWorldX - state.camX) / camRelY;
                    double slopeZ = (surfWorldZ - state.camZ) / camRelY;
                    double depthX = -slopeX * depth;
                    double depthZ = -slopeZ * depth;

                    float waveX = enableLiquidRefraction ? computeFluidWaveX(surfWorldX, surfWorldZ, animTime) : 0.0f;
                    float waveZ = enableLiquidRefraction ? computeFluidWaveZ(surfWorldX, surfWorldZ, animTime) : 0.0f;

                    float localX = (float) (surfWorldX + depthX + waveX - blockWorldX);
                    float localZ = (float) (surfWorldZ + depthZ + waveZ - blockWorldZ);

                    float sz = 0.080f * sn.currentBrightness();
                    float a = starBrightness * sn.coreTwinkle();

                    if (enable3DBillboard) {
                        render3DBillboardStar(consumer, mat, localX, y + 0.0013f, localZ, sz, minX, maxX, minZ, maxZ, state.camX - (blockWorldX + localX), camRelY, state.camZ - (blockWorldZ + localZ), sn.event().r(), sn.event().g(), sn.event().b(), a, light, overlay);
                    } else {
                        renderClippedQuad(consumer, mat, localX, y + 0.0013f, localZ, sz, minX, maxX, minZ, maxZ, sn.event().r(), sn.event().g(), sn.event().b(), a, light, overlay);
                    }
                }
            }
        });

        // ----------------------------------------------------
        // PASS 2.2: The 5 Wandering Spheres (Archon Planets)
        // ----------------------------------------------------
        for (CelestialEventHelper.PlanetDefinition planet : CelestialEventHelper.PLANETS) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(planet.texture()), (pose, consumer) -> {
                Matrix4f mat = pose.pose();
                float[] skyPos = CelestialEventHelper.getPlanetSkyPos(planet, gameTime, state.partialTick);
                float theta = (float) Math.toRadians(skyPos[0]);
                float phi = (float) Math.toRadians(skyPos[1]);

                float cx = Mth.cos(phi) * Mth.sin(theta);
                float cy = Mth.sin(phi);
                float cz = Mth.cos(phi) * Mth.cos(theta);

                Vector3f app = toDiurnalWorldApparent(cx, cy, cz, cosX, sinX);
                if (app.y > 0.02f) {
                    double t = camRelY / app.y;
                    double surfWorldX = state.camX + app.x * t;
                    double surfWorldZ = state.camZ + app.z * t;

                    double depth = enableParallaxDepth ? 0.60 : 0.0;
                    double slopeX = (surfWorldX - state.camX) / camRelY;
                    double slopeZ = (surfWorldZ - state.camZ) / camRelY;
                    double depthX = -slopeX * depth;
                    double depthZ = -slopeZ * depth;

                    float waveX = enableLiquidRefraction ? computeFluidWaveX(surfWorldX, surfWorldZ, animTime) : 0.0f;
                    float waveZ = enableLiquidRefraction ? computeFluidWaveZ(surfWorldX, surfWorldZ, animTime) : 0.0f;

                    float localX = (float) (surfWorldX + depthX + waveX - blockWorldX);
                    float localZ = (float) (surfWorldZ + depthZ + waveZ - blockWorldZ);

                    float sz = 0.065f + planet.angularSize() * 0.004f;
                    float planetTierDimmer = switch (planet.minTier()) {
                        case 1 -> 1.0f;
                        case 2 -> 0.82f;
                        case 3 -> 0.65f;
                        case 4 -> 0.48f;
                        case 5 -> 0.35f;
                        default -> 1.0f;
                    };
                    float a = starBrightness * 0.96f * planetTierDimmer;

                    if (enable3DBillboard) {
                        render3DBillboardStar(consumer, mat, localX, y + 0.0013f, localZ, sz, minX, maxX, minZ, maxZ, state.camX - (blockWorldX + localX), camRelY, state.camZ - (blockWorldZ + localZ), 1.0f, 1.0f, 1.0f, a, light, overlay);
                    } else {
                        renderClippedQuad(consumer, mat, localX, y + 0.0013f, localZ, sz, minX, maxX, minZ, maxZ, 1.0f, 1.0f, 1.0f, a, light, overlay);
                    }
                }
            });
        }

        // ----------------------------------------------------
        // PASS 2.3: Supernova Expanding Shockwave Nebula Rings
        // ----------------------------------------------------
        List<CelestialEventHelper.ActiveSupernovaState> activeSupernovae = CelestialEventHelper.getActiveSupernovae(gameTime, state.partialTick);
        if (!activeSupernovae.isEmpty()) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(SUPERNOVA_RING_TEXTURE), (pose, consumer) -> {
                Matrix4f mat = pose.pose();
                for (CelestialEventHelper.ActiveSupernovaState sn : activeSupernovae) {
                    float theta = (float) Math.toRadians(sn.event().azim());
                    float phi = (float) Math.toRadians(sn.event().alt());
                    float cx = Mth.cos(phi) * Mth.sin(theta);
                    float cy = Mth.sin(phi);
                    float cz = Mth.cos(phi) * Mth.cos(theta);

                    Vector3f app = toDiurnalWorldApparent(cx, cy, cz, cosX, sinX);
                    if (app.y > 0.02f) {
                        double t = camRelY / app.y;
                        double surfWorldX = state.camX + app.x * t;
                        double surfWorldZ = state.camZ + app.z * t;

                        double depth = enableParallaxDepth ? 0.50 : 0.0;
                        double slopeX = (surfWorldX - state.camX) / camRelY;
                        double slopeZ = (surfWorldZ - state.camZ) / camRelY;
                        double depthX = -slopeX * depth;
                        double depthZ = -slopeZ * depth;

                        float waveX = enableLiquidRefraction ? computeFluidWaveX(surfWorldX, surfWorldZ, animTime) : 0.0f;
                        float waveZ = enableLiquidRefraction ? computeFluidWaveZ(surfWorldX, surfWorldZ, animTime) : 0.0f;

                        float localX = (float) (surfWorldX + depthX + waveX - blockWorldX);
                        float localZ = (float) (surfWorldZ + depthZ + waveZ - blockWorldZ);

                        float sz = 0.045f * sn.expandingRadiusDeg();
                        float a = starBrightness * sn.currentBrightness() * 0.70f;

                        renderClippedQuad(consumer, mat, localX, y + 0.0008f, localZ, sz, minX, maxX, minZ, maxZ, sn.event().r(), sn.event().g(), sn.event().b(), a, light, overlay);
                    }
                }
            });
        }

        // ----------------------------------------------------
        // PASS 2.5: Comet Nuclei & Active Meteor Heads (Using COMET_HEAD_TEXTURE)
        // ----------------------------------------------------
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(COMET_HEAD_TEXTURE), (pose, consumer) -> {
            Matrix4f mat = pose.pose();

            // A. Comet Nuclei & Comas
            for (CelestialEventHelper.CometDefinition comet : CelestialEventHelper.COMETS) {
                float azim = (comet.baseAzim() + (gameTime + state.partialTick) * comet.orbitalSpeed() * 0.001f * 360.0f) % 360.0f;
                float alt = comet.baseAlt();
                float azimRad = (float) Math.toRadians(azim);
                float altRad = (float) Math.toRadians(alt);

                float cx = Mth.cos(altRad) * Mth.sin(azimRad);
                float cy = Mth.sin(altRad);
                float cz = Mth.cos(altRad) * Mth.cos(azimRad);

                Vector3f app = toDiurnalWorldApparent(cx, cy, cz, cosX, sinX);

                if (app.y > 0.02f) {
                    double t = camRelY / app.y;
                    double surfWorldX = state.camX + app.x * t;
                    double surfWorldZ = state.camZ + app.z * t;

                    double depth = enableParallaxDepth ? 0.70 : 0.0;
                    double slopeX = (surfWorldX - state.camX) / camRelY;
                    double slopeZ = (surfWorldZ - state.camZ) / camRelY;
                    double depthX = -slopeX * depth;
                    double depthZ = -slopeZ * depth;

                    float waveX = enableLiquidRefraction ? computeFluidWaveX(surfWorldX, surfWorldZ, animTime) : 0.0f;
                    float waveZ = enableLiquidRefraction ? computeFluidWaveZ(surfWorldX, surfWorldZ, animTime) : 0.0f;

                    float localX = (float) (surfWorldX + depthX + waveX - blockWorldX);
                    float localZ = (float) (surfWorldZ + depthZ + waveZ - blockWorldZ);

                    float comaSz = 0.085f + comet.comaSize() * 0.006f;
                    float breathing = 0.88f + 0.12f * Mth.sin(animTime * 2.5f);
                    float a = starBrightness * breathing * 0.98f;

                    if (enable3DBillboard) {
                        render3DBillboardStar(consumer, mat, localX, y + 0.0015f, localZ, comaSz, minX, maxX, minZ, maxZ, state.camX - (blockWorldX + localX), camRelY, state.camZ - (blockWorldZ + localZ), comet.r(), comet.g(), comet.b(), a, light, overlay);
                    } else {
                        renderClippedQuad(consumer, mat, localX, y + 0.0015f, localZ, comaSz, minX, maxX, minZ, maxZ, comet.r(), comet.g(), comet.b(), a, light, overlay);
                    }
                }
            }

            // B. Active Meteor Heads
            for (CelestialEventHelper.ActiveMeteor m : activeMeteors) {
                Vector3f p = CelestialEventHelper.getMeteorPosAt(m, m.progress());
                Vector3f app = toDiurnalWorldApparent(p.x, p.y, p.z, cosX, sinX);

                if (app.y > 0.02f) {
                    double t = camRelY / app.y;
                    double surfWorldX = state.camX + app.x * t;
                    double surfWorldZ = state.camZ + app.z * t;

                    double depth = enableParallaxDepth ? 0.35 : 0.0;
                    double slopeX = (surfWorldX - state.camX) / camRelY;
                    double slopeZ = (surfWorldZ - state.camZ) / camRelY;
                    double depthX = -slopeX * depth;
                    double depthZ = -slopeZ * depth;

                    float waveX = enableLiquidRefraction ? computeFluidWaveX(surfWorldX, surfWorldZ, animTime) : 0.0f;
                    float waveZ = enableLiquidRefraction ? computeFluidWaveZ(surfWorldX, surfWorldZ, animTime) : 0.0f;

                    float localX = (float) (surfWorldX + depthX + waveX - blockWorldX);
                    float localZ = (float) (surfWorldZ + depthZ + waveZ - blockWorldZ);

                    float mSize = 0.055f + m.size() * 0.004f;
                    float a = starBrightness * m.intensity() * 0.98f;

                    if (enable3DBillboard) {
                        render3DBillboardStar(consumer, mat, localX, y + 0.0016f, localZ, mSize, minX, maxX, minZ, maxZ, state.camX - (blockWorldX + localX), camRelY, state.camZ - (blockWorldZ + localZ), m.r(), m.g(), m.b(), a, light, overlay);
                    } else {
                        renderClippedQuad(consumer, mat, localX, y + 0.0016f, localZ, mSize, minX, maxX, minZ, maxZ, m.r(), m.g(), m.b(), a, light, overlay);
                    }
                }
            }
        });

        // ----------------------------------------------------
        // PASS 3: Comet Curved Tails & Meteor Streaks (Using METEOR_TRAIL_TEXTURE)
        // ----------------------------------------------------
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(METEOR_TRAIL_TEXTURE), (pose, consumer) -> {
            Matrix4f mat = pose.pose();

            // A. Comet Curved Tails in Pool
            for (CelestialEventHelper.CometDefinition comet : CelestialEventHelper.COMETS) {
                float azim = (comet.baseAzim() + (gameTime + state.partialTick) * comet.orbitalSpeed() * 0.001f * 360.0f) % 360.0f;
                float alt = comet.baseAlt();

                int segments = 16;
                float prevX = 0, prevZ = 0;
                boolean hasPrev = false;

                float dir = (comet.orbitalSpeed() >= 0.0f) ? 1.0f : -1.0f;
                for (int seg = 0; seg <= segments; seg++) {
                    float segT = seg / (float) segments;
                    float tailAzim = azim - dir * (segT * comet.tailLengthDeg()) + dir * Mth.sin(segT * (float) Math.PI) * comet.tailCurvature();
                    float tailAlt = Mth.clamp(alt - segT * (comet.tailLengthDeg() * 0.25f), 5.0f, 88.0f);

                    float tAzimRad = (float) Math.toRadians(tailAzim);
                    float tAltRad = (float) Math.toRadians(tailAlt);

                    float cx = Mth.cos(tAltRad) * Mth.sin(tAzimRad);
                    float cy = Mth.sin(tAltRad);
                    float cz = Mth.cos(tAltRad) * Mth.cos(tAzimRad);

                    Vector3f app = toDiurnalWorldApparent(cx, cy, cz, cosX, sinX);

                    if (app.y > 0.02f) {
                        double t = camRelY / app.y;
                        double worldX = state.camX + app.x * t;
                        double worldZ = state.camZ + app.z * t;

                        double depth = enableParallaxDepth ? (0.70 - segT * 0.20) : 0.0;
                        double slopeX = (worldX - state.camX) / camRelY;
                        double slopeZ = (worldZ - state.camZ) / camRelY;

                        float waveX = enableLiquidRefraction ? computeFluidWaveX(worldX, worldZ, animTime) : 0.0f;
                        float waveZ = enableLiquidRefraction ? computeFluidWaveZ(worldX, worldZ, animTime) : 0.0f;

                        float x = (float) (worldX - slopeX * depth + waveX - blockWorldX);
                        float z = (float) (worldZ - slopeZ * depth + waveZ - blockWorldZ);

                        if (hasPrev) {
                            float tailFade = (1.0f - segT);
                            float segAlpha = starBrightness * tailFade * 0.75f;
                            float lineWidth = Math.max(0.005f, (1.0f - segT * 0.7f) * 0.020f);

                            renderLiangBarskyClippedLine(consumer, mat, prevX, prevZ, x, z, y + 0.0014f, lineWidth, minX, maxX, minZ, maxZ, comet.tailR(), comet.tailG(), comet.tailB(), segAlpha, light, overlay);
                        }

                        prevX = x;
                        prevZ = z;
                        hasPrev = true;
                    } else {
                        hasPrev = false;
                    }
                }
            }

            // B. Meteor Streaks in Pool
            for (CelestialEventHelper.ActiveMeteor m : activeMeteors) {
                int segments = 12;
                float trailLen = 0.40f;
                float tHead = m.progress();
                float tTail = Math.max(0.0f, tHead - trailLen);

                float prevX = 0, prevZ = 0;
                boolean hasPrev = false;

                for (int seg = 0; seg <= segments; seg++) {
                    float segParam = tTail + (tHead - tTail) * (seg / (float) segments);
                    Vector3f p = CelestialEventHelper.getMeteorPosAt(m, segParam);
                    Vector3f app = toDiurnalWorldApparent(p.x, p.y, p.z, cosX, sinX);

                    if (app.y > 0.02f) {
                        double t = camRelY / app.y;
                        double worldX = state.camX + app.x * t;
                        double worldZ = state.camZ + app.z * t;

                        double depth = enableParallaxDepth ? 0.35 : 0.0;
                        double slopeX = (worldX - state.camX) / camRelY;
                        double slopeZ = (worldZ - state.camZ) / camRelY;

                        float waveX = enableLiquidRefraction ? computeFluidWaveX(worldX, worldZ, animTime) : 0.0f;
                        float waveZ = enableLiquidRefraction ? computeFluidWaveZ(worldX, worldZ, animTime) : 0.0f;

                        float x = (float) (worldX - slopeX * depth + waveX - blockWorldX);
                        float z = (float) (worldZ - slopeZ * depth + waveZ - blockWorldZ);

                        if (hasPrev) {
                            float trailFade = (seg / (float) segments);
                            float segAlpha = starBrightness * m.intensity() * trailFade * 0.90f;
                            float lineWidth = 0.006f + 0.012f * trailFade;

                            renderLiangBarskyClippedLine(consumer, mat, prevX, prevZ, x, z, y + 0.0015f, lineWidth, minX, maxX, minZ, maxZ, m.r(), m.g(), m.b(), segAlpha, light, overlay);
                        }

                        prevX = x;
                        prevZ = z;
                        hasPrev = true;
                    } else {
                        hasPrev = false;
                    }
                }
            }
        });

        // ----------------------------------------------------
        // PASS 4: Charted Constellation Connection Lines (Using WHITE_TEXTURE)
        // ----------------------------------------------------
        if (enableConstellationLines) {
            Set<String> chartedEdges = PlayerAstralProgress.getChartedConnections(player);
            if (!chartedEdges.isEmpty()) {
                collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                    Matrix4f mat = pose.pose();
                    for (String edge : chartedEdges) {
                        try {
                            String[] parts = edge.split("---");
                            if (parts.length == 2) {
                                CelestialStarHelper.StarSkyPos p1 = CelestialStarHelper.getStarSkyPositionAndColor(parts[0]);
                                CelestialStarHelper.StarSkyPos p2 = CelestialStarHelper.getStarSkyPositionAndColor(parts[1]);

                                if (p1 != null && p2 != null) {
                                    float cx1 = Mth.cos(p1.altitudeRad()) * Mth.sin(p1.azimuthRad());
                                    float cy1 = Mth.sin(p1.altitudeRad());
                                    float cz1 = Mth.cos(p1.altitudeRad()) * Mth.cos(p1.azimuthRad());
                                    Vector3f app1 = toDiurnalWorldApparent(cx1, cy1, cz1, cosX, sinX);

                                    float cx2 = Mth.cos(p2.altitudeRad()) * Mth.sin(p2.azimuthRad());
                                    float cy2 = Mth.sin(p2.altitudeRad());
                                    float cz2 = Mth.cos(p2.altitudeRad()) * Mth.cos(p2.azimuthRad());
                                    Vector3f app2 = toDiurnalWorldApparent(cx2, cy2, cz2, cosX, sinX);

                                    if (app1.y > 0.02f && app2.y > 0.02f) {
                                        double t1 = camRelY / app1.y;
                                        double world1X = state.camX + app1.x * t1;
                                        double world1Z = state.camZ + app1.z * t1;

                                        double t2 = camRelY / app2.y;
                                        double world2X = state.camX + app2.x * t2;
                                        double world2Z = state.camZ + app2.z * t2;

                                        double depth = enableParallaxDepth ? 0.25 : 0.0;
                                        double slope1X = (world1X - state.camX) / camRelY;
                                        double slope1Z = (world1Z - state.camZ) / camRelY;
                                        double slope2X = (world2X - state.camX) / camRelY;
                                        double slope2Z = (world2Z - state.camZ) / camRelY;

                                        float wave1X = enableLiquidRefraction ? computeFluidWaveX(world1X, world1Z, animTime) : 0.0f;
                                        float wave1Z = enableLiquidRefraction ? computeFluidWaveZ(world1X, world1Z, animTime) : 0.0f;
                                        float wave2X = enableLiquidRefraction ? computeFluidWaveX(world2X, world2Z, animTime) : 0.0f;
                                        float wave2Z = enableLiquidRefraction ? computeFluidWaveZ(world2X, world2Z, animTime) : 0.0f;

                                        float x1 = (float) (world1X - slope1X * depth + wave1X - blockWorldX);
                                        float z1 = (float) (world1Z - slope1Z * depth + wave1Z - blockWorldZ);
                                        float x2 = (float) (world2X - slope2X * depth + wave2X - blockWorldX);
                                        float z2 = (float) (world2Z - slope2Z * depth + wave2Z - blockWorldZ);

                                        float r = (p1.r() + p2.r()) * 0.5f;
                                        float g = (p1.g() + p2.g()) * 0.5f;
                                        float b = (p1.b() + p2.b()) * 0.5f;
                                        float lineAlpha = starBrightness * 0.90f;

                                        renderLiangBarskyClippedLine(consumer, mat, x1, z1, x2, z2, y + 0.0013f, 0.012f, minX, maxX, minZ, maxZ, r, g, b, lineAlpha, light, overlay);
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                });
            }
        }
    }

    /**
     * Converts unrotated celestial sphere coordinates to the true diurnal world apparent direction vector,
     * bit-for-bit matching PoseStack: mulPose(YP(-90)).mulPose(XP(celestialAngle + 180)).
     */
    private static Vector3f toDiurnalWorldApparent(float cx, float cy, float cz, float cosX, float sinX) {
        float rxX = cx;
        float rxY = cy * cosX - cz * sinX;
        float rxZ = cy * sinX + cz * cosX;
        return new Vector3f(-rxZ, rxY, rxX);
    }

    private static float computeFluidWaveX(double wx, double wz, float animTime) {
        float p1 = animTime * 0.6f + (float) (wx * 0.4f + wz * 0.2f);
        float p2 = animTime * 0.9f - (float) (wz * 0.3f);
        return Mth.sin(p1) * 0.012f + Mth.cos(p2) * 0.006f;
    }

    private static float computeFluidWaveZ(double wx, double wz, float animTime) {
        float p1 = animTime * 0.5f + (float) (-wx * 0.2f + wz * 0.4f);
        float p2 = animTime * 0.8f + (float) (wx * 0.3f);
        return Mth.cos(p1) * 0.012f + Mth.sin(p2) * 0.006f;
    }

    private static float computeCaustic(double wx, double wz, float animTime, float starBrightness) {
        float wave = (float) (Math.sin(animTime * 0.5 + wx * 0.3 + wz * 0.2) * Math.cos(animTime * 0.4 - wx * 0.2 + wz * 0.3));
        return starBrightness * (0.016f + 0.010f * (wave * 0.5f + 0.5f));
    }

    private static void render3DBillboardStar(VertexConsumer consumer, Matrix4f mat, float px, float py, float pz, float sz, float minX, float maxX, float minZ, float maxZ, double toCamX, double toCamY, double toCamZ, float r, float g, float b, float a, int light, int overlay) {
        if (a <= 0.001f) return;

        double vLen = Math.sqrt(toCamX * toCamX + toCamY * toCamY + toCamZ * toCamZ);
        if (vLen < 1e-4) return;
        float vx = (float) (toCamX / vLen);
        float vz = (float) (toCamZ / vLen);

        float rx = -vz;
        float rz = vx;
        float rLen = (float) Math.sqrt(rx * rx + rz * rz);
        if (rLen < 1e-4f) {
            rx = 1.0f;
            rz = 0.0f;
            rLen = 1.0f;
        }
        rx /= rLen;
        rz /= rLen;

        float ux = -rz;
        float uz = rx;

        float h = sz * 0.5f;
        float x0 = px - rx * h - ux * h;
        float z0 = pz - rz * h - uz * h;
        float x1 = px - rx * h + ux * h;
        float z1 = pz - rz * h + uz * h;
        float x2 = px + rx * h + ux * h;
        float z2 = pz + rz * h + uz * h;
        float x3 = px + rx * h - ux * h;
        float z3 = pz + rz * h - uz * h;

        float bMinX = Math.min(Math.min(x0, x1), Math.min(x2, x3));
        float bMaxX = Math.max(Math.max(x0, x1), Math.max(x2, x3));
        float bMinZ = Math.min(Math.min(z0, z1), Math.min(z2, z3));
        float bMaxZ = Math.max(Math.max(z0, z1), Math.max(z2, z3));

        if (bMaxX <= minX || bMinX >= maxX || bMaxZ <= minZ || bMinZ >= maxZ) return;

        if (bMinX >= minX && bMaxX <= maxX && bMinZ >= minZ && bMaxZ <= maxZ) {
            consumer.addVertex(mat, x0, py, z0).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(mat, x1, py, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(mat, x2, py, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(mat, x3, py, z3).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        } else {
            renderClippedQuad(consumer, mat, px, py, pz, sz, minX, maxX, minZ, maxZ, r, g, b, a, light, overlay);
        }
    }

    private static void renderClippedQuad(VertexConsumer consumer, Matrix4f mat, float px, float py, float pz, float sz, float minX, float maxX, float minZ, float maxZ, float r, float g, float b, float a, int light, int overlay) {
        if (a <= 0.001f) return;
        float h = sz * 0.5f;
        float x0 = px - h;
        float x1 = px + h;
        float z0 = pz - h;
        float z1 = pz + h;

        if (x1 <= minX || x0 >= maxX || z1 <= minZ || z0 >= maxZ) return;

        float spanX = x1 - x0;
        float spanZ = z1 - z0;
        if (spanX < 1e-4f || spanZ < 1e-4f) return;

        float u0 = (x0 < minX) ? (minX - x0) / spanX : 0.0f;
        float u1 = (x1 > maxX) ? 1.0f - (x1 - maxX) / spanX : 1.0f;
        float v0 = (z0 < minZ) ? (minZ - z0) / spanZ : 0.0f;
        float v1 = (z1 > maxZ) ? 1.0f - (z1 - maxZ) / spanZ : 1.0f;

        float cx0 = Math.max(minX, x0);
        float cx1 = Math.min(maxX, x1);
        float cz0 = Math.max(minZ, z0);
        float cz1 = Math.min(maxZ, z1);

        consumer.addVertex(mat, cx0, py, cz0).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(mat, cx0, py, cz1).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(mat, cx1, py, cz1).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(mat, cx1, py, cz0).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }

    private static void renderLiangBarskyClippedLine(VertexConsumer consumer, Matrix4f mat, float x1, float z1, float x2, float z2, float y, float width, float minX, float maxX, float minZ, float maxZ, float r, float g, float b, float a, int light, int overlay) {
        float dx = x2 - x1;
        float dz = z2 - z1;

        float t0 = 0.0f;
        float t1 = 1.0f;

        float[] p = {-dx, dx, -dz, dz};
        float[] q = {x1 - minX, maxX - x1, z1 - minZ, maxZ - z1};

        for (int i = 0; i < 4; i++) {
            if (Math.abs(p[i]) < 1e-6f) {
                if (q[i] < 0.0f) return;
            } else {
                float t = q[i] / p[i];
                if (p[i] < 0.0f) {
                    if (t > t1) return;
                    if (t > t0) t0 = t;
                } else {
                    if (t < t0) return;
                    if (t < t1) t1 = t;
                }
            }
        }

        if (t0 > t1) return;

        float cx1 = x1 + t0 * dx;
        float cz1 = z1 + t0 * dz;
        float cx2 = x1 + t1 * dx;
        float cz2 = z1 + t1 * dz;

        float cdx = cx2 - cx1;
        float cdz = cz2 - cz1;
        float clen = (float) Math.sqrt(cdx * cdx + cdz * cdz);
        if (clen < 0.001f) return;

        float nx = -cdz / clen * (width * 0.5f);
        float nz = cdx / clen * (width * 0.5f);

        consumer.addVertex(mat, cx1 + nx, y, cz1 + nz).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(mat, cx1 - nx, y, cz1 - nz).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(mat, cx2 - nx, y, cz2 - nz).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(mat, cx2 + nx, y, cz2 + nz).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }
}
