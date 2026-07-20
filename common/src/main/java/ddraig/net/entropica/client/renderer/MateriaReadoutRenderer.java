package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.IVaporHandler;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.api.materia.MateriaStack;
import ddraig.net.entropica.block.MateriaReadoutBlock;
import ddraig.net.entropica.block.entity.EntropicCoreBlockEntity;
import ddraig.net.entropica.block.entity.MateriaReadoutBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class MateriaReadoutRenderer implements BlockEntityRenderer<MateriaReadoutBlockEntity, MateriaReadoutRenderer.ReadoutRenderState> {
    private final Font font;

    public MateriaReadoutRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public ReadoutRenderState createRenderState() { return new ReadoutRenderState(); }

    @Override
    public void extractRenderState(MateriaReadoutBlockEntity blockEntity, ReadoutRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);
        renderState.facing = blockEntity.getBlockState().getValue(MateriaReadoutBlock.FACING);
        renderState.visPool.clear();
        renderState.hasTarget = false;
        renderState.targetName = "";

        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();

        if (level != null) {
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        BlockEntity targetEntity = level.getBlockEntity(pos.offset(x, y, z));

                        if (targetEntity == null) continue;

                        // 1. Check for Entropic Core Block
                        if (targetEntity instanceof EntropicCoreBlockEntity core) {
                            renderState.hasTarget = true;
                            renderState.targetName = "Core Vis Levels";
                            renderState.visPool.putAll(core.getMateriaFumusPool());
                            return; // Stop searching once found
                        }

                        // 2. Check for Vis Fume Vessels or Chambers via IVaporHandler
                        else if (targetEntity instanceof IVaporHandler fumeHandler) {
                            String className = targetEntity.getClass().getSimpleName();

                            // We only want controllers, not basic pipes/valves!
                            if (className.contains("Controller")) {
                                renderState.hasTarget = true;
                                renderState.targetName = className.contains("Chamber") ? "Chamber Fume Levels" : "Vessel Fume Levels";

                                MateriaStack fume = fumeHandler.getMateriaInTank();
                                if (fume != null && !fume.isEmpty()) {
                                    renderState.visPool.put(fume.getType(), fume.getAmount());
                                }
                                return; // Stop searching once found
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void submit(ReadoutRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!state.hasTarget) return;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        poseStack.pushPose();

        // Move to center of the block
        poseStack.translate(0.5f, 0.5f, 0.5f);

        // Apply rotation precisely based on the facing of the block (supports all 6 sides)
        switch (state.facing) {
            case SOUTH -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0));
            case NORTH -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
            case WEST -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(270));
            case EAST -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
            case UP -> poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));
            case DOWN -> poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
        }

        // Push text strictly outwards to the face surface
        poseStack.translate(0.0f, 0.0f, 0.505f);

        // Applied the negative Z-scale fix so it perfectly respects all block face rotations!
        poseStack.scale(0.010f, -0.010f, -0.010f);

        // Draw the dynamic Header
        this.font.drawInBatch("§n" + state.targetName, -45, -50, 0xFF55FFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

        int y = -35;
        if (state.visPool.isEmpty()) {
            this.font.drawInBatch("§7Empty", -45, y, 0xFF888888, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        } else {
            for (Map.Entry<EssenceType, Integer> entry : state.visPool.entrySet()) {
                if (entry.getValue() > 0) {
                    EssenceType type = entry.getKey();

                    // Convert the essence's vivid RGB array into an ARGB Hex Color
                    int[] rgb = getVibrantColor(type);
                    int colorHex = (0xFF << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];

                    // Draw the text using the exact calculated color
                    this.font.drawInBatch(type.getDisplayName() + ": " + entry.getValue(), -45, y, colorHex, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
                    y += 10;
                }
            }
        }
        poseStack.popPose();
    }

    private int[] getVibrantColor(EssenceType type) {
        int r = type.getR(), g = type.getG(), b = type.getB();
        if (type.name().equals("CHIMERA") || type.isDynamic()) {
            float t = (System.currentTimeMillis() % 4000L) / 4000.0f * (float) Math.PI * 2;
            r = (int) ((Math.sin(t) * 0.5 + 0.5) * 255);
            g = (int) ((Math.sin(t + 2.094) * 0.5 + 0.5) * 255);
            b = (int) ((Math.sin(t + 4.188) * 0.5 + 0.5) * 255);
        }
        return new int[]{r, g, b};
    }

    public static class ReadoutRenderState extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public boolean hasTarget = false;
        public String targetName = "";
        public final Map<EssenceType, Integer> visPool = new EnumMap<>(EssenceType.class);
    }
}