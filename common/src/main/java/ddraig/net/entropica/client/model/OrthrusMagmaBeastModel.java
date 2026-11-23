package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.OrthrusMagmaBeastAnimations;
import ddraig.net.entropica.client.renderer.OrthrusMagmaBeastRenderState;
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
 * Fauna 48 — Orthrus Magma-Beast (Two-Headed Hound).
 * Exact conversion of all 66 cubes, 54 bone groups, and 128x128 UVs from orthrus_magma_beast.bbmodel.
 */
public class OrthrusMagmaBeastModel extends EntityModel<OrthrusMagmaBeastRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "orthrus_magma_beast"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart head_l_grp;
    private final ModelPart head_r_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation runAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation snapComboAnimation;
    private final KeyframeAnimation howlAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public OrthrusMagmaBeastModel(ModelPart modelRoot) {
        super(modelRoot);
        ModelPart root = modelRoot.getChild("root");
        ModelPart bodyGrp = root.getChild("body_grp");
        ModelPart chestGrp = bodyGrp.getChild("chest_grp");
        this.head_l_grp = chestGrp.getChild("neck_1_l_grp").getChild("neck_2_l_grp").getChild("head_l_grp");
        this.head_r_grp = chestGrp.getChild("neck_1_r_grp").getChild("neck_2_r_grp").getChild("head_r_grp");

        this.idleAnimation = OrthrusMagmaBeastAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = OrthrusMagmaBeastAnimations.WALK.bake(modelRoot);
        this.runAnimation = OrthrusMagmaBeastAnimations.RUN.bake(modelRoot);
        this.attackAnimation = OrthrusMagmaBeastAnimations.ATTACK.bake(modelRoot);
        this.snapComboAnimation = OrthrusMagmaBeastAnimations.SNAP_COMBO.bake(modelRoot);
        this.howlAnimation = OrthrusMagmaBeastAnimations.HOWL.bake(modelRoot);
        this.hurtAnimation = OrthrusMagmaBeastAnimations.HURT.bake(modelRoot);
        this.deathAnimation = OrthrusMagmaBeastAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_grp = root.addOrReplaceChild("body_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -21.5F, 0.0F));

        PartDefinition chest_grp = body_grp.addOrReplaceChild("chest_grp", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -8.0F, -17.0F, 20.0F, 16.0F, 18.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.7F, -2.5F, -0.2967F, 0.0F, 0.0F));

        PartDefinition neck_1_l_grp = chest_grp.addOrReplaceChild("neck_1_l_grp", CubeListBuilder.create().texOffs(22, 69).addBox(-3.5F, -6.8F, -3.5F, 7.0F, 8.0F, 7.0F, new CubeDeformation(-0.35F)), PartPose.offsetAndRotation(-5.0F, -3.6F, -12.7F, 0.8203F, 0.2269F, -0.2443F));
        PartDefinition neck_2_l_grp = neck_1_l_grp.addOrReplaceChild("neck_2_l_grp", CubeListBuilder.create().texOffs(0, 79).addBox(-3.0F, -7.2F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, -5.6F, 0.0F, -0.1396F, 0.0698F, 0.1047F));

        PartDefinition head_l_grp = neck_2_l_grp.addOrReplaceChild("head_l_grp", CubeListBuilder.create().texOffs(31, 50).addBox(-5.0F, -5.0F, -6.0F, 10.0F, 8.0F, 10.0F, new CubeDeformation(-0.6F))
                .texOffs(99, 80).addBox(-3.0F, -1.6F, -13.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.3F, -0.192F, 0.0F, 0.1396F));

        head_l_grp.addOrReplaceChild("jaw_l_grp", CubeListBuilder.create().texOffs(43, 84).addBox(-3.0F, -0.4F, -7.6F, 6.0F, 2.0F, 8.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 2.1F, -5.2F, 0.0262F, 0.0F, 0.0F));
        head_l_grp.addOrReplaceChild("ear_l_in_grp", CubeListBuilder.create().texOffs(84, 93).mirror().addBox(-1.0F, -4.6F, -0.4F, 2.0F, 5.0F, 4.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(3.9F, -2.6F, 1.3F, -0.4887F, -0.2094F, 0.3491F));

        PartDefinition horn_l_in_grp = head_l_grp.addOrReplaceChild("horn_l_in_grp", CubeListBuilder.create().texOffs(112, 0).mirror().addBox(-1.0F, -3.6F, -0.4F, 2.0F, 4.0F, 4.0F, new CubeDeformation(-0.15F)).mirror(false), PartPose.offsetAndRotation(3.0F, -3.8F, 0.1F, -0.5934F, -0.1047F, 0.1745F));
        PartDefinition horn_mid_l_in_grp = horn_l_in_grp.addOrReplaceChild("horn_mid_l_in_grp", CubeListBuilder.create().texOffs(115, 50).mirror().addBox(-1.0F, -4.6F, -1.4F, 2.0F, 5.0F, 3.0F, new CubeDeformation(-0.28F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.0F, 2.2F, 0.3142F, 0.0F, -0.0698F));
        horn_mid_l_in_grp.addOrReplaceChild("horn_tip_l_in_grp", CubeListBuilder.create().texOffs(59, 0).mirror().addBox(-1.0F, -4.6F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.48F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.8F, 0.2F, 0.2793F, 0.0F, -0.0698F));

        head_l_grp.addOrReplaceChild("ear_l_out_grp", CubeListBuilder.create().texOffs(84, 93).addBox(-1.0F, -4.6F, -0.4F, 2.0F, 5.0F, 4.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-3.9F, -2.6F, 1.3F, -0.4887F, 0.2094F, -0.3491F));

        PartDefinition horn_l_out_grp = head_l_grp.addOrReplaceChild("horn_l_out_grp", CubeListBuilder.create().texOffs(112, 0).addBox(-1.0F, -3.6F, -0.4F, 2.0F, 4.0F, 4.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(-3.0F, -3.8F, 0.1F, -0.5934F, 0.1047F, -0.1745F));
        PartDefinition horn_mid_l_out_grp = horn_l_out_grp.addOrReplaceChild("horn_mid_l_out_grp", CubeListBuilder.create().texOffs(115, 50).addBox(-1.0F, -4.6F, -1.4F, 2.0F, 5.0F, 3.0F, new CubeDeformation(-0.28F)), PartPose.offsetAndRotation(0.0F, -3.0F, 2.2F, 0.3142F, 0.0F, 0.0698F));
        horn_mid_l_out_grp.addOrReplaceChild("horn_tip_l_out_grp", CubeListBuilder.create().texOffs(59, 0).addBox(-1.0F, -4.6F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.48F)), PartPose.offsetAndRotation(0.0F, -3.8F, 0.2F, 0.2793F, 0.0F, 0.0698F));

        PartDefinition neck_1_r_grp = chest_grp.addOrReplaceChild("neck_1_r_grp", CubeListBuilder.create().texOffs(94, 64).addBox(-3.5F, -6.8F, -3.5F, 7.0F, 8.0F, 7.0F, new CubeDeformation(-0.35F)), PartPose.offsetAndRotation(5.0F, -3.6F, -12.7F, 0.8203F, -0.2269F, 0.2443F));
        PartDefinition neck_2_r_grp = neck_1_r_grp.addOrReplaceChild("neck_2_r_grp", CubeListBuilder.create().texOffs(74, 78).addBox(-3.0F, -7.2F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, -5.6F, 0.0F, -0.1396F, -0.0698F, -0.1047F));

        PartDefinition head_r_grp = neck_2_r_grp.addOrReplaceChild("head_r_grp", CubeListBuilder.create().texOffs(0, 35).addBox(-5.0F, -5.0F, -6.0F, 10.0F, 8.0F, 10.0F, new CubeDeformation(-0.6F))
                .texOffs(99, 80).mirror().addBox(-3.0F, -1.6F, -13.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(0.0F, -6.0F, 0.3F, -0.192F, 0.0F, -0.1396F));

        head_r_grp.addOrReplaceChild("jaw_r_grp", CubeListBuilder.create().texOffs(43, 84).mirror().addBox(-3.0F, -0.4F, -7.6F, 6.0F, 2.0F, 8.0F, new CubeDeformation(-0.3F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.1F, -5.2F, 0.0262F, 0.0F, 0.0F));
        head_r_grp.addOrReplaceChild("ear_r_in_grp", CubeListBuilder.create().texOffs(84, 93).addBox(-1.0F, -4.6F, -0.4F, 2.0F, 5.0F, 4.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-3.9F, -2.6F, 1.3F, -0.4887F, 0.2094F, -0.3491F));

        PartDefinition horn_r_in_grp = head_r_grp.addOrReplaceChild("horn_r_in_grp", CubeListBuilder.create().texOffs(112, 0).addBox(-1.0F, -3.6F, -0.4F, 2.0F, 4.0F, 4.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(-3.0F, -3.8F, 0.1F, -0.5934F, 0.1047F, -0.1745F));
        PartDefinition horn_mid_r_in_grp = horn_r_in_grp.addOrReplaceChild("horn_mid_r_in_grp", CubeListBuilder.create().texOffs(115, 50).addBox(-1.0F, -4.6F, -1.4F, 2.0F, 5.0F, 3.0F, new CubeDeformation(-0.28F)), PartPose.offsetAndRotation(0.0F, -3.0F, 2.2F, 0.3142F, 0.0F, 0.0698F));
        horn_mid_r_in_grp.addOrReplaceChild("horn_tip_r_in_grp", CubeListBuilder.create().texOffs(59, 0).addBox(-1.0F, -4.6F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.48F)), PartPose.offsetAndRotation(0.0F, -3.8F, 0.2F, 0.2793F, 0.0F, 0.0698F));

        head_r_grp.addOrReplaceChild("ear_r_out_grp", CubeListBuilder.create().texOffs(84, 93).mirror().addBox(-1.0F, -4.6F, -0.4F, 2.0F, 5.0F, 4.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(3.9F, -2.6F, 1.3F, -0.4887F, -0.2094F, 0.3491F));

        PartDefinition horn_r_out_grp = head_r_grp.addOrReplaceChild("horn_r_out_grp", CubeListBuilder.create().texOffs(112, 0).mirror().addBox(-1.0F, -3.6F, -0.4F, 2.0F, 4.0F, 4.0F, new CubeDeformation(-0.15F)).mirror(false), PartPose.offsetAndRotation(3.0F, -3.8F, 0.1F, -0.5934F, -0.1047F, 0.1745F));
        PartDefinition horn_mid_r_out_grp = horn_r_out_grp.addOrReplaceChild("horn_mid_r_out_grp", CubeListBuilder.create().texOffs(115, 50).mirror().addBox(-1.0F, -4.6F, -1.4F, 2.0F, 5.0F, 3.0F, new CubeDeformation(-0.28F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.0F, 2.2F, 0.3142F, 0.0F, -0.0698F));
        horn_mid_r_out_grp.addOrReplaceChild("horn_tip_r_out_grp", CubeListBuilder.create().texOffs(59, 0).mirror().addBox(-1.0F, -4.6F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.48F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.8F, 0.2F, 0.2793F, 0.0F, -0.0698F));

        PartDefinition hips_grp = body_grp.addOrReplaceChild("hips_grp", CubeListBuilder.create().texOffs(59, 17).addBox(-8.0F, -7.0F, -2.5F, 16.0F, 14.0F, 18.0F, new CubeDeformation(-1.0F)), PartPose.offset(0.0F, 0.0F, -1.0F));

        PartDefinition leg_bl_grp = hips_grp.addOrReplaceChild("leg_bl_grp", CubeListBuilder.create().texOffs(72, 50).addBox(-3.4F, -1.2F, -4.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-6.4F, -0.9F, 10.2F, -0.2443F, 0.0F, 0.0F));
        PartDefinition calf_bl_grp = leg_bl_grp.addOrReplaceChild("calf_bl_grp", CubeListBuilder.create().texOffs(20, 89).addBox(-2.0F, -0.8F, -2.5F, 4.0F, 9.0F, 5.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.9425F, 0.0F, 0.0F));
        PartDefinition meta_bl_grp = calf_bl_grp.addOrReplaceChild("meta_bl_grp", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.3F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(0.0F, 7.2F, 0.0F, -0.9425F, 0.0F, 0.0F));
        meta_bl_grp.addOrReplaceChild("paw_bl_grp", CubeListBuilder.create().texOffs(31, 35).addBox(-3.0F, 0.0F, -3.8F, 6.0F, 2.0F, 7.0F, new CubeDeformation(-0.05F))
                .texOffs(0, 35).addBox(1.0F, 0.3F, -5.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F))
                .texOffs(0, 35).addBox(-1.0F, 0.3F, -5.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F))
                .texOffs(0, 35).addBox(-3.0F, 0.3F, -5.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F)), PartPose.offsetAndRotation(0.0F, 5.392F, 0.0F, 0.2443F, 0.0F, 0.0F));

        PartDefinition leg_br_grp = hips_grp.addOrReplaceChild("leg_br_grp", CubeListBuilder.create().texOffs(72, 50).mirror().addBox(-2.6F, -1.2F, -4.0F, 6.0F, 12.0F, 8.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(6.4F, -0.9F, 10.2F, -0.2443F, 0.0F, 0.0F));
        PartDefinition calf_br_grp = leg_br_grp.addOrReplaceChild("calf_br_grp", CubeListBuilder.create().texOffs(20, 89).mirror().addBox(-2.0F, -0.8F, -2.5F, 4.0F, 9.0F, 5.0F, new CubeDeformation(-0.15F)).mirror(false), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.9425F, 0.0F, 0.0F));
        PartDefinition meta_br_grp = calf_br_grp.addOrReplaceChild("meta_br_grp", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.0F, -2.3F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(-0.15F)).mirror(false), PartPose.offsetAndRotation(0.0F, 7.2F, 0.0F, -0.9425F, 0.0F, 0.0F));
        meta_br_grp.addOrReplaceChild("paw_br_grp", CubeListBuilder.create().texOffs(31, 35).mirror().addBox(-3.0F, 0.0F, -3.8F, 6.0F, 2.0F, 7.0F, new CubeDeformation(-0.05F)).mirror(false)
                .texOffs(0, 35).mirror().addBox(1.0F, 0.3F, -5.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F)).mirror(false)
                .texOffs(0, 35).mirror().addBox(-1.0F, 0.3F, -5.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F)).mirror(false)
                .texOffs(0, 35).mirror().addBox(-3.0F, 0.3F, -5.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F)).mirror(false), PartPose.offsetAndRotation(0.0F, 5.392F, 0.0F, 0.2443F, 0.0F, 0.0F));

        PartDefinition tail_1_grp = hips_grp.addOrReplaceChild("tail_1_grp", CubeListBuilder.create().texOffs(90, 0).addBox(-3.0F, -3.0F, -0.4F, 6.0F, 6.0F, 9.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, -3.5F, 14.4F, -0.4363F, 0.0F, 0.0F));
        PartDefinition tail_2_grp = tail_1_grp.addOrReplaceChild("tail_2_grp", CubeListBuilder.create().texOffs(59, 0).addBox(-2.5F, -2.5F, -0.4F, 5.0F, 5.0F, 10.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, 8.0F, 0.2967F, 0.0F, 0.0F));
        tail_2_grp.addOrReplaceChild("tail_shard_2l_grp", CubeListBuilder.create().texOffs(65, 93).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-1.8F, -0.4F, 2.2F, 0.2793F, -0.4189F, 0.0F));
        tail_2_grp.addOrReplaceChild("tail_shard_2r_grp", CubeListBuilder.create().texOffs(65, 93).mirror().addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(1.8F, -0.4F, 2.2F, 0.2793F, 0.4189F, 0.0F));
        tail_2_grp.addOrReplaceChild("tail_shard_2u_grp", CubeListBuilder.create().texOffs(65, 93).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, -1.6F, 1.6F, 0.4887F, 0.0F, 0.0F));
        tail_2_grp.addOrReplaceChild("tail_shard_2d_grp", CubeListBuilder.create().texOffs(65, 93).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 1.6F, 2.6F, -0.384F, 0.0F, 0.0F));

        PartDefinition tail_3_grp = tail_2_grp.addOrReplaceChild("tail_3_grp", CubeListBuilder.create().texOffs(51, 69).addBox(-2.0F, -2.0F, -0.4F, 4.0F, 4.0F, 10.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, 9.0F, 0.384F, 0.0F, 0.0F));
        tail_3_grp.addOrReplaceChild("tail_shard_3l_grp", CubeListBuilder.create().texOffs(80, 0).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-1.5F, -0.4F, 2.2F, 0.2443F, -0.4189F, 0.0F));
        tail_3_grp.addOrReplaceChild("tail_shard_3r_grp", CubeListBuilder.create().texOffs(80, 0).mirror().addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(1.5F, -0.4F, 2.2F, 0.2443F, 0.4189F, 0.0F));
        tail_3_grp.addOrReplaceChild("tail_shard_3u_grp", CubeListBuilder.create().texOffs(80, 0).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, -1.4F, 1.6F, 0.4538F, 0.0F, 0.0F));
        tail_3_grp.addOrReplaceChild("tail_shard_3d_grp", CubeListBuilder.create().texOffs(80, 0).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, 1.4F, 2.6F, -0.3491F, 0.0F, 0.0F));

        tail_3_grp.addOrReplaceChild("tail_4_grp", CubeListBuilder.create().texOffs(101, 50).addBox(-1.0F, -1.0F, -0.4F, 2.0F, 2.0F, 9.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(0.0F, 0.0F, 9.0F, 0.1396F, 0.0F, 0.0F));

        PartDefinition leg_fl_grp = body_grp.addOrReplaceChild("leg_fl_grp", CubeListBuilder.create().texOffs(0, 54).addBox(-3.4F, -0.8F, -4.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-7.6F, -0.9F, -11.0F, 0.1745F, 0.0F, 0.0F));
        PartDefinition foreleg_fl_grp = leg_fl_grp.addOrReplaceChild("foreleg_fl_grp", CubeListBuilder.create().texOffs(110, 16).addBox(-2.0F, -0.4F, -2.5F, 4.0F, 11.0F, 5.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(0.0F, 9.6F, 0.0F, -0.3316F, 0.0F, 0.0F));
        foreleg_fl_grp.addOrReplaceChild("paw_fl_grp", CubeListBuilder.create().texOffs(31, 35).addBox(-3.0F, 0.0F, -3.8F, 6.0F, 2.0F, 7.0F, new CubeDeformation(-0.05F))
                .texOffs(0, 35).addBox(1.0F, 0.3F, -5.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F))
                .texOffs(0, 35).addBox(-1.0F, 0.3F, -5.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F))
                .texOffs(0, 35).addBox(-3.0F, 0.3F, -5.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F)), PartPose.offsetAndRotation(0.0F, 11.133F, 0.0F, 0.1571F, 0.0F, 0.0F));

        PartDefinition leg_fr_grp = body_grp.addOrReplaceChild("leg_fr_grp", CubeListBuilder.create().texOffs(0, 54).mirror().addBox(-2.6F, -0.8F, -4.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(7.6F, -0.9F, -11.0F, 0.1745F, 0.0F, 0.0F));
        PartDefinition foreleg_fr_grp = leg_fr_grp.addOrReplaceChild("foreleg_fr_grp", CubeListBuilder.create().texOffs(110, 16).mirror().addBox(-2.0F, -0.4F, -2.5F, 4.0F, 11.0F, 5.0F, new CubeDeformation(-0.15F)).mirror(false), PartPose.offsetAndRotation(0.0F, 9.6F, 0.0F, -0.3316F, 0.0F, 0.0F));
        foreleg_fr_grp.addOrReplaceChild("paw_fr_grp", CubeListBuilder.create().texOffs(31, 35).mirror().addBox(-3.0F, 0.0F, -3.8F, 6.0F, 2.0F, 7.0F, new CubeDeformation(-0.05F)).mirror(false)
                .texOffs(0, 35).mirror().addBox(1.0F, 0.3F, -5.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F)).mirror(false)
                .texOffs(0, 35).mirror().addBox(-1.0F, 0.3F, -5.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F)).mirror(false)
                .texOffs(0, 35).mirror().addBox(-3.0F, 0.3F, -5.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F)).mirror(false), PartPose.offsetAndRotation(0.0F, 11.133F, 0.0F, 0.1571F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(OrthrusMagmaBeastRenderState state) {
        super.setupAnim(state);

        if (state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        if (state.attackAnimationState.isStarted()) {
            this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        } else if (state.snapComboAnimationState.isStarted()) {
            this.snapComboAnimation.apply(state.snapComboAnimationState, state.ageInTicks);
        } else if (state.howlAnimationState.isStarted()) {
            this.howlAnimation.apply(state.howlAnimationState, state.ageInTicks);
        } else if (state.runAnimationState.isStarted()) {
            this.runAnimation.apply(state.runAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        if (state.hurtAnimationState.isStarted()) {
            this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);
        }

        if (!state.howlAnimationState.isStarted()) {
            // Clamp yaw gently so the two non-overlapping wolf crowns never collide!
            float yawRad = Mth.clamp(state.yRot, -22.0F, 22.0F) * Mth.DEG_TO_RAD;
            float pitchRad = Mth.clamp(state.xRot, -20.0F, 20.0F) * Mth.DEG_TO_RAD;
            this.head_l_grp.yRot += yawRad * 0.65F;
            this.head_l_grp.xRot += pitchRad * 0.65F;
            this.head_r_grp.yRot += yawRad * 0.65F;
            this.head_r_grp.xRot += pitchRad * 0.65F;
        }
    }
}
