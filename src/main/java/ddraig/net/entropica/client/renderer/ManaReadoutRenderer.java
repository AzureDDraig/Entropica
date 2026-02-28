package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.ManaReadoutBlock;
import ddraig.net.entropica.block.entity.EntropicCoreBlockEntity;
import ddraig.net.entropica.block.entity.ManaReadoutBlockEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class ManaReadoutRenderer implements BlockEntityRenderer<ManaReadoutBlockEntity, ManaReadoutRenderer.ReadoutRenderState> {
    private final Font font;

    public ManaReadoutRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public ReadoutRenderState createRenderState() { return new ReadoutRenderState(); }

    @Override
    public void extractRenderState(ManaReadoutBlockEntity blockEntity, ReadoutRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);
        renderState.facing = blockEntity.getBlockState().getValue(ManaReadoutBlock.FACING);
        renderState.manaPool.clear();
        renderState.hasCore = false;

        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();

        if (level != null) {
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        if (level.getBlockEntity(pos.offset(x, y, z)) instanceof EntropicCoreBlockEntity core) {
                            renderState.hasCore = true;
                            renderState.manaPool.putAll(core.getManaPool());
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void submit(ReadoutRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!state.hasCore) return;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-state.facing.toYRot()));
        poseStack.translate(0.0f, 0.0f, 0.505f);
        poseStack.scale(0.010f, -0.010f, 0.010f); // Slightly larger since it's one column

        this.font.drawInBatch("§nCore Mana Levels", -45, -50, 0xFF55FFFF, true, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

        int y = -35;
        for (Map.Entry<EssenceType, Integer> entry : state.manaPool.entrySet()) {
            if (entry.getValue() > 0) {
                this.font.drawInBatch(entry.getKey().getDisplayName() + ": " + entry.getValue(), -45, y, 0xFFFFFFFF, true, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
                y += 10;
            }
        }
        poseStack.popPose();
    }

    public static class ReadoutRenderState extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public boolean hasCore = false;
        public final Map<EssenceType, Integer> manaPool = new EnumMap<>(EssenceType.class);
    }
}