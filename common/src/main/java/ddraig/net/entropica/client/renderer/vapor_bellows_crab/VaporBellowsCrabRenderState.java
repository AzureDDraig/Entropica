package ddraig.net.entropica.client.renderer.vapor_bellows_crab;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class VaporBellowsCrabRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState steamVentAnimationState = new AnimationState();
    public final AnimationState braceDefenseAnimationState = new AnimationState();

    public boolean ventCharged;
}
