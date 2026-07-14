package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.api.materia.MateriaStack;
import ddraig.net.entropica.block.VaporPneumaticDiverterBlock;
import ddraig.net.entropica.block.VaporPneumaticOneWayValveBlock;
import ddraig.net.entropica.block.VaporPneumaticPipeBlock;
import ddraig.net.entropica.block.entity.VaporPneumaticPipeBlockEntity;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * The VaporPneumaticPipeRenderer class serves as a custom renderer for the VaporPneumaticPipeBlockEntity.
 * It handles rendering visual representations of Vis fume flow and associated states such as capacity,
 * flow direction, and visual alerts for certain conditions (e.g., purging or low-pressure warnings).
 *
 * This renderer dynamically manages the appearance of Vis fume animation, attached pipe connections,
 * one-way valve arrows, and diagnostic text readouts, providing a cohesive visual representation
 * of the block entity's functionality within a game.
 */
public class VaporPneumaticPipeRenderer implements BlockEntityRenderer<VaporPneumaticPipeBlockEntity, VaporPneumaticPipeRenderer.VisFumePipeRenderState> {

    private static final ResourceLocation GAS_ANIM_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/misc/fume_gas_anim.png");

    public VaporPneumaticPipeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public VisFumePipeRenderState createRenderState() {
        return new VisFumePipeRenderState();
    }

    private void setDirectionState(VisFumePipeRenderState state, Direction dir, boolean value) {
        switch (dir) {
            case NORTH -> state.north = value;
            case SOUTH -> state.south = value;
            case EAST  -> state.east = value;
            case WEST  -> state.west = value;
            case UP    -> state.up = value;
            case DOWN  -> state.down = value;
        }
    }

    @Override
    public void extractRenderState(VaporPneumaticPipeBlockEntity be, VisFumePipeRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);

        MateriaStack fumeStack = be.getMateriaInTank();

        if (fumeStack.isEmpty()) {
            state.type = null;
            state.amount = 0;
        } else {
            state.type = fumeStack.getType();
            state.amount = fumeStack.getAmount();
        }

        // FIXED: Switched from getCapacity() to getSafeCapacity()
        state.maxCapacity = be.getSafeCapacity();
        state.time = (be.getLevel() != null ? be.getLevel().getGameTime() : 0) + partialTick;
        state.isPurging = be.isBeingOverpowered();

        Player player = Minecraft.getInstance().player;
        state.isHoldingDetector = player != null && (
                player.getMainHandItem().is(ModItems.MATERIA_VALUE_DETECTOR.get()) ||
                        player.getOffhandItem().is(ModItems.MATERIA_VALUE_DETECTOR.get())
        );

        BlockState blockState = be.getBlockState();

        if (blockState.getBlock() instanceof VaporPneumaticOneWayValveBlock) {
            state.isOneWayValve = true;
            state.valveFacing = blockState.getValue(VaporPneumaticOneWayValveBlock.FACING);
        } else {
            state.isOneWayValve = false;
            state.valveFacing = null;
        }

        // Reset all directions to false first
        state.north = false;
        state.south = false;
        state.east  = false;
        state.west  = false;
        state.up    = false;
        state.down  = false;

