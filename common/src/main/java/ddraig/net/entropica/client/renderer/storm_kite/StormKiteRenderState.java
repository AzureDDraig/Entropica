package ddraig.net.entropica.client.renderer.storm_kite;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class StormKiteRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState moveAnimationState = new AnimationState();
    public final AnimationState flapAnimationState = new AnimationState();
    public final AnimationState diveAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
}
