package ddraig.net.entropica.event.neoforge;

import ddraig.net.entropica.event.VisTooltipEventHandler;
import ddraig.net.entropica.event.MobDropHandler;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;

import ddraig.net.entropica.Entropica;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Entropica.MODID)
public class NeoForgeEventSubscriber {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        VisTooltipEventHandler.handle(event.getItemStack(), event.getToolTip());
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        MobDropHandler.onMobDeath(event.getEntity(), event.getSource());
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (MobDropHandler.shouldCancelExperience(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
