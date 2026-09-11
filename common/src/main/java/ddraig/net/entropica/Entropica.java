package ddraig.net.entropica;

import ddraig.net.entropica.command.ModCommands;
import ddraig.net.entropica.event.EssenceMappingEvents;
import ddraig.net.entropica.event.MobDropHandler;
import ddraig.net.entropica.event.ModEntityEvents;
import ddraig.net.entropica.network.ModNetwork;
import ddraig.net.entropica.registry.*;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class Entropica {
    public static final String MODID = "entropica";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        LOGGER.info("Entropica: Where Life Fuels Progress - Common Initialization starting.");

        // --- Register the Registries ---
        ModDataComponents.COMPONENTS.register();
        ModEffects.EFFECTS.register();
        ModPotions.POTIONS.register();
        ModBlocks.BLOCKS.register();

        ModItems.ITEMS.register();
        ModBlockEntities.BLOCK_ENTITIES.register();
        ModEntityTypes.ENTITY_TYPES.register();
        ModParticles.PARTICLES.register();
        ModRecipes.register();
        ModSounds.SOUNDS.register();
        ModMenuTypes.MENU_TYPES.register();
        ModCreativeTabs.CREATIVE_MODE_TABS.register();

        // --- Register Network Receiver and Commands ---
        ModNetwork.register();
        ModCommands.register();

        // --- Register Common Events ---
        ModEntityEvents.register();
        EssenceMappingEvents.register();
        MobDropHandler.register();
        ddraig.net.entropica.event.ParalyzedEventHandler.register();
        ddraig.net.entropica.event.CodexJoinHandler.register();
        ddraig.net.entropica.event.MobObservationHandler.register();
        ddraig.net.entropica.event.CodexCraftingLockHandler.register();

        // --- Gravity Field Tick Loop ---
        dev.architectury.event.events.common.TickEvent.SERVER_LEVEL_POST.register(level -> {
            ddraig.net.entropica.gravity.GravityFieldManager.tickLevel(level);
            ddraig.net.entropica.gravity.GravityFieldManager.cleanupExitedEntities(level);
        });

        // --- Graviton Soles Player Tick, Fall Damage Immunity & Barrier CCD ---
        dev.architectury.event.events.common.TickEvent.PLAYER_POST.register(player -> {
            ddraig.net.entropica.gravity.GravityApi.tickGravitonSoles(player);
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                net.minecraft.world.phys.Vec3 prev = new net.minecraft.world.phys.Vec3(serverPlayer.xOld, serverPlayer.yOld, serverPlayer.zOld);
                net.minecraft.world.phys.Vec3 curr = serverPlayer.position();
                if (prev.distanceToSqr(curr) > 1e-6) {
                    ddraig.net.entropica.forcefield.BarrierFieldManager.checkMovementCollisions(serverPlayer, prev, curr);
                }
            }
        });
        dev.architectury.event.events.common.EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
                if (ddraig.net.entropica.gravity.GravityApi.hasGravitonSoles(entity)
                        || entity.removeTag("entropica:bouncepad_immune")) {
                    entity.resetFallDistance();
                    return dev.architectury.event.EventResult.interruptFalse();
                }
            }
            return dev.architectury.event.EventResult.pass();
        });

        // --- Boss Arena Barrier Dissolution on Boss Defeat ---
        dev.architectury.event.events.common.EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (!entity.level().isClientSide() && entity instanceof net.minecraft.world.entity.LivingEntity living) {
                ddraig.net.entropica.forcefield.BarrierFieldManager.onLivingDeath(living);
            }
            return dev.architectury.event.EventResult.pass();
        });

        // --- Firmament Weaver Proximity Dispel on Shift + Left Click Block ---
        dev.architectury.event.events.common.InteractionEvent.LEFT_CLICK_BLOCK.register((player, hand, pos, direction) -> {
            if (player.isShiftKeyDown() && player.getItemInHand(hand).getItem() instanceof ddraig.net.entropica.item.FirmamentWeaverItem) {
                if (ddraig.net.entropica.item.FirmamentWeaverItem.dispelNearestBarrier(player)) {
                    return dev.architectury.event.EventResult.interruptTrue();
                }
            }
            return dev.architectury.event.EventResult.pass();
        });

        LOGGER.info("Entropica: Common Initialization completed.");
    }
}