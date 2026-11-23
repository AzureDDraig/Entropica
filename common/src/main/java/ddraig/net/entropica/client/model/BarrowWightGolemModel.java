package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.BarrowWightGolemAnimations;
import ddraig.net.entropica.client.renderer.BarrowWightGolemRenderState;
import ddraig.net.entropica.entity.barrow_wight_golem.BarrowWightGolemEntity;
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
 * Fauna 52 — Barrow Wight Golem (Crypt Sentinel).
 * Exact conversion of all 37 cubes, 19 bone groups, and 128x128 UVs from barrow_wight_golem.bbmodel.
 */
public class BarrowWightGolemModel extends EntityModel<BarrowWightGolemRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_wight_golem"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation soulfireBurstAnimation;
    private final KeyframeAnimation dormantEnterAnimation;
    private final KeyframeAnimation dormantAnimation;
    private final KeyframeAnimation awakenAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public BarrowWightGolemModel(ModelPart modelRoot) {
        super(modelRoot);
        ModelPart root = modelRoot.getChild("root");
        ModelPart chestGrp = root.getChild("body_grp").getChild("waist_grp").getChild("chest_grp");
        this.head_grp = chestGrp.getChild("neck_grp").getChild("head_grp");

        this.idleAnimation = BarrowWightGolemAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = BarrowWightGolemAnimations.WALK.bake(modelRoot);
        this.attackAnimation = BarrowWightGolemAnimations.ATTACK.bake(modelRoot);
        this.soulfireBurstAnimation = BarrowWightGolemAnimations.SOULFIRE_BURST.bake(modelRoot);
        this.dormantEnterAnimation = BarrowWightGolemAnimations.DORMANT_ENTER.bake(modelRoot);
        this.dormantAnimation = BarrowWightGolemAnimations.DORMANT.bake(modelRoot);
        this.awakenAnimation = BarrowWightGolemAnimations.AWAKEN.bake(modelRoot);
        this.hurtAnimation = BarrowWightGolemAnimations.HURT.bake(modelRoot);
        this.deathAnimation = BarrowWightGolemAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_grp = root.addOrReplaceChild("body_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

        PartDefinition waist_grp = body_grp.addOrReplaceChild("waist_grp", CubeListBuilder.create().texOffs(50, 48).addBox(-6.5F, -4.0F, -4.5F, 13.0F, 6.0F, 9.0F, new CubeDeformation(-1.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        waist_grp.addOrReplaceChild("apron_grp", CubeListBuilder.create().texOffs(64, 78).addBox(-4.5F, -1.5F, -2.2F, 9.0F, 8.0F, 4.0F, new CubeDeformation(-1.52F)), PartPose.offset(0.0F, 0.2F, -3.5F));

        PartDefinition chest_grp = waist_grp.addOrReplaceChild("chest_grp", CubeListBuilder.create()
                .texOffs(0, 38).addBox(-7.5F, -3.0F, -5.0F, 15.0F, 4.0F, 10.0F, new CubeDeformation(-1.0F))
                .texOffs(96, 24).addBox(-8.5F, -11.0F, -5.0F, 5.0F, 10.0F, 10.0F, new CubeDeformation(-1.0F))
                .texOffs(96, 44).addBox(3.5F, -11.0F, -5.0F, 5.0F, 10.0F, 10.0F, new CubeDeformation(-1.0F))
                .texOffs(32, 52).addBox(-5.5F, -11.0F, -4.9F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-1.02F))
                .texOffs(32, 52).mirror().addBox(1.5F, -11.0F, -4.9F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-1.02F)).mirror(false)
                .texOffs(112, 116).addBox(-5.5F, -5.0F, -4.9F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-1.02F))
                .texOffs(112, 116).mirror().addBox(1.5F, -5.0F, -4.9F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-1.02F)).mirror(false)
                .texOffs(70, 114).addBox(-2.5F, -11.0F, 1.2F, 5.0F, 10.0F, 4.0F, new CubeDeformation(-1.02F))
                .texOffs(0, 22).addBox(-9.0F, -14.0F, -5.5F, 18.0F, 5.0F, 11.0F, new CubeDeformation(-1.0F))
                .texOffs(74, 90).addBox(-4.5F, -14.5F, 3.2F, 9.0F, 6.0F, 4.0F, new CubeDeformation(-1.02F))
                .texOffs(22, 117).addBox(-9.0F, -16.0F, -3.5F, 5.0F, 4.0F, 7.0F, new CubeDeformation(-1.01F))
                .texOffs(46, 117).addBox(4.0F, -16.0F, -3.5F, 5.0F, 4.0F, 7.0F, new CubeDeformation(-1.01F)),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        chest_grp.addOrReplaceChild("soul_core_grp", CubeListBuilder.create().texOffs(46, 0).addBox(-5.5F, -5.5F, -5.5F, 11.0F, 11.0F, 11.0F, new CubeDeformation(-3.0F)), PartPose.offset(0.0F, -6.0F, -0.5F));

        PartDefinition neck_grp = chest_grp.addOrReplaceChild("neck_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -13.0F, -0.5F));

        PartDefinition head_grp = neck_grp.addOrReplaceChild("head_grp", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.0F, -9.0F, -5.5F, 12.0F, 11.0F, 11.0F, new CubeDeformation(-2.0F))
                .texOffs(88, 108).addBox(-5.5F, -8.4F, -5.1F, 11.0F, 4.0F, 4.0F, new CubeDeformation(-1.02F))
                .texOffs(34, 78).addBox(-4.0F, -10.0F, -3.5F, 8.0F, 4.0F, 7.0F, new CubeDeformation(-1.01F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head_grp.addOrReplaceChild("jaw_grp", CubeListBuilder.create().texOffs(88, 116).addBox(-4.0F, -1.3F, -3.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(-1.03F)), PartPose.offset(0.0F, -0.5F, -2.0F));

        PartDefinition arm_l_grp = chest_grp.addOrReplaceChild("arm_l_grp", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-4.0F, -3.5F, -4.5F, 7.0F, 6.0F, 9.0F, new CubeDeformation(-1.02F))
                .texOffs(0, 92).addBox(-2.5F, 0.5F, -3.0F, 5.0F, 10.0F, 6.0F, new CubeDeformation(-0.52F)),
                PartPose.offset(-9.5F, -11.0F, 0.0F));

        arm_l_grp.addOrReplaceChild("forearm_l_grp", CubeListBuilder.create()
                .texOffs(90, 0).addBox(-4.5F, -2.7F, -5.0F, 9.0F, 14.0F, 10.0F, new CubeDeformation(-2.0F))
                .texOffs(0, 67).addBox(-3.5F, 3.1F, -4.0F, 7.0F, 3.0F, 8.0F, new CubeDeformation(-0.65F))
                .texOffs(64, 64).addBox(-4.0F, 8.3F, -4.5F, 8.0F, 5.0F, 9.0F, new CubeDeformation(-1.0F)),
                PartPose.offset(0.0F, 9.2F, 0.0F));

        PartDefinition arm_r_grp = chest_grp.addOrReplaceChild("arm_r_grp", CubeListBuilder.create()
                .texOffs(32, 63).addBox(-3.0F, -3.5F, -4.5F, 7.0F, 6.0F, 9.0F, new CubeDeformation(-1.02F))
                .texOffs(0, 108).addBox(-2.5F, 0.5F, -3.0F, 5.0F, 10.0F, 6.0F, new CubeDeformation(-0.52F)),
                PartPose.offset(9.5F, -11.0F, 0.0F));

        arm_r_grp.addOrReplaceChild("forearm_r_grp", CubeListBuilder.create()
                .texOffs(58, 24).addBox(-4.5F, -2.7F, -5.0F, 9.0F, 14.0F, 10.0F, new CubeDeformation(-2.0F))
                .texOffs(22, 92).addBox(-3.5F, 3.1F, -4.0F, 7.0F, 3.0F, 8.0F, new CubeDeformation(-0.65F))
                .texOffs(0, 78).addBox(-4.0F, 8.3F, -4.5F, 8.0F, 5.0F, 9.0F, new CubeDeformation(-1.0F)),
                PartPose.offset(0.0F, 9.2F, 0.0F));

        PartDefinition leg_l_grp = body_grp.addOrReplaceChild("leg_l_grp", CubeListBuilder.create()
                .texOffs(22, 103).addBox(-2.5F, -1.5F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(-0.52F))
                .texOffs(100, 88).addBox(-3.0F, 4.0F, -3.5F, 6.0F, 3.0F, 7.0F, new CubeDeformation(-0.62F)),
                PartPose.offset(-3.5F, 0.5F, 0.0F));
        PartDefinition shin_l_grp = leg_l_grp.addOrReplaceChild("shin_l_grp", CubeListBuilder.create().texOffs(52, 90).addBox(-2.5F, -1.0F, -3.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(-0.55F)), PartPose.offset(0.0F, 5.5F, 0.0F));
        shin_l_grp.addOrReplaceChild("foot_l_grp", CubeListBuilder.create().texOffs(98, 64).addBox(-3.5F, -1.0F, -4.5F, 7.0F, 4.0F, 8.0F, new CubeDeformation(-1.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition leg_r_grp = body_grp.addOrReplaceChild("leg_r_grp", CubeListBuilder.create()
                .texOffs(44, 103).addBox(-2.5F, -1.5F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(-0.52F))
                .texOffs(100, 98).addBox(-3.0F, 4.0F, -3.5F, 6.0F, 3.0F, 7.0F, new CubeDeformation(-0.62F)),
                PartPose.offset(3.5F, 0.5F, 0.0F));
        PartDefinition shin_r_grp = leg_r_grp.addOrReplaceChild("shin_r_grp", CubeListBuilder.create().texOffs(66, 102).addBox(-2.5F, -1.0F, -3.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(-0.55F)), PartPose.offset(0.0F, 5.5F, 0.0F));
        shin_r_grp.addOrReplaceChild("foot_r_grp", CubeListBuilder.create().texOffs(98, 76).addBox(-3.5F, -1.0F, -4.5F, 7.0F, 4.0F, 8.0F, new CubeDeformation(-1.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(BarrowWightGolemRenderState state) {
        super.setupAnim(state);

        if (state.deathTime > 0.0F || state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        if (state.sentinelState == BarrowWightGolemEntity.STATE_DORMANT) {
            this.dormantAnimation.apply(state.dormantAnimationState, state.ageInTicks);
            return;
        }

        if (state.sentinelState == BarrowWightGolemEntity.STATE_ENTERING_DORMANT) {
            this.dormantEnterAnimation.apply(state.dormantEnterAnimationState, state.ageInTicks);
            return;
        }

        if (state.sentinelState == BarrowWightGolemEntity.STATE_AWAKENING) {
            this.awakenAnimation.apply(state.awakenAnimationState, state.ageInTicks);
            return;
        }

        if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        this.soulfireBurstAnimation.apply(state.soulfireBurstAnimationState, state.ageInTicks);
        this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);

        this.head_grp.xRot += Mth.clamp(state.xRot, -22.0F, 22.0F) * Mth.DEG_TO_RAD * 0.6F;
        this.head_grp.yRot += Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD * 0.6F;
    }
}
