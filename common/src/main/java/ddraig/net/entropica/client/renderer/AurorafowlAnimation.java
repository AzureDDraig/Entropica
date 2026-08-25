package ddraig.net.entropica.client.renderer;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class AurorafowlAnimation {

	// ------------------------------------------------------------------------
	// IDLE GROUND: Wings folded flat against the body flanks without Z-fighting
	// ------------------------------------------------------------------------
	public static final AnimationDefinition idle_ground = AnimationDefinition.Builder.withLength(3.0F).looping()
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(-1.5F, 0.0F, 0.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.25F, KeyframeAnimations.degreeVec(1.5F, 0.0F, -0.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.2F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(-2.0F, 10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.25F, KeyframeAnimations.degreeVec(-2.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(2.0F, 15.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.25F, KeyframeAnimations.degreeVec(2.0F, -12.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("crest_seg1", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.85F, KeyframeAnimations.degreeVec(4.0F, -5.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.35F, KeyframeAnimations.degreeVec(4.0F, 4.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("crest_seg3", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.95F, KeyframeAnimations.degreeVec(6.0F, -8.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.7F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.45F, KeyframeAnimations.degreeVec(6.0F, 6.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Left Upper Wing (Folded Flat against Flank)
		.addAnimation("wing_left_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, 82.0F, -18.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.5F, KeyframeAnimations.degreeVec(-4.5F, 83.0F, -18.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(-5.0F, 82.0F, -18.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -165.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.5F, KeyframeAnimations.degreeVec(0.0F, -163.0F, 4.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, -165.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Left Lower Wing (Folded neatly below upper wing)
		.addAnimation("wing_left_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 78.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.5F, KeyframeAnimations.degreeVec(-7.5F, 79.0F, -15.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(-8.0F, 78.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ll_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -160.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, -160.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ll_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Right Upper Wing (Folded Flat against Flank)
		.addAnimation("wing_right_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, -82.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.5F, KeyframeAnimations.degreeVec(-4.5F, -83.0F, 18.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(-5.0F, -82.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 165.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.5F, KeyframeAnimations.degreeVec(0.0F, 163.0F, -4.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 165.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Right Lower Wing (Folded neatly below upper wing)
		.addAnimation("wing_right_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, -78.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.5F, KeyframeAnimations.degreeVec(-7.5F, -79.0F, 15.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(-8.0F, -78.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_rl_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 160.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 160.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_rl_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("tail_base", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(-2.0F, -4.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.25F, KeyframeAnimations.degreeVec(2.0F, 4.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();

	// ------------------------------------------------------------------------
	// WALK: Ground stride with wings securely folded flat
	// ------------------------------------------------------------------------
	public static final AnimationDefinition walk = AnimationDefinition.Builder.withLength(1.4F).looping()
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(-2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -1.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.05F, KeyframeAnimations.degreeVec(-2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.5F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, 0.4F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.05F, KeyframeAnimations.posVec(0.0F, 0.4F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(-28.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.05F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("shin_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.05F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("foot_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(13.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.05F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.05F, KeyframeAnimations.degreeVec(-28.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("shin_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.05F, KeyframeAnimations.degreeVec(35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("foot_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(13.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.05F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(13.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.95F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(-3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.95F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(-3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Left Upper Wing (Folded Flat)
		.addAnimation("wing_left_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, 82.0F, -18.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(-4.0F, 81.0F, -17.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(-6.0F, 83.0F, -19.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.05F, KeyframeAnimations.degreeVec(-4.0F, 81.0F, -17.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(-5.0F, 82.0F, -18.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -165.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, -165.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Left Lower Wing (Folded Flat)
		.addAnimation("wing_left_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 78.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(-8.0F, 78.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ll_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -160.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, -160.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ll_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Right Upper Wing (Folded Flat)
		.addAnimation("wing_right_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, -82.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(-6.0F, -83.0F, 19.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(-4.0F, -81.0F, 17.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.05F, KeyframeAnimations.degreeVec(-6.0F, -83.0F, 19.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(-5.0F, -82.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 165.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, 165.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Right Lower Wing (Folded Flat)
		.addAnimation("wing_right_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, -78.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(-8.0F, -78.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_rl_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 160.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, 160.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_rl_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("tail_base", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, -4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();

	// ------------------------------------------------------------------------
	// IDLE FLIGHT: Thermal gliding with smooth sinusoidal wing & tip flex
	// ------------------------------------------------------------------------
	public static final AnimationDefinition idle_flight = AnimationDefinition.Builder.withLength(2.4F).looping()
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(-18.5F, 0.0F, 0.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.8F, KeyframeAnimations.degreeVec(-21.5F, 0.0F, -0.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, 0.35F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.8F, KeyframeAnimations.posVec(0.0F, -0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("crest_seg1", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(13.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Left Upper Wing (Sinusoidal Soaring Motion)
		.addAnimation("wing_left_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, -12.0F, -24.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(6.0F, -10.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(8.0F, -12.0F, -24.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.8F, KeyframeAnimations.degreeVec(10.0F, -14.0F, -20.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(8.0F, -12.0F, -24.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Right Upper Wing (Sinusoidal Soaring Motion)
		.addAnimation("wing_right_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 12.0F, 24.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(6.0F, 10.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(8.0F, 12.0F, 24.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.8F, KeyframeAnimations.degreeVec(10.0F, 14.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(8.0F, 12.0F, 24.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Lower Wings Sinusoidal Trailing
		.addAnimation("wing_left_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, -15.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(-4.0F, -15.0F, -18.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-4.0F, -15.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.8F, KeyframeAnimations.degreeVec(-4.0F, -15.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(-4.0F, -15.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_right_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 15.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(-4.0F, 15.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-4.0F, 15.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.8F, KeyframeAnimations.degreeVec(-4.0F, 15.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(-4.0F, 15.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("shin_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-128.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("foot_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(88.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("shin_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-128.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("foot_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(88.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("tail_base", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.4F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();

	// ------------------------------------------------------------------------
	// FLYING: Active powerful flapping for gaining height / climbing
	// ------------------------------------------------------------------------
	public static final AnimationDefinition flying = AnimationDefinition.Builder.withLength(0.8F).looping()
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(-28.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -0.6F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, 1.2F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Left Upper Wing (Flapping Downstroke & Upstroke)
		.addAnimation("wing_left_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, -10.0F, -20.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(-20.0F, -14.0F, -55.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(24.0F, -6.0F, 30.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(10.0F, -10.0F, -20.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 30.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -20.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Right Upper Wing (Flapping Downstroke & Upstroke)
		.addAnimation("wing_right_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 10.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(-20.0F, 14.0F, 55.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(24.0F, 6.0F, -30.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(10.0F, 10.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -30.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -20.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Lower Wings Flapping
		.addAnimation("wing_left_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(-24.0F, -18.0F, -50.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(18.0F, -10.0F, 25.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_right_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(-24.0F, 18.0F, 50.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(18.0F, 10.0F, -25.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(63.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("shin_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(-133.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("foot_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(92.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(63.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("shin_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(-133.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("foot_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(92.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();

	// ------------------------------------------------------------------------
	// TAKING OFF: Transition from folded wings -> unfolding -> launch glide
	// ------------------------------------------------------------------------
	public static final AnimationDefinition taking_off = AnimationDefinition.Builder.withLength(1.2F)
		.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, -2.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, 4.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, 7.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("shin_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("foot_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("shin_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("foot_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Left Wing Unfolding
		.addAnimation("wing_left_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, 82.0F, -18.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 35.0F, -35.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(25.0F, -8.0F, 30.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(8.0F, -12.0F, -24.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -165.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, -70.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_left_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 78.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 30.0F, -25.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(20.0F, -5.0F, 25.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-4.0F, -15.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ll_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -160.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, -60.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ll_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Right Wing Unfolding
		.addAnimation("wing_right_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, -82.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, -35.0F, 35.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(25.0F, 8.0F, -30.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(8.0F, 12.0F, 24.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 165.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 70.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, -5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_right_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, -78.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, -30.0F, 25.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(20.0F, 5.0F, -25.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-4.0F, 15.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_rl_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 160.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 60.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_rl_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, -5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();

	// ------------------------------------------------------------------------
	// LANDING: Air-brake aerodynamic flare -> folding wings flush on touchdown
	// ------------------------------------------------------------------------
	public static final AnimationDefinition landing = AnimationDefinition.Builder.withLength(1.2F)
		.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 5.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.45F, KeyframeAnimations.posVec(0.0F, 0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.65F, KeyframeAnimations.posVec(0.0F, -1.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.65F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Left Wing Folding upon touchdown
		.addAnimation("wing_left_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, -12.0F, -24.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(30.0F, -20.0F, -45.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(5.0F, 45.0F, -25.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-5.0F, 82.0F, -18.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, -90.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, -165.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0F, -5.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_left_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, -15.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(25.0F, -18.0F, -35.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 40.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-8.0F, 78.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ll_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0F, -10.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, -85.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, -160.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ll_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0F, -5.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		// Right Wing Folding upon touchdown
		.addAnimation("wing_right_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 12.0F, 24.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(30.0F, 20.0F, 45.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(5.0F, -45.0F, 25.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-5.0F, -82.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 90.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 165.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0F, 5.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_right_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 15.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(25.0F, 18.0F, 35.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, -40.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(-8.0F, -78.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_rl_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 85.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 160.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_rl_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0F, 5.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.65F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("shin_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.65F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("foot_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.65F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.65F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("shin_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.65F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("foot_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.65F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("tail_base", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();

	// ------------------------------------------------------------------------
	// ATTACK GROUND
	// ------------------------------------------------------------------------
	public static final AnimationDefinition attack_ground = AnimationDefinition.Builder.withLength(0.9F)
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.22F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.32F, KeyframeAnimations.degreeVec(35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.22F, KeyframeAnimations.posVec(0.0F, 0.5F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.32F, KeyframeAnimations.posVec(0.0F, -2.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -1.8F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.22F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.32F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.22F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.32F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("crest_seg1", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.22F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.35F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_left_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, 82.0F, -18.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.22F, KeyframeAnimations.degreeVec(5.0F, 60.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.32F, KeyframeAnimations.degreeVec(15.0F, 65.0F, -20.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(-5.0F, 82.0F, -18.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -165.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.22F, KeyframeAnimations.degreeVec(0.0F, -120.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.32F, KeyframeAnimations.degreeVec(0.0F, -140.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, -165.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_lu_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_left_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 78.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(-8.0F, 78.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ll_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -160.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, -160.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ll_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_right_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, -82.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.22F, KeyframeAnimations.degreeVec(5.0F, -60.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.32F, KeyframeAnimations.degreeVec(15.0F, -65.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(-5.0F, -82.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 165.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.22F, KeyframeAnimations.degreeVec(0.0F, 120.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.32F, KeyframeAnimations.degreeVec(0.0F, 140.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 165.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_ru_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_right_lower", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, -78.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(-8.0F, -78.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_rl_mid", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 160.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 160.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_rl_tip", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, -15.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();

	// ------------------------------------------------------------------------
	// ATTACK FLYING
	// ------------------------------------------------------------------------
	public static final AnimationDefinition attack_flying = AnimationDefinition.Builder.withLength(1.0F)
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, -3.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, -2.5F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_left_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, -12.0F, -24.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.28F, KeyframeAnimations.degreeVec(-35.0F, -20.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(25.0F, -10.0F, -45.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(8.0F, -12.0F, -24.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("wing_right_upper", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 12.0F, 24.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.28F, KeyframeAnimations.degreeVec(-35.0F, 20.0F, -20.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(25.0F, 10.0F, 45.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(8.0F, 12.0F, 24.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("shin_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("foot_left", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("leg_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("shin_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(-130.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("foot_right", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.55F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();
}
