package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.CrucibleBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity, CrucibleRenderer.CrucibleRenderState> {

    private final ItemModelResolver itemModelResolver;

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public CrucibleRenderState createRenderState() {
        return new CrucibleRenderState();
    }

    @Override
    public void extractRenderState(CrucibleBlockEntity be, CrucibleRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);

        Level level = be.getLevel();
        state.time = (level != null ? (level.getGameTime() % 360000L) : 0) + partialTick;

        int seed = (int) be.getBlockPos().asLong();

        // Slot 0: The Input Item
        ItemStack inputStack = be.inventory.getItem(0);
        state.hasInput = !inputStack.isEmpty();
        if (state.hasInput && level != null) {
            this.itemModelResolver.updateForTopItem(state.inputItem, inputStack, ItemDisplayContext.FIXED, level, null, seed);
        } else {
            state.inputItem.clear();
        }

        // Slots 1-4: The Output Essences
        for (int i = 0; i < 4; i++) {
            ItemStack outStack = be.inventory.getItem(i + 1);
            state.hasOutput[i] = !outStack.isEmpty();
            if (state.hasOutput[i] && level != null) {
                this.itemModelResolver.updateForTopItem(state.outputItems[i], outStack, ItemDisplayContext.FIXED, level, null, seed + i + 1);
            } else {
                state.outputItems[i].clear();
            }
        }
    }

    @Override
    public void submit(CrucibleRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {

        int light = 15728880;
        float time = state.time;

        // 1. Render Input Item (Bobbing centrally)
        if (state.hasInput) {
            poseStack.pushPose();
            float offset = Mth.sin(time / 10.0f) * 0.1f;
            poseStack.translate(0.5, 0.65 + offset, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.0f));
            poseStack.scale(0.5f, 0.5f, 0.5f);
            state.inputItem.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // 2. Render Output Items (Orbiting the input)
        for (int i = 0; i < 4; i++) {
            if (state.hasOutput[i]) {
                poseStack.pushPose();

                // Calculates a dynamic orbit around the center of the crucible basin
                float angle = (time * 2.0f + (i * 90.0f)) * Mth.DEG_TO_RAD;
                float radius = 0.25f;
                float cx = 0.5f + radius * Mth.cos(angle);
                float cz = 0.5f + radius * Mth.sin(angle);
                float cy = 0.75f + Mth.sin((time + i * 20) / 10.0f) * 0.1f;

                poseStack.translate(cx, cy, cz);
                poseStack.mulPose(Axis.YP.rotationDegrees(time * 3.0f));
                poseStack.scale(0.4f, 0.4f, 0.4f);

                state.outputItems[i].submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }
    }

    public static class CrucibleRenderState extends BlockEntityRenderState {
        public float time;
        public boolean hasInput;
        public final ItemStackRenderState inputItem = new ItemStackRenderState();

        public final boolean[] hasOutput = new boolean[4];
        public final ItemStackRenderState[] outputItems = new ItemStackRenderState[4];

        public CrucibleRenderState() {
            for (int i = 0; i < 4; i++) {
                outputItems[i] = new ItemStackRenderState();
            }
        }
    }
}