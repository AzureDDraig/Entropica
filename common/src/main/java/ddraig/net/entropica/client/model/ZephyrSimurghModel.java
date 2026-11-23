package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.ZephyrSimurghAnimations;
import ddraig.net.entropica.client.renderer.ZephyrSimurghRenderState;
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
 * Fauna 41 — Zephyr-Simurgh (Wind Lord).
 * Exact conversion of all 59 cubes, 28 bone groups, and 64x64 UVs from zephyr_simurgh.bbmodel.
 */
public class ZephyrSimurghModel extends EntityModel<ZephyrSimurghRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "zephyr_simurgh"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart root;
    private final ModelPart body_root;
    private final ModelPart torso_grp;
    private final ModelPart neck_grp;
    private final ModelPart head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation flyAnimation;
    private final KeyframeAnimation takeoffAnimation;
    private final KeyframeAnimation landingAnimation;
    private final KeyframeAnimation flyAttackAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation screechAnimation;

    public ZephyrSimurghModel(ModelPart modelRoot) {
        super(modelRoot);
        this.root = modelRoot.getChild("root");
        this.body_root = this.root.getChild("body_root");
        this.torso_grp = this.body_root.getChild("torso_grp");
        this.neck_grp = this.torso_grp.getChild("neck_grp");
        this.head_grp = this.neck_grp.getChild("head_grp");

        this.idleAnimation = ZephyrSimurghAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = ZephyrSimurghAnimations.WALK.bake(modelRoot);
        this.flyAnimation = ZephyrSimurghAnimations.FLY.bake(modelRoot);
        this.takeoffAnimation = ZephyrSimurghAnimations.TAKEOFF.bake(modelRoot);
        this.landingAnimation = ZephyrSimurghAnimations.LANDING.bake(modelRoot);
        this.flyAttackAnimation = ZephyrSimurghAnimations.FLY_ATTACK.bake(modelRoot);
        this.attackAnimation = ZephyrSimurghAnimations.ATTACK.bake(modelRoot);
        this.screechAnimation = ZephyrSimurghAnimations.SCREECH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_root = root.addOrReplaceChild("body_root", CubeListBuilder.create(), PartPose.offset(0.0F, -9.5F, 0.0F));

        PartDefinition torso_grp = body_root.addOrReplaceChild("torso_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -1.7F, 0.5F));

        torso_grp.addOrReplaceChild("tail_covert_r1", CubeListBuilder.create().texOffs(38, 9).addBox(-2.0F, -1.0F, -0.8F, 4.0F, 2.0F, 5.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 3.0F, 2.7F, -0.3491F, 0.0F, 0.0F));

        torso_grp.addOrReplaceChild("breast_keel_r1", CubeListBuilder.create().texOffs(24, 9).addBox(-2.5F, -4.3F, -4.2F, 5.0F, 7.0F, 2.0F, new CubeDeformation(-0.02F))
                .texOffs(0, 0).addBox(-3.0F, -5.5F, -3.0F, 6.0F, 11.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.8F, 0.0F, 0.6283F, 0.0F, 0.0F));

        PartDefinition neck_grp = torso_grp.addOrReplaceChild("neck_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, -2.9F));
        neck_grp.addOrReplaceChild("neck_plumage_r1", CubeListBuilder.create().texOffs(42, 0).addBox(-2.0F, -4.4F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition head_grp = neck_grp.addOrReplaceChild("head_grp", CubeListBuilder.create().texOffs(24, 0).addBox(-2.0F, -3.8F, -3.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.6F, -0.2F));
        head_grp.addOrReplaceChild("beak_hook_r1", CubeListBuilder.create().texOffs(37, 0).addBox(-1.0F, -0.5F, -0.7F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(0.0F, -1.6F, -5.8F, -0.2443F, 0.0F, 0.0F));
        head_grp.addOrReplaceChild("beak_upper_r1", CubeListBuilder.create().texOffs(18, 0).addBox(-1.0F, -1.0F, -2.8F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.8F, -3.0F, -0.0698F, 0.0F, 0.0F));
        head_grp.addOrReplaceChild("brow_ridge_r1", CubeListBuilder.create().texOffs(49, 36).addBox(-2.0F, -0.5F, -1.5F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.06F)), PartPose.offsetAndRotation(0.0F, -3.0F, -1.7F, -0.0698F, 0.0F, 0.0F));
        head_grp.addOrReplaceChild("jaw_grp", CubeListBuilder.create().texOffs(54, 0).addBox(-1.0F, -0.3F, -2.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(-0.08F)), PartPose.offset(0.0F, -0.7F, -2.9F));

        PartDefinition crest_grp = head_grp.addOrReplaceChild("crest_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -3.4F, 0.6F));
        crest_grp.addOrReplaceChild("crest_center_r1", CubeListBuilder.create().texOffs(10, 17).addBox(-0.5F, -0.6F, -0.2F, 1.0F, 1.0F, 5.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.8F, 0.4189F, 0.0F, 0.0F));
        crest_grp.addOrReplaceChild("crest_r_r1", CubeListBuilder.create().texOffs(42, 16).mirror().addBox(-0.5F, -0.6F, -0.2F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.3F, -0.1F, 0.0F, 0.6283F, 0.1745F, -0.1396F));
        crest_grp.addOrReplaceChild("crest_l_r1", CubeListBuilder.create().texOffs(42, 16).addBox(-0.5F, -0.6F, -0.2F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3F, -0.1F, 0.0F, 0.6283F, -0.1745F, 0.1396F));

        PartDefinition wing_inner_l_grp = torso_grp.addOrReplaceChild("wing_inner_l_grp", CubeListBuilder.create(), PartPose.offset(-2.8F, -3.6F, -1.9F));
        wing_inner_l_grp.addOrReplaceChild("wing_sec_4_l_r1", CubeListBuilder.create().texOffs(32, 16).addBox(-8.3F, -0.22F, 4.3F, 2.0F, 1.0F, 6.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6283F, -0.1222F, 0.4887F));
        wing_inner_l_grp.addOrReplaceChild("wing_sec_3_l_r1", CubeListBuilder.create().texOffs(32, 16).addBox(-6.4F, -0.08F, 4.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6283F, -0.0524F, 0.4887F));
        wing_inner_l_grp.addOrReplaceChild("wing_sec_2_l_r1", CubeListBuilder.create().texOffs(14, 31).addBox(-4.5F, -0.24F, 4.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(-0.03F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6283F, 0.0175F, 0.4887F));
        wing_inner_l_grp.addOrReplaceChild("wing_sec_1_l_r1", CubeListBuilder.create().texOffs(0, 31).addBox(-2.6F, -0.12F, 4.2F, 2.0F, 1.0F, 5.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6283F, 0.0698F, 0.4887F));
        wing_inner_l_grp.addOrReplaceChild("wing_covert_in_l_r1", CubeListBuilder.create().texOffs(30, 30).addBox(-8.8F, -0.4F, 1.0F, 9.0F, 1.0F, 5.0F, new CubeDeformation(-0.02F))
                .texOffs(25, 36).addBox(-8.6F, -1.0F, -1.4F, 9.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6283F, 0.1047F, 0.4887F));

        PartDefinition wing_outer_l_grp = wing_inner_l_grp.addOrReplaceChild("wing_outer_l_grp", CubeListBuilder.create(), PartPose.offset(-7.221F, -3.773F, 0.938F));
        wing_outer_l_grp.addOrReplaceChild("wing_pri_2_l_r1", CubeListBuilder.create().texOffs(0, 17).addBox(-3.4F, -0.28F, 2.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6109F, -0.5585F, 0.4189F));
        wing_outer_l_grp.addOrReplaceChild("wing_pri_1_l_r1", CubeListBuilder.create().texOffs(48, 16).addBox(-1.3F, -0.44F, 2.4F, 2.0F, 1.0F, 6.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6109F, -0.2443F, 0.3142F));
        wing_outer_l_grp.addOrReplaceChild("wing_covert_out_l_r1", CubeListBuilder.create().texOffs(0, 37).addBox(-5.7F, -0.65F, 0.3F, 6.0F, 1.0F, 4.0F, new CubeDeformation(-0.02F))
                .texOffs(47, 40).addBox(-4.6F, -1.0F, -1.2F, 5.0F, 2.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5934F, 0.1396F, 0.2443F));

        PartDefinition wing_tip_l_grp = wing_outer_l_grp.addOrReplaceChild("wing_tip_l_grp", CubeListBuilder.create(), PartPose.offset(-4.099F, -1.223F, 0.49F));
        wing_tip_l_grp.addOrReplaceChild("wing_pri_6_l_r1", CubeListBuilder.create().texOffs(29, 43).addBox(-9.9F, -0.52F, -0.6F, 8.0F, 1.0F, 2.0F, new CubeDeformation(-0.03F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5585F, 0.1047F, 0.192F));
        wing_tip_l_grp.addOrReplaceChild("wing_pri_5_l_r1", CubeListBuilder.create().texOffs(0, 46).addBox(-8.2F, -0.24F, 0.1F, 7.0F, 1.0F, 2.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.576F, 0.3142F, 0.0349F));
        wing_tip_l_grp.addOrReplaceChild("wing_pri_4_l_r1", CubeListBuilder.create().texOffs(0, 43).addBox(-7.3F, 0.05F, 0.8F, 7.0F, 1.0F, 2.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5934F, 0.5585F, -0.1396F));
        wing_tip_l_grp.addOrReplaceChild("wing_pri_3_l_r1", CubeListBuilder.create().texOffs(22, 25).addBox(-5.7F, 0.32F, 1.4F, 6.0F, 1.0F, 2.0F, new CubeDeformation(-0.06F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6109F, 0.8203F, -0.3142F));
        wing_tip_l_grp.addOrReplaceChild("wing_hand_covert_l_r1", CubeListBuilder.create().texOffs(17, 41).addBox(-3.6F, -0.78F, -0.9F, 4.0F, 1.0F, 3.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.576F, 0.1745F, 0.2094F));

        PartDefinition wing_inner_r_grp = torso_grp.addOrReplaceChild("wing_inner_r_grp", CubeListBuilder.create(), PartPose.offset(2.8F, -3.6F, -1.9F));
        wing_inner_r_grp.addOrReplaceChild("wing_sec_4_r_r1", CubeListBuilder.create().texOffs(32, 16).mirror().addBox(6.3F, -0.22F, 4.3F, 2.0F, 1.0F, 6.0F, new CubeDeformation(-0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6283F, 0.1222F, -0.4887F));
        wing_inner_r_grp.addOrReplaceChild("wing_sec_3_r_r1", CubeListBuilder.create().texOffs(32, 16).mirror().addBox(4.4F, -0.08F, 4.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(-0.04F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6283F, 0.0524F, -0.4887F));
        wing_inner_r_grp.addOrReplaceChild("wing_sec_2_r_r1", CubeListBuilder.create().texOffs(14, 31).mirror().addBox(2.5F, -0.24F, 4.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(-0.03F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6283F, -0.0175F, -0.4887F));
        wing_inner_r_grp.addOrReplaceChild("wing_sec_1_r_r1", CubeListBuilder.create().texOffs(0, 31).mirror().addBox(0.6F, -0.12F, 4.2F, 2.0F, 1.0F, 5.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6283F, -0.0698F, -0.4887F));
        wing_inner_r_grp.addOrReplaceChild("wing_covert_in_r_r1", CubeListBuilder.create().texOffs(30, 30).mirror().addBox(-0.2F, -0.4F, 1.0F, 9.0F, 1.0F, 5.0F, new CubeDeformation(-0.02F)).mirror(false)
                .texOffs(25, 36).mirror().addBox(-0.4F, -1.0F, -1.4F, 9.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6283F, -0.1047F, -0.4887F));

        PartDefinition wing_outer_r_grp = wing_inner_r_grp.addOrReplaceChild("wing_outer_r_grp", CubeListBuilder.create(), PartPose.offset(7.221F, -3.773F, 0.938F));
        wing_outer_r_grp.addOrReplaceChild("wing_pri_2_r_r1", CubeListBuilder.create().texOffs(0, 17).mirror().addBox(1.4F, -0.28F, 2.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(-0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6109F, 0.5585F, -0.4189F));
        wing_outer_r_grp.addOrReplaceChild("wing_pri_1_r_r1", CubeListBuilder.create().texOffs(48, 16).mirror().addBox(-0.7F, -0.44F, 2.4F, 2.0F, 1.0F, 6.0F, new CubeDeformation(-0.04F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6109F, 0.2443F, -0.3142F));
        wing_outer_r_grp.addOrReplaceChild("wing_covert_out_r_r1", CubeListBuilder.create().texOffs(0, 37).mirror().addBox(-0.3F, -0.65F, 0.3F, 6.0F, 1.0F, 4.0F, new CubeDeformation(-0.02F)).mirror(false)
                .texOffs(47, 40).mirror().addBox(-0.4F, -1.0F, -1.2F, 5.0F, 2.0F, 2.0F, new CubeDeformation(-0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5934F, -0.1396F, -0.2443F));

        PartDefinition wing_tip_r_grp = wing_outer_r_grp.addOrReplaceChild("wing_tip_r_grp", CubeListBuilder.create(), PartPose.offset(4.099F, -1.223F, 0.49F));
        wing_tip_r_grp.addOrReplaceChild("wing_pri_6_r_r1", CubeListBuilder.create().texOffs(29, 43).mirror().addBox(1.9F, -0.52F, -0.6F, 8.0F, 1.0F, 2.0F, new CubeDeformation(-0.03F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5585F, -0.1047F, -0.192F));
        wing_tip_r_grp.addOrReplaceChild("wing_pri_5_r_r1", CubeListBuilder.create().texOffs(0, 46).mirror().addBox(1.2F, -0.24F, 0.1F, 7.0F, 1.0F, 2.0F, new CubeDeformation(-0.04F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.576F, -0.3142F, -0.0349F));
        wing_tip_r_grp.addOrReplaceChild("wing_pri_4_r_r1", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(0.3F, 0.05F, 0.8F, 7.0F, 1.0F, 2.0F, new CubeDeformation(-0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5934F, -0.5585F, 0.1396F));
        wing_tip_r_grp.addOrReplaceChild("wing_pri_3_r_r1", CubeListBuilder.create().texOffs(22, 25).mirror().addBox(-0.3F, 0.32F, 1.4F, 6.0F, 1.0F, 2.0F, new CubeDeformation(-0.06F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6109F, -0.8203F, 0.3142F));
        wing_tip_r_grp.addOrReplaceChild("wing_hand_covert_r_r1", CubeListBuilder.create().texOffs(17, 41).mirror().addBox(-0.4F, -0.78F, -0.9F, 4.0F, 1.0F, 3.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.576F, -0.1745F, -0.2094F));

        PartDefinition tail_c1_grp = torso_grp.addOrReplaceChild("tail_c1_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 3.6F, 3.9F));
        tail_c1_grp.addOrReplaceChild("tail_c1_r1", CubeListBuilder.create().texOffs(16, 17).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));
        PartDefinition tail_c2_grp = tail_c1_grp.addOrReplaceChild("tail_c2_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 2.409F, 5.166F));
        tail_c2_grp.addOrReplaceChild("tail_c2_r1", CubeListBuilder.create().texOffs(24, 18).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2443F, 0.0F, 0.0F));
        PartDefinition tail_c3_grp = tail_c2_grp.addOrReplaceChild("tail_c3_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 1.379F, 5.531F));
        tail_c3_grp.addOrReplaceChild("tail_c3_r1", CubeListBuilder.create().texOffs(32, 23).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0698F, 0.0F, 0.0F));

        PartDefinition tail_l1_grp = torso_grp.addOrReplaceChild("tail_l1_grp", CubeListBuilder.create(), PartPose.offset(-1.1F, 3.5F, 3.8F));
        tail_l1_grp.addOrReplaceChild("tail_l1_r1", CubeListBuilder.create().texOffs(46, 23).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4538F, -0.2443F, 0.0F));
        PartDefinition tail_l2_grp = tail_l1_grp.addOrReplaceChild("tail_l2_grp", CubeListBuilder.create(), PartPose.offset(-1.239F, 2.499F, 4.971F));
        tail_l2_grp.addOrReplaceChild("tail_l2_r1", CubeListBuilder.create().texOffs(0, 24).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2618F, -0.3142F, 0.0F));
        PartDefinition tail_l3_grp = tail_l2_grp.addOrReplaceChild("tail_l3_grp", CubeListBuilder.create(), PartPose.offset(-1.701F, 1.475F, 5.236F));
        tail_l3_grp.addOrReplaceChild("tail_l3_r1", CubeListBuilder.create().texOffs(14, 24).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, -0.384F, 0.0F));

        PartDefinition tail_r1_grp = torso_grp.addOrReplaceChild("tail_r1_grp", CubeListBuilder.create(), PartPose.offset(1.1F, 3.5F, 3.8F));
        tail_r1_grp.addOrReplaceChild("tail_r1_r1", CubeListBuilder.create().texOffs(46, 23).mirror().addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4538F, 0.2443F, 0.0F));
        PartDefinition tail_r2_grp = tail_r1_grp.addOrReplaceChild("tail_r2_grp", CubeListBuilder.create(), PartPose.offset(1.239F, 2.499F, 4.971F));
        tail_r2_grp.addOrReplaceChild("tail_r2_r1", CubeListBuilder.create().texOffs(0, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2618F, 0.3142F, 0.0F));
        PartDefinition tail_r3_grp = tail_r2_grp.addOrReplaceChild("tail_r3_grp", CubeListBuilder.create(), PartPose.offset(1.701F, 1.475F, 5.236F));
        tail_r3_grp.addOrReplaceChild("tail_r3_r1", CubeListBuilder.create().texOffs(14, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.04F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.384F, 0.0F));

        PartDefinition leg_l_grp = body_root.addOrReplaceChild("leg_l_grp", CubeListBuilder.create(), PartPose.offset(-1.9F, 1.3F, 1.2F));
        leg_l_grp.addOrReplaceChild("thigh_l_r1", CubeListBuilder.create().texOffs(25, 28).addBox(-1.0F, -0.6F, -1.5F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3491F, 0.0F, 0.0F));
        PartDefinition shank_l_grp = leg_l_grp.addOrReplaceChild("shank_l_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 3.2F, 0.8F));
        shank_l_grp.addOrReplaceChild("shank_l_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.384F, 0.0F, 0.0F));
        shank_l_grp.addOrReplaceChild("foot_l_grp", CubeListBuilder.create().texOffs(51, 9).addBox(-1.0F, 0.0F, -2.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(58, 4).addBox(-0.5F, -0.06F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)), PartPose.offset(0.0F, 4.0F, -1.5F));

        PartDefinition leg_r_grp = body_root.addOrReplaceChild("leg_r_grp", CubeListBuilder.create(), PartPose.offset(1.9F, 1.3F, 1.2F));
        leg_r_grp.addOrReplaceChild("thigh_r_r1", CubeListBuilder.create().texOffs(25, 28).mirror().addBox(-1.0F, -0.6F, -1.5F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.02F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3491F, 0.0F, 0.0F));
        PartDefinition shank_r_grp = leg_r_grp.addOrReplaceChild("shank_r_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 3.2F, 0.8F));
        shank_r_grp.addOrReplaceChild("shank_r_r1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.384F, 0.0F, 0.0F));
        shank_r_grp.addOrReplaceChild("foot_r_grp", CubeListBuilder.create().texOffs(51, 9).mirror().addBox(-1.0F, 0.0F, -2.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(58, 4).mirror().addBox(-0.5F, -0.06F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offset(0.0F, 4.0F, -1.5F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(ZephyrSimurghRenderState state) {
        super.setupAnim(state);

        boolean fullBodyAction = state.flyAttackAnimationState.isStarted()
                || state.attackAnimationState.isStarted()
                || state.screechAnimationState.isStarted()
                || state.takeoffAnimationState.isStarted()
                || state.landingAnimationState.isStarted();

        if (state.flyAttackAnimationState.isStarted()) {
            this.flyAttackAnimation.apply(state.flyAttackAnimationState, state.ageInTicks);
        } else if (state.attackAnimationState.isStarted()) {
            this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        } else if (state.screechAnimationState.isStarted()) {
            this.screechAnimation.apply(state.screechAnimationState, state.ageInTicks);
        } else if (state.takeoffAnimationState.isStarted()) {
            this.takeoffAnimation.apply(state.takeoffAnimationState, state.ageInTicks);
        } else if (state.landingAnimationState.isStarted()) {
            this.landingAnimation.apply(state.landingAnimationState, state.ageInTicks);
        } else if (state.flyAnimationState.isStarted()) {
            this.flyAnimation.apply(state.flyAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        if (!fullBodyAction) {
            float clampedYaw = Mth.clamp(state.yRot, -42.0F, 42.0F) * Mth.DEG_TO_RAD;
            float clampedPitch = Mth.clamp(state.xRot, -24.0F, 24.0F) * Mth.DEG_TO_RAD;
            this.neck_grp.yRot += clampedYaw * 0.35F;
            this.head_grp.yRot += clampedYaw * 0.55F;
            this.head_grp.xRot += clampedPitch * 0.60F;
        }
    }
}
