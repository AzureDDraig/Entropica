package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.BrimstoneChimeraAnimations;
import ddraig.net.entropica.client.renderer.BrimstoneChimeraRenderState;
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
 * Fauna 58 — Brimstone Chimera (Lava Beast).
 * Exact conversion of all 54 cubes, 32 bone groups, and 128x128 UVs from brimstone_chimera.bbmodel.
 */
public class BrimstoneChimeraModel extends EntityModel<BrimstoneChimeraRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "brimstone_chimera"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart lion_head_grp;
    private final ModelPart goat_head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation runAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation magmaBreathAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public BrimstoneChimeraModel(ModelPart modelRoot) {
        super(modelRoot);
        ModelPart root = modelRoot.getChild("root");
        ModelPart chestGrp = root.getChild("body_grp").getChild("chest_grp");
        this.lion_head_grp = chestGrp.getChild("lion_neck_grp").getChild("lion_head_grp");
        this.goat_head_grp = chestGrp.getChild("goat_neck_grp").getChild("goat_head_grp");

        this.idleAnimation = BrimstoneChimeraAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = BrimstoneChimeraAnimations.WALK.bake(modelRoot);
        this.runAnimation = BrimstoneChimeraAnimations.RUN.bake(modelRoot);
        this.attackAnimation = BrimstoneChimeraAnimations.ATTACK.bake(modelRoot);
        this.magmaBreathAnimation = BrimstoneChimeraAnimations.MAGMA_BREATH.bake(modelRoot);
        this.hurtAnimation = BrimstoneChimeraAnimations.HURT.bake(modelRoot);
        this.deathAnimation = BrimstoneChimeraAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_grp = root.addOrReplaceChild("body_grp", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.0F, -6.2F, -5.0F, 12.0F, 12.0F, 14.0F, new CubeDeformation(-2.02F)),
                PartPose.offset(0.0F, -15.0F, 1.0F));

        PartDefinition chest_grp = body_grp.addOrReplaceChild("chest_grp", CubeListBuilder.create()
                .texOffs(53, 0).addBox(-6.5F, -6.4F, -8.0F, 13.0F, 13.0F, 11.0F, new CubeDeformation(-2.0F))
                .texOffs(39, 0).addBox(-4.5F, -2.2F, -7.6F, 9.0F, 7.0F, 3.0F, new CubeDeformation(-1.0F)),
                PartPose.offset(0.0F, -0.2F, -3.5F));

        PartDefinition lion_neck_grp = chest_grp.addOrReplaceChild("lion_neck_grp", CubeListBuilder.create()
                .texOffs(53, 101).addBox(-4.5F, -1.5F, -5.1F, 9.0F, 7.0F, 5.0F, new CubeDeformation(-1.5F)),
                PartPose.offset(0.0F, -3.6F, -4.7F));
        lion_neck_grp.addOrReplaceChild("mane_back_r1", CubeListBuilder.create()
                .texOffs(86, 25).addBox(-5.5F, -5.5F, -3.5F, 11.0F, 11.0F, 7.0F, new CubeDeformation(-1.5F)),
                PartPose.offsetAndRotation(0.0F, -3.4F, 0.8F, -0.2443F, 0.0F, 0.0F));

        PartDefinition lion_head_grp = lion_neck_grp.addOrReplaceChild("lion_head_grp", CubeListBuilder.create()
                .texOffs(43, 25).addBox(-5.5F, -6.3F, -6.0F, 11.0F, 11.0F, 10.0F, new CubeDeformation(-2.0F))
                .texOffs(0, 37).addBox(-6.0F, -7.5F, -4.9F, 12.0F, 6.0F, 10.0F, new CubeDeformation(-1.51F))
                .texOffs(34, 47).addBox(-3.5F, -8.3F, -4.7F, 7.0F, 5.0F, 11.0F, new CubeDeformation(-1.5F))
                .texOffs(77, 44).addBox(-6.7F, -5.9F, -4.7F, 5.0F, 12.0F, 9.0F, new CubeDeformation(-1.52F))
                .texOffs(50, 64).addBox(-5.2F, -6.8F, -4.7F, 4.0F, 4.0F, 3.0F, new CubeDeformation(-1.0F))
                .texOffs(91, 0).addBox(-2.6F, 0.6F, -7.9F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F))
                .texOffs(77, 44).mirror().addBox(1.7F, -5.9F, -4.7F, 5.0F, 12.0F, 9.0F, new CubeDeformation(-1.52F)).mirror(false)
                .texOffs(50, 64).mirror().addBox(1.2F, -6.8F, -4.7F, 4.0F, 4.0F, 3.0F, new CubeDeformation(-1.0F)).mirror(false)
                .texOffs(91, 0).mirror().addBox(0.6F, 0.6F, -7.9F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false)
                .texOffs(78, 77).addBox(-4.0F, -2.7F, -9.3F, 8.0F, 6.0F, 7.0F, new CubeDeformation(-1.5F)),
                PartPose.offset(0.0F, -3.4F, -1.6F));
        lion_head_grp.addOrReplaceChild("lion_jaw_grp", CubeListBuilder.create()
                .texOffs(24, 96).addBox(-3.5F, -1.7F, -5.3F, 7.0F, 5.0F, 7.0F, new CubeDeformation(-1.51F)),
                PartPose.offsetAndRotation(0.0F, 1.8F, -3.8F, 0.1745F, 0.0F, 0.0F));

        PartDefinition goat_neck_grp = chest_grp.addOrReplaceChild("goat_neck_grp", CubeListBuilder.create()
                .texOffs(58, 66).addBox(-3.0F, -8.1F, -3.5F, 6.0F, 10.0F, 7.0F, new CubeDeformation(-1.51F)),
                PartPose.offsetAndRotation(3.8F, -2.2F, -4.0F, 0.384F, -0.384F, 0.4887F));

        PartDefinition goat_head_grp = goat_neck_grp.addOrReplaceChild("goat_head_grp", CubeListBuilder.create()
                .texOffs(27, 64).addBox(-3.5F, -4.3F, -5.0F, 7.0F, 7.0F, 8.0F, new CubeDeformation(-1.5F))
                .texOffs(101, 98).addBox(-3.0F, -2.9F, -8.8F, 6.0F, 5.0F, 7.0F, new CubeDeformation(-1.5F)),
                PartPose.offsetAndRotation(0.0F, -6.2F, 0.0F, -0.4887F, 0.0F, -0.4189F));
        goat_head_grp.addOrReplaceChild("goat_ear_r_r1", CubeListBuilder.create().texOffs(23, 27).mirror().addBox(-1.1F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(-1.0F)).mirror(false), PartPose.offsetAndRotation(1.9F, -1.7F, -0.2F, 0.0F, -0.2094F, 0.1745F));
        goat_head_grp.addOrReplaceChild("goat_ear_l_r1", CubeListBuilder.create().texOffs(23, 27).addBox(-3.9F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(-1.9F, -1.7F, -0.2F, 0.0F, 0.2094F, -0.1745F));

        PartDefinition goat_jaw_grp = goat_head_grp.addOrReplaceChild("goat_jaw_grp", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-2.5F, -1.0F, -4.8F, 5.0F, 3.0F, 6.0F, new CubeDeformation(-1.01F)),
                PartPose.offsetAndRotation(0.0F, 0.6F, -3.3F, 0.1396F, 0.0F, 0.0F));
        goat_jaw_grp.addOrReplaceChild("goat_beard_r1", CubeListBuilder.create().texOffs(82, 104).addBox(-1.5F, -1.2F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.9F, -1.9F, -0.2443F, 0.0F, 0.0F));

        PartDefinition goat_horn_l_grp = goat_head_grp.addOrReplaceChild("goat_horn_l_grp", CubeListBuilder.create()
                .texOffs(60, 47).addBox(-1.5F, -4.8F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(-1.0F)),
                PartPose.offsetAndRotation(-1.2F, -2.5F, -1.2F, -0.3142F, 0.0F, -0.1745F));
        goat_horn_l_grp.addOrReplaceChild("goat_horn_tip_l_r1", CubeListBuilder.create().texOffs(116, 18).addBox(-1.5F, -3.8F, -1.4F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.02F)), PartPose.offsetAndRotation(0.0F, -5.8F, 2.4F, -1.2915F, 0.0F, 0.0F));
        goat_horn_l_grp.addOrReplaceChild("goat_horn_mid_l_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -4.8F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-1.01F)), PartPose.offsetAndRotation(0.0F, -3.4F, 0.3F, -0.6283F, 0.0F, 0.0F));

        PartDefinition goat_horn_r_grp = goat_head_grp.addOrReplaceChild("goat_horn_r_grp", CubeListBuilder.create()
                .texOffs(60, 47).mirror().addBox(-1.5F, -4.8F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(-1.0F)).mirror(false),
                PartPose.offsetAndRotation(1.2F, -2.5F, -1.2F, -0.3142F, 0.0F, 0.1745F));
        goat_horn_r_grp.addOrReplaceChild("goat_horn_tip_r_r1", CubeListBuilder.create().texOffs(116, 18).mirror().addBox(-1.5F, -3.8F, -1.4F, 3.0F, 5.0F, 3.0F, new CubeDeformation(-1.02F)).mirror(false), PartPose.offsetAndRotation(0.0F, -5.8F, 2.4F, -1.2915F, 0.0F, 0.0F));
        goat_horn_r_grp.addOrReplaceChild("goat_horn_mid_r_r1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.5F, -4.8F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-1.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.4F, 0.3F, -0.6283F, 0.0F, 0.0F));

        PartDefinition leg_fl_grp = chest_grp.addOrReplaceChild("leg_fl_grp", CubeListBuilder.create()
                .texOffs(102, 0).addBox(-3.0F, -1.3F, -3.5F, 6.0F, 10.0F, 7.0F, new CubeDeformation(-1.49F))
                .texOffs(106, 44).addBox(-1.5F, -0.2F, -0.2F, 3.0F, 8.0F, 4.0F, new CubeDeformation(-1.0F)),
                PartPose.offset(-3.8F, 0.4F, -2.9F));
        PartDefinition elbow_fl_grp = leg_fl_grp.addOrReplaceChild("elbow_fl_grp", CubeListBuilder.create()
                .texOffs(29, 80).addBox(-3.0F, -1.7F, -3.1F, 6.0F, 9.0F, 6.0F, new CubeDeformation(-1.5F)),
                PartPose.offset(0.0F, 7.2F, 0.0F));
        elbow_fl_grp.addOrReplaceChild("paw_fl_grp", CubeListBuilder.create()
                .texOffs(79, 91).addBox(-3.5F, -1.5F, -3.9F, 7.0F, 5.0F, 7.0F, new CubeDeformation(-1.5F)),
                PartPose.offset(0.0F, 5.6F, -0.4F));

        PartDefinition leg_fr_grp = chest_grp.addOrReplaceChild("leg_fr_grp", CubeListBuilder.create()
                .texOffs(102, 0).mirror().addBox(-3.0F, -1.3F, -3.5F, 6.0F, 10.0F, 7.0F, new CubeDeformation(-1.49F)).mirror(false)
                .texOffs(106, 44).mirror().addBox(-1.5F, -0.2F, -0.2F, 3.0F, 8.0F, 4.0F, new CubeDeformation(-1.0F)).mirror(false),
                PartPose.offset(3.8F, 0.4F, -2.9F));
        PartDefinition elbow_fr_grp = leg_fr_grp.addOrReplaceChild("elbow_fr_grp", CubeListBuilder.create()
                .texOffs(29, 80).mirror().addBox(-3.0F, -1.7F, -3.1F, 6.0F, 9.0F, 6.0F, new CubeDeformation(-1.5F)).mirror(false),
                PartPose.offset(0.0F, 7.2F, 0.0F));
        elbow_fr_grp.addOrReplaceChild("paw_fr_grp", CubeListBuilder.create()
                .texOffs(79, 91).mirror().addBox(-3.5F, -1.5F, -3.9F, 7.0F, 5.0F, 7.0F, new CubeDeformation(-1.5F)).mirror(false),
                PartPose.offset(0.0F, 5.6F, -0.4F));

        PartDefinition leg_bl_grp = body_grp.addOrReplaceChild("leg_bl_grp", CubeListBuilder.create(), PartPose.offset(-3.6F, 0.2F, 4.0F));
        leg_bl_grp.addOrReplaceChild("hind_thigh_l_r1", CubeListBuilder.create().texOffs(98, 58).addBox(-3.0F, -1.7F, -4.0F, 6.0F, 10.0F, 8.0F, new CubeDeformation(-1.49F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2793F, 0.0F, 0.0F));
        PartDefinition hock_bl_grp = leg_bl_grp.addOrReplaceChild("hock_bl_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 7.2F, 1.2F));
        hock_bl_grp.addOrReplaceChild("hind_shin_l_r1", CubeListBuilder.create().texOffs(54, 85).addBox(-3.0F, -1.7F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(-1.5F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0698F, 0.0F, 0.0F));
        hock_bl_grp.addOrReplaceChild("paw_bl_grp", CubeListBuilder.create().texOffs(79, 91).addBox(-3.5F, -1.5F, -4.0F, 7.0F, 5.0F, 7.0F, new CubeDeformation(-1.5F)), PartPose.offset(0.0F, 5.6F, -0.4F));

        PartDefinition leg_br_grp = body_grp.addOrReplaceChild("leg_br_grp", CubeListBuilder.create(), PartPose.offset(3.6F, 0.2F, 4.0F));
        leg_br_grp.addOrReplaceChild("hind_thigh_r_r1", CubeListBuilder.create().texOffs(98, 58).mirror().addBox(-3.0F, -1.7F, -4.0F, 6.0F, 10.0F, 8.0F, new CubeDeformation(-1.49F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2793F, 0.0F, 0.0F));
        PartDefinition hock_br_grp = leg_br_grp.addOrReplaceChild("hock_br_grp", CubeListBuilder.create(), PartPose.offset(0.0F, 7.2F, 1.2F));
        hock_br_grp.addOrReplaceChild("hind_shin_r_r1", CubeListBuilder.create().texOffs(54, 85).mirror().addBox(-3.0F, -1.7F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(-1.5F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0698F, 0.0F, 0.0F));
        hock_br_grp.addOrReplaceChild("paw_br_grp", CubeListBuilder.create().texOffs(79, 91).mirror().addBox(-3.5F, -1.5F, -4.0F, 7.0F, 5.0F, 7.0F, new CubeDeformation(-1.5F)).mirror(false), PartPose.offset(0.0F, 5.6F, -0.4F));

        PartDefinition tail_1_grp = body_grp.addOrReplaceChild("tail_1_grp", CubeListBuilder.create().texOffs(0, 73).addBox(-3.5F, -3.5F, -1.7F, 7.0F, 7.0F, 7.0F, new CubeDeformation(-1.5F)), PartPose.offsetAndRotation(0.0F, -0.6F, 6.6F, -0.2443F, -0.4538F, 0.0F));
        PartDefinition tail_2_grp = tail_1_grp.addOrReplaceChild("tail_2_grp", CubeListBuilder.create().texOffs(0, 73).addBox(-3.5F, -3.5F, -1.7F, 7.0F, 7.0F, 7.0F, new CubeDeformation(-1.51F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.6F, 0.3142F, -0.2443F, 0.0F));
        PartDefinition tail_3_grp = tail_2_grp.addOrReplaceChild("tail_3_grp", CubeListBuilder.create().texOffs(102, 84).addBox(-3.0F, -3.0F, -1.7F, 6.0F, 6.0F, 7.0F, new CubeDeformation(-1.52F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.6F, 0.6283F, 0.0F, 0.0F));
        PartDefinition tail_4_grp = tail_3_grp.addOrReplaceChild("tail_4_grp", CubeListBuilder.create().texOffs(102, 84).addBox(-3.0F, -3.0F, -1.7F, 6.0F, 6.0F, 7.0F, new CubeDeformation(-1.53F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.6F, 0.6632F, 0.2443F, 0.0F));
        PartDefinition tail_5_grp = tail_4_grp.addOrReplaceChild("tail_5_grp", CubeListBuilder.create().texOffs(102, 84).addBox(-3.0F, -3.0F, -1.7F, 6.0F, 6.0F, 7.0F, new CubeDeformation(-1.54F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.6F, 0.6283F, 0.2443F, 0.0F));
        PartDefinition tail_6_grp = tail_5_grp.addOrReplaceChild("tail_6_grp", CubeListBuilder.create().texOffs(0, 103).addBox(-3.0F, -3.0F, -1.7F, 6.0F, 6.0F, 6.0F, new CubeDeformation(-1.55F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.6F, 0.5934F, 0.1396F, 0.0F));
        PartDefinition tail_7_grp = tail_6_grp.addOrReplaceChild("tail_7_grp", CubeListBuilder.create().texOffs(0, 103).addBox(-3.0F, -3.0F, -1.7F, 6.0F, 6.0F, 6.0F, new CubeDeformation(-1.56F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.8F, 0.5585F, 0.0698F, 0.0F));

        PartDefinition snake_head_grp = tail_7_grp.addOrReplaceChild("snake_head_grp", CubeListBuilder.create()
                .texOffs(0, 55).addBox(-4.0F, -3.5F, -1.9F, 8.0F, 6.0F, 9.0F, new CubeDeformation(-1.5F))
                .texOffs(91, 0).addBox(-2.4F, 0.1F, 3.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F))
                .texOffs(91, 0).mirror().addBox(0.4F, 0.1F, 3.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, 2.8F, 0.0698F, 0.0F, 3.1416F));
        snake_head_grp.addOrReplaceChild("snake_jaw_grp", CubeListBuilder.create()
                .texOffs(0, 88).addBox(-3.5F, -1.5F, -1.7F, 7.0F, 4.0F, 8.0F, new CubeDeformation(-1.51F)),
                PartPose.offsetAndRotation(0.0F, 0.8F, 0.6F, -0.4887F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(BrimstoneChimeraRenderState state) {
        super.setupAnim(state);

        if (state.deathTime > 0.0F || state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        if (state.runAnimationState.isStarted()) {
            this.runAnimation.apply(state.runAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        this.magmaBreathAnimation.apply(state.magmaBreathAnimationState, state.ageInTicks);
        this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);

        float pitch = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
        float yaw = Mth.clamp(state.yRot, -38.0F, 38.0F) * Mth.DEG_TO_RAD;
        this.lion_head_grp.xRot += pitch * 0.55F;
        this.lion_head_grp.yRot += yaw * 0.55F;
        this.goat_head_grp.xRot += pitch * 0.45F;
        this.goat_head_grp.yRot += yaw * 0.45F;
    }
}
