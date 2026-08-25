package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CryoStalkerModel extends EntityModel<CryoStalkerRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_stalker"), "main");
	private final ModelPart body;
	private final ModelPart spike_d1;
	private final ModelPart spike_d2;
	private final ModelPart spike_d3;
	private final ModelPart neck;
	private final ModelPart throat_spike;
	private final ModelPart head;
	private final ModelPart crest_spike;
	private final ModelPart ear_left;
	private final ModelPart ear_right;
	private final ModelPart cheek_spike_l;
	private final ModelPart cheek_spike_r;
	private final ModelPart body_mid;
	private final ModelPart spike_d4;
	private final ModelPart spike_d5;
	private final ModelPart body_rear;
	private final ModelPart spike_d6;
	private final ModelPart tail_1;
	private final ModelPart tail_2;
	private final ModelPart tail_3;
	private final ModelPart tail_4;
	private final ModelPart tail_5;
	private final ModelPart tail_6;
	private final ModelPart tail_tip_center;
	private final ModelPart tail_tip_left;
	private final ModelPart tail_tip_right;
	private final ModelPart tail_tip_top;
	private final ModelPart leg_back_left;
	private final ModelPart thigh_back_left;
	private final ModelPart shank_back_left;
	private final ModelPart paw_back_left;
	private final ModelPart leg_back_right;
	private final ModelPart thigh_back_right;
	private final ModelPart shank_back_right;
	private final ModelPart paw_back_right;
	private final ModelPart leg_front_left;
	private final ModelPart arm_front_left;
	private final ModelPart forearm_front_left;
	private final ModelPart wrist_quill_fl;
	private final ModelPart paw_front_left;
	private final ModelPart leg_front_right;
	private final ModelPart arm_front_right;
	private final ModelPart forearm_front_right;
	private final ModelPart wrist_quill_fr;
	private final ModelPart paw_front_right;

	private final KeyframeAnimation idleAnimation;
	private final KeyframeAnimation walkAnimation;
	private final KeyframeAnimation runAnimation;
	private final KeyframeAnimation jumpAnimation;
	private final KeyframeAnimation attackAnimation;
	private final KeyframeAnimation swipeAnimation;
	private final KeyframeAnimation biteAnimation;

	public CryoStalkerModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.spike_d1 = this.body.getChild("spike_d1");
		this.spike_d2 = this.body.getChild("spike_d2");
		this.spike_d3 = this.body.getChild("spike_d3");
		this.neck = this.body.getChild("neck");
		this.throat_spike = this.neck.getChild("throat_spike");
		this.head = this.neck.getChild("head");
		this.crest_spike = this.head.getChild("crest_spike");
		this.ear_left = this.head.getChild("ear_left");
		this.ear_right = this.head.getChild("ear_right");
		this.cheek_spike_l = this.head.getChild("cheek_spike_l");
		this.cheek_spike_r = this.head.getChild("cheek_spike_r");
		this.body_mid = this.body.getChild("body_mid");
		this.spike_d4 = this.body_mid.getChild("spike_d4");
		this.spike_d5 = this.body_mid.getChild("spike_d5");
		this.body_rear = this.body_mid.getChild("body_rear");
		this.spike_d6 = this.body_rear.getChild("spike_d6");
		this.tail_1 = this.body_rear.getChild("tail_1");
		this.tail_2 = this.tail_1.getChild("tail_2");
		this.tail_3 = this.tail_2.getChild("tail_3");
		this.tail_4 = this.tail_3.getChild("tail_4");
		this.tail_5 = this.tail_4.getChild("tail_5");
		this.tail_6 = this.tail_5.getChild("tail_6");
		this.tail_tip_center = this.tail_6.getChild("tail_tip_center");
		this.tail_tip_left = this.tail_6.getChild("tail_tip_left");
		this.tail_tip_right = this.tail_6.getChild("tail_tip_right");
		this.tail_tip_top = this.tail_6.getChild("tail_tip_top");
		this.leg_back_left = this.body_rear.getChild("leg_back_left");
		this.thigh_back_left = this.leg_back_left.getChild("thigh_back_left");
		this.shank_back_left = this.thigh_back_left.getChild("shank_back_left");
		this.paw_back_left = this.shank_back_left.getChild("paw_back_left");
		this.leg_back_right = this.body_rear.getChild("leg_back_right");
		this.thigh_back_right = this.leg_back_right.getChild("thigh_back_right");
		this.shank_back_right = this.thigh_back_right.getChild("shank_back_right");
		this.paw_back_right = this.shank_back_right.getChild("paw_back_right");
		this.leg_front_left = this.body.getChild("leg_front_left");
		this.arm_front_left = this.leg_front_left.getChild("arm_front_left");
		this.forearm_front_left = this.arm_front_left.getChild("forearm_front_left");
		this.wrist_quill_fl = this.forearm_front_left.getChild("wrist_quill_fl");
		this.paw_front_left = this.forearm_front_left.getChild("paw_front_left");
		this.leg_front_right = this.body.getChild("leg_front_right");
		this.arm_front_right = this.leg_front_right.getChild("arm_front_right");
		this.forearm_front_right = this.arm_front_right.getChild("forearm_front_right");
		this.wrist_quill_fr = this.forearm_front_right.getChild("wrist_quill_fr");
		this.paw_front_right = this.forearm_front_right.getChild("paw_front_right");

		this.idleAnimation = CryoStalkerAnimation.idle.bake(root);
		this.walkAnimation = CryoStalkerAnimation.walk.bake(root);
		this.runAnimation = CryoStalkerAnimation.run.bake(root);
		this.jumpAnimation = CryoStalkerAnimation.jump.bake(root);
		this.attackAnimation = CryoStalkerAnimation.attack.bake(root);
		this.swipeAnimation = CryoStalkerAnimation.swipe.bake(root);
		this.biteAnimation = CryoStalkerAnimation.bite.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -6.5F, -7.0F, 14.0F, 12.5F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.5F, -3.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition spike_d1 = body.addOrReplaceChild("spike_d1", CubeListBuilder.create().texOffs(0, 85).addBox(-1.2F, -4.0F, -1.2F, 2.4F, 5.5F, 2.4F, new CubeDeformation(0.0F))
		.texOffs(8, 85).addBox(-0.6F, -7.5F, -0.6F, 1.2F, 3.5F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.5F, -4.5F, -0.6109F, 0.0F, 0.0F));

		PartDefinition spike_d2 = body.addOrReplaceChild("spike_d2", CubeListBuilder.create().texOffs(16, 85).addBox(-1.75F, -4.5F, -1.75F, 3.5F, 6.0F, 3.5F, new CubeDeformation(0.0F))
		.texOffs(28, 85).addBox(-1.1F, -8.0F, -1.1F, 2.2F, 3.5F, 2.2F, new CubeDeformation(0.0F))
		.texOffs(36, 85).addBox(-0.5F, -11.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition spike_d3 = body.addOrReplaceChild("spike_d3", CubeListBuilder.create().texOffs(44, 85).addBox(-1.3F, -4.0F, -1.3F, 2.6F, 5.5F, 2.6F, new CubeDeformation(0.0F))
		.texOffs(54, 85).addBox(-0.6F, -7.5F, -0.6F, 1.2F, 3.5F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.5F, 4.5F, -0.6981F, 0.0F, 0.0F));

		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(98, 0).addBox(-4.0F, -1.0F, -7.0F, 8.0F, 7.5F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -6.0F, -0.3142F, 0.0F, 0.0F));

		PartDefinition throat_spike = neck.addOrReplaceChild("throat_spike", CubeListBuilder.create().texOffs(62, 85).addBox(-0.75F, -1.5F, -0.75F, 1.5F, 5.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, -3.5F, 0.6109F, 0.0F, 0.0F));

		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(44, 25).addBox(-4.5F, -3.0F, -7.0F, 9.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(76, 25).addBox(-3.5F, -0.5F, -13.0F, 7.0F, 4.5F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(102, 25).addBox(-3.0F, 4.0F, -12.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(70, 85).addBox(-3.2F, 0.5F, -11.5F, 0.6F, 3.5F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(70, 85).addBox(2.6F, 0.5F, -11.5F, 0.6F, 3.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, -7.0F));

		PartDefinition crest_spike = head.addOrReplaceChild("crest_spike", CubeListBuilder.create().texOffs(76, 85).addBox(-1.1F, -4.5F, -1.0F, 2.2F, 5.5F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(84, 85).addBox(-0.5F, -8.5F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, -3.0F, -0.5236F, 0.0F, 0.0F));

		PartDefinition ear_left = head.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(92, 85).addBox(-1.0F, -3.5F, -1.0F, 2.0F, 4.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -2.0F, -1.5F, -0.2618F, -0.2618F, -0.3491F));

		PartDefinition ear_right = head.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(92, 85).addBox(-1.0F, -3.5F, -1.0F, 2.0F, 4.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -2.0F, -1.5F, -0.2618F, 0.2618F, 0.3491F));

		PartDefinition cheek_spike_l = head.addOrReplaceChild("cheek_spike_l", CubeListBuilder.create().texOffs(100, 85).addBox(-1.5F, -1.0F, -4.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(108, 85).addBox(-1.0F, -0.7F, -7.5F, 1.2F, 1.4F, 3.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 2.0F, -3.0F, 0.0F, 0.6109F, 0.2618F));

		PartDefinition cheek_spike_r = head.addOrReplaceChild("cheek_spike_r", CubeListBuilder.create().texOffs(100, 85).addBox(-0.5F, -1.0F, -4.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(108, 85).addBox(-0.2F, -0.7F, -7.5F, 1.2F, 1.4F, 3.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 2.0F, -3.0F, 0.0F, -0.6109F, -0.2618F));

		PartDefinition body_mid = body.addOrReplaceChild("body_mid", CubeListBuilder.create().texOffs(56, 0).addBox(-5.5F, -4.5F, 0.0F, 11.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.0F, -0.3491F, 0.0F, 0.0F));

		PartDefinition spike_d4 = body_mid.addOrReplaceChild("spike_d4", CubeListBuilder.create().texOffs(0, 95).addBox(-1.1F, -4.0F, -1.0F, 2.2F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(8, 95).addBox(-0.5F, -7.5F, -0.5F, 1.0F, 3.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, 4.0F, -0.6109F, 0.0F, 0.0F));

		PartDefinition spike_d5 = body_mid.addOrReplaceChild("spike_d5", CubeListBuilder.create().texOffs(16, 95).addBox(-1.1F, -4.0F, -1.0F, 2.2F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(24, 95).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, 8.0F, -0.6109F, 0.0F, 0.0F));

		PartDefinition body_rear = body_mid.addOrReplaceChild("body_rear", CubeListBuilder.create().texOffs(0, 25).addBox(-6.0F, -5.0F, 0.0F, 12.0F, 9.5F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 10.0F, -0.1745F, 0.0F, 0.0F));

		PartDefinition spike_d6 = body_rear.addOrReplaceChild("spike_d6", CubeListBuilder.create().texOffs(32, 95).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(40, 95).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

		PartDefinition tail_1 = body_rear.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(0, 45).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 9.5F, 0.1745F, 0.0F, 0.0F));

		PartDefinition tail_2 = tail_1.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(18, 45).addBox(-1.75F, -1.25F, 0.0F, 3.5F, 2.5F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 4.5F, 0.1396F, 0.0F, 0.0F));

		PartDefinition tail_3 = tail_2.addOrReplaceChild("tail_3", CubeListBuilder.create().texOffs(35, 45).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 4.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 4.5F, 0.0873F, 0.0F, 0.0F));

		PartDefinition tail_4 = tail_3.addOrReplaceChild("tail_4", CubeListBuilder.create().texOffs(50, 45).addBox(-1.25F, -0.75F, 0.0F, 2.5F, 1.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 4.0F));

		PartDefinition tail_5 = tail_4.addOrReplaceChild("tail_5", CubeListBuilder.create().texOffs(63, 45).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 3.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.5F, -0.0873F, 0.0F, 0.0F));

		PartDefinition tail_6 = tail_5.addOrReplaceChild("tail_6", CubeListBuilder.create().texOffs(74, 45).addBox(-0.75F, -0.4F, 0.0F, 1.5F, 0.8F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, -0.1396F, 0.0F, 0.0F));

		PartDefinition tail_tip_center = tail_6.addOrReplaceChild("tail_tip_center", CubeListBuilder.create().texOffs(48, 95).addBox(-0.5F, -0.4F, 0.0F, 1.0F, 0.8F, 4.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 3.0F));

		PartDefinition tail_tip_left = tail_6.addOrReplaceChild("tail_tip_left", CubeListBuilder.create().texOffs(48, 95).addBox(-0.5F, -0.4F, 0.0F, 0.75F, 0.8F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.75F, 0.0F, 2.5F, 0.0F, -0.2618F, 0.0F));

		PartDefinition tail_tip_right = tail_6.addOrReplaceChild("tail_tip_right", CubeListBuilder.create().texOffs(48, 95).addBox(-0.25F, -0.4F, 0.0F, 0.75F, 0.8F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.75F, 0.0F, 2.5F, 0.0F, 0.2618F, 0.0F));

		PartDefinition tail_tip_top = tail_6.addOrReplaceChild("tail_tip_top", CubeListBuilder.create().texOffs(48, 95).addBox(-0.4F, -0.6F, 0.0F, 0.8F, 0.8F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.4F, 2.5F, 0.1745F, 0.0F, 0.0F));

		PartDefinition leg_back_left = body_rear.addOrReplaceChild("leg_back_left", CubeListBuilder.create().texOffs(18, 65).addBox(-2.8F, -1.0F, -2.8F, 4.6F, 6.0F, 6.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, -1.5F, 1.0F, 0.0F, 0.0F, 0.1396F));

		PartDefinition thigh_back_left = leg_back_left.addOrReplaceChild("thigh_back_left", CubeListBuilder.create().texOffs(38, 65).addBox(-2.3F, -0.5F, -1.7F, 3.6F, 5.5F, 3.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, 0.5F, 0.4363F, 0.0F, 0.0F));

		PartDefinition shank_back_left = thigh_back_left.addOrReplaceChild("shank_back_left", CubeListBuilder.create().texOffs(54, 65).addBox(-1.9F, -0.5F, -1.3F, 2.8F, 4.5F, 2.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.6109F, 0.0F, 0.0F));

		PartDefinition paw_back_left = shank_back_left.addOrReplaceChild("paw_back_left", CubeListBuilder.create().texOffs(70, 65).addBox(-3.0F, -1.3F, -2.5F, 5.0F, 2.5F, 5.5F, new CubeDeformation(0.0F))
		.texOffs(64, 95).addBox(0.7F, -0.2F, -5.0F, 1.0F, 1.4F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(64, 95).addBox(-0.8F, -0.2F, -5.5F, 1.0F, 1.4F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(64, 95).addBox(-2.3F, -0.2F, -5.0F, 1.0F, 1.4F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.8F, 0.0F, 0.4363F, 0.0F, -0.1396F));

		PartDefinition leg_back_right = body_rear.addOrReplaceChild("leg_back_right", CubeListBuilder.create().texOffs(18, 65).addBox(-1.8F, -1.0F, -2.8F, 4.6F, 6.0F, 6.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, -1.5F, 1.0F, 0.0F, 0.0F, -0.1396F));

		PartDefinition thigh_back_right = leg_back_right.addOrReplaceChild("thigh_back_right", CubeListBuilder.create().texOffs(38, 65).addBox(-1.3F, -0.5F, -1.7F, 3.6F, 5.5F, 3.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, 0.5F, 0.4363F, 0.0F, 0.0F));

		PartDefinition shank_back_right = thigh_back_right.addOrReplaceChild("shank_back_right", CubeListBuilder.create().texOffs(54, 65).addBox(-0.9F, -0.5F, -1.3F, 2.8F, 4.5F, 2.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.6109F, 0.0F, 0.0F));

		PartDefinition paw_back_right = shank_back_right.addOrReplaceChild("paw_back_right", CubeListBuilder.create().texOffs(70, 65).addBox(-2.0F, -1.3F, -2.5F, 5.0F, 2.5F, 5.5F, new CubeDeformation(0.0F))
		.texOffs(64, 95).addBox(-1.7F, -0.2F, -5.0F, 1.0F, 1.4F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(64, 95).addBox(-0.2F, -0.2F, -5.5F, 1.0F, 1.4F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(64, 95).addBox(1.3F, -0.2F, -5.0F, 1.0F, 1.4F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.8F, 0.0F, 0.4363F, 0.0F, 0.1396F));

		PartDefinition leg_front_left = body.addOrReplaceChild("leg_front_left", CubeListBuilder.create().texOffs(76, 45).addBox(-2.1F, -1.0F, -2.7F, 4.2F, 6.0F, 5.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5F, -1.5F, -1.5F, 0.0F, 0.0F, 0.1745F));

		PartDefinition arm_front_left = leg_front_left.addOrReplaceChild("arm_front_left", CubeListBuilder.create().texOffs(96, 45).addBox(-1.7F, -0.5F, -1.8F, 3.4F, 6.0F, 3.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.3142F, 0.0F, 0.0F));

		PartDefinition forearm_front_left = arm_front_left.addOrReplaceChild("forearm_front_left", CubeListBuilder.create().texOffs(109, 45).addBox(-1.4F, -0.5F, -1.3F, 2.8F, 5.0F, 2.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.576F, 0.0F, 0.0F));

		PartDefinition wrist_quill_fl = forearm_front_left.addOrReplaceChild("wrist_quill_fl", CubeListBuilder.create().texOffs(56, 95).addBox(-0.9F, -1.0F, 0.0F, 1.8F, 1.5F, 4.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.5F, 1.3F, -0.4363F, 0.0F, 0.0F));

		PartDefinition paw_front_left = forearm_front_left.addOrReplaceChild("paw_front_left", CubeListBuilder.create().texOffs(0, 65).addBox(-2.3F, -1.3F, -3.5F, 4.6F, 2.5F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(64, 95).addBox(1.0F, -0.2F, -6.0F, 1.0F, 1.4F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(64, 95).addBox(-0.5F, -0.2F, -6.5F, 1.0F, 1.4F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(64, 95).addBox(-2.0F, -0.2F, -6.0F, 1.0F, 1.4F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.3F, 0.0F, -0.5236F, 0.0F, -0.1745F));

		PartDefinition leg_front_right = body.addOrReplaceChild("leg_front_right", CubeListBuilder.create().texOffs(76, 45).addBox(-2.1F, -1.0F, -2.7F, 4.2F, 6.0F, 5.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, -1.5F, -1.5F, 0.0F, 0.0F, -0.1745F));

		PartDefinition arm_front_right = leg_front_right.addOrReplaceChild("arm_front_right", CubeListBuilder.create().texOffs(96, 45).addBox(-1.7F, -0.5F, -1.8F, 3.4F, 6.0F, 3.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.3142F, 0.0F, 0.0F));

		PartDefinition forearm_front_right = arm_front_right.addOrReplaceChild("forearm_front_right", CubeListBuilder.create().texOffs(109, 45).addBox(-1.4F, -0.5F, -1.3F, 2.8F, 5.0F, 2.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.576F, 0.0F, 0.0F));

		PartDefinition wrist_quill_fr = forearm_front_right.addOrReplaceChild("wrist_quill_fr", CubeListBuilder.create().texOffs(56, 95).addBox(-0.9F, -1.0F, 0.0F, 1.8F, 1.5F, 4.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.5F, 1.3F, -0.4363F, 0.0F, 0.0F));

		PartDefinition paw_front_right = forearm_front_right.addOrReplaceChild("paw_front_right", CubeListBuilder.create().texOffs(0, 65).addBox(-2.3F, -1.3F, -3.5F, 4.6F, 2.5F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(64, 95).addBox(-2.0F, -0.2F, -6.0F, 1.0F, 1.4F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(64, 95).addBox(-0.5F, -0.2F, -6.5F, 1.0F, 1.4F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(64, 95).addBox(1.0F, -0.2F, -6.0F, 1.0F, 1.4F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.3F, 0.0F, -0.5236F, 0.0F, 0.1745F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(CryoStalkerRenderState state) {
		super.setupAnim(state);

		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

		this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
		this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
		this.runAnimation.apply(state.runAnimationState, state.ageInTicks);
		this.jumpAnimation.apply(state.jumpAnimationState, state.ageInTicks);
		this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
		this.swipeAnimation.apply(state.swipeAnimationState, state.ageInTicks);
		this.biteAnimation.apply(state.biteAnimationState, state.ageInTicks);
	}
}
