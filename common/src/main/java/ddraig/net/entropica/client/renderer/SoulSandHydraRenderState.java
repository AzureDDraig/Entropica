package ddraig.net.entropica.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class SoulSandHydraRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState submergeAmbushAnimationState = new AnimationState();
    public final AnimationState witherRoarAnimationState = new AnimationState();
    public final AnimationState spellChargeAnimationState = new AnimationState();
    public final AnimationState spellReleaseAnimationState = new AnimationState();
    public final AnimationState breathAttackAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    public boolean isSubmergedAmbush;
    public boolean isChargingRun;
    public boolean isGlandGlowing;
}
