package ddraig.net.entropica.client.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Exact Blockbench keyframe animations for Void Shadow-Gargoyle (Fauna 51 — Rift Sentinel).
 * Split into per-animation static builders and <=60-keyframe helpers so constant pools and method bytecodes stay tiny.
 */
public final class VoidShadowGargoyleAnimations {

	private VoidShadowGargoyleAnimations() {}

	static Keyframe rotL(float t, float x, float y, float z) {
		return new Keyframe(t, KeyframeAnimations.degreeVec(x, y, z), AnimationChannel.Interpolations.LINEAR);
	}
	static Keyframe rotC(float t, float x, float y, float z) {
		return new Keyframe(t, KeyframeAnimations.degreeVec(x, y, z), AnimationChannel.Interpolations.CATMULLROM);
	}
	static Keyframe posL(float t, float x, float y, float z) {
		return new Keyframe(t, KeyframeAnimations.posVec(x, y, z), AnimationChannel.Interpolations.LINEAR);
	}
	static Keyframe posC(float t, float x, float y, float z) {
		return new Keyframe(t, KeyframeAnimations.posVec(x, y, z), AnimationChannel.Interpolations.CATMULLROM);
	}
	static Keyframe sclL(float t, float x, float y, float z) {
		return new Keyframe(t, KeyframeAnimations.scaleVec(x, y, z), AnimationChannel.Interpolations.LINEAR);
	}
	static Keyframe sclC(float t, float x, float y, float z) {
		return new Keyframe(t, KeyframeAnimations.scaleVec(x, y, z), AnimationChannel.Interpolations.CATMULLROM);
	}

	public static final AnimationDefinition IDLE = IdleAnimBuilder.create();
	public static final AnimationDefinition idle = IDLE;
	public static final AnimationDefinition WALK = WalkAnimBuilder.create();
	public static final AnimationDefinition walk = WALK;
	public static final AnimationDefinition FLY = FlyAnimBuilder.create();
	public static final AnimationDefinition fly = FLY;
	public static final AnimationDefinition ATTACK = AttackAnimBuilder.create();
	public static final AnimationDefinition attack = ATTACK;
	public static final AnimationDefinition BLINK_OUT = BlinkOutAnimBuilder.create();
	public static final AnimationDefinition blink_out = BLINK_OUT;
	public static final AnimationDefinition BLINK_IN = BlinkInAnimBuilder.create();
	public static final AnimationDefinition blink_in = BLINK_IN;
	public static final AnimationDefinition HURT = HurtAnimBuilder.create();
	public static final AnimationDefinition hurt = HURT;
	public static final AnimationDefinition DEATH = DeathAnimBuilder.create();
	public static final AnimationDefinition death = DEATH;

