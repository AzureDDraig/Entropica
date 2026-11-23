package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.BrimstoneWyvernAnimations;
import ddraig.net.entropica.client.renderer.BrimstoneWyvernRenderState;
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
 * Fauna 54 — Brimstone Wyvern (Lava Drake).
 * Exact conversion of all 71 cubes, 26 bone groups, and 128x128 UVs from brimstone_wyvern.bbmodel.
 */
public class BrimstoneWyvernModel extends EntityModel<BrimstoneWyvernRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "brimstone_wyvern"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation swimAnimation;
    private final KeyframeAnimation flyAnimation;
    private final KeyframeAnimation takeoffAnimation;
    private final KeyframeAnimation landingAnimation;
    private final KeyframeAnimation flyingAttackAnimation;
    private final KeyframeAnimation flyingFireballSpitAnimation;
    private final KeyframeAnimation flyingHurtAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation fireballSpitAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public BrimstoneWyvernModel(ModelPart modelRoot) {
        super(modelRoot, RenderType::entityCutoutNoCull);
        ModelPart root = modelRoot.getChild("root");
        ModelPart chestGrp = root.getChild("body_grp").getChild("chest_grp");
        this.head_grp = chestGrp.getChild("neck_1_grp").getChild("neck_2_grp").getChild("neck_3_grp").getChild("head_grp");

        this.idleAnimation = BrimstoneWyvernAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = BrimstoneWyvernAnimations.WALK.bake(modelRoot);
        this.swimAnimation = BrimstoneWyvernAnimations.SWIM.bake(modelRoot);
        this.flyAnimation = BrimstoneWyvernAnimations.FLY.bake(modelRoot);
        this.takeoffAnimation = BrimstoneWyvernAnimations.TAKEOFF.bake(modelRoot);
        this.landingAnimation = BrimstoneWyvernAnimations.LANDING.bake(modelRoot);
        this.flyingAttackAnimation = BrimstoneWyvernAnimations.FLYING_ATTACK.bake(modelRoot);
        this.flyingFireballSpitAnimation = BrimstoneWyvernAnimations.FLYING_FIREBALL_SPIT.bake(modelRoot);
        this.flyingHurtAnimation = BrimstoneWyvernAnimations.FLYING_HURT.bake(modelRoot);
        this.attackAnimation = BrimstoneWyvernAnimations.ATTACK.bake(modelRoot);
        this.fireballSpitAnimation = BrimstoneWyvernAnimations.FIREBALL_SPIT.bake(modelRoot);
        this.hurtAnimation = BrimstoneWyvernAnimations.HURT.bake(modelRoot);
        this.deathAnimation = BrimstoneWyvernAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_grp = root.addOrReplaceChild("body_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -14.394F, 2.5F));

        PartDefinition chest_grp = body_grp.addOrReplaceChild("chest_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        chest_grp.addOrReplaceChild("spine_pelvis_r1", CubeListBuilder.create().texOffs(34, 24).addBox(-2.0F, -6.0F, -2.5F, 4.0F, 7.0F, 5.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, -3.8F, 0.6F, -0.5236F, 0.0F, 0.0F));
        chest_grp.addOrReplaceChild("spine_chest_2_r1", CubeListBuilder.create().texOffs(26, 88).addBox(-2.0F, -7.0F, -3.0F, 4.0F, 8.0F, 6.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, -4.3F, -0.5585F, 0.0F, 0.0F));
        chest_grp.addOrReplaceChild("spine_chest_1_r1", CubeListBuilder.create().texOffs(34, 114).addBox(-1.5F, -4.5F, -2.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -8.0F, -8.8F, -0.5585F, 0.0F, 0.0F));
        chest_grp.addOrReplaceChild("chest_keel_r1", CubeListBuilder.create().texOffs(0, 56).addBox(-5.0F, -3.0F, -5.0F, 10.0F, 6.0F, 10.0F, new CubeDeformation(-1.02F)), PartPose.offsetAndRotation(0.0F, -0.2F, -7.3F, -0.3491F, 0.0F, 0.0F));
        chest_grp.addOrReplaceChild("chest_block_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-6.5F, -5.5F, -6.5F, 13.0F, 11.0F, 13.0F, new CubeDeformation(-1.5F)), PartPose.offsetAndRotation(0.0F, -4.0F, -6.7F, -0.384F, 0.0F, 0.0F));
        chest_grp.addOrReplaceChild("pelvis_block_r1", CubeListBuilder.create().texOffs(52, 18).addBox(-5.5F, -5.0F, -5.5F, 11.0F, 10.0F, 11.0F, new CubeDeformation(-1.5F)), PartPose.offsetAndRotation(0.0F, -0.7F, 1.0F, -0.3142F, 0.0F, 0.0F));

        PartDefinition neck_1_grp = chest_grp.addOrReplaceChild("neck_1_grp", CubeListBuilder.create().texOffs(40, 59).addBox(-4.0F, -4.0F, -5.3F, 8.0F, 8.0F, 7.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, -5.6F, -10.7F, -0.5236F, 0.0F, 0.0F));
        neck_1_grp.addOrReplaceChild("spine_neck_1_r1", CubeListBuilder.create().texOffs(116, 0).addBox(-1.5F, -3.5F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -2.4F, -2.0F, -0.3142F, 0.0F, 0.0F));

        PartDefinition neck_2_grp = neck_1_grp.addOrReplaceChild("neck_2_grp", CubeListBuilder.create().texOffs(0, 72).addBox(-3.5F, -3.5F, -5.2F, 7.0F, 7.0F, 7.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, -0.2F, -3.7F, -0.0698F, 0.0F, 0.0F));
        neck_2_grp.addOrReplaceChild("spine_neck_2_r1", CubeListBuilder.create().texOffs(116, 0).addBox(-1.5F, -3.5F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -2.0F, -1.8F, -0.2793F, 0.0F, 0.0F));

        PartDefinition neck_3_grp = neck_2_grp.addOrReplaceChild("neck_3_grp", CubeListBuilder.create().texOffs(56, 82).addBox(-3.5F, -3.5F, -4.4F, 7.0F, 7.0F, 6.0F, new CubeDeformation(-1.04F)), PartPose.offsetAndRotation(0.0F, -0.2F, -3.6F, 0.0698F, 0.0F, 0.0F));
        neck_3_grp.addOrReplaceChild("neck_flare_r_r1", CubeListBuilder.create().texOffs(46, 88).mirror().addBox(-0.5F, -3.3F, -1.5F, 2.0F, 4.0F, 3.0F, new CubeDeformation(-0.5F)).mirror(false), PartPose.offsetAndRotation(2.5F, 0.2F, -1.3F, -0.1745F, 0.2443F, 0.5934F));
        neck_3_grp.addOrReplaceChild("neck_flare_l_r1", CubeListBuilder.create().texOffs(46, 88).addBox(-1.5F, -3.3F, -1.5F, 2.0F, 4.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-2.5F, 0.2F, -1.3F, -0.1745F, -0.2443F, -0.5934F));
        neck_3_grp.addOrReplaceChild("spine_neck_3_r1", CubeListBuilder.create().texOffs(116, 0).addBox(-1.5F, -3.5F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -2.6F, -1.4F, -0.1745F, 0.0F, 0.0F));

        PartDefinition head_grp = neck_3_grp.addOrReplaceChild("head_grp", CubeListBuilder.create()
                .texOffs(54, 39).addBox(-5.0F, -5.2F, -7.2F, 10.0F, 10.0F, 10.0F, new CubeDeformation(-2.0F))
                .texOffs(0, 24).addBox(-4.0F, -3.7F, -12.1F, 8.0F, 6.0F, 9.0F, new CubeDeformation(-1.5F))
                .texOffs(0, 98).addBox(-3.0F, -0.3F, -11.3F, 6.0F, 3.0F, 7.0F, new CubeDeformation(-1.03F)),
                PartPose.offsetAndRotation(0.0F, -0.4F, -2.8F, 0.6283F, 0.0F, 0.0F));

        head_grp.addOrReplaceChild("cheek_horn_r_r1", CubeListBuilder.create().texOffs(48, 114).mirror().addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F)).mirror(false), PartPose.offsetAndRotation(2.8F, 0.8F, -0.7F, 0.2094F, 0.384F, 0.0F));
        head_grp.addOrReplaceChild("horn_tip_r_r1", CubeListBuilder.create().texOffs(56, 74).mirror().addBox(-1.0F, -1.9F, 3.7F, 2.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F)).mirror(false), PartPose.offsetAndRotation(2.1F, -2.2F, -1.5F, 0.6807F, 0.4189F, 0.2443F));
        head_grp.addOrReplaceChild("horn_base_r_r1", CubeListBuilder.create().texOffs(26, 103).mirror().addBox(-2.0F, -2.2F, -1.4F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-1.0F)).mirror(false), PartPose.offsetAndRotation(2.1F, -2.2F, -1.5F, 0.6283F, 0.4189F, 0.2443F));
        head_grp.addOrReplaceChild("cheek_horn_l_r1", CubeListBuilder.create().texOffs(48, 114).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-2.8F, 0.8F, -0.7F, 0.2094F, -0.384F, 0.0F));
        head_grp.addOrReplaceChild("horn_tip_l_r1", CubeListBuilder.create().texOffs(56, 74).addBox(-1.0F, -1.9F, 3.7F, 2.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-2.1F, -2.2F, -1.5F, 0.6807F, -0.4189F, -0.2443F));
        head_grp.addOrReplaceChild("horn_base_l_r1", CubeListBuilder.create().texOffs(26, 103).addBox(-2.0F, -2.2F, -1.4F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(-2.1F, -2.2F, -1.5F, 0.6283F, -0.4189F, -0.2443F));

        head_grp.addOrReplaceChild("jaw_grp", CubeListBuilder.create()
                .texOffs(94, 39).addBox(-4.0F, -2.5F, -7.1F, 8.0F, 5.0F, 9.0F, new CubeDeformation(-1.54F))
                .texOffs(80, 98).addBox(-3.0F, -2.8F, -6.3F, 6.0F, 3.0F, 7.0F, new CubeDeformation(-1.08F)),
                PartPose.offset(0.0F, 1.8F, -4.8F));

        PartDefinition tail_1_grp = chest_grp.addOrReplaceChild("tail_1_grp", CubeListBuilder.create().texOffs(96, 18).addBox(-4.0F, -4.0F, -1.6F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.6F, 4.1F, -0.3491F, 0.0F, 0.0F));
        tail_1_grp.addOrReplaceChild("spine_tail_1_r1", CubeListBuilder.create().texOffs(0, 108).addBox(-2.0F, -5.0F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, -2.4F, 2.5F, -0.1745F, 0.0F, 0.0F));

        PartDefinition tail_2_grp = tail_1_grp.addOrReplaceChild("tail_2_grp", CubeListBuilder.create().texOffs(70, 67).addBox(-3.5F, -3.5F, -1.6F, 7.0F, 7.0F, 8.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.2F, 4.8F, 0.0698F, 0.0F, 0.0F));
        tail_2_grp.addOrReplaceChild("spine_tail_2_r1", CubeListBuilder.create().texOffs(116, 7).addBox(-1.5F, -3.5F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -2.0F, 2.4F, -0.1396F, 0.0F, 0.0F));

        PartDefinition tail_3_grp = tail_2_grp.addOrReplaceChild("tail_3_grp", CubeListBuilder.create().texOffs(28, 74).addBox(-3.0F, -3.0F, -1.6F, 6.0F, 6.0F, 8.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.1F, 4.8F, 0.0873F, 0.0F, 0.0F));
        tail_3_grp.addOrReplaceChild("spine_tail_3_r1", CubeListBuilder.create().texOffs(116, 7).addBox(-1.5F, -3.5F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -1.5F, 2.4F, -0.1396F, 0.0F, 0.0F));

        PartDefinition tail_4_grp = tail_3_grp.addOrReplaceChild("tail_4_grp", CubeListBuilder.create().texOffs(0, 86).addBox(-3.0F, -2.5F, -1.6F, 6.0F, 5.0F, 7.0F, new CubeDeformation(-1.03F)), PartPose.offsetAndRotation(0.0F, 0.1F, 4.8F, 0.0873F, 0.0F, 0.0F));
        tail_4_grp.addOrReplaceChild("spine_tail_4_r1", CubeListBuilder.create().texOffs(116, 118).addBox(-1.0F, -2.5F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -1.1F, 2.0F, -0.1396F, 0.0F, 0.0F));

        PartDefinition tail_5_grp = tail_4_grp.addOrReplaceChild("tail_5_grp", CubeListBuilder.create().texOffs(104, 83).addBox(-2.5F, -2.5F, -1.6F, 5.0F, 5.0F, 7.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.8F, 0.0698F, 0.0F, 0.0F));
        tail_5_grp.addOrReplaceChild("spine_tail_5_r1", CubeListBuilder.create().texOffs(116, 118).addBox(-1.0F, -2.5F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -1.1F, 2.0F, -0.1396F, 0.0F, 0.0F));

        PartDefinition tail_6_grp = tail_5_grp.addOrReplaceChild("tail_6_grp", CubeListBuilder.create().texOffs(94, 53).addBox(-4.0F, -2.5F, -1.9F, 8.0F, 5.0F, 9.0F, new CubeDeformation(-1.5F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.8F, 0.0524F, 0.0F, 0.0F));
        tail_6_grp.addOrReplaceChild("tail_fan_r1_r1", CubeListBuilder.create().texOffs(62, 114).mirror().addBox(-1.0F, -2.1F, -0.9F, 2.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F)).mirror(false), PartPose.offsetAndRotation(1.5F, -0.4F, 1.2F, 0.3491F, 0.8378F, 0.2094F));
        tail_6_grp.addOrReplaceChild("tail_fan_r2_r1", CubeListBuilder.create().texOffs(62, 114).mirror().addBox(-1.0F, -2.1F, -0.9F, 2.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F)).mirror(false), PartPose.offsetAndRotation(0.9F, -0.4F, 3.0F, 0.384F, 0.4363F, 0.1047F));
        tail_6_grp.addOrReplaceChild("tail_fan_c_r1", CubeListBuilder.create().texOffs(62, 114).addBox(-1.0F, -2.1F, -0.9F, 2.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -0.4F, 4.0F, 0.4189F, 0.0F, 0.0F));
        tail_6_grp.addOrReplaceChild("tail_fan_l2_r1", CubeListBuilder.create().texOffs(62, 114).addBox(-1.0F, -2.1F, -0.9F, 2.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-0.9F, -0.4F, 3.0F, 0.384F, -0.4363F, -0.1047F));
        tail_6_grp.addOrReplaceChild("tail_fan_l1_r1", CubeListBuilder.create().texOffs(62, 114).addBox(-1.0F, -2.1F, -0.9F, 2.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-1.5F, -0.4F, 1.2F, 0.3491F, -0.8378F, -0.2094F));

        PartDefinition wing_shoulder_l_grp = chest_grp.addOrReplaceChild("wing_shoulder_l_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.6F, -4.8F, -8.3F, 0.1396F, -0.1745F, -0.8029F));
        wing_shoulder_l_grp.addOrReplaceChild("wing_mem_inner_l_r1", CubeListBuilder.create().texOffs(0, 39).addBox(-11.0F, -1.5F, -0.8F, 13.0F, 3.0F, 14.0F, new CubeDeformation(-1.39F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2094F, 0.0F));
        wing_shoulder_l_grp.addOrReplaceChild("wing_arm_l_r1", CubeListBuilder.create().texOffs(46, 95).addBox(-11.6F, -2.0F, -2.0F, 13.0F, 4.0F, 4.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2618F, 0.0F));

        PartDefinition wing_wrist_l_grp = wing_shoulder_l_grp.addOrReplaceChild("wing_wrist_l_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(-10.0F, 0.0F, -2.7F, 0.5934F, 0.6632F, 1.1868F));
        wing_wrist_l_grp.addOrReplaceChild("wing_mem_outer_l_r1", CubeListBuilder.create()
                .texOffs(52, 0).addBox(-15.8F, -1.5F, -0.8F, 17.0F, 3.0F, 15.0F, new CubeDeformation(-1.37F))
                .texOffs(48, 108).addBox(-16.3F, -1.5F, -1.5F, 17.0F, 3.0F, 3.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.2793F, 0.0F));
        wing_wrist_l_grp.addOrReplaceChild("wing_rib3_l_r1", CubeListBuilder.create().texOffs(88, 113).addBox(-13.1F, -1.0F, -1.0F, 14.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.9774F, 0.0F));
        wing_wrist_l_grp.addOrReplaceChild("wing_rib2_l_r1", CubeListBuilder.create().texOffs(88, 108).addBox(-15.7F, -1.0F, -1.0F, 16.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.6109F, 0.0F));
        wing_wrist_l_grp.addOrReplaceChild("wing_claw_l_r1", CubeListBuilder.create().texOffs(90, 118).addBox(-3.2F, -1.5F, -1.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.733F, 0.0F));

        PartDefinition wing_shoulder_r_grp = chest_grp.addOrReplaceChild("wing_shoulder_r_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(4.6F, -4.8F, -8.3F, 0.1396F, 0.1745F, 0.8029F));
        wing_shoulder_r_grp.addOrReplaceChild("wing_mem_inner_r_r1", CubeListBuilder.create().texOffs(0, 39).mirror().addBox(-2.0F, -1.5F, -0.8F, 13.0F, 3.0F, 14.0F, new CubeDeformation(-1.39F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.2094F, 0.0F));
        wing_shoulder_r_grp.addOrReplaceChild("wing_arm_r_r1", CubeListBuilder.create().texOffs(46, 95).mirror().addBox(-1.4F, -2.0F, -2.0F, 13.0F, 4.0F, 4.0F, new CubeDeformation(-1.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.2618F, 0.0F));

        PartDefinition wing_wrist_r_grp = wing_shoulder_r_grp.addOrReplaceChild("wing_wrist_r_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(10.0F, 0.0F, -2.7F, 0.5934F, -0.6632F, -1.1868F));
        wing_wrist_r_grp.addOrReplaceChild("wing_mem_outer_r_r1", CubeListBuilder.create()
                .texOffs(52, 0).mirror().addBox(-1.2F, -1.5F, -0.8F, 17.0F, 3.0F, 15.0F, new CubeDeformation(-1.37F)).mirror(false)
                .texOffs(48, 108).mirror().addBox(-0.7F, -1.5F, -1.5F, 17.0F, 3.0F, 3.0F, new CubeDeformation(-0.5F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2793F, 0.0F));
        wing_wrist_r_grp.addOrReplaceChild("wing_rib3_r_r1", CubeListBuilder.create().texOffs(88, 113).mirror().addBox(-0.9F, -1.0F, -1.0F, 14.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.9774F, 0.0F));
        wing_wrist_r_grp.addOrReplaceChild("wing_rib2_r_r1", CubeListBuilder.create().texOffs(88, 108).mirror().addBox(-0.3F, -1.0F, -1.0F, 16.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.6109F, 0.0F));
        wing_wrist_r_grp.addOrReplaceChild("wing_claw_r_r1", CubeListBuilder.create().texOffs(90, 118).mirror().addBox(-0.8F, -1.5F, -1.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(-0.5F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.733F, 0.0F));

        PartDefinition leg_l_grp = body_grp.addOrReplaceChild("leg_l_grp", CubeListBuilder.create().texOffs(100, 67).addBox(-3.0F, -1.8F, -3.5F, 6.0F, 9.0F, 7.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(-4.2F, 0.0F, -0.1F, -0.4538F, 0.0F, 0.0F));
        PartDefinition knee_l_grp = leg_l_grp.addOrReplaceChild("knee_l_grp", CubeListBuilder.create().texOffs(82, 83).addBox(-2.5F, -1.8F, -3.0F, 5.0F, 9.0F, 6.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 5.6F, 0.0F, 1.2566F, 0.0F, 0.0F));
        PartDefinition ankle_l_grp = knee_l_grp.addOrReplaceChild("ankle_l_grp", CubeListBuilder.create().texOffs(106, 95).addBox(-2.5F, -1.6F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.04F)), PartPose.offsetAndRotation(0.0F, 5.6F, 0.0F, -1.3614F, 0.0F, 0.0F));
        PartDefinition foot_l_grp = ankle_l_grp.addOrReplaceChild("foot_l_grp", CubeListBuilder.create()
                .texOffs(70, 59).addBox(-2.5F, -1.1F, -2.7F, 5.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F))
                .texOffs(18, 114).addBox(-1.5F, -1.1F, -6.1F, 3.0F, 3.0F, 5.0F, new CubeDeformation(-0.52F))
                .texOffs(76, 118).addBox(-1.5F, -1.1F, 0.7F, 3.0F, 3.0F, 4.0F, new CubeDeformation(-0.54F)),
                PartPose.offsetAndRotation(0.0F, 4.8F, 0.0F, 0.5585F, 0.0F, 0.0F));
        foot_l_grp.addOrReplaceChild("toe_outer_l_r1", CubeListBuilder.create().texOffs(104, 118).addBox(-1.0F, -1.5F, -3.4F, 2.0F, 3.0F, 4.0F, new CubeDeformation(-0.52F)), PartPose.offsetAndRotation(-1.7F, 0.4F, -2.0F, 0.0F, 0.0873F, 0.0F));
        foot_l_grp.addOrReplaceChild("toe_inner_l_r1", CubeListBuilder.create().texOffs(104, 118).addBox(-1.0F, -1.5F, -3.4F, 2.0F, 3.0F, 4.0F, new CubeDeformation(-0.52F)), PartPose.offsetAndRotation(1.7F, 0.4F, -2.0F, 0.0F, -0.0873F, 0.0F));

        PartDefinition leg_r_grp = body_grp.addOrReplaceChild("leg_r_grp", CubeListBuilder.create().texOffs(100, 67).mirror().addBox(-3.0F, -1.8F, -3.5F, 6.0F, 9.0F, 7.0F, new CubeDeformation(-1.0F)).mirror(false), PartPose.offsetAndRotation(4.2F, 0.0F, -0.1F, -0.4538F, 0.0F, 0.0F));
        PartDefinition knee_r_grp = leg_r_grp.addOrReplaceChild("knee_r_grp", CubeListBuilder.create().texOffs(82, 83).mirror().addBox(-2.5F, -1.8F, -3.0F, 5.0F, 9.0F, 6.0F, new CubeDeformation(-1.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 5.6F, 0.0F, 1.2566F, 0.0F, 0.0F));
        PartDefinition ankle_r_grp = knee_r_grp.addOrReplaceChild("ankle_r_grp", CubeListBuilder.create().texOffs(106, 95).mirror().addBox(-2.5F, -1.6F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.04F)).mirror(false), PartPose.offsetAndRotation(0.0F, 5.6F, 0.0F, -1.3614F, 0.0F, 0.0F));
        PartDefinition foot_r_grp = ankle_r_grp.addOrReplaceChild("foot_r_grp", CubeListBuilder.create()
                .texOffs(70, 59).mirror().addBox(-2.5F, -1.1F, -2.7F, 5.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F)).mirror(false)
                .texOffs(18, 114).mirror().addBox(-1.5F, -1.1F, -6.1F, 3.0F, 3.0F, 5.0F, new CubeDeformation(-0.52F)).mirror(false)
                .texOffs(76, 118).mirror().addBox(-1.5F, -1.1F, 0.7F, 3.0F, 3.0F, 4.0F, new CubeDeformation(-0.54F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 4.8F, 0.0F, 0.5585F, 0.0F, 0.0F));
        foot_r_grp.addOrReplaceChild("toe_outer_r_r1", CubeListBuilder.create().texOffs(104, 118).mirror().addBox(-1.0F, -1.5F, -3.4F, 2.0F, 3.0F, 4.0F, new CubeDeformation(-0.52F)).mirror(false), PartPose.offsetAndRotation(1.7F, 0.4F, -2.0F, 0.0F, -0.0873F, 0.0F));
        foot_r_grp.addOrReplaceChild("toe_inner_r_r1", CubeListBuilder.create().texOffs(104, 118).mirror().addBox(-1.0F, -1.5F, -3.4F, 2.0F, 3.0F, 4.0F, new CubeDeformation(-0.52F)).mirror(false), PartPose.offsetAndRotation(-1.7F, 0.4F, -2.0F, 0.0F, 0.0873F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(BrimstoneWyvernRenderState state) {
        super.setupAnim(state);

        if (state.deathTime > 0.0F || state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        if (state.takeoffAnimationState.isStarted()) {
            this.takeoffAnimation.apply(state.takeoffAnimationState, state.ageInTicks);
        } else if (state.landingAnimationState.isStarted()) {
            this.landingAnimation.apply(state.landingAnimationState, state.ageInTicks);
        } else if (state.isAirborne) {
            // flying_attack, flying_fireball_spit, and flying_hurt are full absolute poses starting/ending at fly(0.0F)
            if (state.flyingAttackAnimationState.isStarted()) {
                this.flyingAttackAnimation.apply(state.flyingAttackAnimationState, state.ageInTicks);
            } else if (state.flyingFireballSpitAnimationState.isStarted()) {
                this.flyingFireballSpitAnimation.apply(state.flyingFireballSpitAnimationState, state.ageInTicks);
            } else if (state.flyingHurtAnimationState.isStarted()) {
                this.flyingHurtAnimation.apply(state.flyingHurtAnimationState, state.ageInTicks);
            } else {
                this.flyAnimation.apply(state.flyAnimationState, state.ageInTicks);
            }
        } else {
            if (state.swimAnimationState.isStarted()) {
                this.swimAnimation.apply(state.swimAnimationState, state.ageInTicks);
            } else if (state.walkAnimationState.isStarted()) {
                this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
            } else {
                this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
            }

            this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
            this.fireballSpitAnimation.apply(state.fireballSpitAnimationState, state.ageInTicks);
            this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);
        }

        this.head_grp.xRot += Mth.clamp(state.xRot, -28.0F, 28.0F) * Mth.DEG_TO_RAD * 0.60F;
        this.head_grp.yRot += Mth.clamp(state.yRot, -42.0F, 42.0F) * Mth.DEG_TO_RAD * 0.60F;
    }
}
