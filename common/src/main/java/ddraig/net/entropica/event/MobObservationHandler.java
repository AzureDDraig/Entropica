package ddraig.net.entropica.event;

import ddraig.net.entropica.data.CodexPlayerData;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class MobObservationHandler {

    private static int tickCounter = 0;

    public static void register() {
        // 1. Raycast line of sight observation tick (runs every 10 ticks)
        TickEvent.PLAYER_POST.register(player -> {
            if (player == null || player.level().isClientSide()) return;

            tickCounter++;
            if (tickCounter % 10 != 0) return;

            Vec3 eyePos = player.getEyePosition(1.0f);
            Vec3 viewVec = player.getViewVector(1.0f);
            Vec3 reachVec = eyePos.add(viewVec.x * 16.0, viewVec.y * 16.0, viewVec.z * 16.0);
            AABB searchBox = player.getBoundingBox().expandTowards(viewVec.scale(16.0)).inflate(1.0);

            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                    player, eyePos, reachVec, searchBox,
                    e -> e instanceof LivingEntity && e.isAlive() && e != player,
                    256.0D
            );

            if (entityHit != null && entityHit.getEntity() instanceof LivingEntity targetMob) {
                String mobId = BuiltInRegistries.ENTITY_TYPE.getKey(targetMob.getType()).toString();
                if (!CodexPlayerData.CLIENT_DATA.isMobObserved(mobId)) {
                    CodexPlayerData.CLIENT_DATA.observeMob(mobId);
                    player.displayClientMessage(
                            Component.literal("§dCodex Entry Updated: §f" + targetMob.getDisplayName().getString() + " §7(Partial)"),
                            true
                    );
                }
            }
        });

        // 2. Full Unlock on Mob Death
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (source.getEntity() instanceof Player player && !player.level().isClientSide()) {
                String mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                if (!CodexPlayerData.CLIENT_DATA.isMobFullyUnlocked(mobId)) {
                    CodexPlayerData.CLIENT_DATA.fullyUnlockMob(mobId);
                    player.displayClientMessage(
                            Component.literal("§dCodex Entry Updated: §f" + entity.getDisplayName().getString() + " §7(Complete)"),
                            true
                    );
                }
            }
            return EventResult.pass();
        });

        // 3. Full Unlock on Mob Taming / Interaction
        InteractionEvent.INTERACT_ENTITY.register((player, entity, hand) -> {
            if (!player.level().isClientSide() && entity instanceof TamableAnimal tamable) {
                if (tamable.isTame() && tamable.getOwner() == player) {
                    String mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                    if (!CodexPlayerData.CLIENT_DATA.isMobFullyUnlocked(mobId)) {
                        CodexPlayerData.CLIENT_DATA.fullyUnlockMob(mobId);
                        player.displayClientMessage(
                                Component.literal("§dCodex Entry Updated: §f" + entity.getDisplayName().getString() + " §7(Complete)"),
                                true
                        );
                    }
                }
            }
            return EventResult.pass();
        });
    }
}
