package ddraig.net.entropica.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.Entity;

public class EntityHelper {
    @ExpectPlatform
    public static int getFluidTime(Entity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setFluidTime(Entity entity, int time) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean hasEssenceDropped(Entity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setEssenceDropped(Entity entity, boolean dropped) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static net.minecraft.nbt.CompoundTag getPersistentData(Entity entity) {
        throw new AssertionError();
    }
}
