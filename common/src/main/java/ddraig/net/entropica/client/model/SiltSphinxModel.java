package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.SiltSphinxAnimations;
import ddraig.net.entropica.client.renderer.SiltSphinxRenderState;
import ddraig.net.entropica.entity.silt_sphinx.SiltSphinxEntity;
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
 * Fauna 40 — Silt-Sphinx (Canyon Guardian).
 * Exact conversion of all 35 cubes, 25 bone groups, and 64x64 UVs from silt_sphinx.bbmodel.
 */
public class SiltSphinxModel extends EntityModel<SiltSphinxRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "silt_sphinx"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart root;
    private final ModelPart body_root;
    private final ModelPart torso_grp;
    private final ModelPart neck_head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation dormantAnimation;
    private final KeyframeAnimation awakenAnimation;
    private final KeyframeAnimation returnToDormantAnimation;

    public SiltSphinxModel(ModelPart modelRoot) {
        super(modelRoot);
        this.root = modelRoot.getChild("root");
        this.body_root = this.root.getChild("body_root");
        this.torso_grp = this.body_root.getChild("torso_grp");
        this.neck_head_grp = this.torso_grp.getChild("neck_head_grp");

        this.idleAnimation = SiltSphinxAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = SiltSphinxAnimations.WALK.bake(modelRoot);
        this.attackAnimation = SiltSphinxAnimations.ATTACK.bake(modelRoot);
        this.dormantAnimation = SiltSphinxAnimations.DORMANT.bake(modelRoot);
        this.awakenAnimation = SiltSphinxAnimations.AWAKEN.bake(modelRoot);
        this.returnToDormantAnimation = SiltSphinxAnimations.RETURN_TO_DORMANT.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_root = root.addOrReplaceChild("body_root", CubeListBuilder.create(), PartPose.offset(0.0F, -14.0F, 0.0F));

        PartDefinition torso_grp = body_root.addOrReplaceChild("torso_grp", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -6.6F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.2F, -2.0F));

        PartDefinition neck_head_grp = torso_grp.addOrReplaceChild("neck_head_grp", CubeListBuilder.create().texOffs(16, 27).addBox(-3.0F, -6.0F, -3.2F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 49).addBox(-4.0F, -7.6F, -3.5F, 8.0F, 2.0F, 6.0F, new CubeDeformation(0.03F)), PartPose.offset(0.0F, -3.6F, -4.6F));

        neck_head_grp.addOrReplaceChild("pharaoh_beard_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -0.4F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -0.2F, -2.6F, -0.2094F, 0.0F, 0.0F));
        neck_head_grp.addOrReplaceChild("sphinx_ear_r_r1", CubeListBuilder.create().texOffs(33, 0).mirror().addBox(-0.3F, -1.5F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(-0.05F)).mirror(false), PartPose.offsetAndRotation(3.2F, -3.7F, -0.8F, 0.0F, -0.1745F, 0.0F));
        neck_head_grp.addOrReplaceChild("sphinx_ear_l_r1", CubeListBuilder.create().texOffs(33, 0).addBox(-0.7F, -1.5F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(-3.2F, -3.7F, -0.8F, 0.0F, 0.1745F, 0.0F));
        neck_head_grp.addOrReplaceChild("nemes_back_r1", CubeListBuilder.create().texOffs(48, 49).addBox(-3.0F, 0.0F, -1.0F, 6.0F, 5.0F, 2.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, -5.6F, 2.0F, 0.1396F, 0.0F, 0.0F));
        neck_head_grp.addOrReplaceChild("nemes_lappet_r_r1", CubeListBuilder.create().texOffs(25, 39).mirror().addBox(-1.0F, -1.5F, -1.5F, 2.0F, 7.0F, 3.0F, new CubeDeformation(-0.03F)).mirror(false), PartPose.offsetAndRotation(3.3F, -1.7F, -1.5F, -0.0524F, 0.0F, -0.0698F));
        neck_head_grp.addOrReplaceChild("nemes_lappet_l_r1", CubeListBuilder.create().texOffs(25, 39).addBox(-1.0F, -1.5F, -1.5F, 2.0F, 7.0F, 3.0F, new CubeDeformation(-0.03F)), PartPose.offsetAndRotation(-3.3F, -1.7F, -1.5F, -0.0524F, 0.0F, 0.0698F));
        neck_head_grp.addOrReplaceChild("nemes_hood_r_r1", CubeListBuilder.create().texOffs(11, 39).mirror().addBox(-1.0F, -0.6F, -2.5F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.8F, -5.8F, -0.1F, 0.0F, 0.0F, 0.2443F));
        neck_head_grp.addOrReplaceChild("nemes_hood_l_r1", CubeListBuilder.create().texOffs(11, 39).addBox(-1.0F, -0.6F, -2.5F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8F, -5.8F, -0.1F, 0.0F, 0.0F, -0.2443F));
        neck_head_grp.addOrReplaceChild("uraeus_cobra_r1", CubeListBuilder.create().texOffs(53, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.12F)), PartPose.offsetAndRotation(0.0F, -6.4F, -3.6F, 0.1396F, 0.0F, 0.0F));

        PartDefinition wing_l_grp = torso_grp.addOrReplaceChild("wing_l_grp", CubeListBuilder.create(), PartPose.offset(-4.2F, -2.2F, -2.6F));
        wing_l_grp.addOrReplaceChild("wing_base_l_r1", CubeListBuilder.create().texOffs(44, 20).addBox(-1.0F, -2.4F, -1.0F, 2.0F, 5.0F, 7.0F, new CubeDeformation(0.02F)), PartPose.offsetAndRotation(-0.6F, 0.0F, 0.0F, 0.3142F, -0.1396F, 0.1396F));

        PartDefinition wing_outer_l_grp = wing_l_grp.addOrReplaceChild("wing_outer_l_grp", CubeListBuilder.create(), PartPose.offset(-0.8F, -1.4F, 3.6F));
        wing_outer_l_grp.addOrReplaceChild("wing_tip_l_r1", CubeListBuilder.create().texOffs(32, 31).addBox(-1.0F, -1.6F, 0.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(-0.5F, -1.0F, 1.6F, 0.3142F, -0.192F, 0.192F));
        wing_outer_l_grp.addOrReplaceChild("wing_mid_l_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -1.4F, -1.2F, 2.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, 0.3142F, -0.1745F, 0.1745F));

        PartDefinition wing_r_grp = torso_grp.addOrReplaceChild("wing_r_grp", CubeListBuilder.create(), PartPose.offset(4.2F, -2.2F, -2.6F));
        wing_r_grp.addOrReplaceChild("wing_base_r_r1", CubeListBuilder.create().texOffs(44, 20).mirror().addBox(-1.0F, -2.4F, -1.0F, 2.0F, 5.0F, 7.0F, new CubeDeformation(0.02F)).mirror(false), PartPose.offsetAndRotation(0.6F, 0.0F, 0.0F, 0.3142F, 0.1396F, -0.1396F));

        PartDefinition wing_outer_r_grp = wing_r_grp.addOrReplaceChild("wing_outer_r_grp", CubeListBuilder.create(), PartPose.offset(0.8F, -1.4F, 3.6F));
        wing_outer_r_grp.addOrReplaceChild("wing_tip_r_r1", CubeListBuilder.create().texOffs(32, 31).mirror().addBox(-1.0F, -1.6F, 0.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(0.5F, -1.0F, 1.6F, 0.3142F, 0.192F, -0.192F));
        wing_outer_r_grp.addOrReplaceChild("wing_mid_r_r1", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, -1.4F, -1.2F, 2.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.2F, 0.0F, 0.0F, 0.3142F, 0.1745F, -0.1745F));

        PartDefinition waist_grp = torso_grp.addOrReplaceChild("waist_grp", CubeListBuilder.create().texOffs(25, 14).addBox(-3.0F, -3.0F, -0.4F, 6.0F, 6.0F, 7.0F, new CubeDeformation(-0.02F)), PartPose.offset(0.0F, 0.4F, 1.2F));

        PartDefinition haunch_grp = waist_grp.addOrReplaceChild("haunch_grp", CubeListBuilder.create().texOffs(32, 0).addBox(-3.5F, -3.5F, -2.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 5.4F));

        PartDefinition tail_1_grp = haunch_grp.addOrReplaceChild("tail_1_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -1.4F, 4.2F));
        tail_1_grp.addOrReplaceChild("tail_1_r1", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -0.2F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5934F, 0.0F, 0.0F));

        PartDefinition tail_2_grp = tail_1_grp.addOrReplaceChild("tail_2_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 2.6F, 3.8F));
        tail_2_grp.addOrReplaceChild("tail_2_r1", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -0.2F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2793F, 0.0F, 0.0F));

        PartDefinition tail_3_grp = tail_2_grp.addOrReplaceChild("tail_3_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 1.2F, 4.2F));
        tail_3_grp.addOrReplaceChild("tail_tuft_r1", CubeListBuilder.create().texOffs(40, 56).addBox(-1.5F, -1.5F, -0.2F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition leg_fl_grp = body_root.addOrReplaceChild("leg_fl_grp", CubeListBuilder.create().texOffs(48, 38).addBox(-1.5F, -0.8F, -2.0F, 3.0F, 7.0F, 4.0F, new CubeDeformation(-0.02F)), PartPose.offset(-4.2F, 0.6F, -5.6F));
        PartDefinition fore_lower_fl_grp = leg_fl_grp.addOrReplaceChild("fore_lower_fl_grp", CubeListBuilder.create().texOffs(13, 16).addBox(-1.5F, -0.6F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-0.04F)), PartPose.offset(0.0F, 5.8F, 0.0F));
        fore_lower_fl_grp.addOrReplaceChild("fore_paw_fl_grp", CubeListBuilder.create().texOffs(30, 44).addBox(-2.0F, -0.5F, -3.2F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.1F, 0.0F));

        PartDefinition leg_fr_grp = body_root.addOrReplaceChild("leg_fr_grp", CubeListBuilder.create().texOffs(48, 38).mirror().addBox(-1.5F, -0.8F, -2.0F, 3.0F, 7.0F, 4.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offset(4.2F, 0.6F, -5.6F));
        PartDefinition fore_lower_fr_grp = leg_fr_grp.addOrReplaceChild("fore_lower_fr_grp", CubeListBuilder.create().texOffs(13, 16).mirror().addBox(-1.5F, -0.6F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-0.04F)).mirror(false), PartPose.offset(0.0F, 5.8F, 0.0F));
        fore_lower_fr_grp.addOrReplaceChild("fore_paw_fr_grp", CubeListBuilder.create().texOffs(30, 44).mirror().addBox(-2.0F, -0.5F, -3.2F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 5.1F, 0.0F));

        PartDefinition leg_bl_grp = body_root.addOrReplaceChild("leg_bl_grp", CubeListBuilder.create(), PartPose.offset(-3.8F, 1.0F, 5.8F));
        leg_bl_grp.addOrReplaceChild("hind_thigh_l_r1", CubeListBuilder.create().texOffs(0, 29).addBox(-1.5F, -1.0F, -2.5F, 3.0F, 7.0F, 5.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));
        PartDefinition hind_shank_bl_grp = leg_bl_grp.addOrReplaceChild("hind_shank_bl_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 5.5F, 0.4F));
        hind_shank_bl_grp.addOrReplaceChild("hind_shank_l_r1", CubeListBuilder.create().texOffs(13, 16).addBox(-1.5F, -0.6F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1047F, 0.0F, 0.0F));
        hind_shank_bl_grp.addOrReplaceChild("hind_paw_bl_grp", CubeListBuilder.create().texOffs(28, 52).addBox(-2.0F, -0.5F, -2.5F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -0.2F));

        PartDefinition leg_br_grp = body_root.addOrReplaceChild("leg_br_grp", CubeListBuilder.create(), PartPose.offset(3.8F, 1.0F, 5.8F));
        leg_br_grp.addOrReplaceChild("hind_thigh_r_r1", CubeListBuilder.create().texOffs(0, 29).mirror().addBox(-1.5F, -1.0F, -2.5F, 3.0F, 7.0F, 5.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));
        PartDefinition hind_shank_br_grp = leg_br_grp.addOrReplaceChild("hind_shank_br_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 5.5F, 0.4F));
        hind_shank_br_grp.addOrReplaceChild("hind_shank_r_r1", CubeListBuilder.create().texOffs(13, 16).mirror().addBox(-1.5F, -0.6F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-0.04F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1047F, 0.0F, 0.0F));
        hind_shank_br_grp.addOrReplaceChild("hind_paw_br_grp", CubeListBuilder.create().texOffs(28, 52).mirror().addBox(-2.0F, -0.5F, -2.5F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 5.0F, -0.2F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(SiltSphinxRenderState state) {
        super.setupAnim(state);

        if (state.attackAnimationState.isStarted()) {
            this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        } else if (state.awakenAnimationState.isStarted()) {
            this.awakenAnimation.apply(state.awakenAnimationState, state.ageInTicks);
        } else if (state.returnToDormantAnimationState.isStarted()) {
            this.returnToDormantAnimation.apply(state.returnToDormantAnimationState, state.ageInTicks);
        } else if (state.dormantAnimationState.isStarted()
                || state.monumentState == SiltSphinxEntity.STATE_DORMANT) {
            this.dormantAnimation.apply(state.dormantAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        if (state.monumentState == SiltSphinxEntity.STATE_ACTIVE
                && !state.attackAnimationState.isStarted()
                && !state.awakenAnimationState.isStarted()
                && !state.returnToDormantAnimationState.isStarted()) {
            float clampedYaw = Mth.clamp(state.yRot, -40.0F, 40.0F) * Mth.DEG_TO_RAD;
            float clampedPitch = Mth.clamp(state.xRot, -22.0F, 22.0F) * Mth.DEG_TO_RAD;
            this.neck_head_grp.yRot += clampedYaw * 0.70F;
            this.neck_head_grp.xRot += clampedPitch * 0.55F;
        }
    }
}
