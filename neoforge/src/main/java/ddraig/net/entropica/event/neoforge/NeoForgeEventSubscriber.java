package ddraig.net.entropica.event.neoforge;

import ddraig.net.entropica.event.VisTooltipEventHandler;
import ddraig.net.entropica.event.MobDropHandler;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.Entropica;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

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

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity().hasEffect(ModEffects.PARALYZED)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().hasEffect(ModEffects.PARALYZED)) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().hasEffect(ModEffects.PARALYZED)) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity().hasEffect(ModEffects.PARALYZED)) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }
}
