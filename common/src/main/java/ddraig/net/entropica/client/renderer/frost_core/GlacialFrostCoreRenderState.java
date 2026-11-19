package ddraig.net.entropica.client.renderer.frost_core;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class GlacialFrostCoreRenderState extends LivingEntityRenderState {
    public boolean isAttacking;
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
}
