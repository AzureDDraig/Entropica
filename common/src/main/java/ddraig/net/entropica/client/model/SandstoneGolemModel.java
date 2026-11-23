package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.SandstoneGolemAnimations;
import ddraig.net.entropica.client.renderer.SandstoneGolemRenderState;
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
 * Fauna 39 — Sandstone Golem (Desert Elemental).
 * Exact conversion of all 25 cubes, 16 bone groups, and 64x64 UVs from sandstone_golem.bbmodel.
 */
public class SandstoneGolemModel extends EntityModel<SandstoneGolemRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sandstone_golem"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart root;
    private final ModelPart body_root;
    private final ModelPart chest_frame_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation repairAnimation;

    public SandstoneGolemModel(ModelPart modelRoot) {
        super(modelRoot);
        this.root = modelRoot.getChild("root");
        this.body_root = this.root.getChild("body_root");
        this.chest_frame_grp = this.body_root.getChild("chest_frame_grp");

        this.idleAnimation = SandstoneGolemAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = SandstoneGolemAnimations.WALK.bake(modelRoot);
        this.attackAnimation = SandstoneGolemAnimations.ATTACK.bake(modelRoot);
        this.repairAnimation = SandstoneGolemAnimations.REPAIR.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_root = root.addOrReplaceChild("body_root", CubeListBuilder.create(), PartPose.offset(0.0F, -15.0F, 0.0F));

        body_root.addOrReplaceChild("core_heart_grp", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition core_shards_grp = body_root.addOrReplaceChild("core_shards_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        core_shards_grp.addOrReplaceChild("shard_lower_r_r1", CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(5.8F, 4.6F, 3.8F, -0.3142F, 0.4189F, 0.2094F));
        core_shards_grp.addOrReplaceChild("shard_lower_l_r1", CubeListBuilder.create().texOffs(18, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-5.8F, 4.6F, -3.8F, 0.3142F, -0.4189F, -0.2094F));
        core_shards_grp.addOrReplaceChild("shard_upper_r_r1", CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(6.8F, -7.8F, 1.6F, 0.2793F, -0.384F, -0.2443F));
        core_shards_grp.addOrReplaceChild("shard_upper_l_r1", CubeListBuilder.create().texOffs(18, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-6.8F, -7.8F, -1.6F, -0.2793F, 0.384F, 0.2443F));

        body_root.addOrReplaceChild("chest_frame_grp", CubeListBuilder.create().texOffs(0, 22).addBox(-5.0F, -6.6F, -3.5F, 10.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(48, 35).addBox(-3.0F, -4.65F, -3.4F, 6.0F, 1.0F, 2.0F, new CubeDeformation(-0.03F))
                .texOffs(48, 35).addBox(-3.0F, -4.65F, 1.4F, 6.0F, 1.0F, 2.0F, new CubeDeformation(-0.03F))
                .texOffs(48, 0).addBox(-6.4F, -4.8F, -2.5F, 2.0F, 6.0F, 5.0F, new CubeDeformation(-0.04F))
                .texOffs(41, 11).addBox(4.4F, -4.8F, -2.5F, 2.0F, 6.0F, 5.0F, new CubeDeformation(-0.04F))
                .texOffs(16, 31).addBox(-3.0F, 4.2F, -2.5F, 6.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition orbit_vortex_grp = body_root.addOrReplaceChild("orbit_vortex_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition arm_l_orbit = orbit_vortex_grp.addOrReplaceChild("arm_l_orbit", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition arm_l_grp = arm_l_orbit.addOrReplaceChild("arm_l_grp", CubeListBuilder.create(), PartPose.offset(-10.0F, -2.2F, 0.0F));
        arm_l_grp.addOrReplaceChild("arm_shoulder_l_r1", CubeListBuilder.create().texOffs(46, 27).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.0698F));

        PartDefinition hammer_l_grp = arm_l_grp.addOrReplaceChild("hammer_l_grp", CubeListBuilder.create(), PartPose.offset(-0.2F, 3.0F, 0.0F));
        hammer_l_grp.addOrReplaceChild("hammer_cap_bl_r1", CubeListBuilder.create().texOffs(41, 0).addBox(-2.0F, 5.6F, 3.2F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(41, 0).addBox(-2.0F, 5.6F, -4.2F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(24, 0).addBox(-2.5F, 4.6F, -3.5F, 5.0F, 4.0F, 7.0F, new CubeDeformation(0.03F))
                .texOffs(52, 19).addBox(-1.5F, -0.2F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0698F));

        PartDefinition arm_r_orbit = orbit_vortex_grp.addOrReplaceChild("arm_r_orbit", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition arm_r_grp = arm_r_orbit.addOrReplaceChild("arm_r_grp", CubeListBuilder.create(), PartPose.offset(10.0F, -2.2F, 0.0F));
        arm_r_grp.addOrReplaceChild("arm_shoulder_r_r1", CubeListBuilder.create().texOffs(0, 31).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0698F));

        PartDefinition hammer_r_grp = arm_r_grp.addOrReplaceChild("hammer_r_grp", CubeListBuilder.create(), PartPose.offset(0.2F, 3.0F, 0.0F));
        hammer_r_grp.addOrReplaceChild("hammer_cap_br_r1", CubeListBuilder.create().texOffs(41, 0).mirror().addBox(-2.0F, 5.6F, 3.2F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(41, 0).mirror().addBox(-2.0F, 5.6F, -4.2F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(17, 11).addBox(-2.5F, 4.6F, -3.5F, 5.0F, 4.0F, 7.0F, new CubeDeformation(0.03F))
                .texOffs(52, 19).mirror().addBox(-1.5F, -0.2F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, -0.0698F));

        PartDefinition leg_l_orbit = orbit_vortex_grp.addOrReplaceChild("leg_l_orbit", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        leg_l_orbit.addOrReplaceChild("leg_l_grp", CubeListBuilder.create().texOffs(0, 12).addBox(-2.0F, -0.1F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.02F))
                .texOffs(33, 35).addBox(-2.5F, 4.8F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.8F, 8.2F, 0.0F));

        PartDefinition leg_r_orbit = orbit_vortex_grp.addOrReplaceChild("leg_r_orbit", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        leg_r_orbit.addOrReplaceChild("leg_r_grp", CubeListBuilder.create().texOffs(34, 22).addBox(-2.0F, -0.1F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.02F))
                .texOffs(33, 35).mirror().addBox(-2.5F, 4.8F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.8F, 8.2F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(SandstoneGolemRenderState state) {
        super.setupAnim(state);

        if (!state.attackAnimationState.isStarted() && !state.repairAnimationState.isStarted()) {
            float clampedYaw = Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD;
            float clampedPitch = Mth.clamp(state.xRot, -20.0F, 20.0F) * Mth.DEG_TO_RAD;
            this.chest_frame_grp.yRot += clampedYaw * 0.45F;
            this.chest_frame_grp.xRot += clampedPitch * 0.35F;
        }

        if (state.attackAnimationState.isStarted()) {
            this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        } else if (state.repairAnimationState.isStarted()) {
            this.repairAnimation.apply(state.repairAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }
    }
}
