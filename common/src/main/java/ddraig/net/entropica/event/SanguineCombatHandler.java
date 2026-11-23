package ddraig.net.entropica.event;

import ddraig.net.entropica.item.biome16.SanguineIceLancetItem;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Biome #16 (2.16 Blood-Ice Shallows) combat & ice-harvesting hooks:
 * 1. Sanguine Vigor melee life-siphon: heals direct attackers for 25% of damage dealt (clamped 1.0F..2.5F).
 * 2. Sanguine Ice-Lancet clean ice extraction: cleanly harvests any block in #minecraft:ice intact without
 *    melting into water or double-dropping when enchanted.
 */
public final class SanguineCombatHandler {

    private SanguineCombatHandler() {
    }

    public static void register() {
        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (entity.level().isClientSide() || amount <= 0.0F) {
                return EventResult.pass();
            }
            if (source.getDirectEntity() instanceof LivingEntity attacker
                    && attacker != entity
                    && attacker.isAlive()) {
                Holder<MobEffect> holder = resolveSanguineVigorHolder();
                if (holder != null && attacker.hasEffect(holder)) {
                    float healAmount = Math.min(2.5F, Math.max(1.0F, amount * 0.25F));
                    attacker.heal(healAmount);
                }
            }
            return EventResult.pass();
        });

        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (level.isClientSide()
                    || !(level instanceof ServerLevel serverLevel)
                    || player == null
                    || player.isCreative()) {
                return EventResult.pass();
            }
            if (!state.is(BlockTags.ICE)) {
                return EventResult.pass();
            }

            ItemStack held = player.getMainHandItem();
            boolean holdingLancet = (ModItems.SANGUINE_ICE_LANCET.isBound() && held.is(ModItems.SANGUINE_ICE_LANCET.get()))
                    || held.getItem() instanceof SanguineIceLancetItem;
            if (!holdingLancet) {
                return EventResult.pass();
            }

            Block brokenBlock = state.getBlock();
            Block.popResource(serverLevel, pos, new ItemStack(brokenBlock));
            serverLevel.removeBlock(pos, false);
            held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            player.awardStat(Stats.BLOCK_MINED.get(brokenBlock));
            player.causeFoodExhaustion(0.005F);
            return EventResult.interruptFalse();
        });
    }

    private static Holder<MobEffect> resolveSanguineVigorHolder() {
        if (ModEffects.SANGUINE_VIGOR.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.SANGUINE_VIGOR.get());
        }
        MobEffect fallback = BuiltInRegistries.MOB_EFFECT
                .getOptional(ResourceLocation.fromNamespaceAndPath("entropica", "sanguine_vigor"))
                .orElse(null);
        return fallback != null ? BuiltInRegistries.MOB_EFFECT.wrapAsHolder(fallback) : null;
    }
}
