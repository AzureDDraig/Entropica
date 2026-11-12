package ddraig.net.entropica.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class RimeBackOvisRenderState extends LivingEntityRenderState {
    public boolean isSheared;
    public boolean isBraced;
    public boolean isStunned;
    public boolean isCharging;
    public int variant;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState grazeAnimationState = new AnimationState();
    public final AnimationState restAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState headbuttAnimationState = new AnimationState();
}
