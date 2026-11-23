package ddraig.net.entropica.client.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Exact Blockbench 5.0 keyframe animations for Crystalline Qilin (Glow Guardian).
 * Helper methods contain at most 6 .addAnimation(...) calls each to stay well below JVM bytecode limits.
 */
public final class CrystallineQilinAnimations {

	private CrystallineQilinAnimations() {
	}

	private static Keyframe rotKf(float time, float x, float y, float z) {
		return new Keyframe(time, KeyframeAnimations.degreeVec(x, y, z), AnimationChannel.Interpolations.LINEAR);
	}

	private static Keyframe rotKfSmooth(float time, float x, float y, float z) {
		return new Keyframe(time, KeyframeAnimations.degreeVec(x, y, z), AnimationChannel.Interpolations.CATMULLROM);
	}

	private static Keyframe posKf(float time, float x, float y, float z) {
		return new Keyframe(time, KeyframeAnimations.posVec(x, y, z), AnimationChannel.Interpolations.LINEAR);
	}

	private static Keyframe posKfSmooth(float time, float x, float y, float z) {
		return new Keyframe(time, KeyframeAnimations.posVec(x, y, z), AnimationChannel.Interpolations.CATMULLROM);
	}

	private static Keyframe scaleKf(float time, float x, float y, float z) {
		return new Keyframe(time, KeyframeAnimations.scaleVec(x, y, z), AnimationChannel.Interpolations.LINEAR);
	}

	public static final AnimationDefinition IDLE = create_idle();
	public static final AnimationDefinition idle = IDLE;
	public static final AnimationDefinition WALK = create_walk();
	public static final AnimationDefinition walk = WALK;
	public static final AnimationDefinition RUN = create_run();
	public static final AnimationDefinition run = RUN;
	public static final AnimationDefinition ATTACK = create_attack();
	public static final AnimationDefinition attack = ATTACK;
	public static final AnimationDefinition LIGHT_REFRACT = create_light_refract();
	public static final AnimationDefinition light_refract = LIGHT_REFRACT;
	public static final AnimationDefinition HURT = create_hurt();
	public static final AnimationDefinition hurt = HURT;
	public static final AnimationDefinition DEATH = create_death();
	public static final AnimationDefinition death = DEATH;

