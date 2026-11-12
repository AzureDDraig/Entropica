package ddraig.net.entropica.spell.engine;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.combat.VisInteractionManager;
import ddraig.net.entropica.component.EssenceCombatStats;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModEntityTags;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SpellFlawProfile;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.MateriaFamilyTier;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.mutation.handler.SpellMutationHandlerRegistry;
import ddraig.net.entropica.util.EntityHelper;
import dev.architectury.event.EventResult;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Handles pure elemental Materia spell damage, the 2-tick multi-hit invulnerability regulator,
 * Opposing Elemental Clashes & Harmonic Mergers, Persistent Indefinite Elemental Hexes
 * (with Level I–III stacking, elemental counter-cleansing, caster-death dispel, natural regen lock,
 * boss tenacity caps, and player spawn-lure beacons), real transient AttributeModifiers, and
 * Blue Arcane Ward Shield damage interception.
 */
public final class SpellCombatReactionEngine {

    public static final long INDEFINITE_HEX_TICKS = 72_000_000L;

    private static final String TAG_LAST_SPELL_HIT_TICK = "LastSpellHitTick";
    private static final String TAG_ACTIVE_ELEMENT = "ActiveSpellElement";
    private static final String TAG_ACTIVE_ELEMENT_UNTIL = "ActiveSpellElementUntil";
    private static final String TAG_ARMOR_SHRED = "SpellArmorShredPoints";
    private static final String TAG_ARMOR_SHRED_UNTIL = "SpellArmorShredUntil";
    private static final String TAG_ATTR_SHRED_UNTIL = "SpellAttrShredUntil";
    private static final String TAG_ATTR_OFFENSE_SHRED_UNTIL = "SpellAttrOffenseShredUntil";
    private static final String TAG_ATTR_DEF_BUFF_UNTIL = "SpellAttrDefBuffUntil";

    private static final String TAG_HEX_TYPE = "SpellHexType";
    private static final String TAG_HEX_TIER = "SpellHexTier";
    private static final String TAG_HEX_STACK = "SpellHexStackLevel";
    private static final String TAG_HEX_MUTATED = "SpellHexMutated";
    private static final String TAG_HEX_UNTIL = "SpellHexUntil";
    private static final String TAG_HEX_INITIAL_WINDOW = "SpellHexInitialWindowUntil";
    private static final String TAG_HEX_BOSS_IMMUNE_UNTIL = "SpellHexBossImmuneUntil";
    private static final String TAG_HEX_CASTER_UUID = "SpellHexCasterUuid";
    private static final String TAG_HEX_MAX_ALLOWED_HP = "SpellHexMaxAllowedHp";

    private static final String TAG_WARD_HP = "SpellWardShieldHp";
    private static final String TAG_WARD_UNTIL = "SpellWardUntil";
    private static final String TAG_WARD_MATERIA = "SpellWardMateria";

    private static final ResourceLocation MOD_ARMOR_SHRED_FLAT =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_armor_shred_flat");
    private static final ResourceLocation MOD_ARMOR_SHRED_PCT =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_armor_shred_pct");
    private static final ResourceLocation MOD_TOUGHNESS_SHRED_FLAT =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_toughness_shred_flat");
    private static final ResourceLocation MOD_TOUGHNESS_SHRED_PCT =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_toughness_shred_pct");
    private static final ResourceLocation MOD_DEFENSE_ARMOR_BUFF =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_defense_armor_buff");
    private static final ResourceLocation MOD_DEFENSE_TOUGHNESS_BUFF =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_defense_toughness_buff");
    private static final ResourceLocation MOD_DEFENSE_KB_RES_BUFF =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_defense_kb_res_buff");
    private static final ResourceLocation MOD_DEFENSE_SPEED_MULT =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_defense_speed_mult");
    private static final ResourceLocation MOD_ATTACK_DAMAGE_SHRED_PCT =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_attack_damage_shred_pct");
    private static final ResourceLocation MOD_ATTACK_SPEED_SHRED_PCT =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_attack_speed_shred_pct");
    private static final ResourceLocation MOD_ATTACK_SPEED_BUFF_PCT =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_attack_speed_buff_pct");
    private static final ResourceLocation MOD_HEX_MAX_HEALTH_CUT_PCT =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_hex_max_health_cut_pct");

    private static final ThreadLocal<Boolean> REENTRANT_GUST_HURT = ThreadLocal.withInitial(() -> false);

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private SpellCombatReactionEngine() {
    }

    /**
     * Evaluates friendly-fire protection for owned Eidolons and Ward-safeguarded allies (Section 2.1.4).
     */
    public static boolean isProtectedFromOffensiveSpell(
            LivingEntity target,
            @Nullable LivingEntity caster,
            boolean hasWardSafeguard
    ) {
        if (caster == null) return false;
        if (target == caster) return true;
        if (target instanceof SpellEidolonEntity eidolon && eidolon.isOwnedBy(caster)) {
            return true;
        }
        if (!hasWardSafeguard) return false;

        if (target.isAlliedTo(caster)) return true;
        if (target instanceof OwnableEntity ownable && ownable.getOwner() == caster) {
            return true;
        }
        return false;
    }

    /**
     * Enforces the 2-tick (0.1s) multi-hit spell regulator so multi-projectile / dual-verb
     * spell barrages bypass vanilla's 10-tick i-frame window without infinite 0-tick instant-kill loops.
     */
    public static boolean acquireMultiHitWindow(ServerLevel level, LivingEntity target) {
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        long lastHit = data.getLong(TAG_LAST_SPELL_HIT_TICK).orElse(-100L);
        if (now - lastHit < 2L) {
            return false;
        }
        data.putLong(TAG_LAST_SPELL_HIT_TICK, now);
        target.invulnerableTime = 0;
        return true;
    }

    // --- Real Transient Attribute Modifiers (Armor, Toughness, Knockback Resistance) ---

