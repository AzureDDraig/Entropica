package ddraig.net.entropica.client.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class EmberwingModel extends EntityModel<EntityRenderState> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("modid", "emberwing"), "main");
	private final ModelPart torso;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart beak_upper;
	private final ModelPart beak_lower;
	private final ModelPart wing_left_shoulder;
	private final ModelPart wing_left_elbow;
	private final ModelPart wing_left_wrist;
	private final ModelPart wing_right_shoulder;
	private final ModelPart wing_right_elbow;
	private final ModelPart wing_right_wrist;
	private final ModelPart abdomen;
	private final ModelPart leg_left_thigh;
	private final ModelPart leg_left_calf;
	private final ModelPart foot_left;
	private final ModelPart leg_right_thigh;
	private final ModelPart leg_right_calf;
	private final ModelPart foot_right;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart tail_fin;

	public EmberwingModel(ModelPart root) {
		super(root);
		this.torso = root.getChild("torso");
		this.neck = this.torso.getChild("neck");
		this.head = this.neck.getChild("head");
		this.beak_upper = this.head.getChild("beak_upper");
		this.beak_lower = this.head.getChild("beak_lower");
		this.wing_left_shoulder = this.torso.getChild("wing_left_shoulder");
		this.wing_left_elbow = this.wing_left_shoulder.getChild("wing_left_elbow");
		this.wing_left_wrist = this.wing_left_elbow.getChild("wing_left_wrist");
		this.wing_right_shoulder = this.torso.getChild("wing_right_shoulder");
		this.wing_right_elbow = this.wing_right_shoulder.getChild("wing_right_elbow");
		this.wing_right_wrist = this.wing_right_elbow.getChild("wing_right_wrist");
		this.abdomen = this.torso.getChild("abdomen");
		this.leg_left_thigh = this.abdomen.getChild("leg_left_thigh");
		this.leg_left_calf = this.leg_left_thigh.getChild("leg_left_calf");
		this.foot_left = this.leg_left_calf.getChild("foot_left");
		this.leg_right_thigh = this.abdomen.getChild("leg_right_thigh");
		this.leg_right_calf = this.leg_right_thigh.getChild("leg_right_calf");
		this.foot_right = this.leg_right_calf.getChild("foot_right");
		this.tail1 = this.abdomen.getChild("tail1");
		this.tail2 = this.tail1.getChild("tail2");
		this.tail3 = this.tail2.getChild("tail3");
		this.tail_fin = this.tail3.getChild("tail_fin");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition torso = partdefinition.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.5F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.5F, -2.0F));

		PartDefinition neck = torso.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 24).addBox(-1.5F, -1.5F, -8.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -2.5F, 0.0873F, 0.0F, 0.0F));

		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, -2.0F, -5.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -7.0F, -0.0873F, 0.0F, 0.0F));

		PartDefinition beak_upper = head.addOrReplaceChild("beak_upper", CubeListBuilder.create().texOffs(0, 68).addBox(-1.0F, -1.0F, -8.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition beak_lower = head.addOrReplaceChild("beak_lower", CubeListBuilder.create().texOffs(20, 68).addBox(-1.0F, 0.0F, -8.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition wing_left_shoulder = torso.addOrReplaceChild("wing_left_shoulder", CubeListBuilder.create().texOffs(0, 82).addBox(0.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(40, 52).addBox(0.0F, -0.1F, 0.0F, 4.0F, 0.1F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.1745F));

		PartDefinition wing_left_elbow = wing_left_shoulder.addOrReplaceChild("wing_left_elbow", CubeListBuilder.create().texOffs(24, 82).addBox(0.0F, -0.75F, -0.75F, 5.0F, 1.5F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(40, 52).addBox(-1.0F, -0.1F, 0.0F, 6.0F, 0.1F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition wing_left_wrist = wing_left_elbow.addOrReplaceChild("wing_left_wrist", CubeListBuilder.create().texOffs(52, 82).addBox(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(76, 82).addBox(2.0F, -0.5F, -3.5F, 2.0F, 1.5F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(40, 52).addBox(-2.0F, -0.1F, 0.0F, 6.0F, 0.1F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition wing_right_shoulder = torso.addOrReplaceChild("wing_right_shoulder", CubeListBuilder.create().texOffs(0, 92).addBox(-4.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(40, 52).addBox(-4.0F, -0.1F, 0.0F, 4.0F, 0.1F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.5F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition wing_right_elbow = wing_right_shoulder.addOrReplaceChild("wing_right_elbow", CubeListBuilder.create().texOffs(24, 92).addBox(-5.0F, -0.75F, -0.75F, 5.0F, 1.5F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(40, 52).addBox(-5.0F, -0.1F, 0.0F, 6.0F, 0.1F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition wing_right_wrist = wing_right_elbow.addOrReplaceChild("wing_right_wrist", CubeListBuilder.create().texOffs(52, 92).addBox(-4.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(76, 92).addBox(-4.0F, -0.5F, -3.5F, 2.0F, 1.5F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(40, 52).addBox(-4.0F, -0.1F, 0.0F, 6.0F, 0.1F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition abdomen = torso.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(48, 0).addBox(-2.5F, -2.0F, 0.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 3.0F));

		PartDefinition leg_left_thigh = abdomen.addOrReplaceChild("leg_left_thigh", CubeListBuilder.create().texOffs(0, 102).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 1.5F, 3.0F));

		PartDefinition leg_left_calf = leg_left_thigh.addOrReplaceChild("leg_left_calf", CubeListBuilder.create().texOffs(32, 102).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition foot_left = leg_left_calf.addOrReplaceChild("foot_left", CubeListBuilder.create().texOffs(0, 114).addBox(-1.0F, 0.0F, -4.0F, 2.0F, 1.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.5F, 0.0F));

		PartDefinition leg_right_thigh = abdomen.addOrReplaceChild("leg_right_thigh", CubeListBuilder.create().texOffs(16, 102).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 1.5F, 3.0F));

		PartDefinition leg_right_calf = leg_right_thigh.addOrReplaceChild("leg_right_calf", CubeListBuilder.create().texOffs(48, 102).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition foot_right = leg_right_calf.addOrReplaceChild("foot_right", CubeListBuilder.create().texOffs(24, 114).addBox(-1.0F, 0.0F, -4.0F, 2.0F, 1.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.5F, 0.0F));

		PartDefinition tail1 = abdomen.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(48, 24).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 6.0F, 0.0873F, 0.0F, 0.0F));

		PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(72, 24).addBox(-0.75F, -0.75F, 0.0F, 1.5F, 1.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0873F, 0.0F, 0.0F));

		PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(96, 24).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0873F, 0.0F, 0.0F));

		PartDefinition tail_fin = tail3.addOrReplaceChild("tail_fin", CubeListBuilder.create().texOffs(48, 40).addBox(-3.0F, -0.5F, 0.0F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 4.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(EntityRenderState state) {

	}

	
}