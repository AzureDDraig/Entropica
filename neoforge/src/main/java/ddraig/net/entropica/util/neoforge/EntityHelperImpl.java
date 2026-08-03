package ddraig.net.entropica.util.neoforge;

import ddraig.net.entropica.util.PersistentDataHolder;

import net.minecraft.world.entity.Entity;

public class EntityHelperImpl {
    public static int getFluidTime(Entity entity) {
        return entity.getPersistentData().getInt("EntropicaFluidTime").orElse(0);
    }

    public static void setFluidTime(Entity entity, int time) {
        entity.getPersistentData().putInt("EntropicaFluidTime", time);
    }

    public static boolean hasEssenceDropped(Entity entity) {
        return entity.getPersistentData().getBoolean("EntropicaEssenceDropped").orElse(false);
    }

    public static void setEssenceDropped(Entity entity, boolean dropped) {
        entity.getPersistentData().putBoolean("EntropicaEssenceDropped", dropped);
    }

    public static net.minecraft.nbt.CompoundTag getPersistentData(Entity entity) {
        return entity.getPersistentData();
    }
}
