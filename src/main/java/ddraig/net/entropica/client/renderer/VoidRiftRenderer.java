package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.VoidRiftBlock;
import ddraig.net.entropica.block.entity.VoidRiftBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class VoidRiftRenderer implements BlockEntityRenderer<VoidRiftBlockEntity, VoidRiftRenderer.VoidRiftRenderState> {

    private final ItemModelResolver itemModelResolver;

    public VoidRiftRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public static class VoidRiftRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
        public boolean hasItem = false;
        public float progress = 0;
        public boolean isInput = true;
    }

    @Override
    public VoidRiftRenderState createRenderState() {
        return new VoidRiftRenderState();
    }

    @Override
    public void extractRenderState(VoidRiftBlockEntity be, VoidRiftRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);

        state.hasItem = !be.getProcessingItem().isEmpty();
        if (state.hasItem && be.getLevel() != null) {
            // Safely resolves the item model using the standard top item resolver
            this.itemModelResolver.updateForTopItem(state.itemRenderState, be.getProcessingItem(), ItemDisplayContext.GROUND, be.getLevel(), null, 0);
        }

        state.progress = (be.getProcessTick() + partialTick) / (float) VoidRiftBlockEntity.MAX_PROCESS_TICKS;
        if (state.progress > 1.0f) state.progress = 1.0f;

        state.isInput = be.getBlockState().getValue(VoidRiftBlock.IS_INPUT);

        // Spawn ambient portal particles dynamically
        if (be.getLevel() != null && be.getLevel().random.nextInt(3) == 0) {
            double px = be.getBlockPos().getX() + 0.5 + (be.getLevel().random.nextDouble() - 0.5) * 0.5;
            double py = be.getBlockPos().getY() + 0.5 + (be.getLevel().random.nextDouble() - 0.5) * 0.5;
            double pz = be.getBlockPos().getZ() + 0.5 + (be.getLevel().random.nextDouble() - 0.5) * 0.5;

            if (state.isInput) {
                be.getLevel().addParticle(ParticleTypes.PORTAL, px, py, pz, 0, 0, 0);
            } else {
                be.getLevel().addParticle(ParticleTypes.REVERSE_PORTAL, px, py, pz, 0, 0, 0);
            }
        }
    }

    @Override
    public void submit(VoidRiftRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        if (!state.hasItem) return;

        float scale = state.isInput ? (1.0f - state.progress) : state.progress;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        // Wild, chaotic rotation on non-Y axes to simulate being sucked into a singularity
        poseStack.mulPose(Axis.XP.rotationDegrees(state.progress * 720f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.progress * 1080f));

        poseStack.scale(scale, scale, scale);

        // Render the resolved item safely!
        int light = 15728880;
        state.itemRenderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}