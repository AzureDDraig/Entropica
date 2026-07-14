package ddraig.net.entropica.registry;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.LivingEntity;

public class ModAttachments {
    @ExpectPlatform
    public static String getToxicitySource(LivingEntity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setToxicitySource(LivingEntity entity, String source) {
        throw new AssertionError();
    }
}
