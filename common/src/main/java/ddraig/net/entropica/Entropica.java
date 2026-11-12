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
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.tickServerLevelHexes(serverLevel);
                ddraig.net.entropica.spell.engine.SpellLingeringZoneManager.tickServerLevel(serverLevel);
                ddraig.net.entropica.item.SpellgemSeedItem.tickServerLevel(serverLevel);
            }
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
                // Section 6.32 Null_U Sensory Blackout Hex: eating Mushroom Stew / Suspicious Stew cleanses the abyssal blackout!
                if (serverPlayer.isUsingItem() && serverPlayer.getUseItemRemainingTicks() <= 1) {
                    net.minecraft.world.item.ItemStack using = serverPlayer.getUseItem();
                    if (using.is(net.minecraft.world.item.Items.MUSHROOM_STEW) || using.is(net.minecraft.world.item.Items.SUSPICIOUS_STEW)) {
                        net.minecraft.nbt.CompoundTag pData = ddraig.net.entropica.util.EntityHelper.getPersistentData(serverPlayer);
                        if ("NULL_U".equals(pData.getString("SpellHexType").orElse(""))) {
                            ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.purifyHexesAndNegativeEffects(serverPlayer);
                        }
                    }
                }
            }
        });
        dev.architectury.event.events.common.EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (entity instanceof net.minecraft.world.entity.LivingEntity livingTarget) {
                var wardRes = ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.interceptWardDamage(livingTarget, source, amount);
                if (wardRes.interruptsFurtherEvaluation()) {
                    return wardRes;
                }
            }
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
                ddraig.net.entropica.spell.engine.SpellCombatReactionEngine.onLivingDeathHexContagion(living);
            }
            return dev.architectury.event.EventResult.pass();
        });

        // --- Umbral Anchored Sentry Natural Dark-Spawn Suppression & Spellgem Seed Tracking ---
        dev.architectury.event.events.common.EntityEvent.ADD.register((entity, level) -> {
            if (!level.isClientSide() && entity instanceof net.minecraft.world.entity.item.ItemEntity itemEntity) {
                ddraig.net.entropica.item.SpellgemSeedItem.trackDroppedEntity(itemEntity);
            }
            if (!level.isClientSide()
                    && level instanceof net.minecraft.server.level.ServerLevel srv
                    && entity instanceof net.minecraft.world.entity.monster.Monster monster
                    && !(entity instanceof ddraig.net.entropica.entity.SpellEidolonEntity)
                    && monster.tickCount == 0
                    && !monster.isPersistenceRequired()
                    && srv.getMaxLocalRawBrightness(monster.blockPosition()) <= 7
                    && ddraig.net.entropica.entity.SpellEidolonEntity.isUmbralSentrySuppressingSpawns(srv, monster.blockPosition())) {
                return dev.architectury.event.EventResult.interruptFalse();
            }
            return dev.architectury.event.EventResult.pass();
        });

        // --- Firmament Weaver Proximity Dispel on Shift + Left Click Block ---
        dev.architectury.event.events.common.InteractionEvent.LEFT_CLICK_BLOCK.register((player, hand, pos, direction) -> {
            if (player.isShiftKeyDown() && player.getItemInHand(hand).getItem() instanceof ddraig.net.entropica.item.FirmamentWeaverItem) {
                if (ddraig.net.entropica.item.FirmamentWeaverItem.dispelNearestBarrier(player)) {
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        // --- Bottled Eschaton & Singularity Transmuted Elixir Consumption ---
        dev.architectury.event.events.common.InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            net.minecraft.world.item.ItemStack held = player.getItemInHand(hand);
            net.minecraft.world.item.component.CustomData custom = held.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (custom != null) {
                String elixirType = custom.copyTag().getString("SpellApexElixirType").orElse("");
                if (!elixirType.isEmpty()) {
                    if (!player.level().isClientSide() && player.level() instanceof net.minecraft.server.level.ServerLevel srv) {
                        ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.consumeTransmutedElixir(srv, player, held, hand, elixirType);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }
            }
            // Section 2.1.6 & 8.5: Engraved Weapons & Tools cast their socketed Spellgem on Right-Click
            if (!held.isEmpty()
                    && !(held.getItem() instanceof ddraig.net.entropica.item.CastingApparatusItem)
                    && !(held.getItem() instanceof ddraig.net.entropica.item.DynamicVisWeaponItem)
                    && held.has(ModDataComponents.CASTING_APPARATUS_STATE.get())) {
                if (!player.level().isClientSide() && player.level() instanceof net.minecraft.server.level.ServerLevel srv) {
                    ddraig.net.entropica.spell.engine.SpellExecutionEngine.castFromApparatus(srv, player, hand, held);
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        LOGGER.info("Entropica: Common Initialization completed.");
    }
}