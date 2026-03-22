package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.entity.EidolicShadowEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

public class EidolicShadowRenderer extends EntityRenderer<EidolicShadowEntity, EidolicShadowRenderer.ShadowRenderState> {

    // You can replace this with your actual detailed beast texture file!
    private static final ResourceLocation SHADOW_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private final EidolicShadowModel model;
    private final Font font;
    private final ItemModelResolver itemModelResolver;

    public EidolicShadowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.font = context.getFont();
        this.itemModelResolver = context.getItemModelResolver();
        this.model = new EidolicShadowModel(context.bakeLayer(EidolicShadowModel.LAYER_LOCATION));
    }

    public static class ShadowRenderState extends EntityRenderState {
        public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
        public boolean hasItem = false;
        public String[] runes = new String[4];
        public float yRot = 0;
        public float walkDist = 0;
        public float ageInTicks = 0;
    }

    @Override
    public ShadowRenderState createRenderState() {
        return new ShadowRenderState();
    }

    @Override
    public void extractRenderState(EidolicShadowEntity entity, ShadowRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.hasItem = !entity.getEntityData().get(EidolicShadowEntity.CARRIED_ITEM).isEmpty();
        if (state.hasItem) {
            this.itemModelResolver.updateForTopItem(state.itemRenderState, entity.getEntityData().get(EidolicShadowEntity.CARRIED_ITEM), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, entity.level(), entity, 0);
        }

        state.runes[0] = entity.getEntityData().get(EidolicShadowEntity.RUNE_1);
        state.runes[1] = entity.getEntityData().get(EidolicShadowEntity.RUNE_2);
        state.runes[2] = entity.getEntityData().get(EidolicShadowEntity.RUNE_3);
        state.runes[3] = entity.getEntityData().get(EidolicShadowEntity.RUNE_4);

        state.yRot = entity.getViewYRot(partialTick);
        state.walkDist = Mth.lerp(partialTick, entity.prevWalkDist, entity.walkDist);
        state.ageInTicks = entity.tickCount + partialTick;
    }

    @Override
    public void submit(ShadowRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        int light = 15728880;

        poseStack.pushPose();

        // Flip the entity to face correctly
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - state.yRot));
        // Models inherently render upside down in Minecraft, so we flip it globally
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.5F, 0.0F);

        // Fetch the global buffer source since SubmitNodeCollector doesn't expose a raw VertexConsumer
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        // 1. Setup and render the entire beast model in one clean pass
        this.model.setupAnim(state);

        // Safely extract the Translucent rendering buffer
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(SHADOW_TEXTURE));

        // Render the model natively. (Passing -1 applies a pure white/un-tinted ARGB base color)
        this.model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, -1);

        // 2. Render Runes (Dynamically attached to the Head's position/rotation)
        poseStack.pushPose();

        // Magically sync the poseStack natively to exactly where the Beast's head is looking!
        this.model.body.translateAndRotate(poseStack);
        this.model.head.translateAndRotate(poseStack);

        float textScale = 0.015f;
        poseStack.translate(0, -0.1f, -0.4f); // Shift to the front of the face
        poseStack.scale(textScale, textScale, textScale);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f)); // Flip text right-side up

        drawRune(font, poseStack, bufferSource, state.runes[0], -6, -6, light);
        drawRune(font, poseStack, bufferSource, state.runes[1], 6, -6, light);
        drawRune(font, poseStack, bufferSource, state.runes[2], -6, 6, light);
        drawRune(font, poseStack, bufferSource, state.runes[3], 6, 6, light);

        // Flush everything (Model & Runes) directly to the GPU simultaneously
        bufferSource.endBatch();

        poseStack.popPose();

        // 3. Render Carried Item (Dynamically attached to the Right Gauntlet/Claw's position)
        if (state.hasItem) {
            poseStack.pushPose();

            // Sync the poseStack perfectly to the Beast's right arm swing
            this.model.body.translateAndRotate(poseStack);
            this.model.rightArm.translateAndRotate(poseStack);

            // Offset down the arm to the gauntlet claw area
            poseStack.translate(-0.1f, 0.8f, -0.2f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f)); // Angle item forward
            poseStack.scale(0.8f, 0.8f, 0.8f);

            state.itemRenderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
        super.submit(state, poseStack, collector, cameraState);
    }

    private void drawRune(Font font, PoseStack stack, MultiBufferSource buffer, String rune, float x, float y, int light) {
        if (rune == null || rune.isEmpty()) return;
        float width = font.width(rune);
        // Blood Red Runes (0xFF0000)
        font.drawInBatch(rune, x - (width / 2f), y - (font.lineHeight / 2f), 0xFF0000, false, stack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
    }
}