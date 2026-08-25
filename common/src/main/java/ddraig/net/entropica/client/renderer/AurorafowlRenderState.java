package ddraig.net.entropica.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class AurorafowlRenderState extends LivingEntityRenderState {
    public final AnimationState idleGroundAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState idleFlightAnimationState = new AnimationState();
    public final AnimationState flyingAnimationState = new AnimationState();
    public final AnimationState takingOffAnimationState = new AnimationState();
    public final AnimationState landingAnimationState = new AnimationState();
    public final AnimationState attackGroundAnimationState = new AnimationState();
    public final AnimationState attackFlyingAnimationState = new AnimationState();

    public boolean isFlying;
    public boolean isSheared;
}
