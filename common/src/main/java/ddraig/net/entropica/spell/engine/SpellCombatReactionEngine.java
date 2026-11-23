package ddraig.net.entropica.spell.engine;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.combat.VisInteractionManager;
import ddraig.net.entropica.component.EssenceCombatStats;
import ddraig.net.entropica.component.VisWeaponState;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModEntityTags;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SpellFlawProfile;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.MateriaFamilyTier;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.mutation.handler.SpellMutationHandlerRegistry;
import ddraig.net.entropica.spell.vfx.SpellWardClientManager;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import dev.architectury.event.EventResult;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
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
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
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
    public static final long BOSS_HEX_TENACITY_CAP_TICKS = 600L;
    public static final long BOSS_HEX_ACTIVE_TENACITY_CAP_TICKS = 300L;
    public static final long BOSS_HEX_POST_IMMUNITY_TICKS = 200L;

    private static final String TAG_LAST_SPELL_HIT_TICK = "LastSpellHitTick";
    private static final String TAG_ACTIVE_ELEMENT = "ActiveSpellElement";
    private static final String TAG_ACTIVE_ELEMENT_UNTIL = "ActiveSpellElementUntil";
    private static final String TAG_ARMOR_SHRED = "SpellArmorShredPoints";
    private static final String TAG_ARMOR_SHRED_UNTIL = "SpellArmorShredUntil";
    private static final String TAG_ATTR_SHRED_UNTIL = "SpellAttrShredUntil";
    private static final String TAG_ATTR_OFFENSE_SHRED_UNTIL = "SpellAttrOffenseShredUntil";
    private static final String TAG_ATTR_DEF_BUFF_UNTIL = "SpellAttrDefBuffUntil";

    public static final String TAG_HEX_TYPE = "SpellHexType";
    public static final String TAG_HEX_TIER = "SpellHexTier";
    public static final String TAG_HEX_STACK = "SpellHexStackLevel";
    public static final String TAG_HEX_MUTATED = "SpellHexMutated";
    public static final String TAG_HEX_UNTIL = "SpellHexUntil";
    public static final String TAG_HEX_APPLIED_TICK = "SpellHexAppliedTick";
    public static final String TAG_HEX_INITIAL_WINDOW = "SpellHexInitialWindowUntil";
    public static final String TAG_HEX_BOSS_IMMUNE_UNTIL = "SpellHexBossImmuneUntil";
    public static final String TAG_HEX_BOSS_IMMUNE_TYPE = "SpellHexBossImmuneType";
    public static final String TAG_HEX_BOSS_IMMUNE_PREFIX = "SpellHexBossImmuneUntil_";
    public static final String TAG_HEX_CASTER_UUID = "SpellHexCasterUuid";
    private static final String TAG_HEX_MAX_ALLOWED_HP = "SpellHexMaxAllowedHp";

    private static final String TAG_WARD_HP = "SpellWardShieldHp";
    private static final String TAG_WARD_MAX_HP = "SpellWardMaxHp";
    private static final String TAG_WARD_UNTIL = "SpellWardUntil";
    private static final String TAG_WARD_MATERIA = "SpellWardMateria";
    private static final String TAG_WARD_TIER = "SpellWardTier";

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
    private static final ResourceLocation MOD_MOVE_SPEED_SHRED_PCT =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_move_speed_shred_pct");
    private static final ResourceLocation MOD_ATTACK_SPEED_BUFF_PCT =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_attack_speed_buff_pct");
    private static final ResourceLocation MOD_MOVE_SPEED_BUFF_PCT =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_move_speed_buff_pct");
    private static final ResourceLocation MOD_HEX_MAX_HEALTH_CUT_PCT =
            ResourceLocation.fromNamespaceAndPath("entropica", "spell_hex_max_health_cut_pct");
    public static final String TAG_AIR_HYPOXIA_MELEE_CD_UNTIL = "SpellAirHypoxiaMeleeCdUntil";

    private static final ThreadLocal<Boolean> REENTRANT_GUST_HURT = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> REENTRANT_ELEMENTAL_REACTION = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> IN_ADVANTAGE_HURT = ThreadLocal.withInitial(() -> false);

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

    /**
     * Applies an exact transient percentage movement speed sap to {@link Attributes#MOVEMENT_SPEED}
     * (e.g., Section 6.10 <i>Spectral Soul Swarm</i> {@code -40%} and <i>Creeping Tomb-Dust</i> {@code -50%}).
     */
    public static void applyTimedMovementSpeedSap(
            LivingEntity target,
            ServerLevel level,
            double speedReductionFraction,
            long durationTicks
    ) {
        if (speedReductionFraction <= 0.0D) return;
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        data.putLong(TAG_ATTR_OFFENSE_SHRED_UNTIL,
                Math.max(now + Math.max(15L, durationTicks), data.getLong(TAG_ATTR_OFFENSE_SHRED_UNTIL).orElse(0L)));
        AttributeInstance moveSpd = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpd != null) {
            double clamped = Math.clamp(speedReductionFraction, 0.05D, 0.90D);
            AttributeModifier existing = moveSpd.getModifier(MOD_MOVE_SPEED_SHRED_PCT);
            if (existing != null && Math.abs(existing.amount()) > clamped) {
                clamped = Math.abs(existing.amount());
            }
            moveSpd.removeModifier(MOD_MOVE_SPEED_SHRED_PCT);
            moveSpd.addTransientModifier(new AttributeModifier(
                    MOD_MOVE_SPEED_SHRED_PCT,
                    -clamped,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
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
     * Applies an exact transient percentage movement speed boost to {@link Attributes#MOVEMENT_SPEED}
     * (e.g., Section 6.19 <i>Mycelial Spore-Cloud Mantle</i> {@code +30%} when walking on Mycelium).
     */
    public static void applyTimedMovementSpeedBoost(
            LivingEntity target,
            ServerLevel level,
            double speedBonusFraction,
            long durationTicks
    ) {
        if (speedBonusFraction <= 0.0D) return;
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        data.putLong(TAG_ATTR_DEF_BUFF_UNTIL,
                Math.max(now + Math.max(10L, durationTicks), data.getLong(TAG_ATTR_DEF_BUFF_UNTIL).orElse(0L)));
        AttributeInstance spd = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) {
            spd.removeModifier(MOD_MOVE_SPEED_BUFF_PCT);
            spd.addTransientModifier(new AttributeModifier(
                    MOD_MOVE_SPEED_BUFF_PCT,
                    Math.clamp(speedBonusFraction, 0.05D, 1.50D),
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    public static void clearMovementSpeedBoost(LivingEntity target) {
        AttributeInstance spd = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) {
            spd.removeModifier(MOD_MOVE_SPEED_BUFF_PCT);
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

    /**
     * Restores Materia fuel proportional to the actual combat damage dealt relative to the tier's
     * canonical baseline combat damage (Section 6.24 Aura siphons: 15% Chain, 20% Beam/Agent, 30% Palm).
     */
    public static long siphonCasterMateriaFuelFromDamage(
            Player player,
            @Nullable EssenceType preferredType,
            int tier,
            float actualDamage,
            float siphonFraction
    ) {
        if (!player.isAlive() || siphonFraction <= 0.0F || actualDamage <= 0.0F) return 0L;
        float baseHp = Math.max(1.0F, MateriaStageTable.getBaseCombatDamageHp(tier));
        float dmgRatio = Math.clamp(actualDamage / baseHp, 0.25F, 2.50F);
        long baseCost = MateriaStageTable.getBaseMfumCastCost(tier);
        long restored = Math.max(8L, Math.round(baseCost * siphonFraction * dmgRatio));
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
        data.remove(TAG_ARMOR_SHRED);
        data.remove(TAG_ARMOR_SHRED_UNTIL);
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
        AttributeInstance moveSpd = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpd != null) {
            moveSpd.removeModifier(MOD_MOVE_SPEED_SHRED_PCT);
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
            spd.removeModifier(MOD_MOVE_SPEED_BUFF_PCT);
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
        return applyElementalHarm(
                level,
                caster,
                target,
                spell,
                !spell.subSpells().isEmpty() ? spell.subSpells().getFirst() : null,
                EssenceType.REGULAR,
                effectMateria,
                effectMutation,
                apparatusPotencyMult
        );
    }

    public static float applyElementalHarm(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            SpellAssembly spell,
            @Nullable SubSpellSlot activeSlot,
            @Nullable EssenceType fallbackMateria,
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

        // 2. Materia Advantage + Mutated Flow Advantage Bonus + Touch Weapon Materia Advantage
        EssenceType safeFallback = fallbackMateria != null ? fallbackMateria : EssenceType.REGULAR;
        EssenceType flowMateria = spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), safeFallback);
        MateriaPartMutationHandler flowHandler = SpellMutationHandlerRegistry.resolveActive(flowMateria, spell.flowPart().mutationEnabled());
        float flowBonusAdv;
        if (flowMateria == EssenceType.WATER) {
            flowBonusAdv = (spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY
                    && spell.flowPart().mutationEnabled()
                    && (target.isOnFire()
                        || ddraig.net.entropica.spell.mutation.element.WaterSpellMutationHandler.isTrueNetherOrFireEntity(target)))
                    ? 0.6F
                    : 0.0F;
        } else {
            flowBonusAdv = (flowHandler != null
                    && ddraig.net.entropica.spell.mutation.SpellMutationRegistry.isHandlerFlowMutationApplicable(
                            flowMateria, spell.flowPart().flow()))
                    ? flowHandler.getFlowBonusAdvantageScore(spell.flowPart().flow(), target)
                    : 0.0F;
        }

        SubSpellSlot resolvedSlot = activeSlot != null ? activeSlot : (!spell.subSpells().isEmpty() ? spell.subSpells().getFirst() : null);
        float weaponBonusAdv = 0.0F;
        boolean earthSeismicStrike = false;
        boolean frozenFlashFreezeStrike = false;
        boolean netherCauterizingStrike = false;
        boolean radiantHolySmiteStrike = false;
        boolean umbralCloakingStrike = false;
        boolean undeadVampiricStrike = false;
        boolean voidDestabilizeStrike = false;
        boolean aridMummifyStrike = false;
        boolean lightningOverchargeStrike = false;
        boolean magmaVolcanicStrike = false;
        boolean stormSquallStrike = false;
        boolean glacialBrittleStrike = false;
        boolean overgrowthBotanicalStrike = false;
        boolean dustSmotherStrike = false;
        boolean sporeInfestationStrike = false;
        boolean taigaHeatSiphonStrike = false;
        boolean oasisBlessingStrike = false;
        boolean oasisProjectileLifesteal = false;
        boolean oasisBeamLifesteal = false;
        boolean vitaeOverchargeStrike = false;
        boolean vitaeBeamLifesteal = false;
        boolean bloodExsanguinationStrike = false;
        float bloodFormLifesteal = 0.0F;
        boolean auraChakraPalmStrike = false;
        float auraFormFuelSiphon = 0.0F;
        boolean amberFossilizingStrike = false;
        boolean eclipseSiphonPalmStrike = false;
        boolean blightThornGougeStrike = false;
        boolean astralOverchargeStrike = false;
        boolean soulfireCleaveStrike = false;
        float soulfireFormLifesteal = 0.0F;
        float soulfireBeamFuelSiphon = 0.0F;
        boolean vaporHydroGougeStrike = false;
        boolean nullREventHorizonStrike = false;
        boolean nullUOblivionPalmStrike = false;
        float nullUPalmLifesteal = 0.0F;
        boolean dawnAeroSolarCleaveStrike = false;
        float dawnBeamFuelSiphon = 0.0F;
        boolean abyssDrowningCleaveStrike = false;
        boolean aegisShieldBashStrike = false;
        boolean auroraPolarCleaveStrike = false;
        boolean mirageParanoiaStrike = false;
        boolean wraithPhasingPalmStrike = false;
        boolean barrowTombstoneSlamStrike = false;
        boolean pyreTimberCleaveStrike = false;
        boolean penumbraEclipseCleaveStrike = false;
        boolean rimePermafrostCleaveStrike = false;
        boolean springBlossomCleaveStrike = false;
        boolean staticPalmOverloadStrike = false;
        boolean sylvanCanopyCleaveStrike = false;
        boolean miasmaPalmGougeStrike = false;
        boolean genesisInceptionPalmStrike = false;
        boolean oblivionExtinctionPalmStrike = false;
        boolean aetherFirmamentSunderStrike = false;
        boolean entropicSingularityCleaveStrike = false;
        boolean celestialSupernovaCleaveStrike = false;
        boolean empyreanRootCleaveStrike = false;
        boolean cataclysmCalamitySunderStrike = false;
        boolean terminusGlacierCleaveStrike = false;
        boolean eschatonRealityScytheStrike = false;
        boolean apotheosisGenesisCleaveStrike = false;
        boolean entropicaGrandParadoxStrike = false;
        boolean undeadBeamSiphon = false;
        if (resolvedSlot != null && resolvedSlot.formPart().form() == ddraig.net.entropica.spell.SpellDeliveryForm.TOUCH_STRIKE) {
            if (caster != null) {
                weaponBonusAdv = resolveHeldWeaponAdvantageScore(caster, target);
            }
            if (resolvedSlot.formPart().mutationEnabled()) {
                EssenceType touchFormMat = resolvedSlot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), safeFallback);
                effectMateria = resolveConvertedTouchAttackMateria(effectMateria, touchFormMat, true);
                if (touchFormMat == EssenceType.EARTH) {
                    earthSeismicStrike = true;
                } else if (touchFormMat == EssenceType.FROZEN) {
                    frozenFlashFreezeStrike = true;
                } else if (touchFormMat == EssenceType.NETHER) {
                    netherCauterizingStrike = true;
                } else if (touchFormMat == EssenceType.RADIANT) {
                    radiantHolySmiteStrike = true;
                } else if (touchFormMat == EssenceType.UMBRAL) {
                    umbralCloakingStrike = true;
                } else if (touchFormMat == EssenceType.UNDEAD) {
                    undeadVampiricStrike = true;
                } else if (touchFormMat == EssenceType.VOID) {
                    voidDestabilizeStrike = true;
                } else if (touchFormMat == EssenceType.ARID) {
                    aridMummifyStrike = true;
                } else if (touchFormMat == EssenceType.LIGHTNING) {
                    lightningOverchargeStrike = true;
                } else if (touchFormMat == EssenceType.MAGMA) {
                    magmaVolcanicStrike = true;
                } else if (touchFormMat == EssenceType.STORM) {
                    stormSquallStrike = true;
                } else if (touchFormMat == EssenceType.GLACIAL) {
                    glacialBrittleStrike = true;
                } else if (touchFormMat == EssenceType.OVERGROWTH) {
                    overgrowthBotanicalStrike = true;
                } else if (touchFormMat == EssenceType.DUST) {
                    dustSmotherStrike = true;
                } else if (touchFormMat == EssenceType.SPORE) {
                    sporeInfestationStrike = true;
                } else if (touchFormMat == EssenceType.TAIGA) {
                    taigaHeatSiphonStrike = true;
                } else if (touchFormMat == EssenceType.OASIS) {
                    oasisBlessingStrike = true;
                } else if (touchFormMat == EssenceType.VITAE) {
                    vitaeOverchargeStrike = true;
                } else if (touchFormMat == EssenceType.BLOOD) {
                    bloodExsanguinationStrike = true;
                    bloodFormLifesteal = 0.40F;
                } else if (touchFormMat == EssenceType.AURA) {
                    auraChakraPalmStrike = true;
                    auraFormFuelSiphon = 0.30F;
                } else if (touchFormMat == EssenceType.AMBER) {
                    amberFossilizingStrike = true;
                } else if (touchFormMat == EssenceType.ECLIPSE) {
                    eclipseSiphonPalmStrike = true;
                } else if (touchFormMat == EssenceType.BLIGHT) {
                    blightThornGougeStrike = true;
                } else if (touchFormMat == EssenceType.ASTRAL) {
                    astralOverchargeStrike = true;
                } else if (touchFormMat == EssenceType.SOULFIRE) {
                    soulfireCleaveStrike = true;
                } else if (touchFormMat == EssenceType.VAPOR) {
                    vaporHydroGougeStrike = true;
                } else if (touchFormMat == EssenceType.NULL_R) {
                    nullREventHorizonStrike = true;
                } else if (touchFormMat == EssenceType.NULL_U) {
                    nullUOblivionPalmStrike = true;
                    nullUPalmLifesteal = 0.25F;
                } else if (touchFormMat == EssenceType.DAWN) {
                    dawnAeroSolarCleaveStrike = true;
                } else if (touchFormMat == EssenceType.ABYSS) {
                    abyssDrowningCleaveStrike = true;
                } else if (touchFormMat == EssenceType.AEGIS) {
                    aegisShieldBashStrike = true;
                } else if (touchFormMat == EssenceType.AURORA) {
                    auroraPolarCleaveStrike = true;
                } else if (touchFormMat == EssenceType.MIRAGE) {
                    mirageParanoiaStrike = true;
                } else if (touchFormMat == EssenceType.WRAITH) {
                    wraithPhasingPalmStrike = true;
                } else if (touchFormMat == EssenceType.BARROW) {
                    barrowTombstoneSlamStrike = true;
                } else if (touchFormMat == EssenceType.PYRE) {
                    pyreTimberCleaveStrike = true;
                } else if (touchFormMat == EssenceType.PENUMBRA) {
                    penumbraEclipseCleaveStrike = true;
                } else if (touchFormMat == EssenceType.RIME) {
                    rimePermafrostCleaveStrike = true;
                } else if (touchFormMat == EssenceType.SPRING) {
                    springBlossomCleaveStrike = true;
                } else if (touchFormMat == EssenceType.STATIC) {
                    staticPalmOverloadStrike = true;
                } else if (touchFormMat == EssenceType.SYLVAN) {
                    sylvanCanopyCleaveStrike = true;
                } else if (touchFormMat == EssenceType.MIASMA) {
                    miasmaPalmGougeStrike = true;
                } else if (touchFormMat == EssenceType.GENESIS) {
                    genesisInceptionPalmStrike = true;
                } else if (touchFormMat == EssenceType.OBLIVION) {
                    oblivionExtinctionPalmStrike = true;
                } else if (touchFormMat == EssenceType.AETHER) {
                    aetherFirmamentSunderStrike = true;
                } else if (touchFormMat == EssenceType.ENTROPIC) {
                    entropicSingularityCleaveStrike = true;
                    bloodFormLifesteal = Math.max(bloodFormLifesteal, 0.35F);
                } else if (touchFormMat == EssenceType.CELESTIAL) {
                    celestialSupernovaCleaveStrike = true;
                    bloodFormLifesteal = Math.max(bloodFormLifesteal, 0.35F);
                } else if (touchFormMat == EssenceType.EMPYREAN) {
                    empyreanRootCleaveStrike = true;
                } else if (touchFormMat == EssenceType.CATACLYSM) {
                    cataclysmCalamitySunderStrike = true;
                } else if (touchFormMat == EssenceType.TERMINUS) {
                    terminusGlacierCleaveStrike = true;
                } else if (touchFormMat == EssenceType.ESCHATON) {
                    eschatonRealityScytheStrike = true;
                } else if (touchFormMat == EssenceType.APOTHEOSIS) {
                    apotheosisGenesisCleaveStrike = true;
                } else if (touchFormMat == EssenceType.ENTROPICA) {
                    entropicaGrandParadoxStrike = true;
                }
            }
        } else if (resolvedSlot != null
                && resolvedSlot.formPart().form() == ddraig.net.entropica.spell.SpellDeliveryForm.BEAM
                && resolvedSlot.formPart().mutationEnabled()) {
            EssenceType beamFormMat = resolvedSlot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), safeFallback);
            if (beamFormMat == EssenceType.UNDEAD) {
                undeadBeamSiphon = true;
            } else if (beamFormMat == EssenceType.OASIS) {
                oasisBeamLifesteal = true;
            } else if (beamFormMat == EssenceType.VITAE) {
                vitaeBeamLifesteal = true;
            } else if (beamFormMat == EssenceType.BLOOD) {
                bloodFormLifesteal = 0.35F;
            } else if (beamFormMat == EssenceType.AURA) {
                auraFormFuelSiphon = 0.20F;
            } else if (beamFormMat == EssenceType.DAWN) {
                dawnBeamFuelSiphon = 0.20F;
            }
        } else if (resolvedSlot != null
                && resolvedSlot.formPart().form() == ddraig.net.entropica.spell.SpellDeliveryForm.PROJECTILE
                && resolvedSlot.formPart().mutationEnabled()
                && resolvedSlot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), safeFallback) == EssenceType.OASIS) {
            oasisProjectileLifesteal = true;
        }
        if (resolvedSlot != null && resolvedSlot.formPart().mutationEnabled()
                && resolvedSlot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), safeFallback) == EssenceType.BLOOD) {
            bloodFormLifesteal = switch (resolvedSlot.formPart().form()) {
                case PROJECTILE, GLYPH -> 0.30F;
                case BEAM -> 0.35F;
                case TOUCH_STRIKE -> 0.40F;
                case CONE -> 0.20F;
                case SKYFALL -> 0.15F;
                default -> 0.0F;
            };
        }
        if (resolvedSlot != null && resolvedSlot.formPart().mutationEnabled()
                && resolvedSlot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), safeFallback) == EssenceType.SOULFIRE) {
            soulfireFormLifesteal = switch (resolvedSlot.formPart().form()) {
                case PROJECTILE, BEAM, GLYPH -> 0.20F;
                case TOUCH_STRIKE -> 0.25F;
                case RADIAL_BURST, CONE, SKYFALL -> 0.15F;
                default -> 0.0F;
            };
            if (resolvedSlot.formPart().form() == ddraig.net.entropica.spell.SpellDeliveryForm.BEAM) {
                soulfireBeamFuelSiphon = 0.20F;
            }
        }
        if (spell.flowPart().flow() == SpellFlowModifier.CHAIN_LEAP
                && spell.flowPart().mutationEnabled()
                && flowMateria == EssenceType.BLOOD) {
            bloodFormLifesteal = Math.max(bloodFormLifesteal, 0.20F);
        } else if (spell.flowPart().flow() == SpellFlowModifier.CHAIN_LEAP
                && spell.flowPart().mutationEnabled()
                && flowMateria == EssenceType.AURA) {
            auraFormFuelSiphon = Math.max(auraFormFuelSiphon, 0.15F);
        } else if (spell.flowPart().flow() == SpellFlowModifier.CHAIN_LEAP
                && spell.flowPart().mutationEnabled()
                && flowMateria == EssenceType.SOULFIRE) {
            soulfireFormLifesteal = Math.max(soulfireFormLifesteal, 0.15F);
        }

        Set<EssenceType> defenderAffinities = resolveEntityAffinities(target);
        float seismicEarthAdv = (earthSeismicStrike && effectMateria != EssenceType.EARTH && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.EARTH, defenderAffinities)
                : 0.0F;
        float flashFrozenAdv = (frozenFlashFreezeStrike && effectMateria != EssenceType.FROZEN && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.FROZEN, defenderAffinities)
                : 0.0F;
        float cauterizingNetherAdv = (netherCauterizingStrike && effectMateria != EssenceType.NETHER && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.NETHER, defenderAffinities)
                : 0.0F;
        float holySmiteRadiantAdv = (radiantHolySmiteStrike && effectMateria != EssenceType.RADIANT && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.RADIANT, defenderAffinities)
                : 0.0F;
        float cloakingUmbralAdv = (umbralCloakingStrike && effectMateria != EssenceType.UMBRAL && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.UMBRAL, defenderAffinities)
                : 0.0F;
        float vampiricUndeadAdv = (undeadVampiricStrike && effectMateria != EssenceType.UNDEAD && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.UNDEAD, defenderAffinities)
                : 0.0F;
        float destabilizeVoidAdv = (voidDestabilizeStrike && effectMateria != EssenceType.VOID && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.VOID, defenderAffinities)
                : 0.0F;
        float mummifyAridAdv = (aridMummifyStrike && effectMateria != EssenceType.ARID && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.ARID, defenderAffinities)
                : 0.0F;
        float overchargeLightningAdv = (lightningOverchargeStrike && effectMateria != EssenceType.LIGHTNING && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.LIGHTNING, defenderAffinities)
                : 0.0F;
        float volcanicMagmaAdv = (magmaVolcanicStrike && effectMateria != EssenceType.MAGMA && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.MAGMA, defenderAffinities)
                : 0.0F;
        float squallStormAdv = (stormSquallStrike && effectMateria != EssenceType.STORM && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.STORM, defenderAffinities)
                : 0.0F;
        float brittleGlacialAdv = (glacialBrittleStrike && effectMateria != EssenceType.GLACIAL && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.GLACIAL, defenderAffinities)
                : 0.0F;
        float botanicalOvergrowthAdv = (overgrowthBotanicalStrike && effectMateria != EssenceType.OVERGROWTH && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.OVERGROWTH, defenderAffinities)
                : 0.0F;
        float smotherDustAdv = (dustSmotherStrike && effectMateria != EssenceType.DUST && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.DUST, defenderAffinities)
                : 0.0F;
        float infestationSporeAdv = (sporeInfestationStrike && effectMateria != EssenceType.SPORE && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.SPORE, defenderAffinities)
                : 0.0F;
        float heatSiphonTaigaAdv = (taigaHeatSiphonStrike && effectMateria != EssenceType.TAIGA && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.TAIGA, defenderAffinities)
                : 0.0F;
        float blessingOasisAdv = (oasisBlessingStrike && effectMateria != EssenceType.OASIS && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.OASIS, defenderAffinities)
                : 0.0F;
        float overchargeVitaeAdv = (vitaeOverchargeStrike && effectMateria != EssenceType.VITAE && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.VITAE, defenderAffinities)
                : 0.0F;
        float exsanguinationBloodAdv = (bloodExsanguinationStrike && effectMateria != EssenceType.BLOOD && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.BLOOD, defenderAffinities)
                : 0.0F;
        float chakraPalmAuraAdv = (auraChakraPalmStrike && effectMateria != EssenceType.AURA && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.AURA, defenderAffinities)
                : 0.0F;
        float fossilizingAmberAdv = (amberFossilizingStrike && effectMateria != EssenceType.AMBER && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.AMBER, defenderAffinities)
                : 0.0F;
        float siphonPalmEclipseAdv = (eclipseSiphonPalmStrike && effectMateria != EssenceType.ECLIPSE && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.ECLIPSE, defenderAffinities)
                : 0.0F;
        float thornGougeBlightAdv = (blightThornGougeStrike && effectMateria != EssenceType.BLIGHT && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.BLIGHT, defenderAffinities)
                : 0.0F;
        float overchargeAstralAdv = (astralOverchargeStrike && effectMateria != EssenceType.ASTRAL && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.ASTRAL, defenderAffinities)
                : 0.0F;
        float cleaveSoulfireAdv = (soulfireCleaveStrike && effectMateria != EssenceType.SOULFIRE && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.SOULFIRE, defenderAffinities)
                : 0.0F;
        float hydroGougeVaporAdv = (vaporHydroGougeStrike && effectMateria != EssenceType.VAPOR && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.VAPOR, defenderAffinities)
                : 0.0F;
        float eventHorizonNullRAdv = (nullREventHorizonStrike && effectMateria != EssenceType.NULL_R && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.NULL_R, defenderAffinities)
                : 0.0F;
        float oblivionPalmNullUAdv = (nullUOblivionPalmStrike && effectMateria != EssenceType.NULL_U && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.NULL_U, defenderAffinities)
                : 0.0F;
        float aeroSolarDawnAdv = (dawnAeroSolarCleaveStrike && effectMateria != EssenceType.DAWN && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.DAWN, defenderAffinities)
                : 0.0F;
        float drowningCleaveAbyssAdv = (abyssDrowningCleaveStrike && effectMateria != EssenceType.ABYSS && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.ABYSS, defenderAffinities)
                : 0.0F;
        float shieldBashAegisAdv = (aegisShieldBashStrike && effectMateria != EssenceType.AEGIS && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.AEGIS, defenderAffinities)
                : 0.0F;
        float polarCleaveAuroraAdv = (auroraPolarCleaveStrike && effectMateria != EssenceType.AURORA && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.AURORA, defenderAffinities)
                : 0.0F;
        float paranoiaMirageAdv = (mirageParanoiaStrike && effectMateria != EssenceType.MIRAGE && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.MIRAGE, defenderAffinities)
                : 0.0F;
        float phasingPalmWraithAdv = (wraithPhasingPalmStrike && effectMateria != EssenceType.WRAITH && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.WRAITH, defenderAffinities)
                : 0.0F;
        float tombstoneSlamBarrowAdv = (barrowTombstoneSlamStrike && effectMateria != EssenceType.BARROW && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.BARROW, defenderAffinities)
                : 0.0F;
        float timberCleavePyreAdv = (pyreTimberCleaveStrike && effectMateria != EssenceType.PYRE && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.PYRE, defenderAffinities)
                : 0.0F;
        float eclipseCleavePenumbraAdv = (penumbraEclipseCleaveStrike && effectMateria != EssenceType.PENUMBRA && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.PENUMBRA, defenderAffinities)
                : 0.0F;
        float permafrostCleaveRimeAdv = (rimePermafrostCleaveStrike && effectMateria != EssenceType.RIME && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.RIME, defenderAffinities)
                : 0.0F;
        float blossomCleaveSpringAdv = (springBlossomCleaveStrike && effectMateria != EssenceType.SPRING && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.SPRING, defenderAffinities)
                : 0.0F;
        float palmOverloadStaticAdv = (staticPalmOverloadStrike && effectMateria != EssenceType.STATIC && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.STATIC, defenderAffinities)
                : 0.0F;
        float canopyCleaveSylvanAdv = (sylvanCanopyCleaveStrike && effectMateria != EssenceType.SYLVAN && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.SYLVAN, defenderAffinities)
                : 0.0F;
        float palmGougeMiasmaAdv = (miasmaPalmGougeStrike && effectMateria != EssenceType.MIASMA && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.MIASMA, defenderAffinities)
                : 0.0F;
        float inceptionPalmGenesisAdv = (genesisInceptionPalmStrike && effectMateria != EssenceType.GENESIS && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.GENESIS, defenderAffinities)
                : 0.0F;
        float extinctionPalmOblivionAdv = (oblivionExtinctionPalmStrike && effectMateria != EssenceType.OBLIVION && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.OBLIVION, defenderAffinities)
                : 0.0F;
        float firmamentSunderAetherAdv = (aetherFirmamentSunderStrike && effectMateria != EssenceType.AETHER && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.AETHER, defenderAffinities)
                : 0.0F;
        float singularityCleaveEntropicAdv = (entropicSingularityCleaveStrike && effectMateria != EssenceType.ENTROPIC && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.ENTROPIC, defenderAffinities)
                : 0.0F;
        float supernovaCleaveCelestialAdv = (celestialSupernovaCleaveStrike && effectMateria != EssenceType.CELESTIAL && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.CELESTIAL, defenderAffinities)
                : 0.0F;
        float rootCleaveEmpyreanAdv = (empyreanRootCleaveStrike && effectMateria != EssenceType.EMPYREAN && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.EMPYREAN, defenderAffinities)
                : 0.0F;
        float calamitySunderCataclysmAdv = (cataclysmCalamitySunderStrike && effectMateria != EssenceType.CATACLYSM && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.CATACLYSM, defenderAffinities)
                : 0.0F;
        float glacierCleaveTerminusAdv = (terminusGlacierCleaveStrike && effectMateria != EssenceType.TERMINUS && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.TERMINUS, defenderAffinities)
                : 0.0F;
        float realityScytheEschatonAdv = (eschatonRealityScytheStrike && effectMateria != EssenceType.ESCHATON && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.ESCHATON, defenderAffinities)
                : 0.0F;
        float genesisCleaveApotheosisAdv = (apotheosisGenesisCleaveStrike && effectMateria != EssenceType.APOTHEOSIS && !defenderAffinities.isEmpty())
                ? calculateSpellElementalAdvantageScore(EssenceType.APOTHEOSIS, defenderAffinities)
                : 0.0F;
        float grandParadoxEntropicaAdv = (entropicaGrandParadoxStrike && effectMateria != EssenceType.ENTROPICA)
                ? (!defenderAffinities.isEmpty()
                    ? calculateSpellElementalAdvantageScore(EssenceType.ENTROPICA, defenderAffinities)
                    : 1.0F)
                : 0.0F;

        float touchFormBonusAdv = seismicEarthAdv + flashFrozenAdv + cauterizingNetherAdv
                + holySmiteRadiantAdv + cloakingUmbralAdv + vampiricUndeadAdv + destabilizeVoidAdv + mummifyAridAdv
                + overchargeLightningAdv + volcanicMagmaAdv + squallStormAdv + brittleGlacialAdv + botanicalOvergrowthAdv
                + smotherDustAdv + infestationSporeAdv + heatSiphonTaigaAdv + blessingOasisAdv + overchargeVitaeAdv
                + exsanguinationBloodAdv + chakraPalmAuraAdv + fossilizingAmberAdv + siphonPalmEclipseAdv + thornGougeBlightAdv
                + overchargeAstralAdv + cleaveSoulfireAdv + hydroGougeVaporAdv + eventHorizonNullRAdv + oblivionPalmNullUAdv
                + aeroSolarDawnAdv + drowningCleaveAbyssAdv + shieldBashAegisAdv + polarCleaveAuroraAdv + paranoiaMirageAdv
                + phasingPalmWraithAdv + tombstoneSlamBarrowAdv + timberCleavePyreAdv + eclipseCleavePenumbraAdv
                + permafrostCleaveRimeAdv + blossomCleaveSpringAdv + palmOverloadStaticAdv + canopyCleaveSylvanAdv
                + palmGougeMiasmaAdv + inceptionPalmGenesisAdv + extinctionPalmOblivionAdv + firmamentSunderAetherAdv
                + singularityCleaveEntropicAdv + supernovaCleaveCelestialAdv + rootCleaveEmpyreanAdv + calamitySunderCataclysmAdv
                + glacierCleaveTerminusAdv + realityScytheEschatonAdv + genesisCleaveApotheosisAdv + grandParadoxEntropicaAdv;
        float bonusAdvScore = flowBonusAdv + weaponBonusAdv + touchFormBonusAdv + spell.flowPart().flow().getInnateAdvantageBonus();
        float advMult = calculateSpellAdvantageDamageMultiplier(
                effectMateria, caster, target, defenderAffinities, bonusAdvScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        // Earth Slow-Heavy ("Dense Tectonic Mass" Section 6.5): unified 3.0 + 0.35 * tier Armor Shred for 160t on impact
        if (spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY
                && spell.flowPart().mutationEnabled()
                && flowMateria == EssenceType.EARTH) {
            applyTimedAttributeShred(target, level, 3.0D + tier * 0.35D, 0.0D, 160L);
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
            } else if (effectMateria == EssenceType.GENESIS && isGenesisSmiteTarget(target)) {
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
        // Note: TAG_EARTH_DEFENSE_UNTIL, TAG_FROZEN_SPIKES_UNTIL, TAG_NETHER_SHROUD_UNTIL, and TAG_RADIANT_SHROUD_UNTIL already register
        // EARTH, FROZEN, NETHER, and RADIANT in resolveEntityAffinities(target), so Materia advantage scoring already applies exact 0.50x / 1.50x!
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)
                && MateriaFamilyTier.isFrozenLineage(effectMateria)) {
            target.setTicksFrozen(0);
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.10 Necrotic Shroud: registers defense as Undead (+1 vs Frozen -> 0.50x, -1 vs Radiant -> 1.50x)!
            if (MateriaFamilyTier.isFrozenLineage(effectMateria)) {
                damage *= 0.50F;
                target.setTicksFrozen(0);
            } else if (isRadiantLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L)) {
            // Section 6.11 Zero-G Levitation Aura: registers defense as Void (+1 vs Air -> 0.50x, -1 vs Earth -> 1.50x)!
            if (isAirLine(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isEarthLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_ARID_VEIL_UNTIL).orElse(0L)) {
            // Section 6.12 Sandstorm Veil: registers defense as Arid (Weak vs Water & Glacial -> 1.50x)!
            if (isWaterLine(effectMateria) || effectMateria == EssenceType.GLACIAL) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_LIGHTNING_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.13 Static Shroud: registers defense as Lightning (+1 vs Earth -> 0.50x, -1 vs Void & Water -> 1.50x)!
            if (MateriaFamilyTier.isEarthLineage(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isVoidLineage(effectMateria) || isWaterLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)) {
            // Section 6.14 Igneous Armor Mantle: registers defense as Magma (+1 vs Frozen & Nature -> 0.50x, -1 vs Air & Water -> 1.50x)!
            if (MateriaFamilyTier.isFrozenLineage(effectMateria) || isNatureLine(effectMateria)) {
                damage *= 0.50F;
                target.setTicksFrozen(0);
            } else if (isAirLine(effectMateria) || isWaterLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_STORM_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.15 Squall-Rider Shroud: registers defense as Storm (+1 vs Earth & Glacial -> 0.50x, -1 vs Nature & Void -> 1.50x)!
            if (MateriaFamilyTier.isEarthLineage(effectMateria) || MateriaFamilyTier.isFrozenLineage(effectMateria)) {
                damage *= 0.50F;
            } else if (isNatureLine(effectMateria) || MateriaFamilyTier.isVoidLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.16 Permafrost Cryo-Shroud: registers defense as Glacial (+1 vs Arid & Frostbite immunity -> 0.50x, -1 vs Storm, Nature & Undead -> 1.50x)!
            if (effectMateria == EssenceType.ARID || effectMateria == EssenceType.FROZEN) {
                damage *= 0.50F;
                target.setTicksFrozen(0);
            } else if (effectMateria == EssenceType.STORM || isNatureLine(effectMateria) || isNecroticLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BASTION_UNTIL).orElse(0L)) {
            // Section 6.17 Earth-Root Bastion: registers defense as Overgrowth (+1 vs Water, Earth & Void -> 0.50x, -1 vs Nether, Frozen, Umbral & Air -> 1.50x)!
            if (isWaterLine(effectMateria) || MateriaFamilyTier.isEarthLineage(effectMateria) || MateriaFamilyTier.isVoidLineage(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isNetherLineage(effectMateria) || MateriaFamilyTier.isFrozenLineage(effectMateria)
                    || isUmbralLine(effectMateria) || isAirLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_DUST_VEIL_UNTIL).orElse(0L)) {
            // Section 6.18 Obscuring Chalk Veil: registers defense as Dust (+1 vs Vapor & Earth -> 0.50x, -1 vs Water & Void -> 1.50x)!
            if (effectMateria == EssenceType.VAPOR || effectMateria == EssenceType.EARTH) {
                damage *= 0.50F;
            } else if (isWaterLine(effectMateria) || MateriaFamilyTier.isVoidLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_SPORE_MANTLE_UNTIL).orElse(0L)) {
            // Section 6.19 Mycelial Spore-Cloud Mantle: registers defense as Spore (+1 vs Water & Earth -> 0.50x, -1 vs Nether, Frozen, Umbral & Void -> 1.50x)!
            if (isWaterLine(effectMateria) || MateriaFamilyTier.isEarthLineage(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isNetherLineage(effectMateria) || MateriaFamilyTier.isFrozenLineage(effectMateria)
                    || isUmbralLine(effectMateria) || MateriaFamilyTier.isVoidLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_TAIGA_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.20 Winter Solstice Shroud: registers defense as Taiga (+1 vs Water & Nature -> 0.50x, -1 vs Nether, Umbral & Undead -> 1.50x)!
            if (isWaterLine(effectMateria) || (isNatureLine(effectMateria) && effectMateria != EssenceType.TAIGA)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isNetherLineage(effectMateria) || isUmbralLine(effectMateria) || isNecroticLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_OASIS_CARAPACE_UNTIL).orElse(0L)) {
            // Section 6.21 Succulent Thorn Carapace: registers defense as Oasis (+1 vs Water, Earth & Dust -> 0.50x, -1 vs Nether & Umbral -> 1.50x)!
            if (isWaterLine(effectMateria) || MateriaFamilyTier.isEarthLineage(effectMateria) || effectMateria == EssenceType.DUST) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isNetherLineage(effectMateria) || isUmbralLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VITAE_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.22 Cellular Overdrive Shroud: registers defense as Vitae (+1 vs Undead -> 0.50x, -1 vs Nether, Frozen & Umbral -> 1.50x)!
            if (isNecroticLine(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isNetherLineage(effectMateria)
                    || MateriaFamilyTier.isFrozenLineage(effectMateria)
                    || isUmbralLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_BLOOD_CARAPACE_UNTIL).orElse(0L)) {
            // Section 6.23 Coagulated Crimson Carapace: registers defense as Blood (+1 vs Frozen, Nature & Spring -> 0.50x, -1 vs Radiant, Nether & Umbral -> 1.50x)!
            if (MateriaFamilyTier.isFrozenLineage(effectMateria) || isNatureLine(effectMateria) || effectMateria == EssenceType.SPRING) {
                damage *= 0.50F;
            } else if (isRadiantLine(effectMateria) || MateriaFamilyTier.isNetherLineage(effectMateria) || isUmbralLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_AURA_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.24 Chakra Respiration Shroud: registers defense as Aura (+1 vs Undead & Wraith -> 0.50x, -1 vs Umbral, Void & Nether -> 1.50x)!
            if (isNecroticLine(effectMateria)) {
                damage *= 0.50F;
            } else if (isUmbralLine(effectMateria)
                    || MateriaFamilyTier.isVoidLineage(effectMateria)
                    || MateriaFamilyTier.isNetherLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_AMBER_MANTLE_UNTIL).orElse(0L)) {
            // Section 6.25 Petrified Amber Bastion Mantle: registers defense as Amber (+1 vs Void & Barrow -> 0.50x, -1 vs Nether, Frozen & Umbral -> 1.50x)!
            if (MateriaFamilyTier.isVoidLineage(effectMateria) || effectMateria == EssenceType.BARROW) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isNetherLineage(effectMateria)
                    || MateriaFamilyTier.isFrozenLineage(effectMateria)
                    || isUmbralLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_ECLIPSE_BASTION_UNTIL).orElse(0L)) {
            // Section 6.26 Solar Corona Bastion: registers defense as Eclipse (+1 vs Radiant & Nature -> 0.50x, -1 vs Void -> 1.50x)!
            target.removeEffect(MobEffects.BLINDNESS);
            target.removeEffect(MobEffects.DARKNESS);
            if ((isRadiantLine(effectMateria) && effectMateria != EssenceType.ECLIPSE) || isNatureLine(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isVoidLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_BLIGHT_CARAPACE_UNTIL).orElse(0L)) {
            // Section 6.27 Cadaverous Briar-Carapace: registers defense as Blight (-1 vs Radiant, Nether & Umbral -> 1.50x) + Poison/Hunger immunity!
            target.removeEffect(MobEffects.POISON);
            target.removeEffect(MobEffects.HUNGER);
            if (isRadiantLine(effectMateria) || MateriaFamilyTier.isNetherLineage(effectMateria) || isUmbralLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_ASTRAL_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.28 Cosmic Starlight Shroud: absorbs incoming Lightning and Void spell damage into Materia fuel!
            if (isLightningLine(effectMateria) || effectMateria == EssenceType.VOID) {
                if (target instanceof Player astralPlayer) {
                    SpellFuelService.restorePlayerMateriaFuel(
                            astralPlayer, EssenceType.ASTRAL, Math.max(16_000L, Math.round(damage * 9_500.0F))
                    );
                }
                level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(1.0D), target.getZ(), 14, 0.35D, 0.45D, 0.35D, 0.06D);
                level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.75F, 1.35F);
                return 0.0F;
            }
            if (isAirLine(effectMateria) || isUmbralLine(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isEarthLineage(effectMateria) || isNatureLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_SOULFIRE_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.29 Phantasmal Soulfire Shroud: total fire immunity + Soulfire defense (+1 vs Frozen & Nature -> 0.50x, -1 vs Water, Arid & Radiant -> 1.50x)!
            target.clearFire();
            if (effectMateria == EssenceType.NETHER || effectMateria == EssenceType.MAGMA || effectMateria == EssenceType.PYRE || effectMateria == EssenceType.SOULFIRE) {
                return 0.0F;
            } else if (MateriaFamilyTier.isFrozenLineage(effectMateria) || isNatureLine(effectMateria)) {
                damage *= 0.50F;
                target.setTicksFrozen(0);
            } else if (isWaterLine(effectMateria) || effectMateria == EssenceType.ARID || isRadiantLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VAPOR_MIST_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.30 Mist-Walker Shroud: fire quenching + Vapor defense (+1 vs Nether -> 0.50x, -1 vs Frozen, Nature & Earth -> 1.50x)!
            target.clearFire();
            if (MateriaFamilyTier.isNetherLineage(effectMateria) && effectMateria != EssenceType.VAPOR) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isFrozenLineage(effectMateria) || isNatureLine(effectMateria) || MateriaFamilyTier.isEarthLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NULL_R_BASTION_UNTIL).orElse(0L)) {
            // Section 6.31 Event Horizon Bastion: Blindness/Darkness immunity + Null_R defense (+1 vs Air & Undead -> 0.50x, -1 vs Earth & Umbral -> 1.50x)!
            target.removeEffect(MobEffects.BLINDNESS);
            target.removeEffect(MobEffects.DARKNESS);
            if (isAirLine(effectMateria) || isNecroticLine(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isEarthLineage(effectMateria) || isUmbralLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NULL_U_VEIL_UNTIL).orElse(0L)) {
            // Section 6.32 Abyssal Zero-Acoustic Veil: Null_U defense (+1 vs Air & Radiant -> 0.50x, -1 vs Earth -> 1.50x)!
            if (isAirLine(effectMateria) || isRadiantLine(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isEarthLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_DAWN_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.33 Dawn-Bringer Solar Shroud: Blindness/Darkness immunity + Dawn defense (+1 vs Undead & Earth -> 0.50x, -1 vs Umbral & Void -> 1.50x)!
            target.removeEffect(MobEffects.BLINDNESS);
            target.removeEffect(MobEffects.DARKNESS);
            if (isNecroticLine(effectMateria) || MateriaFamilyTier.isEarthLineage(effectMateria)) {
                damage *= 0.50F;
            } else if (isUmbralLine(effectMateria) || MateriaFamilyTier.isVoidLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_ABYSS_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.34 Hadopelagic Trench Shroud: Abyss defense (+1 vs Radiant, Nether & Nature -> 0.50x, -1 vs Void & Frozen -> 1.50x)!
            if (isRadiantLine(effectMateria) || MateriaFamilyTier.isNetherLineage(effectMateria) || isNatureLine(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isVoidLineage(effectMateria) || MateriaFamilyTier.isFrozenLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_AEGIS_MANTLE_UNTIL).orElse(0L)) {
            // Section 6.35 Gilded Bastion Citadel Mantle: Blindness/Darkness immunity + Aegis defense (+1 vs Void & Undead -> 0.50x, -1 vs Umbral, Air & Nature -> 1.50x)!
            target.removeEffect(MobEffects.BLINDNESS);
            target.removeEffect(MobEffects.DARKNESS);
            if (MateriaFamilyTier.isVoidLineage(effectMateria) || isNecroticLine(effectMateria)) {
                damage *= 0.50F;
            } else if (isUmbralLine(effectMateria) || isAirLine(effectMateria) || isNatureLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_AURORA_MANTLE_UNTIL).orElse(0L)) {
            // Section 6.36 Borealis Prismatic Veil Mantle: Blindness/Darkness/Freeze immunity + Aurora defense (+1 vs Undead, Nether, Earth & Nature -> 0.50x, -1 vs Void -> 1.50x)!
            target.removeEffect(MobEffects.BLINDNESS);
            target.removeEffect(MobEffects.DARKNESS);
            target.setTicksFrozen(0);
            if (isNecroticLine(effectMateria) || MateriaFamilyTier.isNetherLineage(effectMateria)
                    || MateriaFamilyTier.isEarthLineage(effectMateria) || isNatureLine(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isVoidLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_MIRAGE_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.37 Spatial Displacement Glamour Shroud: 50% optical displacement dodge + Mirage defense (+1 vs Water, Void, Blight & Undead -> 0.50x, -1 vs Umbral & Nature -> 1.50x)!
            if (level.random.nextFloat() < 0.50F) {
                level.sendParticles(ParticleTypes.POOF, target.getX(), target.getY(0.9D), target.getZ(), 8, 0.35D, 0.4D, 0.35D, 0.04D);
                syncEntityBarrierState(level, target, 0.65F, null);
                return 0.0F;
            }
            if (isWaterLine(effectMateria) || MateriaFamilyTier.isVoidLineage(effectMateria) || isNecroticLine(effectMateria)) {
                damage *= 0.50F;
            } else if (isUmbralLine(effectMateria) || isNatureLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.38 Specter Ectoplasm Armor: Wither immunity, 25% magic damage reflection, + Wraith defense (+1 vs Air, Umbral, Aura & Frozen -> 0.50x, -1 vs Radiant & Earth -> 1.50x)!
            target.removeEffect(MobEffects.WITHER);
            if (caster != null && caster != target && caster.isAlive() && !REENTRANT_GUST_HURT.get()) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    caster.hurtServer(level, level.damageSources().magic(), Math.max(1.0F, damage * 0.25F));
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
            if (isAirLine(effectMateria) || isUmbralLine(effectMateria)
                    || effectMateria == EssenceType.AURA || MateriaFamilyTier.isFrozenLineage(effectMateria)) {
                damage *= 0.50F;
            } else if (isRadiantLine(effectMateria) || MateriaFamilyTier.isEarthLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_BARROW_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.39 Catacomb Miasma Shroud: Poison & Wither immunity + Barrow defense (+1 vs Frozen, Amber & Void -> 0.50x, -1 vs Radiant, Air & Nature -> 1.50x)!
            target.removeEffect(MobEffects.POISON);
            target.removeEffect(MobEffects.WITHER);
            if (MateriaFamilyTier.isFrozenLineage(effectMateria) || effectMateria == EssenceType.AMBER || MateriaFamilyTier.isVoidLineage(effectMateria)) {
                damage *= 0.50F;
            } else if (isRadiantLine(effectMateria) || isAirLine(effectMateria) || isNatureLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_PYRE_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.40 Pyre Hearth Hearthstone Shroud: Fire, Lava & Freezing immunity + Pyre defense (+1 vs Void, Frozen, Nature, Glacial & Rime -> 0.50x, -1 vs Air -> 1.50x)!
            target.clearFire();
            target.setTicksFrozen(0);
            if (MateriaFamilyTier.isFrozenLineage(effectMateria)) {
                return 0.0F;
            }
            if (MateriaFamilyTier.isVoidLineage(effectMateria) || isNatureLine(effectMateria)) {
                damage *= 0.50F;
            } else if (isAirLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_SHIMMER_UNTIL).orElse(0L)) {
            float dodgePct = targetData.getFloat(SpellLingeringZoneManager.TAG_PENUMBRA_SHIMMER_DODGE).orElse(0.20F);
            if (level.random.nextFloat() < dodgePct) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.9D), target.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.04D);
                return 0.0F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_MANTLE_UNTIL).orElse(0L)) {
            // Section 6.41 Penumbral Corona Mantle: Blindness/Darkness immunity + Penumbra defense (+1 vs Air, Radiant, Nature, Celestial & Aegis -> 0.50x, -1 vs Earth, Void & Undead -> 1.50x)!
            target.removeEffect(MobEffects.BLINDNESS);
            target.removeEffect(MobEffects.DARKNESS);
            if (isAirLine(effectMateria) || isRadiantLine(effectMateria) || isNatureLine(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isEarthLineage(effectMateria) || MateriaFamilyTier.isVoidLineage(effectMateria) || isNecroticLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_RIME_MANTLE_UNTIL).orElse(0L)) {
            // Section 6.42 Hoarfrost Glacial Aegis Mantle: Freeze/PowderSnow/Slowness/Fire immunity + Rime defense (+1 vs Earth, Nature, Magma & Overgrowth -> 0.50x, -1 vs Void & Undead -> 1.50x)!
            target.clearFire();
            target.setTicksFrozen(0);
            target.removeEffect(MobEffects.SLOWNESS);
            if (MateriaFamilyTier.isFrozenLineage(effectMateria)) {
                return 0.0F;
            }
            if (MateriaFamilyTier.isEarthLineage(effectMateria) || isNatureLine(effectMateria) || effectMateria == EssenceType.MAGMA) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isVoidLineage(effectMateria) || isNecroticLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_RIME_FIRE_RES_UNTIL).orElse(0L)
                && MateriaFamilyTier.isNetherLineage(effectMateria)) {
            damage *= 0.50F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_SPRING_MANTLE_UNTIL).orElse(0L)) {
            // Section 6.43 Vernal Fountainhead Mantle: Drowning/Poison/Wither/Fire immunity + Spring defense (+1 vs Undead, Water, Earth, Soulfire & Blight -> 0.50x, -1 vs Umbral -> 1.50x)!
            target.clearFire();
            target.setAirSupply(target.getMaxAirSupply());
            target.removeEffect(MobEffects.POISON);
            target.removeEffect(MobEffects.WITHER);
            if (isNecroticLine(effectMateria)
                    || isWaterLine(effectMateria)
                    || MateriaFamilyTier.isEarthLineage(effectMateria)
                    || effectMateria == EssenceType.SOULFIRE) {
                damage *= 0.50F;
            } else if (isUmbralLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_STATIC_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.44 Dielectric Faraday Shroud: Lightning/Slowness immunity + Static defense (+1 vs Earth, Water, Void, Glacial & Vapor -> 0.50x, -1 vs Nature -> 1.50x)!
            target.removeEffect(MobEffects.SLOWNESS);
            if (isLightningLine(effectMateria)) {
                return 0.0F;
            }
            if (MateriaFamilyTier.isEarthLineage(effectMateria)
                    || isWaterLine(effectMateria)
                    || MateriaFamilyTier.isVoidLineage(effectMateria)
                    || effectMateria == EssenceType.GLACIAL
                    || effectMateria == EssenceType.VAPOR) {
                damage *= 0.50F;
            } else if (isNatureLine(effectMateria)) {
                damage *= 1.50F;
            }
        } else if ((now <= targetData.getLong(SpellLingeringZoneManager.TAG_STATIC_ORBIT_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_STATIC_SENTRY_IMMUNE_UNTIL).orElse(0L))
                && isLightningLine(effectMateria)) {
            return 0.0F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL).orElse(0L)) {
            // Section 6.45 Primeval Photosynthetic Aegis: Poison/Wither/Blindness immunity, Sylvan defense
            // (+1 vs Water, Earth, Undead, Barrow & Wraith -> 0.50x, -1 vs Nether & Air -> 1.50x),
            // and absorbs up to 12 incoming damage instances converting 50% into Sylvan Materia fuel!
            target.removeEffect(MobEffects.POISON);
            target.removeEffect(MobEffects.WITHER);
            target.removeEffect(MobEffects.BLINDNESS);
            if (isWaterLine(effectMateria)
                    || MateriaFamilyTier.isEarthLineage(effectMateria)
                    || isNecroticLine(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isNetherLineage(effectMateria) || isAirLine(effectMateria)) {
                damage *= 1.50F;
            }
            int hitsLeft = targetData.getInt(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_HITS).orElse(0);
            if (hitsLeft > 0) {
                targetData.putInt(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_HITS, hitsLeft - 1);
                if (hitsLeft - 1 <= 0) {
                    targetData.remove(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL);
                    triggerBarrierShatterOrFallback(level, target, EssenceType.SYLVAN, 5, caster != null ? caster.position() : null);
                } else {
                    syncEntityBarrierState(level, target, 0.55F, caster != null ? caster.position() : null);
                }
                if (target instanceof Player sylvanPlayer) {
                    long restored = Math.max(8L, Math.round(damage * 0.50F * 16.0F));
                    SpellFuelService.restorePlayerMateriaFuel(sylvanPlayer, EssenceType.SYLVAN, restored);
                }
                damage *= 0.50F;
            }
        }
        // Section 6.46 Putrescent Mire-Carapace & Septic Immunity: Poison/Wither/Hunger/Drowning immunity + Miasma elemental attunement!
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_MIASMA_SEPTIC_IMMUNE_UNTIL).orElse(0L)) {
            target.removeEffect(MobEffects.POISON);
            target.removeEffect(MobEffects.WITHER);
            target.removeEffect(MobEffects.HUNGER);
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_MIASMA_CARAPACE_UNTIL).orElse(0L)) {
            target.removeEffect(MobEffects.POISON);
            target.removeEffect(MobEffects.WITHER);
            target.removeEffect(MobEffects.HUNGER);
            target.setAirSupply(target.getMaxAirSupply());
            syncEntityBarrierState(level, target, 0.45F, caster != null ? caster.position() : null);
            if (MateriaFamilyTier.isFrozenLineage(effectMateria)
                    || MateriaFamilyTier.isEarthLineage(effectMateria)
                    || effectMateria == EssenceType.OASIS
                    || effectMateria == EssenceType.TAIGA) {
                damage *= 0.50F;
            } else if (isUmbralLine(effectMateria)
                    || isRadiantLine(effectMateria)
                    || MateriaFamilyTier.isNetherLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        // Section 6.47 Amniotic Genesis Shroud: Blindness/Darkness/Wither/Poison/Drowning immunity + absorbs 40% into Regen II & Genesis fuel!
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_GENESIS_SHROUD_UNTIL).orElse(0L)) {
            target.removeEffect(MobEffects.BLINDNESS);
            target.removeEffect(MobEffects.DARKNESS);
            target.removeEffect(MobEffects.WITHER);
            target.removeEffect(MobEffects.POISON);
            target.setAirSupply(target.getMaxAirSupply());
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, true, true));
            syncEntityBarrierState(level, target, 0.50F, caster != null ? caster.position() : null);
            if (target instanceof Player genPlayer) {
                long restored = Math.max(8L, Math.round(damage * 0.40F * 16.0F));
                SpellFuelService.restorePlayerMateriaFuel(genPlayer, EssenceType.GENESIS, restored);
            }
            if (effectMateria == EssenceType.UNDEAD
                    || effectMateria == EssenceType.OBLIVION
                    || effectMateria == EssenceType.WRAITH) {
                damage *= 0.50F;
            } else if (isUmbralLine(effectMateria)) {
                damage *= 1.50F;
            }
            damage *= 0.60F; // Absorbs 40% of incoming damage!
        }
        // Section 6.48 Oblivion Void-Shroud & Obsidian Extinction Armor: Fire/Lava/Wither immunity + Oblivion elemental attunement!
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_OBLIVION_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_OBLIVION_EXTINCTION_ARMOR_UNTIL).orElse(0L)) {
            if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_OBLIVION_EXTINCTION_ARMOR_UNTIL).orElse(0L)) {
                target.clearFire();
                target.removeEffect(MobEffects.WITHER);
            }
            syncEntityBarrierState(level, target, 0.45F, caster != null ? caster.position() : null);
            if (isRadiantLine(effectMateria)
                    || isNatureLine(effectMateria)
                    || MateriaFamilyTier.isFrozenLineage(effectMateria)
                    || effectMateria == EssenceType.AURA) {
                damage *= 0.50F;
            } else if (isWaterLine(effectMateria) || MateriaFamilyTier.isVoidLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        // Section 6.50 Transcendent Firmament Quintessence Shroud & Aegis: Aether elemental attunement (+1 vs Earth/Nether/Magma/Dust/Overgrowth -> 0.50x, -1 vs Nature/Frozen/Arid -> 1.50x)!
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_AETHER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_AETHER_AEGIS_UNTIL).orElse(0L)) {
            target.resetFallDistance();
            target.setAirSupply(target.getMaxAirSupply());
            syncEntityBarrierState(level, target, 0.50F, caster != null ? caster.position() : null);
            if (MateriaFamilyTier.isEarthLineage(effectMateria) && effectMateria != EssenceType.ARID
                    || MateriaFamilyTier.isNetherLineage(effectMateria)) {
                damage *= 0.50F;
            } else if (isNatureLine(effectMateria)
                    || MateriaFamilyTier.isFrozenLineage(effectMateria)
                    || effectMateria == EssenceType.ARID) {
                damage *= 1.50F;
            }
        }
        // Section 6.51 Abyssal Event Horizon Mantle & Apex Entropic Shroud: Wither/Poison/Blindness/Darkness immunity + Entropic elemental attunement!
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_ENTROPIC_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_ENTROPIC_LIFESTEAL_SHROUD_UNTIL).orElse(0L)) {
            target.removeEffect(MobEffects.WITHER);
            target.removeEffect(MobEffects.POISON);
            target.removeEffect(MobEffects.BLINDNESS);
            if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_ENTROPIC_SHROUD_UNTIL).orElse(0L)) {
                target.removeEffect(MobEffects.DARKNESS);
            }
            syncEntityBarrierState(level, target, 0.50F, caster != null ? caster.position() : null);
            if (isAirLine(effectMateria)
                    || isNatureLine(effectMateria)
                    || MateriaFamilyTier.isFrozenLineage(effectMateria)
                    || effectMateria == EssenceType.CELESTIAL
                    || effectMateria == EssenceType.AEGIS
                    || effectMateria == EssenceType.AURORA) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isEarthLineage(effectMateria)
                    || MateriaFamilyTier.isVoidLineage(effectMateria)
                    || MateriaFamilyTier.isNetherLineage(effectMateria)
                    || isUmbralLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        // Section 6.52 Holy Celestial Supernova Shroud & Ascendance Shroud: Wither/Poison/Blindness/Darkness immunity + Celestial elemental attunement!
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_ASCENDANCE_UNTIL).orElse(0L)) {
            target.removeEffect(MobEffects.WITHER);
            target.removeEffect(MobEffects.POISON);
            target.removeEffect(MobEffects.BLINDNESS);
            if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_SHROUD_UNTIL).orElse(0L)) {
                target.removeEffect(MobEffects.DARKNESS);
            }
            syncEntityBarrierState(level, target, 0.50F, caster != null ? caster.position() : null);
            if (isNecroticLine(effectMateria)
                    || isWaterLine(effectMateria)
                    || MateriaFamilyTier.isEarthLineage(effectMateria)) {
                damage *= 0.50F;
            } else if (isUmbralLine(effectMateria) || MateriaFamilyTier.isVoidLineage(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if ((SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                || hasActiveMutatedHex(targetData, now, EssenceType.WATER)
                || hasActiveMutatedHex(targetData, now, EssenceType.STORM))
                && isLightningLine(effectMateria)) {
            damage *= 2.0F;
        }
        // Earth Sundering Hex (#4): exact 2.0x incoming Earth spell damage (do not double-stack hex's own armor shred)!
        boolean sunderingEarthHexHit = hasActiveMutatedHex(targetData, now, EssenceType.EARTH)
                && (MateriaFamilyTier.isEarthLineage(effectMateria) || earthSeismicStrike);
        if (sunderingEarthHexHit) {
            damage *= 2.0F;
        }
        // Frozen Permafrost Stasis Hex (#5): +50% / +75% / +100% shatter bonus on Earth/crush Materia damage!
        if (hasActiveMutatedHex(targetData, now, EssenceType.FROZEN)
                && isFrozenHexShatterMateria(effectMateria)) {
            damage *= resolveFrozenHexShatterMultiplier(getHexStackLevel(target));
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

        // 4. Existing Broken Armor Shred bonus (+4% incoming damage per shred point, max 8; skipped when Sundering Hex 2.0x applies)
        if (!sunderingEarthHexHit && now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)) {
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
        if (resolvedSlot != null) {
            SpellMutationProfile formMut = resolvedSlot.formPart().resolveMutation(
                    spell.materiaSpecific(),
                    safeFallback != EssenceType.REGULAR ? safeFallback : effectMateria
            );
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
        boolean hurt = hurtWithAdvantageTracked(level, target, magicSrc, Math.max(0.5F, damage));
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

        // 7. Mutation Lifesteal & Status Side-Effects (including Section 6.10 Vampiric Touch 30% & Siphon Life 25% HP + Fuel!)
        float resolvedLifesteal = effectMutation.mutated() ? effectMutation.lifestealFraction() : 0.0F;
        if (undeadVampiricStrike) {
            resolvedLifesteal = Math.max(resolvedLifesteal, 0.30F);
        } else if (undeadBeamSiphon) {
            resolvedLifesteal = Math.max(resolvedLifesteal, 0.25F);
        } else if (oasisProjectileLifesteal) {
            // Section 6.21 Miracle Desert Bloom Seed: siphons exact 20% of damage dealt as healing!
            resolvedLifesteal = Math.max(resolvedLifesteal, 0.20F);
        } else if (oasisBeamLifesteal) {
            // Section 6.21 Subterranean Aquifer Stream: heals caster for exact 15% of damage dealt!
            resolvedLifesteal = Math.max(resolvedLifesteal, 0.15F);
        } else if (vitaeBeamLifesteal) {
            // Section 6.22 Cardiovascular Life-Tether: leaches exact 25% of damage dealt back to heal the caster!
            resolvedLifesteal = Math.max(resolvedLifesteal, 0.25F);
        }
        if (bloodFormLifesteal > 0.0F) {
            // Section 6.23 Blood per-form lifesteal (30% Needle/Snare, 35% Tether, 40% Strike, 20% Deluge/Chains, 15% Downpour)!
            resolvedLifesteal = Math.max(resolvedLifesteal, bloodFormLifesteal);
        }
        if (soulfireFormLifesteal > 0.0F) {
            // Section 6.29 Soulfire per-form lifesteal (20% Skull/Jet/Pyre, 25% Cleave, 15% Nova/Wave/Tempest/Chains)!
            resolvedLifesteal = Math.max(resolvedLifesteal, soulfireFormLifesteal);
        }
        if (nullUPalmLifesteal > 0.0F) {
            // Section 6.32 Spacetime Oblivion Palm: siphons 25% of damage dealt directly into the caster's health pool!
            resolvedLifesteal = Math.max(resolvedLifesteal, nullUPalmLifesteal);
        }
        if (resolvedLifesteal > 0.0F && caster != null && caster.isAlive()) {
            float steal = damage * resolvedLifesteal;
            notifyMagicalHeal(caster, steal);
            caster.heal(steal);
        }
        if (undeadBeamSiphon && caster instanceof Player siphonPlayer && siphonPlayer.isAlive()) {
            float dmgRatio = Math.clamp(damage / Math.max(1.0F, baseHp), 0.25F, 2.50F);
            long fuelRestore = Math.max(8L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25F * dmgRatio));
            SpellFuelService.restorePlayerMateriaFuel(siphonPlayer, EssenceType.UNDEAD, fuelRestore);
        }
        if (auraFormFuelSiphon > 0.0F && caster instanceof Player auraPlayer && auraPlayer.isAlive()) {
            siphonCasterMateriaFuelFromDamage(auraPlayer, EssenceType.AURA, tier, damage, auraFormFuelSiphon);
        }
        if (soulfireBeamFuelSiphon > 0.0F && caster instanceof Player soulPlayer && soulPlayer.isAlive()) {
            siphonCasterMateriaFuelFromDamage(soulPlayer, EssenceType.SOULFIRE, tier, damage, soulfireBeamFuelSiphon);
        }
        if (dawnBeamFuelSiphon > 0.0F && caster instanceof Player dawnPlayer && dawnPlayer.isAlive()) {
            siphonCasterMateriaFuelFromDamage(dawnPlayer, EssenceType.DAWN, tier, damage, dawnBeamFuelSiphon);
        }
        if (effectMutation.mutated()) {
            applyMutatedElementalAftereffects(level, target, effectMateria, tier);
        } else if (effectMateria != null && MateriaFamilyTier.isFrozenLineage(effectMateria)) {
            boolean tripleFreeze = SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                    || hasActiveMutatedHex(targetData, now, EssenceType.WATER);
            int freezeGain = (30 + tier * 8) * (tripleFreeze ? 3 : 1);
            target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 120, target.getTicksFrozen() + freezeGain));
        }

        // Section 6.41-6.44 Penumbra, Rime, Spring & Static Orbit Flows: discharge homing darts/bolts on spell hit as well as melee hit!
        if (caster != null && caster != target) {
            CompoundTag cOrbit = EntityHelper.getPersistentData(caster);
            if (now <= cOrbit.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_ORBIT_UNTIL).orElse(0L)
                    || now <= cOrbit.getLong(SpellLingeringZoneManager.TAG_RIME_ORBIT_UNTIL).orElse(0L)
                    || now <= cOrbit.getLong(SpellLingeringZoneManager.TAG_SPRING_ORBIT_UNTIL).orElse(0L)
                    || now <= cOrbit.getLong(SpellLingeringZoneManager.TAG_STATIC_ORBIT_UNTIL).orElse(0L)
                    || now <= cOrbit.getLong(SpellLingeringZoneManager.TAG_SYLVAN_ORBIT_UNTIL).orElse(0L)
                    || now <= cOrbit.getLong(SpellLingeringZoneManager.TAG_GENESIS_ORBIT_UNTIL).orElse(0L)
                    || now <= cOrbit.getLong(SpellLingeringZoneManager.TAG_AETHER_ORBIT_UNTIL).orElse(0L)
                    || now <= cOrbit.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_ORBIT_UNTIL).orElse(0L)
                    || now <= cOrbit.getLong(SpellLingeringZoneManager.TAG_PRISMATIC_ORBIT_UNTIL).orElse(0L)
                    || now <= cOrbit.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_ORBIT_UNTIL).orElse(0L)
                    || now <= cOrbit.getLong(SpellLingeringZoneManager.TAG_CATACLYSM_ORBIT_UNTIL).orElse(0L)
                    || now <= cOrbit.getLong(SpellLingeringZoneManager.TAG_TERMINUS_ORBIT_UNTIL).orElse(0L)
                    || now <= cOrbit.getLong(SpellLingeringZoneManager.TAG_ESCHATON_ORBIT_UNTIL).orElse(0L)) {
                ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, caster, target, 1);
            }
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
            boolean tripleFreeze = SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                    || hasActiveMutatedHex(EntityHelper.getPersistentData(target), level.getGameTime(), EssenceType.WATER);
            int freezeGain = (45 + tier * 10) * (tripleFreeze ? 3 : 1);
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

        if (REENTRANT_ELEMENTAL_REACTION.get()) {
            return;
        }

        EssenceType previous = null;
        if (!prevStr.isEmpty() && now <= until) {
            try {
                previous = EssenceType.valueOf(prevStr);
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Detect active world/status Frozen, Burning, Soaked, or Sundered states even when TAG_ACTIVE_ELEMENT was not previously stamped!
        if (previous == null) {
            boolean targetBurning = target.isOnFire()
                    || now <= data.getLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL).orElse(0L)
                    || hasActiveMutatedHex(data, now, EssenceType.NETHER);
            boolean targetSoaked = SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                    || hasActiveMutatedHex(data, now, EssenceType.WATER);
            boolean targetSundered = now <= data.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                    || hasActiveMutatedHex(data, now, EssenceType.EARTH);

            if (MateriaFamilyTier.isNetherLineage(incoming)
                    && (now <= data.getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L)
                        || target.getTicksFrozen() >= 40
                        || hasActiveMutatedHex(data, now, EssenceType.FROZEN))) {
                previous = EssenceType.FROZEN;
            } else if (MateriaFamilyTier.isFrozenLineage(incoming) && targetBurning) {
                previous = EssenceType.NETHER;
            } else if (isWaterLine(incoming) && targetBurning) {
                previous = EssenceType.NETHER;
            } else if (MateriaFamilyTier.isNetherLineage(incoming) && targetSoaked) {
                previous = EssenceType.WATER;
            } else if (MateriaFamilyTier.isNetherLineage(incoming) && targetSundered) {
                previous = EssenceType.EARTH;
            } else if (MateriaFamilyTier.isEarthLineage(incoming) && targetBurning) {
                previous = EssenceType.NETHER;
            } else if (isRadiantLine(incoming)
                    && (now <= data.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)
                        || hasActiveMutatedHex(data, now, EssenceType.UMBRAL))) {
                previous = EssenceType.UMBRAL;
            } else if (isUmbralLine(incoming)
                    && (now <= data.getLong(SpellLingeringZoneManager.TAG_RADIANT_HALO_REVEAL_UNTIL).orElse(0L)
                        || now <= data.getLong(SpellLingeringZoneManager.TAG_RADIANT_SHROUD_UNTIL).orElse(0L)
                        || hasActiveMutatedHex(data, now, EssenceType.RADIANT))) {
                previous = EssenceType.RADIANT;
            } else if (isNecroticLine(incoming)
                    && (hasActiveMutatedHex(data, now, EssenceType.NATURE)
                        || hasActiveMutatedHex(data, now, EssenceType.SPORE)
                        || target.hasEffect(MobEffects.POISON))) {
                previous = EssenceType.NATURE;
            } else if (isNatureLine(incoming)
                    && (hasActiveMutatedHex(data, now, EssenceType.UNDEAD)
                        || hasActiveMutatedHex(data, now, EssenceType.BLIGHT)
                        || target.hasEffect(MobEffects.WITHER))) {
                previous = EssenceType.UNDEAD;
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

        REENTRANT_ELEMENTAL_REACTION.set(true);
        try {
        // 1. Thermal Shock (Fire/Nether + Ice/Frozen — Guide Sec 1.2A: shatters ice in a superheated steam explosion, clearing both!)
        if ((MateriaFamilyTier.isNetherLineage(previous) && MateriaFamilyTier.isFrozenLineage(incoming))
                || (MateriaFamilyTier.isFrozenLineage(previous) && MateriaFamilyTier.isNetherLineage(incoming))) {
            Vec3 hitOrig = caster != null ? caster.getBoundingBox().getCenter() : null;
            if (now <= data.getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L)) {
                SpellLingeringZoneManager.clearFrozenStasisAndShatter(level, target, data, hitOrig);
            }
            // Section 6.7 Hellfire Sear Hex is NEVER counter-cleansed by Frozen-lineage magic (only Water, Restoration, or caster defeat)!
            if (hasActiveMutatedHex(data, now, EssenceType.FROZEN)) {
                clearHexData(target, data);
            }
            if (!hasActiveMutatedHex(data, now, EssenceType.NETHER)) {
                data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
                data.remove(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL);
                target.clearFire();
            } else {
                target.igniteForSeconds(3);
            }
            target.setTicksFrozen(0);

            target.invulnerableTime = 0;
            hurtWithPureMateria(level, target, reactionSrc, Math.max(4.0F, triggerDamage * 0.65F));
            for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.2D))) {
                if (nearby == target || !nearby.isAlive() || isProtectedFromOffensiveSpell(nearby, caster, wardSafeguard)) continue;
                nearby.invulnerableTime = 0;
                hurtWithPureMateria(level, nearby, reactionSrc, Math.max(2.5F, triggerDamage * 0.45F));
            }
            SpellWorldVfxManager.broadcastProjectileImpact(
                    level, target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D),
                    new Vec3(0.0D, 1.0D, 0.0D), EssenceType.FROZEN, EssenceType.NETHER, 1.85F, 4
            );
            level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(0.55D), target.getZ(), 28, 0.55D, 0.50D, 0.55D, 0.10D);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
                    target.getX(), target.getY(0.55D), target.getZ(), 20, 0.45D, 0.45D, 0.45D, 0.14D);
            level.playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.85F);
            level.playSound(null, target.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.90F, 1.25F);
        }
        // 2. Annihilation Burst (Radiant + Umbral — Guide Sec 1.2A: light colliding with shadow collapses into a blinding flash
        //    that dazes the target, consumes both opposing ailments, and strips temporary armor!)
        else if ((isRadiantLine(previous) && isUmbralLine(incoming))
                || (isUmbralLine(previous) && isRadiantLine(incoming))) {
            data.remove(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL);
            data.remove(SpellLingeringZoneManager.TAG_RADIANT_HALO_REVEAL_UNTIL);
            if ((isUmbralLine(incoming) && hasActiveMutatedHex(data, now, EssenceType.RADIANT))
                    || (isRadiantLine(incoming) && hasActiveMutatedHex(data, now, EssenceType.UMBRAL))) {
                clearHexData(target, data);
            }
            SpellWorldVfxManager.broadcast(
                    level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_RADIAL_NOVA,
                    target.position().add(0.0D, 0.15D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    EssenceType.RADIANT, EssenceType.UMBRAL, 3.6F, 54, 4
            );
            level.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY(0.6D), target.getZ(), 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.65D), target.getZ(), 28, 0.65D, 0.55D, 0.65D, 0.14D);
            level.playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9F, 1.35F);
            level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.1F, 1.45F);

            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5D));
            for (LivingEntity e : nearby) {
                if (!e.isAlive() || isProtectedFromOffensiveSpell(e, caster, wardSafeguard)) continue;
                // Strip temporary armor (Absorption hearts, temporary Resistance, temporary Arcane Ward, and 4 armor points)!
                e.removeEffect(MobEffects.ABSORPTION);
                e.removeEffect(MobEffects.RESISTANCE);
                e.removeEffect(MobEffects.INVISIBILITY);
                if (e.getAbsorptionAmount() > 0.0F) {
                    e.setAbsorptionAmount(0.0F);
                }
                CompoundTag eData = EntityHelper.getPersistentData(e);
                if (now <= eData.getLong(TAG_WARD_UNTIL).orElse(0L)) {
                    eData.putFloat(TAG_WARD_HP, 0.0F);
                    eData.remove(TAG_WARD_UNTIL);
                    syncEntityBarrierState(level, e, 1.0F, target.position());
                }
                applyTimedAttributeShred(e, level, 4.0D, 1.0D, 160L);

                // Blinding flash & daze
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                if (e instanceof Mob dazedMob) {
                    dazedMob.getNavigation().stop();
                }

                e.invulnerableTime = 0;
                float burstMult = (e == target) ? 0.60F : 0.45F;
                hurtWithPureMateria(level, e, reactionSrc, Math.max(2.5F, triggerDamage * burstMult));
            }
        }
        // 3. Atmospheric Eruption (Air + Earth — Guide Sec 1.2.A: grinding dust vortex blinding surrounding foes & shredding armor!)
        else if ((isAirLine(previous) && MateriaFamilyTier.isEarthLineage(incoming))
                || (MateriaFamilyTier.isEarthLineage(previous) && isAirLine(incoming))) {
            target.invulnerableTime = 0;
            hurtWithPureMateria(level, target, reactionSrc, Math.max(2.0F, triggerDamage * 0.45F));
            Vec3 vel = target.getDeltaMovement();
            target.setDeltaMovement(vel.x, Math.max(0.78D, vel.y + 0.70D), vel.z);
            target.hasImpulse = true;
            for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5D))) {
                if (!nearby.isAlive() || isProtectedFromOffensiveSpell(nearby, caster, wardSafeguard)) continue;
                nearby.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                applyTimedAttributeShred(nearby, level, 4.0D, 0.0D, 160L);
            }
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                    level, target.position(), 3.4D, false, EssenceType.AIR, EssenceType.EARTH, 18, 3
            );
            level.sendParticles(ParticleTypes.GUST, target.getX(), target.getY(0.2), target.getZ(), 6, 0.4, 0.2, 0.4, 0.05);
            level.playSound(null, target.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.95F, 0.90F);
        }
        // 4. Conductive Overcharge (Water/Vapor + Lightning/Static/Storm — Guide Sec 1.2B:
        //    wet targets take +100% bonus lightning damage, and electric arcs jump double distance to up to 8 surrounding entities!)
        else if ((isWaterLine(previous) && isLightningLine(incoming))
                || (isLightningLine(previous) && isWaterLine(incoming))) {
            level.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.80F, 1.55F);
            Vec3 originMid = target.getBoundingBox().getCenter();
            List<LivingEntity> chainTargets = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(10.0D));
            chainTargets.sort(java.util.Comparator.comparingDouble(target::distanceToSqr));
            int arcs = 0;
            for (LivingEntity other : chainTargets) {
                if (other == target || !other.isAlive() || target.distanceToSqr(other) > 100.0D) continue;
                if (isProtectedFromOffensiveSpell(other, caster, wardSafeguard)) continue;
                other.invulnerableTime = 0;
                hurtWithPureMateria(level, other, reactionSrc, Math.max(2.0F, triggerDamage * 0.75F));
                SpellWorldVfxManager.broadcastChainArc(
                        level, originMid, other.getBoundingBox().getCenter(), EssenceType.LIGHTNING, EssenceType.WATER, 3
                );
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, other.getX(), other.getY(0.6), other.getZ(), 14, 0.28, 0.38, 0.28, 0.12);
                if (++arcs >= 8) break;
            }
        }
        // 5. Evaporative Scald (Water + Nether — Guide Sec 1.2A: quenches hellfire into a scalding mist cloud that suffocates mobs!)
        else if ((isWaterLine(previous) && MateriaFamilyTier.isNetherLineage(incoming))
                || (MateriaFamilyTier.isNetherLineage(previous) && isWaterLine(incoming))) {
            target.clearFire();
            data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
        data.remove(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL);
            data.remove(SpellLingeringZoneManager.TAG_SOAKED_UNTIL);
            if (hasActiveMutatedHex(data, now, EssenceType.WATER) || hasActiveMutatedHex(data, now, EssenceType.NETHER)) {
                clearHexData(target, data);
            }
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 1));
            target.invulnerableTime = 0;
            hurtWithPureMateria(level, target, level.damageSources().inWall(), Math.max(2.0F, triggerDamage * 0.40F));

            Vec3 mistCenter = target.position().add(0.0D, 0.25D, 0.0D);
            double mistRad = 3.5D;
            final LivingEntity mistOwner = caster;
            final boolean safeMist = wardSafeguard;
            final float scaldTickDmg = Math.max(1.5F, triggerDamage * 0.28F);
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    mistOwner, mistCenter, mistRad, 60, now, null, null, EssenceType.VAPOR, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        if (elapsed % 6 == 0) {
                            srv.sendParticles(ParticleTypes.CLOUD, zone.center.x, zone.center.y + 0.85D, zone.center.z,
                                    16, mistRad * 0.48D, 0.65D, mistRad * 0.48D, 0.03D);
                            srv.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, zone.center.x, zone.center.y + 0.70D, zone.center.z,
                                    8, mistRad * 0.42D, 0.55D, mistRad * 0.42D, 0.02D);
                        }
                        if (elapsed > 0 && elapsed % 15 == 0) {
                            LivingEntity owner = zone.resolveCaster(srv);
                            AABB mistBox = new AABB(
                                    zone.center.x - mistRad, zone.center.y - 1.2D, zone.center.z - mistRad,
                                    zone.center.x + mistRad, zone.center.y + 2.6D, zone.center.z + mistRad
                            );
                            for (LivingEntity suffocating : srv.getEntitiesOfClass(LivingEntity.class, mistBox, LivingEntity::isAlive)) {
                                if (suffocating == owner || isProtectedFromOffensiveSpell(suffocating, owner, safeMist)) {
                                    continue;
                                }
                                suffocating.setAirSupply(Math.max(-20, suffocating.getAirSupply() - 75));
                                suffocating.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                                suffocating.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
                                suffocating.invulnerableTime = 0;
                                hurtWithPureMateria(srv, suffocating, srv.damageSources().inWall(), scaldTickDmg);
                            }
                        }
                        return true;
                    }
            ));
            level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(0.65D), target.getZ(), 26, 0.55D, 0.50D, 0.55D, 0.06D);
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, target.getX(), target.getY(0.6D), target.getZ(), 14, 0.35D, 0.35D, 0.35D, 0.03D);
            level.playSound(null, target.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.15F);
        }
        // 6. Molten Slag (Earth + Nether — Guide Sec 1.2B: sunder + burning merge into clinging molten lava melting innate armor for combat!)
        else if ((MateriaFamilyTier.isEarthLineage(previous) && MateriaFamilyTier.isNetherLineage(incoming))
                || (MateriaFamilyTier.isNetherLineage(previous) && MateriaFamilyTier.isEarthLineage(incoming))) {
            target.igniteForSeconds(6);
            data.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, Math.max(now + 120L,
                    data.getLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL).orElse(0L)));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
            // Permanently melt 35% of innate physical armor & 25% toughness for the rest of combat (10 minutes)!
            applyPercentageArmorShred(target, level, 0.35D, 0.25D, 12_000L);
            applyTimedAttributeShred(target, level, 3.0D, 1.0D, 12_000L);
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                    level, target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D),
                    new Vec3(0.0D, 1.0D, 0.0D), EssenceType.NETHER, EssenceType.EARTH, 1.55F, 3
            );
            level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY(0.45D), target.getZ(), 16, 0.35D, 0.35D, 0.35D, 0.06D);
            level.playSound(null, target.blockPosition(), SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.PLAYERS, 0.85F, 0.90F);
        }
        // 7. Entangling Miasma (Nature/Spore + Undead/Blight — Guide Sec 1.2B: roots and rot combine into virulent infection
        //    that prevents mob health regeneration and spreads to adjacent mobs upon death!)
        else if ((isNatureLine(previous) && isNecroticLine(incoming))
                || (isNecroticLine(previous) && isNatureLine(incoming))) {
            target.removeEffect(MobEffects.REGENERATION);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
            data.putLong(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_UNTIL, now + 180L);
            data.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, target.getHealth());
            SpellWorldVfxManager.broadcast(
                    level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_RADIAL_NOVA,
                    target.position().add(0.0D, 0.15D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    EssenceType.UNDEAD, EssenceType.NATURE, 3.2F, 54, 3
            );
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY(0.55D), target.getZ(), 20, 0.45D, 0.45D, 0.45D, 0.05D);
            level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY(0.65D), target.getZ(), 10, 0.35D, 0.35D, 0.35D, 0.03D);
            level.playSound(null, target.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.65F, 0.90F);
        }
        // 8. Gravitational Collapse (Void + Lightning — Guide Sec 1.2B: singularity suction combined with static electricity
        //    pulls all nearby mobs into a tight cluster and discharges an electric blast through all of them simultaneously!)
        else if ((MateriaFamilyTier.isVoidLineage(previous) && isLightningLine(incoming))
                || (isLightningLine(previous) && MateriaFamilyTier.isVoidLineage(incoming))) {
            Vec3 collapseCenter = target.position();
            Vec3 arcOrigin = collapseCenter.add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
            SpellWorldVfxManager.broadcastVacuumSphere(
                    level, collapseCenter, 4.8D, true, EssenceType.VOID, EssenceType.LIGHTNING, 32, 3
            );
            SpellWorldVfxManager.broadcast(
                    level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_RADIAL_NOVA,
                    collapseCenter.add(0.0D, 0.12D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    EssenceType.VOID, EssenceType.LIGHTNING, 4.6F, 36, 3
            );

            List<LivingEntity> clustered = level.getEntitiesOfClass(
                    LivingEntity.class, target.getBoundingBox().inflate(5.2D), LivingEntity::isAlive);
            var paralyzedHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get());
            for (LivingEntity e : clustered) {
                if (e == caster || isProtectedFromOffensiveSpell(e, caster, wardSafeguard)) {
                    continue;
                }
                if (e != target && !isBossEntity(level, e)) {
                    Vec3 toCluster = collapseCenter.subtract(e.position());
                    double dist = toCluster.length();
                    if (dist > 0.35D) {
                        Vec3 pull = toCluster.scale(1.0D / dist).scale(Math.min(1.45D, 0.50D + dist * 0.22D));
                        SpellExecutionEngine.applySyncedMotion(e, new Vec3(pull.x, 0.26D, pull.z));
                    }
                    SpellWorldVfxManager.broadcastChainArc(
                            level, arcOrigin, e.position().add(0.0D, e.getBbHeight() * 0.55D, 0.0D),
                            EssenceType.LIGHTNING, EssenceType.VOID, 3
                    );
                }
                e.invulnerableTime = 0;
                float dischargeMult = (e == target) ? 0.65F : 0.55F;
                hurtWithPureMateria(level, e, reactionSrc, Math.max(2.5F, triggerDamage * dischargeMult));
                if (!isBossEntity(level, e)) {
                    e.addEffect(new MobEffectInstance(paralyzedHolder, 16, 0));
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, e.getX(), e.getY(0.6D), e.getZ(),
                        12, 0.28D, 0.35D, 0.28D, 0.12D);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, e.getX(), e.getY(0.55D), e.getZ(),
                        8, 0.25D, 0.30D, 0.25D, 0.06D);
            }
            level.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.35F);
            level.playSound(null, target.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.85F, 0.68F);
        } else {
            triggered = false;
        }
        } finally {
            REENTRANT_ELEMENTAL_REACTION.set(false);
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
        return applyPersistentHexInternal(
                level, caster, target, hexMateria, potencyTier, requestedTicks, hasWardSafeguard, mutated, true, -1L
        );
    }

    /**
     * Applies Death Contagion (Section 5.6) strictly at <b>Level I</b> without ever incrementing an already-hexed
     * neighbor to Level II or Level III, and preserving the original activation window rather than resetting it.
     */
    public static boolean applyPersistentHexContagionLevelOne(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            EssenceType hexMateria,
            int potencyTier,
            boolean mutated,
            long inheritedInitialWindowUntil
    ) {
        return applyPersistentHexInternal(
                level, caster, target, hexMateria, potencyTier, 300, false, mutated, false, inheritedInitialWindowUntil
        );
    }

    private static boolean applyPersistentHexInternal(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            EssenceType hexMateria,
            int potencyTier,
            int requestedTicks,
            boolean hasWardSafeguard,
            boolean mutated,
            boolean allowStackIncrement,
            long inheritedInitialWindowUntil
    ) {
        if (isProtectedFromOffensiveSpell(target, caster, hasWardSafeguard)) {
            return false;
        }

        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        boolean isBoss = isBossEntity(level, target);
        EssenceType resolved = hexMateria == null ? EssenceType.UMBRAL : hexMateria;

        // If an earlier Hex already expired between 5-tick sweeps, finalize its expiry (and stamp boss post-expiry immunity) first!
        long existingUntil = data.getLong(TAG_HEX_UNTIL).orElse(0L);
        String existingType = data.getString(TAG_HEX_TYPE).orElse("");
        if (existingUntil > 0L && now > existingUntil && !existingType.isEmpty()) {
            clearHexData(target, data);
            existingUntil = 0L;
            existingType = "";
        }

        if (isBoss && isBossImmuneToHex(data, now, resolved)) {
            return false;
        }

        // Death Contagion strictly applies Level I and never increments an already-hexed neighbor of the same element!
        if (!allowStackIncrement && now <= existingUntil && resolved.name().equals(existingType)) {
            return false;
        }

        // If overwriting an active DIFFERENT Hex element (or unmutating), clean up the replaced Hex's modifiers/tags first!
        boolean hadActiveDifferentHex = now <= existingUntil && !existingType.isEmpty() && !resolved.name().equals(existingType);
        boolean hadActiveMutatedDowngrade = now <= existingUntil && resolved.name().equals(existingType)
                && data.getBoolean(TAG_HEX_MUTATED).orElse(false) && !mutated;
        if (hadActiveDifferentHex || hadActiveMutatedDowngrade) {
            cleanupHexOverwriteState(target, data, now, isBoss && hadActiveDifferentHex, existingType);
        }

        // Hex Stacking (Section 5.6): re-casting the same Hex Materia stacks intensity up to Level III (max 3), including on bosses!
        int stack = computeNextHexStackLevel(data, now, resolved, allowStackIncrement);

        long durationTicks;
        if (isBoss) {
            // Section 2.5.1 / 5.6 Boss Tenacity Cap: 30s (600t) active window (or 15s / 300t active tenacity on Dragon/Wither
            // for non-Tier-5+ Hexes). Post-expiry 10.0s (200t) boss immunity is stamped ONLY when the Hex expires or is cleansed!
            durationTicks = resolveBossHexDurationCapTicks(target, resolved);
        } else {
            // Section 5.6 Indefinite Persistence on standard mobs and players until purified, counter-cleansed, or caster death!
            durationTicks = INDEFINITE_HEX_TICKS;
        }

        data.putString(TAG_HEX_TYPE, resolved.name());
        data.putInt(TAG_HEX_TIER, Math.clamp(potencyTier, 1, 10));
        data.putInt(TAG_HEX_STACK, stack);
        data.putBoolean(TAG_HEX_MUTATED, mutated);
        data.putLong(TAG_HEX_UNTIL, now + durationTicks);
        data.putLong(TAG_HEX_APPLIED_TICK, now);
        data.putLong(TAG_HEX_INITIAL_WINDOW, inheritedInitialWindowUntil >= 0L ? inheritedInitialWindowUntil : (now + 200L));
        if (caster != null) {
            data.putString(TAG_HEX_CASTER_UUID, caster.getUUID().toString());
        } else {
            data.remove(TAG_HEX_CASTER_UUID);
        }

        // Always strip MOD_HEX_MAX_HEALTH_CUT_PCT first so overwriting Miasma/Genesis/Oblivion/Chimera/Entropic never leaks!
        AttributeInstance maxHp = target.getAttribute(Attributes.MAX_HEALTH);
        if (maxHp != null) {
            maxHp.removeModifier(MOD_HEX_MAX_HEALTH_CUT_PCT);
            if (usesHexMaxHealthCut(resolved, mutated)) {
                double maxHpCut = resolved == EssenceType.ENTROPIC ? -0.30D : -0.25D;
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
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_LAST_POS_X, target.getX());
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_LAST_POS_Y, target.getY());
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_LAST_POS_Z, target.getZ());
            data.putLong("SpellVoidLastRubberbandTick", now);
        }

        if (mutated && resolved == EssenceType.AIR) {
            // Section 6.2 Hypoxia Suffocation Hex: immediate on-cast -40% attack speed, -40% stamina movement drag,
            // Mining Fatigue II, sprint exhaustion, and immediate air-supply / dry-land suffocation with zero 1.0s delay!
            float mult = getHexStackMultiplier(target);
            int mfAmp = Math.max(1, stack - 1);
            applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);
            applyTimedMovementSpeedSap(target, level, Math.min(0.75D, 0.40D * mult), 45L);
            target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 45, mfAmp, true, false));
            target.setAirSupply(Math.max(0, target.getAirSupply() - Math.round(55.0F * mult)));
            if (target instanceof Player hypPlayer && !hypPlayer.isCreative() && !hypPlayer.isSpectator() && hypPlayer.isSprinting()) {
                hypPlayer.causeFoodExhaustion(0.85F * mult);
            }
            if (!target.isInWater()) {
                float initHypoxiaDmg = Math.max(1.5F, ddraig.net.entropica.spell.fuel.MateriaStageTable.getBaseCombatDamageHp(potencyTier) * 0.12F) * mult;
                boolean prevReentrant = REENTRANT_GUST_HURT.get();
                REENTRANT_GUST_HURT.set(true);
                try {
                    target.hurtServer(level, level.damageSources().drown(), initHypoxiaDmg);
                } finally {
                    REENTRANT_GUST_HURT.set(prevReentrant);
                }
            }
        } else if (resolved == EssenceType.WATER) {
            // Section 6.3 Waterlogged Drowning Hex: immediately douse pre-existing flames (preventing tick-1 evaporation),
            // stamp soaking wetness, reduce air supply, and deal the initial land-drowning tick with zero delay!
            data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
            data.remove(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL);
            target.clearFire();
            if (mutated) {
                float mult = getHexStackMultiplier(target);
                SpellLingeringZoneManager.applySoaked(target, 60);
                target.setAirSupply(Math.max(-20, target.getAirSupply() - Math.round(25.0F * mult)));
                if (!target.isInWater()) {
                    float initDrownDmg = (1.5F + potencyTier * 0.5F) * mult;
                    boolean prevReentrant = REENTRANT_GUST_HURT.get();
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        target.hurtServer(level, level.damageSources().drown(), initDrownDmg);
                    } finally {
                        REENTRANT_GUST_HURT.set(prevReentrant);
                    }
                    level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY(0.85D), target.getZ(),
                            6, 0.25D, 0.3D, 0.25D, 0.05D);
                }
            }
        } else if (mutated && resolved == EssenceType.NATURE) {
            // Section 6.4 Silencing Spore Hex (A5): immediate 0s-delay setup stamping Arcane Silence and shredding
            // 20% (Level I), 35% (Level II), or 50% (Level III) Armor Toughness with 0 HP tick damage!
            SpellLingeringZoneManager.applyArcaneSilence(target, 45);
            data.putLong("SpellSilencedUntil", Math.max(now + 45L, data.getLong("SpellSilencedUntil").orElse(0L)));
            if (target.isUsingItem()) {
                target.stopUsingItem();
            }
            if (target instanceof Mob mob) {
                mob.setAggressive(false);
            }
            double toughnessShredPct = switch (stack) {
                case 1 -> 0.20D;
                case 2 -> 0.35D;
                default -> 0.50D;
            };
            applyPercentageArmorShred(target, level, 0.0D, toughnessShredPct, 45L);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xB8FF52, 28, 1.0F, 0.05F),
                    target.getX(), target.getY(0.85D), target.getZ(), 6, 0.28D, 0.35D, 0.28D, 0.015D);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    target.getX(), target.getY(0.95D), target.getZ(), 4, 0.25D, 0.30D, 0.25D, 0.015D);
        } else if (mutated && resolved == EssenceType.EARTH) {
            // Section 6.5 Sundering Hex: immediate 0s-delay setup fracturing physical armor by 5.0 / 7.5 / 10.0 across Levels I–III (0 tick damage)!
            applyTimedAttributeShred(target, level, resolveSunderingHexArmorShred(stack), 0.0D, 65L);
        } else if (mutated && resolved == EssenceType.FROZEN) {
            // Section 6.6 Permafrost Stasis Hex: immediate 0s-delay exact 60% movement slow + sprint lock (0 tick damage, no compounding Slowness)!
            target.setSprinting(false);
            applyTimedMovementSpeedSap(target, level, 0.60D, 65L);
        } else if (mutated && resolved == EssenceType.NETHER) {
            // Section 6.7 Hellfire Sear Hex: immediate 0s-delay 30% / 45% / 60% physical armor melt + underwater unquenchable burn + natural regen lock!
            target.removeEffect(MobEffects.REGENERATION);
            target.igniteForSeconds(5);
            data.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL,
                    Math.max(now + 65L, data.getLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL).orElse(0L)));
            applyPercentageArmorShred(target, level, resolveHellfireSearArmorMeltFraction(stack), 0.0D, 65L);
        } else if (mutated && resolved == EssenceType.RADIANT) {
            // Section 6.8 Sun-Scorched Smite Hex: immediate 0s-delay 4.0 / 6.0 / 8.0 physical armor melt across Levels I–III,
            // Glowing + Invisibility/Umbral Shroud strip, and natural health regeneration lock right on cast!
            target.removeEffect(MobEffects.REGENERATION);
            target.removeEffect(MobEffects.INVISIBILITY);
            data.remove(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL);
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 65, 0, true, false));
            applyTimedAttributeShred(target, level, resolveSunScorchedHexArmorMelt(stack), 0.0D, 65L);
            if (target.getType().is(EntityTypeTags.UNDEAD) || target instanceof Phantom) {
                target.igniteForSeconds(3);
            }
        } else if (mutated && resolved == EssenceType.ARID) {
            // Section 6.12 Desiccation Mummification Hex: immediately parch the victim with Hunger III & Slowness II!
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 45, 2, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1, true, true));
            if (target instanceof Player cursedPlayer) {
                cursedPlayer.getFoodData().setSaturation(0.0F);
            }
        } else if (mutated && resolved == EssenceType.LIGHTNING) {
            // Section 6.13 Galvanic Overcharge Hex: immediately cut movement & attack speed by 40% and disable shield blocking!
            applyTimedMovementSpeedSap(target, level, 0.40D, 45L);
            applyTimedAttackDebuff(target, level, 0.0D, 0.40D, 45L);
            if (target instanceof Player p && p.isBlocking()) {
                ItemStack shield = p.getUseItem();
                p.stopUsingItem();
                if (!shield.isEmpty()) {
                    p.getCooldowns().addCooldown(shield, 100);
                }
            }
        } else if (mutated && resolved == EssenceType.MAGMA) {
            // Section 6.14 Molten Slag Encasement Hex: immediately encase in unquenchable fire (even underwater),
            // shred 40% Armor Toughness, and cut movement speed by 30%!
            float mult = getHexStackMultiplier(target);
            target.igniteForSeconds(4);
            data.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 45L);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);
            applyTimedMovementSpeedSap(target, level, Math.min(0.75D, 0.30D * mult), 45L);
        } else if (mutated && resolved == EssenceType.STORM) {
            // Section 6.15 Waterlogged Hypoxia Hex: immediately soak victim, drain air bubbles, and cut attack speed by 40%!
            float mult = getHexStackMultiplier(target);
            target.clearFire();
            SpellLingeringZoneManager.applySoaked(target, 60);
            applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);
        } else if (mutated && resolved == EssenceType.DUST) {
            // Section 6.18 Choking Powder Smother Hex: immediately blind, silence spellcasting & mob abilities, and apply Armor Erosion!
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, true, true));
            SpellLingeringZoneManager.applyArcaneSilence(target, 50);
            ddraig.net.entropica.spell.mutation.element.DustSpellMutationHandler.stackArmorErosionPublic(target, Math.max(1, stack), 80);
        } else if (mutated && resolved == EssenceType.SPORE) {
            // Section 6.19 Hallucinatory Mycelial Hex: immediately inflict Nausea, confuse allegiance onto nearest entity, and cough poison spores!
            target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0, true, true));
            ddraig.net.entropica.spell.mutation.element.SporeSpellMutationHandler.confuseAllegianceAndCoughSporesPublic(
                    level, target, caster, potencyTier
            );
        } else if (mutated && resolved == EssenceType.TAIGA) {
            // Section 6.20 Petrifying Frost-Sap Hex: immediately crystallize joints with Freezing, Slowness III & anti-sprint!
            target.setSprinting(false);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2, true, true));
            if (target.canFreeze()) {
                target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 40, target.getTicksFrozen() + 60));
            }
        } else if (mutated && resolved == EssenceType.OASIS) {
            // Section 6.21 Dehydrating Sunstroke Hex: immediately drain hunger/saturation & cut attack speed by 35%!
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 1, true, true));
            applyTimedAttackDebuff(target, level, 0.0D, 0.35D * stack, 60L);
            if (target instanceof Player sunPlayer) {
                sunPlayer.causeFoodExhaustion(2.0F * stack);
            }
        } else if (mutated && resolved == EssenceType.VITAE) {
            // Section 6.22 Hyper-Metabolic Arrhythmia Hex: immediately strip Regeneration & cut attack damage by 40%!
            target.removeEffect(MobEffects.REGENERATION);
            applyTimedAttackDebuff(target, level, Math.min(0.85D, 0.40D * getHexStackMultiplier(target)), 0.0D, 60L);
            level.playSound(null, target.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.85F, 1.25F);
        } else if (mutated && resolved == EssenceType.BLOOD) {
            // Section 6.23 Sanguine Exsanguination Hex: immediately inflict severe Hemorrhage & prevent blood coagulation!
            target.removeEffect(MobEffects.REGENERATION);
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BLEEDING.get()), 60, 1));
            data.putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, now + 60L);
            data.putInt("SpellBloodHemorrhageAmp", 1);
            data.putInt("SpellBloodHemorrhageTier", potencyTier);
        } else if (mutated && resolved == EssenceType.AURA) {
            // Section 6.24 Chakra Severance Hex: immediately sever spiritual channels (silencing abilities & spellcasting)!
            SpellLingeringZoneManager.applyArcaneSilence(target, 60);
        } else if (mutated && resolved == EssenceType.AMBER) {
            // Section 6.25 Petrifying Amber Stasis Hex: immediately inflict 70% Slowness, block sprint/jump, shred 40% toughness & purge buffs!
            float mult = getHexStackMultiplier(target);
            target.setSprinting(false);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3, true, true));
            applyTimedMovementSpeedSap(target, level, 0.70D, 60L);
            data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 60L);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 60L);
            List<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>> ben = new ArrayList<>();
            for (MobEffectInstance eff : target.getActiveEffects()) {
                if (eff.getEffect().value().isBeneficial()) ben.add(eff.getEffect());
            }
            for (var h : ben) target.removeEffect(h);
        } else if (mutated && resolved == EssenceType.ECLIPSE) {
            // Section 6.26 Coronal Blind-Rot Hex: immediately inflict simultaneous Blindness & Glowing and dissolve 3 physical armor points!
            float mult = getHexStackMultiplier(target);
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, true));
            applyTimedAttributeShred(target, level, 3.0D * mult, 0.0D, 60L);
        } else if (mutated && resolved == EssenceType.BLIGHT) {
            // Section 6.27 Virulent Wither-Blight Hex: immediately shred 40% armor toughness & inflict virulent rot!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 60L);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1, true, true));
        } else if (mutated && resolved == EssenceType.ASTRAL) {
            // Section 6.28 Gravitational Singularity Tether Hex: immediately crush speed by 70% & disable sprinting/jumping!
            applyTimedMovementSpeedSap(target, level, 0.70D, 60L);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3, true, true));
            target.setSprinting(false);
            data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 60L);
        } else if (mutated && resolved == EssenceType.SOULFIRE) {
            // Section 6.29 Phantasmal Soul-Burn Hex: immediately ignite in unquenchable underwater soulfire + Slowness II, Weakness II & Hunger!
            target.igniteForSeconds(5);
            data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, now + 80L);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 1, true, true));
        } else if (mutated && resolved == EssenceType.VAPOR) {
            // Section 6.30 Scalding Dissolution Hex: immediately dissolve 2 armor points, slow 30% & soak!
            data.putInt("VaporHexDissolveSteps", 0);
            applyTimedAttributeShred(target, level, 2.0D * getHexStackMultiplier(target), 0.0D, 60L);
            applyTimedMovementSpeedSap(target, level, 0.30D, 60L);
            SpellLingeringZoneManager.applySoaked(target, 60);
        } else if (mutated && resolved == EssenceType.NULL_R) {
            // Section 6.31 Antimatter Annihilation Hex: immediately melt 2 armor points, purge positive buffs & disable shield!
            data.putInt("NullRHexMeltSteps", 0);
            applyTimedAttributeShred(target, level, 2.0D * getHexStackMultiplier(target), 0.0D, 60L);
            ddraig.net.entropica.spell.mutation.element.EclipseSpellMutationHandler.purgeBeneficialEffects(target);
            if (target.isBlocking()) {
                target.stopUsingItem();
            }
        } else if (mutated && resolved == EssenceType.NULL_U) {
            // Section 6.32 Sensory Blackout Hex: immediately inflict total Blindness & Silence, -40% movement speed & wipe mob aggro!
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, true, true));
            SpellLingeringZoneManager.applyArcaneSilence(target, 60);
            applyTimedMovementSpeedSap(target, level, 0.40D, 60L);
            target.setLastHurtByMob(null);
            if (target instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
        } else if (mutated && resolved == EssenceType.DAWN) {
            // Section 6.33 Solar Blind-Sear Hex: immediately brand with Blindness & Glowing, melt 3 armor points & ignite undead!
            target.removeEffect(MobEffects.INVISIBILITY);
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, true));
            applyTimedAttributeShred(target, level, 3.0D * getHexStackMultiplier(target), 0.0D, 60L);
            if (target.getType().is(EntityTypeTags.UNDEAD)) {
                target.igniteForSeconds(4);
            }
        } else if (mutated && resolved == EssenceType.ABYSS) {
            // Section 6.34 Crushing Benthic Drowning Hex: immediately inflict land-drowning, -40% movement speed, Silence & Soaked!
            SpellLingeringZoneManager.applyAbyssLandDrowning(target, 60);
            SpellLingeringZoneManager.applyArcaneSilence(target, 60);
            SpellLingeringZoneManager.applySoaked(target, 100);
            applyTimedMovementSpeedSap(target, level, 0.40D, 60L);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, true));
        } else if (mutated && resolved == EssenceType.AEGIS) {
            // Section 6.35 Petrifying Citadel Sundering Hex: immediately shred 40% armor toughness, -40% movement speed & block jumping!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.20D * mult, Math.min(0.85D, 0.40D * mult), 60L);
            applyTimedMovementSpeedSap(target, level, 0.40D, 60L);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, true));
            data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 60L);
        } else if (mutated && resolved == EssenceType.AURORA) {
            // Section 6.36 Sub-Zero Polar Stasis Hex: immediately cut attack speed by 50%, slash movement speed by 40%, freeze & inflict Glowing!
            applyTimedAttackDebuff(target, level, 0.0D, 0.50D, 60L);
            applyTimedMovementSpeedSap(target, level, 0.40D, 60L);
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, true));
            if (target.canFreeze()) {
                target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 60, target.getTicksFrozen() + 60));
            }
        } else if (mutated && resolved == EssenceType.MIRAGE) {
            // Section 6.37 Delusional Phantasm Paranoia Hex: immediately shred 40% armor toughness, inflict Nausea & blind target to caster!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 60L);
            target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0, true, true));
            if (target instanceof Mob m && m.getTarget() == caster) {
                m.setTarget(null);
                m.setAggressive(false);
            }
        } else if (mutated && resolved == EssenceType.WRAITH) {
            // Section 6.38 Incorporeal Soul-Hollowing Hex: immediately halve damage dealt (-50%) & cut movement speed by 40%!
            float mult = getHexStackMultiplier(target);
            applyTimedAttackDebuff(target, level, Math.min(0.85D, 0.50D * mult), 0.0D, 60L);
            applyTimedMovementSpeedSap(target, level, 0.40D, 60L);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, true));
        } else if (mutated && resolved == EssenceType.BARROW) {
            // Section 6.39 Petrifying Sepulchral Entombment Hex: immediately cut move & attack speed by 40%, shred 40% toughness & block jumping!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 60L);
            applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 60L);
            applyTimedMovementSpeedSap(target, level, 0.40D, 60L);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, true));
            data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 60L);
        } else if (mutated && resolved == EssenceType.PYRE) {
            // Section 6.40 Unquenchable Wildfire Cremation Hex: immediately ignite with underwater coal fire, burn 40% toughness & block sprinting!
            float mult = getHexStackMultiplier(target);
            data.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 80L);
            target.igniteForSeconds(5);
            target.setSprinting(false);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 60L);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0, true, true));
        } else if (mutated && resolved == EssenceType.PENUMBRA) {
            // Section 6.41 Penumbral Contrast-Collapse Hex: immediately collapse vision, disrupt AI pathfinding & strip 35% toughness!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 60L);
            SpellLingeringZoneManager.applyAiPathDisruption(target, 80);
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, true, true));
        } else if (mutated && resolved == EssenceType.RIME) {
            // Section 6.42 Sub-Zero Cryo-Numbness Hex: immediately strip 30% toughness, freeze soul & trigger initial 0.5s shiver stun!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.30D * mult), 60L);
            target.setTicksFrozen(target.getTicksRequiredToFreeze() + 60);
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 10, 0, true, true));
        } else if (mutated && resolved == EssenceType.SPRING) {
            // Section 6.43 Uncontrolled Vernal Overgrowth Hex: immediately strip 35% toughness, -50% move speed, -40% attack speed & 1.0s vine root!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 60L);
            applyTimedMovementSpeedSap(target, level, Math.min(0.80D, 0.50D * mult), 60L);
            applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 60L);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2, true, true));
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0, true, true));
            SpellLingeringZoneManager.spawnCollectableHealingOrb(level, target.position(), caster, 2.0F, false);
        } else if (mutated && resolved == EssenceType.STATIC) {
            // Section 6.44 Galvanic Nerve-Spasm Hex: immediately strip 35% toughness, -50% move speed, -40% attack speed & interrupt bow/spell!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 60L);
            applyTimedMovementSpeedSap(target, level, Math.min(0.80D, 0.50D * mult), 60L);
            applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 60L);
            target.stopUsingItem();
            SpellLingeringZoneManager.applyArcaneSilence(target, 25);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2, true, true));
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 12, 0, true, true));
        } else if (mutated && resolved == EssenceType.SYLVAN) {
            // Section 6.45 Petrifying Arbor-Root Entombment Hex: immediately strip 35% toughness, -60% move speed, -40% attack speed, 1.0s root & golden acorn orb!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 60L);
            applyTimedMovementSpeedSap(target, level, Math.min(0.85D, 0.60D * mult), 60L);
            applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 60L);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3, true, true));
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0, true, true));
            SpellLingeringZoneManager.spawnCollectableHealingOrb(level, target.position(), caster, 2.0F, true);
        } else if (mutated && resolved == EssenceType.MIASMA) {
            // Section 6.46 Virulent Septic Pestilence Hex: immediately shred 35% toughness and splash a 2-block slowing caustic swamp bile puddle!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 80L);
            level.sendParticles(ParticleTypes.SNEEZE, target.getX(), target.getY(0.75D), target.getZ(), 10, 0.45D, 0.25D, 0.45D, 0.02D);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY(0.25D), target.getZ(), 12, 0.90D, 0.12D, 0.90D, 0.01D);
            for (LivingEntity splash : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.0D), LivingEntity::isAlive)) {
                if (caster != null && (splash == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, splash))) continue;
                splash.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, true));
            }
        } else if (mutated && resolved == EssenceType.GENESIS) {
            // Section 6.47 Hyper-Cellular Genesis Destabilization Hex: immediately shred 35% toughness, stagger attacks & expel radiant healing sparks!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 80L);
            target.stopUsingItem();
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 10, 0, true, true));
            level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.9D), target.getZ(), 10, 0.35D, 0.4D, 0.35D, 0.04D);
        } else if (mutated && resolved == EssenceType.OBLIVION) {
            // Section 6.48 Stygian Oblivion-Extinction Hex: immediately shred 40% toughness, -35% attack damage & ignite unquenchable black hellfire!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 80L);
            applyTimedAttackDebuff(target, level, Math.min(0.80D, 0.35D * mult), 0.0D, 80L);
            EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 80L);
            target.igniteForSeconds(4);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY(0.85D), target.getZ(), 12, 0.35D, 0.4D, 0.35D, 0.03D);
        } else if (mutated && resolved == EssenceType.AETHER) {
            // Section 6.50 Apex Gravitational Collapse Hex: immediately shred 50% toughness, force -80% crawl & block jumping!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.50D * mult), 80L);
            applyTimedMovementSpeedSap(target, level, Math.min(0.90D, 0.80D * mult), 80L);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 4, true, true));
            data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 80L);
            target.setDeltaMovement(target.getDeltaMovement().x * 0.2D, Math.min(0.0D, target.getDeltaMovement().y - 0.55D), target.getDeltaMovement().z * 0.2D);
            target.hasImpulse = true;
            level.sendParticles(
                    ddraig.net.entropica.client.particle.TimedTintableParticleOption.linear(
                            ddraig.net.entropica.registry.ModParticles.TINTABLE_SHIELD.get(), 0xA88654, 24, 1.05F, 0.04F),
                    target.getX(), target.getY(0.35D), target.getZ(), 10, 0.40D, 0.15D, 0.40D, 0.03D
            );
        } else if (mutated && resolved == EssenceType.ENTROPIC) {
            // Section 6.51 Apex Thermodynamic Decay & Exsanguination Hex: immediately shred 50% toughness & trigger crimson hemorrhage!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.50D * mult), 80L);
            data.putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, now + 80L);
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BLEEDING.get()), 80, 0, true, true));
            level.sendParticles(
                    ddraig.net.entropica.client.particle.TimedTintableParticleOption.linear(
                            ddraig.net.entropica.registry.ModParticles.TINTABLE_BLOOD.get(), 0x8A0303, 24, 1.0F, 0.05F),
                    target.getX(), target.getY(0.8D), target.getZ(), 10, 0.35D, 0.4D, 0.35D, 0.03D
            );
        } else if (mutated && resolved == EssenceType.CELESTIAL) {
            // Section 6.52 Divine Genesis Brand / Holy Supernova Overload Hex: immediately shred 40% toughness, purge positive buffs & blind!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 80L);
            ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.purgeBeneficialBuffs(target);
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, true, true));
            level.sendParticles(
                    ddraig.net.entropica.client.particle.TimedTintableParticleOption.linear(
                            ddraig.net.entropica.registry.ModParticles.TINTABLE_SPARKLE.get(), 0xFFEA78, 24, 1.0F, 0.05F),
                    target.getX(), target.getY(0.85D), target.getZ(), 12, 0.35D, 0.4D, 0.35D, 0.03D
            );
        } else if (mutated && resolved == EssenceType.EMPYREAN) {
            // Section 6.54 Radiant Empyrean Brand Hex: immediately shatter 50% armor toughness, disable potion/natural recovery & starlight blind!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.50D * mult), 80L);
            target.removeEffect(MobEffects.REGENERATION);
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, true, true));
            level.sendParticles(
                    ddraig.net.entropica.client.particle.TimedTintableParticleOption.linear(
                            ddraig.net.entropica.registry.ModParticles.TINTABLE_SPARKLE.get(), 0xFFEA78, 26, 1.05F, 0.05F),
                    target.getX(), target.getY(0.85D), target.getZ(), 14, 0.38D, 0.42D, 0.38D, 0.03D
            );
        } else if (mutated && resolved == EssenceType.CATACLYSM) {
            // Section 6.55 Abyssal World-Breaker Singularity Hex: immediately shred 50% armor toughness, cut movement speed by exact 70%,
            // block jumping, crush downward with inward gravity, & ignite unquenchable void-nether fire!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.50D * mult), 80L);
            applyTimedMovementSpeedSap(target, level, Math.min(0.90D, 0.70D * mult), 80L);
            data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 80L);
            data.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 80L);
            target.igniteForSeconds(5);
            target.setDeltaMovement(target.getDeltaMovement().x * 0.2D, Math.min(0.0D, target.getDeltaMovement().y - 0.50D), target.getDeltaMovement().z * 0.2D);
            target.hasImpulse = true;
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.85D), target.getZ(), 14, 0.38D, 0.42D, 0.38D, 0.04D);
            level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY(0.65D), target.getZ(), 8, 0.30D, 0.30D, 0.30D, 0.03D);
        } else if (mutated && resolved == EssenceType.TERMINUS) {
            // Section 6.56 Absolute Zero Molecular Cessation Hex: immediately shred 50% armor toughness, slow movement by exact 80%,
            // cut attack speed by exact 60%, disable all jumping, & deep-freeze molecular motion!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.50D * mult), 80L);
            applyTimedMovementSpeedSap(target, level, Math.min(0.92D, 0.80D * mult), 80L);
            applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.85D, 0.60D * mult), 80L);
            data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 80L);
            target.clearFire();
            target.setTicksFrozen(Math.max(target.getTicksFrozen(), target.getTicksRequiredToFreeze() + 80));
            level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.85D), target.getZ(), 16, 0.38D, 0.42D, 0.38D, 0.03D);
        } else if (mutated && resolved == EssenceType.ESCHATON) {
            // Section 6.57 Omega Event Horizon Dissolution Hex: immediately shred 60% armor toughness, slow movement by exact 85%,
            // block jumping & teleportation, and crush downward into the collapsing event horizon!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.92D, 0.60D * mult), 80L);
            applyTimedMovementSpeedSap(target, level, Math.min(0.94D, 0.85D * mult), 80L);
            data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 80L);
            target.setDeltaMovement(target.getDeltaMovement().x * 0.15D, Math.min(0.0D, target.getDeltaMovement().y - 0.55D), target.getDeltaMovement().z * 0.15D);
            target.hasImpulse = true;
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.85D), target.getZ(), 18, 0.40D, 0.45D, 0.40D, 0.05D);
        } else if (mutated && resolved == EssenceType.APOTHEOSIS) {
            // Section 6.58 Divine Genesis Brand Hex: immediately shred 60% armor toughness, purge regeneration,
            // sear with blinding white-hole cosmic life-fire, and lock health recovery!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.92D, 0.60D * mult), 80L);
            target.removeEffect(MobEffects.REGENERATION);
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, true, false));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, true, false));
            target.igniteForSeconds(5);
            level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.85D), target.getZ(), 20, 0.40D, 0.45D, 0.40D, 0.05D);
        } else if (mutated && resolved == EssenceType.ENTROPICA) {
            // Section 6.59 Grand Equilibrium Paradox Hex: immediately shred 60% armor toughness, slow movement by exact 85%,
            // purge regeneration, and lock victim in absolute thermodynamic collapse!
            float mult = getHexStackMultiplier(target);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.92D, 0.60D * mult), 80L);
            applyTimedMovementSpeedSap(target, level, Math.min(0.94D, 0.85D * mult), 80L);
            target.removeEffect(MobEffects.REGENERATION);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 3, true, false));
            target.setDeltaMovement(target.getDeltaMovement().x * 0.15D, Math.min(0.0D, target.getDeltaMovement().y - 0.50D), target.getDeltaMovement().z * 0.15D);
            target.hasImpulse = true;
            level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.85D), target.getZ(), 14, 0.38D, 0.42D, 0.38D, 0.04D);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.85D), target.getZ(), 14, 0.38D, 0.42D, 0.38D, 0.04D);
        }

        // Ensure baseline signature potion debuffs are immediately present on tick 0 so Anti-Milk preservation holds before the first 20-tick pulse!
        reapplyCanonicalHexPotionDebuffs(target, resolved, mutated, stack);

        if (mutated && MateriaFamilyTier.isMonsterEmpowermentHex(resolved) && !(target instanceof Player) && !isBoss) {
            if (resolved != EssenceType.ARID && resolved != EssenceType.TAIGA
                    && resolved != EssenceType.ABYSS && resolved != EssenceType.AEGIS
                    && resolved != EssenceType.TERMINUS) {
                target.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, stack - 1));
            }
            target.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 600, stack - 1));
            data.putBoolean("SpellHexBonusLoot", true);
        }

        return true;
    }

    public static boolean usesHexMaxHealthCut(@Nullable EssenceType hexMateria, boolean mutated) {
        return mutated && (hexMateria == EssenceType.MIASMA
                || hexMateria == EssenceType.GENESIS
                || hexMateria == EssenceType.OBLIVION
                || hexMateria == EssenceType.CHIMERA
                || hexMateria == EssenceType.ENTROPIC);
    }

    public static long resolveBossHexDurationCapTicks(@Nullable LivingEntity target, EssenceType resolved) {
        boolean thirtySecEschatonBossCap = resolved == EssenceType.EMPYREAN
                || resolved == EssenceType.CATACLYSM
                || resolved == EssenceType.TERMINUS
                || resolved == EssenceType.ESCHATON
                || resolved == EssenceType.APOTHEOSIS
                || resolved == EssenceType.ENTROPICA;
        return (!thirtySecEschatonBossCap && (target instanceof EnderDragon || target instanceof WitherBoss))
                ? BOSS_HEX_ACTIVE_TENACITY_CAP_TICKS
                : BOSS_HEX_TENACITY_CAP_TICKS;
    }

    public static boolean isBossImmuneToHex(CompoundTag data, long now, @Nullable EssenceType incomingHex) {
        if (incomingHex != null && now < data.getLong(TAG_HEX_BOSS_IMMUNE_PREFIX + incomingHex.name()).orElse(0L)) {
            return true;
        }
        long legacyImmuneUntil = data.getLong(TAG_HEX_BOSS_IMMUNE_UNTIL).orElse(0L);
        if (now < legacyImmuneUntil) {
            String scopedType = data.getString(TAG_HEX_BOSS_IMMUNE_TYPE).orElse("");
            return scopedType.isEmpty() || (incomingHex != null && scopedType.equals(incomingHex.name()));
        }
        return false;
    }

    public static void stampBossPostHexImmunity(CompoundTag data, long now, String expiredOrCleansedHexType, long previousUntil) {
        if (expiredOrCleansedHexType == null || expiredOrCleansedHexType.isEmpty()) {
            return;
        }
        long baseExpiry = (previousUntil > 0L && now > previousUntil) ? previousUntil : now;
        long immuneUntil = baseExpiry + BOSS_HEX_POST_IMMUNITY_TICKS;
        if (immuneUntil > now) {
            data.putLong(TAG_HEX_BOSS_IMMUNE_PREFIX + expiredOrCleansedHexType, immuneUntil);
            data.putLong(TAG_HEX_BOSS_IMMUNE_UNTIL, immuneUntil);
            data.putString(TAG_HEX_BOSS_IMMUNE_TYPE, expiredOrCleansedHexType);
        }
    }

    public static int computeNextHexStackLevel(CompoundTag data, long now, EssenceType resolved, boolean allowStackIncrement) {
        if (!allowStackIncrement) {
            return 1;
        }
        long existingUntil = data.getLong(TAG_HEX_UNTIL).orElse(0L);
        if (now <= existingUntil && resolved.name().equals(data.getString(TAG_HEX_TYPE).orElse(""))) {
            return Math.min(3, data.getInt(TAG_HEX_STACK).orElse(1) + 1);
        }
        return 1;
    }

    public static boolean hasAnyActivePersistentHex(CompoundTag data, long now) {
        String hexType = data.getString(TAG_HEX_TYPE).orElse("");
        long until = data.getLong(TAG_HEX_UNTIL).orElse(0L);
        return !hexType.isEmpty() && until > 0L && now <= until;
    }

    public static boolean hasAnyActivePersistentHex(LivingEntity target) {
        if (target == null || target.level() == null) {
            return false;
        }
        return hasAnyActivePersistentHex(EntityHelper.getPersistentData(target), target.level().getGameTime());
    }

    private static void cleanupHexOverwriteState(
            LivingEntity target,
            CompoundTag data,
            long now,
            boolean stampReplacedBossImmunity,
            String replacedHexType
    ) {
        if (stampReplacedBossImmunity && !replacedHexType.isEmpty()) {
            stampBossPostHexImmunity(data, now, replacedHexType, now);
        }
        data.remove("VaporHexDissolveSteps");
        data.remove("NullRHexMeltSteps");
        data.remove("SpellSilencedUntil");
        data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
        data.remove(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL);
        data.remove(SpellLingeringZoneManager.TAG_VOID_ANCHOR_X);
        data.remove(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Y);
        data.remove(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Z);
        data.remove(SpellLingeringZoneManager.TAG_VOID_LAST_POS_X);
        data.remove(SpellLingeringZoneManager.TAG_VOID_LAST_POS_Y);
        data.remove(SpellLingeringZoneManager.TAG_VOID_LAST_POS_Z);
        data.remove("SpellVoidLastRubberbandTick");
        data.remove(TAG_AIR_HYPOXIA_MELEE_CD_UNTIL);
        removeHexMaxHealthCutModifier(target);
        clearShredModifiers(target, data);
        clearOffenseShredModifiers(target, data);
    }

    private static void removeHexMaxHealthCutModifier(LivingEntity target) {
        AttributeInstance maxHp = target.getAttribute(Attributes.MAX_HEALTH);
        if (maxHp != null) {
            maxHp.removeModifier(MOD_HEX_MAX_HEALTH_CUT_PCT);
        }
    }

    /**
     * Snapshots potion effects owned by an active Persistent Materia Hex immediately before {@code removeAllEffects()}
     * or Milk Bucket consumption so normal non-hex potion effects are cured while Hex debuffs remain intact.
     */
    public static List<MobEffectInstance> captureHexEffectsBeforeClear(LivingEntity target) {
        if (!hasAnyActivePersistentHex(target)) {
            return List.of();
        }
        CompoundTag data = EntityHelper.getPersistentData(target);
        String typeStr = data.getString(TAG_HEX_TYPE).orElse("");
        EssenceType hexType;
        try {
            hexType = EssenceType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
        boolean hexMutated = data.getBoolean(TAG_HEX_MUTATED).orElse(true);
        List<MobEffectInstance> captured = new ArrayList<>();
        for (MobEffectInstance inst : target.getActiveEffects()) {
            if (doesHexOwnPotionEffect(target, inst.getEffect(), hexType, hexMutated)) {
                captured.add(new MobEffectInstance(inst));
            }
        }
        return captured;
    }

    /**
     * Immediately restores snapshotted Hex potion debuffs (and guarantees baseline Hex potion debuffs) on the same tick
     * before {@code removeAllEffects()} or Milk Bucket consumption returns.
     */
    public static void restoreHexEffectsAfterClear(LivingEntity target, List<MobEffectInstance> snapshot) {
        if (!hasAnyActivePersistentHex(target)) {
            return;
        }
        for (MobEffectInstance inst : snapshot) {
            target.addEffect(new MobEffectInstance(inst));
        }
        CompoundTag data = EntityHelper.getPersistentData(target);
        String typeStr = data.getString(TAG_HEX_TYPE).orElse("");
        try {
            EssenceType hexType = EssenceType.valueOf(typeStr);
            boolean hexMutated = data.getBoolean(TAG_HEX_MUTATED).orElse(true);
            int stack = Math.clamp(data.getInt(TAG_HEX_STACK).orElse(1), 1, 3);
            reapplyCanonicalHexPotionDebuffs(target, hexType, hexMutated, stack);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public static boolean doesHexOwnPotionEffect(
            LivingEntity target,
            Holder<MobEffect> effect,
            EssenceType hexType,
            boolean hexMutated
    ) {
        boolean isBoss = target.level() instanceof ServerLevel srv && isBossEntity(srv, target);
        if (effect.value().isBeneficial()) {
            if (hexMutated && MateriaFamilyTier.isMonsterEmpowermentHex(hexType) && !(target instanceof Player) && !isBoss) {
                if (effect.equals(MobEffects.STRENGTH)) {
                    return true;
                }
                return effect.equals(MobEffects.SPEED)
                        && hexType != EssenceType.ARID
                        && hexType != EssenceType.TAIGA
                        && hexType != EssenceType.ABYSS
                        && hexType != EssenceType.AEGIS
                        && hexType != EssenceType.TERMINUS;
            }
            return false;
        }

        if (!hexMutated) {
            return effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS);
        }

        MobEffect raw = effect.value();
        boolean isModBleed = raw == ModEffects.BLEEDING.get();
        boolean isModParalyzed = raw == ModEffects.PARALYZED.get();
        boolean isModErosion = raw == ModEffects.ARMOR_EROSION.get();
        boolean isModCrush = raw == ModEffects.GRAVITATIONAL_CRUSH.get();
        boolean isModAnchor = raw == ModEffects.INERTIAL_ANCHOR.get();
        boolean isModVoidTear = raw == ModEffects.VOID_TEAR.get();
        boolean isModHaze = raw == ModEffects.HAZE.get() || raw == ModEffects.DOUBLE_VISION.get();
        boolean isModDampening = raw == ModEffects.DAMPENING.get();
        boolean isModWeightless = raw == ModEffects.WEIGHTLESSNESS.get();
        boolean isModToxicity = raw == ModEffects.MATERIA_TOXICITY.get();

        return switch (hexType) {
            case AIR -> effect.equals(MobEffects.MINING_FATIGUE) || raw == MobEffects.MINING_FATIGUE.value()
                    || effect.equals(MobEffects.SLOWNESS) || raw == MobEffects.SLOWNESS.value()
                    || effect.equals(MobEffects.LEVITATION) || raw == MobEffects.LEVITATION.value()
                    || isModWeightless;
            case WATER -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.MINING_FATIGUE)
                    || effect.equals(MobEffects.WEAKNESS);
            case NATURE, OVERGROWTH -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.POISON)
                    || effect.equals(MobEffects.WEAKNESS) || isModAnchor;
            case EARTH -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.MINING_FATIGUE)
                    || isModCrush || isModAnchor;
            case FROZEN, GLACIAL, TAIGA -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.MINING_FATIGUE)
                    || effect.equals(MobEffects.WEAKNESS);
            case NETHER, PYRE -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS)
                    || effect.equals(MobEffects.HUNGER);
            case RADIANT, DAWN, CELESTIAL, AUREOLE, RADIANT_FIRE -> effect.equals(MobEffects.BLINDNESS)
                    || effect.equals(MobEffects.GLOWING) || effect.equals(MobEffects.WEAKNESS) || effect.equals(MobEffects.SLOWNESS);
            case UMBRAL, PENUMBRA, VESPER, UMBRAL_WATER -> effect.equals(MobEffects.BLINDNESS)
                    || effect.equals(MobEffects.DARKNESS) || effect.equals(MobEffects.WEAKNESS) || effect.equals(MobEffects.SLOWNESS);
            case EQUINOX -> effect.equals(MobEffects.BLINDNESS) || effect.equals(MobEffects.GLOWING)
                    || effect.equals(MobEffects.DARKNESS) || effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS);
            case UNDEAD -> effect.equals(MobEffects.WITHER) || effect.equals(MobEffects.SLOWNESS)
                    || effect.equals(MobEffects.WEAKNESS) || effect.equals(MobEffects.HUNGER);
            case VOID -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS)
                    || isModVoidTear || isModCrush || isModAnchor;
            case ARID, OASIS -> effect.equals(MobEffects.HUNGER) || effect.equals(MobEffects.SLOWNESS)
                    || effect.equals(MobEffects.WEAKNESS) || effect.equals(MobEffects.MINING_FATIGUE) || effect.equals(MobEffects.NAUSEA);
            case LIGHTNING, STATIC -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS) || isModParalyzed;
            case MAGMA -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS) || effect.equals(MobEffects.MINING_FATIGUE);
            case STORM -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS)
                    || effect.equals(MobEffects.BLINDNESS) || isModParalyzed;
            case DUST -> effect.equals(MobEffects.BLINDNESS) || effect.equals(MobEffects.SLOWNESS)
                    || effect.equals(MobEffects.WEAKNESS) || isModErosion;
            case SPORE -> effect.equals(MobEffects.NAUSEA) || effect.equals(MobEffects.POISON)
                    || effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS) || isModHaze;
            case VITAE -> effect.equals(MobEffects.WEAKNESS) || effect.equals(MobEffects.HUNGER)
                    || effect.equals(MobEffects.NAUSEA) || effect.equals(MobEffects.SLOWNESS);
            case BLOOD -> isModBleed || effect.equals(MobEffects.WEAKNESS) || effect.equals(MobEffects.SLOWNESS)
                    || effect.equals(MobEffects.NAUSEA);
            case AURA -> effect.equals(MobEffects.WEAKNESS) || effect.equals(MobEffects.SLOWNESS)
                    || effect.equals(MobEffects.MINING_FATIGUE) || isModDampening;
            case AMBER -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.MINING_FATIGUE)
                    || isModAnchor || isModParalyzed;
            case ECLIPSE -> effect.equals(MobEffects.BLINDNESS) || effect.equals(MobEffects.GLOWING)
                    || effect.equals(MobEffects.DARKNESS) || effect.equals(MobEffects.WEAKNESS);
            case BLIGHT -> effect.equals(MobEffects.POISON) || effect.equals(MobEffects.WITHER)
                    || effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS) || effect.equals(MobEffects.NAUSEA);
            case ASTRAL, AETHER -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.GLOWING)
                    || effect.equals(MobEffects.WEAKNESS) || isModCrush || isModAnchor;
            case SOULFIRE -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS)
                    || effect.equals(MobEffects.HUNGER) || effect.equals(MobEffects.WITHER);
            case VAPOR -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.BLINDNESS)
                    || effect.equals(MobEffects.WEAKNESS) || isModHaze;
            case NULL_R -> effect.equals(MobEffects.GLOWING) || effect.equals(MobEffects.BLINDNESS)
                    || effect.equals(MobEffects.WEAKNESS) || effect.equals(MobEffects.SLOWNESS);
            case NULL_U -> effect.equals(MobEffects.BLINDNESS) || effect.equals(MobEffects.DARKNESS)
                    || effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS) || isModDampening;
            case ABYSS -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.BLINDNESS)
                    || effect.equals(MobEffects.DARKNESS) || effect.equals(MobEffects.MINING_FATIGUE)
                    || effect.equals(MobEffects.WEAKNESS) || isModCrush;
            case AEGIS -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.MINING_FATIGUE)
                    || effect.equals(MobEffects.WEAKNESS) || isModAnchor;
            case AURORA -> effect.equals(MobEffects.GLOWING) || effect.equals(MobEffects.SLOWNESS)
                    || effect.equals(MobEffects.WEAKNESS) || effect.equals(MobEffects.MINING_FATIGUE);
            case MIRAGE -> effect.equals(MobEffects.NAUSEA) || effect.equals(MobEffects.BLINDNESS)
                    || effect.equals(MobEffects.WEAKNESS) || isModHaze;
            case WRAITH -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS)
                    || effect.equals(MobEffects.DARKNESS) || effect.equals(MobEffects.WITHER);
            case BARROW -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.MINING_FATIGUE)
                    || effect.equals(MobEffects.WEAKNESS) || effect.equals(MobEffects.WITHER) || isModAnchor;
            case RIME, TERMINUS -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.MINING_FATIGUE)
                    || effect.equals(MobEffects.WEAKNESS) || isModParalyzed || isModAnchor;
            case SPRING, SYLVAN -> effect.equals(MobEffects.SLOWNESS) || isModParalyzed || isModAnchor;
            case MIASMA -> effect.equals(MobEffects.POISON) || effect.equals(MobEffects.WITHER)
                    || effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.NAUSEA)
                    || effect.equals(MobEffects.WEAKNESS) || isModToxicity;
            case GENESIS -> isModParalyzed || effect.equals(MobEffects.GLOWING)
                    || effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS);
            case OBLIVION -> effect.equals(MobEffects.WITHER) || effect.equals(MobEffects.BLINDNESS)
                    || effect.equals(MobEffects.DARKNESS) || effect.equals(MobEffects.SLOWNESS)
                    || effect.equals(MobEffects.WEAKNESS) || isModVoidTear;
            case CHIMERA -> effect.equals(MobEffects.POISON) || effect.equals(MobEffects.WITHER)
                    || effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS)
                    || effect.equals(MobEffects.NAUSEA) || isModBleed;
            case ENTROPIC -> isModBleed || effect.equals(MobEffects.WITHER)
                    || effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS);
            case PRISMATIC, EMPYREAN -> effect.equals(MobEffects.BLINDNESS) || effect.equals(MobEffects.GLOWING)
                    || effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS)
                    || isModParalyzed || isModAnchor;
            case CATACLYSM -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WITHER)
                    || effect.equals(MobEffects.DARKNESS) || isModCrush || isModAnchor;
            case ESCHATON -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WITHER)
                    || effect.equals(MobEffects.DARKNESS) || effect.equals(MobEffects.BLINDNESS)
                    || isModVoidTear || isModCrush || isModAnchor;
            case APOTHEOSIS -> effect.equals(MobEffects.BLINDNESS) || effect.equals(MobEffects.GLOWING)
                    || effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WEAKNESS);
            case ENTROPICA -> effect.equals(MobEffects.SLOWNESS) || effect.equals(MobEffects.WITHER)
                    || effect.equals(MobEffects.BLINDNESS) || effect.equals(MobEffects.GLOWING)
                    || effect.equals(MobEffects.DARKNESS) || effect.equals(MobEffects.WEAKNESS)
                    || isModBleed || isModCrush || isModAnchor;
            default -> false;
        };
    }

    private static void reapplyCanonicalHexPotionDebuffs(
            LivingEntity target,
            EssenceType hexType,
            boolean hexMutated,
            int stack
    ) {
        int amp = Math.max(0, stack - 1);
        if (!hexMutated) {
            if (MateriaFamilyTier.isFrozenLineage(hexType) && !target.hasEffect(MobEffects.SLOWNESS)) {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1, true, false));
            } else if ((MateriaFamilyTier.isVoidLineage(hexType) || isUmbralLine(hexType)) && !target.hasEffect(MobEffects.WEAKNESS)) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 45, 0, true, false));
            }
            return;
        }
        switch (hexType) {
            case AIR -> {
                if (!target.hasEffect(MobEffects.MINING_FATIGUE)) {
                    target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 45, Math.max(1, amp), true, false));
                }
            }
            case WATER, STORM -> {
                if (!target.hasEffect(MobEffects.SLOWNESS)) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, amp, true, true));
                }
            }
            case GLACIAL -> {
                if (!target.hasEffect(MobEffects.SLOWNESS)) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, Math.min(3, 1 + amp), true, true));
                }
            }
            case OVERGROWTH -> {
                if (!target.hasEffect(MobEffects.SLOWNESS)) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, Math.min(3, 1 + amp), true, true));
                }
            }
            case NETHER -> {
                if (!target.hasEffect(MobEffects.WEAKNESS)) {
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 45, amp, true, true));
                }
            }
            case UMBRAL, VESPER, UMBRAL_WATER -> {
                if (!target.hasEffect(MobEffects.DARKNESS)) {
                    target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 50, 0, true, true));
                }
                if (!target.hasEffect(MobEffects.WEAKNESS)) {
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 45, amp, true, true));
                }
            }
            case RADIANT, RADIANT_FIRE, AUREOLE -> {
                if (!target.hasEffect(MobEffects.GLOWING)) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0, true, true));
                }
            }
            case EQUINOX -> {
                if (!target.hasEffect(MobEffects.GLOWING)) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0, true, true));
                }
                if (!target.hasEffect(MobEffects.DARKNESS)) {
                    target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 50, 0, true, true));
                }
            }
            case UNDEAD -> {
                if (!target.hasEffect(MobEffects.WITHER)) {
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 45, Math.max(1, amp), true, false));
                }
            }
            case VOID -> {
                if (!target.hasEffect(MobEffects.SLOWNESS)) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, amp, true, true));
                }
            }
            case CHIMERA -> {
                if (!target.hasEffect(MobEffects.POISON)) {
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 50, amp, true, true));
                }
            }
            case PRISMATIC -> {
                if (!target.hasEffect(MobEffects.GLOWING)) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, true));
                }
                if (!target.hasEffect(MobEffects.BLINDNESS)) {
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true));
                }
            }
            default -> {
            }
        }
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
        // Section 6.10 Cadaverous Wither-Blight Hex, Section 6.22 Hyper-Metabolic Arrhythmia Hex & Section 1.2B Entangling Miasma prevent ALL health regeneration!
        if (hasActiveMutatedHex(data, now, EssenceType.UNDEAD)
                || hasActiveMutatedHex(data, now, EssenceType.VITAE)
                || hasActiveMutatedHex(data, now, EssenceType.BLOOD)
                || hasActiveMutatedHex(data, now, EssenceType.BLIGHT)
                || (target instanceof Mob && hasActiveMutatedHex(data, now, EssenceType.ECLIPSE))
                || now <= data.getLong(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_UNTIL).orElse(0L)
                || hasActiveMutatedHex(data, now, EssenceType.GENESIS)
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
            case "WATER" -> MateriaFamilyTier.isNetherLineage(incomingElement);
            case "STORM" -> MateriaFamilyTier.isNetherLineage(incomingElement)
                    || incomingElement == EssenceType.ARID
                    || incomingElement == EssenceType.VAPOR
                    || incomingElement == EssenceType.DAWN;
            case "NETHER" -> isWaterLine(incomingElement) || incomingElement == EssenceType.VITAE; // Section 6.7: cleansed strictly via Water-lineage magic, Restoration magic, or caster defeat!
            case "ARID", "MAGMA", "OASIS", "PYRE" -> isWaterLine(incomingElement) || MateriaFamilyTier.isFrozenLineage(incomingElement);
            case "SOULFIRE" -> isWaterLine(incomingElement); // Section 6.29: quenched strictly via Water/Restoration magic or caster defeat!
            case "FROZEN", "GLACIAL", "NATURE", "OVERGROWTH", "SPORE", "TAIGA", "AMBER", "TERMINUS" -> MateriaFamilyTier.isNetherLineage(incomingElement);
            case "AURORA", "RIME" -> MateriaFamilyTier.isNetherLineage(incomingElement) || incomingElement == EssenceType.VITAE; // Section 6.36 & 6.42: Vitae magic or Pyre/Nether fire!
            case "VAPOR" -> MateriaFamilyTier.isFrozenLineage(incomingElement);
            case "DUST" -> isWaterLine(incomingElement) || (isAirLine(incomingElement) && incomingElement != EssenceType.DUST);
            case "VITAE", "AURA", "ECLIPSE", "ASTRAL", "NULL_R" -> false; // Section 6.22, 6.24, 6.26, 6.28 & 6.31: cleansed strictly via Restoration magic or caster defeat!
            case "UMBRAL", "UNDEAD", "BLOOD", "BLIGHT", "NULL_U" -> isRadiantLine(incomingElement);
            case "MIASMA" -> incomingElement == EssenceType.VITAE || isRadiantLine(incomingElement); // Section 6.46: Vitae magic or Radiant purification!
            case "WRAITH", "BARROW", "PENUMBRA" -> isRadiantLine(incomingElement) || incomingElement == EssenceType.VITAE; // Section 6.38-6.41: Vitae magic or Radiant light!
            case "ABYSS" -> isRadiantLine(incomingElement) || incomingElement == EssenceType.VITAE; // Section 6.34: Vitae magic or Radiant light!
            case "OBLIVION" -> incomingElement == EssenceType.VITAE || isRadiantLine(incomingElement) || incomingElement == EssenceType.GENESIS; // Section 6.48: Vitae magic or Genesis/Radiant divine cleansing!
            case "AETHER" -> incomingElement == EssenceType.VITAE || incomingElement == EssenceType.GENESIS || incomingElement == EssenceType.SPRING
                    || MateriaFamilyTier.isVoidLineage(incomingElement) || isUmbralLine(incomingElement);
            case "ENTROPIC" -> incomingElement == EssenceType.VITAE || incomingElement == EssenceType.CELESTIAL || isRadiantLine(incomingElement);
            case "CATACLYSM" -> incomingElement == EssenceType.VITAE || incomingElement == EssenceType.GENESIS || isRadiantLine(incomingElement);
            case "DAWN" -> isUmbralLine(incomingElement) || incomingElement == EssenceType.VITAE; // Section 6.33: Vitae magic or Umbral shadow reactions!
            case "AEGIS" -> isUmbralLine(incomingElement) || incomingElement == EssenceType.VITAE; // Section 6.35: Vitae magic or Umbral darkness!
            case "MIRAGE" -> isUmbralLine(incomingElement) || incomingElement == EssenceType.VITAE; // Section 6.37: Vitae magic or Umbral shadow reactions!
            case "SPRING" -> incomingElement == EssenceType.VITAE || isUmbralLine(incomingElement) || incomingElement == EssenceType.BLIGHT || MateriaFamilyTier.isVoidLineage(incomingElement); // Section 6.43: Vitae magic or Umbral/Blight wither rot!
            case "RADIANT" -> isUmbralLine(incomingElement) || incomingElement == EssenceType.VITAE; // Section 6.8: cleansed strictly via Restoration/Vitae magic, Shadow (Umbral-lineage) magic, or caster defeat!
            case "CELESTIAL", "PRISMATIC", "EMPYREAN" ->
                    isUmbralLine(incomingElement) || incomingElement == EssenceType.BLIGHT || MateriaFamilyTier.isVoidLineage(incomingElement);
            case "GENESIS" -> incomingElement == EssenceType.VITAE || isUmbralLine(incomingElement) || MateriaFamilyTier.isVoidLineage(incomingElement); // Section 6.47: Vitae magic or Umbral/Void corruption!
            case "ESCHATON" -> incomingElement == EssenceType.APOTHEOSIS || incomingElement == EssenceType.ENTROPICA;
            case "APOTHEOSIS" -> incomingElement == EssenceType.ESCHATON || incomingElement == EssenceType.VOID || isUmbralLine(incomingElement);
            case "ENTROPICA" -> false; // Sec 6.59: immune to elemental counter-weaknesses; cleansed ONLY by Restoration!
            case "VOID" -> MateriaFamilyTier.isEarthLineage(incomingElement);
            case "LIGHTNING" -> MateriaFamilyTier.isEarthLineage(incomingElement) || isNatureLine(incomingElement);
            case "STATIC" -> incomingElement == EssenceType.VITAE || MateriaFamilyTier.isEarthLineage(incomingElement) || isNatureLine(incomingElement); // Section 6.44: Vitae magic or Earth/Nature grounding!
            case "SYLVAN" -> incomingElement == EssenceType.VITAE || MateriaFamilyTier.isNetherLineage(incomingElement) || incomingElement == EssenceType.PYRE; // Section 6.45: Vitae magic or Nether/Pyre thermal incineration!
            default -> false;
        };

        if (counterMatched) {
            clearHexData(target, data);
            if (target.level() instanceof ServerLevel srv) {
                if ("WATER".equals(hexStr) || "STORM".equals(hexStr) || "NETHER".equals(hexStr) || "MAGMA".equals(hexStr)
                        || "SOULFIRE".equals(hexStr) || "PYRE".equals(hexStr)) {
                    target.clearFire();
                    srv.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(0.8D), target.getZ(),
                            14, 0.35D, 0.4D, 0.35D, 0.05D);
                    srv.playSound(null, target.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.65F, 1.35F);
                } else if (isRadiantLine(incomingElement)) {
                    srv.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.85D), target.getZ(),
                            14, 0.35D, 0.40D, 0.35D, 0.06D);
                    srv.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.48F);
                } else {
                    srv.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY(0.8D), target.getZ(),
                            8, 0.3D, 0.35D, 0.3D, 0.04D);
                }
            }
            return true;
        }
        return false;
    }

    public static boolean hasActiveMutatedHexOnEntity(LivingEntity entity, long now, EssenceType type) {
        return hasActiveMutatedHex(EntityHelper.getPersistentData(entity), now, type);
    }

    private static void clearHexData(LivingEntity target, CompoundTag data) {
        String oldHexType = data.getString(TAG_HEX_TYPE).orElse("");
        long oldUntil = data.getLong(TAG_HEX_UNTIL).orElse(0L);
        if (!oldHexType.isEmpty() && target.level() instanceof ServerLevel srv && isBossEntity(srv, target)) {
            stampBossPostHexImmunity(data, srv.getGameTime(), oldHexType, oldUntil);
        }
        data.remove(TAG_HEX_TYPE);
        data.remove(TAG_HEX_TIER);
        data.remove(TAG_HEX_STACK);
        data.remove(TAG_HEX_MUTATED);
        data.remove(TAG_HEX_UNTIL);
        data.remove(TAG_HEX_APPLIED_TICK);
        data.remove(TAG_HEX_INITIAL_WINDOW);
        data.remove(TAG_HEX_CASTER_UUID);
        data.remove(TAG_HEX_MAX_ALLOWED_HP);
        data.remove("VaporHexDissolveSteps");
        data.remove("NullRHexMeltSteps");
        data.remove("SpellSilencedUntil");
        data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
        data.remove(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL);
        data.remove(SpellLingeringZoneManager.TAG_VOID_ANCHOR_X);
        data.remove(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Y);
        data.remove(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Z);
        data.remove(SpellLingeringZoneManager.TAG_VOID_LAST_POS_X);
        data.remove(SpellLingeringZoneManager.TAG_VOID_LAST_POS_Y);
        data.remove(SpellLingeringZoneManager.TAG_VOID_LAST_POS_Z);
        data.remove("SpellVoidLastRubberbandTick");
        data.remove(TAG_AIR_HYPOXIA_MELEE_CD_UNTIL);
        removeHexMaxHealthCutModifier(target);
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
            removeHexMaxHealthCutModifier(target);
            clearShredModifiers(target, data);
            clearOffenseShredModifiers(target, data);
            data.remove("SpellSilencedUntil");
            data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
            data.remove(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL);
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
     * Section 6.8 <i>Luminous Cleanse</i> ({@code BLINK}): cleanses exactly <b>1</b> negative status effect
     * (or 1 active Elemental Hex / Unquenchable Fire / freeze if no negative potion effect is present) upon arrival.
     */
    public static boolean cleanseSingleNegativeEffectOrHex(LivingEntity target) {
        List<MobEffectInstance> active = new ArrayList<>(target.getActiveEffects());
        for (MobEffectInstance inst : active) {
            if (!inst.getEffect().value().isBeneficial()) {
                target.removeEffect(inst.getEffect());
                return true;
            }
        }
        CompoundTag data = EntityHelper.getPersistentData(target);
        if (!data.getString(TAG_HEX_TYPE).orElse("").isEmpty()) {
            clearHexData(target, data);
            return true;
        }
        if (target.isOnFire() || data.contains(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL)) {
            data.remove("SpellSilencedUntil");
            data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
            data.remove(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL);
            target.clearFire();
            return true;
        }
        if (target.getTicksFrozen() > 0) {
            target.setTicksFrozen(0);
            return true;
        }
        return false;
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

            // Section 1.2B Entangling Miasma: prevents mob health regeneration while virulent infection is active!
            long miasmaUntil = data.getLong(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_UNTIL).orElse(0L);
            if (miasmaUntil > now) {
                if (living.hasEffect(MobEffects.REGENERATION)) {
                    living.removeEffect(MobEffects.REGENERATION);
                }
                float mAllowed = data.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(living.getMaxHealth());
                if (living.getHealth() > mAllowed + 0.05F) {
                    living.setHealth(Math.max(0.5F, mAllowed));
                } else if (living.getHealth() < mAllowed) {
                    data.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, living.getHealth());
                }
            } else if (miasmaUntil > 0L) {
                data.remove(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_UNTIL);
                data.remove(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP);
            }

            long until = data.getLong(TAG_HEX_UNTIL).orElse(0L);
            if (until <= 0L) {
                removeHexMaxHealthCutModifier(living);
                continue;
            }

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

            // 2. Enforce Natural Health Regeneration Lock, Hypoxia Stamina Drag & Permafrost / Glacial Cryo-Hex Speed Sap every 5 ticks
            if (hexMutated && hexType == EssenceType.AIR) {
                float mult = getHexStackMultiplier(living);
                applyTimedMovementSpeedSap(living, level, Math.min(0.75D, 0.40D * mult), 35L);
                applyTimedAttackDebuff(living, level, 0.0D, Math.min(0.85D, 0.40D * mult), 35L);
                if (living instanceof Player hypPlayer && !hypPlayer.isCreative() && !hypPlayer.isSpectator() && hypPlayer.isSprinting()) {
                    hypPlayer.causeFoodExhaustion(0.40F * mult);
                }
            } else if (hexMutated && hexType == EssenceType.EARTH) {
                int eStack = Math.clamp(data.getInt(TAG_HEX_STACK).orElse(1), 1, 3);
                applyTimedAttributeShred(living, level, resolveSunderingHexArmorShred(eStack), 0.0D, 45L);
            } else if (hexMutated && hexType == EssenceType.FROZEN) {
                living.setSprinting(false);
                applyTimedMovementSpeedSap(living, level, 0.60D, 35L);
            } else if (hexMutated && hexType == EssenceType.NETHER) {
                int nStack = Math.clamp(data.getInt(TAG_HEX_STACK).orElse(1), 1, 3);
                living.removeEffect(MobEffects.REGENERATION);
                living.igniteForSeconds(3);
                data.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL,
                        Math.max(now + 45L, data.getLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL).orElse(0L)));
                applyPercentageArmorShred(living, level, resolveHellfireSearArmorMeltFraction(nStack), 0.0D, 45L);
            } else if (hexMutated && hexType == EssenceType.RADIANT) {
                int rStack = Math.clamp(data.getInt(TAG_HEX_STACK).orElse(1), 1, 3);
                living.removeEffect(MobEffects.REGENERATION);
                living.removeEffect(MobEffects.INVISIBILITY);
                data.remove(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL);
                if (!living.hasEffect(MobEffects.GLOWING)) {
                    living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, true, false));
                }
                applyTimedAttributeShred(living, level, resolveSunScorchedHexArmorMelt(rStack), 0.0D, 45L);
            } else if (hexMutated && hexType == EssenceType.GLACIAL) {
                living.setSprinting(false);
                applyTimedMovementSpeedSap(living, level, Math.min(0.85D, 0.50D * getHexStackMultiplier(living)), 35L);
            } else if (hexMutated && (hexType == EssenceType.NATURE || hexType == EssenceType.OVERGROWTH)) {
                SpellLingeringZoneManager.applyArcaneSilence(living, 35);
                if (hexType == EssenceType.NATURE) {
                    int nStack = Math.clamp(data.getInt(TAG_HEX_STACK).orElse(1), 1, 3);
                    double nShred = switch (nStack) {
                        case 1 -> 0.20D;
                        case 2 -> 0.35D;
                        default -> 0.50D;
                    };
                    applyPercentageArmorShred(living, level, 0.0D, nShred, 45L);
                }
            }
            if (hexMutated && hexType == EssenceType.UNDEAD && living.hasEffect(MobEffects.REGENERATION)) {
                living.removeEffect(MobEffects.REGENERATION);
            }
            if (hexMutated && isRegenLockingHex(hexType) && data.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float allowed = data.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(living.getMaxHealth());
                if (living.getHealth() > allowed + 0.05F) {
                    living.setHealth(Math.max(0.5F, allowed));
                } else if (living.getHealth() < allowed) {
                    data.putFloat(TAG_HEX_MAX_ALLOWED_HP, living.getHealth());
                }
            }

            if (!fullHexPulse) continue;

            // 3. Player Spawn-Lure Beacons (Section 5.6 / 6.10: Undead Necrotic Resonance +150% undead spawn rate in 32m up to light 7!)
            if (hexMutated && living instanceof Player hexedPlayer && !hexedPlayer.isCreative() && !hexedPlayer.isSpectator() && now % 40L == 0L) {
                if (hexType == EssenceType.UNDEAD) {
                    int nearbyUndead = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(32.0D))) {
                        if (!mob.isAlive() || !mob.getType().is(EntityTypeTags.UNDEAD)) continue;
                        nearbyUndead++;
                        if (mob.getTarget() == null) {
                            mob.setTarget(hexedPlayer);
                        }
                    }
                    // Necrotic Resonance: +150% undead monster spawn rate (zombies, skeletons, phantoms, drowned) in a 32-block radius up to light level 7!
                    if (nearbyUndead < 10) {
                        for (int attempt = 0; attempt < 5; attempt++) {
                            double dist = 10.0D + level.random.nextDouble() * 20.0D; // 10m..30m (within 32m radius)
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            int sx = Mth.floor(hexedPlayer.getX() + Math.cos(ang) * dist);
                            int sz = Mth.floor(hexedPlayer.getZ() + Math.sin(ang) * dist);
                            int baseY = hexedPlayer.getBlockY();
                            net.minecraft.core.BlockPos foundPos = null;
                            boolean waterSpot = false;
                            for (int dy = 4; dy >= -5; dy--) {
                                net.minecraft.core.BlockPos candidate = new net.minecraft.core.BlockPos(sx, baseY + dy, sz);
                                if (level.getMaxLocalRawBrightness(candidate) > 7) continue;
                                var bs = level.getBlockState(candidate);
                                if (bs.is(net.minecraft.world.level.block.Blocks.WATER)
                                        && level.getBlockState(candidate.above()).is(net.minecraft.world.level.block.Blocks.WATER)) {
                                    foundPos = candidate;
                                    waterSpot = true;
                                    break;
                                }
                                if (bs.isAir() && level.getBlockState(candidate.above()).isAir()
                                        && level.getBlockState(candidate.below()).isSolidRender()) {
                                    foundPos = candidate;
                                    break;
                                }
                            }
                            if (foundPos != null && level.random.nextFloat() < 0.75F) {
                                Mob spawnedUndead;
                                if (waterSpot) {
                                    spawnedUndead = EntityType.DROWNED.create(level, EntitySpawnReason.MOB_SUMMONED);
                                } else if (level.canSeeSky(foundPos.above(6))
                                        && level.getMaxLocalRawBrightness(foundPos.above(6)) <= 7
                                        && level.random.nextFloat() < 0.25F) {
                                    spawnedUndead = EntityType.PHANTOM.create(level, EntitySpawnReason.MOB_SUMMONED);
                                    foundPos = foundPos.above(6);
                                } else if (level.random.nextBoolean()) {
                                    Skeleton sk = EntityType.SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
                                    if (sk != null) {
                                        sk.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                                    }
                                    spawnedUndead = sk;
                                } else {
                                    spawnedUndead = EntityType.ZOMBIE.create(level, EntitySpawnReason.MOB_SUMMONED);
                                }
                                if (spawnedUndead != null) {
                                    spawnedUndead.setPos(foundPos.getX() + 0.5D, foundPos.getY(), foundPos.getZ() + 0.5D);
                                    spawnedUndead.setTarget(hexedPlayer);
                                    level.addFreshEntity(spawnedUndead);
                                    level.sendParticles(ParticleTypes.SOUL, spawnedUndead.getX(), spawnedUndead.getY(0.6D), spawnedUndead.getZ(),
                                            10, 0.3D, 0.4D, 0.3D, 0.03D);
                                    break;
                                }
                            }
                        }
                    }
                } else if (hexType == EssenceType.VOID) {
                    // Section 5.6 & 6.11 Spacetime Rift Beacon:
                    // Spatial instability opens micro-tears in reality around the afflicted player,
                    // periodically pulling Void-Stalkers and Endermen into the perimeter at a 2x accelerated spawn rate!
                    int nearbyVoidHunters = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(32.0D))) {
                        if (!mob.isAlive()) continue;
                        boolean isWildVoidStalker = mob instanceof ddraig.net.entropica.entity.SpellEidolonEntity eid
                                && eid.getOwnerUuid() == null
                                && eid.getEffectiveMateria() == EssenceType.VOID;
                        if (mob instanceof EnderMan || isWildVoidStalker) {
                            nearbyVoidHunters++;
                            if (mob.getTarget() == null) {
                                mob.setTarget(hexedPlayer);
                            }
                        } else if (mob instanceof Endermite && mob.getTarget() == null) {
                            mob.setTarget(hexedPlayer);
                        }
                    }
                    int hexStack = getHexStackLevel(hexedPlayer);
                    inthexTier: {
                        int beaconTier = Math.clamp(EntityHelper.getPersistentData(hexedPlayer).getInt(TAG_HEX_TIER).orElse(1), 1, 10);
                        long riftInterval = Math.max(70L, 120L - (hexStack - 1) * 20L); // 2x accelerated perimeter spawn rate!
                        if (nearbyVoidHunters < 4 && now % riftInterval == 0L) {
                            for (int attempt = 0; attempt < 5; attempt++) {
                                double ang = level.random.nextDouble() * Math.PI * 2.0D;
                                double dist = 10.0D + level.random.nextDouble() * 12.0D;
                                int sx = (int) Math.floor(hexedPlayer.getX() + Math.cos(ang) * dist);
                                int sz = (int) Math.floor(hexedPlayer.getZ() + Math.sin(ang) * dist);
                                int baseY = hexedPlayer.blockPosition().getY();
                                net.minecraft.core.BlockPos foundPos = null;
                                for (int dy = 4; dy >= -4; dy--) {
                                    net.minecraft.core.BlockPos candidate = new net.minecraft.core.BlockPos(sx, baseY + dy, sz);
                                    if (level.getBlockState(candidate).isAir()
                                            && level.getBlockState(candidate.above()).isAir()
                                            && level.getBlockState(candidate.above(2)).isAir()
                                            && level.getBlockState(candidate.below()).isSolidRender()) {
                                        foundPos = candidate;
                                        break;
                                    }
                                }
                                if (foundPos != null) {
                                    Vec3 riftSpawnPos = new Vec3(foundPos.getX() + 0.5D, foundPos.getY(), foundPos.getZ() + 0.5D);
                                    SpellWorldVfxManager.broadcastBlinkRift(
                                            level, riftSpawnPos.add(0.0D, 2.2D, 0.0D), riftSpawnPos, EssenceType.VOID, beaconTier
                                    );
                                    level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                                            riftSpawnPos.x, riftSpawnPos.y + 1.0D, riftSpawnPos.z,
                                            22, 0.35D, 0.65D, 0.35D, 0.08D);
                                    level.playSound(null, foundPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.85F, 0.72F);
                                    if (level.random.nextBoolean()) {
                                        ddraig.net.entropica.entity.SpellEidolonEntity.spawnWildHostileVoidStalker(
                                                level, riftSpawnPos, hexedPlayer, beaconTier
                                        );
                                    } else {
                                        EnderMan ender = EntityType.ENDERMAN.create(level, EntitySpawnReason.MOB_SUMMONED);
                                        if (ender != null) {
                                            ender.setPos(riftSpawnPos.x, riftSpawnPos.y, riftSpawnPos.z);
                                            ender.setTarget(hexedPlayer);
                                            level.addFreshEntity(ender);
                                        }
                                    }
                                    break inthexTier;
                                }
                            }
                        }
                    }
                } else if (hexType == EssenceType.BLOOD) {
                    // Section 6.23 Scent of Hemorrhage: exposed arterial scent increases carnivorous & predatory hostile spawn rates by +100% in a 32-block radius!
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
                    if (nearbyPredators < 8 && now % 80L == 0L) {
                        for (int attempt = 0; attempt < 4; attempt++) {
                            double dist = 10.0D + level.random.nextDouble() * 20.0D; // 10m..30m within 32m radius
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            int sx = Mth.floor(hexedPlayer.getX() + Math.cos(ang) * dist);
                            int sz = Mth.floor(hexedPlayer.getZ() + Math.sin(ang) * dist);
                            int baseY = hexedPlayer.getBlockY();
                            net.minecraft.core.BlockPos foundPos = null;
                            boolean waterSpot = false;
                            for (int dy = 4; dy >= -5; dy--) {
                                net.minecraft.core.BlockPos candidate = new net.minecraft.core.BlockPos(sx, baseY + dy, sz);
                                if (level.getMaxLocalRawBrightness(candidate) > 7) continue;
                                var bs = level.getBlockState(candidate);
                                if (bs.is(net.minecraft.world.level.block.Blocks.WATER)
                                        && level.getBlockState(candidate.above()).is(net.minecraft.world.level.block.Blocks.WATER)) {
                                    foundPos = candidate;
                                    waterSpot = true;
                                    break;
                                }
                                if (bs.isAir() && level.getBlockState(candidate.above()).isAir()
                                        && level.getBlockState(candidate.below()).isSolidRender()) {
                                    foundPos = candidate;
                                    break;
                                }
                            }
                            if (foundPos != null) {
                                Mob predator = switch (waterSpot ? 0 : (1 + level.random.nextInt(4))) {
                                    case 0 -> EntityType.DROWNED.create(level, EntitySpawnReason.MOB_SUMMONED);
                                    case 1 -> EntityType.SPIDER.create(level, EntitySpawnReason.MOB_SUMMONED);
                                    case 2 -> EntityType.CAVE_SPIDER.create(level, EntitySpawnReason.MOB_SUMMONED);
                                    case 3 -> EntityType.ZOMBIE.create(level, EntitySpawnReason.MOB_SUMMONED);
                                    default -> EntityType.HUSK.create(level, EntitySpawnReason.MOB_SUMMONED);
                                };
                                if (predator != null) {
                                    predator.setPos(foundPos.getX() + 0.5D, foundPos.getY(), foundPos.getZ() + 0.5D);
                                    predator.setTarget(hexedPlayer);
                                    level.addFreshEntity(predator);
                                    level.sendParticles(
                                            ddraig.net.entropica.client.particle.TimedTintableParticleOption.linear(
                                                    ddraig.net.entropica.registry.ModParticles.TINTABLE_BLOOD.get(), 0x8A0303, 22, 0.95F, 0.05F),
                                            predator.getX(), predator.getY(0.5D), predator.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.03D
                                    );
                                    break;
                                }
                            }
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
                            hurtWithAstralMateriaAdvantage(
                                    level, hexedPlayer, resolveHexCaster(level, hexedPlayer),
                                    2.25F * getHexStackMultiplier(hexedPlayer), false, true
                            );
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
                    if (nearbySkel < 3 && now % 140L == 0L && level.random.nextFloat() < 0.45F) {
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
                        Mob pest = switch (level.random.nextInt(3)) {
                            case 0 -> EntityType.SLIME.create(level, EntitySpawnReason.MOB_SUMMONED);
                            case 1 -> EntityType.WITCH.create(level, EntitySpawnReason.MOB_SUMMONED);
                            default -> EntityType.CAVE_SPIDER.create(level, EntitySpawnReason.MOB_SUMMONED);
                        };
                        if (pest != null) {
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            pest.setPos(hexedPlayer.getX() + Math.cos(ang) * 5.0D, hexedPlayer.getY(), hexedPlayer.getZ() + Math.sin(ang) * 5.0D);
                            pest.setTarget(hexedPlayer);
                            level.addFreshEntity(pest);
                            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, pest.getX(), pest.getY(0.5D), pest.getZ(), 10, 0.25D, 0.25D, 0.25D, 0.03D);
                            level.sendParticles(ParticleTypes.SNEEZE, pest.getX(), pest.getY(0.6D), pest.getZ(), 8, 0.25D, 0.25D, 0.25D, 0.02D);
                        }
                    }
                } else if (hexType == EssenceType.OBLIVION) {
                    // Section 6.48 Stygian Extinction Scent: draws nether beasts, wither skeletons, and nightmare predators (+100% dark spawn rate)!
                    int nearbyStygian = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(28.0D))) {
                        if (!mob.isAlive()) continue;
                        if (mob instanceof WitherSkeleton || mob instanceof net.minecraft.world.entity.monster.Blaze || mob instanceof Phantom) {
                            nearbyStygian++;
                        }
                        if (mob instanceof Monster && mob.getTarget() == null) {
                            mob.setTarget(hexedPlayer);
                        }
                    }
                    if (nearbyStygian < 2 && now % 200L == 0L && level.random.nextFloat() < 0.32F) {
                        Mob dread = switch (level.random.nextInt(3)) {
                            case 0 -> EntityType.BLAZE.create(level, EntitySpawnReason.MOB_SUMMONED);
                            case 1 -> EntityType.PHANTOM.create(level, EntitySpawnReason.MOB_SUMMONED);
                            default -> {
                                WitherSkeleton ws = EntityType.WITHER_SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
                                if (ws != null) {
                                    ws.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.world.item.Items.STONE_SWORD));
                                }
                                yield ws;
                            }
                        };
                        if (dread != null) {
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            double spawnY = dread instanceof Phantom ? hexedPlayer.getY() + 3.0D : hexedPlayer.getY();
                            dread.setPos(hexedPlayer.getX() + Math.cos(ang) * 5.5D, spawnY, hexedPlayer.getZ() + Math.sin(ang) * 5.5D);
                            dread.setTarget(hexedPlayer);
                            level.addFreshEntity(dread);
                            level.sendParticles(ParticleTypes.LARGE_SMOKE, dread.getX(), dread.getY(0.7D), dread.getZ(), 14, 0.3D, 0.5D, 0.3D, 0.04D);
                        }
                    }
                } else if (hexType == EssenceType.ENTROPIC) {
                    // Section 6.51 Apex Heat-Death Influx (32m, +200% accelerated rate): spawns cosmic void horrors & entropic apparitions!
                    int nearbyVoid = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(32.0D))) {
                        if (!mob.isAlive()) continue;
                        if (mob instanceof Endermite || mob instanceof Phantom || mob instanceof EnderMan || mob instanceof WitherSkeleton) {
                            nearbyVoid++;
                        }
                        if (mob instanceof Monster && mob.getTarget() == null) mob.setTarget(hexedPlayer);
                    }
                    if (nearbyVoid < 4 && now % 140L == 0L && level.random.nextFloat() < 0.55F) {
                        Mob horror = switch (level.random.nextInt(3)) {
                            case 0 -> EntityType.ENDERMITE.create(level, EntitySpawnReason.MOB_SUMMONED);
                            case 1 -> EntityType.PHANTOM.create(level, EntitySpawnReason.MOB_SUMMONED);
                            default -> EntityType.WITHER_SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
                        };
                        if (horror != null) {
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            double spawnY = horror instanceof Phantom ? hexedPlayer.getY() + 3.5D : hexedPlayer.getY();
                            horror.setPos(hexedPlayer.getX() + Math.cos(ang) * 6.0D, spawnY, hexedPlayer.getZ() + Math.sin(ang) * 6.0D);
                            horror.setTarget(hexedPlayer);
                            level.addFreshEntity(horror);
                            level.sendParticles(ParticleTypes.REVERSE_PORTAL, horror.getX(), horror.getY(0.8D), horror.getZ(), 14, 0.3D, 0.4D, 0.3D, 0.05D);
                        }
                    }
                } else if (hexType == EssenceType.EMPYREAN) {
                    // Section 6.54 Empyrean Starlight Beacon (32m, +120% hostile monster spawn rate around afflicted player):
                    hexedPlayer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0, true, true));
                    int nearbyHostiles = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(32.0D))) {
                        if (mob.isAlive() && mob instanceof Monster) {
                            nearbyHostiles++;
                            if (mob.getTarget() == null) mob.setTarget(hexedPlayer);
                        }
                    }
                    if (nearbyHostiles < 8 && now % 80L == 0L) {
                        for (int attempt = 0; attempt < 4; attempt++) {
                            double dist = 10.0D + level.random.nextDouble() * 19.0D;
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            int sx = Mth.floor(hexedPlayer.getX() + Math.cos(ang) * dist);
                            int sz = Mth.floor(hexedPlayer.getZ() + Math.sin(ang) * dist);
                            int baseY = hexedPlayer.getBlockY();
                            net.minecraft.core.BlockPos foundPos = null;
                            for (int dy = 4; dy >= -4; dy--) {
                                net.minecraft.core.BlockPos cand = new net.minecraft.core.BlockPos(sx, baseY + dy, sz);
                                if (level.getBlockState(cand).isAir()
                                        && level.getBlockState(cand.above()).isAir()
                                        && level.getBlockState(cand.below()).isSolidRender()) {
                                    foundPos = cand;
                                    break;
                                }
                            }
                            if (foundPos != null && level.random.nextFloat() < 0.65F) {
                                Mob spawned;
                                int roll = level.random.nextInt(4);
                                if (roll == 0) {
                                    Skeleton sk = EntityType.SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
                                    if (sk != null) sk.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                                    spawned = sk;
                                } else if (roll == 1) {
                                    spawned = EntityType.SPIDER.create(level, EntitySpawnReason.MOB_SUMMONED);
                                } else {
                                    spawned = EntityType.ZOMBIE.create(level, EntitySpawnReason.MOB_SUMMONED);
                                }
                                if (spawned != null) {
                                    spawned.setPos(foundPos.getX() + 0.5D, foundPos.getY(), foundPos.getZ() + 0.5D);
                                    spawned.setTarget(hexedPlayer);
                                    level.addFreshEntity(spawned);
                                    level.sendParticles(ParticleTypes.END_ROD, spawned.getX(), spawned.getY(0.8D), spawned.getZ(),
                                            10, 0.3D, 0.4D, 0.3D, 0.04D);
                                    break;
                                }
                            }
                        }
                    }
                } else if (hexType == EssenceType.CATACLYSM) {
                    // Section 6.55 Abyssal World-Breaker Beacon (32m, +150% nether beasts, wither skeletons & void horrors):
                    int nearbyAbyssal = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(32.0D))) {
                        if (!mob.isAlive()) continue;
                        if (mob instanceof WitherSkeleton || mob instanceof MagmaCube || mob instanceof Blaze
                                || mob instanceof Endermite || mob instanceof EnderMan
                                || mob instanceof net.minecraft.world.entity.monster.hoglin.Hoglin) {
                            nearbyAbyssal++;
                        }
                        if (mob instanceof Monster && mob.getTarget() == null) mob.setTarget(hexedPlayer);
                    }
                    if (nearbyAbyssal < 8 && now % 70L == 0L) {
                        for (int attempt = 0; attempt < 4; attempt++) {
                            double dist = 10.0D + level.random.nextDouble() * 19.0D;
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            int sx = Mth.floor(hexedPlayer.getX() + Math.cos(ang) * dist);
                            int sz = Mth.floor(hexedPlayer.getZ() + Math.sin(ang) * dist);
                            int baseY = hexedPlayer.getBlockY();
                            net.minecraft.core.BlockPos foundPos = null;
                            for (int dy = 4; dy >= -4; dy--) {
                                net.minecraft.core.BlockPos cand = new net.minecraft.core.BlockPos(sx, baseY + dy, sz);
                                if (level.getBlockState(cand).isAir()
                                        && level.getBlockState(cand.above()).isAir()
                                        && level.getBlockState(cand.below()).isSolidRender()) {
                                    foundPos = cand;
                                    break;
                                }
                            }
                            if (foundPos != null && level.random.nextFloat() < 0.72F) {
                                Mob spawned = switch (level.random.nextInt(5)) {
                                    case 0 -> {
                                        WitherSkeleton ws = EntityType.WITHER_SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
                                        if (ws != null) ws.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
                                        yield ws;
                                    }
                                    case 1 -> EntityType.BLAZE.create(level, EntitySpawnReason.MOB_SUMMONED);
                                    case 2 -> EntityType.MAGMA_CUBE.create(level, EntitySpawnReason.MOB_SUMMONED);
                                    case 3 -> EntityType.ENDERMITE.create(level, EntitySpawnReason.MOB_SUMMONED);
                                    default -> EntityType.ENDERMAN.create(level, EntitySpawnReason.MOB_SUMMONED);
                                };
                                if (spawned != null) {
                                    spawned.setPos(foundPos.getX() + 0.5D, foundPos.getY(), foundPos.getZ() + 0.5D);
                                    spawned.setTarget(hexedPlayer);
                                    level.addFreshEntity(spawned);
                                    level.sendParticles(ParticleTypes.REVERSE_PORTAL, spawned.getX(), spawned.getY(0.8D), spawned.getZ(),
                                            14, 0.35D, 0.45D, 0.35D, 0.05D);
                                    level.sendParticles(ParticleTypes.LAVA, spawned.getX(), spawned.getY(0.5D), spawned.getZ(),
                                            6, 0.25D, 0.25D, 0.25D, 0.03D);
                                    break;
                                }
                            }
                        }
                    }
                } else if (hexType == EssenceType.ESCHATON || hexType == EssenceType.ENTROPICA) {
                    // Sections 6.57 & 6.59 Cosmic Reality Tear / Omega Spawn-Lure (36m, +200% hostile monster & void horror influx with immediate aggro!):
                    int nearbyHorrors = 0;
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, hexedPlayer.getBoundingBox().inflate(36.0D))) {
                        if (!mob.isAlive()) continue;
                        if (mob instanceof Monster) {
                            nearbyHorrors++;
                            if (mob.getTarget() == null) mob.setTarget(hexedPlayer);
                        }
                    }
                    if (nearbyHorrors < 10 && now % 55L == 0L) {
                        for (int attempt = 0; attempt < 4; attempt++) {
                            double dist = 10.0D + level.random.nextDouble() * 22.0D;
                            double ang = level.random.nextDouble() * Math.PI * 2.0D;
                            int sx = Mth.floor(hexedPlayer.getX() + Math.cos(ang) * dist);
                            int sz = Mth.floor(hexedPlayer.getZ() + Math.sin(ang) * dist);
                            int baseY = hexedPlayer.getBlockY();
                            net.minecraft.core.BlockPos foundPos = null;
                            for (int dy = 4; dy >= -4; dy--) {
                                net.minecraft.core.BlockPos cand = new net.minecraft.core.BlockPos(sx, baseY + dy, sz);
                                if (level.getBlockState(cand).isAir()
                                        && level.getBlockState(cand.above()).isAir()
                                        && level.getBlockState(cand.below()).isSolidRender()) {
                                    foundPos = cand;
                                    break;
                                }
                            }
                            if (foundPos != null && level.random.nextFloat() < 0.80F) {
                                Mob spawned = switch (level.random.nextInt(5)) {
                                    case 0 -> EntityType.ENDERMAN.create(level, EntitySpawnReason.MOB_SUMMONED);
                                    case 1 -> EntityType.ENDERMITE.create(level, EntitySpawnReason.MOB_SUMMONED);
                                    case 2 -> {
                                        WitherSkeleton ws = EntityType.WITHER_SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
                                        if (ws != null) ws.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
                                        yield ws;
                                    }
                                    case 3 -> {
                                        net.minecraft.world.entity.monster.Skeleton sk = EntityType.SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
                                        if (sk != null) sk.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                                        yield sk;
                                    }
                                    default -> EntityType.ZOMBIE.create(level, EntitySpawnReason.MOB_SUMMONED);
                                };
                                if (spawned != null) {
                                    spawned.setPos(foundPos.getX() + 0.5D, foundPos.getY(), foundPos.getZ() + 0.5D);
                                    spawned.setTarget(hexedPlayer);
                                    level.addFreshEntity(spawned);
                                    level.sendParticles(ParticleTypes.REVERSE_PORTAL, spawned.getX(), spawned.getY(0.8D), spawned.getZ(),
                                            16, 0.35D, 0.45D, 0.35D, 0.05D);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // Section 6.3 Waterlogged Drowning Hex: fire/heat reactions immediately evaporate & cleanse the soaking curse!
            long hexAppliedTick = data.getLong(TAG_HEX_APPLIED_TICK).orElse(0L);
            if (hexType == EssenceType.WATER
                    && (now - hexAppliedTick > 2L)
                    && (living.isInLava() || living.isOnFire())) {
                clearHexData(living, data);
                living.clearFire();
                level.sendParticles(ParticleTypes.CLOUD, living.getX(), living.getY(0.8D), living.getZ(),
                        16, 0.4D, 0.45D, 0.4D, 0.05D);
                level.playSound(null, living.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.65F, 1.35F);
                continue;
            }

            // Section 6.4 Silencing Spore Hex & Section 6.17 Strangling Phyto-Bramble Hex: burning in fire/lava incinerates & cleanses the curse!
            if ((hexType == EssenceType.NATURE || hexType == EssenceType.OVERGROWTH) && (living.isInLava() || living.isOnFire())) {
                clearHexData(living, data);
                data.remove("SpellSilencedUntil");
                level.sendParticles(ParticleTypes.SMOKE, living.getX(), living.getY(0.85D), living.getZ(),
                        18, 0.4D, 0.45D, 0.4D, 0.04D);
                level.playSound(null, living.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.65F, 1.25F);
                continue;
            }

            // Section 6.6 Permafrost Stasis Hex, Section 6.16 Absolute-Zero Cryo-Hex & Section 6.20 Petrifying Frost-Sap Hex:
            // fire/lava reactions immediately thaw & cleanse the deep cryogenic curse!
            if ((hexType == EssenceType.FROZEN || hexType == EssenceType.GLACIAL || hexType == EssenceType.TAIGA)
                    && (living.isInLava() || living.isOnFire())) {
                clearHexData(living, data);
                living.setTicksFrozen(0);
                level.sendParticles(ParticleTypes.CLOUD, living.getX(), living.getY(0.85D), living.getZ(),
                        18, 0.4D, 0.45D, 0.4D, 0.05D);
                level.playSound(null, living.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.70F, 1.35F);
                continue;
            }

            // Section 6.18 Choking Powder Smother Hex & Section 6.21 Dehydrating Sunstroke Hex:
            // submerging in water or rain washes away choking powder and quenches parching sunstroke!
            if ((hexType == EssenceType.DUST || hexType == EssenceType.OASIS) && living.isInWaterOrRain()) {
                clearHexData(living, data);
                data.remove("SpellSilencedUntil");
                level.sendParticles(ParticleTypes.SPLASH, living.getX(), living.getY(0.85D), living.getZ(),
                        18, 0.4D, 0.45D, 0.4D, 0.08D);
                level.playSound(null, living.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.65F, 1.35F);
                continue;
            }

            MateriaPartMutationHandler hexHandler = SpellMutationHandlerRegistry.resolveActive(hexType, hexMutated);
            if (hexHandler != null && hexHandler.onPersistentHexTick(level, living, tier)) {
                if (hexMutated && hexType == EssenceType.RADIANT && living.isAlive()) {
                    applyTimedAttributeShred(living, level, resolveSunScorchedHexArmorMelt(getHexStackLevel(living)), 0.0D, 45L);
                }
                continue;
            }
            if (hexMutated && (hexType == EssenceType.EARTH || hexType == EssenceType.FROZEN || hexType == EssenceType.NATURE)) {
                continue;
            }

            float hexDot = (0.75F + (tier * 0.45F)) * getHexStackMultiplier(living);
            living.hurtServer(level, level.damageSources().magic(), hexDot);
            if (data.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                data.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(data.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(living.getHealth()), living.getHealth()));
            }
            level.sendParticles(ParticleTypes.WITCH, living.getX(), living.getY(0.7), living.getZ(),
                    5, 0.25, 0.35, 0.25, 0.02);

            if (hexMutated && MateriaFamilyTier.isFrozenLineage(hexType) && hexType != EssenceType.FROZEN) {
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
        SpellExecutionEngine.tryTriggerCorpseBloomOnDeath(level, deceased);
        CompoundTag data = EntityHelper.getPersistentData(deceased);
        long now = level.getGameTime();

        // Spore (#18) Mycelial Infestation Strike: dying host bursts into infectious Poison/Nausea spore pop + mushrooms!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL).orElse(0L)) {
            // Section 6.7 Cauterizing Cleave & Auto-Smelt: strictly cook fresh meat drops & pre-smelt raw ores from the killing blow!
            data.remove(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL);
            Vec3 deathPos = deceased.position();
            ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.dropHeldRawOresPreSmelted(level, deceased);
            cookFreshCauterizingDeathDrops(level, deceased.getBoundingBox().inflate(2.6D));
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    null, deathPos, 2.6D, 2, now, null, null, EssenceType.NETHER, 1.0F,
                    (srv, z, el) -> {
                        cookFreshCauterizingDeathDrops(
                                srv, new AABB(z.center.x - 2.6D, z.center.y - 1.8D, z.center.z - 2.6D,
                                        z.center.x + 2.6D, z.center.y + 2.6D, z.center.z + 2.6D)
                        );
                        return el < z.maxDurationTicks;
                    }
            ));
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL).orElse(0L)) {
            // Section 6.8 Holy Smite & Daylight Ignition: lethal kill disintegrates the target into glowing stardust
            // rather than dropping standard rotting meat!
            data.remove(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL);
            Vec3 deathPos = deceased.position().add(0.0D, deceased.getBbHeight() * 0.55D, 0.0D);
            SpellWorldVfxManager.broadcast(
                    level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                    deathPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.RADIANT, null, 1.95F, 56, 3
            );
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), 0xFFE278, 42, 1.15F, 0.08F),
                    deathPos.x, deathPos.y, deathPos.z, 26, 0.45D, 0.55D, 0.45D, 0.08D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.RUNE_SOWILO.get(), 0xFFFDF0, 34, 1.10F, 0.05F),
                    deathPos.x, deathPos.y + 0.15D, deathPos.z, 6, 0.38D, 0.40D, 0.38D, 0.02D);
            level.sendParticles(ParticleTypes.END_ROD, deathPos.x, deathPos.y, deathPos.z,
                    32, 0.45D, 0.55D, 0.45D, 0.14D);
            level.playSound(null, deceased.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.1F, 1.45F);
            int convertedNow = ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.purifyRottingDropsToStardust(
                    level, deceased.getBoundingBox().inflate(2.6D), deathPos
            );
            final boolean[] spawnedStardust = {convertedNow > 0};
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    null, deathPos, 2.6D, 2, now, null, null, EssenceType.RADIANT, 1.0F,
                    (srv, z, el) -> {
                        int c = ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.purifyRottingDropsToStardust(
                                srv, new AABB(z.center.x - 2.6D, z.center.y - 1.8D, z.center.z - 2.6D,
                                        z.center.x + 2.6D, z.center.y + 2.6D, z.center.z + 2.6D),
                                z.center
                        );
                        if (c > 0) spawnedStardust[0] = true;
                        if (el >= z.maxDurationTicks && !spawnedStardust[0] && !(deceased instanceof Player) && !(deceased instanceof SpellEidolonEntity)) {
                            ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.spawnGuaranteedStardustBurst(srv, z.center);
                        }
                        return el < z.maxDurationTicks;
                    }
            ));
        }
        // Undead (#9) Corpse Bloom Mine: converts slain mobs directly into harvestable Bone Meal!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_CORPSE_BLOOM_BONEMEAL_UNTIL).orElse(0L)
                && !(deceased instanceof Player)
                && !(deceased instanceof SpellEidolonEntity)) {
            data.remove(SpellLingeringZoneManager.TAG_CORPSE_BLOOM_BONEMEAL_UNTIL);
            int boneMealCount = 3 + level.random.nextInt(3);
            ItemEntity mealDrop = new ItemEntity(
                    level, deceased.getX(), deceased.getY() + 0.35D, deceased.getZ(),
                    new ItemStack(Items.BONE_MEAL, boneMealCount)
            );
            mealDrop.setDefaultPickUpDelay();
            level.addFreshEntity(mealDrop);
            level.sendParticles(
                    new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.BONE_BLOCK.defaultBlockState()),
                    deceased.getX(), deceased.getY(0.5D), deceased.getZ(),
                    22, 0.35D, 0.35D, 0.35D, 0.08D
            );
            level.sendParticles(ParticleTypes.SOUL, deceased.getX(), deceased.getY(0.6D), deceased.getZ(),
                    10, 0.30D, 0.30D, 0.30D, 0.04D);
            level.playSound(null, deceased.blockPosition(), SoundEvents.BONE_BLOCK_BREAK, SoundSource.PLAYERS, 0.90F, 0.85F);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SPORE_INFESTED_UNTIL).orElse(0L)) {
            ddraig.net.entropica.spell.mutation.element.SporeSpellMutationHandler.detonateInfestedCorpseSporePopPublic(
                    level, deceased, now
            );
        }

        // Vitae (#21) Cellular Overcharge Mine: dying marked host detonates into a 6m player/ally healing explosion!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_VITAE_DEATH_BLOOM_UNTIL).orElse(0L)) {
            int bloomTier = Math.clamp(data.getInt("SpellVitaeDeathBloomTier").orElse(1), 1, 10);
            float bloomHeal = 6.0F + bloomTier * 1.40F;
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, deceased.position().add(0.0D, 0.35D, 0.0D), 6.0D, EssenceType.VITAE, EssenceType.NATURE, bloomTier
            );
            level.sendParticles(ParticleTypes.HEART, deceased.getX(), deceased.getY(0.85D), deceased.getZ(),
                    18, 0.8D, 0.5D, 0.8D, 0.05D);
            level.playSound(null, deceased.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F, 0.95F);
            level.playSound(null, deceased.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.35F);
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(6.0D))) {
                if (!ally.isAlive() || ally == deceased || ally instanceof Monster) continue;
                notifyMagicalHeal(ally, bloomHeal);
                ally.heal(bloomHeal);
                ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 0));
            }
        }

        // Blood (#22) Vampiric Exsanguination Strike: lethal kill restores exact 2 hunger shanks (4 food) & grants Speed II for 5s!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BLOOD_FEAST_UNTIL).orElse(0L)) {
            Player feastTarget = null;
            String fId = data.getString("SpellBloodFeastCaster").orElse("");
            if (!fId.isEmpty()) {
                try {
                    Entity fe = level.getEntity(java.util.UUID.fromString(fId));
                    if (fe instanceof Player p && p.isAlive()) feastTarget = p;
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (feastTarget != null) {
                feastTarget.getFoodData().eat(4, 0.65F);
                feastTarget.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1));
                level.sendParticles(
                        ddraig.net.entropica.client.particle.TimedTintableParticleOption.linear(
                                ddraig.net.entropica.registry.ModParticles.TINTABLE_BLOOD.get(), 0x8A0303, 22, 0.95F, 0.05F),
                        feastTarget.getX(), feastTarget.getY(0.95D), feastTarget.getZ(), 10, 0.32D, 0.35D, 0.32D, 0.04D
                );
                level.playSound(null, feastTarget.blockPosition(), SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 0.85F, 0.92F);
            } else {
                for (Player nearPlayer : level.getEntitiesOfClass(Player.class, deceased.getBoundingBox().inflate(8.0D))) {
                    if (nearPlayer.isAlive()) {
                        nearPlayer.getFoodData().eat(4, 0.65F);
                        nearPlayer.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1));
                        break;
                    }
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
                if (level.getBlockState(sp).isAir()
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, sp, level.getBlockState(sp))) {
                    level.setBlockAndUpdate(sp, net.minecraft.world.level.block.Blocks.HONEY_BLOCK.defaultBlockState());
                    effigyBlocks.add(sp);
                }
                if (level.getBlockState(sp.above()).isAir()
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, sp.above(), level.getBlockState(sp.above()))) {
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
                    if (level.getBlockState(sp).isAir()
                            && !SpellExecutionEngine.isProtectedBlockOrContainer(level, sp, level.getBlockState(sp))) {
                        level.setBlockAndUpdate(sp, net.minecraft.world.level.block.Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
                    }
                    if (level.getBlockState(sp.above()).isAir()
                            && !SpellExecutionEngine.isProtectedBlockOrContainer(level, sp.above(), level.getBlockState(sp.above()))) {
                        level.setBlockAndUpdate(sp.above(), net.minecraft.world.level.block.Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
                    }
                    level.playSound(null, sp, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.PLAYERS, 1.0F, 0.85F);
                }
            }
        }

        // Blight (#26) Contagious Necro-Spore Pod, Cadaverous Contagion Chains, Necrotic Thorn Gouge & Rot-Briar Snare!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BLIGHT_POD_CONTAGION_UNTIL).orElse(0L)) {
            // Section 6.27 Contagious Necro-Spore Pod: dying while rooted spreads 2.5s root + Poison II cloud to nearby enemies!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, deceased.position().add(0.0D, 0.08D, 0.0D), 3.0D, EssenceType.BLIGHT, null, 2
            );
            for (LivingEntity cloudVictim : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(3.0D))) {
                if (cloudVictim == deceased || !cloudVictim.isAlive() || !(cloudVictim instanceof Monster)) continue;
                cloudVictim.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                cloudVictim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                cloudVictim.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                applyTimedMovementSpeedSap(cloudVictim, level, 0.95D, 50L);
                EntityHelper.getPersistentData(cloudVictim).putLong(SpellLingeringZoneManager.TAG_BLIGHT_POD_CONTAGION_UNTIL, now + 50L);
                hurtWithBlightMateriaAdvantage(level, cloudVictim, null, 3.2F, false, true);
            }
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BLIGHT_CHAIN_MARK_UNTIL).orElse(0L)) {
            // Section 6.27 Cadaverous Contagion Chains Flow: dying within 3.0s immediately leaps to 2 additional nearby hostiles!
            Vec3 fromEye = deceased.position().add(0.0D, deceased.getBbHeight() * 0.58D, 0.0D);
            int jumped = 0;
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(6.5D))) {
                if (foe == deceased || !foe.isAlive() || !(foe instanceof Monster)) continue;
                Vec3 toEye = foe.position().add(0.0D, foe.getBbHeight() * 0.58D, 0.0D);
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(level, fromEye, toEye, EssenceType.BLIGHT, null, 2);
                foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 6));
                foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 30, 0));
                applyTimedMovementSpeedSap(foe, level, 0.95D, 30L);
                applyTimedAttributeShred(foe, level, 1.0D, 0.0D, 100L);
                EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_BLIGHT_CHAIN_MARK_UNTIL, now + 60L);
                hurtWithBlightMateriaAdvantage(level, foe, null, 4.2F, false, true);
                if (++jumped >= 2) break;
            }
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BLIGHT_CORPSE_SNARE_UNTIL).orElse(0L)) {
            // Section 6.27 Necrotic Thorn Gouge: corpse bursts into a root snare trapping adjacent enemies for 3.0s (60t)!
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                    level, deceased.position().add(0.0D, 0.04D, 0.0D), 3.5D, EssenceType.BLIGHT, null, 60, 2
            );
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(3.5D))) {
                if (foe == deceased || !foe.isAlive() || !(foe instanceof Monster)) continue;
                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
                foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                applyTimedMovementSpeedSap(foe, level, 0.95D, 60L);
                foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
            }
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BLIGHT_WITHER_ROSE_SNARE_UNTIL).orElse(0L)) {
            // Section 6.27 Strangling Rot-Briar Snare: leaves behind a cluster of Wither Roses when an entity dies inside!
            net.minecraft.core.BlockPos dp = deceased.blockPosition();
            int plantedRoses = 0;
            for (net.minecraft.core.BlockPos cand : List.of(dp, dp.north(), dp.south(), dp.east(), dp.west())) {
                if (level.getBlockState(cand).isAir()
                        && level.getBlockState(cand.below()).isSolidRender()
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, cand, level.getBlockState(cand))) {
                    level.setBlockAndUpdate(cand, net.minecraft.world.level.block.Blocks.WITHER_ROSE.defaultBlockState());
                    if (++plantedRoses >= 3) break;
                }
            }
        }

        // Soulfire (#28) Phantasmal Soul Cleave (3m soulburst), Chain Burst (damages all chained peers) & Beam Soul Sand Dissolution!
        boolean chainSoulBurst = now <= data.getLong(SpellLingeringZoneManager.TAG_SOULFIRE_CHAIN_BURST_UNTIL).orElse(0L);
        boolean cleaveSoulBurst = now <= data.getLong(SpellLingeringZoneManager.TAG_SOULFIRE_CLEAVE_BURST_UNTIL).orElse(0L);
        boolean soulSandMark = now <= data.getLong(SpellLingeringZoneManager.TAG_SOULFIRE_SOULSAND_MARK_UNTIL).orElse(0L);
        if (chainSoulBurst || cleaveSoulBurst || soulSandMark) {
            LivingEntity burstCaster = deceased.getKillCredit() != null ? deceased.getKillCredit() : deceased.getLastHurtByMob();
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, deceased.getX(), deceased.getY(0.7D), deceased.getZ(), 28, 1.1D, 0.5D, 1.1D, 0.06D);
            level.sendParticles(ParticleTypes.SOUL, deceased.getX(), deceased.getY(0.8D), deceased.getZ(), 10, 0.5D, 0.5D, 0.5D, 0.04D);
            long burnUntil = now + 100L;
            java.util.Set<java.util.UUID> soulBurstHit = new java.util.HashSet<>();
            if (cleaveSoulBurst || chainSoulBurst) {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                        level, deceased.position(), 3.0D, EssenceType.SOULFIRE, EssenceType.UNDEAD, 2
                );
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(3.0D))) {
                    if (foe == deceased || foe == burstCaster || !foe.isAlive()) continue;
                    if (burstCaster != null && isProtectedFromOffensiveSpell(foe, burstCaster, true)) continue;
                    if (!(foe instanceof Monster) && !(foe instanceof Mob m && m.getTarget() == burstCaster)) continue;
                    soulBurstHit.add(foe.getUUID());
                    EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);
                    foe.igniteForSeconds(5);
                    hurtWithSoulfireMateriaAdvantage(level, foe, burstCaster, 4.25F, false, true);
                }
            }
            if (chainSoulBurst) {
                Vec3 deadMid = deceased.position().add(0.0D, deceased.getBbHeight() * 0.55D, 0.0D);
                for (LivingEntity chainedPeer : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(18.0D))) {
                    if (chainedPeer == deceased || chainedPeer == burstCaster || !chainedPeer.isAlive() || soulBurstHit.contains(chainedPeer.getUUID())) continue;
                    if (EntityHelper.getPersistentData(chainedPeer).getLong(SpellLingeringZoneManager.TAG_SOULFIRE_CHAIN_BURST_UNTIL).orElse(0L) > now) {
                        Vec3 peerMid = chainedPeer.position().add(0.0D, chainedPeer.getBbHeight() * 0.55D, 0.0D);
                        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                                level, deadMid, peerMid, EssenceType.SOULFIRE, EssenceType.UNDEAD, 2
                        );
                        EntityHelper.getPersistentData(chainedPeer).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);
                        chainedPeer.igniteForSeconds(5);
                        hurtWithSoulfireMateriaAdvantage(level, chainedPeer, burstCaster, 4.75F, false, true);
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
            LivingEntity boilCaster = deceased.getKillCredit() != null ? deceased.getKillCredit() : deceased.getLastHurtByMob();
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                    level, deceased.position(), 3.5D, EssenceType.VAPOR, EssenceType.WATER, 2
            );
            level.sendParticles(ParticleTypes.CLOUD, deceased.getX(), deceased.getY(0.8D), deceased.getZ(), 28, 1.3D, 0.6D, 1.3D, 0.05D);
            level.playSound(null, deceased.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.95F, 1.15F);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(3.5D))) {
                if (foe == deceased || foe == boilCaster || !foe.isAlive()) continue;
                if (boilCaster != null && isProtectedFromOffensiveSpell(foe, boilCaster, true)) continue;
                if (!(foe instanceof Monster) && !(foe instanceof Mob m && m.getTarget() == boilCaster)) continue;
                foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                SpellLingeringZoneManager.applySoaked(foe, 120);
                hurtWithVaporMateriaAdvantage(level, foe, boilCaster, 2.5F, false, true);
            }
        }

        // Section 1.2B Entangling Miasma Death Contagion: virulent root-rot spreads to adjacent mobs within 5 blocks upon death!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_UNTIL).orElse(0L)) {
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, deceased.getX(), deceased.getY(0.7D), deceased.getZ(),
                    28, 0.9D, 0.55D, 0.9D, 0.06D);
            level.sendParticles(ParticleTypes.SOUL, deceased.getX(), deceased.getY(0.75D), deceased.getZ(),
                    14, 0.7D, 0.5D, 0.7D, 0.04D);
            for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(5.0D), LivingEntity::isAlive)) {
                if (adj == deceased || adj instanceof Player || adj instanceof SpellEidolonEntity) continue;
                if (adj instanceof Enemy || (adj instanceof Mob mob && mob.getTarget() != null)) {
                    adj.removeEffect(MobEffects.REGENERATION);
                    adj.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
                    adj.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
                    adj.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                    CompoundTag adjData = EntityHelper.getPersistentData(adj);
                    adjData.putLong(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_UNTIL, now + 160L);
                    adjData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, adj.getHealth());
                }
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
        LivingEntity origHexCaster = resolveHexCaster(level, deceased);
        CompoundTag casterData = origHexCaster != null ? EntityHelper.getPersistentData(origHexCaster) : null;
        if (!canSpreadHexOnDeath(data, casterData, now)) {
            return;
        }

        long window = data.getLong(TAG_HEX_INITIAL_WINDOW).orElse(0L);
        long activeChannelUntil = resolveActiveHexChannelUntil(data, casterData);
        long inheritedContagionWindow = Math.max(window, activeChannelUntil);
        int tier = data.getInt(TAG_HEX_TIER).orElse(1);
        boolean tier6Contagion = hexMutated && (hexType == EssenceType.ESCHATON
                || hexType == EssenceType.APOTHEOSIS
                || hexType == EssenceType.ENTROPICA);
        double spreadRadius = tier6Contagion ? 6.0D : 5.0D;
        AABB box = deceased.getBoundingBox().inflate(spreadRadius);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (nearby == deceased || !nearby.isAlive() || nearby instanceof Player || nearby instanceof SpellEidolonEntity) continue;
            if (hexType == EssenceType.UNDEAD && nearby.getType().is(EntityTypeTags.UNDEAD)) continue;
            if (nearby instanceof Enemy || (nearby instanceof Mob mob && mob.getTarget() != null)) {
                if (applyPersistentHexContagionLevelOne(
                        level, origHexCaster, nearby, hexType, tier, hexMutated, inheritedContagionWindow
                )) {
                    level.sendParticles(ParticleTypes.SOUL, nearby.getX(), nearby.getY(0.6), nearby.getZ(),
                            8, 0.25, 0.3, 0.25, 0.04);
                }
            }
        }
    }

    /**
     * Section 5.6 Death Contagion Gate: ALL Persistent Hexes (including Undead &amp; Blight) spread on death
     * ONLY within the initial affliction activation window or while the caster's spell is actively channeled.
     */
    public static boolean canSpreadHexOnDeath(CompoundTag deceasedData, @Nullable CompoundTag casterData, long now) {
        long window = deceasedData.getLong(TAG_HEX_INITIAL_WINDOW).orElse(0L);
        long activelyChanneledUntil = resolveActiveHexChannelUntil(deceasedData, casterData);
        return now <= window || now <= activelyChanneledUntil;
    }

    public static long resolveActiveHexChannelUntil(CompoundTag deceasedData, @Nullable CompoundTag casterData) {
        long beamTickMax = Math.max(
                Math.max(
                        Math.max(
                                deceasedData.getLong("EmpyreanBeamLastTick").orElse(0L),
                                deceasedData.getLong("CataclysmBeamLastTick").orElse(0L)
                        ),
                        Math.max(
                                deceasedData.getLong("TerminusBeamLastTick").orElse(0L),
                                deceasedData.getLong("PrismaticBeamLastTick").orElse(0L)
                        )
                ),
                Math.max(
                        deceasedData.getLong("EschatonBeamLastTick").orElse(0L),
                        Math.max(
                                deceasedData.getLong("ApotheosisBeamLastTick").orElse(0L),
                                deceasedData.getLong("EntropicaBeamLastTick").orElse(0L)
                        )
                )
        );
        long beamChannelUntil = beamTickMax > 0L ? (beamTickMax + 30L) : 0L;
        long victimChannelUntil = deceasedData.getLong(SpellLingeringZoneManager.TAG_ACTIVE_SPELL_CHANNEL_UNTIL).orElse(0L);
        long casterChannelUntil = casterData != null
                ? casterData.getLong(SpellLingeringZoneManager.TAG_ACTIVE_SPELL_CHANNEL_UNTIL).orElse(0L)
                : 0L;
        return Math.max(beamChannelUntil, Math.max(victimChannelUntil, casterChannelUntil));
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

        int clampedTier = Math.clamp(potencyTier, 1, 10);
        int dur = Math.max(60, durationTicks);
        EssenceType resolvedWard = wardMateria == null ? EssenceType.AEGIS : wardMateria;

        data.putFloat(TAG_WARD_HP, merged);
        data.putFloat(TAG_WARD_MAX_HP, Math.max(merged, data.getFloat(TAG_WARD_MAX_HP).orElse(0.0F)));
        data.putLong(TAG_WARD_UNTIL, now + dur);
        data.putString(TAG_WARD_MATERIA, resolvedWard.name());
        data.putInt(TAG_WARD_TIER, clampedTier);

        syncEntityBarrierState(level, target, 0.55F, null);

        level.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY(1.0), target.getZ(),
                20, 0.45, 0.6, 0.45, 0.15);
        level.playSound(null, target.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7F, 1.65F);

        if (target instanceof Player player) {
            player.displayClientMessage(Component.literal(String.format(
                    "§b[Arcane Ward] §fShield Active: §b%.1f HP §7(%ds)", merged, dur / 20
            )), true);
        }
    }

    /**
     * Synchronizes any active Ward or Aegis / Elemental Barrier Mantle on {@code entity} to tracking clients
     * so the 3D barrier shell stays continuously visible while active and flares brightly in opacity on hit.
     */
    public static boolean syncEntityBarrierState(
            ServerLevel level,
            LivingEntity entity,
            float hitFlare,
            @Nullable Vec3 hitOrigin
    ) {
        if (!entity.isAlive()) {
            return false;
        }
        CompoundTag data = EntityHelper.getPersistentData(entity);
        long now = level.getGameTime();

        // 1. Primary HP-Pool Arcane Ward
        long wardUntil = data.getLong(TAG_WARD_UNTIL).orElse(0L);
        float wardHp = data.getFloat(TAG_WARD_HP).orElse(0.0F);
        if (wardHp > 0.0F && wardUntil > now) {
            EssenceType wardMat = EssenceType.AEGIS;
            try {
                wardMat = EssenceType.valueOf(data.getString(TAG_WARD_MATERIA).orElse("AEGIS"));
            } catch (IllegalArgumentException ignored) {
            }
            float maxHp = Math.max(wardHp, data.getFloat(TAG_WARD_MAX_HP).orElse(wardHp));
            float ratio = Mth.clamp(wardHp / Math.max(1.0F, maxHp), 0.08F, 1.0F);
            int tier = Math.clamp(data.getInt(TAG_WARD_TIER).orElse(1), 1, 10);

            EssenceType secondary = null;
            if (wardMat != EssenceType.AEGIS
                    && (data.getLong(SpellLingeringZoneManager.TAG_AEGIS_MANTLE_UNTIL).orElse(0L) > now
                    || data.getLong(SpellLingeringZoneManager.TAG_AEGIS_TETHER_UNTIL).orElse(0L) > now
                    || data.getLong(SpellLingeringZoneManager.TAG_AEGIS_ORBIT_UNTIL).orElse(0L) > now)) {
                secondary = EssenceType.AEGIS;
            } else if (wardMat != EssenceType.AIR
                    && (data.getLong(SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL).orElse(0L) > now
                    || data.getLong(SpellLingeringZoneManager.TAG_AIR_SLIPSTREAM_UNTIL).orElse(0L) > now)) {
                secondary = EssenceType.AIR;
            }

            int remTicks = (int) Math.min(Integer.MAX_VALUE, wardUntil - now);
            SpellWardClientManager.broadcastWardState(level, entity, remTicks, wardMat, secondary, tier, ratio, hitFlare, hitOrigin);
            return true;
        }

        // 2. Active Aegis Mantles, Citadel Tethers, Gust Shields & Protective Barrier Domes
        long barrierUntil = 0L;
        EssenceType primary = null;
        EssenceType secondary = null;
        float ratio = 1.0F;
        int tier = Math.clamp(data.getInt(TAG_WARD_TIER).orElse(data.getInt("SpellAirSlipstreamTier").orElse(3)), 1, 10);

        if (data.getLong(SpellLingeringZoneManager.TAG_COMPLETE_INVULNERABILITY_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_COMPLETE_INVULNERABILITY_UNTIL).orElse(0L);
            primary = EssenceType.APOTHEOSIS;
            secondary = EssenceType.AEGIS;
            tier = Math.max(tier, 8);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_AEGIS_MANTLE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_AEGIS_MANTLE_UNTIL).orElse(0L);
            primary = EssenceType.AEGIS;
            secondary = EssenceType.RADIANT;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_AEGIS_TETHER_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_AEGIS_TETHER_UNTIL).orElse(0L);
            primary = EssenceType.AEGIS;
            secondary = EssenceType.RADIANT;
            ratio = 0.85F;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_AEGIS_ORBIT_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_AEGIS_ORBIT_UNTIL).orElse(0L);
            primary = EssenceType.AEGIS;
            ratio = 0.78F;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.EMPYREAN;
            secondary = EssenceType.NATURE;
            tier = Math.max(tier, 7);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_AEGIS_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_AEGIS_UNTIL).orElse(0L);
            primary = EssenceType.EMPYREAN;
            secondary = EssenceType.AEGIS;
            tier = Math.max(tier, 7);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_ORBIT_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_ORBIT_UNTIL).orElse(0L);
            primary = EssenceType.EMPYREAN;
            secondary = EssenceType.VITAE;
            tier = Math.max(tier, 7);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_CATACLYSM_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_CATACLYSM_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.CATACLYSM;
            secondary = EssenceType.VOID;
            tier = Math.max(tier, 7);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_CATACLYSM_ORBIT_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_CATACLYSM_ORBIT_UNTIL).orElse(0L);
            primary = EssenceType.CATACLYSM;
            secondary = EssenceType.NETHER;
            tier = Math.max(tier, 7);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_AETHER_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_AETHER_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.AETHER;
            secondary = EssenceType.AIR;
            tier = Math.max(tier, 6);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_AETHER_AEGIS_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_AETHER_AEGIS_UNTIL).orElse(0L);
            primary = EssenceType.AETHER;
            secondary = EssenceType.AEGIS;
            tier = Math.max(tier, 6);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_ENTROPIC_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_ENTROPIC_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.ENTROPIC;
            secondary = EssenceType.VOID;
            tier = Math.max(tier, 6);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_ENTROPIC_LIFESTEAL_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_ENTROPIC_LIFESTEAL_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.ENTROPIC;
            secondary = EssenceType.BLOOD;
            tier = Math.max(tier, 6);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.CELESTIAL;
            secondary = EssenceType.RADIANT;
            tier = Math.max(tier, 6);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_ASCENDANCE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_ASCENDANCE_UNTIL).orElse(0L);
            primary = EssenceType.CELESTIAL;
            secondary = EssenceType.VITAE;
            tier = Math.max(tier, 6);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL).orElse(0L) > now
                && data.getInt(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_HITS).orElse(0) > 0) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL).orElse(0L);
            primary = EssenceType.SYLVAN;
            secondary = EssenceType.AEGIS;
            ratio = Mth.clamp(data.getInt(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_HITS).orElse(1) / 12.0F, 0.10F, 1.0F);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.TERMINUS;
            secondary = EssenceType.FROZEN;
            tier = Math.max(tier, 7);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_MANTLE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_MANTLE_UNTIL).orElse(0L);
            primary = EssenceType.TERMINUS;
            secondary = EssenceType.WATER;
            tier = Math.max(tier, 7);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_ORBIT_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_ORBIT_UNTIL).orElse(0L);
            primary = EssenceType.TERMINUS;
            secondary = EssenceType.AIR;
            tier = Math.max(tier, 7);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_ESCHATON_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_ESCHATON_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.ESCHATON;
            secondary = EssenceType.VOID;
            tier = Math.max(tier, 8);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_ESCHATON_ORBIT_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_ESCHATON_ORBIT_UNTIL).orElse(0L);
            primary = EssenceType.ESCHATON;
            secondary = EssenceType.VOID;
            tier = Math.max(tier, 8);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.APOTHEOSIS;
            secondary = EssenceType.RADIANT;
            tier = Math.max(tier, 8);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_ORBIT_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_ORBIT_UNTIL).orElse(0L);
            primary = EssenceType.APOTHEOSIS;
            secondary = EssenceType.RADIANT;
            tier = Math.max(tier, 8);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.ENTROPICA;
            secondary = EssenceType.ASTRAL;
            tier = Math.max(tier, 9);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_ENTROPICA_ORBIT_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_ENTROPICA_ORBIT_UNTIL).orElse(0L);
            primary = EssenceType.ENTROPICA;
            secondary = EssenceType.ASTRAL;
            tier = Math.max(tier, 9);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_PRISMATIC_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_PRISMATIC_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.PRISMATIC;
            secondary = EssenceType.RADIANT;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_PRISMATIC_ORBIT_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_PRISMATIC_ORBIT_UNTIL).orElse(0L);
            primary = EssenceType.PRISMATIC;
            secondary = EssenceType.CHIMERA;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_PRISMATIC_DEFLECT_SENTRY_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_PRISMATIC_DEFLECT_SENTRY_UNTIL).orElse(0L);
            primary = EssenceType.PRISMATIC;
            secondary = EssenceType.AEGIS;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L);
            primary = EssenceType.FROZEN;
            secondary = EssenceType.STASIS;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_CRYO_COCOON_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_CRYO_COCOON_UNTIL).orElse(0L);
            primary = EssenceType.GLACIAL;
            secondary = EssenceType.FROZEN;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL).orElse(0L) > now
                || data.getLong(SpellLingeringZoneManager.TAG_AIR_SLIPSTREAM_UNTIL).orElse(0L) > now) {
            barrierUntil = Math.max(
                    data.getLong(SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL).orElse(0L),
                    data.getLong(SpellLingeringZoneManager.TAG_AIR_SLIPSTREAM_UNTIL).orElse(0L)
            );
            primary = EssenceType.AIR;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.WATER;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L);
            primary = EssenceType.NATURE;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_NULL_R_BASTION_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_NULL_R_BASTION_UNTIL).orElse(0L);
            primary = EssenceType.NULL_R;
            secondary = EssenceType.AEGIS;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_NULL_U_VEIL_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_NULL_U_VEIL_UNTIL).orElse(0L);
            primary = EssenceType.NULL_U;
            secondary = EssenceType.UMBRAL;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_DAWN_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_DAWN_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.DAWN;
            secondary = EssenceType.AIR;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_ABYSS_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_ABYSS_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.ABYSS;
            secondary = EssenceType.WATER;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_AURORA_MANTLE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_AURORA_MANTLE_UNTIL).orElse(0L);
            primary = EssenceType.AURORA;
            secondary = EssenceType.FROZEN;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_MIRAGE_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_MIRAGE_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.MIRAGE;
            secondary = EssenceType.ARID;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.WRAITH;
            secondary = EssenceType.UNDEAD;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_BARROW_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_BARROW_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.BARROW;
            secondary = EssenceType.UNDEAD;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_PYRE_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_PYRE_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.PYRE;
            secondary = EssenceType.NETHER;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_MANTLE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_MANTLE_UNTIL).orElse(0L);
            primary = EssenceType.PENUMBRA;
            secondary = EssenceType.RADIANT;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_RIME_MANTLE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_RIME_MANTLE_UNTIL).orElse(0L);
            primary = EssenceType.RIME;
            secondary = EssenceType.FROZEN;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_SPRING_MANTLE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_SPRING_MANTLE_UNTIL).orElse(0L);
            primary = EssenceType.SPRING;
            secondary = EssenceType.WATER;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_STATIC_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_STATIC_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.STATIC;
            secondary = EssenceType.ARID;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL).orElse(0L);
            primary = EssenceType.SYLVAN;
            secondary = EssenceType.RADIANT;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_MIASMA_CARAPACE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_MIASMA_CARAPACE_UNTIL).orElse(0L);
            primary = EssenceType.MIASMA;
            secondary = EssenceType.UNDEAD;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_GENESIS_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_GENESIS_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.GENESIS;
            secondary = EssenceType.RADIANT;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_OBLIVION_EXTINCTION_ARMOR_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_OBLIVION_EXTINCTION_ARMOR_UNTIL).orElse(0L);
            primary = EssenceType.OBLIVION;
            secondary = EssenceType.NETHER;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_OBLIVION_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_OBLIVION_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.OBLIVION;
            secondary = EssenceType.UMBRAL;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_ECLIPSE_BASTION_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_ECLIPSE_BASTION_UNTIL).orElse(0L);
            primary = EssenceType.ECLIPSE;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_BLIGHT_CARAPACE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_BLIGHT_CARAPACE_UNTIL).orElse(0L);
            primary = EssenceType.BLIGHT;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_ASTRAL_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_ASTRAL_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.ASTRAL;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_SOULFIRE_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_SOULFIRE_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.SOULFIRE;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_VAPOR_MIST_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_VAPOR_MIST_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.VAPOR;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_AMBER_MANTLE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_AMBER_MANTLE_UNTIL).orElse(0L);
            primary = EssenceType.AMBER;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BASTION_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BASTION_UNTIL).orElse(0L);
            primary = EssenceType.OVERGROWTH;
            secondary = EssenceType.AEGIS;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_DUST_VEIL_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_DUST_VEIL_UNTIL).orElse(0L);
            primary = EssenceType.DUST;
            secondary = EssenceType.AIR;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_SPORE_MANTLE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_SPORE_MANTLE_UNTIL).orElse(0L);
            primary = EssenceType.SPORE;
            secondary = EssenceType.NATURE;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_SPORE_CARAPACE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_SPORE_CARAPACE_UNTIL).orElse(0L);
            primary = EssenceType.SPORE;
            secondary = EssenceType.AEGIS;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_TAIGA_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_TAIGA_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.TAIGA;
            secondary = EssenceType.FROZEN;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_OASIS_CARAPACE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_OASIS_CARAPACE_UNTIL).orElse(0L);
            primary = EssenceType.OASIS;
            secondary = EssenceType.AEGIS;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_VITAE_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_VITAE_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.VITAE;
            secondary = EssenceType.NATURE;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_BLOOD_CARAPACE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_BLOOD_CARAPACE_UNTIL).orElse(0L);
            primary = EssenceType.BLOOD;
            secondary = EssenceType.AEGIS;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_AURA_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_AURA_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.AURA;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L);
            primary = EssenceType.EARTH;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L);
            primary = EssenceType.FROZEN;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.NETHER;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_RADIANT_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_RADIANT_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.RADIANT;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.UMBRAL;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.UNDEAD;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L);
            primary = EssenceType.VOID;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_ARID_VEIL_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_ARID_VEIL_UNTIL).orElse(0L);
            primary = EssenceType.ARID;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_LIGHTNING_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_LIGHTNING_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.LIGHTNING;
            secondary = null;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L);
            primary = EssenceType.MAGMA;
            secondary = EssenceType.EARTH;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_STORM_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_STORM_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.STORM;
            secondary = EssenceType.AIR;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.GLACIAL;
            secondary = EssenceType.WATER;
        }

        if (primary != null && barrierUntil > now) {
            int remTicks = (int) Math.min(Integer.MAX_VALUE, barrierUntil - now);
            SpellWardClientManager.broadcastWardState(level, entity, remTicks, primary, secondary, tier, ratio, hitFlare, hitOrigin);
            return true;
        }
        return false;
    }

    private static void triggerBarrierShatterOrFallback(
            ServerLevel level,
            LivingEntity entity,
            EssenceType shatteredType,
            int potencyTier,
            @Nullable Vec3 hitOrigin
    ) {
        SpellWorldVfxManager.broadcastRadialNova(
                level,
                entity.position().add(0.0D, entity.getBbHeight() * 0.52D, 0.0D),
                Math.max(1.75D, entity.getBbWidth() + 1.05D),
                shatteredType,
                EssenceType.AEGIS,
                potencyTier
        );
        if (!syncEntityBarrierState(level, entity, 1.32F, hitOrigin)) {
            SpellWardClientManager.broadcastWardShatter(level, entity, shatteredType, EssenceType.AEGIS, potencyTier, hitOrigin);
        }
    }

    @Nullable
    private static Vec3 resolveHitOrigin(LivingEntity defender, DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct != null && direct != defender) {
            return direct.getBoundingBox().getCenter();
        }
        Vec3 srcPos = source.getSourcePosition();
        if (srcPos != null) {
            return srcPos;
        }
        Entity atk = source.getEntity();
        if (atk != null && atk != defender) {
            return atk.getBoundingBox().getCenter();
        }
        return null;
    }

    private static float computeHitFlare(LivingEntity defender, float damageAmount) {
        return Mth.clamp(0.65F + (damageAmount / Math.max(8.0F, defender.getMaxHealth())) * 1.35F, 0.68F, 1.32F);
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
        Vec3 hitOrigin = resolveHitOrigin(entity, source);
        float hitFlare = computeHitFlare(entity, amount);

        // Glacial Cryo-Cocoon Mending (#15) & Tier 4–6 Complete Damage Invulnerability / Void Immunity / Prismatic Mirror Dome!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_COMPLETE_INVULNERABILITY_UNTIL).orElse(0L)
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            syncEntityBarrierState(level, entity, 1.25F, hitOrigin);
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
                    hurtWithPrismaticMateriaAdvantage(level, atk, entity, Math.max(2.0F, amount), false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
            syncEntityBarrierState(level, entity, 1.20F, hitOrigin);
            level.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY(0.95D), entity.getZ(),
                    12, 0.35D, 0.45D, 0.35D, 0.05D);
            return EventResult.interruptFalse();
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_CRYO_COCOON_UNTIL).orElse(0L)
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            syncEntityBarrierState(level, entity, 1.15F, hitOrigin);
            level.sendParticles(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY(0.9D), entity.getZ(),
                    10, 0.35D, 0.45D, 0.35D, 0.04D);
            level.playSound(null, entity.blockPosition(), SoundEvents.GLASS_HIT, SoundSource.PLAYERS, 0.7F, 1.55F);
            return EventResult.interruptFalse();
        }
        // Section 6.6 Frost Nova Burst & Flash-Freeze Stasis: temporary real Blocks.ICE encasement never causes wall-suffocation ticks!
        if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)
                && hasFrozenIceSuffocationImmunity(entity, now)) {
            return EventResult.interruptFalse();
        }

        // Pacified Mob Attack Suppression (#20 Oasis Blessing Touch / Greening Mist / Mirage Decoy, #18 Spore Decoy)
        if (source.getEntity() instanceof Mob pacifiedAttacker
                && now <= EntityHelper.getPersistentData(pacifiedAttacker).getLong(SpellLingeringZoneManager.TAG_PACIFIED_UNTIL).orElse(0L)) {
            pacifiedAttacker.setTarget(null);
            pacifiedAttacker.setAggressive(false);
            return EventResult.interruptFalse();
        }

        // Section 6.6 Flash-Freeze Stasis / Ice Block Encasement: completely halts outgoing attacks while encased in solid ice!
        Entity rawAttacker = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
        if (rawAttacker instanceof LivingEntity stasisAttacker
                && stasisAttacker != entity
                && now <= EntityHelper.getPersistentData(stasisAttacker).getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L)) {
            if (stasisAttacker instanceof Mob stasisMob) {
                stasisMob.setAggressive(false);
            }
            return EventResult.interruptFalse();
        }

        // Section 6.6 Glacial Spikes Retaliation: enforce proportional +30% slower melee swing interval on chilled hostile Mobs!
        if (rawAttacker instanceof Mob chilledMob
                && chilledMob != entity
                && !source.is(DamageTypeTags.IS_PROJECTILE)) {
            CompoundTag atkTag = EntityHelper.getPersistentData(chilledMob);
            if (now <= atkTag.getLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_UNTIL).orElse(0L)) {
                long nextAllowed = atkTag.getLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED).orElse(0L);
                if (now < nextAllowed) {
                    return EventResult.interruptFalse();
                }
                long lastSwing = atkTag.getLong("SpellChilledLastSwingTick").orElse(-100L);
                long baseInterval = (lastSwing > 0L && now > lastSwing && (now - lastSwing) <= 40L)
                        ? Math.max(10L, Math.round((now - lastSwing) / 1.30D))
                        : 20L;
                atkTag.putLong("SpellChilledLastSwingTick", now);
                atkTag.putLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED,
                        now + resolveChilledMobSwingCooldownTicks(baseInterval));
            } else if (hasActiveMutatedHex(atkTag, now, EssenceType.LIGHTNING)
                    || hasActiveMutatedHex(atkTag, now, EssenceType.STORM)
                    || hasActiveMutatedHex(atkTag, now, EssenceType.BARROW)
                    || hasActiveMutatedHex(atkTag, now, EssenceType.SPRING)
                    || hasActiveMutatedHex(atkTag, now, EssenceType.STATIC)
                    || hasActiveMutatedHex(atkTag, now, EssenceType.SYLVAN)) {
                long nextAllowed = atkTag.getLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED).orElse(0L);
                if (now < nextAllowed) {
                    return EventResult.interruptFalse();
                }
                // Section 6.13 Galvanic Overcharge & Section 6.15 Waterlogged Hypoxia Hex: 20 / 0.60 = 33.3t (~33 ticks -> true -40% mob melee attack speed!)
                atkTag.putLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED, now + 33L);
            } else if (hasActiveMutatedHex(atkTag, now, EssenceType.OASIS)) {
                long nextAllowed = atkTag.getLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED).orElse(0L);
                if (now < nextAllowed) {
                    return EventResult.interruptFalse();
                }
                // Section 6.21 Dehydrating Sunstroke Hex: 20 / 0.65 = 30.8t (~31 ticks -> true -35% mob melee attack speed!)
                atkTag.putLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED, now + 31L);
            } else if (hasActiveMutatedHex(atkTag, now, EssenceType.AURORA)) {
                long nextAllowed = atkTag.getLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED).orElse(0L);
                if (now < nextAllowed) {
                    return EventResult.interruptFalse();
                }
                // Section 6.36 Sub-Zero Polar Stasis Hex: 20 / 0.50 = 40 ticks -> true -50% mob melee attack speed!
                atkTag.putLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED, now + 40L);
            }
        }

        // Section 4.4 Primed Touch / Melee Strike Infusion: discharge primed touch spell on next physical swing!
        if (!REENTRANT_GUST_HURT.get()
                && source.getDirectEntity() instanceof LivingEntity meleeAttacker
                && meleeAttacker != entity
                && !source.is(DamageTypeTags.IS_PROJECTILE)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            boolean seismicConversion = SpellExecutionEngine.hasPrimedEarthSeismicStrike(meleeAttacker);
            boolean flashFreezeConversion = SpellExecutionEngine.hasPrimedFrozenFlashFreezeStrike(meleeAttacker);
            boolean flashFreezePermafrost = flashFreezeConversion && SpellExecutionEngine.hasPrimedFrozenPermafrostCreep(meleeAttacker);
            boolean cauterizingConversion = SpellExecutionEngine.hasPrimedNetherCauterizingStrike(meleeAttacker);
            boolean holySmiteConversion = SpellExecutionEngine.hasPrimedRadiantHolySmiteStrike(meleeAttacker);
            boolean umbralCloakingConversion = SpellExecutionEngine.hasPrimedUmbralCloakingStrike(meleeAttacker);
            boolean vampiricConversion = SpellExecutionEngine.hasPrimedUndeadVampiricStrike(meleeAttacker);
            boolean spatialDestabilizeConversion = SpellExecutionEngine.hasPrimedVoidDestabilizationStrike(meleeAttacker);
            boolean aridMummifyConversion = SpellExecutionEngine.hasPrimedAridMummificationStrike(meleeAttacker);
            boolean lightningOverchargeConversion = SpellExecutionEngine.hasPrimedLightningOverchargeStrike(meleeAttacker);
            boolean magmaVolcanicConversion = SpellExecutionEngine.hasPrimedMagmaVolcanicCleaveStrike(meleeAttacker);
            boolean stormSquallConversion = SpellExecutionEngine.hasPrimedStormSquallCleaveStrike(meleeAttacker);
            boolean glacialBrittleConversion = SpellExecutionEngine.hasPrimedGlacialBrittleFrostStrike(meleeAttacker);
            boolean glacialPermafrostCreep = glacialBrittleConversion && SpellExecutionEngine.hasPrimedGlacialPermafrostCreep(meleeAttacker);
            boolean overgrowthBotanicalConversion = SpellExecutionEngine.hasPrimedOvergrowthBotanicalSlamStrike(meleeAttacker);
            boolean dustSmotherConversion = SpellExecutionEngine.hasPrimedDustSmotherStrike(meleeAttacker);
            boolean sporeInfestationConversion = SpellExecutionEngine.hasPrimedSporeInfestationStrike(meleeAttacker);
            boolean taigaHeatSiphonConversion = SpellExecutionEngine.hasPrimedTaigaHeatSiphonStrike(meleeAttacker);
            boolean oasisBlessingConversion = SpellExecutionEngine.hasPrimedOasisBlessingStrike(meleeAttacker);
            boolean vitaeOverchargeConversion = SpellExecutionEngine.hasPrimedVitaeOverchargeStrike(meleeAttacker);
            boolean bloodExsanguinationConversion = SpellExecutionEngine.hasPrimedBloodExsanguinationStrike(meleeAttacker);
            boolean auraChakraPalmConversion = SpellExecutionEngine.hasPrimedAuraChakraPalmStrike(meleeAttacker);
            boolean amberFossilizingConversion = SpellExecutionEngine.hasPrimedAmberFossilizingStrike(meleeAttacker);
            boolean eclipseSiphonPalmConversion = SpellExecutionEngine.hasPrimedEclipseSiphonPalmStrike(meleeAttacker);
            boolean blightThornGougeConversion = SpellExecutionEngine.hasPrimedBlightThornGougeStrike(meleeAttacker);
            boolean astralOverchargeConversion = SpellExecutionEngine.hasPrimedAstralOverchargeStrike(meleeAttacker);
            boolean soulfireCleaveConversion = SpellExecutionEngine.hasPrimedSoulfireCleaveStrike(meleeAttacker);
            boolean vaporHydroGougeConversion = SpellExecutionEngine.hasPrimedVaporHydroGougeStrike(meleeAttacker);
            boolean nullREventHorizonConversion = SpellExecutionEngine.hasPrimedNullREventHorizonStrike(meleeAttacker);
            boolean nullUOblivionPalmConversion = SpellExecutionEngine.hasPrimedNullUOblivionPalmStrike(meleeAttacker);
            boolean dawnAeroSolarCleaveConversion = SpellExecutionEngine.hasPrimedDawnAeroSolarCleaveStrike(meleeAttacker);
            boolean abyssDrowningCleaveConversion = SpellExecutionEngine.hasPrimedAbyssDrowningCleaveStrike(meleeAttacker);
            boolean aegisShieldBashConversion = SpellExecutionEngine.hasPrimedAegisShieldBashStrike(meleeAttacker);
            boolean auroraPolarCleaveConversion = SpellExecutionEngine.hasPrimedAuroraPolarCleaveStrike(meleeAttacker);
            boolean mirageParanoiaConversion = SpellExecutionEngine.hasPrimedMirageParanoiaStrike(meleeAttacker);
            boolean wraithPhasingPalmConversion = SpellExecutionEngine.hasPrimedWraithPhasingPalmStrike(meleeAttacker);
            boolean barrowTombstoneSlamConversion = SpellExecutionEngine.hasPrimedBarrowTombstoneSlamStrike(meleeAttacker);
            boolean pyreTimberCleaveConversion = SpellExecutionEngine.hasPrimedPyreTimberCleaveStrike(meleeAttacker);
            boolean penumbraEclipseCleaveConversion = SpellExecutionEngine.hasPrimedPenumbraEclipseCleaveStrike(meleeAttacker);
            boolean rimePermafrostCleaveConversion = SpellExecutionEngine.hasPrimedRimePermafrostCleaveStrike(meleeAttacker);
            boolean springBlossomCleaveConversion = SpellExecutionEngine.hasPrimedSpringBlossomCleaveStrike(meleeAttacker);
            boolean staticPalmOverloadConversion = SpellExecutionEngine.hasPrimedStaticPalmOverloadStrike(meleeAttacker);
            boolean sylvanCanopyCleaveConversion = SpellExecutionEngine.hasPrimedSylvanCanopyCleaveStrike(meleeAttacker);
            boolean miasmaPalmGougeConversion = SpellExecutionEngine.hasPrimedMiasmaPalmGougeStrike(meleeAttacker);
            boolean genesisInceptionPalmConversion = SpellExecutionEngine.hasPrimedGenesisInceptionPalmStrike(meleeAttacker);
            boolean oblivionExtinctionPalmConversion = SpellExecutionEngine.hasPrimedOblivionExtinctionPalmStrike(meleeAttacker);
            boolean aetherFirmamentSunderConversion = SpellExecutionEngine.hasPrimedAetherFirmamentSunderStrike(meleeAttacker);
            boolean entropicSingularityCleaveConversion = SpellExecutionEngine.hasPrimedEntropicSingularityCleaveStrike(meleeAttacker);
            boolean celestialSupernovaCleaveConversion = SpellExecutionEngine.hasPrimedCelestialSupernovaCleaveStrike(meleeAttacker);
            boolean prismaticPalmShatterConversion = SpellExecutionEngine.hasPrimedPrismaticPalmShatterStrike(meleeAttacker);
            boolean empyreanRootCleaveConversion = SpellExecutionEngine.hasPrimedEmpyreanRootCleaveStrike(meleeAttacker);
            boolean cataclysmCalamitySunderConversion = SpellExecutionEngine.hasPrimedCataclysmCalamitySunderStrike(meleeAttacker);
            boolean terminusGlacierCleaveConversion = SpellExecutionEngine.hasPrimedTerminusGlacierCleaveStrike(meleeAttacker);
            boolean eschatonRealityScytheConversion = SpellExecutionEngine.hasPrimedEschatonRealityScytheStrike(meleeAttacker);
            boolean apotheosisGenesisCleaveConversion = SpellExecutionEngine.hasPrimedApotheosisGenesisCleaveStrike(meleeAttacker);
            boolean entropicaGrandParadoxConversion = SpellExecutionEngine.hasPrimedEntropicaGrandParadoxStrike(meleeAttacker);
            if (vaporHydroGougeConversion) {
                EntityHelper.getPersistentData(entity).putLong(
                        SpellLingeringZoneManager.TAG_VAPOR_BOIL_DEATH_UNTIL, level.getGameTime() + 100L);
            }
            if (soulfireCleaveConversion) {
                EntityHelper.getPersistentData(entity).putLong(
                        SpellLingeringZoneManager.TAG_SOULFIRE_CLEAVE_BURST_UNTIL, level.getGameTime() + 100L);
            }
            if (blightThornGougeConversion) {
                EntityHelper.getPersistentData(entity).putLong(
                        SpellLingeringZoneManager.TAG_BLIGHT_CORPSE_SNARE_UNTIL, level.getGameTime() + 100L);
            }
            if (aridMummifyConversion) {
                EntityHelper.getPersistentData(entity).putLong(
                        SpellLingeringZoneManager.TAG_ARID_MUMMIFY_DROPS_UNTIL, level.getGameTime() + 60L);
            }
            if (magmaVolcanicConversion) {
                EntityHelper.getPersistentData(entity).putLong(
                        SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL, level.getGameTime() + 100L);
            }
            if (sporeInfestationConversion) {
                CompoundTag spData = EntityHelper.getPersistentData(entity);
                spData.putLong(SpellLingeringZoneManager.TAG_SPORE_INFESTED_UNTIL, level.getGameTime() + 120L);
                spData.putString("SpellSporeInfestedCaster", meleeAttacker.getUUID().toString());
            }
            if (taigaHeatSiphonConversion) {
                applyTimedMovementSpeedBoost(meleeAttacker, level, 0.30D, 200L);
                EntityHelper.getPersistentData(meleeAttacker)
                        .putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, level.getGameTime() + 200L);
            }
            if (bloodExsanguinationConversion) {
                CompoundTag bData = EntityHelper.getPersistentData(entity);
                bData.putLong(SpellLingeringZoneManager.TAG_BLOOD_FEAST_UNTIL, level.getGameTime() + 60L);
                bData.putString("SpellBloodFeastCaster", meleeAttacker.getUUID().toString());
                bData.putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, level.getGameTime() + 120L);
                bData.putInt("SpellBloodHemorrhageAmp", 1);
            }
            if (amberFossilizingConversion) {
                EntityHelper.getPersistentData(entity).putLong(
                        SpellLingeringZoneManager.TAG_AMBER_FRAGILE_STATUE_UNTIL, level.getGameTime() + 80L);
            }
            int primedSpellOrWeaponTier = resolveSpellOrWeaponTier(meleeAttacker, 2);
            REENTRANT_GUST_HURT.set(true);
            try {
                SpellExecutionEngine.tryDischargePrimedTouchStrike(level, meleeAttacker, entity);
                // Section 6.5 Seismic Impact: converts the physical melee strike into pure Earth Materia damage (+1 vs Void)!
                if (seismicConversion && !SpellExecutionEngine.hasPrimedEarthSeismicStrike(meleeAttacker) && entity.isAlive()) {
                    hurtWithEarthMateriaAdvantage(level, entity, meleeAttacker, amount, false, true);
                    return EventResult.interruptFalse();
                }
                // Section 6.6 Flash-Freeze Stasis: converts the physical melee strike into pure Frozen Materia damage (+1 vs Water, -1 vs Nether/Undead)!
                if (flashFreezeConversion && !SpellExecutionEngine.hasPrimedFrozenFlashFreezeStrike(meleeAttacker) && entity.isAlive()) {
                    hurtWithFrozenMateriaAdvantage(level, entity, meleeAttacker, amount, flashFreezePermafrost, true);
                    return EventResult.interruptFalse();
                }
                // Section 6.7 Cauterizing Cleave & Auto-Smelt: converts the physical melee strike into pure Nether Materia damage (+1 vs Frozen, -1 vs Water)!
                if (cauterizingConversion && !SpellExecutionEngine.hasPrimedNetherCauterizingStrike(meleeAttacker) && entity.isAlive()) {
                    CompoundTag entTag = EntityHelper.getPersistentData(entity);
                    entTag.putLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL, level.getGameTime() + 2L);
                    hurtWithNetherMateriaAdvantage(level, entity, meleeAttacker, amount, true, true);
                    if (entity.isAlive() && !entity.isDeadOrDying()) {
                        entTag.remove(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL);
                    }
                    return EventResult.interruptFalse();
                }
                // Section 6.8 Holy Smite & Daylight Ignition: converts the physical melee strike into pure Radiant Materia damage (+1 vs Undead, -1 vs Umbral)!
                if (holySmiteConversion && !SpellExecutionEngine.hasPrimedRadiantHolySmiteStrike(meleeAttacker) && entity.isAlive()) {
                    CompoundTag entTag = EntityHelper.getPersistentData(entity);
                    entTag.putLong(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL, level.getGameTime() + 2L);
                    hurtWithRadiantMateriaAdvantage(level, entity, meleeAttacker, amount, true, true);
                    if (entity.isAlive() && !entity.isDeadOrDying()) {
                        entTag.remove(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL);
                    }
                    return EventResult.interruptFalse();
                }
                // Section 6.9 Umbral Shroud Cloaking: converts the physical melee strike into pure Umbral Materia damage (+1 vs Radiant, -1 vs Void)!
                if (umbralCloakingConversion && !SpellExecutionEngine.hasPrimedUmbralCloakingStrike(meleeAttacker) && entity.isAlive()) {
                    hurtWithUmbralMateriaAdvantage(level, entity, meleeAttacker, amount, true, true);
                    if (entity.isAlive() && entity instanceof Mob struckMob) {
                        struckMob.setTarget(null);
                        struckMob.setLastHurtByMob(null);
                        struckMob.setAggressive(false);
                        struckMob.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
                        struckMob.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT);
                        EntityHelper.getPersistentData(struckMob)
                                .putLong(SpellLingeringZoneManager.TAG_UMBRAL_OBFUSCATED_UNTIL, level.getGameTime() + 50L);
                    }
                    return EventResult.interruptFalse();
                }
                // Section 6.10 Vampiric Touch: converts the physical melee strike into pure Undead Materia damage (+1 vs Frozen, -1 vs Radiant)
                // and siphons 30% of the damage dealt directly into the caster's health pool!
                if (vampiricConversion && !SpellExecutionEngine.hasPrimedUndeadVampiricStrike(meleeAttacker) && entity.isAlive()) {
                    float dealtSwing = hurtWithUndeadMateriaAdvantage(level, entity, meleeAttacker, amount, false, true, false);
                    if (dealtSwing > 0.0F && meleeAttacker.isAlive()) {
                        float vampHeal = dealtSwing * 0.30F;
                        notifyMagicalHeal(meleeAttacker, vampHeal);
                        meleeAttacker.heal(vampHeal);
                    }
                    return EventResult.interruptFalse();
                }
                // Section 6.11 Spatial Destabilization Strike: converts the physical melee strike into armor-piercing Void Materia damage (+1 vs Air, -1 vs Earth)!
                if (spatialDestabilizeConversion && !SpellExecutionEngine.hasPrimedVoidDestabilizationStrike(meleeAttacker) && entity.isAlive()) {
                    hurtWithVoidMateriaAdvantage(level, entity, meleeAttacker, amount, true, 0.35F, false);
                    return EventResult.interruptFalse();
                }
                // Section 6.12 Desiccating Mummification Strike: converts the physical melee strike into pure Arid Materia damage
                // and desiccates slain raw drops into dried cured meat and sand!
                if (aridMummifyConversion && !SpellExecutionEngine.hasPrimedAridMummificationStrike(meleeAttacker) && entity.isAlive()) {
                    hurtWithAridMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                    if (!entity.isAlive()) {
                        ddraig.net.entropica.spell.mutation.element.AridSpellMutationHandler.desiccateFreshNearbyLoot(level, entity);
                    }
                    return EventResult.interruptFalse();
                }
                // Section 6.13 Overcharge Discharge Strike: converts the physical melee strike into pure Lightning Materia damage!
                if (lightningOverchargeConversion && !SpellExecutionEngine.hasPrimedLightningOverchargeStrike(meleeAttacker) && entity.isAlive()) {
                    hurtWithLightningMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                    return EventResult.interruptFalse();
                }
                // Section 6.14 Volcanic Slag Cleave: converts the physical melee strike into pure Magma Materia damage
                // (+1 vs Frozen & Nature, -1 vs Air & Water) and auto-smelts drops if slain!
                if (magmaVolcanicConversion && !SpellExecutionEngine.hasPrimedMagmaVolcanicCleaveStrike(meleeAttacker) && entity.isAlive()) {
                    hurtWithMagmaMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                    if (!entity.isAlive()) {
                        ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler
                                .autoSmeltDroppedItems(level, entity.getBoundingBox().inflate(2.5D));
                    }
                    return EventResult.interruptFalse();
                }
                // Section 6.15 Squall Cleave: converts the physical melee strike into pure Storm Materia damage (+1 vs Earth & Glacial, -1 vs Nature)!
                if (stormSquallConversion && !SpellExecutionEngine.hasPrimedStormSquallCleaveStrike(meleeAttacker) && entity.isAlive()) {
                    hurtWithStormMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                    return EventResult.interruptFalse();
                }
                // Section 6.16 Brittle Frost Strike: converts the physical melee strike into pure Glacial Materia damage!
                if (glacialBrittleConversion && !SpellExecutionEngine.hasPrimedGlacialBrittleFrostStrike(meleeAttacker) && entity.isAlive()) {
                    hurtWithGlacialMateriaAdvantage(level, entity, meleeAttacker, amount, glacialPermafrostCreep, true, false);
                    return EventResult.interruptFalse();
                }
                // Section 6.17 Botanical Ground Slam: converts the physical melee strike into pure Overgrowth Materia damage!
                if (overgrowthBotanicalConversion && !SpellExecutionEngine.hasPrimedOvergrowthBotanicalSlamStrike(meleeAttacker) && entity.isAlive()) {
                    hurtWithOvergrowthMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                    return EventResult.interruptFalse();
                }
                // Section 6.18 Choking Smother Strike: converts the physical melee strike into pure Dust Materia damage!
                if (dustSmotherConversion && !SpellExecutionEngine.hasPrimedDustSmotherStrike(meleeAttacker) && entity.isAlive()) {
                    hurtWithDustMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                    return EventResult.interruptFalse();
                }
                // Section 6.19 Mycelial Infestation Strike: converts the physical melee strike into pure Spore Materia damage!
                if (sporeInfestationConversion && !SpellExecutionEngine.hasPrimedSporeInfestationStrike(meleeAttacker) && entity.isAlive()) {
                    hurtWithSporeMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                    return EventResult.interruptFalse();
                }
                // Section 6.20 Heat-Siphon Conifer Touch: converts the physical melee strike into pure Taiga Materia damage!
                if (taigaHeatSiphonConversion && !SpellExecutionEngine.hasPrimedTaigaHeatSiphonStrike(meleeAttacker) && entity.isAlive()) {
                    hurtWithTaigaMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                    return EventResult.interruptFalse();
                }
                // Section 6.21 Desert Blessing Touch: heals & cleanses allies with zero weapon hurt, or converts hostile strike into Oasis Materia damage!
                if (oasisBlessingConversion && !SpellExecutionEngine.hasPrimedOasisBlessingStrike(meleeAttacker)) {
                    if (SpellExecutionEngine.isFriendlyAllyForOasis(meleeAttacker, entity)) {
                        return EventResult.interruptFalse();
                    }
                    if (entity.isAlive()) {
                        hurtWithOasisMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.22 Cellular Overcharge Strike: implants volatile vital force & converts melee strike into pure Vitae Materia damage!
                if (vitaeOverchargeConversion && !SpellExecutionEngine.hasPrimedVitaeOverchargeStrike(meleeAttacker)) {
                    CompoundTag vData = EntityHelper.getPersistentData(entity);
                    vData.putLong(SpellLingeringZoneManager.TAG_VITAE_OVERCHARGE_UNTIL, level.getGameTime() + 100L);
                    vData.putString("SpellVitaeOverchargeCaster", meleeAttacker.getUUID().toString());
                    if (entity.isAlive()) {
                        hurtWithVitaeMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.23 Vampiric Exsanguination Strike: converts melee strike into pure Blood Materia damage + 40% instant lifesteal!
                if (bloodExsanguinationConversion && !SpellExecutionEngine.hasPrimedBloodExsanguinationStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        float dealt = hurtWithBloodMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        if (dealt > 0.0F && meleeAttacker.isAlive()) {
                            float leech = dealt * 0.40F;
                            notifyMagicalHeal(meleeAttacker, leech);
                            meleeAttacker.heal(leech);
                        }
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.24 Chakra Disruptor Palm: 4s ability/spell silence + pure Aura Materia damage + 30% Materia fuel siphon!
                if (auraChakraPalmConversion && !SpellExecutionEngine.hasPrimedAuraChakraPalmStrike(meleeAttacker)) {
                    SpellLingeringZoneManager.applyArcaneSilence(entity, 80);
                    if (entity.isAlive()) {
                        float dealt = hurtWithAuraMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        if (dealt > 0.0F && meleeAttacker instanceof Player palmPlayer && palmPlayer.isAlive()) {
                            siphonCasterMateriaFuelFromDamage(
                                    palmPlayer, EssenceType.AURA, resolveSpellOrWeaponTier(palmPlayer, primedSpellOrWeaponTier), dealt, 0.30F
                            );
                        }
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.25 Fossilizing Stasis Strike: 60% Slowness & -40% attack speed for 4s + pure Amber Materia damage!
                if (amberFossilizingConversion && !SpellExecutionEngine.hasPrimedAmberFossilizingStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        applyTimedMovementSpeedSap(entity, level, 0.60D, 80L);
                        applyTimedAttackDebuff(entity, level, 0.0D, 0.40D, 80L);
                        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                        entity.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 80, 1));
                        CompoundTag eTag = EntityHelper.getPersistentData(entity);
                        eTag.putLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_UNTIL, level.getGameTime() + 80L);
                        eTag.putLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED, level.getGameTime() + 34L);
                        hurtWithAmberMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.26 Umbral Siphon Palm: -40% melee attack damage for 6s (120t) + buff siphon + pure Eclipse Materia damage!
                if (eclipseSiphonPalmConversion && !SpellExecutionEngine.hasPrimedEclipseSiphonPalmStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        applyTimedAttackDebuff(entity, level, 0.40D, 0.0D, 120L);
                        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                        List<MobEffectInstance> stolen = new ArrayList<>();
                        for (MobEffectInstance inst : entity.getActiveEffects()) {
                            if (inst.getEffect().value().isBeneficial()) {
                                stolen.add(new MobEffectInstance(inst));
                            }
                        }
                        for (MobEffectInstance inst : stolen) {
                            entity.removeEffect(inst.getEffect());
                            meleeAttacker.addEffect(new MobEffectInstance(inst.getEffect(), Math.min(240, inst.getDuration()), inst.getAmplifier()));
                        }
                        hurtWithEclipseMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.27 Necrotic Thorn Gouge: Poison II & 40% Slowness for 4s (80t), -2 armor shred & pure Blight Materia damage!
                if (blightThornGougeConversion && !SpellExecutionEngine.hasPrimedBlightThornGougeStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        applyTimedMovementSpeedSap(entity, level, 0.40D, 80L);
                        applyTimedAttributeShred(entity, level, 2.0D, 0.0D, 100L);
                        entity.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                        hurtWithBlightMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.28 Cosmic Overcharge Strike: 1.0s stun, 4-block zero-g launch (3s), 2 spark arcs (3m), +40% caster attack speed (10s) & Astral Materia damage!
                if (astralOverchargeConversion && !SpellExecutionEngine.hasPrimedAstralOverchargeStrike(meleeAttacker)) {
                    applyTimedAttackSpeedBuff(meleeAttacker, level, 0.40D, 200L);
                    if (entity.isAlive()) {
                        entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
                        entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0));
                        Vec3 swingDir = meleeAttacker.getLookAngle();
                        entity.setDeltaMovement(swingDir.x * 0.35D, 0.80D, swingDir.z * 0.35D);
                        entity.hasImpulse = true;
                        hurtWithAstralMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.29 Phantasmal Soul Cleave: 5s underwater soulflame, 25% damage-scaled heal siphon, 3m death soulburst & Soulfire Materia damage!
                if (soulfireCleaveConversion && !SpellExecutionEngine.hasPrimedSoulfireCleaveStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        entity.igniteForSeconds(5);
                        EntityHelper.getPersistentData(entity).putLong(
                                SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, level.getGameTime() + 100L);
                        float dealt = hurtWithSoulfireMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        if (dealt > 0.0F && meleeAttacker.isAlive()) {
                            float heal = dealt * 0.25F;
                            notifyMagicalHeal(meleeAttacker, heal);
                            meleeAttacker.heal(heal);
                        }
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.30 Superheated Hydro-Gouge: bypasses 40% target armor, extinguishes fires, death steam cloud & Vapor Materia damage!
                if (vaporHydroGougeConversion && !SpellExecutionEngine.hasPrimedVaporHydroGougeStrike(meleeAttacker)) {
                    meleeAttacker.clearFire();
                    if (entity.isAlive()) {
                        entity.clearFire();
                        SpellLingeringZoneManager.applySoaked(entity, 140);
                        applyPercentageArmorShred(entity, level, 0.40D, 0.0D, 40L);
                        hurtWithVaporMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.31 Event Horizon Palm: ignores 50% armor, purges positive buffs, 3 random warps over 6s, 2s Blindness & Null_R Materia damage!
                if (nullREventHorizonConversion && !SpellExecutionEngine.hasPrimedNullREventHorizonStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        ddraig.net.entropica.spell.mutation.element.EclipseSpellMutationHandler.purgeBeneficialEffects(entity);
                        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                        EntityHelper.getPersistentData(entity).putLong(
                                SpellLingeringZoneManager.TAG_VOID_DESTABILIZED_UNTIL, level.getGameTime() + 121L);
                        applyPercentageArmorShred(entity, level, 0.50D, 0.0D, 40L);
                        hurtWithNullRMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.32 Spacetime Oblivion Palm: bypasses 50% armor, 2.5s shadow root, 3 random warps over 6s, Blindness, 25% lifesteal & Null_U Materia damage!
                if (nullUOblivionPalmConversion && !SpellExecutionEngine.hasPrimedNullUOblivionPalmStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        ddraig.net.entropica.spell.mutation.element.UmbralSpellMutationHandler.pinEntityShadowToFloor(level, entity, 50, 2);
                        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
                        EntityHelper.getPersistentData(entity).putLong(
                                SpellLingeringZoneManager.TAG_VOID_DESTABILIZED_UNTIL, level.getGameTime() + 121L);
                        applyPercentageArmorShred(entity, level, 0.50D, 0.0D, 40L);
                        float dealt = hurtWithNullUMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        if (dealt > 0.0F && meleeAttacker.isAlive()) {
                            float heal = dealt * 0.25F;
                            notifyMagicalHeal(meleeAttacker, heal);
                            meleeAttacker.heal(heal);
                        }
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.33 Aero-Solar Cleave: 3m frontal sweep, arrow deflection during swing, 4m knockback, +30% sprint & attack speed (5s) & Dawn Materia damage!
                if (dawnAeroSolarCleaveConversion && !SpellExecutionEngine.hasPrimedDawnAeroSolarCleaveStrike(meleeAttacker)) {
                    Vec3 look = meleeAttacker.getLookAngle().normalize();
                    applyTimedMovementSpeedBoost(meleeAttacker, level, 0.30D, 100L);
                    applyTimedAttackSpeedBuff(meleeAttacker, level, 0.30D, 100L);
                    AABB sweepBox = meleeAttacker.getBoundingBox().expandTowards(look.scale(3.0D)).inflate(1.5D, 0.8D, 1.5D);
                    for (Projectile proj : level.getEntitiesOfClass(Projectile.class, sweepBox.inflate(0.8D))) {
                        if (proj.getOwner() == meleeAttacker) continue;
                        if (proj.getOwner() instanceof LivingEntity pOwner && SpellExecutionEngine.isFriendlyAllyForOasis(meleeAttacker, pOwner)) continue;
                        proj.setDeltaMovement(look.scale(Math.max(1.25D, proj.getDeltaMovement().length())));
                        proj.setOwner(meleeAttacker);
                        proj.hasImpulse = true;
                    }
                    for (LivingEntity extra : level.getEntitiesOfClass(LivingEntity.class, sweepBox, LivingEntity::isAlive)) {
                        if (extra == meleeAttacker || extra == entity || SpellExecutionEngine.isFriendlyAllyForOasis(meleeAttacker, extra)) continue;
                        if (isProtectedFromOffensiveSpell(extra, meleeAttacker, true)) continue;
                        extra.setDeltaMovement(look.x * 1.18D, 0.36D, look.z * 1.18D);
                        extra.hasImpulse = true;
                        if (extra.getType().is(EntityTypeTags.UNDEAD)) {
                            extra.igniteForSeconds(5);
                        }
                        hurtWithDawnMateriaAdvantage(level, extra, meleeAttacker, amount * 0.65F, true, true);
                    }
                    if (entity.isAlive()) {
                        entity.setDeltaMovement(look.x * 1.18D, 0.36D, look.z * 1.18D);
                        entity.hasImpulse = true;
                        if (entity.getType().is(EntityTypeTags.UNDEAD)) {
                            entity.igniteForSeconds(5);
                        }
                        hurtWithDawnMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.34 Abyssal Drowning Cleave: 5m knockback, 4s land drowning, aggro drop & Abyss Materia damage!
                if (abyssDrowningCleaveConversion && !SpellExecutionEngine.hasPrimedAbyssDrowningCleaveStrike(meleeAttacker)) {
                    Vec3 look = meleeAttacker.getLookAngle().normalize();
                    if (entity.isAlive()) {
                        entity.setDeltaMovement(look.x * 1.28D, 0.34D, look.z * 1.28D);
                        entity.hasImpulse = true;
                        SpellLingeringZoneManager.applyAbyssLandDrowning(entity, 80);
                        SpellLingeringZoneManager.applySoaked(entity, 140);
                        entity.setLastHurtByMob(null);
                        if (entity instanceof Mob mob) {
                            mob.setTarget(null);
                            mob.setAggressive(false);
                        }
                        hurtWithAbyssMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.35 Gilded Shield-Bash Strike: 6m knockback, 1.5s stun, +50% undead smite, -3 armor, +4 caster Armor (5s) & Aegis Materia damage!
                if (aegisShieldBashConversion && !SpellExecutionEngine.hasPrimedAegisShieldBashStrike(meleeAttacker)) {
                    Vec3 look = meleeAttacker.getLookAngle().normalize();
                    applyTimedArmorAndToughnessBuff(meleeAttacker, level, 4.0D, 0.0D, 0.25D, 0.0D, 100L);
                    if (entity.isAlive()) {
                        entity.setDeltaMovement(look.x * 1.48D, 0.36D, look.z * 1.48D);
                        entity.hasImpulse = true;
                        entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 6));
                        applyTimedAttributeShred(entity, level, 3.0D, 0.0D, 120L);
                        float bashDmg = amount * (entity.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F);
                        if (entity.getType().is(EntityTypeTags.UNDEAD)) {
                            entity.igniteForSeconds(5);
                        }
                        hurtWithAegisMateriaAdvantage(level, entity, meleeAttacker, bashDmg, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.36 Prismatic Aurora Polar Cleave: 1.5s solid freeze, +50% undead smite, 6s Glowing, -2 armor shred & Aurora Materia damage!
                if (auroraPolarCleaveConversion && !SpellExecutionEngine.hasPrimedAuroraPolarCleaveStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 6));
                        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
                        entity.setTicksFrozen(entity.getTicksRequiredToFreeze() + 40);
                        applyTimedAttributeShred(entity, level, 2.0D, 0.0D, 120L);
                        EntityHelper.getPersistentData(entity).putLong(
                                SpellLingeringZoneManager.TAG_AURORA_BRITTLE_UNTIL, level.getGameTime() + 120L);
                        float cleaveDmg = amount * (entity.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F);
                        hurtWithAuroraMateriaAdvantage(level, entity, meleeAttacker, cleaveDmg, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.37 Phantasmal Paranoia & Traitor Strike: 3s frantic air swings, 5s friend-foe traitor inversion, caster invisibility & Mirage Materia damage!
                if (mirageParanoiaConversion && !SpellExecutionEngine.hasPrimedMirageParanoiaStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        long paranoiaUntil = level.getGameTime() + 100L;
                        entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
                        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                        SpellLingeringZoneManager.applyMirageTraitorFrenzy(entity, 100);
                        CompoundTag hData = EntityHelper.getPersistentData(entity);
                        hData.putString(SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_CASTER_UUID, meleeAttacker.getUUID().toString());
                        hData.putLong(SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_UNTIL, paranoiaUntil);
                        if (entity instanceof Mob hMob && hMob.getTarget() == meleeAttacker) {
                            hMob.setTarget(null);
                            hMob.setAggressive(false);
                        }
                        hurtWithMirageMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.38 Spectral Phasing Palm-Gouge: bypasses shields & armor toughness, steals 2 full hearts (4.0 HP), strips 3 armor & stuns 1.5s!
                if (wraithPhasingPalmConversion && !SpellExecutionEngine.hasPrimedWraithPhasingPalmStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        if (entity.isBlocking()) {
                            entity.stopUsingItem();
                        }
                        applyPercentageArmorShred(entity, level, 0.0D, 1.0D, 120L);
                        applyTimedAttributeShred(entity, level, 3.0D, 4.0D, 120L);
                        entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                        if (meleeAttacker.isAlive()) {
                            notifyMagicalHeal(meleeAttacker, 4.0F);
                            meleeAttacker.heal(4.0F);
                        }
                        hurtWithWraithMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.39 Barrow-Cairn Tombstone Slam: 6m knockback, 1.5s flat stun, Slowness IV 3s, -3 armor & +3 Armor to caster 5s!
                if (barrowTombstoneSlamConversion && !SpellExecutionEngine.hasPrimedBarrowTombstoneSlamStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        Vec3 delta = entity.position().subtract(meleeAttacker.position());
                        Vec3 push = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.48D) : new Vec3(0.0D, 0.36D, 0.0D);
                        entity.setDeltaMovement(push.x, 0.36D, push.z);
                        entity.hasImpulse = true;
                        entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
                        applyTimedAttributeShred(entity, level, 3.0D, 0.0D, 140L);
                        applyTimedArmorAndToughnessBuff(meleeAttacker, level, 3.0D, 0.0D, 0.0D, 0.0D, 100L);
                        hurtWithBarrowMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.40 Cremating Timber-Pyre Cleave: 6s wildfire, melts 3 armor points, 5m knockback & Pyre + weapon advantage!
                if (pyreTimberCleaveConversion && !SpellExecutionEngine.hasPrimedPyreTimberCleaveStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        Vec3 delta = entity.position().subtract(meleeAttacker.position());
                        Vec3 push = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.25D) : new Vec3(0.0D, 0.32D, 0.0D);
                        entity.setDeltaMovement(push.x, 0.32D, push.z);
                        entity.hasImpulse = true;
                        entity.igniteForSeconds(6);
                        EntityHelper.getPersistentData(entity).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 120L);
                        applyTimedAttributeShred(entity, level, 3.0D, 0.0D, 140L);
                        hurtWithPyreMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.41 Penumbral Eclipse Cleave: blinds primary target 3.0s, bypasses armor & stacks Penumbra + weapon advantage!
                if (penumbraEclipseCleaveConversion && !SpellExecutionEngine.hasPrimedPenumbraEclipseCleaveStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                        hurtWithPenumbraMateriaAdvantage(level, entity, meleeAttacker, amount * 1.15F, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.42 Permafrost Rime-Cleave: shatters 3 armor, flash-freezes primary target 2.5s (50t), 5m knockback & Rime + weapon advantage!
                if (rimePermafrostCleaveConversion && !SpellExecutionEngine.hasPrimedRimePermafrostCleaveStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        Vec3 delta = entity.position().subtract(meleeAttacker.position());
                        Vec3 push = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.25D) : new Vec3(0.0D, 0.32D, 0.0D);
                        entity.setDeltaMovement(push.x, 0.32D, push.z);
                        entity.hasImpulse = true;
                        applyTimedAttributeShred(entity, level, 3.0D, 0.0D, 140L);
                        entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
                        entity.setTicksFrozen(entity.getTicksRequiredToFreeze() + 80);
                        hurtWithRimeMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.43 Vernal Blossom-Cleave: Touch of Spring on allies (zero weapon hurt), or +50% Undead smite, -3 Undead armor, 5m KB & Spring + weapon advantage!
                if (springBlossomCleaveConversion && !SpellExecutionEngine.hasPrimedSpringBlossomCleaveStrike(meleeAttacker)) {
                    if (SpellExecutionEngine.isFriendlyAllyForOasis(meleeAttacker, entity)) {
                        return EventResult.interruptFalse();
                    }
                    if (entity.isAlive()) {
                        Vec3 delta = entity.position().subtract(meleeAttacker.position());
                        Vec3 push = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.25D) : new Vec3(0.0D, 0.32D, 0.0D);
                        entity.setDeltaMovement(push.x, 0.32D, push.z);
                        entity.hasImpulse = true;
                        boolean isUndead = entity.getType().is(EntityTypeTags.UNDEAD);
                        if (isUndead) {
                            applyTimedAttributeShred(entity, level, 3.0D, 0.0D, 140L);
                        }
                        hurtWithSpringMateriaAdvantage(level, entity, meleeAttacker, amount * (isUndead ? 1.50F : 1.0F), true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.44 Dielectric Palm Overload: safely discharges creeper fuses, 8m knockback, disorienting shock & Static + weapon advantage!
                if (staticPalmOverloadConversion && !SpellExecutionEngine.hasPrimedStaticPalmOverloadStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        if (entity instanceof net.minecraft.world.entity.monster.Creeper creeper) {
                            creeper.setSwellDir(-1);
                            SpellLingeringZoneManager.applyArcaneSilence(creeper, 100);
                        }
                        Vec3 delta = entity.position().subtract(meleeAttacker.position());
                        Vec3 push = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.85D) : new Vec3(0.0D, 0.40D, 0.0D);
                        entity.setDeltaMovement(push.x, 0.40D, push.z);
                        entity.hasImpulse = true;
                        entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
                        hurtWithStaticMateriaAdvantage(level, entity, meleeAttacker, amount * 1.15F, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.45 Elderwood Canopy-Cleave: +50% Undead smite & -3 armor, 6m knockback, 2.5s root & Sylvan + weapon advantage!
                if (sylvanCanopyCleaveConversion && !SpellExecutionEngine.hasPrimedSylvanCanopyCleaveStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        boolean isUndead = entity.getType().is(EntityTypeTags.UNDEAD);
                        applyTimedAttributeShred(entity, level, 3.0D, 0.0D, 140L);
                        Vec3 delta = entity.position().subtract(meleeAttacker.position());
                        Vec3 push = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.45D) : new Vec3(0.0D, 0.36D, 0.0D);
                        entity.setDeltaMovement(push.x, 0.36D, push.z);
                        entity.hasImpulse = true;
                        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                        entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                        hurtWithSylvanMateriaAdvantage(level, entity, meleeAttacker, amount * (isUndead ? 1.50F : 1.0F), true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.46 Septic Quagmire Palm-Gouge: -3 armor, Poison II 6.0s, 2.5s bog sludge root, +25% vuln & Miasma + weapon advantage!
                if (miasmaPalmGougeConversion && !SpellExecutionEngine.hasPrimedMiasmaPalmGougeStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        applyTimedAttributeShred(entity, level, 3.0D, 0.0D, 140L);
                        entity.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
                        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                        entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                        EntityHelper.getPersistentData(entity).putLong(
                                SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL, level.getGameTime() + 50L);
                        hurtWithMiasmaMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.47 Primordial Inception Palm-Strike: heals/saturates allies harmlessly, or +50% Undead/Oblivion/Wraith smite, -3 armor, 6m KB, 2.5s root, +25% vuln & Genesis + weapon advantage!
                if (genesisInceptionPalmConversion && !SpellExecutionEngine.hasPrimedGenesisInceptionPalmStrike(meleeAttacker)) {
                    if (SpellExecutionEngine.isFriendlyAllyForOasis(meleeAttacker, entity)) {
                        return EventResult.interruptFalse();
                    }
                    if (entity.isAlive()) {
                        applyTimedAttributeShred(entity, level, 3.0D, 0.0D, 140L);
                        Vec3 delta = entity.position().subtract(meleeAttacker.position());
                        Vec3 push = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.45D) : new Vec3(0.0D, 0.36D, 0.0D);
                        entity.setDeltaMovement(push.x, 0.36D, push.z);
                        entity.hasImpulse = true;
                        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                        entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                        EntityHelper.getPersistentData(entity).putLong(
                                SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL, level.getGameTime() + 50L);
                        float smiteMult = isGenesisSmiteTarget(entity) ? 1.50F : 1.0F;
                        hurtWithGenesisMateriaAdvantage(level, entity, meleeAttacker, amount * smiteMult, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.48 Stygian Extinction Palm-Cleave: shatters 4 armor, disables shields 4.0s (80t), Stygian Conflagration (6.0s) & Oblivion + weapon advantage!
                if (oblivionExtinctionPalmConversion && !SpellExecutionEngine.hasPrimedOblivionExtinctionPalmStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        applyTimedAttributeShred(entity, level, 4.0D, 0.0D, 160L);
                        if (entity.isBlocking()) {
                            entity.stopUsingItem();
                        }
                        if (entity instanceof Player p) {
                            p.getCooldowns().addCooldown(new ItemStack(Items.SHIELD), 80);
                        }
                        CompoundTag oTag = EntityHelper.getPersistentData(entity);
                        long confUntil = level.getGameTime() + 120L;
                        oTag.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, confUntil);
                        oTag.putLong(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_UNTIL, confUntil);
                        entity.igniteForSeconds(6);
                        entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 1));
                        hurtWithOblivionMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        oTag.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, entity.getHealth());
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.50 Quintessential Firmament Sunder: bypasses 50% armor, shatters 5 armor, disables shields 5.0s (100t),
                // launches target + adjacent foes within 6m 10m airborne into 4.0s zero-g float & stacks Aether + weapon advantage!
                if (aetherFirmamentSunderConversion && !SpellExecutionEngine.hasPrimedAetherFirmamentSunderStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(6.0D), LivingEntity::isAlive)) {
                            if (foe == meleeAttacker || isProtectedFromOffensiveSpell(foe, meleeAttacker, true)) continue;
                            applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 180L);
                            if (foe.isBlocking()) {
                                foe.stopUsingItem();
                            }
                            if (foe instanceof Player p) {
                                p.getCooldowns().addCooldown(new ItemStack(Items.SHIELD), 100);
                            }
                            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.3D, 1.28D, foe.getDeltaMovement().z * 0.3D);
                            foe.hasImpulse = true;
                            foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 80, 0));
                            if (foe != entity) {
                                hurtWithAetherMateriaAdvantage(level, foe, meleeAttacker, amount * 0.50F, true, true);
                            }
                        }
                        hurtWithAetherMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.51 Entropic Singularity Cleave: bypasses 50% armor, shatters 5 armor, disables shields 5.0s (100t),
                // siphons 35% of damage dealt into caster health & stacks Entropic + weapon advantage!
                if (entropicSingularityCleaveConversion && !SpellExecutionEngine.hasPrimedEntropicSingularityCleaveStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        applyTimedAttributeShred(entity, level, 5.0D, 0.0D, 180L);
                        if (entity.isBlocking()) {
                            entity.stopUsingItem();
                        }
                        if (entity instanceof Player p) {
                            p.getCooldowns().addCooldown(new ItemStack(Items.SHIELD), 100);
                        }
                        float dealt = hurtWithEntropicMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        if (dealt > 0.0F && meleeAttacker.isAlive()) {
                            float siphon = dealt * 0.35F;
                            notifyMagicalHeal(meleeAttacker, siphon);
                            meleeAttacker.heal(siphon);
                        }
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.52 Holy Supernova Cleave / White-Hole Touch: bypasses 50% armor, shatters 5 armor, disables shields 5.0s (100t),
                // leeches 35% of damage dealt as health/fuel to caster & adjacent allies, launches 6m back & inflicts Holy Blindness + Brand!
                if (celestialSupernovaCleaveConversion && !SpellExecutionEngine.hasPrimedCelestialSupernovaCleaveStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        applyTimedAttributeShred(entity, level, 5.0D, 0.0D, 180L);
                        if (entity.isBlocking()) {
                            entity.stopUsingItem();
                        }
                        if (entity instanceof Player p) {
                            p.getCooldowns().addCooldown(new ItemStack(Items.SHIELD), 100);
                        }
                        Vec3 delta = entity.position().subtract(meleeAttacker.position());
                        Vec3 push = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.48D) : new Vec3(0.0D, 0.38D, 0.0D);
                        entity.setDeltaMovement(push.x, 0.38D, push.z);
                        entity.hasImpulse = true;
                        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
                        float dealt = hurtWithCelestialMateriaAdvantage(level, entity, meleeAttacker, amount, true, false);
                        if (dealt > 0.0F && meleeAttacker.isAlive()) {
                            float siphon = Math.max(1.5F, dealt * 0.35F);
                            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, meleeAttacker.getBoundingBox().inflate(8.0D), LivingEntity::isAlive)) {
                                if (ally == meleeAttacker || SpellExecutionEngine.isFriendlyAllyForOasis(meleeAttacker, ally)) {
                                    notifyMagicalHeal(ally, siphon);
                                    ally.heal(siphon);
                                }
                            }
                            if (meleeAttacker instanceof Player celPlayer) {
                                siphonCasterMateriaFuelFromDamage(
                                        celPlayer, EssenceType.CELESTIAL, resolveSpellOrWeaponTier(celPlayer, 4), dealt, 0.35F
                                );
                            }
                        }
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.53 Prismatic Crystalline Palm-Shatter: strikes through physical armor (50% bypass), concussive blinding
                // flashbang disarming target 4.0s (80t), shattering 5 armor points, 10.0s (200t) radiant chromatic Glowing & Prismatic damage!
                if (prismaticPalmShatterConversion && !SpellExecutionEngine.hasPrimedPrismaticPalmShatterStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        applyTimedAttributeShred(entity, level, 5.0D, 0.0D, 160L);
                        ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.disableShield(entity, 80);
                        if (entity.isUsingItem()) {
                            entity.stopUsingItem();
                        }
                        applyTimedAttackDebuff(entity, level, 0.65D, 0.0D, 80L);
                        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 3)); // 4.0s disarm!
                        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0)); // 4.0s blinding flashbang!
                        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0)); // 10.0s radiant chromatic outline!
                        hurtWithPrismaticMateriaAdvantage(level, entity, meleeAttacker, amount, true, true);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.54 Siphoning World-Tree Root-Cleave: 60% armor bypass, shatters 6 armor points, disables shields 6.0s (120t),
                // roots victim 4.0s (80t), and releases seeking golden seed tendrils within 8m siphoning health into party absorption!
                if (empyreanRootCleaveConversion && !SpellExecutionEngine.hasPrimedEmpyreanRootCleaveStrike(meleeAttacker)) {
                    if (entity.isAlive()) {
                        applyTimedAttributeShred(entity, level, 6.0D, 0.0D, 180L);
                        ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.disableShield(entity, 120);
                        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
                        entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                        float dealt = hurtWithEmpyreanMateriaAdvantage(level, entity, meleeAttacker, amount, true, false, false, false);
                        float siphoned = Math.max(2.5F, dealt * 0.40F);
                        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(8.0D),
                                e -> e.isAlive() && e != entity && e != meleeAttacker
                                        && !ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.isFriendly(e, meleeAttacker)
                                        && !isProtectedFromOffensiveSpell(e, meleeAttacker, true))) {
                            SpellWorldVfxManager.broadcastChainArc(level, entity.getEyePosition(), adj.getEyePosition(), EssenceType.EMPYREAN, EssenceType.VITAE, 7);
                            adj.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 4));
                            adj.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                            float adjDmg = hurtWithEmpyreanMateriaAdvantage(level, adj, meleeAttacker, 4.5F, true, false, false, false);
                            siphoned += Math.max(1.5F, adjDmg * 0.45F);
                        }
                        SpellLingeringZoneManager.grantPartyAbsorption(level, meleeAttacker, siphoned, 32.0F, 16.0D);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.55 Tectonic Calamity Sunder: 60% armor bypass, shatters 6 armor points, disables shields 6.0s (120t),
                // and opens a 10m fissure of light-devouring void fire dragging pursuing hostiles inward & burning 2.0 hearts/sec!
                if (cataclysmCalamitySunderConversion && !SpellExecutionEngine.hasPrimedCataclysmCalamitySunderStrike(meleeAttacker)) {
                    Vec3 fissureOrigin = entity.position();
                    SpellWorldVfxManager.broadcastArcaneSeal(
                            level, fissureOrigin.add(0.0D, 0.04D, 0.0D), 10.0D, EssenceType.CATACLYSM, EssenceType.VOID, 120, 8
                    );
                    SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                            meleeAttacker, fissureOrigin, 10.0D, 120, level.getGameTime(), null, null, EssenceType.CATACLYSM, 1.0F,
                            (srv, zone, elapsed) -> {
                                if (elapsed > zone.maxDurationTicks) return false;
                                if (elapsed % 20 == 0) {
                                    LivingEntity owner = zone.resolveCaster(srv);
                                    AABB fis = new AABB(
                                            zone.center.x - 10.0D, zone.center.y - 2.0D, zone.center.z - 10.0D,
                                            zone.center.x + 10.0D, zone.center.y + 3.5D, zone.center.z + 10.0D
                                    );
                                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, fis, e -> e.isAlive() && e != owner)) {
                                        if (owner != null && ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.isFriendly(foe, owner)) continue;
                                        if (owner != null && isProtectedFromOffensiveSpell(foe, owner, true)) continue;
                                        Vec3 in = zone.center.subtract(foe.position());
                                        if (in.horizontalDistanceSqr() > 0.25D && !isBossEntity(srv, foe)) {
                                            Vec3 pull = new Vec3(in.x, 0.0D, in.z).normalize().scale(0.42D);
                                            foe.setDeltaMovement(foe.getDeltaMovement().add(pull.x, 0.05D, pull.z));
                                            foe.hasImpulse = true;
                                        }
                                        foe.igniteForSeconds(4);
                                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, srv.getGameTime() + 50L);
                                        hurtWithCataclysmMateriaAdvantage(srv, foe, owner, 4.0F, false, false, true);
                                    }
                                    srv.sendParticles(ParticleTypes.LAVA, zone.center.x, zone.center.y + 0.3D, zone.center.z, 14, 3.5D, 0.2D, 3.5D, 0.04D);
                                    srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + 0.4D, zone.center.z, 12, 3.5D, 0.25D, 3.5D, 0.04D);
                                }
                                return true;
                            }
                    ));
                    if (entity.isAlive()) {
                        applyTimedAttributeShred(entity, level, 6.0D, 0.0D, 180L);
                        ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.disableShield(entity, 120);
                        hurtWithCataclysmMateriaAdvantage(level, entity, meleeAttacker, amount, true, true, false);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.56 Absolute-Zero Glacier Cleave (#54): 60% armor bypass, -6 armor shred, 6.0s shield disable,
                // 4.0s solid blue-ice encasement on victim, & 8-block atmospheric frost-nova freezing surrounding hostiles for 2.5s!
                if (terminusGlacierCleaveConversion && !SpellExecutionEngine.hasPrimedTerminusGlacierCleaveStrike(meleeAttacker)) {
                    Vec3 cleaveCenter = entity.position();
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                            level, meleeAttacker.getEyePosition().add(meleeAttacker.getLookAngle().scale(1.4D)),
                            meleeAttacker.getLookAngle(), 5.2D, EssenceType.TERMINUS, EssenceType.FROZEN, 5
                    );
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                            level, cleaveCenter, 8.0D, EssenceType.TERMINUS, EssenceType.FROZEN, 5
                    );
                    level.playSound(null, entity.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.15F, 0.68F);
                    level.playSound(null, entity.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.55F, 1.55F);
                    for (LivingEntity sur : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(8.0D), e -> e.isAlive() && e != meleeAttacker && e != entity)) {
                        if (ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.isFriendly(sur, meleeAttacker)) continue;
                        if (isProtectedFromOffensiveSpell(sur, meleeAttacker, true)) continue;
                        applyTimedAttributeShred(sur, level, 4.0D, 0.0D, 120L);
                        ddraig.net.entropica.spell.mutation.element.FrozenSpellMutationHandler.applyIceStasis(level, meleeAttacker, sur, 50, 0, null, null);
                    }
                    if (entity.isAlive()) {
                        applyTimedAttributeShred(entity, level, 6.0D, 0.0D, 180L);
                        ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.disableShield(entity, 120);
                        hurtWithTerminusMateriaAdvantage(level, entity, meleeAttacker, amount, true, true, false);
                        if (entity.isAlive()) {
                            ddraig.net.entropica.spell.mutation.element.FrozenSpellMutationHandler.applyIceStasis(level, meleeAttacker, entity, 80, 0, null, null);
                        }
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.57 Reality-Cleaving Scythe (#55): 75% armor bypass, -8 armor shred, cleaves shields (8.0s),
                // severs all positive buffs, locks primary victim in 4.0s (80t) event-horizon stasis, & pulls adjacent enemies into the slash arc!
                if (eschatonRealityScytheConversion && !SpellExecutionEngine.hasPrimedEschatonRealityScytheStrike(meleeAttacker)) {
                    Vec3 arcCenter = entity.position();
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                            level, meleeAttacker.getEyePosition().add(meleeAttacker.getLookAngle().scale(1.5D)),
                            meleeAttacker.getLookAngle(), 5.8D, EssenceType.ESCHATON, EssenceType.VOID, 6
                    );
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                            level, arcCenter.add(0.0D, 0.8D, 0.0D), 8.0D, true, EssenceType.ESCHATON, EssenceType.VOID, 32, 6
                    );
                    level.playSound(null, entity.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.15F, 0.55F);
                    for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(8.0D), e -> e.isAlive() && e != meleeAttacker && e != entity)) {
                        if (ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.isFriendly(adj, meleeAttacker)) continue;
                        if (isProtectedFromOffensiveSpell(adj, meleeAttacker, true)) continue;
                        if (!isBossEntity(level, adj)) {
                            Vec3 in = arcCenter.subtract(adj.position());
                            if (in.lengthSqr() > 1.0E-4D) {
                                Vec3 pull = in.normalize().scale(0.85D);
                                adj.setDeltaMovement(pull.x, 0.20D, pull.z);
                                adj.hasImpulse = true;
                            }
                        }
                        applyTimedAttributeShred(adj, level, 4.0D, 0.0D, 140L);
                    }
                    if (entity.isAlive()) {
                        ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.disableShield(entity, 160);
                        ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.purgeBeneficialBuffs(entity);
                        applyTimedAttributeShred(entity, level, 8.0D, 0.0D, 240L);
                        hurtWithEschatonMateriaAdvantage(level, entity, meleeAttacker, amount, true, true, false);
                        if (entity.isAlive()) {
                            entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 80, 0));
                            entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                                    level, entity.position().add(0.0D, entity.getBbHeight() * 0.55D, 0.0D),
                                    Math.max(1.2D, entity.getBbWidth() * 1.35D), true, EssenceType.ESCHATON, EssenceType.VOID, 80, 6
                            );
                        }
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.58 Ascendant Solar Genesis Cleave (#56): 75% armor bypass, shatters 8 armor points,
                // disables shields for 8.0s (160t), smites dark entities with catastrophic holy damage, roots foes in
                // starlight briars for 4.0s (80t), and releases seeking golden crowns within 10m siphoning health into party absorption!
                if (apotheosisGenesisCleaveConversion && !SpellExecutionEngine.hasPrimedApotheosisGenesisCleaveStrike(meleeAttacker)) {
                    Vec3 cleaveCenter = entity.position();
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                            level, meleeAttacker.getEyePosition().add(meleeAttacker.getLookAngle().scale(1.5D)),
                            meleeAttacker.getLookAngle(), 5.8D, EssenceType.APOTHEOSIS, EssenceType.RADIANT, 6
                    );
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastRadialNova(
                            level, cleaveCenter, 4.0D, EssenceType.APOTHEOSIS, EssenceType.RADIANT, 6
                    );
                    level.playSound(null, entity.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.55F);
                    level.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.15F, 1.35F);
                    if (entity.isAlive()) {
                        ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.disableShield(entity, 160);
                        applyTimedAttributeShred(entity, level, 8.0D, 0.0D, 240L);
                        float dealt = hurtWithApotheosisMateriaAdvantage(level, entity, meleeAttacker, amount, true, true, true, false);
                        if (entity.isAlive()) {
                            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
                            entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
                            if (ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(entity)) {
                                entity.igniteForSeconds(5);
                            }
                        }
                        float siphoned = Math.max(3.5F, dealt * 0.45F);
                        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(10.0D),
                                e -> e.isAlive() && e != entity && e != meleeAttacker
                                        && !ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.isFriendly(e, meleeAttacker)
                                        && !isProtectedFromOffensiveSpell(e, meleeAttacker, true))) {
                            SpellWorldVfxManager.broadcastChainArc(
                                    level, entity.getEyePosition(), adj.getEyePosition(), EssenceType.APOTHEOSIS, EssenceType.RADIANT, 8
                            );
                            adj.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 5));
                            adj.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                            applyTimedAttributeShred(adj, level, 4.0D, 0.0D, 160L);
                            float adjDmg = hurtWithApotheosisMateriaAdvantage(level, adj, meleeAttacker, 5.5F, true, true, true, false);
                            siphoned += Math.max(2.0F, adjDmg * 0.50F);
                        }
                        SpellLingeringZoneManager.grantPartyAbsorption(level, meleeAttacker, siphoned, 36.0F, 16.0D);
                        return EventResult.interruptFalse();
                    }
                }
                // Section 6.59 Blade of the Grand Paradox (#57): 75% armor bypass, shatters 8 armor points,
                // disables shields for 8.0s (160t), drains 50% of damage dealt into party absorption shields,
                // and casts a mini-vortex that draws adjacent hostiles into the slash arc!
                if (entropicaGrandParadoxConversion && !SpellExecutionEngine.hasPrimedEntropicaGrandParadoxStrike(meleeAttacker)) {
                    Vec3 slashCenter = entity.position();
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastCrescentSlash(
                            level, meleeAttacker.getEyePosition().add(meleeAttacker.getLookAngle().scale(1.5D)),
                            meleeAttacker.getLookAngle(), 5.8D, EssenceType.ENTROPICA, EssenceType.ASTRAL, 9
                    );
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastVacuumSphere(
                            level, slashCenter.add(0.0D, 0.7D, 0.0D), 3.6D, true, EssenceType.ENTROPICA, EssenceType.ASTRAL, 26, 9
                    );
                    level.playSound(null, entity.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.48F);
                    level.playSound(null, entity.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 0.80F, 1.32F);
                    if (entity.isAlive()) {
                        ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.disableShield(entity, 160);
                        applyTimedAttributeShred(entity, level, 8.0D, 0.0D, 240L);
                        float dealt = hurtWithEntropicaMateriaAdvantage(level, entity, meleeAttacker, amount, true, true, false);
                        float drained = Math.max(4.0F, dealt * 0.50F);
                        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(8.0D),
                                e -> e.isAlive() && e != entity && e != meleeAttacker
                                        && !ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.isFriendly(e, meleeAttacker)
                                        && !isProtectedFromOffensiveSpell(e, meleeAttacker, true))) {
                            Vec3 in = slashCenter.subtract(adj.position());
                            if (in.lengthSqr() > 1.0E-4D) {
                                Vec3 pull = in.normalize().scale(0.68D);
                                adj.setDeltaMovement(adj.getDeltaMovement().scale(0.30D).add(pull.x, 0.18D, pull.z));
                                adj.hasImpulse = true;
                            }
                            applyTimedAttributeShred(adj, level, 8.0D, 0.0D, 200L);
                            float adjDealt = hurtWithEntropicaMateriaAdvantage(level, adj, meleeAttacker, 5.5F, true, true, false);
                            drained += Math.max(2.0F, adjDealt * 0.50F);
                        }
                        SpellLingeringZoneManager.grantPartyAbsorption(level, meleeAttacker, drained, 36.0F, 20.0D);
                        return EventResult.interruptFalse();
                    }
                }
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
        }

        // Earth Defensive Attunement (#4): hexagonal bastion hit-flare + non-spell Void resistance (+1 -> 0.50x) & Air vulnerability (-1 -> 1.50x)!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)
                    && !source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)) {
                Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                float earthWardMult = 1.0F;
                if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.VOID)) {
                        earthWardMult = 0.50F;
                    } else if (atkAffs.contains(EssenceType.AIR)) {
                        earthWardMult = 1.50F;
                    }
                }
                if (earthWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * earthWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Nether Immolation Shroud (#6): registers defense as Nether (+1 vs Frozen -> 0.50x, -1 vs Water -> 1.50x),
        // grants 100% fire/lava immunity & sets adjacent melee attackers ablaze upon hit!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FIRE)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)) {
                entity.clearFire();
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                attacker.igniteForSeconds(5);
                EntityHelper.getPersistentData(attacker).putLong(
                        SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL,
                        Math.max(now + 100L, EntityHelper.getPersistentData(attacker)
                                .getLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL).orElse(0L))
                );
                level.sendParticles(ParticleTypes.FLAME, attacker.getX(), attacker.getY(0.7D), attacker.getZ(),
                        14, 0.25D, 0.30D, 0.25D, 0.04D);
                level.sendParticles(ParticleTypes.LAVA, attacker.getX(), attacker.getY(0.65D), attacker.getZ(),
                        5, 0.20D, 0.25D, 0.20D, 0.03D);
            }
            if (!IN_ADVANTAGE_HURT.get() && !REENTRANT_ELEMENTAL_REACTION.get()) {
                Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                float netherWardMult = 1.0F;
                if (source.is(DamageTypeTags.IS_FREEZING)) {
                    netherWardMult = 0.50F;
                    entity.setTicksFrozen(0);
                } else if (source.is(DamageTypeTags.IS_DROWNING)) {
                    netherWardMult = 1.50F;
                } else if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.FROZEN) && !atkAffs.contains(EssenceType.WATER)) {
                        netherWardMult = 0.50F;
                        entity.setTicksFrozen(0);
                    } else if (atkAffs.contains(EssenceType.WATER) && !atkAffs.contains(EssenceType.FROZEN)) {
                        netherWardMult = 1.50F;
                    }
                }
                if (netherWardMult == 1.0F
                        && source.is(DamageTypeTags.WITCH_RESISTANT_TO)
                        && data.getLong(SpellLingeringZoneManager.TAG_SOAKED_APPLIED_TICK).orElse(-10L) == now) {
                    netherWardMult = 1.50F;
                }
                if (netherWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * netherWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Radiant Pacification Shroud (#7): registers defense as Radiant (+1 vs Undead -> 0.50x, -1 vs Umbral -> 1.50x),
        // pacifies unprovoked neutral mobs, and prevents Endermen from attacking when looked at in the eyes!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_RADIANT_SHROUD_UNTIL).orElse(0L)) {
            Entity atkRaw = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            if (atkRaw instanceof Mob atkMob && atkMob != entity && atkMob.getLastHurtByMob() != entity) {
                if (atkMob instanceof EnderMan ender) {
                    ender.stopBeingAngry();
                    ender.setPersistentAngerTarget(null);
                    ender.setTarget(null);
                    ender.setAggressive(false);
                    syncEntityBarrierState(level, entity, 0.85F, hitOrigin);
                    return EventResult.interruptFalse();
                } else if (atkMob instanceof NeutralMob neutral) {
                    neutral.stopBeingAngry();
                    neutral.setPersistentAngerTarget(null);
                    atkMob.setTarget(null);
                    atkMob.setAggressive(false);
                    syncEntityBarrierState(level, entity, 0.85F, hitOrigin);
                    return EventResult.interruptFalse();
                }
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (!IN_ADVANTAGE_HURT.get()
                    && !REENTRANT_ELEMENTAL_REACTION.get()
                    && !source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)
                    && !source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)) {
                float radiantWardMult = 1.0F;
                if (atkRaw instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    boolean hasUndead = atkAffs.contains(EssenceType.UNDEAD);
                    boolean hasUmbral = atkAffs.contains(EssenceType.UMBRAL);
                    if (hasUndead && !hasUmbral) {
                        radiantWardMult = 0.50F;
                    } else if (hasUmbral && !hasUndead) {
                        radiantWardMult = 1.50F;
                    } else if (!hasUndead && !hasUmbral && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                        float weapScore = resolveHeldWeaponAdvantageScore(atkLiv, entity);
                        if (weapScore < 0.0F) {
                            radiantWardMult = 0.50F;
                        } else if (weapScore > 0.0F) {
                            radiantWardMult = 1.50F;
                        }
                    }
                } else if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)
                        || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER_SKULL)) {
                    radiantWardMult = 0.50F;
                } else if (source.is(net.minecraft.world.damagesource.DamageTypes.SONIC_BOOM)) {
                    radiantWardMult = 1.50F;
                }
                if (radiantWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * radiantWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Umbral Shroud of Silence & Stealth (#8): registers defense as Umbral (+1 vs Radiant -> 0.50x, -1 vs Void -> 1.50x)
        // and blinds adjacent attackers stepping inside the 3-block obscuring mist!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            Entity atkRaw = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            if (atkRaw instanceof LivingEntity atkLiv && atkLiv != entity && atkLiv.distanceToSqr(entity) <= 9.0D) {
                atkLiv.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            }
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                float umbralWardMult = 1.0F;
                if (source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)) {
                    umbralWardMult = 1.50F;
                } else if (atkRaw instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.RADIANT)) {
                        umbralWardMult = 0.50F;
                    } else if (atkAffs.contains(EssenceType.VOID)) {
                        umbralWardMult = 1.50F;
                    }
                }
                if (umbralWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * umbralWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Undead Necrotic Shroud (#9): registers defense as Undead (+1 vs Frozen -> 0.50x, -1 vs Radiant -> 1.50x)
        // and causes unprovoked vanilla Undead mobs (zombies, skeletons, phantoms, drowned, etc.) to treat the caster as friendly!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL).orElse(0L)) {
            Entity atkRaw = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            if (atkRaw instanceof Mob atkMob
                    && atkMob != entity
                    && atkMob.getType().is(EntityTypeTags.UNDEAD)
                    && atkMob.getLastHurtByMob() != entity) {
                if (atkMob instanceof NeutralMob neutral) {
                    neutral.stopBeingAngry();
                    neutral.setPersistentAngerTarget(null);
                }
                atkMob.setTarget(null);
                atkMob.setAggressive(false);
                syncEntityBarrierState(level, entity, 0.85F, hitOrigin);
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                float undeadWardMult = 1.0F;
                if (source.is(DamageTypeTags.IS_FREEZING)) {
                    undeadWardMult = 0.50F;
                    entity.setTicksFrozen(0);
                } else if (atkRaw instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.FROZEN)) {
                        undeadWardMult = 0.50F;
                        entity.setTicksFrozen(0);
                    } else if (atkAffs.contains(EssenceType.RADIANT)) {
                        undeadWardMult = 1.50F;
                    }
                }
                if (undeadWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * undeadWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Void Zero-G Levitation Aura (#10): registers defense as Void (+1 vs Air -> 0.50x, -1 vs Earth -> 1.50x)
        // and grants 100% weightless fall-damage immunity!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FALL)) {
                entity.resetFallDistance();
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                Entity atkRaw = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                float voidWardMult = 1.0F;
                if (atkRaw instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.AIR)) {
                        voidWardMult = 0.50F;
                    } else if (atkAffs.contains(EssenceType.EARTH)) {
                        voidWardMult = 1.50F;
                    }
                }
                if (voidWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * voidWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Arid Sandstorm Veil (#11): registers defense as Arid (Mutually Volatile Clash: -1 Weak vs Water -> 1.50x)!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_ARID_VEIL_UNTIL).orElse(0L)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                Entity atkRaw = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                if (atkRaw instanceof LivingEntity atkLiv && atkLiv != entity
                        && resolveEntityAffinities(atkLiv).contains(EssenceType.WATER)) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * 1.50F);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Lightning Static Shroud (#12): absorb lightning strikes into 100% full Materia fuel restoration, shock melee attackers (0.5s stun),
        // and apply Lightning defensive affinity (+1 vs Earth -> 0.50x, -1 vs Void & Water -> 1.50x)!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_LIGHTNING_SHROUD_UNTIL).orElse(0L)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (source.is(DamageTypeTags.IS_LIGHTNING)) {
                entity.clearFire();
                notifyMagicalHeal(entity, 6.0F);
                entity.heal(6.0F);
                if (entity instanceof Player player) {
                    int filled = SpellFuelService.restoreAllPlayerOrbisCellsToFull(player, EssenceType.LIGHTNING);
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("§e⚡ [Static Shroud] §7Lightning strike absorbed — §f"
                                    + (filled > 0 ? filled + " Orbis Cell(s) recharged to 100%" : "Full Materia surge absorbed") + "§7!"),
                            true
                    );
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getY(1.0), entity.getZ(),
                        36, 0.48D, 0.68D, 0.48D, 0.14D);
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                attacker.addEffect(new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 10, 0));
                SpellWorldVfxManager.broadcastChainArc(
                        level, entity.getBoundingBox().getCenter(), attacker.getBoundingBox().getCenter(),
                        EssenceType.LIGHTNING, null, 2
                );
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithLightningMateriaAdvantage(level, attacker, entity, 2.5F, false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
            if (!REENTRANT_GUST_HURT.get() && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                float shroudMult = 1.0F;
                if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.EARTH)) {
                        shroudMult = 0.50F;
                    } else if (atkAffs.contains(EssenceType.VOID) || atkAffs.contains(EssenceType.WATER)) {
                        shroudMult = 1.50F;
                    }
                }
                if (shroudMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.invulnerableTime = 0;
                        entity.hurtServer(level, source, amount * shroudMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Frozen Solid Ice Block Stasis & Permafrost Stasis Hex (#5): fire reactions immediately thaw/shatter; blunt/physical hits deal +50% shatter damage!
        if (source.is(DamageTypeTags.IS_FIRE) && now <= data.getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L)) {
            SpellLingeringZoneManager.clearFrozenStasisAndShatter(level, entity, data, hitOrigin);
            level.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(0.85D), entity.getZ(),
                    18, 0.4D, 0.45D, 0.4D, 0.06D);
            level.playSound(null, entity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.80F, 1.35F);
        }
        if ((hasActiveMutatedHex(data, now, EssenceType.NATURE) || hasActiveMutatedHex(data, now, EssenceType.OVERGROWTH))
                && source.is(DamageTypeTags.IS_FIRE)) {
            clearHexData(entity, data);
            data.remove("SpellSilencedUntil");
            level.sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY(0.85D), entity.getZ(),
                    16, 0.4D, 0.45D, 0.4D, 0.04D);
            level.playSound(null, entity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.65F, 1.25F);
        }
        if ((hasActiveMutatedHex(data, now, EssenceType.FROZEN)
                || hasActiveMutatedHex(data, now, EssenceType.GLACIAL)
                || hasActiveMutatedHex(data, now, EssenceType.TAIGA))
                && source.is(DamageTypeTags.IS_FIRE)) {
            clearHexData(entity, data);
            entity.setTicksFrozen(0);
            level.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(0.85D), entity.getZ(),
                    16, 0.4D, 0.45D, 0.4D, 0.05D);
            level.playSound(null, entity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.70F, 1.35F);
        } else if (!REENTRANT_GUST_HURT.get()
                && !IN_ADVANTAGE_HURT.get()
                && hasActiveMutatedHex(data, now, EssenceType.FROZEN)
                && isFrozenHexShatterDamage(source)) {
            float mult = resolveFrozenHexShatterMultiplier(getHexStackLevel(entity));
            REENTRANT_GUST_HURT.set(true);
            try {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
                        entity.getX(), entity.getY(0.8D), entity.getZ(), 14, 0.32D, 0.38D, 0.32D, 0.10D);
                level.sendParticles(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY(0.8D), entity.getZ(),
                        8, 0.3D, 0.35D, 0.3D, 0.04D);
                level.playSound(null, entity.blockPosition(), SoundEvents.GLASS_HIT, SoundSource.PLAYERS, 0.75F, 1.40F);
                entity.hurtServer(level, source, amount * mult);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Umbral Abyssal Shadow Lance (#8): drains outgoing ranged/projectile/magic attack strength by 30%
        // (melee attacks are already reduced by 30% via Attributes.ATTACK_DAMAGE)!
        if (!REENTRANT_GUST_HURT.get() && source.getEntity() instanceof LivingEntity weakenedAtk && weakenedAtk != entity) {
            CompoundTag atkTag = EntityHelper.getPersistentData(weakenedAtk);
            if (now <= atkTag.getLong(SpellLingeringZoneManager.TAG_UMBRAL_WEAKENED_30_UNTIL).orElse(0L)
                    && (source.is(DamageTypeTags.IS_PROJECTILE) || source.getDirectEntity() != weakenedAtk)) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    entity.hurtServer(level, source, amount * 0.70F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
        }

        // Umbral Sensory Void Hex (#8) on Players ("Vulnerable Darkness"): surrounding hostiles within 16m deal +25% attack damage!
        if (!REENTRANT_GUST_HURT.get()
                && entity instanceof Player
                && hasActiveMutatedHex(data, now, EssenceType.UMBRAL)) {
            Mob hexHostile = source.getEntity() instanceof Mob m && (m instanceof Enemy || m.getTarget() == entity) ? m
                    : (source.getDirectEntity() instanceof Mob dm && (dm instanceof Enemy || dm.getTarget() == entity) ? dm : null);
            if (hexHostile != null && hexHostile.distanceToSqr(entity) <= 256.0D) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    entity.hurtServer(level, source, amount * (1.0F + 0.25F * getHexStackMultiplier(entity)));
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
        }

        // Arid Desiccation Mummification Hex (#11): 2.0x fire damage & Desiccated Fragility (+30% knockback & 25% armor bypass on players)!
        if (!REENTRANT_GUST_HURT.get() && hasActiveMutatedHex(data, now, EssenceType.ARID)) {
            if (source.is(DamageTypeTags.IS_FIRE)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    entity.hurtServer(level, source, amount * 2.0F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
            if (entity instanceof Player
                    && source.getDirectEntity() instanceof LivingEntity attacker
                    && attacker != entity
                    && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                float hexMult = getHexStackMultiplier(entity);
                Vec3 delta = entity.position().subtract(attacker.position());
                Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
                if (horiz.lengthSqr() > 1.0E-4D) {
                    Vec3 kb = horiz.normalize().scale(0.38D * hexMult); // +30%+ knockback impulse!
                    entity.push(kb.x, 0.12D, kb.z);
                    entity.hasImpulse = true;
                    entity.hurtMarked = true;
                }
                int armorVal = entity.getArmorValue();
                if (armorVal > 0) {
                    // Bypass an additional 25% of player armor: deal 75% via normal armored hit + 25% as pure armor-bypassing hit!
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * 0.75F);
                        if (entity.isAlive()) {
                            entity.invulnerableTime = 0;
                            hurtWithPureMateria(level, entity, level.damageSources().indirectMagic(attacker, attacker), amount * 0.25F * hexMult);
                        }
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Frozen Glacial Spikes Retaliation (#5): registers defense as Frozen, stabs melee attackers & cuts attack speed by 30%!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (source.getDirectEntity() instanceof LivingEntity attacker
                    && attacker != entity
                    && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    int spikeTier = Math.clamp(data.getInt("SpellFrozenSpikesTier").orElse(2), 1, 10);
                    hurtWithFrozenMateriaAdvantage(level, attacker, entity, 2.5F + spikeTier * 0.65F, false, true);
                    applyTimedAttackDebuff(attacker, level, 0.0D, 0.30D, 100L); // Exact -30% Attack Speed (no compounding Mining Fatigue)!
                    CompoundTag atkData = EntityHelper.getPersistentData(attacker);
                    atkData.putLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_UNTIL, now + 100L);
                    atkData.putLong("SpellChilledLastSwingTick", now);
                    atkData.putLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED,
                            now + resolveChilledMobSwingCooldownTicks(20L));
                    Vec3 outDir = attacker.position().subtract(entity.position());
                    Vec3 nDir = outDir.lengthSqr() > 1.0E-4D ? outDir.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
                    ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                            level, attacker.position().add(0.0D, attacker.getBbHeight() * 0.55D, 0.0D),
                            nDir, EssenceType.FROZEN, null, 0.95F, spikeTier
                    );
                    level.playSound(null, attacker.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.75F, 1.45F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                float frozenWardMult = 1.0F;
                if (source.is(DamageTypeTags.IS_FIRE)) {
                    frozenWardMult = 1.50F;
                } else if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.WATER)) {
                        frozenWardMult = 0.50F;
                    } else if (atkAffs.contains(EssenceType.NETHER) || atkAffs.contains(EssenceType.UNDEAD)) {
                        frozenWardMult = 1.50F;
                    }
                }
                if (frozenWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * frozenWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Umbral Silent Shadow Chains (#8): silently echo 25% of damage taken to all other shadow-chained victims!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_UNTIL).orElse(0L)) {
            long group = data.getLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_GROUP).orElse(0L);
            if (group != 0L) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    float echo = Math.max(0.5F, amount * 0.25F);
                    LivingEntity chainCaster = null;
                    String casterStr = data.getString(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_CASTER).orElse("");
                    if (!casterStr.isEmpty()) {
                        try {
                            Entity resolved = level.getEntity(java.util.UUID.fromString(casterStr));
                            if (resolved instanceof LivingEntity liv) {
                                chainCaster = liv;
                            }
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    DamageSource echoSrc = chainCaster != null
                            ? level.damageSources().indirectMagic(chainCaster, chainCaster)
                            : level.damageSources().magic();
                    for (LivingEntity peer : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(28.0D))) {
                        if (peer == entity || !peer.isAlive()) continue;
                        CompoundTag peerData = EntityHelper.getPersistentData(peer);
                        if (now <= peerData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_UNTIL).orElse(0L)
                                && peerData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_GROUP).orElse(0L) == group) {
                            peer.invulnerableTime = 0;
                            hurtWithPureMateria(level, peer, echoSrc, echo);
                            level.sendParticles(
                                    ddraig.net.entropica.client.particle.TimedTintableParticleOption.linear(
                                            ddraig.net.entropica.registry.ModParticles.TINTABLE_MIST.get(), 0x211A21, 22, 0.95F, 0.12F),
                                    peer.getX(), peer.getY(0.60D), peer.getZ(),
                                    4, 0.22D, 0.30D, 0.22D, 0.015D
                            );
                            level.sendParticles(ParticleTypes.SMOKE, peer.getX(), peer.getY(0.65D), peer.getZ(),
                                    4, 0.20D, 0.28D, 0.20D, 0.015D);
                        }
                    }
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // Water Oceanic Shroud (#2): instant auto-extinguishing of fire/burn status & 40% physical explosion dampening!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FIRE)) {
                entity.clearFire();
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.is(DamageTypeTags.IS_EXPLOSION)) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
                level.sendParticles(ParticleTypes.SPLASH, entity.getX(), entity.getY(0.8D), entity.getZ(),
                        18, 0.45D, 0.45D, 0.45D, 0.08D);
                REENTRANT_GUST_HURT.set(true);
                try {
                    entity.hurtServer(level, source, amount * 0.60F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
        }

        // Water Waterlogged Drowning Hex (#2): fire/heat damage evaporates & cleanses the soaking curse!
        if (source.is(DamageTypeTags.IS_FIRE) && hasActiveMutatedHex(data, now, EssenceType.WATER)) {
            clearHexData(entity, data);
            level.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(0.8D), entity.getZ(),
                    14, 0.35D, 0.4D, 0.35D, 0.05D);
            level.playSound(null, entity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.65F, 1.35F);
        }

        // Air Gust Shield (#1): 50% incoming projectile damage reduction
        if (!REENTRANT_GUST_HURT.get()
                && source.is(DamageTypeTags.IS_PROJECTILE)
                && now <= data.getLong(SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL).orElse(0L)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
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

        // Magma Igneous Armor Mantle (#13): registers defense as Magma (+1 vs Frozen & Nature -> 0.50x, -1 vs Air & Water -> 1.50x),
        // grants 100% fire/lava/hot-floor immunity, flares 3D obsidian/pumice plates, & ignites melee attackers!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FIRE)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)) {
                entity.clearFire();
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                attacker.igniteForSeconds(4);
                level.sendParticles(ParticleTypes.LAVA, attacker.getX(), attacker.getY(0.7D), attacker.getZ(),
                        5, 0.20D, 0.25D, 0.20D, 0.04D);
            }
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                float magmaWardMult = 1.0F;
                if (source.is(DamageTypeTags.IS_FREEZING)) {
                    magmaWardMult = 0.50F;
                    entity.setTicksFrozen(0);
                } else if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.FROZEN) || atkAffs.contains(EssenceType.NATURE)) {
                        magmaWardMult = 0.50F;
                        entity.setTicksFrozen(0);
                    } else if (atkAffs.contains(EssenceType.AIR) || atkAffs.contains(EssenceType.WATER)) {
                        magmaWardMult = 1.50F;
                    }
                }
                if (magmaWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * magmaWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Storm Squall-Rider Shroud (#14): fall immunity, 3D storm-wing hit flare, & Storm defense (+1 vs Earth/Glacial -> 0.50x, -1 vs Nature/Void -> 1.50x)!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_STORM_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FALL)) {
                entity.resetFallDistance();
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                float stormWardMult = 1.0F;
                if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.EARTH) || atkAffs.contains(EssenceType.GLACIAL) || atkAffs.contains(EssenceType.FROZEN)) {
                        stormWardMult = 0.50F;
                    } else if (atkAffs.contains(EssenceType.NATURE) || atkAffs.contains(EssenceType.VOID)) {
                        stormWardMult = 1.50F;
                    }
                }
                if (stormWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * stormWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Water Waterlogged Drowning Hex (#2), Soaked Status, & Storm Waterlogged Hypoxia Hex (#14): 2.0x incoming Lightning damage!
        if (!REENTRANT_GUST_HURT.get()
                && source.is(DamageTypeTags.IS_LIGHTNING)
                && (hasActiveMutatedHex(data, now, EssenceType.STORM)
                    || hasActiveMutatedHex(data, now, EssenceType.WATER)
                    || SpellLingeringZoneManager.isEntityWetOrSoaked(entity))) {
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * 2.0F);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Glacial Permafrost Cryo-Shroud (#15) & Taiga Cold Immunity / Winter Solstice Shroud (#19)
        boolean glacialShroud = now <= data.getLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL).orElse(0L);
        boolean taigaShroudActive = now <= data.getLong(SpellLingeringZoneManager.TAG_TAIGA_SHROUD_UNTIL).orElse(0L);
        boolean taigaColdImmune = taigaShroudActive || now <= data.getLong(SpellLingeringZoneManager.TAG_TAIGA_COLD_IMMUNE_UNTIL).orElse(0L);
        if ((glacialShroud || taigaColdImmune) && source.is(DamageTypeTags.IS_FREEZING)) {
            entity.setTicksFrozen(0);
            if (glacialShroud || taigaShroudActive) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            }
            return EventResult.interruptFalse();
        }
        if (glacialShroud && !REENTRANT_GUST_HURT.get()) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (source.getDirectEntity() instanceof LivingEntity attacker
                    && attacker != entity
                    && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                applyTimedMovementSpeedSap(attacker, level, 0.40D, 80L); // True 40% Slowness!
                attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                REENTRANT_GUST_HURT.set(true);
                try {
                    // 0.5s (10-tick) Solid Freeze Stutter on any mob/attacker striking the caster in melee!
                    ddraig.net.entropica.spell.mutation.element.FrozenSpellMutationHandler.applyIceStasis(
                            level, entity, attacker, 10, null, null
                    );
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                float glacialWardMult = 1.0F;
                if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.ARID) || atkAffs.contains(EssenceType.FROZEN)) {
                        glacialWardMult = 0.50F;
                    } else if (atkAffs.contains(EssenceType.STORM) || atkAffs.contains(EssenceType.NATURE) || atkAffs.contains(EssenceType.UNDEAD)) {
                        glacialWardMult = 1.50F;
                    }
                }
                if (glacialWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * glacialWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Overgrowth Earth-Root Bastion (#16): 3D bastion hit-flare & Overgrowth defense (+1 vs Water/Earth/Void -> 0.50x, -1 vs Nether/Frozen/Umbral/Air -> 1.50x)!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BASTION_UNTIL).orElse(0L)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                float overgrowthWardMult = 1.0F;
                if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FREEZING)) {
                    overgrowthWardMult = 1.50F;
                } else if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.WATER) || atkAffs.contains(EssenceType.EARTH) || atkAffs.contains(EssenceType.VOID)) {
                        overgrowthWardMult = 0.50F;
                    } else if (atkAffs.contains(EssenceType.NETHER) || atkAffs.contains(EssenceType.MAGMA)
                            || atkAffs.contains(EssenceType.FROZEN) || atkAffs.contains(EssenceType.UMBRAL)
                            || atkAffs.contains(EssenceType.AIR) || atkAffs.contains(EssenceType.STORM)) {
                        overgrowthWardMult = 1.50F;
                    }
                }
                if (overgrowthWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * overgrowthWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Dust Obscuring Chalk Veil (#17): instant fire extinguishing, hostile arrow deflection, 3D chalk hit-flare,
        // & Dust defense (+1 vs Vapor & Earth -> 0.50x, -1 vs Water & Void -> 1.50x)!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_DUST_VEIL_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FIRE)) {
                entity.clearFire();
                return EventResult.interruptFalse();
            }
            if (source.is(DamageTypeTags.IS_PROJECTILE) && source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
                Vec3 away = arrow.position().subtract(entity.getBoundingBox().getCenter());
                Vec3 dir = away.lengthSqr() > 1.0E-4D ? away.normalize() : new Vec3(0.0D, 0.5D, 0.0D);
                arrow.setDeltaMovement(dir.x * 0.95D, Math.max(0.30D, dir.y * 0.65D), dir.z * 0.95D);
                arrow.hasImpulse = true;
                level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                        entity.getX(), entity.getY(0.95D), entity.getZ(), 10, 0.35D, 0.35D, 0.35D, 0.06D);
                level.playSound(null, entity.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.45F, 1.45F);
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                float dustWardMult = 1.0F;
                if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.EARTH) || atkAffs.contains(EssenceType.VAPOR)) {
                        dustWardMult = 0.50F;
                    } else if (atkAffs.contains(EssenceType.WATER) || atkAffs.contains(EssenceType.VOID)) {
                        dustWardMult = 1.50F;
                    }
                }
                if (dustWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * dustWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Spore Cloud Mantle / Orbit (#18): Poison immunity, 3D ward flare, elemental defense & melee spore-puff retaliation!
        boolean hasSporeMantle = now <= data.getLong(SpellLingeringZoneManager.TAG_SPORE_MANTLE_UNTIL).orElse(0L);
        boolean hasSporeOrbit = now <= data.getLong(SpellLingeringZoneManager.TAG_SPORE_ORBIT_UNTIL).orElse(0L);
        boolean hasSporeCarapace = now <= data.getLong(SpellLingeringZoneManager.TAG_SPORE_CARAPACE_UNTIL).orElse(0L);
        if (hasSporeMantle || hasSporeOrbit || hasSporeCarapace) {
            if (hasSporeMantle || hasSporeCarapace) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            }
            if ((hasSporeMantle || hasSporeOrbit)
                    && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity && !REENTRANT_GUST_HURT.get()) {
                attacker.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                attacker.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0x1A5923, 24, 0.95F, 0.08F),
                        attacker.getX(), attacker.getY(0.75D), attacker.getZ(), 10, 0.32D, 0.32D, 0.32D, 0.05D
                );
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xA88654, 24, 0.90F, 0.08F),
                        attacker.getX(), attacker.getY(0.85D), attacker.getZ(), 8, 0.30D, 0.30D, 0.30D, 0.04D
                );
                int wTier = Math.max(1, data.getInt("SpellWardTier").orElse(1));
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithSporeMateriaAdvantage(level, attacker, entity, 1.5F + wTier * 0.45F, false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
            if (hasSporeMantle && !REENTRANT_GUST_HURT.get() && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                float sporeWardMult = 1.0F;
                if (source.is(DamageTypeTags.IS_DROWNING)) {
                    sporeWardMult = 0.50F;
                } else if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FREEZING)) {
                    sporeWardMult = 1.50F;
                } else if (source.getEntity() instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.WATER) || atkAffs.contains(EssenceType.EARTH)) {
                        sporeWardMult = 0.50F;
                    } else if (atkAffs.contains(EssenceType.NETHER) || atkAffs.contains(EssenceType.FROZEN)
                            || atkAffs.contains(EssenceType.UMBRAL) || atkAffs.contains(EssenceType.VOID)) {
                        sporeWardMult = 1.50F;
                    }
                }
                if (sporeWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * sporeWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Taiga Winter Solstice Shroud (#19): 50% incoming arrow/projectile deflection + Taiga elemental defense!
        if (taigaShroudActive) {
            if (source.is(DamageTypeTags.IS_PROJECTILE) && level.random.nextFloat() < 0.50F) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
                level.sendParticles(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY(0.9D), entity.getZ(),
                        10, 0.35D, 0.35D, 0.35D, 0.05D);
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x1A5923, 20, 0.90F, 0.06F),
                        entity.getX(), entity.getY(0.85D), entity.getZ(), 6, 0.30D, 0.30D, 0.30D, 0.04D
                );
                level.playSound(null, entity.blockPosition(), SoundEvents.SNOW_PLACE, SoundSource.PLAYERS, 0.75F, 1.35F);
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (!REENTRANT_GUST_HURT.get() && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                float taigaWardMult = 1.0F;
                if (source.is(DamageTypeTags.IS_DROWNING)) {
                    taigaWardMult = 0.50F;
                } else if (source.is(DamageTypeTags.IS_FIRE)) {
                    taigaWardMult = 1.50F;
                } else if (source.getEntity() instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.WATER) || atkAffs.contains(EssenceType.NATURE)) {
                        taigaWardMult = 0.50F;
                    } else if (atkAffs.contains(EssenceType.NETHER) || atkAffs.contains(EssenceType.UMBRAL)
                            || atkAffs.contains(EssenceType.UNDEAD)) {
                        taigaWardMult = 1.50F;
                    }
                }
                if (taigaWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.invulnerableTime = 0;
                        entity.hurtServer(level, source, amount * taigaWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Taiga Petrifying Frost-Sap Hex / Brittle Mark (#19): +50% incoming physical/shatter damage!
        boolean hasTaigaHex = hasActiveMutatedHex(data, now, EssenceType.TAIGA);
        if (!REENTRANT_GUST_HURT.get()
                && (hasTaigaHex || now <= data.getLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL).orElse(0L))
                && !source.is(DamageTypeTags.IS_FIRE)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            float shatterMult = hasTaigaHex ? (1.0F + 0.50F * getHexStackMultiplier(entity)) : 1.50F;
            REENTRANT_GUST_HURT.set(true);
            try {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
                        entity.getX(), entity.getY(0.8D), entity.getZ(), 10, 0.3D, 0.35D, 0.3D, 0.08D);
                level.sendParticles(ParticleTypes.ITEM_SNOWBALL, entity.getX(), entity.getY(0.8D), entity.getZ(),
                        8, 0.3D, 0.35D, 0.3D, 0.06D);
                level.playSound(null, entity.blockPosition(), SoundEvents.GLASS_HIT, SoundSource.PLAYERS, 0.70F, 1.38F);
                entity.hurtServer(level, source, amount * shatterMult);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // Vitae Cellular Overcharge Mark (#21): taking damage pulses healing to nearby players/allies over 5s!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_VITAE_OVERCHARGE_UNTIL).orElse(0L)) {
            int overTier = Math.clamp(data.getInt("SpellVitaeOverchargeTier").orElse(1), 1, 10);
            float pulseHeal = 2.5F + overTier * 0.55F;
            LivingEntity overCaster = null;
            String cId = data.getString("SpellVitaeOverchargeCaster").orElse("");
            if (!cId.isEmpty()) {
                try {
                    Entity ce = level.getEntity(java.util.UUID.fromString(cId));
                    if (ce instanceof LivingEntity lv) overCaster = lv;
                } catch (IllegalArgumentException ignored) {
                }
            }
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(6.0D))) {
                if (!ally.isAlive() || ally == entity) continue;
                boolean eligible = (overCaster != null)
                        ? SpellExecutionEngine.isFriendlyAllyForOasis(overCaster, ally)
                        : !(ally instanceof Monster);
                if (!eligible) continue;
                notifyMagicalHeal(ally, pulseHeal);
                ally.heal(pulseHeal);
                level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(),
                        3, 0.25D, 0.25D, 0.25D, 0.02D);
            }
            level.sendParticles(
                    ddraig.net.entropica.client.particle.TimedTintableParticleOption.linear(
                            ddraig.net.entropica.registry.ModParticles.TINTABLE_SPARKLE.get(), 0xFF6B9D, 18, 0.90F, 0.05F),
                    entity.getX(), entity.getY(0.8D), entity.getZ(), 8, 0.45D, 0.35D, 0.45D, 0.03D
            );
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
            // Vitae (#21) Hyper-Metabolic Arrhythmia Hex: self-damaging arrhythmia recoil whenever attacking + -40% projectile/ranged attack damage!
            if (hasActiveMutatedHex(atkData, now, EssenceType.VITAE)) {
                if (now >= atkData.getLong(SpellLingeringZoneManager.TAG_VITAE_ARRHYTHMIA_NEXT_JOLT).orElse(0L)) {
                    atkData.putLong(SpellLingeringZoneManager.TAG_VITAE_ARRHYTHMIA_NEXT_JOLT, now + 14L);
                    int hexTier = Math.clamp(atkData.getInt(TAG_HEX_TIER).orElse(1), 1, 10);
                    float recoil = (2.8F + hexTier * 0.65F) * getHexStackMultiplier(attacker);
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        hurtWithVitaeMateriaAdvantage(level, attacker, null, recoil, false, true);
                        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, attacker.getX(), attacker.getY(0.8D), attacker.getZ(),
                                4, 0.22D, 0.25D, 0.22D, 0.03D);
                        level.playSound(null, attacker.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.70F, 1.35F);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                }
                if (source.is(DamageTypeTags.IS_PROJECTILE) || source.getDirectEntity() != attacker) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * Math.max(0.20F, 1.0F - 0.40F * getHexStackMultiplier(attacker)));
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
            // Blood (#22) Hemorrhage: extra bleeding laceration damage whenever moving OR attacking!
            if (now <= atkData.getLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL).orElse(0L)
                    && now >= atkData.getLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_NEXT_TICK).orElse(0L)) {
                atkData.putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_NEXT_TICK, now + 12L);
                int bTier = Math.clamp(atkData.getInt("SpellBloodHemorrhageTier").orElse(1), 1, 10);
                int bAmp = Math.clamp(atkData.getInt("SpellBloodHemorrhageAmp").orElse(0), 0, 3);
                float bleedDmg = (1.60F + bTier * 0.38F) * (1.0F + 0.35F * bAmp);
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithBloodMateriaAdvantage(level, attacker, null, bleedDmg, false, true);
                    level.sendParticles(
                            ddraig.net.entropica.client.particle.TimedTintableParticleOption.linear(
                                    ddraig.net.entropica.registry.ModParticles.TINTABLE_BLOOD.get(), 0x8A0303, 18, 0.92F, 0.05F),
                            attacker.getX(), attacker.getY(0.70D), attacker.getZ(), 5, 0.22D, 0.25D, 0.22D, 0.03D
                    );
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // Aura Chakra Respiration Shroud (#23): 100% drowning & suffocation immunity + Aura Materia ward resistance!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_AURA_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_DROWNING)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.CRAMMING)) {
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
                Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                float auraWardMult = 1.0F;
                if (source.is(DamageTypeTags.IS_FIRE)) {
                    auraWardMult = 1.50F;
                } else if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.UNDEAD) || atkAffs.contains(EssenceType.WRAITH)) {
                        auraWardMult = 0.50F;
                    } else if (atkAffs.contains(EssenceType.UMBRAL)
                            || atkAffs.contains(EssenceType.VOID)
                            || atkAffs.contains(EssenceType.NETHER)) {
                        auraWardMult = 1.50F;
                    }
                }
                if (auraWardMult != 1.0F) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * auraWardMult);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Aura Chakra Severance Hex (#23): doubles (2x) all knockback taken!
        if (hasActiveMutatedHex(data, now, EssenceType.AURA)) {
            Entity kbSource = source.getDirectEntity() != null ? source.getDirectEntity() : source.getEntity();
            if (kbSource != null && kbSource != entity && !SpellCombatReactionEngine.isBossEntity(level, entity)) {
                Vec3 away = entity.position().subtract(kbSource.position());
                Vec3 flat = new Vec3(away.x, 0.0D, away.z);
                if (flat.lengthSqr() > 1.0E-4D) {
                    Vec3 kb = flat.normalize().scale(0.62D * getHexStackMultiplier(entity));
                    SpellExecutionEngine.applySyncedMotion(
                            entity, entity.getDeltaMovement().add(kb.x, 0.24D, kb.z)
                    );
                }
            }
        }

        // Amber Petrified Bastion Mantle (#24): splash melee attackers with exact 40% Slowness for 3.0s (60t) + Amber ward defense!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_AMBER_MANTLE_UNTIL).orElse(0L)) {
            if (source.getDirectEntity() instanceof LivingEntity attacker
                    && attacker != entity
                    && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                applyTimedMovementSpeedSap(attacker, level, 0.40D, 60L);
                attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                level.sendParticles(
                        new net.minecraft.core.particles.BlockParticleOption(
                                ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.HONEY_BLOCK.defaultBlockState()),
                        attacker.getX(), attacker.getY(0.75D), attacker.getZ(), 10, 0.25D, 0.30D, 0.25D, 0.05D
                );
            }
            if (!REENTRANT_GUST_HURT.get()) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
                if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                    Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                    float amberWardMult = 1.0F;
                    if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FREEZING)) {
                        amberWardMult = 1.50F;
                    } else if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                        Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                        if (atkAffs.contains(EssenceType.VOID) || atkAffs.contains(EssenceType.BARROW)) {
                            amberWardMult = 0.50F;
                        } else if (atkAffs.contains(EssenceType.NETHER)
                                || atkAffs.contains(EssenceType.FROZEN)
                                || atkAffs.contains(EssenceType.UMBRAL)) {
                            amberWardMult = 1.50F;
                        }
                    }
                    if (amberWardMult != 1.0F) {
                        REENTRANT_GUST_HURT.set(true);
                        try {
                            entity.hurtServer(level, source, amount * amberWardMult);
                        } finally {
                            REENTRANT_GUST_HURT.set(false);
                        }
                        return EventResult.interruptFalse();
                    }
                }
            }
        }

        // Oasis Succulent Thorn Carapace (#20): 20% physical damage reduction & genuine Oasis Materia thorn retaliation!
        if (!REENTRANT_GUST_HURT.get()
                && now <= data.getLong(SpellLingeringZoneManager.TAG_OASIS_CARAPACE_UNTIL).orElse(0L)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            int cTier = Math.clamp(data.getInt("SpellOasisCarapaceTier").orElse(2), 1, 10);
            float incomingMult = 0.80F; // -20% incoming physical damage!
            if (source.is(DamageTypeTags.IS_FIRE)) {
                incomingMult *= 1.50F;
            } else if (source.getEntity() instanceof LivingEntity atkLiv && atkLiv != entity) {
                Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                if (atkAffs.contains(EssenceType.WATER) || atkAffs.contains(EssenceType.EARTH) || atkAffs.contains(EssenceType.DUST)) {
                    incomingMult *= 0.50F;
                } else if (atkAffs.contains(EssenceType.NETHER) || atkAffs.contains(EssenceType.UMBRAL)) {
                    incomingMult *= 1.50F;
                }
            }
            REENTRANT_GUST_HURT.set(true);
            try {
                if (source.getDirectEntity() instanceof LivingEntity attacker
                        && attacker != entity
                        && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                    float thornDmg = 2.2F + cTier * 0.65F;
                    hurtWithOasisMateriaAdvantage(level, attacker, entity, thornDmg, false, true);
                    level.sendParticles(
                            TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x1A5923, 22, 0.95F, 0.06F),
                            attacker.getX(), attacker.getY(0.70D), attacker.getZ(), 8, 0.28D, 0.30D, 0.28D, 0.04D
                    );
                }
                entity.invulnerableTime = 0;
                entity.hurtServer(level, source, amount * incomingMult);
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }

        // --- Ethereal, Paradox & Fusion Shrouds / Tethers / Hex Vectors (#25 through #36) ---

        // Eclipse Solar Corona Bastion (#25): melee attackers set ablaze in unquenchable celestial fire + Eclipse ward defense!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ECLIPSE_BASTION_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.BLINDNESS);
            entity.removeEffect(MobEffects.DARKNESS);
            if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                attacker.igniteForSeconds(5);
                attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                EntityHelper.getPersistentData(attacker).putLong(SpellLingeringZoneManager.TAG_ECLIPSE_CORONA_BURN_UNTIL, now + 100L);
            }
            if (!REENTRANT_GUST_HURT.get()) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
                if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                    Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                    float eclipseWardMult = 1.0F;
                    if (source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)) {
                        eclipseWardMult = 1.50F;
                    } else if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                        Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                        if (atkAffs.contains(EssenceType.RADIANT) || atkAffs.contains(EssenceType.NATURE)) {
                            eclipseWardMult = 0.50F;
                        } else if (atkAffs.contains(EssenceType.VOID)) {
                            eclipseWardMult = 1.50F;
                        }
                    }
                    if (eclipseWardMult != 1.0F) {
                        REENTRANT_GUST_HURT.set(true);
                        try {
                            entity.hurtServer(level, source, amount * eclipseWardMult);
                        } finally {
                            REENTRANT_GUST_HURT.set(false);
                        }
                        return EventResult.interruptFalse();
                    }
                }
            }
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

        // Blight Cadaverous Briar-Carapace (#26): unprovoked undead neutrality + 30% Blight thorns & Poison II (3s) + Blight ward defense!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BLIGHT_CARAPACE_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.HUNGER);
            Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            if (atkEnt instanceof Mob undeadAtk
                    && undeadAtk.getType().is(EntityTypeTags.UNDEAD)
                    && undeadAtk.getLastHurtByMob() != entity
                    && entity.getLastHurtMob() != undeadAtk) {
                undeadAtk.setTarget(null);
                undeadAtk.setAggressive(false);
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get()) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
                if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        hurtWithBlightMateriaAdvantage(level, attacker, entity, Math.max(1.5F, amount * 0.30F), false, true);
                        attacker.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                }
                if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO) && atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    float blightWardMult = 1.0F;
                    if (atkAffs.contains(EssenceType.RADIANT) || atkAffs.contains(EssenceType.NETHER) || atkAffs.contains(EssenceType.UMBRAL)) {
                        blightWardMult = 1.50F;
                    }
                    if (blightWardMult != 1.0F) {
                        REENTRANT_GUST_HURT.set(true);
                        try {
                            entity.hurtServer(level, source, amount * blightWardMult);
                        } finally {
                            REENTRANT_GUST_HURT.set(false);
                        }
                        return EventResult.interruptFalse();
                    }
                }
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
        // Astral Cosmic Starlight Shroud (#27): absorbs Lightning/Void into Materia fuel + 0.5s melee shock stun & 3-block knockback!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ASTRAL_SHROUD_UNTIL).orElse(0L)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (source.is(DamageTypeTags.IS_LIGHTNING)
                    || (source.getEntity() instanceof LivingEntity atk
                    && (resolveEntityAffinities(atk).contains(EssenceType.VOID) || resolveEntityAffinities(atk).contains(EssenceType.LIGHTNING)))) {
                if (entity instanceof Player player) {
                    SpellFuelService.restorePlayerMateriaFuel(
                            player, EssenceType.ASTRAL, Math.max(24_000L, Math.round(amount * 12_000.0F))
                    );
                }
                level.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY(1.0D), entity.getZ(), 16, 0.4D, 0.5D, 0.4D, 0.08D);
                level.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.80F, 1.35F);
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                attacker.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 10, 0));
                Vec3 away = attacker.position().subtract(entity.position());
                Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                Vec3 norm = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : entity.getLookAngle();
                attacker.setDeltaMovement(norm.x * 1.08D, 0.34D, norm.z * 1.08D);
                attacker.hasImpulse = true;
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithAstralMateriaAdvantage(level, attacker, entity, Math.max(1.5F, amount * 0.25F), false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, attacker.getX(), attacker.getY(0.85D), attacker.getZ(), 12, 0.25D, 0.30D, 0.25D, 0.08D);
            }
        }

        // Soulfire Phantasmal Soulfire Shroud (#28): total fire/lava immunity + ignite melee attackers in soulfire & siphon 20%!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SOULFIRE_SHROUD_UNTIL).orElse(0L)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (source.is(DamageTypeTags.IS_FIRE)) {
                entity.clearFire();
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                attacker.igniteForSeconds(5);
                EntityHelper.getPersistentData(attacker).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, now + 100L);
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithSoulfireMateriaAdvantage(level, attacker, entity, Math.max(1.5F, amount * 0.25F), false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                float siphon = Math.max(0.5F, amount * 0.20F);
                notifyMagicalHeal(entity, siphon);
                entity.heal(siphon);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, attacker.getX(), attacker.getY(0.85D), attacker.getZ(), 10, 0.25D, 0.30D, 0.25D, 0.04D);
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
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_VAPOR_MIST_SHROUD_UNTIL).orElse(0L)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (source.is(DamageTypeTags.IS_FIRE)) {
                entity.clearFire();
                return EventResult.interruptFalse();
            }
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

        // Null_R Event Horizon Bastion / Orbit (#30): blind & repel melee attackers 4 blocks with antimatter pulse (or zero-g lift + orb discharge on orbit)!
        if ((now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_R_BASTION_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_R_ORBIT_UNTIL).orElse(0L))
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            boolean hasBastion = now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_R_BASTION_UNTIL).orElse(0L);
            boolean hasOrbit = now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_R_ORBIT_UNTIL).orElse(0L);
            if (!REENTRANT_GUST_HURT.get()) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
                if (hasBastion) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        hurtWithNullRMateriaAdvantage(level, attacker, entity, Math.max(1.5F, amount * 0.25F), false, true);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                }
            }
            attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
            Vec3 away = attacker.position().subtract(entity.position());
            Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
            Vec3 norm = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : entity.getLookAngle();
            if (hasOrbit) {
                attacker.setDeltaMovement(norm.x * 0.55D, 0.65D, norm.z * 0.55D);
                attacker.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 50, 0));
                ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, attacker, 2);
            } else {
                attacker.setDeltaMovement(norm.x * 1.18D, 0.36D, norm.z * 1.18D);
            }
            attacker.hasImpulse = true;
            level.sendParticles(ParticleTypes.END_ROD, attacker.getX(), attacker.getY(0.9D), attacker.getZ(), 12, 0.3D, 0.3D, 0.3D, 0.06D);
        }
        // Null_R Antimatter Annihilation Hex (#30): permanently disables shield blocking!
        if (entity instanceof Player hexedShieldPlayer && hasActiveMutatedHex(data, now, EssenceType.NULL_R)) {
            if (hexedShieldPlayer.isBlocking() && !hexedShieldPlayer.getUseItem().isEmpty()) {
                hexedShieldPlayer.getCooldowns().addCooldown(hexedShieldPlayer.getUseItem(), 100);
                hexedShieldPlayer.stopUsingItem();
            }
        }

        // Null_U Abyssal Zero-Acoustic Veil / Orbit (#31): blind & silence melee attackers for 3s (or pull inward + orb discharge on orbit)!
        if ((now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_U_VEIL_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_U_ORBIT_UNTIL).orElse(0L))
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity
                && !SpellExecutionEngine.isFriendlyAllyForOasis(entity, attacker)) {
            attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            SpellLingeringZoneManager.applyArcaneSilence(attacker, 60);
            if (now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_U_VEIL_UNTIL).orElse(0L)) {
                syncEntityBarrierState(level, entity, 0.90F, attacker.getEyePosition());
                level.sendParticles(ParticleTypes.SQUID_INK, attacker.getX(), attacker.getY(0.85D), attacker.getZ(), 8, 0.25D, 0.30D, 0.25D, 0.03D);
            }
            if (now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_U_ORBIT_UNTIL).orElse(0L)) {
                Vec3 toSelf = entity.position().subtract(attacker.position());
                Vec3 pull = toSelf.lengthSqr() > 1.0E-4D ? toSelf.normalize().scale(0.60D) : Vec3.ZERO;
                attacker.setDeltaMovement(pull.x, 0.22D, pull.z);
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
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity
                && !SpellExecutionEngine.isFriendlyAllyForOasis(entity, attacker)) {
            syncEntityBarrierState(level, entity, 0.90F, attacker.getEyePosition());
            attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            Vec3 fromSelf = attacker.position().subtract(entity.position());
            Vec3 away = fromSelf.lengthSqr() > 1.0E-4D ? fromSelf.normalize().scale(0.98D) : new Vec3(0.0D, 0.35D, 0.0D);
            attacker.setDeltaMovement(away.x, 0.34D, away.z);
            attacker.hasImpulse = true;
            level.sendParticles(ParticleTypes.END_ROD, attacker.getX(), attacker.getY(0.85D), attacker.getZ(), 8, 0.25D, 0.30D, 0.25D, 0.04D);
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

        // Abyss Hadopelagic Trench Shroud & Orbit (#33): blind + drench melee attackers, or absorb blow with 40% slow + knockback!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ABYSS_SHROUD_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            SpellLingeringZoneManager.applySoaked(attacker, 140);
            level.sendParticles(ParticleTypes.SQUID_INK, attacker.getX(), attacker.getY(0.8D), attacker.getZ(), 10, 0.25D, 0.30D, 0.25D, 0.03D);
            if (!REENTRANT_GUST_HURT.get()) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            }
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ABYSS_ORBIT_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
            applyTimedMovementSpeedSap(attacker, level, 0.40D, 60L);
            attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
            SpellLingeringZoneManager.applySoaked(attacker, 100);
            Vec3 away = attacker.position().subtract(entity.position());
            Vec3 push = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(0.85D) : new Vec3(0.0D, 0.26D, 0.0D);
            attacker.setDeltaMovement(push.x, 0.26D, push.z);
            attacker.hasImpulse = true;
            ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, attacker, 1);
            if (!REENTRANT_GUST_HURT.get() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    entity.hurtServer(level, source, amount * 0.75F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
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
            if (entity instanceof Player && source.getEntity() instanceof LivingEntity attacker && attacker != entity) {
                Set<EssenceType> atkAffs = resolveEntityAffinities(attacker);
                boolean aquaticOrAbyssal = attacker instanceof Drowned || attacker instanceof Guardian
                        || attacker instanceof ElderGuardian || attacker instanceof Bogged
                        || attacker instanceof WaterAnimal || attacker instanceof Squid
                        || atkAffs.contains(EssenceType.WATER) || atkAffs.contains(EssenceType.ABYSS)
                        || atkAffs.contains(EssenceType.UMBRAL_WATER)
                        || SpellLingeringZoneManager.isEntityWetOrSoaked(attacker);
                if (aquaticOrAbyssal) {
                    attacker.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100, 0));
                    applyTimedMovementSpeedBoost(attacker, level, 0.50D, 100L);
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        entity.hurtServer(level, source, amount * 2.0F);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }
        }

        // Aegis Gilded Bastion Orbit (#34): launches orbiting golden granite plates outward to batter melee attackers!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_AEGIS_ORBIT_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity aegisAttacker && aegisAttacker != entity) {
            ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, aegisAttacker, 2);
            if (!REENTRANT_GUST_HURT.get()) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            }
        }
        // Aegis Citadel Tether Beam (#34): 50% damage reduction on anchored ally!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_AEGIS_TETHER_UNTIL).orElse(0L)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            level.playSound(null, entity.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 0.65F, 1.45F);
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
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            level.playSound(null, entity.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 0.70F, 1.30F);
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
                boolean hostileMobStrike = source.getEntity() instanceof Monster
                        || (source.getEntity() instanceof Mob m && m.getTarget() == entity);
                if (entity instanceof Player && hostileMobStrike) {
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
            if (!REENTRANT_GUST_HURT.get()) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            }
            if (source.is(DamageTypeTags.IS_FREEZING)) {
                entity.setTicksFrozen(0);
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity attacker
                    && attacker != entity
                    && !SpellExecutionEngine.isFriendlyAllyForOasis(entity, attacker)) {
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
            if (!REENTRANT_GUST_HURT.get()) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            }
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
                        || now <= atkData.getLong(SpellLingeringZoneManager.TAG_STATIC_ORBIT_UNTIL).orElse(0L)
                        || now <= atkData.getLong(SpellLingeringZoneManager.TAG_SYLVAN_ORBIT_UNTIL).orElse(0L)
                        || now <= atkData.getLong(SpellLingeringZoneManager.TAG_GENESIS_ORBIT_UNTIL).orElse(0L)
                        || now <= atkData.getLong(SpellLingeringZoneManager.TAG_AETHER_ORBIT_UNTIL).orElse(0L)) {
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
        if (!REENTRANT_GUST_HURT.get()
                && source.getEntity() instanceof LivingEntity hexedWraithAtk
                && hexedWraithAtk != entity
                && !(source.getDirectEntity() == hexedWraithAtk)
                && hasActiveMutatedHex(EntityHelper.getPersistentData(hexedWraithAtk), now, EssenceType.WRAITH)) {
            // Section 6.38 Incorporeal Soul-Hollowing Hex: permanently halves ranged/magic damage dealt (-50%) alongside melee!
            REENTRANT_GUST_HURT.set(true);
            try {
                entity.hurtServer(level, source, amount * Math.max(0.20F, 1.0F - 0.50F * getHexStackMultiplier(hexedWraithAtk)));
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
            return EventResult.interruptFalse();
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (!REENTRANT_GUST_HURT.get()
                    && source.is(DamageTypeTags.WITCH_RESISTANT_TO)
                    && source.getEntity() instanceof LivingEntity magicAtk && magicAtk != entity) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithWraithMateriaAdvantage(level, magicAtk, entity, Math.max(1.0F, amount * 0.25F), false, true);
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

        // #38 Barrow Shroud (Poison & Wither immune), Ossuary Bone-Shield Orbit (-25% physical + bone shrapnel), & Sepulchral Hex (+35% physical)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BARROW_SHROUD_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WITHER);
            if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
        }
        if (!REENTRANT_GUST_HURT.get()
                && now <= data.getLong(SpellLingeringZoneManager.TAG_BARROW_ORBIT_UNTIL).orElse(0L)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != entity) {
                    hurtWithBarrowMateriaAdvantage(level, attacker, entity, 2.65F, false, true);
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

        // #39 Pyre Shroud (Fire/Lava/Freeze immune), Orbit (4s attacker burn + Pyre ember contact), & Cremation Hex (struck victims emit incendiary embers!)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_PYRE_SHROUD_UNTIL).orElse(0L)) {
            entity.clearFire();
            entity.setTicksFrozen(0);
            if (source.is(DamageTypeTags.IS_FIRE)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)
                    || source.is(DamageTypeTags.IS_FREEZING)) {
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_PYRE_ORBIT_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity pyreAtk && pyreAtk != entity) {
            pyreAtk.igniteForSeconds(4);
            EntityHelper.getPersistentData(pyreAtk).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 80L);
            if (!REENTRANT_GUST_HURT.get()) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithPyreMateriaAdvantage(level, pyreAtk, entity, 2.25F, false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }
        if (!REENTRANT_GUST_HURT.get() && hasActiveMutatedHex(data, now, EssenceType.PYRE)) {
            REENTRANT_GUST_HURT.set(true);
            try {
                LivingEntity hexCaster = resolveHexCaster(level, entity);
                float emberMult = getHexStackMultiplier(entity);
                level.sendParticles(ParticleTypes.LAVA, entity.getX(), entity.getY(0.85D), entity.getZ(), 8, 0.35D, 0.35D, 0.35D, 0.05D);
                for (LivingEntity peer : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(3.5D))) {
                    if (peer == entity || peer == hexCaster || !peer.isAlive()) continue;
                    if (hexCaster != null && isProtectedFromOffensiveSpell(peer, hexCaster, true)) continue;
                    if ((entity instanceof Monster && peer instanceof Monster) || peer.isAlliedTo(entity)) {
                        peer.igniteForSeconds(3);
                        EntityHelper.getPersistentData(peer).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 60L);
                        hurtWithPyreMateriaAdvantage(level, peer, hexCaster, 2.20F * emberMult, false, true);
                    }
                }
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
        }

        // #40 Penumbra Shimmer Dodge, Mantle (25% projectile void-glance + Penumbral Dazzle), & Orbit (contact Penumbra damage + 3s blind)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_SHIMMER_UNTIL).orElse(0L)
                && (source.getDirectEntity() != null || source.is(DamageTypeTags.IS_PROJECTILE))) {
            float dodgePct = data.getFloat(SpellLingeringZoneManager.TAG_PENUMBRA_SHIMMER_DODGE).orElse(0.20F);
            if (level.random.nextFloat() < dodgePct) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, entity.getX(), entity.getY(0.9D), entity.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.04D);
                return EventResult.interruptFalse();
            }
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_MANTLE_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.BLINDNESS);
            entity.removeEffect(MobEffects.DARKNESS);
            if (source.is(DamageTypeTags.IS_PROJECTILE) && level.random.nextFloat() < 0.25F) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, entity.getX(), entity.getY(0.9D), entity.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.04D);
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (source.getDirectEntity() instanceof LivingEntity penAtk && penAtk != entity) {
                EntityHelper.getPersistentData(penAtk).putLong(SpellLingeringZoneManager.TAG_PENUMBRA_DAZZLE_UNTIL, now + 60L);
            }
        }
        if (!REENTRANT_GUST_HURT.get()
                && now <= data.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_ORBIT_UNTIL).orElse(0L)
                && source.getDirectEntity() instanceof LivingEntity penAtk && penAtk != entity) {
            REENTRANT_GUST_HURT.set(true);
            try {
                hurtWithPenumbraMateriaAdvantage(level, penAtk, entity, 2.65F, false, true);
                penAtk.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
        }

        // #41 Rime Mantle, Orbit, Hoarfrost Carapace (50% fire res), & Cryo-Numbness Hex (armor enchant suppression)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_RIME_MANTLE_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.SLOWNESS);
            if (source.is(DamageTypeTags.IS_FREEZING) || source.is(DamageTypeTags.IS_FIRE)) {
                entity.clearFire();
                entity.setTicksFrozen(0);
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (source.getDirectEntity() instanceof LivingEntity rimeAtk && rimeAtk != entity) {
                applyTimedAttackDebuff(rimeAtk, level, 0.0D, 0.35D, 80L);
                rimeAtk.setTicksFrozen(Math.min(rimeAtk.getTicksRequiredToFreeze() + 40, rimeAtk.getTicksFrozen() + 60));
                if (!REENTRANT_GUST_HURT.get()) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        hurtWithRimeMateriaAdvantage(level, rimeAtk, entity, 2.35F, false, true);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                }
            }
        } else if (now <= data.getLong(SpellLingeringZoneManager.TAG_RIME_ORBIT_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.SLOWNESS);
            if (source.is(DamageTypeTags.IS_FREEZING)) {
                entity.setTicksFrozen(0);
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity rimeAtk && rimeAtk != entity) {
                applyTimedAttackDebuff(rimeAtk, level, 0.0D, 0.30D, 60L);
                if (!REENTRANT_GUST_HURT.get()) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        hurtWithRimeMateriaAdvantage(level, rimeAtk, entity, 2.10F, false, true);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                }
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

        // #42 Spring Vernal Fountainhead Mantle & Orbit (Drowning, Poison, Wither, Fire immunity & projectile -> 1 HP healing mist)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SPRING_MANTLE_UNTIL).orElse(0L)) {
            entity.clearFire();
            entity.setAirSupply(entity.getMaxAirSupply());
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WITHER);
            if (source.is(DamageTypeTags.IS_DROWNING)
                    || source.is(DamageTypeTags.IS_FIRE)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SPRING_ORBIT_UNTIL).orElse(0L)) {
            entity.clearFire();
            entity.setAirSupply(entity.getMaxAirSupply());
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WITHER);
            if (source.is(DamageTypeTags.IS_DROWNING) || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            if (source.is(DamageTypeTags.IS_PROJECTILE) && source.getDirectEntity() instanceof Projectile incProj) {
                level.sendParticles(ParticleTypes.CHERRY_LEAVES, entity.getX(), entity.getY(0.9D), entity.getZ(), 6, 0.25D, 0.25D, 0.25D, 0.03D);
                incProj.discard();
                notifyMagicalHeal(entity, 1.0F);
                entity.heal(1.0F);
                return EventResult.interruptFalse();
            }
        }

        // #43 Static Dielectric Faraday Shroud, Galvanic Tesla Orbit, Sentry Immunity & Arc-Snare Shared Shock
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_STATIC_SHROUD_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.SLOWNESS);
            if (source.is(DamageTypeTags.IS_LIGHTNING)
                    || source.is(DamageTypeTags.IS_FALL)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.THORNS)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.FLY_INTO_WALL)) {
                entity.resetFallDistance();
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity staticAtk && staticAtk != entity) {
                REENTRANT_GUST_HURT.set(true);
                try {
                    staticAtk.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                    hurtWithStaticMateriaAdvantage(level, staticAtk, entity, 3.85F, false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        } else if (now <= data.getLong(SpellLingeringZoneManager.TAG_STATIC_ORBIT_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.SLOWNESS);
            if (source.is(DamageTypeTags.IS_LIGHTNING) || source.is(DamageTypeTags.IS_FALL)) {
                entity.resetFallDistance();
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity staticAtk && staticAtk != entity) {
                staticAtk.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
                if (!REENTRANT_GUST_HURT.get()) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        hurtWithStaticMateriaAdvantage(level, staticAtk, entity, 2.40F, false, true);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                }
            }
        } else if (now <= data.getLong(SpellLingeringZoneManager.TAG_STATIC_SENTRY_IMMUNE_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_LIGHTNING) || source.is(DamageTypeTags.IS_FALL)) {
                entity.resetFallDistance();
                return EventResult.interruptFalse();
            }
        }
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_STATIC_ARC_SNARE_UNTIL).orElse(0L)) {
            String group = data.getString(SpellLingeringZoneManager.TAG_STATIC_ARC_SNARE_GROUP).orElse("");
            if (!group.isEmpty()) {
                String cStr = data.getString(SpellLingeringZoneManager.TAG_STATIC_ARC_SNARE_CASTER).orElse("");
                LivingEntity snareCaster = null;
                if (!cStr.isEmpty()) {
                    try {
                        Entity ce = level.getEntity(java.util.UUID.fromString(cStr));
                        if (ce instanceof LivingEntity cl) snareCaster = cl;
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                REENTRANT_GUST_HURT.set(true);
                try {
                    float sharedShock = Math.max(1.25F, amount * 0.45F);
                    for (LivingEntity peer : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(10.0D), e -> e.isAlive() && e != entity)) {
                        CompoundTag pTag = EntityHelper.getPersistentData(peer);
                        if (now <= pTag.getLong(SpellLingeringZoneManager.TAG_STATIC_ARC_SNARE_UNTIL).orElse(0L)
                                && group.equals(pTag.getString(SpellLingeringZoneManager.TAG_STATIC_ARC_SNARE_GROUP).orElse(""))) {
                            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastChainArc(
                                    level, entity.getBoundingBox().getCenter(), peer.getBoundingBox().getCenter(),
                                    EssenceType.STATIC, EssenceType.ARID, 3
                            );
                            hurtWithStaticMateriaAdvantage(level, peer, snareCaster, sharedShock, false, true);
                        }
                    }
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // #44 Sylvan Primeval Photosynthetic Aegis (absorbs up to 12 hits, converting 50% into Materia fuel), Orbit (1.5s attacker root) & Solar-Glide
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SYLVAN_SOLAR_GLIDE_UNTIL).orElse(0L)
                && source.is(DamageTypeTags.IS_FALL)) {
            entity.resetFallDistance();
            return EventResult.interruptFalse();
        }
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WITHER);
            entity.removeEffect(MobEffects.BLINDNESS);
            if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            int hitsLeft = data.getInt(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_HITS).orElse(0);
            if (hitsLeft > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                data.putInt(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_HITS, hitsLeft - 1);
                if (hitsLeft - 1 <= 0) {
                    data.remove(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL);
                    triggerBarrierShatterOrFallback(level, entity, EssenceType.SYLVAN, 5, hitOrigin);
                } else {
                    syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
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
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SYLVAN_ORBIT_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WITHER);
            if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity sylAtk && sylAtk != entity) {
                sylAtk.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 5));
                sylAtk.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 30, 0));
                if (!REENTRANT_GUST_HURT.get()) {
                    REENTRANT_GUST_HURT.set(true);
                    try {
                        float smite = sylAtk.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
                        hurtWithSylvanMateriaAdvantage(level, sylAtk, entity, 2.35F * smite, false, true);
                    } finally {
                        REENTRANT_GUST_HURT.set(false);
                    }
                }
            }
        }

        // #45 Miasma Putrescent Mire-Carapace & Septic Immunity (Drown/Wither/Poison/Hunger immune, 35% melee reflect, -2 attacker armor)
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_MIASMA_SEPTIC_IMMUNE_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WITHER);
            entity.removeEffect(MobEffects.HUNGER);
            if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_MIASMA_CARAPACE_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WITHER);
            entity.removeEffect(MobEffects.HUNGER);
            entity.setAirSupply(entity.getMaxAirSupply());
            if (source.is(DamageTypeTags.IS_DROWNING) || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity miasmaAtk && miasmaAtk != entity) {
                syncEntityBarrierState(level, entity, 0.50F, miasmaAtk.position());
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithMiasmaMateriaAdvantage(level, miasmaAtk, entity, Math.max(1.5F, amount * 0.35F), false, true);
                    applyTimedAttributeShred(miasmaAtk, level, 2.0D, 0.0D, 100L);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // #46 Genesis Amniotic Shroud (absorbs 40% -> Regen II + Materia fuel, blinds & knocks back 4m) & Orbit (5m KB + 3s blind)
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_GENESIS_SHROUD_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.BLINDNESS);
            entity.removeEffect(MobEffects.DARKNESS);
            entity.removeEffect(MobEffects.WITHER);
            entity.removeEffect(MobEffects.POISON);
            entity.setAirSupply(entity.getMaxAirSupply());
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
                    syncEntityBarrierState(level, entity, 0.52F, genAtk.position());
                    genAtk.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    Vec3 delta = genAtk.position().subtract(entity.position());
                    Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.15D) : new Vec3(0.0D, 0.32D, 0.0D);
                    genAtk.setDeltaMovement(away.x, 0.32D, away.z);
                    genAtk.hasImpulse = true;
                } else {
                    syncEntityBarrierState(level, entity, 0.45F, null);
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

        // #47 Oblivion Extinction Armor (Fire/Lava/Wither immunity + 40% melee reflection) & Ash-Warp / Cataclysm Void-Warp Fire/Fall Immunity
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_OBLIVION_FIRE_FALL_IMMUNE_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FIRE)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)
                    || source.is(DamageTypeTags.IS_FALL)) {
                entity.clearFire();
                entity.resetFallDistance();
                return EventResult.interruptFalse();
            }
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_OBLIVION_EXTINCTION_ARMOR_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FIRE) || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                entity.clearFire();
                entity.removeEffect(MobEffects.WITHER);
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity oblAtk && oblAtk != entity) {
                syncEntityBarrierState(level, entity, 0.50F, oblAtk.position());
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithOblivionMateriaAdvantage(level, oblAtk, entity, Math.max(1.5F, amount * 0.40F), false, true);
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

        boolean isCombatHit = source.getEntity() != null
                || source.getDirectEntity() != null
                || source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypeTags.IS_EXPLOSION);

        long until = data.getLong(TAG_WARD_UNTIL).orElse(0L);
        if (now > until) {
            if (!REENTRANT_GUST_HURT.get() && isCombatHit) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            }
            return EventResult.pass();
        }

        float shieldHp = data.getFloat(TAG_WARD_HP).orElse(0.0F);
        if (shieldHp <= 0.0F) {
            if (!REENTRANT_GUST_HURT.get() && isCombatHit) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            }
            return EventResult.pass();
        }

        // Section 5.3 Combat Damage Only (Environmental Bypass):
        // Ambient lava, fire, drowning, suffocation, fall damage, and Void out-of-world bypass the Arcane Shield completely!
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
        int wardTier = Math.clamp(data.getInt(TAG_WARD_TIER).orElse(1), 1, 10);

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
            syncEntityBarrierState(level, entity, 1.30F, hitOrigin);
            level.sendParticles(ParticleTypes.ENCHANT, entity.getX(), entity.getY(0.9D), entity.getZ(),
                    14, 0.35D, 0.45D, 0.35D, 0.1D);
            level.playSound(null, entity.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 0.75F, 1.65F);
            return EventResult.interruptFalse();
        }

        if (shieldHp >= amount) {
            float left = shieldHp - amount;
            data.putFloat(TAG_WARD_HP, left);
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
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
            triggerBarrierShatterOrFallback(level, entity, wardType, wardTier, hitOrigin);
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

    public static Set<EssenceType> resolveEntityAffinities(LivingEntity entity) {
        Set<EssenceType> set = EnumSet.noneOf(EssenceType.class);
        var type = entity.getType();
        CompoundTag data = EntityHelper.getPersistentData(entity);
        long now = entity.level().getGameTime();

        if (entity instanceof SpellEidolonEntity eid) {
            EssenceType em = eid.getEffectiveMateria();
            if (em != null && em != EssenceType.REGULAR) {
                set.add(em);
            }
        }

        if (type.is(EntityTypeTags.UNDEAD) || type.is(ModEntityTags.DROPS_UNDEAD_ESSENCE)
                || entity instanceof Zombie || entity instanceof Skeleton
                || entity instanceof WitherSkeleton || entity instanceof Phantom || entity instanceof WitherBoss
                || now <= data.getLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.UNDEAD);
        }
        if (type.is(ModEntityTags.DROPS_WATER_ESSENCE) || entity instanceof Drowned || entity instanceof Guardian
                || entity instanceof ElderGuardian || entity instanceof WaterAnimal || entity instanceof Squid
                || now <= data.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.WATER);
        }
        if (type.is(ModEntityTags.DROPS_NETHER_ESSENCE) || entity instanceof ZombifiedPiglin || entity instanceof Piglin
                || entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Ghast
                || now <= data.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.NETHER);
        }
        if (type.is(ModEntityTags.DROPS_FROZEN_ESSENCE) || entity instanceof Stray || entity instanceof PolarBear
                || now <= data.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)) {
            set.add(EssenceType.FROZEN);
        }
        if (type.is(ModEntityTags.DROPS_VOID_ESSENCE) || entity instanceof EnderMan || entity instanceof Endermite
                || entity instanceof Shulker || entity instanceof EnderDragon) {
            set.add(EssenceType.VOID);
        }
        if (type.is(ModEntityTags.DROPS_AIR_ESSENCE) || entity instanceof Breeze || entity instanceof AmbientCreature
                || now <= data.getLong(SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_AIR_SLIPSTREAM_UNTIL).orElse(0L)) {
            set.add(EssenceType.AIR);
        }
        if (type.is(ModEntityTags.DROPS_EARTH_ESSENCE) || entity instanceof Slime || entity instanceof Silverfish
                || now <= data.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)) {
            set.add(EssenceType.EARTH);
        }
        if (type.is(ModEntityTags.DROPS_NATURE_ESSENCE)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L)) {
            set.add(EssenceType.NATURE);
        }
        if (type.is(ModEntityTags.DROPS_RADIANT_ESSENCE)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_RADIANT_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.RADIANT);
        }
        if (type.is(ModEntityTags.DROPS_UMBRAL_ESSENCE) || entity instanceof Warden
                || now <= data.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.UMBRAL);
        }
        if (type.is(ModEntityTags.DROPS_LIGHTNING_ESSENCE)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_LIGHTNING_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.LIGHTNING);
        }
        if (type.is(ModEntityTags.DROPS_ARID_ESSENCE) || entity instanceof Husk) {
            set.add(EssenceType.ARID);
        }
        if (entity instanceof MagmaCube || now <= data.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)) {
            set.add(EssenceType.MAGMA);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_STORM_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.STORM);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.GLACIAL);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BASTION_UNTIL).orElse(0L)) {
            set.add(EssenceType.OVERGROWTH);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_DUST_VEIL_UNTIL).orElse(0L)) {
            set.add(EssenceType.DUST);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SPORE_MANTLE_UNTIL).orElse(0L)) {
            set.add(EssenceType.SPORE);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_TAIGA_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.TAIGA);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_OASIS_CARAPACE_UNTIL).orElse(0L)) {
            set.add(EssenceType.OASIS);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_VITAE_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.VITAE);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BLOOD_CARAPACE_UNTIL).orElse(0L)) {
            set.add(EssenceType.BLOOD);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_AURA_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.AURA);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_AMBER_MANTLE_UNTIL).orElse(0L)) {
            set.add(EssenceType.AMBER);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ECLIPSE_BASTION_UNTIL).orElse(0L)) {
            set.add(EssenceType.ECLIPSE);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BLIGHT_CARAPACE_UNTIL).orElse(0L)) {
            set.add(EssenceType.BLIGHT);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ASTRAL_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.ASTRAL);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SOULFIRE_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.SOULFIRE);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_VAPOR_MIST_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.VAPOR);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_R_BASTION_UNTIL).orElse(0L)) {
            set.add(EssenceType.NULL_R);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_NULL_U_VEIL_UNTIL).orElse(0L)) {
            set.add(EssenceType.NULL_U);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_DAWN_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.DAWN);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ABYSS_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.ABYSS);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_AEGIS_MANTLE_UNTIL).orElse(0L)) {
            set.add(EssenceType.AEGIS);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_AURORA_MANTLE_UNTIL).orElse(0L)) {
            set.add(EssenceType.AURORA);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_MIRAGE_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.MIRAGE);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.WRAITH);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_BARROW_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.BARROW);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_PYRE_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.PYRE);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_PENUMBRA_MANTLE_UNTIL).orElse(0L)) {
            set.add(EssenceType.PENUMBRA);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_RIME_MANTLE_UNTIL).orElse(0L)) {
            set.add(EssenceType.RIME);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SPRING_MANTLE_UNTIL).orElse(0L)) {
            set.add(EssenceType.SPRING);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_STATIC_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.STATIC);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL).orElse(0L)) {
            set.add(EssenceType.SYLVAN);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_MIASMA_CARAPACE_UNTIL).orElse(0L)) {
            set.add(EssenceType.MIASMA);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_GENESIS_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.GENESIS);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_OBLIVION_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_OBLIVION_EXTINCTION_ARMOR_UNTIL).orElse(0L)) {
            set.add(EssenceType.OBLIVION);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_AETHER_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_AETHER_AEGIS_UNTIL).orElse(0L)) {
            set.add(EssenceType.AETHER);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ENTROPIC_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_ENTROPIC_LIFESTEAL_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.ENTROPIC);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_ASCENDANCE_UNTIL).orElse(0L)) {
            set.add(EssenceType.CELESTIAL);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_AEGIS_UNTIL).orElse(0L)) {
            set.add(EssenceType.EMPYREAN);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_CATACLYSM_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.CATACLYSM);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_MANTLE_UNTIL).orElse(0L)) {
            set.add(EssenceType.TERMINUS);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ESCHATON_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.ESCHATON);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.APOTHEOSIS);
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL).orElse(0L)) {
            set.add(EssenceType.ENTROPICA);
        }
        return set;
    }

    public static boolean isBossEntity(ServerLevel level, LivingEntity entity) {
        return isBossEntity(entity);
    }

    public static boolean isBossEntity(LivingEntity entity) {
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
                || t == EssenceType.VESPER || t == EssenceType.ABYSS || t == EssenceType.UMBRAL_WATER
                || t == EssenceType.PENUMBRA || t == EssenceType.OBLIVION || t == EssenceType.ESCHATON;
    }

    private static boolean isAirLine(EssenceType t) {
        return t == EssenceType.AIR || t == EssenceType.STORM || t == EssenceType.AURA
                || t == EssenceType.DUST || t == EssenceType.RIME || t == EssenceType.STATIC;
    }

    private static boolean isWaterLine(EssenceType t) {
        return t == EssenceType.WATER || t == EssenceType.STORM || t == EssenceType.VAPOR || t == EssenceType.ABYSS
                || t == EssenceType.SPRING || t == EssenceType.MIASMA || t == EssenceType.GENESIS;
    }

    private static boolean isLightningLine(EssenceType t) {
        return t == EssenceType.LIGHTNING || t == EssenceType.STATIC || t == EssenceType.ASTRAL;
    }

    private static boolean isNatureLine(EssenceType t) {
        return t == EssenceType.NATURE || t == EssenceType.OVERGROWTH || t == EssenceType.SPORE
                || t == EssenceType.TAIGA || t == EssenceType.OASIS
                || t == EssenceType.SPRING || t == EssenceType.SYLVAN || t == EssenceType.MIASMA;
    }

    /**
     * Routes spell elemental advantage scoring directly through {@link VisInteractionManager}'s canonical
     * Defender-Lineage &amp; Offensive/Defensive Clash engine so combat spells and Restoration share one exact matrix.
     */
    public static int calculateSpellElementalAdvantageScore(
            @Nullable EssenceType attackMateria,
            Collection<EssenceType> defenderAffinities
    ) {
        return VisInteractionManager.getInstance().calculateLineageAdvantageScore(attackMateria, defenderAffinities);
    }

    /**
     * Routes spell damage multipliers through {@link VisInteractionManager}'s Universal Linear Scale
     * ({@code 1.0 + 0.5 * score} positive, {@code 0.5^|score|} negative), Mob-to-Player survival bounds
     * ({@code 0.10x} floor / {@code 3.2x} cap, or {@code 0.0x} Alpha / {@code 2.5x} Omega), Tier 5 Divine Override
     * ({@code 4.0x}), and Tier 6 Singularity Absolute Override ({@code 0.0x} immunity / {@code 5.0x} Mirror &amp; Entropica clash).
     */
    public static float calculateSpellAdvantageDamageMultiplier(
            @Nullable EssenceType attackMateria,
            @Nullable LivingEntity caster,
            @Nullable LivingEntity target,
            Collection<EssenceType> defenderAffinities,
            float bonusAdvantageScore
    ) {
        boolean fromPlayer = (caster instanceof Player)
                || (caster instanceof SpellEidolonEntity eid
                    && eid.level() instanceof ServerLevel eidSrv
                    && eid.resolveSummoner(eidSrv) instanceof Player);
        boolean mobToPlayer = (target instanceof Player) && !fromPlayer;
        boolean alphaLeaning = isAlphaLeaningEntity(target);
        boolean omegaLeaning = isOmegaLeaningEntity(target);
        return VisInteractionManager.getInstance().calculateDamageMultiplier(
                attackMateria,
                defenderAffinities,
                mobToPlayer,
                bonusAdvantageScore,
                alphaLeaning,
                omegaLeaning
        );
    }

    private static boolean isAlphaLeaningEntity(@Nullable LivingEntity entity) {
        if (entity == null) return false;
        CompoundTag data = EntityHelper.getPersistentData(entity);
        if (data.getBoolean("AlphaLeaning").orElse(false) || data.getBoolean("SpellAlphaLeaning").orElse(false)) {
            return true;
        }
        return hasWeaponCoreKeyword(entity.getMainHandItem(), "alpha")
                || hasWeaponCoreKeyword(entity.getOffhandItem(), "alpha");
    }

    private static boolean isOmegaLeaningEntity(@Nullable LivingEntity entity) {
        if (entity == null) return false;
        CompoundTag data = EntityHelper.getPersistentData(entity);
        if (data.getBoolean("OmegaLeaning").orElse(false) || data.getBoolean("SpellOmegaLeaning").orElse(false)) {
            return true;
        }
        return hasWeaponCoreKeyword(entity.getMainHandItem(), "omega")
                || hasWeaponCoreKeyword(entity.getOffhandItem(), "omega");
    }

    private static boolean hasWeaponCoreKeyword(ItemStack stack, String keyword) {
        if (stack == null || stack.isEmpty()) return false;
        VisWeaponState ws = stack.get(ModDataComponents.VIS_WEAPON_STATE.get());
        return ws != null && ws.coreType() != null && ws.coreType().toLowerCase(Locale.ROOT).contains(keyword);
    }

    /**
     * Resolves the active spell or weapon potency tier for melee conversion fuel siphons, checking persistent primed/cast
     * tier data first, then equipped Casting Apparatus active socket, Inscribed Spellgem assembly, and Materia weapon tier,
     * falling back to {@code fallbackTier} only when no tier metadata is present.
     */
    public static int resolveSpellOrWeaponTier(@Nullable LivingEntity attacker, int fallbackTier) {
        int safeFallback = Math.clamp(fallbackTier, 1, 10);
        if (attacker == null) {
            return safeFallback;
        }
        CompoundTag data = EntityHelper.getPersistentData(attacker);
        int primedTier = data.getInt("SpellPrimedTouchTier").orElse(0);
        if (primedTier > 0) {
            return Math.clamp(primedTier, 1, 10);
        }
        int lastCastTier = data.getInt("SpellLastCastTier").orElse(0);
        if (lastCastTier > 0) {
            return Math.clamp(lastCastTier, 1, 10);
        }
        for (ItemStack held : List.of(attacker.getMainHandItem(), attacker.getOffhandItem())) {
            if (held.isEmpty()) continue;
            CastingApparatusState app = held.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
            if (app != null && app.getActiveSpell().isPresent()) {
                return app.getActiveSpell().get().getEffectiveTier();
            }
            SpellAssembly asm = held.get(ModDataComponents.SPELL_ASSEMBLY.get());
            if (asm != null && asm.potencyTier() > 0) {
                return asm.getEffectiveTier();
            }
            VisWeaponState ws = held.get(ModDataComponents.VIS_WEAPON_STATE.get());
            if (ws != null && ws.baseType() != EssenceType.REGULAR) {
                int familyMinTier = Math.max(1, MateriaFamilyTier.classify(ws.baseType()).getFusionTier());
                return Math.clamp(Math.max(familyMinTier, 1 + ws.slottedFragments().size()), 1, 10);
            }
        }
        return safeFallback;
    }

    private static EventResult interceptApexEschatonSingularityDamage(
            ServerLevel level,
            LivingEntity entity,
            CompoundTag data,
            DamageSource source,
            float amount,
            long now
    ) {
        // 1. Attacker Hex Stamina Winded Cadence (#1 Air Hypoxia +40% mob melee interval), Fumble Miss & Weapon Recoil
        if (!REENTRANT_GUST_HURT.get() && source.getEntity() instanceof LivingEntity atk && atk != entity) {
            CompoundTag atkData = EntityHelper.getPersistentData(atk);
            if (atk instanceof Mob mobAtk
                    && hasActiveMutatedHex(atkData, now, EssenceType.AIR)
                    && !source.is(DamageTypeTags.IS_PROJECTILE)
                    && (source.getDirectEntity() == mobAtk || source.getDirectEntity() == null)) {
                long meleeCdUntil = atkData.getLong(TAG_AIR_HYPOXIA_MELEE_CD_UNTIL).orElse(0L);
                if (now < meleeCdUntil) {
                    level.sendParticles(ParticleTypes.CLOUD,
                            mobAtk.getX(), mobAtk.getEyeY() - 0.1D, mobAtk.getZ(),
                            4, 0.2D, 0.15D, 0.2D, 0.02D);
                    return EventResult.interruptFalse();
                }
                // Enforce +40% longer melee attack interval (28t base vs vanilla 20t, scaling with Hex stack)!
                long windedInterval = Math.max(14L, Math.round(28.0F * getHexStackMultiplier(mobAtk)));
                atkData.putLong(TAG_AIR_HYPOXIA_MELEE_CD_UNTIL, now + windedInterval);
            }

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
                || source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.CRAMMING)
                || source.is(net.minecraft.world.damagesource.DamageTypes.FLY_INTO_WALL))) {
            entity.resetFallDistance();
            entity.setAirSupply(entity.getMaxAirSupply());
            syncEntityBarrierState(level, entity, computeHitFlare(entity, amount), resolveHitOrigin(entity, source));
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
                syncEntityBarrierState(level, entity, 0.55F, entAtk.position());
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithEntropicMateriaAdvantage(level, entAtk, entity, Math.max(1.5F, amount * 0.40F), false, true);
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
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity celAtk && celAtk != entity) {
                syncEntityBarrierState(level, entity, 0.60F, celAtk.position());
                celAtk.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                Vec3 diff = celAtk.position().subtract(entity.position());
                Vec3 away = (diff.lengthSqr() > 1.0E-4D ? diff.normalize() : new Vec3(0.0D, 0.3D, 1.0D)).scale(1.25D);
                celAtk.setDeltaMovement(away.x, 0.38D, away.z);
                celAtk.hasImpulse = true;
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithCelestialMateriaAdvantage(level, celAtk, entity, Math.max(2.0F, amount * 0.40F), false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        } else if (now <= data.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_ASCENDANCE_UNTIL).orElse(0L)) {
            if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                return EventResult.interruptFalse();
            }
            if (!REENTRANT_GUST_HURT.get() && source.getDirectEntity() instanceof LivingEntity celAtk && celAtk != entity) {
                syncEntityBarrierState(level, entity, 0.55F, celAtk.position());
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithCelestialMateriaAdvantage(level, celAtk, entity, Math.max(2.0F, amount * 0.40F), false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_CELESTIAL_ORBIT_UNTIL).orElse(0L)) {
            Entity orbitAtk = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            if (orbitAtk instanceof LivingEntity celAtk && celAtk != entity && celAtk.isAlive()) {
                celAtk.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, celAtk, 1);
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithCelestialMateriaAdvantage(level, celAtk, entity, 3.6F, false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // 5. #51 Prismatic Shroud (Fire/Lava/Drowning Immunity) & Kaleidoscope Orbit Retaliation
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_PRISMATIC_SHROUD_UNTIL).orElse(0L)
                && (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_DROWNING))) {
            entity.clearFire();
            syncEntityBarrierState(level, entity, computeHitFlare(entity, amount), resolveHitOrigin(entity, source));
            return EventResult.interruptFalse();
        }
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_PRISMATIC_ORBIT_UNTIL).orElse(0L)) {
            Entity orbitAtk = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            if (orbitAtk instanceof LivingEntity prismAtk && prismAtk != entity && prismAtk.isAlive()) {
                prismAtk.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, prismAtk, 1);
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithPrismaticMateriaAdvantage(level, prismAtk, entity, 3.5F, false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // 6. #52 Empyrean Shroud, Aegis & Orbit (Suffocation/Drowning/Poison/Wither Immunity + 2s Orbit Root, -3 Armor & Siphon)
        if ((now <= data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_AEGIS_UNTIL).orElse(0L))
                && (source.is(DamageTypeTags.IS_DROWNING)
                || source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)
                || source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC) && entity.hasEffect(MobEffects.POISON))) {
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WITHER);
            entity.removeEffect(MobEffects.HUNGER);
            syncEntityBarrierState(level, entity, computeHitFlare(entity, amount), resolveHitOrigin(entity, source));
            return EventResult.interruptFalse();
        }
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_ORBIT_UNTIL).orElse(0L)) {
            Entity orbitAtk = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            if (orbitAtk instanceof LivingEntity empAtk && empAtk != entity && empAtk.isAlive()) {
                empAtk.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 5));
                empAtk.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
                applyTimedAttributeShred(empAtk, level, 3.0D, 0.0D, 120L);
                ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, empAtk, 1);
                REENTRANT_GUST_HURT.set(true);
                try {
                    float dealt = hurtWithEmpyreanMateriaAdvantage(level, empAtk, entity, 3.8F, false, true, false, false);
                    if (dealt > 0.0F) {
                        SpellLingeringZoneManager.grantPartyAbsorption(level, entity, Math.max(2.0F, dealt * 0.50F), 32.0F, 12.0D);
                    }
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // 7. #53 Cataclysm Shroud (Fire/Lava/Hot-Floor/Wither Immunity) & Singularity Orbit Retaliation
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_CATACLYSM_SHROUD_UNTIL).orElse(0L)) {
            entity.removeEffect(MobEffects.BLINDNESS);
            entity.removeEffect(MobEffects.WITHER);
            if (source.is(DamageTypeTags.IS_FIRE)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                entity.clearFire();
                syncEntityBarrierState(level, entity, computeHitFlare(entity, amount), resolveHitOrigin(entity, source));
                return EventResult.interruptFalse();
            }
            syncEntityBarrierState(level, entity, computeHitFlare(entity, amount), resolveHitOrigin(entity, source));
        }
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_CATACLYSM_ORBIT_UNTIL).orElse(0L)) {
            Entity orbitAtk = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            if (orbitAtk instanceof LivingEntity catAtk && catAtk != entity && catAtk.isAlive()) {
                applyPercentageArmorShred(catAtk, level, 0.0D, 0.40D, 120L);
                catAtk.igniteForSeconds(4);
                EntityHelper.getPersistentData(catAtk).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 80L);
                ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, catAtk, 1);
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithCataclysmMateriaAdvantage(level, catAtk, entity, 3.8F, false, false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // 8. #54 Terminus Shroud, Glacial Mantle, Flash-Slide Fall Immunity & Polar Vortex Orbit
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_FLASH_SLIDE_FALL_IMMUNE_UNTIL).orElse(0L)
                && source.is(DamageTypeTags.IS_FALL)) {
            entity.resetFallDistance();
            return EventResult.interruptFalse();
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_SHROUD_UNTIL).orElse(0L)) {
            if (source.is(DamageTypeTags.IS_FIRE)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)
                    || source.is(DamageTypeTags.IS_DROWNING)
                    || source.is(DamageTypeTags.IS_FREEZING)
                    || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)) {
                entity.clearFire();
                entity.setTicksFrozen(0);
                syncEntityBarrierState(level, entity, computeHitFlare(entity, amount), resolveHitOrigin(entity, source));
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity termAtk && termAtk != entity && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                ddraig.net.entropica.spell.mutation.element.FrozenSpellMutationHandler.applyIceStasis(level, entity, termAtk, 40, 0, null, null);
                applyTimedAttackDebuff(termAtk, level, 0.0D, 0.60D, 80L);
            }
            if (!REENTRANT_GUST_HURT.get() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                syncEntityBarrierState(level, entity, computeHitFlare(entity, amount), resolveHitOrigin(entity, source));
                REENTRANT_GUST_HURT.set(true);
                try {
                    entity.hurtServer(level, source, amount * 0.50F);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
        } else if (now <= data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_MANTLE_UNTIL).orElse(0L)
                && (source.is(DamageTypeTags.IS_FIRE)
                || source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
                || source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)
                || source.is(DamageTypeTags.IS_DROWNING)
                || source.is(DamageTypeTags.IS_FREEZING))) {
            entity.clearFire();
            entity.setTicksFrozen(0);
            syncEntityBarrierState(level, entity, computeHitFlare(entity, amount), resolveHitOrigin(entity, source));
            return EventResult.interruptFalse();
        }
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_TERMINUS_ORBIT_UNTIL).orElse(0L)) {
            Entity orbitAtk = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            if (orbitAtk instanceof LivingEntity termOrbitAtk && termOrbitAtk != entity && termOrbitAtk.isAlive()) {
                applyPercentageArmorShred(termOrbitAtk, level, 0.0D, 0.40D, 120L);
                applyTimedAttackDebuff(termOrbitAtk, level, 0.0D, 0.40D, 80L);
                termOrbitAtk.setTicksFrozen(Math.min(termOrbitAtk.getTicksRequiredToFreeze() + 80, termOrbitAtk.getTicksFrozen() + 60));
                ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, termOrbitAtk, 1);
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithTerminusMateriaAdvantage(level, termOrbitAtk, entity, 3.8F, false, true, false);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // 9. #55 Eschaton, #56 Apotheosis & #57 Entropica Singularity Mantles (Environmental & Void Immunity) + #55 Event Horizon Orbit Retaliation
        if ((now <= data.getLong(SpellLingeringZoneManager.TAG_ESCHATON_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL).orElse(0L))
                && (source.is(DamageTypeTags.IS_FIRE)
                || source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
                || source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)
                || source.is(DamageTypeTags.IS_DROWNING)
                || source.is(DamageTypeTags.IS_FREEZING)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)
                || source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD))) {
            entity.clearFire();
            entity.setTicksFrozen(0);
            entity.resetFallDistance();
            syncEntityBarrierState(level, entity, computeHitFlare(entity, amount), resolveHitOrigin(entity, source));
            return EventResult.interruptFalse();
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_ESCHATON_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL).orElse(0L)) {
            syncEntityBarrierState(level, entity, computeHitFlare(entity, amount), resolveHitOrigin(entity, source));
        }
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_ESCHATON_ORBIT_UNTIL).orElse(0L)) {
            Entity orbitAtk = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            if (orbitAtk instanceof LivingEntity eschAtk && eschAtk != entity && eschAtk.isAlive()) {
                applyPercentageArmorShred(eschAtk, level, 0.0D, 0.50D, 140L);
                ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, eschAtk, 1);
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithEschatonMateriaAdvantage(level, eschAtk, entity, 4.5F, false, false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_ORBIT_UNTIL).orElse(0L)) {
            Entity orbitAtk = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            if (orbitAtk instanceof LivingEntity apoAtk && apoAtk != entity && apoAtk.isAlive()) {
                applyPercentageArmorShred(apoAtk, level, 0.0D, 0.50D, 140L);
                apoAtk.igniteForSeconds(4);
                ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, apoAtk, 1);
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithApotheosisMateriaAdvantage(level, apoAtk, entity, 4.5F, false, false, true, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_ENTROPICA_ORBIT_UNTIL).orElse(0L)) {
            Entity orbitAtk = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            if (orbitAtk instanceof LivingEntity entAtk && entAtk != entity && entAtk.isAlive()) {
                applyPercentageArmorShred(entAtk, level, 0.0D, 0.50D, 140L);
                ddraig.net.entropica.entity.projectile.SpellProjectileEntity.releaseOrbitingProjectilesToward(level, entity, entAtk, 1);
                REENTRANT_GUST_HURT.set(true);
                try {
                    hurtWithEntropicaMateriaAdvantage(level, entAtk, entity, 4.8F, false, false, true);
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
            }
        }

        // 10. Player Hex Vectors (Monster Empowerment: #48 Aether, #54 Terminus, #56 Apotheosis, #57 Entropica)
        if (!REENTRANT_GUST_HURT.get() && entity instanceof Player hexedPlayer && source.getEntity() instanceof Monster mob) {
            if (hasActiveMutatedHex(data, now, EssenceType.AETHER)
                    && (!mob.onGround() || mob.getDeltaMovement().horizontalDistanceSqr() > 0.015D)) {
                data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 40L);
                level.sendParticles(
                        ddraig.net.entropica.client.particle.TimedTintableParticleOption.linear(
                                ddraig.net.entropica.registry.ModParticles.TINTABLE_SHIELD.get(), 0xA88654, 22, 1.0F, 0.05F),
                        hexedPlayer.getX(), hexedPlayer.getY(0.55D), hexedPlayer.getZ(), 8, 0.35D, 0.20D, 0.35D, 0.04D
                );
                level.playSound(null, hexedPlayer.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.45F, 0.65F);
                REENTRANT_GUST_HURT.set(true);
                try {
                    hexedPlayer.hurtServer(level, source, amount * (1.0F + 0.50F * getHexStackMultiplier(hexedPlayer)));
                } finally {
                    REENTRANT_GUST_HURT.set(false);
                }
                return EventResult.interruptFalse();
            }
            if (hasActiveMutatedHex(data, now, EssenceType.TERMINUS)) {
                if (source.getDirectEntity() == mob && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                    hexedPlayer.setTicksFrozen(Math.max(hexedPlayer.getTicksRequiredToFreeze() + 60, hexedPlayer.getTicksFrozen() + 80));
                    level.sendParticles(ParticleTypes.SNOWFLAKE, hexedPlayer.getX(), hexedPlayer.getY(0.8D), hexedPlayer.getZ(), 10, 0.3D, 0.35D, 0.3D, 0.03D);
                } else {
                    hexedPlayer.setTicksFrozen(Math.min(hexedPlayer.getTicksRequiredToFreeze() + 80, hexedPlayer.getTicksFrozen() + 60));
                }
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

    private static boolean hurtWithAdvantageTracked(
            ServerLevel level,
            LivingEntity target,
            DamageSource magicSrc,
            float spellDamage
    ) {
        boolean prev = IN_ADVANTAGE_HURT.get();
        IN_ADVANTAGE_HURT.set(true);
        try {
            return hurtWithPureMateria(level, target, magicSrc, spellDamage);
        } finally {
            IN_ADVANTAGE_HURT.set(prev);
        }
    }

    private static void cookFreshCauterizingDeathDrops(ServerLevel level, AABB box) {
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (!itemEntity.isAlive() || itemEntity.getAge() > 4) {
                continue;
            }
            ItemStack in = itemEntity.getItem();
            if (in.isEmpty()) {
                continue;
            }
            ItemStack oreSmelted = ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.trySmeltRawOreInTransit(level, in);
            if (oreSmelted != in) {
                itemEntity.setItem(oreSmelted);
                level.sendParticles(ParticleTypes.FLAME, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                        6, 0.12D, 0.12D, 0.12D, 0.02D);
                continue;
            }
            ItemStack smelted = ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.trySmeltStack(level, in);
            if (smelted != in && (in.has(net.minecraft.core.component.DataComponents.FOOD)
                    || smelted.has(net.minecraft.core.component.DataComponents.FOOD))) {
                itemEntity.setItem(smelted);
                level.sendParticles(ParticleTypes.FLAME, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                        6, 0.12D, 0.12D, 0.12D, 0.02D);
            }
        }
    }

    /**
     * Converts a Touch/Strike spell hit's attacking Materia cleanly to {@code EARTH}, {@code FROZEN}, {@code NETHER},
     * or {@code RADIANT} when the Touch form is mutated with one of those elements (without stacking a second element's advantage).
     */
    @Nullable
    public static EssenceType resolveConvertedTouchAttackMateria(
            @Nullable EssenceType baseEffectMateria,
            @Nullable EssenceType touchFormMateria,
            boolean touchFormMutated
    ) {
        if (touchFormMutated && (touchFormMateria == EssenceType.EARTH
                || touchFormMateria == EssenceType.FROZEN
                || touchFormMateria == EssenceType.NETHER
                || touchFormMateria == EssenceType.RADIANT)) {
            return touchFormMateria;
        }
        return baseEffectMateria;
    }

    /**
     * Section 6.8 <i>Sun-Scorched Smite Hex</i> physical armor melt across Hex Levels I–III ({@code 4.0 / 6.0 / 8.0}).
     */
    public static double resolveSunScorchedHexArmorMelt(int stack) {
        return switch (Math.clamp(stack, 1, 3)) {
            case 1 -> 4.0D;
            case 2 -> 6.0D;
            default -> 8.0D;
        };
    }

    /**
     * Section 6.5 <i>Sundering Hex</i> physical armor shred across Hex Levels I–III ({@code 5.0 / 7.5 / 10.0}).
     */
    public static double resolveSunderingHexArmorShred(int stack) {
        return switch (Math.clamp(stack, 1, 3)) {
            case 1 -> 5.0D;
            case 2 -> 7.5D;
            default -> 10.0D;
        };
    }

    /**
     * Section 6.7 <i>Hellfire Sear Hex</i> physical armor reduction fraction across Hex Levels I–III ({@code 0.30 / 0.45 / 0.60}).
     */
    public static double resolveHellfireSearArmorMeltFraction(int stack) {
        return switch (Math.clamp(stack, 1, 3)) {
            case 1 -> 0.30D;
            case 2 -> 0.45D;
            default -> 0.60D;
        };
    }

    /**
     * Returns {@code true} if {@code target} currently has active Frozen Ice Stasis encasement immunity against {@code IN_WALL} suffocation.
     */
    public static boolean hasFrozenIceSuffocationImmunity(LivingEntity target, long now) {
        CompoundTag data = EntityHelper.getPersistentData(target);
        return now <= data.getLong(SpellLingeringZoneManager.TAG_FROZEN_ICE_STASIS_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L);
    }

    /**
     * Section 6.6 <i>Glacial Spikes Retaliation</i> proportional {@code +30%} slower melee swing interval for chilled Mobs.
     */
    public static long resolveChilledMobSwingCooldownTicks(long baseIntervalTicks) {
        return Math.max(1L, Math.round(Math.max(1L, baseIntervalTicks) * 1.30D));
    }

    /**
     * Section 6.6 <i>Permafrost Stasis Hex</i> shatter damage multiplier across Hex Levels I–III ({@code 1.50x / 1.75x / 2.00x}).
     */
    public static float resolveFrozenHexShatterMultiplier(int stack) {
        return switch (Math.clamp(stack, 1, 3)) {
            case 1 -> 1.50F;
            case 2 -> 1.75F;
            default -> 2.00F;
        };
    }

    /**
     * Returns {@code true} if {@code key} is a blunt/physical melee, Mace smash, or kinetic fall/collision damage type
     * eligible for the <i>Permafrost Stasis Hex</i> shatter bonus.
     */
    public static boolean isFrozenHexShatterDamageType(
            @Nullable net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> key
    ) {
        if (key == null) {
            return false;
        }
        return key == net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK
                || key == net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK
                || key == net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK_NO_AGGRO
                || key == net.minecraft.world.damagesource.DamageTypes.MACE_SMASH
                || key == net.minecraft.world.damagesource.DamageTypes.FALL
                || key == net.minecraft.world.damagesource.DamageTypes.FLY_INTO_WALL
                || key == net.minecraft.world.damagesource.DamageTypes.STALAGMITE
                || key == net.minecraft.world.damagesource.DamageTypes.FALLING_BLOCK
                || key == net.minecraft.world.damagesource.DamageTypes.FALLING_ANVIL
                || key == net.minecraft.world.damagesource.DamageTypes.FALLING_STALACTITE;
    }

    /**
     * Returns {@code true} if {@code source} triggers the <i>Permafrost Stasis Hex</i> shatter bonus
     * (strictly blunt/physical melee, Mace smash, or fall/kinetic collision—excluding projectiles, drowning, poison, starvation, etc.).
     */
    public static boolean isFrozenHexShatterDamage(DamageSource source) {
        if (source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypeTags.WITCH_RESISTANT_TO)
                || source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_LIGHTNING)
                || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.is(DamageTypeTags.IS_FREEZING)
                || source.is(DamageTypeTags.IS_DROWNING)) {
            return false;
        }
        var keyOpt = source.typeHolder().unwrapKey();
        if (keyOpt.isPresent()) {
            return isFrozenHexShatterDamageType(keyOpt.get());
        }
        return source.getDirectEntity() instanceof LivingEntity;
    }

    /**
     * Returns {@code true} if incoming Materia spell damage counts as Earth/crush damage for <i>Permafrost Stasis Hex</i> shatter.
     */
    public static boolean isFrozenHexShatterMateria(@Nullable EssenceType attackMateria) {
        return attackMateria != null && MateriaFamilyTier.isEarthLineage(attackMateria);
    }

    /**
     * Applies pure Materia spell damage via {@link LivingEntity#hurtServer}, compensating for vanilla
     * {@link net.minecraft.world.entity.monster.Witch} 85% potion-splash magic absorption ({@code * 0.15F})
     * so Entropica spells deal their true Tier 1–10 HP while still honoring Resistance & Protection enchantments!
     */
    public static boolean hurtWithPureMateria(
            ServerLevel level,
            LivingEntity target,
            DamageSource magicSrc,
            float spellDamage
    ) {
        float adjusted = target instanceof net.minecraft.world.entity.monster.Witch
                ? (spellDamage / 0.15F)
                : spellDamage;
        return target.hurtServer(level, magicSrc, Math.max(0.5F, adjusted));
    }

    /**
     * Section 6.4 Pure Nature Materia Damage Helper:
     * Applies true Nature Materia spell damage routing through canonical Nature Materia Advantage
     * ({@code +1 vs Water & Earth}, {@code -1 vs Nether, Frozen & Umbral}), Armor Shred amplification,
     * Elemental Reactions, and Elemental Hex Counter-Cleansing. Returns actual damage dealt ({@code 0.0F} if negated).
     */
    public static float hurtWithNatureMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage
    ) {
        return hurtWithNatureMateriaAdvantage(level, target, caster, baseDamage, true);
    }

    public static float hurtWithNatureMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.NATURE, caster, target, defenderAffs, 0.0F
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.NATURE);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        if (now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)) {
            int shredPts = targetData.getInt(TAG_ARMOR_SHRED).orElse(0);
            if (shredPts > 0) {
                damage *= (1.0F + 0.04F * Math.min(8, shredPts));
            }
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithAdvantageTracked(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.NATURE, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.NATURE);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Section 6.5 Pure Earth Materia Damage Helper:
     * Applies true Earth Materia spell damage including canonical Earth Advantage ({@code +1 vs Void}, {@code -1 vs Air}),
     * optional <i>Dense Tectonic Mass</i> ({@code +0.4} advantage &amp; {@code 3.0 + 0.35 * tier} armor shred for {@code 160t}),
     * <i>Sundering Hex</i> exact {@code 2.0x} Earth damage doubling, Armor Shred amplification, and Elemental Hex Counter-Cleansing.
     */
    public static boolean hurtWithEarthMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean denseTectonicMass,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return false;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return false;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = denseTectonicMass ? 0.40F : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.EARTH, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return false;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        boolean sunderingHexActive = hasActiveMutatedHex(targetData, now, EssenceType.EARTH);
        if (sunderingHexActive) {
            damage *= 2.0F; // Section 6.5 Sundering Hex: exact 2.0x incoming Earth Materia damage (no extra 1.20x shred stack)!
        }
        if (hasActiveMutatedHex(targetData, now, EssenceType.FROZEN)) {
            damage *= resolveFrozenHexShatterMultiplier(getHexStackLevel(target));
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.EARTH);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        if (!sunderingHexActive && now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)) {
            int shredPts = targetData.getInt(TAG_ARMOR_SHRED).orElse(0);
            if (shredPts > 0) {
                damage *= (1.0F + 0.04F * Math.min(8, shredPts));
            }
        }

        if (denseTectonicMass) {
            int shredTier = resolveSpellOrWeaponTier(caster, 1);
            applyTimedAttributeShred(target, level, 3.0D + shredTier * 0.35D, 0.0D, 160L);
        }

        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithAdvantageTracked(level, target, magicSrc, Math.max(0.5F, damage));
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            checkElementalHexCounterCleanse(target, EssenceType.EARTH);
        }
        return hurt;
    }

    /**
     * Section 6.6 Pure Frozen Materia Damage Helper:
     * Applies true Frozen Materia spell damage including canonical Frozen Advantage ({@code +1 vs Water},
     * {@code -1 vs Nether & Undead}), optional <i>Permafrost Creep</i> ({@code +0.4} advantage strictly against Water targets),
     * Armor Shred amplification, and Elemental Hex Counter-Cleansing.
     */
    public static boolean hurtWithFrozenMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean permafrostCreep,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return false;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return false;
        }

        float damage = baseDamage;
        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        boolean isWaterTarget = defenderAffs.contains(EssenceType.WATER)
                || SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)
                || hasActiveMutatedHex(targetData, now, EssenceType.WATER)
                || (target instanceof ddraig.net.entropica.entity.SpellEidolonEntity eid && eid.getEffectiveMateria() == EssenceType.WATER);
        float bonusScore = (permafrostCreep && isWaterTarget) ? 0.40F : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.FROZEN, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return false;
        }
        damage *= advMult;

        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.7 Immolation Shroud already registers NETHER in defenderAffs (exact 0.50x); clear any frost ticks!
            target.setTicksFrozen(0);
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.FROZEN);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        if (now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)) {
            int shredPts = targetData.getInt(TAG_ARMOR_SHRED).orElse(0);
            if (shredPts > 0) {
                damage *= (1.0F + 0.04F * Math.min(8, shredPts));
            }
        }

        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithAdvantageTracked(level, target, magicSrc, Math.max(0.5F, damage));
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.FROZEN, damage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.FROZEN);
        }
        return hurt;
    }

    /**
     * Deals pure <b>Nether Materia</b> damage (Section 6.7: +1 vs Frozen, -1 vs Water) with full
     * Materia armor resistance, Frozen/Water shroud interactions, and elemental hex counter-cleanse.
     */
    public static boolean hurtWithNetherMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean bypassSpellHitWindow
    ) {
        return hurtWithNetherMateriaAdvantage(level, target, caster, baseDamage, bypassSpellHitWindow, false);
    }

    public static boolean hurtWithNetherMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean bypassSpellHitWindow,
            boolean includeHeldWeaponSynergy
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return false;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return false;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.NETHER, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return false;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.NETHER);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        if (now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)) {
            int shredPts = targetData.getInt(TAG_ARMOR_SHRED).orElse(0);
            if (shredPts > 0) {
                damage *= (1.0F + 0.04F * Math.min(8, shredPts));
            }
        }

        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithAdvantageTracked(level, target, magicSrc, Math.max(0.5F, damage));
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.NETHER, damage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.NETHER);
        }
        return hurt;
    }

    /**
     * Guide Section 6.8 Radiant Materia Damage Helper:
     * Applies pure Radiant Materia damage respecting canonical Radiant Materia Advantage
     * ({@code +1} vs <b>UNDEAD</b>, {@code -1} vs <b>UMBRAL</b>), plus held Materia weapon synergy on melee strikes!
     */
    public static boolean hurtWithRadiantMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy
    ) {
        return hurtWithRadiantMateriaAdvantage(level, target, caster, baseDamage, includeHeldWeaponSynergy, false);
    }

    public static boolean hurtWithRadiantMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return false;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return false;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.RADIANT, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return false;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL).orElse(0L)) {
            damage *= 1.50F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)) {
            damage *= 0.67F; // Combined with -1 Umbral affinity (0.75 * 0.67 = 0.50x)!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.RADIANT);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        if (now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)) {
            int shredPts = targetData.getInt(TAG_ARMOR_SHRED).orElse(0);
            if (shredPts > 0) {
                damage *= (1.0F + 0.04F * Math.min(8, shredPts));
            }
        }

        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithAdvantageTracked(level, target, magicSrc, Math.max(0.5F, damage));
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.RADIANT, damage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.RADIANT);
        }
        return hurt;
    }

    /**
     * Guide Section 6.9 Umbral Materia Damage Helper:
     * Applies pure Umbral Materia damage respecting canonical Umbral Materia Advantage
     * ({@code +1} vs <b>RADIANT</b>, {@code -1} vs <b>VOID</b>), plus held Materia weapon synergy on melee strikes!
     */
    public static boolean hurtWithUmbralMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy
    ) {
        return hurtWithUmbralMateriaAdvantage(level, target, caster, baseDamage, includeHeldWeaponSynergy, false);
    }

    public static boolean hurtWithUmbralMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return false;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return false;
        }

        // Guide Section 1.7 Umbral Stealth Assassination:
        // If striking an unaware mob, record surrounding unaware pack mates so the strike emits ZERO pack alert calls!
        boolean targetWasUnaware = target instanceof Mob targetMob && targetMob.getTarget() == null;
        List<Mob> unawarePackMates = List.of();
        if (targetWasUnaware) {
            unawarePackMates = level.getEntitiesOfClass(
                    Mob.class,
                    target.getBoundingBox().inflate(24.0D),
                    m -> m != target && m.isAlive() && m.getTarget() == null && m.getLastHurtByMob() == null
            );
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.UMBRAL, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return false;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.UMBRAL);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        if (now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)) {
            int shredPts = targetData.getInt(TAG_ARMOR_SHRED).orElse(0);
            if (shredPts > 0) {
                damage *= (1.0F + 0.04F * Math.min(8, shredPts));
            }
        }

        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithAdvantageTracked(level, target, magicSrc, Math.max(0.5F, damage));
        if (hurt) {
            if (targetWasUnaware && !unawarePackMates.isEmpty()) {
                for (Mob packMate : unawarePackMates) {
                    if (packMate.isAlive()) {
                        packMate.setTarget(null);
                        packMate.setLastHurtByMob(null);
                        packMate.setAggressive(false);
                        packMate.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
                        packMate.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT);
                        if (packMate instanceof NeutralMob neutral) {
                            neutral.stopBeingAngry();
                            neutral.setPersistentAngerTarget(null);
                        }
                    }
                }
            }
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.UMBRAL, damage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.UMBRAL);
        }
        return hurt;
    }

    /**
     * Guide Section 6.10 Undead Materia Damage Helper:
     * Applies pure Undead Materia damage respecting canonical Undead Materia Advantage
     * ({@code +1} vs <b>FROZEN</b>, {@code -1} vs <b>RADIANT</b>), optional <i>Creeping Tomb-Dust</i>
     * ({@code +0.4} advantage), held Materia weapon synergy on melee strikes, and physical shield bypass
     * for ghostly aerial wraith dive-bombs. Returns the actual damage dealt ({@code 0.0F} if negated).
     */
    public static float hurtWithUndeadMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean creepingTombDust,
            boolean includeHeldWeaponSynergy,
            boolean bypassPhysicalShield
    ) {
        return hurtWithUndeadMateriaAdvantage(
                level, target, caster, baseDamage, creepingTombDust, includeHeldWeaponSynergy, bypassPhysicalShield, false
        );
    }

    public static float hurtWithUndeadMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean creepingTombDust,
            boolean includeHeldWeaponSynergy,
            boolean bypassPhysicalShield,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        if (bypassPhysicalShield && target.isBlocking()) {
            if (target instanceof Player blockingPlayer) {
                ItemStack shield = blockingPlayer.getUseItem();
                blockingPlayer.stopUsingItem();
                if (!shield.isEmpty()) {
                    blockingPlayer.getCooldowns().addCooldown(shield, 80);
                }
            } else {
                target.stopUsingItem();
            }
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (creepingTombDust ? 0.40F : 0.0F)
                + ((includeHeldWeaponSynergy && caster != null) ? resolveHeldWeaponAdvantageScore(caster, target) : 0.0F);
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.UNDEAD, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.UNDEAD);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        if (now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)) {
            int shredPts = targetData.getInt(TAG_ARMOR_SHRED).orElse(0);
            if (shredPts > 0) {
                damage *= (1.0F + 0.04F * Math.min(8, shredPts));
            }
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithAdvantageTracked(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.UNDEAD, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.UNDEAD);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.11 Void Materia Damage Helper:
     * Applies pure Void Materia damage respecting canonical Void Materia Advantage
     * ({@code +1} vs <b>AIR</b>, {@code -1} vs <b>EARTH</b>), {@code +50%} bonus damage against
     * victims afflicted by <i>Spacetime Anchoring Curse</i>, held Materia weapon synergy on melee strikes,
     * and armor-ignoring Void tear scaling ({@code armorPierceFraction}) for <i>Spatial Destabilization Strike</i>
     * and <i>Void-Stalker</i> claw attacks.
     */
    public static float hurtWithVoidMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            float armorPierceFraction,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.VOID, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        // Armor-ignoring spatial tear bonus against heavily armored enemies!
        if (armorPierceFraction > 0.0F) {
            int armorPts = target.getArmorValue();
            if (armorPts > 0) {
                damage += Math.min(8.0F, armorPts * 0.22F * armorPierceFraction);
                applyTimedAttributeShred(target, level, Math.max(2.0D, armorPts * armorPierceFraction), 0.0D, 80L);
            }
        }

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (hasActiveMutatedHex(targetData, now, EssenceType.VOID)) {
            // Section 6.11 Spacetime Anchoring Curse: +50% incoming Void damage!
            damage *= 1.50F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)) {
            damage *= 0.50F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_AIR_SLIPSTREAM_UNTIL).orElse(0L)) {
            damage *= 1.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.VOID);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        if (now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)) {
            int shredPts = targetData.getInt(TAG_ARMOR_SHRED).orElse(0);
            if (shredPts > 0) {
                damage *= (1.0F + 0.04F * Math.min(8, shredPts));
            }
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.VOID, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.VOID);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.12 Arid Materia Damage Helper:
     * Applies pure Arid Materia damage respecting the Mutually Volatile Clash against <b>WATER (+1)</b>
     * and soaked targets (instantly flash-drying {@code TAG_SOAKED_UNTIL} on hit!), plus held Materia
     * weapon synergy on melee strikes.
     */
    public static float hurtWithAridMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (!defenderAffs.contains(EssenceType.WATER) && SpellLingeringZoneManager.isEntityWetOrSoaked(target) ? 1.0F : 0.0F)
                + ((includeHeldWeaponSynergy && caster != null) ? resolveHeldWeaponAdvantageScore(caster, target) : 0.0F);
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.ARID, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)) {
            damage *= 1.25F; // Mutually volatile clash against Oceanic Shroud!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.ARID);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        if (now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)) {
            int shredPts = targetData.getInt(TAG_ARMOR_SHRED).orElse(0);
            if (shredPts > 0) {
                damage *= (1.0F + 0.04F * Math.min(8, shredPts));
            }
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            // Desiccating Arid strike flash-dries surface soaking!
            if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_SOAKED_UNTIL).orElse(0L)) {
                targetData.remove(SpellLingeringZoneManager.TAG_SOAKED_UNTIL);
                level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(0.75D), target.getZ(),
                        6, 0.25D, 0.30D, 0.25D, 0.03D);
            }
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.ARID, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.ARID);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Lightning Materia (#12)</b> spell damage:
     * <ul>
     *     <li>Strong against <b>EARTH (+1)</b> and <b>WATER (+1)</b> | Weak against <b>VOID (-1)</b></li>
     *     <li><b>Conductive Overcharge (Guide Sec 1.2B)</b>: wet/soaked targets or targets with
     *         <i>Waterlogged Drowning Hex</i> take {@code +100%} bonus ({@code 2.0x}) Lightning damage!</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     *     <li>Triggers Elemental Reactions ({@code Conductive Overcharge}, {@code Gravitational Collapse}) and Hex Counter-Cleansing</li>
     * </ul>
     */
    public static float hurtWithLightningMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.LIGHTNING, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        // Guide Sec 1.2B & 6.15 Conductive Overcharge / Waterlogged Hypoxia Hex: wet/soaked/hypoxia targets take +100% bonus (2.0x) Lightning damage!
        if (SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                || hasActiveMutatedHex(targetData, now, EssenceType.WATER)
                || hasActiveMutatedHex(targetData, now, EssenceType.STORM)) {
            damage *= 2.0F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)) {
            damage *= 1.25F; // Mutually volatile clash vs Oceanic Shroud!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L)) {
            damage *= 0.67F; // Weak against Void!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.LIGHTNING);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        if (now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)) {
            int shredPts = targetData.getInt(TAG_ARMOR_SHRED).orElse(0);
            if (shredPts > 0) {
                damage *= (1.0F + 0.04F * Math.min(8, shredPts));
            }
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.LIGHTNING, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.LIGHTNING);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Magma Materia (#13 — Tier 1 Fusion: Earth + Nether)</b> spell damage:
     * <ul>
     *     <li>Strong against <b>FROZEN (+1)</b> and <b>NATURE (+1)</b> | Weak against <b>AIR (-1)</b> and <b>WATER (-1)</b>
     *         (Dominance: {@code magma_earth_dom} computed automatically by {@code VisInteractionManager})</li>
     *     <li>Tier 1 Elemental Fusion lineage traits: {@code 10%} physical armor bypass &amp; {@code 1} base armor shred point</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     *     <li>Triggers Elemental Reactions ({@code Thermal Shock}, {@code Atmospheric Eruption}, {@code Evaporative Scald}, {@code Molten Slag})
     *         and Hex Counter-Cleansing</li>
     * </ul>
     */
    public static float hurtWithMagmaMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.MAGMA, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L)) {
            damage *= 1.50F; // Strong against Frozen & Nature shrouds!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_AIR_SLIPSTREAM_UNTIL).orElse(0L)) {
            damage *= 0.50F; // Weak against Air & Water shrouds!
        }
        if (hasActiveMutatedHex(targetData, now, EssenceType.EARTH)) {
            damage *= 2.0F; // Parent Earth-lineage synergy with Sundering Hex!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.MAGMA);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        // Tier 1 Elemental Fusion: stamp 1 base armor shred point & apply existing shred bonus
        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ELEMENTAL_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.MAGMA, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.MAGMA);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Storm Materia (#14 — Tier 1 Fusion: Air + Water)</b> spell damage:
     * <ul>
     *     <li>Strong against <b>EARTH (+1)</b> and <b>GLACIAL (+1)</b> | Weak against <b>NATURE (-1)</b>
     *         (Dominance: {@code storm_air_dom} computed automatically by {@code VisInteractionManager})</li>
     *     <li>Tier 1 Elemental Fusion lineage traits: {@code 10%} physical armor bypass &amp; {@code 1} base armor shred point</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     *     <li>Triggers Elemental Reactions ({@code Atmospheric Eruption}, {@code Evaporative Scald}, {@code Conductive Overcharge})
     *         and Hex Counter-Cleansing</li>
     * </ul>
     */
    public static float hurtWithStormMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.STORM, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL).orElse(0L)) {
            damage *= 1.50F; // Strong against Earth & Glacial shrouds!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BASTION_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L)) {
            damage *= 0.50F; // Weak against Nature & Void shrouds!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.STORM);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        // Tier 1 Elemental Fusion: stamp 1 base armor shred point & apply existing shred bonus
        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ELEMENTAL_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.STORM, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.STORM);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Glacial Materia (#15 — Tier 1 Fusion: Water + Frozen)</b> spell damage:
     * <ul>
     *     <li>Strong against <b>NETHER (+1)</b>, <b>ARID (+1)</b>, and <b>NATURE (+1)</b> |
     *         Weak against <b>NATURE (-1)</b>, <b>NETHER (-1)</b>, and <b>UNDEAD (-1)</b>
     *         (Dominance: {@code glacial_water_dom} -> Strong vs Arid, Weak vs Nature &amp; Undead; Nether neutralized,
     *         computed automatically by {@code VisInteractionManager})</li>
     *     <li>Optional <i>Permafrost Creep</i> ({@code +0.4} Materia Advantage score &amp; Slow/Heavy bonus)</li>
     *     <li>Tier 1 Elemental Fusion lineage traits: {@code 10%} physical armor bypass &amp; {@code 1} base armor shred point</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     *     <li>Triggers Elemental Reactions ({@code Thermal Shock}, {@code Flash-Freeze}, {@code Superconduct})
     *         and Hex Counter-Cleansing</li>
     * </ul>
     */
    public static float hurtWithGlacialMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean permafrostCreep,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (permafrostCreep ? 0.40F : 0.0F)
                + ((includeHeldWeaponSynergy && caster != null) ? resolveHeldWeaponAdvantageScore(caster, target) : 0.0F);
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.GLACIAL, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_ARID_VEIL_UNTIL).orElse(0L)) {
            damage *= 1.50F; // Strong against Arid shrouds!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BASTION_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_STORM_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)) {
            damage *= 0.50F; // Weak against Nature, Undead, Storm & Magma shrouds!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.GLACIAL);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        // Tier 1 Elemental Fusion: stamp 1 base armor shred point & apply existing shred bonus
        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ELEMENTAL_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.GLACIAL, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.GLACIAL);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Overgrowth Materia (#16 — Tier 1 Fusion: Nature + Earth)</b> spell damage:
     * <ul>
     *     <li>Strong against <b>WATER (+1)</b> and <b>EARTH (+1)</b> |
     *         Weak against <b>NETHER (-1)</b>, <b>FROZEN (-1)</b>, <b>UMBRAL (-1)</b>, and <b>AIR (-1)</b>
     *         (Dominance: {@code overgrowth_earth_dom} -&gt; Strong vs Void, Weak vs Nether, Frozen, Umbral &amp; Air,
     *         computed automatically by {@code VisInteractionManager})</li>
     *     <li>Tier 1 Elemental Fusion lineage traits: {@code 10%} physical armor bypass &amp; {@code 1} base armor shred point</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     *     <li>Triggers Elemental Reactions and Hex Counter-Cleansing</li>
     * </ul>
     */
    public static float hurtWithOvergrowthMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.OVERGROWTH, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L)) {
            damage *= 1.50F; // Strong against Water, Earth & Void shrouds!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_AIR_SLIPSTREAM_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_STORM_SHROUD_UNTIL).orElse(0L)) {
            damage *= 0.50F; // Weak against Nether, Frozen, Umbral & Air shrouds!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.OVERGROWTH);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        // Tier 1 Elemental Fusion: stamp 1 base armor shred point & apply existing shred bonus
        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ELEMENTAL_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.OVERGROWTH, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.OVERGROWTH);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Dust Materia (#17 — Tier 1 Fusion: Air + Earth)</b> spell damage:
     * <ul>
     *     <li>Strong against <b>WATER (+1)</b>, <b>VOID (+1)</b>, and <b>VAPOR (+1)</b> |
     *         Weak against <b>WATER (-1)</b> and <b>VOID (-1)</b>
     *         (Dominance: {@code dust_air_dom} -&gt; Strong vs Earth, Weak vs Water &amp; Void,
     *         computed automatically by {@code VisInteractionManager})</li>
     *     <li>Tier 1 Elemental Fusion lineage traits: {@code 10%} physical armor bypass &amp; {@code 1} base armor shred point</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     *     <li>Triggers Elemental Reactions and Hex Counter-Cleansing</li>
     * </ul>
     */
    public static float hurtWithDustMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.DUST, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)) {
            damage *= 1.50F; // Mutually volatile / Strong against Water, Void & Earth shrouds!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.DUST);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        // Tier 1 Elemental Fusion: stamp 1 base armor shred point & apply existing shred bonus
        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ELEMENTAL_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.DUST, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.DUST);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Spore Materia (#18 — Tier 1 Fusion: Nature + Air)</b> spell damage:
     * <ul>
     *     <li>Dual Nature + Air harmonic matrix: Strong against <b>WATER (+1)</b> and <b>EARTH (+1)</b> |
     *         Weak against <b>NETHER (-1)</b>, <b>FROZEN (-1)</b>, <b>UMBRAL (-1)</b>, and <b>VOID (-1)</b>
     *         (Dominance: {@code spore_air_dom} -&gt; Strong vs Earth, Weak vs Nether, Frozen, Umbral &amp; Void,
     *         computed dynamically at runtime by {@code VisInteractionManager})</li>
     *     <li>Tier 1 Elemental Fusion lineage traits: {@code 10%} physical armor bypass &amp; {@code 1} base armor shred point</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     *     <li>Triggers Elemental Reactions (including <i>Entangling Miasma</i>) and Hex Counter-Cleansing</li>
     * </ul>
     */
    public static float hurtWithSporeMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.SPORE, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)) {
            damage *= 1.50F; // Strong against Water & Earth shrouds!
        } else if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L)) {
            damage *= 0.50F; // Weak against Nether, Frozen & Void shrouds!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.SPORE);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        // Tier 1 Elemental Fusion: stamp 1 base armor shred point & apply existing shred bonus
        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ELEMENTAL_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.SPORE, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.SPORE);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Taiga Materia (#19 — Tier 1 Fusion: Nature + Frozen)</b> spell damage:
     * <ul>
     *     <li>Dual Nature + Frozen harmonic matrix: Strong against <b>WATER (+1)</b> and <b>NATURE (+1)</b> |
     *         Weak against <b>NETHER (-1)</b>, <b>UMBRAL (-1)</b>, and <b>UNDEAD (-1)</b>
     *         (Dominance: {@code taiga_nature_dom} -&gt; Strong vs Water &amp; Earth, Weak vs Nether, Umbral &amp; Undead,
     *         computed dynamically at runtime by {@code VisInteractionManager})</li>
     *     <li>Tier 1 Elemental Fusion lineage traits: {@code 10%} physical armor bypass &amp; {@code 1} base armor shred point</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     *     <li>Triggers Elemental Reactions and Hex Counter-Cleansing</li>
     * </ul>
     */
    public static float hurtWithTaigaMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.TAIGA, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BASTION_UNTIL).orElse(0L)) {
            damage *= 1.50F; // Strong against Water & Nature shrouds!
        } else if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL).orElse(0L)) {
            damage *= 0.50F; // Weak against Nether, Umbral & Undead shrouds!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.TAIGA);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        // Tier 1 Elemental Fusion: stamp 1 base armor shred point & apply existing shred bonus
        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ELEMENTAL_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.TAIGA, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.TAIGA);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Oasis Materia (#20 — Tier 1 Fusion: Nature + Arid)</b> spell damage:
     * <ul>
     *     <li>Dual Nature + Arid harmonic matrix: Strong against <b>WATER (+1)</b>, <b>EARTH (+1)</b>, and <b>DUST (+1)</b> |
     *         Weak against <b>NETHER (-1)</b> and <b>UMBRAL (-1)</b>
     *         (Dominance: {@code oasis_arid_dom} -&gt; Strong vs Water &amp; Void, Weak vs Nether &amp; Umbral,
     *         computed dynamically at runtime by {@code VisInteractionManager})</li>
     *     <li>Tier 1 Elemental Fusion lineage traits: {@code 10%} physical armor bypass &amp; {@code 1} base armor shred point</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     *     <li>Triggers Elemental Reactions and Hex Counter-Cleansing</li>
     * </ul>
     */
    public static float hurtWithOasisMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.OASIS, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_DUST_VEIL_UNTIL).orElse(0L)) {
            damage *= 1.50F; // Strong against Water, Earth & Dust shrouds!
        } else if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)) {
            damage *= 0.50F; // Weak against Nether & Umbral shrouds!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.OASIS);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        // Tier 1 Elemental Fusion: stamp 1 base armor shred point & apply existing shred bonus
        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ELEMENTAL_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.OASIS, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.OASIS);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Vitae Materia (#21 — Vital Fusion)</b> spell damage:
     * <ul>
     *     <li>Strong against <b>UNDEAD (+1)</b> | Weak against <b>NETHER (-1)</b>, <b>FROZEN (-1)</b>, and <b>UMBRAL (-1)</b>
     *         (Dominance: {@code vitae_nature_dom} -&gt; Strong vs Water &amp; Earth, Weak vs Nether, Frozen &amp; Umbral,
     *         computed dynamically at runtime by {@code VisInteractionManager})</li>
     *     <li>Vital Fusion lineage traits: {@code 15%} physical armor bypass &amp; {@code 1} base armor shred point</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     *     <li>Triggers Elemental Reactions and Hex Counter-Cleansing</li>
     * </ul>
     */
    public static float hurtWithVitaeMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.VITAE, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL).orElse(0L)) {
            damage *= 1.50F; // Strong against Undead shrouds!
        } else if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_TAIGA_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)) {
            damage *= 0.50F; // Weak against Nether, Frozen & Umbral shrouds!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.VITAE);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.VITAL_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.VITAE, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.VITAE);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Blood Materia (#22 — Vital Fusion: Vitae + Nether/Undead)</b> spell damage:
     * <ul>
     *     <li>Strong against <b>FROZEN (+1)</b>, <b>NATURE (+1)</b>, and <b>SPRING (+1)</b> |
     *         Weak against <b>RADIANT (-1)</b>, <b>NETHER (-1)</b>, and <b>UMBRAL (-1)</b>
     *         (Dominance: {@code blood_vitae_dom} -&gt; Strong vs Undead, Weak vs Radiant, Nether &amp; Umbral,
     *         computed dynamically at runtime by {@code VisInteractionManager})</li>
     *     <li>Vital Fusion lineage traits: {@code 15%} physical armor bypass &amp; {@code 1} base armor shred point</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     *     <li>Triggers Elemental Reactions and Hex Counter-Cleansing</li>
     * </ul>
     */
    public static float hurtWithBloodMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.BLOOD, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_TAIGA_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BASTION_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_SPORE_MANTLE_UNTIL).orElse(0L)) {
            damage *= 1.50F; // Strong against Frozen & Nature shrouds!
        } else if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)) {
            damage *= 0.50F; // Weak against Magma & Umbral shrouds (Radiant & Nether already applied via defenderAffs)!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.BLOOD);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.VITAL_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithAdvantageTracked(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.BLOOD, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.BLOOD);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Aura Materia (#23 — Vital Fusion: Vitae + Air)</b> spell damage:
     * <ul>
     *     <li>Strong against <b>UNDEAD (+1)</b> and <b>WRAITH (+1)</b> |
     *         Weak against <b>UMBRAL (-1)</b>, <b>VOID (-1)</b>, and <b>NETHER (-1)</b>
     *         (Dominance: {@code aura_air_dom} -&gt; Strong vs Earth, Weak vs Umbral, Void &amp; Nether,
     *         computed dynamically at runtime by {@code VisInteractionManager})</li>
     *     <li>Vital Fusion lineage traits: {@code 15%} physical armor bypass &amp; {@code 1} base armor shred point</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     *     <li>Triggers Elemental Reactions and Hex Counter-Cleansing</li>
     * </ul>
     */
    public static float hurtWithAuraMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.AURA, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL).orElse(0L)) {
            damage *= 1.50F; // Strong against Undead/Wraith shrouds!
        } else if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)) {
            damage *= 0.50F; // Weak against Umbral, Void & Nether shrouds!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.AURA);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.VITAL_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.AURA, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.AURA);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Amber Materia (#24 — Vital Fusion: Vitae + Earth)</b> spell damage:
     * <ul>
     *     <li>Strong against <b>VOID (+1)</b> and <b>BARROW (+1)</b> |
     *         Weak against <b>NETHER (-1)</b>, <b>FROZEN (-1)</b>, and <b>UMBRAL (-1)</b>
     *         (Dominance: {@code amber_vitae_dom} -&gt; Strong vs Undead, Weak vs Nether, Frozen &amp; Umbral,
     *         computed dynamically at runtime by {@code VisInteractionManager})</li>
     *     <li>Vital Fusion lineage traits: {@code 15%} physical armor bypass &amp; {@code 1} base armor shred point</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     *     <li>Triggers Elemental Reactions and Hex Counter-Cleansing</li>
     * </ul>
     */
    public static float hurtWithAmberMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.AMBER, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L)) {
            damage *= 1.50F; // Strong against Void shrouds!
        } else if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)) {
            damage *= 0.50F; // Weak against Nether, Frozen & Umbral shrouds!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.AMBER);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.VITAL_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.AMBER, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.AMBER);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Eclipse Materia (#25 — Ethereal &amp; Paradox Fusion: Radiant + Umbral [Leans: Umbral])</b> spell damage:
     * <ul>
     *     <li>Strong against <b>RADIANT (+1)</b> and <b>NATURE (+1)</b> (Dominance {@code eclipse_radiant_dom}: Strong vs Undead)
     *         | Weak against <b>VOID (-1)</b>, computed dynamically by {@code VisInteractionManager}</li>
     *     <li>Ethereal &amp; Paradox lineage traits: innate physical armor bypass &amp; base armor wear</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithEclipseMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.ECLIPSE, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L)) {
            damage *= 0.50F; // Weak against Void shrouds (Radiant & Nature already applied via defenderAffs)!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.ECLIPSE);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithAdvantageTracked(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.ECLIPSE, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.ECLIPSE);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Blight Materia (#26 — Ethereal &amp; Paradox Fusion: Undead + Nature [Leans: Undead])</b> spell damage:
     * <ul>
     *     <li>Strong against <b>FROZEN (+1)</b> (Dominance {@code blight_nature_dom}: Strong vs Water &amp; Earth) |
     *         Weak against <b>RADIANT (-1)</b>, <b>NETHER (-1)</b>, <b>FROZEN (-1)</b>, and <b>UMBRAL (-1)</b>,
     *         computed dynamically by {@code VisInteractionManager}</li>
     *     <li>Ethereal &amp; Paradox lineage traits: innate physical armor bypass &amp; base armor wear</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithBlightMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.BLIGHT, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L) && !defenderAffs.contains(EssenceType.UMBRAL)) {
            damage *= 0.50F; // Radiant, Nether & Umbral already applied via defenderAffs!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.BLIGHT);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithAdvantageTracked(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.BLIGHT, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.BLIGHT);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Astral Materia (#27 — Ethereal &amp; Paradox Fusion: Void + Lightning [Leans: Void])</b> spell damage:
     * <ul>
     *     <li>Matchups dynamically resolved at runtime via {@code VisInteractionManager}
     *         (Strong vs Air, Umbral, Null &amp; Dominance vs Earth | Weak vs Earth &amp; Nature)</li>
     *     <li>Ethereal &amp; Paradox lineage traits: innate physical armor bypass &amp; base armor wear</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithAstralMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.ASTRAL, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if ((SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                || hasActiveMutatedHex(targetData, now, EssenceType.WATER)
                || hasActiveMutatedHex(targetData, now, EssenceType.STORM))
                && defenderAffs.isEmpty()) {
            damage *= 1.25F; // Conductive cosmic lightning surge on soaked targets!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L)) {
            damage *= 0.75F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.ASTRAL);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.ASTRAL, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.ASTRAL);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Soulfire Materia (#28 — Ethereal &amp; Paradox Fusion: Nether + Undead [Leans: Nether])</b> spell damage:
     * <ul>
     *     <li>Matchups dynamically resolved at runtime via {@code VisInteractionManager}
     *         (Strong vs Frozen &amp; Nature | Weak vs Water &amp; Radiant)</li>
     *     <li>Ethereal &amp; Paradox lineage traits: innate physical armor bypass &amp; base armor wear</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithSoulfireMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.SOULFIRE, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if ((now <= targetData.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L))
                && defenderAffs.isEmpty()) {
            damage *= 1.25F; // Strong against Frozen & Nature shrouds!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_ARID_VEIL_UNTIL).orElse(0L)) {
            damage *= 0.50F; // Weak against Arid veil (Water & Radiant already applied via defenderAffs)!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.SOULFIRE);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithAdvantageTracked(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.SOULFIRE, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.SOULFIRE);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Vapor Materia (#29 — Ethereal &amp; Paradox Fusion: Water + Nether [Leans: Water])</b> spell damage:
     * <ul>
     *     <li>Matchups dynamically resolved at runtime via {@code VisInteractionManager}
     *         (Strong vs Arid &amp; Nether [or Frozen &amp; Nature under Nether dominance] | Weak vs Frozen, Nature &amp; Arid)</li>
     *     <li>Ethereal &amp; Paradox lineage traits: innate physical armor bypass &amp; base armor wear</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithVaporMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.VAPOR, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L) && defenderAffs.isEmpty()) {
            damage *= 1.50F; // Scalding steam quenches Nether shroud!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_DUST_VEIL_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)) {
            damage *= 0.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.VAPOR);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.VAPOR, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.VAPOR);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Null_R Materia (#30 — Ethereal &amp; Paradox Fusion: Radiant + Void [Leans: Void])</b> spell damage:
     * <ul>
     *     <li>Matchups dynamically resolved at runtime via {@code VisInteractionManager}
     *         (Strong vs Air &amp; Undead [under Radiant dominance] | Weak vs Earth &amp; Umbral)</li>
     *     <li>Ethereal &amp; Paradox lineage traits: innate physical armor bypass &amp; base armor wear</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithNullRMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.NULL_R, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (target.getType().is(EntityTypeTags.UNDEAD) && defenderAffs.isEmpty()) {
            damage *= 1.25F; // Radiant-Void antimatter smite!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)) {
            damage *= 0.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.NULL_R);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.NULL_R, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.NULL_R);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Null_U Materia (#31 — Ethereal &amp; Paradox Fusion: Void + Umbral [Leans: Void])</b> spell damage:
     * <ul>
     *     <li>Matchups dynamically resolved at runtime via {@code VisInteractionManager}
     *         (Strong vs Air &amp; Radiant | Weak vs Earth)</li>
     *     <li>Ethereal &amp; Paradox lineage traits: innate physical armor bypass &amp; base armor wear</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithNullUMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.NULL_U, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)) {
            damage *= 0.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.NULL_U);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.NULL_U, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.NULL_U);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Dawn Materia (#32 — Ethereal &amp; Paradox / Tier 1 Fusion: Radiant + Air [Leans: Radiant])</b> spell damage:
     * <ul>
     *     <li>Matchups dynamically resolved at runtime via {@code VisInteractionManager}
     *         (Strong vs Undead &amp; Earth | Weak vs Umbral &amp; Void)</li>
     *     <li>Ethereal &amp; Paradox lineage traits: innate physical armor bypass &amp; base armor wear</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithDawnMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.DAWN, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (target.getType().is(EntityTypeTags.UNDEAD) && defenderAffs.isEmpty()) {
            damage *= 1.25F; // Sunrise holy smite!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_NULL_U_VEIL_UNTIL).orElse(0L)) {
            damage *= 0.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.DAWN);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.DAWN, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.DAWN);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Abyss Materia (#33 — Ethereal &amp; Paradox / Tier 1 Fusion: Umbral + Water [Leans: Umbral])</b> spell damage:
     * <ul>
     *     <li>Matchups dynamically resolved at runtime via {@code VisInteractionManager}
     *         (Strong vs Radiant, Nether &amp; Nature | Weak vs Void &amp; Frozen)</li>
     *     <li>Ethereal &amp; Paradox lineage traits: innate physical armor bypass &amp; base armor wear</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithAbyssMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.ABYSS, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L)) {
            damage *= 0.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.ABYSS);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.ABYSS, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.ABYSS);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals canonical <b>Aegis Materia (#34 — Ethereal &amp; Paradox / Tier 1 Fusion: Radiant + Earth [Leans: Earth])</b> spell damage:
     * <ul>
     *     <li>Matchups dynamically resolved at runtime via {@code VisInteractionManager}
     *         (Strong vs Void &amp; Undead | Weak vs Umbral, Air &amp; Nature)</li>
     *     <li>Ethereal &amp; Paradox lineage traits: innate physical armor bypass &amp; base armor wear</li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithAegisMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.AEGIS, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (target.getType().is(EntityTypeTags.UNDEAD) && defenderAffs.isEmpty()) {
            damage *= 1.25F; // Holy bastion smite!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L)) {
            damage *= 0.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.AEGIS);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.AEGIS, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.AEGIS);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Section 6.36 Pure Aurora Materia Damage Helper:
     * Applies true Aurora Materia spell damage ({@code #FFEA78} Radiant Solar Gold + {@code #98DCF2} Crystalline Cryo Cyan):
     * <ul>
     *     <li>Canonical Aurora Advantage via {@link VisInteractionManager}: Strong vs <b>UNDEAD (+1)</b>, <b>NETHER (+1)</b>,
     *         <b>EARTH (+1)</b>, and <b>NATURE (+1)</b> | Weak vs <b>VOID (-1)</b></li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithAuroraMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.AURORA, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (target.getType().is(EntityTypeTags.UNDEAD) && defenderAffs.isEmpty()) {
            damage *= 1.25F; // Polar radiant smite!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL).orElse(0L)) {
            damage *= 0.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.AURORA);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.AURORA, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.AURORA);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Section 6.37 Pure Mirage Materia Damage Helper:
     * Applies true Mirage Materia spell damage ({@code #FFEA78} Sunlit Mirage Gold + {@code #C2B28F} Bleached Desert Sandstone):
     * <ul>
     *     <li>Canonical Mirage Advantage via {@link VisInteractionManager}: Strong vs <b>WATER (+1)</b>, <b>VOID (+1)</b>,
     *         <b>BLIGHT (+1)</b>, and <b>UNDEAD (+1)</b> | Weak vs <b>UMBRAL (-1)</b> and <b>NATURE (-1)</b></li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithMirageMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.MIRAGE, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (target.getType().is(EntityTypeTags.UNDEAD) && defenderAffs.isEmpty()) {
            damage *= 1.25F; // Sunlit mirage smite!
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L)) {
            damage *= 0.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.MIRAGE);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.MIRAGE, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.MIRAGE);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.38: Pure <b>Wraith Materia</b> damage helper (Dual Void + Undead [Leans: Void]):
     * <ul>
     *     <li>Canonical Wraith Advantage via {@link VisInteractionManager}: Strong vs <b>AIR (+1)</b>, <b>UMBRAL (+1)</b>,
     *         <b>AURA (+1)</b>, and <b>FROZEN (+1)</b> | Weak vs <b>RADIANT (-1)</b> and <b>EARTH (-1)</b></li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithWraithMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.WRAITH, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)) {
            damage *= 0.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.WRAITH);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.WRAITH, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.WRAITH);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.39: Pure <b>Barrow Materia</b> damage helper (Dual Earth + Undead [Leans: Undead]):
     * <ul>
     *     <li>Canonical Barrow Advantage via {@link VisInteractionManager}: Strong vs <b>FROZEN (+1)</b>, <b>AMBER (+1)</b>,
     *         and <b>VOID (+1)</b> | Weak vs <b>RADIANT (-1)</b>, <b>AIR (-1)</b>, and <b>NATURE (-1)</b></li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithBarrowMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.BARROW, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_AMBER_MANTLE_UNTIL).orElse(0L)) {
            damage *= 1.50F;
        } else if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L)) {
            damage *= 0.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.BARROW);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.ETHEREAL_PARADOX.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.BARROW, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.BARROW);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.40: Pure <b>Pyre Materia</b> damage helper (Tier 3 Trinity Earth + Nether + Nature):
     * <ul>
     *     <li>Canonical Pyre Advantage via {@link VisInteractionManager}: Strong vs <b>VOID (+1)</b>, <b>FROZEN (+1)</b>,
     *         <b>NATURE (+1)</b>, <b>GLACIAL (+1)</b>, and <b>RIME (+1)</b> | Weak vs <b>AIR (-1)</b></li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithPyreMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.PYRE, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_CRYO_COCOON_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L)) {
            damage *= 1.50F;
        } else if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_AIR_SLIPSTREAM_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL).orElse(0L)) {
            damage *= 0.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.PYRE);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.TRINITY_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.PYRE, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.PYRE);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.41: Pure <b>Penumbra Materia</b> damage helper (Tier 3 Trinity Radiant + Void + Umbral):
     * <ul>
     *     <li>Canonical Penumbra Advantage via {@link VisInteractionManager}: Strong vs <b>AIR (+1)</b>, <b>RADIANT (+1)</b>,
     *         <b>NATURE (+1)</b>, <b>CELESTIAL (+1)</b>, and <b>AEGIS (+1)</b> | Weak vs <b>EARTH (-1)</b>, <b>VOID (-1)</b>, and <b>UNDEAD (-1)</b></li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithPenumbraMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.PENUMBRA, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.PENUMBRA);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.TRINITY_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithAdvantageTracked(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.PENUMBRA, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.PENUMBRA);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.42: Pure <b>Rime Materia</b> damage helper (Tier 3 Trinity Air + Water + Frozen):
     * <ul>
     *     <li>Canonical Rime Advantage via {@link VisInteractionManager}: Strong vs <b>EARTH (+1)</b>, <b>NATURE (+1)</b>,
     *         <b>MAGMA (+1)</b>, and <b>OVERGROWTH (+1)</b> (plus <b>NETHER</b>, <b>ARID</b> under Water Dominance) |
     *         Weak vs <b>VOID (-1)</b> and <b>UNDEAD (-1)</b></li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithRimeMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.RIME, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL).orElse(0L)) {
            damage *= 1.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.RIME);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.TRINITY_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.RIME, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.RIME);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.43: Pure <b>Spring Materia</b> damage helper (Tier 3 Trinity Vitae + Nature + Water):
     * <ul>
     *     <li>Canonical Spring Advantage via {@link VisInteractionManager}: Strong vs <b>UNDEAD (+1)</b>, <b>WATER (+1)</b>,
     *         <b>EARTH (+1)</b>, <b>SOULFIRE (+1)</b>, and <b>BLIGHT (+1)</b> (plus <b>NETHER</b>, <b>ARID</b> under Water Dominance) |
     *         Weak vs <b>UMBRAL (-1)</b></li>
     *     <li>Stacks held weapon Materia Advantage when {@code includeHeldWeaponSynergy == true}</li>
     * </ul>
     */
    public static float hurtWithSpringMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.SPRING, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_BLIGHT_CARAPACE_UNTIL).orElse(0L)
                || now <= targetData.getLong(SpellLingeringZoneManager.TAG_SOULFIRE_SHROUD_UNTIL).orElse(0L)) {
            damage *= 1.50F;
        } else if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_BLOOD_CARAPACE_UNTIL).orElse(0L)) {
            damage *= 0.50F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.SPRING);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.TRINITY_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.SPRING, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.SPRING);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.44: Pure <b>Static Materia</b> damage helper (Tier 3 Trinity Lightning + Air + Arid):
     * <ul>
     *     <li>Canonical Static Advantage via {@link VisInteractionManager}: Strong vs <b>EARTH (+1)</b>, <b>WATER (+1)</b>,
     *         <b>VOID (+1)</b>, <b>GLACIAL (+1)</b>, and <b>VAPOR (+1)</b> | Weak vs <b>NATURE (-1)</b></li>
     *     <li>Honors wet/soaked 2.0x electrical amplification and stacks held weapon Materia Advantage when requested</li>
     * </ul>
     */
    public static float hurtWithStaticMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.STATIC, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                || hasActiveMutatedHex(targetData, now, EssenceType.WATER)
                || hasActiveMutatedHex(targetData, now, EssenceType.STORM)
                || hasActiveMutatedHex(targetData, now, EssenceType.ABYSS)) {
            damage *= 2.0F;
        } else if (hasActiveMutatedHex(targetData, now, EssenceType.VAPOR)) {
            damage *= (1.0F + 0.50F * getHexStackMultiplier(target));
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.STATIC);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.TRINITY_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.STATIC, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.STATIC);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.45: Pure <b>Sylvan Materia</b> damage helper (Tier 3 Trinity Nature + Earth + Radiant):
     * <ul>
     *     <li>Canonical Sylvan Advantage via {@link VisInteractionManager}: Strong vs <b>WATER (+1)</b>, <b>EARTH (+1)</b>,
     *         <b>UNDEAD (+1)</b>, <b>BARROW (+1)</b>, and <b>WRAITH (+1)</b> | Weak vs <b>NETHER (-1)</b> and <b>AIR (-1)</b></li>
     *     <li>Stacks held weapon Materia Advantage when requested and triggers counter-hex evaluation</li>
     * </ul>
     */
    public static float hurtWithSylvanMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.SYLVAN, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.SYLVAN);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.TRINITY_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.SYLVAN, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.SYLVAN);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.46: Pure <b>Miasma Materia</b> damage helper (Tier 3 Trinity Nature + Undead + Water):
     * <ul>
     *     <li>Canonical Miasma Advantage via {@link VisInteractionManager}: Strong vs <b>FROZEN (+1)</b>, <b>EARTH (+1)</b>,
     *         <b>OASIS (+1)</b>, and <b>TAIGA (+1)</b> | Weak vs <b>UMBRAL (-1)</b>, <b>RADIANT (-1)</b>, and <b>NETHER (-1)</b></li>
     *     <li>Stacks held weapon Materia Advantage when requested and triggers counter-hex evaluation</li>
     * </ul>
     */
    public static float hurtWithMiasmaMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.MIASMA, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.MIASMA);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.TRINITY_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.MIASMA, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.MIASMA);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Returns {@code true} if the target is Undead, Oblivion-attuned, or Wraith-attuned (Section 6.47 Genesis +50% Smite)!
     */
    public static boolean isGenesisSmiteTarget(LivingEntity target) {
        if (target.getType().is(EntityTypeTags.UNDEAD)) {
            return true;
        }
        if (target instanceof SpellEidolonEntity eid) {
            EssenceType em = eid.getEffectiveMateria();
            if (em == EssenceType.UNDEAD || em == EssenceType.OBLIVION || em == EssenceType.WRAITH) {
                return true;
            }
        }
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = target.level().getGameTime();
        return now <= data.getLong(SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_OBLIVION_SHROUD_UNTIL).orElse(0L)
                || now <= data.getLong(SpellLingeringZoneManager.TAG_OBLIVION_EXTINCTION_ARMOR_UNTIL).orElse(0L)
                || hasActiveMutatedHex(data, now, EssenceType.UNDEAD)
                || hasActiveMutatedHex(data, now, EssenceType.OBLIVION)
                || hasActiveMutatedHex(data, now, EssenceType.WRAITH);
    }

    /**
     * Guide Section 6.47: Pure <b>Genesis Materia</b> damage helper (Tier 3 Trinity Vitae + Radiant + Water):
     * <ul>
     *     <li>Canonical Genesis Advantage via {@link VisInteractionManager}: Strong vs <b>UNDEAD (+1)</b>, <b>OBLIVION (+1)</b>,
     *         and <b>WRAITH (+1)</b> | Weak vs <b>UMBRAL (-1)</b></li>
     *     <li>Stacks held weapon Materia Advantage when requested and triggers counter-hex evaluation</li>
     * </ul>
     */
    public static float hurtWithGenesisMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.GENESIS, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.GENESIS);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.TRINITY_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.GENESIS, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.GENESIS);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.48: Pure <b>Oblivion Materia</b> damage helper (Tier 3 Trinity Nether + Umbral + Undead):
     * <ul>
     *     <li>Canonical Oblivion Advantage via {@link VisInteractionManager}: Strong vs <b>RADIANT (+1)</b>, <b>NATURE (+1)</b>,
     *         <b>FROZEN (+1)</b>, <b>GENESIS (+1)</b>, and <b>AURA (+1)</b> | Weak vs <b>WATER (-1)</b> and <b>VOID (-1)</b></li>
     *     <li>Stacks held weapon Materia Advantage when requested and triggers counter-hex evaluation</li>
     * </ul>
     */
    public static float hurtWithOblivionMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.OBLIVION, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.OBLIVION);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.TRINITY_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 120L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP)) {
                float prevConf = targetData.getFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, Math.min(prevConf, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.OBLIVION, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.OBLIVION);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.50: Pure <b>Aether Materia</b> damage helper (Tier 4 Apex Air + Earth + Water + Nether catalyzed by Chimera):
     * <ul>
     *     <li>Canonical Aether Advantage via {@link VisInteractionManager}: Strong vs <b>EARTH (+1)</b>, <b>NETHER (+1)</b>,
     *         <b>VOID (+1)</b>, <b>MAGMA (+1)</b>, <b>DUST (+1)</b>, and <b>OVERGROWTH (+1)</b> |
     *         Weak vs <b>VOID (-1)</b>, <b>NATURE (-1)</b>, <b>FROZEN (-1)</b>, and <b>ARID (-1)</b></li>
     *     <li>Bypasses armor via pure Materia damage, stacks held weapon Materia Advantage when requested, and triggers counter-hex evaluation</li>
     * </ul>
     */
    public static float hurtWithAetherMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.AETHER, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.AETHER);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.APEX_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 140L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP)) {
                float prevConf = targetData.getFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, Math.min(prevConf, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.AETHER, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.AETHER);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.51: Pure <b>Entropic Materia</b> damage helper (Tier 4 Apex Void + Umbral + Undead + Blood catalyzed by Chimera):
     * <ul>
     *     <li>Canonical Entropic Advantage via {@link VisInteractionManager}: Strong vs <b>AIR (+1)</b>, <b>RADIANT (+1)</b>,
     *         <b>NATURE (+1)</b>, <b>FROZEN (+1)</b>, <b>CELESTIAL (+1)</b>, <b>AEGIS (+1)</b>, and <b>AURORA (+1)</b> |
     *         Weak vs <b>EARTH (-1)</b>, <b>VOID (-1)</b>, <b>RADIANT (-1)</b>, <b>NETHER (-1)</b>, and <b>UMBRAL (-1)</b></li>
     *     <li>Bypasses armor via pure Materia damage, stacks held weapon Materia Advantage when requested, and triggers counter-hex evaluation</li>
     * </ul>
     */
    public static float hurtWithEntropicMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.ENTROPIC, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.ENTROPIC);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.APEX_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 140L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP)) {
                float prevConf = targetData.getFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, Math.min(prevConf, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.ENTROPIC, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.ENTROPIC);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.52: Pure <b>Celestial Materia</b> damage helper (Tier 4 Apex Radiant + Nature + Vitae + Air catalyzed by Chimera):
     * <ul>
     *     <li>Canonical Celestial Advantage via {@link VisInteractionManager}: Strong vs <b>UNDEAD (+1)</b>, <b>WATER (+1)</b>,
     *         <b>EARTH (+1)</b>, <b>OBLIVION (+1)</b>, <b>WRAITH (+1)</b>, and <b>BARROW (+1)</b> |
     *         Weak vs <b>UMBRAL (-1)</b> and <b>VOID (-1)</b></li>
     *     <li>Applies holy-pranic smite multiplication against Undead, Demonic (Nether), and Abyssal (Void/Oblivion) entities,
     *         bypasses armor via pure Materia damage, stacks held weapon Materia Advantage, and triggers counter-hex evaluation</li>
     * </ul>
     */
    public static float hurtWithCelestialMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow
    ) {
        return hurtWithCelestialMateriaAdvantage(
                level, target, caster, baseDamage, includeHeldWeaponSynergy, bypassSpellHitWindow, 1.75F
        );
    }

    public static float hurtWithCelestialMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSpellHitWindow,
            float undeadDemonicAbyssalSmiteMultiplier
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (bypassSpellHitWindow) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.CELESTIAL, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        if (undeadDemonicAbyssalSmiteMultiplier > 1.0F
                && (ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(target)
                || defenderAffs.contains(EssenceType.UNDEAD)
                || defenderAffs.contains(EssenceType.NETHER)
                || defenderAffs.contains(EssenceType.VOID)
                || defenderAffs.contains(EssenceType.OBLIVION))) {
            damage *= undeadDemonicAbyssalSmiteMultiplier;
        }

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.CELESTIAL);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, MateriaFamilyTier.APEX_FUSION.getBonusArmorShredPoints()));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 140L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP)) {
                float prevConf = targetData.getFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, Math.min(prevConf, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.CELESTIAL, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.CELESTIAL);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Inflicts true Prismatic Materia (#51) spell damage (Strong vs UNDEAD, BLIGHT, SOULFIRE & BLOOD, Weak vs UMBRAL),
     * optionally bypassing 50% of target physical armor and applying bonus radiant-prismatic smite against corrupt/undead foes.
     */
    public static float hurtWithPrismaticMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassFiftyPercentArmor
    ) {
        return hurtWithPrismaticMateriaAdvantage(
                level, target, caster, baseDamage, includeHeldWeaponSynergy, bypassFiftyPercentArmor, 1.50F
        );
    }

    public static float hurtWithPrismaticMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassFiftyPercentArmor,
            float corruptionSmiteMultiplier
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        float damage = baseDamage;
        if (!bypassFiftyPercentArmor) {
            damage = net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb(
                    target,
                    damage,
                    level.damageSources().magic(),
                    target.getArmorValue(),
                    (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS)
            );
        } else {
            float halfArmor = target.getArmorValue() * 0.50F;
            float halfToughness = (float) (target.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * 0.50D);
            damage = net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb(
                    target,
                    damage,
                    level.damageSources().magic(),
                    halfArmor,
                    halfToughness
            );
        }

        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.PRISMATIC, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        boolean corruptTarget = ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(target)
                || defenderAffs.contains(EssenceType.UNDEAD)
                || defenderAffs.contains(EssenceType.BLIGHT)
                || defenderAffs.contains(EssenceType.SOULFIRE)
                || defenderAffs.contains(EssenceType.BLOOD)
                || targetData.getLong(SpellLingeringZoneManager.TAG_BLIGHT_CHAIN_MARK_UNTIL).orElse(0L) > now;
        if (corruptionSmiteMultiplier > 1.0F && corruptTarget) {
            damage *= corruptionSmiteMultiplier;
        }

        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.PRISMATIC);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, 4));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 140L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP)) {
                float prevConf = targetData.getFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, Math.min(prevConf, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.PRISMATIC, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.PRISMATIC);
            return finalDamage;
        }
        return 0.0F;
    }

    @Nullable
    public static LivingEntity resolveHexCasterPublic(ServerLevel level, LivingEntity afflictedTarget) {
        return resolveHexCaster(level, afflictedTarget);
    }

    /**
     * Section 6.54 Empyrean Materia (#52 — Tier 5 Eschaton: Radiant + Vitae + Nature, "The Infinite Life") spell damage:
     * +1 vs UNDEAD, WATER, EARTH, NETHER, VOID, OBLIVION, WRAITH, BARROW, BLIGHT | -1 vs UMBRAL.
     * Supports Tier 5 60% armor bypass, optional 100% true damage bypass, and optional +60% Undead/Dark smite!
     */
    public static float hurtWithEmpyreanMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSixtyPercentArmor,
            boolean trueDamageBypassAllArmor,
            boolean smiteUndeadAndDark
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        float damage = baseDamage;
        if (!trueDamageBypassAllArmor) {
            float armorFrac = bypassSixtyPercentArmor ? 0.40F : 1.0F;
            damage = net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb(
                    target,
                    damage,
                    level.damageSources().magic(),
                    target.getArmorValue() * armorFrac,
                    (float) (target.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * armorFrac)
            );
        }

        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.EMPYREAN, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (smiteUndeadAndDark && (ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(target)
                || defenderAffs.contains(EssenceType.UNDEAD)
                || defenderAffs.contains(EssenceType.VOID)
                || defenderAffs.contains(EssenceType.UMBRAL)
                || defenderAffs.contains(EssenceType.OBLIVION)
                || defenderAffs.contains(EssenceType.WRAITH)
                || defenderAffs.contains(EssenceType.BARROW)
                || defenderAffs.contains(EssenceType.BLIGHT))) {
            damage *= 1.60F; // Section 6.54: +60% bonus smite vs Undead & Dark entities!
        }

        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.EMPYREAN);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, 6));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 180L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP)) {
                float prevConf = targetData.getFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, Math.min(prevConf, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.EMPYREAN, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.EMPYREAN);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 6.55: Pure <b>Cataclysm Materia (#53)</b> damage helper ("The World Breaker" — Void + Umbral + Nether):
     * Strong vs {@code AIR (+1)}, {@code RADIANT (+1)}, {@code NATURE (+1)}, {@code FROZEN (+1)}, {@code WATER (+1)},
     * {@code CELESTIAL (+1)}, {@code AEGIS (+1)}, {@code AURORA (+1)}, and {@code GENESIS (+1)} | Weak vs {@code VITAE (-1)}.
     */
    public static float hurtWithCataclysmMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSixtyPercentArmor,
            boolean trueDamageBypassAllArmor
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (trueDamageBypassAllArmor) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        if (!trueDamageBypassAllArmor) {
            float armorFrac = bypassSixtyPercentArmor ? 0.40F : 1.0F;
            damage = net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb(
                    target,
                    damage,
                    level.damageSources().magic(),
                    target.getArmorValue() * armorFrac,
                    (float) (target.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * armorFrac)
            );
        }

        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.CATACLYSM, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.CATACLYSM);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, 6));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 180L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP)) {
                float prevConf = targetData.getFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, Math.min(prevConf, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.CATACLYSM, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.CATACLYSM);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals pure Terminus Materia damage (#54 Tier 5 Eschaton: +1 vs Nether, Arid, Earth, Water, Magma, Pyre, Blight, Soulfire, Miasma;
     * -1 vs Undead), supporting 60% armor bypass or 100% true damage bypass, 6-point armor shredding, & held weapon synergy!
     */
    public static float hurtWithTerminusMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSixtyPercentArmor,
            boolean trueDamageBypassAllArmor
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (trueDamageBypassAllArmor) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        if (!trueDamageBypassAllArmor) {
            float armorFrac = bypassSixtyPercentArmor ? 0.40F : 1.0F;
            damage = net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb(
                    target,
                    damage,
                    level.damageSources().magic(),
                    target.getArmorValue() * armorFrac,
                    (float) (target.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * armorFrac)
            );
        }

        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.TERMINUS, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.TERMINUS);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, 6));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 180L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP)) {
                float prevConf = targetData.getFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, Math.min(prevConf, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.TERMINUS, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.TERMINUS);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals pure Eschaton Materia damage (#55 Tier 6 Singularity: +1 vs ALL lower-tier elements; -1 vs Apotheosis),
     * supporting 75% armor bypass or 100% true damage bypass, 8-point armor shredding, & held weapon synergy!
     */
    public static float hurtWithEschatonMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSeventyFivePercentArmor,
            boolean trueDamageBypassAllArmor
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (trueDamageBypassAllArmor) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        if (!trueDamageBypassAllArmor) {
            float armorFrac = bypassSeventyFivePercentArmor ? 0.25F : 1.0F;
            damage = net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb(
                    target,
                    damage,
                    level.damageSources().magic(),
                    target.getArmorValue() * armorFrac,
                    (float) (target.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * armorFrac)
            );
        }

        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.ESCHATON, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.ESCHATON);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, 8));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 200L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP)) {
                float prevConf = targetData.getFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, Math.min(prevConf, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.ESCHATON, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.ESCHATON);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Deals pure Apotheosis Materia damage (#56 Tier 6 Singularity: +1 vs ALL lower-tier elements, +2 vs dark/void/nether/undead;
     * -1 vs Eschaton), supporting 75% armor bypass or 100% true holy damage bypass, optional 2.0x smite multiplier vs
     * Undead/Demonic/Void/Abyssal entities, 8-point armor shredding, & held weapon synergy!
     */
    public static float hurtWithApotheosisMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSeventyFivePercentArmor,
            boolean doubleAgainstDarkUndeadVoid,
            boolean trueDamageBypassAllArmor
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (trueDamageBypassAllArmor) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        if (doubleAgainstDarkUndeadVoid
                && ddraig.net.entropica.spell.mutation.element.ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(target)) {
            damage *= 2.0F;
        }

        if (!trueDamageBypassAllArmor) {
            float armorFrac = bypassSeventyFivePercentArmor ? 0.25F : 1.0F;
            damage = net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb(
                    target,
                    damage,
                    level.damageSources().magic(),
                    target.getArmorValue() * armorFrac,
                    (float) (target.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * armorFrac)
            );
        }

        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.APOTHEOSIS, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.APOTHEOSIS);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, 8));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 200L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP)) {
                float prevConf = targetData.getFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, Math.min(prevConf, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.APOTHEOSIS, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.APOTHEOSIS);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Section 6.59 Pure Entropica (#57) Materia Damage Helper:
     * Applies Tier 6 Grand Cosmic Equilibrium Singularity spell damage with universal {@code +1} Elemental Advantage
     * against EVERYTHING (including Eschaton and Apotheosis), complete immunity to counter-weaknesses,
     * {@code 75%} armor bypass (or {@code 100%} true damage bypass), {@code 8}-point armor shred, and held weapon synergy.
     */
    public static float hurtWithEntropicaMateriaAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage,
            boolean includeHeldWeaponSynergy,
            boolean bypassSeventyFivePercentArmor,
            boolean trueDamageBypassAllArmor
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) {
            return 0.0F;
        }
        if (trueDamageBypassAllArmor) {
            target.invulnerableTime = 0;
        } else if (!acquireMultiHitWindow(level, target)) {
            return 0.0F;
        }

        float damage = baseDamage;
        if (!trueDamageBypassAllArmor) {
            float armorFrac = bypassSeventyFivePercentArmor ? 0.25F : 1.0F;
            damage = net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb(
                    target,
                    damage,
                    level.damageSources().magic(),
                    target.getArmorValue() * armorFrac,
                    (float) (target.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * armorFrac)
            );
        }

        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        float bonusScore = (includeHeldWeaponSynergy && caster != null)
                ? resolveHeldWeaponAdvantageScore(caster, target)
                : 0.0F;
        float advMult = calculateSpellAdvantageDamageMultiplier(
                EssenceType.ENTROPICA, caster, target, defenderAffs, bonusScore
        );
        if (advMult <= 0.0F) {
            return 0.0F;
        }
        damage *= advMult;

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.ENTROPICA);
        if (materiaResist < 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 1.0F, 1.50F);
        }

        int currentShred = now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)
                ? targetData.getInt(TAG_ARMOR_SHRED).orElse(0) : 0;
        int nextShred = Math.min(8, Math.max(currentShred, 8));
        targetData.putInt(TAG_ARMOR_SHRED, nextShred);
        targetData.putLong(TAG_ARMOR_SHRED_UNTIL, now + 200L);
        if (currentShred > 0) {
            damage *= (1.0F + 0.04F * Math.min(8, currentShred));
        }

        float finalDamage = Math.max(0.5F, damage);
        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, finalDamage);
        if (hurt) {
            if (targetData.contains(TAG_HEX_MAX_ALLOWED_HP)) {
                float prevCeil = targetData.getFloat(TAG_HEX_MAX_ALLOWED_HP).orElse(target.getMaxHealth());
                targetData.putFloat(TAG_HEX_MAX_ALLOWED_HP, Math.min(prevCeil, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP)) {
                float prevMiasma = targetData.getFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, Math.min(prevMiasma, target.getHealth()));
            }
            if (targetData.contains(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP)) {
                float prevConf = targetData.getFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP).orElse(target.getMaxHealth());
                targetData.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, Math.min(prevConf, target.getHealth()));
            }
            if (target.isAlive()) {
                evaluateElementalReaction(level, caster, target, EssenceType.ENTROPICA, finalDamage, true);
            }
            checkElementalHexCounterCleanse(target, EssenceType.ENTROPICA);
            return finalDamage;
        }
        return 0.0F;
    }

    /**
     * Guide Section 4.4 Touch / Melee Strike Weapon Synergy:
     * When a Touch spell is cast through a Materia-attuned weapon ({@code ModDataComponents.ESSENCE_STATS}),
     * the weapon's innate Materia Advantage stacks additively with the spell's Materia!
     */
    private static float resolveHeldWeaponAdvantageScore(LivingEntity caster, LivingEntity target) {
        ItemStack held = caster.getMainHandItem();
        if (held.isEmpty()) return 0.0F;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        if (defenderAffs.isEmpty()) return 0.0F;
        VisWeaponState weaponState = held.get(ModDataComponents.VIS_WEAPON_STATE.get());
        if (weaponState != null && weaponState.baseType() != EssenceType.REGULAR) {
            List<String> profiles = new ArrayList<>();
            profiles.add(weaponState.activeProfile());
            for (EssenceType frag : weaponState.slottedFragments()) {
                if (frag != null && frag != EssenceType.REGULAR) {
                    profiles.add(frag.getSerializedName());
                }
            }
            return VisInteractionManager.getInstance().calculateLineageAdvantageScore(profiles, defenderAffs);
        }
        EssenceCombatStats stats = held.get(ModDataComponents.ESSENCE_STATS.get());
        if (stats == null || stats.attackBonuses().isEmpty()) return 0.0F;
        int score = 0;
        for (EssenceType atkType : stats.attackBonuses().keySet()) {
            if (atkType != null && atkType != EssenceType.REGULAR) {
                score += calculateSpellElementalAdvantageScore(atkType, defenderAffs);
            }
        }
        return score;
    }

    private static boolean isNecroticLine(EssenceType t) {
        return t == EssenceType.UNDEAD || t == EssenceType.BLIGHT || t == EssenceType.MIASMA
                || t == EssenceType.WRAITH || t == EssenceType.BARROW || t == EssenceType.OBLIVION;
    }
}
