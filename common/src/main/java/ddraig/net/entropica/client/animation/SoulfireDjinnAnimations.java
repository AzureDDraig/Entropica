package ddraig.net.entropica.client.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Exact Blockbench keyframe animations for Soulfire Djinn (Fauna 57 — Desert Efreet).
 * Split into per-animation static builders and <=60-keyframe helpers so constant pools and method bytecodes stay tiny.
 */
public final class SoulfireDjinnAnimations {

	private SoulfireDjinnAnimations() {}

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
	public static final AnimationDefinition GLIDE_TAKEOFF = GlideTakeoffAnimBuilder.create();
	public static final AnimationDefinition glide_takeoff = GLIDE_TAKEOFF;
	public static final AnimationDefinition GLIDE = GlideAnimBuilder.create();
	public static final AnimationDefinition glide = GLIDE;
	public static final AnimationDefinition GLIDE_LANDING = GlideLandingAnimBuilder.create();
	public static final AnimationDefinition glide_landing = GLIDE_LANDING;
	public static final AnimationDefinition ATTACK = AttackAnimBuilder.create();
	public static final AnimationDefinition attack = ATTACK;
	public static final AnimationDefinition PHASE_GLIDE = PhaseGlideAnimBuilder.create();
	public static final AnimationDefinition phase_glide = PHASE_GLIDE;
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
				posL(0.9F, 0.0F, 1.0F, 0.0F),
				posL(1.8F, 0.0F, 0.2F, 0.0F),
				posL(2.7F, 0.0F, -0.6F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, -4.0F, 0.0F, 0.0F),
				rotL(2.7F, 3.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 2.0F, -4.0F, 1.5F),
				rotL(2.7F, -2.0F, 4.0F, -1.5F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, -3.0F, -8.0F, 3.0F),
				rotL(2.0F, 2.0F, 6.0F, -2.0F),
				rotL(2.9F, -4.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.9F, 1.0F, 1.14F, 1.0F),
				sclL(1.8F, 1.0F, 0.94F, 1.0F),
				sclL(2.8F, 1.06F, 1.18F, 1.06F),
				sclL(3.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, -38.0F, -28.0F, -18.0F),
				rotL(1.9F, -38.0F, -28.0F, -18.0F),
				rotL(2.8F, 8.0F, 10.0F, 12.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, -42.0F, -36.0F, -8.0F),
				rotL(1.9F, -42.0F, -36.0F, -8.0F),
				rotL(2.8F, 10.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, -8.0F, 0.0F, 0.0F),
				rotL(1.9F, -8.0F, 0.0F, 0.0F),
				rotL(2.8F, 12.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, -34.0F, 28.0F, 18.0F),
				rotL(1.9F, -34.0F, 28.0F, 18.0F),
				rotL(2.8F, 8.0F, -10.0F, -12.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, -46.0F, 36.0F, 8.0F),
				rotL(1.9F, -46.0F, 36.0F, 8.0F),
				rotL(2.8F, 10.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, -8.0F, 0.0F, 0.0F),
				rotL(1.9F, -8.0F, 0.0F, 0.0F),
				rotL(2.8F, 12.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(3.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, -3.0F, 0.0F, 8.0F),
				rotL(1.8F, 1.5F, 0.0F, -4.0F),
				rotL(2.7F, 3.0F, 0.0F, -8.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(3.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 4.0F, 0.0F, 12.0F),
				rotL(1.8F, -2.0F, 0.0F, -6.0F),
				rotL(2.7F, -4.0F, 0.0F, -12.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, -5.0F, 0.0F, -14.0F),
				rotL(1.8F, 2.5F, 0.0F, 7.0F),
				rotL(2.7F, 5.0F, 0.0F, 14.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 6.0F, 0.0F, -12.0F),
				rotL(1.8F, -3.0F, 0.0F, 6.0F),
				rotL(2.7F, -6.0F, 0.0F, 12.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, -6.0F, 0.0F, 14.0F),
				rotL(1.8F, 3.0F, 0.0F, -7.0F),
				rotL(2.7F, 6.0F, 0.0F, -14.0F),
				rotL(3.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
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
			return b.build();
		}
	}

	private static final class WalkAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 12.0F, -4.0F, 2.0F),
				rotL(0.8F, 12.0F, 0.0F, 0.0F),
				rotL(1.2F, 12.0F, 4.0F, -2.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.4F, 0.0F, 0.8F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, -0.5F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 4.0F, -4.0F, 0.0F),
				rotL(0.8F, 4.0F, 0.0F, 0.0F),
				rotL(1.2F, 4.0F, 4.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -4.0F, 3.0F, 0.0F),
				rotL(0.8F, -4.0F, 0.0F, 0.0F),
				rotL(1.2F, -4.0F, -3.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -14.0F, 6.0F, -2.0F),
				rotL(0.8F, -14.0F, 0.0F, 0.0F),
				rotL(1.2F, -14.0F, -6.0F, 2.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.4F, 1.0F, 1.16F, 1.0F),
				sclL(1.2F, 1.0F, 1.12F, 1.0F),
				sclL(1.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 4.0F, 0.0F, 8.0F),
				rotL(0.8F, 12.0F, 0.0F, 8.0F),
				rotL(1.2F, 20.0F, 0.0F, 8.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 6.0F, 0.0F, 0.0F),
				rotL(0.8F, 6.0F, 0.0F, 0.0F),
				rotL(1.2F, 6.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 20.0F, 0.0F, -8.0F),
				rotL(0.8F, 12.0F, 0.0F, -8.0F),
				rotL(1.2F, 4.0F, 0.0F, -8.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 6.0F, 0.0F, 0.0F),
				rotL(0.8F, 6.0F, 0.0F, 0.0F),
				rotL(1.2F, 6.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 12.0F, 0.0F, 10.0F),
				rotL(0.8F, 12.0F, 0.0F, 0.0F),
				rotL(1.2F, 12.0F, 0.0F, -10.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 8.0F, 0.0F, 14.0F),
				rotL(0.8F, 8.0F, 0.0F, 0.0F),
				rotL(1.2F, 8.0F, 0.0F, -14.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -6.0F, 0.0F, -16.0F),
				rotL(0.8F, -6.0F, 0.0F, 0.0F),
				rotL(1.2F, -6.0F, 0.0F, 16.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -4.0F, 0.0F, -14.0F),
				rotL(0.8F, -4.0F, 0.0F, 0.0F),
				rotL(1.2F, -4.0F, 0.0F, 14.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 8.0F, 0.0F, 16.0F),
				rotL(0.8F, 8.0F, 0.0F, 0.0F),
				rotL(1.2F, 8.0F, 0.0F, -16.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
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
			return b.build();
		}
	}

	private static final class GlideTakeoffAnimBuilder {
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
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -10.0F, 0.0F, 0.0F),
				rotL(0.82F, 24.0F, 0.0F, 0.0F),
				rotL(1.2F, 30.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.36F, 0.0F, -0.8F, 0.8F),
				posL(0.82F, 0.0F, 8.4F, -0.8F),
				posL(1.2F, 0.0F, 7.5F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -8.0F, 0.0F, 0.0F),
				rotL(0.82F, 6.0F, 0.0F, 0.0F),
				rotL(1.2F, 10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, 6.0F, 0.0F, 0.0F),
				rotL(0.82F, -8.0F, 0.0F, 0.0F),
				rotL(1.2F, -10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, 10.0F, 0.0F, 0.0F),
				rotL(0.82F, -32.0F, 0.0F, 0.0F),
				rotL(1.2F, -38.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.36F, 0.95F, 0.9F, 0.95F),
				sclL(0.82F, 1.15F, 1.38F, 1.15F),
				sclL(1.2F, 1.05F, 1.24F, 1.05F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -32.0F, -22.0F, -16.0F),
				rotL(0.82F, 42.0F, 16.0F, 32.0F),
				rotL(1.2F, 36.0F, 12.0F, 24.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -30.0F, -18.0F, 0.0F),
				rotL(0.82F, 26.0F, 0.0F, 0.0F),
				rotL(1.2F, 22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.82F, 18.0F, 0.0F, 0.0F),
				rotL(1.2F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -32.0F, 22.0F, 16.0F),
				rotL(0.82F, 42.0F, -16.0F, -32.0F),
				rotL(1.2F, 36.0F, -12.0F, -24.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -30.0F, 18.0F, 0.0F),
				rotL(0.82F, 26.0F, 0.0F, 0.0F),
				rotL(1.2F, 22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.82F, 18.0F, 0.0F, 0.0F),
				rotL(1.2F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.2F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -12.0F, 0.0F, 0.0F),
				rotL(0.82F, 34.0F, 0.0F, 0.0F),
				rotL(1.2F, 28.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.2F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -16.0F, 0.0F, 0.0F),
				rotL(0.82F, 18.0F, 0.0F, 0.0F),
				rotL(1.2F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.82F, -20.0F, 0.0F, 0.0F),
				rotL(1.2F, -16.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 12.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.2F);
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
			return b.build();
		}
	}

	private static final class GlideAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 30.0F, 0.0F, 0.0F),
				rotL(0.4F, 31.65F, -5.0F, -6.0F),
				rotL(0.8F, 30.0F, 0.0F, 0.0F),
				rotL(1.2F, 28.65F, 5.0F, 6.0F),
				rotL(1.6F, 30.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 7.5F, 0.0F),
				posL(0.4F, -0.6F, 8.6F, 0.0F),
				posL(0.8F, 0.0F, 7.5F, 0.0F),
				posL(1.2F, 0.6F, 6.6F, 0.0F),
				posL(1.6F, 0.0F, 7.5F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, 0.0F, 0.0F),
				rotL(0.4F, 10.0F, -4.0F, 0.0F),
				rotL(0.8F, 10.0F, 0.0F, 0.0F),
				rotL(1.2F, 10.0F, 4.0F, 0.0F),
				rotL(1.6F, 10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -10.0F, 0.0F, 0.0F),
				rotL(0.4F, -10.0F, 4.0F, 3.0F),
				rotL(0.8F, -10.0F, 0.0F, 0.0F),
				rotL(1.2F, -10.0F, -4.0F, -3.0F),
				rotL(1.6F, -10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -38.0F, 0.0F, 0.0F),
				rotL(0.4F, -39.65F, 7.0F, 5.0F),
				rotL(0.8F, -38.0F, 0.0F, 0.0F),
				rotL(1.2F, -36.65F, -7.0F, -5.0F),
				rotL(1.6F, -38.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.05F, 1.24F, 1.05F),
				sclL(0.4F, 1.1F, 1.34F, 1.1F),
				sclL(0.8F, 1.02F, 1.18F, 1.02F),
				sclL(1.2F, 1.1F, 1.32F, 1.1F),
				sclL(1.6F, 1.05F, 1.24F, 1.05F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 36.0F, 12.0F, 24.0F),
				rotL(0.4F, 30.0F, 12.0F, 28.4F),
				rotL(0.8F, 36.0F, 12.0F, 24.0F),
				rotL(1.2F, 42.0F, 12.0F, 20.4F),
				rotL(1.6F, 36.0F, 12.0F, 24.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 22.0F, 0.0F, 0.0F),
				rotL(0.4F, 22.0F, 0.0F, 0.0F),
				rotL(0.8F, 22.0F, 0.0F, 0.0F),
				rotL(1.2F, 22.0F, 0.0F, 0.0F),
				rotL(1.6F, 22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 14.0F, 0.0F, 0.0F),
				rotL(0.4F, 17.3F, 0.0F, 0.0F),
				rotL(0.8F, 14.0F, 0.0F, 0.0F),
				rotL(1.2F, 11.3F, 0.0F, 0.0F),
				rotL(1.6F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 36.0F, -12.0F, -24.0F),
				rotL(0.4F, 42.0F, -12.0F, -28.4F),
				rotL(0.8F, 36.0F, -12.0F, -24.0F),
				rotL(1.2F, 30.0F, -12.0F, -20.4F),
				rotL(1.6F, 36.0F, -12.0F, -24.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 22.0F, 0.0F, 0.0F),
				rotL(0.4F, 22.0F, 0.0F, 0.0F),
				rotL(0.8F, 22.0F, 0.0F, 0.0F),
				rotL(1.2F, 22.0F, 0.0F, 0.0F),
				rotL(1.6F, 22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 14.0F, 0.0F, 0.0F),
				rotL(0.4F, 17.3F, 0.0F, 0.0F),
				rotL(0.8F, 14.0F, 0.0F, 0.0F),
				rotL(1.2F, 11.3F, 0.0F, 0.0F),
				rotL(1.6F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 28.0F, 0.0F, 0.0F),
				rotL(0.4F, 28.0F, 0.0F, 12.0F),
				rotL(0.8F, 28.0F, 0.0F, 0.0F),
				rotL(1.2F, 28.0F, 0.0F, -12.0F),
				rotL(1.6F, 28.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 14.0F, 0.0F, 0.0F),
				rotL(0.4F, 14.0F, 0.0F, 16.0F),
				rotL(0.8F, 14.0F, 0.0F, 0.0F),
				rotL(1.2F, 14.0F, 0.0F, -16.0F),
				rotL(1.6F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -16.0F, 0.0F, 0.0F),
				rotL(0.4F, -16.0F, 0.0F, -18.0F),
				rotL(0.8F, -16.0F, 0.0F, 0.0F),
				rotL(1.2F, -16.0F, 0.0F, 18.0F),
				rotL(1.6F, -16.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 8.0F, 0.0F, 0.0F),
				rotL(0.4F, 8.0F, 0.0F, -16.0F),
				rotL(0.8F, 8.0F, 0.0F, 0.0F),
				rotL(1.2F, 8.0F, 0.0F, 16.0F),
				rotL(1.6F, 8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 12.0F, 0.0F, 0.0F),
				rotL(0.4F, 12.0F, 0.0F, 20.0F),
				rotL(0.8F, 12.0F, 0.0F, 0.0F),
				rotL(1.2F, 12.0F, 0.0F, -20.0F),
				rotL(1.6F, 12.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
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
			return b.build();
		}
	}

	private static final class GlideLandingAnimBuilder {
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
				rotL(0.0F, 30.0F, 0.0F, 0.0F),
				rotL(0.48F, -16.0F, 0.0F, 0.0F),
				rotL(0.82F, -5.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 7.5F, 0.0F),
				posL(0.48F, 0.0F, 2.2F, 0.8F),
				posL(0.82F, 0.0F, -0.7F, 0.3F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, 0.0F, 0.0F),
				rotL(0.48F, -10.0F, 0.0F, 0.0F),
				rotL(0.82F, -4.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -10.0F, 0.0F, 0.0F),
				rotL(0.48F, 8.0F, 0.0F, 0.0F),
				rotL(0.82F, 4.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -38.0F, 0.0F, 0.0F),
				rotL(0.48F, 14.0F, 0.0F, 0.0F),
				rotL(0.82F, 4.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.05F, 1.24F, 1.05F),
				sclL(0.48F, 1.12F, 1.28F, 1.12F),
				sclL(0.82F, 0.96F, 0.92F, 0.96F),
				sclL(1.2F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 36.0F, 12.0F, 24.0F),
				rotL(0.48F, -36.0F, -8.0F, 26.0F),
				rotL(0.82F, -12.0F, -4.0F, 10.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 22.0F, 0.0F, 0.0F),
				rotL(0.48F, 12.0F, 0.0F, 0.0F),
				rotL(0.82F, -8.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 14.0F, 0.0F, 0.0F),
				rotL(0.48F, 28.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 36.0F, -12.0F, -24.0F),
				rotL(0.48F, -36.0F, 8.0F, -26.0F),
				rotL(0.82F, -12.0F, 4.0F, -10.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 22.0F, 0.0F, 0.0F),
				rotL(0.48F, 12.0F, 0.0F, 0.0F),
				rotL(0.82F, -8.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 14.0F, 0.0F, 0.0F),
				rotL(0.48F, 28.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.2F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 28.0F, 0.0F, 0.0F),
				rotL(0.48F, -16.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.2F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 14.0F, 0.0F, 0.0F),
				rotL(0.48F, -12.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -16.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 8.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 12.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.2F);
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
			return b.build();
		}
	}

	private static final class AttackAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, -12.0F, 0.0F, 0.0F),
				rotL(0.78F, 18.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.5F, 0.0F, 1.2F, 1.2F),
				posL(0.78F, 0.0F, 0.4F, -2.4F),
				posL(1.05F, 0.0F, 0.2F, -1.2F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, -12.0F, 0.0F, 0.0F),
				rotL(0.78F, 14.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 6.0F, 0.0F, 0.0F),
				rotL(0.78F, -8.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 8.0F, 0.0F, 0.0F),
				rotL(0.78F, -24.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.5F, 1.15F, 1.32F, 1.15F),
				sclL(0.78F, 1.2F, 1.4F, 1.2F),
				sclL(1.5F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, -54.0F, -28.0F, -22.0F),
				rotL(0.78F, -72.0F, -8.0F, -10.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, -32.0F, -20.0F, 0.0F),
				rotL(0.78F, 18.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 14.0F, 0.0F, 0.0F),
				rotL(0.78F, 32.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, -54.0F, 28.0F, 22.0F),
				rotL(0.78F, -72.0F, 8.0F, 10.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, -32.0F, 20.0F, 0.0F),
				rotL(0.78F, 18.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 14.0F, 0.0F, 0.0F),
				rotL(0.78F, 32.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.5F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.78F, 22.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.5F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.78F, 14.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.5F);
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
			return b.build();
		}
	}

	private static final class PhaseGlideAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 24.0F, -18.0F, 0.0F),
				rotL(0.9F, 24.0F, 18.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.45F, 0.0F, 0.8F, -1.5F),
				posL(0.9F, 0.0F, 0.8F, -3.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 8.0F, -14.0F, 0.0F),
				rotL(0.9F, 8.0F, 14.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, -28.0F, 24.0F, 0.0F),
				rotL(0.9F, -28.0F, -24.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.45F, 0.85F, 1.35F, 0.85F),
				sclL(0.9F, 0.85F, 1.35F, 0.85F),
				sclL(1.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, -32.0F, -30.0F, -22.0F),
				rotL(0.9F, -32.0F, -30.0F, -22.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, -44.0F, -34.0F, 0.0F),
				rotL(0.9F, -44.0F, -34.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, -32.0F, 30.0F, 22.0F),
				rotL(0.9F, -32.0F, 30.0F, 22.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, -44.0F, 34.0F, 0.0F),
				rotL(0.9F, -44.0F, 34.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.45F, 0.68F, 1.05F, 0.75F),
				sclL(0.9F, 0.68F, 1.05F, 0.75F),
				sclL(1.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 24.0F, 0.0F, 14.0F),
				rotL(0.9F, 24.0F, 0.0F, -14.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.45F, 0.65F, 1.1F, 0.75F),
				sclL(0.9F, 0.65F, 1.1F, 0.75F),
				sclL(1.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 14.0F, 0.0F, -16.0F),
				rotL(0.9F, 14.0F, 0.0F, 16.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.4F);
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
			return b.build();
		}
	}

	private static final class HurtAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -14.0F, 8.0F, 5.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.16F, 0.0F, 0.5F, 1.2F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -12.0F, 6.0F, 4.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -8.0F, -6.0F, -5.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -16.0F, -14.0F, -8.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.16F, 0.75F, 0.7F, 0.75F),
				sclL(0.36F, 1.12F, 1.22F, 1.12F),
				sclL(0.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 22.0F, 16.0F, 24.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 14.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 18.0F, -16.0F, -24.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 14.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
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
			return b.build();
		}
	}

	private static final class DeathAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.425F, 0.0F, 0.0F, 0.0F),
				posL(0.45F, 0.0F, 0.0F, 0.0F),
				posL(0.475F, 0.0F, -0.038F, -0.012F),
				posL(0.5F, 0.0F, -0.152F, -0.047F),
				posL(0.525F, 0.0F, -0.343F, -0.102F),
				posL(0.55F, 0.0F, -0.611F, -0.177F),
				posL(0.575F, 0.0F, -0.958F, -0.27F),
				posL(0.6F, 0.0F, -1.383F, -0.378F),
				posL(0.625F, 0.0F, -1.887F, -0.5F),
				posL(0.65F, 0.0F, -2.468F, -0.634F),
				posL(0.675F, 0.0F, -3.126F, -0.779F),
				posL(0.7F, 0.0F, -3.856F, -0.933F),
				posL(0.725F, 0.0F, -4.574F, -1.094F),
				posL(0.75F, 0.0F, -5.042F, -1.259F),
				posL(0.775F, 0.0F, -5.502F, -1.429F),
				posL(0.8F, 0.0F, -5.785F, -1.6F),
				posL(0.825F, 0.0F, -5.99F, -1.771F),
				posL(0.85F, 0.0F, -6.17F, -1.941F),
				posL(0.875F, 0.0F, -6.341F, -2.106F),
				posL(0.9F, 0.0F, -6.52F, -2.267F),
				posL(0.925F, 0.0F, -6.724F, -2.421F),
				posL(0.95F, 0.0F, -6.968F, -2.566F),
				posL(0.975F, 0.0F, -7.267F, -2.7F),
				posL(1.0F, 0.0F, -7.632F, -2.822F),
				posL(1.025F, 0.0F, -8.067F, -2.93F),
				posL(1.05F, 0.0F, -8.576F, -3.023F),
				posL(1.075F, 0.0F, -9.073F, -3.098F),
				posL(1.1F, 0.0F, -9.619F, -3.153F),
				posL(1.125F, 0.0F, -10.208F, -3.188F),
				posL(1.15F, 0.0F, -10.822F, -3.2F),
				posL(1.175F, 0.0F, -10.827F, -3.2F),
				posL(1.2F, 0.0F, -10.832F, -3.2F),
				posL(1.225F, 0.0F, -10.837F, -3.2F),
				posL(1.25F, 0.0F, -10.842F, -3.2F),
				posL(1.275F, 0.0F, -10.847F, -3.2F),
				posL(1.3F, 0.0F, -10.851F, -3.2F),
				posL(1.325F, 0.0F, -10.856F, -3.2F),
				posL(1.35F, 0.0F, -10.861F, -3.2F),
				posL(1.375F, 0.0F, -10.866F, -3.2F),
				posL(1.4F, 0.0F, -10.871F, -3.2F),
				posL(1.425F, 0.0F, -10.877F, -3.2F),
				posL(1.45F, 0.0F, -10.882F, -3.2F),
				posL(1.475F, 0.0F, -10.887F, -3.2F),
				posL(1.5F, 0.0F, -10.892F, -3.2F),
				posL(1.525F, 0.0F, -10.897F, -3.2F),
				posL(1.55F, 0.0F, -10.902F, -3.2F),
				posL(1.575F, 0.0F, -10.908F, -3.2F),
				posL(1.6F, 0.0F, -10.913F, -3.2F),
				posL(1.625F, 0.0F, -10.918F, -3.2F),
				posL(1.65F, 0.0F, -10.924F, -3.2F),
				posL(1.675F, 0.0F, -10.929F, -3.2F),
				posL(1.7F, 0.0F, -10.935F, -3.2F),
				posL(1.725F, 0.0F, -10.94F, -3.2F),
				posL(1.75F, 0.0F, -10.946F, -3.2F),
				posL(1.775F, 0.0F, -10.951F, -3.2F),
				posL(1.8F, 0.0F, -10.957F, -3.2F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -18.0F, 10.0F, 6.0F),
				rotL(1.15F, 78.72F, 0.0F, 4.0F),
				rotL(1.8F, 82.0F, 0.0F, 4.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.4F, 0.0F, 1.6F, 0.8F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -14.0F, 8.0F, 4.0F),
				rotL(1.15F, 4.0F, 0.0F, 0.0F),
				rotL(1.8F, 4.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, -14.0F, 0.0F, 0.0F),
				rotL(1.8F, -14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("collar_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -24.0F, -12.0F, -8.0F),
				rotL(1.15F, -12.0F, -18.0F, -6.0F),
				rotL(1.8F, -12.0F, -18.0F, -6.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("crown_flame_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.4F, 1.2F, 1.38F, 1.2F),
				sclL(1.15F, 0.12F, 0.08F, 0.12F),
				sclL(1.8F, 0.12F, 0.08F, 0.12F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -76.0F, 18.0F, 28.0F),
				rotL(1.15F, -6.0F, 32.0F, 36.0F),
				rotL(1.8F, -6.0F, 32.0F, 36.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 26.0F, 0.0F, 10.0F),
				rotL(1.8F, 26.0F, 0.0F, 10.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hand_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -72.0F, -18.0F, -28.0F),
				rotL(1.15F, -4.0F, -32.0F, -36.0F),
				rotL(1.8F, -4.0F, -32.0F, -36.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 24.0F, 0.0F, -10.0F),
				rotL(1.8F, 24.0F, 0.0F, -10.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("hand_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("ribs_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.15F, 0.35F, 0.28F, 0.35F),
				sclL(1.8F, 0.35F, 0.28F, 0.35F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.15F, 0.18F, 0.18F, 0.18F),
				sclL(1.8F, 0.18F, 0.18F, 0.18F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
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
			return b.build();
		}
	}

}
