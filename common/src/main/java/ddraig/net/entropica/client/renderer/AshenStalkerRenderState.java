package ddraig.net.entropica.client.renderer;

import net.minecraft.world.entity.AnimationState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class AshenStalkerRenderState extends LivingEntityRenderState {
    public float xRot;
    public float yRot;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState stalkAnimationState = new AnimationState();
    public final AnimationState sprintAnimationState = new AnimationState();
    public final AnimationState feedAnimationState = new AnimationState();
    public final AnimationState scentAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
}