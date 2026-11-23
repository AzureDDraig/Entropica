package ddraig.net.entropica.client.renderer.mud_lurker_salamander;

import ddraig.net.entropica.Entropica;
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
 * Fauna 33 — Mud-Lurker Salamander (Amphibious Crawler).
 * Exact conversion of all 24 bone groups, 41 cubes, and 64x64 UV space (mapped onto 128x128 variant PNGs)
 * from mud_lurker_salamander.bbmodel.
 */
public class MudLurkerSalamanderModel extends EntityModel<MudLurkerSalamanderRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mud_lurker_salamander"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart root;
    private final ModelPart body_root;
    private final ModelPart body_2_grp;
    private final ModelPart body_1_grp;
    private final ModelPart neck_grp;
    private final ModelPart head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation enterWaterAnimation;
    private final KeyframeAnimation swimIdleAnimation;
    private final KeyframeAnimation swimAnimation;
    private final KeyframeAnimation swimAttackAnimation;
    private final KeyframeAnimation exitWaterAnimation;

    public MudLurkerSalamanderModel(ModelPart modelRoot) {
        super(modelRoot);
        this.root = modelRoot.getChild("root");
        this.body_root = this.root.getChild("body_root");
        this.body_2_grp = this.body_root.getChild("body_2_grp");
        this.body_1_grp = this.body_2_grp.getChild("body_1_grp");
        this.neck_grp = this.body_1_grp.getChild("neck_grp");
        this.head_grp = this.neck_grp.getChild("head_grp");

        this.idleAnimation = MudLurkerSalamanderAnimation.IDLE.bake(modelRoot);
        this.walkAnimation = MudLurkerSalamanderAnimation.WALK.bake(modelRoot);
        this.attackAnimation = MudLurkerSalamanderAnimation.ATTACK.bake(modelRoot);
        this.enterWaterAnimation = MudLurkerSalamanderAnimation.ENTER_WATER.bake(modelRoot);
        this.swimIdleAnimation = MudLurkerSalamanderAnimation.SWIM_IDLE.bake(modelRoot);
        this.swimAnimation = MudLurkerSalamanderAnimation.SWIM.bake(modelRoot);
        this.swimAttackAnimation = MudLurkerSalamanderAnimation.SWIM_ATTACK.bake(modelRoot);
        this.exitWaterAnimation = MudLurkerSalamanderAnimation.EXIT_WATER.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition body_root = root.addOrReplaceChild("body_root", CubeListBuilder.create(), PartPose.offset(0.0F, -4.5F, 0.0F));
        PartDefinition body_2_grp = body_root.addOrReplaceChild("body_2_grp", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -2.5F, -2.5F, 9.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(46, 34).addBox(-2.0F, -3.45F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body_1_grp = body_2_grp.addOrReplaceChild("body_1_grp", CubeListBuilder.create()
                .texOffs(28, 0).addBox(-4.0F, -2.5F, -4.7F, 8.0F, 5.0F, 5.0F, new CubeDeformation(-0.02F))
                .texOffs(30, 34).addBox(-2.0F, -3.42F, -4.2F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -2.5F));
        PartDefinition neck_grp = body_1_grp.addOrReplaceChild("neck_grp", CubeListBuilder.create()
                .texOffs(0, 29).addBox(-3.0F, -2.0F, -1.9F, 6.0F, 4.0F, 2.0F, new CubeDeformation(-0.04F)), PartPose.offset(0.0F, 0.1F, -4.6F));
        PartDefinition head_grp = neck_grp.addOrReplaceChild("head_grp", CubeListBuilder.create()
                .texOffs(26, 10).addBox(-4.0F, -2.1F, -4.9F, 8.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(18, 28).addBox(-3.0F, -2.1F, -8.8F, 6.0F, 2.0F, 4.0F, new CubeDeformation(-0.02F))
                .texOffs(12, 34).addBox(-2.5F, -0.2F, -8.5F, 5.0F, 1.0F, 4.0F, new CubeDeformation(-0.06F)), PartPose.offset(0.0F, 0.0F, -1.8F));
        head_grp.addOrReplaceChild("jaw_grp", CubeListBuilder.create()
                .texOffs(38, 29).addBox(-3.0F, -0.2F, -4.0F, 6.0F, 1.0F, 4.0F, new CubeDeformation(-0.04F)), PartPose.offset(0.0F, 0.9F, -4.8F));
        head_grp.addOrReplaceChild("throat_grp", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-3.0F, -0.5F, -2.5F, 6.0F, 1.0F, 5.0F, new CubeDeformation(-0.06F)), PartPose.offset(0.0F, 1.9F, -2.2F));
        PartDefinition leg_fl_grp = body_1_grp.addOrReplaceChild("leg_fl_grp", CubeListBuilder.create(), PartPose.offset(-3.8F, 0.3F, -2.5F));
        leg_fl_grp.addOrReplaceChild("arm_fl_r1", CubeListBuilder.create().texOffs(52, 15).addBox(-3.7F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2443F, -0.384F));
        PartDefinition forearm_fl_grp = leg_fl_grp.addOrReplaceChild("forearm_fl_grp", CubeListBuilder.create()
                .texOffs(23, 0).addBox(-1.0F, -0.4F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.03F)), PartPose.offset(-2.8F, 1.1F, -0.7F));
        PartDefinition foot_fl_grp = forearm_fl_grp.addOrReplaceChild("foot_fl_grp", CubeListBuilder.create()
                .texOffs(38, 19).addBox(-1.0F, -0.5F, -1.1F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.6F, 0.0F));
        foot_fl_grp.addOrReplaceChild("toe_fl_1_r1", CubeListBuilder.create().texOffs(57, 0).addBox(-1.8F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.03F)), PartPose.offsetAndRotation(-0.5F, 0.0F, -0.5F, 0.0F, -0.8727F, 0.0F));
        foot_fl_grp.addOrReplaceChild("toe_fl_2_r1", CubeListBuilder.create().texOffs(57, 0).addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, -0.1745F, 0.0F));
        foot_fl_grp.addOrReplaceChild("toe_fl_3_r1", CubeListBuilder.create().texOffs(57, 0).addBox(-1.8F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.07F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.5F, 0.0F, 0.5585F, 0.0F));
        PartDefinition leg_fr_grp = body_1_grp.addOrReplaceChild("leg_fr_grp", CubeListBuilder.create(), PartPose.offset(3.8F, 0.3F, -2.5F));
        leg_fr_grp.addOrReplaceChild("arm_fr_r1", CubeListBuilder.create().texOffs(52, 15).mirror().addBox(-0.3F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.2443F, 0.384F));
        PartDefinition forearm_fr_grp = leg_fr_grp.addOrReplaceChild("forearm_fr_grp", CubeListBuilder.create()
                .texOffs(23, 0).mirror().addBox(-1.0F, -0.4F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.03F)).mirror(false), PartPose.offset(2.8F, 1.1F, -0.7F));
        PartDefinition foot_fr_grp = forearm_fr_grp.addOrReplaceChild("foot_fr_grp", CubeListBuilder.create()
                .texOffs(38, 19).mirror().addBox(-1.0F, -0.5F, -1.1F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 2.6F, 0.0F));
        foot_fr_grp.addOrReplaceChild("toe_fr_1_r1", CubeListBuilder.create().texOffs(57, 0).mirror().addBox(-0.2F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.03F)).mirror(false), PartPose.offsetAndRotation(0.5F, 0.0F, -0.5F, 0.0F, 0.8727F, 0.0F));
        foot_fr_grp.addOrReplaceChild("toe_fr_2_r1", CubeListBuilder.create().texOffs(57, 0).mirror().addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.05F)).mirror(false), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 0.1745F, 0.0F));
        foot_fr_grp.addOrReplaceChild("toe_fr_3_r1", CubeListBuilder.create().texOffs(57, 0).mirror().addBox(-0.2F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.07F)).mirror(false), PartPose.offsetAndRotation(0.5F, 0.0F, 0.5F, 0.0F, -0.5585F, 0.0F));
        PartDefinition body_3_grp = body_2_grp.addOrReplaceChild("body_3_grp", CubeListBuilder.create()
                .texOffs(0, 10).addBox(-4.0F, -2.5F, -0.3F, 8.0F, 5.0F, 5.0F, new CubeDeformation(-0.02F))
                .texOffs(0, 35).addBox(-2.0F, -3.42F, 0.2F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 2.5F));
        PartDefinition tail_1_grp = body_3_grp.addOrReplaceChild("tail_1_grp", CubeListBuilder.create()
                .texOffs(21, 19).addBox(-3.0F, -2.0F, -0.2F, 6.0F, 4.0F, 5.0F, new CubeDeformation(-0.04F))
                .texOffs(49, 0).addBox(-1.0F, -2.92F, 0.2F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.3F, 4.6F));
        PartDefinition tail_2_grp = tail_1_grp.addOrReplaceChild("tail_2_grp", CubeListBuilder.create()
                .texOffs(43, 19).addBox(-2.0F, -2.5F, -0.1F, 4.0F, 5.0F, 5.0F, new CubeDeformation(-0.02F)), PartPose.offset(0.0F, -0.1F, 4.6F));
        tail_2_grp.addOrReplaceChild("tail_3_grp", CubeListBuilder.create()
                .texOffs(49, 5).addBox(-1.0F, -2.5F, -0.1F, 2.0F, 5.0F, 5.0F, new CubeDeformation(-0.04F))
                .texOffs(58, 29).addBox(-0.5F, -2.0F, 4.6F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.02F)), PartPose.offset(0.0F, -0.1F, 4.7F));
        PartDefinition leg_bl_grp = body_3_grp.addOrReplaceChild("leg_bl_grp", CubeListBuilder.create(), PartPose.offset(-3.8F, 0.3F, 2.3F));
        leg_bl_grp.addOrReplaceChild("thigh_bl_r1", CubeListBuilder.create().texOffs(14, 39).addBox(-3.7F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.2443F, -0.384F));
        PartDefinition shin_bl_grp = leg_bl_grp.addOrReplaceChild("shin_bl_grp", CubeListBuilder.create()
                .texOffs(21, 10).addBox(-1.0F, -0.4F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.03F)), PartPose.offset(-2.8F, 1.1F, 0.7F));
        PartDefinition foot_bl_grp = shin_bl_grp.addOrReplaceChild("foot_bl_grp", CubeListBuilder.create()
                .texOffs(56, 19).addBox(-1.0F, -0.5F, -1.1F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.6F, 0.0F));
        foot_bl_grp.addOrReplaceChild("toe_bl_1_r1", CubeListBuilder.create().texOffs(57, 0).addBox(-1.8F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.03F)), PartPose.offsetAndRotation(-0.5F, 0.0F, -0.5F, 0.0F, -0.7679F, 0.0F));
        foot_bl_grp.addOrReplaceChild("toe_bl_2_r1", CubeListBuilder.create().texOffs(57, 0).addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, -0.1047F, 0.0F));
        foot_bl_grp.addOrReplaceChild("toe_bl_3_r1", CubeListBuilder.create().texOffs(57, 0).addBox(-1.8F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.07F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.5F, 0.0F, 0.5934F, 0.0F));
        PartDefinition leg_br_grp = body_3_grp.addOrReplaceChild("leg_br_grp", CubeListBuilder.create(), PartPose.offset(3.8F, 0.3F, 2.3F));
        leg_br_grp.addOrReplaceChild("thigh_br_r1", CubeListBuilder.create().texOffs(14, 39).mirror().addBox(-0.3F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2443F, 0.384F));
        PartDefinition shin_br_grp = leg_br_grp.addOrReplaceChild("shin_br_grp", CubeListBuilder.create()
                .texOffs(21, 10).mirror().addBox(-1.0F, -0.4F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.03F)).mirror(false), PartPose.offset(2.8F, 1.1F, 0.7F));
        PartDefinition foot_br_grp = shin_br_grp.addOrReplaceChild("foot_br_grp", CubeListBuilder.create()
                .texOffs(56, 19).mirror().addBox(-1.0F, -0.5F, -1.1F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 2.6F, 0.0F));
        foot_br_grp.addOrReplaceChild("toe_br_1_r1", CubeListBuilder.create().texOffs(57, 0).mirror().addBox(-0.2F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.03F)).mirror(false), PartPose.offsetAndRotation(0.5F, 0.0F, -0.5F, 0.0F, 0.7679F, 0.0F));
        foot_br_grp.addOrReplaceChild("toe_br_2_r1", CubeListBuilder.create().texOffs(57, 0).mirror().addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.05F)).mirror(false), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 0.1047F, 0.0F));
        foot_br_grp.addOrReplaceChild("toe_br_3_r1", CubeListBuilder.create().texOffs(57, 0).mirror().addBox(-0.2F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.07F)).mirror(false), PartPose.offsetAndRotation(0.5F, 0.0F, 0.5F, 0.0F, -0.5934F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(MudLurkerSalamanderRenderState state) {
        super.setupAnim(state);

        // All 8 animations in mud_lurker_salamander.bbmodel define full-body absolute poses
        // (including tucked-leg swim vs sprawled-leg crawl transitions), so apply mutually exclusively.
        if (state.swimAttackAnimationState.isStarted()) {
            this.swimAttackAnimation.apply(state.swimAttackAnimationState, state.ageInTicks);
        } else if (state.attackAnimationState.isStarted()) {
            this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        } else if (state.enterWaterAnimationState.isStarted()) {
            this.enterWaterAnimation.apply(state.enterWaterAnimationState, state.ageInTicks);
        } else if (state.exitWaterAnimationState.isStarted()) {
            this.exitWaterAnimation.apply(state.exitWaterAnimationState, state.ageInTicks);
        } else if (state.inFluid) {
            if (state.swimAnimationState.isStarted()) {
                this.swimAnimation.apply(state.swimAnimationState, state.ageInTicks);
            } else {
                this.swimIdleAnimation.apply(state.swimIdleAnimationState, state.ageInTicks);
            }
        } else {
            if (state.walkAnimationState.isStarted()) {
                this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
            } else {
                this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
            }
        }

        if (!state.attackAnimationState.isStarted() && !state.swimAttackAnimationState.isStarted()) {
            float clampedYaw = Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD;
            float clampedPitch = Mth.clamp(state.xRot, -20.0F, 20.0F) * Mth.DEG_TO_RAD;
            this.neck_grp.yRot += clampedYaw * 0.35F;
            this.head_grp.yRot += clampedYaw * 0.55F;
            this.head_grp.xRot += clampedPitch * 0.55F;
        }
    }
}
