package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.ObsidianWendigoAnimations;
import ddraig.net.entropica.client.renderer.ObsidianWendigoRenderState;
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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Fauna 59 — Obsidian Wendigo (Forest Fiend).
 * Exact conversion of all 114 cubes, 26 bone groups, and 64x64 Box-UV grid (mapped onto 128x128 PNG)
 * from obsidian_wendigo.bbmodel, plus all 14 keyframe animations.
 */
public class ObsidianWendigoModel extends EntityModel<ObsidianWendigoRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "obsidian_wendigo"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkStartAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation walkStopAnimation;
    private final KeyframeAnimation runStartAnimation;
    private final KeyframeAnimation runAnimation;
    private final KeyframeAnimation runStopAnimation;
    private final KeyframeAnimation climbStartAnimation;
    private final KeyframeAnimation climbAnimation;
    private final KeyframeAnimation climbStopAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation screechAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public ObsidianWendigoModel(ModelPart modelRoot) {
        super(modelRoot, RenderType::entityCutoutNoCull);
        this.head_grp = modelRoot.getChild("root")
                .getChild("body_grp")
                .getChild("waist_grp")
                .getChild("chest_grp")
                .getChild("neck_1_grp")
                .getChild("neck_2_grp")
                .getChild("head_grp");

        this.idleAnimation = ObsidianWendigoAnimations.IDLE.bake(modelRoot);
        this.walkStartAnimation = ObsidianWendigoAnimations.WALK_START.bake(modelRoot);
        this.walkAnimation = ObsidianWendigoAnimations.WALK.bake(modelRoot);
        this.walkStopAnimation = ObsidianWendigoAnimations.WALK_STOP.bake(modelRoot);
        this.runStartAnimation = ObsidianWendigoAnimations.RUN_START.bake(modelRoot);
        this.runAnimation = ObsidianWendigoAnimations.RUN.bake(modelRoot);
        this.runStopAnimation = ObsidianWendigoAnimations.RUN_STOP.bake(modelRoot);
        this.climbStartAnimation = ObsidianWendigoAnimations.CLIMB_START.bake(modelRoot);
        this.climbAnimation = ObsidianWendigoAnimations.CLIMB.bake(modelRoot);
        this.climbStopAnimation = ObsidianWendigoAnimations.CLIMB_STOP.bake(modelRoot);
        this.attackAnimation = ObsidianWendigoAnimations.ATTACK.bake(modelRoot);
        this.screechAnimation = ObsidianWendigoAnimations.SCREECH.bake(modelRoot);
        this.hurtAnimation = ObsidianWendigoAnimations.HURT.bake(modelRoot);
        this.deathAnimation = ObsidianWendigoAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_grp = root.addOrReplaceChild("body_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -25.5F, 0.8F));

        PartDefinition pelvis_grp = body_grp.addOrReplaceChild("pelvis_grp", CubeListBuilder.create().texOffs(1, 48).addBox(-2.0F, -2.5F, -1.4F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        pelvis_grp.addOrReplaceChild("ischium_bar_l_r1", CubeListBuilder.create().texOffs(31, 45).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.1F, 0.9F, 0.0F, 0.0F, 0.0F, -0.2443F));
        pelvis_grp.addOrReplaceChild("iliac_wing_l_r1", CubeListBuilder.create().texOffs(31, 45).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.1F, -2.3F, -0.1F, 0.0F, 0.0F, 0.1745F));
        pelvis_grp.addOrReplaceChild("ischium_bar_r_r1", CubeListBuilder.create().texOffs(31, 45).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.1F, 0.9F, 0.0F, 0.0F, 0.0F, 0.2443F));
        pelvis_grp.addOrReplaceChild("iliac_wing_r_r1", CubeListBuilder.create().texOffs(31, 45).mirror().addBox(-1.5F, -2.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.1F, -2.3F, -0.1F, 0.0F, 0.0F, -0.1745F));

        PartDefinition leg_r_grp = body_grp.addOrReplaceChild("leg_r_grp", CubeListBuilder.create().texOffs(47, 15).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.3F, 0.5F, 0.0F));
        leg_r_grp.addOrReplaceChild("thigh_r_r1", CubeListBuilder.create().texOffs(47, 15).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2531F, 0.0F, 0.0F));

        PartDefinition shin_r_grp = leg_r_grp.addOrReplaceChild("shin_r_grp", CubeListBuilder.create().texOffs(16, 48).mirror().addBox(-1.5F, -1.6F, -1.3F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 9.0F, -2.3F));
        shin_r_grp.addOrReplaceChild("shin_r_r1", CubeListBuilder.create().texOffs(47, 15).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition ankle_r_grp = shin_r_grp.addOrReplaceChild("ankle_r_grp", CubeListBuilder.create().texOffs(47, 15).mirror().addBox(-1.0F, -1.1F, -0.8F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 7.8F, 4.5F));
        ankle_r_grp.addOrReplaceChild("dewclaw_r_r1", CubeListBuilder.create().texOffs(58, 1).mirror().addBox(-0.5F, -0.2F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 4.5F, -1.6F, 0.4887F, 0.0F, 0.0F));
        ankle_r_grp.addOrReplaceChild("ankle_r_r1", CubeListBuilder.create().texOffs(47, 15).mirror().addBox(-1.0F, -0.2F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4189F, 0.0F, 0.0F));

        PartDefinition hoof_r_grp = ankle_r_grp.addOrReplaceChild("hoof_r_grp", CubeListBuilder.create().texOffs(44, 45).mirror().addBox(-1.5F, 0.0F, -2.6F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 6.2F, -2.8F));
        hoof_r_grp.addOrReplaceChild("hoof_slope_r_r1", CubeListBuilder.create().texOffs(45, 46).mirror().addBox(-1.5F, -0.5F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -0.8F, 0.3142F, 0.0F, 0.0F));

        PartDefinition leg_l_grp = body_grp.addOrReplaceChild("leg_l_grp", CubeListBuilder.create().texOffs(47, 15).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.3F, 0.5F, 0.0F));
        leg_l_grp.addOrReplaceChild("thigh_l_r1", CubeListBuilder.create().texOffs(47, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2531F, 0.0F, 0.0F));

        PartDefinition shin_l_grp = leg_l_grp.addOrReplaceChild("shin_l_grp", CubeListBuilder.create().texOffs(16, 48).addBox(-1.5F, -1.6F, -1.3F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.0F, -2.3F));
        shin_l_grp.addOrReplaceChild("shin_l_r1", CubeListBuilder.create().texOffs(47, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition ankle_l_grp = shin_l_grp.addOrReplaceChild("ankle_l_grp", CubeListBuilder.create().texOffs(47, 15).addBox(-1.0F, -1.1F, -0.8F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.8F, 4.5F));
        ankle_l_grp.addOrReplaceChild("dewclaw_l_r1", CubeListBuilder.create().texOffs(58, 1).addBox(-0.5F, -0.2F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, -1.6F, 0.4887F, 0.0F, 0.0F));
        ankle_l_grp.addOrReplaceChild("ankle_l_r1", CubeListBuilder.create().texOffs(47, 15).addBox(-1.0F, -0.2F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4189F, 0.0F, 0.0F));

        PartDefinition hoof_l_grp = ankle_l_grp.addOrReplaceChild("hoof_l_grp", CubeListBuilder.create().texOffs(44, 45).addBox(-1.5F, 0.0F, -2.6F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.2F, -2.8F));
        hoof_l_grp.addOrReplaceChild("hoof_slope_l_r1", CubeListBuilder.create().texOffs(45, 46).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.8F, 0.3142F, 0.0F, 0.0F));

        PartDefinition waist_grp = body_grp.addOrReplaceChild("waist_grp", CubeListBuilder.create().texOffs(28, 28).addBox(-1.0F, -5.8F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(1, 48).addBox(-2.0F, -2.2F, -1.5F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(1, 48).addBox(-2.0F, -4.4F, -1.5F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(44, 52).addBox(-0.5F, -2.7F, 0.6F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(44, 52).addBox(-0.5F, -5.0F, 0.7F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.3F, 0.4F));

        PartDefinition chest_grp = waist_grp.addOrReplaceChild("chest_grp", CubeListBuilder.create().texOffs(28, 28).addBox(-1.0F, -8.0F, -4.9F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(31, 45).mirror().addBox(5.3F, -10.0F, -2.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(31, 45).addBox(-8.3F, -10.0F, -2.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.4F, 0.0F));

        chest_grp.addOrReplaceChild("clavicle_l_r1", CubeListBuilder.create().texOffs(27, 53).addBox(-6.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(-0.9F, -8.3F, -3.6F, 0.0F, 0.384F, 0.0698F));
        chest_grp.addOrReplaceChild("scapula_l_r1", CubeListBuilder.create().texOffs(53, 52).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.1F, -5.4F, 1.3F, -0.1396F, -0.1745F, -0.1396F));
        chest_grp.addOrReplaceChild("rib_s4_l_r1", CubeListBuilder.create().texOffs(2, 55).addBox(-2.0F, -0.5F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -0.9F, -1.1F, 0.0F, -1.5708F, -0.1047F));
        chest_grp.addOrReplaceChild("rib_f4_l_r1", CubeListBuilder.create().texOffs(2, 55).addBox(-4.0F, -0.5F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -0.9F, -3.7F, -0.1047F, 0.1745F, -0.1047F));
        chest_grp.addOrReplaceChild("rib_s3_l_r1", CubeListBuilder.create().texOffs(1, 55).addBox(-2.5F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.3F, -2.9F, -1.1F, 0.0F, -1.5708F, -0.1047F));
        chest_grp.addOrReplaceChild("rib_f3_l_r1", CubeListBuilder.create().texOffs(1, 55).addBox(-5.0F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -2.9F, -3.9F, -0.1047F, 0.1396F, 0.0F));
        chest_grp.addOrReplaceChild("rib_s2_l_r1", CubeListBuilder.create().texOffs(1, 55).addBox(-2.5F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.3F, -4.9F, -1.1F, 0.0F, -1.5708F, -0.1047F));
        chest_grp.addOrReplaceChild("rib_f2_l_r1", CubeListBuilder.create().texOffs(1, 55).addBox(-5.0F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -4.9F, -3.9F, -0.1047F, 0.1396F, 0.0F));
        chest_grp.addOrReplaceChild("rib_s1_l_r1", CubeListBuilder.create().texOffs(1, 55).addBox(-2.5F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.3F, -6.9F, -1.1F, 0.0F, -1.5708F, -0.1047F));
        chest_grp.addOrReplaceChild("rib_f1_l_r1", CubeListBuilder.create().texOffs(1, 55).addBox(-5.0F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -6.9F, -3.9F, -0.1047F, 0.1396F, 0.0F));

        chest_grp.addOrReplaceChild("clavicle_r_r1", CubeListBuilder.create().texOffs(27, 53).mirror().addBox(0.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(0.9F, -8.3F, -3.6F, 0.0F, -0.384F, -0.0698F));
        chest_grp.addOrReplaceChild("scapula_r_r1", CubeListBuilder.create().texOffs(53, 52).mirror().addBox(-1.5F, -2.0F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.1F, -5.4F, 1.3F, -0.1396F, 0.1745F, 0.1396F));
        chest_grp.addOrReplaceChild("rib_s4_r_r1", CubeListBuilder.create().texOffs(2, 55).mirror().addBox(-2.0F, -0.5F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.5F, -0.9F, -1.1F, 0.0F, 1.5708F, 0.1047F));
        chest_grp.addOrReplaceChild("rib_f4_r_r1", CubeListBuilder.create().texOffs(2, 55).mirror().addBox(0.0F, -0.5F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, -0.9F, -3.7F, -0.1047F, -0.1745F, 0.1047F));
        chest_grp.addOrReplaceChild("rib_s3_r_r1", CubeListBuilder.create().texOffs(1, 55).mirror().addBox(-2.5F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.3F, -2.9F, -1.1F, 0.0F, 1.5708F, 0.1047F));
        chest_grp.addOrReplaceChild("rib_f3_r_r1", CubeListBuilder.create().texOffs(1, 55).mirror().addBox(0.0F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, -2.9F, -3.9F, -0.1047F, -0.1396F, 0.0F));
        chest_grp.addOrReplaceChild("rib_s2_r_r1", CubeListBuilder.create().texOffs(1, 55).mirror().addBox(-2.5F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.3F, -4.9F, -1.1F, 0.0F, 1.5708F, 0.1047F));
        chest_grp.addOrReplaceChild("rib_f2_r_r1", CubeListBuilder.create().texOffs(1, 55).mirror().addBox(0.0F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, -4.9F, -3.9F, -0.1047F, -0.1396F, 0.0F));
        chest_grp.addOrReplaceChild("rib_s1_r_r1", CubeListBuilder.create().texOffs(1, 55).mirror().addBox(-2.5F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.3F, -6.9F, -1.1F, 0.0F, 1.5708F, 0.1047F));
        chest_grp.addOrReplaceChild("rib_f1_r_r1", CubeListBuilder.create().texOffs(1, 55).mirror().addBox(0.0F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, -6.9F, -3.9F, -0.1047F, -0.1396F, 0.0F));

        chest_grp.addOrReplaceChild("dorsal_spike_4_r1", CubeListBuilder.create().texOffs(44, 52).addBox(-0.5F, -1.0F, -1.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(-0.09F)), PartPose.offsetAndRotation(0.0F, -7.7F, 0.75F, -0.2793F, 0.0F, 0.0F));
        chest_grp.addOrReplaceChild("dorsal_spike_3_r1", CubeListBuilder.create().texOffs(44, 52).addBox(-0.5F, -1.0F, -1.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(-0.06F)), PartPose.offsetAndRotation(0.0F, -5.8F, 0.83F, -0.2443F, 0.0F, 0.0F));
        chest_grp.addOrReplaceChild("dorsal_spike_2_r1", CubeListBuilder.create().texOffs(44, 52).addBox(-0.5F, -1.0F, -1.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(-0.03F)), PartPose.offsetAndRotation(0.0F, -3.6F, 0.92F, -0.2094F, 0.0F, 0.0F));
        chest_grp.addOrReplaceChild("dorsal_spike_1_r1", CubeListBuilder.create().texOffs(44, 52).addBox(-0.5F, -1.0F, -1.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.4F, 1.01F, -0.1745F, 0.0F, 0.0F));
        chest_grp.addOrReplaceChild("thoracic_spine_r1", CubeListBuilder.create().texOffs(28, 28).addBox(-1.0F, -8.64F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.04F)), PartPose.offsetAndRotation(0.0F, 0.6F, 0.05F, 0.0416F, 0.0F, 0.0F));

        PartDefinition arm_r_grp = chest_grp.addOrReplaceChild("arm_r_grp", CubeListBuilder.create().texOffs(47, 15).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(31, 45).mirror().addBox(-1.5F, 8.8F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(7.2F, -8.0F, -1.2F, 0.1396F, 0.0F, -0.1396F));
        PartDefinition forearm_r_grp = arm_r_grp.addOrReplaceChild("forearm_r_grp", CubeListBuilder.create().texOffs(47, 1).mirror().addBox(-1.0F, 0.0F, -1.5F, 2.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 10.2F, 0.0F, -0.4538F, 0.0F, 0.0524F));
        PartDefinition hand_r_grp = forearm_r_grp.addOrReplaceChild("hand_r_grp", CubeListBuilder.create().texOffs(50, 28).mirror().addBox(-1.0F, 0.0F, -1.9F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 9.8F, 0.0F, 0.1745F, 0.0F, 0.1745F));
        hand_r_grp.addOrReplaceChild("claw_ring_r_r1", CubeListBuilder.create().texOffs(58, 1).mirror().addBox(-0.5F, -0.2F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(58, 1).mirror().addBox(-0.5F, -0.2F, -1.9F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(58, 1).mirror().addBox(-0.5F, -0.2F, -3.3F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.1F, 2.8F, 1.5F, 0.0F, 0.0F, 0.3142F));
        hand_r_grp.addOrReplaceChild("thumb_r_r1", CubeListBuilder.create().texOffs(58, 1).mirror().addBox(-0.5F, -0.4F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.8F, 2.0F, -1.8F, 0.384F, 0.0F, 0.3491F));

        PartDefinition arm_l_grp = chest_grp.addOrReplaceChild("arm_l_grp", CubeListBuilder.create().texOffs(47, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(31, 45).addBox(-1.5F, 8.8F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.2F, -8.0F, -1.2F, 0.1396F, 0.0F, 0.1396F));
        PartDefinition forearm_l_grp = arm_l_grp.addOrReplaceChild("forearm_l_grp", CubeListBuilder.create().texOffs(47, 1).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.2F, 0.0F, -0.4538F, 0.0F, -0.0524F));
        PartDefinition hand_l_grp = forearm_l_grp.addOrReplaceChild("hand_l_grp", CubeListBuilder.create().texOffs(50, 28).addBox(-1.0F, 0.0F, -1.9F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 9.8F, 0.0F, 0.1745F, 0.0F, -0.1745F));
        hand_l_grp.addOrReplaceChild("claw_ring_l_r1", CubeListBuilder.create().texOffs(58, 1).addBox(-0.5F, -0.2F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(58, 1).addBox(-0.5F, -0.2F, -1.9F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(58, 1).addBox(-0.5F, -0.2F, -3.3F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, 2.8F, 1.5F, 0.0F, 0.0F, -0.3142F));
        hand_l_grp.addOrReplaceChild("thumb_l_r1", CubeListBuilder.create().texOffs(58, 1).addBox(-0.5F, -0.4F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8F, 2.0F, -1.8F, 0.384F, 0.0F, -0.3491F));

        PartDefinition neck_1_grp = chest_grp.addOrReplaceChild("neck_1_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -7.8F, -0.3F));
        neck_1_grp.addOrReplaceChild("neck_spike_2_r1", CubeListBuilder.create().texOffs(44, 52).addBox(-0.5F, -4.2F, -1.1F, 1.0F, 2.0F, 3.0F, new CubeDeformation(-0.22F))
                .texOffs(44, 52).addBox(-0.5F, -2.3F, -1.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(-0.18F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6074F, 0.0F, 0.0F));
        neck_1_grp.addOrReplaceChild("neck_lower_r1", CubeListBuilder.create().texOffs(28, 28).addBox(-1.0F, -4.617F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.06F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.607F, 0.0F, 0.0F));

        PartDefinition neck_2_grp = neck_1_grp.addOrReplaceChild("neck_2_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -3.6F, -2.5F));
        neck_2_grp.addOrReplaceChild("neck_spike_3_r1", CubeListBuilder.create().texOffs(44, 52).addBox(-0.5F, -2.2F, -1.2F, 1.0F, 2.0F, 3.0F, new CubeDeformation(-0.28F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.829F, 0.0F, 0.0F));
        neck_2_grp.addOrReplaceChild("neck_upper_r1", CubeListBuilder.create().texOffs(28, 28).addBox(-1.0F, -3.558F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.14F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8289F, 0.0F, 0.0F));

        PartDefinition head_grp = neck_2_grp.addOrReplaceChild("head_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -1.9F, -2.6F));

        PartDefinition skull_grp = head_grp.addOrReplaceChild("skull_grp", CubeListBuilder.create().texOffs(1, 28).addBox(-3.5F, -3.0F, -4.6F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(37, 28).mirror().addBox(2.8F, -0.7F, -4.8F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(37, 28).addBox(-3.8F, -0.7F, -4.8F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.05F))
                .texOffs(1, 18).addBox(-2.5F, -1.8F, -9.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(16, 40).addBox(-1.5F, -1.0F, -12.4F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4189F, 0.0F, 0.0F));

        skull_grp.addOrReplaceChild("jaw_grp", CubeListBuilder.create().texOffs(37, 37).addBox(-2.0F, -0.2F, -4.6F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(1, 40).addBox(-1.0F, -0.2F, -9.2F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -2.2F));

        PartDefinition antler_r_grp = head_grp.addOrReplaceChild("antler_r_grp", CubeListBuilder.create(), PartPose.offset(2.0F, -1.1F, -1.0F));
        antler_r_grp.addOrReplaceChild("ant_spur_2_r_r1", CubeListBuilder.create().texOffs(26, 15).mirror().addBox(-2.5F, -4.571F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-2.06F)).mirror(false), PartPose.offsetAndRotation(13.95F, -7.85F, 6.1F, -2.9003F, 0.0F, -1.301F));
        antler_r_grp.addOrReplaceChild("ant_crown_3_r_r1", CubeListBuilder.create().texOffs(26, 15).mirror().addBox(-2.5F, -4.517F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-2.08F)).mirror(false), PartPose.offsetAndRotation(10.6F, -13.8F, 6.7F, -1.5708F, -1.4474F, 1.0516F));
        antler_r_grp.addOrReplaceChild("ant_crown_2_r_r1", CubeListBuilder.create().texOffs(26, 1).mirror().addBox(-2.5F, -5.798F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.94F)).mirror(false), PartPose.offsetAndRotation(12.2F, -10.5F, 6.1F, -0.1622F, 0.0F, -0.4514F));
        antler_r_grp.addOrReplaceChild("ant_crown_1_r_r1", CubeListBuilder.create().texOffs(26, 1).mirror().addBox(-2.5F, -5.854F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.82F)).mirror(false), PartPose.offsetAndRotation(12.5F, -6.7F, 5.5F, -1.5708F, 1.4147F, -1.6496F));
        antler_r_grp.addOrReplaceChild("ant_mid_2_r_r1", CubeListBuilder.create().texOffs(26, 15).mirror().addBox(-2.5F, -4.538F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-2.06F)).mirror(false), PartPose.offsetAndRotation(7.9F, -8.3F, 4.2F, -1.5708F, 1.4494F, -2.0344F));
        antler_r_grp.addOrReplaceChild("ant_mid_1_r_r1", CubeListBuilder.create().texOffs(26, 1).mirror().addBox(-2.5F, -5.805F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.88F)).mirror(false), PartPose.offsetAndRotation(8.3F, -4.6F, 3.7F, -3.008F, 0.0F, 3.0339F));
        antler_r_grp.addOrReplaceChild("ant_spur_1_r_r1", CubeListBuilder.create().texOffs(26, 15).mirror().addBox(-2.5F, -4.567F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-2.04F)).mirror(false), PartPose.offsetAndRotation(6.15F, -3.65F, 2.75F, -1.5708F, 1.3514F, 0.3089F));
        antler_r_grp.addOrReplaceChild("ant_brow_2_r_r1", CubeListBuilder.create().texOffs(26, 15).mirror().addBox(-2.5F, -4.528F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-2.06F)).mirror(false), PartPose.offsetAndRotation(4.1F, -6.3F, 1.5F, -1.5708F, -1.4897F, 1.1071F));
        antler_r_grp.addOrReplaceChild("ant_brow_1_r_r1", CubeListBuilder.create().texOffs(26, 1).mirror().addBox(-2.5F, -5.725F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.92F)).mirror(false), PartPose.offsetAndRotation(4.0F, -2.7F, 1.8F, 0.0831F, 0.0F, 0.0278F));
        antler_r_grp.addOrReplaceChild("ant_out_2_r_r1", CubeListBuilder.create().texOffs(26, 15).mirror().addBox(-2.5F, -4.562F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-2.08F)).mirror(false), PartPose.offsetAndRotation(16.1F, -12.5F, 7.2F, -3.0609F, 0.0F, 2.8966F));
        antler_r_grp.addOrReplaceChild("ant_out_1_r_r1", CubeListBuilder.create().texOffs(26, 1).mirror().addBox(-2.5F, -5.731F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.96F)).mirror(false), PartPose.offsetAndRotation(15.4F, -9.0F, 6.7F, -1.5708F, -1.4316F, 1.7682F));
        antler_r_grp.addOrReplaceChild("ant_main_4_r_r1", CubeListBuilder.create().texOffs(26, 1).mirror().addBox(-2.5F, -5.881F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.84F)).mirror(false), PartPose.offsetAndRotation(12.5F, -6.7F, 5.5F, -1.5708F, 1.2573F, -0.6705F));
        antler_r_grp.addOrReplaceChild("ant_main_3_r_r1", CubeListBuilder.create().texOffs(1, 1).mirror().addBox(-3.0F, -7.447F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(-2.26F)).mirror(false), PartPose.offsetAndRotation(8.3F, -4.6F, 3.7F, -2.7755F, 0.0F, -2.0344F));
        antler_r_grp.addOrReplaceChild("ant_main_2_r_r1", CubeListBuilder.create().texOffs(1, 1).mirror().addBox(-3.0F, -7.45F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(-2.18F)).mirror(false), PartPose.offsetAndRotation(4.0F, -2.7F, 1.8F, -1.5708F, -1.1867F, 2.7255F));
        antler_r_grp.addOrReplaceChild("ant_main_1_r_r1", CubeListBuilder.create().texOffs(1, 1).mirror().addBox(-3.0F, -7.54F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(-2.06F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.2F, 0.0F, -0.3494F, 0.0F, 0.9435F));

        PartDefinition antler_l_grp = head_grp.addOrReplaceChild("antler_l_grp", CubeListBuilder.create(), PartPose.offset(-2.0F, -1.1F, -1.0F));
        antler_l_grp.addOrReplaceChild("ant_spur_2_l_r1", CubeListBuilder.create().texOffs(26, 15).addBox(-2.5F, -4.571F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-2.06F)), PartPose.offsetAndRotation(-13.95F, -7.85F, 6.1F, -2.9003F, 0.0F, 1.301F));
        antler_l_grp.addOrReplaceChild("ant_crown_3_l_r1", CubeListBuilder.create().texOffs(26, 15).addBox(-2.5F, -4.517F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-2.08F)), PartPose.offsetAndRotation(-10.6F, -13.8F, 6.7F, -1.5708F, -1.4474F, 2.0899F));
        antler_l_grp.addOrReplaceChild("ant_crown_2_l_r1", CubeListBuilder.create().texOffs(26, 1).addBox(-2.5F, -5.798F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.94F)), PartPose.offsetAndRotation(-12.2F, -10.5F, 6.1F, -0.1622F, 0.0F, 0.4514F));
        antler_l_grp.addOrReplaceChild("ant_crown_1_l_r1", CubeListBuilder.create().texOffs(26, 1).addBox(-2.5F, -5.854F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.82F)), PartPose.offsetAndRotation(-12.5F, -6.7F, 5.5F, -1.5708F, 1.4147F, -1.492F));
        antler_l_grp.addOrReplaceChild("ant_mid_2_l_r1", CubeListBuilder.create().texOffs(26, 15).addBox(-2.5F, -4.538F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-2.06F)), PartPose.offsetAndRotation(-7.9F, -8.3F, 4.2F, -1.5708F, 1.4494F, -1.1071F));
        antler_l_grp.addOrReplaceChild("ant_mid_1_l_r1", CubeListBuilder.create().texOffs(26, 1).addBox(-2.5F, -5.805F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.88F)), PartPose.offsetAndRotation(-8.3F, -4.6F, 3.7F, -3.008F, 0.0F, -3.0339F));
        antler_l_grp.addOrReplaceChild("ant_spur_1_l_r1", CubeListBuilder.create().texOffs(26, 15).addBox(-2.5F, -4.567F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-2.04F)), PartPose.offsetAndRotation(-6.15F, -3.65F, 2.75F, -1.5708F, 1.3514F, 2.8327F));
        antler_l_grp.addOrReplaceChild("ant_brow_2_l_r1", CubeListBuilder.create().texOffs(26, 15).addBox(-2.5F, -4.528F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-2.06F)), PartPose.offsetAndRotation(-4.1F, -6.3F, 1.5F, -1.5708F, -1.4897F, 2.0344F));
        antler_l_grp.addOrReplaceChild("ant_brow_1_l_r1", CubeListBuilder.create().texOffs(26, 1).addBox(-2.5F, -5.725F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.92F)), PartPose.offsetAndRotation(-4.0F, -2.7F, 1.8F, 0.0831F, 0.0F, -0.0278F));
        antler_l_grp.addOrReplaceChild("ant_out_2_l_r1", CubeListBuilder.create().texOffs(26, 15).addBox(-2.5F, -4.562F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-2.08F)), PartPose.offsetAndRotation(-16.1F, -12.5F, 7.2F, -3.0609F, 0.0F, -2.8966F));
        antler_l_grp.addOrReplaceChild("ant_out_1_l_r1", CubeListBuilder.create().texOffs(26, 1).addBox(-2.5F, -5.731F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.96F)), PartPose.offsetAndRotation(-15.4F, -9.0F, 6.7F, -1.5708F, -1.4316F, 1.3734F));
        antler_l_grp.addOrReplaceChild("ant_main_4_l_r1", CubeListBuilder.create().texOffs(26, 1).addBox(-2.5F, -5.881F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.84F)), PartPose.offsetAndRotation(-12.5F, -6.7F, 5.5F, -1.5708F, 1.2573F, -2.4711F));
        antler_l_grp.addOrReplaceChild("ant_main_3_l_r1", CubeListBuilder.create().texOffs(1, 1).addBox(-3.0F, -7.447F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(-2.26F)), PartPose.offsetAndRotation(-8.3F, -4.6F, 3.7F, -2.7755F, 0.0F, 2.0344F));
        antler_l_grp.addOrReplaceChild("ant_main_2_l_r1", CubeListBuilder.create().texOffs(1, 1).addBox(-3.0F, -7.45F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(-2.18F)), PartPose.offsetAndRotation(-4.0F, -2.7F, 1.8F, -1.5708F, -1.1867F, 0.4161F));
        antler_l_grp.addOrReplaceChild("ant_main_1_l_r1", CubeListBuilder.create().texOffs(1, 1).addBox(-3.0F, -7.54F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(-2.06F)), PartPose.offsetAndRotation(0.0F, 0.2F, 0.0F, -0.3494F, 0.0F, -0.9435F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(ObsidianWendigoRenderState state) {
        super.setupAnim(state);

        if (state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        // Mutually-exclusive locomotion state machine (at most one active per tick)
        this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        this.walkStartAnimation.apply(state.walkStartAnimationState, state.ageInTicks);
        this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        this.walkStopAnimation.apply(state.walkStopAnimationState, state.ageInTicks);
        this.runStartAnimation.apply(state.runStartAnimationState, state.ageInTicks);
        this.runAnimation.apply(state.runAnimationState, state.ageInTicks);
        this.runStopAnimation.apply(state.runStopAnimationState, state.ageInTicks);
        this.climbStartAnimation.apply(state.climbStartAnimationState, state.ageInTicks);
        this.climbAnimation.apply(state.climbAnimationState, state.ageInTicks);
        this.climbStopAnimation.apply(state.climbStopAnimationState, state.ageInTicks);

        // Combat & reaction overlays
        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        this.screechAnimation.apply(state.screechAnimationState, state.ageInTicks);
        this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);

        if (!state.screechAnimationState.isStarted() && !state.attackAnimationState.isStarted()) {
            float yawRad = Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD;
            float pitchRad = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
            this.head_grp.yRot += yawRad * 0.55F;
            this.head_grp.xRot += pitchRad * 0.50F;
        }
    }
}