	private static void add_idle_0(AnimationDefinition.Builder b) {
		b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
			posKf(0.0F, 0.0F, 0.0F, 0.0F),
			posKf(0.8F, 0.0F, 0.14F, 0.0F),
			posKf(1.6F, 0.0F, 0.28F, 0.0F),
			posKf(2.4F, 0.0F, 0.12F, 0.0F),
			posKf(3.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("scale_flank_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.8F, 0.0F, 0.0F, 9.0F),
			rotKf(1.6F, 0.0F, 0.0F, 2.0F),
			rotKf(2.4F, 0.0F, 0.0F, 11.0F),
			rotKf(3.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("scale_flank_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.8F, 0.0F, 0.0F, -11.0F),
			rotKf(1.6F, 0.0F, 0.0F, -2.0F),
			rotKf(2.4F, 0.0F, 0.0F, -9.0F),
			rotKf(3.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.8F, -2.5F, -3.0F, 0.0F),
			rotKf(1.6F, 1.5F, 0.0F, 0.0F),
			rotKf(2.4F, -2.5F, 3.0F, 0.0F),
			rotKf(3.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.8F, -3.0F, -7.0F, 6.0F),
			rotKf(1.6F, 2.0F, 0.0F, 0.0F),
			rotKf(2.4F, -3.0F, 7.0F, -6.0F),
			rotKf(3.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(1.6F, 2.0F, 0.0F, 0.0F),
			rotKf(3.2F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_idle_1(AnimationDefinition.Builder b) {
		b.addAnimation("ear_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.8F, 0.0F, -6.0F, 8.0F),
			rotKf(2.0F, 0.0F, 4.0F, -3.0F),
			rotKf(3.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("ear_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, -4.0F, 3.0F),
			rotKf(2.4F, 0.0F, 6.0F, -8.0F),
			rotKf(3.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.8F, 4.0F, -7.0F, 0.0F),
			rotKf(1.6F, 2.0F, 0.0F, 0.0F),
			rotKf(2.4F, 4.0F, 7.0F, 0.0F),
			rotKf(3.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.8F, 3.0F, -10.0F, 0.0F),
			rotKf(2.4F, 3.0F, 10.0F, 0.0F),
			rotKf(3.2F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static AnimationDefinition create_idle() {
		AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(3.2F).looping();
		add_idle_0(b);
		add_idle_1(b);
		return b.build();
	}

	private static void add_walk_0(AnimationDefinition.Builder b) {
		b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, 1.5F, 0.0F, 0.0F),
			rotKf(0.4F, -1.0F, 0.0F, 0.0F),
			rotKf(0.6F, 0.5F, 0.0F, 0.0F),
			rotKf(0.8F, 1.0F, 0.0F, 0.0F),
			rotKf(1.0F, 1.5F, 0.0F, 0.0F),
			rotKf(1.2F, -1.0F, 0.0F, 0.0F),
			rotKf(1.42F, 0.5F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
			posKf(0.0F, 0.0F, 0.0F, 0.0F),
			posKf(0.025F, 0.0F, 0.033F, 0.0F),
			posKf(0.05F, 0.0F, 0.04F, 0.0F),
			posKf(0.075F, 0.0F, 0.033F, 0.0F),
			posKf(0.1F, 0.0F, 0.013F, 0.0F),
			posKf(0.125F, 0.0F, -0.021F, 0.0F),
			posKf(0.15F, 0.0F, -0.069F, 0.0F),
			posKf(0.175F, 0.0F, -0.131F, 0.0F),
			posKf(0.2F, 0.0F, -0.206F, 0.0F),
			posKf(0.225F, 0.0F, -0.29F, 0.0F),
			posKf(0.25F, 0.0F, -0.356F, 0.0F),
			posKf(0.275F, 0.0F, -0.265F, 0.0F),
			posKf(0.3F, 0.0F, -0.181F, 0.0F),
			posKf(0.325F, 0.0F, -0.1F, 0.0F),
			posKf(0.35F, 0.0F, -0.013F, 0.0F),
			posKf(0.375F, 0.0F, 0.068F, 0.0F),
			posKf(0.4F, 0.0F, 0.142F, 0.0F),
			posKf(0.425F, 0.0F, 0.154F, 0.0F),
			posKf(0.45F, 0.0F, 0.157F, 0.0F),
			posKf(0.475F, 0.0F, 0.15F, 0.0F),
			posKf(0.5F, 0.0F, 0.132F, 0.0F),
			posKf(0.525F, 0.0F, 0.104F, 0.0F),
			posKf(0.55F, 0.0F, 0.073F, 0.0F),
			posKf(0.575F, 0.0F, 0.043F, 0.0F),
			posKf(0.6F, 0.0F, 0.079F, 0.0F),
			posKf(0.625F, 0.0F, 0.085F, 0.0F),
			posKf(0.65F, 0.0F, 0.088F, 0.0F),
			posKf(0.675F, 0.0F, 0.086F, 0.0F),
			posKf(0.7F, 0.0F, 0.08F, 0.0F),
			posKf(0.725F, 0.0F, 0.069F, 0.0F),
			posKf(0.75F, 0.0F, 0.054F, 0.0F),
			posKf(0.775F, 0.0F, 0.035F, 0.0F),
			posKf(0.8F, 0.0F, 0.012F, 0.0F),
			posKf(0.825F, 0.0F, -0.02F, 0.0F),
			posKf(0.85F, 0.0F, -0.058F, 0.0F),
			posKf(0.875F, 0.0F, -0.1F, 0.0F),
			posKf(0.9F, 0.0F, -0.147F, 0.0F),
			posKf(0.925F, 0.0F, -0.2F, 0.0F),
			posKf(0.95F, 0.0F, -0.257F, 0.0F),
			posKf(0.975F, 0.0F, -0.32F, 0.0F),
			posKf(1.0F, 0.0F, -0.388F, 0.0F),
			posKf(1.025F, 0.0F, -0.381F, 0.0F),
			posKf(1.05F, 0.0F, -0.298F, 0.0F),
			posKf(1.075F, 0.0F, -0.221F, 0.0F),
			posKf(1.1F, 0.0F, -0.148F, 0.0F),
			posKf(1.125F, 0.0F, -0.078F, 0.0F),
			posKf(1.15F, 0.0F, 0.0F, 0.0F),
			posKf(1.175F, 0.0F, 0.074F, 0.0F),
			posKf(1.2F, 0.0F, 0.142F, 0.0F),
			posKf(1.225F, 0.0F, 0.147F, 0.0F),
			posKf(1.25F, 0.0F, 0.147F, 0.0F),
			posKf(1.275F, 0.0F, 0.14F, 0.0F),
			posKf(1.3F, 0.0F, 0.127F, 0.0F),
			posKf(1.325F, 0.0F, 0.109F, 0.0F),
			posKf(1.35F, 0.0F, 0.085F, 0.0F),
			posKf(1.375F, 0.0F, 0.063F, 0.0F),
			posKf(1.4F, 0.0F, 0.038F, 0.0F),
			posKf(1.425F, 0.0F, 0.071F, 0.0F),
			posKf(1.45F, 0.0F, 0.064F, 0.0F),
			posKf(1.475F, 0.0F, 0.057F, 0.0F),
			posKf(1.5F, 0.0F, 0.049F, 0.0F),
			posKf(1.525F, 0.0F, 0.04F, 0.0F),
			posKf(1.55F, 0.0F, 0.031F, 0.0F),
			posKf(1.575F, 0.0F, 0.022F, 0.0F),
			posKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("scale_flank_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, 0.0F, 0.0F, 6.0F),
			rotKf(0.4F, 0.0F, 0.0F, 10.0F),
			rotKf(0.6F, 0.0F, 0.0F, 4.0F),
			rotKf(0.8F, 0.0F, 0.0F, 5.0F),
			rotKf(1.0F, 0.0F, 0.0F, 8.0F),
			rotKf(1.2F, 0.0F, 0.0F, 10.0F),
			rotKf(1.42F, 0.0F, 0.0F, 4.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("scale_flank_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, 0.0F, 0.0F, -6.0F),
			rotKf(0.4F, 0.0F, 0.0F, -10.0F),
			rotKf(0.6F, 0.0F, 0.0F, -4.0F),
			rotKf(0.8F, 0.0F, 0.0F, -5.0F),
			rotKf(1.0F, 0.0F, 0.0F, -8.0F),
			rotKf(1.2F, 0.0F, 0.0F, -10.0F),
			rotKf(1.42F, 0.0F, 0.0F, -4.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, -3.0F, 0.0F, 0.0F),
			rotKf(0.4F, 3.0F, 0.0F, 0.0F),
			rotKf(0.6F, -1.0F, 0.0F, 0.0F),
			rotKf(0.8F, -2.0F, 0.0F, 0.0F),
			rotKf(1.0F, -3.0F, 0.0F, 0.0F),
			rotKf(1.2F, 3.0F, 0.0F, 0.0F),
			rotKf(1.42F, -1.0F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, 2.1F, 0.0F, 0.0F),
			rotKf(0.4F, -2.1F, 0.0F, 0.0F),
			rotKf(0.6F, 0.7F, 0.0F, 0.0F),
			rotKf(0.8F, 1.4F, 0.0F, 0.0F),
			rotKf(1.0F, 2.1F, 0.0F, 0.0F),
			rotKf(1.2F, -2.1F, 0.0F, 0.0F),
			rotKf(1.42F, 0.7F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_walk_1(AnimationDefinition.Builder b) {
		b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, -22.0F, 0.0F, 0.0F),
			rotKf(0.4F, -18.0F, 0.0F, 0.0F),
			rotKf(0.6F, -2.0F, 0.0F, 0.0F),
			rotKf(0.8F, 6.0F, 0.0F, 0.0F),
			rotKf(1.0F, 14.0F, 0.0F, 0.0F),
			rotKf(1.2F, 18.0F, 0.0F, 0.0F),
			rotKf(1.42F, -8.0F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
			posKf(0.0F, 0.0F, 0.0F, 0.0F),
			posKf(0.2F, 0.0F, 0.9F, 0.0F),
			posKf(0.4F, 0.0F, 0.0F, 0.0F),
			posKf(0.6F, 0.0F, 0.0F, 0.0F),
			posKf(0.8F, 0.0F, 0.0F, 0.0F),
			posKf(1.0F, 0.0F, 0.0F, 0.0F),
			posKf(1.2F, 0.0F, 0.0F, 0.0F),
			posKf(1.42F, 0.0F, 0.7F, 0.0F),
			posKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("knee_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, 32.0F, 0.0F, 0.0F),
			rotKf(0.4F, 6.0F, 0.0F, 0.0F),
			rotKf(0.6F, 4.0F, 0.0F, 0.0F),
			rotKf(0.8F, 4.0F, 0.0F, 0.0F),
			rotKf(1.0F, 6.0F, 0.0F, 0.0F),
			rotKf(1.2F, 8.0F, 0.0F, 0.0F),
			rotKf(1.42F, 26.0F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hoof_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, -10.0F, 0.0F, 0.0F),
			rotKf(0.4F, 12.0F, 0.0F, 0.0F),
			rotKf(0.6F, -2.0F, 0.0F, 0.0F),
			rotKf(0.8F, -10.0F, 0.0F, 0.0F),
			rotKf(1.0F, -20.0F, 0.0F, 0.0F),
			rotKf(1.2F, -26.0F, 0.0F, 0.0F),
			rotKf(1.42F, -18.0F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, 12.0F, 0.0F, 0.0F),
			rotKf(0.4F, 18.0F, 0.0F, 0.0F),
			rotKf(0.6F, -6.0F, 0.0F, 0.0F),
			rotKf(0.8F, -18.0F, 0.0F, 0.0F),
			rotKf(1.0F, -22.0F, 0.0F, 0.0F),
			rotKf(1.2F, -18.0F, 0.0F, 0.0F),
			rotKf(1.42F, -4.0F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
			posKf(0.0F, 0.0F, 0.0F, 0.0F),
			posKf(0.2F, 0.0F, 0.0F, 0.0F),
			posKf(0.4F, 0.0F, 0.0F, 0.0F),
			posKf(0.6F, 0.0F, 0.7F, 0.0F),
			posKf(0.8F, 0.0F, 0.7F, 0.0F),
			posKf(1.0F, 0.0F, 0.8F, 0.0F),
			posKf(1.2F, 0.0F, 0.0F, 0.0F),
			posKf(1.42F, 0.0F, 0.0F, 0.0F),
			posKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_walk_2(AnimationDefinition.Builder b) {
		b.addAnimation("knee_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, 4.0F, 0.0F, 0.0F),
			rotKf(0.4F, 8.0F, 0.0F, 0.0F),
			rotKf(0.6F, 28.0F, 0.0F, 0.0F),
			rotKf(0.8F, 26.0F, 0.0F, 0.0F),
			rotKf(1.0F, 28.0F, 0.0F, 0.0F),
			rotKf(1.2F, 6.0F, 0.0F, 0.0F),
			rotKf(1.42F, 4.0F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hoof_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, -16.0F, 0.0F, 0.0F),
			rotKf(0.4F, -26.0F, 0.0F, 0.0F),
			rotKf(0.6F, -22.0F, 0.0F, 0.0F),
			rotKf(0.8F, -8.0F, 0.0F, 0.0F),
			rotKf(1.0F, -6.0F, 0.0F, 0.0F),
			rotKf(1.2F, 12.0F, 0.0F, 0.0F),
			rotKf(1.42F, 0.0F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, 12.0F, 0.0F, 0.0F),
			rotKf(0.4F, 18.0F, 0.0F, 0.0F),
			rotKf(0.6F, -6.0F, 0.0F, 0.0F),
			rotKf(0.8F, -16.0F, 0.0F, 0.0F),
			rotKf(1.0F, -20.0F, 0.0F, 0.0F),
			rotKf(1.2F, -16.0F, 0.0F, 0.0F),
			rotKf(1.42F, -4.0F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
			posKf(0.0F, 0.0F, 0.0F, 0.0F),
			posKf(0.2F, 0.0F, 0.0F, 0.0F),
			posKf(0.4F, 0.0F, 0.0F, 0.0F),
			posKf(0.6F, 0.0F, 0.6F, 0.0F),
			posKf(0.8F, 0.0F, 0.6F, 0.0F),
			posKf(1.0F, 0.0F, 0.7F, 0.0F),
			posKf(1.2F, 0.0F, 0.0F, 0.0F),
			posKf(1.42F, 0.0F, 0.0F, 0.0F),
			posKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, -6.0F, 0.0F, 0.0F),
			rotKf(0.4F, -10.0F, 0.0F, 0.0F),
			rotKf(0.6F, 20.0F, 0.0F, 0.0F),
			rotKf(0.8F, 20.0F, 0.0F, 0.0F),
			rotKf(1.0F, 22.0F, 0.0F, 0.0F),
			rotKf(1.2F, 6.0F, 0.0F, 0.0F),
			rotKf(1.42F, -2.0F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hoof_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, -6.0F, 0.0F, 0.0F),
			rotKf(0.4F, -8.0F, 0.0F, 0.0F),
			rotKf(0.6F, -14.0F, 0.0F, 0.0F),
			rotKf(0.8F, -4.0F, 0.0F, 0.0F),
			rotKf(1.0F, -2.0F, 0.0F, 0.0F),
			rotKf(1.2F, 10.0F, 0.0F, 0.0F),
			rotKf(1.42F, 6.0F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_walk_3(AnimationDefinition.Builder b) {
		b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, -20.0F, 0.0F, 0.0F),
			rotKf(0.4F, -16.0F, 0.0F, 0.0F),
			rotKf(0.6F, -2.0F, 0.0F, 0.0F),
			rotKf(0.8F, 6.0F, 0.0F, 0.0F),
			rotKf(1.0F, 14.0F, 0.0F, 0.0F),
			rotKf(1.2F, 18.0F, 0.0F, 0.0F),
			rotKf(1.42F, -8.0F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
			posKf(0.0F, 0.0F, 0.0F, 0.0F),
			posKf(0.2F, 0.0F, 0.8F, 0.0F),
			posKf(0.4F, 0.0F, 0.0F, 0.0F),
			posKf(0.6F, 0.0F, 0.0F, 0.0F),
			posKf(0.8F, 0.0F, 0.0F, 0.0F),
			posKf(1.0F, 0.0F, 0.0F, 0.0F),
			posKf(1.2F, 0.0F, 0.0F, 0.0F),
			posKf(1.42F, 0.0F, 0.6F, 0.0F),
			posKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, 24.0F, 0.0F, 0.0F),
			rotKf(0.4F, 6.0F, 0.0F, 0.0F),
			rotKf(0.6F, -2.0F, 0.0F, 0.0F),
			rotKf(0.8F, -4.0F, 0.0F, 0.0F),
			rotKf(1.0F, -8.0F, 0.0F, 0.0F),
			rotKf(1.2F, -10.0F, 0.0F, 0.0F),
			rotKf(1.42F, 18.0F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hoof_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, -4.0F, 0.0F, 0.0F),
			rotKf(0.4F, 10.0F, 0.0F, 0.0F),
			rotKf(0.6F, 4.0F, 0.0F, 0.0F),
			rotKf(0.8F, -2.0F, 0.0F, 0.0F),
			rotKf(1.0F, -6.0F, 0.0F, 0.0F),
			rotKf(1.2F, -8.0F, 0.0F, 0.0F),
			rotKf(1.42F, -10.0F, 0.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, 6.0F, -8.0F, 0.0F),
			rotKf(0.4F, 6.0F, -10.0F, 0.0F),
			rotKf(0.6F, 6.0F, -3.0F, 0.0F),
			rotKf(0.8F, 6.0F, 4.0F, 0.0F),
			rotKf(1.0F, 6.0F, 9.0F, 0.0F),
			rotKf(1.2F, 6.0F, 10.0F, 0.0F),
			rotKf(1.42F, 6.0F, 3.0F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.2F, 4.0F, -9.6F, 0.0F),
			rotKf(0.4F, 4.0F, -12.0F, 0.0F),
			rotKf(0.6F, 4.0F, -3.6F, 0.0F),
			rotKf(0.8F, 4.0F, 4.8F, 0.0F),
			rotKf(1.0F, 4.0F, 10.8F, 0.0F),
			rotKf(1.2F, 4.0F, 12.0F, 0.0F),
			rotKf(1.42F, 4.0F, 3.6F, 0.0F),
			rotKf(1.6F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static AnimationDefinition create_walk() {
		AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.6F).looping();
		add_walk_0(b);
		add_walk_1(b);
		add_walk_2(b);
		add_walk_3(b);
		return b.build();
	}

	private static void add_run_0(AnimationDefinition.Builder b) {
		b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, -8.0F, 0.0F, 0.0F),
			rotKf(0.52F, 6.0F, 0.0F, 0.0F),
			rotKf(0.88F, 4.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
			posKf(0.0F, 0.0F, 0.0F, 0.0F),
			posKf(0.025F, 0.0F, 0.055F, 0.0F),
			posKf(0.05F, 0.0F, 0.091F, 0.0F),
			posKf(0.075F, 0.0F, 0.122F, 0.0F),
			posKf(0.1F, 0.0F, 0.149F, 0.0F),
			posKf(0.125F, 0.0F, 0.172F, 0.0F),
			posKf(0.15F, 0.0F, 0.191F, 0.0F),
			posKf(0.175F, 0.0F, 0.509F, 0.0F),
			posKf(0.2F, 0.0F, 0.819F, 0.0F),
			posKf(0.225F, 0.0F, 1.076F, 0.0F),
			posKf(0.25F, 0.0F, 1.14F, 0.0F),
			posKf(0.275F, 0.0F, 1.17F, 0.0F),
			posKf(0.3F, 0.0F, 1.162F, 0.0F),
			posKf(0.325F, 0.0F, 1.114F, 0.0F),
			posKf(0.35F, 0.0F, 1.122F, 0.0F),
			posKf(0.375F, 0.0F, 1.131F, 0.0F),
			posKf(0.4F, 0.0F, 1.457F, 0.0F),
			posKf(0.425F, 0.0F, 1.748F, 0.0F),
			posKf(0.45F, 0.0F, 1.999F, 0.0F),
			posKf(0.475F, 0.0F, 2.211F, 0.0F),
			posKf(0.5F, 0.0F, 2.384F, 0.0F),
			posKf(0.525F, 0.0F, 2.513F, 0.0F),
			posKf(0.55F, 0.0F, 2.582F, 0.0F),
			posKf(0.575F, 0.0F, 2.598F, 0.0F),
			posKf(0.6F, 0.0F, 2.56F, 0.0F),
			posKf(0.625F, 0.0F, 2.471F, 0.0F),
			posKf(0.65F, 0.0F, 2.333F, 0.0F),
			posKf(0.675F, 0.0F, 2.149F, 0.0F),
			posKf(0.7F, 0.0F, 1.921F, 0.0F),
			posKf(0.725F, 0.0F, 1.653F, 0.0F),
			posKf(0.75F, 0.0F, 1.35F, 0.0F),
			posKf(0.775F, 0.0F, 1.014F, 0.0F),
			posKf(0.8F, 0.0F, 0.648F, 0.0F),
			posKf(0.825F, 0.0F, 0.32F, 0.0F),
			posKf(0.85F, 0.0F, 0.214F, 0.0F),
			posKf(0.875F, 0.0F, 0.139F, 0.0F),
			posKf(0.9F, 0.0F, 0.149F, 0.0F),
			posKf(0.925F, 0.0F, 0.166F, 0.0F),
			posKf(0.95F, 0.0F, 0.177F, 0.0F),
			posKf(0.975F, 0.0F, 0.184F, 0.0F),
			posKf(1.0F, 0.0F, 0.185F, 0.0F),
			posKf(1.025F, 0.0F, 0.181F, 0.0F),
			posKf(1.05F, 0.0F, 0.173F, 0.0F),
			posKf(1.075F, 0.0F, 0.159F, 0.0F),
			posKf(1.1F, 0.0F, 0.14F, 0.0F),
			posKf(1.125F, 0.0F, 0.116F, 0.0F),
			posKf(1.15F, 0.0F, 0.087F, 0.0F),
			posKf(1.175F, 0.0F, 0.054F, 0.0F),
			posKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, -4.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("scale_flank_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, 0.0F, 0.0F, 16.0F),
			rotKf(0.52F, 0.0F, 0.0F, 22.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("scale_flank_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, 0.0F, 0.0F, -16.0F),
			rotKf(0.52F, 0.0F, 0.0F, -22.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, 6.0F, 0.0F, 0.0F),
			rotKf(0.52F, 10.0F, 0.0F, 0.0F),
			rotKf(0.88F, -6.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_run_1(AnimationDefinition.Builder b) {
		b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, -8.0F, 0.0F, 0.0F),
			rotKf(0.52F, -12.0F, 0.0F, 0.0F),
			rotKf(0.88F, 4.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, -32.0F, 0.0F, 0.0F),
			rotKf(0.52F, -36.0F, 0.0F, 0.0F),
			rotKf(0.88F, 14.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("knee_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, 42.0F, 0.0F, 0.0F),
			rotKf(0.52F, 18.0F, 0.0F, 0.0F),
			rotKf(0.88F, 8.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hoof_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.52F, 18.0F, 0.0F, 0.0F),
			rotKf(0.88F, -22.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, -24.0F, 0.0F, 0.0F),
			rotKf(0.52F, -28.0F, 0.0F, 0.0F),
			rotKf(0.88F, 8.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("knee_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, 36.0F, 0.0F, 0.0F),
			rotKf(0.52F, 14.0F, 0.0F, 0.0F),
			rotKf(0.88F, 10.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_run_2(AnimationDefinition.Builder b) {
		b.addAnimation("hoof_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.52F, 14.0F, 0.0F, 0.0F),
			rotKf(0.88F, -18.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, 18.0F, 0.0F, 0.0F),
			rotKf(0.52F, 30.0F, 0.0F, 0.0F),
			rotKf(0.88F, -22.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, -14.0F, 0.0F, 0.0F),
			rotKf(0.52F, -18.0F, 0.0F, 0.0F),
			rotKf(0.88F, 24.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hoof_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, -4.0F, 0.0F, 0.0F),
			rotKf(0.88F, -2.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, 22.0F, 0.0F, 0.0F),
			rotKf(0.52F, 24.0F, 0.0F, 0.0F),
			rotKf(0.88F, -18.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, -16.0F, 0.0F, 0.0F),
			rotKf(0.52F, -16.0F, 0.0F, 0.0F),
			rotKf(0.88F, 20.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_run_3(AnimationDefinition.Builder b) {
		b.addAnimation("hoof_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, -6.0F, 0.0F, 0.0F),
			rotKf(0.88F, -2.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.22F, 14.0F, 0.0F, 0.0F),
			rotKf(0.52F, 18.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.52F, 10.0F, 0.0F, 0.0F),
			rotKf(1.2F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static AnimationDefinition create_run() {
		AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.2F).looping();
		add_run_0(b);
		add_run_1(b);
		add_run_2(b);
		add_run_3(b);
		return b.build();
	}

	private static void add_attack_0(AnimationDefinition.Builder b) {
		b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, -10.0F, 0.0F, 0.0F),
			rotKf(0.72F, 8.0F, 0.0F, 0.0F),
			rotKf(0.92F, 3.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
			posKf(0.0F, 0.0F, 0.0F, 0.0F),
			posKf(0.025F, 0.0F, 0.08F, 0.06F),
			posKf(0.05F, 0.0F, 0.131F, 0.119F),
			posKf(0.075F, 0.0F, 0.182F, 0.179F),
			posKf(0.1F, 0.0F, 0.233F, 0.238F),
			posKf(0.125F, 0.0F, 0.283F, 0.298F),
			posKf(0.15F, 0.0F, 0.333F, 0.357F),
			posKf(0.175F, 0.0F, 0.382F, 0.417F),
			posKf(0.2F, 0.0F, 0.431F, 0.476F),
			posKf(0.225F, 0.0F, 0.479F, 0.536F),
			posKf(0.25F, 0.0F, 0.526F, 0.595F),
			posKf(0.275F, 0.0F, 0.574F, 0.655F),
			posKf(0.3F, 0.0F, 0.62F, 0.714F),
			posKf(0.325F, 0.0F, 0.667F, 0.774F),
			posKf(0.35F, 0.0F, 0.712F, 0.833F),
			posKf(0.375F, 0.0F, 0.757F, 0.893F),
			posKf(0.4F, 0.0F, 0.802F, 0.952F),
			posKf(0.425F, 0.0F, 0.803F, 0.937F),
			posKf(0.45F, 0.0F, 0.625F, 0.62F),
			posKf(0.475F, 0.0F, 0.431F, 0.303F),
			posKf(0.5F, 0.0F, 0.224F, -0.013F),
			posKf(0.525F, 0.0F, 0.003F, -0.33F),
			posKf(0.55F, 0.0F, -0.232F, -0.647F),
			posKf(0.575F, 0.0F, -0.041F, -0.963F),
			posKf(0.6F, 0.0F, 0.21F, -1.28F),
			posKf(0.625F, 0.0F, 0.431F, -1.597F),
			posKf(0.65F, 0.0F, 0.623F, -1.913F),
			posKf(0.675F, 0.0F, 0.784F, -2.23F),
			posKf(0.7F, 0.0F, 0.915F, -2.547F),
			posKf(0.725F, 0.0F, 1.028F, -2.8F),
			posKf(0.75F, 0.0F, 0.984F, -2.8F),
			posKf(0.775F, 0.0F, 0.934F, -2.8F),
			posKf(0.8F, 0.0F, 0.889F, -2.8F),
			posKf(0.825F, 0.0F, 0.836F, -2.8F),
			posKf(0.85F, 0.0F, 0.773F, -2.8F),
			posKf(0.875F, 0.0F, 0.7F, -2.8F),
			posKf(0.9F, 0.0F, 0.619F, -2.8F),
			posKf(0.925F, 0.0F, 0.543F, -2.771F),
			posKf(0.95F, 0.0F, 0.52F, -2.625F),
			posKf(0.975F, 0.0F, 0.497F, -2.479F),
			posKf(1.0F, 0.0F, 0.473F, -2.333F),
			posKf(1.025F, 0.0F, 0.449F, -2.188F),
			posKf(1.05F, 0.0F, 0.425F, -2.042F),
			posKf(1.075F, 0.0F, 0.4F, -1.896F),
			posKf(1.1F, 0.0F, 0.374F, -1.75F),
			posKf(1.125F, 0.0F, 0.348F, -1.604F),
			posKf(1.15F, 0.0F, 0.321F, -1.458F),
			posKf(1.175F, 0.0F, 0.294F, -1.312F),
			posKf(1.2F, 0.0F, 0.267F, -1.167F),
			posKf(1.225F, 0.0F, 0.239F, -1.021F),
			posKf(1.25F, 0.0F, 0.21F, -0.875F),
			posKf(1.275F, 0.0F, 0.181F, -0.729F),
			posKf(1.3F, 0.0F, 0.151F, -0.583F),
			posKf(1.325F, 0.0F, 0.121F, -0.438F),
			posKf(1.35F, 0.0F, 0.091F, -0.292F),
			posKf(1.375F, 0.0F, 0.06F, -0.146F),
			posKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, -6.0F, 0.0F, 0.0F),
			rotKf(0.72F, 10.0F, 0.0F, 0.0F),
			rotKf(0.92F, 2.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("scale_flank_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 0.0F, 0.0F, 18.0F),
			rotKf(0.72F, 0.0F, 0.0F, 22.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("scale_flank_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 0.0F, 0.0F, -18.0F),
			rotKf(0.72F, 0.0F, 0.0F, -22.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, -12.0F, 0.0F, 0.0F),
			rotKf(0.72F, 24.0F, 0.0F, 0.0F),
			rotKf(0.92F, -6.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_attack_1(AnimationDefinition.Builder b) {
		b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 18.0F, 0.0F, 0.0F),
			rotKf(0.72F, 26.0F, 0.0F, 0.0F),
			rotKf(0.92F, -14.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("ear_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.72F, 0.0F, 12.0F, -8.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("ear_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.72F, 0.0F, -12.0F, 8.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, -28.0F, 0.0F, 0.0F),
			rotKf(0.72F, -22.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("knee_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 42.0F, 0.0F, 0.0F),
			rotKf(0.72F, 20.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hoof_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.72F, -16.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_attack_2(AnimationDefinition.Builder b) {
		b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, -14.0F, 0.0F, 0.0F),
			rotKf(0.72F, -16.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("knee_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 24.0F, 0.0F, 0.0F),
			rotKf(0.72F, 16.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hoof_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.72F, -18.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 8.0F, 0.0F, 0.0F),
			rotKf(0.72F, 18.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 8.0F, 0.0F, 0.0F),
			rotKf(0.72F, -14.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hoof_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, -6.0F, 0.0F, 0.0F),
			rotKf(0.72F, -12.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_attack_3(AnimationDefinition.Builder b) {
		b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 8.0F, 0.0F, 0.0F),
			rotKf(0.72F, 18.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 8.0F, 0.0F, 0.0F),
			rotKf(0.72F, -14.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hoof_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, -6.0F, 0.0F, 0.0F),
			rotKf(0.72F, -12.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 10.0F, 0.0F, 0.0F),
			rotKf(0.72F, 18.0F, 0.0F, 0.0F),
			rotKf(0.92F, 12.0F, 0.0F, 0.0F),
			rotKf(1.4F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static AnimationDefinition create_attack() {
		AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.4F);
		add_attack_0(b);
		add_attack_1(b);
		add_attack_2(b);
		add_attack_3(b);
		return b.build();
	}

	private static void add_light_refract_0(AnimationDefinition.Builder b) {
		b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, -4.0F, 0.0F, 0.0F),
			rotKf(1.35F, -3.68F, 0.0F, 0.0F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
			posKf(0.0F, 0.0F, 0.0F, 0.0F),
			posKf(0.025F, 0.0F, 0.025F, 0.0F),
			posKf(0.05F, 0.0F, 0.039F, 0.0F),
			posKf(0.075F, 0.0F, 0.052F, 0.0F),
			posKf(0.1F, 0.0F, 0.065F, 0.0F),
			posKf(0.125F, 0.0F, 0.078F, 0.0F),
			posKf(0.15F, 0.0F, 0.092F, 0.0F),
			posKf(0.175F, 0.0F, 0.105F, 0.0F),
			posKf(0.2F, 0.0F, 0.118F, 0.0F),
			posKf(0.225F, 0.0F, 0.131F, 0.0F),
			posKf(0.25F, 0.0F, 0.144F, 0.0F),
			posKf(0.275F, 0.0F, 0.157F, 0.0F),
			posKf(0.3F, 0.0F, 0.17F, 0.0F),
			posKf(0.325F, 0.0F, 0.184F, 0.0F),
			posKf(0.35F, 0.0F, 0.197F, 0.0F),
			posKf(0.375F, 0.0F, 0.21F, 0.0F),
			posKf(0.4F, 0.0F, 0.223F, 0.0F),
			posKf(0.425F, 0.0F, 0.236F, 0.0F),
			posKf(0.45F, 0.0F, 0.249F, 0.0F),
			posKf(0.475F, 0.0F, 0.261F, 0.0F),
			posKf(0.5F, 0.0F, 0.274F, 0.0F),
			posKf(0.525F, 0.0F, 0.287F, 0.0F),
			posKf(0.55F, 0.0F, 0.3F, 0.0F),
			posKf(0.575F, 0.0F, 0.299F, 0.0F),
			posKf(0.6F, 0.0F, 0.299F, 0.0F),
			posKf(0.625F, 0.0F, 0.298F, 0.0F),
			posKf(0.65F, 0.0F, 0.297F, 0.0F),
			posKf(0.675F, 0.0F, 0.297F, 0.0F),
			posKf(0.7F, 0.0F, 0.296F, 0.0F),
			posKf(0.725F, 0.0F, 0.295F, 0.0F),
			posKf(0.75F, 0.0F, 0.294F, 0.0F),
			posKf(0.775F, 0.0F, 0.294F, 0.0F),
			posKf(0.8F, 0.0F, 0.293F, 0.0F),
			posKf(0.825F, 0.0F, 0.292F, 0.0F),
			posKf(0.85F, 0.0F, 0.292F, 0.0F),
			posKf(0.875F, 0.0F, 0.291F, 0.0F),
			posKf(0.9F, 0.0F, 0.29F, 0.0F),
			posKf(0.925F, 0.0F, 0.289F, 0.0F),
			posKf(0.95F, 0.0F, 0.289F, 0.0F),
			posKf(0.975F, 0.0F, 0.288F, 0.0F),
			posKf(1.0F, 0.0F, 0.287F, 0.0F),
			posKf(1.025F, 0.0F, 0.287F, 0.0F),
			posKf(1.05F, 0.0F, 0.286F, 0.0F),
			posKf(1.075F, 0.0F, 0.285F, 0.0F),
			posKf(1.1F, 0.0F, 0.285F, 0.0F),
			posKf(1.125F, 0.0F, 0.284F, 0.0F),
			posKf(1.15F, 0.0F, 0.283F, 0.0F),
			posKf(1.175F, 0.0F, 0.282F, 0.0F),
			posKf(1.2F, 0.0F, 0.282F, 0.0F),
			posKf(1.225F, 0.0F, 0.281F, 0.0F),
			posKf(1.25F, 0.0F, 0.28F, 0.0F),
			posKf(1.275F, 0.0F, 0.28F, 0.0F),
			posKf(1.3F, 0.0F, 0.279F, 0.0F),
			posKf(1.325F, 0.0F, 0.278F, 0.0F),
			posKf(1.35F, 0.0F, 0.277F, 0.0F),
			posKf(1.375F, 0.0F, 0.267F, 0.0F),
			posKf(1.4F, 0.0F, 0.257F, 0.0F),
			posKf(1.425F, 0.0F, 0.247F, 0.0F),
			posKf(1.45F, 0.0F, 0.237F, 0.0F),
			posKf(1.475F, 0.0F, 0.227F, 0.0F),
			posKf(1.5F, 0.0F, 0.217F, 0.0F),
			posKf(1.525F, 0.0F, 0.207F, 0.0F),
			posKf(1.55F, 0.0F, 0.197F, 0.0F),
			posKf(1.575F, 0.0F, 0.187F, 0.0F),
			posKf(1.6F, 0.0F, 0.176F, 0.0F),
			posKf(1.625F, 0.0F, 0.166F, 0.0F),
			posKf(1.65F, 0.0F, 0.156F, 0.0F),
			posKf(1.675F, 0.0F, 0.146F, 0.0F),
			posKf(1.7F, 0.0F, 0.136F, 0.0F),
			posKf(1.725F, 0.0F, 0.125F, 0.0F),
			posKf(1.75F, 0.0F, 0.115F, 0.0F),
			posKf(1.775F, 0.0F, 0.105F, 0.0F),
			posKf(1.8F, 0.0F, 0.095F, 0.0F),
			posKf(1.825F, 0.0F, 0.084F, 0.0F),
			posKf(1.85F, 0.0F, 0.074F, 0.0F),
			posKf(1.875F, 0.0F, 0.064F, 0.0F),
			posKf(1.9F, 0.0F, 0.053F, 0.0F),
			posKf(1.925F, 0.0F, 0.043F, 0.0F),
			posKf(1.95F, 0.0F, 0.033F, 0.0F),
			posKf(1.975F, 0.0F, 0.022F, 0.0F),
			posKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, -6.0F, 0.0F, 0.0F),
			rotKf(1.35F, -5.52F, 0.0F, 0.0F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("scale_flank_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, 0.0F, 0.0F, 26.0F),
			rotKf(0.95F, 0.0F, 0.0F, 16.0F),
			rotKf(1.35F, 0.0F, 0.0F, 23.92F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("scale_flank_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, 0.0F, 0.0F, -26.0F),
			rotKf(0.95F, 0.0F, 0.0F, -16.0F),
			rotKf(1.35F, 0.0F, 0.0F, -23.92F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, -14.0F, 0.0F, 0.0F),
			rotKf(1.35F, -12.88F, 0.0F, 0.0F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_light_refract_1(AnimationDefinition.Builder b) {
		b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, -16.0F, -8.0F, 6.0F),
			rotKf(1.35F, -14.72F, 8.0F, -6.0F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, 6.0F, 0.0F, 0.0F),
			rotKf(1.35F, 5.52F, 0.0F, 0.0F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("ear_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, 0.0F, -8.0F, 12.0F),
			rotKf(1.35F, 0.0F, -8.0F, 12.0F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("ear_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, 0.0F, 8.0F, -12.0F),
			rotKf(1.35F, 0.0F, 8.0F, -12.0F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, 4.0F, 0.0F, 0.0F),
			rotKf(1.35F, 3.68F, 0.0F, 0.0F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, 4.0F, 0.0F, 0.0F),
			rotKf(1.35F, 3.68F, 0.0F, 0.0F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_light_refract_2(AnimationDefinition.Builder b) {
		b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, 4.0F, 0.0F, 0.0F),
			rotKf(1.35F, 3.68F, 0.0F, 0.0F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, 4.0F, 0.0F, 0.0F),
			rotKf(1.35F, 3.68F, 0.0F, 0.0F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, 18.0F, -8.0F, 0.0F),
			rotKf(1.35F, 16.56F, 8.0F, 0.0F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.55F, 10.0F, -12.0F, 0.0F),
			rotKf(1.35F, 9.2F, 12.0F, 0.0F),
			rotKf(2.0F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static AnimationDefinition create_light_refract() {
		AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(2.0F);
		add_light_refract_0(b);
		add_light_refract_1(b);
		add_light_refract_2(b);
		return b.build();
	}

	private static void add_hurt_0(AnimationDefinition.Builder b) {
		b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, -6.0F, 4.0F, 3.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.POSITION,
			posKf(0.0F, 0.0F, 0.0F, 0.0F),
			posKf(0.025F, 0.0F, 0.183F, 0.0F),
			posKf(0.05F, 0.0F, 0.346F, 0.0F),
			posKf(0.075F, 0.0F, 0.502F, 0.0F),
			posKf(0.1F, 0.0F, 0.65F, 0.0F),
			posKf(0.125F, 0.0F, 0.792F, 0.0F),
			posKf(0.15F, 0.0F, 0.926F, 0.0F),
			posKf(0.175F, 0.0F, 0.882F, 0.0F),
			posKf(0.2F, 0.0F, 0.837F, 0.0F),
			posKf(0.225F, 0.0F, 0.792F, 0.0F),
			posKf(0.25F, 0.0F, 0.745F, 0.0F),
			posKf(0.275F, 0.0F, 0.698F, 0.0F),
			posKf(0.3F, 0.0F, 0.65F, 0.0F),
			posKf(0.325F, 0.0F, 0.602F, 0.0F),
			posKf(0.35F, 0.0F, 0.552F, 0.0F),
			posKf(0.375F, 0.0F, 0.502F, 0.0F),
			posKf(0.4F, 0.0F, 0.451F, 0.0F),
			posKf(0.425F, 0.0F, 0.399F, 0.0F),
			posKf(0.45F, 0.0F, 0.346F, 0.0F),
			posKf(0.475F, 0.0F, 0.292F, 0.0F),
			posKf(0.5F, 0.0F, 0.238F, 0.0F),
			posKf(0.525F, 0.0F, 0.183F, 0.0F),
			posKf(0.55F, 0.0F, 0.127F, 0.0F),
			posKf(0.575F, 0.0F, 0.07F, 0.0F),
			posKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, -6.0F, 4.0F, 3.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("scale_flank_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, 0.0F, 0.0F, 18.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("scale_flank_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, 0.0F, 0.0F, -18.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, -16.0F, 8.0F, 0.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_hurt_1(AnimationDefinition.Builder b) {
		b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, -14.0F, -12.0F, -8.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, 8.0F, 0.0F, 0.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("ear_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, 0.0F, 16.0F, -10.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("ear_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, 0.0F, -16.0F, 10.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, -14.0F, 0.0F, 0.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("knee_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, 26.0F, 0.0F, 0.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static void add_hurt_2(AnimationDefinition.Builder b) {
		b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, 8.0F, 0.0F, 0.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, 8.0F, 0.0F, 0.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, 8.0F, 0.0F, 0.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, 8.0F, 0.0F, 0.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
		b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.15F, -10.0F, 0.0F, 0.0F),
			rotKf(0.6F, 0.0F, 0.0F, 0.0F)
		));
	}

	private static AnimationDefinition create_hurt() {
		AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(0.6F);
		add_hurt_0(b);
		add_hurt_1(b);
		add_hurt_2(b);
		return b.build();
	}

	private static void add_death_0(AnimationDefinition.Builder b) {
		b.addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
			posKf(0.0F, 0.0F, 0.0F, 0.0F),
			posKf(0.025F, 0.048F, 0.213F, 0.0F),
			posKf(0.05F, 0.095F, 0.383F, 0.0F),
			posKf(0.075F, 0.143F, 0.532F, 0.0F),
			posKf(0.1F, 0.19F, 0.662F, 0.0F),
			posKf(0.125F, 0.238F, 0.772F, 0.0F),
			posKf(0.15F, 0.286F, 0.862F, 0.0F),
			posKf(0.175F, 0.333F, 0.932F, 0.0F),
			posKf(0.2F, 0.381F, 0.983F, 0.0F),
			posKf(0.225F, 0.429F, 1.014F, 0.0F),
			posKf(0.25F, 0.476F, 1.026F, 0.0F),
			posKf(0.275F, 0.524F, 1.019F, 0.0F),
			posKf(0.3F, 0.571F, 0.997F, 0.0F),
			posKf(0.325F, 0.619F, 0.994F, 0.0F),
			posKf(0.35F, 0.667F, 0.979F, 0.0F),
			posKf(0.375F, 0.714F, 0.951F, 0.0F),
			posKf(0.4F, 0.762F, 0.911F, 0.0F),
			posKf(0.425F, 0.825F, 0.877F, 0.0F),
			posKf(0.45F, 0.952F, 0.889F, 0.0F),
			posKf(0.475F, 1.079F, 0.863F, 0.0F),
			posKf(0.5F, 1.206F, 0.8F, 0.0F),
			posKf(0.525F, 1.333F, 0.7F, 0.0F),
			posKf(0.55F, 1.46F, 0.564F, 0.0F),
			posKf(0.575F, 1.587F, 0.391F, 0.0F),
			posKf(0.6F, 1.714F, 0.182F, 0.0F),
			posKf(0.625F, 1.841F, -0.062F, 0.0F),
			posKf(0.65F, 1.968F, -0.341F, 0.0F),
			posKf(0.675F, 2.095F, -0.654F, 0.0F),
			posKf(0.7F, 2.222F, -1.001F, 0.0F),
			posKf(0.725F, 2.349F, -1.38F, 0.0F),
			posKf(0.75F, 2.476F, -1.79F, 0.0F),
			posKf(0.775F, 2.603F, -2.231F, 0.0F),
			posKf(0.8F, 2.73F, -2.702F, 0.0F),
			posKf(0.825F, 2.857F, -3.201F, 0.0F),
			posKf(0.85F, 2.984F, -3.726F, 0.0F),
			posKf(0.875F, 3.111F, -4.278F, 0.0F),
			posKf(0.9F, 3.238F, -4.853F, 0.0F),
			posKf(0.925F, 3.365F, -5.452F, 0.0F),
			posKf(0.95F, 3.492F, -6.072F, 0.0F),
			posKf(0.975F, 3.619F, -6.712F, 0.0F),
			posKf(1.0F, 3.746F, -7.37F, 0.0F),
			posKf(1.025F, 3.873F, -8.045F, 0.0F),
			posKf(1.05F, 4.0F, -8.413F, 0.0F),
			posKf(1.075F, 4.0F, -8.364F, 0.0F),
			posKf(1.1F, 4.0F, -8.315F, 0.0F),
			posKf(1.125F, 4.0F, -8.266F, 0.0F),
			posKf(1.15F, 4.0F, -8.217F, 0.0F),
			posKf(1.175F, 4.0F, -8.168F, 0.0F),
			posKf(1.2F, 4.0F, -8.119F, 0.0F),
			posKf(1.225F, 4.0F, -8.07F, 0.0F),
			posKf(1.25F, 4.0F, -8.021F, 0.0F),
			posKf(1.275F, 4.0F, -7.972F, 0.0F),
			posKf(1.3F, 4.0F, -7.923F, 0.0F),
			posKf(1.325F, 4.0F, -7.874F, 0.0F),
			posKf(1.35F, 4.0F, -7.826F, 0.0F),
			posKf(1.375F, 4.0F, -7.777F, 0.0F),
			posKf(1.4F, 4.0F, -7.728F, 0.0F),
			posKf(1.425F, 4.0F, -7.68F, 0.0F),
			posKf(1.45F, 4.0F, -7.631F, 0.0F),
			posKf(1.475F, 4.0F, -7.582F, 0.0F),
			posKf(1.5F, 4.0F, -7.534F, 0.0F),
			posKf(1.525F, 4.0F, -7.485F, 0.0F),
			posKf(1.55F, 4.0F, -7.437F, 0.0F),
			posKf(1.575F, 4.0F, -7.388F, 0.0F),
			posKf(1.6F, 4.0F, -7.34F, 0.0F)
		));
		b.addAnimation("body_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 6.0F, 0.0F, 16.0F),
			rotKf(1.05F, 0.0F, 0.0F, 83.42F),
			rotKf(1.6F, 0.0F, 0.0F, 86.0F)
		));
		b.addAnimation("chest_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(1.05F, 4.0F, 0.0F, 2.0F),
			rotKf(1.6F, 4.0F, 0.0F, 2.0F)
		));
		b.addAnimation("scale_flank_l_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(1.05F, 0.0F, 0.0F, -8.0F),
			rotKf(1.6F, 0.0F, 0.0F, -8.0F)
		));
		b.addAnimation("scale_flank_r_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(1.05F, 0.0F, 0.0F, -14.0F),
			rotKf(1.6F, 0.0F, 0.0F, -14.0F)
		));
		b.addAnimation("neck_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, -18.0F, -8.0F, 8.0F),
			rotKf(1.05F, 18.0F, -8.0F, 6.0F),
			rotKf(1.6F, 18.0F, -8.0F, 6.0F)
		));
	}

	private static void add_death_1(AnimationDefinition.Builder b) {
		b.addAnimation("head_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, -16.0F, -12.0F, 0.0F),
			rotKf(1.05F, 10.0F, -8.0F, 4.0F),
			rotKf(1.6F, 10.0F, -8.0F, 4.0F)
		));
		b.addAnimation("jaw_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 8.0F, 0.0F, 0.0F),
			rotKf(1.05F, 4.0F, 0.0F, 0.0F),
			rotKf(1.6F, 4.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, -24.0F, 0.0F, 0.0F),
			rotKf(1.05F, -22.0F, 0.0F, 6.0F),
			rotKf(1.6F, -22.0F, 0.0F, 6.0F)
		));
		b.addAnimation("knee_fl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 52.0F, 0.0F, 0.0F),
			rotKf(1.05F, 44.0F, 0.0F, 0.0F),
			rotKf(1.6F, 44.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, -28.0F, 0.0F, 0.0F),
			rotKf(1.05F, -12.0F, 0.0F, -4.0F),
			rotKf(1.6F, -12.0F, 0.0F, -4.0F)
		));
		b.addAnimation("knee_fr_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 58.0F, 0.0F, 0.0F),
			rotKf(1.05F, 28.0F, 0.0F, 0.0F),
			rotKf(1.6F, 28.0F, 0.0F, 0.0F)
		));
	}

	private static void add_death_2(AnimationDefinition.Builder b) {
		b.addAnimation("leg_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, -20.0F, 0.0F, 0.0F),
			rotKf(1.05F, 14.0F, 0.0F, 6.0F),
			rotKf(1.6F, 14.0F, 0.0F, 6.0F)
		));
		b.addAnimation("hock_bl_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 38.0F, 0.0F, 0.0F),
			rotKf(1.05F, 24.0F, 0.0F, 0.0F),
			rotKf(1.6F, 24.0F, 0.0F, 0.0F)
		));
		b.addAnimation("leg_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, -24.0F, 0.0F, 0.0F),
			rotKf(1.05F, 8.0F, 0.0F, -4.0F),
			rotKf(1.6F, 8.0F, 0.0F, -4.0F)
		));
		b.addAnimation("hock_br_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(0.42F, 44.0F, 0.0F, 0.0F),
			rotKf(1.05F, 18.0F, 0.0F, 0.0F),
			rotKf(1.6F, 18.0F, 0.0F, 0.0F)
		));
		b.addAnimation("tail_1_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(1.05F, -8.0F, 14.0F, 0.0F),
			rotKf(1.6F, -8.0F, 14.0F, 0.0F)
		));
		b.addAnimation("tail_2_grp", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			rotKf(0.0F, 0.0F, 0.0F, 0.0F),
			rotKf(1.05F, -6.0F, 10.0F, 0.0F),
			rotKf(1.6F, -6.0F, 10.0F, 0.0F)
		));
	}

	private static AnimationDefinition create_death() {
		AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(1.6F);
		add_death_0(b);
		add_death_1(b);
		add_death_2(b);
		return b.build();
	}

}
