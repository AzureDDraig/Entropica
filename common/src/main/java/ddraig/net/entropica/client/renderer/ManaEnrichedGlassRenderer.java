package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.materia.IVaporMultiblockController;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.block.entity.ManaEnrichedGlassBlockEntity;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ManaEnrichedGlassRenderer implements BlockEntityRenderer<ManaEnrichedGlassBlockEntity, ManaEnrichedGlassRenderer.GlassRenderState> {

    // FIXED: Standard syntax for ResourceLocation in 1.21.x
    private static final ResourceLocation GAS_ANIM_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/misc/fume_gas_anim.png");

    public ManaEnrichedGlassRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public GlassRenderState createRenderState() {
        return new GlassRenderState();
    }

    @Override
    public void extractRenderState(ManaEnrichedGlassBlockEntity be, GlassRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, renderState, crumblingOverlay);

        renderState.hasFume = false;
        renderState.fillRatio = 0.0f;
        Level level = be.getLevel();
        renderState.time = (level != null ? level.getGameTime() : 0) + partialTick;

        BlockPos controllerPos = be.getControllerPos();

        if (level != null && controllerPos != null) {
            BlockState controllerState = level.getBlockState(controllerPos);

            // NEW: Abort rendering entirely if the glass is part of a Pressure Chamber!
            // This leaves the glass perfectly clear so the custom particles can do the visual work.
            if (controllerState.is(ModBlocks.MATERIA_PRESSURE_CHAMBER_CONTROLLER.get())) {
                return;
            }

            BlockEntity controllerBE = level.getBlockEntity(controllerPos);

            if (controllerBE instanceof IVaporMultiblockController controller && controller.isFormed()) {
                MateriaFumusStack stored = controller.getStoredMateria();

                if (!stored.isEmpty()) {
                    renderState.hasFume = true;
                    renderState.colorInt = stored.getType().getColorInt();
                    // Changed to getSafeCapacity() to match the updated IVaporMultiblockController interface
                    renderState.fillRatio = Math.min(1.0f, Math.max(0.0f, (float) stored.getAmount() / controller.getSafeCapacity()));

                    BlockPos myPos = be.getBlockPos();
                    renderState.connectNorth = isConnected(level, controllerPos, myPos.north());
                    renderState.connectSouth = isConnected(level, controllerPos, myPos.south());
                    renderState.connectEast  = isConnected(level, controllerPos, myPos.east());
                    renderState.connectWest  = isConnected(level, controllerPos, myPos.west());
                    renderState.connectUp    = isConnected(level, controllerPos, myPos.above());
                    renderState.connectDown  = isConnected(level, controllerPos, myPos.below());
                }
            }
        }
    }

    private boolean isConnected(Level level, BlockPos controllerPos, BlockPos neighborPos) {
        BlockEntity neighbor = level.getBlockEntity(neighborPos);
        if (neighbor instanceof ManaEnrichedGlassBlockEntity glass) {
            return glass.getControllerPos() != null && glass.getControllerPos().equals(controllerPos);
        }
        return false;
    }

    @Override
    public void submit(GlassRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        // Because we hit 'return' during extraction for the Pressure Chamber,
        // hasFume will be strictly false here, naturally aborting the render pass!
        if (!renderState.hasFume || renderState.fillRatio <= 0.0f) return;

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(GAS_ANIM_TEXTURE));

        int colorInt = renderState.colorInt;
        float r = ((colorInt >> 16) & 0xFF) / 255.0f;
        float g = ((colorInt >> 8)  & 0xFF) / 255.0f;
        float b = (colorInt & 0xFF)          / 255.0f;

        float alpha = 0.15f + (0.75f * renderState.fillRatio);
        int light = 15728880;

        int frameCount = 32;
        float flowSpeed = 0.4f;
        int currentFrame = (int) (renderState.time * flowSpeed) % frameCount;
        float vMin = (float) currentFrame / frameCount;
        float vMax = (float) (currentFrame + 1) / frameCount;

        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();

        float gap = 0.0625f;

        float minX = renderState.connectWest  ? 0.0f : gap;
        float maxX = renderState.connectEast  ? 1.0f : 1.0f - gap;
        float minY = renderState.connectDown  ? 0.0f : gap;
        float maxY = renderState.connectUp    ? 1.0f : 1.0f - gap;
        float minZ = renderState.connectNorth ? 0.0f : gap;
        float maxZ = renderState.connectSouth ? 1.0f : 1.0f - gap;

        renderCuboid(pose, consumer, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, alpha, light, vMin, vMax);

        poseStack.popPose();
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

    public static class GlassRenderState extends BlockEntityRenderState {
        public boolean hasFume;
        public int colorInt;
        public float fillRatio;
        public float time;
        public boolean connectNorth, connectSouth, connectEast, connectWest, connectUp, connectDown;
    }
}