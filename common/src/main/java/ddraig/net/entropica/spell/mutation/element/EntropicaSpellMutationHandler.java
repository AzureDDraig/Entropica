package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Entropica Materia (Tier 6 Singularity Fusion #57)</b>
 * (Spellcrafting System Guide Section 6.59 — Apotheosis + Ascended Entropic:
 * <i>"The Grand Cosmic Equilibrium Singularity"</i>).
 *
 * <p>Palette: Shifting Chrono-Silver ({@code #E0E5EC}), Deep Event-Horizon Indigo ({@code #1A153B}),
 * and Transcendent Genesis Gold ({@code #FAD02C}); formatted with bright aqua {@code "§b"}.</p>
 */
public final class EntropicaSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int ENTROPICA_CHRONO_SILVER_HEX_COLOR = 0xE0E5EC;
    public static final int ENTROPICA_EVENT_HORIZON_INDIGO_HEX_COLOR = 0x1A153B;
    public static final int ENTROPICA_GENESIS_GOLD_HEX_COLOR = 0xFAD02C;
    public static final int ENTROPICA_CRIMSON_VOID_PARALLAX_HEX_COLOR = 0x7A0E24;

    private static final String TAG_TERMINAL_NOVA_FIRED = "SpellEntropicaTerminalNovaFired";
    private static final String TAG_BEAM_START_TICK = "SpellEntropicaBeamStartTick";
    private static final String TAG_BEAM_LAST_TICK = "SpellEntropicaBeamLastTick";
    private static final String TAG_BEAM_BLAST_FIRED = "SpellEntropicaBeamAnnihilationFired";

    @Override
    public EssenceType materiaType() {
        return EssenceType.ENTROPICA;
    }

    @Override
    public @Nullable String resolveFlowTitle(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE ? "Cosmic Parallax Singularity Orbit Flow" : null;
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE
                ? "Modifies projectile and channeled spells to manifest 4 persistent orbiting cores featuring End-portal "
                        + "starfield parallax depths and golden-silver coronas (#E0E5EC / #1A153B / #FAD02C) that circle the "
                        + "primary spell or caster; phases through solid walls, vaporizes 100% of enemy projectiles into pure "
                        + "Materia fuel, and fires retaliatory seeking balance-darts (-50% armor toughness and true damage)."
                : null;
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Singularity Equilibrium Lance";
            case BEAM -> "Twin Helix Singularity Stream & Annihilation Blast";
            case RADIAL_BURST -> "Cosmic Equilibrium Nova";
            case TOUCH_STRIKE -> "Blade of the Grand Paradox";
            case AURA -> "Mantle of Entropica";
            case GLYPH -> "Singularity Horizon Mandala";
            case CONE -> "Thermodynamic Heat-Death Wave";
            case SKYFALL -> "Cataclysmic Singularity Equilibrium Starfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Launches an ultra-dense, wall-phasing iridescent javelin forged of chrono-silver, event-horizon "
                            + "indigo, and genesis gold (#E0E5EC / #1A153B / #FAD02C); pierces solid terrain and up to "
                            + "12 targets (75% armor bypass), pulls enemies inward along its trajectory, and collapses "
                            + "into a 10m balanced creation/dissolution nova (-8 armor, knocks mobs flat, and yields "
                            + "collectible Essence Orbs).";
            case BEAM ->
                    "Channels a continuous double-helix ray of intertwining white-hole light and black-hole void; "
                            + "melts 3 armor points/sec and gravitationally pulls mobs to the beam center; sustaining "
                            + "for 5.0 continuous seconds triggers an omni-directional 16m Singularity Annihilation "
                            + "Blast (-8 armor, 16m knockback, and purges all active mob buffs).";
            case RADIAL_BURST ->
                    "Wall-penetrating 16-block concussive shockwave of iridescent silver rings, indigo gravity waves, "
                            + "and gold flares (pure shader/bloom without chunk light recalculations); knocks hostiles "
                            + "16m back flat, shatters 8 armor points, deals massive Tier 6 Entropica damage (+1 vs "
                            + "all elements, immune to counter-weaknesses), purges party debuffs, heals allies for 12 "
                            + "hearts, and grants party Mantle of Entropica for 20.0s.";
            case TOUCH_STRIKE ->
                    "Wall-cleaving two-handed blade strike woven of shifting chrono-silver, event-horizon indigo, and "
                            + "genesis gold; bypasses 75% armor rating, shatters 8 armor points, disables shields for "
                            + "8.0s, drains 50% of damage dealt into party absorption shields, and casts a mini-vortex "
                            + "drawing adjacent hostiles into the slash arc.";
            case AURA ->
                    "Transcendent End-portal starfield parallax shroud (mobile or Sneak-anchored, zero chunk light "
                            + "recalculation lag); grants +14 Armor, +8 Toughness, complete fire/lava/drowning/freeze/"
                            + "wither/blindness/void immunity, -90% mob detection, 3D flight + Slow Falling, 6m 100% "
                            + "projectile-to-Materia-fuel disintegration, and an 8m balanced aura (3.0 hearts/s true "
                            + "damage to hostiles and +3 hearts/s healing to allies).";
            case GLYPH ->
                    "Omni-surface cosmic mandala glyph on any floor, wall, or ceiling (disarmable, Single or Repeating); "
                            + "hostile trigger locks mobs in an 8m stasis cage for 6.0s (-8 armor, strips buffs, and "
                            + "draws incoming projectiles directly into caught targets); ally step restores full HP & "
                            + "hunger, cures curses, grants +12 Absorption hearts, and provides 3D flight.";
            case CONE ->
                    "Wall-penetrating wave of pure entropy and equilibrium plasma; freezes standing water into blue ice, "
                            + "vitrifies sand into smooth crystal glass, extinguishes fires, stuns hostiles for 5.0s, "
                            + "shatters 8 armor points, and inflicts Cosmic Equilibrium Stupor (35.0s AI pathfinding "
                            + "disruption and -75% movement speed).";
            case SKYFALL ->
                    "30.0s Cataclysmic Singularity Equilibrium Starfall across a 20m+ crimson-tinted void-red End-portal "
                            + "parallax sky fissure calling down 8 3D balanced anti-matter lances and golden solar "
                            + "starcores that permanently materialize on completion; shatters 8 armor points, knocks "
                            + "hostiles flat, lifts mobs into zero-g stasis every few seconds, heals allies (+3 hearts/s), "
                            + "and leaves a 15.0s Equilibrium Ground (-80% slow and party Materia fuel regeneration).";
            case SUMMON_AGENT ->
                    "Summons a 140 HP Avatar of Entropica (90s refreshing lifecycle) — colossal wall-phasing floating "
                            + "entity delivering dual-elemental sweeping cleaves and hurling alternating void/radiant "
                            + "orbs (75% armor bypass, -5 armor/strike, and pulls distant foes into its aura).";
            case SUMMON_SENTRY ->
                    "Summons a 150 HP Altar of the Horizon (90s refreshing lifecycle) — bombards hostiles up to 24m with "
                            + "wall-penetrating balance rays (-4 armor/hit) while pulsing a 20m Equilibrium Field (2 "
                            + "hearts/s true damage to hostiles and +2 hearts/s continuous healing to allies).";
            case SUMMON_DECOY ->
                    "Summons a Fractured Paradox Core (20s duration) — End-portal parallax effigy taunting hostiles "
                            + "across 20m, reflecting 50% incoming damage, and detonating in a 20m reality rupture "
                            + "(inward pull, 5.0s stun, -8 armor, and massive true Entropica damage).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Parallax Event-Horizon Fold";
            case TRANSMUTE, BREAK -> "Grand Cosmic Equilibrium Transmutation";
            case AFFLICTION -> "Grand Equilibrium Paradox Hex";
            case WARD -> "Grand Equilibrium Singularity Ward";
            default -> "Grand Cosmic Equilibrium " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Teleports the caster up to 24 blocks through solid terrain on an iridescent rift; departure leaves "
                            + "an End-portal parallax decoy that taunts and pulls pursuers for 4.0s before exploding "
                            + "into a balanced stasis nova; arrival grants 12.0s 3D flight, Slow Falling, 4.0s complete "
                            + "damage immunity, and sheds 100% hostile mob aggro.";
            case TRANSMUTE, BREAK ->
                    "5.0s continuous channel across placed physical blocks in a 16m radius (strictly NO fluids, NO empty "
                            + "air); breaks down placed cobblestone, dirt, and gravel directly into collectible Essence "
                            + "Orbs; transmutes placed stone, deepslate, and netherrack into Iridescent Primordial "
                            + "Runestone, Calcite, and Harmonic Crystal Blocks; permanently purges all curses and "
                            + "elevates ALL existing enchantments on worn equipment by +5 tiers.";
            case AFFLICTION ->
                    "Milk-immune supreme thermodynamic collapse hex cleansed only by Restoration (30.0s boss cap + 10.0s "
                            + "immunity, 6m conditional death contagion); deals continuous true damage, -85% move speed, "
                            + "disables natural & magical healing, -60% armor toughness, and 60% weapon self-damage "
                            + "recoil. Player Hybrid Vector (Cosmic Paradox Distortion): +200% Spawn-Lure Beacon across "
                            + "36m AND +100% monster attack speed / +50% crit chance.";
            case WARD ->
                    "Harmonizes the recipient's arcane barrier in absolute cosmic equilibrium, purging negative debuffs "
                            + "and fortifying absorption shields.";
            default -> "Infuses " + effect.getDisplayName() + " with Tier 6 Grand Cosmic Equilibrium Singularity perfection.";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATIONS — COSMIC PARALLAX SINGULARITY ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow != SpellFlowModifier.ORBIT_SATELLITE) {
            return;
        }
        projectile.setWallPhasing(true);
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;

        // 1. Render 4 orbiting End-portal parallax cores & golden-silver coronas (#E0E5EC / #1A153B / #FAD02C)!
        double baseAngle = projectile.tickCount * 0.44D;
        for (int i = 0; i < 4; i++) {
            double ang = baseAngle + (Math.PI * 0.5D * i);
            double ox = projectile.getX() + Math.cos(ang) * 0.70D;
            double oy = projectile.getY() + 0.14D + Math.sin(ang * 2.0D) * 0.18D;
            double oz = projectile.getZ() + Math.sin(ang) * 0.70D;
            int rgb = switch (i & 3) {
                case 0 -> ENTROPICA_CHRONO_SILVER_HEX_COLOR;
                case 1 -> ENTROPICA_GENESIS_GOLD_HEX_COLOR;
                case 2 -> ENTROPICA_EVENT_HORIZON_INDIGO_HEX_COLOR;
                default -> ENTROPICA_CHRONO_SILVER_HEX_COLOR;
            };
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), rgb, 14, 0.92F, 0.03F),
                    ox, oy, oz, 1, 0.02D, 0.02D, 0.02D, 0.0D
            );
        }

        // 2. Vaporize 100% of incoming enemy physical & hostile magical projectiles into pure Entropica Materia fuel!
        for (Projectile incoming : level.getEntitiesOfClass(Projectile.class, projectile.getBoundingBox().inflate(3.4D))) {
            if (incoming == projectile || (owner != null && incoming.getOwner() == owner)) continue;
            if (incoming instanceof SpellProjectileEntity sp
                    && owner != null
                    && sp.getOwner() instanceof LivingEntity spo
                    && ApexEschatonSingularityHelper.isFriendly(spo, owner)) {
                continue;
            }
            Vec3 pPos = incoming.position();
            incoming.discard();
            level.sendParticles(ParticleTypes.END_ROD, pPos.x, pPos.y, pPos.z, 5, 0.10D, 0.10D, 0.10D, 0.03D);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, pPos.x, pPos.y, pPos.z, 5, 0.10D, 0.10D, 0.10D, 0.03D);
            if (owner instanceof Player p) {
                SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.ENTROPICA, 72L);
            }
        }

        // 3. Fire retaliatory seeking balance-darts (-50% armor toughness & true Entropica damage)!
        if (projectile.tickCount % 12 == 0) {
            int tier = Math.max(1, projectile.getVisualPotencyTier());
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(7.8D), LivingEntity::isAlive)) {
                if (foe == owner || (owner != null && ApexEschatonSingularityHelper.isFriendly(foe, owner))) continue;
                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true)) continue;
                if (foe instanceof Monster || (owner != null && foe == owner.getLastHurtMob())) {
                    SpellWorldVfxManager.broadcastChainArc(
                            level, projectile.position(), foe.getEyePosition().subtract(0.0D, 0.2D, 0.0D),
                            EssenceType.ENTROPICA, EssenceType.ASTRAL, tier
                    );
                    SpellCombatReactionEngine.applyPercentageArmorShred(foe, level, 0.0D, 0.50D, 140L);
                    SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                            level, foe, owner, 3.8F + tier * 0.95F, false, false, true
                    );
                    break;
                }
            }
        }
    }

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromTarget,
            LivingEntity toTarget,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        long now = level.getGameTime();
        if (spell.flowPart().flow() == SpellFlowModifier.ORBIT_SATELLITE) {
            EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ENTROPICA_ORBIT_UNTIL, now + 200L);
        }
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
        SpellWorldVfxManager.broadcastChainArc(
                level, fromTarget.getEyePosition(), toTarget.getEyePosition(),
                EssenceType.ENTROPICA, EssenceType.ASTRAL, spell.getEffectiveTier()
        );
        if (!ApexEschatonSingularityHelper.isFriendly(toTarget, caster)
                && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(toTarget, caster, spell.hasWardFriendlyFireSafeguard())) {
            SpellCombatReactionEngine.applyPercentageArmorShred(toTarget, level, 0.0D, 0.50D, 140L);
            SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                    level, toTarget, caster, 4.0F + spell.getEffectiveTier() * 1.00F, false, false, true
            );
        }
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (9 CANONICAL FORMS)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        projectile.setNoGravity(true);
        projectile.setWallPhasing(true);
        emitEntropicaBurstParticles(level, projectile.position(), 12, 0.25D);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        // Section 6.59 Singularity Equilibrium Lance: pulls enemies inward along its trajectory!
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(4.2D), LivingEntity::isAlive)) {
            if (foe == owner) continue;
            if (owner != null && (ApexEschatonSingularityHelper.isFriendly(foe, owner)
                    || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true))) {
                continue;
            }
            if (!(foe instanceof Monster) && (owner == null || foe != owner.getLastHurtMob())) continue;
            Vec3 pull = projectile.position().subtract(foe.position()).scale(0.32D);
            foe.setDeltaMovement(foe.getDeltaMovement().add(pull.x, 0.05D, pull.z));
            foe.hasImpulse = true;
        }

        double spin = projectile.tickCount * 0.62D;
        for (int i = 0; i < 3; i++) {
            double ang = spin + (i * Math.PI * 2.0D / 3.0D);
            double px = projectile.getX() + Math.cos(ang) * 0.36D;
            double py = projectile.getY() + Math.sin(ang * 1.5D) * 0.14D;
            double pz = projectile.getZ() + Math.sin(ang) * 0.36D;
            int rgb = (i == 0) ? ENTROPICA_CHRONO_SILVER_HEX_COLOR
                    : (i == 1 ? ENTROPICA_GENESIS_GOLD_HEX_COLOR : ENTROPICA_EVENT_HORIZON_INDIGO_HEX_COLOR);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), rgb, 12, 0.88F, 0.03F),
                    px, py, pz, 1, 0.02D, 0.02D, 0.02D, 0.0D
            );
        }
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
        // Section 6.59 Singularity Equilibrium Lance: pierces up to 12 targets with 75% armor bypass;
        // upon terminal impact collapses into a balanced creation/dissolution nova!
        int tier = spell.getEffectiveTier();
        if (hitEntity instanceof LivingEntity le && le.isAlive()) {
            if (!ApexEschatonSingularityHelper.isFriendly(le, caster)
                    && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(le, caster, spell.hasWardFriendlyFireSafeguard()))) {
                float lanceDmg = (slot.hasHarmVerb() ? 4.5F : 8.2F) + tier * 1.70F;
                SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                        level, le, caster, lanceDmg, false, true, false
                );
            }
        }
        if (projectile.getPierceCountRemaining() <= 0) {
            tryCollapseEquilibriumNovaOnce(level, projectile, caster, hitEntity.position(), spell);
        }
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
        if (!projectile.isWallPhasing()) {
            tryCollapseEquilibriumNovaOnce(level, projectile, caster, blockHit.getLocation(), spell);
        }
    }

    @Override
    public void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        tryCollapseEquilibriumNovaOnce(level, projectile, caster, projectile.position(), spell);
    }

    private static void tryCollapseEquilibriumNovaOnce(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell
    ) {
        CompoundTag pTag = EntityHelper.getPersistentData(projectile);
        if (pTag.getBoolean(TAG_TERMINAL_NOVA_FIRED).orElse(false)) {
            return;
        }
        pTag.putBoolean(TAG_TERMINAL_NOVA_FIRED, true);
        collapseEquilibriumNova(level, caster, center, spell);
    }

    private static void collapseEquilibriumNova(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell
    ) {
        // Section 6.59 Terminal 10m Balanced Creation/Dissolution Nova:
        // Shatters 8 armor points, knocks mobs flat, weaves Primordial Genesis & Dissolution Loom,
        // and yields collectible Entropica Essence Orbs!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, 10.0D, EssenceType.ENTROPICA, EssenceType.ASTRAL, tier);
        SpellWorldVfxManager.broadcastVacuumSphere(level, center.add(0.0D, 0.7D, 0.0D), 4.5D, true, EssenceType.ENTROPICA, EssenceType.ASTRAL, 32, tier);
        weavePrimordialGenesisAndDissolutionLoom(level, BlockPos.containing(center), 10, 20);

        AABB nova = new AABB(
                center.x - 10.0D, center.y - 3.0D, center.z - 10.0D,
                center.x + 10.0D, center.y + 5.5D, center.z + 10.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, nova, LivingEntity::isAlive)) {
            if (foe == caster || ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 8.0D, 0.0D, 240L);
            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.2D, -0.60D, foe.getDeltaMovement().z * 0.2D);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                    level, foe, caster, 12.0F + tier * 2.35F, false, true, false
            );
        }

        SpellLingeringZoneManager.spawnTypedEssenceOrb(level, center, EssenceType.ENTROPICA, 3);
        emitEntropicaBurstParticles(level, center.add(0.0D, 0.8D, 0.0D), 48, 3.0D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.10F, 1.35F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 0.90F, 1.25F);
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
        // Section 6.59 Twin Helix Singularity Stream & Annihilation Blast:
        // Double-helix ray of intertwining white-hole light & black-hole void; melts 3 armor points/sec and
        // gravitationally pulls mobs to the beam center; sustaining for 5.0 continuous seconds (100t) triggers
        // an omni-directional 16m Singularity Annihilation Blast (-8 armor, 16m knockback, purges all mob buffs)!
        int tier = spell.getEffectiveTier();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 rayStart = caster.getEyePosition();
        Vec3 epicenter = blockHit != null ? blockHit.getLocation() : rayStart.add(dir.scale(Math.min(20.0D, maxReach)));
        long now = level.getGameTime();

        CompoundTag casterData = EntityHelper.getPersistentData(caster);
        long lastTick = casterData.getLong(TAG_BEAM_LAST_TICK).orElse(-100L);
        boolean sameTickRay = lastTick == now;
        long startTick;
        boolean alreadyBlasted;
        if (heldTicks <= 0 || now - lastTick > 14L) {
            startTick = now;
            alreadyBlasted = false;
            casterData.putLong(TAG_BEAM_START_TICK, startTick);
            casterData.putBoolean(TAG_BEAM_BLAST_FIRED, false);
        } else {
            startTick = casterData.getLong(TAG_BEAM_START_TICK).orElse(now);
            alreadyBlasted = casterData.getBoolean(TAG_BEAM_BLAST_FIRED).orElse(false);
        }
        casterData.putLong(TAG_BEAM_LAST_TICK, now);

        // 1. Gravitationally pull nearby hostiles perpendicular toward the beam axis!
        AABB corridor = new AABB(rayStart, epicenter).inflate(4.5D);
        double beamLen = Math.max(0.5D, rayStart.distanceTo(epicenter));
        for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, corridor, LivingEntity::isAlive)) {
            if (near == caster || ApexEschatonSingularityHelper.isFriendly(near, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(near, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            if (!(near instanceof Monster) && near != caster.getLastHurtMob()) continue;
            Vec3 rel = near.position().add(0.0D, near.getBbHeight() * 0.5D, 0.0D).subtract(rayStart);
            double projT = Math.clamp(rel.dot(dir), 0.5D, beamLen);
            Vec3 closestOnAxis = rayStart.add(dir.scale(projT));
            Vec3 toAxis = closestOnAxis.subtract(near.position().add(0.0D, near.getBbHeight() * 0.5D, 0.0D));
            double dSq = toAxis.lengthSqr();
            if (dSq > 0.04D && dSq <= 20.25D) {
                Vec3 pull = toAxis.normalize().scale(0.46D);
                near.setDeltaMovement(near.getDeltaMovement().scale(0.45D).add(pull.x, Math.max(0.04D, pull.y * 0.35D), pull.z));
                near.hasImpulse = true;
            }
        }

        // 2. Melt 3 armor points/sec & deal Tier 6 Entropica damage on struck targets
        float tickDmg = (3.6F + tier * 0.85F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            epicenter = hit.position();

            CompoundTag hData = EntityHelper.getPersistentData(hit);
            long prevHit = hData.getLong("EntropicaBeamLastTick").orElse(-100L);
            if (prevHit == now) continue;
            double shred = (now - prevHit <= 30L)
                    ? Math.min(12.0D, hData.getDouble("EntropicaBeamArmorMelt").orElse(0.0D) + 1.5D)
                    : 3.0D; // Melts 3 armor points per second!
            hData.putLong("EntropicaBeamLastTick", now);
            hData.putDouble("EntropicaBeamArmorMelt", shred);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, shred, 0.0D, 85L);

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                        level, hit, caster, tickDmg, false, true, false
                );
            }
        }

        if (!sameTickRay) {
            weavePrimordialGenesisAndDissolutionLoom(level, BlockPos.containing(epicenter), 4, 6);
        }

        // 3. Sustaining for 5.0 continuous seconds (100t) triggers an omni-directional Singularity Annihilation Blast!
        if (heldTicks >= 100 && !alreadyBlasted && (now - startTick) >= 100L) {
            casterData.putBoolean(TAG_BEAM_BLAST_FIRED, true);
            casterData.putLong(TAG_BEAM_START_TICK, now);

            SpellWorldVfxManager.broadcastRadialNova(level, epicenter, 16.0D, EssenceType.ENTROPICA, EssenceType.ASTRAL, tier);
            SpellWorldVfxManager.broadcastVacuumSphere(level, epicenter.add(0.0D, 0.8D, 0.0D), 7.5D, false, EssenceType.ENTROPICA, EssenceType.ASTRAL, 36, tier);
            weavePrimordialGenesisAndDissolutionLoom(level, BlockPos.containing(epicenter), 14, 32);

            AABB blastBox = new AABB(
                    epicenter.x - 16.0D, epicenter.y - 4.0D, epicenter.z - 16.0D,
                    epicenter.x + 16.0D, epicenter.y + 7.0D, epicenter.z + 16.0D
            );
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
                if (foe == caster || ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;

                ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 8.0D, 0.0D, 240L);
                if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                    Vec3 diff = foe.position().subtract(epicenter);
                    Vec3 away = (diff.horizontalDistanceSqr() > 1.0E-4D
                            ? new Vec3(diff.x, 0.0D, diff.z).normalize()
                            : new Vec3(dir.x, 0.0D, dir.z)).scale(3.15D); // 16-block knockback!
                    SpellExecutionEngine.applySyncedMotion(foe, new Vec3(away.x, 0.52D, away.z));
                }
                SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                        level, foe, caster, 22.0F + tier * 3.50F, false, true, false
                );
            }

            if (caster instanceof ServerPlayer sp) {
                sp.displayClientMessage(Component.literal("§b✦ Singularity Annihilation Blast unleashed!"), true);
            }
            emitEntropicaBurstParticles(level, epicenter.add(0.0D, 0.9D, 0.0D), 60, 4.2D);
            level.playSound(null, BlockPos.containing(epicenter), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.25F, 1.32F);
            level.playSound(null, BlockPos.containing(epicenter), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.10F, 0.72F);
        }
    }

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
        // Section 6.59 Cosmic Equilibrium Nova:
        // Wall-penetrating 16-block concussive shockwave (pure shader/bloom — strictly ZERO light blocks!),
        // knocks all hostiles 16 blocks back flat, shatters 8 armor points, deals massive Tier 6 Entropica Materia
        // damage (+1 vs all elements, immune to counter-weaknesses), purges party debuffs, heals allies for 12 hearts
        // (24.0 HP), and grants the party Mantle of Entropica for 20.0 seconds (400t)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(16.0D, radius);
        double rSq = (r + 0.5D) * (r + 0.5D);
        long mantleUntil = level.getGameTime() + 400L;

        // Strictly NO Blocks.LIGHT placement — pure shader/bloom shockwaves!
        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.ENTROPICA, EssenceType.ASTRAL, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.04D, 0.0D), r, EssenceType.ENTROPICA, EssenceType.ASTRAL, 90, tier);
        weavePrimordialGenesisAndDissolutionLoom(level, BlockPos.containing(center), 20, 38);

        float novaDmg = (11.5F + tier * 2.45F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 3.5D, center.z - r, center.x + r, center.y + 6.5D, center.z + r);

        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double dx = ent.getX() - center.x;
            double dz = ent.getZ() - center.z;
            if (dx * dx + dz * dz > rSq) continue;

            if (ent == caster || ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                grantMantleOfEntropicaAndHeal(level, ent, mantleUntil, 24.0F); // 12 hearts + 20.0s Mantle!
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;

            if (!SpellCombatReactionEngine.isBossEntity(level, ent)) {
                Vec3 horiz = new Vec3(dx, 0.0D, dz);
                Vec3 away = (horiz.lengthSqr() > 1.0E-4D
                        ? horiz.normalize()
                        : new Vec3(direction.x, 0.0D, direction.z)).scale(3.15D); // 16m flat knockback!
                SpellExecutionEngine.applySyncedMotion(ent, new Vec3(away.x, 0.30D, away.z));
            }
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 8.0D, 0.0D, 240L);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                        level, ent, caster, novaDmg, false, true, false
                );
            }
        }

        grantMantleOfEntropicaAndHeal(level, caster, mantleUntil, 24.0F);
        emitEntropicaBurstParticles(level, center.add(0.0D, 1.0D, 0.0D), 64, r * 0.38D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.25F, 1.30F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.48F);
    }

    private static void grantMantleOfEntropicaAndHeal(
            ServerLevel level,
            LivingEntity ally,
            long mantleUntil,
            float healAmount
    ) {
        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ally);
        if (healAmount > 0.0F) {
            SpellCombatReactionEngine.notifyMagicalHeal(ally, healAmount);
            ally.heal(healAmount);
        }
        CompoundTag aData = EntityHelper.getPersistentData(ally);
        aData.putLong(SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL,
                Math.max(mantleUntil, aData.getLong(SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL).orElse(0L)));
        aData.putLong(SpellLingeringZoneManager.TAG_VOID_IMMUNE_UNTIL,
                Math.max(mantleUntil, aData.getLong(SpellLingeringZoneManager.TAG_VOID_IMMUNE_UNTIL).orElse(0L)));
        int ticks = (int) Math.max(40L, mantleUntil - level.getGameTime());
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 14.0D, 8.0D, 0.0D, 0.0D, ticks);
        SpellLingeringZoneManager.grantArcane3DFlight(ally, ticks);
        ally.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, ticks, 0, true, false));
        SpellCombatReactionEngine.syncEntityBarrierState(level, ally, 0.85F, null);
    }

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
        // Section 6.59 Blade of the Grand Paradox:
        // Wall-cleaving two-handed blade strike; bypasses 75% armor rating, shatters 8 armor points,
        // disables shields for 8.0s (160t), drains 50% of damage dealt into party absorption shields,
        // and casts a mini-vortex drawing adjacent hostiles into the slash arc!
        int tier = spell.getEffectiveTier();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        Vec3 arcOrigin = hitEntities.isEmpty() ? caster.position().add(dir.scale(2.5D)) : hitEntities.getFirst().position();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().add(dir.scale(1.5D)), dir, 5.8D, EssenceType.ENTROPICA, EssenceType.ASTRAL, tier
        );
        SpellWorldVfxManager.broadcastVacuumSphere(
                level, arcOrigin.add(0.0D, 0.7D, 0.0D), 3.6D, true, EssenceType.ENTROPICA, EssenceType.ASTRAL, 26, tier
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.80F, 1.50F);

        float cleaveDmg = (9.2F + tier * 2.00F) * Math.max(0.5F, apparatusPotency);
        float totalDrained = 0.0F;

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
            ApexEschatonSingularityHelper.disableShield(hit, 160); // 8.0s shield disable!
            float dealt = !slot.hasHarmVerb()
                    ? SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(level, hit, caster, cleaveDmg, true, true, false)
                    : cleaveDmg;
            totalDrained += Math.max(4.0F, dealt * 0.50F);
        }

        AABB vortex = new AABB(
                arcOrigin.x - 8.0D, arcOrigin.y - 2.0D, arcOrigin.z - 8.0D,
                arcOrigin.x + 8.0D, arcOrigin.y + 4.5D, arcOrigin.z + 8.0D
        );
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, vortex,
                e -> e.isAlive() && e != caster && !ApexEschatonSingularityHelper.isFriendly(e, caster))) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, safeguard)) continue;
            Vec3 in = arcOrigin.subtract(adj.position()).scale(0.48D);
            adj.setDeltaMovement(in.x, 0.18D, in.z);
            adj.hasImpulse = true;
            if (!hitEntities.contains(adj)) {
                SpellCombatReactionEngine.applyTimedAttributeShred(adj, level, 8.0D, 0.0D, 200L);
                float adjDealt = SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                        level, adj, caster, 5.2F + tier * 1.15F, true, true, false
                );
                totalDrained += Math.max(2.0F, adjDealt * 0.50F);
            }
        }

        if (totalDrained > 0.0F) {
            SpellLingeringZoneManager.grantPartyAbsorption(level, caster, totalDrained, 36.0F, 20.0D);
        }
        emitEntropicaBurstParticles(level, arcOrigin.add(0.0D, 0.8D, 0.0D), 24, 1.8D);
    }

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
        // Section 6.59 Mantle of Entropica:
        // Strictly NO dynamic light emission (avoids vanilla chunk light recalculation lag!);
        // +14 Armor, +8 Toughness, complete fire/lava/drowning/freeze/wither/blindness/void immunity,
        // -90% mob detection range, 3D flight + Slow Falling, 6m 100% projectile-to-Materia-fuel disintegration,
        // and 8m balanced aura (3.0 hearts/s true damage to hostiles & +3 hearts/s healing to allies)!
        int tier = spell.getEffectiveTier();
        int dur = 360 + tier * 40;
        long until = level.getGameTime() + dur;

        if (!caster.isShiftKeyDown()) {
            grantMantleOfEntropicaAndHeal(level, caster, until, 0.0F);
        }
        emitEntropicaBurstParticles(level, caster.position().add(0.0D, 1.0D, 0.0D), 36, 0.85D);
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.35F);

        if (caster.isShiftKeyDown()) {
            double domeRad = Math.max(8.0D, radius);
            Vec3 anchorCenter = caster.position();
            SpellWorldVfxManager.broadcastArcaneSeal(
                    level, anchorCenter.add(0.0D, 0.03D, 0.0D), domeRad, EssenceType.ENTROPICA, EssenceType.ASTRAL, dur, tier
            );
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, anchorCenter, domeRad, dur, level.getGameTime(), spell, slot, EssenceType.ENTROPICA, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        LivingEntity owner = zone.resolveCaster(srv);
                        if (elapsed % 12 == 0) {
                            emitEntropicaBurstParticles(srv, zone.center.add(0.0D, 1.0D, 0.0D), 14, zone.radius * 0.45D);
                        }
                        AABB pBox = new AABB(
                                zone.center.x - 6.0D, zone.center.y - 2.0D, zone.center.z - 6.0D,
                                zone.center.x + 6.0D, zone.center.y + 5.0D, zone.center.z + 6.0D
                        );
                        for (Projectile proj : srv.getEntitiesOfClass(Projectile.class, pBox)) {
                            if (owner != null && proj.getOwner() == owner) continue;
                            if (proj instanceof SpellProjectileEntity sp
                                    && owner != null
                                    && sp.getOwner() instanceof LivingEntity spo
                                    && ApexEschatonSingularityHelper.isFriendly(spo, owner)) {
                                continue;
                            }
                            srv.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.03D);
                            srv.sendParticles(ParticleTypes.REVERSE_PORTAL, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.03D);
                            proj.discard();
                            if (owner instanceof Player p) {
                                SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.ENTROPICA, 80L);
                            }
                        }
                        if (elapsed % 20 == 0) {
                            AABB box = new AABB(
                                    zone.center.x - zone.radius, zone.center.y - 2.5D, zone.center.z - zone.radius,
                                    zone.center.x + zone.radius, zone.center.y + 5.5D, zone.center.z + zone.radius
                            );
                            for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                                if (owner != null && (ent == owner || ApexEschatonSingularityHelper.isFriendly(ent, owner))) {
                                    grantMantleOfEntropicaAndHeal(srv, ent, srv.getGameTime() + 30L, 6.0F);
                                } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, true)) {
                                    if (ent instanceof Monster || (owner != null && ent == owner.getLastHurtMob())) {
                                        SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                                                srv, ent, owner, 6.0F, false, false, true
                                        );
                                    }
                                }
                            }
                        }
                        return true;
                    }
            ));
        }
    }

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
        // Section 6.59 Singularity Horizon Mandala:
        // Disarmable omni-surface cosmic mandala glyph (Single or Repeating);
        // - Hostiles: locks mobs in an 8-block stasis cage for 6.0s (120t), shatters 8 armor points,
        //   strips all active positive buffs, and draws incoming projectiles directly into caught targets!
        // - Allies: fully restores health and hunger, cures all negative curses & hexes,
        //   grants +12 Absorption hearts (24.0 HP), and provides 3D flight!
        final int tier = spell.getEffectiveTier();
        final int glyphDuration = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        String subMode = slot.primaryEffect().subMode();
        final boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        final int[] cooldown = {0};

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, glyphCenter.add(0.0D, 0.03D, 0.0D), 6.0D, EssenceType.ENTROPICA, EssenceType.ASTRAL, Math.min(240, glyphDuration), tier
        );
        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.85F, 1.35F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 6.0D, glyphDuration, level.getGameTime(), spell, slot, EssenceType.ENTROPICA, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (cooldown[0] > 0) {
                        cooldown[0]--;
                        return true;
                    }
                    if (elapsed % 8 == 0) {
                        emitEntropicaBurstParticles(srv, zone.center.add(0.0D, 0.15D, 0.0D), 6, 1.6D);
                    }
                    if (elapsed < 10) return true;

                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB trig = new AABB(
                            zone.center.x - 6.0D, zone.center.y - 2.0D, zone.center.z - 6.0D,
                            zone.center.x + 6.0D, zone.center.y + 3.5D, zone.center.z + 6.0D
                    );
                    List<LivingEntity> steppers = srv.getEntitiesOfClass(LivingEntity.class, trig, LivingEntity::isAlive);
                    if (steppers.isEmpty()) return true;

                    // 1. Ally sanctuary step (inner 3.2m mandala core)
                    for (LivingEntity st : steppers) {
                        if (owner != null && (st == owner || ApexEschatonSingularityHelper.isFriendly(st, owner))) {
                            if (st == owner && elapsed < 24) continue;
                            if (st.position().distanceToSqr(zone.center) > 10.24D) continue;

                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(st);
                            for (EquipmentSlot eq : EquipmentSlot.values()) {
                                PenumbraSpellMutationHandler.purgeCursesFromStack(st.getItemBySlot(eq));
                            }
                            float full = st.getMaxHealth();
                            SpellCombatReactionEngine.notifyMagicalHeal(st, full);
                            st.heal(full);
                            if (st instanceof Player p) {
                                p.getFoodData().eat(20, 1.0F);
                            }
                            st.setAbsorptionAmount(Math.max(st.getAbsorptionAmount(), 24.0F)); // +12 Absorption hearts!
                            SpellLingeringZoneManager.grantArcane3DFlight(st, 600);
                            srv.sendParticles(ParticleTypes.HEART, st.getX(), st.getY(1.0D), st.getZ(), 10, 0.4D, 0.4D, 0.4D, 0.03D);
                            srv.playSound(null, st.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.40F);

                            if (repeating) {
                                cooldown[0] = 140;
                                return true;
                            }
                            return false;
                        }
                    }

                    // 2. Hostile step -> 8-block stasis cage for 6.0s (120t) + projectile-into-target magnet!
                    boolean hasHostile = false;
                    for (LivingEntity st : steppers) {
                        if (owner != null && ApexEschatonSingularityHelper.isFriendly(st, owner)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                st, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }
                        if (st instanceof Monster || (owner != null && st == owner.getLastHurtMob())) {
                            hasHostile = true;
                            break;
                        }
                    }
                    if (!hasHostile) return true;

                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                    SpellWorldVfxManager.broadcastArcaneSeal(
                            srv, zone.center.add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.ENTROPICA, EssenceType.ASTRAL, 120, zTier
                    );
                    for (int p = 0; p < 8; p++) {
                        double pAng = (Math.PI * 2.0D * p) / 8.0D;
                        Vec3 pillarPos = zone.center.add(Math.cos(pAng) * 7.8D, 0.0D, Math.sin(pAng) * 7.8D);
                        SpellWorldVfxManager.broadcastSummonPillar(srv, pillarPos, 1.25D, EssenceType.ENTROPICA, EssenceType.ASTRAL, zTier);
                    }

                    AABB cage = new AABB(
                            zone.center.x - 8.0D, zone.center.y - 2.5D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 5.5D, zone.center.z + 8.0D
                    );
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, cage, e -> e.isAlive() && e != owner)) {
                        if (owner != null && ApexEschatonSingularityHelper.isFriendly(foe, owner)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                foe, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }
                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 8.0D, 0.0D, 240L);
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 120, 0));
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 120, 0));
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, foe, Vec3.ZERO, zone.spell, zone.slot, EssenceType.ENTROPICA, zone.apparatusPotency
                            );
                        }
                    }

                    // 6.0s (120t) stasis cage boundary + draws incoming projectiles directly into caught targets!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 10.0D, 120, srv.getGameTime(), zone.spell, zone.slot, EssenceType.ENTROPICA, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB cBox = new AABB(
                                        sZone.center.x - 8.5D, sZone.center.y - 2.5D, sZone.center.z - 8.5D,
                                        sZone.center.x + 8.5D, sZone.center.y + 5.5D, sZone.center.z + 8.5D
                                );
                                LivingEntity primaryCaught = null;
                                for (LivingEntity trapped : sLvl.getEntitiesOfClass(LivingEntity.class, cBox, LivingEntity::isAlive)) {
                                    if (trapped == sOwner || (sOwner != null && ApexEschatonSingularityHelper.isFriendly(trapped, sOwner))) continue;
                                    if (!(trapped instanceof Monster)) continue;
                                    if (primaryCaught == null) {
                                        primaryCaught = trapped;
                                    }
                                    Vec3 rel = new Vec3(trapped.getX() - sZone.center.x, 0.0D, trapped.getZ() - sZone.center.z);
                                    if (rel.length() > 7.4D) {
                                        Vec3 inward = rel.normalize().scale(-0.65D);
                                        trapped.setDeltaMovement(inward.x, Math.min(0.0D, trapped.getDeltaMovement().y), inward.z);
                                        trapped.hasImpulse = true;
                                    }
                                }
                                Vec3 magnetTarget = primaryCaught != null
                                        ? primaryCaught.getEyePosition().subtract(0.0D, 0.25D, 0.0D)
                                        : sZone.center.add(0.0D, 1.0D, 0.0D);
                                AABB pBox = new AABB(
                                        sZone.center.x - 12.0D, sZone.center.y - 3.5D, sZone.center.z - 12.0D,
                                        sZone.center.x + 12.0D, sZone.center.y + 6.5D, sZone.center.z + 12.0D
                                );
                                ApexEschatonSingularityHelper.pullProjectilesToward(sLvl, sOwner, pBox, magnetTarget);
                                return true;
                            }
                    ));

                    emitEntropicaBurstParticles(srv, zone.center.add(0.0D, 1.0D, 0.0D), 36, 2.6D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.15F, 1.28F);

                    if (repeating) {
                        cooldown[0] = 160;
                        return true;
                    }
                    return false;
                }
        ).markDisarmableGlyph().withOnDisarm((srv, z) ->
                emitEntropicaBurstParticles(srv, z.center.add(0.0D, 0.3D, 0.0D), 14, 0.45D)));

        return true;
    }

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
        // Section 6.59 Thermodynamic Heat-Death Wave:
        // Equalizes temperatures: freezes standing water -> Blue Ice, vitrifies sand -> smooth crystal Glass,
        // extinguishes fires, stuns hostiles for 5.0s (100t), shatters 8 armor points, and inflicts
        // Cosmic Equilibrium Stupor (35.0s / 700t AI pathfinding disruption & exact -75% movement speed)!
        int tier = spell.getEffectiveTier();
        double reach = Math.max(14.0D, coneRange);
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 origin = caster.getEyePosition();

        SpellWorldVfxManager.broadcastVortexCone(
                level, origin.subtract(0.0D, 0.2D, 0.0D), dir, reach, EssenceType.ENTROPICA, EssenceType.ASTRAL, tier
        );

        for (int step = 2; step <= (int) Math.min(14.0D, reach); step += 3) {
            BlockPos stepCenter = BlockPos.containing(origin.add(dir.scale(step)));
            int spread = Math.min(5, 2 + step / 4);
            for (BlockPos p : BlockPos.betweenClosed(stepCenter.offset(-spread, -2, -spread), stepCenter.offset(spread, 2, spread))) {
                BlockState bs = level.getBlockState(p);
                if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
                if (bs.is(Blocks.WATER) && bs.getFluidState().isSource()) {
                    level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
                } else if (bs.is(Blocks.SAND) || bs.is(Blocks.RED_SAND)) {
                    level.setBlockAndUpdate(p, Blocks.GLASS.defaultBlockState());
                } else if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                }
            }
        }

        float waveDmg = (8.8F + tier * 1.95F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive()) continue;
            hit.clearFire();
            if (ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 100, 0)); // 5.0s stun!

            // Cosmic Equilibrium Stupor: 35.0s (700t) AI pathfinding disruption + exact -75% movement speed!
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.75D, 700L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 700, 4));
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 700);
            if (hit instanceof Mob mob) {
                mob.getNavigation().stop();
                mob.setTarget(null);
            }

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                        level, hit, caster, waveDmg, false, true, false
                );
            }
        }

        for (int s = 3; s <= 12; s += 3) {
            emitEntropicaBurstParticles(level, origin.add(dir.scale(s)), 14, s * 0.22D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.0F, 1.35F);
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.85F, 1.25F);
    }

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
        // Section 6.59 Cataclysmic Singularity Equilibrium Starfall [30.0s Starfall + 15.0s Equilibrium Ground = 900t, 20m+ AOE]:
        // Iridescent cosmic sky fissure rendered with deep crimson-tinted void-red End-portal parallax starfield depth
        // and chrono-silver/genesis-gold edges; calls down 8 3D Skyfall Pseudoblock balanced anti-matter lances &
        // golden solar starcores that permanently materialize on completion; shatters 8 armor points, knocks hostiles
        // flat, lifts mobs into zero-g stasis every few seconds, continuously heals allies (+3 hearts/sec), and leaves
        // a 15.0-second Equilibrium Ground slowing hostiles by 80% while regenerating party Materia fuel!
        final int tier = spell.getEffectiveTier();
        final double r = Math.max(20.0D, radius);
        final BlockPos centerPos = BlockPos.containing(impactCenter);
        final double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.ENTROPICA, EssenceType.CATACLYSM, 900, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.04D, 0.0D), r, EssenceType.ENTROPICA, EssenceType.ASTRAL, 900, tier);
        level.playSound(null, centerPos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.25F, 1.25F);
        level.playSound(null, centerPos, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.65F, 1.18F);

        final int lanceCount = 8;
        final BlockPos[] lanceTargets = new BlockPos[lanceCount];
        lanceTargets[0] = SpellWorldVfxManager.findSkyfallSurfaceBase(level, centerPos);
        for (int i = 1; i < lanceCount; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / (lanceCount - 1.0D)) + level.random.nextDouble() * 0.40D;
            double dist = 0.22D * r + level.random.nextDouble() * (0.58D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            lanceTargets[i] = SpellWorldVfxManager.findSkyfallSurfaceBase(level, raw);
        }

        final boolean[] landed = new boolean[lanceCount];
        final boolean[] materialized = {false};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 900, level.getGameTime(), spell, slot, EssenceType.ENTROPICA, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int mIdx = 0; mIdx < lanceCount; mIdx++) {
                                BlockState pseudoState = resolveEquilibriumStarcoreBlockState(mIdx);
                                int colHeight = (mIdx % 2 == 0) ? 2 : 1;
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, lanceTargets[mIdx], pseudoState, colHeight);
                            }
                        }
                        return false;
                    }

                    boolean starfall = elapsed < 600; // 30.0s active starfall, then 15.0s Equilibrium Ground!
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (elapsed == 20 || elapsed == 260 || elapsed == 500) {
                        weavePrimordialGenesisAndDissolutionLoom(srv, BlockPos.containing(zone.center), 20, 26);
                    }

                    if (starfall) {
                        for (int mIdx = 0; mIdx < lanceCount; mIdx++) {
                            int impactTick = 24 + mIdx * 68;
                            BlockPos strikePos = lanceTargets[mIdx];
                            BlockState pseudoState = resolveEquilibriumStarcoreBlockState(mIdx);
                            int colHeight = (mIdx % 2 == 0) ? 2 : 1;

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
                                        EssenceType.ENTROPICA,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.ENTROPICA, EssenceType.ASTRAL, 3.05F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 26, 1.3D, 0.5D, 1.3D, 0.10D
                                );
                                srv.playSound(null, strikePos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.32F);
                            }
                        }

                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 lanceHit = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, lanceHit, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.ENTROPICA, (elapsed & 8) == 0 ? EssenceType.APOTHEOSIS : EssenceType.ESCHATON, 1.85F, zTier
                            );
                        }
                    } else if (elapsed % 80 == 0) {
                        double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                        double dist = srv.random.nextDouble() * (zone.radius * 0.45D);
                        SpellLingeringZoneManager.spawnTypedEssenceOrb(
                                srv,
                                zone.center.add(Math.cos(ang) * dist, 0.4D, Math.sin(ang) * dist),
                                EssenceType.ENTROPICA,
                                2
                        );
                    }

                    for (int mIdx = 0; mIdx < lanceCount; mIdx++) {
                        if (landed[mIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, lanceTargets[mIdx], (mIdx % 2 == 0) ? 2 : 1, false);
                        }
                    }

                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.CRIMSON_SPORE,
                                zone.center.x, zone.center.y + (starfall ? 5.5D : 0.4D), zone.center.z,
                                18, zone.radius * 0.55D, starfall ? 2.6D : 0.3D, zone.radius * 0.55D, 0.04D);
                        emitEntropicaBurstParticles(
                                srv,
                                zone.center.add(0.0D, starfall ? 5.5D : 0.4D, 0.0D),
                                24,
                                zone.radius * 0.50D
                        );
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float starfallDmg = (8.0F + zTier * 1.85F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 3.5D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 14.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (owner != null && (ent == owner || ApexEschatonSingularityHelper.isFriendly(ent, owner))) {
                                if (starfall) {
                                    SpellCombatReactionEngine.notifyMagicalHeal(ent, 6.0F); // +3 hearts/sec!
                                    ent.heal(6.0F);
                                } else if (ent instanceof Player p) {
                                    SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.ENTROPICA, 120L);
                                }
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, wardSafe)) {
                                continue;
                            }
                            if (starfall) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 8.0D, 0.0D, 220L);
                                if (elapsed % 60 == 0) {
                                    // Lifts mobs into zero-g stasis every few seconds!
                                    ent.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 26, 0));
                                    ent.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 34, 0));
                                } else {
                                    ent.setDeltaMovement(ent.getDeltaMovement().x * 0.2D, -0.65D, ent.getDeltaMovement().z * 0.2D);
                                    ent.hasImpulse = true;
                                }
                                if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                    SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                                            srv, ent, owner, starfallDmg, false, true, false
                                    );
                                }
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, ent, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.ENTROPICA, zone.apparatusPotency * 0.65F
                                    );
                                }
                            } else {
                                // 15.0s Equilibrium Ground: slows hostiles by exact 80%!
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(ent, srv, 0.80D, 35L);
                                ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 5));
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    private static BlockState resolveEquilibriumStarcoreBlockState(int index) {
        return switch (index & 3) {
            case 0 -> ModBlocks.RUNED_ASTRAL_MARBLE.isPresent()
                    ? ModBlocks.RUNED_ASTRAL_MARBLE.get().defaultBlockState()
                    : Blocks.QUARTZ_PILLAR.defaultBlockState();
            case 1 -> Blocks.SEA_LANTERN.defaultBlockState();
            case 2 -> ModBlocks.AETERIUM_CRYSTAL_BLOCK.isPresent()
                    ? ModBlocks.AETERIUM_CRYSTAL_BLOCK.get().defaultBlockState()
                    : Blocks.AMETHYST_BLOCK.defaultBlockState();
            default -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
        };
    }

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
        tag.putString("SpellEidolonMateria", EssenceType.ENTROPICA.name());
        tag.putInt("SpellEidolonTintRGB", ENTROPICA_CHRONO_SILVER_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(2.0D, radius), EssenceType.ENTROPICA, EssenceType.ASTRAL, spell.getEffectiveTier()
        );
        emitEntropicaBurstParticles(level, spawnPos.add(0.0D, 1.1D, 0.0D), 40, 0.65D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE, AFFLICTION & WARD
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.59 Parallax Event-Horizon Fold:
        // Teleports caster up to 24 blocks through solid terrain on an iridescent rift;
        // - Departure: End-portal parallax decoy that taunts & pulls pursuers for 4.0s (80t) before exploding into a balanced stasis nova;
        // - Arrival: 12.0s (240t) 3D arcane flight, Slow Falling, 4.0s (80t) complete damage immunity, and sheds 100% hostile mob aggro!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.ENTROPICA, potencyTier);
        SpellWorldVfxManager.broadcastSummonPillar(level, arrivalPos, 1.6D, EssenceType.ENTROPICA, EssenceType.ASTRAL, potencyTier);

        SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.ENTROPICA,
                32.0F, 80, "Parallax Event-Horizon Decoy", null
        );
        SpellLingeringZoneManager.grantArcane3DFlight(caster, 240);
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 260, 0));
        caster.resetFallDistance();
        CompoundTag cData = EntityHelper.getPersistentData(caster);
        cData.putLong(SpellLingeringZoneManager.TAG_COMPLETE_INVULNERABILITY_UNTIL, level.getGameTime() + 80L);
        cData.putLong(SpellLingeringZoneManager.TAG_VOID_IMMUNE_UNTIL, level.getGameTime() + 240L);

        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 28.0D);
        emitEntropicaBurstParticles(level, arrivalPos.add(0.0D, 0.9D, 0.0D), 28, 0.55D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.45F, 1.35F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.59 Grand Cosmic Equilibrium Transmutation [5.0s Channel, 16m radius, strictly NO fluids, strictly NO empty air]:
        // - Breaks down placed cobblestone, dirt, and gravel directly into collectible Essence Orbs;
        // - Transmutes placed stone, deepslate, and netherrack into Iridescent Primordial Runestone (Runed Astral Marble),
        //   Calcite, and Harmonic Crystal Blocks (Aeterium Crystal Block);
        // - Permanently purges all curses and permanently elevates ALL existing enchantments on worn equipment by +5 tiers!
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }

        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 16, potencyTier, EssenceType.ENTROPICA,
                "Grand Cosmic Equilibrium Transmutation",
                () -> {
                    if (caster != null) {
                        ApexEschatonSingularityHelper.purgeCursesAndElevateEquipment(caster, 5, 1.0F, Integer.MAX_VALUE);
                    }
                })) {
            return true;
        }

        return loomTransmuteSingleBlock(level, pos, state, caster == null ? 1.0F : 0.40F);
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (ApexEschatonSingularityHelper.purgeCursesAndElevateStack(stack, level.random, 5, 1.0F, Integer.MAX_VALUE)) {
            itemEntity.setItem(stack.copy());
            emitEntropicaBurstParticles(level, itemEntity.position().add(0.0D, 0.3D, 0.0D), 8, 0.25D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.59 Grand Equilibrium Paradox Hex:
        // Continuous true Entropica damage, exact -85% movement speed, disables all natural & magical healing,
        // shreds 60% of armor toughness, 60% weapon self-damage recoil, and Ultimate Hybrid Player Hex Vector
        // (Cosmic Paradox Distortion: +200% Spawn-Lure Beacon across 36m AND +100% monster attack speed / +50% crit)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.92D, 0.60D * mult), 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, Math.min(0.94D, 0.85D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 5, true, true));
        target.removeEffect(MobEffects.REGENERATION);

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCasterPublic(level, target);
        float paradoxDmg = (4.0F + potencyTier * 0.95F) * mult;
        SpellCombatReactionEngine.hurtWithEntropicaMateriaAdvantage(
                level, target, hexCaster, paradoxDmg, false, false, true
        );
        emitEntropicaBurstParticles(level, target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D), 6, 0.30D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            Vec3 direction,
            SpellCoreEffect effect,
            int potencyTier,
            SpellAssembly spell
    ) {
        if (effect == SpellCoreEffect.WARD && (caster == null || target == caster || ApexEschatonSingularityHelper.isFriendly(target, caster))) {
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(target);
            target.setAbsorptionAmount(Math.max(target.getAbsorptionAmount(), 10.0F + potencyTier * 2.0F));
        }
    }

    // =========================================================================
    // PRIMORDIAL GENESIS & DISSOLUTION LOOM + PARTICLE HELPERS
    // =========================================================================

    private static void weavePrimordialGenesisAndDissolutionLoom(
            ServerLevel level,
            BlockPos center,
            int radius,
            int maxOperations
    ) {
        int r = Math.clamp(radius, 1, 20);
        int rSq = r * r;
        int ops = 0;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -2, -r), center.offset(r, 2, r))) {
            if (ops >= maxOperations) break;
            if (center.distSqr(p) > (double) rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.isAir() || !bs.getFluidState().isEmpty()) continue;
            if (bs.getDestroySpeed(level, p) < 0.0F) continue;
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (loomTransmuteSingleBlock(level, p, bs, 0.25F)) {
                ops++;
            }
        }
    }

    private static boolean loomTransmuteSingleBlock(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            float orbChance
    ) {
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }
        if (state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }

        // 1. Dissolution Loom: break down placed low-tier cobblestone, dirt, and gravel into collectible Essence Orbs!
        if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE)
                || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.GRAVEL)) {
            level.removeBlock(pos, false);
            if (level.random.nextFloat() < orbChance) {
                SpellLingeringZoneManager.spawnTypedEssenceOrb(level, Vec3.atCenterOf(pos), EssenceType.ENTROPICA, 2);
            }
            return true;
        }

        // 2. Primordial Genesis Loom: transmute placed stone, deepslate, and netherrack into Iridescent Primordial Runestone,
        //    Calcite, and Harmonic Crystal Blocks!
        if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.NETHERRACK)) {
            BlockState out = switch (level.random.nextInt(3)) {
                case 0 -> ModBlocks.RUNED_ASTRAL_MARBLE.isPresent()
                        ? ModBlocks.RUNED_ASTRAL_MARBLE.get().defaultBlockState()
                        : Blocks.QUARTZ_BLOCK.defaultBlockState();
                case 1 -> Blocks.CALCITE.defaultBlockState();
                default -> ModBlocks.AETERIUM_CRYSTAL_BLOCK.isPresent()
                        ? ModBlocks.AETERIUM_CRYSTAL_BLOCK.get().defaultBlockState()
                        : Blocks.AMETHYST_BLOCK.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            return true;
        }
        return false;
    }

    private static void emitEntropicaBurstParticles(ServerLevel level, Vec3 center, int count, double spread) {
        int third = Math.max(1, count / 3);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), ENTROPICA_CHRONO_SILVER_HEX_COLOR, 22, 1.05F, 0.05F),
                center.x, center.y, center.z, third, spread, spread * 0.55D, spread, 0.04D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), ENTROPICA_GENESIS_GOLD_HEX_COLOR, 24, 1.10F, 0.05F),
                center.x, center.y, center.z, third, spread, spread * 0.55D, spread, 0.04D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), ENTROPICA_EVENT_HORIZON_INDIGO_HEX_COLOR, 22, 1.00F, 0.05F),
                center.x, center.y, center.z, third, spread, spread * 0.55D, spread, 0.04D
        );
        level.sendParticles(
                ParticleTypes.END_ROD,
                center.x, center.y, center.z, Math.max(3, count / 4), spread, spread * 0.50D, spread, 0.05D
        );
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                center.x, center.y, center.z, Math.max(3, count / 4), spread, spread * 0.50D, spread, 0.05D
        );
    }
}
