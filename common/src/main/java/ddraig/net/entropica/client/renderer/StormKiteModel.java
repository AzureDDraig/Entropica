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


public class StormKiteModel extends EntityModel<EntityRenderState> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("modid", "storm_kite"), "main");
	private final ModelPart body_1;
	private final ModelPart body_2;
	private final ModelPart body_3;
	private final ModelPart body_4;
	private final ModelPart body_5;
	private final ModelPart body_6;
	private final ModelPart tail_1;
	private final ModelPart tail_2;
	private final ModelPart tail_3;
	private final ModelPart tail_tuft;
	private final ModelPart wing_left_1;
	private final ModelPart wing_left_2;
	private final ModelPart wing_left_3;
	private final ModelPart wing_left_4;
	private final ModelPart wing_left_5;
	private final ModelPart wing_left_6;
	private final ModelPart wing_right_1;
	private final ModelPart wing_right_2;
	private final ModelPart wing_right_3;
	private final ModelPart wing_right_4;
	private final ModelPart wing_right_5;
	private final ModelPart wing_right_6;
	private final ModelPart cephalic_left_1;
	private final ModelPart cephalic_left_2;
	private final ModelPart cephalic_left_3;
	private final ModelPart cephalic_right_1;
	private final ModelPart cephalic_right_2;
	private final ModelPart cephalic_right_3;
	private final ModelPart hitbox;

	public StormKiteModel(ModelPart root) {
		super(root);
		this.body_1 = root.getChild("body_1");
		this.body_2 = this.body_1.getChild("body_2");
		this.body_3 = this.body_2.getChild("body_3");
		this.body_4 = this.body_3.getChild("body_4");
		this.body_5 = this.body_4.getChild("body_5");
		this.body_6 = this.body_5.getChild("body_6");
		this.tail_1 = this.body_6.getChild("tail_1");
		this.tail_2 = this.tail_1.getChild("tail_2");
		this.tail_3 = this.tail_2.getChild("tail_3");
		this.tail_tuft = this.tail_3.getChild("tail_tuft");
		this.wing_left_1 = this.body_3.getChild("wing_left_1");
		this.wing_left_2 = this.wing_left_1.getChild("wing_left_2");
		this.wing_left_3 = this.wing_left_2.getChild("wing_left_3");
		this.wing_left_4 = this.wing_left_3.getChild("wing_left_4");
		this.wing_left_5 = this.wing_left_4.getChild("wing_left_5");
		this.wing_left_6 = this.wing_left_5.getChild("wing_left_6");
		this.wing_right_1 = this.body_3.getChild("wing_right_1");
		this.wing_right_2 = this.wing_right_1.getChild("wing_right_2");
		this.wing_right_3 = this.wing_right_2.getChild("wing_right_3");
		this.wing_right_4 = this.wing_right_3.getChild("wing_right_4");
		this.wing_right_5 = this.wing_right_4.getChild("wing_right_5");
		this.wing_right_6 = this.wing_right_5.getChild("wing_right_6");
		this.cephalic_left_1 = this.body_1.getChild("cephalic_left_1");
		this.cephalic_left_2 = this.cephalic_left_1.getChild("cephalic_left_2");
		this.cephalic_left_3 = this.cephalic_left_2.getChild("cephalic_left_3");
		this.cephalic_right_1 = this.body_1.getChild("cephalic_right_1");
		this.cephalic_right_2 = this.cephalic_right_1.getChild("cephalic_right_2");
		this.cephalic_right_3 = this.cephalic_right_2.getChild("cephalic_right_3");
		this.hitbox = root.getChild("hitbox");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body_1 = partdefinition.addOrReplaceChild("body_1", CubeListBuilder.create().texOffs(86, 43).addBox(-5.0F, -2.0F, -8.0F, 10.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, -16.0F));

		PartDefinition body_2 = body_1.addOrReplaceChild("body_2", CubeListBuilder.create().texOffs(86, 55).addBox(-6.0F, -2.5F, -8.0F, 12.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));

		PartDefinition body_3 = body_2.addOrReplaceChild("body_3", CubeListBuilder.create().texOffs(86, 68).addBox(-6.5F, -3.0F, -8.0F, 13.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));

		PartDefinition body_4 = body_3.addOrReplaceChild("body_4", CubeListBuilder.create().texOffs(86, 82).addBox(-6.0F, -2.5F, -8.0F, 12.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));

		PartDefinition body_5 = body_4.addOrReplaceChild("body_5", CubeListBuilder.create().texOffs(86, 95).addBox(-5.0F, -2.0F, -8.0F, 10.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));

		PartDefinition body_6 = body_5.addOrReplaceChild("body_6", CubeListBuilder.create().texOffs(86, 107).addBox(-3.5F, -1.5F, -8.0F, 7.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));

		PartDefinition tail_1 = body_6.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(96, 0).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tail_2 = tail_1.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(96, 14).addBox(-1.0F, -0.8F, 0.0F, 2.0F, 1.6F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 12.0F));

		PartDefinition tail_3 = tail_2.addOrReplaceChild("tail_3", CubeListBuilder.create().texOffs(96, 28).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 12.0F));

		PartDefinition tail_tuft = tail_3.addOrReplaceChild("tail_tuft", CubeListBuilder.create().texOffs(52, 108).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 12.0F));

		PartDefinition wing_left_1 = body_3.addOrReplaceChild("wing_left_1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.5F, -20.0F, 8.0F, 3.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 0.0F, 0.0F));

		PartDefinition wing_left_2 = wing_left_1.addOrReplaceChild("wing_left_2", CubeListBuilder.create().texOffs(0, 43).addBox(0.0F, -1.5F, -16.0F, 10.0F, 3.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, 2.0F));

		PartDefinition wing_left_3 = wing_left_2.addOrReplaceChild("wing_left_3", CubeListBuilder.create().texOffs(0, 78).addBox(0.0F, -1.0F, -12.0F, 10.0F, 2.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, 0.0F, 2.0F));

		PartDefinition wing_left_4 = wing_left_3.addOrReplaceChild("wing_left_4", CubeListBuilder.create().texOffs(0, 104).addBox(0.0F, -1.0F, -8.0F, 10.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, 0.0F, 2.0F));

		PartDefinition wing_left_5 = wing_left_4.addOrReplaceChild("wing_left_5", CubeListBuilder.create().texOffs(86, 118).addBox(0.0F, -0.5F, -4.5F, 10.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, 0.0F, 1.5F));

		PartDefinition wing_left_6 = wing_left_5.addOrReplaceChild("wing_left_6", CubeListBuilder.create().texOffs(52, 104).addBox(0.0F, -0.5F, -1.5F, 12.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, 0.0F, 2.0F));

		PartDefinition wing_right_1 = body_3.addOrReplaceChild("wing_right_1", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -1.5F, -20.0F, 8.0F, 3.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 0.0F, 0.0F));

		PartDefinition wing_right_2 = wing_right_1.addOrReplaceChild("wing_right_2", CubeListBuilder.create().texOffs(0, 43).addBox(-10.0F, -1.5F, -16.0F, 10.0F, 3.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 0.0F, 2.0F));

		PartDefinition wing_right_3 = wing_right_2.addOrReplaceChild("wing_right_3", CubeListBuilder.create().texOffs(0, 78).addBox(-10.0F, -1.0F, -12.0F, 10.0F, 2.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 2.0F));

		PartDefinition wing_right_4 = wing_right_3.addOrReplaceChild("wing_right_4", CubeListBuilder.create().texOffs(0, 104).addBox(-10.0F, -1.0F, -8.0F, 10.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 2.0F));

		PartDefinition wing_right_5 = wing_right_4.addOrReplaceChild("wing_right_5", CubeListBuilder.create().texOffs(86, 118).addBox(-10.0F, -0.5F, -4.5F, 10.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 1.5F));

		PartDefinition wing_right_6 = wing_right_5.addOrReplaceChild("wing_right_6", CubeListBuilder.create().texOffs(52, 104).addBox(-12.0F, -0.5F, -1.5F, 12.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 2.0F));

		PartDefinition cephalic_left_1 = body_1.addOrReplaceChild("cephalic_left_1", CubeListBuilder.create().texOffs(68, 78).addBox(-0.5F, -0.5F, -8.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 0.0F, -4.0F));

		PartDefinition cephalic_left_2 = cephalic_left_1.addOrReplaceChild("cephalic_left_2", CubeListBuilder.create().texOffs(68, 89).addBox(-0.8F, 0.0F, -6.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -8.0F));

		PartDefinition cephalic_left_3 = cephalic_left_2.addOrReplaceChild("cephalic_left_3", CubeListBuilder.create().texOffs(68, 98).addBox(-1.2F, 0.5F, -4.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -6.0F));

		PartDefinition cephalic_right_1 = body_1.addOrReplaceChild("cephalic_right_1", CubeListBuilder.create().texOffs(68, 78).addBox(-1.5F, -0.5F, -8.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0F, -4.0F));

		PartDefinition cephalic_right_2 = cephalic_right_1.addOrReplaceChild("cephalic_right_2", CubeListBuilder.create().texOffs(68, 89).addBox(-1.2F, 0.0F, -6.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -8.0F));

		PartDefinition cephalic_right_3 = cephalic_right_2.addOrReplaceChild("cephalic_right_3", CubeListBuilder.create().texOffs(68, 98).addBox(-0.8F, 0.5F, -4.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -6.0F));

		PartDefinition hitbox = partdefinition.addOrReplaceChild("hitbox", CubeListBuilder.create().texOffs(0, 0).addBox(-64.0F, -3.0F, -8.0F, 128.0F, 6.0F, 48.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-6.0F, -2.0F, 40.0F, 12.0F, 4.0F, 46.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, -16.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(EntityRenderState state) {

	}

	
}