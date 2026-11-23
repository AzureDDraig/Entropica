package ddraig.net.entropica.client.renderer.vapor_bellows_crab;

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
 * Fauna 22 — Vapor-Bellows Crab (Thermal Crustacean)
 * Exact conversion of all 45 cubes, 43 bones, and 128x128 UVs from vapor_bellows_crab.bbmodel.
 */
public class VaporBellowsCrabModel extends EntityModel<VaporBellowsCrabRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_bellows_crab"), "main");

	private final ModelPart root;
	private final ModelPart body_center;
	private final ModelPart carapace;
	private final ModelPart eye_left;
	private final ModelPart eye_right;
	private final ModelPart mandible_left;
	private final ModelPart mandible_right;
	private final ModelPart bellows_lower;
	private final ModelPart bellows_upper;
	private final ModelPart chimney_main;
	private final ModelPart chimney_side_r;
	private final ModelPart chimney_front_l;
	private final ModelPart chimney_mid_l;
	private final ModelPart chimney_rear_r;
	private final ModelPart chimney_rear_l;
	private final ModelPart arm_left;
	private final ModelPart forearm_left;
	private final ModelPart claw_left;
	private final ModelPart claw_l_dactyl;
	private final ModelPart claw_l_pollex;
	private final ModelPart arm_right;
	private final ModelPart forearm_right;
	private final ModelPart claw_right;
	private final ModelPart claw_r_dactyl;
	private final ModelPart claw_r_pollex;
	private final ModelPart leg_l1;
	private final ModelPart leg_l1_tib;
	private final ModelPart leg_l1_tar;
	private final ModelPart leg_l2;
	private final ModelPart leg_l2_tib;
	private final ModelPart leg_l2_tar;
	private final ModelPart leg_l3;
	private final ModelPart leg_l3_tib;
	private final ModelPart leg_l3_tar;
	private final ModelPart leg_r1;
	private final ModelPart leg_r1_tib;
	private final ModelPart leg_r1_tar;
	private final ModelPart leg_r2;
	private final ModelPart leg_r2_tib;
	private final ModelPart leg_r2_tar;
	private final ModelPart leg_r3;
	private final ModelPart leg_r3_tib;
	private final ModelPart leg_r3_tar;

	private final KeyframeAnimation idleAnimation;
	private final KeyframeAnimation walkAnimation;
	private final KeyframeAnimation attackAnimation;
	private final KeyframeAnimation steamVentAnimation;
	private final KeyframeAnimation braceDefenseAnimation;

	public VaporBellowsCrabModel(ModelPart modelRoot) {
		super(modelRoot);
		this.root = modelRoot.getChild("root");
		this.body_center = this.root.getChild("body_center");
		this.carapace = this.body_center.getChild("carapace");
		this.eye_left = this.carapace.getChild("eye_left");
		this.eye_right = this.carapace.getChild("eye_right");
		this.mandible_left = this.carapace.getChild("mandible_left");
		this.mandible_right = this.carapace.getChild("mandible_right");
		this.bellows_lower = this.carapace.getChild("bellows_lower");
		this.bellows_upper = this.bellows_lower.getChild("bellows_upper");
		this.chimney_main = this.bellows_upper.getChild("chimney_main");
		this.chimney_side_r = this.bellows_upper.getChild("chimney_side_r");
		this.chimney_front_l = this.bellows_upper.getChild("chimney_front_l");
		this.chimney_mid_l = this.bellows_upper.getChild("chimney_mid_l");
		this.chimney_rear_r = this.bellows_upper.getChild("chimney_rear_r");
		this.chimney_rear_l = this.bellows_upper.getChild("chimney_rear_l");
		this.arm_left = this.carapace.getChild("arm_left");
		this.forearm_left = this.arm_left.getChild("forearm_left");
		this.claw_left = this.forearm_left.getChild("claw_left");
		this.claw_l_dactyl = this.claw_left.getChild("claw_l_dactyl");
		this.claw_l_pollex = this.claw_left.getChild("claw_l_pollex");
		this.arm_right = this.carapace.getChild("arm_right");
		this.forearm_right = this.arm_right.getChild("forearm_right");
		this.claw_right = this.forearm_right.getChild("claw_right");
		this.claw_r_dactyl = this.claw_right.getChild("claw_r_dactyl");
		this.claw_r_pollex = this.claw_right.getChild("claw_r_pollex");
		this.leg_l1 = this.body_center.getChild("leg_l1");
		this.leg_l1_tib = this.leg_l1.getChild("leg_l1_tib");
		this.leg_l1_tar = this.leg_l1_tib.getChild("leg_l1_tar");
		this.leg_l2 = this.body_center.getChild("leg_l2");
		this.leg_l2_tib = this.leg_l2.getChild("leg_l2_tib");
		this.leg_l2_tar = this.leg_l2_tib.getChild("leg_l2_tar");
		this.leg_l3 = this.body_center.getChild("leg_l3");
		this.leg_l3_tib = this.leg_l3.getChild("leg_l3_tib");
		this.leg_l3_tar = this.leg_l3_tib.getChild("leg_l3_tar");
		this.leg_r1 = this.body_center.getChild("leg_r1");
		this.leg_r1_tib = this.leg_r1.getChild("leg_r1_tib");
		this.leg_r1_tar = this.leg_r1_tib.getChild("leg_r1_tar");
		this.leg_r2 = this.body_center.getChild("leg_r2");
		this.leg_r2_tib = this.leg_r2.getChild("leg_r2_tib");
		this.leg_r2_tar = this.leg_r2_tib.getChild("leg_r2_tar");
		this.leg_r3 = this.body_center.getChild("leg_r3");
		this.leg_r3_tib = this.leg_r3.getChild("leg_r3_tib");
		this.leg_r3_tar = this.leg_r3_tib.getChild("leg_r3_tar");

		this.idleAnimation = VaporBellowsCrabAnimations.idle.bake(modelRoot);
		this.walkAnimation = VaporBellowsCrabAnimations.walk.bake(modelRoot);
		this.attackAnimation = VaporBellowsCrabAnimations.attack.bake(modelRoot);
		this.steamVentAnimation = VaporBellowsCrabAnimations.steam_vent.bake(modelRoot);
		this.braceDefenseAnimation = VaporBellowsCrabAnimations.brace_defense.bake(modelRoot);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body_center = root.addOrReplaceChild("body_center", CubeListBuilder.create(), PartPose.offset(0.0F, -6.5F, 0.0F));

		PartDefinition carapace = body_center.addOrReplaceChild("carapace", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -6.0F, -5.0F, 14.0F, 6.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(49, 0).addBox(-7.5F, -0.9F, -5.5F, 15.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(102, 0).addBox(-5.5F, -4.9F, 5.0F, 11.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 17).addBox(-5.5F, 0.0F, -4.0F, 11.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(39, 17).addBox(-2.5F, -0.3F, -5.6F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition eye_left = carapace.addOrReplaceChild("eye_left", CubeListBuilder.create().texOffs(68, 17).addBox(-1.0F, -1.0F, -0.75F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.12F)), PartPose.offset(-2.5F, -4.9F, -5.0F));

		PartDefinition eye_right = carapace.addOrReplaceChild("eye_right", CubeListBuilder.create().texOffs(75, 17).addBox(-1.0F, -1.0F, -0.75F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.12F)), PartPose.offset(2.5F, -4.9F, -5.0F));

		PartDefinition mandible_left = carapace.addOrReplaceChild("mandible_left", CubeListBuilder.create().texOffs(54, 17).addBox(-1.0F, -1.4F, -0.6F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.05F)), PartPose.offset(-1.25F, -2.2F, -5.0F));

		PartDefinition mandible_right = carapace.addOrReplaceChild("mandible_right", CubeListBuilder.create().texOffs(61, 17).addBox(-1.0F, -1.4F, -0.6F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.05F)), PartPose.offset(1.25F, -2.2F, -5.0F));

		PartDefinition bellows_lower = carapace.addOrReplaceChild("bellows_lower", CubeListBuilder.create().texOffs(82, 17).addBox(-6.5F, -2.0F, -4.5F, 13.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.7F, 0.0F));

		PartDefinition bellows_upper = bellows_lower.addOrReplaceChild("bellows_upper", CubeListBuilder.create().texOffs(0, 29).addBox(-5.0F, -2.0F, -3.5F, 10.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.8F, 0.0F));

		PartDefinition chimney_main = bellows_upper.addOrReplaceChild("chimney_main", CubeListBuilder.create().texOffs(35, 29).addBox(-2.0F, -5.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.1F)), PartPose.offset(1.4F, -1.6F, -1.6F));

		PartDefinition chimney_side_r = bellows_upper.addOrReplaceChild("chimney_side_r", CubeListBuilder.create().texOffs(52, 29).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.22F)), PartPose.offset(4.7F, 0.0F, -0.7F));

		PartDefinition chimney_front_l = bellows_upper.addOrReplaceChild("chimney_front_l", CubeListBuilder.create().texOffs(65, 29).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.12F)), PartPose.offset(-2.3F, -1.6F, -1.8F));

		PartDefinition chimney_mid_l = bellows_upper.addOrReplaceChild("chimney_mid_l", CubeListBuilder.create().texOffs(78, 29).addBox(-1.5F, -4.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.22F)), PartPose.offset(-4.6F, 0.0F, 0.9F));

		PartDefinition chimney_rear_r = bellows_upper.addOrReplaceChild("chimney_rear_r", CubeListBuilder.create().texOffs(91, 29).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.12F)), PartPose.offset(2.2F, -1.6F, 2.1F));

		PartDefinition chimney_rear_l = bellows_upper.addOrReplaceChild("chimney_rear_l", CubeListBuilder.create().texOffs(104, 29).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.18F)), PartPose.offset(-1.9F, -1.6F, 2.2F));

		PartDefinition arm_left = carapace.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(0, 39).addBox(-1.5F, -1.5F, -4.7F, 3.0F, 3.0F, 5.0F, new CubeDeformation(-0.18F)), PartPose.offsetAndRotation(-6.0F, -1.4F, -4.2F, -0.1745F, 0.8378F, -0.1047F));

		PartDefinition forearm_left = arm_left.addOrReplaceChild("forearm_left", CubeListBuilder.create().texOffs(17, 39).addBox(-1.5F, -1.5F, -3.7F, 3.0F, 3.0F, 4.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.0F, 0.0F, -4.4F, -0.1396F, -0.5934F, 0.0F));

		PartDefinition claw_left = forearm_left.addOrReplaceChild("claw_left", CubeListBuilder.create().texOffs(32, 39).addBox(-2.0F, -2.5F, -3.8F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.08F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.4F, 0.0F, -0.3142F, -0.1396F));

		PartDefinition claw_l_dactyl = claw_left.addOrReplaceChild("claw_l_dactyl", CubeListBuilder.create().texOffs(49, 39).addBox(-1.5F, -1.0F, -3.8F, 3.0F, 2.0F, 4.0F, new CubeDeformation(-0.18F)), PartPose.offsetAndRotation(0.0F, -1.6F, -3.8F, -0.192F, 0.0F, 0.0F));

		PartDefinition claw_l_pollex = claw_left.addOrReplaceChild("claw_l_pollex", CubeListBuilder.create().texOffs(64, 39).addBox(-1.5F, -1.0F, -3.8F, 3.0F, 2.0F, 4.0F, new CubeDeformation(-0.18F)), PartPose.offsetAndRotation(0.0F, 1.6F, -3.8F, 0.0873F, 0.0F, 0.0F));

		PartDefinition arm_right = carapace.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(79, 39).addBox(-1.5F, -1.5F, -4.7F, 3.0F, 3.0F, 5.0F, new CubeDeformation(-0.18F)), PartPose.offsetAndRotation(6.0F, -1.4F, -4.2F, -0.1745F, -0.8378F, 0.1047F));

		PartDefinition forearm_right = arm_right.addOrReplaceChild("forearm_right", CubeListBuilder.create().texOffs(96, 39).addBox(-1.5F, -1.5F, -3.7F, 3.0F, 3.0F, 4.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.0F, 0.0F, -4.4F, -0.1396F, 0.5934F, 0.0F));

		PartDefinition claw_right = forearm_right.addOrReplaceChild("claw_right", CubeListBuilder.create().texOffs(111, 39).addBox(-2.0F, -2.5F, -3.8F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.08F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.4F, 0.0F, 0.3142F, 0.1396F));

		PartDefinition claw_r_dactyl = claw_right.addOrReplaceChild("claw_r_dactyl", CubeListBuilder.create().texOffs(0, 49).addBox(-1.5F, -1.0F, -3.8F, 3.0F, 2.0F, 4.0F, new CubeDeformation(-0.18F)), PartPose.offsetAndRotation(0.0F, -1.6F, -3.8F, -0.192F, 0.0F, 0.0F));

		PartDefinition claw_r_pollex = claw_right.addOrReplaceChild("claw_r_pollex", CubeListBuilder.create().texOffs(15, 49).addBox(-1.5F, -1.0F, -3.8F, 3.0F, 2.0F, 4.0F, new CubeDeformation(-0.18F)), PartPose.offsetAndRotation(0.0F, 1.6F, -3.8F, 0.0873F, 0.0F, 0.0F));

		PartDefinition leg_l1 = body_center.addOrReplaceChild("leg_l1", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.2F, -0.1F, -2.8F, 0.0F, -0.384F, 0.0F));

		PartDefinition leg_l1_femur_r1 = leg_l1.addOrReplaceChild("leg_l1_femur_r1", CubeListBuilder.create().texOffs(30, 49).addBox(-3.7F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.384F));

		PartDefinition leg_l1_tib = leg_l1.addOrReplaceChild("leg_l1_tib", CubeListBuilder.create(), PartPose.offset(-3.4F, 1.4F, 0.0F));

		PartDefinition leg_l1_tibia_r1 = leg_l1_tib.addOrReplaceChild("leg_l1_tibia_r1", CubeListBuilder.create().texOffs(43, 49).addBox(-3.6F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.18F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.9774F));

		PartDefinition leg_l1_tar = leg_l1_tib.addOrReplaceChild("leg_l1_tar", CubeListBuilder.create().texOffs(56, 49).addBox(-0.5F, -0.48F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offset(-1.8F, 2.5F, 0.0F));

		PartDefinition leg_l2 = body_center.addOrReplaceChild("leg_l2", CubeListBuilder.create(), PartPose.offset(-6.2F, -0.1F, 0.2F));

		PartDefinition leg_l2_femur_r1 = leg_l2.addOrReplaceChild("leg_l2_femur_r1", CubeListBuilder.create().texOffs(61, 49).addBox(-3.7F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.384F));

		PartDefinition leg_l2_tib = leg_l2.addOrReplaceChild("leg_l2_tib", CubeListBuilder.create(), PartPose.offset(-3.4F, 1.4F, 0.0F));

		PartDefinition leg_l2_tibia_r1 = leg_l2_tib.addOrReplaceChild("leg_l2_tibia_r1", CubeListBuilder.create().texOffs(74, 49).addBox(-3.6F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.18F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.9774F));

		PartDefinition leg_l2_tar = leg_l2_tib.addOrReplaceChild("leg_l2_tar", CubeListBuilder.create().texOffs(87, 49).addBox(-0.5F, -0.48F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offset(-1.8F, 2.5F, 0.0F));

		PartDefinition leg_l3 = body_center.addOrReplaceChild("leg_l3", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.2F, -0.1F, 3.2F, 0.0F, 0.4189F, 0.0F));

		PartDefinition leg_l3_femur_r1 = leg_l3.addOrReplaceChild("leg_l3_femur_r1", CubeListBuilder.create().texOffs(92, 49).addBox(-3.7F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.384F));

		PartDefinition leg_l3_tib = leg_l3.addOrReplaceChild("leg_l3_tib", CubeListBuilder.create(), PartPose.offset(-3.4F, 1.4F, 0.0F));

		PartDefinition leg_l3_tibia_r1 = leg_l3_tib.addOrReplaceChild("leg_l3_tibia_r1", CubeListBuilder.create().texOffs(105, 49).addBox(-3.6F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.18F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.9774F));

		PartDefinition leg_l3_tar = leg_l3_tib.addOrReplaceChild("leg_l3_tar", CubeListBuilder.create().texOffs(118, 49).addBox(-0.5F, -0.48F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offset(-1.8F, 2.5F, 0.0F));

		PartDefinition leg_r1 = body_center.addOrReplaceChild("leg_r1", CubeListBuilder.create(), PartPose.offsetAndRotation(6.2F, -0.1F, -2.8F, 0.0F, 0.384F, 0.0F));

		PartDefinition leg_r1_femur_r1 = leg_r1.addOrReplaceChild("leg_r1_femur_r1", CubeListBuilder.create().texOffs(0, 56).addBox(-0.3F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.384F));

		PartDefinition leg_r1_tib = leg_r1.addOrReplaceChild("leg_r1_tib", CubeListBuilder.create(), PartPose.offset(3.4F, 1.4F, 0.0F));

		PartDefinition leg_r1_tibia_r1 = leg_r1_tib.addOrReplaceChild("leg_r1_tibia_r1", CubeListBuilder.create().texOffs(13, 56).addBox(-0.4F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.18F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9774F));

		PartDefinition leg_r1_tar = leg_r1_tib.addOrReplaceChild("leg_r1_tar", CubeListBuilder.create().texOffs(26, 56).addBox(-0.5F, -0.48F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offset(1.8F, 2.5F, 0.0F));

		PartDefinition leg_r2 = body_center.addOrReplaceChild("leg_r2", CubeListBuilder.create(), PartPose.offset(6.2F, -0.1F, 0.2F));

		PartDefinition leg_r2_femur_r1 = leg_r2.addOrReplaceChild("leg_r2_femur_r1", CubeListBuilder.create().texOffs(31, 56).addBox(-0.3F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.384F));

		PartDefinition leg_r2_tib = leg_r2.addOrReplaceChild("leg_r2_tib", CubeListBuilder.create(), PartPose.offset(3.4F, 1.4F, 0.0F));

		PartDefinition leg_r2_tibia_r1 = leg_r2_tib.addOrReplaceChild("leg_r2_tibia_r1", CubeListBuilder.create().texOffs(44, 56).addBox(-0.4F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.18F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9774F));

		PartDefinition leg_r2_tar = leg_r2_tib.addOrReplaceChild("leg_r2_tar", CubeListBuilder.create().texOffs(57, 56).addBox(-0.5F, -0.48F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offset(1.8F, 2.5F, 0.0F));

		PartDefinition leg_r3 = body_center.addOrReplaceChild("leg_r3", CubeListBuilder.create(), PartPose.offsetAndRotation(6.2F, -0.1F, 3.2F, 0.0F, -0.4189F, 0.0F));

		PartDefinition leg_r3_femur_r1 = leg_r3.addOrReplaceChild("leg_r3_femur_r1", CubeListBuilder.create().texOffs(62, 56).addBox(-0.3F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.384F));

		PartDefinition leg_r3_tib = leg_r3.addOrReplaceChild("leg_r3_tib", CubeListBuilder.create(), PartPose.offset(3.4F, 1.4F, 0.0F));

		PartDefinition leg_r3_tibia_r1 = leg_r3_tib.addOrReplaceChild("leg_r3_tibia_r1", CubeListBuilder.create().texOffs(75, 56).addBox(-0.4F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.18F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9774F));

		PartDefinition leg_r3_tar = leg_r3_tib.addOrReplaceChild("leg_r3_tar", CubeListBuilder.create().texOffs(88, 56).addBox(-0.5F, -0.48F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.18F)), PartPose.offset(1.8F, 2.5F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(VaporBellowsCrabRenderState state) {
		super.setupAnim(state);

		float clampedYaw = Mth.clamp(state.yRot, -30.0F, 30.0F) * Mth.DEG_TO_RAD;
		float clampedPitch = Mth.clamp(state.xRot, -20.0F, 20.0F) * Mth.DEG_TO_RAD;
		this.eye_left.yRot += clampedYaw * 0.4F;
		this.eye_right.yRot += clampedYaw * 0.4F;
		this.eye_left.xRot += clampedPitch * 0.3F;
		this.eye_right.xRot += clampedPitch * 0.3F;

		this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
		this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
		this.braceDefenseAnimation.apply(state.braceDefenseAnimationState, state.ageInTicks);
		this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
		this.steamVentAnimation.apply(state.steamVentAnimationState, state.ageInTicks);
	}
}
