package ddraig.net.entropica.client.renderer.gemini_goat_mage;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class GeminiGoatMageRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState castSolarAnimationState = new AnimationState();
    public final AnimationState castUmbralAnimationState = new AnimationState();
    public final AnimationState castDualAnimationState = new AnimationState();

    public int castingPhase = 0;
    public boolean isWitnessingAltar = false;
}
