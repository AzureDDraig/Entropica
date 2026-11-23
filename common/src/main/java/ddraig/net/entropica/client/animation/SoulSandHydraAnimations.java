package ddraig.net.entropica.client.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Exact Blockbench keyframe animations for Soul-Sand Hydra (Fauna 50 — Multi-Headed Serpentine).
 * Split into per-animation static builders and <=60-keyframe helpers so constant pools and method bytecodes stay tiny.
 */
public final class SoulSandHydraAnimations {

	private SoulSandHydraAnimations() {}

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
	public static final AnimationDefinition SUBMERGE_AMBUSH = SubmergeAmbushAnimBuilder.create();
	public static final AnimationDefinition submerge_ambush = SUBMERGE_AMBUSH;
	public static final AnimationDefinition WITHER_ROAR = WitherRoarAnimBuilder.create();
	public static final AnimationDefinition wither_roar = WITHER_ROAR;
	public static final AnimationDefinition HURT = HurtAnimBuilder.create();
	public static final AnimationDefinition hurt = HURT;
	public static final AnimationDefinition DEATH = DeathAnimBuilder.create();
	public static final AnimationDefinition death = DEATH;
	public static final AnimationDefinition SPELL_CHARGE = SpellChargeAnimBuilder.create();
	public static final AnimationDefinition spell_charge = SPELL_CHARGE;
	public static final AnimationDefinition SPELL_RELEASE = SpellReleaseAnimBuilder.create();
	public static final AnimationDefinition spell_release = SPELL_RELEASE;
	public static final AnimationDefinition BREATH_ATTACK = BreathAttackAnimBuilder.create();
	public static final AnimationDefinition breath_attack = BREATH_ATTACK;

