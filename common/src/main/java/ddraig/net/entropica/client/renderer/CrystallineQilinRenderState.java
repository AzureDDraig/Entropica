package ddraig.net.entropica.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class CrystallineQilinRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState lightRefractAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    public final AnimationState idle = idleAnimationState;
    public final AnimationState walk = walkAnimationState;
    public final AnimationState run = runAnimationState;
    public final AnimationState attack = attackAnimationState;
    public final AnimationState lightRefract = lightRefractAnimationState;
    public final AnimationState hurt = hurtAnimationState;
    public final AnimationState death = deathAnimationState;

    public boolean isRefracting;
    public boolean isProvoked;
}
