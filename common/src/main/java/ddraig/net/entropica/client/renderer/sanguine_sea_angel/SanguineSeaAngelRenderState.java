package ddraig.net.entropica.client.renderer.sanguine_sea_angel;

import ddraig.net.entropica.entity.sanguine_sea_angel.SanguineSeaAngelVariant;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class SanguineSeaAngelRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState swimAnimationState = new AnimationState();
    public final AnimationState iceHoverAnimationState = new AnimationState();
    public final AnimationState iceGlideAnimationState = new AnimationState();
    public final AnimationState spitDartAnimationState = new AnimationState();
    public final AnimationState lungeAttackAnimationState = new AnimationState();
    public final AnimationState damagedAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    public SanguineSeaAngelVariant variant = SanguineSeaAngelVariant.POLAR_BLOOD_SHALLOWS;
    public boolean inWater = false;
    public boolean hunting = false;
}
