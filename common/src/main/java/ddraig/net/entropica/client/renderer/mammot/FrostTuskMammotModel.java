package ddraig.net.entropica.client.renderer.mammot;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.KeyframeAnimations;
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
 * Fauna 20 — Frost-Tusk Mammot (Glacial Excavator)
 * Blockbench Model Spec: Heavy slate-grey wool torso, wool fringe, tail, 4 wide column legs,
 * low-slung head, brow, 2 ears, chin beard, 2-part trunk snout, and 2 curved 2-segment glacier-blue tusks (19 cubes total).
 * Texture size: 128x128 (textures/entity/mammot/frost_tusk_mammot.png & frost_tusk_mammot_sheared.png).
 */
public class FrostTuskMammotModel extends EntityModel<FrostTuskMammotRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frost_tusk_mammot"), "main"
    );

    public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(3.2F).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.6F, KeyframeAnimations.posVec(0.0F, 0.45F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(3.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9F, KeyframeAnimations.degreeVec(2.5F, -4.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.1F, KeyframeAnimations.degreeVec(1.5F, 4.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(3.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.8F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.2F, KeyframeAnimations.degreeVec(4.0F, 0.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(3.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.3F, KeyframeAnimations.degreeVec(8.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(3.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(3.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();

    public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(1.4F).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, -0.85F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.05F, KeyframeAnimations.posVec(0.0F, -0.85F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(4.0F, -2.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.35F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(4.0F, 2.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.05F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.4F, KeyframeAnimations.degreeVec(4.0F, -2.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.4F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("leg_front_left", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.4F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("leg_back_right", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.4F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("leg_front_right", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.4F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("leg_back_left", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.4F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();

    public static final AnimationDefinition ATTACK = AnimationDefinition.Builder.withLength(0.9F)
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.48F, KeyframeAnimations.degreeVec(-44.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.48F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.48F, KeyframeAnimations.degreeVec(-32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();

    private final ModelPart head;
    private final ModelPart woolFringe;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;

    public FrostTuskMammotModel(ModelPart root) {
        super(root);
        ModelPart mammotRoot = root.getChild("mammot_root");
        ModelPart body = mammotRoot.getChild("body");
        this.woolFringe = body.getChild("wool_fringe");
        this.head = body.getChild("head");

        this.idleAnimation = IDLE.bake(root);
        this.walkAnimation = WALK.bake(root);
        this.attackAnimation = ATTACK.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition mammotRoot = partdefinition.addOrReplaceChild(
                "mammot_root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F)
        );

        // Cube 1: Front woolly shoulder hump (UV 0,0 -> 56x27)
        PartDefinition body = mammotRoot.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -10.0F, -11.0F, 16.0F, 15.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -12.0F, 0.0F)
        );

        // Cube 2: Heavy sloped hindquarters (UV 0,28 -> 52x24)
        PartDefinition bodyRear = body.addOrReplaceChild(
                "body_rear",
                CubeListBuilder.create()
                        .texOffs(0, 28)
                        .addBox(-7.5F, -8.5F, 0.0F, 15.0F, 13.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 1.0F)
        );

        // Cube 3: Shaggy hanging wool skirt covering joints (UV 0,54 -> 78x26)
        body.addOrReplaceChild(
                "wool_fringe",
                CubeListBuilder.create()
                        .texOffs(0, 54)
                        .addBox(-8.5F, -1.0F, 0.0F, 17.0F, 4.0F, 22.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 5.0F, -10.5F)
        );

        // Cube 4: Tufted glacial tail (UV 58,0 -> 10x9)
        bodyRear.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(58, 0)
                        .addBox(-1.5F, 0.0F, -1.0F, 3.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -4.0F, 11.0F, 0.2618F, 0.0F, 0.0F)
        );

        // Cubes 5-8: Four wide column legs (UVs at y=82, each 20x17)
        mammotRoot.addOrReplaceChild(
                "leg_front_left",
                CubeListBuilder.create()
                        .texOffs(0, 82)
                        .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.8F, -12.0F, -7.0F)
        );

        mammotRoot.addOrReplaceChild(
                "leg_front_right",
                CubeListBuilder.create()
                        .texOffs(22, 82)
                        .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.8F, -12.0F, -7.0F)
        );

        mammotRoot.addOrReplaceChild(
                "leg_back_left",
                CubeListBuilder.create()
                        .texOffs(44, 82)
                        .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.6F, -12.0F, 7.0F)
        );

        mammotRoot.addOrReplaceChild(
                "leg_back_right",
                CubeListBuilder.create()
                        .texOffs(66, 82)
                        .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.6F, -12.0F, 7.0F)
        );

        // Cubes 9-10: Low-slung cranium (UV 58,12 -> 36x17) + overhanging woolly brow (UV 58,31 -> 32x8)
        PartDefinition head = body.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(58, 12)
                        .addBox(-5.0F, -5.0F, -8.0F, 10.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(58, 31)
                        .addBox(-5.5F, -6.0F, -8.5F, 11.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -3.0F, -11.0F, 0.0873F, 0.0F, 0.0F)
        );

        // Cubes 11-12: Left & right woolly ears (UV 96,12 and 106,12 -> each 8x6)
        head.addOrReplaceChild(
                "ear_left",
                CubeListBuilder.create()
                        .texOffs(96, 12)
                        .addBox(0.0F, -2.0F, -0.5F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.0F, -3.0F, -3.5F, 0.0F, -0.3491F, 0.1745F)
        );

        head.addOrReplaceChild(
                "ear_right",
                CubeListBuilder.create()
                        .texOffs(106, 12)
                        .addBox(-3.0F, -2.0F, -0.5F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.0F, -3.0F, -3.5F, 0.0F, 0.3491F, -0.1745F)
        );

        // Cubes 13-14: Short flat trunk base (UV 96,20 -> 16x9) + articulated trunk tip (UV 96,31 -> 12x7)
        PartDefinition trunkUpper = head.addOrReplaceChild(
                "trunk_upper",
                CubeListBuilder.create()
                        .texOffs(96, 20)
                        .addBox(-2.5F, 0.0F, -2.0F, 5.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.5F, -8.0F, -0.1745F, 0.0F, 0.0F)
        );

        trunkUpper.addOrReplaceChild(
                "trunk_tip",
                CubeListBuilder.create()
                        .texOffs(96, 31)
                        .addBox(-2.0F, 0.0F, -1.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 5.5F, -0.5F, 0.2182F, 0.0F, 0.0F)
        );

        // Cube 15: Shaggy frost-tipped chin beard (UV 92,40 -> 20x8)
        head.addOrReplaceChild(
                "chin_beard",
                CubeListBuilder.create()
                        .texOffs(92, 40)
                        .addBox(-3.0F, 0.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 4.0F, -5.0F)
        );

        // Cubes 16-17: Left curved glacier-blue tusk (base UV 0,102 -> 22x11; tip UV 24,102 -> 18x9)
        PartDefinition tuskLeftBase = head.addOrReplaceChild(
                "tusk_left_base",
                CubeListBuilder.create()
                        .texOffs(0, 102)
                        .addBox(-1.5F, -1.5F, -8.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.2F, 2.5F, -7.0F, 0.2618F, -0.1745F, 0.0F)
        );

        tuskLeftBase.addOrReplaceChild(
                "tusk_left_tip",
                CubeListBuilder.create()
                        .texOffs(24, 102)
                        .addBox(-1.0F, -1.0F, -7.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -7.5F, -0.6981F, -0.0873F, 0.0F)
        );

        // Cubes 18-19: Right curved glacier-blue tusk (base UV 44,102 -> 22x11; tip UV 68,102 -> 18x9)
        PartDefinition tuskRightBase = head.addOrReplaceChild(
                "tusk_right_base",
                CubeListBuilder.create()
                        .texOffs(44, 102)
                        .addBox(-1.5F, -1.5F, -8.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.2F, 2.5F, -7.0F, 0.2618F, 0.1745F, 0.0F)
        );

        tuskRightBase.addOrReplaceChild(
                "tusk_right_tip",
                CubeListBuilder.create()
                        .texOffs(68, 102)
                        .addBox(-1.0F, -1.0F, -7.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -7.5F, -0.6981F, 0.0873F, 0.0F)
        );

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(FrostTuskMammotRenderState state) {
        super.setupAnim(state);
        this.woolFringe.visible = !state.isSheared;

        if (!state.attackAnimationState.isStarted()) {
            this.head.yRot += state.yRot * Mth.DEG_TO_RAD * 0.35F;
            this.head.xRot += state.xRot * Mth.DEG_TO_RAD * 0.30F;
        }

        this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
    }
}
