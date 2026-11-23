package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.SoulfirePhenexModel;
import ddraig.net.entropica.entity.soulfire_phenex.SoulfirePhenexEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class SoulfirePhenexRenderer extends MobRenderer<SoulfirePhenexEntity, SoulfirePhenexRenderState, SoulfirePhenexModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/soulfire_phenex/soulfire_phenex.png"
    );
    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_LIGHT = 15728880;
    private static final int NO_OVERLAY = OverlayTexture.NO_OVERLAY;

    public SoulfirePhenexRenderer(EntityRendererProvider.Context context) {
        super(context, new SoulfirePhenexModel(context.bakeLayer(SoulfirePhenexModel.LAYER_LOCATION)), 0.55F);
        this.addLayer(new SoulfireRibcageWispLayer(this));
    }

    @Override
    public SoulfirePhenexRenderState createRenderState() {
        return new SoulfirePhenexRenderState();
    }

    @Override
    public void extractRenderState(SoulfirePhenexEntity entity, SoulfirePhenexRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.flyAnimationState.copyFrom(entity.flyAnimationState);
        state.takeoffAnimationState.copyFrom(entity.takeoffAnimationState);
        state.landingAnimationState.copyFrom(entity.landingAnimationState);
        state.flyAttackAnimationState.copyFrom(entity.flyAttackAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.flightMode = entity.getFlightMode();
        state.isSoulStormBoosted = entity.isSoulStormBoosted();
        state.isBreathingSoulfire = entity.isBreathingSoulfire();
        state.wispDeathTicks = entity.isAlive() ? 0.0F : (entity.deathTime + partialTick);
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(SoulfirePhenexEntity entity, BlockPos pos) {
        int soulGlow;
        if (!entity.isAlive()) {
            // Flares brightly once (ticks 1..8) then gutters out completely to 0 inside the collapsing ribcage!
            if (entity.deathTime <= 8) {
                soulGlow = 15;
            } else if (entity.deathTime <= 26) {
                soulGlow = Math.max(0, 12 - (entity.deathTime - 8));
            } else {
                soulGlow = 0;
            }
        } else if (entity.isBreathingSoulfire() || entity.isSoulStormBoosted()) {
            soulGlow = 14;
        } else {
            soulGlow = 11;
        }
        return Math.max(soulGlow, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(SoulfirePhenexRenderState state) {
        return TEXTURE;
    }

    /**
     * Renders the blazing cyan-blue soulfire wisp inside the Phenex's hollow 360-degree ribcage
     * (and glowing throat flames during soulfire breath), flaring once and guttering out to zero upon death.
     */
    private static final class SoulfireRibcageWispLayer extends RenderLayer<SoulfirePhenexRenderState, SoulfirePhenexModel> {

        SoulfireRibcageWispLayer(RenderLayerParent<SoulfirePhenexRenderState, SoulfirePhenexModel> parent) {
            super(parent);
        }

        @Override
        public void submit(
                PoseStack poseStack,
                SubmitNodeCollector collector,
                int packedLight,
                SoulfirePhenexRenderState state,
                float yRot,
                float xRot
        ) {
            float wispScale;
            float wispAlpha;
            if (state.wispDeathTicks > 0.0F) {
                if (state.wispDeathTicks >= 27.0F) {
                    return; // Completely guttered out inside the collapsed bleached ribcage!
                }
                if (state.wispDeathTicks <= 7.0F) {
                    // Sudden brilliant death flare!
                    float u = state.wispDeathTicks / 7.0F;
                    wispScale = 1.0F + 0.68F * Mth.sin(u * ((float) Math.PI * 0.5F));
                    wispAlpha = 1.0F;
                } else {
                    // Gutters out completely from tick 7 -> tick 27
                    float u = (state.wispDeathTicks - 7.0F) / 20.0F;
                    wispScale = Math.max(0.0F, (1.0F - u) * 1.68F * (1.0F - 0.35F * u));
                    wispAlpha = Math.max(0.0F, 1.0F - u);
                }
            } else {
                float flicker = 0.94F + 0.14F * Mth.sin(state.ageInTicks * 0.42F)
                        + 0.06F * Mth.cos(state.ageInTicks * 0.89F);
                float surge = (state.isBreathingSoulfire || state.takeoffAnimationState.isStarted())
                        ? 1.32F
                        : (state.isSoulStormBoosted ? 1.16F : 1.0F);
                wispScale = flicker * surge;
                wispAlpha = 0.94F;
            }

            if (wispScale <= 0.01F || wispAlpha <= 0.01F) {
                return;
            }

            final float finalScale = wispScale;
            final float finalAlpha = wispAlpha;
            final float age = state.ageInTicks;

            // 1. Hollow Ribcage Blazing Soulfire Wisp
            poseStack.pushPose();
            this.getParentModel().translateToRibcageCavity(poseStack);
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                Matrix4f mat = pose.pose();
                // Incandescent white-cyan inner flame heart (#CFFAFE)
                renderFlameBipyramid(mat, vc, 0.075F * finalScale, 0.155F * finalScale, 0.115F * finalScale,
                        age * 0.22F, 0.812F, 0.980F, 0.996F, finalAlpha);
                // Mid cyan-blue soulfire tongue (#38BDF8)
                renderFlameBipyramid(mat, vc, 0.118F * finalScale, 0.235F * finalScale, 0.155F * finalScale,
                        -age * 0.31F, 0.220F, 0.741F, 0.973F, finalAlpha * 0.78F);
                // Outer deep soulfire aura (#06B6D4)
                renderFlameBipyramid(mat, vc, 0.152F * finalScale, 0.295F * finalScale, 0.185F * finalScale,
                        age * 0.15F + 0.6F, 0.024F, 0.714F, 0.831F, finalAlpha * 0.50F);
            });
            poseStack.popPose();

            // 2. Open Throat/Beak Soulfire Flame Core while breathing soulfire (or flaring at death onset)
            if (state.isBreathingSoulfire || (state.wispDeathTicks > 0.0F && state.wispDeathTicks <= 9.0F)) {
                final float throatScale = state.isBreathingSoulfire ? (0.85F * finalScale) : (0.55F * finalScale);
                poseStack.pushPose();
                this.getParentModel().translateToThroatMaw(poseStack);
                collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                    Matrix4f mat = pose.pose();
                    renderFlameBipyramid(mat, vc, 0.055F * throatScale, 0.11F * throatScale, 0.10F * throatScale,
                            age * 0.45F, 0.812F, 0.980F, 0.996F, finalAlpha * 0.92F);
                    renderFlameBipyramid(mat, vc, 0.090F * throatScale, 0.16F * throatScale, 0.14F * throatScale,
                            -age * 0.35F, 0.024F, 0.714F, 0.831F, finalAlpha * 0.65F);
                });
                poseStack.popPose();
            }
        }

        private static void renderFlameBipyramid(
                Matrix4f mat,
                VertexConsumer vc,
                float radius,
                float topExtend,
                float bottomExtend,
                float spinAngle,
                float r, float g, float b, float a
        ) {
            // Note: in EntityModel local coordinates +Y points downward, so upward flame apex is -topExtend!
            float topY = -topExtend;
            float botY = bottomExtend;
            int sides = 6;
            for (int i = 0; i < sides; i++) {
                float a0 = spinAngle + (float) (Math.PI * 2.0D * i / sides);
                float a1 = spinAngle + (float) (Math.PI * 2.0D * (i + 1) / sides);
                float x0 = Mth.cos(a0) * radius;
                float z0 = Mth.sin(a0) * radius;
                float x1 = Mth.cos(a1) * radius;
                float z1 = Mth.sin(a1) * radius;

                // Upper flame facet (equator -> upward tip)
                emitTriAsQuad(mat, vc, x0, 0.0F, z0, x1, 0.0F, z1, 0.0F, topY, 0.0F, r, g, b, a);
                // Lower flame facet (equator -> base tip)
                emitTriAsQuad(mat, vc, x1, 0.0F, z1, x0, 0.0F, z0, 0.0F, botY, 0.0F, r, g, b, a * 0.88F);
            }
        }

        private static void emitTriAsQuad(
                Matrix4f mat,
                VertexConsumer vc,
                float x0, float y0, float z0,
                float x1, float y1, float z1,
                float x2, float y2, float z2,
                float r, float g, float b, float a
        ) {
            vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
            vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setUv(1.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
            vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a).setUv(0.5F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
            vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a).setUv(0.5F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);

            vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a).setUv(0.5F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
            vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setUv(1.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
            vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
            vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        }
    }
}
