package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.renderer.VoidRiftShadeRenderState;
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
 * Fauna 42 — Void-Rift Shade (Dimensional Rift-Lurker)
 * Exact conversion of all 80 cubes, 41 bones, and 128x128 UVs from void_rift_shade.bbmodel.
 */
public class VoidRiftShadeModel extends EntityModel<VoidRiftShadeRenderState> {
	public static final ModelLayerLocation MODEL_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_rift_shade"), "main");
	public static final ModelLayerLocation LAYER_LOCATION = MODEL_LOCATION;

	private final ModelPart root;
	private final ModelPart body_root;
	private final ModelPart pelvis_grp;
	private final ModelPart spine_grp;
	private final ModelPart chest_grp;
	private final ModelPart neck_grp;
	private final ModelPart arm_l_grp;
	private final ModelPart forearm_l_grp;
	private final ModelPart hand_l_grp;
	private final ModelPart thumb_l_grp;
	private final ModelPart claw1_l_grp;
	private final ModelPart claw2_l_grp;
	private final ModelPart claw3_l_grp;
	private final ModelPart arm_r_grp;
	private final ModelPart forearm_r_grp;
	private final ModelPart hand_r_grp;
	private final ModelPart thumb_r_grp;
	private final ModelPart claw1_r_grp;
	private final ModelPart claw2_r_grp;
	private final ModelPart claw3_r_grp;
	private final ModelPart tendril_ul_1_grp;
	private final ModelPart tendril_ul_2_grp;
	private final ModelPart tendril_ul_3_grp;
	private final ModelPart tendril_ur_1_grp;
	private final ModelPart tendril_ur_2_grp;
	private final ModelPart tendril_ur_3_grp;
	private final ModelPart tendril_ll_1_grp;
	private final ModelPart tendril_ll_2_grp;
	private final ModelPart tendril_ll_3_grp;
	private final ModelPart tendril_lr_1_grp;
	private final ModelPart tendril_lr_2_grp;
	private final ModelPart tendril_lr_3_grp;
	private final ModelPart head_grp;
	private final ModelPart void_core_grp;
	private final ModelPart eyes_grp;
	private final ModelPart leg_l_grp;
	private final ModelPart shin_l_grp;
	private final ModelPart foot_l_grp;
	private final ModelPart leg_r_grp;
	private final ModelPart shin_r_grp;
	private final ModelPart foot_r_grp;

	private final KeyframeAnimation idleAnimation;
	private final KeyframeAnimation walkAnimation;
	private final KeyframeAnimation attackAnimation;
	private final KeyframeAnimation blink_outAnimation;
	private final KeyframeAnimation blink_inAnimation;
	private final KeyframeAnimation aggroAnimation;
	private final KeyframeAnimation hurtAnimation;
	private final KeyframeAnimation hurt_staggerAnimation;
	private final KeyframeAnimation deathAnimation;

