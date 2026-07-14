package ddraig.net.entropica.util.fabric;

import ddraig.net.entropica.util.PersistentDataHolder;

import net.minecraft.world.entity.Entity;

public class EntityHelperImpl {
    public static int getFluidTime(Entity entity) {
        if (entity instanceof PersistentDataHolder holder) {
            return holder.entropica$getPersistentData().getInt("EntropicaFluidTime").orElse(0);
        }
        return 0;
    }

    public static void setFluidTime(Entity entity, int time) {
        if (entity instanceof PersistentDataHolder holder) {
            holder.entropica$getPersistentData().putInt("EntropicaFluidTime", time);
        }
    }

    public static boolean hasEssenceDropped(Entity entity) {
        if (entity instanceof PersistentDataHolder holder) {
            return holder.entropica$getPersistentData().getBoolean("EntropicaEssenceDropped").orElse(false);
        }
        return false;
    }

    public static void setEssenceDropped(Entity entity, boolean dropped) {
        if (entity instanceof PersistentDataHolder holder) {
            holder.entropica$getPersistentData().putBoolean("EntropicaEssenceDropped", dropped);
        }
    }
}
