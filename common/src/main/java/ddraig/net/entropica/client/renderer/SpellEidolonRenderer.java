package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders {@link SpellEidolonEntity} using {@link EidolicShadowModel} tinted with the spell's
 * canonical Materia color, inscribed with the Triad archetype glyph on the construct's brow,
 * and displaying any mirrored equipment weapon in its gauntlet claw.
 */
public class SpellEidolonRenderer extends EntityRenderer<SpellEidolonEntity, SpellEidolonRenderer.EidolonRenderState> {

    private static final ResourceLocation EIDOLON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private final EidolicShadowModel model;
    private final Font font;
    private final ItemModelResolver itemModelResolver;

    public SpellEidolonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.font = context.getFont();
        this.itemModelResolver = context.getItemModelResolver();
        this.model = new EidolicShadowModel(context.bakeLayer(EidolicShadowModel.LAYER_LOCATION));
    }

    public static class EidolonRenderState extends EidolicShadowRenderer.ShadowRenderState {
        public int argbTint = 0xCC88CCFF;
        public int runeRgb = 0x88CCFF;
        public final ItemStackRenderState offhandRenderState = new ItemStackRenderState();
        public boolean hasOffhandItem = false;
        public final ItemStackRenderState headEquipRenderState = new ItemStackRenderState();
        public boolean hasHeadEquip = false;
        public final ItemStackRenderState chestEquipRenderState = new ItemStackRenderState();
        public boolean hasChestEquip = false;
    }

    @Override
    public EidolonRenderState createRenderState() {
        return new EidolonRenderState();
    }

    @Override
    public void extractRenderState(SpellEidolonEntity entity, EidolonRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        ItemStack displayItem = entity.getDisplayItem();
        state.hasItem = !displayItem.isEmpty();
        if (state.hasItem) {
            this.itemModelResolver.updateForTopItem(
                    state.itemRenderState,
                    displayItem,
                    ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    entity.level(),
                    entity,
                    0
            );
        }

        ItemStack offhandItem = entity.getItemBySlot(EquipmentSlot.OFFHAND);
        state.hasOffhandItem = !offhandItem.isEmpty();
        if (state.hasOffhandItem) {
            this.itemModelResolver.updateForTopItem(
                    state.offhandRenderState,
                    offhandItem,
                    ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                    entity.level(),
                    entity,
                    1
            );
        }

        ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
        state.hasHeadEquip = !headItem.isEmpty();
        if (state.hasHeadEquip) {
            this.itemModelResolver.updateForTopItem(
                    state.headEquipRenderState,
                    headItem,
                    ItemDisplayContext.HEAD,
                    entity.level(),
                    entity,
                    2
            );
        }

        ItemStack chestItem = entity.getItemBySlot(EquipmentSlot.CHEST);
        state.hasChestEquip = !chestItem.isEmpty();
        if (state.hasChestEquip) {
            this.itemModelResolver.updateForTopItem(
                    state.chestEquipRenderState,
                    chestItem,
                    ItemDisplayContext.FIXED,
                    entity.level(),
                    entity,
                    3
            );
        }

        int rgb;
        EssenceType mat = entity.getEffectiveMateria();
        if (entity.isMutated() && mat != EssenceType.REGULAR) {
            int[] cycleRgb = mat.getCurrentRGB(entity.level().getGameTime() + partialTick);
            rgb = ((cycleRgb[0] & 0xFF) << 16) | ((cycleRgb[1] & 0xFF) << 8) | (cycleRgb[2] & 0xFF);
        } else {
            rgb = entity.getTintRgb() & 0xFFFFFF;
        }
        state.argbTint = (0xCC << 24) | rgb;
        state.runeRgb = 0xFF000000 | Math.min(0xFFFFFF, rgb | 0x303030);

        String glyph = entity.getArchetype().getRuneGlyph();
        state.runes[0] = glyph;
        state.runes[1] = "";
        state.runes[2] = "";
        state.runes[3] = "";

        state.yRot = entity.getViewYRot(partialTick);
        state.walkDist = Mth.lerp(partialTick, entity.prevWalkDistAnim, entity.walkDistAnim);
        state.ageInTicks = entity.tickCount + partialTick;
    }

    @Override
    public void submit(EidolonRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        int light = 15728880;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - state.yRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.5F, 0.0F);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        this.model.setupAnim(state);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(EIDOLON_TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, state.argbTint);

        poseStack.pushPose();
        this.model.body.translateAndRotate(poseStack);
        this.model.head.translateAndRotate(poseStack);

        float textScale = 0.018f;
        poseStack.translate(0.0f, -0.1f, -0.41f);
        poseStack.scale(textScale, textScale, textScale);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        String rune = state.runes[0];
        if (rune != null && !rune.isEmpty()) {
            float width = this.font.width(rune);
            this.font.drawInBatch(rune, -(width / 2f), -(this.font.lineHeight / 2f), state.runeRgb, false,
                    poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
        }

        bufferSource.endBatch();
        poseStack.popPose();

        if (state.hasHeadEquip) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            this.model.head.translateAndRotate(poseStack);
            poseStack.translate(0.0f, -0.15f, -0.12f);
            poseStack.scale(0.65f, -0.65f, -0.65f);
            state.headEquipRenderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        if (state.hasChestEquip) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            poseStack.translate(0.0f, -0.15f, -0.40f);
            poseStack.scale(0.65f, -0.65f, -0.65f);
            state.chestEquipRenderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        if (state.hasItem) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            this.model.rightArm.translateAndRotate(poseStack);
            poseStack.translate(-0.1f, 0.8f, -0.2f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            poseStack.scale(0.8f, 0.8f, 0.8f);
            state.itemRenderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        if (state.hasOffhandItem) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            this.model.leftArm.translateAndRotate(poseStack);
            poseStack.translate(0.1f, 0.8f, -0.2f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            poseStack.scale(0.8f, 0.8f, 0.8f);
            state.offhandRenderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
        super.submit(state, poseStack, collector, cameraState);
    }
}
