package ddraig.net.entropica.registry.fabric;

import ddraig.net.entropica.util.PersistentDataHolder;
import net.minecraft.world.entity.LivingEntity;

public class ModAttachmentsImpl {
    public static String getToxicitySource(LivingEntity entity) {
        if (entity instanceof PersistentDataHolder holder) {
            return holder.entropica$getPersistentData().getString("entropica_toxicity_source").orElse("");
        }
        return "";
    }

    public static void setToxicitySource(LivingEntity entity, String source) {
        if (entity instanceof PersistentDataHolder holder) {
            holder.entropica$getPersistentData().putString("entropica_toxicity_source", source);
        }
    }
}
