package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ddraig.net.entropica.block.entity.CreativeParticleGeneratorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CreativeParticleGeneratorRenderer implements BlockEntityRenderer<CreativeParticleGeneratorBlockEntity, CreativeParticleGeneratorRenderer.ParticleRenderState> {

    private final Font font;

    public CreativeParticleGeneratorRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public ParticleRenderState createRenderState() {
        return new ParticleRenderState();
    }

    @Override
    public void extractRenderState(CreativeParticleGeneratorBlockEntity be, ParticleRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.worldPos = be.getBlockPos();

        // Extract 5 strings (current + 2 neighbors on each side) to render the list thread-safely
        for (int i = -2; i <= 2; i++) {
            state.displayNames[i + 2] = be.getParticleNameOffset(i);
        }
    }

    @Override
    public void submit(ParticleRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        if (state.worldPos == null) return;

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Safely check if the player is looking directly at this specific generator
        boolean isHovered = false;
        if (mc.hitResult instanceof BlockHitResult bhr) {
            isHovered = bhr.getBlockPos().equals(state.worldPos);
        }

        int listRange = isHovered ? 2 : 0;

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            poseStack.pushPose();

            poseStack.translate(
                    0.5 + dir.getStepX() * 0.505,
                    0.5,
                    0.5 + dir.getStepZ() * 0.505
            );

            switch (dir) {
                case SOUTH -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0));
                case NORTH -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
                case WEST -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(270));
                case EAST -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
            }

            // A slightly smaller scale since Particle namespaces (e.g. "minecraft:soul_fire_flame") are long!
            float baseScale = 0.009f;
            poseStack.scale(baseScale, -baseScale, baseScale);

            for (int i = -listRange; i <= listRange; i++) {
                String text = state.displayNames[i + 2];
                if (text == null) continue;

                poseStack.pushPose();

                float yOffset = i * 14.0f;
                poseStack.translate(0, yOffset, 0);

                float scale = i == 0 ? 1.0f : (Math.abs(i) == 1) ? 0.75f : 0.5f;
                poseStack.scale(scale, scale, 1.0f);

                int alpha = i == 0 ? 255 : (Math.abs(i) == 1) ? 150 : 60;

                // Purple/Pink for the selected particle, Gray for neighbors
                int color = i == 0 ? 0xFF55FF : 0xAAAAAA;
                int finalColor = (alpha << 24) | (color & 0xFFFFFF);

                if (i != 0) {
                    text = ChatFormatting.stripFormatting(text);
                }

                float xOffset = (float)(-this.font.width(text) / 2);

                this.font.drawInBatch(text, xOffset, 0, finalColor, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

                poseStack.popPose();
            }

            poseStack.popPose();
        }

        bufferSource.endBatch();
    }

    public static class ParticleRenderState extends BlockEntityRenderState {
        public String[] displayNames = new String[5];
        public BlockPos worldPos;
    }
}