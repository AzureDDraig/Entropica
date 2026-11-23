package ddraig.net.entropica.client.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Exact Blockbench keyframe animations for Barrow Wight Golem (Fauna 52 — Crypt Sentinel).
 * Split into per-animation static builders and <=60-keyframe helpers so constant pools and method bytecodes stay tiny.
 */
public final class BarrowWightGolemAnimations {

	private BarrowWightGolemAnimations() {}

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
	public static final AnimationDefinition ATTACK = AttackAnimBuilder.create();
	public static final AnimationDefinition attack = ATTACK;
	public static final AnimationDefinition SOULFIRE_BURST = SoulfireBurstAnimBuilder.create();
	public static final AnimationDefinition soulfire_burst = SOULFIRE_BURST;
	public static final AnimationDefinition DORMANT_ENTER = DormantEnterAnimBuilder.create();
	public static final AnimationDefinition dormant_enter = DORMANT_ENTER;
	public static final AnimationDefinition DORMANT = DormantAnimBuilder.create();
	public static final AnimationDefinition dormant = DORMANT;
	public static final AnimationDefinition AWAKEN = AwakenAnimBuilder.create();
	public static final AnimationDefinition awaken = AWAKEN;
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
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, -2.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, -1.5F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.25F, 0.0F),
				posL(1.6F, 0.0F, 0.45F, 0.0F),
				posL(2.4F, 0.0F, 0.2F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, -10.0F, -90.0F, 8.0F),
				rotL(1.6F, 0.0F, -180.0F, 0.0F),
				rotL(2.4F, 10.0F, -270.0F, -8.0F),
				rotL(3.2F, 0.0F, -360.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.45F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, -0.4F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.8F, 1.08F, 1.08F, 1.08F),
				sclL(1.6F, 1.0F, 1.0F, 1.0F),
				sclL(2.4F, 1.08F, 1.08F, 1.08F),
				sclL(3.2F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 2.0F, -4.0F, 2.0F),
				rotL(1.6F, -1.0F, 0.0F, 0.0F),
				rotL(2.4F, 2.0F, 4.0F, -2.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 3.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, -2.5F, 0.0F, 2.5F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, -3.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 2.5F, 0.0F, -2.5F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, -3.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
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
			return b.build();
		}
	}

	private static final class WalkAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.025F, 0.0F, 0.033F, 0.0F),
				posL(0.075F, 0.0F, 0.013F, 0.0F),
				posL(0.15F, 0.0F, -0.055F, 0.0F),
				posL(0.2F, 0.0F, -0.11F, 0.0F),
				posL(0.3F, 0.0F, -0.269F, 0.0F),
				posL(0.6F, 0.0F, -0.412F, 0.0F),
				posL(0.65F, 0.0F, -0.302F, 0.0F),
				posL(0.7F, 0.0F, -0.226F, 0.0F),
				posL(0.75F, 0.0F, -0.148F, 0.0F),
				posL(0.8F, 0.0F, -0.087F, 0.0F),
				posL(0.9F, 0.0F, 0.004F, 0.0F),
				posL(0.95F, 0.0F, 0.01F, 0.0F),
				posL(1.0F, 0.0F, -0.014F, 0.0F),
				posL(1.05F, 0.0F, -0.035F, 0.0F),
				posL(1.1F, 0.0F, -0.072F, 0.0F),
				posL(1.2F, 0.0F, -0.198F, 0.0F),
				posL(1.5F, 0.0F, -0.415F, 0.0F),
				posL(1.55F, 0.0F, -0.319F, 0.0F),
				posL(1.6F, 0.0F, -0.252F, 0.0F),
				posL(1.65F, 0.0F, -0.179F, 0.0F),
				posL(1.7F, 0.0F, -0.12F, 0.0F),
				posL(1.775F, 0.0F, -0.054F, 0.0F),
				posL(1.85F, 0.0F, -0.007F, 0.0F),
				posL(1.925F, 0.0F, -0.005F, 0.0F),
				posL(2.15F, 0.0F, -0.074F, 0.0F),
				posL(2.2F, 0.0F, -0.039F, 0.0F),
				posL(2.275F, 0.0F, -0.006F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 1.6F, 2.0F, -2.5F),
				rotL(0.6F, 2.4F, 3.0F, -3.5F),
				rotL(0.9F, 1.2F, 1.0F, -1.5F),
				rotL(1.2F, 1.6F, -1.5F, 2.0F),
				rotL(1.5F, 2.4F, -3.0F, 3.5F),
				rotL(1.85F, 1.2F, -1.0F, 1.5F),
				rotL(2.15F, 0.8F, -0.5F, 0.5F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -9.9F, 0.0F, 0.0F),
				rotL(0.6F, -9.0F, 0.0F, 0.0F),
				rotL(0.9F, -4.0F, 0.0F, 0.0F),
				rotL(1.2F, -4.0F, 0.0F, 0.0F),
				rotL(1.5F, -8.1F, 0.0F, 0.0F),
				rotL(1.85F, -4.0F, 0.0F, 0.0F),
				rotL(2.15F, -4.5F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 2.4F, 4.0F, -1.5F),
				rotL(0.6F, 3.6F, 6.0F, -2.1F),
				rotL(0.9F, 1.8F, 2.0F, -0.9F),
				rotL(1.2F, 2.4F, -3.0F, 1.2F),
				rotL(1.5F, 3.6F, -6.0F, 2.1F),
				rotL(1.85F, 1.8F, -2.0F, 0.9F),
				rotL(2.15F, 1.2F, -1.0F, 0.3F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, -12.0F, -180.0F, -10.0F),
				rotL(2.4F, 0.0F, -360.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.6F, 1.1F, 1.1F, 1.1F),
				sclL(1.2F, 1.0F, 1.0F, 1.0F),
				sclL(1.8F, 1.1F, 1.1F, 1.1F),
				sclL(2.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -2.8F, -3.2F, 1.75F),
				rotL(0.6F, -4.2F, -4.8F, 2.45F),
				rotL(0.9F, -2.1F, -1.6F, 1.05F),
				rotL(1.2F, -2.8F, 2.4F, -1.4F),
				rotL(1.5F, -4.2F, 4.8F, -2.45F),
				rotL(1.85F, -2.1F, 1.6F, -1.05F),
				rotL(2.15F, -1.4F, 0.8F, -0.35F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 20.0F, 0.0F, 3.0F),
				rotL(0.6F, 26.0F, 0.0F, 3.0F),
				rotL(0.9F, 8.0F, 0.0F, 3.0F),
				rotL(1.2F, -18.0F, 0.0F, 3.0F),
				rotL(1.5F, -28.0F, 0.0F, 3.0F),
				rotL(1.85F, -10.0F, 0.0F, 3.0F),
				rotL(2.15F, -4.0F, 0.0F, 3.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -10.0F, 0.0F, 0.0F),
				rotL(0.6F, -8.0F, 0.0F, 0.0F),
				rotL(0.9F, -12.0F, 0.0F, 0.0F),
				rotL(1.2F, -16.0F, 0.0F, 0.0F),
				rotL(1.5F, -22.0F, 0.0F, 0.0F),
				rotL(1.85F, -14.0F, 0.0F, 0.0F),
				rotL(2.15F, -6.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -22.0F, 0.0F, -3.0F),
				rotL(0.6F, -28.0F, 0.0F, -3.0F),
				rotL(0.9F, -10.0F, 0.0F, -3.0F),
				rotL(1.2F, 20.0F, 0.0F, -3.0F),
				rotL(1.5F, 26.0F, 0.0F, -3.0F),
				rotL(1.85F, 8.0F, 0.0F, -3.0F),
				rotL(2.15F, 4.0F, 0.0F, -3.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -18.0F, 0.0F, 0.0F),
				rotL(0.6F, -22.0F, 0.0F, 0.0F),
				rotL(0.9F, -14.0F, 0.0F, 0.0F),
				rotL(1.2F, -10.0F, 0.0F, 0.0F),
				rotL(1.5F, -8.0F, 0.0F, 0.0F),
				rotL(1.85F, -12.0F, 0.0F, 0.0F),
				rotL(2.15F, -6.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -22.0F, 0.0F, 0.0F),
				rotL(0.6F, -20.0F, 0.0F, 0.0F),
				rotL(0.9F, -6.0F, 0.0F, 0.0F),
				rotL(1.2F, 8.0F, 0.0F, 0.0F),
				rotL(1.5F, 18.0F, 0.0F, 0.0F),
				rotL(1.85F, -8.0F, 0.0F, 0.0F),
				rotL(2.15F, -10.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.3F, 0.0F, 0.75F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F),
				posL(0.9F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F),
				posL(1.85F, 0.0F, 0.85F, 0.0F),
				posL(2.15F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 30.0F, 0.0F, 0.0F),
				rotL(0.6F, 6.0F, 0.0F, 0.0F),
				rotL(0.9F, 8.0F, 0.0F, 0.0F),
				rotL(1.2F, 8.0F, 0.0F, 0.0F),
				rotL(1.5F, 12.0F, 0.0F, 0.0F),
				rotL(1.85F, 32.0F, 0.0F, 0.0F),
				rotL(2.15F, 10.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -8.0F, 0.0F, 0.0F),
				rotL(0.6F, 14.0F, 0.0F, 0.0F),
				rotL(0.9F, -2.0F, 0.0F, 0.0F),
				rotL(1.2F, -16.0F, 0.0F, 0.0F),
				rotL(1.5F, -30.0F, 0.0F, 0.0F),
				rotL(1.85F, -24.0F, 0.0F, 0.0F),
				rotL(2.15F, 0.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 10.0F, 0.0F, 0.0F),
				rotL(0.6F, 18.0F, 0.0F, 0.0F),
				rotL(0.9F, -8.0F, 0.0F, 0.0F),
				rotL(1.2F, -22.0F, 0.0F, 0.0F),
				rotL(1.5F, -20.0F, 0.0F, 0.0F),
				rotL(1.85F, -6.0F, 0.0F, 0.0F),
				rotL(2.15F, 6.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.3F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F),
				posL(0.9F, 0.0F, 0.85F, 0.0F),
				posL(1.2F, 0.0F, 0.55F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F),
				posL(1.85F, 0.0F, 0.0F, 0.0F),
				posL(2.15F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 8.0F, 0.0F, 0.0F),
				rotL(0.6F, 12.0F, 0.0F, 0.0F),
				rotL(0.9F, 34.0F, 0.0F, 0.0F),
				rotL(1.2F, 24.0F, 0.0F, 0.0F),
				rotL(1.5F, 6.0F, 0.0F, 0.0F),
				rotL(1.85F, 8.0F, 0.0F, 0.0F),
				rotL(2.15F, 6.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -18.0F, 0.0F, 0.0F),
				rotL(0.6F, -30.0F, 0.0F, 0.0F),
				rotL(0.9F, -26.0F, 0.0F, 0.0F),
				rotL(1.2F, -2.0F, 0.0F, 0.0F),
				rotL(1.5F, 14.0F, 0.0F, 0.0F),
				rotL(1.85F, -2.0F, 0.0F, 0.0F),
				rotL(2.15F, -12.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(2.4F).looping();
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
			return b.build();
		}
	}

	private static final class AttackAnimBuilder {
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
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.025F, 0.0F, 0.032F, 0.0F),
				posL(0.075F, 0.0F, -0.008F, 0.0F),
				posL(0.125F, 0.0F, -0.048F, 0.0F),
				posL(0.25F, 0.0F, -0.214F, 0.0F),
				posL(0.3F, 0.0F, -0.144F, 0.0F),
				posL(0.35F, 0.0F, -0.101F, 0.0F),
				posL(0.4F, 0.0F, -0.054F, 0.0F),
				posL(0.45F, 0.0F, -0.019F, 0.0F),
				posL(0.62F, 0.0F, 0.098F, 0.0F),
				posL(0.675F, 0.0F, 0.062F, 0.0F),
				posL(0.725F, 0.0F, -0.024F, 0.0F),
				posL(0.775F, 0.0F, -0.173F, 0.0F),
				posL(0.825F, 0.0F, -0.398F, 0.0F),
				posL(0.9F, 0.0F, -0.801F, 0.0F),
				posL(1.12F, 0.0F, -0.772F, 0.0F),
				posL(1.175F, 0.0F, -0.654F, 0.0F),
				posL(1.225F, 0.0F, -0.55F, 0.0F),
				posL(1.275F, 0.0F, -0.455F, 0.0F),
				posL(1.325F, 0.0F, -0.368F, 0.0F),
				posL(1.375F, 0.0F, -0.29F, 0.0F),
				posL(1.425F, 0.0F, -0.221F, 0.0F),
				posL(1.475F, 0.0F, -0.175F, 0.0F),
				posL(1.525F, 0.0F, -0.121F, 0.0F),
				posL(1.575F, 0.0F, -0.076F, 0.0F),
				posL(1.625F, 0.0F, -0.04F, 0.0F),
				posL(1.7F, 0.0F, -0.003F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, 6.0F, 0.0F, 0.0F),
				rotL(0.62F, -10.0F, 0.0F, 0.0F),
				rotL(0.9F, 22.0F, 0.0F, 0.0F),
				rotL(1.12F, 22.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, -14.0F, 0.0F, 0.0F),
				rotL(0.62F, 6.0F, 0.0F, 0.0F),
				rotL(0.9F, -52.0F, 0.0F, 0.0F),
				rotL(1.12F, -52.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, 10.0F, 0.0F, 0.0F),
				rotL(0.62F, -16.0F, 0.0F, 0.0F),
				rotL(0.9F, 26.0F, 0.0F, 0.0F),
				rotL(1.12F, 26.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, -25.0F, -180.0F, 0.0F),
				rotL(0.9F, 0.0F, -300.0F, 0.0F),
				rotL(1.12F, 0.0F, -340.0F, 0.0F),
				rotL(1.8F, 0.0F, -360.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.62F, 1.2F, 1.2F, 1.2F),
				sclL(0.9F, 1.26F, 1.26F, 1.26F),
				sclL(1.12F, 1.12F, 1.12F, 1.12F),
				sclL(1.8F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, -12.0F, 0.0F, 0.0F),
				rotL(0.9F, -26.0F, 0.0F, 0.0F),
				rotL(1.12F, -26.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.62F, 16.0F, 0.0F, 0.0F),
				rotL(0.9F, 18.0F, 0.0F, 0.0F),
				rotL(1.12F, 8.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, 20.0F, 0.0F, 6.0F),
				rotL(0.62F, -146.0F, 0.0F, 12.0F),
				rotL(0.9F, -68.0F, 0.0F, -6.0F),
				rotL(1.12F, -68.0F, 0.0F, -6.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, -28.0F, 0.0F, 0.0F),
				rotL(0.62F, -20.0F, 0.0F, 0.0F),
				rotL(0.9F, -10.0F, 0.0F, 0.0F),
				rotL(1.12F, -10.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, 20.0F, 0.0F, -6.0F),
				rotL(0.62F, -146.0F, 0.0F, -12.0F),
				rotL(0.9F, -68.0F, 0.0F, 6.0F),
				rotL(1.12F, -68.0F, 0.0F, 6.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, -28.0F, 0.0F, 0.0F),
				rotL(0.62F, -20.0F, 0.0F, 0.0F),
				rotL(0.9F, -10.0F, 0.0F, 0.0F),
				rotL(1.12F, -10.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, -14.0F, 0.0F, 0.0F),
				rotL(0.62F, 4.0F, 0.0F, 0.0F),
				rotL(0.9F, -24.0F, 0.0F, 0.0F),
				rotL(1.12F, -24.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, 26.0F, 0.0F, 0.0F),
				rotL(0.62F, -4.0F, 0.0F, 0.0F),
				rotL(0.9F, 48.0F, 0.0F, 0.0F),
				rotL(1.12F, 48.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, -12.0F, 0.0F, 0.0F),
				rotL(0.62F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, -24.0F, 0.0F, 0.0F),
				rotL(1.12F, -24.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, -14.0F, 0.0F, 0.0F),
				rotL(0.62F, 4.0F, 0.0F, 0.0F),
				rotL(0.9F, -24.0F, 0.0F, 0.0F),
				rotL(1.12F, -24.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, 26.0F, 0.0F, 0.0F),
				rotL(0.62F, -4.0F, 0.0F, 0.0F),
				rotL(0.9F, 48.0F, 0.0F, 0.0F),
				rotL(1.12F, 48.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.25F, -12.0F, 0.0F, 0.0F),
				rotL(0.62F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, -24.0F, 0.0F, 0.0F),
				rotL(1.12F, -24.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
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
			return b.build();
		}
	}

	private static final class SoulfireBurstAnimBuilder {
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
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.025F, 0.0F, 0.014F, 0.0F),
				posL(0.075F, 0.0F, 0.008F, 0.0F),
				posL(0.125F, 0.0F, -0.004F, 0.0F),
				posL(0.175F, 0.0F, -0.022F, 0.0F),
				posL(0.225F, 0.0F, -0.047F, 0.0F),
				posL(0.3F, 0.0F, -0.095F, 0.0F),
				posL(0.425F, 0.0F, -0.205F, 0.0F),
				posL(0.55F, 0.0F, -0.33F, 0.0F),
				posL(0.6F, 0.0F, -0.241F, 0.0F),
				posL(0.65F, 0.0F, -0.166F, 0.0F),
				posL(0.725F, 0.0F, -0.088F, 0.0F),
				posL(0.775F, 0.0F, -0.042F, 0.0F),
				posL(0.92F, 0.0F, 0.02F, 0.0F),
				posL(1.25F, 0.0F, 0.02F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, 8.0F, 0.0F, 0.0F),
				rotL(0.92F, -10.0F, 0.0F, 0.0F),
				rotL(1.25F, -10.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -20.0F, 0.0F, 0.0F),
				rotL(0.92F, 6.0F, 0.0F, 0.0F),
				rotL(1.25F, 6.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, 12.0F, 0.0F, 0.0F),
				rotL(0.92F, -18.0F, 0.0F, 0.0F),
				rotL(1.25F, -18.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -35.0F, -160.0F, -25.0F),
				rotL(0.92F, 25.0F, -420.0F, 25.0F),
				rotL(1.25F, 10.0F, -620.0F, 10.0F),
				rotL(2.0F, 0.0F, -720.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.92F, 0.0F, 0.4F, -1.8F),
				posL(1.25F, 0.0F, 0.4F, -1.8F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.55F, 0.68F, 0.68F, 0.68F),
				sclL(0.92F, 1.35F, 1.35F, 1.35F),
				sclL(1.25F, 1.22F, 1.22F, 1.22F),
				sclL(2.0F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, 14.0F, 0.0F, 0.0F),
				rotL(0.92F, -16.0F, 0.0F, 0.0F),
				rotL(1.25F, -16.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.92F, 20.0F, 0.0F, 0.0F),
				rotL(1.25F, 20.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -58.0F, 28.0F, -18.0F),
				rotL(0.92F, 18.0F, -18.0F, 68.0F),
				rotL(1.25F, 18.0F, -18.0F, 68.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -46.0F, 0.0F, 0.0F),
				rotL(0.92F, -16.0F, 0.0F, 0.0F),
				rotL(1.25F, -16.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -58.0F, -28.0F, 18.0F),
				rotL(0.92F, 18.0F, 18.0F, -68.0F),
				rotL(1.25F, 18.0F, 18.0F, -68.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -46.0F, 0.0F, 0.0F),
				rotL(0.92F, -16.0F, 0.0F, 0.0F),
				rotL(1.25F, -16.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -16.0F, 0.0F, 0.0F),
				rotL(0.92F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, 32.0F, 0.0F, 0.0F),
				rotL(0.92F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -16.0F, 0.0F, 0.0F),
				rotL(0.92F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -16.0F, 0.0F, 0.0F),
				rotL(0.92F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, 32.0F, 0.0F, 0.0F),
				rotL(0.92F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -16.0F, 0.0F, 0.0F),
				rotL(0.92F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
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
			return b.build();
		}
	}

	private static final class DormantEnterAnimBuilder {
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
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, -11.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 4.2F, 0.4F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, -1.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, -5.8F, 1.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.2F, 0.4F, 0.4F, 0.4F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, -8.2F, 1.4F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 6.5F, 1.5F, -1.2F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 6.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, -6.5F, 1.5F, -1.2F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 6.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, -1.2F, 6.0F, 0.4F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 5.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 1.2F, 6.0F, 0.4F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 5.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
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
			return b.build();
		}
	}

	private static final class DormantAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, -11.0F, 0.0F),
				posL(3.0F, 0.0F, -11.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 4.2F, 0.4F),
				posL(3.0F, 0.0F, 4.2F, 0.4F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, -1.0F, 0.0F),
				posL(3.0F, 0.0F, -1.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, -5.8F, 1.0F),
				posL(3.0F, 0.0F, -5.8F, 1.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 0.4F, 0.4F, 0.4F),
				sclL(1.5F, 0.45F, 0.45F, 0.45F),
				sclL(3.0F, 0.4F, 0.4F, 0.4F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, -8.2F, 1.4F),
				posL(3.0F, 0.0F, -8.2F, 1.4F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 6.5F, 1.5F, -1.2F),
				posL(3.0F, 6.5F, 1.5F, -1.2F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 6.0F, 0.0F),
				posL(3.0F, 0.0F, 6.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, -6.5F, 1.5F, -1.2F),
				posL(3.0F, -6.5F, 1.5F, -1.2F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 6.0F, 0.0F),
				posL(3.0F, 0.0F, 6.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, -1.2F, 6.0F, 0.4F),
				posL(3.0F, -1.2F, 6.0F, 0.4F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 5.0F, 0.0F),
				posL(3.0F, 0.0F, 5.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 1.2F, 6.0F, 0.4F),
				posL(3.0F, 1.2F, 6.0F, 0.4F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 5.0F, 0.0F),
				posL(3.0F, 0.0F, 5.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(3.0F).looping();
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
			return b.build();
		}
	}

	private static final class AwakenAnimBuilder {
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
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, -11.0F, 0.0F),
				posL(0.95F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.95F, -6.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 4.2F, 0.4F),
				posL(0.95F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.95F, -12.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, -1.0F, 0.0F),
				posL(0.95F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, -15.0F, -120.0F, -10.0F),
				rotL(0.95F, 0.0F, -280.0F, 0.0F),
				rotL(1.4F, 0.0F, -360.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, -5.8F, 1.0F),
				posL(0.95F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 0.4F, 0.4F, 0.4F),
				sclL(0.35F, 1.18F, 1.18F, 1.18F),
				sclL(0.95F, 1.22F, 1.22F, 1.22F),
				sclL(1.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, -8.2F, 1.4F),
				posL(0.95F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.95F, -10.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.95F, 16.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.95F, 14.0F, 0.0F, 28.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 6.5F, 1.5F, -1.2F),
				posL(0.45F, 1.5F, 0.8F, -0.4F),
				posL(0.95F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.95F, -18.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 6.0F, 0.0F),
				posL(0.95F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.95F, 14.0F, 0.0F, -28.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, -6.5F, 1.5F, -1.2F),
				posL(0.45F, -1.5F, 0.8F, -0.4F),
				posL(0.95F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.95F, -18.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 6.0F, 0.0F),
				posL(0.95F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, -1.2F, 6.0F, 0.4F),
				posL(0.95F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 5.0F, 0.0F),
				posL(0.95F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 1.2F, 6.0F, 0.4F),
				posL(0.95F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 5.0F, 0.0F),
				posL(0.95F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
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
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.025F, 0.0F, 0.011F, 0.125F),
				posL(0.075F, 0.0F, -0.017F, 0.375F),
				posL(0.16F, 0.0F, -0.124F, 0.8F),
				posL(0.25F, 0.0F, -0.081F, 0.653F),
				posL(0.325F, 0.0F, -0.049F, 0.531F),
				posL(0.425F, 0.0F, -0.015F, 0.368F),
				posL(0.575F, 0.0F, 0.012F, 0.123F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -10.0F, -8.0F, -4.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -16.0F, -12.0F, -6.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -28.0F, 50.0F, 22.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.16F, 0.78F, 0.78F, 0.78F),
				sclL(0.65F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -14.0F, 10.0F, 6.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 14.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -28.0F, 0.0F, 24.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -22.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 18.0F, 0.0F, -26.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -16.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -10.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 20.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -10.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -10.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 20.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -10.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
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
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.025F, 0.0F, 0.014F, 0.0F),
				posL(0.075F, 0.0F, 0.008F, 0.0F),
				posL(0.125F, 0.0F, -0.005F, 0.0F),
				posL(0.2F, 0.0F, -0.037F, 0.0F),
				posL(0.4F, 0.0F, -0.193F, 0.0F),
				posL(0.85F, 0.0F, 0.011F, 1.5F),
				posL(0.975F, 0.0F, -0.045F, 2.102F),
				posL(1.025F, 0.0F, 0.024F, 2.343F),
				posL(1.075F, 0.0F, -0.186F, 2.584F),
				posL(1.12F, 0.0F, -0.485F, 2.8F),
				posL(1.45F, 0.0F, -3.533F, 5.0F),
				posL(1.8F, 0.0F, -3.533F, 5.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -12.0F, 8.0F, 5.0F),
				rotL(0.85F, 26.0F, -4.0F, -3.0F),
				rotL(1.12F, 54.0F, 0.0F, 0.0F),
				rotL(1.45F, 80.0F, 0.0F, 0.0F),
				rotL(1.8F, 80.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.85F, -48.0F, 0.0F, 0.0F),
				rotL(1.12F, -64.0F, 0.0F, 0.0F),
				rotL(1.45F, -88.0F, 0.0F, 0.0F),
				rotL(1.8F, -88.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -16.0F, 10.0F, 6.0F),
				rotL(0.85F, 20.0F, -6.0F, -4.0F),
				rotL(1.12F, 8.0F, 0.0F, 0.0F),
				rotL(1.45F, 8.0F, 0.0F, 0.0F),
				rotL(1.8F, 8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -40.0F, -210.0F, -30.0F),
				rotL(0.85F, -15.0F, -340.0F, 10.0F),
				rotL(1.12F, 0.0F, -360.0F, 0.0F),
				rotL(1.45F, 0.0F, -360.0F, 0.0F),
				rotL(1.8F, 0.0F, -360.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.12F, 0.0F, -0.4F, 0.4F),
				posL(1.45F, 0.0F, -0.4F, 0.4F),
				posL(1.8F, 0.0F, -0.4F, 0.4F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("soul_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.4F, 1.22F, 1.22F, 1.22F),
				sclL(0.85F, 0.45F, 0.45F, 0.45F),
				sclL(1.12F, 0.3F, 0.3F, 0.3F),
				sclL(1.45F, 0.22F, 0.22F, 0.22F),
				sclL(1.8F, 0.22F, 0.22F, 0.22F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -18.0F, -12.0F, -8.0F),
				rotL(0.85F, 16.0F, 8.0F, 6.0F),
				rotL(1.12F, -4.0F, -20.0F, -8.0F),
				rotL(1.45F, -10.0F, -20.0F, -8.0F),
				rotL(1.8F, -10.0F, -20.0F, -8.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 18.0F, 0.0F, 0.0F),
				rotL(0.85F, 8.0F, 0.0F, 0.0F),
				rotL(1.12F, 12.0F, 0.0F, 0.0F),
				rotL(1.45F, 12.0F, 0.0F, 0.0F),
				rotL(1.8F, 12.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 20.0F, 0.0F, 32.0F),
				rotL(0.85F, -60.0F, 0.0F, 22.0F),
				rotL(1.12F, -124.0F, 0.0F, 34.0F),
				rotL(1.45F, -164.0F, 0.0F, 34.0F),
				rotL(1.8F, -164.0F, 0.0F, 34.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("arm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.85F, -24.0F, 0.0F, 0.0F),
				rotL(1.12F, -10.0F, 0.0F, 0.0F),
				rotL(1.45F, -10.0F, 0.0F, 0.0F),
				rotL(1.8F, -10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -26.0F, 0.0F, -28.0F),
				rotL(0.85F, -56.0F, 0.0F, -24.0F),
				rotL(1.12F, -120.0F, 0.0F, -30.0F),
				rotL(1.45F, -162.0F, 0.0F, -30.0F),
				rotL(1.8F, -162.0F, 0.0F, -30.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("arm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.85F, -28.0F, 0.0F, 0.0F),
				rotL(1.12F, -12.0F, 0.0F, 0.0F),
				rotL(1.45F, -12.0F, 0.0F, 0.0F),
				rotL(1.8F, -12.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -12.0F, 0.0F, 0.0F),
				rotL(0.85F, -34.0F, 0.0F, 0.0F),
				rotL(1.12F, 22.0F, 0.0F, 8.0F),
				rotL(1.45F, 84.0F, 0.0F, 8.0F),
				rotL(1.8F, 84.0F, 0.0F, 8.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.12F, 0.0F, 0.0F, 0.0F),
				posL(1.45F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 24.0F, 0.0F, 0.0F),
				rotL(0.85F, 68.0F, 0.0F, 0.0F),
				rotL(1.12F, 42.0F, 0.0F, 0.0F),
				rotL(1.45F, 4.0F, 0.0F, 0.0F),
				rotL(1.8F, 4.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -12.0F, 0.0F, 0.0F),
				rotL(0.85F, -34.0F, 0.0F, 0.0F),
				rotL(1.12F, -24.0F, 0.0F, 0.0F),
				rotL(1.45F, -12.0F, 0.0F, 0.0F),
				rotL(1.8F, -12.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -12.0F, 0.0F, 0.0F),
				rotL(0.85F, -34.0F, 0.0F, 0.0F),
				rotL(1.12F, 20.0F, 0.0F, -10.0F),
				rotL(1.45F, 82.0F, 0.0F, -10.0F),
				rotL(1.8F, 82.0F, 0.0F, -10.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.12F, 0.0F, 0.0F, 0.0F),
				posL(1.45F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 24.0F, 0.0F, 0.0F),
				rotL(0.85F, 68.0F, 0.0F, 0.0F),
				rotL(1.12F, 44.0F, 0.0F, 0.0F),
				rotL(1.45F, 6.0F, 0.0F, 0.0F),
				rotL(1.8F, 6.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, -12.0F, 0.0F, 0.0F),
				rotL(0.85F, -34.0F, 0.0F, 0.0F),
				rotL(1.12F, -24.0F, 0.0F, 0.0F),
				rotL(1.45F, -12.0F, 0.0F, 0.0F),
				rotL(1.8F, -12.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
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
			return b.build();
		}
	}

}
