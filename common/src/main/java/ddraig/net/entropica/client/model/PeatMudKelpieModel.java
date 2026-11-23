package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.PeatMudKelpieAnimations;
import ddraig.net.entropica.client.renderer.PeatMudKelpieRenderState;
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
 * Fauna 37 — Peat-Mud Kelpie (Swamp Kelpie).
 * Exact conversion of all 26 cubes, 24 bone groups, and 64x64 UVs from peat_mud_kelpie.bbmodel.
 */
public class PeatMudKelpieModel extends EntityModel<PeatMudKelpieRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "peat_mud_kelpie"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart root;
    private final ModelPart body_root;
    private final ModelPart haunch_group;
    private final ModelPart chest_group;
    private final ModelPart neck_group;
    private final ModelPart head_group;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation swimAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation rearAnimation;

    public PeatMudKelpieModel(ModelPart modelRoot) {
        super(modelRoot);
        this.root = modelRoot.getChild("root");
        this.body_root = this.root.getChild("body_root");
        this.haunch_group = this.body_root.getChild("haunch_group");
        this.chest_group = this.haunch_group.getChild("chest_group");
        this.neck_group = this.chest_group.getChild("neck_group");
        this.head_group = this.neck_group.getChild("head_group");

        this.idleAnimation = PeatMudKelpieAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = PeatMudKelpieAnimations.WALK.bake(modelRoot);
        this.swimAnimation = PeatMudKelpieAnimations.SWIM.bake(modelRoot);
        this.attackAnimation = PeatMudKelpieAnimations.ATTACK.bake(modelRoot);
        this.rearAnimation = PeatMudKelpieAnimations.REAR.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_root = root.addOrReplaceChild("body_root", CubeListBuilder.create(), PartPose.offset(0.0F, -15.0F, 0.0F));

        PartDefinition haunch_group = body_root.addOrReplaceChild("haunch_group", CubeListBuilder.create().texOffs(29, 9).addBox(-4.0F, -5.0F, -6.0F, 8.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 6.0F));

        PartDefinition chest_group = haunch_group.addOrReplaceChild("chest_group", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -4.0F, -9.5F, 9.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, -5.5F));

        PartDefinition neck_group = chest_group.addOrReplaceChild("neck_group", CubeListBuilder.create(), PartPose.offset(0.0F, -2.2F, -7.7F));

        neck_group.addOrReplaceChild("mane_upper_r1", CubeListBuilder.create().texOffs(54, 9).addBox(-1.0F, -10.0F, 1.8F, 2.0F, 5.0F, 3.0F, new CubeDeformation(-0.02F))
                .texOffs(0, 0).addBox(-1.0F, -5.6F, 2.0F, 2.0F, 6.0F, 3.0F, new CubeDeformation(0.02F))
                .texOffs(0, 18).addBox(-2.5F, -9.0F, -2.5F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4887F, 0.0F, 0.0F));

        PartDefinition head_group = neck_group.addOrReplaceChild("head_group", CubeListBuilder.create(), PartPose.offset(0.0F, -7.1F, -3.8F));

        head_group.addOrReplaceChild("mane_forelock_r1", CubeListBuilder.create().texOffs(52, 47).addBox(-1.0F, -3.9F, -2.4F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.03F))
                .texOffs(45, 36).addBox(-2.0F, -2.1F, -8.8F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(36, 25).addBox(-3.0F, -2.5F, -3.8F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4887F, 0.0F, 0.0F));

        PartDefinition jaw_group = head_group.addOrReplaceChild("jaw_group", CubeListBuilder.create(), PartPose.offset(0.0F, 3.1F, -2.7F));
        jaw_group.addOrReplaceChild("jaw_lower_r1", CubeListBuilder.create().texOffs(0, 44).addBox(-2.0F, -1.0F, -4.65F, 4.0F, 2.0F, 5.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4887F, 0.0F, 0.0F));

        PartDefinition ear_l_grp = head_group.addOrReplaceChild("ear_l_grp", CubeListBuilder.create(), PartPose.offset(-2.1F, -2.7F, -0.3F));
        ear_l_grp.addOrReplaceChild("ear_l_r1", CubeListBuilder.create().texOffs(28, 0).addBox(-1.0F, -2.6F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.384F, -0.1745F, -0.1047F));

        PartDefinition ear_r_grp = head_group.addOrReplaceChild("ear_r_grp", CubeListBuilder.create(), PartPose.offset(2.1F, -2.7F, -0.3F));
        ear_r_grp.addOrReplaceChild("ear_r_r1", CubeListBuilder.create().texOffs(28, 0).mirror().addBox(-1.0F, -2.6F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.384F, 0.1745F, 0.1047F));

        PartDefinition leg_fl_grp = chest_group.addOrReplaceChild("leg_fl_grp", CubeListBuilder.create(), PartPose.offset(-2.9F, 2.5F, -7.0F));
        leg_fl_grp.addOrReplaceChild("fore_upper_l_r1", CubeListBuilder.create().texOffs(0, 33).addBox(-1.5F, -1.0F, -2.0F, 3.0F, 7.0F, 4.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition shin_fl_grp = leg_fl_grp.addOrReplaceChild("shin_fl_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 5.7F, -1.0F));
        shin_fl_grp.addOrReplaceChild("fore_shin_l_r1", CubeListBuilder.create().texOffs(48, 0).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1396F, 0.0F, 0.0F));
        shin_fl_grp.addOrReplaceChild("hoof_fl_grp", CubeListBuilder.create().texOffs(18, 45).addBox(-2.0F, -0.4F, -2.3F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.2F, 0.7F));

        PartDefinition leg_fr_grp = chest_group.addOrReplaceChild("leg_fr_grp", CubeListBuilder.create(), PartPose.offset(2.9F, 2.5F, -7.0F));
        leg_fr_grp.addOrReplaceChild("fore_upper_r_r1", CubeListBuilder.create().texOffs(0, 33).mirror().addBox(-1.5F, -1.0F, -2.0F, 3.0F, 7.0F, 4.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition shin_fr_grp = leg_fr_grp.addOrReplaceChild("shin_fr_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 5.7F, -1.0F));
        shin_fr_grp.addOrReplaceChild("fore_shin_r_r1", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-1.5F, -0.5F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1396F, 0.0F, 0.0F));
        shin_fr_grp.addOrReplaceChild("hoof_fr_grp", CubeListBuilder.create().texOffs(18, 45).mirror().addBox(-2.0F, -0.4F, -2.3F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 5.2F, 0.7F));

        PartDefinition tail_1_grp = haunch_group.addOrReplaceChild("tail_1_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -3.9F, 2.6F));
        tail_1_grp.addOrReplaceChild("tail_1_r1", CubeListBuilder.create().texOffs(14, 37).addBox(-1.5F, -1.5F, -0.4F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6632F, 0.0F, 0.0F));

        PartDefinition tail_2_grp = tail_1_grp.addOrReplaceChild("tail_2_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 2.6F, 3.4F));
        tail_2_grp.addOrReplaceChild("tail_2_r1", CubeListBuilder.create().texOffs(28, 0).addBox(-2.0F, -1.5F, -0.4F, 4.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.8378F, 0.0F, 0.0F));

        PartDefinition tail_3_grp = tail_2_grp.addOrReplaceChild("tail_3_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 3.6F, 3.2F));
        tail_3_grp.addOrReplaceChild("tail_3_r1", CubeListBuilder.create().texOffs(40, 44).addBox(-1.5F, -1.0F, -0.4F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.9774F, 0.0F, 0.0F));

        PartDefinition leg_bl_grp = body_root.addOrReplaceChild("leg_bl_grp", CubeListBuilder.create(), PartPose.offset(-2.8F, 0.5F, 6.0F));
        leg_bl_grp.addOrReplaceChild("hind_thigh_l_r1", CubeListBuilder.create().texOffs(20, 25).addBox(-1.5F, -0.7F, -2.5F, 3.0F, 7.0F, 5.0F, new CubeDeformation(0.04F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2443F, 0.0F, 0.0F));

        PartDefinition shin_bl_grp = leg_bl_grp.addOrReplaceChild("shin_bl_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 5.8F, 1.4F));
        shin_bl_grp.addOrReplaceChild("hind_shin_l_r1", CubeListBuilder.create().texOffs(33, 36).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2094F, 0.0F, 0.0F));
        shin_bl_grp.addOrReplaceChild("hoof_bl_grp", CubeListBuilder.create().texOffs(0, 51).addBox(-2.0F, -0.4F, -2.3F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.1F, -1.3F));

        PartDefinition leg_br_grp = body_root.addOrReplaceChild("leg_br_grp", CubeListBuilder.create(), PartPose.offset(2.8F, 0.5F, 6.0F));
        leg_br_grp.addOrReplaceChild("hind_thigh_r_r1", CubeListBuilder.create().texOffs(20, 25).mirror().addBox(-1.5F, -0.7F, -2.5F, 3.0F, 7.0F, 5.0F, new CubeDeformation(0.04F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2443F, 0.0F, 0.0F));

        PartDefinition shin_br_grp = leg_br_grp.addOrReplaceChild("shin_br_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 5.8F, 1.4F));
        shin_br_grp.addOrReplaceChild("hind_shin_r_r1", CubeListBuilder.create().texOffs(33, 36).mirror().addBox(-1.5F, -0.5F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(-0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2094F, 0.0F, 0.0F));
        shin_br_grp.addOrReplaceChild("hoof_br_grp", CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-2.0F, -0.4F, -2.3F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 6.1F, -1.3F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(PeatMudKelpieRenderState state) {
        super.setupAnim(state);

        if (state.attackAnimationState.isStarted()) {
            this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        } else if (state.rearAnimationState.isStarted()) {
            this.rearAnimation.apply(state.rearAnimationState, state.ageInTicks);
        } else if (state.swimAnimationState.isStarted()) {
            this.swimAnimation.apply(state.swimAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        if (!state.attackAnimationState.isStarted() && !state.rearAnimationState.isStarted()) {
            float clampedYaw = Mth.clamp(state.yRot, -40.0F, 40.0F) * Mth.DEG_TO_RAD;
            float clampedPitch = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
            this.neck_group.yRot += clampedYaw * 0.35F;
            this.head_group.yRot += clampedYaw * 0.55F;
            this.head_group.xRot += clampedPitch * 0.65F;
        }
    }
}
