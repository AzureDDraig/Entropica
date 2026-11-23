package ddraig.net.entropica.client.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Exact Blockbench keyframe animations for Obsidian Wendigo (Fauna 59 — Forest Fiend).
 * Split into per-animation static builders and <=60-keyframe helpers so constant pools and method bytecodes stay tiny.
 */
public final class ObsidianWendigoAnimations {

	private ObsidianWendigoAnimations() {}

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
	public static final AnimationDefinition WALK_START = WalkStartAnimBuilder.create();
	public static final AnimationDefinition walk_start = WALK_START;
	public static final AnimationDefinition WALK_STOP = WalkStopAnimBuilder.create();
	public static final AnimationDefinition walk_stop = WALK_STOP;
	public static final AnimationDefinition RUN = RunAnimBuilder.create();
	public static final AnimationDefinition run = RUN;
	public static final AnimationDefinition RUN_START = RunStartAnimBuilder.create();
	public static final AnimationDefinition run_start = RUN_START;
	public static final AnimationDefinition RUN_STOP = RunStopAnimBuilder.create();
	public static final AnimationDefinition run_stop = RUN_STOP;
	public static final AnimationDefinition CLIMB = ClimbAnimBuilder.create();
	public static final AnimationDefinition climb = CLIMB;
	public static final AnimationDefinition CLIMB_START = ClimbStartAnimBuilder.create();
	public static final AnimationDefinition climb_start = CLIMB_START;
	public static final AnimationDefinition CLIMB_STOP = ClimbStopAnimBuilder.create();
	public static final AnimationDefinition climb_stop = CLIMB_STOP;
	public static final AnimationDefinition ATTACK = AttackAnimBuilder.create();
	public static final AnimationDefinition attack = ATTACK;
	public static final AnimationDefinition SCREECH = ScreechAnimBuilder.create();
	public static final AnimationDefinition screech = SCREECH;
	public static final AnimationDefinition HURT = HurtAnimBuilder.create();
	public static final AnimationDefinition hurt = HURT;
	public static final AnimationDefinition DEATH = DeathAnimBuilder.create();
	public static final AnimationDefinition death = DEATH;

