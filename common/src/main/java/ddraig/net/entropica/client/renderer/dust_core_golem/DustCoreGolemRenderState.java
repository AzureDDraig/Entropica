package ddraig.net.entropica.client.renderer.dust_core_golem;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class DustCoreGolemRenderState extends LivingEntityRenderState {
    public int variant;
    public int actionState;
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState repairAnimationState = new AnimationState();
}
