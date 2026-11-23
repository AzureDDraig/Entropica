package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.CrystallineQilinAnimations;
import ddraig.net.entropica.client.renderer.CrystallineQilinRenderState;
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
 * Crystalline Qilin (Glow Guardian) — 2.10 Radiant Glaze-Plains.
 * Exact conversion of all 66 cubes, 28 bones, and 128x128 UVs from crystalline_qilin.bbmodel.
 */
public class CrystallineQilinModel extends EntityModel<CrystallineQilinRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "crystalline_qilin"), "main");
	public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

	private final ModelPart root;
	private final ModelPart body_grp;
	private final ModelPart chest_grp;
	private final ModelPart neck_grp;
	private final ModelPart head_grp;
	private final ModelPart jaw_grp;
	private final ModelPart ear_l_grp;
	private final ModelPart antler_l_grp;
	private final ModelPart antler_beam_l_grp;
	private final ModelPart ear_r_grp;
	private final ModelPart antler_r_grp;
	private final ModelPart antler_beam_r_grp;
	private final ModelPart leg_fl_grp;
	private final ModelPart knee_fl_grp;
	private final ModelPart hoof_fl_grp;
	private final ModelPart leg_fr_grp;
	private final ModelPart knee_fr_grp;
	private final ModelPart hoof_fr_grp;
	private final ModelPart scale_flank_l_grp;
	private final ModelPart scale_flank_r_grp;
	private final ModelPart leg_bl_grp;
	private final ModelPart hock_bl_grp;
	private final ModelPart hoof_bl_grp;
	private final ModelPart leg_br_grp;
	private final ModelPart hock_br_grp;
	private final ModelPart hoof_br_grp;
	private final ModelPart tail_1_grp;
	private final ModelPart tail_2_grp;

	private final KeyframeAnimation idleAnimation;
	private final KeyframeAnimation walkAnimation;
	private final KeyframeAnimation runAnimation;
	private final KeyframeAnimation attackAnimation;
	private final KeyframeAnimation lightRefractAnimation;
	private final KeyframeAnimation hurtAnimation;
	private final KeyframeAnimation deathAnimation;

	public CrystallineQilinModel(ModelPart modelRoot) {
		super(modelRoot);
		this.root = modelRoot.getChild("root");
		this.body_grp = this.root.getChild("body_grp");
		this.chest_grp = this.body_grp.getChild("chest_grp");
		this.neck_grp = this.chest_grp.getChild("neck_grp");
		this.head_grp = this.neck_grp.getChild("head_grp");
		this.jaw_grp = this.head_grp.getChild("jaw_grp");
		this.ear_l_grp = this.head_grp.getChild("ear_l_grp");
		this.antler_l_grp = this.head_grp.getChild("antler_l_grp");
		this.antler_beam_l_grp = this.antler_l_grp.getChild("antler_beam_l_grp");
		this.ear_r_grp = this.head_grp.getChild("ear_r_grp");
		this.antler_r_grp = this.head_grp.getChild("antler_r_grp");
		this.antler_beam_r_grp = this.antler_r_grp.getChild("antler_beam_r_grp");
		this.leg_fl_grp = this.chest_grp.getChild("leg_fl_grp");
		this.knee_fl_grp = this.leg_fl_grp.getChild("knee_fl_grp");
		this.hoof_fl_grp = this.knee_fl_grp.getChild("hoof_fl_grp");
		this.leg_fr_grp = this.chest_grp.getChild("leg_fr_grp");
		this.knee_fr_grp = this.leg_fr_grp.getChild("knee_fr_grp");
		this.hoof_fr_grp = this.knee_fr_grp.getChild("hoof_fr_grp");
		this.scale_flank_l_grp = this.body_grp.getChild("scale_flank_l_grp");
		this.scale_flank_r_grp = this.body_grp.getChild("scale_flank_r_grp");
		this.leg_bl_grp = this.body_grp.getChild("leg_bl_grp");
		this.hock_bl_grp = this.leg_bl_grp.getChild("hock_bl_grp");
		this.hoof_bl_grp = this.hock_bl_grp.getChild("hoof_bl_grp");
		this.leg_br_grp = this.body_grp.getChild("leg_br_grp");
		this.hock_br_grp = this.leg_br_grp.getChild("hock_br_grp");
		this.hoof_br_grp = this.hock_br_grp.getChild("hoof_br_grp");
		this.tail_1_grp = this.body_grp.getChild("tail_1_grp");
		this.tail_2_grp = this.tail_1_grp.getChild("tail_2_grp");

		this.idleAnimation = CrystallineQilinAnimations.IDLE.bake(modelRoot);
		this.walkAnimation = CrystallineQilinAnimations.WALK.bake(modelRoot);
		this.runAnimation = CrystallineQilinAnimations.RUN.bake(modelRoot);
		this.attackAnimation = CrystallineQilinAnimations.ATTACK.bake(modelRoot);
		this.lightRefractAnimation = CrystallineQilinAnimations.LIGHT_REFRACT.bake(modelRoot);
		this.hurtAnimation = CrystallineQilinAnimations.HURT.bake(modelRoot);
		this.deathAnimation = CrystallineQilinAnimations.DEATH.bake(modelRoot);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body_grp = root.addOrReplaceChild("body_grp", CubeListBuilder.create().texOffs(0, 0).addBox(-6.5F, -7.5F, -7.5F, 13.0F, 14.0F, 17.0F, new CubeDeformation(-3.02F)), PartPose.offset(0.0F, -16.5F, 1.0F));

		PartDefinition chest_grp = body_grp.addOrReplaceChild("chest_grp", CubeListBuilder.create().texOffs(61, 0).addBox(-6.0F, -7.1F, -7.8F, 12.0F, 14.0F, 11.0F, new CubeDeformation(-2.5F))
				.texOffs(46, 99).addBox(-3.5F, -3.3F, -7.5F, 7.0F, 7.0F, 4.0F, new CubeDeformation(-1.5F)), PartPose.offset(0.0F, -0.5F, -4.5F));

		PartDefinition neck_grp = chest_grp.addOrReplaceChild("neck_grp", CubeListBuilder.create().texOffs(0, 32).addBox(-5.0F, -10.5F, -5.0F, 10.0F, 14.0F, 10.0F, new CubeDeformation(-2.51F))
				.texOffs(97, 0).addBox(-3.0F, -7.1F, -4.7F, 6.0F, 6.0F, 4.0F, new CubeDeformation(-1.5F))
				.texOffs(44, 0).addBox(-3.5F, -4.3F, -4.9F, 7.0F, 6.0F, 4.0F, new CubeDeformation(-1.49F)), PartPose.offsetAndRotation(0.0F, -3.2F, -2.7F, 0.2443F, 0.0F, 0.0F));

		PartDefinition head_grp = neck_grp.addOrReplaceChild("head_grp", CubeListBuilder.create().texOffs(50, 26).addBox(-5.5F, -7.7F, -6.3F, 11.0F, 11.0F, 11.0F, new CubeDeformation(-2.5F))
				.texOffs(68, 60).addBox(-4.0F, -4.6F, -9.8F, 8.0F, 6.0F, 8.0F, new CubeDeformation(-2.0F)), PartPose.offsetAndRotation(0.0F, -7.2F, 0.0F, -0.2443F, 0.0F, 0.0F));

		PartDefinition jaw_grp = head_grp.addOrReplaceChild("jaw_grp", CubeListBuilder.create().texOffs(0, 79).addBox(-3.5F, -1.5F, -5.5F, 7.0F, 4.0F, 7.0F, new CubeDeformation(-1.51F)), PartPose.offset(0.0F, -0.6F, -3.8F));

		PartDefinition ear_l_grp = head_grp.addOrReplaceChild("ear_l_grp", CubeListBuilder.create().texOffs(58, 49).addBox(-4.3F, -2.5F, -2.0F, 6.0F, 5.0F, 4.0F, new CubeDeformation(-1.5F)), PartPose.offsetAndRotation(-2.8F, -3.8F, 1.0F, 0.0F, 0.3491F, 0.3142F));

		PartDefinition antler_l_grp = head_grp.addOrReplaceChild("antler_l_grp", CubeListBuilder.create(), PartPose.offset(-1.5F, -5.0F, -1.4F));
		antler_l_grp.addOrReplaceChild("antler_pedicle_l_r1", CubeListBuilder.create().texOffs(31, 32).addBox(-2.0F, -3.5F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-1.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

		PartDefinition antler_beam_l_grp = antler_l_grp.addOrReplaceChild("antler_beam_l_grp", CubeListBuilder.create().texOffs(29, 86).addBox(-2.0F, -12.5F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(-1.3F)), PartPose.offsetAndRotation(0.0F, -1.6F, 0.0F, -0.6632F, 0.0F, -0.5585F));
		antler_beam_l_grp.addOrReplaceChild("antler_tine1_l_r1", CubeListBuilder.create().texOffs(109, 103).addBox(-2.0F, -4.4F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-1.41F)), PartPose.offsetAndRotation(0.0F, -1.8F, -0.2F, 0.5585F, 0.0F, 0.384F));
		antler_beam_l_grp.addOrReplaceChild("antler_tine2_l_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.4F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(-1.41F)), PartPose.offsetAndRotation(0.0F, -4.4F, -0.2F, 0.5236F, 0.0F, 0.3491F));
		antler_beam_l_grp.addOrReplaceChild("antler_tine3_l_r1", CubeListBuilder.create().texOffs(112, 34).addBox(-2.0F, -5.4F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-1.41F)), PartPose.offsetAndRotation(0.0F, -7.4F, -0.2F, 0.4538F, 0.0F, 0.2793F));

		PartDefinition ear_r_grp = head_grp.addOrReplaceChild("ear_r_grp", CubeListBuilder.create().texOffs(58, 49).mirror().addBox(-1.7F, -2.5F, -2.0F, 6.0F, 5.0F, 4.0F, new CubeDeformation(-1.5F)).mirror(false), PartPose.offsetAndRotation(2.8F, -3.8F, 1.0F, 0.0F, -0.3491F, -0.3142F));

		PartDefinition antler_r_grp = head_grp.addOrReplaceChild("antler_r_grp", CubeListBuilder.create(), PartPose.offset(1.5F, -5.0F, -1.4F));
		antler_r_grp.addOrReplaceChild("antler_pedicle_r_r1", CubeListBuilder.create().texOffs(31, 32).mirror().addBox(-2.0F, -3.5F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-1.2F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

		PartDefinition antler_beam_r_grp = antler_r_grp.addOrReplaceChild("antler_beam_r_grp", CubeListBuilder.create().texOffs(29, 86).mirror().addBox(-2.0F, -12.5F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(-1.3F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.6F, 0.0F, -0.6632F, 0.0F, 0.5585F));
		antler_beam_r_grp.addOrReplaceChild("antler_tine1_r_r1", CubeListBuilder.create().texOffs(109, 103).mirror().addBox(-2.0F, -4.4F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-1.41F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.8F, -0.2F, 0.5585F, 0.0F, -0.384F));
		antler_beam_r_grp.addOrReplaceChild("antler_tine2_r_r1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.0F, -6.4F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(-1.41F)).mirror(false), PartPose.offsetAndRotation(0.0F, -4.4F, -0.2F, 0.5236F, 0.0F, -0.3491F));
		antler_beam_r_grp.addOrReplaceChild("antler_tine3_r_r1", CubeListBuilder.create().texOffs(112, 34).mirror().addBox(-2.0F, -5.4F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-1.41F)).mirror(false), PartPose.offsetAndRotation(0.0F, -7.4F, -0.2F, 0.4538F, 0.0F, -0.2793F));

		PartDefinition leg_fl_grp = chest_grp.addOrReplaceChild("leg_fl_grp", CubeListBuilder.create().texOffs(85, 39).addBox(-4.3F, -4.8F, -5.0F, 8.0F, 10.0F, 10.0F, new CubeDeformation(-2.49F))
				.texOffs(23, 70).addBox(-3.0F, 0.4F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(-1.7F)), PartPose.offset(-3.4F, 2.5F, -2.3F));

		PartDefinition knee_fl_grp = leg_fl_grp.addOrReplaceChild("knee_fl_grp", CubeListBuilder.create().texOffs(48, 70).addBox(-3.0F, -1.9F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(-1.91F)), PartPose.offset(0.0F, 7.5F, 0.0F));
		knee_fl_grp.addOrReplaceChild("spur_fl_1_r1", CubeListBuilder.create().texOffs(115, 8).addBox(-1.5F, -3.7F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.1F)), PartPose.offsetAndRotation(-0.6F, 3.0F, 1.0F, -0.6632F, 0.0F, -0.2793F));
		knee_fl_grp.addOrReplaceChild("spur_fl_2_r1", CubeListBuilder.create().texOffs(115, 8).addBox(-1.5F, -3.7F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.11F)), PartPose.offsetAndRotation(-0.6F, 4.7F, 1.0F, -0.733F, 0.0F, -0.2793F));

		PartDefinition hoof_fl_grp = knee_fl_grp.addOrReplaceChild("hoof_fl_grp", CubeListBuilder.create().texOffs(94, 91).addBox(-3.0F, -1.5F, -3.2F, 6.0F, 5.0F, 6.0F, new CubeDeformation(-1.5F)), PartPose.offset(0.0F, 5.0F, 0.0F));

		PartDefinition leg_fr_grp = chest_grp.addOrReplaceChild("leg_fr_grp", CubeListBuilder.create().texOffs(85, 39).mirror().addBox(-3.7F, -4.8F, -5.0F, 8.0F, 10.0F, 10.0F, new CubeDeformation(-2.49F)).mirror(false)
				.texOffs(23, 70).mirror().addBox(-3.0F, 0.4F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(-1.7F)).mirror(false), PartPose.offset(3.4F, 2.5F, -2.3F));

		PartDefinition knee_fr_grp = leg_fr_grp.addOrReplaceChild("knee_fr_grp", CubeListBuilder.create().texOffs(48, 70).mirror().addBox(-3.0F, -1.9F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(-1.91F)).mirror(false), PartPose.offset(0.0F, 7.5F, 0.0F));
		knee_fr_grp.addOrReplaceChild("spur_fr_1_r1", CubeListBuilder.create().texOffs(115, 8).mirror().addBox(-1.5F, -3.7F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.1F)).mirror(false), PartPose.offsetAndRotation(0.6F, 3.0F, 1.0F, -0.6632F, 0.0F, 0.2793F));
		knee_fr_grp.addOrReplaceChild("spur_fr_2_r1", CubeListBuilder.create().texOffs(115, 8).mirror().addBox(-1.5F, -3.7F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.11F)).mirror(false), PartPose.offsetAndRotation(0.6F, 4.7F, 1.0F, -0.733F, 0.0F, 0.2793F));

		PartDefinition hoof_fr_grp = knee_fr_grp.addOrReplaceChild("hoof_fr_grp", CubeListBuilder.create().texOffs(94, 91).mirror().addBox(-3.0F, -1.5F, -3.2F, 6.0F, 5.0F, 6.0F, new CubeDeformation(-1.5F)).mirror(false), PartPose.offset(0.0F, 5.0F, 0.0F));

		PartDefinition scale_flank_l_grp = body_grp.addOrReplaceChild("scale_flank_l_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.5F, -4.0F, -1.0F, 0.0F, 0.0F, 0.2094F));
		scale_flank_l_grp.addOrReplaceChild("scale_u_l_1_r1", CubeListBuilder.create().texOffs(99, 17).addBox(-2.6F, -2.0F, -3.0F, 5.0F, 7.0F, 9.0F, new CubeDeformation(-2.0F)), PartPose.offsetAndRotation(-0.1F, -1.0F, -6.9F, -0.2443F, 0.0F, 0.0F));
		scale_flank_l_grp.addOrReplaceChild("scale_u_l_2_r1", CubeListBuilder.create().texOffs(99, 17).addBox(-2.68F, -2.0F, -3.0F, 5.0F, 7.0F, 9.0F, new CubeDeformation(-2.01F)), PartPose.offsetAndRotation(-0.1F, -0.65F, -3.7F, -0.2443F, 0.0F, 0.0F));
		scale_flank_l_grp.addOrReplaceChild("scale_u_l_3_r1", CubeListBuilder.create().texOffs(99, 17).addBox(-2.76F, -2.0F, -3.0F, 5.0F, 7.0F, 9.0F, new CubeDeformation(-2.02F)), PartPose.offsetAndRotation(-0.1F, -0.3F, -0.5F, -0.2443F, 0.0F, 0.0F));
		scale_flank_l_grp.addOrReplaceChild("scale_u_l_4_r1", CubeListBuilder.create().texOffs(99, 17).addBox(-2.84F, -2.0F, -3.0F, 5.0F, 7.0F, 9.0F, new CubeDeformation(-2.03F)), PartPose.offsetAndRotation(-0.1F, 0.05F, 2.7F, -0.2443F, 0.0F, 0.0F));
		scale_flank_l_grp.addOrReplaceChild("scale_l_l_1_r1", CubeListBuilder.create().texOffs(101, 60).addBox(-2.65F, -2.0F, -3.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(-2.02F)), PartPose.offsetAndRotation(-0.2F, 1.5F, -4.8F, -0.2094F, 0.0F, 0.0F));
		scale_flank_l_grp.addOrReplaceChild("scale_l_l_2_r1", CubeListBuilder.create().texOffs(101, 60).addBox(-2.73F, -2.0F, -3.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(-2.03F)), PartPose.offsetAndRotation(-0.2F, 1.8F, -1.6F, -0.2094F, 0.0F, 0.0F));
		scale_flank_l_grp.addOrReplaceChild("scale_l_l_3_r1", CubeListBuilder.create().texOffs(101, 60).addBox(-2.81F, -2.0F, -3.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(-2.04F)), PartPose.offsetAndRotation(-0.2F, 2.1F, 1.6F, -0.2094F, 0.0F, 0.0F));

		PartDefinition scale_flank_r_grp = body_grp.addOrReplaceChild("scale_flank_r_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(3.5F, -4.0F, -1.0F, 0.0F, 0.0F, -0.2094F));
		scale_flank_r_grp.addOrReplaceChild("scale_u_r_1_r1", CubeListBuilder.create().texOffs(99, 17).mirror().addBox(-2.4F, -2.0F, -3.0F, 5.0F, 7.0F, 9.0F, new CubeDeformation(-2.0F)).mirror(false), PartPose.offsetAndRotation(0.1F, -1.0F, -6.9F, -0.2443F, 0.0F, 0.0F));
		scale_flank_r_grp.addOrReplaceChild("scale_u_r_2_r1", CubeListBuilder.create().texOffs(99, 17).mirror().addBox(-2.32F, -2.0F, -3.0F, 5.0F, 7.0F, 9.0F, new CubeDeformation(-2.01F)).mirror(false), PartPose.offsetAndRotation(0.1F, -0.65F, -3.7F, -0.2443F, 0.0F, 0.0F));
		scale_flank_r_grp.addOrReplaceChild("scale_u_r_3_r1", CubeListBuilder.create().texOffs(99, 17).mirror().addBox(-2.24F, -2.0F, -3.0F, 5.0F, 7.0F, 9.0F, new CubeDeformation(-2.02F)).mirror(false), PartPose.offsetAndRotation(0.1F, -0.3F, -0.5F, -0.2443F, 0.0F, 0.0F));
		scale_flank_r_grp.addOrReplaceChild("scale_u_r_4_r1", CubeListBuilder.create().texOffs(99, 17).mirror().addBox(-2.16F, -2.0F, -3.0F, 5.0F, 7.0F, 9.0F, new CubeDeformation(-2.03F)).mirror(false), PartPose.offsetAndRotation(0.1F, 0.05F, 2.7F, -0.2443F, 0.0F, 0.0F));
		scale_flank_r_grp.addOrReplaceChild("scale_l_r_1_r1", CubeListBuilder.create().texOffs(101, 60).mirror().addBox(-2.35F, -2.0F, -3.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(-2.02F)).mirror(false), PartPose.offsetAndRotation(0.2F, 1.5F, -4.8F, -0.2094F, 0.0F, 0.0F));
		scale_flank_r_grp.addOrReplaceChild("scale_l_r_2_r1", CubeListBuilder.create().texOffs(101, 60).mirror().addBox(-2.27F, -2.0F, -3.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(-2.03F)).mirror(false), PartPose.offsetAndRotation(0.2F, 1.8F, -1.6F, -0.2094F, 0.0F, 0.0F));
		scale_flank_r_grp.addOrReplaceChild("scale_l_r_3_r1", CubeListBuilder.create().texOffs(101, 60).mirror().addBox(-2.19F, -2.0F, -3.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(-2.04F)).mirror(false), PartPose.offsetAndRotation(0.2F, 2.1F, 1.6F, -0.2094F, 0.0F, 0.0F));

		PartDefinition leg_bl_grp = body_grp.addOrReplaceChild("leg_bl_grp", CubeListBuilder.create().texOffs(31, 49).addBox(-4.3F, -5.0F, -4.9F, 8.0F, 10.0F, 10.0F, new CubeDeformation(-2.49F)), PartPose.offset(-3.4F, 2.0F, 4.2F));
		leg_bl_grp.addOrReplaceChild("thigh_bl_r1", CubeListBuilder.create().texOffs(73, 75).addBox(-3.0F, -1.7F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(-1.7F)), PartPose.offsetAndRotation(0.0F, 2.1F, 0.0F, 0.4538F, 0.0F, 0.0F));

		PartDefinition hock_bl_grp = leg_bl_grp.addOrReplaceChild("hock_bl_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 7.4F, 2.0F));
		hock_bl_grp.addOrReplaceChild("shin_bl_r1", CubeListBuilder.create().texOffs(98, 75).addBox(-3.0F, -1.9F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(-1.91F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));
		hock_bl_grp.addOrReplaceChild("spur_bl_1_r1", CubeListBuilder.create().texOffs(115, 8).addBox(-1.5F, -3.7F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.1F)), PartPose.offsetAndRotation(-0.6F, 3.1F, 0.6F, -0.6632F, 0.0F, -0.2793F));
		hock_bl_grp.addOrReplaceChild("spur_bl_2_r1", CubeListBuilder.create().texOffs(115, 8).addBox(-1.5F, -3.7F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.11F)), PartPose.offsetAndRotation(-0.6F, 4.8F, 0.4F, -0.733F, 0.0F, -0.2793F));

		PartDefinition hoof_bl_grp = hock_bl_grp.addOrReplaceChild("hoof_bl_grp", CubeListBuilder.create().texOffs(94, 91).addBox(-3.0F, -1.5F, -3.2F, 6.0F, 5.0F, 6.0F, new CubeDeformation(-1.5F)), PartPose.offset(0.0F, 5.1F, -0.7F));

		PartDefinition leg_br_grp = body_grp.addOrReplaceChild("leg_br_grp", CubeListBuilder.create().texOffs(31, 49).mirror().addBox(-3.7F, -5.0F, -4.9F, 8.0F, 10.0F, 10.0F, new CubeDeformation(-2.49F)).mirror(false), PartPose.offset(3.4F, 2.0F, 4.2F));
		leg_br_grp.addOrReplaceChild("thigh_br_r1", CubeListBuilder.create().texOffs(73, 75).mirror().addBox(-3.0F, -1.7F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(-1.7F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.1F, 0.0F, 0.4538F, 0.0F, 0.0F));

		PartDefinition hock_br_grp = leg_br_grp.addOrReplaceChild("hock_br_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 7.4F, 2.0F));
		hock_br_grp.addOrReplaceChild("shin_br_r1", CubeListBuilder.create().texOffs(98, 75).mirror().addBox(-3.0F, -1.9F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(-1.91F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));
		hock_br_grp.addOrReplaceChild("spur_br_1_r1", CubeListBuilder.create().texOffs(115, 8).mirror().addBox(-1.5F, -3.7F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.1F)).mirror(false), PartPose.offsetAndRotation(0.6F, 3.1F, 0.6F, -0.6632F, 0.0F, 0.2793F));
		hock_br_grp.addOrReplaceChild("spur_br_2_r1", CubeListBuilder.create().texOffs(115, 8).mirror().addBox(-1.5F, -3.7F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.11F)).mirror(false), PartPose.offsetAndRotation(0.6F, 4.8F, 0.4F, -0.733F, 0.0F, 0.2793F));

		PartDefinition hoof_br_grp = hock_br_grp.addOrReplaceChild("hoof_br_grp", CubeListBuilder.create().texOffs(94, 91).mirror().addBox(-3.0F, -1.5F, -3.2F, 6.0F, 5.0F, 6.0F, new CubeDeformation(-1.5F)).mirror(false), PartPose.offset(0.0F, 5.1F, -0.7F));

		PartDefinition tail_1_grp = body_grp.addOrReplaceChild("tail_1_grp", CubeListBuilder.create().texOffs(0, 57).addBox(-3.0F, -3.0F, -1.7F, 6.0F, 6.0F, 8.0F, new CubeDeformation(-1.5F)), PartPose.offsetAndRotation(0.0F, -3.1F, 6.4F, -0.5934F, 0.0F, 0.0F));
		tail_1_grp.addOrReplaceChild("tail_crest_r1", CubeListBuilder.create().texOffs(46, 86).addBox(-2.5F, -3.0F, -1.9F, 5.0F, 5.0F, 7.0F, new CubeDeformation(-1.51F)), PartPose.offsetAndRotation(0.0F, -1.2F, 0.8F, 0.1745F, 0.0F, 0.0F));
		tail_1_grp.addOrReplaceChild("tail_frond_l_r1", CubeListBuilder.create().texOffs(71, 91).addBox(-2.0F, -2.5F, -1.7F, 4.0F, 5.0F, 7.0F, new CubeDeformation(-1.51F)), PartPose.offsetAndRotation(-1.5F, 0.2F, 0.8F, 0.0F, -0.3142F, 0.0F));
		tail_1_grp.addOrReplaceChild("tail_frond_r_r1", CubeListBuilder.create().texOffs(71, 91).mirror().addBox(-2.0F, -2.5F, -1.7F, 4.0F, 5.0F, 7.0F, new CubeDeformation(-1.51F)).mirror(false), PartPose.offsetAndRotation(1.5F, 0.2F, 0.8F, 0.0F, 0.3142F, 0.0F));

		PartDefinition tail_2_grp = tail_1_grp.addOrReplaceChild("tail_2_grp", CubeListBuilder.create().texOffs(0, 91).addBox(-2.5F, -2.5F, -1.8F, 5.0F, 5.0F, 7.0F, new CubeDeformation(-1.52F))
				.texOffs(88, 103).addBox(-2.0F, -2.0F, 1.7F, 4.0F, 4.0F, 6.0F, new CubeDeformation(-1.5F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.4F, -0.1047F, 0.0F, 0.0F));
		tail_2_grp.addOrReplaceChild("tail_under_frond_r1", CubeListBuilder.create().texOffs(46, 86).addBox(-2.5F, -2.1F, -1.7F, 5.0F, 5.0F, 7.0F, new CubeDeformation(-1.53F)), PartPose.offsetAndRotation(0.0F, 0.8F, -1.0F, -0.2793F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(CrystallineQilinRenderState state) {
		super.setupAnim(state);

		if (!state.deathAnimationState.isStarted()
				&& !state.attackAnimationState.isStarted()
				&& !state.lightRefractAnimationState.isStarted()) {
			float clampedYaw = Mth.clamp(state.yRot, -45.0F, 45.0F) * Mth.DEG_TO_RAD;
			float clampedPitch = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
			this.neck_grp.yRot += clampedYaw * 0.35F;
			this.head_grp.yRot += clampedYaw * 0.65F;
			this.head_grp.xRot += clampedPitch * 0.65F;
		}

		if (state.deathAnimationState.isStarted()) {
			this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
		} else if (state.attackAnimationState.isStarted()) {
			this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
		} else if (state.lightRefractAnimationState.isStarted()) {
			this.lightRefractAnimation.apply(state.lightRefractAnimationState, state.ageInTicks);
		} else if (state.hurtAnimationState.isStarted()) {
			this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);
		} else if (state.runAnimationState.isStarted()) {
			this.runAnimation.apply(state.runAnimationState, state.ageInTicks);
		} else if (state.walkAnimationState.isStarted()) {
			this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
		} else {
			this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
		}
	}
}