	private static final class IdleAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.25F, 0.0F, 0.017F, 0.0F),
				posC(0.5F, 0.0F, 0.064F, 0.0F),
				posC(0.75F, 0.0F, 0.136F, 0.0F),
				posC(1.0F, 0.0F, 0.22F, 0.0F),
				posC(1.25F, 0.0F, 0.304F, 0.0F),
				posC(1.5F, 0.0F, 0.376F, 0.0F),
				posC(1.75F, 0.0F, 0.423F, 0.0F),
				posC(2.0F, 0.0F, 0.44F, 0.0F),
				posC(2.25F, 0.0F, 0.423F, 0.0F),
				posC(2.5F, 0.0F, 0.376F, 0.0F),
				posC(2.75F, 0.0F, 0.304F, 0.0F),
				posC(3.0F, 0.0F, 0.22F, 0.0F),
				posC(3.25F, 0.0F, 0.136F, 0.0F),
				posC(3.5F, 0.0F, 0.064F, 0.0F),
				posC(3.75F, 0.0F, 0.017F, 0.0F),
				posC(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, -0.689F, 0.0F, 0.0F),
				rotC(0.5F, -1.273F, 0.0F, 0.0F),
				rotC(0.75F, -1.663F, 0.0F, 0.0F),
				rotC(1.0F, -1.8F, 0.0F, 0.0F),
				rotC(1.25F, -1.663F, 0.0F, 0.0F),
				rotC(1.5F, -1.273F, 0.0F, 0.0F),
				rotC(1.75F, -0.689F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F),
				rotC(2.25F, 0.689F, 0.0F, 0.0F),
				rotC(2.5F, 1.273F, 0.0F, 0.0F),
				rotC(2.75F, 1.663F, 0.0F, 0.0F),
				rotC(3.0F, 1.8F, 0.0F, 0.0F),
				rotC(3.25F, 1.663F, 0.0F, 0.0F),
				rotC(3.5F, 1.273F, 0.0F, 0.0F),
				rotC(3.75F, 0.689F, 0.0F, 0.0F),
				rotC(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, 0.061F, 0.0F, 0.0F),
				rotC(0.5F, 0.234F, 0.0F, 0.0F),
				rotC(0.75F, 0.494F, 0.0F, 0.0F),
				rotC(1.0F, 0.8F, 0.0F, 0.0F),
				rotC(1.25F, 1.106F, 0.0F, 0.0F),
				rotC(1.5F, 1.366F, 0.0F, 0.0F),
				rotC(1.75F, 1.539F, 0.0F, 0.0F),
				rotC(2.0F, 1.6F, 0.0F, 0.0F),
				rotC(2.25F, 1.539F, 0.0F, 0.0F),
				rotC(2.5F, 1.366F, 0.0F, 0.0F),
				rotC(2.75F, 1.106F, 0.0F, 0.0F),
				rotC(3.0F, 0.8F, 0.0F, 0.0F),
				rotC(3.25F, 0.494F, 0.0F, 0.0F),
				rotC(3.5F, 0.234F, 0.0F, 0.0F),
				rotC(3.75F, 0.061F, 0.0F, 0.0F),
				rotC(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, -0.061F, 0.0F, 0.0F),
				rotC(0.5F, -0.234F, 0.0F, 0.0F),
				rotC(0.75F, -0.494F, 0.0F, 0.0F),
				rotC(1.0F, -0.8F, 0.0F, 0.0F),
				rotC(1.25F, -1.106F, 0.0F, 0.0F),
				rotC(1.5F, -1.366F, 0.0F, 0.0F),
				rotC(1.75F, -1.539F, 0.0F, 0.0F),
				rotC(2.0F, -1.6F, 0.0F, 0.0F),
				rotC(2.25F, -1.539F, 0.0F, 0.0F),
				rotC(2.5F, -1.366F, 0.0F, 0.0F),
				rotC(2.75F, -1.106F, 0.0F, 0.0F),
				rotC(3.0F, -0.8F, 0.0F, 0.0F),
				rotC(3.25F, -0.494F, 0.0F, 0.0F),
				rotC(3.5F, -0.234F, 0.0F, 0.0F),
				rotC(3.75F, -0.061F, 0.0F, 0.0F),
				rotC(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, 0.061F, 0.0F, 0.0F),
				rotC(0.5F, 0.234F, 0.0F, 0.0F),
				rotC(0.75F, 0.494F, 0.0F, 0.0F),
				rotC(1.0F, 0.8F, 0.0F, 0.0F),
				rotC(1.25F, 1.106F, 0.0F, 0.0F),
				rotC(1.5F, 1.366F, 0.0F, 0.0F),
				rotC(1.75F, 1.539F, 0.0F, 0.0F),
				rotC(2.0F, 1.6F, 0.0F, 0.0F),
				rotC(2.25F, 1.539F, 0.0F, 0.0F),
				rotC(2.5F, 1.366F, 0.0F, 0.0F),
				rotC(2.75F, 1.106F, 0.0F, 0.0F),
				rotC(3.0F, 0.8F, 0.0F, 0.0F),
				rotC(3.25F, 0.494F, 0.0F, 0.0F),
				rotC(3.5F, 0.234F, 0.0F, 0.0F),
				rotC(3.75F, 0.061F, 0.0F, 0.0F),
				rotC(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, -0.061F, 0.0F, 0.0F),
				rotC(0.5F, -0.234F, 0.0F, 0.0F),
				rotC(0.75F, -0.494F, 0.0F, 0.0F),
				rotC(1.0F, -0.8F, 0.0F, 0.0F),
				rotC(1.25F, -1.106F, 0.0F, 0.0F),
				rotC(1.5F, -1.366F, 0.0F, 0.0F),
				rotC(1.75F, -1.539F, 0.0F, 0.0F),
				rotC(2.0F, -1.6F, 0.0F, 0.0F),
				rotC(2.25F, -1.539F, 0.0F, 0.0F),
				rotC(2.5F, -1.366F, 0.0F, 0.0F),
				rotC(2.75F, -1.106F, 0.0F, 0.0F),
				rotC(3.0F, -0.8F, 0.0F, 0.0F),
				rotC(3.25F, -0.494F, 0.0F, 0.0F),
				rotC(3.5F, -0.234F, 0.0F, 0.0F),
				rotC(3.75F, -0.061F, 0.0F, 0.0F),
				rotC(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("shank_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, 0.061F, 0.0F, 0.0F),
				rotC(0.5F, 0.234F, 0.0F, 0.0F),
				rotC(0.75F, 0.494F, 0.0F, 0.0F),
				rotC(1.0F, 0.8F, 0.0F, 0.0F),
				rotC(1.25F, 1.106F, 0.0F, 0.0F),
				rotC(1.5F, 1.366F, 0.0F, 0.0F),
				rotC(1.75F, 1.539F, 0.0F, 0.0F),
				rotC(2.0F, 1.6F, 0.0F, 0.0F),
				rotC(2.25F, 1.539F, 0.0F, 0.0F),
				rotC(2.5F, 1.366F, 0.0F, 0.0F),
				rotC(2.75F, 1.106F, 0.0F, 0.0F),
				rotC(3.0F, 0.8F, 0.0F, 0.0F),
				rotC(3.25F, 0.494F, 0.0F, 0.0F),
				rotC(3.5F, 0.234F, 0.0F, 0.0F),
				rotC(3.75F, 0.061F, 0.0F, 0.0F),
				rotC(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("foot_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, -0.061F, 0.0F, 0.0F),
				rotC(0.5F, -0.234F, 0.0F, 0.0F),
				rotC(0.75F, -0.494F, 0.0F, 0.0F),
				rotC(1.0F, -0.8F, 0.0F, 0.0F),
				rotC(1.25F, -1.106F, 0.0F, 0.0F),
				rotC(1.5F, -1.366F, 0.0F, 0.0F),
				rotC(1.75F, -1.539F, 0.0F, 0.0F),
				rotC(2.0F, -1.6F, 0.0F, 0.0F),
				rotC(2.25F, -1.539F, 0.0F, 0.0F),
				rotC(2.5F, -1.366F, 0.0F, 0.0F),
				rotC(2.75F, -1.106F, 0.0F, 0.0F),
				rotC(3.0F, -0.8F, 0.0F, 0.0F),
				rotC(3.25F, -0.494F, 0.0F, 0.0F),
				rotC(3.5F, -0.234F, 0.0F, 0.0F),
				rotC(3.75F, -0.061F, 0.0F, 0.0F),
				rotC(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("shank_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, 0.061F, 0.0F, 0.0F),
				rotC(0.5F, 0.234F, 0.0F, 0.0F),
				rotC(0.75F, 0.494F, 0.0F, 0.0F),
				rotC(1.0F, 0.8F, 0.0F, 0.0F),
				rotC(1.25F, 1.106F, 0.0F, 0.0F),
				rotC(1.5F, 1.366F, 0.0F, 0.0F),
				rotC(1.75F, 1.539F, 0.0F, 0.0F),
				rotC(2.0F, 1.6F, 0.0F, 0.0F),
				rotC(2.25F, 1.539F, 0.0F, 0.0F),
				rotC(2.5F, 1.366F, 0.0F, 0.0F),
				rotC(2.75F, 1.106F, 0.0F, 0.0F),
				rotC(3.0F, 0.8F, 0.0F, 0.0F),
				rotC(3.25F, 0.494F, 0.0F, 0.0F),
				rotC(3.5F, 0.234F, 0.0F, 0.0F),
				rotC(3.75F, 0.061F, 0.0F, 0.0F),
				rotC(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("foot_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, -0.061F, 0.0F, 0.0F),
				rotC(0.5F, -0.234F, 0.0F, 0.0F),
				rotC(0.75F, -0.494F, 0.0F, 0.0F),
				rotC(1.0F, -0.8F, 0.0F, 0.0F),
				rotC(1.25F, -1.106F, 0.0F, 0.0F),
				rotC(1.5F, -1.366F, 0.0F, 0.0F),
				rotC(1.75F, -1.539F, 0.0F, 0.0F),
				rotC(2.0F, -1.6F, 0.0F, 0.0F),
				rotC(2.25F, -1.539F, 0.0F, 0.0F),
				rotC(2.5F, -1.366F, 0.0F, 0.0F),
				rotC(2.75F, -1.106F, 0.0F, 0.0F),
				rotC(3.0F, -0.8F, 0.0F, 0.0F),
				rotC(3.25F, -0.494F, 0.0F, 0.0F),
				rotC(3.5F, -0.234F, 0.0F, 0.0F),
				rotC(3.75F, -0.061F, 0.0F, 0.0F),
				rotC(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, -3.5F, 0.0F),
				rotC(0.25F, -0.957F, -3.234F, -0.765F),
				rotC(0.5F, -1.768F, -2.475F, -1.414F),
				rotC(0.75F, -2.31F, -1.339F, -1.848F),
				rotC(1.0F, -2.5F, 0.0F, -2.0F),
				rotC(1.25F, -2.31F, 1.339F, -1.848F),
				rotC(1.5F, -1.768F, 2.475F, -1.414F),
				rotC(1.75F, -0.957F, 3.234F, -0.765F),
				rotC(2.0F, 0.0F, 3.5F, 0.0F),
				rotC(2.25F, 0.957F, 3.234F, 0.765F),
				rotC(2.5F, 1.768F, 2.475F, 1.414F),
				rotC(2.75F, 2.31F, 1.339F, 1.848F),
				rotC(3.0F, 2.5F, 0.0F, 2.0F),
				rotC(3.25F, 2.31F, -1.339F, 1.848F),
				rotC(3.5F, 1.768F, -2.475F, 1.414F),
				rotC(3.75F, 0.957F, -3.234F, 0.765F),
				rotC(4.0F, 0.0F, -3.5F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 1.438F, -3.51F, 0.0F),
				rotC(0.25F, 0.321F, -3.977F, 0.0F),
				rotC(0.5F, -0.845F, -3.838F, 0.0F),
				rotC(0.75F, -1.882F, -3.115F, 0.0F),
				rotC(1.0F, -2.633F, -1.918F, 0.0F),
				rotC(1.25F, -2.983F, -0.428F, 0.0F),
				rotC(1.5F, -2.879F, 1.126F, 0.0F),
				rotC(1.75F, -2.336F, 2.509F, 0.0F),
				rotC(2.0F, -1.438F, 3.51F, 0.0F),
				rotC(2.25F, -0.321F, 3.977F, 0.0F),
				rotC(2.5F, 0.845F, 3.838F, 0.0F),
				rotC(2.75F, 1.882F, 3.115F, 0.0F),
				rotC(3.0F, 2.633F, 1.918F, 0.0F),
				rotC(3.25F, 2.983F, 0.428F, 0.0F),
				rotC(3.5F, 2.879F, -1.126F, 0.0F),
				rotC(3.75F, 2.336F, -2.509F, 0.0F),
				rotC(4.0F, 1.438F, -3.51F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -2.524F, -2.161F, -1.683F),
				rotC(0.25F, -1.712F, -3.285F, -1.141F),
				rotC(0.5F, -0.639F, -3.908F, -0.426F),
				rotC(0.75F, 0.531F, -3.937F, 0.354F),
				rotC(1.0F, 1.621F, -3.366F, 1.081F),
				rotC(1.25F, 2.464F, -2.283F, 1.642F),
				rotC(1.5F, 2.931F, -0.852F, 1.954F),
				rotC(1.75F, 2.953F, 0.709F, 1.968F),
				rotC(2.0F, 2.524F, 2.161F, 1.683F),
				rotC(2.25F, 1.712F, 3.285F, 1.141F),
				rotC(2.5F, 0.639F, 3.908F, 0.426F),
				rotC(2.75F, -0.531F, 3.937F, -0.354F),
				rotC(3.0F, -1.621F, 3.366F, -1.081F),
				rotC(3.25F, -2.464F, 2.283F, -1.642F),
				rotC(3.5F, -2.931F, 0.852F, -1.954F),
				rotC(3.75F, -2.953F, -0.709F, -1.968F),
				rotC(4.0F, -2.524F, -2.161F, -1.683F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -2.464F, -0.51F, 0.0F),
				rotC(0.25F, -2.113F, -1.602F, 0.0F),
				rotC(0.5F, -1.442F, -2.451F, 0.0F),
				rotC(0.75F, -0.55F, -2.926F, 0.0F),
				rotC(1.0F, 0.425F, -2.956F, 0.0F),
				rotC(1.25F, 1.335F, -2.536F, 0.0F),
				rotC(1.5F, 2.043F, -1.73F, 0.0F),
				rotC(1.75F, 2.439F, -0.66F, 0.0F),
				rotC(2.0F, 2.464F, 0.51F, 0.0F),
				rotC(2.25F, 2.113F, 1.602F, 0.0F),
				rotC(2.5F, 1.442F, 2.451F, 0.0F),
				rotC(2.75F, 0.55F, 2.926F, 0.0F),
				rotC(3.0F, -0.425F, 2.956F, 0.0F),
				rotC(3.25F, -1.335F, 2.536F, 0.0F),
				rotC(3.5F, -2.043F, 1.73F, 0.0F),
				rotC(3.75F, -2.439F, 0.66F, 0.0F),
				rotC(4.0F, -2.464F, -0.51F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("head_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 5.0F, 0.0F),
				rotC(0.25F, 0.765F, 4.619F, 0.957F),
				rotC(0.5F, 1.414F, 3.536F, 1.768F),
				rotC(0.75F, 1.848F, 1.913F, 2.31F),
				rotC(1.0F, 2.0F, 0.0F, 2.5F),
				rotC(1.25F, 1.848F, -1.913F, 2.31F),
				rotC(1.5F, 1.414F, -3.536F, 1.768F),
				rotC(1.75F, 0.765F, -4.619F, 0.957F),
				rotC(2.0F, 0.0F, -5.0F, 0.0F),
				rotC(2.25F, -0.765F, -4.619F, -0.957F),
				rotC(2.5F, -1.414F, -3.536F, -1.768F),
				rotC(2.75F, -1.848F, -1.913F, -2.31F),
				rotC(3.0F, -2.0F, 0.0F, -2.5F),
				rotC(3.25F, -1.848F, 1.913F, -2.31F),
				rotC(3.5F, -1.414F, 3.536F, -1.768F),
				rotC(3.75F, -0.765F, 4.619F, -0.957F),
				rotC(4.0F, 0.0F, 5.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, 1.318F, 0.0F, 0.0F),
				rotC(0.5F, 4.5F, 0.0F, 0.0F),
				rotC(0.75F, 7.682F, 0.0F, 0.0F),
				rotC(1.0F, 9.0F, 0.0F, 0.0F),
				rotC(1.25F, 7.682F, 0.0F, 0.0F),
				rotC(1.5F, 4.5F, 0.0F, 0.0F),
				rotC(1.75F, 1.318F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F),
				rotC(2.25F, 0.0F, 0.0F, 0.0F),
				rotC(2.5F, 0.0F, 0.0F, 0.0F),
				rotC(2.75F, 0.0F, 0.0F, 0.0F),
				rotC(3.0F, 0.0F, 0.0F, 0.0F),
				rotC(3.25F, 0.0F, 0.0F, 0.0F),
				rotC(3.5F, 0.0F, 0.0F, 0.0F),
				rotC(3.75F, 0.0F, 0.0F, 0.0F),
				rotC(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -2.165F, 1.75F, -1.732F),
				rotC(0.25F, -1.522F, 2.777F, -1.218F),
				rotC(0.5F, -0.647F, 3.381F, -0.518F),
				rotC(0.75F, 0.326F, 3.47F, 0.261F),
				rotC(1.0F, 1.25F, 3.031F, 1.0F),
				rotC(1.25F, 1.983F, 2.131F, 1.587F),
				rotC(1.5F, 2.415F, 0.906F, 1.932F),
				rotC(1.75F, 2.479F, -0.457F, 1.983F),
				rotC(2.0F, 2.165F, -1.75F, 1.732F),
				rotC(2.25F, 1.522F, -2.777F, 1.218F),
				rotC(2.5F, 0.647F, -3.381F, 0.518F),
				rotC(2.75F, -0.326F, -3.47F, -0.261F),
				rotC(3.0F, -1.25F, -3.031F, -1.0F),
				rotC(3.25F, -1.983F, -2.131F, -1.587F),
				rotC(3.5F, -2.415F, -0.906F, -1.932F),
				rotC(3.75F, -2.479F, 0.457F, -1.983F),
				rotC(4.0F, -2.165F, 1.75F, -1.732F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -2.999F, 0.094F, 0.0F),
				rotC(0.25F, -2.744F, 1.618F, 0.0F),
				rotC(0.5F, -2.071F, 2.894F, 0.0F),
				rotC(0.75F, -1.082F, 3.731F, 0.0F),
				rotC(1.0F, 0.071F, 3.999F, 0.0F),
				rotC(1.25F, 1.213F, 3.658F, 0.0F),
				rotC(1.5F, 2.171F, 2.761F, 0.0F),
				rotC(1.75F, 2.798F, 1.443F, 0.0F),
				rotC(2.0F, 2.999F, -0.094F, 0.0F),
				rotC(2.25F, 2.744F, -1.618F, 0.0F),
				rotC(2.5F, 2.071F, -2.894F, 0.0F),
				rotC(2.75F, 1.082F, -3.731F, 0.0F),
				rotC(3.0F, -0.071F, -3.999F, 0.0F),
				rotC(3.25F, -1.213F, -3.658F, 0.0F),
				rotC(3.5F, -2.171F, -2.761F, 0.0F),
				rotC(3.75F, -2.798F, -1.443F, 0.0F),
				rotC(4.0F, -2.999F, 0.094F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 2.666F, -1.834F, 1.777F),
				rotC(0.25F, 2.989F, -0.334F, 1.993F),
				rotC(0.5F, 2.858F, 1.216F, 1.905F),
				rotC(0.75F, 2.291F, 2.582F, 1.527F),
				rotC(1.0F, 1.376F, 3.555F, 0.917F),
				rotC(1.25F, 0.251F, 3.986F, 0.167F),
				rotC(1.5F, -0.912F, 3.811F, -0.608F),
				rotC(1.75F, -1.937F, 3.055F, -1.291F),
				rotC(2.0F, -2.666F, 1.834F, -1.777F),
				rotC(2.25F, -2.989F, 0.334F, -1.993F),
				rotC(2.5F, -2.858F, -1.216F, -1.905F),
				rotC(2.75F, -2.291F, -2.582F, -1.527F),
				rotC(3.0F, -1.376F, -3.555F, -0.917F),
				rotC(3.25F, -0.251F, -3.986F, -0.167F),
				rotC(3.5F, 0.912F, -3.811F, 0.608F),
				rotC(3.75F, 1.937F, -3.055F, 1.291F),
				rotC(4.0F, 2.666F, -1.834F, 1.777F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 1.6F, -2.305F, 0.0F),
				rotC(0.25F, 2.213F, -1.395F, 0.0F),
				rotC(0.5F, 2.49F, -0.273F, 0.0F),
				rotC(0.75F, 2.387F, 0.891F, 0.0F),
				rotC(1.0F, 1.921F, 1.92F, 0.0F),
				rotC(1.25F, 1.163F, 2.656F, 0.0F),
				rotC(1.5F, 0.227F, 2.988F, 0.0F),
				rotC(1.75F, -0.743F, 2.865F, 0.0F),
				rotC(2.0F, -1.6F, 2.305F, 0.0F),
				rotC(2.25F, -2.213F, 1.395F, 0.0F),
				rotC(2.5F, -2.49F, 0.273F, 0.0F),
				rotC(2.75F, -2.387F, -0.891F, 0.0F),
				rotC(3.0F, -1.921F, -1.92F, 0.0F),
				rotC(3.25F, -1.163F, -2.656F, 0.0F),
				rotC(3.5F, -0.227F, -2.988F, 0.0F),
				rotC(3.75F, 0.743F, -2.865F, 0.0F),
				rotC(4.0F, 1.6F, -2.305F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("head_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 1.732F, -2.5F, 2.165F),
				rotC(0.25F, 1.218F, -3.967F, 1.522F),
				rotC(0.5F, 0.518F, -4.83F, 0.647F),
				rotC(0.75F, -0.261F, -4.957F, -0.326F),
				rotC(1.0F, -1.0F, -4.33F, -1.25F),
				rotC(1.25F, -1.587F, -3.044F, -1.983F),
				rotC(1.5F, -1.932F, -1.294F, -2.415F),
				rotC(1.75F, -1.983F, 0.653F, -2.479F),
				rotC(2.0F, -1.732F, 2.5F, -2.165F),
				rotC(2.25F, -1.218F, 3.967F, -1.522F),
				rotC(2.5F, -0.518F, 4.83F, -0.647F),
				rotC(2.75F, 0.261F, 4.957F, 0.326F),
				rotC(3.0F, 1.0F, 4.33F, 1.25F),
				rotC(3.25F, 1.587F, 3.044F, 1.983F),
				rotC(3.5F, 1.932F, 1.294F, 2.415F),
				rotC(3.75F, 1.983F, -0.653F, 2.479F),
				rotC(4.0F, 1.732F, -2.5F, 2.165F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 6.75F, 0.0F, 0.0F),
				rotC(0.25F, 3.335F, 0.0F, 0.0F),
				rotC(0.5F, 0.603F, 0.0F, 0.0F),
				rotC(0.75F, 0.0F, 0.0F, 0.0F),
				rotC(1.0F, 0.0F, 0.0F, 0.0F),
				rotC(1.25F, 0.0F, 0.0F, 0.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F),
				rotC(1.75F, 0.0F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F),
				rotC(2.25F, 0.0F, 0.0F, 0.0F),
				rotC(2.5F, 0.0F, 0.0F, 0.0F),
				rotC(2.75F, 0.153F, 0.0F, 0.0F),
				rotC(3.0F, 2.25F, 0.0F, 0.0F),
				rotC(3.25F, 5.665F, 0.0F, 0.0F),
				rotC(3.5F, 8.397F, 0.0F, 0.0F),
				rotC(3.75F, 8.847F, 0.0F, 0.0F),
				rotC(4.0F, 6.75F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 2.165F, 1.75F, 1.732F),
				rotC(0.25F, 2.479F, 0.457F, 1.983F),
				rotC(0.5F, 2.415F, -0.906F, 1.932F),
				rotC(0.75F, 1.983F, -2.131F, 1.587F),
				rotC(1.0F, 1.25F, -3.031F, 1.0F),
				rotC(1.25F, 0.326F, -3.47F, 0.261F),
				rotC(1.5F, -0.647F, -3.381F, -0.518F),
				rotC(1.75F, -1.522F, -2.777F, -1.218F),
				rotC(2.0F, -2.165F, -1.75F, -1.732F),
				rotC(2.25F, -2.479F, -0.457F, -1.983F),
				rotC(2.5F, -2.415F, 0.906F, -1.932F),
				rotC(2.75F, -1.983F, 2.131F, -1.587F),
				rotC(3.0F, -1.25F, 3.031F, -1.0F),
				rotC(3.25F, -0.326F, 3.47F, -0.261F),
				rotC(3.5F, 0.647F, 3.381F, 0.518F),
				rotC(3.75F, 1.522F, 2.777F, 1.218F),
				rotC(4.0F, 2.165F, 1.75F, 1.732F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 1.561F, 3.416F, 0.0F),
				rotC(0.25F, 2.422F, 2.359F, 0.0F),
				rotC(0.5F, 2.915F, 0.944F, 0.0F),
				rotC(0.75F, 2.964F, -0.616F, 0.0F),
				rotC(1.0F, 2.562F, -2.081F, 0.0F),
				rotC(1.25F, 1.77F, -3.23F, 0.0F),
				rotC(1.5F, 0.708F, -3.887F, 0.0F),
				rotC(1.75F, -0.462F, -3.952F, 0.0F),
				rotC(2.0F, -1.561F, -3.416F, 0.0F),
				rotC(2.25F, -2.422F, -2.359F, 0.0F),
				rotC(2.5F, -2.915F, -0.944F, 0.0F),
				rotC(2.75F, -2.964F, 0.616F, 0.0F),
				rotC(3.0F, -2.562F, 2.081F, 0.0F),
				rotC(3.25F, -1.77F, 3.23F, 0.0F),
				rotC(3.5F, -0.708F, 3.887F, 0.0F),
				rotC(3.75F, 0.462F, 3.952F, 0.0F),
				rotC(4.0F, 1.561F, 3.416F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -0.142F, 3.996F, -0.094F),
				rotC(0.25F, -1.278F, 3.619F, -0.852F),
				rotC(0.5F, -2.219F, 2.692F, -1.479F),
				rotC(0.75F, -2.823F, 1.355F, -1.882F),
				rotC(1.0F, -2.997F, -0.189F, -1.998F),
				rotC(1.25F, -2.714F, -1.703F, -1.81F),
				rotC(1.5F, -2.019F, -2.959F, -1.346F),
				rotC(1.75F, -1.016F, -3.764F, -0.677F),
				rotC(2.0F, 0.142F, -3.996F, 0.094F),
				rotC(2.25F, 1.278F, -3.619F, 0.852F),
				rotC(2.5F, 2.219F, -2.692F, 1.479F),
				rotC(2.75F, 2.823F, -1.355F, 1.882F),
				rotC(3.0F, 2.997F, 0.189F, 1.998F),
				rotC(3.25F, 2.714F, 1.703F, 1.81F),
				rotC(3.5F, 2.019F, 2.959F, 1.346F),
				rotC(3.75F, 1.016F, 3.764F, 0.677F),
				rotC(4.0F, -0.142F, 3.996F, -0.094F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.864F, 2.815F, 0.0F),
				rotC(0.25F, -0.1F, 2.998F, 0.0F),
				rotC(0.5F, -1.048F, 2.724F, 0.0F),
				rotC(0.75F, -1.837F, 2.035F, 0.0F),
				rotC(1.0F, -2.346F, 1.037F, 0.0F),
				rotC(1.25F, -2.498F, -0.12F, 0.0F),
				rotC(1.5F, -2.27F, -1.258F, 0.0F),
				rotC(1.75F, -1.696F, -2.204F, 0.0F),
				rotC(2.0F, -0.864F, -2.815F, 0.0F),
				rotC(2.25F, 0.1F, -2.998F, 0.0F),
				rotC(2.5F, 1.048F, -2.724F, 0.0F),
				rotC(2.75F, 1.837F, -2.035F, 0.0F),
				rotC(3.0F, 2.346F, -1.037F, 0.0F),
				rotC(3.25F, 2.498F, 0.12F, 0.0F),
				rotC(3.5F, 2.27F, 1.258F, 0.0F),
				rotC(3.75F, 1.696F, 2.204F, 0.0F),
				rotC(4.0F, 0.864F, 2.815F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("head_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -1.732F, -2.5F, -2.165F),
				rotC(0.25F, -1.983F, -0.653F, -2.479F),
				rotC(0.5F, -1.932F, 1.294F, -2.415F),
				rotC(0.75F, -1.587F, 3.044F, -1.983F),
				rotC(1.0F, -1.0F, 4.33F, -1.25F),
				rotC(1.25F, -0.261F, 4.957F, -0.326F),
				rotC(1.5F, 0.518F, 4.83F, 0.647F),
				rotC(1.75F, 1.218F, 3.967F, 1.522F),
				rotC(2.0F, 1.732F, 2.5F, 2.165F),
				rotC(2.25F, 1.983F, 0.653F, 2.479F),
				rotC(2.5F, 1.932F, -1.294F, 2.415F),
				rotC(2.75F, 1.587F, -3.044F, 1.983F),
				rotC(3.0F, 1.0F, -4.33F, 1.25F),
				rotC(3.25F, 0.261F, -4.957F, 0.326F),
				rotC(3.5F, -0.518F, -4.83F, -0.647F),
				rotC(3.75F, -1.218F, -3.967F, -1.522F),
				rotC(4.0F, -1.732F, -2.5F, -2.165F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, 0.0F, 0.0F, 0.0F),
				rotC(0.5F, 0.0F, 0.0F, 0.0F),
				rotC(0.75F, 0.0F, 0.0F, 0.0F),
				rotC(1.0F, 0.0F, 0.0F, 0.0F),
				rotC(1.25F, 0.0F, 0.0F, 0.0F),
				rotC(1.5F, 0.603F, 0.0F, 0.0F),
				rotC(1.75F, 3.335F, 0.0F, 0.0F),
				rotC(2.0F, 6.75F, 0.0F, 0.0F),
				rotC(2.25F, 8.847F, 0.0F, 0.0F),
				rotC(2.5F, 8.397F, 0.0F, 0.0F),
				rotC(2.75F, 5.665F, 0.0F, 0.0F),
				rotC(3.0F, 2.25F, 0.0F, 0.0F),
				rotC(3.25F, 0.153F, 0.0F, 0.0F),
				rotC(3.5F, 0.0F, 0.0F, 0.0F),
				rotC(3.75F, 0.0F, 0.0F, 0.0F),
				rotC(4.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.852F, 2.001F, 0.0F),
				rotC(0.25F, 1.154F, 0.263F, 0.0F),
				rotC(0.5F, 1.463F, -1.514F, 0.0F),
				rotC(0.75F, 1.732F, -3.061F, 0.0F),
				rotC(1.0F, 1.92F, -4.142F, 0.0F),
				rotC(1.25F, 1.999F, -4.592F, 0.0F),
				rotC(1.5F, 1.955F, -4.344F, 0.0F),
				rotC(1.75F, 1.797F, -3.434F, 0.0F),
				rotC(2.0F, 1.548F, -2.001F, 0.0F),
				rotC(2.25F, 1.246F, -0.263F, 0.0F),
				rotC(2.5F, 0.937F, 1.514F, 0.0F),
				rotC(2.75F, 0.668F, 3.061F, 0.0F),
				rotC(3.0F, 0.48F, 4.142F, 0.0F),
				rotC(3.25F, 0.401F, 4.592F, 0.0F),
				rotC(3.5F, 0.445F, 4.344F, 0.0F),
				rotC(3.75F, 0.603F, 3.434F, 0.0F),
				rotC(4.0F, 0.852F, 2.001F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.573F, 4.465F, 0.0F),
				rotC(0.25F, 0.811F, 2.769F, 0.0F),
				rotC(0.5F, 1.109F, 0.652F, 0.0F),
				rotC(0.75F, 1.42F, -1.565F, 0.0F),
				rotC(1.0F, 1.697F, -3.543F, 0.0F),
				rotC(1.25F, 1.899F, -4.982F, 0.0F),
				rotC(1.5F, 1.995F, -5.663F, 0.0F),
				rotC(1.75F, 1.969F, -5.481F, 0.0F),
				rotC(2.0F, 1.827F, -4.465F, 0.0F),
				rotC(2.25F, 1.589F, -2.769F, 0.0F),
				rotC(2.5F, 1.291F, -0.652F, 0.0F),
				rotC(2.75F, 0.98F, 1.565F, 0.0F),
				rotC(3.0F, 0.703F, 3.543F, 0.0F),
				rotC(3.25F, 0.501F, 4.982F, 0.0F),
				rotC(3.5F, 0.405F, 5.663F, 0.0F),
				rotC(3.75F, 0.431F, 5.481F, 0.0F),
				rotC(4.0F, 0.573F, 4.465F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.419F, 6.635F, 0.0F),
				rotC(0.25F, 0.546F, 5.56F, 0.0F),
				rotC(0.5F, 0.772F, 3.639F, 0.0F),
				rotC(0.75F, 1.063F, 1.163F, 0.0F),
				rotC(1.0F, 1.375F, -1.489F, 0.0F),
				rotC(1.25F, 1.661F, -3.915F, 0.0F),
				rotC(1.5F, 1.876F, -5.745F, 0.0F),
				rotC(1.75F, 1.988F, -6.7F, 0.0F),
				rotC(2.0F, 1.981F, -6.635F, 0.0F),
				rotC(2.25F, 1.854F, -5.56F, 0.0F),
				rotC(2.5F, 1.628F, -3.639F, 0.0F),
				rotC(2.75F, 1.337F, -1.163F, 0.0F),
				rotC(3.0F, 1.025F, 1.489F, 0.0F),
				rotC(3.25F, 0.739F, 3.915F, 0.0F),
				rotC(3.5F, 0.524F, 5.745F, 0.0F),
				rotC(3.75F, 0.412F, 6.7F, 0.0F),
				rotC(4.0F, 0.419F, 6.635F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.421F, 7.693F, 0.0F),
				rotC(0.25F, 0.411F, 7.795F, 0.0F),
				rotC(0.5F, 0.521F, 6.709F, 0.0F),
				rotC(0.75F, 0.734F, 4.602F, 0.0F),
				rotC(1.0F, 1.018F, 1.795F, 0.0F),
				rotC(1.25F, 1.33F, -1.286F, 0.0F),
				rotC(1.5F, 1.622F, -4.171F, 0.0F),
				rotC(1.75F, 1.85F, -6.421F, 0.0F),
				rotC(2.0F, 1.979F, -7.693F, 0.0F),
				rotC(2.25F, 1.989F, -7.795F, 0.0F),
				rotC(2.5F, 1.879F, -6.709F, 0.0F),
				rotC(2.75F, 1.666F, -4.602F, 0.0F),
				rotC(3.0F, 1.382F, -1.795F, 0.0F),
				rotC(3.25F, 1.07F, 1.286F, 0.0F),
				rotC(3.5F, 0.778F, 4.171F, 0.0F),
				rotC(3.75F, 0.55F, 6.421F, 0.0F),
				rotC(4.0F, 0.421F, 7.693F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.578F, 7.003F, 0.0F),
				rotC(0.25F, 0.433F, 8.633F, 0.0F),
				rotC(0.5F, 0.405F, 8.949F, 0.0F),
				rotC(0.75F, 0.498F, 7.903F, 0.0F),
				rotC(1.0F, 0.697F, 5.654F, 0.0F),
				rotC(1.25F, 0.974F, 2.543F, 0.0F),
				rotC(1.5F, 1.285F, -0.954F, 0.0F),
				rotC(1.75F, 1.583F, -4.306F, 0.0F),
				rotC(2.0F, 1.822F, -7.003F, 0.0F),
				rotC(2.25F, 1.967F, -8.633F, 0.0F),
				rotC(2.5F, 1.995F, -8.949F, 0.0F),
				rotC(2.75F, 1.902F, -7.903F, 0.0F),
				rotC(3.0F, 1.703F, -5.654F, 0.0F),
				rotC(3.25F, 1.426F, -2.543F, 0.0F),
				rotC(3.5F, 1.115F, 0.954F, 0.0F),
				rotC(3.75F, 0.817F, 4.306F, 0.0F),
				rotC(4.0F, 0.578F, 7.003F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.858F, 4.317F, 0.0F),
				rotC(0.25F, 0.607F, 7.482F, 0.0F),
				rotC(0.5F, 0.447F, 9.509F, 0.0F),
				rotC(0.75F, 0.401F, 10.088F, 0.0F),
				rotC(1.0F, 0.477F, 9.131F, 0.0F),
				rotC(1.25F, 0.663F, 6.784F, 0.0F),
				rotC(1.5F, 0.93F, 3.404F, 0.0F),
				rotC(1.75F, 1.239F, -0.494F, 0.0F),
				rotC(2.0F, 1.542F, -4.317F, 0.0F),
				rotC(2.25F, 1.793F, -7.482F, 0.0F),
				rotC(2.5F, 1.953F, -9.509F, 0.0F),
				rotC(2.75F, 1.999F, -10.088F, 0.0F),
				rotC(3.0F, 1.923F, -9.131F, 0.0F),
				rotC(3.25F, 1.737F, -6.784F, 0.0F),
				rotC(3.5F, 1.47F, -3.404F, 0.0F),
				rotC(3.75F, 1.161F, 0.494F, 0.0F),
				rotC(4.0F, 0.858F, 4.317F, 0.0F)
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
			return b.build();
		}
	}

	private static final class WalkAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.1F, 0.0F, 0.0F),
				rotC(0.125F, 0.51F, -0.957F, -0.842F),
				rotC(0.25F, 1.5F, -1.768F, -1.556F),
				rotC(0.375F, 2.49F, -2.31F, -2.033F),
				rotC(0.5F, 2.9F, -2.5F, -2.2F),
				rotC(0.625F, 2.49F, -2.31F, -2.033F),
				rotC(0.75F, 1.5F, -1.768F, -1.556F),
				rotC(0.875F, 0.51F, -0.957F, -0.842F),
				rotC(1.0F, 0.1F, 0.0F, 0.0F),
				rotC(1.125F, 0.51F, 0.957F, 0.842F),
				rotC(1.25F, 1.5F, 1.768F, 1.556F),
				rotC(1.375F, 2.49F, 2.31F, 2.033F),
				rotC(1.5F, 2.9F, 2.5F, 2.2F),
				rotC(1.625F, 2.49F, 2.31F, 2.033F),
				rotC(1.75F, 1.5F, 1.768F, 1.556F),
				rotC(1.875F, 0.51F, 0.957F, 0.842F),
				rotC(2.0F, 0.1F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.62F, 0.0F),
				posC(0.125F, -0.172F, 0.792F, 0.0F),
				posC(0.25F, -0.318F, 0.938F, 0.0F),
				posC(0.375F, -0.416F, 1.036F, 0.0F),
				posC(0.5F, -0.45F, 1.07F, 0.0F),
				posC(0.625F, -0.416F, 1.036F, 0.0F),
				posC(0.75F, -0.318F, 0.938F, 0.0F),
				posC(0.875F, -0.172F, 0.792F, 0.0F),
				posC(1.0F, 0.0F, 0.62F, 0.0F),
				posC(1.125F, 0.172F, 0.792F, 0.0F),
				posC(1.25F, 0.318F, 0.938F, 0.0F),
				posC(1.375F, 0.416F, 1.036F, 0.0F),
				posC(1.5F, 0.45F, 1.07F, 0.0F),
				posC(1.625F, 0.416F, 1.036F, 0.0F),
				posC(1.75F, 0.318F, 0.938F, 0.0F),
				posC(1.875F, 0.172F, 0.792F, 0.0F),
				posC(2.0F, 0.0F, 0.62F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.125F, -6.123F, 0.0F, 0.0F),
				rotC(0.25F, -11.314F, 0.0F, 0.0F),
				rotC(0.375F, -14.782F, 0.0F, 0.0F),
				rotC(0.5F, -16.0F, 0.0F, 0.0F),
				rotC(0.625F, -14.782F, 0.0F, 0.0F),
				rotC(0.75F, -11.314F, 0.0F, 0.0F),
				rotC(0.875F, -6.123F, 0.0F, 0.0F),
				rotC(1.0F, 0.0F, 0.0F, 0.0F),
				rotC(1.125F, 6.123F, 0.0F, 0.0F),
				rotC(1.25F, 11.314F, 0.0F, 0.0F),
				rotC(1.375F, 14.782F, 0.0F, 0.0F),
				rotC(1.5F, 16.0F, 0.0F, 0.0F),
				rotC(1.625F, 14.782F, 0.0F, 0.0F),
				rotC(1.75F, 11.314F, 0.0F, 0.0F),
				rotC(1.875F, 6.123F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 22.0F, 0.0F, 0.0F),
				rotC(0.125F, 20.325F, 0.0F, 0.0F),
				rotC(0.25F, 15.556F, 0.0F, 0.0F),
				rotC(0.375F, 8.419F, 0.0F, 0.0F),
				rotC(0.5F, 0.0F, 0.0F, 0.0F),
				rotC(0.625F, 0.0F, 0.0F, 0.0F),
				rotC(0.75F, 0.0F, 0.0F, 0.0F),
				rotC(0.875F, 0.0F, 0.0F, 0.0F),
				rotC(1.0F, 0.0F, 0.0F, 0.0F),
				rotC(1.125F, 0.0F, 0.0F, 0.0F),
				rotC(1.25F, 0.0F, 0.0F, 0.0F),
				rotC(1.375F, 0.0F, 0.0F, 0.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F),
				rotC(1.625F, 8.419F, 0.0F, 0.0F),
				rotC(1.75F, 15.556F, 0.0F, 0.0F),
				rotC(1.875F, 20.325F, 0.0F, 0.0F),
				rotC(2.0F, 22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -23.5F, 0.0F, 0.0F),
				rotC(0.125F, -15.702F, 0.0F, 0.0F),
				rotC(0.25F, -5.743F, 0.0F, 0.0F),
				rotC(0.375F, 4.863F, 0.0F, 0.0F),
				rotC(0.5F, 14.5F, 0.0F, 0.0F),
				rotC(0.625F, 13.282F, 0.0F, 0.0F),
				rotC(0.75F, 9.814F, 0.0F, 0.0F),
				rotC(0.875F, 4.623F, 0.0F, 0.0F),
				rotC(1.0F, -1.5F, 0.0F, 0.0F),
				rotC(1.125F, -7.623F, 0.0F, 0.0F),
				rotC(1.25F, -12.814F, 0.0F, 0.0F),
				rotC(1.375F, -16.282F, 0.0F, 0.0F),
				rotC(1.5F, -17.5F, 0.0F, 0.0F),
				rotC(1.625F, -24.701F, 0.0F, 0.0F),
				rotC(1.75F, -28.37F, 0.0F, 0.0F),
				rotC(1.875F, -27.948F, 0.0F, 0.0F),
				rotC(2.0F, -23.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.125F, -6.123F, 0.0F, 0.0F),
				rotC(0.25F, -11.314F, 0.0F, 0.0F),
				rotC(0.375F, -14.782F, 0.0F, 0.0F),
				rotC(0.5F, -16.0F, 0.0F, 0.0F),
				rotC(0.625F, -14.782F, 0.0F, 0.0F),
				rotC(0.75F, -11.314F, 0.0F, 0.0F),
				rotC(0.875F, -6.123F, 0.0F, 0.0F),
				rotC(1.0F, 0.0F, 0.0F, 0.0F),
				rotC(1.125F, 6.123F, 0.0F, 0.0F),
				rotC(1.25F, 11.314F, 0.0F, 0.0F),
				rotC(1.375F, 14.782F, 0.0F, 0.0F),
				rotC(1.5F, 16.0F, 0.0F, 0.0F),
				rotC(1.625F, 14.782F, 0.0F, 0.0F),
				rotC(1.75F, 11.314F, 0.0F, 0.0F),
				rotC(1.875F, 6.123F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("shank_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 22.0F, 0.0F, 0.0F),
				rotC(0.125F, 20.325F, 0.0F, 0.0F),
				rotC(0.25F, 15.556F, 0.0F, 0.0F),
				rotC(0.375F, 8.419F, 0.0F, 0.0F),
				rotC(0.5F, 0.0F, 0.0F, 0.0F),
				rotC(0.625F, 0.0F, 0.0F, 0.0F),
				rotC(0.75F, 0.0F, 0.0F, 0.0F),
				rotC(0.875F, 0.0F, 0.0F, 0.0F),
				rotC(1.0F, 0.0F, 0.0F, 0.0F),
				rotC(1.125F, 0.0F, 0.0F, 0.0F),
				rotC(1.25F, 0.0F, 0.0F, 0.0F),
				rotC(1.375F, 0.0F, 0.0F, 0.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F),
				rotC(1.625F, 8.419F, 0.0F, 0.0F),
				rotC(1.75F, 15.556F, 0.0F, 0.0F),
				rotC(1.875F, 20.325F, 0.0F, 0.0F),
				rotC(2.0F, 22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("foot_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -23.5F, 0.0F, 0.0F),
				rotC(0.125F, -15.702F, 0.0F, 0.0F),
				rotC(0.25F, -5.743F, 0.0F, 0.0F),
				rotC(0.375F, 4.863F, 0.0F, 0.0F),
				rotC(0.5F, 14.5F, 0.0F, 0.0F),
				rotC(0.625F, 13.282F, 0.0F, 0.0F),
				rotC(0.75F, 9.814F, 0.0F, 0.0F),
				rotC(0.875F, 4.623F, 0.0F, 0.0F),
				rotC(1.0F, -1.5F, 0.0F, 0.0F),
				rotC(1.125F, -7.623F, 0.0F, 0.0F),
				rotC(1.25F, -12.814F, 0.0F, 0.0F),
				rotC(1.375F, -16.282F, 0.0F, 0.0F),
				rotC(1.5F, -17.5F, 0.0F, 0.0F),
				rotC(1.625F, -24.701F, 0.0F, 0.0F),
				rotC(1.75F, -28.37F, 0.0F, 0.0F),
				rotC(1.875F, -27.948F, 0.0F, 0.0F),
				rotC(2.0F, -23.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.125F, 6.123F, 0.0F, 0.0F),
				rotC(0.25F, 11.314F, 0.0F, 0.0F),
				rotC(0.375F, 14.782F, 0.0F, 0.0F),
				rotC(0.5F, 16.0F, 0.0F, 0.0F),
				rotC(0.625F, 14.782F, 0.0F, 0.0F),
				rotC(0.75F, 11.314F, 0.0F, 0.0F),
				rotC(0.875F, 6.123F, 0.0F, 0.0F),
				rotC(1.0F, 0.0F, 0.0F, 0.0F),
				rotC(1.125F, -6.123F, 0.0F, 0.0F),
				rotC(1.25F, -11.314F, 0.0F, 0.0F),
				rotC(1.375F, -14.782F, 0.0F, 0.0F),
				rotC(1.5F, -16.0F, 0.0F, 0.0F),
				rotC(1.625F, -14.782F, 0.0F, 0.0F),
				rotC(1.75F, -11.314F, 0.0F, 0.0F),
				rotC(1.875F, -6.123F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.125F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, 0.0F, 0.0F, 0.0F),
				rotC(0.375F, 0.0F, 0.0F, 0.0F),
				rotC(0.5F, 0.0F, 0.0F, 0.0F),
				rotC(0.625F, 8.419F, 0.0F, 0.0F),
				rotC(0.75F, 15.556F, 0.0F, 0.0F),
				rotC(0.875F, 20.325F, 0.0F, 0.0F),
				rotC(1.0F, 22.0F, 0.0F, 0.0F),
				rotC(1.125F, 20.325F, 0.0F, 0.0F),
				rotC(1.25F, 15.556F, 0.0F, 0.0F),
				rotC(1.375F, 8.419F, 0.0F, 0.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F),
				rotC(1.625F, 0.0F, 0.0F, 0.0F),
				rotC(1.75F, 0.0F, 0.0F, 0.0F),
				rotC(1.875F, 0.0F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -1.5F, 0.0F, 0.0F),
				rotC(0.125F, -7.623F, 0.0F, 0.0F),
				rotC(0.25F, -12.814F, 0.0F, 0.0F),
				rotC(0.375F, -16.282F, 0.0F, 0.0F),
				rotC(0.5F, -17.5F, 0.0F, 0.0F),
				rotC(0.625F, -24.701F, 0.0F, 0.0F),
				rotC(0.75F, -28.37F, 0.0F, 0.0F),
				rotC(0.875F, -27.948F, 0.0F, 0.0F),
				rotC(1.0F, -23.5F, 0.0F, 0.0F),
				rotC(1.125F, -15.702F, 0.0F, 0.0F),
				rotC(1.25F, -5.743F, 0.0F, 0.0F),
				rotC(1.375F, 4.863F, 0.0F, 0.0F),
				rotC(1.5F, 14.5F, 0.0F, 0.0F),
				rotC(1.625F, 13.282F, 0.0F, 0.0F),
				rotC(1.75F, 9.814F, 0.0F, 0.0F),
				rotC(1.875F, 4.623F, 0.0F, 0.0F),
				rotC(2.0F, -1.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.125F, 6.123F, 0.0F, 0.0F),
				rotC(0.25F, 11.314F, 0.0F, 0.0F),
				rotC(0.375F, 14.782F, 0.0F, 0.0F),
				rotC(0.5F, 16.0F, 0.0F, 0.0F),
				rotC(0.625F, 14.782F, 0.0F, 0.0F),
				rotC(0.75F, 11.314F, 0.0F, 0.0F),
				rotC(0.875F, 6.123F, 0.0F, 0.0F),
				rotC(1.0F, 0.0F, 0.0F, 0.0F),
				rotC(1.125F, -6.123F, 0.0F, 0.0F),
				rotC(1.25F, -11.314F, 0.0F, 0.0F),
				rotC(1.375F, -14.782F, 0.0F, 0.0F),
				rotC(1.5F, -16.0F, 0.0F, 0.0F),
				rotC(1.625F, -14.782F, 0.0F, 0.0F),
				rotC(1.75F, -11.314F, 0.0F, 0.0F),
				rotC(1.875F, -6.123F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("shank_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.125F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, 0.0F, 0.0F, 0.0F),
				rotC(0.375F, 0.0F, 0.0F, 0.0F),
				rotC(0.5F, 0.0F, 0.0F, 0.0F),
				rotC(0.625F, 8.419F, 0.0F, 0.0F),
				rotC(0.75F, 15.556F, 0.0F, 0.0F),
				rotC(0.875F, 20.325F, 0.0F, 0.0F),
				rotC(1.0F, 22.0F, 0.0F, 0.0F),
				rotC(1.125F, 20.325F, 0.0F, 0.0F),
				rotC(1.25F, 15.556F, 0.0F, 0.0F),
				rotC(1.375F, 8.419F, 0.0F, 0.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F),
				rotC(1.625F, 0.0F, 0.0F, 0.0F),
				rotC(1.75F, 0.0F, 0.0F, 0.0F),
				rotC(1.875F, 0.0F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("foot_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -1.5F, 0.0F, 0.0F),
				rotC(0.125F, -7.623F, 0.0F, 0.0F),
				rotC(0.25F, -12.814F, 0.0F, 0.0F),
				rotC(0.375F, -16.282F, 0.0F, 0.0F),
				rotC(0.5F, -17.5F, 0.0F, 0.0F),
				rotC(0.625F, -24.701F, 0.0F, 0.0F),
				rotC(0.75F, -28.37F, 0.0F, 0.0F),
				rotC(0.875F, -27.948F, 0.0F, 0.0F),
				rotC(1.0F, -23.5F, 0.0F, 0.0F),
				rotC(1.125F, -15.702F, 0.0F, 0.0F),
				rotC(1.25F, -5.743F, 0.0F, 0.0F),
				rotC(1.375F, 4.863F, 0.0F, 0.0F),
				rotC(1.5F, 14.5F, 0.0F, 0.0F),
				rotC(1.625F, 13.282F, 0.0F, 0.0F),
				rotC(1.75F, 9.814F, 0.0F, 0.0F),
				rotC(1.875F, 4.623F, 0.0F, 0.0F),
				rotC(2.0F, -1.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 1.95F, 0.0F, 0.0F),
				rotC(0.125F, -0.525F, -1.339F, 0.0F),
				rotC(0.25F, -1.55F, -2.475F, 0.0F),
				rotC(0.375F, -0.525F, -3.234F, 0.0F),
				rotC(0.5F, 1.95F, -3.5F, 0.0F),
				rotC(0.625F, 4.425F, -3.234F, 0.0F),
				rotC(0.75F, 5.45F, -2.475F, 0.0F),
				rotC(0.875F, 4.425F, -1.339F, 0.0F),
				rotC(1.0F, 1.95F, 0.0F, 0.0F),
				rotC(1.125F, -0.525F, 1.339F, 0.0F),
				rotC(1.25F, -1.55F, 2.475F, 0.0F),
				rotC(1.375F, -0.525F, 3.234F, 0.0F),
				rotC(1.5F, 1.95F, 3.5F, 0.0F),
				rotC(1.625F, 4.425F, 3.234F, 0.0F),
				rotC(1.75F, 5.45F, 2.475F, 0.0F),
				rotC(1.875F, 4.425F, 1.339F, 0.0F),
				rotC(2.0F, 1.95F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -1.976F, -4.0F, 0.0F),
				rotC(0.125F, 0.645F, -3.696F, 0.0F),
				rotC(0.25F, 2.889F, -2.828F, 0.0F),
				rotC(0.375F, 3.44F, -1.531F, 0.0F),
				rotC(0.5F, 1.976F, 0.0F, 0.0F),
				rotC(0.625F, -0.645F, 1.531F, 0.0F),
				rotC(0.75F, -2.889F, 2.828F, 0.0F),
				rotC(0.875F, -3.44F, 3.696F, 0.0F),
				rotC(1.0F, -1.976F, 4.0F, 0.0F),
				rotC(1.125F, 0.645F, 3.696F, 0.0F),
				rotC(1.25F, 2.889F, 2.828F, 0.0F),
				rotC(1.375F, 3.44F, 1.531F, 0.0F),
				rotC(1.5F, 1.976F, 0.0F, 0.0F),
				rotC(1.625F, -0.645F, -1.531F, 0.0F),
				rotC(1.75F, -2.889F, -2.828F, 0.0F),
				rotC(1.875F, -3.44F, -3.696F, 0.0F),
				rotC(2.0F, -1.976F, -4.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("head_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -1.8F, 0.0F, 0.0F),
				rotC(0.125F, -0.386F, 1.339F, 0.0F),
				rotC(0.25F, 0.2F, 2.475F, 0.0F),
				rotC(0.375F, -0.386F, 3.234F, 0.0F),
				rotC(0.5F, -1.8F, 3.5F, 0.0F),
				rotC(0.625F, -3.214F, 3.234F, 0.0F),
				rotC(0.75F, -3.8F, 2.475F, 0.0F),
				rotC(0.875F, -3.214F, 1.339F, 0.0F),
				rotC(1.0F, -1.8F, 0.0F, 0.0F),
				rotC(1.125F, -0.386F, -1.339F, 0.0F),
				rotC(1.25F, 0.2F, -2.475F, 0.0F),
				rotC(1.375F, -0.386F, -3.234F, 0.0F),
				rotC(1.5F, -1.8F, -3.5F, 0.0F),
				rotC(1.625F, -3.214F, -3.234F, 0.0F),
				rotC(1.75F, -3.8F, -2.475F, 0.0F),
				rotC(1.875F, -3.214F, -1.339F, 0.0F),
				rotC(2.0F, -1.8F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.125F, 1.531F, 0.0F, 0.0F),
				rotC(0.25F, 2.828F, 0.0F, 0.0F),
				rotC(0.375F, 3.696F, 0.0F, 0.0F),
				rotC(0.5F, 4.0F, 0.0F, 0.0F),
				rotC(0.625F, 3.696F, 0.0F, 0.0F),
				rotC(0.75F, 2.828F, 0.0F, 0.0F),
				rotC(0.875F, 1.531F, 0.0F, 0.0F),
				rotC(1.0F, 0.0F, 0.0F, 0.0F),
				rotC(1.125F, 0.0F, 0.0F, 0.0F),
				rotC(1.25F, 0.0F, 0.0F, 0.0F),
				rotC(1.375F, 0.0F, 0.0F, 0.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F),
				rotC(1.625F, 0.0F, 0.0F, 0.0F),
				rotC(1.75F, 0.0F, 0.0F, 0.0F),
				rotC(1.875F, 0.0F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -1.081F, -3.031F, 0.0F),
				rotC(0.125F, 1.044F, -2.131F, 0.0F),
				rotC(0.25F, 3.7F, -0.906F, 0.0F),
				rotC(0.375F, 5.331F, 0.457F, 0.0F),
				rotC(0.5F, 4.981F, 1.75F, 0.0F),
				rotC(0.625F, 2.856F, 2.777F, 0.0F),
				rotC(0.75F, 0.2F, 3.381F, 0.0F),
				rotC(0.875F, -1.431F, 3.47F, 0.0F),
				rotC(1.0F, -1.081F, 3.031F, 0.0F),
				rotC(1.125F, 1.044F, 2.131F, 0.0F),
				rotC(1.25F, 3.7F, 0.906F, 0.0F),
				rotC(1.375F, 5.331F, -0.457F, 0.0F),
				rotC(1.5F, 4.981F, -1.75F, 0.0F),
				rotC(1.625F, 2.856F, -2.777F, 0.0F),
				rotC(1.75F, 0.2F, -3.381F, 0.0F),
				rotC(1.875F, -1.431F, -3.47F, 0.0F),
				rotC(2.0F, -1.081F, -3.031F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 3.49F, 2.0F, 0.0F),
				rotC(0.125F, 2.657F, 3.173F, 0.0F),
				rotC(0.25F, 0.267F, 3.864F, 0.0F),
				rotC(0.375F, -2.279F, 3.966F, 0.0F),
				rotC(0.5F, -3.49F, 3.464F, 0.0F),
				rotC(0.625F, -2.657F, 2.435F, 0.0F),
				rotC(0.75F, -0.267F, 1.035F, 0.0F),
				rotC(0.875F, 2.279F, -0.522F, 0.0F),
				rotC(1.0F, 3.49F, -2.0F, 0.0F),
				rotC(1.125F, 2.657F, -3.173F, 0.0F),
				rotC(1.25F, 0.267F, -3.864F, 0.0F),
				rotC(1.375F, -2.279F, -3.966F, 0.0F),
				rotC(1.5F, -3.49F, -3.464F, 0.0F),
				rotC(1.625F, -2.657F, -2.435F, 0.0F),
				rotC(1.75F, -0.267F, -1.035F, 0.0F),
				rotC(1.875F, 2.279F, 0.522F, 0.0F),
				rotC(2.0F, 3.49F, 2.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("head_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -0.068F, 3.031F, 0.0F),
				rotC(0.125F, -1.282F, 2.131F, 0.0F),
				rotC(0.25F, -2.8F, 0.906F, 0.0F),
				rotC(0.375F, -3.732F, -0.457F, 0.0F),
				rotC(0.5F, -3.532F, -1.75F, 0.0F),
				rotC(0.625F, -2.318F, -2.777F, 0.0F),
				rotC(0.75F, -0.8F, -3.381F, 0.0F),
				rotC(0.875F, 0.132F, -3.47F, 0.0F),
				rotC(1.0F, -0.068F, -3.031F, 0.0F),
				rotC(1.125F, -1.282F, -2.131F, 0.0F),
				rotC(1.25F, -2.8F, -0.906F, 0.0F),
				rotC(1.375F, -3.732F, 0.457F, 0.0F),
				rotC(1.5F, -3.532F, 1.75F, 0.0F),
				rotC(1.625F, -2.318F, 2.777F, 0.0F),
				rotC(1.75F, -0.8F, 3.381F, 0.0F),
				rotC(1.875F, 0.132F, 3.47F, 0.0F),
				rotC(2.0F, -0.068F, 3.031F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 3.464F, 0.0F, 0.0F),
				rotC(0.125F, 2.435F, 0.0F, 0.0F),
				rotC(0.25F, 1.035F, 0.0F, 0.0F),
				rotC(0.375F, 0.0F, 0.0F, 0.0F),
				rotC(0.5F, 0.0F, 0.0F, 0.0F),
				rotC(0.625F, 0.0F, 0.0F, 0.0F),
				rotC(0.75F, 0.0F, 0.0F, 0.0F),
				rotC(0.875F, 0.0F, 0.0F, 0.0F),
				rotC(1.0F, 0.0F, 0.0F, 0.0F),
				rotC(1.125F, 0.0F, 0.0F, 0.0F),
				rotC(1.25F, 0.0F, 0.0F, 0.0F),
				rotC(1.375F, 0.522F, 0.0F, 0.0F),
				rotC(1.5F, 2.0F, 0.0F, 0.0F),
				rotC(1.625F, 3.173F, 0.0F, 0.0F),
				rotC(1.75F, 3.864F, 0.0F, 0.0F),
				rotC(1.875F, 3.966F, 0.0F, 0.0F),
				rotC(2.0F, 3.464F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 4.981F, 3.031F, 0.0F),
				rotC(0.125F, 5.331F, 3.47F, 0.0F),
				rotC(0.25F, 3.7F, 3.381F, 0.0F),
				rotC(0.375F, 1.044F, 2.777F, 0.0F),
				rotC(0.5F, -1.081F, 1.75F, 0.0F),
				rotC(0.625F, -1.431F, 0.457F, 0.0F),
				rotC(0.75F, 0.2F, -0.906F, 0.0F),
				rotC(0.875F, 2.856F, -2.131F, 0.0F),
				rotC(1.0F, 4.981F, -3.031F, 0.0F),
				rotC(1.125F, 5.331F, -3.47F, 0.0F),
				rotC(1.25F, 3.7F, -3.381F, 0.0F),
				rotC(1.375F, 1.044F, -2.777F, 0.0F),
				rotC(1.5F, -1.081F, -1.75F, 0.0F),
				rotC(1.625F, -1.431F, -0.457F, 0.0F),
				rotC(1.75F, 0.2F, 0.906F, 0.0F),
				rotC(1.875F, 2.856F, 2.131F, 0.0F),
				rotC(2.0F, 4.981F, 3.031F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -1.514F, 2.0F, 0.0F),
				rotC(0.125F, -3.302F, 0.522F, 0.0F),
				rotC(0.25F, -3.156F, -1.035F, 0.0F),
				rotC(0.375F, -1.161F, -2.435F, 0.0F),
				rotC(0.5F, 1.514F, -3.464F, 0.0F),
				rotC(0.625F, 3.302F, -3.966F, 0.0F),
				rotC(0.75F, 3.156F, -3.864F, 0.0F),
				rotC(0.875F, 1.161F, -3.173F, 0.0F),
				rotC(1.0F, -1.514F, -2.0F, 0.0F),
				rotC(1.125F, -3.302F, -0.522F, 0.0F),
				rotC(1.25F, -3.156F, 1.035F, 0.0F),
				rotC(1.375F, -1.161F, 2.435F, 0.0F),
				rotC(1.5F, 1.514F, 3.464F, 0.0F),
				rotC(1.625F, 3.302F, 3.966F, 0.0F),
				rotC(1.75F, 3.156F, 3.864F, 0.0F),
				rotC(1.875F, 1.161F, 3.173F, 0.0F),
				rotC(2.0F, -1.514F, 2.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("head_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -3.532F, -3.031F, 0.0F),
				rotC(0.125F, -3.732F, -3.47F, 0.0F),
				rotC(0.25F, -2.8F, -3.381F, 0.0F),
				rotC(0.375F, -1.282F, -2.777F, 0.0F),
				rotC(0.5F, -0.068F, -1.75F, 0.0F),
				rotC(0.625F, 0.132F, -0.457F, 0.0F),
				rotC(0.75F, -0.8F, 0.906F, 0.0F),
				rotC(0.875F, -2.318F, 2.131F, 0.0F),
				rotC(1.0F, -3.532F, 3.031F, 0.0F),
				rotC(1.125F, -3.732F, 3.47F, 0.0F),
				rotC(1.25F, -2.8F, 3.381F, 0.0F),
				rotC(1.375F, -1.282F, 2.777F, 0.0F),
				rotC(1.5F, -0.068F, 1.75F, 0.0F),
				rotC(1.625F, 0.132F, 0.457F, 0.0F),
				rotC(1.75F, -0.8F, -0.906F, 0.0F),
				rotC(1.875F, -2.318F, -2.131F, 0.0F),
				rotC(2.0F, -3.532F, -3.031F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.125F, 0.0F, 0.0F, 0.0F),
				rotC(0.25F, 0.0F, 0.0F, 0.0F),
				rotC(0.375F, 0.0F, 0.0F, 0.0F),
				rotC(0.5F, 0.0F, 0.0F, 0.0F),
				rotC(0.625F, 0.0F, 0.0F, 0.0F),
				rotC(0.75F, 1.035F, 0.0F, 0.0F),
				rotC(0.875F, 2.435F, 0.0F, 0.0F),
				rotC(1.0F, 3.464F, 0.0F, 0.0F),
				rotC(1.125F, 3.966F, 0.0F, 0.0F),
				rotC(1.25F, 3.864F, 0.0F, 0.0F),
				rotC(1.375F, 3.173F, 0.0F, 0.0F),
				rotC(1.5F, 2.0F, 0.0F, 0.0F),
				rotC(1.625F, 0.522F, 0.0F, 0.0F),
				rotC(1.75F, 0.0F, 0.0F, 0.0F),
				rotC(1.875F, 0.0F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 2.205F, 3.279F, 0.0F),
				rotC(0.125F, 2.574F, 0.838F, 0.0F),
				rotC(0.25F, 2.49F, -1.731F, 0.0F),
				rotC(0.375F, 2.001F, -4.037F, 0.0F),
				rotC(0.5F, 1.395F, -5.728F, 0.0F),
				rotC(0.625F, 1.026F, -6.547F, 0.0F),
				rotC(0.75F, 1.11F, -6.369F, 0.0F),
				rotC(0.875F, 1.599F, -5.222F, 0.0F),
				rotC(1.0F, 2.205F, -3.279F, 0.0F),
				rotC(1.125F, 2.574F, -0.838F, 0.0F),
				rotC(1.25F, 2.49F, 1.731F, 0.0F),
				rotC(1.375F, 2.001F, 4.037F, 0.0F),
				rotC(1.5F, 1.395F, 5.728F, 0.0F),
				rotC(1.625F, 1.026F, 6.547F, 0.0F),
				rotC(1.75F, 1.11F, 6.369F, 0.0F),
				rotC(1.875F, 1.599F, 5.222F, 0.0F),
				rotC(2.0F, 2.205F, 3.279F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 1.41F, 7.072F, 0.0F),
				rotC(0.125F, 2.018F, 4.945F, 0.0F),
				rotC(0.25F, 2.499F, 2.065F, 0.0F),
				rotC(0.375F, 2.57F, -1.129F, 0.0F),
				rotC(0.5F, 2.19F, -4.151F, 0.0F),
				rotC(0.625F, 1.582F, -6.541F, 0.0F),
				rotC(0.75F, 1.101F, -7.936F, 0.0F),
				rotC(0.875F, 1.03F, -8.122F, 0.0F),
				rotC(1.0F, 1.41F, -7.072F, 0.0F),
				rotC(1.125F, 2.018F, -4.945F, 0.0F),
				rotC(1.25F, 2.499F, -2.065F, 0.0F),
				rotC(1.375F, 2.57F, 1.129F, 0.0F),
				rotC(1.5F, 2.19F, 4.151F, 0.0F),
				rotC(1.625F, 1.582F, 6.541F, 0.0F),
				rotC(1.75F, 1.101F, 7.936F, 0.0F),
				rotC(1.875F, 1.03F, 8.122F, 0.0F),
				rotC(2.0F, 1.41F, 7.072F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 1.0F, 9.799F, 0.0F),
				rotC(0.125F, 1.247F, 9.013F, 0.0F),
				rotC(0.25F, 1.817F, 6.854F, 0.0F),
				rotC(0.375F, 2.378F, 3.652F, 0.0F),
				rotC(0.5F, 2.6F, -0.106F, 0.0F),
				rotC(0.625F, 2.353F, -3.848F, 0.0F),
				rotC(0.75F, 1.783F, -7.004F, 0.0F),
				rotC(0.875F, 1.222F, -9.094F, 0.0F),
				rotC(1.0F, 1.0F, -9.799F, 0.0F),
				rotC(1.125F, 1.247F, -9.013F, 0.0F),
				rotC(1.25F, 1.817F, -6.854F, 0.0F),
				rotC(1.375F, 2.378F, -3.652F, 0.0F),
				rotC(1.5F, 2.6F, 0.106F, 0.0F),
				rotC(1.625F, 2.353F, 3.848F, 0.0F),
				rotC(1.75F, 1.783F, 7.004F, 0.0F),
				rotC(1.875F, 1.222F, 9.094F, 0.0F),
				rotC(2.0F, 1.0F, 9.799F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 1.38F, 9.954F, 0.0F),
				rotC(0.125F, 1.022F, 11.323F, 0.0F),
				rotC(0.25F, 1.119F, 10.968F, 0.0F),
				rotC(0.375F, 1.615F, 8.943F, 0.0F),
				rotC(0.5F, 2.22F, 5.557F, 0.0F),
				rotC(0.625F, 2.578F, 1.325F, 0.0F),
				rotC(0.75F, 2.481F, -3.109F, 0.0F),
				rotC(0.875F, 1.985F, -7.069F, 0.0F),
				rotC(1.0F, 1.38F, -9.954F, 0.0F),
				rotC(1.125F, 1.022F, -11.323F, 0.0F),
				rotC(1.25F, 1.119F, -10.968F, 0.0F),
				rotC(1.375F, 1.615F, -8.943F, 0.0F),
				rotC(1.5F, 2.22F, -5.557F, 0.0F),
				rotC(1.625F, 2.578F, -1.325F, 0.0F),
				rotC(1.75F, 2.481F, 3.109F, 0.0F),
				rotC(1.875F, 1.985F, 7.069F, 0.0F),
				rotC(2.0F, 1.38F, 9.954F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 2.175F, 6.702F, 0.0F),
				rotC(0.125F, 1.565F, 10.454F, 0.0F),
				rotC(0.25F, 1.093F, 12.616F, 0.0F),
				rotC(0.375F, 1.035F, 12.856F, 0.0F),
				rotC(0.5F, 1.425F, 11.14F, 0.0F),
				rotC(0.625F, 2.035F, 7.727F, 0.0F),
				rotC(0.75F, 2.507F, 3.138F, 0.0F),
				rotC(0.875F, 2.565F, -1.928F, 0.0F),
				rotC(1.0F, 2.175F, -6.702F, 0.0F),
				rotC(1.125F, 1.565F, -10.454F, 0.0F),
				rotC(1.25F, 1.093F, -12.616F, 0.0F),
				rotC(1.375F, 1.035F, -12.856F, 0.0F),
				rotC(1.5F, 1.425F, -11.14F, 0.0F),
				rotC(1.625F, 2.035F, -7.727F, 0.0F),
				rotC(1.75F, 2.507F, -3.138F, 0.0F),
				rotC(1.875F, 2.565F, 1.928F, 0.0F),
				rotC(2.0F, 2.175F, 6.702F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 2.599F, 0.315F, 0.0F),
				rotC(0.125F, 2.341F, 5.877F, 0.0F),
				rotC(0.25F, 1.765F, 10.544F, 0.0F),
				rotC(0.375F, 1.21F, 13.606F, 0.0F),
				rotC(0.5F, 1.001F, 14.597F, 0.0F),
				rotC(0.625F, 1.259F, 13.365F, 0.0F),
				rotC(0.75F, 1.835F, 10.098F, 0.0F),
				rotC(0.875F, 2.39F, 5.295F, 0.0F),
				rotC(1.0F, 2.599F, -0.315F, 0.0F),
				rotC(1.125F, 2.341F, -5.877F, 0.0F),
				rotC(1.25F, 1.765F, -10.544F, 0.0F),
				rotC(1.375F, 1.21F, -13.606F, 0.0F),
				rotC(1.5F, 1.001F, -14.597F, 0.0F),
				rotC(1.625F, 1.259F, -13.365F, 0.0F),
				rotC(1.75F, 1.835F, -10.098F, 0.0F),
				rotC(1.875F, 2.39F, -5.295F, 0.0F),
				rotC(2.0F, 2.599F, 0.315F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(2.0F).looping();
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
			return b.build();
		}
	}

	private static final class RunAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 3.1F, 0.0F, 0.0F),
				rotC(0.075F, 3.51F, -0.957F, -0.842F),
				rotC(0.15F, 4.5F, -1.768F, -1.556F),
				rotC(0.225F, 5.49F, -2.31F, -2.033F),
				rotC(0.3F, 5.9F, -2.5F, -2.2F),
				rotC(0.375F, 5.49F, -2.31F, -2.033F),
				rotC(0.45F, 4.5F, -1.768F, -1.556F),
				rotC(0.525F, 3.51F, -0.957F, -0.842F),
				rotC(0.6F, 3.1F, 0.0F, 0.0F),
				rotC(0.675F, 3.51F, 0.957F, 0.842F),
				rotC(0.75F, 4.5F, 1.768F, 1.556F),
				rotC(0.825F, 5.49F, 2.31F, 2.033F),
				rotC(0.9F, 5.9F, 2.5F, 2.2F),
				rotC(0.975F, 5.49F, 2.31F, 2.033F),
				rotC(1.05F, 4.5F, 1.768F, 1.556F),
				rotC(1.125F, 3.51F, 0.957F, 0.842F),
				rotC(1.2F, 3.1F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 1.25F, 0.0F),
				posC(0.075F, -0.172F, 1.422F, 0.0F),
				posC(0.15F, -0.318F, 1.568F, 0.0F),
				posC(0.225F, -0.416F, 1.666F, 0.0F),
				posC(0.3F, -0.45F, 1.7F, 0.0F),
				posC(0.375F, -0.416F, 1.666F, 0.0F),
				posC(0.45F, -0.318F, 1.568F, 0.0F),
				posC(0.525F, -0.172F, 1.422F, 0.0F),
				posC(0.6F, 0.0F, 1.25F, 0.0F),
				posC(0.675F, 0.172F, 1.422F, 0.0F),
				posC(0.75F, 0.318F, 1.568F, 0.0F),
				posC(0.825F, 0.416F, 1.666F, 0.0F),
				posC(0.9F, 0.45F, 1.7F, 0.0F),
				posC(0.975F, 0.416F, 1.666F, 0.0F),
				posC(1.05F, 0.318F, 1.568F, 0.0F),
				posC(1.125F, 0.172F, 1.422F, 0.0F),
				posC(1.2F, 0.0F, 1.25F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.075F, -8.802F, 0.0F, 0.0F),
				rotC(0.15F, -16.263F, 0.0F, 0.0F),
				rotC(0.225F, -21.249F, 0.0F, 0.0F),
				rotC(0.3F, -23.0F, 0.0F, 0.0F),
				rotC(0.375F, -21.249F, 0.0F, 0.0F),
				rotC(0.45F, -16.263F, 0.0F, 0.0F),
				rotC(0.525F, -8.802F, 0.0F, 0.0F),
				rotC(0.6F, 0.0F, 0.0F, 0.0F),
				rotC(0.675F, 8.802F, 0.0F, 0.0F),
				rotC(0.75F, 16.263F, 0.0F, 0.0F),
				rotC(0.825F, 21.249F, 0.0F, 0.0F),
				rotC(0.9F, 23.0F, 0.0F, 0.0F),
				rotC(0.975F, 21.249F, 0.0F, 0.0F),
				rotC(1.05F, 16.263F, 0.0F, 0.0F),
				rotC(1.125F, 8.802F, 0.0F, 0.0F),
				rotC(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 28.0F, 0.0F, 0.0F),
				rotC(0.075F, 25.869F, 0.0F, 0.0F),
				rotC(0.15F, 19.799F, 0.0F, 0.0F),
				rotC(0.225F, 10.715F, 0.0F, 0.0F),
				rotC(0.3F, 0.0F, 0.0F, 0.0F),
				rotC(0.375F, 0.0F, 0.0F, 0.0F),
				rotC(0.45F, 0.0F, 0.0F, 0.0F),
				rotC(0.525F, 0.0F, 0.0F, 0.0F),
				rotC(0.6F, 0.0F, 0.0F, 0.0F),
				rotC(0.675F, 0.0F, 0.0F, 0.0F),
				rotC(0.75F, 0.0F, 0.0F, 0.0F),
				rotC(0.825F, 0.0F, 0.0F, 0.0F),
				rotC(0.9F, 0.0F, 0.0F, 0.0F),
				rotC(0.975F, 10.715F, 0.0F, 0.0F),
				rotC(1.05F, 19.799F, 0.0F, 0.0F),
				rotC(1.125F, 25.869F, 0.0F, 0.0F),
				rotC(1.2F, 28.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -32.5F, 0.0F, 0.0F),
				rotC(0.075F, -21.567F, 0.0F, 0.0F),
				rotC(0.15F, -8.036F, 0.0F, 0.0F),
				rotC(0.225F, 6.034F, 0.0F, 0.0F),
				rotC(0.3F, 18.5F, 0.0F, 0.0F),
				rotC(0.375F, 16.749F, 0.0F, 0.0F),
				rotC(0.45F, 11.763F, 0.0F, 0.0F),
				rotC(0.525F, 4.302F, 0.0F, 0.0F),
				rotC(0.6F, -4.5F, 0.0F, 0.0F),
				rotC(0.675F, -13.302F, 0.0F, 0.0F),
				rotC(0.75F, -20.763F, 0.0F, 0.0F),
				rotC(0.825F, -25.749F, 0.0F, 0.0F),
				rotC(0.9F, -27.5F, 0.0F, 0.0F),
				rotC(0.975F, -36.464F, 0.0F, 0.0F),
				rotC(1.05F, -40.562F, 0.0F, 0.0F),
				rotC(1.125F, -39.17F, 0.0F, 0.0F),
				rotC(1.2F, -32.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.075F, -8.802F, 0.0F, 0.0F),
				rotC(0.15F, -16.263F, 0.0F, 0.0F),
				rotC(0.225F, -21.249F, 0.0F, 0.0F),
				rotC(0.3F, -23.0F, 0.0F, 0.0F),
				rotC(0.375F, -21.249F, 0.0F, 0.0F),
				rotC(0.45F, -16.263F, 0.0F, 0.0F),
				rotC(0.525F, -8.802F, 0.0F, 0.0F),
				rotC(0.6F, 0.0F, 0.0F, 0.0F),
				rotC(0.675F, 8.802F, 0.0F, 0.0F),
				rotC(0.75F, 16.263F, 0.0F, 0.0F),
				rotC(0.825F, 21.249F, 0.0F, 0.0F),
				rotC(0.9F, 23.0F, 0.0F, 0.0F),
				rotC(0.975F, 21.249F, 0.0F, 0.0F),
				rotC(1.05F, 16.263F, 0.0F, 0.0F),
				rotC(1.125F, 8.802F, 0.0F, 0.0F),
				rotC(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("shank_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 28.0F, 0.0F, 0.0F),
				rotC(0.075F, 25.869F, 0.0F, 0.0F),
				rotC(0.15F, 19.799F, 0.0F, 0.0F),
				rotC(0.225F, 10.715F, 0.0F, 0.0F),
				rotC(0.3F, 0.0F, 0.0F, 0.0F),
				rotC(0.375F, 0.0F, 0.0F, 0.0F),
				rotC(0.45F, 0.0F, 0.0F, 0.0F),
				rotC(0.525F, 0.0F, 0.0F, 0.0F),
				rotC(0.6F, 0.0F, 0.0F, 0.0F),
				rotC(0.675F, 0.0F, 0.0F, 0.0F),
				rotC(0.75F, 0.0F, 0.0F, 0.0F),
				rotC(0.825F, 0.0F, 0.0F, 0.0F),
				rotC(0.9F, 0.0F, 0.0F, 0.0F),
				rotC(0.975F, 10.715F, 0.0F, 0.0F),
				rotC(1.05F, 19.799F, 0.0F, 0.0F),
				rotC(1.125F, 25.869F, 0.0F, 0.0F),
				rotC(1.2F, 28.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("foot_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -32.5F, 0.0F, 0.0F),
				rotC(0.075F, -21.567F, 0.0F, 0.0F),
				rotC(0.15F, -8.036F, 0.0F, 0.0F),
				rotC(0.225F, 6.034F, 0.0F, 0.0F),
				rotC(0.3F, 18.5F, 0.0F, 0.0F),
				rotC(0.375F, 16.749F, 0.0F, 0.0F),
				rotC(0.45F, 11.763F, 0.0F, 0.0F),
				rotC(0.525F, 4.302F, 0.0F, 0.0F),
				rotC(0.6F, -4.5F, 0.0F, 0.0F),
				rotC(0.675F, -13.302F, 0.0F, 0.0F),
				rotC(0.75F, -20.763F, 0.0F, 0.0F),
				rotC(0.825F, -25.749F, 0.0F, 0.0F),
				rotC(0.9F, -27.5F, 0.0F, 0.0F),
				rotC(0.975F, -36.464F, 0.0F, 0.0F),
				rotC(1.05F, -40.562F, 0.0F, 0.0F),
				rotC(1.125F, -39.17F, 0.0F, 0.0F),
				rotC(1.2F, -32.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.075F, 8.802F, 0.0F, 0.0F),
				rotC(0.15F, 16.263F, 0.0F, 0.0F),
				rotC(0.225F, 21.249F, 0.0F, 0.0F),
				rotC(0.3F, 23.0F, 0.0F, 0.0F),
				rotC(0.375F, 21.249F, 0.0F, 0.0F),
				rotC(0.45F, 16.263F, 0.0F, 0.0F),
				rotC(0.525F, 8.802F, 0.0F, 0.0F),
				rotC(0.6F, 0.0F, 0.0F, 0.0F),
				rotC(0.675F, -8.802F, 0.0F, 0.0F),
				rotC(0.75F, -16.263F, 0.0F, 0.0F),
				rotC(0.825F, -21.249F, 0.0F, 0.0F),
				rotC(0.9F, -23.0F, 0.0F, 0.0F),
				rotC(0.975F, -21.249F, 0.0F, 0.0F),
				rotC(1.05F, -16.263F, 0.0F, 0.0F),
				rotC(1.125F, -8.802F, 0.0F, 0.0F),
				rotC(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.075F, 0.0F, 0.0F, 0.0F),
				rotC(0.15F, 0.0F, 0.0F, 0.0F),
				rotC(0.225F, 0.0F, 0.0F, 0.0F),
				rotC(0.3F, 0.0F, 0.0F, 0.0F),
				rotC(0.375F, 10.715F, 0.0F, 0.0F),
				rotC(0.45F, 19.799F, 0.0F, 0.0F),
				rotC(0.525F, 25.869F, 0.0F, 0.0F),
				rotC(0.6F, 28.0F, 0.0F, 0.0F),
				rotC(0.675F, 25.869F, 0.0F, 0.0F),
				rotC(0.75F, 19.799F, 0.0F, 0.0F),
				rotC(0.825F, 10.715F, 0.0F, 0.0F),
				rotC(0.9F, 0.0F, 0.0F, 0.0F),
				rotC(0.975F, 0.0F, 0.0F, 0.0F),
				rotC(1.05F, 0.0F, 0.0F, 0.0F),
				rotC(1.125F, 0.0F, 0.0F, 0.0F),
				rotC(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -4.5F, 0.0F, 0.0F),
				rotC(0.075F, -13.302F, 0.0F, 0.0F),
				rotC(0.15F, -20.763F, 0.0F, 0.0F),
				rotC(0.225F, -25.749F, 0.0F, 0.0F),
				rotC(0.3F, -27.5F, 0.0F, 0.0F),
				rotC(0.375F, -36.464F, 0.0F, 0.0F),
				rotC(0.45F, -40.562F, 0.0F, 0.0F),
				rotC(0.525F, -39.17F, 0.0F, 0.0F),
				rotC(0.6F, -32.5F, 0.0F, 0.0F),
				rotC(0.675F, -21.567F, 0.0F, 0.0F),
				rotC(0.75F, -8.036F, 0.0F, 0.0F),
				rotC(0.825F, 6.034F, 0.0F, 0.0F),
				rotC(0.9F, 18.5F, 0.0F, 0.0F),
				rotC(0.975F, 16.749F, 0.0F, 0.0F),
				rotC(1.05F, 11.763F, 0.0F, 0.0F),
				rotC(1.125F, 4.302F, 0.0F, 0.0F),
				rotC(1.2F, -4.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.075F, 8.802F, 0.0F, 0.0F),
				rotC(0.15F, 16.263F, 0.0F, 0.0F),
				rotC(0.225F, 21.249F, 0.0F, 0.0F),
				rotC(0.3F, 23.0F, 0.0F, 0.0F),
				rotC(0.375F, 21.249F, 0.0F, 0.0F),
				rotC(0.45F, 16.263F, 0.0F, 0.0F),
				rotC(0.525F, 8.802F, 0.0F, 0.0F),
				rotC(0.6F, 0.0F, 0.0F, 0.0F),
				rotC(0.675F, -8.802F, 0.0F, 0.0F),
				rotC(0.75F, -16.263F, 0.0F, 0.0F),
				rotC(0.825F, -21.249F, 0.0F, 0.0F),
				rotC(0.9F, -23.0F, 0.0F, 0.0F),
				rotC(0.975F, -21.249F, 0.0F, 0.0F),
				rotC(1.05F, -16.263F, 0.0F, 0.0F),
				rotC(1.125F, -8.802F, 0.0F, 0.0F),
				rotC(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("shank_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.075F, 0.0F, 0.0F, 0.0F),
				rotC(0.15F, 0.0F, 0.0F, 0.0F),
				rotC(0.225F, 0.0F, 0.0F, 0.0F),
				rotC(0.3F, 0.0F, 0.0F, 0.0F),
				rotC(0.375F, 10.715F, 0.0F, 0.0F),
				rotC(0.45F, 19.799F, 0.0F, 0.0F),
				rotC(0.525F, 25.869F, 0.0F, 0.0F),
				rotC(0.6F, 28.0F, 0.0F, 0.0F),
				rotC(0.675F, 25.869F, 0.0F, 0.0F),
				rotC(0.75F, 19.799F, 0.0F, 0.0F),
				rotC(0.825F, 10.715F, 0.0F, 0.0F),
				rotC(0.9F, 0.0F, 0.0F, 0.0F),
				rotC(0.975F, 0.0F, 0.0F, 0.0F),
				rotC(1.05F, 0.0F, 0.0F, 0.0F),
				rotC(1.125F, 0.0F, 0.0F, 0.0F),
				rotC(1.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("foot_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -4.5F, 0.0F, 0.0F),
				rotC(0.075F, -13.302F, 0.0F, 0.0F),
				rotC(0.15F, -20.763F, 0.0F, 0.0F),
				rotC(0.225F, -25.749F, 0.0F, 0.0F),
				rotC(0.3F, -27.5F, 0.0F, 0.0F),
				rotC(0.375F, -36.464F, 0.0F, 0.0F),
				rotC(0.45F, -40.562F, 0.0F, 0.0F),
				rotC(0.525F, -39.17F, 0.0F, 0.0F),
				rotC(0.6F, -32.5F, 0.0F, 0.0F),
				rotC(0.675F, -21.567F, 0.0F, 0.0F),
				rotC(0.75F, -8.036F, 0.0F, 0.0F),
				rotC(0.825F, 6.034F, 0.0F, 0.0F),
				rotC(0.9F, 18.5F, 0.0F, 0.0F),
				rotC(0.975F, 16.749F, 0.0F, 0.0F),
				rotC(1.05F, 11.763F, 0.0F, 0.0F),
				rotC(1.125F, 4.302F, 0.0F, 0.0F),
				rotC(1.2F, -4.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 5.85F, 0.0F, 0.0F),
				rotC(0.075F, 3.375F, -1.339F, 0.0F),
				rotC(0.15F, 2.35F, -2.475F, 0.0F),
				rotC(0.225F, 3.375F, -3.234F, 0.0F),
				rotC(0.3F, 5.85F, -3.5F, 0.0F),
				rotC(0.375F, 8.325F, -3.234F, 0.0F),
				rotC(0.45F, 9.35F, -2.475F, 0.0F),
				rotC(0.525F, 8.325F, -1.339F, 0.0F),
				rotC(0.6F, 5.85F, 0.0F, 0.0F),
				rotC(0.675F, 3.375F, 1.339F, 0.0F),
				rotC(0.75F, 2.35F, 2.475F, 0.0F),
				rotC(0.825F, 3.375F, 3.234F, 0.0F),
				rotC(0.9F, 5.85F, 3.5F, 0.0F),
				rotC(0.975F, 8.325F, 3.234F, 0.0F),
				rotC(1.05F, 9.35F, 2.475F, 0.0F),
				rotC(1.125F, 8.325F, 1.339F, 0.0F),
				rotC(1.2F, 5.85F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -1.976F, -4.0F, 0.0F),
				rotC(0.075F, 0.645F, -3.696F, 0.0F),
				rotC(0.15F, 2.889F, -2.828F, 0.0F),
				rotC(0.225F, 3.44F, -1.531F, 0.0F),
				rotC(0.3F, 1.976F, 0.0F, 0.0F),
				rotC(0.375F, -0.645F, 1.531F, 0.0F),
				rotC(0.45F, -2.889F, 2.828F, 0.0F),
				rotC(0.525F, -3.44F, 3.696F, 0.0F),
				rotC(0.6F, -1.976F, 4.0F, 0.0F),
				rotC(0.675F, 0.645F, 3.696F, 0.0F),
				rotC(0.75F, 2.889F, 2.828F, 0.0F),
				rotC(0.825F, 3.44F, 1.531F, 0.0F),
				rotC(0.9F, 1.976F, 0.0F, 0.0F),
				rotC(0.975F, -0.645F, -1.531F, 0.0F),
				rotC(1.05F, -2.889F, -2.828F, 0.0F),
				rotC(1.125F, -3.44F, -3.696F, 0.0F),
				rotC(1.2F, -1.976F, -4.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("head_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -5.4F, 0.0F, 0.0F),
				rotC(0.075F, -3.986F, 1.339F, 0.0F),
				rotC(0.15F, -3.4F, 2.475F, 0.0F),
				rotC(0.225F, -3.986F, 3.234F, 0.0F),
				rotC(0.3F, -5.4F, 3.5F, 0.0F),
				rotC(0.375F, -6.814F, 3.234F, 0.0F),
				rotC(0.45F, -7.4F, 2.475F, 0.0F),
				rotC(0.525F, -6.814F, 1.339F, 0.0F),
				rotC(0.6F, -5.4F, 0.0F, 0.0F),
				rotC(0.675F, -3.986F, -1.339F, 0.0F),
				rotC(0.75F, -3.4F, -2.475F, 0.0F),
				rotC(0.825F, -3.986F, -3.234F, 0.0F),
				rotC(0.9F, -5.4F, -3.5F, 0.0F),
				rotC(0.975F, -6.814F, -3.234F, 0.0F),
				rotC(1.05F, -7.4F, -2.475F, 0.0F),
				rotC(1.125F, -6.814F, -1.339F, 0.0F),
				rotC(1.2F, -5.4F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 8.0F, 0.0F, 0.0F),
				rotC(0.075F, 9.531F, 0.0F, 0.0F),
				rotC(0.15F, 10.828F, 0.0F, 0.0F),
				rotC(0.225F, 11.696F, 0.0F, 0.0F),
				rotC(0.3F, 12.0F, 0.0F, 0.0F),
				rotC(0.375F, 11.696F, 0.0F, 0.0F),
				rotC(0.45F, 10.828F, 0.0F, 0.0F),
				rotC(0.525F, 9.531F, 0.0F, 0.0F),
				rotC(0.6F, 8.0F, 0.0F, 0.0F),
				rotC(0.675F, 8.0F, 0.0F, 0.0F),
				rotC(0.75F, 8.0F, 0.0F, 0.0F),
				rotC(0.825F, 8.0F, 0.0F, 0.0F),
				rotC(0.9F, 8.0F, 0.0F, 0.0F),
				rotC(0.975F, 8.0F, 0.0F, 0.0F),
				rotC(1.05F, 8.0F, 0.0F, 0.0F),
				rotC(1.125F, 8.0F, 0.0F, 0.0F),
				rotC(1.2F, 8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 2.819F, -3.031F, 0.0F),
				rotC(0.075F, 4.944F, -2.131F, 0.0F),
				rotC(0.15F, 7.6F, -0.906F, 0.0F),
				rotC(0.225F, 9.231F, 0.457F, 0.0F),
				rotC(0.3F, 8.881F, 1.75F, 0.0F),
				rotC(0.375F, 6.756F, 2.777F, 0.0F),
				rotC(0.45F, 4.1F, 3.381F, 0.0F),
				rotC(0.525F, 2.469F, 3.47F, 0.0F),
				rotC(0.6F, 2.819F, 3.031F, 0.0F),
				rotC(0.675F, 4.944F, 2.131F, 0.0F),
				rotC(0.75F, 7.6F, 0.906F, 0.0F),
				rotC(0.825F, 9.231F, -0.457F, 0.0F),
				rotC(0.9F, 8.881F, -1.75F, 0.0F),
				rotC(0.975F, 6.756F, -2.777F, 0.0F),
				rotC(1.05F, 4.1F, -3.381F, 0.0F),
				rotC(1.125F, 2.469F, -3.47F, 0.0F),
				rotC(1.2F, 2.819F, -3.031F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 3.49F, 2.0F, 0.0F),
				rotC(0.075F, 2.657F, 3.173F, 0.0F),
				rotC(0.15F, 0.267F, 3.864F, 0.0F),
				rotC(0.225F, -2.279F, 3.966F, 0.0F),
				rotC(0.3F, -3.49F, 3.464F, 0.0F),
				rotC(0.375F, -2.657F, 2.435F, 0.0F),
				rotC(0.45F, -0.267F, 1.035F, 0.0F),
				rotC(0.525F, 2.279F, -0.522F, 0.0F),
				rotC(0.6F, 3.49F, -2.0F, 0.0F),
				rotC(0.675F, 2.657F, -3.173F, 0.0F),
				rotC(0.75F, 0.267F, -3.864F, 0.0F),
				rotC(0.825F, -2.279F, -3.966F, 0.0F),
				rotC(0.9F, -3.49F, -3.464F, 0.0F),
				rotC(0.975F, -2.657F, -2.435F, 0.0F),
				rotC(1.05F, -0.267F, -1.035F, 0.0F),
				rotC(1.125F, 2.279F, 0.522F, 0.0F),
				rotC(1.2F, 3.49F, 2.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("head_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -3.668F, 3.031F, 0.0F),
				rotC(0.075F, -4.882F, 2.131F, 0.0F),
				rotC(0.15F, -6.4F, 0.906F, 0.0F),
				rotC(0.225F, -7.332F, -0.457F, 0.0F),
				rotC(0.3F, -7.132F, -1.75F, 0.0F),
				rotC(0.375F, -5.918F, -2.777F, 0.0F),
				rotC(0.45F, -4.4F, -3.381F, 0.0F),
				rotC(0.525F, -3.468F, -3.47F, 0.0F),
				rotC(0.6F, -3.668F, -3.031F, 0.0F),
				rotC(0.675F, -4.882F, -2.131F, 0.0F),
				rotC(0.75F, -6.4F, -0.906F, 0.0F),
				rotC(0.825F, -7.332F, 0.457F, 0.0F),
				rotC(0.9F, -7.132F, 1.75F, 0.0F),
				rotC(0.975F, -5.918F, 2.777F, 0.0F),
				rotC(1.05F, -4.4F, 3.381F, 0.0F),
				rotC(1.125F, -3.468F, 3.47F, 0.0F),
				rotC(1.2F, -3.668F, 3.031F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 11.464F, 0.0F, 0.0F),
				rotC(0.075F, 10.435F, 0.0F, 0.0F),
				rotC(0.15F, 9.035F, 0.0F, 0.0F),
				rotC(0.225F, 8.0F, 0.0F, 0.0F),
				rotC(0.3F, 8.0F, 0.0F, 0.0F),
				rotC(0.375F, 8.0F, 0.0F, 0.0F),
				rotC(0.45F, 8.0F, 0.0F, 0.0F),
				rotC(0.525F, 8.0F, 0.0F, 0.0F),
				rotC(0.6F, 8.0F, 0.0F, 0.0F),
				rotC(0.675F, 8.0F, 0.0F, 0.0F),
				rotC(0.75F, 8.0F, 0.0F, 0.0F),
				rotC(0.825F, 8.522F, 0.0F, 0.0F),
				rotC(0.9F, 10.0F, 0.0F, 0.0F),
				rotC(0.975F, 11.173F, 0.0F, 0.0F),
				rotC(1.05F, 11.864F, 0.0F, 0.0F),
				rotC(1.125F, 11.966F, 0.0F, 0.0F),
				rotC(1.2F, 11.464F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 8.881F, 3.031F, 0.0F),
				rotC(0.075F, 9.231F, 3.47F, 0.0F),
				rotC(0.15F, 7.6F, 3.381F, 0.0F),
				rotC(0.225F, 4.944F, 2.777F, 0.0F),
				rotC(0.3F, 2.819F, 1.75F, 0.0F),
				rotC(0.375F, 2.469F, 0.457F, 0.0F),
				rotC(0.45F, 4.1F, -0.906F, 0.0F),
				rotC(0.525F, 6.756F, -2.131F, 0.0F),
				rotC(0.6F, 8.881F, -3.031F, 0.0F),
				rotC(0.675F, 9.231F, -3.47F, 0.0F),
				rotC(0.75F, 7.6F, -3.381F, 0.0F),
				rotC(0.825F, 4.944F, -2.777F, 0.0F),
				rotC(0.9F, 2.819F, -1.75F, 0.0F),
				rotC(0.975F, 2.469F, -0.457F, 0.0F),
				rotC(1.05F, 4.1F, 0.906F, 0.0F),
				rotC(1.125F, 6.756F, 2.131F, 0.0F),
				rotC(1.2F, 8.881F, 3.031F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -1.514F, 2.0F, 0.0F),
				rotC(0.075F, -3.302F, 0.522F, 0.0F),
				rotC(0.15F, -3.156F, -1.035F, 0.0F),
				rotC(0.225F, -1.161F, -2.435F, 0.0F),
				rotC(0.3F, 1.514F, -3.464F, 0.0F),
				rotC(0.375F, 3.302F, -3.966F, 0.0F),
				rotC(0.45F, 3.156F, -3.864F, 0.0F),
				rotC(0.525F, 1.161F, -3.173F, 0.0F),
				rotC(0.6F, -1.514F, -2.0F, 0.0F),
				rotC(0.675F, -3.302F, -0.522F, 0.0F),
				rotC(0.75F, -3.156F, 1.035F, 0.0F),
				rotC(0.825F, -1.161F, 2.435F, 0.0F),
				rotC(0.9F, 1.514F, 3.464F, 0.0F),
				rotC(0.975F, 3.302F, 3.966F, 0.0F),
				rotC(1.05F, 3.156F, 3.864F, 0.0F),
				rotC(1.125F, 1.161F, 3.173F, 0.0F),
				rotC(1.2F, -1.514F, 2.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("head_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -7.132F, -3.031F, 0.0F),
				rotC(0.075F, -7.332F, -3.47F, 0.0F),
				rotC(0.15F, -6.4F, -3.381F, 0.0F),
				rotC(0.225F, -4.882F, -2.777F, 0.0F),
				rotC(0.3F, -3.668F, -1.75F, 0.0F),
				rotC(0.375F, -3.468F, -0.457F, 0.0F),
				rotC(0.45F, -4.4F, 0.906F, 0.0F),
				rotC(0.525F, -5.918F, 2.131F, 0.0F),
				rotC(0.6F, -7.132F, 3.031F, 0.0F),
				rotC(0.675F, -7.332F, 3.47F, 0.0F),
				rotC(0.75F, -6.4F, 3.381F, 0.0F),
				rotC(0.825F, -4.882F, 2.777F, 0.0F),
				rotC(0.9F, -3.668F, 1.75F, 0.0F),
				rotC(0.975F, -3.468F, 0.457F, 0.0F),
				rotC(1.05F, -4.4F, -0.906F, 0.0F),
				rotC(1.125F, -5.918F, -2.131F, 0.0F),
				rotC(1.2F, -7.132F, -3.031F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 8.0F, 0.0F, 0.0F),
				rotC(0.075F, 8.0F, 0.0F, 0.0F),
				rotC(0.15F, 8.0F, 0.0F, 0.0F),
				rotC(0.225F, 8.0F, 0.0F, 0.0F),
				rotC(0.3F, 8.0F, 0.0F, 0.0F),
				rotC(0.375F, 8.0F, 0.0F, 0.0F),
				rotC(0.45F, 9.035F, 0.0F, 0.0F),
				rotC(0.525F, 10.435F, 0.0F, 0.0F),
				rotC(0.6F, 11.464F, 0.0F, 0.0F),
				rotC(0.675F, 11.966F, 0.0F, 0.0F),
				rotC(0.75F, 11.864F, 0.0F, 0.0F),
				rotC(0.825F, 11.173F, 0.0F, 0.0F),
				rotC(0.9F, 10.0F, 0.0F, 0.0F),
				rotC(0.975F, 8.522F, 0.0F, 0.0F),
				rotC(1.05F, 8.0F, 0.0F, 0.0F),
				rotC(1.125F, 8.0F, 0.0F, 0.0F),
				rotC(1.2F, 8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 2.205F, 3.279F, 0.0F),
				rotC(0.075F, 2.574F, 0.838F, 0.0F),
				rotC(0.15F, 2.49F, -1.731F, 0.0F),
				rotC(0.225F, 2.001F, -4.037F, 0.0F),
				rotC(0.3F, 1.395F, -5.728F, 0.0F),
				rotC(0.375F, 1.026F, -6.547F, 0.0F),
				rotC(0.45F, 1.11F, -6.369F, 0.0F),
				rotC(0.525F, 1.599F, -5.222F, 0.0F),
				rotC(0.6F, 2.205F, -3.279F, 0.0F),
				rotC(0.675F, 2.574F, -0.838F, 0.0F),
				rotC(0.75F, 2.49F, 1.731F, 0.0F),
				rotC(0.825F, 2.001F, 4.037F, 0.0F),
				rotC(0.9F, 1.395F, 5.728F, 0.0F),
				rotC(0.975F, 1.026F, 6.547F, 0.0F),
				rotC(1.05F, 1.11F, 6.369F, 0.0F),
				rotC(1.125F, 1.599F, 5.222F, 0.0F),
				rotC(1.2F, 2.205F, 3.279F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 1.41F, 7.072F, 0.0F),
				rotC(0.075F, 2.018F, 4.945F, 0.0F),
				rotC(0.15F, 2.499F, 2.065F, 0.0F),
				rotC(0.225F, 2.57F, -1.129F, 0.0F),
				rotC(0.3F, 2.19F, -4.151F, 0.0F),
				rotC(0.375F, 1.582F, -6.541F, 0.0F),
				rotC(0.45F, 1.101F, -7.936F, 0.0F),
				rotC(0.525F, 1.03F, -8.122F, 0.0F),
				rotC(0.6F, 1.41F, -7.072F, 0.0F),
				rotC(0.675F, 2.018F, -4.945F, 0.0F),
				rotC(0.75F, 2.499F, -2.065F, 0.0F),
				rotC(0.825F, 2.57F, 1.129F, 0.0F),
				rotC(0.9F, 2.19F, 4.151F, 0.0F),
				rotC(0.975F, 1.582F, 6.541F, 0.0F),
				rotC(1.05F, 1.101F, 7.936F, 0.0F),
				rotC(1.125F, 1.03F, 8.122F, 0.0F),
				rotC(1.2F, 1.41F, 7.072F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 1.0F, 9.799F, 0.0F),
				rotC(0.075F, 1.247F, 9.013F, 0.0F),
				rotC(0.15F, 1.817F, 6.854F, 0.0F),
				rotC(0.225F, 2.378F, 3.652F, 0.0F),
				rotC(0.3F, 2.6F, -0.106F, 0.0F),
				rotC(0.375F, 2.353F, -3.848F, 0.0F),
				rotC(0.45F, 1.783F, -7.004F, 0.0F),
				rotC(0.525F, 1.222F, -9.094F, 0.0F),
				rotC(0.6F, 1.0F, -9.799F, 0.0F),
				rotC(0.675F, 1.247F, -9.013F, 0.0F),
				rotC(0.75F, 1.817F, -6.854F, 0.0F),
				rotC(0.825F, 2.378F, -3.652F, 0.0F),
				rotC(0.9F, 2.6F, 0.106F, 0.0F),
				rotC(0.975F, 2.353F, 3.848F, 0.0F),
				rotC(1.05F, 1.783F, 7.004F, 0.0F),
				rotC(1.125F, 1.222F, 9.094F, 0.0F),
				rotC(1.2F, 1.0F, 9.799F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 1.38F, 9.954F, 0.0F),
				rotC(0.075F, 1.022F, 11.323F, 0.0F),
				rotC(0.15F, 1.119F, 10.968F, 0.0F),
				rotC(0.225F, 1.615F, 8.943F, 0.0F),
				rotC(0.3F, 2.22F, 5.557F, 0.0F),
				rotC(0.375F, 2.578F, 1.325F, 0.0F),
				rotC(0.45F, 2.481F, -3.109F, 0.0F),
				rotC(0.525F, 1.985F, -7.069F, 0.0F),
				rotC(0.6F, 1.38F, -9.954F, 0.0F),
				rotC(0.675F, 1.022F, -11.323F, 0.0F),
				rotC(0.75F, 1.119F, -10.968F, 0.0F),
				rotC(0.825F, 1.615F, -8.943F, 0.0F),
				rotC(0.9F, 2.22F, -5.557F, 0.0F),
				rotC(0.975F, 2.578F, -1.325F, 0.0F),
				rotC(1.05F, 2.481F, 3.109F, 0.0F),
				rotC(1.125F, 1.985F, 7.069F, 0.0F),
				rotC(1.2F, 1.38F, 9.954F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 2.175F, 6.702F, 0.0F),
				rotC(0.075F, 1.565F, 10.454F, 0.0F),
				rotC(0.15F, 1.093F, 12.616F, 0.0F),
				rotC(0.225F, 1.035F, 12.856F, 0.0F),
				rotC(0.3F, 1.425F, 11.14F, 0.0F),
				rotC(0.375F, 2.035F, 7.727F, 0.0F),
				rotC(0.45F, 2.507F, 3.138F, 0.0F),
				rotC(0.525F, 2.565F, -1.928F, 0.0F),
				rotC(0.6F, 2.175F, -6.702F, 0.0F),
				rotC(0.675F, 1.565F, -10.454F, 0.0F),
				rotC(0.75F, 1.093F, -12.616F, 0.0F),
				rotC(0.825F, 1.035F, -12.856F, 0.0F),
				rotC(0.9F, 1.425F, -11.14F, 0.0F),
				rotC(0.975F, 2.035F, -7.727F, 0.0F),
				rotC(1.05F, 2.507F, -3.138F, 0.0F),
				rotC(1.125F, 2.565F, 1.928F, 0.0F),
				rotC(1.2F, 2.175F, 6.702F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 2.599F, 0.315F, 0.0F),
				rotC(0.075F, 2.341F, 5.877F, 0.0F),
				rotC(0.15F, 1.765F, 10.544F, 0.0F),
				rotC(0.225F, 1.21F, 13.606F, 0.0F),
				rotC(0.3F, 1.001F, 14.597F, 0.0F),
				rotC(0.375F, 1.259F, 13.365F, 0.0F),
				rotC(0.45F, 1.835F, 10.098F, 0.0F),
				rotC(0.525F, 2.39F, 5.295F, 0.0F),
				rotC(0.6F, 2.599F, -0.315F, 0.0F),
				rotC(0.675F, 2.341F, -5.877F, 0.0F),
				rotC(0.75F, 1.765F, -10.544F, 0.0F),
				rotC(0.825F, 1.21F, -13.606F, 0.0F),
				rotC(0.9F, 1.001F, -14.597F, 0.0F),
				rotC(0.975F, 1.259F, -13.365F, 0.0F),
				rotC(1.05F, 1.835F, -10.098F, 0.0F),
				rotC(1.125F, 2.39F, -5.295F, 0.0F),
				rotC(1.2F, 2.599F, 0.315F, 0.0F)
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
			return b.build();
		}
	}

	private static final class AttackAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.34F, 0.0F, 0.4F, 1.4F),
				posC(0.56F, 0.0F, 0.1F, -1.2F),
				posC(0.78F, 0.0F, 0.1F, -1.6F),
				posC(1.0F, 0.0F, 0.15F, -2.6F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.34F, -9.0F, 0.0F, 0.0F),
				rotC(0.56F, 5.0F, 5.0F, 0.0F),
				rotC(0.78F, 5.0F, -5.0F, 0.0F),
				rotC(1.0F, 9.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.3F, -18.0F, 6.0F, -4.0F),
				rotC(0.52F, 24.0F, 6.0F, -3.0F),
				rotC(0.6F, 20.0F, 3.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.3F, -14.0F, 0.0F, 0.0F),
				rotC(0.52F, 16.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.3F, 10.0F, 0.0F, 0.0F),
				rotC(0.52F, 10.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("head_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.3F, 14.0F, 0.0F, 0.0F),
				rotC(0.52F, -28.0F, 0.0F, 0.0F),
				rotC(0.6F, -22.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.3F, 26.0F, 0.0F, 0.0F),
				rotC(0.47F, 36.0F, 0.0F, 0.0F),
				rotC(0.6F, -5.0F, 0.0F, 0.0F),
				rotC(0.76F, 6.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, -18.0F, -6.0F, 4.0F),
				rotC(0.74F, 24.0F, -6.0F, 3.0F),
				rotC(0.82F, 20.0F, -3.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, -14.0F, 0.0F, 0.0F),
				rotC(0.74F, 16.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 10.0F, 0.0F, 0.0F),
				rotC(0.74F, 10.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("head_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 14.0F, 0.0F, 0.0F),
				rotC(0.74F, -28.0F, 0.0F, 0.0F),
				rotC(0.82F, -22.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 26.0F, 0.0F, 0.0F),
				rotC(0.69F, 36.0F, 0.0F, 0.0F),
				rotC(0.82F, -5.0F, 0.0F, 0.0F),
				rotC(0.98F, 6.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.54F, -18.0F, 0.0F, 0.0F),
				rotC(0.96F, 24.0F, 0.0F, 0.0F),
				rotC(1.04F, 20.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.54F, -14.0F, 0.0F, 0.0F),
				rotC(0.96F, 16.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.54F, 10.0F, 0.0F, 0.0F),
				rotC(0.96F, 10.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("head_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.54F, 14.0F, 0.0F, 0.0F),
				rotC(0.96F, -28.0F, 0.0F, 0.0F),
				rotC(1.04F, -22.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.54F, 26.0F, 0.0F, 0.0F),
				rotC(0.91F, 36.0F, 0.0F, 0.0F),
				rotC(1.04F, -5.0F, 0.0F, 0.0F),
				rotC(1.2F, 6.0F, 0.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.36F, 2.5F, -8.0F, 0.0F),
				rotC(0.82F, 3.0F, 10.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.36F, 2.5F, -8.0F, 0.0F),
				rotC(0.82F, 3.0F, 10.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.36F, 2.5F, -8.0F, 0.0F),
				rotC(0.82F, 3.0F, 10.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.36F, 2.5F, -8.0F, 0.0F),
				rotC(0.82F, 3.0F, 10.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.36F, 2.5F, -8.0F, 0.0F),
				rotC(0.82F, 3.0F, 10.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.36F, 2.5F, -8.0F, 0.0F),
				rotC(0.82F, 3.0F, 10.0F, 0.0F),
				rotC(1.6F, 0.0F, 0.0F, 0.0F)
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
			return b.build();
		}
	}

	private static final class SubmergeAmbushAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.3F, 0.0F, -2.2F, 0.2F),
				posL(0.65F, 0.0F, -4.208F, 0.5F),
				posL(1.25F, 0.0F, -4.208F, 0.8F),
				posL(1.58F, 0.0F, 2.2F, -1.8F),
				posL(2.22F, 0.0F, 0.2F, -0.2F),
				posL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 8.0F, 0.0F, 0.0F),
				rotL(1.25F, 8.0F, 0.0F, 0.0F),
				rotC(1.58F, -14.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -4.0F, 0.0F, 36.0F),
				rotL(0.65F, -8.0F, 0.0F, 56.0F),
				rotL(1.25F, -8.0F, 0.0F, 56.0F),
				rotL(1.58F, 14.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 18.0F, 0.0F, -20.0F),
				rotL(1.25F, 18.0F, 0.0F, -20.0F),
				rotL(1.58F, -10.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, -10.0F, 0.0F, -36.0F),
				rotL(1.25F, -10.0F, 0.0F, -36.0F),
				rotL(1.58F, -4.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -4.0F, 0.0F, -36.0F),
				rotL(0.65F, -8.0F, 0.0F, -56.0F),
				rotL(1.25F, -8.0F, 0.0F, -56.0F),
				rotL(1.58F, 14.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 18.0F, 0.0F, 20.0F),
				rotL(1.25F, 18.0F, 0.0F, 20.0F),
				rotL(1.58F, -10.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, -10.0F, 0.0F, 36.0F),
				rotL(1.25F, -10.0F, 0.0F, 36.0F),
				rotL(1.58F, -4.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -4.0F, 0.0F, 36.0F),
				rotL(0.65F, -8.0F, 0.0F, 56.0F),
				rotL(1.25F, -8.0F, 0.0F, 56.0F),
				rotL(1.58F, 14.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shank_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 18.0F, 0.0F, -20.0F),
				rotL(1.25F, 18.0F, 0.0F, -20.0F),
				rotL(1.58F, -10.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("foot_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, -10.0F, 0.0F, -36.0F),
				rotL(1.25F, -10.0F, 0.0F, -36.0F),
				rotL(1.58F, -4.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -4.0F, 0.0F, -36.0F),
				rotL(0.65F, -8.0F, 0.0F, -56.0F),
				rotL(1.25F, -8.0F, 0.0F, -56.0F),
				rotL(1.58F, 14.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("shank_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 18.0F, 0.0F, 20.0F),
				rotL(1.25F, 18.0F, 0.0F, 20.0F),
				rotL(1.58F, -10.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("foot_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, -10.0F, 0.0F, 36.0F),
				rotL(1.25F, -10.0F, 0.0F, 36.0F),
				rotL(1.58F, -4.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 34.0F, 0.0F, 0.0F),
				rotL(1.25F, 36.0F, 0.0F, 0.0F),
				rotC(1.58F, -12.0F, 0.0F, 0.0F),
				rotC(1.95F, 18.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, -28.0F, 0.0F, 0.0F),
				rotL(1.25F, -30.0F, 0.0F, 0.0F),
				rotC(1.58F, 14.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("head_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, -16.0F, 0.0F, 0.0F),
				rotL(1.25F, -18.0F, 0.0F, 0.0F),
				rotC(1.58F, 16.0F, 0.0F, 0.0F),
				rotC(1.95F, -18.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, -3.0F, 0.0F, 0.0F),
				rotC(1.58F, 38.0F, 0.0F, 0.0F),
				rotC(1.98F, -4.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 34.0F, 10.0F, -8.0F),
				rotL(1.25F, 36.0F, 10.0F, -8.0F),
				rotC(1.58F, -12.0F, 6.0F, -4.0F),
				rotC(1.95F, 18.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, -28.0F, 0.0F, 0.0F),
				rotL(1.25F, -30.0F, 0.0F, 0.0F),
				rotC(1.58F, 14.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("head_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, -16.0F, 0.0F, 0.0F),
				rotL(1.25F, -18.0F, 0.0F, 0.0F),
				rotC(1.58F, 16.0F, 0.0F, 0.0F),
				rotC(1.95F, -18.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, -3.0F, 0.0F, 0.0F),
				rotC(1.58F, 38.0F, 0.0F, 0.0F),
				rotC(1.98F, -4.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 34.0F, -10.0F, 8.0F),
				rotL(1.25F, 36.0F, -10.0F, 8.0F),
				rotC(1.58F, -12.0F, -6.0F, 4.0F),
				rotC(1.95F, 18.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, -28.0F, 0.0F, 0.0F),
				rotL(1.25F, -30.0F, 0.0F, 0.0F),
				rotC(1.58F, 14.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("head_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, -16.0F, 0.0F, 0.0F),
				rotL(1.25F, -18.0F, 0.0F, 0.0F),
				rotC(1.58F, 16.0F, 0.0F, 0.0F),
				rotC(1.95F, -18.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, -3.0F, 0.0F, 0.0F),
				rotC(1.58F, 38.0F, 0.0F, 0.0F),
				rotC(1.98F, -4.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 8.0F, 6.0F, 0.0F),
				rotL(1.25F, 8.0F, -6.0F, 0.0F),
				rotC(1.58F, 3.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 8.0F, -6.0F, 0.0F),
				rotL(1.25F, 8.0F, 6.0F, 0.0F),
				rotC(1.58F, 3.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 2.0F, 6.0F, 0.0F),
				rotL(1.25F, 2.0F, -6.0F, 0.0F),
				rotC(1.58F, 3.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 2.0F, -6.0F, 0.0F),
				rotL(1.25F, 2.0F, 6.0F, 0.0F),
				rotC(1.58F, 3.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 2.0F, 6.0F, 0.0F),
				rotL(1.25F, 2.0F, -6.0F, 0.0F),
				rotC(1.58F, 3.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.65F, 2.0F, -6.0F, 0.0F),
				rotL(1.25F, 2.0F, 6.0F, 0.0F),
				rotC(1.58F, 3.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(2.6F);
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
			return b.build();
		}
	}

	private static final class WitherRoarAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, -12.0F, 0.0F, 0.0F),
				rotC(1.35F, -10.0F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, -14.0F, 0.0F, 0.0F),
				rotC(0.9F, 8.0F, 0.0F, 0.0F),
				rotC(1.45F, 6.0F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("head_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 18.0F, 0.0F, 0.0F),
				rotC(0.75F, -10.0F, 5.0F, 0.0F),
				rotC(1.05F, -12.0F, -5.0F, 0.0F),
				rotC(1.38F, -8.0F, 4.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.4F, 38.0F, 0.0F, 0.0F),
				rotC(0.85F, 32.0F, 0.0F, 0.0F),
				rotC(1.15F, 40.0F, 0.0F, 0.0F),
				rotC(1.45F, 34.0F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, -14.0F, 10.0F, -6.0F),
				rotC(0.9F, 8.0F, 12.0F, -6.0F),
				rotC(1.45F, 6.0F, 10.0F, -6.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("head_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 18.0F, 0.0F, 0.0F),
				rotC(0.75F, -10.0F, 5.0F, 0.0F),
				rotC(1.05F, -12.0F, -5.0F, 0.0F),
				rotC(1.38F, -8.0F, 4.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.4F, 38.0F, 0.0F, 0.0F),
				rotC(0.85F, 32.0F, 0.0F, 0.0F),
				rotC(1.15F, 40.0F, 0.0F, 0.0F),
				rotC(1.45F, 34.0F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, -14.0F, -10.0F, 6.0F),
				rotC(0.9F, 8.0F, -12.0F, 6.0F),
				rotC(1.45F, 6.0F, -10.0F, 6.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("head_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 18.0F, 0.0F, 0.0F),
				rotC(0.75F, -10.0F, 5.0F, 0.0F),
				rotC(1.05F, -12.0F, -5.0F, 0.0F),
				rotC(1.38F, -8.0F, 4.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.4F, 38.0F, 0.0F, 0.0F),
				rotC(0.85F, 32.0F, 0.0F, 0.0F),
				rotC(1.15F, 40.0F, 0.0F, 0.0F),
				rotC(1.45F, 34.0F, 0.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.55F, 3.0F, -12.0F, 0.0F),
				rotC(1.25F, 3.0F, 12.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.55F, 3.0F, -12.0F, 0.0F),
				rotC(1.25F, 3.0F, 12.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.55F, 3.0F, -12.0F, 0.0F),
				rotC(1.25F, 3.0F, 12.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.55F, 3.0F, -12.0F, 0.0F),
				rotC(1.25F, 3.0F, 12.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.55F, 3.0F, -12.0F, 0.0F),
				rotC(1.25F, 3.0F, 12.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.55F, 3.0F, -12.0F, 0.0F),
				rotC(1.25F, 3.0F, 12.0F, 0.0F),
				rotC(2.0F, 0.0F, 0.0F, 0.0F)
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
			return b.build();
		}
	}

	private static final class HurtAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.15F, 0.0F, 0.1F, 1.8F),
				posL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.15F, -11.0F, 0.0F, 4.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.16F, -20.0F, 0.0F, 0.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("head_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.16F, 16.0F, 0.0F, 0.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.16F, 26.0F, 0.0F, 0.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.16F, -20.0F, 8.0F, -6.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("head_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.16F, 16.0F, 0.0F, 0.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.16F, 26.0F, 0.0F, 0.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.16F, -20.0F, -8.0F, 6.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("head_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.16F, 16.0F, 0.0F, 0.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.16F, 26.0F, 0.0F, 0.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
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
			return b.build();
		}
	}

	private static final class DeathAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.35F, -0.4F, 0.3F, 1.0F),
				posL(0.72F, -0.1F, -2.5F, 0.4F),
				posL(1.15F, 0.0F, -4.991F, 0.0F),
				posL(2.0F, 0.0F, -4.991F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, -12.0F, 0.0F, 0.0F),
				rotL(1.15F, 10.0F, 0.0F, 0.0F),
				rotL(2.0F, 10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.72F, -6.0F, 0.0F, 40.0F),
				rotL(1.15F, -10.0F, 0.0F, 60.0F),
				rotL(2.0F, -10.0F, 0.0F, 60.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 20.0F, 0.0F, -20.0F),
				rotL(2.0F, 20.0F, 0.0F, -20.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, -10.0F, 0.0F, -40.0F),
				rotL(2.0F, -10.0F, 0.0F, -40.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.72F, -6.0F, 0.0F, -40.0F),
				rotL(1.15F, -10.0F, 0.0F, -60.0F),
				rotL(2.0F, -10.0F, 0.0F, -60.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 20.0F, 0.0F, 20.0F),
				rotL(2.0F, 20.0F, 0.0F, 20.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, -10.0F, 0.0F, 40.0F),
				rotL(2.0F, -10.0F, 0.0F, 40.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.72F, -6.0F, 0.0F, 40.0F),
				rotL(1.15F, -10.0F, 0.0F, 60.0F),
				rotL(2.0F, -10.0F, 0.0F, 60.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("shank_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 20.0F, 0.0F, -20.0F),
				rotL(2.0F, 20.0F, 0.0F, -20.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("foot_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, -10.0F, 0.0F, -40.0F),
				rotL(2.0F, -10.0F, 0.0F, -40.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.72F, -6.0F, 0.0F, -40.0F),
				rotL(1.15F, -10.0F, 0.0F, -60.0F),
				rotL(2.0F, -10.0F, 0.0F, -60.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("shank_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 20.0F, 0.0F, 20.0F),
				rotL(2.0F, 20.0F, 0.0F, 20.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("foot_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, -10.0F, 0.0F, 40.0F),
				rotL(2.0F, -10.0F, 0.0F, 40.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -16.0F, 0.0F, 0.0F),
				rotL(1.25F, 99.28F, 0.0F, 0.0F),
				rotL(2.0F, 99.28F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, 22.0F, 0.0F, 0.0F),
				rotL(2.0F, 22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, -22.0F, 0.0F, 0.0F),
				rotL(2.0F, -22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, -24.0F, 0.0F, 0.0F),
				rotL(2.0F, -24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("head_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, -34.0F, 0.0F, 0.0F),
				rotL(2.0F, -34.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 28.0F, 0.0F, 0.0F),
				rotL(1.25F, -4.0F, 0.0F, 0.0F),
				rotL(2.0F, -4.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -16.0F, 8.0F, -5.0F),
				rotL(1.25F, 65.682F, 16.0F, -10.0F),
				rotL(2.0F, 65.682F, 16.0F, -10.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, 22.0F, 0.0F, 0.0F),
				rotL(2.0F, 22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, -22.0F, 0.0F, 0.0F),
				rotL(2.0F, -22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, -24.0F, 0.0F, 0.0F),
				rotL(2.0F, -24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("head_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, -34.0F, -8.0F, 10.0F),
				rotL(2.0F, -34.0F, -8.0F, 10.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 28.0F, 0.0F, 0.0F),
				rotL(1.25F, -4.0F, 0.0F, 0.0F),
				rotL(2.0F, -4.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, -16.0F, -8.0F, 5.0F),
				rotL(1.25F, 65.682F, -16.0F, 10.0F),
				rotL(2.0F, 65.682F, -16.0F, 10.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, 22.0F, 0.0F, 0.0F),
				rotL(2.0F, 22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, -22.0F, 0.0F, 0.0F),
				rotL(2.0F, -22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, -24.0F, 0.0F, 0.0F),
				rotL(2.0F, -24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("head_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, -34.0F, 8.0F, -10.0F),
				rotL(2.0F, -34.0F, 8.0F, -10.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 28.0F, 0.0F, 0.0F),
				rotL(1.25F, -4.0F, 0.0F, 0.0F),
				rotL(2.0F, -4.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 10.0F, -3.0F, 0.0F),
				rotL(2.0F, 10.0F, -3.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 4.0F, -6.0F, 0.0F),
				rotL(2.0F, 4.0F, -6.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, -9.0F, 0.0F),
				rotL(2.0F, 0.0F, -9.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, -12.0F, 0.0F),
				rotL(2.0F, 0.0F, -12.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, -15.0F, 0.0F),
				rotL(2.0F, 0.0F, -15.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, -18.0F, 0.0F),
				rotL(2.0F, 0.0F, -18.0F, 0.0F)
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
			return b.build();
		}
	}

	private static final class SpellChargeAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, -1.5F, 0.0F, 0.0F),
				rotL(0.9F, -3.4F, 0.0F, 0.0F),
				rotL(1.2F, -3.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.5F, 0.0F, 0.25F, 0.6F),
				posL(0.9F, 0.0F, 0.6F, 1.35F),
				posL(1.2F, 0.0F, 0.52F, 1.2F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, -9.0F, 0.0F, 0.0F),
				rotL(0.88F, -18.0F, 0.0F, 0.0F),
				rotL(1.2F, -16.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.55F, 1.02F, 1.02F, 1.02F),
				sclL(0.92F, 1.05F, 1.05F, 1.05F),
				sclL(1.2F, 1.04F, 1.04F, 1.04F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 14.0F, 0.0F, 4.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, -21.0F, 0.0F, -4.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 14.0F, 0.0F, -4.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, -21.0F, 0.0F, 4.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("foot_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 3.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("foot_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 3.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, -10.0F, 0.0F, 0.0F),
				rotL(0.9F, -22.0F, 0.0F, 0.0F),
				rotL(1.2F, -20.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, -16.0F, 0.0F, 0.0F),
				rotL(1.2F, -14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, 14.0F, 0.0F, 0.0F),
				rotL(1.2F, 12.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_c_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.6F, 1.03F, 1.02F, 1.03F),
				sclL(0.92F, 1.1F, 1.05F, 1.1F),
				sclL(1.2F, 1.08F, 1.04F, 1.08F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, 12.0F, 0.0F, 0.0F),
				rotL(1.2F, 10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_c_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.65F, 1.04F, 1.02F, 1.04F),
				sclL(0.95F, 1.12F, 1.06F, 1.12F),
				sclL(1.2F, 1.1F, 1.05F, 1.1F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("head_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 10.0F, 0.0F, 0.0F),
				rotL(0.9F, 23.0F, 0.0F, 0.0F),
				rotL(1.2F, 20.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 14.0F, 0.0F, 0.0F),
				rotL(0.88F, 38.0F, 0.0F, 0.0F),
				rotL(1.2F, 34.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, -10.0F, 7.0F, -5.0F),
				rotL(0.9F, -22.0F, 15.0F, -10.0F),
				rotL(1.2F, -20.0F, 14.0F, -10.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, -16.0F, 5.0F, 0.0F),
				rotL(1.2F, -14.0F, 5.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, 14.0F, 0.0F, -3.0F),
				rotL(1.2F, 12.0F, 0.0F, -3.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_l_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.6F, 1.03F, 1.02F, 1.03F),
				sclL(0.92F, 1.1F, 1.05F, 1.1F),
				sclL(1.2F, 1.08F, 1.04F, 1.08F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, 12.0F, 0.0F, 0.0F),
				rotL(1.2F, 10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_l_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.65F, 1.04F, 1.02F, 1.04F),
				sclL(0.95F, 1.12F, 1.06F, 1.12F),
				sclL(1.2F, 1.1F, 1.05F, 1.1F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("head_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 10.0F, 0.0F, 0.0F),
				rotL(0.9F, 23.0F, 0.0F, 0.0F),
				rotL(1.2F, 20.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 14.0F, 0.0F, 0.0F),
				rotL(0.88F, 38.0F, 0.0F, 0.0F),
				rotL(1.2F, 34.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, -10.0F, -7.0F, 5.0F),
				rotL(0.9F, -22.0F, -15.0F, 10.0F),
				rotL(1.2F, -20.0F, -14.0F, 10.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, -16.0F, -5.0F, 0.0F),
				rotL(1.2F, -14.0F, -5.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, 14.0F, 0.0F, 3.0F),
				rotL(1.2F, 12.0F, 0.0F, 3.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_r_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.6F, 1.03F, 1.02F, 1.03F),
				sclL(0.92F, 1.1F, 1.05F, 1.1F),
				sclL(1.2F, 1.08F, 1.04F, 1.08F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.88F, 12.0F, 0.0F, 0.0F),
				rotL(1.2F, 10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_r_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.65F, 1.04F, 1.02F, 1.04F),
				sclL(0.95F, 1.12F, 1.06F, 1.12F),
				sclL(1.2F, 1.1F, 1.05F, 1.1F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("head_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 10.0F, 0.0F, 0.0F),
				rotL(0.9F, 23.0F, 0.0F, 0.0F),
				rotL(1.2F, 20.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 14.0F, 0.0F, 0.0F),
				rotL(0.88F, 38.0F, 0.0F, 0.0F),
				rotL(1.2F, 34.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 2.5F, -5.0F, 0.0F),
				rotL(1.2F, 3.5F, 4.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 2.5F, 5.0F, 0.0F),
				rotL(1.2F, 3.5F, -4.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 2.5F, -5.0F, 0.0F),
				rotL(1.2F, 3.5F, 4.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 2.5F, 5.0F, 0.0F),
				rotL(1.2F, 3.5F, -4.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 2.5F, -5.0F, 0.0F),
				rotL(1.2F, 3.5F, 4.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 2.5F, 5.0F, 0.0F),
				rotL(1.2F, 3.5F, -4.0F, 0.0F)
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
			ch_41(b);
			return b.build();
		}
	}

	private static final class SpellReleaseAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -3.0F, 0.0F, 0.0F),
				rotL(0.2F, 2.0F, 0.0F, 0.0F),
				rotL(0.5F, -1.2F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.52F, 1.2F),
				posL(0.2F, 0.0F, 0.72F, -2.2F),
				posL(0.5F, 0.0F, 0.34F, 0.7F),
				posL(0.95F, 0.0F, 0.1F, -0.2F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -16.0F, 0.0F, 0.0F),
				rotL(0.2F, 12.0F, 0.0F, 0.0F),
				rotL(0.52F, -6.0F, 0.0F, 0.0F),
				rotL(0.95F, 2.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.04F, 1.04F, 1.04F),
				sclL(0.2F, 0.97F, 0.97F, 0.97F),
				sclL(0.55F, 1.01F, 1.01F, 1.01F),
				sclL(1.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 14.0F, 0.0F, 4.0F),
				rotL(0.2F, -8.0F, 0.0F, 0.0F),
				rotL(0.55F, 3.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, 0.0F, 0.0F),
				rotL(0.2F, 8.0F, 0.0F, 0.0F),
				rotL(0.55F, -3.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -21.0F, 0.0F, -4.0F),
				rotL(0.2F, -2.0F, 0.0F, 0.0F),
				rotL(0.55F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 14.0F, 0.0F, -4.0F),
				rotL(0.2F, -8.0F, 0.0F, 0.0F),
				rotL(0.55F, 3.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("foreleg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, 0.0F, 0.0F),
				rotL(0.2F, 8.0F, 0.0F, 0.0F),
				rotL(0.55F, -3.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("foot_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -21.0F, 0.0F, 4.0F),
				rotL(0.2F, -2.0F, 0.0F, 0.0F),
				rotL(0.55F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("foot_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.0F, 0.0F, 0.0F),
				rotL(0.2F, -2.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("foot_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.0F, 0.0F, 0.0F),
				rotL(0.2F, -2.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -20.0F, 0.0F, 0.0F),
				rotL(0.2F, 26.0F, 0.0F, 0.0F),
				rotL(0.5F, -8.0F, 0.0F, 0.0F),
				rotL(0.92F, 3.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 0.0F, 0.0F),
				rotL(0.2F, 16.0F, 0.0F, 0.0F),
				rotL(0.52F, -6.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 12.0F, 0.0F, 0.0F),
				rotL(0.2F, -10.0F, 0.0F, 0.0F),
				rotL(0.52F, 4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_c_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.08F, 1.04F, 1.08F),
				sclL(0.22F, 0.96F, 1.0F, 0.96F),
				sclL(0.6F, 1.0F, 1.0F, 1.0F),
				sclL(1.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, 0.0F, 0.0F),
				rotL(0.2F, -12.0F, 0.0F, 0.0F),
				rotL(0.52F, 4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_c_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.1F, 1.05F, 1.1F),
				sclL(0.22F, 0.95F, 1.0F, 0.95F),
				sclL(0.6F, 1.0F, 1.0F, 1.0F),
				sclL(1.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("head_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 20.0F, 0.0F, 0.0F),
				rotL(0.2F, -30.0F, 0.0F, 0.0F),
				rotL(0.52F, 8.0F, 0.0F, 0.0F),
				rotL(0.95F, -3.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 34.0F, 0.0F, 0.0F),
				rotL(0.2F, 44.0F, 0.0F, 0.0F),
				rotL(0.52F, 12.0F, 0.0F, 0.0F),
				rotL(0.85F, -3.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -20.0F, 14.0F, -10.0F),
				rotL(0.2F, 26.0F, 12.0F, -6.0F),
				rotL(0.5F, -8.0F, 6.0F, -4.0F),
				rotL(0.92F, 3.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 5.0F, 0.0F),
				rotL(0.2F, 16.0F, 3.0F, 0.0F),
				rotL(0.52F, -6.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 12.0F, 0.0F, -3.0F),
				rotL(0.2F, -10.0F, 0.0F, 0.0F),
				rotL(0.52F, 4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_l_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.08F, 1.04F, 1.08F),
				sclL(0.22F, 0.96F, 1.0F, 0.96F),
				sclL(0.6F, 1.0F, 1.0F, 1.0F),
				sclL(1.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, 0.0F, 0.0F),
				rotL(0.2F, -12.0F, 0.0F, 0.0F),
				rotL(0.52F, 4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_l_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.1F, 1.05F, 1.1F),
				sclL(0.22F, 0.95F, 1.0F, 0.95F),
				sclL(0.6F, 1.0F, 1.0F, 1.0F),
				sclL(1.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("head_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 20.0F, 0.0F, 0.0F),
				rotL(0.2F, -30.0F, 0.0F, 0.0F),
				rotL(0.52F, 8.0F, 0.0F, 0.0F),
				rotL(0.95F, -3.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 34.0F, 0.0F, 0.0F),
				rotL(0.2F, 44.0F, 0.0F, 0.0F),
				rotL(0.52F, 12.0F, 0.0F, 0.0F),
				rotL(0.85F, -3.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -20.0F, -14.0F, 10.0F),
				rotL(0.2F, 26.0F, -12.0F, 6.0F),
				rotL(0.5F, -8.0F, -6.0F, 4.0F),
				rotL(0.92F, 3.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, -5.0F, 0.0F),
				rotL(0.2F, 16.0F, -3.0F, 0.0F),
				rotL(0.52F, -6.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 12.0F, 0.0F, 3.0F),
				rotL(0.2F, -10.0F, 0.0F, 0.0F),
				rotL(0.52F, 4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_r_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.08F, 1.04F, 1.08F),
				sclL(0.22F, 0.96F, 1.0F, 0.96F),
				sclL(0.6F, 1.0F, 1.0F, 1.0F),
				sclL(1.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, 0.0F, 0.0F),
				rotL(0.2F, -12.0F, 0.0F, 0.0F),
				rotL(0.52F, 4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_r_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.1F, 1.05F, 1.1F),
				sclL(0.22F, 0.95F, 1.0F, 0.95F),
				sclL(0.6F, 1.0F, 1.0F, 1.0F),
				sclL(1.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("head_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 20.0F, 0.0F, 0.0F),
				rotL(0.2F, -30.0F, 0.0F, 0.0F),
				rotL(0.52F, 8.0F, 0.0F, 0.0F),
				rotL(0.95F, -3.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 34.0F, 0.0F, 0.0F),
				rotL(0.2F, 44.0F, 0.0F, 0.0F),
				rotL(0.52F, 12.0F, 0.0F, 0.0F),
				rotL(0.85F, -3.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.5F, 4.0F, 0.0F),
				rotL(0.22F, 4.5F, -8.0F, 0.0F),
				rotL(0.62F, 2.0F, 5.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.5F, -4.0F, 0.0F),
				rotL(0.22F, 4.5F, 8.0F, 0.0F),
				rotL(0.62F, 2.0F, -5.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.5F, 4.0F, 0.0F),
				rotL(0.22F, 4.5F, -8.0F, 0.0F),
				rotL(0.62F, 2.0F, 5.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.5F, -4.0F, 0.0F),
				rotL(0.22F, 4.5F, 8.0F, 0.0F),
				rotL(0.62F, 2.0F, -5.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.5F, 4.0F, 0.0F),
				rotL(0.22F, 4.5F, -8.0F, 0.0F),
				rotL(0.62F, 2.0F, 5.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.5F, -4.0F, 0.0F),
				rotL(0.22F, 4.5F, 8.0F, 0.0F),
				rotL(0.62F, 2.0F, -5.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
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
			ch_41(b);
			return b.build();
		}
	}

	private static final class BreathAttackAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.42F, 0.0F, 0.35F, 1.0F),
				posL(0.72F, 0.0F, 0.15F, -1.4F),
				posL(1.25F, 0.0F, 0.15F, -1.6F),
				posL(1.78F, 0.0F, 0.15F, -1.4F),
				posL(2.12F, 0.0F, 0.1F, -0.8F),
				posL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, -12.0F, 0.0F, 0.0F),
				rotC(0.72F, 10.0F, 15.0F, -3.0F),
				rotC(1.25F, 11.0F, 0.0F, 0.0F),
				rotC(1.78F, 10.0F, -15.0F, 3.0F),
				rotC(2.12F, 6.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.42F, 1.05F, 1.05F, 1.05F),
				sclL(0.72F, 1.02F, 1.02F, 1.02F),
				sclL(2.05F, 0.99F, 0.99F, 0.99F),
				sclL(2.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, -16.0F, 0.0F, 0.0F),
				rotC(0.72F, 22.0F, 8.0F, 0.0F),
				rotC(1.25F, 24.0F, 0.0F, 0.0F),
				rotC(1.78F, 22.0F, -8.0F, 0.0F),
				rotC(2.12F, 14.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, -10.0F, 0.0F, 0.0F),
				rotC(0.72F, 14.0F, 4.0F, 0.0F),
				rotC(1.25F, 15.0F, 0.0F, 0.0F),
				rotC(1.78F, 14.0F, -4.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 8.0F, 0.0F, 0.0F),
				rotC(0.72F, -12.0F, 0.0F, 0.0F),
				rotC(1.78F, -12.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_c_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.42F, 1.08F, 1.04F, 1.08F),
				sclL(0.72F, 1.06F, 1.03F, 1.06F),
				sclL(2.05F, 1.05F, 1.02F, 1.05F),
				sclL(2.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("head_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 16.0F, 0.0F, 0.0F),
				rotC(0.72F, -24.0F, 6.0F, 0.0F),
				rotC(1.25F, -26.0F, 0.0F, 0.0F),
				rotC(1.78F, -24.0F, -6.0F, 0.0F),
				rotC(2.12F, -14.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 26.0F, 0.0F, 0.0F),
				rotL(0.7F, 42.0F, 0.0F, 0.0F),
				rotL(0.95F, 38.0F, 0.0F, 0.0F),
				rotL(1.25F, 44.0F, 0.0F, 0.0F),
				rotL(1.55F, 38.0F, 0.0F, 0.0F),
				rotL(1.85F, 43.0F, 0.0F, 0.0F),
				rotL(2.12F, 24.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, -16.0F, 8.0F, -5.0F),
				rotC(0.72F, 22.0F, 17.0F, -5.0F),
				rotC(1.25F, 24.0F, 10.0F, -5.0F),
				rotC(1.78F, 22.0F, 1.0F, -5.0F),
				rotC(2.12F, 14.0F, 6.0F, -3.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, -10.0F, 0.0F, 0.0F),
				rotC(0.72F, 14.0F, 4.0F, 0.0F),
				rotC(1.25F, 15.0F, 0.0F, 0.0F),
				rotC(1.78F, 14.0F, -4.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 8.0F, 0.0F, 0.0F),
				rotC(0.72F, -12.0F, 0.0F, 0.0F),
				rotC(1.78F, -12.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_l_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.42F, 1.08F, 1.04F, 1.08F),
				sclL(0.72F, 1.06F, 1.03F, 1.06F),
				sclL(2.05F, 1.05F, 1.02F, 1.05F),
				sclL(2.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("head_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 16.0F, 0.0F, 0.0F),
				rotC(0.72F, -24.0F, 6.0F, 0.0F),
				rotC(1.25F, -26.0F, 0.0F, 0.0F),
				rotC(1.78F, -24.0F, -6.0F, 0.0F),
				rotC(2.12F, -14.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 26.0F, 0.0F, 0.0F),
				rotL(0.7F, 42.0F, 0.0F, 0.0F),
				rotL(0.95F, 38.0F, 0.0F, 0.0F),
				rotL(1.25F, 44.0F, 0.0F, 0.0F),
				rotL(1.55F, 38.0F, 0.0F, 0.0F),
				rotL(1.85F, 43.0F, 0.0F, 0.0F),
				rotL(2.12F, 24.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, -16.0F, -8.0F, 5.0F),
				rotC(0.72F, 22.0F, -1.0F, 5.0F),
				rotC(1.25F, 24.0F, -10.0F, 5.0F),
				rotC(1.78F, 22.0F, -17.0F, 5.0F),
				rotC(2.12F, 14.0F, -6.0F, 3.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, -10.0F, 0.0F, 0.0F),
				rotC(0.72F, 14.0F, 4.0F, 0.0F),
				rotC(1.25F, 15.0F, 0.0F, 0.0F),
				rotC(1.78F, 14.0F, -4.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 8.0F, 0.0F, 0.0F),
				rotC(0.72F, -12.0F, 0.0F, 0.0F),
				rotC(1.78F, -12.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("neck_4_r_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.42F, 1.08F, 1.04F, 1.08F),
				sclL(0.72F, 1.06F, 1.03F, 1.06F),
				sclL(2.05F, 1.05F, 1.02F, 1.05F),
				sclL(2.6F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("head_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 16.0F, 0.0F, 0.0F),
				rotC(0.72F, -24.0F, 6.0F, 0.0F),
				rotC(1.25F, -26.0F, 0.0F, 0.0F),
				rotC(1.78F, -24.0F, -6.0F, 0.0F),
				rotC(2.12F, -14.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 26.0F, 0.0F, 0.0F),
				rotL(0.7F, 42.0F, 0.0F, 0.0F),
				rotL(0.95F, 38.0F, 0.0F, 0.0F),
				rotL(1.25F, 44.0F, 0.0F, 0.0F),
				rotL(1.55F, 38.0F, 0.0F, 0.0F),
				rotL(1.85F, 43.0F, 0.0F, 0.0F),
				rotL(2.12F, 24.0F, 0.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 2.5F, 0.0F, 0.0F),
				rotC(0.72F, 3.0F, -8.2F, 0.0F),
				rotC(1.25F, 3.0F, 0.0F, 0.0F),
				rotC(1.78F, 3.0F, 8.2F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 2.5F, 0.0F, 0.0F),
				rotC(0.72F, 3.0F, -9.4F, 0.0F),
				rotC(1.25F, 3.0F, 0.0F, 0.0F),
				rotC(1.78F, 3.0F, 9.4F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 2.5F, 0.0F, 0.0F),
				rotC(0.72F, 3.0F, -10.6F, 0.0F),
				rotC(1.25F, 3.0F, 0.0F, 0.0F),
				rotC(1.78F, 3.0F, 10.6F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 2.5F, 0.0F, 0.0F),
				rotC(0.72F, 3.0F, -11.8F, 0.0F),
				rotC(1.25F, 3.0F, 0.0F, 0.0F),
				rotC(1.78F, 3.0F, 11.8F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 2.5F, 0.0F, 0.0F),
				rotC(0.72F, 3.0F, -13.0F, 0.0F),
				rotC(1.25F, 3.0F, 0.0F, 0.0F),
				rotC(1.78F, 3.0F, 13.0F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.42F, 2.5F, 0.0F, 0.0F),
				rotC(0.72F, 3.0F, -14.2F, 0.0F),
				rotC(1.25F, 3.0F, 0.0F, 0.0F),
				rotC(1.78F, 3.0F, 14.2F, 0.0F),
				rotL(2.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(2.6F);
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
			return b.build();
		}
	}

}
