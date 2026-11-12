package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.Entropica;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class AurorafowlModel extends EntityModel<AurorafowlRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aurorafowl"), "main");
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart neck_lower;
	private final ModelPart neck_upper;
	private final ModelPart head;
	private final ModelPart crest_seg1;
	private final ModelPart crest_seg2;
	private final ModelPart crest_seg3;
	private final ModelPart crest_seg4;
	private final ModelPart tail_base;
	private final ModelPart tail_feather_center;
	private final ModelPart tail_feather_mid_left;
	private final ModelPart tail_feather_mid_right;
	private final ModelPart tail_feather_outer_left;
	private final ModelPart tail_feather_outer_right;
	private final ModelPart wing_left_upper;
	private final ModelPart wing_lu_mid;
	private final ModelPart wing_lu_tip;
	private final ModelPart wing_left_lower;
	private final ModelPart wing_ll_mid;
	private final ModelPart wing_ll_tip;
	private final ModelPart wing_right_upper;
	private final ModelPart wing_ru_mid;
	private final ModelPart wing_ru_tip;
	private final ModelPart wing_right_lower;
	private final ModelPart wing_rl_mid;
	private final ModelPart wing_rl_tip;
	private final ModelPart leg_left;
	private final ModelPart shin_left;
	private final ModelPart foot_left;
	private final ModelPart leg_right;
	private final ModelPart shin_right;
	private final ModelPart foot_right;

	private final KeyframeAnimation idleGroundAnimation;
	private final KeyframeAnimation walkAnimation;
	private final KeyframeAnimation idleFlightAnimation;
	private final KeyframeAnimation flyingAnimation;
	private final KeyframeAnimation takingOffAnimation;
	private final KeyframeAnimation landingAnimation;
	private final KeyframeAnimation attackGroundAnimation;
	private final KeyframeAnimation attackFlyingAnimation;

	public AurorafowlModel(ModelPart root) {
		super(root);
		this.root = root.getChild("root");
		this.body = this.root.getChild("body");
		this.neck_lower = this.body.getChild("neck_lower");
		this.neck_upper = this.neck_lower.getChild("neck_upper");
		this.head = this.neck_upper.getChild("head");
		this.crest_seg1 = this.head.getChild("crest_seg1");
		this.crest_seg2 = this.crest_seg1.getChild("crest_seg2");
		this.crest_seg3 = this.crest_seg2.getChild("crest_seg3");
		this.crest_seg4 = this.crest_seg3.getChild("crest_seg4");
		this.tail_base = this.body.getChild("tail_base");
		this.tail_feather_center = this.tail_base.getChild("tail_feather_center");
		this.tail_feather_mid_left = this.tail_base.getChild("tail_feather_mid_left");
		this.tail_feather_mid_right = this.tail_base.getChild("tail_feather_mid_right");
		this.tail_feather_outer_left = this.tail_base.getChild("tail_feather_outer_left");
		this.tail_feather_outer_right = this.tail_base.getChild("tail_feather_outer_right");
		this.wing_left_upper = this.body.getChild("wing_left_upper");
		this.wing_lu_mid = this.wing_left_upper.getChild("wing_lu_mid");
		this.wing_lu_tip = this.wing_lu_mid.getChild("wing_lu_tip");
		this.wing_left_lower = this.body.getChild("wing_left_lower");
		this.wing_ll_mid = this.wing_left_lower.getChild("wing_ll_mid");
		this.wing_ll_tip = this.wing_ll_mid.getChild("wing_ll_tip");
		this.wing_right_upper = this.body.getChild("wing_right_upper");
		this.wing_ru_mid = this.wing_right_upper.getChild("wing_ru_mid");
		this.wing_ru_tip = this.wing_ru_mid.getChild("wing_ru_tip");
		this.wing_right_lower = this.body.getChild("wing_right_lower");
		this.wing_rl_mid = this.wing_right_lower.getChild("wing_rl_mid");
		this.wing_rl_tip = this.wing_rl_mid.getChild("wing_rl_tip");
		this.leg_left = this.root.getChild("leg_left");
		this.shin_left = this.leg_left.getChild("shin_left");
		this.foot_left = this.shin_left.getChild("foot_left");
		this.leg_right = this.root.getChild("leg_right");
		this.shin_right = this.leg_right.getChild("shin_right");
		this.foot_right = this.shin_right.getChild("foot_right");

		this.idleGroundAnimation = AurorafowlAnimation.idle_ground.bake(root);
		this.walkAnimation = AurorafowlAnimation.walk.bake(root);
		this.idleFlightAnimation = AurorafowlAnimation.idle_flight.bake(root);
		this.flyingAnimation = AurorafowlAnimation.flying.bake(root);
		this.takingOffAnimation = AurorafowlAnimation.taking_off.bake(root);
		this.landingAnimation = AurorafowlAnimation.landing.bake(root);
		this.attackGroundAnimation = AurorafowlAnimation.attack_ground.bake(root);
		this.attackFlyingAnimation = AurorafowlAnimation.attack_flying.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -4.0F, -8.0F, 10.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(28, 52).addBox(-4.5F, -7.0F, -11.0F, 9.0F, 7.0F, 11.5F, new CubeDeformation(0.0F))
		.texOffs(36, 71).addBox(-4.0F, -2.0F, -13.0F, 8.0F, 10.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 71).addBox(-3.5F, -8.0F, -5.0F, 7.0F, 4.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(70, 52).addBox(-4.5F, -1.0F, 7.5F, 9.0F, 10.0F, 6.5F, new CubeDeformation(0.0F))
		.texOffs(0, 51).addBox(-6.5F, -2.0F, -6.0F, 1.7F, 8.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 51).mirror().addBox(4.8F, -2.0F, -6.0F, 1.7F, 8.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -44.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

		PartDefinition neck_lower = body.addOrReplaceChild("neck_lower", CubeListBuilder.create().texOffs(102, 27).addBox(-2.5F, -9.0F, -3.0F, 5.0F, 11.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -8.0F, -0.4887F, 0.0F, 0.0F));

		PartDefinition neck_upper = neck_lower.addOrReplaceChild("neck_upper", CubeListBuilder.create().texOffs(104, 44).addBox(-2.0F, -10.0F, -3.0F, 4.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, 0.2443F, 0.0F, 0.0F));

		PartDefinition head = neck_upper.addOrReplaceChild("head", CubeListBuilder.create().texOffs(102, 70).addBox(-2.5F, -6.0F, -6.0F, 5.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(46, 28).addBox(-2.5F, -8.5F, -2.0F, 1.0F, 2.5F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(46, 28).mirror().addBox(1.5F, -8.5F, -2.0F, 1.0F, 2.5F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(70, 70).addBox(-1.2F, -4.5F, -20.0F, 2.4F, 4.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(0, 87).addBox(-1.0F, -0.5F, -18.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.2443F, 0.0F, 0.0F));

		PartDefinition crest_seg1 = head.addOrReplaceChild("crest_seg1", CubeListBuilder.create().texOffs(116, 0).addBox(-1.0F, -1.5F, 0.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.5F, 2.0F, -0.2793F, 0.0F, 0.0F));

		PartDefinition crest_seg2 = crest_seg1.addOrReplaceChild("crest_seg2", CubeListBuilder.create().texOffs(116, 7).addBox(-0.8F, -1.3F, 0.0F, 1.6F, 2.8F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, -0.3142F, 0.0F, 0.0F));

		PartDefinition crest_seg3 = crest_seg2.addOrReplaceChild("crest_seg3", CubeListBuilder.create().texOffs(116, 14).addBox(-0.6F, -1.0F, 0.0F, 1.2F, 2.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 4.0F, -0.3491F, 0.0F, 0.0F));

		PartDefinition crest_seg4 = crest_seg3.addOrReplaceChild("crest_seg4", CubeListBuilder.create().texOffs(118, 60).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 2.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 4.0F, -0.384F, 0.0F, 0.0F));

		PartDefinition tail_base = body.addOrReplaceChild("tail_base", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 6.0F, 12.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition tail_feather_center = tail_base.addOrReplaceChild("tail_feather_center", CubeListBuilder.create().texOffs(52, 0).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 1.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tail_feather_mid_left = tail_base.addOrReplaceChild("tail_feather_mid_left", CubeListBuilder.create().texOffs(52, 27).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 1.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.1309F, 0.0F));

		PartDefinition tail_feather_mid_right = tail_base.addOrReplaceChild("tail_feather_mid_right", CubeListBuilder.create().texOffs(52, 27).mirror().addBox(-1.5F, -1.5F, 0.0F, 3.0F, 1.0F, 22.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, -0.1309F, 0.0F));

		PartDefinition tail_feather_outer_left = tail_base.addOrReplaceChild("tail_feather_outer_left", CubeListBuilder.create().texOffs(0, 28).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 1.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.2618F, 0.0F));

		PartDefinition tail_feather_outer_right = tail_base.addOrReplaceChild("tail_feather_outer_right", CubeListBuilder.create().texOffs(0, 28).mirror().addBox(-1.5F, -1.5F, 0.0F, 3.0F, 1.0F, 20.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, -0.2618F, 0.0F));

		PartDefinition wing_left_upper = body.addOrReplaceChild("wing_left_upper", CubeListBuilder.create().texOffs(56, 88).addBox(-17.0F, -2.0F, -3.0F, 17.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 101).addBox(-17.0F, -0.52F, 2.02F, 17.0F, 0.04F, 13.98F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -2.0F, -3.0F, -0.1776F, 0.2681F, 0.4578F));

		PartDefinition wing_lu_mid = wing_left_upper.addOrReplaceChild("wing_lu_mid", CubeListBuilder.create().texOffs(0, 115).addBox(-17.0F, -2.0F, -4.0F, 17.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(62, 101).addBox(-17.0F, -0.48F, 0.02F, 17.0F, 0.04F, 11.98F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.0524F));

		PartDefinition wing_lu_tip = wing_lu_mid.addOrReplaceChild("wing_lu_tip", CubeListBuilder.create().texOffs(62, 113).addBox(-17.0F, -0.44F, -2.98F, 17.0F, 0.04F, 11.98F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0524F));

		PartDefinition wing_left_lower = body.addOrReplaceChild("wing_left_lower", CubeListBuilder.create().texOffs(56, 88).addBox(-16.0F, -2.0F, -4.0F, 16.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 101).addBox(-16.0F, -0.52F, 0.02F, 16.0F, 0.04F, 11.98F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 4.0F, 2.0F, 0.0825F, 0.342F, 0.0273F));

		PartDefinition wing_ll_mid = wing_left_lower.addOrReplaceChild("wing_ll_mid", CubeListBuilder.create().texOffs(0, 115).addBox(-17.0F, -2.0F, -4.0F, 17.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(62, 101).addBox(-17.0F, -0.48F, 0.02F, 17.0F, 0.04F, 9.98F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0349F));

		PartDefinition wing_ll_tip = wing_ll_mid.addOrReplaceChild("wing_ll_tip", CubeListBuilder.create().texOffs(62, 113).addBox(-16.0F, -0.44F, -2.98F, 16.0F, 0.04F, 9.98F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0349F));

		PartDefinition wing_right_upper = body.addOrReplaceChild("wing_right_upper", CubeListBuilder.create().texOffs(56, 88).mirror().addBox(0.0F, -2.0F, -3.0F, 17.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 101).mirror().addBox(0.0F, -0.52F, 2.02F, 17.0F, 0.04F, 13.98F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, -2.0F, -3.0F, -0.1776F, -0.2681F, -0.4578F));

		PartDefinition wing_ru_mid = wing_right_upper.addOrReplaceChild("wing_ru_mid", CubeListBuilder.create().texOffs(0, 115).mirror().addBox(0.0F, -2.0F, -4.0F, 17.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(62, 101).mirror().addBox(0.0F, -0.48F, 0.02F, 17.0F, 0.04F, 11.98F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(17.0F, 0.0F, 3.0F, 0.0F, 0.0F, -0.0524F));

		PartDefinition wing_ru_tip = wing_ru_mid.addOrReplaceChild("wing_ru_tip", CubeListBuilder.create().texOffs(62, 113).mirror().addBox(0.0F, -0.44F, -2.98F, 17.0F, 0.04F, 11.98F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(17.0F, 0.0F, 2.0F, 0.0F, 0.0F, -0.0524F));

		PartDefinition wing_right_lower = body.addOrReplaceChild("wing_right_lower", CubeListBuilder.create().texOffs(56, 88).mirror().addBox(0.0F, -2.0F, -4.0F, 16.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 101).mirror().addBox(0.0F, -0.52F, 0.02F, 16.0F, 0.04F, 11.98F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, 4.0F, 2.0F, 0.0825F, -0.342F, -0.0273F));

		PartDefinition wing_rl_mid = wing_right_lower.addOrReplaceChild("wing_rl_mid", CubeListBuilder.create().texOffs(0, 115).mirror().addBox(0.0F, -2.0F, -4.0F, 17.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(62, 101).mirror().addBox(0.0F, -0.48F, 0.02F, 17.0F, 0.04F, 9.98F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(16.0F, 0.0F, 2.0F, 0.0F, 0.0F, -0.0349F));

		PartDefinition wing_rl_tip = wing_rl_mid.addOrReplaceChild("wing_rl_tip", CubeListBuilder.create().texOffs(62, 113).mirror().addBox(0.0F, -0.44F, -2.98F, 16.0F, 0.04F, 9.98F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(17.0F, 0.0F, 2.0F, 0.0F, 0.0F, -0.0349F));

		PartDefinition leg_left = root.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(102, 86).addBox(-1.5F, 0.0F, -2.5F, 3.0F, 10.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(36, 86).addBox(-2.0F, 9.0F, -3.0F, 4.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -42.0F, 2.0F, 0.4538F, 0.0F, 0.0F));

		PartDefinition shin_left = leg_left.addOrReplaceChild("shin_left", CubeListBuilder.create().texOffs(108, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 22.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, -0.7679F, 0.0F, 0.0F));

		PartDefinition foot_left = shin_left.addOrReplaceChild("foot_left", CubeListBuilder.create().texOffs(42, 115).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(104, 60).addBox(-0.8F, 0.5F, -7.0F, 1.6F, 1.5F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(116, 21).addBox(0.8F, 0.5F, -6.0F, 1.6F, 1.5F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(116, 21).mirror().addBox(-2.4F, 0.5F, -6.0F, 1.6F, 1.5F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(118, 86).addBox(-0.8F, 0.5F, 2.0F, 1.6F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 22.0F, 0.0F, 0.3142F, 0.0F, 0.0F));

		PartDefinition leg_right = root.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(102, 86).mirror().addBox(-1.5F, 0.0F, -2.5F, 3.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(36, 86).mirror().addBox(-2.0F, 9.0F, -3.0F, 4.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.0F, -42.0F, 2.0F, 0.4538F, 0.0F, 0.0F));

		PartDefinition shin_right = leg_right.addOrReplaceChild("shin_right", CubeListBuilder.create().texOffs(108, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 22.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, -0.7679F, 0.0F, 0.0F));

		PartDefinition foot_right = shin_right.addOrReplaceChild("foot_right", CubeListBuilder.create().texOffs(42, 115).mirror().addBox(-1.5F, 0.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(104, 60).mirror().addBox(-0.8F, 0.5F, -7.0F, 1.6F, 1.5F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(116, 21).mirror().addBox(0.8F, 0.5F, -6.0F, 1.6F, 1.5F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(116, 21).addBox(-2.4F, 0.5F, -6.0F, 1.6F, 1.5F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(118, 86).mirror().addBox(-0.8F, 0.5F, 2.0F, 1.6F, 1.5F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 22.0F, 0.0F, 0.3142F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(AurorafowlRenderState state) {
		super.setupAnim(state);
		
		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

		this.idleGroundAnimation.apply(state.idleGroundAnimationState, state.ageInTicks);
		this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
		this.idleFlightAnimation.apply(state.idleFlightAnimationState, state.ageInTicks);
		this.flyingAnimation.apply(state.flyingAnimationState, state.ageInTicks);
		this.takingOffAnimation.apply(state.takingOffAnimationState, state.ageInTicks);
		this.landingAnimation.apply(state.landingAnimationState, state.ageInTicks);
		this.attackGroundAnimation.apply(state.attackGroundAnimationState, state.ageInTicks);
		this.attackFlyingAnimation.apply(state.attackFlyingAnimationState, state.ageInTicks);
	}
}
