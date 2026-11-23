package ddraig.net.entropica.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.projectile.BloodFrostDartEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Velocity-aligned 3D crossed-quad renderer for BloodFrostDartEntity.
 * Samples the 2x5 dart region at [28..30, 27..32) on sanguine_sea_angel.png (32x32)
 * using RenderType.entityCutoutNoCull at fullbright (15728880).
 */
public class BloodFrostDartRenderer extends EntityRenderer<BloodFrostDartEntity, BloodFrostDartRenderer.BloodFrostDartRenderState> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/sanguine_sea_angel/sanguine_sea_angel.png"
    );
    private static final int FULL_LIGHT = 15728880;
    private static final float U0 = 28.0F / 32.0F;
    private static final float U1 = 30.0F / 32.0F;
    private static final float V0 = 27.0F / 32.0F; // Crystalline tip (+Z forward)
    private static final float V1 = 32.0F / 32.0F; // Crimson tail (-Z trailing)

    public static class BloodFrostDartRenderState extends EntityRenderState {
        public float ageTicks;
        public float halfHeight = 0.125F;
        public Vec3 velocity = new Vec3(0.0D, 0.0D, 1.0D);
    }

    public BloodFrostDartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(BloodFrostDartEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public BloodFrostDartRenderState createRenderState() {
        return new BloodFrostDartRenderState();
    }

    @Override
    public void extractRenderState(BloodFrostDartEntity entity, BloodFrostDartRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageTicks = entity.tickCount + partialTick;
        state.halfHeight = entity.getBbHeight() * 0.5F;
        Vec3 vel = entity.getDeltaMovement();
        state.velocity = vel.lengthSqr() > 1.0E-5D
                ? vel
                : Vec3.directionFromRotation(entity.getXRot(), entity.getYRot());
    }

    @Override
    public void submit(BloodFrostDartRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0.0F, state.halfHeight, 0.0F);

        Vec3 fwd = state.velocity.normalize();
        float yawRad = (float) Mth.atan2(fwd.x, fwd.z);
        float pitchRad = (float) -Math.asin(Mth.clamp(fwd.y, -1.0D, 1.0D));

        poseStack.mulPose(Axis.YP.rotation(yawRad));
        poseStack.mulPose(Axis.XP.rotation(pitchRad));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.ageTicks * 16.0F));

        collector.submitCustomGeometry(poseStack, RenderType.entityCutoutNoCull(TEXTURE), (pose, vc) -> {
            float halfW = 0.095F;
            float halfL = 0.24F;

            // Horizontal X-Z crossed quad
            emitVertex(pose, vc, -halfW, 0.0F, -halfL, U0, V1, 0.0F, 1.0F, 0.0F);
            emitVertex(pose, vc,  halfW, 0.0F, -halfL, U1, V1, 0.0F, 1.0F, 0.0F);
            emitVertex(pose, vc,  halfW, 0.0F,  halfL, U1, V0, 0.0F, 1.0F, 0.0F);
            emitVertex(pose, vc, -halfW, 0.0F,  halfL, U0, V0, 0.0F, 1.0F, 0.0F);

            // Vertical Y-Z perpendicular crossed quad
            emitVertex(pose, vc, 0.0F, -halfW, -halfL, U0, V1, 1.0F, 0.0F, 0.0F);
            emitVertex(pose, vc, 0.0F,  halfW, -halfL, U1, V1, 1.0F, 0.0F, 0.0F);
            emitVertex(pose, vc, 0.0F,  halfW,  halfL, U1, V0, 1.0F, 0.0F, 0.0F);
            emitVertex(pose, vc, 0.0F, -halfW,  halfL, U0, V0, 1.0F, 0.0F, 0.0F);
        });

        poseStack.popPose();
        super.submit(state, poseStack, collector, cameraState);
    }

    private static void emitVertex(
            PoseStack.Pose pose,
            VertexConsumer vc,
            float x,
            float y,
            float z,
            float u,
            float v,
            float nx,
            float ny,
            float nz
    ) {
        vc.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_LIGHT)
                .setNormal(pose, nx, ny, nz);
    }
}
