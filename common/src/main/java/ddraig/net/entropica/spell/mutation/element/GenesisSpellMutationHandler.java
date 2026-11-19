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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Genesis Materia (Tier 3 Trinity Fusion #46)</b>
 * (Spellcrafting System Guide Section 6.47 — Trinity Vitae + Radiant + Water harmonic matrix):
 * <ul>
 *     <li><b>Palette</b>: Vitae Blossom Pink ({@code #FF6B9D}) + Radiant Solar Gold ({@code #FFEA78}) + Sacred Oceanic Teal ({@code #225C7C})</li>
 *     <li><b>Advantage</b>: Strong vs <b>UNDEAD (+1)</b>, <b>OBLIVION (+1)</b>, and <b>WRAITH (+1)</b>
 *         (or <b>NETHER</b>, <b>ARID</b>, <b>OBLIVION</b>, <b>WRAITH</b> under Water Dominance) |
 *         Weak vs <b>UMBRAL (-1)</b></li>
 * </ul>
 */
public final class GenesisSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int VITAE_LOTUS_PINK_HEX_COLOR = 0xFF6B9D;
    public static final int DAWN_GENESIS_HEX_COLOR = 0xFFEA78;
    public static final int SACRED_OCEAN_TEAL_HEX_COLOR = 0x225C7C;

    private static final String TAG_GENESIS_BEAM_LAST_TICK = "GenesisBeamLastTick";
    private static final String TAG_GENESIS_BEAM_TICKS = "GenesisBeamContactTicks";
    private static final String TAG_GENESIS_CONE_ARMOR_LAST = "GenesisConeArmorLastTick";
    private static final String TAG_GENESIS_CONE_ARMOR_STACKS = "GenesisConeArmorStacks";
    private static final String TAG_GENESIS_TRANSMUTE_BATCH_TICK = "GenesisTransmuteBatchTick";

    @Override
    public EssenceType materiaType() {
        return EssenceType.GENESIS;
    }

    // =========================================================================
    // TITLES & CODEX / TOOLTIP DESCRIPTIONS
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Dawn-Tide Lotus Orbit Flow";
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return "4 floating crystalline lotus blossoms and radiant water droplets (#FF6B9D / #FFEA78 / #225C7C) orbit the caster, dissolving incoming enemy projectiles, healing nearby allies for 1 heart/s with +20% movement speed, knocking melee attackers 5m back with 3.0s Blindness, and firing smiting lotus darts on melee or spell hit.";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Primordial Dawn-Tear Globule";
            case BEAM -> "Primordial Genesis Creation Ray & Rebirth Pulse";
            case RADIAL_BURST -> "Dawn-Tide Rebirth Nova";
            case TOUCH_STRIKE -> "Primordial Inception Palm-Strike";
            case AURA -> "Amniotic Genesis Shroud";
            case GLYPH -> "Well of Creation & Cleansing Geyser";
            case CONE -> "Dawn-Tide Rejuvenation Deluge";
            case SKYFALL -> "Cataclysmic Waters of Creation Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Launches an iridescent droplet of radiant creation water & cellular vitae (#FF6B9D / #FFEA78 / #225C7C) dealing heavy Genesis Materia damage, smiting Undead, Oblivion & Wraith (+50% damage & -3 armor), and bursting in a 4m splash that heals allies for 3 hearts with Regeneration II while rooting foes for 2.0s.";
            case BEAM ->
                    "Channels a dual-helix torrent of radiant gold & living water (#FF6B9D / #FFEA78 / #225C7C) smiting Undead/Oblivion/Wraith while healing allies (2 hearts/s + saturation); after 5.0s sustained triggers a Cataclysmic Rebirth Pulse (8m knockback, -5 armor & +6 Absorption Hearts + Speed II for 15.0s).";
            case RADIAL_BURST ->
                    "6m Dawn-Tide Rebirth Nova (#FF6B9D / #FFEA78 / #225C7C, zero physical light blocks): hurls hostiles 8m outward, smites Undead/Oblivion/Wraith, strips 3 armor, cures all negative status effects on allies, and leaves a 5.0s Aura of Inception (1 heart/s heal, +4 Armor & +2 Toughness).";
            case TOUCH_STRIKE ->
                    "Strikes with concentrated dawn vitae & sacred tidal water (#FF6B9D / #FFEA78 / #225C7C) dealing heavy Genesis Materia damage, smiting Undead/Oblivion/Wraith (+50% & -3 armor), knocking 6m back, and leaving a 3m Amniotic Waters of Origin puddle (heals allies 2 hearts + Saturation; roots foes 2.5s with +25% vulnerability).";
            case AURA ->
                    "Amniotic Genesis Shroud (#FF6B9D / #FFEA78 / #225C7C): +7 Armor, +3 Toughness, full Blindness/Darkness/Wither/Poison/Drowning immunity (+50% swim speed), absorbs 40% incoming damage into Regeneration II & bonus Genesis Materia fuel, and blinds & knocks melee attackers 4m back.";
            case GLYPH ->
                    "Disarmable Single/Repeating Well of Creation & Cleansing Geyser (#FF6B9D / #FFEA78 / #225C7C): restores 6 hearts, cures curses & grants +4 Absorption Hearts (30s) when stepped on by allies, OR erupts under hostiles launching them 10m high, purging buffs, shredding 3 armor, smiting Undead & inflicting +50% fall vulnerability (6s).";
            case CONE ->
                    "Dawn-Tide Rejuvenation Deluge (#FF6B9D / #FFEA78 / #225C7C): purges all debuffs & curses from caught allies (2 hearts/s + hunger), smites Undead/Oblivion/Wraith (+50% bonus & melts 1 armor/s up to 4), and inflicts Dawn-Glory Delirium (25.0s AI pathfinding disruption & -50% speed).";
            case SKYFALL ->
                    "Extended 18.0s Cataclysmic Waters of Creation Downpour across a 12m zone (#FF6B9D / #FFEA78 / #225C7C): rains 3D amniotic water boulders & sacred light beams knocking hostiles flat, shattering 5 armor, smiting Undead/Oblivion/Wraith, and leaving a shimmering pool (+1 heart/s ally heal & 60% enemy slow) for 18s + 8s after.";
            case SUMMON_AGENT ->
                    "Solar-Hydro Templar (45s lifecycle / 70 HP): swift crusader charging hostiles with water-jet thrusts that bypass 35% armor, deal heavy Genesis Materia damage, smite Undead/Oblivion/Wraith, and blind foes for 3.0s.";
            case SUMMON_SENTRY ->
                    "Dawn-Glory Spire (45s lifecycle / 80 HP): stationary sun-prism spire firing continuous golden water beams at up to 3 hostiles simultaneously up to 16m away, shredding 2 armor per hit and granting adjacent allies +6 Armor & +3 Toughness.";
            case SUMMON_DECOY ->
                    "Amniotic Water-Phantom (12s duration): 14m taunt shimmering liquid decoy detonating into a blinding flash of holy mist (-4 armor, 4.0s Blindness, Undead smite & 6-heart ally heal).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Dawn-Glory Solar Slipstream";
            case TRANSMUTE, BREAK -> "Primordial Creation & Genesis Rebirth Fusion";
            case AFFLICTION -> "Hyper-Cellular Genesis Destabilization Hex";
            default -> "Primordial Genesis " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Rides a surging jet of pressurized solar water (#FF6B9D / #FFEA78 / #225C7C) 18m forward in a blink; knocks aside, blinds for 4.0s, and smites all hostiles passed through while completely shedding hostile mob aggro.";
            case TRANSMUTE, BREAK ->
                    "Requires 5.0s continuous channeling: instantly cures Zombie Villagers into Villagers, transforms Netherrack/Soul Sand/Basalt into Grass, Clay & Flowering Azalea, converts water into Sacred Dawn Water (cures debuffs & 2x crop growth), and cleanses cursed gear.";
            case AFFLICTION ->
                    "Cellular overload curse cutting Max Health by 25%, blocking all conventional healing, shredding 35% armor toughness, staggering attacks, dealing 1 heart of true damage every 3.5s, and expelling radiant sparks that heal adjacent opponents.";
            default -> "Infuses the effect with primordial Genesis Materia resonance (#FF6B9D / #FFEA78 / #225C7C).";
        };
    }

    // =========================================================================
    // PART 1 — FLOW MUTATIONS (DAWN-TIDE LOTUS ORBIT FLOW)
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), VITAE_LOTUS_PINK_HEX_COLOR, 16, 0.85F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), DAWN_GENESIS_HEX_COLOR, 16, 0.82F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
            );
        }
    }

    // =========================================================================
    // PART 2 — DELIVERY FORM MUTATIONS
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
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
        if (hitEntity instanceof LivingEntity directVictim && directVictim.isAlive()) {
            if (caster != null && isFriendlyTarget(directVictim, caster)) {
                splashAmnioticDew(level, caster, directVictim.position(), null, spell);
                return;
            }
            if (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(directVictim, caster, wardSafe)) {
                boolean smite = SpellCombatReactionEngine.isGenesisSmiteTarget(directVictim);
                if (smite) {
                    SpellCombatReactionEngine.applyTimedAttributeShred(directVictim, level, 3.0D, 0.0D, 140L);
                }
                if (!slot.hasHarmVerb()) {
                    float dmg = (3.95F + tier * 0.98F) * (smite ? 1.50F : 1.0F);
                    SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(level, directVictim, caster, dmg, false, true);
                }
            }
        }
        splashAmnioticDew(level, caster, hitEntity.position(), hitEntity, spell);
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
        splashAmnioticDew(level, caster, blockHit.getLocation(), null, spell);
    }

    private static void splashAmnioticDew(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            @Nullable Entity directHit,
            SpellAssembly spell
    ) {
        // Section 6.47 Primordial Dawn-Tear Globule:
        // 4m splash of sacred amniotic dew healing allies for 3 hearts (6.0 HP) with Regeneration II,
        // smiting Undead/Oblivion/Wraith (+50% & -3 armor), and rooting foes in radiant water-bands for 2.0s (40t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, center.add(0.0D, 0.2D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.GENESIS, EssenceType.RADIANT, 1.85F, tier
        );
        SpellWorldVfxManager.broadcastRadialNova(level, center, 4.0D, EssenceType.GENESIS, EssenceType.WATER, tier);

        float splashDmg = 3.15F + tier * 0.78F;
        AABB box = new AABB(
                center.x - 4.0D, center.y - 1.5D, center.z - 4.0D,
                center.x + 4.0D, center.y + 2.5D, center.z + 4.0D
        );
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (caster != null && isFriendlyTarget(ent, caster)) {
                SpellCombatReactionEngine.notifyMagicalHeal(ent, 6.0F);
                ent.heal(6.0F);
                ent.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 1));
                level.sendParticles(ParticleTypes.HEART, ent.getX(), ent.getY(1.0D), ent.getZ(), 4, 0.25D, 0.25D, 0.25D, 0.03D);
                continue;
            }
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) continue;
            boolean smite = SpellCombatReactionEngine.isGenesisSmiteTarget(ent);
            if (smite) {
                SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 3.0D, 0.0D, 140L);
            }
            ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
            ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
            if (ent != directHit) {
                SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(
                        level, ent, caster, splashDmg * (smite ? 1.50F : 1.0F), false, true
                );
            }
        }
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.6D, center.z, 22, 1.4D, 0.5D, 1.4D, 0.05D);
        level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.4D, center.z, 20, 1.4D, 0.4D, 1.4D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.32F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 2 — PRIMORDIAL GENESIS CREATION RAY & REBIRTH PULSE (BEAM)
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
        // Section 6.47 Primordial Genesis Creation Ray & Rebirth Pulse:
        // Smites Undead/Oblivion/Wraith while continuously healing allies for 2 hearts/s (4.0 HP) & restoring saturation;
        // REQUIRES 5.0s (100 contact ticks) continuous channeling to trigger Cataclysmic Rebirth Pulse
        // (8m knockback, melts 5 armor points, and grants Aegis of the First Dawn: +6 Absorption Hearts & Speed II for 15.0s / 300t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        float beamDmg = (2.55F + tier * 0.68F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) {
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 4.0F);
                hit.heal(4.0F); // 2 hearts per second!
                if (hit instanceof Player p) {
                    p.getFoodData().eat(1, 0.5F);
                }
                level.sendParticles(ParticleTypes.HEART, hit.getX(), hit.getY(1.0D), hit.getZ(), 2, 0.25D, 0.25D, 0.25D, 0.02D);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            boolean smite = SpellCombatReactionEngine.isGenesisSmiteTarget(hit);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(
                        level, hit, caster, beamDmg * (smite ? 1.50F : 1.0F), false, true
                );
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_GENESIS_BEAM_LAST_TICK).orElse(-100L);
            long delta = now - last;
            int stepTicks = (delta > 0L && delta <= 8L) ? (int) Math.max(4L, delta) : 20;
            int contactTicks = (delta <= 45L) ? data.getInt(TAG_GENESIS_BEAM_TICKS).orElse(0) + stepTicks : stepTicks;
            data.putLong(TAG_GENESIS_BEAM_LAST_TICK, now);
            data.putInt(TAG_GENESIS_BEAM_TICKS, contactTicks);

            if (contactTicks >= 100) {
                data.putInt(TAG_GENESIS_BEAM_TICKS, 0);
                SpellWorldVfxManager.broadcastRadialNova(level, hit.position(), 4.8D, EssenceType.GENESIS, EssenceType.RADIANT, tier);
                float pulseDmg = (10.50F + tier * 2.50F) * Math.max(0.5F, apparatusPotency);
                for (LivingEntity caught : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(4.8D), e -> e.isAlive() && e != caster)) {
                    if (isFriendlyTarget(caught, caster)) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(caught, caster, wardSafe)) continue;
                    Vec3 away = dirNorm.scale(1.85D);
                    caught.setDeltaMovement(away.x, 0.44D, away.z); // 8m knockback!
                    caught.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(caught, level, 5.0D, 0.0D, 200L);
                    float cSmite = SpellCombatReactionEngine.isGenesisSmiteTarget(caught) ? 1.50F : 1.0F;
                    SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(level, caught, caster, pulseDmg * cSmite, false, true);
                }

                grantFirstDawnAegis(caster);
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(10.0D), e -> isFriendlyTarget(e, caster))) {
                    grantFirstDawnAegis(ally);
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F),
                        hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, hit.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.38F);
            }
        }
    }

    private static void grantFirstDawnAegis(LivingEntity ally) {
        ally.setAbsorptionAmount(Math.max(ally.getAbsorptionAmount(), 12.0F)); // +6 Absorption Hearts (12.0 HP)
        ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 2, true, true));
        ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 300, 1, true, true)); // Speed II for 15.0s (300t)
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 3 — DAWN-TIDE REBIRTH NOVA (RADIAL BURST)
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
        // Section 6.47 Dawn-Tide Rebirth Nova:
        // 6m radius, shaders/bloom only without chunk light recalculations (zero physical light blocks);
        // hurls hostiles 8m outward, smites Undead/Oblivion/Wraith, strips 3 armor points, instantly cures all
        // negative status effects on caster & allies, and leaves a 5.0s (100t) Aura of Inception (+1 heart/s, +4 Armor, +2 Toughness)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        double r = Math.max(6.0D, radius);

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.GENESIS, EssenceType.RADIANT, tier);
        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(caster);

        float novaDmg = (4.25F + tier * 1.05F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(
                center.x - r, center.y - 2.0D, center.z - r,
                center.x + r, center.y + 3.5D, center.z + r
        );
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (isFriendlyTarget(ent, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ent);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) continue;

            Vec3 delta = ent.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.85D) : new Vec3(0.0D, 0.42D, 0.0D);
            ent.setDeltaMovement(away.x, 0.42D, away.z); // 8m outward hurl!
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 3.0D, 0.0D, 160L);
            if (!slot.hasHarmVerb()) {
                float smite = SpellCombatReactionEngine.isGenesisSmiteTarget(ent) ? 1.50F : 1.0F;
                SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(level, ent, caster, novaDmg * smite, false, true);
            }
        }

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, center.add(0.0D, 0.04D, 0.0D), r, EssenceType.GENESIS, EssenceType.WATER, 100, tier
        );
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, r, 100, level.getGameTime(), spell, slot, EssenceType.GENESIS, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB aura = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 3.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, aura, e -> owner != null && isFriendlyTarget(e, owner))) {
                            SpellCombatReactionEngine.notifyMagicalHeal(ally, 2.0F);
                            ally.heal(2.0F); // 1 heart per second!
                            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, srv, 4.0D, 2.0D, 0.0D, 0.0D, 35L);
                        }
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.5D, zone.center.z, 14, 2.0D, 0.3D, 2.0D, 0.03D);
                    }
                    return true;
                }
        ));
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F),
                center.x, center.y + 1.0D, center.z, 1, 0, 0, 0, 0);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.95F, 1.32F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 4 — PRIMORDIAL INCEPTION PALM-STRIKE (TOUCH / STRIKE)
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
        // Section 6.47 Primordial Inception Palm-Strike:
        // Heavy Genesis Materia damage, smites Undead/Oblivion/Wraith (+50% & strips 3 armor), knocks 6m back,
        // and leaves a 3m Amniotic Waters of Origin puddle healing allies for 2 hearts + Saturation while rooting
        // foes 2.5s (50t) and causing +25% increased damage from all attacks!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.3D, 0.0D), push, 2.9D,
                EssenceType.GENESIS, EssenceType.RADIANT, tier
        );

        Vec3 puddlePos = hitEntities.isEmpty() ? caster.position().add(push.scale(2.0D)) : hitEntities.getFirst().position();
        float strikeDmg = (4.30F + tier * 1.08F) * Math.max(0.5F, apparatusPotency);
        long nowInit = level.getGameTime();

        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) {
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 4.0F);
                hit.heal(4.0F);
                if (hit instanceof Player p) p.getFoodData().eat(1, 0.5F);
                level.sendParticles(ParticleTypes.HEART, hit.getX(), hit.getY(1.0D), hit.getZ(), 5, 0.25D, 0.25D, 0.25D, 0.03D);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            hit.setDeltaMovement(push.x * 1.45D, 0.36D, push.z * 1.45D); // 6m knockback!
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL, nowInit + 50L);
            if (!slot.hasHarmVerb()) {
                float smite = SpellCombatReactionEngine.isGenesisSmiteTarget(hit) ? 1.50F : 1.0F;
                SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(level, hit, caster, strikeDmg * smite, true, true);
            }
        }

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, puddlePos.add(0.0D, 0.04D, 0.0D), 3.0D, EssenceType.GENESIS, EssenceType.WATER, 80, tier
        );
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, puddlePos, 3.0D, 80, nowInit, spell, slot, EssenceType.GENESIS, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        long now = srv.getGameTime();
                        AABB box = new AABB(
                                zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                                zone.center.x + 3.0D, zone.center.y + 2.0D, zone.center.z + 3.0D
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (owner != null && isFriendlyTarget(ent, owner)) {
                                SpellCombatReactionEngine.notifyMagicalHeal(ent, 4.0F);
                                ent.heal(4.0F);
                                if (ent instanceof Player p) p.getFoodData().eat(1, 0.5F);
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zSafe)) {
                                ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                                ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL, now + 50L);
                            }
                        }
                        srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 0.2D, zone.center.z, 10, 1.1D, 0.1D, 1.1D, 0.04D);
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 5 — AMNIOTIC GENESIS SHROUD (AURA / SELF)
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
        // Section 6.47 Amniotic Genesis Shroud:
        // +7 Armor, +3 Toughness, full Blindness/Darkness/Wither/Poison/Drowning immunity (+50% swim speed),
        // absorbs 40% damage into Regeneration II & bonus Genesis Materia fuel, and blinds & knocks melee attackers 4m back!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_GENESIS_SHROUD_UNTIL, level.getGameTime() + dur);
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        caster.removeEffect(MobEffects.WITHER);
        caster.removeEffect(MobEffects.POISON);
        caster.setAirSupply(caster.getMaxAirSupply());
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 7.0D, 3.0D, 0.0D, 0.0D, dur);
        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur + 40, 0, false, false));
        caster.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, dur, 1, false, false));
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.58F, null);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.04D);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.30F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 6 — WELL OF CREATION & CLEANSING GEYSER (GLYPH / TRAP)
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
        // Section 6.47 Well of Creation & Cleansing Geyser:
        // Disarmable Single/Repeating dual-purpose radiant water & vitae glyph (#FF6B9D / #FFEA78 / #225C7C);
        // Ally step -> Fountain of Inception (6 hearts / 12.0 HP, cures all curses/hexes, +4 Absorption Hearts for 30.0s / 600t);
        // Hostile step -> Cataclysmic Cleansing Geyser (10m launch, purges buffs, shreds 3 armor, smites Undead, +50% fall vuln 6s)!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.05D, 0.0D), trapRad, EssenceType.GENESIS, EssenceType.RADIANT, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.GENESIS, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.05D, 0.0D), zone.radius, EssenceType.GENESIS, EssenceType.RADIANT, rem, tier
                        );
                    }
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.15D, zone.center.z, 4, 0.7D, 0.05D, 0.7D, 0.01D);
                    }

                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    AABB trigger = new AABB(
                            zone.center.x - 2.6D, zone.center.y - 1.0D, zone.center.z - 2.6D,
                            zone.center.x + 2.6D, zone.center.y + 2.4D, zone.center.z + 2.6D
                    );
                    List<LivingEntity> steppers = srv.getEntitiesOfClass(LivingEntity.class, trigger, LivingEntity::isAlive);
                    if (steppers.isEmpty()) return true;

                    // 1. Fountain of Inception on injured or cursed/hexed friendly ally (or non-caster ally)!
                    for (LivingEntity st : steppers) {
                        if (owner != null && isFriendlyTarget(st, owner)) {
                            boolean needsBlessing = st != owner
                                    || st.getHealth() < st.getMaxHealth() - 0.5F
                                    || st.getAbsorptionAmount() < 8.0F;
                            if (!needsBlessing) continue;

                            triggersLeft[0]--;
                            rearmCooldown[0] = 50;
                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(st);
                            for (EquipmentSlot eq : EquipmentSlot.values()) {
                                PenumbraSpellMutationHandler.purgeCursesFromStack(st.getItemBySlot(eq));
                            }
                            SpellCombatReactionEngine.notifyMagicalHeal(st, 12.0F);
                            st.heal(12.0F); // 6 full hearts!
                            st.setAbsorptionAmount(Math.max(st.getAbsorptionAmount(), 8.0F)); // +4 Absorption Hearts
                            st.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1, true, true)); // 30.0s (600t)
                            SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 3.2D, EssenceType.GENESIS, EssenceType.VITAE, tier);
                            srv.sendParticles(ParticleTypes.HEART, st.getX(), st.getY(1.0D), st.getZ(), 10, 0.35D, 0.4D, 0.35D, 0.03D);
                            srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.38F);
                            return triggersLeft[0] > 0;
                        }
                    }

                    // 2. Cataclysmic Cleansing Geyser on hostile steppers!
                    List<LivingEntity> foes = steppers.stream()
                            .filter(e -> (owner == null || !isFriendlyTarget(e, owner))
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)))
                            .toList();
                    if (foes.isEmpty()) return true;

                    triggersLeft[0]--;
                    rearmCooldown[0] = 50;
                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 3.8D, EssenceType.GENESIS, EssenceType.WATER, tier);
                    long fallVuln = srv.getGameTime() + 120L;
                    float geyserDmg = (4.45F + tier * 1.10F) * Math.max(0.5F, zone.apparatusPotency);
                    for (LivingEntity foe : foes) {
                        MiasmaSpellMutationHandler.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 3.0D, 0.0D, 160L);
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_FALL_VULN_50_UNTIL, fallVuln);
                        foe.setDeltaMovement(foe.getDeltaMovement().x * 0.25D, 1.42D, foe.getDeltaMovement().z * 0.25D); // 10m vertical launch!
                        foe.hasImpulse = true;
                        float smite = SpellCombatReactionEngine.isGenesisSmiteTarget(foe) ? 1.50F : 1.0F;
                        SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(srv, foe, owner, geyserDmg * smite, false, true);
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, foe, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, EssenceType.GENESIS, zone.apparatusPotency
                            );
                        }
                    }
                    srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 0.8D, zone.center.z, 36, 1.2D, 0.9D, 1.2D, 0.14D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 1.12F);
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();

        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 7 — DAWN-TIDE REJUVENATION DELUGE (CONE / SPRAY)
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
        // Section 6.47 Dawn-Tide Rejuvenation Deluge:
        // Purges all negative debuffs & curses from caught allies (+2 hearts/s & hunger);
        // smites Undead/Oblivion/Wraith (+50% bonus & melts 1 armor/s up to 4);
        // inflicts Dawn-Glory Delirium (25.0s / 500t AI pathfinding disruption & -50% movement speed)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), dirNorm, coneRange,
                EssenceType.GENESIS, EssenceType.WATER, tier
        );

        float coneDmg = (3.30F + tier * 0.85F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (isFriendlyTarget(hit, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                for (EquipmentSlot eq : EquipmentSlot.values()) {
                    PenumbraSpellMutationHandler.purgeCursesFromStack(hit.getItemBySlot(eq));
                }
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 4.0F);
                hit.heal(4.0F);
                if (hit instanceof Player p) p.getFoodData().eat(1, 0.4F);
                level.sendParticles(ParticleTypes.HEART, hit.getX(), hit.getY(1.0D), hit.getZ(), 3, 0.25D, 0.25D, 0.25D, 0.02D);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            boolean smite = SpellCombatReactionEngine.isGenesisSmiteTarget(hit);
            CompoundTag hData = EntityHelper.getPersistentData(hit);
            long prevArmorTick = hData.getLong(TAG_GENESIS_CONE_ARMOR_LAST).orElse(-100L);
            int stacks = (now - prevArmorTick <= 45L)
                    ? Math.min(4, hData.getInt(TAG_GENESIS_CONE_ARMOR_STACKS).orElse(0) + 1)
                    : 1;
            hData.putLong(TAG_GENESIS_CONE_ARMOR_LAST, now);
            hData.putInt(TAG_GENESIS_CONE_ARMOR_STACKS, stacks);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, stacks, 0.0D, 120L);

            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.50D, 80L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(
                        level, hit, caster, coneDmg * (smite ? 1.50F : 1.0F), false, true
                );
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 8 — CATACLYSMIC WATERS OF CREATION DOWNPOUR (SKYFALL)
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
        // Section 6.47 Cataclysmic Waters of Creation Downpour:
        // Extended 18.0s (360t) torrential downpour across a 12m+ zone + 8.0s (160t) shimmering pool (520t total);
        // rains 8 falling 3D Skyfall Pseudoblock golden-pink amniotic water boulders alongside heavy radiant dawn droplets
        // and sacred light beams — knocking hostiles flat, shattering 5 armor points, smiting Undead/Oblivion/Wraith,
        // and coating the floor in a shimmering pool (+1 heart/s ally heal & 60% enemy slow across all 520t)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(12.0D, radius);
        BlockPos centerPos = BlockPos.containing(impactCenter);
        double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.GENESIS, EssenceType.RADIANT, 365, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.05D, 0.0D), r, EssenceType.GENESIS, EssenceType.WATER, 520, tier);

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
        Map<BlockPos, BlockState> savedSanctuaryPool = new HashMap<>();

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, EssenceType.GENESIS, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (Map.Entry<BlockPos, BlockState> e : savedSanctuaryPool.entrySet()) {
                            BlockState cur = srv.getBlockState(e.getKey());
                            if (cur.is(Blocks.PRISMARINE) || cur.is(Blocks.SEA_LANTERN) || cur.is(Blocks.FLOWERING_AZALEA)) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        return false;
                    }

                    boolean stormActive = elapsed < 360;
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (stormActive) {
                        // 1. 8 falling 3D amniotic radiant-water boulder pseudoblock columns!
                        for (int mIdx = 0; mIdx < boulderCount; mIdx++) {
                            int impactTick = 20 + mIdx * 42;
                            BlockPos strikePos = boulderTargets[mIdx];
                            boolean dawnCore = (mIdx % 2 == 0);
                            BlockState pseudoState = dawnCore
                                    ? Blocks.SEA_LANTERN.defaultBlockState()
                                    : Blocks.PRISMARINE.defaultBlockState();
                            int colHeight = dawnCore ? 2 : 1;

                            if (elapsed == impactTick - 14) {
                                int holdTicks = Math.max(8, 360 - impactTick);
                                SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                                        srv,
                                        new Vec3(strikePos.getX() + 0.5D, strikePos.getY() + skyH, strikePos.getZ() + 0.5D),
                                        strikePos,
                                        pseudoState,
                                        colHeight,
                                        14,
                                        holdTicks,
                                        EssenceType.GENESIS,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.GENESIS, EssenceType.RADIANT, 2.25F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 20, 1.0D, 0.5D, 1.0D, 0.09D
                                );
                                srv.playSound(null, strikePos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.90F, 1.25F);
                            }
                            if (landed[mIdx]) {
                                SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, strikePos, colHeight, false);
                            }
                        }

                        // 2. Heavy radiant dawn droplets & sacred light beams!
                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 strike = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, strike, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.GENESIS, EssenceType.WATER, 1.45F, zTier
                            );
                            srv.sendParticles(ParticleTypes.SPLASH, strike.x, strike.y + 0.6D, strike.z, 10, 0.7D, 0.5D, 0.7D, 0.06D);
                        }
                    } else if (elapsed == 360) {
                        // Downpour complete: materialize real temporary shimmering sanctuary prismarine/azalea turf for 8.0s (160t)!
                        for (int mIdx = 0; mIdx < boulderCount; mIdx++) {
                            BlockPos strikePos = boulderTargets[mIdx];
                            BlockPos ground = strikePos.below();
                            BlockState gs = srv.getBlockState(ground);
                            if ((gs.is(BlockTags.DIRT) || gs.is(Blocks.STONE) || gs.is(Blocks.COBBLESTONE))
                                    && !savedSanctuaryPool.containsKey(ground)
                                    && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, ground, gs)) {
                                savedSanctuaryPool.put(ground.immutable(), gs);
                                srv.setBlockAndUpdate(ground, Blocks.PRISMARINE.defaultBlockState());
                            }
                            BlockState above = srv.getBlockState(strikePos);
                            if (above.isAir()
                                    && Blocks.FLOWERING_AZALEA.defaultBlockState().canSurvive(srv, strikePos)
                                    && !savedSanctuaryPool.containsKey(strikePos)
                                    && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, strikePos, above)) {
                                savedSanctuaryPool.put(strikePos.immutable(), above);
                                srv.setBlockAndUpdate(strikePos, Blocks.FLOWERING_AZALEA.defaultBlockState());
                            }
                        }
                    }

                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD,
                                zone.center.x, zone.center.y + (stormActive ? 3.8D : 0.35D), zone.center.z,
                                20, zone.radius * 0.55D, stormActive ? 1.8D : 0.2D, zone.radius * 0.55D, 0.04D);
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float stormDmg = (3.65F + zTier * 0.90F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (owner != null && isFriendlyTarget(ent, owner)) {
                                SpellCombatReactionEngine.notifyMagicalHeal(ent, 2.0F);
                                ent.heal(2.0F); // +1 heart/s ally healing!
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, wardSafe)) continue;

                            // 60% shimmering pool slow across all 520t (18s + 8s)!
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(ent, srv, 0.60D, 35L);
                            ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3));
                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 5.0D, 0.0D, 160L);
                                ent.setDeltaMovement(ent.getDeltaMovement().x * 0.3D, -0.38D, ent.getDeltaMovement().z * 0.3D);
                                ent.hasImpulse = true;
                                float smite = SpellCombatReactionEngine.isGenesisSmiteTarget(ent) ? 1.50F : 1.0F;
                                SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(
                                        srv, ent, owner, stormDmg * smite, false, true
                                );
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, ent, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.GENESIS, zone.apparatusPotency * 0.45F
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
    // FORM MUTATION 9 — CREATION TEMPLAR TRIAD (SUMMON / EIDOLON)
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
        tag.putString("SpellEidolonMateria", EssenceType.GENESIS.name());
        tag.putInt("SpellEidolonTintRGB", DAWN_GENESIS_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.8D, radius * 0.55D), EssenceType.GENESIS, EssenceType.RADIANT, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.04D);
    }

    // =========================================================================
    // PART 3 — CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.47 Dawn-Glory Solar Slipstream:
        // Rides a surging jet of pressurized solar water (#FF6B9D / #FFEA78 / #225C7C) 18m forward in a blink;
        // knocks aside, blinds for 4.0s (80t), and smites all hostiles passed through while completely shedding mob aggro!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.GENESIS, potencyTier);

        AABB path = new AABB(departurePos, arrivalPos).inflate(1.6D);
        for (LivingEntity passed : level.getEntitiesOfClass(LivingEntity.class, path, e -> e.isAlive() && e != caster)) {
            if (isFriendlyTarget(passed, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(passed, caster, true)) continue;
            Vec3 delta = passed.position().subtract(departurePos);
            Vec3 side = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.15D) : new Vec3(0.0D, 0.34D, 0.0D);
            passed.setDeltaMovement(side.x, 0.34D, side.z);
            passed.hasImpulse = true;
            passed.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            float smite = SpellCombatReactionEngine.isGenesisSmiteTarget(passed) ? 1.50F : 1.0F;
            SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(
                    level, passed, caster, (3.85F + potencyTier * 0.95F) * smite, false, true
            );
        }
        for (Mob mob : level.getEntitiesOfClass(Mob.class, path.inflate(16.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
        }
        level.sendParticles(ParticleTypes.SPLASH, departurePos.x, departurePos.y + 0.8D, departurePos.z, 22, 0.4D, 0.4D, 0.4D, 0.08D);
        level.sendParticles(ParticleTypes.END_ROD, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 20, 0.4D, 0.4D, 0.4D, 0.05D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS, 0.80F, 1.25F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.47 Primordial Creation & Genesis Rebirth Fusion [5.0s Continuous Catalytic Channel]:
        // Instantly cures Zombie Villagers back to healthy Villagers; turns Netherrack, Soul Sand, and Basalt into
        // Grass Blocks, Clay, and Flowering Azalea; turns standing Water into Sacred Dawn Water (curing debuffs &
        // doubling crop growth); and cleanses cursed equipment of negative enchantments!
        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 5, potencyTier, EssenceType.GENESIS, "Primordial Creation & Genesis Rebirth Fusion", null)) {
            return true;
        }

        boolean didAny = SpringSpellMutationHandler.cureNearbyZombieVillagers(level, pos);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D), ItemEntity::isAlive)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        if (caster != null) {
            for (EquipmentSlot eq : EquipmentSlot.values()) {
                if (PenumbraSpellMutationHandler.purgeCursesFromStack(caster.getItemBySlot(eq))) {
                    didAny = true;
                }
            }
        }
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            if (didAny) refundBatchDiscount(level, caster, potencyTier);
            return didAny;
        }

        if (state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL) || state.is(Blocks.BASALT)) {
            BlockState out = switch (level.random.nextInt(3)) {
                case 0 -> Blocks.GRASS_BLOCK.defaultBlockState();
                case 1 -> Blocks.CLAY.defaultBlockState();
                default -> Blocks.MOSS_BLOCK.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            if (level.getBlockState(pos.above()).isAir()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, pos.above(), level.getBlockState(pos.above()))
                    && level.random.nextBoolean()) {
                level.setBlockAndUpdate(pos.above(), Blocks.FLOWERING_AZALEA.defaultBlockState());
            }
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.WATER)) {
            registerSacredDawnWaterZone(level, caster, pos.immutable());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (didAny) {
            refundBatchDiscount(level, caster, potencyTier);
        }
        return didAny;
    }

    private static void registerSacredDawnWaterZone(ServerLevel level, @Nullable LivingEntity caster, BlockPos waterPos) {
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, Vec3.atCenterOf(waterPos), 2.5D, 2400, level.getGameTime(), null, null, EssenceType.GENESIS, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || !srv.getBlockState(waterPos).is(Blocks.WATER)) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        for (LivingEntity bather : srv.getEntitiesOfClass(LivingEntity.class, new AABB(waterPos).inflate(1.5D), LivingEntity::isAlive)) {
                            if (owner == null || isFriendlyTarget(bather, owner) || bather instanceof Player) {
                                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(bather);
                            }
                        }
                        for (ItemEntity item : srv.getEntitiesOfClass(ItemEntity.class, new AABB(waterPos).inflate(1.2D), ItemEntity::isAlive)) {
                            ItemStack st = item.getItem();
                            if (st.is(Items.GLASS_BOTTLE)) {
                                item.setItem(PotionContents.createItemStack(Items.POTION, Potions.STRONG_REGENERATION).copyWithCount(st.getCount()));
                                srv.playSound(null, waterPos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.75F, 1.25F);
                            }
                        }
                    }
                    if (elapsed % 40 == 0) {
                        SpringSpellMutationHandler.matureCropsInRadius(srv, waterPos, 4); // 2x crop fluid acceleration!
                        srv.sendParticles(ParticleTypes.END_ROD, waterPos.getX() + 0.5D, waterPos.getY() + 0.95D, waterPos.getZ() + 0.5D,
                                3, 0.35D, 0.05D, 0.35D, 0.01D);
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (PenumbraSpellMutationHandler.purgeCursesFromStack(stack)) {
            itemEntity.setItem(stack.copy());
            return true;
        }
        if (stack.is(Items.NETHERRACK) || stack.is(Items.BASALT) || stack.is(Items.SOUL_SAND)) {
            itemEntity.setItem(new ItemStack(Items.FLOWERING_AZALEA, stack.getCount()));
            return true;
        }
        if (stack.is(Items.GLASS_BOTTLE)) {
            itemEntity.setItem(PotionContents.createItemStack(Items.POTION, Potions.STRONG_REGENERATION).copyWithCount(stack.getCount()));
            return true;
        }
        return false;
    }

    private static void refundBatchDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (caster instanceof Player player) {
            CompoundTag pData = EntityHelper.getPersistentData(player);
            long now = level.getGameTime();
            if (pData.getLong(TAG_GENESIS_TRANSMUTE_BATCH_TICK).orElse(-10L) != now) {
                pData.putLong(TAG_GENESIS_TRANSMUTE_BATCH_TICK, now);
                long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
                SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.GENESIS, refund);
            }
        }
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.47 Hyper-Cellular Genesis Destabilization Hex:
        // Cuts maximum health by 25% & blocks all conventional healing (enforced in SpellCombatReactionEngine),
        // shreds 35% armor toughness, staggers attacks, deals 1 heart (2.0 HP) of true damage every 3.5s (70t),
        // and periodically expels radiant sparks that heal adjacent opponents!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 45L);
        target.stopUsingItem();

        LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float smite = SpellCombatReactionEngine.isGenesisSmiteTarget(target) ? 1.50F : 1.0F;
        SpellCombatReactionEngine.hurtWithGenesisMateriaAdvantage(
                level, target, caster, (1.45F + potencyTier * 0.45F) * mult * smite, false, true
        );

        if ((now % 70L) < 20L && (now % 20L) == 0L) {
            target.invulnerableTime = 0;
            target.hurtServer(level, level.damageSources().fellOutOfWorld(), 2.0F * mult); // 1 heart true damage!
            if (caster != null && caster.isAlive() && caster.distanceToSqr(target) <= 100.0D) {
                SpellCombatReactionEngine.notifyMagicalHeal(caster, 2.5F);
                caster.heal(2.5F);
            } else {
                for (Monster opp : level.getEntitiesOfClass(Monster.class, target.getBoundingBox().inflate(5.0D), e -> e != target && e.isAlive())) {
                    opp.heal(2.5F);
                    break;
                }
            }
            level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.9D), target.getZ(), 10, 0.35D, 0.4D, 0.35D, 0.04D);
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
