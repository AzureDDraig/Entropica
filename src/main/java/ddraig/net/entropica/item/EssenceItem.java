package ddraig.net.entropica.item;

import ddraig.net.entropica.entity.EssenceOrbEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class EssenceItem extends Item {
    public EssenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        if (location instanceof ItemEntity vanillaEntity) {
            EssenceOrbEntity orb = new EssenceOrbEntity(level, vanillaEntity.getX(), vanillaEntity.getY(), vanillaEntity.getZ(), stack);

            orb.setDeltaMovement(vanillaEntity.getDeltaMovement());
            orb.setPickUpDelay(40);

            // Copy the owner from the original item entity so getThrower works
            if (vanillaEntity.getOwner() instanceof Player) {
                orb.markThrownByPlayer();
            }

            return orb;
        }
        return null;
    }
}