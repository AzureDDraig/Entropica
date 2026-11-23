package ddraig.net.entropica.client.renderer.aurora_jelly;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class AuroraJellyRenderState extends LivingEntityRenderState {
    public boolean isStartled;
    public boolean isMagnetizing;
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState magnetizeAnimationState = new AnimationState();
    public final AnimationState startledFlashAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();
}
