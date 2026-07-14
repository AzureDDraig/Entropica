package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.CreativeMateriaGeneratorBlockEntity;
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

public class CreativeMateriaGeneratorRenderer implements BlockEntityRenderer<CreativeMateriaGeneratorBlockEntity, CreativeMateriaGeneratorRenderer.GeneratorRenderState> {

    private final Font font;

    public CreativeMateriaGeneratorRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public GeneratorRenderState createRenderState() {
        return new GeneratorRenderState();
    }

    @Override
    public void extractRenderState(CreativeMateriaGeneratorBlockEntity be, GeneratorRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.currentType = be.getCurrentType();
        state.worldPos = be.getBlockPos();
    }

    @Override
    public void submit(GeneratorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        if (state.currentType == null || state.worldPos == null) return;

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Safely check if the player is looking directly at this specific generator
        boolean isHovered = false;
        if (mc.hitResult instanceof BlockHitResult bhr) {
            isHovered = bhr.getBlockPos().equals(state.worldPos);
        }

        EssenceType[] types = EssenceType.values();
        int curIdx = state.currentType.ordinal();

        // If hovered, expand to show 5 items. If not hovered, only show the currently selected one.
        int listRange = isHovered ? 2 : 0;

        // Render the UI perfectly flat against all 4 horizontal faces
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            poseStack.pushPose();

            // 1. Move to block center
            poseStack.translate(0.5f, 0.5f, 0.5f);

            // 2. Rotate the text so it faces perfectly outwards based on the block face
            switch (dir) {
                case SOUTH -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0));
                case NORTH -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
                case WEST -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(270));
                case EAST -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
            }

            // 3. Push it to the very edge of the specific face (+0.505 prevents Z-fighting)
            poseStack.translate(0.0f, 0.0f, 0.505f);

            // 4. Flip Y and Z so text is oriented correctly (Z is negative to face outwards)
            float baseScale = 0.012f;
            poseStack.scale(baseScale, -baseScale, -baseScale);

            // 4. Draw the dynamic list for this face
            for (int i = -listRange; i <= listRange; i++) {
                int idx = (curIdx + i) % types.length;
                if (idx < 0) idx += types.length;
                EssenceType t = types[idx];

                poseStack.pushPose();

                // Move up/down based on position in the column
                float yOffset = i * 14.0f;
                poseStack.translate(0, yOffset, 0);

                // Scale shrinks the further away from the center they are
                float scale = i == 0 ? 1.0f : (Math.abs(i) == 1) ? 0.75f : 0.5f;
                poseStack.scale(scale, scale, 1.0f);

                // Alpha fades out the further away they are
                int alpha = i == 0 ? 255 : (Math.abs(i) == 1) ? 150 : 60;

                // Central item gets its custom color, neighbors are forced to flat gray
                int color = i == 0 ? t.getColorInt() : 0xAAAAAA;
                int finalColor = (alpha << 24) | (color & 0xFFFFFF);

                String text = t.getFormattedName();
                if (text == null) text = t.name(); // Fallback

                // Strip formatting codes from neighbors so they don't break the gray color override
                if (i != 0) {
                    text = ChatFormatting.stripFormatting(text);
                }

                float xOffset = (float)(-this.font.width(text) / 2);

                // Draw without shadow (false) for a cleaner "digital screen" look
                this.font.drawInBatch(text, xOffset, 0, finalColor, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

                poseStack.popPose();
            }

            poseStack.popPose();
        }
    }

    public static class GeneratorRenderState extends BlockEntityRenderState {
        public EssenceType currentType;
        public BlockPos worldPos;
    }
}