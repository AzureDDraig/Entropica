package ddraig.net.entropica.client.renderer.amber_weeping_stag;

import ddraig.net.entropica.entity.amber_weeping_stag.AmberStagVariant;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class AmberWeepingStagRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public AmberStagVariant variant = AmberStagVariant.AMBER;
}
