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
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
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
 * Complete per-part Signature Mutation implementation for <b>Sylvan Materia (Tier 3 Trinity Fusion #44)</b>
 * (Spellcrafting System Guide Section 6.45 — Trinity Nature + Earth + Radiant harmonic matrix):
 * <ul>
 *     <li><b>Palette</b>: Emerald Canopy Green ({@code #1A5923}) + Petrified Bark Brown ({@code #773214}) + Solar Canopy Gold ({@code #FFEA78})</li>
 *     <li><b>Advantage</b>: Strong vs <b>WATER (+1)</b>, <b>EARTH (+1)</b>, <b>UNDEAD (+1)</b>, <b>BARROW (+1)</b>,
 *         and <b>WRAITH (+1)</b> (or <b>VOID</b>, <b>UNDEAD</b>, <b>BARROW</b>, <b>WRAITH</b> under Earth Dominance) |
 *         Weak vs <b>NETHER (-1)</b> and <b>AIR (-1)</b></li>
 * </ul>
 */
public final class SylvanSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int PRIMEVAL_SYLVAN_HEX_COLOR = 0x1A5923;
    public static final int IRONWOOD_BARK_HEX_COLOR = 0x773214;
    public static final int CANOPY_SOLAR_GOLD_HEX_COLOR = 0xFFEA78;

    private static final String TAG_SYLVAN_BEAM_LAST_TICK = "SylvanBeamLastTick";
    private static final String TAG_SYLVAN_BEAM_TICKS = "SylvanBeamContactTicks";
    private static final String TAG_SYLVAN_TRANSMUTE_BATCH_TICK = "SylvanTransmuteBatchTick";

    private static final Block[] FLORAL_TERRACOTTA_BLOCKS = {
            Blocks.YELLOW_GLAZED_TERRACOTTA,
            Blocks.GREEN_GLAZED_TERRACOTTA,
            Blocks.LIME_GLAZED_TERRACOTTA,
            Blocks.BROWN_GLAZED_TERRACOTTA
    };

    @Override
    public EssenceType materiaType() {
        return EssenceType.SYLVAN;
    }

    // =========================================================================
    // TITLES & CODEX / TOOLTIP DESCRIPTIONS
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Photosynthetic Sun-Canopy Orbit Flow";
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return "4 golden solar prisms and petrified leaves (#1A5923 / #773214 / #FFEA78) focus ambient sunlight to boost spell damage by +20%, grant +5 Armor, +2 Toughness, complete Poison/Wither immunity, passive Sylvan Materia regeneration in direct sunlight, 1.5s melee attacker rooting, and 2.0s-rooting Undead-smiting solar darts.";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Elderwood Sun-Bough Javelin";
            case BEAM -> "Canopy Solar-Root Tether Beam";
            case RADIAL_BURST -> "Primeval Sylvan Canopy Nova";
            case TOUCH_STRIKE -> "Elderwood Canopy-Cleave";
            case AURA -> "Primeval Photosynthetic Aegis";
            case GLYPH -> "Canopy Solar-Mine";
            case CONE -> "Canopy Solar-Flare Breath";
            case SKYFALL -> "Sun-Canopy Orbital Meteor Barrage";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Hurls a dense javelin of petrified ironwood, golden solar light & thorny briars (#1A5923 / #773214 / #FFEA78) dealing heavy Sylvan Materia damage, smiting Undead (+50% bonus damage), rooting the target for 3.0s, shattering 3 armor points, and sprouting a 4.0s entangled briar cluster (-50% enemy speed).";
            case BEAM ->
                    "Channels sun-dappled golden light & living ironwood roots (#1A5923 / #773214 / #FFEA78) that sear hostiles, drag enemies 2m/s closer, smite Undead, and heal caught allies (1 heart/s); after 5.0s sustained triggers Primeval Arbor Overgrowth (8m knockback + 3.0s wooden root cages).";
            case RADIAL_BURST ->
                    "6m Primeval Sylvan Canopy Nova (#1A5923 / #773214 / #FFEA78, zero physical light blocks): hurls hostiles 7m back, smites Undead, roots caught foes for 3.0s, instantly matures crops/saplings, and grants allies Ironwood Barkskin (+6 Armor, +2 Toughness & Knockback Resistance for 8.0s).";
            case TOUCH_STRIKE ->
                    "Sweeping Elderwood Canopy-Cleave (#1A5923 / #773214 / #FFEA78) dealing heavy Sylvan Materia damage, smiting Undead (+50% damage & -3 armor), knocking foes 6m back, rooting the target for 2.5s, and leaving a living foliage arc that roots crossing enemies for 2.0s while granting Regeneration I to allies.";
            case AURA ->
                    "Primeval Photosynthetic Aegis (#1A5923 / #773214 / #FFEA78): +7 Armor, +3 Toughness, complete Poison/Wither/Blindness immunity, and absorbs up to 12 incoming damage instances while converting 50% of incoming damage into Sylvan Materia fuel.";
            case GLYPH ->
                    "Disarmable Single/Repeating floating Canopy Solar-Mine (#1A5923 / #773214 / #FFEA78): detonates when hostiles approach within 3m, blinding enemies for 5.0s, launching them airborne, stripping 3 armor points, and dealing heavy Sylvan Materia damage.";
            case CONE ->
                    "Canopy Solar-Flare Breath (#1A5923 / #773214 / #FFEA78): purges Poison & Wither from allies, ignites enemies in holy arbor flame, deals continuous Sylvan Materia damage, and applies Solar Arbor Blindness (-50% attack speed & 25.0s AI pathfinding disruption).";
            case SKYFALL ->
                    "16.0s Sun-Canopy Orbital Meteor Barrage across a 10m zone (#1A5923 / #773214 / #FFEA78): rains 3D petrified ironwood boulders & 0.4s solar seed strikes that launch hostiles airborne, shatter 5 armor, smite Undead, and slow enemies by 60% for 16s + 8s after.";
            case SUMMON_AGENT ->
                    "Ironwood Juggernaut (45s lifecycle / 70 HP): massive armored warrior delivering earth-shattering ground slams that bypass 35% of enemy armor, deal heavy Sylvan damage, smite Undead, and root targets for 2.5s.";
            case SUMMON_SENTRY ->
                    "Petrified Monolith Sentry (45s lifecycle / 85 HP): anchored stone-timber totem raining stone & petrified wood blocks up to 16m, pulsing an 8m root wave every 4.0s, and granting allies +6 Armor, +3 Toughness & Knockback Resistance.";
            case SUMMON_DECOY ->
                    "Thorn-Bramble Effigy (12s duration): 14m taunt dense briar effigy reflecting 35% of melee damage before detonating into a 10m burst (-4 armor, heavy Sylvan damage & 4.0s petrified root-cages).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Canopy Solar-Glide Step";
            case TRANSMUTE, BREAK -> "Petrified Amber-Bark Fusion";
            case AFFLICTION -> "Petrifying Arbor-Root Entombment Hex";
            default -> "Primeval Sylvan " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Launches upward and forward up to 16m on a surging gust of golden solar leaves (#1A5923 / #773214 / #FFEA78), granting Slow Falling and complete fall damage immunity for 6.0s while shedding all hostile mob aggro.";
            case TRANSMUTE, BREAK ->
                    "Converts wood logs into Amber-Wood, turns clay into floral Terracotta, accelerates crops & saplings across a 6m radius, and purifies Netherrack/Soul Sand into Moss Blocks & Ferns (25% batch fuel discount).";
            case AFFLICTION ->
                    "Persistent botanical petrification curse rooting & stunning victims for 1.0s every 3.5s, cutting move speed by 60%, attack speed by 40%, stripping 35% toughness, dealing crushing Sylvan ticks (+50% vs Undead), and dropping 1-heart golden acorn healing orbs.";
            default -> "Infuses the effect with primeval Sylvan Materia resonance (#1A5923 / #773214 / #FFEA78).";
        };
    }

    // =========================================================================
    // PART 1 — FLOW MUTATIONS (PHOTOSYNTHETIC SUN-CANOPY ORBIT FLOW)
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), PRIMEVAL_SYLVAN_HEX_COLOR, 16, 0.85F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), CANOPY_SOLAR_GOLD_HEX_COLOR, 16, 0.80F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
        }
    }

    // =========================================================================
    // PART 2 — DELIVERY FORM MUTATIONS
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D);
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
        // Section 6.45 Elderwood Sun-Bough Javelin:
        // Heavy Sylvan Materia damage, smites Undead (+50% bonus damage), roots primary target 3.0s (60t),
        // shatters 3 armor points, and sprouts an entangled briar cluster slowing nearby hostiles by 50% for 4.0s (80t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        if (hitEntity instanceof LivingEntity victim && victim.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, wardSafe))) {
            boolean isUndead = victim.getType().is(EntityTypeTags.UNDEAD);
            if (isUndead) {
                victim.igniteForSeconds(4);
            }
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
            victim.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(victim, level, 3.0D, 0.0D, 140L);
            if (!slot.hasHarmVerb()) {
                float baseDmg = (3.80F + tier * 0.95F) * (isUndead ? 1.50F : 1.0F);
                SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(level, victim, caster, baseDmg, false, true);
            }
        }
        sproutBriarCluster(level, caster, hitEntity.position(), spell);
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
        sproutBriarCluster(level, caster, blockHit.getLocation(), spell);
    }

    private static void sproutBriarCluster(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        int tier = spell.getEffectiveTier();
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, center.add(0.0D, 0.2D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.SYLVAN, EssenceType.RADIANT, 1.70F, tier
        );
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, center.add(0.0D, 0.04D, 0.0D), 2.8D, EssenceType.SYLVAN, EssenceType.EARTH, 80, tier
        );
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, 2.8D, 80, level.getGameTime(), spell, null, EssenceType.SYLVAN, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB box = new AABB(
                                zone.center.x - 2.8D, zone.center.y - 1.0D, zone.center.z - 2.8D,
                                zone.center.x + 2.8D, zone.center.y + 2.0D, zone.center.z + 2.8D
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && isFriendlyTarget(foe, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zSafe)) continue;
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.50D, 30L);
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 2));
                            float smite = foe.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
                            SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(
                                    srv, foe, owner, (1.85F + tier * 0.45F) * smite, false, true
                            );
                        }
                        srv.sendParticles(ParticleTypes.HAPPY_VILLAGER, zone.center.x, zone.center.y + 0.3D, zone.center.z, 8, 1.0D, 0.2D, 1.0D, 0.02D);
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 2 — CANOPY SOLAR-ROOT TETHER BEAM (BEAM)
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
        // Section 6.45 Canopy Solar-Root Tether Beam:
        // Sears hostiles with Sylvan Materia damage, drags caught enemies 2m/s closer to caster, smites Undead
        // with solar combustion, and heals friendly allies (1 heart / 2.0 HP per second);
        // REQUIRES 5.0s (100 contact ticks) sustained channeling to trigger Primeval Arbor Overgrowth
        // (8m knockback + 3.0s / 60t wooden root cages)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        float beamDmg = (2.40F + tier * 0.62F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) {
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 2.0F);
                hit.heal(2.0F); // 1 heart per second!
                level.sendParticles(ParticleTypes.HEART, hit.getX(), hit.getY(1.0D), hit.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.02D);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            Vec3 toCaster = caster.position().subtract(hit.position());
            if (toCaster.lengthSqr() > 1.0E-4D) {
                Vec3 pull = toCaster.normalize().scale(0.38D); // ~2m/s pull toward caster!
                hit.setDeltaMovement(pull.x, 0.08D, pull.z);
                hit.hasImpulse = true;
            }
            boolean isUndead = hit.getType().is(EntityTypeTags.UNDEAD);
            if (isUndead) {
                hit.igniteForSeconds(3);
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(
                        level, hit, caster, beamDmg * (isUndead ? 1.50F : 1.0F), false, true
                );
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_SYLVAN_BEAM_LAST_TICK).orElse(-100L);
            long delta = now - last;
            int stepTicks = (delta > 0L && delta <= 8L) ? (int) Math.max(4L, delta) : 20;
            int contactTicks = (delta <= 45L) ? data.getInt(TAG_SYLVAN_BEAM_TICKS).orElse(0) + stepTicks : stepTicks;
            data.putLong(TAG_SYLVAN_BEAM_LAST_TICK, now);
            data.putInt(TAG_SYLVAN_BEAM_TICKS, contactTicks);

            if (contactTicks >= 100) {
                data.putInt(TAG_SYLVAN_BEAM_TICKS, 0);
                SpellWorldVfxManager.broadcastRadialNova(level, hit.position(), 4.5D, EssenceType.SYLVAN, EssenceType.RADIANT, tier);
                float burstDmg = (10.5F + tier * 2.50F) * Math.max(0.5F, apparatusPotency);
                for (LivingEntity caught : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(4.5D), e -> e.isAlive() && e != caster)) {
                    if (isFriendlyTarget(caught, caster)) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(caught, caster, wardSafe)) continue;
                    Vec3 away = dirNorm.scale(1.85D);
                    caught.setDeltaMovement(away.x, 0.44D, away.z); // 8m knockback!
                    caught.hasImpulse = true;
                    caught.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 60, 0));
                    caught.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                    float smite = caught.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
                    SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(level, caught, caster, burstDmg * smite, false, true);
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F),
                        hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, hit.blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.PLAYERS, 1.0F, 0.68F);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 3 — PRIMEVAL SYLVAN CANOPY NOVA (RADIAL BURST)
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
        // Section 6.45 Primeval Sylvan Canopy Nova:
        // 6m radius, strictly ZERO physical light blocks — hurls hostiles 7m backward, deals heavy Sylvan Materia
        // damage, smites Undead with holy arbor flame, roots caught hostiles 3.0s (60t), instantly grows crops/saplings,
        // and grants caster & allies Ironwood Barkskin (+6 Armor, +2 Toughness, KB Resistance for 8.0s / 160t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        double r = Math.max(6.0D, radius);

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.SYLVAN, EssenceType.RADIANT, tier);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 2.0D, 0.50D, 0.0D, 160L);

        float novaDmg = (4.20F + tier * 1.05F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (isFriendlyTarget(ent, caster)) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 6.0D, 2.0D, 0.50D, 0.0D, 160L);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) continue;
            Vec3 delta = ent.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.65D) : new Vec3(0.0D, 0.38D, 0.0D);
            ent.setDeltaMovement(away.x, 0.38D, away.z); // 7m knockback!
            ent.hasImpulse = true;
            ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
            ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
            boolean isUndead = ent.getType().is(EntityTypeTags.UNDEAD);
            if (isUndead) {
                ent.igniteForSeconds(5);
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(
                        level, ent, caster, novaDmg * (isUndead ? 1.50F : 1.0F), false, true
                );
            }
        }
        SpringSpellMutationHandler.matureCropsInRadius(level, BlockPos.containing(center), (int) Math.min(8, Math.ceil(r)));
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F),
                center.x, center.y + 1.0D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, center.x, center.y + 0.9D, center.z, 32, 2.2D, 0.6D, 2.2D, 0.05D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.15F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 4 — ELDERWOOD CANOPY-CLEAVE (TOUCH / STRIKE)
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
        // Section 6.45 Elderwood Canopy-Cleave:
        // Heavy Sylvan Materia damage, smites Undead (+50% bonus damage & strips 3 armor points), knocks enemies 6m back,
        // roots primary target 2.5s (50t), and leaves a crescent foliage arc rooting enemies 2.0s while granting Regen I to allies!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), push, 2.8D,
                EssenceType.SYLVAN, EssenceType.RADIANT, tier
        );

        float cleaveDmg = (4.45F + tier * 1.10F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) {
                hit.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0));
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            boolean isUndead = hit.getType().is(EntityTypeTags.UNDEAD);
            if (isUndead) {
                hit.igniteForSeconds(4);
            }
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            hit.setDeltaMovement(push.x * 1.45D, 0.36D, push.z * 1.45D); // 6m knockback!
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(
                        level, hit, caster, cleaveDmg * (isUndead ? 1.50F : 1.0F), true, true
                );
            }
        }

        Vec3 arcPos = caster.position().add(push.scale(2.4D));
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, arcPos.add(0.0D, 0.04D, 0.0D), 2.5D, EssenceType.SYLVAN, EssenceType.RADIANT, 80, tier
        );
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, arcPos, 2.5D, 80, level.getGameTime(), spell, slot, EssenceType.SYLVAN, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB box = new AABB(
                                zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                                zone.center.x + 2.5D, zone.center.y + 1.8D, zone.center.z + 2.5D
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (owner != null && isFriendlyTarget(ent, owner)) {
                                ent.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 0));
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zSafe)) {
                                ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
                                ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
                            }
                        }
                        srv.sendParticles(ParticleTypes.HAPPY_VILLAGER, zone.center.x, zone.center.y + 0.2D, zone.center.z, 6, 1.0D, 0.1D, 1.0D, 0.02D);
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 5 — PRIMEVAL PHOTOSYNTHETIC AEGIS (AURA / SELF)
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
        // Section 6.45 Primeval Photosynthetic Aegis:
        // +7 Armor, +3 Toughness, complete immunity to Poison, Wither & Blindness;
        // absorbs up to 12 incoming damage instances converting 50% directly into bonus Sylvan Materia fuel!
        int dur = 200 + spell.getEffectiveTier() * 40;
        CompoundTag tag = EntityHelper.getPersistentData(caster);
        tag.putLong(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL, level.getGameTime() + dur);
        tag.putInt(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_HITS, 12);
        caster.removeEffect(MobEffects.POISON);
        caster.removeEffect(MobEffects.WITHER);
        caster.removeEffect(MobEffects.BLINDNESS);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 7.0D, 3.0D, 0.0D, 0.0D, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.60F, null);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, caster.getX(), caster.getY(0.9D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.03D);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.25F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 6 — CANOPY SOLAR-MINE (GLYPH / TRAP)
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
        // Section 6.45 Canopy Solar-Mine:
        // Disarmable Single/Repeating hidden floating leaf-prism mine (#1A5923 / #773214 / #FFEA78);
        // when hostiles approach within 3m, violently detonates blinding enemies 5.0s (100t), launching them airborne,
        // stripping 3 armor points, and dealing heavy Sylvan Materia damage!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.45D, 0.0D), trapRad, EssenceType.SYLVAN, EssenceType.RADIANT, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.SYLVAN, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.45D, 0.0D), zone.radius, EssenceType.SYLVAN, EssenceType.RADIANT, rem, tier
                        );
                    }
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.55D, zone.center.z, 3, 0.5D, 0.2D, 0.5D, 0.01D);
                    }

                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    AABB trigger = new AABB(
                            zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                            zone.center.x + 3.0D, zone.center.y + 2.6D, zone.center.z + 3.0D
                    );
                    List<LivingEntity> foes = srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner
                            && (owner == null || !isFriendlyTarget(e, owner))
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)));
                    if (foes.isEmpty()) return true;

                    triggersLeft[0]--;
                    rearmCooldown[0] = 50;

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 3.6D, EssenceType.SYLVAN, EssenceType.RADIANT, tier);
                    float mineDmg = (4.35F + tier * 1.05F) * Math.max(0.5F, zone.apparatusPotency);
                    for (LivingEntity v : foes) {
                        v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                        SpellCombatReactionEngine.applyTimedAttributeShred(v, srv, 3.0D, 0.0D, 160L);
                        v.setDeltaMovement(v.getDeltaMovement().x * 0.4D, 0.90D, v.getDeltaMovement().z * 0.4D);
                        v.hasImpulse = true;
                        float smite = v.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
                        SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(srv, v, owner, mineDmg * smite, false, true);
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, EssenceType.SYLVAN, zone.apparatusPotency
                            );
                        }
                    }
                    srv.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F),
                            zone.center.x, zone.center.y + 0.8D, zone.center.z, 1, 0, 0, 0, 0);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.95F, 1.15F);
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();

        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 7 — CANOPY SOLAR-FLARE BREATH (CONE / SPRAY)
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
        // Section 6.45 Canopy Solar-Flare Breath:
        // Purges Poison & Wither from caught allies; ignites enemies in holy arbor flame, deals continuous
        // Sylvan Materia damage, and applies Solar Arbor Blindness (-50% attack speed & 25.0s / 500t AI disruption)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), dirNorm, coneRange,
                EssenceType.SYLVAN, EssenceType.RADIANT, tier
        );

        float coneDmg = (3.20F + tier * 0.82F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (isFriendlyTarget(hit, caster)) {
                hit.removeEffect(MobEffects.POISON);
                hit.removeEffect(MobEffects.WITHER);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, hit.getX(), hit.getY(1.0D), hit.getZ(), 4, 0.25D, 0.25D, 0.25D, 0.02D);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            hit.igniteForSeconds(4);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            SpellCombatReactionEngine.applyTimedAttackDebuff(hit, level, 0.0D, 0.50D, 120L);
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
            if (!slot.hasHarmVerb()) {
                float smite = hit.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
                SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(level, hit, caster, coneDmg * smite, false, true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 8 — SUN-CANOPY ORBITAL METEOR BARRAGE (SKYFALL)
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
        // Section 6.45 Sun-Canopy Orbital Meteor Barrage:
        // Extended 16.0s (320t) orbital barrage across a 10m+ zone + 8.0s (160t) lingering entangled root field (480t total);
        // rains 8 falling 3D Skyfall Pseudoblock petrified ironwood & mossy stone boulders alongside 0.4s (8t) solar seed strikes,
        // launching hostiles airborne, shattering 5 armor points, smiting Undead, and slowing enemies by 60% across all 480t!
        int tier = spell.getEffectiveTier();
        double r = Math.max(10.0D, radius);
        BlockPos centerPos = BlockPos.containing(impactCenter);
        double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.SYLVAN, EssenceType.RADIANT, 325, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.05D, 0.0D), r, EssenceType.SYLVAN, EssenceType.EARTH, 480, tier);

        final int boulderCount = 8;
        final BlockPos[] boulderTargets = new BlockPos[boulderCount];
        boulderTargets[0] = findGroundSurfaceNear(level, centerPos).above();
        for (int i = 1; i < boulderCount; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / (boulderCount - 1.0D)) + level.random.nextDouble() * 0.40D;
            double dist = 0.22D * r + level.random.nextDouble() * (0.58D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            boulderTargets[i] = findGroundSurfaceNear(level, raw).above();
        }

        final boolean[] landed = new boolean[boulderCount];
        Map<BlockPos, BlockState> savedRootTurf = new HashMap<>();

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 480, level.getGameTime(), spell, slot, EssenceType.SYLVAN, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (Map.Entry<BlockPos, BlockState> e : savedRootTurf.entrySet()) {
                            BlockState cur = srv.getBlockState(e.getKey());
                            if (cur.is(Blocks.MOSS_BLOCK) || cur.is(Blocks.FERN) || cur.is(ModBlocks.AMBER_LOG.get())) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        return false;
                    }

                    boolean barrageActive = elapsed < 320;
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (barrageActive) {
                        // 1. 8 falling 3D petrified ironwood & stone boulder pseudoblocks!
                        for (int mIdx = 0; mIdx < boulderCount; mIdx++) {
                            int impactTick = 20 + mIdx * 38;
                            BlockPos strikePos = boulderTargets[mIdx];
                            boolean ironwood = (mIdx % 2 == 0);
                            BlockState pseudoState = ironwood
                                    ? ModBlocks.AMBER_LOG.get().defaultBlockState()
                                    : Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                            int colHeight = ironwood ? 2 : 1;

                            if (elapsed == impactTick - 14) {
                                int holdTicks = Math.max(8, 320 - impactTick);
                                SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                                        srv,
                                        new Vec3(strikePos.getX() + 0.5D, strikePos.getY() + skyH, strikePos.getZ() + 0.5D),
                                        strikePos,
                                        pseudoState,
                                        colHeight,
                                        14,
                                        holdTicks,
                                        EssenceType.SYLVAN,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.SYLVAN, EssenceType.RADIANT, 2.2F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 20, 1.0D, 0.5D, 1.0D, 0.09D
                                );
                                srv.playSound(null, strikePos, SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 0.85F, 0.72F);
                            }
                            if (landed[mIdx]) {
                                SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, strikePos, colHeight, false);
                            }
                        }

                        // 2. Solar meteoritic seed strikes every 0.4s (8 ticks)!
                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 strike = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, strike, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.SYLVAN, EssenceType.RADIANT, 1.45F, zTier
                            );
                            srv.sendParticles(ParticleTypes.END_ROD, strike.x, strike.y + 1.0D, strike.z, 10, 0.7D, 0.8D, 0.7D, 0.05D);
                        }
                    } else if (elapsed == 320) {
                        // Barrage complete: materialize real temporary entangled moss/fern turf for the 8.0s (160t) root field!
                        for (int mIdx = 0; mIdx < boulderCount; mIdx++) {
                            BlockPos strikePos = boulderTargets[mIdx];
                            BlockPos ground = strikePos.below();
                            BlockState gs = srv.getBlockState(ground);
                            if ((gs.is(BlockTags.DIRT) || gs.is(Blocks.STONE) || gs.is(Blocks.COBBLESTONE))
                                    && !savedRootTurf.containsKey(ground)
                                    && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, ground, gs)) {
                                savedRootTurf.put(ground.immutable(), gs);
                                srv.setBlockAndUpdate(ground, Blocks.MOSS_BLOCK.defaultBlockState());
                            }
                            BlockState above = srv.getBlockState(strikePos);
                            if (above.isAir()
                                    && Blocks.FERN.defaultBlockState().canSurvive(srv, strikePos)
                                    && !savedRootTurf.containsKey(strikePos)
                                    && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, strikePos, above)) {
                                savedRootTurf.put(strikePos.immutable(), above);
                                srv.setBlockAndUpdate(strikePos, Blocks.FERN.defaultBlockState());
                            }
                        }
                    }

                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float barrageDmg = (3.45F + zTier * 0.86F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && isFriendlyTarget(foe, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                            // 60% entangled root slow for full 16s storm + 8s after (480t)!
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.60D, 35L);
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3));
                            if (barrageActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 160L);
                                foe.setDeltaMovement(foe.getDeltaMovement().x * 0.4D, 0.48D, foe.getDeltaMovement().z * 0.4D);
                                foe.hasImpulse = true;
                                boolean isUndead = foe.getType().is(EntityTypeTags.UNDEAD);
                                if (isUndead) {
                                    foe.igniteForSeconds(4);
                                }
                                SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(
                                        srv, foe, owner, barrageDmg * (isUndead ? 1.50F : 1.0F), false, true
                                );
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.SYLVAN, zone.apparatusPotency * 0.45F
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
    // FORM MUTATION 9 — IRONWOOD COLOSSUS SUITE (SUMMON / EIDOLON)
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
        tag.putString("SpellEidolonMateria", EssenceType.SYLVAN.name());
        tag.putInt("SpellEidolonTintRGB", PRIMEVAL_SYLVAN_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.8D, radius * 0.55D), EssenceType.SYLVAN, EssenceType.RADIANT, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.04D);
    }

    // =========================================================================
    // PART 3 — CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.45 Canopy Solar-Glide Step:
        // Launches upward & forward up to 16m on a surging gust of golden solar leaves, granting Slow Falling
        // and complete fall damage immunity for 6.0s (120t) while shedding all hostile mob aggro!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.SYLVAN, potencyTier);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, arrivalPos).inflate(18.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
        }
        EntityHelper.getPersistentData(caster).putLong(
                SpellLingeringZoneManager.TAG_SYLVAN_SOLAR_GLIDE_UNTIL, level.getGameTime() + 120L
        );
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0));
        caster.resetFallDistance();
        Vec3 curVel = caster.getDeltaMovement();
        caster.setDeltaMovement(curVel.x, Math.max(curVel.y, 0.34D), curVel.z);
        caster.hasImpulse = true;
        level.sendParticles(ParticleTypes.END_ROD, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 18, 0.4D, 0.4D, 0.4D, 0.04D);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, departurePos.x, departurePos.y + 0.8D, departurePos.z, 14, 0.4D, 0.4D, 0.4D, 0.03D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.45 Petrified Amber-Bark Fusion:
        // Converts ordinary wood logs into amber-encrusted Petrified Wood (Amber-Wood); turns clay into floral
        // Terracotta; accelerates crops & saplings across a 6m radius; purifies Netherrack/Soul Sand into Moss & Ferns!
        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D), ItemEntity::isAlive)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        SpringSpellMutationHandler.matureCropsInRadius(level, pos, 6);

        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            if (didAny) refundBatchDiscount(level, caster, potencyTier);
            return didAny;
        }

        if (state.is(BlockTags.LOGS) && !state.is(ModBlocks.AMBER_LOG.get()) && !state.is(ModBlocks.AMBER_WOOD.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AMBER_LOG.get().withPropertiesOf(state));
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.CLAY)) {
            Block floral = FLORAL_TERRACOTTA_BLOCKS[level.random.nextInt(FLORAL_TERRACOTTA_BLOCKS.length)];
            level.setBlockAndUpdate(pos, floral.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)) {
            level.setBlockAndUpdate(pos, Blocks.MOSS_BLOCK.defaultBlockState());
            if (level.getBlockState(pos.above()).isAir()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, pos.above(), level.getBlockState(pos.above()))) {
                level.setBlockAndUpdate(pos.above(), Blocks.FERN.defaultBlockState());
            }
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (didAny) {
            refundBatchDiscount(level, caster, potencyTier);
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(ItemTags.LOGS) && !stack.is(ModBlocks.AMBER_LOG.get().asItem())) {
            itemEntity.setItem(new ItemStack(ModBlocks.AMBER_LOG.get().asItem(), stack.getCount()));
            return true;
        }
        if (stack.is(Items.CLAY) || stack.is(Items.CLAY_BALL)) {
            Block floral = FLORAL_TERRACOTTA_BLOCKS[level.random.nextInt(FLORAL_TERRACOTTA_BLOCKS.length)];
            int outCount = stack.is(Items.CLAY_BALL) ? Math.max(1, stack.getCount() / 4) : stack.getCount();
            itemEntity.setItem(new ItemStack(floral.asItem(), outCount));
            return true;
        }
        if (stack.is(Items.NETHERRACK) || stack.is(Items.SOUL_SAND) || stack.is(Items.SOUL_SOIL)) {
            itemEntity.setItem(new ItemStack(Items.MOSS_BLOCK, stack.getCount()));
            return true;
        }
        return false;
    }

    private static void refundBatchDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (caster instanceof Player player) {
            CompoundTag pData = EntityHelper.getPersistentData(player);
            long now = level.getGameTime();
            if (pData.getLong(TAG_SYLVAN_TRANSMUTE_BATCH_TICK).orElse(-10L) != now) {
                pData.putLong(TAG_SYLVAN_TRANSMUTE_BATCH_TICK, now);
                long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
                SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.SYLVAN, refund);
            }
        }
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.45 Petrifying Arbor-Root Entombment Hex:
        // Periodically roots & stuns 1.0s (20t) every 3.5s (70t), cuts movement speed by 60%, reduces attack speed
        // by 40%, strips 35% armor toughness, inflicts crushing Sylvan Materia ticks (+50% vs Undead), and drops
        // collectable 1-heart (2.0 HP) golden acorn healing orbs!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, Math.min(0.85D, 0.60D * mult), 45L);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 3, true, true)); // -60% speed

        LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        if ((now % 70L) < 20L && (now % 20L) == 0L) {
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
            SpellLingeringZoneManager.spawnCollectableHealingOrb(level, target.position(), caster, 2.0F, true);
        }

        float smite = target.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
        float dmg = (1.60F + potencyTier * 0.52F) * mult * smite;
        SpellCombatReactionEngine.hurtWithSylvanMateriaAdvantage(level, target, caster, dmg, false, true);

        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY(0.85D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.02D);
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
