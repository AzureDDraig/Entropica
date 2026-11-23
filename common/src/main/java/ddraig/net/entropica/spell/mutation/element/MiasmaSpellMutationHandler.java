package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellEidolonSpecs;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Miasma Materia (Tier 3 Trinity Fusion #45)</b>
 * (Spellcrafting System Guide Section 6.46 — Trinity Nature + Undead + Water harmonic matrix):
 * <ul>
 *     <li><b>Palette</b>: Sickly Virulent Green ({@code #1A5923}) + Necrotic Olive Slate ({@code #4D6453}) + Stagnant Toxic Teal/Cyan ({@code #225C7C})</li>
 *     <li><b>Advantage</b>: Strong vs <b>FROZEN (+1)</b>, <b>EARTH (+1)</b>, <b>OASIS (+1)</b>, and <b>TAIGA (+1)</b>
 *         (or <b>NETHER</b>, <b>ARID</b>, <b>FROZEN</b>, <b>OASIS</b>, <b>TAIGA</b> under Water Dominance) |
 *         Weak vs <b>UMBRAL (-1)</b>, <b>RADIANT (-1)</b>, and <b>NETHER (-1)</b></li>
 * </ul>
 */
public final class MiasmaSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int VIRULENT_MIASMA_GREEN_HEX_COLOR = 0x1A5923;
    public static final int NECROTIC_OLIVE_SLATE_HEX_COLOR = 0x4D6453;
    public static final int PESTILENT_MIASMA_HEX_COLOR = 0x225C7C;

    private static final String TAG_MIASMA_BEAM_LAST_TICK = "MiasmaBeamLastTick";
    private static final String TAG_MIASMA_BEAM_TICKS = "MiasmaBeamContactTicks";
    private static final String TAG_MIASMA_BEAM_NEXT_SIPHON = "MiasmaBeamNextSiphonTick";
    private static final String TAG_MIASMA_TRANSMUTE_BATCH_TICK = "MiasmaTransmuteBatchTick";

    @Override
    public EssenceType materiaType() {
        return EssenceType.MIASMA;
    }

    // =========================================================================
    // TITLES & CODEX / TOOLTIP DESCRIPTIONS
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.CHAIN_LEAP ? "Virulent Contagion-Chain Flow" : null;
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.CHAIN_LEAP
                ? "Modifies projectile and strike spells so caustic tendrils of stagnant swamp bile (#1A5923 / #4D6453 / #225C7C) leap to up to 4 nearby hostiles within 6m; each chained target takes 60% initial damage, suffers Slowness II, and loses 25% armor toughness for 5.0s."
                : null;
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Putrid Swampland Gas-Pod";
            case BEAM -> "Decomposing Quagmire Siphon Ray & Sinkhole";
            case RADIAL_BURST -> "Putrescent Swamp-Blight Nova";
            case TOUCH_STRIKE -> "Septic Quagmire Palm-Gouge";
            case AURA -> "Putrescent Mire-Carapace";
            case GLYPH -> "Pressurized Septic Gas-Geyser";
            case CONE -> "Choking Bog-Gas Fan";
            case SKYFALL -> "Cataclysmic Miasmic Bog-Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Launches an explosive spore-sac of pressurized swamp gas (#1A5923 / #4D6453 / #225C7C) that knocks hostiles airborne on contact, strips 3 armor points, deals heavy Miasma Materia damage, and blinds enemies in a lingering gas cloud for 4.0s.";
            case BEAM ->
                    "Channels a murky necrotic siphon stream (#1A5923 / #4D6453 / #225C7C) restoring 1 heart every 1.5s to the caster while dragging victims 1.5m/s closer; after 5.0s sustained triggers a Swamp Quagmire Collapse (drags hostiles downward, roots 4.0s & inflicts Wither II).";
            case RADIAL_BURST ->
                    "6m Putrescent Swamp-Blight Nova (#1A5923 / #4D6453 / #225C7C, zero physical light blocks): hurls hostiles 7m outward, shreds 3 armor points, resets mob aggro, and grants the caster and allies Septic Immunity (Poison/Wither/Hunger immunity for 10.0s).";
            case TOUCH_STRIKE ->
                    "Strikes with decomposing swamp muck and necrotic bile (#1A5923 / #4D6453 / #225C7C), dealing heavy Miasma Materia damage, shredding 3 armor points, inflicting Poison II for 6.0s, and splashing a 3m bog sludge puddle (2.5s root & +25% incoming damage).";
            case AURA ->
                    "Putrescent Mire-Carapace (#1A5923 / #4D6453 / #225C7C): +7 Armor, +3 Toughness, full immunity to Poison, Wither, Hunger, and Drowning, reflects 35% melee damage (-2 attacker armor), and solidifies water/mud underfoot into temporary bog turf.";
            case GLYPH ->
                    "Disarmable Single/Repeating Pressurized Septic Gas-Geyser (#1A5923 / #4D6453 / #225C7C): erupts when triggered by hostiles, launching enemies 8m airborne, clearing all positive potion buffs, and inflicting +50% fall damage vulnerability for 6.0s.";
            case CONE ->
                    "Choking Bog-Gas Fan (#1A5923 / #4D6453 / #225C7C): knocks back incoming projectiles, drains 2 hunger bars from hostile players, deals continuous Miasma Materia damage, and inflicts Miasmic Asphyxiation (25.0s AI pathfinding disruption & 3.0s attack interrupts).";
            case SKYFALL ->
                    "Extended 18.0s Cataclysmic Miasmic Bog-Downpour across a 12m zone (#1A5923 / #4D6453 / #225C7C): rains 3D septic sludge boulders & putrid bog water that knock hostiles flat, shred 5 armor, and coat the ground in a 60%-slowing poisoning quagmire for 18s + 8s after.";
            case SUMMON_AGENT ->
                    "Septic Bog-Stalker (45s lifecycle / 65 HP): predatory swamp beast that submerges into mud to ambush hostiles from behind, dealing heavy Miasma Materia damage, siphoning 2 hearts per strike to itself or the caster, and blinding targets for 3.0s.";
            case SUMMON_SENTRY ->
                    "Cadaverous Geyser Pylon (45s lifecycle / 80 HP): anchored fungal geyser pylon erupting boiling swamp acid every 3.0s in a 6m radius, launching enemies airborne, shredding 2 armor points, and purging all positive potion buffs.";
            case SUMMON_DECOY ->
                    "Decomposing Bog-Husk (12s duration): 12m taunt mud-and-bone swamp effigy that absorbs damage and violently detonates into blinding swamp gas & toxic sludge (-3 armor, Poison II & 4.0s Blindness).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Putrid Miasma Phase-Shift";
            case TRANSMUTE, BREAK -> "Virulent Quagmire & Peat Fusion";
            case AFFLICTION -> "Virulent Septic Pestilence Hex";
            default -> "Putrescent Miasma " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Dissolves into foul necrotic marsh vapor (#1A5923 / #4D6453 / #225C7C), phasing up to 14m through enemies and thin barriers (fences, iron bars); passed-through hostiles lose target lock, cough uncontrollably, and take Miasma Materia damage for 3.0s while shedding mob aggro.";
            case TRANSMUTE, BREAK ->
                    "Requires 5.0s continuous channeling: transforms dirt/grass/mud into fertile Bog Peat (3x mushroom & nether wart growth + double harvest drops), converts standing water into Algae-Choked Swamp Water (bottles into Poison II), and turns rotten flesh/spider eyes into slime balls & bone meal (+25% yield).";
            case AFFLICTION ->
                    "Parasitic swamp plague cutting Max Health by 25%, locking natural health regeneration, shredding 35% armor toughness, and forcing victims to vomit caustic swamp bile every 3.5s (1 heart necrotic damage + 2m slowing puddle); Pestilence Pheromone Trail lures slimes, witches & arthropods.";
            default -> "Infuses the effect with putrescent Miasma Materia resonance (#1A5923 / #4D6453 / #225C7C).";
        };
    }

    // =========================================================================
    // PART 1 — FLOW MUTATIONS (VIRULENT CONTAGION-CHAIN FLOW)
    // =========================================================================

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromTarget,
            LivingEntity toTarget,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.46 Virulent Contagion-Chain Flow:
        // Leaps to up to 4 nearby hostiles within 6m (60% initial damage handled in SpellExecutionEngine),
        // applying Slowness II and -25% armor toughness for 5.0s (100t)!
        toTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
        SpellCombatReactionEngine.applyPercentageArmorShred(toTarget, level, 0.0D, 0.25D, 100L);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), VIRULENT_MIASMA_GREEN_HEX_COLOR, 20, 0.90F, 0.06F),
                toTarget.getX(), toTarget.getY(0.8D), toTarget.getZ(), 8, 0.25D, 0.30D, 0.25D, 0.03D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_ACID.get(), PESTILENT_MIASMA_HEX_COLOR, 18, 0.85F, 0.06F),
                toTarget.getX(), toTarget.getY(0.65D), toTarget.getZ(), 6, 0.22D, 0.25D, 0.22D, 0.03D
        );
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), VIRULENT_MIASMA_GREEN_HEX_COLOR, 16, 0.85F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_ACID.get(), PESTILENT_MIASMA_HEX_COLOR, 16, 0.80F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
        }
    }

    // =========================================================================
    // PART 2 — DELIVERY FORM MUTATIONS
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        if (hitEntity instanceof LivingEntity directVictim && directVictim.isAlive()
                && !slot.hasHarmVerb()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(directVictim, caster, wardSafe))) {
            SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(
                    level, directVictim, caster, 3.85F + tier * 0.95F, false, true
            );
        }
        detonateSwampGasPod(level, caster, hitEntity.position(), hitEntity, spell, slot);
    }

    @Override
    public void onProjectileHitBlockFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        detonateSwampGasPod(level, caster, blockHit.getLocation(), null, spell, slot);
    }

    private static void detonateSwampGasPod(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            @Nullable Entity directHit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.46 Putrid Swampland Gas-Pod:
        // Knocks hostiles airborne in a noxious green-cyan blast, strips 3 armor points, deals heavy Miasma Materia
        // damage (+1 vs Frozen, Earth, Oasis, Taiga), and blinds enemies in a lingering gas cloud for 4.0s (80t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, center.add(0.0D, 0.2D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.MIASMA, EssenceType.UNDEAD, 1.75F, tier
        );
        SpellWorldVfxManager.broadcastRadialNova(level, center, 3.5D, EssenceType.MIASMA, EssenceType.WATER, tier);

        float splashDmg = 3.20F + tier * 0.80F;
        AABB box = new AABB(
                center.x - 3.5D, center.y - 1.5D, center.z - 3.5D,
                center.x + 3.5D, center.y + 2.6D, center.z + 3.5D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (caster != null && isFriendlyTarget(foe, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.4D, 0.70D, foe.getDeltaMovement().z * 0.4D);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 140L);
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            if (foe != directHit) {
                SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(level, foe, caster, splashDmg, false, true);
            }
        }
        level.sendParticles(ParticleTypes.SNEEZE, center.x, center.y + 0.6D, center.z, 22, 1.4D, 0.5D, 1.4D, 0.04D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.90F, 0.72F);

        // 4.0s (80t) lingering blinding swamp gas cloud!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, 3.2D, 80, level.getGameTime(), spell, slot, EssenceType.MIASMA, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 12 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB gasBox = new AABB(
                                zone.center.x - 3.2D, zone.center.y - 1.0D, zone.center.z - 3.2D,
                                zone.center.x + 3.2D, zone.center.y + 2.2D, zone.center.z + 3.2D
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, gasBox, e -> e.isAlive() && e != owner)) {
                            if (owner != null && isFriendlyTarget(foe, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zSafe)) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                        }
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, zone.center.x, zone.center.y + 0.5D, zone.center.z, 10, 1.2D, 0.35D, 1.2D, 0.02D);
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 2 — DECOMPOSING QUAGMIRE SIPHON RAY & SINKHOLE (BEAM)
    // -------------------------------------------------------------------------

    @Override
    public void onBeamFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double maxReach,
            List<LivingEntity> hitEntities,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        onBeamFormCast(level, caster, direction, maxReach, hitEntities, blockHit, spell, slot, fallbackMateria, apparatusPotency, 0);
    }

    @Override
    public void onBeamFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double maxReach,
            List<LivingEntity> hitEntities,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency,
            int heldTicks
    ) {
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        float beamDmg = (2.45F + tier * 0.65F) * Math.max(0.5F, apparatusPotency);
        boolean healedThisCast = false;

        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            Vec3 toCaster = caster.position().subtract(hit.position());
            if (toCaster.lengthSqr() > 1.0E-4D) {
                Vec3 pull = toCaster.normalize().scale(0.30D); // ~1.5m/s drag toward caster!
                hit.setDeltaMovement(pull.x, 0.04D, pull.z);
                hit.hasImpulse = true;
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(level, hit, caster, beamDmg, false, true);
            }

            if (!healedThisCast && caster.isAlive()) {
                CompoundTag cTag = EntityHelper.getPersistentData(caster);
                if (now >= cTag.getLong(TAG_MIASMA_BEAM_NEXT_SIPHON).orElse(0L)) {
                    cTag.putLong(TAG_MIASMA_BEAM_NEXT_SIPHON, now + 30L); // 1 heart every 1.5s (30t)!
                    SpellCombatReactionEngine.notifyMagicalHeal(caster, 2.0F);
                    caster.heal(2.0F);
                    healedThisCast = true;
                }
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_MIASMA_BEAM_LAST_TICK).orElse(-100L);
            if (last == now) {
                continue;
            }
            long delta = now - last;
            int contactTicks = heldTicks <= 0
                    ? 0
                    : ((delta > 0L && delta <= 16L) ? data.getInt(TAG_MIASMA_BEAM_TICKS).orElse(0) + (int) delta : 4);
            data.putLong(TAG_MIASMA_BEAM_LAST_TICK, now);
            data.putInt(TAG_MIASMA_BEAM_TICKS, contactTicks);

            if (heldTicks > 0 && contactTicks >= 100) {
                data.putInt(TAG_MIASMA_BEAM_TICKS, 0);
                SpellWorldVfxManager.broadcastRadialNova(level, hit.position(), 3.8D, EssenceType.MIASMA, EssenceType.UNDEAD, tier);
                SpellWorldVfxManager.broadcastArcaneSeal(level, hit.position().add(0.0D, 0.04D, 0.0D), 3.8D, EssenceType.MIASMA, EssenceType.EARTH, 80, tier);
                float collapseDmg = (9.50F + tier * 2.35F) * Math.max(0.5F, apparatusPotency);
                for (LivingEntity caught : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(3.8D), e -> e.isAlive() && e != caster)) {
                    if (isFriendlyTarget(caught, caster)) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(caught, caster, wardSafe)) continue;
                    caught.setDeltaMovement(0.0D, -0.48D, 0.0D);
                    caught.hasImpulse = true;
                    caught.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
                    caught.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                    caught.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                    SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(level, caught, caster, collapseDmg, false, true);
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.133F, 0.361F, 0.486F),
                        hit.getX(), hit.getY(0.8D), hit.getZ(), 1, 0, 0, 0, 0);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()),
                        hit.getX(), hit.getY(0.3D), hit.getZ(), 24, 1.4D, 0.25D, 1.4D, 0.08D);
                level.playSound(null, hit.blockPosition(), SoundEvents.MUD_BREAK, SoundSource.PLAYERS, 1.0F, 0.65F);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 3 — PUTRESCENT SWAMP-BLIGHT NOVA (RADIAL BURST)
    // -------------------------------------------------------------------------

    @Override
    public void onRadialBurstFormCast(
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
        // Section 6.46 Putrescent Swamp-Blight Nova:
        // 6m radius, shaders/bloom only without chunk light recalculations (zero physical light blocks);
        // hurls hostiles 7m outward, shreds 3 armor points, resets mob aggro, deals Miasma Materia damage,
        // and grants caster & allies Septic Immunity (complete Poison, Wither & Hunger immunity for 10.0s / 200t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        double r = Math.max(6.0D, radius);
        long septicUntil = level.getGameTime() + 200L;

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.MIASMA, EssenceType.UNDEAD, tier);
        grantSepticImmunity(caster, septicUntil);

        float novaDmg = (4.10F + tier * 1.00F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(
                center.x - r, center.y - 2.0D, center.z - r,
                center.x + r, center.y + 3.5D, center.z + r
        );
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ent == caster || isFriendlyTarget(ent, caster)) {
                grantSepticImmunity(ent, septicUntil);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) continue;

            Vec3 delta = ent.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.65D) : new Vec3(0.0D, 0.38D, 0.0D);
            ent.setDeltaMovement(away.x, 0.38D, away.z); // 7m outward hurl!
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 3.0D, 0.0D, 160L);
            ent.setLastHurtByMob(null);
            if (ent instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(level, ent, caster, novaDmg, false, true);
            }
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.133F, 0.361F, 0.486F),
                center.x, center.y + 0.9D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.SNEEZE, center.x, center.y + 0.9D, center.z, 36, 2.3D, 0.6D, 2.3D, 0.05D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.95F, 0.68F);
    }

    private static void grantSepticImmunity(LivingEntity ally, long septicUntil) {
        EntityHelper.getPersistentData(ally).putLong(SpellLingeringZoneManager.TAG_MIASMA_SEPTIC_IMMUNE_UNTIL, septicUntil);
        ally.removeEffect(MobEffects.POISON);
        ally.removeEffect(MobEffects.WITHER);
        ally.removeEffect(MobEffects.HUNGER);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 4 — SEPTIC QUAGMIRE PALM-GOUGE (TOUCH / STRIKE)
    // -------------------------------------------------------------------------

    @Override
    public void onTouchStrikeFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            List<LivingEntity> hitEntities,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Section 6.46 Septic Quagmire Palm-Gouge:
        // Heavy Miasma Materia damage, shreds 3 armor points, inflicts Poison II for 6.0s (120t),
        // and splashes a 3m bubbling bog sludge puddle rooting victims 2.5s (50t) and causing +25% increased damage!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.3D, 0.0D), dirNorm, 2.8D,
                EssenceType.MIASMA, EssenceType.UNDEAD, tier
        );

        Vec3 puddlePos = hitEntities.isEmpty() ? caster.position().add(dirNorm.scale(2.0D)) : hitEntities.getFirst().position();
        float gougeDmg = (4.20F + tier * 1.05F) * Math.max(0.5F, apparatusPotency);
        long nowInit = level.getGameTime();

        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL, nowInit + 50L);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(level, hit, caster, gougeDmg, true, true);
            }
        }

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, puddlePos.add(0.0D, 0.04D, 0.0D), 3.0D, EssenceType.MIASMA, EssenceType.WATER, 60, tier
        );
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, puddlePos, 3.0D, 60, nowInit, spell, slot, EssenceType.MIASMA, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        long now = srv.getGameTime();
                        AABB box = new AABB(
                                zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                                zone.center.x + 3.0D, zone.center.y + 1.8D, zone.center.z + 3.0D
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && isFriendlyTarget(foe, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zSafe)) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL, now + 50L);
                        }
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()),
                                zone.center.x, zone.center.y + 0.15D, zone.center.z, 8, 1.1D, 0.1D, 1.1D, 0.03D);
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 5 — PUTRESCENT MIRE-CARAPACE (AURA / SELF)
    // -------------------------------------------------------------------------

    @Override
    public void onAuraFormCast(
            ServerLevel level,
            LivingEntity caster,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Section 6.46 Putrescent Mire-Carapace:
        // +7 Armor, +3 Toughness, full Poison/Wither/Hunger/Drowning immunity, 35% melee damage reflection,
        // -2 attacker armor shred, and solidifies water/mud underfoot into temporary bog turf!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_MIASMA_CARAPACE_UNTIL, level.getGameTime() + dur);
        caster.removeEffect(MobEffects.POISON);
        caster.removeEffect(MobEffects.WITHER);
        caster.removeEffect(MobEffects.HUNGER);
        caster.setAirSupply(caster.getMaxAirSupply());
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 7.0D, 3.0D, 0.0D, 0.0D, dur);
        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur + 40, 0, false, false));
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, caster.getX(), caster.getY(0.9D), caster.getZ(), 22, 0.45D, 0.55D, 0.45D, 0.03D);
        level.playSound(null, caster.blockPosition(), SoundEvents.MUD_PLACE, SoundSource.PLAYERS, 0.90F, 0.78F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 6 — PRESSURIZED SEPTIC GAS-GEYSER (GLYPH / TRAP)
    // -------------------------------------------------------------------------

    @Override
    public boolean onGlyphFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 glyphCenter,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Section 6.46 Pressurized Septic Gas-Geyser:
        // Disarmable Single/Repeating pressure rune etched in necrotic swamp runes (#1A5923 / #4D6453 / #225C7C);
        // violently erupts when triggered by hostiles, launching enemies 8m airborne, clearing all active positive
        // potion buffs, and inflicting +50% fall damage vulnerability for 6.0s (120t)!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.05D, 0.0D), trapRad, EssenceType.MIASMA, EssenceType.UNDEAD, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.MIASMA, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.05D, 0.0D), zone.radius, EssenceType.MIASMA, EssenceType.UNDEAD, rem, tier
                        );
                    }
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, zone.center.x, zone.center.y + 0.12D, zone.center.z, 4, 0.7D, 0.05D, 0.7D, 0.01D);
                    }

                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    AABB trigger = new AABB(
                            zone.center.x - 2.8D, zone.center.y - 1.0D, zone.center.z - 2.8D,
                            zone.center.x + 2.8D, zone.center.y + 2.4D, zone.center.z + 2.8D
                    );
                    List<LivingEntity> foes = srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner
                            && (owner == null || !isFriendlyTarget(e, owner))
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)));
                    if (foes.isEmpty()) return true;

                    triggersLeft[0]--;
                    rearmCooldown[0] = 50;

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 3.6D, EssenceType.MIASMA, EssenceType.WATER, tier);
                    long fallVulnUntil = srv.getGameTime() + 120L;
                    float geyserDmg = (4.15F + tier * 1.00F) * Math.max(0.5F, zone.apparatusPotency);
                    for (LivingEntity v : foes) {
                        purgeBeneficialBuffs(v);
                        EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_FALL_VULN_50_UNTIL, fallVulnUntil);
                        v.setDeltaMovement(v.getDeltaMovement().x * 0.3D, 1.22D, v.getDeltaMovement().z * 0.3D); // 8m airborne launch!
                        v.hasImpulse = true;
                        SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(srv, v, owner, geyserDmg, false, true);
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, EssenceType.MIASMA, zone.apparatusPotency
                            );
                        }
                    }
                    srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 0.6D, zone.center.z, 36, 1.2D, 0.8D, 1.2D, 0.12D);
                    srv.sendParticles(ParticleTypes.SNEEZE, zone.center.x, zone.center.y + 0.8D, zone.center.z, 18, 1.0D, 0.6D, 1.0D, 0.05D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 0.72F);
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();

        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    public static void purgeBeneficialBuffs(LivingEntity target) {
        List<MobEffectInstance> copy = new ArrayList<>(target.getActiveEffects());
        for (MobEffectInstance inst : copy) {
            if (inst.getEffect().value().isBeneficial()) {
                target.removeEffect(inst.getEffect());
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 7 — CHOKING BOG-GAS FAN (CONE / SPRAY)
    // -------------------------------------------------------------------------

    @Override
    public void onConeFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double coneRange,
            List<LivingEntity> hitTargets,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Section 6.46 Choking Bog-Gas Fan:
        // Knocks back incoming projectiles, drains 2 hunger bars (4 food) from hostile players, deals continuous
        // Miasma Materia damage, and inflicts Miasmic Asphyxiation (25.0s / 500t AI pathfinding disruption &
        // attack animation interrupts every 3.0s)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), dirNorm, coneRange,
                EssenceType.MIASMA, EssenceType.UNDEAD, tier
        );

        AABB projCone = new AABB(caster.getEyePosition(), caster.getEyePosition().add(dirNorm.scale(coneRange))).inflate(2.5D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, projCone, p -> p.getOwner() != caster && !(p instanceof SpellProjectileEntity))) {
            if (proj.getOwner() instanceof LivingEntity pOwner && isFriendlyTarget(pOwner, caster)) continue;
            proj.setDeltaMovement(dirNorm.scale(Math.max(1.25D, proj.getDeltaMovement().length())));
            proj.hasImpulse = true;
        }

        long asphyxiaUntil = level.getGameTime() + 500L;
        float coneDmg = (3.15F + tier * 0.80F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (isFriendlyTarget(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            hit.stopUsingItem();
            if (hit instanceof Player p) {
                p.getFoodData().setFoodLevel(Math.max(0, p.getFoodData().getFoodLevel() - 4));
            }
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_MIASMA_ASPHYXIA_UNTIL, asphyxiaUntil);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 8 — CATACLYSMIC MIASMIC BOG-DOWNPOUR (SKYFALL)
    // -------------------------------------------------------------------------

    @Override
    public boolean onSkyfallFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 impactCenter,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Section 6.46 Cataclysmic Miasmic Bog-Downpour:
        // Extended 18.0s (360t) torrential downpour across a 12m+ zone + 8.0s (160t) lingering bubbling quagmire (520t total);
        // rains 8 falling 3D Skyfall Pseudoblock heavy septic sludge boulders alongside putrid bog water & virulent smog,
        // knocking hostiles flat, shredding 5 armor points, dealing heavy Miasma Materia damage, slowing movement by 60%,
        // and continuously poisoning any hostile inside across all 520t!
        int tier = spell.getEffectiveTier();
        double r = Math.max(12.0D, radius);
        BlockPos centerPos = BlockPos.containing(impactCenter);
        double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.MIASMA, EssenceType.UNDEAD, 365, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.05D, 0.0D), r, EssenceType.MIASMA, EssenceType.WATER, 520, tier);

        final int boulderCount = 8;
        final BlockPos[] boulderTargets = new BlockPos[boulderCount];
        boulderTargets[0] = findGroundSurfaceNear(level, centerPos).above();
        for (int i = 1; i < boulderCount; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / (boulderCount - 1.0D)) + level.random.nextDouble() * 0.40D;
            double dist = 0.20D * r + level.random.nextDouble() * (0.60D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            boulderTargets[i] = findGroundSurfaceNear(level, raw).above();
        }

        final boolean[] landed = new boolean[boulderCount];
        final boolean[] materialized = {false};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, EssenceType.MIASMA, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int mIdx = 0; mIdx < boulderCount; mIdx++) {
                                boolean peatCore = (mIdx % 2 == 0);
                                BlockState pseudoState = peatCore
                                        ? Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState()
                                        : Blocks.MUD.defaultBlockState();
                                int colHeight = peatCore ? 2 : 1;
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, boulderTargets[mIdx], pseudoState, colHeight);
                            }
                        }
                        return false;
                    }

                    boolean stormActive = elapsed < 360;
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (stormActive) {
                        // 1. 8 falling 3D heavy septic sludge boulder pseudoblock columns!
                        for (int mIdx = 0; mIdx < boulderCount; mIdx++) {
                            int impactTick = 20 + mIdx * 42;
                            BlockPos strikePos = boulderTargets[mIdx];
                            boolean peatCore = (mIdx % 2 == 0);
                            BlockState pseudoState = peatCore
                                    ? Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState()
                                    : Blocks.MUD.defaultBlockState();
                            int colHeight = peatCore ? 2 : 1;

                            if (elapsed == impactTick - 14) {
                                int holdTicks = Math.max(4, zone.maxDurationTicks - impactTick);
                                SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                                        srv,
                                        new Vec3(strikePos.getX() + 0.5D, strikePos.getY() + skyH, strikePos.getZ() + 0.5D),
                                        strikePos,
                                        pseudoState,
                                        colHeight,
                                        14,
                                        holdTicks,
                                        EssenceType.MIASMA,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.MIASMA, EssenceType.UNDEAD, 2.25F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 20, 1.0D, 0.5D, 1.0D, 0.09D
                                );
                                srv.playSound(null, strikePos, SoundEvents.MUD_BREAK, SoundSource.PLAYERS, 0.90F, 0.68F);
                            }
                        }

                        // 2. Torrential putrid bog-water & virulent smog bursts!
                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 strike = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, strike, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.MIASMA, EssenceType.WATER, 1.45F, zTier
                            );
                            srv.sendParticles(ParticleTypes.SNEEZE, strike.x, strike.y + 0.8D, strike.z, 8, 0.7D, 0.6D, 0.7D, 0.04D);
                        }
                    }
                    for (int mIdx = 0; mIdx < boulderCount; mIdx++) {
                        if (landed[mIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, boulderTargets[mIdx], (mIdx % 2 == 0) ? 2 : 1, false);
                        }
                    }

                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                                zone.center.x, zone.center.y + (stormActive ? 3.5D : 0.35D), zone.center.z,
                                22, zone.radius * 0.55D, stormActive ? 1.8D : 0.2D, zone.radius * 0.55D, 0.04D);
                    }

                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float stormDmg = (3.55F + zTier * 0.88F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && isFriendlyTarget(foe, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                            // 60% bubbling quagmire slow & continuous Poison II across all 520t (18s + 8s)!
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.60D, 35L);
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3));
                            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 160L);
                                foe.setDeltaMovement(foe.getDeltaMovement().x * 0.3D, -0.38D, foe.getDeltaMovement().z * 0.3D);
                                foe.hasImpulse = true;
                                SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(srv, foe, owner, stormDmg, false, true);
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.MIASMA, zone.apparatusPotency * 0.45F
                                    );
                                }
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 9 — PESTILENT SWAMPLORD TRIAD (SUMMON / EIDOLON)
    // -------------------------------------------------------------------------

    @Override
    public void onSummonFormSpawned(
            ServerLevel level,
            LivingEntity caster,
            Entity summonEntity,
            Vec3 spawnPos,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.MIASMA.name());
        tag.putInt("SpellEidolonTintRGB", PESTILENT_MIASMA_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.8D, radius * 0.55D), EssenceType.MIASMA, EssenceType.UNDEAD, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.04D);
    }

    // =========================================================================
    // PART 3 — CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.46 Putrid Miasma Phase-Shift:
        // Dissolves into foul necrotic marsh vapor (#1A5923 / #4D6453 / #225C7C), phasing forward up to 14m through
        // enemies & thin barriers (fences, iron bars); passed-through hostiles lose target lock, cough uncontrollably,
        // and take heavy Miasma Materia damage over 3.0s (60t) while completely shedding hostile mob aggro!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.MIASMA, potencyTier);

        AABB path = new AABB(departurePos, arrivalPos).inflate(1.5D);
        List<LivingEntity> coughed = new ArrayList<>();
        for (LivingEntity passed : level.getEntitiesOfClass(LivingEntity.class, path, e -> e.isAlive() && e != caster)) {
            if (isFriendlyTarget(passed, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(passed, caster, true)) continue;
            passed.stopUsingItem();
            passed.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
            passed.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
            passed.setLastHurtByMob(null);
            if (passed instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
            SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(level, passed, caster, 3.40F + potencyTier * 0.90F, false, true);
            coughed.add(passed);
        }

        // Sustain 3.0s (60t) uncontrollable coughing & Miasma damage on passed-through foes while keeping aggro shed!
        if (!coughed.isEmpty()) {
            List<java.util.UUID> victimIds = coughed.stream().map(Entity::getUUID).toList();
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, arrivalPos, 4.0D, 60, level.getGameTime(), null, null, EssenceType.MIASMA, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        if (elapsed == 20 || elapsed == 40 || elapsed == 60) {
                            LivingEntity owner = zone.resolveCaster(srv);
                            for (java.util.UUID id : victimIds) {
                                if (srv.getEntity(id) instanceof LivingEntity foe && foe.isAlive()) {
                                    foe.stopUsingItem();
                                    SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(
                                            srv, foe, owner, 2.00F + potencyTier * 0.55F, false, true
                                    );
                                    foe.setLastHurtByMob(null);
                                    if (foe instanceof Mob mob) {
                                        mob.setTarget(null);
                                        mob.setAggressive(false);
                                    }
                                    srv.sendParticles(ParticleTypes.SNEEZE, foe.getX(), foe.getY(0.85D), foe.getZ(), 6, 0.25D, 0.25D, 0.25D, 0.02D);
                                }
                            }
                        }
                        return true;
                    }
            ));
        }

        for (Mob mob : level.getEntitiesOfClass(Mob.class, path.inflate(16.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
        }
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, departurePos.x, departurePos.y + 0.8D, departurePos.z, 18, 0.4D, 0.4D, 0.4D, 0.04D);
        level.sendParticles(ParticleTypes.SNEEZE, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 18, 0.4D, 0.4D, 0.4D, 0.04D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.75F, 0.65F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.46 Virulent Quagmire & Peat Fusion [5.0s Continuous Catalytic Channel]:
        // Transforms dirt, grass, and mud into fertile Bog Peat / Toxic Silt (tripling mushroom & nether wart growth
        // speed and yielding double drops upon harvest); converts standing water into Algae-Choked Swamp Water
        // (bottling into Potions of Poison II); and transmutes rotten flesh & spider eyes into slime balls & bone meal (+25% bonus)!
        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 5, potencyTier, EssenceType.MIASMA, "Virulent Quagmire & Peat Fusion", null)) {
            return true;
        }

        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D), ItemEntity::isAlive)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            if (didAny) refundBatchDiscount(level, caster, potencyTier);
            return didAny;
        }

        if (state.is(BlockTags.DIRT) || state.is(Blocks.MUD) || state.is(Blocks.SOUL_SAND)) {
            Block peatBlock = state.is(Blocks.SOUL_SAND)
                    ? Blocks.SOUL_SAND
                    : (level.random.nextBoolean() ? Blocks.MUDDY_MANGROVE_ROOTS : Blocks.PODZOL);
            if (!state.is(peatBlock)) {
                level.setBlockAndUpdate(pos, peatBlock.defaultBlockState());
            }
            registerFertileBogPeatZone(level, caster, pos.immutable());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.WATER)) {
            if (level.getBlockState(pos.above()).isAir()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, pos.above(), level.getBlockState(pos.above()))) {
                level.setBlockAndUpdate(pos.above(), Blocks.LILY_PAD.defaultBlockState());
            }
            registerAlgaeSwampWaterZone(level, caster, pos.immutable(), potencyTier);
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (didAny) {
            refundBatchDiscount(level, caster, potencyTier);
        }
        return didAny;
    }

    private static void registerFertileBogPeatZone(ServerLevel level, @Nullable LivingEntity caster, BlockPos peatPos) {
        final Block[] trackedCrop = {Blocks.AIR};
        final boolean[] trackedMatureWart = {false};
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, Vec3.atCenterOf(peatPos), 1.5D, 2400, level.getGameTime(), null, null, EssenceType.MIASMA, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    BlockPos cropPos = peatPos.above();
                    BlockState above = srv.getBlockState(cropPos);

                    // Double-drop harvest reward if a tracked mushroom or mature nether wart was just harvested!
                    if (above.isAir() && trackedCrop[0] != Blocks.AIR) {
                        if (trackedCrop[0] == Blocks.RED_MUSHROOM) {
                            srv.addFreshEntity(new ItemEntity(srv, cropPos.getX() + 0.5D, cropPos.getY() + 0.2D, cropPos.getZ() + 0.5D,
                                    new ItemStack(Items.RED_MUSHROOM, 1)));
                        } else if (trackedCrop[0] == Blocks.BROWN_MUSHROOM) {
                            srv.addFreshEntity(new ItemEntity(srv, cropPos.getX() + 0.5D, cropPos.getY() + 0.2D, cropPos.getZ() + 0.5D,
                                    new ItemStack(Items.BROWN_MUSHROOM, 1)));
                        } else if (trackedCrop[0] == Blocks.NETHER_WART && trackedMatureWart[0]) {
                            srv.addFreshEntity(new ItemEntity(srv, cropPos.getX() + 0.5D, cropPos.getY() + 0.2D, cropPos.getZ() + 0.5D,
                                    new ItemStack(Items.NETHER_WART, 2 + srv.random.nextInt(2))));
                        }
                        trackedCrop[0] = Blocks.AIR;
                        trackedMatureWart[0] = false;
                    }

                    if (above.is(Blocks.RED_MUSHROOM) || above.is(Blocks.BROWN_MUSHROOM)) {
                        trackedCrop[0] = above.getBlock();
                        if (elapsed % 80 == 0) {
                            BlockPos spread = cropPos.offset(srv.random.nextInt(3) - 1, 0, srv.random.nextInt(3) - 1);
                            if (srv.getBlockState(spread).isAir() && above.canSurvive(srv, spread)) {
                                srv.setBlockAndUpdate(spread, above);
                            }
                        }
                    } else if (above.is(Blocks.NETHER_WART) && above.hasProperty(BlockStateProperties.AGE_3)) {
                        trackedCrop[0] = Blocks.NETHER_WART;
                        int age = above.getValue(BlockStateProperties.AGE_3);
                        trackedMatureWart[0] = (age >= 3);
                        if (age < 3 && elapsed % 40 == 0) {
                            int nextAge = Math.min(3, age + 1);
                            srv.setBlockAndUpdate(cropPos, above.setValue(BlockStateProperties.AGE_3, nextAge));
                            trackedMatureWart[0] = (nextAge >= 3);
                        }
                    }
                    if (elapsed % 30 == 0) {
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, peatPos.getX() + 0.5D, peatPos.getY() + 1.05D, peatPos.getZ() + 0.5D,
                                2, 0.25D, 0.05D, 0.25D, 0.01D);
                    }
                    return true;
                }
        ));
    }

    private static void registerAlgaeSwampWaterZone(ServerLevel level, @Nullable LivingEntity caster, BlockPos waterPos, int tier) {
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, Vec3.atCenterOf(waterPos), 1.8D, 1200, level.getGameTime(), null, null, EssenceType.MIASMA, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || !srv.getBlockState(waterPos).is(Blocks.WATER)) return false;
                    if (elapsed % 15 == 0) {
                        for (ItemEntity item : srv.getEntitiesOfClass(ItemEntity.class, new AABB(waterPos).inflate(1.2D), ItemEntity::isAlive)) {
                            ItemStack st = item.getItem();
                            if (st.is(Items.GLASS_BOTTLE)) {
                                item.setItem(PotionContents.createItemStack(Items.POTION, Potions.STRONG_POISON).copyWithCount(st.getCount()));
                                srv.playSound(null, waterPos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.75F, 0.85F);
                            }
                        }
                        srv.sendParticles(ParticleTypes.SNEEZE, waterPos.getX() + 0.5D, waterPos.getY() + 0.95D, waterPos.getZ() + 0.5D,
                                2, 0.3D, 0.05D, 0.3D, 0.01D);
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.SPIDER_EYE)) {
            int boosted = Math.min(64, Math.max(stack.getCount() + 1, Math.round(stack.getCount() * 1.25F)));
            itemEntity.setItem(new ItemStack(Items.SLIME_BALL, boosted));
            level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    new ItemStack(Items.BONE_MEAL, Math.max(1, boosted / 2))));
            return true;
        }
        if (stack.is(Items.GLASS_BOTTLE)) {
            itemEntity.setItem(PotionContents.createItemStack(Items.POTION, Potions.STRONG_POISON).copyWithCount(stack.getCount()));
            return true;
        }
        return false;
    }

    private static void refundBatchDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (caster instanceof Player player) {
            CompoundTag pData = EntityHelper.getPersistentData(player);
            long now = level.getGameTime();
            if (pData.getLong(TAG_MIASMA_TRANSMUTE_BATCH_TICK).orElse(-10L) != now) {
                pData.putLong(TAG_MIASMA_TRANSMUTE_BATCH_TICK, now);
                long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
                SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.MIASMA, refund);
            }
        }
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.46 Virulent Septic Pestilence Hex:
        // Cuts maximum health by 25% & locks natural regeneration (enforced in SpellCombatReactionEngine),
        // shreds 35% armor toughness, deals continuous Miasma Materia plague ticks, and every 3.5s (70t) causes
        // the victim to vomit caustic swamp bile (1 heart / 2.0 HP necrotic damage + 2m slowing puddle)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 45L);

        LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float plagueDmg = (1.50F + potencyTier * 0.48F) * mult;
        SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(level, target, caster, plagueDmg, false, true);

        if ((now % 70L) < 20L && (now % 20L) == 0L) {
            SpellCombatReactionEngine.hurtWithMiasmaMateriaAdvantage(level, target, caster, 2.0F * mult, false, true);
            for (LivingEntity puddleVictim : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.0D), LivingEntity::isAlive)) {
                if (caster != null && isFriendlyTarget(puddleVictim, caster)) continue;
                if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(puddleVictim, caster, true)) continue;
                puddleVictim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
            }
            level.sendParticles(ParticleTypes.SNEEZE, target.getX(), target.getY(0.75D), target.getZ(), 10, 0.45D, 0.20D, 0.45D, 0.02D);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY(0.25D), target.getZ(), 12, 0.90D, 0.12D, 0.90D, 0.02D);
        }
        return true;
    }

    private static boolean isFriendlyTarget(LivingEntity entity, LivingEntity caster) {
        return SpellExecutionEngine.isFriendlyAllyForOasis(caster, entity)
                || (entity instanceof SpellEidolonEntity eid && caster.getUUID().equals(eid.getOwnerUuid()));
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos origin) {
        for (int dy = 2; dy >= -6; dy--) {
            BlockPos check = origin.offset(0, dy, 0);
            BlockState ground = level.getBlockState(check);
            if (!ground.getCollisionShape(level, check).isEmpty() && level.getBlockState(check.above()).isAir()) {
                return check;
            }
        }
        return origin.below();
    }
}
