package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.AstralCrystalBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * AstralCrystalRenderer — 6-way directional Block Entity Renderer for Astral Crystal clusters and buds.
 * Dispatches procedural irregular crystal meshes to SubmitNodeCollector.
 */
public class AstralCrystalRenderer implements BlockEntityRenderer<AstralCrystalBlockEntity, AstralCrystalRenderer.CrystalRenderState> {

    public static class CrystalRenderState extends BlockEntityRenderState {
        public int stage = 4;
        public Direction facing = Direction.UP;
        public double smoothTime = 0.0;
    }

    public AstralCrystalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public CrystalRenderState createRenderState() {
        return new CrystalRenderState();
    }

    @Override
    public void extractRenderState(AstralCrystalBlockEntity be, CrystalRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.stage = be.getStage();
        if (be.getBlockState().hasProperty(BlockStateProperties.FACING)) {
            state.facing = be.getBlockState().getValue(BlockStateProperties.FACING);
        } else {
            state.facing = Direction.UP;
        }
        long gameTime = be.getLevel() != null ? be.getLevel().getGameTime() : 0L;
        state.smoothTime = (double) gameTime + (double) partialTick;
    }

    @Override
    public void submit(CrystalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = state.lightCoords;
        int overlay = OverlayTexture.NO_OVERLAY;
        Direction facing = state.facing != null ? state.facing : Direction.UP;

        poseStack.pushPose();

        switch (facing) {
            case UP -> poseStack.translate(0.5, 0.0, 0.5);
            case DOWN -> {
                poseStack.translate(0.5, 1.0, 0.5);
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
            }
            case NORTH -> {
                poseStack.translate(0.5, 0.5, 1.0);
                poseStack.mulPose(Axis.XN.rotationDegrees(90.0f));
            }
            case SOUTH -> {
                poseStack.translate(0.5, 0.5, 0.0);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            }
            case WEST -> {
                poseStack.translate(1.0, 0.5, 0.5);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
            }
            case EAST -> {
                poseStack.translate(0.0, 0.5, 0.5);
                poseStack.mulPose(Axis.ZN.rotationDegrees(90.0f));
            }
        }

        collector.submitCustomGeometry(poseStack, AstralCrystalMesh.getRenderType(), (pose, consumer) -> {
            AstralCrystalMesh.renderCluster(pose.pose(), consumer, state.stage, light, overlay, state.smoothTime);
        });

        poseStack.popPose();
    }
}
