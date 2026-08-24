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

public class RimeBackOvisModel extends EntityModel<RimeBackOvisRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "rime_back_ovis"), "main");
	
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart body_mid;
	private final ModelPart body_rear;
	private final ModelPart tail;
	private final ModelPart leg_back_left;
	private final ModelPart leg_back_left_calf;
	private final ModelPart leg_back_left_hoof;
	private final ModelPart leg_back_right;
	private final ModelPart leg_back_right_calf;
	private final ModelPart leg_back_right_hoof;
	private final ModelPart neck;
	private final ModelPart throat_ruff;
	private final ModelPart head;
	private final ModelPart forehead_crest;
	private final ModelPart ear_left;
	private final ModelPart ear_right;
	private final ModelPart horn_l_1;
	private final ModelPart horn_r_1;
	private final ModelPart leg_front_left;
	private final ModelPart leg_front_left_calf;
	private final ModelPart leg_front_left_hoof;
	private final ModelPart leg_front_right;
	private final ModelPart leg_front_right_calf;
	private final ModelPart leg_front_right_hoof;

	private final KeyframeAnimation idleAnimation;
	private final KeyframeAnimation walkAnimation;
	private final KeyframeAnimation runAnimation;
	private final KeyframeAnimation grazeAnimation;
	private final KeyframeAnimation restAnimation;
	private final KeyframeAnimation attackAnimation;
	private final KeyframeAnimation headbuttAnimation;

	public RimeBackOvisModel(ModelPart root) {
		super(root);
		this.root = root;
		this.body = root.getChild("body");
		this.body_mid = this.body.getChild("body_mid");
		this.body_rear = this.body_mid.getChild("body_rear");
		this.tail = this.body_rear.getChild("tail");
		this.leg_back_left = this.body_rear.getChild("leg_back_left");
		this.leg_back_left_calf = this.leg_back_left.getChild("leg_back_left_calf");
		this.leg_back_left_hoof = this.leg_back_left_calf.getChild("leg_back_left_hoof");
		this.leg_back_right = this.body_rear.getChild("leg_back_right");
		this.leg_back_right_calf = this.leg_back_right.getChild("leg_back_right_calf");
		this.leg_back_right_hoof = this.leg_back_right_calf.getChild("leg_back_right_hoof");
		this.neck = this.body.getChild("neck");
		this.throat_ruff = this.neck.getChild("throat_ruff");
		this.head = this.neck.getChild("head");
		this.forehead_crest = this.head.getChild("forehead_crest");
		this.ear_left = this.head.getChild("ear_left");
		this.ear_right = this.head.getChild("ear_right");
		this.horn_l_1 = this.head.getChild("horn_l_1");
		this.horn_r_1 = this.head.getChild("horn_r_1");
		this.leg_front_left = this.body.getChild("leg_front_left");
		this.leg_front_left_calf = this.leg_front_left.getChild("leg_front_left_calf");
		this.leg_front_left_hoof = this.leg_front_left_calf.getChild("leg_front_left_hoof");
		this.leg_front_right = this.body.getChild("leg_front_right");
		this.leg_front_right_calf = this.leg_front_right.getChild("leg_front_right_calf");
		this.leg_front_right_hoof = this.leg_front_right_calf.getChild("leg_front_right_hoof");

		this.idleAnimation = RimeBackOvisAnimation.IDLE.bake(root);
		this.walkAnimation = RimeBackOvisAnimation.WALK.bake(root);
		this.runAnimation = RimeBackOvisAnimation.RUN.bake(root);
		this.grazeAnimation = RimeBackOvisAnimation.GRAZE.bake(root);
		this.restAnimation = RimeBackOvisAnimation.REST.bake(root);
		this.attackAnimation = RimeBackOvisAnimation.ATTACK.bake(root);
		this.headbuttAnimation = RimeBackOvisAnimation.HEADBUTT.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -4.5F, -3.0F, 10.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.0F, -3.0F));

		PartDefinition body_mid = body.addOrReplaceChild("body_mid", CubeListBuilder.create().texOffs(32, 0).addBox(-5.0F, -4.5F, 0.0F, 10.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 3.0F));

		PartDefinition body_rear = body_mid.addOrReplaceChild("body_rear", CubeListBuilder.create().texOffs(0, 15).addBox(-4.5F, -4.0F, 0.0F, 9.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 6.0F));

		PartDefinition tail = body_rear.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(50, 39).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 5.0F, 0.6109F, 0.0F, 0.0F));

		PartDefinition leg_back_left = body_rear.addOrReplaceChild("leg_back_left", CubeListBuilder.create().texOffs(28, 27).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 1.0F, 1.0F));

		PartDefinition leg_back_left_calf = leg_back_left.addOrReplaceChild("leg_back_left_calf", CubeListBuilder.create().texOffs(52, 24).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition leg_back_left_hoof = leg_back_left_calf.addOrReplaceChild("leg_back_left_hoof", CubeListBuilder.create().texOffs(20, 39).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition leg_back_right = body_rear.addOrReplaceChild("leg_back_right", CubeListBuilder.create().texOffs(28, 27).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 1.0F, 1.0F));

		PartDefinition leg_back_right_calf = leg_back_right.addOrReplaceChild("leg_back_right_calf", CubeListBuilder.create().texOffs(52, 24).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition leg_back_right_hoof = leg_back_right_calf.addOrReplaceChild("leg_back_right_hoof", CubeListBuilder.create().texOffs(20, 39).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 39).addBox(-2.5F, -3.5F, -4.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -2.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition throat_ruff = neck.addOrReplaceChild("throat_ruff", CubeListBuilder.create().texOffs(18, 28).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.5F, -2.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition throat_plate_r2_r1 = throat_ruff.addOrReplaceChild("throat_plate_r2_r1", CubeListBuilder.create().texOffs(20, 45).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, 1.0F, -0.5F, 0.0F, 0.0F, -0.4189F));

		PartDefinition throat_plate_l2_r1 = throat_ruff.addOrReplaceChild("throat_plate_l2_r1", CubeListBuilder.create().texOffs(20, 45).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 1.0F, -0.5F, 0.0F, 0.0F, 0.4189F));

		PartDefinition throat_plate_r1_r1 = throat_ruff.addOrReplaceChild("throat_plate_r1_r1", CubeListBuilder.create().texOffs(18, 34).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 2.0F, -1.0F, 0.0F, 0.0F, -0.2094F));

		PartDefinition throat_plate_l1_r1 = throat_ruff.addOrReplaceChild("throat_plate_l1_r1", CubeListBuilder.create().texOffs(18, 34).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 2.0F, -1.0F, 0.0F, 0.0F, 0.2094F));

		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 15).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(36, 39).addBox(-2.0F, -2.0F, -9.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, -3.5F, 0.3491F, 0.0F, 0.0F));

		PartDefinition forehead_crest = head.addOrReplaceChild("forehead_crest", CubeListBuilder.create().texOffs(26, 46).addBox(-2.0F, -1.0F, -1.5F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(40, 46).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -5.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition ear_left = head.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(48, 46).addBox(0.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -1.0F, -2.0F, 0.0F, 0.3491F, -0.2618F));

		PartDefinition ear_right = head.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(48, 46).addBox(-3.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.0F, -2.0F, 0.0F, -0.3491F, 0.2618F));

		PartDefinition horn_l_1 = head.addOrReplaceChild("horn_l_1", CubeListBuilder.create().texOffs(0, 49).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -4.0F, -3.0F, 0.0F, 0.0698F, 0.2443F));

		PartDefinition horn_l_2 = horn_l_1.addOrReplaceChild("horn_l_2", CubeListBuilder.create().texOffs(12, 50).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.2443F, 0.1047F, 0.2094F));

		PartDefinition horn_l_3 = horn_l_2.addOrReplaceChild("horn_l_3", CubeListBuilder.create().texOffs(40, 50).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.3142F, 0.1396F, 0.1745F));

		PartDefinition horn_l_4 = horn_l_3.addOrReplaceChild("horn_l_4", CubeListBuilder.create().texOffs(52, 50).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.3142F, 0.1396F, 0.1047F));

		PartDefinition horn_l_5 = horn_l_4.addOrReplaceChild("horn_l_5", CubeListBuilder.create().texOffs(24, 51).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.3142F, 0.0698F, 0.0F));

		PartDefinition horn_l_6 = horn_l_5.addOrReplaceChild("horn_l_6", CubeListBuilder.create().texOffs(32, 51).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.3142F, -0.0349F, -0.1047F));

		PartDefinition horn_l_7 = horn_l_6.addOrReplaceChild("horn_l_7", CubeListBuilder.create().texOffs(0, 54).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.3142F, -0.1396F, -0.1396F));

		PartDefinition horn_l_8 = horn_l_7.addOrReplaceChild("horn_l_8", CubeListBuilder.create().texOffs(32, 54).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.3142F, -0.1745F, -0.1396F));

		PartDefinition horn_l_9 = horn_l_8.addOrReplaceChild("horn_l_9", CubeListBuilder.create().texOffs(48, 27).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.3142F, -0.1745F, -0.0698F));

		PartDefinition horn_l_10 = horn_l_9.addOrReplaceChild("horn_l_10", CubeListBuilder.create().texOffs(48, 29).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.3142F, -0.1396F, 0.0349F));

		PartDefinition horn_l_11 = horn_l_10.addOrReplaceChild("horn_l_11", CubeListBuilder.create().texOffs(48, 31).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.3142F, -0.1047F, 0.1396F));

		PartDefinition horn_l_12 = horn_l_11.addOrReplaceChild("horn_l_12", CubeListBuilder.create().texOffs(24, 34).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.3142F, -0.0698F, 0.2094F));

		PartDefinition horn_l_13 = horn_l_12.addOrReplaceChild("horn_l_13", CubeListBuilder.create().texOffs(24, 36).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.2793F, -0.0349F, 0.2443F));

		PartDefinition horn_l_14 = horn_l_13.addOrReplaceChild("horn_l_14", CubeListBuilder.create().texOffs(58, 46).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.2443F, 0.0F, 0.2793F));

		PartDefinition horn_r_1 = head.addOrReplaceChild("horn_r_1", CubeListBuilder.create().texOffs(0, 49).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -4.0F, -3.0F, 0.0F, -0.0698F, -0.2443F));

		PartDefinition horn_r_2 = horn_r_1.addOrReplaceChild("horn_r_2", CubeListBuilder.create().texOffs(12, 50).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.2443F, -0.1047F, -0.2094F));

		PartDefinition horn_r_3 = horn_r_2.addOrReplaceChild("horn_r_3", CubeListBuilder.create().texOffs(40, 50).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.3142F, -0.1396F, -0.1745F));

		PartDefinition horn_r_4 = horn_r_3.addOrReplaceChild("horn_r_4", CubeListBuilder.create().texOffs(52, 50).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.3142F, -0.1396F, -0.1047F));

		PartDefinition horn_r_5 = horn_r_4.addOrReplaceChild("horn_r_5", CubeListBuilder.create().texOffs(24, 51).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.3142F, -0.0698F, 0.0F));

		PartDefinition horn_r_6 = horn_r_5.addOrReplaceChild("horn_r_6", CubeListBuilder.create().texOffs(32, 51).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.3142F, 0.0349F, 0.1047F));

		PartDefinition horn_r_7 = horn_r_6.addOrReplaceChild("horn_r_7", CubeListBuilder.create().texOffs(0, 54).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.3142F, 0.1396F, 0.1396F));

		PartDefinition horn_r_8 = horn_r_7.addOrReplaceChild("horn_r_8", CubeListBuilder.create().texOffs(32, 54).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.3142F, 0.1745F, 0.1396F));

		PartDefinition horn_r_9 = horn_r_8.addOrReplaceChild("horn_r_9", CubeListBuilder.create().texOffs(48, 27).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.3142F, 0.1745F, 0.0698F));

		PartDefinition horn_r_10 = horn_r_9.addOrReplaceChild("horn_r_10", CubeListBuilder.create().texOffs(48, 29).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.3142F, 0.1396F, -0.0349F));

		PartDefinition horn_r_11 = horn_r_10.addOrReplaceChild("horn_r_11", CubeListBuilder.create().texOffs(48, 31).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.3142F, 0.1047F, -0.1396F));

		PartDefinition horn_r_12 = horn_r_11.addOrReplaceChild("horn_r_12", CubeListBuilder.create().texOffs(24, 34).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.3142F, 0.0698F, -0.2094F));

		PartDefinition horn_r_13 = horn_r_12.addOrReplaceChild("horn_r_13", CubeListBuilder.create().texOffs(24, 36).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.2793F, 0.0349F, -0.2443F));

		PartDefinition horn_r_14 = horn_r_13.addOrReplaceChild("horn_r_14", CubeListBuilder.create().texOffs(58, 46).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.2443F, 0.0F, -0.2793F));

		PartDefinition leg_front_left = body.addOrReplaceChild("leg_front_left", CubeListBuilder.create().texOffs(0, 28).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 1.0F, -1.0F));

		PartDefinition leg_front_left_calf = leg_front_left.addOrReplaceChild("leg_front_left_calf", CubeListBuilder.create().texOffs(52, 15).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

		PartDefinition leg_front_left_hoof = leg_front_left_calf.addOrReplaceChild("leg_front_left_hoof", CubeListBuilder.create().texOffs(48, 33).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

		PartDefinition leg_front_right = body.addOrReplaceChild("leg_front_right", CubeListBuilder.create().texOffs(0, 28).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 1.0F, -1.0F));

		PartDefinition leg_front_right_calf = leg_front_right.addOrReplaceChild("leg_front_right_calf", CubeListBuilder.create().texOffs(52, 15).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

		PartDefinition leg_front_right_hoof = leg_front_right_calf.addOrReplaceChild("leg_front_right_hoof", CubeListBuilder.create().texOffs(48, 33).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(RimeBackOvisRenderState state) {
		super.setupAnim(state);
		this.root.resetPose();

		// Head tracking when not performing heavy head animations
		if (!state.headbuttAnimationState.isStarted() && !state.attackAnimationState.isStarted() && !state.grazeAnimationState.isStarted()) {
			this.head.yRot = state.yRot * Mth.DEG_TO_RAD * 0.5F;
			this.head.xRot = state.xRot * Mth.DEG_TO_RAD * 0.5F;
		}

		// Apply keyframe animations
		this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
		this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
		this.runAnimation.apply(state.runAnimationState, state.ageInTicks);
		this.grazeAnimation.apply(state.grazeAnimationState, state.ageInTicks);
		this.restAnimation.apply(state.restAnimationState, state.ageInTicks);
		this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
		this.headbuttAnimation.apply(state.headbuttAnimationState, state.ageInTicks);
	}
}
