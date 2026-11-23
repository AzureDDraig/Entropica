package ddraig.net.entropica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.SoulfirePhenexAnimations;
import ddraig.net.entropica.client.renderer.SoulfirePhenexRenderState;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Fauna 45 — Soulfire Phenex (Goetic Flyer).
 * Exact conversion of all 94 cubes, 27 bone groups, and 64x64 UVs from soulfire_phenex.bbmodel,
 * with open hollow ribcage transform hooks for dynamic in-game cyan-blue soulfire wisp rendering.
 */
public class SoulfirePhenexModel extends EntityModel<SoulfirePhenexRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "soulfire_phenex"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart rootPart;
    private final ModelPart body_root;
    private final ModelPart pelvis_grp;
    private final ModelPart torso_grp;
    private final ModelPart rib_back_r_4_r1;
    private final ModelPart neck_grp;
    private final ModelPart head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation flyAnimation;
    private final KeyframeAnimation takeoffAnimation;
    private final KeyframeAnimation landingAnimation;
    private final KeyframeAnimation flyAttackAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public SoulfirePhenexModel(ModelPart modelRoot) {
        super(modelRoot);
        this.rootPart = modelRoot.getChild("root");
        this.body_root = this.rootPart.getChild("body_root");
        this.pelvis_grp = this.body_root.getChild("pelvis_grp");
        this.torso_grp = this.pelvis_grp.getChild("torso_grp");
        this.rib_back_r_4_r1 = this.torso_grp.getChild("rib_back_r_4_r1");
        this.neck_grp = this.torso_grp.getChild("neck_grp");
        this.head_grp = this.neck_grp.getChild("head_grp");

        this.idleAnimation = SoulfirePhenexAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = SoulfirePhenexAnimations.WALK.bake(modelRoot);
        this.flyAnimation = SoulfirePhenexAnimations.FLY.bake(modelRoot);
        this.takeoffAnimation = SoulfirePhenexAnimations.TAKEOFF.bake(modelRoot);
        this.landingAnimation = SoulfirePhenexAnimations.LANDING.bake(modelRoot);
        this.flyAttackAnimation = SoulfirePhenexAnimations.FLY_ATTACK.bake(modelRoot);
        this.attackAnimation = SoulfirePhenexAnimations.ATTACK.bake(modelRoot);
        this.hurtAnimation = SoulfirePhenexAnimations.HURT.bake(modelRoot);
        this.deathAnimation = SoulfirePhenexAnimations.DEATH.bake(modelRoot);
    }

    /**
     * Transforms the given PoseStack directly into the center of the hollow 360-degree ribcage cavity.
     */
    public void translateToRibcageCavity(PoseStack poseStack) {
        this.rootPart.translateAndRotate(poseStack);
        this.body_root.translateAndRotate(poseStack);
        this.pelvis_grp.translateAndRotate(poseStack);
        this.torso_grp.translateAndRotate(poseStack);
        this.rib_back_r_4_r1.translateAndRotate(poseStack);
        poseStack.translate(0.0F, -5.2F / 16.0F, -1.5F / 16.0F);
    }

    /**
     * Transforms the given PoseStack directly into the open raptor throat/beak cavity for soulfire breath glow.
     */
    public void translateToThroatMaw(PoseStack poseStack) {
        this.rootPart.translateAndRotate(poseStack);
        this.body_root.translateAndRotate(poseStack);
        this.pelvis_grp.translateAndRotate(poseStack);
        this.torso_grp.translateAndRotate(poseStack);
        this.neck_grp.translateAndRotate(poseStack);
        this.head_grp.translateAndRotate(poseStack);
        poseStack.translate(0.0F, -0.5F / 16.0F, -5.4F / 16.0F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_root = root.addOrReplaceChild("body_root", CubeListBuilder.create(), PartPose.offset(0.0F, -10.0F, 0.0F));

        PartDefinition pelvis_grp = body_root.addOrReplaceChild("pelvis_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.2F, 0.2F));

        pelvis_grp.addOrReplaceChild("pelvis_block_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -1.5F, -2.5F, 7.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.4189F, 0.0F, 0.0F));

        PartDefinition torso_grp = pelvis_grp.addOrReplaceChild("torso_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -0.7F, 0.0F));

        torso_grp.addOrReplaceChild("rib_back_r_4_r1", CubeListBuilder.create().texOffs(43, 33).mirror().addBox(1.0F, -1.8F, 1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F)).mirror(false)
                .texOffs(43, 33).addBox(-4.0F, -1.8F, 1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F))
                .texOffs(46, 1).mirror().addBox(3.0F, -1.8F, -4.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.02F)).mirror(false)
                .texOffs(46, 1).addBox(-4.0F, -1.8F, -4.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.02F))
                .texOffs(43, 33).mirror().addBox(1.0F, -1.8F, -4.1F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(43, 33).addBox(-4.0F, -1.8F, -4.1F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.01F))
                .texOffs(24, 34).mirror().addBox(0.8F, -4.1F, 1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F)).mirror(false)
                .texOffs(24, 34).addBox(-4.8F, -4.1F, 1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F))
                .texOffs(46, 1).mirror().addBox(3.8F, -4.1F, -4.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.03F)).mirror(false)
                .texOffs(46, 1).addBox(-4.8F, -4.1F, -4.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.03F))
                .texOffs(24, 34).mirror().addBox(0.8F, -4.1F, -4.1F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(24, 34).addBox(-4.8F, -4.1F, -4.1F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.01F))
                .texOffs(24, 34).mirror().addBox(1.0F, -6.4F, 1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F)).mirror(false)
                .texOffs(24, 34).addBox(-5.0F, -6.4F, 1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F))
                .texOffs(46, 1).mirror().addBox(4.0F, -6.4F, -4.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.02F)).mirror(false)
                .texOffs(46, 1).addBox(-5.0F, -6.4F, -4.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.02F))
                .texOffs(24, 34).mirror().addBox(1.0F, -6.4F, -4.1F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(24, 34).addBox(-5.0F, -6.4F, -4.1F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.01F))
                .texOffs(43, 33).mirror().addBox(1.1F, -8.7F, 1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F)).mirror(false)
                .texOffs(43, 33).addBox(-4.1F, -8.7F, 1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F))
                .texOffs(46, 1).mirror().addBox(3.1F, -8.7F, -4.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.02F)).mirror(false)
                .texOffs(46, 1).addBox(-4.1F, -8.7F, -4.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.02F))
                .texOffs(43, 33).mirror().addBox(1.1F, -8.7F, -4.1F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(43, 33).addBox(-4.1F, -8.7F, -4.1F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.01F))
                .texOffs(6, 25).addBox(-1.0F, -8.8F, 2.1F, 2.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(42, 13).addBox(-3.0F, -9.1F, 1.4F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.02F))
                .texOffs(24, 0).addBox(-2.0F, -9.3F, 0.6F, 4.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(22, 17).addBox(-1.0F, -9.3F, -4.2F, 2.0F, 9.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4887F, 0.0F, 0.0F));

        PartDefinition neck_grp = torso_grp.addOrReplaceChild("neck_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -8.3F, -2.7F));

        neck_grp.addOrReplaceChild("vert_c2_r1", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -3.5F, 0.8F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(50, 26).mirror().addBox(-1.0F, -4.6F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.03F)).mirror(false)
                .texOffs(19, 0).addBox(-0.5F, -1.3F, 0.8F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(50, 26).addBox(-1.0F, -2.4F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5934F, 0.0F, 0.0F));

        PartDefinition head_grp = neck_grp.addOrReplaceChild("head_grp", CubeListBuilder.create().texOffs(36, 0).addBox(-2.5F, -3.0F, -1.4F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(6, 21).addBox(-2.5F, -3.0F, -4.4F, 5.0F, 1.0F, 3.0F, new CubeDeformation(0.01F))
                .texOffs(6, 21).mirror().addBox(-2.5F, 0.0F, -4.4F, 5.0F, 1.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(50, 31).addBox(-1.5F, -2.0F, -4.4F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(42, 18).addBox(-2.5F, -2.0F, -3.4F, 5.0F, 2.0F, 2.0F, new CubeDeformation(-0.22F))
                .texOffs(0, 32).addBox(-1.0F, -2.2F, -5.8F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.02F))
                .texOffs(18, 11).addBox(-1.0F, -1.8F, -8.2F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.01F))
                .texOffs(6, 16).addBox(-1.0F, -1.8F, -9.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.02F)), PartPose.offset(0.0F, -3.4F, -2.3F));

        head_grp.addOrReplaceChild("jaw_grp", CubeListBuilder.create().texOffs(21, 29).addBox(-1.0F, -0.4F, -3.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(-0.04F)), PartPose.offset(0.0F, 0.4F, -4.2F));

        PartDefinition crest_l_grp = head_grp.addOrReplaceChild("crest_l_grp", CubeListBuilder.create(), PartPose.offset(-1.9F, -2.2F, 0.8F));
        crest_l_grp.addOrReplaceChild("crest_l_r1", CubeListBuilder.create().texOffs(54, 0).addBox(-1.0F, -5.6F, -0.5F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.733F, -0.2443F, -0.384F));

        PartDefinition crest_r_grp = head_grp.addOrReplaceChild("crest_r_grp", CubeListBuilder.create(), PartPose.offset(1.9F, -2.2F, 0.8F));
        crest_r_grp.addOrReplaceChild("crest_r_r1", CubeListBuilder.create().texOffs(54, 0).mirror().addBox(-1.0F, -5.6F, -0.5F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.733F, 0.2443F, 0.384F));

        PartDefinition wing_inner_l_grp = torso_grp.addOrReplaceChild("wing_inner_l_grp", CubeListBuilder.create(), PartPose.offset(-3.2F, -7.3F, -1.4F));
        wing_inner_l_grp.addOrReplaceChild("sec_3_l_r1", CubeListBuilder.create().texOffs(58, 24).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 9.0F, 1.0F, new CubeDeformation(-0.03F)), PartPose.offsetAndRotation(-4.5F, -1.6F, 1.3F, 0.733F, 0.1396F, 0.1745F));
        wing_inner_l_grp.addOrReplaceChild("sec_2_l_r1", CubeListBuilder.create().texOffs(12, 25).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 8.0F, 1.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(-2.8F, -0.8F, 1.1F, 0.733F, 0.1047F, 0.1222F));
        wing_inner_l_grp.addOrReplaceChild("sec_1_l_r1", CubeListBuilder.create().texOffs(44, 26).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 6.0F, 1.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(-1.1F, 0.0F, 0.9F, 0.733F, 0.0698F, 0.0698F));
        wing_inner_l_grp.addOrReplaceChild("wing_cov_in_l_r1", CubeListBuilder.create().texOffs(48, 8).addBox(-6.1F, -1.0F, -0.3F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(42, 22).addBox(-6.0F, -1.2F, -0.8F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.733F, 0.1396F, 0.4363F));

        PartDefinition wing_outer_l_grp = wing_inner_l_grp.addOrReplaceChild("wing_outer_l_grp", CubeListBuilder.create(), PartPose.offset(-5.4F, -2.5F, 0.9F));
        wing_outer_l_grp.addOrReplaceChild("pri_1_l_r1", CubeListBuilder.create().texOffs(56, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 10.0F, 1.0F, new CubeDeformation(-0.03F)), PartPose.offsetAndRotation(-2.6F, 1.6F, 1.0F, 0.733F, 0.1745F, 0.2793F));
        wing_outer_l_grp.addOrReplaceChild("sec_4_l_r1", CubeListBuilder.create().texOffs(38, 26).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(-0.9F, 0.9F, 0.6F, 0.733F, 0.1396F, 0.2094F));
        wing_outer_l_grp.addOrReplaceChild("wing_cov_out_l_r1", CubeListBuilder.create().texOffs(48, 8).addBox(-6.0F, -0.7F, -0.6F, 6.0F, 3.0F, 2.0F, new CubeDeformation(-0.01F))
                .texOffs(42, 22).addBox(-6.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.733F, 0.2443F, -0.4189F));

        PartDefinition wing_tip_l_grp = wing_outer_l_grp.addOrReplaceChild("wing_tip_l_grp", CubeListBuilder.create(), PartPose.offset(-5.2F, 2.4F, 1.3F));
        wing_tip_l_grp.addOrReplaceChild("pri_4_l_r1", CubeListBuilder.create().texOffs(56, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 10.0F, 1.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(-2.3F, 1.9F, 0.6F, 0.733F, 0.2443F, 0.4712F));
        wing_tip_l_grp.addOrReplaceChild("pri_3_l_r1", CubeListBuilder.create().texOffs(30, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 11.0F, 1.0F, new CubeDeformation(-0.03F)), PartPose.offsetAndRotation(-0.7F, 1.1F, 0.3F, 0.733F, 0.2094F, 0.4014F));
        wing_tip_l_grp.addOrReplaceChild("pri_2_l_r1", CubeListBuilder.create().texOffs(30, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 11.0F, 1.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.9F, 0.4F, 0.0F, 0.733F, 0.1745F, 0.3316F));
        wing_tip_l_grp.addOrReplaceChild("wing_hand_l_r1", CubeListBuilder.create().texOffs(26, 25).addBox(-4.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.733F, 0.3142F, -0.4887F));

        PartDefinition wing_inner_r_grp = torso_grp.addOrReplaceChild("wing_inner_r_grp", CubeListBuilder.create(), PartPose.offset(3.2F, -7.3F, -1.4F));
        wing_inner_r_grp.addOrReplaceChild("sec_3_r_r1", CubeListBuilder.create().texOffs(58, 24).mirror().addBox(-1.0F, 0.0F, -0.5F, 2.0F, 9.0F, 1.0F, new CubeDeformation(-0.03F)).mirror(false), PartPose.offsetAndRotation(4.5F, -1.6F, 1.3F, 0.733F, -0.1396F, -0.1745F));
        wing_inner_r_grp.addOrReplaceChild("sec_2_r_r1", CubeListBuilder.create().texOffs(12, 25).mirror().addBox(-1.0F, 0.0F, -0.5F, 2.0F, 8.0F, 1.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(2.8F, -0.8F, 1.1F, 0.733F, -0.1047F, -0.1222F));
        wing_inner_r_grp.addOrReplaceChild("sec_1_r_r1", CubeListBuilder.create().texOffs(44, 26).mirror().addBox(-1.0F, 0.0F, -0.5F, 2.0F, 6.0F, 1.0F, new CubeDeformation(-0.01F)).mirror(false), PartPose.offsetAndRotation(1.1F, 0.0F, 0.9F, 0.733F, -0.0698F, -0.0698F));
        wing_inner_r_grp.addOrReplaceChild("wing_cov_in_r_r1", CubeListBuilder.create().texOffs(48, 8).mirror().addBox(0.1F, -1.0F, -0.3F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(42, 22).mirror().addBox(0.0F, -1.2F, -0.8F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.02F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.733F, -0.1396F, -0.4363F));

        PartDefinition wing_outer_r_grp = wing_inner_r_grp.addOrReplaceChild("wing_outer_r_grp", CubeListBuilder.create(), PartPose.offset(5.4F, -2.5F, 0.9F));
        wing_outer_r_grp.addOrReplaceChild("pri_1_r_r1", CubeListBuilder.create().texOffs(56, 13).mirror().addBox(-1.0F, 0.0F, -0.5F, 2.0F, 10.0F, 1.0F, new CubeDeformation(-0.03F)).mirror(false), PartPose.offsetAndRotation(2.6F, 1.6F, 1.0F, 0.733F, -0.1745F, -0.2793F));
        wing_outer_r_grp.addOrReplaceChild("sec_4_r_r1", CubeListBuilder.create().texOffs(38, 26).mirror().addBox(-1.0F, 0.0F, -0.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(0.9F, 0.9F, 0.6F, 0.733F, -0.1396F, -0.2094F));
        wing_outer_r_grp.addOrReplaceChild("wing_cov_out_r_r1", CubeListBuilder.create().texOffs(48, 8).mirror().addBox(0.0F, -0.7F, -0.6F, 6.0F, 3.0F, 2.0F, new CubeDeformation(-0.01F)).mirror(false)
                .texOffs(42, 22).mirror().addBox(0.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.733F, -0.2443F, 0.4189F));

        PartDefinition wing_tip_r_grp = wing_outer_r_grp.addOrReplaceChild("wing_tip_r_grp", CubeListBuilder.create(), PartPose.offset(5.2F, 2.4F, 1.3F));
        wing_tip_r_grp.addOrReplaceChild("pri_4_r_r1", CubeListBuilder.create().texOffs(56, 13).mirror().addBox(-1.0F, 0.0F, -0.5F, 2.0F, 10.0F, 1.0F, new CubeDeformation(-0.04F)).mirror(false), PartPose.offsetAndRotation(2.3F, 1.9F, 0.6F, 0.733F, -0.2443F, -0.4712F));
        wing_tip_r_grp.addOrReplaceChild("pri_3_r_r1", CubeListBuilder.create().texOffs(30, 13).mirror().addBox(-1.0F, 0.0F, -0.5F, 2.0F, 11.0F, 1.0F, new CubeDeformation(-0.03F)).mirror(false), PartPose.offsetAndRotation(0.7F, 1.1F, 0.3F, 0.733F, -0.2094F, -0.4014F));
        wing_tip_r_grp.addOrReplaceChild("pri_2_r_r1", CubeListBuilder.create().texOffs(30, 13).mirror().addBox(-1.0F, 0.0F, -0.5F, 2.0F, 11.0F, 1.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(-0.9F, 0.4F, 0.0F, 0.733F, -0.1745F, -0.3316F));
        wing_tip_r_grp.addOrReplaceChild("wing_hand_r_r1", CubeListBuilder.create().texOffs(26, 25).mirror().addBox(0.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.733F, -0.3142F, 0.4887F));

        PartDefinition leg_l_grp = pelvis_grp.addOrReplaceChild("leg_l_grp", CubeListBuilder.create(), PartPose.offset(-2.5F, 0.2F, -0.5F));
        leg_l_grp.addOrReplaceChild("thigh_l_r1", CubeListBuilder.create().texOffs(0, 8).addBox(-1.5F, -0.6F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition shank_l_grp = leg_l_grp.addOrReplaceChild("shank_l_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 3.6F, 2.1F));
        shank_l_grp.addOrReplaceChild("shank_l_r1", CubeListBuilder.create().texOffs(18, 25).addBox(-0.5F, -0.2F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4887F, 0.0F, 0.0F));

        PartDefinition foot_l_grp = shank_l_grp.addOrReplaceChild("foot_l_grp", CubeListBuilder.create().texOffs(16, 33).addBox(-1.0F, -0.2F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.03F))
                .texOffs(18, 8).addBox(-0.5F, -0.2F, 0.8F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.02F))
                .texOffs(0, 0).addBox(-0.5F, -0.2F, 2.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.2F, -2.1F));
        foot_l_grp.addOrReplaceChild("claw_out_l_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -0.5F, -3.6F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.01F))
                .texOffs(28, 30).addBox(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-0.6F, 0.3F, -0.6F, 0.0F, 0.2094F, 0.0F));
        foot_l_grp.addOrReplaceChild("claw_in_l_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -0.5F, -3.6F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(28, 30).addBox(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.02F)), PartPose.offsetAndRotation(0.6F, 0.3F, -0.6F, 0.0F, -0.1396F, 0.0F));

        PartDefinition leg_r_grp = pelvis_grp.addOrReplaceChild("leg_r_grp", CubeListBuilder.create(), PartPose.offset(2.5F, 0.2F, -0.5F));
        leg_r_grp.addOrReplaceChild("thigh_r_r1", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-1.5F, -0.6F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition shank_r_grp = leg_r_grp.addOrReplaceChild("shank_r_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 3.6F, 2.1F));
        shank_r_grp.addOrReplaceChild("shank_r_r1", CubeListBuilder.create().texOffs(18, 25).mirror().addBox(-0.5F, -0.2F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4887F, 0.0F, 0.0F));

        PartDefinition foot_r_grp = shank_r_grp.addOrReplaceChild("foot_r_grp", CubeListBuilder.create().texOffs(16, 33).mirror().addBox(-1.0F, -0.2F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.03F)).mirror(false)
                .texOffs(18, 8).mirror().addBox(-0.5F, -0.2F, 0.8F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.02F)).mirror(false)
                .texOffs(0, 0).mirror().addBox(-0.5F, -0.2F, 2.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 4.2F, -2.1F));
        foot_r_grp.addOrReplaceChild("claw_out_r_r1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, -0.5F, -3.6F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.01F)).mirror(false)
                .texOffs(28, 30).mirror().addBox(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.6F, 0.3F, -0.6F, 0.0F, -0.2094F, 0.0F));
        foot_r_grp.addOrReplaceChild("claw_in_r_r1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, -0.5F, -3.6F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(28, 30).mirror().addBox(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.02F)).mirror(false), PartPose.offsetAndRotation(-0.6F, 0.3F, -0.6F, 0.0F, 0.1396F, 0.0F));

        PartDefinition tail_grp = pelvis_grp.addOrReplaceChild("tail_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.4F, 2.0F));
        tail_grp.addOrReplaceChild("tail_base_r1", CubeListBuilder.create().texOffs(32, 7).addBox(-2.0F, -0.8F, -0.8F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6632F, 0.0F, 0.0F));

        PartDefinition tail_c_grp = tail_grp.addOrReplaceChild("tail_c_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.2F, 1.0F));
        tail_c_grp.addOrReplaceChild("tail_c_r1", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7679F, 0.0F, 0.0F));

        PartDefinition tail_il_grp = tail_grp.addOrReplaceChild("tail_il_grp", CubeListBuilder.create(), PartPose.offset(-0.9F, 0.2F, 0.9F));
        tail_il_grp.addOrReplaceChild("tail_il_r1", CubeListBuilder.create().texOffs(36, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 11.0F, 1.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7679F, -0.1047F, 0.2269F));

        PartDefinition tail_ir_grp = tail_grp.addOrReplaceChild("tail_ir_grp", CubeListBuilder.create(), PartPose.offset(0.9F, 0.2F, 0.9F));
        tail_ir_grp.addOrReplaceChild("tail_ir_r1", CubeListBuilder.create().texOffs(36, 13).mirror().addBox(-1.0F, 0.0F, -0.5F, 2.0F, 11.0F, 1.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7679F, 0.1047F, -0.2269F));

        PartDefinition tail_ol_grp = tail_grp.addOrReplaceChild("tail_ol_grp", CubeListBuilder.create(), PartPose.offset(-1.7F, 0.2F, 0.8F));
        tail_ol_grp.addOrReplaceChild("tail_ol_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 10.0F, 1.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7679F, -0.2094F, 0.4538F));

        PartDefinition tail_or_grp = tail_grp.addOrReplaceChild("tail_or_grp", CubeListBuilder.create(), PartPose.offset(1.7F, 0.2F, 0.8F));
        tail_or_grp.addOrReplaceChild("tail_or_r1", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, 0.0F, -0.5F, 2.0F, 10.0F, 1.0F, new CubeDeformation(-0.04F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7679F, 0.2094F, -0.4538F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(SoulfirePhenexRenderState state) {
        super.setupAnim(state);

        if (state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        if (state.takeoffAnimationState.isStarted()) {
            this.takeoffAnimation.apply(state.takeoffAnimationState, state.ageInTicks);
        } else if (state.landingAnimationState.isStarted()) {
            this.landingAnimation.apply(state.landingAnimationState, state.ageInTicks);
        } else if (state.flyAttackAnimationState.isStarted()) {
            this.flyAttackAnimation.apply(state.flyAttackAnimationState, state.ageInTicks);
        } else if (state.attackAnimationState.isStarted()) {
            this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        } else if (state.flyAnimationState.isStarted()) {
            this.flyAnimation.apply(state.flyAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        if (state.hurtAnimationState.isStarted()) {
            this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);
        }

        float yawRad = Mth.clamp(state.yRot, -40.0F, 40.0F) * Mth.DEG_TO_RAD;
        float pitchRad = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
        this.neck_grp.yRot += yawRad * 0.35F;
        this.head_grp.yRot += yawRad * 0.65F;
        this.head_grp.xRot += pitchRad * 0.70F;
    }
}
