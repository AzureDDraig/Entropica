package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class GrotRenderState extends LivingEntityRenderState {
    public EssenceType essenceType;
    public float grotAgeInTicks;
    public float size = 1.0f;
    public float squish = 0.0f;
    public float grotDeathTime = 0.0f;

    // NEW: Tracking variables for the visual flashes!
    public int hurtTime = 0;
    public int healTime = 0;
}