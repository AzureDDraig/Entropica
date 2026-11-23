package ddraig.net.entropica.client.renderer.cryo_static_hedgehog;

import ddraig.net.entropica.Entropica;
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
 * Fauna 21 — Cryo-Static Hedgehog (Needle-Quill Rodent)
 * Exact conversion of all 71 cubes, 24 bones, and 64x64 UVs from cryo_static_needle_hedgehog.bbmodel.
 */
public class CryoStaticHedgehogModel extends EntityModel<CryoStaticHedgehogRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_static_hedgehog"), "main");

	private final ModelPart root;
	private final ModelPart body_center;
	private final ModelPart chest;
	private final ModelPart head;
	private final ModelPart ear_left;
	private final ModelPart ear_right;
	private final ModelPart snout;
	private final ModelPart leg_fl;
	private final ModelPart leg_fl_paw;
	private final ModelPart leg_fr;
	private final ModelPart leg_fr_paw;
	private final ModelPart quills_front_top;
	private final ModelPart quills_front_left;
	private final ModelPart quills_front_right;
	private final ModelPart abdomen;
	private final ModelPart leg_bl;
	private final ModelPart leg_bl_shin;
	private final ModelPart leg_bl_paw;
	private final ModelPart leg_br;
	private final ModelPart leg_br_shin;
	private final ModelPart leg_br_paw;
	private final ModelPart quills_rear_top;
	private final ModelPart quills_rear_left;
	private final ModelPart quills_rear_right;

	private final KeyframeAnimation idleAnimation;
	private final KeyframeAnimation walkAnimation;
	private final KeyframeAnimation curlAnimation;
	private final KeyframeAnimation sneezeAnimation;
	private final KeyframeAnimation digInAnimation;
	private final KeyframeAnimation digHoldAnimation;
	private final KeyframeAnimation digOutAnimation;

	public CryoStaticHedgehogModel(ModelPart modelRoot) {
		super(modelRoot);
		this.root = modelRoot.getChild("root");
		this.body_center = this.root.getChild("body_center");
		this.chest = this.body_center.getChild("chest");
		this.head = this.chest.getChild("head");
		this.ear_left = this.head.getChild("ear_left");
		this.ear_right = this.head.getChild("ear_right");
		this.snout = this.head.getChild("snout");
		this.leg_fl = this.chest.getChild("leg_fl");
		this.leg_fl_paw = this.leg_fl.getChild("leg_fl_paw");
		this.leg_fr = this.chest.getChild("leg_fr");
		this.leg_fr_paw = this.leg_fr.getChild("leg_fr_paw");
		this.quills_front_top = this.chest.getChild("quills_front_top");
		this.quills_front_left = this.chest.getChild("quills_front_left");
		this.quills_front_right = this.chest.getChild("quills_front_right");
		this.abdomen = this.body_center.getChild("abdomen");
		this.leg_bl = this.abdomen.getChild("leg_bl");
		this.leg_bl_shin = this.leg_bl.getChild("leg_bl_shin");
		this.leg_bl_paw = this.leg_bl_shin.getChild("leg_bl_paw");
		this.leg_br = this.abdomen.getChild("leg_br");
		this.leg_br_shin = this.leg_br.getChild("leg_br_shin");
		this.leg_br_paw = this.leg_br_shin.getChild("leg_br_paw");
		this.quills_rear_top = this.abdomen.getChild("quills_rear_top");
		this.quills_rear_left = this.abdomen.getChild("quills_rear_left");
		this.quills_rear_right = this.abdomen.getChild("quills_rear_right");

		this.idleAnimation = CryoStaticHedgehogAnimations.idle.bake(modelRoot);
		this.walkAnimation = CryoStaticHedgehogAnimations.walk.bake(modelRoot);
		this.curlAnimation = CryoStaticHedgehogAnimations.curl.bake(modelRoot);
		this.sneezeAnimation = CryoStaticHedgehogAnimations.sneeze.bake(modelRoot);
		this.digInAnimation = CryoStaticHedgehogAnimations.dig_in.bake(modelRoot);
		this.digHoldAnimation = CryoStaticHedgehogAnimations.dig_hold.bake(modelRoot);
		this.digOutAnimation = CryoStaticHedgehogAnimations.dig_out.bake(modelRoot);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 17.0F, 0.0F));

		PartDefinition body_center = root.addOrReplaceChild("body_center", CubeListBuilder.create().texOffs(24, 12).addBox(-4.0F, -2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 1.0F));

		PartDefinition chest = body_center.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -3.0F, -6.0F, 10.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 12).addBox(-4.0F, -2.0F, -4.0F, 8.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -6.0F));

		PartDefinition ear_left = head.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(48, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -2.0F, -1.0F));

		PartDefinition ear_right = head.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(56, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -2.0F, -1.0F));

		PartDefinition snout = head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(0, 21).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(12, 21).addBox(-1.0F, -1.5F, -4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.5F, -4.0F));

		PartDefinition whisker_ul_r1 = snout.addOrReplaceChild("whisker_ul_r1", CubeListBuilder.create().texOffs(48, 17).addBox(-2.8F, -0.6F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-1.2F, -0.3F, -2.0F, 0.0F, 0.1745F, -0.1396F));

		PartDefinition whisker_ur_r1 = snout.addOrReplaceChild("whisker_ur_r1", CubeListBuilder.create().texOffs(56, 17).addBox(-0.2F, -0.6F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(1.2F, -0.3F, -2.0F, 0.0F, -0.1745F, 0.1396F));

		PartDefinition whisker_ll_r1 = snout.addOrReplaceChild("whisker_ll_r1", CubeListBuilder.create().texOffs(48, 19).addBox(-2.8F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-1.2F, 0.3F, -2.0F, 0.0F, 0.2443F, -0.384F));

		PartDefinition whisker_lr_r1 = snout.addOrReplaceChild("whisker_lr_r1", CubeListBuilder.create().texOffs(56, 19).addBox(-0.2F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(1.2F, 0.3F, -2.0F, 0.0F, -0.2443F, 0.384F));

		PartDefinition leg_fl = chest.addOrReplaceChild("leg_fl", CubeListBuilder.create(), PartPose.offset(-4.0F, 2.5F, -4.0F));

		PartDefinition leg_fl_upper_r1 = leg_fl.addOrReplaceChild("leg_fl_upper_r1", CubeListBuilder.create().texOffs(26, 21).addBox(-1.0F, -0.1F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.384F, 0.0F, 0.0F));

		PartDefinition leg_fl_paw = leg_fl.addOrReplaceChild("leg_fl_paw", CubeListBuilder.create().texOffs(16, 27).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.5F, -1.0F));

		PartDefinition leg_fr = chest.addOrReplaceChild("leg_fr", CubeListBuilder.create(), PartPose.offset(4.0F, 2.5F, -4.0F));

		PartDefinition leg_fr_upper_r1 = leg_fr.addOrReplaceChild("leg_fr_upper_r1", CubeListBuilder.create().texOffs(34, 21).addBox(-1.0F, -0.1F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.384F, 0.0F, 0.0F));

		PartDefinition leg_fr_paw = leg_fr.addOrReplaceChild("leg_fr_paw", CubeListBuilder.create().texOffs(26, 27).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.5F, -1.0F));

		PartDefinition quills_front_top = chest.addOrReplaceChild("quills_front_top", CubeListBuilder.create(), PartPose.offset(0.0F, -2.5F, -3.0F));

		PartDefinition q_c1_r1 = quills_front_top.addOrReplaceChild("q_c1_r1", CubeListBuilder.create().texOffs(0, 31).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(0.0F, 0.3F, -2.2F, -0.2094F, 0.0F, 0.0F));

		PartDefinition q_c2_r1 = quills_front_top.addOrReplaceChild("q_c2_r1", CubeListBuilder.create().texOffs(4, 31).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.22F)), PartPose.offsetAndRotation(0.0F, 0.3F, -0.2F, -0.384F, 0.0F, 0.0F));

		PartDefinition q_c3_r1 = quills_front_top.addOrReplaceChild("q_c3_r1", CubeListBuilder.create().texOffs(8, 31).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.22F)), PartPose.offsetAndRotation(0.0F, 0.3F, 1.8F, -0.5585F, 0.0F, 0.0F));

		PartDefinition q_l1_1_r1 = quills_front_top.addOrReplaceChild("q_l1_1_r1", CubeListBuilder.create().texOffs(12, 31).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(-1.6F, 0.5F, -1.8F, -0.2793F, -0.1047F, -0.3142F));

		PartDefinition q_r1_1_r1 = quills_front_top.addOrReplaceChild("q_r1_1_r1", CubeListBuilder.create().texOffs(16, 31).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(1.6F, 0.5F, -1.8F, -0.2793F, 0.1047F, 0.3142F));

		PartDefinition q_l1_2_r1 = quills_front_top.addOrReplaceChild("q_l1_2_r1", CubeListBuilder.create().texOffs(20, 31).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.22F)), PartPose.offsetAndRotation(-1.7F, 0.5F, 0.2F, -0.4538F, -0.1396F, -0.3316F));

		PartDefinition q_r1_2_r1 = quills_front_top.addOrReplaceChild("q_r1_2_r1", CubeListBuilder.create().texOffs(24, 31).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.22F)), PartPose.offsetAndRotation(1.7F, 0.5F, 0.2F, -0.4538F, 0.1396F, 0.3316F));

		PartDefinition q_l1_3_r1 = quills_front_top.addOrReplaceChild("q_l1_3_r1", CubeListBuilder.create().texOffs(28, 31).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.22F)), PartPose.offsetAndRotation(-1.7F, 0.5F, 2.2F, -0.6283F, -0.1745F, -0.3491F));

		PartDefinition q_r1_3_r1 = quills_front_top.addOrReplaceChild("q_r1_3_r1", CubeListBuilder.create().texOffs(32, 31).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.22F)), PartPose.offsetAndRotation(1.7F, 0.5F, 2.2F, -0.6283F, 0.1745F, 0.3491F));

		PartDefinition quills_front_left = chest.addOrReplaceChild("quills_front_left", CubeListBuilder.create(), PartPose.offset(-3.5F, -1.0F, -3.0F));

		PartDefinition q_l2_1_r1 = quills_front_left.addOrReplaceChild("q_l2_1_r1", CubeListBuilder.create().texOffs(36, 31).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(0.3F, -0.7F, -1.4F, -0.3142F, -0.1745F, -0.6283F));

		PartDefinition q_l2_2_r1 = quills_front_left.addOrReplaceChild("q_l2_2_r1", CubeListBuilder.create().texOffs(44, 31).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.2F, -0.7F, 0.6F, -0.4887F, -0.2094F, -0.6458F));

		PartDefinition q_l2_3_r1 = quills_front_left.addOrReplaceChild("q_l2_3_r1", CubeListBuilder.create().texOffs(52, 31).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.22F)), PartPose.offsetAndRotation(0.2F, -0.6F, 2.5F, -0.6632F, -0.2443F, -0.6632F));

		PartDefinition q_l3_1_r1 = quills_front_left.addOrReplaceChild("q_l3_1_r1", CubeListBuilder.create().texOffs(12, 40).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.16F)), PartPose.offsetAndRotation(-1.1F, 1.1F, -1.1F, -0.3491F, -0.2094F, -1.2217F));

		PartDefinition q_l3_2_r1 = quills_front_left.addOrReplaceChild("q_l3_2_r1", CubeListBuilder.create().texOffs(20, 40).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(-1.1F, 1.5F, 0.9F, -0.5585F, -0.2443F, -1.2741F));

		PartDefinition q_l3_3_r1 = quills_front_left.addOrReplaceChild("q_l3_3_r1", CubeListBuilder.create().texOffs(28, 40).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(-1.1F, 1.9F, 2.7F, -0.7679F, -0.2793F, -1.3265F));

		PartDefinition q_l2b_1_r1 = quills_front_left.addOrReplaceChild("q_l2b_1_r1", CubeListBuilder.create().texOffs(60, 31).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(-0.6F, 0.1F, -0.5F, -0.384F, -0.2094F, -0.925F));

		PartDefinition q_l2b_2_r1 = quills_front_left.addOrReplaceChild("q_l2b_2_r1", CubeListBuilder.create().texOffs(4, 40).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-0.7F, 0.2F, 1.7F, -0.5934F, -0.2443F, -0.9599F));

		PartDefinition quills_front_right = chest.addOrReplaceChild("quills_front_right", CubeListBuilder.create(), PartPose.offset(3.5F, -1.0F, -3.0F));

		PartDefinition q_r2_1_r1 = quills_front_right.addOrReplaceChild("q_r2_1_r1", CubeListBuilder.create().texOffs(40, 31).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(-0.3F, -0.7F, -1.4F, -0.3142F, 0.1745F, 0.6283F));

		PartDefinition q_r2_2_r1 = quills_front_right.addOrReplaceChild("q_r2_2_r1", CubeListBuilder.create().texOffs(48, 31).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-0.2F, -0.7F, 0.6F, -0.4887F, 0.2094F, 0.6458F));

		PartDefinition q_r2_3_r1 = quills_front_right.addOrReplaceChild("q_r2_3_r1", CubeListBuilder.create().texOffs(56, 31).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.22F)), PartPose.offsetAndRotation(-0.2F, -0.6F, 2.5F, -0.6632F, 0.2443F, 0.6632F));

		PartDefinition q_r3_1_r1 = quills_front_right.addOrReplaceChild("q_r3_1_r1", CubeListBuilder.create().texOffs(16, 40).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.16F)), PartPose.offsetAndRotation(1.1F, 1.1F, -1.1F, -0.3491F, 0.2094F, 1.2217F));

		PartDefinition q_r3_2_r1 = quills_front_right.addOrReplaceChild("q_r3_2_r1", CubeListBuilder.create().texOffs(24, 40).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(1.1F, 1.5F, 0.9F, -0.5585F, 0.2443F, 1.2741F));

		PartDefinition q_r3_3_r1 = quills_front_right.addOrReplaceChild("q_r3_3_r1", CubeListBuilder.create().texOffs(32, 40).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(1.1F, 1.9F, 2.7F, -0.7679F, 0.2793F, 1.3265F));

		PartDefinition q_r2b_1_r1 = quills_front_right.addOrReplaceChild("q_r2b_1_r1", CubeListBuilder.create().texOffs(0, 40).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(0.6F, 0.1F, -0.5F, -0.384F, 0.2094F, 0.925F));

		PartDefinition q_r2b_2_r1 = quills_front_right.addOrReplaceChild("q_r2b_2_r1", CubeListBuilder.create().texOffs(8, 40).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.7F, 0.2F, 1.7F, -0.5934F, 0.2443F, 0.9599F));

		PartDefinition abdomen = body_center.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(32, 0).addBox(-5.0F, -3.0F, 0.0F, 10.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(20, 21).addBox(-1.0F, 0.0F, 6.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leg_bl = abdomen.addOrReplaceChild("leg_bl", CubeListBuilder.create(), PartPose.offset(-4.0F, 2.5F, 3.0F));

		PartDefinition leg_bl_thigh_r1 = leg_bl.addOrReplaceChild("leg_bl_thigh_r1", CubeListBuilder.create().texOffs(42, 21).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4887F, 0.0F, 0.0F));

		PartDefinition leg_bl_shin = leg_bl.addOrReplaceChild("leg_bl_shin", CubeListBuilder.create().texOffs(0, 27).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 1.0F));

		PartDefinition leg_bl_paw = leg_bl_shin.addOrReplaceChild("leg_bl_paw", CubeListBuilder.create().texOffs(36, 27).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.5F, 0.0F));

		PartDefinition leg_br = abdomen.addOrReplaceChild("leg_br", CubeListBuilder.create(), PartPose.offset(4.0F, 2.5F, 3.0F));

		PartDefinition leg_br_thigh_r1 = leg_br.addOrReplaceChild("leg_br_thigh_r1", CubeListBuilder.create().texOffs(50, 21).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4887F, 0.0F, 0.0F));

		PartDefinition leg_br_shin = leg_br.addOrReplaceChild("leg_br_shin", CubeListBuilder.create().texOffs(8, 27).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 1.0F));

		PartDefinition leg_br_paw = leg_br_shin.addOrReplaceChild("leg_br_paw", CubeListBuilder.create().texOffs(46, 27).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.5F, 0.0F));

		PartDefinition quills_rear_top = abdomen.addOrReplaceChild("quills_rear_top", CubeListBuilder.create(), PartPose.offset(0.0F, -2.5F, 2.5F));

		PartDefinition q_c4_r1 = quills_rear_top.addOrReplaceChild("q_c4_r1", CubeListBuilder.create().texOffs(36, 40).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.22F)), PartPose.offsetAndRotation(0.0F, 0.3F, -1.7F, -0.7679F, 0.0F, 0.0F));

		PartDefinition q_c5_r1 = quills_rear_top.addOrReplaceChild("q_c5_r1", CubeListBuilder.create().texOffs(40, 40).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.22F)), PartPose.offsetAndRotation(0.0F, 0.5F, 0.3F, -1.0123F, 0.0F, 0.0F));

		PartDefinition q_c6_r1 = quills_rear_top.addOrReplaceChild("q_c6_r1", CubeListBuilder.create().texOffs(44, 40).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.0F, 1.5F, 2.3F, -1.2915F, 0.0F, 0.0F));

		PartDefinition q_l1_4_r1 = quills_rear_top.addOrReplaceChild("q_l1_4_r1", CubeListBuilder.create().texOffs(48, 40).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.22F)), PartPose.offsetAndRotation(-1.7F, 0.5F, -1.3F, -0.8378F, -0.1745F, -0.3491F));

		PartDefinition q_r1_4_r1 = quills_rear_top.addOrReplaceChild("q_r1_4_r1", CubeListBuilder.create().texOffs(52, 40).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.22F)), PartPose.offsetAndRotation(1.7F, 0.5F, -1.3F, -0.8378F, 0.1745F, 0.3491F));

		PartDefinition q_l1_5_r1 = quills_rear_top.addOrReplaceChild("q_l1_5_r1", CubeListBuilder.create().texOffs(56, 40).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-1.6F, 0.9F, 0.7F, -1.0821F, -0.2094F, -0.3142F));

		PartDefinition q_r1_5_r1 = quills_rear_top.addOrReplaceChild("q_r1_5_r1", CubeListBuilder.create().texOffs(60, 40).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(1.6F, 0.9F, 0.7F, -1.0821F, 0.2094F, 0.3142F));

		PartDefinition q_l1_6_r1 = quills_rear_top.addOrReplaceChild("q_l1_6_r1", CubeListBuilder.create().texOffs(0, 49).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(-1.4F, 2.0F, 2.5F, -1.3614F, -0.1745F, -0.2443F));

		PartDefinition q_r1_6_r1 = quills_rear_top.addOrReplaceChild("q_r1_6_r1", CubeListBuilder.create().texOffs(4, 49).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(1.4F, 2.0F, 2.5F, -1.3614F, 0.1745F, 0.2443F));

		PartDefinition quills_rear_left = abdomen.addOrReplaceChild("quills_rear_left", CubeListBuilder.create(), PartPose.offset(-3.5F, -1.0F, 2.5F));

		PartDefinition q_l2_4_r1 = quills_rear_left.addOrReplaceChild("q_l2_4_r1", CubeListBuilder.create().texOffs(8, 49).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.2F, -0.5F, -1.1F, -0.8727F, -0.2618F, -0.6632F));

		PartDefinition q_l2_5_r1 = quills_rear_left.addOrReplaceChild("q_l2_5_r1", CubeListBuilder.create().texOffs(16, 49).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.4F, 0.1F, 0.9F, -1.117F, -0.2793F, -0.6632F));

		PartDefinition q_l2_6_r1 = quills_rear_left.addOrReplaceChild("q_l2_6_r1", CubeListBuilder.create().texOffs(24, 49).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(0.8F, 1.1F, 2.5F, -1.3614F, -0.2793F, -0.5934F));

		PartDefinition q_l3_4_r1 = quills_rear_left.addOrReplaceChild("q_l3_4_r1", CubeListBuilder.create().texOffs(48, 49).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(-1.1F, 1.9F, -0.9F, -0.9774F, -0.3142F, -1.3265F));

		PartDefinition q_l3_5_r1 = quills_rear_left.addOrReplaceChild("q_l3_5_r1", CubeListBuilder.create().texOffs(56, 49).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.16F)), PartPose.offsetAndRotation(-0.7F, 2.3F, 1.3F, -1.2566F, -0.3142F, -1.2915F));

		PartDefinition q_l2b_3_r1 = quills_rear_left.addOrReplaceChild("q_l2b_3_r1", CubeListBuilder.create().texOffs(32, 49).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-0.7F, 0.4F, -1.7F, -0.8029F, -0.2793F, -0.9774F));

		PartDefinition q_l2b_4_r1 = quills_rear_left.addOrReplaceChild("q_l2b_4_r1", CubeListBuilder.create().texOffs(40, 49).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(-0.5F, 0.9F, 0.4F, -1.0821F, -0.3142F, -0.9774F));

		PartDefinition quills_rear_right = abdomen.addOrReplaceChild("quills_rear_right", CubeListBuilder.create(), PartPose.offset(3.5F, -1.0F, 2.5F));

		PartDefinition q_r2_4_r1 = quills_rear_right.addOrReplaceChild("q_r2_4_r1", CubeListBuilder.create().texOffs(12, 49).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-0.2F, -0.5F, -1.1F, -0.8727F, 0.2618F, 0.6632F));

		PartDefinition q_r2_5_r1 = quills_rear_right.addOrReplaceChild("q_r2_5_r1", CubeListBuilder.create().texOffs(20, 49).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-0.4F, 0.1F, 0.9F, -1.117F, 0.2793F, 0.6632F));

		PartDefinition q_r2_6_r1 = quills_rear_right.addOrReplaceChild("q_r2_6_r1", CubeListBuilder.create().texOffs(28, 49).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(-0.8F, 1.1F, 2.5F, -1.3614F, 0.2793F, 0.5934F));

		PartDefinition q_r3_4_r1 = quills_rear_right.addOrReplaceChild("q_r3_4_r1", CubeListBuilder.create().texOffs(52, 49).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(1.1F, 1.9F, -0.9F, -0.9774F, 0.3142F, 1.3265F));

		PartDefinition q_r3_5_r1 = quills_rear_right.addOrReplaceChild("q_r3_5_r1", CubeListBuilder.create().texOffs(60, 49).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.16F)), PartPose.offsetAndRotation(0.7F, 2.3F, 1.3F, -1.2566F, 0.3142F, 1.2915F));

		PartDefinition q_r2b_3_r1 = quills_rear_right.addOrReplaceChild("q_r2b_3_r1", CubeListBuilder.create().texOffs(36, 49).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.7F, 0.4F, -1.7F, -0.8029F, 0.2793F, 0.9774F));

		PartDefinition q_r2b_4_r1 = quills_rear_right.addOrReplaceChild("q_r2b_4_r1", CubeListBuilder.create().texOffs(44, 49).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offsetAndRotation(0.5F, 0.9F, 0.4F, -1.0821F, 0.3142F, 0.9774F));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(CryoStaticHedgehogRenderState state) {
		super.setupAnim(state);

		if (!state.isCurled) {
			float clampedYaw = Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD;
			float clampedPitch = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
			this.head.yRot += clampedYaw * 0.7F;
			this.head.xRot += clampedPitch * 0.6F;
		}

		this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
		this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks, 1.35F);
		this.digInAnimation.apply(state.digInAnimationState, state.ageInTicks);
		this.digHoldAnimation.apply(state.digHoldAnimationState, state.ageInTicks);
		this.digOutAnimation.apply(state.digOutAnimationState, state.ageInTicks);
		this.sneezeAnimation.apply(state.sneezeAnimationState, state.ageInTicks);
		this.curlAnimation.apply(state.curlAnimationState, state.ageInTicks);

		if (state.isStormCharged) {
			float buzz = Mth.sin(state.ageInTicks * 1.65F) * 0.045F;
			float altBuzz = Mth.cos(state.ageInTicks * 1.95F) * 0.04F;
			this.quills_front_top.xRot -= Math.abs(buzz);
			this.quills_rear_top.xRot += Math.abs(altBuzz);
			this.quills_front_left.zRot -= buzz;
			this.quills_front_right.zRot += buzz;
			this.quills_rear_left.zRot -= altBuzz;
			this.quills_rear_right.zRot += altBuzz;
			this.snout.xRot += Mth.sin(state.ageInTicks * 0.9F) * 0.03F;
		}
	}
}
