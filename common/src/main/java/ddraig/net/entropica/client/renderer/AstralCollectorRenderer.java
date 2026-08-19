package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ddraig.net.entropica.block.entity.AstralCollectorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AstralCollectorRenderer implements BlockEntityRenderer<AstralCollectorBlockEntity, AstralCollectorRenderer.CollectorRenderState> {

    public AstralCollectorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public CollectorRenderState createRenderState() {
        return new CollectorRenderState();
    }

    @Override
    public void extractRenderState(AstralCollectorBlockEntity be, CollectorRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
    }

    @Override
    public void submit(CollectorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        // Astral Collector uses its 3D block model; no fluid level rendered.
    }

    public static class CollectorRenderState extends BlockEntityRenderState {
    }
}
