package ddraig.net.entropica.client.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Exact Blockbench keyframe animations for Brimstone Wyvern (Fauna 54 — Lava Drake).
 * Split into per-animation static builders and <=60-keyframe helpers so constant pools and method bytecodes stay tiny.
 */
public final class BrimstoneWyvernAnimations {

	private BrimstoneWyvernAnimations() {}

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
	public static final AnimationDefinition SWIM = SwimAnimBuilder.create();
	public static final AnimationDefinition swim = SWIM;
	public static final AnimationDefinition FLY = FlyAnimBuilder.create();
	public static final AnimationDefinition fly = FLY;
	public static final AnimationDefinition TAKEOFF = TakeoffAnimBuilder.create();
	public static final AnimationDefinition takeoff = TAKEOFF;
	public static final AnimationDefinition LANDING = LandingAnimBuilder.create();
	public static final AnimationDefinition landing = LANDING;
	public static final AnimationDefinition FLYING_ATTACK = FlyingAttackAnimBuilder.create();
	public static final AnimationDefinition flying_attack = FLYING_ATTACK;
	public static final AnimationDefinition FLYING_FIREBALL_SPIT = FlyingFireballSpitAnimBuilder.create();
	public static final AnimationDefinition flying_fireball_spit = FLYING_FIREBALL_SPIT;
	public static final AnimationDefinition FLYING_HURT = FlyingHurtAnimBuilder.create();
	public static final AnimationDefinition flying_hurt = FLYING_HURT;
	public static final AnimationDefinition ATTACK = AttackAnimBuilder.create();
	public static final AnimationDefinition attack = ATTACK;
	public static final AnimationDefinition FIREBALL_SPIT = FireballSpitAnimBuilder.create();
	public static final AnimationDefinition fireball_spit = FIREBALL_SPIT;
	public static final AnimationDefinition HURT = HurtAnimBuilder.create();
	public static final AnimationDefinition hurt = HURT;
	public static final AnimationDefinition DEATH = DeathAnimBuilder.create();
	public static final AnimationDefinition death = DEATH;

