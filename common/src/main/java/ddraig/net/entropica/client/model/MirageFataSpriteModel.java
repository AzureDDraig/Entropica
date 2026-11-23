package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.MirageFataSpriteAnimations;
import ddraig.net.entropica.client.renderer.MirageFataSpriteRenderState;
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

/**
 * Fauna 43 — Mirage Fata-Sprite (Desert Sylph).
 * Exact conversion of all 31 cubes, 15 bone groups, and 64x64 UVs from mirage_fata_sprite.bbmodel.
 */
public class MirageFataSpriteModel extends EntityModel<MirageFataSpriteRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_fata_sprite"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation mirageIllusionAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public MirageFataSpriteModel(ModelPart modelRoot) {
        super(modelRoot);
        this.idleAnimation = MirageFataSpriteAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = MirageFataSpriteAnimations.WALK.bake(modelRoot);
        this.attackAnimation = MirageFataSpriteAnimations.ATTACK.bake(modelRoot);
        this.mirageIllusionAnimation = MirageFataSpriteAnimations.MIRAGE_ILLUSION.bake(modelRoot);
        this.hurtAnimation = MirageFataSpriteAnimations.HURT.bake(modelRoot);
        this.deathAnimation = MirageFataSpriteAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_root = root.addOrReplaceChild("body_root", CubeListBuilder.create(), PartPose.offset(0.0F, -18.0F, 0.0F));

        PartDefinition core_assembly = body_root.addOrReplaceChild("core_assembly", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        core_assembly.addOrReplaceChild("sun_core_grp", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(40, 0).addBox(-4.0F, -4.0F, -5.75F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 9).addBox(-4.0F, -4.0F, 4.75F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 20).addBox(4.75F, -4.0F, -4.0F, 1.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(18, 20).addBox(-5.75F, -4.0F, -4.0F, 1.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-4.0F, -5.75F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 45).addBox(-4.0F, 4.75F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        core_assembly.addOrReplaceChild("mirage_frame_grp", CubeListBuilder.create().texOffs(36, 20).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 20).addBox(-6.0F, -6.0F, 5.0F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 22).addBox(-6.0F, 5.0F, -6.0F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 22).addBox(-6.0F, 5.0F, 5.0F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 24).addBox(5.0F, -6.0F, -5.0F, 1.0F, 1.0F, 10.0F, new CubeDeformation(-0.005F))
                .texOffs(36, 24).mirror().addBox(-6.0F, -6.0F, -5.0F, 1.0F, 1.0F, 10.0F, new CubeDeformation(-0.005F)).mirror(false)
                .texOffs(36, 35).addBox(5.0F, 5.0F, -5.0F, 1.0F, 1.0F, 10.0F, new CubeDeformation(-0.005F))
                .texOffs(36, 35).mirror().addBox(-6.0F, 5.0F, -5.0F, 1.0F, 1.0F, 10.0F, new CubeDeformation(-0.005F)).mirror(false)
                .texOffs(58, 0).addBox(5.0F, -5.0F, -6.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.005F))
                .texOffs(58, 0).mirror().addBox(-6.0F, -5.0F, -6.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.005F)).mirror(false)
                .texOffs(58, 24).addBox(5.0F, -5.0F, 5.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.005F))
                .texOffs(58, 24).mirror().addBox(-6.0F, -5.0F, 5.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.005F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition orbit_gimbal_grp = body_root.addOrReplaceChild("orbit_gimbal_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition orbit_ring_grp = orbit_gimbal_grp.addOrReplaceChild("orbit_ring_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition shard_arm_a_grp = orbit_ring_grp.addOrReplaceChild("shard_arm_a_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        shard_arm_a_grp.addOrReplaceChild("shard_a_grp", CubeListBuilder.create().texOffs(0, 54).addBox(-4.0F, -2.0F, -0.5F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(18, 54).addBox(-3.0F, -1.0F, -1.25F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(18, 57).addBox(-3.0F, -1.0F, 0.25F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -14.0F));

        PartDefinition shard_arm_b_grp = orbit_ring_grp.addOrReplaceChild("shard_arm_b_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        shard_arm_b_grp.addOrReplaceChild("shard_b_grp", CubeListBuilder.create().texOffs(32, 46).addBox(-0.5F, -2.0F, -4.0F, 1.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(50, 46).addBox(-1.25F, -1.0F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(50, 54).addBox(0.25F, -1.0F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-14.0F, 0.0F, 0.0F));

        PartDefinition shard_arm_c_grp = orbit_ring_grp.addOrReplaceChild("shard_arm_c_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        shard_arm_c_grp.addOrReplaceChild("shard_c_grp", CubeListBuilder.create().texOffs(0, 59).addBox(-4.0F, -2.0F, -0.5F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(18, 54).mirror().addBox(-3.0F, -1.0F, 0.25F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(18, 57).mirror().addBox(-3.0F, -1.0F, -1.25F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 14.0F));

        PartDefinition shard_arm_d_grp = orbit_ring_grp.addOrReplaceChild("shard_arm_d_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        shard_arm_d_grp.addOrReplaceChild("shard_d_grp", CubeListBuilder.create().texOffs(32, 46).mirror().addBox(-0.5F, -2.0F, -4.0F, 1.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(50, 46).mirror().addBox(0.25F, -1.0F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(50, 54).mirror().addBox(-1.25F, -1.0F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(14.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(MirageFataSpriteRenderState state) {
        super.setupAnim(state);

        if (state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
        } else if (state.mirageIllusionAnimationState.isStarted()) {
            this.mirageIllusionAnimation.apply(state.mirageIllusionAnimationState, state.ageInTicks);
        } else if (state.attackAnimationState.isStarted()) {
            this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        } else if (state.hurtAnimationState.isStarted()) {
            this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }
    }
}
