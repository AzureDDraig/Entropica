package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.block.ModChestBlock;
import ddraig.net.entropica.block.entity.ModChestBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.HashMap;
import java.util.Map;

public class ModChestRenderer extends ChestRenderer<ModChestBlockEntity> {
    private static final Map<String, RenderType[]> RENDER_TYPE_CACHE = new HashMap<>();

    private final ChestModel singleModel;
    private final ChestModel doubleLeftModel;
    private final ChestModel doubleRightModel;

    public ModChestRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.singleModel = new ChestModel(context.bakeLayer(ModelLayers.CHEST));
        this.doubleLeftModel = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT));
        this.doubleRightModel = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT));
    }

    @Override
    public void submit(ChestRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraState) {
        if (renderState.blockState != null && renderState.blockState.getBlock() instanceof ModChestBlock chestBlock) {
            String woodType = chestBlock.getWoodType();
            RenderType[] renderTypes = RENDER_TYPE_CACHE.computeIfAbsent(woodType, key -> new RenderType[]{
                RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath("entropica", "textures/entity/chest/" + key + ".png")),
                RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath("entropica", "textures/entity/chest/" + key + "_left.png")),
                RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath("entropica", "textures/entity/chest/" + key + "_right.png"))
            });

            ChestType chestType = renderState.type != null ? renderState.type : ChestType.SINGLE;
            RenderType renderType = switch (chestType) {
                case LEFT -> renderTypes[1];
                case RIGHT -> renderTypes[2];
                default -> renderTypes[0];
            };

            poseStack.pushPose();
            BlockState state = renderState.blockState;
            Direction facing = state.hasProperty(ChestBlock.FACING) ? state.getValue(ChestBlock.FACING) : Direction.SOUTH;
            float rotation = facing.toYRot();
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
            poseStack.translate(-0.5D, -0.5D, -0.5D);

            float openProgress = 1.0F - renderState.open;
            openProgress = 1.0F - openProgress * openProgress * openProgress;

            ChestModel model = switch (chestType) {
                case LEFT -> this.doubleLeftModel;
                case RIGHT -> this.doubleRightModel;
                default -> this.singleModel;
            };

            nodeCollector.submitModel(
                model,
                openProgress,
                poseStack,
                renderType,
                renderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                null,
                0,
                renderState.breakProgress
            );

            poseStack.popPose();
            return;
        }

        super.submit(renderState, poseStack, nodeCollector, cameraState);
    }
}
