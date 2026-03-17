package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.AttunementPedestalBlockEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AttunementPedestalRenderer implements BlockEntityRenderer<AttunementPedestalBlockEntity, AttunementPedestalRenderer.PedestalRenderState> {

    private final ItemModelResolver itemModelResolver;

    public AttunementPedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    // Expand the render bounding box so items don't vanish when looking away
    @Override
    public AABB getRenderBoundingBox(AttunementPedestalBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(2.0);
    }

    @Override
    public PedestalRenderState createRenderState() {
        return new PedestalRenderState();
    }

    @Override
    public void extractRenderState(AttunementPedestalBlockEntity be, PedestalRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);

        Level level = be.getLevel();

        // Use a modulo for game time to preserve float precision for smooth animations!
        state.time = (level != null ? (level.getGameTime() % 360000L) : 0) + partialTick;

        int seed = (int) be.getBlockPos().asLong();

        // Extract Slot 0
        ItemStack item1 = be.inventory.getItem(0);
        state.hasItem1 = !item1.isEmpty();
        if (state.hasItem1 && level != null) {
            this.itemModelResolver.updateForTopItem(state.item1State, item1, ItemDisplayContext.GROUND, level, null, seed);
        } else {
            state.item1State.clear();
        }

        // Extract Slot 1
        ItemStack item2 = be.inventory.getItem(1);
        state.hasItem2 = !item2.isEmpty();
        if (state.hasItem2 && level != null) {
            this.itemModelResolver.updateForTopItem(state.item2State, item2, ItemDisplayContext.GROUND, level, null, seed + 1);
        } else {
            state.item2State.clear();
        }
    }

    @Override
    public void submit(PedestalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        if (!state.hasItem1 && !state.hasItem2) return;

        int light = 15728880; // Render items at full magical brightness
        float time = state.time;

        // Adds a slow, magical bobbing motion
        float offset = (float) Math.sin(time / 10.0f) * 0.1f;
        // Continuously rotates the items
        float rotation = time * 3.0f;

        if (state.hasItem1) {
            poseStack.pushPose();
            // Center it horizontally, and push it ~1.2 blocks up
            poseStack.translate(0.5, 1.2 + offset, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.scale(0.5f, 0.5f, 0.5f);

            state.item1State.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        if (state.hasItem2) {
            poseStack.pushPose();
            // Second item floats slightly higher and bobs/spins in the exact opposite direction!
            poseStack.translate(0.5, 1.6 - offset, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
            poseStack.scale(0.5f, 0.5f, 0.5f);

            state.item2State.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    public static class PedestalRenderState extends BlockEntityRenderState {
        public float time;
        public final ItemStackRenderState item1State = new ItemStackRenderState();
        public boolean hasItem1;
        public final ItemStackRenderState item2State = new ItemStackRenderState();
        public boolean hasItem2;
    }
}