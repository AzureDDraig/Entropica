package ddraig.net.entropica.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;

public class EssenceOrbEntity extends ItemEntity {

    public EssenceOrbEntity(EntityType<? extends ItemEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public EssenceOrbEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntityTypes.ESSENCE_ORB.get(), level);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isNoGravity()) this.setNoGravity(true);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.95D, 0.95D, 0.95D)); // Gentle friction
    }

    // --- NEW METHODS FOR RENDERING ---

    public int getTier() {
        String name = BuiltInRegistries.ITEM.getKey(this.getItem().getItem()).getPath();
        if (name.contains("strong")) return 3;   // Large
        if (name.contains("average")) return 2;  // Medium
        return 1;                                // Small (Weak)
    }

    public EssenceType getEssenceType() {
        String name = BuiltInRegistries.ITEM.getKey(this.getItem().getItem()).getPath().toUpperCase();
        for (EssenceType type : EssenceType.values()) {
            if (name.contains(type.name())) {
                return type;
            }
        }
        return EssenceType.REGULAR; // Fallback
    }
}