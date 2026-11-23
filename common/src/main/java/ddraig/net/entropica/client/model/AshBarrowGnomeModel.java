package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.AshBarrowGnomeAnimations;
import ddraig.net.entropica.client.renderer.AshBarrowGnomeRenderState;
import ddraig.net.entropica.entity.ash_barrow_gnome.AshBarrowGnomeEntity;
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
 * Fauna 47 — Ash Barrow-Gnome (Underground Digger).
 * Exact conversion of all 47 cubes, 29 bone groups, and 64x64 UVs from ash_barrow_gnome.bbmodel.
 */
public class AshBarrowGnomeModel extends EntityModel<AshBarrowGnomeRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "ash_barrow_gnome"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation digAnimation;
    private final KeyframeAnimation hideAnimation;
    private final KeyframeAnimation unhideAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public AshBarrowGnomeModel(ModelPart modelRoot) {
        super(modelRoot);
        ModelPart root = modelRoot.getChild("root");
        ModelPart hipsGrp = root.getChild("hips_grp");
        ModelPart chestGrp = hipsGrp.getChild("chest_grp");
        this.head_grp = chestGrp.getChild("head_grp");

        this.idleAnimation = AshBarrowGnomeAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = AshBarrowGnomeAnimations.WALK.bake(modelRoot);
        this.attackAnimation = AshBarrowGnomeAnimations.ATTACK.bake(modelRoot);
        this.digAnimation = AshBarrowGnomeAnimations.DIG.bake(modelRoot);
        this.hideAnimation = AshBarrowGnomeAnimations.HIDE.bake(modelRoot);
        this.unhideAnimation = AshBarrowGnomeAnimations.UNHIDE.bake(modelRoot);
        this.hurtAnimation = AshBarrowGnomeAnimations.HURT.bake(modelRoot);
        this.deathAnimation = AshBarrowGnomeAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition leg_l_grp = root.addOrReplaceChild("leg_l_grp", CubeListBuilder.create().texOffs(34, 36).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.3F, -4.0F, 0.2F));
        leg_l_grp.addOrReplaceChild("foot_l_grp", CubeListBuilder.create().texOffs(44, 10).addBox(-2.0F, 0.0F, -2.9F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

        PartDefinition leg_r_grp = root.addOrReplaceChild("leg_r_grp", CubeListBuilder.create().texOffs(34, 36).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.3F, -4.0F, 0.2F));
        leg_r_grp.addOrReplaceChild("foot_r_grp", CubeListBuilder.create().texOffs(44, 10).mirror().addBox(-2.0F, 0.0F, -2.9F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 3.0F, 0.0F));

        PartDefinition hips_grp = root.addOrReplaceChild("hips_grp", CubeListBuilder.create().texOffs(25, 28).addBox(-4.0F, -2.0F, -2.5F, 8.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition chest_grp = hips_grp.addOrReplaceChild("chest_grp", CubeListBuilder.create().texOffs(0, 14).addBox(-5.0F, -4.0F, -3.0F, 10.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(13, 36).addBox(-4.0F, -6.0F, 1.45F, 8.0F, 2.0F, 2.0F, new CubeDeformation(-0.02F))
                .texOffs(0, 6).addBox(-2.0F, -3.5F, 2.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(-0.15F)), PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition head_grp = chest_grp.addOrReplaceChild("head_grp", CubeListBuilder.create().texOffs(37, 0).addBox(-3.0F, -3.8F, -2.6F, 6.0F, 4.0F, 5.0F, new CubeDeformation(0.02F))
                .texOffs(19, 27).addBox(-2.6F, -3.2F, -3.2F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.04F))
                .texOffs(19, 27).mirror().addBox(0.6F, -3.2F, -3.2F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.04F)).mirror(false)
                .texOffs(55, 0).addBox(-1.0F, -2.4F, -3.8F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.24F))
                .texOffs(37, 0).addBox(-3.95F, -2.6F, -0.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(37, 0).mirror().addBox(2.95F, -2.6F, -0.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -4.2F, -1.0F));

        PartDefinition beard_grp = head_grp.addOrReplaceChild("beard_grp", CubeListBuilder.create().texOffs(27, 14).addBox(-3.0F, -0.5F, -0.7F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.04F)), PartPose.offset(0.0F, -0.4F, -2.6F));

        beard_grp.addOrReplaceChild("beard_center_grp", CubeListBuilder.create().texOffs(56, 39).addBox(-1.0F, -0.4F, -0.8F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.08F))
                .texOffs(58, 10).addBox(-1.0F, 3.0F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.4F, -0.2F, -0.1396F, 0.0F, 0.0F));

        beard_grp.addOrReplaceChild("beard_braid_l_grp", CubeListBuilder.create().texOffs(56, 26).addBox(-1.0F, -0.4F, -0.8F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.08F))
                .texOffs(23, 41).addBox(-1.0F, 1.2F, -0.9F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.18F))
                .texOffs(0, 42).addBox(-1.0F, 2.7F, -0.9F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.26F)), PartPose.offsetAndRotation(-2.0F, 0.3F, 0.0F, -0.0873F, 0.0F, 0.0349F));

        beard_grp.addOrReplaceChild("beard_braid_r_grp", CubeListBuilder.create().texOffs(56, 26).mirror().addBox(-1.0F, -0.4F, -0.8F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.08F)).mirror(false)
                .texOffs(23, 41).mirror().addBox(-1.0F, 1.2F, -0.9F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.18F)).mirror(false)
                .texOffs(0, 42).mirror().addBox(-1.0F, 2.7F, -0.9F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.26F)).mirror(false), PartPose.offsetAndRotation(2.0F, 0.3F, 0.0F, -0.0873F, 0.0F, -0.0349F));

        PartDefinition hat_grp = head_grp.addOrReplaceChild("hat_grp", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -0.8F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(25, 17).addBox(-4.0F, -2.8F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-3.0F, -4.8F, -2.7F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.2F, 0.2F, -0.1571F, 0.0F, 0.0F));

        PartDefinition hat_tip_grp = hat_grp.addOrReplaceChild("hat_tip_grp", CubeListBuilder.create().texOffs(48, 32).addBox(-2.0F, -1.8F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(10, 41).addBox(-1.5F, -3.6F, -1.1F, 3.0F, 2.0F, 3.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.0F, -4.8F, 0.5F, -0.1745F, 0.0F, 0.0F));

        hat_tip_grp.addOrReplaceChild("hat_crook_grp", CubeListBuilder.create().texOffs(43, 39).addBox(-1.0F, -0.2F, -0.2F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, -3.4F, 1.1F, -0.384F, 0.0F, 0.0F));

        PartDefinition arm_l_grp = chest_grp.addOrReplaceChild("arm_l_grp", CubeListBuilder.create().texOffs(50, 17).addBox(-2.5F, -0.4F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -3.8F, 0.0F, 0.1745F, 0.0F, 0.3142F));
        PartDefinition forearm_l_grp = arm_l_grp.addOrReplaceChild("forearm_l_grp", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, -0.7F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.12F)), PartPose.offsetAndRotation(-1.0F, 3.3F, 0.0F, -0.384F, 0.0F, -0.0698F));
        PartDefinition hand_l_grp = forearm_l_grp.addOrReplaceChild("hand_l_grp", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -0.6F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.04F)), PartPose.offset(0.0F, 3.1F, 0.0F));

        PartDefinition claw_fingers_l_grp = hand_l_grp.addOrReplaceChild("claw_fingers_l_grp", CubeListBuilder.create().texOffs(0, 14).addBox(-0.5F, -0.4F, -1.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F))
                .texOffs(0, 14).addBox(-0.5F, -0.4F, 0.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-0.6F, 1.0F, 0.0F, 0.0F, 0.0F, 0.3491F));
        claw_fingers_l_grp.addOrReplaceChild("claw_tips_l_grp", CubeListBuilder.create().texOffs(60, 5).addBox(-0.5F, -0.7F, -1.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.16F))
                .texOffs(60, 5).addBox(-0.5F, -0.7F, 0.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.16F)), PartPose.offsetAndRotation(0.0F, 1.3F, 0.0F, 0.0F, 0.0F, -0.733F));

        PartDefinition claw_thumb_l_grp = hand_l_grp.addOrReplaceChild("claw_thumb_l_grp", CubeListBuilder.create().texOffs(0, 14).addBox(-0.5F, -0.4F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.8F, 0.9F, -0.5F, 0.0F, 0.0F, -0.4189F));
        claw_thumb_l_grp.addOrReplaceChild("claw_thumb_tip_l_grp", CubeListBuilder.create().texOffs(60, 5).addBox(-0.5F, -0.7F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.16F)), PartPose.offsetAndRotation(0.0F, 1.3F, 0.0F, 0.0F, 0.0F, 0.6981F));

        PartDefinition arm_r_grp = chest_grp.addOrReplaceChild("arm_r_grp", CubeListBuilder.create().texOffs(50, 17).mirror().addBox(-0.5F, -0.4F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, -3.8F, 0.0F, 0.1745F, 0.0F, -0.3142F));
        PartDefinition forearm_r_grp = arm_r_grp.addOrReplaceChild("forearm_r_grp", CubeListBuilder.create().texOffs(0, 34).mirror().addBox(-1.5F, -0.7F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.12F)).mirror(false), PartPose.offsetAndRotation(1.0F, 3.3F, 0.0F, -0.384F, 0.0F, 0.0698F));
        PartDefinition hand_r_grp = forearm_r_grp.addOrReplaceChild("hand_r_grp", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -0.6F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.04F)).mirror(false), PartPose.offset(0.0F, 3.1F, 0.0F));

        PartDefinition claw_fingers_r_grp = hand_r_grp.addOrReplaceChild("claw_fingers_r_grp", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(-0.5F, -0.4F, -1.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)).mirror(false)
                .texOffs(0, 14).mirror().addBox(-0.5F, -0.4F, 0.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)).mirror(false), PartPose.offsetAndRotation(0.6F, 1.0F, 0.0F, 0.0F, 0.0F, -0.3491F));
        claw_fingers_r_grp.addOrReplaceChild("claw_tips_r_grp", CubeListBuilder.create().texOffs(60, 5).mirror().addBox(-0.5F, -0.7F, -1.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.16F)).mirror(false)
                .texOffs(60, 5).mirror().addBox(-0.5F, -0.7F, 0.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.16F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.3F, 0.0F, 0.0F, 0.0F, 0.733F));

        PartDefinition claw_thumb_r_grp = hand_r_grp.addOrReplaceChild("claw_thumb_r_grp", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(-0.5F, -0.4F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)).mirror(false), PartPose.offsetAndRotation(-0.8F, 0.9F, -0.5F, 0.0F, 0.0F, 0.4189F));
        claw_thumb_r_grp.addOrReplaceChild("claw_thumb_tip_r_grp", CubeListBuilder.create().texOffs(60, 5).mirror().addBox(-0.5F, -0.7F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.16F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.3F, 0.0F, 0.0F, 0.0F, -0.6981F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(AshBarrowGnomeRenderState state) {
        super.setupAnim(state);

        if (state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }
        if (state.cairnState == AshBarrowGnomeEntity.STATE_HIDING || state.hideAnimationState.isStarted()) {
            this.hideAnimation.apply(state.hideAnimationState, state.ageInTicks);
            return;
        }
        if (state.unhideAnimationState.isStarted()) {
            this.unhideAnimation.apply(state.unhideAnimationState, state.ageInTicks);
            return;
        }

        if (state.attackAnimationState.isStarted()) {
            this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        } else if (state.digAnimationState.isStarted()) {
            this.digAnimation.apply(state.digAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        if (state.hurtAnimationState.isStarted()) {
            this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);
        }

        if (!state.digAnimationState.isStarted()) {
            float yawRad = Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD;
            float pitchRad = Mth.clamp(state.xRot, -20.0F, 20.0F) * Mth.DEG_TO_RAD;
            this.head_grp.yRot += yawRad * 0.75F;
            this.head_grp.xRot += pitchRad * 0.65F;
        }
    }
}
