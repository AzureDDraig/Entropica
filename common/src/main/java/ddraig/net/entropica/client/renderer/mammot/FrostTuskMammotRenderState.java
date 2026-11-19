package ddraig.net.entropica.client.renderer.mammot;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class FrostTuskMammotRenderState extends LivingEntityRenderState {
    public boolean isSheared;
    public boolean isAttacking;
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
}
