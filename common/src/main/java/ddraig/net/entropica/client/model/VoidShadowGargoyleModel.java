package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.VoidShadowGargoyleAnimations;
import ddraig.net.entropica.client.renderer.VoidShadowGargoyleRenderState;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Fauna 51 — Void Shadow-Gargoyle (Rift Sentinel).
 * Exact conversion of all 68 cubes, 56 bone groups, and 128x128 UVs from void_shadow_gargoyle.bbmodel.
 */
public class VoidShadowGargoyleModel extends EntityModel<VoidShadowGargoyleRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_shadow_gargoyle"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation flyAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation blinkOutAnimation;
    private final KeyframeAnimation blinkInAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public VoidShadowGargoyleModel(ModelPart modelRoot) {
        super(modelRoot, RenderType::entityCutoutNoCull);
        ModelPart root = modelRoot.getChild("root");
        ModelPart bodyGrp = root.getChild("body_grp");
        ModelPart chestPose = bodyGrp.getChild("waist_grp").getChild("waist_pose").getChild("chest_grp").getChild("chest_pose");
        this.head_grp = chestPose.getChild("neck_grp").getChild("neck_pose").getChild("head_grp");

        this.idleAnimation = VoidShadowGargoyleAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = VoidShadowGargoyleAnimations.WALK.bake(modelRoot);
        this.flyAnimation = VoidShadowGargoyleAnimations.FLY.bake(modelRoot);
        this.attackAnimation = VoidShadowGargoyleAnimations.ATTACK.bake(modelRoot);
        this.blinkOutAnimation = VoidShadowGargoyleAnimations.BLINK_OUT.bake(modelRoot);
        this.blinkInAnimation = VoidShadowGargoyleAnimations.BLINK_IN.bake(modelRoot);
        this.hurtAnimation = VoidShadowGargoyleAnimations.HURT.bake(modelRoot);
        this.deathAnimation = VoidShadowGargoyleAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_grp = root.addOrReplaceChild("body_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -13.039F, 2.0F));

        PartDefinition waist_grp = body_grp.addOrReplaceChild("waist_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition waist_pose = waist_grp.addOrReplaceChild("waist_pose", CubeListBuilder.create()
                .texOffs(87, 1).addBox(-4.0F, -6.0F, -3.0F, 8.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(62, 74).addBox(-3.0F, -1.4F, -3.6F, 6.0F, 3.0F, 3.0F, new CubeDeformation(-0.05F))
                .texOffs(58, 45).addBox(-1.0F, -3.5F, 2.2F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.05F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4887F, 0.0F, 0.0F));

        PartDefinition chest_grp = waist_pose.addOrReplaceChild("chest_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -5.2F, 0.0F));

        PartDefinition chest_pose = chest_grp.addOrReplaceChild("chest_pose", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-6.0F, -8.3F, -3.5F, 12.0F, 9.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(1, 71).addBox(-5.0F, -6.6F, -4.3F, 10.0F, 5.0F, 2.0F, new CubeDeformation(-0.04F))
                .texOffs(52, 55).addBox(-5.0F, -10.0F, -3.0F, 10.0F, 3.0F, 6.0F, new CubeDeformation(-0.05F))
                .texOffs(33, 1).addBox(-1.0F, -7.8F, 2.8F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.05F))
                .texOffs(116, 1).addBox(-1.0F, -4.0F, 2.8F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.05F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition neck_grp = chest_pose.addOrReplaceChild("neck_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -8.6F, -0.2F));

        PartDefinition neck_pose = neck_grp.addOrReplaceChild("neck_pose", CubeListBuilder.create().texOffs(35, 45).addBox(-3.0F, -4.0F, -2.5F, 6.0F, 5.0F, 5.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2793F, 0.0F, 0.0F));

        PartDefinition head_grp = neck_pose.addOrReplaceChild("head_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, -0.3F));

        PartDefinition head_pose = head_grp.addOrReplaceChild("head_pose", CubeListBuilder.create()
                .texOffs(40, 1).addBox(-4.0F, -7.0F, -4.7F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(44, 65).addBox(-3.0F, -4.0F, -9.7F, 6.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(65, 1).addBox(-1.0F, -5.7F, -9.7F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.04F))
                .texOffs(107, 65).addBox(-2.5F, -1.4F, -9.2F, 5.0F, 1.0F, 4.0F, new CubeDeformation(-0.06F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3142F, 0.0F, 0.0F));

        head_pose.addOrReplaceChild("brow_r_r1", CubeListBuilder.create().texOffs(117, 39).mirror().addBox(-1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(2.5F, -5.5F, -4.4F, 0.0F, 0.0F, 0.0698F));
        head_pose.addOrReplaceChild("brow_l_r1", CubeListBuilder.create().texOffs(117, 39).addBox(-1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(-2.5F, -5.5F, -4.4F, 0.0F, 0.0F, -0.0698F));
        head_pose.addOrReplaceChild("jaw_grp", CubeListBuilder.create().texOffs(39, 74).addBox(-3.0F, 0.0F, -5.0F, 6.0F, 2.0F, 5.0F, new CubeDeformation(-0.02F)), PartPose.offset(0.0F, -1.0F, -4.7F));
        head_pose.addOrReplaceChild("horn_l_grp", CubeListBuilder.create().texOffs(69, 32).addBox(-1.0F, -3.6F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(-3.0F, -6.4F, 0.7F, -0.5934F, 0.0F, 0.0F));
        head_pose.addOrReplaceChild("horn_r_grp", CubeListBuilder.create().texOffs(117, 32).addBox(-1.0F, -3.6F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(3.0F, -6.4F, 0.7F, -0.5934F, 0.0F, 0.0F));

        PartDefinition arm_l_grp = chest_pose.addOrReplaceChild("arm_l_grp", CubeListBuilder.create(), PartPose.offset(-8.0F, -6.6F, -0.2F));
        PartDefinition arm_l_pose = arm_l_grp.addOrReplaceChild("arm_l_pose", CubeListBuilder.create()
                .texOffs(85, 55).addBox(-2.5F, -2.8F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(-0.06F))
                .texOffs(1, 32).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.8378F, 0.0F, 0.0F));
        PartDefinition forearm_l_grp = arm_l_pose.addOrReplaceChild("forearm_l_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 7.2F, 0.0F));
        forearm_l_grp.addOrReplaceChild("forearm_l_pose", CubeListBuilder.create()
                .texOffs(35, 32).addBox(-2.0F, -0.4F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(-0.04F))
                .texOffs(87, 1).addBox(0.7F, 7.2F, -1.8F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.05F))
                .texOffs(87, 1).addBox(-0.5F, 7.2F, -1.8F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.05F))
                .texOffs(87, 1).addBox(-1.7F, 7.2F, -1.8F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.05F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

        PartDefinition arm_r_grp = chest_pose.addOrReplaceChild("arm_r_grp", CubeListBuilder.create(), PartPose.offset(8.0F, -6.6F, -0.2F));
        PartDefinition arm_r_pose = arm_r_grp.addOrReplaceChild("arm_r_pose", CubeListBuilder.create()
                .texOffs(106, 55).addBox(-2.5F, -2.8F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(-0.06F))
                .texOffs(18, 32).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.8378F, 0.0F, 0.0F));
        PartDefinition forearm_r_grp = arm_r_pose.addOrReplaceChild("forearm_r_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 7.2F, 0.0F));
        forearm_r_grp.addOrReplaceChild("forearm_r_pose", CubeListBuilder.create()
                .texOffs(52, 32).addBox(-2.0F, -0.4F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(-0.04F))
                .texOffs(87, 1).mirror().addBox(0.7F, 7.2F, -1.8F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.05F)).mirror(false)
                .texOffs(87, 1).mirror().addBox(-0.5F, 7.2F, -1.8F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.05F)).mirror(false)
                .texOffs(87, 1).mirror().addBox(-1.7F, 7.2F, -1.8F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.05F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

        PartDefinition wing_mount_grp = chest_pose.addOrReplaceChild("wing_mount_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -5.6F, 2.6F, -0.5934F, 0.0F, 0.0F));

        PartDefinition wing_shoulder_l_grp = wing_mount_grp.addOrReplaceChild("wing_shoulder_l_grp", CubeListBuilder.create(), PartPose.offset(-3.4F, 0.0F, 0.0F));
        PartDefinition wing_shoulder_l_pose = wing_shoulder_l_grp.addOrReplaceChild("wing_shoulder_l_pose", CubeListBuilder.create()
                .texOffs(81, 74).addBox(-12.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(116, 8).addBox(-12.8F, -2.0F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.04F))
                .texOffs(30, 18).addBox(-12.0F, 0.0F, -0.5F, 12.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.5934F, 0.4538F));
        wing_shoulder_l_pose.addOrReplaceChild("wing_rib1_l_r1", CubeListBuilder.create().texOffs(73, 1).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 12.0F, 2.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(-11.8F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

        PartDefinition wing_elbow_l_grp = wing_shoulder_l_pose.addOrReplaceChild("wing_elbow_l_grp", CubeListBuilder.create(), PartPose.offset(-11.8F, 0.0F, 0.0F));
        PartDefinition wing_elbow_l_pose = wing_elbow_l_grp.addOrReplaceChild("wing_elbow_l_pose", CubeListBuilder.create().texOffs(79, 79).addBox(-9.0F, -1.0F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.1745F, -0.6632F));
        wing_elbow_l_pose.addOrReplaceChild("wing_mem_mid_l_r1", CubeListBuilder.create().texOffs(86, 15).addBox(-9.5F, 1.4F, -0.44F, 13.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1047F));
        wing_elbow_l_pose.addOrReplaceChild("wing_rib2_l_r1", CubeListBuilder.create().texOffs(74, 84).addBox(-13.0F, -0.5F, -1.0F, 13.0F, 1.0F, 2.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7679F));

        PartDefinition wing_wrist_l_grp = wing_elbow_l_pose.addOrReplaceChild("wing_wrist_l_grp", CubeListBuilder.create(), PartPose.offset(-8.8F, 0.0F, 0.0F));
        PartDefinition wing_wrist_l_pose = wing_wrist_l_grp.addOrReplaceChild("wing_wrist_l_pose", CubeListBuilder.create()
                .texOffs(28, 82).addBox(-9.0F, -1.0F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(-0.04F))
                .texOffs(69, 43).addBox(-9.0F, 0.0F, -0.38F, 11.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.0698F, -0.0698F));
        wing_wrist_l_pose.addOrReplaceChild("wing_rib3_l_r1", CubeListBuilder.create().texOffs(32, 87).addBox(-11.0F, -0.5F, -1.0F, 11.0F, 1.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4189F));

        PartDefinition wing_shoulder_r_grp = wing_mount_grp.addOrReplaceChild("wing_shoulder_r_grp", CubeListBuilder.create(), PartPose.offset(3.4F, 0.0F, 0.0F));
        PartDefinition wing_shoulder_r_pose = wing_shoulder_r_grp.addOrReplaceChild("wing_shoulder_r_pose", CubeListBuilder.create()
                .texOffs(1, 79).addBox(0.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(115, 15).addBox(10.8F, -2.0F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.04F))
                .texOffs(57, 18).addBox(0.0F, 0.0F, -0.5F, 12.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, -0.5934F, -0.4538F));
        wing_shoulder_r_pose.addOrReplaceChild("wing_rib1_r_r1", CubeListBuilder.create().texOffs(80, 1).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 12.0F, 2.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(11.8F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

        PartDefinition wing_elbow_r_grp = wing_shoulder_r_pose.addOrReplaceChild("wing_elbow_r_grp", CubeListBuilder.create(), PartPose.offset(11.8F, 0.0F, 0.0F));
        PartDefinition wing_elbow_r_pose = wing_elbow_r_grp.addOrReplaceChild("wing_elbow_r_pose", CubeListBuilder.create().texOffs(102, 79).addBox(0.0F, -1.0F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.1745F, 0.6632F));
        wing_elbow_r_pose.addOrReplaceChild("wing_mem_mid_r_r1", CubeListBuilder.create().texOffs(1, 18).addBox(-3.5F, 1.4F, -0.44F, 13.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1047F));
        wing_elbow_r_pose.addOrReplaceChild("wing_rib2_r_r1", CubeListBuilder.create().texOffs(1, 87).addBox(0.0F, -0.5F, -1.0F, 13.0F, 1.0F, 2.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7679F));

        PartDefinition wing_wrist_r_grp = wing_elbow_r_pose.addOrReplaceChild("wing_wrist_r_grp", CubeListBuilder.create(), PartPose.offset(8.8F, 0.0F, 0.0F));
        PartDefinition wing_wrist_r_pose = wing_wrist_r_grp.addOrReplaceChild("wing_wrist_r_pose", CubeListBuilder.create()
                .texOffs(51, 82).addBox(0.0F, -1.0F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(-0.04F))
                .texOffs(94, 43).addBox(-2.0F, 0.0F, -0.38F, 11.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0698F, 0.0698F));
        wing_wrist_r_pose.addOrReplaceChild("wing_rib3_r_r1", CubeListBuilder.create().texOffs(59, 88).addBox(0.0F, -0.5F, -1.0F, 11.0F, 1.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4189F));

        PartDefinition leg_l_grp = body_grp.addOrReplaceChild("leg_l_grp", CubeListBuilder.create(), PartPose.offset(-4.2F, -0.4F, 0.2F));
        PartDefinition thigh_l_pose = leg_l_grp.addOrReplaceChild("thigh_l_pose", CubeListBuilder.create().texOffs(79, 29).addBox(-2.0F, -0.6F, -2.5F, 4.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4887F, 0.0F, 0.0F));
        PartDefinition shin_l_grp = thigh_l_pose.addOrReplaceChild("shin_l_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition shin_l_pose = shin_l_grp.addOrReplaceChild("shin_l_pose", CubeListBuilder.create().texOffs(1, 45).addBox(-2.0F, -0.4F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0472F, 0.0F, 0.0F));
        PartDefinition foot_l_grp = shin_l_pose.addOrReplaceChild("foot_l_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 6.2F, 0.0F));
        foot_l_grp.addOrReplaceChild("foot_l_pose", CubeListBuilder.create()
                .texOffs(67, 65).addBox(-2.5F, 0.0F, -4.0F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(1, 1).addBox(1.1F, 0.0F, -5.6F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.04F))
                .texOffs(1, 1).addBox(-0.5F, 0.0F, -5.6F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.04F))
                .texOffs(1, 1).addBox(-2.1F, 0.0F, -5.6F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.04F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5585F, 0.0F, 0.0F));

        PartDefinition leg_r_grp = body_grp.addOrReplaceChild("leg_r_grp", CubeListBuilder.create(), PartPose.offset(4.2F, -0.4F, 0.2F));
        PartDefinition thigh_r_pose = leg_r_grp.addOrReplaceChild("thigh_r_pose", CubeListBuilder.create().texOffs(98, 29).addBox(-2.0F, -0.6F, -2.5F, 4.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4887F, 0.0F, 0.0F));
        PartDefinition shin_r_grp = thigh_r_pose.addOrReplaceChild("shin_r_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition shin_r_pose = shin_r_grp.addOrReplaceChild("shin_r_pose", CubeListBuilder.create().texOffs(18, 45).addBox(-2.0F, -0.4F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0472F, 0.0F, 0.0F));
        PartDefinition foot_r_grp = shin_r_pose.addOrReplaceChild("foot_r_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 6.2F, 0.0F));
        foot_r_grp.addOrReplaceChild("foot_r_pose", CubeListBuilder.create()
                .texOffs(90, 65).addBox(-2.5F, 0.0F, -4.0F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(1, 1).mirror().addBox(1.1F, 0.0F, -5.6F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.04F)).mirror(false)
                .texOffs(1, 1).mirror().addBox(-0.5F, 0.0F, -5.6F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.04F)).mirror(false)
                .texOffs(1, 1).mirror().addBox(-2.1F, 0.0F, -5.6F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.04F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5585F, 0.0F, 0.0F));

        PartDefinition tail_1_grp = body_grp.addOrReplaceChild("tail_1_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.4F, 2.2F));
        PartDefinition tail_1_pose = tail_1_grp.addOrReplaceChild("tail_1_pose", CubeListBuilder.create().texOffs(30, 56).addBox(-2.0F, -2.0F, -0.4F, 4.0F, 4.0F, 5.0F, new CubeDeformation(-0.03F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6109F, 0.0F, 0.0F));
        PartDefinition tail_2_grp = tail_1_pose.addOrReplaceChild("tail_2_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 4.4F));
        PartDefinition tail_2_pose = tail_2_grp.addOrReplaceChild("tail_2_pose", CubeListBuilder.create().texOffs(1, 57).addBox(-2.0F, -2.0F, -0.4F, 4.0F, 4.0F, 5.0F, new CubeDeformation(-0.06F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.0F));
        PartDefinition tail_3_grp = tail_2_pose.addOrReplaceChild("tail_3_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 4.4F));
        PartDefinition tail_3_pose = tail_3_grp.addOrReplaceChild("tail_3_pose", CubeListBuilder.create().texOffs(15, 62).addBox(-1.5F, -1.5F, -0.4F, 3.0F, 3.0F, 5.0F, new CubeDeformation(-0.09F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1222F, 0.0F, 0.0F));
        PartDefinition tail_4_grp = tail_3_pose.addOrReplaceChild("tail_4_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 4.4F));
        PartDefinition tail_4_pose = tail_4_grp.addOrReplaceChild("tail_4_pose", CubeListBuilder.create().texOffs(27, 66).addBox(-1.5F, -1.5F, -0.4F, 3.0F, 3.0F, 5.0F, new CubeDeformation(-0.12F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1396F, 0.0F, 0.0F));
        PartDefinition tail_5_grp = tail_4_pose.addOrReplaceChild("tail_5_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 4.4F));
        tail_5_grp.addOrReplaceChild("tail_5_pose", CubeListBuilder.create()
                .texOffs(112, 24).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.15F))
                .texOffs(84, 65).addBox(-1.0F, -1.0F, 4.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.18F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1222F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(VoidShadowGargoyleRenderState state) {
        super.setupAnim(state);

        if (state.deathTime > 0.0F || state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        if (state.flyAnimationState.isStarted()) {
            this.flyAnimation.apply(state.flyAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        this.blinkOutAnimation.apply(state.blinkOutAnimationState, state.ageInTicks);
        this.blinkInAnimation.apply(state.blinkInAnimationState, state.ageInTicks);
        this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);

        this.head_grp.xRot += Mth.clamp(state.xRot, -30.0F, 30.0F) * Mth.DEG_TO_RAD * 0.65F;
        this.head_grp.yRot += Mth.clamp(state.yRot, -45.0F, 45.0F) * Mth.DEG_TO_RAD * 0.65F;
    }
}
