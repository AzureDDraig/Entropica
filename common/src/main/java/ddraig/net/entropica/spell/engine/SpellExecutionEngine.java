package ddraig.net.entropica.spell.engine;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.AutocastPedestalBlockEntity;
import ddraig.net.entropica.combat.VisInteractionManager;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.gravity.GravityField;
import ddraig.net.entropica.gravity.GravityFieldManager;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.spell.*;
import ddraig.net.entropica.spell.apparatus.ApparatusType;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.apparatus.SocketedSpellgem;
import ddraig.net.entropica.spell.assembly.*;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellCostBreakdown;
import ddraig.net.entropica.spell.fuel.SpellFuelCalculator;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.MateriaFamilyTier;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.mutation.handler.SpellMutationHandlerRegistry;
import ddraig.net.entropica.spell.research.SpellResearchManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Authoritative server-side Spell Execution Engine for Entropica.
 *
 * <p>Executes Casting Apparatus casts (enforcing Arcane Silence, 60s field-swap lockout, 45% first-cast backfire,
 * and 0-cost Sneak-Cast Eidolon safe dismissal), drains multi-Materia fuel atomically across all parts,
 * and dispatches every Flow, Delivery Form (including the Eidolon Summon Triad), and Primary / Dual-Verb
 * Secondary Core Effect while strictly honoring each part's individual {@link EssenceType} and {@code mutationEnabled} toggle.</p>
 */
public final class SpellExecutionEngine {

    private record PrimedTouchStrike(
            SpellAssembly spell,
            List<SubSpellSlot> touchSlots,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            float apparatusRange,
            long primedAtTick,
            Map<EssenceType, Long> trickleCostPerSec
    ) {
    }

    private record ActiveCorpseBloom(
            @Nullable UUID casterUuid,
            SpellAssembly spell,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            float apparatusRange,
            long expiresAtTick
    ) {
    }

    private static final Map<UUID, PrimedTouchStrike> PRIMED_TOUCH_STRIKES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<UUID, ActiveCorpseBloom> ACTIVE_CORPSE_BLOOMS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> BLINK_CANCELLED_REFUND_FLAG = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<String> BLINK_CANCELLED_REASON = new ThreadLocal<>();

    private SpellExecutionEngine() {
    }

    /**
     * Marks a struck entity with a 3.0-second (60-tick) {@link SpellTriggerType#ON_DEATH} Corpse Bloom seed (Guide Section 3.5).
     * If the entity dies while marked, {@link #tryTriggerCorpseBloomOnDeath} detonates all Multi-Cast sub-spells from its corpse!
     */
    public static void markEntityForCorpseBloom(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            float apparatusRange
    ) {
        ACTIVE_CORPSE_BLOOMS.put(
                target.getUUID(),
                new ActiveCorpseBloom(
                        caster != null ? caster.getUUID() : null,
                        spell,
                        fallbackMateria,
                        apparatusPotency,
                        apparatusRange,
                        level.getGameTime() + 60L
                )
        );
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY(0.65D), target.getZ(),
                12, 0.3D, 0.4D, 0.3D, 0.04D);
        level.playSound(null, target.blockPosition(), SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 0.85F, 1.2F);
    }

    /**
     * Detonates an active {@link SpellTriggerType#ON_DEATH} Corpse Bloom seed when the marked entity dies within its 3.0s window!
     */
    public static void tryTriggerCorpseBloomOnDeath(ServerLevel level, LivingEntity deceased) {
        ActiveCorpseBloom bloom = ACTIVE_CORPSE_BLOOMS.remove(deceased.getUUID());
        if (bloom == null || level.getGameTime() > bloom.expiresAtTick()) {
            return;
        }
        LivingEntity caster = bloom.casterUuid() != null && level.getEntity(bloom.casterUuid()) instanceof LivingEntity l ? l : null;
        Vec3 corpseCenter = deceased.position().add(0.0D, 0.4D, 0.0D);

        EssenceType pMat = bloom.spell().subSpells().isEmpty()
                ? EssenceType.UNDEAD
                : bloom.spell().subSpells().getFirst().primaryEffect().resolveEffectiveMateria(bloom.spell().materiaSpecific(), bloom.fallbackMateria());
        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                level, corpseCenter, 2.8D, pMat, EssenceType.UNDEAD, bloom.spell().getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.SOUL, corpseCenter.x, corpseCenter.y + 0.3D, corpseCenter.z,
                22, 0.55D, 0.45D, 0.55D, 0.06D);
        level.playSound(null, deceased.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.0F, 1.15F);

        executeContactMultiCast(
                level,
                caster,
                null,
                corpseCenter,
                null,
                deceased.blockPosition(),
                Direction.UP,
                new Vec3(0.0D, 1.0D, 0.0D),
                bloom.spell(),
                bloom.fallbackMateria(),
                bloom.apparatusPotency(),
                bloom.apparatusRange()
        );
        if (bloom.spell().flowPart().flow() == SpellFlowModifier.CHAIN_LEAP && caster != null) {
            chainLeapMultiCastFrom(
                    level,
                    caster,
                    deceased,
                    bloom.spell(),
                    bloom.fallbackMateria(),
                    bloom.apparatusPotency(),
                    bloom.apparatusRange()
            );
        }
    }

    /**
     * Consumes the remaining 75% dynamic trigger Materia fee when a triggered/relay payload actually contacts or activates
     * (Guide Section 3.6 Dynamic Trigger Billing).
     */
    public static boolean consumeDynamicTriggerFeeForPayload(
            ServerLevel level,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            @Nullable EssenceType fallbackMateria
    ) {
        if (!spell.flowPart().usesDynamicTriggerBilling(spell.materiaSpecific(), fallbackMateria) || !(caster instanceof Player player)) {
            return true;
        }
        CastingApparatusState held = resolveHeldApparatusState(player);
        ApparatusType appType = held != null ? held.apparatusType() : ApparatusType.ARCANUM_FOCUS;
        float effBonus = held != null ? held.materialEfficiencyBonus() : 0.0F;
        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(spell, appType, effBonus, fallbackMateria);
        if (cost.dynamicTriggerMfumByType().isEmpty()) {
            return true;
        }

        int payloadDivisor = Math.max(1, spell.flowPart().flow().getCarrierPayloadCount());
        boolean auraDiscount = level.getGameTime() <= EntityHelper.getPersistentData(player).getLong(SpellLingeringZoneManager.TAG_AURA_SHROUD_UNTIL).orElse(0L);
        Map<EssenceType, Long> perPayloadTriggerCost = new EnumMap<>(EssenceType.class);
        for (Map.Entry<EssenceType, Long> entry : cost.dynamicTriggerMfumByType().entrySet()) {
            long share = Math.max(1L, Math.round((double) entry.getValue() / payloadDivisor));
            if (auraDiscount) {
                share = Math.max(1L, Math.round(share * 0.80D));
            }
            perPayloadTriggerCost.put(entry.getKey(), share);
        }

        SpellFuelService.FuelCheckResult check = SpellFuelService.consumePlayerSpellFuel(
                player, spell.getEffectiveTier(), perPayloadTriggerCost, false
        );
        if (!check.success()) {
            player.displayClientMessage(Component.literal("§cInsufficient Materia Fuel to trigger spell payload!"), true);
            return false;
        }
        return true;
    }

    /**
     * Guide Section 3.6 Delayed Miss Fuel Refund: refunds the 25% upfront activation deposit 5.0 seconds (100 ticks)
     * after a dynamic-billing carrier expires in thin air without finding a target!
     */
    public static void scheduleMissFuelRefund(
            ServerLevel level,
            Player player,
            SpellAssembly spell,
            @Nullable EssenceType fallbackMateria
    ) {
        if (player.isCreative()) {
            return;
        }
        CastingApparatusState held = resolveHeldApparatusState(player);
        ApparatusType appType = held != null ? held.apparatusType() : ApparatusType.ARCANUM_FOCUS;
        float effBonus = held != null ? held.materialEfficiencyBonus() : 0.0F;
        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(spell, appType, effBonus, fallbackMateria);
        if (cost.upfrontMfumByType().isEmpty()) {
            return;
        }
        int payloadDivisor = Math.max(1, spell.flowPart().flow().getCarrierPayloadCount());
        Map<EssenceType, Long> refundByType = new EnumMap<>(EssenceType.class);
        for (Map.Entry<EssenceType, Long> entry : cost.upfrontMfumByType().entrySet()) {
            refundByType.put(entry.getKey(), Math.max(1L, Math.round((double) entry.getValue() / payloadDivisor)));
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                player,
                player.position(),
                0.5D,
                100,
                level.getGameTime(),
                null,
                null,
                EssenceType.REGULAR,
                1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed < zone.maxDurationTicks) {
                        return true;
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (owner instanceof Player ownerPlayer && ownerPlayer.isAlive()) {
                        long totalRestored = 0L;
                        for (Map.Entry<EssenceType, Long> entry : refundByType.entrySet()) {
                            totalRestored += SpellFuelService.restorePlayerMateriaFuel(ownerPlayer, entry.getKey(), entry.getValue());
                        }
                        if (totalRestored > 0L) {
                            srv.playSound(null, ownerPlayer.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.35F, 1.6F);
                            ownerPlayer.displayClientMessage(
                                    Component.literal("§b✦ Unspent Flow Reclaimed §7— Upfront Materia fuel refunded after miss."),
                                    true
                            );
                        }
                    }
                    return false;
                }
        ));
    }

    public static boolean hasAnyOpenAirVerb(SpellAssembly spell) {
        for (SubSpellSlot slot : spell.subSpells()) {
            if (hasOpenAirBlockVerb(slot) && slot.primaryEffect().effect() != SpellCoreEffect.FLIGHT) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} when a carrier payload reaching its configured Outer Rim Travel Distance stop
     * ({@code 8m .. 128m}) in open air should detonate its slotted form(s) in mid-air rather than fizzling.
     */
    public static boolean shouldDetonateCarrierAtMaxDistance(SpellAssembly spell) {
        if (hasAnyOpenAirVerb(spell)) {
            return true;
        }
        for (SubSpellSlot slot : spell.subSpells()) {
            SpellDeliveryForm form = slot.formPart().form();
            if (form == SpellDeliveryForm.RADIAL_BURST
                    || form == SpellDeliveryForm.AURA
                    || form == SpellDeliveryForm.CONE
                    || form == SpellDeliveryForm.SKYFALL
                    || form == SpellDeliveryForm.GLYPH
                    || form.isSummonForm()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static CastingApparatusState resolveHeldApparatusState(Player player) {
        CastingApparatusState main = player.getMainHandItem().get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        if (main != null) return main;
        return player.getOffhandItem().get(ModDataComponents.CASTING_APPARATUS_STATE.get());
    }

    /**
     * Primes the caster's weapon/hand with one or more {@link SpellDeliveryForm#TOUCH_STRIKE} sub-spells
     * (Spellcrafting Guide Section 4.4: 30s base prime window, post-30s Materia fuel drinking, discharged on next melee hit!).
     */
    public static void primeCasterTouchStrike(
            ServerLevel level,
            Player player,
            SpellAssembly spell,
            List<SubSpellSlot> touchSlots,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            float apparatusRange,
            @Nullable SpellCostBreakdown costBreakdown
    ) {
        if (touchSlots.isEmpty()) {
            return;
        }
        Map<EssenceType, Long> tricklePerSec = new EnumMap<>(EssenceType.class);
        if (costBreakdown != null) {
            for (Map.Entry<EssenceType, Long> e : costBreakdown.totalMfumByType().entrySet()) {
                tricklePerSec.put(e.getKey(), Math.max(1L, Math.round(e.getValue() * 0.10D)));
            }
        }

        float primedPotency = apparatusPotency == 1.0F ? apparatusPotency * 1.15F : apparatusPotency;
        PRIMED_TOUCH_STRIKES.put(
                player.getUUID(),
                new PrimedTouchStrike(
                        spell,
                        List.copyOf(touchSlots),
                        fallbackMateria,
                        primedPotency,
                        apparatusRange,
                        level.getGameTime(),
                        tricklePerSec
                )
        );

        SubSpellSlot first = touchSlots.getFirst();
        EssenceType formMat = first.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        EssenceType effMat = first.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        EssenceType vPrimary = formMat != EssenceType.REGULAR ? formMat : effMat;
        EssenceType vSecondary = first.secondaryEffect()
                .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                .orElse(effMat != vPrimary ? effMat : null);

        Vec3 handPos = computeCasterMuzzleOrigin(player, InteractionHand.MAIN_HAND);
        if (!isSilentStealthSpell(spell, fallbackMateria)) {
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnCastMuzzleBurst(
                    level, handPos, player.getLookAngle(), vPrimary, vSecondary, spell.getEffectiveTier()
            );
            level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.80F, 1.35F);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.70F, 1.45F);
        } else {
            level.sendParticles(ParticleTypes.SMOKE, handPos.x, handPos.y, handPos.z, 6, 0.14D, 0.14D, 0.14D, 0.01D);
        }

        String countLabel = touchSlots.size() > 1 ? " (" + SpellFlowModifier.getCastCountTitle(touchSlots.size()) + ")" : "";
        player.displayClientMessage(
                Component.literal("§b✦ Touch / Strike Primed (30s) §7— Next hit unleashes §f"
                        + spell.customName() + countLabel + "§7!"),
                true
        );
    }

    /**
     * Ticks active primed Touch / Melee Strike weapon infusions once per server tick:
     * renders subtle Materia sparks on the primed hand and drinks 10% cast fuel/sec if held past 30s (Guide Sec 4.4).
     */
    public static void tickPrimedTouchStrikes(ServerLevel level) {
        if (PRIMED_TOUCH_STRIKES.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        Iterator<Map.Entry<UUID, PrimedTouchStrike>> it = PRIMED_TOUCH_STRIKES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PrimedTouchStrike> entry = it.next();
            Player player = level.getPlayerByUUID(entry.getKey());
            if (player == null) {
                continue;
            }
            if (!player.isAlive()) {
                it.remove();
                continue;
            }
            PrimedTouchStrike primed = entry.getValue();
            long elapsed = now - primed.primedAtTick();
            if (elapsed > 3600L) {
                it.remove();
                continue;
            }

            // Subtle primed hand/weapon shimmer every 6 ticks
            if (elapsed > 0L && elapsed % 6L == 0L && !primed.touchSlots().isEmpty()) {
                SubSpellSlot first = primed.touchSlots().getFirst();
                EssenceType formMat = first.formPart().resolveEffectiveMateria(primed.spell().materiaSpecific(), primed.fallbackMateria());
                EssenceType effMat = first.primaryEffect().resolveEffectiveMateria(primed.spell().materiaSpecific(), primed.fallbackMateria());
                EssenceType vPrimary = formMat != EssenceType.REGULAR ? formMat : effMat;
                Vec3 handPos = computeCasterMuzzleOrigin(player, InteractionHand.MAIN_HAND);
                ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                        level, handPos, vPrimary, null, 0.28D, 1
                );
            }

            // Guide Section 4.4 Fuel Drinking: after 30s (600 ticks) without striking, slowly drinks Materia fuel every second!
            if (elapsed > 600L && (elapsed - 600L) % 20L == 0L && !primed.trickleCostPerSec().isEmpty()) {
                SpellFuelService.FuelCheckResult check = SpellFuelService.consumePlayerSpellFuel(
                        player,
                        primed.spell().getEffectiveTier(),
                        primed.trickleCostPerSec(),
                        false
                );
                if (!check.success()) {
                    it.remove();
                    player.displayClientMessage(
                            Component.literal("§7✦ Primed Touch spell dissipated (Materia fuel exhausted)."),
                            true
                    );
                    level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.45F, 1.5F);
                } else if (elapsed == 620L) {
                    player.displayClientMessage(
                            Component.literal("§e✦ Touch Prime Over-Held (>30s) §7— Weapon is drinking trickle Materia until swung!"),
                            true
                    );
                }
            }
        }
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Earth {@code TOUCH_STRIKE}
     * (Section 6.5 <i>Seismic Impact</i>), so the physical melee swing itself converts into pure Earth Materia damage.
     */
    public static boolean hasPrimedEarthSeismicStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.EARTH) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Frozen {@code TOUCH_STRIKE}
     * (Section 6.6 <i>Flash-Freeze Stasis</i>), so the physical melee swing converts into pure Frozen Materia damage.
     */
    public static boolean hasPrimedFrozenFlashFreezeStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.FROZEN) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker's primed Frozen {@code TOUCH_STRIKE} also carries mutated Frozen
     * {@code SLOW_HEAVY} Flow (Section 6.6 <i>Permafrost Creep</i>, +0.4 Materia Advantage vs Water targets).
     */
    public static boolean hasPrimedFrozenPermafrostCreep(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        return spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY
                && spell.flowPart().mutationEnabled()
                && spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), primed.fallbackMateria()) == EssenceType.FROZEN;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Nether {@code TOUCH_STRIKE}
     * (Section 6.7 <i>Cauterizing Cleave &amp; Auto-Smelt</i>), so the physical melee swing converts into pure Nether Materia damage.
     */
    public static boolean hasPrimedNetherCauterizingStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.NETHER) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Radiant {@code TOUCH_STRIKE}
     * (Section 6.8 <i>Holy Smite &amp; Daylight Ignition</i>), so the physical melee swing converts into pure Radiant Materia damage.
     */
    public static boolean hasPrimedRadiantHolySmiteStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.RADIANT) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Umbral {@code TOUCH_STRIKE}
     * (Section 6.9 <i>Umbral Shroud Cloaking</i>), so the physical melee swing converts into pure Umbral Materia damage.
     */
    public static boolean hasPrimedUmbralCloakingStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.UMBRAL) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Undead {@code TOUCH_STRIKE}
     * (Section 6.10 <i>Vampiric Touch</i>), so the physical melee swing converts into pure Undead Materia damage
     * and siphons 30% of the damage dealt directly into the caster's health pool.
     */
    public static boolean hasPrimedUndeadVampiricStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.UNDEAD) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Void {@code TOUCH_STRIKE}
     * (Section 6.11 <i>Spatial Destabilization Strike</i>), so the physical melee swing converts into
     * armor-piercing Void Materia damage (+1 vs Air, -1 vs Earth).
     */
    public static boolean hasPrimedVoidDestabilizationStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.VOID) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Arid {@code TOUCH_STRIKE}
     * (Section 6.12 <i>Desiccating Mummification Strike</i>), so the physical melee swing converts into
     * pure Arid Materia damage and desiccates slain raw drops into dried cured meat and sand.
     */
    public static boolean hasPrimedAridMummificationStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.ARID) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Lightning {@code TOUCH_STRIKE}
     * (Section 6.13 <i>Overcharge Discharge Strike</i>), so the physical melee swing converts into
     * pure Lightning Materia damage (+1 vs Earth &amp; Water, -1 vs Void, +100% vs wet).
     */
    public static boolean hasPrimedLightningOverchargeStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.LIGHTNING) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Magma {@code TOUCH_STRIKE}
     * (Section 6.14 <i>Volcanic Slag Cleave</i>), so the physical melee swing converts into pure Magma Materia
     * damage (+1 vs Frozen &amp; Nature, -1 vs Air &amp; Water) and auto-smelts drops on lethal blows.
     */
    public static boolean hasPrimedMagmaVolcanicCleaveStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.MAGMA) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Storm {@code TOUCH_STRIKE}
     * (Section 6.15 <i>Squall Cleave</i>), so the physical melee swing converts into pure Storm Materia
     * damage (+1 vs Earth &amp; Glacial, -1 vs Nature).
     */
    public static boolean hasPrimedStormSquallCleaveStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.STORM) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Glacial {@code TOUCH_STRIKE}
     * (Section 6.16 <i>Brittle Frost Strike</i>), so the physical melee swing converts into pure Glacial Materia damage.
     */
    public static boolean hasPrimedGlacialBrittleFrostStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.GLACIAL) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker's primed Glacial {@code TOUCH_STRIKE} also carries mutated Glacial
     * {@code SLOW_HEAVY} Flow (Section 6.16 <i>Permafrost Creep</i>, +0.4 Materia Advantage score).
     */
    public static boolean hasPrimedGlacialPermafrostCreep(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        return spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY
                && spell.flowPart().mutationEnabled()
                && spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), primed.fallbackMateria()) == EssenceType.GLACIAL;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Overgrowth {@code TOUCH_STRIKE}
     * (Section 6.17 <i>Botanical Ground Slam</i>), so the physical melee swing converts into pure Overgrowth Materia damage.
     */
    public static boolean hasPrimedOvergrowthBotanicalSlamStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.OVERGROWTH) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Dust {@code TOUCH_STRIKE}
     * (Section 6.18 <i>Choking Smother Strike</i>), so the physical melee swing converts into pure Dust Materia damage.
     */
    public static boolean hasPrimedDustSmotherStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.DUST) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the attacker currently holds a primed mutated Spore {@code TOUCH_STRIKE}
     * (Section 6.19 <i>Mycelial Infestation Strike</i>), so the physical melee swing converts into pure Spore Materia damage
     * and stamps infectious death-pop metadata even on one-shot kills.
     */
    public static boolean hasPrimedSporeInfestationStrike(LivingEntity attacker) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) return false;
        SpellAssembly spell = primed.spell();
        EssenceType fallback = primed.fallbackMateria();
        for (SubSpellSlot s : primed.touchSlots()) {
            if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && s.formPart().mutationEnabled()
                    && s.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallback) == EssenceType.SPORE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Discharges an active primed Touch / Melee Strike spell when the caster lands their next physical melee hit
     * (Spellcrafting System Guide Section 4.4: 100% primary target + 50% cleave splash + weapon Materia Advantage stacking!).
     */
    public static void tryDischargePrimedTouchStrike(
            ServerLevel level,
            LivingEntity attacker,
            LivingEntity primaryTarget
    ) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(attacker.getUUID());
        if (primed == null) {
            return;
        }
        if (primaryTarget == attacker || !primaryTarget.isAlive()) {
            return;
        }

        SpellAssembly spell = primed.spell();
        boolean anyBeneficial = false;
        for (SubSpellSlot s : primed.touchSlots()) {
            if (isCasterBeneficialVerb(s.primaryEffect().effect())) {
                anyBeneficial = true;
                break;
            }
        }
        if (!anyBeneficial && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                primaryTarget, attacker, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }
        PRIMED_TOUCH_STRIKES.remove(attacker.getUUID());

        EssenceType fallbackMateria = primed.fallbackMateria();
        SpellFlowModifier flow = spell.flowPart().flow();
        float flowPotencyMult = switch (flow) {
            case DOUBLE_BIFURCATE -> 1.50F;
            case TRIPLE_TRIFURCATE -> 2.00F;
            case OCTUPLE_DOME -> 2.25F;
            default -> 1.0F;
        };
        float potency = primed.apparatusPotency() * flowPotencyMult;
        int tier = spell.getEffectiveTier();
        float flawRangeMult = spell.flawProfile().getRangeAndRadiusMultiplier();

        Vec3 delta = primaryTarget.getEyePosition().subtract(attacker.getEyePosition());
        Vec3 direction = delta.lengthSqr() > 1.0E-4D ? delta.normalize() : attacker.getLookAngle().normalize();

        EssenceType rayFlowMat = spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        boolean miasmaOrOblivionStrikeChain = spell.flowPart().mutationEnabled()
                && (rayFlowMat == EssenceType.MIASMA || rayFlowMat == EssenceType.OBLIVION);

        for (SubSpellSlot slot : primed.touchSlots()) {
            EssenceType formMat = slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            SpellMutationProfile formMut = slot.formPart().resolveMutation(spell.materiaSpecific(), fallbackMateria);
            double cleaveRad = Math.max(2.5D, 2.5D * (formMut.mutated() ? formMut.radiusMultiplier() : 1.0F) * primed.apparatusRange() * flawRangeMult);

            EssenceType effMat = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            EssenceType vPrimary = formMat != EssenceType.REGULAR ? formMat : effMat;
            EssenceType vSecondary = slot.secondaryEffect()
                    .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                    .orElse(effMat != vPrimary ? effMat : null);

            if (!isSilentStealthSpell(spell, fallbackMateria)) {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                        level, attacker.getEyePosition().subtract(0.0D, 0.28D, 0.0D), direction, 3.6D, vPrimary, vSecondary, tier
                );
                if (flow == SpellFlowModifier.DOUBLE_BIFURCATE || flow == SpellFlowModifier.TRIPLE_TRIFURCATE || flow == SpellFlowModifier.OCTUPLE_DOME) {
                    Vec3 crossDir = Vec3.directionFromRotation(attacker.getXRot(), attacker.getYRot() + 22.0F);
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                            level, attacker.getEyePosition().subtract(0.0D, 0.22D, 0.0D), crossDir, 3.6D, vSecondary != null ? vSecondary : vPrimary, vPrimary, tier
                    );
                }
                ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                        level, primaryTarget.position().add(0.0D, primaryTarget.getBbHeight() * 0.55D, 0.0D), vPrimary, vSecondary, 1.30D, tier
                );
            }

            boolean radiantPurifyDrops = slot.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && slot.formPart().mutationEnabled()
                    && formMat == EssenceType.RADIANT;
            boolean netherAutoSmeltDrops = slot.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && slot.formPart().mutationEnabled()
                    && (formMat == EssenceType.NETHER || formMat == EssenceType.MAGMA);
            boolean aridMummifyDrops = slot.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                    && slot.formPart().mutationEnabled()
                    && formMat == EssenceType.ARID;
            long radiantPurifyUntil = level.getGameTime() + 40L;
            long netherAutoSmeltUntil = level.getGameTime() + 130L;
            long aridMummifyUntil = level.getGameTime() + 60L;
            if (radiantPurifyDrops) {
                EntityHelper.getPersistentData(primaryTarget).putLong(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL, radiantPurifyUntil);
            }
            if (netherAutoSmeltDrops) {
                EntityHelper.getPersistentData(primaryTarget).putLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL, netherAutoSmeltUntil);
            }
            if (aridMummifyDrops) {
                EntityHelper.getPersistentData(primaryTarget).putLong(SpellLingeringZoneManager.TAG_ARID_MUMMIFY_DROPS_UNTIL, aridMummifyUntil);
            }

            deliverSubSpellOnEntity(level, attacker, primaryTarget, direction, spell, slot, fallbackMateria, potency);
            if (flow == SpellFlowModifier.CHAIN_LEAP || miasmaOrOblivionStrikeChain) {
                chainLeapFrom(level, attacker, primaryTarget, spell, slot, fallbackMateria, potency);
            }

            List<LivingEntity> allTouchStruck = new ArrayList<>();
            allTouchStruck.add(primaryTarget);

            // Section 4.4 Cleave Splash: cleaves adjacent enemies within sweeping arc radius at 50% splash potency!
            for (LivingEntity cleaveTarget : level.getEntitiesOfClass(
                    LivingEntity.class, primaryTarget.getBoundingBox().inflate(cleaveRad), LivingEntity::isAlive)) {
                if (cleaveTarget == attacker || cleaveTarget == primaryTarget) continue;
                if (!isCasterBeneficialVerb(slot.primaryEffect().effect())
                        && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                        cleaveTarget, attacker, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                if (radiantPurifyDrops) {
                    EntityHelper.getPersistentData(cleaveTarget).putLong(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL, radiantPurifyUntil);
                }
                if (netherAutoSmeltDrops) {
                    EntityHelper.getPersistentData(cleaveTarget).putLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL, level.getGameTime() + 70L);
                }
                if (aridMummifyDrops) {
                    EntityHelper.getPersistentData(cleaveTarget).putLong(SpellLingeringZoneManager.TAG_ARID_MUMMIFY_DROPS_UNTIL, aridMummifyUntil);
                }
                allTouchStruck.add(cleaveTarget);
                if (slot.primaryEffect().effect() == SpellCoreEffect.BLINK) {
                    slot.secondaryEffect().ifPresent(sec -> {
                        if (sec.effect() != SpellCoreEffect.BLINK) {
                            executeSingleEffectOnEntity(level, attacker, cleaveTarget, direction, spell, slot, sec,
                                    SpellPartSlotType.SECONDARY_EFFECT, fallbackMateria, potency * 0.50F);
                        }
                    });
                    continue;
                }
                deliverSubSpellOnEntity(level, attacker, cleaveTarget, direction, spell, slot, fallbackMateria, potency * 0.50F);
            }

            MateriaPartMutationHandler formHandler = SpellMutationHandlerRegistry.resolveActive(formMat, slot.formPart().mutationEnabled());
            if (formHandler != null) {
                formHandler.onTouchStrikeFormCast(level, attacker, direction, allTouchStruck, spell, slot, fallbackMateria, potency);
            }
        }
        if (!isSilentStealthSpell(spell, fallbackMateria)) {
            level.playSound(null, primaryTarget.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.95F, 1.15F);
        }
    }

    /**
     * Discharges a primed Utility / Block-targeting Touch Strike spell when the player punches a block!
     * Combat Harm/Affliction primes are preserved so brushing grass or stone never wastes a Combat melee infusion.
     */
    public static boolean tryDischargePrimedTouchStrikeOnBlock(
            ServerLevel level,
            Player player,
            BlockPos pos,
            Direction face
    ) {
        PrimedTouchStrike primed = PRIMED_TOUCH_STRIKES.get(player.getUUID());
        if (primed == null) {
            return false;
        }
        SpellAssembly spell = primed.spell();
        boolean blockOriented = spell.prefix() != SpellPrefix.COMBAT;
        if (!blockOriented) {
            for (SubSpellSlot s : primed.touchSlots()) {
                SpellCoreEffect p = s.primaryEffect().effect();
                if (p == SpellCoreEffect.BREAK || p == SpellCoreEffect.TRANSMUTE || p == SpellCoreEffect.CONSTRUCT
                        || p == SpellCoreEffect.CONJURE || p == SpellCoreEffect.GRAVITY || p == SpellCoreEffect.DIAGNOSTIC) {
                    blockOriented = true;
                    break;
                }
            }
        }
        if (!blockOriented) {
            return false;
        }

        PRIMED_TOUCH_STRIKES.remove(player.getUUID());
        EssenceType fallbackMateria = primed.fallbackMateria();
        int tier = spell.getEffectiveTier();
        Vec3 hitCenter = Vec3.atCenterOf(pos);
        Vec3 dir = player.getLookAngle().normalize();

        for (SubSpellSlot slot : primed.touchSlots()) {
            EssenceType formMat = slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            EssenceType effMat = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            EssenceType vPrimary = formMat != EssenceType.REGULAR ? formMat : effMat;
            EssenceType vSecondary = slot.secondaryEffect()
                    .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                    .orElse(effMat != vPrimary ? effMat : null);

            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                    level, player.getEyePosition().subtract(0.0D, 0.28D, 0.0D), dir, 3.2D, vPrimary, vSecondary, tier
            );
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, hitCenter, vPrimary, vSecondary, 1.05D, tier);
            deliverSubSpellOnBlock(level, player, pos, face, spell, slot, fallbackMateria, primed.apparatusPotency());
        }
        return true;
    }

    private static Vec3 computeCasterMuzzleOrigin(LivingEntity caster, InteractionHand hand) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle().normalize();
        Vec3 worldUp = Math.abs(look.y) > 0.94D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = look.cross(worldUp).normalize();
        double handSide = (hand == InteractionHand.OFF_HAND) ? -0.26D : 0.26D;
        return eye.add(right.scale(handSide)).subtract(0.0D, 0.16D, 0.0D).add(look.scale(0.42D));
    }

    /**
     * Begins a continuous Right-Click Channeled Beam from the caster (Guide Section 4.2),
     * performing activation checks and immediately firing & billing tick 0.
     */
    public static boolean tryBeginChanneledBeam(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack apparatusStack
    ) {
        CastingApparatusState state = apparatusStack.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        if (state == null) {
            return false;
        }
        if (hand == InteractionHand.OFF_HAND && !state.apparatusType().allowsOffhandCast()) {
            player.displayClientMessage(
                    Component.literal("§c[" + state.apparatusType().getDisplayName() + "] Requires Main-Hand casting!"),
                    true
            );
            return false;
        }
        if (player.getCooldowns().isOnCooldown(apparatusStack)) {
            return false;
        }

        long now = level.getGameTime();
        long silencedUntil = EntityHelper.getPersistentData(player).getLong("SpellSilencedUntil").orElse(0L);
        if (now < silencedUntil) {
            long remSec = (silencedUntil - now + 19L) / 20L;
            player.displayClientMessage(
                    Component.literal("§5[Arcane Silence] §7Vocal & somatic channels silenced (§d" + remSec + "s§7)!"),
                    true
            );
            level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.55F);
            return false;
        }

        SocketedSpellgem activeSocket = state.getActiveSocket();
        if (activeSocket.isEmpty() || activeSocket.spell().isEmpty()) {
            player.displayClientMessage(
                    Component.literal("§7[" + state.apparatusType().getDisplayName() + "] No Spellgem socketed in Slot ["
                            + (state.activeSlotIndex() + 1) + "/" + state.apparatusType().getGemCapacity() + "]."),
                    true
            );
            return false;
        }
        if (activeSocket.isLockedOut(now)) {
            long remSec = (activeSocket.getRemainingLockoutTicks(now) + 19L) / 20L;
            player.displayClientMessage(
                    Component.literal("§c[Field-Swap Lockout] §7Socket stabilizing (§e" + remSec
                            + "s§7). Swap at a Scribing Table or Runic Anvil for 0s lockout!"),
                    true
            );
            level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.6F);
            return false;
        }

        SpellAssembly spell = activeSocket.spell().get();
        if (!spell.isBoundToTier()) {
            player.displayClientMessage(
                    Component.literal("§cSpell blueprint is not bound to a Potency Tier yet!"),
                    true
            );
            return false;
        }

        EssenceType fallbackMateria = spell.materiaSpecific()
                ? EssenceType.REGULAR
                : Optional.ofNullable(SpellFuelService.detectPrimaryAvailableMateria(player)).orElse(EssenceType.REGULAR);

        // First-Cast Field-Swap 45% Backfire Check
        if (activeSocket.pendingFieldBackfireRoll()) {
            CastingApparatusState cleared = state.withSocket(state.activeSlotIndex(), activeSocket.clearPendingBackfire());
            apparatusStack.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), cleared);
            if (level.random.nextFloat() < SocketedSpellgem.FIELD_SWAP_FIRST_CAST_BACKFIRE_CHANCE) {
                triggerArcaneBackfire(level, player, 3.0F, "§c[Arcane Backfire!] §7Unaligned field-swapped Spellgem discharged on first cast!");
                player.getCooldowns().addCooldown(apparatusStack, 14);
                return false;
            }
        }

        // Flawed Infusion Unstable Casting (25% fizzle roll on beam ignition)
        if (spell.flawProfile().unstableCasting()
                && level.random.nextFloat() < SpellFlawProfile.UNSTABLE_BACKFIRE_CHANCE) {
            triggerArcaneBackfire(level, player, 2.0F, "§c[Unstable Spellgem!] §7Flawed ritual infusion caused the beam to fizzle!");
            player.getCooldowns().addCooldown(apparatusStack, 12);
            return false;
        }

        if (SpellCombatReactionEngine.hasActiveMutatedHexPublic(player, EssenceType.APOTHEOSIS)
                && isDarkOrVoidSpellAssembly(spell, fallbackMateria)) {
            triggerArcaneBackfire(level, player, 4.0F + spell.getEffectiveTier(),
                    "§e[Divine Genesis Brand!] §7Attempting dark or void magic under Apotheosis judgment seared your soul!");
            player.getCooldowns().addCooldown(apparatusStack, 14);
            return false;
        }

        if (!tickChanneledBeam(level, player, hand, apparatusStack, 0)) {
            return false;
        }

        SpellResearchManager.onSpellCast(player, spell, fallbackMateria);
        if (!isSilentStealthSpell(spell, fallbackMateria)) {
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.55F, 1.55F);
        }
        return true;
    }

    private record BeamRaySpec(Vec3 origin, Vec3 direction, float potencyScale) {
    }

    private static List<BeamRaySpec> resolveChanneledBeamRays(
            ServerLevel level,
            Player caster,
            InteractionHand hand,
            SpellAssembly spell,
            float apparatusRange,
            int heldTicks
    ) {
        Vec3 baseMuzzle = computeCasterMuzzleOrigin(caster, hand);
        Vec3 look = caster.getLookAngle().normalize();
        float yaw = caster.getYRot();
        float pitch = caster.getXRot();
        SpellFlowModifier flow = spell.flowPart().flow();

        List<BeamRaySpec> rays = new ArrayList<>();
        switch (flow) {
            case DOUBLE_BIFURCATE -> {
                rays.add(new BeamRaySpec(baseMuzzle, Vec3.directionFromRotation(pitch, yaw - 7.5F), 0.85F));
                rays.add(new BeamRaySpec(baseMuzzle, Vec3.directionFromRotation(pitch, yaw + 7.5F), 0.85F));
            }
            case TRIPLE_TRIFURCATE -> {
                rays.add(new BeamRaySpec(baseMuzzle, Vec3.directionFromRotation(pitch, yaw - 13.0F), 0.80F));
                rays.add(new BeamRaySpec(baseMuzzle, look, 0.85F));
                rays.add(new BeamRaySpec(baseMuzzle, Vec3.directionFromRotation(pitch, yaw + 13.0F), 0.80F));
            }
            case OCTUPLE_DOME -> {
                for (int i = 0; i < 8; i++) {
                    rays.add(new BeamRaySpec(baseMuzzle, Vec3.directionFromRotation(pitch * 0.35F, yaw + i * 45.0F), 0.65F));
                }
            }
            case ORBIT_SATELLITE -> {
                double baseAng = (level.getGameTime() + heldTicks) * 0.16D;
                for (int i = 0; i < 4; i++) {
                    double a = baseAng + (Math.PI * 0.5D * i);
                    Vec3 orbitOrigin = caster.position().add(Math.cos(a) * 1.35D, caster.getBbHeight() * 0.72D, Math.sin(a) * 1.35D);
                    rays.add(new BeamRaySpec(orbitOrigin, look, 0.65F));
                }
            }
            default -> rays.add(new BeamRaySpec(baseMuzzle, look, 1.0F));
        }
        return rays;
    }

    /**
     * Ticks an active Right-Click Channeled Beam directly from the caster toward their live crosshair aim (Guide Section 4.2):
     * <ul>
     *     <li>Every 2 ticks: broadcasts smooth 3D continuous beam geometry attached to the caster's hand.</li>
     *     <li>Every 4 ticks (5x/sec): drains 1/5th of the spell's {@code Materia/s} cost and ticks continuous beam damage & block mining.</li>
     *     <li>Every 60 ticks (3.0s): triggers <b>The Burst Finisher</b> shockwave pulse at the beam's impact point!</li>
     * </ul>
     */
    public static boolean tickChanneledBeam(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack apparatusStack,
            int heldTicks
    ) {
        CastingApparatusState state = apparatusStack.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        if (state == null) return false;
        SocketedSpellgem activeSocket = state.getActiveSocket();
        if (activeSocket.isEmpty() || activeSocket.spell().isEmpty()) return false;

        long now = level.getGameTime();
        if (now < EntityHelper.getPersistentData(player).getLong("SpellSilencedUntil").orElse(0L)) {
            return false;
        }

        SpellAssembly spell = activeSocket.spell().get();
        if (!spell.isBoundToTier() || !spell.hasChanneledBeamForm()) {
            return false;
        }

        EssenceType fallbackMateria = spell.materiaSpecific()
                ? EssenceType.REGULAR
                : Optional.ofNullable(SpellFuelService.detectPrimaryAvailableMateria(player)).orElse(EssenceType.REGULAR);

        SpellCostBreakdown perSecCost = SpellFuelCalculator.calculateCastCost(
                spell,
                state.apparatusType(),
                state.materialEfficiencyBonus(),
                fallbackMateria
        );

        boolean actionPulse = (heldTicks % 4 == 0);
        boolean visualPulse = (heldTicks % 2 == 0);
        boolean burstFinisher = (heldTicks > 0 && heldTicks % 60 == 0);

        // 1. Drain 1/5th of per-second Materia cost every 4 ticks (5 pulses = 1.0 second)
        if (actionPulse) {
            boolean auraDiscount = now <= EntityHelper.getPersistentData(player).getLong(SpellLingeringZoneManager.TAG_AURA_SHROUD_UNTIL).orElse(0L);
            Map<EssenceType, Long> sliceCost = new EnumMap<>(EssenceType.class);
            for (Map.Entry<EssenceType, Long> e : perSecCost.totalMfumByType().entrySet()) {
                double mult = auraDiscount ? 0.16D : 0.20D;
                sliceCost.put(e.getKey(), Math.max(1L, Math.round(e.getValue() * mult)));
            }
            SpellFuelService.FuelCheckResult fuelCheck = SpellFuelService.consumePlayerSpellFuel(
                    player,
                    spell.getEffectiveTier(),
                    sliceCost,
                    false
            );
            if (!fuelCheck.success()) {
                if (heldTicks == 0) {
                    String missingNames = fuelCheck.missingTypes().stream()
                            .map(EssenceType::getDisplayName)
                            .collect(Collectors.joining(", "));
                    player.displayClientMessage(
                            Component.literal("§cInsufficient Materia Fuel! Missing: §e" + missingNames),
                            true
                    );
                } else {
                    player.displayClientMessage(
                            Component.literal("§cMateria Fuel depleted — Beam channel ended!"),
                            true
                    );
                }
                level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.5F);
                return false;
            }
        }

        if (!visualPulse && !actionPulse && !burstFinisher) {
            return true;
        }

        float apparatusPotency = state.apparatusType().getPotencyMultiplier();
        float apparatusRange = state.apparatusType().getRangeMultiplier();
        if (spell.flowPart().flow() == SpellFlowModifier.FAST_DART) {
            apparatusRange *= 1.25F;
        }

        int tier = spell.getEffectiveTier();
        float flawRangeMult = spell.flawProfile().getRangeAndRadiusMultiplier();
        boolean silent = isSilentStealthSpell(spell, fallbackMateria);
        boolean voidFlowPhase = spell.flowPart().isVoidWarpAuthorized(spell.materiaSpecific(), fallbackMateria);

        List<BeamRaySpec> rays = resolveChanneledBeamRays(level, player, hand, spell, apparatusRange, heldTicks);
        List<SubSpellSlot> touchToPrime = (heldTicks == 0) ? new ArrayList<>() : List.of();

        if (heldTicks > 0 && heldTicks % 12 == 0 && !silent) {
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.40F, 1.55F);
        }

        List<SubSpellSlot> slots = spell.subSpells();
        for (int slotIdx = 0; slotIdx < slots.size(); slotIdx++) {
            SubSpellSlot slot = slots.get(slotIdx);
            SpellDeliveryForm form = slot.formPart().form();
            if (form == SpellDeliveryForm.TOUCH_STRIKE) {
                if (heldTicks == 0) {
                    touchToPrime.add(slot);
                }
                continue;
            }
            if (form != SpellDeliveryForm.BEAM) {
                // Any companion non-beam sub-spell in a hybrid Multi-Cast pulses cleanly once per second from the caster
                if (heldTicks % 20 == 0) {
                    dispatchSingleSubSpellForm(level, player, player.getLookAngle().normalize(), spell, slotIdx, slot,
                            fallbackMateria, apparatusPotency, apparatusRange);
                }
                continue;
            }

            EssenceType formMat = slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            SpellMutationProfile formMutation = slot.formPart().resolveMutation(spell.materiaSpecific(), fallbackMateria);
            MateriaPartMutationHandler formHandler = SpellMutationHandlerRegistry.resolveActive(formMat, slot.formPart().mutationEnabled());
            float beamReachMult = (formMutation.mutated() ? formMutation.rangeMultiplier() : 1.0F) * apparatusRange * flawRangeMult;

            boolean earthReachBonus = formMutation.mutated() && MateriaFamilyTier.isEarthLineage(formMat);
            double maxReach = spell.flowPart().distanceStep().resolveBaseBeamReachBlocks(
                    tier, spell.prefix() == SpellPrefix.COMBAT, earthReachBonus
            ) * beamReachMult;

            boolean wallPhasing = ((formMutation.mutated() && formMutation.wallPhasing()) || voidFlowPhase)
                    && spell.prefix() != SpellPrefix.UTILITY;

            EssenceType effMat = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            EssenceType vPrimary = formMat != EssenceType.REGULAR ? formMat : effMat;
            EssenceType vSecondary = slot.secondaryEffect()
                    .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                    .orElse(effMat != vPrimary ? effMat : null);

            for (BeamRaySpec ray : rays) {
                Vec3 rayStart = player.getEyePosition();
                Vec3 rayEnd = rayStart.add(ray.direction().scale(maxReach));
                boolean cryoBridgeBeam = formMat == EssenceType.FROZEN && formMutation.mutated();
                ClipContext.Fluid beamFluidClip = (formMat == EssenceType.WATER || cryoBridgeBeam || !slotHasEffect(slot, SpellCoreEffect.TRANSMUTE))
                        ? ClipContext.Fluid.NONE
                        : ClipContext.Fluid.ANY;
                BlockHitResult blockHit = level.clip(new ClipContext(rayStart, rayEnd, ClipContext.Block.OUTLINE, beamFluidClip, player));
                Vec3 effectiveEnd = (!wallPhasing && blockHit.getType() == HitResult.Type.BLOCK) ? blockHit.getLocation() : rayEnd;
                // Section 6.6 Absolute-Zero Cryo-Ray: when grazing the top surface of newly formed ice bridges across water,
                // project the ray smoothly across the ice sheet out to full maxReach until hitting a genuine non-ice solid wall!
                if (cryoBridgeBeam && !wallPhasing && blockHit.getType() == HitResult.Type.BLOCK && blockHit.getDirection() == Direction.UP) {
                    BlockState grazed = level.getBlockState(blockHit.getBlockPos());
                    if (grazed.is(Blocks.PACKED_ICE) || grazed.is(Blocks.BLUE_ICE) || grazed.is(Blocks.ICE) || grazed.is(Blocks.FROSTED_ICE)) {
                        double reachD = rayStart.distanceTo(blockHit.getLocation());
                        Vec3 bridgeEnd = rayEnd;
                        Vec3 normDir = ray.direction().normalize();
                        for (double step = reachD + 0.75D; step <= maxReach; step += 0.75D) {
                            Vec3 probePt = rayStart.add(normDir.scale(step));
                            BlockPos probePos = BlockPos.containing(probePt);
                            BlockState ps = level.getBlockState(probePos);
                            if (!ps.isAir() && !ps.is(Blocks.WATER) && !ps.is(Blocks.LAVA)
                                    && !ps.is(Blocks.PACKED_ICE) && !ps.is(Blocks.BLUE_ICE)
                                    && !ps.is(Blocks.ICE) && !ps.is(Blocks.FROSTED_ICE)
                                    && !ps.getCollisionShape(level, probePos).isEmpty()) {
                                bridgeEnd = probePt;
                                break;
                            }
                        }
                        effectiveEnd = bridgeEnd;
                    }
                }

                // Section 6.7 & 4.2 Nether Tendril Tether: flexible locking hellfire leash staying anchored within a 45° angle while moving!
                LivingEntity latchedNetherTarget = null;
                if (formMat == EssenceType.NETHER && formMutation.mutated() && !spell.prefix().enforcesZeroEntityHarm()) {
                    latchedNetherTarget = ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler
                            .resolveLatchedTetherTarget(level, player, rayStart, ray.direction(), maxReach);
                    if (latchedNetherTarget != null) {
                        effectiveEnd = latchedNetherTarget.position().add(0.0D, latchedNetherTarget.getBbHeight() * 0.55D, 0.0D);
                    }
                }

                if (visualPulse && !silent) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChanneledBeamSlice(
                            level, ray.origin(), effectiveEnd, vPrimary, vSecondary, tier, 3
                    );
                }

                if (actionPulse) {
                    float tickPotency = apparatusPotency * ray.potencyScale() * 0.25F;
                    if (!silent) {
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                                level, effectiveEnd, vPrimary, vSecondary, 0.72D, Math.max(1, tier / 2)
                        );
                    }

                    int maxTargets = 1 + (formMutation.mutated() ? formMutation.bonusPierceTargets() : 0);
                    AABB search = new AABB(rayStart, effectiveEnd).inflate(0.85D);
                    List<LivingEntity> hitEntities = new ArrayList<>();
                    if (latchedNetherTarget != null && latchedNetherTarget.isAlive()) {
                        hitEntities.add(latchedNetherTarget);
                    }
                    for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, search)) {
                        if (candidate == player || !candidate.isAlive() || candidate == latchedNetherTarget) continue;
                        if (candidate.getBoundingBox().inflate(0.38D).clip(rayStart, effectiveEnd).isPresent()) {
                            hitEntities.add(candidate);
                        }
                    }
                    hitEntities.sort(Comparator.comparingDouble(player::distanceToSqr));

                    List<LivingEntity> struckTargets = new ArrayList<>();
                    int hitCount = 0;
                    for (LivingEntity target : hitEntities) {
                        struckTargets.add(target);
                        deliverSubSpellOnEntity(level, player, target, ray.direction(), spell, slot, fallbackMateria, tickPotency);
                        if (spell.flowPart().flow() == SpellFlowModifier.CHAIN_LEAP) {
                            chainLeapFrom(level, player, target, spell, slot, fallbackMateria, tickPotency);
                        }
                        if (++hitCount >= maxTargets) break;
                    }

                    if (hasItemTargetingVerb(slot)) {
                        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, search)) {
                            if (item.isAlive() && item.getBoundingBox().inflate(0.45D).clip(rayStart, effectiveEnd).isPresent()) {
                                deliverSubSpellOnEntity(level, player, item, ray.direction(), spell, slot, fallbackMateria, tickPotency);
                            }
                        }
                    }

                    if (struckTargets.isEmpty() && slotSelfTargetsCasterWhenNoEntityHit(spell, slot)) {
                        deliverSubSpellOnEntity(level, player, player, ray.direction(), spell, slot, fallbackMateria, tickPotency);
                    }

                    if (blockHit.getType() == HitResult.Type.BLOCK) {
                        deliverSubSpellOnBlock(level, player, blockHit.getBlockPos(), blockHit.getDirection(), spell, slot, fallbackMateria, tickPotency);
                    } else if (hasOpenAirBlockVerb(slot)) {
                        deliverSubSpellOnBlock(level, player, BlockPos.containing(effectiveEnd), Direction.UP, spell, slot, fallbackMateria, tickPotency);
                    }

                    if (formHandler != null) {
                        formHandler.onBeamFormCast(level, player, ray.direction(), maxReach, struckTargets, blockHit, spell, slot, fallbackMateria, tickPotency);
                    }
                }

                // Guide Section 4.2 "The Burst Finisher": automatic shockwave pulse every 3.0 seconds (60 ticks) of continuous channel!
                if (burstFinisher) {
                    double burstRad = 2.4D + tier * 0.22D;
                    if (!silent) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                                level, effectiveEnd, burstRad, vPrimary, vSecondary, tier
                        );
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                                level, effectiveEnd, vPrimary, vSecondary, burstRad * 0.8D, tier
                        );
                        level.playSound(null, BlockPos.containing(effectiveEnd), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),
                                SoundSource.PLAYERS, 0.90F, 1.35F);
                    }
                    deliverRadialArea(level, player, effectiveEnd, burstRad, ray.direction(), spell, slot,
                            fallbackMateria, apparatusPotency * ray.potencyScale() * 0.75F);
                    for (LivingEntity stag : level.getEntitiesOfClass(LivingEntity.class, new AABB(effectiveEnd, effectiveEnd).inflate(burstRad), LivingEntity::isAlive)) {
                        if (stag == player || SpellCombatReactionEngine.isBossEntity(level, stag)) continue;
                        if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(stag, player, spell.hasWardFriendlyFireSafeguard())) continue;
                        Vec3 out = stag.position().subtract(effectiveEnd);
                        Vec3 push = out.lengthSqr() > 1.0E-4D ? out.normalize() : ray.direction();
                        applySyncedMotion(stag, new Vec3(push.x * 0.65D, 0.34D, push.z * 0.65D));
                    }
                }
            }
        }

        if (!touchToPrime.isEmpty()) {
            primeCasterTouchStrike(level, player, spell, touchToPrime, fallbackMateria, apparatusPotency, apparatusRange, perSecCost);
        }
        return true;
    }

    /**
     * Primary entry point when a player activates a Casting Apparatus or Engraved Weapon/Tool.
     *
     * @return true if a cast (or backfire/dismissal) occurred; false if blocked by silence, empty socket, lockout, cooldown, or missing fuel.
     */
    public static boolean castFromApparatus(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack apparatusStack
    ) {
        return castFromApparatus(level, player, hand, apparatusStack, 1.0F);
    }

    public static boolean castFromApparatus(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack apparatusStack,
            float extraPotencyMult
    ) {
        CastingApparatusState state = apparatusStack.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        if (state == null) {
            return false;
        }

        if (hand == InteractionHand.OFF_HAND && !state.apparatusType().allowsOffhandCast()) {
            player.displayClientMessage(
                    Component.literal("§c[" + state.apparatusType().getDisplayName() + "] Requires Main-Hand casting!"),
                    true
            );
            return false;
        }

        if (player.getCooldowns().isOnCooldown(apparatusStack)) {
            return false;
        }

        long now = level.getGameTime();
        long silencedUntil = EntityHelper.getPersistentData(player).getLong("SpellSilencedUntil").orElse(0L);
        if (now < silencedUntil) {
            long remSec = (silencedUntil - now + 19L) / 20L;
            player.displayClientMessage(
                    Component.literal("§5[Arcane Silence] §7Vocal & somatic channels silenced (§d" + remSec + "s§7)!"),
                    true
            );
            level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.55F);
            return false;
        }

        SocketedSpellgem activeSocket = state.getActiveSocket();
        if (activeSocket.isEmpty() || activeSocket.spell().isEmpty()) {
            player.displayClientMessage(
                    Component.literal("§7[" + state.apparatusType().getDisplayName() + "] No Spellgem socketed in Slot ["
                            + (state.activeSlotIndex() + 1) + "/" + state.apparatusType().getGemCapacity() + "]."),
                    true
            );
            return false;
        }

        if (activeSocket.isLockedOut(now)) {
            long remSec = (activeSocket.getRemainingLockoutTicks(now) + 19L) / 20L;
            player.displayClientMessage(
                    Component.literal("§c[Field-Swap Lockout] §7Socket stabilizing (§e" + remSec
                            + "s§7). Swap at a Scribing Table or Runic Anvil for 0s lockout!"),
                    true
            );
            level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.6F);
            return false;
        }

        SpellAssembly spell = activeSocket.spell().get();
        if (!spell.isBoundToTier()) {
            player.displayClientMessage(
                    Component.literal("§cSpell blueprint is not bound to a Potency Tier yet!"),
                    true
            );
            return false;
        }

        // Section 4.9 Safe Dismissal: Sneak-casting a Summon spell safely dismisses active Eidolons at 0 Materia cost!
        Optional<SpellDeliveryForm> chosenSummonForm = spell.getChosenSummonForm();
        if (player.isShiftKeyDown() && chosenSummonForm.isPresent()) {
            int dismissed = SpellEidolonEntity.dismissCasterEidolonsByArchetype(
                    level,
                    player,
                    chosenSummonForm.get().getSummonArchetype()
            );
            if (dismissed > 0) {
                player.getCooldowns().addCooldown(apparatusStack, 8);
                return true;
            }
        }

        // Section 4.6 Safe Glyph Disarm: Sneak-casting while looking near an owned Glyph safely removes it & refunds 50% fuel!
        if (player.isShiftKeyDown() && SpellLingeringZoneManager.tryDisarmCasterGlyph(level, player)) {
            player.getCooldowns().addCooldown(apparatusStack, 6);
            return true;
        }

        // Section 4.4 Safe Sheath: Sneak-casting a Touch/Strike spell while already primed safely clears the infusion at 0 cost!
        if (player.isShiftKeyDown() && spell.isPureTouchStrikeSpell() && PRIMED_TOUCH_STRIKES.remove(player.getUUID()) != null) {
            player.displayClientMessage(Component.literal("§7✦ Sheathed primed Touch / Strike weapon infusion."), true);
            level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.PLAYERS, 0.7F, 1.2F);
            player.getCooldowns().addCooldown(apparatusStack, 6);
            return true;
        }

        // Fallback for Engraved Vanilla Tools/Weapons casting a Channeled Beam via RIGHT_CLICK_ITEM event:
        if (spell.isChanneledHoldBeam()) {
            boolean pulsed = tryBeginChanneledBeam(level, player, hand, apparatusStack);
            if (pulsed) {
                player.getCooldowns().addCooldown(apparatusStack, 4);
            }
            return pulsed;
        }

        EssenceType fallbackMateria = spell.materiaSpecific()
                ? EssenceType.REGULAR
                : Optional.ofNullable(SpellFuelService.detectPrimaryAvailableMateria(player)).orElse(EssenceType.REGULAR);

        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(
                spell,
                state.apparatusType(),
                state.materialEfficiencyBonus(),
                fallbackMateria
        );

        // Section 5.4 Spatial Fatigue: Blinking repeatedly within 3 seconds (60t) adds +50% cumulative fuel surcharge
        boolean hasBlink = containsBlinkEffect(spell);
        Map<EssenceType, Long> effectiveUpfrontCost = cost.upfrontMfumByType();
        int prevBlinkFatigueStacks = 0;
        long prevLastBlinkTick = -200L;
        int blinkFatigueStacks = 0;
        if (hasBlink) {
            CompoundTag pData = EntityHelper.getPersistentData(player);
            prevBlinkFatigueStacks = pData.getInt("SpellBlinkFatigueStacks").orElse(0);
            prevLastBlinkTick = pData.getLong("LastSpellBlinkTick").orElse(-200L);
            if (now - prevLastBlinkTick <= 60L) {
                blinkFatigueStacks = prevBlinkFatigueStacks + 1;
                double fatigueMult = 1.0D + (0.5D * blinkFatigueStacks);
                Map<EssenceType, Long> surcharged = new EnumMap<>(EssenceType.class);
                for (Map.Entry<EssenceType, Long> entry : effectiveUpfrontCost.entrySet()) {
                    surcharged.put(entry.getKey(), Math.max(1L, Math.round(entry.getValue() * fatigueMult)));
                }
                effectiveUpfrontCost = surcharged;
            }
        }

        // Section 6.24 Aura Chakra Respiration Shroud / Spire: -20% spell Materia fuel cost while active!
        if (now <= EntityHelper.getPersistentData(player).getLong(SpellLingeringZoneManager.TAG_AURA_SHROUD_UNTIL).orElse(0L)) {
            Map<EssenceType, Long> discounted = new EnumMap<>(EssenceType.class);
            for (Map.Entry<EssenceType, Long> entry : effectiveUpfrontCost.entrySet()) {
                discounted.put(entry.getKey(), Math.max(1L, Math.round(entry.getValue() * 0.80D)));
            }
            effectiveUpfrontCost = discounted;
        }

        SpellFuelService.FuelCheckResult fuelCheck = SpellFuelService.consumePlayerSpellFuel(
                player,
                spell.getEffectiveTier(),
                effectiveUpfrontCost,
                false
        );

        if (!fuelCheck.success()) {
            String missingNames = fuelCheck.missingTypes().stream()
                    .map(EssenceType::getDisplayName)
                    .collect(Collectors.joining(", "));
            player.displayClientMessage(
                    Component.literal("§cInsufficient Materia Fuel! Missing: §e" + missingNames),
                    true
            );
            level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.5F);
            return false;
        }

        if (hasBlink) {
            CompoundTag pData = EntityHelper.getPersistentData(player);
            pData.putInt("SpellBlinkFatigueStacks", blinkFatigueStacks);
            pData.putLong("LastSpellBlinkTick", now);
        }

        // Apply Apparatus-modified cast cooldown (smooth 6-tick cadence when hovering downward with Air Jetstream Propulsion!)
        int cooldown = Math.max(4, Math.round(spell.getBaseCooldownTicks() * state.apparatusType().getCooldownMultiplier()));
        if (isDownwardAirJetstreamCast(player, spell, fallbackMateria)) {
            cooldown = Math.min(cooldown, 6);
        }
        player.getCooldowns().addCooldown(apparatusStack, cooldown);

        // First-Cast Field-Swap 45% Backfire Check (clears flag immediately whether it backfires or succeeds!)
        if (activeSocket.pendingFieldBackfireRoll()) {
            CastingApparatusState cleared = state.withSocket(state.activeSlotIndex(), activeSocket.clearPendingBackfire());
            apparatusStack.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), cleared);

            if (level.random.nextFloat() < SocketedSpellgem.FIELD_SWAP_FIRST_CAST_BACKFIRE_CHANCE) {
                triggerArcaneBackfire(level, player, 3.0F, "§c[Arcane Backfire!] §7Unaligned field-swapped Spellgem discharged on first cast!");
                return true;
            }
        }

        // Flawed Infusion Unstable Casting (25% backfire/fizzle roll)
        if (spell.flawProfile().unstableCasting()
                && level.random.nextFloat() < SpellFlawProfile.UNSTABLE_BACKFIRE_CHANCE) {
            triggerArcaneBackfire(level, player, 2.0F, "§c[Unstable Spellgem!] §7Flawed ritual infusion caused the incantation to fizzle!");
            return true;
        }

        // Two-Step Codex Research Observation Hook
        SpellResearchManager.onSpellCast(player, spell, fallbackMateria);

        // Sec 6.58 Apotheosis Divine Genesis Brand Hex: casting dark/void/necrotic spells backfires with holy self-damage!
        if (SpellCombatReactionEngine.hasActiveMutatedHexPublic(player, EssenceType.APOTHEOSIS)
                && isDarkOrVoidSpellAssembly(spell, fallbackMateria)) {
            triggerArcaneBackfire(level, player, 4.0F + spell.getEffectiveTier(),
                    "§e[Divine Genesis Brand!] §7Attempting dark or void magic under Apotheosis judgment seared your soul!");
            return true;
        }

        // Dispatch Spell Assembly (with Section 5.4 100% Materia Fuel Refund if Blink cancels with no safe landing spot!)
        BLINK_CANCELLED_REFUND_FLAG.set(false);
        BLINK_CANCELLED_REASON.remove();
        try {
            executeSpellAssembly(
                    level,
                    player,
                    spell,
                    fallbackMateria,
                    state.apparatusType(),
                    cost,
                    extraPotencyMult
            );
            if (BLINK_CANCELLED_REFUND_FLAG.get()) {
                if (!player.isCreative()) {
                    for (Map.Entry<EssenceType, Long> entry : effectiveUpfrontCost.entrySet()) {
                        SpellFuelService.restorePlayerMateriaFuel(player, entry.getKey(), entry.getValue());
                    }
                }
                if (hasBlink) {
                    CompoundTag pData = EntityHelper.getPersistentData(player);
                    pData.putInt("SpellBlinkFatigueStacks", prevBlinkFatigueStacks);
                    pData.putLong("LastSpellBlinkTick", prevLastBlinkTick);
                }
                player.getCooldowns().addCooldown(apparatusStack, 4);
                String cancelMsg = BLINK_CANCELLED_REASON.get();
                player.displayClientMessage(
                        Component.literal(cancelMsg != null
                                ? cancelMsg
                                : "§e✦ Blink Cancelled §7— No safe landing spot (§a100% Materia refunded§7)"),
                        true
                );
                return false;
            }
        } finally {
            BLINK_CANCELLED_REFUND_FLAG.set(false);
            BLINK_CANCELLED_REASON.remove();
        }

        return true;
    }

    private static void triggerArcaneBackfire(ServerLevel level, Player player, float feedbackDamage, String msg) {
        player.displayClientMessage(Component.literal(msg), true);
        player.hurtServer(level, level.damageSources().magic(), feedbackDamage);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY(1.0), player.getZ(),
                16, 0.35, 0.4, 0.35, 0.05);
        level.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY(1.0), player.getZ(),
                14, 0.35, 0.4, 0.35, 0.08);
        level.playSound(null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 0.9F, 1.3F);
    }

    /**
     * Automated spell discharge from an {@link AutocastPedestalBlockEntity} (Spellcrafting Guide Sections 2.1.7, 8.4).
     * Drains fuel from the pedestal's fuel slot or 6-way adjacent Orbis Cells, executes the mounted spell
     * along {@code facing} + {@code aimPitch}, and routes harvested drops / {@link SpellCoreEffect#LOGISTICS}
     * transfers across the pedestal's linked container inventories.
     */
    public static boolean castFromPedestal(
            ServerLevel level,
            BlockPos pedestalPos,
            Direction facing,
            float aimPitch,
            AutocastPedestalBlockEntity pedestal
    ) {
        Optional<SpellAssembly> optSpell = pedestal.getActiveSpellAssembly();
        if (optSpell.isEmpty()) {
            return false;
        }
        SpellAssembly spell = optSpell.get();
        if (!spell.isBoundToTier()) {
            return false;
        }

        EssenceType detectedFuel = SpellFuelService.detectPedestalAvailableMateria(
                level,
                pedestalPos,
                pedestal.inventory,
                AutocastPedestalBlockEntity.SLOT_FUEL
        );
        EssenceType fallbackMateria = spell.materiaSpecific()
                ? EssenceType.REGULAR
                : (detectedFuel != null ? detectedFuel : EssenceType.REGULAR);

        ApparatusType mountedApparatus = pedestal.getMountedApparatusType();
        float efficiencyBonus = pedestal.getMountedMaterialEfficiencyBonus();

        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(
                spell,
                mountedApparatus,
                efficiencyBonus,
                fallbackMateria
        );

        SpellFuelService.FuelCheckResult upfrontCheck = SpellFuelService.consumePedestalSpellFuel(
                level,
                pedestalPos,
                pedestal.inventory,
                AutocastPedestalBlockEntity.SLOT_FUEL,
                spell.getEffectiveTier(),
                cost.upfrontMfumByType(),
                false
        );
        if (!upfrontCheck.success()) {
            return false;
        }

        // Dynamic Trigger Billing for Triggered / Satellite Flows (25% idle upkeep, 75% on enemy proximity for offensive Combat spells)
        if (spell.flowPart().usesDynamicTriggerBilling(spell.materiaSpecific(), fallbackMateria)
                && spell.prefix() == SpellPrefix.COMBAT
                && !isCasterBeneficialSpell(spell)) {
            List<Monster> hostiles = level.getEntitiesOfClass(
                    Monster.class,
                    new AABB(pedestalPos).inflate(10.0D),
                    LivingEntity::isAlive
            );
            if (hostiles.isEmpty()) {
                level.sendParticles(ParticleTypes.ENCHANT,
                        pedestalPos.getX() + 0.5D, pedestalPos.getY() + 1.15D, pedestalPos.getZ() + 0.5D,
                        8, 0.25D, 0.2D, 0.25D, 0.03D);
                return true;
            }
            SpellFuelService.FuelCheckResult triggerCheck = SpellFuelService.consumePedestalSpellFuel(
                    level,
                    pedestalPos,
                    pedestal.inventory,
                    AutocastPedestalBlockEntity.SLOT_FUEL,
                    spell.getEffectiveTier(),
                    cost.dynamicTriggerMfumByType(),
                    false
            );
            if (!triggerCheck.success()) {
                return false;
            }
        }

        // Flawed Infusion Unstable Casting (25% fizzle roll)
        if (spell.flawProfile().unstableCasting()
                && level.random.nextFloat() < SpellFlawProfile.UNSTABLE_BACKFIRE_CHANCE) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    pedestalPos.getX() + 0.5D, pedestalPos.getY() + 1.15D, pedestalPos.getZ() + 0.5D,
                    12, 0.25D, 0.25D, 0.25D, 0.04D);
            level.playSound(null, pedestalPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7F, 1.2F);
            return true;
        }

        float yRot = facing.toYRot();
        float xRot = Math.clamp(aimPitch, -90.0F, 90.0F);
        Vec3 look = Vec3.directionFromRotation(xRot, yRot);
        Vec3 muzzle = new Vec3(
                pedestalPos.getX() + 0.5D,
                pedestalPos.getY() + 1.08D,
                pedestalPos.getZ() + 0.5D
        ).add(look.scale(0.65D));

        ArmorStand casterProxy = new ArmorStand(EntityType.ARMOR_STAND, level);
        casterProxy.setYRot(yRot);
        casterProxy.yRotO = yRot;
        casterProxy.setYHeadRot(yRot);
        casterProxy.yHeadRotO = yRot;
        casterProxy.setXRot(xRot);
        casterProxy.xRotO = xRot;
        casterProxy.setPos(muzzle.x, muzzle.y - casterProxy.getEyeHeight(), muzzle.z);

        level.sendParticles(ParticleTypes.END_ROD,
                pedestalPos.getX() + 0.5D, pedestalPos.getY() + 1.12D, pedestalPos.getZ() + 0.5D,
                6, 0.12D, 0.12D, 0.12D, 0.02D);

        executeSpellAssembly(level, casterProxy, spell, fallbackMateria, mountedApparatus, null);

        List<BlockPos> linkedPositions = pedestal.getLinkedInventories();
        if (!linkedPositions.isEmpty()) {
            boolean hasLogistics = containsEffect(spell, SpellCoreEffect.LOGISTICS);
            routePedestalItemsToLinkedInventories(level, pedestalPos, muzzle, look, spell.getEffectiveTier(), hasLogistics, linkedPositions);
        }

        return true;
    }

    private static boolean containsEffect(SpellAssembly spell, SpellCoreEffect targetEffect) {
        for (SubSpellSlot slot : spell.subSpells()) {
            if (slot.primaryEffect().effect() == targetEffect) return true;
            if (slot.secondaryEffect().isPresent() && slot.secondaryEffect().get().effect() == targetEffect) return true;
        }
        return false;
    }

    private static void routePedestalItemsToLinkedInventories(
            ServerLevel level,
            BlockPos pedestalPos,
            Vec3 muzzle,
            Vec3 look,
            int effectiveTier,
            boolean hasLogistics,
            List<BlockPos> linkedPositions
    ) {
        List<Container> validContainers = new ArrayList<>();
        for (BlockPos pos : linkedPositions) {
            if (pos.distSqr(pedestalPos) <= AutocastPedestalBlockEntity.MAX_LINK_DISTANCE_SQ
                    && level.getBlockEntity(pos) instanceof Container c
                    && !(c instanceof AutocastPedestalBlockEntity)) {
                validContainers.add(c);
            }
        }
        if (validContainers.isEmpty()) {
            return;
        }

        // 1. Collect newly harvested / loose ItemEntities into linked containers
        AABB harvestBox = new AABB(pedestalPos).inflate(10.0D).minmax(new AABB(muzzle, muzzle.add(look.scale(12.0D))).inflate(3.0D));
        List<ItemEntity> looseItems = level.getEntitiesOfClass(
                ItemEntity.class,
                harvestBox,
                ie -> ie.isAlive() && !ie.getItem().isEmpty() && (hasLogistics || ie.getAge() <= 5)
        );
        for (ItemEntity itemEntity : looseItems) {
            ItemStack remaining = itemEntity.getItem().copy();
            for (Container dest : validContainers) {
                remaining = insertStackIntoContainer(dest, remaining);
                if (remaining.isEmpty()) break;
            }
            if (remaining.isEmpty()) {
                itemEntity.discard();
            } else if (remaining.getCount() != itemEntity.getItem().getCount()) {
                itemEntity.setItem(remaining);
            }
        }

        // 2. Logistics Container-to-Container Transfer: pull from Source (Link 0) into Destinations (Links 1..N)
        if (hasLogistics && validContainers.size() >= 2) {
            Container source = validContainers.getFirst();
            List<Container> destinations = validContainers.subList(1, validContainers.size());
            int budget = Math.max(8, effectiveTier * 8);

            for (int slot = 0; slot < source.getContainerSize() && budget > 0; slot++) {
                ItemStack stack = source.getItem(slot);
                if (stack.isEmpty()) continue;

                int moveAmount = Math.min(budget, stack.getCount());
                ItemStack toMove = stack.copyWithCount(moveAmount);
                ItemStack leftover = toMove;
                for (Container dest : destinations) {
                    leftover = insertStackIntoContainer(dest, leftover);
                    if (leftover.isEmpty()) break;
                }
                int moved = moveAmount - leftover.getCount();
                if (moved > 0) {
                    stack.shrink(moved);
                    source.setItem(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
                    source.setChanged();
                    budget -= moved;
                }
            }
        }
    }

    public static ItemStack insertStackIntoContainer(Container dest, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack rem = stack.copy();

        // Merge with existing stacks first
        for (int i = 0; i < dest.getContainerSize() && !rem.isEmpty(); i++) {
            ItemStack existing = dest.getItem(i);
            if (!existing.isEmpty()
                    && ItemStack.isSameItemSameComponents(existing, rem)
                    && dest.canPlaceItem(i, rem)) {
                int max = Math.min(dest.getMaxStackSize(existing), existing.getMaxStackSize());
                int space = max - existing.getCount();
                if (space > 0) {
                    int add = Math.min(space, rem.getCount());
                    existing.grow(add);
                    rem.shrink(add);
                    dest.setChanged();
                }
            }
        }

        // Fill empty slots next
        for (int i = 0; i < dest.getContainerSize() && !rem.isEmpty(); i++) {
            if (dest.getItem(i).isEmpty() && dest.canPlaceItem(i, rem)) {
                int max = Math.min(dest.getMaxStackSize(rem), rem.getMaxStackSize());
                int add = Math.min(max, rem.getCount());
                dest.setItem(i, rem.split(add));
                dest.setChanged();
            }
        }
        return rem;
    }

    /**
     * Dispatches all sub-spells of a {@link SpellAssembly} according to its overarching {@link FlowPartBinding}.
     */
    public static void executeSpellAssembly(
            ServerLevel level,
            LivingEntity caster,
            SpellAssembly spell,
            @Nullable EssenceType fallbackMateria,
            @Nullable ApparatusType apparatusType,
            @Nullable SpellCostBreakdown costBreakdown
    ) {
        executeSpellAssembly(level, caster, spell, fallbackMateria, apparatusType, costBreakdown, 1.0F);
    }

    public static void executeSpellAssembly(
            ServerLevel level,
            LivingEntity caster,
            SpellAssembly spell,
            @Nullable EssenceType fallbackMateria,
            @Nullable ApparatusType apparatusType,
            @Nullable SpellCostBreakdown costBreakdown,
            float extraPotencyMult
    ) {
        FlowPartBinding flowBinding = spell.flowPart();
        SpellFlowModifier flow = flowBinding.flow();
        SpellTriggerType trigger = flowBinding.trigger();
        final boolean materiaSpecific = spell.materiaSpecific();

        float apparatusPotency = (apparatusType != null ? apparatusType.getPotencyMultiplier() : 1.0F) * extraPotencyMult;
        float apparatusRange = apparatusType != null ? apparatusType.getRangeMultiplier() : 1.0F;

        List<SubSpellSlot> slots = spell.subSpells();
        if (slots.isEmpty()) {
            return;
        }

        boolean silentCast = isSilentStealthSpell(spell, fallbackMateria);
        if (!silentCast) {
            level.playSound(null, caster.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 0.85F, 1.2F);
            SubSpellSlot first = slots.getFirst();
            EssenceType pMat = first.primaryEffect().resolveEffectiveMateria(materiaSpecific, fallbackMateria);
            EssenceType sMat = first.secondaryEffect().map(s -> s.resolveEffectiveMateria(materiaSpecific, fallbackMateria)).orElse(null);
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnCastMuzzleBurst(
                    level,
                    caster.getEyePosition().subtract(0.0D, 0.18D, 0.0D),
                    caster.getLookAngle(),
                    pMat,
                    sMat,
                    spell.getEffectiveTier()
            );
        }

        EssenceType flowMateria = flowBinding.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        MateriaPartMutationHandler flowHandler = SpellMutationHandlerRegistry.resolveActive(flowMateria, flowBinding.mutationEnabled());

        // 0. Touch / Melee Strike Priming (Guide Section 4.4):
        // Touch/Strike ALWAYS primes the player's weapon/hand for their next hit rather than firing as a spell payload!
        if (caster instanceof Player player && spell.hasTouchStrikeForm()) {
            List<SubSpellSlot> touchSlots = new ArrayList<>();
            List<SubSpellSlot> remainingSlots = new ArrayList<>();
            for (SubSpellSlot s : slots) {
                if (s.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE) {
                    touchSlots.add(s);
                } else {
                    remainingSlots.add(s);
                }
            }
            primeCasterTouchStrike(level, player, spell, touchSlots, fallbackMateria, apparatusPotency, apparatusRange, costBreakdown);
            if (remainingSlots.isEmpty()) {
                return;
            }
            spell = new SpellAssembly(
                    spell.customName(),
                    spell.customLore(),
                    spell.materiaSpecific(),
                    spell.prefix(),
                    spell.potencyTier(),
                    spell.flowPart().withSubSpellCount(remainingSlots.size()),
                    remainingSlots,
                    spell.flawProfile(),
                    spell.resoniteCritFragments()
            );
            slots = spell.subSpells();
        }

        // 1. Self Trigger: immediately execute all 1..8 Multi-Cast sub-spells centered on the caster (or launch Orbit/Dome satellites)!
        if (trigger == SpellTriggerType.SELF) {
            if (flow == SpellFlowModifier.ORBIT_SATELLITE || flow == SpellFlowModifier.OCTUPLE_DOME) {
                launchFlowCarrierPayloads(level, caster, flow, flowHandler, spell, fallbackMateria, apparatusPotency, apparatusRange);
            } else {
                executeSelfMultiCast(level, caster, spell, fallbackMateria, apparatusPotency, apparatusRange, silentCast);
            }
            return;
        }

        // 2. Self-Timed Contingency Trigger (Guide Section 3.5: 30s / 60s / 120s delayed self-discharge at 100% potency!)
        if (trigger == SpellTriggerType.SELF_TIMED) {
            float configuredSec = flowBinding.delaySeconds() >= 5.0F ? flowBinding.delaySeconds() : 30.0F;
            int delayTicks = Math.clamp(Math.round(configuredSec * 20.0F), 100, 2400);
            SubSpellSlot first = slots.getFirst();
            EssenceType pMat = first.primaryEffect().resolveEffectiveMateria(materiaSpecific, fallbackMateria);
            EssenceType sMat = first.secondaryEffect().map(sec -> sec.resolveEffectiveMateria(materiaSpecific, fallbackMateria)).orElse(null);
            if (!silentCast) {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                        level, caster.position(), 1.4D, pMat, sMat, 30, spell.getEffectiveTier()
                );
                level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.35F);
            }
            if (caster instanceof Player player) {
                player.displayClientMessage(
                        Component.literal("§d✦ Self-Timed Contingency Armed §7— §f" + spell.customName()
                                + " §7(" + SpellFlowModifier.getCastCountTitle(slots.size())
                                + ") triggers in §e" + Math.round(configuredSec) + "s§7!"),
                        true
                );
            }
            final SpellAssembly contingencySpell = spell;
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster,
                    caster.position(),
                    1.0D,
                    delayTicks,
                    level.getGameTime(),
                    null,
                    null,
                    pMat,
                    apparatusPotency,
                    (srv, zone, elapsed) -> {
                        LivingEntity owner = zone.resolveCaster(srv);
                        if (owner == null || !owner.isAlive()) {
                            return false;
                        }
                        if (elapsed < zone.maxDurationTicks) {
                            if (!silentCast && elapsed > 0 && elapsed % 20 == 0) {
                                srv.sendParticles(ParticleTypes.ENCHANT, owner.getX(), owner.getY(0.75D), owner.getZ(),
                                        5, 0.35D, 0.35D, 0.35D, 0.04D);
                            }
                            return true;
                        }
                        if (consumeDynamicTriggerFeeForPayload(srv, owner, contingencySpell, fallbackMateria)) {
                            if (flow == SpellFlowModifier.ORBIT_SATELLITE || flow == SpellFlowModifier.OCTUPLE_DOME) {
                                launchFlowCarrierPayloads(srv, owner, flow, flowHandler, contingencySpell, fallbackMateria, apparatusPotency, apparatusRange);
                            } else {
                                executeSelfMultiCast(srv, owner, contingencySpell, fallbackMateria, apparatusPotency, apparatusRange, silentCast);
                            }
                        }
                        return false;
                    }
            ));
            return;
        }

        if (flowBinding.mutationEnabled() && flowMateria == EssenceType.CHIMERA) {
            String rolled = switch (level.random.nextInt(4)) {
                case 0 -> "AIR";
                case 1 -> "EARTH";
                case 2 -> "WATER";
                default -> "NETHER";
            };
            EntityHelper.getPersistentData(caster).putString(SpellLingeringZoneManager.TAG_CHIMERA_ORBIT_ELEMENT, rolled);
        }
        if (flowBinding.mutationEnabled() && (flow == SpellFlowModifier.ORBIT_SATELLITE
                || flowBinding.usesDynamicTriggerBilling(spell.materiaSpecific(), fallbackMateria))) {
            long orbitUntil = level.getGameTime() + 160L;
            CompoundTag cData = EntityHelper.getPersistentData(caster);
            switch (flowMateria) {
                case SPORE -> cData.putLong(SpellLingeringZoneManager.TAG_SPORE_ORBIT_UNTIL, orbitUntil);
                case NULL_R -> cData.putLong(SpellLingeringZoneManager.TAG_NULL_R_ORBIT_UNTIL, orbitUntil);
                case NULL_U -> cData.putLong(SpellLingeringZoneManager.TAG_NULL_U_ORBIT_UNTIL, orbitUntil);
                case DAWN -> cData.putLong(SpellLingeringZoneManager.TAG_DAWN_ORBIT_UNTIL, orbitUntil);
                case ABYSS -> cData.putLong(SpellLingeringZoneManager.TAG_ABYSS_ORBIT_UNTIL, orbitUntil);
                case AEGIS -> {
                    cData.putLong(SpellLingeringZoneManager.TAG_AEGIS_ORBIT_UNTIL, orbitUntil);
                    SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.0D, 0.0D, 160L);
                }
                case AURORA -> cData.putLong(SpellLingeringZoneManager.TAG_AURORA_ORBIT_UNTIL, orbitUntil);
                case MIRAGE -> cData.putLong(SpellLingeringZoneManager.TAG_MIRAGE_ORBIT_UNTIL, orbitUntil);
                case WRAITH -> {
                    cData.putLong(SpellLingeringZoneManager.TAG_WRAITH_ORBIT_UNTIL, orbitUntil);
                    SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.0D, 0.0D, 160L);
                }
                case BARROW -> cData.putLong(SpellLingeringZoneManager.TAG_BARROW_ORBIT_UNTIL, orbitUntil);
                case PYRE -> {
                    cData.putLong(SpellLingeringZoneManager.TAG_PYRE_ORBIT_UNTIL, orbitUntil);
                    SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 3.0D, 0.0D, 0.0D, 0.0D, 160L);
                }
                case PENUMBRA -> {
                    cData.putLong(SpellLingeringZoneManager.TAG_PENUMBRA_ORBIT_UNTIL, orbitUntil);
                    SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 5.0D, 2.0D, 0.0D, 0.0D, 160L);
                }
                case RIME -> {
                    cData.putLong(SpellLingeringZoneManager.TAG_RIME_ORBIT_UNTIL, orbitUntil);
                    SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 5.0D, 2.0D, 0.0D, 0.0D, 160L);
                }
                case SPRING -> {
                    cData.putLong(SpellLingeringZoneManager.TAG_SPRING_ORBIT_UNTIL, orbitUntil);
                    SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 5.0D, 2.0D, 0.0D, 0.0D, 160L);
                }
                case STATIC -> {
                    cData.putLong(SpellLingeringZoneManager.TAG_STATIC_ORBIT_UNTIL, orbitUntil);
                    SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 5.0D, 2.0D, 0.0D, 0.0D, 160L);
                }
                case SYLVAN -> {
                    cData.putLong(SpellLingeringZoneManager.TAG_SYLVAN_ORBIT_UNTIL, orbitUntil);
                    SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 5.0D, 2.0D, 0.0D, 0.0D, 160L);
                }
                case GENESIS -> cData.putLong(SpellLingeringZoneManager.TAG_GENESIS_ORBIT_UNTIL, orbitUntil);
                case CHIMERA -> cData.putLong(SpellLingeringZoneManager.TAG_CHIMERA_ORBIT_UNTIL, orbitUntil);
                case AETHER -> cData.putLong(SpellLingeringZoneManager.TAG_AETHER_ORBIT_UNTIL, orbitUntil);
                case CELESTIAL -> cData.putLong(SpellLingeringZoneManager.TAG_CELESTIAL_ORBIT_UNTIL, orbitUntil);
                case PRISMATIC -> cData.putLong(SpellLingeringZoneManager.TAG_PRISMATIC_ORBIT_UNTIL, orbitUntil);
                case EMPYREAN -> cData.putLong(SpellLingeringZoneManager.TAG_EMPYREAN_ORBIT_UNTIL, orbitUntil);
                case CATACLYSM -> cData.putLong(SpellLingeringZoneManager.TAG_CATACLYSM_ORBIT_UNTIL, orbitUntil);
                case TERMINUS -> cData.putLong(SpellLingeringZoneManager.TAG_TERMINUS_ORBIT_UNTIL, orbitUntil);
                case ESCHATON -> cData.putLong(SpellLingeringZoneManager.TAG_ESCHATON_ORBIT_UNTIL, orbitUntil);
                case APOTHEOSIS -> cData.putLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_ORBIT_UNTIL, orbitUntil);
                case ENTROPICA -> cData.putLong(SpellLingeringZoneManager.TAG_ENTROPICA_ORBIT_UNTIL, orbitUntil);
                default -> {
                }
            }
            SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.50F, null);
        }

        // 2b. Point-Blank 0m Travel Distance Stop (Outer Rim Distance Circle = 0m):
        //     Discharges at point-blank muzzle / caster origin with zero forward carrier travel (except Orbiting Satellites, which orbit first).
        if (flowBinding.distanceStep().isPointBlankZero() && flow != SpellFlowModifier.ORBIT_SATELLITE) {
            SubSpellSlot leadSlot = slots.getFirst();
            SpellDeliveryForm leadForm = leadSlot.formPart().form();
            if (leadSlot.primaryEffect().effect() == SpellCoreEffect.BLINK
                    && leadForm != SpellDeliveryForm.GLYPH
                    && !leadForm.isSummonForm()) {
                Vec3 lookDir = caster.getLookAngle().normalize();
                dispatchSingleSubSpellForm(
                        level, caster, lookDir, spell, 0, leadSlot, fallbackMateria, apparatusPotency, apparatusRange
                );
                if (BLINK_CANCELLED_REFUND_FLAG.get()) {
                    return;
                }
                for (int i = 1; i < slots.size(); i++) {
                    dispatchSingleSubSpellForm(
                            level, caster, lookDir, spell, i, slots.get(i), fallbackMateria, apparatusPotency, apparatusRange
                    );
                }
                return;
            }
            executePointBlankZeroCast(level, caster, spell, fallbackMateria, apparatusPotency, apparatusRange);
            return;
        }

        // 3. Void-Warp Travel Mutation Relay (Guide Section 3.4: requires Void or Void-Created Materia + Mutation ACTIVE!)
        if (flowBinding.isVoidWarpAuthorized(spell.materiaSpecific(), fallbackMateria)) {
            executeVoidWarpRelay(level, caster, spell, fallbackMateria, apparatusPotency, apparatusRange, flowMateria);
            return;
        }

        // 4. Direct Instant Blink / Lead-Blink Multi-Cast (Guide Section 5.4):
        //    Standard Self-Blink & Position Swap execute immediately along crosshair without launching a slow carrier orb;
        //    multi-cast assemblies starting with Self-Blink teleport first and detonate follow-up sub-spells upon arrival!
        boolean linearBlinkFlow = flow == SpellFlowModifier.SINGLE
                || flow == SpellFlowModifier.FAST_DART
                || flow == SpellFlowModifier.SLOW_HEAVY
                || flow == SpellFlowModifier.VOID_WARP;
        boolean directBlinkTrigger = trigger == SpellTriggerType.ON_ENTITY_HIT
                || trigger == SpellTriggerType.ON_BLOCK_COLLISION
                || trigger == SpellTriggerType.ON_RELEASE;
        SubSpellSlot firstSlot = slots.getFirst();
        if (linearBlinkFlow && directBlinkTrigger && firstSlot.primaryEffect().effect() == SpellCoreEffect.BLINK) {
            SpellDeliveryForm firstForm = firstSlot.formPart().form();
            boolean directBlinkForm = firstForm != SpellDeliveryForm.GLYPH && !firstForm.isSummonForm();
            String firstMode = firstSlot.primaryEffect().subMode().toLowerCase(Locale.ROOT);
            boolean firstIsSelfBlink = !firstMode.equals("swap") && !firstMode.equals("transposition");
            if (directBlinkForm && (slots.size() == 1 || firstIsSelfBlink)) {
                Vec3 lookDir = caster.getLookAngle().normalize();
                dispatchSingleSubSpellForm(
                        level, caster, lookDir, spell, 0, firstSlot, fallbackMateria, apparatusPotency, apparatusRange
                );
                if (BLINK_CANCELLED_REFUND_FLAG.get()) {
                    return;
                }
                for (int i = 1; i < slots.size(); i++) {
                    dispatchSingleSubSpellForm(
                            level, caster, lookDir, spell, i, slots.get(i), fallbackMateria, apparatusPotency, apparatusRange
                    );
                }
                return;
            }
        }

        // 4b. Native Single Cast (1x) + Normal Flow + Direct/Release Trigger: execute the single sub-spell Form directly!
        if (slots.size() == 1
                && (flow == SpellFlowModifier.SINGLE || flow == SpellFlowModifier.VOID_WARP)
                && (trigger == SpellTriggerType.ON_ENTITY_HIT || trigger == SpellTriggerType.ON_RELEASE)) {
            dispatchSingleSubSpellForm(
                    level,
                    caster,
                    caster.getLookAngle().normalize(),
                    spell,
                    0,
                    slots.getFirst(),
                    fallbackMateria,
                    apparatusPotency,
                    apparatusRange
            );
            return;
        }

        // 5. Carrier Payload Flows, Triggered Shells & Point-of-Contact Multi-Cast (Double 2x .. Octuple 8x):
        // Launch the Flow Modifier's carrier projectile(s); each payload casts ALL slotted spells when its Trigger fires!
        launchFlowCarrierPayloads(
                level,
                caster,
                flow,
                flowHandler,
                spell,
                fallbackMateria,
                apparatusPotency,
                apparatusRange
        );
    }

    private static void executeSelfMultiCast(
            ServerLevel level,
            LivingEntity caster,
            SpellAssembly spell,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            float apparatusRange,
            boolean silentCast
    ) {
        List<SubSpellSlot> slots = spell.subSpells();
        int tier = spell.getEffectiveTier();
        Vec3 look = caster.getLookAngle().normalize();

        for (int i = 0; i < slots.size(); i++) {
            SubSpellSlot s = slots.get(i);
            SpellDeliveryForm form = s.formPart().form();
            EssenceType pMat = s.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            EssenceType sMat = s.secondaryEffect().map(sec -> sec.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria)).orElse(null);

            if (s.primaryEffect().effect() == SpellCoreEffect.BLINK && form != SpellDeliveryForm.GLYPH && !form.isSummonForm()) {
                dispatchSingleSubSpellForm(level, caster, look, spell, i, s, fallbackMateria, apparatusPotency, apparatusRange);
                if (i == 0 && BLINK_CANCELLED_REFUND_FLAG.get()) {
                    return;
                }
                continue;
            }

            if (!silentCast && (form == SpellDeliveryForm.PROJECTILE
                    || form == SpellDeliveryForm.BEAM
                    || form == SpellDeliveryForm.TOUCH_STRIKE)) {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastWardShell(
                        level, caster.position(), 1.35D, pMat, sMat, tier
                );
            }

            switch (form) {
                case AURA, RADIAL_BURST, GLYPH, CONE, SKYFALL, SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> {
                    dispatchSingleSubSpellForm(level, caster, look, spell, i, s, fallbackMateria, apparatusPotency, apparatusRange);
                    if (form != SpellDeliveryForm.AURA
                            && form != SpellDeliveryForm.RADIAL_BURST
                            && s.primaryEffect().effect() != SpellCoreEffect.BLINK
                            && isCasterBeneficialVerb(s.primaryEffect().effect())) {
                        deliverSelfFocusedSubSpell(level, caster, spell, s, fallbackMateria, apparatusPotency);
                    }
                }
                default -> deliverSelfFocusedSubSpell(level, caster, spell, s, fallbackMateria, apparatusPotency);
            }
        }
    }

    private static void executePointBlankZeroCast(
            ServerLevel level,
            LivingEntity caster,
            SpellAssembly spell,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            float apparatusRange
    ) {
        List<SubSpellSlot> slots = spell.subSpells();
        if (slots.isEmpty()) {
            return;
        }
        Vec3 look = caster.getLookAngle().normalize();

        // Grant caster wings immediately if any slotted sub-spell includes FLIGHT!
        for (SubSpellSlot s : slots) {
            if (slotHasEffect(s, SpellCoreEffect.FLIGHT)) {
                deliverFlightOnlyToCaster(level, caster, look, spell, s, fallbackMateria, apparatusPotency);
                break;
            }
        }

        Vec3 eye = caster.getEyePosition();
        double pbReach = 1.85D;
        Vec3 maxEnd = eye.add(look.scale(pbReach));
        BlockHitResult blockHit = level.clip(new ClipContext(eye, maxEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, caster));
        Vec3 clipStop = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation() : maxEnd;

        SpellTriggerType trigger = spell.flowPart().trigger();
        LivingEntity hitEntity = null;
        double bestDistSq = eye.distanceToSqr(clipStop);
        if (trigger != SpellTriggerType.ON_BLOCK_COLLISION) {
            AABB pbBox = new AABB(eye, clipStop).inflate(0.85D);
            for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, pbBox, LivingEntity::isAlive)) {
                if (cand == caster) continue;
                Optional<Vec3> clip = cand.getBoundingBox().inflate(0.4D).clip(eye, clipStop);
                if (clip.isPresent()) {
                    double dSq = eye.distanceToSqr(clip.get());
                    if (dSq < bestDistSq) {
                        bestDistSq = dSq;
                        hitEntity = cand;
                    }
                }
            }
        }

        if (!consumeDynamicTriggerFeeForPayload(level, caster, spell, fallbackMateria)) {
            return;
        }

        Vec3 contactPos;
        BlockPos hitBlockPos;
        Direction hitFace;
        if (hitEntity != null) {
            contactPos = hitEntity.position().add(0.0D, hitEntity.getBbHeight() * 0.55D, 0.0D);
            hitBlockPos = hitEntity.blockPosition();
            hitFace = Direction.UP;
        } else if (blockHit.getType() == HitResult.Type.BLOCK) {
            contactPos = blockHit.getLocation();
            hitBlockPos = blockHit.getBlockPos();
            hitFace = blockHit.getDirection();
        } else {
            contactPos = caster.position().add(look.scale(0.85D)).add(0.0D, caster.getBbHeight() * 0.55D, 0.0D);
            hitBlockPos = BlockPos.containing(contactPos);
            hitFace = Direction.UP;
        }

        if (trigger == SpellTriggerType.ON_DEATH && hitEntity != null) {
            markEntityForCorpseBloom(level, hitEntity, caster, spell, fallbackMateria, apparatusPotency, apparatusRange);
            deliverSubSpellOnEntity(level, caster, hitEntity, look, spell, slots.getFirst(), fallbackMateria, apparatusPotency);
            return;
        }

        if (trigger == SpellTriggerType.ON_TIMER) {
            float sec = spell.flowPart().delaySeconds() >= 0.5F ? spell.flowPart().delaySeconds() : 1.5F;
            int fuseTicks = Math.clamp(Math.round(sec * 20.0F), 10, 80);
            EssenceType pMat = slots.getFirst().primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            LivingEntity lockedEnt = hitEntity;
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, contactPos, 1.0D, fuseTicks, level.getGameTime(), null, null, pMat, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) {
                            if (elapsed % 4 == 0) {
                                srv.sendParticles(ParticleTypes.ENCHANT, zone.center.x, zone.center.y, zone.center.z,
                                        5, 0.18D, 0.18D, 0.18D, 0.03D);
                            }
                            return true;
                        }
                        LivingEntity owner = zone.resolveCaster(srv);
                        executeContactMultiCast(srv, owner, null, zone.center, lockedEnt != null && lockedEnt.isAlive() ? lockedEnt : null,
                                hitBlockPos, hitFace, look, spell, fallbackMateria, apparatusPotency, apparatusRange);
                        return false;
                    }
            ));
            return;
        }

        executeContactMultiCast(
                level,
                caster,
                null,
                contactPos,
                hitEntity,
                hitBlockPos,
                hitFace,
                look,
                spell,
                fallbackMateria,
                apparatusPotency,
                apparatusRange
        );

        if (spell.flowPart().flow() == SpellFlowModifier.CHAIN_LEAP && hitEntity != null) {
            chainLeapMultiCastFrom(level, caster, hitEntity, spell, fallbackMateria, apparatusPotency, apparatusRange);
        }
    }

    private static void executeVoidWarpRelay(
            ServerLevel level,
            LivingEntity caster,
            SpellAssembly spell,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            float apparatusRange,
            EssenceType flowMateria
    ) {
        int tier = spell.getEffectiveTier();
        double baseWarpMeters = spell.flowPart().distanceStep().isPointBlankZero()
                ? 1.5D
                : spell.flowPart().travelDistanceMeters();
        double maxRange = baseWarpMeters * apparatusRange * spell.flawProfile().getRangeAndRadiusMultiplier();
        Vec3 start = caster.getEyePosition();
        Vec3 look = caster.getLookAngle().normalize();
        Vec3 maxEnd = start.add(look.scale(maxRange));

        BlockHitResult blockHit = level.clip(new ClipContext(start, maxEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, caster));
        // Section 3.4 Barrier Phasing: Void-Warp ALWAYS ignores intervening glass panes, bars, fences, leaves & thin obstacles in transit!
        int thinPhaseSteps = 0;
        Vec3 probeStart = start;
        while (blockHit.getType() == HitResult.Type.BLOCK
                && thinPhaseSteps++ < 8
                && ddraig.net.entropica.spell.mutation.element.VoidSpellMutationHandler.isThinVoidPassableObstacle(
                        level, blockHit.getBlockPos(), level.getBlockState(blockHit.getBlockPos()), look)) {
            probeStart = blockHit.getLocation().add(look.scale(0.85D));
            if (start.distanceToSqr(probeStart) >= maxRange * maxRange) {
                blockHit = BlockHitResult.miss(maxEnd, Direction.getApproximateNearest(look.x, look.y, look.z), BlockPos.containing(maxEnd));
                break;
            }
            blockHit = level.clip(new ClipContext(probeStart, maxEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, caster));
        }
        Vec3 stopPos = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation() : maxEnd;

        SpellTriggerType trigger = spell.flowPart().trigger();

        // Ray-cast living entities along visible line first (unless On-Block-Collision specifically targets world geometry!)
        LivingEntity bestEntity = null;
        double bestDistSq = start.distanceToSqr(stopPos);
        if (trigger != SpellTriggerType.ON_BLOCK_COLLISION) {
            AABB rayBox = new AABB(start, stopPos).inflate(1.0D);
            for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, rayBox, LivingEntity::isAlive)) {
                if (cand == caster) continue;
                Optional<Vec3> clip = cand.getBoundingBox().inflate(0.4D).clip(start, stopPos);
                if (clip.isPresent()) {
                    double dSq = start.distanceToSqr(clip.get());
                    if (dSq < bestDistSq) {
                        bestDistSq = dSq;
                        bestEntity = cand;
                    }
                }
            }

            // Section 3.4 & 6.11 Wall Phasing Synergy: Tier 4+ spells, Void-Warp Phasing Flow, OR Starlight-Sonar-revealed
            // targets can be warped directly behind solid walls into hidden rooms!
            boolean fullVoidWallPhase = tier >= 4
                    || (spell.flowPart().mutationEnabled() && flowMateria == EssenceType.VOID);
            boolean canBypassWalls = fullVoidWallPhase
                    || (spell.flowPart().mutationEnabled() && MateriaFamilyTier.hasVoidCreationHeritage(flowMateria));
            if (bestEntity == null && canBypassWalls && blockHit.getType() == HitResult.Type.BLOCK) {
                AABB fullBox = new AABB(start, maxEnd).inflate(1.5D);
                double wallBest = Double.MAX_VALUE;
                for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, fullBox, LivingEntity::isAlive)) {
                    if (cand == caster || (!fullVoidWallPhase && !cand.hasEffect(MobEffects.GLOWING))) continue;
                    Optional<Vec3> clip = cand.getBoundingBox().inflate(0.65D).clip(start, maxEnd);
                    if (clip.isPresent()) {
                        double dSq = start.distanceToSqr(clip.get());
                        if (dSq < wallBest) {
                            wallBest = dSq;
                            bestEntity = cand;
                        }
                    }
                }
            }
        }
        // Section 3.4 Barrier Phasing: Void-Warp materializes behind/inside raised shields, ignoring shield blocks!
        if (bestEntity != null && bestEntity.isBlocking() && !spell.prefix().enforcesZeroEntityHarm()) {
            if (bestEntity instanceof Player bp && !bp.getUseItem().isEmpty()) {
                bp.getCooldowns().addCooldown(bp.getUseItem(), 60);
            }
            bestEntity.stopUsingItem();
        }

        Vec3 handRift = start.subtract(0.0D, 0.2D, 0.0D).add(look.scale(0.55D));
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, handRift.x, handRift.y, handRift.z, 14, 0.15D, 0.15D, 0.15D, 0.04D);

        if (bestEntity == null && blockHit.getType() != HitResult.Type.BLOCK && !hasAnyOpenAirVerb(spell)
                && trigger != SpellTriggerType.ON_TIMER) {
            // Complete open-sky miss: play fizzled rift at max range and schedule 5s upfront fuel refund!
            level.sendParticles(ParticleTypes.PORTAL, maxEnd.x, maxEnd.y, maxEnd.z, 16, 0.25D, 0.25D, 0.25D, 0.08D);
            if (caster instanceof Player player) {
                scheduleMissFuelRefund(level, player, spell, fallbackMateria);
            }
            return;
        }

        if (!consumeDynamicTriggerFeeForPayload(level, caster, spell, fallbackMateria)) {
            return;
        }

        Vec3 contactPos = bestEntity != null
                ? bestEntity.position().add(0.0D, bestEntity.getBbHeight() * 0.5D, 0.0D)
                : (blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation() : maxEnd);
        BlockPos hitBlockPos = bestEntity != null
                ? bestEntity.blockPosition()
                : (blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getBlockPos() : BlockPos.containing(maxEnd));
        Direction hitFace = bestEntity != null
                ? Direction.UP
                : (blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getDirection() : Direction.UP);

        EssenceType riftMat = flowMateria != EssenceType.REGULAR ? flowMateria : EssenceType.VOID;
        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastBlinkRift(level, handRift, contactPos, riftMat, tier);
        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                level, contactPos.subtract(0.0D, 0.75D, 0.0D), 1.45D, true, riftMat, null, 18, tier
        );
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, contactPos.x, contactPos.y, contactPos.z, 20, 0.3D, 0.3D, 0.3D, 0.06D);
        level.playSound(null, BlockPos.containing(contactPos), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.9F, 1.3F);

        if (trigger == SpellTriggerType.ON_DEATH && bestEntity != null) {
            markEntityForCorpseBloom(level, bestEntity, caster, spell, fallbackMateria, apparatusPotency, apparatusRange);
            deliverSubSpellOnEntity(level, caster, bestEntity, look, spell, spell.subSpells().getFirst(), fallbackMateria, apparatusPotency);
            return;
        }

        if (trigger == SpellTriggerType.ON_TIMER) {
            float sec = spell.flowPart().delaySeconds() >= 0.5F ? spell.flowPart().delaySeconds() : 1.5F;
            int fuseTicks = Math.clamp(Math.round(sec * 20.0F), 10, 80);
            LivingEntity lockedEnt = bestEntity;
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, contactPos, 1.2D, fuseTicks, level.getGameTime(), null, null, riftMat, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) {
                            if (elapsed % 4 == 0) {
                                srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y, zone.center.z,
                                        6, 0.2D, 0.2D, 0.2D, 0.03D);
                            }
                            return true;
                        }
                        LivingEntity owner = zone.resolveCaster(srv);
                        executeContactMultiCast(srv, owner, null, zone.center, lockedEnt != null && lockedEnt.isAlive() ? lockedEnt : null,
                                hitBlockPos, hitFace, look, spell, fallbackMateria, apparatusPotency, apparatusRange);
                        return false;
                    }
            ));
            return;
        }

        float effectivePotency = (trigger == SpellTriggerType.ON_ENTITY_HIT
                && bestEntity == null
                && blockHit.getType() == HitResult.Type.BLOCK
                && shouldPenalizeEntityTriggerBlockFallback(spell))
                ? (apparatusPotency * 0.75F)
                : apparatusPotency;

        executeContactMultiCast(
                level,
                caster,
                null,
                contactPos,
                bestEntity,
                hitBlockPos,
                hitFace,
                look,
                spell,
                fallbackMateria,
                effectivePotency,
                apparatusRange
        );
    }

    private static void launchFlowCarrierPayloads(
            ServerLevel level,
            LivingEntity caster,
            SpellFlowModifier flow,
            @Nullable MateriaPartMutationHandler flowHandler,
            SpellAssembly spell,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            float apparatusRange
    ) {
        List<SubSpellSlot> slots = spell.subSpells();
        SubSpellSlot visualSlot = slots.getFirst();
        EssenceType formMateria = visualSlot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        SpellMutationProfile formMutation = visualSlot.formPart().resolveMutation(spell.materiaSpecific(), fallbackMateria);
        MateriaPartMutationHandler formHandler = SpellMutationHandlerRegistry.resolveActive(formMateria, visualSlot.formPart().mutationEnabled());
        SpellMutationProfile flowMutation = spell.flowPart().resolveMutation(spell.materiaSpecific(), fallbackMateria);

        // Grant caster wings on launch if any slotted sub-spell includes FLIGHT!
        for (SubSpellSlot s : slots) {
            if (slotHasEffect(s, SpellCoreEffect.FLIGHT)) {
                deliverFlightOnlyToCaster(level, caster, caster.getLookAngle().normalize(), spell, s, fallbackMateria, apparatusPotency);
                break;
            }
        }

        SpellTriggerType trigger = spell.flowPart().trigger();
        boolean timedMidAirForkShell = trigger == SpellTriggerType.ON_TIMER
                && (flow == SpellFlowModifier.DOUBLE_BIFURCATE
                        || flow == SpellFlowModifier.TRIPLE_TRIFURCATE
                        || flow == SpellFlowModifier.OCTUPLE_DOME);

        float baseYaw = caster.getYRot();
        float basePitch = caster.getXRot();
        List<Vec3> launchDirs = new ArrayList<>();
        List<UUID> assignedTargets = new ArrayList<>();

        if (timedMidAirForkShell) {
            // Guide Section 3.5: Combining Timed Fuse with Fork/Scatter launches 1 primary fuse shell that splits mid-air!
            launchDirs.add(caster.getLookAngle().normalize());
            assignedTargets.add(null);
        } else {
            switch (flow) {
                case DOUBLE_BIFURCATE -> {
                    launchDirs.add(Vec3.directionFromRotation(basePitch, baseYaw - 12.0F));
                    launchDirs.add(Vec3.directionFromRotation(basePitch, baseYaw + 12.0F));
                    double lockDist = Math.max(8.0D, spell.flowPart().travelDistanceMeters()) * apparatusRange;
                    List<LivingEntity> hostiles = findForwardHostileTargets(level, caster, lockDist, spell);
                    for (int i = 0; i < 2; i++) {
                        assignedTargets.add(hostiles.isEmpty() ? null : hostiles.get(i % hostiles.size()).getUUID());
                    }
                }
                case TRIPLE_TRIFURCATE -> {
                    launchDirs.add(Vec3.directionFromRotation(basePitch, baseYaw - 18.0F));
                    launchDirs.add(Vec3.directionFromRotation(basePitch, baseYaw));
                    launchDirs.add(Vec3.directionFromRotation(basePitch, baseYaw + 18.0F));
                    double lockDist = Math.max(8.0D, spell.flowPart().travelDistanceMeters()) * apparatusRange;
                    List<LivingEntity> hostiles = findForwardHostileTargets(level, caster, lockDist, spell);
                    for (int i = 0; i < 3; i++) {
                        assignedTargets.add(hostiles.isEmpty() ? null : hostiles.get(i % hostiles.size()).getUUID());
                    }
                }
                case OCTUPLE_DOME -> {
                    for (int i = 0; i < 8; i++) {
                        float yaw = baseYaw + (45.0F * i);
                        float pitch = (i % 2 == 0) ? -25.0F : -52.0F;
                        launchDirs.add(Vec3.directionFromRotation(pitch, yaw));
                        assignedTargets.add(null);
                    }
                }
                case ORBIT_SATELLITE -> {
                    for (int i = 0; i < 4; i++) {
                        float yaw = baseYaw + (90.0F * i);
                        launchDirs.add(Vec3.directionFromRotation(0.0F, yaw));
                        assignedTargets.add(null);
                    }
                }
                default -> {
                    launchDirs.add(caster.getLookAngle().normalize());
                    assignedTargets.add(null);
                }
            }
        }

        if (launchDirs.size() > 1 && flowHandler != null) {
            flowHandler.onFlowMultiCastOrigin(level, caster, flow, launchDirs.size(), spell);
        }

        float baseSpeed = trigger == SpellTriggerType.ON_PROXIMITY
                ? 0.95F
                : switch (flow) {
                    case FAST_DART -> 2.45F;
                    case SLOW_HEAVY -> 0.75F;
                    case OCTUPLE_DOME -> 1.35F;
                    default -> 1.65F;
                };
        float speed = baseSpeed
                * (flowMutation.mutated() ? flowMutation.velocityMultiplier() : 1.0F)
                * (visualSlot.formPart().form() == SpellDeliveryForm.PROJECTILE && formMutation.mutated() ? formMutation.velocityMultiplier() : 1.0F);

        for (int i = 0; i < launchDirs.size(); i++) {
            Vec3 dir = launchDirs.get(i);
            SpellProjectileEntity proj = new SpellProjectileEntity(level, caster);
            proj.setPos(caster.getX() + dir.x * 0.6D, caster.getEyeY() - 0.15D + dir.y * 0.6D, caster.getZ() + dir.z * 0.6D);
            proj.configurePayload(spell, 0, fallbackMateria, apparatusPotency);
            proj.configureCarrierPayload(assignedTargets.get(i), apparatusRange);
            if (flow == SpellFlowModifier.ORBIT_SATELLITE) {
                proj.setOrbitPhaseRadians((float) (i * (Math.PI * 0.5D)));
            }
            proj.shoot(dir.x, dir.y, dir.z, speed, 0.0F);
            level.addFreshEntity(proj);
            if (visualSlot.formPart().form() == SpellDeliveryForm.PROJECTILE && formHandler != null) {
                formHandler.onProjectileSpawnedFromForm(level, proj, caster, spell, visualSlot);
            }
        }
    }

    /**
     * Guide Section 3.5 Timed Fuse + Multi-Payload Flow Synergy:
     * Splits a mid-air Timed Fuse shell into its 2 (Bifurcate), 3 (Trifurcate), or 8 (Octuple Dome) child carrier payloads!
     */
    public static void burstTimedMidAirFlowSplit(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 origin,
            Vec3 forwardDir,
            SpellAssembly spell,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            float apparatusRange
    ) {
        SpellFlowModifier flow = spell.flowPart().flow();
        SpellAssembly childSpell = new SpellAssembly(
                spell.customName(),
                spell.customLore(),
                spell.materiaSpecific(),
                spell.prefix(),
                spell.potencyTier(),
                spell.flowPart().withTrigger(SpellTriggerType.ON_ENTITY_HIT),
                spell.subSpells(),
                spell.flawProfile(),
                spell.resoniteCritFragments()
        );

        float baseYaw = (float) (Math.toDegrees(Math.atan2(-forwardDir.x, forwardDir.z)));
        float basePitch = (float) (Math.toDegrees(-Math.asin(Math.clamp(forwardDir.y, -1.0D, 1.0D))));
        List<Vec3> childDirs = new ArrayList<>();
        switch (flow) {
            case DOUBLE_BIFURCATE -> {
                childDirs.add(Vec3.directionFromRotation(basePitch, baseYaw - 16.0F));
                childDirs.add(Vec3.directionFromRotation(basePitch, baseYaw + 16.0F));
            }
            case TRIPLE_TRIFURCATE -> {
                childDirs.add(Vec3.directionFromRotation(basePitch, baseYaw - 22.0F));
                childDirs.add(Vec3.directionFromRotation(basePitch, baseYaw));
                childDirs.add(Vec3.directionFromRotation(basePitch, baseYaw + 22.0F));
            }
            default -> {
                for (int i = 0; i < 8; i++) {
                    float yaw = baseYaw + (45.0F * i);
                    float pitch = (i % 2 == 0) ? -18.0F : -42.0F;
                    childDirs.add(Vec3.directionFromRotation(pitch, yaw));
                }
            }
        }

        List<LivingEntity> nearbyHostiles = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(origin, origin).inflate(14.0D * apparatusRange),
                e -> e.isAlive()
                        && e != caster
                        && (e instanceof net.minecraft.world.entity.monster.Enemy || (caster != null && e == caster.getLastHurtMob()))
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                e, caster, spell.hasWardFriendlyFireSafeguard()))
        );
        nearbyHostiles.sort(Comparator.comparingDouble(e -> e.distanceToSqr(origin)));

        level.sendParticles(ParticleTypes.FIREWORK, origin.x, origin.y, origin.z, 18, 0.25D, 0.25D, 0.25D, 0.08D);
        SubSpellSlot splitVisualSlot = childSpell.subSpells().getFirst();
        EssenceType splitFormMat = splitVisualSlot.formPart().resolveEffectiveMateria(childSpell.materiaSpecific(), fallbackMateria);
        MateriaPartMutationHandler splitFormHandler = SpellMutationHandlerRegistry.resolveActive(
                splitFormMat, splitVisualSlot.formPart().mutationEnabled()
        );
        for (int i = 0; i < childDirs.size(); i++) {
            Vec3 d = childDirs.get(i);
            SpellProjectileEntity child = caster != null
                    ? new SpellProjectileEntity(level, caster)
                    : new SpellProjectileEntity(ModEntityTypes.SPELL_PROJECTILE.get(), level);
            child.setPos(origin.x + d.x * 0.35D, origin.y + d.y * 0.35D, origin.z + d.z * 0.35D);
            child.configurePayload(childSpell, 0, fallbackMateria, apparatusPotency);
            UUID targetId = (!nearbyHostiles.isEmpty() && flow != SpellFlowModifier.OCTUPLE_DOME)
                    ? nearbyHostiles.get(i % nearbyHostiles.size()).getUUID()
                    : null;
            child.configureCarrierPayload(targetId, apparatusRange);
            child.setSplitChild(true);
            child.shoot(d.x, d.y, d.z, flow == SpellFlowModifier.OCTUPLE_DOME ? 1.25F : 1.55F, 0.0F);
            level.addFreshEntity(child);
            if (splitVisualSlot.formPart().form() == SpellDeliveryForm.PROJECTILE && splitFormHandler != null && caster != null) {
                splitFormHandler.onProjectileSpawnedFromForm(level, child, caster, childSpell, splitVisualSlot);
            }
        }
    }

    /**
     * Guide Section 3.5 Ground Collision Fallback:
     * Returns true when an {@link SpellTriggerType#ON_ENTITY_HIT} spell is aimed at living targets and should suffer
     * the 25% potency penalty (75% potency) upon accidentally striking world blocks instead of an entity.
     */
    public static boolean shouldPenalizeEntityTriggerBlockFallback(SpellAssembly spell) {
        if (spell.prefix() == SpellPrefix.UTILITY) {
            return false;
        }
        for (SubSpellSlot s : spell.subSpells()) {
            SpellCoreEffect eff = s.primaryEffect().effect();
            switch (eff) {
                case HARM, AFFLICTION, KINETIC, WARD, RESTORATION -> {
                    return true;
                }
                default -> {
                }
            }
        }
        return false;
    }

    private static List<LivingEntity> findForwardHostileTargets(
            ServerLevel level,
            LivingEntity caster,
            double range,
            SpellAssembly spell
    ) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle().normalize();
        List<LivingEntity> found = level.getEntitiesOfClass(
                LivingEntity.class,
                caster.getBoundingBox().inflate(range),
                e -> e.isAlive()
                        && e != caster
                        && (e instanceof net.minecraft.world.entity.monster.Enemy || e == caster.getLastHurtMob())
                        && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())
                        && e.getEyePosition().subtract(eye).normalize().dot(look) >= 0.35D
        );
        found.sort(Comparator.comparingDouble(caster::distanceToSqr));
        return found;
    }

    /**
     * Point-of-Contact Multi-Cast Engine:
     * Executes ALL slotted sub-spells ({@code Single 1x} through {@code Octuple 8x}) originating directly at {@code contactPos}!
     */
    public static void executeContactMultiCast(
            ServerLevel level,
            @Nullable LivingEntity caster,
            @Nullable SpellProjectileEntity carrierProj,
            Vec3 contactPos,
            @Nullable Entity directHit,
            @Nullable BlockPos hitBlockPos,
            @Nullable Direction hitFace,
            Vec3 travelDir,
            SpellAssembly spell,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            float apparatusRange
    ) {
        List<SubSpellSlot> slots = spell.subSpells();
        if (slots.isEmpty()) {
            return;
        }
        int tier = spell.getEffectiveTier();
        float flawRangeMult = spell.flawProfile().getRangeAndRadiusMultiplier();
        BlockPos effectiveBlockPos = hitBlockPos != null ? hitBlockPos : BlockPos.containing(contactPos);
        Direction effectiveFace = hitFace != null ? hitFace : Direction.UP;
        Vec3 surfaceNormal = Vec3.atLowerCornerOf(effectiveFace.getUnitVec3i());
        Vec3 unitDir = travelDir.lengthSqr() > 1.0E-4D ? travelDir.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
        boolean silent = isSilentStealthSpell(spell, fallbackMateria);

        for (int slotIndex = 0; slotIndex < slots.size(); slotIndex++) {
            SubSpellSlot slot = slots.get(slotIndex);
            SpellDeliveryForm form = slot.formPart().form();
            EssenceType formMateria = slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            SpellMutationProfile formMutation = slot.formPart().resolveMutation(spell.materiaSpecific(), fallbackMateria);
            MateriaPartMutationHandler formHandler = SpellMutationHandlerRegistry.resolveActive(formMateria, slot.formPart().mutationEnabled());
            float formRangeMult = (formMutation.mutated() ? formMutation.radiusMultiplier() : 1.0F) * apparatusRange * flawRangeMult;

            EssenceType effPrimary = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            EssenceType vPrimary = formMateria != EssenceType.REGULAR ? formMateria : effPrimary;
            EssenceType vSecondary = slot.secondaryEffect()
                    .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                    .orElse(effPrimary != vPrimary ? effPrimary : null);

            switch (form) {
                case PROJECTILE -> {
                    if (!silent) {
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, contactPos, vPrimary, vSecondary, 1.15D, tier);
                    }
                    SpellProjectileEntity hookProj = carrierProj;
                    if (hookProj == null && formHandler != null) {
                        hookProj = caster != null
                                ? new SpellProjectileEntity(level, caster)
                                : new SpellProjectileEntity(ModEntityTypes.SPELL_PROJECTILE.get(), level);
                        hookProj.setPos(contactPos.x, contactPos.y, contactPos.z);
                        hookProj.configurePayload(spell, slotIndex, fallbackMateria, apparatusPotency);
                    }
                    if (directHit != null) {
                        deliverSubSpellOnEntity(level, caster, directHit, unitDir, spell, slot, fallbackMateria, apparatusPotency);
                        if (formHandler != null && hookProj != null) {
                            formHandler.onProjectileHitEntityFromForm(level, hookProj, caster, directHit, spell, slot);
                        }
                    } else {
                        deliverSubSpellOnBlock(level, caster, effectiveBlockPos, effectiveFace, spell, slot, fallbackMateria, apparatusPotency);
                        if (formHandler != null && hookProj != null) {
                            formHandler.onProjectileHitBlockFromForm(
                                    level, hookProj, caster,
                                    new BlockHitResult(contactPos, effectiveFace, effectiveBlockPos, false),
                                    spell, slot
                            );
                        }
                        // Near-Miss / Geometry Blast Splash within 2.2m of impact spot (75% fallback already factored into apparatusPotency when On-Entity-Hit misses!)
                        AABB nearSplash = new AABB(contactPos, contactPos).inflate(2.2D);
                        for (LivingEntity splashTarget : level.getEntitiesOfClass(LivingEntity.class, nearSplash, LivingEntity::isAlive)) {
                            if (splashTarget == caster && !slotIncludesCasterInArea(spell, slot)) continue;
                            if (splashTarget.position().distanceToSqr(contactPos) > 4.84D) continue;
                            Vec3 outDir = splashTarget.position().distanceToSqr(contactPos) > 1.0E-4D
                                    ? splashTarget.position().subtract(contactPos).normalize()
                                    : unitDir;
                            deliverSubSpellOnEntity(level, caster, splashTarget, outDir, spell, slot, fallbackMateria, apparatusPotency);
                        }
                    }
                }
                case BEAM -> {
                    boolean earthReachBonus = formMutation.mutated() && MateriaFamilyTier.isEarthLineage(formMateria);
                    float beamReachMult = (formMutation.mutated() ? formMutation.rangeMultiplier() : 1.0F) * apparatusRange * flawRangeMult;
                    double maxReach = spell.flowPart().distanceStep().resolveBaseBeamReachBlocks(
                            tier, spell.prefix() == SpellPrefix.COMBAT, earthReachBonus
                    ) * beamReachMult;
                    Vec3 beamOrigin = caster != null ? computeCasterMuzzleOrigin(caster, InteractionHand.MAIN_HAND) : contactPos;
                    Vec3 beamDir = caster != null && contactPos.distanceToSqr(beamOrigin) > 0.09D
                            ? contactPos.subtract(beamOrigin).normalize()
                            : resolveContactOutwardDirection(level, caster, contactPos, directHit, unitDir, surfaceNormal, maxReach, spell);
                    Vec3 beamEnd = beamOrigin.add(beamDir.scale(maxReach));
                    boolean wallPhasing = formMutation.mutated() && formMutation.wallPhasing() && spell.prefix() != SpellPrefix.UTILITY;
                    ClipContext.Fluid bFluidClip = (formMateria == EssenceType.WATER || !slotHasEffect(slot, SpellCoreEffect.TRANSMUTE))
                            ? ClipContext.Fluid.NONE
                            : ClipContext.Fluid.ANY;
                    BlockHitResult bHit = level.clip(new ClipContext(beamOrigin, beamEnd, ClipContext.Block.OUTLINE, bFluidClip,
                            caster != null ? caster : (directHit != null ? directHit : carrierProj)));
                    Vec3 effectiveEnd = (!wallPhasing && bHit.getType() == HitResult.Type.BLOCK) ? bHit.getLocation() : beamEnd;

                    if (!silent) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastBeam(level, beamOrigin, effectiveEnd, vPrimary, vSecondary, tier);
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, effectiveEnd, vPrimary, vSecondary, 1.05D, tier);
                    }

                    int maxTargets = 1 + (formMutation.mutated() ? formMutation.bonusPierceTargets() : 0);
                    List<LivingEntity> struck = new ArrayList<>();
                    if (directHit instanceof LivingEntity directLiv && directLiv.isAlive()) {
                        struck.add(directLiv);
                        deliverSubSpellOnEntity(level, caster, directLiv, beamDir, spell, slot, fallbackMateria, apparatusPotency);
                    }
                    if (struck.size() < maxTargets) {
                        AABB search = new AABB(beamOrigin, effectiveEnd).inflate(0.85D);
                        List<LivingEntity> candidates = new ArrayList<>();
                        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, search, LivingEntity::isAlive)) {
                            if (cand == caster || cand == directHit) continue;
                            if (cand.getBoundingBox().inflate(0.35D).clip(beamOrigin, effectiveEnd).isPresent()) {
                                candidates.add(cand);
                            }
                        }
                        candidates.sort(Comparator.comparingDouble(e -> e.distanceToSqr(beamOrigin)));
                        for (LivingEntity t : candidates) {
                            struck.add(t);
                            deliverSubSpellOnEntity(level, caster, t, beamDir, spell, slot, fallbackMateria, apparatusPotency);
                            if (struck.size() >= maxTargets) break;
                        }
                    }
                    if (bHit.getType() == HitResult.Type.BLOCK) {
                        deliverSubSpellOnBlock(level, caster, bHit.getBlockPos(), bHit.getDirection(), spell, slot, fallbackMateria, apparatusPotency);
                    } else if (directHit == null) {
                        deliverSubSpellOnBlock(level, caster, effectiveBlockPos, effectiveFace, spell, slot, fallbackMateria, apparatusPotency);
                    }
                    if (formHandler != null && caster != null) {
                        formHandler.onBeamFormCast(level, caster, beamDir, maxReach, struck, bHit, spell, slot, fallbackMateria, apparatusPotency);
                    }
                }
                case TOUCH_STRIKE -> {
                    if (caster instanceof Player player) {
                        primeCasterTouchStrike(level, player, spell, List.of(slot), fallbackMateria, apparatusPotency, apparatusRange, null);
                        break;
                    }
                    float touchPotency = apparatusPotency * 1.15F;
                    double reach = SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.TOUCH_STRIKE, tier) * formRangeMult;
                    Vec3 slashDir = directHit != null ? unitDir : resolveContactOutwardDirection(level, caster, contactPos, null, unitDir, surfaceNormal, reach, spell);
                    if (!silent) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                                level, contactPos, slashDir, reach, vPrimary, vSecondary, tier
                        );
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, contactPos, vPrimary, vSecondary, 1.15D, tier);
                    }
                    boolean radiantPurifyCarrier = slot.formPart().mutationEnabled() && vPrimary == EssenceType.RADIANT;
                    long radiantPurifyCarrierUntil = level.getGameTime() + 40L;
                    List<LivingEntity> struck = new ArrayList<>();
                    if (directHit instanceof LivingEntity directLiv && directLiv.isAlive()) {
                        if (radiantPurifyCarrier) {
                            EntityHelper.getPersistentData(directLiv).putLong(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL, radiantPurifyCarrierUntil);
                        }
                        struck.add(directLiv);
                        deliverSubSpellOnEntity(level, caster, directLiv, slashDir, spell, slot, fallbackMateria, touchPotency);
                    }
                    for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, new AABB(contactPos, contactPos).inflate(Math.max(2.5D, reach)), LivingEntity::isAlive)) {
                        if (near == caster || near == directHit) continue;
                        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(near, caster, spell.hasWardFriendlyFireSafeguard())) continue;
                        if (radiantPurifyCarrier) {
                            EntityHelper.getPersistentData(near).putLong(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL, radiantPurifyCarrierUntil);
                        }
                        float mult = struck.isEmpty() ? 1.0F : 0.50F;
                        struck.add(near);
                        deliverSubSpellOnEntity(level, caster, near, slashDir, spell, slot, fallbackMateria, touchPotency * mult);
                    }
                    if (directHit == null) {
                        deliverSubSpellOnBlock(level, caster, effectiveBlockPos, effectiveFace, spell, slot, fallbackMateria, touchPotency);
                    }
                    if (formHandler != null && caster != null && !struck.isEmpty()) {
                        formHandler.onTouchStrikeFormCast(level, caster, slashDir, struck, spell, slot, fallbackMateria, touchPotency);
                    }
                }
                case RADIAL_BURST -> {
                    double radius = SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.RADIAL_BURST, tier) * formRangeMult;
                    if (formHandler != null && (vPrimary == EssenceType.NATURE
                            || vPrimary == EssenceType.FROZEN
                            || vPrimary == EssenceType.NETHER
                            || vPrimary == EssenceType.RADIANT
                            || vPrimary == EssenceType.UMBRAL
                            || vPrimary == EssenceType.UNDEAD
                            || vPrimary == EssenceType.DUST
                            || vPrimary == EssenceType.SPORE)) {
                        radius = Math.max(radius, 6.0D * formRangeMult);
                    }
                    if (!silent) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(level, contactPos, radius, vPrimary, vSecondary, tier);
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, contactPos, vPrimary, vSecondary, radius, tier);
                    }
                    deliverRadialArea(level, caster, contactPos, radius, unitDir, spell, slot, fallbackMateria, apparatusPotency);
                    if (formHandler != null && caster != null) {
                        formHandler.onRadialBurstFormCast(level, caster, contactPos, radius, unitDir, spell, slot, fallbackMateria, apparatusPotency);
                    }
                }
                case AURA -> {
                    double radius = SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.AURA, tier) * formRangeMult;
                    if (!silent) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastWardShell(level, contactPos, Math.min(radius, 3.2D), vPrimary, vSecondary, tier);
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(level, contactPos, radius, vPrimary, vSecondary, 36, tier);
                    }
                    deliverRadialArea(level, caster, contactPos, radius, unitDir, spell, slot, fallbackMateria, apparatusPotency);
                    if (formHandler != null && caster != null && directHit == caster) {
                        formHandler.onAuraFormCast(level, caster, radius, spell, slot, fallbackMateria, apparatusPotency);
                        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
                    }
                    if (caster != null) {
                        int anchorDuration = 160 + (tier * 30);
                        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                                caster,
                                contactPos,
                                radius,
                                anchorDuration,
                                level.getGameTime(),
                                spell,
                                slot,
                                fallbackMateria,
                                apparatusPotency,
                                (srv, zone, elapsed) -> {
                                    if (elapsed > zone.maxDurationTicks) return false;
                                    if (elapsed > 0 && elapsed % 20 == 0) {
                                        LivingEntity owner = zone.resolveCaster(srv);
                                        if (owner != null) {
                                            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                                                    srv, zone.center, zone.radius, vPrimary, vSecondary, 22, tier
                                            );
                                            deliverRadialArea(
                                                    srv, owner, zone.center, zone.radius, new Vec3(0.0D, 1.0D, 0.0D),
                                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                                            );
                                        }
                                    }
                                    return true;
                                }
                        ));
                    }
                }
                case GLYPH -> {
                    double radius = SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.GLYPH, tier) * formRangeMult;
                    Vec3 glyphCenter = directHit != null
                            ? directHit.position().add(0.0D, 0.05D, 0.0D)
                            : contactPos.add(surfaceNormal.scale(0.08D));
                    boolean invisibleCorpseBloom = caster != null && formMutation.mutated() && formMateria == EssenceType.UNDEAD;
                    if (!silent && !invisibleCorpseBloom) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                                level, glyphCenter.subtract(0.0D, 0.40D, 0.0D), radius, vPrimary, vSecondary, 28, tier
                        );
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, glyphCenter, vPrimary, vSecondary, radius * 0.55D, tier);
                    }
                    if (caster != null) {
                        if (formHandler != null
                                && formHandler.onGlyphFormCast(level, caster, glyphCenter, radius, spell, slot, fallbackMateria, apparatusPotency)) {
                            break;
                        }
                        plantStandardGlyphTrap(level, caster, glyphCenter, radius, vPrimary, vSecondary, spell, slot, fallbackMateria, apparatusPotency);
                    } else {
                        deliverRadialArea(level, null, glyphCenter, radius, unitDir, spell, slot, fallbackMateria, apparatusPotency);
                    }
                }
                case CONE -> {
                    double coneRange = SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.CONE, tier) * formRangeMult;
                    if (formMutation.mutated() && formMateria == EssenceType.WATER) {
                        coneRange = Math.max(12.0D * apparatusRange * flawRangeMult, coneRange);
                    } else if (formMutation.mutated() && formMateria == EssenceType.FROZEN) {
                        coneRange = Math.max(10.0D * apparatusRange * flawRangeMult, coneRange);
                    }
                    Vec3 coneDir = resolveContactOutwardDirection(level, caster, contactPos, directHit, unitDir, surfaceNormal, coneRange, spell);
                    if (!silent) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVortexCone(
                                level, contactPos, coneDir, coneRange, vPrimary, vSecondary, tier
                        );
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                                level, contactPos.add(coneDir.scale(coneRange * 0.45D)), vPrimary, vSecondary, coneRange * 0.45D, tier
                        );
                    }
                    AABB box = new AABB(contactPos, contactPos).expandTowards(coneDir.scale(coneRange)).inflate(coneRange * 0.55D);
                    boolean radiantCleansingCone = formMutation.mutated() && formMateria == EssenceType.RADIANT;
                    List<LivingEntity> hitTargets = new ArrayList<>();
                    if (directHit instanceof LivingEntity directLiv && directLiv.isAlive()) {
                        hitTargets.add(directLiv);
                        if (!(radiantCleansingCone && caster != null
                                && ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.isFriendlyRecipientPublic(caster, directLiv)
                                && !isCasterBeneficialVerb(slot.primaryEffect().effect()))) {
                            deliverSubSpellOnEntity(level, caster, directLiv, coneDir, spell, slot, fallbackMateria, apparatusPotency);
                        }
                    }
                    for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                        if (target == caster || target == directHit) continue;
                        Vec3 toTarget = target.getEyePosition().subtract(contactPos);
                        if (toTarget.lengthSqr() < 0.25D || toTarget.normalize().dot(coneDir) >= 0.50D) {
                            hitTargets.add(target);
                            if (!(radiantCleansingCone && caster != null
                                    && ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.isFriendlyRecipientPublic(caster, target)
                                    && !isCasterBeneficialVerb(slot.primaryEffect().effect()))) {
                                deliverSubSpellOnEntity(level, caster, target, coneDir, spell, slot, fallbackMateria, apparatusPotency);
                            }
                        }
                    }
                    if (hasItemTargetingVerb(slot)) {
                        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box, ItemEntity::isAlive)) {
                            Vec3 toItem = item.position().subtract(contactPos);
                            if (toItem.lengthSqr() < 0.25D || toItem.normalize().dot(coneDir) >= 0.45D) {
                                deliverSubSpellOnEntity(level, caster, item, coneDir, spell, slot, fallbackMateria, apparatusPotency);
                            }
                        }
                    }
                    if (hasKineticVerb(slot)) {
                        sweepLooseObjectsAndProjectiles(level, caster, box, contactPos, coneDir, tier, true);
                    }
                    deliverConeBlocksFromOrigin(level, caster, contactPos, coneDir, coneRange, spell, slot, fallbackMateria, apparatusPotency);
                    if (formHandler != null && caster != null) {
                        formHandler.onConeFormCast(level, caster, coneDir, coneRange, hitTargets, spell, slot, fallbackMateria, apparatusPotency);
                    }
                }
                case SKYFALL -> {
                    Vec3 skyImpact = directHit != null ? directHit.position() : contactPos;
                    double radius = SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.SKYFALL, tier) * 0.5D * formRangeMult;
                    if (formMutation.mutated() && formMateria == EssenceType.WATER) {
                        radius = Math.max(8.0D * apparatusRange * flawRangeMult, radius);
                    }
                    if (!silent) {
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, skyImpact, vPrimary, vSecondary, radius, tier);
                    }
                    int zonesBefore = SpellLingeringZoneManager.getActiveZoneCount(level.dimension());
                    if (formHandler != null && caster != null
                            && formHandler.onSkyfallFormCast(level, caster, skyImpact, radius, spell, slot, fallbackMateria, apparatusPotency)) {
                        if (!silent && SpellLingeringZoneManager.getActiveZoneCount(level.dimension()) == zonesBefore) {
                            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastSkyfallPillar(level, skyImpact, radius, vPrimary, vSecondary, tier);
                        }
                        break;
                    }
                    deliverRadialArea(level, caster, skyImpact, radius, new Vec3(0.0D, -1.0D, 0.0D), spell, slot, fallbackMateria, apparatusPotency);
                    if (caster != null) {
                        final double sustainRad = Math.max(3.6D, radius);
                        final int sustainTicks = 140 + (tier * 12);
                        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                                caster, skyImpact, sustainRad, sustainTicks, level.getGameTime(),
                                spell, slot, fallbackMateria, apparatusPotency,
                                (srv, zone, elapsed) -> {
                                    if (elapsed > zone.maxDurationTicks) return false;
                                    if (elapsed > 0 && elapsed % 20 == 0) {
                                        LivingEntity owner = zone.resolveCaster(srv);
                                        deliverRadialArea(srv, owner, zone.center, zone.radius, new Vec3(0.0D, -1.0D, 0.0D),
                                                zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.25F);
                                    }
                                    return true;
                                }
                        ));
                    } else if (!silent) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastSkyfallPillar(level, skyImpact, radius, vPrimary, vSecondary, tier);
                    }
                }
                case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> {
                    if (caster == null) break;
                    Vec3 spawnPos = directHit != null
                            ? directHit.position().add(unitDir.scale(-0.8D))
                            : Vec3.atBottomCenterOf(effectiveBlockPos.relative(effectiveFace));
                    if (!silent) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastSummonPillar(level, spawnPos, 1.75D, vPrimary, vSecondary, tier);
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, spawnPos, vPrimary, vSecondary, 1.8D, tier);
                    }
                    SpellEidolonEntity.TriadArchetype reqArchetype = slot.formPart().resolveSummonArchetype();
                    if (formMutation.mutated()
                            && SpellEidolonEntity.isLifecycleRefreshMateria(formMateria)
                            && reqArchetype != SpellEidolonEntity.TriadArchetype.DECOY
                            && SpellEidolonEntity.tryRefreshLifecycleConstruct(level, caster, formMateria, reqArchetype, tier, spawnPos)) {
                        break;
                    }
                    SpellEidolonEntity.enforceSummonCap(level, caster, tier);
                    SpellEidolonEntity eidolon = ModEntityTypes.SPELL_EIDOLON.get().create(level, EntitySpawnReason.MOB_SUMMONED);
                    if (eidolon != null) {
                        eidolon.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                        eidolon.configureConstruct(caster, spell, slotIndex, fallbackMateria, apparatusPotency, spawnPos);
                        level.addFreshEntity(eidolon);
                        if (formHandler != null) {
                            formHandler.onSummonFormSpawned(level, caster, eidolon, spawnPos, 3.0D * formRangeMult, spell, slot);
                        }
                    }
                }
            }
        }
    }

    private static Vec3 resolveContactOutwardDirection(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 contactPos,
            @Nullable Entity directHit,
            Vec3 travelDir,
            Vec3 surfaceNormal,
            double searchRange,
            SpellAssembly spell
    ) {
        // Aim toward the nearest other hostile if one is nearby so contact-cast Beams & Cones sweep enemies intelligently!
        List<LivingEntity> nearbyHostiles = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(contactPos, contactPos).inflate(Math.min(14.0D, Math.max(5.0D, searchRange))),
                e -> e.isAlive()
                        && e != caster
                        && e != directHit
                        && (e instanceof Monster || (caster != null && e == caster.getLastHurtMob()))
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                e, caster, spell.hasWardFriendlyFireSafeguard()))
        );
        if (!nearbyHostiles.isEmpty()) {
            nearbyHostiles.sort(Comparator.comparingDouble(e -> e.distanceToSqr(contactPos)));
            Vec3 toFoe = nearbyHostiles.getFirst().getEyePosition().subtract(contactPos);
            if (toFoe.lengthSqr() > 1.0E-4D) {
                return toFoe.normalize();
            }
        }
        if (directHit == null && surfaceNormal.lengthSqr() > 0.25D && travelDir.dot(surfaceNormal) < -0.15D) {
            Vec3 reflected = travelDir.subtract(surfaceNormal.scale(2.0D * travelDir.dot(surfaceNormal)));
            if (reflected.lengthSqr() > 1.0E-4D) {
                return reflected.normalize();
            }
        }
        return travelDir;
    }

    private static void deliverConeBlocksFromOrigin(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 origin,
            Vec3 direction,
            double coneRange,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        if (slotHasVoxelSweepBlockVerb(slot)) {
            int maxDist = Math.min(slotHasEffect(slot, SpellCoreEffect.CONSTRUCT) ? 3 : 6, (int) Math.ceil(coneRange));
            BlockPos base = BlockPos.containing(origin);
            for (BlockPos p : BlockPos.betweenClosed(base.offset(-maxDist, -2, -maxDist), base.offset(maxDist, 2, maxDist))) {
                Vec3 center = Vec3.atCenterOf(p);
                double dSq = center.distanceToSqr(origin);
                if (dSq > (double) (maxDist * maxDist)) continue;
                if (dSq < 1.0D || center.subtract(origin).normalize().dot(direction) >= 0.55D) {
                    deliverSubSpellOnBlock(level, caster, p.immutable(), Direction.UP, spell, slot, fallbackMateria, apparatusPotency);
                }
            }
        } else if (slotHasSinglePointBlockVerb(slot) && !slotSelfTargetsCasterWhenNoEntityHit(spell, slot)) {
            BlockPos apex = BlockPos.containing(origin.add(direction.scale(Math.min(4.0D, coneRange * 0.5D))));
            deliverSubSpellOnBlock(level, caster, apex, Direction.UP, spell, slot, fallbackMateria, apparatusPotency);
        }
    }

    /**
     * Multi-Cast {@code CHAIN_LEAP} across adjacent hostiles: executes {@link #executeContactMultiCast} at every chained target!
     */
    public static void chainLeapMultiCastFrom(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity firstTarget,
            SpellAssembly spell,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            float apparatusRange
    ) {
        if (spell.subSpells().isEmpty()) {
            return;
        }
        SubSpellSlot firstSlot = spell.subSpells().getFirst();
        EssenceType flowMateria = spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        boolean flowMutated = spell.flowPart().mutationEnabled();
        MateriaPartMutationHandler flowHandler = SpellMutationHandlerRegistry.resolveActive(flowMateria, flowMutated);

        boolean lightningBranching = flowMutated && (flowMateria == EssenceType.LIGHTNING || flowMateria == EssenceType.ASTRAL);
        boolean entropicQuintupleChain = flowMutated && flowMateria == EssenceType.ENTROPIC;
        boolean voidIgnoreWalls = flowMutated && (flowMateria == EssenceType.VOID || flowMateria == EssenceType.ASTRAL || entropicQuintupleChain);
        boolean trinityQuadChain = flowMutated && (flowMateria == EssenceType.MIASMA || flowMateria == EssenceType.OBLIVION);

        int leaps = (trinityQuadChain || entropicQuintupleChain) ? 1 : (1 + (spell.getEffectiveTier() / 3));
        Set<UUID> visited = new HashSet<>();
        visited.add(firstTarget.getUUID());
        List<LivingEntity> frontier = List.of(firstTarget);

        for (int step = 0; step < leaps && !frontier.isEmpty(); step++) {
            List<LivingEntity> nextFrontier = new ArrayList<>();
            int branchesPerAnchor = entropicQuintupleChain ? 5 : (trinityQuadChain ? 4 : (lightningBranching ? 2 : 1));
            float hopPotencyMult = entropicQuintupleChain ? 0.70F : (trinityQuadChain ? 0.60F : 0.85F);

            for (LivingEntity anchor : frontier) {
                boolean stormRainBoost = flowMutated && flowMateria == EssenceType.STORM && level.isRaining();
                double jumpRad = entropicQuintupleChain ? 12.0D
                        : (((lightningBranching && SpellLingeringZoneManager.isEntityWetOrSoaked(anchor)) || stormRainBoost)
                        ? 18.0D : 12.0D);
                List<LivingEntity> candidates = level.getEntitiesOfClass(
                        LivingEntity.class,
                        anchor.getBoundingBox().inflate(jumpRad),
                        e -> e.isAlive()
                                && e != caster
                                && !visited.contains(e.getUUID())
                                && (voidIgnoreWalls || anchor.hasLineOfSight(e))
                                && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())
                );
                candidates.sort(Comparator.comparingDouble(anchor::distanceToSqr));

                EssenceType arcPrimary = flowMateria != EssenceType.REGULAR
                        ? flowMateria
                        : firstSlot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
                EssenceType arcSecondary = firstSlot.secondaryEffect()
                        .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                        .orElse(null);

                if (candidates.isEmpty()) {
                    if (step == 0) {
                        float concPotency = apparatusPotency * (0.25F * leaps);
                        Vec3 mid = anchor.position().add(0.0D, anchor.getBbHeight() * 0.55D, 0.0D);
                        if (!(flowMutated && flowMateria == EssenceType.UMBRAL)) {
                            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                                    level, mid, arcPrimary, arcSecondary, 1.15D, spell.getEffectiveTier()
                            );
                        }
                        executeContactMultiCast(
                                level, caster, null, mid, anchor, anchor.blockPosition(), Direction.UP,
                                caster.getLookAngle().normalize(), spell, fallbackMateria, concPotency, apparatusRange
                        );
                        return;
                    } else {
                        List<LivingEntity> bounceCandidates = level.getEntitiesOfClass(
                                LivingEntity.class,
                                anchor.getBoundingBox().inflate(jumpRad),
                                e -> e.isAlive()
                                        && e != caster
                                        && e != anchor
                                        && (voidIgnoreWalls || anchor.hasLineOfSight(e))
                                        && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())
                        );
                        bounceCandidates.sort(Comparator.comparingDouble(anchor::distanceToSqr));
                        candidates = bounceCandidates;
                    }
                }

                int branched = 0;
                for (LivingEntity next : candidates) {
                    visited.add(next.getUUID());
                    Vec3 anchorMid = anchor.position().add(0.0D, anchor.getBbHeight() * 0.55D, 0.0D);
                    Vec3 nextMid = next.position().add(0.0D, next.getBbHeight() * 0.55D, 0.0D);
                    // Section 6.9 Silent Shadow Chains: leaps along completely silent, invisible shadow threads!
                    if (!(flowMutated && flowMateria == EssenceType.UMBRAL)) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                                level, anchorMid, nextMid, arcPrimary, arcSecondary, spell.getEffectiveTier()
                        );
                    }
                    Vec3 hopDir = next.position().subtract(anchor.position()).normalize();
                    executeContactMultiCast(
                            level, caster, null, nextMid, next, next.blockPosition(), Direction.UP,
                            hopDir, spell, fallbackMateria, apparatusPotency * hopPotencyMult, apparatusRange
                    );
                    if (flowHandler != null) {
                        flowHandler.onChainLeapStep(level, caster, anchor, next, spell, firstSlot);
                    }
                    nextFrontier.add(next);
                    if (++branched >= branchesPerAnchor) break;
                }
            }
            frontier = nextFrontier;
        }
    }

    private static void dispatchSingleSubSpellForm(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            SpellAssembly spell,
            int slotIndex,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            float apparatusRange
    ) {
        SpellDeliveryForm form = slot.formPart().form();
        EssenceType formMateria = slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        SpellMutationProfile formMutation = slot.formPart().resolveMutation(spell.materiaSpecific(), fallbackMateria);
        MateriaPartMutationHandler formHandler = SpellMutationHandlerRegistry.resolveActive(formMateria, slot.formPart().mutationEnabled());
        SpellMutationProfile flowMutation = spell.flowPart().resolveMutation(spell.materiaSpecific(), fallbackMateria);
        int tier = spell.getEffectiveTier();
        float flawRangeMult = spell.flawProfile().getRangeAndRadiusMultiplier();
        float formRangeMult = (formMutation.mutated() ? formMutation.radiusMultiplier() : 1.0F) * apparatusRange * flawRangeMult;

        if (apparatusPotency == 1.0F && form == SpellDeliveryForm.TOUCH_STRIKE) {
            apparatusPotency *= 1.15F;
        }

        if (tryDispatchDirectBlinkSlot(
                level, caster, direction, spell, slotIndex, slot,
                fallbackMateria, apparatusPotency, apparatusRange, formRangeMult, formMutation, formHandler, flowMutation)) {
            return;
        }

        switch (form) {
            case PROJECTILE -> {
                // Immediately grant Arcane Wings & 3D Flight to the caster when launching a FLIGHT projectile,
                // while still firing the bolt so allies struck downrange can also receive wings!
                if (slotHasEffect(slot, SpellCoreEffect.FLIGHT)) {
                    deliverFlightOnlyToCaster(level, caster, direction, spell, slot, fallbackMateria, apparatusPotency);
                }

                SpellProjectileEntity proj = new SpellProjectileEntity(level, caster);
                proj.setPos(caster.getX() + direction.x * 0.6D, caster.getEyeY() - 0.15D + direction.y * 0.6D, caster.getZ() + direction.z * 0.6D);
                proj.configurePayload(spell, slotIndex, fallbackMateria, apparatusPotency);
                proj.setApparatusRangeMult(apparatusRange);

                float baseSpeed = switch (spell.flowPart().flow()) {
                    case FAST_DART -> 2.35F;
                    case SLOW_HEAVY -> 0.95F;
                    default -> 1.65F;
                };
                float speed = baseSpeed
                        * (flowMutation.mutated() ? flowMutation.velocityMultiplier() : 1.0F)
                        * (formMutation.mutated() ? formMutation.velocityMultiplier() : 1.0F);
                proj.shoot(direction.x, direction.y, direction.z, speed, 0.0F);
                level.addFreshEntity(proj);
                if (formHandler != null) {
                    formHandler.onProjectileSpawnedFromForm(level, proj, caster, spell, slot);
                }
            }
            case BEAM -> {
                boolean earthReachBonus = formMutation.mutated() && MateriaFamilyTier.isEarthLineage(formMateria);
                float beamReachMult = (formMutation.mutated() ? formMutation.rangeMultiplier() : 1.0F) * apparatusRange * flawRangeMult;
                double maxReach = spell.flowPart().distanceStep().resolveBaseBeamReachBlocks(
                        tier, spell.prefix() == SpellPrefix.COMBAT, earthReachBonus
                ) * beamReachMult;

                castRayOrBeam(level, caster, direction, maxReach, spell, slot, fallbackMateria, apparatusPotency, formMutation, formHandler);
            }
            case TOUCH_STRIKE -> {
                if (caster instanceof Player player) {
                    primeCasterTouchStrike(level, player, spell, List.of(slot), fallbackMateria, apparatusPotency, apparatusRange, null);
                } else {
                    double maxReach = SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.TOUCH_STRIKE, tier) * formRangeMult;
                    castRayOrBeam(level, caster, direction, maxReach, spell, slot, fallbackMateria, apparatusPotency, formMutation, formHandler);
                }
            }
            case RADIAL_BURST, AURA, GLYPH -> {
                double radius = SpellDeliveryForm.getCombatRadiusBlocks(form, tier) * formRangeMult;
                if (form == SpellDeliveryForm.RADIAL_BURST && formMutation.mutated()
                        && (formMateria == EssenceType.NATURE
                        || formMateria == EssenceType.FROZEN
                        || formMateria == EssenceType.NETHER
                        || formMateria == EssenceType.RADIANT
                        || formMateria == EssenceType.UMBRAL
                        || formMateria == EssenceType.UNDEAD
                        || formMateria == EssenceType.DUST
                        || formMateria == EssenceType.SPORE)) {
                    radius = Math.max(radius, 6.0D * formRangeMult);
                }
                if (form == SpellDeliveryForm.GLYPH && formMutation.mutated() && formMateria == EssenceType.NATURE) {
                    radius = Math.max(radius, 4.0D * formRangeMult);
                }
                Vec3 center = caster.position();
                if (spell.flowPart().isVoidWarpAuthorized(spell.materiaSpecific(), fallbackMateria) || form == SpellDeliveryForm.GLYPH) {
                    double glyphReach = Math.max(1.5D, spell.flowPart().travelDistanceMeters()) * apparatusRange * flawRangeMult;
                    BlockHitResult hit = raycastBlocks(level, caster, direction, glyphReach);
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        center = Vec3.atCenterOf(hit.getBlockPos().relative(hit.getDirection()));
                    }
                }
                EssenceType effPrimary = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
                EssenceType vPrimary = formMateria != EssenceType.REGULAR ? formMateria : effPrimary;
                EssenceType vSecondary = slot.secondaryEffect()
                        .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                        .orElse(effPrimary != vPrimary ? effPrimary : null);

                boolean invisibleCorpseBloom = form == SpellDeliveryForm.GLYPH
                        && formMutation.mutated()
                        && formMateria == EssenceType.UNDEAD;
                if (!isSilentStealthSpell(spell, fallbackMateria) && !invisibleCorpseBloom) {
                    if (form == SpellDeliveryForm.RADIAL_BURST) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(level, center, radius, vPrimary, vSecondary, tier);
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, center, vPrimary, vSecondary, radius, tier);
                    } else if (form == SpellDeliveryForm.AURA) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastWardShell(level, center, Math.min(radius, 3.2D), vPrimary, vSecondary, tier);
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(level, center, radius, vPrimary, vSecondary, 36, tier);
                    } else {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(level, center.subtract(0.0D, 0.45D, 0.0D), radius, vPrimary, vSecondary, 28, tier);
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, center, vPrimary, vSecondary, radius * 0.55D, tier);
                    }
                }

                if (form == SpellDeliveryForm.GLYPH) {
                    if (formHandler != null
                            && formHandler.onGlyphFormCast(level, caster, center, radius, spell, slot, fallbackMateria, apparatusPotency)) {
                        break;
                    }
                    plantStandardGlyphTrap(level, caster, center, radius, vPrimary, vSecondary, spell, slot, fallbackMateria, apparatusPotency);
                    break;
                }

                deliverRadialArea(level, caster, center, radius, direction, spell, slot, fallbackMateria, apparatusPotency);
                if (formHandler != null) {
                    if (form == SpellDeliveryForm.RADIAL_BURST) {
                        formHandler.onRadialBurstFormCast(level, caster, center, radius, direction, spell, slot, fallbackMateria, apparatusPotency);
                    } else if (form == SpellDeliveryForm.AURA) {
                        formHandler.onAuraFormCast(level, caster, radius, spell, slot, fallbackMateria, apparatusPotency);
                        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
                    }
                }

                // Section 4.5 Anchored Aura Option: sneaking while casting AURA anchors a stationary pulsing field!
                if (form == SpellDeliveryForm.AURA && caster.isShiftKeyDown()) {
                    int anchorDuration = 160 + (tier * 30);
                    Vec3 anchoredCenter = center;
                    SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                            caster,
                            anchoredCenter,
                            radius,
                            anchorDuration,
                            level.getGameTime(),
                            spell,
                            slot,
                            fallbackMateria,
                            apparatusPotency,
                            (srv, zone, elapsed) -> {
                                if (elapsed > zone.maxDurationTicks) return false;
                                if (elapsed > 0 && elapsed % 20 == 0) {
                                    LivingEntity owner = zone.resolveCaster(srv);
                                    if (owner != null) {
                                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                                                srv, zone.center, zone.radius, vPrimary, vSecondary, 22, tier
                                        );
                                        deliverRadialArea(
                                                srv, owner, zone.center, zone.radius, new Vec3(0.0D, 1.0D, 0.0D),
                                                zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                                        );
                                        if (formMutation.mutated() && formMateria == EssenceType.FROZEN) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 2.5D, zone.center.z + zone.radius);
                                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ally == owner || ally.isAlliedTo(owner)) {
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ally);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL, Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)));
                                                    aTag.putInt("SpellFrozenSpikesTier", tier);
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ally, 0.0F, null);
                                                }
                                            }
                                        } else if (formMutation.mutated() && formMateria == EssenceType.NETHER) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 2.5D, zone.center.z + zone.radius);
                                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ally == owner || ally.isAlliedTo(owner)) {
                                                    ally.clearFire();
                                                    ally.setTicksFrozen(0);
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ally);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL,
                                                            Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)));
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ally, 0.0F, null);
                                                }
                                            }
                                            ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler
                                                    .meltIceAndSnowInRadius(srv, BlockPos.containing(zone.center), (int) Math.ceil(zone.radius));
                                        } else if (formMutation.mutated() && formMateria == EssenceType.RADIANT) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 2.5D, zone.center.z + zone.radius);
                                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ally == owner || ally.isAlliedTo(owner)) {
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ally);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_RADIANT_SHROUD_UNTIL,
                                                            Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_RADIANT_SHROUD_UNTIL).orElse(0L)));
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ally, 0.0F, null);
                                                }
                                            }
                                        } else if (formMutation.mutated() && formMateria == EssenceType.UMBRAL) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 2.5D, zone.center.z + zone.radius);
                                            for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ent == owner || ent.isAlliedTo(owner)) {
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ent);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL,
                                                            Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)));
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ent, 0.0F, null);
                                                } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                                        ent, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                                                    ent.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, true, false));
                                                }
                                            }
                                        } else if (formMutation.mutated() && formMateria == EssenceType.UNDEAD) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 2.5D, zone.center.z + zone.radius);
                                            for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ent == owner || ent.isAlliedTo(owner)) {
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ent);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL,
                                                            Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL).orElse(0L)));
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ent, 0.0F, null);
                                                } else if (!ent.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)
                                                        && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                                                ent, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())
                                                        && (ent instanceof net.minecraft.world.entity.monster.Enemy
                                                            || (ent instanceof Mob m && m.getTarget() == owner))) {
                                                    SpellCombatReactionEngine.hurtWithPureMateria(
                                                            srv, ent, srv.damageSources().indirectMagic(owner, owner), 1.0F
                                                    );
                                                }
                                            }
                                        } else if (formMutation.mutated() && formMateria == EssenceType.VOID) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 3.0D, zone.center.z + zone.radius);
                                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ally == owner || ally.isAlliedTo(owner)) {
                                                    ally.resetFallDistance();
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ally);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL,
                                                            Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L)));
                                                    ally.addEffect(new MobEffectInstance(
                                                            net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(
                                                                    ddraig.net.entropica.registry.ModEffects.WEIGHTLESSNESS.get()),
                                                            35, 0, true, false));
                                                    ally.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 35, 2, true, false));
                                                    ally.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 35, 0, true, false));
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ally, 0.0F, null);
                                                }
                                            }
                                        } else if (formMutation.mutated() && formMateria == EssenceType.ARID) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 2.8D, zone.center.z + zone.radius);
                                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ally == owner || ally.isAlliedTo(owner)) {
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ally);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_ARID_VEIL_UNTIL,
                                                            Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_ARID_VEIL_UNTIL).orElse(0L)));
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ally, 0.0F, null);
                                                }
                                            }
                                        } else if (formMutation.mutated() && formMateria == EssenceType.LIGHTNING) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 2.8D, zone.center.z + zone.radius);
                                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ally == owner || ally.isAlliedTo(owner)) {
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ally);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_LIGHTNING_SHROUD_UNTIL,
                                                            Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_LIGHTNING_SHROUD_UNTIL).orElse(0L)));
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ally, 0.0F, null);
                                                }
                                            }
                                        } else if (formMutation.mutated() && formMateria == EssenceType.MAGMA) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 2.8D, zone.center.z + zone.radius);
                                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ally == owner || ally.isAlliedTo(owner)) {
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ally);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL,
                                                            Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)));
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ally, 0.0F, null);
                                                }
                                            }
                                        } else if (formMutation.mutated() && formMateria == EssenceType.STORM) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 2.8D, zone.center.z + zone.radius);
                                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ally == owner || ally.isAlliedTo(owner)) {
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ally);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_STORM_SHROUD_UNTIL,
                                                            Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_STORM_SHROUD_UNTIL).orElse(0L)));
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ally, 0.0F, null);
                                                }
                                            }
                                        } else if (formMutation.mutated() && formMateria == EssenceType.GLACIAL) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 2.8D, zone.center.z + zone.radius);
                                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ally == owner || ally.isAlliedTo(owner)) {
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ally);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL,
                                                            Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL).orElse(0L)));
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ally, 0.0F, null);
                                                }
                                            }
                                        } else if (formMutation.mutated() && formMateria == EssenceType.OVERGROWTH) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 2.8D, zone.center.z + zone.radius);
                                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ally == owner || ally.isAlliedTo(owner)) {
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ally);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BASTION_UNTIL,
                                                            Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BASTION_UNTIL).orElse(0L)));
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ally, 0.0F, null);
                                                }
                                            }
                                        } else if (formMutation.mutated() && formMateria == EssenceType.DUST) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 2.8D, zone.center.z + zone.radius);
                                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ally == owner || ally.isAlliedTo(owner)) {
                                                    ally.clearFire();
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ally);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_DUST_VEIL_UNTIL,
                                                            Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_DUST_VEIL_UNTIL).orElse(0L)));
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ally, 0.0F, null);
                                                }
                                            }
                                            ddraig.net.entropica.spell.mutation.element.DustSpellMutationHandler
                                                    .snuffTorchesAndFiresPublic(srv, BlockPos.containing(zone.center), (int) Math.max(3, Math.ceil(zone.radius)));
                                        } else if (formMutation.mutated() && formMateria == EssenceType.SPORE) {
                                            long zNow = srv.getGameTime();
                                            AABB allyBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                                    zone.center.x + zone.radius, zone.center.y + 2.8D, zone.center.z + zone.radius);
                                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
                                                if (ally == owner || ally.isAlliedTo(owner)) {
                                                    ally.removeEffect(MobEffects.POISON);
                                                    ally.removeEffect(MobEffects.NAUSEA);
                                                    CompoundTag aTag = EntityHelper.getPersistentData(ally);
                                                    aTag.putLong(SpellLingeringZoneManager.TAG_SPORE_MANTLE_UNTIL,
                                                            Math.max(zNow + 30L, aTag.getLong(SpellLingeringZoneManager.TAG_SPORE_MANTLE_UNTIL).orElse(0L)));
                                                    SpellCombatReactionEngine.syncEntityBarrierState(srv, ally, 0.0F, null);
                                                }
                                            }
                                            ddraig.net.entropica.spell.mutation.element.SporeSpellMutationHandler
                                                    .sproutMushroomsPublic(srv, BlockPos.containing(zone.center), 2, false);
                                        }
                                    }
                                }
                                return true;
                            }
                    ));
                    if (caster instanceof Player player) {
                        player.displayClientMessage(
                                Component.literal("§b✦ Aura Anchored §7— Stationary field deployed for §f" + (anchorDuration / 20) + "s§7!"),
                                true
                        );
                    }
                }
            }
            case CONE -> {
                double coneRange = SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.CONE, tier) * formRangeMult;
                if (formMutation.mutated() && formMateria == EssenceType.WATER) {
                    coneRange = Math.max(12.0D * apparatusRange * flawRangeMult, coneRange);
                } else if (formMutation.mutated() && formMateria == EssenceType.FROZEN) {
                    coneRange = Math.max(10.0D * apparatusRange * flawRangeMult, coneRange);
                }
                EssenceType effPrimary = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
                EssenceType vPrimary = formMateria != EssenceType.REGULAR ? formMateria : effPrimary;
                EssenceType vSecondary = slot.secondaryEffect()
                        .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                        .orElse(effPrimary != vPrimary ? effPrimary : null);

                boolean silentCone = isSilentStealthSpell(spell, fallbackMateria);
                if (!silentCone) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVortexCone(
                            level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), direction, coneRange, vPrimary, vSecondary, tier
                    );
                    ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                            level, caster.getEyePosition().add(direction.scale(coneRange * 0.45D)), vPrimary, vSecondary, coneRange * 0.45D, tier
                    );
                }

                boolean radiantCleansingCone = formMutation.mutated() && formMateria == EssenceType.RADIANT;
                AABB box = caster.getBoundingBox().expandTowards(direction.scale(coneRange)).inflate(coneRange * 0.55D);
                List<LivingEntity> hitTargets = new ArrayList<>();
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
                    if (target == caster || !target.isAlive()) continue;
                    Vec3 toTarget = target.getEyePosition().subtract(caster.getEyePosition()).normalize();
                    if (toTarget.dot(direction) >= 0.55D) {
                        hitTargets.add(target);
                        if (!(radiantCleansingCone
                                && ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.isFriendlyRecipientPublic(caster, target)
                                && !isCasterBeneficialVerb(slot.primaryEffect().effect()))) {
                            deliverSubSpellOnEntity(level, caster, target, direction, spell, slot, fallbackMateria, apparatusPotency);
                        }
                    }
                }
                if (hitTargets.isEmpty() && slotSelfTargetsCasterWhenNoEntityHit(spell, slot)) {
                    deliverSubSpellOnEntity(level, caster, caster, direction, spell, slot, fallbackMateria, apparatusPotency);
                }
                if (hasItemTargetingVerb(slot)) {
                    for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
                        if (!item.isAlive()) continue;
                        Vec3 toItem = item.position().subtract(caster.getEyePosition()).normalize();
                        if (toItem.dot(direction) >= 0.50D) {
                            deliverSubSpellOnEntity(level, caster, item, direction, spell, slot, fallbackMateria, apparatusPotency);
                        }
                    }
                }
                if (hasKineticVerb(slot)) {
                    sweepLooseObjectsAndProjectiles(level, caster, box, caster.getEyePosition(), direction, tier, true);
                }
                deliverConeBlocks(level, caster, direction, coneRange, spell, slot, fallbackMateria, apparatusPotency);
                if (formHandler != null) {
                    formHandler.onConeFormCast(level, caster, direction, coneRange, hitTargets, spell, slot, fallbackMateria, apparatusPotency);
                }
            }
            case SKYFALL -> {
                double skyfallReach = Math.max(1.5D, spell.flowPart().travelDistanceMeters()) * apparatusRange * flawRangeMult;
                double openAirFallback = Math.max(1.0D, spell.flowPart().travelDistanceMeters() * 0.65D) * apparatusRange * flawRangeMult;
                BlockHitResult hit = raycastBlocks(level, caster, direction, skyfallReach);
                Vec3 impact = hit.getType() == HitResult.Type.BLOCK
                        ? Vec3.atBottomCenterOf(hit.getBlockPos().above())
                        : caster.position().add(direction.scale(openAirFallback));
                double radius = SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.SKYFALL, tier) * 0.5D * formRangeMult;
                if (formMutation.mutated() && formMateria == EssenceType.WATER) {
                    radius = Math.max(8.0D * apparatusRange * flawRangeMult, radius);
                }
                EssenceType effPrimary = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
                EssenceType vPrimary = formMateria != EssenceType.REGULAR ? formMateria : effPrimary;
                EssenceType vSecondary = slot.secondaryEffect()
                        .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                        .orElse(effPrimary != vPrimary ? effPrimary : null);

                boolean silentSkyfall = isSilentStealthSpell(spell, fallbackMateria);
                if (!silentSkyfall) {
                    ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, impact, vPrimary, vSecondary, radius, tier);
                }

                int zonesBefore = SpellLingeringZoneManager.getActiveZoneCount(level.dimension());
                if (formHandler != null
                        && formHandler.onSkyfallFormCast(level, caster, impact, radius, spell, slot, fallbackMateria, apparatusPotency)) {
                    if (!silentSkyfall && SpellLingeringZoneManager.getActiveZoneCount(level.dimension()) == zonesBefore) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastSkyfallPillar(level, impact, radius, vPrimary, vSecondary, tier);
                    }
                    break;
                }
                deliverRadialArea(level, caster, impact, radius, new Vec3(0, -1, 0), spell, slot, fallbackMateria, apparatusPotency);
                final double sustainRad = Math.max(3.6D, radius);
                final int sustainTicks = 140 + (tier * 12);
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        caster, impact, sustainRad, sustainTicks, level.getGameTime(),
                        spell, slot, fallbackMateria, apparatusPotency,
                        (srv, zone, elapsed) -> {
                            if (elapsed > zone.maxDurationTicks) return false;
                            if (elapsed > 0 && elapsed % 20 == 0) {
                                LivingEntity owner = zone.resolveCaster(srv);
                                deliverRadialArea(srv, owner, zone.center, zone.radius, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.25F);
                            }
                            return true;
                        }
                ));
            }
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> {
                double summonReach = Math.max(1.85D, spell.flowPart().travelDistanceMeters() * 0.75D) * apparatusRange * flawRangeMult;
                double fallbackStep = spell.flowPart().distanceStep().isPointBlankZero()
                        ? 1.2D
                        : Math.min(6.0D, Math.max(1.75D, spell.flowPart().travelDistanceMeters() * 0.25D));
                BlockHitResult hit = raycastBlocks(level, caster, direction, summonReach);
                Vec3 spawnPos = hit.getType() == HitResult.Type.BLOCK
                        ? Vec3.atBottomCenterOf(hit.getBlockPos().relative(hit.getDirection()))
                        : caster.position().add(direction.scale(fallbackStep));

                EssenceType effPrimary = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
                EssenceType vPrimary = formMateria != EssenceType.REGULAR ? formMateria : effPrimary;
                EssenceType vSecondary = slot.secondaryEffect()
                        .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                        .orElse(effPrimary != vPrimary ? effPrimary : null);

                if (!isSilentStealthSpell(spell, fallbackMateria)) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastSummonPillar(level, spawnPos, 1.75D, vPrimary, vSecondary, tier);
                    ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, spawnPos, vPrimary, vSecondary, 1.8D, tier);
                }

                SpellEidolonEntity.TriadArchetype reqArchetype = slot.formPart().resolveSummonArchetype();
                if (formMutation.mutated()
                        && SpellEidolonEntity.isLifecycleRefreshMateria(formMateria)
                        && reqArchetype != SpellEidolonEntity.TriadArchetype.DECOY
                        && SpellEidolonEntity.tryRefreshLifecycleConstruct(level, caster, formMateria, reqArchetype, tier, spawnPos)) {
                    break;
                }

                SpellEidolonEntity.enforceSummonCap(level, caster, tier);
                SpellEidolonEntity eidolon = ModEntityTypes.SPELL_EIDOLON.get().create(level, EntitySpawnReason.MOB_SUMMONED);
                if (eidolon != null) {
                    eidolon.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                    eidolon.configureConstruct(caster, spell, slotIndex, fallbackMateria, apparatusPotency, spawnPos);
                    level.addFreshEntity(eidolon);
                    if (formHandler != null) {
                        formHandler.onSummonFormSpawned(level, caster, eidolon, spawnPos, 3.0D * formRangeMult, spell, slot);
                    }
                }
            }
        }
    }

    /**
     * Section 4.6 Baseline Glyph / Trap Persistence:
     * Arms after 1.0 second (20t), persists 10 minutes (12,000t) or indefinitely at Tier 5+,
     * can be safely disarmed via Sneak + Right-Click for a 50% Materia fuel refund, and erupts on proximity!
     */
    private static void plantStandardGlyphTrap(
            ServerLevel level,
            LivingEntity caster,
            Vec3 glyphCenter,
            double radius,
            EssenceType vPrimary,
            @Nullable EssenceType vSecondary,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        int tier = spell.getEffectiveTier();
        int maxWaitTicks = tier >= 5 ? 72_000_000 : 12_000;
        double triggerRad = Math.max(1.6D, Math.min(3.0D, radius * 0.65D));
        boolean beneficial = isCasterBeneficialVerb(slot.primaryEffect().effect());

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster,
                glyphCenter,
                radius,
                maxWaitTicks,
                level.getGameTime(),
                spell,
                slot,
                fallbackMateria,
                apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 16 == 0) {
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnZonePerimeterParticles(
                                srv, zone.center, Math.min(1.6D, zone.radius * 0.45D), vPrimary, elapsed
                        );
                    }
                    // 1.0-second (20-tick) arming delay!
                    if (elapsed < 20) {
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB triggerBox = new AABB(
                            zone.center.x - triggerRad, zone.center.y - 0.85D, zone.center.z - triggerRad,
                            zone.center.x + triggerRad, zone.center.y + 1.85D, zone.center.z + triggerRad
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, ent -> {
                        if (!ent.isAlive()) return false;
                        if (beneficial) return true;
                        if (ent == owner) return false;
                        return !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                ent, owner, zone.spell.hasWardFriendlyFireSafeguard()
                        );
                    });
                    if (stepped.isEmpty()) {
                        return true;
                    }

                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                            srv, zone.center.subtract(0.0D, 0.4D, 0.0D), zone.radius, vPrimary, vSecondary, 34, tier
                    );
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                            srv, zone.center, zone.radius, vPrimary, vSecondary, tier
                    );
                    ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                            srv, zone.center, vPrimary, vSecondary, zone.radius, tier
                    );
                    if (owner != null) {
                        deliverRadialArea(
                                srv, owner, zone.center, zone.radius, new Vec3(0.0D, 1.0D, 0.0D),
                                zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                        );
                    }
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),
                            SoundSource.PLAYERS, 0.9F, 1.25F);
                    return false;
                }
        ).markDisarmableGlyph());

        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.7F, 1.35F);
    }

    private static void castRayOrBeam(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double maxReach,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            SpellMutationProfile formMutation,
            @Nullable MateriaPartMutationHandler formHandler
    ) {
        Vec3 start = caster.getEyePosition();
        Vec3 end = start.add(direction.scale(maxReach));

        boolean voidFlowPhase = spell.flowPart().isVoidWarpAuthorized(spell.materiaSpecific(), fallbackMateria);
        boolean wallPhasing = ((formMutation.mutated() && formMutation.wallPhasing()) || voidFlowPhase)
                && spell.prefix() != SpellPrefix.UTILITY;

        EssenceType formMat = slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        ClipContext.Fluid rayFluidClip = (formMat == EssenceType.WATER || !slotHasEffect(slot, SpellCoreEffect.TRANSMUTE))
                ? ClipContext.Fluid.NONE
                : ClipContext.Fluid.ANY;
        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, rayFluidClip, caster));
        Vec3 effectiveEnd = (!wallPhasing && blockHit.getType() == HitResult.Type.BLOCK) ? blockHit.getLocation() : end;

        // Broadcast custom 3D volumetric Beam or Crescent Slash + Materia-matched custom particles!
        EssenceType effMat = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        EssenceType vPrimary = formMat != EssenceType.REGULAR ? formMat : effMat;
        EssenceType vSecondary = slot.secondaryEffect()
                .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                .orElse(effMat != vPrimary ? effMat : null);
        int tier = spell.getEffectiveTier();

        if (slot.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                    level, start.subtract(0.0D, 0.28D, 0.0D), direction, maxReach, vPrimary, vSecondary, tier
            );
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, effectiveEnd, vPrimary, vSecondary, 1.15D, tier);
        } else {
            Vec3 muzzleOrigin = start.subtract(0.0D, 0.18D, 0.0D).add(direction.scale(0.42D));
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastBeam(
                    level, muzzleOrigin, effectiveEnd, vPrimary, vSecondary, tier
            );
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, effectiveEnd, vPrimary, vSecondary, 1.10D, tier);
        }

        int maxTargets = 1 + (formMutation.mutated() ? formMutation.bonusPierceTargets() : 0);
        AABB search = new AABB(start, effectiveEnd).inflate(0.85D);
        List<LivingEntity> hitEntities = new ArrayList<>();
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, search)) {
            if (candidate == caster || !candidate.isAlive()) continue;
            if (candidate.getBoundingBox().inflate(0.35D).clip(start, effectiveEnd).isPresent()) {
                hitEntities.add(candidate);
            }
        }
        hitEntities.sort(Comparator.comparingDouble(caster::distanceToSqr));

        List<LivingEntity> struckTargets = new ArrayList<>();
        int hitCount = 0;
        EssenceType rayFlowMat = spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        boolean miasmaOrOblivionStrikeChain = slot.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                && spell.flowPart().mutationEnabled()
                && (rayFlowMat == EssenceType.MIASMA || rayFlowMat == EssenceType.OBLIVION);
        boolean radiantRayTouchPurify = slot.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                && slot.formPart().mutationEnabled()
                && slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria) == EssenceType.RADIANT;
        boolean netherRayTouchSmelt = slot.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                && slot.formPart().mutationEnabled()
                && slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria) == EssenceType.NETHER;
        boolean aridRayTouchMummify = slot.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE
                && slot.formPart().mutationEnabled()
                && slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria) == EssenceType.ARID;
        long radiantRayPurifyUntil = level.getGameTime() + 40L;
        long netherRaySmeltUntil = level.getGameTime() + 130L;
        long aridRayMummifyUntil = level.getGameTime() + 60L;
        for (LivingEntity target : hitEntities) {
            if (radiantRayTouchPurify) {
                EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL, radiantRayPurifyUntil);
            }
            if (netherRayTouchSmelt) {
                EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL, netherRaySmeltUntil);
            }
            if (aridRayTouchMummify) {
                EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_ARID_MUMMIFY_DROPS_UNTIL, aridRayMummifyUntil);
            }
            struckTargets.add(target);
            deliverSubSpellOnEntity(level, caster, target, direction, spell, slot, fallbackMateria, apparatusPotency);
            if (spell.flowPart().flow() == SpellFlowModifier.CHAIN_LEAP || miasmaOrOblivionStrikeChain) {
                chainLeapFrom(level, caster, target, spell, slot, fallbackMateria, apparatusPotency);
            }
            if (++hitCount >= maxTargets) break;
        }

        // Section 4.4 Touch / Melee Strike: 50% adjacent cleave splash on direct hit OR 30-second primed weapon infusion!
        List<LivingEntity> allRayTouchStruck = new ArrayList<>(struckTargets);
        if (slot.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE) {
            if (!struckTargets.isEmpty()) {
                Set<UUID> alreadyStruck = struckTargets.stream().map(Entity::getUUID).collect(Collectors.toSet());
                for (LivingEntity primary : struckTargets) {
                    for (LivingEntity cleaveTarget : level.getEntitiesOfClass(
                            LivingEntity.class, primary.getBoundingBox().inflate(2.5D), LivingEntity::isAlive)) {
                        if (cleaveTarget == caster || alreadyStruck.contains(cleaveTarget.getUUID())) continue;
                        if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                cleaveTarget, caster, spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }
                        alreadyStruck.add(cleaveTarget.getUUID());
                        allRayTouchStruck.add(cleaveTarget);
                        if (radiantRayTouchPurify) {
                            EntityHelper.getPersistentData(cleaveTarget).putLong(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL, radiantRayPurifyUntil);
                        }
                        if (netherRayTouchSmelt) {
                            EntityHelper.getPersistentData(cleaveTarget).putLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL, level.getGameTime() + 70L);
                        }
                        if (slot.primaryEffect().effect() == SpellCoreEffect.BLINK) {
                            slot.secondaryEffect().ifPresent(sec -> {
                                if (sec.effect() != SpellCoreEffect.BLINK) {
                                    executeSingleEffectOnEntity(level, caster, cleaveTarget, direction, spell, slot, sec,
                                            SpellPartSlotType.SECONDARY_EFFECT, fallbackMateria, apparatusPotency * 0.50F);
                                }
                            });
                            continue;
                        }
                        deliverSubSpellOnEntity(level, caster, cleaveTarget, direction, spell, slot, fallbackMateria, apparatusPotency * 0.50F);
                    }
                }
            } else if (caster instanceof Player primeOwner
                    && !slotSelfTargetsCasterWhenNoEntityHit(spell, slot)
                    && blockHit.getType() != HitResult.Type.BLOCK
                    && !hasOpenAirBlockVerb(slot)) {
                primeCasterTouchStrike(level, primeOwner, spell, List.of(slot), fallbackMateria, apparatusPotency, 1.0F, null);
            }
        }

        // Process floating ItemEntities along beam when spell uses Transmute or Logistics!
        if (hasItemTargetingVerb(slot)) {
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, search)) {
                if (item.isAlive() && item.getBoundingBox().inflate(0.45D).clip(start, effectiveEnd).isPresent()) {
                    deliverSubSpellOnEntity(level, caster, item, direction, spell, slot, fallbackMateria, apparatusPotency);
                }
            }
        }

        if (struckTargets.isEmpty() && slotSelfTargetsCasterWhenNoEntityHit(spell, slot)) {
            deliverSubSpellOnEntity(level, caster, caster, direction, spell, slot, fallbackMateria, apparatusPotency);
        }

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            deliverSubSpellOnBlock(level, caster, blockHit.getBlockPos(), blockHit.getDirection(), spell, slot, fallbackMateria, apparatusPotency);
        } else if (hasOpenAirBlockVerb(slot)) {
            deliverSubSpellOnBlock(level, caster, BlockPos.containing(effectiveEnd), Direction.UP, spell, slot, fallbackMateria, apparatusPotency);
        }

        if (formHandler != null) {
            if (slot.formPart().form() == SpellDeliveryForm.BEAM) {
                formHandler.onBeamFormCast(level, caster, direction, maxReach, struckTargets, blockHit, spell, slot, fallbackMateria, apparatusPotency);
            } else if (slot.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE && !allRayTouchStruck.isEmpty()) {
                formHandler.onTouchStrikeFormCast(level, caster, direction, allRayTouchStruck, spell, slot, fallbackMateria, apparatusPotency);
            }
        }
    }

    private static boolean isDownwardAirJetstreamCast(Player player, SpellAssembly spell, @Nullable EssenceType fallbackMateria) {
        if (player.getXRot() < 32.0F) {
            return false;
        }
        for (SubSpellSlot slot : spell.subSpells()) {
            if (slot.formPart().form() == SpellDeliveryForm.BEAM
                    && slot.formPart().mutationEnabled()
                    && slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria) == EssenceType.AIR) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsBlinkEffect(SpellAssembly spell) {
        for (SubSpellSlot slot : spell.subSpells()) {
            if (slot.primaryEffect().effect() == SpellCoreEffect.BLINK) return true;
            if (slot.secondaryEffect().map(s -> s.effect() == SpellCoreEffect.BLINK).orElse(false)) return true;
        }
        return false;
    }

    private static boolean hasItemTargetingVerb(SubSpellSlot slot) {
        SpellCoreEffect p = slot.primaryEffect().effect();
        if (p == SpellCoreEffect.TRANSMUTE || p == SpellCoreEffect.LOGISTICS) return true;
        return slot.secondaryEffect()
                .map(s -> s.effect() == SpellCoreEffect.TRANSMUTE || s.effect() == SpellCoreEffect.LOGISTICS)
                .orElse(false);
    }

    private static boolean hasKineticVerb(SubSpellSlot slot) {
        if (slot.primaryEffect().effect() == SpellCoreEffect.KINETIC) return true;
        return slot.secondaryEffect().map(s -> s.effect() == SpellCoreEffect.KINETIC).orElse(false);
    }

    public static boolean isSilentStealthSpellPublic(SpellAssembly spell, @Nullable EssenceType fallbackMateria) {
        return isSilentStealthSpell(spell, fallbackMateria);
    }

    private static boolean isSilentStealthSpell(SpellAssembly spell, @Nullable EssenceType fallbackMateria) {
        SubSpellSlot first = spell.subSpells().isEmpty() ? null : spell.subSpells().getFirst();
        if (first != null && isSilentBlinkBinding(first.primaryEffect(), spell, fallbackMateria)
                && first.secondaryEffect().map(sec -> isSilentBlinkBinding(sec, spell, fallbackMateria)).orElse(true)) {
            return true;
        }
        // Guide Section 1.7 Umbral / Silence Stealth Assassination:
        // Infusing a spell with Umbral or Silence Sub-Materia suppresses all sound and light emissions!
        EssenceType flowMat = spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        if (flowMat == EssenceType.UMBRAL || flowMat == EssenceType.NULL_U) {
            return true;
        }
        for (SubSpellSlot slot : spell.subSpells()) {
            EssenceType formMat = slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            EssenceType effMat = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            if (formMat == EssenceType.UMBRAL || formMat == EssenceType.NULL_U
                    || effMat == EssenceType.UMBRAL || effMat == EssenceType.NULL_U) {
                return true;
            }
            if (slot.formPart().mutationEnabled()
                    && slot.formPart().form() == SpellDeliveryForm.RADIAL_BURST
                    && formMat == EssenceType.ABYSS) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSilentBlinkBinding(EffectPartBinding binding, SpellAssembly spell, @Nullable EssenceType fallbackMateria) {
        if (binding.effect() != SpellCoreEffect.BLINK) {
            return false;
        }
        // Section 5.4 Clean Stealth Transit: unmutated Blink is 100% silent & smoke-free by default!
        if (!binding.mutationEnabled()) {
            return true;
        }
        EssenceType mat = binding.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        return mat == EssenceType.AIR || mat == EssenceType.WATER || mat == EssenceType.DUST || mat == EssenceType.AURA
                || mat == EssenceType.ABYSS || mat == EssenceType.NULL_U || mat == EssenceType.UMBRAL
                || mat == EssenceType.DAWN || mat == EssenceType.WRAITH || mat == EssenceType.STATIC
                || mat == EssenceType.CATACLYSM || mat == EssenceType.ESCHATON;
    }

    private static boolean isDarkOrVoidSpellAssembly(SpellAssembly spell, @Nullable EssenceType fallbackMateria) {
        EssenceType flowMat = spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        if (isDarkOrVoidMateria(flowMat)) return true;
        for (SubSpellSlot slot : spell.subSpells()) {
            if (isDarkOrVoidMateria(slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))) return true;
            if (isDarkOrVoidMateria(slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))) return true;
            if (slot.secondaryEffect().map(s -> isDarkOrVoidMateria(s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDarkOrVoidMateria(@Nullable EssenceType t) {
        if (t == null) return false;
        return switch (t) {
            case VOID, UMBRAL, UNDEAD, BLOOD, BLIGHT, ECLIPSE, NULL_U, ABYSS, WRAITH, BARROW, PENUMBRA, MIASMA, OBLIVION, ENTROPIC, CATACLYSM, ESCHATON -> true;
            default -> false;
        };
    }

    public static boolean hasOpenAirBlockVerb(SubSpellSlot slot) {
        if (isOpenAirVerb(slot.primaryEffect())) return true;
        return slot.secondaryEffect().map(SpellExecutionEngine::isOpenAirVerb).orElse(false);
    }

    private static boolean isOpenAirVerb(EffectPartBinding binding) {
        SpellCoreEffect e = binding.effect();
        if (e == SpellCoreEffect.CONJURE
                || e == SpellCoreEffect.BLINK
                || e == SpellCoreEffect.GRAVITY
                || e == SpellCoreEffect.FLIGHT
                || e == SpellCoreEffect.ATMOSPHERE
                || e == SpellCoreEffect.SONAR
                || e == SpellCoreEffect.ACOUSTIC
                || e == SpellCoreEffect.LOGISTICS
                || e == SpellCoreEffect.DIAGNOSTIC) {
            return true;
        }
        return e == SpellCoreEffect.TRANSMUTE && binding.mutationEnabled()
                && (binding.materiaType() == EssenceType.VAPOR || binding.materiaType() == EssenceType.AETHER);
    }

    /**
     * Section 5.2 Loose Object & Projectile Sweep: deflects mid-air hostile projectiles and sweeps
     * loose items, falling blocks, boats, and minecarts along the kinetic impulse vector.
     */
    private static void sweepLooseObjectsAndProjectiles(
            ServerLevel level,
            @Nullable LivingEntity caster,
            AABB searchBox,
            Vec3 origin,
            Vec3 fixedDir,
            int tier,
            boolean useConeFilter
    ) {
        double pushSpeed = 0.55D + (tier * 0.12D);
        for (Entity e : level.getEntities(caster, searchBox, ent -> ent.isAlive() && !(ent instanceof LivingEntity))) {
            if (useConeFilter) {
                Vec3 toEnt = e.position().subtract(origin).normalize();
                if (toEnt.dot(fixedDir) < 0.45D) continue;
            }
            Vec3 pushDir = useConeFilter
                    ? fixedDir
                    : (e.position().distanceToSqr(origin) > 1.0E-4D ? e.position().subtract(origin).normalize() : fixedDir);

            if (e instanceof Projectile proj) {
                if (proj.getOwner() == caster) continue;
                double deflectedSpeed = Math.max(1.25D, proj.getDeltaMovement().length() * 1.15D);
                applySyncedMotion(proj, pushDir.scale(deflectedSpeed));
            } else if (e instanceof ItemEntity
                    || e instanceof FallingBlockEntity
                    || e.getType().toShortString().contains("boat")
                    || e.getType().toShortString().contains("raft")
                    || e.getType().toShortString().contains("minecart")) {
                applySyncedMotion(e, new Vec3(pushDir.x * pushSpeed, Math.max(0.24D, pushDir.y * pushSpeed + 0.18D), pushDir.z * pushSpeed));
            }
        }
    }

    /**
     * Executes {@code CHAIN_LEAP} across adjacent hostiles, supporting Lightning's +50% wet jump radius
     * and exponential binary branching ({@code 1 -> 2 -> 4 -> 8}) as well as Void Warp wall-bypassing leaps.
     */
    public static void chainLeapFrom(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity firstTarget,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        EssenceType flowMateria = spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        boolean flowMutated = spell.flowPart().mutationEnabled();
        MateriaPartMutationHandler flowHandler = SpellMutationHandlerRegistry.resolveActive(flowMateria, flowMutated);

        boolean lightningBranching = flowMutated && (flowMateria == EssenceType.LIGHTNING || flowMateria == EssenceType.ASTRAL);
        boolean entropicQuintupleChain = flowMutated && flowMateria == EssenceType.ENTROPIC;
        boolean voidIgnoreWalls = flowMutated && (flowMateria == EssenceType.VOID || flowMateria == EssenceType.ASTRAL || entropicQuintupleChain);
        boolean trinityQuadChain = flowMutated && (flowMateria == EssenceType.MIASMA || flowMateria == EssenceType.OBLIVION);

        int leaps = (trinityQuadChain || entropicQuintupleChain) ? 1 : (1 + (spell.getEffectiveTier() / 3));
        if (lightningBranching) {
            // Section 6.13 Supercharged Forked Chains: always supports at least 2 forked leaps (1 -> 2 -> 4 targets)!
            leaps = Math.max(2, leaps);
        }
        Set<UUID> visited = new HashSet<>();
        visited.add(firstTarget.getUUID());
        List<LivingEntity> frontier = List.of(firstTarget);

        for (int step = 0; step < leaps && !frontier.isEmpty(); step++) {
            List<LivingEntity> nextFrontier = new ArrayList<>();
            int branchesPerAnchor = entropicQuintupleChain ? 5 : (trinityQuadChain ? 4 : (lightningBranching ? 2 : 1));
            float hopPotencyMult = entropicQuintupleChain ? 0.70F : (trinityQuadChain ? 0.60F : 0.85F);

            for (LivingEntity anchor : frontier) {
                boolean stormRainBoost = flowMutated && flowMateria == EssenceType.STORM && level.isRaining();
                boolean anchorWet = SpellLingeringZoneManager.isEntityWetOrSoaked(anchor);
                // Section 3.2.1 & 6.13 Chain Leap: 12-block search radius (+50% = 18m across wet mobs or water surfaces)
                double jumpRad = entropicQuintupleChain ? 12.0D
                        : ((lightningBranching || stormRainBoost) ? 18.0D : 12.0D);
                List<LivingEntity> candidates = level.getEntitiesOfClass(
                        LivingEntity.class,
                        anchor.getBoundingBox().inflate(jumpRad),
                        e -> {
                            if (!e.isAlive() || e == caster || visited.contains(e.getUUID())) return false;
                            if (!voidIgnoreWalls && !anchor.hasLineOfSight(e)) return false;
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())) {
                                return false;
                            }
                            double distSq = anchor.distanceToSqr(e);
                            if (distSq <= 144.0D || stormRainBoost) return true;
                            return lightningBranching
                                    && distSq <= 324.0D
                                    && (anchorWet
                                    || SpellLingeringZoneManager.isEntityWetOrSoaked(e)
                                    || ddraig.net.entropica.spell.mutation.element.LightningSpellMutationHandler
                                            .hasWaterAlongArc(level, anchor.position(), e.position()));
                        }
                );
                candidates.sort(Comparator.comparingDouble(anchor::distanceToSqr));

                EssenceType arcPrimary = flowMateria != EssenceType.REGULAR
                        ? flowMateria
                        : slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
                EssenceType arcSecondary = slot.secondaryEffect()
                        .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                        .orElse(null);

                // Section 3.2.1 Single-Target Concentration & Two-Target Ping-Pong Bounce:
                if (candidates.isEmpty()) {
                    if (step == 0) {
                        // Isolated single target: collapse all unused leaps inward for +25% concentrated surge per unused leap!
                        float concPotency = apparatusPotency * (0.25F * leaps);
                        Vec3 mid = anchor.position().add(0.0D, anchor.getBbHeight() * 0.55D, 0.0D);
                        if (!(flowMutated && flowMateria == EssenceType.UMBRAL)) {
                            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                                    level, mid, arcPrimary, arcSecondary, 1.15D, spell.getEffectiveTier()
                            );
                        }
                        deliverSubSpellOnEntity(
                                level, caster, anchor, caster != null ? caster.getLookAngle() : new Vec3(0, 1, 0),
                                spell, slot, fallbackMateria, concPotency
                        );
                        return;
                    } else {
                        // 2+ target ping-pong bounce: allow leaping back to a previously struck hostile != anchor!
                        List<LivingEntity> bounceCandidates = level.getEntitiesOfClass(
                                LivingEntity.class,
                                anchor.getBoundingBox().inflate(jumpRad),
                                e -> e.isAlive()
                                        && e != caster
                                        && e != anchor
                                        && (voidIgnoreWalls || anchor.hasLineOfSight(e))
                                        && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())
                        );
                        bounceCandidates.sort(Comparator.comparingDouble(anchor::distanceToSqr));
                        candidates = bounceCandidates;
                    }
                }

                int branched = 0;
                for (LivingEntity next : candidates) {
                    visited.add(next.getUUID());
                    Vec3 anchorMid = anchor.position().add(0.0D, anchor.getBbHeight() * 0.55D, 0.0D);
                    Vec3 nextMid = next.position().add(0.0D, next.getBbHeight() * 0.55D, 0.0D);
                    // Section 6.9 Silent Shadow Chains: leaps along completely silent, invisible shadow threads!
                    if (!(flowMutated && flowMateria == EssenceType.UMBRAL)) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                                level, anchorMid, nextMid, arcPrimary, arcSecondary, spell.getEffectiveTier()
                        );
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                                level, nextMid, arcPrimary, arcSecondary, 0.9D, spell.getEffectiveTier()
                        );
                    }
                    Vec3 hopDir = next.position().subtract(anchor.position()).normalize();
                    deliverSubSpellOnEntity(level, caster, next, hopDir, spell, slot, fallbackMateria, apparatusPotency * hopPotencyMult);
                    if (flowHandler != null) {
                        flowHandler.onChainLeapStep(level, caster, anchor, next, spell, slot);
                    }
                    nextFrontier.add(next);
                    if (++branched >= branchesPerAnchor) break;
                }
            }
            frontier = nextFrontier;
        }
    }

    private static void deliverRadialArea(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            double radius,
            Vec3 direction,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        level.sendParticles(ParticleTypes.ENCHANT, center.x, center.y + 0.4D, center.z, 24, radius * 0.4, 0.3, radius * 0.4, 0.12);
        AABB area = new AABB(center.x - radius, center.y - 2.0D, center.z - radius,
                center.x + radius, center.y + 2.5D, center.z + radius);

        boolean includeCaster = slotIncludesCasterInArea(spell, slot);
        boolean beneficialNoFalloff = slot.primaryEffect().effect() == SpellCoreEffect.WARD
                || slot.primaryEffect().effect() == SpellCoreEffect.RESTORATION
                || slot.primaryEffect().effect() == SpellCoreEffect.FLIGHT;
        boolean casterHitInEntityLoop = false;

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!entity.isAlive()) continue;
            double distSq = entity.position().distanceToSqr(center);
            if (distSq > radius * radius) continue;
            if (entity == caster && !includeCaster) continue;
            if (entity == caster) {
                casterHitInEntityLoop = true;
            }

            // Section 5.1 Linear Distance Falloff: 100% potency at epicenter scaling linearly down to 33% at outer rim
            float radialPotency = apparatusPotency;
            if (!beneficialNoFalloff && entity != caster) {
                double dist = Math.sqrt(distSq);
                float falloff = Math.clamp(1.0F - 0.67F * (float) (dist / Math.max(0.5D, radius)), 0.33F, 1.0F);
                radialPotency *= falloff;
            }

            Vec3 radialDir = entity == caster
                    ? direction
                    : (entity.position().distanceToSqr(center) > 1.0E-4D ? entity.position().subtract(center).normalize() : direction);
            if (entity != caster && slot.primaryEffect().effect() == SpellCoreEffect.BLINK) {
                // Never loop-blink the caster onto every surrounding mob in a radial area!
                final float secPotency = radialPotency;
                slot.secondaryEffect().ifPresent(sec -> {
                    if (sec.effect() != SpellCoreEffect.BLINK) {
                        executeSingleEffectOnEntity(level, caster, entity, radialDir, spell, slot, sec,
                                SpellPartSlotType.SECONDARY_EFFECT, fallbackMateria, secPotency);
                    }
                });
                continue;
            }
            deliverSubSpellOnEntity(level, caster, entity, radialDir, spell, slot, fallbackMateria, radialPotency);
        }

        if (hasItemTargetingVerb(slot)) {
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
                if (item.isAlive() && item.position().distanceToSqr(center) <= radius * radius) {
                    deliverSubSpellOnEntity(level, caster, item, direction, spell, slot, fallbackMateria, apparatusPotency);
                }
            }
        }

        if (hasKineticVerb(slot)) {
            sweepLooseObjectsAndProjectiles(level, caster, area, center, direction, spell.getEffectiveTier(), false);
        }

        BlockPos centerBlock = BlockPos.containing(center);
        if (slotHasVoxelSweepBlockVerb(slot)) {
            int maxSweepRad = slotHasEffect(slot, SpellCoreEffect.CONSTRUCT) ? 2 : 4;
            int blockRad = Math.min(maxSweepRad, (int) Math.ceil(radius * 0.35D));
            for (BlockPos p : BlockPos.betweenClosed(
                    centerBlock.offset(-blockRad, -1, -blockRad),
                    centerBlock.offset(blockRad, 1, blockRad))) {
                deliverSubSpellOnBlock(level, caster, p.immutable(), Direction.UP, spell, slot, fallbackMateria, apparatusPotency);
            }
        } else if (slotHasSinglePointBlockVerb(slot)) {
            // Single-origin utility/misc effects (Gravity, Atmosphere, Sonar, Logistics, Blink, Conjure, Acoustic, Diagnostic, Grapple)
            // fire cleanly ONCE at the radial center rather than 75 times across a voxel grid!
            boolean alreadyTriggeredOnCaster = casterHitInEntityLoop
                    && (slot.primaryEffect().effect() == SpellCoreEffect.FLIGHT
                    || slot.primaryEffect().effect() == SpellCoreEffect.BLINK
                    || slot.primaryEffect().effect() == SpellCoreEffect.CONJURE
                    || slot.primaryEffect().effect() == SpellCoreEffect.SONAR
                    || slot.primaryEffect().effect() == SpellCoreEffect.ATMOSPHERE
                    || slot.primaryEffect().effect() == SpellCoreEffect.LOGISTICS
                    || slot.primaryEffect().effect() == SpellCoreEffect.ACOUSTIC
                    || slot.primaryEffect().effect() == SpellCoreEffect.GRAVITY);
            if (!alreadyTriggeredOnCaster) {
                deliverSubSpellOnBlock(level, caster, centerBlock, Direction.UP, spell, slot, fallbackMateria, apparatusPotency);
            }
        }
    }

    /**
     * Executes both the Primary Core Effect AND (if present) the Dual-Verb Secondary Core Effect
     * on an impacted entity, using each effect part's own independent Materia type and mutation toggle!
     */
    public static void deliverSubSpellOnEntity(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            Vec3 direction,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        executeSingleEffectOnEntity(
                level,
                caster,
                hitEntity,
                direction,
                spell,
                slot,
                slot.primaryEffect(),
                SpellPartSlotType.CORE_EFFECT,
                fallbackMateria,
                apparatusPotency
        );

        slot.secondaryEffect().ifPresent(secondary -> executeSingleEffectOnEntity(
                level,
                caster,
                hitEntity,
                direction,
                spell,
                slot,
                secondary,
                SpellPartSlotType.SECONDARY_EFFECT,
                fallbackMateria,
                apparatusPotency
        ));
    }

    private static void executeSingleEffectOnEntity(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            Vec3 direction,
            SpellAssembly spell,
            @Nullable SubSpellSlot activeSlot,
            EffectPartBinding effectBinding,
            SpellPartSlotType slotType,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        SpellCoreEffect effect = effectBinding.effect();
        EssenceType effectMateria = effectBinding.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        SpellMutationProfile effectMutation = effectBinding.resolveMutation(slotType, spell.materiaSpecific(), fallbackMateria);
        MateriaPartMutationHandler effHandler = SpellMutationHandlerRegistry.resolveActive(effectMateria, effectBinding.mutationEnabled());
        int tier = spell.getEffectiveTier();
        String subMode = effectBinding.subMode().toLowerCase(Locale.ROOT);

        // Floating ItemEntity Transmutation & Logistics (works across Utility, Misc, and Combat casts!)
        if (hitEntity instanceof ItemEntity itemEntity && itemEntity.isAlive()) {
            if (effect == SpellCoreEffect.LOGISTICS && caster != null) {
                if (MateriaFamilyTier.isNetherLineage(effectMateria)) {
                    ItemStack refined = ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler
                            .trySmeltRawOreInTransit(level, itemEntity.getItem());
                    if (refined != itemEntity.getItem()) {
                        itemEntity.setItem(refined);
                        level.sendParticles(ParticleTypes.FLAME, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                                6, 0.14D, 0.14D, 0.14D, 0.02D);
                    }
                }
                itemEntity.setNoPickUpDelay();
                itemEntity.setPos(caster.getX(), caster.getY() + 0.5D, caster.getZ());
            } else if (effect == SpellCoreEffect.TRANSMUTE) {
                transmuteDroppedItemWithFallback(level, caster, itemEntity, tier, effectMateria, effHandler);
            }
            return;
        }

        // Enforce strict Zero-Entity-Harm safeguard for UTILITY / MISC spells (Section 3.0 & 4.0)
        // while allowing every Utility & Miscellaneous verb to function reliably!
        if (spell.prefix().enforcesZeroEntityHarm()) {
            if (hitEntity instanceof LivingEntity living) {
                switch (effect) {
                    case RESTORATION -> applySpellRestoration(
                            level, caster, living, spell, effectBinding, effectMateria, effectMutation, apparatusPotency, true
                    );
                    case WARD -> SpellCombatReactionEngine.grantWardShield(
                            living,
                            effectMateria,
                            tier,
                            effectMutation.potencyMultiplier() * spell.flawProfile().getPotencyMultiplier(),
                            160 + tier * 40
                    );
                    case FLIGHT -> {
                        boolean validRecipient = living == caster
                                || living instanceof Player
                                || (caster != null && living.isAlliedTo(caster))
                                || (living instanceof SpellEidolonEntity eid && caster != null && eid.isFriendlyTo(caster));
                        if (validRecipient) {
                            applyArcaneFlightAndWings(level, caster, living, direction, spell, effectBinding, effectMateria, effectMutation, fallbackMateria, apparatusPotency);
                        }
                    }
                    case KINETIC -> {
                        double strength = (0.55D + tier * 0.18D)
                                * effectMutation.potencyMultiplier()
                                * Math.max(0.33F, apparatusPotency)
                                * spell.flawProfile().getPotencyMultiplier();
                        if (living == caster) {
                            Vec3 jumpDir = direction.y < -0.35D ? direction.scale(-1.0D) : direction;
                            applySyncedMotion(living, new Vec3(
                                    jumpDir.x * strength,
                                    Math.max(0.48D, jumpDir.y * strength + 0.32D),
                                    jumpDir.z * strength
                            ));
                        } else if (!SpellCombatReactionEngine.isBossEntity(level, living)) {
                            // Harmless utility push (0 HP damage)
                            applySyncedMotion(living, new Vec3(
                                    direction.x * strength * 0.85D,
                                    Math.max(0.28D, direction.y * strength * 0.7D + 0.22D),
                                    direction.z * strength * 0.85D
                            ));
                        }
                    }
                    case GRAPPLE -> {
                        if (caster != null) {
                            double grappleDist = Math.max(2.0D, spell.flowPart().travelDistanceMeters())
                                    * spell.flawProfile().getRangeAndRadiusMultiplier();
                            Vec3 anchor = living == caster
                                    ? caster.position().add(caster.getLookAngle().scale(grappleDist))
                                    : living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D);
                            performGrappleTetherToPoint(level, caster, anchor, tier, effectMateria, effectMutation.potencyMultiplier());
                        }
                    }
                    case BLINK -> {
                        if (caster != null) {
                            boolean zeroStep = spell.flowPart().distanceStep().isPointBlankZero();
                            float flawScale = spell.flawProfile().getRangeAndRadiusMultiplier();
                            float rangeScale = zeroStep
                                    ? flawScale
                                    : flawScale * spell.flowPart().distanceStep().getDistanceScaleFromStandard();
                            if (subMode.equals("swap") || subMode.equals("transposition")) {
                                double swapReach = zeroStep
                                        ? 2.5D * flawScale
                                        : (tier <= 3 ? 16.0D : 16.0D + (tier - 3) * 2.5D) * rangeScale;
                                LivingEntity swapTarget = living != caster
                                        ? living
                                        : raycastBlinkSwapTarget(level, caster, caster.getLookAngle().normalize(),
                                                swapReach, tier, effectMateria, effectMutation.mutated());
                                if (swapTarget == null || !performTranspositionSwap(level, caster, swapTarget, tier, effectMateria, effectMutation.mutated(), rangeScale)) {
                                    if (spell.subSpells().size() == 1 && slotType == SpellPartSlotType.CORE_EFFECT) {
                                        BLINK_CANCELLED_REFUND_FLAG.set(true);
                                        BLINK_CANCELLED_REASON.set("§e✦ Position Swap Cancelled §7— No valid target in sights (§a100% Materia refunded§7)");
                                    }
                                }
                            } else if (living == caster) {
                                boolean blinked = zeroStep
                                        ? performPointBlankZeroBlink(level, caster, tier, effectMateria, effectMutation.mutated())
                                        : performAimedSelfBlink(level, caster, caster.getLookAngle(), tier, effectMateria, effectMutation.mutated(), rangeScale);
                                if (!blinked && spell.subSpells().size() == 1 && slotType == SpellPartSlotType.CORE_EFFECT) {
                                    BLINK_CANCELLED_REFUND_FLAG.set(true);
                                }
                            } else {
                                Vec3 stepDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
                                performSafeBlink(level, caster, living.position().add(stepDir.scale(1.6D)), tier, effectMateria, effectMutation.mutated(), rangeScale);
                            }
                        }
                    }
                    case CONJURE -> {
                        boolean isBridge = subMode.equals("bridge") || subMode.equals("platform") || subMode.equals("step")
                                || (living == caster && (living.getXRot() >= 48.0F || (!living.onGround() && living.isShiftKeyDown())));
                        BlockPos conjPos = isBridge ? living.blockPosition().below() : living.blockPosition().above();
                        materializeConjurationAt(level, caster, conjPos, spell, effectBinding, effectMateria);
                    }
                    case LOGISTICS -> executeLogisticsMagnet(level, caster, living.position(), tier, effectMateria);
                    case SONAR -> executeSonarPulse(level, caster, living.position(), tier, effectMateria);
                    case ATMOSPHERE -> executeAtmosphereShift(level, caster, living.position(), tier, effectMateria);
                    case ACOUSTIC -> executeAcousticEffect(level, caster, living, living.position(), tier, effectMateria, true);
                    case DIAGNOSTIC -> executeDiagnosticScan(level, caster, living, null, tier, effectMateria);
                    case GRAVITY -> spawnSpellGravityZone(level, caster, living.blockPosition(), tier, effectMateria, effectMutation.mutated());
                    default -> {
                    }
                }
                if (effHandler != null) {
                    effHandler.onEffectAppliedToEntity(level, caster, living, direction, effect, tier, spell);
                }
            }
            return;
        }

        if (!(hitEntity instanceof LivingEntity living)) {
            return;
        }

        switch (effect) {
            case HARM -> SpellCombatReactionEngine.applyElementalHarm(
                    level, caster, living, spell, activeSlot, fallbackMateria, effectMateria, effectMutation, apparatusPotency
            );
            case FLIGHT -> {
                boolean friendlyOrSelf = living == caster
                        || living instanceof Player
                        || (caster != null && living.isAlliedTo(caster))
                        || (living instanceof SpellEidolonEntity eid && caster != null && eid.isFriendlyTo(caster));
                if (friendlyOrSelf) {
                    applyArcaneFlightAndWings(level, caster, living, direction, spell, effectBinding, effectMateria, effectMutation, fallbackMateria, apparatusPotency);
                } else if (!SpellCombatReactionEngine.isBossEntity(level, living)
                        && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
                    // Updraft levitation lift on non-allied targets struck in Combat
                    Vec3 boost = direction.scale(0.65D + tier * 0.08D);
                    applySyncedMotion(living, new Vec3(boost.x, Math.max(0.75D, boost.y + 0.45D), boost.z));
                    living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40 + tier * 10, 0));
                }
            }
            case KINETIC, GRAPPLE -> {
                // Sneaking in Combat with GRAPPLE zips the caster toward the struck target instead of pulling them!
                if (effect == SpellCoreEffect.GRAPPLE && caster != null && living != caster && caster.isShiftKeyDown()) {
                    performGrappleTetherToPoint(
                            level,
                            caster,
                            living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D),
                            tier,
                            effectMateria,
                            effectMutation.potencyMultiplier()
                    );
                    break;
                }
                if (living != caster && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
                    return;
                }
                // Section 5.2 Boss Displacement Immunity: convert blocked displacement into kinetic blunt compensation damage!
                if (living != caster && SpellCombatReactionEngine.isBossEntity(level, living)) {
                    float bluntComp = (4.0F + tier * 2.5F)
                            * effectMutation.potencyMultiplier()
                            * Math.max(0.25F, apparatusPotency)
                            * spell.flawProfile().getPotencyMultiplier();
                    living.hurtServer(
                            level,
                            caster != null ? level.damageSources().indirectMagic(caster, caster) : level.damageSources().magic(),
                            bluntComp
                    );
                    level.sendParticles(ParticleTypes.CRIT, living.getX(), living.getY(0.6D), living.getZ(), 10, 0.4D, 0.4D, 0.4D, 0.12D);
                    break;
                }

                double strength = (0.55D + tier * 0.18D)
                        * effectMutation.potencyMultiplier()
                        * Math.max(0.33F, apparatusPotency)
                        * spell.flawProfile().getPotencyMultiplier();

                boolean defaultPull = subMode.equals("default")
                        && ((effectMutation.mutated() && MateriaFamilyTier.isVoidLineage(effectMateria)) || effect == SpellCoreEffect.GRAPPLE)
                        && caster != null && living != caster;

                if (effect == SpellCoreEffect.GRAPPLE && caster != null && living != caster) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                            level,
                            caster.position().add(0.0D, caster.getBbHeight() * 0.6D, 0.0D),
                            living.position().add(0.0D, living.getBbHeight() * 0.55D, 0.0D),
                            effectMateria,
                            null,
                            tier
                    );
                }

                if (subMode.equals("pull") || defaultPull) {
                    Vec3 pull = (caster != null && living != caster)
                            ? caster.position().subtract(living.position()).normalize().scale(strength)
                            : direction.scale(-strength);
                    applySyncedMotion(living, new Vec3(pull.x, 0.30D, pull.z));
                } else if (subMode.equals("launch")) {
                    Vec3 cur = living.getDeltaMovement();
                    applySyncedMotion(living, new Vec3(cur.x * 0.35D, 0.78D + (tier * 0.10D * effectMutation.potencyMultiplier()), cur.z * 0.35D));
                } else if (subMode.equals("slam")) {
                    Vec3 cur = living.getDeltaMovement();
                    if (living.onGround()) {
                        applySyncedMotion(living, new Vec3(cur.x * 0.25D, 0.85D, cur.z * 0.25D));
                    } else {
                        applySyncedMotion(living, new Vec3(cur.x * 0.20D, -1.55D - (tier * 0.12D), cur.z * 0.20D));
                    }
                } else if (living == caster) {
                    // Section 5.2 Self-Launch / Rocket Jump (explicitly preserves fall distance risk!)
                    Vec3 jumpDir = direction.y < -0.35D ? direction.scale(-1.0D) : direction;
                    applySyncedMotion(living, new Vec3(jumpDir.x * strength, Math.max(0.48D, jumpDir.y * strength + 0.32D), jumpDir.z * strength));
                } else {
                    applySyncedMotion(living, new Vec3(direction.x * strength, Math.max(0.32D, direction.y * strength + 0.25D), direction.z * strength));
                }
            }
            case WARD -> SpellCombatReactionEngine.grantWardShield(
                    living,
                    effectMateria,
                    tier,
                    effectMutation.potencyMultiplier() * spell.flawProfile().getPotencyMultiplier(),
                    160 + tier * 40
            );
            case BLINK -> {
                if (caster != null) {
                    boolean zeroStep = spell.flowPart().distanceStep().isPointBlankZero();
                    float flawScale = spell.flawProfile().getRangeAndRadiusMultiplier();
                    float rangeScale = zeroStep
                            ? flawScale
                            : flawScale * spell.flowPart().distanceStep().getDistanceScaleFromStandard();
                    if (subMode.equals("swap") || subMode.equals("transposition")) {
                        double swapReach = zeroStep
                                ? 2.5D * flawScale
                                : (tier <= 3 ? 16.0D : 16.0D + (tier - 3) * 2.5D) * rangeScale;
                        LivingEntity swapTarget = living != caster
                                ? living
                                : raycastBlinkSwapTarget(level, caster, caster.getLookAngle().normalize(),
                                        swapReach, tier, effectMateria, effectMutation.mutated());
                        if (swapTarget == null || !performTranspositionSwap(level, caster, swapTarget, tier, effectMateria, effectMutation.mutated(), rangeScale)) {
                            if (spell.subSpells().size() == 1 && slotType == SpellPartSlotType.CORE_EFFECT) {
                                BLINK_CANCELLED_REFUND_FLAG.set(true);
                                BLINK_CANCELLED_REASON.set("§e✦ Position Swap Cancelled §7— No valid target in sights (§a100% Materia refunded§7)");
                            }
                        }
                    } else if (living == caster) {
                        boolean blinked = zeroStep
                                ? performPointBlankZeroBlink(level, caster, tier, effectMateria, effectMutation.mutated())
                                : performAimedSelfBlink(level, caster, caster.getLookAngle(), tier, effectMateria, effectMutation.mutated(), rangeScale);
                        if (!blinked && spell.subSpells().size() == 1 && slotType == SpellPartSlotType.CORE_EFFECT) {
                            BLINK_CANCELLED_REFUND_FLAG.set(true);
                        }
                    } else {
                        Vec3 stepDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
                        performSafeBlink(level, caster, living.position().add(stepDir.scale(1.6D)), tier, effectMateria, effectMutation.mutated(), rangeScale);
                    }
                }
            }
            case AFFLICTION -> SpellCombatReactionEngine.applyPersistentHex(
                    level,
                    caster,
                    living,
                    effectMateria,
                    tier,
                    120 + tier * 40,
                    spell.hasWardFriendlyFireSafeguard(),
                    effectMutation.mutated()
            );
            case RESTORATION -> applySpellRestoration(
                    level, caster, living, spell, effectBinding, effectMateria, effectMutation, apparatusPotency, false
            );
            case CONJURE -> {
                if (living == caster) {
                    boolean isBridge = subMode.equals("bridge") || subMode.equals("platform") || subMode.equals("step")
                            || living.getXRot() >= 48.0F || (!living.onGround() && living.isShiftKeyDown());
                    BlockPos conjPos = isBridge ? living.blockPosition().below() : living.blockPosition().above();
                    materializeConjurationAt(level, caster, conjPos, spell, effectBinding, effectMateria);
                }
            }
            case LOGISTICS -> executeLogisticsMagnet(level, caster, living.position(), tier, effectMateria);
            case SONAR -> executeSonarPulse(level, caster, living.position(), tier, effectMateria);
            case ATMOSPHERE -> executeAtmosphereShift(level, caster, living.position(), tier, effectMateria);
            case ACOUSTIC -> executeAcousticEffect(level, caster, living, living.position(), tier, effectMateria, false);
            case DIAGNOSTIC -> executeDiagnosticScan(level, caster, living, null, tier, effectMateria);
            case GRAVITY -> spawnSpellGravityZone(level, caster, living.blockPosition(), tier, effectMateria, effectMutation.mutated());
            default -> {
            }
        }

        if (effHandler != null) {
            effHandler.onEffectAppliedToEntity(level, caster, living, direction, effect, tier, spell);
        }
    }

    /**
     * Executes both Primary and Secondary Core Effects on an impacted world block while strictly
     * enforcing Section 3.1.1 Chest & Container Immunity (blocks with a BlockEntity are 100% immune).
     */
    public static void deliverSubSpellOnBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            Direction hitFace,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        executeSingleEffectOnBlock(level, caster, pos, hitFace, spell, slot.primaryEffect(), SpellPartSlotType.CORE_EFFECT, fallbackMateria);
        slot.secondaryEffect().ifPresent(sec ->
                executeSingleEffectOnBlock(level, caster, pos, hitFace, spell, sec, SpellPartSlotType.SECONDARY_EFFECT, fallbackMateria));
    }

    private static void executeSingleEffectOnBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            Direction hitFace,
            SpellAssembly spell,
            EffectPartBinding effectBinding,
            SpellPartSlotType slotType,
            @Nullable EssenceType fallbackMateria
    ) {
        SpellCoreEffect effect = effectBinding.effect();
        EssenceType effectMateria = effectBinding.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        SpellMutationProfile effectMutation = effectBinding.resolveMutation(slotType, spell.materiaSpecific(), fallbackMateria);
        MateriaPartMutationHandler effHandler = SpellMutationHandlerRegistry.resolveActive(effectMateria, effectBinding.mutationEnabled());
        int tier = spell.getEffectiveTier();

        BlockState state = level.getBlockState(pos);

        switch (effect) {
            case FLIGHT -> {
                if (caster != null) {
                    applyArcaneFlightAndWings(level, caster, caster, caster.getLookAngle(), spell, effectBinding, effectMateria, effectMutation, fallbackMateria, 1.0F);
                }
            }
            case WARD -> {
                if (caster != null && caster.position().distanceToSqr(Vec3.atCenterOf(pos)) <= 20.25D) {
                    SpellCombatReactionEngine.grantWardShield(
                            caster,
                            effectMateria,
                            tier,
                            effectMutation.potencyMultiplier() * spell.flawProfile().getPotencyMultiplier(),
                            160 + tier * 40
                    );
                }
            }
            case RESTORATION -> {
                if (caster != null && caster.position().distanceToSqr(Vec3.atCenterOf(pos)) <= 20.25D) {
                    applySpellRestoration(level, caster, caster, spell, effectBinding, effectMateria, effectMutation, 1.0F, true);
                    if (effHandler != null) {
                        effHandler.onEffectAppliedToEntity(level, caster, caster, Vec3.atLowerCornerOf(hitFace.getUnitVec3i()), effect, tier, spell);
                    }
                }
            }
            case KINETIC -> {
                // Section 6.11 Void Gravity Well: aiming a Void-mutated Kinetic spell at terrain spawns a stationary 3D black hole!
                if (effectMutation.mutated() && effectMateria == EssenceType.VOID) {
                    Vec3 wellPos = state.isAir()
                            ? Vec3.atCenterOf(pos)
                            : Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(hitFace.getUnitVec3i()).scale(0.85D));
                    ddraig.net.entropica.spell.mutation.element.VoidSpellMutationHandler.spawnGravityWell(
                            level, caster, wellPos, tier, spell
                    );
                    break;
                }
                // Section 6.13 Magnetic Railgun: casting a Lightning-mutated Kinetic spell into open space / ground launches caster horizontally forward!
                if (effectMutation.mutated() && effectMateria == EssenceType.LIGHTNING && caster != null) {
                    ddraig.net.entropica.spell.mutation.element.LightningSpellMutationHandler
                            .fireMagneticRailgunCasterLaunch(level, caster, caster.getLookAngle(), tier);
                    break;
                }
                // Section 6.14 Volcanic Eruption Blast: aiming a Magma-mutated Kinetic spell at terrain erupts an upward volcanic blast launching mobs 12m airborne!
                if (effectMutation.mutated() && effectMateria == EssenceType.MAGMA) {
                    ddraig.net.entropica.spell.mutation.element.MagmaSpellMutationHandler
                            .eruptVolcanicKineticBlastAt(level, caster, Vec3.atCenterOf(pos), tier, spell);
                    break;
                }
                // Section 6.15 Hurricane Force Push: aiming a Storm-mutated Kinetic spell at terrain blasts nearby mobs 14m (2x if wet)!
                if (effectMutation.mutated() && effectMateria == EssenceType.STORM) {
                    ddraig.net.entropica.spell.mutation.element.StormSpellMutationHandler
                            .unleashHurricaneForcePushAt(level, caster, Vec3.atCenterOf(pos),
                                    caster != null ? caster.getLookAngle() : new Vec3(0.0D, 0.0D, 1.0D), tier, spell);
                    break;
                }
                // Section 6.18 Pneumatic Dust-Blast: aiming a Dust-mutated Kinetic spell at terrain unleashes a 10m blast with Armor Erosion!
                if (effectMutation.mutated() && effectMateria == EssenceType.DUST) {
                    ddraig.net.entropica.spell.mutation.element.DustSpellMutationHandler
                            .unleashPneumaticDustBlastAt(level, caster, Vec3.atCenterOf(pos),
                                    caster != null ? caster.getLookAngle() : new Vec3(0.0D, 0.0D, 1.0D), tier, spell);
                    break;
                }
                // Rocket-jumping by firing a Kinetic spell at the ground near your feet!
                if (caster != null && caster.position().distanceToSqr(Vec3.atCenterOf(pos)) <= 20.25D) {
                    double strength = (0.60D + tier * 0.18D)
                            * effectMutation.potencyMultiplier()
                            * spell.flawProfile().getPotencyMultiplier();
                    Vec3 away = caster.position().add(0.0D, 0.5D, 0.0D).subtract(Vec3.atCenterOf(pos));
                    Vec3 launch = away.lengthSqr() > 1.0E-4D ? away.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
                    applySyncedMotion(caster, new Vec3(
                            launch.x * strength * 0.85D,
                            Math.max(0.55D, launch.y * strength + 0.35D),
                            launch.z * strength * 0.85D
                    ));
                }
                sweepLooseObjectsAndProjectiles(level, caster, new AABB(pos).inflate(2.5D), Vec3.atCenterOf(pos), Vec3.atLowerCornerOf(hitFace.getUnitVec3i()), tier, false);
            }
            case GRAPPLE -> {
                if (caster != null) {
                    Vec3 anchor = state.isAir() ? Vec3.atCenterOf(pos) : Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(hitFace.getUnitVec3i()).scale(0.45D));
                    performGrappleTetherToPoint(level, caster, anchor, tier, effectMateria, effectMutation.potencyMultiplier());
                }
            }
            case BREAK -> {
                if (isProtectedBlockOrContainer(level, pos, state)) return;
                float maxHardness = 2.0F + (tier * 5.0F);
                // Section 2.2 / 2.3 / 6.6 Frozen & Glacial Lineage Excavation: guaranteed Silk Touch drops
                // AND automatic freezing of exposed adjacent water/lava leaks into Ice / Obsidian!
                if (MateriaFamilyTier.isFrozenLineage(effectMateria)) {
                    if (ddraig.net.entropica.spell.mutation.element.FrozenSpellMutationHandler
                            .harvestBlockWithSilkTouchAndSealLiquids(
                                    level, caster, pos, state, maxHardness,
                                    effectMutation.mutated(), spell, fallbackMateria)) {
                        return;
                    }
                }
                // Section 2.2 / 2.3 / 6.7 Nether & Pyre Lineage Excavation: in-world auto-smelting of broken blocks,
                // batch fuel discount, AND automatic vaporization of exposed water pockets into high-pressure steam!
                if (MateriaFamilyTier.isNetherLineage(effectMateria)) {
                    if (ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler
                            .harvestBlockWithAutoSmeltAndVaporizeWater(
                                    level, caster, pos, state, maxHardness, tier, effectMateria)) {
                        return;
                    }
                }
                // Section 2.2 / 2.3 Void-Lineage Utility Specialization (Direct-to-Inventory & Omni-Sponge):
                // Teleports all mined drops straight into the player's inventory and sponges up liquid source blocks into held buckets!
                if (MateriaFamilyTier.isVoidLineage(effectMateria)) {
                    if (ddraig.net.entropica.spell.mutation.element.VoidSpellMutationHandler
                            .harvestBlockWithDirectToInventoryAndOmniSponge(
                                    level, caster, pos, state, maxHardness, tier,
                                    effectMutation.mutated(), effHandler)) {
                        return;
                    }
                }
                boolean radiantLineageExcavate = effectMateria == EssenceType.RADIANT
                        || effectMateria == EssenceType.DAWN
                        || effectMateria == EssenceType.AURORA
                        || effectMateria == EssenceType.CELESTIAL
                        || effectMateria == EssenceType.EMPYREAN
                        || effectMateria == EssenceType.APOTHEOSIS;
                if (effHandler != null && effHandler.onTransmuteBlock(level, caster, pos, state, tier)) {
                    if (radiantLineageExcavate) {
                        ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler
                                .onRadiantLineageExcavateBlock(level, caster, pos, tier);
                    }
                    return;
                }
                float hardness = state.getDestroySpeed(level, pos);
                if (hardness >= 0.0F && hardness <= maxHardness) {
                    level.destroyBlock(pos, true, caster);
                    if (radiantLineageExcavate) {
                        ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler
                                .onRadiantLineageExcavateBlock(level, caster, pos, tier);
                    }
                }
            }
            case HARVEST -> {
                harvestOrGrowPlantBlock(level, caster, pos, state, tier, effectMateria, effectMutation.mutated());
                if (MateriaFamilyTier.isVoidLineage(effectMateria) && caster instanceof Player voidHarvester) {
                    ddraig.net.entropica.spell.mutation.element.VoidSpellMutationHandler
                            .collectFreshDropsDirectToInventory(level, voidHarvester, pos, 1.85D);
                }
            }
            case TRANSMUTE -> {
                // Also transmute any floating ItemEntities resting directly on this block!
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.25D))) {
                    if (item.isAlive()) {
                        transmuteDroppedItemWithFallback(level, caster, item, tier, effectMateria, effHandler);
                    }
                }
                // Section 2.2 Lightning / Static Lineage Machine Re-Charging: recharge placed Orbis Cells & redstone machinery safely before container guard!
                if ((effectMateria == EssenceType.LIGHTNING || effectMateria == EssenceType.STATIC)
                        && ddraig.net.entropica.spell.mutation.element.LightningSpellMutationHandler
                                .tryMachineRecharging(level, pos, effectMateria, tier)) {
                    return;
                }
                if (!state.isAir() && isProtectedBlockOrContainer(level, pos, state)) return;
                if (effHandler != null) {
                    // Magma (#13) Igneous Forging: when a ray/bolt passes through a water pool to hit the floor/wall behind it,
                    // solidify the adjacent water block directly into Obsidian / Magma Block!
                    if (effectMateria == EssenceType.MAGMA && effectMutation.mutated() && !state.is(Blocks.WATER)) {
                        BlockPos adjWater = pos.relative(hitFace);
                        BlockState adjWaterState = level.getBlockState(adjWater);
                        if (adjWaterState.is(Blocks.WATER) && effHandler.onTransmuteBlock(level, caster, adjWater, adjWaterState, tier)) {
                            return;
                        }
                    }
                    // Storm (#14) Aero-Hydraulic Erosion: when a ray/bolt passes through a lava pool to hit the basin behind it,
                    // cool the adjacent lava block directly into Obsidian!
                    if (effectMateria == EssenceType.STORM && effectMutation.mutated() && !state.is(Blocks.LAVA)) {
                        BlockPos adjLava = pos.relative(hitFace);
                        BlockState adjLavaState = level.getBlockState(adjLava);
                        if (adjLavaState.is(Blocks.LAVA) && effHandler.onTransmuteBlock(level, caster, adjLava, adjLavaState, tier)) {
                            return;
                        }
                    }
                    // Glacial (#15) Cryo-Solidification & Silk Touch: when a ray/bolt passes through a water or lava pool to hit
                    // the solid basin behind it, solidify the adjacent water into Blue/Packed Ice or cool lava into Obsidian!
                    if (effectMateria == EssenceType.GLACIAL && effectMutation.mutated() && !state.is(Blocks.WATER) && !state.is(Blocks.LAVA)) {
                        BlockPos adjFluid = pos.relative(hitFace);
                        BlockState adjFluidState = level.getBlockState(adjFluid);
                        if ((adjFluidState.is(Blocks.WATER) || adjFluidState.is(Blocks.LAVA))
                                && effHandler.onTransmuteBlock(level, caster, adjFluid, adjFluidState, tier)) {
                            return;
                        }
                    }
                    if (effHandler.onTransmuteBlock(level, caster, pos, state, tier)) {
                        return;
                    }
                    // Vapor (#29) & Aether (#48) Condensation Transmute: when striking a solid basin floor/wall, condense the adjacent air block into water!
                    if ((effectMateria == EssenceType.VAPOR || effectMateria == EssenceType.AETHER) && effectMutation.mutated() && !state.isAir()) {
                        BlockPos adjPos = pos.relative(hitFace);
                        BlockState adjState = level.getBlockState(adjPos);
                        if (adjState.isAir() && effHandler.onTransmuteBlock(level, caster, adjPos, adjState, tier)) {
                            return;
                        }
                    }
                }
                boolean blockTransmuted = false;
                if (MateriaFamilyTier.isFrozenLineage(effectMateria)) {
                    if (state.is(Blocks.WATER)) {
                        level.setBlockAndUpdate(pos, effectMutation.mutated() ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.ICE.defaultBlockState());
                        blockTransmuted = true;
                    } else if (state.is(Blocks.LAVA)) {
                        level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
                        blockTransmuted = true;
                    }
                } else if (MateriaFamilyTier.isNetherLineage(effectMateria)) {
                    if (state.is(Blocks.SAND)) {
                        level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
                        blockTransmuted = true;
                    } else if (state.is(Blocks.COBBLESTONE)) {
                        level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
                        blockTransmuted = true;
                    } else if (state.is(Blocks.CLAY)) {
                        level.setBlockAndUpdate(pos, Blocks.TERRACOTTA.defaultBlockState());
                        blockTransmuted = true;
                    } else if (state.is(Blocks.ICE) || state.is(Blocks.SNOW_BLOCK)) {
                        level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
                        blockTransmuted = true;
                    }
                } else if (effectMateria == EssenceType.NATURE || effectMateria == EssenceType.OVERGROWTH || effectMateria == EssenceType.SYLVAN) {
                    if (state.is(Blocks.COBBLESTONE)) {
                        level.setBlockAndUpdate(pos, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                        blockTransmuted = true;
                    } else if (state.is(Blocks.STONE_BRICKS)) {
                        level.setBlockAndUpdate(pos, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
                        blockTransmuted = true;
                    } else if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)) {
                        level.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
                        blockTransmuted = true;
                    }
                } else if (effectMateria == EssenceType.SPORE) {
                    if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
                        level.setBlockAndUpdate(pos, Blocks.MYCELIUM.defaultBlockState());
                        blockTransmuted = true;
                    }
                } else if (MateriaFamilyTier.isEarthLineage(effectMateria)) {
                    if (state.is(Blocks.STONE)) {
                        level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
                        blockTransmuted = true;
                    } else if (state.is(Blocks.COBBLESTONE)) {
                        level.setBlockAndUpdate(pos, Blocks.GRAVEL.defaultBlockState());
                        blockTransmuted = true;
                    } else if (state.is(Blocks.GRAVEL)) {
                        level.setBlockAndUpdate(pos, Blocks.SAND.defaultBlockState());
                        blockTransmuted = true;
                    }
                } else if (MateriaFamilyTier.isVoidLineage(effectMateria)) {
                    if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)) {
                        level.setBlockAndUpdate(pos, Blocks.DEEPSLATE.defaultBlockState());
                        blockTransmuted = true;
                    }
                }
                if (blockTransmuted) {
                    ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                            level, Vec3.atCenterOf(pos), effectMateria, null, 0.85D, tier
                    );
                }
                // Section 5.5 Invalid Organic Charring Fallback: invalid organic matter chars into Charcoal/ash
                if (!blockTransmuted && isOrganicBlock(state)) {
                    level.removeBlock(pos, false);
                    Block.popResource(level, pos, new ItemStack(Items.CHARCOAL, 1));
                    level.sendParticles(ParticleTypes.LARGE_SMOKE,
                            pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                            8, 0.25D, 0.25D, 0.25D, 0.03D);
                }
            }
            case CONSTRUCT -> {
                BlockPos targetPos = state.canBeReplaced() ? pos : pos.relative(hitFace);
                if (!level.getBlockState(targetPos).canBeReplaced()) return;
                if (!level.getEntitiesOfClass(LivingEntity.class, new AABB(targetPos)).isEmpty()) return;
                if (caster instanceof Player player && !player.getOffhandItem().isEmpty()
                        && player.getOffhandItem().getItem() instanceof BlockItem offhandBlock) {
                    BlockState placeState = offhandBlock.getBlock().defaultBlockState();
                    level.setBlockAndUpdate(targetPos, placeState);
                    if (!player.isCreative()) {
                        player.getOffhandItem().shrink(1);
                    }
                } else {
                    Block conjured = MateriaFamilyTier.isFrozenLineage(effectMateria) ? Blocks.FROSTED_ICE
                            : (MateriaFamilyTier.isNetherLineage(effectMateria) ? Blocks.BASALT
                            : (effectMateria == EssenceType.DUST ? Blocks.SANDSTONE
                            : (effectMateria == EssenceType.NATURE || effectMateria == EssenceType.OVERGROWTH ? Blocks.MOSSY_COBBLESTONE
                            : Blocks.COBBLESTONE)));
                    level.setBlockAndUpdate(targetPos, conjured.defaultBlockState());
                }
                ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                        level, Vec3.atCenterOf(targetPos), effectMateria, null, 0.65D, tier
                );
            }
            case CONJURE -> {
                BlockPos anchorPos = state.isAir() ? pos : pos.relative(hitFace);
                materializeConjurationAt(level, caster, anchorPos, spell, effectBinding, effectMateria);
            }
            case BLINK -> {
                if (caster != null) {
                    String subMode = effectBinding.subMode().toLowerCase(Locale.ROOT);
                    if (subMode.equals("swap") || subMode.equals("transposition")) {
                        // A missed Position Swap projectile striking world blocks does NOT self-blink!
                        break;
                    }
                    BlockPos safeDest = state.isAir() ? pos : pos.relative(hitFace);
                    performSafeBlink(
                            level,
                            caster,
                            Vec3.atBottomCenterOf(safeDest),
                            tier,
                            effectMateria,
                            effectMutation.mutated(),
                            spell.flawProfile().getRangeAndRadiusMultiplier()
                    );
                }
            }
            case LOGISTICS -> executeLogisticsMagnet(level, caster, Vec3.atCenterOf(pos), tier, effectMateria);
            case SONAR -> executeSonarPulse(level, caster, Vec3.atCenterOf(pos), tier, effectMateria);
            case GRAVITY -> spawnSpellGravityZone(level, caster, state.isAir() ? pos : pos.relative(hitFace), tier, effectMateria, effectMutation.mutated());
            case ATMOSPHERE -> executeAtmosphereShift(level, caster, Vec3.atCenterOf(pos), tier, effectMateria);
            case ACOUSTIC -> executeAcousticEffect(level, caster, null, Vec3.atCenterOf(pos), tier, effectMateria, spell.prefix().enforcesZeroEntityHarm());
            case DIAGNOSTIC -> executeDiagnosticScan(level, caster, null, pos, tier, effectMateria);
            default -> {
            }
        }
    }

    /**
     * Section 5.7 Restoration: 0.5x Base Combat Damage healing table, Undead Materia matchup inversion
     * (1.5x / 1.0x / 0.25x), Burst Vitae vs Mending Flow, Cleanse, 100% Eidolon summon healing,
     * and full-HP Overheal Saturation & Vitality conversion.
     */
    private static void applySpellRestoration(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity living,
            SpellAssembly spell,
            EffectPartBinding effectBinding,
            @Nullable EssenceType effectMateria,
            SpellMutationProfile effectMutation,
            float apparatusPotency,
            boolean zeroEntityHarmSafeguard
    ) {
        int tier = spell.getEffectiveTier();
        float healAmt = MateriaStageTable.getBaseRestorationHealHp(tier)
                * effectMutation.potencyMultiplier()
                * Math.max(0.25F, apparatusPotency)
                * spell.flawProfile().getPotencyMultiplier();

        boolean ownedSummon = living instanceof SpellEidolonEntity eidolon && caster != null && eidolon.isOwnedBy(caster);
        boolean alliedTarget = living == caster || ownedSummon || (caster != null && living.isAlliedTo(caster));

        // Section 5.7 Sliding Scale Undead Matchup: Combat Restoration inverts into Materia-scaled magic damage vs hostile Undead!
        if (!zeroEntityHarmSafeguard && !alliedTarget && living.getType().is(EntityTypeTags.UNDEAD)) {
            int score = (effectMateria != null && effectMateria != EssenceType.REGULAR)
                    ? VisInteractionManager.getInstance().calculateTotalVisScore(
                            List.of(effectMateria.getSerializedName()),
                            Set.of(EssenceType.UNDEAD)
                    )
                    : 0;
            float undeadMult = score > 0 ? 1.50F : (score < 0 ? 0.25F : 1.00F);
            SpellCombatReactionEngine.hurtWithPureMateria(
                    level,
                    living,
                    caster != null ? level.damageSources().indirectMagic(caster, caster) : level.damageSources().magic(),
                    healAmt * undeadMult
            );
            level.sendParticles(ParticleTypes.END_ROD, living.getX(), living.getY(0.85D), living.getZ(),
                    10, 0.3D, 0.4D, 0.3D, 0.06D);
            return;
        }

        boolean alreadyFullHp = living.getHealth() >= living.getMaxHealth() - 0.1F;
        String subMode = effectBinding.subMode().toLowerCase(Locale.ROOT);

        if (subMode.equals("cleanse")
                || subMode.equals("purify")
                || subMode.equals("default")
                || effectMateria == EssenceType.RADIANT
                || effectMateria == EssenceType.VITAE) {
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(living);
        }

        if (subMode.equals("mending") || subMode.equals("flow")) {
            float upfront = healAmt * 0.25F;
            SpellCombatReactionEngine.notifyMagicalHeal(living, upfront);
            living.heal(upfront);
            living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, Math.min(3, tier / 3)));
        } else {
            SpellCombatReactionEngine.notifyMagicalHeal(living, healAmt);
            living.heal(healAmt);
        }

        // Section 5.7 Saturation & Vitality Overheal Conversion when already at 100% HP
        if (alreadyFullHp) {
            if (living instanceof Player player) {
                player.getFoodData().eat(Math.min(6, 2 + (tier / 2)), 0.6F);
            }
            living.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 0, false, true));
            living.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, false, true));
        }

        level.sendParticles(ParticleTypes.HEART, living.getX(), living.getY(0.85D), living.getZ(), 5, 0.3D, 0.3D, 0.3D, 0.05D);
    }

    /**
     * Section 5.5 Dropped-Item Transmutation, Invalid Organic Charring Fallback, and 30% Bulk Stack Fuel Discount.
     */
    private static void transmuteDroppedItemWithFallback(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int tier,
            @Nullable EssenceType effectMateria,
            @Nullable MateriaPartMutationHandler effHandler
    ) {
        ItemStack stackBefore = itemEntity.getItem();
        if (stackBefore.isEmpty()) return;
        int countBefore = stackBefore.getCount();

        boolean transmuted = effHandler != null && effHandler.onTransmuteDroppedItem(level, caster, itemEntity, tier);
        if (!transmuted && itemEntity.isAlive() && isOrganicItem(stackBefore)) {
            itemEntity.setItem(new ItemStack(Items.CHARCOAL, countBefore));
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    6, 0.15D, 0.15D, 0.15D, 0.02D);
            transmuted = true;
        }

        // Section 5.5 30% Bulk Discount when transmuting full/bulk dropped item stacks (>= 16 items)
        if (transmuted && countBefore >= 16 && caster instanceof Player player && effectMateria != null) {
            CompoundTag pData = EntityHelper.getPersistentData(player);
            long now = level.getGameTime();
            if (pData.getLong("LastTransmuteBulkRefundTick").orElse(-10L) != now) {
                pData.putLong("LastTransmuteBulkRefundTick", now);
                long refundMfum = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.30D));
                SpellFuelService.restorePlayerMateriaFuel(player, effectMateria, refundMfum);
            }
        }
    }

    private static boolean isOrganicItem(ItemStack stack) {
        return stack.is(ItemTags.LOGS)
                || stack.is(ItemTags.LEAVES)
                || stack.is(ItemTags.FLOWERS)
                || stack.is(ItemTags.SAPLINGS)
                || stack.is(ItemTags.PLANKS)
                || stack.is(ItemTags.WOOL)
                || stack.is(Items.WHEAT)
                || stack.is(Items.PAPER)
                || stack.is(Items.SUGAR_CANE)
                || stack.is(Items.VINE)
                || stack.is(Items.SHORT_GRASS)
                || stack.is(Items.TALL_GRASS);
    }

    private static boolean isOrganicBlock(BlockState state) {
        return state.is(BlockTags.LOGS)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOOL)
                || state.is(BlockTags.CROPS);
    }

    /**
     * Section 5.8 Conjuration: Brightness Dial (7–15) invisible arcane light motes OR
     * temporary 8-second (160t) translucent stepping-stone bridges strictly in empty air.
     */
    private static void materializeConjurationAt(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos anchorPos,
            SpellAssembly spell,
            EffectPartBinding effectBinding,
            @Nullable EssenceType effectMateria
    ) {
        String subMode = effectBinding.subMode().toLowerCase(Locale.ROOT);
        boolean wantsBridge = subMode.equals("bridge")
                || subMode.equals("platform")
                || subMode.equals("step")
                || (caster != null && (caster.getXRot() >= 48.0F || (!caster.onGround() && caster.isShiftKeyDown())));
        if (wantsBridge) {
            Block platformBlock = (effectMateria == EssenceType.UMBRAL || MateriaFamilyTier.isVoidLineage(effectMateria))
                    ? Blocks.TINTED_GLASS
                    : (MateriaFamilyTier.isFrozenLineage(effectMateria) ? Blocks.ICE : Blocks.GLASS);

            List<BlockPos> placedPositions = new ArrayList<>(9);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = anchorPos.offset(dx, 0, dz).immutable();
                    if (level.getBlockState(p).isAir()) {
                        level.setBlockAndUpdate(p, platformBlock.defaultBlockState());
                        placedPositions.add(p);
                    }
                }
            }
            if (!placedPositions.isEmpty()) {
                ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                        level, Vec3.atCenterOf(anchorPos), effectMateria, null, 1.35D, spell.getEffectiveTier()
                );
                SubSpellSlot refSlot = spell.subSpells().getFirst();
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        caster,
                        Vec3.atCenterOf(anchorPos),
                        2.5D,
                        300,
                        level.getGameTime(),
                        spell,
                        refSlot,
                        effectMateria,
                        1.0F,
                        (lvl, zone, elapsed) -> {
                            if (elapsed >= zone.maxDurationTicks) {
                                for (BlockPos bp : placedPositions) {
                                    if (lvl.getBlockState(bp).is(platformBlock)) {
                                        lvl.removeBlock(bp, false);
                                        lvl.sendParticles(ParticleTypes.END_ROD,
                                                bp.getX() + 0.5D, bp.getY() + 0.5D, bp.getZ() + 0.5D,
                                                4, 0.2D, 0.2D, 0.2D, 0.02D);
                                    }
                                }
                                return false;
                            }
                            if (elapsed % 20 == 0) {
                                lvl.sendParticles(ParticleTypes.ENCHANT,
                                        zone.center.x, zone.center.y + 0.6D, zone.center.z,
                                        6, 0.9D, 0.1D, 0.9D, 0.04D);
                            }
                            return true;
                        }
                ));
            }
            return;
        }

        if (level.getBlockState(anchorPos).isAir()) {
            int brightness = Math.clamp(effectBinding.dialValue(), 7, 15);
            level.setBlockAndUpdate(anchorPos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, brightness));
            Vec3 moteCenter = Vec3.atCenterOf(anchorPos);
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                    level, moteCenter, effectMateria, null, 0.85D, spell.getEffectiveTier()
            );
            level.playSound(null, anchorPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.7F, 1.4F);
            // Keep a soft visible arcane wisp pulsing at the conjured light anchor for 12s so players can see it even in bright daylight!
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster,
                    moteCenter,
                    1.0D,
                    240,
                    level.getGameTime(),
                    null,
                    null,
                    effectMateria,
                    1.0F,
                    (lvl, zone, elapsed) -> {
                        if (!lvl.getBlockState(anchorPos).is(Blocks.LIGHT)) {
                            return false;
                        }
                        if (elapsed % 12 == 0) {
                            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                                    lvl, moteCenter, effectMateria, null, 0.35D, 1
                            );
                        }
                        return elapsed < zone.maxDurationTicks;
                    }
            ));
        }
    }

    /**
     * Section 3.1.1 Strict Container & TileEntity Safeguard:
     * Chests, Barrels, Shulker Boxes, Furnaces, Hoppers, Orbis Cells, and all BlockEntities are 100% immune
     * to spell excavation and transmutation.
     */
    public static boolean isProtectedBlockOrContainer(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir()) return true;
        if (state.getDestroySpeed(level, pos) < 0.0F) return true;
        if (level.getBlockEntity(pos) != null) return true;
        return state.hasBlockEntity();
    }

    /**
     * Direct Blink / Step & Position Swap dispatcher for {@link #dispatchSingleSubSpellForm} (Guide Section 5.4).
     * Ensures {@code Self-Blink} executes an instant eye-origin raycast step without waiting for a slow projectile
     * or multi-blinking onto nearby mobs, while {@code Position Swap} swaps spatial coordinates cleanly.
     */
    private static boolean tryDispatchDirectBlinkSlot(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            SpellAssembly spell,
            int slotIndex,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            float apparatusRange,
            float formRangeMult,
            SpellMutationProfile formMutation,
            @Nullable MateriaPartMutationHandler formHandler,
            SpellMutationProfile flowMutation
    ) {
        if (slot.primaryEffect().effect() != SpellCoreEffect.BLINK) {
            return false;
        }
        SpellDeliveryForm form = slot.formPart().form();
        if (form == SpellDeliveryForm.GLYPH || form == SpellDeliveryForm.TOUCH_STRIKE || form.isSummonForm()) {
            return false;
        }

        int tier = spell.getEffectiveTier();
        EssenceType effPrimary = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        SpellMutationProfile primMut = slot.primaryEffect().resolveMutation(SpellPartSlotType.CORE_EFFECT, spell.materiaSpecific(), fallbackMateria);
        String subMode = slot.primaryEffect().subMode().toLowerCase(Locale.ROOT);
        boolean isSwap = subMode.equals("swap") || subMode.equals("transposition");
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        boolean zeroStep = spell.flowPart().distanceStep().isPointBlankZero();
        float blinkTravelScale = zeroStep
                ? formRangeMult
                : formRangeMult * spell.flowPart().distanceStep().getDistanceScaleFromStandard();

        if (isSwap) {
            double swapReach = zeroStep
                    ? 2.5D * formRangeMult
                    : resolveBlinkMaxRange(
                            level,
                            caster.position(),
                            BlockPos.containing(caster.position().add(normDir.scale(Math.max(8.0D, spell.flowPart().travelDistanceMeters())))),
                            tier,
                            effPrimary,
                            primMut.mutated(),
                            blinkTravelScale
                    );
            LivingEntity swapTarget = raycastBlinkSwapTarget(level, caster, normDir, swapReach, tier, effPrimary, primMut.mutated());
            if (swapTarget != null) {
                boolean swapped = performTranspositionSwap(level, caster, swapTarget, tier, effPrimary, primMut.mutated(), blinkTravelScale);
                if (!swapped) {
                    if (slotIndex == 0) {
                        BLINK_CANCELLED_REFUND_FLAG.set(true);
                        BLINK_CANCELLED_REASON.set("§e✦ Position Swap Cancelled §7— Target is anchored or boss-immune (§a100% Materia refunded§7)");
                    }
                    return true;
                }
                slot.secondaryEffect().ifPresent(sec -> executeSingleEffectOnEntity(
                        level, caster, swapTarget, normDir, spell, slot, sec,
                        SpellPartSlotType.SECONDARY_EFFECT, fallbackMateria, apparatusPotency
                ));
                return true;
            }
            if (form == SpellDeliveryForm.PROJECTILE && !zeroStep) {
                // Lead moving distant targets with a fast zero-drop Transposition Bolt!
                SpellProjectileEntity proj = new SpellProjectileEntity(level, caster);
                proj.setPos(caster.getX() + normDir.x * 0.6D, caster.getEyeY() - 0.15D + normDir.y * 0.6D, caster.getZ() + normDir.z * 0.6D);
                proj.configurePayload(spell, slotIndex, fallbackMateria, apparatusPotency);
                proj.setApparatusRangeMult(apparatusRange);
                proj.setNoGravity(true);
                float speed = 2.35F
                        * (flowMutation.mutated() ? flowMutation.velocityMultiplier() : 1.0F)
                        * (formMutation.mutated() ? formMutation.velocityMultiplier() : 1.0F);
                proj.shoot(normDir.x, normDir.y, normDir.z, speed, 0.0F);
                level.addFreshEntity(proj);
                if (formHandler != null) {
                    formHandler.onProjectileSpawnedFromForm(level, proj, caster, spell, slot);
                }
                return true;
            }
            if (slotIndex == 0) {
                BLINK_CANCELLED_REFUND_FLAG.set(true);
                BLINK_CANCELLED_REASON.set("§e✦ Position Swap Cancelled §7— No valid target in sights (§a100% Materia refunded§7)");
            }
            return true;
        }

        // Self-Blink: instant spatial relocation scaled by Outer Rim Travel Distance (0m .. 128m)!
        boolean blinked;
        if (zeroStep) {
            blinked = performPointBlankZeroBlink(level, caster, tier, effPrimary, primMut.mutated());
        } else if (form == SpellDeliveryForm.SKYFALL) {
            BlockHitResult hit = raycastBlocks(level, caster, normDir, 24.0D * blinkTravelScale);
            Vec3 impact = hit.getType() == HitResult.Type.BLOCK
                    ? Vec3.atBottomCenterOf(hit.getBlockPos().above())
                    : caster.getEyePosition().add(normDir.scale(16.0D * blinkTravelScale));
            blinked = performSafeBlink(level, caster, impact, tier, effPrimary, primMut.mutated(), blinkTravelScale);
        } else {
            blinked = performAimedSelfBlink(level, caster, normDir, tier, effPrimary, primMut.mutated(), blinkTravelScale);
        }

        if (!blinked) {
            if (slotIndex == 0) {
                BLINK_CANCELLED_REFUND_FLAG.set(true);
            }
            return true;
        }

        Vec3 arrivalCenter = caster.position();
        double arrivalRad = Math.max(
                3.2D,
                SpellDeliveryForm.getCombatRadiusBlocks(
                        (form == SpellDeliveryForm.PROJECTILE || form == SpellDeliveryForm.BEAM)
                                ? SpellDeliveryForm.RADIAL_BURST
                                : form,
                        tier
                ) * formRangeMult
        );

        // Hybridized Dual-Verb arrival payload (e.g., Blink + Harm = Telefrag Strike, Blink + Ward, Blink + Kinetic)
        if (slot.secondaryEffect().isPresent()) {
            EffectPartBinding sec = slot.secondaryEffect().get();
            EssenceType secMat = sec.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            if (isCasterBeneficialVerb(sec.effect())) {
                executeSingleEffectOnEntity(
                        level, caster, caster, normDir, spell, slot, sec,
                        SpellPartSlotType.SECONDARY_EFFECT, fallbackMateria, apparatusPotency
                );
            } else {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(level, arrivalCenter, arrivalRad, secMat, effPrimary, tier);
                ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, arrivalCenter, secMat, effPrimary, arrivalRad * 0.85D, tier);
                AABB arrivalBox = new AABB(
                        arrivalCenter.x - arrivalRad, arrivalCenter.y - 1.5D, arrivalCenter.z - arrivalRad,
                        arrivalCenter.x + arrivalRad, arrivalCenter.y + 2.2D, arrivalCenter.z + arrivalRad
                );
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, arrivalBox, LivingEntity::isAlive)) {
                    if (foe == caster) continue;
                    double dSq = foe.position().distanceToSqr(arrivalCenter);
                    if (dSq > arrivalRad * arrivalRad) continue;
                    float falloff = Math.clamp(1.0F - 0.67F * (float) (Math.sqrt(dSq) / Math.max(0.5D, arrivalRad)), 0.33F, 1.0F);
                    Vec3 outDir = dSq > 1.0E-4D ? foe.position().subtract(arrivalCenter).normalize() : normDir;
                    executeSingleEffectOnEntity(
                            level, caster, foe, outDir, spell, slot, sec,
                            SpellPartSlotType.SECONDARY_EFFECT, fallbackMateria, apparatusPotency * falloff
                    );
                }
            }
        }

        // Preserve arrival Form mutations when combined with Radial Burst, Aura, or Cone (e.g., Air Gust Shield / Slipstream Shield)
        if (formHandler != null) {
            if (form == SpellDeliveryForm.RADIAL_BURST) {
                formHandler.onRadialBurstFormCast(level, caster, arrivalCenter, arrivalRad, normDir, spell, slot, fallbackMateria, apparatusPotency);
            } else if (form == SpellDeliveryForm.AURA) {
                formHandler.onAuraFormCast(level, caster, arrivalRad, spell, slot, fallbackMateria, apparatusPotency);
                SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
            } else if (form == SpellDeliveryForm.CONE) {
                formHandler.onConeFormCast(level, caster, normDir, arrivalRad, List.of(), spell, slot, fallbackMateria, apparatusPotency);
            }
        }

        return true;
    }

    @Nullable
    private static LivingEntity raycastBlinkSwapTarget(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double maxReach,
            int tier,
            EssenceType materia,
            boolean mutated
    ) {
        Vec3 start = caster.getEyePosition();
        Vec3 end = start.add(direction.normalize().scale(maxReach));
        boolean phaseWalls = canBlinkPhaseWalls(level, caster.position(), BlockPos.containing(end), tier, materia, mutated);
        if (!phaseWalls) {
            BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                end = blockHit.getLocation();
            }
        }

        AABB search = new AABB(start, end).inflate(1.0D);
        LivingEntity best = null;
        double bestDistSq = Double.MAX_VALUE;
        long now = level.getGameTime();
        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, search, LivingEntity::isAlive)) {
            if (cand == caster
                    || SpellCombatReactionEngine.isBossEntity(level, cand)
                    || SpellCombatReactionEngine.isBlinkAnchored(cand, now)) {
                continue;
            }
            Optional<Vec3> clip = cand.getBoundingBox().inflate(0.55D).clip(start, end);
            if (clip.isPresent()) {
                double dSq = start.distanceToSqr(clip.get());
                if (dSq < bestDistSq) {
                    bestDistSq = dSq;
                    best = cand;
                }
            }
        }
        return best;
    }

    /**
     * Section 5.4 Blink Transposition ("Position Swap"): swaps spatial coordinates cleanly with a non-boss entity.
     */
    private static boolean performTranspositionSwap(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity target,
            int tier,
            EssenceType materia,
            boolean mutated,
            float rangeScale
    ) {
        long now = level.getGameTime();
        if (SpellCombatReactionEngine.isBlinkAnchored(caster, now)
                || SpellCombatReactionEngine.isBlinkAnchored(target, now)
                || SpellCombatReactionEngine.isBossEntity(level, target)) {
            return false;
        }
        double maxDist = resolveBlinkMaxRange(level, caster.position(), target.blockPosition(), tier, materia, mutated, rangeScale);
        if (caster.distanceToSqr(target) > (maxDist + 1.5D) * (maxDist + 1.5D)) {
            return false;
        }
        Vec3 casterOrigin = caster.position();
        Vec3 targetOrigin = target.position();

        caster.teleportTo(targetOrigin.x, targetOrigin.y, targetOrigin.z);
        caster.resetFallDistance();
        target.teleportTo(casterOrigin.x, casterOrigin.y, casterOrigin.z);
        target.resetFallDistance();

        if (mutated && materia != EssenceType.AIR
                && materia != EssenceType.WATER
                && materia != EssenceType.UMBRAL
                && materia != EssenceType.ABYSS
                && materia != EssenceType.NULL_U) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastBlinkRift(level, casterOrigin, targetOrigin, materia, tier);
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, targetOrigin, materia, null, 1.0D, tier);
        }

        MateriaPartMutationHandler effHandler = SpellMutationHandlerRegistry.resolveActive(materia, mutated);
        if (effHandler != null) {
            effHandler.onBlinkCast(level, caster, casterOrigin, targetOrigin, tier);
        }
        return true;
    }

    private static boolean performPointBlankZeroBlink(
            ServerLevel level,
            LivingEntity caster,
            int tier,
            EssenceType materia,
            boolean mutated
    ) {
        if (SpellCombatReactionEngine.isBlinkAnchored(caster, level.getGameTime())) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, caster.getX(), caster.getY(0.8D), caster.getZ(),
                    14, 0.3D, 0.4D, 0.3D, 0.08D);
            BLINK_CANCELLED_REASON.set("§5[Spacetime Anchor] §7Dimensional Blink is locked by a Void Hex (§a100% Materia refunded§7)!");
            return false;
        }

        Vec3 origin = caster.position();
        double freefallSpeed = caster.getDeltaMovement().y < -0.25D ? -caster.getDeltaMovement().y : 0.0D;
        caster.resetFallDistance();

        if (freefallSpeed > 0.0D) {
            Vec3 look = caster.getLookAngle();
            Vec3 horiz = new Vec3(look.x, 0.0D, look.z);
            Vec3 forwardDir = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : look.normalize();
            Vec3 converted = forwardDir.scale(Math.min(1.85D, freefallSpeed * 0.90D));
            applySyncedMotion(caster, new Vec3(converted.x, Math.max(0.08D, caster.getDeltaMovement().y), converted.z));
        }

        boolean suppressGenericRift = !mutated
                || materia == EssenceType.AIR
                || materia == EssenceType.WATER
                || materia == EssenceType.NATURE
                || materia == EssenceType.OVERGROWTH
                || materia == EssenceType.EARTH
                || materia == EssenceType.FROZEN
                || materia == EssenceType.GLACIAL
                || materia == EssenceType.NETHER
                || materia == EssenceType.RADIANT
                || materia == EssenceType.UMBRAL
                || materia == EssenceType.UNDEAD
                || materia == EssenceType.ABYSS
                || materia == EssenceType.NULL_U
                || materia == EssenceType.DAWN
                || materia == EssenceType.WRAITH
                || materia == EssenceType.STATIC
                || materia == EssenceType.CATACLYSM
                || materia == EssenceType.ESCHATON;
        if (!suppressGenericRift) {
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, origin, materia, null, 1.0D, tier);
        }

        MateriaPartMutationHandler effHandler = SpellMutationHandlerRegistry.resolveActive(materia, mutated);
        if (effHandler != null) {
            effHandler.onBlinkCast(level, caster, origin, origin, tier);
            return true;
        }

        if (mutated && MateriaFamilyTier.isNetherLineage(materia)) {
            for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(2.5D))) {
                if (nearby != caster && nearby.isAlive()) {
                    nearby.igniteForSeconds(3 + tier);
                }
            }
        }
        return true;
    }

    private static boolean performAimedSelfBlink(
            ServerLevel level,
            LivingEntity caster,
            Vec3 lookDirection,
            int tier,
            EssenceType materia,
            boolean mutated,
            float rangeScale
    ) {
        Vec3 dir = lookDirection.lengthSqr() > 1.0E-4D ? lookDirection.normalize() : caster.getLookAngle().normalize();
        Vec3 eye = caster.getEyePosition();
        BlockPos approxEnd = BlockPos.containing(eye.add(dir.scale(16.0D * Math.max(0.25D, rangeScale))));
        double maxDist = resolveBlinkMaxRange(level, caster.position(), approxEnd, tier, materia, mutated, rangeScale);
        boolean phaseWalls = canBlinkPhaseWalls(level, caster.position(), approxEnd, tier, materia, mutated);
        boolean miasmaThinBarrierPhase = mutated && materia == EssenceType.MIASMA;

        Vec3 rayEnd = eye.add(dir.scale(maxDist));
        BlockHitResult losHit = level.clip(new ClipContext(
                eye,
                rayEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                caster
        ));

        Vec3 desiredPos;
        if (losHit.getType() == HitResult.Type.BLOCK) {
            BlockState barrier = level.getBlockState(losHit.getBlockPos());
            boolean thinBarrier = miasmaThinBarrierPhase
                    && (barrier.is(BlockTags.FENCES)
                    || barrier.is(BlockTags.FENCE_GATES)
                    || barrier.is(Blocks.IRON_BARS)
                    || barrier.getBlock() instanceof net.minecraft.world.level.block.CrossCollisionBlock);
            if (thinBarrier) {
                desiredPos = rayEnd.subtract(0.0D, 0.8D, 0.0D);
            } else if (!phaseWalls || (losHit.getDirection() == Direction.UP && dir.y > -0.55D)) {
                // Visible floor surface ahead (or Tiers 1–3 line-of-sight block): snap onto the visible surface!
                if (losHit.getDirection() == Direction.UP) {
                    desiredPos = new Vec3(losHit.getLocation().x, losHit.getBlockPos().getY() + 1.0D, losHit.getLocation().z);
                } else {
                    desiredPos = Vec3.atBottomCenterOf(losHit.getBlockPos().relative(losHit.getDirection()));
                }
            } else {
                // Tier 4+ / Void / Earth wall-phasing through walls, doors, cliffs, ceilings, or steep floors!
                desiredPos = rayEnd.subtract(0.0D, 0.8D, 0.0D);
            }
        } else {
            desiredPos = rayEnd.subtract(0.0D, 0.8D, 0.0D);
        }

        return performSafeBlinkInternal(level, caster, eye, dir, desiredPos, maxDist, phaseWalls, tier, materia, mutated);
    }

    private static boolean performSafeBlink(
            ServerLevel level,
            LivingEntity caster,
            Vec3 desiredPos,
            int tier,
            EssenceType materia,
            boolean mutated,
            float rangeScale
    ) {
        Vec3 eye = caster.getEyePosition();
        BlockPos roughTarget = BlockPos.containing(desiredPos);
        double maxDist = resolveBlinkMaxRange(level, caster.position(), roughTarget, tier, materia, mutated, rangeScale);
        boolean phaseWalls = canBlinkPhaseWalls(level, caster.position(), roughTarget, tier, materia, mutated);
        boolean miasmaThinBarrierPhase = mutated && materia == EssenceType.MIASMA;

        Vec3 deltaFromFeet = desiredPos.subtract(caster.position());
        if (deltaFromFeet.length() > maxDist) {
            desiredPos = caster.position().add(deltaFromFeet.normalize().scale(maxDist));
        }

        if (!phaseWalls) {
            boolean treePhase = mutated && (materia == EssenceType.NATURE || materia == EssenceType.OVERGROWTH);
            boolean earthBurrowPhase = mutated && materia == EssenceType.EARTH;
            Vec3 clipEnd = desiredPos.add(0.0D, 0.8D, 0.0D);
            BlockHitResult losHit = level.clip(new ClipContext(
                    eye,
                    clipEnd,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    caster
            ));
            if (losHit.getType() == HitResult.Type.BLOCK) {
                BlockState barrier = level.getBlockState(losHit.getBlockPos());
                boolean thinBarrier = miasmaThinBarrierPhase
                        && (barrier.is(BlockTags.FENCES)
                        || barrier.is(BlockTags.FENCE_GATES)
                        || barrier.is(Blocks.IRON_BARS)
                        || barrier.getBlock() instanceof net.minecraft.world.level.block.CrossCollisionBlock);
                if (treePhase && (barrier.is(BlockTags.LOGS) || barrier.is(BlockTags.LEAVES))) {
                    // Section 6.4 Tree-Stepping: phase cleanly through living tree trunks & leaf canopies without phasing stone walls!
                    Vec3 marchSpan = clipEnd.subtract(eye);
                    double marchLen = marchSpan.length();
                    if (marchLen > 0.25D) {
                        Vec3 marchDir = marchSpan.scale(1.0D / marchLen);
                        for (double d = 0.5D; d <= marchLen; d += 0.45D) {
                            Vec3 probePt = eye.add(marchDir.scale(d));
                            BlockPos bp = BlockPos.containing(probePt);
                            BlockState bs = level.getBlockState(bp);
                            if (!bs.getCollisionShape(level, bp).isEmpty()
                                    && !bs.is(BlockTags.LOGS)
                                    && !bs.is(BlockTags.LEAVES)) {
                                desiredPos = eye.add(marchDir.scale(Math.max(0.0D, d - 0.55D))).subtract(0.0D, 0.8D, 0.0D);
                                break;
                            }
                        }
                    }
                } else if (earthBurrowPhase && isStoneOrSoilStrata(barrier)) {
                    // Section 6.5 Burrow Step: submerge through solid stone, soil, & ore walls while stopping cleanly at wood/metal/bedrock!
                    Vec3 marchSpan = clipEnd.subtract(eye);
                    double marchLen = marchSpan.length();
                    if (marchLen > 0.25D) {
                        Vec3 marchDir = marchSpan.scale(1.0D / marchLen);
                        for (double d = 0.5D; d <= marchLen; d += 0.45D) {
                            Vec3 probePt = eye.add(marchDir.scale(d));
                            BlockPos bp = BlockPos.containing(probePt);
                            BlockState bs = level.getBlockState(bp);
                            if (!bs.getCollisionShape(level, bp).isEmpty() && !isStoneOrSoilStrata(bs)) {
                                desiredPos = eye.add(marchDir.scale(Math.max(0.0D, d - 0.55D))).subtract(0.0D, 0.8D, 0.0D);
                                break;
                            }
                        }
                    }
                } else if (!thinBarrier) {
                    desiredPos = losHit.getDirection() == Direction.UP
                            ? new Vec3(losHit.getLocation().x, losHit.getBlockPos().getY() + 1.0D, losHit.getLocation().z)
                            : Vec3.atBottomCenterOf(losHit.getBlockPos().relative(losHit.getDirection()));
                }
            }
        }

        Vec3 rayVec = desiredPos.subtract(caster.position());
        Vec3 rayDir = rayVec.lengthSqr() > 1.0E-4D ? rayVec.normalize() : caster.getLookAngle().normalize();
        return performSafeBlinkInternal(level, caster, eye, rayDir, desiredPos, maxDist, phaseWalls, tier, materia, mutated);
    }

    private static double resolveBlinkMaxRange(
            ServerLevel level,
            Vec3 departurePos,
            BlockPos roughTarget,
            int tier,
            EssenceType materia,
            boolean mutated,
            float rangeScale
    ) {
        // Section 5.4 Canonical Blink Range: 16.0 blocks at Tiers 1–3, scaling upward at Tier 4+!
        double maxDist = tier <= 3 ? 16.0D : 16.0D + (tier - 3) * 2.5D;

        boolean silentUmbralStep = mutated && materia == EssenceType.UMBRAL;
        boolean natureTreeStep = mutated && (materia == EssenceType.NATURE || materia == EssenceType.OVERGROWTH);
        boolean sporeNetworkLinked = mutated && materia == EssenceType.SPORE
                && (isNearMyceliumOrMushrooms(level, BlockPos.containing(departurePos))
                || isNearMyceliumOrMushrooms(level, roughTarget));
        boolean taigaPineStep = mutated && materia == EssenceType.TAIGA;
        boolean astralCometDash = mutated && (materia == EssenceType.ASTRAL
                || materia == EssenceType.RIME
                || materia == EssenceType.GENESIS
                || materia == EssenceType.PRISMATIC);
        boolean twentyBlockApexStep = mutated && (materia == EssenceType.AETHER
                || materia == EssenceType.ENTROPIC
                || materia == EssenceType.CELESTIAL);
        boolean twentyTwoBlockEschatonStep = mutated && (materia == EssenceType.EMPYREAN
                || materia == EssenceType.CATACLYSM
                || materia == EssenceType.TERMINUS);
        boolean twentyFourBlockSingularityStep = mutated && (materia == EssenceType.ESCHATON
                || materia == EssenceType.APOTHEOSIS
                || materia == EssenceType.ENTROPICA);
        boolean sixteenBlockDashStep = mutated && (materia == EssenceType.BLOOD
                || materia == EssenceType.ECLIPSE
                || materia == EssenceType.VAPOR
                || materia == EssenceType.NULL_R
                || materia == EssenceType.NULL_U
                || materia == EssenceType.PENUMBRA
                || materia == EssenceType.SYLVAN
                || materia == EssenceType.OBLIVION
                || materia == EssenceType.CHIMERA);
        boolean fourteenBlockFusionStep = mutated && (materia == EssenceType.GLACIAL
                || materia == EssenceType.OVERGROWTH
                || materia == EssenceType.DUST
                || materia == EssenceType.SPORE
                || materia == EssenceType.TAIGA
                || materia == EssenceType.OASIS
                || materia == EssenceType.VITAE
                || materia == EssenceType.AURA
                || materia == EssenceType.AMBER
                || materia == EssenceType.BLIGHT
                || materia == EssenceType.DAWN
                || materia == EssenceType.ABYSS
                || materia == EssenceType.AURORA
                || materia == EssenceType.MIRAGE
                || materia == EssenceType.BARROW
                || materia == EssenceType.PYRE
                || materia == EssenceType.SPRING
                || materia == EssenceType.STATIC
                || materia == EssenceType.MIASMA);
        boolean twelveBlockStep = mutated && (materia == EssenceType.MAGMA
                || materia == EssenceType.SOULFIRE
                || materia == EssenceType.AEGIS
                || materia == EssenceType.WRAITH);

        if (silentUmbralStep && level.getMaxLocalRawBrightness(roughTarget) <= 9) {
            boolean fromShadow = level.getMaxLocalRawBrightness(BlockPos.containing(departurePos)) <= 9;
            maxDist *= fromShadow ? 1.50D : 1.35D;
        }
        if (twelveBlockStep) {
            maxDist = Math.max(maxDist, 12.0D + (tier - 1) * 2.5D);
        }
        if (fourteenBlockFusionStep) {
            maxDist = Math.max(maxDist, 14.0D + (tier - 1) * 2.5D);
        }
        if (natureTreeStep && (isNearTreeFoliage(level, BlockPos.containing(departurePos)) || isNearTreeFoliage(level, roughTarget))) {
            maxDist = Math.max(maxDist, 24.0D + tier * 2.5D);
        }
        if (sporeNetworkLinked) {
            maxDist = Math.max(maxDist, 24.0D + tier * 2.5D);
        }
        if (taigaPineStep && (isNearTreeFoliage(level, BlockPos.containing(departurePos)) || isNearTreeFoliage(level, roughTarget))) {
            maxDist = Math.max(maxDist, 18.0D + tier * 2.5D);
        }
        if (sixteenBlockDashStep) {
            maxDist = Math.max(maxDist, 16.0D + (tier - 1) * 2.5D);
        }
        if (astralCometDash) {
            maxDist = Math.max(maxDist, 18.0D + (tier - 1) * 2.5D);
        }
        if (twentyBlockApexStep) {
            maxDist = Math.max(maxDist, 20.0D + (tier - 1) * 2.5D);
        }
        if (twentyTwoBlockEschatonStep) {
            maxDist = Math.max(maxDist, 22.0D + (tier - 1) * 2.5D);
        }
        if (twentyFourBlockSingularityStep) {
            maxDist = Math.max(maxDist, 24.0D + (tier - 1) * 2.5D);
        }

        return maxDist * Math.max(0.25D, rangeScale);
    }

    private static boolean canBlinkPhaseWalls(
            ServerLevel level,
            Vec3 departurePos,
            BlockPos roughTarget,
            int tier,
            EssenceType materia,
            boolean mutated
    ) {
        boolean sporeNetworkLinked = mutated && materia == EssenceType.SPORE
                && (isNearMyceliumOrMushrooms(level, BlockPos.containing(departurePos))
                || isNearMyceliumOrMushrooms(level, roughTarget));
        return tier >= 4
                || sporeNetworkLinked
                || (mutated && (MateriaFamilyTier.isVoidLineage(materia)
                        || (MateriaFamilyTier.isEarthLineage(materia) && materia != EssenceType.EARTH)
                        || materia == EssenceType.VAPOR
                        || materia == EssenceType.PRISMATIC));
    }

    private static boolean performSafeBlinkInternal(
            ServerLevel level,
            LivingEntity caster,
            Vec3 rayOriginEye,
            Vec3 rayDirection,
            Vec3 desiredPos,
            double maxDist,
            boolean canPhaseWalls,
            int tier,
            EssenceType materia,
            boolean mutated
    ) {
        // Void Spacetime Anchoring Curse (#10) completely locks out Blink!
        if (SpellCombatReactionEngine.isBlinkAnchored(caster, level.getGameTime())) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, caster.getX(), caster.getY(0.8D), caster.getZ(),
                    14, 0.3D, 0.4D, 0.3D, 0.08D);
            BLINK_CANCELLED_REASON.set("§5[Spacetime Anchor] §7Dimensional Blink is locked by a Void Hex (§a100% Materia refunded§7)!");
            return false;
        }

        Vec3 departurePos = caster.position();

        // Sec 6.57 Eschaton Spacetime Fold Slip: 24m silent swap with furthest non-boss hostile within 24m through walls!
        if (mutated && materia == EssenceType.ESCHATON) {
            LivingEntity furthestFoe = null;
            double bestDistSq = 0.0D;
            for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(24.0D), LivingEntity::isAlive)) {
                if (cand == caster || SpellCombatReactionEngine.isBossEntity(level, cand)
                        || SpellCombatReactionEngine.isBlinkAnchored(cand, level.getGameTime())
                        || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, true)) continue;
                if (!(cand instanceof Monster) && cand != caster.getLastHurtMob()) continue;
                double dSq = caster.distanceToSqr(cand);
                if (dSq <= 576.0D && dSq > bestDistSq) {
                    bestDistSq = dSq;
                    furthestFoe = cand;
                }
            }
            if (furthestFoe != null) {
                Vec3 foePos = furthestFoe.position();
                caster.teleportTo(foePos.x, foePos.y, foePos.z);
                caster.resetFallDistance();
                furthestFoe.teleportTo(departurePos.x, departurePos.y, departurePos.z);
                furthestFoe.resetFallDistance();
                for (MobEffectInstance eff : new ArrayList<>(furthestFoe.getActiveEffects())) {
                    if (eff.getEffect().value().isBeneficial()) furthestFoe.removeEffect(eff.getEffect());
                }
                furthestFoe.addEffect(new MobEffectInstance(
                        net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ddraig.net.entropica.registry.ModEffects.PARALYZED.get()), 80, 0));
                furthestFoe.addEffect(new MobEffectInstance(
                        net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ddraig.net.entropica.registry.ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                MateriaPartMutationHandler effHandler = SpellMutationHandlerRegistry.resolveActive(materia, true);
                if (effHandler != null) {
                    effHandler.onBlinkCast(level, caster, departurePos, caster.position(), tier);
                }
                return true;
            }
        }

        boolean natureTreeStep = mutated && (materia == EssenceType.NATURE || materia == EssenceType.OVERGROWTH);
        boolean sporeNetworkLinked = mutated && materia == EssenceType.SPORE
                && (isNearMyceliumOrMushrooms(level, BlockPos.containing(departurePos))
                || isNearMyceliumOrMushrooms(level, BlockPos.containing(desiredPos)));
        boolean earthBurrowStep = mutated && (materia == EssenceType.EARTH || sporeNetworkLinked);

        // Capture downward freefall speed prior to Blink for Forward Momentum Conversion (Section 5.4)
        double freefallSpeed = caster.getDeltaMovement().y < -0.25D ? -caster.getDeltaMovement().y : 0.0D;
        boolean allowMidAirExit = freefallSpeed > 0.0D
                || caster.isFallFlying()
                || (caster instanceof Player player && player.getAbilities().flying);

        // Section 5.4 Raycast Smart Snap Fail-Safe:
        // Raycast backward along the cast line to find the nearest valid 2-block open air space with solid, non-hazardous ground underfoot!
        Vec3 transitVec = desiredPos.subtract(departurePos);
        double totalLen = transitVec.length();
        Vec3 transitUnit = totalLen > 1.0E-4D ? transitVec.scale(1.0D / totalLen) : rayDirection;

        BlockPos chosenLanding = null;
        if (mutated && materia == EssenceType.SPORE) {
            chosenLanding = findHyphalNetworkNodeNear(level, desiredPos, 5);
        }
        if (chosenLanding == null) {
            chosenLanding = findGroundedBlinkSpotAt(level, desiredPos, natureTreeStep, earthBurrowStep, canPhaseWalls);
        }
        if (chosenLanding == null && totalLen > 0.65D) {
            for (double stepBack = 0.65D; stepBack <= totalLen - 0.85D; stepBack += 0.65D) {
                Vec3 sample = desiredPos.subtract(transitUnit.scale(stepBack));
                BlockPos grounded = findGroundedBlinkSpotAt(level, sample, natureTreeStep, earthBurrowStep, canPhaseWalls);
                if (grounded != null) {
                    chosenLanding = grounded;
                    break;
                }
            }
        }

        // Mid-air freefall / flight exit fallback (for Section 5.4 Forward Momentum Conversion when high above non-lava terrain)
        if (chosenLanding == null && allowMidAirExit) {
            BlockPos midAir = BlockPos.containing(desiredPos);
            if (isSafeBlinkClearance(level, midAir) && isSafeMidAirExitOverNonLava(level, midAir)) {
                chosenLanding = midAir;
            } else if (totalLen > 0.65D) {
                for (double stepBack = 0.65D; stepBack <= totalLen - 0.85D; stepBack += 0.65D) {
                    BlockPos cand = BlockPos.containing(desiredPos.subtract(transitUnit.scale(stepBack)));
                    if (isSafeBlinkClearance(level, cand) && isSafeMidAirExitOverNonLava(level, cand)) {
                        chosenLanding = cand;
                        break;
                    }
                }
            }
        }

        if (chosenLanding == null) {
            return false;
        }

        Vec3 arrivalPos = new Vec3(chosenLanding.getX() + 0.5D, chosenLanding.getY(), chosenLanding.getZ() + 0.5D);
        if (arrivalPos.distanceToSqr(departurePos) < 1.15D) {
            // Point-blank blocked wall / cliff edge with nowhere safe ahead: cancel safely & refund 100% fuel!
            return false;
        }

        if (mutated && (materia == EssenceType.DUST || materia == EssenceType.SPORE || materia == EssenceType.AURA)) {
            EntityHelper.getPersistentData(caster).putLong(
                    SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 100L);
        }

        caster.teleportTo(arrivalPos.x, arrivalPos.y, arrivalPos.z);
        caster.resetFallDistance();

        // Section 5.4 Clean Stealth Transit:
        // Unmutated Blink is 100% silent & leaves zero rifts/explosions; Air renders its Vacuum Implosion at departure!
        boolean suppressGenericRift = !mutated
                || materia == EssenceType.AIR
                || materia == EssenceType.WATER
                || materia == EssenceType.NATURE
                || materia == EssenceType.OVERGROWTH
                || materia == EssenceType.EARTH
                || materia == EssenceType.FROZEN
                || materia == EssenceType.GLACIAL
                || materia == EssenceType.DUST
                || materia == EssenceType.SPORE
                || materia == EssenceType.NETHER
                || materia == EssenceType.RADIANT
                || materia == EssenceType.UMBRAL
                || materia == EssenceType.UNDEAD
                || materia == EssenceType.ABYSS
                || materia == EssenceType.NULL_U
                || materia == EssenceType.DAWN
                || materia == EssenceType.WRAITH
                || materia == EssenceType.STATIC
                || materia == EssenceType.CATACLYSM
                || materia == EssenceType.ESCHATON;
        if (!suppressGenericRift) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, caster.position(), materia, tier);
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, caster.position(), materia, null, 1.0D, tier);
        }

        // Section 5.4 Forward Momentum Conversion: convert downward terminal velocity into forward horizontal propulsion!
        if (freefallSpeed > 0.0D) {
            Vec3 look = caster.getLookAngle();
            Vec3 horiz = new Vec3(look.x, 0.0D, look.z);
            Vec3 forwardDir = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : look.normalize();
            Vec3 converted = forwardDir.scale(Math.min(1.85D, freefallSpeed * 0.90D));
            applySyncedMotion(caster, new Vec3(converted.x, Math.max(0.08D, caster.getDeltaMovement().y), converted.z));
        }

        MateriaPartMutationHandler effHandler = SpellMutationHandlerRegistry.resolveActive(materia, mutated);
        if (effHandler != null) {
            effHandler.onBlinkCast(level, caster, departurePos, caster.position(), tier);
            return true;
        }

        if (mutated && MateriaFamilyTier.isNetherLineage(materia)) {
            for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(2.5D))) {
                if (nearby != caster && nearby instanceof Monster) {
                    nearby.igniteForSeconds(3);
                }
            }
        }
        return true;
    }

    @Nullable
    private static BlockPos findGroundedBlinkSpotAt(
            ServerLevel level,
            Vec3 samplePos,
            boolean natureTreeStep,
            boolean earthBurrowStep,
            boolean canPhaseWalls
    ) {
        BlockPos base = BlockPos.containing(samplePos);
        if (natureTreeStep) {
            for (int dy = 10; dy >= -6; dy--) {
                BlockPos cand = base.offset(0, dy, 0);
                if (isSafeBlinkClearance(level, cand) && hasSafeGroundUnderfoot(level, cand)) {
                    return cand;
                }
            }
            return null;
        }
        if (earthBurrowStep) {
            for (int dy = 0; dy <= 8; dy++) {
                BlockPos up = base.above(dy);
                if (isSafeBlinkClearance(level, up) && hasSafeGroundUnderfoot(level, up)) {
                    return up;
                }
                if (dy > 0 && dy <= 6) {
                    BlockPos down = base.below(dy);
                    if (isSafeBlinkClearance(level, down) && hasSafeGroundUnderfoot(level, down)) {
                        return down;
                    }
                }
            }
            return null;
        }

        // Standard grounded snap: check exact block, +1 step-up, and up to 4 blocks of open air drop onto solid ground
        int[] offsets = new int[]{0, 1, -1, -2, -3, -4};
        for (int dy : offsets) {
            BlockPos cand = base.offset(0, dy, 0);
            if (!isSafeBlinkClearance(level, cand) || !hasSafeGroundUnderfoot(level, cand)) {
                continue;
            }
            if (!canPhaseWalls && dy < 0) {
                // Ensure we only drop through open air from the ray height down to the floor (never phasing through a ceiling!)
                boolean blockedAbove = false;
                for (int checkY = base.getY(); checkY > cand.getY(); checkY--) {
                    BlockPos airCol = new BlockPos(cand.getX(), checkY, cand.getZ());
                    if (!level.getBlockState(airCol).getCollisionShape(level, airCol).isEmpty() || isHazardousBlinkBlock(level, airCol)) {
                        blockedAbove = true;
                        break;
                    }
                }
                if (blockedAbove) {
                    continue;
                }
            }
            return cand;
        }
        return null;
    }

    private static boolean isSafeBlinkClearance(ServerLevel level, BlockPos feet) {
        BlockPos head = feet.above();
        return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(head).getCollisionShape(level, head).isEmpty()
                && !isHazardousBlinkBlock(level, feet)
                && !isHazardousBlinkBlock(level, head);
    }

    private static boolean hasSafeGroundUnderfoot(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        BlockState ground = level.getBlockState(below);
        return !ground.getCollisionShape(level, below).isEmpty() && !isHazardousBlinkBlock(level, below);
    }

    private static boolean isHazardousBlinkBlock(ServerLevel level, BlockPos pos) {
        if (level.getFluidState(pos).is(net.minecraft.tags.FluidTags.LAVA)) {
            return true;
        }
        BlockState bs = level.getBlockState(pos);
        Block b = bs.getBlock();
        if (b instanceof BaseFireBlock
                || bs.is(Blocks.MAGMA_BLOCK)
                || bs.is(Blocks.CACTUS)
                || bs.is(Blocks.SWEET_BERRY_BUSH)
                || bs.is(Blocks.POWDER_SNOW)
                || bs.is(Blocks.WITHER_ROSE)
                || bs.is(Blocks.POINTED_DRIPSTONE)) {
            return true;
        }
        return b instanceof CampfireBlock && bs.hasProperty(CampfireBlock.LIT) && bs.getValue(CampfireBlock.LIT);
    }

    private static boolean isSafeMidAirExitOverNonLava(ServerLevel level, BlockPos feet) {
        for (int depth = 1; depth <= 24; depth++) {
            BlockPos probe = feet.below(depth);
            if (isHazardousBlinkBlock(level, probe)) {
                return false;
            }
            BlockState bs = level.getBlockState(probe);
            if (!bs.getCollisionShape(level, probe).isEmpty() || !level.getFluidState(probe).isEmpty()) {
                return true;
            }
        }
        return true;
    }

    private static boolean isNearTreeFoliage(ServerLevel level, BlockPos center) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 3, 3))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(BlockTags.LOGS) || bs.is(BlockTags.LEAVES)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStoneOrSoilStrata(BlockState bs) {
        if (bs.is(Blocks.BEDROCK) || bs.is(Blocks.REINFORCED_DEEPSLATE) || bs.is(Blocks.OBSIDIAN) || bs.is(Blocks.CRYING_OBSIDIAN)) {
            return false;
        }
        return bs.is(BlockTags.BASE_STONE_OVERWORLD)
                || bs.is(BlockTags.BASE_STONE_NETHER)
                || bs.is(BlockTags.DIRT)
                || bs.is(BlockTags.SAND)
                || bs.is(BlockTags.TERRACOTTA)
                || bs.is(BlockTags.STONE_ORE_REPLACEABLES)
                || bs.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
                || bs.is(Blocks.GRAVEL)
                || bs.is(Blocks.COBBLESTONE)
                || bs.is(Blocks.MOSSY_COBBLESTONE)
                || bs.is(Blocks.COBBLED_DEEPSLATE)
                || bs.is(Blocks.SANDSTONE)
                || bs.is(Blocks.RED_SANDSTONE)
                || bs.is(Blocks.CLAY)
                || bs.is(Blocks.DRIPSTONE_BLOCK)
                || bs.is(Blocks.CALCITE)
                || bs.is(Blocks.SMOOTH_BASALT)
                || bs.is(Blocks.BLACKSTONE)
                || bs.is(Blocks.END_STONE)
                || bs.getBlock() instanceof net.minecraft.world.level.block.DropExperienceBlock;
    }

    private static boolean isNearMyceliumOrMushrooms(ServerLevel level, BlockPos center) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 2, 3))) {
            BlockState bs = level.getBlockState(p);
            if (isFungalNetworkBlock(bs)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFungalNetworkBlock(BlockState bs) {
        return bs.is(Blocks.MYCELIUM)
                || bs.is(Blocks.PODZOL)
                || bs.is(Blocks.RED_MUSHROOM) || bs.is(Blocks.BROWN_MUSHROOM)
                || bs.is(Blocks.RED_MUSHROOM_BLOCK) || bs.is(Blocks.BROWN_MUSHROOM_BLOCK)
                || bs.is(Blocks.MUSHROOM_STEM)
                || bs.is(Blocks.CRIMSON_NYLIUM) || bs.is(Blocks.WARPED_NYLIUM)
                || bs.is(Blocks.CRIMSON_FUNGUS) || bs.is(Blocks.WARPED_FUNGUS);
    }

    @Nullable
    private static BlockPos findHyphalNetworkNodeNear(ServerLevel level, Vec3 desiredPos, int searchRadius) {
        BlockPos center = BlockPos.containing(desiredPos);
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-searchRadius, -3, -searchRadius), center.offset(searchRadius, 3, searchRadius))) {
            BlockState bs = level.getBlockState(p);
            if (!isFungalNetworkBlock(bs)) continue;
            BlockPos stand = (bs.is(Blocks.RED_MUSHROOM) || bs.is(Blocks.BROWN_MUSHROOM)
                    || bs.is(Blocks.CRIMSON_FUNGUS) || bs.is(Blocks.WARPED_FUNGUS))
                    ? p.immutable()
                    : p.above();
            if (!isSafeBlinkClearance(level, stand)) continue;
            BlockState floor = level.getBlockState(stand.below());
            if (floor.getCollisionShape(level, stand.below()).isEmpty() || floor.is(Blocks.LAVA) || floor.is(Blocks.MAGMA_BLOCK)) continue;
            double dSq = stand.distToCenterSqr(desiredPos);
            if (dSq < bestDistSq) {
                bestDistSq = dSq;
                best = stand;
            }
        }
        return best;
    }

    private static void spawnSpellGravityZone(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            int tier,
            @Nullable EssenceType materia,
            boolean mutated
    ) {
        GravityField.Mode mode = GravityField.Mode.LUNAR;
        if (MateriaFamilyTier.isVoidLineage(materia)) {
            mode = GravityField.Mode.SINGULARITY;
        } else if (materia == EssenceType.ASTRAL || materia == EssenceType.CELESTIAL || materia == EssenceType.ENTROPICA) {
            mode = GravityField.Mode.ZERO_G;
        } else if (materia == EssenceType.AIR || materia == EssenceType.AETHER || materia == EssenceType.STORM || materia == EssenceType.VAPOR) {
            mode = GravityField.Mode.GRAV_LIFT;
        } else if (mutated && (materia == EssenceType.EARTH || materia == EssenceType.AEGIS)) {
            mode = GravityField.Mode.REPULSOR;
        } else if (mutated && (materia == EssenceType.CHIMERA || materia == EssenceType.MIRAGE)) {
            mode = GravityField.Mode.INVERSION;
        }
        double rad = 4.5D + tier * 1.2D;
        int duration = 160 + tier * 40;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("entropica", "spell_grav_" + level.getGameTime() + "_" + pos.asLong());
        GravityFieldManager.registerField(new GravityField(id, level.dimension(), pos, rad, mode, duration));

        Vec3 center = Vec3.atCenterOf(pos);
        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(level, center.subtract(0.0D, 0.4D, 0.0D), rad, materia, null, duration, tier);
        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastWardShell(level, center.subtract(0.0D, 0.9D, 0.0D), rad, materia, null, duration, tier);
        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, center, materia, null, rad * 0.65D, tier);
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.65F, 1.35F);

        // Give nearby creatures an immediate initial gravity pulse so the field is felt right on cast!
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(rad))) {
            if (!ent.isAlive() || ent.position().distanceToSqr(center) > rad * rad) continue;
            Vec3 cur = ent.getDeltaMovement();
            switch (mode) {
                case GRAV_LIFT, INVERSION -> applySyncedMotion(ent, new Vec3(cur.x, Math.max(0.48D, cur.y + 0.42D), cur.z));
                case ZERO_G, LUNAR -> {
                    if (ent.onGround()) {
                        applySyncedMotion(ent, new Vec3(cur.x, 0.36D, cur.z));
                    }
                    ent.resetFallDistance();
                }
                case SINGULARITY -> {
                    if (ent != caster) {
                        Vec3 toCenter = center.subtract(ent.position()).normalize().scale(0.45D);
                        applySyncedMotion(ent, cur.add(toCenter));
                    }
                }
                case REPULSOR -> {
                    if (ent != caster) {
                        Vec3 away = ent.position().subtract(center).normalize().scale(0.65D);
                        applySyncedMotion(ent, new Vec3(away.x, 0.35D, away.z));
                    }
                }
                default -> {
                }
            }
        }
        if (caster instanceof Player player) {
            player.displayClientMessage(
                    Component.literal("§b✦ Gravity Field §7[" + mode.getDisplayName() + "§7] — §f" + (duration / 20) + "s"),
                    true
            );
        }
    }

    /**
     * Sets an entity's motion and marks {@code hurtMarked = true} + {@code hasImpulse = true} so
     * {@link ServerPlayer}s receive an immediate velocity sync packet on the client!
     */
    public static void applySyncedMotion(Entity entity, Vec3 motion) {
        entity.setDeltaMovement(motion);
        entity.hasImpulse = true;
        entity.hurtMarked = true;
    }

    private static boolean isCasterBeneficialSpell(SpellAssembly spell) {
        for (SubSpellSlot slot : spell.subSpells()) {
            if (isCasterBeneficialVerb(slot.primaryEffect().effect())) return true;
            if (slot.secondaryEffect().map(s -> isCasterBeneficialVerb(s.effect())).orElse(false)) return true;
        }
        return false;
    }

    private static boolean isCasterBeneficialVerb(SpellCoreEffect e) {
        return e == SpellCoreEffect.FLIGHT
                || e == SpellCoreEffect.WARD
                || e == SpellCoreEffect.RESTORATION
                || e == SpellCoreEffect.BLINK
                || e == SpellCoreEffect.CONJURE
                || e == SpellCoreEffect.LOGISTICS
                || e == SpellCoreEffect.SONAR
                || e == SpellCoreEffect.ATMOSPHERE
                || e == SpellCoreEffect.ACOUSTIC
                || e == SpellCoreEffect.DIAGNOSTIC;
    }

    private static boolean slotHasEffect(SubSpellSlot slot, SpellCoreEffect target) {
        if (slot.primaryEffect().effect() == target) return true;
        return slot.secondaryEffect().map(s -> s.effect() == target).orElse(false);
    }

    private static boolean slotSelfTargetsCasterWhenNoEntityHit(SpellAssembly spell, SubSpellSlot slot) {
        if (slotHasEffect(slot, SpellCoreEffect.FLIGHT)
                || slotHasEffect(slot, SpellCoreEffect.WARD)
                || slotHasEffect(slot, SpellCoreEffect.RESTORATION)) {
            return true;
        }
        return spell.prefix() == SpellPrefix.UTILITY && slotHasEffect(slot, SpellCoreEffect.KINETIC);
    }

    private static boolean slotIncludesCasterInArea(SpellAssembly spell, SubSpellSlot slot) {
        if (isCasterBeneficialVerb(slot.primaryEffect().effect())
                || slot.primaryEffect().effect() == SpellCoreEffect.GRAVITY) {
            return true;
        }
        if (slot.secondaryEffect().map(s -> isCasterBeneficialVerb(s.effect()) || s.effect() == SpellCoreEffect.GRAVITY).orElse(false)) {
            return true;
        }
        return spell.prefix() == SpellPrefix.UTILITY && slotHasEffect(slot, SpellCoreEffect.KINETIC);
    }

    private static boolean slotHasVoxelSweepBlockVerb(SubSpellSlot slot) {
        return isVoxelSweepVerb(slot.primaryEffect().effect())
                || slot.secondaryEffect().map(s -> isVoxelSweepVerb(s.effect())).orElse(false);
    }

    private static boolean isVoxelSweepVerb(SpellCoreEffect e) {
        return e == SpellCoreEffect.BREAK
                || e == SpellCoreEffect.HARVEST
                || e == SpellCoreEffect.TRANSMUTE
                || e == SpellCoreEffect.CONSTRUCT;
    }

    private static boolean slotHasSinglePointBlockVerb(SubSpellSlot slot) {
        return isSinglePointBlockVerb(slot.primaryEffect().effect())
                || slot.secondaryEffect().map(s -> isSinglePointBlockVerb(s.effect())).orElse(false);
    }

    private static boolean isSinglePointBlockVerb(SpellCoreEffect e) {
        return e == SpellCoreEffect.CONJURE
                || e == SpellCoreEffect.BLINK
                || e == SpellCoreEffect.FLIGHT
                || e == SpellCoreEffect.GRAPPLE
                || e == SpellCoreEffect.LOGISTICS
                || e == SpellCoreEffect.SONAR
                || e == SpellCoreEffect.ATMOSPHERE
                || e == SpellCoreEffect.ACOUSTIC
                || e == SpellCoreEffect.DIAGNOSTIC
                || e == SpellCoreEffect.GRAVITY;
    }

    private static void deliverFlightOnlyToCaster(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        if (slot.primaryEffect().effect() == SpellCoreEffect.FLIGHT) {
            EssenceType mat = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            SpellMutationProfile mut = slot.primaryEffect().resolveMutation(SpellPartSlotType.CORE_EFFECT, spell.materiaSpecific(), fallbackMateria);
            applyArcaneFlightAndWings(level, caster, caster, direction, spell, slot.primaryEffect(), mat, mut, fallbackMateria, apparatusPotency);
        } else if (slot.secondaryEffect().isPresent() && slot.secondaryEffect().get().effect() == SpellCoreEffect.FLIGHT) {
            EffectPartBinding sec = slot.secondaryEffect().get();
            EssenceType mat = sec.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            SpellMutationProfile mut = sec.resolveMutation(SpellPartSlotType.SECONDARY_EFFECT, spell.materiaSpecific(), fallbackMateria);
            applyArcaneFlightAndWings(level, caster, caster, direction, spell, sec, mat, mut, fallbackMateria, apparatusPotency);
        }
    }

    private static void deliverSelfFocusedSubSpell(
            ServerLevel level,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        Vec3 look = caster.getLookAngle();
        int tier = spell.getEffectiveTier();

        // If SELF_CAST has GRAPPLE and the caster is aiming at a solid block, reel directly to that block!
        if (slotHasEffect(slot, SpellCoreEffect.GRAPPLE)) {
            double grapReach = Math.max(2.0D, spell.flowPart().travelDistanceMeters())
                    * spell.flawProfile().getRangeAndRadiusMultiplier();
            BlockHitResult grapHit = raycastBlocks(level, caster, look, grapReach);
            if (grapHit.getType() == HitResult.Type.BLOCK) {
                deliverSubSpellOnBlock(level, caster, grapHit.getBlockPos(), grapHit.getDirection(), spell, slot, fallbackMateria, apparatusPotency);
                return;
            }
        }

        deliverSubSpellOnEntity(level, caster, caster, look, spell, slot, fallbackMateria, apparatusPotency);

        if (slotHasEffect(slot, SpellCoreEffect.HARVEST)) {
            BlockPos center = caster.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
                deliverSubSpellOnBlock(level, caster, p.immutable(), Direction.UP, spell, slot, fallbackMateria, apparatusPotency);
            }
        } else if (slotHasEffect(slot, SpellCoreEffect.BREAK)
                || slotHasEffect(slot, SpellCoreEffect.TRANSMUTE)
                || slotHasEffect(slot, SpellCoreEffect.CONSTRUCT)) {
            BlockHitResult hit = raycastBlocks(level, caster, look, 6.0D + tier);
            if (hit.getType() == HitResult.Type.BLOCK) {
                deliverSubSpellOnBlock(level, caster, hit.getBlockPos(), hit.getDirection(), spell, slot, fallbackMateria, apparatusPotency);
            } else {
                deliverSubSpellOnBlock(level, caster, caster.blockPosition().below(), Direction.UP, spell, slot, fallbackMateria, apparatusPotency);
            }
        }
    }

    private static void deliverConeBlocks(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double coneRange,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        if (slotHasVoxelSweepBlockVerb(slot)) {
            int maxDist = Math.min(slotHasEffect(slot, SpellCoreEffect.CONSTRUCT) ? 3 : 6, (int) Math.ceil(coneRange));
            Vec3 origin = caster.getEyePosition();
            BlockPos base = BlockPos.containing(origin);
            for (BlockPos p : BlockPos.betweenClosed(
                    base.offset(-maxDist, -2, -maxDist),
                    base.offset(maxDist, 2, maxDist))) {
                Vec3 center = Vec3.atCenterOf(p);
                double dSq = center.distanceToSqr(origin);
                if (dSq < 1.0D || dSq > (double) (maxDist * maxDist)) continue;
                if (center.subtract(origin).normalize().dot(direction) >= 0.60D) {
                    deliverSubSpellOnBlock(level, caster, p.immutable(), Direction.UP, spell, slot, fallbackMateria, apparatusPotency);
                }
            }
        } else if (slotHasSinglePointBlockVerb(slot)
                && !slotSelfTargetsCasterWhenNoEntityHit(spell, slot)) {
            BlockHitResult hit = raycastBlocks(level, caster, direction, coneRange);
            if (hit.getType() == HitResult.Type.BLOCK) {
                deliverSubSpellOnBlock(level, caster, hit.getBlockPos(), hit.getDirection(), spell, slot, fallbackMateria, apparatusPotency);
            } else {
                BlockPos apex = BlockPos.containing(caster.getEyePosition().add(direction.scale(Math.min(6.0D, coneRange * 0.65D))));
                deliverSubSpellOnBlock(level, caster, apex, Direction.UP, spell, slot, fallbackMateria, apparatusPotency);
            }
        }
    }

    /**
     * Grants true timed 3D Arcane Flight, syncs Materia-colored 3D Arcane Wings to nearby clients,
     * and applies an immediate upward + look-angled wing-stroke impulse!
     */
    private static void applyArcaneFlightAndWings(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            Vec3 direction,
            SpellAssembly spell,
            EffectPartBinding effectBinding,
            @Nullable EssenceType effectMateria,
            SpellMutationProfile effectMutation,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        CompoundTag tag = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (tag.getLong("LastArcaneFlightCastTick").orElse(-20L) == now) {
            return;
        }
        tag.putLong("LastArcaneFlightCastTick", now);

        int tier = spell.getEffectiveTier();

        // Resolve primary & secondary Wing Materia from the Flight rune, sibling spell parts, or consumed Materia fuel!
        EssenceType wingPrimary = (effectMateria != null && effectMateria != EssenceType.REGULAR) ? effectMateria : null;
        EssenceType wingSecondary = null;

        if (!spell.subSpells().isEmpty()) {
            SubSpellSlot slot = spell.subSpells().getFirst();
            EssenceType pEffMat = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            EssenceType sEffMat = slot.secondaryEffect().map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria)).orElse(null);
            EssenceType formMat = slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            EssenceType flowMat = spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);

            if (wingPrimary == null) {
                if (pEffMat != EssenceType.REGULAR) wingPrimary = pEffMat;
                else if (sEffMat != null && sEffMat != EssenceType.REGULAR) wingPrimary = sEffMat;
                else if (formMat != EssenceType.REGULAR) wingPrimary = formMat;
                else if (flowMat != EssenceType.REGULAR) wingPrimary = flowMat;
                else wingPrimary = fallbackMateria != null ? fallbackMateria : EssenceType.REGULAR;
            }

            if (sEffMat != null && sEffMat != EssenceType.REGULAR && sEffMat != wingPrimary) {
                wingSecondary = sEffMat;
            } else if (pEffMat != EssenceType.REGULAR && pEffMat != wingPrimary) {
                wingSecondary = pEffMat;
            } else if (formMat != EssenceType.REGULAR && formMat != wingPrimary) {
                wingSecondary = formMat;
            } else if (flowMat != EssenceType.REGULAR && flowMat != wingPrimary) {
                wingSecondary = flowMat;
            }
        }
        if (wingPrimary == null) {
            wingPrimary = fallbackMateria != null ? fallbackMateria : EssenceType.REGULAR;
        }

        int baseDurationTicks = 200 + (tier * 100); // 15s at Tier 1 scaling up to 60s at Tier 10
        int durationTicks = Math.max(120, Math.round(
                baseDurationTicks
                        * effectMutation.potencyMultiplier()
                        * Math.max(0.5F, apparatusPotency)
                        * spell.flawProfile().getPotencyMultiplier()
        ));

        SpellLingeringZoneManager.grantArcane3DFlight(target, durationTicks, wingPrimary, wingSecondary, tier, true);

        // Immediate wing-stroke takeoff / aerial surge
        Vec3 aim = (target == caster ? target.getLookAngle() : direction).normalize();
        double speed = 0.72D + (tier * 0.08D);
        double liftY = target.onGround() ? Math.max(0.58D, aim.y * speed + 0.42D) : Math.max(0.22D, aim.y * speed);
        applySyncedMotion(target, new Vec3(aim.x * speed, liftY, aim.z * speed));
        target.resetFallDistance();

        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                level, target.position().add(0.0D, target.getBbHeight() * 0.65D, 0.0D), wingPrimary, wingSecondary, 1.15D, tier
        );
        level.playSound(null, target.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.75F, 1.35F);
        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.80F, 1.25F);

        if (target instanceof Player player) {
            String matLabel = wingSecondary != null
                    ? wingPrimary.getDisplayName() + " + " + wingSecondary.getDisplayName()
                    : wingPrimary.getDisplayName();
            player.displayClientMessage(
                    Component.literal("§b✦ Arcane Wings §7[" + matLabel + "§7] — §f" + (durationTicks / 20) + "s Flight"),
                    true
            );
        }
    }

    private static void performGrappleTetherToPoint(
            ServerLevel level,
            LivingEntity caster,
            Vec3 anchor,
            int tier,
            @Nullable EssenceType materia,
            float potencyMultiplier
    ) {
        Vec3 delta = anchor.subtract(caster.position());
        double dist = delta.length();
        if (dist < 0.35D) return;

        double pullSpeed = Math.min(2.65D, (0.95D + tier * 0.16D + dist * 0.06D) * potencyMultiplier);
        Vec3 vel = delta.normalize().scale(pullSpeed);
        applySyncedMotion(caster, new Vec3(vel.x, Math.max(0.42D, vel.y + 0.24D), vel.z));
        caster.resetFallDistance();

        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                level,
                caster.position().add(0.0D, caster.getBbHeight() * 0.6D, 0.0D),
                anchor,
                materia,
                null,
                tier
        );
        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, anchor, materia, null, 0.75D, tier);
        level.playSound(null, caster.blockPosition(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 0.85F, 1.15F);
    }

    private static void harvestOrGrowPlantBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int tier,
            @Nullable EssenceType materia,
            boolean mutated
    ) {
        boolean growthAffinity = mutated
                || materia == EssenceType.NATURE
                || materia == EssenceType.OVERGROWTH
                || materia == EssenceType.VITAE
                || materia == EssenceType.SPRING
                || materia == EssenceType.SYLVAN
                || materia == EssenceType.OASIS
                || materia == EssenceType.GENESIS
                || materia == EssenceType.WATER
                || materia == EssenceType.RADIANT;

        if (state.getBlock() instanceof CropBlock crop) {
            if (crop.isMaxAge(state)) {
                Block.dropResources(state, level, pos, null, caster, ItemStack.EMPTY);
                level.setBlock(pos, crop.getStateForAge(0), Block.UPDATE_ALL);
                ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, Vec3.atCenterOf(pos), materia, null, 0.55D, tier);
            } else if (growthAffinity) {
                crop.growCrops(level, pos, state);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5, 0.25D, 0.2D, 0.25D, 0.02D);
            }
            return;
        }
        if (state.getBlock() instanceof NetherWartBlock) {
            int age = state.getValue(NetherWartBlock.AGE);
            if (age >= NetherWartBlock.MAX_AGE) {
                Block.dropResources(state, level, pos, null, caster, ItemStack.EMPTY);
                level.setBlock(pos, state.setValue(NetherWartBlock.AGE, 0), Block.UPDATE_ALL);
            } else if (growthAffinity) {
                level.setBlock(pos, state.setValue(NetherWartBlock.AGE, Math.min(NetherWartBlock.MAX_AGE, age + 1)), Block.UPDATE_ALL);
            }
            return;
        }
        if (state.getBlock() instanceof CocoaBlock) {
            int age = state.getValue(CocoaBlock.AGE);
            if (age >= CocoaBlock.MAX_AGE) {
                Block.dropResources(state, level, pos, null, caster, ItemStack.EMPTY);
                level.setBlock(pos, state.setValue(CocoaBlock.AGE, 0), Block.UPDATE_ALL);
            } else if (growthAffinity) {
                level.setBlock(pos, state.setValue(CocoaBlock.AGE, Math.min(CocoaBlock.MAX_AGE, age + 1)), Block.UPDATE_ALL);
            }
            return;
        }
        if (state.getBlock() instanceof SweetBerryBushBlock) {
            int age = state.getValue(SweetBerryBushBlock.AGE);
            if (age >= 2) {
                int count = (age == 3 ? 2 : 1) + level.random.nextInt(2);
                Block.popResource(level, pos, new ItemStack(Items.SWEET_BERRIES, count));
                level.setBlock(pos, state.setValue(SweetBerryBushBlock.AGE, 1), Block.UPDATE_CLIENTS);
            } else if (growthAffinity) {
                level.setBlock(pos, state.setValue(SweetBerryBushBlock.AGE, Math.min(3, age + 1)), Block.UPDATE_ALL);
            }
            return;
        }
        if (state.hasProperty(BlockStateProperties.BERRIES) && state.getValue(BlockStateProperties.BERRIES)) {
            Block.popResource(level, pos, new ItemStack(Items.GLOW_BERRIES, 1));
            level.setBlock(pos, state.setValue(BlockStateProperties.BERRIES, false), Block.UPDATE_CLIENTS);
            return;
        }
        if (state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN)) {
            level.destroyBlock(pos, true, caster);
            return;
        }
        if (state.getBlock() instanceof SugarCaneBlock
                || state.getBlock() instanceof BambooStalkBlock
                || state.getBlock() instanceof CactusBlock) {
            if (level.getBlockState(pos.below()).is(state.getBlock())) {
                level.destroyBlock(pos, true, caster);
            }
            return;
        }
        if (growthAffinity && state.getBlock() instanceof BonemealableBlock growable
                && !(state.getBlock() instanceof GrassBlock)
                && growable.isValidBonemealTarget(level, pos, state)) {
            if (growable.isBonemealSuccess(level, level.random, pos, state)) {
                growable.performBonemeal(level, level.random, pos, state);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5, 0.25D, 0.25D, 0.25D, 0.02D);
            }
        }
    }

    private static void executeLogisticsMagnet(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            int tier,
            @Nullable EssenceType materia
    ) {
        if (caster == null) return;
        double rad = 6.0D + tier * 2.5D;
        AABB search = new AABB(center.x - rad, center.y - rad * 0.6D, center.z - rad,
                center.x + rad, center.y + rad * 0.6D, center.z + rad);
        boolean netherTransitSmelt = materia != null && MateriaFamilyTier.isNetherLineage(materia);
        boolean voidInstantTeleport = materia != null && MateriaFamilyTier.isVoidLineage(materia);
        int pulled = 0;
        int smeltedInTransit = 0;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, search, ItemEntity::isAlive)) {
            Vec3 from = item.position();
            if (netherTransitSmelt) {
                ItemStack refined = ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler
                        .trySmeltRawOreInTransit(level, item.getItem());
                if (refined != item.getItem()) {
                    item.setItem(refined);
                    smeltedInTransit++;
                    level.sendParticles(ParticleTypes.FLAME, from.x, from.y + 0.25D, from.z,
                            6, 0.16D, 0.16D, 0.16D, 0.025D);
                }
            }
            if (voidInstantTeleport && caster instanceof Player voidPlayer) {
                // Section 2.3 Void-Lineage Logistics: instantly teleports collected items straight into the player's inventory!
                ItemStack copy = item.getItem().copy();
                if (voidPlayer.getInventory().add(copy)) {
                    if (copy.isEmpty()) {
                        item.discard();
                    } else {
                        item.setItem(copy);
                        item.setNoPickUpDelay();
                        item.setPos(caster.getX(), caster.getY() + 0.5D, caster.getZ());
                    }
                } else {
                    item.setNoPickUpDelay();
                    item.setPos(caster.getX(), caster.getY() + 0.5D, caster.getZ());
                }
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y + 0.25D, from.z,
                        8, 0.16D, 0.16D, 0.16D, 0.04D);
                if (pulled < 8) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastBlinkRift(
                            level, from, caster.position().add(0.0D, 0.7D, 0.0D), materia, tier
                    );
                }
            } else {
                item.setNoPickUpDelay();
                item.setPos(caster.getX(), caster.getY() + 0.5D, caster.getZ());
                if (pulled < 8) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                            level, from, caster.position().add(0.0D, 0.7D, 0.0D), materia, null, tier
                    );
                }
            }
            pulled++;
        }
        if (smeltedInTransit > 0) {
            level.playSound(null, caster.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.65F, 1.28F);
        }
        for (ExperienceOrb xp : level.getEntitiesOfClass(ExperienceOrb.class, search, Entity::isAlive)) {
            xp.setPos(caster.getX(), caster.getY() + 0.5D, caster.getZ());
            pulled++;
        }
        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, center, materia, null, 0.9D, tier);
        if (caster instanceof Player player) {
            player.displayClientMessage(
                    Component.literal("§e✦ Arcane Logistics §7— Pulled §f" + pulled + "§7 dropped item/XP stacks (" + (int) rad + "m)"),
                    true
            );
        }
    }

    private static void executeSonarPulse(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            int tier,
            @Nullable EssenceType materia
    ) {
        double entityRad = 14.0D + tier * 4.0D;
        int revealedEntities = 0;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(entityRad))) {
            if (e != caster && e.isAlive() && e.position().distanceToSqr(center) <= entityRad * entityRad) {
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160 + tier * 25, 0));
                revealedEntities++;
            }
        }

        int oreRad = Math.min(12, 5 + tier);
        BlockPos origin = BlockPos.containing(center);
        int oreBlocks = 0;
        BlockPos nearestOre = null;
        String nearestOreName = "";
        double nearestDistSq = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-oreRad, -oreRad, -oreRad), origin.offset(oreRad, oreRad, oreRad))) {
            BlockState bs = level.getBlockState(p);
            if (isValuableOreBlock(bs)) {
                oreBlocks++;
                double dSq = p.distToCenterSqr(center);
                if (dSq < nearestDistSq) {
                    nearestDistSq = dSq;
                    nearestOre = p.immutable();
                    nearestOreName = bs.getBlock().getName().getString();
                }
                if (oreBlocks <= 14) {
                    ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, Vec3.atCenterOf(p), materia, null, 0.45D, tier);
                }
            }
        }

        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(level, center, Math.min(10.0D, entityRad * 0.45D), materia, null, tier);
        level.playSound(null, origin, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.95F, 1.15F);

        if (caster instanceof Player player) {
            String oreReport = nearestOre != null
                    ? "§b" + oreBlocks + " Ores §7(Nearest: §f" + nearestOreName + " §7at §e" + Math.round(Math.sqrt(nearestDistSq)) + "m§7)"
                    : "§70 Ores nearby";
            player.displayClientMessage(
                    Component.literal("§b✦ Arcane Sonar §7— Revealed §f" + revealedEntities + " Creatures§7 | " + oreReport),
                    true
            );
        }
    }

    private static boolean isValuableOreBlock(BlockState bs) {
        if (bs.isAir()) return false;
        if (bs.is(BlockTags.COAL_ORES)
                || bs.is(BlockTags.IRON_ORES)
                || bs.is(BlockTags.COPPER_ORES)
                || bs.is(BlockTags.GOLD_ORES)
                || bs.is(BlockTags.REDSTONE_ORES)
                || bs.is(BlockTags.LAPIS_ORES)
                || bs.is(BlockTags.DIAMOND_ORES)
                || bs.is(BlockTags.EMERALD_ORES)
                || bs.is(Blocks.ANCIENT_DEBRIS)
                || bs.is(Blocks.NETHER_QUARTZ_ORE)) {
            return true;
        }
        String key = bs.getBlock().getDescriptionId();
        return key.endsWith("_ore");
    }

    private static void executeAtmosphereShift(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            int tier,
            @Nullable EssenceType materia
    ) {
        int durationTicks = 1200 + tier * 1200; // 1 min at Tier 1 up to 11 min at Tier 10
        EssenceType mat = materia != null ? materia : EssenceType.REGULAR;
        String shiftLabel;

        boolean stormLineage = mat == EssenceType.STORM || mat == EssenceType.LIGHTNING || mat == EssenceType.STATIC
                || mat == EssenceType.ABYSS || mat == EssenceType.CATACLYSM;
        boolean rainLineage = mat == EssenceType.WATER || MateriaFamilyTier.isFrozenLineage(mat)
                || mat == EssenceType.VAPOR || mat == EssenceType.OASIS || mat == EssenceType.SPRING
                || mat == EssenceType.SPORE;

        if (stormLineage) {
            level.setWeatherParameters(0, durationTicks, true, true);
            shiftLabel = "§9Calling Thunderstorm";
            level.playSound(null, BlockPos.containing(center), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.75F, 1.15F);
        } else if (rainLineage) {
            level.setWeatherParameters(0, durationTicks, true, false);
            shiftLabel = "§bSummoning Rain & Mist";
            level.playSound(null, BlockPos.containing(center), SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.80F, 1.0F);
            // Hydrate local farmland & extinguish fires around the cast site
            BlockPos bp = BlockPos.containing(center);
            for (BlockPos p : BlockPos.betweenClosed(bp.offset(-4, -2, -4), bp.offset(4, 2, 4))) {
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                    level.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
                } else if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                }
            }
        } else {
            // Clear skies by default (Radiant, Dawn, Air, Nether, Aether, Celestial, Regular, etc.)
            // or toggle to Rain if cast with Regular while skies are already clear and caster is sneaking!
            if (mat == EssenceType.REGULAR && !level.isRaining() && caster != null && caster.isShiftKeyDown()) {
                level.setWeatherParameters(0, durationTicks, true, false);
                shiftLabel = "§bSummoning Rain";
            } else {
                level.setWeatherParameters(durationTicks, 0, false, false);
                shiftLabel = "§eClearing Skies";
                level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.WEATHER, 0.85F, 1.35F);
            }
        }

        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastSkyfallPillar(level, center, 2.2D, mat, null, tier);
        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, center, mat, null, 1.8D, tier);
        if (caster instanceof Player player) {
            player.displayClientMessage(
                    Component.literal("§b✦ Atmosphere Shift §7— " + shiftLabel + " §7(" + (durationTicks / 20) + "s)"),
                    true
            );
        }
    }

    private static void executeAcousticEffect(
            ServerLevel level,
            @Nullable LivingEntity caster,
            @Nullable LivingEntity hitTarget,
            Vec3 center,
            int tier,
            @Nullable EssenceType materia,
            boolean zeroEntityHarm
    ) {
        EssenceType mat = materia != null ? materia : EssenceType.REGULAR;
        BlockPos origin = BlockPos.containing(center);
        boolean hushLineage = mat == EssenceType.UMBRAL || MateriaFamilyTier.isVoidLineage(mat)
                || mat == EssenceType.NULL_R || mat == EssenceType.WRAITH;

        // Ring nearby Bells and Note Blocks!
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-4, -2, -4), origin.offset(4, 3, 4))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof BellBlock bell) {
                bell.attemptToRing(caster, level, p, Direction.UP);
            } else if (bs.getBlock() instanceof NoteBlock) {
                level.blockEvent(p, bs.getBlock(), 0, 0);
            }
        }

        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(level, center, 2.5D + tier * 0.4D, mat, null, 28, tier);
        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, center, mat, null, 1.2D, tier);

        if (hushLineage) {
            level.playSound(null, origin, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 0.8F, 1.35F);
            if (hitTarget != null && hitTarget == caster) {
                caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 120 + tier * 30, 0, false, false));
                if (caster instanceof Player player) {
                    player.displayClientMessage(Component.literal("§5✦ Acoustic Hush §7— Soundwaves muffled around you!"), true);
                }
            } else {
                double hushRad = 4.5D + tier * 0.8D;
                for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, new AABB(origin).inflate(hushRad))) {
                    if (!ent.isAlive()) continue;
                    if (ent == caster) {
                        ent.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100 + tier * 25, 0, false, false));
                    } else if (!zeroEntityHarm && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, true)) {
                        EntityHelper.getPersistentData(ent).putLong("SpellSilencedUntil", level.getGameTime() + 60L + tier * 15L);
                        ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60 + tier * 10, 0));
                        if (ent instanceof Mob mob && mob.getTarget() == caster) {
                            mob.setTarget(null);
                        }
                    }
                }
            }
        } else {
            float pitch = 0.85F + ((mat.ordinal() % 12) * 0.06F);
            level.playSound(null, origin, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, pitch);
            level.playSound(null, origin, SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 0.45F, 1.55F);

            double waveRad = 4.0D + tier * 0.75D;
            sweepLooseObjectsAndProjectiles(level, caster, new AABB(origin).inflate(waveRad), center, caster != null ? caster.getLookAngle() : new Vec3(0, 1, 0), tier, false);
            for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, new AABB(origin).inflate(waveRad))) {
                if (!ent.isAlive()) continue;
                if (ent == caster) {
                    ent.removeEffect(MobEffects.DARKNESS);
                    continue;
                }
                if (SpellCombatReactionEngine.isBossEntity(level, ent)) continue;
                Vec3 push = ent.position().subtract(center).normalize().scale(0.65D + tier * 0.1D);
                applySyncedMotion(ent, new Vec3(push.x, 0.32D, push.z));
                if (!zeroEntityHarm && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, true)) {
                    ent.hurtServer(level, level.damageSources().sonicBoom(caster != null ? caster : ent), 2.5F + tier * 1.2F);
                }
            }
            if (caster instanceof Player player) {
                player.displayClientMessage(Component.literal("§b✦ Acoustic Resonance §7— Sonic shockwave emitted!"), true);
            }
        }
    }

    private static void executeDiagnosticScan(
            ServerLevel level,
            @Nullable LivingEntity caster,
            @Nullable LivingEntity hitTarget,
            @Nullable BlockPos hitPos,
            int tier,
            @Nullable EssenceType materia
    ) {
        if (!(caster instanceof Player player)) {
            return;
        }
        EssenceType mat = materia != null ? materia : EssenceType.REGULAR;

        if (hitTarget != null) {
            if (hitTarget != caster) {
                hitTarget.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120 + tier * 20, 0));
            }
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                    level, hitTarget.position(), Math.max(1.0D, hitTarget.getBbWidth() + 0.6D), mat, null, 24, tier
            );
            level.playSound(null, hitTarget.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.55F);

            String name = hitTarget.getName().getString();
            String hp = String.format(Locale.ROOT, "%.1f/%.1f HP", hitTarget.getHealth(), hitTarget.getMaxHealth());
            int armor = hitTarget.getArmorValue();
            int effectCount = hitTarget.getActiveEffects().size();
            float ward = EntityHelper.getPersistentData(hitTarget).getFloat("SpellWardShieldHp").orElse(0.0F);
            String wardStr = ward > 0.05F ? String.format(Locale.ROOT, " §b| Ward: %.1f", ward) : "";
            player.displayClientMessage(
                    Component.literal("§a✦ Diagnostic §7[" + name + "] §c❤ " + hp + " §7| 🛡 " + armor + wardStr + " §7| Effects: §e" + effectCount),
                    true
            );
            return;
        }

        if (hitPos != null) {
            BlockState bs = level.getBlockState(hitPos);
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, Vec3.atCenterOf(hitPos), mat, null, 0.55D, tier);
            level.playSound(null, hitPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.55F);

            String blockName = bs.getBlock().getName().getString();
            int light = level.getMaxLocalRawBrightness(hitPos.above());
            int signal = level.getBestNeighborSignal(hitPos);
            StringBuilder extra = new StringBuilder();
            if (bs.getBlock() instanceof CropBlock crop) {
                int pct = Math.round(100.0F * crop.getAge(bs) / Math.max(1, crop.getMaxAge()));
                extra.append(" §7| Growth: §a").append(pct).append("%");
            }
            if (bs.hasProperty(BeehiveBlock.HONEY_LEVEL)) {
                extra.append(" §7| Honey: §6").append(bs.getValue(BeehiveBlock.HONEY_LEVEL)).append("/5");
            }
            if (level.getBlockEntity(hitPos) instanceof Container c) {
                int filled = 0;
                for (int i = 0; i < c.getContainerSize(); i++) {
                    if (!c.getItem(i).isEmpty()) filled++;
                }
                extra.append(" §7| Slots: §e").append(filled).append("/").append(c.getContainerSize());
            }
            if (signal > 0) {
                extra.append(" §7| Redstone: §c").append(signal);
            }
            player.displayClientMessage(
                    Component.literal("§a✦ Diagnostic §7[" + blockName + "] §7Light: §e" + light + extra),
                    true
            );
        }
    }

    private static BlockHitResult raycastBlocks(ServerLevel level, LivingEntity caster, Vec3 direction, double distance) {
        Vec3 start = caster.getEyePosition();
        Vec3 end = start.add(direction.scale(distance));
        return level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, caster));
    }
}
