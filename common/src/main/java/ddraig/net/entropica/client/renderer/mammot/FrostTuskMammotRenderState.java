package ddraig.net.entropica.client.renderer.mammot;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class FrostTuskMammotRenderState extends LivingEntityRenderState {
    public boolean isSheared;
    public boolean isCharging;
    public boolean isAttacking;
    public int restPhase;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState chargeAnimationState = new AnimationState();
    public final AnimationState laydownAnimationState = new AnimationState();
    public final AnimationState layAnimationState = new AnimationState();
    public final AnimationState standUpAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();
}
