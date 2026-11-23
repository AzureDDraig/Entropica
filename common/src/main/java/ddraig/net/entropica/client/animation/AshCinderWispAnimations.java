package ddraig.net.entropica.client.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Exact Blockbench keyframe animations for Ash-Cinder Wisp (Fauna 49 — Will-o'-Wisp).
 * Split into per-animation static builders and <=60-keyframe helpers so constant pools and method bytecodes stay tiny.
 */
public final class AshCinderWispAnimations {

	private AshCinderWispAnimations() {}

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
	public static final AnimationDefinition FLUTTER_LIGHT = FlutterLightAnimBuilder.create();
	public static final AnimationDefinition flutter_light = FLUTTER_LIGHT;
	public static final AnimationDefinition HURT = HurtAnimBuilder.create();
	public static final AnimationDefinition hurt = HURT;
	public static final AnimationDefinition DEATH = DeathAnimBuilder.create();
	public static final AnimationDefinition death = DEATH;

	private static final class IdleAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_assembly", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, -3.5F),
				rotC(0.25F, -1.225F, 0.0F, -3.234F),
				rotC(0.5F, -2.263F, 0.0F, -2.475F),
				rotC(0.75F, -2.956F, 0.0F, -1.339F),
				rotC(1.0F, -3.2F, 0.0F, 0.0F),
				rotC(1.25F, -2.956F, 0.0F, 1.339F),
				rotC(1.5F, -2.263F, 0.0F, 2.475F),
				rotC(1.75F, -1.225F, 0.0F, 3.234F),
				rotC(2.0F, 0.0F, 0.0F, 3.5F),
				rotC(2.25F, 1.225F, 0.0F, 3.234F),
				rotC(2.5F, 2.263F, 0.0F, 2.475F),
				rotC(2.75F, 2.956F, 0.0F, 1.339F),
				rotC(3.0F, 3.2F, 0.0F, 0.0F),
				rotC(3.25F, 2.956F, 0.0F, -1.339F),
				rotC(3.5F, 2.263F, 0.0F, -2.475F),
				rotC(3.75F, 1.225F, 0.0F, -3.234F),
				rotC(4.0F, 0.0F, 0.0F, -3.5F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_assembly", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.35F),
				posC(0.25F, -0.172F, 0.813F, 0.323F),
				posC(0.5F, -0.318F, 1.15F, 0.247F),
				posC(0.75F, -0.416F, 0.813F, 0.134F),
				posC(1.0F, -0.45F, 0.0F, 0.0F),
				posC(1.25F, -0.416F, -0.813F, -0.134F),
				posC(1.5F, -0.318F, -1.15F, -0.247F),
				posC(1.75F, -0.172F, -0.813F, -0.323F),
				posC(2.0F, 0.0F, 0.0F, -0.35F),
				posC(2.25F, 0.172F, 0.813F, -0.323F),
				posC(2.5F, 0.318F, 1.15F, -0.247F),
				posC(2.75F, 0.416F, 0.813F, -0.134F),
				posC(3.0F, 0.45F, 0.0F, 0.0F),
				posC(3.25F, 0.416F, -0.813F, 0.134F),
				posC(3.5F, 0.318F, -1.15F, 0.247F),
				posC(3.75F, 0.172F, -0.813F, 0.323F),
				posC(4.0F, 0.0F, 0.0F, 0.35F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, -10.043F, 8.0F),
				rotC(0.25F, -3.444F, -13.011F, 7.391F),
				rotC(0.5F, -6.364F, -13.999F, 5.657F),
				rotC(0.75F, -8.315F, -12.855F, 3.061F),
				rotC(1.0F, -9.0F, -9.754F, 0.0F),
				rotC(1.25F, -8.315F, -5.168F, -3.061F),
				rotC(1.5F, -6.364F, 0.204F, -5.657F),
				rotC(1.75F, -3.444F, 5.546F, -7.391F),
				rotC(2.0F, 0.0F, 10.043F, -8.0F),
				rotC(2.25F, 3.444F, 13.011F, -7.391F),
				rotC(2.5F, 6.364F, 13.999F, -5.657F),
				rotC(2.75F, 8.315F, 12.855F, -3.061F),
				rotC(3.0F, 9.0F, 9.754F, 0.0F),
				rotC(3.25F, 8.315F, 5.168F, 3.061F),
				rotC(3.5F, 6.364F, -0.204F, 5.657F),
				rotC(3.75F, 3.444F, -5.546F, 7.391F),
				rotC(4.0F, 0.0F, -10.043F, 8.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclC(0.0F, 1.0F, 1.0F, 1.0F),
				sclC(0.25F, 1.032F, 1.032F, 1.032F),
				sclC(0.5F, 1.045F, 1.045F, 1.045F),
				sclC(0.75F, 1.032F, 1.032F, 1.032F),
				sclC(1.0F, 1.0F, 1.0F, 1.0F),
				sclC(1.25F, 0.968F, 0.968F, 0.968F),
				sclC(1.5F, 0.955F, 0.955F, 0.955F),
				sclC(1.75F, 0.968F, 0.968F, 0.968F),
				sclC(2.0F, 1.0F, 1.0F, 1.0F),
				sclC(2.25F, 1.032F, 1.032F, 1.032F),
				sclC(2.5F, 1.045F, 1.045F, 1.045F),
				sclC(2.75F, 1.032F, 1.032F, 1.032F),
				sclC(3.0F, 1.0F, 1.0F, 1.0F),
				sclC(3.25F, 0.968F, 0.968F, 0.968F),
				sclC(3.5F, 0.955F, 0.955F, 0.955F),
				sclC(3.75F, 0.968F, 0.968F, 0.968F),
				sclC(4.0F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("orbit_gimbal_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 5.5F),
				rotC(0.25F, -2.105F, 0.0F, 5.081F),
				rotC(0.5F, -3.889F, 0.0F, 3.889F),
				rotC(0.75F, -5.081F, 0.0F, 2.105F),
				rotC(1.0F, -5.5F, 0.0F, 0.0F),
				rotC(1.25F, -5.081F, 0.0F, -2.105F),
				rotC(1.5F, -3.889F, 0.0F, -3.889F),
				rotC(1.75F, -2.105F, 0.0F, -5.081F),
				rotC(2.0F, 0.0F, 0.0F, -5.5F),
				rotC(2.25F, 2.105F, 0.0F, -5.081F),
				rotC(2.5F, 3.889F, 0.0F, -3.889F),
				rotC(2.75F, 5.081F, 0.0F, -2.105F),
				rotC(3.0F, 5.5F, 0.0F, 0.0F),
				rotC(3.25F, 5.081F, 0.0F, 2.105F),
				rotC(3.5F, 3.889F, 0.0F, 3.889F),
				rotC(3.75F, 2.105F, 0.0F, 5.081F),
				rotC(4.0F, 0.0F, 0.0F, 5.5F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("orbit_ring_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(4.0F, 0.0F, -360.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("cinder_gimbal_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 6.0F, 0.0F, 0.0F),
				rotC(0.25F, 5.543F, 0.0F, 2.296F),
				rotC(0.5F, 4.243F, 0.0F, 4.243F),
				rotC(0.75F, 2.296F, 0.0F, 5.543F),
				rotC(1.0F, 0.0F, 0.0F, 6.0F),
				rotC(1.25F, -2.296F, 0.0F, 5.543F),
				rotC(1.5F, -4.243F, 0.0F, 4.243F),
				rotC(1.75F, -5.543F, 0.0F, 2.296F),
				rotC(2.0F, -6.0F, 0.0F, 0.0F),
				rotC(2.25F, -5.543F, 0.0F, -2.296F),
				rotC(2.5F, -4.243F, 0.0F, -4.243F),
				rotC(2.75F, -2.296F, 0.0F, -5.543F),
				rotC(3.0F, 0.0F, 0.0F, -6.0F),
				rotC(3.25F, 2.296F, 0.0F, -5.543F),
				rotC(3.5F, 4.243F, 0.0F, -4.243F),
				rotC(3.75F, 5.543F, 0.0F, -2.296F),
				rotC(4.0F, 6.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("cinder_ring_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(4.0F, 0.0F, 360.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("flake_a_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, -8.0F, 0.0F),
				rotC(0.25F, 4.95F, -5.657F, 3.536F),
				rotC(0.5F, 7.0F, 0.0F, 5.0F),
				rotC(0.75F, 4.95F, 5.657F, 3.536F),
				rotC(1.0F, 0.0F, 8.0F, 0.0F),
				rotC(1.25F, -4.95F, 5.657F, -3.536F),
				rotC(1.5F, -7.0F, 0.0F, -5.0F),
				rotC(1.75F, -4.95F, -5.657F, -3.536F),
				rotC(2.0F, 0.0F, -8.0F, 0.0F),
				rotC(2.25F, 4.95F, -5.657F, 3.536F),
				rotC(2.5F, 7.0F, 0.0F, 5.0F),
				rotC(2.75F, 4.95F, 5.657F, 3.536F),
				rotC(3.0F, 0.0F, 8.0F, 0.0F),
				rotC(3.25F, -4.95F, 5.657F, -3.536F),
				rotC(3.5F, -7.0F, 0.0F, -5.0F),
				rotC(3.75F, -4.95F, -5.657F, -3.536F),
				rotC(4.0F, 0.0F, -8.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("flake_a_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, -0.35F),
				posC(0.25F, 0.0F, 0.389F, -0.247F),
				posC(0.5F, 0.0F, 0.55F, 0.0F),
				posC(0.75F, 0.0F, 0.389F, 0.247F),
				posC(1.0F, 0.0F, 0.0F, 0.35F),
				posC(1.25F, 0.0F, -0.389F, 0.247F),
				posC(1.5F, 0.0F, -0.55F, 0.0F),
				posC(1.75F, 0.0F, -0.389F, -0.247F),
				posC(2.0F, 0.0F, 0.0F, -0.35F),
				posC(2.25F, 0.0F, 0.389F, -0.247F),
				posC(2.5F, 0.0F, 0.55F, 0.0F),
				posC(2.75F, 0.0F, 0.389F, 0.247F),
				posC(3.0F, 0.0F, 0.0F, 0.35F),
				posC(3.25F, 0.0F, -0.389F, 0.247F),
				posC(3.5F, 0.0F, -0.55F, 0.0F),
				posC(3.75F, 0.0F, -0.389F, -0.247F),
				posC(4.0F, 0.0F, 0.0F, -0.35F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("flake_upper_a_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 5.0F, 0.0F, 0.0F),
				rotC(0.25F, 3.536F, 0.0F, 2.828F),
				rotC(0.5F, 0.0F, 0.0F, 4.0F),
				rotC(0.75F, -3.536F, 0.0F, 2.828F),
				rotC(1.0F, -5.0F, 0.0F, 0.0F),
				rotC(1.25F, -3.536F, 0.0F, -2.828F),
				rotC(1.5F, 0.0F, 0.0F, -4.0F),
				rotC(1.75F, 3.536F, 0.0F, -2.828F),
				rotC(2.0F, 5.0F, 0.0F, 0.0F),
				rotC(2.25F, 3.536F, 0.0F, 2.828F),
				rotC(2.5F, 0.0F, 0.0F, 4.0F),
				rotC(2.75F, -3.536F, 0.0F, 2.828F),
				rotC(3.0F, -5.0F, 0.0F, 0.0F),
				rotC(3.25F, -3.536F, 0.0F, -2.828F),
				rotC(3.5F, 0.0F, 0.0F, -4.0F),
				rotC(3.75F, 3.536F, 0.0F, -2.828F),
				rotC(4.0F, 5.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("flake_lower_a_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -6.0F, 0.0F, 0.0F),
				rotC(0.25F, -4.243F, 0.0F, -3.536F),
				rotC(0.5F, 0.0F, 0.0F, -5.0F),
				rotC(0.75F, 4.243F, 0.0F, -3.536F),
				rotC(1.0F, 6.0F, 0.0F, 0.0F),
				rotC(1.25F, 4.243F, 0.0F, 3.536F),
				rotC(1.5F, 0.0F, 0.0F, 5.0F),
				rotC(1.75F, -4.243F, 0.0F, 3.536F),
				rotC(2.0F, -6.0F, 0.0F, 0.0F),
				rotC(2.25F, -4.243F, 0.0F, -3.536F),
				rotC(2.5F, 0.0F, 0.0F, -5.0F),
				rotC(2.75F, 4.243F, 0.0F, -3.536F),
				rotC(3.0F, 6.0F, 0.0F, 0.0F),
				rotC(3.25F, 4.243F, 0.0F, 3.536F),
				rotC(3.5F, 0.0F, 0.0F, 5.0F),
				rotC(3.75F, -4.243F, 0.0F, 3.536F),
				rotC(4.0F, -6.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("flake_b_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 6.062F, 4.0F, 4.33F),
				rotC(0.25F, 1.812F, 7.727F, 1.294F),
				rotC(0.5F, -3.5F, 6.928F, -2.5F),
				rotC(0.75F, -6.761F, 2.071F, -4.83F),
				rotC(1.0F, -6.062F, -4.0F, -4.33F),
				rotC(1.25F, -1.812F, -7.727F, -1.294F),
				rotC(1.5F, 3.5F, -6.928F, 2.5F),
				rotC(1.75F, 6.761F, -2.071F, 4.83F),
				rotC(2.0F, 6.062F, 4.0F, 4.33F),
				rotC(2.25F, 1.812F, 7.727F, 1.294F),
				rotC(2.5F, -3.5F, 6.928F, -2.5F),
				rotC(2.75F, -6.761F, 2.071F, -4.83F),
				rotC(3.0F, -6.062F, -4.0F, -4.33F),
				rotC(3.25F, -1.812F, -7.727F, -1.294F),
				rotC(3.5F, 3.5F, -6.928F, 2.5F),
				rotC(3.75F, 6.761F, -2.071F, 4.83F),
				rotC(4.0F, 6.062F, 4.0F, 4.33F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("flake_b_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.476F, 0.175F),
				posC(0.25F, 0.0F, 0.142F, 0.338F),
				posC(0.5F, 0.0F, -0.275F, 0.303F),
				posC(0.75F, 0.0F, -0.531F, 0.091F),
				posC(1.0F, 0.0F, -0.476F, -0.175F),
				posC(1.25F, 0.0F, -0.142F, -0.338F),
				posC(1.5F, 0.0F, 0.275F, -0.303F),
				posC(1.75F, 0.0F, 0.531F, -0.091F),
				posC(2.0F, 0.0F, 0.476F, 0.175F),
				posC(2.25F, 0.0F, 0.142F, 0.338F),
				posC(2.5F, 0.0F, -0.275F, 0.303F),
				posC(2.75F, 0.0F, -0.531F, 0.091F),
				posC(3.0F, 0.0F, -0.476F, -0.175F),
				posC(3.25F, 0.0F, -0.142F, -0.338F),
				posC(3.5F, 0.0F, 0.275F, -0.303F),
				posC(3.75F, 0.0F, 0.531F, -0.091F),
				posC(4.0F, 0.0F, 0.476F, 0.175F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("flake_upper_b_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -2.5F, 0.0F, 3.464F),
				rotC(0.25F, -4.83F, 0.0F, 1.035F),
				rotC(0.5F, -4.33F, 0.0F, -2.0F),
				rotC(0.75F, -1.294F, 0.0F, -3.864F),
				rotC(1.0F, 2.5F, 0.0F, -3.464F),
				rotC(1.25F, 4.83F, 0.0F, -1.035F),
				rotC(1.5F, 4.33F, 0.0F, 2.0F),
				rotC(1.75F, 1.294F, 0.0F, 3.864F),
				rotC(2.0F, -2.5F, 0.0F, 3.464F),
				rotC(2.25F, -4.83F, 0.0F, 1.035F),
				rotC(2.5F, -4.33F, 0.0F, -2.0F),
				rotC(2.75F, -1.294F, 0.0F, -3.864F),
				rotC(3.0F, 2.5F, 0.0F, -3.464F),
				rotC(3.25F, 4.83F, 0.0F, -1.035F),
				rotC(3.5F, 4.33F, 0.0F, 2.0F),
				rotC(3.75F, 1.294F, 0.0F, 3.864F),
				rotC(4.0F, -2.5F, 0.0F, 3.464F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("flake_lower_b_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 3.0F, 0.0F, -4.33F),
				rotC(0.25F, 5.796F, 0.0F, -1.294F),
				rotC(0.5F, 5.196F, 0.0F, 2.5F),
				rotC(0.75F, 1.553F, 0.0F, 4.83F),
				rotC(1.0F, -3.0F, 0.0F, 4.33F),
				rotC(1.25F, -5.796F, 0.0F, 1.294F),
				rotC(1.5F, -5.196F, 0.0F, -2.5F),
				rotC(1.75F, -1.553F, 0.0F, -4.83F),
				rotC(2.0F, 3.0F, 0.0F, -4.33F),
				rotC(2.25F, 5.796F, 0.0F, -1.294F),
				rotC(2.5F, 5.196F, 0.0F, 2.5F),
				rotC(2.75F, 1.553F, 0.0F, 4.83F),
				rotC(3.0F, -3.0F, 0.0F, 4.33F),
				rotC(3.25F, -5.796F, 0.0F, 1.294F),
				rotC(3.5F, -5.196F, 0.0F, -2.5F),
				rotC(3.75F, -1.553F, 0.0F, -4.83F),
				rotC(4.0F, 3.0F, 0.0F, -4.33F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("flake_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -6.062F, 4.0F, -4.33F),
				rotC(0.25F, -6.761F, -2.071F, -4.83F),
				rotC(0.5F, -3.5F, -6.928F, -2.5F),
				rotC(0.75F, 1.812F, -7.727F, 1.294F),
				rotC(1.0F, 6.062F, -4.0F, 4.33F),
				rotC(1.25F, 6.761F, 2.071F, 4.83F),
				rotC(1.5F, 3.5F, 6.928F, 2.5F),
				rotC(1.75F, -1.812F, 7.727F, -1.294F),
				rotC(2.0F, -6.062F, 4.0F, -4.33F),
				rotC(2.25F, -6.761F, -2.071F, -4.83F),
				rotC(2.5F, -3.5F, -6.928F, -2.5F),
				rotC(2.75F, 1.812F, -7.727F, 1.294F),
				rotC(3.0F, 6.062F, -4.0F, 4.33F),
				rotC(3.25F, 6.761F, 2.071F, 4.83F),
				rotC(3.5F, 3.5F, 6.928F, 2.5F),
				rotC(3.75F, -1.812F, 7.727F, -1.294F),
				rotC(4.0F, -6.062F, 4.0F, -4.33F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("flake_c_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, -0.476F, 0.175F),
				posC(0.25F, 0.0F, -0.531F, -0.091F),
				posC(0.5F, 0.0F, -0.275F, -0.303F),
				posC(0.75F, 0.0F, 0.142F, -0.338F),
				posC(1.0F, 0.0F, 0.476F, -0.175F),
				posC(1.25F, 0.0F, 0.531F, 0.091F),
				posC(1.5F, 0.0F, 0.275F, 0.303F),
				posC(1.75F, 0.0F, -0.142F, 0.338F),
				posC(2.0F, 0.0F, -0.476F, 0.175F),
				posC(2.25F, 0.0F, -0.531F, -0.091F),
				posC(2.5F, 0.0F, -0.275F, -0.303F),
				posC(2.75F, 0.0F, 0.142F, -0.338F),
				posC(3.0F, 0.0F, 0.476F, -0.175F),
				posC(3.25F, 0.0F, 0.531F, 0.091F),
				posC(3.5F, 0.0F, 0.275F, 0.303F),
				posC(3.75F, 0.0F, -0.142F, 0.338F),
				posC(4.0F, 0.0F, -0.476F, 0.175F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("flake_upper_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, -2.5F, 0.0F, -3.464F),
				rotC(0.25F, 1.294F, 0.0F, -3.864F),
				rotC(0.5F, 4.33F, 0.0F, -2.0F),
				rotC(0.75F, 4.83F, 0.0F, 1.035F),
				rotC(1.0F, 2.5F, 0.0F, 3.464F),
				rotC(1.25F, -1.294F, 0.0F, 3.864F),
				rotC(1.5F, -4.33F, 0.0F, 2.0F),
				rotC(1.75F, -4.83F, 0.0F, -1.035F),
				rotC(2.0F, -2.5F, 0.0F, -3.464F),
				rotC(2.25F, 1.294F, 0.0F, -3.864F),
				rotC(2.5F, 4.33F, 0.0F, -2.0F),
				rotC(2.75F, 4.83F, 0.0F, 1.035F),
				rotC(3.0F, 2.5F, 0.0F, 3.464F),
				rotC(3.25F, -1.294F, 0.0F, 3.864F),
				rotC(3.5F, -4.33F, 0.0F, 2.0F),
				rotC(3.75F, -4.83F, 0.0F, -1.035F),
				rotC(4.0F, -2.5F, 0.0F, -3.464F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("flake_lower_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 3.0F, 0.0F, 4.33F),
				rotC(0.25F, -1.553F, 0.0F, 4.83F),
				rotC(0.5F, -5.196F, 0.0F, 2.5F),
				rotC(0.75F, -5.796F, 0.0F, -1.294F),
				rotC(1.0F, -3.0F, 0.0F, -4.33F),
				rotC(1.25F, 1.553F, 0.0F, -4.83F),
				rotC(1.5F, 5.196F, 0.0F, -2.5F),
				rotC(1.75F, 5.796F, 0.0F, 1.294F),
				rotC(2.0F, 3.0F, 0.0F, 4.33F),
				rotC(2.25F, -1.553F, 0.0F, 4.83F),
				rotC(2.5F, -5.196F, 0.0F, 2.5F),
				rotC(2.75F, -5.796F, 0.0F, -1.294F),
				rotC(3.0F, -3.0F, 0.0F, -4.33F),
				rotC(3.25F, 1.553F, 0.0F, -4.83F),
				rotC(3.5F, 5.196F, 0.0F, -2.5F),
				rotC(3.75F, 5.796F, 0.0F, 1.294F),
				rotC(4.0F, 3.0F, 0.0F, 4.33F)
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
			return b.build();
		}
	}

	private static final class WalkAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_assembly", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 11.5F, 0.0F, -6.0F),
				rotC(0.167F, 13.25F, -2.5F, -5.196F),
				rotC(0.333F, 16.75F, -4.33F, -3.0F),
				rotC(0.5F, 18.5F, -5.0F, 0.0F),
				rotC(0.667F, 16.75F, -4.33F, 3.0F),
				rotC(0.833F, 13.25F, -2.5F, 5.196F),
				rotC(1.0F, 11.5F, 0.0F, 6.0F),
				rotC(1.167F, 13.25F, 2.5F, 5.196F),
				rotC(1.333F, 16.75F, 4.33F, 3.0F),
				rotC(1.5F, 18.5F, 5.0F, 0.0F),
				rotC(1.667F, 16.75F, 4.33F, -3.0F),
				rotC(1.833F, 13.25F, 2.5F, -5.196F),
				rotC(2.0F, 11.5F, 0.0F, -6.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_assembly", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, -0.45F),
				posC(0.167F, -0.275F, 0.953F, -0.225F),
				posC(0.333F, -0.476F, 0.953F, 0.225F),
				posC(0.5F, -0.55F, 0.0F, 0.45F),
				posC(0.667F, -0.476F, -0.953F, 0.225F),
				posC(0.833F, -0.275F, -0.953F, -0.225F),
				posC(1.0F, 0.0F, 0.0F, -0.45F),
				posC(1.167F, 0.275F, 0.953F, -0.225F),
				posC(1.333F, 0.476F, 0.953F, 0.225F),
				posC(1.5F, 0.55F, 0.0F, 0.45F),
				posC(1.667F, 0.476F, -0.953F, 0.225F),
				posC(1.833F, 0.275F, -0.953F, -0.225F),
				posC(2.0F, 0.0F, 0.0F, -0.45F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, -16.0F, 0.0F),
				rotC(0.167F, -4.0F, -13.856F, 3.5F),
				rotC(0.333F, -6.928F, -8.0F, 6.062F),
				rotC(0.5F, -8.0F, 0.0F, 7.0F),
				rotC(0.667F, -6.928F, 8.0F, 6.062F),
				rotC(0.833F, -4.0F, 13.856F, 3.5F),
				rotC(1.0F, 0.0F, 16.0F, 0.0F),
				rotC(1.167F, 4.0F, 13.856F, -3.5F),
				rotC(1.333F, 6.928F, 8.0F, -6.062F),
				rotC(1.5F, 8.0F, 0.0F, -7.0F),
				rotC(1.667F, 6.928F, -8.0F, -6.062F),
				rotC(1.833F, 4.0F, -13.856F, -3.5F),
				rotC(2.0F, 0.0F, -16.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclC(0.0F, 1.0F, 1.0F, 1.0F),
				sclC(0.167F, 1.043F, 1.043F, 1.043F),
				sclC(0.333F, 1.043F, 1.043F, 1.043F),
				sclC(0.5F, 1.0F, 1.0F, 1.0F),
				sclC(0.667F, 0.957F, 0.957F, 0.957F),
				sclC(0.833F, 0.957F, 0.957F, 0.957F),
				sclC(1.0F, 1.0F, 1.0F, 1.0F),
				sclC(1.167F, 1.043F, 1.043F, 1.043F),
				sclC(1.333F, 1.043F, 1.043F, 1.043F),
				sclC(1.5F, 1.0F, 1.0F, 1.0F),
				sclC(1.667F, 0.957F, 0.957F, 0.957F),
				sclC(1.833F, 0.957F, 0.957F, 0.957F),
				sclC(2.0F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("orbit_ring_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, -360.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("cinder_ring_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.0F, 0.0F, 360.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("flake_a_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 10.0F, -10.0F, 0.0F),
				rotC(0.167F, 13.0F, -8.66F, 3.0F),
				rotC(0.333F, 15.196F, -5.0F, 5.196F),
				rotC(0.5F, 16.0F, 0.0F, 6.0F),
				rotC(0.667F, 15.196F, 5.0F, 5.196F),
				rotC(0.833F, 13.0F, 8.66F, 3.0F),
				rotC(1.0F, 10.0F, 10.0F, 0.0F),
				rotC(1.167F, 7.0F, 8.66F, -3.0F),
				rotC(1.333F, 4.804F, 5.0F, -5.196F),
				rotC(1.5F, 4.0F, 0.0F, -6.0F),
				rotC(1.667F, 4.804F, -5.0F, -5.196F),
				rotC(1.833F, 7.0F, -8.66F, -3.0F),
				rotC(2.0F, 10.0F, -10.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("flake_a_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, -0.8F),
				posC(0.167F, 0.0F, 0.225F, -0.753F),
				posC(0.333F, 0.0F, 0.39F, -0.625F),
				posC(0.5F, 0.0F, 0.45F, -0.45F),
				posC(0.667F, 0.0F, 0.39F, -0.275F),
				posC(0.833F, 0.0F, 0.225F, -0.147F),
				posC(1.0F, 0.0F, 0.0F, -0.1F),
				posC(1.167F, 0.0F, -0.225F, -0.147F),
				posC(1.333F, 0.0F, -0.39F, -0.275F),
				posC(1.5F, 0.0F, -0.45F, -0.45F),
				posC(1.667F, 0.0F, -0.39F, -0.625F),
				posC(1.833F, 0.0F, -0.225F, -0.753F),
				posC(2.0F, 0.0F, 0.0F, -0.8F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("flake_b_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 15.196F, 5.0F, 5.196F),
				rotC(0.167F, 13.0F, 8.66F, 3.0F),
				rotC(0.333F, 10.0F, 10.0F, 0.0F),
				rotC(0.5F, 7.0F, 8.66F, -3.0F),
				rotC(0.667F, 4.804F, 5.0F, -5.196F),
				rotC(0.833F, 4.0F, 0.0F, -6.0F),
				rotC(1.0F, 4.804F, -5.0F, -5.196F),
				rotC(1.167F, 7.0F, -8.66F, -3.0F),
				rotC(1.333F, 10.0F, -10.0F, 0.0F),
				rotC(1.5F, 13.0F, -8.66F, 3.0F),
				rotC(1.667F, 15.196F, -5.0F, 5.196F),
				rotC(1.833F, 16.0F, 0.0F, 6.0F),
				rotC(2.0F, 15.196F, 5.0F, 5.196F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("flake_b_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.39F, -0.275F),
				posC(0.167F, 0.0F, 0.225F, -0.147F),
				posC(0.333F, 0.0F, 0.0F, -0.1F),
				posC(0.5F, 0.0F, -0.225F, -0.147F),
				posC(0.667F, 0.0F, -0.39F, -0.275F),
				posC(0.833F, 0.0F, -0.45F, -0.45F),
				posC(1.0F, 0.0F, -0.39F, -0.625F),
				posC(1.167F, 0.0F, -0.225F, -0.753F),
				posC(1.333F, 0.0F, 0.0F, -0.8F),
				posC(1.5F, 0.0F, 0.225F, -0.753F),
				posC(1.667F, 0.0F, 0.39F, -0.625F),
				posC(1.833F, 0.0F, 0.45F, -0.45F),
				posC(2.0F, 0.0F, 0.39F, -0.275F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("flake_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 4.804F, 5.0F, -5.196F),
				rotC(0.167F, 4.0F, 0.0F, -6.0F),
				rotC(0.333F, 4.804F, -5.0F, -5.196F),
				rotC(0.5F, 7.0F, -8.66F, -3.0F),
				rotC(0.667F, 10.0F, -10.0F, 0.0F),
				rotC(0.833F, 13.0F, -8.66F, 3.0F),
				rotC(1.0F, 15.196F, -5.0F, 5.196F),
				rotC(1.167F, 16.0F, 0.0F, 6.0F),
				rotC(1.333F, 15.196F, 5.0F, 5.196F),
				rotC(1.5F, 13.0F, 8.66F, 3.0F),
				rotC(1.667F, 10.0F, 10.0F, 0.0F),
				rotC(1.833F, 7.0F, 8.66F, -3.0F),
				rotC(2.0F, 4.804F, 5.0F, -5.196F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("flake_c_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, -0.39F, -0.275F),
				posC(0.167F, 0.0F, -0.45F, -0.45F),
				posC(0.333F, 0.0F, -0.39F, -0.625F),
				posC(0.5F, 0.0F, -0.225F, -0.753F),
				posC(0.667F, 0.0F, 0.0F, -0.8F),
				posC(0.833F, 0.0F, 0.225F, -0.753F),
				posC(1.0F, 0.0F, 0.39F, -0.625F),
				posC(1.167F, 0.0F, 0.45F, -0.45F),
				posC(1.333F, 0.0F, 0.39F, -0.275F),
				posC(1.5F, 0.0F, 0.225F, -0.147F),
				posC(1.667F, 0.0F, 0.0F, -0.1F),
				posC(1.833F, 0.0F, -0.225F, -0.147F),
				posC(2.0F, 0.0F, -0.39F, -0.275F)
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
			return b.build();
		}
	}

	private static final class AttackAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_assembly", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.34F, -8.0F, 0.0F, 0.0F),
				rotC(0.56F, 14.0F, 0.0F, 0.0F),
				rotC(1.05F, 4.0F, 0.0F, 0.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_assembly", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.34F, 0.0F, -0.8F, 0.9F),
				posC(0.56F, 0.0F, 1.5F, -1.6F),
				posC(0.88F, 0.0F, 0.7F, -0.5F),
				posC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.34F, 12.0F, 25.0F, 8.0F),
				rotC(0.56F, -18.0F, -65.0F, -14.0F),
				rotC(0.95F, 8.0F, -25.0F, 6.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclC(0.0F, 1.0F, 1.0F, 1.0F),
				sclC(0.34F, 0.9F, 0.9F, 0.9F),
				sclC(0.56F, 1.15F, 1.15F, 1.15F),
				sclC(0.86F, 1.05F, 1.05F, 1.05F),
				sclC(1.5F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("cinder_ring_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.34F, 0.0F, -40.0F, 0.0F),
				rotC(0.62F, 0.0F, 230.0F, 0.0F),
				rotC(1.5F, 0.0F, 360.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("cinder_ring_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclC(0.0F, 1.0F, 1.0F, 1.0F),
				sclC(0.34F, 0.96F, 0.96F, 0.96F),
				sclC(0.56F, 1.24F, 1.24F, 1.24F),
				sclC(0.92F, 1.1F, 1.1F, 1.1F),
				sclC(1.5F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("orbit_ring_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.34F, 0.0F, 35.0F, 0.0F),
				rotC(0.62F, 0.0F, -225.0F, 0.0F),
				rotC(1.05F, 0.0F, -338.0F, 0.0F),
				rotC(1.5F, 0.0F, -360.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("flake_a_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.34F, -16.0F, 12.0F, 8.0F),
				rotC(0.56F, 34.0F, -28.0F, -18.0F),
				rotC(0.74F, 18.0F, 24.0F, 14.0F),
				rotC(0.94F, 26.0F, -18.0F, -10.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("flake_a_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.34F, 0.0F, -0.3F, 0.55F),
				posC(0.56F, 0.0F, 0.8F, -3.6F),
				posC(0.78F, 0.0F, 0.4F, -2.7F),
				posC(1.05F, 0.0F, 0.1F, -1.1F),
				posC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("flake_upper_a_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.56F, 22.0F, 0.0F, 16.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("flake_lower_a_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.56F, -24.0F, 0.0F, -18.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("flake_b_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.34F, -16.0F, 12.0F, 8.0F),
				rotC(0.56F, 34.0F, -28.0F, -18.0F),
				rotC(0.74F, 18.0F, 24.0F, 14.0F),
				rotC(0.94F, 26.0F, -18.0F, -10.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("flake_b_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.34F, 0.0F, -0.3F, 0.55F),
				posC(0.56F, 0.0F, 0.8F, -3.6F),
				posC(0.78F, 0.0F, 0.4F, -2.7F),
				posC(1.05F, 0.0F, 0.1F, -1.1F),
				posC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("flake_upper_b_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.56F, 22.0F, 0.0F, 16.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("flake_lower_b_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.56F, -24.0F, 0.0F, -18.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("flake_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.34F, -16.0F, 12.0F, 8.0F),
				rotC(0.56F, 34.0F, -28.0F, -18.0F),
				rotC(0.74F, 18.0F, 24.0F, 14.0F),
				rotC(0.94F, 26.0F, -18.0F, -10.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("flake_c_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.34F, 0.0F, -0.3F, 0.55F),
				posC(0.56F, 0.0F, 0.8F, -3.6F),
				posC(0.78F, 0.0F, 0.4F, -2.7F),
				posC(1.05F, 0.0F, 0.1F, -1.1F),
				posC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("flake_upper_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.56F, 22.0F, 0.0F, 16.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("flake_lower_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.56F, -24.0F, 0.0F, -18.0F),
				rotC(1.5F, 0.0F, 0.0F, 0.0F)
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
			return b.build();
		}
	}

	private static final class FlutterLightAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_assembly", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.45F, 12.0F, 14.0F, 10.0F),
				rotC(0.95F, 16.0F, -16.0F, -12.0F),
				rotC(1.45F, 10.0F, 12.0F, 9.0F),
				rotC(1.95F, 4.0F, -6.0F, -5.0F),
				rotC(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_assembly", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.45F, 1.4F, -1.2F, -1.8F),
				posC(0.95F, -1.6F, 2.2F, -2.4F),
				posC(1.45F, 1.2F, 1.5F, -1.5F),
				posC(1.95F, -0.7F, -0.5F, -0.6F),
				posC(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclC(0.0F, 1.0F, 1.0F, 1.0F),
				sclC(0.95F, 1.1F, 1.1F, 1.1F),
				sclC(1.6F, 1.06F, 1.06F, 1.06F),
				sclC(2.4F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("orbit_ring_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, -360.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("cinder_ring_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(2.4F, 0.0F, 360.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("flake_a_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.15F, 3.965F, 1.344F, 2.163F),
				rotC(0.3F, -5.953F, 4.871F, -3.247F),
				rotC(0.45F, -4.677F, -9.239F, -2.551F),
				rotC(0.6F, 15.556F, 0.0F, 8.485F),
				rotC(0.75F, -7.0F, 13.827F, -3.818F),
				rotC(0.9F, -14.372F, -11.759F, -7.839F),
				rotC(1.05F, 19.935F, -6.756F, 10.874F),
				rotC(1.2F, 0.0F, 18.0F, 0.0F),
				rotC(1.35F, -19.935F, -6.756F, -10.874F),
				rotC(1.5F, 14.372F, -11.759F, 7.839F),
				rotC(1.65F, 7.0F, 13.827F, 3.818F),
				rotC(1.8F, -15.556F, 0.0F, -8.485F),
				rotC(1.95F, 4.677F, -9.239F, 2.551F),
				rotC(2.1F, 5.953F, 4.871F, 3.247F),
				rotC(2.25F, -3.965F, 1.344F, -2.163F),
				rotC(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("flake_a_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.15F, 0.0F, 0.117F, -0.215F),
				posC(0.3F, 0.0F, 0.0F, -0.421F),
				posC(0.45F, 0.0F, -0.333F, -0.611F),
				posC(0.6F, 0.0F, 0.0F, -0.778F),
				posC(0.75F, 0.0F, 0.499F, -0.915F),
				posC(0.9F, 0.0F, 0.0F, -1.016F),
				posC(1.05F, 0.0F, -0.588F, -1.079F),
				posC(1.2F, 0.0F, 0.0F, -1.1F),
				posC(1.35F, 0.0F, 0.588F, -1.079F),
				posC(1.5F, 0.0F, 0.0F, -1.016F),
				posC(1.65F, 0.0F, -0.499F, -0.915F),
				posC(1.8F, 0.0F, 0.0F, -0.778F),
				posC(1.95F, 0.0F, 0.333F, -0.611F),
				posC(2.1F, 0.0F, 0.0F, -0.421F),
				posC(2.25F, 0.0F, -0.117F, -0.215F),
				posC(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("flake_b_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.15F, -3.405F, 2.138F, -1.857F),
				rotC(0.3F, -2.179F, -6.654F, -1.189F),
				rotC(0.45F, 12.118F, 1.305F, 6.61F),
				rotC(0.6F, -7.778F, 11.023F, -4.243F),
				rotC(0.75F, -11.136F, -11.874F, -6.074F),
				rotC(0.9F, 19.633F, -4.304F, 10.709F),
				rotC(1.05F, -2.816F, 17.503F, -1.536F),
				rotC(1.2F, -19.053F, -9.0F, -10.392F),
				rotC(1.35F, 17.118F, -10.747F, 9.337F),
				rotC(1.5F, 5.261F, 16.063F, 2.869F),
				rotC(1.65F, -18.136F, -1.954F, -9.892F),
				rotC(1.8F, 7.778F, -11.023F, 4.243F),
				rotC(1.95F, 7.441F, 7.934F, 4.059F),
				rotC(2.1F, -8.132F, 1.783F, -4.436F),
				rotC(2.25F, 0.56F, -3.482F, 0.306F),
				rotC(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("flake_b_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.15F, 0.0F, -0.059F, -0.215F),
				posC(0.3F, 0.0F, -0.199F, -0.421F),
				posC(0.45F, 0.0F, 0.167F, -0.611F),
				posC(0.6F, 0.0F, 0.367F, -0.778F),
				posC(0.75F, 0.0F, -0.249F, -0.915F),
				posC(0.9F, 0.0F, -0.48F, -1.016F),
				posC(1.05F, 0.0F, 0.294F, -1.079F),
				posC(1.2F, 0.0F, 0.52F, -1.1F),
				posC(1.35F, 0.0F, -0.294F, -1.079F),
				posC(1.5F, 0.0F, -0.48F, -1.016F),
				posC(1.65F, 0.0F, 0.249F, -0.915F),
				posC(1.8F, 0.0F, 0.367F, -0.778F),
				posC(1.95F, 0.0F, -0.167F, -0.611F),
				posC(2.1F, 0.0F, -0.199F, -0.421F),
				posC(2.25F, 0.0F, 0.059F, -0.215F),
				posC(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("flake_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.15F, -0.56F, -3.482F, -0.306F),
				rotC(0.3F, 8.132F, 1.783F, 4.436F),
				rotC(0.45F, -7.441F, 7.934F, -4.059F),
				rotC(0.6F, -7.778F, -11.023F, -4.243F),
				rotC(0.75F, 18.136F, -1.954F, 9.892F),
				rotC(0.9F, -5.261F, 16.063F, -2.869F),
				rotC(1.05F, -17.118F, -10.747F, -9.337F),
				rotC(1.2F, 19.053F, -9.0F, 10.392F),
				rotC(1.35F, 2.816F, 17.503F, 1.536F),
				rotC(1.5F, -19.633F, -4.304F, -10.709F),
				rotC(1.65F, 11.136F, -11.874F, 6.074F),
				rotC(1.8F, 7.778F, 11.023F, 4.243F),
				rotC(1.95F, -12.118F, 1.305F, -6.61F),
				rotC(2.1F, 2.179F, -6.654F, 1.189F),
				rotC(2.25F, 3.405F, 2.138F, 1.857F),
				rotC(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("flake_c_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.15F, 0.0F, -0.059F, -0.215F),
				posC(0.3F, 0.0F, 0.199F, -0.421F),
				posC(0.45F, 0.0F, 0.167F, -0.611F),
				posC(0.6F, 0.0F, -0.367F, -0.778F),
				posC(0.75F, 0.0F, -0.249F, -0.915F),
				posC(0.9F, 0.0F, 0.48F, -1.016F),
				posC(1.05F, 0.0F, 0.294F, -1.079F),
				posC(1.2F, 0.0F, -0.52F, -1.1F),
				posC(1.35F, 0.0F, -0.294F, -1.079F),
				posC(1.5F, 0.0F, 0.48F, -1.016F),
				posC(1.65F, 0.0F, 0.249F, -0.915F),
				posC(1.8F, 0.0F, -0.367F, -0.778F),
				posC(1.95F, 0.0F, -0.167F, -0.611F),
				posC(2.1F, 0.0F, 0.199F, -0.421F),
				posC(2.25F, 0.0F, 0.059F, -0.215F),
				posC(2.4F, 0.0F, 0.0F, 0.0F)
			));
		}
		static AnimationDefinition create() {
			AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(2.4F);
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

	private static final class HurtAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_assembly", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.14F, -22.0F, 10.0F, 12.0F),
				rotC(0.36F, 6.0F, -4.0F, -5.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_assembly", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.14F, -0.5F, 0.9F, 2.5F),
				posC(0.34F, 0.3F, -0.4F, 0.9F),
				posC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclC(0.0F, 1.0F, 1.0F, 1.0F),
				sclC(0.14F, 0.85F, 0.85F, 0.85F),
				sclC(0.36F, 1.06F, 1.06F, 1.06F),
				sclC(0.65F, 1.0F, 1.0F, 1.0F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("flake_a_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.16F, 26.0F, -20.0F, -14.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("flake_a_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.16F, 0.0F, 0.5F, -2.3F),
				posC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("flake_b_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.16F, 26.0F, -20.0F, -14.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("flake_b_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.16F, 0.0F, 0.5F, -2.3F),
				posC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("flake_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotC(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.16F, 26.0F, -20.0F, -14.0F),
				rotC(0.65F, 0.0F, 0.0F, 0.0F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("flake_c_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posC(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.16F, 0.0F, 0.5F, -2.3F),
				posC(0.65F, 0.0F, 0.0F, 0.0F)
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
			return b.build();
		}
	}

	private static final class DeathAnimBuilder {
		private static void ch_0(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_assembly", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posC(0.25F, -0.6F, 0.8F, 0.4F),
				posC(0.52F, 0.5F, -1.8F, -0.3F),
				posL(1.18F, 0.0F, -12.844F, 0.0F),
				posL(1.38F, 0.0F, -12.394F, 0.0F),
				posL(1.8F, 0.0F, -12.844F, 0.0F)
			));
		}
		private static void ch_1(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_core_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.52F, -16.0F, -48.0F, -14.0F),
				rotL(1.25F, 0.0F, -95.0F, 0.0F),
				rotL(1.8F, 0.0F, -102.0F, 0.0F)
			));
		}
		private static void ch_2(AnimationDefinition.Builder b) {
			b.addAnimation("wisp_core_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclC(0.25F, 1.1F, 1.1F, 1.1F),
				sclC(0.55F, 0.76F, 0.76F, 0.76F),
				sclL(1.18F, 0.55F, 0.55F, 0.55F),
				sclL(1.8F, 0.55F, 0.55F, 0.55F)
			));
		}
		private static void ch_3(AnimationDefinition.Builder b) {
			b.addAnimation("orbit_gimbal_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, 8.0F, 0.0F, 6.0F),
				rotL(1.8F, 8.0F, 0.0F, 6.0F)
			));
		}
		private static void ch_4(AnimationDefinition.Builder b) {
			b.addAnimation("orbit_ring_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotC(0.52F, 0.0F, -72.0F, 0.0F),
				rotL(1.25F, 0.0F, -118.0F, 0.0F),
				rotL(1.8F, 0.0F, -122.0F, 0.0F)
			));
		}
		private static void ch_5(AnimationDefinition.Builder b) {
			b.addAnimation("cinder_gimbal_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.15F, -14.0F, 0.0F, -10.0F),
				rotL(1.8F, -14.0F, 0.0F, -10.0F)
			));
		}
		private static void ch_6(AnimationDefinition.Builder b) {
			b.addAnimation("cinder_ring_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(1.18F, 0.0F, -0.982F, 0.0F),
				posL(1.8F, 0.0F, -0.982F, 0.0F)
			));
		}
		private static void ch_7(AnimationDefinition.Builder b) {
			b.addAnimation("cinder_ring_grp", new AnimationChannel(AnimationChannel.Targets.SCALE,
				sclL(0.0F, 1.0F, 1.0F, 1.0F),
				sclL(0.55F, 1.04F, 1.04F, 1.04F),
				sclL(1.18F, 1.08F, 1.0F, 1.08F),
				sclL(1.8F, 1.08F, 1.0F, 1.08F)
			));
		}
		private static void ch_8(AnimationDefinition.Builder b) {
			b.addAnimation("flake_a_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 28.0F, 18.0F, 16.0F),
				rotL(1.18F, 96.0F, 14.0F, 12.0F),
				rotL(1.8F, 96.0F, 14.0F, 12.0F)
			));
		}
		private static void ch_9(AnimationDefinition.Builder b) {
			b.addAnimation("flake_a_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.5F, 0.0F, 0.8F, -1.8F),
				posL(1.18F, 0.0F, -3.196F, -2.2F),
				posL(1.38F, 0.0F, -2.946F, -2.35F),
				posL(1.8F, 0.0F, -3.196F, -2.4F)
			));
		}
		private static void ch_10(AnimationDefinition.Builder b) {
			b.addAnimation("flake_upper_a_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.18F, -8.0F, -5.0F, -14.0F),
				rotL(1.8F, -8.0F, -5.0F, -14.0F)
			));
		}
		private static void ch_11(AnimationDefinition.Builder b) {
			b.addAnimation("flake_lower_a_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.18F, 10.0F, 5.0F, 15.0F),
				rotL(1.8F, 10.0F, 5.0F, 15.0F)
			));
		}
		private static void ch_12(AnimationDefinition.Builder b) {
			b.addAnimation("flake_b_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 28.0F, 0.0F, 16.0F),
				rotL(1.18F, 96.0F, 0.0F, 12.0F),
				rotL(1.8F, 96.0F, 0.0F, 12.0F)
			));
		}
		private static void ch_13(AnimationDefinition.Builder b) {
			b.addAnimation("flake_b_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.5F, 0.0F, 0.8F, -1.8F),
				posL(1.18F, 0.0F, -2.296F, -2.2F),
				posL(1.38F, 0.0F, -2.046F, -2.35F),
				posL(1.8F, 0.0F, -2.296F, -2.4F)
			));
		}
		private static void ch_14(AnimationDefinition.Builder b) {
			b.addAnimation("flake_upper_b_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.18F, -8.0F, -5.0F, -14.0F),
				rotL(1.8F, -8.0F, -5.0F, -14.0F)
			));
		}
		private static void ch_15(AnimationDefinition.Builder b) {
			b.addAnimation("flake_lower_b_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.18F, 10.0F, 5.0F, 15.0F),
				rotL(1.8F, 10.0F, 5.0F, 15.0F)
			));
		}
		private static void ch_16(AnimationDefinition.Builder b) {
			b.addAnimation("flake_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(0.48F, 28.0F, -18.0F, 16.0F),
				rotL(1.18F, 96.0F, -14.0F, 12.0F),
				rotL(1.8F, 96.0F, -14.0F, 12.0F)
			));
		}
		private static void ch_17(AnimationDefinition.Builder b) {
			b.addAnimation("flake_c_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
				posL(0.0F, 0.0F, 0.0F, 0.0F),
				posL(0.5F, 0.0F, 0.8F, -1.8F),
				posL(1.18F, 0.0F, -2.896F, -2.2F),
				posL(1.38F, 0.0F, -2.646F, -2.35F),
				posL(1.8F, 0.0F, -2.896F, -2.4F)
			));
		}
		private static void ch_18(AnimationDefinition.Builder b) {
			b.addAnimation("flake_upper_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.18F, -8.0F, -5.0F, -14.0F),
				rotL(1.8F, -8.0F, -5.0F, -14.0F)
			));
		}
		private static void ch_19(AnimationDefinition.Builder b) {
			b.addAnimation("flake_lower_c_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
				rotL(0.0F, 0.0F, 0.0F, 0.0F),
				rotL(1.18F, 10.0F, 5.0F, 15.0F),
				rotL(1.8F, 10.0F, 5.0F, 15.0F)
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
			return b.build();
		}
	}

}