        if (blockState.getBlock() instanceof VaporPneumaticPipeBlock) {
            state.north = blockState.getValue(VaporPneumaticPipeBlock.NORTH);
            state.south = blockState.getValue(VaporPneumaticPipeBlock.SOUTH);
            state.east  = blockState.getValue(VaporPneumaticPipeBlock.EAST);
            state.west  = blockState.getValue(VaporPneumaticPipeBlock.WEST);
            state.up    = blockState.getValue(VaporPneumaticPipeBlock.UP);
            state.down  = blockState.getValue(VaporPneumaticPipeBlock.DOWN);
        } else if (blockState.getBlock() instanceof VaporPneumaticDiverterBlock) {
            Direction inputFace = blockState.getValue(VaporPneumaticDiverterBlock.FACING);
            Direction playerPerspective = blockState.getValue(VaporPneumaticDiverterBlock.HORIZONTAL_FACING);

            Direction forwardFace = inputFace.getOpposite();
            Direction rightFace;
            Direction leftFace;

            if (inputFace.getAxis().isVertical()) {
                leftFace = playerPerspective.getCounterClockWise();
                rightFace = playerPerspective.getClockWise();
            } else {
                leftFace = inputFace.getClockWise();
                rightFace = inputFace.getCounterClockWise();
            }

            setDirectionState(state, inputFace, true);
            setDirectionState(state, forwardFace, blockState.getValue(VaporPneumaticDiverterBlock.FORWARD_OPEN));
            setDirectionState(state, leftFace, blockState.getValue(VaporPneumaticDiverterBlock.LEFT_OPEN));
            setDirectionState(state, rightFace, blockState.getValue(VaporPneumaticDiverterBlock.RIGHT_OPEN));
        }
    }

    @Override
    public void submit(VisFumePipeRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState cameraState) {
        if (state.type == null || state.amount <= 0) return;

        int colorInt = state.type.getColorInt();
        float r = ((colorInt >> 16) & 0xFF) / 255.0f;
        float g = ((colorInt >> 8)  & 0xFF) / 255.0f;
        float b = (colorInt & 0xFF)          / 255.0f;
        float alpha = 1.0f;

        int frameCount = 32;
        float flowSpeed = 0.4f;
        int currentFrame = (int) (state.time * flowSpeed) % frameCount;
        float vMin = (float) currentFrame / frameCount;
        float vMax = (float) (currentFrame + 1) / frameCount;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderType renderType = RenderType.entityTranslucent(GAS_ANIM_TEXTURE);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        PoseStack.Pose pose = poseStack.last();
        float min = 0.385f;
        float max = 0.615f;
        int light = 15728880;

        renderCuboid(pose, consumer, min, min, min, max, max, max, r, g, b, alpha, light, vMin, vMax);

        if (state.north) renderCuboid(pose, consumer, min, min, 0.001f, max, max, min,   r, g, b, alpha, light, vMin, vMax);
        if (state.south) renderCuboid(pose, consumer, min, min, max,   max, max, 0.999f, r, g, b, alpha, light, vMin, vMax);
        if (state.east)  renderCuboid(pose, consumer, max, min, min,   0.999f, max, max, r, g, b, alpha, light, vMin, vMax);
        if (state.west)  renderCuboid(pose, consumer, 0.001f, min, min, min,  max, max,  r, g, b, alpha, light, vMin, vMax);
        if (state.up)    renderCuboid(pose, consumer, min, max, min,   max, 0.999f, max, r, g, b, alpha, light, vMin, vMax);
        if (state.down)  renderCuboid(pose, consumer, min, 0.001f, min, max,  min, max,  r, g, b, alpha, light, vMin, vMax);

        // --- 1. PINNED VALUE TEXT RENDERING ---
        if (state.isHoldingDetector) {
            poseStack.pushPose();

            boolean isLowPressure = state.amount > 0 && state.amount < 10;
            float xShake = 0;
            float yShake = 0;
            float scaleModifier = 1.0f;
            int renderColor = colorInt | 0xFF000000;
            String text = state.amount + " Vf"; // Changed from mb to Vf

            if (state.isPurging) {
                renderColor = 0xFFFF5555;
                scaleModifier = 1.2f + (float) Math.sin(state.time * 15.0f) * 0.1f;
                xShake = (float) Math.sin(state.time * 30.0f) * 0.04f;
                text = "!! PURGE !! " + text;
            } else if (isLowPressure) {
                float shakeStrength = 0.02f;
                float shakeSpeed = 20.0f;
                xShake = (float) Math.sin(state.time * shakeSpeed) * shakeStrength;
                yShake = (float) Math.cos(state.time * shakeSpeed * 1.2f) * shakeStrength;
                float flash = (float) (Math.sin(state.time * 10.0f) * 0.5 + 0.5);
                if (flash < 0.3f) renderColor = 0xFF555555;
                text = "⚠ " + text;
            }

            poseStack.translate(0.5 + xShake, 1.0 + yShake, 0.5);
            poseStack.mulPose(cameraState.orientation);

            float baseScale = -0.02F;
            poseStack.scale(baseScale * scaleModifier, baseScale * scaleModifier, baseScale * scaleModifier);

            Font font = Minecraft.getInstance().font;
            float xOffset = (float)(-font.width(text) / 2);
            Matrix4f textMatrix = poseStack.last().pose();

            font.drawInBatch(text, xOffset, 0, renderColor, true, textMatrix, bufferSource, Font.DisplayMode.NORMAL, 0, light);
            poseStack.popPose();
        }

        // --- 2. ONE-WAY VALVE ARROW RENDERING ---
        if (state.isOneWayValve && state.valveFacing != null) {
            poseStack.pushPose();

            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(state.valveFacing.getRotation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.translate(0, 0, 0.26);
            poseStack.mulPose(Axis.YP.rotationDegrees(180));

            poseStack.mulPose(Axis.ZP.rotationDegrees(-90));

            poseStack.scale(-0.03F, -0.03F, 0.03F);

            int arrowColor = state.amount > 0 ? colorInt | 0xFF000000 : 0xFFFFFFFF;
            String arrowStr = "➔";

            Font font = Minecraft.getInstance().font;
            float xOffset = (float)(-font.width(arrowStr) / 2);
            float yOffset = (float)(-font.lineHeight / 2);

            font.drawInBatch(arrowStr, xOffset, yOffset, arrowColor, true, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

            poseStack.popPose();
        }

        bufferSource.endBatch();
    }

    private void renderCuboid(PoseStack.Pose pose, VertexConsumer consumer,
                              float minX, float minY, float minZ,
                              float maxX, float maxY, float maxZ,
                              float r, float g, float b, float a, int light, float vMin, float vMax) {
        renderQuad(pose, consumer, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, Direction.DOWN,  r, g, b, a, light, vMin, vMax);
        renderQuad(pose, consumer, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, Direction.UP,    r, g, b, a, light, vMin, vMax);
        renderQuad(pose, consumer, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, Direction.NORTH, r, g, b, a, light, vMin, vMax);
        renderQuad(pose, consumer, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, Direction.SOUTH, r, g, b, a, light, vMin, vMax);
        renderQuad(pose, consumer, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, Direction.WEST,  r, g, b, a, light, vMin, vMax);
        renderQuad(pose, consumer, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, Direction.EAST,  r, g, b, a, light, vMin, vMax);
    }

    private void renderQuad(PoseStack.Pose pose, VertexConsumer consumer,
                            float x1, float y1, float z1, float x2, float y2, float z2,
                            float x3, float y3, float z3, float x4, float y4, float z4,
                            Direction normal, float r, float g, float b, float a, int light, float vMin, float vMax) {
        float nx = normal.getStepX();
        float ny = normal.getStepY();
        float nz = normal.getStepZ();
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(0, vMin).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(1, vMin).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(1, vMax).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x4, y4, z4).setColor(r, g, b, a).setUv(0, vMax).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    public static class VisFumePipeRenderState extends BlockEntityRenderState {
        public EssenceType type;
        public int amount;
        public int maxCapacity;
        public float time;
        public boolean isPurging;
        public boolean isHoldingDetector;
        public boolean isOneWayValve;
        public Direction valveFacing;
        public boolean north, south, east, west, up, down;
    }
}