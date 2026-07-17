package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RimeBackOvisModel extends EntityModel<RimeBackOvisRenderState> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "rime_back_ovis"), "main");
	private final ModelPart test_body;
	private final ModelPart body;
	private final ModelPart body_mid;
	private final ModelPart body_rear;
	private final ModelPart tail;
	private final ModelPart plate_g_11;
	private final ModelPart plate_g_12;
	private final ModelPart plate_g_13;
	private final ModelPart plate_g_14;
	private final ModelPart plate_g_15;
	private final ModelPart leg_back_left;
	private final ModelPart leg_back_left_mid;
	private final ModelPart leg_back_left_calf;
	private final ModelPart leg_back_left_hoof;
	private final ModelPart leg_back_right;
	private final ModelPart leg_back_right_mid;
	private final ModelPart leg_back_right_calf;
	private final ModelPart leg_back_right_hoof;
	private final ModelPart plate_g_6;
	private final ModelPart plate_g_7;
	private final ModelPart plate_g_8;
	private final ModelPart plate_g_9;
	private final ModelPart plate_g_10;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart horn_l_1;
	private final ModelPart horn_l_2;
	private final ModelPart horn_l_3;
	private final ModelPart horn_l_4;
	private final ModelPart horn_l_5;
	private final ModelPart horn_l_6;
	private final ModelPart horn_l_7;
	private final ModelPart horn_l_8;
	private final ModelPart horn_l_9;
	private final ModelPart horn_l_10;
	private final ModelPart horn_l_11;
	private final ModelPart horn_l_12;
	private final ModelPart horn_l_13;
	private final ModelPart horn_l_14;
	private final ModelPart horn_l_15;
	private final ModelPart horn_r_1;
	private final ModelPart horn_r_2;
	private final ModelPart horn_r_3;
	private final ModelPart horn_r_4;
	private final ModelPart horn_r_5;
	private final ModelPart horn_r_6;
	private final ModelPart horn_r_7;
	private final ModelPart horn_r_8;
	private final ModelPart horn_r_9;
	private final ModelPart horn_r_10;
	private final ModelPart horn_r_11;
	private final ModelPart horn_r_12;
	private final ModelPart horn_r_13;
	private final ModelPart horn_r_14;
	private final ModelPart horn_r_15;
	private final ModelPart plate_g_16;
	private final ModelPart plate_g_17;
	private final ModelPart plate_g_18;
	private final ModelPart plate_g_19;
	private final ModelPart plate_g_20;
	private final ModelPart plate_g_21;
	private final ModelPart plate_g_22;
	private final ModelPart plate_g_23;
	private final ModelPart plate_g_24;
	private final ModelPart plate_g_25;
	private final ModelPart plate_g_1;
	private final ModelPart plate_g_2;
	private final ModelPart plate_g_3;
	private final ModelPart plate_g_4;
	private final ModelPart plate_g_5;
	private final ModelPart leg_front_left;
	private final ModelPart leg_front_left_mid;
	private final ModelPart leg_front_left_calf;
	private final ModelPart leg_front_left_hoof;
	private final ModelPart leg_front_right;
	private final ModelPart leg_front_right_mid;
	private final ModelPart leg_front_right_calf;
	private final ModelPart leg_front_right_hoof;

	public RimeBackOvisModel(ModelPart root) {
		super(root);
		this.test_body = root.getChild("test_body");
		this.body = root.getChild("body");
		this.body_mid = this.body.getChild("body_mid");
		this.body_rear = this.body_mid.getChild("body_rear");
		this.tail = this.body_rear.getChild("tail");
		this.plate_g_11 = this.body_rear.getChild("plate_g_11");
		this.plate_g_12 = this.body_rear.getChild("plate_g_12");
		this.plate_g_13 = this.body_rear.getChild("plate_g_13");
		this.plate_g_14 = this.body_rear.getChild("plate_g_14");
		this.plate_g_15 = this.body_rear.getChild("plate_g_15");
		this.leg_back_left = this.body_rear.getChild("leg_back_left");
		this.leg_back_left_mid = this.leg_back_left.getChild("leg_back_left_mid");
		this.leg_back_left_calf = this.leg_back_left_mid.getChild("leg_back_left_calf");
		this.leg_back_left_hoof = this.leg_back_left_calf.getChild("leg_back_left_hoof");
		this.leg_back_right = this.body_rear.getChild("leg_back_right");
		this.leg_back_right_mid = this.leg_back_right.getChild("leg_back_right_mid");
		this.leg_back_right_calf = this.leg_back_right_mid.getChild("leg_back_right_calf");
		this.leg_back_right_hoof = this.leg_back_right_calf.getChild("leg_back_right_hoof");
		this.plate_g_6 = this.body_mid.getChild("plate_g_6");
		this.plate_g_7 = this.body_mid.getChild("plate_g_7");
		this.plate_g_8 = this.body_mid.getChild("plate_g_8");
		this.plate_g_9 = this.body_mid.getChild("plate_g_9");
		this.plate_g_10 = this.body_mid.getChild("plate_g_10");
		this.neck = this.body.getChild("neck");
		this.head = this.neck.getChild("head");
		this.horn_l_1 = this.head.getChild("horn_l_1");
		this.horn_l_2 = this.horn_l_1.getChild("horn_l_2");
		this.horn_l_3 = this.horn_l_2.getChild("horn_l_3");
		this.horn_l_4 = this.horn_l_3.getChild("horn_l_4");
		this.horn_l_5 = this.horn_l_4.getChild("horn_l_5");
		this.horn_l_6 = this.horn_l_5.getChild("horn_l_6");
		this.horn_l_7 = this.horn_l_6.getChild("horn_l_7");
		this.horn_l_8 = this.horn_l_7.getChild("horn_l_8");
		this.horn_l_9 = this.horn_l_8.getChild("horn_l_9");
		this.horn_l_10 = this.horn_l_9.getChild("horn_l_10");
		this.horn_l_11 = this.horn_l_10.getChild("horn_l_11");
		this.horn_l_12 = this.horn_l_11.getChild("horn_l_12");
		this.horn_l_13 = this.horn_l_12.getChild("horn_l_13");
		this.horn_l_14 = this.horn_l_13.getChild("horn_l_14");
		this.horn_l_15 = this.horn_l_14.getChild("horn_l_15");
		this.horn_r_1 = this.head.getChild("horn_r_1");
		this.horn_r_2 = this.horn_r_1.getChild("horn_r_2");
		this.horn_r_3 = this.horn_r_2.getChild("horn_r_3");
		this.horn_r_4 = this.horn_r_3.getChild("horn_r_4");
		this.horn_r_5 = this.horn_r_4.getChild("horn_r_5");
		this.horn_r_6 = this.horn_r_5.getChild("horn_r_6");
		this.horn_r_7 = this.horn_r_6.getChild("horn_r_7");
		this.horn_r_8 = this.horn_r_7.getChild("horn_r_8");
		this.horn_r_9 = this.horn_r_8.getChild("horn_r_9");
		this.horn_r_10 = this.horn_r_9.getChild("horn_r_10");
		this.horn_r_11 = this.horn_r_10.getChild("horn_r_11");
		this.horn_r_12 = this.horn_r_11.getChild("horn_r_12");
		this.horn_r_13 = this.horn_r_12.getChild("horn_r_13");
		this.horn_r_14 = this.horn_r_13.getChild("horn_r_14");
		this.horn_r_15 = this.horn_r_14.getChild("horn_r_15");
		this.plate_g_16 = this.head.getChild("plate_g_16");
		this.plate_g_17 = this.head.getChild("plate_g_17");
		this.plate_g_18 = this.head.getChild("plate_g_18");
		this.plate_g_19 = this.neck.getChild("plate_g_19");
		this.plate_g_20 = this.neck.getChild("plate_g_20");
		this.plate_g_21 = this.neck.getChild("plate_g_21");
		this.plate_g_22 = this.neck.getChild("plate_g_22");
		this.plate_g_23 = this.neck.getChild("plate_g_23");
		this.plate_g_24 = this.neck.getChild("plate_g_24");
		this.plate_g_25 = this.neck.getChild("plate_g_25");
		this.plate_g_1 = this.body.getChild("plate_g_1");
		this.plate_g_2 = this.body.getChild("plate_g_2");
		this.plate_g_3 = this.body.getChild("plate_g_3");
		this.plate_g_4 = this.body.getChild("plate_g_4");
		this.plate_g_5 = this.body.getChild("plate_g_5");
		this.leg_front_left = this.body.getChild("leg_front_left");
		this.leg_front_left_mid = this.leg_front_left.getChild("leg_front_left_mid");
		this.leg_front_left_calf = this.leg_front_left_mid.getChild("leg_front_left_calf");
		this.leg_front_left_hoof = this.leg_front_left_calf.getChild("leg_front_left_hoof");
		this.leg_front_right = this.body.getChild("leg_front_right");
		this.leg_front_right_mid = this.leg_front_right.getChild("leg_front_right_mid");
		this.leg_front_right_calf = this.leg_front_right_mid.getChild("leg_front_right_calf");
		this.leg_front_right_hoof = this.leg_front_right_calf.getChild("leg_front_right_hoof");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition test_body = partdefinition.addOrReplaceChild("test_body", CubeListBuilder.create(), PartPose.offset(0.0F, 10.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -4.0F, -3.0F, 10.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.0F, -3.0F));

		PartDefinition body_mid = body.addOrReplaceChild("body_mid", CubeListBuilder.create().texOffs(0, 15).addBox(-5.0F, -4.0F, -2.5F, 10.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 5.5F));

		PartDefinition body_rear = body_mid.addOrReplaceChild("body_rear", CubeListBuilder.create().texOffs(0, 29).addBox(-5.0F, -4.0F, -2.5F, 10.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 5.0F));

		PartDefinition tail = body_rear.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 43).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 2.5F, 0.4363F, 0.0F, 0.0F));

		PartDefinition plate_g_11 = body_rear.addOrReplaceChild("plate_g_11", CubeListBuilder.create().texOffs(16, 24).addBox(-3.0F, -1.0F, -1.5F, 6.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -2.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition plate_g_12 = body_rear.addOrReplaceChild("plate_g_12", CubeListBuilder.create().texOffs(16, 24).addBox(-3.0F, -0.75F, -1.5F, 6.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -3.75F, -2.0F, 0.2618F, 0.1745F, -0.5236F));

		PartDefinition plate_g_13 = body_rear.addOrReplaceChild("plate_g_13", CubeListBuilder.create().texOffs(16, 24).addBox(-3.0F, -0.75F, -1.5F, 6.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -3.75F, -2.0F, 0.2618F, -0.1745F, 0.5236F));

		PartDefinition plate_g_14 = body_rear.addOrReplaceChild("plate_g_14", CubeListBuilder.create().texOffs(16, 24).addBox(-2.5F, -0.75F, -1.5F, 5.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.75F, -2.0F, 0.2618F, 0.3491F, -0.8727F));

		PartDefinition plate_g_15 = body_rear.addOrReplaceChild("plate_g_15", CubeListBuilder.create().texOffs(16, 24).addBox(-2.5F, -0.75F, -1.5F, 5.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.75F, -2.0F, 0.2618F, -0.3491F, 0.8727F));

		PartDefinition leg_back_left = body_rear.addOrReplaceChild("leg_back_left", CubeListBuilder.create().texOffs(44, 20).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 0.0F, -2.5F));

		PartDefinition leg_back_left_mid = leg_back_left.addOrReplaceChild("leg_back_left_mid", CubeListBuilder.create().texOffs(44, 28).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 3.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition leg_back_left_calf = leg_back_left_mid.addOrReplaceChild("leg_back_left_calf", CubeListBuilder.create().texOffs(44, 35).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition leg_back_left_hoof = leg_back_left_calf.addOrReplaceChild("leg_back_left_hoof", CubeListBuilder.create().texOffs(44, 43).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 1.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 0.0F));

		PartDefinition leg_back_right = body_rear.addOrReplaceChild("leg_back_right", CubeListBuilder.create().texOffs(44, 20).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, 0.0F, -2.5F));

		PartDefinition leg_back_right_mid = leg_back_right.addOrReplaceChild("leg_back_right_mid", CubeListBuilder.create().texOffs(44, 28).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 3.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition leg_back_right_calf = leg_back_right_mid.addOrReplaceChild("leg_back_right_calf", CubeListBuilder.create().texOffs(44, 35).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition leg_back_right_hoof = leg_back_right_calf.addOrReplaceChild("leg_back_right_hoof", CubeListBuilder.create().texOffs(44, 43).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 1.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 0.0F));

		PartDefinition plate_g_6 = body_mid.addOrReplaceChild("plate_g_6", CubeListBuilder.create().texOffs(16, 24).addBox(-3.0F, -1.0F, -1.5F, 6.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -1.5F, 0.2618F, 0.0F, 0.0F));

		PartDefinition plate_g_7 = body_mid.addOrReplaceChild("plate_g_7", CubeListBuilder.create().texOffs(16, 24).addBox(-3.0F, -0.75F, -1.5F, 6.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -3.75F, -1.5F, 0.2618F, 0.1745F, -0.5236F));

		PartDefinition plate_g_8 = body_mid.addOrReplaceChild("plate_g_8", CubeListBuilder.create().texOffs(16, 24).addBox(-3.0F, -0.75F, -1.5F, 6.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -3.75F, -1.5F, 0.2618F, -0.1745F, 0.5236F));

		PartDefinition plate_g_9 = body_mid.addOrReplaceChild("plate_g_9", CubeListBuilder.create().texOffs(16, 24).addBox(-2.5F, -0.75F, -1.5F, 5.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.75F, -1.5F, 0.2618F, 0.3491F, -0.8727F));

		PartDefinition plate_g_10 = body_mid.addOrReplaceChild("plate_g_10", CubeListBuilder.create().texOffs(16, 24).addBox(-2.5F, -0.75F, -1.5F, 5.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.75F, -1.5F, 0.2618F, -0.3491F, 0.8727F));

		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.0F, -3.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(32, 12).addBox(-2.0F, -2.0F, -10.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -2.0F, -0.3491F, 0.0F, 0.0F));

		PartDefinition ear_right_r1 = head.addOrReplaceChild("ear_right_r1", CubeListBuilder.create().texOffs(0, 49).addBox(-2.5F, -0.75F, -0.75F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.75F, -1.25F, 0.0F, 0.0F, 0.2618F));

		PartDefinition ear_left_r1 = head.addOrReplaceChild("ear_left_r1", CubeListBuilder.create().texOffs(0, 49).addBox(-0.5F, -0.75F, -0.75F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -1.75F, -1.25F, 0.0F, 0.0F, -0.2618F));

		PartDefinition horn_l_1 = head.addOrReplaceChild("horn_l_1", CubeListBuilder.create().texOffs(0, 34).addBox(-1.2F, -1.2F, -2.0F, 2.4F, 2.4F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -2.5F, -2.0F, -0.2443F, 0.1047F, -0.384F));

		PartDefinition horn_l_2 = horn_l_1.addOrReplaceChild("horn_l_2", CubeListBuilder.create().texOffs(0, 34).addBox(-1.15F, -1.15F, -1.9429F, 2.3F, 2.3F, 1.9429F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3693F, -0.3726F, -1.93F, -0.2518F, 0.1047F, -0.384F));

		PartDefinition horn_l_3 = horn_l_2.addOrReplaceChild("horn_l_3", CubeListBuilder.create().texOffs(0, 34).addBox(-1.1F, -1.1F, -1.8857F, 2.2F, 2.2F, 1.8857F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8085F, -0.5542F, -1.6775F, -0.2593F, 0.1047F, -0.384F));

		PartDefinition horn_l_4 = horn_l_3.addOrReplaceChild("horn_l_4", CubeListBuilder.create().texOffs(0, 34).addBox(-1.05F, -1.05F, -1.8286F, 2.1F, 2.1F, 1.8286F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2231F, -0.5121F, -1.3408F, -0.2668F, 0.1047F, -0.384F));

		PartDefinition horn_l_5 = horn_l_4.addOrReplaceChild("horn_l_5", CubeListBuilder.create().texOffs(0, 34).addBox(-1.0F, -1.0F, -1.7714F, 2.0F, 2.0F, 1.7714F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5221F, -0.2596F, -0.9795F, -0.2743F, 0.1047F, -0.384F));

		PartDefinition horn_l_6 = horn_l_5.addOrReplaceChild("horn_l_6", CubeListBuilder.create().texOffs(0, 34).addBox(-0.95F, -0.95F, -1.7143F, 1.9F, 1.9F, 1.7143F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6376F, 0.1452F, -0.6596F, -0.2817F, 0.1047F, -0.384F));

		PartDefinition horn_l_7 = horn_l_6.addOrReplaceChild("horn_l_7", CubeListBuilder.create().texOffs(0, 34).addBox(-0.9F, -0.9F, -1.6571F, 1.8F, 1.8F, 1.6571F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5405F, 0.6106F, -0.439F, -0.2892F, 0.1047F, -0.384F));

		PartDefinition horn_l_8 = horn_l_7.addOrReplaceChild("horn_l_8", CubeListBuilder.create().texOffs(0, 34).addBox(-0.85F, -0.85F, -1.6F, 1.7F, 1.7F, 1.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2482F, 1.0307F, -0.3548F, -0.2967F, 0.1047F, -0.384F));

		PartDefinition horn_l_9 = horn_l_8.addOrReplaceChild("horn_l_9", CubeListBuilder.create().texOffs(0, 34).addBox(-0.8F, -0.8F, -1.5429F, 1.6F, 1.6F, 1.5429F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8218F, 1.309F, -0.4138F, -0.3042F, 0.1047F, -0.384F));

		PartDefinition horn_l_10 = horn_l_9.addOrReplaceChild("horn_l_10", CubeListBuilder.create().texOffs(0, 34).addBox(-0.75F, -0.75F, -1.4857F, 1.5F, 1.5F, 1.4857F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3534F, 1.3809F, -0.5905F, -0.3117F, 0.1047F, -0.384F));

		PartDefinition horn_l_11 = horn_l_10.addOrReplaceChild("horn_l_11", CubeListBuilder.create().texOffs(0, 34).addBox(-0.7F, -0.7F, -1.4286F, 1.4F, 1.4F, 1.4286F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0547F, 1.2294F, -0.8324F, -0.3191F, 0.1047F, -0.384F));

		PartDefinition horn_l_12 = horn_l_11.addOrReplaceChild("horn_l_12", CubeListBuilder.create().texOffs(0, 34).addBox(-0.65F, -0.65F, -1.3714F, 1.3F, 1.3F, 1.3714F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.315F, 0.8907F, -1.0716F, -0.3266F, 0.1047F, -0.384F));

		PartDefinition horn_l_13 = horn_l_12.addOrReplaceChild("horn_l_13", CubeListBuilder.create().texOffs(0, 34).addBox(-0.6F, -0.6F, -1.3143F, 1.2F, 1.2F, 1.3143F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3755F, 0.4455F, -1.2415F, -0.3341F, 0.1047F, -0.384F));

		PartDefinition horn_l_14 = horn_l_13.addOrReplaceChild("horn_l_14", CubeListBuilder.create().texOffs(0, 34).addBox(-0.55F, -0.55F, -1.2571F, 1.1F, 1.1F, 1.2571F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2336F, 0.0F, -1.2934F, -0.3416F, 0.1047F, -0.384F));

		PartDefinition horn_l_15 = horn_l_14.addOrReplaceChild("horn_l_15", CubeListBuilder.create().texOffs(0, 34).addBox(-0.5F, -0.5F, -1.2F, 1.0F, 1.0F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.063F, -0.3407F, -1.2085F, -0.3491F, 0.1047F, -0.384F));

		PartDefinition horn_r_1 = head.addOrReplaceChild("horn_r_1", CubeListBuilder.create().texOffs(0, 48).addBox(-1.2F, -1.2F, -2.0F, 2.4F, 2.4F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -2.5F, -2.0F, -0.2443F, -0.1047F, 0.384F));

		PartDefinition horn_r_2 = horn_r_1.addOrReplaceChild("horn_r_2", CubeListBuilder.create().texOffs(0, 48).addBox(-1.15F, -1.15F, -1.9429F, 2.3F, 2.3F, 1.9429F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3693F, -0.3726F, -1.93F, -0.2518F, -0.1047F, 0.384F));

		PartDefinition horn_r_3 = horn_r_2.addOrReplaceChild("horn_r_3", CubeListBuilder.create().texOffs(0, 48).addBox(-1.1F, -1.1F, -1.8857F, 2.2F, 2.2F, 1.8857F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8085F, -0.5542F, -1.6775F, -0.2593F, -0.1047F, 0.384F));

		PartDefinition horn_r_4 = horn_r_3.addOrReplaceChild("horn_r_4", CubeListBuilder.create().texOffs(0, 48).addBox(-1.05F, -1.05F, -1.8286F, 2.1F, 2.1F, 1.8286F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2231F, -0.5121F, -1.3408F, -0.2668F, -0.1047F, 0.384F));

		PartDefinition horn_r_5 = horn_r_4.addOrReplaceChild("horn_r_5", CubeListBuilder.create().texOffs(0, 48).addBox(-1.0F, -1.0F, -1.7714F, 2.0F, 2.0F, 1.7714F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5221F, -0.2596F, -0.9795F, -0.2743F, -0.1047F, 0.384F));

		PartDefinition horn_r_6 = horn_r_5.addOrReplaceChild("horn_r_6", CubeListBuilder.create().texOffs(0, 48).addBox(-0.95F, -0.95F, -1.7143F, 1.9F, 1.9F, 1.7143F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6376F, 0.1452F, -0.6596F, -0.2817F, -0.1047F, 0.384F));

		PartDefinition horn_r_7 = horn_r_6.addOrReplaceChild("horn_r_7", CubeListBuilder.create().texOffs(0, 48).addBox(-0.9F, -0.9F, -1.6571F, 1.8F, 1.8F, 1.6571F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5405F, 0.6106F, -0.439F, -0.2892F, -0.1047F, 0.384F));

		PartDefinition horn_r_8 = horn_r_7.addOrReplaceChild("horn_r_8", CubeListBuilder.create().texOffs(0, 48).addBox(-0.85F, -0.85F, -1.6F, 1.7F, 1.7F, 1.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2482F, 1.0307F, -0.3548F, -0.2967F, -0.1047F, 0.384F));

		PartDefinition horn_r_9 = horn_r_8.addOrReplaceChild("horn_r_9", CubeListBuilder.create().texOffs(0, 48).addBox(-0.8F, -0.8F, -1.5429F, 1.6F, 1.6F, 1.5429F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8218F, 1.309F, -0.4138F, -0.3042F, -0.1047F, 0.384F));

		PartDefinition horn_r_10 = horn_r_9.addOrReplaceChild("horn_r_10", CubeListBuilder.create().texOffs(0, 48).addBox(-0.75F, -0.75F, -1.4857F, 1.5F, 1.5F, 1.4857F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3534F, 1.3809F, -0.5905F, -0.3117F, -0.1047F, 0.384F));

		PartDefinition horn_r_11 = horn_r_10.addOrReplaceChild("horn_r_11", CubeListBuilder.create().texOffs(0, 48).addBox(-0.7F, -0.7F, -1.4286F, 1.4F, 1.4F, 1.4286F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0547F, 1.2294F, -0.8324F, -0.3191F, -0.1047F, 0.384F));

		PartDefinition horn_r_12 = horn_r_11.addOrReplaceChild("horn_r_12", CubeListBuilder.create().texOffs(0, 48).addBox(-0.65F, -0.65F, -1.3714F, 1.3F, 1.3F, 1.3714F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.315F, 0.8907F, -1.0716F, -0.3266F, -0.1047F, 0.384F));

		PartDefinition horn_r_13 = horn_r_12.addOrReplaceChild("horn_r_13", CubeListBuilder.create().texOffs(0, 48).addBox(-0.6F, -0.6F, -1.3143F, 1.2F, 1.2F, 1.3143F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3755F, 0.4455F, -1.2415F, -0.3341F, -0.1047F, 0.384F));

		PartDefinition horn_r_14 = horn_r_13.addOrReplaceChild("horn_r_14", CubeListBuilder.create().texOffs(0, 48).addBox(-0.55F, -0.55F, -1.2571F, 1.1F, 1.1F, 1.2571F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2336F, 0.0F, -1.2934F, -0.3416F, -0.1047F, 0.384F));

		PartDefinition horn_r_15 = horn_r_14.addOrReplaceChild("horn_r_15", CubeListBuilder.create().texOffs(0, 48).addBox(-0.5F, -0.5F, -1.2F, 1.0F, 1.0F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.063F, -0.3407F, -1.2085F, -0.3491F, -0.1047F, 0.384F));

		PartDefinition plate_g_16 = head.addOrReplaceChild("plate_g_16", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, -0.75F, -1.5F, 4.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.25F, -5.5F, -0.3491F, 0.0F, 0.0F));

		PartDefinition plate_g_17 = head.addOrReplaceChild("plate_g_17", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, -0.75F, -1.5F, 4.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.25F, -3.5F));

		PartDefinition plate_g_18 = head.addOrReplaceChild("plate_g_18", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, -0.75F, -1.5F, 4.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.25F, -1.5F, 0.3491F, 0.0F, 0.0F));

		PartDefinition plate_g_19 = neck.addOrReplaceChild("plate_g_19", CubeListBuilder.create().texOffs(16, 54).addBox(-0.75F, -1.0F, -0.75F, 1.5F, 5.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition plate_g_20 = neck.addOrReplaceChild("plate_g_20", CubeListBuilder.create().texOffs(16, 54).addBox(-0.75F, -1.0F, -0.75F, 1.5F, 4.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 0.0F, -2.75F, -0.2618F, 0.0F, -0.1745F));

		PartDefinition plate_g_21 = neck.addOrReplaceChild("plate_g_21", CubeListBuilder.create().texOffs(16, 54).addBox(-0.75F, -1.0F, -0.75F, 1.5F, 4.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 0.0F, -2.75F, -0.2618F, 0.0F, 0.1745F));

		PartDefinition plate_g_22 = neck.addOrReplaceChild("plate_g_22", CubeListBuilder.create().texOffs(16, 54).addBox(-0.75F, -1.0F, -0.75F, 1.5F, 4.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 0.0F, -2.5F, -0.2618F, 0.0F, -0.3491F));

		PartDefinition plate_g_23 = neck.addOrReplaceChild("plate_g_23", CubeListBuilder.create().texOffs(16, 54).addBox(-0.75F, -1.0F, -0.75F, 1.5F, 4.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, -2.5F, -0.2618F, 0.0F, 0.3491F));

		PartDefinition plate_g_24 = neck.addOrReplaceChild("plate_g_24", CubeListBuilder.create().texOffs(16, 54).addBox(-0.6F, -1.0F, -0.6F, 1.2F, 3.5F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.35F, 0.0F, -2.4F, -0.2618F, 0.0F, -0.5236F));

		PartDefinition plate_g_25 = neck.addOrReplaceChild("plate_g_25", CubeListBuilder.create().texOffs(16, 54).addBox(-0.6F, -1.0F, -0.6F, 1.2F, 3.5F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.35F, 0.0F, -2.4F, -0.2618F, 0.0F, 0.5236F));

		PartDefinition plate_g_1 = body.addOrReplaceChild("plate_g_1", CubeListBuilder.create().texOffs(16, 24).addBox(-3.0F, -1.0F, -1.5F, 6.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -0.5F, 0.2618F, 0.0F, 0.0F));

		PartDefinition plate_g_2 = body.addOrReplaceChild("plate_g_2", CubeListBuilder.create().texOffs(16, 24).addBox(-3.0F, -0.75F, -1.5F, 6.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -3.75F, -0.5F, 0.2618F, 0.1745F, -0.5236F));

		PartDefinition plate_g_3 = body.addOrReplaceChild("plate_g_3", CubeListBuilder.create().texOffs(16, 24).addBox(-3.0F, -0.75F, -1.5F, 6.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -3.75F, -0.5F, 0.2618F, -0.1745F, 0.5236F));

		PartDefinition plate_g_4 = body.addOrReplaceChild("plate_g_4", CubeListBuilder.create().texOffs(16, 24).addBox(-2.5F, -0.75F, -1.5F, 5.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.75F, -0.5F, 0.2618F, 0.3491F, -0.8727F));

		PartDefinition plate_g_5 = body.addOrReplaceChild("plate_g_5", CubeListBuilder.create().texOffs(16, 24).addBox(-2.5F, -0.75F, -1.5F, 5.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.75F, -0.5F, 0.2618F, -0.3491F, 0.8727F));

		PartDefinition leg_front_left = body.addOrReplaceChild("leg_front_left", CubeListBuilder.create().texOffs(32, 20).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 0.0F, -1.0F));

		PartDefinition leg_front_left_mid = leg_front_left.addOrReplaceChild("leg_front_left_mid", CubeListBuilder.create().texOffs(32, 28).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 3.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition leg_front_left_calf = leg_front_left_mid.addOrReplaceChild("leg_front_left_calf", CubeListBuilder.create().texOffs(32, 35).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition leg_front_left_hoof = leg_front_left_calf.addOrReplaceChild("leg_front_left_hoof", CubeListBuilder.create().texOffs(32, 43).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 1.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 0.0F));

		PartDefinition leg_front_right = body.addOrReplaceChild("leg_front_right", CubeListBuilder.create().texOffs(32, 20).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, 0.0F, -1.0F));

		PartDefinition leg_front_right_mid = leg_front_right.addOrReplaceChild("leg_front_right_mid", CubeListBuilder.create().texOffs(32, 28).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 3.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition leg_front_right_calf = leg_front_right_mid.addOrReplaceChild("leg_front_right_calf", CubeListBuilder.create().texOffs(32, 35).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition leg_front_right_hoof = leg_front_right_calf.addOrReplaceChild("leg_front_right_hoof", CubeListBuilder.create().texOffs(32, 43).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 1.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(RimeBackOvisRenderState state) {
		float walkSpeed = state.walkAnimationSpeed;
		float walkPos = state.walkAnimationPos;

		if (state.isStunned) {
			this.head.yRot = Mth.sin(state.ageInTicks * 0.4F) * 0.3F;
			this.head.xRot = 0.1F;
		} else if (state.isBraced) {
			this.neck.xRot = -0.4F;
			this.head.xRot = -0.3F;
		} else {
			this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
			this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
			this.neck.xRot = 0.0F;
		}

		this.leg_front_left.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * walkSpeed;
		this.leg_front_right.xRot = Mth.cos(walkPos * 0.6662F + Mth.PI) * 1.4F * walkSpeed;
		this.leg_back_left.xRot = Mth.cos(walkPos * 0.6662F + Mth.PI) * 1.4F * walkSpeed;
		this.leg_back_right.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * walkSpeed;
	}
}