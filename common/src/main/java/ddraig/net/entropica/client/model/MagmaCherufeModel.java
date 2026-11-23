package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.MagmaCherufeAnimations;
import ddraig.net.entropica.client.renderer.MagmaCherufeRenderState;
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
 * Fauna 44 — Magma Cherufe (Lava Lizard).
 * Exact conversion of all 69 cubes, 24 bone groups, and 64x64 UVs from magma_cherufe.bbmodel.
 */
public class MagmaCherufeModel extends EntityModel<MagmaCherufeRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "magma_cherufe"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart neck_grp;
    private final ModelPart head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation climbAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation tailWhipAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public MagmaCherufeModel(ModelPart modelRoot) {
        super(modelRoot);
        ModelPart root = modelRoot.getChild("root");
        ModelPart bodyRoot = root.getChild("body_root");
        ModelPart pelvisGrp = bodyRoot.getChild("pelvis_grp");
        ModelPart chestGrp = pelvisGrp.getChild("chest_grp");
        this.neck_grp = chestGrp.getChild("neck_grp");
        this.head_grp = this.neck_grp.getChild("head_grp");

        this.idleAnimation = MagmaCherufeAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = MagmaCherufeAnimations.WALK.bake(modelRoot);
        this.climbAnimation = MagmaCherufeAnimations.CLIMB.bake(modelRoot);
        this.attackAnimation = MagmaCherufeAnimations.ATTACK.bake(modelRoot);
        this.tailWhipAnimation = MagmaCherufeAnimations.TAIL_WHIP.bake(modelRoot);
        this.hurtAnimation = MagmaCherufeAnimations.HURT.bake(modelRoot);
        this.deathAnimation = MagmaCherufeAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_root = root.addOrReplaceChild("body_root", CubeListBuilder.create(), PartPose.offset(0.0F, -11.0F, -1.0F));

        PartDefinition pelvis_grp = body_root.addOrReplaceChild("pelvis_grp", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -5.0F, -3.0F, 10.0F, 9.0F, 5.0F, new CubeDeformation(0.25F))
                .texOffs(51, 53).addBox(-7.0F, -3.7F, -2.5F, 2.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(51, 53).mirror().addBox(5.0F, -3.7F, -2.5F, 2.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 56).addBox(-2.0F, -6.7F, -2.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).mirror().addBox(-5.0F, -4.0F, 2.0F, 10.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(51, 53).addBox(-6.5F, -2.8F, 2.5F, 2.0F, 7.0F, 4.0F, new CubeDeformation(-0.25F))
                .texOffs(51, 53).mirror().addBox(4.5F, -2.8F, 2.5F, 2.0F, 7.0F, 4.0F, new CubeDeformation(-0.25F)).mirror(false)
                .texOffs(0, 56).mirror().addBox(-2.0F, -5.5F, 3.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -1.5F, 3.0F));

        PartDefinition chest_grp = pelvis_grp.addOrReplaceChild("chest_grp", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-5.0F, -5.0F, -5.0F, 10.0F, 9.0F, 5.0F, new CubeDeformation(0.52F)).mirror(false)
                .texOffs(51, 53).addBox(-7.35F, -3.8F, -4.5F, 2.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(51, 53).mirror().addBox(5.35F, -3.8F, -4.5F, 2.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 56).mirror().addBox(-2.0F, -7.0F, -4.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 0).addBox(-5.0F, -4.5F, -10.0F, 10.0F, 9.0F, 5.0F, new CubeDeformation(0.48F))
                .texOffs(51, 53).addBox(-7.25F, -3.5F, -9.5F, 2.0F, 7.0F, 4.0F, new CubeDeformation(-0.01F))
                .texOffs(51, 53).mirror().addBox(5.25F, -3.5F, -9.5F, 2.0F, 7.0F, 4.0F, new CubeDeformation(-0.01F)).mirror(false)
                .texOffs(0, 56).addBox(-2.0F, -6.5F, -9.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(23, 26).addBox(-6.0F, -5.5F, -9.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(23, 26).mirror().addBox(4.0F, -5.5F, -9.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(20, 18).addBox(-3.5F, 4.8F, -8.0F, 7.0F, 2.0F, 6.0F, new CubeDeformation(0.35F)), PartPose.offset(0.0F, -0.5F, -3.0F));

        PartDefinition neck_grp = chest_grp.addOrReplaceChild("neck_grp", CubeListBuilder.create().texOffs(0, 45).addBox(-4.5F, -4.0F, -4.0F, 9.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(20, 18).mirror().addBox(-3.5F, 3.0F, -4.0F, 7.0F, 2.0F, 6.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offset(0.0F, -0.5F, -9.5F));

        PartDefinition head_grp = neck_grp.addOrReplaceChild("head_grp", CubeListBuilder.create().texOffs(21, 53).addBox(-5.0F, -3.0F, -5.0F, 10.0F, 6.0F, 5.0F, new CubeDeformation(-0.01F))
                .texOffs(11, 58).addBox(-5.35F, -3.1F, -4.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(11, 58).mirror().addBox(4.35F, -3.1F, -4.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 14).addBox(-4.0F, -2.0F, -10.0F, 8.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(28, 26).addBox(-2.5F, 1.5F, -9.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.2F))
                .texOffs(0, 0).addBox(-3.9F, 1.5F, -9.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.02F))
                .texOffs(0, 0).mirror().addBox(2.9F, 1.5F, -9.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.02F)).mirror(false)
                .texOffs(0, 0).addBox(-3.9F, 1.5F, -6.9F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.02F))
                .texOffs(0, 0).mirror().addBox(2.9F, 1.5F, -6.9F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.02F)).mirror(false)
                .texOffs(0, 0).addBox(-0.5F, 1.4F, -9.9F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.03F)), PartPose.offset(0.0F, 0.0F, -4.0F));

        PartDefinition jaw_grp = head_grp.addOrReplaceChild("jaw_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 2.7F, -0.3F));

        jaw_grp.addOrReplaceChild("fang_lf_r_r1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.7F, -1.3F, -9.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.04F)).mirror(false)
                .texOffs(0, 0).addBox(-1.7F, -1.3F, -9.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.04F))
                .texOffs(0, 0).mirror().addBox(2.3F, -1.3F, -7.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.03F)).mirror(false)
                .texOffs(0, 0).addBox(-3.3F, -1.3F, -7.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.03F))
                .texOffs(0, 0).mirror().addBox(2.3F, -1.3F, -9.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.03F)).mirror(false)
                .texOffs(0, 0).addBox(-3.3F, -1.3F, -9.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.03F))
                .texOffs(28, 26).mirror().addBox(-2.5F, -0.2F, -8.4F, 5.0F, 1.0F, 6.0F, new CubeDeformation(-0.02F)).mirror(false)
                .texOffs(25, 11).addBox(-3.5F, 0.4F, -9.7F, 7.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(-4.5F, -0.5F, -5.0F, 9.0F, 3.0F, 5.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition tail_1_grp = pelvis_grp.addOrReplaceChild("tail_1_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 1.3F, 7.0F));
        tail_1_grp.addOrReplaceChild("tail_seg_1_r1", CubeListBuilder.create().texOffs(23, 33).addBox(-4.0F, -3.3F, 0.0F, 8.0F, 7.0F, 5.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698F, 0.0F, 0.0F));

        PartDefinition tail_2_grp = tail_1_grp.addOrReplaceChild("tail_2_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 5.0F));
        tail_2_grp.addOrReplaceChild("tail_seg_2_r1", CubeListBuilder.create().texOffs(0, 34).addBox(-3.0F, -2.5F, 0.0F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698F, 0.0F, 0.0F));

        PartDefinition tail_3_grp = tail_2_grp.addOrReplaceChild("tail_3_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 5.0F));
        tail_3_grp.addOrReplaceChild("tail_seg_3_r1", CubeListBuilder.create().texOffs(46, 29).addBox(-2.5F, -1.8F, 0.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698F, 0.0F, 0.0F));

        PartDefinition tail_4_grp = tail_3_grp.addOrReplaceChild("tail_4_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.9F, 4.0F));
        tail_4_grp.addOrReplaceChild("tail_seg_4_r1", CubeListBuilder.create().texOffs(46, 20).addBox(-2.0F, -1.2F, 0.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698F, 0.0F, 0.0F));

        PartDefinition tail_5_grp = tail_4_grp.addOrReplaceChild("tail_5_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.8F, 4.0F));
        tail_5_grp.addOrReplaceChild("tail_seg_5_r1", CubeListBuilder.create().texOffs(26, 45).addBox(-1.5F, -0.6F, 0.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698F, 0.0F, 0.0F));

        PartDefinition leg_fl_grp = body_root.addOrReplaceChild("leg_fl_grp", CubeListBuilder.create(), PartPose.offset(-6.8F, -2.2F, -7.2F));
        leg_fl_grp.addOrReplaceChild("arm_fl_r1", CubeListBuilder.create().texOffs(44, 40).addBox(-3.0F, -0.6F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.384F, -0.1396F, 0.3491F));

        PartDefinition shin_fl_grp = leg_fl_grp.addOrReplaceChild("shin_fl_grp", CubeListBuilder.create(), PartPose.offset(-2.7F, 5.6F, 2.2F));
        shin_fl_grp.addOrReplaceChild("forearm_fl_r1", CubeListBuilder.create().texOffs(30, 0).addBox(-2.0F, -0.6F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.384F, 0.2094F, 0.1745F));

        PartDefinition foot_fl_grp = shin_fl_grp.addOrReplaceChild("foot_fl_grp", CubeListBuilder.create(), PartPose.offset(-1.1F, 6.1F, -2.5F));
        foot_fl_grp.addOrReplaceChild("claw_fl_3_r1", CubeListBuilder.create().texOffs(25, 0).addBox(-2.2F, -0.5F, -5.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(0.0F, 1.0F, -0.3F, 0.0F, 0.5236F, 0.0F));
        foot_fl_grp.addOrReplaceChild("claw_fl_2_r1", CubeListBuilder.create().texOffs(25, 0).addBox(-0.5F, -0.5F, -5.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.025F))
                .texOffs(44, 13).addBox(-2.5F, -1.5F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -0.3F, 0.0F, 0.3491F, 0.0F));
        foot_fl_grp.addOrReplaceChild("claw_fl_1_r1", CubeListBuilder.create().texOffs(25, 0).addBox(1.2F, -0.5F, -5.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, 1.0F, -0.3F, 0.0F, 0.1745F, 0.0F));

        PartDefinition leg_fr_grp = body_root.addOrReplaceChild("leg_fr_grp", CubeListBuilder.create(), PartPose.offset(6.8F, -2.2F, -7.2F));
        leg_fr_grp.addOrReplaceChild("arm_fr_r1", CubeListBuilder.create().texOffs(44, 40).mirror().addBox(-2.0F, -0.6F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.384F, 0.1396F, -0.3491F));

        PartDefinition shin_fr_grp = leg_fr_grp.addOrReplaceChild("shin_fr_grp", CubeListBuilder.create(), PartPose.offset(2.7F, 5.6F, 2.2F));
        shin_fr_grp.addOrReplaceChild("forearm_fr_r1", CubeListBuilder.create().texOffs(30, 0).mirror().addBox(-2.0F, -0.6F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.384F, -0.2094F, -0.1745F));

        PartDefinition foot_fr_grp = shin_fr_grp.addOrReplaceChild("foot_fr_grp", CubeListBuilder.create(), PartPose.offset(1.1F, 6.1F, -2.5F));
        foot_fr_grp.addOrReplaceChild("claw_fr_3_r1", CubeListBuilder.create().texOffs(25, 0).mirror().addBox(1.2F, -0.5F, -5.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.04F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, -0.3F, 0.0F, -0.5236F, 0.0F));
        foot_fr_grp.addOrReplaceChild("claw_fr_2_r1", CubeListBuilder.create().texOffs(25, 0).mirror().addBox(-0.5F, -0.5F, -5.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.025F)).mirror(false)
                .texOffs(44, 13).mirror().addBox(-2.5F, -1.5F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, -0.3F, 0.0F, -0.3491F, 0.0F));
        foot_fr_grp.addOrReplaceChild("claw_fr_1_r1", CubeListBuilder.create().texOffs(25, 0).mirror().addBox(-2.2F, -0.5F, -5.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, -0.3F, 0.0F, -0.1745F, 0.0F));

        PartDefinition leg_bl_grp = body_root.addOrReplaceChild("leg_bl_grp", CubeListBuilder.create(), PartPose.offset(-6.0F, -1.0F, 7.0F));
        leg_bl_grp.addOrReplaceChild("thigh_bl_r1", CubeListBuilder.create().texOffs(44, 40).addBox(-3.0F, -1.0F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4189F, 0.2443F, 0.3491F));

        PartDefinition shin_bl_grp = leg_bl_grp.addOrReplaceChild("shin_bl_grp", CubeListBuilder.create(), PartPose.offset(-2.6F, 5.2F, -2.0F));
        shin_bl_grp.addOrReplaceChild("shin_bl_r1", CubeListBuilder.create().texOffs(46, 0).addBox(-2.0F, -0.4F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2443F, 0.2793F, 0.1745F));

        PartDefinition foot_bl_grp = shin_bl_grp.addOrReplaceChild("foot_bl_grp", CubeListBuilder.create(), PartPose.offset(-1.2F, 5.3F, 1.0F));
        foot_bl_grp.addOrReplaceChild("claw_bl_3_r1", CubeListBuilder.create().texOffs(25, 0).addBox(-2.2F, -0.5F, -5.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(0.0F, 1.0F, -0.3F, 0.0F, 0.5934F, 0.0F));
        foot_bl_grp.addOrReplaceChild("claw_bl_2_r1", CubeListBuilder.create().texOffs(25, 0).addBox(-0.5F, -0.5F, -5.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.025F))
                .texOffs(44, 13).addBox(-2.5F, -1.5F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -0.3F, 0.0F, 0.4189F, 0.0F));
        foot_bl_grp.addOrReplaceChild("claw_bl_1_r1", CubeListBuilder.create().texOffs(25, 0).addBox(1.2F, -0.5F, -5.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, 1.0F, -0.3F, 0.0F, 0.2443F, 0.0F));

        PartDefinition leg_br_grp = body_root.addOrReplaceChild("leg_br_grp", CubeListBuilder.create(), PartPose.offset(6.0F, -1.0F, 7.0F));
        leg_br_grp.addOrReplaceChild("thigh_br_r1", CubeListBuilder.create().texOffs(44, 40).mirror().addBox(-2.0F, -1.0F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4189F, -0.2443F, -0.3491F));

        PartDefinition shin_br_grp = leg_br_grp.addOrReplaceChild("shin_br_grp", CubeListBuilder.create(), PartPose.offset(2.6F, 5.2F, -2.0F));
        shin_br_grp.addOrReplaceChild("shin_br_r1", CubeListBuilder.create().texOffs(46, 0).mirror().addBox(-2.0F, -0.4F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2443F, -0.2793F, -0.1745F));

        PartDefinition foot_br_grp = shin_br_grp.addOrReplaceChild("foot_br_grp", CubeListBuilder.create(), PartPose.offset(1.2F, 5.3F, 1.0F));
        foot_br_grp.addOrReplaceChild("claw_br_3_r1", CubeListBuilder.create().texOffs(25, 0).mirror().addBox(1.2F, -0.5F, -5.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.04F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, -0.3F, 0.0F, -0.5934F, 0.0F));
        foot_br_grp.addOrReplaceChild("claw_br_2_r1", CubeListBuilder.create().texOffs(25, 0).mirror().addBox(-0.5F, -0.5F, -5.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.025F)).mirror(false)
                .texOffs(44, 13).mirror().addBox(-2.5F, -1.5F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, -0.3F, 0.0F, -0.4189F, 0.0F));
        foot_br_grp.addOrReplaceChild("claw_br_1_r1", CubeListBuilder.create().texOffs(25, 0).mirror().addBox(-2.2F, -0.5F, -5.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, -0.3F, 0.0F, -0.2443F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(MagmaCherufeRenderState state) {
        super.setupAnim(state);

        if (state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        if (state.attackAnimationState.isStarted()) {
            this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        } else if (state.tailWhipAnimationState.isStarted()) {
            this.tailWhipAnimation.apply(state.tailWhipAnimationState, state.ageInTicks);
        } else if (state.climbAnimationState.isStarted()) {
            this.climbAnimation.apply(state.climbAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        if (state.hurtAnimationState.isStarted()) {
            this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);
        }

        if (!state.tailWhipAnimationState.isStarted() && !state.climbAnimationState.isStarted()) {
            float yawRad = Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD;
            float pitchRad = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
            this.neck_grp.yRot += yawRad * 0.40F;
            this.head_grp.yRot += yawRad * 0.60F;
            this.head_grp.xRot += pitchRad * 0.65F;
        }
    }
}
