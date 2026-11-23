package ddraig.net.entropica.client.renderer.cryo_static_hedgehog;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class CryoStaticHedgehogRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState curlAnimationState = new AnimationState();
    public final AnimationState sneezeAnimationState = new AnimationState();
    public final AnimationState digInAnimationState = new AnimationState();
    public final AnimationState digHoldAnimationState = new AnimationState();
    public final AnimationState digOutAnimationState = new AnimationState();

    public boolean isCurled;
    public boolean isStormCharged;
}
