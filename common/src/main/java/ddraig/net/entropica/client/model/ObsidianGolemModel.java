package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.ObsidianGolemAnimations;
import ddraig.net.entropica.client.renderer.ObsidianGolemRenderState;
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
 * Fauna 55 — Obsidian Golem (Nether Sentinel).
 * Exact conversion of all 59 cubes, 24 bone groups, and 128x128 UVs from obsidian_golem.bbmodel.
 */
public class ObsidianGolemModel extends EntityModel<ObsidianGolemRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "obsidian_golem"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation spatialWaveAnimation;
    private final KeyframeAnimation shieldBlockAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public ObsidianGolemModel(ModelPart modelRoot) {
        super(modelRoot);
        ModelPart root = modelRoot.getChild("root");
        ModelPart chestGrp = root.getChild("body_grp").getChild("waist_grp").getChild("chest_grp");
        this.head_grp = chestGrp.getChild("neck_grp").getChild("head_grp");

        this.idleAnimation = ObsidianGolemAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = ObsidianGolemAnimations.WALK.bake(modelRoot);
        this.attackAnimation = ObsidianGolemAnimations.ATTACK.bake(modelRoot);
        this.spatialWaveAnimation = ObsidianGolemAnimations.SPATIAL_WAVE.bake(modelRoot);
        this.shieldBlockAnimation = ObsidianGolemAnimations.SHIELD_BLOCK.bake(modelRoot);
        this.hurtAnimation = ObsidianGolemAnimations.HURT.bake(modelRoot);
        this.deathAnimation = ObsidianGolemAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_grp = root.addOrReplaceChild("body_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -12.5F, 0.0F));

        PartDefinition waist_grp = body_grp.addOrReplaceChild("waist_grp", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-6.0F, -4.5F, -5.0F, 12.0F, 6.0F, 10.0F, new CubeDeformation(-1.0F))
                .texOffs(30, 89).mirror().addBox(-4.0F, -2.1F, 2.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(-1.02F)).mirror(false),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        waist_grp.addOrReplaceChild("apron_grp", CubeListBuilder.create()
                .texOffs(30, 89).addBox(-4.0F, -1.2F, -2.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(-1.02F)),
                PartPose.offset(0.0F, -0.1F, -4.0F));

        PartDefinition chest_grp = waist_grp.addOrReplaceChild("chest_grp", CubeListBuilder.create()
                .texOffs(112, 52).addBox(-7.0F, -13.0F, -7.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(-1.0F))
                .texOffs(112, 52).mirror().addBox(3.0F, -13.0F, -7.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(-1.0F)).mirror(false)
                .texOffs(112, 52).addBox(-7.0F, -13.0F, 3.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(-1.0F))
                .texOffs(112, 52).mirror().addBox(3.0F, -13.0F, 3.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(-1.0F)).mirror(false)
                .texOffs(56, 104).addBox(-5.0F, -13.0F, -7.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-1.01F))
                .texOffs(56, 104).addBox(-5.0F, -13.0F, 3.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-1.01F))
                .texOffs(56, 104).addBox(-5.0F, -3.0F, -7.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-1.01F))
                .texOffs(56, 104).addBox(-5.0F, -3.0F, 3.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-1.01F))
                .texOffs(84, 52).addBox(-7.0F, -13.0F, -5.0F, 4.0F, 4.0F, 10.0F, new CubeDeformation(-1.02F))
                .texOffs(84, 52).mirror().addBox(3.0F, -13.0F, -5.0F, 4.0F, 4.0F, 10.0F, new CubeDeformation(-1.02F)).mirror(false)
                .texOffs(84, 52).addBox(-7.0F, -3.0F, -5.0F, 4.0F, 4.0F, 10.0F, new CubeDeformation(-1.02F))
                .texOffs(84, 52).mirror().addBox(3.0F, -3.0F, -5.0F, 4.0F, 4.0F, 10.0F, new CubeDeformation(-1.02F)).mirror(false)
                .texOffs(108, 93).addBox(-6.5F, -10.5F, -7.7F, 3.0F, 9.0F, 3.0F, new CubeDeformation(-0.53F))
                .texOffs(108, 93).mirror().addBox(3.5F, -10.5F, -7.7F, 3.0F, 9.0F, 3.0F, new CubeDeformation(-0.53F)).mirror(false)
                .texOffs(108, 93).addBox(-6.5F, -10.5F, 4.7F, 3.0F, 9.0F, 3.0F, new CubeDeformation(-0.53F))
                .texOffs(108, 93).mirror().addBox(3.5F, -10.5F, 4.7F, 3.0F, 9.0F, 3.0F, new CubeDeformation(-0.53F)).mirror(false)
                .texOffs(52, 0).addBox(-8.0F, -15.0F, -6.0F, 16.0F, 4.0F, 12.0F, new CubeDeformation(-1.0F))
                .texOffs(74, 36).addBox(-8.0F, -17.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(-0.52F))
                .texOffs(74, 36).mirror().addBox(4.0F, -17.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(-0.52F)).mirror(false),
                PartPose.offset(0.0F, -3.5F, 0.0F));

        PartDefinition portal_core_grp = chest_grp.addOrReplaceChild("portal_core_grp", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.5F, -6.5F, -6.5F, 13.0F, 13.0F, 13.0F, new CubeDeformation(-2.0F)),
                PartPose.offset(0.0F, -6.0F, 0.0F));

        PartDefinition portal_heart_grp = portal_core_grp.addOrReplaceChild("portal_heart_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        portal_heart_grp.addOrReplaceChild("portal_heart_block_r1", CubeListBuilder.create()
                .texOffs(82, 82).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(-1.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6109F, -0.7854F, 0.0F));

        PartDefinition neck_grp = chest_grp.addOrReplaceChild("neck_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -14.0F, -0.5F));

        PartDefinition head_grp = neck_grp.addOrReplaceChild("head_grp", CubeListBuilder.create()
                .texOffs(52, 16).addBox(-5.5F, -8.5F, -5.0F, 11.0F, 10.0F, 10.0F, new CubeDeformation(-1.5F))
                .texOffs(26, 101).addBox(-5.5F, -8.4F, -5.2F, 11.0F, 4.0F, 4.0F, new CubeDeformation(-1.02F))
                .texOffs(0, 82).addBox(-4.0F, -10.0F, -3.5F, 8.0F, 4.0F, 7.0F, new CubeDeformation(-1.01F))
                .texOffs(26, 42).addBox(-1.5F, -12.1F, -2.5F, 3.0F, 4.0F, 5.0F, new CubeDeformation(-0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head_grp.addOrReplaceChild("jaw_grp", CubeListBuilder.create()
                .texOffs(0, 74).addBox(-3.5F, -0.8F, -2.5F, 7.0F, 3.0F, 3.0F, new CubeDeformation(-0.53F)),
                PartPose.offset(0.0F, -0.5F, -2.0F));

        PartDefinition arm_ul_grp = chest_grp.addOrReplaceChild("arm_ul_grp", CubeListBuilder.create()
                .texOffs(44, 36).addBox(-3.5F, -4.0F, -4.0F, 7.0F, 7.0F, 8.0F, new CubeDeformation(-1.01F))
                .texOffs(106, 81).addBox(-4.6F, -4.0F, -2.5F, 3.0F, 7.0F, 5.0F, new CubeDeformation(-0.5F))
                .texOffs(82, 66).addBox(-3.0F, 0.3F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(-1.02F)),
                PartPose.offsetAndRotation(-10.2F, -12.8F, 0.8F, 0.0349F, 0.0F, 0.4887F));

        arm_ul_grp.addOrReplaceChild("forearm_ul_grp", CubeListBuilder.create()
                .texOffs(94, 16).addBox(-3.5F, -1.6F, -3.5F, 7.0F, 11.0F, 7.0F, new CubeDeformation(-1.0F))
                .texOffs(54, 89).addBox(-3.5F, 2.9F, -3.5F, 7.0F, 3.0F, 7.0F, new CubeDeformation(-0.6F))
                .texOffs(52, 51).addBox(-4.0F, 7.1F, -4.0F, 8.0F, 5.0F, 8.0F, new CubeDeformation(-1.0F)),
                PartPose.offsetAndRotation(0.0F, 8.9F, 0.0F, -0.4189F, 0.0F, -0.2443F));

        PartDefinition arm_ur_grp = chest_grp.addOrReplaceChild("arm_ur_grp", CubeListBuilder.create()
                .texOffs(44, 36).mirror().addBox(-3.5F, -4.0F, -4.0F, 7.0F, 7.0F, 8.0F, new CubeDeformation(-1.01F)).mirror(false)
                .texOffs(106, 81).mirror().addBox(1.6F, -4.0F, -2.5F, 3.0F, 7.0F, 5.0F, new CubeDeformation(-0.5F)).mirror(false)
                .texOffs(82, 66).mirror().addBox(-3.0F, 0.3F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(-1.02F)).mirror(false),
                PartPose.offsetAndRotation(10.2F, -12.8F, 0.8F, 0.0349F, 0.0F, -0.4887F));

        arm_ur_grp.addOrReplaceChild("forearm_ur_grp", CubeListBuilder.create()
                .texOffs(94, 34).addBox(-3.5F, -1.6F, -3.5F, 7.0F, 11.0F, 7.0F, new CubeDeformation(-1.0F))
                .texOffs(54, 89).mirror().addBox(-3.5F, 2.9F, -3.5F, 7.0F, 3.0F, 7.0F, new CubeDeformation(-0.6F)).mirror(false)
                .texOffs(52, 51).mirror().addBox(-4.0F, 7.1F, -4.0F, 8.0F, 5.0F, 8.0F, new CubeDeformation(-1.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 8.9F, 0.0F, -0.4189F, 0.0F, 0.2443F));

        PartDefinition arm_ll_grp = chest_grp.addOrReplaceChild("arm_ll_grp", CubeListBuilder.create()
                .texOffs(106, 70).addBox(-2.5F, -2.5F, -3.0F, 5.0F, 5.0F, 6.0F, new CubeDeformation(-0.52F))
                .texOffs(108, 0).addBox(-2.0F, 0.9F, -2.5F, 4.0F, 8.0F, 5.0F, new CubeDeformation(-0.52F)),
                PartPose.offsetAndRotation(-7.8F, -5.4F, -1.0F, -0.384F, 0.0F, 0.2618F));

        arm_ll_grp.addOrReplaceChild("forearm_ll_grp", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-3.0F, -1.6F, -3.5F, 6.0F, 10.0F, 7.0F, new CubeDeformation(-1.0F))
                .texOffs(82, 94).addBox(-3.0F, 2.3F, -3.5F, 6.0F, 3.0F, 7.0F, new CubeDeformation(-0.6F))
                .texOffs(52, 64).addBox(-3.5F, 6.0F, -4.0F, 7.0F, 5.0F, 8.0F, new CubeDeformation(-1.0F)),
                PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -0.733F, -0.1396F, -0.1396F));

        PartDefinition arm_lr_grp = chest_grp.addOrReplaceChild("arm_lr_grp", CubeListBuilder.create()
                .texOffs(106, 70).mirror().addBox(-2.5F, -2.5F, -3.0F, 5.0F, 5.0F, 6.0F, new CubeDeformation(-0.52F)).mirror(false)
                .texOffs(108, 0).mirror().addBox(-2.0F, 0.9F, -2.5F, 4.0F, 8.0F, 5.0F, new CubeDeformation(-0.52F)).mirror(false),
                PartPose.offsetAndRotation(7.8F, -5.4F, -1.0F, -0.384F, 0.0F, -0.2618F));

        arm_lr_grp.addOrReplaceChild("forearm_lr_grp", CubeListBuilder.create()
                .texOffs(26, 51).addBox(-3.0F, -1.6F, -3.5F, 6.0F, 10.0F, 7.0F, new CubeDeformation(-1.0F))
                .texOffs(82, 94).mirror().addBox(-3.0F, 2.3F, -3.5F, 6.0F, 3.0F, 7.0F, new CubeDeformation(-0.6F)).mirror(false)
                .texOffs(52, 64).mirror().addBox(-3.5F, 6.0F, -4.0F, 7.0F, 5.0F, 8.0F, new CubeDeformation(-1.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -0.733F, 0.1396F, 0.1396F));

        PartDefinition leg_l_grp = body_grp.addOrReplaceChild("leg_l_grp", CubeListBuilder.create()
                .texOffs(0, 59).addBox(-3.0F, -1.0F, -3.5F, 6.0F, 8.0F, 7.0F, new CubeDeformation(-1.02F))
                .texOffs(0, 93).addBox(-3.0F, 4.3F, -3.5F, 6.0F, 3.0F, 7.0F, new CubeDeformation(-0.58F)),
                PartPose.offset(-3.8F, 0.5F, 0.0F));
        PartDefinition shin_l_grp = leg_l_grp.addOrReplaceChild("shin_l_grp", CubeListBuilder.create()
                .texOffs(26, 68).addBox(-3.0F, -1.5F, -3.5F, 6.0F, 7.0F, 7.0F, new CubeDeformation(-1.04F)),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        shin_l_grp.addOrReplaceChild("foot_l_grp", CubeListBuilder.create()
                .texOffs(52, 77).addBox(-3.5F, -1.0F, -4.5F, 7.0F, 4.0F, 8.0F, new CubeDeformation(-1.0F)),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition leg_r_grp = body_grp.addOrReplaceChild("leg_r_grp", CubeListBuilder.create()
                .texOffs(0, 59).mirror().addBox(-3.0F, -1.0F, -3.5F, 6.0F, 8.0F, 7.0F, new CubeDeformation(-1.02F)).mirror(false)
                .texOffs(0, 93).mirror().addBox(-3.0F, 4.3F, -3.5F, 6.0F, 3.0F, 7.0F, new CubeDeformation(-0.58F)).mirror(false),
                PartPose.offset(3.8F, 0.5F, 0.0F));
        PartDefinition shin_r_grp = leg_r_grp.addOrReplaceChild("shin_r_grp", CubeListBuilder.create()
                .texOffs(26, 68).mirror().addBox(-3.0F, -1.5F, -3.5F, 6.0F, 7.0F, 7.0F, new CubeDeformation(-1.04F)).mirror(false),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        shin_r_grp.addOrReplaceChild("foot_r_grp", CubeListBuilder.create()
                .texOffs(52, 77).mirror().addBox(-3.5F, -1.0F, -4.5F, 7.0F, 4.0F, 8.0F, new CubeDeformation(-1.0F)).mirror(false),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(ObsidianGolemRenderState state) {
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
        this.spatialWaveAnimation.apply(state.spatialWaveAnimationState, state.ageInTicks);
        this.shieldBlockAnimation.apply(state.shieldBlockAnimationState, state.ageInTicks);
        this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);

        this.head_grp.xRot += Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD * 0.55F;
        this.head_grp.yRot += Mth.clamp(state.yRot, -38.0F, 38.0F) * Mth.DEG_TO_RAD * 0.55F;
    }
}
