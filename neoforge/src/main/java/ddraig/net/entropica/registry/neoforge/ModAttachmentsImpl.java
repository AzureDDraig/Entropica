package ddraig.net.entropica.registry.neoforge;

import net.minecraft.world.entity.LivingEntity;

public class ModAttachmentsImpl {
    public static String getToxicitySource(LivingEntity entity) {
        return entity.getData(ModAttachmentsNeoForge.TOXICITY_SOURCE.get());
    }

    public static void setToxicitySource(LivingEntity entity, String source) {
        entity.setData(ModAttachmentsNeoForge.TOXICITY_SOURCE.get(), source);
    }
}
