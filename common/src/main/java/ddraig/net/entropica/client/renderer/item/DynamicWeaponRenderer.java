package ddraig.net.entropica.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.component.VisWeaponState;
import ddraig.net.entropica.registry.ModDataComponents;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Set;

public class DynamicWeaponRenderer implements SpecialModelRenderer<VisWeaponState> {

    // ==========================================
    // WEAPON MODEL INTERFACE
    // ==========================================

    interface WeaponModel {
        void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                    int baseColor, int materialColor, int essenceColor, long time);
        ResourceLocation texture();
    }

    // ==========================================
    // SWORDS
    // ==========================================

    public static class GladiusModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/gladius.png");
        private final ModelPart handle, blade, materialLayer, essenceLayer;

        public GladiusModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 10.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("blade", CubeListBuilder.create().texOffs(0, 12).addBox(-1.0F, -22.0F, -0.5F, 2.0F, 14.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 10.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(8, 12).addBox(-1.0F, -22.0F, -0.5F, 2.0F, 14.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 32).bakeRoot();
            this.handle = baked.getChild("handle");
            this.blade = baked.getChild("blade");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            blade.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class LongswordModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/longsword.png");
        private final ModelPart handle, guard, blade, materialLayer, essenceLayer;

        public LongswordModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("guard", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -10.0F, -1.5F, 8.0F, 2.0F, 3.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("blade", CubeListBuilder.create().texOffs(0, 21).addBox(-1.0F, -36.0F, -0.5F, 2.0F, 26.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 21).addBox(-1.0F, -36.0F, -0.5F, 2.0F, 26.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 48).bakeRoot();
            this.handle = baked.getChild("handle");
            this.guard = baked.getChild("guard");
            this.blade = baked.getChild("blade");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            guard.render(poseStack, consumer, light, overlay, materialColor);
            blade.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class ShortswordModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/shortsword.png");
        private final ModelPart handle, blade, materialLayer, essenceLayer;

        public ShortswordModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 10.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("blade", CubeListBuilder.create().texOffs(0, 12).addBox(-1.0F, -18.0F, -0.5F, 2.0F, 10.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 10.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(8, 12).addBox(-1.0F, -18.0F, -0.5F, 2.0F, 10.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 32).bakeRoot();
            this.handle = baked.getChild("handle");
            this.blade = baked.getChild("blade");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            blade.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class AkrafenaModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/akrafena.png");
        private final ModelPart handle, blade, wingLeft, wingRight, materialLayer, essenceLayer;

        public AkrafenaModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("blade", CubeListBuilder.create().texOffs(0, 14).addBox(-1.5F, -32.0F, -0.5F, 3.0F, 24.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(0, 39).addBox(-4.0F, -12.0F, -0.5F, 3.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(8, 39).addBox(1.0F, -12.0F, -0.5F, 3.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 14).addBox(-1.5F, -32.0F, -0.5F, 3.0F, 24.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 48).bakeRoot();
            this.handle = baked.getChild("handle");
            this.blade = baked.getChild("blade");
            this.wingLeft = baked.getChild("wing_left");
            this.wingRight = baked.getChild("wing_right");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            blade.render(poseStack, consumer, light, overlay, baseColor);
            wingLeft.render(poseStack, consumer, light, overlay, materialColor);
            wingRight.render(poseStack, consumer, light, overlay, materialColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    // ==========================================
    // AXES
    // ==========================================

    public static class HandAxeModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/hand_axe.png");
        private final ModelPart handle, head, materialLayer, essenceLayer;

        public HandAxeModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 16.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, -18.0F, -1.0F, 6.0F, 8.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 16.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 18).addBox(-3.0F, -18.0F, -1.0F, 6.0F, 8.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 32).bakeRoot();
            this.handle = baked.getChild("handle");
            this.head = baked.getChild("head");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            head.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class WarAxeModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/war_axe.png");
        private final ModelPart handle, headMain, headBack, materialLayer, essenceLayer;

        public WarAxeModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("head_main", CubeListBuilder.create().texOffs(0, 20).addBox(-5.0F, -20.0F, -1.5F, 8.0F, 10.0F, 3.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("head_back", CubeListBuilder.create().texOffs(0, 33).addBox(3.0F, -18.0F, -1.0F, 3.0F, 6.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 20).addBox(-5.0F, -20.0F, -1.5F, 8.0F, 10.0F, 3.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 48).bakeRoot();
            this.handle = baked.getChild("handle");
            this.headMain = baked.getChild("head_main");
            this.headBack = baked.getChild("head_back");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            headMain.render(poseStack, consumer, light, overlay, baseColor);
            headBack.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class PoleAxeModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/pole_axe.png");
        private final ModelPart handle, head, spike, materialLayer, essenceLayer;

        public PoleAxeModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 22.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 24).addBox(-5.0F, -22.0F, -1.0F, 8.0F, 10.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("spike", CubeListBuilder.create().texOffs(0, 36).addBox(-0.5F, -30.0F, -0.5F, 1.0F, 8.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 22.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 24).addBox(-5.0F, -22.0F, -1.0F, 8.0F, 10.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 48).bakeRoot();
            this.handle = baked.getChild("handle");
            this.head = baked.getChild("head");
            this.spike = baked.getChild("spike");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            head.render(poseStack, consumer, light, overlay, baseColor);
            spike.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class HalberdModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/halberd.png");
        private final ModelPart handle, axeHead, spike, hook, materialLayer, essenceLayer;

        public HalberdModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 24.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("axe_head", CubeListBuilder.create().texOffs(0, 26).addBox(-6.0F, -24.0F, -1.0F, 8.0F, 10.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("spike", CubeListBuilder.create().texOffs(0, 38).addBox(-0.5F, -34.0F, -0.5F, 1.0F, 10.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("hook", CubeListBuilder.create().texOffs(4, 38).addBox(2.0F, -20.0F, -0.5F, 3.0F, 3.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 24.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 26).addBox(-6.0F, -24.0F, -1.0F, 8.0F, 10.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 48).bakeRoot();
            this.handle = baked.getChild("handle");
            this.axeHead = baked.getChild("axe_head");
            this.spike = baked.getChild("spike");
            this.hook = baked.getChild("hook");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            axeHead.render(poseStack, consumer, light, overlay, baseColor);
            spike.render(poseStack, consumer, light, overlay, baseColor);
            hook.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class BeardAxeModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/beard_axe.png");
        private final ModelPart handle, head, beard, materialLayer, essenceLayer;

        public BeardAxeModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 20).addBox(-5.0F, -20.0F, -1.0F, 7.0F, 7.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("beard", CubeListBuilder.create().texOffs(0, 29).addBox(-5.0F, -13.0F, -1.0F, 4.0F, 5.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 20).addBox(-5.0F, -20.0F, -1.0F, 7.0F, 7.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 48).bakeRoot();
            this.handle = baked.getChild("handle");
            this.head = baked.getChild("head");
            this.beard = baked.getChild("beard");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            head.render(poseStack, consumer, light, overlay, baseColor);
            beard.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    // ==========================================
    // BOWS
    // ==========================================

    public static class ShortbowModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/shortbow.png");
        private final ModelPart limbs, string, materialLayer, essenceLayer;

        public ShortbowModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("limbs", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 20.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.15F));
            root.addOrReplaceChild("string", CubeListBuilder.create().texOffs(8, 0).addBox(-0.01F, -16.0F, -2.0F, 0.01F, 20.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 20.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.15F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 20).addBox(-0.01F, -16.0F, -2.0F, 0.01F, 20.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 32).bakeRoot();
            this.limbs = baked.getChild("limbs");
            this.string = baked.getChild("string");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            limbs.render(poseStack, consumer, light, overlay, baseColor);
            string.render(poseStack, consumer, light, overlay, essenceColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class LongbowModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/longbow.png");
        private final ModelPart limbTop, limbBottom, string, materialLayer, essenceLayer;

        public LongbowModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("limb_top", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, -0.2F));
            root.addOrReplaceChild("limb_bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 10.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.2F));
            root.addOrReplaceChild("string", CubeListBuilder.create().texOffs(8, 0).addBox(-0.01F, -24.0F, -2.0F, 0.01F, 48.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, -0.2F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(8, 0).addBox(-0.01F, -24.0F, -2.0F, 0.01F, 48.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 48).bakeRoot();
            this.limbTop = baked.getChild("limb_top");
            this.limbBottom = baked.getChild("limb_bottom");
            this.string = baked.getChild("string");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            limbTop.render(poseStack, consumer, light, overlay, baseColor);
            limbBottom.render(poseStack, consumer, light, overlay, baseColor);
            string.render(poseStack, consumer, light, overlay, essenceColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class CrossbowModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/crossbow.png");
        private final ModelPart stock, prod, string, materialLayer, essenceLayer;

        public CrossbowModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("stock", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("prod", CubeListBuilder.create().texOffs(0, 20).addBox(-8.0F, -12.0F, -1.0F, 16.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("string", CubeListBuilder.create().texOffs(0, 24).addBox(-8.0F, -11.0F, -2.0F, 16.0F, 0.01F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 20).addBox(-8.0F, -12.0F, -1.0F, 16.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 32).bakeRoot();
            this.stock = baked.getChild("stock");
            this.prod = baked.getChild("prod");
            this.string = baked.getChild("string");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            stock.render(poseStack, consumer, light, overlay, baseColor);
            prod.render(poseStack, consumer, light, overlay, baseColor);
            string.render(poseStack, consumer, light, overlay, essenceColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class RepeaterModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/repeater.png");
        private final ModelPart stock, prod, magazine, string, materialLayer, essenceLayer;

        public RepeaterModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("stock", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -8.0F, -1.5F, 3.0F, 18.0F, 3.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("prod", CubeListBuilder.create().texOffs(0, 21).addBox(-8.0F, -12.0F, -1.0F, 16.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("magazine", CubeListBuilder.create().texOffs(0, 25).addBox(-1.5F, -8.0F, -3.5F, 3.0F, 6.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("string", CubeListBuilder.create().texOffs(0, 33).addBox(-8.0F, -11.0F, -2.0F, 16.0F, 0.01F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.5F, -8.0F, -1.5F, 3.0F, 18.0F, 3.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 21).addBox(-8.0F, -12.0F, -1.0F, 16.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 48).bakeRoot();
            this.stock = baked.getChild("stock");
            this.prod = baked.getChild("prod");
            this.magazine = baked.getChild("magazine");
            this.string = baked.getChild("string");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            stock.render(poseStack, consumer, light, overlay, baseColor);
            prod.render(poseStack, consumer, light, overlay, baseColor);
            magazine.render(poseStack, consumer, light, overlay, baseColor);
            string.render(poseStack, consumer, light, overlay, essenceColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class WarBowModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/war_bow.png");
        private final ModelPart limbTop, limbBottom, grip, string, materialLayer, essenceLayer;

        public WarBowModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("limb_top", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -28.0F, -1.5F, 3.0F, 18.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, -0.25F));
            root.addOrReplaceChild("limb_bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 10.0F, -1.5F, 3.0F, 18.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.25F));
            root.addOrReplaceChild("grip", CubeListBuilder.create().texOffs(0, 21).addBox(-2.0F, -10.0F, -2.0F, 4.0F, 20.0F, 4.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("string", CubeListBuilder.create().texOffs(10, 0).addBox(-0.01F, -28.0F, -3.0F, 0.01F, 56.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.5F, -28.0F, -1.5F, 3.0F, 18.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, -0.25F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(10, 0).addBox(-0.01F, -28.0F, -3.0F, 0.01F, 56.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 64).bakeRoot();
            this.limbTop = baked.getChild("limb_top");
            this.limbBottom = baked.getChild("limb_bottom");
            this.grip = baked.getChild("grip");
            this.string = baked.getChild("string");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            limbTop.render(poseStack, consumer, light, overlay, baseColor);
            limbBottom.render(poseStack, consumer, light, overlay, baseColor);
            grip.render(poseStack, consumer, light, overlay, baseColor);
            string.render(poseStack, consumer, light, overlay, essenceColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    // ==========================================
    // TOOLS
    // ==========================================

    public static class PickaxeModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/pickaxe.png");
        private final ModelPart handle, head, materialLayer, essenceLayer;

        public PickaxeModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 20).addBox(-8.0F, -16.0F, -1.0F, 16.0F, 4.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 20).addBox(-8.0F, -16.0F, -1.0F, 16.0F, 4.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 32).bakeRoot();
            this.handle = baked.getChild("handle");
            this.head = baked.getChild("head");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            head.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class ShovelModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/shovel.png");
        private final ModelPart handle, head, materialLayer, essenceLayer;

        public ShovelModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 20).addBox(-2.5F, -22.0F, -0.5F, 5.0F, 12.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 20).addBox(-2.5F, -22.0F, -0.5F, 5.0F, 12.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 32).bakeRoot();
            this.handle = baked.getChild("handle");
            this.head = baked.getChild("head");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            head.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class AdzeModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/adze.png");
        private final ModelPart handle, blade, materialLayer, essenceLayer;

        public AdzeModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("blade", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0F, -16.0F, -2.0F, 8.0F, 4.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 20).addBox(-4.0F, -16.0F, -2.0F, 8.0F, 4.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 32).bakeRoot();
            this.handle = baked.getChild("handle");
            this.blade = baked.getChild("blade");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            blade.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class PaxelModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/paxel.png");
        private final ModelPart handle, axeHead, pickHead, shovelHead, materialLayer, essenceLayer;

        public PaxelModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("axe_head", CubeListBuilder.create().texOffs(0, 20).addBox(-5.0F, -20.0F, -1.5F, 7.0F, 6.0F, 3.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("pick_head", CubeListBuilder.create().texOffs(0, 29).addBox(-5.0F, -14.0F, -1.0F, 10.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("shovel_head", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0F, -26.0F, -0.5F, 4.0F, 6.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 18.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 20).addBox(-5.0F, -20.0F, -1.5F, 7.0F, 6.0F, 3.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 48).bakeRoot();
            this.handle = baked.getChild("handle");
            this.axeHead = baked.getChild("axe_head");
            this.pickHead = baked.getChild("pick_head");
            this.shovelHead = baked.getChild("shovel_head");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            axeHead.render(poseStack, consumer, light, overlay, baseColor);
            pickHead.render(poseStack, consumer, light, overlay, baseColor);
            shovelHead.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    // ==========================================
    // OTHERS
    // ==========================================

    public static class SpearModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/spear.png");
        private final ModelPart shaft, tip, materialLayer, essenceLayer;

        public SpearModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("shaft", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 28.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(0, 29).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 6.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(8, 0).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 28.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(8, 29).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 6.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 16, 48).bakeRoot();
            this.shaft = baked.getChild("shaft");
            this.tip = baked.getChild("tip");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            shaft.render(poseStack, consumer, light, overlay, baseColor);
            tip.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class MaceModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/mace.png");
        private final ModelPart handle, head, flangeA, flangeB, flangeC, flangeD, materialLayer, essenceLayer;

        public MaceModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 16.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, -16.0F, -3.0F, 6.0F, 8.0F, 6.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("flange_a", CubeListBuilder.create().texOffs(0, 32).addBox(-4.0F, -15.0F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("flange_b", CubeListBuilder.create().texOffs(0, 32).addBox(3.0F, -15.0F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("flange_c", CubeListBuilder.create().texOffs(0, 32).addBox(-0.5F, -15.0F, -4.0F, 1.0F, 6.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("flange_d", CubeListBuilder.create().texOffs(0, 32).addBox(-0.5F, -15.0F, 3.0F, 1.0F, 6.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 16.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 18).addBox(-3.0F, -16.0F, -3.0F, 6.0F, 8.0F, 6.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 48).bakeRoot();
            this.handle = baked.getChild("handle");
            this.head = baked.getChild("head");
            this.flangeA = baked.getChild("flange_a");
            this.flangeB = baked.getChild("flange_b");
            this.flangeC = baked.getChild("flange_c");
            this.flangeD = baked.getChild("flange_d");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            head.render(poseStack, consumer, light, overlay, baseColor);
            flangeA.render(poseStack, consumer, light, overlay, materialColor);
            flangeB.render(poseStack, consumer, light, overlay, materialColor);
            flangeC.render(poseStack, consumer, light, overlay, materialColor);
            flangeD.render(poseStack, consumer, light, overlay, materialColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class MorningStarModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/morning_star.png");
        private final ModelPart handle, head, spikeTop, spikeA, spikeB, spikeC, spikeD, materialLayer, essenceLayer;

        public MorningStarModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 16.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, -16.0F, -3.0F, 6.0F, 8.0F, 6.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("spike_top", CubeListBuilder.create().texOffs(0, 32).addBox(-0.5F, -20.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("spike_a", CubeListBuilder.create().texOffs(4, 32).addBox(-5.0F, -14.0F, -0.5F, 2.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("spike_b", CubeListBuilder.create().texOffs(4, 32).addBox(3.0F, -14.0F, -0.5F, 2.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("spike_c", CubeListBuilder.create().texOffs(4, 32).addBox(-0.5F, -14.0F, -5.0F, 1.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("spike_d", CubeListBuilder.create().texOffs(4, 32).addBox(-0.5F, -14.0F, 3.0F, 1.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 16.0F, 2.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(16, 18).addBox(-3.0F, -16.0F, -3.0F, 6.0F, 8.0F, 6.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 32, 48).bakeRoot();
            this.handle = baked.getChild("handle");
            this.head = baked.getChild("head");
            this.spikeTop = baked.getChild("spike_top");
            this.spikeA = baked.getChild("spike_a");
            this.spikeB = baked.getChild("spike_b");
            this.spikeC = baked.getChild("spike_c");
            this.spikeD = baked.getChild("spike_d");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            head.render(poseStack, consumer, light, overlay, baseColor);
            spikeTop.render(poseStack, consumer, light, overlay, materialColor);
            spikeA.render(poseStack, consumer, light, overlay, materialColor);
            spikeB.render(poseStack, consumer, light, overlay, materialColor);
            spikeC.render(poseStack, consumer, light, overlay, materialColor);
            spikeD.render(poseStack, consumer, light, overlay, materialColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    public static class WarhammerModel implements WeaponModel {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/warhammer.png");
        private final ModelPart handle, head, materialLayer, essenceLayer;

        public WarhammerModel() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -8.0F, -1.5F, 3.0F, 22.0F, 3.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 25).addBox(-5.0F, -18.0F, -5.0F, 10.0F, 10.0F, 10.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("material_layer", CubeListBuilder.create().texOffs(22, 0).addBox(-1.5F, -8.0F, -1.5F, 3.0F, 22.0F, 3.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            root.addOrReplaceChild("essence_layer", CubeListBuilder.create().texOffs(22, 25).addBox(-5.0F, -18.0F, -5.0F, 10.0F, 10.0F, 10.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
            ModelPart baked = LayerDefinition.create(mesh, 48, 48).bakeRoot();
            this.handle = baked.getChild("handle");
            this.head = baked.getChild("head");
            this.materialLayer = baked.getChild("material_layer");
            this.essenceLayer = baked.getChild("essence_layer");
        }

        @Override
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                           int baseColor, int materialColor, int essenceColor, long time) {
            handle.render(poseStack, consumer, light, overlay, baseColor);
            head.render(poseStack, consumer, light, overlay, baseColor);
            materialLayer.render(poseStack, consumer, light, overlay, materialColor);
            essenceLayer.render(poseStack, consumer, light, overlay, essenceColor);
        }

        @Override public ResourceLocation texture() { return TEXTURE; }
    }

    // ==========================================
    // SHARED MODIFIER PARTS
    // ==========================================

    private static final ResourceLocation MODIFIER_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/item/weapon/modifiers.png");

    private final ModelPart modWings;
    private final ModelPart modSpellGem;
    private final ModelPart modPhantomHalo;
    private final ModelPart attachmentStud;

    // ==========================================
    // WEAPON REGISTRY
    // ==========================================

    private final Map<String, WeaponModel> weaponModels;

    public DynamicWeaponRenderer() {
        MeshDefinition modMesh = new MeshDefinition();
        PartDefinition modRoot = modMesh.getRoot();
        modRoot.addOrReplaceChild("mod_wings", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -9.0F, -0.5F, 6.0F, 1.0F, 1.0F).texOffs(0, 0).addBox(4.0F, -9.0F, -0.5F, 6.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
        modRoot.addOrReplaceChild("mod_spell_gem", CubeListBuilder.create().texOffs(0, 2).addBox(-1.5F, -13.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
        modRoot.addOrReplaceChild("mod_phantom_halo", CubeListBuilder.create().texOffs(0, 7).addBox(-6.0F, -21.0F, -6.0F, 12.0F, 1.0F, 12.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
        modRoot.addOrReplaceChild("attachment_stud", CubeListBuilder.create().texOffs(0, 20).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
        ModelPart bakedMods = LayerDefinition.create(modMesh, 32, 32).bakeRoot();
        this.modWings = bakedMods.getChild("mod_wings");
        this.modSpellGem = bakedMods.getChild("mod_spell_gem");
        this.modPhantomHalo = bakedMods.getChild("mod_phantom_halo");
        this.attachmentStud = bakedMods.getChild("attachment_stud");

        this.weaponModels = Map.ofEntries(
                Map.entry("gladius", new GladiusModel()),
                Map.entry("longsword", new LongswordModel()),
                Map.entry("shortsword", new ShortswordModel()),
                Map.entry("akrafena", new AkrafenaModel()),
                Map.entry("hand_axe", new HandAxeModel()),
                Map.entry("war_axe", new WarAxeModel()),
                Map.entry("pole_axe", new PoleAxeModel()),
                Map.entry("halberd", new HalberdModel()),
                Map.entry("beard_axe", new BeardAxeModel()),
                Map.entry("shortbow", new ShortbowModel()),
                Map.entry("longbow", new LongbowModel()),
                Map.entry("crossbow", new CrossbowModel()),
                Map.entry("repeater", new RepeaterModel()),
                Map.entry("war_bow", new WarBowModel()),
                Map.entry("pickaxe", new PickaxeModel()),
                Map.entry("shovel", new ShovelModel()),
                Map.entry("adze", new AdzeModel()),
                Map.entry("paxel", new PaxelModel()),
                Map.entry("spear", new SpearModel()),
                Map.entry("mace", new MaceModel()),
                Map.entry("morning_star", new MorningStarModel()),
                Map.entry("warhammer", new WarhammerModel())
        );
    }

    // ==========================================
    // SPECIAL MODEL RENDERER INTERFACE
    // ==========================================

    @Override
    public void getExtents(Set<Vector3f> extents) {
        extents.add(new Vector3f(-1.0f, -1.0f, -1.0f));
        extents.add(new Vector3f(1.0f, 1.0f, 1.0f));
    }

    @Override
    public @Nullable VisWeaponState extractArgument(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.VIS_WEAPON_STATE.get(), VisWeaponState.EMPTY);
    }

    @Override
    public void submit(@Nullable VisWeaponState state, ItemDisplayContext context, PoseStack poseStack,
                       SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int seed) {
        MultiBufferSource.BufferSource bufferSource = net.minecraft.client.Minecraft.getInstance().renderBuffers().bufferSource();

        if (state == null) state = VisWeaponState.EMPTY;

        String profile = state.activeProfile() != null ? state.activeProfile().toLowerCase() : "longsword";
        WeaponModel weapon = weaponModels.getOrDefault(profile, weaponModels.get("longsword"));

        float[] matColor = getMaterialColor(state.materialTier());
        float[] coreColor = getEssenceColor(state.baseType());
        int baseColor = packColor(1.0f, 1.0f, 1.0f, 1.0f);
        int materialColor = packColor(1.0f, matColor[0], matColor[1], matColor[2]);
        int essenceColor = packColor(1.0f, coreColor[0], coreColor[1], coreColor[2]);

        long time = System.currentTimeMillis();

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180));
        poseStack.scale(0.04f, 0.04f, 0.04f);

        // Render weapon with its own texture
        VertexConsumer weaponConsumer = bufferSource.getBuffer(RenderType.entityCutout(weapon.texture()));
        weapon.render(poseStack, weaponConsumer, light, overlay, baseColor, materialColor, essenceColor, time);

        // Render shared modifiers with modifier texture
        VertexConsumer modConsumer = bufferSource.getBuffer(RenderType.entityCutout(MODIFIER_TEXTURE));

        if (state.bonusSpeed() > 0) {
            modWings.render(poseStack, modConsumer, light, overlay, packColor(1.0f, 0.9f, 0.9f, 0.9f));
        }

        if (!state.activeSpell().equals("none")) {
            poseStack.pushPose();
            float spinRot = (time % 4000L) / 4000.0f * 360.0f;
            float hoverOffset = (float) Math.sin((time % 2000L) / 2000.0f * Math.PI * 2) * 1.0f;
            poseStack.translate(0, hoverOffset, 0);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(spinRot));
            modSpellGem.render(poseStack, modConsumer, light, overlay, packColor(1.0f, 0.8f, 0.2f, 1.0f));
            poseStack.popPose();
        }

        if (state.isAutonomous()) {
            poseStack.pushPose();
            float haloSpin = (time % 6000L) / 6000.0f * 360.0f;
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-haloSpin));
            modPhantomHalo.render(poseStack, modConsumer, light, overlay, packColor(0.8f, 0.6f, 0.6f, 1.0f));
            poseStack.popPose();
        }

        if (!state.slottedFragments().isEmpty()) {
            int count = state.slottedFragments().size();
            for (int i = 0; i < count; i++) {
                EssenceType fragType = state.slottedFragments().get(i);
                float[] fragColor = getEssenceColor(fragType);
                int packedFragColor = packColor(1.0f, fragColor[0], fragColor[1], fragColor[2]);

                poseStack.pushPose();
                float xOffset = (i - (count - 1) / 2.0f) * 3.0f;
                poseStack.translate(xOffset, -8.5f, -2.0f);
                attachmentStud.render(poseStack, modConsumer, light, overlay, packedFragColor);
                poseStack.popPose();
            }
        }

        poseStack.popPose();
        bufferSource.endBatch();
    }

    // ==========================================
    // COLOR HELPERS
    // ==========================================

    private int packColor(float a, float r, float g, float b) {
        return ((int) (a * 255.0F) << 24) | ((int) (r * 255.0F) << 16) | ((int) (g * 255.0F) << 8) | (int) (b * 255.0F);
    }

    private float[] getMaterialColor(String material) {
        if (material == null) return new float[]{0.8f, 0.8f, 0.8f};
        String lower = material.toLowerCase();
        if (lower.contains("viscanite")) return new float[]{1.0f, 0.35f, 0.0f};
        if (lower.contains("resonite")) return new float[]{1.0f, 0.85f, 0.0f};
        if (lower.contains("eidolite")) return new float[]{0.6f, 0.6f, 1.0f};
        return new float[]{0.0f, 1.0f, 1.0f};
    }

    private float[] getEssenceColor(EssenceType type) {
        if (type == null) return new float[]{1.0f, 1.0f, 1.0f};
        int color = type.getColorInt();
        return new float[]{
                ((color >> 16) & 0xFF) / 255.0f,
                ((color >> 8) & 0xFF) / 255.0f,
                (color & 0xFF) / 255.0f
        };
    }

    // ==========================================
    // BAKING REGISTRY
    // ==========================================

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() { return MAP_CODEC; }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
            return new DynamicWeaponRenderer();
        }
    }
}