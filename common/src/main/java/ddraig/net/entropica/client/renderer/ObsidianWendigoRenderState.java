package ddraig.net.entropica.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class ObsidianWendigoRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkStartAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState walkStopAnimationState = new AnimationState();
    public final AnimationState runStartAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState runStopAnimationState = new AnimationState();
    public final AnimationState climbStartAnimationState = new AnimationState();
    public final AnimationState climbAnimationState = new AnimationState();
    public final AnimationState climbStopAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState screechAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    public boolean isSprintingRun = false;
    public boolean isClimbingTrunk = false;
    public boolean isScreeching = false;
    public boolean isShadowCloaked = false;
}
