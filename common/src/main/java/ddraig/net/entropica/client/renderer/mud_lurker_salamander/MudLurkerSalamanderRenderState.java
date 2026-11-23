package ddraig.net.entropica.client.renderer.mud_lurker_salamander;

import ddraig.net.entropica.entity.mud_lurker_salamander.MudLurkerSalamanderVariant;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class MudLurkerSalamanderRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState enterWaterAnimationState = new AnimationState();
    public final AnimationState swimIdleAnimationState = new AnimationState();
    public final AnimationState swimAnimationState = new AnimationState();
    public final AnimationState swimAttackAnimationState = new AnimationState();
    public final AnimationState exitWaterAnimationState = new AnimationState();

    public MudLurkerSalamanderVariant variant = MudLurkerSalamanderVariant.OVERWORLD_MUD;
    public boolean inFluid = false;
}
