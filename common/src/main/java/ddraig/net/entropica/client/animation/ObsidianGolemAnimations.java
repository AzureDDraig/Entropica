package ddraig.net.entropica.client.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Exact Blockbench keyframe animations for Obsidian Golem (Fauna 55 — Nether Sentinel).
 * Split into per-animation static builders and <=60-keyframe helpers so constant pools and method bytecodes stay tiny.
 */
public final class ObsidianGolemAnimations {

	private ObsidianGolemAnimations() {}

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
	public static final AnimationDefinition SPATIAL_WAVE = SpatialWaveAnimBuilder.create();
	public static final AnimationDefinition spatial_wave = SPATIAL_WAVE;
	public static final AnimationDefinition SHIELD_BLOCK = ShieldBlockAnimBuilder.create();
	public static final AnimationDefinition shield_block = SHIELD_BLOCK;
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
				rotL(1.6F, 1.5F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.2F, 0.0F),
				posL(1.6F, 0.0F, 0.38F, 0.0F),
				posL(2.4F, 0.0F, 0.18F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.8F, 1.03F, 1.03F, 1.03F),
				sclL(1.6F, 0.99F, 0.99F, 0.99F),
				sclL(2.4F, 1.03F, 1.03F, 1.03F),
				sclL(3.2F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, -12.0F, -90.0F, 8.0F),
				rotL(1.6F, 0.0F, -180.0F, 0.0F),
				rotL(2.4F, 12.0F, -270.0F, -8.0F),
				rotL(3.2F, 0.0F, -360.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(3.2F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 2.0F, -6.0F, 1.5F),
				rotL(1.6F, -1.5F, 0.0F, 0.0F),
				rotL(2.4F, 2.0F, 6.0F, -1.5F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 2.5F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, -3.0F, 0.0F, 2.5F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, -3.5F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, -3.0F, 0.0F, -2.5F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, -3.5F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, -2.0F, -2.0F, 1.5F),
				rotL(2.4F, -4.5F, 1.5F, 2.5F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, -4.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, -4.5F, -1.5F, -2.5F),
				rotL(2.4F, -2.0F, 2.0F, -1.5F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, -4.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
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
				posL(0.05F, 0.0F, 0.004F, 0.0F),
				posL(0.1F, 0.0F, -0.02F, 0.0F),
				posL(0.15F, 0.0F, -0.06F, 0.0F),
				posL(0.2F, 0.0F, -0.116F, 0.0F),
				posL(0.25F, 0.0F, -0.188F, 0.0F),
				posL(0.3F, 0.0F, -0.275F, 0.0F),
				posL(0.35F, 0.0F, -0.346F, 0.0F),
				posL(0.4F, 0.0F, -0.425F, 0.0F),
				posL(0.45F, 0.0F, -0.512F, 0.0F),
				posL(0.5F, 0.0F, -0.606F, 0.0F),
				posL(0.55F, 0.0F, -0.556F, 0.0F),
				posL(0.6F, 0.0F, -0.469F, 0.0F),
				posL(0.65F, 0.0F, -0.349F, 0.0F),
				posL(0.7F, 0.0F, -0.247F, 0.0F),
				posL(0.75F, 0.0F, -0.164F, 0.0F),
				posL(0.8F, 0.0F, -0.098F, 0.0F),
				posL(0.85F, 0.0F, -0.052F, 0.0F),
				posL(0.9F, 0.0F, -0.023F, 0.0F),
				posL(0.95F, 0.0F, -0.012F, 0.0F),
				posL(1.0F, 0.0F, -0.017F, 0.0F),
				posL(1.05F, 0.0F, -0.038F, 0.0F),
				posL(1.1F, 0.0F, -0.076F, 0.0F),
				posL(1.15F, 0.0F, -0.131F, 0.0F),
				posL(1.2F, 0.0F, -0.201F, 0.0F),
				posL(1.25F, 0.0F, -0.276F, 0.0F),
				posL(1.3F, 0.0F, -0.362F, 0.0F),
				posL(1.35F, 0.0F, -0.46F, 0.0F),
				posL(1.4F, 0.0F, -0.568F, 0.0F),
				posL(1.45F, 0.0F, -0.54F, 0.0F),
				posL(1.5F, 0.0F, -0.469F, 0.0F),
				posL(1.55F, 0.0F, -0.365F, 0.0F),
				posL(1.6F, 0.0F, -0.274F, 0.0F),
				posL(1.65F, 0.0F, -0.197F, 0.0F),
				posL(1.7F, 0.0F, -0.133F, 0.0F),
				posL(1.75F, 0.0F, -0.083F, 0.0F),
				posL(1.8F, 0.0F, -0.046F, 0.0F),
				posL(1.85F, 0.0F, -0.023F, 0.0F),
				posL(1.9F, 0.0F, -0.011F, 0.0F),
				posL(1.95F, 0.0F, -0.009F, 0.0F),
				posL(2.0F, 0.0F, -0.018F, 0.0F),
				posL(2.05F, 0.0F, -0.037F, 0.0F),
				posL(2.1F, 0.0F, -0.067F, 0.0F),
				posL(2.15F, 0.0F, -0.079F, 0.0F),
				posL(2.2F, 0.0F, -0.046F, 0.0F),
				posL(2.25F, 0.0F, -0.021F, 0.0F),
				posL(2.3F, 0.0F, -0.003F, 0.0F),
				posL(2.35F, 0.0F, 0.008F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 1.4F, 2.5F, -2.5F),
				rotL(0.6F, 2.0F, 3.5F, -3.5F),
				rotL(0.9F, 1.2F, 1.0F, -1.5F),
				rotL(1.2F, 1.4F, -2.0F, 2.0F),
				rotL(1.5F, 2.0F, -3.5F, 3.5F),
				rotL(1.85F, 1.2F, -1.0F, 1.5F),
				rotL(2.15F, 0.6F, -0.5F, 0.5F),
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
				rotL(0.3F, 2.1F, 5.0F, -1.5F),
				rotL(0.6F, 3.0F, 7.0F, -2.1F),
				rotL(0.9F, 1.8F, 2.0F, -0.9F),
				rotL(1.2F, 2.1F, -4.0F, 1.2F),
				rotL(1.5F, 3.0F, -7.0F, 2.1F),
				rotL(1.85F, 1.8F, -2.0F, 0.9F),
				rotL(2.15F, 0.9F, -1.0F, 0.3F),
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
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.6F, 1.03F, 1.03F, 1.03F),
				sclL(1.2F, 0.99F, 0.99F, 0.99F),
				sclL(1.8F, 1.03F, 1.03F, 1.03F),
				sclL(2.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, -10.0F, -90.0F, 8.0F),
				rotL(1.2F, 0.0F, -180.0F, 0.0F),
				rotL(1.8F, 10.0F, -270.0F, -8.0F),
				rotL(2.4F, 0.0F, -360.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(2.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -2.45F, -4.25F, 1.75F),
				rotL(0.6F, -3.5F, -5.95F, 2.45F),
				rotL(0.9F, -2.1F, -1.7F, 1.05F),
				rotL(1.2F, -2.45F, 3.4F, -1.4F),
				rotL(1.5F, -3.5F, 5.95F, -2.45F),
				rotL(1.85F, -2.1F, 1.7F, -1.05F),
				rotL(2.15F, -1.05F, 0.85F, -0.35F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 18.0F, 0.0F, 2.5F),
				rotL(0.6F, 24.0F, 0.0F, 2.5F),
				rotL(0.9F, 8.0F, 0.0F, 2.5F),
				rotL(1.2F, -18.0F, 0.0F, 2.5F),
				rotL(1.5F, -26.0F, 0.0F, 2.5F),
				rotL(1.85F, -10.0F, 0.0F, 2.5F),
				rotL(2.15F, -4.0F, 0.0F, 2.5F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -4.0F, 0.0F, 0.0F),
				rotL(0.6F, -4.0F, 0.0F, 0.0F),
				rotL(0.9F, -5.2F, 0.0F, 0.0F),
				rotL(1.2F, -14.3F, 0.0F, 0.0F),
				rotL(1.5F, -17.1F, 0.0F, 0.0F),
				rotL(1.85F, -11.5F, 0.0F, 0.0F),
				rotL(2.15F, -9.4F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -20.0F, 0.0F, -2.5F),
				rotL(0.6F, -26.0F, 0.0F, -2.5F),
				rotL(0.9F, -10.0F, 0.0F, -2.5F),
				rotL(1.2F, 18.0F, 0.0F, -2.5F),
				rotL(1.5F, 24.0F, 0.0F, -2.5F),
				rotL(1.85F, 8.0F, 0.0F, -2.5F),
				rotL(2.15F, 4.0F, 0.0F, -2.5F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -15.0F, 0.0F, 0.0F),
				rotL(0.6F, -17.1F, 0.0F, 0.0F),
				rotL(0.9F, -11.5F, 0.0F, 0.0F),
				rotL(1.2F, -4.0F, 0.0F, 0.0F),
				rotL(1.5F, -4.0F, 0.0F, 0.0F),
				rotL(1.85F, -5.2F, 0.0F, 0.0F),
				rotL(2.15F, -6.6F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -14.0F, -3.0F, 2.0F),
				rotL(0.6F, -18.0F, -3.0F, 2.0F),
				rotL(0.9F, -6.0F, -3.0F, 2.0F),
				rotL(1.2F, 14.0F, -3.0F, 2.0F),
				rotL(1.5F, 18.0F, -3.0F, 2.0F),
				rotL(1.85F, 6.0F, -3.0F, 2.0F),
				rotL(2.15F, 3.0F, -3.0F, 2.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -10.2F, 0.0F, 0.0F),
				rotL(0.6F, -11.4F, 0.0F, 0.0F),
				rotL(0.9F, -7.8F, 0.0F, 0.0F),
				rotL(1.2F, -4.0F, 0.0F, 0.0F),
				rotL(1.5F, -4.0F, 0.0F, 0.0F),
				rotL(1.85F, -4.2F, 0.0F, 0.0F),
				rotL(2.15F, -5.1F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 14.0F, 3.0F, -2.0F),
				rotL(0.6F, 18.0F, 3.0F, -2.0F),
				rotL(0.9F, 6.0F, 3.0F, -2.0F),
				rotL(1.2F, -14.0F, 3.0F, -2.0F),
				rotL(1.5F, -18.0F, 3.0F, -2.0F),
				rotL(1.85F, -6.0F, 3.0F, -2.0F),
				rotL(2.15F, -3.0F, 3.0F, -2.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -4.0F, 0.0F, 0.0F),
				rotL(0.6F, -4.0F, 0.0F, 0.0F),
				rotL(0.9F, -4.2F, 0.0F, 0.0F),
				rotL(1.2F, -10.2F, 0.0F, 0.0F),
				rotL(1.5F, -11.4F, 0.0F, 0.0F),
				rotL(1.85F, -7.8F, 0.0F, 0.0F),
				rotL(2.15F, -6.9F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
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
		private static void ch_39(AnimationDefinition.Builder b) {
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
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 28.0F, 0.0F, 0.0F),
				rotL(0.6F, 6.0F, 0.0F, 0.0F),
				rotL(0.9F, 8.0F, 0.0F, 0.0F),
				rotL(1.2F, 8.0F, 0.0F, 0.0F),
				rotL(1.5F, 12.0F, 0.0F, 0.0F),
				rotL(1.85F, 30.0F, 0.0F, 0.0F),
				rotL(2.15F, 10.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -6.0F, 0.0F, 0.0F),
				rotL(0.6F, 14.0F, 0.0F, 0.0F),
				rotL(0.9F, -2.0F, 0.0F, 0.0F),
				rotL(1.2F, -16.0F, 0.0F, 0.0F),
				rotL(1.5F, -30.0F, 0.0F, 0.0F),
				rotL(1.85F, -22.0F, 0.0F, 0.0F),
				rotL(2.15F, 0.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
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
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.3F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F),
				posL(0.9F, 0.0F, 0.85F, 0.0F),
				posL(1.2F, 0.0F, 0.6F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F),
				posL(1.85F, 0.0F, 0.0F, 0.0F),
				posL(2.15F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 8.0F, 0.0F, 0.0F),
				rotL(0.6F, 12.0F, 0.0F, 0.0F),
				rotL(0.9F, 32.0F, 0.0F, 0.0F),
				rotL(1.2F, 24.0F, 0.0F, 0.0F),
				rotL(1.5F, 6.0F, 0.0F, 0.0F),
				rotL(1.85F, 8.0F, 0.0F, 0.0F),
				rotL(2.15F, 6.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -18.0F, 0.0F, 0.0F),
				rotL(0.6F, -30.0F, 0.0F, 0.0F),
				rotL(0.9F, -24.0F, 0.0F, 0.0F),
				rotL(1.2F, -2.0F, 0.0F, 0.0F),
				rotL(1.5F, 14.0F, 0.0F, 0.0F),
				rotL(1.85F, -2.0F, 0.0F, 0.0F),
				rotL(2.15F, -12.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
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
		private static void ch_3_p0(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.025F, 0.0F, 0.02F, 0.038F),
				posL(0.05F, 0.0F, 0.018F, 0.077F),
				posL(0.075F, 0.0F, 0.016F, 0.115F),
				posL(0.1F, 0.0F, 0.014F, 0.154F),
				posL(0.125F, 0.0F, 0.01F, 0.192F),
				posL(0.15F, 0.0F, 0.006F, 0.231F),
				posL(0.175F, 0.0F, 0.001F, 0.269F),
				posL(0.2F, 0.0F, -0.005F, 0.308F),
				posL(0.225F, 0.0F, -0.012F, 0.346F),
				posL(0.25F, 0.0F, -0.019F, 0.385F),
				posL(0.275F, 0.0F, -0.028F, 0.423F),
				posL(0.3F, 0.0F, -0.037F, 0.462F),
				posL(0.325F, 0.0F, -0.047F, 0.5F),
				posL(0.35F, 0.0F, -0.057F, 0.538F),
				posL(0.375F, 0.0F, -0.069F, 0.577F),
				posL(0.4F, 0.0F, -0.081F, 0.615F),
				posL(0.425F, 0.0F, -0.094F, 0.654F),
				posL(0.45F, 0.0F, -0.107F, 0.692F),
				posL(0.475F, 0.0F, -0.122F, 0.731F),
				posL(0.5F, 0.0F, -0.137F, 0.769F),
				posL(0.525F, 0.0F, -0.155F, 0.762F),
				posL(0.55F, 0.0F, -0.183F, 0.569F),
				posL(0.575F, 0.0F, -0.213F, 0.377F),
				posL(0.6F, 0.0F, -0.245F, 0.185F),
				posL(0.625F, 0.0F, -0.264F, -0.008F),
				posL(0.65F, 0.0F, -0.269F, -0.2F),
				posL(0.675F, 0.0F, -0.288F, -0.392F),
				posL(0.7F, 0.0F, -0.323F, -0.585F),
				posL(0.725F, 0.0F, -0.374F, -0.777F),
				posL(0.75F, 0.0F, -0.439F, -0.969F),
				posL(0.775F, 0.0F, -0.52F, -1.162F),
				posL(0.8F, 0.0F, -0.538F, -1.2F),
				posL(0.825F, 0.0F, -0.538F, -1.2F),
				posL(0.85F, 0.0F, -0.538F, -1.2F),
				posL(0.875F, 0.0F, -0.538F, -1.2F),
				posL(0.9F, 0.0F, -0.538F, -1.2F),
				posL(0.925F, 0.0F, -0.538F, -1.2F),
				posL(0.95F, 0.0F, -0.538F, -1.2F),
				posL(0.975F, 0.0F, -0.518F, -1.179F),
				posL(1.0F, 0.0F, -0.486F, -1.143F),
				posL(1.025F, 0.0F, -0.455F, -1.107F),
				posL(1.05F, 0.0F, -0.425F, -1.071F),
				posL(1.075F, 0.0F, -0.396F, -1.036F),
				posL(1.1F, 0.0F, -0.368F, -1.0F),
				posL(1.125F, 0.0F, -0.341F, -0.964F),
				posL(1.15F, 0.0F, -0.315F, -0.929F),
				posL(1.175F, 0.0F, -0.29F, -0.893F),
				posL(1.2F, 0.0F, -0.266F, -0.857F),
				posL(1.225F, 0.0F, -0.243F, -0.821F),
				posL(1.25F, 0.0F, -0.22F, -0.786F),
				posL(1.275F, 0.0F, -0.199F, -0.75F),
				posL(1.3F, 0.0F, -0.179F, -0.714F),
				posL(1.325F, 0.0F, -0.159F, -0.679F),
				posL(1.35F, 0.0F, -0.141F, -0.643F),
				posL(1.375F, 0.0F, -0.124F, -0.607F),
				posL(1.4F, 0.0F, -0.107F, -0.571F),
				posL(1.425F, 0.0F, -0.092F, -0.536F),
				posL(1.45F, 0.0F, -0.078F, -0.5F),
				posL(1.475F, 0.0F, -0.064F, -0.464F)
			));
		}
		private static void ch_3_p1(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(1.5F, 0.0F, -0.052F, -0.429F),
				posL(1.525F, 0.0F, -0.04F, -0.393F),
				posL(1.55F, 0.0F, -0.03F, -0.357F),
				posL(1.575F, 0.0F, -0.02F, -0.321F),
				posL(1.6F, 0.0F, -0.012F, -0.286F),
				posL(1.625F, 0.0F, -0.004F, -0.25F),
				posL(1.65F, 0.0F, 0.002F, -0.214F),
				posL(1.675F, 0.0F, 0.008F, -0.179F),
				posL(1.7F, 0.0F, 0.012F, -0.143F),
				posL(1.725F, 0.0F, 0.016F, -0.107F),
				posL(1.75F, 0.0F, 0.018F, -0.071F),
				posL(1.775F, 0.0F, 0.02F, -0.036F),
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
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -12.0F, 0.0F, 0.0F),
				rotL(0.78F, 22.0F, 0.0F, 0.0F),
				rotL(0.96F, 21.1F, 0.0F, 0.0F),
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
				rotL(0.52F, -8.0F, 0.0F, 0.0F),
				rotL(0.78F, -32.0F, 0.0F, 0.0F),
				rotL(0.96F, -32.0F, 0.0F, 0.0F),
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
				rotL(0.52F, -16.0F, 0.0F, 0.0F),
				rotL(0.78F, 28.0F, 0.0F, 0.0F),
				rotL(0.96F, 26.5F, 0.0F, 0.0F),
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
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.52F, 1.06F, 1.06F, 1.06F),
				sclL(0.78F, 1.1F, 1.1F, 1.1F),
				sclL(1.15F, 1.02F, 1.02F, 1.02F),
				sclL(1.8F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -20.0F, -120.0F, 15.0F),
				rotL(0.85F, 20.0F, -270.0F, -15.0F),
				rotL(1.8F, 0.0F, -360.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.8F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -14.0F, 0.0F, 0.0F),
				rotL(0.78F, -22.0F, 0.0F, 0.0F),
				rotL(0.96F, -22.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, 8.0F, 0.0F, 0.0F),
				rotL(0.78F, 10.0F, 0.0F, 0.0F),
				rotL(0.96F, 10.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -118.0F, -8.0F, 10.0F),
				rotL(0.78F, -54.0F, -14.0F, -22.0F),
				rotL(0.96F, -51.0F, -14.0F, -22.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -22.0F, 0.0F, 0.0F),
				rotL(0.78F, 12.0F, 0.0F, 10.0F),
				rotL(0.96F, 12.0F, 0.0F, 10.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -118.0F, 8.0F, -10.0F),
				rotL(0.78F, -54.0F, 14.0F, 22.0F),
				rotL(0.96F, -51.0F, 14.0F, 22.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -22.0F, 0.0F, 0.0F),
				rotL(0.78F, 12.0F, 0.0F, -10.0F),
				rotL(0.96F, 12.0F, 0.0F, -10.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -78.0F, 12.0F, 14.0F),
				rotL(0.78F, 0.0F, -14.0F, -8.0F),
				rotL(0.96F, 3.0F, -14.0F, -8.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -24.0F, 0.0F, 0.0F),
				rotL(0.78F, 22.0F, 0.0F, 0.0F),
				rotL(0.96F, 22.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -78.0F, -12.0F, -14.0F),
				rotL(0.78F, 0.0F, 14.0F, 8.0F),
				rotL(0.96F, 3.0F, 14.0F, 8.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -24.0F, 0.0F, 0.0F),
				rotL(0.78F, 22.0F, 0.0F, 0.0F),
				rotL(0.96F, 22.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, 10.0F, 0.0F, 0.0F),
				rotL(0.78F, -20.0F, 0.0F, 0.0F),
				rotL(0.96F, -20.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, 14.0F, 0.0F, 0.0F),
				rotL(0.78F, 38.0F, 0.0F, 0.0F),
				rotL(0.96F, 38.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -24.0F, 0.0F, 0.0F),
				rotL(0.78F, -18.0F, 0.0F, 0.0F),
				rotL(0.96F, -18.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -12.0F, 0.0F, 0.0F),
				rotL(0.78F, -20.0F, 0.0F, 0.0F),
				rotL(0.96F, -20.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, 20.0F, 0.0F, 0.0F),
				rotL(0.78F, 38.0F, 0.0F, 0.0F),
				rotL(0.96F, 38.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.52F, -8.0F, 0.0F, 0.0F),
				rotL(0.78F, -18.0F, 0.0F, 0.0F),
				rotL(0.96F, -18.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
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
			return b.build();
		}
	}

	private static final class SpatialWaveAnimBuilder {
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
				posL(0.05F, 0.0F, 0.01F, 0.0F),
				posL(0.1F, 0.0F, 0.006F, 0.0F),
				posL(0.15F, 0.0F, -0.002F, 0.0F),
				posL(0.2F, 0.0F, -0.013F, 0.0F),
				posL(0.25F, 0.0F, -0.028F, 0.0F),
				posL(0.3F, 0.0F, -0.045F, 0.0F),
				posL(0.35F, 0.0F, -0.066F, 0.0F),
				posL(0.4F, 0.0F, -0.09F, 0.0F),
				posL(0.45F, 0.0F, -0.117F, 0.0F),
				posL(0.5F, 0.0F, -0.147F, 0.0F),
				posL(0.55F, 0.0F, -0.18F, 0.0F),
				posL(0.6F, 0.0F, -0.123F, 0.0F),
				posL(0.65F, 0.0F, -0.091F, 0.0F),
				posL(0.7F, 0.0F, -0.083F, 0.0F),
				posL(0.75F, 0.0F, -0.099F, 0.0F),
				posL(0.8F, 0.0F, -0.14F, 0.0F),
				posL(0.85F, 0.0F, -0.135F, 0.0F),
				posL(0.9F, 0.0F, -0.13F, 0.0F),
				posL(0.95F, 0.0F, -0.126F, 0.0F),
				posL(1.0F, 0.0F, -0.121F, 0.0F),
				posL(1.05F, 0.0F, -0.117F, 0.0F),
				posL(1.1F, 0.0F, -0.113F, 0.0F),
				posL(1.15F, 0.0F, -0.1F, 0.0F),
				posL(1.2F, 0.0F, -0.084F, 0.0F),
				posL(1.25F, 0.0F, -0.069F, 0.0F),
				posL(1.3F, 0.0F, -0.055F, 0.0F),
				posL(1.35F, 0.0F, -0.042F, 0.0F),
				posL(1.4F, 0.0F, -0.031F, 0.0F),
				posL(1.45F, 0.0F, -0.021F, 0.0F),
				posL(1.5F, 0.0F, -0.012F, 0.0F),
				posL(1.55F, 0.0F, -0.005F, 0.0F),
				posL(1.6F, 0.0F, 0.001F, 0.0F),
				posL(1.65F, 0.0F, 0.006F, 0.0F),
				posL(1.7F, 0.0F, 0.009F, 0.0F),
				posL(1.75F, 0.0F, 0.011F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, 8.0F, 0.0F, 0.0F),
				rotL(0.8F, -10.0F, 0.0F, 0.0F),
				rotL(1.12F, -9.0F, 0.0F, 0.0F),
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
				rotL(0.55F, -14.0F, 0.0F, 0.0F),
				rotL(0.8F, 6.0F, 0.0F, 0.0F),
				rotL(1.12F, 5.4F, 0.0F, 0.0F),
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
				rotL(0.55F, 12.0F, 0.0F, 0.0F),
				rotL(0.8F, -16.0F, 0.0F, 0.0F),
				rotL(1.12F, -14.4F, 0.0F, 0.0F),
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
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.55F, 0.76F, 0.76F, 0.76F),
				sclL(0.8F, 1.14F, 1.14F, 1.14F),
				sclL(1.12F, 1.08F, 1.08F, 1.08F),
				sclL(1.8F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, 25.0F, -90.0F, -20.0F),
				rotL(0.85F, -30.0F, -270.0F, 25.0F),
				rotL(1.8F, 0.0F, -360.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.55F, 0.7F, 0.7F, 0.7F),
				sclL(0.8F, 1.3F, 1.3F, 1.3F),
				sclL(1.8F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, 12.0F, 0.0F, 0.0F),
				rotL(0.8F, -18.0F, 0.0F, 0.0F),
				rotL(1.12F, -16.2F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 10.0F, 0.0F, 0.0F),
				rotL(1.12F, 9.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -38.0F, -24.0F, -22.0F),
				rotL(0.8F, 14.0F, 22.0F, 34.0F),
				rotL(1.12F, 12.6F, 19.8F, 30.6F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -40.0F, -16.0F, 0.0F),
				rotL(0.8F, 12.0F, 0.0F, 10.0F),
				rotL(1.12F, 10.8F, 0.0F, 9.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -38.0F, 24.0F, 22.0F),
				rotL(0.8F, 14.0F, -22.0F, -34.0F),
				rotL(1.12F, 12.6F, -19.8F, -30.6F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -40.0F, 16.0F, 0.0F),
				rotL(0.8F, 12.0F, 0.0F, -10.0F),
				rotL(1.12F, 10.8F, 0.0F, -9.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -28.0F, -28.0F, -12.0F),
				rotL(0.8F, 18.0F, 28.0F, 24.0F),
				rotL(1.12F, 16.2F, 25.2F, 21.6F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -32.0F, -14.0F, 0.0F),
				rotL(0.8F, 20.0F, 0.0F, 8.0F),
				rotL(1.12F, 18.0F, 0.0F, 7.2F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -28.0F, 28.0F, 12.0F),
				rotL(0.8F, 18.0F, -28.0F, -24.0F),
				rotL(1.12F, 16.2F, -25.2F, -21.6F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -32.0F, 14.0F, 0.0F),
				rotL(0.8F, 20.0F, 0.0F, -8.0F),
				rotL(1.12F, 18.0F, 0.0F, -7.2F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -12.0F, 0.0F, 0.0F),
				rotL(0.8F, 6.0F, 0.0F, 0.0F),
				rotL(1.12F, 5.4F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, 22.0F, 0.0F, 0.0F),
				rotL(0.8F, 8.0F, 0.0F, 0.0F),
				rotL(1.12F, 7.2F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -10.0F, 0.0F, 0.0F),
				rotL(0.8F, -14.0F, 0.0F, 0.0F),
				rotL(1.12F, -12.6F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -12.0F, 0.0F, 0.0F),
				rotL(0.8F, 6.0F, 0.0F, 0.0F),
				rotL(1.12F, 5.4F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, 22.0F, 0.0F, 0.0F),
				rotL(0.8F, 8.0F, 0.0F, 0.0F),
				rotL(1.12F, 7.2F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.55F, -10.0F, 0.0F, 0.0F),
				rotL(0.8F, -14.0F, 0.0F, 0.0F),
				rotL(1.12F, -12.6F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
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
			return b.build();
		}
	}

	private static final class ShieldBlockAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.05F, 0.0F, -0.001F, 0.0F),
				posL(0.1F, 0.0F, -0.04F, 0.0F),
				posL(0.15F, 0.0F, -0.105F, 0.0F),
				posL(0.2F, 0.0F, -0.196F, 0.0F),
				posL(0.25F, 0.0F, -0.313F, 0.0F),
				posL(0.3F, 0.0F, -0.373F, 0.167F),
				posL(0.35F, 0.0F, -0.418F, 0.375F),
				posL(0.4F, 0.0F, -0.465F, 0.583F),
				posL(0.45F, 0.0F, -0.501F, 0.703F),
				posL(0.5F, 0.0F, -0.48F, 0.469F),
				posL(0.55F, 0.0F, -0.46F, 0.234F),
				posL(0.6F, 0.0F, -0.441F, 0.0F),
				posL(0.65F, 0.0F, -0.422F, 0.0F),
				posL(0.7F, 0.0F, -0.403F, 0.0F),
				posL(0.75F, 0.0F, -0.385F, 0.0F),
				posL(0.8F, 0.0F, -0.367F, 0.0F),
				posL(0.85F, 0.0F, -0.349F, 0.0F),
				posL(0.9F, 0.0F, -0.307F, 0.0F),
				posL(0.95F, 0.0F, -0.232F, 0.0F),
				posL(1.0F, 0.0F, -0.168F, 0.0F),
				posL(1.05F, 0.0F, -0.113F, 0.0F),
				posL(1.1F, 0.0F, -0.068F, 0.0F),
				posL(1.15F, 0.0F, -0.033F, 0.0F),
				posL(1.2F, 0.0F, -0.008F, 0.0F),
				posL(1.25F, 0.0F, 0.007F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, 4.0F, 0.0F, 0.0F),
				rotL(0.44F, -1.0F, 0.0F, 0.0F),
				rotL(0.88F, 4.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, -14.0F, 0.0F, 0.0F),
				rotL(0.44F, -14.0F, 0.0F, 0.0F),
				rotL(0.88F, -14.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("apron_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, 6.0F, 0.0F, 0.0F),
				rotL(0.44F, -2.0F, 0.0F, 0.0F),
				rotL(0.88F, 6.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.44F, 1.06F, 1.06F, 1.06F),
				sclL(1.3F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.44F, -15.0F, -95.0F, 10.0F),
				rotL(1.3F, 0.0F, -180.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.3F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, 8.0F, 0.0F, 0.0F),
				rotL(0.44F, 3.0F, 0.0F, 0.0F),
				rotL(0.88F, 8.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, -46.0F, -24.0F, -22.0F),
				rotL(0.44F, -42.0F, -24.0F, -22.0F),
				rotL(0.88F, -46.0F, -24.0F, -22.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, -52.0F, -14.0F, -8.0F),
				rotL(0.44F, -57.0F, -14.0F, -8.0F),
				rotL(0.88F, -52.0F, -14.0F, -8.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, -46.0F, 24.0F, 22.0F),
				rotL(0.44F, -42.0F, 24.0F, 22.0F),
				rotL(0.88F, -46.0F, 24.0F, 22.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, -52.0F, 14.0F, 8.0F),
				rotL(0.44F, -57.0F, 14.0F, 8.0F),
				rotL(0.88F, -52.0F, 14.0F, 8.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, -24.0F, -26.0F, -12.0F),
				rotL(0.44F, -21.0F, -26.0F, -12.0F),
				rotL(0.88F, -24.0F, -26.0F, -12.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, -36.0F, -12.0F, 0.0F),
				rotL(0.44F, -40.0F, -12.0F, 0.0F),
				rotL(0.88F, -36.0F, -12.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, -24.0F, 26.0F, 12.0F),
				rotL(0.44F, -21.0F, 26.0F, 12.0F),
				rotL(0.88F, -24.0F, 26.0F, 12.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, -36.0F, 12.0F, 0.0F),
				rotL(0.44F, -40.0F, 12.0F, 0.0F),
				rotL(0.88F, -36.0F, 12.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, -16.0F, 0.0F, 0.0F),
				rotL(0.44F, -20.0F, 0.0F, 0.0F),
				rotL(0.88F, -16.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, 30.0F, 0.0F, 0.0F),
				rotL(0.44F, 36.0F, 0.0F, 0.0F),
				rotL(0.88F, 30.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, -14.0F, 0.0F, 0.0F),
				rotL(0.44F, -16.0F, 0.0F, 0.0F),
				rotL(0.88F, -14.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, -16.0F, 0.0F, 0.0F),
				rotL(0.44F, -20.0F, 0.0F, 0.0F),
				rotL(0.88F, -16.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, 30.0F, 0.0F, 0.0F),
				rotL(0.44F, 36.0F, 0.0F, 0.0F),
				rotL(0.88F, 30.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.26F, -14.0F, 0.0F, 0.0F),
				rotL(0.44F, -16.0F, 0.0F, 0.0F),
				rotL(0.88F, -14.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.3F);
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
				posL(0.05F, 0.0F, -0.001F, 0.0F),
				posL(0.1F, 0.0F, -0.039F, 0.0F),
				posL(0.15F, 0.0F, -0.102F, 0.0F),
				posL(0.2F, 0.0F, -0.098F, 0.0F),
				posL(0.25F, 0.0F, -0.075F, 0.0F),
				posL(0.3F, 0.0F, -0.054F, 0.0F),
				posL(0.35F, 0.0F, -0.037F, 0.0F),
				posL(0.4F, 0.0F, -0.022F, 0.0F),
				posL(0.45F, 0.0F, -0.01F, 0.0F),
				posL(0.5F, 0.0F, 0.0F, 0.0F),
				posL(0.55F, 0.0F, 0.007F, 0.0F),
				posL(0.6F, 0.0F, 0.011F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -10.0F, 6.0F, 4.0F),
				rotL(0.38F, 4.0F, -2.0F, -1.0F),
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
				rotL(0.16F, -15.0F, 8.0F, 5.0F),
				rotL(0.38F, 5.0F, -3.0F, -2.0F),
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
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.16F, 0.88F, 0.88F, 0.88F),
				sclL(0.36F, 1.05F, 1.05F, 1.05F),
				sclL(0.65F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.65F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -16.0F, -10.0F, -6.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 8.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 18.0F, 10.0F, 16.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -14.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 14.0F, -10.0F, -16.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -14.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 12.0F, 14.0F, 10.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 12.0F, -14.0F, -10.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -10.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 18.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -8.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -10.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, 18.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -8.0F, 0.0F, 0.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
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
				posL(0.025F, 0.0F, 0.152F, 0.066F),
				posL(0.05F, 0.0F, 0.257F, 0.132F),
				posL(0.075F, 0.0F, 0.343F, 0.197F),
				posL(0.1F, 0.0F, 0.412F, 0.263F),
				posL(0.125F, 0.0F, 0.463F, 0.329F),
				posL(0.15F, 0.0F, 0.496F, 0.395F),
				posL(0.175F, 0.0F, 0.512F, 0.461F),
				posL(0.2F, 0.0F, 0.51F, 0.526F),
				posL(0.225F, 0.0F, 0.491F, 0.592F),
				posL(0.25F, 0.0F, 0.455F, 0.658F),
				posL(0.275F, 0.0F, 0.402F, 0.724F),
				posL(0.3F, 0.0F, 0.333F, 0.789F),
				posL(0.325F, 0.0F, 0.248F, 0.855F),
				posL(0.35F, 0.0F, 0.148F, 0.921F),
				posL(0.375F, 0.0F, 0.032F, 0.987F),
				posL(0.4F, 0.0F, -0.049F, 0.843F),
				posL(0.425F, 0.0F, -0.135F, 0.646F),
				posL(0.45F, 0.0F, -0.238F, 0.45F),
				posL(0.475F, 0.0F, -0.324F, 0.254F),
				posL(0.5F, 0.0F, -0.081F, 0.057F),
				posL(0.525F, 0.0F, 0.094F, -0.139F),
				posL(0.55F, 0.0F, 0.203F, -0.336F),
				posL(0.575F, 0.0F, 0.246F, -0.532F),
				posL(0.6F, 0.0F, 0.223F, -0.729F),
				posL(0.625F, 0.0F, 0.135F, -0.925F),
				posL(0.65F, 0.0F, 0.099F, -1.121F),
				posL(0.675F, 0.0F, 0.32F, -1.318F),
				posL(0.7F, 0.0F, 0.478F, -1.514F),
				posL(0.725F, 0.0F, 0.573F, -1.711F),
				posL(0.75F, 0.0F, 0.607F, -1.907F),
				posL(0.775F, 0.0F, 0.58F, -2.104F),
				posL(0.8F, 0.0F, 0.494F, -2.3F),
				posL(0.825F, 0.0F, 0.35F, -2.496F),
				posL(0.85F, 0.0F, 0.151F, -2.693F),
				posL(0.875F, 0.0F, -0.102F, -2.889F),
				posL(0.9F, 0.0F, -0.406F, -3.086F),
				posL(0.925F, 0.0F, -0.757F, -3.282F),
				posL(0.95F, 0.0F, -1.153F, -3.479F),
				posL(0.975F, 0.0F, -1.59F, -3.675F),
				posL(1.0F, 0.0F, -2.065F, -3.871F),
				posL(1.025F, 0.0F, -2.574F, -4.068F),
				posL(1.05F, 0.0F, -3.112F, -4.264F),
				posL(1.075F, 0.0F, -3.677F, -4.461F),
				posL(1.1F, 0.0F, -3.799F, -4.5F),
				posL(1.125F, 0.0F, -3.806F, -4.5F),
				posL(1.15F, 0.0F, -3.814F, -4.5F),
				posL(1.175F, 0.0F, -3.822F, -4.5F),
				posL(1.2F, 0.0F, -3.83F, -4.5F),
				posL(1.225F, 0.0F, -3.837F, -4.5F),
				posL(1.25F, 0.0F, -3.845F, -4.5F),
				posL(1.275F, 0.0F, -3.853F, -4.5F),
				posL(1.3F, 0.0F, -3.861F, -4.5F),
				posL(1.325F, 0.0F, -3.869F, -4.5F),
				posL(1.35F, 0.0F, -3.877F, -4.5F),
				posL(1.375F, 0.0F, -3.885F, -4.5F),
				posL(1.4F, 0.0F, -3.893F, -4.5F),
				posL(1.425F, 0.0F, -3.901F, -4.5F),
				posL(1.45F, 0.0F, -3.909F, -4.5F),
				posL(1.475F, 0.0F, -3.916F, -4.5F)
			));
		}
		private static void ch_1_p1(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(1.5F, 0.0F, -3.924F, -4.5F),
				posL(1.525F, 0.0F, -3.933F, -4.5F),
				posL(1.55F, 0.0F, -3.941F, -4.5F),
				posL(1.575F, 0.0F, -3.949F, -4.5F),
				posL(1.6F, 0.0F, -3.957F, -4.5F),
				posL(1.625F, 0.0F, -3.965F, -4.5F),
				posL(1.65F, 0.0F, -3.973F, -4.5F),
				posL(1.675F, 0.0F, -3.981F, -4.5F),
				posL(1.7F, 0.0F, -3.989F, -4.5F),
				posL(1.725F, 0.0F, -3.997F, -4.5F),
				posL(1.75F, 0.0F, -4.006F, -4.5F),
				posL(1.775F, 0.0F, -4.014F, -4.5F),
				posL(1.8F, 0.0F, -4.022F, -4.5F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			List<Keyframe> kfs = new ArrayList<>(73);
			ch_1_p0(kfs);
			ch_1_p1(kfs);
			b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION, kfs.toArray(new Keyframe[0])));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.08F, 83.42F, 0.0F, 3.0F),
				rotL(1.8F, 86.0F, 0.0F, 3.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("waist_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -14.0F, 6.0F, 5.0F),
				rotL(1.08F, 2.0F, 0.0F, 0.0F),
				rotL(1.8F, 2.0F, 0.0F, 0.0F)
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
				rotL(1.08F, -6.0F, 0.0F, 0.0F),
				rotL(1.8F, -6.0F, 0.0F, 0.0F)
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
				rotL(0.38F, -18.0F, 8.0F, 6.0F),
				rotL(1.08F, 2.0F, 3.0F, 0.0F),
				rotL(1.8F, 2.0F, 3.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("portal_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.3F, 1.08F, 1.08F, 1.08F),
				sclL(0.65F, 0.52F, 0.52F, 0.52F),
				sclL(1.08F, 0.16F, 0.16F, 0.16F),
				sclL(1.8F, 0.12F, 0.12F, 0.12F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.08F, -45.0F, -240.0F, 30.0F),
				rotL(1.8F, -45.0F, -260.0F, 30.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("portal_heart_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(1.08F, 0.1F, 0.1F, 0.1F),
				sclL(1.8F, 0.08F, 0.08F, 0.08F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -22.0F, -10.0F, -6.0F),
				rotL(1.08F, -16.0F, -22.0F, -6.0F),
				rotL(1.8F, -16.0F, -22.0F, -6.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 12.0F, 0.0F, 0.0F),
				rotL(1.08F, 6.0F, 0.0F, 0.0F),
				rotL(1.8F, 6.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 20.0F, 14.0F, 18.0F),
				rotL(1.08F, -4.0F, 36.0F, 42.0F),
				rotL(1.8F, -4.0F, 36.0F, 42.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.08F, 20.0F, 0.0F, 14.0F),
				rotL(1.8F, 20.0F, 0.0F, 14.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ul_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 16.0F, -14.0F, -18.0F),
				rotL(1.08F, -2.0F, -36.0F, -42.0F),
				rotL(1.8F, -2.0F, -36.0F, -42.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.08F, 18.0F, 0.0F, -14.0F),
				rotL(1.8F, 18.0F, 0.0F, -14.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ur_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 14.0F, 16.0F, 12.0F),
				rotL(1.08F, 18.0F, 22.0F, 24.0F),
				rotL(1.8F, 18.0F, 22.0F, 24.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("arm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.08F, 36.0F, 0.0F, 6.0F),
				rotL(1.8F, 36.0F, 0.0F, 6.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_ll_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 14.0F, -16.0F, -12.0F),
				rotL(1.08F, 18.0F, -22.0F, -24.0F),
				rotL(1.8F, 18.0F, -22.0F, -24.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("arm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.08F, 36.0F, 0.0F, -6.0F),
				rotL(1.8F, 36.0F, 0.0F, -6.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("forearm_lr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -24.0F, 0.0F, 4.0F),
				rotL(1.08F, -4.0F, 0.0F, 9.0F),
				rotL(1.8F, -4.0F, 0.0F, 9.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 46.0F, 0.0F, 0.0F),
				rotL(1.08F, 10.0F, 0.0F, 0.0F),
				rotL(1.8F, 10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("shin_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -22.0F, 0.0F, 0.0F),
				rotL(1.08F, -18.0F, 0.0F, 0.0F),
				rotL(1.8F, -18.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_43(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_44(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -20.0F, 0.0F, -4.0F),
				rotL(1.08F, -2.0F, 0.0F, -9.0F),
				rotL(1.8F, -2.0F, 0.0F, -9.0F)
			));
		}
		private static void ch_45(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_46(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 42.0F, 0.0F, 0.0F),
				rotL(1.08F, 8.0F, 0.0F, 0.0F),
				rotL(1.8F, 8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_47(AnimationDefinition.Builder b) {
			b.addAnimation("shin_r_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_48(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -22.0F, 0.0F, 0.0F),
				rotL(1.08F, -16.0F, 0.0F, 0.0F),
				rotL(1.8F, -16.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_49(AnimationDefinition.Builder b) {
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
			return b.build();
		}
	}

}
