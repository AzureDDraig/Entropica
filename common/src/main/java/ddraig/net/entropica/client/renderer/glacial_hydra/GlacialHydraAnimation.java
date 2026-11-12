package ddraig.net.entropica.client.renderer.glacial_hydra;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class GlacialHydraAnimation {
    public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(3F).looping()
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
            new Keyframe(0.0F, KeyframeAnimations.posVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.5F, KeyframeAnimations.posVec(0F, 0.2F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0F, KeyframeAnimations.posVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.5F, KeyframeAnimations.degreeVec(-3F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.7917F, KeyframeAnimations.degreeVec(1F, -4F, 2F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0F, KeyframeAnimations.degreeVec(1F, 4F, -2F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.4167F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("throat_fluff", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.5F, KeyframeAnimations.degreeVec(4F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("throat_fluff_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.5F, KeyframeAnimations.degreeVec(3F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .build();

    public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(1.2F).looping()
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
            new Keyframe(0.0F, KeyframeAnimations.posVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.posVec(0F, 0.15F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.posVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.posVec(0F, 0.15F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.posVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-2F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-2F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(2F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(2F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("throat_fluff", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-5F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(5F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("throat_fluff_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-4F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(4F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_front_left", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-20F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-10F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(20F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_front_left_calf", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-30F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-5F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_front_left_foot", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(15F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_front_right", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(10F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(20F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-20F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(10F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_front_right_calf", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-30F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_front_right_foot", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(15F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_back_left", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(10F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(20F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-15F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(10F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_back_left_calf", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-25F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_back_left_foot", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(10F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_back_right", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-15F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-10F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(15F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_back_right_calf", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-25F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-5F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_back_right_foot", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(10F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .build();

    public static final AnimationDefinition RUN = AnimationDefinition.Builder.withLength(0.6F).looping()
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(0F, 0F, -2F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.4583F, KeyframeAnimations.degreeVec(0F, 0F, 2F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
            new Keyframe(0.0F, KeyframeAnimations.posVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1667F, KeyframeAnimations.posVec(0F, 0.4F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.posVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.4583F, KeyframeAnimations.posVec(0F, 0.4F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.posVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(3F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-3F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-3F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.4583F, KeyframeAnimations.degreeVec(3F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("throat_fluff", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-10F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.4583F, KeyframeAnimations.degreeVec(10F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("throat_fluff_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-8F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.4583F, KeyframeAnimations.degreeVec(7F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_front_left", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_front_left_calf", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-40F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-40F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_front_left_foot", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(20F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(20F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_front_right", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_front_right_calf", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-40F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_front_right_foot", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(20F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_back_left", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_back_left_calf", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-40F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_back_left_foot", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(20F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_back_right", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_back_right_calf", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(-40F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-40F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("leg_back_right_foot", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(20F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5833F, KeyframeAnimations.degreeVec(20F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .build();

    public static final AnimationDefinition ATTACK = AnimationDefinition.Builder.withLength(1.2F).looping()
        .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-25F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(45F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.7083F, KeyframeAnimations.degreeVec(45F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
            new Keyframe(0.0F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-20F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.7083F, KeyframeAnimations.degreeVec(35F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0F, 0F, 0F), AnimationChannel.Interpolations.CATMULLROM)
        ))
        .build();

}
