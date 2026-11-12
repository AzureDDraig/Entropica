package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class EidolicShadowModel extends EntityModel<EidolicShadowRenderer.ShadowRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("entropica", "eidolic_shadow"), "main");

    public final ModelPart body;
    public final ModelPart head;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;

    public EidolicShadowModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // MASSIVE HUNCHED TORSO (Base Rot: 0.35F)
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-9.0F, -12.0F, -6.0F, 18.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 28).addBox(-6.0F, 4.0F, -4.0F, 12.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.35F, 0.0F, 0.0F));

        // SMALL HEAD SUNKEN INTO SHOULDERS
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(60, 0).addBox(-4.0F, -6.0F, -6.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -10.0F, -6.0F));

        // MASSIVE RIGHT ARM (Base Rot Z: 0.15F)
        PartDefinition rightArm = body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(60, 16).addBox(-6.0F, -2.0F, -4.0F, 6.0F, 14.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-9.0F, -9.0F, 0.0F, 0.0F, 0.0F, 0.15F));

        // RIGHT GAUNTLET & CLAWS
        PartDefinition rightGauntlet = rightArm.addOrReplaceChild("right_gauntlet", CubeListBuilder.create()
                        .texOffs(88, 16).addBox(-3.5F, 0.0F, -4.5F, 7.0F, 12.0F, 9.0F, new CubeDeformation(0.2F))
                        .texOffs(0, 60).addBox(-2.5F, 12.0F, -4.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 60).addBox(0.5F, 12.0F, -4.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 60).addBox(2.5F, 10.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 12.0F, 0.0F));

        // MASSIVE LEFT ARM (Mirrored, Base Rot Z: -0.15F)
        PartDefinition leftArm = body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(60, 16).mirror().addBox(0.0F, -2.0F, -4.0F, 6.0F, 14.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(9.0F, -9.0F, 0.0F, 0.0F, 0.0F, -0.15F));

        // LEFT GAUNTLET & CLAWS (Mirrored)
        PartDefinition leftGauntlet = leftArm.addOrReplaceChild("left_gauntlet", CubeListBuilder.create()
                        .texOffs(88, 16).mirror().addBox(-3.5F, 0.0F, -4.5F, 7.0F, 12.0F, 9.0F, new CubeDeformation(0.2F)).mirror(false)
                        .texOffs(0, 60).mirror().addBox(0.5F, 12.0F, -4.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                        .texOffs(0, 60).mirror().addBox(-2.5F, 12.0F, -4.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                        .texOffs(8, 60).mirror().addBox(-4.5F, 10.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(3.0F, 12.0F, 0.0F));

        // THICK STOMPING LEGS
        PartDefinition rightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(32, 44).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.0F, 12.0F, 0.0F));

        PartDefinition leftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(32, 44).mirror().addBox(-3.0F, 0.0F, -3.0F, 6.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(4.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(EidolicShadowRenderer.ShadowRenderState state) {
        float walkDist = state.walkDist;
        float ageInTicks = state.ageInTicks;
        float walkSpeed = 0.6F;

        // Massive, lumbering leg strides
        float legSwing = Mth.cos(walkDist * walkSpeed) * 1.0F;
        this.rightLeg.xRot = legSwing;
        this.leftLeg.xRot = -legSwing;

        // Heavy body bobbing and shifting weight
        this.body.y = 10.0F + Math.abs(Mth.sin(walkDist * walkSpeed)) * 2.0F;
        // Maintain the 0.35F base hunch, and add breathing/walking weight shifting
        this.body.xRot = 0.35F + Mth.cos(walkDist * walkSpeed * 2.0F) * 0.05F;

        // Head looking around while bobbing
        this.head.xRot = Mth.sin(ageInTicks * 0.1F) * 0.05F;
        this.head.yRot = Mth.cos(ageInTicks * 0.05F) * 0.05F;

        float armSwing = Mth.cos(walkDist * walkSpeed) * 0.8F;

        if (state.hasItem) {
            // Lock the Right Arm forward to securely clutch the payload!
            this.rightArm.xRot = -1.2F;
            this.rightArm.yRot = -0.2F;
            this.rightArm.zRot = 0.15F; // Maintain base outward flare

            // Left Arm swings normally
            this.leftArm.xRot = armSwing;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = -0.15F; // Maintain base outward flare
        } else {
            // Both arms swing in opposition to the legs
            this.rightArm.xRot = -armSwing;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.15F;

            this.leftArm.xRot = armSwing;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = -0.15F;
        }
    }
}