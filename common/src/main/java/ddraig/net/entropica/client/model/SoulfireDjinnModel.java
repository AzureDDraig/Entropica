package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.SoulfireDjinnAnimations;
import ddraig.net.entropica.client.renderer.SoulfireDjinnRenderState;
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
 * Fauna 57 — Soulfire Djinn (Desert Efreet).
 * Exact conversion of all 54 cubes, 19 bone groups, and 128x128 UVs from soulfire_djinn.bbmodel.
 */
public class SoulfireDjinnModel extends EntityModel<SoulfireDjinnRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "soulfire_djinn"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation glideTakeoffAnimation;
    private final KeyframeAnimation glideAnimation;
    private final KeyframeAnimation glideLandingAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation phaseGlideAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public SoulfireDjinnModel(ModelPart modelRoot) {
        super(modelRoot);
        ModelPart root = modelRoot.getChild("root");
        ModelPart chestGrp = root.getChild("body_grp").getChild("chest_grp");
        this.head_grp = chestGrp.getChild("neck_grp").getChild("head_grp");

        this.idleAnimation = SoulfireDjinnAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = SoulfireDjinnAnimations.WALK.bake(modelRoot);
        this.glideTakeoffAnimation = SoulfireDjinnAnimations.GLIDE_TAKEOFF.bake(modelRoot);
        this.glideAnimation = SoulfireDjinnAnimations.GLIDE.bake(modelRoot);
        this.glideLandingAnimation = SoulfireDjinnAnimations.GLIDE_LANDING.bake(modelRoot);
        this.attackAnimation = SoulfireDjinnAnimations.ATTACK.bake(modelRoot);
        this.phaseGlideAnimation = SoulfireDjinnAnimations.PHASE_GLIDE.bake(modelRoot);
        this.hurtAnimation = SoulfireDjinnAnimations.HURT.bake(modelRoot);
        this.deathAnimation = SoulfireDjinnAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_grp = root.addOrReplaceChild("body_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -17.0F, 0.0F));

        PartDefinition chest_grp = body_grp.addOrReplaceChild("chest_grp", CubeListBuilder.create()
                .texOffs(53, 0).addBox(-7.0F, -5.7F, -5.5F, 14.0F, 10.0F, 11.0F, new CubeDeformation(-2.5F)),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition collar_grp = chest_grp.addOrReplaceChild("collar_grp", CubeListBuilder.create()
                .texOffs(24, 60).addBox(-4.5F, -3.1F, -7.95F, 9.0F, 6.0F, 5.0F, new CubeDeformation(-1.5F)),
                PartPose.offsetAndRotation(0.0F, -3.6F, -0.2F, 0.2618F, 0.0F, 0.0F));
        collar_grp.addOrReplaceChild("collar_fr_r1", CubeListBuilder.create().texOffs(73, 60).mirror().addBox(-4.0F, -3.0F, -2.5F, 8.0F, 6.0F, 5.0F, new CubeDeformation(-1.52F)).mirror(false), PartPose.offsetAndRotation(3.854F, -0.1F, -3.854F, 0.0F, -0.7854F, 0.0F));
        collar_grp.addOrReplaceChild("collar_r_r1", CubeListBuilder.create().texOffs(24, 60).mirror().addBox(-4.5F, -3.0F, -2.5F, 9.0F, 6.0F, 5.0F, new CubeDeformation(-1.5F)).mirror(false), PartPose.offsetAndRotation(5.45F, -0.1F, 0.0F, 0.0F, -1.5708F, 0.0F));
        collar_grp.addOrReplaceChild("collar_br_r1", CubeListBuilder.create().texOffs(73, 60).mirror().addBox(-4.0F, -3.0F, -2.5F, 8.0F, 6.0F, 5.0F, new CubeDeformation(-1.52F)).mirror(false), PartPose.offsetAndRotation(3.854F, -0.1F, 3.854F, 0.0F, -2.3562F, 0.0F));
        collar_grp.addOrReplaceChild("collar_b_r1", CubeListBuilder.create().texOffs(24, 60).mirror().addBox(-4.5F, -3.0F, -2.5F, 9.0F, 6.0F, 5.0F, new CubeDeformation(-1.5F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.1F, 5.45F, 0.0F, 3.1416F, 0.0F));
        collar_grp.addOrReplaceChild("collar_bl_r1", CubeListBuilder.create().texOffs(73, 60).addBox(-4.0F, -3.0F, -2.5F, 8.0F, 6.0F, 5.0F, new CubeDeformation(-1.52F)), PartPose.offsetAndRotation(-3.854F, -0.1F, 3.854F, 0.0F, 2.3562F, 0.0F));
        collar_grp.addOrReplaceChild("collar_l_r1", CubeListBuilder.create().texOffs(24, 60).addBox(-4.5F, -3.0F, -2.5F, 9.0F, 6.0F, 5.0F, new CubeDeformation(-1.5F)), PartPose.offsetAndRotation(-5.45F, -0.1F, 0.0F, 0.0F, 1.5708F, 0.0F));
        collar_grp.addOrReplaceChild("collar_fl_r1", CubeListBuilder.create().texOffs(73, 60).addBox(-4.0F, -3.0F, -2.5F, 8.0F, 6.0F, 5.0F, new CubeDeformation(-1.52F)), PartPose.offsetAndRotation(-3.854F, -0.1F, -3.854F, 0.0F, 0.7854F, 0.0F));

        PartDefinition neck_grp = chest_grp.addOrReplaceChild("neck_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -3.8F, -0.3F));

        PartDefinition head_grp = neck_grp.addOrReplaceChild("head_grp", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.5F, -10.5F, -6.5F, 13.0F, 13.0F, 13.0F, new CubeDeformation(-2.5F)),
                PartPose.offset(0.0F, -0.6F, 0.0F));
        head_grp.addOrReplaceChild("brow_r_r1", CubeListBuilder.create().texOffs(21, 72).mirror().addBox(-3.0F, -2.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(-1.5F)).mirror(false), PartPose.offsetAndRotation(2.1F, -6.0F, -4.0F, 0.0F, 0.0F, -0.1396F));
        head_grp.addOrReplaceChild("brow_l_r1", CubeListBuilder.create().texOffs(21, 72).addBox(-3.0F, -2.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(-1.5F)), PartPose.offsetAndRotation(-2.1F, -6.0F, -4.0F, 0.0F, 0.0F, 0.1396F));

        PartDefinition crown_flame_grp = head_grp.addOrReplaceChild("crown_flame_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -7.8F, 0.0F));
        crown_flame_grp.addOrReplaceChild("horn_tip_r_r1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.3F, -7.8F, -1.3F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-1.0F)).mirror(false), PartPose.offsetAndRotation(3.8F, 2.8F, 0.8F, -0.733F, 0.0F, 0.2443F));
        crown_flame_grp.addOrReplaceChild("horn_base_r_r1", CubeListBuilder.create().texOffs(0, 65).mirror().addBox(-2.0F, -4.9F, -3.0F, 4.0F, 7.0F, 6.0F, new CubeDeformation(-1.5F)).mirror(false), PartPose.offsetAndRotation(3.8F, 2.8F, 0.8F, -0.6632F, 0.0F, 0.2094F));
        crown_flame_grp.addOrReplaceChild("crown_tip_r_r1", CubeListBuilder.create().texOffs(58, 40).mirror().addBox(-1.4F, -6.0F, -1.4F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.0F)).mirror(false), PartPose.offsetAndRotation(2.1F, 0.0F, -0.3F, -0.5585F, 0.0F, 0.1396F));
        crown_flame_grp.addOrReplaceChild("crown_spire_r_r1", CubeListBuilder.create().texOffs(48, 67).mirror().addBox(-2.5F, -4.1F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(-1.51F)).mirror(false), PartPose.offsetAndRotation(2.1F, 0.0F, -0.3F, -0.4887F, 0.0F, 0.1047F));
        crown_flame_grp.addOrReplaceChild("horn_tip_l_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.7F, -7.8F, -1.3F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(-3.8F, 2.8F, 0.8F, -0.733F, 0.0F, -0.2443F));
        crown_flame_grp.addOrReplaceChild("horn_base_l_r1", CubeListBuilder.create().texOffs(0, 65).addBox(-2.0F, -4.9F, -3.0F, 4.0F, 7.0F, 6.0F, new CubeDeformation(-1.5F)), PartPose.offsetAndRotation(-3.8F, 2.8F, 0.8F, -0.6632F, 0.0F, -0.2094F));
        crown_flame_grp.addOrReplaceChild("crown_tip_l_r1", CubeListBuilder.create().texOffs(58, 40).addBox(-1.6F, -6.0F, -1.4F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(-2.1F, 0.0F, -0.3F, -0.5585F, 0.0F, -0.1396F));
        crown_flame_grp.addOrReplaceChild("crown_spire_l_r1", CubeListBuilder.create().texOffs(48, 67).addBox(-2.5F, -4.1F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(-1.51F)), PartPose.offsetAndRotation(-2.1F, 0.0F, -0.3F, -0.4887F, 0.0F, -0.1047F));
        crown_flame_grp.addOrReplaceChild("crown_tip_c_r1", CubeListBuilder.create().texOffs(58, 40).addBox(-1.5F, -7.8F, -1.3F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.9F, -0.4887F, 0.0F, 0.0F));
        crown_flame_grp.addOrReplaceChild("crown_spire_c_r1", CubeListBuilder.create().texOffs(107, 13).addBox(-2.5F, -6.1F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.5F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.9F, -0.4189F, 0.0F, 0.0F));

        PartDefinition arm_l_grp = chest_grp.addOrReplaceChild("arm_l_grp", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-2.0F))
                .texOffs(0, 49).addBox(-3.5F, -0.2F, -3.5F, 7.0F, 8.0F, 7.0F, new CubeDeformation(-2.01F)),
                PartPose.offsetAndRotation(-6.3F, -1.6F, 0.0F, -0.1047F, 0.0F, 0.3491F));
        PartDefinition forearm_l_grp = arm_l_grp.addOrReplaceChild("forearm_l_grp", CubeListBuilder.create()
                .texOffs(98, 49).addBox(-3.5F, -2.0F, -3.5F, 7.0F, 8.0F, 7.0F, new CubeDeformation(-2.0F))
                .texOffs(25, 40).addBox(-4.0F, -0.6F, -4.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(-1.98F)),
                PartPose.offsetAndRotation(0.0F, 5.6F, 0.0F, -0.5934F, 0.0F, -0.1396F));
        PartDefinition hand_l_grp = forearm_l_grp.addOrReplaceChild("hand_l_grp", CubeListBuilder.create()
                .texOffs(100, 65).addBox(-3.0F, -1.7F, -2.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(-1.5F)),
                PartPose.offsetAndRotation(0.0F, 4.2F, 0.0F, -0.1745F, 0.0F, 0.0F));
        hand_l_grp.addOrReplaceChild("finger_2_l_r1", CubeListBuilder.create().texOffs(86, 72).addBox(-1.5F, -1.4F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.02F)), PartPose.offsetAndRotation(-1.0F, 2.4F, 0.6F, -0.2094F, 0.0F, -0.4887F));
        hand_l_grp.addOrReplaceChild("finger_1_l_r1", CubeListBuilder.create().texOffs(86, 72).addBox(-1.5F, -1.4F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.01F)), PartPose.offsetAndRotation(-1.0F, 2.4F, -0.6F, -0.2793F, 0.0F, -0.4887F));
        hand_l_grp.addOrReplaceChild("thumb_l_r1", CubeListBuilder.create().texOffs(86, 72).addBox(-1.2F, -1.4F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.01F)), PartPose.offsetAndRotation(1.5F, 1.0F, -0.9F, -0.3491F, 0.0F, -0.4189F));

        PartDefinition arm_r_grp = chest_grp.addOrReplaceChild("arm_r_grp", CubeListBuilder.create()
                .texOffs(0, 27).mirror().addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-2.0F)).mirror(false)
                .texOffs(0, 49).mirror().addBox(-3.5F, -0.2F, -3.5F, 7.0F, 8.0F, 7.0F, new CubeDeformation(-2.01F)).mirror(false),
                PartPose.offsetAndRotation(6.3F, -1.6F, 0.0F, -0.1047F, 0.0F, -0.3491F));
        PartDefinition forearm_r_grp = arm_r_grp.addOrReplaceChild("forearm_r_grp", CubeListBuilder.create()
                .texOffs(98, 49).mirror().addBox(-3.5F, -2.0F, -3.5F, 7.0F, 8.0F, 7.0F, new CubeDeformation(-2.0F)).mirror(false)
                .texOffs(25, 40).mirror().addBox(-4.0F, -0.6F, -4.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(-1.98F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 5.6F, 0.0F, -0.5934F, 0.0F, 0.1396F));
        PartDefinition hand_r_grp = forearm_r_grp.addOrReplaceChild("hand_r_grp", CubeListBuilder.create()
                .texOffs(100, 65).mirror().addBox(-3.0F, -1.7F, -2.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(-1.5F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 4.2F, 0.0F, -0.1745F, 0.0F, 0.0F));
        hand_r_grp.addOrReplaceChild("finger_2_r_r1", CubeListBuilder.create().texOffs(86, 72).mirror().addBox(-1.5F, -1.4F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.02F)).mirror(false), PartPose.offsetAndRotation(1.0F, 2.4F, 0.6F, -0.2094F, 0.0F, 0.4887F));
        hand_r_grp.addOrReplaceChild("finger_1_r_r1", CubeListBuilder.create().texOffs(86, 72).mirror().addBox(-1.5F, -1.4F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.01F)).mirror(false), PartPose.offsetAndRotation(1.0F, 2.4F, -0.6F, -0.2793F, 0.0F, 0.4887F));
        hand_r_grp.addOrReplaceChild("thumb_r_r1", CubeListBuilder.create().texOffs(86, 72).mirror().addBox(-1.8F, -1.4F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.01F)).mirror(false), PartPose.offsetAndRotation(-1.5F, 1.0F, -0.9F, -0.3491F, 0.0F, 0.4189F));

        PartDefinition ribs_grp = chest_grp.addOrReplaceChild("ribs_grp", CubeListBuilder.create()
                .texOffs(80, 22).addBox(-4.5F, -2.0F, -4.0F, 9.0F, 8.0F, 8.0F, new CubeDeformation(-2.01F))
                .texOffs(40, 0).addBox(-6.3F, -1.1F, -2.7F, 6.0F, 4.0F, 5.0F, new CubeDeformation(-1.5F))
                .texOffs(40, 0).addBox(-5.9F, 0.6F, -2.5F, 6.0F, 4.0F, 5.0F, new CubeDeformation(-1.51F))
                .texOffs(110, 34).addBox(-5.5F, 1.5F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(-1.5F))
                .texOffs(40, 0).mirror().addBox(0.3F, -1.1F, -2.7F, 6.0F, 4.0F, 5.0F, new CubeDeformation(-1.5F)).mirror(false)
                .texOffs(40, 0).mirror().addBox(-0.1F, 0.6F, -2.5F, 6.0F, 4.0F, 5.0F, new CubeDeformation(-1.51F)).mirror(false)
                .texOffs(110, 34).mirror().addBox(1.5F, 1.5F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(-1.5F)).mirror(false),
                PartPose.offset(0.0F, 1.8F, 0.0F));

        PartDefinition tail_1_grp = ribs_grp.addOrReplaceChild("tail_1_grp", CubeListBuilder.create()
                .texOffs(45, 22).addBox(-4.5F, -2.2F, -4.0F, 9.0F, 9.0F, 8.0F, new CubeDeformation(-2.02F)),
                PartPose.offsetAndRotation(0.0F, 3.8F, 0.0F, 0.1396F, 0.0F, 0.0698F));
        tail_1_grp.addOrReplaceChild("tail_wisp_1_r_r1", CubeListBuilder.create().texOffs(99, 77).mirror().addBox(-1.5F, -3.8F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.0F)).mirror(false), PartPose.offsetAndRotation(2.8F, 4.0F, 0.0F, 0.0F, 0.0F, 0.1396F));
        tail_1_grp.addOrReplaceChild("tail_wisp_1_l_r1", CubeListBuilder.create().texOffs(99, 77).addBox(-1.5F, -3.8F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(-2.8F, 3.6F, 0.0F, 0.0F, 0.0F, -0.1396F));

        PartDefinition tail_2_grp = tail_1_grp.addOrReplaceChild("tail_2_grp", CubeListBuilder.create()
                .texOffs(72, 39).addBox(-4.0F, -2.2F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-2.03F)),
                PartPose.offsetAndRotation(0.0F, 4.4F, 0.0F, 0.384F, 0.0F, -0.1745F));
        tail_2_grp.addOrReplaceChild("tail_wisp_2_r_r1", CubeListBuilder.create().texOffs(99, 77).mirror().addBox(-1.5F, -3.8F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.0F)).mirror(false), PartPose.offsetAndRotation(2.3F, 3.0F, 0.0F, 0.0F, 0.0F, 0.1745F));
        tail_2_grp.addOrReplaceChild("tail_wisp_2_l_r1", CubeListBuilder.create().texOffs(99, 77).addBox(-1.5F, -3.8F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(-2.3F, 3.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition tail_3_grp = tail_2_grp.addOrReplaceChild("tail_3_grp", CubeListBuilder.create()
                .texOffs(51, 49).addBox(-3.0F, -1.7F, -3.5F, 6.0F, 6.0F, 7.0F, new CubeDeformation(-1.54F)),
                PartPose.offsetAndRotation(0.0F, 3.6F, 0.2F, 0.733F, 0.0F, 0.2094F));

        PartDefinition tail_4_grp = tail_3_grp.addOrReplaceChild("tail_4_grp", CubeListBuilder.create()
                .texOffs(104, 0).addBox(-2.5F, -2.5F, -1.9F, 5.0F, 5.0F, 7.0F, new CubeDeformation(-1.55F)),
                PartPose.offsetAndRotation(0.0F, 2.0F, 1.6F, -0.4887F, 0.0F, 0.1396F));

        tail_4_grp.addOrReplaceChild("tail_5_grp", CubeListBuilder.create()
                .texOffs(69, 72).addBox(-1.5F, -1.5F, -1.2F, 3.0F, 3.0F, 5.0F, new CubeDeformation(-1.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 3.2F, 0.4887F, 0.0F, 0.0698F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(SoulfireDjinnRenderState state) {
        super.setupAnim(state);

        if (state.deathTime > 0.0F || state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        if (state.phaseGlideAnimationState.isStarted()) {
            this.phaseGlideAnimation.apply(state.phaseGlideAnimationState, state.ageInTicks);
        } else if (state.attackAnimationState.isStarted()) {
            this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        } else if (state.glideTakeoffAnimationState.isStarted()) {
            this.glideTakeoffAnimation.apply(state.glideTakeoffAnimationState, state.ageInTicks);
        } else if (state.glideLandingAnimationState.isStarted()) {
            this.glideLandingAnimation.apply(state.glideLandingAnimationState, state.ageInTicks);
        } else if (state.isGliding) {
            this.glideAnimation.apply(state.glideAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);

        this.head_grp.xRot += Mth.clamp(state.xRot, -28.0F, 28.0F) * Mth.DEG_TO_RAD * 0.60F;
        this.head_grp.yRot += Mth.clamp(state.yRot, -42.0F, 42.0F) * Mth.DEG_TO_RAD * 0.60F;
    }
}
