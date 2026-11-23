package ddraig.net.entropica.client.renderer.sanguine_sea_angel;

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
 * 32x32 Translucent Model for Sanguine Sea-Angel (entropica:sanguine_sea_angel).
 * Separates the outer mantle cube onto a PartPose.ZERO child ("torso_shell") added after "heart_core"
 * so the glowing arterial heart core renders cleanly inside the translucent body shell without depth occlusion.
 */
public class SanguineSeaAngelModel extends EntityModel<SanguineSeaAngelRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sanguine_sea_angel"), "main"
    );
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart heart_core;
    private final ModelPart tail;
    private final ModelPart tail_veil;
    private final ModelPart wing_left;
    private final ModelPart wing_left_tip;
    private final ModelPart wing_right;
    private final ModelPart wing_right_tip;
    private final ModelPart head;
    private final ModelPart cowl_left;
    private final ModelPart cowl_right;
    private final ModelPart buccal_crown;
    private final ModelPart buccal_1;
    private final ModelPart buccal_2;
    private final ModelPart buccal_3;
    private final ModelPart buccal_4;
    private final ModelPart buccal_5;
    private final ModelPart buccal_6;
    private final ModelPart torso_shell;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation swimAnimation;
    private final KeyframeAnimation iceHoverAnimation;
    private final KeyframeAnimation iceGlideAnimation;
    private final KeyframeAnimation spitDartAnimation;
    private final KeyframeAnimation lungeAttackAnimation;
    private final KeyframeAnimation damagedAnimation;
    private final KeyframeAnimation deathAnimation;

    public SanguineSeaAngelModel(ModelPart modelRoot) {
        super(modelRoot);
        this.root = modelRoot.getChild("root");
        this.body = this.root.getChild("body");
        this.heart_core = this.body.getChild("heart_core");
        this.tail = this.body.getChild("tail");
        this.tail_veil = this.tail.getChild("tail_veil");
        this.wing_left = this.body.getChild("wing_left");
        this.wing_left_tip = this.wing_left.getChild("wing_left_tip");
        this.wing_right = this.body.getChild("wing_right");
        this.wing_right_tip = this.wing_right.getChild("wing_right_tip");
        this.head = this.body.getChild("head");
        this.cowl_left = this.head.getChild("cowl_left");
        this.cowl_right = this.head.getChild("cowl_right");
        this.buccal_crown = this.head.getChild("buccal_crown");
        this.buccal_1 = this.buccal_crown.getChild("buccal_1");
        this.buccal_2 = this.buccal_crown.getChild("buccal_2");
        this.buccal_3 = this.buccal_crown.getChild("buccal_3");
        this.buccal_4 = this.buccal_crown.getChild("buccal_4");
        this.buccal_5 = this.buccal_crown.getChild("buccal_5");
        this.buccal_6 = this.buccal_crown.getChild("buccal_6");
        this.torso_shell = this.body.getChild("torso_shell");

        this.idleAnimation = SanguineSeaAngelAnimation.IDLE.bake(modelRoot);
        this.swimAnimation = SanguineSeaAngelAnimation.SWIM.bake(modelRoot);
        this.iceHoverAnimation = SanguineSeaAngelAnimation.ICE_HOVER.bake(modelRoot);
        this.iceGlideAnimation = SanguineSeaAngelAnimation.ICE_GLIDE.bake(modelRoot);
        this.spitDartAnimation = SanguineSeaAngelAnimation.SPIT_DART.bake(modelRoot);
        this.lungeAttackAnimation = SanguineSeaAngelAnimation.LUNGE_ATTACK.bake(modelRoot);
        this.damagedAnimation = SanguineSeaAngelAnimation.DAMAGED.bake(modelRoot);
        this.deathAnimation = SanguineSeaAngelAnimation.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild(
                "root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 12.0F, 0.0F)
        );

        // Empty cube list on "body" so inner "heart_core" renders BEFORE outer translucent "torso_shell"
        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -2.5F, 0.0F)
        );

        body.addOrReplaceChild(
                "heart_core",
                CubeListBuilder.create()
                        .texOffs(22, 0).addBox(-1.5F, -2.5F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        PartDefinition tail = body.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(16, 14).addBox(-2.0F, 0.0F, -1.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 4.5F, 0.0F)
        );

        tail.addOrReplaceChild(
                "tail_veil",
                CubeListBuilder.create()
                        .texOffs(22, 7).addBox(-4.0F, -1.0F, 0.0F, 4.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                        .texOffs(22, 7).mirror().addBox(0.0F, -1.0F, 0.0F, 4.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(0.0F, 3.0F, 0.0F)
        );

        PartDefinition wing_left = body.addOrReplaceChild(
                "wing_left",
                CubeListBuilder.create()
                        .texOffs(0, 23).addBox(-5.0F, -3.5F, 0.0F, 5.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, -0.5F, 0.0F)
        );

        wing_left.addOrReplaceChild(
                "wing_left_tip",
                CubeListBuilder.create()
                        .texOffs(10, 23).addBox(-6.0F, -3.5F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 0.0F, 0.0F)
        );

        PartDefinition wing_right = body.addOrReplaceChild(
                "wing_right",
                CubeListBuilder.create()
                        .texOffs(0, 23).mirror().addBox(0.0F, -3.5F, 0.0F, 5.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(3.0F, -0.5F, 0.0F)
        );

        wing_right.addOrReplaceChild(
                "wing_right_tip",
                CubeListBuilder.create()
                        .texOffs(10, 23).mirror().addBox(0.0F, -3.5F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(5.0F, 0.0F, 0.0F)
        );

        PartDefinition head = body.addOrReplaceChild(
                "head",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -4.5F, 0.0F)
        );

        head.addOrReplaceChild(
                "cowl_left",
                CubeListBuilder.create()
                        .texOffs(0, 14).addBox(0.0F, -4.0F, -2.5F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 23).addBox(0.5F, -8.0F, 0.0F, 2.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 0.0F, 0.0F)
        );

        head.addOrReplaceChild(
                "cowl_right",
                CubeListBuilder.create()
                        .texOffs(0, 14).mirror().addBox(-3.0F, -4.0F, -2.5F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                        .texOffs(28, 23).mirror().addBox(-2.5F, -8.0F, 0.0F, 2.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(3.0F, 0.0F, 0.0F)
        );

        PartDefinition buccal_crown = head.addOrReplaceChild(
                "buccal_crown",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        buccal_crown.addOrReplaceChild(
                "buccal_1",
                CubeListBuilder.create()
                        .texOffs(22, 23).addBox(-1.5F, -6.0F, -1.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 5.8F, 0.0F, 0.0F, -0.5236F, 0.0F)
        );

        buccal_crown.addOrReplaceChild(
                "buccal_2",
                CubeListBuilder.create()
                        .texOffs(22, 23).addBox(-1.5F, -6.0F, -1.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 5.8F, 0.0F, 0.0F, -1.5708F, 0.0F)
        );

        buccal_crown.addOrReplaceChild(
                "buccal_3",
                CubeListBuilder.create()
                        .texOffs(22, 23).addBox(-1.5F, -6.0F, -1.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 5.8F, 0.0F, 0.0F, -2.618F, 0.0F)
        );

        buccal_crown.addOrReplaceChild(
                "buccal_4",
                CubeListBuilder.create()
                        .texOffs(22, 23).addBox(-1.5F, -6.0F, -1.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 5.8F, 0.0F, 0.0F, -3.6652F, 0.0F)
        );

        buccal_crown.addOrReplaceChild(
                "buccal_5",
                CubeListBuilder.create()
                        .texOffs(22, 23).addBox(-1.5F, -6.0F, -1.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 5.8F, 0.0F, 0.0F, -4.7124F, 0.0F)
        );

        buccal_crown.addOrReplaceChild(
                "buccal_6",
                CubeListBuilder.create()
                        .texOffs(22, 23).addBox(-1.5F, -6.0F, -1.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 5.8F, 0.0F, 0.0F, -5.7596F, 0.0F)
        );

        // Added LAST on "body" so "heart_core" renders before translucent "torso_shell"
        body.addOrReplaceChild(
                "torso_shell",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -4.5F, -2.5F, 6.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO
        );

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(SanguineSeaAngelRenderState state) {
        super.setupAnim(state);

        // Keep buccal tentacles tucked at 0.85x inside closed cowl; when spit_dart / lunge_attack / death
        // runs, their KeyframeAnimations.scaleVec(0.85F -> 1.05F/1.12F/0.50F) adds relative to 1.0F cleanly.
        boolean buccalAnimated = state.deathAnimationState.isStarted()
                || state.spitDartAnimationState.isStarted()
                || state.lungeAttackAnimationState.isStarted();
        if (!buccalAnimated) {
            this.buccal_crown.xScale = 0.85F;
            this.buccal_crown.yScale = 0.85F;
            this.buccal_crown.zScale = 0.85F;
        }

        if (state.deathAnimationState.isStarted() || state.deathTime > 0.0F) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        if (!state.inWater) {
            if (state.iceGlideAnimationState.isStarted()) {
                this.iceGlideAnimation.apply(state.iceGlideAnimationState, state.ageInTicks);
            } else {
                this.iceHoverAnimation.apply(state.iceHoverAnimationState, state.ageInTicks);
            }
        } else {
            if (state.swimAnimationState.isStarted()) {
                this.swimAnimation.apply(state.swimAnimationState, state.ageInTicks);
            } else {
                this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
            }
        }

        this.spitDartAnimation.apply(state.spitDartAnimationState, state.ageInTicks);
        this.lungeAttackAnimation.apply(state.lungeAttackAnimationState, state.ageInTicks);
        this.damagedAnimation.apply(state.damagedAnimationState, state.ageInTicks);

        if (!state.spitDartAnimationState.isStarted() && !state.lungeAttackAnimationState.isStarted()) {
            float clampedYaw = Mth.clamp(state.yRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
            float clampedPitch = Mth.clamp(state.xRot, -20.0F, 20.0F) * Mth.DEG_TO_RAD;
            this.head.yRot += clampedYaw * 0.45F;
            this.head.xRot += clampedPitch * 0.35F;
        }
    }
}
