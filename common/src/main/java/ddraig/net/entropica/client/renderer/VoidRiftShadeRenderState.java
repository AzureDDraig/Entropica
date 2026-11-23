package ddraig.net.entropica.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class VoidRiftShadeRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState blinkOutAnimationState = new AnimationState();
    public final AnimationState blinkInAnimationState = new AnimationState();
    public final AnimationState aggroAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState hurtStaggerAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    public boolean isProvoked;
    public boolean isRiftStabilized;
}
