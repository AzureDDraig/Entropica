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

    private SpellExecutionEngine() {
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
        int blinkFatigueStacks = 0;
        if (hasBlink) {
            CompoundTag pData = EntityHelper.getPersistentData(player);
            long lastBlink = pData.getLong("LastSpellBlinkTick").orElse(-200L);
            if (now - lastBlink <= 60L) {
                blinkFatigueStacks = pData.getInt("SpellBlinkFatigueStacks").orElse(0) + 1;
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

        // Apply Apparatus-modified cast cooldown
        int cooldown = Math.max(4, Math.round(spell.getBaseCooldownTicks() * state.apparatusType().getCooldownMultiplier()));
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

        // Dispatch Spell Assembly
        executeSpellAssembly(
                level,
                player,
                spell,
                fallbackMateria,
                state.apparatusType(),
                cost
        );

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
        if (spell.flowPart().flow().usesDynamicTriggerBilling()
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
        FlowPartBinding flowBinding = spell.flowPart();
        SpellFlowModifier flow = flowBinding.flow();

        float apparatusPotency = apparatusType != null ? apparatusType.getPotencyMultiplier() : 1.0F;
        float apparatusRange = apparatusType != null ? apparatusType.getRangeMultiplier() : 1.0F;

        int dispatchCount = Math.max(1, flowBinding.subSpellCount());
        List<SubSpellSlot> slots = spell.subSpells();

        boolean silentCast = isSilentStealthSpell(spell, fallbackMateria);
        if (!silentCast) {
            level.playSound(null, caster.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 0.85F, 1.2F);
            if (!slots.isEmpty()) {
                SubSpellSlot first = slots.getFirst();
                EssenceType pMat = first.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
                EssenceType sMat = first.secondaryEffect().map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria)).orElse(null);
                ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnCastMuzzleBurst(
                        level,
                        caster.getEyePosition().subtract(0.0D, 0.18D, 0.0D),
                        caster.getLookAngle(),
                        pMat,
                        sMat,
                        spell.getEffectiveTier()
                );
            }
        }

        if (flow == SpellFlowModifier.SELF_CAST || flow == SpellFlowModifier.SELF_TIMED) {
            for (int i = 0; i < slots.size(); i++) {
                SubSpellSlot s = slots.get(i);
                EssenceType pMat = s.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
                EssenceType sMat = s.secondaryEffect().map(sec -> sec.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria)).orElse(null);
                if (!silentCast) {
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastWardShell(
                            level, caster.position(), 1.35D, pMat, sMat, spell.getEffectiveTier()
                    );
                }
                deliverSelfFocusedSubSpell(level, caster, spell, s, fallbackMateria, apparatusPotency);

                if (flow == SpellFlowModifier.SELF_TIMED) {
                    SubSpellSlot delayedSlot = s;
                    SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                            caster,
                            caster.position(),
                            1.0D,
                            40,
                            level.getGameTime(),
                            null,
                            null,
                            pMat,
                            apparatusPotency,
                            (srv, zone, elapsed) -> {
                                if (elapsed < zone.maxDurationTicks) {
                                    return true;
                                }
                                LivingEntity owner = zone.resolveCaster(srv);
                                if (owner != null && owner.isAlive()) {
                                    if (!silentCast) {
                                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastWardShell(
                                                srv, owner.position(), 1.45D, pMat, sMat, spell.getEffectiveTier()
                                        );
                                    }
                                    deliverSelfFocusedSubSpell(srv, owner, spell, delayedSlot, fallbackMateria, apparatusPotency * 0.75F);
                                }
                                return false;
                            }
                    ));
                }
            }
            return;
        }

        // Dynamic Trigger Billing check for Triggered / Satellite Flows (75% remaining fuel when enemies are proximate on offensive Combat spells)
        if (flow.usesDynamicTriggerBilling() && caster instanceof Player player && costBreakdown != null) {
            if (spell.prefix() == SpellPrefix.COMBAT && !isCasterBeneficialSpell(spell)) {
                List<LivingEntity> nearbyHostiles = level.getEntitiesOfClass(
                        LivingEntity.class,
                        caster.getBoundingBox().inflate(10.0D),
                        e -> e.isAlive() && e != caster && e instanceof Monster
                );
                if (nearbyHostiles.isEmpty()) {
                    level.sendParticles(ParticleTypes.ENCHANT, caster.getX(), caster.getY(0.5), caster.getZ(),
                            18, 0.6, 0.2, 0.6, 0.05);
                    return;
                }
            }
            SpellFuelService.FuelCheckResult triggerFuel = SpellFuelService.consumePlayerSpellFuel(
                    player,
                    spell.getEffectiveTier(),
                    costBreakdown.dynamicTriggerMfumByType(),
                    false
            );
            if (!triggerFuel.success()) {
                return;
            }
        }

        EssenceType flowMateria = flowBinding.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        MateriaPartMutationHandler flowHandler = SpellMutationHandlerRegistry.resolveActive(flowMateria, flowBinding.mutationEnabled());
        if (flowBinding.mutationEnabled() && flowMateria == EssenceType.CHIMERA) {
            String rolled = switch (level.random.nextInt(4)) {
                case 0 -> "AIR";
                case 1 -> "EARTH";
                case 2 -> "WATER";
                default -> "NETHER";
            };
            EntityHelper.getPersistentData(caster).putString(SpellLingeringZoneManager.TAG_CHIMERA_ORBIT_ELEMENT, rolled);
        }
        if (flowBinding.mutationEnabled() && flow.usesDynamicTriggerBilling()) {
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
        }

        List<Vec3> directions = computeFlowDirections(caster, flow, dispatchCount);
        if (directions.size() > 1 && flowHandler != null) {
            flowHandler.onFlowMultiCastOrigin(level, caster, flow, directions.size(), spell);
        }
        for (int i = 0; i < directions.size(); i++) {
            SubSpellSlot slot = slots.get(i % slots.size());
            Vec3 dir = directions.get(i);
            dispatchSingleSubSpellForm(
                    level,
                    caster,
                    dir,
                    spell,
                    i % slots.size(),
                    slot,
                    fallbackMateria,
                    apparatusPotency,
                    apparatusRange
            );
        }
    }

    private static List<Vec3> computeFlowDirections(LivingEntity caster, SpellFlowModifier flow, int count) {
        Vec3 baseLook = caster.getLookAngle().normalize();
        if (count <= 1 && flow != SpellFlowModifier.SEPTUPLE_CROWN && flow != SpellFlowModifier.OCTUPLE_DOME) {
            return List.of(baseLook);
        }

        List<Vec3> list = new ArrayList<>(count);
        float baseYaw = caster.getYRot();
        float basePitch = caster.getXRot();

        switch (flow) {
            case SEPTUPLE_CROWN -> {
                int ringCount = Math.max(count, 7);
                for (int i = 0; i < ringCount; i++) {
                    float yaw = baseYaw + (360.0F * i / ringCount);
                    list.add(Vec3.directionFromRotation(0.0F, yaw));
                }
            }
            case OCTUPLE_DOME -> {
                int domeCount = Math.max(count, 8);
                for (int i = 0; i < domeCount; i++) {
                    float yaw = baseYaw + (360.0F * i / domeCount);
                    float pitch = (i % 2 == 0) ? -25.0F : -55.0F;
                    list.add(Vec3.directionFromRotation(pitch, yaw));
                }
            }
            case QUAD_SPREAD -> {
                list.add(Vec3.directionFromRotation(basePitch - 8.0F, baseYaw));
                list.add(Vec3.directionFromRotation(basePitch + 8.0F, baseYaw));
                list.add(Vec3.directionFromRotation(basePitch, baseYaw - 10.0F));
                list.add(Vec3.directionFromRotation(basePitch, baseYaw + 10.0F));
            }
            default -> {
                float totalSpread = Math.min(60.0F, (count - 1) * 10.0F);
                float startOffset = -totalSpread / 2.0F;
                float step = count > 1 ? totalSpread / (count - 1) : 0.0F;
                for (int i = 0; i < count; i++) {
                    list.add(Vec3.directionFromRotation(basePitch, baseYaw + startOffset + (step * i)));
                }
            }
        }
        return list;
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
            case BEAM, TOUCH_STRIKE -> {
                boolean earthReachBonus = formMutation.mutated() && MateriaFamilyTier.isEarthLineage(formMateria);
                double maxReach = (form == SpellDeliveryForm.TOUCH_STRIKE
                        ? SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.TOUCH_STRIKE, tier)
                        : SpellDeliveryForm.getBeamReachBlocks(tier, earthReachBonus)) * formRangeMult;

                castRayOrBeam(level, caster, direction, maxReach, spell, slot, fallbackMateria, apparatusPotency, formMutation, formHandler);
            }
            case RADIAL_BURST, AURA, GLYPH -> {
                double radius = SpellDeliveryForm.getCombatRadiusBlocks(form, tier) * formRangeMult;
                Vec3 center = caster.position();
                if (spell.flowPart().flow() == SpellFlowModifier.VOID_WARP || form == SpellDeliveryForm.GLYPH) {
                    BlockHitResult hit = raycastBlocks(level, caster, direction, 16.0D * formRangeMult);
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        center = Vec3.atCenterOf(hit.getBlockPos().relative(hit.getDirection()));
                    }
                }
                EssenceType effPrimary = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
                EssenceType vPrimary = formMateria != EssenceType.REGULAR ? formMateria : effPrimary;
                EssenceType vSecondary = slot.secondaryEffect()
                        .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                        .orElse(effPrimary != vPrimary ? effPrimary : null);

                if (!isSilentStealthSpell(spell, fallbackMateria)) {
                    if (form == SpellDeliveryForm.RADIAL_BURST) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(level, center, radius, vPrimary, vSecondary, tier);
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, center, vPrimary, vSecondary, radius, tier);
                    } else if (form == SpellDeliveryForm.AURA) {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastWardShell(level, center, Math.min(radius, 3.2D), vPrimary, vSecondary, tier);
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(level, center, radius, vPrimary, vSecondary, 36, tier);
                    } else {
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(level, center.subtract(0.0D, 0.45D, 0.0D), radius, vPrimary, vSecondary, 50, tier);
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, center, vPrimary, vSecondary, radius * 0.7D, tier);
                    }
                }

                if (form == SpellDeliveryForm.GLYPH
                        && formHandler != null
                        && formHandler.onGlyphFormCast(level, caster, center, radius, spell, slot, fallbackMateria, apparatusPotency)) {
                    break;
                }
                deliverRadialArea(level, caster, center, radius, direction, spell, slot, fallbackMateria, apparatusPotency);
                if (formHandler != null) {
                    if (form == SpellDeliveryForm.RADIAL_BURST) {
                        formHandler.onRadialBurstFormCast(level, caster, center, radius, direction, spell, slot, fallbackMateria, apparatusPotency);
                    } else if (form == SpellDeliveryForm.AURA) {
                        formHandler.onAuraFormCast(level, caster, radius, spell, slot, fallbackMateria, apparatusPotency);
                    }
                }
            }
            case CONE -> {
                double coneRange = SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.CONE, tier) * formRangeMult;
                EssenceType effPrimary = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
                EssenceType vPrimary = formMateria != EssenceType.REGULAR ? formMateria : effPrimary;
                EssenceType vSecondary = slot.secondaryEffect()
                        .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                        .orElse(effPrimary != vPrimary ? effPrimary : null);

                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVortexCone(
                        level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), direction, coneRange, vPrimary, vSecondary, tier
                );
                ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                        level, caster.getEyePosition().add(direction.scale(coneRange * 0.45D)), vPrimary, vSecondary, coneRange * 0.45D, tier
                );

                AABB box = caster.getBoundingBox().expandTowards(direction.scale(coneRange)).inflate(coneRange * 0.55D);
                List<LivingEntity> hitTargets = new ArrayList<>();
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
                    if (target == caster || !target.isAlive()) continue;
                    Vec3 toTarget = target.getEyePosition().subtract(caster.getEyePosition()).normalize();
                    if (toTarget.dot(direction) >= 0.55D) {
                        hitTargets.add(target);
                        deliverSubSpellOnEntity(level, caster, target, direction, spell, slot, fallbackMateria, apparatusPotency);
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
                BlockHitResult hit = raycastBlocks(level, caster, direction, 24.0D * formRangeMult);
                Vec3 impact = hit.getType() == HitResult.Type.BLOCK
                        ? Vec3.atBottomCenterOf(hit.getBlockPos().above())
                        : caster.position().add(direction.scale(10.0D));
                double radius = SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.SKYFALL, tier) * 0.5D * formRangeMult;
                EssenceType effPrimary = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
                EssenceType vPrimary = formMateria != EssenceType.REGULAR ? formMateria : effPrimary;
                EssenceType vSecondary = slot.secondaryEffect()
                        .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                        .orElse(effPrimary != vPrimary ? effPrimary : null);

                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastSkyfallPillar(level, impact, radius, vPrimary, vSecondary, tier);
                ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, impact, vPrimary, vSecondary, radius, tier);

                if (formHandler != null
                        && formHandler.onSkyfallFormCast(level, caster, impact, radius, spell, slot, fallbackMateria, apparatusPotency)) {
                    break;
                }
                deliverRadialArea(level, caster, impact, radius, new Vec3(0, -1, 0), spell, slot, fallbackMateria, apparatusPotency);
            }
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> {
                BlockHitResult hit = raycastBlocks(level, caster, direction, 12.0D);
                Vec3 spawnPos = hit.getType() == HitResult.Type.BLOCK
                        ? Vec3.atBottomCenterOf(hit.getBlockPos().relative(hit.getDirection()))
                        : caster.position().add(direction.scale(2.5D));

                EssenceType effPrimary = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
                EssenceType vPrimary = formMateria != EssenceType.REGULAR ? formMateria : effPrimary;
                EssenceType vSecondary = slot.secondaryEffect()
                        .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                        .orElse(effPrimary != vPrimary ? effPrimary : null);

                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastSummonPillar(level, spawnPos, 1.75D, vPrimary, vSecondary, tier);
                ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, spawnPos, vPrimary, vSecondary, 1.8D, tier);

                SpellEidolonEntity.TriadArchetype reqArchetype = slot.formPart().resolveSummonArchetype();
                if (formMutation.mutated()
                        && SpellEidolonEntity.isLifecycleRefreshMateria(formMateria)
                        && reqArchetype != SpellEidolonEntity.TriadArchetype.DECOY
                        && SpellEidolonEntity.tryRefreshLifecycleConstruct(level, caster, formMateria, reqArchetype, tier, spawnPos)) {
                    deliverRadialArea(level, caster, spawnPos, 3.0D * formRangeMult, direction, spell, slot, fallbackMateria, apparatusPotency);
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
                deliverRadialArea(level, caster, spawnPos, 3.0D * formRangeMult, direction, spell, slot, fallbackMateria, apparatusPotency);
            }
        }
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

        boolean voidFlowPhase = spell.flowPart().flow() == SpellFlowModifier.VOID_WARP
                && spell.flowPart().mutationEnabled()
                && spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria) == EssenceType.VOID;
        boolean wallPhasing = ((formMutation.mutated() && formMutation.wallPhasing()) || voidFlowPhase)
                && spell.prefix() != SpellPrefix.UTILITY;

        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, caster));
        Vec3 effectiveEnd = (!wallPhasing && blockHit.getType() == HitResult.Type.BLOCK) ? blockHit.getLocation() : end;

        // Broadcast custom 3D volumetric Beam or Crescent Slash + Materia-matched custom particles!
        EssenceType formMat = slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
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
        for (LivingEntity target : hitEntities) {
            struckTargets.add(target);
            deliverSubSpellOnEntity(level, caster, target, direction, spell, slot, fallbackMateria, apparatusPotency);
            if (spell.flowPart().flow() == SpellFlowModifier.CHAIN_LEAP || miasmaOrOblivionStrikeChain) {
                chainLeapFrom(level, caster, target, spell, slot, fallbackMateria, apparatusPotency);
            }
            if (++hitCount >= maxTargets) break;
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
            } else if (slot.formPart().form() == SpellDeliveryForm.TOUCH_STRIKE) {
                formHandler.onTouchStrikeFormCast(level, caster, direction, struckTargets, spell, slot, fallbackMateria, apparatusPotency);
            }
        }
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

    private static boolean isSilentStealthSpell(SpellAssembly spell, @Nullable EssenceType fallbackMateria) {
        for (SubSpellSlot slot : spell.subSpells()) {
            EssenceType formMat = slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
            if (slot.formPart().mutationEnabled()
                    && slot.formPart().form() == SpellDeliveryForm.RADIAL_BURST
                    && (formMat == EssenceType.ABYSS || formMat == EssenceType.NULL_U || formMat == EssenceType.UMBRAL)) {
                return true;
            }
            if (isSilentBlinkBinding(slot.primaryEffect(), spell, fallbackMateria)) {
                return true;
            }
            if (slot.secondaryEffect().map(sec -> isSilentBlinkBinding(sec, spell, fallbackMateria)).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSilentBlinkBinding(EffectPartBinding binding, SpellAssembly spell, @Nullable EssenceType fallbackMateria) {
        if (binding.effect() != SpellCoreEffect.BLINK || !binding.mutationEnabled()) {
            return false;
        }
        EssenceType mat = binding.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        return mat == EssenceType.ABYSS || mat == EssenceType.NULL_U || mat == EssenceType.UMBRAL
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
        Set<UUID> visited = new HashSet<>();
        visited.add(firstTarget.getUUID());
        List<LivingEntity> frontier = List.of(firstTarget);

        for (int step = 0; step < leaps && !frontier.isEmpty(); step++) {
            List<LivingEntity> nextFrontier = new ArrayList<>();
            int branchesPerAnchor = entropicQuintupleChain ? 5 : (trinityQuadChain ? 4 : (lightningBranching ? 2 : 1));
            float hopPotencyMult = entropicQuintupleChain ? 0.70F : (trinityQuadChain ? 0.60F : 0.85F);

            for (LivingEntity anchor : frontier) {
                boolean stormRainBoost = flowMutated && flowMateria == EssenceType.STORM && level.isRaining();
                double jumpRad = entropicQuintupleChain ? 8.0D
                        : (((lightningBranching && SpellLingeringZoneManager.isEntityWetOrSoaked(anchor)) || stormRainBoost)
                        ? 9.0D : 6.0D); // 8m wall-phasing chain (Entropic) or +50% jump search radius when wet/raining!
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

                int branched = 0;
                EssenceType arcPrimary = flowMateria != EssenceType.REGULAR
                        ? flowMateria
                        : slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
                EssenceType arcSecondary = slot.secondaryEffect()
                        .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                        .orElse(null);
                for (LivingEntity next : candidates) {
                    visited.add(next.getUUID());
                    Vec3 anchorMid = anchor.position().add(0.0D, anchor.getBbHeight() * 0.55D, 0.0D);
                    Vec3 nextMid = next.position().add(0.0D, next.getBbHeight() * 0.55D, 0.0D);
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                            level, anchorMid, nextMid, arcPrimary, arcSecondary, spell.getEffectiveTier()
                    );
                    ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                            level, nextMid, arcPrimary, arcSecondary, 0.9D, spell.getEffectiveTier()
                    );
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
            LivingEntity caster,
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
                    case WARD -> {
                        SpellCombatReactionEngine.grantWardShield(
                                living,
                                effectMateria,
                                tier,
                                effectMutation.potencyMultiplier() * spell.flawProfile().getPotencyMultiplier(),
                                160 + tier * 40
                        );
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastWardShell(
                                level, living.position(), Math.max(1.25D, living.getBbWidth() + 0.75D), effectMateria, null, tier
                        );
                    }
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
                            Vec3 anchor = living == caster
                                    ? caster.position().add(caster.getLookAngle().scale(10.0D + tier * 2.0D))
                                    : living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D);
                            performGrappleTetherToPoint(level, caster, anchor, tier, effectMateria, effectMutation.potencyMultiplier());
                        }
                    }
                    case BLINK -> {
                        if (caster != null) {
                            if ((subMode.equals("swap") || subMode.equals("transposition")) && living != caster) {
                                performTranspositionSwap(level, caster, living, tier, effectMateria, effectMutation.mutated());
                            } else {
                                Vec3 dest = living == caster
                                        ? caster.position().add(caster.getLookAngle().scale(6.0D + tier * 2.5D))
                                        : living.position();
                                performSafeBlink(level, caster, dest, tier, effectMateria, effectMutation.mutated());
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
                    level, caster, living, spell, effectMateria, effectMutation, apparatusPotency
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
            case WARD -> {
                SpellCombatReactionEngine.grantWardShield(
                        living,
                        effectMateria,
                        tier,
                        effectMutation.potencyMultiplier() * spell.flawProfile().getPotencyMultiplier(),
                        160 + tier * 40
                );
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastWardShell(
                        level, living.position(), Math.max(1.25D, living.getBbWidth() + 0.75D), effectMateria, null, tier
                );
            }
            case BLINK -> {
                if (caster != null) {
                    if ((subMode.equals("swap") || subMode.equals("transposition")) && living != caster) {
                        performTranspositionSwap(level, caster, living, tier, effectMateria, effectMutation.mutated());
                    } else {
                        Vec3 dest = living == caster
                                ? caster.position().add(caster.getLookAngle().scale(6.0D + tier * 2.5D))
                                : living.position().subtract(direction.scale(1.2D));
                        performSafeBlink(level, caster, dest, tier, effectMateria, effectMutation.mutated());
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
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastWardShell(
                            level, caster.position(), Math.max(1.25D, caster.getBbWidth() + 0.75D), effectMateria, null, tier
                    );
                }
            }
            case RESTORATION -> {
                if (caster != null && caster.position().distanceToSqr(Vec3.atCenterOf(pos)) <= 20.25D) {
                    applySpellRestoration(level, caster, caster, spell, effectBinding, effectMateria, effectMutation, 1.0F, true);
                }
            }
            case KINETIC -> {
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
                if (effHandler != null && effHandler.onTransmuteBlock(level, caster, pos, state, tier)) {
                    return;
                }
                float maxHardness = 2.0F + (tier * 5.0F);
                float hardness = state.getDestroySpeed(level, pos);
                if (hardness >= 0.0F && hardness <= maxHardness) {
                    level.destroyBlock(pos, true, caster);
                }
            }
            case HARVEST -> harvestOrGrowPlantBlock(level, caster, pos, state, tier, effectMateria, effectMutation.mutated());
            case TRANSMUTE -> {
                // Also transmute any floating ItemEntities resting directly on this block!
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.25D))) {
                    if (item.isAlive()) {
                        transmuteDroppedItemWithFallback(level, caster, item, tier, effectMateria, effHandler);
                    }
                }
                if (!state.isAir() && isProtectedBlockOrContainer(level, pos, state)) return;
                if (effHandler != null) {
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
                } else if (MateriaFamilyTier.isEarthLineage(effectMateria) || effectMateria == EssenceType.AIR) {
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
                    BlockPos safeDest = state.isAir() ? pos : pos.relative(hitFace);
                    performSafeBlink(level, caster, Vec3.atBottomCenterOf(safeDest), tier, effectMateria, effectMutation.mutated());
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
            living.hurtServer(
                    level,
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
                        160,
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
     * Section 5.4 Blink Transposition ("Swap"): swaps spatial coordinates cleanly with a non-boss entity.
     */
    private static void performTranspositionSwap(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity target,
            int tier,
            EssenceType materia,
            boolean mutated
    ) {
        long now = level.getGameTime();
        if (SpellCombatReactionEngine.isBlinkAnchored(caster, now)
                || SpellCombatReactionEngine.isBlinkAnchored(target, now)
                || SpellCombatReactionEngine.isBossEntity(level, target)) {
            return;
        }
        double maxDist = 8.0D + (tier * 3.5D);
        if (caster.distanceToSqr(target) > maxDist * maxDist) {
            return;
        }
        Vec3 casterOrigin = caster.position();
        Vec3 targetOrigin = target.position();

        caster.teleportTo(targetOrigin.x, targetOrigin.y, targetOrigin.z);
        caster.resetFallDistance();
        target.teleportTo(casterOrigin.x, casterOrigin.y, casterOrigin.z);
        target.resetFallDistance();

        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastBlinkRift(level, casterOrigin, targetOrigin, materia, tier);
        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, targetOrigin, materia, null, 1.1D, tier);

        MateriaPartMutationHandler effHandler = SpellMutationHandlerRegistry.resolveActive(materia, mutated);
        if (effHandler != null) {
            effHandler.onBlinkCast(level, caster, casterOrigin, targetOrigin, tier);
        }
    }

    private static void performSafeBlink(
            ServerLevel level,
            LivingEntity caster,
            Vec3 desiredPos,
            int tier,
            EssenceType materia,
            boolean mutated
    ) {
        // Void Spacetime Anchoring Curse (#10) completely locks out Blink!
        if (SpellCombatReactionEngine.isBlinkAnchored(caster, level.getGameTime())) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, caster.getX(), caster.getY(0.8D), caster.getZ(),
                    14, 0.3D, 0.4D, 0.3D, 0.08D);
            if (caster instanceof Player player) {
                player.displayClientMessage(Component.literal("§5[Spacetime Anchor] §7Dimensional Blink is locked by a Void Hex!"), true);
            }
            return;
        }

        double maxDist = 8.0D + tier * 3.5D;
        Vec3 departurePos = caster.position();
        BlockPos roughTarget = BlockPos.containing(desiredPos);

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
                return;
            }
        }

        boolean silentUmbralStep = mutated && materia == EssenceType.UMBRAL;
        boolean natureTreeStep = mutated && (materia == EssenceType.NATURE || materia == EssenceType.OVERGROWTH);
        boolean sporeHyphalStep = mutated && materia == EssenceType.SPORE;
        boolean sporeNetworkLinked = sporeHyphalStep && (isNearMyceliumOrMushrooms(level, BlockPos.containing(departurePos))
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
        boolean earthBurrowStep = mutated && (materia == EssenceType.EARTH || sporeNetworkLinked);
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
        boolean miasmaThinBarrierPhase = mutated && materia == EssenceType.MIASMA;
        boolean canPhaseWalls = tier >= 4
                || sporeNetworkLinked
                || (mutated && (MateriaFamilyTier.isVoidLineage(materia)
                        || MateriaFamilyTier.isEarthLineage(materia)
                        || materia == EssenceType.VAPOR
                        || materia == EssenceType.PRISMATIC));

        if (silentUmbralStep && level.getMaxLocalRawBrightness(roughTarget) <= 9) {
            maxDist *= 1.35D; // Extended Shadow-Step range between dim/dark areas
        }
        if (twelveBlockStep) {
            maxDist = Math.max(maxDist, 12.0D + (tier - 1) * 2.5D); // 12m+ baseline (Magma, Soulfire, Aegis, Wraith)
        }
        if (fourteenBlockFusionStep) {
            maxDist = Math.max(maxDist, 14.0D + (tier - 1) * 2.5D); // Canonical 14m+ blink baseline
        }
        if (natureTreeStep && (isNearTreeFoliage(level, BlockPos.containing(departurePos)) || isNearTreeFoliage(level, roughTarget))) {
            maxDist = Math.max(maxDist, 24.0D + tier * 2.5D); // Extended Tree-Stepping range across woodland canopies
        }
        if (sporeNetworkLinked) {
            maxDist = Math.max(maxDist, 24.0D + tier * 2.5D); // Extended underground Hyphal Network Blink between mycelium & mushrooms
        }
        if (taigaPineStep && (isNearTreeFoliage(level, BlockPos.containing(departurePos)) || isNearTreeFoliage(level, roughTarget))) {
            maxDist = Math.max(maxDist, 18.0D + tier * 2.5D); // Boreal Pine-Phasing extended stride through timber
        }
        if (sixteenBlockDashStep) {
            maxDist = Math.max(maxDist, 16.0D + (tier - 1) * 2.5D); // 16m+ surge (Blood, Eclipse, Vapor, Null_R, Null_U, Penumbra, Sylvan, Oblivion, Chimera)
        }
        if (astralCometDash) {
            maxDist = Math.max(maxDist, 18.0D + (tier - 1) * 2.5D); // 18m+ surge (Astral, Rime, Genesis, Prismatic)
        }
        if (twentyBlockApexStep) {
            maxDist = Math.max(maxDist, 20.0D + (tier - 1) * 2.5D); // 20m+ surge (Aether, Entropic, Celestial)
        }
        if (twentyTwoBlockEschatonStep) {
            maxDist = Math.max(maxDist, 22.0D + (tier - 1) * 2.5D); // 22m+ surge (Empyrean, Cataclysm, Terminus)
        }
        if (twentyFourBlockSingularityStep) {
            maxDist = Math.max(maxDist, 24.0D + (tier - 1) * 2.5D); // 24m+ surge (Eschaton, Apotheosis, Entropica)
        }

        Vec3 delta = desiredPos.subtract(departurePos);
        if (delta.length() > maxDist) {
            desiredPos = departurePos.add(delta.normalize().scale(maxDist));
        }

        // Section 5.4 Tiers 1–3 Line-of-Sight Enforcement (Tier 4+ / Void / Earth phase through solid barriers; Miasma phases thin fences/bars!)
        if (!canPhaseWalls) {
            BlockHitResult losHit = level.clip(new ClipContext(
                    caster.getEyePosition(),
                    desiredPos.add(0.0D, 0.8D, 0.0D),
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
                if (!thinBarrier) {
                    desiredPos = Vec3.atBottomCenterOf(losHit.getBlockPos().relative(losHit.getDirection()));
                }
            }
        }

        BlockPos targetBlock = BlockPos.containing(desiredPos);
        if (natureTreeStep) {
            // Scan upward through tree leaves/branches to emerge cleanly atop foliage
            for (int dy = 0; dy <= 6; dy++) {
                BlockPos cand = targetBlock.above(dy);
                if (isTwoBlockClearance(level, cand)) {
                    targetBlock = cand;
                    break;
                }
            }
        } else if (earthBurrowStep) {
            // Scan vertically through stone/dirt strata to emerge safely on open stone/dirt terrain
            for (int dy = 0; dy <= 6; dy++) {
                BlockPos up = targetBlock.above(dy);
                if (isTwoBlockClearance(level, up)) {
                    targetBlock = up;
                    break;
                }
                BlockPos down = targetBlock.below(dy);
                if (isTwoBlockClearance(level, down)) {
                    targetBlock = down;
                    break;
                }
            }
        } else if (!level.getBlockState(targetBlock).getCollisionShape(level, targetBlock).isEmpty()) {
            targetBlock = targetBlock.above();
        }

        // Section 5.4 Raycast Smart-Snap Backward: step backward along transit line to nearest valid 2-block clearance
        if (!isTwoBlockClearance(level, targetBlock)) {
            Vec3 rayVec = desiredPos.subtract(departurePos);
            double totalLen = rayVec.length();
            if (totalLen > 0.5D) {
                Vec3 unit = rayVec.normalize();
                for (double stepBack = 0.75D; stepBack < totalLen; stepBack += 0.75D) {
                    BlockPos cand = BlockPos.containing(desiredPos.subtract(unit.scale(stepBack)));
                    if (isTwoBlockClearance(level, cand)) {
                        targetBlock = cand;
                        break;
                    }
                    if (isTwoBlockClearance(level, cand.above())) {
                        targetBlock = cand.above();
                        break;
                    }
                }
            }
        }

        if (!isTwoBlockClearance(level, targetBlock)) {
            return;
        }

        // Capture downward freefall speed prior to Blink for Forward Momentum Conversion (Section 5.4)
        double freefallSpeed = caster.getDeltaMovement().y < -0.25D ? -caster.getDeltaMovement().y : 0.0D;

        // Clean Stealth Transit by Default (Section 5.4): silent & flash-free on stealth shadow-blinks, 3D Rift on arcane/mutated blinks!
        caster.teleportTo(targetBlock.getX() + 0.5D, targetBlock.getY(), targetBlock.getZ() + 0.5D);
        caster.resetFallDistance();

        if (!silentUmbralStep && materia != EssenceType.ABYSS && materia != EssenceType.NULL_U) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, caster.position(), materia, tier);
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(level, caster.position(), materia, null, 1.0D, tier);
        }

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
            return;
        }

        if (mutated && MateriaFamilyTier.isNetherLineage(materia)) {
            for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(2.5D))) {
                if (nearby != caster && nearby instanceof Monster) {
                    nearby.igniteForSeconds(3);
                }
            }
        }
    }

    private static boolean isTwoBlockClearance(ServerLevel level, BlockPos feet) {
        return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty();
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

    private static boolean isNearMyceliumOrMushrooms(ServerLevel level, BlockPos center) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 2, 3))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.MYCELIUM)
                    || bs.is(Blocks.PODZOL)
                    || bs.is(Blocks.RED_MUSHROOM) || bs.is(Blocks.BROWN_MUSHROOM)
                    || bs.is(Blocks.RED_MUSHROOM_BLOCK) || bs.is(Blocks.BROWN_MUSHROOM_BLOCK)
                    || bs.is(Blocks.MUSHROOM_STEM)
                    || bs.is(Blocks.CRIMSON_NYLIUM) || bs.is(Blocks.WARPED_NYLIUM)
                    || bs.is(Blocks.CRIMSON_FUNGUS) || bs.is(Blocks.WARPED_FUNGUS)) {
                return true;
            }
        }
        return false;
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
        int duration = 120 + tier * 35;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("entropica", "spell_grav_" + level.getGameTime() + "_" + pos.asLong());
        GravityFieldManager.registerField(new GravityField(id, level.dimension(), pos, rad, mode, duration));

        Vec3 center = Vec3.atCenterOf(pos);
        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(level, center.subtract(0.0D, 0.4D, 0.0D), rad, materia, null, Math.min(80, duration), tier);
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
    private static void applySyncedMotion(Entity entity, Vec3 motion) {
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
            BlockHitResult grapHit = raycastBlocks(level, caster, look, 14.0D + tier * 3.0D);
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
        int pulled = 0;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, search, ItemEntity::isAlive)) {
            Vec3 from = item.position();
            item.setNoPickUpDelay();
            item.setPos(caster.getX(), caster.getY() + 0.5D, caster.getZ());
            if (pulled < 8) {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                        level, from, caster.position().add(0.0D, 0.7D, 0.0D), materia, null, tier
                );
            }
            pulled++;
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
