package ddraig.net.entropica.client.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Exact Blockbench keyframe animations for Brimstone Chimera (Fauna 58 — Lava Beast).
 * Split into per-animation static builders and <=60-keyframe helpers so constant pools and method bytecodes stay tiny.
 */
public final class BrimstoneChimeraAnimations {

	private BrimstoneChimeraAnimations() {}

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
	public static final AnimationDefinition RUN = RunAnimBuilder.create();
	public static final AnimationDefinition run = RUN;
	public static final AnimationDefinition ATTACK = AttackAnimBuilder.create();
	public static final AnimationDefinition attack = ATTACK;
	public static final AnimationDefinition MAGMA_BREATH = MagmaBreathAnimBuilder.create();
	public static final AnimationDefinition magma_breath = MAGMA_BREATH;
	public static final AnimationDefinition HURT = HurtAnimBuilder.create();
	public static final AnimationDefinition hurt = HURT;
	public static final AnimationDefinition DEATH = DeathAnimBuilder.create();
	public static final AnimationDefinition death = DEATH;

	private static final class IdleAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.16F, 0.0F),
				posL(1.6F, 0.0F, 0.32F, 0.0F),
				posL(2.4F, 0.0F, 0.14F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 3.0F, -8.0F, 2.0F),
				rotL(1.6F, -3.0F, 0.0F, 0.0F),
				rotL(2.4F, 2.0F, 8.0F, -2.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 14.0F, 0.0F, 0.0F),
				rotL(1.6F, -2.0F, 0.0F, 0.0F),
				rotL(2.4F, 8.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, -3.0F, -4.0F, 0.0F),
				rotL(2.3F, 3.0F, 4.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, -4.0F, 10.0F, -4.0F),
				rotL(1.8F, 4.0F, -12.0F, 5.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 18.0F, 0.0F, 0.0F),
				rotL(2.3F, -4.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, -2.0F, -8.0F, 0.0F),
				rotL(1.6F, 0.8F, 3.2F, 0.0F),
				rotL(2.4F, 2.0F, 8.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 2.0F, -10.0F, 0.0F),
				rotL(1.6F, -0.8F, 4.0F, 0.0F),
				rotL(2.4F, -2.0F, 10.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, -3.0F, -6.0F, 0.0F),
				rotL(1.6F, 1.2F, 2.4F, 0.0F),
				rotL(2.4F, 3.0F, 6.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_52(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 3.0F, 8.0F, 0.0F),
				rotL(1.6F, -1.2F, -3.2F, 0.0F),
				rotL(2.4F, -3.0F, -8.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_53(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_54(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 10.0F, 0.0F),
				rotL(1.6F, 0.0F, -4.0F, 0.0F),
				rotL(2.4F, 0.0F, -10.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_55(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_56(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_57(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_58(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_59(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_60(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 4.0F, -10.0F, 0.0F),
				rotL(1.6F, -1.6F, 4.0F, 0.0F),
				rotL(2.4F, -4.0F, 10.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_61(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_62(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, -10.0F, 0.0F, 0.0F),
				rotL(1.4F, 22.0F, 0.0F, 0.0F),
				rotL(1.8F, -4.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_63(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(3.2F).looping();
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
			ch_52(b);
			ch_53(b);
			ch_54(b);
			ch_55(b);
			ch_56(b);
			ch_57(b);
			ch_58(b);
			ch_59(b);
			ch_60(b);
			ch_61(b);
			ch_62(b);
			ch_63(b);
			return b.build();
		}
	}

	private static final class WalkAnimBuilder {
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
				rotL(0.18F, 1.5F, -2.0F, 1.5F),
				rotL(0.35F, -1.2F, -1.6F, 1.2F),
				rotL(0.52F, 0.5F, -0.4F, 0.3F),
				rotL(0.7F, 1.2F, 1.2F, -0.9F),
				rotL(0.88F, 1.5F, 2.0F, -1.5F),
				rotL(1.05F, -1.2F, 1.6F, -1.2F),
				rotL(1.24F, 0.5F, -0.4F, 0.3F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.025F, 0.0F, 0.067F, 0.0F),
				posL(0.05F, 0.0F, 0.102F, 0.0F),
				posL(0.075F, 0.0F, 0.122F, 0.0F),
				posL(0.1F, 0.0F, 0.125F, 0.0F),
				posL(0.125F, 0.0F, 0.112F, 0.0F),
				posL(0.15F, 0.0F, 0.082F, 0.0F),
				posL(0.175F, 0.0F, 0.036F, 0.0F),
				posL(0.2F, 0.0F, -0.062F, 0.0F),
				posL(0.225F, 0.0F, -0.177F, 0.0F),
				posL(0.25F, 0.0F, -0.298F, 0.0F),
				posL(0.275F, 0.0F, -0.315F, 0.0F),
				posL(0.3F, 0.0F, -0.204F, 0.0F),
				posL(0.325F, 0.0F, -0.1F, 0.0F),
				posL(0.35F, 0.0F, -0.003F, 0.0F),
				posL(0.375F, 0.0F, 0.037F, 0.0F),
				posL(0.4F, 0.0F, 0.063F, 0.0F),
				posL(0.425F, 0.0F, 0.075F, 0.0F),
				posL(0.45F, 0.0F, 0.073F, 0.0F),
				posL(0.475F, 0.0F, 0.06F, 0.0F),
				posL(0.5F, 0.0F, 0.047F, 0.0F),
				posL(0.525F, 0.0F, 0.054F, 0.0F),
				posL(0.55F, 0.0F, 0.087F, 0.0F),
				posL(0.575F, 0.0F, 0.113F, 0.0F),
				posL(0.6F, 0.0F, 0.134F, 0.0F),
				posL(0.625F, 0.0F, 0.144F, 0.0F),
				posL(0.65F, 0.0F, 0.143F, 0.0F),
				posL(0.675F, 0.0F, 0.13F, 0.0F),
				posL(0.7F, 0.0F, 0.105F, 0.0F),
				posL(0.725F, 0.0F, 0.073F, 0.0F),
				posL(0.75F, 0.0F, 0.035F, 0.0F),
				posL(0.775F, 0.0F, -0.01F, 0.0F),
				posL(0.8F, 0.0F, -0.06F, 0.0F),
				posL(0.825F, 0.0F, -0.117F, 0.0F),
				posL(0.85F, 0.0F, -0.18F, 0.0F),
				posL(0.875F, 0.0F, -0.249F, 0.0F),
				posL(0.9F, 0.0F, -0.329F, 0.0F),
				posL(0.925F, 0.0F, -0.412F, 0.0F),
				posL(0.95F, 0.0F, -0.349F, 0.0F),
				posL(0.975F, 0.0F, -0.271F, 0.0F),
				posL(1.0F, 0.0F, -0.175F, 0.0F),
				posL(1.025F, 0.0F, -0.085F, 0.0F),
				posL(1.05F, 0.0F, -0.003F, 0.0F),
				posL(1.075F, 0.0F, 0.026F, 0.0F),
				posL(1.1F, 0.0F, 0.044F, 0.0F),
				posL(1.125F, 0.0F, 0.051F, 0.0F),
				posL(1.15F, 0.0F, 0.046F, 0.0F),
				posL(1.175F, 0.0F, 0.029F, 0.0F),
				posL(1.2F, 0.0F, 0.012F, 0.0F),
				posL(1.225F, 0.0F, 0.037F, 0.0F),
				posL(1.25F, 0.0F, 0.082F, 0.0F),
				posL(1.275F, 0.0F, 0.073F, 0.0F),
				posL(1.3F, 0.0F, 0.063F, 0.0F),
				posL(1.325F, 0.0F, 0.052F, 0.0F),
				posL(1.35F, 0.0F, 0.04F, 0.0F),
				posL(1.375F, 0.0F, 0.028F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, -0.75F, -2.0F, 0.0F),
				rotL(0.35F, 0.6F, -1.6F, 0.0F),
				rotL(0.52F, -0.25F, -0.4F, 0.0F),
				rotL(0.7F, -0.6F, 1.2F, 0.0F),
				rotL(0.88F, -0.75F, 2.0F, 0.0F),
				rotL(1.05F, 0.6F, 1.6F, 0.0F),
				rotL(1.24F, -0.25F, -0.4F, 0.0F),
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
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, -1.2F, 3.0F, 0.0F),
				rotL(0.35F, 0.96F, 2.4F, 0.0F),
				rotL(0.52F, -0.4F, 0.6F, 0.0F),
				rotL(0.7F, -0.96F, -1.8F, 0.0F),
				rotL(0.88F, -1.2F, -3.0F, 0.0F),
				rotL(1.05F, 0.96F, -2.4F, 0.0F),
				rotL(1.24F, -0.4F, 0.6F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, 1.8F, -4.0F, 0.0F),
				rotL(0.35F, -1.44F, -3.2F, 0.0F),
				rotL(0.52F, 0.6F, -0.8F, 0.0F),
				rotL(0.7F, 1.44F, 2.4F, 0.0F),
				rotL(0.88F, 1.8F, 4.0F, 0.0F),
				rotL(1.05F, -1.44F, 3.2F, 0.0F),
				rotL(1.24F, 0.6F, -0.8F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, -24.0F, 0.0F, 0.0F),
				rotL(0.35F, -20.0F, 0.0F, 0.0F),
				rotL(0.52F, -4.0F, 0.0F, 0.0F),
				rotL(0.7F, 8.0F, 0.0F, 0.0F),
				rotL(0.88F, 16.0F, 0.0F, 0.0F),
				rotL(1.05F, 20.0F, 0.0F, 0.0F),
				rotL(1.24F, -8.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.18F, 0.0F, 0.9F, 0.0F),
				posL(0.35F, 0.0F, 0.0F, 0.0F),
				posL(0.52F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F),
				posL(1.05F, 0.0F, 0.0F, 0.0F),
				posL(1.24F, 0.0F, 0.7F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, 28.0F, 0.0F, 0.0F),
				rotL(0.35F, 6.0F, 0.0F, 0.0F),
				rotL(0.52F, 4.0F, 0.0F, 0.0F),
				rotL(0.7F, 4.0F, 0.0F, 0.0F),
				rotL(0.88F, 6.0F, 0.0F, 0.0F),
				rotL(1.05F, 8.0F, 0.0F, 0.0F),
				rotL(1.24F, 24.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, -4.0F, 0.0F, 0.0F),
				rotL(0.35F, 14.0F, 0.0F, 0.0F),
				rotL(0.52F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, -12.0F, 0.0F, 0.0F),
				rotL(0.88F, -22.0F, 0.0F, 0.0F),
				rotL(1.05F, -28.0F, 0.0F, 0.0F),
				rotL(1.24F, -16.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, 12.0F, 0.0F, 0.0F),
				rotL(0.35F, 20.0F, 0.0F, 0.0F),
				rotL(0.52F, -8.0F, 0.0F, 0.0F),
				rotL(0.7F, -20.0F, 0.0F, 0.0F),
				rotL(0.88F, -24.0F, 0.0F, 0.0F),
				rotL(1.05F, -20.0F, 0.0F, 0.0F),
				rotL(1.24F, -4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.18F, 0.0F, 0.0F, 0.0F),
				posL(0.35F, 0.0F, 0.0F, 0.0F),
				posL(0.52F, 0.0F, 0.7F, 0.0F),
				posL(0.7F, 0.0F, 0.8F, 0.0F),
				posL(0.88F, 0.0F, 0.8F, 0.0F),
				posL(1.05F, 0.0F, 0.0F, 0.0F),
				posL(1.24F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, 4.0F, 0.0F, 0.0F),
				rotL(0.35F, 8.0F, 0.0F, 0.0F),
				rotL(0.52F, 26.0F, 0.0F, 0.0F),
				rotL(0.7F, 24.0F, 0.0F, 0.0F),
				rotL(0.88F, 26.0F, 0.0F, 0.0F),
				rotL(1.05F, 6.0F, 0.0F, 0.0F),
				rotL(1.24F, 4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, -16.0F, 0.0F, 0.0F),
				rotL(0.35F, -28.0F, 0.0F, 0.0F),
				rotL(0.52F, -18.0F, 0.0F, 0.0F),
				rotL(0.7F, -4.0F, 0.0F, 0.0F),
				rotL(0.88F, -2.0F, 0.0F, 0.0F),
				rotL(1.05F, 14.0F, 0.0F, 0.0F),
				rotL(1.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, 12.0F, 0.0F, 0.0F),
				rotL(0.35F, 20.0F, 0.0F, 0.0F),
				rotL(0.52F, -8.0F, 0.0F, 0.0F),
				rotL(0.7F, -18.0F, 0.0F, 0.0F),
				rotL(0.88F, -22.0F, 0.0F, 0.0F),
				rotL(1.05F, -18.0F, 0.0F, 0.0F),
				rotL(1.24F, -4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.18F, 0.0F, 0.0F, 0.0F),
				posL(0.35F, 0.0F, 0.0F, 0.0F),
				posL(0.52F, 0.0F, 0.6F, 0.0F),
				posL(0.7F, 0.0F, 0.7F, 0.0F),
				posL(0.88F, 0.0F, 0.7F, 0.0F),
				posL(1.05F, 0.0F, 0.0F, 0.0F),
				posL(1.24F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, -6.0F, 0.0F, 0.0F),
				rotL(0.35F, -10.0F, 0.0F, 0.0F),
				rotL(0.52F, 20.0F, 0.0F, 0.0F),
				rotL(0.7F, 20.0F, 0.0F, 0.0F),
				rotL(0.88F, 22.0F, 0.0F, 0.0F),
				rotL(1.05F, 6.0F, 0.0F, 0.0F),
				rotL(1.24F, -2.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, -6.0F, 0.0F, 0.0F),
				rotL(0.35F, -10.0F, 0.0F, 0.0F),
				rotL(0.52F, -12.0F, 0.0F, 0.0F),
				rotL(0.7F, -2.0F, 0.0F, 0.0F),
				rotL(0.88F, 0.0F, 0.0F, 0.0F),
				rotL(1.05F, 12.0F, 0.0F, 0.0F),
				rotL(1.24F, 6.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, -22.0F, 0.0F, 0.0F),
				rotL(0.35F, -18.0F, 0.0F, 0.0F),
				rotL(0.52F, -4.0F, 0.0F, 0.0F),
				rotL(0.7F, 8.0F, 0.0F, 0.0F),
				rotL(0.88F, 16.0F, 0.0F, 0.0F),
				rotL(1.05F, 20.0F, 0.0F, 0.0F),
				rotL(1.24F, -8.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.18F, 0.0F, 0.8F, 0.0F),
				posL(0.35F, 0.0F, 0.0F, 0.0F),
				posL(0.52F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F),
				posL(0.88F, 0.0F, 0.0F, 0.0F),
				posL(1.05F, 0.0F, 0.0F, 0.0F),
				posL(1.24F, 0.0F, 0.6F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, 22.0F, 0.0F, 0.0F),
				rotL(0.35F, 6.0F, 0.0F, 0.0F),
				rotL(0.52F, -2.0F, 0.0F, 0.0F),
				rotL(0.7F, -4.0F, 0.0F, 0.0F),
				rotL(0.88F, -8.0F, 0.0F, 0.0F),
				rotL(1.05F, -10.0F, 0.0F, 0.0F),
				rotL(1.24F, 18.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, 12.0F, 0.0F, 0.0F),
				rotL(0.52F, 6.0F, 0.0F, 0.0F),
				rotL(0.7F, -4.0F, 0.0F, 0.0F),
				rotL(0.88F, -8.0F, 0.0F, 0.0F),
				rotL(1.05F, -10.0F, 0.0F, 0.0F),
				rotL(1.24F, -10.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, 3.0F, -8.0F, 0.0F),
				rotL(0.35F, -2.4F, -6.4F, 0.0F),
				rotL(0.52F, 1.0F, -1.6F, 0.0F),
				rotL(0.7F, 2.4F, 4.8F, 0.0F),
				rotL(0.88F, 3.0F, 8.0F, 0.0F),
				rotL(1.05F, -2.4F, 6.4F, 0.0F),
				rotL(1.24F, 1.0F, -1.6F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, 0.0F, 10.0F, 0.0F),
				rotL(0.35F, 0.0F, 8.0F, 0.0F),
				rotL(0.52F, 0.0F, 2.0F, 0.0F),
				rotL(0.7F, 0.0F, -6.0F, 0.0F),
				rotL(0.88F, 0.0F, -10.0F, 0.0F),
				rotL(1.05F, 0.0F, -8.0F, 0.0F),
				rotL(1.24F, 0.0F, 2.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_52(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_53(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_54(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_55(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_56(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_57(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_58(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_59(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_60(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.18F, 0.0F, -8.0F, 0.0F),
				rotL(0.35F, 0.0F, -6.4F, 0.0F),
				rotL(0.52F, 0.0F, -1.6F, 0.0F),
				rotL(0.7F, 0.0F, 4.8F, 0.0F),
				rotL(0.88F, 0.0F, 8.0F, 0.0F),
				rotL(1.05F, 0.0F, 6.4F, 0.0F),
				rotL(1.24F, 0.0F, -1.6F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_61(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_62(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_63(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.4F).looping();
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
			ch_52(b);
			ch_53(b);
			ch_54(b);
			ch_55(b);
			ch_56(b);
			ch_57(b);
			ch_58(b);
			ch_59(b);
			ch_60(b);
			ch_61(b);
			ch_62(b);
			ch_63(b);
			return b.build();
		}
	}

	private static final class RunAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -8.0F, 0.0F, 0.0F),
				rotL(0.48F, 6.0F, 0.0F, 0.0F),
				rotL(0.76F, 4.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.025F, 0.0F, 0.07F, 0.0F),
				posL(0.05F, 0.0F, 0.12F, 0.0F),
				posL(0.075F, 0.0F, 0.165F, 0.0F),
				posL(0.1F, 0.0F, 0.206F, 0.0F),
				posL(0.125F, 0.0F, 0.242F, 0.0F),
				posL(0.15F, 0.0F, 0.396F, 0.0F),
				posL(0.175F, 0.0F, 0.728F, 0.0F),
				posL(0.2F, 0.0F, 1.048F, 0.0F),
				posL(0.225F, 0.0F, 1.302F, 0.0F),
				posL(0.25F, 0.0F, 1.339F, 0.0F),
				posL(0.275F, 0.0F, 1.335F, 0.0F),
				posL(0.3F, 0.0F, 1.284F, 0.0F),
				posL(0.325F, 0.0F, 1.242F, 0.0F),
				posL(0.35F, 0.0F, 1.172F, 0.0F),
				posL(0.375F, 0.0F, 1.108F, 0.0F),
				posL(0.4F, 0.0F, 1.136F, 0.0F),
				posL(0.425F, 0.0F, 1.367F, 0.0F),
				posL(0.45F, 0.0F, 1.552F, 0.0F),
				posL(0.475F, 0.0F, 1.691F, 0.0F),
				posL(0.5F, 0.0F, 1.836F, 0.0F),
				posL(0.525F, 0.0F, 1.915F, 0.0F),
				posL(0.55F, 0.0F, 1.914F, 0.0F),
				posL(0.575F, 0.0F, 1.837F, 0.0F),
				posL(0.6F, 0.0F, 1.686F, 0.0F),
				posL(0.625F, 0.0F, 1.467F, 0.0F),
				posL(0.65F, 0.0F, 1.185F, 0.0F),
				posL(0.675F, 0.0F, 0.841F, 0.0F),
				posL(0.7F, 0.0F, 0.447F, 0.0F),
				posL(0.725F, 0.0F, 0.315F, 0.0F),
				posL(0.75F, 0.0F, 0.137F, 0.0F),
				posL(0.775F, 0.0F, 0.087F, 0.0F),
				posL(0.8F, 0.0F, 0.132F, 0.0F),
				posL(0.825F, 0.0F, 0.165F, 0.0F),
				posL(0.85F, 0.0F, 0.184F, 0.0F),
				posL(0.875F, 0.0F, 0.189F, 0.0F),
				posL(0.9F, 0.0F, 0.181F, 0.0F),
				posL(0.925F, 0.0F, 0.16F, 0.0F),
				posL(0.95F, 0.0F, 0.125F, 0.0F),
				posL(0.975F, 0.0F, 0.077F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -4.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 8.0F, 0.0F, 0.0F),
				rotL(0.48F, -8.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 14.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 6.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -34.0F, 0.0F, 0.0F),
				rotL(0.48F, -38.0F, 0.0F, 0.0F),
				rotL(0.76F, 16.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 36.0F, 0.0F, 0.0F),
				rotL(0.48F, 14.0F, 0.0F, 0.0F),
				rotL(0.76F, 8.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 24.0F, 0.0F, 0.0F),
				rotL(0.76F, -24.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -26.0F, 0.0F, 0.0F),
				rotL(0.48F, -30.0F, 0.0F, 0.0F),
				rotL(0.76F, 10.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 30.0F, 0.0F, 0.0F),
				rotL(0.48F, 12.0F, 0.0F, 0.0F),
				rotL(0.76F, 10.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 18.0F, 0.0F, 0.0F),
				rotL(0.76F, -20.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 20.0F, 0.0F, 0.0F),
				rotL(0.48F, 28.0F, 0.0F, 0.0F),
				rotL(0.76F, -22.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -14.0F, 0.0F, 0.0F),
				rotL(0.48F, -18.0F, 0.0F, 0.0F),
				rotL(0.76F, 22.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -6.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, 24.0F, 0.0F, 0.0F),
				rotL(0.48F, 22.0F, 0.0F, 0.0F),
				rotL(0.76F, -18.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -16.0F, 0.0F, 0.0F),
				rotL(0.48F, -16.0F, 0.0F, 0.0F),
				rotL(0.76F, 18.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -8.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.22F, -12.0F, 0.0F, 0.0F),
				rotL(0.48F, 12.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_52(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, -14.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_53(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_54(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_55(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_56(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_57(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_58(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_59(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_60(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_61(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_62(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_63(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.0F).looping();
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
			ch_52(b);
			ch_53(b);
			ch_54(b);
			ch_55(b);
			ch_56(b);
			ch_57(b);
			ch_58(b);
			ch_59(b);
			ch_60(b);
			ch_61(b);
			ch_62(b);
			ch_63(b);
			return b.build();
		}
	}

	private static final class AttackAnimBuilder {
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
				rotL(0.42F, -14.0F, -5.0F, 0.0F),
				rotL(0.62F, 6.0F, 4.0F, 0.0F),
				rotL(0.8F, 8.0F, 3.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3_p0(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.02F, 0.0F, 0.095F, 0.048F),
				posL(0.04F, 0.0F, 0.164F, 0.095F),
				posL(0.06F, 0.0F, 0.232F, 0.143F),
				posL(0.08F, 0.0F, 0.299F, 0.19F),
				posL(0.1F, 0.0F, 0.364F, 0.238F),
				posL(0.12F, 0.0F, 0.428F, 0.286F),
				posL(0.14F, 0.0F, 0.491F, 0.333F),
				posL(0.16F, 0.0F, 0.553F, 0.381F),
				posL(0.18F, 0.0F, 0.613F, 0.429F),
				posL(0.2F, 0.0F, 0.672F, 0.476F),
				posL(0.22F, 0.0F, 0.73F, 0.524F),
				posL(0.24F, 0.0F, 0.787F, 0.571F),
				posL(0.26F, 0.0F, 0.842F, 0.619F),
				posL(0.28F, 0.0F, 0.896F, 0.667F),
				posL(0.3F, 0.0F, 0.948F, 0.714F),
				posL(0.32F, 0.0F, 0.999F, 0.762F),
				posL(0.34F, 0.0F, 1.049F, 0.81F),
				posL(0.36F, 0.0F, 1.098F, 0.857F),
				posL(0.38F, 0.0F, 1.145F, 0.905F),
				posL(0.4F, 0.0F, 1.191F, 0.952F),
				posL(0.42F, 0.0F, 1.235F, 1.0F),
				posL(0.44F, 0.0F, 1.1F, 0.811F),
				posL(0.46F, 0.0F, 0.942F, 0.621F),
				posL(0.48F, 0.0F, 0.76F, 0.432F),
				posL(0.5F, 0.0F, 0.555F, 0.242F),
				posL(0.52F, 0.0F, 0.328F, 0.053F),
				posL(0.54F, 0.0F, 0.087F, -0.137F),
				posL(0.56F, 0.0F, 0.404F, -0.326F),
				posL(0.58F, 0.0F, 0.708F, -0.516F),
				posL(0.6F, 0.0F, 0.999F, -0.705F),
				posL(0.62F, 0.0F, 1.275F, -0.895F),
				posL(0.64F, 0.0F, 1.313F, -1.084F),
				posL(0.66F, 0.0F, 1.351F, -1.274F),
				posL(0.68F, 0.0F, 1.388F, -1.463F),
				posL(0.7F, 0.0F, 1.426F, -1.653F),
				posL(0.72F, 0.0F, 1.463F, -1.842F),
				posL(0.74F, 0.0F, 1.556F, -2.032F),
				posL(0.76F, 0.0F, 1.702F, -2.221F),
				posL(0.78F, 0.0F, 1.8F, -2.411F),
				posL(0.8F, 0.0F, 1.851F, -2.6F),
				posL(0.82F, 0.0F, 1.813F, -2.6F),
				posL(0.84F, 0.0F, 1.775F, -2.6F),
				posL(0.86F, 0.0F, 1.736F, -2.6F),
				posL(0.88F, 0.0F, 1.696F, -2.6F),
				posL(0.9F, 0.0F, 1.657F, -2.6F),
				posL(0.92F, 0.0F, 1.617F, -2.6F),
				posL(0.94F, 0.0F, 1.576F, -2.6F),
				posL(0.96F, 0.0F, 1.535F, -2.6F),
				posL(0.98F, 0.0F, 1.494F, -2.6F),
				posL(1.0F, 0.0F, 1.452F, -2.6F),
				posL(1.02F, 0.0F, 1.41F, -2.6F),
				posL(1.04F, 0.0F, 1.368F, -2.51F),
				posL(1.06F, 0.0F, 1.325F, -2.421F),
				posL(1.08F, 0.0F, 1.282F, -2.331F),
				posL(1.1F, 0.0F, 1.238F, -2.241F),
				posL(1.12F, 0.0F, 1.194F, -2.152F),
				posL(1.14F, 0.0F, 1.15F, -2.062F),
				posL(1.16F, 0.0F, 1.105F, -1.972F),
				posL(1.18F, 0.0F, 1.06F, -1.883F)
			));
		}
		private static void ch_3_p1(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(1.2F, 0.0F, 1.014F, -1.793F),
				posL(1.22F, 0.0F, 0.968F, -1.703F),
				posL(1.24F, 0.0F, 0.922F, -1.614F),
				posL(1.26F, 0.0F, 0.875F, -1.524F),
				posL(1.28F, 0.0F, 0.828F, -1.434F),
				posL(1.3F, 0.0F, 0.781F, -1.345F),
				posL(1.32F, 0.0F, 0.733F, -1.255F),
				posL(1.34F, 0.0F, 0.685F, -1.166F),
				posL(1.36F, 0.0F, 0.636F, -1.076F),
				posL(1.38F, 0.0F, 0.587F, -0.986F),
				posL(1.4F, 0.0F, 0.538F, -0.897F),
				posL(1.42F, 0.0F, 0.489F, -0.807F),
				posL(1.44F, 0.0F, 0.438F, -0.717F),
				posL(1.46F, 0.0F, 0.388F, -0.628F),
				posL(1.48F, 0.0F, 0.337F, -0.538F),
				posL(1.5F, 0.0F, 0.286F, -0.448F),
				posL(1.52F, 0.0F, 0.235F, -0.359F),
				posL(1.54F, 0.0F, 0.183F, -0.269F),
				posL(1.56F, 0.0F, 0.131F, -0.179F),
				posL(1.58F, 0.0F, 0.078F, -0.09F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			List<Keyframe> kfs = new ArrayList<>(81);
			ch_3_p0(kfs);
			ch_3_p1(kfs);
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION, kfs.toArray(new Keyframe[0])));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -8.0F, -6.0F, 0.0F),
				rotL(0.62F, 8.0F, 4.0F, 0.0F),
				rotL(0.8F, 10.0F, 3.0F, 0.0F),
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
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -12.0F, 6.0F, 0.0F),
				rotL(0.62F, 6.0F, -4.0F, 0.0F),
				rotL(0.8F, 12.0F, -4.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 30.0F, 0.0F, 0.0F),
				rotL(0.62F, 26.0F, 0.0F, 0.0F),
				rotL(0.8F, -8.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -10.0F, 0.0F, 0.0F),
				rotL(0.62F, 12.0F, 0.0F, 0.0F),
				rotL(0.8F, 14.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -12.0F, 8.0F, 0.0F),
				rotL(0.62F, 8.0F, -8.0F, 0.0F),
				rotL(0.8F, 10.0F, -8.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 28.0F, 0.0F, 0.0F),
				rotL(0.62F, 24.0F, 0.0F, 0.0F),
				rotL(0.8F, -6.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 10.0F, 0.0F, 0.0F),
				rotL(0.62F, -14.0F, 0.0F, 0.0F),
				rotL(0.8F, -16.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, 16.0F, 0.0F, 0.0F),
				rotL(0.8F, 16.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 4.0F, 0.0F, 0.0F),
				rotL(0.62F, -8.0F, 0.0F, 0.0F),
				rotL(0.8F, -8.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -56.0F, 0.0F, -12.0F),
				rotL(0.62F, -44.0F, -8.0F, 6.0F),
				rotL(0.8F, -18.0F, -6.0F, 6.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 32.0F, 0.0F, 0.0F),
				rotL(0.62F, 12.0F, 0.0F, 0.0F),
				rotL(0.8F, 14.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 18.0F, 0.0F, 0.0F),
				rotL(0.62F, 12.0F, 0.0F, 0.0F),
				rotL(0.8F, -4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 16.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, -12.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, -12.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 16.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, -12.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, -12.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -18.0F, 0.0F, 0.0F),
				rotL(0.62F, 28.0F, 8.0F, 0.0F),
				rotL(0.8F, 26.0F, 6.0F, 0.0F),
				rotL(1.02F, 14.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -10.0F, 0.0F, 0.0F),
				rotL(0.62F, 18.0F, 6.0F, 0.0F),
				rotL(0.8F, 16.0F, 4.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_52(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, -24.0F, -6.0F, 0.0F),
				rotL(0.8F, -22.0F, -4.0F, 0.0F),
				rotL(1.02F, -12.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_53(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_54(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, -24.0F, -6.0F, 0.0F),
				rotL(0.8F, -22.0F, -4.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_55(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_56(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, -18.0F, 0.0F, 0.0F),
				rotL(0.8F, -16.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_57(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_58(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_59(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_60(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 14.0F, 0.0F, 0.0F),
				rotL(0.62F, -18.0F, 0.0F, 0.0F),
				rotL(0.8F, -14.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_61(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_62(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -18.0F, 0.0F, 0.0F),
				rotL(0.62F, -22.0F, 0.0F, 0.0F),
				rotL(0.8F, 26.0F, 0.0F, 0.0F),
				rotL(1.02F, 12.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_63(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
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
			ch_52(b);
			ch_53(b);
			ch_54(b);
			ch_55(b);
			ch_56(b);
			ch_57(b);
			ch_58(b);
			ch_59(b);
			ch_60(b);
			ch_61(b);
			ch_62(b);
			ch_63(b);
			return b.build();
		}
	}

	private static final class MagmaBreathAnimBuilder {
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
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -8.0F, 0.0F, 0.0F),
				rotL(0.62F, 6.0F, 5.5F, 0.0F),
				rotL(0.98F, 6.0F, 0.0F, 0.0F),
				rotL(1.34F, 6.0F, -5.5F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3_p0(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.025F, 0.0F, 0.077F, 0.0F),
				posL(0.05F, 0.0F, 0.133F, 0.0F),
				posL(0.075F, 0.0F, 0.188F, 0.0F),
				posL(0.1F, 0.0F, 0.241F, 0.0F),
				posL(0.125F, 0.0F, 0.293F, 0.0F),
				posL(0.15F, 0.0F, 0.344F, 0.0F),
				posL(0.175F, 0.0F, 0.394F, 0.0F),
				posL(0.2F, 0.0F, 0.442F, 0.0F),
				posL(0.225F, 0.0F, 0.489F, 0.0F),
				posL(0.25F, 0.0F, 0.535F, 0.0F),
				posL(0.275F, 0.0F, 0.579F, 0.0F),
				posL(0.3F, 0.0F, 0.622F, 0.0F),
				posL(0.325F, 0.0F, 0.664F, 0.0F),
				posL(0.35F, 0.0F, 0.704F, 0.0F),
				posL(0.375F, 0.0F, 0.743F, 0.0F),
				posL(0.4F, 0.0F, 0.663F, 0.0F),
				posL(0.425F, 0.0F, 0.544F, 0.0F),
				posL(0.45F, 0.0F, 0.415F, 0.0F),
				posL(0.475F, 0.0F, 0.276F, 0.0F),
				posL(0.5F, 0.0F, 0.237F, 0.0F),
				posL(0.525F, 0.0F, 0.457F, 0.0F),
				posL(0.55F, 0.0F, 0.65F, 0.0F),
				posL(0.575F, 0.0F, 0.811F, 0.0F),
				posL(0.6F, 0.0F, 0.938F, 0.0F),
				posL(0.625F, 0.0F, 1.015F, 0.0F),
				posL(0.65F, 0.0F, 1.012F, 0.0F),
				posL(0.675F, 0.0F, 1.009F, 0.0F),
				posL(0.7F, 0.0F, 1.006F, 0.0F),
				posL(0.725F, 0.0F, 1.003F, 0.0F),
				posL(0.75F, 0.0F, 1.0F, 0.0F),
				posL(0.775F, 0.0F, 0.997F, 0.0F),
				posL(0.8F, 0.0F, 0.994F, 0.0F),
				posL(0.825F, 0.0F, 0.992F, 0.0F),
				posL(0.85F, 0.0F, 0.989F, 0.0F),
				posL(0.875F, 0.0F, 0.986F, 0.0F),
				posL(0.9F, 0.0F, 0.983F, 0.0F),
				posL(0.925F, 0.0F, 0.98F, 0.0F),
				posL(0.95F, 0.0F, 0.977F, 0.0F),
				posL(0.975F, 0.0F, 0.974F, 0.0F),
				posL(1.0F, 0.0F, 0.976F, 0.0F),
				posL(1.025F, 0.0F, 0.979F, 0.0F),
				posL(1.05F, 0.0F, 0.982F, 0.0F),
				posL(1.075F, 0.0F, 0.985F, 0.0F),
				posL(1.1F, 0.0F, 0.988F, 0.0F),
				posL(1.125F, 0.0F, 0.991F, 0.0F),
				posL(1.15F, 0.0F, 0.993F, 0.0F),
				posL(1.175F, 0.0F, 0.996F, 0.0F),
				posL(1.2F, 0.0F, 0.999F, 0.0F),
				posL(1.225F, 0.0F, 1.002F, 0.0F),
				posL(1.25F, 0.0F, 1.005F, 0.0F),
				posL(1.275F, 0.0F, 1.008F, 0.0F),
				posL(1.3F, 0.0F, 1.011F, 0.0F),
				posL(1.325F, 0.0F, 1.014F, 0.0F),
				posL(1.35F, 0.0F, 1.001F, 0.0F),
				posL(1.375F, 0.0F, 0.964F, 0.0F),
				posL(1.4F, 0.0F, 0.926F, 0.0F),
				posL(1.425F, 0.0F, 0.885F, 0.0F),
				posL(1.45F, 0.0F, 0.842F, 0.0F),
				posL(1.475F, 0.0F, 0.797F, 0.0F)
			));
		}
		private static void ch_3_p1(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(1.5F, 0.0F, 0.749F, 0.0F),
				posL(1.525F, 0.0F, 0.7F, 0.0F),
				posL(1.55F, 0.0F, 0.649F, 0.0F),
				posL(1.575F, 0.0F, 0.596F, 0.0F),
				posL(1.6F, 0.0F, 0.54F, 0.0F),
				posL(1.625F, 0.0F, 0.483F, 0.0F),
				posL(1.65F, 0.0F, 0.423F, 0.0F),
				posL(1.675F, 0.0F, 0.361F, 0.0F),
				posL(1.7F, 0.0F, 0.297F, 0.0F),
				posL(1.725F, 0.0F, 0.231F, 0.0F),
				posL(1.75F, 0.0F, 0.163F, 0.0F),
				posL(1.775F, 0.0F, 0.093F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			List<Keyframe> kfs = new ArrayList<>(73);
			ch_3_p0(kfs);
			ch_3_p1(kfs);
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION, kfs.toArray(new Keyframe[0])));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -6.0F, 0.0F, 0.0F),
				rotL(0.62F, 8.0F, 8.8F, 0.0F),
				rotL(0.98F, 8.0F, 0.0F, 0.0F),
				rotL(1.34F, 8.0F, -8.8F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, 6.0F, 7.7F, 0.0F),
				rotL(0.98F, 6.0F, 0.0F, 0.0F),
				rotL(1.34F, 6.0F, -7.7F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -14.0F, 12.0F, 0.0F),
				rotL(0.62F, -8.0F, 18.7F, 0.0F),
				rotL(0.98F, -8.0F, 0.0F, 0.0F),
				rotL(1.34F, -8.0F, -18.7F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -4.0F, 0.0F, 0.0F),
				rotL(0.62F, 32.0F, 0.0F, 0.0F),
				rotL(0.98F, 32.0F, 0.0F, 0.0F),
				rotL(1.34F, 32.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -8.0F, 0.0F, 0.0F),
				rotL(0.62F, 10.0F, 8.8F, 0.0F),
				rotL(0.98F, 10.0F, 0.0F, 0.0F),
				rotL(1.34F, 10.0F, -8.8F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -14.0F, 14.0F, 0.0F),
				rotL(0.62F, -8.0F, 19.8F, 0.0F),
				rotL(0.98F, -8.0F, 0.0F, 0.0F),
				rotL(1.34F, -8.0F, -19.8F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -4.0F, 0.0F, 0.0F),
				rotL(0.62F, 30.0F, 0.0F, 0.0F),
				rotL(0.98F, 30.0F, 0.0F, 0.0F),
				rotL(1.34F, 30.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, -12.0F, 0.0F, 4.0F),
				rotL(0.98F, -12.0F, 0.0F, 4.0F),
				rotL(1.34F, -12.0F, 0.0F, 4.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, 18.0F, 0.0F, 0.0F),
				rotL(0.98F, 18.0F, 0.0F, 0.0F),
				rotL(1.34F, 18.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, -20.0F, 0.0F, 0.0F),
				rotL(0.98F, -20.0F, 0.0F, 0.0F),
				rotL(1.34F, -20.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, -12.0F, 0.0F, -4.0F),
				rotL(0.98F, -12.0F, 0.0F, -4.0F),
				rotL(1.34F, -12.0F, 0.0F, -4.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, 18.0F, 0.0F, 0.0F),
				rotL(0.98F, 18.0F, 0.0F, 0.0F),
				rotL(1.34F, 18.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, -20.0F, 0.0F, 0.0F),
				rotL(0.98F, -20.0F, 0.0F, 0.0F),
				rotL(1.34F, -20.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -12.0F, 0.0F, 0.0F),
				rotL(0.62F, 10.0F, -11.0F, 0.0F),
				rotL(0.98F, 10.0F, 0.0F, 0.0F),
				rotL(1.34F, 10.0F, 11.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, -8.0F, -11.0F, 0.0F),
				rotL(0.98F, -8.0F, 0.0F, 0.0F),
				rotL(1.34F, -8.0F, 11.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_52(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_53(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_54(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_55(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_56(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_57(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_58(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_59(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_60(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, 6.0F, 15.4F, 0.0F),
				rotL(0.98F, 6.0F, 0.0F, 0.0F),
				rotL(1.34F, 6.0F, -15.4F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_61(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_62(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, -18.0F, 0.0F, 0.0F),
				rotL(0.98F, -18.0F, 0.0F, 0.0F),
				rotL(1.34F, -18.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_63(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
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
			ch_52(b);
			ch_53(b);
			ch_54(b);
			ch_55(b);
			ch_56(b);
			ch_57(b);
			ch_58(b);
			ch_59(b);
			ch_60(b);
			ch_61(b);
			ch_62(b);
			ch_63(b);
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
				rotL(0.16F, -8.0F, 5.0F, 4.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.02F, 0.0F, 0.177F, 0.0F),
				posL(0.04F, 0.0F, 0.328F, 0.0F),
				posL(0.06F, 0.0F, 0.475F, 0.0F),
				posL(0.08F, 0.0F, 0.617F, 0.0F),
				posL(0.1F, 0.0F, 0.753F, 0.0F),
				posL(0.12F, 0.0F, 0.884F, 0.0F),
				posL(0.14F, 0.0F, 1.008F, 0.0F),
				posL(0.16F, 0.0F, 1.127F, 0.0F),
				posL(0.18F, 0.0F, 1.084F, 0.0F),
				posL(0.2F, 0.0F, 1.041F, 0.0F),
				posL(0.22F, 0.0F, 0.997F, 0.0F),
				posL(0.24F, 0.0F, 0.952F, 0.0F),
				posL(0.26F, 0.0F, 0.907F, 0.0F),
				posL(0.28F, 0.0F, 0.86F, 0.0F),
				posL(0.3F, 0.0F, 0.813F, 0.0F),
				posL(0.32F, 0.0F, 0.765F, 0.0F),
				posL(0.34F, 0.0F, 0.716F, 0.0F),
				posL(0.36F, 0.0F, 0.667F, 0.0F),
				posL(0.38F, 0.0F, 0.617F, 0.0F),
				posL(0.4F, 0.0F, 0.566F, 0.0F),
				posL(0.42F, 0.0F, 0.514F, 0.0F),
				posL(0.44F, 0.0F, 0.462F, 0.0F),
				posL(0.46F, 0.0F, 0.409F, 0.0F),
				posL(0.48F, 0.0F, 0.355F, 0.0F),
				posL(0.5F, 0.0F, 0.301F, 0.0F),
				posL(0.52F, 0.0F, 0.246F, 0.0F),
				posL(0.54F, 0.0F, 0.191F, 0.0F),
				posL(0.56F, 0.0F, 0.134F, 0.0F),
				posL(0.58F, 0.0F, 0.077F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -8.0F, 6.0F, 4.0F),
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
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -16.0F, -12.0F, -6.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 24.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -18.0F, 14.0F, 8.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 22.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -14.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 24.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -16.0F, 12.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_52(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_53(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_54(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_55(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_56(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_57(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_58(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_59(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_60(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_61(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_62(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -16.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_63(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
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
			ch_52(b);
			ch_53(b);
			ch_54(b);
			ch_55(b);
			ch_56(b);
			ch_57(b);
			ch_58(b);
			ch_59(b);
			ch_60(b);
			ch_61(b);
			ch_62(b);
			ch_63(b);
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
				posL(0.025F, -0.05F, 0.217F, 0.0F),
				posL(0.05F, -0.1F, 0.395F, 0.0F),
				posL(0.075F, -0.15F, 0.558F, 0.0F),
				posL(0.1F, -0.2F, 0.707F, 0.0F),
				posL(0.125F, -0.25F, 0.842F, 0.0F),
				posL(0.15F, -0.3F, 0.962F, 0.0F),
				posL(0.175F, -0.35F, 1.068F, 0.0F),
				posL(0.2F, -0.4F, 1.159F, 0.0F),
				posL(0.225F, -0.45F, 1.236F, 0.0F),
				posL(0.25F, -0.5F, 1.299F, 0.0F),
				posL(0.275F, -0.55F, 1.347F, 0.0F),
				posL(0.3F, -0.6F, 1.381F, 0.0F),
				posL(0.325F, -0.65F, 1.402F, 0.0F),
				posL(0.35F, -0.7F, 1.409F, 0.0F),
				posL(0.375F, -0.75F, 1.402F, 0.0F),
				posL(0.4F, -0.8F, 1.382F, 0.0F),
				posL(0.425F, -0.921F, 1.523F, 0.0F),
				posL(0.45F, -1.043F, 1.628F, 0.0F),
				posL(0.475F, -1.164F, 1.697F, 0.0F),
				posL(0.5F, -1.286F, 1.73F, 0.0F),
				posL(0.525F, -1.407F, 1.726F, 0.0F),
				posL(0.55F, -1.529F, 1.687F, 0.0F),
				posL(0.575F, -1.65F, 1.611F, 0.0F),
				posL(0.6F, -1.771F, 1.5F, 0.0F),
				posL(0.625F, -1.893F, 1.354F, 0.0F),
				posL(0.65F, -2.014F, 1.173F, 0.0F),
				posL(0.675F, -2.136F, 0.959F, 0.0F),
				posL(0.7F, -2.257F, 0.712F, 0.0F),
				posL(0.725F, -2.379F, 0.432F, 0.0F),
				posL(0.75F, -2.5F, 0.122F, 0.0F),
				posL(0.775F, -2.621F, -0.218F, 0.0F),
				posL(0.8F, -2.743F, -0.586F, 0.0F),
				posL(0.825F, -2.864F, -0.982F, 0.0F),
				posL(0.85F, -2.986F, -1.403F, 0.0F),
				posL(0.875F, -3.107F, -1.848F, 0.0F),
				posL(0.9F, -3.229F, -2.317F, 0.0F),
				posL(0.925F, -3.35F, -2.806F, 0.0F),
				posL(0.95F, -3.471F, -3.314F, 0.0F),
				posL(0.975F, -3.593F, -3.84F, 0.0F),
				posL(1.0F, -3.714F, -4.382F, 0.0F),
				posL(1.025F, -3.836F, -4.938F, 0.0F),
				posL(1.05F, -3.957F, -5.506F, 0.0F),
				posL(1.075F, -4.079F, -6.085F, 0.0F),
				posL(1.1F, -4.2F, -6.671F, 0.0F),
				posL(1.125F, -4.2F, -6.698F, 0.0F),
				posL(1.15F, -4.2F, -6.724F, 0.0F),
				posL(1.175F, -4.2F, -6.751F, 0.0F),
				posL(1.2F, -4.2F, -6.777F, 0.0F),
				posL(1.225F, -4.2F, -6.804F, 0.0F),
				posL(1.25F, -4.2F, -6.83F, 0.0F),
				posL(1.275F, -4.2F, -6.857F, 0.0F),
				posL(1.3F, -4.2F, -6.884F, 0.0F),
				posL(1.325F, -4.2F, -6.91F, 0.0F),
				posL(1.35F, -4.2F, -6.937F, 0.0F),
				posL(1.375F, -4.2F, -6.964F, 0.0F),
				posL(1.4F, -4.2F, -6.991F, 0.0F),
				posL(1.425F, -4.2F, -7.017F, 0.0F),
				posL(1.45F, -4.2F, -7.044F, 0.0F),
				posL(1.475F, -4.2F, -7.071F, 0.0F)
			));
		}
		private static void ch_1_p1(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(1.5F, -4.2F, -7.098F, 0.0F),
				posL(1.525F, -4.2F, -7.125F, 0.0F),
				posL(1.55F, -4.2F, -7.152F, 0.0F),
				posL(1.575F, -4.2F, -7.179F, 0.0F),
				posL(1.6F, -4.2F, -7.206F, 0.0F),
				posL(1.625F, -4.2F, -7.232F, 0.0F),
				posL(1.65F, -4.2F, -7.26F, 0.0F),
				posL(1.675F, -4.2F, -7.287F, 0.0F),
				posL(1.7F, -4.2F, -7.314F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			List<Keyframe> kfs = new ArrayList<>(69);
			ch_1_p0(kfs);
			ch_1_p1(kfs);
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION, kfs.toArray(new Keyframe[0])));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -10.0F, 0.0F, -14.0F),
				rotL(1.1F, 0.0F, 0.0F, -83.42F),
				rotL(1.7F, 0.0F, 0.0F, -86.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 4.0F, 0.0F, -2.0F),
				rotL(1.7F, 4.0F, 0.0F, -2.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("lion_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -22.0F, 8.0F, 0.0F),
				rotL(1.1F, 8.0F, 12.0F, -6.0F),
				rotL(1.7F, 8.0F, 12.0F, -6.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("lion_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 28.0F, 0.0F, 0.0F),
				rotL(1.1F, 14.0F, 0.0F, 0.0F),
				rotL(1.7F, 14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("lion_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 10.0F, 8.0F, -14.0F),
				rotL(1.7F, 10.0F, 8.0F, -14.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("goat_neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -24.0F, -10.0F, 0.0F),
				rotL(1.1F, 8.0F, 10.0F, 0.0F),
				rotL(1.7F, 8.0F, 10.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("goat_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 24.0F, 0.0F, 0.0F),
				rotL(1.1F, 12.0F, 0.0F, 0.0F),
				rotL(1.7F, 12.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("goat_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("goat_horn_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -26.0F, 0.0F, 0.0F),
				rotL(1.1F, -14.0F, 0.0F, 4.0F),
				rotL(1.7F, -14.0F, 0.0F, 4.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 52.0F, 0.0F, 0.0F),
				rotL(1.1F, 30.0F, 0.0F, 0.0F),
				rotL(1.7F, 30.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -22.0F, 0.0F, 0.0F),
				rotL(1.1F, -22.0F, 0.0F, -6.0F),
				rotL(1.7F, -22.0F, 0.0F, -6.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 46.0F, 0.0F, 0.0F),
				rotL(1.1F, 42.0F, 0.0F, 0.0F),
				rotL(1.7F, 42.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("elbow_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("paw_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -24.0F, 0.0F, 0.0F),
				rotL(1.1F, 10.0F, 0.0F, 4.0F),
				rotL(1.7F, 10.0F, 0.0F, 4.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 42.0F, 0.0F, 0.0F),
				rotL(1.1F, 20.0F, 0.0F, 0.0F),
				rotL(1.7F, 20.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("paw_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -20.0F, 0.0F, 0.0F),
				rotL(1.1F, 16.0F, 0.0F, -6.0F),
				rotL(1.7F, 16.0F, 0.0F, -6.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 38.0F, 0.0F, 0.0F),
				rotL(1.1F, 26.0F, 0.0F, 0.0F),
				rotL(1.7F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("paw_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 10.0F, 20.0F, 0.0F),
				rotL(1.7F, 10.0F, 20.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, -18.0F, 10.0F, 0.0F),
				rotL(1.7F, -18.0F, 10.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_50(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, -32.0F, 0.0F, 0.0F),
				rotL(1.7F, -32.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_51(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_52(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, -34.0F, -10.0F, 0.0F),
				rotL(1.7F, -34.0F, -10.0F, 0.0F)
			));
		}
		private static void ch_53(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_54(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, -32.0F, -10.0F, 0.0F),
				rotL(1.7F, -32.0F, -10.0F, 0.0F)
			));
		}
		private static void ch_55(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_56(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, -28.0F, -6.0F, 0.0F),
				rotL(1.7F, -28.0F, -6.0F, 0.0F)
			));
		}
		private static void ch_57(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_58(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, -24.0F, -4.0F, 0.0F),
				rotL(1.7F, -24.0F, -4.0F, 0.0F)
			));
		}
		private static void ch_59(AnimationDefinition.Builder b) {
			b.addAnimation("tail_7_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_60(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, -8.0F, 0.0F, 0.0F),
				rotL(1.7F, -8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_61(AnimationDefinition.Builder b) {
			b.addAnimation("snake_head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_62(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 16.0F, 0.0F, 0.0F),
				rotL(1.7F, 16.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_63(AnimationDefinition.Builder b) {
			b.addAnimation("snake_jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.7F);
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
			ch_52(b);
			ch_53(b);
			ch_54(b);
			ch_55(b);
			ch_56(b);
			ch_57(b);
			ch_58(b);
			ch_59(b);
			ch_60(b);
			ch_61(b);
			ch_62(b);
			ch_63(b);
			return b.build();
		}
	}

}
