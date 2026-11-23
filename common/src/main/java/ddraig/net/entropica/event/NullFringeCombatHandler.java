package ddraig.net.entropica.event;

import ddraig.net.entropica.item.biome9.LeviathanRiftBeltItem;
import ddraig.net.entropica.item.biome9.NullWeaveMantleItem;
import ddraig.net.entropica.util.EntityHelper;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;

/**
 * Biome #9 (2.9 Null-Fringe Bay) spatial gear combat hooks:
 * 1. Halves fall damage when wearing the Null-Weave Mantle.
 * 2. Halves projectile damage during the 3-second Leviathan Rift-Belt dash veil.
 */
public final class NullFringeCombatHandler {

    private static final ThreadLocal<Boolean> REENTRANT_NULL_HURT = ThreadLocal.withInitial(() -> false);

    private NullFringeCombatHandler() {}

    public static void register() {
        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (entity.level().isClientSide()
                    || !(entity.level() instanceof ServerLevel serverLevel)
                    || amount <= 0.0F
                    || REENTRANT_NULL_HURT.get()) {
                return EventResult.pass();
            }

            float damageMult = 1.0F;
            if (source.is(DamageTypeTags.IS_FALL) && NullWeaveMantleItem.hasNullWeaveMantle(entity)) {
                damageMult *= 0.5F;
            }
            if (source.is(DamageTypeTags.IS_PROJECTILE)
                    && EntityHelper.getPersistentData(entity)
                            .getLong(LeviathanRiftBeltItem.TAG_RIFT_DASH_UNTIL)
                            .orElse(0L) > serverLevel.getGameTime()) {
                damageMult *= 0.5F;
            }

            if (damageMult < 0.999F) {
                float reducedAmount = amount * damageMult;
                try {
                    REENTRANT_NULL_HURT.set(true);
                    entity.hurtServer(serverLevel, source, reducedAmount);
                } finally {
                    REENTRANT_NULL_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });
    }
}
