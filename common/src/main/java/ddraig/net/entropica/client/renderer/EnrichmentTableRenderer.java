package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.EnrichmentTableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EnrichmentTableRenderer implements BlockEntityRenderer<EnrichmentTableBlockEntity, EnrichmentTableRenderer.EnrichmentTableRenderState> {

    private final ItemModelResolver itemModelResolver;

    public EnrichmentTableRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public static class EnrichmentTableRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState itemState = new ItemStackRenderState();
        public float glowIntensity;
        public float renderTime;
        public boolean isEmpty;
    }

    @Override
    public EnrichmentTableRenderState createRenderState() {
        return new EnrichmentTableRenderState();
    }

    @Override
    public void extractRenderState(EnrichmentTableBlockEntity blockEntity, EnrichmentTableRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);

        state.glowIntensity = blockEntity.getGlowIntensity();
        state.renderTime = (blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0) + partialTick;

        ItemStack stack = blockEntity.getHeldItem();
        state.isEmpty = stack.isEmpty();

        if (!stack.isEmpty()) {
            this.itemModelResolver.updateForNonLiving(
                    state.itemState,
                    stack,
                    ItemDisplayContext.FIXED,
                    Minecraft.getInstance().player
            );
        } else {
            state.itemState.clear();
        }
    }

    @Override
    public void submit(EnrichmentTableRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState cameraState) {
        if (state.isEmpty) return;

        poseStack.pushPose();

        poseStack.translate(0.502D, 1.4D, 0.502D);

        float bob = (float) Math.sin(state.renderTime / 10.0F) * 0.05F;
        poseStack.translate(0.0D, bob, 0.0D);

        float rotation = (state.renderTime *  1.3f) % 360.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        poseStack.scale(0.5F, 0.5F, 0.5F);

        int lightToUse;
        if (state.glowIntensity > 0) {
            int blockLight = (int) (15 * state.glowIntensity);
            lightToUse = LightTexture.pack(blockLight, blockLight);
        } else {
            lightToUse = LightTexture.FULL_BRIGHT;
        }

        state.itemState.submit(poseStack, collector, lightToUse, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}