	private static final class IdleAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, -2.2F, 0.0F, 0.0F),
				rotL(2.0F, 1.5F, 0.0F, 0.0F),
				rotL(3.0F, -2.4F, 0.0F, 0.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 2.0F, 0.0F, 0.0F),
				rotL(1.08F, -3.0F, 18.0F, 5.0F),
				rotL(1.75F, -2.0F, 15.0F, 4.0F),
				rotL(2.05F, 1.0F, 0.0F, 0.0F),
				rotL(2.48F, 1.0F, 0.0F, 0.0F),
				rotL(2.65F, -4.0F, -17.0F, -5.0F),
				rotL(3.35F, -2.0F, -14.0F, -4.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 6.0F, 0.0F, 0.0F),
				rotL(1.85F, 0.0F, 0.0F, 0.0F),
				rotL(2.75F, 8.0F, 0.0F, 0.0F),
				rotL(3.45F, 0.0F, 0.0F, 0.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, -22.0F, -8.0F),
				rotL(2.55F, 0.0F, -24.0F, -10.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.45F, 0.0F, -26.0F, -12.0F),
				rotL(2.65F, 0.0F, -28.0F, -14.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.55F, 0.0F, -18.0F, -6.0F),
				rotL(2.75F, 0.0F, -20.0F, -8.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, -4.0F, 0.0F, 3.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 22.0F, 8.0F),
				rotL(2.55F, 0.0F, 24.0F, 10.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.45F, 0.0F, 26.0F, 12.0F),
				rotL(2.65F, 0.0F, 28.0F, 14.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.55F, 0.0F, 18.0F, 6.0F),
				rotL(2.75F, 0.0F, 20.0F, 8.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, -4.0F, 0.0F, -3.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 1.5F, -4.8F, 0.0F),
				rotL(2.0F, -0.5F, 0.0F, 0.0F),
				rotL(3.0F, 1.5F, 4.8F, 0.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 1.5F, -5.6F, 0.0F),
				rotL(2.0F, -0.5F, 0.0F, 0.0F),
				rotL(3.0F, 1.5F, 5.6F, 0.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 1.5F, -6.4F, 0.0F),
				rotL(2.0F, -0.5F, 0.0F, 0.0F),
				rotL(3.0F, 1.5F, 6.4F, 0.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 1.5F, -7.2F, 0.0F),
				rotL(2.0F, -0.5F, 0.0F, 0.0F),
				rotL(3.0F, 1.5F, 7.2F, 0.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 1.5F, -8.0F, 0.0F),
				rotL(2.0F, -0.5F, 0.0F, 0.0F),
				rotL(3.0F, 1.5F, 8.0F, 0.0F),
				rotL(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(4.0F).looping();
			ch_0(b);
			ch_1(b);
			ch_2(b);
			ch_3(b);
			ch_4(b);
			ch_5(b);
			ch_6(b);
			ch_7(b);
			ch_8(b);
			ch_9(b);
			ch_10(b);
			ch_11(b);
			ch_12(b);
			ch_13(b);
			ch_14(b);
			ch_15(b);
			return b.build();
		}
	}

	private static final class WalkAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.28F, 0.0F, -0.82F, 0.6F),
				posL(0.68F, 0.0F, 4.8F, -1.8F),
				posL(1.12F, 0.0F, -0.62F, 0.4F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 10.0F, 0.0F, 0.0F),
				rotL(0.68F, -12.0F, 0.0F, 0.0F),
				rotL(1.12F, 8.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, -10.0F, 0.0F, 0.0F),
				rotL(0.68F, 8.0F, 0.0F, 0.0F),
				rotL(1.12F, -8.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, -14.0F, 0.0F, 0.0F),
				rotL(0.68F, -24.0F, 0.0F, 0.0F),
				rotL(1.12F, -10.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 24.0F, 0.0F, 0.0F),
				rotL(0.68F, 18.0F, 0.0F, 0.0F),
				rotL(1.12F, 18.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, -10.0F, 0.0F, 0.0F),
				rotL(0.68F, 28.0F, 0.0F, 0.0F),
				rotL(1.12F, -8.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 18.0F, 0.0F, 5.0F),
				rotL(0.68F, -26.0F, 0.0F, 8.0F),
				rotL(1.12F, 8.0F, 0.0F, 4.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 0.0F, 10.0F, 18.0F),
				rotL(0.68F, 0.0F, -14.0F, -20.0F),
				rotL(1.12F, 0.0F, 6.0F, 10.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 0.0F, 0.0F, 12.0F),
				rotL(0.68F, 0.0F, -10.0F, -18.0F),
				rotL(1.12F, 0.0F, 0.0F, 8.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, -14.0F, 0.0F, 0.0F),
				rotL(0.72F, -24.0F, 0.0F, 0.0F),
				rotL(1.12F, -10.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 24.0F, 0.0F, 0.0F),
				rotL(0.72F, 18.0F, 0.0F, 0.0F),
				rotL(1.12F, 18.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, -10.0F, 0.0F, 0.0F),
				rotL(0.72F, 28.0F, 0.0F, 0.0F),
				rotL(1.12F, -8.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 18.0F, 0.0F, -5.0F),
				rotL(0.68F, -26.0F, 0.0F, -8.0F),
				rotL(1.12F, 8.0F, 0.0F, -4.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 0.0F, -10.0F, -18.0F),
				rotL(0.68F, 0.0F, 14.0F, 20.0F),
				rotL(1.12F, 0.0F, -6.0F, -10.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 0.0F, 0.0F, -12.0F),
				rotL(0.68F, 0.0F, 10.0F, 18.0F),
				rotL(1.12F, 0.0F, 0.0F, -8.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 5.0F, 0.0F, 0.0F),
				rotL(0.68F, -3.0F, 0.0F, 0.0F),
				rotL(1.12F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 5.0F, 0.0F, 0.0F),
				rotL(0.68F, -3.0F, 0.0F, 0.0F),
				rotL(1.12F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 5.0F, 0.0F, 0.0F),
				rotL(0.68F, -3.0F, 0.0F, 0.0F),
				rotL(1.12F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 5.0F, 0.0F, 0.0F),
				rotL(0.68F, -3.0F, 0.0F, 0.0F),
				rotL(1.12F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 5.0F, 0.0F, 0.0F),
				rotL(0.68F, -3.0F, 0.0F, 0.0F),
				rotL(1.12F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.6F).looping();
			ch_0(b);
			ch_1(b);
			ch_2(b);
			ch_3(b);
			ch_4(b);
			ch_5(b);
			ch_6(b);
			ch_7(b);
			ch_8(b);
			ch_9(b);
			ch_10(b);
			ch_11(b);
			ch_12(b);
			ch_13(b);
			ch_14(b);
			ch_15(b);
			ch_16(b);
			ch_17(b);
			ch_18(b);
			ch_19(b);
			return b.build();
		}
	}

	private static final class FlyAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 8.0F, 0.0F),
				posL(0.1F, 0.0F, 7.1F, 0.0F),
				posL(0.2F, 0.0F, 6.441F, 0.0F),
				posL(0.3F, 0.0F, 6.2F, 0.0F),
				posL(0.4F, 0.0F, 6.441F, 0.0F),
				posL(0.5F, 0.0F, 7.1F, 0.0F),
				posL(0.6F, 0.0F, 8.0F, 0.0F),
				posL(0.7F, 0.0F, 8.9F, 0.0F),
				posL(0.8F, 0.0F, 9.559F, 0.0F),
				posL(0.9F, 0.0F, 9.8F, 0.0F),
				posL(1.0F, 0.0F, 9.559F, 0.0F),
				posL(1.1F, 0.0F, 8.9F, 0.0F),
				posL(1.2F, 0.0F, 8.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 12.5F, 0.0F, 0.0F),
				rotL(0.1F, 12.969F, 0.0F, 0.0F),
				rotL(0.2F, 14.25F, 0.0F, 0.0F),
				rotL(0.3F, 16.0F, 0.0F, 0.0F),
				rotL(0.4F, 17.75F, 0.0F, 0.0F),
				rotL(0.5F, 19.031F, 0.0F, 0.0F),
				rotL(0.6F, 19.5F, 0.0F, 0.0F),
				rotL(0.7F, 19.031F, 0.0F, 0.0F),
				rotL(0.8F, 17.75F, 0.0F, 0.0F),
				rotL(0.9F, 16.0F, 0.0F, 0.0F),
				rotL(1.0F, 14.25F, 0.0F, 0.0F),
				rotL(1.1F, 12.969F, 0.0F, 0.0F),
				rotL(1.2F, 12.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -13.0F, 0.0F, 0.0F),
				rotL(0.1F, -13.402F, 0.0F, 0.0F),
				rotL(0.2F, -14.5F, 0.0F, 0.0F),
				rotL(0.3F, -16.0F, 0.0F, 0.0F),
				rotL(0.4F, -17.5F, 0.0F, 0.0F),
				rotL(0.5F, -18.598F, 0.0F, 0.0F),
				rotL(0.6F, -19.0F, 0.0F, 0.0F),
				rotL(0.7F, -18.598F, 0.0F, 0.0F),
				rotL(0.8F, -17.5F, 0.0F, 0.0F),
				rotL(0.9F, -16.0F, 0.0F, 0.0F),
				rotL(1.0F, -14.5F, 0.0F, 0.0F),
				rotL(1.1F, -13.402F, 0.0F, 0.0F),
				rotL(1.2F, -13.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 7.0F, 17.0F),
				rotL(0.2F, 0.0F, 12.124F, 29.445F),
				rotL(0.3F, 0.0F, 14.0F, 34.0F),
				rotL(0.4F, 0.0F, 12.124F, 29.445F),
				rotL(0.5F, 0.0F, 7.0F, 17.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, -7.0F, -17.0F),
				rotL(0.8F, 0.0F, -12.124F, -29.445F),
				rotL(0.9F, 0.0F, -14.0F, -34.0F),
				rotL(1.0F, 0.0F, -12.124F, -29.445F),
				rotL(1.1F, 0.0F, -7.0F, -17.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, -8.134F, -22.776F),
				rotL(0.1F, 0.0F, -4.136F, -11.581F),
				rotL(0.2F, 0.0F, 0.97F, 2.717F),
				rotL(0.3F, 0.0F, 5.817F, 16.287F),
				rotL(0.4F, 0.0F, 9.105F, 25.493F),
				rotL(0.5F, 0.0F, 9.953F, 27.868F),
				rotL(0.6F, 0.0F, 8.134F, 22.776F),
				rotL(0.7F, 0.0F, 4.136F, 11.581F),
				rotL(0.8F, 0.0F, -0.97F, -2.717F),
				rotL(0.9F, 0.0F, -5.817F, -16.287F),
				rotL(1.0F, 0.0F, -9.105F, -25.493F),
				rotL(1.1F, 0.0F, -9.953F, -27.868F),
				rotL(1.2F, 0.0F, -8.134F, -22.776F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, -21.648F),
				rotL(0.1F, 0.0F, 0.0F, -20.708F),
				rotL(0.2F, 0.0F, 0.0F, -14.22F),
				rotL(0.3F, 0.0F, 0.0F, -3.921F),
				rotL(0.4F, 0.0F, 0.0F, 7.428F),
				rotL(0.5F, 0.0F, 0.0F, 16.787F),
				rotL(0.6F, 0.0F, 0.0F, 21.648F),
				rotL(0.7F, 0.0F, 0.0F, 20.708F),
				rotL(0.8F, 0.0F, 0.0F, 14.22F),
				rotL(0.9F, 0.0F, 0.0F, 3.921F),
				rotL(1.0F, 0.0F, 0.0F, -7.428F),
				rotL(1.1F, 0.0F, 0.0F, -16.787F),
				rotL(1.2F, 0.0F, 0.0F, -21.648F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 22.0F, 0.0F, 6.0F),
				rotL(0.1F, 20.0F, 0.0F, 6.0F),
				rotL(0.2F, 18.536F, 0.0F, 6.0F),
				rotL(0.3F, 18.0F, 0.0F, 6.0F),
				rotL(0.4F, 18.536F, 0.0F, 6.0F),
				rotL(0.5F, 20.0F, 0.0F, 6.0F),
				rotL(0.6F, 22.0F, 0.0F, 6.0F),
				rotL(0.7F, 24.0F, 0.0F, 6.0F),
				rotL(0.8F, 25.464F, 0.0F, 6.0F),
				rotL(0.9F, 26.0F, 0.0F, 6.0F),
				rotL(1.0F, 25.464F, 0.0F, 6.0F),
				rotL(1.1F, 24.0F, 0.0F, 6.0F),
				rotL(1.2F, 22.0F, 0.0F, 6.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, 0.0F, 0.0F),
				rotL(0.1F, -26.0F, 0.0F, 0.0F),
				rotL(0.2F, -26.0F, 0.0F, 0.0F),
				rotL(0.3F, -26.0F, 0.0F, 0.0F),
				rotL(0.4F, -26.0F, 0.0F, 0.0F),
				rotL(0.5F, -26.0F, 0.0F, 0.0F),
				rotL(0.6F, -26.0F, 0.0F, 0.0F),
				rotL(0.7F, -26.0F, 0.0F, 0.0F),
				rotL(0.8F, -26.0F, 0.0F, 0.0F),
				rotL(0.9F, -26.0F, 0.0F, 0.0F),
				rotL(1.0F, -26.0F, 0.0F, 0.0F),
				rotL(1.1F, -26.0F, 0.0F, 0.0F),
				rotL(1.2F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 15.0F, 0.0F, 0.0F),
				rotL(0.1F, 15.402F, 0.0F, 0.0F),
				rotL(0.2F, 16.5F, 0.0F, 0.0F),
				rotL(0.3F, 18.0F, 0.0F, 0.0F),
				rotL(0.4F, 19.5F, 0.0F, 0.0F),
				rotL(0.5F, 20.598F, 0.0F, 0.0F),
				rotL(0.6F, 21.0F, 0.0F, 0.0F),
				rotL(0.7F, 20.598F, 0.0F, 0.0F),
				rotL(0.8F, 19.5F, 0.0F, 0.0F),
				rotL(0.9F, 18.0F, 0.0F, 0.0F),
				rotL(1.0F, 16.5F, 0.0F, 0.0F),
				rotL(1.1F, 15.402F, 0.0F, 0.0F),
				rotL(1.2F, 15.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 16.0F, 0.0F, 0.0F),
				rotL(0.1F, 16.0F, 0.0F, 0.0F),
				rotL(0.2F, 16.0F, 0.0F, 0.0F),
				rotL(0.3F, 16.0F, 0.0F, 0.0F),
				rotL(0.4F, 16.0F, 0.0F, 0.0F),
				rotL(0.5F, 16.0F, 0.0F, 0.0F),
				rotL(0.6F, 16.0F, 0.0F, 0.0F),
				rotL(0.7F, 16.0F, 0.0F, 0.0F),
				rotL(0.8F, 16.0F, 0.0F, 0.0F),
				rotL(0.9F, 16.0F, 0.0F, 0.0F),
				rotL(1.0F, 16.0F, 0.0F, 0.0F),
				rotL(1.1F, 16.0F, 0.0F, 0.0F),
				rotL(1.2F, 16.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.1F, 26.0F, 0.0F, 0.0F),
				rotL(0.2F, 26.0F, 0.0F, 0.0F),
				rotL(0.3F, 26.0F, 0.0F, 0.0F),
				rotL(0.4F, 26.0F, 0.0F, 0.0F),
				rotL(0.5F, 26.0F, 0.0F, 0.0F),
				rotL(0.6F, 26.0F, 0.0F, 0.0F),
				rotL(0.7F, 26.0F, 0.0F, 0.0F),
				rotL(0.8F, 26.0F, 0.0F, 0.0F),
				rotL(0.9F, 26.0F, 0.0F, 0.0F),
				rotL(1.0F, 26.0F, 0.0F, 0.0F),
				rotL(1.1F, 26.0F, 0.0F, 0.0F),
				rotL(1.2F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, -7.0F, -17.0F),
				rotL(0.2F, 0.0F, -12.124F, -29.445F),
				rotL(0.3F, 0.0F, -14.0F, -34.0F),
				rotL(0.4F, 0.0F, -12.124F, -29.445F),
				rotL(0.5F, 0.0F, -7.0F, -17.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 7.0F, 17.0F),
				rotL(0.8F, 0.0F, 12.124F, 29.445F),
				rotL(0.9F, 0.0F, 14.0F, 34.0F),
				rotL(1.0F, 0.0F, 12.124F, 29.445F),
				rotL(1.1F, 0.0F, 7.0F, 17.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 8.134F, 22.776F),
				rotL(0.1F, 0.0F, 4.136F, 11.581F),
				rotL(0.2F, 0.0F, -0.97F, -2.717F),
				rotL(0.3F, 0.0F, -5.817F, -16.287F),
				rotL(0.4F, 0.0F, -9.105F, -25.493F),
				rotL(0.5F, 0.0F, -9.953F, -27.868F),
				rotL(0.6F, 0.0F, -8.134F, -22.776F),
				rotL(0.7F, 0.0F, -4.136F, -11.581F),
				rotL(0.8F, 0.0F, 0.97F, 2.717F),
				rotL(0.9F, 0.0F, 5.817F, 16.287F),
				rotL(1.0F, 0.0F, 9.105F, 25.493F),
				rotL(1.1F, 0.0F, 9.953F, 27.868F),
				rotL(1.2F, 0.0F, 8.134F, 22.776F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 21.648F),
				rotL(0.1F, 0.0F, 0.0F, 20.708F),
				rotL(0.2F, 0.0F, 0.0F, 14.22F),
				rotL(0.3F, 0.0F, 0.0F, 3.921F),
				rotL(0.4F, 0.0F, 0.0F, -7.428F),
				rotL(0.5F, 0.0F, 0.0F, -16.787F),
				rotL(0.6F, 0.0F, 0.0F, -21.648F),
				rotL(0.7F, 0.0F, 0.0F, -20.708F),
				rotL(0.8F, 0.0F, 0.0F, -14.22F),
				rotL(0.9F, 0.0F, 0.0F, -3.921F),
				rotL(1.0F, 0.0F, 0.0F, 7.428F),
				rotL(1.1F, 0.0F, 0.0F, 16.787F),
				rotL(1.2F, 0.0F, 0.0F, 21.648F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 22.0F, 0.0F, -6.0F),
				rotL(0.1F, 20.0F, 0.0F, -6.0F),
				rotL(0.2F, 18.536F, 0.0F, -6.0F),
				rotL(0.3F, 18.0F, 0.0F, -6.0F),
				rotL(0.4F, 18.536F, 0.0F, -6.0F),
				rotL(0.5F, 20.0F, 0.0F, -6.0F),
				rotL(0.6F, 22.0F, 0.0F, -6.0F),
				rotL(0.7F, 24.0F, 0.0F, -6.0F),
				rotL(0.8F, 25.464F, 0.0F, -6.0F),
				rotL(0.9F, 26.0F, 0.0F, -6.0F),
				rotL(1.0F, 25.464F, 0.0F, -6.0F),
				rotL(1.1F, 24.0F, 0.0F, -6.0F),
				rotL(1.2F, 22.0F, 0.0F, -6.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, 0.0F, 0.0F),
				rotL(0.1F, -26.0F, 0.0F, 0.0F),
				rotL(0.2F, -26.0F, 0.0F, 0.0F),
				rotL(0.3F, -26.0F, 0.0F, 0.0F),
				rotL(0.4F, -26.0F, 0.0F, 0.0F),
				rotL(0.5F, -26.0F, 0.0F, 0.0F),
				rotL(0.6F, -26.0F, 0.0F, 0.0F),
				rotL(0.7F, -26.0F, 0.0F, 0.0F),
				rotL(0.8F, -26.0F, 0.0F, 0.0F),
				rotL(0.9F, -26.0F, 0.0F, 0.0F),
				rotL(1.0F, -26.0F, 0.0F, 0.0F),
				rotL(1.1F, -26.0F, 0.0F, 0.0F),
				rotL(1.2F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 15.0F, 0.0F, 0.0F),
				rotL(0.1F, 15.402F, 0.0F, 0.0F),
				rotL(0.2F, 16.5F, 0.0F, 0.0F),
				rotL(0.3F, 18.0F, 0.0F, 0.0F),
				rotL(0.4F, 19.5F, 0.0F, 0.0F),
				rotL(0.5F, 20.598F, 0.0F, 0.0F),
				rotL(0.6F, 21.0F, 0.0F, 0.0F),
				rotL(0.7F, 20.598F, 0.0F, 0.0F),
				rotL(0.8F, 19.5F, 0.0F, 0.0F),
				rotL(0.9F, 18.0F, 0.0F, 0.0F),
				rotL(1.0F, 16.5F, 0.0F, 0.0F),
				rotL(1.1F, 15.402F, 0.0F, 0.0F),
				rotL(1.2F, 15.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 16.0F, 0.0F, 0.0F),
				rotL(0.1F, 16.0F, 0.0F, 0.0F),
				rotL(0.2F, 16.0F, 0.0F, 0.0F),
				rotL(0.3F, 16.0F, 0.0F, 0.0F),
				rotL(0.4F, 16.0F, 0.0F, 0.0F),
				rotL(0.5F, 16.0F, 0.0F, 0.0F),
				rotL(0.6F, 16.0F, 0.0F, 0.0F),
				rotL(0.7F, 16.0F, 0.0F, 0.0F),
				rotL(0.8F, 16.0F, 0.0F, 0.0F),
				rotL(0.9F, 16.0F, 0.0F, 0.0F),
				rotL(1.0F, 16.0F, 0.0F, 0.0F),
				rotL(1.1F, 16.0F, 0.0F, 0.0F),
				rotL(1.2F, 16.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.1F, 26.0F, 0.0F, 0.0F),
				rotL(0.2F, 26.0F, 0.0F, 0.0F),
				rotL(0.3F, 26.0F, 0.0F, 0.0F),
				rotL(0.4F, 26.0F, 0.0F, 0.0F),
				rotL(0.5F, 26.0F, 0.0F, 0.0F),
				rotL(0.6F, 26.0F, 0.0F, 0.0F),
				rotL(0.7F, 26.0F, 0.0F, 0.0F),
				rotL(0.8F, 26.0F, 0.0F, 0.0F),
				rotL(0.9F, 26.0F, 0.0F, 0.0F),
				rotL(1.0F, 26.0F, 0.0F, 0.0F),
				rotL(1.1F, 26.0F, 0.0F, 0.0F),
				rotL(1.2F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 4.392F, 0.0F, 0.0F),
				rotL(0.1F, 2.765F, 0.0F, 0.0F),
				rotL(0.2F, 1.201F, 0.0F, 0.0F),
				rotL(0.3F, 0.119F, 0.0F, 0.0F),
				rotL(0.4F, -0.191F, 0.0F, 0.0F),
				rotL(0.5F, 0.354F, 0.0F, 0.0F),
				rotL(0.6F, 1.608F, 0.0F, 0.0F),
				rotL(0.7F, 3.235F, 0.0F, 0.0F),
				rotL(0.8F, 4.799F, 0.0F, 0.0F),
				rotL(0.9F, 5.881F, 0.0F, 0.0F),
				rotL(1.0F, 6.191F, 0.0F, 0.0F),
				rotL(1.1F, 5.646F, 0.0F, 0.0F),
				rotL(1.2F, 4.392F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.507F, 0.0F, 0.0F),
				rotL(0.1F, 4.176F, 0.0F, 0.0F),
				rotL(0.2F, 2.531F, 0.0F, 0.0F),
				rotL(0.3F, 1.011F, 0.0F, 0.0F),
				rotL(0.4F, 0.024F, 0.0F, 0.0F),
				rotL(0.5F, -0.165F, 0.0F, 0.0F),
				rotL(0.6F, 0.493F, 0.0F, 0.0F),
				rotL(0.7F, 1.824F, 0.0F, 0.0F),
				rotL(0.8F, 3.469F, 0.0F, 0.0F),
				rotL(0.9F, 4.989F, 0.0F, 0.0F),
				rotL(1.0F, 5.976F, 0.0F, 0.0F),
				rotL(1.1F, 6.165F, 0.0F, 0.0F),
				rotL(1.2F, 5.507F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 6.122F, 0.0F, 0.0F),
				rotL(0.1F, 5.354F, 0.0F, 0.0F),
				rotL(0.2F, 3.954F, 0.0F, 0.0F),
				rotL(0.3F, 2.299F, 0.0F, 0.0F),
				rotL(0.4F, 0.832F, 0.0F, 0.0F),
				rotL(0.5F, -0.054F, 0.0F, 0.0F),
				rotL(0.6F, -0.122F, 0.0F, 0.0F),
				rotL(0.7F, 0.646F, 0.0F, 0.0F),
				rotL(0.8F, 2.046F, 0.0F, 0.0F),
				rotL(0.9F, 3.701F, 0.0F, 0.0F),
				rotL(1.0F, 5.168F, 0.0F, 0.0F),
				rotL(1.1F, 6.054F, 0.0F, 0.0F),
				rotL(1.2F, 6.122F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 6.116F, 0.0F, 0.0F),
				rotL(0.1F, 6.062F, 0.0F, 0.0F),
				rotL(0.2F, 5.188F, 0.0F, 0.0F),
				rotL(0.3F, 3.727F, 0.0F, 0.0F),
				rotL(0.4F, 2.071F, 0.0F, 0.0F),
				rotL(0.5F, 0.665F, 0.0F, 0.0F),
				rotL(0.6F, -0.116F, 0.0F, 0.0F),
				rotL(0.7F, -0.062F, 0.0F, 0.0F),
				rotL(0.8F, 0.812F, 0.0F, 0.0F),
				rotL(0.9F, 2.273F, 0.0F, 0.0F),
				rotL(1.0F, 3.929F, 0.0F, 0.0F),
				rotL(1.1F, 5.335F, 0.0F, 0.0F),
				rotL(1.2F, 6.116F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.49F, 0.0F, 0.0F),
				rotL(0.1F, 6.161F, 0.0F, 0.0F),
				rotL(0.2F, 5.986F, 0.0F, 0.0F),
				rotL(0.3F, 5.01F, 0.0F, 0.0F),
				rotL(0.4F, 3.496F, 0.0F, 0.0F),
				rotL(0.5F, 1.849F, 0.0F, 0.0F),
				rotL(0.6F, 0.51F, 0.0F, 0.0F),
				rotL(0.7F, -0.161F, 0.0F, 0.0F),
				rotL(0.8F, 0.014F, 0.0F, 0.0F),
				rotL(0.9F, 0.99F, 0.0F, 0.0F),
				rotL(1.0F, 2.504F, 0.0F, 0.0F),
				rotL(1.1F, 4.151F, 0.0F, 0.0F),
				rotL(1.2F, 5.49F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.2F).looping();
			ch_0(b);
			ch_1(b);
			ch_2(b);
			ch_3(b);
			ch_4(b);
			ch_5(b);
			ch_6(b);
			ch_7(b);
			ch_8(b);
			ch_9(b);
			ch_10(b);
			ch_11(b);
			ch_12(b);
			ch_13(b);
			ch_14(b);
			ch_15(b);
			ch_16(b);
			ch_17(b);
			ch_18(b);
			ch_19(b);
			ch_20(b);
			ch_21(b);
			ch_22(b);
			ch_23(b);
			return b.build();
		}
	}

	private static final class AttackAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.45F, 0.0F, 3.2F, 1.8F),
				posL(0.85F, 0.0F, 4.2F, -5.2F),
				posL(1.22F, 0.0F, 0.5F, -1.2F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, -22.0F, 0.0F, 0.0F),
				rotL(0.85F, 22.0F, 0.0F, 0.0F),
				rotL(1.22F, -6.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 10.0F, 0.0F, 0.0F),
				rotL(0.85F, -20.0F, 0.0F, 0.0F),
				rotL(1.22F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, 38.0F, 0.0F, 0.0F),
				rotL(0.52F, 42.0F, 0.0F, 0.0F),
				rotL(0.86F, -4.0F, 0.0F, 0.0F),
				rotL(1.18F, 6.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 0.0F, 16.0F, 34.0F),
				rotL(0.85F, 0.0F, -28.0F, -24.0F),
				rotL(1.22F, 0.0F, 8.0F, 12.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 0.0F, 8.0F, 22.0F),
				rotL(0.85F, 0.0F, -24.0F, -20.0F),
				rotL(1.22F, 0.0F, 0.0F, 8.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, -52.0F, 12.0F, 22.0F),
				rotL(0.85F, -28.0F, -18.0F, -10.0F),
				rotL(1.22F, 8.0F, 0.0F, 6.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, -28.0F, 0.0F, 0.0F),
				rotL(0.85F, -34.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, -16.0F, 0.0F, 0.0F),
				rotL(0.85F, -32.0F, 0.0F, 0.0F),
				rotL(1.22F, -6.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 18.0F, 0.0F, 0.0F),
				rotL(0.85F, 16.0F, 0.0F, 0.0F),
				rotL(1.22F, 6.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 0.0F, -16.0F, -34.0F),
				rotL(0.85F, 0.0F, 28.0F, 24.0F),
				rotL(1.22F, 0.0F, -8.0F, -12.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 0.0F, -8.0F, -22.0F),
				rotL(0.85F, 0.0F, 24.0F, 20.0F),
				rotL(1.22F, 0.0F, 0.0F, -8.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, -52.0F, -12.0F, -22.0F),
				rotL(0.85F, -28.0F, 18.0F, 10.0F),
				rotL(1.22F, 8.0F, 0.0F, -6.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, -28.0F, 0.0F, 0.0F),
				rotL(0.85F, -34.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, -16.0F, 0.0F, 0.0F),
				rotL(0.85F, -32.0F, 0.0F, 0.0F),
				rotL(1.22F, -6.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 18.0F, 0.0F, 0.0F),
				rotL(0.85F, 16.0F, 0.0F, 0.0F),
				rotL(1.22F, 6.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 6.0F, 0.0F, 0.0F),
				rotL(0.85F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 6.0F, 0.0F, 0.0F),
				rotL(0.85F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 6.0F, 0.0F, 0.0F),
				rotL(0.85F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 6.0F, 0.0F, 0.0F),
				rotL(0.85F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 6.0F, 0.0F, 0.0F),
				rotL(0.85F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.6F);
			ch_0(b);
			ch_1(b);
			ch_2(b);
			ch_3(b);
			ch_4(b);
			ch_5(b);
			ch_6(b);
			ch_7(b);
			ch_8(b);
			ch_9(b);
			ch_10(b);
			ch_11(b);
			ch_12(b);
			ch_13(b);
			ch_14(b);
			ch_15(b);
			ch_16(b);
			ch_17(b);
			ch_18(b);
			ch_19(b);
			ch_20(b);
			return b.build();
		}
	}

	private static final class BlinkOutAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, 18.0F, 0.0F),
				rotL(0.7F, 0.0F, -110.0F, 0.0F),
				rotL(1.0F, 0.0F, -140.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.25F, 0.0F, 3.2F, 0.0F),
				posL(0.65F, 0.0F, 12.5F, 0.0F),
				posL(0.9F, 0.0F, 17.0F, 0.0F),
				posL(1.0F, 0.0F, 17.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.25F, 0.92F, 1.08F, 0.92F),
				sclL(0.6F, 0.28F, 0.72F, 0.28F),
				sclL(0.85F, 0.06F, 0.35F, 0.06F),
				sclL(0.94F, 0.0F, 0.0F, 0.0F),
				sclL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 0.0F, -44.0F, -14.0F),
				rotL(1.0F, 0.0F, -44.0F, -14.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 0.0F, -48.0F, -20.0F),
				rotL(1.0F, 0.0F, -48.0F, -20.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 0.0F, -30.0F, -10.0F),
				rotL(1.0F, 0.0F, -30.0F, -10.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, -26.0F, -22.0F, -12.0F),
				rotL(1.0F, -26.0F, -22.0F, -12.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, -22.0F, 0.0F, 0.0F),
				rotL(1.0F, -22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 26.0F, 0.0F, 0.0F),
				rotL(1.0F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 0.0F, 44.0F, 14.0F),
				rotL(1.0F, 0.0F, 44.0F, 14.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 0.0F, 48.0F, 20.0F),
				rotL(1.0F, 0.0F, 48.0F, 20.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 0.0F, 30.0F, 10.0F),
				rotL(1.0F, 0.0F, 30.0F, 10.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, -26.0F, 22.0F, 12.0F),
				rotL(1.0F, -26.0F, 22.0F, 12.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, -22.0F, 0.0F, 0.0F),
				rotL(1.0F, -22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 26.0F, 0.0F, 0.0F),
				rotL(1.0F, 26.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.0F);
			ch_0(b);
			ch_1(b);
			ch_2(b);
			ch_3(b);
			ch_4(b);
			ch_5(b);
			ch_6(b);
			ch_7(b);
			ch_8(b);
			ch_9(b);
			ch_10(b);
			ch_11(b);
			ch_12(b);
			ch_13(b);
			ch_14(b);
			return b.build();
		}
	}

	private static final class BlinkInAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, -140.0F, 0.0F),
				rotL(0.35F, 0.0F, 10.0F, 0.0F),
				rotL(0.68F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 17.0F, 0.0F),
				posL(0.32F, 0.0F, 5.2F, 0.0F),
				posL(0.62F, 0.0F, 0.25F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 0.0F, 0.0F, 0.0F),
				sclL(0.08F, 0.16F, 0.58F, 0.16F),
				sclL(0.28F, 1.05F, 0.96F, 1.05F),
				sclL(0.55F, 1.0F, 1.0F, 1.0F),
				sclL(1.0F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 28.0F, 0.0F, 0.0F),
				rotL(0.68F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, -44.0F, -14.0F),
				rotL(0.32F, 0.0F, 16.0F, 28.0F),
				rotL(0.65F, 0.0F, 4.0F, 8.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, -48.0F, -20.0F),
				rotL(0.32F, 0.0F, 8.0F, 20.0F),
				rotL(0.65F, 0.0F, 0.0F, 6.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, -30.0F, -10.0F),
				rotL(0.32F, 0.0F, 0.0F, 12.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, -22.0F, -12.0F),
				rotL(0.32F, -18.0F, 10.0F, 16.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -22.0F, 0.0F, 0.0F),
				rotL(0.32F, -14.0F, 0.0F, 0.0F),
				rotL(0.62F, -4.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.32F, 14.0F, 0.0F, 0.0F),
				rotL(0.62F, 4.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 44.0F, 14.0F),
				rotL(0.32F, 0.0F, -16.0F, -28.0F),
				rotL(0.65F, 0.0F, -4.0F, -8.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 48.0F, 20.0F),
				rotL(0.32F, 0.0F, -8.0F, -20.0F),
				rotL(0.65F, 0.0F, 0.0F, -6.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 30.0F, 10.0F),
				rotL(0.32F, 0.0F, 0.0F, -12.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, 22.0F, 12.0F),
				rotL(0.32F, -18.0F, -10.0F, -16.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -22.0F, 0.0F, 0.0F),
				rotL(0.32F, -14.0F, 0.0F, 0.0F),
				rotL(0.62F, -4.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.32F, 14.0F, 0.0F, 0.0F),
				rotL(0.62F, 4.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.0F);
			ch_0(b);
			ch_1(b);
			ch_2(b);
			ch_3(b);
			ch_4(b);
			ch_5(b);
			ch_6(b);
			ch_7(b);
			ch_8(b);
			ch_9(b);
			ch_10(b);
			ch_11(b);
			ch_12(b);
			ch_13(b);
			ch_14(b);
			ch_15(b);
			return b.build();
		}
	}

	private static final class HurtAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.16F, 0.0F, 0.45F, 1.5F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -15.0F, -6.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -14.0F, -14.0F, -6.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 28.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 0.0F, -30.0F, 14.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -28.0F, 0.0F, 14.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 0.0F, 16.0F, -14.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -28.0F, 0.0F, -14.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(0.6F);
			ch_0(b);
			ch_1(b);
			ch_2(b);
			ch_3(b);
			ch_4(b);
			ch_5(b);
			ch_6(b);
			ch_7(b);
			return b.build();
		}
	}

	private static final class DeathAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.35F, 0.0F, 0.8F, 1.4F),
				posL(0.9F, 0.0F, 3.214F, -0.5F),
				posL(1.45F, 0.0F, -2.39F, -2.5F),
				posL(1.8F, 0.0F, -2.39F, -2.5F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, -18.0F, 0.0F, 0.0F),
				rotL(0.85F, 22.0F, 0.0F, 0.0F),
				rotL(1.45F, 52.0F, 0.0F, 0.0F),
				rotL(1.8F, 52.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, -16.0F, 8.0F, 0.0F),
				rotL(1.45F, -28.0F, -14.0F, -8.0F),
				rotL(1.8F, -28.0F, -14.0F, -8.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 36.0F, 0.0F, 0.0F),
				rotL(1.45F, 14.0F, 0.0F, 0.0F),
				rotL(1.8F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 0.0F, 14.0F, 24.0F),
				rotL(1.45F, 18.0F, 8.0F, -26.0F),
				rotL(1.8F, 18.0F, 8.0F, -26.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 0.0F, 0.0F, 16.0F),
				rotL(1.45F, 0.0F, 0.0F, 22.0F),
				rotL(1.8F, 0.0F, 0.0F, 22.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, -22.0F, 0.0F, 18.0F),
				rotL(1.45F, -86.0F, 12.0F, 42.0F),
				rotL(1.8F, -86.0F, 12.0F, 42.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.45F, -22.0F, 0.0F, 0.0F),
				rotL(1.8F, -22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 0.0F, 0.0F, 0.0F),
				rotL(1.45F, 52.0F, 0.0F, 26.0F),
				rotL(1.8F, 52.0F, 0.0F, 26.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.45F, 14.0F, 0.0F, 0.0F),
				rotL(1.8F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.45F, 18.0F, 0.0F, 0.0F),
				rotL(1.8F, 18.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 0.0F, -14.0F, -24.0F),
				rotL(1.45F, 18.0F, -8.0F, 26.0F),
				rotL(1.8F, 18.0F, -8.0F, 26.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("wing_elbow_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 0.0F, 0.0F, -16.0F),
				rotL(1.45F, 0.0F, 0.0F, -22.0F),
				rotL(1.8F, 0.0F, 0.0F, -22.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, -22.0F, 0.0F, -18.0F),
				rotL(1.45F, -86.0F, -12.0F, -42.0F),
				rotL(1.8F, -86.0F, -12.0F, -42.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.45F, -22.0F, 0.0F, 0.0F),
				rotL(1.8F, -22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 0.0F, 0.0F, 0.0F),
				rotL(1.45F, 52.0F, 0.0F, -26.0F),
				rotL(1.8F, 52.0F, 0.0F, -26.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.45F, 14.0F, 0.0F, 0.0F),
				rotL(1.8F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.45F, 18.0F, 0.0F, 0.0F),
				rotL(1.8F, 18.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 5.0F, 0.0F, 0.0F),
				rotL(1.45F, 4.0F, -2.5F, 0.0F),
				rotL(1.8F, 4.0F, -2.5F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 5.0F, 0.0F, 0.0F),
				rotL(1.45F, 4.0F, -5.0F, 0.0F),
				rotL(1.8F, 4.0F, -5.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 5.0F, 0.0F, 0.0F),
				rotL(1.45F, 4.0F, -7.5F, 0.0F),
				rotL(1.8F, 4.0F, -7.5F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 5.0F, 0.0F, 0.0F),
				rotL(1.45F, 4.0F, -10.0F, 0.0F),
				rotL(1.8F, 4.0F, -10.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 5.0F, 0.0F, 0.0F),
				rotL(1.45F, 4.0F, -12.5F, 0.0F),
				rotL(1.8F, 4.0F, -12.5F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.8F);
			ch_0(b);
			ch_1(b);
			ch_2(b);
			ch_3(b);
			ch_4(b);
			ch_5(b);
			ch_6(b);
			ch_7(b);
			ch_8(b);
			ch_9(b);
			ch_10(b);
			ch_11(b);
			ch_12(b);
			ch_13(b);
			ch_14(b);
			ch_15(b);
			ch_16(b);
			ch_17(b);
			ch_18(b);
			ch_19(b);
			ch_20(b);
			ch_21(b);
			ch_22(b);
			return b.build();
		}
	}

}
