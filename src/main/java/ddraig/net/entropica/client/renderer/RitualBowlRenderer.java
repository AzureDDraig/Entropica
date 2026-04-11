package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.RitualBowlBlockEntity;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RitualBowlRenderer implements BlockEntityRenderer<RitualBowlBlockEntity, RitualBowlRenderer.BowlRenderState> {

    private final ItemModelResolver itemModelResolver;

    public RitualBowlRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public BowlRenderState createRenderState() {
        return new BowlRenderState();
    }

    @Override
    public void extractRenderState(RitualBowlBlockEntity be, BowlRenderState state, float partialTick,
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
    public void submit(BowlRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        float time = state.time;
        int light = 15728880;

        if (state.hasInput) {
            poseStack.pushPose();
            float offset = Mth.sin(time / 15.0f) * 0.05f;
            poseStack.translate(0.5, 0.40 + offset, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.0f));
            poseStack.scale(0.35f, 0.35f, 0.35f);
            state.inputItem.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        for (int i = 0; i < 4; i++) {
            if (state.hasOutput[i]) {
                poseStack.pushPose();

                // Tighter orbit suitable for the smaller Ritual Bowl
                float angle = (time * 3.0f + (i * 90.0f)) * Mth.DEG_TO_RAD;
                float radius = 0.20f;
                float cx = 0.5f + radius * Mth.cos(angle);
                float cz = 0.5f + radius * Mth.sin(angle);
                float cy = 0.45f + Mth.sin((time + i * 20) / 10.0f) * 0.08f;

                poseStack.translate(cx, cy, cz);
                poseStack.mulPose(Axis.YP.rotationDegrees(time * 4.0f));
                poseStack.scale(0.25f, 0.25f, 0.25f);

                state.outputItems[i].submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }
    }

    public static class BowlRenderState extends BlockEntityRenderState {
        public float time;
        public boolean hasInput;
        public final ItemStackRenderState inputItem = new ItemStackRenderState();

        public final boolean[] hasOutput = new boolean[4];
        public final ItemStackRenderState[] outputItems = new ItemStackRenderState[4];

        public BowlRenderState() {
            for (int i = 0; i < 4; i++) {
                outputItems[i] = new ItemStackRenderState();
            }
        }
    }
}