	public VoidRiftShadeModel(ModelPart modelRoot) {
		super(modelRoot);
		this.root = modelRoot.getChild("root");
		this.body_root = this.root.getChild("body_root");
		this.pelvis_grp = this.body_root.getChild("pelvis_grp");
		this.spine_grp = this.pelvis_grp.getChild("spine_grp");
		this.chest_grp = this.spine_grp.getChild("chest_grp");
		this.neck_grp = this.chest_grp.getChild("neck_grp");
		this.arm_l_grp = this.chest_grp.getChild("arm_l_grp");
		this.forearm_l_grp = this.arm_l_grp.getChild("forearm_l_grp");
		this.hand_l_grp = this.forearm_l_grp.getChild("hand_l_grp");
		this.thumb_l_grp = this.hand_l_grp.getChild("thumb_l_grp");
		this.claw1_l_grp = this.hand_l_grp.getChild("claw1_l_grp");
		this.claw2_l_grp = this.hand_l_grp.getChild("claw2_l_grp");
		this.claw3_l_grp = this.hand_l_grp.getChild("claw3_l_grp");
		this.arm_r_grp = this.chest_grp.getChild("arm_r_grp");
		this.forearm_r_grp = this.arm_r_grp.getChild("forearm_r_grp");
		this.hand_r_grp = this.forearm_r_grp.getChild("hand_r_grp");
		this.thumb_r_grp = this.hand_r_grp.getChild("thumb_r_grp");
		this.claw1_r_grp = this.hand_r_grp.getChild("claw1_r_grp");
		this.claw2_r_grp = this.hand_r_grp.getChild("claw2_r_grp");
		this.claw3_r_grp = this.hand_r_grp.getChild("claw3_r_grp");
		this.tendril_ul_1_grp = this.chest_grp.getChild("tendril_ul_1_grp");
		this.tendril_ul_2_grp = this.tendril_ul_1_grp.getChild("tendril_ul_2_grp");
		this.tendril_ul_3_grp = this.tendril_ul_2_grp.getChild("tendril_ul_3_grp");
		this.tendril_ur_1_grp = this.chest_grp.getChild("tendril_ur_1_grp");
		this.tendril_ur_2_grp = this.tendril_ur_1_grp.getChild("tendril_ur_2_grp");
		this.tendril_ur_3_grp = this.tendril_ur_2_grp.getChild("tendril_ur_3_grp");
		this.tendril_ll_1_grp = this.chest_grp.getChild("tendril_ll_1_grp");
		this.tendril_ll_2_grp = this.tendril_ll_1_grp.getChild("tendril_ll_2_grp");
		this.tendril_ll_3_grp = this.tendril_ll_2_grp.getChild("tendril_ll_3_grp");
		this.tendril_lr_1_grp = this.chest_grp.getChild("tendril_lr_1_grp");
		this.tendril_lr_2_grp = this.tendril_lr_1_grp.getChild("tendril_lr_2_grp");
		this.tendril_lr_3_grp = this.tendril_lr_2_grp.getChild("tendril_lr_3_grp");
		this.head_grp = this.body_root.getChild("head_grp");
		this.void_core_grp = this.head_grp.getChild("void_core_grp");
		this.eyes_grp = this.head_grp.getChild("eyes_grp");
		this.leg_l_grp = this.body_root.getChild("leg_l_grp");
		this.shin_l_grp = this.leg_l_grp.getChild("shin_l_grp");
		this.foot_l_grp = this.shin_l_grp.getChild("foot_l_grp");
		this.leg_r_grp = this.body_root.getChild("leg_r_grp");
		this.shin_r_grp = this.leg_r_grp.getChild("shin_r_grp");
		this.foot_r_grp = this.shin_r_grp.getChild("foot_r_grp");

		this.idleAnimation = VoidRiftShadeAnimations.IDLE.bake(modelRoot);
		this.walkAnimation = VoidRiftShadeAnimations.WALK.bake(modelRoot);
		this.attackAnimation = VoidRiftShadeAnimations.ATTACK.bake(modelRoot);
		this.blink_outAnimation = VoidRiftShadeAnimations.BLINK_OUT.bake(modelRoot);
		this.blink_inAnimation = VoidRiftShadeAnimations.BLINK_IN.bake(modelRoot);
		this.aggroAnimation = VoidRiftShadeAnimations.AGGRO.bake(modelRoot);
		this.hurtAnimation = VoidRiftShadeAnimations.HURT.bake(modelRoot);
		this.hurt_staggerAnimation = VoidRiftShadeAnimations.HURT_STAGGER.bake(modelRoot);
		this.deathAnimation = VoidRiftShadeAnimations.DEATH.bake(modelRoot);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body_root = root.addOrReplaceChild("body_root", CubeListBuilder.create(), PartPose.offset(0.0F, -35.0F, 0.0F));

		PartDefinition pelvis_grp = body_root.addOrReplaceChild("pelvis_grp", CubeListBuilder.create().texOffs(44, 25).addBox(-5.0F, -1.5F, -3.0F, 10.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.5F, 0.0F));

		PartDefinition spine_grp = pelvis_grp.addOrReplaceChild("spine_grp", CubeListBuilder.create().texOffs(78, 47).addBox(-3.0F, -6.8F, -2.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(-0.04F)), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition chest_grp = spine_grp.addOrReplaceChild("chest_grp", CubeListBuilder.create().texOffs(0, 25).addBox(-5.0F, -2.8F, -3.0F, 10.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(63, 60).addBox(-3.0F, -4.2F, -2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(-0.08F))
				.texOffs(0, 0).addBox(-6.0F, -9.2F, -3.5F, 12.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.8F, 0.0F));

		PartDefinition neck_grp = chest_grp.addOrReplaceChild("neck_grp", CubeListBuilder.create().texOffs(60, 52).addBox(-3.0F, -0.4F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(-0.06F)), PartPose.offset(0.0F, -10.0F, 0.0F));

		PartDefinition arm_l_grp = chest_grp.addOrReplaceChild("arm_l_grp", CubeListBuilder.create(), PartPose.offset(-6.4F, -7.7F, 0.0F));
		PartDefinition shoulder_l_r1 = arm_l_grp.addOrReplaceChild("shoulder_l_r1", CubeListBuilder.create().texOffs(29, 55).addBox(-3.3F, -1.7F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.2094F));
		PartDefinition upper_arm_l_r1 = arm_l_grp.addOrReplaceChild("upper_arm_l_r1", CubeListBuilder.create().texOffs(28, 30).addBox(-2.8F, 1.7F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.2094F));

		PartDefinition forearm_l_grp = arm_l_grp.addOrReplaceChild("forearm_l_grp", CubeListBuilder.create(), PartPose.offset(-3.724F, 13.671F, 1.238F));
		PartDefinition forearm_l_r1 = forearm_l_grp.addOrReplaceChild("forearm_l_r1", CubeListBuilder.create().texOffs(78, 0).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.2443F));

		PartDefinition hand_l_grp = forearm_l_grp.addOrReplaceChild("hand_l_grp", CubeListBuilder.create(), PartPose.offset(-3.001F, 12.035F, -4.515F));
		PartDefinition palm_l_r1 = hand_l_grp.addOrReplaceChild("palm_l_r1", CubeListBuilder.create().texOffs(46, 63).addBox(-2.5F, -0.5F, -1.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4189F, 0.6632F, 0.2793F));

		PartDefinition thumb_l_grp = hand_l_grp.addOrReplaceChild("thumb_l_grp", CubeListBuilder.create(), PartPose.offset(0.326F, 1.836F, -1.794F));
		PartDefinition thumb_prox_l_r1 = thumb_l_grp.addOrReplaceChild("thumb_prox_l_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4538F, 0.3142F, -0.4887F));
		PartDefinition thumb_dist_l_r1 = thumb_l_grp.addOrReplaceChild("thumb_dist_l_r1", CubeListBuilder.create().texOffs(56, 11).addBox(-0.5F, -0.4F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.12F)), PartPose.offsetAndRotation(0.968F, 2.743F, -1.334F, -0.2793F, 0.2793F, 0.2443F));

		PartDefinition claw1_l_grp = hand_l_grp.addOrReplaceChild("claw1_l_grp", CubeListBuilder.create(), PartPose.offset(-0.814F, 3.8F, -2.259F));
		PartDefinition claw1_prox_l_r1 = claw1_l_grp.addOrReplaceChild("claw1_prox_l_r1", CubeListBuilder.create().texOffs(31, 0).addBox(-0.5F, -0.3F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5236F, 0.6283F, 0.4189F));
		PartDefinition claw1_dist_l_r1 = claw1_l_grp.addOrReplaceChild("claw1_dist_l_r1", CubeListBuilder.create().texOffs(70, 11).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.12F)), PartPose.offsetAndRotation(-2.11F, 2.283F, -1.375F, -0.2443F, 0.6283F, -0.2793F));

