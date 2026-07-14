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


public class GlacialHydraModel extends EntityModel<EntityRenderState> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("modid", "rime_shepherd"), "main");
	private final ModelPart body;
	private final ModelPart neck;
	private final ModelPart throat_fluff;
	private final ModelPart throat_fluff_lower;
	private final ModelPart head;
	private final ModelPart antlers_young;
	private final ModelPart antlers_medium;
	private final ModelPart antlers_old;
	private final ModelPart jaw;
	private final ModelPart leg_front_left;
	private final ModelPart leg_front_left_calf;
	private final ModelPart leg_front_left_foot;
	private final ModelPart leg_front_right;
	private final ModelPart leg_front_right_calf;
	private final ModelPart leg_front_right_foot;
	private final ModelPart leg_back_left;
	private final ModelPart leg_back_left_calf;
	private final ModelPart leg_back_left_foot;
	private final ModelPart leg_back_right;
	private final ModelPart leg_back_right_calf;
	private final ModelPart leg_back_right_foot;
	private final ModelPart Hitbox;

	public GlacialHydraModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.neck = this.body.getChild("neck");
		this.throat_fluff = this.neck.getChild("throat_fluff");
		this.throat_fluff_lower = this.throat_fluff.getChild("throat_fluff_lower");
		this.head = this.neck.getChild("head");
		this.antlers_young = this.head.getChild("antlers_young");
		this.antlers_medium = this.head.getChild("antlers_medium");
		this.antlers_old = this.head.getChild("antlers_old");
		this.jaw = this.head.getChild("jaw");
		this.leg_front_left = this.body.getChild("leg_front_left");
		this.leg_front_left_calf = this.leg_front_left.getChild("leg_front_left_calf");
		this.leg_front_left_foot = this.leg_front_left_calf.getChild("leg_front_left_foot");
		this.leg_front_right = this.body.getChild("leg_front_right");
		this.leg_front_right_calf = this.leg_front_right.getChild("leg_front_right_calf");
		this.leg_front_right_foot = this.leg_front_right_calf.getChild("leg_front_right_foot");
		this.leg_back_left = this.body.getChild("leg_back_left");
		this.leg_back_left_calf = this.leg_back_left.getChild("leg_back_left_calf");
		this.leg_back_left_foot = this.leg_back_left_calf.getChild("leg_back_left_foot");
		this.leg_back_right = this.body.getChild("leg_back_right");
		this.leg_back_right_calf = this.leg_back_right.getChild("leg_back_right_calf");
		this.leg_back_right_foot = this.leg_back_right_calf.getChild("leg_back_right_foot");
		this.Hitbox = root.getChild("Hitbox");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -6.0F, 6.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(0, 19).addBox(-2.75F, -7.0F, 4.0F, 5.5F, 7.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(17, 53).addBox(-2.0F, -10.0F, -5.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.0F, 0.0F));

		PartDefinition tail_r1 = body.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(17, 47).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 11.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 47).addBox(-1.75F, -13.5F, -3.0F, 3.5F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.5F, -3.5F, 0.6109F, 0.0F, 0.0F));

		PartDefinition neck_lower_r1 = neck.addOrReplaceChild("neck_lower_r1", CubeListBuilder.create().texOffs(0, 34).addBox(-2.0F, -7.5F, -3.5F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.1745F, 0.0F, 0.0F));

		PartDefinition throat_fluff = neck.addOrReplaceChild("throat_fluff", CubeListBuilder.create().texOffs(54, 25).addBox(-1.0F, -2.6953F, 1.0634F, 2.0F, 3.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, -5.5F, -0.2618F, 0.0F, 0.0F));

		PartDefinition throat_fluff_lower = throat_fluff.addOrReplaceChild("throat_fluff_lower", CubeListBuilder.create().texOffs(54, 25).addBox(-0.5F, -3.1223F, 1.3169F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.5F, -1.0F, -0.1745F, 0.0F, 0.0F));

		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(46, 0).addBox(-2.0F, -0.5F, -5.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(46, 10).addBox(-1.5F, 0.5F, -9.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.5F, 1.0F, -0.6109F, 0.0F, 0.0F));

		PartDefinition ear_right_r1 = head.addOrReplaceChild("ear_right_r1", CubeListBuilder.create().texOffs(48, 25).addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, -1.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition ear_left_r1 = head.addOrReplaceChild("ear_left_r1", CubeListBuilder.create().texOffs(48, 25).addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, -1.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition antlers_young = head.addOrReplaceChild("antlers_young", CubeListBuilder.create(), PartPose.offset(0.0F, -0.5F, -2.0F));

		PartDefinition antler_r_young_tine_r1 = antlers_young.addOrReplaceChild("antler_r_young_tine_r1", CubeListBuilder.create().texOffs(52, 31).addBox(-1.0F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -2.5F, -0.5F, -0.1745F, -0.0873F, -0.4363F));

		PartDefinition antler_r_young_main_r1 = antlers_young.addOrReplaceChild("antler_r_young_main_r1", CubeListBuilder.create().texOffs(48, 31).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, -0.2618F, -0.1745F, -0.1745F));

		PartDefinition antler_l_young_tine_r1 = antlers_young.addOrReplaceChild("antler_l_young_tine_r1", CubeListBuilder.create().texOffs(52, 31).addBox(0.0F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -2.5F, -0.5F, -0.1745F, 0.0873F, 0.4363F));

		PartDefinition antler_l_young_main_r1 = antlers_young.addOrReplaceChild("antler_l_young_main_r1", CubeListBuilder.create().texOffs(48, 31).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, -0.2618F, 0.1745F, 0.1745F));

		PartDefinition antlers_medium = head.addOrReplaceChild("antlers_medium", CubeListBuilder.create(), PartPose.offset(0.0F, -0.5F, -2.0F));

		PartDefinition antler_r_med_tine2_r1 = antlers_medium.addOrReplaceChild("antler_r_med_tine2_r1", CubeListBuilder.create().texOffs(48, 43).addBox(-1.0F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -6.0F, 1.0F, 0.1745F, -0.2618F, -0.2618F));

		PartDefinition antler_r_med_tine1_r1 = antlers_medium.addOrReplaceChild("antler_r_med_tine1_r1", CubeListBuilder.create().texOffs(48, 38).addBox(-2.0F, -3.0F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -3.0F, -0.5F, -0.1745F, -0.0873F, -0.5236F));

		PartDefinition antler_r_med_main_r1 = antlers_medium.addOrReplaceChild("antler_r_med_main_r1", CubeListBuilder.create().texOffs(56, 31).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, -0.3491F, -0.2618F, -0.2618F));

		PartDefinition antler_l_med_tine2_r1 = antlers_medium.addOrReplaceChild("antler_l_med_tine2_r1", CubeListBuilder.create().texOffs(48, 43).addBox(-1.0F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -6.0F, 1.0F, 0.1745F, 0.2618F, 0.2618F));

		PartDefinition antler_l_med_tine1_r1 = antlers_medium.addOrReplaceChild("antler_l_med_tine1_r1", CubeListBuilder.create().texOffs(48, 38).addBox(0.0F, -3.0F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -3.0F, -0.5F, -0.1745F, 0.0873F, 0.5236F));

		PartDefinition antler_l_med_main_r1 = antlers_medium.addOrReplaceChild("antler_l_med_main_r1", CubeListBuilder.create().texOffs(56, 31).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, -0.3491F, 0.2618F, 0.2618F));

		PartDefinition antlers_old = head.addOrReplaceChild("antlers_old", CubeListBuilder.create(), PartPose.offset(0.0F, -0.5F, -2.0F));

		PartDefinition antler_r_old_xtal2_r1 = antlers_old.addOrReplaceChild("antler_r_old_xtal2_r1", CubeListBuilder.create().texOffs(18, 39).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -10.0F, 1.0F, -0.5236F, 0.5236F, -0.7854F));

		PartDefinition antler_r_old_xtal1_r1 = antlers_old.addOrReplaceChild("antler_r_old_xtal1_r1", CubeListBuilder.create().texOffs(18, 34).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -6.0F, 0.0F, -0.7854F, 0.0F, -0.7854F));

		PartDefinition antler_r_old_brow_r1 = antlers_old.addOrReplaceChild("antler_r_old_brow_r1", CubeListBuilder.create().texOffs(57, 54).addBox(-1.0F, -4.0F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -1.0F, -3.0F, -0.7854F, -0.1745F, 0.0F));

		PartDefinition antler_r_old_tine2_r1 = antlers_old.addOrReplaceChild("antler_r_old_tine2_r1", CubeListBuilder.create().texOffs(48, 54).addBox(-2.0F, -5.0F, -0.5F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -8.0F, 1.0F, 0.2618F, -0.2618F, -0.3491F));

		PartDefinition antler_r_old_tine1_r1 = antlers_old.addOrReplaceChild("antler_r_old_tine1_r1", CubeListBuilder.create().texOffs(48, 49).addBox(-3.0F, -3.0F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -4.0F, -1.0F, -0.2618F, -0.1745F, -0.6109F));

		PartDefinition antler_r_old_main_r1 = antlers_old.addOrReplaceChild("antler_r_old_main_r1", CubeListBuilder.create().texOffs(55, 41).addBox(-0.5F, -11.0F, -0.5F, 1.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, -0.4363F, -0.3491F, -0.3491F));

		PartDefinition antler_l_old_xtal2_r1 = antlers_old.addOrReplaceChild("antler_l_old_xtal2_r1", CubeListBuilder.create().texOffs(18, 39).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -10.0F, 1.0F, -0.5236F, -0.5236F, 0.7854F));

		PartDefinition antler_l_old_xtal1_r1 = antlers_old.addOrReplaceChild("antler_l_old_xtal1_r1", CubeListBuilder.create().texOffs(18, 34).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, -6.0F, 0.0F, -0.7854F, 0.0F, 0.7854F));

		PartDefinition antler_l_old_brow_r1 = antlers_old.addOrReplaceChild("antler_l_old_brow_r1", CubeListBuilder.create().texOffs(57, 54).addBox(0.0F, -4.0F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -1.0F, -3.0F, -0.7854F, 0.1745F, 0.0F));

		PartDefinition antler_l_old_tine2_r1 = antlers_old.addOrReplaceChild("antler_l_old_tine2_r1", CubeListBuilder.create().texOffs(48, 54).addBox(-1.0F, -5.0F, -0.5F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -8.0F, 1.0F, 0.2618F, 0.2618F, 0.3491F));

		PartDefinition antler_l_old_tine1_r1 = antlers_old.addOrReplaceChild("antler_l_old_tine1_r1", CubeListBuilder.create().texOffs(48, 49).addBox(0.0F, -3.0F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -4.0F, -1.0F, -0.2618F, 0.1745F, 0.6109F));

		PartDefinition antler_l_old_main_r1 = antlers_old.addOrReplaceChild("antler_l_old_main_r1", CubeListBuilder.create().texOffs(55, 41).addBox(-0.5F, -11.0F, -0.5F, 1.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, -0.4363F, 0.3491F, 0.3491F));

		PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(46, 18).addBox(-1.5F, 0.0F, -4.0F, 3.0F, 1.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.5F, -5.0F));

		PartDefinition leg_front_left = body.addOrReplaceChild("leg_front_left", CubeListBuilder.create().texOffs(32, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -1.0F, -3.0F));

		PartDefinition leg_front_left_calf = leg_front_left.addOrReplaceChild("leg_front_left_calf", CubeListBuilder.create().texOffs(32, 10).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition leg_front_left_foot = leg_front_left_calf.addOrReplaceChild("leg_front_left_foot", CubeListBuilder.create().texOffs(32, 19).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition leg_front_right = body.addOrReplaceChild("leg_front_right", CubeListBuilder.create().texOffs(32, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -1.0F, -3.0F));

		PartDefinition leg_front_right_calf = leg_front_right.addOrReplaceChild("leg_front_right_calf", CubeListBuilder.create().texOffs(32, 10).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition leg_front_right_foot = leg_front_right_calf.addOrReplaceChild("leg_front_right_foot", CubeListBuilder.create().texOffs(32, 19).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition leg_back_left = body.addOrReplaceChild("leg_back_left", CubeListBuilder.create().texOffs(32, 25).addBox(-1.75F, 0.0F, -1.75F, 3.5F, 6.0F, 3.5F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -1.0F, 8.0F));

		PartDefinition leg_back_left_calf = leg_back_left.addOrReplaceChild("leg_back_left_calf", CubeListBuilder.create().texOffs(32, 36).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition leg_back_left_foot = leg_back_left_calf.addOrReplaceChild("leg_back_left_foot", CubeListBuilder.create().texOffs(32, 46).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 3.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition leg_back_right = body.addOrReplaceChild("leg_back_right", CubeListBuilder.create().texOffs(32, 25).addBox(-1.75F, 0.0F, -1.75F, 3.5F, 6.0F, 3.5F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -1.0F, 8.0F));

		PartDefinition leg_back_right_calf = leg_back_right.addOrReplaceChild("leg_back_right_calf", CubeListBuilder.create().texOffs(32, 36).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition leg_back_right_foot = leg_back_right_calf.addOrReplaceChild("leg_back_right_foot", CubeListBuilder.create().texOffs(32, 46).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 3.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition Hitbox = partdefinition.addOrReplaceChild("Hitbox", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -16.0F, -40.0F, 32.0F, 24.0F, 80.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 8.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(EntityRenderState state) {

	}

	
}