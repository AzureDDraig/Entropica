package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.ObsidianScorpioidAnimations;
import ddraig.net.entropica.client.renderer.ObsidianScorpioidRenderState;
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
 * Fauna 53 — Obsidian Scorpioid (Lava Beast).
 * Exact conversion of all 50 cubes, 44 bone groups, and 128x128 UVs from obsidian_scorpioid.bbmodel.
 */
public class ObsidianScorpioidModel extends EntityModel<ObsidianScorpioidRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "obsidian_scorpioid"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart body_grp;
    private final ModelPart head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation pincerClampAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public ObsidianScorpioidModel(ModelPart modelRoot) {
        super(modelRoot);
        ModelPart root = modelRoot.getChild("root");
        this.body_grp = root.getChild("body_grp");
        this.head_grp = this.body_grp.getChild("thorax_grp").getChild("head_grp");

        this.idleAnimation = ObsidianScorpioidAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = ObsidianScorpioidAnimations.WALK.bake(modelRoot);
        this.attackAnimation = ObsidianScorpioidAnimations.ATTACK.bake(modelRoot);
        this.pincerClampAnimation = ObsidianScorpioidAnimations.PINCER_CLAMP.bake(modelRoot);
        this.hurtAnimation = ObsidianScorpioidAnimations.HURT.bake(modelRoot);
        this.deathAnimation = ObsidianScorpioidAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_grp = root.addOrReplaceChild("body_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -9.378F, -1.0F));

        PartDefinition thorax_grp = body_grp.addOrReplaceChild("thorax_grp", CubeListBuilder.create()
                .texOffs(46, 21).addBox(-3.5F, 0.7F, -6.0F, 7.0F, 2.0F, 12.0F, new CubeDeformation(-0.02F))
                .texOffs(84, 0).addBox(-6.5F, -5.5F, -9.0F, 13.0F, 9.0F, 9.0F, new CubeDeformation(-2.0F))
                .texOffs(0, 21).addBox(-7.0F, -5.8F, -4.3F, 14.0F, 9.0F, 9.0F, new CubeDeformation(-1.98F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition abdomen_grp = thorax_grp.addOrReplaceChild("abdomen_grp", CubeListBuilder.create().texOffs(84, 18).addBox(-6.5F, -4.7F, -2.2F, 13.0F, 9.0F, 9.0F, new CubeDeformation(-2.0F)), PartPose.offsetAndRotation(0.0F, -1.3F, 2.5F, -0.1396F, 0.0F, 0.0F));

        PartDefinition tail_1_grp = abdomen_grp.addOrReplaceChild("tail_1_grp", CubeListBuilder.create().texOffs(0, 72).addBox(-4.5F, -5.2F, -4.0F, 9.0F, 7.0F, 8.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, -0.4F, 4.0F, -0.8029F, 0.0F, 0.0F));
        PartDefinition tail_2_grp = tail_1_grp.addOrReplaceChild("tail_2_grp", CubeListBuilder.create().texOffs(0, 102).addBox(-4.0F, -5.2F, -3.5F, 8.0F, 7.0F, 7.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, -4.2F, 0.0F, 0.4189F, 0.0F, 0.0F));
        PartDefinition tail_3_grp = tail_2_grp.addOrReplaceChild("tail_3_grp", CubeListBuilder.create().texOffs(28, 87).addBox(-4.0F, -5.2F, -3.5F, 8.0F, 7.0F, 7.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, -4.2F, 0.0F, 0.4538F, 0.0F, 0.0F));
        PartDefinition tail_4_grp = tail_3_grp.addOrReplaceChild("tail_4_grp", CubeListBuilder.create().texOffs(34, 72).addBox(-3.5F, -5.2F, -3.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, -4.2F, 0.0F, 0.4887F, 0.0F, 0.0F));
        PartDefinition tail_5_grp = tail_4_grp.addOrReplaceChild("tail_5_grp", CubeListBuilder.create().texOffs(30, 101).addBox(-3.5F, -5.2F, -3.0F, 7.0F, 7.0F, 6.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, -4.2F, 0.0F, 0.5236F, 0.0F, 0.0F));
        PartDefinition tail_6_grp = tail_5_grp.addOrReplaceChild("tail_6_grp", CubeListBuilder.create().texOffs(30, 114).addBox(-3.0F, -5.2F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, -4.2F, 0.0F, 0.4887F, 0.0F, 0.0F));

        PartDefinition stinger_grp = tail_6_grp.addOrReplaceChild("stinger_grp", CubeListBuilder.create().texOffs(86, 36).addBox(-4.5F, -7.4F, -4.5F, 9.0F, 10.0F, 9.0F, new CubeDeformation(-2.0F)), PartPose.offsetAndRotation(0.0F, -4.2F, 0.0F, 0.5585F, 0.0F, 0.0F));
        stinger_grp.addOrReplaceChild("stinger_spike_tip_r1", CubeListBuilder.create()
                .texOffs(56, 101).addBox(-2.5F, -9.2F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-1.95F))
                .texOffs(0, 87).addBox(-3.5F, -5.8F, -3.5F, 7.0F, 8.0F, 7.0F, new CubeDeformation(-2.15F)),
                PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

        PartDefinition head_grp = thorax_grp.addOrReplaceChild("head_grp", CubeListBuilder.create().texOffs(46, 36).addBox(-5.5F, -4.4F, -6.6F, 11.0F, 9.0F, 9.0F, new CubeDeformation(-2.0F)), PartPose.offset(0.0F, -0.5F, -6.8F));

        PartDefinition fang_l_grp = head_grp.addOrReplaceChild("fang_l_grp", CubeListBuilder.create().texOffs(108, 81).addBox(-2.0F, -2.0F, -2.8F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-1.02F)), PartPose.offset(-1.6F, 2.0F, -4.4F));
        fang_l_grp.addOrReplaceChild("fang_tip_l_r1", CubeListBuilder.create().texOffs(78, 114).addBox(-1.5F, -1.2F, -2.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.6F, -1.0F, 0.2443F, 0.0F, 0.0F));

        PartDefinition fang_r_grp = head_grp.addOrReplaceChild("fang_r_grp", CubeListBuilder.create().texOffs(108, 55).addBox(-2.0F, -2.0F, -2.8F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-1.02F)), PartPose.offset(1.6F, 2.0F, -4.4F));
        fang_r_grp.addOrReplaceChild("fang_tip_r_r1", CubeListBuilder.create().texOffs(92, 114).addBox(-1.5F, -1.2F, -2.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.6F, -1.0F, 0.2443F, 0.0F, 0.0F));

        PartDefinition pedipalp_l_grp = thorax_grp.addOrReplaceChild("pedipalp_l_grp", CubeListBuilder.create().texOffs(0, 116).addBox(-2.5F, -2.5F, -6.0F, 5.0F, 5.0F, 7.0F, new CubeDeformation(-1.02F)), PartPose.offsetAndRotation(-4.5F, 0.3F, -5.2F, 0.0F, 0.8378F, 0.0F));
        PartDefinition claw_l_grp = pedipalp_l_grp.addOrReplaceChild("claw_l_grp", CubeListBuilder.create()
                .texOffs(58, 86).addBox(-2.5F, -2.5F, -4.5F, 5.0F, 5.0F, 6.0F, new CubeDeformation(-1.04F))
                .texOffs(0, 0).addBox(-5.0F, -5.0F, -11.7F, 10.0F, 10.0F, 11.0F, new CubeDeformation(-2.0F))
                .texOffs(0, 39).addBox(-4.0F, -5.0F, -17.3F, 8.0F, 7.0F, 10.0F, new CubeDeformation(-2.02F))
                .texOffs(106, 71).addBox(-2.0F, -1.3F, -15.8F, 4.0F, 3.0F, 7.0F, new CubeDeformation(-1.04F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -4.6F, -0.0698F, -0.733F, -0.1745F));
        claw_l_grp.addOrReplaceChild("pincer_lower_l_grp", CubeListBuilder.create()
                .texOffs(0, 56).addBox(-4.0F, -3.0F, -8.0F, 8.0F, 6.0F, 10.0F, new CubeDeformation(-2.03F))
                .texOffs(84, 71).addBox(-2.0F, -2.7F, -6.5F, 4.0F, 3.0F, 7.0F, new CubeDeformation(-1.05F)),
                PartPose.offsetAndRotation(0.0F, 2.0F, -9.3F, 0.2443F, 0.0F, 0.0F));

        PartDefinition pedipalp_r_grp = thorax_grp.addOrReplaceChild("pedipalp_r_grp", CubeListBuilder.create().texOffs(54, 114).addBox(-2.5F, -2.5F, -6.0F, 5.0F, 5.0F, 7.0F, new CubeDeformation(-1.02F)), PartPose.offsetAndRotation(4.5F, 0.3F, -5.2F, 0.0F, -0.8378F, 0.0F));
        PartDefinition claw_r_grp = pedipalp_r_grp.addOrReplaceChild("claw_r_grp", CubeListBuilder.create()
                .texOffs(62, 71).addBox(-2.5F, -2.5F, -4.5F, 5.0F, 5.0F, 6.0F, new CubeDeformation(-1.04F))
                .texOffs(42, 0).addBox(-5.0F, -5.0F, -11.7F, 10.0F, 10.0F, 11.0F, new CubeDeformation(-2.0F))
                .texOffs(36, 54).addBox(-4.0F, -5.0F, -17.3F, 8.0F, 7.0F, 10.0F, new CubeDeformation(-2.02F))
                .texOffs(106, 71).mirror().addBox(-2.0F, -1.3F, -15.8F, 4.0F, 3.0F, 7.0F, new CubeDeformation(-1.04F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, -4.6F, -0.0698F, 0.733F, 0.1745F));
        claw_r_grp.addOrReplaceChild("pincer_lower_r_grp", CubeListBuilder.create()
                .texOffs(72, 55).addBox(-4.0F, -3.0F, -8.0F, 8.0F, 6.0F, 10.0F, new CubeDeformation(-2.03F))
                .texOffs(84, 71).mirror().addBox(-2.0F, -2.7F, -6.5F, 4.0F, 3.0F, 7.0F, new CubeDeformation(-1.05F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 2.0F, -9.3F, 0.2443F, 0.0F, 0.0F));

        PartDefinition hip_1_l_grp = thorax_grp.addOrReplaceChild("hip_1_l_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.4F, 0.5F, -4.2F, 0.0F, -0.384F, 0.0F));
        PartDefinition femur_1_l_grp = hip_1_l_grp.addOrReplaceChild("femur_1_l_grp", CubeListBuilder.create().texOffs(104, 90).addBox(-6.6F, -2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));
        PartDefinition tibia_1_l_grp = femur_1_l_grp.addOrReplaceChild("tibia_1_l_grp", CubeListBuilder.create().texOffs(76, 97).addBox(-8.6F, -2.0F, -2.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-1.05F)), PartPose.offsetAndRotation(-5.6F, 0.0F, 0.0F, 0.0F, 0.0F, -1.4835F));
        tibia_1_l_grp.addOrReplaceChild("tarsus_1_l_grp", CubeListBuilder.create().texOffs(108, 63).addBox(-5.6F, -1.5F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(-0.95F)), PartPose.offsetAndRotation(-7.6F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3142F));

        PartDefinition hip_1_r_grp = thorax_grp.addOrReplaceChild("hip_1_r_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(4.4F, 0.5F, -4.2F, 0.0F, 0.384F, 0.0F));
        PartDefinition femur_1_r_grp = hip_1_r_grp.addOrReplaceChild("femur_1_r_grp", CubeListBuilder.create().texOffs(104, 90).mirror().addBox(-1.4F, -2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(-1.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));
        PartDefinition tibia_1_r_grp = femur_1_r_grp.addOrReplaceChild("tibia_1_r_grp", CubeListBuilder.create().texOffs(76, 97).mirror().addBox(-1.4F, -2.0F, -2.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-1.05F)).mirror(false), PartPose.offsetAndRotation(5.6F, 0.0F, 0.0F, 0.0F, 0.0F, 1.4835F));
        tibia_1_r_grp.addOrReplaceChild("tarsus_1_r_grp", CubeListBuilder.create().texOffs(108, 63).mirror().addBox(-1.4F, -1.5F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(-0.95F)).mirror(false), PartPose.offsetAndRotation(7.6F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3142F));

        PartDefinition hip_2_l_grp = thorax_grp.addOrReplaceChild("hip_2_l_grp", CubeListBuilder.create(), PartPose.offset(-4.4F, 0.5F, 0.4F));
        PartDefinition femur_2_l_grp = hip_2_l_grp.addOrReplaceChild("femur_2_l_grp", CubeListBuilder.create().texOffs(104, 98).addBox(-6.6F, -2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));
        PartDefinition tibia_2_l_grp = femur_2_l_grp.addOrReplaceChild("tibia_2_l_grp", CubeListBuilder.create().texOffs(76, 105).addBox(-8.6F, -2.0F, -2.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-1.05F)), PartPose.offsetAndRotation(-5.6F, 0.0F, 0.0F, 0.0F, 0.0F, -1.4835F));
        tibia_2_l_grp.addOrReplaceChild("tarsus_2_l_grp", CubeListBuilder.create().texOffs(80, 90).addBox(-5.6F, -1.5F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(-0.95F)), PartPose.offsetAndRotation(-7.6F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3142F));

        PartDefinition hip_2_r_grp = thorax_grp.addOrReplaceChild("hip_2_r_grp", CubeListBuilder.create(), PartPose.offset(4.4F, 0.5F, 0.4F));
        PartDefinition femur_2_r_grp = hip_2_r_grp.addOrReplaceChild("femur_2_r_grp", CubeListBuilder.create().texOffs(104, 98).mirror().addBox(-1.4F, -2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(-1.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));
        PartDefinition tibia_2_r_grp = femur_2_r_grp.addOrReplaceChild("tibia_2_r_grp", CubeListBuilder.create().texOffs(76, 105).mirror().addBox(-1.4F, -2.0F, -2.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-1.05F)).mirror(false), PartPose.offsetAndRotation(5.6F, 0.0F, 0.0F, 0.0F, 0.0F, 1.4835F));
        tibia_2_r_grp.addOrReplaceChild("tarsus_2_r_grp", CubeListBuilder.create().texOffs(80, 90).mirror().addBox(-1.4F, -1.5F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(-0.95F)).mirror(false), PartPose.offsetAndRotation(7.6F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3142F));

        PartDefinition hip_3_l_grp = thorax_grp.addOrReplaceChild("hip_3_l_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.4F, 0.5F, 5.0F, 0.0F, 0.4887F, 0.0F));
        PartDefinition femur_3_l_grp = hip_3_l_grp.addOrReplaceChild("femur_3_l_grp", CubeListBuilder.create().texOffs(104, 106).addBox(-6.6F, -2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));
        PartDefinition tibia_3_l_grp = femur_3_l_grp.addOrReplaceChild("tibia_3_l_grp", CubeListBuilder.create().texOffs(80, 82).addBox(-8.6F, -2.0F, -2.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-1.05F)), PartPose.offsetAndRotation(-5.6F, 0.0F, 0.0F, 0.0F, 0.0F, -1.4835F));
        tibia_3_l_grp.addOrReplaceChild("tarsus_3_l_grp", CubeListBuilder.create().texOffs(106, 114).addBox(-5.6F, -1.5F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(-0.95F)), PartPose.offsetAndRotation(-7.6F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3142F));

        PartDefinition hip_3_r_grp = thorax_grp.addOrReplaceChild("hip_3_r_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(4.4F, 0.5F, 5.0F, 0.0F, -0.4887F, 0.0F));
        PartDefinition femur_3_r_grp = hip_3_r_grp.addOrReplaceChild("femur_3_r_grp", CubeListBuilder.create().texOffs(104, 106).mirror().addBox(-1.4F, -2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(-1.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));
        PartDefinition tibia_3_r_grp = femur_3_r_grp.addOrReplaceChild("tibia_3_r_grp", CubeListBuilder.create().texOffs(80, 82).mirror().addBox(-1.4F, -2.0F, -2.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-1.05F)).mirror(false), PartPose.offsetAndRotation(5.6F, 0.0F, 0.0F, 0.0F, 0.0F, 1.4835F));
        tibia_3_r_grp.addOrReplaceChild("tarsus_3_r_grp", CubeListBuilder.create().texOffs(106, 114).mirror().addBox(-1.4F, -1.5F, -1.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(-0.95F)).mirror(false), PartPose.offsetAndRotation(7.6F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3142F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(ObsidianScorpioidRenderState state) {
        super.setupAnim(state);

        if (state.deathTime > 0.0F || state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        this.pincerClampAnimation.apply(state.pincerClampAnimationState, state.ageInTicks);
        this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);

        if (state.isClimbing) {
            this.body_grp.xRot -= 0.7854F; // 45° vertical basalt wall pitch
        }

        this.head_grp.xRot += Mth.clamp(state.xRot, -20.0F, 20.0F) * Mth.DEG_TO_RAD * 0.5F;
        this.head_grp.yRot += Mth.clamp(state.yRot, -30.0F, 30.0F) * Mth.DEG_TO_RAD * 0.5F;
    }
}
