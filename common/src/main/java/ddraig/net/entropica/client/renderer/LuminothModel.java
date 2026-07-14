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


public class LuminothModel extends EntityModel<EntityRenderState> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("modid", "luminoth"), "main");
	private final ModelPart body;
	private final ModelPart abdomen;
	private final ModelPart head;
	private final ModelPart wing_left;
	private final ModelPart wing_right;

	public LuminothModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.abdomen = this.body.getChild("abdomen");
		this.head = this.body.getChild("head");
		this.wing_left = this.body.getChild("wing_left");
		this.wing_right = this.body.getChild("wing_right");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));

		PartDefinition abdomen = body.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 3.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 32).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 40).addBox(1.5F, -1.0F, -2.5F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(12, 40).addBox(-2.5F, -1.0F, -2.5F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, -3.0F));

		PartDefinition antenna_right_r1 = head.addOrReplaceChild("antenna_right_r1", CubeListBuilder.create().texOffs(8, 48).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.5F, -3.0F, 0.5236F, -0.3491F, -0.2618F));

		PartDefinition antenna_left_r1 = head.addOrReplaceChild("antenna_left_r1", CubeListBuilder.create().texOffs(0, 48).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -1.5F, -3.0F, 0.5236F, 0.3491F, 0.2618F));

		PartDefinition wing_left = body.addOrReplaceChild("wing_left", CubeListBuilder.create(), PartPose.offset(1.5F, -2.0F, 0.0F));

		PartDefinition wing_left_cube_r1 = wing_left.addOrReplaceChild("wing_left_cube_r1", CubeListBuilder.create().texOffs(24, 0).addBox(0.0F, 0.0F, -4.0F, 11.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

		PartDefinition wing_right = body.addOrReplaceChild("wing_right", CubeListBuilder.create(), PartPose.offset(-1.5F, -2.0F, 0.0F));

		PartDefinition wing_right_cube_r1 = wing_right.addOrReplaceChild("wing_right_cube_r1", CubeListBuilder.create().texOffs(24, 16).addBox(-11.0F, 0.0F, -4.0F, 11.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(EntityRenderState state) {

	}

	
}