	private static final class IdleAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 3.0F, -1.0F, 1.5F),
				rotL(1.75F, -2.0F, 1.0F, -1.0F),
				rotL(2.75F, 4.0F, -2.0F, 2.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 4.0F, -3.0F, -1.5F),
				rotL(1.75F, -3.0F, 2.0F, 1.0F),
				rotL(2.75F, 5.0F, 3.0F, -2.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, -3.0F, 0.0F, -2.0F),
				rotL(0.94F, -4.0F, 0.0F, -3.0F),
				rotL(1.06F, -5.0F, 0.0F, -2.0F),
				rotL(1.75F, 2.0F, 0.0F, -1.0F),
				rotL(2.36F, -12.0F, 0.0F, 5.0F),
				rotL(2.48F, -5.0F, 0.0F, -4.0F),
				rotL(2.6F, -8.0F, 0.0F, 2.0F),
				rotL(3.2F, -1.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, -5.0F, 0.0F, 0.0F),
				rotL(0.94F, -8.0F, 0.0F, 0.0F),
				rotL(1.06F, -6.0F, 0.0F, 0.0F),
				rotL(1.75F, -2.0F, 0.0F, 0.0F),
				rotL(2.36F, -18.0F, 0.0F, 0.0F),
				rotL(2.48F, -9.0F, 0.0F, 0.0F),
				rotL(2.6F, -12.0F, 0.0F, 0.0F),
				rotL(3.2F, -2.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, -10.0F),
				rotL(0.94F, 0.0F, 0.0F, 14.0F),
				rotL(1.06F, 0.0F, 0.0F, -12.0F),
				rotL(1.75F, 0.0F, 0.0F, 4.0F),
				rotL(2.36F, 0.0F, 0.0F, 22.0F),
				rotL(2.48F, 0.0F, 0.0F, -18.0F),
				rotL(2.6F, 0.0F, 0.0F, 14.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, -4.0F, 0.0F, 2.0F),
				rotL(0.94F, -10.0F, 0.0F, -4.0F),
				rotL(1.06F, -5.0F, 0.0F, 3.0F),
				rotL(1.75F, 2.0F, 0.0F, 1.0F),
				rotL(2.36F, -4.0F, 0.0F, 3.0F),
				rotL(2.48F, -6.0F, 0.0F, 2.0F),
				rotL(2.6F, -3.0F, 0.0F, 1.0F),
				rotL(3.2F, -1.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, -6.0F, 0.0F, 0.0F),
				rotL(0.94F, -16.0F, 0.0F, 0.0F),
				rotL(1.06F, -8.0F, 0.0F, 0.0F),
				rotL(1.75F, -2.0F, 0.0F, 0.0F),
				rotL(2.36F, -7.0F, 0.0F, 0.0F),
				rotL(2.48F, -10.0F, 0.0F, 0.0F),
				rotL(2.6F, -4.0F, 0.0F, 0.0F),
				rotL(3.2F, -2.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 10.0F),
				rotL(0.94F, 0.0F, 0.0F, -20.0F),
				rotL(1.06F, 0.0F, 0.0F, 16.0F),
				rotL(1.75F, 0.0F, 0.0F, -4.0F),
				rotL(2.36F, 0.0F, 0.0F, -14.0F),
				rotL(2.48F, 0.0F, 0.0F, 12.0F),
				rotL(2.6F, 0.0F, 0.0F, -6.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 5.0F, 0.0F, 2.0F),
				rotL(1.75F, -4.0F, 0.0F, -2.0F),
				rotL(2.75F, 6.0F, 0.0F, -3.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.86F, 2.0F, -2.0F, 1.0F),
				rotL(0.94F, -8.0F, -20.0F, 28.0F),
				rotL(1.02F, -5.0F, -17.0F, 24.0F),
				rotL(1.48F, -5.0F, -18.0F, 25.0F),
				rotL(1.85F, 3.0F, 0.0F, 0.0F),
				rotL(2.28F, 3.0F, 2.0F, -1.0F),
				rotL(2.36F, -10.0F, 22.0F, -30.0F),
				rotL(2.44F, -4.0F, 14.0F, -18.0F),
				rotL(2.52F, -9.0F, 20.0F, -27.0F),
				rotL(2.95F, -6.0F, 15.0F, -20.0F),
				rotL(3.32F, 1.0F, 3.0F, -4.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.86F, 0.0F, 0.0F, 0.0F),
				rotL(0.93F, 22.0F, 0.0F, 0.0F),
				rotL(1.0F, -2.0F, 0.0F, 0.0F),
				rotL(1.07F, 14.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, 0.0F),
				rotL(2.28F, 0.0F, 0.0F, 0.0F),
				rotL(2.35F, 24.0F, 0.0F, 0.0F),
				rotL(2.42F, -2.0F, 0.0F, 0.0F),
				rotL(2.49F, 18.0F, 0.0F, 0.0F),
				rotL(2.56F, -2.0F, 0.0F, 0.0F),
				rotL(2.64F, 10.0F, 0.0F, 0.0F),
				rotL(2.74F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(3.6F).looping();
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

	private static final class WalkAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 7.0F, 5.0F, -2.0F),
				rotL(0.24F, 8.0F, 3.0F, -1.5F),
				rotL(0.48F, 9.0F, -1.0F, 0.5F),
				rotL(0.72F, 6.0F, -5.0F, 2.0F),
				rotL(0.9F, 7.0F, -5.0F, 2.0F),
				rotL(1.14F, 8.0F, -3.0F, 1.5F),
				rotL(1.38F, 9.0F, 1.0F, -0.5F),
				rotL(1.62F, 6.0F, 5.0F, -2.0F),
				rotL(1.8F, 7.0F, 5.0F, -2.0F)
			));
		}
		private static void ch_3_p0(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(0.0F, 0.0F, -0.609F, 0.0F),
				posL(0.02F, 0.0F, -0.822F, 0.0F),
				posL(0.04F, 0.0F, -1.053F, 0.0F),
				posL(0.06F, 0.0F, -1.082F, 0.0F),
				posL(0.08F, 0.0F, -0.968F, 0.0F),
				posL(0.1F, 0.0F, -0.857F, 0.0F),
				posL(0.12F, 0.0F, -0.749F, 0.0F),
				posL(0.14F, 0.0F, -0.643F, 0.0F),
				posL(0.16F, 0.0F, -0.54F, 0.0F),
				posL(0.18F, 0.0F, -0.439F, 0.0F),
				posL(0.2F, 0.0F, -0.341F, 0.0F),
				posL(0.22F, 0.0F, -0.246F, 0.0F),
				posL(0.24F, 0.0F, -0.153F, 0.0F),
				posL(0.26F, 0.0F, -0.111F, 0.0F),
				posL(0.28F, 0.0F, -0.073F, 0.0F),
				posL(0.3F, 0.0F, -0.042F, 0.0F),
				posL(0.32F, 0.0F, -0.015F, 0.0F),
				posL(0.34F, 0.0F, 0.006F, 0.0F),
				posL(0.36F, 0.0F, 0.022F, 0.0F),
				posL(0.38F, 0.0F, 0.032F, 0.0F),
				posL(0.4F, 0.0F, 0.036F, 0.0F),
				posL(0.42F, 0.0F, 0.035F, 0.0F),
				posL(0.44F, 0.0F, 0.038F, 0.0F),
				posL(0.46F, 0.0F, 0.034F, 0.0F),
				posL(0.48F, 0.0F, 0.025F, 0.0F),
				posL(0.5F, 0.0F, 0.015F, 0.0F),
				posL(0.52F, 0.0F, 0.004F, 0.0F),
				posL(0.54F, 0.0F, -0.009F, 0.0F),
				posL(0.56F, 0.0F, -0.023F, 0.0F),
				posL(0.58F, 0.0F, -0.039F, 0.0F),
				posL(0.6F, 0.0F, -0.057F, 0.0F),
				posL(0.62F, 0.0F, -0.075F, 0.0F),
				posL(0.64F, 0.0F, -0.096F, 0.0F),
				posL(0.66F, 0.0F, -0.118F, 0.0F),
				posL(0.68F, 0.0F, -0.141F, 0.0F),
				posL(0.7F, 0.0F, -0.166F, 0.0F),
				posL(0.72F, 0.0F, -0.193F, 0.0F),
				posL(0.74F, 0.0F, -0.231F, 0.0F),
				posL(0.76F, 0.0F, -0.272F, 0.0F),
				posL(0.78F, 0.0F, -0.314F, 0.0F),
				posL(0.8F, 0.0F, -0.359F, 0.0F),
				posL(0.82F, 0.0F, -0.405F, 0.0F),
				posL(0.84F, 0.0F, -0.453F, 0.0F),
				posL(0.86F, 0.0F, -0.503F, 0.0F),
				posL(0.88F, 0.0F, -0.555F, 0.0F),
				posL(0.9F, 0.0F, -0.609F, 0.0F),
				posL(0.92F, 0.0F, -0.822F, 0.0F),
				posL(0.94F, 0.0F, -1.053F, 0.0F),
				posL(0.96F, 0.0F, -1.082F, 0.0F),
				posL(0.98F, 0.0F, -0.968F, 0.0F),
				posL(1.0F, 0.0F, -0.857F, 0.0F),
				posL(1.02F, 0.0F, -0.749F, 0.0F),
				posL(1.04F, 0.0F, -0.643F, 0.0F),
				posL(1.06F, 0.0F, -0.54F, 0.0F),
				posL(1.08F, 0.0F, -0.439F, 0.0F),
				posL(1.1F, 0.0F, -0.341F, 0.0F),
				posL(1.12F, 0.0F, -0.246F, 0.0F),
				posL(1.14F, 0.0F, -0.153F, 0.0F),
				posL(1.16F, 0.0F, -0.111F, 0.0F),
				posL(1.18F, 0.0F, -0.073F, 0.0F)
			));
		}
		private static void ch_3_p1(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(1.2F, 0.0F, -0.042F, 0.0F),
				posL(1.22F, 0.0F, -0.015F, 0.0F),
				posL(1.24F, 0.0F, 0.006F, 0.0F),
				posL(1.26F, 0.0F, 0.022F, 0.0F),
				posL(1.28F, 0.0F, 0.032F, 0.0F),
				posL(1.3F, 0.0F, 0.036F, 0.0F),
				posL(1.32F, 0.0F, 0.035F, 0.0F),
				posL(1.34F, 0.0F, 0.038F, 0.0F),
				posL(1.36F, 0.0F, 0.034F, 0.0F),
				posL(1.38F, 0.0F, 0.025F, 0.0F),
				posL(1.4F, 0.0F, 0.015F, 0.0F),
				posL(1.42F, 0.0F, 0.004F, 0.0F),
				posL(1.44F, 0.0F, -0.009F, 0.0F),
				posL(1.46F, 0.0F, -0.023F, 0.0F),
				posL(1.48F, 0.0F, -0.039F, 0.0F),
				posL(1.5F, 0.0F, -0.057F, 0.0F),
				posL(1.52F, 0.0F, -0.075F, 0.0F),
				posL(1.54F, 0.0F, -0.096F, 0.0F),
				posL(1.56F, 0.0F, -0.118F, 0.0F),
				posL(1.58F, 0.0F, -0.141F, 0.0F),
				posL(1.6F, 0.0F, -0.166F, 0.0F),
				posL(1.62F, 0.0F, -0.193F, 0.0F),
				posL(1.64F, 0.0F, -0.231F, 0.0F),
				posL(1.66F, 0.0F, -0.272F, 0.0F),
				posL(1.68F, 0.0F, -0.314F, 0.0F),
				posL(1.7F, 0.0F, -0.359F, 0.0F),
				posL(1.72F, 0.0F, -0.405F, 0.0F),
				posL(1.74F, 0.0F, -0.453F, 0.0F),
				posL(1.76F, 0.0F, -0.503F, 0.0F),
				posL(1.78F, 0.0F, -0.555F, 0.0F),
				posL(1.8F, 0.0F, -0.609F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			List<Keyframe> kfs = new ArrayList<>(91);
			ch_3_p0(kfs);
			ch_3_p1(kfs);
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION, kfs.toArray(new Keyframe[0])));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 16.0F, 0.0F, -1.0F),
				rotL(0.24F, 4.0F, 0.0F, -1.0F),
				rotL(0.48F, -22.0F, 0.0F, -1.0F),
				rotL(0.72F, -28.0F, 0.0F, -1.0F),
				rotL(0.9F, -24.0F, 0.0F, -1.0F),
				rotL(1.14F, -14.0F, 0.0F, -1.0F),
				rotL(1.38F, 0.0F, 0.0F, -1.0F),
				rotL(1.62F, 10.0F, 0.0F, -1.0F),
				rotL(1.8F, 16.0F, 0.0F, -1.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -10.0F, 0.0F, 0.0F),
				rotL(0.24F, 30.0F, 0.0F, 0.0F),
				rotL(0.48F, 40.0F, 0.0F, 0.0F),
				rotL(0.72F, 28.0F, 0.0F, 0.0F),
				rotL(0.9F, 26.0F, 0.0F, 0.0F),
				rotL(1.14F, 12.0F, 0.0F, 0.0F),
				rotL(1.38F, 2.0F, 0.0F, 0.0F),
				rotL(1.62F, -6.0F, 0.0F, 0.0F),
				rotL(1.8F, -10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -8.0F, 0.0F, 0.0F),
				rotL(0.24F, -18.0F, 0.0F, 0.0F),
				rotL(0.48F, -22.0F, 0.0F, 0.0F),
				rotL(0.72F, -10.0F, 0.0F, 0.0F),
				rotL(0.9F, -10.0F, 0.0F, 0.0F),
				rotL(1.14F, -2.0F, 0.0F, 0.0F),
				rotL(1.38F, 0.0F, 0.0F, 0.0F),
				rotL(1.62F, -4.0F, 0.0F, 0.0F),
				rotL(1.8F, -8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 2.0F, 0.0F, 1.0F),
				rotL(0.24F, -26.0F, 0.0F, 1.0F),
				rotL(0.48F, -8.0F, 0.0F, 1.0F),
				rotL(0.72F, 8.0F, 0.0F, 1.0F),
				rotL(0.9F, 8.0F, 0.0F, 1.0F),
				rotL(1.14F, 4.0F, 0.0F, 1.0F),
				rotL(1.38F, -2.0F, 0.0F, 1.0F),
				rotL(1.62F, 0.0F, 0.0F, 1.0F),
				rotL(1.8F, 2.0F, 0.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -24.0F, 0.0F, 1.0F),
				rotL(0.24F, -14.0F, 0.0F, 1.0F),
				rotL(0.48F, 0.0F, 0.0F, 1.0F),
				rotL(0.72F, 10.0F, 0.0F, 1.0F),
				rotL(0.9F, 16.0F, 0.0F, 1.0F),
				rotL(1.14F, 4.0F, 0.0F, 1.0F),
				rotL(1.38F, -22.0F, 0.0F, 1.0F),
				rotL(1.62F, -28.0F, 0.0F, 1.0F),
				rotL(1.8F, -24.0F, 0.0F, 1.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.24F, 12.0F, 0.0F, 0.0F),
				rotL(0.48F, 2.0F, 0.0F, 0.0F),
				rotL(0.72F, -6.0F, 0.0F, 0.0F),
				rotL(0.9F, -10.0F, 0.0F, 0.0F),
				rotL(1.14F, 30.0F, 0.0F, 0.0F),
				rotL(1.38F, 40.0F, 0.0F, 0.0F),
				rotL(1.62F, 28.0F, 0.0F, 0.0F),
				rotL(1.8F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -10.0F, 0.0F, 0.0F),
				rotL(0.24F, -2.0F, 0.0F, 0.0F),
				rotL(0.48F, 0.0F, 0.0F, 0.0F),
				rotL(0.72F, -4.0F, 0.0F, 0.0F),
				rotL(0.9F, -8.0F, 0.0F, 0.0F),
				rotL(1.14F, -18.0F, 0.0F, 0.0F),
				rotL(1.38F, -22.0F, 0.0F, 0.0F),
				rotL(1.62F, -10.0F, 0.0F, 0.0F),
				rotL(1.8F, -10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 8.0F, 0.0F, -1.0F),
				rotL(0.24F, 4.0F, 0.0F, -1.0F),
				rotL(0.48F, -2.0F, 0.0F, -1.0F),
				rotL(0.72F, 0.0F, 0.0F, -1.0F),
				rotL(0.9F, 2.0F, 0.0F, -1.0F),
				rotL(1.14F, -26.0F, 0.0F, -1.0F),
				rotL(1.38F, -8.0F, 0.0F, -1.0F),
				rotL(1.62F, 8.0F, 0.0F, -1.0F),
				rotL(1.8F, 8.0F, 0.0F, -1.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 8.0F, 3.0F, 1.0F),
				rotL(0.24F, 8.0F, -1.8F, 0.75F),
				rotL(0.48F, 8.0F, 0.6F, -0.25F),
				rotL(0.72F, 8.0F, 3.0F, -1.0F),
				rotL(0.9F, 8.0F, 3.0F, -1.0F),
				rotL(1.14F, 8.0F, 1.8F, -0.75F),
				rotL(1.38F, 8.0F, -0.6F, 0.25F),
				rotL(1.62F, 8.0F, -3.0F, 1.0F),
				rotL(1.8F, 8.0F, 3.0F, 1.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, -7.0F, -2.0F),
				rotL(0.24F, 10.0F, -4.2F, -1.5F),
				rotL(0.48F, 10.0F, 1.4F, 0.5F),
				rotL(0.72F, 10.0F, 7.0F, 2.0F),
				rotL(0.9F, 10.0F, 7.0F, 2.0F),
				rotL(1.14F, 10.0F, 4.2F, 1.5F),
				rotL(1.38F, 10.0F, -1.4F, -0.5F),
				rotL(1.62F, 10.0F, -7.0F, -2.0F),
				rotL(1.8F, 10.0F, -7.0F, -2.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -40.0F, 0.0F, -4.0F),
				rotL(0.24F, -34.0F, 0.0F, -4.0F),
				rotL(0.48F, -22.0F, 0.0F, -4.0F),
				rotL(0.72F, -12.0F, 0.0F, -4.0F),
				rotL(0.9F, -14.0F, 0.0F, -4.0F),
				rotL(1.14F, -22.0F, 0.0F, -4.0F),
				rotL(1.38F, -32.0F, 0.0F, -4.0F),
				rotL(1.62F, -42.0F, 0.0F, -4.0F),
				rotL(1.8F, -40.0F, 0.0F, -4.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -20.0F, 0.0F, 0.0F),
				rotL(0.24F, -17.7F, 0.0F, 0.0F),
				rotL(0.48F, -13.5F, 0.0F, 0.0F),
				rotL(0.72F, -10.0F, 0.0F, 0.0F),
				rotL(0.9F, -10.7F, 0.0F, 0.0F),
				rotL(1.14F, -13.5F, 0.0F, 0.0F),
				rotL(1.38F, -17.0F, 0.0F, 0.0F),
				rotL(1.62F, -20.5F, 0.0F, 0.0F),
				rotL(1.8F, -20.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 8.0F, 0.0F, 14.0F),
				rotL(0.24F, 4.0F, 0.0F, 16.0F),
				rotL(0.48F, 4.0F, 0.0F, -16.0F),
				rotL(0.72F, 4.0F, 0.0F, 12.0F),
				rotL(0.9F, 4.0F, 0.0F, -12.0F),
				rotL(1.14F, 4.0F, 0.0F, -14.0F),
				rotL(1.38F, 4.0F, 0.0F, 10.0F),
				rotL(1.62F, 4.0F, 0.0F, -18.0F),
				rotL(1.8F, 8.0F, 0.0F, 14.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 0.0F, 4.0F),
				rotL(0.24F, -22.0F, 0.0F, 4.0F),
				rotL(0.48F, -32.0F, 0.0F, 4.0F),
				rotL(0.72F, -42.0F, 0.0F, 4.0F),
				rotL(0.9F, -40.0F, 0.0F, 4.0F),
				rotL(1.14F, -34.0F, 0.0F, 4.0F),
				rotL(1.38F, -22.0F, 0.0F, 4.0F),
				rotL(1.62F, -12.0F, 0.0F, 4.0F),
				rotL(1.8F, -14.0F, 0.0F, 4.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -12.0F, 0.0F, 0.0F),
				rotL(0.24F, -13.5F, 0.0F, 0.0F),
				rotL(0.48F, -17.0F, 0.0F, 0.0F),
				rotL(0.72F, -20.5F, 0.0F, 0.0F),
				rotL(0.9F, -19.8F, 0.0F, 0.0F),
				rotL(1.14F, -17.7F, 0.0F, 0.0F),
				rotL(1.38F, -13.5F, 0.0F, 0.0F),
				rotL(1.62F, -10.0F, 0.0F, 0.0F),
				rotL(1.8F, -12.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -6.0F, 0.0F, 12.0F),
				rotL(0.24F, -4.0F, 0.0F, -14.0F),
				rotL(0.48F, -4.0F, 0.0F, 10.0F),
				rotL(0.72F, -4.0F, 0.0F, -18.0F),
				rotL(0.9F, -4.0F, 0.0F, 14.0F),
				rotL(1.14F, -4.0F, 0.0F, 16.0F),
				rotL(1.38F, -4.0F, 0.0F, -16.0F),
				rotL(1.62F, -4.0F, 0.0F, 12.0F),
				rotL(1.8F, -6.0F, 0.0F, 12.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, 0.0F, 0.0F),
				rotL(0.24F, 10.0F, 0.0F, 0.0F),
				rotL(0.48F, 10.0F, 0.0F, 0.0F),
				rotL(0.72F, 10.0F, 0.0F, 0.0F),
				rotL(0.9F, 10.0F, 0.0F, 0.0F),
				rotL(1.14F, 10.0F, 0.0F, 0.0F),
				rotL(1.38F, 10.0F, 0.0F, 0.0F),
				rotL(1.62F, 10.0F, 0.0F, 0.0F),
				rotL(1.8F, 10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 4.0F, 0.0F, 0.0F),
				rotL(0.24F, 4.0F, 0.0F, 0.0F),
				rotL(0.48F, 4.0F, 0.0F, 0.0F),
				rotL(0.72F, 4.0F, 0.0F, 0.0F),
				rotL(0.9F, 4.0F, 0.0F, 0.0F),
				rotL(1.14F, 4.0F, 0.0F, 0.0F),
				rotL(1.38F, 4.0F, 0.0F, 0.0F),
				rotL(1.62F, 4.0F, 0.0F, 0.0F),
				rotL(1.8F, 4.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -36.0F, -4.0F, 16.0F),
				rotL(0.24F, -37.0F, -2.4F, 18.0F),
				rotL(0.48F, -38.0F, 0.8F, 12.0F),
				rotL(0.72F, -35.0F, 4.0F, -20.0F),
				rotL(0.9F, -36.0F, 4.0F, -18.0F),
				rotL(1.14F, -37.0F, 2.4F, -16.0F),
				rotL(1.38F, -38.0F, -0.8F, -10.0F),
				rotL(1.62F, -35.0F, -4.0F, 20.0F),
				rotL(1.8F, -36.0F, -4.0F, 16.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 8.0F, 0.0F, 0.0F),
				rotL(0.24F, 14.0F, 0.0F, 0.0F),
				rotL(0.48F, 4.0F, 0.0F, 0.0F),
				rotL(0.72F, 18.0F, 0.0F, 0.0F),
				rotL(0.9F, 6.0F, 0.0F, 0.0F),
				rotL(1.14F, 12.0F, 0.0F, 0.0F),
				rotL(1.38F, 4.0F, 0.0F, 0.0F),
				rotL(1.62F, 16.0F, 0.0F, 0.0F),
				rotL(1.8F, 8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.8F).looping();
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

	private static final class WalkStartAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 2.0F, -2.0F, 1.0F),
				rotL(0.48F, 8.0F, 3.0F, -1.0F),
				rotL(0.75F, 7.0F, 5.0F, -2.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.02F, 0.0F, 0.037F, 0.0F),
				posL(0.04F, 0.0F, 0.068F, 0.0F),
				posL(0.06F, 0.0F, 0.098F, 0.0F),
				posL(0.08F, 0.0F, 0.127F, 0.0F),
				posL(0.1F, 0.0F, 0.155F, 0.0F),
				posL(0.12F, 0.0F, 0.182F, 0.0F),
				posL(0.14F, 0.0F, 0.208F, 0.0F),
				posL(0.16F, 0.0F, 0.233F, 0.0F),
				posL(0.18F, 0.0F, 0.257F, 0.0F),
				posL(0.2F, 0.0F, 0.28F, 0.0F),
				posL(0.22F, 0.0F, 0.302F, 0.0F),
				posL(0.24F, 0.0F, 0.288F, 0.0F),
				posL(0.26F, 0.0F, 0.271F, 0.0F),
				posL(0.28F, 0.0F, 0.252F, 0.0F),
				posL(0.3F, 0.0F, 0.229F, 0.0F),
				posL(0.32F, 0.0F, 0.203F, 0.0F),
				posL(0.34F, 0.0F, 0.175F, 0.0F),
				posL(0.36F, 0.0F, 0.147F, 0.0F),
				posL(0.38F, 0.0F, 0.12F, 0.0F),
				posL(0.4F, 0.0F, 0.09F, 0.0F),
				posL(0.42F, 0.0F, 0.057F, 0.0F),
				posL(0.44F, 0.0F, 0.021F, 0.0F),
				posL(0.46F, 0.0F, -0.018F, 0.0F),
				posL(0.48F, 0.0F, -0.061F, 0.0F),
				posL(0.5F, 0.0F, -0.095F, 0.0F),
				posL(0.52F, 0.0F, -0.131F, 0.0F),
				posL(0.54F, 0.0F, -0.168F, 0.0F),
				posL(0.56F, 0.0F, -0.205F, 0.0F),
				posL(0.58F, 0.0F, -0.244F, 0.0F),
				posL(0.6F, 0.0F, -0.283F, 0.0F),
				posL(0.62F, 0.0F, -0.323F, 0.0F),
				posL(0.64F, 0.0F, -0.365F, 0.0F),
				posL(0.66F, 0.0F, -0.407F, 0.0F),
				posL(0.68F, 0.0F, -0.45F, 0.0F),
				posL(0.7F, 0.0F, -0.495F, 0.0F),
				posL(0.72F, 0.0F, -0.54F, 0.0F),
				posL(0.75F, 0.0F, -0.609F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 4.0F, 0.0F, -1.0F),
				rotL(0.48F, 8.0F, 0.0F, -1.0F),
				rotL(0.75F, 16.0F, 0.0F, -1.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -4.0F, 0.0F, 0.0F),
				rotL(0.48F, -6.0F, 0.0F, 0.0F),
				rotL(0.75F, -10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, -4.0F, 0.0F, 0.0F),
				rotL(0.75F, -8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 0.0F, 0.0F, 1.0F),
				rotL(0.48F, 2.0F, 0.0F, 1.0F),
				rotL(0.75F, 2.0F, 0.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -6.0F, 0.0F, 1.0F),
				rotL(0.48F, -22.0F, 0.0F, 1.0F),
				rotL(0.75F, -24.0F, 0.0F, 1.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 14.0F, 0.0F, 0.0F),
				rotL(0.48F, 38.0F, 0.0F, 0.0F),
				rotL(0.75F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -8.0F, 0.0F, 0.0F),
				rotL(0.48F, -20.0F, 0.0F, 0.0F),
				rotL(0.75F, -10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 0.0F, 0.0F, -1.0F),
				rotL(0.48F, -4.0F, 0.0F, -1.0F),
				rotL(0.75F, 8.0F, 0.0F, -1.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 5.0F, 0.0F, 2.0F),
				rotL(0.48F, 9.0F, 2.0F, 1.0F),
				rotL(0.75F, 8.0F, 3.0F, 1.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 7.0F, 4.0F, -3.0F),
				rotL(0.48F, 12.0F, -5.0F, -2.0F),
				rotL(0.75F, 10.0F, -7.0F, -2.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -16.0F, 0.0F, -6.0F),
				rotL(0.48F, -34.0F, 0.0F, -5.0F),
				rotL(0.75F, -40.0F, 0.0F, -4.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -18.0F, 0.0F, 0.0F),
				rotL(0.48F, -22.0F, 0.0F, 0.0F),
				rotL(0.75F, -20.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 0.0F, 0.0F, 18.0F),
				rotL(0.48F, 6.0F, 0.0F, -16.0F),
				rotL(0.75F, 8.0F, 0.0F, 14.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -12.0F, 0.0F, 6.0F),
				rotL(0.48F, -24.0F, 0.0F, 5.0F),
				rotL(0.75F, -14.0F, 0.0F, 4.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -18.0F, 0.0F, 0.0F),
				rotL(0.48F, -14.0F, 0.0F, 0.0F),
				rotL(0.75F, -12.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 0.0F, 0.0F, -18.0F),
				rotL(0.48F, -4.0F, 0.0F, 14.0F),
				rotL(0.75F, -6.0F, 0.0F, 12.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 6.0F, 0.0F, 0.0F),
				rotL(0.48F, 12.0F, 0.0F, 0.0F),
				rotL(0.75F, 10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 4.0F, 0.0F, 0.0F),
				rotL(0.75F, 4.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -18.0F, 14.0F, -26.0F),
				rotL(0.48F, -38.0F, -8.0F, 20.0F),
				rotL(0.75F, -36.0F, -4.0F, 16.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 22.0F, 0.0F, 0.0F),
				rotL(0.48F, -2.0F, 0.0F, 0.0F),
				rotL(0.75F, 8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(0.75F);
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

	private static final class WalkStopAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 7.0F, 5.0F, -2.0F),
				rotL(0.3F, 4.0F, 1.0F, 0.0F),
				rotL(0.58F, -1.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, -0.609F, 0.0F),
				posL(0.02F, 0.0F, -0.539F, 0.0F),
				posL(0.04F, 0.0F, -0.476F, 0.0F),
				posL(0.06F, 0.0F, -0.423F, 0.0F),
				posL(0.08F, 0.0F, -0.377F, 0.0F),
				posL(0.1F, 0.0F, -0.34F, 0.0F),
				posL(0.12F, 0.0F, -0.312F, 0.0F),
				posL(0.14F, 0.0F, -0.292F, 0.0F),
				posL(0.16F, 0.0F, -0.28F, 0.0F),
				posL(0.18F, 0.0F, -0.277F, 0.0F),
				posL(0.2F, 0.0F, -0.282F, 0.0F),
				posL(0.22F, 0.0F, -0.296F, 0.0F),
				posL(0.24F, 0.0F, -0.317F, 0.0F),
				posL(0.26F, 0.0F, -0.347F, 0.0F),
				posL(0.28F, 0.0F, -0.385F, 0.0F),
				posL(0.3F, 0.0F, -0.431F, 0.0F),
				posL(0.32F, 0.0F, -0.416F, 0.0F),
				posL(0.34F, 0.0F, -0.401F, 0.0F),
				posL(0.36F, 0.0F, -0.388F, 0.0F),
				posL(0.38F, 0.0F, -0.375F, 0.0F),
				posL(0.4F, 0.0F, -0.364F, 0.0F),
				posL(0.42F, 0.0F, -0.353F, 0.0F),
				posL(0.44F, 0.0F, -0.343F, 0.0F),
				posL(0.46F, 0.0F, -0.335F, 0.0F),
				posL(0.48F, 0.0F, -0.327F, 0.0F),
				posL(0.5F, 0.0F, -0.32F, 0.0F),
				posL(0.52F, 0.0F, -0.315F, 0.0F),
				posL(0.54F, 0.0F, -0.29F, 0.0F),
				posL(0.56F, 0.0F, -0.261F, 0.0F),
				posL(0.58F, 0.0F, -0.233F, 0.0F),
				posL(0.6F, 0.0F, -0.21F, 0.0F),
				posL(0.62F, 0.0F, -0.188F, 0.0F),
				posL(0.64F, 0.0F, -0.165F, 0.0F),
				posL(0.66F, 0.0F, -0.143F, 0.0F),
				posL(0.68F, 0.0F, -0.121F, 0.0F),
				posL(0.7F, 0.0F, -0.1F, 0.0F),
				posL(0.72F, 0.0F, -0.078F, 0.0F),
				posL(0.74F, 0.0F, -0.057F, 0.0F),
				posL(0.76F, 0.0F, -0.036F, 0.0F),
				posL(0.78F, 0.0F, -0.015F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 16.0F, 0.0F, -1.0F),
				rotL(0.3F, -6.0F, 0.0F, -1.0F),
				rotL(0.58F, -2.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -10.0F, 0.0F, 0.0F),
				rotL(0.3F, 10.0F, 0.0F, 0.0F),
				rotL(0.58F, 4.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -8.0F, 0.0F, 0.0F),
				rotL(0.3F, -4.0F, 0.0F, 0.0F),
				rotL(0.58F, -2.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 2.0F, 0.0F, 1.0F),
				rotL(0.3F, 0.0F, 0.0F, 1.0F),
				rotL(0.58F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -24.0F, 0.0F, 1.0F),
				rotL(0.3F, -8.0F, 0.0F, 1.0F),
				rotL(0.58F, -2.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.3F, 12.0F, 0.0F, 0.0F),
				rotL(0.58F, 4.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -10.0F, 0.0F, 0.0F),
				rotL(0.3F, -4.0F, 0.0F, 0.0F),
				rotL(0.58F, -2.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 8.0F, 0.0F, -1.0F),
				rotL(0.3F, 0.0F, 0.0F, -1.0F),
				rotL(0.58F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 8.0F, 3.0F, 1.0F),
				rotL(0.3F, 5.0F, 0.0F, 0.0F),
				rotL(0.58F, -2.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, -7.0F, -2.0F),
				rotL(0.3F, 9.0F, -2.0F, -1.0F),
				rotL(0.58F, -2.0F, 2.0F, 1.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -40.0F, 0.0F, -4.0F),
				rotL(0.3F, -20.0F, 0.0F, -3.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -20.0F, 0.0F, 0.0F),
				rotL(0.3F, -12.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 8.0F, 0.0F, 14.0F),
				rotL(0.58F, 0.0F, 0.0F, 12.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 0.0F, 4.0F),
				rotL(0.3F, -18.0F, 0.0F, 3.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -12.0F, 0.0F, 0.0F),
				rotL(0.3F, -10.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -6.0F, 0.0F, 12.0F),
				rotL(0.58F, 0.0F, 0.0F, -12.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, 0.0F, 0.0F),
				rotL(0.3F, 12.0F, 0.0F, 0.0F),
				rotL(0.58F, -2.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 4.0F, 0.0F, 0.0F),
				rotL(0.3F, 6.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -36.0F, -4.0F, 16.0F),
				rotL(0.3F, -10.0F, 8.0F, -18.0F),
				rotL(0.58F, -4.0F, -8.0F, 14.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 8.0F, 0.0F, 0.0F),
				rotL(0.3F, 14.0F, 0.0F, 0.0F),
				rotL(0.58F, -2.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(0.8F);
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

	private static final class RunAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 16.0F, 6.0F, -3.0F),
				rotL(0.16F, 14.0F, 2.0F, -1.0F),
				rotL(0.3F, 17.0F, -3.0F, 1.0F),
				rotL(0.44F, 16.0F, -6.0F, 3.0F),
				rotL(0.6F, 14.0F, -2.0F, 1.0F),
				rotL(0.74F, 17.0F, 3.0F, -1.0F),
				rotL(0.88F, 16.0F, 6.0F, -3.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, -2.237F, 0.0F),
				posL(0.02F, 0.0F, -1.824F, 0.0F),
				posL(0.04F, 0.0F, -1.428F, 0.0F),
				posL(0.06F, 0.0F, -1.049F, 0.0F),
				posL(0.08F, 0.0F, -0.687F, 0.0F),
				posL(0.1F, 0.0F, 0.149F, 0.0F),
				posL(0.12F, 0.0F, 0.932F, 0.0F),
				posL(0.14F, 0.0F, 1.633F, 0.0F),
				posL(0.16F, 0.0F, 2.222F, 0.0F),
				posL(0.18F, 0.0F, 2.39F, 0.0F),
				posL(0.2F, 0.0F, 2.365F, 0.0F),
				posL(0.22F, 0.0F, 2.142F, 0.0F),
				posL(0.24F, 0.0F, 1.732F, 0.0F),
				posL(0.26F, 0.0F, 1.15F, 0.0F),
				posL(0.28F, 0.0F, 0.406F, 0.0F),
				posL(0.3F, 0.0F, -0.469F, 0.0F),
				posL(0.32F, 0.0F, -1.127F, 0.0F),
				posL(0.34F, 0.0F, -1.303F, 0.0F),
				posL(0.36F, 0.0F, -1.492F, 0.0F),
				posL(0.38F, 0.0F, -1.692F, 0.0F),
				posL(0.4F, 0.0F, -1.905F, 0.0F),
				posL(0.42F, 0.0F, -2.128F, 0.0F),
				posL(0.44F, 0.0F, -2.237F, 0.0F),
				posL(0.46F, 0.0F, -1.824F, 0.0F),
				posL(0.48F, 0.0F, -1.428F, 0.0F),
				posL(0.5F, 0.0F, -1.049F, 0.0F),
				posL(0.52F, 0.0F, -0.687F, 0.0F),
				posL(0.54F, 0.0F, 0.149F, 0.0F),
				posL(0.56F, 0.0F, 0.932F, 0.0F),
				posL(0.58F, 0.0F, 1.633F, 0.0F),
				posL(0.6F, 0.0F, 2.222F, 0.0F),
				posL(0.62F, 0.0F, 2.39F, 0.0F),
				posL(0.64F, 0.0F, 2.365F, 0.0F),
				posL(0.66F, 0.0F, 2.142F, 0.0F),
				posL(0.68F, 0.0F, 1.732F, 0.0F),
				posL(0.7F, 0.0F, 1.15F, 0.0F),
				posL(0.72F, 0.0F, 0.406F, 0.0F),
				posL(0.74F, 0.0F, -0.469F, 0.0F),
				posL(0.76F, 0.0F, -1.127F, 0.0F),
				posL(0.78F, 0.0F, -1.303F, 0.0F),
				posL(0.8F, 0.0F, -1.492F, 0.0F),
				posL(0.82F, 0.0F, -1.692F, 0.0F),
				posL(0.84F, 0.0F, -1.905F, 0.0F),
				posL(0.86F, 0.0F, -2.128F, 0.0F),
				posL(0.88F, 0.0F, -2.237F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, -2.0F),
				rotL(0.16F, 12.0F, 0.0F, -2.0F),
				rotL(0.3F, -22.0F, 0.0F, -2.0F),
				rotL(0.44F, -34.0F, 0.0F, -2.0F),
				rotL(0.6F, -18.0F, 0.0F, -2.0F),
				rotL(0.74F, 10.0F, 0.0F, -2.0F),
				rotL(0.88F, 24.0F, 0.0F, -2.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 0.0F, 0.0F),
				rotL(0.16F, 26.0F, 0.0F, 0.0F),
				rotL(0.3F, 40.0F, 0.0F, 0.0F),
				rotL(0.44F, 36.0F, 0.0F, 0.0F),
				rotL(0.6F, 8.0F, 0.0F, 0.0F),
				rotL(0.74F, -6.0F, 0.0F, 0.0F),
				rotL(0.88F, -14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -12.0F, 0.0F, 0.0F),
				rotL(0.16F, -18.0F, 0.0F, 0.0F),
				rotL(0.3F, -20.0F, 0.0F, 0.0F),
				rotL(0.44F, -16.0F, 0.0F, 0.0F),
				rotL(0.6F, 8.0F, 0.0F, 0.0F),
				rotL(0.74F, -6.0F, 0.0F, 0.0F),
				rotL(0.88F, -12.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 6.0F, 0.0F, 2.0F),
				rotL(0.16F, -30.0F, 0.0F, 2.0F),
				rotL(0.3F, -10.0F, 0.0F, 2.0F),
				rotL(0.44F, 6.0F, 0.0F, 2.0F),
				rotL(0.6F, 2.0F, 0.0F, 2.0F),
				rotL(0.74F, 2.0F, 0.0F, 2.0F),
				rotL(0.88F, 6.0F, 0.0F, 2.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -34.0F, 0.0F, 2.0F),
				rotL(0.16F, -18.0F, 0.0F, 2.0F),
				rotL(0.3F, 10.0F, 0.0F, 2.0F),
				rotL(0.44F, 24.0F, 0.0F, 2.0F),
				rotL(0.6F, 12.0F, 0.0F, 2.0F),
				rotL(0.74F, -22.0F, 0.0F, 2.0F),
				rotL(0.88F, -34.0F, 0.0F, 2.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 36.0F, 0.0F, 0.0F),
				rotL(0.16F, 8.0F, 0.0F, 0.0F),
				rotL(0.3F, -6.0F, 0.0F, 0.0F),
				rotL(0.44F, -14.0F, 0.0F, 0.0F),
				rotL(0.6F, 26.0F, 0.0F, 0.0F),
				rotL(0.74F, 40.0F, 0.0F, 0.0F),
				rotL(0.88F, 36.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -16.0F, 0.0F, 0.0F),
				rotL(0.16F, 8.0F, 0.0F, 0.0F),
				rotL(0.3F, -6.0F, 0.0F, 0.0F),
				rotL(0.44F, -12.0F, 0.0F, 0.0F),
				rotL(0.6F, -18.0F, 0.0F, 0.0F),
				rotL(0.74F, -20.0F, 0.0F, 0.0F),
				rotL(0.88F, -16.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 6.0F, 0.0F, -2.0F),
				rotL(0.16F, 2.0F, 0.0F, -2.0F),
				rotL(0.3F, 2.0F, 0.0F, -2.0F),
				rotL(0.44F, 6.0F, 0.0F, -2.0F),
				rotL(0.6F, -30.0F, 0.0F, -2.0F),
				rotL(0.74F, -10.0F, 0.0F, -2.0F),
				rotL(0.88F, 6.0F, 0.0F, -2.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, 4.0F, 0.0F),
				rotL(0.16F, 10.0F, -1.2F, 0.0F),
				rotL(0.3F, 10.0F, 1.8F, 0.0F),
				rotL(0.44F, 10.0F, 3.6F, 0.0F),
				rotL(0.6F, 10.0F, 1.2F, 0.0F),
				rotL(0.74F, 10.0F, -1.8F, 0.0F),
				rotL(0.88F, 10.0F, 4.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 12.0F, -8.0F, -2.0F),
				rotL(0.16F, 12.0F, -2.6F, -0.6F),
				rotL(0.3F, 12.0F, 3.9F, 0.6F),
				rotL(0.44F, 12.0F, 7.8F, 1.8F),
				rotL(0.6F, 12.0F, 2.6F, 0.6F),
				rotL(0.74F, 12.0F, -3.9F, -0.6F),
				rotL(0.88F, 12.0F, -8.0F, -2.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -18.0F, -6.0F, -10.0F),
				rotL(0.16F, -54.0F, -6.0F, 8.0F),
				rotL(0.3F, -72.0F, -6.0F, 8.0F),
				rotL(0.44F, -68.0F, -6.0F, 8.0F),
				rotL(0.6F, -40.0F, -6.0F, 8.0F),
				rotL(0.74F, -12.0F, -6.0F, 8.0F),
				rotL(0.88F, -18.0F, -6.0F, -10.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -32.0F, 0.0F, 0.0F),
				rotL(0.16F, -22.0F, 0.0F, 0.0F),
				rotL(0.3F, -20.0F, 0.0F, 0.0F),
				rotL(0.44F, -24.0F, 0.0F, 0.0F),
				rotL(0.6F, -32.0F, 0.0F, 0.0F),
				rotL(0.74F, -34.0F, 0.0F, 0.0F),
				rotL(0.88F, -32.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -12.0F, 0.0F, 16.0F),
				rotL(0.16F, 10.0F, 0.0F, -18.0F),
				rotL(0.3F, 10.0F, 0.0F, -20.0F),
				rotL(0.44F, 10.0F, 0.0F, 16.0F),
				rotL(0.6F, 10.0F, 0.0F, -16.0F),
				rotL(0.74F, 10.0F, 0.0F, 18.0F),
				rotL(0.88F, -12.0F, 0.0F, 16.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -68.0F, 8.0F, -8.0F),
				rotL(0.16F, -40.0F, 6.0F, -8.0F),
				rotL(0.3F, -12.0F, 6.0F, -8.0F),
				rotL(0.44F, -18.0F, 6.0F, -8.0F),
				rotL(0.6F, -54.0F, 6.0F, -8.0F),
				rotL(0.74F, -72.0F, 6.0F, -8.0F),
				rotL(0.88F, -68.0F, 8.0F, -8.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -24.0F, 0.0F, 0.0F),
				rotL(0.16F, -32.0F, 0.0F, 0.0F),
				rotL(0.3F, -34.0F, 0.0F, 0.0F),
				rotL(0.44F, -32.0F, 0.0F, 0.0F),
				rotL(0.6F, -22.0F, 0.0F, 0.0F),
				rotL(0.74F, -20.0F, 0.0F, 0.0F),
				rotL(0.88F, -24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 12.0F, 0.0F, -16.0F),
				rotL(0.16F, 10.0F, 0.0F, 16.0F),
				rotL(0.3F, 10.0F, 0.0F, 18.0F),
				rotL(0.44F, 10.0F, 0.0F, -16.0F),
				rotL(0.6F, 10.0F, 0.0F, 18.0F),
				rotL(0.74F, 10.0F, 0.0F, -20.0F),
				rotL(0.88F, 12.0F, 0.0F, -16.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 8.0F, 0.0F, 0.0F),
				rotL(0.16F, 8.0F, 0.0F, 0.0F),
				rotL(0.3F, 8.0F, 0.0F, 0.0F),
				rotL(0.44F, 8.0F, 0.0F, 0.0F),
				rotL(0.6F, 8.0F, 0.0F, 0.0F),
				rotL(0.74F, 8.0F, 0.0F, 0.0F),
				rotL(0.88F, 8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -4.0F, 0.0F, 0.0F),
				rotL(0.16F, -4.0F, 0.0F, 0.0F),
				rotL(0.3F, -4.0F, 0.0F, 0.0F),
				rotL(0.44F, -4.0F, 0.0F, 0.0F),
				rotL(0.6F, -4.0F, 0.0F, 0.0F),
				rotL(0.74F, -4.0F, 0.0F, 0.0F),
				rotL(0.88F, -4.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -44.0F, -4.0F, 12.0F),
				rotL(0.16F, -44.0F, -1.2F, 8.0F),
				rotL(0.3F, -44.0F, 1.8F, -8.0F),
				rotL(0.44F, -44.0F, 3.6F, -14.0F),
				rotL(0.6F, -44.0F, 1.2F, -6.0F),
				rotL(0.74F, -44.0F, -1.8F, 10.0F),
				rotL(0.88F, -44.0F, -4.0F, 12.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, 0.0F),
				rotL(0.16F, 10.0F, 0.0F, 0.0F),
				rotL(0.3F, 28.0F, 0.0F, 0.0F),
				rotL(0.44F, 22.0F, 0.0F, 0.0F),
				rotL(0.6F, 8.0F, 0.0F, 0.0F),
				rotL(0.74F, 30.0F, 0.0F, 0.0F),
				rotL(0.88F, 24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(0.88F).looping();
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

	private static final class RunStartAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, -4.0F, -6.0F, 3.0F),
				rotL(0.46F, 18.0F, 4.0F, -2.0F),
				rotL(0.7F, 16.0F, 6.0F, -3.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.02F, 0.0F, -0.122F, 0.08F),
				posL(0.04F, 0.0F, -0.265F, 0.16F),
				posL(0.06F, 0.0F, -0.417F, 0.24F),
				posL(0.08F, 0.0F, -0.578F, 0.32F),
				posL(0.1F, 0.0F, -0.749F, 0.4F),
				posL(0.12F, 0.0F, -0.929F, 0.48F),
				posL(0.14F, 0.0F, -1.119F, 0.56F),
				posL(0.16F, 0.0F, -1.317F, 0.64F),
				posL(0.18F, 0.0F, -1.524F, 0.72F),
				posL(0.2F, 0.0F, -1.74F, 0.8F),
				posL(0.22F, 0.0F, -1.554F, 0.768F),
				posL(0.24F, 0.0F, -1.409F, 0.736F),
				posL(0.26F, 0.0F, -1.225F, 0.704F),
				posL(0.28F, 0.0F, -1.054F, 0.672F),
				posL(0.3F, 0.0F, -0.929F, 0.64F),
				posL(0.32F, 0.0F, -0.85F, 0.608F),
				posL(0.34F, 0.0F, -0.819F, 0.576F),
				posL(0.36F, 0.0F, -0.833F, 0.544F),
				posL(0.38F, 0.0F, -0.88F, 0.512F),
				posL(0.4F, 0.0F, -0.977F, 0.48F),
				posL(0.42F, 0.0F, -1.123F, 0.448F),
				posL(0.44F, 0.0F, -1.32F, 0.416F),
				posL(0.46F, 0.0F, -1.567F, 0.384F),
				posL(0.48F, 0.0F, -1.626F, 0.352F),
				posL(0.5F, 0.0F, -1.685F, 0.32F),
				posL(0.52F, 0.0F, -1.747F, 0.288F),
				posL(0.54F, 0.0F, -1.809F, 0.256F),
				posL(0.56F, 0.0F, -1.874F, 0.224F),
				posL(0.58F, 0.0F, -1.939F, 0.192F),
				posL(0.6F, 0.0F, -2.006F, 0.16F),
				posL(0.62F, 0.0F, -2.075F, 0.128F),
				posL(0.64F, 0.0F, -2.145F, 0.096F),
				posL(0.66F, 0.0F, -2.216F, 0.064F),
				posL(0.68F, 0.0F, -2.289F, 0.032F),
				posL(0.7F, 0.0F, -2.237F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, -12.0F, 0.0F, -3.0F),
				rotL(0.46F, 14.0F, 0.0F, -2.0F),
				rotL(0.7F, 24.0F, 0.0F, -2.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 26.0F, 0.0F, 0.0F),
				rotL(0.46F, -8.0F, 0.0F, 0.0F),
				rotL(0.7F, -14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, -14.0F, 0.0F, 0.0F),
				rotL(0.46F, -8.0F, 0.0F, 0.0F),
				rotL(0.7F, -12.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, 0.0F, 3.0F),
				rotL(0.46F, 2.0F, 0.0F, 2.0F),
				rotL(0.7F, 6.0F, 0.0F, 2.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, -16.0F, 0.0F, 3.0F),
				rotL(0.46F, -28.0F, 0.0F, 2.0F),
				rotL(0.7F, -34.0F, 0.0F, 2.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 32.0F, 0.0F, 0.0F),
				rotL(0.46F, 42.0F, 0.0F, 0.0F),
				rotL(0.7F, 36.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, -16.0F, 0.0F, 0.0F),
				rotL(0.46F, -22.0F, 0.0F, 0.0F),
				rotL(0.7F, -16.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, 0.0F, -3.0F),
				rotL(0.46F, -2.0F, 0.0F, -2.0F),
				rotL(0.7F, 6.0F, 0.0F, -2.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, -4.0F, 0.0F, 0.0F),
				rotL(0.46F, 12.0F, 2.0F, 0.0F),
				rotL(0.7F, 10.0F, 4.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, -6.0F, 8.0F, -3.0F),
				rotL(0.46F, 14.0F, -6.0F, -2.0F),
				rotL(0.7F, 12.0F, -8.0F, -2.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 18.0F, -12.0F, -28.0F),
				rotL(0.46F, -36.0F, -8.0F, -8.0F),
				rotL(0.7F, -18.0F, -6.0F, -10.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, -30.0F, 0.0F, 0.0F),
				rotL(0.46F, -28.0F, 0.0F, 0.0F),
				rotL(0.7F, -32.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 16.0F, 0.0F, -22.0F),
				rotL(0.7F, -12.0F, 0.0F, 16.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 18.0F, 12.0F, 28.0F),
				rotL(0.46F, -74.0F, 10.0F, -8.0F),
				rotL(0.7F, -68.0F, 8.0F, -8.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, -30.0F, 0.0F, 0.0F),
				rotL(0.46F, -20.0F, 0.0F, 0.0F),
				rotL(0.7F, -24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 16.0F, 0.0F, 22.0F),
				rotL(0.46F, 14.0F, 0.0F, -20.0F),
				rotL(0.7F, 12.0F, 0.0F, -16.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, -4.0F, 0.0F, 0.0F),
				rotL(0.46F, 10.0F, 0.0F, 0.0F),
				rotL(0.7F, 8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.46F, -4.0F, 0.0F, 0.0F),
				rotL(0.7F, -4.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, -12.0F, 16.0F, -22.0F),
				rotL(0.46F, -48.0F, -6.0F, 16.0F),
				rotL(0.7F, -44.0F, -4.0F, 12.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 36.0F, 0.0F, 0.0F),
				rotL(0.46F, 28.0F, 0.0F, 0.0F),
				rotL(0.7F, 24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(0.7F);
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

	private static final class RunStopAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 16.0F, 6.0F, -3.0F),
				rotL(0.26F, 12.0F, -8.0F, 3.0F),
				rotL(0.58F, -3.0F, 3.0F, -1.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, -2.237F, 0.0F),
				posL(0.02F, 0.0F, -2.149F, -0.138F),
				posL(0.04F, 0.0F, -1.973F, -0.277F),
				posL(0.06F, 0.0F, -1.836F, -0.415F),
				posL(0.08F, 0.0F, -1.738F, -0.554F),
				posL(0.1F, 0.0F, -1.678F, -0.692F),
				posL(0.12F, 0.0F, -1.657F, -0.831F),
				posL(0.14F, 0.0F, -1.663F, -0.969F),
				posL(0.16F, 0.0F, -1.695F, -1.108F),
				posL(0.18F, 0.0F, -1.764F, -1.246F),
				posL(0.2F, 0.0F, -1.871F, -1.385F),
				posL(0.22F, 0.0F, -2.014F, -1.523F),
				posL(0.24F, 0.0F, -2.192F, -1.662F),
				posL(0.26F, 0.0F, -2.405F, -1.8F),
				posL(0.28F, 0.0F, -2.215F, -1.739F),
				posL(0.3F, 0.0F, -2.034F, -1.678F),
				posL(0.32F, 0.0F, -1.863F, -1.617F),
				posL(0.34F, 0.0F, -1.701F, -1.556F),
				posL(0.36F, 0.0F, -1.548F, -1.495F),
				posL(0.38F, 0.0F, -1.405F, -1.434F),
				posL(0.4F, 0.0F, -1.272F, -1.373F),
				posL(0.42F, 0.0F, -1.149F, -1.312F),
				posL(0.44F, 0.0F, -1.036F, -1.251F),
				posL(0.46F, 0.0F, -0.933F, -1.19F),
				posL(0.48F, 0.0F, -0.841F, -1.129F),
				posL(0.5F, 0.0F, -0.759F, -1.068F),
				posL(0.52F, 0.0F, -0.661F, -1.007F),
				posL(0.54F, 0.0F, -0.522F, -0.946F),
				posL(0.56F, 0.0F, -0.395F, -0.885F),
				posL(0.58F, 0.0F, -0.278F, -0.824F),
				posL(0.6F, 0.0F, -0.252F, -0.763F),
				posL(0.62F, 0.0F, -0.228F, -0.702F),
				posL(0.64F, 0.0F, -0.203F, -0.641F),
				posL(0.66F, 0.0F, -0.18F, -0.58F),
				posL(0.68F, 0.0F, -0.157F, -0.519F),
				posL(0.7F, 0.0F, -0.135F, -0.458F),
				posL(0.72F, 0.0F, -0.114F, -0.397F),
				posL(0.74F, 0.0F, -0.093F, -0.336F),
				posL(0.76F, 0.0F, -0.073F, -0.275F),
				posL(0.78F, 0.0F, -0.053F, -0.214F),
				posL(0.8F, 0.0F, -0.034F, -0.153F),
				posL(0.82F, 0.0F, -0.016F, -0.092F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, -2.0F),
				rotL(0.26F, -18.0F, 0.0F, -3.0F),
				rotL(0.58F, -2.0F, 0.0F, -1.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 0.0F, 0.0F),
				rotL(0.26F, 36.0F, 0.0F, 0.0F),
				rotL(0.58F, 4.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -12.0F, 0.0F, 0.0F),
				rotL(0.26F, -18.0F, 0.0F, 0.0F),
				rotL(0.58F, -2.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 6.0F, 0.0F, 2.0F),
				rotL(0.26F, 0.0F, 0.0F, 3.0F),
				rotL(0.58F, 0.0F, 0.0F, 1.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -34.0F, 0.0F, 2.0F),
				rotL(0.26F, -26.0F, 0.0F, 3.0F),
				rotL(0.58F, -4.0F, 0.0F, 1.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 36.0F, 0.0F, 0.0F),
				rotL(0.26F, 46.0F, 0.0F, 0.0F),
				rotL(0.58F, 8.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -16.0F, 0.0F, 0.0F),
				rotL(0.26F, -22.0F, 0.0F, 0.0F),
				rotL(0.58F, -4.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 6.0F, 0.0F, -2.0F),
				rotL(0.26F, 2.0F, 0.0F, -3.0F),
				rotL(0.58F, 0.0F, 0.0F, -1.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, 4.0F, 0.0F),
				rotL(0.26F, 8.0F, -4.0F, 0.0F),
				rotL(0.58F, -2.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 12.0F, -8.0F, -2.0F),
				rotL(0.26F, 12.0F, 12.0F, 3.0F),
				rotL(0.58F, -4.0F, -4.0F, -2.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -18.0F, -6.0F, -10.0F),
				rotL(0.26F, -46.0F, -10.0F, -10.0F),
				rotL(0.58F, -4.0F, 0.0F, -6.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -32.0F, 0.0F, 0.0F),
				rotL(0.26F, -18.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -12.0F, 0.0F, 16.0F),
				rotL(0.26F, -14.0F, 0.0F, -18.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -68.0F, 8.0F, -8.0F),
				rotL(0.26F, 14.0F, 8.0F, 20.0F),
				rotL(0.58F, -6.0F, 0.0F, 6.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -24.0F, 0.0F, 0.0F),
				rotL(0.26F, -24.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 12.0F, 0.0F, -16.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 8.0F, 0.0F, 0.0F),
				rotL(0.26F, 10.0F, 0.0F, 0.0F),
				rotL(0.58F, -3.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -4.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -44.0F, -4.0F, 12.0F),
				rotL(0.26F, -36.0F, 14.0F, -22.0F),
				rotL(0.58F, -6.0F, -12.0F, 18.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, 0.0F),
				rotL(0.26F, 18.0F, 0.0F, 0.0F),
				rotL(0.58F, -2.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.85F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(0.85F);
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

	private static final class ClimbAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -4.0F, 5.0F, -4.0F),
				rotL(0.28F, -4.0F, 1.5F, -1.2F),
				rotL(0.6F, -4.0F, -5.0F, 4.0F),
				rotL(0.9F, -4.0F, -1.5F, 1.2F),
				rotL(1.2F, -4.0F, 5.0F, -4.0F)
			));
		}
		private static void ch_3_p0(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(0.0F, 0.0F, -0.608F, 0.0F),
				posL(0.02F, 0.0F, -0.761F, 0.0F),
				posL(0.04F, 0.0F, -0.921F, 0.0F),
				posL(0.06F, 0.0F, -1.088F, 0.0F),
				posL(0.08F, 0.0F, -1.261F, 0.0F),
				posL(0.1F, 0.0F, -1.441F, 0.0F),
				posL(0.12F, 0.0F, -1.627F, 0.0F),
				posL(0.14F, 0.0F, -1.819F, 0.0F),
				posL(0.16F, 0.0F, -2.017F, 0.0F),
				posL(0.18F, 0.0F, -2.221F, 0.0F),
				posL(0.2F, 0.0F, -2.43F, 0.0F),
				posL(0.22F, 0.0F, -2.645F, 0.0F),
				posL(0.24F, 0.0F, -2.866F, 0.0F),
				posL(0.26F, 0.0F, -3.091F, 0.0F),
				posL(0.28F, 0.0F, -2.925F, 0.0F),
				posL(0.3F, 0.0F, -2.77F, 0.0F),
				posL(0.32F, 0.0F, -2.617F, 0.0F),
				posL(0.34F, 0.0F, -2.467F, 0.0F),
				posL(0.36F, 0.0F, -2.32F, 0.0F),
				posL(0.38F, 0.0F, -2.158F, 0.0F),
				posL(0.4F, 0.0F, -2.0F, 0.0F),
				posL(0.42F, 0.0F, -1.845F, 0.0F),
				posL(0.44F, 0.0F, -1.693F, 0.0F),
				posL(0.46F, 0.0F, -1.544F, 0.0F),
				posL(0.48F, 0.0F, -1.399F, 0.0F),
				posL(0.5F, 0.0F, -1.257F, 0.0F),
				posL(0.52F, 0.0F, -1.12F, 0.0F),
				posL(0.54F, 0.0F, -0.986F, 0.0F),
				posL(0.56F, 0.0F, -0.856F, 0.0F),
				posL(0.58F, 0.0F, -0.73F, 0.0F),
				posL(0.6F, 0.0F, -0.608F, 0.0F),
				posL(0.62F, 0.0F, -0.751F, 0.0F),
				posL(0.64F, 0.0F, -0.9F, 0.0F),
				posL(0.66F, 0.0F, -1.054F, 0.0F),
				posL(0.68F, 0.0F, -1.215F, 0.0F),
				posL(0.7F, 0.0F, -1.381F, 0.0F),
				posL(0.72F, 0.0F, -1.552F, 0.0F),
				posL(0.74F, 0.0F, -1.729F, 0.0F),
				posL(0.76F, 0.0F, -1.911F, 0.0F),
				posL(0.78F, 0.0F, -2.098F, 0.0F),
				posL(0.8F, 0.0F, -2.29F, 0.0F),
				posL(0.82F, 0.0F, -2.487F, 0.0F),
				posL(0.84F, 0.0F, -2.689F, 0.0F),
				posL(0.86F, 0.0F, -2.895F, 0.0F),
				posL(0.88F, 0.0F, -3.107F, 0.0F),
				posL(0.9F, 0.0F, -2.925F, 0.0F),
				posL(0.92F, 0.0F, -2.76F, 0.0F),
				posL(0.94F, 0.0F, -2.597F, 0.0F),
				posL(0.96F, 0.0F, -2.438F, 0.0F),
				posL(0.98F, 0.0F, -2.277F, 0.0F),
				posL(1.0F, 0.0F, -2.105F, 0.0F),
				posL(1.02F, 0.0F, -1.937F, 0.0F),
				posL(1.04F, 0.0F, -1.773F, 0.0F),
				posL(1.06F, 0.0F, -1.613F, 0.0F),
				posL(1.08F, 0.0F, -1.456F, 0.0F),
				posL(1.1F, 0.0F, -1.304F, 0.0F),
				posL(1.12F, 0.0F, -1.156F, 0.0F),
				posL(1.14F, 0.0F, -1.012F, 0.0F),
				posL(1.16F, 0.0F, -0.873F, 0.0F),
				posL(1.18F, 0.0F, -0.738F, 0.0F)
			));
		}
		private static void ch_3_p1(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(1.2F, 0.0F, -0.608F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			List<Keyframe> kfs = new ArrayList<>(61);
			ch_3_p0(kfs);
			ch_3_p1(kfs);
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION, kfs.toArray(new Keyframe[0])));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -48.0F, 0.0F, -5.0F),
				rotL(0.28F, -24.0F, 0.0F, -5.0F),
				rotL(0.6F, -8.0F, 0.0F, -5.0F),
				rotL(0.9F, -28.0F, 0.0F, -5.0F),
				rotL(1.2F, -48.0F, 0.0F, -5.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 56.0F, 0.0F, 0.0F),
				rotL(0.28F, 28.0F, 0.0F, 0.0F),
				rotL(0.6F, 14.0F, 0.0F, 0.0F),
				rotL(0.9F, 32.0F, 0.0F, 0.0F),
				rotL(1.2F, 56.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, 0.0F, 0.0F),
				rotL(0.28F, -14.0F, 0.0F, 0.0F),
				rotL(0.6F, -8.0F, 0.0F, 0.0F),
				rotL(0.9F, -16.0F, 0.0F, 0.0F),
				rotL(1.2F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 12.0F, 0.0F, 5.0F),
				rotL(0.28F, 4.0F, 0.0F, 5.0F),
				rotL(0.6F, -4.0F, 0.0F, 5.0F),
				rotL(0.9F, 6.0F, 0.0F, 5.0F),
				rotL(1.2F, 12.0F, 0.0F, 5.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -8.0F, 0.0F, 5.0F),
				rotL(0.28F, -28.0F, 0.0F, 5.0F),
				rotL(0.6F, -48.0F, 0.0F, 5.0F),
				rotL(0.9F, -24.0F, 0.0F, 5.0F),
				rotL(1.2F, -8.0F, 0.0F, 5.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 14.0F, 0.0F, 0.0F),
				rotL(0.28F, 32.0F, 0.0F, 0.0F),
				rotL(0.6F, 56.0F, 0.0F, 0.0F),
				rotL(0.9F, 28.0F, 0.0F, 0.0F),
				rotL(1.2F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -8.0F, 0.0F, 0.0F),
				rotL(0.28F, -16.0F, 0.0F, 0.0F),
				rotL(0.6F, -26.0F, 0.0F, 0.0F),
				rotL(0.9F, -14.0F, 0.0F, 0.0F),
				rotL(1.2F, -8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -4.0F, 0.0F, -5.0F),
				rotL(0.28F, 6.0F, 0.0F, -5.0F),
				rotL(0.6F, 12.0F, 0.0F, -5.0F),
				rotL(0.9F, 4.0F, 0.0F, -5.0F),
				rotL(1.2F, -4.0F, 0.0F, -5.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.0F, 2.0F, 0.0F),
				rotL(0.28F, -2.0F, 0.6F, 0.0F),
				rotL(0.6F, -2.0F, -2.0F, 0.0F),
				rotL(0.9F, -2.0F, -0.6F, 0.0F),
				rotL(1.2F, -2.0F, 2.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -6.0F, -6.0F, 0.0F),
				rotL(0.28F, -6.0F, -1.8F, 0.0F),
				rotL(0.6F, -6.0F, 6.0F, 0.0F),
				rotL(0.9F, -6.0F, 1.8F, 0.0F),
				rotL(1.2F, -6.0F, -6.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -42.0F, -8.0F, 8.0F),
				rotL(0.28F, -92.0F, -6.0F, 6.0F),
				rotL(0.6F, -130.0F, -10.0F, 10.0F),
				rotL(0.9F, -84.0F, -6.0F, 6.0F),
				rotL(1.2F, -42.0F, -8.0F, 8.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -38.0F, 0.0F, 0.0F),
				rotL(0.28F, -28.0F, 0.0F, 0.0F),
				rotL(0.6F, -24.0F, 0.0F, 0.0F),
				rotL(0.9F, -34.0F, 0.0F, 0.0F),
				rotL(1.2F, -38.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -12.0F, 0.0F, -18.0F),
				rotL(0.28F, -12.0F, 0.0F, 14.0F),
				rotL(0.6F, -12.0F, 0.0F, 20.0F),
				rotL(0.9F, -12.0F, 0.0F, 18.0F),
				rotL(1.2F, -12.0F, 0.0F, -18.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -130.0F, 10.0F, -10.0F),
				rotL(0.28F, -84.0F, 6.0F, -6.0F),
				rotL(0.6F, -42.0F, 8.0F, -8.0F),
				rotL(0.9F, -92.0F, 6.0F, -6.0F),
				rotL(1.2F, -130.0F, 10.0F, -10.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -24.0F, 0.0F, 0.0F),
				rotL(0.28F, -34.0F, 0.0F, 0.0F),
				rotL(0.6F, -38.0F, 0.0F, 0.0F),
				rotL(0.9F, -28.0F, 0.0F, 0.0F),
				rotL(1.2F, -24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 0.0F, -20.0F),
				rotL(0.28F, -14.0F, 0.0F, -18.0F),
				rotL(0.6F, -14.0F, 0.0F, 18.0F),
				rotL(0.9F, -14.0F, 0.0F, -14.0F),
				rotL(1.2F, -14.0F, 0.0F, -20.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -6.0F, 0.0F, 0.0F),
				rotL(0.28F, -6.0F, 0.0F, 0.0F),
				rotL(0.6F, -6.0F, 0.0F, 0.0F),
				rotL(0.9F, -6.0F, 0.0F, 0.0F),
				rotL(1.2F, -6.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.0F, 0.0F, 0.0F),
				rotL(0.28F, -2.0F, 0.0F, 0.0F),
				rotL(0.6F, -2.0F, 0.0F, 0.0F),
				rotL(0.9F, -2.0F, 0.0F, 0.0F),
				rotL(1.2F, -2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -24.0F, 8.0F, 16.0F),
				rotL(0.28F, -24.0F, 2.4F, 8.0F),
				rotL(0.6F, -24.0F, -8.0F, -16.0F),
				rotL(0.9F, -24.0F, -2.4F, -6.0F),
				rotL(1.2F, -24.0F, 8.0F, 16.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 14.0F, 0.0F, 0.0F),
				rotL(0.28F, 4.0F, 0.0F, 0.0F),
				rotL(0.6F, 16.0F, 0.0F, 0.0F),
				rotL(0.9F, 4.0F, 0.0F, 0.0F),
				rotL(1.2F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

	private static final class ClimbStartAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 8.0F, -2.0F, 1.0F),
				rotL(0.5F, -5.0F, 4.0F, -3.0F),
				rotL(0.75F, -4.0F, 5.0F, -4.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.02F, 0.0F, -0.138F, 0.0F),
				posL(0.04F, 0.0F, -0.301F, 0.0F),
				posL(0.06F, 0.0F, -0.479F, 0.0F),
				posL(0.08F, 0.0F, -0.671F, 0.0F),
				posL(0.1F, 0.0F, -0.877F, 0.0F),
				posL(0.12F, 0.0F, -1.098F, 0.0F),
				posL(0.14F, 0.0F, -1.331F, 0.0F),
				posL(0.16F, 0.0F, -1.578F, 0.0F),
				posL(0.18F, 0.0F, -1.837F, 0.0F),
				posL(0.2F, 0.0F, -2.109F, 0.0F),
				posL(0.22F, 0.0F, -2.393F, 0.0F),
				posL(0.24F, 0.0F, -2.688F, 0.0F),
				posL(0.26F, 0.0F, -2.844F, 0.0F),
				posL(0.28F, 0.0F, -2.92F, 0.0F),
				posL(0.3F, 0.0F, -2.77F, 0.0F),
				posL(0.32F, 0.0F, -2.617F, 0.0F),
				posL(0.34F, 0.0F, -2.472F, 0.0F),
				posL(0.36F, 0.0F, -2.336F, 0.0F),
				posL(0.38F, 0.0F, -2.133F, 0.0F),
				posL(0.4F, 0.0F, -1.929F, 0.0F),
				posL(0.42F, 0.0F, -1.737F, 0.0F),
				posL(0.44F, 0.0F, -1.556F, 0.0F),
				posL(0.46F, 0.0F, -1.386F, 0.0F),
				posL(0.48F, 0.0F, -1.228F, 0.0F),
				posL(0.5F, 0.0F, -1.082F, 0.0F),
				posL(0.52F, 0.0F, -1.043F, 0.0F),
				posL(0.54F, 0.0F, -1.004F, 0.0F),
				posL(0.56F, 0.0F, -0.965F, 0.0F),
				posL(0.58F, 0.0F, -0.927F, 0.0F),
				posL(0.6F, 0.0F, -0.888F, 0.0F),
				posL(0.62F, 0.0F, -0.85F, 0.0F),
				posL(0.64F, 0.0F, -0.813F, 0.0F),
				posL(0.66F, 0.0F, -0.775F, 0.0F),
				posL(0.68F, 0.0F, -0.737F, 0.0F),
				posL(0.7F, 0.0F, -0.7F, 0.0F),
				posL(0.72F, 0.0F, -0.663F, 0.0F),
				posL(0.75F, 0.0F, -0.608F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -20.0F, 0.0F, -3.0F),
				rotL(0.5F, -44.0F, 0.0F, -5.0F),
				rotL(0.75F, -48.0F, 0.0F, -5.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 38.0F, 0.0F, 0.0F),
				rotL(0.5F, 52.0F, 0.0F, 0.0F),
				rotL(0.75F, 56.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -18.0F, 0.0F, 0.0F),
				rotL(0.5F, -24.0F, 0.0F, 0.0F),
				rotL(0.75F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 3.0F),
				rotL(0.5F, 10.0F, 0.0F, 5.0F),
				rotL(0.75F, 12.0F, 0.0F, 5.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -22.0F, 0.0F, 3.0F),
				rotL(0.5F, -10.0F, 0.0F, 4.0F),
				rotL(0.75F, -8.0F, 0.0F, 5.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 42.0F, 0.0F, 0.0F),
				rotL(0.5F, 18.0F, 0.0F, 0.0F),
				rotL(0.75F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -20.0F, 0.0F, 0.0F),
				rotL(0.5F, -10.0F, 0.0F, 0.0F),
				rotL(0.75F, -8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, -3.0F),
				rotL(0.5F, -2.0F, 0.0F, -4.0F),
				rotL(0.75F, -4.0F, 0.0F, -5.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 6.0F, 0.0F, 0.0F),
				rotL(0.5F, -2.0F, 2.0F, 0.0F),
				rotL(0.75F, -2.0F, 2.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 6.0F, 4.0F, 0.0F),
				rotL(0.5F, -8.0F, -5.0F, 0.0F),
				rotL(0.75F, -6.0F, -6.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -18.0F, -6.0F, -12.0F),
				rotL(0.5F, -56.0F, -8.0F, 8.0F),
				rotL(0.75F, -42.0F, -8.0F, 8.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -32.0F, 0.0F, 0.0F),
				rotL(0.5F, -36.0F, 0.0F, 0.0F),
				rotL(0.75F, -38.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, -12.0F, 0.0F, -16.0F),
				rotL(0.75F, -12.0F, 0.0F, -18.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -28.0F, 6.0F, 12.0F),
				rotL(0.5F, -136.0F, 12.0F, -10.0F),
				rotL(0.75F, -130.0F, 10.0F, -10.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -32.0F, 0.0F, 0.0F),
				rotL(0.5F, -20.0F, 0.0F, 0.0F),
				rotL(0.75F, -24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, -16.0F, 0.0F, -22.0F),
				rotL(0.75F, -14.0F, 0.0F, -20.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -4.0F, 0.0F, 0.0F),
				rotL(0.5F, -6.0F, 0.0F, 0.0F),
				rotL(0.75F, -6.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, -2.0F, 0.0F, 0.0F),
				rotL(0.75F, -2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -26.0F, -10.0F, -18.0F),
				rotL(0.5F, -26.0F, 6.0F, 18.0F),
				rotL(0.75F, -24.0F, 8.0F, 16.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 22.0F, 0.0F, 0.0F),
				rotL(0.5F, 8.0F, 0.0F, 0.0F),
				rotL(0.75F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.75F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(0.75F);
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

	private static final class ClimbStopAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -4.0F, 5.0F, -4.0F),
				rotL(0.28F, 8.0F, -2.0F, 1.0F),
				rotL(0.56F, -2.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, -0.608F, 0.0F),
				posL(0.02F, 0.0F, -0.729F, 0.0F),
				posL(0.04F, 0.0F, -0.859F, 0.0F),
				posL(0.06F, 0.0F, -0.997F, 0.0F),
				posL(0.08F, 0.0F, -1.143F, 0.0F),
				posL(0.1F, 0.0F, -1.297F, 0.0F),
				posL(0.12F, 0.0F, -1.46F, 0.0F),
				posL(0.14F, 0.0F, -1.631F, 0.0F),
				posL(0.16F, 0.0F, -1.788F, 0.0F),
				posL(0.18F, 0.0F, -1.887F, 0.0F),
				posL(0.2F, 0.0F, -1.993F, 0.0F),
				posL(0.22F, 0.0F, -2.107F, 0.0F),
				posL(0.24F, 0.0F, -2.215F, 0.0F),
				posL(0.26F, 0.0F, -2.326F, 0.0F),
				posL(0.28F, 0.0F, -2.445F, 0.0F),
				posL(0.3F, 0.0F, -2.259F, 0.0F),
				posL(0.32F, 0.0F, -2.081F, 0.0F),
				posL(0.34F, 0.0F, -1.909F, 0.0F),
				posL(0.36F, 0.0F, -1.744F, 0.0F),
				posL(0.38F, 0.0F, -1.586F, 0.0F),
				posL(0.4F, 0.0F, -1.435F, 0.0F),
				posL(0.42F, 0.0F, -1.291F, 0.0F),
				posL(0.44F, 0.0F, -1.155F, 0.0F),
				posL(0.46F, 0.0F, -1.027F, 0.0F),
				posL(0.48F, 0.0F, -0.906F, 0.0F),
				posL(0.5F, 0.0F, -0.793F, 0.0F),
				posL(0.52F, 0.0F, -0.648F, 0.0F),
				posL(0.54F, 0.0F, -0.501F, 0.0F),
				posL(0.56F, 0.0F, -0.362F, 0.0F),
				posL(0.58F, 0.0F, -0.328F, 0.0F),
				posL(0.6F, 0.0F, -0.294F, 0.0F),
				posL(0.62F, 0.0F, -0.261F, 0.0F),
				posL(0.64F, 0.0F, -0.228F, 0.0F),
				posL(0.66F, 0.0F, -0.196F, 0.0F),
				posL(0.68F, 0.0F, -0.165F, 0.0F),
				posL(0.7F, 0.0F, -0.134F, 0.0F),
				posL(0.72F, 0.0F, -0.104F, 0.0F),
				posL(0.74F, 0.0F, -0.075F, 0.0F),
				posL(0.76F, 0.0F, -0.046F, 0.0F),
				posL(0.78F, 0.0F, -0.018F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -48.0F, 0.0F, -5.0F),
				rotL(0.28F, -22.0F, 0.0F, -3.0F),
				rotL(0.56F, -3.0F, 0.0F, -1.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 56.0F, 0.0F, 0.0F),
				rotL(0.28F, 40.0F, 0.0F, 0.0F),
				rotL(0.56F, 6.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, 0.0F, 0.0F),
				rotL(0.28F, -18.0F, 0.0F, 0.0F),
				rotL(0.56F, -3.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 12.0F, 0.0F, 5.0F),
				rotL(0.28F, 0.0F, 0.0F, 3.0F),
				rotL(0.56F, 0.0F, 0.0F, 1.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -8.0F, 0.0F, 5.0F),
				rotL(0.28F, -18.0F, 0.0F, 3.0F),
				rotL(0.56F, -3.0F, 0.0F, 1.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 14.0F, 0.0F, 0.0F),
				rotL(0.28F, 34.0F, 0.0F, 0.0F),
				rotL(0.56F, 6.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -8.0F, 0.0F, 0.0F),
				rotL(0.28F, -16.0F, 0.0F, 0.0F),
				rotL(0.56F, -3.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -4.0F, 0.0F, -5.0F),
				rotL(0.28F, 0.0F, 0.0F, -3.0F),
				rotL(0.56F, 0.0F, 0.0F, -1.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.0F, 2.0F, 0.0F),
				rotL(0.28F, 6.0F, 0.0F, 0.0F),
				rotL(0.56F, -2.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -6.0F, -6.0F, 0.0F),
				rotL(0.28F, 8.0F, 4.0F, 0.0F),
				rotL(0.56F, -2.0F, -2.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -42.0F, -8.0F, 8.0F),
				rotL(0.28F, -22.0F, -4.0F, -8.0F),
				rotL(0.56F, -4.0F, 0.0F, -4.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -38.0F, 0.0F, 0.0F),
				rotL(0.28F, -24.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -12.0F, 0.0F, -18.0F),
				rotL(0.28F, 0.0F, 0.0F, -16.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -130.0F, 10.0F, -10.0F),
				rotL(0.28F, -58.0F, 4.0F, 8.0F),
				rotL(0.56F, -8.0F, 0.0F, 4.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -24.0F, 0.0F, 0.0F),
				rotL(0.28F, -26.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 0.0F, -20.0F),
				rotL(0.28F, 0.0F, 0.0F, 16.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -6.0F, 0.0F, 0.0F),
				rotL(0.28F, 6.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -24.0F, 8.0F, 16.0F),
				rotL(0.28F, -22.0F, -10.0F, -18.0F),
				rotL(0.56F, -4.0F, 8.0F, 12.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 14.0F, 0.0F, 0.0F),
				rotL(0.28F, 16.0F, 0.0F, 0.0F),
				rotL(0.56F, -2.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(0.8F);
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

	private static final class AttackAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, -10.0F, -20.0F, 6.0F),
				rotL(0.54F, 14.0F, 22.0F, -5.0F),
				rotL(0.8F, 12.0F, -20.0F, 4.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3_p0(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.02F, 0.0F, 0.065F, 0.088F),
				posL(0.04F, 0.0F, 0.108F, 0.175F),
				posL(0.06F, 0.0F, 0.149F, 0.262F),
				posL(0.08F, 0.0F, 0.188F, 0.35F),
				posL(0.1F, 0.0F, 0.225F, 0.438F),
				posL(0.12F, 0.0F, 0.261F, 0.525F),
				posL(0.14F, 0.0F, 0.295F, 0.613F),
				posL(0.16F, 0.0F, 0.327F, 0.7F),
				posL(0.18F, 0.0F, 0.357F, 0.788F),
				posL(0.2F, 0.0F, 0.385F, 0.875F),
				posL(0.22F, 0.0F, 0.412F, 0.962F),
				posL(0.24F, 0.0F, 0.437F, 1.05F),
				posL(0.26F, 0.0F, 0.46F, 1.138F),
				posL(0.28F, 0.0F, 0.481F, 1.225F),
				posL(0.3F, 0.0F, 0.5F, 1.313F),
				posL(0.32F, 0.0F, 0.518F, 1.4F),
				posL(0.34F, 0.0F, 0.443F, 0.945F),
				posL(0.36F, 0.0F, 0.33F, 0.491F),
				posL(0.38F, 0.0F, 0.172F, 0.036F),
				posL(0.4F, 0.0F, -0.036F, -0.418F),
				posL(0.42F, 0.0F, -0.237F, -0.873F),
				posL(0.44F, 0.0F, -0.413F, -1.327F),
				posL(0.46F, 0.0F, -0.596F, -1.782F),
				posL(0.48F, 0.0F, -0.844F, -2.236F),
				posL(0.5F, 0.0F, -1.162F, -2.691F),
				posL(0.52F, 0.0F, -1.557F, -3.145F),
				posL(0.54F, 0.0F, -1.497F, -3.6F),
				posL(0.56F, 0.0F, -1.511F, -3.6F),
				posL(0.58F, 0.0F, -1.53F, -3.6F),
				posL(0.6F, 0.0F, -1.551F, -3.6F),
				posL(0.62F, 0.0F, -1.418F, -3.6F),
				posL(0.64F, 0.0F, -1.299F, -3.6F),
				posL(0.66F, 0.0F, -1.193F, -3.6F),
				posL(0.68F, 0.0F, -1.102F, -3.6F),
				posL(0.7F, 0.0F, -0.995F, -3.6F),
				posL(0.72F, 0.0F, -0.895F, -3.6F),
				posL(0.74F, 0.0F, -0.808F, -3.6F),
				posL(0.76F, 0.0F, -0.735F, -3.6F),
				posL(0.78F, 0.0F, -0.675F, -3.6F),
				posL(0.8F, 0.0F, -0.629F, -3.6F),
				posL(0.82F, 0.0F, -0.535F, -3.469F),
				posL(0.84F, 0.0F, -0.447F, -3.338F),
				posL(0.86F, 0.0F, -0.364F, -3.207F),
				posL(0.88F, 0.0F, -0.287F, -3.076F),
				posL(0.9F, 0.0F, -0.215F, -2.945F),
				posL(0.92F, 0.0F, -0.149F, -2.815F),
				posL(0.94F, 0.0F, -0.089F, -2.684F),
				posL(0.96F, 0.0F, -0.034F, -2.553F),
				posL(0.98F, 0.0F, 0.016F, -2.422F),
				posL(1.0F, 0.0F, 0.06F, -2.291F),
				posL(1.02F, 0.0F, 0.099F, -2.16F),
				posL(1.04F, 0.0F, 0.133F, -2.029F),
				posL(1.06F, 0.0F, 0.161F, -1.898F),
				posL(1.08F, 0.0F, 0.184F, -1.767F),
				posL(1.1F, 0.0F, 0.202F, -1.636F),
				posL(1.12F, 0.0F, 0.215F, -1.505F),
				posL(1.14F, 0.0F, 0.223F, -1.375F),
				posL(1.16F, 0.0F, 0.226F, -1.244F),
				posL(1.18F, 0.0F, 0.224F, -1.113F)
			));
		}
		private static void ch_3_p1(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(1.2F, 0.0F, 0.217F, -0.982F),
				posL(1.22F, 0.0F, 0.206F, -0.851F),
				posL(1.24F, 0.0F, 0.189F, -0.72F),
				posL(1.26F, 0.0F, 0.169F, -0.589F),
				posL(1.28F, 0.0F, 0.143F, -0.458F),
				posL(1.3F, 0.0F, 0.114F, -0.327F),
				posL(1.32F, 0.0F, 0.079F, -0.196F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			List<Keyframe> kfs = new ArrayList<>(68);
			ch_3_p0(kfs);
			ch_3_p1(kfs);
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION, kfs.toArray(new Keyframe[0])));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, 14.0F, 0.0F, -3.0F),
				rotL(0.54F, 20.0F, 0.0F, -3.0F),
				rotL(0.8F, 16.0F, 0.0F, -2.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, -8.0F, 0.0F, 0.0F),
				rotL(0.54F, -12.0F, 0.0F, 0.0F),
				rotL(0.8F, -10.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, -6.0F, 0.0F, 0.0F),
				rotL(0.54F, -8.0F, 0.0F, 0.0F),
				rotL(0.8F, -6.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, 0.0F, 0.0F, 3.0F),
				rotL(0.54F, 0.0F, 0.0F, 3.0F),
				rotL(0.8F, 0.0F, 0.0F, 2.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, -20.0F, 0.0F, 3.0F),
				rotL(0.54F, -30.0F, 0.0F, 3.0F),
				rotL(0.8F, -24.0F, 0.0F, 2.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, 26.0F, 0.0F, 0.0F),
				rotL(0.54F, 34.0F, 0.0F, 0.0F),
				rotL(0.8F, 28.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, -10.0F, 0.0F, 0.0F),
				rotL(0.54F, -12.0F, 0.0F, 0.0F),
				rotL(0.8F, -10.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, 4.0F, 0.0F, -3.0F),
				rotL(0.54F, 8.0F, 0.0F, -3.0F),
				rotL(0.8F, 6.0F, 0.0F, -2.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, -6.0F, -8.0F, 4.0F),
				rotL(0.54F, 8.0F, 8.0F, 0.0F),
				rotL(0.8F, 6.0F, -6.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, -10.0F, -26.0F, -4.0F),
				rotL(0.54F, 14.0F, 28.0F, 4.0F),
				rotL(0.8F, 12.0F, -26.0F, -3.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, -124.0F, -20.0F, -28.0F),
				rotL(0.54F, -58.0F, 34.0F, -10.0F),
				rotL(0.8F, 20.0F, -12.0F, -16.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, -30.0F, 0.0F, 0.0F),
				rotL(0.54F, -18.0F, 0.0F, 0.0F),
				rotL(0.8F, -24.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, 20.0F, 0.0F, -24.0F),
				rotL(0.54F, -18.0F, 0.0F, -20.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, 26.0F, 14.0F, 20.0F),
				rotL(0.54F, -96.0F, 16.0F, 24.0F),
				rotL(0.8F, -52.0F, -34.0F, -14.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, -26.0F, 0.0F, 0.0F),
				rotL(0.54F, -28.0F, 0.0F, 0.0F),
				rotL(0.8F, -16.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.54F, 18.0F, 0.0F, 22.0F),
				rotL(0.8F, -20.0F, 0.0F, -20.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, -8.0F, 0.0F, 0.0F),
				rotL(0.54F, 10.0F, 0.0F, 0.0F),
				rotL(0.8F, 12.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, -6.0F, 22.0F, -28.0F),
				rotL(0.54F, -28.0F, -20.0F, 22.0F),
				rotL(0.8F, -26.0F, 16.0F, -18.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.32F, 38.0F, 0.0F, 0.0F),
				rotL(0.54F, 32.0F, 0.0F, 0.0F),
				rotL(0.8F, -4.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.35F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.35F);
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

	private static final class ScreechAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 12.0F, 0.0F, 0.0F),
				rotL(0.62F, -8.0F, -4.0F, 2.0F),
				rotL(0.74F, -8.0F, 4.0F, -2.0F),
				rotL(0.86F, -8.0F, -4.0F, 2.0F),
				rotL(0.98F, -8.0F, 4.0F, -2.0F),
				rotL(1.1F, -8.0F, -4.0F, 2.0F),
				rotL(1.22F, -8.0F, 4.0F, -2.0F),
				rotL(1.36F, -8.0F, -2.8F, 2.0F),
				rotL(1.5F, -8.0F, 2.8F, -2.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3_p0(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.02F, 0.0F, -0.07F, 0.0F),
				posL(0.04F, 0.0F, -0.167F, 0.0F),
				posL(0.06F, 0.0F, -0.273F, 0.0F),
				posL(0.08F, 0.0F, -0.386F, 0.0F),
				posL(0.1F, 0.0F, -0.507F, 0.0F),
				posL(0.12F, 0.0F, -0.635F, 0.0F),
				posL(0.14F, 0.0F, -0.771F, 0.0F),
				posL(0.16F, 0.0F, -0.914F, 0.0F),
				posL(0.18F, 0.0F, -1.064F, 0.0F),
				posL(0.2F, 0.0F, -1.221F, 0.0F),
				posL(0.22F, 0.0F, -1.385F, 0.0F),
				posL(0.24F, 0.0F, -1.556F, 0.0F),
				posL(0.26F, 0.0F, -1.733F, 0.0F),
				posL(0.28F, 0.0F, -1.917F, 0.0F),
				posL(0.3F, 0.0F, -2.106F, 0.0F),
				posL(0.32F, 0.0F, -2.302F, 0.0F),
				posL(0.34F, 0.0F, -2.504F, 0.0F),
				posL(0.36F, 0.0F, -2.712F, 0.0F),
				posL(0.38F, 0.0F, -2.925F, 0.0F),
				posL(0.4F, 0.0F, -2.537F, 0.0F),
				posL(0.42F, 0.0F, -2.174F, 0.0F),
				posL(0.44F, 0.0F, -1.836F, 0.0F),
				posL(0.46F, 0.0F, -1.526F, 0.0F),
				posL(0.48F, 0.0F, -1.243F, 0.0F),
				posL(0.5F, 0.0F, -0.99F, 0.0F),
				posL(0.52F, 0.0F, -0.766F, 0.0F),
				posL(0.54F, 0.0F, -0.484F, 0.0F),
				posL(0.56F, 0.0F, -0.207F, 0.0F),
				posL(0.58F, 0.0F, 0.038F, 0.0F),
				posL(0.6F, 0.0F, 0.25F, 0.0F),
				posL(0.62F, 0.0F, 0.429F, 0.0F),
				posL(0.64F, 0.0F, 0.357F, 0.0F),
				posL(0.66F, 0.0F, 0.281F, 0.0F),
				posL(0.68F, 0.0F, 0.203F, 0.0F),
				posL(0.7F, 0.0F, 0.281F, 0.0F),
				posL(0.72F, 0.0F, 0.357F, 0.0F),
				posL(0.74F, 0.0F, 0.429F, 0.0F),
				posL(0.76F, 0.0F, 0.357F, 0.0F),
				posL(0.78F, 0.0F, 0.281F, 0.0F),
				posL(0.8F, 0.0F, 0.203F, 0.0F),
				posL(0.82F, 0.0F, 0.281F, 0.0F),
				posL(0.84F, 0.0F, 0.357F, 0.0F),
				posL(0.86F, 0.0F, 0.429F, 0.0F),
				posL(0.88F, 0.0F, 0.357F, 0.0F),
				posL(0.9F, 0.0F, 0.281F, 0.0F),
				posL(0.92F, 0.0F, 0.203F, 0.0F),
				posL(0.94F, 0.0F, 0.281F, 0.0F),
				posL(0.96F, 0.0F, 0.357F, 0.0F),
				posL(0.98F, 0.0F, 0.429F, 0.0F),
				posL(1.0F, 0.0F, 0.357F, 0.0F),
				posL(1.02F, 0.0F, 0.281F, 0.0F),
				posL(1.04F, 0.0F, 0.203F, 0.0F),
				posL(1.06F, 0.0F, 0.281F, 0.0F),
				posL(1.08F, 0.0F, 0.357F, 0.0F),
				posL(1.1F, 0.0F, 0.429F, 0.0F),
				posL(1.12F, 0.0F, 0.357F, 0.0F),
				posL(1.14F, 0.0F, 0.281F, 0.0F),
				posL(1.16F, 0.0F, 0.203F, 0.0F),
				posL(1.18F, 0.0F, 0.281F, 0.0F)
			));
		}
		private static void ch_3_p1(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(1.2F, 0.0F, 0.357F, 0.0F),
				posL(1.22F, 0.0F, 0.429F, 0.0F),
				posL(1.24F, 0.0F, 0.367F, 0.0F),
				posL(1.26F, 0.0F, 0.303F, 0.0F),
				posL(1.28F, 0.0F, 0.237F, 0.0F),
				posL(1.3F, 0.0F, 0.237F, 0.0F),
				posL(1.32F, 0.0F, 0.303F, 0.0F),
				posL(1.34F, 0.0F, 0.367F, 0.0F),
				posL(1.36F, 0.0F, 0.428F, 0.0F),
				posL(1.38F, 0.0F, 0.367F, 0.0F),
				posL(1.4F, 0.0F, 0.303F, 0.0F),
				posL(1.42F, 0.0F, 0.237F, 0.0F),
				posL(1.44F, 0.0F, 0.237F, 0.0F),
				posL(1.46F, 0.0F, 0.303F, 0.0F),
				posL(1.48F, 0.0F, 0.367F, 0.0F),
				posL(1.5F, 0.0F, 0.428F, 0.0F),
				posL(1.52F, 0.0F, 0.419F, 0.0F),
				posL(1.54F, 0.0F, 0.409F, 0.0F),
				posL(1.56F, 0.0F, 0.399F, 0.0F),
				posL(1.58F, 0.0F, 0.388F, 0.0F),
				posL(1.6F, 0.0F, 0.377F, 0.0F),
				posL(1.62F, 0.0F, 0.365F, 0.0F),
				posL(1.64F, 0.0F, 0.352F, 0.0F),
				posL(1.66F, 0.0F, 0.339F, 0.0F),
				posL(1.68F, 0.0F, 0.325F, 0.0F),
				posL(1.7F, 0.0F, 0.31F, 0.0F),
				posL(1.72F, 0.0F, 0.295F, 0.0F),
				posL(1.74F, 0.0F, 0.28F, 0.0F),
				posL(1.76F, 0.0F, 0.263F, 0.0F),
				posL(1.78F, 0.0F, 0.247F, 0.0F),
				posL(1.8F, 0.0F, 0.229F, 0.0F),
				posL(1.82F, 0.0F, 0.211F, 0.0F),
				posL(1.84F, 0.0F, 0.192F, 0.0F),
				posL(1.86F, 0.0F, 0.173F, 0.0F),
				posL(1.88F, 0.0F, 0.153F, 0.0F),
				posL(1.9F, 0.0F, 0.132F, 0.0F),
				posL(1.92F, 0.0F, 0.111F, 0.0F),
				posL(1.94F, 0.0F, 0.089F, 0.0F),
				posL(1.96F, 0.0F, 0.067F, 0.0F),
				posL(1.98F, 0.0F, 0.044F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			List<Keyframe> kfs = new ArrayList<>(101);
			ch_3_p0(kfs);
			ch_3_p1(kfs);
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION, kfs.toArray(new Keyframe[0])));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -22.0F, 0.0F, -4.0F),
				rotL(0.62F, 8.0F, 0.0F, -5.0F),
				rotL(0.74F, 8.0F, 0.0F, -5.0F),
				rotL(0.86F, 8.0F, 0.0F, -5.0F),
				rotL(0.98F, 8.0F, 0.0F, -5.0F),
				rotL(1.1F, 8.0F, 0.0F, -5.0F),
				rotL(1.22F, 8.0F, 0.0F, -5.0F),
				rotL(1.36F, 8.0F, 0.0F, -5.0F),
				rotL(1.5F, 8.0F, 0.0F, -5.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 40.0F, 0.0F, 0.0F),
				rotL(0.62F, -8.0F, 0.0F, 0.0F),
				rotL(0.74F, -8.0F, 0.0F, 0.0F),
				rotL(0.86F, -8.0F, 0.0F, 0.0F),
				rotL(0.98F, -8.0F, 0.0F, 0.0F),
				rotL(1.1F, -8.0F, 0.0F, 0.0F),
				rotL(1.22F, -8.0F, 0.0F, 0.0F),
				rotL(1.36F, -8.0F, 0.0F, 0.0F),
				rotL(1.5F, -8.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -18.0F, 0.0F, 0.0F),
				rotL(0.62F, 0.0F, 0.0F, 0.0F),
				rotL(0.74F, 0.0F, 0.0F, 0.0F),
				rotL(0.86F, 0.0F, 0.0F, 0.0F),
				rotL(0.98F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 0.0F, 0.0F, 0.0F),
				rotL(1.22F, 0.0F, 0.0F, 0.0F),
				rotL(1.36F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 0.0F, 0.0F, 4.0F),
				rotL(0.62F, 0.0F, 0.0F, 5.0F),
				rotL(0.74F, 0.0F, 0.0F, 5.0F),
				rotL(0.86F, 0.0F, 0.0F, 5.0F),
				rotL(0.98F, 0.0F, 0.0F, 5.0F),
				rotL(1.1F, 0.0F, 0.0F, 5.0F),
				rotL(1.22F, 0.0F, 0.0F, 5.0F),
				rotL(1.36F, 0.0F, 0.0F, 5.0F),
				rotL(1.5F, 0.0F, 0.0F, 5.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -22.0F, 0.0F, 4.0F),
				rotL(0.62F, 8.0F, 0.0F, 5.0F),
				rotL(0.74F, 8.0F, 0.0F, 5.0F),
				rotL(0.86F, 8.0F, 0.0F, 5.0F),
				rotL(0.98F, 8.0F, 0.0F, 5.0F),
				rotL(1.1F, 8.0F, 0.0F, 5.0F),
				rotL(1.22F, 8.0F, 0.0F, 5.0F),
				rotL(1.36F, 8.0F, 0.0F, 5.0F),
				rotL(1.5F, 8.0F, 0.0F, 5.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 40.0F, 0.0F, 0.0F),
				rotL(0.62F, -8.0F, 0.0F, 0.0F),
				rotL(0.74F, -8.0F, 0.0F, 0.0F),
				rotL(0.86F, -8.0F, 0.0F, 0.0F),
				rotL(0.98F, -8.0F, 0.0F, 0.0F),
				rotL(1.1F, -8.0F, 0.0F, 0.0F),
				rotL(1.22F, -8.0F, 0.0F, 0.0F),
				rotL(1.36F, -8.0F, 0.0F, 0.0F),
				rotL(1.5F, -8.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -18.0F, 0.0F, 0.0F),
				rotL(0.62F, 0.0F, 0.0F, 0.0F),
				rotL(0.74F, 0.0F, 0.0F, 0.0F),
				rotL(0.86F, 0.0F, 0.0F, 0.0F),
				rotL(0.98F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 0.0F, 0.0F, 0.0F),
				rotL(1.22F, 0.0F, 0.0F, 0.0F),
				rotL(1.36F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 0.0F, 0.0F, -4.0F),
				rotL(0.62F, 0.0F, 0.0F, -5.0F),
				rotL(0.74F, 0.0F, 0.0F, -5.0F),
				rotL(0.86F, 0.0F, 0.0F, -5.0F),
				rotL(0.98F, 0.0F, 0.0F, -5.0F),
				rotL(1.1F, 0.0F, 0.0F, -5.0F),
				rotL(1.22F, 0.0F, 0.0F, -5.0F),
				rotL(1.36F, 0.0F, 0.0F, -5.0F),
				rotL(1.5F, 0.0F, 0.0F, -5.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 10.0F, 0.0F, 0.0F),
				rotL(0.62F, -6.0F, 0.0F, 0.0F),
				rotL(0.74F, -6.0F, 0.0F, 0.0F),
				rotL(0.86F, -6.0F, 0.0F, 0.0F),
				rotL(0.98F, -6.0F, 0.0F, 0.0F),
				rotL(1.1F, -6.0F, 0.0F, 0.0F),
				rotL(1.22F, -6.0F, 0.0F, 0.0F),
				rotL(1.36F, -6.0F, 0.0F, 0.0F),
				rotL(1.5F, -6.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 16.0F, 0.0F, 0.0F),
				rotL(0.62F, -14.0F, -5.0F, 0.0F),
				rotL(0.74F, -14.0F, 5.0F, 0.0F),
				rotL(0.86F, -14.0F, -5.0F, 0.0F),
				rotL(0.98F, -14.0F, 5.0F, 0.0F),
				rotL(1.1F, -14.0F, -5.0F, 0.0F),
				rotL(1.22F, -14.0F, 5.0F, 0.0F),
				rotL(1.36F, -14.0F, -3.5F, 0.0F),
				rotL(1.5F, -14.0F, 3.5F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -72.0F, 22.0F, 16.0F),
				rotL(0.62F, 34.0F, -12.0F, -46.0F),
				rotL(0.74F, 34.0F, -12.0F, -38.0F),
				rotL(0.86F, 34.0F, -12.0F, -46.0F),
				rotL(0.98F, 34.0F, -12.0F, -38.0F),
				rotL(1.1F, 34.0F, -12.0F, -46.0F),
				rotL(1.22F, 34.0F, -12.0F, -38.0F),
				rotL(1.36F, 34.0F, -12.0F, -46.0F),
				rotL(1.5F, 34.0F, -12.0F, -38.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -46.0F, 0.0F, 0.0F),
				rotL(0.62F, -10.0F, 0.0F, -10.0F),
				rotL(0.74F, -10.0F, 0.0F, -10.0F),
				rotL(0.86F, -10.0F, 0.0F, -10.0F),
				rotL(0.98F, -10.0F, 0.0F, -10.0F),
				rotL(1.1F, -10.0F, 0.0F, -10.0F),
				rotL(1.22F, -10.0F, 0.0F, -10.0F),
				rotL(1.36F, -10.0F, 0.0F, -10.0F),
				rotL(1.5F, -10.0F, 0.0F, -10.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -14.0F, 0.0F, 20.0F),
				rotL(0.62F, 18.0F, 0.0F, -24.0F),
				rotL(0.74F, 18.0F, 0.0F, -24.0F),
				rotL(0.86F, 18.0F, 0.0F, -24.0F),
				rotL(0.98F, 18.0F, 0.0F, -24.0F),
				rotL(1.1F, 18.0F, 0.0F, -24.0F),
				rotL(1.22F, 18.0F, 0.0F, -24.0F),
				rotL(1.36F, 18.0F, 0.0F, -24.0F),
				rotL(1.5F, 18.0F, 0.0F, -24.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -72.0F, -22.0F, -16.0F),
				rotL(0.62F, 34.0F, 12.0F, 46.0F),
				rotL(0.74F, 34.0F, 12.0F, 38.0F),
				rotL(0.86F, 34.0F, 12.0F, 46.0F),
				rotL(0.98F, 34.0F, 12.0F, 38.0F),
				rotL(1.1F, 34.0F, 12.0F, 46.0F),
				rotL(1.22F, 34.0F, 12.0F, 38.0F),
				rotL(1.36F, 34.0F, 12.0F, 46.0F),
				rotL(1.5F, 34.0F, 12.0F, 38.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -46.0F, 0.0F, 0.0F),
				rotL(0.62F, -10.0F, 0.0F, 10.0F),
				rotL(0.74F, -10.0F, 0.0F, 10.0F),
				rotL(0.86F, -10.0F, 0.0F, 10.0F),
				rotL(0.98F, -10.0F, 0.0F, 10.0F),
				rotL(1.1F, -10.0F, 0.0F, 10.0F),
				rotL(1.22F, -10.0F, 0.0F, 10.0F),
				rotL(1.36F, -10.0F, 0.0F, 10.0F),
				rotL(1.5F, -10.0F, 0.0F, 10.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -14.0F, 0.0F, -20.0F),
				rotL(0.62F, 18.0F, 0.0F, 24.0F),
				rotL(0.74F, 18.0F, 0.0F, 24.0F),
				rotL(0.86F, 18.0F, 0.0F, 24.0F),
				rotL(0.98F, 18.0F, 0.0F, 24.0F),
				rotL(1.1F, 18.0F, 0.0F, 24.0F),
				rotL(1.22F, 18.0F, 0.0F, 24.0F),
				rotL(1.36F, 18.0F, 0.0F, 24.0F),
				rotL(1.5F, 18.0F, 0.0F, 24.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 16.0F, 0.0F, 0.0F),
				rotL(0.62F, -10.0F, 0.0F, 0.0F),
				rotL(0.74F, -10.0F, 0.0F, 0.0F),
				rotL(0.86F, -10.0F, 0.0F, 0.0F),
				rotL(0.98F, -10.0F, 0.0F, 0.0F),
				rotL(1.1F, -10.0F, 0.0F, 0.0F),
				rotL(1.22F, -10.0F, 0.0F, 0.0F),
				rotL(1.36F, -10.0F, 0.0F, 0.0F),
				rotL(1.5F, -10.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, -6.0F, 0.0F, 0.0F),
				rotL(0.74F, -6.0F, 0.0F, 0.0F),
				rotL(0.86F, -6.0F, 0.0F, 0.0F),
				rotL(0.98F, -6.0F, 0.0F, 0.0F),
				rotL(1.1F, -6.0F, 0.0F, 0.0F),
				rotL(1.22F, -6.0F, 0.0F, 0.0F),
				rotL(1.36F, -6.0F, 0.0F, 0.0F),
				rotL(1.5F, -6.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 8.0F, 0.0F, 14.0F),
				rotL(0.62F, -22.0F, -14.0F, 16.0F),
				rotL(0.74F, -22.0F, 14.0F, -16.0F),
				rotL(0.86F, -22.0F, -14.0F, 16.0F),
				rotL(0.98F, -22.0F, 14.0F, -16.0F),
				rotL(1.1F, -22.0F, -14.0F, 16.0F),
				rotL(1.22F, -22.0F, 14.0F, -16.0F),
				rotL(1.36F, -22.0F, -9.8F, 11.2F),
				rotL(1.5F, -22.0F, 9.8F, -11.2F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -2.0F, 0.0F, 0.0F),
				rotL(0.62F, 44.0F, 0.0F, 0.0F),
				rotL(0.74F, 38.0F, 0.0F, 0.0F),
				rotL(0.86F, 44.0F, 0.0F, 0.0F),
				rotL(0.98F, 38.0F, 0.0F, 0.0F),
				rotL(1.1F, 44.0F, 0.0F, 0.0F),
				rotL(1.22F, 38.0F, 0.0F, 0.0F),
				rotL(1.36F, 44.0F, 0.0F, 0.0F),
				rotL(1.5F, 38.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(2.0F);
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

	private static final class HurtAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -12.0F, 10.0F, 6.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.02F, 0.0F, 0.141F, 0.0F),
				posL(0.04F, 0.0F, 0.242F, 0.0F),
				posL(0.06F, 0.0F, 0.322F, 0.0F),
				posL(0.08F, 0.0F, 0.379F, 0.0F),
				posL(0.1F, 0.0F, 0.411F, 0.0F),
				posL(0.12F, 0.0F, 0.418F, 0.0F),
				posL(0.14F, 0.0F, 0.397F, 0.0F),
				posL(0.16F, 0.0F, 0.406F, 0.0F),
				posL(0.18F, 0.0F, 0.412F, 0.0F),
				posL(0.2F, 0.0F, 0.416F, 0.0F),
				posL(0.22F, 0.0F, 0.418F, 0.0F),
				posL(0.24F, 0.0F, 0.419F, 0.0F),
				posL(0.26F, 0.0F, 0.417F, 0.0F),
				posL(0.28F, 0.0F, 0.413F, 0.0F),
				posL(0.3F, 0.0F, 0.407F, 0.0F),
				posL(0.32F, 0.0F, 0.399F, 0.0F),
				posL(0.34F, 0.0F, 0.39F, 0.0F),
				posL(0.36F, 0.0F, 0.378F, 0.0F),
				posL(0.38F, 0.0F, 0.365F, 0.0F),
				posL(0.4F, 0.0F, 0.349F, 0.0F),
				posL(0.42F, 0.0F, 0.332F, 0.0F),
				posL(0.44F, 0.0F, 0.314F, 0.0F),
				posL(0.46F, 0.0F, 0.293F, 0.0F),
				posL(0.48F, 0.0F, 0.271F, 0.0F),
				posL(0.5F, 0.0F, 0.247F, 0.0F),
				posL(0.52F, 0.0F, 0.222F, 0.0F),
				posL(0.54F, 0.0F, 0.195F, 0.0F),
				posL(0.56F, 0.0F, 0.166F, 0.0F),
				posL(0.58F, 0.0F, 0.136F, 0.0F),
				posL(0.6F, 0.0F, 0.105F, 0.0F),
				posL(0.62F, 0.0F, 0.072F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 6.0F, 0.0F, -3.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -6.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 0.0F, 0.0F, 3.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -14.0F, 0.0F, 3.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 24.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -10.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 0.0F, 0.0F, -3.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -14.0F, 14.0F, 6.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 22.0F, 0.0F, -28.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -30.0F, 0.0F, 28.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -12.0F, 0.0F, -6.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -22.0F, -24.0F, -32.0F),
				rotL(0.38F, 4.0F, 10.0F, 16.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 30.0F, 0.0F, 0.0F),
				rotL(0.38F, -2.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(0.65F);
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

	private static final class DeathAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1_p0(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.02F, 0.0F, -0.086F, 0.0F),
				posL(0.04F, 0.0F, -0.214F, 0.0F),
				posL(0.06F, 0.0F, -0.358F, 0.0F),
				posL(0.08F, 0.0F, -0.519F, 0.0F),
				posL(0.1F, 0.0F, -0.695F, 0.0F),
				posL(0.12F, 0.0F, -0.887F, 0.0F),
				posL(0.14F, 0.0F, -1.093F, 0.0F),
				posL(0.16F, 0.0F, -1.313F, 0.0F),
				posL(0.18F, 0.0F, -1.547F, 0.0F),
				posL(0.2F, 0.0F, -1.794F, 0.0F),
				posL(0.22F, 0.0F, -2.054F, 0.0F),
				posL(0.24F, 0.0F, -2.326F, 0.0F),
				posL(0.26F, 0.0F, -2.609F, 0.0F),
				posL(0.28F, 0.0F, -2.904F, 0.0F),
				posL(0.3F, 0.0F, -3.208F, 0.0F),
				posL(0.32F, 0.0F, -3.523F, 0.0F),
				posL(0.34F, 0.0F, -3.846F, 0.0F),
				posL(0.36F, 0.0F, -4.179F, 0.0F),
				posL(0.38F, 0.0F, -4.52F, 0.0F),
				posL(0.4F, 0.0F, -4.45F, 0.171F),
				posL(0.42F, 0.0F, -4.425F, 0.343F),
				posL(0.44F, 0.0F, -4.445F, 0.514F),
				posL(0.46F, 0.0F, -4.51F, 0.686F),
				posL(0.48F, 0.0F, -4.436F, 0.857F),
				posL(0.5F, 0.0F, -4.383F, 1.029F),
				posL(0.52F, 0.0F, -4.375F, 1.2F),
				posL(0.54F, 0.0F, -4.414F, 1.371F),
				posL(0.56F, 0.0F, -4.5F, 1.543F),
				posL(0.58F, 0.0F, -4.632F, 1.714F),
				posL(0.6F, 0.0F, -4.812F, 1.886F),
				posL(0.62F, 0.0F, -5.037F, 2.057F),
				posL(0.64F, 0.0F, -5.309F, 2.229F),
				posL(0.66F, 0.0F, -5.626F, 2.4F),
				posL(0.68F, 0.0F, -5.989F, 2.571F),
				posL(0.7F, 0.0F, -6.396F, 2.743F),
				posL(0.72F, 0.0F, -6.847F, 2.914F),
				posL(0.74F, 0.0F, -7.342F, 3.086F),
				posL(0.76F, 0.0F, -7.877F, 3.257F),
				posL(0.78F, 0.0F, -8.454F, 3.429F),
				posL(0.8F, 0.0F, -9.07F, 3.6F),
				posL(0.82F, 0.0F, -9.725F, 3.771F),
				posL(0.84F, 0.0F, -10.416F, 3.943F),
				posL(0.86F, 0.0F, -11.143F, 4.114F),
				posL(0.88F, 0.0F, -11.903F, 4.286F),
				posL(0.9F, 0.0F, -12.695F, 4.457F),
				posL(0.92F, 0.0F, -13.517F, 4.629F),
				posL(0.94F, 0.0F, -14.367F, 4.8F),
				posL(0.96F, 0.0F, -15.188F, 4.971F),
				posL(0.98F, 0.0F, -15.945F, 5.143F),
				posL(1.0F, 0.0F, -16.724F, 5.314F),
				posL(1.02F, 0.0F, -17.335F, 5.486F),
				posL(1.04F, 0.0F, -17.914F, 5.657F),
				posL(1.06F, 0.0F, -18.51F, 5.829F),
				posL(1.08F, 0.0F, -19.123F, 6.0F),
				posL(1.1F, 0.0F, -19.159F, 6.0F),
				posL(1.12F, 0.0F, -19.196F, 6.0F),
				posL(1.14F, 0.0F, -19.232F, 6.0F),
				posL(1.16F, 0.0F, -19.269F, 6.0F),
				posL(1.18F, 0.0F, -19.305F, 6.0F)
			));
		}
		private static void ch_1_p1(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(1.2F, 0.0F, -19.342F, 6.0F),
				posL(1.22F, 0.0F, -19.378F, 6.0F),
				posL(1.24F, 0.0F, -19.415F, 6.0F),
				posL(1.26F, 0.0F, -19.452F, 6.0F),
				posL(1.28F, 0.0F, -19.488F, 6.0F),
				posL(1.3F, 0.0F, -19.525F, 6.0F),
				posL(1.32F, 0.0F, -19.525F, 6.0F),
				posL(1.34F, 0.0F, -19.525F, 6.0F),
				posL(1.36F, 0.0F, -19.525F, 6.0F),
				posL(1.38F, 0.0F, -19.525F, 6.0F),
				posL(1.4F, 0.0F, -19.525F, 6.0F),
				posL(1.42F, 0.0F, -19.525F, 6.0F),
				posL(1.44F, 0.0F, -19.525F, 6.0F),
				posL(1.46F, 0.0F, -19.525F, 6.0F),
				posL(1.48F, 0.0F, -19.525F, 6.0F),
				posL(1.5F, 0.0F, -19.525F, 6.0F),
				posL(1.52F, 0.0F, -19.525F, 6.0F),
				posL(1.54F, 0.0F, -19.525F, 6.0F),
				posL(1.56F, 0.0F, -19.525F, 6.0F),
				posL(1.58F, 0.0F, -19.525F, 6.0F),
				posL(1.6F, 0.0F, -19.525F, 6.0F),
				posL(1.62F, 0.0F, -19.525F, 6.0F),
				posL(1.64F, 0.0F, -19.525F, 6.0F),
				posL(1.66F, 0.0F, -19.525F, 6.0F),
				posL(1.68F, 0.0F, -19.525F, 6.0F),
				posL(1.7F, 0.0F, -19.525F, 6.0F),
				posL(1.72F, 0.0F, -19.525F, 6.0F),
				posL(1.74F, 0.0F, -19.525F, 6.0F),
				posL(1.76F, 0.0F, -19.525F, 6.0F),
				posL(1.78F, 0.0F, -19.525F, 6.0F),
				posL(1.8F, 0.0F, -19.525F, 6.0F),
				posL(1.82F, 0.0F, -19.525F, 6.0F),
				posL(1.84F, 0.0F, -19.525F, 6.0F),
				posL(1.85F, 0.0F, -19.525F, 6.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			List<Keyframe> kfs = new ArrayList<>(94);
			ch_1_p0(kfs);
			ch_1_p1(kfs);
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION, kfs.toArray(new Keyframe[0])));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 14.0F, -8.0F, -10.0F),
				rotL(1.08F, -80.36F, -8.0F, -14.0F),
				rotL(1.3F, -82.0F, -8.0F, -14.0F),
				rotL(1.42F, -82.0F, -8.0F, -14.0F),
				rotL(1.56F, -82.0F, -8.0F, -14.0F),
				rotL(1.85F, -82.0F, -8.0F, -14.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("pelvis_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -26.0F, 0.0F, -4.0F),
				rotL(1.08F, -10.0F, 0.0F, -12.0F),
				rotL(1.3F, -10.0F, 0.0F, -12.0F),
				rotL(1.42F, -10.0F, 0.0F, -12.0F),
				rotL(1.56F, -10.0F, 0.0F, -12.0F),
				rotL(1.85F, -10.0F, 0.0F, -12.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 54.0F, 0.0F, 0.0F),
				rotL(1.08F, 24.0F, 0.0F, 0.0F),
				rotL(1.3F, 24.0F, 0.0F, 0.0F),
				rotL(1.42F, 24.0F, 0.0F, 0.0F),
				rotL(1.56F, 24.0F, 0.0F, 0.0F),
				rotL(1.85F, 24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -28.0F, 0.0F, 0.0F),
				rotL(1.08F, -16.0F, 0.0F, 0.0F),
				rotL(1.3F, -16.0F, 0.0F, 0.0F),
				rotL(1.42F, -16.0F, 0.0F, 0.0F),
				rotL(1.56F, -16.0F, 0.0F, 0.0F),
				rotL(1.85F, -16.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 0.0F, 0.0F, 4.0F),
				rotL(1.08F, -8.0F, 0.0F, 12.0F),
				rotL(1.3F, -8.0F, 0.0F, 12.0F),
				rotL(1.42F, -8.0F, 0.0F, 12.0F),
				rotL(1.56F, -8.0F, 0.0F, 12.0F),
				rotL(1.85F, -8.0F, 0.0F, 12.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -30.0F, 0.0F, 4.0F),
				rotL(1.08F, -18.0F, 0.0F, 10.0F),
				rotL(1.3F, -18.0F, 0.0F, 10.0F),
				rotL(1.42F, -18.0F, 0.0F, 10.0F),
				rotL(1.56F, -18.0F, 0.0F, 10.0F),
				rotL(1.85F, -18.0F, 0.0F, 10.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 60.0F, 0.0F, 0.0F),
				rotL(1.08F, 36.0F, 0.0F, 0.0F),
				rotL(1.3F, 36.0F, 0.0F, 0.0F),
				rotL(1.42F, 36.0F, 0.0F, 0.0F),
				rotL(1.56F, 36.0F, 0.0F, 0.0F),
				rotL(1.85F, 36.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -30.0F, 0.0F, 0.0F),
				rotL(1.08F, -20.0F, 0.0F, 0.0F),
				rotL(1.3F, -20.0F, 0.0F, 0.0F),
				rotL(1.42F, -20.0F, 0.0F, 0.0F),
				rotL(1.56F, -20.0F, 0.0F, 0.0F),
				rotL(1.85F, -20.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 0.0F, 0.0F, -4.0F),
				rotL(1.08F, -10.0F, 0.0F, -10.0F),
				rotL(1.3F, -10.0F, 0.0F, -10.0F),
				rotL(1.42F, -10.0F, 0.0F, -10.0F),
				rotL(1.56F, -10.0F, 0.0F, -10.0F),
				rotL(1.85F, -10.0F, 0.0F, -10.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("hoof_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -16.0F, 0.0F, 0.0F),
				rotL(1.08F, -6.0F, 0.0F, -4.0F),
				rotL(1.3F, -6.0F, 0.0F, -4.0F),
				rotL(1.42F, -6.0F, 0.0F, -4.0F),
				rotL(1.56F, -6.0F, 0.0F, -4.0F),
				rotL(1.85F, -6.0F, 0.0F, -4.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -30.0F, 0.0F, -28.0F),
				rotL(1.08F, -10.0F, 0.0F, -44.0F),
				rotL(1.3F, -10.0F, 0.0F, -44.0F),
				rotL(1.42F, -10.0F, 0.0F, -44.0F),
				rotL(1.56F, -10.0F, 0.0F, -44.0F),
				rotL(1.85F, -10.0F, 0.0F, -44.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.08F, -24.0F, 0.0F, 0.0F),
				rotL(1.3F, -24.0F, 0.0F, 0.0F),
				rotL(1.42F, -34.0F, 0.0F, 0.0F),
				rotL(1.56F, -24.0F, 0.0F, 0.0F),
				rotL(1.85F, -24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.08F, 0.0F, 0.0F, -12.0F),
				rotL(1.3F, 0.0F, 0.0F, -10.0F),
				rotL(1.42F, 0.0F, 0.0F, 18.0F),
				rotL(1.56F, 0.0F, 0.0F, -12.0F),
				rotL(1.85F, 0.0F, 0.0F, -12.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -38.0F, 0.0F, 28.0F),
				rotL(1.08F, -16.0F, 0.0F, 48.0F),
				rotL(1.3F, -16.0F, 0.0F, 48.0F),
				rotL(1.42F, -16.0F, 0.0F, 48.0F),
				rotL(1.56F, -16.0F, 0.0F, 48.0F),
				rotL(1.85F, -16.0F, 0.0F, 48.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.08F, -18.0F, 0.0F, 0.0F),
				rotL(1.3F, -18.0F, 0.0F, 0.0F),
				rotL(1.42F, -18.0F, 0.0F, 0.0F),
				rotL(1.56F, -18.0F, 0.0F, 0.0F),
				rotL(1.85F, -18.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.08F, 0.0F, 0.0F, 14.0F),
				rotL(1.3F, 0.0F, 0.0F, 12.0F),
				rotL(1.42F, 0.0F, 0.0F, -20.0F),
				rotL(1.56F, 0.0F, 0.0F, 14.0F),
				rotL(1.85F, 0.0F, 0.0F, 14.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.08F, 18.0F, 0.0F, 0.0F),
				rotL(1.3F, 18.0F, 0.0F, 0.0F),
				rotL(1.42F, 18.0F, 0.0F, 0.0F),
				rotL(1.56F, 18.0F, 0.0F, 0.0F),
				rotL(1.85F, 18.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.08F, 12.0F, 0.0F, 0.0F),
				rotL(1.3F, 12.0F, 0.0F, 0.0F),
				rotL(1.42F, 12.0F, 0.0F, 0.0F),
				rotL(1.56F, 12.0F, 0.0F, 0.0F),
				rotL(1.85F, 12.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -30.0F, 16.0F, 22.0F),
				rotL(1.08F, 8.0F, -26.0F, -16.0F),
				rotL(1.3F, 8.0F, -26.0F, -18.0F),
				rotL(1.42F, 8.0F, -26.0F, -6.0F),
				rotL(1.56F, 8.0F, -26.0F, -18.0F),
				rotL(1.85F, 8.0F, -26.0F, -18.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("skull_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 38.0F, 0.0F, 0.0F),
				rotL(1.08F, 22.0F, 0.0F, 0.0F),
				rotL(1.3F, 18.0F, 0.0F, 0.0F),
				rotL(1.42F, 32.0F, 0.0F, 0.0F),
				rotL(1.56F, 16.0F, 0.0F, 0.0F),
				rotL(1.85F, 16.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("antler_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("antler_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.85F);
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
			ch_24(b);
			ch_25(b);
			ch_26(b);
			ch_27(b);
			ch_28(b);
			ch_29(b);
			ch_30(b);
			ch_31(b);
			ch_32(b);
			ch_33(b);
			ch_34(b);
			ch_35(b);
			ch_36(b);
			ch_37(b);
			ch_38(b);
			ch_39(b);
			ch_40(b);
			ch_41(b);
			ch_42(b);
			ch_43(b);
			ch_44(b);
			ch_45(b);
			ch_46(b);
			ch_47(b);
			ch_48(b);
			ch_49(b);
			ch_50(b);
			ch_51(b);
			return b.build();
		}
	}

}
