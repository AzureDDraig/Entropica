package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.AshCinderWispAnimations;
import ddraig.net.entropica.client.renderer.AshCinderWispRenderState;
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
 * Fauna 49 — Ash-Cinder Wisp (Will-o'-Wisp).
 * Exact conversion of all 25 cubes, 22 bone groups, and 64x64 UVs from ash_cinder_wisp.bbmodel.
 */
public class AshCinderWispModel extends EntityModel<AshCinderWispRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "ash_cinder_wisp"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart wisp_assembly;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation flutterLightAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public AshCinderWispModel(ModelPart modelRoot) {
        super(modelRoot);
        ModelPart root = modelRoot.getChild("root");
        this.wisp_assembly = root.getChild("wisp_assembly");

        this.idleAnimation = AshCinderWispAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = AshCinderWispAnimations.WALK.bake(modelRoot);
        this.attackAnimation = AshCinderWispAnimations.ATTACK.bake(modelRoot);
        this.flutterLightAnimation = AshCinderWispAnimations.FLUTTER_LIGHT.bake(modelRoot);
        this.hurtAnimation = AshCinderWispAnimations.HURT.bake(modelRoot);
        this.deathAnimation = AshCinderWispAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition wisp_assembly = root.addOrReplaceChild("wisp_assembly", CubeListBuilder.create(), PartPose.offset(0.0F, -16.0F, 0.0F));

        wisp_assembly.addOrReplaceChild("wisp_core_grp", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(40, 21).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(-0.005F))
                .texOffs(25, 21).addBox(-3.0F, -3.0F, 4.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(-0.005F))
                .texOffs(33, 0).addBox(4.0F, -3.0F, -3.0F, 1.0F, 6.0F, 6.0F, new CubeDeformation(-0.005F))
                .texOffs(48, 0).addBox(-5.0F, -3.0F, -3.0F, 1.0F, 6.0F, 6.0F, new CubeDeformation(-0.005F))
                .texOffs(27, 13).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(-0.005F))
                .texOffs(0, 17).addBox(-3.0F, 4.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(-0.005F))
                .texOffs(42, 0).addBox(-2.0F, -2.0F, -5.8F, 4.0F, 4.0F, 1.0F, new CubeDeformation(-0.08F))
                .texOffs(25, 0).addBox(-2.0F, -2.0F, 4.8F, 4.0F, 4.0F, 1.0F, new CubeDeformation(-0.08F))
                .texOffs(54, 13).addBox(4.8F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(-0.08F))
                .texOffs(0, 25).addBox(-5.8F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(-0.08F))
                .texOffs(11, 25).addBox(-2.0F, -5.8F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(-0.08F))
                .texOffs(24, 29).addBox(-2.0F, 4.8F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(-0.08F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition orbit_gimbal_grp = wisp_assembly.addOrReplaceChild("orbit_gimbal_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, -0.1047F));

        PartDefinition orbit_ring_grp = orbit_gimbal_grp.addOrReplaceChild("orbit_ring_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0123F, 0.0F));

        PartDefinition flake_arm_a_grp = orbit_ring_grp.addOrReplaceChild("flake_arm_a_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flake_a_grp = flake_arm_a_grp.addOrReplaceChild("flake_a_grp", CubeListBuilder.create().texOffs(11, 31).addBox(-2.0F, -2.5F, -0.5F, 4.0F, 5.0F, 1.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(0.0F, -0.4F, -9.8F, -0.1047F, 0.0F, -0.2094F));
        flake_a_grp.addOrReplaceChild("flake_upper_a_grp", CubeListBuilder.create().texOffs(55, 22).addBox(-1.5F, -2.8F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(-0.22F)), PartPose.offsetAndRotation(-0.7F, -2.0F, 0.0F, 0.1396F, 0.0873F, 0.2443F));
        flake_a_grp.addOrReplaceChild("flake_lower_a_grp", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, -0.2F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(-0.22F)), PartPose.offsetAndRotation(0.7F, 2.0F, 0.0F, -0.1745F, -0.0873F, -0.2618F));

        PartDefinition flake_arm_b_grp = orbit_ring_grp.addOrReplaceChild("flake_arm_b_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));
        PartDefinition flake_b_grp = flake_arm_b_grp.addOrReplaceChild("flake_b_grp", CubeListBuilder.create().texOffs(41, 29).addBox(-2.0F, -2.5F, -0.5F, 4.0F, 5.0F, 1.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(0.0F, 0.5F, -9.8F, -0.1047F, 0.0F, -0.2094F));
        flake_b_grp.addOrReplaceChild("flake_upper_b_grp", CubeListBuilder.create().texOffs(55, 22).mirror().addBox(-1.5F, -2.8F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(-0.22F)).mirror(false), PartPose.offsetAndRotation(-0.7F, -2.0F, 0.0F, 0.1396F, 0.0873F, 0.2443F));
        flake_b_grp.addOrReplaceChild("flake_lower_b_grp", CubeListBuilder.create().texOffs(0, 34).mirror().addBox(-1.5F, -0.2F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(-0.22F)).mirror(false), PartPose.offsetAndRotation(0.7F, 2.0F, 0.0F, -0.1745F, -0.0873F, -0.2618F));

        PartDefinition flake_arm_c_grp = orbit_ring_grp.addOrReplaceChild("flake_arm_c_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -4.1888F, 0.0F));
        PartDefinition flake_c_grp = flake_arm_c_grp.addOrReplaceChild("flake_c_grp", CubeListBuilder.create().texOffs(54, 28).addBox(-2.0F, -2.5F, -0.5F, 4.0F, 5.0F, 1.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(0.0F, -0.1F, -9.8F, -0.1047F, 0.0F, -0.2094F));
        flake_c_grp.addOrReplaceChild("flake_upper_c_grp", CubeListBuilder.create().texOffs(55, 22).addBox(-1.5F, -2.8F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(-0.22F)), PartPose.offsetAndRotation(-0.7F, -2.0F, 0.0F, 0.1396F, 0.0873F, 0.2443F));
        flake_c_grp.addOrReplaceChild("flake_lower_c_grp", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, -0.2F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(-0.22F)), PartPose.offsetAndRotation(0.7F, 2.0F, 0.0F, -0.1745F, -0.0873F, -0.2618F));

        PartDefinition cinder_gimbal_grp = wisp_assembly.addOrReplaceChild("cinder_gimbal_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2443F, 0.0F, 0.1745F));

        PartDefinition cinder_ring_grp = cinder_gimbal_grp.addOrReplaceChild("cinder_ring_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0595F, 0.0F));

        PartDefinition spark_arm_a_grp = cinder_ring_grp.addOrReplaceChild("spark_arm_a_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        spark_arm_a_grp.addOrReplaceChild("cinder_spark_a_r1", CubeListBuilder.create().texOffs(22, 35).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.48F)), PartPose.offsetAndRotation(0.0F, -1.2F, -8.8F, -0.6109F, -0.3142F, 0.6109F));

        PartDefinition spark_arm_b_grp = cinder_ring_grp.addOrReplaceChild("spark_arm_b_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));
        spark_arm_b_grp.addOrReplaceChild("cinder_spark_b_r1", CubeListBuilder.create().texOffs(22, 35).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.48F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.3F, -8.8F, -0.6109F, -0.3142F, 0.6109F));

        PartDefinition spark_arm_c_grp = cinder_ring_grp.addOrReplaceChild("spark_arm_c_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -4.1888F, 0.0F));
        spark_arm_c_grp.addOrReplaceChild("cinder_spark_c_r1", CubeListBuilder.create().texOffs(22, 35).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.48F)), PartPose.offsetAndRotation(0.0F, -0.3F, -8.8F, -0.6109F, -0.3142F, 0.6109F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(AshCinderWispRenderState state) {
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

        this.flutterLightAnimation.apply(state.flutterLightAnimationState, state.ageInTicks);
        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);

        float pitchRad = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD * 0.35F;
        float yawRad = Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD * 0.35F;
        this.wisp_assembly.xRot += pitchRad;
        this.wisp_assembly.yRot += yawRad;
    }
}
