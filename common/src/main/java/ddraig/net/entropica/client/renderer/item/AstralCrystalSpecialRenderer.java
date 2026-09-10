package ddraig.net.entropica.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.client.renderer.AstralCrystalMesh;
import ddraig.net.entropica.item.AstralCrystalItem;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Set;

/**
 * AstralCrystalSpecialRenderer — SpecialModelRenderer for Astral Crystals in GUIs, player hands,
 * pedestals, and ground drops. Emits true 3D procedural faceted crystal geometry.
 */
public class AstralCrystalSpecialRenderer implements SpecialModelRenderer<AstralCrystalSpecialRenderer.CrystalItemState> {

    public record CrystalItemState(int stage, int size) {
        public static final CrystalItemState DEFAULT = new CrystalItemState(0, 1);
    }

    @Override
    public void getExtents(Set<Vector3f> extents) {
        extents.add(new Vector3f(-0.5f, -0.5f, -0.5f));
        extents.add(new Vector3f(1.5f, 1.5f, 1.5f));
    }

    @Override
    public @Nullable CrystalItemState extractArgument(ItemStack stack) {
        if (stack.is(ModItems.ASTRAL_CRYSTAL.get())) {
            return new CrystalItemState(0, AstralCrystalItem.getSize(stack));
        } else if (stack.is(ModBlocks.SMALL_ASTRAL_CRYSTAL_BUD.get().asItem())) {
            return new CrystalItemState(1, 1);
        } else if (stack.is(ModBlocks.MEDIUM_ASTRAL_CRYSTAL_BUD.get().asItem())) {
            return new CrystalItemState(2, 1);
        } else if (stack.is(ModBlocks.LARGE_ASTRAL_CRYSTAL_BUD.get().asItem())) {
            return new CrystalItemState(3, 1);
        } else if (stack.is(ModBlocks.ASTRAL_CRYSTAL_CLUSTER.get().asItem())) {
            return new CrystalItemState(4, 1);
        }
        return CrystalItemState.DEFAULT;
    }

    @Override
    public void submit(@Nullable CrystalItemState state, ItemDisplayContext context, PoseStack poseStack,
                       SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int seed) {
        if (state == null) state = CrystalItemState.DEFAULT;

        poseStack.pushPose();

        switch (context) {
            case GUI -> {
                poseStack.translate(0.5, 0.5, 0.5);
                if (state.stage() == 0) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(25.0f));
                    poseStack.mulPose(Axis.YP.rotationDegrees(45.0f));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(-15.0f));
                    poseStack.scale(0.85f, 0.85f, 0.85f);
                } else {
                    poseStack.translate(0.0, -0.22, 0.0);
                    poseStack.mulPose(Axis.XP.rotationDegrees(30.0f));
                    poseStack.mulPose(Axis.YP.rotationDegrees(45.0f));
                    poseStack.scale(0.70f, 0.70f, 0.70f);
                }
            }
            case GROUND -> {
                poseStack.translate(0.5, 0.20, 0.5);
                poseStack.scale(0.65f, 0.65f, 0.65f);
            }
            case FIXED -> {
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
                poseStack.scale(0.65f, 0.65f, 0.65f);
            }
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                poseStack.translate(0.5, 0.40, 0.5);
                poseStack.mulPose(Axis.XP.rotationDegrees(15.0f));
                poseStack.mulPose(Axis.YP.rotationDegrees(35.0f));
                poseStack.scale(0.55f, 0.55f, 0.55f);
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                poseStack.translate(0.5, 0.35, 0.5);
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0f));
                poseStack.mulPose(Axis.YP.rotationDegrees(30.0f));
                poseStack.scale(0.50f, 0.50f, 0.50f);
            }
            default -> {
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(0.65f, 0.65f, 0.65f);
            }
        }

        double time = (double) (System.currentTimeMillis() % 100000L) * 0.05;
        final CrystalItemState curState = state;

        collector.submitCustomGeometry(poseStack, AstralCrystalMesh.getRenderType(), (pose, consumer) -> {
            if (curState.stage() == 0) {
                AstralCrystalMesh.renderGem(pose.pose(), consumer, curState.size(), light, overlay, time);
            } else {
                AstralCrystalMesh.renderCluster(pose.pose(), consumer, curState.stage(), light, overlay, time);
            }
        });

        poseStack.popPose();
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
            return new AstralCrystalSpecialRenderer();
        }
    }
}
