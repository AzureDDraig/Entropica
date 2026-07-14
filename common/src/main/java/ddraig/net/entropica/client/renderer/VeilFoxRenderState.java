package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class VeilFoxRenderState extends LivingEntityRenderState {
    public EssenceType essenceType;
    public boolean isSitting = false;

    // Alert System
    public boolean isDetecting = false;

    public float bodyRot = 0.0f;
    public float strobeAlpha = 1.0f;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState blinkOutAnimationState = new AnimationState();
    public final AnimationState blinkInAnimationState = new AnimationState();
    public final AnimationState sleepAnimationState = new AnimationState();
    public final AnimationState sleepInAnimationState = new AnimationState();
    public final AnimationState sleepOutAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState rolloverAnimationState = new AnimationState();
    public final AnimationState digAnimationState = new AnimationState();
}