	private static final class IdleAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, -0.842F, -0.612F, 0.0F),
				rotL(0.4F, -1.556F, -1.131F, 0.0F),
				rotL(0.6F, -2.033F, -1.478F, 0.0F),
				rotL(0.8F, -2.2F, -1.6F, 0.0F),
				rotL(1.0F, -2.033F, -1.478F, 0.0F),
				rotL(1.2F, -1.556F, -1.131F, 0.0F),
				rotL(1.4F, -0.842F, -0.612F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F),
				rotL(1.8F, 0.842F, 0.612F, 0.0F),
				rotL(2.0F, 1.556F, 1.131F, 0.0F),
				rotL(2.2F, 2.033F, 1.478F, 0.0F),
				rotL(2.4F, 2.2F, 1.6F, 0.0F),
				rotL(2.6F, 2.033F, 1.478F, 0.0F),
				rotL(2.8F, 1.556F, 1.131F, 0.0F),
				rotL(3.0F, 0.842F, 0.612F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.701F, 0.65F, 0.0F),
				rotL(0.2F, 0.013F, -0.204F, 0.0F),
				rotL(0.4F, -0.677F, -1.026F, 0.0F),
				rotL(0.6F, -1.263F, -1.693F, 0.0F),
				rotL(0.8F, -1.658F, -2.102F, 0.0F),
				rotL(1.0F, -1.8F, -2.191F, 0.0F),
				rotL(1.2F, -1.668F, -1.946F, 0.0F),
				rotL(1.4F, -1.282F, -1.405F, 0.0F),
				rotL(1.6F, -0.701F, -0.65F, 0.0F),
				rotL(1.8F, -0.013F, 0.204F, 0.0F),
				rotL(2.0F, 0.677F, 1.026F, 0.0F),
				rotL(2.2F, 1.263F, 1.693F, 0.0F),
				rotL(2.4F, 1.658F, 2.102F, 0.0F),
				rotL(2.6F, 1.8F, 2.191F, 0.0F),
				rotL(2.8F, 1.668F, 1.946F, 0.0F),
				rotL(3.0F, 1.282F, 1.405F, 0.0F),
				rotL(3.2F, 0.701F, 0.65F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.076F, 1.355F, 0.0F),
				rotL(0.2F, 0.594F, 0.494F, 0.0F),
				rotL(0.4F, 0.022F, -0.442F, 0.0F),
				rotL(0.6F, -0.554F, -1.311F, 0.0F),
				rotL(0.8F, -1.045F, -1.981F, 0.0F),
				rotL(1.0F, -1.377F, -2.349F, 0.0F),
				rotL(1.2F, -1.5F, -2.359F, 0.0F),
				rotL(1.4F, -1.394F, -2.01F, 0.0F),
				rotL(1.6F, -1.076F, -1.355F, 0.0F),
				rotL(1.8F, -0.594F, -0.494F, 0.0F),
				rotL(2.0F, -0.022F, 0.442F, 0.0F),
				rotL(2.2F, 0.554F, 1.311F, 0.0F),
				rotL(2.4F, 1.045F, 1.981F, 0.0F),
				rotL(2.6F, 1.377F, 2.349F, 0.0F),
				rotL(2.8F, 1.5F, 2.359F, 0.0F),
				rotL(3.0F, 1.394F, 2.01F, 0.0F),
				rotL(3.2F, 1.076F, 1.355F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.069F, 1.567F, 0.0F),
				rotL(0.2F, 0.78F, 0.972F, 0.0F),
				rotL(0.4F, 0.371F, 0.229F, 0.0F),
				rotL(0.6F, -0.094F, -0.549F, 0.0F),
				rotL(0.8F, -0.544F, -1.243F, 0.0F),
				rotL(1.0F, -0.912F, -1.748F, 0.0F),
				rotL(1.2F, -1.141F, -1.987F, 0.0F),
				rotL(1.4F, -1.196F, -1.923F, 0.0F),
				rotL(1.6F, -1.069F, -1.567F, 0.0F),
				rotL(1.8F, -0.78F, -0.972F, 0.0F),
				rotL(2.0F, -0.371F, -0.229F, 0.0F),
				rotL(2.2F, 0.094F, 0.549F, 0.0F),
				rotL(2.4F, 0.544F, 1.243F, 0.0F),
				rotL(2.6F, 0.912F, 1.748F, 0.0F),
				rotL(2.8F, 1.141F, 1.987F, 0.0F),
				rotL(3.0F, 1.196F, 1.923F, 0.0F),
				rotL(3.2F, 1.069F, 1.567F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.151F, 2.317F, 1.5F),
				rotL(0.2F, -0.257F, 1.689F, 1.386F),
				rotL(0.4F, 0.676F, 0.805F, 1.061F),
				rotL(0.6F, 1.506F, -0.203F, 0.574F),
				rotL(0.8F, 2.106F, -1.179F, 0.0F),
				rotL(1.0F, 2.386F, -1.976F, -0.574F),
				rotL(1.2F, 2.303F, -2.472F, -1.061F),
				rotL(1.4F, 1.869F, -2.592F, -1.386F),
				rotL(1.6F, 1.151F, -2.317F, -1.5F),
				rotL(1.8F, 0.257F, -1.689F, -1.386F),
				rotL(2.0F, -0.676F, -0.805F, -1.061F),
				rotL(2.2F, -1.506F, 0.203F, -0.574F),
				rotL(2.4F, -2.106F, 1.179F, 0.0F),
				rotL(2.6F, -2.386F, 1.976F, 0.574F),
				rotL(2.8F, -2.303F, 2.472F, 1.061F),
				rotL(3.0F, -1.869F, 2.592F, 1.386F),
				rotL(3.2F, -1.151F, 2.317F, 1.5F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.2F, 0.0F, 0.0F),
				rotL(0.2F, 1.2F, 0.0F, 0.0F),
				rotL(0.4F, 2.889F, 0.0F, 0.0F),
				rotL(0.6F, 4.964F, 0.0F, 0.0F),
				rotL(0.8F, 6.465F, 0.0F, 0.0F),
				rotL(1.0F, 7.165F, 0.0F, 0.0F),
				rotL(1.2F, 6.957F, 0.0F, 0.0F),
				rotL(1.4F, 5.873F, 0.0F, 0.0F),
				rotL(1.6F, 4.077F, 0.0F, 0.0F),
				rotL(1.8F, 1.843F, 0.0F, 0.0F),
				rotL(2.0F, 1.2F, 0.0F, 0.0F),
				rotL(2.2F, 1.2F, 0.0F, 0.0F),
				rotL(2.4F, 1.2F, 0.0F, 0.0F),
				rotL(2.6F, 1.2F, 0.0F, 0.0F),
				rotL(2.8F, 1.2F, 0.0F, 0.0F),
				rotL(3.0F, 1.2F, 0.0F, 0.0F),
				rotL(3.2F, 1.2F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, -1.034F),
				rotL(0.2F, -0.574F, 0.765F, 0.324F),
				rotL(0.4F, -1.061F, 1.414F, 1.633F),
				rotL(0.6F, -1.386F, 1.848F, 2.693F),
				rotL(0.8F, -1.5F, 2.0F, 3.344F),
				rotL(1.0F, -1.386F, 1.848F, 3.485F),
				rotL(1.2F, -1.061F, 1.414F, 3.096F),
				rotL(1.4F, -0.574F, 0.765F, 2.235F),
				rotL(1.6F, 0.0F, 0.0F, 1.034F),
				rotL(1.8F, 0.574F, -0.765F, -0.324F),
				rotL(2.0F, 1.061F, -1.414F, -1.633F),
				rotL(2.2F, 1.386F, -1.848F, -2.693F),
				rotL(2.4F, 1.5F, -2.0F, -3.344F),
				rotL(2.6F, 1.386F, -1.848F, -3.485F),
				rotL(2.8F, 1.061F, -1.414F, -3.096F),
				rotL(3.0F, 0.574F, -0.765F, -2.235F),
				rotL(3.2F, 0.0F, 0.0F, -1.034F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.959F, 1.168F, 2.157F),
				rotL(0.2F, 0.214F, 0.022F, 0.482F),
				rotL(0.4F, -0.563F, -1.128F, -1.267F),
				rotL(0.6F, -1.255F, -2.106F, -2.823F),
				rotL(0.8F, -1.755F, -2.763F, -3.949F),
				rotL(1.0F, -1.988F, -3.0F, -4.474F),
				rotL(1.2F, -1.919F, -2.78F, -4.318F),
				rotL(1.4F, -1.558F, -2.137F, -3.504F),
				rotL(1.6F, -0.959F, -1.168F, -2.157F),
				rotL(1.8F, -0.214F, -0.022F, -0.482F),
				rotL(2.0F, 0.563F, 1.128F, 1.267F),
				rotL(2.2F, 1.255F, 2.106F, 2.823F),
				rotL(2.4F, 1.755F, 2.763F, 3.949F),
				rotL(2.6F, 1.988F, 3.0F, 4.474F),
				rotL(2.8F, 1.919F, 2.78F, 4.318F),
				rotL(3.0F, 1.558F, 2.137F, 3.504F),
				rotL(3.2F, 0.959F, 1.168F, 2.157F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 1.034F),
				rotL(0.2F, -0.574F, -0.765F, -0.324F),
				rotL(0.4F, -1.061F, -1.414F, -1.633F),
				rotL(0.6F, -1.386F, -1.848F, -2.693F),
				rotL(0.8F, -1.5F, -2.0F, -3.344F),
				rotL(1.0F, -1.386F, -1.848F, -3.485F),
				rotL(1.2F, -1.061F, -1.414F, -3.096F),
				rotL(1.4F, -0.574F, -0.765F, -2.235F),
				rotL(1.6F, 0.0F, 0.0F, -1.034F),
				rotL(1.8F, 0.574F, 0.765F, 0.324F),
				rotL(2.0F, 1.061F, 1.414F, 1.633F),
				rotL(2.2F, 1.386F, 1.848F, 2.693F),
				rotL(2.4F, 1.5F, 2.0F, 3.344F),
				rotL(2.6F, 1.386F, 1.848F, 3.485F),
				rotL(2.8F, 1.061F, 1.414F, 3.096F),
				rotL(3.0F, 0.574F, 0.765F, 2.235F),
				rotL(3.2F, 0.0F, 0.0F, 1.034F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.959F, -1.168F, -2.157F),
				rotL(0.2F, 0.214F, -0.022F, -0.482F),
				rotL(0.4F, -0.563F, 1.128F, 1.267F),
				rotL(0.6F, -1.255F, 2.106F, 2.823F),
				rotL(0.8F, -1.755F, 2.763F, 3.949F),
				rotL(1.0F, -1.988F, 3.0F, 4.474F),
				rotL(1.2F, -1.919F, 2.78F, 4.318F),
				rotL(1.4F, -1.558F, 2.137F, 3.504F),
				rotL(1.6F, -0.959F, 1.168F, 2.157F),
				rotL(1.8F, -0.214F, 0.022F, 0.482F),
				rotL(2.0F, 0.563F, -1.128F, -1.267F),
				rotL(2.2F, 1.255F, -2.106F, -2.823F),
				rotL(2.4F, 1.755F, -2.763F, -3.949F),
				rotL(2.6F, 1.988F, -3.0F, -4.474F),
				rotL(2.8F, 1.919F, -2.78F, -4.318F),
				rotL(3.0F, 1.558F, -2.137F, -3.504F),
				rotL(3.2F, 0.959F, -1.168F, -2.157F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.436F, 1.509F, 1.075F),
				rotL(0.2F, -0.78F, 0.229F, 1.197F),
				rotL(0.4F, -1.539F, -1.087F, 1.137F),
				rotL(0.6F, -1.397F, -2.237F, 0.904F),
				rotL(0.8F, -0.436F, -3.047F, 0.533F),
				rotL(1.0F, 0.78F, -3.392F, 0.081F),
				rotL(1.2F, 1.539F, -3.222F, -0.384F),
				rotL(1.4F, 1.397F, -2.56F, -0.79F),
				rotL(1.6F, 0.436F, -1.509F, -1.075F),
				rotL(1.8F, -0.78F, -0.229F, -1.197F),
				rotL(2.0F, -1.539F, 1.087F, -1.137F),
				rotL(2.2F, -1.397F, 2.237F, -0.904F),
				rotL(2.4F, -0.436F, 3.047F, -0.533F),
				rotL(2.6F, 0.78F, 3.392F, -0.081F),
				rotL(2.8F, 1.539F, 3.222F, 0.384F),
				rotL(3.0F, 1.397F, 2.56F, 0.79F),
				rotL(3.2F, 0.436F, 1.509F, 1.075F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.839F, 2.705F, 0.727F),
				rotL(0.2F, -0.37F, 1.711F, 1.037F),
				rotL(0.4F, -1.362F, 0.456F, 1.189F),
				rotL(0.6F, -1.557F, -0.868F, 1.16F),
				rotL(0.8F, -0.839F, -2.06F, 0.955F),
				rotL(1.0F, 0.37F, -2.938F, 0.604F),
				rotL(1.2F, 1.362F, -3.369F, 0.161F),
				rotL(1.4F, 1.557F, -3.287F, -0.306F),
				rotL(1.6F, 0.839F, -2.705F, -0.727F),
				rotL(1.8F, -0.37F, -1.711F, -1.037F),
				rotL(2.0F, -1.362F, -0.456F, -1.189F),
				rotL(2.2F, -1.557F, 0.868F, -1.16F),
				rotL(2.4F, -0.839F, 2.06F, -0.955F),
				rotL(2.6F, 0.37F, 2.938F, -0.604F),
				rotL(2.8F, 1.362F, 3.369F, -0.161F),
				rotL(3.0F, 1.557F, 3.287F, 0.306F),
				rotL(3.2F, 0.839F, 2.705F, 0.727F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.179F, 3.338F, 0.228F),
				rotL(0.2F, 0.068F, 2.837F, 0.661F),
				rotL(0.4F, -1.082F, 1.905F, 0.994F),
				rotL(0.6F, -1.599F, 0.682F, 1.176F),
				rotL(0.8F, -1.179F, -0.645F, 1.178F),
				rotL(1.0F, -0.068F, -1.873F, 1.001F),
				rotL(1.2F, 1.082F, -2.816F, 0.672F),
				rotL(1.4F, 1.599F, -3.331F, 0.241F),
				rotL(1.6F, 1.179F, -3.338F, -0.228F),
				rotL(1.8F, 0.068F, -2.837F, -0.661F),
				rotL(2.0F, -1.082F, -1.905F, -0.994F),
				rotL(2.2F, -1.599F, -0.682F, -1.176F),
				rotL(2.4F, -1.179F, 0.645F, -1.178F),
				rotL(2.6F, -0.068F, 1.873F, -1.001F),
				rotL(2.8F, 1.082F, 2.816F, -0.672F),
				rotL(3.0F, 1.599F, 3.331F, -0.241F),
				rotL(3.2F, 1.179F, 3.338F, 0.228F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.429F, 3.278F, -0.319F),
				rotL(0.2F, 0.501F, 3.374F, 0.148F),
				rotL(0.4F, -0.72F, 2.957F, 0.592F),
				rotL(0.6F, -1.519F, 2.09F, 0.947F),
				rotL(0.8F, -1.429F, 0.904F, 1.157F),
				rotL(1.0F, -0.501F, -0.419F, 1.191F),
				rotL(1.2F, 0.72F, -1.678F, 1.044F),
				rotL(1.4F, 1.519F, -2.682F, 0.738F),
				rotL(1.6F, 1.429F, -3.278F, 0.319F),
				rotL(1.8F, 0.501F, -3.374F, -0.148F),
				rotL(2.0F, -0.72F, -2.957F, -0.592F),
				rotL(2.2F, -1.519F, -2.09F, -0.947F),
				rotL(2.4F, -1.429F, -0.904F, -1.157F),
				rotL(2.6F, -0.501F, 0.419F, -1.191F),
				rotL(2.8F, 0.72F, 1.678F, -1.044F),
				rotL(3.0F, 1.519F, 2.682F, -0.738F),
				rotL(3.2F, 1.429F, 3.278F, -0.319F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.571F, 2.535F, -0.8F),
				rotL(0.2F, 0.896F, 3.209F, -0.396F),
				rotL(0.4F, -0.303F, 3.395F, 0.067F),
				rotL(0.6F, -1.325F, 3.063F, 0.521F),
				rotL(0.8F, -1.571F, 2.265F, 0.895F),
				rotL(1.0F, -0.896F, 1.123F, 1.133F),
				rotL(1.2F, 0.303F, -0.191F, 1.198F),
				rotL(1.4F, 1.325F, -1.475F, 1.081F),
				rotL(1.6F, 1.571F, -2.535F, 0.8F),
				rotL(1.8F, 0.896F, -3.209F, 0.396F),
				rotL(2.0F, -0.303F, -3.395F, -0.067F),
				rotL(2.2F, -1.325F, -3.063F, -0.521F),
				rotL(2.4F, -1.571F, -2.265F, -0.895F),
				rotL(2.6F, -0.896F, -1.123F, -1.133F),
				rotL(2.8F, 0.303F, 0.191F, -1.198F),
				rotL(3.0F, 1.325F, 1.475F, -1.081F),
				rotL(3.2F, 1.571F, 2.535F, -0.8F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.594F, 1.266F, -1.114F),
				rotL(0.2F, 1.224F, 2.377F, -0.858F),
				rotL(0.4F, 0.136F, 3.127F, -0.472F),
				rotL(0.6F, -1.031F, 3.4F, -0.013F),
				rotL(0.8F, -1.594F, 3.155F, 0.447F),
				rotL(1.0F, -1.224F, 2.431F, 0.839F),
				rotL(1.2F, -0.136F, 1.336F, 1.103F),
				rotL(1.4F, 1.031F, 0.038F, 1.2F),
				rotL(1.6F, 1.594F, -1.266F, 1.114F),
				rotL(1.8F, 1.224F, -2.377F, 0.858F),
				rotL(2.0F, 0.136F, -3.127F, 0.472F),
				rotL(2.2F, -1.031F, -3.4F, 0.013F),
				rotL(2.4F, -1.594F, -3.155F, -0.447F),
				rotL(2.6F, -1.224F, -2.431F, -0.839F),
				rotL(2.8F, -0.136F, -1.336F, -1.103F),
				rotL(3.0F, 1.031F, -0.038F, -1.2F),
				rotL(3.2F, 1.594F, 1.266F, -1.114F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("knee_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("knee_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(3.2F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
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
			return b.build();
		}
	}

	private static final class WalkAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 3.0F, 0.0F),
				rotL(0.05F, 0.0F, 2.942F, -0.429F),
				rotL(0.1F, 0.0F, 2.772F, -0.842F),
				rotL(0.15F, 0.0F, 2.494F, -1.222F),
				rotL(0.2F, 0.0F, 2.121F, -1.556F),
				rotL(0.25F, 0.0F, 1.667F, -1.829F),
				rotL(0.3F, 0.0F, 1.148F, -2.033F),
				rotL(0.35F, 0.0F, 0.585F, -2.158F),
				rotL(0.4F, 0.0F, 0.0F, -2.2F),
				rotL(0.45F, 0.0F, -0.585F, -2.158F),
				rotL(0.5F, 0.0F, -1.148F, -2.033F),
				rotL(0.55F, 0.0F, -1.667F, -1.829F),
				rotL(0.6F, 0.0F, -2.121F, -1.556F),
				rotL(0.65F, 0.0F, -2.494F, -1.222F),
				rotL(0.7F, 0.0F, -2.772F, -0.842F),
				rotL(0.75F, 0.0F, -2.942F, -0.429F),
				rotL(0.8F, 0.0F, -3.0F, 0.0F),
				rotL(0.85F, 0.0F, -2.942F, 0.429F),
				rotL(0.9F, 0.0F, -2.772F, 0.842F),
				rotL(0.95F, 0.0F, -2.494F, 1.222F),
				rotL(1.0F, 0.0F, -2.121F, 1.556F),
				rotL(1.05F, 0.0F, -1.667F, 1.829F),
				rotL(1.1F, 0.0F, -1.148F, 2.033F),
				rotL(1.15F, 0.0F, -0.585F, 2.158F),
				rotL(1.2F, 0.0F, 0.0F, 2.2F),
				rotL(1.25F, 0.0F, 0.585F, 2.158F),
				rotL(1.3F, 0.0F, 1.148F, 2.033F),
				rotL(1.35F, 0.0F, 1.667F, 1.829F),
				rotL(1.4F, 0.0F, 2.121F, 1.556F),
				rotL(1.45F, 0.0F, 2.494F, 1.222F),
				rotL(1.5F, 0.0F, 2.772F, 0.842F),
				rotL(1.55F, 0.0F, 2.942F, 0.429F),
				rotL(1.6F, 0.0F, 3.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, -0.645F, 0.0F),
				posL(0.05F, -0.127F, -0.576F, 0.0F),
				posL(0.1F, -0.249F, -0.466F, 0.0F),
				posL(0.15F, -0.361F, -0.331F, 0.0F),
				posL(0.2F, -0.46F, -0.194F, 0.0F),
				posL(0.25F, -0.54F, -0.081F, 0.0F),
				posL(0.3F, -0.601F, 0.018F, 0.0F),
				posL(0.35F, -0.638F, 0.058F, 0.0F),
				posL(0.4F, -0.65F, 0.026F, 0.0F),
				posL(0.45F, -0.638F, -0.076F, 0.0F),
				posL(0.5F, -0.601F, -0.241F, 0.0F),
				posL(0.55F, -0.54F, -0.415F, 0.0F),
				posL(0.6F, -0.46F, -0.463F, 0.0F),
				posL(0.65F, -0.361F, -0.535F, 0.0F),
				posL(0.7F, -0.249F, -0.618F, 0.0F),
				posL(0.75F, -0.127F, -0.697F, 0.0F),
				posL(0.8F, 0.0F, -0.645F, 0.0F),
				posL(0.85F, 0.127F, -0.576F, 0.0F),
				posL(0.9F, 0.249F, -0.466F, 0.0F),
				posL(0.95F, 0.361F, -0.331F, 0.0F),
				posL(1.0F, 0.46F, -0.194F, 0.0F),
				posL(1.05F, 0.54F, -0.081F, 0.0F),
				posL(1.1F, 0.601F, 0.018F, 0.0F),
				posL(1.15F, 0.638F, 0.058F, 0.0F),
				posL(1.2F, 0.65F, 0.026F, 0.0F),
				posL(1.25F, 0.638F, -0.076F, 0.0F),
				posL(1.3F, 0.601F, -0.241F, 0.0F),
				posL(1.35F, 0.54F, -0.415F, 0.0F),
				posL(1.4F, 0.46F, -0.463F, 0.0F),
				posL(1.45F, 0.361F, -0.535F, 0.0F),
				posL(1.5F, 0.249F, -0.618F, 0.0F),
				posL(1.55F, 0.127F, -0.697F, 0.0F),
				posL(1.6F, 0.0F, -0.645F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.6F, -4.0F, 0.0F),
				rotL(0.05F, 0.783F, -3.923F, 0.0F),
				rotL(0.1F, 1.303F, -3.696F, 0.0F),
				rotL(0.15F, 2.082F, -3.326F, 0.0F),
				rotL(0.2F, 3.0F, -2.828F, 0.0F),
				rotL(0.25F, 3.918F, -2.222F, 0.0F),
				rotL(0.3F, 4.697F, -1.531F, 0.0F),
				rotL(0.35F, 5.217F, -0.78F, 0.0F),
				rotL(0.4F, 5.4F, 0.0F, 0.0F),
				rotL(0.45F, 5.217F, 0.78F, 0.0F),
				rotL(0.5F, 4.697F, 1.531F, 0.0F),
				rotL(0.55F, 3.918F, 2.222F, 0.0F),
				rotL(0.6F, 3.0F, 2.828F, 0.0F),
				rotL(0.65F, 2.082F, 3.326F, 0.0F),
				rotL(0.7F, 1.303F, 3.696F, 0.0F),
				rotL(0.75F, 0.783F, 3.923F, 0.0F),
				rotL(0.8F, 0.6F, 4.0F, 0.0F),
				rotL(0.85F, 0.783F, 3.923F, 0.0F),
				rotL(0.9F, 1.303F, 3.696F, 0.0F),
				rotL(0.95F, 2.082F, 3.326F, 0.0F),
				rotL(1.0F, 3.0F, 2.828F, 0.0F),
				rotL(1.05F, 3.918F, 2.222F, 0.0F),
				rotL(1.1F, 4.697F, 1.531F, 0.0F),
				rotL(1.15F, 5.217F, 0.78F, 0.0F),
				rotL(1.2F, 5.4F, 0.0F, 0.0F),
				rotL(1.25F, 5.217F, -0.78F, 0.0F),
				rotL(1.3F, 4.697F, -1.531F, 0.0F),
				rotL(1.35F, 3.918F, -2.222F, 0.0F),
				rotL(1.4F, 3.0F, -2.828F, 0.0F),
				rotL(1.45F, 2.082F, -3.326F, 0.0F),
				rotL(1.5F, 1.303F, -3.696F, 0.0F),
				rotL(1.55F, 0.783F, -3.923F, 0.0F),
				rotL(1.6F, 0.6F, -4.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.055F, 2.2F, 0.0F),
				rotL(0.05F, 0.236F, 2.158F, 0.0F),
				rotL(0.1F, -0.619F, 2.033F, 0.0F),
				rotL(0.15F, -1.38F, 1.829F, 0.0F),
				rotL(0.2F, -1.931F, 1.556F, 0.0F),
				rotL(0.25F, -2.187F, 1.222F, 0.0F),
				rotL(0.3F, -2.111F, 0.842F, 0.0F),
				rotL(0.35F, -1.713F, 0.429F, 0.0F),
				rotL(0.4F, -1.055F, 0.0F, 0.0F),
				rotL(0.45F, -0.236F, -0.429F, 0.0F),
				rotL(0.5F, 0.619F, -0.842F, 0.0F),
				rotL(0.55F, 1.38F, -1.222F, 0.0F),
				rotL(0.6F, 1.931F, -1.556F, 0.0F),
				rotL(0.65F, 2.187F, -1.829F, 0.0F),
				rotL(0.7F, 2.111F, -2.033F, 0.0F),
				rotL(0.75F, 1.713F, -2.158F, 0.0F),
				rotL(0.8F, 1.055F, -2.2F, 0.0F),
				rotL(0.85F, 0.236F, -2.158F, 0.0F),
				rotL(0.9F, -0.619F, -2.033F, 0.0F),
				rotL(0.95F, -1.38F, -1.829F, 0.0F),
				rotL(1.0F, -1.931F, -1.556F, 0.0F),
				rotL(1.05F, -2.187F, -1.222F, 0.0F),
				rotL(1.1F, -2.111F, -0.842F, 0.0F),
				rotL(1.15F, -1.713F, -0.429F, 0.0F),
				rotL(1.2F, -1.055F, 0.0F, 0.0F),
				rotL(1.25F, -0.236F, 0.429F, 0.0F),
				rotL(1.3F, 0.619F, 0.842F, 0.0F),
				rotL(1.35F, 1.38F, 1.222F, 0.0F),
				rotL(1.4F, 1.931F, 1.556F, 0.0F),
				rotL(1.45F, 2.187F, 1.829F, 0.0F),
				rotL(1.5F, 2.111F, 2.033F, 0.0F),
				rotL(1.55F, 1.713F, 2.158F, 0.0F),
				rotL(1.6F, 1.055F, 2.2F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.41F, 1.8F, 0.0F),
				rotL(0.05F, 0.874F, 1.765F, 0.0F),
				rotL(0.1F, 0.206F, 1.663F, 0.0F),
				rotL(0.15F, -0.494F, 1.497F, 0.0F),
				rotL(0.2F, -1.119F, 1.273F, 0.0F),
				rotL(0.25F, -1.573F, 1.0F, 0.0F),
				rotL(0.3F, -1.788F, 0.689F, 0.0F),
				rotL(0.35F, -1.731F, 0.351F, 0.0F),
				rotL(0.4F, -1.41F, 0.0F, 0.0F),
				rotL(0.45F, -0.874F, -0.351F, 0.0F),
				rotL(0.5F, -0.206F, -0.689F, 0.0F),
				rotL(0.55F, 0.494F, -1.0F, 0.0F),
				rotL(0.6F, 1.119F, -1.273F, 0.0F),
				rotL(0.65F, 1.573F, -1.497F, 0.0F),
				rotL(0.7F, 1.788F, -1.663F, 0.0F),
				rotL(0.75F, 1.731F, -1.765F, 0.0F),
				rotL(0.8F, 1.41F, -1.8F, 0.0F),
				rotL(0.85F, 0.874F, -1.765F, 0.0F),
				rotL(0.9F, 0.206F, -1.663F, 0.0F),
				rotL(0.95F, -0.494F, -1.497F, 0.0F),
				rotL(1.0F, -1.119F, -1.273F, 0.0F),
				rotL(1.05F, -1.573F, -1.0F, 0.0F),
				rotL(1.1F, -1.788F, -0.689F, 0.0F),
				rotL(1.15F, -1.731F, -0.351F, 0.0F),
				rotL(1.2F, -1.41F, 0.0F, 0.0F),
				rotL(1.25F, -0.874F, 0.351F, 0.0F),
				rotL(1.3F, -0.206F, 0.689F, 0.0F),
				rotL(1.35F, 0.494F, 1.0F, 0.0F),
				rotL(1.4F, 1.119F, 1.273F, 0.0F),
				rotL(1.45F, 1.573F, 1.497F, 0.0F),
				rotL(1.5F, 1.788F, 1.663F, 0.0F),
				rotL(1.55F, 1.731F, 1.765F, 0.0F),
				rotL(1.6F, 1.41F, 1.8F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.118F, 0.0F, 0.0F),
				rotL(0.05F, 0.867F, 0.0F, 0.0F),
				rotL(0.1F, 0.483F, 0.0F, 0.0F),
				rotL(0.15F, 0.026F, 0.0F, 0.0F),
				rotL(0.2F, -0.435F, 0.0F, 0.0F),
				rotL(0.25F, -0.83F, 0.0F, 0.0F),
				rotL(0.3F, -1.098F, 0.0F, 0.0F),
				rotL(0.35F, -1.2F, 0.0F, 0.0F),
				rotL(0.4F, -1.118F, 0.0F, 0.0F),
				rotL(0.45F, -0.867F, 0.0F, 0.0F),
				rotL(0.5F, -0.483F, 0.0F, 0.0F),
				rotL(0.55F, -0.026F, 0.0F, 0.0F),
				rotL(0.6F, 0.435F, 0.0F, 0.0F),
				rotL(0.65F, 0.83F, 0.0F, 0.0F),
				rotL(0.7F, 1.098F, 0.0F, 0.0F),
				rotL(0.75F, 1.2F, 0.0F, 0.0F),
				rotL(0.8F, 1.118F, 0.0F, 0.0F),
				rotL(0.85F, 0.867F, 0.0F, 0.0F),
				rotL(0.9F, 0.483F, 0.0F, 0.0F),
				rotL(0.95F, 0.026F, 0.0F, 0.0F),
				rotL(1.0F, -0.435F, 0.0F, 0.0F),
				rotL(1.05F, -0.83F, 0.0F, 0.0F),
				rotL(1.1F, -1.098F, 0.0F, 0.0F),
				rotL(1.15F, -1.2F, 0.0F, 0.0F),
				rotL(1.2F, -1.118F, 0.0F, 0.0F),
				rotL(1.25F, -0.867F, 0.0F, 0.0F),
				rotL(1.3F, -0.483F, 0.0F, 0.0F),
				rotL(1.35F, -0.026F, 0.0F, 0.0F),
				rotL(1.4F, 0.435F, 0.0F, 0.0F),
				rotL(1.45F, 0.83F, 0.0F, 0.0F),
				rotL(1.5F, 1.098F, 0.0F, 0.0F),
				rotL(1.55F, 1.2F, 0.0F, 0.0F),
				rotL(1.6F, 1.118F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.807F, 1.5F, 0.0F),
				rotL(0.05F, -0.659F, 1.471F, 0.39F),
				rotL(0.1F, 0.59F, 1.386F, 0.765F),
				rotL(0.15F, 1.749F, 1.247F, 1.111F),
				rotL(0.2F, 2.641F, 1.061F, 1.414F),
				rotL(0.25F, 3.131F, 0.833F, 1.663F),
				rotL(0.3F, 3.145F, 0.574F, 1.848F),
				rotL(0.35F, 2.68F, 0.293F, 1.962F),
				rotL(0.4F, 1.807F, 0.0F, 2.0F),
				rotL(0.45F, 0.659F, -0.293F, 1.962F),
				rotL(0.5F, -0.59F, -0.574F, 1.848F),
				rotL(0.55F, -1.749F, -0.833F, 1.663F),
				rotL(0.6F, -2.641F, -1.061F, 1.414F),
				rotL(0.65F, -3.131F, -1.247F, 1.111F),
				rotL(0.7F, -3.145F, -1.386F, 0.765F),
				rotL(0.75F, -2.68F, -1.471F, 0.39F),
				rotL(0.8F, -1.807F, -1.5F, 0.0F),
				rotL(0.85F, -0.659F, -1.471F, -0.39F),
				rotL(0.9F, 0.59F, -1.386F, -0.765F),
				rotL(0.95F, 1.749F, -1.247F, -1.111F),
				rotL(1.0F, 2.641F, -1.061F, -1.414F),
				rotL(1.05F, 3.131F, -0.833F, -1.663F),
				rotL(1.1F, 3.145F, -0.574F, -1.848F),
				rotL(1.15F, 2.68F, -0.293F, -1.962F),
				rotL(1.2F, 1.807F, 0.0F, -2.0F),
				rotL(1.25F, 0.659F, 0.293F, -1.962F),
				rotL(1.3F, -0.59F, 0.574F, -1.848F),
				rotL(1.35F, -1.749F, 0.833F, -1.663F),
				rotL(1.4F, -2.641F, 1.061F, -1.414F),
				rotL(1.45F, -3.131F, 1.247F, -1.111F),
				rotL(1.5F, -3.145F, 1.386F, -0.765F),
				rotL(1.55F, -2.68F, 1.471F, -0.39F),
				rotL(1.6F, -1.807F, 1.5F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.5F, 0.0F, 0.0F),
				rotL(0.05F, 3.031F, 0.0F, 0.0F),
				rotL(0.1F, 4.328F, 0.0F, 0.0F),
				rotL(0.15F, 5.196F, 0.0F, 0.0F),
				rotL(0.2F, 5.5F, 0.0F, 0.0F),
				rotL(0.25F, 5.196F, 0.0F, 0.0F),
				rotL(0.3F, 4.328F, 0.0F, 0.0F),
				rotL(0.35F, 3.031F, 0.0F, 0.0F),
				rotL(0.4F, 1.5F, 0.0F, 0.0F),
				rotL(0.45F, 1.5F, 0.0F, 0.0F),
				rotL(0.5F, 1.5F, 0.0F, 0.0F),
				rotL(0.55F, 1.5F, 0.0F, 0.0F),
				rotL(0.6F, 1.5F, 0.0F, 0.0F),
				rotL(0.65F, 1.5F, 0.0F, 0.0F),
				rotL(0.7F, 1.5F, 0.0F, 0.0F),
				rotL(0.75F, 1.5F, 0.0F, 0.0F),
				rotL(0.8F, 1.5F, 0.0F, 0.0F),
				rotL(0.85F, 3.031F, 0.0F, 0.0F),
				rotL(0.9F, 4.328F, 0.0F, 0.0F),
				rotL(0.95F, 5.196F, 0.0F, 0.0F),
				rotL(1.0F, 5.5F, 0.0F, 0.0F),
				rotL(1.05F, 5.196F, 0.0F, 0.0F),
				rotL(1.1F, 4.328F, 0.0F, 0.0F),
				rotL(1.15F, 3.031F, 0.0F, 0.0F),
				rotL(1.2F, 1.5F, 0.0F, 0.0F),
				rotL(1.25F, 1.5F, 0.0F, 0.0F),
				rotL(1.3F, 1.5F, 0.0F, 0.0F),
				rotL(1.35F, 1.5F, 0.0F, 0.0F),
				rotL(1.4F, 1.5F, 0.0F, 0.0F),
				rotL(1.45F, 1.5F, 0.0F, 0.0F),
				rotL(1.5F, 1.5F, 0.0F, 0.0F),
				rotL(1.55F, 1.5F, 0.0F, 0.0F),
				rotL(1.6F, 1.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.009F, -4.565F, 0.0F),
				rotL(0.05F, -2.199F, -4.875F, 0.0F),
				rotL(0.1F, -2.055F, -4.998F, 0.0F),
				rotL(0.15F, -1.598F, -4.929F, 0.0F),
				rotL(0.2F, -0.897F, -4.67F, 0.0F),
				rotL(0.25F, -0.06F, -4.232F, 0.0F),
				rotL(0.3F, 0.786F, -3.631F, 0.0F),
				rotL(0.35F, 1.513F, -2.89F, 0.0F),
				rotL(0.4F, 2.009F, -2.039F, 0.0F),
				rotL(0.45F, 2.199F, -1.109F, 0.0F),
				rotL(0.5F, 2.055F, -0.136F, 0.0F),
				rotL(0.55F, 1.598F, 0.841F, 0.0F),
				rotL(0.6F, 0.897F, 1.787F, 0.0F),
				rotL(0.65F, 0.06F, 2.663F, 0.0F),
				rotL(0.7F, -0.786F, 3.438F, 0.0F),
				rotL(0.75F, -1.513F, 4.08F, 0.0F),
				rotL(0.8F, -2.009F, 4.565F, 0.0F),
				rotL(0.85F, -2.199F, 4.875F, 0.0F),
				rotL(0.9F, -2.055F, 4.998F, 0.0F),
				rotL(0.95F, -1.598F, 4.929F, 0.0F),
				rotL(1.0F, -0.897F, 4.67F, 0.0F),
				rotL(1.05F, -0.06F, 4.232F, 0.0F),
				rotL(1.1F, 0.786F, 3.631F, 0.0F),
				rotL(1.15F, 1.513F, 2.89F, 0.0F),
				rotL(1.2F, 2.009F, 2.039F, 0.0F),
				rotL(1.25F, 2.199F, 1.109F, 0.0F),
				rotL(1.3F, 2.055F, 0.136F, 0.0F),
				rotL(1.35F, 1.598F, -0.841F, 0.0F),
				rotL(1.4F, 0.897F, -1.787F, 0.0F),
				rotL(1.45F, 0.06F, -2.663F, 0.0F),
				rotL(1.5F, -0.786F, -3.438F, 0.0F),
				rotL(1.55F, -1.513F, -4.08F, 0.0F),
				rotL(1.6F, -2.009F, -4.565F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.468F, -3.337F, 0.0F),
				rotL(0.05F, -1.984F, -4.0F, 0.0F),
				rotL(0.1F, -2.197F, -4.508F, 0.0F),
				rotL(0.15F, -2.075F, -4.843F, 0.0F),
				rotL(0.2F, -1.638F, -4.993F, 0.0F),
				rotL(0.25F, -0.952F, -4.95F, 0.0F),
				rotL(0.3F, -0.12F, -4.717F, 0.0F),
				rotL(0.35F, 0.73F, -4.303F, 0.0F),
				rotL(0.4F, 1.468F, -3.723F, 0.0F),
				rotL(0.45F, 1.984F, -3.001F, 0.0F),
				rotL(0.5F, 2.197F, -2.163F, 0.0F),
				rotL(0.55F, 2.075F, -1.242F, 0.0F),
				rotL(0.6F, 1.638F, -0.273F, 0.0F),
				rotL(0.65F, 0.952F, 0.706F, 0.0F),
				rotL(0.7F, 0.12F, 1.658F, 0.0F),
				rotL(0.75F, -0.73F, 2.547F, 0.0F),
				rotL(0.8F, -1.468F, 3.337F, 0.0F),
				rotL(0.85F, -1.984F, 4.0F, 0.0F),
				rotL(0.9F, -2.197F, 4.508F, 0.0F),
				rotL(0.95F, -2.075F, 4.843F, 0.0F),
				rotL(1.0F, -1.638F, 4.993F, 0.0F),
				rotL(1.05F, -0.952F, 4.95F, 0.0F),
				rotL(1.1F, -0.12F, 4.717F, 0.0F),
				rotL(1.15F, 0.73F, 4.303F, 0.0F),
				rotL(1.2F, 1.468F, 3.723F, 0.0F),
				rotL(1.25F, 1.984F, 3.001F, 0.0F),
				rotL(1.3F, 2.197F, 2.163F, 0.0F),
				rotL(1.35F, 2.075F, 1.242F, 0.0F),
				rotL(1.4F, 1.638F, 0.273F, 0.0F),
				rotL(1.45F, 0.952F, -0.706F, 0.0F),
				rotL(1.5F, 0.12F, -1.658F, 0.0F),
				rotL(1.55F, -0.73F, -2.547F, 0.0F),
				rotL(1.6F, -1.468F, -3.337F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -0.673F, -1.529F, 0.0F),
				rotL(0.05F, -1.423F, -2.428F, 0.0F),
				rotL(0.1F, -1.957F, -3.234F, 0.0F),
				rotL(0.15F, -2.193F, -3.916F, 0.0F),
				rotL(0.2F, -2.095F, -4.447F, 0.0F),
				rotL(0.25F, -1.678F, -4.808F, 0.0F),
				rotL(0.3F, -1.005F, -4.983F, 0.0F),
				rotL(0.35F, -0.18F, -4.967F, 0.0F),
				rotL(0.4F, 0.673F, -4.76F, 0.0F),
				rotL(0.45F, 1.423F, -4.371F, 0.0F),
				rotL(0.5F, 1.957F, -3.813F, 0.0F),
				rotL(0.55F, 2.193F, -3.109F, 0.0F),
				rotL(0.6F, 2.095F, -2.285F, 0.0F),
				rotL(0.65F, 1.678F, -1.373F, 0.0F),
				rotL(0.7F, 1.005F, -0.409F, 0.0F),
				rotL(0.75F, 0.18F, 0.571F, 0.0F),
				rotL(0.8F, -0.673F, 1.529F, 0.0F),
				rotL(0.85F, -1.423F, 2.428F, 0.0F),
				rotL(0.9F, -1.957F, 3.234F, 0.0F),
				rotL(0.95F, -2.193F, 3.916F, 0.0F),
				rotL(1.0F, -2.095F, 4.447F, 0.0F),
				rotL(1.05F, -1.678F, 4.808F, 0.0F),
				rotL(1.1F, -1.005F, 4.983F, 0.0F),
				rotL(1.15F, -0.18F, 4.967F, 0.0F),
				rotL(1.2F, 0.673F, 4.76F, 0.0F),
				rotL(1.25F, 1.423F, 4.371F, 0.0F),
				rotL(1.3F, 1.957F, 3.813F, 0.0F),
				rotL(1.35F, 2.193F, 3.109F, 0.0F),
				rotL(1.4F, 2.095F, 2.285F, 0.0F),
				rotL(1.45F, 1.678F, 1.373F, 0.0F),
				rotL(1.5F, 1.005F, 0.409F, 0.0F),
				rotL(1.55F, 0.18F, -0.571F, 0.0F),
				rotL(1.6F, -0.673F, -1.529F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.24F, 0.545F, 0.0F),
				rotL(0.05F, -0.615F, -0.435F, 0.0F),
				rotL(0.1F, -1.377F, -1.399F, 0.0F),
				rotL(0.15F, -1.929F, -2.308F, 0.0F),
				rotL(0.2F, -2.187F, -3.129F, 0.0F),
				rotL(0.25F, -2.112F, -3.83F, 0.0F),
				rotL(0.3F, -1.716F, -4.383F, 0.0F),
				rotL(0.35F, -1.058F, -4.768F, 0.0F),
				rotL(0.4F, -0.24F, -4.97F, 0.0F),
				rotL(0.45F, 0.615F, -4.981F, 0.0F),
				rotL(0.5F, 1.377F, -4.8F, 0.0F),
				rotL(0.55F, 1.929F, -4.435F, 0.0F),
				rotL(0.6F, 2.187F, -3.9F, 0.0F),
				rotL(0.65F, 2.112F, -3.214F, 0.0F),
				rotL(0.7F, 1.716F, -2.405F, 0.0F),
				rotL(0.75F, 1.058F, -1.504F, 0.0F),
				rotL(0.8F, 0.24F, -0.545F, 0.0F),
				rotL(0.85F, -0.615F, 0.435F, 0.0F),
				rotL(0.9F, -1.377F, 1.399F, 0.0F),
				rotL(0.95F, -1.929F, 2.308F, 0.0F),
				rotL(1.0F, -2.187F, 3.129F, 0.0F),
				rotL(1.05F, -2.112F, 3.83F, 0.0F),
				rotL(1.1F, -1.716F, 4.383F, 0.0F),
				rotL(1.15F, -1.058F, 4.768F, 0.0F),
				rotL(1.2F, -0.24F, 4.97F, 0.0F),
				rotL(1.25F, 0.615F, 4.981F, 0.0F),
				rotL(1.3F, 1.377F, 4.8F, 0.0F),
				rotL(1.35F, 1.929F, 4.435F, 0.0F),
				rotL(1.4F, 2.187F, 3.9F, 0.0F),
				rotL(1.45F, 2.112F, 3.214F, 0.0F),
				rotL(1.5F, 1.716F, 2.405F, 0.0F),
				rotL(1.55F, 1.058F, 1.504F, 0.0F),
				rotL(1.6F, 0.24F, 0.545F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.111F, 2.524F, 0.0F),
				rotL(0.05F, 0.299F, 1.634F, 0.0F),
				rotL(0.1F, -0.557F, 0.68F, 0.0F),
				rotL(0.15F, -1.329F, -0.299F, 0.0F),
				rotL(0.2F, -1.899F, -1.267F, 0.0F),
				rotL(0.25F, -2.18F, -2.186F, 0.0F),
				rotL(0.3F, -2.128F, -3.022F, 0.0F),
				rotL(0.35F, -1.753F, -3.741F, 0.0F),
				rotL(0.4F, -1.111F, -4.316F, 0.0F),
				rotL(0.45F, -0.299F, -4.726F, 0.0F),
				rotL(0.5F, 0.557F, -4.953F, 0.0F),
				rotL(0.55F, 1.329F, -4.991F, 0.0F),
				rotL(0.6F, 1.899F, -4.837F, 0.0F),
				rotL(0.65F, 2.18F, -4.497F, 0.0F),
				rotL(0.7F, 2.128F, -3.984F, 0.0F),
				rotL(0.75F, 1.753F, -3.318F, 0.0F),
				rotL(0.8F, 1.111F, -2.524F, 0.0F),
				rotL(0.85F, 0.299F, -1.634F, 0.0F),
				rotL(0.9F, -0.557F, -0.68F, 0.0F),
				rotL(0.95F, -1.329F, 0.299F, 0.0F),
				rotL(1.0F, -1.899F, 1.267F, 0.0F),
				rotL(1.05F, -2.18F, 2.186F, 0.0F),
				rotL(1.1F, -2.128F, 3.022F, 0.0F),
				rotL(1.15F, -1.753F, 3.741F, 0.0F),
				rotL(1.2F, -1.111F, 4.316F, 0.0F),
				rotL(1.25F, -0.299F, 4.726F, 0.0F),
				rotL(1.3F, 0.557F, 4.953F, 0.0F),
				rotL(1.35F, 1.329F, 4.991F, 0.0F),
				rotL(1.4F, 1.899F, 4.837F, 0.0F),
				rotL(1.45F, 2.18F, 4.497F, 0.0F),
				rotL(1.5F, 2.128F, 3.984F, 0.0F),
				rotL(1.55F, 1.753F, 3.318F, 0.0F),
				rotL(1.6F, 1.111F, 2.524F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.788F, 4.065F, 0.0F),
				rotL(0.05F, 1.162F, 3.419F, 0.0F),
				rotL(0.1F, 0.359F, 2.641F, 0.0F),
				rotL(0.15F, -0.499F, 1.762F, 0.0F),
				rotL(0.2F, -1.281F, 0.815F, 0.0F),
				rotL(0.25F, -1.868F, -0.163F, 0.0F),
				rotL(0.3F, -2.171F, -1.135F, 0.0F),
				rotL(0.35F, -2.143F, -2.063F, 0.0F),
				rotL(0.4F, -1.788F, -2.912F, 0.0F),
				rotL(0.45F, -1.162F, -3.649F, 0.0F),
				rotL(0.5F, -0.359F, -4.246F, 0.0F),
				rotL(0.55F, 0.499F, -4.679F, 0.0F),
				rotL(0.6F, 1.281F, -4.933F, 0.0F),
				rotL(0.65F, 1.868F, -4.997F, 0.0F),
				rotL(0.7F, 2.171F, -4.87F, 0.0F),
				rotL(0.75F, 2.143F, -4.555F, 0.0F),
				rotL(0.8F, 1.788F, -4.065F, 0.0F),
				rotL(0.85F, 1.162F, -3.419F, 0.0F),
				rotL(0.9F, 0.359F, -2.641F, 0.0F),
				rotL(0.95F, -0.499F, -1.762F, 0.0F),
				rotL(1.0F, -1.281F, -0.815F, 0.0F),
				rotL(1.05F, -1.868F, 0.163F, 0.0F),
				rotL(1.1F, -2.171F, 1.135F, 0.0F),
				rotL(1.15F, -2.143F, 2.063F, 0.0F),
				rotL(1.2F, -1.788F, 2.912F, 0.0F),
				rotL(1.25F, -1.162F, 3.649F, 0.0F),
				rotL(1.3F, -0.359F, 4.246F, 0.0F),
				rotL(1.35F, 0.499F, 4.679F, 0.0F),
				rotL(1.4F, 1.281F, 4.933F, 0.0F),
				rotL(1.45F, 1.868F, 4.997F, 0.0F),
				rotL(1.5F, 2.171F, 4.87F, 0.0F),
				rotL(1.55F, 2.143F, 4.555F, 0.0F),
				rotL(1.6F, 1.788F, 4.065F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -18.0F, 0.0F, 0.0F),
				rotL(0.05F, -17.654F, 0.0F, 0.0F),
				rotL(0.1F, -16.63F, 0.0F, 0.0F),
				rotL(0.15F, -14.966F, 0.0F, 0.0F),
				rotL(0.2F, -12.728F, 0.0F, 0.0F),
				rotL(0.25F, -10.0F, 0.0F, 0.0F),
				rotL(0.3F, -6.888F, 0.0F, 0.0F),
				rotL(0.35F, -3.512F, 0.0F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.45F, 3.512F, 0.0F, 0.0F),
				rotL(0.5F, 6.888F, 0.0F, 0.0F),
				rotL(0.55F, 10.0F, 0.0F, 0.0F),
				rotL(0.6F, 12.728F, 0.0F, 0.0F),
				rotL(0.65F, 14.966F, 0.0F, 0.0F),
				rotL(0.7F, 16.63F, 0.0F, 0.0F),
				rotL(0.75F, 17.654F, 0.0F, 0.0F),
				rotL(0.8F, 18.0F, 0.0F, 0.0F),
				rotL(0.85F, 15.313F, 0.0F, 0.0F),
				rotL(0.9F, 12.038F, 0.0F, 0.0F),
				rotL(0.95F, 8.3F, 0.0F, 0.0F),
				rotL(1.0F, 4.243F, 0.0F, 0.0F),
				rotL(1.05F, 0.023F, 0.0F, 0.0F),
				rotL(1.1F, -4.198F, 0.0F, 0.0F),
				rotL(1.15F, -8.258F, 0.0F, 0.0F),
				rotL(1.2F, -12.0F, 0.0F, 0.0F),
				rotL(1.25F, -15.281F, 0.0F, 0.0F),
				rotL(1.3F, -17.975F, 0.0F, 0.0F),
				rotL(1.35F, -19.978F, 0.0F, 0.0F),
				rotL(1.4F, -21.213F, 0.0F, 0.0F),
				rotL(1.45F, -21.633F, 0.0F, 0.0F),
				rotL(1.5F, -21.222F, 0.0F, 0.0F),
				rotL(1.55F, -19.995F, 0.0F, 0.0F),
				rotL(1.6F, -18.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("knee_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -8.0F, 0.0F, 0.0F),
				rotL(0.05F, -7.827F, 0.0F, 0.0F),
				rotL(0.1F, -7.315F, 0.0F, 0.0F),
				rotL(0.15F, -6.483F, 0.0F, 0.0F),
				rotL(0.2F, -5.364F, 0.0F, 0.0F),
				rotL(0.25F, -4.0F, 0.0F, 0.0F),
				rotL(0.3F, -2.444F, 0.0F, 0.0F),
				rotL(0.35F, -0.756F, 0.0F, 0.0F),
				rotL(0.4F, 1.0F, 0.0F, 0.0F),
				rotL(0.45F, 2.756F, 0.0F, 0.0F),
				rotL(0.5F, 4.444F, 0.0F, 0.0F),
				rotL(0.55F, 6.0F, 0.0F, 0.0F),
				rotL(0.6F, 7.364F, 0.0F, 0.0F),
				rotL(0.65F, 8.483F, 0.0F, 0.0F),
				rotL(0.7F, 9.315F, 0.0F, 0.0F),
				rotL(0.75F, 9.827F, 0.0F, 0.0F),
				rotL(0.8F, 10.0F, 0.0F, 0.0F),
				rotL(0.85F, 14.899F, 0.0F, 0.0F),
				rotL(0.9F, 19.265F, 0.0F, 0.0F),
				rotL(0.95F, 22.928F, 0.0F, 0.0F),
				rotL(1.0F, 25.749F, 0.0F, 0.0F),
				rotL(1.05F, 27.618F, 0.0F, 0.0F),
				rotL(1.1F, 28.465F, 0.0F, 0.0F),
				rotL(1.15F, 28.256F, 0.0F, 0.0F),
				rotL(1.2F, 27.0F, 0.0F, 0.0F),
				rotL(1.25F, 24.745F, 0.0F, 0.0F),
				rotL(1.3F, 21.577F, 0.0F, 0.0F),
				rotL(1.35F, 17.618F, 0.0F, 0.0F),
				rotL(1.4F, 13.021F, 0.0F, 0.0F),
				rotL(1.45F, 7.962F, 0.0F, 0.0F),
				rotL(1.5F, 2.635F, 0.0F, 0.0F),
				rotL(1.55F, -2.755F, 0.0F, 0.0F),
				rotL(1.6F, -8.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 6.0F, 0.0F, 0.0F),
				rotL(0.05F, 5.808F, 0.0F, 0.0F),
				rotL(0.1F, 5.239F, 0.0F, 0.0F),
				rotL(0.15F, 4.315F, 0.0F, 0.0F),
				rotL(0.2F, 3.071F, 0.0F, 0.0F),
				rotL(0.25F, 1.556F, 0.0F, 0.0F),
				rotL(0.3F, -0.173F, 0.0F, 0.0F),
				rotL(0.35F, -2.049F, 0.0F, 0.0F),
				rotL(0.4F, -4.0F, 0.0F, 0.0F),
				rotL(0.45F, -5.951F, 0.0F, 0.0F),
				rotL(0.5F, -7.827F, 0.0F, 0.0F),
				rotL(0.55F, -9.556F, 0.0F, 0.0F),
				rotL(0.6F, -11.071F, 0.0F, 0.0F),
				rotL(0.65F, -12.315F, 0.0F, 0.0F),
				rotL(0.7F, -13.239F, 0.0F, 0.0F),
				rotL(0.75F, -13.808F, 0.0F, 0.0F),
				rotL(0.8F, -14.0F, 0.0F, 0.0F),
				rotL(0.85F, -17.319F, 0.0F, 0.0F),
				rotL(0.9F, -20.127F, 0.0F, 0.0F),
				rotL(0.95F, -22.315F, 0.0F, 0.0F),
				rotL(1.0F, -23.799F, 0.0F, 0.0F),
				rotL(1.05F, -24.522F, 0.0F, 0.0F),
				rotL(1.1F, -24.457F, 0.0F, 0.0F),
				rotL(1.15F, -23.605F, 0.0F, 0.0F),
				rotL(1.2F, -22.0F, 0.0F, 0.0F),
				rotL(1.25F, -19.703F, 0.0F, 0.0F),
				rotL(1.3F, -16.803F, 0.0F, 0.0F),
				rotL(1.35F, -13.411F, 0.0F, 0.0F),
				rotL(1.4F, -9.657F, 0.0F, 0.0F),
				rotL(1.45F, -5.686F, 0.0F, 0.0F),
				rotL(1.5F, -1.65F, 0.0F, 0.0F),
				rotL(1.55F, 2.296F, 0.0F, 0.0F),
				rotL(1.6F, 6.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 20.0F, 0.0F, 0.0F),
				rotL(0.05F, 19.673F, 0.0F, 0.0F),
				rotL(0.1F, 18.706F, 0.0F, 0.0F),
				rotL(0.15F, 17.135F, 0.0F, 0.0F),
				rotL(0.2F, 15.021F, 0.0F, 0.0F),
				rotL(0.25F, 12.445F, 0.0F, 0.0F),
				rotL(0.3F, 9.506F, 0.0F, 0.0F),
				rotL(0.35F, 6.317F, 0.0F, 0.0F),
				rotL(0.4F, 3.0F, 0.0F, 0.0F),
				rotL(0.45F, -0.317F, 0.0F, 0.0F),
				rotL(0.5F, -3.506F, 0.0F, 0.0F),
				rotL(0.55F, -6.445F, 0.0F, 0.0F),
				rotL(0.6F, -9.021F, 0.0F, 0.0F),
				rotL(0.65F, -11.135F, 0.0F, 0.0F),
				rotL(0.7F, -12.706F, 0.0F, 0.0F),
				rotL(0.75F, -13.673F, 0.0F, 0.0F),
				rotL(0.8F, -14.0F, 0.0F, 0.0F),
				rotL(0.85F, -8.991F, 0.0F, 0.0F),
				rotL(0.9F, -3.522F, 0.0F, 0.0F),
				rotL(0.95F, 2.199F, 0.0F, 0.0F),
				rotL(1.0F, 7.95F, 0.0F, 0.0F),
				rotL(1.05F, 13.511F, 0.0F, 0.0F),
				rotL(1.1F, 18.667F, 0.0F, 0.0F),
				rotL(1.15F, 23.222F, 0.0F, 0.0F),
				rotL(1.2F, 27.0F, 0.0F, 0.0F),
				rotL(1.25F, 29.855F, 0.0F, 0.0F),
				rotL(1.3F, 31.679F, 0.0F, 0.0F),
				rotL(1.35F, 32.4F, 0.0F, 0.0F),
				rotL(1.4F, 31.991F, 0.0F, 0.0F),
				rotL(1.45F, 30.469F, 0.0F, 0.0F),
				rotL(1.5F, 27.89F, 0.0F, 0.0F),
				rotL(1.55F, 24.356F, 0.0F, 0.0F),
				rotL(1.6F, 20.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 18.0F, 0.0F, 0.0F),
				rotL(0.05F, 15.313F, 0.0F, 0.0F),
				rotL(0.1F, 12.038F, 0.0F, 0.0F),
				rotL(0.15F, 8.3F, 0.0F, 0.0F),
				rotL(0.2F, 4.243F, 0.0F, 0.0F),
				rotL(0.25F, 0.023F, 0.0F, 0.0F),
				rotL(0.3F, -4.198F, 0.0F, 0.0F),
				rotL(0.35F, -8.258F, 0.0F, 0.0F),
				rotL(0.4F, -12.0F, 0.0F, 0.0F),
				rotL(0.45F, -15.281F, 0.0F, 0.0F),
				rotL(0.5F, -17.975F, 0.0F, 0.0F),
				rotL(0.55F, -19.978F, 0.0F, 0.0F),
				rotL(0.6F, -21.213F, 0.0F, 0.0F),
				rotL(0.65F, -21.633F, 0.0F, 0.0F),
				rotL(0.7F, -21.222F, 0.0F, 0.0F),
				rotL(0.75F, -19.995F, 0.0F, 0.0F),
				rotL(0.8F, -18.0F, 0.0F, 0.0F),
				rotL(0.85F, -17.654F, 0.0F, 0.0F),
				rotL(0.9F, -16.63F, 0.0F, 0.0F),
				rotL(0.95F, -14.966F, 0.0F, 0.0F),
				rotL(1.0F, -12.728F, 0.0F, 0.0F),
				rotL(1.05F, -10.0F, 0.0F, 0.0F),
				rotL(1.1F, -6.888F, 0.0F, 0.0F),
				rotL(1.15F, -3.512F, 0.0F, 0.0F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.25F, 3.512F, 0.0F, 0.0F),
				rotL(1.3F, 6.888F, 0.0F, 0.0F),
				rotL(1.35F, 10.0F, 0.0F, 0.0F),
				rotL(1.4F, 12.728F, 0.0F, 0.0F),
				rotL(1.45F, 14.966F, 0.0F, 0.0F),
				rotL(1.5F, 16.63F, 0.0F, 0.0F),
				rotL(1.55F, 17.654F, 0.0F, 0.0F),
				rotL(1.6F, 18.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("knee_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 10.0F, 0.0F, 0.0F),
				rotL(0.05F, 14.899F, 0.0F, 0.0F),
				rotL(0.1F, 19.265F, 0.0F, 0.0F),
				rotL(0.15F, 22.928F, 0.0F, 0.0F),
				rotL(0.2F, 25.749F, 0.0F, 0.0F),
				rotL(0.25F, 27.618F, 0.0F, 0.0F),
				rotL(0.3F, 28.465F, 0.0F, 0.0F),
				rotL(0.35F, 28.256F, 0.0F, 0.0F),
				rotL(0.4F, 27.0F, 0.0F, 0.0F),
				rotL(0.45F, 24.745F, 0.0F, 0.0F),
				rotL(0.5F, 21.577F, 0.0F, 0.0F),
				rotL(0.55F, 17.618F, 0.0F, 0.0F),
				rotL(0.6F, 13.021F, 0.0F, 0.0F),
				rotL(0.65F, 7.962F, 0.0F, 0.0F),
				rotL(0.7F, 2.635F, 0.0F, 0.0F),
				rotL(0.75F, -2.755F, 0.0F, 0.0F),
				rotL(0.8F, -8.0F, 0.0F, 0.0F),
				rotL(0.85F, -7.827F, 0.0F, 0.0F),
				rotL(0.9F, -7.315F, 0.0F, 0.0F),
				rotL(0.95F, -6.483F, 0.0F, 0.0F),
				rotL(1.0F, -5.364F, 0.0F, 0.0F),
				rotL(1.05F, -4.0F, 0.0F, 0.0F),
				rotL(1.1F, -2.444F, 0.0F, 0.0F),
				rotL(1.15F, -0.756F, 0.0F, 0.0F),
				rotL(1.2F, 1.0F, 0.0F, 0.0F),
				rotL(1.25F, 2.756F, 0.0F, 0.0F),
				rotL(1.3F, 4.444F, 0.0F, 0.0F),
				rotL(1.35F, 6.0F, 0.0F, 0.0F),
				rotL(1.4F, 7.364F, 0.0F, 0.0F),
				rotL(1.45F, 8.483F, 0.0F, 0.0F),
				rotL(1.5F, 9.315F, 0.0F, 0.0F),
				rotL(1.55F, 9.827F, 0.0F, 0.0F),
				rotL(1.6F, 10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 0.0F, 0.0F),
				rotL(0.05F, -17.319F, 0.0F, 0.0F),
				rotL(0.1F, -20.127F, 0.0F, 0.0F),
				rotL(0.15F, -22.315F, 0.0F, 0.0F),
				rotL(0.2F, -23.799F, 0.0F, 0.0F),
				rotL(0.25F, -24.522F, 0.0F, 0.0F),
				rotL(0.3F, -24.457F, 0.0F, 0.0F),
				rotL(0.35F, -23.605F, 0.0F, 0.0F),
				rotL(0.4F, -22.0F, 0.0F, 0.0F),
				rotL(0.45F, -19.703F, 0.0F, 0.0F),
				rotL(0.5F, -16.803F, 0.0F, 0.0F),
				rotL(0.55F, -13.411F, 0.0F, 0.0F),
				rotL(0.6F, -9.657F, 0.0F, 0.0F),
				rotL(0.65F, -5.686F, 0.0F, 0.0F),
				rotL(0.7F, -1.65F, 0.0F, 0.0F),
				rotL(0.75F, 2.296F, 0.0F, 0.0F),
				rotL(0.8F, 6.0F, 0.0F, 0.0F),
				rotL(0.85F, 5.808F, 0.0F, 0.0F),
				rotL(0.9F, 5.239F, 0.0F, 0.0F),
				rotL(0.95F, 4.315F, 0.0F, 0.0F),
				rotL(1.0F, 3.071F, 0.0F, 0.0F),
				rotL(1.05F, 1.556F, 0.0F, 0.0F),
				rotL(1.1F, -0.173F, 0.0F, 0.0F),
				rotL(1.15F, -2.049F, 0.0F, 0.0F),
				rotL(1.2F, -4.0F, 0.0F, 0.0F),
				rotL(1.25F, -5.951F, 0.0F, 0.0F),
				rotL(1.3F, -7.827F, 0.0F, 0.0F),
				rotL(1.35F, -9.556F, 0.0F, 0.0F),
				rotL(1.4F, -11.071F, 0.0F, 0.0F),
				rotL(1.45F, -12.315F, 0.0F, 0.0F),
				rotL(1.5F, -13.239F, 0.0F, 0.0F),
				rotL(1.55F, -13.808F, 0.0F, 0.0F),
				rotL(1.6F, -14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 0.0F, 0.0F),
				rotL(0.05F, -8.991F, 0.0F, 0.0F),
				rotL(0.1F, -3.522F, 0.0F, 0.0F),
				rotL(0.15F, 2.199F, 0.0F, 0.0F),
				rotL(0.2F, 7.95F, 0.0F, 0.0F),
				rotL(0.25F, 13.511F, 0.0F, 0.0F),
				rotL(0.3F, 18.667F, 0.0F, 0.0F),
				rotL(0.35F, 23.222F, 0.0F, 0.0F),
				rotL(0.4F, 27.0F, 0.0F, 0.0F),
				rotL(0.45F, 29.855F, 0.0F, 0.0F),
				rotL(0.5F, 31.679F, 0.0F, 0.0F),
				rotL(0.55F, 32.4F, 0.0F, 0.0F),
				rotL(0.6F, 31.991F, 0.0F, 0.0F),
				rotL(0.65F, 30.469F, 0.0F, 0.0F),
				rotL(0.7F, 27.89F, 0.0F, 0.0F),
				rotL(0.75F, 24.356F, 0.0F, 0.0F),
				rotL(0.8F, 20.0F, 0.0F, 0.0F),
				rotL(0.85F, 19.673F, 0.0F, 0.0F),
				rotL(0.9F, 18.706F, 0.0F, 0.0F),
				rotL(0.95F, 17.135F, 0.0F, 0.0F),
				rotL(1.0F, 15.021F, 0.0F, 0.0F),
				rotL(1.05F, 12.445F, 0.0F, 0.0F),
				rotL(1.1F, 9.506F, 0.0F, 0.0F),
				rotL(1.15F, 6.317F, 0.0F, 0.0F),
				rotL(1.2F, 3.0F, 0.0F, 0.0F),
				rotL(1.25F, -0.317F, 0.0F, 0.0F),
				rotL(1.3F, -3.506F, 0.0F, 0.0F),
				rotL(1.35F, -6.445F, 0.0F, 0.0F),
				rotL(1.4F, -9.021F, 0.0F, 0.0F),
				rotL(1.45F, -11.135F, 0.0F, 0.0F),
				rotL(1.5F, -12.706F, 0.0F, 0.0F),
				rotL(1.55F, -13.673F, 0.0F, 0.0F),
				rotL(1.6F, -14.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.5F, 0.0F, 5.5F),
				rotL(0.05F, -2.31F, 0.683F, 6.378F),
				rotL(0.1F, -1.768F, 1.339F, 7.222F),
				rotL(0.15F, -0.957F, 1.944F, 8.0F),
				rotL(0.2F, 0.0F, 2.475F, 8.682F),
				rotL(0.25F, 0.957F, 2.91F, 9.242F),
				rotL(0.3F, 1.768F, 3.234F, 9.657F),
				rotL(0.35F, 2.31F, 3.433F, 9.914F),
				rotL(0.4F, 2.5F, 3.5F, 10.0F),
				rotL(0.45F, 2.31F, 3.433F, 9.914F),
				rotL(0.5F, 1.768F, 3.234F, 9.657F),
				rotL(0.55F, 0.957F, 2.91F, 9.242F),
				rotL(0.6F, 0.0F, 2.475F, 8.682F),
				rotL(0.65F, -0.957F, 1.944F, 8.0F),
				rotL(0.7F, -1.768F, 1.339F, 7.222F),
				rotL(0.75F, -2.31F, 0.683F, 6.378F),
				rotL(0.8F, -2.5F, 0.0F, 5.5F),
				rotL(0.85F, -2.31F, -0.683F, 4.622F),
				rotL(0.9F, -1.768F, -1.339F, 3.778F),
				rotL(0.95F, -0.957F, -1.944F, 3.0F),
				rotL(1.0F, 0.0F, -2.475F, 2.318F),
				rotL(1.05F, 0.957F, -2.91F, 1.758F),
				rotL(1.1F, 1.768F, -3.234F, 1.343F),
				rotL(1.15F, 2.31F, -3.433F, 1.086F),
				rotL(1.2F, 2.5F, -3.5F, 1.0F),
				rotL(1.25F, 2.31F, -3.433F, 1.086F),
				rotL(1.3F, 1.768F, -3.234F, 1.343F),
				rotL(1.35F, 0.957F, -2.91F, 1.758F),
				rotL(1.4F, 0.0F, -2.475F, 2.318F),
				rotL(1.45F, -0.957F, -1.944F, 3.0F),
				rotL(1.5F, -1.768F, -1.339F, 3.778F),
				rotL(1.55F, -2.31F, -0.683F, 4.622F),
				rotL(1.6F, -2.5F, 0.0F, 5.5F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 1.918F, 2.337F),
				rotL(0.05F, -1.148F, 1.196F, 1.213F),
				rotL(0.1F, -2.121F, 0.428F, 0.044F),
				rotL(0.15F, -2.772F, -0.356F, -1.128F),
				rotL(0.2F, -3.0F, -1.126F, -2.256F),
				rotL(0.25F, -2.772F, -1.853F, -3.297F),
				rotL(0.3F, -2.121F, -2.509F, -4.212F),
				rotL(0.35F, -1.148F, -3.069F, -4.964F),
				rotL(0.4F, 0.0F, -3.51F, -5.526F),
				rotL(0.45F, 1.148F, -3.817F, -5.876F),
				rotL(0.5F, 2.121F, -3.977F, -6.0F),
				rotL(0.55F, 2.772F, -3.984F, -5.893F),
				rotL(0.6F, 3.0F, -3.838F, -5.56F),
				rotL(0.65F, 2.772F, -3.545F, -5.013F),
				rotL(0.7F, 2.121F, -3.115F, -4.274F),
				rotL(0.75F, 1.148F, -2.566F, -3.37F),
				rotL(0.8F, 0.0F, -1.918F, -2.337F),
				rotL(0.85F, -1.148F, -1.196F, -1.213F),
				rotL(0.9F, -2.121F, -0.428F, -0.044F),
				rotL(0.95F, -2.772F, 0.356F, 1.128F),
				rotL(1.0F, -3.0F, 1.126F, 2.256F),
				rotL(1.05F, -2.772F, 1.853F, 3.297F),
				rotL(1.1F, -2.121F, 2.509F, 4.212F),
				rotL(1.15F, -1.148F, 3.069F, 4.964F),
				rotL(1.2F, 0.0F, 3.51F, 5.526F),
				rotL(1.25F, 1.148F, 3.817F, 5.876F),
				rotL(1.3F, 2.121F, 3.977F, 6.0F),
				rotL(1.35F, 2.772F, 3.984F, 5.893F),
				rotL(1.4F, 3.0F, 3.838F, 5.56F),
				rotL(1.45F, 2.772F, 3.545F, 5.013F),
				rotL(1.5F, 2.121F, 3.115F, 4.274F),
				rotL(1.55F, 1.148F, 2.566F, 3.37F),
				rotL(1.6F, 0.0F, 1.918F, 2.337F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.5F, 0.0F, -5.5F),
				rotL(0.05F, -2.31F, 0.683F, -4.622F),
				rotL(0.1F, -1.768F, 1.339F, -3.778F),
				rotL(0.15F, -0.957F, 1.944F, -3.0F),
				rotL(0.2F, 0.0F, 2.475F, -2.318F),
				rotL(0.25F, 0.957F, 2.91F, -1.758F),
				rotL(0.3F, 1.768F, 3.234F, -1.343F),
				rotL(0.35F, 2.31F, 3.433F, -1.086F),
				rotL(0.4F, 2.5F, 3.5F, -1.0F),
				rotL(0.45F, 2.31F, 3.433F, -1.086F),
				rotL(0.5F, 1.768F, 3.234F, -1.343F),
				rotL(0.55F, 0.957F, 2.91F, -1.758F),
				rotL(0.6F, 0.0F, 2.475F, -2.318F),
				rotL(0.65F, -0.957F, 1.944F, -3.0F),
				rotL(0.7F, -1.768F, 1.339F, -3.778F),
				rotL(0.75F, -2.31F, 0.683F, -4.622F),
				rotL(0.8F, -2.5F, 0.0F, -5.5F),
				rotL(0.85F, -2.31F, -0.683F, -6.378F),
				rotL(0.9F, -1.768F, -1.339F, -7.222F),
				rotL(0.95F, -0.957F, -1.944F, -8.0F),
				rotL(1.0F, 0.0F, -2.475F, -8.682F),
				rotL(1.05F, 0.957F, -2.91F, -9.242F),
				rotL(1.1F, 1.768F, -3.234F, -9.657F),
				rotL(1.15F, 2.31F, -3.433F, -9.914F),
				rotL(1.2F, 2.5F, -3.5F, -10.0F),
				rotL(1.25F, 2.31F, -3.433F, -9.914F),
				rotL(1.3F, 1.768F, -3.234F, -9.657F),
				rotL(1.35F, 0.957F, -2.91F, -9.242F),
				rotL(1.4F, 0.0F, -2.475F, -8.682F),
				rotL(1.45F, -0.957F, -1.944F, -8.0F),
				rotL(1.5F, -1.768F, -1.339F, -7.222F),
				rotL(1.55F, -2.31F, -0.683F, -6.378F),
				rotL(1.6F, -2.5F, 0.0F, -5.5F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 1.918F, 2.337F),
				rotL(0.05F, -1.148F, 1.196F, 1.213F),
				rotL(0.1F, -2.121F, 0.428F, 0.044F),
				rotL(0.15F, -2.772F, -0.356F, -1.128F),
				rotL(0.2F, -3.0F, -1.126F, -2.256F),
				rotL(0.25F, -2.772F, -1.853F, -3.297F),
				rotL(0.3F, -2.121F, -2.509F, -4.212F),
				rotL(0.35F, -1.148F, -3.069F, -4.964F),
				rotL(0.4F, 0.0F, -3.51F, -5.526F),
				rotL(0.45F, 1.148F, -3.817F, -5.876F),
				rotL(0.5F, 2.121F, -3.977F, -6.0F),
				rotL(0.55F, 2.772F, -3.984F, -5.893F),
				rotL(0.6F, 3.0F, -3.838F, -5.56F),
				rotL(0.65F, 2.772F, -3.545F, -5.013F),
				rotL(0.7F, 2.121F, -3.115F, -4.274F),
				rotL(0.75F, 1.148F, -2.566F, -3.37F),
				rotL(0.8F, 0.0F, -1.918F, -2.337F),
				rotL(0.85F, -1.148F, -1.196F, -1.213F),
				rotL(0.9F, -2.121F, -0.428F, -0.044F),
				rotL(0.95F, -2.772F, 0.356F, 1.128F),
				rotL(1.0F, -3.0F, 1.126F, 2.256F),
				rotL(1.05F, -2.772F, 1.853F, 3.297F),
				rotL(1.1F, -2.121F, 2.509F, 4.212F),
				rotL(1.15F, -1.148F, 3.069F, 4.964F),
				rotL(1.2F, 0.0F, 3.51F, 5.526F),
				rotL(1.25F, 1.148F, 3.817F, 5.876F),
				rotL(1.3F, 2.121F, 3.977F, 6.0F),
				rotL(1.35F, 2.772F, 3.984F, 5.893F),
				rotL(1.4F, 3.0F, 3.838F, 5.56F),
				rotL(1.45F, 2.772F, 3.545F, 5.013F),
				rotL(1.5F, 2.121F, 3.115F, 4.274F),
				rotL(1.55F, 1.148F, 2.566F, 3.37F),
				rotL(1.6F, 0.0F, 1.918F, 2.337F)
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
			return b.build();
		}
	}

	private static final class SwimAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, -9.0F, -4.5F),
				rotL(0.1F, 0.0F, -8.315F, -4.157F),
				rotL(0.2F, 0.0F, -6.364F, -3.182F),
				rotL(0.3F, 0.0F, -3.444F, -1.722F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, 3.444F, 1.722F),
				rotL(0.6F, 0.0F, 6.364F, 3.182F),
				rotL(0.7F, 0.0F, 8.315F, 4.157F),
				rotL(0.8F, 0.0F, 9.0F, 4.5F),
				rotL(0.9F, 0.0F, 8.315F, 4.157F),
				rotL(1.0F, 0.0F, 6.364F, 3.182F),
				rotL(1.1F, 0.0F, 3.444F, 1.722F),
				rotL(1.2F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, -3.444F, -1.722F),
				rotL(1.4F, 0.0F, -6.364F, -3.182F),
				rotL(1.5F, 0.0F, -8.315F, -4.157F),
				rotL(1.6F, 0.0F, -9.0F, -4.5F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 2.8F, 0.0F),
				posL(0.1F, -0.689F, 3.26F, 0.0F),
				posL(0.2F, -1.273F, 3.45F, 0.0F),
				posL(0.3F, -1.663F, 3.26F, 0.0F),
				posL(0.4F, -1.8F, 2.8F, 0.0F),
				posL(0.5F, -1.663F, 2.34F, 0.0F),
				posL(0.6F, -1.273F, 2.15F, 0.0F),
				posL(0.7F, -0.689F, 2.34F, 0.0F),
				posL(0.8F, 0.0F, 2.8F, 0.0F),
				posL(0.9F, 0.689F, 3.26F, 0.0F),
				posL(1.0F, 1.273F, 3.45F, 0.0F),
				posL(1.1F, 1.663F, 3.26F, 0.0F),
				posL(1.2F, 1.8F, 2.8F, 0.0F),
				posL(1.3F, 1.663F, 2.34F, 0.0F),
				posL(1.4F, 1.273F, 2.15F, 0.0F),
				posL(1.5F, 0.689F, 2.34F, 0.0F),
				posL(1.6F, 0.0F, 2.8F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 16.0F, -5.732F, 0.0F),
				rotL(0.1F, 14.444F, -5.974F, 0.0F),
				rotL(0.2F, 13.8F, -5.307F, 0.0F),
				rotL(0.3F, 14.444F, -3.832F, 0.0F),
				rotL(0.4F, 16.0F, -1.773F, 0.0F),
				rotL(0.5F, 17.556F, 0.555F, 0.0F),
				rotL(0.6F, 18.2F, 2.799F, 0.0F),
				rotL(0.7F, 17.556F, 4.617F, 0.0F),
				rotL(0.8F, 16.0F, 5.732F, 0.0F),
				rotL(0.9F, 14.444F, 5.974F, 0.0F),
				rotL(1.0F, 13.8F, 5.307F, 0.0F),
				rotL(1.1F, 14.444F, 3.832F, 0.0F),
				rotL(1.2F, 16.0F, 1.773F, 0.0F),
				rotL(1.3F, 17.556F, -0.555F, 0.0F),
				rotL(1.4F, 18.2F, -2.799F, 0.0F),
				rotL(1.5F, 17.556F, -4.617F, 0.0F),
				rotL(1.6F, 16.0F, -5.732F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -6.8F, 5.5F, 0.0F),
				rotL(0.1F, -6.273F, 5.081F, 0.0F),
				rotL(0.2F, -5.0F, 3.889F, 0.0F),
				rotL(0.3F, -3.727F, 2.105F, 0.0F),
				rotL(0.4F, -3.2F, 0.0F, 0.0F),
				rotL(0.5F, -3.727F, -2.105F, 0.0F),
				rotL(0.6F, -5.0F, -3.889F, 0.0F),
				rotL(0.7F, -6.273F, -5.081F, 0.0F),
				rotL(0.8F, -6.8F, -5.5F, 0.0F),
				rotL(0.9F, -6.273F, -5.081F, 0.0F),
				rotL(1.0F, -5.0F, -3.889F, 0.0F),
				rotL(1.1F, -3.727F, -2.105F, 0.0F),
				rotL(1.2F, -3.2F, 0.0F, 0.0F),
				rotL(1.3F, -3.727F, 2.105F, 0.0F),
				rotL(1.4F, -5.0F, 3.889F, 0.0F),
				rotL(1.5F, -6.273F, 5.081F, 0.0F),
				rotL(1.6F, -6.8F, 5.5F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -6.382F, 4.9F, 0.0F),
				rotL(0.1F, -6.39F, 4.907F, 0.0F),
				rotL(0.2F, -5.584F, 4.167F, 0.0F),
				rotL(0.3F, -4.436F, 2.793F, 0.0F),
				rotL(0.4F, -3.618F, 0.993F, 0.0F),
				rotL(0.5F, -3.61F, -0.958F, 0.0F),
				rotL(0.6F, -4.416F, -2.763F, 0.0F),
				rotL(0.7F, -5.564F, -4.147F, 0.0F),
				rotL(0.8F, -6.382F, -4.9F, 0.0F),
				rotL(0.9F, -6.39F, -4.907F, 0.0F),
				rotL(1.0F, -5.584F, -4.167F, 0.0F),
				rotL(1.1F, -4.436F, -2.793F, 0.0F),
				rotL(1.2F, -3.618F, -0.993F, 0.0F),
				rotL(1.3F, -3.61F, 0.958F, 0.0F),
				rotL(1.4F, -4.416F, 2.763F, 0.0F),
				rotL(1.5F, -5.564F, 4.147F, 0.0F),
				rotL(1.6F, -6.382F, 4.9F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -4.0F, 4.145F, 0.0F),
				rotL(0.1F, -4.0F, 4.5F, 0.0F),
				rotL(0.2F, -4.0F, 4.17F, 0.0F),
				rotL(0.3F, -4.0F, 3.205F, 0.0F),
				rotL(0.4F, -4.0F, 1.752F, 0.0F),
				rotL(0.5F, -4.0F, 0.033F, 0.0F),
				rotL(0.6F, -4.0F, -1.692F, 0.0F),
				rotL(0.7F, -4.0F, -3.159F, 0.0F),
				rotL(0.8F, -4.0F, -4.145F, 0.0F),
				rotL(0.9F, -4.0F, -4.5F, 0.0F),
				rotL(1.0F, -4.0F, -4.17F, 0.0F),
				rotL(1.1F, -4.0F, -3.205F, 0.0F),
				rotL(1.2F, -4.0F, -1.752F, 0.0F),
				rotL(1.3F, -4.0F, -0.033F, 0.0F),
				rotL(1.4F, -4.0F, 1.692F, 0.0F),
				rotL(1.5F, -4.0F, 3.159F, 0.0F),
				rotL(1.6F, -4.0F, 4.145F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -4.0F, 3.51F, 3.5F),
				rotL(0.1F, -2.586F, 3.977F, 3.234F),
				rotL(0.2F, -2.0F, 3.838F, 2.475F),
				rotL(0.3F, -2.586F, 3.115F, 1.339F),
				rotL(0.4F, -4.0F, 1.918F, 0.0F),
				rotL(0.5F, -5.414F, 0.428F, -1.339F),
				rotL(0.6F, -6.0F, -1.126F, -2.475F),
				rotL(0.7F, -5.414F, -2.509F, -3.234F),
				rotL(0.8F, -4.0F, -3.51F, -3.5F),
				rotL(0.9F, -2.586F, -3.977F, -3.234F),
				rotL(1.0F, -2.0F, -3.838F, -2.475F),
				rotL(1.1F, -2.586F, -3.115F, -1.339F),
				rotL(1.2F, -4.0F, -1.918F, 0.0F),
				rotL(1.3F, -5.414F, -0.428F, 1.339F),
				rotL(1.4F, -6.0F, 1.126F, 2.475F),
				rotL(1.5F, -5.414F, 2.509F, 3.234F),
				rotL(1.6F, -4.0F, 3.51F, 3.5F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 1.5F, 0.0F, 0.0F),
				rotL(0.1F, 3.413F, 0.0F, 0.0F),
				rotL(0.2F, 5.036F, 0.0F, 0.0F),
				rotL(0.3F, 6.119F, 0.0F, 0.0F),
				rotL(0.4F, 6.5F, 0.0F, 0.0F),
				rotL(0.5F, 6.119F, 0.0F, 0.0F),
				rotL(0.6F, 5.036F, 0.0F, 0.0F),
				rotL(0.7F, 3.413F, 0.0F, 0.0F),
				rotL(0.8F, 1.5F, 0.0F, 0.0F),
				rotL(0.9F, 1.5F, 0.0F, 0.0F),
				rotL(1.0F, 1.5F, 0.0F, 0.0F),
				rotL(1.1F, 1.5F, 0.0F, 0.0F),
				rotL(1.2F, 1.5F, 0.0F, 0.0F),
				rotL(1.3F, 1.5F, 0.0F, 0.0F),
				rotL(1.4F, 1.5F, 0.0F, 0.0F),
				rotL(1.5F, 1.5F, 0.0F, 0.0F),
				rotL(1.6F, 1.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 4.0F, 18.0F, 14.0F),
				rotL(0.1F, 4.0F, 16.469F, 13.696F),
				rotL(0.2F, 4.0F, 15.172F, 12.828F),
				rotL(0.3F, 4.0F, 14.304F, 11.531F),
				rotL(0.4F, 4.0F, 14.0F, 10.0F),
				rotL(0.5F, 4.0F, 14.304F, 8.469F),
				rotL(0.6F, 4.0F, 15.172F, 7.172F),
				rotL(0.7F, 4.0F, 16.469F, 6.304F),
				rotL(0.8F, 4.0F, 18.0F, 6.0F),
				rotL(0.9F, 4.0F, 19.531F, 6.304F),
				rotL(1.0F, 4.0F, 20.828F, 7.172F),
				rotL(1.1F, 4.0F, 21.696F, 8.469F),
				rotL(1.2F, 4.0F, 22.0F, 10.0F),
				rotL(1.3F, 4.0F, 21.696F, 11.531F),
				rotL(1.4F, 4.0F, 20.828F, 12.828F),
				rotL(1.5F, 4.0F, 19.531F, 13.696F),
				rotL(1.6F, 4.0F, 18.0F, 14.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -8.0F, 16.397F, 8.0F),
				rotL(0.1F, -8.0F, 14.535F, 8.0F),
				rotL(0.2F, -8.0F, 12.592F, 8.0F),
				rotL(0.3F, -8.0F, 10.863F, 8.0F),
				rotL(0.4F, -8.0F, 9.612F, 8.0F),
				rotL(0.5F, -8.0F, 9.029F, 8.0F),
				rotL(0.6F, -8.0F, 9.202F, 8.0F),
				rotL(0.7F, -8.0F, 10.106F, 8.0F),
				rotL(0.8F, -8.0F, 11.603F, 8.0F),
				rotL(0.9F, -8.0F, 13.465F, 8.0F),
				rotL(1.0F, -8.0F, 15.408F, 8.0F),
				rotL(1.1F, -8.0F, 17.137F, 8.0F),
				rotL(1.2F, -8.0F, 18.388F, 8.0F),
				rotL(1.3F, -8.0F, 18.971F, 8.0F),
				rotL(1.4F, -8.0F, 18.798F, 8.0F),
				rotL(1.5F, -8.0F, 17.894F, 8.0F),
				rotL(1.6F, -8.0F, 16.397F, 8.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 4.0F, -18.0F, -6.0F),
				rotL(0.1F, 4.0F, -19.531F, -6.304F),
				rotL(0.2F, 4.0F, -20.828F, -7.172F),
				rotL(0.3F, 4.0F, -21.696F, -8.469F),
				rotL(0.4F, 4.0F, -22.0F, -10.0F),
				rotL(0.5F, 4.0F, -21.696F, -11.531F),
				rotL(0.6F, 4.0F, -20.828F, -12.828F),
				rotL(0.7F, 4.0F, -19.531F, -13.696F),
				rotL(0.8F, 4.0F, -18.0F, -14.0F),
				rotL(0.9F, 4.0F, -16.469F, -13.696F),
				rotL(1.0F, 4.0F, -15.172F, -12.828F),
				rotL(1.1F, 4.0F, -14.304F, -11.531F),
				rotL(1.2F, 4.0F, -14.0F, -10.0F),
				rotL(1.3F, 4.0F, -14.304F, -8.469F),
				rotL(1.4F, 4.0F, -15.172F, -7.172F),
				rotL(1.5F, 4.0F, -16.469F, -6.304F),
				rotL(1.6F, 4.0F, -18.0F, -6.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -8.0F, -11.603F, -8.0F),
				rotL(0.1F, -8.0F, -13.465F, -8.0F),
				rotL(0.2F, -8.0F, -15.408F, -8.0F),
				rotL(0.3F, -8.0F, -17.137F, -8.0F),
				rotL(0.4F, -8.0F, -18.388F, -8.0F),
				rotL(0.5F, -8.0F, -18.971F, -8.0F),
				rotL(0.6F, -8.0F, -18.798F, -8.0F),
				rotL(0.7F, -8.0F, -17.894F, -8.0F),
				rotL(0.8F, -8.0F, -16.397F, -8.0F),
				rotL(0.9F, -8.0F, -14.535F, -8.0F),
				rotL(1.0F, -8.0F, -12.592F, -8.0F),
				rotL(1.1F, -8.0F, -10.863F, -8.0F),
				rotL(1.2F, -8.0F, -9.612F, -8.0F),
				rotL(1.3F, -8.0F, -9.029F, -8.0F),
				rotL(1.4F, -8.0F, -9.202F, -8.0F),
				rotL(1.5F, -8.0F, -10.106F, -8.0F),
				rotL(1.6F, -8.0F, -11.603F, -8.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 48.0F, 0.0F, 0.0F),
				rotL(0.1F, 45.704F, 0.0F, 0.0F),
				rotL(0.2F, 43.757F, 0.0F, 0.0F),
				rotL(0.3F, 42.457F, 0.0F, 0.0F),
				rotL(0.4F, 42.0F, 0.0F, 0.0F),
				rotL(0.5F, 42.457F, 0.0F, 0.0F),
				rotL(0.6F, 43.757F, 0.0F, 0.0F),
				rotL(0.7F, 45.704F, 0.0F, 0.0F),
				rotL(0.8F, 48.0F, 0.0F, 0.0F),
				rotL(0.9F, 50.296F, 0.0F, 0.0F),
				rotL(1.0F, 52.243F, 0.0F, 0.0F),
				rotL(1.1F, 53.543F, 0.0F, 0.0F),
				rotL(1.2F, 54.0F, 0.0F, 0.0F),
				rotL(1.3F, 53.543F, 0.0F, 0.0F),
				rotL(1.4F, 52.243F, 0.0F, 0.0F),
				rotL(1.5F, 50.296F, 0.0F, 0.0F),
				rotL(1.6F, 48.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("knee_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 17.0F, 0.0F, 0.0F),
				rotL(0.1F, 17.381F, 0.0F, 0.0F),
				rotL(0.2F, 18.464F, 0.0F, 0.0F),
				rotL(0.3F, 20.087F, 0.0F, 0.0F),
				rotL(0.4F, 22.0F, 0.0F, 0.0F),
				rotL(0.5F, 23.913F, 0.0F, 0.0F),
				rotL(0.6F, 25.536F, 0.0F, 0.0F),
				rotL(0.7F, 26.619F, 0.0F, 0.0F),
				rotL(0.8F, 27.0F, 0.0F, 0.0F),
				rotL(0.9F, 26.619F, 0.0F, 0.0F),
				rotL(1.0F, 25.536F, 0.0F, 0.0F),
				rotL(1.1F, 23.913F, 0.0F, 0.0F),
				rotL(1.2F, 22.0F, 0.0F, 0.0F),
				rotL(1.3F, 20.087F, 0.0F, 0.0F),
				rotL(1.4F, 18.464F, 0.0F, 0.0F),
				rotL(1.5F, 17.381F, 0.0F, 0.0F),
				rotL(1.6F, 17.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -24.0F, 0.0F, 0.0F),
				rotL(0.1F, -24.0F, 0.0F, 0.0F),
				rotL(0.2F, -24.0F, 0.0F, 0.0F),
				rotL(0.3F, -24.0F, 0.0F, 0.0F),
				rotL(0.4F, -24.0F, 0.0F, 0.0F),
				rotL(0.5F, -24.0F, 0.0F, 0.0F),
				rotL(0.6F, -24.0F, 0.0F, 0.0F),
				rotL(0.7F, -24.0F, 0.0F, 0.0F),
				rotL(0.8F, -24.0F, 0.0F, 0.0F),
				rotL(0.9F, -24.0F, 0.0F, 0.0F),
				rotL(1.0F, -24.0F, 0.0F, 0.0F),
				rotL(1.1F, -24.0F, 0.0F, 0.0F),
				rotL(1.2F, -24.0F, 0.0F, 0.0F),
				rotL(1.3F, -24.0F, 0.0F, 0.0F),
				rotL(1.4F, -24.0F, 0.0F, 0.0F),
				rotL(1.5F, -24.0F, 0.0F, 0.0F),
				rotL(1.6F, -24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 35.835F, 0.0F, 0.0F),
				rotL(0.1F, 32.857F, 0.0F, 0.0F),
				rotL(0.2F, 29.748F, 0.0F, 0.0F),
				rotL(0.3F, 26.982F, 0.0F, 0.0F),
				rotL(0.4F, 24.979F, 0.0F, 0.0F),
				rotL(0.5F, 24.046F, 0.0F, 0.0F),
				rotL(0.6F, 24.324F, 0.0F, 0.0F),
				rotL(0.7F, 25.77F, 0.0F, 0.0F),
				rotL(0.8F, 28.165F, 0.0F, 0.0F),
				rotL(0.9F, 31.143F, 0.0F, 0.0F),
				rotL(1.0F, 34.252F, 0.0F, 0.0F),
				rotL(1.1F, 37.018F, 0.0F, 0.0F),
				rotL(1.2F, 39.021F, 0.0F, 0.0F),
				rotL(1.3F, 39.954F, 0.0F, 0.0F),
				rotL(1.4F, 39.676F, 0.0F, 0.0F),
				rotL(1.5F, 38.23F, 0.0F, 0.0F),
				rotL(1.6F, 35.835F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 48.0F, 0.0F, 0.0F),
				rotL(0.1F, 50.296F, 0.0F, 0.0F),
				rotL(0.2F, 52.243F, 0.0F, 0.0F),
				rotL(0.3F, 53.543F, 0.0F, 0.0F),
				rotL(0.4F, 54.0F, 0.0F, 0.0F),
				rotL(0.5F, 53.543F, 0.0F, 0.0F),
				rotL(0.6F, 52.243F, 0.0F, 0.0F),
				rotL(0.7F, 50.296F, 0.0F, 0.0F),
				rotL(0.8F, 48.0F, 0.0F, 0.0F),
				rotL(0.9F, 45.704F, 0.0F, 0.0F),
				rotL(1.0F, 43.757F, 0.0F, 0.0F),
				rotL(1.1F, 42.457F, 0.0F, 0.0F),
				rotL(1.2F, 42.0F, 0.0F, 0.0F),
				rotL(1.3F, 42.457F, 0.0F, 0.0F),
				rotL(1.4F, 43.757F, 0.0F, 0.0F),
				rotL(1.5F, 45.704F, 0.0F, 0.0F),
				rotL(1.6F, 48.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("knee_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 27.0F, 0.0F, 0.0F),
				rotL(0.1F, 26.619F, 0.0F, 0.0F),
				rotL(0.2F, 25.536F, 0.0F, 0.0F),
				rotL(0.3F, 23.913F, 0.0F, 0.0F),
				rotL(0.4F, 22.0F, 0.0F, 0.0F),
				rotL(0.5F, 20.087F, 0.0F, 0.0F),
				rotL(0.6F, 18.464F, 0.0F, 0.0F),
				rotL(0.7F, 17.381F, 0.0F, 0.0F),
				rotL(0.8F, 17.0F, 0.0F, 0.0F),
				rotL(0.9F, 17.381F, 0.0F, 0.0F),
				rotL(1.0F, 18.464F, 0.0F, 0.0F),
				rotL(1.1F, 20.087F, 0.0F, 0.0F),
				rotL(1.2F, 22.0F, 0.0F, 0.0F),
				rotL(1.3F, 23.913F, 0.0F, 0.0F),
				rotL(1.4F, 25.536F, 0.0F, 0.0F),
				rotL(1.5F, 26.619F, 0.0F, 0.0F),
				rotL(1.6F, 27.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -24.0F, 0.0F, 0.0F),
				rotL(0.1F, -24.0F, 0.0F, 0.0F),
				rotL(0.2F, -24.0F, 0.0F, 0.0F),
				rotL(0.3F, -24.0F, 0.0F, 0.0F),
				rotL(0.4F, -24.0F, 0.0F, 0.0F),
				rotL(0.5F, -24.0F, 0.0F, 0.0F),
				rotL(0.6F, -24.0F, 0.0F, 0.0F),
				rotL(0.7F, -24.0F, 0.0F, 0.0F),
				rotL(0.8F, -24.0F, 0.0F, 0.0F),
				rotL(0.9F, -24.0F, 0.0F, 0.0F),
				rotL(1.0F, -24.0F, 0.0F, 0.0F),
				rotL(1.1F, -24.0F, 0.0F, 0.0F),
				rotL(1.2F, -24.0F, 0.0F, 0.0F),
				rotL(1.3F, -24.0F, 0.0F, 0.0F),
				rotL(1.4F, -24.0F, 0.0F, 0.0F),
				rotL(1.5F, -24.0F, 0.0F, 0.0F),
				rotL(1.6F, -24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 28.165F, 0.0F, 0.0F),
				rotL(0.1F, 31.143F, 0.0F, 0.0F),
				rotL(0.2F, 34.252F, 0.0F, 0.0F),
				rotL(0.3F, 37.018F, 0.0F, 0.0F),
				rotL(0.4F, 39.021F, 0.0F, 0.0F),
				rotL(0.5F, 39.954F, 0.0F, 0.0F),
				rotL(0.6F, 39.676F, 0.0F, 0.0F),
				rotL(0.7F, 38.23F, 0.0F, 0.0F),
				rotL(0.8F, 35.835F, 0.0F, 0.0F),
				rotL(0.9F, 32.857F, 0.0F, 0.0F),
				rotL(1.0F, 29.748F, 0.0F, 0.0F),
				rotL(1.1F, 26.982F, 0.0F, 0.0F),
				rotL(1.2F, 24.979F, 0.0F, 0.0F),
				rotL(1.3F, 24.046F, 0.0F, 0.0F),
				rotL(1.4F, 24.324F, 0.0F, 0.0F),
				rotL(1.5F, 25.77F, 0.0F, 0.0F),
				rotL(1.6F, 28.165F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.043F, 5.227F, 3.836F),
				rotL(0.1F, 1.523F, 1.567F, 4.444F),
				rotL(0.2F, 0.575F, -2.332F, 4.376F),
				rotL(0.3F, 0.755F, -5.876F, 3.641F),
				rotL(0.4F, 1.957F, -8.525F, 2.352F),
				rotL(0.5F, 3.477F, -9.877F, 0.705F),
				rotL(0.6F, 4.425F, -9.724F, -1.05F),
				rotL(0.7F, 4.245F, -8.091F, -2.644F),
				rotL(0.8F, 3.043F, -5.227F, -3.836F),
				rotL(0.9F, 1.523F, -1.567F, -4.444F),
				rotL(1.0F, 0.575F, 2.332F, -4.376F),
				rotL(1.1F, 0.755F, 5.876F, -3.641F),
				rotL(1.2F, 1.957F, 8.525F, -2.352F),
				rotL(1.3F, 3.477F, 9.877F, -0.705F),
				rotL(1.4F, 4.425F, 9.724F, 1.05F),
				rotL(1.5F, 4.245F, 8.091F, 2.644F),
				rotL(1.6F, 3.043F, 5.227F, 3.836F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.545F, 11.586F, 2.041F),
				rotL(0.1F, 2.034F, 8.447F, 3.421F),
				rotL(0.2F, 0.795F, 4.023F, 4.279F),
				rotL(0.3F, 0.555F, -1.014F, 4.486F),
				rotL(0.4F, 1.455F, -5.897F, 4.01F),
				rotL(0.5F, 2.966F, -9.882F, 2.924F),
				rotL(0.6F, 4.205F, -12.362F, 1.392F),
				rotL(0.7F, 4.445F, -12.96F, -0.351F),
				rotL(0.8F, 3.545F, -11.586F, -2.041F),
				rotL(0.9F, 2.034F, -8.447F, -3.421F),
				rotL(1.0F, 0.795F, -4.023F, -4.279F),
				rotL(1.1F, 0.555F, 1.014F, -4.486F),
				rotL(1.2F, 1.455F, 5.897F, -4.01F),
				rotL(1.3F, 2.966F, 9.882F, -2.924F),
				rotL(1.4F, 4.205F, 12.362F, -1.392F),
				rotL(1.5F, 4.445F, 12.96F, 0.351F),
				rotL(1.6F, 3.545F, 11.586F, 2.041F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.969F, 15.95F, -0.356F),
				rotL(0.1F, 2.579F, 15.22F, 1.388F),
				rotL(0.2F, 1.143F, 12.173F, 2.92F),
				rotL(0.3F, 0.502F, 7.273F, 4.008F),
				rotL(0.4F, 1.031F, 1.266F, 4.486F),
				rotL(0.5F, 2.421F, -4.934F, 4.281F),
				rotL(0.6F, 3.857F, -10.383F, 3.424F),
				rotL(0.7F, 4.498F, -14.251F, 2.046F),
				rotL(0.8F, 3.969F, -15.95F, 0.356F),
				rotL(0.9F, 2.579F, -15.22F, -1.388F),
				rotL(1.0F, 1.143F, -12.173F, -2.92F),
				rotL(1.1F, 0.502F, -7.273F, -4.008F),
				rotL(1.2F, 1.031F, -1.266F, -4.486F),
				rotL(1.3F, 2.421F, 4.934F, -4.281F),
				rotL(1.4F, 3.857F, 10.383F, -3.424F),
				rotL(1.5F, 4.498F, 14.251F, -2.046F),
				rotL(1.6F, 3.969F, 15.95F, -0.356F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 4.282F, 15.361F, -2.648F),
				rotL(0.1F, 3.119F, 18.471F, -1.054F),
				rotL(0.2F, 1.593F, 18.769F, 0.7F),
				rotL(0.3F, 0.598F, 16.209F, 2.348F),
				rotL(0.4F, 0.718F, 11.182F, 3.638F),
				rotL(0.5F, 1.881F, 4.452F, 4.375F),
				rotL(0.6F, 3.407F, -2.956F, 4.445F),
				rotL(0.7F, 4.402F, -9.913F, 3.839F),
				rotL(0.8F, 4.282F, -15.361F, 2.648F),
				rotL(0.9F, 3.119F, -18.471F, 1.054F),
				rotL(1.0F, 1.593F, -18.769F, -0.7F),
				rotL(1.1F, 0.598F, -16.209F, -2.348F),
				rotL(1.2F, 0.718F, -11.182F, -3.638F),
				rotL(1.3F, 1.881F, -4.452F, -4.375F),
				rotL(1.4F, 3.407F, 2.956F, -4.445F),
				rotL(1.5F, 4.402F, 9.913F, -3.839F),
				rotL(1.6F, 4.282F, 15.361F, -2.648F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 4.462F, 8.397F, -4.159F),
				rotL(0.1F, 3.612F, 15.539F, -3.185F),
				rotL(0.2F, 2.111F, 20.316F, -1.727F),
				rotL(0.3F, 0.838F, 22.0F, -0.005F),
				rotL(0.4F, 0.538F, 20.335F, 1.717F),
				rotL(0.5F, 1.388F, 15.574F, 3.178F),
				rotL(0.6F, 2.889F, 8.442F, 4.156F),
				rotL(0.7F, 4.162F, 0.024F, 4.5F),
				rotL(0.8F, 4.462F, -8.397F, 4.159F),
				rotL(0.9F, 3.612F, -15.539F, 3.185F),
				rotL(1.0F, 2.111F, -20.316F, 1.727F),
				rotL(1.1F, 0.838F, -22.0F, 0.005F),
				rotL(1.2F, 0.538F, -20.335F, -1.717F),
				rotL(1.3F, 1.388F, -15.574F, -3.178F),
				rotL(1.4F, 2.889F, -8.442F, -4.156F),
				rotL(1.5F, 4.162F, -0.024F, -4.5F),
				rotL(1.6F, 4.462F, 8.397F, -4.159F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 4.494F, -4.101F, -4.444F),
				rotL(0.1F, 4.022F, 6.036F, -4.377F),
				rotL(0.2F, 2.658F, 15.254F, -3.644F),
				rotL(0.3F, 1.202F, 22.151F, -2.356F),
				rotL(0.4F, 0.506F, 25.674F, -0.71F),
				rotL(0.5F, 0.978F, 25.29F, 1.045F),
				rotL(0.6F, 2.342F, 21.055F, 2.64F),
				rotL(0.7F, 3.798F, 13.614F, 3.834F),
				rotL(0.8F, 4.494F, 4.101F, 4.444F),
				rotL(0.9F, 4.022F, -6.036F, 4.377F),
				rotL(1.0F, 2.658F, -15.254F, 3.644F),
				rotL(1.1F, 1.202F, -22.151F, 2.356F),
				rotL(1.2F, 0.506F, -25.674F, 0.71F),
				rotL(1.3F, 0.978F, -25.29F, -1.045F),
				rotL(1.4F, 2.342F, -21.055F, -2.64F),
				rotL(1.5F, 3.798F, -13.614F, -3.834F),
				rotL(1.6F, 4.494F, -4.101F, -4.444F)
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
			return b.build();
		}
	}

	private static final class FlyAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.1F, 0.0F, 0.0F, 0.0F),
				rotL(0.2F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 0.0F, 0.0F, 0.0F),
				rotL(0.4F, 0.0F, 0.0F, 0.0F),
				rotL(0.5F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, 0.0F, 0.0F),
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
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 5.92F, -0.8F),
				posL(0.1F, 0.0F, 5.11F, -0.739F),
				posL(0.2F, 0.0F, 4.314F, -0.566F),
				posL(0.3F, 0.0F, 3.653F, -0.306F),
				posL(0.4F, 0.0F, 3.227F, 0.0F),
				posL(0.5F, 0.0F, 3.102F, 0.306F),
				posL(0.6F, 0.0F, 3.296F, 0.566F),
				posL(0.7F, 0.0F, 3.78F, 0.739F),
				posL(0.8F, 0.0F, 4.48F, 0.8F),
				posL(0.9F, 0.0F, 5.29F, 0.739F),
				posL(1.0F, 0.0F, 6.086F, 0.566F),
				posL(1.1F, 0.0F, 6.747F, 0.306F),
				posL(1.2F, 0.0F, 7.173F, 0.0F),
				posL(1.3F, 0.0F, 7.298F, -0.306F),
				posL(1.4F, 0.0F, 7.104F, -0.566F),
				posL(1.5F, 0.0F, 6.62F, -0.739F),
				posL(1.6F, 0.0F, 5.92F, -0.8F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.5F, 0.0F, 0.0F),
				rotL(0.1F, 5.843F, 0.0F, 0.0F),
				rotL(0.2F, 6.818F, 0.0F, 0.0F),
				rotL(0.3F, 8.278F, 0.0F, 0.0F),
				rotL(0.4F, 10.0F, 0.0F, 0.0F),
				rotL(0.5F, 11.722F, 0.0F, 0.0F),
				rotL(0.6F, 13.182F, 0.0F, 0.0F),
				rotL(0.7F, 14.157F, 0.0F, 0.0F),
				rotL(0.8F, 14.5F, 0.0F, 0.0F),
				rotL(0.9F, 14.157F, 0.0F, 0.0F),
				rotL(1.0F, 13.182F, 0.0F, 0.0F),
				rotL(1.1F, 11.722F, 0.0F, 0.0F),
				rotL(1.2F, 10.0F, 0.0F, 0.0F),
				rotL(1.3F, 8.278F, 0.0F, 0.0F),
				rotL(1.4F, 6.818F, 0.0F, 0.0F),
				rotL(1.5F, 5.843F, 0.0F, 0.0F),
				rotL(1.6F, 5.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.112F, 0.0F, 0.0F),
				rotL(0.1F, -1.011F, 0.0F, 0.0F),
				rotL(0.2F, -1.289F, 0.0F, 0.0F),
				rotL(0.3F, -1.903F, 0.0F, 0.0F),
				rotL(0.4F, -2.761F, 0.0F, 0.0F),
				rotL(0.5F, -3.731F, 0.0F, 0.0F),
				rotL(0.6F, -4.666F, 0.0F, 0.0F),
				rotL(0.7F, -5.424F, 0.0F, 0.0F),
				rotL(0.8F, -5.888F, 0.0F, 0.0F),
				rotL(0.9F, -5.989F, 0.0F, 0.0F),
				rotL(1.0F, -5.711F, 0.0F, 0.0F),
				rotL(1.1F, -5.097F, 0.0F, 0.0F),
				rotL(1.2F, -4.239F, 0.0F, 0.0F),
				rotL(1.3F, -3.269F, 0.0F, 0.0F),
				rotL(1.4F, -2.334F, 0.0F, 0.0F),
				rotL(1.5F, -1.576F, 0.0F, 0.0F),
				rotL(1.6F, -1.112F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.245F, 0.0F, 0.0F),
				rotL(0.1F, -1.012F, 0.0F, 0.0F),
				rotL(0.2F, -1.081F, 0.0F, 0.0F),
				rotL(0.3F, -1.442F, 0.0F, 0.0F),
				rotL(0.4F, -2.041F, 0.0F, 0.0F),
				rotL(0.5F, -2.786F, 0.0F, 0.0F),
				rotL(0.6F, -3.563F, 0.0F, 0.0F),
				rotL(0.7F, -4.255F, 0.0F, 0.0F),
				rotL(0.8F, -4.755F, 0.0F, 0.0F),
				rotL(0.9F, -4.988F, 0.0F, 0.0F),
				rotL(1.0F, -4.919F, 0.0F, 0.0F),
				rotL(1.1F, -4.558F, 0.0F, 0.0F),
				rotL(1.2F, -3.959F, 0.0F, 0.0F),
				rotL(1.3F, -3.214F, 0.0F, 0.0F),
				rotL(1.4F, -2.437F, 0.0F, 0.0F),
				rotL(1.5F, -1.745F, 0.0F, 0.0F),
				rotL(1.6F, -1.245F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.0F, 0.0F, 0.0F),
				rotL(0.1F, -2.0F, 0.0F, 0.0F),
				rotL(0.2F, -2.0F, 0.0F, 0.0F),
				rotL(0.3F, -2.0F, 0.0F, 0.0F),
				rotL(0.4F, -2.0F, 0.0F, 0.0F),
				rotL(0.5F, -2.0F, 0.0F, 0.0F),
				rotL(0.6F, -2.0F, 0.0F, 0.0F),
				rotL(0.7F, -2.0F, 0.0F, 0.0F),
				rotL(0.8F, -2.0F, 0.0F, 0.0F),
				rotL(0.9F, -2.0F, 0.0F, 0.0F),
				rotL(1.0F, -2.0F, 0.0F, 0.0F),
				rotL(1.1F, -2.0F, 0.0F, 0.0F),
				rotL(1.2F, -2.0F, 0.0F, 0.0F),
				rotL(1.3F, -2.0F, 0.0F, 0.0F),
				rotL(1.4F, -2.0F, 0.0F, 0.0F),
				rotL(1.5F, -2.0F, 0.0F, 0.0F),
				rotL(1.6F, -2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -5.263F, 0.0F, 0.0F),
				rotL(0.1F, -5.5F, 0.0F, 0.0F),
				rotL(0.2F, -5.28F, 0.0F, 0.0F),
				rotL(0.3F, -4.637F, 0.0F, 0.0F),
				rotL(0.4F, -3.668F, 0.0F, 0.0F),
				rotL(0.5F, -2.522F, 0.0F, 0.0F),
				rotL(0.6F, -1.372F, 0.0F, 0.0F),
				rotL(0.7F, -0.394F, 0.0F, 0.0F),
				rotL(0.8F, 0.263F, 0.0F, 0.0F),
				rotL(0.9F, 0.5F, 0.0F, 0.0F),
				rotL(1.0F, 0.28F, 0.0F, 0.0F),
				rotL(1.1F, -0.363F, 0.0F, 0.0F),
				rotL(1.2F, -1.332F, 0.0F, 0.0F),
				rotL(1.3F, -2.478F, 0.0F, 0.0F),
				rotL(1.4F, -3.628F, 0.0F, 0.0F),
				rotL(1.5F, -4.606F, 0.0F, 0.0F),
				rotL(1.6F, -5.263F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 2.0F, 0.0F, 0.0F),
				rotL(0.1F, 2.0F, 0.0F, 0.0F),
				rotL(0.2F, 2.0F, 0.0F, 0.0F),
				rotL(0.3F, 2.0F, 0.0F, 0.0F),
				rotL(0.4F, 2.0F, 0.0F, 0.0F),
				rotL(0.5F, 2.0F, 0.0F, 0.0F),
				rotL(0.6F, 2.0F, 0.0F, 0.0F),
				rotL(0.7F, 2.0F, 0.0F, 0.0F),
				rotL(0.8F, 2.0F, 0.0F, 0.0F),
				rotL(0.9F, 4.487F, 0.0F, 0.0F),
				rotL(1.0F, 6.596F, 0.0F, 0.0F),
				rotL(1.1F, 8.005F, 0.0F, 0.0F),
				rotL(1.2F, 8.5F, 0.0F, 0.0F),
				rotL(1.3F, 8.005F, 0.0F, 0.0F),
				rotL(1.4F, 6.596F, 0.0F, 0.0F),
				rotL(1.5F, 4.487F, 0.0F, 0.0F),
				rotL(1.6F, 2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 1.0F, 46.0F),
				rotL(0.1F, -13.543F, 1.685F, 58.629F),
				rotL(0.2F, -12.243F, 3.636F, 69.335F),
				rotL(0.3F, -10.296F, 6.556F, 76.488F),
				rotL(0.4F, -8.0F, 10.0F, 79.0F),
				rotL(0.5F, -5.704F, 13.444F, 76.488F),
				rotL(0.6F, -3.757F, 16.364F, 69.335F),
				rotL(0.7F, -2.457F, 18.315F, 58.629F),
				rotL(0.8F, -2.0F, 19.0F, 46.0F),
				rotL(0.9F, -2.457F, 18.315F, 33.371F),
				rotL(1.0F, -3.757F, 16.364F, 22.665F),
				rotL(1.1F, -5.704F, 13.444F, 15.512F),
				rotL(1.2F, -8.0F, 10.0F, 13.0F),
				rotL(1.3F, -10.296F, 6.556F, 15.512F),
				rotL(1.4F, -12.243F, 3.636F, 22.665F),
				rotL(1.5F, -13.543F, 1.685F, 33.371F),
				rotL(1.6F, -14.0F, 1.0F, 46.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -30.856F, -38.0F, -83.72F),
				rotL(0.1F, -32.583F, -33.408F, -75.084F),
				rotL(0.2F, -34.526F, -29.515F, -65.37F),
				rotL(0.3F, -36.389F, -26.913F, -56.056F),
				rotL(0.4F, -37.888F, -26.0F, -48.561F),
				rotL(0.5F, -38.795F, -26.913F, -44.025F),
				rotL(0.6F, -38.972F, -29.515F, -43.139F),
				rotL(0.7F, -38.392F, -33.408F, -46.038F),
				rotL(0.8F, -37.144F, -38.0F, -52.28F),
				rotL(0.9F, -35.417F, -38.0F, -60.916F),
				rotL(1.0F, -33.474F, -38.0F, -70.63F),
				rotL(1.1F, -31.611F, -38.0F, -79.944F),
				rotL(1.2F, -30.112F, -38.0F, -87.439F),
				rotL(1.3F, -29.205F, -38.0F, -91.975F),
				rotL(1.4F, -29.028F, -38.0F, -92.861F),
				rotL(1.5F, -29.608F, -38.0F, -89.962F),
				rotL(1.6F, -30.856F, -38.0F, -83.72F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, -1.0F, -46.0F),
				rotL(0.1F, -13.543F, -1.685F, -58.629F),
				rotL(0.2F, -12.243F, -3.636F, -69.335F),
				rotL(0.3F, -10.296F, -6.556F, -76.488F),
				rotL(0.4F, -8.0F, -10.0F, -79.0F),
				rotL(0.5F, -5.704F, -13.444F, -76.488F),
				rotL(0.6F, -3.757F, -16.364F, -69.335F),
				rotL(0.7F, -2.457F, -18.315F, -58.629F),
				rotL(0.8F, -2.0F, -19.0F, -46.0F),
				rotL(0.9F, -2.457F, -18.315F, -33.371F),
				rotL(1.0F, -3.757F, -16.364F, -22.665F),
				rotL(1.1F, -5.704F, -13.444F, -15.512F),
				rotL(1.2F, -8.0F, -10.0F, -13.0F),
				rotL(1.3F, -10.296F, -6.556F, -15.512F),
				rotL(1.4F, -12.243F, -3.636F, -22.665F),
				rotL(1.5F, -13.543F, -1.685F, -33.371F),
				rotL(1.6F, -14.0F, -1.0F, -46.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -30.856F, 38.0F, 83.72F),
				rotL(0.1F, -32.583F, 33.408F, 75.084F),
				rotL(0.2F, -34.526F, 29.515F, 65.37F),
				rotL(0.3F, -36.389F, 26.913F, 56.056F),
				rotL(0.4F, -37.888F, 26.0F, 48.561F),
				rotL(0.5F, -38.795F, 26.913F, 44.025F),
				rotL(0.6F, -38.972F, 29.515F, 43.139F),
				rotL(0.7F, -38.392F, 33.408F, 46.038F),
				rotL(0.8F, -37.144F, 38.0F, 52.28F),
				rotL(0.9F, -35.417F, 38.0F, 60.916F),
				rotL(1.0F, -33.474F, 38.0F, 70.63F),
				rotL(1.1F, -31.611F, 38.0F, 79.944F),
				rotL(1.2F, -30.112F, 38.0F, 87.439F),
				rotL(1.3F, -29.205F, 38.0F, 91.975F),
				rotL(1.4F, -29.028F, 38.0F, 92.861F),
				rotL(1.5F, -29.608F, 38.0F, 89.962F),
				rotL(1.6F, -30.856F, 38.0F, 83.72F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 29.5F, 0.0F, 0.0F),
				rotL(0.1F, 29.843F, 0.0F, 0.0F),
				rotL(0.2F, 30.818F, 0.0F, 0.0F),
				rotL(0.3F, 32.278F, 0.0F, 0.0F),
				rotL(0.4F, 34.0F, 0.0F, 0.0F),
				rotL(0.5F, 35.722F, 0.0F, 0.0F),
				rotL(0.6F, 37.182F, 0.0F, 0.0F),
				rotL(0.7F, 38.157F, 0.0F, 0.0F),
				rotL(0.8F, 38.5F, 0.0F, 0.0F),
				rotL(0.9F, 38.157F, 0.0F, 0.0F),
				rotL(1.0F, 37.182F, 0.0F, 0.0F),
				rotL(1.1F, 35.722F, 0.0F, 0.0F),
				rotL(1.2F, 34.0F, 0.0F, 0.0F),
				rotL(1.3F, 32.278F, 0.0F, 0.0F),
				rotL(1.4F, 30.818F, 0.0F, 0.0F),
				rotL(1.5F, 29.843F, 0.0F, 0.0F),
				rotL(1.6F, 29.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("knee_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, 0.0F),
				rotL(0.1F, 24.0F, 0.0F, 0.0F),
				rotL(0.2F, 24.0F, 0.0F, 0.0F),
				rotL(0.3F, 24.0F, 0.0F, 0.0F),
				rotL(0.4F, 24.0F, 0.0F, 0.0F),
				rotL(0.5F, 24.0F, 0.0F, 0.0F),
				rotL(0.6F, 24.0F, 0.0F, 0.0F),
				rotL(0.7F, 24.0F, 0.0F, 0.0F),
				rotL(0.8F, 24.0F, 0.0F, 0.0F),
				rotL(0.9F, 24.0F, 0.0F, 0.0F),
				rotL(1.0F, 24.0F, 0.0F, 0.0F),
				rotL(1.1F, 24.0F, 0.0F, 0.0F),
				rotL(1.2F, 24.0F, 0.0F, 0.0F),
				rotL(1.3F, 24.0F, 0.0F, 0.0F),
				rotL(1.4F, 24.0F, 0.0F, 0.0F),
				rotL(1.5F, 24.0F, 0.0F, 0.0F),
				rotL(1.6F, 24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
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
				rotL(1.2F, -26.0F, 0.0F, 0.0F),
				rotL(1.3F, -26.0F, 0.0F, 0.0F),
				rotL(1.4F, -26.0F, 0.0F, 0.0F),
				rotL(1.5F, -26.0F, 0.0F, 0.0F),
				rotL(1.6F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.1F, 24.087F, 0.0F, 0.0F),
				rotL(0.2F, 22.464F, 0.0F, 0.0F),
				rotL(0.3F, 21.381F, 0.0F, 0.0F),
				rotL(0.4F, 21.0F, 0.0F, 0.0F),
				rotL(0.5F, 21.381F, 0.0F, 0.0F),
				rotL(0.6F, 22.464F, 0.0F, 0.0F),
				rotL(0.7F, 24.087F, 0.0F, 0.0F),
				rotL(0.8F, 26.0F, 0.0F, 0.0F),
				rotL(0.9F, 27.913F, 0.0F, 0.0F),
				rotL(1.0F, 29.536F, 0.0F, 0.0F),
				rotL(1.1F, 30.619F, 0.0F, 0.0F),
				rotL(1.2F, 31.0F, 0.0F, 0.0F),
				rotL(1.3F, 30.619F, 0.0F, 0.0F),
				rotL(1.4F, 29.536F, 0.0F, 0.0F),
				rotL(1.5F, 27.913F, 0.0F, 0.0F),
				rotL(1.6F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 29.5F, 0.0F, 0.0F),
				rotL(0.1F, 29.843F, 0.0F, 0.0F),
				rotL(0.2F, 30.818F, 0.0F, 0.0F),
				rotL(0.3F, 32.278F, 0.0F, 0.0F),
				rotL(0.4F, 34.0F, 0.0F, 0.0F),
				rotL(0.5F, 35.722F, 0.0F, 0.0F),
				rotL(0.6F, 37.182F, 0.0F, 0.0F),
				rotL(0.7F, 38.157F, 0.0F, 0.0F),
				rotL(0.8F, 38.5F, 0.0F, 0.0F),
				rotL(0.9F, 38.157F, 0.0F, 0.0F),
				rotL(1.0F, 37.182F, 0.0F, 0.0F),
				rotL(1.1F, 35.722F, 0.0F, 0.0F),
				rotL(1.2F, 34.0F, 0.0F, 0.0F),
				rotL(1.3F, 32.278F, 0.0F, 0.0F),
				rotL(1.4F, 30.818F, 0.0F, 0.0F),
				rotL(1.5F, 29.843F, 0.0F, 0.0F),
				rotL(1.6F, 29.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("knee_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, 0.0F),
				rotL(0.1F, 24.0F, 0.0F, 0.0F),
				rotL(0.2F, 24.0F, 0.0F, 0.0F),
				rotL(0.3F, 24.0F, 0.0F, 0.0F),
				rotL(0.4F, 24.0F, 0.0F, 0.0F),
				rotL(0.5F, 24.0F, 0.0F, 0.0F),
				rotL(0.6F, 24.0F, 0.0F, 0.0F),
				rotL(0.7F, 24.0F, 0.0F, 0.0F),
				rotL(0.8F, 24.0F, 0.0F, 0.0F),
				rotL(0.9F, 24.0F, 0.0F, 0.0F),
				rotL(1.0F, 24.0F, 0.0F, 0.0F),
				rotL(1.1F, 24.0F, 0.0F, 0.0F),
				rotL(1.2F, 24.0F, 0.0F, 0.0F),
				rotL(1.3F, 24.0F, 0.0F, 0.0F),
				rotL(1.4F, 24.0F, 0.0F, 0.0F),
				rotL(1.5F, 24.0F, 0.0F, 0.0F),
				rotL(1.6F, 24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
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
				rotL(1.2F, -26.0F, 0.0F, 0.0F),
				rotL(1.3F, -26.0F, 0.0F, 0.0F),
				rotL(1.4F, -26.0F, 0.0F, 0.0F),
				rotL(1.5F, -26.0F, 0.0F, 0.0F),
				rotL(1.6F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.1F, 24.087F, 0.0F, 0.0F),
				rotL(0.2F, 22.464F, 0.0F, 0.0F),
				rotL(0.3F, 21.381F, 0.0F, 0.0F),
				rotL(0.4F, 21.0F, 0.0F, 0.0F),
				rotL(0.5F, 21.381F, 0.0F, 0.0F),
				rotL(0.6F, 22.464F, 0.0F, 0.0F),
				rotL(0.7F, 24.087F, 0.0F, 0.0F),
				rotL(0.8F, 26.0F, 0.0F, 0.0F),
				rotL(0.9F, 27.913F, 0.0F, 0.0F),
				rotL(1.0F, 29.536F, 0.0F, 0.0F),
				rotL(1.1F, 30.619F, 0.0F, 0.0F),
				rotL(1.2F, 31.0F, 0.0F, 0.0F),
				rotL(1.3F, 30.619F, 0.0F, 0.0F),
				rotL(1.4F, 29.536F, 0.0F, 0.0F),
				rotL(1.5F, 27.913F, 0.0F, 0.0F),
				rotL(1.6F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.558F, -2.322F, 0.0F),
				rotL(0.1F, 1.947F, -2.5F, 0.0F),
				rotL(0.2F, 0.344F, -2.297F, 0.0F),
				rotL(0.3F, -1.007F, -1.745F, 0.0F),
				rotL(0.4F, -1.9F, -0.927F, 0.0F),
				rotL(0.5F, -2.2F, 0.032F, 0.0F),
				rotL(0.6F, -1.86F, 0.986F, 0.0F),
				rotL(0.7F, -0.932F, 1.79F, 0.0F),
				rotL(0.8F, 0.442F, 2.322F, 0.0F),
				rotL(0.9F, 2.053F, 2.5F, 0.0F),
				rotL(1.0F, 3.656F, 2.297F, 0.0F),
				rotL(1.1F, 5.007F, 1.745F, 0.0F),
				rotL(1.2F, 5.9F, 0.927F, 0.0F),
				rotL(1.3F, 6.2F, -0.032F, 0.0F),
				rotL(1.4F, 5.86F, -0.986F, 0.0F),
				rotL(1.5F, 4.932F, -1.79F, 0.0F),
				rotL(1.6F, 3.558F, -2.322F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 4.893F, -1.812F, 0.0F),
				rotL(0.1F, 3.508F, -2.333F, 0.0F),
				rotL(0.2F, 1.893F, -2.499F, 0.0F),
				rotL(0.3F, 0.295F, -2.285F, 0.0F),
				rotL(0.4F, -1.044F, -1.722F, 0.0F),
				rotL(0.5F, -1.92F, -0.898F, 0.0F),
				rotL(0.6F, -2.199F, 0.063F, 0.0F),
				rotL(0.7F, -1.838F, 1.015F, 0.0F),
				rotL(0.8F, -0.893F, 1.812F, 0.0F),
				rotL(0.9F, 0.492F, 2.333F, 0.0F),
				rotL(1.0F, 2.107F, 2.499F, 0.0F),
				rotL(1.1F, 3.705F, 2.285F, 0.0F),
				rotL(1.2F, 5.044F, 1.722F, 0.0F),
				rotL(1.3F, 5.92F, 0.898F, 0.0F),
				rotL(1.4F, 6.199F, -0.063F, 0.0F),
				rotL(1.5F, 5.838F, -1.015F, 0.0F),
				rotL(1.6F, 4.893F, -1.812F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.816F, -1.044F, 0.0F),
				rotL(0.1F, 4.855F, -1.834F, 0.0F),
				rotL(0.2F, 3.458F, -2.344F, 0.0F),
				rotL(0.3F, 1.84F, -2.498F, 0.0F),
				rotL(0.4F, 0.246F, -2.272F, 0.0F),
				rotL(0.5F, -1.081F, -1.699F, 0.0F),
				rotL(0.6F, -1.939F, -0.868F, 0.0F),
				rotL(0.7F, -2.197F, 0.095F, 0.0F),
				rotL(0.8F, -1.816F, 1.044F, 0.0F),
				rotL(0.9F, -0.855F, 1.834F, 0.0F),
				rotL(1.0F, 0.542F, 2.344F, 0.0F),
				rotL(1.1F, 2.16F, 2.498F, 0.0F),
				rotL(1.2F, 3.754F, 2.272F, 0.0F),
				rotL(1.3F, 5.081F, 1.699F, 0.0F),
				rotL(1.4F, 5.939F, 0.868F, 0.0F),
				rotL(1.5F, 6.197F, -0.095F, 0.0F),
				rotL(1.6F, 5.816F, -1.044F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 6.195F, -0.127F, 0.0F),
				rotL(0.1F, 5.794F, -1.073F, 0.0F),
				rotL(0.2F, 4.815F, -1.855F, 0.0F),
				rotL(0.3F, 3.408F, -2.355F, 0.0F),
				rotL(0.4F, 1.787F, -2.497F, 0.0F),
				rotL(0.5F, 0.198F, -2.258F, 0.0F),
				rotL(0.6F, -1.117F, -1.676F, 0.0F),
				rotL(0.7F, -1.957F, -0.838F, 0.0F),
				rotL(0.8F, -2.195F, 0.127F, 0.0F),
				rotL(0.9F, -1.794F, 1.073F, 0.0F),
				rotL(1.0F, -0.815F, 1.855F, 0.0F),
				rotL(1.1F, 0.592F, 2.355F, 0.0F),
				rotL(1.2F, 2.213F, 2.497F, 0.0F),
				rotL(1.3F, 3.802F, 2.258F, 0.0F),
				rotL(1.4F, 5.117F, 1.676F, 0.0F),
				rotL(1.5F, 5.957F, 0.838F, 0.0F),
				rotL(1.6F, 6.195F, -0.127F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.974F, 0.808F, 0.0F),
				rotL(0.1F, 6.192F, -0.159F, 0.0F),
				rotL(0.2F, 5.77F, -1.101F, 0.0F),
				rotL(0.3F, 4.775F, -1.876F, 0.0F),
				rotL(0.4F, 3.358F, -2.366F, 0.0F),
				rotL(0.5F, 1.733F, -2.495F, 0.0F),
				rotL(0.6F, 0.15F, -2.244F, 0.0F),
				rotL(0.7F, -1.152F, -1.652F, 0.0F),
				rotL(0.8F, -1.974F, -0.808F, 0.0F),
				rotL(0.9F, -2.192F, 0.159F, 0.0F),
				rotL(1.0F, -1.77F, 1.101F, 0.0F),
				rotL(1.1F, -0.775F, 1.876F, 0.0F),
				rotL(1.2F, 0.642F, 2.366F, 0.0F),
				rotL(1.3F, 2.267F, 2.495F, 0.0F),
				rotL(1.4F, 3.85F, 2.244F, 0.0F),
				rotL(1.5F, 5.152F, 1.652F, 0.0F),
				rotL(1.6F, 5.974F, 0.808F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.187F, 1.628F, 0.0F),
				rotL(0.1F, 5.991F, 0.778F, 0.0F),
				rotL(0.2F, 6.188F, -0.19F, 0.0F),
				rotL(0.3F, 5.747F, -1.13F, 0.0F),
				rotL(0.4F, 4.735F, -1.897F, 0.0F),
				rotL(0.5F, 3.307F, -2.376F, 0.0F),
				rotL(0.6F, 1.68F, -2.493F, 0.0F),
				rotL(0.7F, 0.102F, -2.23F, 0.0F),
				rotL(0.8F, -1.187F, -1.628F, 0.0F),
				rotL(0.9F, -1.991F, -0.778F, 0.0F),
				rotL(1.0F, -2.188F, 0.19F, 0.0F),
				rotL(1.1F, -1.747F, 1.13F, 0.0F),
				rotL(1.2F, -0.735F, 1.897F, 0.0F),
				rotL(1.3F, 0.693F, 2.376F, 0.0F),
				rotL(1.4F, 2.32F, 2.493F, 0.0F),
				rotL(1.5F, 3.898F, 2.23F, 0.0F),
				rotL(1.6F, 5.187F, 1.628F, 0.0F)
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
			return b.build();
		}
	}

	private static final class TakeoffAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 0.0F, 0.0F, 0.0F),
				rotL(0.84F, 0.0F, 0.0F, 0.0F),
				rotL(1.18F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1_p0(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.025F, 0.0F, 0.079F, 0.056F),
				posL(0.05F, 0.0F, 0.129F, 0.112F),
				posL(0.075F, 0.0F, 0.166F, 0.166F),
				posL(0.1F, 0.0F, 0.189F, 0.219F),
				posL(0.125F, 0.0F, 0.198F, 0.27F),
				posL(0.15F, 0.0F, 0.193F, 0.319F),
				posL(0.175F, 0.0F, 0.173F, 0.365F),
				posL(0.2F, 0.0F, 0.149F, 0.408F),
				posL(0.225F, 0.0F, 0.117F, 0.447F),
				posL(0.25F, 0.0F, 0.069F, 0.483F),
				posL(0.275F, 0.0F, 0.004F, 0.514F),
				posL(0.3F, 0.0F, -0.076F, 0.541F),
				posL(0.325F, 0.0F, -0.172F, 0.563F),
				posL(0.35F, 0.0F, -0.284F, 0.58F),
				posL(0.375F, 0.0F, -0.412F, 0.592F),
				posL(0.4F, 0.0F, -0.557F, 0.598F),
				posL(0.425F, 0.0F, 0.002F, 0.575F),
				posL(0.45F, 0.0F, 0.205F, 0.45F),
				posL(0.475F, 0.0F, 0.934F, 0.325F),
				posL(0.5F, 0.0F, 1.543F, 0.2F),
				posL(0.525F, 0.0F, 2.029F, 0.075F),
				posL(0.55F, 0.0F, 2.392F, -0.05F),
				posL(0.575F, 0.0F, 2.636F, -0.175F),
				posL(0.6F, 0.0F, 2.761F, -0.3F),
				posL(0.625F, 0.0F, 3.273F, -0.425F),
				posL(0.65F, 0.0F, 3.907F, -0.55F),
				posL(0.675F, 0.0F, 4.523F, -0.675F),
				posL(0.7F, 0.0F, 5.1F, -0.8F),
				posL(0.725F, 0.0F, 5.618F, -0.925F),
				posL(0.75F, 0.0F, 6.058F, -1.05F),
				posL(0.775F, 0.0F, 6.406F, -1.175F),
				posL(0.8F, 0.0F, 6.649F, -1.3F),
				posL(0.825F, 0.0F, 6.779F, -1.425F),
				posL(0.85F, 0.0F, 6.8F, -1.5F),
				posL(0.875F, 0.0F, 6.794F, -1.495F),
				posL(0.9F, 0.0F, 6.782F, -1.486F),
				posL(0.925F, 0.0F, 6.764F, -1.472F),
				posL(0.95F, 0.0F, 6.741F, -1.453F),
				posL(0.975F, 0.0F, 6.712F, -1.43F),
				posL(1.0F, 0.0F, 6.678F, -1.403F),
				posL(1.025F, 0.0F, 6.64F, -1.373F),
				posL(1.05F, 0.0F, 6.598F, -1.339F),
				posL(1.075F, 0.0F, 6.552F, -1.303F),
				posL(1.1F, 0.0F, 6.504F, -1.264F),
				posL(1.125F, 0.0F, 6.454F, -1.224F),
				posL(1.15F, 0.0F, 6.402F, -1.183F),
				posL(1.175F, 0.0F, 6.35F, -1.142F),
				posL(1.2F, 0.0F, 6.297F, -1.1F),
				posL(1.225F, 0.0F, 6.246F, -1.059F),
				posL(1.25F, 0.0F, 6.196F, -1.02F),
				posL(1.275F, 0.0F, 6.149F, -0.982F),
				posL(1.3F, 0.0F, 6.105F, -0.947F),
				posL(1.325F, 0.0F, 6.064F, -0.915F),
				posL(1.35F, 0.0F, 6.027F, -0.885F),
				posL(1.375F, 0.0F, 5.996F, -0.86F),
				posL(1.4F, 0.0F, 5.969F, -0.839F),
				posL(1.425F, 0.0F, 5.948F, -0.822F),
				posL(1.45F, 0.0F, 5.932F, -0.81F),
				posL(1.475F, 0.0F, 5.923F, -0.802F)
			));
		}
		private static void ch_1_p1(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(1.5F, 0.0F, 5.92F, -0.8F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			List<Keyframe> kfs = new ArrayList<>(61);
			ch_1_p0(kfs);
			ch_1_p1(kfs);
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION, kfs.toArray(new Keyframe[0])));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 12.0F, 0.0F, 0.0F),
				rotL(0.84F, -8.0F, 0.0F, 0.0F),
				rotL(1.18F, 4.0F, 0.0F, 0.0F),
				rotL(1.5F, 5.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -8.0F, 0.0F, 0.0F),
				rotL(0.84F, -4.0F, 0.0F, 0.0F),
				rotL(1.18F, -2.0F, 0.0F, 0.0F),
				rotL(1.5F, -1.112F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -6.0F, 0.0F, 0.0F),
				rotL(0.84F, -3.0F, 0.0F, 0.0F),
				rotL(1.18F, -2.0F, 0.0F, 0.0F),
				rotL(1.5F, -1.245F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -4.0F, 0.0F, 0.0F),
				rotL(0.84F, -2.0F, 0.0F, 0.0F),
				rotL(1.18F, -2.0F, 0.0F, 0.0F),
				rotL(1.5F, -2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 4.0F, 0.0F, 0.0F),
				rotL(0.84F, 8.0F, 0.0F, 0.0F),
				rotL(1.18F, -3.0F, 0.0F, 0.0F),
				rotL(1.5F, -5.263F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 18.0F, 0.0F, 0.0F),
				rotL(0.84F, 28.0F, 0.0F, 0.0F),
				rotL(1.18F, 8.0F, 0.0F, 0.0F),
				rotL(1.5F, 2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 2.5F, 0.0F, 0.0F),
				rotL(0.84F, -1.5F, 0.0F, 0.0F),
				rotL(1.18F, 1.8F, -1.5F, 0.0F),
				rotL(1.5F, 3.558F, -2.322F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 2.5F, 0.0F, 0.0F),
				rotL(0.84F, -1.5F, 0.0F, 0.0F),
				rotL(1.18F, 1.8F, -1.5F, 0.0F),
				rotL(1.5F, 4.893F, -1.812F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 2.5F, 0.0F, 0.0F),
				rotL(0.84F, -1.5F, 0.0F, 0.0F),
				rotL(1.18F, 1.8F, -1.5F, 0.0F),
				rotL(1.5F, 5.816F, -1.044F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 2.5F, 0.0F, 0.0F),
				rotL(0.84F, -1.5F, 0.0F, 0.0F),
				rotL(1.18F, 1.8F, -1.5F, 0.0F),
				rotL(1.5F, 6.195F, -0.127F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 2.5F, 0.0F, 0.0F),
				rotL(0.84F, -1.5F, 0.0F, 0.0F),
				rotL(1.18F, 1.8F, -1.5F, 0.0F),
				rotL(1.5F, 5.974F, 0.808F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 2.5F, 0.0F, 0.0F),
				rotL(0.84F, -1.5F, 0.0F, 0.0F),
				rotL(1.18F, 1.8F, -1.5F, 0.0F),
				rotL(1.5F, 5.187F, 1.628F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -20.0F, 0.0F, 0.0F),
				rotL(0.84F, 42.0F, 0.0F, 0.0F),
				rotL(1.18F, 32.0F, 0.0F, 0.0F),
				rotL(1.5F, 29.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("knee_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -24.0F, 0.0F, 0.0F),
				rotL(0.84F, 28.0F, 0.0F, 0.0F),
				rotL(1.18F, 25.0F, 0.0F, 0.0F),
				rotL(1.5F, 24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 20.0F, 0.0F, 0.0F),
				rotL(0.84F, -30.0F, 0.0F, 0.0F),
				rotL(1.18F, -27.0F, 0.0F, 0.0F),
				rotL(1.5F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 24.0F, 0.0F, 0.0F),
				rotL(0.84F, 34.0F, 0.0F, 0.0F),
				rotL(1.18F, 28.0F, 0.0F, 0.0F),
				rotL(1.5F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -20.0F, 0.0F, 0.0F),
				rotL(0.84F, 42.0F, 0.0F, 0.0F),
				rotL(1.18F, 32.0F, 0.0F, 0.0F),
				rotL(1.5F, 29.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("knee_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -24.0F, 0.0F, 0.0F),
				rotL(0.84F, 28.0F, 0.0F, 0.0F),
				rotL(1.18F, 25.0F, 0.0F, 0.0F),
				rotL(1.5F, 24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 20.0F, 0.0F, 0.0F),
				rotL(0.84F, -30.0F, 0.0F, 0.0F),
				rotL(1.18F, -27.0F, 0.0F, 0.0F),
				rotL(1.5F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, 24.0F, 0.0F, 0.0F),
				rotL(0.84F, 34.0F, 0.0F, 0.0F),
				rotL(1.18F, 28.0F, 0.0F, 0.0F),
				rotL(1.5F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -12.0F, 18.0F, 76.0F),
				rotL(0.84F, -4.0F, -14.0F, 12.0F),
				rotL(1.18F, -10.0F, 10.0F, 66.0F),
				rotL(1.5F, -14.0F, 1.0F, 46.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -36.0F, -26.0F, -42.0F),
				rotL(0.84F, -28.0F, -44.0F, -92.0F),
				rotL(1.18F, -36.0F, -30.0F, -50.0F),
				rotL(1.5F, -30.856F, -38.0F, -83.72F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -12.0F, -18.0F, -76.0F),
				rotL(0.84F, -4.0F, 14.0F, -12.0F),
				rotL(1.18F, -10.0F, -10.0F, -66.0F),
				rotL(1.5F, -14.0F, -1.0F, -46.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.42F, -36.0F, 26.0F, 42.0F),
				rotL(0.84F, -28.0F, 44.0F, 92.0F),
				rotL(1.18F, -36.0F, 30.0F, 50.0F),
				rotL(1.5F, -30.856F, 38.0F, 83.72F)
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
			return b.build();
		}
	}

	private static final class LandingAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 0.0F, 0.0F, 0.0F),
				rotL(0.72F, 0.0F, 0.0F, 0.0F),
				rotL(0.96F, 0.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1_p0(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(0.0F, 0.0F, 5.92F, -0.8F),
				posL(0.025F, 0.0F, 5.904F, -0.797F),
				posL(0.05F, 0.0F, 5.861F, -0.786F),
				posL(0.075F, 0.0F, 5.794F, -0.769F),
				posL(0.1F, 0.0F, 5.705F, -0.746F),
				posL(0.125F, 0.0F, 5.598F, -0.717F),
				posL(0.15F, 0.0F, 5.471F, -0.681F),
				posL(0.175F, 0.0F, 5.324F, -0.64F),
				posL(0.2F, 0.0F, 5.154F, -0.595F),
				posL(0.225F, 0.0F, 4.956F, -0.544F),
				posL(0.25F, 0.0F, 4.726F, -0.491F),
				posL(0.275F, 0.0F, 4.458F, -0.433F),
				posL(0.3F, 0.0F, 4.146F, -0.374F),
				posL(0.325F, 0.0F, 3.783F, -0.312F),
				posL(0.35F, 0.0F, 3.361F, -0.25F),
				posL(0.375F, 0.0F, 2.877F, -0.187F),
				posL(0.4F, 0.0F, 2.323F, -0.125F),
				posL(0.425F, 0.0F, 1.81F, -0.064F),
				posL(0.45F, 0.0F, 1.471F, -0.005F),
				posL(0.475F, 0.0F, 1.13F, 0.052F),
				posL(0.5F, 0.0F, 0.848F, 0.105F),
				posL(0.525F, 0.0F, 0.608F, 0.154F),
				posL(0.55F, 0.0F, 0.392F, 0.199F),
				posL(0.575F, 0.0F, 0.204F, 0.239F),
				posL(0.6F, 0.0F, 0.046F, 0.273F),
				posL(0.625F, 0.0F, -0.081F, 0.301F),
				posL(0.65F, 0.0F, -0.175F, 0.323F),
				posL(0.675F, 0.0F, -0.234F, 0.339F),
				posL(0.7F, 0.0F, -0.208F, 0.348F),
				posL(0.725F, 0.0F, -0.168F, 0.348F),
				posL(0.75F, 0.0F, -0.199F, 0.337F),
				posL(0.775F, 0.0F, -0.237F, 0.325F),
				posL(0.8F, 0.0F, -0.28F, 0.314F),
				posL(0.825F, 0.0F, -0.33F, 0.303F),
				posL(0.85F, 0.0F, -0.385F, 0.292F),
				posL(0.875F, 0.0F, -0.447F, 0.28F),
				posL(0.9F, 0.0F, -0.515F, 0.269F),
				posL(0.925F, 0.0F, -0.588F, 0.258F),
				posL(0.95F, 0.0F, -0.668F, 0.247F),
				posL(0.975F, 0.0F, -0.63F, 0.236F),
				posL(1.0F, 0.0F, -0.518F, 0.224F),
				posL(1.025F, 0.0F, -0.415F, 0.213F),
				posL(1.05F, 0.0F, -0.321F, 0.202F),
				posL(1.075F, 0.0F, -0.236F, 0.191F),
				posL(1.1F, 0.0F, -0.16F, 0.179F),
				posL(1.125F, 0.0F, -0.093F, 0.168F),
				posL(1.15F, 0.0F, -0.035F, 0.157F),
				posL(1.175F, 0.0F, 0.014F, 0.146F),
				posL(1.2F, 0.0F, 0.054F, 0.135F),
				posL(1.225F, 0.0F, 0.085F, 0.123F),
				posL(1.25F, 0.0F, 0.107F, 0.112F),
				posL(1.275F, 0.0F, 0.127F, 0.101F),
				posL(1.3F, 0.0F, 0.145F, 0.09F),
				posL(1.325F, 0.0F, 0.156F, 0.079F),
				posL(1.35F, 0.0F, 0.158F, 0.067F),
				posL(1.375F, 0.0F, 0.152F, 0.056F),
				posL(1.4F, 0.0F, 0.139F, 0.045F),
				posL(1.425F, 0.0F, 0.118F, 0.034F),
				posL(1.45F, 0.0F, 0.089F, 0.022F),
				posL(1.475F, 0.0F, 0.052F, 0.011F)
			));
		}
		private static void ch_1_p1(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			List<Keyframe> kfs = new ArrayList<>(61);
			ch_1_p0(kfs);
			ch_1_p1(kfs);
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION, kfs.toArray(new Keyframe[0])));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.5F, 0.0F, 0.0F),
				rotL(0.48F, -20.0F, 0.0F, 0.0F),
				rotL(0.72F, -8.0F, 0.0F, 0.0F),
				rotL(0.96F, 10.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.112F, 0.0F, 0.0F),
				rotL(0.48F, -6.0F, 0.0F, 0.0F),
				rotL(0.72F, -4.0F, 0.0F, 0.0F),
				rotL(0.96F, -6.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.245F, 0.0F, 0.0F),
				rotL(0.48F, -4.0F, 0.0F, 0.0F),
				rotL(0.72F, -3.0F, 0.0F, 0.0F),
				rotL(0.96F, -4.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.0F, 0.0F, 0.0F),
				rotL(0.48F, -2.0F, 0.0F, 0.0F),
				rotL(0.72F, -2.0F, 0.0F, 0.0F),
				rotL(0.96F, -2.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -5.263F, 0.0F, 0.0F),
				rotL(0.48F, 14.0F, 0.0F, 0.0F),
				rotL(0.72F, 8.0F, 0.0F, 0.0F),
				rotL(0.96F, 2.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 2.0F, 0.0F, 0.0F),
				rotL(0.48F, 16.0F, 0.0F, 0.0F),
				rotL(0.72F, 10.0F, 0.0F, 0.0F),
				rotL(0.96F, 4.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.558F, -2.322F, 0.0F),
				rotL(0.48F, 3.8F, 0.0F, 0.0F),
				rotL(0.72F, 2.8F, 0.0F, 0.0F),
				rotL(0.96F, 1.5F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 4.893F, -1.812F, 0.0F),
				rotL(0.48F, 3.8F, 0.0F, 0.0F),
				rotL(0.72F, 2.8F, 0.0F, 0.0F),
				rotL(0.96F, 1.5F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.816F, -1.044F, 0.0F),
				rotL(0.48F, 3.8F, 0.0F, 0.0F),
				rotL(0.72F, 2.8F, 0.0F, 0.0F),
				rotL(0.96F, 1.5F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 6.195F, -0.127F, 0.0F),
				rotL(0.48F, 3.8F, 0.0F, 0.0F),
				rotL(0.72F, 2.8F, 0.0F, 0.0F),
				rotL(0.96F, 1.5F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.974F, 0.808F, 0.0F),
				rotL(0.48F, 3.8F, 0.0F, 0.0F),
				rotL(0.72F, 2.8F, 0.0F, 0.0F),
				rotL(0.96F, 1.5F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.187F, 1.628F, 0.0F),
				rotL(0.48F, 3.8F, 0.0F, 0.0F),
				rotL(0.72F, 2.8F, 0.0F, 0.0F),
				rotL(0.96F, 1.5F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 29.5F, 0.0F, 0.0F),
				rotL(0.48F, -24.0F, 0.0F, 0.0F),
				rotL(0.72F, -14.0F, 0.0F, 0.0F),
				rotL(0.96F, -20.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("knee_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, 0.0F),
				rotL(0.48F, -10.0F, 0.0F, 0.0F),
				rotL(0.72F, -12.0F, 0.0F, 0.0F),
				rotL(0.96F, -22.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, 0.0F, 0.0F),
				rotL(0.48F, 12.0F, 0.0F, 0.0F),
				rotL(0.72F, 10.0F, 0.0F, 0.0F),
				rotL(0.96F, 18.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.48F, 14.0F, 0.0F, 0.0F),
				rotL(0.72F, 16.0F, 0.0F, 0.0F),
				rotL(0.96F, 24.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 29.5F, 0.0F, 0.0F),
				rotL(0.48F, -24.0F, 0.0F, 0.0F),
				rotL(0.72F, -14.0F, 0.0F, 0.0F),
				rotL(0.96F, -20.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("knee_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, 0.0F),
				rotL(0.48F, -10.0F, 0.0F, 0.0F),
				rotL(0.72F, -12.0F, 0.0F, 0.0F),
				rotL(0.96F, -22.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, 0.0F, 0.0F),
				rotL(0.48F, 12.0F, 0.0F, 0.0F),
				rotL(0.72F, 10.0F, 0.0F, 0.0F),
				rotL(0.96F, 18.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.48F, 14.0F, 0.0F, 0.0F),
				rotL(0.72F, 16.0F, 0.0F, 0.0F),
				rotL(0.96F, 24.0F, 0.0F, 0.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 1.0F, 46.0F),
				rotL(0.48F, 10.0F, -18.0F, 58.0F),
				rotL(0.72F, -4.0F, 8.0F, 36.0F),
				rotL(0.96F, 2.0F, -6.0F, 14.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -30.856F, -38.0F, -83.72F),
				rotL(0.48F, -20.0F, -44.0F, -52.0F),
				rotL(0.72F, -22.0F, -24.0F, -46.0F),
				rotL(0.96F, -8.0F, -10.0F, -18.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, -1.0F, -46.0F),
				rotL(0.48F, 10.0F, 18.0F, -58.0F),
				rotL(0.72F, -4.0F, -8.0F, -36.0F),
				rotL(0.96F, 2.0F, 6.0F, -14.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -30.856F, 38.0F, 83.72F),
				rotL(0.48F, -20.0F, 44.0F, 52.0F),
				rotL(0.72F, -22.0F, 24.0F, 46.0F),
				rotL(0.96F, -8.0F, 10.0F, 18.0F),
				rotL(1.5F, 0.0F, 0.0F, 0.0F)
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
			return b.build();
		}
	}

	private static final class FlyingAttackAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.34F, 0.0F, 0.0F, 0.0F),
				rotL(0.58F, 0.0F, 0.0F, 0.0F),
				rotL(0.7F, 0.0F, -3.0F, -3.0F),
				rotL(0.92F, 0.0F, 2.0F, 2.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 5.92F, -0.8F),
				posL(0.34F, 0.0F, 7.2F, 1.4F),
				posL(0.58F, 0.0F, 4.4F, -3.6F),
				posL(0.7F, 0.0F, 4.3F, -3.8F),
				posL(0.92F, 0.0F, 5.4F, -1.8F),
				posL(1.3F, 0.0F, 5.92F, -0.8F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.5F, 0.0F, 0.0F),
				rotL(0.34F, -12.0F, 0.0F, 0.0F),
				rotL(0.58F, 18.0F, 0.0F, 0.0F),
				rotL(0.7F, 18.0F, -5.0F, 0.0F),
				rotL(0.92F, 12.0F, 3.0F, 0.0F),
				rotL(1.3F, 5.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.112F, 0.0F, 0.0F),
				rotL(0.34F, -9.0F, 0.0F, 0.0F),
				rotL(0.58F, 10.0F, 0.0F, 0.0F),
				rotL(0.7F, 9.0F, -6.0F, 0.0F),
				rotL(0.92F, 2.0F, 3.0F, 0.0F),
				rotL(1.3F, -1.112F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.245F, 0.0F, 0.0F),
				rotL(0.34F, -7.0F, 0.0F, 0.0F),
				rotL(0.58F, 8.0F, 0.0F, 0.0F),
				rotL(0.7F, 7.0F, -5.0F, 0.0F),
				rotL(0.92F, 1.0F, 2.0F, 0.0F),
				rotL(1.3F, -1.245F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.0F, 0.0F, 0.0F),
				rotL(0.34F, -5.0F, 0.0F, 0.0F),
				rotL(0.58F, 6.0F, 0.0F, 0.0F),
				rotL(0.7F, 5.0F, -4.0F, 0.0F),
				rotL(0.92F, 0.0F, 2.0F, 0.0F),
				rotL(1.3F, -2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -5.263F, 0.0F, 0.0F),
				rotL(0.34F, 14.0F, 0.0F, 0.0F),
				rotL(0.58F, -26.0F, 0.0F, 0.0F),
				rotL(0.7F, -24.0F, -7.0F, 6.0F),
				rotL(0.92F, -12.0F, 4.0F, -4.0F),
				rotL(1.3F, -5.263F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 2.0F, 0.0F, 0.0F),
				rotL(0.34F, 36.0F, 0.0F, 0.0F),
				rotL(0.58F, 30.0F, 0.0F, 0.0F),
				rotL(0.7F, -2.0F, 0.0F, 0.0F),
				rotL(0.92F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 1.0F, 46.0F),
				rotL(0.34F, -14.0F, 16.0F, 74.0F),
				rotL(0.58F, -4.0F, -18.0F, 22.0F),
				rotL(0.7F, -6.0F, -14.0F, 18.0F),
				rotL(0.92F, -12.0F, 8.0F, 62.0F),
				rotL(1.3F, -14.0F, 1.0F, 46.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -30.856F, -38.0F, -83.72F),
				rotL(0.34F, -36.0F, -26.0F, -46.0F),
				rotL(0.58F, -28.0F, -44.0F, -86.0F),
				rotL(0.7F, -30.0F, -40.0F, -82.0F),
				rotL(0.92F, -35.0F, -32.0F, -56.0F),
				rotL(1.3F, -30.856F, -38.0F, -83.72F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, -1.0F, -46.0F),
				rotL(0.34F, -14.0F, -16.0F, -74.0F),
				rotL(0.58F, -4.0F, 18.0F, -22.0F),
				rotL(0.7F, -6.0F, 14.0F, -18.0F),
				rotL(0.92F, -12.0F, -8.0F, -62.0F),
				rotL(1.3F, -14.0F, -1.0F, -46.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -30.856F, 38.0F, 83.72F),
				rotL(0.34F, -36.0F, 26.0F, 46.0F),
				rotL(0.58F, -28.0F, 44.0F, 86.0F),
				rotL(0.7F, -30.0F, 40.0F, 82.0F),
				rotL(0.92F, -35.0F, 32.0F, 56.0F),
				rotL(1.3F, -30.856F, 38.0F, 83.72F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 29.5F, 0.0F, 0.0F),
				rotL(0.34F, -10.0F, 0.0F, 0.0F),
				rotL(0.58F, -22.0F, 0.0F, 0.0F),
				rotL(0.7F, 14.0F, 0.0F, 0.0F),
				rotL(0.92F, 28.0F, 0.0F, 0.0F),
				rotL(1.3F, 29.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("knee_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, 0.0F),
				rotL(0.34F, 8.0F, 0.0F, 0.0F),
				rotL(0.58F, 6.0F, 0.0F, 0.0F),
				rotL(0.7F, 18.0F, 0.0F, 0.0F),
				rotL(0.92F, 22.0F, 0.0F, 0.0F),
				rotL(1.3F, 24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, 0.0F, 0.0F),
				rotL(0.34F, -10.0F, 0.0F, 0.0F),
				rotL(0.58F, -8.0F, 0.0F, 0.0F),
				rotL(0.7F, -20.0F, 0.0F, 0.0F),
				rotL(0.92F, -24.0F, 0.0F, 0.0F),
				rotL(1.3F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.34F, 12.0F, 0.0F, 0.0F),
				rotL(0.58F, 18.0F, 0.0F, 0.0F),
				rotL(0.7F, 22.0F, 0.0F, 0.0F),
				rotL(0.92F, 25.0F, 0.0F, 0.0F),
				rotL(1.3F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 29.5F, 0.0F, 0.0F),
				rotL(0.34F, -10.0F, 0.0F, 0.0F),
				rotL(0.58F, -22.0F, 0.0F, 0.0F),
				rotL(0.7F, 14.0F, 0.0F, 0.0F),
				rotL(0.92F, 28.0F, 0.0F, 0.0F),
				rotL(1.3F, 29.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("knee_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, 0.0F),
				rotL(0.34F, 8.0F, 0.0F, 0.0F),
				rotL(0.58F, 6.0F, 0.0F, 0.0F),
				rotL(0.7F, 18.0F, 0.0F, 0.0F),
				rotL(0.92F, 22.0F, 0.0F, 0.0F),
				rotL(1.3F, 24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, 0.0F, 0.0F),
				rotL(0.34F, -10.0F, 0.0F, 0.0F),
				rotL(0.58F, -8.0F, 0.0F, 0.0F),
				rotL(0.7F, -20.0F, 0.0F, 0.0F),
				rotL(0.92F, -24.0F, 0.0F, 0.0F),
				rotL(1.3F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.34F, 12.0F, 0.0F, 0.0F),
				rotL(0.58F, 18.0F, 0.0F, 0.0F),
				rotL(0.7F, 22.0F, 0.0F, 0.0F),
				rotL(0.92F, 25.0F, 0.0F, 0.0F),
				rotL(1.3F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.558F, -2.322F, 0.0F),
				rotL(0.34F, 3.5F, 0.0F, 0.0F),
				rotL(0.58F, 4.5F, 0.0F, 0.0F),
				rotL(0.7F, 3.5F, -5.0F, 0.0F),
				rotL(0.92F, 2.0F, 3.0F, 0.0F),
				rotL(1.3F, 3.558F, -2.322F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 4.893F, -1.812F, 0.0F),
				rotL(0.34F, 3.5F, 0.0F, 0.0F),
				rotL(0.58F, 4.5F, 0.0F, 0.0F),
				rotL(0.7F, 3.5F, -5.0F, 0.0F),
				rotL(0.92F, 2.0F, 3.0F, 0.0F),
				rotL(1.3F, 4.893F, -1.812F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.816F, -1.044F, 0.0F),
				rotL(0.34F, 3.5F, 0.0F, 0.0F),
				rotL(0.58F, 4.5F, 0.0F, 0.0F),
				rotL(0.7F, 3.5F, -5.0F, 0.0F),
				rotL(0.92F, 2.0F, 3.0F, 0.0F),
				rotL(1.3F, 5.816F, -1.044F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 6.195F, -0.127F, 0.0F),
				rotL(0.34F, 3.5F, 0.0F, 0.0F),
				rotL(0.58F, 4.5F, 0.0F, 0.0F),
				rotL(0.7F, 3.5F, -5.0F, 0.0F),
				rotL(0.92F, 2.0F, 3.0F, 0.0F),
				rotL(1.3F, 6.195F, -0.127F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.974F, 0.808F, 0.0F),
				rotL(0.34F, 3.5F, 0.0F, 0.0F),
				rotL(0.58F, 4.5F, 0.0F, 0.0F),
				rotL(0.7F, 3.5F, -5.0F, 0.0F),
				rotL(0.92F, 2.0F, 3.0F, 0.0F),
				rotL(1.3F, 5.974F, 0.808F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.187F, 1.628F, 0.0F),
				rotL(0.34F, 3.5F, 0.0F, 0.0F),
				rotL(0.58F, 4.5F, 0.0F, 0.0F),
				rotL(0.7F, 3.5F, -5.0F, 0.0F),
				rotL(0.92F, 2.0F, 3.0F, 0.0F),
				rotL(1.3F, 5.187F, 1.628F, 0.0F)
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
			return b.build();
		}
	}

	private static final class FlyingFireballSpitAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 0.0F, 0.0F, 0.0F),
				rotL(0.68F, 0.0F, 0.0F, 0.0F),
				rotL(0.96F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 5.92F, -0.8F),
				posL(0.48F, 0.0F, 6.9F, 1.2F),
				posL(0.68F, 0.0F, 6.2F, 2.2F),
				posL(0.96F, 0.0F, 5.8F, 1.4F),
				posL(1.6F, 0.0F, 5.92F, -0.8F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.5F, 0.0F, 0.0F),
				rotL(0.48F, -18.0F, 0.0F, 0.0F),
				rotL(0.68F, 15.0F, 0.0F, 0.0F),
				rotL(0.96F, 12.0F, 0.0F, 0.0F),
				rotL(1.6F, 5.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.112F, 0.0F, 0.0F),
				rotL(0.48F, -10.0F, 0.0F, 0.0F),
				rotL(0.68F, 8.0F, 0.0F, 0.0F),
				rotL(0.96F, 5.0F, 0.0F, 0.0F),
				rotL(1.6F, -1.112F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.245F, 0.0F, 0.0F),
				rotL(0.48F, -8.0F, 0.0F, 0.0F),
				rotL(0.68F, 6.0F, 0.0F, 0.0F),
				rotL(0.96F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, -1.245F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.0F, 0.0F, 0.0F),
				rotL(0.48F, -6.0F, 0.0F, 0.0F),
				rotL(0.68F, 4.0F, 0.0F, 0.0F),
				rotL(0.96F, 2.0F, 0.0F, 0.0F),
				rotL(1.6F, -2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -5.263F, 0.0F, 0.0F),
				rotL(0.48F, 18.0F, 0.0F, 0.0F),
				rotL(0.68F, -24.0F, 0.0F, 0.0F),
				rotL(0.96F, -18.0F, 0.0F, 0.0F),
				rotL(1.6F, -5.263F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 2.0F, 0.0F, 0.0F),
				rotL(0.48F, 18.0F, 0.0F, 0.0F),
				rotL(0.68F, 40.0F, 0.0F, 0.0F),
				rotL(0.96F, 36.0F, 0.0F, 0.0F),
				rotL(1.6F, 2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 1.0F, 46.0F),
				rotL(0.48F, -16.0F, 18.0F, 72.0F),
				rotL(0.68F, -6.0F, -16.0F, 26.0F),
				rotL(0.96F, -12.0F, 6.0F, 58.0F),
				rotL(1.6F, -14.0F, 1.0F, 46.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -30.856F, -38.0F, -83.72F),
				rotL(0.48F, -38.0F, -28.0F, -48.0F),
				rotL(0.68F, -30.0F, -42.0F, -82.0F),
				rotL(0.96F, -34.0F, -34.0F, -60.0F),
				rotL(1.6F, -30.856F, -38.0F, -83.72F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, -1.0F, -46.0F),
				rotL(0.48F, -16.0F, -18.0F, -72.0F),
				rotL(0.68F, -6.0F, 16.0F, -26.0F),
				rotL(0.96F, -12.0F, -6.0F, -58.0F),
				rotL(1.6F, -14.0F, -1.0F, -46.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -30.856F, 38.0F, 83.72F),
				rotL(0.48F, -38.0F, 28.0F, 48.0F),
				rotL(0.68F, -30.0F, 42.0F, 82.0F),
				rotL(0.96F, -34.0F, 34.0F, 60.0F),
				rotL(1.6F, -30.856F, 38.0F, 83.72F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 29.5F, 0.0F, 0.0F),
				rotL(0.48F, 18.0F, 0.0F, 0.0F),
				rotL(0.68F, 38.0F, 0.0F, 0.0F),
				rotL(0.96F, 34.0F, 0.0F, 0.0F),
				rotL(1.6F, 29.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("knee_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, 0.0F),
				rotL(0.48F, 16.0F, 0.0F, 0.0F),
				rotL(0.68F, 26.0F, 0.0F, 0.0F),
				rotL(0.96F, 24.0F, 0.0F, 0.0F),
				rotL(1.6F, 24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, 0.0F, 0.0F),
				rotL(0.48F, -18.0F, 0.0F, 0.0F),
				rotL(0.68F, -28.0F, 0.0F, 0.0F),
				rotL(0.96F, -26.0F, 0.0F, 0.0F),
				rotL(1.6F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.48F, 20.0F, 0.0F, 0.0F),
				rotL(0.68F, 28.0F, 0.0F, 0.0F),
				rotL(0.96F, 26.0F, 0.0F, 0.0F),
				rotL(1.6F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 29.5F, 0.0F, 0.0F),
				rotL(0.48F, 18.0F, 0.0F, 0.0F),
				rotL(0.68F, 38.0F, 0.0F, 0.0F),
				rotL(0.96F, 34.0F, 0.0F, 0.0F),
				rotL(1.6F, 29.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("knee_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, 0.0F),
				rotL(0.48F, 16.0F, 0.0F, 0.0F),
				rotL(0.68F, 26.0F, 0.0F, 0.0F),
				rotL(0.96F, 24.0F, 0.0F, 0.0F),
				rotL(1.6F, 24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, 0.0F, 0.0F),
				rotL(0.48F, -18.0F, 0.0F, 0.0F),
				rotL(0.68F, -28.0F, 0.0F, 0.0F),
				rotL(0.96F, -26.0F, 0.0F, 0.0F),
				rotL(1.6F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.48F, 20.0F, 0.0F, 0.0F),
				rotL(0.68F, 28.0F, 0.0F, 0.0F),
				rotL(0.96F, 26.0F, 0.0F, 0.0F),
				rotL(1.6F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.558F, -2.322F, 0.0F),
				rotL(0.48F, 4.0F, 0.0F, 0.0F),
				rotL(0.68F, 4.8F, 0.0F, 0.0F),
				rotL(0.96F, 3.2F, 0.0F, 0.0F),
				rotL(1.6F, 3.558F, -2.322F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 4.893F, -1.812F, 0.0F),
				rotL(0.48F, 4.0F, 0.0F, 0.0F),
				rotL(0.68F, 4.8F, 0.0F, 0.0F),
				rotL(0.96F, 3.2F, 0.0F, 0.0F),
				rotL(1.6F, 4.893F, -1.812F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.816F, -1.044F, 0.0F),
				rotL(0.48F, 4.0F, 0.0F, 0.0F),
				rotL(0.68F, 4.8F, 0.0F, 0.0F),
				rotL(0.96F, 3.2F, 0.0F, 0.0F),
				rotL(1.6F, 5.816F, -1.044F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 6.195F, -0.127F, 0.0F),
				rotL(0.48F, 4.0F, 0.0F, 0.0F),
				rotL(0.68F, 4.8F, 0.0F, 0.0F),
				rotL(0.96F, 3.2F, 0.0F, 0.0F),
				rotL(1.6F, 6.195F, -0.127F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.974F, 0.808F, 0.0F),
				rotL(0.48F, 4.0F, 0.0F, 0.0F),
				rotL(0.68F, 4.8F, 0.0F, 0.0F),
				rotL(0.96F, 3.2F, 0.0F, 0.0F),
				rotL(1.6F, 5.974F, 0.808F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.187F, 1.628F, 0.0F),
				rotL(0.48F, 4.0F, 0.0F, 0.0F),
				rotL(0.68F, 4.8F, 0.0F, 0.0F),
				rotL(0.96F, 3.2F, 0.0F, 0.0F),
				rotL(1.6F, 5.187F, 1.628F, 0.0F)
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
			return b.build();
		}
	}

	private static final class FlyingHurtAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.15F, -6.0F, -10.0F, -14.0F),
				rotL(0.38F, 2.0F, 3.0F, 4.0F),
				rotL(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 5.92F, -0.8F),
				posL(0.15F, -1.2F, 6.6F, 1.5F),
				posL(0.38F, 0.4F, 5.1F, 0.2F),
				posL(0.65F, 0.0F, 5.92F, -0.8F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.5F, 0.0F, 0.0F),
				rotL(0.15F, -8.0F, -8.0F, -5.0F),
				rotL(0.38F, 10.0F, 3.0F, 2.0F),
				rotL(0.65F, 5.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.112F, 0.0F, 0.0F),
				rotL(0.15F, -10.0F, -8.0F, 0.0F),
				rotL(0.38F, 0.0F, 2.0F, 0.0F),
				rotL(0.65F, -1.112F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -1.245F, 0.0F, 0.0F),
				rotL(0.15F, -6.0F, -6.0F, 0.0F),
				rotL(0.38F, -1.0F, 2.0F, 0.0F),
				rotL(0.65F, -1.245F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -2.0F, 0.0F, 0.0F),
				rotL(0.15F, -4.0F, -4.0F, 0.0F),
				rotL(0.38F, -2.0F, 0.0F, 0.0F),
				rotL(0.65F, -2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -5.263F, 0.0F, 0.0F),
				rotL(0.15F, 14.0F, -10.0F, 8.0F),
				rotL(0.38F, -6.0F, 3.0F, -3.0F),
				rotL(0.65F, -5.263F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 2.0F, 0.0F, 0.0F),
				rotL(0.15F, 28.0F, 0.0F, 0.0F),
				rotL(0.38F, 8.0F, 0.0F, 0.0F),
				rotL(0.65F, 2.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, 1.0F, 46.0F),
				rotL(0.15F, -14.0F, 14.0F, 72.0F),
				rotL(0.38F, -6.0F, -10.0F, 24.0F),
				rotL(0.65F, -14.0F, 1.0F, 46.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -30.856F, -38.0F, -83.72F),
				rotL(0.15F, -36.0F, -26.0F, -46.0F),
				rotL(0.38F, -30.0F, -40.0F, -84.0F),
				rotL(0.65F, -30.856F, -38.0F, -83.72F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -14.0F, -1.0F, -46.0F),
				rotL(0.15F, -4.0F, -20.0F, -22.0F),
				rotL(0.38F, -8.0F, 8.0F, -54.0F),
				rotL(0.65F, -14.0F, -1.0F, -46.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -30.856F, 38.0F, 83.72F),
				rotL(0.15F, -26.0F, 42.0F, 84.0F),
				rotL(0.38F, -34.0F, 34.0F, 66.0F),
				rotL(0.65F, -30.856F, 38.0F, 83.72F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 29.5F, 0.0F, 0.0F),
				rotL(0.15F, 16.0F, 0.0F, 8.0F),
				rotL(0.38F, 32.0F, 0.0F, 0.0F),
				rotL(0.65F, 29.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("knee_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, 0.0F),
				rotL(0.15F, 14.0F, 0.0F, 0.0F),
				rotL(0.38F, 24.0F, 0.0F, 0.0F),
				rotL(0.65F, 24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, 0.0F, 0.0F),
				rotL(0.15F, -16.0F, 0.0F, 0.0F),
				rotL(0.38F, -26.0F, 0.0F, 0.0F),
				rotL(0.65F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.15F, 14.0F, 0.0F, 0.0F),
				rotL(0.38F, 26.0F, 0.0F, 0.0F),
				rotL(0.65F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 29.5F, 0.0F, 0.0F),
				rotL(0.15F, 42.0F, 0.0F, -6.0F),
				rotL(0.38F, 30.0F, 0.0F, 0.0F),
				rotL(0.65F, 29.5F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("knee_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 24.0F, 0.0F, 0.0F),
				rotL(0.15F, 28.0F, 0.0F, 0.0F),
				rotL(0.38F, 24.0F, 0.0F, 0.0F),
				rotL(0.65F, 24.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, -26.0F, 0.0F, 0.0F),
				rotL(0.15F, -30.0F, 0.0F, 0.0F),
				rotL(0.38F, -26.0F, 0.0F, 0.0F),
				rotL(0.65F, -26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 26.0F, 0.0F, 0.0F),
				rotL(0.15F, 30.0F, 0.0F, 0.0F),
				rotL(0.38F, 26.0F, 0.0F, 0.0F),
				rotL(0.65F, 26.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 3.558F, -2.322F, 0.0F),
				rotL(0.15F, 4.2F, -8.0F, 0.0F),
				rotL(0.38F, 1.5F, 4.0F, 0.0F),
				rotL(0.65F, 3.558F, -2.322F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 4.893F, -1.812F, 0.0F),
				rotL(0.15F, 4.2F, -8.0F, 0.0F),
				rotL(0.38F, 1.5F, 4.0F, 0.0F),
				rotL(0.65F, 4.893F, -1.812F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.816F, -1.044F, 0.0F),
				rotL(0.15F, 4.2F, -8.0F, 0.0F),
				rotL(0.38F, 1.5F, 4.0F, 0.0F),
				rotL(0.65F, 5.816F, -1.044F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 6.195F, -0.127F, 0.0F),
				rotL(0.15F, 4.2F, -8.0F, 0.0F),
				rotL(0.38F, 1.5F, 4.0F, 0.0F),
				rotL(0.65F, 6.195F, -0.127F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.974F, 0.808F, 0.0F),
				rotL(0.15F, 4.2F, -8.0F, 0.0F),
				rotL(0.38F, 1.5F, 4.0F, 0.0F),
				rotL(0.65F, 5.974F, 0.808F, 0.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 5.187F, 1.628F, 0.0F),
				rotL(0.15F, 4.2F, -8.0F, 0.0F),
				rotL(0.38F, 1.5F, 4.0F, 0.0F),
				rotL(0.65F, 5.187F, 1.628F, 0.0F)
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
			return b.build();
		}
	}

	private static final class AttackAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, -14.0F, 0.0F, 0.0F),
				rotL(0.54F, 18.0F, 0.0F, 0.0F),
				rotL(0.65F, 19.0F, -4.0F, 0.0F),
				rotL(0.8F, 14.0F, 4.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, -8.0F, 0.0F, 0.0F),
				rotL(0.54F, 10.0F, 0.0F, 0.0F),
				rotL(0.65F, 9.0F, -5.0F, 0.0F),
				rotL(0.8F, 6.0F, 5.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, -6.0F, 0.0F, 0.0F),
				rotL(0.54F, 8.0F, 0.0F, 0.0F),
				rotL(0.65F, 7.0F, -5.0F, 0.0F),
				rotL(0.8F, 5.0F, 5.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, -4.0F, 0.0F, 0.0F),
				rotL(0.54F, 6.0F, 0.0F, 0.0F),
				rotL(0.65F, 5.0F, -4.0F, 0.0F),
				rotL(0.8F, 3.0F, 4.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 14.0F, 0.0F, 0.0F),
				rotL(0.54F, -26.0F, 0.0F, 0.0F),
				rotL(0.65F, -24.0F, -6.0F, 5.0F),
				rotL(0.8F, -18.0F, 6.0F, -5.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 34.0F, 0.0F, 0.0F),
				rotL(0.54F, 30.0F, 0.0F, 0.0F),
				rotL(0.65F, -2.0F, 0.0F, 0.0F),
				rotL(0.8F, -1.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, -6.0F, 12.0F, 28.0F),
				rotL(0.54F, 6.0F, -14.0F, 16.0F),
				rotL(0.65F, 8.0F, -16.0F, 10.0F),
				rotL(0.8F, 4.0F, -10.0F, 8.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, -18.0F, -22.0F, -36.0F),
				rotL(0.54F, -8.0F, -12.0F, -20.0F),
				rotL(0.65F, -4.0F, -8.0F, -12.0F),
				rotL(0.8F, -2.0F, -6.0F, -8.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, -6.0F, -12.0F, -28.0F),
				rotL(0.54F, 6.0F, 14.0F, -16.0F),
				rotL(0.65F, 8.0F, 16.0F, -10.0F),
				rotL(0.8F, 4.0F, 10.0F, -8.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, -18.0F, 22.0F, 36.0F),
				rotL(0.54F, -8.0F, 12.0F, 20.0F),
				rotL(0.65F, -4.0F, 8.0F, 12.0F),
				rotL(0.8F, -2.0F, 6.0F, 8.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 3.2F, 0.0F, 0.0F),
				rotL(0.54F, 5.5F, 0.0F, 0.0F),
				rotL(0.65F, 6.0F, -5.0F, 0.0F),
				rotL(0.8F, 4.0F, 5.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 3.2F, 0.0F, 0.0F),
				rotL(0.54F, 5.5F, 0.0F, 0.0F),
				rotL(0.65F, 6.0F, -5.0F, 0.0F),
				rotL(0.8F, 4.0F, 5.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 3.2F, 0.0F, 0.0F),
				rotL(0.54F, 5.5F, 0.0F, 0.0F),
				rotL(0.65F, 6.0F, -5.0F, 0.0F),
				rotL(0.8F, 4.0F, 5.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 3.2F, 0.0F, 0.0F),
				rotL(0.54F, 5.5F, 0.0F, 0.0F),
				rotL(0.65F, 6.0F, -5.0F, 0.0F),
				rotL(0.8F, 4.0F, 5.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 3.2F, 0.0F, 0.0F),
				rotL(0.54F, 5.5F, 0.0F, 0.0F),
				rotL(0.65F, 6.0F, -5.0F, 0.0F),
				rotL(0.8F, 4.0F, 5.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.28F, 3.2F, 0.0F, 0.0F),
				rotL(0.54F, 5.5F, 0.0F, 0.0F),
				rotL(0.65F, 6.0F, -5.0F, 0.0F),
				rotL(0.8F, 4.0F, 5.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("knee_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("knee_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.3F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
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
			return b.build();
		}
	}

	private static final class FireballSpitAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, -20.0F, 0.0F, 0.0F),
				rotL(0.66F, 14.0F, 0.0F, 0.0F),
				rotL(0.92F, 11.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, -9.0F, 0.0F, 0.0F),
				rotL(0.66F, 7.0F, 0.0F, 0.0F),
				rotL(0.92F, 5.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, -8.0F, 0.0F, 0.0F),
				rotL(0.66F, 6.0F, 0.0F, 0.0F),
				rotL(0.92F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, -6.0F, 0.0F, 0.0F),
				rotL(0.66F, 4.0F, 0.0F, 0.0F),
				rotL(0.92F, 3.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 18.0F, 0.0F, 0.0F),
				rotL(0.66F, -22.0F, 0.0F, 0.0F),
				rotL(0.92F, -18.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 16.0F, 0.0F, 0.0F),
				rotL(0.66F, 38.0F, 0.0F, 0.0F),
				rotL(0.92F, 35.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, -6.8F, 8.5F, 39.1F),
				rotL(0.66F, -10.0F, -8.0F, 26.0F),
				rotL(0.92F, -6.0F, -4.0F, 18.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, -28.9F, -32.3F, -57.8F),
				rotL(0.66F, -24.0F, -24.0F, -46.0F),
				rotL(0.92F, -16.0F, -16.0F, -32.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, -6.8F, -8.5F, -39.1F),
				rotL(0.66F, -10.0F, 8.0F, -26.0F),
				rotL(0.92F, -6.0F, 4.0F, -18.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, -28.9F, 32.3F, 57.8F),
				rotL(0.66F, -24.0F, 24.0F, 46.0F),
				rotL(0.92F, -16.0F, 16.0F, 32.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 4.2F, 0.0F, 0.0F),
				rotL(0.66F, 5.0F, 0.0F, 0.0F),
				rotL(0.92F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 4.2F, 0.0F, 0.0F),
				rotL(0.66F, 5.0F, 0.0F, 0.0F),
				rotL(0.92F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 4.2F, 0.0F, 0.0F),
				rotL(0.66F, 5.0F, 0.0F, 0.0F),
				rotL(0.92F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 4.2F, 0.0F, 0.0F),
				rotL(0.66F, 5.0F, 0.0F, 0.0F),
				rotL(0.92F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 4.2F, 0.0F, 0.0F),
				rotL(0.66F, 5.0F, 0.0F, 0.0F),
				rotL(0.92F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 4.2F, 0.0F, 0.0F),
				rotL(0.66F, 5.0F, 0.0F, 0.0F),
				rotL(0.92F, 4.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("knee_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("knee_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
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
			return b.build();
		}
	}

	private static final class HurtAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -13.0F, -8.0F, -5.0F),
				rotL(0.34F, 4.0F, 3.0F, 2.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -10.0F, -8.0F, 0.0F),
				rotL(0.34F, 3.0F, 3.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 14.0F, -10.0F, 8.0F),
				rotL(0.34F, -4.0F, 4.0F, -3.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 26.0F, 0.0F, 0.0F),
				rotL(0.34F, 8.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -6.0F, 12.0F, 24.0F),
				rotL(0.34F, 2.0F, -4.0F, 6.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -14.0F, -16.0F, -28.0F),
				rotL(0.34F, -4.0F, -4.0F, -8.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -6.0F, -12.0F, -24.0F),
				rotL(0.34F, 2.0F, 4.0F, -6.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, -14.0F, 16.0F, 28.0F),
				rotL(0.34F, -4.0F, 4.0F, 8.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 5.0F, -8.0F, 0.0F),
				rotL(0.34F, -2.0F, 4.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 5.0F, -8.0F, 0.0F),
				rotL(0.34F, -2.0F, 4.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 5.0F, -8.0F, 0.0F),
				rotL(0.34F, -2.0F, 4.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 5.0F, -8.0F, 0.0F),
				rotL(0.34F, -2.0F, 4.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 5.0F, -8.0F, 0.0F),
				rotL(0.34F, -2.0F, 4.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.14F, 5.0F, -8.0F, 0.0F),
				rotL(0.34F, -2.0F, 4.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("knee_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("knee_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.6F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
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
			return b.build();
		}
	}

	private static final class DeathAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -6.0F, 0.0F, -4.0F),
				rotL(0.95F, 10.0F, -4.0F, 8.0F),
				rotL(1.4F, 12.0F, -5.0F, 10.0F),
				rotL(1.8F, 12.0F, -5.0F, 10.0F)
			));
		}
		private static void ch_1_p0(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.025F, 0.0F, -0.022F, -0.025F),
				posL(0.05F, 0.0F, -0.059F, -0.05F),
				posL(0.075F, 0.0F, -0.078F, -0.075F),
				posL(0.1F, 0.0F, -0.099F, -0.1F),
				posL(0.125F, 0.0F, -0.122F, -0.125F),
				posL(0.15F, 0.0F, -0.146F, -0.15F),
				posL(0.175F, 0.0F, -0.172F, -0.175F),
				posL(0.2F, 0.0F, -0.199F, -0.2F),
				posL(0.225F, 0.0F, -0.228F, -0.225F),
				posL(0.25F, 0.0F, -0.258F, -0.25F),
				posL(0.275F, 0.0F, -0.29F, -0.275F),
				posL(0.3F, 0.0F, -0.323F, -0.3F),
				posL(0.325F, 0.0F, -0.604F, -0.325F),
				posL(0.35F, 0.0F, -0.85F, -0.35F),
				posL(0.375F, 0.0F, -0.752F, -0.375F),
				posL(0.4F, 0.0F, -0.632F, -0.4F),
				posL(0.425F, 0.0F, -0.533F, -0.425F),
				posL(0.45F, 0.0F, -0.442F, -0.45F),
				posL(0.475F, 0.0F, -0.375F, -0.475F),
				posL(0.5F, 0.0F, -0.33F, -0.5F),
				posL(0.525F, 0.0F, -0.29F, -0.525F),
				posL(0.55F, 0.0F, -0.195F, -0.55F),
				posL(0.575F, 0.0F, -0.119F, -0.575F),
				posL(0.6F, 0.0F, -0.06F, -0.6F),
				posL(0.625F, 0.0F, -0.02F, -0.625F),
				posL(0.65F, 0.0F, 0.002F, -0.65F),
				posL(0.675F, 0.0F, 0.005F, -0.675F),
				posL(0.7F, 0.0F, -0.01F, -0.7F),
				posL(0.725F, 0.0F, -0.044F, -0.725F),
				posL(0.75F, 0.0F, -0.097F, -0.75F),
				posL(0.775F, 0.0F, -0.168F, -0.775F),
				posL(0.8F, 0.0F, -0.258F, -0.8F),
				posL(0.825F, 0.0F, -0.367F, -0.825F),
				posL(0.85F, 0.0F, -0.494F, -0.85F),
				posL(0.875F, 0.0F, -0.639F, -0.875F),
				posL(0.9F, 0.0F, -0.803F, -0.9F),
				posL(0.925F, 0.0F, -0.984F, -0.925F),
				posL(0.95F, 0.0F, -0.383F, -0.95F),
				posL(0.975F, 0.0F, -0.246F, -0.975F),
				posL(1.0F, 0.0F, -0.109F, -1.0F),
				posL(1.025F, 0.0F, 0.027F, -1.025F),
				posL(1.05F, 0.0F, 0.162F, -1.05F),
				posL(1.075F, 0.0F, 0.297F, -1.075F),
				posL(1.1F, 0.0F, 0.432F, -1.1F),
				posL(1.125F, 0.0F, 0.565F, -1.125F),
				posL(1.15F, 0.0F, 0.698F, -1.15F),
				posL(1.175F, 0.0F, 0.831F, -1.175F),
				posL(1.2F, 0.0F, 0.962F, -1.2F),
				posL(1.225F, 0.0F, 1.093F, -1.2F),
				posL(1.25F, 0.0F, 1.223F, -1.2F),
				posL(1.275F, 0.0F, 1.352F, -1.2F),
				posL(1.3F, 0.0F, 1.481F, -1.2F),
				posL(1.325F, 0.0F, 1.608F, -1.2F),
				posL(1.35F, 0.0F, 1.735F, -1.2F),
				posL(1.375F, 0.0F, 1.86F, -1.2F),
				posL(1.4F, 0.0F, 1.985F, -1.2F),
				posL(1.425F, 0.0F, 1.985F, -1.2F),
				posL(1.45F, 0.0F, 1.985F, -1.2F),
				posL(1.475F, 0.0F, 1.985F, -1.2F)
			));
		}
		private static void ch_1_p1(List<Keyframe> out) {
			out.addAll(Arrays.asList(
				posL(1.5F, 0.0F, 1.985F, -1.2F),
				posL(1.525F, 0.0F, 1.985F, -1.2F),
				posL(1.55F, 0.0F, 1.985F, -1.2F),
				posL(1.575F, 0.0F, 1.985F, -1.2F),
				posL(1.6F, 0.0F, 1.985F, -1.2F),
				posL(1.625F, 0.0F, 1.985F, -1.2F),
				posL(1.65F, 0.0F, 1.985F, -1.2F),
				posL(1.675F, 0.0F, 1.985F, -1.2F),
				posL(1.7F, 0.0F, 1.985F, -1.2F),
				posL(1.725F, 0.0F, 1.985F, -1.2F),
				posL(1.75F, 0.0F, 1.985F, -1.2F),
				posL(1.775F, 0.0F, 1.985F, -1.2F),
				posL(1.8F, 0.0F, 1.985F, -1.2F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			List<Keyframe> kfs = new ArrayList<>(73);
			ch_1_p0(kfs);
			ch_1_p1(kfs);
			b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION, kfs.toArray(new Keyframe[0])));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -18.0F, -6.0F, 0.0F),
				rotL(0.95F, 20.0F, -6.0F, 4.0F),
				rotL(1.4F, 22.0F, -8.0F, 5.0F),
				rotL(1.8F, 22.0F, -8.0F, 5.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("neck_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -10.0F, -6.0F, 0.0F),
				rotL(0.95F, 12.0F, -8.0F, 0.0F),
				rotL(1.4F, 14.0F, -10.0F, 0.0F),
				rotL(1.8F, 14.0F, -10.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("neck_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -8.0F, -5.0F, 0.0F),
				rotL(0.95F, 10.0F, -6.0F, 0.0F),
				rotL(1.4F, 11.0F, -8.0F, 0.0F),
				rotL(1.8F, 11.0F, -8.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("neck_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -6.0F, -4.0F, 0.0F),
				rotL(0.95F, 6.0F, -4.0F, 0.0F),
				rotL(1.4F, 7.0F, -5.0F, 0.0F),
				rotL(1.8F, 7.0F, -5.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 14.0F, -8.0F, 6.0F),
				rotL(0.95F, -26.0F, -12.0F, 10.0F),
				rotL(1.4F, -32.0F, -14.0F, 14.0F),
				rotL(1.8F, -32.0F, -14.0F, 14.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 34.0F, 0.0F, 0.0F),
				rotL(0.95F, 16.0F, 0.0F, 0.0F),
				rotL(1.4F, 10.0F, 0.0F, 0.0F),
				rotL(1.8F, 10.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 4.2F, -4.0F, 0.0F),
				rotL(0.95F, 3.0F, -5.0F, 2.0F),
				rotL(1.4F, 3.2F, -6.0F, 3.0F),
				rotL(1.8F, 3.2F, -6.0F, 3.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 4.2F, -4.0F, 0.0F),
				rotL(0.95F, 3.0F, -5.0F, 2.0F),
				rotL(1.4F, 3.2F, -6.0F, 3.0F),
				rotL(1.8F, 3.2F, -6.0F, 3.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("tail_3_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 4.2F, -4.0F, 0.0F),
				rotL(0.95F, 3.0F, -5.0F, 2.0F),
				rotL(1.4F, 3.2F, -6.0F, 3.0F),
				rotL(1.8F, 3.2F, -6.0F, 3.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("tail_4_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 4.2F, -4.0F, 0.0F),
				rotL(0.95F, 3.0F, -5.0F, 2.0F),
				rotL(1.4F, 3.2F, -6.0F, 3.0F),
				rotL(1.8F, 3.2F, -6.0F, 3.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("tail_5_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 4.2F, -4.0F, 0.0F),
				rotL(0.95F, 3.0F, -5.0F, 2.0F),
				rotL(1.4F, 3.2F, -6.0F, 3.0F),
				rotL(1.8F, 3.2F, -6.0F, 3.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("tail_6_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 4.2F, -4.0F, 0.0F),
				rotL(0.95F, 3.0F, -5.0F, 2.0F),
				rotL(1.4F, 3.2F, -6.0F, 3.0F),
				rotL(1.8F, 3.2F, -6.0F, 3.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("leg_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 8.0F, 0.0F, 4.0F),
				rotL(0.95F, 42.0F, 0.0F, 14.0F),
				rotL(1.4F, 52.0F, 0.0F, 18.0F),
				rotL(1.8F, 52.0F, 0.0F, 18.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("knee_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 12.0F, 0.0F, 0.0F),
				rotL(0.95F, 28.0F, 0.0F, 0.0F),
				rotL(1.4F, 30.0F, 0.0F, 0.0F),
				rotL(1.8F, 30.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -14.0F, 0.0F, 0.0F),
				rotL(0.95F, -30.0F, 0.0F, 0.0F),
				rotL(1.4F, -32.0F, 0.0F, 0.0F),
				rotL(1.8F, -32.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("foot_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -6.0F, 0.0F, 0.0F),
				rotL(0.95F, 18.0F, 0.0F, 0.0F),
				rotL(1.4F, 22.0F, 0.0F, 0.0F),
				rotL(1.8F, 22.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("leg_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 8.0F, 0.0F, -4.0F),
				rotL(0.95F, 38.0F, 0.0F, -10.0F),
				rotL(1.4F, 46.0F, 0.0F, -14.0F),
				rotL(1.8F, 46.0F, 0.0F, -14.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("knee_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, 12.0F, 0.0F, 0.0F),
				rotL(0.95F, 26.0F, 0.0F, 0.0F),
				rotL(1.4F, 28.0F, 0.0F, 0.0F),
				rotL(1.8F, 28.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_20(AnimationDefinition.Builder b) {
			b.addAnimation("ankle_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -14.0F, 0.0F, 0.0F),
				rotL(0.95F, -28.0F, 0.0F, 0.0F),
				rotL(1.4F, -30.0F, 0.0F, 0.0F),
				rotL(1.8F, -30.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_21(AnimationDefinition.Builder b) {
			b.addAnimation("foot_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -6.0F, 0.0F, 0.0F),
				rotL(0.95F, 16.0F, 0.0F, 0.0F),
				rotL(1.4F, 20.0F, 0.0F, 0.0F),
				rotL(1.8F, 20.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_22(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -8.0F, 12.0F, 30.0F),
				rotL(0.95F, -8.0F, 8.0F, 38.0F),
				rotL(1.4F, -8.0F, 10.0F, 36.0F),
				rotL(1.8F, -8.0F, 10.0F, 36.0F)
			));
		}
		private static void ch_23(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -24.0F, -24.0F, -44.0F),
				rotL(0.95F, -30.0F, -32.0F, -62.0F),
				rotL(1.4F, -32.0F, -36.0F, -64.0F),
				rotL(1.8F, -32.0F, -36.0F, -64.0F)
			));
		}
		private static void ch_24(AnimationDefinition.Builder b) {
			b.addAnimation("wing_shoulder_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -8.0F, -12.0F, -30.0F),
				rotL(0.95F, -8.0F, -8.0F, -34.0F),
				rotL(1.4F, -8.0F, -10.0F, -30.0F),
				rotL(1.8F, -8.0F, -10.0F, -30.0F)
			));
		}
		private static void ch_25(AnimationDefinition.Builder b) {
			b.addAnimation("wing_wrist_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.3F, -24.0F, 24.0F, 44.0F),
				rotL(0.95F, -30.0F, 32.0F, 58.0F),
				rotL(1.4F, -32.0F, 36.0F, 56.0F),
				rotL(1.8F, -32.0F, 36.0F, 56.0F)
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
			return b.build();
		}
	}

}
