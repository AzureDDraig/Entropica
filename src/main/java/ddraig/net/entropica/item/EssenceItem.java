package ddraig.net.entropica.item;

import ddraig.net.entropica.entity.EssenceOrbEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class EssenceItem extends Item {
    public EssenceItem(Properties properties) {
        super(properties);
    }

    // Tells NeoForge that this item uses a custom entity when dropped
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    // Intercepts the drop and replaces the vanilla item entity with our orb
    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        if (location instanceof ItemEntity vanillaEntity) {
            // We haven't made ModEntityTypes yet, but we will point to it here
            EssenceOrbEntity orb = new EssenceOrbEntity(level, vanillaEntity.getX(), vanillaEntity.getY(), vanillaEntity.getZ(), stack);

            // Match the motion and pickup delay of the original dropped item
            orb.setDeltaMovement(vanillaEntity.getDeltaMovement());
            orb.setPickUpDelay(40);

            return orb;
        }
        return null;
    }
}