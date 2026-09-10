package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ddraig.net.entropica.entity.projectile.SingularityGrenadeEntity;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Dedicated EntityRenderer for SingularityGrenadeEntity.
 * In flight: renders the thrown grenade item.
 * When anchored / vortex active: renders the genuine 3D astrophysical black hole
 * with dynamic growth, swirling pulsation, and implosive collapse.
 */
public class SingularityGrenadeRenderer extends EntityRenderer<SingularityGrenadeEntity, SingularityGrenadeRenderer.SingularityGrenadeRenderState> {

    private static final Int2IntMap ANCHOR_START_TICKS = new Int2IntOpenHashMap();
    private static final int FULL_LIGHT = 15728880;

    private final ItemModelResolver itemModelResolver;

    public SingularityGrenadeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    public static class SingularityGrenadeRenderState extends EntityRenderState {
        public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
        public boolean isAnchored;
        public float activeTicks;
        public float scale;
        public float ageTicks;
    }

    @Override
    public SingularityGrenadeRenderState createRenderState() {
        return new SingularityGrenadeRenderState();
    }

    @Override
    public void extractRenderState(SingularityGrenadeEntity entity, SingularityGrenadeRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageTicks = entity.tickCount + partialTick;

        // Clean up tracking map if entity is removed or map grows too large
        if (ANCHOR_START_TICKS.size() > 64) {
            ANCHOR_START_TICKS.clear();
        }

        // An entity is anchored if gravity is disabled or linear velocity is zeroed out
        boolean anchored = entity.isNoGravity() || entity.getDeltaMovement().lengthSqr() < 1e-4;
        state.isAnchored = anchored;

        if (anchored) {
            if (!ANCHOR_START_TICKS.containsKey(entity.getId())) {
                ANCHOR_START_TICKS.put(entity.getId(), entity.tickCount);
            }
            int startTick = ANCHOR_START_TICKS.get(entity.getId());
            float active = Math.max(0.0F, (entity.tickCount - startTick) + partialTick);
            state.activeTicks = active;

            // Dynamic growth, pulsation, and implosive collapse (80-tick lifecycle)
            if (active < 12.0F) {
                // Growth phase (0 - 12 ticks): rapid gravitational emergence
                float t = active / 12.0F;
                state.scale = (float) Math.sin(t * (Math.PI / 2.0));
            } else if (active < 70.0F) {
                // Swirling stable pulsation phase (12 - 70 ticks)
                state.scale = 1.0F + 0.05F * Mth.sin(active * 0.35F);
            } else if (active <= 85.0F) {
                // Implosive collapse phase (70 - 85 ticks): sudden violent pinch into singularity
                float t = Mth.clamp((active - 70.0F) / 10.0F, 0.0F, 1.0F);
                float remaining = 1.0F - t;
                state.scale = remaining * remaining;
            } else {
                state.scale = 0.0F;
            }
        } else {
            ANCHOR_START_TICKS.remove(entity.getId());
            state.activeTicks = 0.0F;
            state.scale = 0.0F;

            // In-flight: update thrown item model
            this.itemModelResolver.updateForTopItem(
                state.itemRenderState,
                entity.getItem(),
                ItemDisplayContext.GROUND,
                entity.level(),
                null,
                entity.getId()
            );
        }
    }

    @Override
    public void submit(SingularityGrenadeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.isAnchored) {
            if (state.scale > 0.002F) {
                poseStack.pushPose();
                poseStack.translate(0.0, 0.25, 0.0);
                poseStack.mulPose(Axis.YP.rotationDegrees(state.activeTicks * 4.0F));

                float radius = 0.65F * state.scale;
                GravitationalLensingRenderer.renderBlackHole(
                    poseStack,
                    collector,
                    radius,
                    state.activeTicks,
                    FULL_LIGHT,
                    OverlayTexture.NO_OVERLAY
                );

                poseStack.popPose();
            }
        } else {
            // Render flying thrown item oriented towards camera
            poseStack.pushPose();
            poseStack.mulPose(cameraState.orientation);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            state.itemRenderState.submit(poseStack, collector, FULL_LIGHT, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