		PartDefinition claw2_l_grp = hand_l_grp.addOrReplaceChild("claw2_l_grp", CubeListBuilder.create(), PartPose.offset(-2.144F, 3.377F, -1.285F));
		PartDefinition claw2_prox_l_r1 = claw2_l_grp.addOrReplaceChild("claw2_prox_l_r1", CubeListBuilder.create().texOffs(37, 0).addBox(-0.5F, -0.3F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4189F, 0.6283F, 0.4887F));
		PartDefinition claw2_dist_l_r1 = claw2_l_grp.addOrReplaceChild("claw2_dist_l_r1", CubeListBuilder.create().texOffs(0, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.12F)), PartPose.offsetAndRotation(-2.176F, 2.361F, -1.119F, -0.1745F, 0.6283F, -0.3142F));

		PartDefinition claw3_l_grp = hand_l_grp.addOrReplaceChild("claw3_l_grp", CubeListBuilder.create(), PartPose.offset(-3.475F, 2.953F, -0.31F));
		PartDefinition claw3_prox_l_r1 = claw3_l_grp.addOrReplaceChild("claw3_prox_l_r1", CubeListBuilder.create().texOffs(68, 0).addBox(-0.5F, -0.3F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3142F, 0.6283F, 0.5585F));
		PartDefinition claw3_dist_l_r1 = claw3_l_grp.addOrReplaceChild("claw3_dist_l_r1", CubeListBuilder.create().texOffs(4, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.12F)), PartPose.offsetAndRotation(-2.237F, 2.415F, -0.85F, -0.1047F, 0.6283F, -0.3491F));

		PartDefinition arm_r_grp = chest_grp.addOrReplaceChild("arm_r_grp", CubeListBuilder.create(), PartPose.offset(6.4F, -7.7F, 0.0F));
		PartDefinition shoulder_r_r1 = arm_r_grp.addOrReplaceChild("shoulder_r_r1", CubeListBuilder.create().texOffs(93, 57).addBox(-1.7F, -1.7F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, -0.2094F));
		PartDefinition upper_arm_r_r1 = arm_r_grp.addOrReplaceChild("upper_arm_r_r1", CubeListBuilder.create().texOffs(72, 30).addBox(-1.2F, 1.7F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, -0.2094F));

		PartDefinition forearm_r_grp = arm_r_grp.addOrReplaceChild("forearm_r_grp", CubeListBuilder.create(), PartPose.offset(3.724F, 13.671F, 1.238F));
		PartDefinition forearm_r_r1 = forearm_r_grp.addOrReplaceChild("forearm_r_r1", CubeListBuilder.create().texOffs(94, 0).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, -0.2443F));

		PartDefinition hand_r_grp = forearm_r_grp.addOrReplaceChild("hand_r_grp", CubeListBuilder.create(), PartPose.offset(3.001F, 12.035F, -4.515F));
		PartDefinition palm_r_r1 = hand_r_grp.addOrReplaceChild("palm_r_r1", CubeListBuilder.create().texOffs(26, 64).addBox(-2.5F, -0.5F, -1.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4189F, -0.6632F, -0.2793F));

		PartDefinition thumb_r_grp = hand_r_grp.addOrReplaceChild("thumb_r_grp", CubeListBuilder.create(), PartPose.offset(-0.326F, 1.836F, -1.794F));
		PartDefinition thumb_prox_r_r1 = thumb_r_grp.addOrReplaceChild("thumb_prox_r_r1", CubeListBuilder.create().texOffs(41, 4).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4538F, -0.3142F, 0.4887F));
		PartDefinition thumb_dist_r_r1 = thumb_r_grp.addOrReplaceChild("thumb_dist_r_r1", CubeListBuilder.create().texOffs(14, 13).addBox(-0.5F, -0.4F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.12F)), PartPose.offsetAndRotation(-0.968F, 2.743F, -1.334F, -0.2793F, -0.2793F, -0.2443F));

		PartDefinition claw1_r_grp = hand_r_grp.addOrReplaceChild("claw1_r_grp", CubeListBuilder.create(), PartPose.offset(0.814F, 3.8F, -2.259F));
		PartDefinition claw1_prox_r_r1 = claw1_r_grp.addOrReplaceChild("claw1_prox_r_r1", CubeListBuilder.create().texOffs(72, 4).addBox(-0.5F, -0.3F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5236F, -0.6283F, -0.4189F));
		PartDefinition claw1_dist_r_r1 = claw1_r_grp.addOrReplaceChild("claw1_dist_r_r1", CubeListBuilder.create().texOffs(18, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.12F)), PartPose.offsetAndRotation(2.11F, 2.283F, -1.375F, -0.2443F, -0.6283F, 0.2793F));

		PartDefinition claw2_r_grp = hand_r_grp.addOrReplaceChild("claw2_r_grp", CubeListBuilder.create(), PartPose.offset(2.144F, 3.377F, -1.285F));
		PartDefinition claw2_prox_r_r1 = claw2_r_grp.addOrReplaceChild("claw2_prox_r_r1", CubeListBuilder.create().texOffs(50, 11).addBox(-0.5F, -0.3F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4189F, -0.6283F, -0.4887F));
		PartDefinition claw2_dist_r_r1 = claw2_r_grp.addOrReplaceChild("claw2_dist_r_r1", CubeListBuilder.create().texOffs(26, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.12F)), PartPose.offsetAndRotation(2.176F, 2.361F, -1.119F, -0.1745F, -0.6283F, 0.3142F));

		PartDefinition claw3_r_grp = hand_r_grp.addOrReplaceChild("claw3_r_grp", CubeListBuilder.create(), PartPose.offset(3.475F, 2.953F, -0.31F));
		PartDefinition claw3_prox_r_r1 = claw3_r_grp.addOrReplaceChild("claw3_prox_r_r1", CubeListBuilder.create().texOffs(64, 11).addBox(-0.5F, -0.3F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3142F, -0.6283F, -0.5585F));
		PartDefinition claw3_dist_r_r1 = claw3_r_grp.addOrReplaceChild("claw3_dist_r_r1", CubeListBuilder.create().texOffs(30, 13).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.12F)), PartPose.offsetAndRotation(2.237F, 2.415F, -0.85F, -0.1047F, -0.6283F, 0.3491F));

		PartDefinition tendril_ul_1_grp = chest_grp.addOrReplaceChild("tendril_ul_1_grp", CubeListBuilder.create(), PartPose.offset(-4.5F, -7.4F, 3.0F));
		PartDefinition tendril_ul_1_r1 = tendril_ul_1_grp.addOrReplaceChild("tendril_ul_1_r1", CubeListBuilder.create().texOffs(14, 13).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 8.0F, new CubeDeformation(-0.14F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2094F, -0.5585F, 0.1047F));

		PartDefinition tendril_ul_2_grp = tendril_ul_1_grp.addOrReplaceChild("tendril_ul_2_grp", CubeListBuilder.create(), PartPose.offset(-3.761F, 1.068F, 5.807F));
		PartDefinition tendril_ul_2_r1 = tendril_ul_2_grp.addOrReplaceChild("tendril_ul_2_r1", CubeListBuilder.create().texOffs(46, 44).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 8.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.733F, -0.384F, 0.0698F));

		PartDefinition tendril_ul_3_grp = tendril_ul_2_grp.addOrReplaceChild("tendril_ul_3_grp", CubeListBuilder.create(), PartPose.offset(-2.271F, 4.537F, 4.823F));
		PartDefinition tendril_ul_3_r1 = tendril_ul_3_grp.addOrReplaceChild("tendril_ul_3_r1", CubeListBuilder.create().texOffs(111, 50).addBox(-0.5F, -1.0F, -0.4F, 1.0F, 2.0F, 7.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.1519F, -0.2094F, 0.0349F));

		PartDefinition tendril_ur_1_grp = chest_grp.addOrReplaceChild("tendril_ur_1_grp", CubeListBuilder.create(), PartPose.offset(4.5F, -7.4F, 3.0F));
		PartDefinition tendril_ur_1_r1 = tendril_ur_1_grp.addOrReplaceChild("tendril_ur_1_r1", CubeListBuilder.create().texOffs(98, 47).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 8.0F, new CubeDeformation(-0.14F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2094F, 0.5585F, -0.1047F));

		PartDefinition tendril_ur_2_grp = tendril_ur_1_grp.addOrReplaceChild("tendril_ur_2_grp", CubeListBuilder.create(), PartPose.offset(3.761F, 1.068F, 5.807F));
		PartDefinition tendril_ur_2_r1 = tendril_ur_2_grp.addOrReplaceChild("tendril_ur_2_r1", CubeListBuilder.create().texOffs(14, 48).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 8.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.733F, 0.384F, -0.0698F));

		PartDefinition tendril_ur_3_grp = tendril_ur_2_grp.addOrReplaceChild("tendril_ur_3_grp", CubeListBuilder.create(), PartPose.offset(2.271F, 4.537F, 4.823F));
		PartDefinition tendril_ur_3_r1 = tendril_ur_3_grp.addOrReplaceChild("tendril_ur_3_r1", CubeListBuilder.create().texOffs(106, 59).addBox(-0.5F, -1.0F, -0.4F, 1.0F, 2.0F, 7.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.1519F, 0.2094F, -0.0349F));

		PartDefinition tendril_ll_1_grp = chest_grp.addOrReplaceChild("tendril_ll_1_grp", CubeListBuilder.create(), PartPose.offset(-3.0F, -3.8F, 2.8F));
		PartDefinition tendril_ll_1_r1 = tendril_ll_1_grp.addOrReplaceChild("tendril_ll_1_r1", CubeListBuilder.create().texOffs(49, 54).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.14F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4887F, -0.3491F, 0.0698F));

		PartDefinition tendril_ll_2_grp = tendril_ll_1_grp.addOrReplaceChild("tendril_ll_2_grp", CubeListBuilder.create(), PartPose.offset(-2.004F, 2.684F, 4.978F));
		PartDefinition tendril_ll_2_r1 = tendril_ll_2_grp.addOrReplaceChild("tendril_ll_2_r1", CubeListBuilder.create().texOffs(0, 55).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.9425F, -0.2443F, 0.0524F));

		PartDefinition tendril_ll_3_grp = tendril_ll_2_grp.addOrReplaceChild("tendril_ll_3_grp", CubeListBuilder.create(), PartPose.offset(-1.106F, 4.803F, 3.422F));
		PartDefinition tendril_ll_3_r1 = tendril_ll_3_grp.addOrReplaceChild("tendril_ll_3_r1", CubeListBuilder.create().texOffs(114, 41).addBox(-0.5F, -1.0F, -0.4F, 1.0F, 2.0F, 6.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.2566F, -0.1396F, 0.0349F));

		PartDefinition tendril_lr_1_grp = chest_grp.addOrReplaceChild("tendril_lr_1_grp", CubeListBuilder.create(), PartPose.offset(3.0F, -3.8F, 2.8F));
		PartDefinition tendril_lr_1_r1 = tendril_lr_1_grp.addOrReplaceChild("tendril_lr_1_r1", CubeListBuilder.create().texOffs(11, 58).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.14F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4887F, 0.3491F, -0.0698F));

		PartDefinition tendril_lr_2_grp = tendril_lr_1_grp.addOrReplaceChild("tendril_lr_2_grp", CubeListBuilder.create(), PartPose.offset(2.004F, 2.684F, 4.978F));
		PartDefinition tendril_lr_2_r1 = tendril_lr_2_grp.addOrReplaceChild("tendril_lr_2_r1", CubeListBuilder.create().texOffs(77, 59).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.9425F, 0.2443F, -0.0524F));

		PartDefinition tendril_lr_3_grp = tendril_lr_2_grp.addOrReplaceChild("tendril_lr_3_grp", CubeListBuilder.create(), PartPose.offset(1.106F, 4.803F, 3.422F));
		PartDefinition tendril_lr_3_r1 = tendril_lr_3_grp.addOrReplaceChild("tendril_lr_3_r1", CubeListBuilder.create().texOffs(0, 64).addBox(-0.5F, -1.0F, -0.4F, 1.0F, 2.0F, 6.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.2566F, 0.1396F, -0.0349F));

		PartDefinition head_grp = body_root.addOrReplaceChild("head_grp", CubeListBuilder.create().texOffs(62, 68).addBox(-7.0F, -7.0F, -7.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(94, 68).addBox(-7.0F, -7.0F, 5.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(50, 11).addBox(-7.0F, -7.0F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(-0.005F))
				.texOffs(0, 13).addBox(5.0F, -7.0F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(-0.005F))
				.texOffs(0, 72).addBox(-7.0F, 5.0F, -7.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(32, 72).addBox(-7.0F, 5.0F, 5.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(64, 13).addBox(-7.0F, 5.0F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(-0.005F))
				.texOffs(78, 18).addBox(5.0F, 5.0F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(-0.005F))
				.texOffs(38, 0).addBox(-5.0F, 5.9F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(-0.02F))
				.texOffs(64, 72).addBox(-7.0F, -5.0F, -7.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.01F))
				.texOffs(72, 72).addBox(5.0F, -5.0F, -7.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.01F))
				.texOffs(80, 72).addBox(-7.0F, -5.0F, 5.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.01F))
				.texOffs(88, 72).addBox(5.0F, -5.0F, 5.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.01F))
				.texOffs(58, 48).addBox(-5.0F, -3.6F, -6.1F, 10.0F, 1.0F, 1.0F, new CubeDeformation(-0.16F))
				.texOffs(0, 76).addBox(-5.0F, 2.6F, -6.1F, 10.0F, 1.0F, 1.0F, new CubeDeformation(-0.16F))
				.texOffs(120, 30).addBox(-3.8F, -5.0F, -6.1F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.22F))
				.texOffs(124, 30).addBox(2.8F, -5.0F, -6.1F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.22F))
				.texOffs(22, 76).addBox(-5.0F, -3.6F, 5.1F, 10.0F, 1.0F, 1.0F, new CubeDeformation(-0.16F))
				.texOffs(0, 78).addBox(-5.0F, 2.6F, 5.1F, 10.0F, 1.0F, 1.0F, new CubeDeformation(-0.16F))
				.texOffs(124, 59).addBox(-3.8F, -5.0F, 5.1F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.22F))
				.texOffs(96, 72).addBox(2.8F, -5.0F, 5.1F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.22F))
				.texOffs(16, 37).addBox(-6.1F, -3.6F, -5.0F, 1.0F, 1.0F, 10.0F, new CubeDeformation(-0.16F))
				.texOffs(60, 37).addBox(-6.1F, 2.6F, -5.0F, 1.0F, 1.0F, 10.0F, new CubeDeformation(-0.16F))
				.texOffs(100, 72).addBox(-6.1F, -5.0F, -3.8F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.22F))
				.texOffs(104, 72).addBox(-6.1F, -5.0F, 2.8F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.22F))
				.texOffs(0, 44).addBox(5.1F, -3.6F, -5.0F, 1.0F, 1.0F, 10.0F, new CubeDeformation(-0.16F))
				.texOffs(34, 44).addBox(5.1F, 2.6F, -5.0F, 1.0F, 1.0F, 10.0F, new CubeDeformation(-0.16F))
				.texOffs(108, 72).addBox(5.1F, -5.0F, -3.8F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.22F))
				.texOffs(112, 72).addBox(5.1F, -5.0F, 2.8F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.22F)), PartPose.offset(0.0F, -20.5F, 0.0F));

		PartDefinition void_core_grp = head_grp.addOrReplaceChild("void_core_grp", CubeListBuilder.create().texOffs(102, 18).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(-0.35F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition eyes_grp = head_grp.addOrReplaceChild("eyes_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 2.2F, -5.8F));
		PartDefinition eye_l_r1 = eyes_grp.addOrReplaceChild("eye_l_r1", CubeListBuilder.create().texOffs(41, 0).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.06F)), PartPose.offsetAndRotation(-1.75F, -0.05F, 0.0F, 0.0F, 0.0F, -0.2618F));
		PartDefinition eye_r_r1 = eyes_grp.addOrReplaceChild("eye_r_r1", CubeListBuilder.create().texOffs(72, 0).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.06F)), PartPose.offsetAndRotation(1.75F, -0.05F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition leg_l_grp = body_root.addOrReplaceChild("leg_l_grp", CubeListBuilder.create().texOffs(88, 30).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(-0.02F)), PartPose.offset(-3.0F, 6.0F, 0.0F));

		PartDefinition shin_l_grp = leg_l_grp.addOrReplaceChild("shin_l_grp", CubeListBuilder.create().texOffs(110, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(-0.04F)), PartPose.offset(0.0F, 13.0F, 0.0F));

		PartDefinition foot_l_grp = shin_l_grp.addOrReplaceChild("foot_l_grp", CubeListBuilder.create().texOffs(0, 34).addBox(-2.6F, 0.0F, -5.6F, 5.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, 0.0F));

		PartDefinition leg_r_grp = body_root.addOrReplaceChild("leg_r_grp", CubeListBuilder.create().texOffs(104, 30).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(-0.02F)), PartPose.offset(3.0F, 6.0F, 0.0F));

		PartDefinition shin_r_grp = leg_r_grp.addOrReplaceChild("shin_r_grp", CubeListBuilder.create().texOffs(34, 11).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(-0.04F)), PartPose.offset(0.0F, 13.0F, 0.0F));

		PartDefinition foot_r_grp = shin_r_grp.addOrReplaceChild("foot_r_grp", CubeListBuilder.create().texOffs(44, 34).addBox(-2.4F, 0.0F, -5.6F, 5.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(VoidRiftShadeRenderState state) {
		super.setupAnim(state);

		if (!state.deathAnimationState.isStarted()
				&& !state.blinkOutAnimationState.isStarted()
				&& !state.blinkInAnimationState.isStarted()
				&& !state.aggroAnimationState.isStarted()) {
			float clampedYaw = Mth.clamp(state.yRot, -40.0F, 40.0F) * Mth.DEG_TO_RAD;
			float clampedPitch = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
			this.neck_grp.yRot += clampedYaw * 0.25F;
			this.head_grp.yRot += clampedYaw * 0.75F;
			this.head_grp.xRot += clampedPitch * 0.65F;
		}

		if (state.deathAnimationState.isStarted()) {
			this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
		} else if (state.blinkOutAnimationState.isStarted()) {
			this.blink_outAnimation.apply(state.blinkOutAnimationState, state.ageInTicks);
		} else if (state.blinkInAnimationState.isStarted()) {
			this.blink_inAnimation.apply(state.blinkInAnimationState, state.ageInTicks);
		} else if (state.hurtStaggerAnimationState.isStarted()) {
			this.hurt_staggerAnimation.apply(state.hurtStaggerAnimationState, state.ageInTicks);
		} else if (state.attackAnimationState.isStarted()) {
			this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
		} else if (state.aggroAnimationState.isStarted()) {
			this.aggroAnimation.apply(state.aggroAnimationState, state.ageInTicks);
		} else if (state.hurtAnimationState.isStarted()) {
			this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);
		} else if (state.walkAnimationState.isStarted()) {
			this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
		} else {
			this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
		}
	}
}
