package ddraig.net.entropica.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class GeyserWiggleWormRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState slitherAnimationState = new AnimationState();
    public final AnimationState rearUpAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState ventSteamAnimationState = new AnimationState();

    public boolean isVentingSteam;
    public boolean isRearingUp;
    public boolean isAttacking;
}
