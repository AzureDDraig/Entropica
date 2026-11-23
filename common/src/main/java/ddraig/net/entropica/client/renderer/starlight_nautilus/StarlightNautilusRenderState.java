package ddraig.net.entropica.client.renderer.starlight_nautilus;

import ddraig.net.entropica.entity.starlight_nautilus.StarlightNautilusArmorType;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class StarlightNautilusRenderState extends LivingEntityRenderState {
    public boolean isSaddled = false;
    public StarlightNautilusArmorType armorType = StarlightNautilusArmorType.NONE;
    public float dashProgress = 0.0F;
    public boolean inWater = false;
    public boolean isSitting = false;
    public boolean isRidden = false;
    public int flightBuffKind = 0;
}
