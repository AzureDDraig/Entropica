package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SporeDrifterModel extends EntityModel<SporeDrifterRenderState> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_drifter"), "main");
	private final ModelPart bell;
	private final ModelPart bell_side_n;
	private final ModelPart bell_flap_n_1;
	private final ModelPart bell_flap_n_2;
	private final ModelPart bell_flap_n_3;
	private final ModelPart bell_flap_n_4;
	private final ModelPart bell_side_ne;
	private final ModelPart bell_flap_ne_1;
	private final ModelPart bell_flap_ne_2;
	private final ModelPart bell_flap_ne_3;
	private final ModelPart bell_flap_ne_4;
	private final ModelPart bell_side_e;
	private final ModelPart bell_flap_e_1;
	private final ModelPart bell_flap_e_2;
	private final ModelPart bell_flap_e_3;
	private final ModelPart bell_flap_e_4;
	private final ModelPart bell_side_se;
	private final ModelPart bell_flap_se_1;
	private final ModelPart bell_flap_se_2;
	private final ModelPart bell_flap_se_3;
	private final ModelPart bell_flap_se_4;
	private final ModelPart bell_side_s;
	private final ModelPart bell_flap_s_1;
	private final ModelPart bell_flap_s_2;
	private final ModelPart bell_flap_s_3;
	private final ModelPart bell_flap_s_4;
	private final ModelPart bell_side_sw;
	private final ModelPart bell_flap_sw_1;
	private final ModelPart bell_flap_sw_2;
	private final ModelPart bell_flap_sw_3;
	private final ModelPart bell_flap_sw_4;
	private final ModelPart bell_side_w;
	private final ModelPart bell_flap_w_1;
	private final ModelPart bell_flap_w_2;
	private final ModelPart bell_flap_w_3;
	private final ModelPart bell_flap_w_4;
	private final ModelPart bell_side_nw;
	private final ModelPart bell_flap_nw_1;
	private final ModelPart bell_flap_nw_2;
	private final ModelPart bell_flap_nw_3;
	private final ModelPart bell_flap_nw_4;
	private final ModelPart inner_organs;
	private final ModelPart organ_center;
	private final ModelPart organ_lobe_1;
	private final ModelPart organ_lobe_2;
	private final ModelPart organ_lobe_3;
	private final ModelPart organ_lobe_4;
	private final ModelPart tendril_fl_seg1;
	private final ModelPart tendril_fl_seg2;
	private final ModelPart tendril_fl_seg3;
	private final ModelPart tendril_fl_seg4;
	private final ModelPart tendril_fl_seg5;
	private final ModelPart tendril_fl_seg6;
	private final ModelPart tendril_fl_seg7;
	private final ModelPart tendril_fl_seg8;
	private final ModelPart spore_sac_tendril_fl;
	private final ModelPart tendril_fr_seg1;
	private final ModelPart tendril_fr_seg2;
	private final ModelPart tendril_fr_seg3;
	private final ModelPart tendril_fr_seg4;
	private final ModelPart tendril_fr_seg5;
	private final ModelPart tendril_fr_seg6;
	private final ModelPart tendril_fr_seg7;
	private final ModelPart tendril_fr_seg8;
	private final ModelPart spore_sac_tendril_fr;
	private final ModelPart tendril_bl_seg1;
	private final ModelPart tendril_bl_seg2;
	private final ModelPart tendril_bl_seg3;
	private final ModelPart tendril_bl_seg4;
	private final ModelPart tendril_bl_seg5;
	private final ModelPart tendril_bl_seg6;
	private final ModelPart tendril_bl_seg7;
	private final ModelPart tendril_bl_seg8;
	private final ModelPart spore_sac_tendril_bl;
	private final ModelPart tendril_br_seg1;
	private final ModelPart tendril_br_seg2;
	private final ModelPart tendril_br_seg3;
	private final ModelPart tendril_br_seg4;
	private final ModelPart tendril_br_seg5;
	private final ModelPart tendril_br_seg6;
	private final ModelPart tendril_br_seg7;
	private final ModelPart tendril_br_seg8;
	private final ModelPart spore_sac_tendril_br;
	private final ModelPart tendril_ifl_seg1;
	private final ModelPart tendril_ifl_seg2;
	private final ModelPart tendril_ifl_seg3;
	private final ModelPart tendril_ifl_seg4;
	private final ModelPart spore_sac_tendril_ifl;
	private final ModelPart tendril_ifr_seg1;
	private final ModelPart tendril_ifr_seg2;
	private final ModelPart tendril_ifr_seg3;
	private final ModelPart tendril_ifr_seg4;
	private final ModelPart spore_sac_tendril_ifr;
	private final ModelPart tendril_ibl_seg1;
	private final ModelPart tendril_ibl_seg2;
	private final ModelPart tendril_ibl_seg3;
	private final ModelPart tendril_ibl_seg4;
	private final ModelPart spore_sac_tendril_ibl;
	private final ModelPart tendril_ibr_seg1;
	private final ModelPart tendril_ibr_seg2;
	private final ModelPart tendril_ibr_seg3;
	private final ModelPart tendril_ibr_seg4;
	private final ModelPart spore_sac_tendril_ibr;

	public SporeDrifterModel(ModelPart root) {
		super(root);
		this.bell = root.getChild("bell");
		this.bell_side_n = this.bell.getChild("bell_side_n");
		this.bell_flap_n_1 = this.bell_side_n.getChild("bell_flap_n_1");
		this.bell_flap_n_2 = this.bell_flap_n_1.getChild("bell_flap_n_2");
		this.bell_flap_n_3 = this.bell_flap_n_2.getChild("bell_flap_n_3");
		this.bell_flap_n_4 = this.bell_flap_n_3.getChild("bell_flap_n_4");
		this.bell_side_ne = this.bell.getChild("bell_side_ne");
		this.bell_flap_ne_1 = this.bell_side_ne.getChild("bell_flap_ne_1");
		this.bell_flap_ne_2 = this.bell_flap_ne_1.getChild("bell_flap_ne_2");
		this.bell_flap_ne_3 = this.bell_flap_ne_2.getChild("bell_flap_ne_3");
		this.bell_flap_ne_4 = this.bell_flap_ne_3.getChild("bell_flap_ne_4");
		this.bell_side_e = this.bell.getChild("bell_side_e");
		this.bell_flap_e_1 = this.bell_side_e.getChild("bell_flap_e_1");
		this.bell_flap_e_2 = this.bell_flap_e_1.getChild("bell_flap_e_2");
		this.bell_flap_e_3 = this.bell_flap_e_2.getChild("bell_flap_e_3");
		this.bell_flap_e_4 = this.bell_flap_e_3.getChild("bell_flap_e_4");
		this.bell_side_se = this.bell.getChild("bell_side_se");
		this.bell_flap_se_1 = this.bell_side_se.getChild("bell_flap_se_1");
		this.bell_flap_se_2 = this.bell_flap_se_1.getChild("bell_flap_se_2");
		this.bell_flap_se_3 = this.bell_flap_se_2.getChild("bell_flap_se_3");
		this.bell_flap_se_4 = this.bell_flap_se_3.getChild("bell_flap_se_4");
		this.bell_side_s = this.bell.getChild("bell_side_s");
		this.bell_flap_s_1 = this.bell_side_s.getChild("bell_flap_s_1");
		this.bell_flap_s_2 = this.bell_flap_s_1.getChild("bell_flap_s_2");
		this.bell_flap_s_3 = this.bell_flap_s_2.getChild("bell_flap_s_3");
		this.bell_flap_s_4 = this.bell_flap_s_3.getChild("bell_flap_s_4");
		this.bell_side_sw = this.bell.getChild("bell_side_sw");
		this.bell_flap_sw_1 = this.bell_side_sw.getChild("bell_flap_sw_1");
		this.bell_flap_sw_2 = this.bell_flap_sw_1.getChild("bell_flap_sw_2");
		this.bell_flap_sw_3 = this.bell_flap_sw_2.getChild("bell_flap_sw_3");
		this.bell_flap_sw_4 = this.bell_flap_sw_3.getChild("bell_flap_sw_4");
		this.bell_side_w = this.bell.getChild("bell_side_w");
		this.bell_flap_w_1 = this.bell_side_w.getChild("bell_flap_w_1");
		this.bell_flap_w_2 = this.bell_flap_w_1.getChild("bell_flap_w_2");
		this.bell_flap_w_3 = this.bell_flap_w_2.getChild("bell_flap_w_3");
		this.bell_flap_w_4 = this.bell_flap_w_3.getChild("bell_flap_w_4");
		this.bell_side_nw = this.bell.getChild("bell_side_nw");
		this.bell_flap_nw_1 = this.bell_side_nw.getChild("bell_flap_nw_1");
		this.bell_flap_nw_2 = this.bell_flap_nw_1.getChild("bell_flap_nw_2");
		this.bell_flap_nw_3 = this.bell_flap_nw_2.getChild("bell_flap_nw_3");
		this.bell_flap_nw_4 = this.bell_flap_nw_3.getChild("bell_flap_nw_4");
		this.inner_organs = this.bell.getChild("inner_organs");
		this.organ_center = this.inner_organs.getChild("organ_center");
		this.organ_lobe_1 = this.inner_organs.getChild("organ_lobe_1");
		this.organ_lobe_2 = this.inner_organs.getChild("organ_lobe_2");
		this.organ_lobe_3 = this.inner_organs.getChild("organ_lobe_3");
		this.organ_lobe_4 = this.inner_organs.getChild("organ_lobe_4");
		this.tendril_fl_seg1 = this.bell.getChild("tendril_fl_seg1");
		this.tendril_fl_seg2 = this.tendril_fl_seg1.getChild("tendril_fl_seg2");
		this.tendril_fl_seg3 = this.tendril_fl_seg2.getChild("tendril_fl_seg3");
		this.tendril_fl_seg4 = this.tendril_fl_seg3.getChild("tendril_fl_seg4");
		this.tendril_fl_seg5 = this.tendril_fl_seg4.getChild("tendril_fl_seg5");
		this.tendril_fl_seg6 = this.tendril_fl_seg5.getChild("tendril_fl_seg6");
		this.tendril_fl_seg7 = this.tendril_fl_seg6.getChild("tendril_fl_seg7");
		this.tendril_fl_seg8 = this.tendril_fl_seg7.getChild("tendril_fl_seg8");
		this.spore_sac_tendril_fl = this.tendril_fl_seg8.getChild("spore_sac_tendril_fl");
		this.tendril_fr_seg1 = this.bell.getChild("tendril_fr_seg1");
		this.tendril_fr_seg2 = this.tendril_fr_seg1.getChild("tendril_fr_seg2");
		this.tendril_fr_seg3 = this.tendril_fr_seg2.getChild("tendril_fr_seg3");
		this.tendril_fr_seg4 = this.tendril_fr_seg3.getChild("tendril_fr_seg4");
		this.tendril_fr_seg5 = this.tendril_fr_seg4.getChild("tendril_fr_seg5");
		this.tendril_fr_seg6 = this.tendril_fr_seg5.getChild("tendril_fr_seg6");
		this.tendril_fr_seg7 = this.tendril_fr_seg6.getChild("tendril_fr_seg7");
		this.tendril_fr_seg8 = this.tendril_fr_seg7.getChild("tendril_fr_seg8");
		this.spore_sac_tendril_fr = this.tendril_fr_seg8.getChild("spore_sac_tendril_fr");
		this.tendril_bl_seg1 = this.bell.getChild("tendril_bl_seg1");
		this.tendril_bl_seg2 = this.tendril_bl_seg1.getChild("tendril_bl_seg2");
		this.tendril_bl_seg3 = this.tendril_bl_seg2.getChild("tendril_bl_seg3");
		this.tendril_bl_seg4 = this.tendril_bl_seg3.getChild("tendril_bl_seg4");
		this.tendril_bl_seg5 = this.tendril_bl_seg4.getChild("tendril_bl_seg5");
		this.tendril_bl_seg6 = this.tendril_bl_seg5.getChild("tendril_bl_seg6");
		this.tendril_bl_seg7 = this.tendril_bl_seg6.getChild("tendril_bl_seg7");
		this.tendril_bl_seg8 = this.tendril_bl_seg7.getChild("tendril_bl_seg8");
		this.spore_sac_tendril_bl = this.tendril_bl_seg8.getChild("spore_sac_tendril_bl");
		this.tendril_br_seg1 = this.bell.getChild("tendril_br_seg1");
		this.tendril_br_seg2 = this.tendril_br_seg1.getChild("tendril_br_seg2");
		this.tendril_br_seg3 = this.tendril_br_seg2.getChild("tendril_br_seg3");
		this.tendril_br_seg4 = this.tendril_br_seg3.getChild("tendril_br_seg4");
		this.tendril_br_seg5 = this.tendril_br_seg4.getChild("tendril_br_seg5");
		this.tendril_br_seg6 = this.tendril_br_seg5.getChild("tendril_br_seg6");
		this.tendril_br_seg7 = this.tendril_br_seg6.getChild("tendril_br_seg7");
		this.tendril_br_seg8 = this.tendril_br_seg7.getChild("tendril_br_seg8");
		this.spore_sac_tendril_br = this.tendril_br_seg8.getChild("spore_sac_tendril_br");
		this.tendril_ifl_seg1 = this.bell.getChild("tendril_ifl_seg1");
		this.tendril_ifl_seg2 = this.tendril_ifl_seg1.getChild("tendril_ifl_seg2");
		this.tendril_ifl_seg3 = this.tendril_ifl_seg2.getChild("tendril_ifl_seg3");
		this.tendril_ifl_seg4 = this.tendril_ifl_seg3.getChild("tendril_ifl_seg4");
		this.spore_sac_tendril_ifl = this.tendril_ifl_seg4.getChild("spore_sac_tendril_ifl");
		this.tendril_ifr_seg1 = this.bell.getChild("tendril_ifr_seg1");
		this.tendril_ifr_seg2 = this.tendril_ifr_seg1.getChild("tendril_ifr_seg2");
		this.tendril_ifr_seg3 = this.tendril_ifr_seg2.getChild("tendril_ifr_seg3");
		this.tendril_ifr_seg4 = this.tendril_ifr_seg3.getChild("tendril_ifr_seg4");
		this.spore_sac_tendril_ifr = this.tendril_ifr_seg4.getChild("spore_sac_tendril_ifr");
		this.tendril_ibl_seg1 = this.bell.getChild("tendril_ibl_seg1");
		this.tendril_ibl_seg2 = this.tendril_ibl_seg1.getChild("tendril_ibl_seg2");
		this.tendril_ibl_seg3 = this.tendril_ibl_seg2.getChild("tendril_ibl_seg3");
		this.tendril_ibl_seg4 = this.tendril_ibl_seg3.getChild("tendril_ibl_seg4");
		this.spore_sac_tendril_ibl = this.tendril_ibl_seg4.getChild("spore_sac_tendril_ibl");
		this.tendril_ibr_seg1 = this.bell.getChild("tendril_ibr_seg1");
		this.tendril_ibr_seg2 = this.tendril_ibr_seg1.getChild("tendril_ibr_seg2");
		this.tendril_ibr_seg3 = this.tendril_ibr_seg2.getChild("tendril_ibr_seg3");
		this.tendril_ibr_seg4 = this.tendril_ibr_seg3.getChild("tendril_ibr_seg4");
		this.spore_sac_tendril_ibr = this.tendril_ibr_seg4.getChild("spore_sac_tendril_ibr");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bell = partdefinition.addOrReplaceChild("bell", CubeListBuilder.create().texOffs(2, 0).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bell_side_n = bell.addOrReplaceChild("bell_side_n", CubeListBuilder.create().texOffs(48, 8).addBox(-2.0F, -5.0F, -7.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 12).addBox(-3.5F, -3.0F, -9.0F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition bell_flap_n_1 = bell_side_n.addOrReplaceChild("bell_flap_n_1", CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.5F));

		PartDefinition bell_flap_n_2 = bell_flap_n_1.addOrReplaceChild("bell_flap_n_2", CubeListBuilder.create().texOffs(0, 24).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_n_3 = bell_flap_n_2.addOrReplaceChild("bell_flap_n_3", CubeListBuilder.create().texOffs(0, 30).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_n_4 = bell_flap_n_3.addOrReplaceChild("bell_flap_n_4", CubeListBuilder.create().texOffs(0, 36).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_side_ne = bell.addOrReplaceChild("bell_side_ne", CubeListBuilder.create().texOffs(48, 8).addBox(-2.0F, -5.0F, -7.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(40, 12).addBox(-3.5F, -3.0F, -9.0F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition bell_flap_ne_1 = bell_side_ne.addOrReplaceChild("bell_flap_ne_1", CubeListBuilder.create().texOffs(40, 18).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.5F));

		PartDefinition bell_flap_ne_2 = bell_flap_ne_1.addOrReplaceChild("bell_flap_ne_2", CubeListBuilder.create().texOffs(40, 24).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_ne_3 = bell_flap_ne_2.addOrReplaceChild("bell_flap_ne_3", CubeListBuilder.create().texOffs(40, 30).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_ne_4 = bell_flap_ne_3.addOrReplaceChild("bell_flap_ne_4", CubeListBuilder.create().texOffs(40, 36).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_side_e = bell.addOrReplaceChild("bell_side_e", CubeListBuilder.create().texOffs(48, 8).addBox(-2.0F, -5.0F, -7.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(20, 12).addBox(-3.5F, -3.0F, -9.0F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bell_flap_e_1 = bell_side_e.addOrReplaceChild("bell_flap_e_1", CubeListBuilder.create().texOffs(20, 18).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.5F));

		PartDefinition bell_flap_e_2 = bell_flap_e_1.addOrReplaceChild("bell_flap_e_2", CubeListBuilder.create().texOffs(20, 24).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_e_3 = bell_flap_e_2.addOrReplaceChild("bell_flap_e_3", CubeListBuilder.create().texOffs(20, 30).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_e_4 = bell_flap_e_3.addOrReplaceChild("bell_flap_e_4", CubeListBuilder.create().texOffs(20, 36).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_side_se = bell.addOrReplaceChild("bell_side_se", CubeListBuilder.create().texOffs(48, 8).addBox(-2.0F, -5.0F, -7.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(40, 12).addBox(-3.5F, -3.0F, -9.0F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.0F, -2.3562F, 0.0F));

		PartDefinition bell_flap_se_1 = bell_side_se.addOrReplaceChild("bell_flap_se_1", CubeListBuilder.create().texOffs(40, 18).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.5F));

		PartDefinition bell_flap_se_2 = bell_flap_se_1.addOrReplaceChild("bell_flap_se_2", CubeListBuilder.create().texOffs(40, 24).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_se_3 = bell_flap_se_2.addOrReplaceChild("bell_flap_se_3", CubeListBuilder.create().texOffs(40, 30).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_se_4 = bell_flap_se_3.addOrReplaceChild("bell_flap_se_4", CubeListBuilder.create().texOffs(40, 36).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_side_s = bell.addOrReplaceChild("bell_side_s", CubeListBuilder.create().texOffs(48, 8).addBox(-2.0F, -5.0F, -7.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 12).addBox(-3.5F, -3.0F, -9.0F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.0F, -3.1416F, 0.0F));

		PartDefinition bell_flap_s_1 = bell_side_s.addOrReplaceChild("bell_flap_s_1", CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.5F));

		PartDefinition bell_flap_s_2 = bell_flap_s_1.addOrReplaceChild("bell_flap_s_2", CubeListBuilder.create().texOffs(0, 24).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_s_3 = bell_flap_s_2.addOrReplaceChild("bell_flap_s_3", CubeListBuilder.create().texOffs(0, 30).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_s_4 = bell_flap_s_3.addOrReplaceChild("bell_flap_s_4", CubeListBuilder.create().texOffs(0, 36).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_side_sw = bell.addOrReplaceChild("bell_side_sw", CubeListBuilder.create().texOffs(48, 8).addBox(-2.0F, -5.0F, -7.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(40, 12).addBox(-3.5F, -3.0F, -9.0F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.0F, 2.3562F, 0.0F));

		PartDefinition bell_flap_sw_1 = bell_side_sw.addOrReplaceChild("bell_flap_sw_1", CubeListBuilder.create().texOffs(40, 18).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.5F));

		PartDefinition bell_flap_sw_2 = bell_flap_sw_1.addOrReplaceChild("bell_flap_sw_2", CubeListBuilder.create().texOffs(40, 24).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_sw_3 = bell_flap_sw_2.addOrReplaceChild("bell_flap_sw_3", CubeListBuilder.create().texOffs(40, 30).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_sw_4 = bell_flap_sw_3.addOrReplaceChild("bell_flap_sw_4", CubeListBuilder.create().texOffs(40, 36).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_side_w = bell.addOrReplaceChild("bell_side_w", CubeListBuilder.create().texOffs(48, 8).addBox(-2.0F, -5.0F, -7.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(20, 12).addBox(-3.5F, -3.0F, -9.0F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition bell_flap_w_1 = bell_side_w.addOrReplaceChild("bell_flap_w_1", CubeListBuilder.create().texOffs(20, 18).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.5F));

		PartDefinition bell_flap_w_2 = bell_flap_w_1.addOrReplaceChild("bell_flap_w_2", CubeListBuilder.create().texOffs(20, 24).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_w_3 = bell_flap_w_2.addOrReplaceChild("bell_flap_w_3", CubeListBuilder.create().texOffs(20, 30).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_w_4 = bell_flap_w_3.addOrReplaceChild("bell_flap_w_4", CubeListBuilder.create().texOffs(20, 36).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_side_nw = bell.addOrReplaceChild("bell_side_nw", CubeListBuilder.create().texOffs(48, 8).addBox(-2.0F, -5.0F, -7.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(40, 12).addBox(-3.5F, -3.0F, -9.0F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition bell_flap_nw_1 = bell_side_nw.addOrReplaceChild("bell_flap_nw_1", CubeListBuilder.create().texOffs(40, 18).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.5F));

		PartDefinition bell_flap_nw_2 = bell_flap_nw_1.addOrReplaceChild("bell_flap_nw_2", CubeListBuilder.create().texOffs(40, 24).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_nw_3 = bell_flap_nw_2.addOrReplaceChild("bell_flap_nw_3", CubeListBuilder.create().texOffs(40, 30).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition bell_flap_nw_4 = bell_flap_nw_3.addOrReplaceChild("bell_flap_nw_4", CubeListBuilder.create().texOffs(40, 36).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition inner_organs = bell.addOrReplaceChild("inner_organs", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition organ_center = inner_organs.addOrReplaceChild("organ_center", CubeListBuilder.create().texOffs(48, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition organ_lobe_1 = inner_organs.addOrReplaceChild("organ_lobe_1", CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 1.0F, 2.0F));

		PartDefinition organ_lobe_2 = inner_organs.addOrReplaceChild("organ_lobe_2", CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 1.0F, -2.0F));

		PartDefinition organ_lobe_3 = inner_organs.addOrReplaceChild("organ_lobe_3", CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -0.5F, 2.0F));

		PartDefinition organ_lobe_4 = inner_organs.addOrReplaceChild("organ_lobe_4", CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -0.5F, -2.0F));

		PartDefinition tendril_fl_seg1 = bell.addOrReplaceChild("tendril_fl_seg1", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 6.0F, -5.0F));

		PartDefinition tendril_fl_seg2 = tendril_fl_seg1.addOrReplaceChild("tendril_fl_seg2", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_fl_seg3 = tendril_fl_seg2.addOrReplaceChild("tendril_fl_seg3", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_fl_seg4 = tendril_fl_seg3.addOrReplaceChild("tendril_fl_seg4", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_fl_seg5 = tendril_fl_seg4.addOrReplaceChild("tendril_fl_seg5", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_fl_seg6 = tendril_fl_seg5.addOrReplaceChild("tendril_fl_seg6", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_fl_seg7 = tendril_fl_seg6.addOrReplaceChild("tendril_fl_seg7", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_fl_seg8 = tendril_fl_seg7.addOrReplaceChild("tendril_fl_seg8", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition spore_sac_tendril_fl = tendril_fl_seg8.addOrReplaceChild("spore_sac_tendril_fl", CubeListBuilder.create().texOffs(0, 42).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_fr_seg1 = bell.addOrReplaceChild("tendril_fr_seg1", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 6.0F, -5.0F));

		PartDefinition tendril_fr_seg2 = tendril_fr_seg1.addOrReplaceChild("tendril_fr_seg2", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_fr_seg3 = tendril_fr_seg2.addOrReplaceChild("tendril_fr_seg3", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_fr_seg4 = tendril_fr_seg3.addOrReplaceChild("tendril_fr_seg4", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_fr_seg5 = tendril_fr_seg4.addOrReplaceChild("tendril_fr_seg5", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_fr_seg6 = tendril_fr_seg5.addOrReplaceChild("tendril_fr_seg6", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_fr_seg7 = tendril_fr_seg6.addOrReplaceChild("tendril_fr_seg7", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_fr_seg8 = tendril_fr_seg7.addOrReplaceChild("tendril_fr_seg8", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition spore_sac_tendril_fr = tendril_fr_seg8.addOrReplaceChild("spore_sac_tendril_fr", CubeListBuilder.create().texOffs(0, 42).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_bl_seg1 = bell.addOrReplaceChild("tendril_bl_seg1", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 6.0F, 5.0F));

		PartDefinition tendril_bl_seg2 = tendril_bl_seg1.addOrReplaceChild("tendril_bl_seg2", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_bl_seg3 = tendril_bl_seg2.addOrReplaceChild("tendril_bl_seg3", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_bl_seg4 = tendril_bl_seg3.addOrReplaceChild("tendril_bl_seg4", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_bl_seg5 = tendril_bl_seg4.addOrReplaceChild("tendril_bl_seg5", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_bl_seg6 = tendril_bl_seg5.addOrReplaceChild("tendril_bl_seg6", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_bl_seg7 = tendril_bl_seg6.addOrReplaceChild("tendril_bl_seg7", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_bl_seg8 = tendril_bl_seg7.addOrReplaceChild("tendril_bl_seg8", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition spore_sac_tendril_bl = tendril_bl_seg8.addOrReplaceChild("spore_sac_tendril_bl", CubeListBuilder.create().texOffs(0, 42).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_br_seg1 = bell.addOrReplaceChild("tendril_br_seg1", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 6.0F, 5.0F));

		PartDefinition tendril_br_seg2 = tendril_br_seg1.addOrReplaceChild("tendril_br_seg2", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_br_seg3 = tendril_br_seg2.addOrReplaceChild("tendril_br_seg3", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_br_seg4 = tendril_br_seg3.addOrReplaceChild("tendril_br_seg4", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_br_seg5 = tendril_br_seg4.addOrReplaceChild("tendril_br_seg5", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_br_seg6 = tendril_br_seg5.addOrReplaceChild("tendril_br_seg6", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_br_seg7 = tendril_br_seg6.addOrReplaceChild("tendril_br_seg7", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_br_seg8 = tendril_br_seg7.addOrReplaceChild("tendril_br_seg8", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition spore_sac_tendril_br = tendril_br_seg8.addOrReplaceChild("spore_sac_tendril_br", CubeListBuilder.create().texOffs(0, 42).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_ifl_seg1 = bell.addOrReplaceChild("tendril_ifl_seg1", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 6.0F, -2.5F));

		PartDefinition tendril_ifl_seg2 = tendril_ifl_seg1.addOrReplaceChild("tendril_ifl_seg2", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_ifl_seg3 = tendril_ifl_seg2.addOrReplaceChild("tendril_ifl_seg3", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_ifl_seg4 = tendril_ifl_seg3.addOrReplaceChild("tendril_ifl_seg4", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition spore_sac_tendril_ifl = tendril_ifl_seg4.addOrReplaceChild("spore_sac_tendril_ifl", CubeListBuilder.create().texOffs(0, 42).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_ifr_seg1 = bell.addOrReplaceChild("tendril_ifr_seg1", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 6.0F, -2.5F));

		PartDefinition tendril_ifr_seg2 = tendril_ifr_seg1.addOrReplaceChild("tendril_ifr_seg2", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_ifr_seg3 = tendril_ifr_seg2.addOrReplaceChild("tendril_ifr_seg3", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_ifr_seg4 = tendril_ifr_seg3.addOrReplaceChild("tendril_ifr_seg4", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition spore_sac_tendril_ifr = tendril_ifr_seg4.addOrReplaceChild("spore_sac_tendril_ifr", CubeListBuilder.create().texOffs(0, 42).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_ibl_seg1 = bell.addOrReplaceChild("tendril_ibl_seg1", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 6.0F, 2.5F));

		PartDefinition tendril_ibl_seg2 = tendril_ibl_seg1.addOrReplaceChild("tendril_ibl_seg2", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_ibl_seg3 = tendril_ibl_seg2.addOrReplaceChild("tendril_ibl_seg3", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_ibl_seg4 = tendril_ibl_seg3.addOrReplaceChild("tendril_ibl_seg4", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition spore_sac_tendril_ibl = tendril_ibl_seg4.addOrReplaceChild("spore_sac_tendril_ibl", CubeListBuilder.create().texOffs(0, 42).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_ibr_seg1 = bell.addOrReplaceChild("tendril_ibr_seg1", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 6.0F, 2.5F));

		PartDefinition tendril_ibr_seg2 = tendril_ibr_seg1.addOrReplaceChild("tendril_ibr_seg2", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_ibr_seg3 = tendril_ibr_seg2.addOrReplaceChild("tendril_ibr_seg3", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition tendril_ibr_seg4 = tendril_ibr_seg3.addOrReplaceChild("tendril_ibr_seg4", CubeListBuilder.create().texOffs(40, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition spore_sac_tendril_ibr = tendril_ibr_seg4.addOrReplaceChild("spore_sac_tendril_ibr", CubeListBuilder.create().texOffs(0, 42).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(SporeDrifterRenderState state) {
		float age = state.ageInTicks;

		// Pulsation contracting animation on the bell
		float pulsation = Mth.sin(age * 0.05F) * 0.15F + 0.15F;
		this.bell.yScale = 1.0F - pulsation * 0.5F;
		this.bell.xScale = 1.0F + pulsation * 0.2F;
		this.bell.zScale = 1.0F + pulsation * 0.2F;

		// Outer trailing tendril sways
		float wave = Mth.sin(age * 0.08F) * 0.1F;

		this.tendril_fl_seg1.zRot = wave;
		this.tendril_fl_seg2.zRot = wave * 1.2F;
		this.tendril_fl_seg3.zRot = wave * 1.4F;
		this.tendril_fl_seg4.zRot = wave * 1.6F;
		this.tendril_fl_seg5.zRot = wave * 1.8F;
		this.tendril_fl_seg6.zRot = wave * 2.0F;
		this.tendril_fl_seg7.zRot = wave * 2.2F;
		this.tendril_fl_seg8.zRot = wave * 2.4F;

		this.tendril_fr_seg1.zRot = -wave;
		this.tendril_fr_seg2.zRot = -wave * 1.2F;
		this.tendril_fr_seg3.zRot = -wave * 1.4F;
		this.tendril_fr_seg4.zRot = -wave * 1.6F;
		this.tendril_fr_seg5.zRot = -wave * 1.8F;
		this.tendril_fr_seg6.zRot = -wave * 2.0F;
		this.tendril_fr_seg7.zRot = -wave * 2.2F;
		this.tendril_fr_seg8.zRot = -wave * 2.4F;

		this.tendril_bl_seg1.xRot = wave;
		this.tendril_bl_seg2.xRot = wave * 1.2F;
		this.tendril_bl_seg3.xRot = wave * 1.4F;
		this.tendril_bl_seg4.xRot = wave * 1.6F;
		this.tendril_bl_seg5.xRot = wave * 1.8F;
		this.tendril_bl_seg6.xRot = wave * 2.0F;
		this.tendril_bl_seg7.xRot = wave * 2.2F;
		this.tendril_bl_seg8.xRot = wave * 2.4F;

		this.tendril_br_seg1.xRot = -wave;
		this.tendril_br_seg2.xRot = -wave * 1.2F;
		this.tendril_br_seg3.xRot = -wave * 1.4F;
		this.tendril_br_seg4.xRot = -wave * 1.6F;
		this.tendril_br_seg5.xRot = -wave * 1.8F;
		this.tendril_br_seg6.xRot = -wave * 2.0F;
		this.tendril_br_seg7.xRot = -wave * 2.2F;
		this.tendril_br_seg8.xRot = -wave * 2.4F;

		// Inner tendril chains (shorter)
		float waveInner = Mth.sin(age * 0.12F) * 0.08F;

		this.tendril_ifl_seg1.zRot = waveInner;
		this.tendril_ifl_seg2.zRot = waveInner * 1.3F;
		this.tendril_ifl_seg3.zRot = waveInner * 1.6F;
		this.tendril_ifl_seg4.zRot = waveInner * 1.9F;

		this.tendril_ifr_seg1.zRot = -waveInner;
		this.tendril_ifr_seg2.zRot = -waveInner * 1.3F;
		this.tendril_ifr_seg3.zRot = -waveInner * 1.6F;
		this.tendril_ifr_seg4.zRot = -waveInner * 1.9F;

		this.tendril_ibl_seg1.xRot = waveInner;
		this.tendril_ibl_seg2.xRot = waveInner * 1.3F;
		this.tendril_ibl_seg3.xRot = waveInner * 1.6F;
		this.tendril_ibl_seg4.xRot = waveInner * 1.9F;

		this.tendril_ibr_seg1.xRot = -waveInner;
		this.tendril_ibr_seg2.xRot = -waveInner * 1.3F;
		this.tendril_ibr_seg3.xRot = -waveInner * 1.6F;
		this.tendril_ibr_seg4.xRot = -waveInner * 1.9F;
	}
}