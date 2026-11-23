package ddraig.net.entropica.client.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Exact Blockbench keyframe animations for Obsidian Scorpioid (Fauna 53 — Lava Beast).
 * Split into per-animation static builders and <=60-keyframe helpers so constant pools and method bytecodes stay tiny.
 */
public final class ObsidianScorpioidAnimations {

	private ObsidianScorpioidAnimations() {}

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
	public static final AnimationDefinition PINCER_CLAMP = PincerClampAnimBuilder.create();
	public static final AnimationDefinition pincer_clamp = PINCER_CLAMP;
	public static final AnimationDefinition HURT = HurtAnimBuilder.create();
	public static final AnimationDefinition hurt = HURT;
	public static final AnimationDefinition DEATH = DeathAnimBuilder.create();
	public static final AnimationDefinition death = DEATH;

	private static final class IdleAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("thorax_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, -0.536F, 0.0F),
				rotL(0.4F, 0.0F, -0.99F, 0.0F),
				rotL(0.6F, 0.0F, -1.293F, 0.0F),
				rotL(0.8F, 0.0F, -1.4F, 0.0F),
				rotL(1.0F, 0.0F, -1.293F, 0.0F),
				rotL(1.2F, 0.0F, -0.99F, 0.0F),
				rotL(1.4F, 0.0F, -0.536F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.0F, 0.536F, 0.0F),
				rotL(2.0F, 0.0F, 0.99F, 0.0F),
				rotL(2.2F, 0.0F, 1.293F, 0.0F),
				rotL(2.4F, 0.0F, 1.4F, 0.0F),
				rotL(2.6F, 0.0F, 1.293F, 0.0F),
				rotL(2.8F, 0.0F, 0.99F, 0.0F),
				rotL(3.0F, 0.0F, 0.536F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.857F, -1.199F, 0.0F),
				rotL(0.2F, -0.827F, -1.947F, 0.0F),
				rotL(0.4F, -2.026F, -2.399F, 0.0F),
				rotL(0.6F, -2.039F, -2.486F, 0.0F),
				rotL(0.8F, -0.857F, -2.194F, 0.0F),
				rotL(1.0F, 0.827F, -1.568F, 0.0F),
				rotL(1.2F, 2.026F, -0.704F, 0.0F),
				rotL(1.4F, 2.039F, 0.268F, 0.0F),
				rotL(1.6F, 0.857F, 1.199F, 0.0F),
				rotL(1.8F, -0.827F, 1.947F, 0.0F),
				rotL(2.0F, -2.026F, 2.399F, 0.0F),
				rotL(2.2F, -2.039F, 2.486F, 0.0F),
				rotL(2.4F, -0.857F, 2.194F, 0.0F),
				rotL(2.6F, 0.827F, 1.568F, 0.0F),
				rotL(2.8F, 2.026F, 0.704F, 0.0F),
				rotL(3.0F, 2.039F, -0.268F, 0.0F),
				rotL(3.2F, 0.857F, -1.199F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("fang_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, -1.034F),
				rotL(0.2F, -2.121F, 0.0F, -3.096F),
				rotL(0.4F, -3.0F, 0.0F, -3.344F),
				rotL(0.6F, -2.121F, 0.0F, -1.633F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 2.121F, 0.0F, 0.0F),
				rotL(1.2F, 3.0F, 0.0F, 0.0F),
				rotL(1.4F, 2.121F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, -1.034F),
				rotL(1.8F, -2.121F, 0.0F, -3.096F),
				rotL(2.0F, -3.0F, 0.0F, -3.344F),
				rotL(2.2F, -2.121F, 0.0F, -1.633F),
				rotL(2.4F, 0.0F, 0.0F, 0.0F),
				rotL(2.6F, 2.121F, 0.0F, 0.0F),
				rotL(2.8F, 3.0F, 0.0F, 0.0F),
				rotL(3.0F, 2.121F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, -1.034F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("fang_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.152F, 0.0F, 3.119F),
				rotL(0.2F, -3.0F, 0.0F, 3.328F),
				rotL(0.4F, -2.09F, 0.0F, 1.588F),
				rotL(0.6F, 0.044F, 0.0F, 0.0F),
				rotL(0.8F, 2.152F, 0.0F, 0.0F),
				rotL(1.0F, 3.0F, 0.0F, 0.0F),
				rotL(1.2F, 2.09F, 0.0F, 0.0F),
				rotL(1.4F, -0.044F, 0.0F, 1.083F),
				rotL(1.6F, -2.152F, 0.0F, 3.119F),
				rotL(1.8F, -3.0F, 0.0F, 3.328F),
				rotL(2.0F, -2.09F, 0.0F, 1.588F),
				rotL(2.2F, 0.044F, 0.0F, 0.0F),
				rotL(2.4F, 2.152F, 0.0F, 0.0F),
				rotL(2.6F, 3.0F, 0.0F, 0.0F),
				rotL(2.8F, 2.09F, 0.0F, 0.0F),
				rotL(3.0F, -0.044F, 0.0F, 1.083F),
				rotL(3.2F, -2.152F, 0.0F, 3.119F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("pedipalp_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -0.497F, 0.0F, 0.0F),
				rotL(0.2F, -1.397F, -1.148F, 0.0F),
				rotL(0.4F, -2.084F, -2.121F, 0.0F),
				rotL(0.6F, -2.454F, -2.772F, 0.0F),
				rotL(0.8F, -2.45F, -3.0F, 0.0F),
				rotL(1.0F, -2.074F, -2.772F, 0.0F),
				rotL(1.2F, -1.381F, -2.121F, 0.0F),
				rotL(1.4F, -0.479F, -1.148F, 0.0F),
				rotL(1.6F, 0.497F, 0.0F, 0.0F),
				rotL(1.8F, 1.397F, 1.148F, 0.0F),
				rotL(2.0F, 2.084F, 2.121F, 0.0F),
				rotL(2.2F, 2.454F, 2.772F, 0.0F),
				rotL(2.4F, 2.45F, 3.0F, 0.0F),
				rotL(2.6F, 2.074F, 2.772F, 0.0F),
				rotL(2.8F, 1.381F, 2.121F, 0.0F),
				rotL(3.0F, 0.479F, 1.148F, 0.0F),
				rotL(3.2F, -0.497F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("claw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 1.199F, 0.0F),
				rotL(0.2F, -1.414F, 0.268F, 0.0F),
				rotL(0.4F, -2.0F, -0.704F, 0.0F),
				rotL(0.6F, -1.414F, -1.568F, 0.0F),
				rotL(0.8F, 0.0F, -2.194F, 0.0F),
				rotL(1.0F, 1.414F, -2.486F, 0.0F),
				rotL(1.2F, 2.0F, -2.399F, 0.0F),
				rotL(1.4F, 1.414F, -1.947F, 0.0F),
				rotL(1.6F, 0.0F, -1.199F, 0.0F),
				rotL(1.8F, -1.414F, -0.268F, 0.0F),
				rotL(2.0F, -2.0F, 0.704F, 0.0F),
				rotL(2.2F, -1.414F, 1.568F, 0.0F),
				rotL(2.4F, 0.0F, 2.194F, 0.0F),
				rotL(2.6F, 1.414F, 2.486F, 0.0F),
				rotL(2.8F, 2.0F, 2.399F, 0.0F),
				rotL(3.0F, 1.414F, 1.947F, 0.0F),
				rotL(3.2F, 0.0F, 1.199F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("pincer_lower_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 2.0F, 0.0F, 0.0F),
				rotL(0.2F, -1.499F, 0.0F, 0.0F),
				rotL(0.4F, -5.165F, 0.0F, 0.0F),
				rotL(0.6F, -4.634F, 0.0F, 0.0F),
				rotL(0.8F, -0.216F, 0.0F, 0.0F),
				rotL(1.0F, 2.0F, 0.0F, 0.0F),
				rotL(1.2F, 2.0F, 0.0F, 0.0F),
				rotL(1.4F, 2.0F, 0.0F, 0.0F),
				rotL(1.6F, 2.0F, 0.0F, 0.0F),
				rotL(1.8F, -1.499F, 0.0F, 0.0F),
				rotL(2.0F, -5.165F, 0.0F, 0.0F),
				rotL(2.2F, -4.634F, 0.0F, 0.0F),
				rotL(2.4F, -0.216F, 0.0F, 0.0F),
				rotL(2.6F, 2.0F, 0.0F, 0.0F),
				rotL(2.8F, 2.0F, 0.0F, 0.0F),
				rotL(3.0F, 2.0F, 0.0F, 0.0F),
				rotL(3.2F, 2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("pedipalp_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.499F, 2.152F, 0.0F),
				rotL(0.2F, -2.281F, 2.788F, 0.0F),
				rotL(0.4F, -1.715F, 3.0F, 0.0F),
				rotL(0.6F, -0.889F, 2.755F, 0.0F),
				rotL(0.8F, 0.073F, 2.09F, 0.0F),
				rotL(1.0F, 1.024F, 1.107F, 0.0F),
				rotL(1.2F, 1.819F, -0.044F, 0.0F),
				rotL(1.4F, 2.337F, -1.188F, 0.0F),
				rotL(1.6F, 2.499F, -2.152F, 0.0F),
				rotL(1.8F, 2.281F, -2.788F, 0.0F),
				rotL(2.0F, 1.715F, -3.0F, 0.0F),
				rotL(2.2F, 0.889F, -2.755F, 0.0F),
				rotL(2.4F, -0.073F, -2.09F, 0.0F),
				rotL(2.6F, -1.024F, -1.107F, 0.0F),
				rotL(2.8F, -1.819F, 0.044F, 0.0F),
				rotL(3.0F, -2.337F, 1.188F, 0.0F),
				rotL(3.2F, -2.499F, 2.152F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("claw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.864F, 0.739F, 0.0F),
				rotL(0.2F, -1.831F, 1.597F, 0.0F),
				rotL(0.4F, -0.725F, 2.211F, 0.0F),
				rotL(0.6F, 0.806F, 2.489F, 0.0F),
				rotL(0.8F, 1.864F, 2.388F, 0.0F),
				rotL(1.0F, 1.831F, 1.924F, 0.0F),
				rotL(1.2F, 0.725F, 1.166F, 0.0F),
				rotL(1.4F, -0.806F, 0.231F, 0.0F),
				rotL(1.6F, -1.864F, -0.739F, 0.0F),
				rotL(1.8F, -1.831F, -1.597F, 0.0F),
				rotL(2.0F, -0.725F, -2.211F, 0.0F),
				rotL(2.2F, 0.806F, -2.489F, 0.0F),
				rotL(2.4F, 1.864F, -2.388F, 0.0F),
				rotL(2.6F, 1.831F, -1.924F, 0.0F),
				rotL(2.8F, 0.725F, -1.166F, 0.0F),
				rotL(3.0F, -0.806F, -0.231F, 0.0F),
				rotL(3.2F, -1.864F, 0.739F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("pincer_lower_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -5.391F, 0.0F, 0.0F),
				rotL(0.2F, -4.128F, 0.0F, 0.0F),
				rotL(0.4F, 0.725F, 0.0F, 0.0F),
				rotL(0.6F, 2.0F, 0.0F, 0.0F),
				rotL(0.8F, 2.0F, 0.0F, 0.0F),
				rotL(1.0F, 2.0F, 0.0F, 0.0F),
				rotL(1.2F, 2.0F, 0.0F, 0.0F),
				rotL(1.4F, -2.325F, 0.0F, 0.0F),
				rotL(1.6F, -5.391F, 0.0F, 0.0F),
				rotL(1.8F, -4.128F, 0.0F, 0.0F),
				rotL(2.0F, 0.725F, 0.0F, 0.0F),
				rotL(2.2F, 2.0F, 0.0F, 0.0F),
				rotL(2.4F, 2.0F, 0.0F, 0.0F),
				rotL(2.6F, 2.0F, 0.0F, 0.0F),
				rotL(2.8F, 2.0F, 0.0F, 0.0F),
				rotL(3.0F, -2.325F, 0.0F, 0.0F),
				rotL(3.2F, -5.391F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("abdomen_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, -0.689F, 0.0F, 0.0F),
				rotL(0.4F, -1.273F, 0.0F, 0.0F),
				rotL(0.6F, -1.663F, 0.0F, 0.0F),
				rotL(0.8F, -1.8F, 0.0F, 0.0F),
				rotL(1.0F, -1.663F, 0.0F, 0.0F),
				rotL(1.2F, -1.273F, 0.0F, 0.0F),
				rotL(1.4F, -0.689F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.689F, 0.0F, 0.0F),
				rotL(2.0F, 1.273F, 0.0F, 0.0F),
				rotL(2.2F, 1.663F, 0.0F, 0.0F),
				rotL(2.4F, 1.8F, 0.0F, 0.0F),
				rotL(2.6F, 1.663F, 0.0F, 0.0F),
				rotL(2.8F, 1.273F, 0.0F, 0.0F),
				rotL(3.0F, 0.689F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.499F, 1.305F, 1.644F),
				rotL(0.2F, -1.017F, 0.087F, 1.799F),
				rotL(0.4F, -1.937F, -1.143F, 1.681F),
				rotL(0.6F, -1.722F, -2.2F, 1.307F),
				rotL(0.8F, -0.499F, -2.922F, 0.734F),
				rotL(1.0F, 1.017F, -3.199F, 0.049F),
				rotL(1.2F, 1.937F, -2.989F, -0.643F),
				rotL(1.4F, 1.722F, -2.324F, -1.238F),
				rotL(1.6F, 0.499F, -1.305F, -1.644F),
				rotL(1.8F, -1.017F, -0.087F, -1.799F),
				rotL(2.0F, -1.937F, 1.143F, -1.681F),
				rotL(2.2F, -1.722F, 2.2F, -1.307F),
				rotL(2.4F, -0.499F, 2.922F, -0.734F),
				rotL(2.6F, 1.017F, 3.199F, -0.049F),
				rotL(2.8F, 1.937F, 2.989F, 0.643F),
				rotL(3.0F, 1.722F, 2.324F, 1.238F),
				rotL(3.2F, 0.499F, 1.305F, 1.644F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.966F, 2.383F, 1.201F),
				rotL(0.2F, -0.555F, 1.384F, 1.623F),
				rotL(0.4F, -1.751F, 0.175F, 1.797F),
				rotL(0.6F, -1.921F, -1.061F, 1.698F),
				rotL(0.8F, -0.966F, -2.136F, 1.34F),
				rotL(1.0F, 0.555F, -2.885F, 0.779F),
				rotL(1.2F, 1.751F, -3.195F, 0.098F),
				rotL(1.4F, 1.921F, -3.019F, -0.597F),
				rotL(1.6F, 0.966F, -2.383F, -1.201F),
				rotL(1.8F, -0.555F, -1.384F, -1.623F),
				rotL(2.0F, -1.751F, -0.175F, -1.797F),
				rotL(2.2F, -1.921F, 1.061F, -1.698F),
				rotL(2.4F, -0.966F, 2.136F, -1.34F),
				rotL(2.6F, 0.555F, 2.885F, -0.779F),
				rotL(2.8F, 1.751F, 3.195F, -0.098F),
				rotL(3.0F, 1.921F, 3.019F, 0.597F),
				rotL(3.2F, 0.966F, 2.383F, 1.201F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.372F, 3.047F, 0.55F),
				rotL(0.2F, -0.059F, 2.44F, 1.164F),
				rotL(0.4F, -1.455F, 1.462F, 1.601F),
				rotL(0.6F, -1.999F, 0.262F, 1.794F),
				rotL(0.8F, -1.372F, -0.979F, 1.714F),
				rotL(1.0F, 0.059F, -2.07F, 1.373F),
				rotL(1.2F, 1.455F, -2.846F, 0.823F),
				rotL(1.4F, 1.999F, -3.189F, 0.147F),
				rotL(1.6F, 1.372F, -3.047F, -0.55F),
				rotL(1.8F, -0.059F, -2.44F, -1.164F),
				rotL(2.0F, -1.455F, -1.462F, -1.601F),
				rotL(2.2F, -1.999F, -0.262F, -1.794F),
				rotL(2.4F, -1.372F, 0.979F, -1.714F),
				rotL(2.6F, 0.059F, 2.07F, -1.373F),
				rotL(2.8F, 1.455F, 2.846F, -0.823F),
				rotL(3.0F, 1.999F, 3.189F, -0.147F),
				rotL(3.2F, 1.372F, 3.047F, 0.55F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.692F, 3.181F, -0.196F),
				rotL(0.2F, 0.442F, 3.072F, 0.503F),
				rotL(0.4F, -1.067F, 2.496F, 1.126F),
				rotL(0.6F, -1.951F, 1.54F, 1.578F),
				rotL(0.8F, -1.692F, 0.349F, 1.789F),
				rotL(1.0F, -0.442F, -0.895F, 1.728F),
				rotL(1.2F, 1.067F, -2.003F, 1.404F),
				rotL(1.4F, 1.951F, -2.805F, 0.866F),
				rotL(1.6F, 1.692F, -3.181F, 0.196F),
				rotL(1.8F, 0.442F, -3.072F, -0.503F),
				rotL(2.0F, -1.067F, -2.496F, -1.126F),
				rotL(2.2F, -1.951F, -1.54F, -1.578F),
				rotL(2.4F, -1.692F, -0.349F, -1.789F),
				rotL(2.6F, -0.442F, 0.895F, -1.728F),
				rotL(2.8F, 1.067F, 2.003F, -1.404F),
				rotL(3.0F, 1.951F, 2.805F, -0.866F),
				rotL(3.2F, 1.692F, 3.181F, -0.196F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.904F, 2.762F, -0.909F),
				rotL(0.2F, 0.914F, 3.17F, -0.245F),
				rotL(0.4F, -0.612F, 3.096F, 0.456F),
				rotL(0.6F, -1.779F, 2.55F, 1.088F),
				rotL(0.8F, -1.904F, 1.616F, 1.554F),
				rotL(1.0F, -0.914F, 0.435F, 1.783F),
				rotL(1.2F, 0.612F, -0.811F, 1.741F),
				rotL(1.4F, 1.779F, -1.934F, 1.434F),
				rotL(1.6F, 1.904F, -2.762F, 0.909F),
				rotL(1.8F, 0.914F, -3.17F, 0.245F),
				rotL(2.0F, -0.612F, -3.096F, -0.456F),
				rotL(2.2F, -1.779F, -2.55F, -1.088F),
				rotL(2.4F, -1.904F, -1.616F, -1.554F),
				rotL(2.6F, -0.914F, -0.435F, -1.783F),
				rotL(2.8F, 0.612F, 0.811F, -1.741F),
				rotL(3.0F, 1.779F, 1.934F, -1.434F),
				rotL(3.2F, 1.904F, 2.762F, -0.909F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.997F, 1.863F, -1.463F),
				rotL(0.2F, 1.329F, 2.717F, -0.951F),
				rotL(0.4F, -0.118F, 3.157F, -0.294F),
				rotL(0.6F, -1.495F, 3.117F, 0.408F),
				rotL(0.8F, -1.997F, 2.601F, 1.048F),
				rotL(1.0F, -1.329F, 1.69F, 1.528F),
				rotL(1.2F, 0.118F, 0.522F, 1.776F),
				rotL(1.4F, 1.495F, -0.726F, 1.753F),
				rotL(1.6F, 1.997F, -1.863F, 1.463F),
				rotL(1.8F, 1.329F, -2.717F, 0.951F),
				rotL(2.0F, -0.118F, -3.157F, 0.294F),
				rotL(2.2F, -1.495F, -3.117F, -0.408F),
				rotL(2.4F, -1.997F, -2.601F, -1.048F),
				rotL(2.6F, -1.329F, -1.69F, -1.528F),
				rotL(2.8F, 0.118F, -0.522F, -1.776F),
				rotL(3.0F, 1.495F, 0.726F, -1.753F),
				rotL(3.2F, 1.997F, 1.863F, -1.463F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("stinger_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.638F, 1.005F, 0.0F),
				rotL(0.2F, 4.445F, 2.01F, 0.0F),
				rotL(0.4F, 2.648F, 2.709F, 0.0F),
				rotL(0.6F, -0.7F, 2.996F, 0.0F),
				rotL(0.8F, -3.638F, 2.827F, 0.0F),
				rotL(1.0F, -4.445F, 2.227F, 0.0F),
				rotL(1.2F, -2.648F, 1.288F, 0.0F),
				rotL(1.4F, 0.7F, 0.153F, 0.0F),
				rotL(1.6F, 3.638F, -1.005F, 0.0F),
				rotL(1.8F, 4.445F, -2.01F, 0.0F),
				rotL(2.0F, 2.648F, -2.709F, 0.0F),
				rotL(2.2F, -0.7F, -2.996F, 0.0F),
				rotL(2.4F, -3.638F, -2.827F, 0.0F),
				rotL(2.6F, -4.445F, -2.227F, 0.0F),
				rotL(2.8F, -2.648F, -1.288F, 0.0F),
				rotL(3.0F, 0.7F, -0.153F, 0.0F),
				rotL(3.2F, 3.638F, 1.005F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("hip_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("femur_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("hip_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("femur_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("hip_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("femur_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("hip_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("femur_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("hip_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("femur_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("hip_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("femur_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
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
			return b.build();
		}
	}

	private static final class WalkAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("hip_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 13.5F, 0.0F),
				rotL(0.1F, 0.0F, 12.472F, 0.0F),
				rotL(0.2F, 0.0F, 9.546F, 0.0F),
				rotL(0.3F, 0.0F, 5.166F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, -5.166F, 0.0F),
				rotL(0.6F, 0.0F, -9.546F, 0.0F),
				rotL(0.7F, 0.0F, -12.472F, 0.0F),
				rotL(0.8F, 0.0F, -13.5F, 0.0F),
				rotL(0.9F, 0.0F, -10.125F, 0.0F),
				rotL(1.0F, 0.0F, -6.75F, 0.0F),
				rotL(1.1F, 0.0F, -3.375F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 3.375F, 0.0F),
				rotL(1.4F, 0.0F, 6.75F, 0.0F),
				rotL(1.5F, 0.0F, 10.125F, 0.0F),
				rotL(1.6F, 0.0F, 13.5F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("femur_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 5.74F),
				rotL(0.2F, 0.0F, 0.0F, 10.607F),
				rotL(0.3F, 0.0F, 0.0F, 13.858F),
				rotL(0.4F, 0.0F, 0.0F, 15.0F),
				rotL(0.5F, 0.0F, 0.0F, 13.858F),
				rotL(0.6F, 0.0F, 0.0F, 10.607F),
				rotL(0.7F, 0.0F, 0.0F, 5.74F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, -2.679F),
				rotL(0.2F, 0.0F, 0.0F, -4.95F),
				rotL(0.3F, 0.0F, 0.0F, -6.467F),
				rotL(0.4F, 0.0F, 0.0F, -7.0F),
				rotL(0.5F, 0.0F, 0.0F, -6.467F),
				rotL(0.6F, 0.0F, 0.0F, -4.95F),
				rotL(0.7F, 0.0F, 0.0F, -2.679F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, -1.531F),
				rotL(0.2F, 0.0F, 0.0F, -2.828F),
				rotL(0.3F, 0.0F, 0.0F, -3.696F),
				rotL(0.4F, 0.0F, 0.0F, -4.0F),
				rotL(0.5F, 0.0F, 0.0F, -3.696F),
				rotL(0.6F, 0.0F, 0.0F, -2.828F),
				rotL(0.7F, 0.0F, 0.0F, -1.531F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("hip_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, -14.5F, 0.0F),
				rotL(0.1F, 0.0F, -13.396F, 0.0F),
				rotL(0.2F, 0.0F, -10.253F, 0.0F),
				rotL(0.3F, 0.0F, -5.549F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, 5.549F, 0.0F),
				rotL(0.6F, 0.0F, 10.253F, 0.0F),
				rotL(0.7F, 0.0F, 13.396F, 0.0F),
				rotL(0.8F, 0.0F, 14.5F, 0.0F),
				rotL(0.9F, 0.0F, 10.875F, 0.0F),
				rotL(1.0F, 0.0F, 7.25F, 0.0F),
				rotL(1.1F, 0.0F, 3.625F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, -3.625F, 0.0F),
				rotL(1.4F, 0.0F, -7.25F, 0.0F),
				rotL(1.5F, 0.0F, -10.875F, 0.0F),
				rotL(1.6F, 0.0F, -14.5F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("femur_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, -5.74F),
				rotL(0.2F, 0.0F, 0.0F, -10.607F),
				rotL(0.3F, 0.0F, 0.0F, -13.858F),
				rotL(0.4F, 0.0F, 0.0F, -15.0F),
				rotL(0.5F, 0.0F, 0.0F, -13.858F),
				rotL(0.6F, 0.0F, 0.0F, -10.607F),
				rotL(0.7F, 0.0F, 0.0F, -5.74F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 2.679F),
				rotL(0.2F, 0.0F, 0.0F, 4.95F),
				rotL(0.3F, 0.0F, 0.0F, 6.467F),
				rotL(0.4F, 0.0F, 0.0F, 7.0F),
				rotL(0.5F, 0.0F, 0.0F, 6.467F),
				rotL(0.6F, 0.0F, 0.0F, 4.95F),
				rotL(0.7F, 0.0F, 0.0F, 2.679F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 1.531F),
				rotL(0.2F, 0.0F, 0.0F, 2.828F),
				rotL(0.3F, 0.0F, 0.0F, 3.696F),
				rotL(0.4F, 0.0F, 0.0F, 4.0F),
				rotL(0.5F, 0.0F, 0.0F, 3.696F),
				rotL(0.6F, 0.0F, 0.0F, 2.828F),
				rotL(0.7F, 0.0F, 0.0F, 1.531F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("hip_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 13.5F, 0.0F),
				rotL(0.1F, 0.0F, 12.472F, 0.0F),
				rotL(0.2F, 0.0F, 9.546F, 0.0F),
				rotL(0.3F, 0.0F, 5.166F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, -5.166F, 0.0F),
				rotL(0.6F, 0.0F, -9.546F, 0.0F),
				rotL(0.7F, 0.0F, -12.472F, 0.0F),
				rotL(0.8F, 0.0F, -13.5F, 0.0F),
				rotL(0.9F, 0.0F, -10.125F, 0.0F),
				rotL(1.0F, 0.0F, -6.75F, 0.0F),
				rotL(1.1F, 0.0F, -3.375F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 3.375F, 0.0F),
				rotL(1.4F, 0.0F, 6.75F, 0.0F),
				rotL(1.5F, 0.0F, 10.125F, 0.0F),
				rotL(1.6F, 0.0F, 13.5F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("femur_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 5.74F),
				rotL(0.2F, 0.0F, 0.0F, 10.607F),
				rotL(0.3F, 0.0F, 0.0F, 13.858F),
				rotL(0.4F, 0.0F, 0.0F, 15.0F),
				rotL(0.5F, 0.0F, 0.0F, 13.858F),
				rotL(0.6F, 0.0F, 0.0F, 10.607F),
				rotL(0.7F, 0.0F, 0.0F, 5.74F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, -2.679F),
				rotL(0.2F, 0.0F, 0.0F, -4.95F),
				rotL(0.3F, 0.0F, 0.0F, -6.467F),
				rotL(0.4F, 0.0F, 0.0F, -7.0F),
				rotL(0.5F, 0.0F, 0.0F, -6.467F),
				rotL(0.6F, 0.0F, 0.0F, -4.95F),
				rotL(0.7F, 0.0F, 0.0F, -2.679F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, -1.531F),
				rotL(0.2F, 0.0F, 0.0F, -2.828F),
				rotL(0.3F, 0.0F, 0.0F, -3.696F),
				rotL(0.4F, 0.0F, 0.0F, -4.0F),
				rotL(0.5F, 0.0F, 0.0F, -3.696F),
				rotL(0.6F, 0.0F, 0.0F, -2.828F),
				rotL(0.7F, 0.0F, 0.0F, -1.531F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 0.0F),
				rotL(1.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.1F, 0.0F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hip_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 13.5F, 0.0F),
				rotL(0.1F, 0.0F, 10.125F, 0.0F),
				rotL(0.2F, 0.0F, 6.75F, 0.0F),
				rotL(0.3F, 0.0F, 3.375F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, -3.375F, 0.0F),
				rotL(0.6F, 0.0F, -6.75F, 0.0F),
				rotL(0.7F, 0.0F, -10.125F, 0.0F),
				rotL(0.8F, 0.0F, -13.5F, 0.0F),
				rotL(0.9F, 0.0F, -12.472F, 0.0F),
				rotL(1.0F, 0.0F, -9.546F, 0.0F),
				rotL(1.1F, 0.0F, -5.166F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 5.166F, 0.0F),
				rotL(1.4F, 0.0F, 9.546F, 0.0F),
				rotL(1.5F, 0.0F, 12.472F, 0.0F),
				rotL(1.6F, 0.0F, 13.5F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("femur_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, -5.74F),
				rotL(1.0F, 0.0F, 0.0F, -10.607F),
				rotL(1.1F, 0.0F, 0.0F, -13.858F),
				rotL(1.2F, 0.0F, 0.0F, -15.0F),
				rotL(1.3F, 0.0F, 0.0F, -13.858F),
				rotL(1.4F, 0.0F, 0.0F, -10.607F),
				rotL(1.5F, 0.0F, 0.0F, -5.74F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 2.679F),
				rotL(1.0F, 0.0F, 0.0F, 4.95F),
				rotL(1.1F, 0.0F, 0.0F, 6.467F),
				rotL(1.2F, 0.0F, 0.0F, 7.0F),
				rotL(1.3F, 0.0F, 0.0F, 6.467F),
				rotL(1.4F, 0.0F, 0.0F, 4.95F),
				rotL(1.5F, 0.0F, 0.0F, 2.679F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 1.531F),
				rotL(1.0F, 0.0F, 0.0F, 2.828F),
				rotL(1.1F, 0.0F, 0.0F, 3.696F),
				rotL(1.2F, 0.0F, 0.0F, 4.0F),
				rotL(1.3F, 0.0F, 0.0F, 3.696F),
				rotL(1.4F, 0.0F, 0.0F, 2.828F),
				rotL(1.5F, 0.0F, 0.0F, 1.531F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("hip_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, -14.5F, 0.0F),
				rotL(0.1F, 0.0F, -10.875F, 0.0F),
				rotL(0.2F, 0.0F, -7.25F, 0.0F),
				rotL(0.3F, 0.0F, -3.625F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, 3.625F, 0.0F),
				rotL(0.6F, 0.0F, 7.25F, 0.0F),
				rotL(0.7F, 0.0F, 10.875F, 0.0F),
				rotL(0.8F, 0.0F, 14.5F, 0.0F),
				rotL(0.9F, 0.0F, 13.396F, 0.0F),
				rotL(1.0F, 0.0F, 10.253F, 0.0F),
				rotL(1.1F, 0.0F, 5.549F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, -5.549F, 0.0F),
				rotL(1.4F, 0.0F, -10.253F, 0.0F),
				rotL(1.5F, 0.0F, -13.396F, 0.0F),
				rotL(1.6F, 0.0F, -14.5F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("femur_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 5.74F),
				rotL(1.0F, 0.0F, 0.0F, 10.607F),
				rotL(1.1F, 0.0F, 0.0F, 13.858F),
				rotL(1.2F, 0.0F, 0.0F, 15.0F),
				rotL(1.3F, 0.0F, 0.0F, 13.858F),
				rotL(1.4F, 0.0F, 0.0F, 10.607F),
				rotL(1.5F, 0.0F, 0.0F, 5.74F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, -2.679F),
				rotL(1.0F, 0.0F, 0.0F, -4.95F),
				rotL(1.1F, 0.0F, 0.0F, -6.467F),
				rotL(1.2F, 0.0F, 0.0F, -7.0F),
				rotL(1.3F, 0.0F, 0.0F, -6.467F),
				rotL(1.4F, 0.0F, 0.0F, -4.95F),
				rotL(1.5F, 0.0F, 0.0F, -2.679F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, -1.531F),
				rotL(1.0F, 0.0F, 0.0F, -2.828F),
				rotL(1.1F, 0.0F, 0.0F, -3.696F),
				rotL(1.2F, 0.0F, 0.0F, -4.0F),
				rotL(1.3F, 0.0F, 0.0F, -3.696F),
				rotL(1.4F, 0.0F, 0.0F, -2.828F),
				rotL(1.5F, 0.0F, 0.0F, -1.531F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hip_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 13.5F, 0.0F),
				rotL(0.1F, 0.0F, 10.125F, 0.0F),
				rotL(0.2F, 0.0F, 6.75F, 0.0F),
				rotL(0.3F, 0.0F, 3.375F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, -3.375F, 0.0F),
				rotL(0.6F, 0.0F, -6.75F, 0.0F),
				rotL(0.7F, 0.0F, -10.125F, 0.0F),
				rotL(0.8F, 0.0F, -13.5F, 0.0F),
				rotL(0.9F, 0.0F, -12.472F, 0.0F),
				rotL(1.0F, 0.0F, -9.546F, 0.0F),
				rotL(1.1F, 0.0F, -5.166F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 5.166F, 0.0F),
				rotL(1.4F, 0.0F, 9.546F, 0.0F),
				rotL(1.5F, 0.0F, 12.472F, 0.0F),
				rotL(1.6F, 0.0F, 13.5F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("femur_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, -5.74F),
				rotL(1.0F, 0.0F, 0.0F, -10.607F),
				rotL(1.1F, 0.0F, 0.0F, -13.858F),
				rotL(1.2F, 0.0F, 0.0F, -15.0F),
				rotL(1.3F, 0.0F, 0.0F, -13.858F),
				rotL(1.4F, 0.0F, 0.0F, -10.607F),
				rotL(1.5F, 0.0F, 0.0F, -5.74F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 2.679F),
				rotL(1.0F, 0.0F, 0.0F, 4.95F),
				rotL(1.1F, 0.0F, 0.0F, 6.467F),
				rotL(1.2F, 0.0F, 0.0F, 7.0F),
				rotL(1.3F, 0.0F, 0.0F, 6.467F),
				rotL(1.4F, 0.0F, 0.0F, 4.95F),
				rotL(1.5F, 0.0F, 0.0F, 2.679F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, 0.0F, 1.531F),
				rotL(1.0F, 0.0F, 0.0F, 2.828F),
				rotL(1.1F, 0.0F, 0.0F, 3.696F),
				rotL(1.2F, 0.0F, 0.0F, 4.0F),
				rotL(1.3F, 0.0F, 0.0F, 3.696F),
				rotL(1.4F, 0.0F, 0.0F, 2.828F),
				rotL(1.5F, 0.0F, 0.0F, 1.531F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.1F, 0.0F, 0.0F, 0.0F),
				posL(0.2F, 0.0F, 0.0F, 0.0F),
				posL(0.3F, 0.0F, 0.0F, 0.0F),
				posL(0.4F, 0.0F, 0.0F, 0.0F),
				posL(0.5F, 0.0F, 0.0F, 0.0F),
				posL(0.6F, 0.0F, 0.0F, 0.0F),
				posL(0.7F, 0.0F, 0.0F, 0.0F),
				posL(0.8F, 0.0F, 0.0F, 0.0F),
				posL(0.9F, 0.0F, 0.0F, 0.0F),
				posL(1.0F, 0.0F, 0.0F, 0.0F),
				posL(1.1F, 0.0F, 0.0F, 0.0F),
				posL(1.2F, 0.0F, 0.0F, 0.0F),
				posL(1.3F, 0.0F, 0.0F, 0.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F),
				posL(1.5F, 0.0F, 0.0F, 0.0F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("thorax_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.995F, 0.0F),
				rotL(0.2F, 0.0F, 1.838F, 0.0F),
				rotL(0.3F, 0.0F, 2.402F, 0.0F),
				rotL(0.4F, 0.0F, 2.6F, 0.0F),
				rotL(0.5F, 0.0F, 2.402F, 0.0F),
				rotL(0.6F, 0.0F, 1.838F, 0.0F),
				rotL(0.7F, 0.0F, 0.995F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 0.0F, -0.995F, 0.0F),
				rotL(1.0F, 0.0F, -1.838F, 0.0F),
				rotL(1.1F, 0.0F, -2.402F, 0.0F),
				rotL(1.2F, 0.0F, -2.6F, 0.0F),
				rotL(1.3F, 0.0F, -2.402F, 0.0F),
				rotL(1.4F, 0.0F, -1.838F, 0.0F),
				rotL(1.5F, 0.0F, -0.995F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, -1.414F, -1.072F, 0.0F),
				rotL(0.2F, -2.0F, -1.98F, 0.0F),
				rotL(0.3F, -1.414F, -2.587F, 0.0F),
				rotL(0.4F, 0.0F, -2.8F, 0.0F),
				rotL(0.5F, 1.414F, -2.587F, 0.0F),
				rotL(0.6F, 2.0F, -1.98F, 0.0F),
				rotL(0.7F, 1.414F, -1.072F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, -1.414F, 1.072F, 0.0F),
				rotL(1.0F, -2.0F, 1.98F, 0.0F),
				rotL(1.1F, -1.414F, 2.587F, 0.0F),
				rotL(1.2F, 0.0F, 2.8F, 0.0F),
				rotL(1.3F, 1.414F, 2.587F, 0.0F),
				rotL(1.4F, 2.0F, 1.98F, 0.0F),
				rotL(1.5F, 1.414F, 1.072F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("fang_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, -2.475F, 0.0F, -1.768F),
				rotL(0.2F, -3.5F, 0.0F, -2.5F),
				rotL(0.3F, -2.475F, 0.0F, -1.768F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 2.475F, 0.0F, 0.0F),
				rotL(0.6F, 3.5F, 0.0F, 0.0F),
				rotL(0.7F, 2.475F, 0.0F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, -2.475F, 0.0F, -1.768F),
				rotL(1.0F, -3.5F, 0.0F, -2.5F),
				rotL(1.1F, -2.475F, 0.0F, -1.768F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 2.475F, 0.0F, 0.0F),
				rotL(1.4F, 3.5F, 0.0F, 0.0F),
				rotL(1.5F, 2.475F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("fang_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 2.475F, 0.0F, 0.0F),
				rotL(0.2F, 3.5F, 0.0F, 0.0F),
				rotL(0.3F, 2.475F, 0.0F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, -2.475F, 0.0F, 1.768F),
				rotL(0.6F, -3.5F, 0.0F, 2.5F),
				rotL(0.7F, -2.475F, 0.0F, 1.768F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 2.475F, 0.0F, 0.0F),
				rotL(1.0F, 3.5F, 0.0F, 0.0F),
				rotL(1.1F, 2.475F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, -2.475F, 0.0F, 1.768F),
				rotL(1.4F, -3.5F, 0.0F, 2.5F),
				rotL(1.5F, -2.475F, 0.0F, 1.768F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("pedipalp_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, -2.121F, -1.913F, 0.0F),
				rotL(0.2F, -3.0F, -3.536F, 0.0F),
				rotL(0.3F, -2.121F, -4.619F, 0.0F),
				rotL(0.4F, 0.0F, -5.0F, 0.0F),
				rotL(0.5F, 2.121F, -4.619F, 0.0F),
				rotL(0.6F, 3.0F, -3.536F, 0.0F),
				rotL(0.7F, 2.121F, -1.913F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, -2.121F, 1.913F, 0.0F),
				rotL(1.0F, -3.0F, 3.536F, 0.0F),
				rotL(1.1F, -2.121F, 4.619F, 0.0F),
				rotL(1.2F, 0.0F, 5.0F, 0.0F),
				rotL(1.3F, 2.121F, 4.619F, 0.0F),
				rotL(1.4F, 3.0F, 3.536F, 0.0F),
				rotL(1.5F, 2.121F, 1.913F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("claw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.5F, 1.363F, 0.0F),
				rotL(0.1F, -1.768F, 0.026F, 0.0F),
				rotL(0.2F, 0.0F, -1.316F, 0.0F),
				rotL(0.3F, 1.768F, -2.457F, 0.0F),
				rotL(0.4F, 2.5F, -3.224F, 0.0F),
				rotL(0.5F, 1.768F, -3.5F, 0.0F),
				rotL(0.6F, 0.0F, -3.243F, 0.0F),
				rotL(0.7F, -1.768F, -2.493F, 0.0F),
				rotL(0.8F, -2.5F, -1.363F, 0.0F),
				rotL(0.9F, -1.768F, -0.026F, 0.0F),
				rotL(1.0F, 0.0F, 1.316F, 0.0F),
				rotL(1.1F, 1.768F, 2.457F, 0.0F),
				rotL(1.2F, 2.5F, 3.224F, 0.0F),
				rotL(1.3F, 1.768F, 3.5F, 0.0F),
				rotL(1.4F, 0.0F, 3.243F, 0.0F),
				rotL(1.5F, -1.768F, 2.493F, 0.0F),
				rotL(1.6F, -2.5F, 1.363F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("pincer_lower_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.0F, 0.0F, 0.0F),
				rotL(0.1F, -5.536F, 0.0F, 0.0F),
				rotL(0.2F, -7.0F, 0.0F, 0.0F),
				rotL(0.3F, -5.536F, 0.0F, 0.0F),
				rotL(0.4F, -2.0F, 0.0F, 0.0F),
				rotL(0.5F, 1.536F, 0.0F, 0.0F),
				rotL(0.6F, 3.0F, 0.0F, 0.0F),
				rotL(0.7F, 1.536F, 0.0F, 0.0F),
				rotL(0.8F, -2.0F, 0.0F, 0.0F),
				rotL(0.9F, -5.536F, 0.0F, 0.0F),
				rotL(1.0F, -7.0F, 0.0F, 0.0F),
				rotL(1.1F, -5.536F, 0.0F, 0.0F),
				rotL(1.2F, -2.0F, 0.0F, 0.0F),
				rotL(1.3F, 1.536F, 0.0F, 0.0F),
				rotL(1.4F, 3.0F, 0.0F, 0.0F),
				rotL(1.5F, 1.536F, 0.0F, 0.0F),
				rotL(1.6F, -2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("pedipalp_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 2.121F, -1.913F, 0.0F),
				rotL(0.2F, 3.0F, -3.536F, 0.0F),
				rotL(0.3F, 2.121F, -4.619F, 0.0F),
				rotL(0.4F, 0.0F, -5.0F, 0.0F),
				rotL(0.5F, -2.121F, -4.619F, 0.0F),
				rotL(0.6F, -3.0F, -3.536F, 0.0F),
				rotL(0.7F, -2.121F, -1.913F, 0.0F),
				rotL(0.8F, 0.0F, 0.0F, 0.0F),
				rotL(0.9F, 2.121F, 1.913F, 0.0F),
				rotL(1.0F, 3.0F, 3.536F, 0.0F),
				rotL(1.1F, 2.121F, 4.619F, 0.0F),
				rotL(1.2F, 0.0F, 5.0F, 0.0F),
				rotL(1.3F, -2.121F, 4.619F, 0.0F),
				rotL(1.4F, -3.0F, 3.536F, 0.0F),
				rotL(1.5F, -2.121F, 1.913F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("claw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 2.5F, 1.363F, 0.0F),
				rotL(0.1F, 1.768F, 0.026F, 0.0F),
				rotL(0.2F, 0.0F, -1.316F, 0.0F),
				rotL(0.3F, -1.768F, -2.457F, 0.0F),
				rotL(0.4F, -2.5F, -3.224F, 0.0F),
				rotL(0.5F, -1.768F, -3.5F, 0.0F),
				rotL(0.6F, 0.0F, -3.243F, 0.0F),
				rotL(0.7F, 1.768F, -2.493F, 0.0F),
				rotL(0.8F, 2.5F, -1.363F, 0.0F),
				rotL(0.9F, 1.768F, -0.026F, 0.0F),
				rotL(1.0F, 0.0F, 1.316F, 0.0F),
				rotL(1.1F, -1.768F, 2.457F, 0.0F),
				rotL(1.2F, -2.5F, 3.224F, 0.0F),
				rotL(1.3F, -1.768F, 3.5F, 0.0F),
				rotL(1.4F, 0.0F, 3.243F, 0.0F),
				rotL(1.5F, 1.768F, 2.493F, 0.0F),
				rotL(1.6F, 2.5F, 1.363F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("pincer_lower_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.0F, 0.0F, 0.0F),
				rotL(0.1F, 1.536F, 0.0F, 0.0F),
				rotL(0.2F, 3.0F, 0.0F, 0.0F),
				rotL(0.3F, 1.536F, 0.0F, 0.0F),
				rotL(0.4F, -2.0F, 0.0F, 0.0F),
				rotL(0.5F, -5.536F, 0.0F, 0.0F),
				rotL(0.6F, -7.0F, 0.0F, 0.0F),
				rotL(0.7F, -5.536F, 0.0F, 0.0F),
				rotL(0.8F, -2.0F, 0.0F, 0.0F),
				rotL(0.9F, 1.536F, 0.0F, 0.0F),
				rotL(1.0F, 3.0F, 0.0F, 0.0F),
				rotL(1.1F, 1.536F, 0.0F, 0.0F),
				rotL(1.2F, -2.0F, 0.0F, 0.0F),
				rotL(1.3F, -5.536F, 0.0F, 0.0F),
				rotL(1.4F, -7.0F, 0.0F, 0.0F),
				rotL(1.5F, -5.536F, 0.0F, 0.0F),
				rotL(1.6F, -2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("abdomen_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.65F, 0.0F, 0.0F),
				rotL(0.1F, -1.026F, 0.0F, 0.0F),
				rotL(0.2F, -2.102F, 0.0F, 0.0F),
				rotL(0.3F, -1.946F, 0.0F, 0.0F),
				rotL(0.4F, -0.65F, 0.0F, 0.0F),
				rotL(0.5F, 1.026F, 0.0F, 0.0F),
				rotL(0.6F, 2.102F, 0.0F, 0.0F),
				rotL(0.7F, 1.946F, 0.0F, 0.0F),
				rotL(0.8F, 0.65F, 0.0F, 0.0F),
				rotL(0.9F, -1.026F, 0.0F, 0.0F),
				rotL(1.0F, -2.102F, 0.0F, 0.0F),
				rotL(1.1F, -1.946F, 0.0F, 0.0F),
				rotL(1.2F, -0.65F, 0.0F, 0.0F),
				rotL(1.3F, 1.026F, 0.0F, 0.0F),
				rotL(1.4F, 2.102F, 0.0F, 0.0F),
				rotL(1.5F, 1.946F, 0.0F, 0.0F),
				rotL(1.6F, 0.65F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.631F, 1.335F, 1.857F),
				rotL(0.1F, -1.191F, -0.046F, 2.0F),
				rotL(0.2F, -2.316F, -1.42F, 1.838F),
				rotL(0.3F, -2.083F, -2.578F, 1.396F),
				rotL(0.4F, -0.631F, -3.343F, 0.742F),
				rotL(0.5F, 1.191F, -3.6F, -0.025F),
				rotL(0.6F, 2.316F, -3.308F, -0.789F),
				rotL(0.7F, 2.083F, -2.513F, -1.432F),
				rotL(0.8F, 0.631F, -1.335F, -1.857F),
				rotL(0.9F, -1.191F, 0.046F, -2.0F),
				rotL(1.0F, -2.316F, 1.42F, -1.838F),
				rotL(1.1F, -2.083F, 2.578F, -1.396F),
				rotL(1.2F, -0.631F, 3.343F, -0.742F),
				rotL(1.3F, 1.191F, 3.6F, 0.025F),
				rotL(1.4F, 2.316F, 3.308F, 0.789F),
				rotL(1.5F, 2.083F, 2.513F, 1.432F),
				rotL(1.6F, 0.631F, 1.335F, 1.857F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.217F, 2.48F, 1.45F),
				rotL(0.1F, -0.602F, 1.293F, 1.867F),
				rotL(0.2F, -2.068F, -0.091F, 1.999F),
				rotL(0.3F, -2.323F, -1.462F, 1.828F),
				rotL(0.4F, -1.217F, -2.609F, 1.378F),
				rotL(0.5F, 0.602F, -3.36F, 0.718F),
				rotL(0.6F, 2.068F, -3.599F, -0.051F),
				rotL(0.7F, 2.323F, -3.29F, -0.812F),
				rotL(0.8F, 1.217F, -2.48F, -1.45F),
				rotL(0.9F, -0.602F, -1.293F, -1.867F),
				rotL(1.0F, -2.068F, 0.091F, -1.999F),
				rotL(1.1F, -2.323F, 1.462F, -1.828F),
				rotL(1.2F, -1.217F, 2.609F, -1.378F),
				rotL(1.3F, 0.602F, 3.36F, -0.718F),
				rotL(1.4F, 2.068F, 3.599F, 0.051F),
				rotL(1.5F, 2.323F, 3.29F, 0.812F),
				rotL(1.6F, 1.217F, 2.48F, 1.45F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.718F, 3.271F, 0.835F),
				rotL(0.1F, 0.03F, 2.447F, 1.467F),
				rotL(0.2F, -1.676F, 1.25F, 1.876F),
				rotL(0.3F, -2.4F, -0.137F, 1.999F),
				rotL(0.4F, -1.718F, -1.503F, 1.817F),
				rotL(0.5F, -0.03F, -2.641F, 1.359F),
				rotL(0.6F, 1.676F, -3.376F, 0.694F),
				rotL(0.7F, 2.4F, -3.597F, -0.076F),
				rotL(0.8F, 1.718F, -3.271F, -0.835F),
				rotL(0.9F, 0.03F, -2.447F, -1.467F),
				rotL(1.0F, -1.676F, -1.25F, -1.876F),
				rotL(1.1F, -2.4F, 0.137F, -1.999F),
				rotL(1.2F, -1.718F, 1.503F, -1.817F),
				rotL(1.3F, -0.03F, 2.641F, -1.359F),
				rotL(1.4F, 1.676F, 3.376F, -0.694F),
				rotL(1.5F, 2.4F, 3.597F, 0.076F),
				rotL(1.6F, 1.718F, 3.271F, 0.835F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 2.098F, 3.595F, 0.102F),
				rotL(0.1F, 0.66F, 3.252F, 0.858F),
				rotL(0.2F, -1.165F, 2.413F, 1.484F),
				rotL(0.3F, -2.307F, 1.207F, 1.884F),
				rotL(0.4F, -2.098F, -0.183F, 1.997F),
				rotL(0.5F, -0.66F, -1.545F, 1.807F),
				rotL(0.6F, 1.165F, -2.672F, 1.341F),
				rotL(0.7F, 2.307F, -3.392F, 0.671F),
				rotL(0.8F, 2.098F, -3.595F, -0.102F),
				rotL(0.9F, 0.66F, -3.252F, -0.858F),
				rotL(1.0F, -1.165F, -2.413F, -1.484F),
				rotL(1.1F, -2.307F, -1.207F, -1.884F),
				rotL(1.2F, -2.098F, 0.183F, -1.997F),
				rotL(1.3F, -0.66F, 1.545F, -1.807F),
				rotL(1.4F, 1.165F, 2.672F, -1.341F),
				rotL(1.5F, 2.307F, 3.392F, -0.671F),
				rotL(1.6F, 2.098F, 3.595F, 0.102F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 2.331F, 3.407F, -0.647F),
				rotL(0.1F, 1.243F, 3.593F, 0.127F),
				rotL(0.2F, -0.572F, 3.232F, 0.881F),
				rotL(0.3F, -2.053F, 2.379F, 1.501F),
				rotL(0.4F, -2.331F, 1.164F, 1.893F),
				rotL(0.5F, -1.243F, -0.228F, 1.996F),
				rotL(0.6F, 0.572F, -1.586F, 1.795F),
				rotL(0.7F, 2.053F, -2.702F, 1.322F),
				rotL(0.8F, 2.331F, -3.407F, 0.647F),
				rotL(0.9F, 1.243F, -3.593F, -0.127F),
				rotL(1.0F, -0.572F, -3.232F, -0.881F),
				rotL(1.1F, -2.053F, -2.379F, -1.501F),
				rotL(1.2F, -2.331F, -1.164F, -1.893F),
				rotL(1.3F, -1.243F, 0.228F, -1.996F),
				rotL(1.4F, 0.572F, 1.586F, -1.795F),
				rotL(1.5F, 2.053F, 2.702F, -1.322F),
				rotL(1.6F, 2.331F, 3.407F, -0.647F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 2.399F, 2.732F, -1.302F),
				rotL(0.1F, 1.739F, 3.421F, -0.622F),
				rotL(0.2F, 0.06F, 3.59F, 0.152F),
				rotL(0.3F, -1.654F, 3.211F, 0.904F),
				rotL(0.4F, -2.399F, 2.344F, 1.518F),
				rotL(0.5F, -1.739F, 1.12F, 1.901F),
				rotL(0.6F, -0.06F, -0.274F, 1.994F),
				rotL(0.7F, 1.654F, -1.627F, 1.784F),
				rotL(0.8F, 2.399F, -2.732F, 1.302F),
				rotL(0.9F, 1.739F, -3.421F, 0.622F),
				rotL(1.0F, 0.06F, -3.59F, -0.152F),
				rotL(1.1F, -1.654F, -3.211F, -0.904F),
				rotL(1.2F, -2.399F, -2.344F, -1.518F),
				rotL(1.3F, -1.739F, -1.12F, -1.901F),
				rotL(1.4F, -0.06F, 0.274F, -1.994F),
				rotL(1.5F, 1.654F, 1.627F, -1.784F),
				rotL(1.6F, 2.399F, 2.732F, -1.302F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("stinger_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.638F, 1.795F, 0.0F),
				rotL(0.1F, 4.445F, 2.579F, 0.0F),
				rotL(0.2F, 2.648F, 2.969F, 0.0F),
				rotL(0.3F, -0.7F, 2.908F, 0.0F),
				rotL(0.4F, -3.638F, 2.403F, 0.0F),
				rotL(0.5F, -4.445F, 1.533F, 0.0F),
				rotL(0.6F, -2.648F, 0.43F, 0.0F),
				rotL(0.7F, 0.7F, -0.739F, 0.0F),
				rotL(0.8F, 3.638F, -1.795F, 0.0F),
				rotL(0.9F, 4.445F, -2.579F, 0.0F),
				rotL(1.0F, 2.648F, -2.969F, 0.0F),
				rotL(1.1F, -0.7F, -2.908F, 0.0F),
				rotL(1.2F, -3.638F, -2.403F, 0.0F),
				rotL(1.3F, -4.445F, -1.533F, 0.0F),
				rotL(1.4F, -2.648F, -0.43F, 0.0F),
				rotL(1.5F, 0.7F, 0.739F, 0.0F),
				rotL(1.6F, 3.638F, 1.795F, 0.0F)
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
			ch_41(b);
			ch_42(b);
			return b.build();
		}
	}

	private static final class AttackAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("hip_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, 0.0F, -7.0F, 0.0F),
				rotL(0.56F, 0.0F, 10.0F, 0.0F),
				rotL(0.72F, 0.0F, 8.0F, 0.0F),
				rotL(1.12F, 0.0F, -2.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("femur_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("hip_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, 0.0F, 7.0F, 0.0F),
				rotL(0.56F, 0.0F, -10.0F, 0.0F),
				rotL(0.72F, 0.0F, -8.0F, 0.0F),
				rotL(1.12F, 0.0F, 2.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("femur_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("hip_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, 0.0F, -7.0F, 0.0F),
				rotL(0.56F, 0.0F, 10.0F, 0.0F),
				rotL(0.72F, 0.0F, 8.0F, 0.0F),
				rotL(1.12F, 0.0F, -2.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("femur_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hip_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, 0.0F, 7.0F, 0.0F),
				rotL(0.56F, 0.0F, -10.0F, 0.0F),
				rotL(0.72F, 0.0F, -8.0F, 0.0F),
				rotL(1.12F, 0.0F, 2.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("femur_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("hip_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, 0.0F, -7.0F, 0.0F),
				rotL(0.56F, 0.0F, 10.0F, 0.0F),
				rotL(0.72F, 0.0F, 8.0F, 0.0F),
				rotL(1.12F, 0.0F, -2.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("femur_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hip_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, 0.0F, 7.0F, 0.0F),
				rotL(0.56F, 0.0F, -10.0F, 0.0F),
				rotL(0.72F, 0.0F, -8.0F, 0.0F),
				rotL(1.12F, 0.0F, 2.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("femur_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.36F, 0.0F, 0.0F, 1.4F),
				posL(0.56F, 0.0F, 0.0F, -2.2F),
				posL(0.72F, 0.0F, 0.0F, -1.8F),
				posL(1.12F, 0.0F, 0.0F, 0.4F),
				posL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("thorax_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -8.0F, 0.0F, 0.0F),
				rotL(0.56F, 8.0F, 0.0F, 0.0F),
				rotL(0.72F, 6.0F, 0.0F, 0.0F),
				rotL(1.1F, -3.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("fang_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -10.0F, 0.0F, 8.0F),
				rotL(0.56F, 8.0F, 0.0F, -8.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("fang_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -10.0F, 0.0F, -8.0F),
				rotL(0.56F, 8.0F, 0.0F, 8.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("pedipalp_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -10.0F, 16.0F, 0.0F),
				rotL(0.56F, 4.0F, 10.0F, 0.0F),
				rotL(1.05F, -3.0F, 3.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("pedipalp_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -10.0F, -16.0F, 0.0F),
				rotL(0.56F, 4.0F, -10.0F, 0.0F),
				rotL(1.05F, -3.0F, -3.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("claw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -8.0F, 12.0F, 0.0F),
				rotL(0.56F, 6.0F, 6.0F, 0.0F),
				rotL(1.12F, -2.0F, 2.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("claw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -8.0F, -12.0F, 0.0F),
				rotL(0.56F, 6.0F, -6.0F, 0.0F),
				rotL(1.12F, -2.0F, -2.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("pincer_lower_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, 14.0F, 0.0F, 0.0F),
				rotL(0.58F, -10.0F, 0.0F, 0.0F),
				rotL(1.1F, 3.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("pincer_lower_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, 14.0F, 0.0F, 0.0F),
				rotL(0.58F, -10.0F, 0.0F, 0.0F),
				rotL(1.1F, 3.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("abdomen_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -10.0F, 0.0F, 0.0F),
				rotL(0.56F, 14.0F, 0.0F, 0.0F),
				rotL(0.7F, 12.0F, 0.0F, 0.0F),
				rotL(1.08F, -5.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.35F, -22.0F, 0.0F, 0.0F),
				rotL(0.56F, 56.0F, 0.0F, 0.0F),
				rotL(0.7F, 50.0F, 0.0F, 0.0F),
				rotL(1.1F, -10.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, -8.0F, 0.0F, 0.0F),
				rotL(0.57F, 16.0F, 0.0F, 0.0F),
				rotL(0.71F, 13.0F, 0.0F, 0.0F),
				rotL(1.12F, -6.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.37F, 6.0F, 0.0F, 0.0F),
				rotL(0.58F, -18.0F, 0.0F, 0.0F),
				rotL(0.72F, -15.0F, 0.0F, 0.0F),
				rotL(1.14F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.38F, 8.0F, 0.0F, 0.0F),
				rotL(0.58F, -22.0F, 0.0F, 0.0F),
				rotL(0.72F, -19.0F, 0.0F, 0.0F),
				rotL(1.16F, 5.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.39F, 10.0F, 0.0F, 0.0F),
				rotL(0.59F, -24.0F, 0.0F, 0.0F),
				rotL(0.73F, -21.0F, 0.0F, 0.0F),
				rotL(1.18F, 5.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 10.0F, 0.0F, 0.0F),
				rotL(0.6F, -22.0F, 0.0F, 0.0F),
				rotL(0.74F, -19.0F, 0.0F, 0.0F),
				rotL(1.2F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("stinger_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 12.0F, 0.0F, 0.0F),
				rotL(0.6F, -22.0F, 0.0F, 0.0F),
				rotL(0.74F, -18.0F, 0.0F, 0.0F),
				rotL(1.22F, -8.0F, 0.0F, 0.0F),
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
			return b.build();
		}
	}

	private static final class PincerClampAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("hip_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, 0.0F, -8.0F, 0.0F),
				rotL(0.54F, 0.0F, 11.0F, 0.0F),
				rotL(0.76F, 0.0F, 9.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("femur_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("hip_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, 0.0F, 8.0F, 0.0F),
				rotL(0.54F, 0.0F, -11.0F, 0.0F),
				rotL(0.76F, 0.0F, -9.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("femur_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("hip_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, 0.0F, -8.0F, 0.0F),
				rotL(0.54F, 0.0F, 11.0F, 0.0F),
				rotL(0.76F, 0.0F, 9.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("femur_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hip_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, 0.0F, 8.0F, 0.0F),
				rotL(0.54F, 0.0F, -11.0F, 0.0F),
				rotL(0.76F, 0.0F, -9.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("femur_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("hip_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, 0.0F, -8.0F, 0.0F),
				rotL(0.54F, 0.0F, 11.0F, 0.0F),
				rotL(0.76F, 0.0F, 9.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("femur_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hip_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, 0.0F, 8.0F, 0.0F),
				rotL(0.54F, 0.0F, -11.0F, 0.0F),
				rotL(0.76F, 0.0F, -9.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("femur_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.34F, 0.0F, 0.0F, 1.5F),
				posL(0.54F, 0.0F, 0.0F, -2.5F),
				posL(0.76F, 0.0F, 0.0F, -2.0F),
				posL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("thorax_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -6.0F, 0.0F, 0.0F),
				rotL(0.54F, 5.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("fang_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -12.0F, 0.0F, 10.0F),
				rotL(0.54F, 6.0F, 0.0F, -14.0F),
				rotL(0.76F, 4.0F, 0.0F, -10.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("fang_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -12.0F, 0.0F, -10.0F),
				rotL(0.54F, 6.0F, 0.0F, 14.0F),
				rotL(0.76F, 4.0F, 0.0F, 10.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("pedipalp_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -12.0F, 24.0F, 0.0F),
				rotL(0.52F, 2.0F, -11.0F, 0.0F),
				rotL(0.76F, 2.0F, -10.0F, 0.0F),
				rotL(1.08F, -3.0F, 4.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("pedipalp_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -12.0F, -24.0F, 0.0F),
				rotL(0.52F, 2.0F, 11.0F, 0.0F),
				rotL(0.76F, 2.0F, 10.0F, 0.0F),
				rotL(1.08F, -3.0F, -4.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("claw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -10.0F, 18.0F, 0.0F),
				rotL(0.54F, 2.0F, -10.0F, 0.0F),
				rotL(0.76F, 2.0F, -9.0F, 0.0F),
				rotL(1.12F, -2.0F, 3.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("claw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -10.0F, -18.0F, 0.0F),
				rotL(0.54F, 2.0F, 10.0F, 0.0F),
				rotL(0.76F, 2.0F, 9.0F, 0.0F),
				rotL(1.12F, -2.0F, -3.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("pincer_lower_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, 16.0F, 0.0F, 0.0F),
				rotL(0.55F, -13.5F, 0.0F, 0.0F),
				rotL(0.78F, -13.5F, 0.0F, 0.0F),
				rotL(1.14F, 4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("pincer_lower_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.36F, 16.0F, 0.0F, 0.0F),
				rotL(0.55F, -13.5F, 0.0F, 0.0F),
				rotL(0.78F, -13.5F, 0.0F, 0.0F),
				rotL(1.14F, 4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("abdomen_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -6.0F, 0.0F, 0.0F),
				rotL(0.54F, 4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -4.0F, 0.0F, 0.0F),
				rotL(0.56F, 6.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -4.0F, 0.0F, 0.0F),
				rotL(0.56F, 6.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -4.0F, 0.0F, 0.0F),
				rotL(0.56F, -4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -4.0F, 0.0F, 0.0F),
				rotL(0.56F, -4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -4.0F, 0.0F, 0.0F),
				rotL(0.56F, -4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, -4.0F, 0.0F, 0.0F),
				rotL(0.56F, -4.0F, 0.0F, 0.0F),
				rotL(1.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("stinger_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.56F, -10.0F, 0.0F, 0.0F),
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
			ch_42(b);
			return b.build();
		}
	}

	private static final class HurtAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("hip_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 0.0F, -8.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("femur_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("hip_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 0.0F, 8.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("femur_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("hip_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 0.0F, -8.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("femur_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hip_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 0.0F, 8.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("femur_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("hip_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 0.0F, -8.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("femur_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hip_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 0.0F, 8.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("femur_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.14F, 0.0F, 0.0F, 1.8F),
				posL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("thorax_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 0.0F, -4.0F, 0.0F),
				rotL(0.34F, 0.0F, 2.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.12F, -10.0F, 5.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("pedipalp_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -16.0F, -14.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("pedipalp_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -16.0F, 14.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("claw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -12.0F, -10.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("claw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -12.0F, 10.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("pincer_lower_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 12.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("pincer_lower_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 12.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("abdomen_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -10.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -6.0F, 5.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -6.0F, -5.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -6.0F, 5.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -6.0F, -5.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -6.0F, 5.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -6.0F, -5.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("stinger_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.16F, -12.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("fang_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("fang_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
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
			return b.build();
		}
	}

	private static final class DeathAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("thorax_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 4.0F, 0.0F),
				rotL(1.15F, 0.0F, -3.0F, 0.0F),
				rotL(1.8F, 0.0F, -3.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -12.0F, 0.0F, 0.0F),
				rotL(0.7F, -16.0F, -3.0F, 0.0F),
				rotL(1.15F, -22.0F, -6.0F, 0.0F),
				rotL(1.8F, -22.0F, -6.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("fang_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, -18.0F, 0.0F, 6.0F),
				rotL(1.15F, -32.0F, 0.0F, 10.0F),
				rotL(1.8F, -32.0F, 0.0F, 10.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("fang_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, -18.0F, 0.0F, -6.0F),
				rotL(1.15F, -32.0F, 0.0F, -10.0F),
				rotL(1.8F, -32.0F, 0.0F, -10.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("hip_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, -6.0F, 0.0F),
				rotL(1.15F, 0.0F, -10.0F, 0.0F),
				rotL(1.8F, 0.0F, -10.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("femur_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, 36.0F),
				rotL(1.8F, 0.0F, 0.0F, 36.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, 8.0F),
				rotL(1.8F, 0.0F, 0.0F, 8.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_1_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, 6.0F),
				rotL(1.8F, 0.0F, 0.0F, 6.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("hip_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 6.0F, 0.0F),
				rotL(1.15F, 0.0F, 10.0F, 0.0F),
				rotL(1.8F, 0.0F, 10.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("femur_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, -36.0F),
				rotL(1.8F, 0.0F, 0.0F, -36.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, -8.0F),
				rotL(1.8F, 0.0F, 0.0F, -8.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_1_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, -6.0F),
				rotL(1.8F, 0.0F, 0.0F, -6.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("hip_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, -6.0F, 0.0F),
				rotL(1.15F, 0.0F, -2.0F, 0.0F),
				rotL(1.8F, 0.0F, -2.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("femur_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, 36.0F),
				rotL(1.8F, 0.0F, 0.0F, 36.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, 8.0F),
				rotL(1.8F, 0.0F, 0.0F, 8.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_2_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, 6.0F),
				rotL(1.8F, 0.0F, 0.0F, 6.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("hip_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 6.0F, 0.0F),
				rotL(1.15F, 0.0F, 2.0F, 0.0F),
				rotL(1.8F, 0.0F, 2.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("femur_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, -36.0F),
				rotL(1.8F, 0.0F, 0.0F, -36.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, -8.0F),
				rotL(1.8F, 0.0F, 0.0F, -8.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_2_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, -6.0F),
				rotL(1.8F, 0.0F, 0.0F, -6.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("hip_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, -6.0F, 0.0F),
				rotL(1.15F, 0.0F, 10.0F, 0.0F),
				rotL(1.8F, 0.0F, 10.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("femur_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, 36.0F),
				rotL(1.8F, 0.0F, 0.0F, 36.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, 8.0F),
				rotL(1.8F, 0.0F, 0.0F, 8.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_3_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, 6.0F),
				rotL(1.8F, 0.0F, 0.0F, 6.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("hip_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 6.0F, 0.0F),
				rotL(1.15F, 0.0F, -10.0F, 0.0F),
				rotL(1.8F, 0.0F, -10.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("femur_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, -36.0F),
				rotL(1.8F, 0.0F, 0.0F, -36.0F)
			));
		}
		private static void ch_26(AnimationDefinition.Builder b) {
			b.addAnimation("tibia_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, -8.0F),
				rotL(1.8F, 0.0F, 0.0F, -8.0F)
			));
		}
		private static void ch_27(AnimationDefinition.Builder b) {
			b.addAnimation("tarsus_3_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 0.0F, 0.0F, -6.0F),
				rotL(1.8F, 0.0F, 0.0F, -6.0F)
			));
		}
		private static void ch_28(AnimationDefinition.Builder b) {
			b.addAnimation("pedipalp_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -14.0F, 14.0F, 0.0F),
				rotL(0.7F, -7.0F, 18.0F, 0.0F),
				rotL(1.15F, -5.0F, 22.0F, 0.0F),
				rotL(1.8F, -5.0F, 22.0F, 0.0F)
			));
		}
		private static void ch_29(AnimationDefinition.Builder b) {
			b.addAnimation("pedipalp_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -14.0F, -14.0F, 0.0F),
				rotL(0.7F, -7.0F, -18.0F, 0.0F),
				rotL(1.15F, -5.0F, -22.0F, 0.0F),
				rotL(1.8F, -5.0F, -22.0F, 0.0F)
			));
		}
		private static void ch_30(AnimationDefinition.Builder b) {
			b.addAnimation("claw_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -10.0F, 8.0F, 0.0F),
				rotL(1.15F, -3.0F, 8.0F, 0.0F),
				rotL(1.8F, -3.0F, 8.0F, 0.0F)
			));
		}
		private static void ch_31(AnimationDefinition.Builder b) {
			b.addAnimation("claw_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -10.0F, -8.0F, 0.0F),
				rotL(1.15F, -3.0F, -8.0F, 0.0F),
				rotL(1.8F, -3.0F, -8.0F, 0.0F)
			));
		}
		private static void ch_32(AnimationDefinition.Builder b) {
			b.addAnimation("pincer_lower_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 14.0F, 0.0F, 0.0F),
				rotL(1.15F, -8.0F, 0.0F, 0.0F),
				rotL(1.8F, -8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_33(AnimationDefinition.Builder b) {
			b.addAnimation("pincer_lower_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, 14.0F, 0.0F, 0.0F),
				rotL(1.15F, -8.0F, 0.0F, 0.0F),
				rotL(1.8F, -8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_34(AnimationDefinition.Builder b) {
			b.addAnimation("abdomen_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -8.0F, 0.0F, 0.0F),
				rotL(1.15F, 8.0F, 0.0F, 0.0F),
				rotL(1.8F, 8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_35(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -6.0F, 4.0F, 0.0F),
				rotL(1.25F, -8.0F, -10.0F, 12.0F),
				rotL(1.8F, -8.0F, -10.0F, 12.0F)
			));
		}
		private static void ch_36(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -6.0F, 4.0F, 0.0F),
				rotL(1.25F, -14.0F, -8.0F, 10.0F),
				rotL(1.8F, -14.0F, -8.0F, 10.0F)
			));
		}
		private static void ch_37(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -6.0F, 4.0F, 0.0F),
				rotL(1.25F, -14.0F, -6.0F, 6.0F),
				rotL(1.8F, -14.0F, -6.0F, 6.0F)
			));
		}
		private static void ch_38(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -6.0F, 4.0F, 0.0F),
				rotL(1.25F, -12.0F, -4.0F, 4.0F),
				rotL(1.8F, -12.0F, -4.0F, 4.0F)
			));
		}
		private static void ch_39(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -6.0F, 4.0F, 0.0F),
				rotL(1.25F, -12.0F, -2.0F, 2.0F),
				rotL(1.8F, -12.0F, -2.0F, 2.0F)
			));
		}
		private static void ch_40(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -6.0F, 4.0F, 0.0F),
				rotL(1.25F, -10.0F, 0.0F, 0.0F),
				rotL(1.8F, -10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_41(AnimationDefinition.Builder b) {
			b.addAnimation("stinger_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.24F, -10.0F, 0.0F, 0.0F),
				rotL(1.25F, -14.0F, 0.0F, 0.0F),
				rotL(1.8F, -14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_42(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.05F, 0.0F, 0.015F, 0.25F),
				posL(0.1F, 0.0F, 0.015F, 0.5F),
				posL(0.15F, 0.0F, 0.015F, 0.75F),
				posL(0.2F, 0.0F, 0.015F, 1.0F),
				posL(0.24F, 0.0F, 0.015F, 1.2F),
				posL(0.3F, 0.0F, -0.426F, 1.154F),
				posL(0.35F, 0.0F, -0.815F, 1.115F),
				posL(0.4F, 0.0F, -1.222F, 1.077F),
				posL(0.45F, 0.0F, -1.647F, 1.038F),
				posL(0.5F, 0.0F, -2.088F, 1.0F),
				posL(0.55F, 0.0F, -2.546F, 0.962F),
				posL(0.6F, 0.0F, -3.019F, 0.923F),
				posL(0.65F, 0.0F, -3.507F, 0.885F),
				posL(0.7F, 0.0F, -4.009F, 0.846F),
				posL(0.75F, 0.0F, -4.524F, 0.808F),
				posL(0.8F, 0.0F, -5.051F, 0.769F),
				posL(0.85F, 0.0F, -5.589F, 0.731F),
				posL(0.9F, 0.0F, -6.138F, 0.692F),
				posL(0.95F, 0.0F, -6.683F, 0.654F),
				posL(1.0F, 0.0F, -6.683F, 0.615F),
				posL(1.05F, 0.0F, -6.683F, 0.577F),
				posL(1.1F, 0.0F, -6.683F, 0.538F),
				posL(1.15F, 0.0F, -6.683F, 0.5F),
				posL(1.2F, 0.0F, -6.683F, 0.5F),
				posL(1.25F, 0.0F, -6.683F, 0.5F),
				posL(1.3F, 0.0F, -6.683F, 0.5F),
				posL(1.35F, 0.0F, -6.683F, 0.5F),
				posL(1.4F, 0.0F, -6.683F, 0.5F),
				posL(1.45F, 0.0F, -6.683F, 0.5F),
				posL(1.5F, 0.0F, -6.683F, 0.5F),
				posL(1.55F, 0.0F, -6.683F, 0.5F),
				posL(1.6F, 0.0F, -6.683F, 0.5F),
				posL(1.65F, 0.0F, -6.683F, 0.5F),
				posL(1.7F, 0.0F, -6.683F, 0.5F),
				posL(1.75F, 0.0F, -6.683F, 0.5F),
				posL(1.8F, 0.0F, -6.683F, 0.5F)
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
			return b.build();
		}
	}

}
