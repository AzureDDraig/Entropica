package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.EssenceReadoutBlock;
import ddraig.net.entropica.block.entity.EntropicCoreBlockEntity;
import ddraig.net.entropica.block.entity.EssenceReadoutBlockEntity;
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
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EssenceReadoutRenderer implements BlockEntityRenderer<EssenceReadoutBlockEntity, EssenceReadoutRenderer.ReadoutRenderState> {
    private final Font font;
    private static final int MAX_VISIBLE_LINES = 8;
    private static final int TICKS_PER_LINE = 40;

    public EssenceReadoutRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public ReadoutRenderState createRenderState() { return new ReadoutRenderState(); }

    @Override
    public void extractRenderState(EssenceReadoutBlockEntity blockEntity, ReadoutRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);
        renderState.facing = blockEntity.getBlockState().getValue(EssenceReadoutBlock.FACING);
        renderState.essencePool.clear();
        renderState.hasCore = false;
        renderState.gameTime = 0;

        Level level = blockEntity.getLevel();
        if (level != null) {
            EntropicCoreBlockEntity masterCore = null;
            BlockPos pos = blockEntity.getBlockPos();

            for (int x = -4; x <= 4; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -4; z <= 4; z++) {
                        if (level.getBlockEntity(pos.offset(x, y, z)) instanceof EntropicCoreBlockEntity foundCore) {
                            masterCore = foundCore.getMaster();
                            break;
                        }
                    }
                }
                if (masterCore != null) break;
            }

            if (masterCore != null && masterCore.isFormed()) {
                renderState.hasCore = true;
                renderState.gameTime = level.getGameTime();

                masterCore.getEssencePool().forEach((type, amt) -> {
                    if (amt > 0) {
                        renderState.essencePool.merge(type, amt, Integer::sum);
                    }
                });

                for (int i = 0; i < masterCore.sharedReceptacleBuffer.getContainerSize(); i++) {
                    net.minecraft.world.item.ItemStack stack = masterCore.sharedReceptacleBuffer.getItem(i);
                    if (!stack.isEmpty()) {
                        EssenceType type = masterCore.getEssenceTypeFromItem(stack);
                        if (type != null) {
                            int value = stack.getCount() * getEssenceValue(stack);
                            renderState.essencePool.merge(type, value, Integer::sum);
                        }
                    }
                }
            }
        }
    }

    private int getEssenceValue(net.minecraft.world.item.ItemStack stack) {
        String name = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (name.contains("strong") || name.contains("large")) return 16;
        if (name.contains("average") || name.contains("medium")) return 4;
        return 1;
    }

    @Override
    public void submit(ReadoutRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!state.hasCore) return;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        poseStack.pushPose();

        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-state.facing.toYRot()));
        poseStack.translate(0.0f, 0.0f, 0.506f);
        poseStack.scale(0.008f, -0.008f, 0.008f);

        List<Map.Entry<EssenceType, Integer>> activeEntries = new ArrayList<>();
        for (Map.Entry<EssenceType, Integer> entry : state.essencePool.entrySet()) {
            if (entry.getValue() > 0) activeEntries.add(entry);
        }

        int y = -50;
        this.font.drawInBatch("§b📥 ESSENCE BUFFER", -55, y, 0xFFFFFFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        y += 12;

        if (activeEntries.isEmpty()) {
            this.font.drawInBatch("§8[ No Essence Detected ]", -50, y, 0xFFFFFFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        } else if (activeEntries.size() <= MAX_VISIBLE_LINES) {
            for (Map.Entry<EssenceType, Integer> entry : activeEntries) {
                renderLine(entry.getKey(), entry.getValue(), y, state.gameTime, poseStack, bufferSource);
                y += 10;
            }
        } else {
            int totalLines = activeEntries.size();
            int maxScroll = totalLines - MAX_VISIBLE_LINES;

            long time = state.gameTime / TICKS_PER_LINE;
            int scrollPos = (int) (time % (maxScroll + 2));
            scrollPos = Mth.clamp(scrollPos, 0, maxScroll);

            for (int i = 0; i < MAX_VISIBLE_LINES; i++) {
                int index = scrollPos + i;
                if (index < totalLines) {
                    Map.Entry<EssenceType, Integer> entry = activeEntries.get(index);
                    renderLine(entry.getKey(), entry.getValue(), y, state.gameTime, poseStack, bufferSource);
                    y += 10;
                }
            }

            String scrollText = "§7--- ↓ Scroll [" + (scrollPos + 1) + "/" + totalLines + "] ↓ ---";
            this.font.drawInBatch(scrollText, -this.font.width(scrollText)/2, 50, 0xFFFFFFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        }

        poseStack.popPose();
    }

    private void renderLine(EssenceType type, int amount, int y, long gameTime, PoseStack ps, MultiBufferSource buf) {
        int[] rgb = type.getCurrentRGB(gameTime);
        int color = (0xFF << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
        String label = "• " + type.getDisplayName();
        String val = String.valueOf(amount);

        this.font.drawInBatch(label, -50, y, color, false, ps.last().pose(), buf, Font.DisplayMode.NORMAL, 0, 15728880);
        this.font.drawInBatch(val, 50 - this.font.width(val), y, 0xFFCCCCCC, false, ps.last().pose(), buf, Font.DisplayMode.NORMAL, 0, 15728880);
    }

    public static class ReadoutRenderState extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public boolean hasCore = false;
        public long gameTime = 0;
        public final Map<EssenceType, Integer> essencePool = new EnumMap<>(EssenceType.class);
    }
}