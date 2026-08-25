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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
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

    public AABB getRenderBoundingBox(AstralMirrorBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(16.0, 16.0, 16.0);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(AstralMirrorBlockEntity blockEntity, Vec3 cameraPos) {
        return true;
    }

    public static class MirrorRenderState extends BlockEntityRenderState {
        public BlockPos pos = BlockPos.ZERO;
        public boolean up;
        public boolean down;
        public boolean north;
        public boolean east;
        public boolean south;
        public boolean west;
        public int ticks;
        public float partialTick;
        public double camX, camY, camZ;
        public ClientLevel level;
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
        if (be.getLevel() instanceof ClientLevel cl) {
            state.level = cl;
        }

        BlockState bs = be.getBlockState();
        state.up    = bs.hasProperty(AstralMirrorBlock.UP)    && bs.getValue(AstralMirrorBlock.UP);
        state.down  = bs.hasProperty(AstralMirrorBlock.DOWN)  && bs.getValue(AstralMirrorBlock.DOWN);
        state.north = bs.hasProperty(AstralMirrorBlock.NORTH) && bs.getValue(AstralMirrorBlock.NORTH);
        state.east  = bs.hasProperty(AstralMirrorBlock.EAST)  && bs.getValue(AstralMirrorBlock.EAST);
        state.south = bs.hasProperty(AstralMirrorBlock.SOUTH) && bs.getValue(AstralMirrorBlock.SOUTH);
        state.west  = bs.hasProperty(AstralMirrorBlock.WEST)  && bs.getValue(AstralMirrorBlock.WEST);
    }

    private static boolean isMirror(@Nullable Level level, BlockPos pos) {
        return level != null && level.getBlockState(pos).getBlock() instanceof AstralMirrorBlock;
    }

    private static class SecondaryPlane {
        final Direction dirFromF1; // Cardinal direction from primary face to this secondary mirror
        final Direction normal;    // Normal of secondary mirror face
        final double planeCoord;   // Axis coordinate of the plane

        SecondaryPlane(Direction dirFromF1, Direction normal, double planeCoord) {
            this.dirFromF1 = dirFromF1;
            this.normal = normal;
            this.planeCoord = planeCoord;
        }
    }

    private static class CornerPair {
        final SecondaryPlane p2;
        final SecondaryPlane p3;

        CornerPair(SecondaryPlane p2, SecondaryPlane p3) {
            this.p2 = p2;
            this.p3 = p3;
        }
    }

    private static class FaceContext {
        final Direction face;
        final boolean connMinU, connMaxU, connMinV, connMaxV;
        final float minU, maxU, minV, maxV;
        final float clipMinU, clipMaxU, clipMinV, clipMaxV;
        final double normalX, normalY, normalZ;
        final double planeOriginX, planeOriginY, planeOriginZ;
        final double uDirX, uDirY, uDirZ;
        final double vDirX, vDirY, vDirZ;
        final double camDist;
        final ClientLevel level;
        final List<SecondaryPlane> secondaryPlanes = new ArrayList<>();
        final List<CornerPair> cornerPlanes = new ArrayList<>();

        FaceContext(
                Direction face,
                boolean connMinU, boolean connMaxU, boolean connMinV, boolean connMaxV,
                double normalX, double normalY, double normalZ,
                double planeOriginX, double planeOriginY, double planeOriginZ,
                double uDirX, double uDirY, double uDirZ,
                double vDirX, double vDirY, double vDirZ,
                double camDist,
                BlockPos pos,
                ClientLevel level
        ) {
            this.face = face;
            this.connMinU = connMinU;
            this.connMaxU = connMaxU;
            this.connMinV = connMinV;
            this.connMaxV = connMaxV;
            this.minU = connMinU ? 0.0f : 0.125f;
            this.maxU = connMaxU ? 1.0f : 0.875f;
            this.minV = connMinV ? 0.0f : 0.125f;
            this.maxV = connMaxV ? 1.0f : 0.875f;
            this.clipMinU = connMinU ? -10.0f : 0.125f;
            this.clipMaxU = connMaxU ? 10.0f : 0.875f;
            this.clipMinV = connMinV ? -10.0f : 0.125f;
            this.clipMaxV = connMaxV ? 10.0f : 0.875f;
            this.normalX = normalX;
            this.normalY = normalY;
            this.normalZ = normalZ;
            this.planeOriginX = planeOriginX;
            this.planeOriginY = planeOriginY;
            this.planeOriginZ = planeOriginZ;
            this.uDirX = uDirX;
            this.uDirY = uDirY;
            this.uDirZ = uDirZ;
            this.vDirX = vDirX;
            this.vDirY = vDirY;
            this.vDirZ = vDirZ;
            this.camDist = camDist;
            this.level = level;

            // Discover nearby perpendicular mirror planes in the volume in front of this face
            if (level != null && pos != null) {
                Direction[] perpDirs = switch (face.getAxis()) {
                    case Y -> new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
                    case Z -> new Direction[]{Direction.UP, Direction.DOWN, Direction.WEST, Direction.EAST};
                    case X -> new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH};
                };

                for (Direction perpDir : perpDirs) {
                    Direction secNormal = perpDir.getOpposite();
                    boolean foundPlane = false;
                    for (int perpStep = 1; perpStep <= 24; perpStep++) {
                        for (int fwdStep = 0; fwdStep <= 24; fwdStep++) {
                            BlockPos checkPos = pos.relative(perpDir, perpStep).relative(face, fwdStep);
                            BlockState bs = level.getBlockState(checkPos);
                            if (bs.getBlock() instanceof AstralMirrorBlock) {
                                BlockPos inFrontPos = checkPos.relative(secNormal);
                                BlockState inFrontBs = level.getBlockState(inFrontPos);
                                if (!(inFrontBs.getBlock() instanceof AstralMirrorBlock)) {
                                    double planeCoord = switch (secNormal) {
                                        case UP    -> checkPos.getY() + 1.0;
                                        case DOWN  -> checkPos.getY();
                                        case NORTH -> checkPos.getZ();
                                        case SOUTH -> checkPos.getZ() + 1.0;
                                        case WEST  -> checkPos.getX();
                                        case EAST  -> checkPos.getX() + 1.0;
                                    };
                                    this.secondaryPlanes.add(new SecondaryPlane(perpDir, secNormal, planeCoord));
                                    foundPlane = true;
                                    break;
                                }
                            }
                        }
                        if (foundPlane) break;
                    }
                }

                // Check for 3-way inside corners (pairs of orthogonal secondary planes)
                for (int i = 0; i < this.secondaryPlanes.size(); i++) {
                    for (int j = i + 1; j < this.secondaryPlanes.size(); j++) {
                        SecondaryPlane sp1 = this.secondaryPlanes.get(i);
                        SecondaryPlane sp2 = this.secondaryPlanes.get(j);
                        if (sp1.normal.getAxis() != sp2.normal.getAxis()) {
                            this.cornerPlanes.add(new CornerPair(sp1, sp2));
                            this.cornerPlanes.add(new CornerPair(sp2, sp1));
                        }
                    }
                }
            }
        }

        boolean isOwnerOrPerimeter(double rawU, double rawV, double margin) {
            if (rawU >= 0.0 && rawU < 1.0 && rawV >= 0.0 && rawV < 1.0) {
                return true;
            }
            if (!connMinU && rawU >= minU - margin && rawU < 0.0 && rawV >= minV - margin && rawV <= maxV + margin) {
                return true;
            }
            if (!connMaxU && rawU >= 1.0 && rawU <= maxU + margin && rawV >= minV - margin && rawV <= maxV + margin) {
                return true;
            }
            if (!connMinV && rawV >= minV - margin && rawV < 0.0 && rawU >= minU - margin && rawU <= maxU + margin) {
                return true;
            }
            if (!connMaxV && rawV >= 1.0 && rawV <= maxV + margin && rawU >= minU - margin && rawU <= maxU + margin) {
                return true;
            }
            return false;
        }
    }

    @FunctionalInterface
    private interface ProjectedPointConsumer {
        void accept(float u, float v, float alphaMult, double worldX, double worldY, double worldZ, double camRelDist, int bounce);
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

        long gameTime = level.getGameTime();
        float celestialAngle = level.getTimeOfDay(state.partialTick) * 360.0f;
        float animTime = (gameTime + state.partialTick) * 0.04f;

        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        float radX = (float) Math.toRadians(celestialAngle + 180.0f);
        float cosX = Mth.cos(radX);
        float sinX = Mth.sin(radX);

        List<CelestialEventHelper.ActiveMeteor> activeMeteors = CelestialEventHelper.getActiveMeteors(gameTime, state.partialTick);
        Set<String> chartedEdges = enableConstellationLines ? PlayerAstralProgress.getChartedConnections(player) : Set.of();

        double blockWorldX = state.pos.getX();
        double blockWorldY = state.pos.getY();
        double blockWorldZ = state.pos.getZ();

        List<FaceContext> activeFaces = new ArrayList<>();

        // 1. UP Face (Top Floor)
        if (!state.up) {
            double dist = state.camY - (blockWorldY + 1.0);
            if (dist > 0.02) {
                boolean connMinU = state.west  || isMirror(level, state.pos.west().above());
                boolean connMaxU = state.east  || isMirror(level, state.pos.east().above());
                boolean connMinV = state.north || isMirror(level, state.pos.north().above());
                boolean connMaxV = state.south || isMirror(level, state.pos.south().above());
                activeFaces.add(new FaceContext(Direction.UP, connMinU, connMaxU, connMinV, connMaxV, 0, 1, 0, blockWorldX, blockWorldY + 1.0, blockWorldZ, 1, 0, 0, 0, 0, 1, dist, state.pos, level));
            }
        }

        // 2. DOWN Face (Bottom Ceiling)
        if (!state.down) {
            double dist = blockWorldY - state.camY;
            if (dist > 0.02) {
                boolean connMinU = state.west  || isMirror(level, state.pos.west().below());
                boolean connMaxU = state.east  || isMirror(level, state.pos.east().below());
                boolean connMinV = state.north || isMirror(level, state.pos.north().below());
                boolean connMaxV = state.south || isMirror(level, state.pos.south().below());
                activeFaces.add(new FaceContext(Direction.DOWN, connMinU, connMaxU, connMinV, connMaxV, 0, -1, 0, blockWorldX, blockWorldY, blockWorldZ, 1, 0, 0, 0, 0, 1, dist, state.pos, level));
            }
        }

        // 3. NORTH Face (North Wall)
        if (!state.north) {
            double dist = blockWorldZ - state.camZ;
            if (dist > 0.02) {
                boolean connMinU = state.west || isMirror(level, state.pos.west().north());
                boolean connMaxU = state.east || isMirror(level, state.pos.east().north());
                boolean connMinV = state.down || isMirror(level, state.pos.below().north());
                boolean connMaxV = state.up   || isMirror(level, state.pos.above().north());
                activeFaces.add(new FaceContext(Direction.NORTH, connMinU, connMaxU, connMinV, connMaxV, 0, 0, -1, blockWorldX, blockWorldY, blockWorldZ, 1, 0, 0, 0, 1, 0, dist, state.pos, level));
            }
        }

        // 4. SOUTH Face (South Wall)
        if (!state.south) {
            double dist = state.camZ - (blockWorldZ + 1.0);
            if (dist > 0.02) {
                boolean connMinU = state.west || isMirror(level, state.pos.west().south());
                boolean connMaxU = state.east || isMirror(level, state.pos.east().south());
                boolean connMinV = state.down || isMirror(level, state.pos.below().south());
                boolean connMaxV = state.up   || isMirror(level, state.pos.above().south());
                activeFaces.add(new FaceContext(Direction.SOUTH, connMinU, connMaxU, connMinV, connMaxV, 0, 0, 1, blockWorldX, blockWorldY, blockWorldZ + 1.0, 1, 0, 0, 0, 1, 0, dist, state.pos, level));
            }
        }

        // 5. WEST Face (West Wall)
        if (!state.west) {
            double dist = blockWorldX - state.camX;
            if (dist > 0.02) {
                boolean connMinU = state.north || isMirror(level, state.pos.north().west());
                boolean connMaxU = state.south || isMirror(level, state.pos.south().west());
                boolean connMinV = state.down  || isMirror(level, state.pos.below().west());
                boolean connMaxV = state.up    || isMirror(level, state.pos.above().west());
                activeFaces.add(new FaceContext(Direction.WEST, connMinU, connMaxU, connMinV, connMaxV, -1, 0, 0, blockWorldX, blockWorldY, blockWorldZ, 0, 0, 1, 0, 1, 0, dist, state.pos, level));
            }
        }

        // 6. EAST Face (East Wall)
        if (!state.east) {
            double dist = state.camX - (blockWorldX + 1.0);
            if (dist > 0.02) {
                boolean connMinU = state.north || isMirror(level, state.pos.north().east());
                boolean connMaxU = state.south || isMirror(level, state.pos.south().east());
                boolean connMinV = state.down  || isMirror(level, state.pos.below().east());
                boolean connMaxV = state.up    || isMirror(level, state.pos.above().east());
                activeFaces.add(new FaceContext(Direction.EAST, connMinU, connMaxU, connMinV, connMaxV, 1, 0, 0, blockWorldX + 1.0, blockWorldY, blockWorldZ, 0, 0, 1, 0, 1, 0, dist, state.pos, level));
            }
        }

        if (activeFaces.isEmpty()) {
            return;
        }

        // ----------------------------------------------------
        // PASS 0: Continuous World-Space Liquid Ether Surface Caustic Sheen
        // ----------------------------------------------------
        if (enableLiquidRefraction) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                Matrix4f mat = pose.pose();
                for (FaceContext fc : activeFaces) {
                    float c00 = computeCaustic(fc.face, blockWorldX, blockWorldY, blockWorldZ, fc.minU, fc.minV, animTime, starBrightness);
                    float c01 = computeCaustic(fc.face, blockWorldX, blockWorldY, blockWorldZ, fc.minU, fc.maxV, animTime, starBrightness);
                    float c11 = computeCaustic(fc.face, blockWorldX, blockWorldY, blockWorldZ, fc.maxU, fc.maxV, animTime, starBrightness);
                    float c10 = computeCaustic(fc.face, blockWorldX, blockWorldY, blockWorldZ, fc.maxU, fc.minV, animTime, starBrightness);

                    addFaceQuadGrad(consumer, mat, fc.face, fc.minU, fc.minV, fc.maxU, fc.maxV, 0.0002f, c00, c01, c11, c10, light, overlay);
                }
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

                        float cx = Mth.cos(alt) * Mth.sin(azim);
                        float cy = Mth.sin(alt);
                        float cz = Mth.cos(alt) * Mth.cos(azim);

                        Vector3f app = toDiurnalWorldApparent(cx, cy, cz, cosX, sinX);

                        float sz = puff.size() * 0.0085f;
                        float breathing = 0.80f + 0.20f * Mth.sin(animTime * 1.5f + puff.phase());
                        float alpha = starBrightness * puff.a() * breathing * 0.32f;

                        for (FaceContext fc : activeFaces) {
                            projectCelestialVectorMultiBounce(app, fc, state, enableParallaxDepth, 1.20, enableLiquidRefraction, animTime, Math.max(0.6, sz * 2.0),
                                    (u, v, alphaMult, wx, wy, wz, dist, bounce) -> {
                                        renderClippedQuad(consumer, mat, fc.face, u, v, sz * 2.0f, fc.clipMinU, fc.clipMaxU, fc.clipMinV, fc.clipMaxV, 0.0005f, puff.r(), puff.g(), puff.b(), alpha * alphaMult, light, overlay);
                                    }
                            );
                        }
                    }
                }
            });
        }

        // ----------------------------------------------------
        // PASS 2: Crisp Constellation & Star Points (Using STAR_TEXTURE)
        // ----------------------------------------------------
        Collection<Constellation> visibleConstellations = ModConstellations.getAllConstellations();
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
                float sz = 0.038f + s.size() * 0.008f;
                float[] rgb = CelestialStarHelper.getShiftingStarRGB(s.spectralClass(), s.essenceType(), animTime * 2.0f, s.azimuth());
                float twinkle = 0.75f + 0.25f * Mth.sin(animTime * 4.0f + s.azimuth());
                float a = starBrightness * twinkle;

                for (FaceContext fc : activeFaces) {
                    projectCelestialVectorMultiBounce(app, fc, state, enableParallaxDepth, 0.90, enableLiquidRefraction, animTime, 0.35,
                            (u, v, alphaMult, wx, wy, wz, dist, bounce) -> {
                                if (enable3DBillboard) {
                                    render3DBillboardStar(consumer, mat, fc, u, v, sz, 0.0010f, state.camX - wx, state.camY - wy, state.camZ - wz, rgb[0], rgb[1], rgb[2], a * alphaMult, light, overlay);
                                } else {
                                    renderClippedQuad(consumer, mat, fc.face, u, v, sz, fc.clipMinU, fc.clipMaxU, fc.clipMinV, fc.clipMaxV, 0.0010f, rgb[0], rgb[1], rgb[2], a * alphaMult, light, overlay);
                                }
                            }
                    );
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
                float sz = 0.055f + ls.size() * 0.010f;
                float[] rgb = CelestialStarHelper.getShiftingStarRGB(ls.spectralClass(), ls.essenceType(), animTime * 2.0f, ls.azimuth());
                float twinkle = 0.85f + 0.15f * Mth.sin(animTime * 3.0f + ls.azimuth());
                float a = starBrightness * twinkle;

                for (FaceContext fc : activeFaces) {
                    projectCelestialVectorMultiBounce(app, fc, state, enableParallaxDepth, 0.50, enableLiquidRefraction, animTime, 0.35,
                            (u, v, alphaMult, wx, wy, wz, dist, bounce) -> {
                                if (enable3DBillboard) {
                                    render3DBillboardStar(consumer, mat, fc, u, v, sz, 0.0012f, state.camX - wx, state.camY - wy, state.camZ - wz, rgb[0], rgb[1], rgb[2], a * alphaMult, light, overlay);
                                } else {
                                    renderClippedQuad(consumer, mat, fc.face, u, v, sz, fc.clipMinU, fc.clipMaxU, fc.clipMinV, fc.clipMaxV, 0.0012f, rgb[0], rgb[1], rgb[2], a * alphaMult, light, overlay);
                                }
                            }
                    );
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

                    for (FaceContext fc : activeFaces) {
                        projectCelestialVectorMultiBounce(app, fc, state, enableParallaxDepth, 0.25, enableLiquidRefraction, animTime, 0.35,
                                (u, v, alphaMult, wx, wy, wz, dist, bounce) -> {
                                    if (enable3DBillboard) {
                                        render3DBillboardStar(consumer, mat, fc, u, v, sz, 0.0014f, state.camX - wx, state.camY - wy, state.camZ - wz, rgb[0], rgb[1], rgb[2], a * alphaMult, light, overlay);
                                    } else {
                                        renderClippedQuad(consumer, mat, fc.face, u, v, sz, fc.clipMinU, fc.clipMaxU, fc.clipMinV, fc.clipMaxV, 0.0014f, rgb[0], rgb[1], rgb[2], a * alphaMult, light, overlay);
                                    }
                                }
                        );
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
                float sz = 0.080f * sn.currentBrightness();
                float a = starBrightness * sn.coreTwinkle();

                for (FaceContext fc : activeFaces) {
                    projectCelestialVectorMultiBounce(app, fc, state, enableParallaxDepth, 0.50, enableLiquidRefraction, animTime, 0.35,
                            (u, v, alphaMult, wx, wy, wz, dist, bounce) -> {
                                if (enable3DBillboard) {
                                    render3DBillboardStar(consumer, mat, fc, u, v, sz, 0.0013f, state.camX - wx, state.camY - wy, state.camZ - wz, sn.event().r(), sn.event().g(), sn.event().b(), a * alphaMult, light, overlay);
                                } else {
                                    renderClippedQuad(consumer, mat, fc.face, u, v, sz, fc.clipMinU, fc.clipMaxU, fc.clipMinV, fc.clipMaxV, 0.0013f, sn.event().r(), sn.event().g(), sn.event().b(), a * alphaMult, light, overlay);
                                }
                            }
                    );
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

                for (FaceContext fc : activeFaces) {
                    projectCelestialVectorMultiBounce(app, fc, state, enableParallaxDepth, 0.60, enableLiquidRefraction, animTime, 0.35,
                            (u, v, alphaMult, wx, wy, wz, dist, bounce) -> {
                                if (enable3DBillboard) {
                                    render3DBillboardStar(consumer, mat, fc, u, v, sz, 0.0013f, state.camX - wx, state.camY - wy, state.camZ - wz, 1.0f, 1.0f, 1.0f, a * alphaMult, light, overlay);
                                } else {
                                    renderClippedQuad(consumer, mat, fc.face, u, v, sz, fc.clipMinU, fc.clipMaxU, fc.clipMinV, fc.clipMaxV, 0.0013f, 1.0f, 1.0f, 1.0f, a * alphaMult, light, overlay);
                                }
                            }
                    );
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
                    float sz = 0.045f * sn.expandingRadiusDeg();
                    float a = starBrightness * sn.currentBrightness() * 0.70f;

                    for (FaceContext fc : activeFaces) {
                        projectCelestialVectorMultiBounce(app, fc, state, enableParallaxDepth, 0.50, enableLiquidRefraction, animTime, Math.max(0.6, sz * 1.5),
                                (u, v, alphaMult, wx, wy, wz, dist, bounce) -> {
                                    renderClippedQuad(consumer, mat, fc.face, u, v, sz, fc.clipMinU, fc.clipMaxU, fc.clipMinV, fc.clipMaxV, 0.0008f, sn.event().r(), sn.event().g(), sn.event().b(), a * alphaMult, light, overlay);
                                }
                        );
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
                float comaSz = 0.085f + comet.comaSize() * 0.006f;
                float breathing = 0.88f + 0.12f * Mth.sin(animTime * 2.5f);
                float a = starBrightness * breathing * 0.98f;

                for (FaceContext fc : activeFaces) {
                    projectCelestialVectorMultiBounce(app, fc, state, enableParallaxDepth, 0.70, enableLiquidRefraction, animTime, 0.35,
                            (u, v, alphaMult, wx, wy, wz, dist, bounce) -> {
                                if (enable3DBillboard) {
                                    render3DBillboardStar(consumer, mat, fc, u, v, comaSz, 0.0015f, state.camX - wx, state.camY - wy, state.camZ - wz, comet.r(), comet.g(), comet.b(), a * alphaMult, light, overlay);
                                } else {
                                    renderClippedQuad(consumer, mat, fc.face, u, v, comaSz, fc.clipMinU, fc.clipMaxU, fc.clipMinV, fc.clipMaxV, 0.0015f, comet.r(), comet.g(), comet.b(), a * alphaMult, light, overlay);
                                }
                            }
                    );
                }
            }

            // B. Active Meteor Heads
            for (CelestialEventHelper.ActiveMeteor m : activeMeteors) {
                Vector3f p = CelestialEventHelper.getMeteorPosAt(m, m.progress());
                Vector3f app = toDiurnalWorldApparent(p.x, p.y, p.z, cosX, sinX);
                float mSize = 0.055f + m.size() * 0.004f;
                float a = starBrightness * m.intensity() * 0.98f;

                for (FaceContext fc : activeFaces) {
                    projectCelestialVectorMultiBounce(app, fc, state, enableParallaxDepth, 0.35, enableLiquidRefraction, animTime, 0.35,
                            (u, v, alphaMult, wx, wy, wz, dist, bounce) -> {
                                if (enable3DBillboard) {
                                    render3DBillboardStar(consumer, mat, fc, u, v, mSize, 0.0016f, state.camX - wx, state.camY - wy, state.camZ - wz, m.r(), m.g(), m.b(), a * alphaMult, light, overlay);
                                } else {
                                    renderClippedQuad(consumer, mat, fc.face, u, v, mSize, fc.clipMinU, fc.clipMaxU, fc.clipMinV, fc.clipMaxV, 0.0016f, m.r(), m.g(), m.b(), a * alphaMult, light, overlay);
                                }
                            }
                    );
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
                float dir = (comet.orbitalSpeed() >= 0.0f) ? 1.0f : -1.0f;

                for (FaceContext fc : activeFaces) {
                    float[] prevU = new float[4];
                    float[] prevV = new float[4];
                    boolean[] hasPrev = new boolean[4];

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
                        double depth = enableParallaxDepth ? (0.70 - segT * 0.20) : 0.0;

                        boolean[] hitInSeg = new boolean[4];
                        projectCelestialVectorMultiBounce(app, fc, state, enableParallaxDepth, depth, enableLiquidRefraction, animTime, 200.0,
                                (u, v, alphaMult, wx, wy, wz, dist, bounce) -> {
                                    int bIdx = Math.min(3, Math.max(0, bounce));
                                    hitInSeg[bIdx] = true;
                                    if (hasPrev[bIdx]) {
                                        float tailFade = (1.0f - segT);
                                        float segAlpha = starBrightness * tailFade * 0.75f * alphaMult;
                                        float lineWidth = Math.max(0.005f, (1.0f - segT * 0.7f) * 0.020f);

                                        renderLiangBarskyClippedLine(consumer, mat, fc, prevU[bIdx], prevV[bIdx], u, v, 0.0014f, lineWidth, comet.tailR(), comet.tailG(), comet.tailB(), segAlpha, light, overlay);
                                    }
                                    prevU[bIdx] = u;
                                    prevV[bIdx] = v;
                                    hasPrev[bIdx] = true;
                                }
                        );
                        for (int b = 0; b < 4; b++) {
                            if (!hitInSeg[b]) hasPrev[b] = false;
                        }
                    }
                }
            }

            // B. Meteor Streaks in Pool
            for (CelestialEventHelper.ActiveMeteor m : activeMeteors) {
                int segments = 12;
                float trailLen = 0.40f;
                float tHead = m.progress();
                float tTail = Math.max(0.0f, tHead - trailLen);

                for (FaceContext fc : activeFaces) {
                    float[] prevU = new float[4];
                    float[] prevV = new float[4];
                    boolean[] hasPrev = new boolean[4];

                    for (int seg = 0; seg <= segments; seg++) {
                        float segParam = tTail + (tHead - tTail) * (seg / (float) segments);
                        final float trailFade = (seg / (float) segments);
                        Vector3f p = CelestialEventHelper.getMeteorPosAt(m, segParam);
                        Vector3f app = toDiurnalWorldApparent(p.x, p.y, p.z, cosX, sinX);

                        boolean[] hitInSeg = new boolean[4];
                        projectCelestialVectorMultiBounce(app, fc, state, enableParallaxDepth, 0.35, enableLiquidRefraction, animTime, 200.0,
                                (u, v, alphaMult, wx, wy, wz, dist, bounce) -> {
                                    int bIdx = Math.min(3, Math.max(0, bounce));
                                    hitInSeg[bIdx] = true;
                                    if (hasPrev[bIdx]) {
                                        float segAlpha = starBrightness * m.intensity() * trailFade * 0.90f * alphaMult;
                                        float lineWidth = 0.006f + 0.012f * trailFade;

                                        renderLiangBarskyClippedLine(consumer, mat, fc, prevU[bIdx], prevV[bIdx], u, v, 0.0015f, lineWidth, m.r(), m.g(), m.b(), segAlpha, light, overlay);
                                    }
                                    prevU[bIdx] = u;
                                    prevV[bIdx] = v;
                                    hasPrev[bIdx] = true;
                                }
                        );
                        for (int b = 0; b < 4; b++) {
                            if (!hitInSeg[b]) hasPrev[b] = false;
                        }
                    }
                }
            }
        });

        // ----------------------------------------------------
        // PASS 4: Charted Constellation Connection Lines (Using WHITE_TEXTURE)
        // ----------------------------------------------------
        if (enableConstellationLines && !chartedEdges.isEmpty()) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                Matrix4f mat = pose.pose();
                for (String edge : chartedEdges) {
                    try {
                        String[] parts = edge.split("---");
                        if (parts.length == 2) {
                            CelestialStarHelper.StarSkyPos s1 = CelestialStarHelper.getStarSkyPositionAndColor(parts[0]);
                            CelestialStarHelper.StarSkyPos s2 = CelestialStarHelper.getStarSkyPositionAndColor(parts[1]);

                            if (s1 != null && s2 != null) {
                                float cx1 = Mth.cos(s1.altitudeRad()) * Mth.sin(s1.azimuthRad());
                                float cy1 = Mth.sin(s1.altitudeRad());
                                float cz1 = Mth.cos(s1.altitudeRad()) * Mth.cos(s1.azimuthRad());
                                Vector3f app1 = toDiurnalWorldApparent(cx1, cy1, cz1, cosX, sinX);

                                float cx2 = Mth.cos(s2.altitudeRad()) * Mth.sin(s2.azimuthRad());
                                float cy2 = Mth.sin(s2.altitudeRad());
                                float cz2 = Mth.cos(s2.altitudeRad()) * Mth.cos(s2.azimuthRad());
                                Vector3f app2 = toDiurnalWorldApparent(cx2, cy2, cz2, cosX, sinX);

                                float r = (s1.r() + s2.r()) * 0.5f;
                                float g = (s1.g() + s2.g()) * 0.5f;
                                float b = (s1.b() + s2.b()) * 0.5f;
                                float lineAlpha = starBrightness * 0.90f;

                                for (FaceContext fc : activeFaces) {
                                    projectCelestialVectorMultiBounce(app1, fc, state, enableParallaxDepth, 0.25, enableLiquidRefraction, animTime, 200.0,
                                            (u1, v1, a1, wx1, wy1, wz1, d1, b1) -> {
                                                projectCelestialVectorMultiBounce(app2, fc, state, enableParallaxDepth, 0.25, enableLiquidRefraction, animTime, 200.0,
                                                        (u2, v2, a2, wx2, wy2, wz2, d2, b2) -> {
                                                            if (b1 == b2) {
                                                                renderLiangBarskyClippedLine(consumer, mat, fc, u1, v1, u2, v2, 0.0013f, 0.012f, r, g, b, lineAlpha * (a1 + a2) * 0.5f, light, overlay);
                                                            }
                                                        }
                                                );
                                            }
                                    );
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
            });
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

    private static void projectCelestialVectorMultiBounce(
            Vector3f app,
            FaceContext fc,
            MirrorRenderState state,
            boolean enableParallaxDepth,
            double depth,
            boolean enableLiquidRefraction,
            float animTime,
            double boundMargin,
            ProjectedPointConsumer consumer
    ) {
        // 1. Direct Reflection (1 Bounce: Sky -> F1 -> Cam)
        float dot1 = app.x * (float) fc.normalX + app.y * (float) fc.normalY + app.z * (float) fc.normalZ;
        if (dot1 > 0.005f) {
            float rx = app.x - 2.0f * dot1 * (float) fc.normalX;
            float ry = app.y - 2.0f * dot1 * (float) fc.normalY;
            float rz = app.z - 2.0f * dot1 * (float) fc.normalZ;

            double t = fc.camDist / dot1;
            double surfWorldX = state.camX + rx * t;
            double surfWorldY = state.camY + ry * t;
            double surfWorldZ = state.camZ + rz * t;

            double dX = surfWorldX - fc.planeOriginX;
            double dY = surfWorldY - fc.planeOriginY;
            double dZ = surfWorldZ - fc.planeOriginZ;

            double rawU = dX * fc.uDirX + dY * fc.uDirY + dZ * fc.uDirZ;
            double rawV = dX * fc.vDirX + dY * fc.vDirY + dZ * fc.vDirZ;

            boolean accept = (boundMargin > 10.0)
                    ? (rawU >= -50.0 && rawU <= 50.0 && rawV >= -50.0 && rawV <= 50.0)
                    : fc.isOwnerOrPerimeter(rawU, rawV, boundMargin);

            if (accept) {
                float waveU = 0.0f, waveV = 0.0f;
                if (enableLiquidRefraction) {
                    float wx = computeFluidWaveX(surfWorldX, surfWorldZ, animTime);
                    float wz = computeFluidWaveZ(surfWorldX, surfWorldZ, animTime);
                    waveU = (float) (wx * fc.uDirX + wz * fc.uDirZ);
                    waveV = (float) (wx * fc.vDirX + wz * fc.vDirZ);
                }
                consumer.accept((float) (rawU + waveU), (float) (rawV + waveV), 1.0f, surfWorldX, surfWorldY, surfWorldZ, fc.camDist, 1);
            }
        }

        // 2. Secondary Reflection (2 Bounces: Sky -> P2 -> F1 -> Cam)
        if (!fc.secondaryPlanes.isEmpty()) {
            for (int pIdx = 0; pIdx < fc.secondaryPlanes.size(); pIdx++) {
                SecondaryPlane p2 = fc.secondaryPlanes.get(pIdx);
                Vector3f n2 = getFaceNormal(p2.normal);
                float dot2 = app.x * n2.x + app.y * n2.y + app.z * n2.z;
                if (dot2 > 0.005f) {
                    float d1x = app.x - 2.0f * dot2 * n2.x;
                    float d1y = app.y - 2.0f * dot2 * n2.y;
                    float d1z = app.z - 2.0f * dot2 * n2.z;

                    float dotRef1 = d1x * (float) fc.normalX + d1y * (float) fc.normalY + d1z * (float) fc.normalZ;
                    if (dotRef1 > 0.005f) {
                        float rx = d1x - 2.0f * dotRef1 * (float) fc.normalX;
                        float ry = d1y - 2.0f * dotRef1 * (float) fc.normalY;
                        float rz = d1z - 2.0f * dotRef1 * (float) fc.normalZ;

                        double t1 = fc.camDist / dotRef1;
                        double s1WorldX = state.camX + rx * t1;
                        double s1WorldY = state.camY + ry * t1;
                        double s1WorldZ = state.camZ + rz * t1;

                        double dX1 = s1WorldX - fc.planeOriginX;
                        double dY1 = s1WorldY - fc.planeOriginY;
                        double dZ1 = s1WorldZ - fc.planeOriginZ;

                        double rawU = dX1 * fc.uDirX + dY1 * fc.uDirY + dZ1 * fc.uDirZ;
                        double rawV = dX1 * fc.vDirX + dY1 * fc.vDirY + dZ1 * fc.vDirZ;

                        boolean accept = (boundMargin > 10.0)
                                ? (rawU >= -50.0 && rawU <= 50.0 && rawV >= -50.0 && rawV <= 50.0)
                                : fc.isOwnerOrPerimeter(rawU, rawV, boundMargin);

                        if (accept) {
                            double distToP2 = switch (p2.normal) {
                                case UP    -> s1WorldY - p2.planeCoord;
                                case DOWN  -> p2.planeCoord - s1WorldY;
                                case NORTH -> p2.planeCoord - s1WorldZ;
                                case SOUTH -> s1WorldZ - p2.planeCoord;
                                case WEST  -> p2.planeCoord - s1WorldX;
                                case EAST  -> s1WorldX - p2.planeCoord;
                            };

                            if (distToP2 > 0.01) {
                                double t2 = distToP2 / dot2;
                                double s2WorldX = s1WorldX + d1x * t2;
                                double s2WorldY = s1WorldY + d1y * t2;
                                double s2WorldZ = s1WorldZ + d1z * t2;

                                BlockPos p2BlockPos = switch (p2.normal) {
                                    case UP    -> BlockPos.containing(s2WorldX, p2.planeCoord - 0.5, s2WorldZ);
                                    case DOWN  -> BlockPos.containing(s2WorldX, p2.planeCoord + 0.5, s2WorldZ);
                                    case NORTH -> BlockPos.containing(s2WorldX, s2WorldY, p2.planeCoord + 0.5);
                                    case SOUTH -> BlockPos.containing(s2WorldX, s2WorldY, p2.planeCoord - 0.5);
                                    case WEST  -> BlockPos.containing(p2.planeCoord + 0.5, s2WorldY, s2WorldZ);
                                    case EAST  -> BlockPos.containing(p2.planeCoord - 0.5, s2WorldY, s2WorldZ);
                                };

                                if (fc.level == null || fc.level.getBlockState(p2BlockPos).getBlock() instanceof AstralMirrorBlock) {
                                    float waveU = 0.0f, waveV = 0.0f;
                                    if (enableLiquidRefraction) {
                                        float wx = computeFluidWaveX(s1WorldX, s1WorldZ, animTime);
                                        float wz = computeFluidWaveZ(s1WorldX, s1WorldZ, animTime);
                                        waveU = (float) (wx * fc.uDirX + wz * fc.uDirZ);
                                        waveV = (float) (wx * fc.vDirX + wz * fc.vDirZ);
                                    }
                                    consumer.accept((float) (rawU + waveU), (float) (rawV + waveV), 0.88f, s1WorldX, s1WorldY, s1WorldZ, fc.camDist + t2, 100 + pIdx);
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Three-Way Corner Reflection (3 Bounces: Sky -> P3 -> P2 -> F1 -> Cam)
        if (!fc.cornerPlanes.isEmpty()) {
            for (int cIdx = 0; cIdx < fc.cornerPlanes.size(); cIdx++) {
                CornerPair cp = fc.cornerPlanes.get(cIdx);
                Vector3f n2 = getFaceNormal(cp.p2.normal);
                Vector3f n3 = getFaceNormal(cp.p3.normal);
                float dot3 = app.x * n3.x + app.y * n3.y + app.z * n3.z;

                if (dot3 > 0.005f) {
                    float d2x = app.x - 2.0f * dot3 * n3.x;
                    float d2y = app.y - 2.0f * dot3 * n3.y;
                    float d2z = app.z - 2.0f * dot3 * n3.z;

                    float dotRef2 = d2x * n2.x + d2y * n2.y + d2z * n2.z;
                    if (dotRef2 > 0.005f) {
                        float d1x = d2x - 2.0f * dotRef2 * n2.x;
                        float d1y = d2y - 2.0f * dotRef2 * n2.y;
                        float d1z = d2z - 2.0f * dotRef2 * n2.z;

                        float dotRef1 = d1x * (float) fc.normalX + d1y * (float) fc.normalY + d1z * (float) fc.normalZ;
                        if (dotRef1 > 0.005f) {
                            float rx = d1x - 2.0f * dotRef1 * (float) fc.normalX;
                            float ry = d1y - 2.0f * dotRef1 * (float) fc.normalY;
                            float rz = d1z - 2.0f * dotRef1 * (float) fc.normalZ;

                            double t1 = fc.camDist / dotRef1;
                            double s1WorldX = state.camX + rx * t1;
                            double s1WorldY = state.camY + ry * t1;
                            double s1WorldZ = state.camZ + rz * t1;

                            double dX1 = s1WorldX - fc.planeOriginX;
                            double dY1 = s1WorldY - fc.planeOriginY;
                            double dZ1 = s1WorldZ - fc.planeOriginZ;

                            double rawU = dX1 * fc.uDirX + dY1 * fc.uDirY + dZ1 * fc.uDirZ;
                            double rawV = dX1 * fc.vDirX + dY1 * fc.vDirY + dZ1 * fc.vDirZ;

                            boolean accept = (boundMargin > 10.0)
                                    ? (rawU >= -50.0 && rawU <= 50.0 && rawV >= -50.0 && rawV <= 50.0)
                                    : fc.isOwnerOrPerimeter(rawU, rawV, boundMargin);

                            if (accept) {
                                double distToP2 = switch (cp.p2.normal) {
                                    case UP    -> s1WorldY - cp.p2.planeCoord;
                                    case DOWN  -> cp.p2.planeCoord - s1WorldY;
                                    case NORTH -> cp.p2.planeCoord - s1WorldZ;
                                    case SOUTH -> s1WorldZ - cp.p2.planeCoord;
                                    case WEST  -> cp.p2.planeCoord - s1WorldX;
                                    case EAST  -> s1WorldX - cp.p2.planeCoord;
                                };

                                if (distToP2 > 0.01) {
                                    double t2 = distToP2 / dotRef2;
                                    double s2WorldX = s1WorldX + d1x * t2;
                                    double s2WorldY = s1WorldY + d1y * t2;
                                    double s2WorldZ = s1WorldZ + d1z * t2;

                                    double distToP3 = switch (cp.p3.normal) {
                                        case UP    -> s2WorldY - cp.p3.planeCoord;
                                        case DOWN  -> cp.p3.planeCoord - s2WorldY;
                                        case NORTH -> cp.p3.planeCoord - s2WorldZ;
                                        case SOUTH -> s2WorldZ - cp.p3.planeCoord;
                                        case WEST  -> cp.p3.planeCoord - s2WorldX;
                                        case EAST  -> s2WorldX - cp.p3.planeCoord;
                                    };

                                    if (distToP3 > 0.01) {
                                        double t3 = distToP3 / dot3;
                                        double s3WorldX = s2WorldX + d2x * t3;
                                        double s3WorldY = s2WorldY + d2y * t3;
                                        double s3WorldZ = s2WorldZ + d2z * t3;

                                        BlockPos p3BlockPos = switch (cp.p3.normal) {
                                            case UP    -> BlockPos.containing(s3WorldX, cp.p3.planeCoord - 0.5, s3WorldZ);
                                            case DOWN  -> BlockPos.containing(s3WorldX, cp.p3.planeCoord + 0.5, s3WorldZ);
                                            case NORTH -> BlockPos.containing(s3WorldX, s3WorldY, cp.p3.planeCoord + 0.5);
                                            case SOUTH -> BlockPos.containing(s3WorldX, s3WorldY, cp.p3.planeCoord - 0.5);
                                            case WEST  -> BlockPos.containing(cp.p3.planeCoord + 0.5, s3WorldY, s3WorldZ);
                                            case EAST  -> BlockPos.containing(cp.p3.planeCoord - 0.5, s3WorldY, s3WorldZ);
                                        };

                                        if (fc.level == null || fc.level.getBlockState(p3BlockPos).getBlock() instanceof AstralMirrorBlock) {
                                            float waveU = 0.0f, waveV = 0.0f;
                                            if (enableLiquidRefraction) {
                                                float wx = computeFluidWaveX(s1WorldX, s1WorldZ, animTime);
                                                float wz = computeFluidWaveZ(s1WorldX, s1WorldZ, animTime);
                                                waveU = (float) (wx * fc.uDirX + wz * fc.uDirZ);
                                                waveV = (float) (wx * fc.vDirX + wz * fc.vDirZ);
                                            }
                                            consumer.accept((float) (rawU + waveU), (float) (rawV + waveV), 0.77f, s1WorldX, s1WorldY, s1WorldZ, fc.camDist + t2 + t3, 200 + cIdx);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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

    private static float computeCaustic(Direction face, double bx, double by, double bz, float u, float v, float animTime, float starBrightness) {
        Vector3f mPos = getFaceModelPos(face, u, v, 0.0f);
        double wx = bx + mPos.x;
        double wy = by + mPos.y;
        double wz = bz + mPos.z;
        float wave = (float) (Math.sin(animTime * 0.5 + wx * 0.3 + wy * 0.25 + wz * 0.2) * Math.cos(animTime * 0.4 - wx * 0.2 + wy * 0.15 + wz * 0.3));
        return starBrightness * (0.016f + 0.010f * (wave * 0.5f + 0.5f));
    }

    private static Vector3f getFaceModelPos(Direction face, float u, float v, float offset) {
        return switch (face) {
            case UP    -> new Vector3f(u, 1.0f + offset, v);
            case DOWN  -> new Vector3f(u, -offset, v);
            case NORTH -> new Vector3f(u, v, -offset);
            case SOUTH -> new Vector3f(u, v, 1.0f + offset);
            case WEST  -> new Vector3f(-offset, v, u);
            case EAST  -> new Vector3f(1.0f + offset, v, u);
        };
    }

    private static Vector3f getFaceNormal(Direction face) {
        return switch (face) {
            case UP    -> new Vector3f(0, 1, 0);
            case DOWN  -> new Vector3f(0, -1, 0);
            case NORTH -> new Vector3f(0, 0, -1);
            case SOUTH -> new Vector3f(0, 0, 1);
            case WEST  -> new Vector3f(-1, 0, 0);
            case EAST  -> new Vector3f(1, 0, 0);
        };
    }

    private static void addFaceQuad(
            VertexConsumer consumer,
            Matrix4f mat,
            Direction face,
            float u0, float v0, float u1, float v1,
            float uv_u0, float uv_v0, float uv_u1, float uv_v1,
            float offset,
            float r, float g, float b, float a,
            int light, int overlay
    ) {
        if (a <= 0.001f) return;
        Vector3f norm = getFaceNormal(face);

        switch (face) {
            case UP, NORTH, EAST -> {
                Vector3f p0 = getFaceModelPos(face, u0, v0, offset);
                Vector3f p1 = getFaceModelPos(face, u0, v1, offset);
                Vector3f p2 = getFaceModelPos(face, u1, v1, offset);
                Vector3f p3 = getFaceModelPos(face, u1, v0, offset);

                consumer.addVertex(mat, p0.x, p0.y, p0.z).setColor(r, g, b, a).setUv(uv_u0, uv_v0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, p1.x, p1.y, p1.z).setColor(r, g, b, a).setUv(uv_u0, uv_v1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, p2.x, p2.y, p2.z).setColor(r, g, b, a).setUv(uv_u1, uv_v1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, p3.x, p3.y, p3.z).setColor(r, g, b, a).setUv(uv_u1, uv_v0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
            }
            case DOWN, SOUTH, WEST -> {
                Vector3f p0 = getFaceModelPos(face, u0, v0, offset);
                Vector3f p1 = getFaceModelPos(face, u1, v0, offset);
                Vector3f p2 = getFaceModelPos(face, u1, v1, offset);
                Vector3f p3 = getFaceModelPos(face, u0, v1, offset);

                consumer.addVertex(mat, p0.x, p0.y, p0.z).setColor(r, g, b, a).setUv(uv_u0, uv_v0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, p1.x, p1.y, p1.z).setColor(r, g, b, a).setUv(uv_u1, uv_v0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, p2.x, p2.y, p2.z).setColor(r, g, b, a).setUv(uv_u1, uv_v1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, p3.x, p3.y, p3.z).setColor(r, g, b, a).setUv(uv_u0, uv_v1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
            }
        }
    }

    private static void addFaceQuadGrad(
            VertexConsumer consumer,
            Matrix4f mat,
            Direction face,
            float u0, float v0, float u1, float v1,
            float offset,
            float c00, float c01, float c11, float c10,
            int light, int overlay
    ) {
        Vector3f norm = getFaceNormal(face);

        switch (face) {
            case UP, NORTH, EAST -> {
                Vector3f p0 = getFaceModelPos(face, u0, v0, offset);
                Vector3f p1 = getFaceModelPos(face, u0, v1, offset);
                Vector3f p2 = getFaceModelPos(face, u1, v1, offset);
                Vector3f p3 = getFaceModelPos(face, u1, v0, offset);

                consumer.addVertex(mat, p0.x, p0.y, p0.z).setColor(0.25f, 0.55f, 0.95f, c00).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, p1.x, p1.y, p1.z).setColor(0.20f, 0.45f, 0.85f, c01).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, p2.x, p2.y, p2.z).setColor(0.30f, 0.60f, 1.00f, c11).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, p3.x, p3.y, p3.z).setColor(0.22f, 0.50f, 0.90f, c10).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
            }
            case DOWN, SOUTH, WEST -> {
                Vector3f p0 = getFaceModelPos(face, u0, v0, offset);
                Vector3f p1 = getFaceModelPos(face, u1, v0, offset);
                Vector3f p2 = getFaceModelPos(face, u1, v1, offset);
                Vector3f p3 = getFaceModelPos(face, u0, v1, offset);

                consumer.addVertex(mat, p0.x, p0.y, p0.z).setColor(0.25f, 0.55f, 0.95f, c00).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, p1.x, p1.y, p1.z).setColor(0.22f, 0.50f, 0.90f, c10).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, p2.x, p2.y, p2.z).setColor(0.30f, 0.60f, 1.00f, c11).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, p3.x, p3.y, p3.z).setColor(0.20f, 0.45f, 0.85f, c01).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
            }
        }
    }

    private static void renderClippedQuad(
            VertexConsumer consumer,
            Matrix4f mat,
            Direction face,
            float pu, float pv, float sz,
            float clipMinU, float clipMaxU, float clipMinV, float clipMaxV,
            float offset,
            float r, float g, float b, float a,
            int light, int overlay
    ) {
        if (a <= 0.001f) return;
        float h = sz * 0.5f;
        float u0 = pu - h;
        float u1 = pu + h;
        float v0 = pv - h;
        float v1 = pv + h;

        if (u1 <= clipMinU || u0 >= clipMaxU || v1 <= clipMinV || v0 >= clipMaxV) return;

        float spanU = u1 - u0;
        float spanV = v1 - v0;
        if (spanU < 1e-4f || spanV < 1e-4f) return;

        float uv_u0 = (u0 < clipMinU) ? (clipMinU - u0) / spanU : 0.0f;
        float uv_u1 = (u1 > clipMaxU) ? 1.0f - (u1 - clipMaxU) / spanU : 1.0f;
        float uv_v0 = (v0 < clipMinV) ? (clipMinV - v0) / spanV : 0.0f;
        float uv_v1 = (v1 > clipMaxV) ? 1.0f - (v1 - clipMaxV) / spanV : 1.0f;

        float cu0 = Math.max(clipMinU, u0);
        float cu1 = Math.min(clipMaxU, u1);
        float cv0 = Math.max(clipMinV, v0);
        float cv1 = Math.min(clipMaxV, v1);

        addFaceQuad(consumer, mat, face, cu0, cv0, cu1, cv1, uv_u0, uv_v0, uv_u1, uv_v1, offset, r, g, b, a, light, overlay);
    }

    private static void render3DBillboardStar(
            VertexConsumer consumer,
            Matrix4f mat,
            FaceContext fc,
            float pu, float pv, float sz,
            float offset,
            double toCamX, double toCamY, double toCamZ,
            float r, float g, float b, float a,
            int light, int overlay
    ) {
        if (a <= 0.001f) return;

        double toCamU = toCamX * fc.uDirX + toCamY * fc.uDirY + toCamZ * fc.uDirZ;
        double toCamV = toCamX * fc.vDirX + toCamY * fc.vDirY + toCamZ * fc.vDirZ;
        double vLen = Math.sqrt(toCamU * toCamU + toCamV * toCamV);

        float rx = 1.0f;
        float rz = 0.0f;
        if (vLen > 1e-4) {
            float vx = (float) (toCamU / vLen);
            float vz = (float) (toCamV / vLen);
            rx = -vz;
            rz = vx;
            float rLen = (float) Math.sqrt(rx * rx + rz * rz);
            if (rLen > 1e-4f) {
                rx /= rLen;
                rz /= rLen;
            }
        }

        float ux = -rz;
        float uz = rx;

        float h = sz * 0.5f;
        float u0 = pu - rx * h - ux * h;
        float v0 = pv - rz * h - uz * h;
        float u1 = pu - rx * h + ux * h;
        float v1 = pv - rz * h + uz * h;
        float u2 = pu + rx * h + ux * h;
        float v2 = pv + rz * h + uz * h;
        float u3 = pu + rx * h - ux * h;
        float v3 = pv + rz * h - uz * h;

        float bMinU = Math.min(Math.min(u0, u1), Math.min(u2, u3));
        float bMaxU = Math.max(Math.max(u0, u1), Math.max(u2, u3));
        float bMinV = Math.min(Math.min(v0, v1), Math.min(v2, v3));
        float bMaxV = Math.max(Math.max(v0, v1), Math.max(v2, v3));

        if (bMaxU <= fc.clipMinU || bMinU >= fc.clipMaxU || bMaxV <= fc.clipMinV || bMinV >= fc.clipMaxV) return;

        if (bMinU >= fc.clipMinU && bMaxU <= fc.clipMaxU && bMinV >= fc.clipMinV && bMaxV <= fc.clipMaxV) {
            Vector3f norm = getFaceNormal(fc.face);
            Vector3f p0 = getFaceModelPos(fc.face, u0, v0, offset);
            Vector3f p1 = getFaceModelPos(fc.face, u1, v1, offset);
            Vector3f p2 = getFaceModelPos(fc.face, u2, v2, offset);
            Vector3f p3 = getFaceModelPos(fc.face, u3, v3, offset);

            switch (fc.face) {
                case UP, NORTH, EAST -> {
                    consumer.addVertex(mat, p0.x, p0.y, p0.z).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                    consumer.addVertex(mat, p1.x, p1.y, p1.z).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                    consumer.addVertex(mat, p2.x, p2.y, p2.z).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                    consumer.addVertex(mat, p3.x, p3.y, p3.z).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                }
                case DOWN, SOUTH, WEST -> {
                    consumer.addVertex(mat, p0.x, p0.y, p0.z).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                    consumer.addVertex(mat, p3.x, p3.y, p3.z).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                    consumer.addVertex(mat, p2.x, p2.y, p2.z).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                    consumer.addVertex(mat, p1.x, p1.y, p1.z).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                }
            }
        } else {
            renderClippedQuad(consumer, mat, fc.face, pu, pv, sz, fc.clipMinU, fc.clipMaxU, fc.clipMinV, fc.clipMaxV, offset, r, g, b, a, light, overlay);
        }
    }

    private static void renderLiangBarskyClippedLine(
            VertexConsumer consumer,
            Matrix4f mat,
            FaceContext fc,
            float u1, float v1, float u2, float v2,
            float offset,
            float width,
            float r, float g, float b, float a,
            int light, int overlay
    ) {
        float du = u2 - u1;
        float dv = v2 - v1;

        float t0 = 0.0f;
        float t1 = 1.0f;

        float[] p = {-du, du, -dv, dv};
        float[] q = {u1 - fc.minU, fc.maxU - u1, v1 - fc.minV, fc.maxV - v1};

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

        float cu1 = u1 + t0 * du;
        float cv1 = v1 + t0 * dv;
        float cu2 = u1 + t1 * du;
        float cv2 = v1 + t1 * dv;

        float cdu = cu2 - cu1;
        float cdv = cv2 - cv1;
        float clen = (float) Math.sqrt(cdu * cdu + cdv * cdv);
        if (clen < 0.0005f) return;

        // Slight micro-extension along line direction to ensure perfectly seamless joins across block borders
        float extU = (cdu / clen) * 0.002f;
        float extV = (cdv / clen) * 0.002f;
        cu1 -= extU;
        cv1 -= extV;
        cu2 += extU;
        cv2 += extV;

        float nu = -cdv / clen * (width * 0.5f);
        float nv = cdu / clen * (width * 0.5f);

        Vector3f norm = getFaceNormal(fc.face);
        Vector3f pt0 = getFaceModelPos(fc.face, cu1 + nu, cv1 + nv, offset);
        Vector3f pt1 = getFaceModelPos(fc.face, cu1 - nu, cv1 - nv, offset);
        Vector3f pt2 = getFaceModelPos(fc.face, cu2 - nu, cv2 - nv, offset);
        Vector3f pt3 = getFaceModelPos(fc.face, cu2 + nu, cv2 + nv, offset);

        switch (fc.face) {
            case UP, NORTH, EAST -> {
                consumer.addVertex(mat, pt0.x, pt0.y, pt0.z).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, pt1.x, pt1.y, pt1.z).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, pt2.x, pt2.y, pt2.z).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, pt3.x, pt3.y, pt3.z).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
            }
            case DOWN, SOUTH, WEST -> {
                consumer.addVertex(mat, pt0.x, pt0.y, pt0.z).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, pt3.x, pt3.y, pt3.z).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, pt2.x, pt2.y, pt2.z).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
                consumer.addVertex(mat, pt1.x, pt1.y, pt1.z).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(norm.x, norm.y, norm.z);
            }
        }
    }
}