    public static void applyTimedAttributeShred(
            LivingEntity target,
            ServerLevel level,
            double flatArmorShred,
            double flatToughnessShred,
            long durationTicks
    ) {
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        data.putLong(TAG_ATTR_SHRED_UNTIL, Math.max(now + Math.max(20L, durationTicks), data.getLong(TAG_ATTR_SHRED_UNTIL).orElse(0L)));

        if (flatArmorShred > 0.0D) {
            AttributeInstance arm = target.getAttribute(Attributes.ARMOR);
            if (arm != null) {
                arm.removeModifier(MOD_ARMOR_SHRED_FLAT);
                arm.addTransientModifier(new AttributeModifier(MOD_ARMOR_SHRED_FLAT, -flatArmorShred, AttributeModifier.Operation.ADD_VALUE));
            }
            int pts = Math.min(8, (int) Math.ceil(flatArmorShred));
            data.putInt(TAG_ARMOR_SHRED, Math.max(pts, data.getInt(TAG_ARMOR_SHRED).orElse(0)));
            data.putLong(TAG_ARMOR_SHRED_UNTIL, now + Math.max(20L, durationTicks));
        }

        if (flatToughnessShred > 0.0D) {
            AttributeInstance tough = target.getAttribute(Attributes.ARMOR_TOUGHNESS);
            if (tough != null) {
                tough.removeModifier(MOD_TOUGHNESS_SHRED_FLAT);
                tough.addTransientModifier(new AttributeModifier(MOD_TOUGHNESS_SHRED_FLAT, -flatToughnessShred, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    public static void applyPercentageArmorShred(
            LivingEntity target,
            ServerLevel level,
            double armorFraction,
            double toughnessFraction,
            long durationTicks
    ) {
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        data.putLong(TAG_ATTR_SHRED_UNTIL, Math.max(now + Math.max(20L, durationTicks), data.getLong(TAG_ATTR_SHRED_UNTIL).orElse(0L)));

        if (armorFraction > 0.0D) {
            AttributeInstance arm = target.getAttribute(Attributes.ARMOR);
            if (arm != null) {
                arm.removeModifier(MOD_ARMOR_SHRED_PCT);
                arm.addTransientModifier(new AttributeModifier(
                        MOD_ARMOR_SHRED_PCT,
                        -Math.clamp(armorFraction, 0.05D, 0.85D),
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        }
        if (toughnessFraction > 0.0D) {
            AttributeInstance tough = target.getAttribute(Attributes.ARMOR_TOUGHNESS);
            if (tough != null) {
                tough.removeModifier(MOD_TOUGHNESS_SHRED_PCT);
                tough.addTransientModifier(new AttributeModifier(
                        MOD_TOUGHNESS_SHRED_PCT,
                        -Math.clamp(toughnessFraction, 0.05D, 0.85D),
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        }
    }

    public static void applyTimedDefenseBuff(
            LivingEntity target,
            ServerLevel level,
            double bonusToughness,
            double bonusKnockbackRes,
            long durationTicks
    ) {
        applyTimedArmorAndToughnessBuff(target, level, 0.0D, bonusToughness, bonusKnockbackRes, 0.0D, durationTicks);
    }

    public static void applyTimedArmorAndToughnessBuff(
            LivingEntity target,
            ServerLevel level,
            double bonusArmor,
            double bonusToughness,
            double bonusKnockbackRes,
            double moveSpeedMult,
            long durationTicks
    ) {
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        data.putLong(TAG_ATTR_DEF_BUFF_UNTIL, Math.max(now + Math.max(10L, durationTicks), data.getLong(TAG_ATTR_DEF_BUFF_UNTIL).orElse(0L)));

        if (bonusArmor != 0.0D) {
            AttributeInstance arm = target.getAttribute(Attributes.ARMOR);
            if (arm != null) {
                arm.removeModifier(MOD_DEFENSE_ARMOR_BUFF);
                arm.addTransientModifier(new AttributeModifier(MOD_DEFENSE_ARMOR_BUFF, bonusArmor, AttributeModifier.Operation.ADD_VALUE));
            }
        }
        if (bonusToughness != 0.0D) {
            AttributeInstance tough = target.getAttribute(Attributes.ARMOR_TOUGHNESS);
            if (tough != null) {
                tough.removeModifier(MOD_DEFENSE_TOUGHNESS_BUFF);
                tough.addTransientModifier(new AttributeModifier(MOD_DEFENSE_TOUGHNESS_BUFF, bonusToughness, AttributeModifier.Operation.ADD_VALUE));
            }
        }
        if (bonusKnockbackRes != 0.0D) {
            AttributeInstance kb = target.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kb != null) {
                kb.removeModifier(MOD_DEFENSE_KB_RES_BUFF);
                kb.addTransientModifier(new AttributeModifier(MOD_DEFENSE_KB_RES_BUFF, bonusKnockbackRes, AttributeModifier.Operation.ADD_VALUE));
            }
        }
        if (moveSpeedMult != 0.0D) {
            AttributeInstance spd = target.getAttribute(Attributes.MOVEMENT_SPEED);
            if (spd != null) {
                spd.removeModifier(MOD_DEFENSE_SPEED_MULT);
                spd.addTransientModifier(new AttributeModifier(MOD_DEFENSE_SPEED_MULT, moveSpeedMult, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }

    /**
     * Applies real transient percentage reductions to {@link Attributes#ATTACK_DAMAGE} and/or {@link Attributes#ATTACK_SPEED}
     * (e.g., Storm Hypoxia -40% attack speed, Oasis Sunstroke -35% attack speed, Vitae Arrhythmia -40% attack damage,
     * Aura Chakra Disruption -25% attack power, Amber Fossilizing Strike -40% attack speed).
     */
    public static void applyTimedAttackDebuff(
            LivingEntity target,
            ServerLevel level,
            double attackDamageReductionFraction,
            double attackSpeedReductionFraction,
            long durationTicks
    ) {
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        data.putLong(TAG_ATTR_OFFENSE_SHRED_UNTIL,
                Math.max(now + Math.max(15L, durationTicks), data.getLong(TAG_ATTR_OFFENSE_SHRED_UNTIL).orElse(0L)));

        if (attackDamageReductionFraction > 0.0D) {
            AttributeInstance dmg = target.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmg != null) {
                dmg.removeModifier(MOD_ATTACK_DAMAGE_SHRED_PCT);
                dmg.addTransientModifier(new AttributeModifier(
                        MOD_ATTACK_DAMAGE_SHRED_PCT,
                        -Math.clamp(attackDamageReductionFraction, 0.05D, 0.85D),
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        }
        if (attackSpeedReductionFraction > 0.0D) {
            AttributeInstance atkSpd = target.getAttribute(Attributes.ATTACK_SPEED);
            if (atkSpd != null) {
                atkSpd.removeModifier(MOD_ATTACK_SPEED_SHRED_PCT);
                atkSpd.addTransientModifier(new AttributeModifier(
                        MOD_ATTACK_SPEED_SHRED_PCT,
                        -Math.clamp(attackSpeedReductionFraction, 0.05D, 0.85D),
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        }
    }

    public static void applyTimedAttackSpeedBuff(
            LivingEntity target,
            ServerLevel level,
            double attackSpeedBonusFraction,
            long durationTicks
    ) {
        if (attackSpeedBonusFraction <= 0.0D) return;
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        data.putLong(TAG_ATTR_DEF_BUFF_UNTIL,
                Math.max(now + Math.max(15L, durationTicks), data.getLong(TAG_ATTR_DEF_BUFF_UNTIL).orElse(0L)));
        AttributeInstance atkSpd = target.getAttribute(Attributes.ATTACK_SPEED);
        if (atkSpd != null) {
            atkSpd.removeModifier(MOD_ATTACK_SPEED_BUFF_PCT);
            atkSpd.addTransientModifier(new AttributeModifier(
                    MOD_ATTACK_SPEED_BUFF_PCT,
                    Math.clamp(attackSpeedBonusFraction, 0.05D, 1.50D),
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    /**
     * Scales percentage lifesteal directly from the canonical 10-Tier {@link MateriaStageTable#getBaseCombatDamageHp(int)}
     * curve (4 HP at Tier 1 up to 2,500 HP at Tier 10), unlocking hex natural-regeneration ceilings as well.
     */
    public static float applyTierScaledLifesteal(
            ServerLevel level,
            @Nullable LivingEntity recipient,
            int tier,
            float lifestealFraction,
            float potencyMultiplier
    ) {
        if (recipient == null || !recipient.isAlive() || lifestealFraction <= 0.0F) {
            return 0.0F;
        }
        float baseHp = MateriaStageTable.getBaseCombatDamageHp(tier);
        float stolen = Math.max(0.8F, baseHp * lifestealFraction * Math.max(0.25F, potencyMultiplier));
        notifyMagicalHeal(recipient, stolen);
        recipient.heal(stolen);
        level.sendParticles(ParticleTypes.HEART, recipient.getX(), recipient.getY(0.95D), recipient.getZ(),
                Math.min(6, 1 + (int) (stolen / 3.0F)), 0.25D, 0.25D, 0.25D, 0.03D);
        return stolen;
    }

    /**
     * Restores a percentage of a spell tier's canonical {@link MateriaStageTable#getBaseMfumCastCost(int)}
     * back into the player's Orbis Cells (used by Aura siphons: 15% Chain, 20% Beam/Agent, 30% Touch).
     */
    public static long siphonCasterMateriaFuel(
            Player player,
            @Nullable EssenceType preferredType,
            int tier,
            float costFraction
    ) {
        if (!player.isAlive() || costFraction <= 0.0F) return 0L;
        long baseCost = MateriaStageTable.getBaseMfumCastCost(tier);
        long restored = Math.max(8L, Math.round(baseCost * costFraction));
        return SpellFuelService.restorePlayerMateriaFuel(player, preferredType, restored);
    }

    private static void clearShredModifiers(LivingEntity target, CompoundTag data) {
        AttributeInstance arm = target.getAttribute(Attributes.ARMOR);
        if (arm != null) {
            arm.removeModifier(MOD_ARMOR_SHRED_FLAT);
            arm.removeModifier(MOD_ARMOR_SHRED_PCT);
        }
        AttributeInstance tough = target.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (tough != null) {
            tough.removeModifier(MOD_TOUGHNESS_SHRED_FLAT);
            tough.removeModifier(MOD_TOUGHNESS_SHRED_PCT);
        }
        data.remove(TAG_ATTR_SHRED_UNTIL);
    }

    private static void clearOffenseShredModifiers(LivingEntity target, CompoundTag data) {
        AttributeInstance dmg = target.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) {
            dmg.removeModifier(MOD_ATTACK_DAMAGE_SHRED_PCT);
        }
        AttributeInstance atkSpd = target.getAttribute(Attributes.ATTACK_SPEED);
        if (atkSpd != null) {
            atkSpd.removeModifier(MOD_ATTACK_SPEED_SHRED_PCT);
        }
        data.remove(TAG_ATTR_OFFENSE_SHRED_UNTIL);
    }

    private static void clearDefenseBuffModifiers(LivingEntity target, CompoundTag data) {
        AttributeInstance arm = target.getAttribute(Attributes.ARMOR);
        if (arm != null) {
            arm.removeModifier(MOD_DEFENSE_ARMOR_BUFF);
        }
        AttributeInstance tough = target.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (tough != null) {
            tough.removeModifier(MOD_DEFENSE_TOUGHNESS_BUFF);
        }
        AttributeInstance kb = target.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kb != null) {
            kb.removeModifier(MOD_DEFENSE_KB_RES_BUFF);
        }
        AttributeInstance spd = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) {
            spd.removeModifier(MOD_DEFENSE_SPEED_MULT);
        }
        AttributeInstance atkSpd = target.getAttribute(Attributes.ATTACK_SPEED);
        if (atkSpd != null) {
            atkSpd.removeModifier(MOD_ATTACK_SPEED_BUFF_PCT);
        }
        data.remove(TAG_ATTR_DEF_BUFF_UNTIL);
    }

    /**
     * Applies pure elemental Materia spell damage, calculating:
     * <ul>
     *     <li>10-Tier {@link MateriaStageTable} base HP scaling</li>
     *     <li>Per-part {@link SpellMutationProfile} potency & lifesteal (strictly unmutated when toggled OFF)</li>
     *     <li>{@link VisInteractionManager} Materia Advantage + {@link SpellFlowModifier} innate advantage bonus</li>
     *     <li>Hex incoming damage multipliers (Earth 2.0x, Void 1.5x) & Elemental Counter-Cleansing</li>
     *     <li>Target {@link EssenceCombatStats} armor Materia resistance</li>
     *     <li>{@link MateriaFamilyTier} progressive armor bypass (0% -> 75%) & Broken Armor shred</li>
     *     <li>Resonite critical strike roll (+75% damage)</li>
     *     <li>Opposing Clashes & Harmonic Mergers</li>
     * </ul>
     */
    public static float applyElementalHarm(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            SpellAssembly spell,
            EssenceType effectMateria,
            SpellMutationProfile effectMutation,
            float apparatusPotencyMult
    ) {
        if (!target.isAlive() || isProtectedFromOffensiveSpell(target, caster, spell.hasWardFriendlyFireSafeguard())) {
            return 0.0F;
        }
        if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        int tier = spell.getEffectiveTier();
        float baseHp = MateriaStageTable.getBaseCombatDamageHp(tier);
        SpellFlawProfile flaw = spell.flawProfile();

        // 1. Mutation, Apparatus, and Flaw multipliers
        float damage = baseHp
                * effectMutation.potencyMultiplier()
                * Math.max(0.25F, apparatusPotencyMult)
                * flaw.getPotencyMultiplier();

        // 2. Materia Advantage via VisInteractionManager + Mutated Flow Advantage Bonus
        EssenceType flowMateria = spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), EssenceType.REGULAR);
        MateriaPartMutationHandler flowHandler = SpellMutationHandlerRegistry.resolveActive(flowMateria, spell.flowPart().mutationEnabled());
        float flowBonusAdv = flowHandler != null ? flowHandler.getFlowBonusAdvantageScore(spell.flowPart().flow(), target) : 0.0F;

        if (effectMateria != null && effectMateria != EssenceType.REGULAR) {
            Set<EssenceType> defenderAffinities = resolveEntityAffinities(target);
            if (effectMateria == EssenceType.ENTROPICA) {
                // Sec 6.59 Entropica: +1 Elemental Advantage against EVERYTHING, 0 weaknesses!
                float totalScore = 1.0F + flowBonusAdv;
                float advMult = Math.clamp(1.0F + (totalScore * 0.25F), 1.25F, 1.85F) + spell.flowPart().flow().getInnateAdvantageBonus();
                damage *= advMult;
            } else if (!defenderAffinities.isEmpty()) {
                int advScore = calculateSpellElementalAdvantageScore(effectMateria, defenderAffinities);
                float totalScore = advScore + flowBonusAdv;
                float advMult = Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
                if (totalScore > 0) {
                    advMult += spell.flowPart().flow().getInnateAdvantageBonus();
                }
                damage *= advMult;
            } else if (spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY) {
                damage *= (1.15F + flowBonusAdv * 0.15F);
            }
        }

        // Earth Slow-Heavy ("Dense Tectonic Mass" Section 6.5): guaranteed 2-point Armor Shred on impact
        if (spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY
                && spell.flowPart().mutationEnabled()
                && flowMateria == EssenceType.EARTH) {
            applyTimedAttributeShred(target, level, 2.0D, 0.0D, 140L);
        }

        // 2b. Earth Defensive Attunement, Water Soaked & Active Hex Multipliers
        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (caster != null
                && now <= EntityHelper.getPersistentData(caster).getLong(SpellLingeringZoneManager.TAG_SYLVAN_ORBIT_UNTIL).orElse(0L)) {
            damage *= 1.20F; // #44 Photosynthetic Sun-Canopy Orbit Flow: +20% spell damage!
        }
        if (spell.flowPart().mutationEnabled() && flowMateria == EssenceType.CHIMERA) {
            // Sec 6.49 Chimera Unstable Quad-Elemental Mutation Flow: 25% Air / 25% Earth / 25% Water / 25% Nether per trigger!
            String rolled = caster != null
                    ? EntityHelper.getPersistentData(caster).getString(SpellLingeringZoneManager.TAG_CHIMERA_ORBIT_ELEMENT).orElse("")
                    : "";
            if (rolled.isEmpty()) {
                rolled = switch (level.random.nextInt(4)) {
                    case 0 -> "AIR";
                    case 1 -> "EARTH";
                    case 2 -> "WATER";
                    default -> "NETHER";
                };
            }
            switch (rolled) {
                case "AIR" -> {
                    Vec3 away = caster != null
                            ? target.position().subtract(caster.position()).normalize().scale(0.95D)
                            : new Vec3(0.0D, 0.35D, 0.0D);
                    target.setDeltaMovement(away.x, 0.36D, away.z);
                    target.hasImpulse = true;
                    if (caster != null) caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 50, 0, true, false));
                }
                case "EARTH" -> {
                    damage *= 1.15F;
                    applyTimedAttributeShred(target, level, 2.0D, 0.0D, 100L);
                }
                case "WATER" -> {
                    SpellLingeringZoneManager.applySoaked(target, 120);
                    if (caster != null) {
                        Vec3 pull = caster.position().subtract(target.position()).normalize().scale(0.45D);
                        target.setDeltaMovement(pull.x, 0.15D, pull.z);
                        target.hasImpulse = true;
                    }
                }
                default -> target.igniteForSeconds(4);
            }
        }
        if (effectMutation.mutated()) {
            Set<EssenceType> targetAffs = resolveEntityAffinities(target);
            boolean isUndeadOrAbyssal = target.getType().is(EntityTypeTags.UNDEAD)
                    || targetAffs.contains(EssenceType.VOID)
                    || targetAffs.contains(EssenceType.NETHER);
            if ((effectMateria == EssenceType.SPRING || effectMateria == EssenceType.SYLVAN)
                    && target.getType().is(EntityTypeTags.UNDEAD)) {
                damage *= 1.50F; // +50% Undead smite bonus (#42 Spring & #44 Sylvan)!
            } else if (effectMateria == EssenceType.GENESIS
                    && (target.getType().is(EntityTypeTags.UNDEAD)
                    || hasActiveMutatedHex(targetData, now, EssenceType.OBLIVION)
                    || hasActiveMutatedHex(targetData, now, EssenceType.WRAITH))) {
                damage *= 1.50F; // +50% Undead/Oblivion/Wraith smite bonus (#46 Genesis)!
            } else if (effectMateria == EssenceType.CELESTIAL && isUndeadOrAbyssal) {
                boolean projOrSkyfall = !spell.subSpells().isEmpty() && (
                        spell.subSpells().getFirst().formPart().form() == ddraig.net.entropica.spell.SpellDeliveryForm.PROJECTILE
                                || spell.subSpells().getFirst().formPart().form() == ddraig.net.entropica.spell.SpellDeliveryForm.SKYFALL);
                damage *= projOrSkyfall ? 3.50F : 1.75F; // Sec 6.52 Celestial: 3.5x on Projectile/Skyfall vs Undead/Nether/Void!
            } else if (effectMateria == EssenceType.PRISMATIC
                    && (target.getType().is(EntityTypeTags.UNDEAD)
                    || targetData.getLong(SpellLingeringZoneManager.TAG_BLIGHT_CHAIN_MARK_UNTIL).orElse(0L) > now)) {
                damage *= 1.50F; // Sec 6.53 Prismatic anti-corruption smite!
            } else if (effectMateria == EssenceType.EMPYREAN) {
                if (isUndeadOrAbyssal) {
                    damage *= 1.60F; // Sec 6.54 Empyrean: +60% vs Undead/Dark!
                }
                if (caster != null) {
                    SpellLingeringZoneManager.grantPartyAbsorption(level, caster, damage * 0.35F, 36.0F, 12.0D);
                }
            } else if (effectMateria == EssenceType.APOTHEOSIS && isUndeadOrAbyssal) {
                damage *= 2.00F; // Sec 6.58 Apotheosis: 2.0x vs Undead/Demonic/Void!
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F; // +25% damage vulnerability puddle (#45 Miasma & #46 Genesis)!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)) {
            if (MateriaFamilyTier.isVoidLineage(effectMateria)) {
                damage *= 0.50F;
            } else if (isAirLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (SpellLingeringZoneManager.isEntityWetOrSoaked(target) && isLightningLine(effectMateria)) {
            damage *= 2.0F;
        }
        // Earth Sundering Hex (#4): 2.0x incoming Earth spell damage!
        if (hasActiveMutatedHex(targetData, now, EssenceType.EARTH) && MateriaFamilyTier.isEarthLineage(effectMateria)) {
            damage *= 2.0F;
        }
        // Void Spacetime Anchoring Curse (#10): +50% incoming Void spell damage!
        if (hasActiveMutatedHex(targetData, now, EssenceType.VOID) && MateriaFamilyTier.isVoidLineage(effectMateria)) {
            damage *= 1.50F;
        }
        // Vapor Scalding Dissolution Hex (#29): +50% incoming electrical shock / Lightning-lineage spell damage!
        if (hasActiveMutatedHex(targetData, now, EssenceType.VAPOR) && isLightningLine(effectMateria)) {
            damage *= (1.0F + 0.50F * getHexStackMultiplier(target));
        }
        // Abyss Crushing Benthic Drowning Hex (#33): 2.0x incoming Lightning-lineage spell damage!
        if (hasActiveMutatedHex(targetData, now, EssenceType.ABYSS) && isLightningLine(effectMateria)) {
            damage *= 2.0F;
        }

        // 3. Target Materia Armor Resistance (ModDataComponents.ESSENCE_STATS)
        float materiaResist = getTargetMateriaResistance(target, effectMateria);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        // 4. Existing Broken Armor Shred bonus (+4% incoming damage per shred point, max 8)
        if (now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)) {
            int shredPts = targetData.getInt(TAG_ARMOR_SHRED).orElse(0);
            if (shredPts > 0) {
                damage *= (1.0F + 0.04F * Math.min(8, shredPts));
            }
        }

        // 5. Resonite Critical Strike Roll (+75% damage)
        float critChance = spell.getCriticalStrikeChance();
        if (critChance > 0.0F && level.random.nextFloat() < critChance) {
            damage *= 1.75F;
            level.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    target.getX(), target.getY(0.6), target.getZ(),
                    14, 0.35, 0.35, 0.35, 0.25);
            level.playSound(null, target.blockPosition(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.1F, 1.55F);
        }

        // 6. Progressive Armor Bypass & Armor Shred from Materia Family Tier + Form-Specific Bypass
        MateriaFamilyTier familyTier = MateriaFamilyTier.classify(effectMateria);
        float formArmorBypass = 0.0F;
        if (!spell.subSpells().isEmpty()) {
            SpellMutationProfile formMut = spell.subSpells().getFirst().formPart().resolveMutation(spell.materiaSpecific(), effectMateria);
            if (formMut.mutated()) {
                formArmorBypass = formMut.armorBypassFraction();
            }
        }
        float armorBypass = Math.max(
                effectMutation.mutated() ? Math.max(familyTier.getArmorBypassFraction(), effectMutation.armorBypassFraction()) : 0.0F,
                formArmorBypass
        );
        if (effectMutation.mutated() && familyTier.getBonusArmorShredPoints() > 0) {
            int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                    ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
            int nextShred = Math.min(8, Math.max(currentShred, familyTier.getBonusArmorShredPoints()));
            targetData.putInt(TAG_ARMOR_SHRED, nextShred);
            targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        }

        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();

        // Section 5.1 Armor Threshold Penetration:
        // Tiers 1-3 are partially mitigated by physical armor; Tier 4+ (Coagulata+) ignores 100% of vanilla physical armor!
        if (tier <= 3 && target.getArmorValue() > 0) {
            float effectiveArmor = target.getArmorValue() * (1.0F - armorBypass);
            damage *= Math.clamp(1.0F - (effectiveArmor * 0.032F), 0.25F, 1.0F);
        }

        float preHitHealth = target.getHealth();
        boolean hurt = target.hurtServer(level, magicSrc, Math.max(0.5F, damage));
        if (!hurt) {
            return 0.0F;
        }

        // Section 5.1 Overkill Conversion (Essence Rupture): excess lethal Materia damage ruptures into bonus Essence droplets!
        if ((!target.isAlive() || target.getHealth() <= 0.0F)
                && !(target instanceof Player)
                && !(target instanceof SpellEidolonEntity)) {
            float overkill = damage - preHitHealth;
            if (overkill >= 4.0F) {
                tryTriggerOverkillEssenceRupture(level, target, effectMateria, tier, overkill);
            }
        }

        // Update regen-lock ceiling downward when damage is dealt
        if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
            float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
            targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
        }

        // Elemental Hex Counter-Cleansing (Section 5.6)
        if (effectMateria != null && effectMateria != EssenceType.REGULAR) {
            checkElementalHexCounterCleanse(target, effectMateria);
        }

        // 7. Mutation Lifesteal & Status Side-Effects
        if (effectMutation.mutated()) {
            if (effectMutation.lifestealFraction() > 0.0F && caster != null && caster.isAlive()) {
                float steal = damage * effectMutation.lifestealFraction();
                notifyMagicalHeal(caster, steal);
                caster.heal(steal);
            }
            applyMutatedElementalAftereffects(level, target, effectMateria, tier);
        }

        // 8. Opposing Elemental Clashes & Harmonic Mergers
        if (effectMateria != null && effectMateria != EssenceType.REGULAR) {
            evaluateElementalReaction(level, caster, target, effectMateria, damage, spell.hasWardFriendlyFireSafeguard());
        }

        return damage;
    }

    private static void tryTriggerOverkillEssenceRupture(
            ServerLevel level,
            LivingEntity victim,
            @Nullable EssenceType spellMateria,
            int tier,
            float overkillDamage
    ) {
        float maxHp = Math.max(10.0F, victim.getMaxHealth());
        float chance = Math.clamp(0.18F + (overkillDamage / maxHp) * 0.40F, 0.18F, 0.85F);
        if (level.random.nextFloat() > chance) {
            return;
        }

        EssenceType dropType = (spellMateria != null && spellMateria != EssenceType.REGULAR)
                ? spellMateria
                : resolveEntityAffinities(victim).stream().findFirst().orElse(null);
        if (dropType == null || dropType == EssenceType.REGULAR) {
            return;
        }

        Item essenceTierItem = ModItems.WEAK_ESSENCE.get();
        if (tier >= 7 || overkillDamage >= 120.0F) {
            essenceTierItem = ModItems.STRONG_ESSENCE.get();
        } else if (tier >= 4 || overkillDamage >= 35.0F) {
            essenceTierItem = ModItems.AVERAGE_ESSENCE.get();
        }

        ItemStack dropStack = new ItemStack(essenceTierItem, 1);
        EssenceItem.setEssenceType(dropStack, dropType);
        ItemEntity drop = new ItemEntity(level, victim.getX(), victim.getY() + 0.6D, victim.getZ(), dropStack);
        drop.setNoGravity(true);
        drop.setInvulnerable(true);
        drop.setGlowingTag(true);
        level.addFreshEntity(drop);
        level.sendParticles(ParticleTypes.END_ROD, victim.getX(), victim.getY(0.6D), victim.getZ(),
                12, 0.3D, 0.3D, 0.3D, 0.08D);
    }

    private static void applyMutatedElementalAftereffects(ServerLevel level, LivingEntity target, EssenceType type, int tier) {
        if (MateriaFamilyTier.isNetherLineage(type)) {
            target.igniteForSeconds(2 + tier);
        } else if (MateriaFamilyTier.isFrozenLineage(type)) {
            int freezeGain = (45 + tier * 10) * (SpellLingeringZoneManager.isEntityWetOrSoaked(target) ? 3 : 1);
            target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 120, target.getTicksFrozen() + freezeGain));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40 + tier * 10, Math.min(3, tier / 3)));
        } else if (type == EssenceType.BLIGHT || type == EssenceType.MIASMA || type == EssenceType.SPORE) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 50 + tier * 10, Math.min(2, tier / 4)));
        } else if (type == EssenceType.UMBRAL || type == EssenceType.PENUMBRA || type == EssenceType.OBLIVION) {
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
        } else if (type == EssenceType.RADIANT || type == EssenceType.DAWN || type == EssenceType.CELESTIAL) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
        }
    }

    /**
     * Checks whether the target carries a recent active elemental imprint and triggers
     * Opposing Clashes or Harmonic Mergers (Section 2.1.2). Works across multi-Materia Dual-Verb casts!
     */
    public static void evaluateElementalReaction(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            EssenceType incoming,
            float triggerDamage,
            boolean wardSafeguard
    ) {
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        String prevStr = data.getString(TAG_ACTIVE_ELEMENT).orElse("");
        long until = data.getLong(TAG_ACTIVE_ELEMENT_UNTIL).orElse(0L);

        EssenceType previous = null;
        if (!prevStr.isEmpty() && now <= until) {
            try {
                previous = EssenceType.valueOf(prevStr);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (previous == null || previous == incoming) {
            data.putString(TAG_ACTIVE_ELEMENT, incoming.name());
            data.putLong(TAG_ACTIVE_ELEMENT_UNTIL, now + 80L);
            return;
        }

        boolean triggered = true;
        DamageSource reactionSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();

        // 1. Thermal Shock (Fire/Nether + Ice/Frozen)
        if ((MateriaFamilyTier.isNetherLineage(previous) && MateriaFamilyTier.isFrozenLineage(incoming))
                || (MateriaFamilyTier.isFrozenLineage(previous) && MateriaFamilyTier.isNetherLineage(incoming))) {
            target.invulnerableTime = 0;
            target.hurtServer(level, reactionSrc, Math.max(2.0F, triggerDamage * 0.50F));
            target.clearFire();
            target.setTicksFrozen(0);
            level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(0.5), target.getZ(), 18, 0.4, 0.4, 0.4, 0.08);
            level.playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.85F);
        }
        // 2. Annihilation Burst (Radiant + Umbral)
        else if ((isRadiantLine(previous) && isUmbralLine(incoming))
                || (isUmbralLine(previous) && isRadiantLine(incoming))) {
            level.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY(0.6), target.getZ(), 1, 0, 0, 0, 0);
            level.playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9F, 1.35F);
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5D));
            for (LivingEntity e : nearby) {
                if (!isProtectedFromOffensiveSpell(e, caster, wardSafeguard)) {
                    e.invulnerableTime = 0;
                    e.hurtServer(level, reactionSrc, Math.max(2.0F, triggerDamage * 0.45F));
                }
            }
        }
        // 3. Atmospheric Eruption (Air + Earth)
        else if ((isAirLine(previous) && MateriaFamilyTier.isEarthLineage(incoming))
                || (MateriaFamilyTier.isEarthLineage(previous) && isAirLine(incoming))) {
            Vec3 vel = target.getDeltaMovement();
            target.setDeltaMovement(vel.x, Math.max(0.72D, vel.y + 0.65D), vel.z);
            target.hasImpulse = true;
            level.sendParticles(ParticleTypes.GUST, target.getX(), target.getY(0.2), target.getZ(), 4, 0.3, 0.1, 0.3, 0.05);
        }
        // 4. Conductive Overcharge (Water/Vapor + Lightning/Static/Storm)
        else if ((isWaterLine(previous) && isLightningLine(incoming))
                || (isLightningLine(previous) && isWaterLine(incoming))) {
            level.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.65F, 1.6F);
            List<LivingEntity> chainTargets = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(5.0D));
            int arcs = 0;
            for (LivingEntity other : chainTargets) {
                if (other == target || !other.isAlive() || isProtectedFromOffensiveSpell(other, caster, wardSafeguard)) continue;
                other.invulnerableTime = 0;
                other.hurtServer(level, reactionSrc, Math.max(1.5F, triggerDamage * 0.60F));
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, other.getX(), other.getY(0.6), other.getZ(), 12, 0.25, 0.35, 0.25, 0.1);
                if (++arcs >= 3) break;
            }
        }
        // 5. Evaporative Scald (Water + Nether)
        else if ((isWaterLine(previous) && MateriaFamilyTier.isNetherLineage(incoming))
                || (MateriaFamilyTier.isNetherLineage(previous) && isWaterLine(incoming))) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, target.getX(), target.getY(0.6), target.getZ(), 14, 0.35, 0.35, 0.35, 0.03);
        }
        // 6. Molten Slag (Earth + Nether)
        else if ((MateriaFamilyTier.isEarthLineage(previous) && MateriaFamilyTier.isNetherLineage(incoming))
                || (MateriaFamilyTier.isNetherLineage(previous) && MateriaFamilyTier.isEarthLineage(incoming))) {
            target.igniteForSeconds(5);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            applyTimedAttributeShred(target, level, 2.0D, 0.0D, 160L);
            level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY(0.4), target.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
        }
        // 7. Entangling Miasma (Nature/Spore + Undead/Blight)
        else if ((isNatureLine(previous) && isNecroticLine(incoming))
                || (isNecroticLine(previous) && isNatureLine(incoming))) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY(0.5), target.getZ(), 16, 0.4, 0.4, 0.4, 0.05);
        }
        // 8. Gravitational Collapse (Void + Lightning/Astral)
        else if ((MateriaFamilyTier.isVoidLineage(previous) && isLightningLine(incoming))
                || (isLightningLine(previous) && MateriaFamilyTier.isVoidLineage(incoming))) {
            List<LivingEntity> pulled = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.5D));
            for (LivingEntity e : pulled) {
                if (e == target || isProtectedFromOffensiveSpell(e, caster, wardSafeguard)) continue;
                Vec3 inward = target.position().subtract(e.position()).normalize().scale(0.55D);
                e.setDeltaMovement(e.getDeltaMovement().add(inward.x, 0.15D, inward.z));
                e.hasImpulse = true;
            }
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.5), target.getZ(), 20, 0.4, 0.4, 0.4, 0.08);
        } else {
            triggered = false;
        }

        if (triggered) {
            data.remove(TAG_ACTIVE_ELEMENT);
            data.remove(TAG_ACTIVE_ELEMENT_UNTIL);
        } else {
            data.putString(TAG_ACTIVE_ELEMENT, incoming.name());
            data.putLong(TAG_ACTIVE_ELEMENT_UNTIL, now + 80L);
        }
    }

    // --- Persistent Indefinite Elemental Hexes (Anti-Milk, Level I–III Stacking, Counter-Cleanse, Boss Cap) ---

    public static boolean applyPersistentHex(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            EssenceType hexMateria,
            int potencyTier,
            int requestedTicks,
            boolean hasWardSafeguard
    ) {
        return applyPersistentHex(level, caster, target, hexMateria, potencyTier, requestedTicks, hasWardSafeguard, true);
    }

    public static boolean applyPersistentHex(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            EssenceType hexMateria,
            int potencyTier,
            int requestedTicks,
            boolean hasWardSafeguard,
            boolean mutated
    ) {
        if (isProtectedFromOffensiveSpell(target, caster, hasWardSafeguard)) {
            return false;
        }

        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();

        boolean isBoss = isBossEntity(level, target);
        if (isBoss && now < data.getLong(TAG_HEX_BOSS_IMMUNE_UNTIL).orElse(0L)) {
            return false;
        }

        EssenceType resolved = hexMateria == null ? EssenceType.UMBRAL : hexMateria;

        // Hex Stacking (Section 5.6): re-casting the same Hex Materia stacks intensity up to Level III (max 3)
        int stack = 1;
        long existingUntil = data.getLong(TAG_HEX_UNTIL).orElse(0L);
        if (now <= existingUntil && resolved.name().equals(data.getString(TAG_HEX_TYPE).orElse(""))) {
            stack = Math.min(3, data.getInt(TAG_HEX_STACK).orElse(1) + 1);
        }

        long durationTicks;
        if (isBoss) {
            // Section 2.5.1 / 5.6 Boss Tenacity Cap: 15s (300t) on Dragon/Wither, 30s (600t) on other bosses + 10s (200t) Hex Immunity
            long bossCap = (target instanceof EnderDragon || target instanceof WitherBoss) ? 300L : 600L;
            durationTicks = Math.min(bossCap, Math.max(100L, requestedTicks));
            data.putLong(TAG_HEX_BOSS_IMMUNE_UNTIL, now + durationTicks + 200L);
        } else {
            // Section 5.6 Indefinite Persistence on standard mobs and players until purified, counter-cleansed, or caster death!
            durationTicks = INDEFINITE_HEX_TICKS;
        }

        data.putString(TAG_HEX_TYPE, resolved.name());
        data.putInt(TAG_HEX_TIER, Math.clamp(potencyTier, 1, 10));
        data.putInt(TAG_HEX_STACK, stack);
        data.putBoolean(TAG_HEX_MUTATED, mutated);
        data.putLong(TAG_HEX_UNTIL, now + durationTicks);
        data.putLong(TAG_HEX_INITIAL_WINDOW, now + 200L);
        if (caster != null) {
            data.putString(TAG_HEX_CASTER_UUID, caster.getUUID().toString());
        } else {
            data.remove(TAG_HEX_CASTER_UUID);
        }

        if (mutated && (resolved == EssenceType.MIASMA
                || resolved == EssenceType.GENESIS
                || resolved == EssenceType.OBLIVION
                || resolved == EssenceType.CHIMERA
                || resolved == EssenceType.ENTROPIC)) {
            double maxHpCut = resolved == EssenceType.ENTROPIC ? -0.30D : -0.25D;
            AttributeInstance maxHp = target.getAttribute(Attributes.MAX_HEALTH);
            if (maxHp != null) {
                maxHp.removeModifier(MOD_HEX_MAX_HEALTH_CUT_PCT);
                maxHp.addTransientModifier(new AttributeModifier(
                        MOD_HEX_MAX_HEALTH_CUT_PCT,
                        maxHpCut,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
                if (target.getHealth() > target.getMaxHealth()) {
                    target.setHealth(target.getMaxHealth());
                }
            }
        }

        if (mutated && isRegenLockingHex(resolved)) {
            float prevCeil = data.contains(TAG_HEX_MAX_ALLOWED_HP)
                    ? data.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getHealth())
                    : target.getHealth();
            data.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
        } else {
            data.remove(TAG_HEX_MAX_ALLOWED_HP);
        }

        if (mutated && (resolved == EssenceType.VOID || resolved == EssenceType.ASTRAL || resolved == EssenceType.ESCHATON)) {
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_X, target.getX());
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Y, target.getY());
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Z, target.getZ());
        }

        if (mutated && MateriaFamilyTier.isMonsterEmpowermentHex(resolved) && !(target instanceof Player) && !isBoss) {
            target.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, stack - 1));
            target.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 600, stack - 1));
            data.putBoolean("SpellHexBonusLoot", true);
        }

        return true;
    }

    public static int getHexStackLevel(LivingEntity target) {
        CompoundTag data = EntityHelper.getPersistentData(target);
        if (target.level().getGameTime() > data.getLong(TAG_HEX_UNTIL).orElse(0L)) {
            return 0;
        }
        return Math.clamp(data.getInt(TAG_HEX_STACK).orElse(1), 1, 3);
    }

    public static float getHexStackMultiplier(LivingEntity target) {
        int stack = getHexStackLevel(target);
        if (stack <= 1) return 1.0F;
        return 1.0F + 0.35F * (stack - 1); // I = 1.0x, II = 1.35x, III = 1.70x
    }

    public static boolean isBlinkAnchored(LivingEntity target, long now) {
        CompoundTag data = EntityHelper.getPersistentData(target);
        return hasActiveMutatedHex(data, now, EssenceType.VOID)
                || hasActiveMutatedHex(data, now, EssenceType.ASTRAL)
                || hasActiveMutatedHex(data, now, EssenceType.ESCHATON);
    }

    public static boolean hasActiveMutatedHexPublic(LivingEntity target, EssenceType expected) {
        return hasActiveMutatedHex(EntityHelper.getPersistentData(target), target.level().getGameTime(), expected);
    }

    private static boolean hasActiveMutatedHex(CompoundTag data, long now, EssenceType expected) {
        return now <= data.getLong(TAG_HEX_UNTIL).orElse(0L)
                && data.getBoolean(TAG_HEX_MUTATED).orElse(false)
                && expected.name().equals(data.getString(TAG_HEX_TYPE).orElse(""));
    }

    @Nullable
    public static LivingEntity resolveHexCaster(ServerLevel level, LivingEntity target) {
        String idStr = EntityHelper.getPersistentData(target).getString(TAG_HEX_CASTER_UUID).orElse("");
        if (idStr.isEmpty()) return null;
        try {
            Entity e = level.getEntity(java.util.UUID.fromString(idStr));
            return e instanceof LivingEntity l ? l : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static boolean isRegenLockingHex(EssenceType type) {
        return type == EssenceType.NETHER
                || type == EssenceType.RADIANT
                || type == EssenceType.UNDEAD
                || type == EssenceType.ARID
                || type == EssenceType.MAGMA
                || type == EssenceType.VITAE
                || type == EssenceType.BLOOD
                || type == EssenceType.ECLIPSE
                || type == EssenceType.BLIGHT
                || type == EssenceType.NULL_U
                || type == EssenceType.WRAITH
                || type == EssenceType.PYRE
                || type == EssenceType.PENUMBRA
                || type == EssenceType.MIASMA
                || type == EssenceType.GENESIS
                || type == EssenceType.OBLIVION
                || type == EssenceType.CHIMERA
                || type == EssenceType.ENTROPIC
                || type == EssenceType.CELESTIAL
                || type == EssenceType.EMPYREAN
                || type == EssenceType.APOTHEOSIS
                || type == EssenceType.ENTROPICA;
    }

    /**
     * Raises the natural-regeneration lock ceiling when an active magical heal ({@code RESTORATION}, lifesteal, etc.) is applied,
     * UNLESS the victim suffers from a conventional-heal-blocking curse which requires full curse purification first!
     */
    public static void notifyMagicalHeal(LivingEntity target, float healAmount) {
        if (healAmount <= 0.0F) return;
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = target.level().getGameTime();
        if (hasActiveMutatedHex(data, now, EssenceType.GENESIS)
                || hasActiveMutatedHex(data, now, EssenceType.OBLIVION)
                || hasActiveMutatedHex(data, now, EssenceType.CHIMERA)
                || hasActiveMutatedHex(data, now, EssenceType.ENTROPIC)
                || hasActiveMutatedHex(data, now, EssenceType.CELESTIAL)
                || hasActiveMutatedHex(data, now, EssenceType.EMPYREAN)
                || hasActiveMutatedHex(data, now, EssenceType.APOTHEOSIS)
                || hasActiveMutatedHex(data, now, EssenceType.ENTROPICA)) {
            return;
        }
        if (data.contains(TAG_HEX_MAX_ALLOWED_HP)) {
            float curr = data.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getHealth());
            data.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(target.getMaxHealth(), curr + healAmount));
        }
    }

    /**
     * Section 5.6 Elemental Counter-Cleansing: opposing Materia washes away active Elemental Hexes!
     */
    public static boolean checkElementalHexCounterCleanse(LivingEntity target, EssenceType incomingElement) {
        if (incomingElement == null || incomingElement == EssenceType.REGULAR) return false;
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = target.level().getGameTime();
        if (now > data.getLong(TAG_HEX_UNTIL).orElse(0L)) return false;

        String hexStr = data.getString(TAG_HEX_TYPE).orElse("");
        if (hexStr.isEmpty()) return false;

        boolean counterMatched = switch (hexStr) {
            case "NETHER", "ARID", "MAGMA", "OASIS", "SOULFIRE", "PYRE" -> isWaterLine(incomingElement) || MateriaFamilyTier.isFrozenLineage(incomingElement);
            case "FROZEN", "GLACIAL", "NATURE", "OVERGROWTH", "SPORE", "TAIGA", "AMBER", "AURORA", "RIME", "SYLVAN", "TERMINUS" -> MateriaFamilyTier.isNetherLineage(incomingElement);
            case "VAPOR" -> MateriaFamilyTier.isFrozenLineage(incomingElement);
            case "DUST" -> isWaterLine(incomingElement) || isAirLine(incomingElement);
            case "UMBRAL", "UNDEAD", "VITAE", "BLOOD", "ECLIPSE", "BLIGHT", "NULL_U", "ABYSS", "WRAITH", "BARROW", "PENUMBRA", "MIASMA" -> isRadiantLine(incomingElement);
            case "OBLIVION" -> isRadiantLine(incomingElement) || incomingElement == EssenceType.GENESIS;
            case "AETHER" -> incomingElement == EssenceType.VITAE || incomingElement == EssenceType.GENESIS || incomingElement == EssenceType.SPRING
                    || MateriaFamilyTier.isVoidLineage(incomingElement) || isUmbralLine(incomingElement);
            case "ENTROPIC" -> incomingElement == EssenceType.VITAE || incomingElement == EssenceType.CELESTIAL || isRadiantLine(incomingElement);
            case "CATACLYSM" -> incomingElement == EssenceType.VITAE || incomingElement == EssenceType.GENESIS || isRadiantLine(incomingElement);
            case "RADIANT", "DAWN", "AEGIS", "MIRAGE", "SPRING", "GENESIS", "CELESTIAL", "PRISMATIC", "EMPYREAN" ->
                    isUmbralLine(incomingElement) || incomingElement == EssenceType.BLIGHT || MateriaFamilyTier.isVoidLineage(incomingElement);
            case "ESCHATON" -> incomingElement == EssenceType.APOTHEOSIS || incomingElement == EssenceType.ENTROPICA;
            case "APOTHEOSIS" -> incomingElement == EssenceType.ESCHATON || incomingElement == EssenceType.VOID || isUmbralLine(incomingElement);
            case "ENTROPICA" -> false; // Sec 6.59: immune to elemental counter-weaknesses; cleansed ONLY by Restoration!
            case "LIGHTNING", "STORM", "AURA", "ASTRAL", "NULL_R", "STATIC" -> MateriaFamilyTier.isEarthLineage(incomingElement) || isNatureLine(incomingElement);
            default -> false;
        };

        if (counterMatched) {
            clearHexData(target, data);
            if (target.level() instanceof ServerLevel srv) {
                srv.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY(0.8D), target.getZ(),
                        8, 0.3D, 0.35D, 0.3D, 0.04D);
            }
            return true;
        }
        return false;
    }

    private static void clearHexData(LivingEntity target, CompoundTag data) {
        data.remove(TAG_HEX_TYPE);
        data.remove(TAG_HEX_TIER);
        data.remove(TAG_HEX_STACK);
        data.remove(TAG_HEX_MUTATED);
        data.remove(TAG_HEX_UNTIL);
        data.remove(TAG_HEX_INITIAL_WINDOW);
        data.remove(TAG_HEX_CASTER_UUID);
        data.remove(TAG_HEX_MAX_ALLOWED_HP);
        data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
        AttributeInstance maxHp = target.getAttribute(Attributes.MAX_HEALTH);
        if (maxHp != null) {
            maxHp.removeModifier(MOD_HEX_MAX_HEALTH_CUT_PCT);
        }
        clearShredModifiers(target, data);
        clearOffenseShredModifiers(target, data);
    }

    public static int purifyHexesAndNegativeEffects(LivingEntity target) {
        int cleansed = 0;
        CompoundTag data = EntityHelper.getPersistentData(target);
        if (!data.getString(TAG_HEX_TYPE).orElse("").isEmpty()) {
            clearHexData(target, data);
            cleansed++;
        } else {
            clearShredModifiers(target, data);
            clearOffenseShredModifiers(target, data);
            data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
        }
        List<MobEffectInstance> active = new ArrayList<>(target.getActiveEffects());
        for (MobEffectInstance inst : active) {
            if (!inst.getEffect().value().isBeneficial()) {
                target.removeEffect(inst.getEffect());
                cleansed++;
            }
        }
        target.clearFire();
        target.setTicksFrozen(0);
        return cleansed;
    }

    /**
     * Ticks active Persistent Elemental Hexes, natural health-regen locks, player spawn-lure beacons,
     * and transient attribute modifier expirations across loaded entities in the level.
     */
    public static void tickServerLevelHexes(ServerLevel level) {
        long now = level.getGameTime();
        if (now % 5L != 0L) return;

        boolean fullHexPulse = (now % 20L == 0L);

        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            CompoundTag data = EntityHelper.getPersistentData(living);

            // 1. Expire timed attribute shreds / offense debuffs / defense buffs cleanly
            long shredUntil = data.getLong(TAG_ATTR_SHRED_UNTIL).orElse(0L);
            if (shredUntil > 0L && now > shredUntil) {
                clearShredModifiers(living, data);
            }
            long offenseShredUntil = data.getLong(TAG_ATTR_OFFENSE_SHRED_UNTIL).orElse(0L);
            if (offenseShredUntil > 0L && now > offenseShredUntil) {
                clearOffenseShredModifiers(living, data);
            }
            long buffUntil = data.getLong(TAG_ATTR_DEF_BUFF_UNTIL).orElse(0L);
            if (buffUntil > 0L && now > buffUntil) {
                clearDefenseBuffModifiers(living, data);
            }

            long until = data.getLong(TAG_HEX_UNTIL).orElse(0L);
            if (until <= 0L) continue;

            if (now > until) {
                clearHexData(living, data);
                continue;
            }

            String typeStr = data.getString(TAG_HEX_TYPE).orElse("");
            int tier = data.getInt(TAG_HEX_TIER).orElse(1);
            boolean hexMutated = data.getBoolean(TAG_HEX_MUTATED).orElse(true);
            EssenceType hexType = EssenceType.UMBRAL;
            try {
                if (!typeStr.isEmpty()) hexType = EssenceType.valueOf(typeStr);
            } catch (IllegalArgumentException ignored) {
            }

            // 2. Enforce Natural Health Regeneration Lock every 5 ticks (Nether, Radiant, Undead, Arid)
            if (hexMutated && isRegenLockingHex(hexType) && data.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float allowed = data.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(living.getMaxHealth());
                if (living.getHealth() > allowed + 0.05F) {
                    living.setHealth(Math.max(0.5F, allowed));
                } else if (living.getHealth() < allowed) {
                    data.putFloat(TAG_HEX_MAX_ALLOWED_HP, living.getHealth());
                }
            }

            if (!fullHexPulse) continue;

            // 3. Player Spawn-Lure Beacons (Section 5.6: Undead lures undead hordes; Void lures Endermen / rift Endermites)
            if (hexMutated && living instanceof Player hexedPlayer && !hexedPlayer.isCreative() && !hexedPlayer.isSpectator() && now % 40L == 0L) {
                if (hexType == EssenceType.UNDEAD) {
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(28.0D))) {
                        if (mob.isAlive() && mob.getType().is(EntityTypeTags.UNDEAD) && mob.getTarget() == null) {
                            mob.setTarget(hexedPlayer);
                        }
                    }
                } else if (hexType == EssenceType.VOID) {
                    int nearbyMites = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(28.0D))) {
                        if (!mob.isAlive()) continue;
                        if (mob instanceof Endermite) nearbyMites++;
                        if ((mob instanceof EnderMan || mob instanceof Endermite) && mob.getTarget() == null) {
                            mob.setTarget(hexedPlayer);
                        }
                    }
                    if (nearbyMites < 2 && now % 200L == 0L && level.random.nextFloat() < 0.25F) {
                        Endermite mite = EntityType.ENDERMITE.create(level, EntitySpawnReason.MOB_SUMMONED);
                        if (mite != null) {
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            mite.setPos(hexedPlayer.getX() + Math.cos(ang) * 4.5D, hexedPlayer.getY(), hexedPlayer.getZ() + Math.sin(ang) * 4.5D);
                            mite.setTarget(hexedPlayer);
                            level.addFreshEntity(mite);
                            level.sendParticles(ParticleTypes.REVERSE_PORTAL, mite.getX(), mite.getY(0.4D), mite.getZ(),
                                    10, 0.2D, 0.2D, 0.2D, 0.05D);
                        }
                    }
                } else if (hexType == EssenceType.BLOOD) {
                    // Section 6.23 Blood Scent of Hemorrhage: lures nocturnal/predatory monsters within 32m!
                    int nearbyPredators = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(32.0D))) {
                        if (!mob.isAlive()) continue;
                        if (mob instanceof Monster) {
                            nearbyPredators++;
                            if (mob.getTarget() == null) {
                                mob.setTarget(hexedPlayer);
                            }
                        }
                    }
                    if (nearbyPredators < 2 && now % 240L == 0L && level.random.nextFloat() < 0.25F) {
                        CaveSpider hunter = EntityType.CAVE_SPIDER.create(level, EntitySpawnReason.MOB_SUMMONED);
                        if (hunter != null) {
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            hunter.setPos(hexedPlayer.getX() + Math.cos(ang) * 5.5D, hexedPlayer.getY(), hexedPlayer.getZ() + Math.sin(ang) * 5.5D);
                            hunter.setTarget(hexedPlayer);
                            level.addFreshEntity(hunter);
                            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, hunter.getX(), hunter.getY(0.4D), hunter.getZ(),
                                    6, 0.2D, 0.2D, 0.2D, 0.04D);
                        }
                    }
                } else if (hexType == EssenceType.ASTRAL) {
                    // Section 6.28 Cosmic Meteoric Attraction: lures Phantoms & Endermen and periodically calls down an astral micro-meteor!
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(28.0D))) {
                        if (!mob.isAlive()) continue;
                        if ((mob instanceof Phantom || mob instanceof EnderMan) && mob.getTarget() == null) {
                            mob.setTarget(hexedPlayer);
                        }
                    }
                    if (now % 120L == 0L) {
                        double ang = level.random.nextDouble() * Math.PI * 2.0D;
                        double dist = 1.5D + level.random.nextDouble() * 3.0D;
                        double mx = hexedPlayer.getX() + Math.cos(ang) * dist;
                        double my = hexedPlayer.getY();
                        double mz = hexedPlayer.getZ() + Math.sin(ang) * dist;
                        level.sendParticles(ParticleTypes.FIREWORK, mx, my + 4.0D, mz, 14, 0.25D, 1.8D, 0.25D, 0.08D);
                        level.sendParticles(ParticleTypes.END_ROD, mx, my + 0.3D, mz, 12, 0.5D, 0.2D, 0.5D, 0.06D);
                        level.playSound(null, hexedPlayer.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 0.85F, 0.75F);
                        if (hexedPlayer.position().distanceToSqr(mx, my, mz) <= 6.25D) {
                            hexedPlayer.hurtServer(level, level.damageSources().magic(), 2.0F * getHexStackMultiplier(hexedPlayer));
                        }
                    }
                } else if (hexType == EssenceType.NULL_R) {
                    // Section 6.31 Antimatter Beacon: spatial white-hole flare reveals player with Glowing and lures Void entities!
                    hexedPlayer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0, true, true));
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(28.0D))) {
                        if (!mob.isAlive()) continue;
                        if ((mob instanceof EnderMan || mob instanceof Endermite || mob instanceof Monster) && mob.getTarget() == null) {
                            mob.setTarget(hexedPlayer);
                        }
                    }
                } else if (hexType == EssenceType.BARROW) {
                    // Section 6.39 Catacomb Awakening: awakens subterranean monsters & armored skeletons around the player!
                    int nearbySkel = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(28.0D))) {
                        if (!mob.isAlive()) continue;
                        if (mob instanceof Skeleton) nearbySkel++;
                        if (mob instanceof Monster && mob.getTarget() == null) {
                            mob.setTarget(hexedPlayer);
                        }
                    }
                    if (nearbySkel < 2 && now % 240L == 0L && level.random.nextFloat() < 0.30F) {
                        Skeleton skel = EntityType.SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
                        if (skel != null) {
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            skel.setPos(hexedPlayer.getX() + Math.cos(ang) * 5.5D, hexedPlayer.getY(), hexedPlayer.getZ() + Math.sin(ang) * 5.5D);
                            skel.setItemSlot(EquipmentSlot.HEAD, new ItemStack(net.minecraft.world.item.Items.CHAINMAIL_HELMET));
                            skel.setItemSlot(EquipmentSlot.CHEST, new ItemStack(net.minecraft.world.item.Items.CHAINMAIL_CHESTPLATE));
                            skel.setTarget(hexedPlayer);
                            level.addFreshEntity(skel);
                            level.sendParticles(ParticleTypes.ASH, skel.getX(), skel.getY(0.6D), skel.getZ(), 12, 0.25D, 0.4D, 0.25D, 0.03D);
                        }
                    }
                } else if (hexType == EssenceType.MIASMA) {
                    // Section 6.46 Pestilence Pheromone Trail: accelerates slime, witch, and swarming arthropod spawns (+120%)!
                    int nearbySwarm = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(28.0D))) {
                        if (!mob.isAlive()) continue;
                        if (mob instanceof CaveSpider || mob instanceof Slime || mob instanceof Witch) nearbySwarm++;
                        if (mob instanceof Monster && mob.getTarget() == null) {
                            mob.setTarget(hexedPlayer);
                        }
                    }
                    if (nearbySwarm < 2 && now % 200L == 0L && level.random.nextFloat() < 0.35F) {
                        CaveSpider pest = EntityType.CAVE_SPIDER.create(level, EntitySpawnReason.MOB_SUMMONED);
                        if (pest != null) {
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            pest.setPos(hexedPlayer.getX() + Math.cos(ang) * 5.0D, hexedPlayer.getY(), hexedPlayer.getZ() + Math.sin(ang) * 5.0D);
                            pest.setTarget(hexedPlayer);
                            level.addFreshEntity(pest);
                            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, pest.getX(), pest.getY(0.5D), pest.getZ(), 10, 0.25D, 0.25D, 0.25D, 0.03D);
                        }
                    }
                } else if (hexType == EssenceType.OBLIVION) {
                    // Section 6.48 Stygian Extinction Scent: draws nether beasts, wither skeletons, and nightmare predators!
                    int nearbyWither = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(28.0D))) {
                        if (!mob.isAlive()) continue;
                        if (mob instanceof WitherSkeleton) nearbyWither++;
                        if (mob instanceof Monster && mob.getTarget() == null) {
                            mob.setTarget(hexedPlayer);
                        }
                    }
                    if (nearbyWither < 2 && now % 240L == 0L && level.random.nextFloat() < 0.28F) {
                        WitherSkeleton wSkel = EntityType.WITHER_SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
                        if (wSkel != null) {
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            wSkel.setPos(hexedPlayer.getX() + Math.cos(ang) * 5.5D, hexedPlayer.getY(), hexedPlayer.getZ() + Math.sin(ang) * 5.5D);
                            wSkel.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.world.item.Items.STONE_SWORD));
                            wSkel.setTarget(hexedPlayer);
                            level.addFreshEntity(wSkel);
                            level.sendParticles(ParticleTypes.SMOKE, wSkel.getX(), wSkel.getY(0.7D), wSkel.getZ(), 14, 0.3D, 0.5D, 0.3D, 0.04D);
                        }
                    }
                } else if (hexType == EssenceType.ENTROPIC) {
                    // Section 6.51 Entropic Spawn-Lure (32m): lures Void horrors, Phantoms & Endermites!
                    int nearbyVoid = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(32.0D))) {
                        if (!mob.isAlive()) continue;
                        if (mob instanceof Endermite || mob instanceof Phantom || mob instanceof EnderMan) nearbyVoid++;
                        if (mob instanceof Monster && mob.getTarget() == null) mob.setTarget(hexedPlayer);
                    }
                    if (nearbyVoid < 2 && now % 200L == 0L && level.random.nextFloat() < 0.35F) {
                        Endermite mite = EntityType.ENDERMITE.create(level, EntitySpawnReason.MOB_SUMMONED);
                        if (mite != null) {
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            mite.setPos(hexedPlayer.getX() + Math.cos(ang) * 5.0D, hexedPlayer.getY(), hexedPlayer.getZ() + Math.sin(ang) * 5.0D);
                            mite.setTarget(hexedPlayer);
                            level.addFreshEntity(mite);
                        }
                    }
                } else if (hexType == EssenceType.EMPYREAN) {
                    // Section 6.54 Empyrean Radiant Beacon (32m, +120% hostile spawn lure):
                    hexedPlayer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0, true, true));
                    int nearbyHostiles = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(32.0D))) {
                        if (mob.isAlive() && mob instanceof Monster) {
                            nearbyHostiles++;
                            if (mob.getTarget() == null) mob.setTarget(hexedPlayer);
                        }
                    }
                    if (nearbyHostiles < 3 && now % 200L == 0L && level.random.nextFloat() < 0.35F) {
                        Zombie z = EntityType.ZOMBIE.create(level, EntitySpawnReason.MOB_SUMMONED);
                        if (z != null) {
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            z.setPos(hexedPlayer.getX() + Math.cos(ang) * 6.0D, hexedPlayer.getY(), hexedPlayer.getZ() + Math.sin(ang) * 6.0D);
                            z.setTarget(hexedPlayer);
                            level.addFreshEntity(z);
                        }
                    }
                } else if (hexType == EssenceType.CATACLYSM) {
                    // Section 6.55 Cataclysm Abyssal Beacon (32m, +150% nether/wither predator lure):
                    int nearbyNether = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(32.0D))) {
                        if (!mob.isAlive()) continue;
                        if (mob instanceof WitherSkeleton || mob instanceof MagmaCube || mob instanceof Blaze) nearbyNether++;
                        if (mob instanceof Monster && mob.getTarget() == null) mob.setTarget(hexedPlayer);
                    }
                    if (nearbyNether < 3 && now % 180L == 0L && level.random.nextFloat() < 0.40F) {
                        WitherSkeleton ws = EntityType.WITHER_SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
                        if (ws != null) {
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            ws.setPos(hexedPlayer.getX() + Math.cos(ang) * 6.0D, hexedPlayer.getY(), hexedPlayer.getZ() + Math.sin(ang) * 6.0D);
                            ws.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.world.item.Items.STONE_SWORD));
                            ws.setTarget(hexedPlayer);
                            level.addFreshEntity(ws);
                        }
                    }
                } else if (hexType == EssenceType.ESCHATON || hexType == EssenceType.ENTROPICA) {
                    // Sections 6.57 & 6.59 Omega / Cosmic Spawn-Lure (36m, +200% influx with immediate aggro!):
                    int nearbyHorrors = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(36.0D))) {
                        if (!mob.isAlive()) continue;
                        if (mob instanceof Endermite || mob instanceof EnderMan || mob instanceof WitherSkeleton) nearbyHorrors++;
                        if (mob instanceof Monster && mob.getTarget() == null) mob.setTarget(hexedPlayer);
                    }
                    if (nearbyHorrors < 3 && now % 160L == 0L && level.random.nextFloat() < 0.45F) {
                        Endermite horror = EntityType.ENDERMITE.create(level, EntitySpawnReason.MOB_SUMMONED);
                        if (horror != null) {
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            horror.setPos(hexedPlayer.getX() + Math.cos(ang) * 5.5D, hexedPlayer.getY(), hexedPlayer.getZ() + Math.sin(ang) * 5.5D);
                            horror.setTarget(hexedPlayer);
                            level.addFreshEntity(horror);
                            level.sendParticles(ParticleTypes.REVERSE_PORTAL, horror.getX(), horror.getY(0.5D), horror.getZ(), 14, 0.25D, 0.3D, 0.25D, 0.05D);
                        }
                    }
                }
            }

            MateriaPartMutationHandler hexHandler = SpellMutationHandlerRegistry.resolveActive(hexType, hexMutated);
            if (hexHandler != null && hexHandler.onPersistentHexTick(level, living, tier)) {
                continue;
            }

            float hexDot = (0.75F + (tier * 0.45F)) * getHexStackMultiplier(living);
            living.hurtServer(level, level.damageSources().magic(), hexDot);
            if (data.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                data.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(data.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(living.getHealth()), living.getHealth()));
            }
            level.sendParticles(ParticleTypes.WITCH, living.getX(), living.getY(0.7), living.getZ(),
                    5, 0.25, 0.35, 0.25, 0.02);

            if (hexMutated && MateriaFamilyTier.isFrozenLineage(hexType)) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1, true, false));
            } else if (hexMutated && (MateriaFamilyTier.isVoidLineage(hexType) || isUmbralLine(hexType))) {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 0, true, false));
            }
        }
    }

    /**
     * Handles both Caster-Death Hex Dispel (Section 5.6: if the caster dies, all hexes they inflicted dissolve)
     * and Contagious Hex Transmission (Undead Cadaverous Wither-Blight spreads on death at any time; other hexes spread in initial window).
     */
    public static void onLivingDeathHexContagion(LivingEntity deceased) {
        if (!(deceased.level() instanceof ServerLevel level)) return;

        // 1. Caster-Death Dispel: break all active hexes cast by this deceased entity!
        String deceasedUuidStr = deceased.getUUID().toString();
        for (Entity e : level.getAllEntities()) {
            if (!(e instanceof LivingEntity candidate) || candidate == deceased || !candidate.isAlive()) continue;
            CompoundTag cData = EntityHelper.getPersistentData(candidate);
            if (deceasedUuidStr.equals(cData.getString(TAG_HEX_CASTER_UUID).orElse(""))) {
                clearHexData(candidate, cData);
            }
        }

        // 2. Elemental Death Marks & Contagious Hex Transmission from a dying victim
        CompoundTag data = EntityHelper.getPersistentData(deceased);
        long now = level.getGameTime();

        // Spore (#18) Mycelial Infestation Strike: dying host bursts into infectious Poison/Nausea spore pop + mushrooms!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SPORE_INFESTED_UNTIL).orElse(0L)) {
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, deceased.getX(), deceased.getY(0.7D), deceased.getZ(),
                    30, 0.75D, 0.5D, 0.75D, 0.06D);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(4.0D))) {
                if (foe == deceased || !foe.isAlive() || !(foe instanceof Monster)) continue;
                foe.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
            }
            net.minecraft.core.BlockPos dp = deceased.blockPosition();
            if (level.getBlockState(dp).isAir() && level.getBlockState(dp.below()).isSolidRender()) {
                level.setBlockAndUpdate(dp, level.random.nextBoolean()
                        ? net.minecraft.world.level.block.Blocks.RED_MUSHROOM.defaultBlockState()
                        : net.minecraft.world.level.block.Blocks.BROWN_MUSHROOM.defaultBlockState());
            }
        }

        // Vitae (#21) Cellular Overcharge Bloom / Mine: dying host detonates into a 6m player/ally healing burst!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_VITAE_DEATH_BLOOM_UNTIL).orElse(0L)) {
            level.sendParticles(ParticleTypes.HEART, deceased.getX(), deceased.getY(0.8D), deceased.getZ(),
                    16, 0.7D, 0.5D, 0.7D, 0.05D);
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(6.0D))) {
                if (!ally.isAlive() || ally == deceased || ally instanceof Monster) continue;
                notifyMagicalHeal(ally, 6.0F);
                ally.heal(6.0F);
                ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 0));
            }
        }

        // Blood (#22) Vampiric Exsanguination Strike: lethal kill restores 2 hunger shanks (4 food) & Speed II 5s!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BLOOD_FEAST_UNTIL).orElse(0L)) {
            for (Player nearPlayer : level.getEntitiesOfClass(Player.class, deceased.getBoundingBox().inflate(8.0D))) {
                if (nearPlayer.isAlive()) {
                    nearPlayer.getFoodData().eat(4, 0.6F);
                    nearPlayer.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1));
                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, nearPlayer.getX(), nearPlayer.getY(1.0D), nearPlayer.getZ(),
                            6, 0.3D, 0.3D, 0.3D, 0.04D);
                }
            }
        }

        // Amber (#24) Fossilizing Stasis Strike (Fragile Effigy Statue that shatters into bonus amber shards!)
        // vs Prehistoric Amber Monsoon (Permanent Decorative Amber Monument + bonus Amber Chunk!)
        boolean fragileEffigy = now <= data.getLong(SpellLingeringZoneManager.TAG_AMBER_FRAGILE_STATUE_UNTIL).orElse(0L);
        boolean permanentMonument = now <= data.getLong(SpellLingeringZoneManager.TAG_AMBER_FOSSILIZE_UNTIL).orElse(0L);
        if ((fragileEffigy || permanentMonument) && !(deceased instanceof Player)) {
            if (fragileEffigy && !isBossEntity(level, deceased)) {
                net.minecraft.core.BlockPos sp = deceased.blockPosition().immutable();
                List<net.minecraft.core.BlockPos> effigyBlocks = new ArrayList<>(2);
                if (level.getBlockState(sp).isAir()) {
                    level.setBlockAndUpdate(sp, net.minecraft.world.level.block.Blocks.HONEY_BLOCK.defaultBlockState());
                    effigyBlocks.add(sp);
                }
                if (level.getBlockState(sp.above()).isAir()) {
                    level.setBlockAndUpdate(sp.above(), net.minecraft.world.level.block.Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
                    effigyBlocks.add(sp.above());
                }
                level.playSound(null, sp, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.PLAYERS, 1.0F, 1.15F);

                if (!effigyBlocks.isEmpty()) {
                    SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                            null,
                            Vec3.atCenterOf(sp),
                            1.8D,
                            100, // Shatters automatically after 5s OR immediately if struck/mined!
                            now,
                            null,
                            null,
                            EssenceType.AMBER,
                            1.0F,
                            (lvl, z, elapsed) -> {
                                boolean brokenEarly = false;
                                for (net.minecraft.core.BlockPos bp : effigyBlocks) {
                                    if (lvl.getBlockState(bp).isAir()) {
                                        brokenEarly = true;
                                        break;
                                    }
                                }
                                if (brokenEarly || elapsed >= z.maxDurationTicks) {
                                    for (net.minecraft.core.BlockPos bp : effigyBlocks) {
                                        if (lvl.getBlockState(bp).is(net.minecraft.world.level.block.Blocks.HONEY_BLOCK)
                                                || lvl.getBlockState(bp).is(net.minecraft.world.level.block.Blocks.ORANGE_STAINED_GLASS)) {
                                            lvl.removeBlock(bp, false);
                                        }
                                    }
                                    ItemEntity shards = new ItemEntity(lvl, z.center.x, z.center.y + 0.5D, z.center.z,
                                            new ItemStack(ModItems.AMBER_CHUNK.get(), 2 + lvl.random.nextInt(2)));
                                    shards.setGlowingTag(true);
                                    lvl.addFreshEntity(shards);
                                    if (lvl.random.nextFloat() < 0.45F) {
                                        ItemEntity tear = new ItemEntity(lvl, z.center.x, z.center.y + 0.65D, z.center.z,
                                                new ItemStack(ModItems.WEEPING_AMBER_TEAR.get(), 1));
                                        tear.setGlowingTag(true);
                                        lvl.addFreshEntity(tear);
                                    }
                                    lvl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(
                                                    ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.HONEY_BLOCK.defaultBlockState()),
                                            z.center.x, z.center.y + 0.7D, z.center.z, 28, 0.45D, 0.55D, 0.45D, 0.1D);
                                    lvl.playSound(null, sp, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.25F);
                                    return false;
                                }
                                return true;
                            }
                    ));
                } else {
                    ItemEntity amberDrop = new ItemEntity(level, deceased.getX(), deceased.getY() + 0.5D, deceased.getZ(),
                            new ItemStack(ModItems.AMBER_CHUNK.get(), 2));
                    amberDrop.setGlowingTag(true);
                    level.addFreshEntity(amberDrop);
                }
            } else {
                ItemEntity amberDrop = new ItemEntity(level, deceased.getX(), deceased.getY() + 0.5D, deceased.getZ(),
                        new ItemStack(ModItems.AMBER_CHUNK.get(), 1 + level.random.nextInt(2)));
                amberDrop.setGlowingTag(true);
                level.addFreshEntity(amberDrop);
                if (!isBossEntity(level, deceased)) {
                    net.minecraft.core.BlockPos sp = deceased.blockPosition();
                    if (level.getBlockState(sp).isAir()) {
                        level.setBlockAndUpdate(sp, net.minecraft.world.level.block.Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
                    }
                    if (level.getBlockState(sp.above()).isAir()) {
                        level.setBlockAndUpdate(sp.above(), net.minecraft.world.level.block.Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
                    }
                    level.playSound(null, sp, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.PLAYERS, 1.0F, 0.85F);
                }
            }
        }

        // Blight (#26) Cadaverous Contagion Chain Mark / Necro-Spore Pod & Necrotic Thorn Gouge / Snare Corpse Burst!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BLIGHT_CHAIN_MARK_UNTIL).orElse(0L)) {
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, deceased.getX(), deceased.getY(0.7D), deceased.getZ(), 26, 1.4D, 0.5D, 1.4D, 0.05D);
            for (LivingEntity cloudVictim : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(3.0D))) {
                if (cloudVictim == deceased || !cloudVictim.isAlive() || !(cloudVictim instanceof Monster)) continue;
                cloudVictim.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            }
            int jumped = 0;
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(6.0D))) {
                if (foe == deceased || !foe.isAlive() || !(foe instanceof Monster)) continue;
                foe.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 5));
                foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 30, 0));
                applyTimedAttributeShred(foe, level, 1.0D, 0.0D, 80L);
                EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_BLIGHT_CHAIN_MARK_UNTIL, now + 60L);
                foe.hurtServer(level, level.damageSources().magic(), 4.5F);
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, foe.getX(), foe.getY(0.7D), foe.getZ(), 12, 0.3D, 0.3D, 0.3D, 0.04D);
                if (++jumped >= 2) break;
            }
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BLIGHT_CORPSE_SNARE_UNTIL).orElse(0L)) {
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, deceased.getX(), deceased.getY(0.6D), deceased.getZ(), 24, 1.2D, 0.4D, 1.2D, 0.04D);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(3.5D))) {
                if (foe == deceased || !foe.isAlive() || !(foe instanceof Monster)) continue;
                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
                foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
            }
            net.minecraft.core.BlockPos dp = deceased.blockPosition();
            int plantedRoses = 0;
            for (net.minecraft.core.BlockPos cand : List.of(dp, dp.north(), dp.south(), dp.east(), dp.west())) {
                if (level.getBlockState(cand).isAir() && level.getBlockState(cand.below()).isSolidRender()) {
                    level.setBlockAndUpdate(cand, net.minecraft.world.level.block.Blocks.WITHER_ROSE.defaultBlockState());
                    if (++plantedRoses >= 3) break;
                }
            }
        }

        // Soulfire (#28) Phantasmal Soul Cleave / Chain Burst & Beam Soul Sand Dissolution!
        boolean soulBurst = now <= data.getLong(SpellLingeringZoneManager.TAG_SOULFIRE_CHAIN_BURST_UNTIL).orElse(0L);
        boolean soulSandMark = now <= data.getLong(SpellLingeringZoneManager.TAG_SOULFIRE_SOULSAND_MARK_UNTIL).orElse(0L);
        if (soulBurst || soulSandMark) {
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, deceased.getX(), deceased.getY(0.7D), deceased.getZ(), 28, 1.1D, 0.5D, 1.1D, 0.06D);
            level.sendParticles(ParticleTypes.SOUL, deceased.getX(), deceased.getY(0.8D), deceased.getZ(), 10, 0.5D, 0.5D, 0.5D, 0.04D);
            long burnUntil = now + 100L;
            java.util.Set<java.util.UUID> soulBurstHit = new java.util.HashSet<>();
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(3.5D))) {
                if (foe == deceased || !foe.isAlive() || !(foe instanceof Monster)) continue;
                soulBurstHit.add(foe.getUUID());
                EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);
                foe.igniteForSeconds(5);
                foe.hurtServer(level, level.damageSources().magic(), 4.0F);
            }
            if (soulBurst) {
                for (LivingEntity chainedPeer : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(18.0D))) {
                    if (chainedPeer == deceased || !chainedPeer.isAlive() || soulBurstHit.contains(chainedPeer.getUUID())) continue;
                    if (EntityHelper.getPersistentData(chainedPeer).getLong(SpellLingeringZoneManager.TAG_SOULFIRE_CHAIN_BURST_UNTIL).orElse(0L) > now) {
                        EntityHelper.getPersistentData(chainedPeer).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);
                        chainedPeer.igniteForSeconds(5);
                        chainedPeer.hurtServer(level, level.damageSources().magic(), 4.5F);
                        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, chainedPeer.getX(), chainedPeer.getY(0.8D), chainedPeer.getZ(),
                                14, 0.3D, 0.4D, 0.3D, 0.05D);
                    }
                }
            }
            if (soulSandMark && !(deceased instanceof Player)) {
                net.minecraft.core.BlockPos below = deceased.blockPosition().below();
                var bs = level.getBlockState(below);
                if ((bs.is(net.minecraft.tags.BlockTags.DIRT) || bs.is(net.minecraft.tags.BlockTags.SAND))
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, below, bs)) {
                    level.setBlockAndUpdate(below, net.minecraft.world.level.block.Blocks.SOUL_SAND.defaultBlockState());
                } else {
                    ItemEntity ash = new ItemEntity(level, deceased.getX(), deceased.getY() + 0.4D, deceased.getZ(),
                            new ItemStack(net.minecraft.world.item.Items.BONE_MEAL, 2));
                    level.addFreshEntity(ash);
                }
            }
        }

        // Vapor (#29) Superheated Hydro-Gouge: slain target dissolves into a blinding scalding steam cloud!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_VAPOR_BOIL_DEATH_UNTIL).orElse(0L)) {
            level.sendParticles(ParticleTypes.CLOUD, deceased.getX(), deceased.getY(0.8D), deceased.getZ(), 26, 1.3D, 0.6D, 1.3D, 0.05D);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(3.5D))) {
                if (foe == deceased || !foe.isAlive() || !(foe instanceof Monster)) continue;
                foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                SpellLingeringZoneManager.applySoaked(foe, 120);
            }
        }

        long until = data.getLong(TAG_HEX_UNTIL).orElse(0L);
        if (until <= now) return;

        String typeStr = data.getString(TAG_HEX_TYPE).orElse("");
        if (typeStr.isEmpty()) return;
        EssenceType hexType;
        try {
            hexType = EssenceType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return;
        }

        boolean hexMutated = data.getBoolean(TAG_HEX_MUTATED).orElse(true);
        long window = data.getLong(TAG_HEX_INITIAL_WINDOW).orElse(0L);
        boolean tier6Contagion = hexMutated && (hexType == EssenceType.ESCHATON
                || hexType == EssenceType.APOTHEOSIS
                || hexType == EssenceType.ENTROPICA);
        boolean undeadPlagueSpread = hexMutated && (hexType == EssenceType.UNDEAD || hexType == EssenceType.BLIGHT);
        if (!undeadPlagueSpread && !tier6Contagion && now > window) {
            return;
        }

        int tier = data.getInt(TAG_HEX_TIER).orElse(1);
        double spreadRadius = tier6Contagion ? 6.0D : 5.0D;
        AABB box = deceased.getBoundingBox().inflate(spreadRadius);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (nearby == deceased || !nearby.isAlive() || nearby instanceof Player) continue;
            if (nearby instanceof Monster) {
                applyPersistentHex(level, null, nearby, hexType, tier, 300, false, hexMutated);
                level.sendParticles(ParticleTypes.SOUL, nearby.getX(), nearby.getY(0.6), nearby.getZ(),
                        8, 0.25, 0.3, 0.25, 0.04);
            }
        }
    }

    // --- Ward Blue Arcane Shield Pool (Section 5.3: 1.5x Base Damage Scaling, Combat-Only, Zero-Overflow Shield Gate, 25% Fuel Focus) ---

    public static void grantWardShield(LivingEntity target, EssenceType wardMateria, int potencyTier, float potencyMult, int durationTicks) {
        if (!(target.level() instanceof ServerLevel level)) return;
        if (wardMateria != null && wardMateria != EssenceType.REGULAR) {
            checkElementalHexCounterCleanse(target, wardMateria);
        }
        float shieldPool = MateriaStageTable.getBaseWardShieldHp(potencyTier) * Math.max(0.5F, potencyMult);
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        float existing = now <= data.getLong(TAG_WARD_UNTIL).orElse(0L)
                ? data.getFloat(TAG_WARD_HP).orElse(0.0F) : 0.0F;
        float merged = Math.min(shieldPool * 1.5F, Math.max(existing, shieldPool));

        data.putFloat(TAG_WARD_HP, merged);
        data.putLong(TAG_WARD_UNTIL, now + Math.max(60, durationTicks));
        data.putString(TAG_WARD_MATERIA, (wardMateria == null ? EssenceType.AEGIS : wardMateria).name());

        level.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY(1.0), target.getZ(),
                20, 0.45, 0.6, 0.45, 0.15);
        level.playSound(null, target.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7F, 1.65F);

        if (target instanceof Player player) {
            player.displayClientMessage(Component.literal(String.format(
                    "§b[Arcane Ward] §fShield Active: §b%.1f HP §7(%ds)", merged, durationTicks / 20
            )), true);
        }
    }

    /**
     * Intercepts incoming hurt events for Elemental Shrouds, Hex incoming damage amplifiers, and Blue Arcane Ward Shields.
     */
    public static EventResult interceptWardDamage(LivingEntity entity, DamageSource source, float amount) {
        if (entity.level().isClientSide() || amount <= 0.0F) {
            return EventResult.pass();
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return EventResult.pass();
        }

        CompoundTag data = EntityHelper.getPersistentData(entity);
        long now = level.getGameTime();

        // Glacial Cryo-Cocoon Mending (#15) & Tier 4–6 Complete Damage Invulnerability / Void Immunity / Prismatic Mirror Dome!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_COMPLETE_INVULNERABILITY_UNTIL).orElse(0L)
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            level.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY(0.95D), entity.getZ(),
                    10, 0.35D, 0.45D, 0.35D, 0.04D);
            return EventResult.interruptFalse();
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_VOID_IMMUNE_UNTIL).orElse(0L)
                && source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)) {
            entity.resetFallDistance();
            return EventResult.interruptFalse();
        }
        if (!REENTRANT_GUST_HURT.get()
                && now <= data.getLong(SpellLingeringZoneManager.TAG_PRISMATIC_DEFLECT_SENTRY_UNTIL).orElse(0L)
                && (source.is(DamageTypeTags.IS_PROJECTILE) || source.is(DamageTypeTags.WITCH_RESISTANT_TO))) {
            Entity reflectTarget = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            if (reflectTarget instanceof LivingEntity atk && atk != entity) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    atk.hurtServer(level, level.damageSources().magic(), amount);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
            level.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY(0.95D), entity.getZ(),
                    12, 0.35D, 0.45D, 0.35D, 0.05D);
            return EventResult.interruptFalse();
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_CRYO_COCOON_UNTIL).orElse(0L)
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            level.sendParticles(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY(0.9D), entity.getZ(),
                    10, 0.35D, 0.45D, 0.35D, 0.04D);
            level.playSound(null, entity.blockPosition(), SoundEvents.GLASS_HIT, SoundSource.PLAYERS, 0.7F, 1.55F);
            return EventResult.interruptFalse();
        }

        // Pacified Mob Attack Suppression (#20 Oasis Blessing Touch / Greening Mist / Mirage Decoy, #18 Spore Decoy)
        if (source.getEntity() instanceof Mob pacifiedAttacker
                && now <= EntityHelper.getPersistentData(pacifiedAttacker).getLong(SpellLingeringZoneManager.TAG_PACIFIED_UNTIL).orElse(0L)) {
            pacifiedAttacker.setTarget(null);
            pacifiedAttacker.setAggressive(false);
            return EventResult.interruptFalse();
        }

        // Nether Immolation Shroud (#6): 100% fire/lava immunity & melee attacker ignition
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FIRE)) {
                entity.clearFire();
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                attacker.igniteForSeconds(4);
            }
        }

        // Lightning Static Shroud (#12): absorb lightning strikes into full Materia fuel restoration & shock melee attackers
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_LIGHTNING_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_LIGHTNING)) {
                notifyMagicalHeal(entity, 6.0F);
                entity.heal(6.0F);
                if (entity instanceof Player player) {
                    SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.LIGHTNING, 64_000L);
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getY(1.0), entity.getZ(),
                        24, 0.4D, 0.6D, 0.4D, 0.12D);
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                attacker.addEffect(new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 10, 0));
            }
        }

        // Frozen Deep-Freeze Shatter Hex (#5): +50% incoming physical/melee/projectile shatter damage!
        if (!REENTRANT_GUST_HURT.get()
                && hasActiveMutatedHex(data, now, EssenceType.FROZEN)
                && !source.is(DamageTypeTags.IS_FIRE)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            float mult = 1.0F + 0.50F * getHexStackMultiplier(entity);
            REENTRANT_GUST_HURT.set(true);
            try {
                level.sendParticles(ParticleTypes.ITEM_SNOWBALL, entity.getX(), entity.getY(0.8D), entity.getZ(),
                        10, 0.3D, 0.35D, 0.3D, 0.06D);
                entity.hurtServer(level, source, amount * mult);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Umbral Sensory Void Hex (#8) on Players: +25% incoming melee damage from Monsters!
        if (!REENTRANT_GUST_HURT.get()
                && entity instanceof Player
                && hasActiveMutatedHex(data, now, EssenceType.UMBRAL)
                && source.getDirectEntity() instanceof Monster) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * (1.0F + 0.25F * getHexStackMultiplier(entity)));
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Arid Desiccation Mummification Hex (#11): double fire damage & Desiccated Fragility on players
        if (!REENTRANT_GUST_HURT.get() && hasActiveMutatedHex(data, now, EssenceType.ARID)) {
            if (source.is(DamageTypeTags.IS_FIRE)) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    entity.hurtServer(level, source, amount * 2.0F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
            if (entity instanceof Player && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                Vec3 kb = entity.position().subtract(attacker.position()).normalize().scale(0.35D);
                entity.push(kb.x, 0.1D, kb.z);
                entity.hasImpulse = true;
                if (entity.getArmorValue() > 0) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * 1.25F);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Frozen Glacial Spikes Retaliation (#5): stab melee attackers & cut attack speed by 30%
        if (!REENTRANT_GUST_HURT.get()
                && now <= data.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            REENTRANT_GUST_HURT.set(true);
            try {
                attacker.hurtServer(level, level.damageSources().thorns(entity), 2.5F);
                attacker.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 60, 1));
                attacker.setTicksFrozen(attacker.getTicksFrozen() + 50);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
        }

        // Umbral Silent Shadow Chains (#8): echo 25% of damage taken to other shadow-chained victims!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_UNTIL).orElse(0L)) {
            long group = data.getLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_GROUP).orElse(0L);
            if (group != 0L) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    float echo = Math.max(0.5F, amount * 0.25F);
                    for (LivingEntity peer : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(14.0D))) {
                        if (peer == entity || !peer.isAlive()) continue;
                        CompoundTag peerData = EntityHelper.getPersistentData(peer);
                        if (now <= peerData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_UNTIL).orElse(0L)
                                && peerData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_GROUP).orElse(0L) == group) {
                            peer.hurtServer(level, level.damageSources().magic(), echo);
                            level.sendParticles(ParticleTypes.SMOKE, peer.getX(), peer.getY(0.6), peer.getZ(),
                                    5, 0.2D, 0.3D, 0.2D, 0.02D);
                        }
                    }
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // Water Oceanic Shroud (#2): dampen incoming physical explosion damage by 40%
        if (!REENTRANT_GUST_HURT.get()
                && source.is(DamageTypeTags.IS_EXPLOSION)
                && now <= data.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * 0.60F);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Air Gust Shield (#1): 50% incoming projectile damage reduction
        if (!REENTRANT_GUST_HURT.get()
                && source.is(DamageTypeTags.IS_PROJECTILE)
                && now <= data.getLong(SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL).orElse(0L)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                level.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(0.8), entity.getZ(),
                        6, 0.3D, 0.35D, 0.3D, 0.04D);
                entity.hurtServer(level, source, amount * 0.5F);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Magma Igneous Armor Mantle (#13): fire immunity & melee attacker ignition
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FIRE)) {
                entity.clearFire();
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                attacker.igniteForSeconds(4);
            }
        }

        // Storm Waterlogged Hypoxia Hex (#14): 2.0x incoming Lightning damage!
        if (!REENTRANT_GUST_HURT.get()
                && source.is(DamageTypeTags.IS_LIGHTNING)
                && hasActiveMutatedHex(data, now, EssenceType.STORM)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * 2.0F);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Glacial Permafrost Cryo-Shroud (#15) & Taiga Cold Immunity (#19)
        boolean glacialShroud = now <= data.getLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL).orElse(0L);
        boolean taigaColdImmune = now <= data.getLong(SpellLingeringZoneManager.TAG_TAIGA_COLD_IMMUNE_UNTIL).orElse(0L);
        if ((glacialShroud || taigaColdImmune) && source.is(DamageTypeTags.IS_FREEZING)) {
            entity.setTicksFrozen(0);
            return EventResult.interruptFalse();
        }
        if (glacialShroud && !REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
            attacker.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 10, 0));
            attacker.setTicksFrozen(attacker.getTicksFrozen() + 45);
        }

        // Spore Cloud Mantle / Orbit (#18): melee attackers inhale Poison II + Nausea!
        if ((now <= data.getLong(SpellLingeringZoneManager.TAG_SPORE_MANTLE_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_SPORE_ORBIT_UNTIL).orElse(0L))
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            attacker.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
            attacker.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));
        }

        // Taiga Winter Solstice Shroud (#19): 50% incoming arrow/projectile deflection!
        if (source.is(DamageTypeTags.IS_PROJECTILE)
                && now <= data.getLong(SpellLingeringZoneManager.TAG_TAIGA_SHROUD_UNTIL).orElse(0L)
                && level.random.nextFloat() < 0.50F) {
            level.sendParticles(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY(0.9D), entity.getZ(),
                    8, 0.3D, 0.3D, 0.3D, 0.05D);
            return EventResult.interruptFalse();
        }

        // Taiga Petrifying Frost-Sap Hex / Brittle Mark (#19): +50% incoming physical/shatter damage!
        if (!REENTRANT_GUST_HURT.get()
                && (hasActiveMutatedHex(data, now, EssenceType.TAIGA)
                    || now <= data.getLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL).orElse(0L))
                && !source.is(DamageTypeTags.IS_FIRE)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                level.sendParticles(ParticleTypes.ITEM_SNOWBALL, entity.getX(), entity.getY(0.8D), entity.getZ(),
                        8, 0.3D, 0.35D, 0.3D, 0.06D);
                entity.hurtServer(level, source, amount * 1.50F);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Vitae Cellular Overcharge Mark (#21): taking damage pulses healing to nearby players/allies!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_VITAE_OVERCHARGE_UNTIL).orElse(0L)) {
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(6.0D))) {
                if (!ally.isAlive() || ally == entity || ally instanceof Monster) continue;
                notifyMagicalHeal(ally, 2.5F);
                ally.heal(2.5F);
                level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(),
                        2, 0.2D, 0.2D, 0.2D, 0.02D);
            }
        }

        // Vitae Cellular Rejuvenation Spray (#21 Disoriented Accuracy), Vitae Arrhythmia Hex (#21), & Blood Hemorrhage (#22 Attack Bleed)
        if (!REENTRANT_GUST_HURT.get() && source.getEntity() instanceof LivingEntity attacker && attacker != entity) {
            CompoundTag atkData = EntityHelper.getPersistentData(attacker);
            // Vitae (#21) Cellular Rejuvenation Spray: disorients enemies with 35% reduced attack accuracy!
            if (now <= atkData.getLong(SpellLingeringZoneManager.TAG_VITAE_DISORIENTED_UNTIL).orElse(0L)
                    && level.random.nextFloat() < 0.35F) {
                level.sendParticles(ParticleTypes.POOF, entity.getX(), entity.getY(0.85D), entity.getZ(),
                        5, 0.25D, 0.25D, 0.25D, 0.03D);
                return EventResult.interruptFalse();
            }
            // Vitae (#21) Hyper-Metabolic Arrhythmia Hex: self-damaging arrhythmia recoil whenever attacking!
            if (hasActiveMutatedHex(atkData, now, EssenceType.VITAE)) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    attacker.hurtServer(level, level.damageSources().magic(), 2.0F * getHexStackMultiplier(attacker));
                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, attacker.getX(), attacker.getY(0.8D), attacker.getZ(),
                            3, 0.2D, 0.2D, 0.2D, 0.03D);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
            // Blood (#22) Hemorrhage: extra bleeding laceration damage whenever moving OR attacking!
            if (now <= atkData.getLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL).orElse(0L)) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    attacker.hurtServer(level, level.damageSources().magic(), 1.75F);
                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, attacker.getX(), attacker.getY(0.75D), attacker.getZ(),
                            3, 0.2D, 0.2D, 0.2D, 0.03D);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // Aura Chakra Respiration Shroud (#23): 100% drowning & suffocation immunity!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_AURA_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_DROWNING)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) {
                return EventResult.interruptFalse();
            }
        }

        // Aura Chakra Severance Hex (#23): 2x knockback taken from direct attackers!
        if (hasActiveMutatedHex(data, now, EssenceType.AURA) && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            Vec3 kb = entity.position().subtract(attacker.position()).normalize().scale(0.65D);
            entity.push(kb.x, 0.22D, kb.z);
            entity.hasImpulse = true;
        }

        // Amber Petrified Bastion Mantle (#24): splash melee attackers with 40% slow for 3s!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_AMBER_MANTLE_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
        }

        // Oasis Succulent Thorn Carapace (#20): 20% physical damage reduction & thorn retaliation!
        if (!REENTRANT_GUST_HURT.get()
                && now <= data.getLong(SpellLingeringZoneManager.TAG_OASIS_CARAPACE_UNTIL).orElse(0L)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                    attacker.hurtServer(level, level.damageSources().thorns(entity), 2.5F);
                }
                entity.hurtServer(level, source, amount * 0.80F);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // --- Ethereal, Paradox & Fusion Shrouds / Tethers / Hex Vectors (#25 through #36) ---

        // Eclipse Solar Corona Bastion (#25): melee attackers set ablaze in unquenchable celestial fire & blinded!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ECLIPSE_BASTION_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            attacker.igniteForSeconds(5);
            attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            EntityHelper.getPersistentData(attacker).putLong(SpellLingeringZoneManager.TAG_ECLIPSE_CORONA_BURN_UNTIL, now + 100L);
        }
        // Eclipse Coronal Blind-Rot Hex (#25) on Players: +30% incoming melee damage in darkness (<= 7 light)!
        if (!REENTRANT_GUST_HURT.get()
                && entity instanceof Player
                && hasActiveMutatedHex(data, now, EssenceType.ECLIPSE)
                && source.getDirectEntity() instanceof Monster
                && level.getMaxLocalRawBrightness(entity.blockPosition()) <= 7) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * (1.0F + 0.30F * getHexStackMultiplier(entity)));
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Blight Cadaverous Briar-Carapace (#26): reflect 30% melee damage + Poison II for 3s!
        if (!REENTRANT_GUST_HURT.get()
                && now <= data.getLong(SpellLingeringZoneManager.TAG_BLIGHT_CARAPACE_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            REENTRANT_GUST_HURT.set(true);
            try {
                attacker.hurtServer(level, level.damageSources().thorns(entity), Math.max(1.5F, amount * 0.30F));
                attacker.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
        }
        // Blight Virulent Wither-Blight Hex (#26) Player Hex Vector (Virulent Blood-Tasting):
        // Hostile mobs striking infected player gain Strength I for 4.0s and leech 20% of damage dealt as health!
        if (entity instanceof Player
                && hasActiveMutatedHex(data, now, EssenceType.BLIGHT)
                && source.getEntity() instanceof Monster monsterAttacker
                && monsterAttacker.isAlive()) {
            monsterAttacker.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 80, 0));
            monsterAttacker.heal(Math.max(0.5F, amount * 0.20F));
        }

        // Astral Cosmic Starlight Shroud (#27): absorb Lightning/Void damage into Materia fuel & shock/repel melee attackers!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ASTRAL_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_LIGHTNING)
                    || (source.getEntity() instanceof LivingEntity atk && resolveEntityAffinities(atk).contains(EssenceType.VOID))) {
                if (entity instanceof Player player) {
                    SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.ASTRAL, 48_000L);
                }
                level.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY(1.0D), entity.getZ(), 16, 0.4D, 0.5D, 0.4D, 0.08D);
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                attacker.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 10, 0));
                Vec3 away = attacker.position().subtract(entity.position()).normalize().scale(0.95D);
                attacker.setDeltaMovement(away.x, 0.30D, away.z);
                attacker.hasImpulse = true;
            }
        }

        // Soulfire Phantasmal Soulfire Shroud (#28): fire/lava immunity + ignite melee attackers in soulfire & siphon 20%!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SOULFIRE_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FIRE)) {
                entity.clearFire();
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                attacker.igniteForSeconds(5);
                EntityHelper.getPersistentData(attacker).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, now + 100L);
                float siphon = Math.max(0.5F, amount * 0.20F);
                notifyMagicalHeal(entity, siphon);
                entity.heal(siphon);
            }
        }
        // Soulfire Phantasmal Soul-Burn Hex (#28) Player Hex Vector (Soul-Flame Beacon):
        // Hostiles ignite the afflicted player in soulfire and deal +30% bonus damage!
        if (!REENTRANT_GUST_HURT.get()
                && entity instanceof Player
                && hasActiveMutatedHex(data, now, EssenceType.SOULFIRE)
                && source.getEntity() instanceof Monster) {
            entity.igniteForSeconds(4);
            data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, now + 80L);
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * (1.0F + 0.30F * getHexStackMultiplier(entity)));
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Vapor Mist-Walker Shroud (#29) & Scalding Dissolution Hex (#29: +50% incoming Lightning shock damage!)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_VAPOR_MIST_SHROUD_UNTIL).orElse(0L) && source.is(DamageTypeTags.IS_FIRE)) {
            entity.clearFire();
            return EventResult.interruptFalse();
        }
        if (!REENTRANT_GUST_HURT.get()
                && source.is(DamageTypeTags.IS_LIGHTNING)
                && hasActiveMutatedHex(data, now, EssenceType.VAPOR)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * (1.0F + 0.50F * getHexStackMultiplier(entity)));
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Null_R Event Horizon Bastion / Orbit (#30): blind & repel melee attackers 4 blocks (or zero-g lift + orb discharge on orbit)!
        if ((now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_R_BASTION_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_R_ORBIT_UNTIL).orElse(0L))
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
            Vec3 away = attacker.position().subtract(entity.position()).normalize().scale(1.15D);
            attacker.setDeltaMovement(away.x, 0.58D, away.z);
            attacker.hasImpulse = true;
            attacker.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 50, 0));
            if (now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_R_ORBIT_UNTIL).orElse(0L)) {
                ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, attacker, 2);
            }
        }
        // Null_R Antimatter Annihilation Hex (#30): permanently disables shield blocking!
        if (entity instanceof Player hexedShieldPlayer && hasActiveMutatedHex(data, now, EssenceType.NULL_R)) {
            if (hexedShieldPlayer.isBlocking() && !hexedShieldPlayer.getUseItem().isEmpty()) {
                hexedShieldPlayer.getCooldowns().addCooldown(hexedShieldPlayer.getUseItem(), 100);
                hexedShieldPlayer.stopUsingItem();
            }
        }

        // Null_U Abyssal Zero-Acoustic Veil / Orbit (#31): blind & silence melee attackers (or pull inward + orb discharge on orbit)!
        if ((now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_U_VEIL_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_U_ORBIT_UNTIL).orElse(0L))
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            SpellLingeringZoneManager.applyArcaneSilence(attacker, 60);
            if (now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_U_ORBIT_UNTIL).orElse(0L)) {
                Vec3 pull = entity.position().subtract(attacker.position()).normalize().scale(0.55D);
                attacker.setDeltaMovement(pull.x, 0.20D, pull.z);
                attacker.hasImpulse = true;
                ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, attacker, 2);
            }
        }
        // Null_U Sensory Blackout Hex (#31) on Players: monsters deal +25% ambush damage!
        if (!REENTRANT_GUST_HURT.get()
                && entity instanceof Player
                && hasActiveMutatedHex(data, now, EssenceType.NULL_U)
                && source.getDirectEntity() instanceof Monster) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * (1.0F + 0.25F * getHexStackMultiplier(entity)));
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Dawn Convective Horizon Orbit (#32): hurls orbiting daylight orbs outward toward enemies when striking in melee!
        if (source.getDirectEntity() instanceof LivingEntity dawnAttacker && dawnAttacker != entity
                && now <= EntityHelper.getPersistentData(dawnAttacker).getLong(SpellLingeringZoneManager.TAG_DAWN_ORBIT_UNTIL).orElse(0L)) {
            ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, dawnAttacker, entity, 2);
        }
        // Dawn Solar Shroud (#32): 3-block gale pushback + 2s Blindness on melee attackers!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_DAWN_SHROUD_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            Vec3 away = attacker.position().subtract(entity.position()).normalize().scale(0.95D);
            attacker.setDeltaMovement(away.x, 0.32D, away.z);
            attacker.hasImpulse = true;
        }
        // Dawn Solar Blind-Sear Hex (#32) on Players: incoming projectiles deal +30% bonus glare exposure damage!
        if (!REENTRANT_GUST_HURT.get()
                && entity instanceof Player
                && source.is(DamageTypeTags.IS_PROJECTILE)
                && hasActiveMutatedHex(data, now, EssenceType.DAWN)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * (1.0F + 0.30F * getHexStackMultiplier(entity)));
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Abyss Hadopelagic Trench Shroud & Orbit (#33): blind + drench melee attackers, or slow 40% + knockback!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ABYSS_SHROUD_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            SpellLingeringZoneManager.applySoaked(attacker, 140);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ABYSS_ORBIT_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
            Vec3 away = attacker.position().subtract(entity.position()).normalize().scale(0.80D);
            attacker.setDeltaMovement(away.x, 0.25D, away.z);
            attacker.hasImpulse = true;
        }
        // Abyss Crushing Benthic Drowning Hex (#33): 2.0x incoming Lightning damage + Hadopelagic Helplessness on Players!
        if (!REENTRANT_GUST_HURT.get() && hasActiveMutatedHex(data, now, EssenceType.ABYSS)) {
            if (source.is(DamageTypeTags.IS_LIGHTNING)) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    entity.hurtServer(level, source, amount * 2.0F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
            if (entity instanceof Player && source.getEntity() instanceof LivingEntity attacker && attacker != entity
                    && (attacker instanceof Drowned || attacker instanceof Guardian || attacker instanceof ElderGuardian
                    || SpellLingeringZoneManager.isEntityWetOrSoaked(attacker))) {
                attacker.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 1));
                REENTRANT_GUST_HURT.set(true);
                try {
                    entity.hurtServer(level, source, amount * 2.0F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
        }

        // Aegis Gilded Bastion Orbit (#34): launches orbiting golden granite plates outward to batter melee attackers!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_AEGIS_ORBIT_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity aegisAttacker && aegisAttacker != entity) {
            ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, aegisAttacker, 2);
        }
        // Aegis Citadel Tether Beam (#34): 50% damage reduction on anchored ally!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_AEGIS_TETHER_UNTIL).orElse(0L)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * 0.50F);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }
        // Aegis Gilded Bastion Citadel Mantle (#34): -25% physical damage reduction & reflect 40% melee damage + 2-block knockback!
        if (!REENTRANT_GUST_HURT.get()
                && now <= data.getLong(SpellLingeringZoneManager.TAG_AEGIS_MANTLE_UNTIL).orElse(0L)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                    attacker.hurtServer(level, level.damageSources().thorns(entity), Math.max(1.5F, amount * 0.40F));
                    Vec3 away = attacker.position().subtract(entity.position()).normalize().scale(0.68D);
                    attacker.setDeltaMovement(away.x, 0.22D, away.z);
                    attacker.hasImpulse = true;
                }
                entity.hurtServer(level, source, amount * 0.75F);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }
        // Aegis Petrifying Citadel Sundering Hex (#34): +35% incoming blunt/physical damage & Fractured Bastion (+40% true damage on Players)!
        if (!REENTRANT_GUST_HURT.get()
                && hasActiveMutatedHex(data, now, EssenceType.AEGIS)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                if (entity instanceof Player && source.getEntity() instanceof Monster) {
                    entity.hurtServer(level, level.damageSources().magic(), amount * (1.0F + 0.40F * getHexStackMultiplier(entity)));
                } else {
                    entity.hurtServer(level, source, amount * (1.0F + 0.35F * getHexStackMultiplier(entity)));
                }
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Aurora Borealis Prismatic Veil Mantle (#35): freeze immunity + melee attacker Blindness & 2s Frostbite!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_AURORA_MANTLE_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FREEZING)) {
                entity.setTicksFrozen(0);
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2));
                attacker.setTicksFrozen(Math.min(attacker.getTicksRequiredToFreeze() + 40, attacker.getTicksFrozen() + 60));
            }
        }
        if (!REENTRANT_GUST_HURT.get()
                && now <= data.getLong(SpellLingeringZoneManager.TAG_AURORA_BRITTLE_UNTIL).orElse(0L)
                && !source.is(DamageTypeTags.IS_FIRE)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * 1.35F);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Mirage Spatial Displacement Glamour Shroud (#36: 50% miss), Mirror Orbit (#36: blind attackers), Tether Redirect (50%), & Delusional Paranoia Hex!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_MIRAGE_SHROUD_UNTIL).orElse(0L)
                && (source.getDirectEntity() != null || source.is(DamageTypeTags.IS_PROJECTILE))
                && level.random.nextFloat() < 0.50F) {
            level.sendParticles(ParticleTypes.POOF, entity.getX(), entity.getY(0.9D), entity.getZ(), 8, 0.35D, 0.4D, 0.35D, 0.04D);
            return EventResult.interruptFalse();
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_MIRAGE_ORBIT_UNTIL).orElse(0L)) {
            if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
            }
            for (LivingEntity adjFoe : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(4.0D))) {
                if (adjFoe != entity && adjFoe.isAlive() && !adjFoe.isAlliedTo(entity) && adjFoe instanceof Monster) {
                    adjFoe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                }
            }
        }
        // Attacker infected by Mirage Phantasmal Paranoia & Traitor Strike (#36): treats caster as completely invisible!
        if (source.getEntity() instanceof LivingEntity paranoiaAttacker && paranoiaAttacker != entity) {
            CompoundTag pData = EntityHelper.getPersistentData(paranoiaAttacker);
            if (now <= pData.getLong(SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_UNTIL).orElse(0L)
                    && entity.getUUID().toString().equals(pData.getString(SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_CASTER_UUID).orElse(""))) {
                if (paranoiaAttacker instanceof Mob pMob) {
                    pMob.setTarget(null);
                    pMob.setAggressive(false);
                }
                return EventResult.interruptFalse();
            }
        }
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_MIRAGE_REDIRECT_UNTIL).orElse(0L)) {
            String redirStr = data.getString(SpellLingeringZoneManager.TAG_MIRAGE_REDIRECT_UUID).orElse("");
            if (!redirStr.isEmpty()) {
                try {
                    Entity linked = level.getEntity(java.util.UUID.fromString(redirStr));
                    if (linked instanceof LivingEntity victim && victim.isAlive() && victim != entity && entity.distanceToSqr(victim) <= 400.0D) {
                        REENTRANT_GUST_HURT.set(true);
                        try {
                            victim.hurtServer(level, level.damageSources().magic(), amount * 0.50F);
                            entity.hurtServer(level, source, amount * 0.50F);
                        } finally {
                            REENTRANT_GUST_HURT.set(false);
                        }
                        return EventResult.interruptFalse();
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        // Attacker afflicted with Mirage Delusional Phantasm Paranoia Hex (#36):
        // Cannot harm the Hex caster (un-targetable mirage) AND 50% of all swings/projectiles miss into thin air!
        if (source.getEntity() instanceof LivingEntity mirageAttacker && mirageAttacker != entity) {
            CompoundTag mData = EntityHelper.getPersistentData(mirageAttacker);
            if (hasActiveMutatedHex(mData, now, EssenceType.MIRAGE)) {
                LivingEntity hexOwner = resolveHexCaster(level, mirageAttacker);
                if (hexOwner != null && hexOwner == entity) {
                    if (mirageAttacker instanceof Mob mMob) {
                        mMob.setTarget(null);
                    }
                    level.sendParticles(ParticleTypes.ENCHANT, entity.getX(), entity.getY(0.9D), entity.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.04D);
                    return EventResult.interruptFalse();
                }
                if (level.random.nextFloat() < 0.50F) {
                    level.sendParticles(ParticleTypes.POOF, entity.getX(), entity.getY(0.85D), entity.getZ(), 5, 0.25D, 0.25D, 0.25D, 0.03D);
                    return EventResult.interruptFalse();
                }
            }
        }
        // Mirage Paranoid Guard-Fumble (#36) on Hexed Players: 40% shield fumble + 35% monster 1.5x critical strike!
        if (!REENTRANT_GUST_HURT.get()
                && entity instanceof Player mirageHexedPlayer
                && hasActiveMutatedHex(data, now, EssenceType.MIRAGE)
                && source.getEntity() instanceof Monster) {
            if (mirageHexedPlayer.isBlocking() && !mirageHexedPlayer.getUseItem().isEmpty() && level.random.nextFloat() < 0.40F) {
                mirageHexedPlayer.getCooldowns().addCooldown(mirageHexedPlayer.getUseItem(), 100);
                mirageHexedPlayer.stopUsingItem();
                level.playSound(null, mirageHexedPlayer.blockPosition(), SoundEvents.SHIELD_BREAK.value(), SoundSource.HOSTILE, 0.9F, 0.95F);
            }
            if (level.random.nextFloat() < 0.35F) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    level.sendParticles(ParticleTypes.CRIT, mirageHexedPlayer.getX(), mirageHexedPlayer.getY(0.9D), mirageHexedPlayer.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.1D);
                    mirageHexedPlayer.hurtServer(level, source, amount * 1.50F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
        }

        // --- Sections 6.38–6.49 (#37 Wraith through Chimera Catalyst) Shrouds, Orbits & Hex Reactions ---

        // #37 Wraith Intangible Phase-Step (caster beneficial dodge) & Offensive Ghost-Stasis (attacker cannot deal damage!)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_INTANGIBLE_CASTER_UNTIL).orElse(0L)
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            level.sendParticles(ParticleTypes.SOUL, entity.getX(), entity.getY(0.9D), entity.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.02D);
            return EventResult.interruptFalse();
        }
        if (source.getEntity() instanceof LivingEntity intangibleAttacker && intangibleAttacker != entity
                && now <= EntityHelper.getPersistentData(intangibleAttacker).getLong(SpellLingeringZoneManager.TAG_INTANGIBLE_OFFENSIVE_UNTIL).orElse(0L)) {
            return EventResult.interruptFalse();
        }

        // Attacker Dazzle (#40 Penumbra Mantle 30% miss), Penumbra Contrast-Collapse Hex (50% miss), & Rime Enchantment-Suppression
        if (source.getEntity() instanceof LivingEntity atkEntity && atkEntity != entity) {
            CompoundTag atkData = EntityHelper.getPersistentData(atkEntity);
            if (now <= atkData.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_DAZZLE_UNTIL).orElse(0L)
                    && level.random.nextFloat() < 0.30F) {
                level.sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY(0.9D), entity.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.02D);
                return EventResult.interruptFalse();
            }
            if (hasActiveMutatedHex(atkData, now, EssenceType.PENUMBRA) && level.random.nextFloat() < 0.50F) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, entity.getX(), entity.getY(0.85D), entity.getZ(), 6, 0.25D, 0.25D, 0.25D, 0.03D);
                return EventResult.interruptFalse();
            }
            // Outgoing melee boosts & Orbit dart discharges (#39 Pyre, #40 Penumbra, #41 Rime, #42 Spring, #43 Static, Chimera)
            if (source.getDirectEntity() == atkEntity) {
                if (now <= atkData.getLong(SpellLingeringZoneManager.TAG_PYRE_ORBIT_UNTIL).orElse(0L)
                        || now <= atkData.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_ORBIT_UNTIL).orElse(0L)
                        || now <= atkData.getLong(SpellLingeringZoneManager.TAG_RIME_ORBIT_UNTIL).orElse(0L)
                        || now <= atkData.getLong(SpellLingeringZoneManager.TAG_STATIC_ORBIT_UNTIL).orElse(0L)) {
                    ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, atkEntity, entity, 2);
                }
                if (now <= atkData.getLong(SpellLingeringZoneManager.TAG_SPRING_ORBIT_UNTIL).orElse(0L)) {
                    ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, atkEntity, entity, 2);
                    notifyMagicalHeal(atkEntity, 1.5F);
                    atkEntity.heal(1.5F);
                }
                if (!REENTRANT_GUST_HURT.get()
                        && (now <= atkData.getLong(SpellLingeringZoneManager.TAG_PYRE_MELEE_BOOST_UNTIL).orElse(0L)
                        || now <= atkData.getLong(SpellLingeringZoneManager.TAG_CHIMERA_PRIMAL_ROAR_UNTIL).orElse(0L))) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * 1.25F);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
                if (!REENTRANT_GUST_HURT.get()
                        && hasActiveMutatedHex(atkData, now, EssenceType.RIME)
                        && atkEntity.getMainHandItem().isEnchanted()) {
                    // #41 Rime Sub-Zero Cryo-Numbness Hex suppresses weapon enchantments while afflicted!
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * 0.78F);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // #37 Wraith Specter Ectoplasm Armor (suffocation/wither immune + 25% magic reflect) & Soul-Shield Orbit (20% physical -> hunger)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get()
                    && source.is(DamageTypeTags.WITCH_RESISTANT_TO)
                    && source.getEntity() instanceof LivingEntity magicAtk && magicAtk != entity) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    magicAtk.hurtServer(level, level.damageSources().magic(), Math.max(1.0F, amount * 0.25F));
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_WRAITH_ORBIT_UNTIL).orElse(0L)
                && entity instanceof Player wraithPlayer
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            int foodGain = Math.max(1, Math.round(amount * 0.20F));
            wraithPlayer.getFoodData().eat(foodGain, 0.35F);
        }

        // #38 Barrow Shroud (Wither immune), Ossuary Bone-Shield Orbit (-25% physical + bone shrapnel), & Sepulchral Hex (+35% physical)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BARROW_SHROUD_UNTIL).orElse(0L)
                && source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
            return EventResult.interruptFalse();
        }
        if (!REENTRANT_GUST_HURT.get()
                && now <= data.getLong(SpellLingeringZoneManager.TAG_BARROW_ORBIT_UNTIL).orElse(0L)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                    attacker.hurtServer(level, level.damageSources().thorns(entity), 2.5F);
                }
                entity.hurtServer(level, source, amount * 0.75F);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }
        if (!REENTRANT_GUST_HURT.get()
                && hasActiveMutatedHex(data, now, EssenceType.BARROW)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * (1.0F + 0.35F * getHexStackMultiplier(entity)));
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // #39 Pyre Shroud (Fire/Lava/Freeze immune), Orbit (4s attacker burn), & Cremation Hex (struck victims emit incendiary embers!)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_PYRE_SHROUD_UNTIL).orElse(0L)
                && (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FREEZING))) {
            entity.clearFire();
            entity.setTicksFrozen(0);
            return EventResult.interruptFalse();
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_PYRE_ORBIT_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity pyreAtk && pyreAtk != entity) {
            pyreAtk.igniteForSeconds(4);
        }
        if (!REENTRANT_GUST_HURT.get() && hasActiveMutatedHex(data, now, EssenceType.PYRE)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                for (LivingEntity peer : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(3.5D))) {
                    if (peer == entity || !peer.isAlive()) continue;
                    if ((entity instanceof Monster && peer instanceof Monster) || peer.isAlliedTo(entity)) {
                        peer.igniteForSeconds(3);
                        peer.hurtServer(level, level.damageSources().onFire(), 2.0F);
                    }
                }
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
        }

        // #40 Penumbra Shimmer Dodge, Mantle (Penumbral Dazzle), & Orbit (contact damage + 3s blind)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_SHIMMER_UNTIL).orElse(0L)
                && (source.getDirectEntity() != null || source.is(DamageTypeTags.IS_PROJECTILE))) {
            float dodgePct = data.getFloat(SpellLingeringZoneManager.TAG_PENUMBRA_SHIMMER_DODGE).orElse(0.20F);
            if (level.random.nextFloat() < dodgePct) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, entity.getX(), entity.getY(0.9D), entity.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.04D);
                return EventResult.interruptFalse();
            }
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_MANTLE_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity penAtk && penAtk != entity) {
            EntityHelper.getPersistentData(penAtk).putLong(SpellLingeringZoneManager.TAG_PENUMBRA_DAZZLE_UNTIL, now + 60L);
        }
        if (!REENTRANT_GUST_HURT.get()
                && now <= data.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_ORBIT_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity penAtk && penAtk != entity) {
            REENTRANT_GUST_HURT.set(true);
            try {
                penAtk.hurtServer(level, level.damageSources().magic(), 2.5F);
                penAtk.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
        }

        // #41 Rime Mantle, Orbit, Hoarfrost Carapace (50% fire res), & Cryo-Numbness Hex (armor enchant suppression)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_RIME_MANTLE_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FREEZING) || source.is(DamageTypeTags.IS_FIRE)) {
                entity.clearFire();
                entity.setTicksFrozen(0);
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity rimeAtk && rimeAtk != entity) {
                applyTimedAttackDebuff(rimeAtk, level, 0.0D, 0.35D, 80L);
                rimeAtk.setTicksFrozen(Math.min(rimeAtk.getTicksRequiredToFreeze() + 40, rimeAtk.getTicksFrozen() + 60));
            }
        } else if (now <= data.getLong(SpellLingeringZoneManager.TAG_RIME_ORBIT_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FREEZING)) {
                entity.setTicksFrozen(0);
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity rimeAtk && rimeAtk != entity) {
                applyTimedAttackDebuff(rimeAtk, level, 0.0D, 0.30D, 60L);
            }
        }
        if (!REENTRANT_GUST_HURT.get()
                && now <= data.getLong(SpellLingeringZoneManager.TAG_RIME_FIRE_RES_UNTIL).orElse(0L)
                && source.is(DamageTypeTags.IS_FIRE)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * 0.50F);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }
        if (!REENTRANT_GUST_HURT.get() && hasActiveMutatedHex(data, now, EssenceType.RIME)) {
            boolean hasEnchantedArmor = false;
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                if (entity.getItemBySlot(slot).isEnchanted()) {
                    hasEnchantedArmor = true;
                    break;
                }
            }
            if (hasEnchantedArmor) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    entity.hurtServer(level, source, amount * 1.25F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
        }

        // #42 Spring Vernal Fountainhead Mantle & Orbit (Drowning, Wither, Fire immunity)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SPRING_MANTLE_UNTIL).orElse(0L)
                && (source.is(DamageTypeTags.IS_DROWNING)
                || source.is(DamageTypeTags.IS_FIRE)
                || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER))) {
            entity.clearFire();
            return EventResult.interruptFalse();
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SPRING_ORBIT_UNTIL).orElse(0L)
                && (source.is(DamageTypeTags.IS_DROWNING) || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER))) {
            return EventResult.interruptFalse();
        }

        // #43 Static Dielectric Faraday Shroud & Galvanic Tesla Orbit
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_STATIC_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_LIGHTNING)
                    || source.is(DamageTypeTags.IS_FALL)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.THORNS)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.FLY_INTO_WALL)) {
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity staticAtk && staticAtk != entity) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    staticAtk.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                    staticAtk.hurtServer(level, level.damageSources().lightningBolt(), 3.5F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        } else if (now <= data.getLong(SpellLingeringZoneManager.TAG_STATIC_ORBIT_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_LIGHTNING) || source.is(DamageTypeTags.IS_FALL)) {
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity staticAtk && staticAtk != entity) {
                staticAtk.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
            }
        } else if (now <= data.getLong(SpellLingeringZoneManager.TAG_STATIC_SENTRY_IMMUNE_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_LIGHTNING) || source.is(DamageTypeTags.IS_FALL)) {
                entity.resetFallDistance();
                return EventResult.interruptFalse();
            }
        }

        // #44 Sylvan Primeval Photosynthetic Aegis (absorbs up to 12 hits, converting 50% into Materia fuel) & Orbit (1.5s attacker root)
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL).orElse(0L)) {
            int hitsLeft = data.getInt(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_HITS).orElse(0);
            if (hitsLeft > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                data.putInt(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_HITS, hitsLeft - 1);
                if (hitsLeft - 1 <= 0) {
                    data.remove(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL);
                }
                if (entity instanceof Player sylvanPlayer) {
                    long restored = Math.max(8L, Math.round(amount * 0.50F * 16.0F));
                    SpellFuelService.restorePlayerMateriaFuel(sylvanPlayer, EssenceType.SYLVAN, restored);
                }
                REENTRANT_GUST_HURT.set(true);
                try {
                    entity.hurtServer(level, source, amount * 0.50F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SYLVAN_ORBIT_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity sylAtk && sylAtk != entity) {
            sylAtk.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 5));
            sylAtk.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 30, 0));
        }

        // #45 Miasma Putrescent Mire-Carapace (Drown/Wither immune, 35% melee reflect, -2 attacker armor)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_MIASMA_CARAPACE_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_DROWNING) || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity miasmaAtk && miasmaAtk != entity) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    miasmaAtk.hurtServer(level, level.damageSources().thorns(entity), Math.max(1.5F, amount * 0.35F));
                    applyTimedAttributeShred(miasmaAtk, level, 2.0D, 0.0D, 100L);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // #46 Genesis Amniotic Shroud (absorbs 40% -> Regen II + Materia fuel, blinds & knocks back 4m) & Orbit (5m KB + 3s blind)
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_GENESIS_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_DROWNING) || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, true, true));
                if (entity instanceof Player genPlayer) {
                    long restored = Math.max(8L, Math.round(amount * 0.40F * 16.0F));
                    SpellFuelService.restorePlayerMateriaFuel(genPlayer, EssenceType.GENESIS, restored);
                }
                if (source.getDirectEntity() instanceof LivingEntity genAtk && genAtk != entity) {
                    genAtk.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    Vec3 away = genAtk.position().subtract(entity.position()).normalize().scale(1.15D);
                    genAtk.setDeltaMovement(away.x, 0.32D, away.z);
                    genAtk.hasImpulse = true;
                }
                REENTRANT_GUST_HURT.set(true);
                try {
                    entity.hurtServer(level, source, amount * 0.60F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_GENESIS_ORBIT_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity genAtk && genAtk != entity) {
            genAtk.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            Vec3 away = genAtk.position().subtract(entity.position()).normalize().scale(1.35D);
            genAtk.setDeltaMovement(away.x, 0.35D, away.z);
            genAtk.hasImpulse = true;
        }

        // #47 Oblivion Extinction Armor (Fire/Lava/Wither immunity + 40% melee reflection)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_OBLIVION_EXTINCTION_ARMOR_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FIRE) || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                entity.clearFire();
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity oblAtk && oblAtk != entity) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    oblAtk.hurtServer(level, level.damageSources().thorns(entity), Math.max(1.5F, amount * 0.40F));
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // Sec 6.49 Chimera Hybrid Flux Shroud (20% dodge) & Chimeric Flux Infusion (Monster Empowerment +20% + Flame/Frost/Shock/Poison)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_CHIMERA_SHROUD_UNTIL).orElse(0L)
                && (source.getDirectEntity() != null || source.is(DamageTypeTags.IS_PROJECTILE))
                && level.random.nextFloat() < 0.20F) {
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, entity.getX(), entity.getY(0.9D), entity.getZ(), 8, 0.3D, 0.35D, 0.3D, 0.05D);
            return EventResult.interruptFalse();
        }
        if (!REENTRANT_GUST_HURT.get()
                && entity instanceof Player chimeraHexedPlayer
                && hasActiveMutatedHex(data, now, EssenceType.CHIMERA)
                && source.getEntity() instanceof Monster chimeraMob
                && chimeraMob.distanceToSqr(chimeraHexedPlayer) <= 196.0D) {
            switch (level.random.nextInt(4)) {
                case 0 -> chimeraHexedPlayer.igniteForSeconds(3);
                case 1 -> chimeraHexedPlayer.setTicksFrozen(chimeraHexedPlayer.getTicksFrozen() + 50);
                case 2 -> chimeraHexedPlayer.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 10, 0));
                default -> chimeraHexedPlayer.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
            }
            REENTRANT_GUST_HURT.set(true);
            try {
                chimeraHexedPlayer.hurtServer(level, source, amount * (1.0F + 0.20F * getHexStackMultiplier(chimeraHexedPlayer)));
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // --- #48–#57 Apex, Prismatic, Eschaton & Singularity Shrouds, Orbits, Fumble/Recoil & Monster Empowerment ---
        EventResult apexIntercept = interceptApexEschatonSingularityDamage(level, entity, data, source, amount, now);
        if (apexIntercept.interruptsFurtherEvaluation()) {
            return apexIntercept;
        }

        // Shared +25% All-Source Damage Vulnerability (#45 Miasma & #46 Genesis puddles) & +50% Fall Vulnerability (#45/#46 Geysers)
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_FALL_VULN_50_UNTIL).orElse(0L)
                && source.is(DamageTypeTags.IS_FALL)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * 1.50F);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * 1.25F);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        long until = data.getLong(TAG_WARD_UNTIL).orElse(0L);
        if (now > until) {
            return EventResult.pass();
        }

        float shieldHp = data.getFloat(TAG_WARD_HP).orElse(0.0F);
        if (shieldHp <= 0.0F) {
            return EventResult.pass();
        }

        // Section 5.3 Combat Damage Only (Environmental Bypass):
        // Ambient lava, fire, drowning, suffocation, fall damage, and Void out-of-world bypass the Arcane Shield completely!
        boolean isCombatHit = source.getEntity() != null
                || source.getDirectEntity() != null
                || source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypeTags.IS_EXPLOSION);
        if (!isCombatHit || source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_DROWNING) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return EventResult.pass();
        }

        // Elemental counter-pulse & Section 5.3 Elemental Resistance Focus (100% absorption + 25% Materia fuel conversion!)
        String wardTypeStr = data.getString(TAG_WARD_MATERIA).orElse("AEGIS");
        EssenceType wardType = EssenceType.AEGIS;
        try {
            wardType = EssenceType.valueOf(wardTypeStr);
        } catch (IllegalArgumentException ignored) {
        }

        if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            if ("NETHER".equals(wardTypeStr) || "PYRE".equals(wardTypeStr) || "MAGMA".equals(wardTypeStr)) {
                attacker.igniteForSeconds(3);
            } else if ("FROZEN".equals(wardTypeStr) || "GLACIAL".equals(wardTypeStr) || "RIME".equals(wardTypeStr)) {
                attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
            } else if ("LIGHTNING".equals(wardTypeStr) || "STORM".equals(wardTypeStr)) {
                attacker.hurtServer(level, level.damageSources().thorns(entity), 2.5F);
            }
        }

        if (doesDamageMatchWardElement(source, wardType)) {
            if (entity instanceof Player player) {
                long restoredMfum = Math.max(4L, Math.round(amount * 0.25F * 16.0F));
                SpellFuelService.restorePlayerMateriaFuel(player, wardType, restoredMfum);
            }
            level.sendParticles(ParticleTypes.ENCHANT, entity.getX(), entity.getY(0.9D), entity.getZ(),
                    14, 0.35D, 0.45D, 0.35D, 0.1D);
            level.playSound(null, entity.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 0.75F, 1.65F);
            return EventResult.interruptFalse();
        }

        if (shieldHp >= amount) {
            float left = shieldHp - amount;
            data.putFloat(TAG_WARD_HP, left);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getY(0.9), entity.getZ(),
                    10, 0.35, 0.45, 0.35, 0.08);
            level.playSound(null, entity.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 0.75F, 1.45F);
            return EventResult.interruptFalse();
        } else {
            // Section 5.3 Shield Gate Grace Window (Zero Overflow):
            // Shattering blow consumes remaining shield HP with ZERO overflow into real HP + 0.5s (10t) reaction grace window!
            data.putFloat(TAG_WARD_HP, 0.0F);
            data.remove(TAG_WARD_UNTIL);
            entity.invulnerableTime = Math.max(entity.invulnerableTime, 10);
            level.sendParticles(ParticleTypes.CRIT, entity.getX(), entity.getY(1.0D), entity.getZ(),
                    18, 0.4D, 0.5D, 0.4D, 0.15D);
            level.playSound(null, entity.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.9F, 1.2F);
            return EventResult.interruptFalse();
        }
    }

    private static boolean doesDamageMatchWardElement(DamageSource source, EssenceType wardType) {
        if (wardType == null || wardType == EssenceType.REGULAR || wardType == EssenceType.AEGIS) {
            return false;
        }
        if (MateriaFamilyTier.isNetherLineage(wardType) && source.is(DamageTypeTags.IS_FIRE)) {
            return true;
        }
        if (isLightningLine(wardType) && source.is(DamageTypeTags.IS_LIGHTNING)) {
            return true;
        }
        if (MateriaFamilyTier.isFrozenLineage(wardType) && source.is(DamageTypeTags.IS_FREEZING)) {
            return true;
        }
        Entity attacker = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            return resolveEntityAffinities(livingAttacker).contains(wardType);
        }
        return false;
    }

    public static float getActiveWardShieldHp(LivingEntity entity) {
        CompoundTag data = EntityHelper.getPersistentData(entity);
        if (entity.level().getGameTime() > data.getLong(TAG_WARD_UNTIL).orElse(0L)) {
            return 0.0F;
        }
        return data.getFloat(TAG_WARD_HP).orElse(0.0F);
    }

    private static float getTargetMateriaResistance(LivingEntity target, @Nullable EssenceType attackType) {
        if (attackType == null || attackType == EssenceType.REGULAR) return 0.0F;
        float total = 0.0F;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armor = target.getItemBySlot(slot);
            if (armor.isEmpty()) continue;
            EssenceCombatStats stats = armor.get(ModDataComponents.ESSENCE_STATS.get());
            if (stats != null && stats.resistances() != null) {
                total += stats.resistances().getOrDefault(attackType, 0.0F);
            }
        }
        return Math.clamp(total, -0.50F, 0.75F);
    }

    private static Set<EssenceType> resolveEntityAffinities(LivingEntity entity) {
        Set<EssenceType> set = EnumSet.noneOf(EssenceType.class);
        var type = entity.getType();

        if (type.is(ModEntityTags.DROPS_UNDEAD_ESSENCE) || entity instanceof Zombie || entity instanceof Skeleton
                || entity instanceof WitherSkeleton || entity instanceof Phantom || entity instanceof WitherBoss) {
            set.add(EssenceType.UNDEAD);
        }
        if (type.is(ModEntityTags.DROPS_WATER_ESSENCE) || entity instanceof Drowned || entity instanceof Guardian
                || entity instanceof ElderGuardian || entity instanceof WaterAnimal || entity instanceof Squid) {
            set.add(EssenceType.WATER);
        }
        if (type.is(ModEntityTags.DROPS_NETHER_ESSENCE) || entity instanceof ZombifiedPiglin || entity instanceof Piglin
                || entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Ghast) {
            set.add(EssenceType.NETHER);
        }
        if (type.is(ModEntityTags.DROPS_FROZEN_ESSENCE) || entity instanceof Stray || entity instanceof PolarBear) {
            set.add(EssenceType.FROZEN);
        }
        if (type.is(ModEntityTags.DROPS_VOID_ESSENCE) || entity instanceof EnderMan || entity instanceof Endermite
                || entity instanceof Shulker || entity instanceof EnderDragon) {
            set.add(EssenceType.VOID);
        }
        if (type.is(ModEntityTags.DROPS_AIR_ESSENCE) || entity instanceof Breeze || entity instanceof AmbientCreature) {
            set.add(EssenceType.AIR);
        }
        if (type.is(ModEntityTags.DROPS_EARTH_ESSENCE) || entity instanceof Slime || entity instanceof Silverfish) {
            set.add(EssenceType.EARTH);
        }
        if (type.is(ModEntityTags.DROPS_ARID_ESSENCE) || entity instanceof Husk) {
            set.add(EssenceType.ARID);
        }
        return set;
    }

    public static boolean isBossEntity(ServerLevel level, LivingEntity entity) {
        return entity instanceof EnderDragon
                || entity instanceof WitherBoss
                || entity instanceof ElderGuardian
                || entity.getMaxHealth() >= 150.0F
                || !entity.canUsePortal(false);
    }

    private static boolean isRadiantLine(EssenceType t) {
        return t == EssenceType.RADIANT || t == EssenceType.DAWN || t == EssenceType.AEGIS
                || t == EssenceType.AURORA || t == EssenceType.MIRAGE
                || t == EssenceType.SYLVAN || t == EssenceType.GENESIS
                || t == EssenceType.CELESTIAL || t == EssenceType.EMPYREAN || t == EssenceType.APOTHEOSIS;
    }

    private static boolean isUmbralLine(EssenceType t) {
        return t == EssenceType.UMBRAL || t == EssenceType.ECLIPSE || t == EssenceType.NULL_U
                || t == EssenceType.ABYSS || t == EssenceType.PENUMBRA
                || t == EssenceType.OBLIVION || t == EssenceType.TERMINUS || t == EssenceType.ESCHATON;
    }

    private static boolean isAirLine(EssenceType t) {
        return t == EssenceType.AIR || t == EssenceType.STORM || t == EssenceType.AURA
                || t == EssenceType.DUST || t == EssenceType.RIME || t == EssenceType.STATIC;
    }

    private static boolean isWaterLine(EssenceType t) {
        return t == EssenceType.WATER || t == EssenceType.VAPOR || t == EssenceType.ABYSS
                || t == EssenceType.SPRING || t == EssenceType.MIASMA || t == EssenceType.GENESIS;
    }

    private static boolean isLightningLine(EssenceType t) {
        return t == EssenceType.LIGHTNING || t == EssenceType.STORM || t == EssenceType.STATIC || t == EssenceType.ASTRAL;
    }

    private static boolean isNatureLine(EssenceType t) {
        return t == EssenceType.NATURE || t == EssenceType.OVERGROWTH || t == EssenceType.SPORE
                || t == EssenceType.SPRING || t == EssenceType.SYLVAN || t == EssenceType.MIASMA;
    }

    private static int calculateSpellElementalAdvantageScore(EssenceType attackMateria, Set<EssenceType> defenderAffinities) {
        if (attackMateria == EssenceType.APOTHEOSIS) {
            for (EssenceType aff : defenderAffinities) {
                if (aff == EssenceType.UNDEAD || aff == EssenceType.VOID || aff == EssenceType.UMBRAL || aff == EssenceType.NETHER) {
                    return 2;
                }
            }
            return 1;
        }
        if (attackMateria == EssenceType.ESCHATON) {
            for (EssenceType aff : defenderAffinities) {
                if (aff == EssenceType.EARTH || aff == EssenceType.NATURE || aff == EssenceType.RADIANT) {
                    return 2;
                }
            }
            return 1;
        }
        if (attackMateria == EssenceType.TERMINUS) {
            for (EssenceType aff : defenderAffinities) {
                if (aff == EssenceType.NETHER || aff == EssenceType.WATER || aff == EssenceType.AIR) {
                    return 2;
                }
                if (aff == EssenceType.EARTH) {
                    return -1;
                }
            }
        }
        return VisInteractionManager.getInstance().calculateTotalVisScore(
                List.of(attackMateria.getSerializedName()),
                defenderAffinities
        );
    }

    private static EventResult interceptApexEschatonSingularityDamage(
            ServerLevel level,
            LivingEntity entity,
            CompoundTag data,
            DamageSource source,
            float amount,
            long now
    ) {
        // 1. Attacker Hex Fumble Miss (50% Celestial/Prismatic/Empyrean, 60% Apotheosis), Weapon Recoil (50% Aether/Cataclysm, 60% Eschaton/Entropica) & Entropic Shroud Lifesteal
        if (!REENTRANT_GUST_HURT.get() && source.getEntity() instanceof LivingEntity atk && atk != entity) {
            CompoundTag atkData = EntityHelper.getPersistentData(atk);
            float fumbleChance = 0.0F;
            if (hasActiveMutatedHex(atkData, now, EssenceType.APOTHEOSIS)) {
                fumbleChance = 0.60F;
            } else if (hasActiveMutatedHex(atkData, now, EssenceType.CELESTIAL)
                    || hasActiveMutatedHex(atkData, now, EssenceType.PRISMATIC)
                    || hasActiveMutatedHex(atkData, now, EssenceType.EMPYREAN)) {
                fumbleChance = 0.50F;
            }
            if (fumbleChance > 0.0F && level.random.nextFloat() < fumbleChance) {
                level.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY(0.9D), entity.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.03D);
                return EventResult.interruptFalse();
            }

            float recoilFraction = 0.0F;
            if (hasActiveMutatedHex(atkData, now, EssenceType.ESCHATON) || hasActiveMutatedHex(atkData, now, EssenceType.ENTROPICA)) {
                recoilFraction = 0.60F;
            } else if (hasActiveMutatedHex(atkData, now, EssenceType.AETHER) || hasActiveMutatedHex(atkData, now, EssenceType.CATACLYSM)) {
                recoilFraction = 0.50F;
            }
            if (recoilFraction > 0.0F) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    atk.hurtServer(level, level.damageSources().magic(), Math.max(1.0F, amount * recoilFraction));
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }

            if (now <= atkData.getLong(SpellLingeringZoneManager.TAG_ENTROPIC_LIFESTEAL_SHROUD_UNTIL).orElse(0L)) {
                float healAmt = Math.max(0.5F, amount * 0.25F);
                notifyMagicalHeal(atk, healAmt);
                atk.heal(healAmt);
            }
        }

        // 2. #48 Aether Shroud/Aegis (Fall, Drowning, Suffocation Immunity)
        if ((now <= data.getLong(SpellLingeringZoneManager.TAG_AETHER_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_AETHER_AEGIS_UNTIL).orElse(0L))
                && (source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypeTags.IS_DROWNING)
                || source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL))) {
            entity.resetFallDistance();
            return EventResult.interruptFalse();
        }

        // 3. #49 Entropic Shroud & Lifesteal Shroud (Wither Immunity + 40% Melee Reflection)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ENTROPIC_SHROUD_UNTIL).orElse(0L)
                && source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
            return EventResult.interruptFalse();
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ENTROPIC_LIFESTEAL_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity entAtk && entAtk != entity) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    entAtk.hurtServer(level, level.damageSources().thorns(entity), Math.max(1.5F, amount * 0.40F));
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // 4. #50 Celestial Shroud, Ascendance & Orbit
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity celAtk && celAtk != entity) {
                celAtk.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                Vec3 away = celAtk.position().subtract(entity.position()).normalize().scale(1.1D);
                celAtk.setDeltaMovement(away.x, 0.35D, away.z);
                celAtk.hasImpulse = true;
            }
        } else if (now <= data.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_ASCENDANCE_UNTIL).orElse(0L)) {
            if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity celAtk && celAtk != entity) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    celAtk.hurtServer(level, level.damageSources().magic(), Math.max(2.0F, amount * 0.40F));
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_ORBIT_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity celAtk && celAtk != entity) {
            celAtk.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        }

        // 5. #51 Prismatic Shroud (Fire/Lava/Drowning Immunity)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_PRISMATIC_SHROUD_UNTIL).orElse(0L)
                && (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_DROWNING))) {
            entity.clearFire();
            return EventResult.interruptFalse();
        }

        // 6. #52 Empyrean Shroud, Aegis & Orbit (Suffocation/Drowning/Wither Immunity + 2s Orbit Root)
        if ((now <= data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_AEGIS_UNTIL).orElse(0L))
                && (source.is(DamageTypeTags.IS_DROWNING)
                || source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER))) {
            return EventResult.interruptFalse();
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_ORBIT_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity empAtk && empAtk != entity) {
            empAtk.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 5));
            empAtk.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
        }

        // 7. #53 Cataclysm Shroud (Fire/Lava/Wither Immunity)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_CATACLYSM_SHROUD_UNTIL).orElse(0L)
                && (source.is(DamageTypeTags.IS_FIRE) || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER))) {
            entity.clearFire();
            return EventResult.interruptFalse();
        }

        // 8. #54 Terminus Shroud & Mantle (50% Absorption + 2s Melee Freeze & -60% Attack Speed)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_DROWNING)
                    || source.is(DamageTypeTags.IS_FREEZING) || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                entity.clearFire();
                entity.setTicksFrozen(0);
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity termAtk && termAtk != entity) {
                termAtk.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                applyTimedAttackDebuff(termAtk, level, 0.0D, 0.60D, 80L);
            }
            if (!REENTRANT_GUST_HURT.get() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    entity.hurtServer(level, source, amount * 0.50F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
        } else if (now <= data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_MANTLE_UNTIL).orElse(0L)
                && (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_DROWNING) || source.is(DamageTypeTags.IS_FREEZING))) {
            entity.clearFire();
            entity.setTicksFrozen(0);
            return EventResult.interruptFalse();
        }

        // 9. #55 Eschaton, #56 Apotheosis & #57 Entropica Singularity Mantles (Environmental & Void Immunity)
        if ((now <= data.getLong(SpellLingeringZoneManager.TAG_ESCHATON_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL).orElse(0L))
                && (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_DROWNING)
                || source.is(DamageTypeTags.IS_FREEZING)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)
                || source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD))) {
            entity.clearFire();
            entity.setTicksFrozen(0);
            entity.resetFallDistance();
            return EventResult.interruptFalse();
        }

        // 10. Player Hex Vectors (Monster Empowerment: #48 Aether, #54 Terminus, #56 Apotheosis, #57 Entropica)
        if (!REENTRANT_GUST_HURT.get() && entity instanceof Player hexedPlayer && source.getEntity() instanceof Monster mob) {
            if (hasActiveMutatedHex(data, now, EssenceType.AETHER)
                    && (!mob.onGround() || mob.getDeltaMovement().horizontalDistanceSqr() > 0.015D)) {
                data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 40L);
                REENTRANT_GUST_HURT.set(true);
                try {
                    hexedPlayer.hurtServer(level, source, amount * (1.0F + 0.50F * getHexStackMultiplier(hexedPlayer)));
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
            if (hasActiveMutatedHex(data, now, EssenceType.TERMINUS)) {
                hexedPlayer.setTicksFrozen(Math.min(hexedPlayer.getTicksRequiredToFreeze() + 80, hexedPlayer.getTicksFrozen() + 80));
                hexedPlayer.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                REENTRANT_GUST_HURT.set(true);
                try {
                    hexedPlayer.hurtServer(level, source, amount * (1.0F + 0.40F * getHexStackMultiplier(hexedPlayer)));
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
            if (hasActiveMutatedHex(data, now, EssenceType.APOTHEOSIS)) {
                applyTimedAttackSpeedBuff(mob, level, 1.50D, 80L);
                level.sendParticles(ParticleTypes.CRIT, hexedPlayer.getX(), hexedPlayer.getY(0.85D), hexedPlayer.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.1D);
                REENTRANT_GUST_HURT.set(true);
                try {
                    hexedPlayer.hurtServer(level, source, amount * 1.50F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
            if (hasActiveMutatedHex(data, now, EssenceType.ENTROPICA)) {
                applyTimedAttackSpeedBuff(mob, level, 1.00D, 80L);
                if (level.random.nextFloat() < 0.50F) {
                    level.sendParticles(ParticleTypes.CRIT, hexedPlayer.getX(), hexedPlayer.getY(0.85D), hexedPlayer.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.1D);
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        hexedPlayer.hurtServer(level, source, amount * 1.50F);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }
        return EventResult.pass();
    }

    private static boolean isNecroticLine(EssenceType t) {
        return t == EssenceType.UNDEAD || t == EssenceType.BLIGHT || t == EssenceType.MIASMA
                || t == EssenceType.WRAITH || t == EssenceType.BARROW || t == EssenceType.OBLIVION;
    }
}
