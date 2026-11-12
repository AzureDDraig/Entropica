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

public class BloomCrawlerModel extends EntityModel<BloomCrawlerRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "bloom_crawler"), "main");
	private final ModelPart hitbox;
	private final ModelPart body_front;
	private final ModelPart body_back;
	private final ModelPart tail;
	private final ModelPart shoulder_back_left;
	private final ModelPart leg_back_left;
	private final ModelPart shin_back_left;
	private final ModelPart ankle_back_left;
	private final ModelPart shoulder_back_right;
	private final ModelPart leg_back_right;
	private final ModelPart shin_back_right;
	private final ModelPart ankle_back_right;
	private final ModelPart flora_back;
	private final ModelPart flora_young;
	private final ModelPart flora_medium;
	private final ModelPart flora_old;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart jaw;
	private final ModelPart brow_l;
	private final ModelPart brow_r;
	private final ModelPart sensory_leaf_l;
	private final ModelPart leaf_l_mid;
	private final ModelPart leaf_l_tip;
	private final ModelPart sensory_leaf_r;
	private final ModelPart leaf_r_mid;
	private final ModelPart leaf_r_tip;
	private final ModelPart shoulder_front_left;
	private final ModelPart leg_front_left;
	private final ModelPart shin_front_left;
	private final ModelPart ankle_front_left;
	private final ModelPart shoulder_front_right;
	private final ModelPart leg_front_right;
	private final ModelPart shin_front_right;
	private final ModelPart ankle_front_right;
	private final ModelPart flora_front;
	private final ModelPart flora_mid;

	private final KeyframeAnimation idleAnimation;
	private final KeyframeAnimation walkAnimation;
	private final KeyframeAnimation runAnimation;
	private final KeyframeAnimation grazeAnimation;

	public BloomCrawlerModel(ModelPart root) {
		super(root);
		this.hitbox = root.getChild("hitbox");
		this.body_front = root.getChild("body_front");
		this.body_back = this.body_front.getChild("body_back");
		this.tail = this.body_back.getChild("tail");
		this.shoulder_back_left = this.body_back.getChild("shoulder_back_left");
		this.leg_back_left = this.shoulder_back_left.getChild("leg_back_left");
		this.shin_back_left = this.leg_back_left.getChild("shin_back_left");
		this.ankle_back_left = this.shin_back_left.getChild("ankle_back_left");
		this.shoulder_back_right = this.body_back.getChild("shoulder_back_right");
		this.leg_back_right = this.shoulder_back_right.getChild("leg_back_right");
		this.shin_back_right = this.leg_back_right.getChild("shin_back_right");
		this.ankle_back_right = this.shin_back_right.getChild("ankle_back_right");
		this.flora_back = this.body_back.getChild("flora_back");
		this.flora_young = this.flora_back.getChild("flora_young");
		this.flora_medium = this.flora_back.getChild("flora_medium");
		this.flora_old = this.flora_back.getChild("flora_old");
		this.neck = this.body_front.getChild("neck");
		this.head = this.neck.getChild("head");
		this.jaw = this.head.getChild("jaw");
		this.brow_l = this.head.getChild("brow_l");
		this.brow_r = this.head.getChild("brow_r");
		this.sensory_leaf_l = this.head.getChild("sensory_leaf_l");
		this.leaf_l_mid = this.sensory_leaf_l.getChild("leaf_l_mid");
		this.leaf_l_tip = this.leaf_l_mid.getChild("leaf_l_tip");
		this.sensory_leaf_r = this.head.getChild("sensory_leaf_r");
		this.leaf_r_mid = this.sensory_leaf_r.getChild("leaf_r_mid");
		this.leaf_r_tip = this.leaf_r_mid.getChild("leaf_r_tip");
		this.shoulder_front_left = this.body_front.getChild("shoulder_front_left");
		this.leg_front_left = this.shoulder_front_left.getChild("leg_front_left");
		this.shin_front_left = this.leg_front_left.getChild("shin_front_left");
		this.ankle_front_left = this.shin_front_left.getChild("ankle_front_left");
		this.shoulder_front_right = this.body_front.getChild("shoulder_front_right");
		this.leg_front_right = this.shoulder_front_right.getChild("leg_front_right");
		this.shin_front_right = this.leg_front_right.getChild("shin_front_right");
		this.ankle_front_right = this.shin_front_right.getChild("ankle_front_right");
		this.flora_front = this.body_front.getChild("flora_front");
		this.flora_mid = this.body_front.getChild("flora_mid");

		this.idleAnimation = BloomCrawlerAnimation.idle.bake(root);
		this.walkAnimation = BloomCrawlerAnimation.walk.bake(root);
		this.runAnimation = BloomCrawlerAnimation.run.bake(root);
		this.grazeAnimation = BloomCrawlerAnimation.graze.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition hitbox = partdefinition.addOrReplaceChild("hitbox", CubeListBuilder.create(), PartPose.offset(0.0F, 21.0F, 0.0F));

		PartDefinition body_front = partdefinition.addOrReplaceChild("body_front", CubeListBuilder.create().texOffs(0, 58).addBox(-7.0F, -3.5F, -9.0F, 14.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-6.0F, -11.0F, -1.0F, 12.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(24, 76).addBox(4.2F, -7.5F, -9.0F, 2.8F, 4.1F, 1.1F, new CubeDeformation(0.0F))
		.texOffs(32, 76).mirror().addBox(-7.0F, -7.5F, -9.0F, 2.8F, 4.1F, 1.1F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(40, 76).addBox(-4.2F, -7.5F, -9.0F, 8.4F, 2.8F, 1.1F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.5F, -4.0F));

		PartDefinition shell_lip_mid_r_r1 = body_front.addOrReplaceChild("shell_lip_mid_r_r1", CubeListBuilder.create().texOffs(38, 5).mirror().addBox(-1.0F, -1.0F, -4.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-10.0F, -2.5F, 3.5F, 0.0F, 0.0F, 0.6109F));

		PartDefinition shell_lip_mid_l_r1 = body_front.addOrReplaceChild("shell_lip_mid_l_r1", CubeListBuilder.create().texOffs(38, 5).addBox(-1.0F, -1.0F, -4.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0F, -2.5F, 3.5F, 0.0F, 0.0F, -0.6109F));

		PartDefinition shell_lip_front_r_r1 = body_front.addOrReplaceChild("shell_lip_front_r_r1", CubeListBuilder.create().texOffs(38, 5).mirror().addBox(-1.0F, -1.0F, -4.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-10.0F, -2.0F, -4.5F, 0.0F, 0.1745F, 0.6109F));

		PartDefinition shell_lip_front_l_r1 = body_front.addOrReplaceChild("shell_lip_front_l_r1", CubeListBuilder.create().texOffs(38, 5).addBox(-1.0F, -1.0F, -4.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0F, -2.0F, -4.5F, 0.0F, -0.1745F, -0.6109F));

		PartDefinition shell_lip_front_r1 = body_front.addOrReplaceChild("shell_lip_front_r1", CubeListBuilder.create().texOffs(38, 0).addBox(-7.0F, -1.0F, -1.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, -9.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition shell_side_mid_r_layer2_r1 = body_front.addOrReplaceChild("shell_side_mid_r_layer2_r1", CubeListBuilder.create().texOffs(17, 35).mirror().addBox(-0.99F, -2.0F, -2.5F, 1.98F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.5F, -6.0F, 3.5F, 0.0349F, 0.0F, 0.4887F));

		PartDefinition shell_side_mid_r_layer1_r1 = body_front.addOrReplaceChild("shell_side_mid_r_layer1_r1", CubeListBuilder.create().texOffs(0, 35).mirror().addBox(-1.49F, -2.0F, -3.5F, 2.98F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-8.5F, -6.0F, 3.5F, -0.0524F, 0.0F, 0.384F));

		PartDefinition shell_side_mid_r_base_r1 = body_front.addOrReplaceChild("shell_side_mid_r_base_r1", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(-1.99F, -3.0F, -4.5F, 3.98F, 6.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.5F, -6.0F, 3.5F, 0.0F, 0.0F, 0.4363F));

		PartDefinition shell_side_mid_l_layer2_r1 = body_front.addOrReplaceChild("shell_side_mid_l_layer2_r1", CubeListBuilder.create().texOffs(17, 35).addBox(-0.99F, -2.0F, -2.5F, 1.98F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5F, -6.0F, 3.5F, 0.0349F, 0.0F, -0.4887F));

		PartDefinition shell_side_mid_l_layer1_r1 = body_front.addOrReplaceChild("shell_side_mid_l_layer1_r1", CubeListBuilder.create().texOffs(0, 35).addBox(-1.49F, -2.0F, -3.5F, 2.98F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5F, -6.0F, 3.5F, -0.0524F, 0.0F, -0.384F));

		PartDefinition shell_side_mid_l_base_r1 = body_front.addOrReplaceChild("shell_side_mid_l_base_r1", CubeListBuilder.create().texOffs(0, 20).addBox(-1.99F, -3.0F, -4.5F, 3.98F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, -6.0F, 3.5F, 0.0F, 0.0F, -0.4363F));

		PartDefinition shell_side_front_r_layer2_r1 = body_front.addOrReplaceChild("shell_side_front_r_layer2_r1", CubeListBuilder.create().texOffs(17, 35).mirror().addBox(-1.0F, -2.0F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.5F, -5.5F, -4.5F, 0.0349F, 0.0873F, 0.4887F));

		PartDefinition shell_side_front_r_layer1_r1 = body_front.addOrReplaceChild("shell_side_front_r_layer1_r1", CubeListBuilder.create().texOffs(0, 35).mirror().addBox(-1.5F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-8.5F, -5.5F, -4.5F, -0.0349F, 0.1396F, 0.3491F));

		PartDefinition shell_side_front_r_base_r1 = body_front.addOrReplaceChild("shell_side_front_r_base_r1", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(-2.0F, -3.0F, -4.5F, 4.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.5F, -5.5F, -4.5F, 0.0F, 0.1745F, 0.4363F));

		PartDefinition shell_side_front_l_layer2_r1 = body_front.addOrReplaceChild("shell_side_front_l_layer2_r1", CubeListBuilder.create().texOffs(17, 35).addBox(-1.0F, -2.0F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5F, -5.5F, -4.5F, 0.0349F, -0.0873F, -0.4887F));

		PartDefinition shell_side_front_l_layer1_r1 = body_front.addOrReplaceChild("shell_side_front_l_layer1_r1", CubeListBuilder.create().texOffs(0, 35).addBox(-1.5F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5F, -5.5F, -4.5F, -0.0349F, -0.1396F, -0.3491F));

		PartDefinition shell_side_front_l_base_r1 = body_front.addOrReplaceChild("shell_side_front_l_base_r1", CubeListBuilder.create().texOffs(0, 20).addBox(-2.0F, -3.0F, -4.5F, 4.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, -5.5F, -4.5F, 0.0F, -0.1745F, -0.4363F));

		PartDefinition shell_top_mid_layer2_r1 = body_front.addOrReplaceChild("shell_top_mid_layer2_r1", CubeListBuilder.create().texOffs(22, 12).addBox(-3.0F, -0.5F, -2.5F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.52F, 3.5F, 0.0349F, 0.0F, 0.0175F));

		PartDefinition shell_top_mid_layer1_r1 = body_front.addOrReplaceChild("shell_top_mid_layer1_r1", CubeListBuilder.create().texOffs(0, 12).addBox(-4.0F, -0.5F, -3.5F, 8.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.51F, 3.5F, -0.0524F, 0.0F, -0.0175F));

		PartDefinition shell_top_front_layer2_r1 = body_front.addOrReplaceChild("shell_top_front_layer2_r1", CubeListBuilder.create().texOffs(22, 12).addBox(-3.0F, -0.5F, -2.5F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.02F, -4.5F, 0.0F, 0.0524F, 0.0349F));

		PartDefinition shell_top_front_layer1_r1 = body_front.addOrReplaceChild("shell_top_front_layer1_r1", CubeListBuilder.create().texOffs(0, 12).addBox(-4.0F, -0.5F, -3.5F, 8.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.01F, -4.5F, 0.0349F, -0.0524F, 0.0F));

		PartDefinition shell_top_front_base_r1 = body_front.addOrReplaceChild("shell_top_front_base_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -2.0F, -4.5F, 12.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.5F, -4.5F, 0.0873F, 0.0F, 0.0F));

		PartDefinition body_back = body_front.addOrReplaceChild("body_back", CubeListBuilder.create().texOffs(0, 58).addBox(-6.99F, -3.49F, -1.0F, 13.98F, 4.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(0, 76).addBox(-6.0F, -7.5F, 12.5F, 12.0F, 5.6F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 4.0F));

		PartDefinition shell_lip_back_r_r1 = body_back.addOrReplaceChild("shell_lip_back_r_r1", CubeListBuilder.create().texOffs(38, 5).mirror().addBox(-1.0F, -1.0F, -4.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-10.0F, -2.0F, 7.5F, 0.0F, -0.1745F, 0.6109F));

		PartDefinition shell_lip_back_l_r1 = body_back.addOrReplaceChild("shell_lip_back_l_r1", CubeListBuilder.create().texOffs(38, 5).addBox(-1.0F, -1.0F, -4.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0F, -2.0F, 7.5F, 0.0F, 0.1745F, -0.6109F));

		PartDefinition shell_lip_back_r1 = body_back.addOrReplaceChild("shell_lip_back_r1", CubeListBuilder.create().texOffs(38, 0).addBox(-7.0F, -1.0F, -1.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 13.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition shell_back_r_layer2_r1 = body_back.addOrReplaceChild("shell_back_r_layer2_r1", CubeListBuilder.create().texOffs(10, 52).mirror().addBox(-1.0F, -2.0F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.5F, -4.0F, 12.5F, -0.1745F, 0.0873F, 0.4363F));

		PartDefinition shell_back_r_layer1_r1 = body_back.addOrReplaceChild("shell_back_r_layer1_r1", CubeListBuilder.create().texOffs(0, 52).mirror().addBox(-1.5F, -2.0F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.5F, -4.0F, 12.0F, -0.2618F, 0.1745F, 0.2618F));

		PartDefinition shell_back_r_base_r1 = body_back.addOrReplaceChild("shell_back_r_base_r1", CubeListBuilder.create().texOffs(0, 44).mirror().addBox(-2.0F, -3.0F, -1.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.5F, -4.0F, 12.0F, -0.3491F, 0.2618F, 0.3491F));

		PartDefinition shell_back_l_layer2_r1 = body_back.addOrReplaceChild("shell_back_l_layer2_r1", CubeListBuilder.create().texOffs(10, 52).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, -4.0F, 12.5F, -0.1745F, -0.0873F, -0.4363F));

		PartDefinition shell_back_l_layer1_r1 = body_back.addOrReplaceChild("shell_back_l_layer1_r1", CubeListBuilder.create().texOffs(0, 52).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, -4.0F, 12.0F, -0.2618F, -0.1745F, -0.2618F));

		PartDefinition shell_back_l_base_r1 = body_back.addOrReplaceChild("shell_back_l_base_r1", CubeListBuilder.create().texOffs(0, 44).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, -4.0F, 12.0F, -0.3491F, -0.2618F, -0.3491F));

		PartDefinition shell_back_center_layer2_r1 = body_back.addOrReplaceChild("shell_back_center_layer2_r1", CubeListBuilder.create().texOffs(10, 52).addBox(-3.0F, -2.0F, -0.5F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.5F, 13.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition shell_back_center_layer1_r1 = body_back.addOrReplaceChild("shell_back_center_layer1_r1", CubeListBuilder.create().texOffs(0, 52).addBox(-4.0F, -3.0F, -0.5F, 8.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.5F, 12.5F, -0.3491F, 0.0F, 0.0F));

		PartDefinition shell_back_center_base_r1 = body_back.addOrReplaceChild("shell_back_center_base_r1", CubeListBuilder.create().texOffs(0, 44).addBox(-6.0F, -4.0F, -1.0F, 12.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.5F, 12.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition shell_side_back_r_layer2_r1 = body_back.addOrReplaceChild("shell_side_back_r_layer2_r1", CubeListBuilder.create().texOffs(17, 35).mirror().addBox(-1.0F, -2.0F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.5F, -5.5F, 7.5F, -0.0349F, -0.0873F, 0.4887F));

		PartDefinition shell_side_back_r_layer1_r1 = body_back.addOrReplaceChild("shell_side_back_r_layer1_r1", CubeListBuilder.create().texOffs(0, 35).mirror().addBox(-1.5F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-8.5F, -5.5F, 7.5F, 0.0349F, -0.1396F, 0.3491F));

		PartDefinition shell_side_back_r_base_r1 = body_back.addOrReplaceChild("shell_side_back_r_base_r1", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(-2.0F, -3.0F, -4.5F, 4.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.5F, -5.5F, 7.5F, 0.0F, -0.1745F, 0.4363F));

		PartDefinition shell_side_back_l_layer2_r1 = body_back.addOrReplaceChild("shell_side_back_l_layer2_r1", CubeListBuilder.create().texOffs(17, 35).addBox(-1.0F, -2.0F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5F, -5.5F, 7.5F, -0.0349F, 0.0873F, -0.4887F));

		PartDefinition shell_side_back_l_layer1_r1 = body_back.addOrReplaceChild("shell_side_back_l_layer1_r1", CubeListBuilder.create().texOffs(0, 35).addBox(-1.5F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5F, -5.5F, 7.5F, 0.0349F, 0.1396F, -0.3491F));

		PartDefinition shell_side_back_l_base_r1 = body_back.addOrReplaceChild("shell_side_back_l_base_r1", CubeListBuilder.create().texOffs(0, 20).addBox(-2.0F, -3.0F, -4.5F, 4.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, -5.5F, 7.5F, 0.0F, 0.1745F, -0.4363F));

		PartDefinition shell_top_back_layer2_r1 = body_back.addOrReplaceChild("shell_top_back_layer2_r1", CubeListBuilder.create().texOffs(22, 12).addBox(-3.0F, -0.5F, -2.5F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.02F, 7.5F, 0.0F, -0.0524F, -0.0349F));

		PartDefinition shell_top_back_layer1_r1 = body_back.addOrReplaceChild("shell_top_back_layer1_r1", CubeListBuilder.create().texOffs(0, 12).addBox(-4.0F, -0.5F, -3.5F, 8.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.01F, 7.5F, -0.0349F, 0.0524F, 0.0F));

		PartDefinition shell_top_back_base_r1 = body_back.addOrReplaceChild("shell_top_back_base_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -2.0F, -4.5F, 12.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.5F, 7.5F, -0.0873F, 0.0F, 0.0F));

		PartDefinition tail = body_back.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(80, 20).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 12.0F));

		PartDefinition tail_scale_r1 = tail.addOrReplaceChild("tail_scale_r1", CubeListBuilder.create().texOffs(94, 20).addBox(-1.0F, -0.5F, -2.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 1.5F, -0.2618F, 0.0F, 0.0F));

		PartDefinition shoulder_back_left = body_back.addOrReplaceChild("shoulder_back_left", CubeListBuilder.create().texOffs(0, 108).addBox(-3.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5F, -3.43F, 8.0F, 0.0F, -0.5585F, 0.4363F));

		PartDefinition leg_back_left = shoulder_back_left.addOrReplaceChild("leg_back_left", CubeListBuilder.create().texOffs(0, 114).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition shin_back_left = leg_back_left.addOrReplaceChild("shin_back_left", CubeListBuilder.create().texOffs(12, 114).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 1.0F, 0.0F, 0.0F, 0.0F, -0.6109F));

		PartDefinition back_left_leg_bark_r1 = shin_back_left.addOrReplaceChild("back_left_leg_bark_r1", CubeListBuilder.create().texOffs(0, 119).addBox(-0.7F, -1.0F, -1.6F, 2.0F, 2.0F, 3.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0873F, 0.0F));

		PartDefinition ankle_back_left = shin_back_left.addOrReplaceChild("ankle_back_left", CubeListBuilder.create().texOffs(10, 119).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition back_left_foot_plate_2_r1 = ankle_back_left.addOrReplaceChild("back_left_foot_plate_2_r1", CubeListBuilder.create().texOffs(12, 125).addBox(-1.0F, -0.5F, -0.9F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.8397F, 1.3F, -0.3118F, 0.7524F, 0.2793F));

		PartDefinition back_left_foot_plate_1_r1 = ankle_back_left.addOrReplaceChild("back_left_foot_plate_1_r1", CubeListBuilder.create().texOffs(2, 125).addBox(0.5F, -0.5F, -0.9F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.1603F, -1.3F, -0.2331F, 0.2469F, 0.4381F));

		PartDefinition shoulder_back_right = body_back.addOrReplaceChild("shoulder_back_right", CubeListBuilder.create().texOffs(0, 108).mirror().addBox(-1.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-6.5F, -3.43F, 8.0F, 0.0F, 0.5585F, -0.4363F));

		PartDefinition leg_back_right = shoulder_back_right.addOrReplaceChild("leg_back_right", CubeListBuilder.create().texOffs(0, 114).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.5F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.5F, 0.5F, 0.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition shin_back_right = leg_back_right.addOrReplaceChild("shin_back_right", CubeListBuilder.create().texOffs(12, 114).mirror().addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.6109F));

		PartDefinition back_right_leg_bark_r1 = shin_back_right.addOrReplaceChild("back_right_leg_bark_r1", CubeListBuilder.create().texOffs(0, 119).mirror().addBox(-1.3F, -1.0F, -1.6F, 2.0F, 2.0F, 3.2F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, -0.0873F, 0.0F));

		PartDefinition ankle_back_right = shin_back_right.addOrReplaceChild("ankle_back_right", CubeListBuilder.create().texOffs(10, 119).mirror().addBox(-1.0F, 0.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition back_right_foot_plate_2_r1 = ankle_back_right.addOrReplaceChild("back_right_foot_plate_2_r1", CubeListBuilder.create().texOffs(12, 125).mirror().addBox(-1.0F, -0.5F, -0.9F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.8397F, 1.3F, -0.3118F, -0.7524F, -0.2793F));

		PartDefinition back_right_foot_plate_1_r1 = ankle_back_right.addOrReplaceChild("back_right_foot_plate_1_r1", CubeListBuilder.create().texOffs(2, 125).mirror().addBox(0.5F, -0.5F, -0.9F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.1603F, -1.3F, -0.2331F, -0.2469F, -0.4381F));

		PartDefinition flora_back = body_back.addOrReplaceChild("flora_back", CubeListBuilder.create(), PartPose.offset(0.0F, -9.5F, 7.0F));

		PartDefinition flora_young = flora_back.addOrReplaceChild("flora_young", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition sprout_2_r1 = flora_young.addOrReplaceChild("sprout_2_r1", CubeListBuilder.create().texOffs(66, 48).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition sprout_1_r1 = flora_young.addOrReplaceChild("sprout_1_r1", CubeListBuilder.create().texOffs(58, 48).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition flora_medium = flora_back.addOrReplaceChild("flora_medium", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition fern_2_r1 = flora_medium.addOrReplaceChild("fern_2_r1", CubeListBuilder.create().texOffs(98, 48).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, 2.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition fern_1_r1 = flora_medium.addOrReplaceChild("fern_1_r1", CubeListBuilder.create().texOffs(90, 48).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, 2.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition flower_l2_r1 = flora_medium.addOrReplaceChild("flower_l2_r1", CubeListBuilder.create().texOffs(82, 48).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, -2.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition flower_l1_r1 = flora_medium.addOrReplaceChild("flower_l1_r1", CubeListBuilder.create().texOffs(74, 48).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, -2.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition flora_old = flora_back.addOrReplaceChild("flora_old", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bush_2_r1 = flora_old.addOrReplaceChild("bush_2_r1", CubeListBuilder.create().texOffs(98, 56).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 4.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition bush_1_r1 = flora_old.addOrReplaceChild("bush_1_r1", CubeListBuilder.create().texOffs(90, 56).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 4.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition shroom_2_r1 = flora_old.addOrReplaceChild("shroom_2_r1", CubeListBuilder.create().texOffs(82, 56).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 0.0F, 4.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition shroom_1_r1 = flora_old.addOrReplaceChild("shroom_1_r1", CubeListBuilder.create().texOffs(74, 56).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 0.0F, 4.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition tall_flower_2_r1 = flora_old.addOrReplaceChild("tall_flower_2_r1", CubeListBuilder.create().texOffs(66, 56).addBox(-2.0F, -16.0F, 0.0F, 4.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, -1.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition tall_flower_1_r1 = flora_old.addOrReplaceChild("tall_flower_1_r1", CubeListBuilder.create().texOffs(58, 56).addBox(-2.0F, -16.0F, 0.0F, 4.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, -1.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition neck = body_front.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(58, 0).addBox(-3.5F, -2.5F, -4.0F, 7.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(58, 8).addBox(-4.0F, -3.01F, -5.0F, 8.0F, 4.02F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, -7.0F));

		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(58, 16).addBox(-3.0F, -2.5F, -6.0F, 6.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(80, 0).addBox(-2.5F, -1.5F, -8.0F, 5.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, -3.0F));

		PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(80, 5).addBox(-2.5F, 0.0F, -5.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.5F, -2.0F));

		PartDefinition brow_l = head.addOrReplaceChild("brow_l", CubeListBuilder.create().texOffs(80, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -2.5F, -4.5F, 0.0F, 0.0F, -0.1745F));

		PartDefinition brow_r = head.addOrReplaceChild("brow_r", CubeListBuilder.create().texOffs(80, 16).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.5F, -2.5F, -4.5F, 0.0F, 0.0F, 0.1745F));

		PartDefinition sensory_leaf_l = head.addOrReplaceChild("sensory_leaf_l", CubeListBuilder.create().texOffs(94, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -1.5F, -5.0F, -0.2618F, 0.0F, 0.1745F));

		PartDefinition leaf_l_mid = sensory_leaf_l.addOrReplaceChild("leaf_l_mid", CubeListBuilder.create().texOffs(94, 5).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -2.0F, 0.0F, -0.1745F, 0.0F, 0.0873F));

		PartDefinition leaf_l_tip = leaf_l_mid.addOrReplaceChild("leaf_l_tip", CubeListBuilder.create().texOffs(94, 11).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -2.0F, 0.0F, -0.1745F, 0.0F, 0.0873F));

		PartDefinition sensory_leaf_r = head.addOrReplaceChild("sensory_leaf_r", CubeListBuilder.create().texOffs(94, 25).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, -1.5F, -5.0F, -0.2618F, 0.0F, -0.1745F));

		PartDefinition leaf_r_mid = sensory_leaf_r.addOrReplaceChild("leaf_r_mid", CubeListBuilder.create().texOffs(94, 30).mirror().addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.5F, -2.0F, 0.0F, -0.1745F, 0.0F, -0.0873F));

		PartDefinition leaf_r_tip = leaf_r_mid.addOrReplaceChild("leaf_r_tip", CubeListBuilder.create().texOffs(94, 36).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.5F, -2.0F, 0.0F, -0.1745F, 0.0F, -0.0873F));

		PartDefinition shoulder_front_left = body_front.addOrReplaceChild("shoulder_front_left", CubeListBuilder.create().texOffs(0, 108).addBox(-3.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5F, -3.43F, -4.0F, 0.0F, 0.5585F, 0.4363F));

		PartDefinition leg_front_left = shoulder_front_left.addOrReplaceChild("leg_front_left", CubeListBuilder.create().texOffs(0, 114).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition shin_front_left = leg_front_left.addOrReplaceChild("shin_front_left", CubeListBuilder.create().texOffs(12, 114).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 1.0F, 0.0F, 0.0F, 0.0F, -0.6109F));

		PartDefinition front_left_leg_bark_r1 = shin_front_left.addOrReplaceChild("front_left_leg_bark_r1", CubeListBuilder.create().texOffs(0, 119).addBox(-0.7F, -1.0F, -1.6F, 2.0F, 2.0F, 3.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0873F, 0.0F));

		PartDefinition ankle_front_left = shin_front_left.addOrReplaceChild("ankle_front_left", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition front_left_foot_plate_2_r1 = ankle_front_left.addOrReplaceChild("front_left_foot_plate_2_r1", CubeListBuilder.create().texOffs(12, 125).addBox(-1.0F, -0.5F, -0.9F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.1603F, 1.3F, 0.2331F, -0.2469F, 0.4381F));

		PartDefinition front_left_foot_plate_1_r1 = ankle_front_left.addOrReplaceChild("front_left_foot_plate_1_r1", CubeListBuilder.create().texOffs(2, 125).addBox(0.5F, -0.5F, -0.9F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.8397F, -1.3F, 0.3118F, -0.7524F, 0.2793F));

		PartDefinition front_left_ankle_cube_r1 = ankle_front_left.addOrReplaceChild("front_left_ankle_cube_r1", CubeListBuilder.create().texOffs(10, 119).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

		PartDefinition shoulder_front_right = body_front.addOrReplaceChild("shoulder_front_right", CubeListBuilder.create().texOffs(0, 108).mirror().addBox(-1.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-6.5F, -3.43F, -4.0F, 0.0F, -0.5585F, -0.4363F));

		PartDefinition leg_front_right = shoulder_front_right.addOrReplaceChild("leg_front_right", CubeListBuilder.create().texOffs(0, 114).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.5F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.5F, 0.5F, 0.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition shin_front_right = leg_front_right.addOrReplaceChild("shin_front_right", CubeListBuilder.create().texOffs(12, 114).mirror().addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.6109F));

		PartDefinition front_right_leg_bark_r1 = shin_front_right.addOrReplaceChild("front_right_leg_bark_r1", CubeListBuilder.create().texOffs(0, 119).mirror().addBox(-1.3F, -1.0F, -1.6F, 2.0F, 2.0F, 3.2F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, -0.0873F, 0.0F));

		PartDefinition ankle_front_right = shin_front_right.addOrReplaceChild("ankle_front_right", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition front_right_foot_plate_2_r1 = ankle_front_right.addOrReplaceChild("front_right_foot_plate_2_r1", CubeListBuilder.create().texOffs(12, 125).mirror().addBox(-1.0F, -0.5F, -0.9F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.1603F, 1.3F, 0.2331F, 0.2469F, -0.4381F));

		PartDefinition front_right_foot_plate_1_r1 = ankle_front_right.addOrReplaceChild("front_right_foot_plate_1_r1", CubeListBuilder.create().texOffs(2, 125).mirror().addBox(0.5F, -0.5F, -0.9F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.8397F, -1.3F, 0.3118F, 0.7524F, -0.2793F));

		PartDefinition front_right_ankle_cube_r1 = ankle_front_right.addOrReplaceChild("front_right_ankle_cube_r1", CubeListBuilder.create().texOffs(10, 119).mirror().addBox(-1.0F, 0.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

		PartDefinition flora_front = body_front.addOrReplaceChild("flora_front", CubeListBuilder.create(), PartPose.offset(0.0F, -9.5F, -4.0F));

		PartDefinition sprout_front_2_r1 = flora_front.addOrReplaceChild("sprout_front_2_r1", CubeListBuilder.create().texOffs(66, 40).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition sprout_front_1_r1 = flora_front.addOrReplaceChild("sprout_front_1_r1", CubeListBuilder.create().texOffs(58, 40).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition flora_mid = body_front.addOrReplaceChild("flora_mid", CubeListBuilder.create(), PartPose.offset(0.0F, -10.5F, 4.0F));

		PartDefinition shroom_mid_2_r1 = flora_mid.addOrReplaceChild("shroom_mid_2_r1", CubeListBuilder.create().texOffs(98, 40).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, -2.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition shroom_mid_1_r1 = flora_mid.addOrReplaceChild("shroom_mid_1_r1", CubeListBuilder.create().texOffs(90, 40).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, -2.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition fern_mid_2_r1 = flora_mid.addOrReplaceChild("fern_mid_2_r1", CubeListBuilder.create().texOffs(82, 40).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition fern_mid_1_r1 = flora_mid.addOrReplaceChild("fern_mid_1_r1", CubeListBuilder.create().texOffs(74, 40).addBox(-2.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(BloomCrawlerRenderState state) {
		super.setupAnim(state);
		
		// Dynamic look rotations
		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

		// Shell age flora visibility
		this.flora_young.visible = (state.shellAge == 0);
		this.flora_medium.visible = (state.shellAge == 1);
		this.flora_old.visible = (state.shellAge == 2);

		// Apply keyframe animations
		this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
		this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
		this.runAnimation.apply(state.runAnimationState, state.ageInTicks);
		this.grazeAnimation.apply(state.grazeAnimationState, state.ageInTicks);
	}
}
