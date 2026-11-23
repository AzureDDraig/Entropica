package ddraig.net.entropica.event;

import ddraig.net.entropica.block.biome8.CryptSiphonLanternBlock;
import ddraig.net.entropica.block.biome8.GraveMossBlock;
import ddraig.net.entropica.block.biome8.NecroticSoilBlock;
import ddraig.net.entropica.effect.BarrowWardEffect;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.item.biome8.BarrowSlateGreavesItem;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Biome #8 (2.8 Barrow-Mound Tundra) combat, ward, and flora hooks:
 * 1. Prevents mundane crop/sapling right-click planting on Necrotic Soil.
 * 2. Suppresses natural Undead spawns within 16 blocks of a Crypt-Siphon Lantern.
 * 3. Wither immunity & Undead/Barrow Hound damage reduction from Barrow Ward & Barrow-Slate Greaves.
 * 4. Necrotic Cold Slowness I on Undead melee strikes inside Barrow-Mound Tundra.
 * 5. Grave-Moss stealth (-50% mob follow range) & >5s stationary Hunger I drain.
 */
public final class BarrowCombatHandler {

    private static final ResourceKey<Biome> BARROW_MOUND_TUNDRA_KEY = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("entropica", "barrow_mound_tundra")
    );

    private static final ThreadLocal<Boolean> REENTRANT_BARROW_HURT = ThreadLocal.withInitial(() -> false);
    private static final Map<UUID, Integer> GRAVE_MOSS_STILL_TICKS = new ConcurrentHashMap<>();

    private BarrowCombatHandler() {}

    public static void resetGraveMossTimer(UUID playerId) {
        if (playerId != null) {
            GRAVE_MOSS_STILL_TICKS.remove(playerId);
        }
    }

    private static boolean isSuppressibleUndeadSpawn(EntitySpawnReason reason) {
        return reason == EntitySpawnReason.NATURAL
                || reason == EntitySpawnReason.CHUNK_GENERATION
                || reason == EntitySpawnReason.REINFORCEMENT
                || reason == EntitySpawnReason.PATROL;
    }

    private static boolean isUndeadOrBarrowHound(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity.getType().is(EntityTypeTags.UNDEAD)) {
            return true;
        }
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        return "barrow_hound".equals(path);
    }

    public static void register() {
        // 1. Prevent normal crop/sapling right-click planting on Necrotic Soil
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, direction) -> {
            if (player == null) {
                return InteractionResult.PASS;
            }
            ItemStack held = player.getItemInHand(hand);
            if (!(held.getItem() instanceof BlockItem blockItem)
                    || !NecroticSoilBlock.isRejectedMundanePlant(blockItem.getBlock())) {
                return InteractionResult.PASS;
            }

            Level level = player.level();
            BlockState clickedState = level.getBlockState(pos);
            BlockPos targetPlantPos = clickedState.canBeReplaced() ? pos : pos.relative(direction);
            BlockState soilBelow = level.getBlockState(targetPlantPos.below());

            if (soilBelow.getBlock() instanceof NecroticSoilBlock) {
                if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, targetPlantPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.45F, 1.35F);
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            targetPlantPos.getX() + 0.5D, targetPlantPos.getY() + 0.1D, targetPlantPos.getZ() + 0.5D,
                            6, 0.25D, 0.08D, 0.25D, 0.02D);
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        // 2. Crypt-Siphon Lantern 16-block Undead Spawn Suppression Ward
        EntityEvent.LIVING_CHECK_SPAWN.register((entity, world, x, y, z, spawnReason, spawner) -> {
            if (entity != null
                    && !(entity instanceof SpellEidolonEntity)
                    && entity.getType().is(EntityTypeTags.UNDEAD)
                    && isSuppressibleUndeadSpawn(spawnReason)) {
                ServerLevel serverLevel = null;
                if (world instanceof ServerLevel sl) {
                    serverLevel = sl;
                } else if (world instanceof ServerLevelAccessor sla) {
                    serverLevel = sla.getLevel();
                }
                if (serverLevel != null) {
                    BlockPos spawnPos = BlockPos.containing(x, y, z);
                    if (CryptSiphonLanternBlock.isWithinUndeadWardRadius(
                            serverLevel, spawnPos, CryptSiphonLanternBlock.DEFAULT_WARD_RADIUS)) {
                        return EventResult.interruptFalse();
                    }
                }
            }
            return EventResult.pass();
        });

        EntityEvent.ADD.register((entity, level) -> {
            if (!level.isClientSide()
                    && level instanceof ServerLevel serverLevel
                    && entity instanceof Mob mob
                    && !(entity instanceof SpellEidolonEntity)
                    && mob.getType().is(EntityTypeTags.UNDEAD)
                    && mob.tickCount == 0
                    && !mob.isPersistenceRequired()
                    && CryptSiphonLanternBlock.isWithinUndeadWardRadius(
                            serverLevel, mob.blockPosition(), CryptSiphonLanternBlock.DEFAULT_WARD_RADIUS)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        // 3. Wither Immunity, Undead/Barrow Hound Damage Reduction & Necrotic Cold
        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (entity.level().isClientSide()
                    || !(entity.level() instanceof ServerLevel serverLevel)
                    || amount <= 0.0F
                    || REENTRANT_BARROW_HURT.get()) {
                return EventResult.pass();
            }

            // 3a. Cancel Wither damage when protected by Barrow Ward
            if (source.is(DamageTypes.WITHER) && BarrowWardEffect.hasBarrowWard(entity)) {
                return EventResult.interruptFalse();
            }

            // 3b. Necrotic Cold: Undead melee attacks inside Barrow-Mound Tundra inflict Slowness I (60t)
            if (!source.is(DamageTypeTags.IS_PROJECTILE)) {
                Entity direct = source.getDirectEntity();
                if (direct instanceof LivingEntity directAttacker
                        && directAttacker != entity
                        && directAttacker.getType().is(EntityTypeTags.UNDEAD)
                        && serverLevel.getBiome(entity.blockPosition()).is(BARROW_MOUND_TUNDRA_KEY)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0), directAttacker);
                }
            }

            // 3c. Damage reduction from Undead & Barrow Hounds (-20% Barrow-Slate Greaves, -15% Barrow Ward)
            boolean fromUndeadOrHound = isUndeadOrBarrowHound(source.getEntity())
                    || isUndeadOrBarrowHound(source.getDirectEntity());
            float damageMult = 1.0F;
            if (fromUndeadOrHound) {
                if (BarrowSlateGreavesItem.hasBarrowSlateGreaves(entity)) {
                    damageMult *= 0.80F;
                }
                if (BarrowWardEffect.hasBarrowWard(entity)) {
                    damageMult *= 0.85F;
                }
            }

            if (damageMult < 0.999F) {
                float reducedAmount = amount * damageMult;
                try {
                    REENTRANT_BARROW_HURT.set(true);
                    entity.hurtServer(serverLevel, source, reducedAmount);
                } finally {
                    REENTRANT_BARROW_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });

        // 4. Player Tick: Grave-Moss Stealth & Stationary Hunger Drain
        TickEvent.PLAYER_POST.register(player -> {
            if (player == null || player.level().isClientSide() || !(player.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            boolean onGraveMoss = GraveMossBlock.isStandingOnGraveMoss(serverLevel, player);
            if (!onGraveMoss
                    || player.isCreative()
                    || player.isSpectator()
                    || BarrowSlateGreavesItem.hasBarrowSlateGreaves(player)) {
                GRAVE_MOSS_STILL_TICKS.remove(player.getUUID());
            } else {
                double dx = player.getX() - player.xOld;
                double dz = player.getZ() - player.zOld;
                double horizSpeedSq = Math.max(dx * dx + dz * dz, player.getKnownMovement().horizontalDistanceSqr());
                if (horizSpeedSq < 0.0015D) {
                    if (GRAVE_MOSS_STILL_TICKS.size() > 512) {
                        GRAVE_MOSS_STILL_TICKS.clear();
                    }
                    int stillTicks = GRAVE_MOSS_STILL_TICKS.merge(player.getUUID(), 1, Integer::sum);
                    if (stillTicks >= 100 && (stillTicks - 100) % 40 == 0) {
                        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0, true, true, true));
                    }
                } else {
                    GRAVE_MOSS_STILL_TICKS.remove(player.getUUID());
                }
            }

            long syncedTick = serverLevel.getGameTime() + player.getId();

            // Grave-Moss -50% mob follow range stealth check every 10 ticks
            if (onGraveMoss && syncedTick % 10L == 0L) {
                for (Mob mob : serverLevel.getEntitiesOfClass(
                        Mob.class,
                        player.getBoundingBox().inflate(32.0D),
                        m -> m.isAlive() && m.getTarget() == player && m.getLastHurtByMob() != player
                )) {
                    double effectiveRange = mob.getAttributeValue(Attributes.FOLLOW_RANGE) * 0.5D;
                    if (mob.distanceToSqr(player) > effectiveRange * effectiveRange) {
                        mob.setTarget(null);
                        mob.getNavigation().stop();
                    }
                }
            }
        });
    }
}
