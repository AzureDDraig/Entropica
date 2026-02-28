package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.EssenceOrbEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * The EssenceOrbRenderer class is responsible for rendering the EssenceOrbEntity in the game world.
 * It utilizes various rendering techniques to display the orb with its base texture, color, size, and
 * additional flare effects, dependent on its tier and type. The renderer is specifically designed
 * to provide a dynamic and visually appealing representation of the entity.
 */
public class EssenceOrbRenderer extends EntityRenderer<EssenceOrbEntity, EssenceOrbRenderer.EssenceOrbRenderState> {
    private static final ResourceLocation ORB_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/orb_base.png");
    private static final ResourceLocation DEFAULT_FLARE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/orb_flare.png");

    public EssenceOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static ResourceLocation getFlareTexture(EssenceType type) {
        return switch (type) {
            case AIR -> ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/flare_air.png");
            case ARID -> ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/flare_arid.png");
            case CHIMERA -> ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/flare_chimera.png"); // Added Chimera!
            case EARTH -> ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/flare_earth.png");
            case FROZEN -> ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/flare_frozen.png");
            case LIGHTNING -> ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/flare_lightning.png");
            case NATURE -> ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/flare_nature.png");
            case NETHER -> ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/flare_nether.png");
            case RADIANT -> ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/flare_radiant.png");
            case UMBRAL -> ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/flare_umbral.png");
            case UNDEAD -> ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/flare_undead.png");
            case VOID -> ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/flare_void.png");
            case WATER -> ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/flare_water.png");
            default -> DEFAULT_FLARE;
        };
    }

    @Override
    public EssenceOrbRenderState createRenderState() {
        return new EssenceOrbRenderState();
    }

    @Override
    public void extractRenderState(EssenceOrbEntity entity, EssenceOrbRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.tier = entity.getTier();
        state.essenceType = entity.getEssenceType();
        state.time = entity.tickCount + partialTick;
        state.randomOffset = entity.getId() * 13.37f;
        state.flareTexture = getFlareTexture(state.essenceType);
    }

    @Override
    public void submit(EssenceOrbRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.125F, 0.0F);
        poseStack.mulPose(cameraState.orientation);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        int tier = state.tier;
        EssenceType type = state.essenceType;

        float rRaw = type.getR() / 255.0F;
        float gRaw = type.getG() / 255.0F;
        float bRaw = type.getB() / 255.0F;

        if (type.name().equals("CHIMERA")) {
            float speed = 0.05f;
            float cycle = (state.time + state.randomOffset) * speed;
            rRaw = (float) (Math.sin(cycle) * 0.5f + 0.5f);
            gRaw = (float) (Math.sin(cycle + 2.094f) * 0.5f + 0.5f); // Offset by 120 degrees
            bRaw = (float) (Math.sin(cycle + 4.188f) * 0.5f + 0.5f); // Offset by 240 degrees
        }

        float maxChannel = Math.max(rRaw, Math.max(gRaw, bRaw));
        final float r = (maxChannel > 0) ? rRaw / maxChannel : 0;
        final float g = (maxChannel > 0) ? gRaw / maxChannel : 0;
        final float b = (maxChannel > 0) ? bRaw / maxChannel : 0;

        float baseScale = 0.2f + (tier * 0.2f);
        poseStack.scale(baseScale, baseScale, baseScale);
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(ORB_TEXTURE), (pose, consumer) -> {
            Matrix4f matrix4f = pose.pose();
            drawQuad(consumer, matrix4f, r, g, b, 1.0f);
        });

        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.translate(0.0f, 0.0f, -0.001f);
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(ORB_TEXTURE), (pose, consumer) -> {
            Matrix4f matrix4f = pose.pose();
            drawQuad(consumer, matrix4f, 1.0f, 1.0f, 1.0f, 0.9f);
        });
        poseStack.popPose();

        if (tier > 1) {
            float pulseThreshold = (tier == 3) ? 0.2f : 0.8f;
            float flareAlpha = (float) Math.sin((state.time + state.randomOffset) * 0.1f);

            if (flareAlpha > pulseThreshold) {
                flareAlpha = (flareAlpha - pulseThreshold) / (1.0f - pulseThreshold);

                poseStack.pushPose();
                poseStack.translate(0.0f, 0.0f, -0.002f);
                poseStack.mulPose(Axis.ZP.rotationDegrees(state.time * 2.0f));
                poseStack.scale(1.5f, 1.5f, 1.5f);

                final float finalFlareAlpha = flareAlpha;

                collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(state.flareTexture), (pose, consumer) -> {
                    Matrix4f flareMatrix = pose.pose();
                    drawQuad(consumer, flareMatrix, r, g, b, finalFlareAlpha);
                });

                poseStack.popPose();
            }
        }

        poseStack.popPose();
        super.submit(state, poseStack, collector, cameraState);
    }

    private void drawQuad(VertexConsumer consumer, Matrix4f matrix, float r, float g, float b, float alpha) {
        consumer.addVertex(matrix, -0.5F, -0.5F, 0.0F).setColor(r, g, b, alpha).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(255).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, 0.5F, -0.5F, 0.0F).setColor(r, g, b, alpha).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(255).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, 0.5F, 0.5F, 0.0F).setColor(r, g, b, alpha).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(255).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, -0.5F, 0.5F, 0.0F).setColor(r, g, b, alpha).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(255).setNormal(0.0F, 1.0F, 0.0F);
    }

    public static class EssenceOrbRenderState extends EntityRenderState {
        public int tier;
        public EssenceType essenceType;
        public float time;
        public float randomOffset;
        public ResourceLocation flareTexture;
    }
}