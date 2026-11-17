package ddraig.net.entropica.spell.engine;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.combat.VisInteractionManager;
import ddraig.net.entropica.component.EssenceCombatStats;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModEntityTags;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellFlowModifier;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
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

    private static final ThreadLocal<Boolean> REENTRANT_GUST_HURT = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> REENTRANT_ELEMENTAL_REACTION = ThreadLocal.withInitial(() -> false);

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

        // 2. Materia Advantage via VisInteractionManager + Mutated Flow Advantage Bonus + Touch Weapon Materia Advantage
        EssenceType safeFallback = fallbackMateria != null ? fallbackMateria : EssenceType.REGULAR;
        EssenceType flowMateria = spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), safeFallback);
        MateriaPartMutationHandler flowHandler = SpellMutationHandlerRegistry.resolveActive(flowMateria, spell.flowPart().mutationEnabled());
        float flowBonusAdv = flowHandler != null ? flowHandler.getFlowBonusAdvantageScore(spell.flowPart().flow(), target) : 0.0F;

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
        boolean undeadBeamSiphon = false;
        if (resolvedSlot != null && resolvedSlot.formPart().form() == ddraig.net.entropica.spell.SpellDeliveryForm.TOUCH_STRIKE) {
            if (caster != null) {
                weaponBonusAdv = resolveHeldWeaponAdvantageScore(caster, target);
            }
            if (resolvedSlot.formPart().mutationEnabled()) {
                EssenceType touchFormMat = resolvedSlot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), safeFallback);
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
                }
            }
        } else if (resolvedSlot != null
                && resolvedSlot.formPart().form() == ddraig.net.entropica.spell.SpellDeliveryForm.BEAM
                && resolvedSlot.formPart().mutationEnabled()
                && resolvedSlot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), safeFallback) == EssenceType.UNDEAD) {
            undeadBeamSiphon = true;
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

        if (effectMateria == EssenceType.ENTROPICA) {
            // Sec 6.59 Entropica: +1 Elemental Advantage against EVERYTHING, 0 weaknesses!
            float totalScore = 1.0F + flowBonusAdv + weaponBonusAdv + seismicEarthAdv + flashFrozenAdv + cauterizingNetherAdv
                    + holySmiteRadiantAdv + cloakingUmbralAdv + vampiricUndeadAdv + destabilizeVoidAdv + mummifyAridAdv
                    + overchargeLightningAdv + volcanicMagmaAdv + squallStormAdv + brittleGlacialAdv + botanicalOvergrowthAdv
                    + smotherDustAdv + infestationSporeAdv;
            float advMult = Math.clamp(1.0F + (totalScore * 0.25F), 1.25F, 1.85F) + spell.flowPart().flow().getInnateAdvantageBonus();
            damage *= advMult;
        } else {
            int advScore = (effectMateria != null && effectMateria != EssenceType.REGULAR && !defenderAffinities.isEmpty())
                    ? calculateSpellElementalAdvantageScore(effectMateria, defenderAffinities)
                    : 0;
            float totalScore = advScore + flowBonusAdv + weaponBonusAdv + seismicEarthAdv + flashFrozenAdv + cauterizingNetherAdv
                    + holySmiteRadiantAdv + cloakingUmbralAdv + vampiricUndeadAdv + destabilizeVoidAdv + mummifyAridAdv
                    + overchargeLightningAdv + volcanicMagmaAdv + squallStormAdv + brittleGlacialAdv + botanicalOvergrowthAdv
                    + smotherDustAdv + infestationSporeAdv;
            if (totalScore != 0.0F) {
                float advMult = Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
                if (totalScore > 0 || spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY) {
                    advMult += spell.flowPart().flow().getInnateAdvantageBonus();
                }
                damage *= advMult;
            } else if (spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY) {
                damage *= 1.15F;
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
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)) {
            // Section 6.6 Glacial Spikes Retaliation: registers defense as Frozen (+1 vs Water -> 0.50x, -1 vs Nether/Undead -> 1.50x)!
            if (isWaterLine(effectMateria)) {
                damage *= 0.50F;
            } else if (MateriaFamilyTier.isNetherLineage(effectMateria) || isNecroticLine(effectMateria)) {
                damage *= 1.50F;
            }
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.7 Immolation Shroud: registers defense as Nether (+1 vs Frozen -> 0.50x, -1 vs Water -> 1.50x; Glacial Water+Frozen neutralized)!
            if (MateriaFamilyTier.isFrozenLineage(effectMateria) && effectMateria != EssenceType.GLACIAL) {
                damage *= 0.50F;
                target.setTicksFrozen(0);
            } else if (isWaterLine(effectMateria)) {
                damage *= 1.50F;
            }
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
        if ((SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                || hasActiveMutatedHex(targetData, now, EssenceType.WATER)
                || hasActiveMutatedHex(targetData, now, EssenceType.STORM))
                && isLightningLine(effectMateria)) {
            damage *= 2.0F;
        }
        // Earth Sundering Hex (#4): 2.0x incoming Earth spell damage!
        if (hasActiveMutatedHex(targetData, now, EssenceType.EARTH)
                && (MateriaFamilyTier.isEarthLineage(effectMateria) || earthSeismicStrike)) {
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
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, Math.max(0.5F, damage));
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
        if (effectMutation.mutated()) {
            applyMutatedElementalAftereffects(level, target, effectMateria, tier);
        } else if (effectMateria != null && MateriaFamilyTier.isFrozenLineage(effectMateria)) {
            boolean tripleFreeze = SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                    || hasActiveMutatedHex(targetData, now, EssenceType.WATER);
            int freezeGain = (30 + tier * 8) * (tripleFreeze ? 3 : 1);
            target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 120, target.getTicksFrozen() + freezeGain));
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
            if (hasActiveMutatedHex(data, now, EssenceType.FROZEN) || hasActiveMutatedHex(data, now, EssenceType.NETHER)) {
                clearHexData(target, data);
            }
            data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
            target.clearFire();
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
            if (hasActiveMutatedHex(data, now, EssenceType.RADIANT) || hasActiveMutatedHex(data, now, EssenceType.UMBRAL)) {
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
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_LAST_POS_X, target.getX());
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_LAST_POS_Y, target.getY());
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_LAST_POS_Z, target.getZ());
            data.putLong("SpellVoidLastRubberbandTick", now);
        }

        if (mutated && resolved == EssenceType.NATURE) {
            // Section 6.4 Silencing Spore Hex: immediately silence spellcasting & mob special abilities and shred 20% Armor Toughness!
            data.putLong("SpellSilencedUntil", now + 45L);
            applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.20D * getHexStackMultiplier(target)), 45L);
        } else if (mutated && resolved == EssenceType.EARTH) {
            // Section 6.5 Sundering Hex: immediately fracture physical armor by 5 points (scaling I–III) on cast!
            applyTimedAttributeShred(target, level, 5.0D * getHexStackMultiplier(target), 0.0D, 65L);
        } else if (mutated && resolved == EssenceType.RADIANT) {
            // Section 6.8 Sun-Scorched Smite Hex: immediately melt away 4 armor points (scaling I–III) on cast!
            applyTimedAttributeShred(target, level, 4.0D * getHexStackMultiplier(target), 0.0D, 65L);
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
        }

        if (mutated && MateriaFamilyTier.isMonsterEmpowermentHex(resolved) && !(target instanceof Player) && !isBoss) {
            if (resolved != EssenceType.ARID) {
                target.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, stack - 1));
            }
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
        // Section 6.10 Cadaverous Wither-Blight Hex & Section 1.2B Entangling Miasma prevent ALL health regeneration!
        if (hasActiveMutatedHex(data, now, EssenceType.UNDEAD)
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
            case "WATER", "STORM" -> MateriaFamilyTier.isNetherLineage(incomingElement)
                    || incomingElement == EssenceType.ARID
                    || incomingElement == EssenceType.VAPOR
                    || incomingElement == EssenceType.DAWN;
            case "NETHER", "ARID", "MAGMA", "OASIS", "SOULFIRE", "PYRE" -> isWaterLine(incomingElement) || MateriaFamilyTier.isFrozenLineage(incomingElement);
            case "FROZEN", "GLACIAL", "NATURE", "OVERGROWTH", "SPORE", "TAIGA", "AMBER", "AURORA", "RIME", "SYLVAN", "TERMINUS" -> MateriaFamilyTier.isNetherLineage(incomingElement);
            case "VAPOR" -> MateriaFamilyTier.isFrozenLineage(incomingElement);
            case "DUST" -> isWaterLine(incomingElement) || (isAirLine(incomingElement) && incomingElement != EssenceType.DUST);
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
            case "VOID" -> MateriaFamilyTier.isEarthLineage(incomingElement);
            case "LIGHTNING", "AURA", "ASTRAL", "NULL_R", "STATIC" -> MateriaFamilyTier.isEarthLineage(incomingElement) || isNatureLine(incomingElement);
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
        data.remove(TAG_HEX_TYPE);
        data.remove(TAG_HEX_TIER);
        data.remove(TAG_HEX_STACK);
        data.remove(TAG_HEX_MUTATED);
        data.remove(TAG_HEX_UNTIL);
        data.remove(TAG_HEX_INITIAL_WINDOW);
        data.remove(TAG_HEX_CASTER_UUID);
        data.remove(TAG_HEX_MAX_ALLOWED_HP);
        data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
        data.remove(SpellLingeringZoneManager.TAG_VOID_ANCHOR_X);
        data.remove(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Y);
        data.remove(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Z);
        data.remove(SpellLingeringZoneManager.TAG_VOID_LAST_POS_X);
        data.remove(SpellLingeringZoneManager.TAG_VOID_LAST_POS_Y);
        data.remove(SpellLingeringZoneManager.TAG_VOID_LAST_POS_Z);
        data.remove("SpellVoidLastRubberbandTick");
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
            data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
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

            // 2. Enforce Natural Health Regeneration Lock & Permafrost / Glacial Cryo-Hex Speed Sap every 5 ticks
            if (hexMutated && hexType == EssenceType.FROZEN) {
                living.setSprinting(false);
                applyTimedMovementSpeedSap(living, level, 0.60D, 35L);
            } else if (hexMutated && hexType == EssenceType.GLACIAL) {
                living.setSprinting(false);
                applyTimedMovementSpeedSap(living, level, Math.min(0.85D, 0.50D * getHexStackMultiplier(living)), 35L);
            } else if (hexMutated && (hexType == EssenceType.NATURE || hexType == EssenceType.OVERGROWTH)) {
                SpellLingeringZoneManager.applyArcaneSilence(living, 35);
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

            // Section 6.3 Waterlogged Drowning Hex: fire/heat reactions immediately evaporate & cleanse the soaking curse!
            if (hexType == EssenceType.WATER && (living.isInLava() || living.isOnFire())) {
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

            // Section 6.6 Permafrost Stasis Hex & Section 6.16 Absolute-Zero Cryo-Hex: fire/lava reactions immediately thaw & cleanse the deep cryogenic curse!
            if ((hexType == EssenceType.FROZEN || hexType == EssenceType.GLACIAL) && (living.isInLava() || living.isOnFire())) {
                clearHexData(living, data);
                living.setTicksFrozen(0);
                level.sendParticles(ParticleTypes.CLOUD, living.getX(), living.getY(0.85D), living.getZ(),
                        18, 0.4D, 0.45D, 0.4D, 0.05D);
                level.playSound(null, living.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.70F, 1.35F);
                continue;
            }

            // Section 6.18 Choking Powder Smother Hex: submerging in water or rain washes away the choking chalk powder!
            if (hexType == EssenceType.DUST && living.isInWaterOrRain()) {
                clearHexData(living, data);
                data.remove("SpellSilencedUntil");
                level.sendParticles(ParticleTypes.SPLASH, living.getX(), living.getY(0.85D), living.getZ(),
                        18, 0.4D, 0.45D, 0.4D, 0.08D);
                level.playSound(null, living.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.65F, 1.35F);
                continue;
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
        SpellExecutionEngine.tryTriggerCorpseBloomOnDeath(level, deceased);
        CompoundTag data = EntityHelper.getPersistentData(deceased);
        long now = level.getGameTime();

        // Spore (#18) Mycelial Infestation Strike: dying host bursts into infectious Poison/Nausea spore pop + mushrooms!
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL).orElse(0L)) {
            // Section 6.7 Cauterizing Cleave & Auto-Smelt: all meat drops spawn cooked & held raw ores drop pre-smelted!
            Vec3 deathPos = deceased.position();
            ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.dropHeldRawOresPreSmelted(level, deceased);
            ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.autoSmeltDroppedItems(
                    level, deceased.getBoundingBox().inflate(3.5D)
            );
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    null, deathPos, 3.5D, 8, now, null, null, EssenceType.NETHER, 1.0F,
                    (srv, z, el) -> {
                        ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.autoSmeltDroppedItems(
                                srv, new AABB(z.center.x - 3.5D, z.center.y - 2.0D, z.center.z - 3.5D,
                                        z.center.x + 3.5D, z.center.y + 3.0D, z.center.z + 3.5D)
                        );
                        return el < z.maxDurationTicks;
                    }
            ));
        }
        if (now <= data.getLong(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL).orElse(0L)) {
            // Section 6.8 Holy Smite & Daylight Ignition: lethal kill disintegrates the target into glowing stardust
            // rather than dropping standard rotting meat!
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
                    level, deceased.getBoundingBox().inflate(3.5D), deathPos
            );
            final boolean[] spawnedStardust = {convertedNow > 0};
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    null, deathPos, 3.5D, 8, now, null, null, EssenceType.RADIANT, 1.0F,
                    (srv, z, el) -> {
                        int c = ddraig.net.entropica.spell.mutation.element.RadiantSpellMutationHandler.purifyRottingDropsToStardust(
                                srv, new AABB(z.center.x - 3.5D, z.center.y - 2.5D, z.center.z - 3.5D,
                                        z.center.x + 3.5D, z.center.y + 2.5D, z.center.z + 3.5D),
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
        long window = data.getLong(TAG_HEX_INITIAL_WINDOW).orElse(0L);
        boolean tier6Contagion = hexMutated && (hexType == EssenceType.ESCHATON
                || hexType == EssenceType.APOTHEOSIS
                || hexType == EssenceType.ENTROPICA);
        boolean undeadPlagueSpread = hexMutated && (hexType == EssenceType.UNDEAD || hexType == EssenceType.BLIGHT);
        if (!undeadPlagueSpread && !tier6Contagion && now > window) {
            return;
        }

        int tier = data.getInt(TAG_HEX_TIER).orElse(1);
        LivingEntity origHexCaster = resolveHexCaster(level, deceased);
        double spreadRadius = tier6Contagion ? 6.0D : 5.0D;
        AABB box = deceased.getBoundingBox().inflate(spreadRadius);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (nearby == deceased || !nearby.isAlive() || nearby instanceof Player || nearby instanceof SpellEidolonEntity) continue;
            if (hexType == EssenceType.UNDEAD && nearby.getType().is(EntityTypeTags.UNDEAD)) continue;
            if (nearby instanceof Enemy || (nearby instanceof Mob mob && mob.getTarget() != null)) {
                applyPersistentHex(level, origHexCaster, nearby, hexType, tier, 300, false, hexMutated);
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
        } else if (data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_AEGIS_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_EMPYREAN_AEGIS_UNTIL).orElse(0L);
            primary = EssenceType.EMPYREAN;
            secondary = EssenceType.AEGIS;
            tier = Math.max(tier, 7);
        } else if (data.getLong(SpellLingeringZoneManager.TAG_AETHER_AEGIS_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_AETHER_AEGIS_UNTIL).orElse(0L);
            primary = EssenceType.AETHER;
            secondary = EssenceType.AEGIS;
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
            secondary = EssenceType.AEGIS;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_ESCHATON_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_ESCHATON_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.ESCHATON;
            secondary = EssenceType.AEGIS;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.APOTHEOSIS;
            secondary = EssenceType.AEGIS;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL).orElse(0L);
            primary = EssenceType.ENTROPICA;
            secondary = EssenceType.AEGIS;
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
        } else if (data.getLong(SpellLingeringZoneManager.TAG_ECLIPSE_BASTION_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_ECLIPSE_BASTION_UNTIL).orElse(0L);
            primary = EssenceType.ECLIPSE;
            secondary = EssenceType.AEGIS;
        } else if (data.getLong(SpellLingeringZoneManager.TAG_AMBER_MANTLE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_AMBER_MANTLE_UNTIL).orElse(0L);
            primary = EssenceType.AMBER;
            secondary = EssenceType.AEGIS;
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
        } else if (data.getLong(SpellLingeringZoneManager.TAG_OASIS_CARAPACE_UNTIL).orElse(0L) > now) {
            barrierUntil = data.getLong(SpellLingeringZoneManager.TAG_OASIS_CARAPACE_UNTIL).orElse(0L);
            primary = EssenceType.OASIS;
            secondary = EssenceType.AEGIS;
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
                    atk.hurtServer(level, level.damageSources().magic(), amount);
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

        // Section 6.6 Glacial Spikes Retaliation: enforce genuine -30% melee swing rate on chilled hostile Mobs!
        if (rawAttacker instanceof Mob chilledMob
                && chilledMob != entity
                && !source.is(DamageTypeTags.IS_PROJECTILE)) {
            CompoundTag atkTag = EntityHelper.getPersistentData(chilledMob);
            if (now <= atkTag.getLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_UNTIL).orElse(0L)) {
                long nextAllowed = atkTag.getLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED).orElse(0L);
                if (now < nextAllowed) {
                    return EventResult.interruptFalse();
                }
                // Vanilla mob melee interval is 20t; 20 / 0.70 = 28.6t (~29 ticks -> true -30% attack speed!)
                atkTag.putLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED, now + 29L);
            } else if (hasActiveMutatedHex(atkTag, now, EssenceType.LIGHTNING)
                    || hasActiveMutatedHex(atkTag, now, EssenceType.STORM)) {
                long nextAllowed = atkTag.getLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED).orElse(0L);
                if (now < nextAllowed) {
                    return EventResult.interruptFalse();
                }
                // Section 6.13 Galvanic Overcharge & Section 6.15 Waterlogged Hypoxia Hex: 20 / 0.60 = 33.3t (~33 ticks -> true -40% mob melee attack speed!)
                atkTag.putLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED, now + 33L);
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
                    hurtWithNetherMateriaAdvantage(level, entity, meleeAttacker, amount, true);
                    return EventResult.interruptFalse();
                }
                // Section 6.8 Holy Smite & Daylight Ignition: converts the physical melee strike into pure Radiant Materia damage (+1 vs Undead, -1 vs Umbral)!
                if (holySmiteConversion && !SpellExecutionEngine.hasPrimedRadiantHolySmiteStrike(meleeAttacker) && entity.isAlive()) {
                    EntityHelper.getPersistentData(entity).putLong(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL, level.getGameTime() + 40L);
                    hurtWithRadiantMateriaAdvantage(level, entity, meleeAttacker, amount, true);
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
            } finally {
                REENTRANT_GUST_HURT.set(false);
            }
        }

        // Earth Defensive Attunement (#4): hexagonal bastion hit-flare + non-spell Void resistance (+1 -> 0.50x) & Air vulnerability (-1 -> 1.50x)!
        if (!REENTRANT_GUST_HURT.get() && now <= data.getLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL).orElse(0L)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                float earthWardMult = 1.0F;
                if (source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)) {
                    earthWardMult = 0.50F;
                } else if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
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
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                Entity atkEnt = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
                float netherWardMult = 1.0F;
                if (source.is(DamageTypeTags.IS_FREEZING)) {
                    netherWardMult = 0.50F;
                    entity.setTicksFrozen(0);
                } else if (atkEnt instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.FROZEN)) {
                        netherWardMult = 0.50F;
                        entity.setTicksFrozen(0);
                    } else if (atkAffs.contains(EssenceType.WATER)) {
                        netherWardMult = 1.50F;
                    }
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
            if (!source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                float radiantWardMult = 1.0F;
                if (atkRaw instanceof LivingEntity atkLiv && atkLiv != entity) {
                    Set<EssenceType> atkAffs = resolveEntityAffinities(atkLiv);
                    if (atkAffs.contains(EssenceType.UNDEAD)) {
                        radiantWardMult = 0.50F;
                    } else if (atkAffs.contains(EssenceType.UMBRAL)) {
                        radiantWardMult = 1.50F;
                    }
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
        if ((hasActiveMutatedHex(data, now, EssenceType.FROZEN) || hasActiveMutatedHex(data, now, EssenceType.GLACIAL))
                && source.is(DamageTypeTags.IS_FIRE)) {
            clearHexData(entity, data);
            entity.setTicksFrozen(0);
            level.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(0.85D), entity.getZ(),
                    16, 0.4D, 0.45D, 0.4D, 0.05D);
            level.playSound(null, entity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.70F, 1.35F);
        } else if (!REENTRANT_GUST_HURT.get()
                && hasActiveMutatedHex(data, now, EssenceType.FROZEN)
                && !source.is(DamageTypeTags.IS_FIRE)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            float mult = 1.0F + 0.50F * getHexStackMultiplier(entity);
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
                    applyTimedAttackDebuff(attacker, level, 0.0D, 0.30D, 100L); // True -30% Attack Speed on Players!
                    CompoundTag atkData = EntityHelper.getPersistentData(attacker);
                    atkData.putLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_UNTIL, now + 100L); // True -30% Attack Speed on Mobs!
                    atkData.putLong(SpellLingeringZoneManager.TAG_CHILLED_SWING_NEXT_ALLOWED, now + 29L);
                    attacker.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 0));
                    attacker.setTicksFrozen(Math.min(attacker.getTicksRequiredToFreeze() + 60, attacker.getTicksFrozen() + 60));
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

        // Glacial Permafrost Cryo-Shroud (#15) & Taiga Cold Immunity (#19)
        boolean glacialShroud = now <= data.getLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL).orElse(0L);
        boolean taigaColdImmune = now <= data.getLong(SpellLingeringZoneManager.TAG_TAIGA_COLD_IMMUNE_UNTIL).orElse(0L);
        if ((glacialShroud || taigaColdImmune) && source.is(DamageTypeTags.IS_FREEZING)) {
            entity.setTicksFrozen(0);
            if (glacialShroud) {
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
            if (!REENTRANT_GUST_HURT.get()) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            }
        }

        // Oasis Succulent Thorn Carapace (#20): 20% physical damage reduction & thorn retaliation!
        if (!REENTRANT_GUST_HURT.get()
                && now <= data.getLong(SpellLingeringZoneManager.TAG_OASIS_CARAPACE_UNTIL).orElse(0L)
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
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
            if (!REENTRANT_GUST_HURT.get()) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
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
            if (!REENTRANT_GUST_HURT.get()) {
                syncEntityBarrierState(level, entity, hitFlare, hitOrigin);
            }
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
        return t == EssenceType.WATER || t == EssenceType.STORM || t == EssenceType.VAPOR || t == EssenceType.ABYSS
                || t == EssenceType.SPRING || t == EssenceType.MIASMA || t == EssenceType.GENESIS;
    }

    private static boolean isLightningLine(EssenceType t) {
        return t == EssenceType.LIGHTNING || t == EssenceType.STATIC || t == EssenceType.ASTRAL;
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
        int rawScore = VisInteractionManager.getInstance().calculateTotalVisScore(
                List.of(attackMateria.getSerializedName()),
                defenderAffinities
        );
        // Section 6.12, 6.13, 6.18 & 6.3 Mutually Volatile Clashes (Arid <-> Water, Lightning <-> Water, Dust <-> Water/Void):
        // Both sides deal +1 offensive volatile clash damage when attacking the other instead of cancelling (+1 - 1 = 0)!
        if ((attackMateria == EssenceType.ARID || attackMateria == EssenceType.LIGHTNING)
                && defenderAffinities.contains(EssenceType.WATER)) {
            rawScore += 1;
        } else if (attackMateria == EssenceType.DUST
                && (defenderAffinities.contains(EssenceType.WATER) || defenderAffinities.contains(EssenceType.VOID))) {
            rawScore += 1;
        } else if (attackMateria == EssenceType.WATER
                && (defenderAffinities.contains(EssenceType.ARID)
                    || defenderAffinities.contains(EssenceType.LIGHTNING)
                    || defenderAffinities.contains(EssenceType.DUST))) {
            rawScore += 1;
        } else if (attackMateria == EssenceType.VOID && defenderAffinities.contains(EssenceType.DUST)) {
            rawScore += 1;
        }
        return rawScore;
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
            syncEntityBarrierState(level, entity, computeHitFlare(entity, amount), resolveHitOrigin(entity, source));
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
                syncEntityBarrierState(level, entity, computeHitFlare(entity, amount), resolveHitOrigin(entity, source));
                return EventResult.interruptFalse();
            }
            if (source.getDirectEntity() instanceof LivingEntity termAtk && termAtk != entity) {
                termAtk.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
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
            syncEntityBarrierState(level, entity, computeHitFlare(entity, amount), resolveHitOrigin(entity, source));
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
     * Section 6.5 Pure Earth Materia Damage Helper:
     * Applies true Earth Materia spell damage including canonical Earth Advantage ({@code +1 vs Void}, {@code -1 vs Air}),
     * optional <i>Dense Tectonic Mass</i> ({@code +0.4} advantage), <i>Sundering Hex</i> {@code 2.0x} Earth damage doubling,
     * Armor Shred amplification, and Elemental Hex Counter-Cleansing.
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
        int advScore = defenderAffs.isEmpty() ? 0 : calculateSpellElementalAdvantageScore(EssenceType.EARTH, defenderAffs);
        float totalScore = advScore + (denseTectonicMass ? 0.40F : 0.0F);
        if (totalScore != 0.0F) {
            float advMult = Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F) + (denseTectonicMass ? 0.15F : 0.0F);
            damage *= advMult;
        } else if (denseTectonicMass) {
            damage *= 1.15F;
        }

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (hasActiveMutatedHex(targetData, now, EssenceType.EARTH)) {
            damage *= 2.0F; // Section 6.5 Sundering Hex: doubles (2.0x) all incoming Earth Materia damage!
        }

        float materiaResist = getTargetMateriaResistance(target, EssenceType.EARTH);
        if (materiaResist != 0.0F) {
            damage *= Math.clamp(1.0F - materiaResist, 0.20F, 1.50F);
        }

        if (now <= targetData.getLong(TAG_ARMOR_SHRED_UNTIL).orElse(0L)) {
            int shredPts = targetData.getInt(TAG_ARMOR_SHRED).orElse(0);
            if (shredPts > 0) {
                damage *= (1.0F + 0.04F * Math.min(8, shredPts));
            }
        }

        if (denseTectonicMass) {
            applyTimedAttributeShred(target, level, 2.0D, 0.0D, 140L);
        }

        DamageSource magicSrc = caster != null
                ? level.damageSources().indirectMagic(caster, caster)
                : level.damageSources().magic();
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, Math.max(0.5F, damage));
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
     * {@code -1 vs Nether & Undead}), optional <i>Permafrost Creep</i> ({@code +0.4} advantage against Water targets),
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
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        int advScore = defenderAffs.isEmpty() ? 0 : calculateSpellElementalAdvantageScore(EssenceType.FROZEN, defenderAffs);
        boolean isWaterTarget = defenderAffs.contains(EssenceType.WATER)
                || SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                || level.getGameTime() <= EntityHelper.getPersistentData(target).getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)
                || (target instanceof ddraig.net.entropica.entity.SpellEidolonEntity eid && eid.getEffectiveMateria() == EssenceType.WATER);
        float totalScore = advScore + ((permafrostCreep && isWaterTarget) ? 0.40F : 0.0F);
        if (totalScore != 0.0F) {
            float advMult = Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F) + (permafrostCreep ? 0.15F : 0.0F);
            damage *= advMult;
        } else if (permafrostCreep) {
            damage *= 1.15F;
        }

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)) {
            // Section 6.7 Immolation Shroud: +1 resistance vs incoming Frozen attacks (0.75 * 0.67 = 0.50x)!
            damage *= 0.67F;
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
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, Math.max(0.5F, damage));
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
        float totalScore = defenderAffs.isEmpty() ? 0.0F : calculateSpellElementalAdvantageScore(EssenceType.NETHER, defenderAffs);
        if (includeHeldWeaponSynergy && caster != null) {
            totalScore += resolveHeldWeaponAdvantageScore(caster, target);
        }
        if (totalScore != 0.0F) {
            damage *= Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
        }

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)) {
            damage *= 1.50F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)) {
            damage *= 0.67F; // Combined with -1 Water affinity (0.75 * 0.67 = 0.50x)!
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
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, Math.max(0.5F, damage));
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
        float totalScore = defenderAffs.isEmpty() ? 0.0F : calculateSpellElementalAdvantageScore(EssenceType.RADIANT, defenderAffs);
        if (includeHeldWeaponSynergy && caster != null) {
            totalScore += resolveHeldWeaponAdvantageScore(caster, target);
        }
        if (totalScore != 0.0F) {
            damage *= Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
        }

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
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, Math.max(0.5F, damage));
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
        float totalScore = defenderAffs.isEmpty() ? 0.0F : calculateSpellElementalAdvantageScore(EssenceType.UMBRAL, defenderAffs);
        if (includeHeldWeaponSynergy && caster != null) {
            totalScore += resolveHeldWeaponAdvantageScore(caster, target);
        }
        if (totalScore != 0.0F) {
            damage *= Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
        }

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_RADIANT_SHROUD_UNTIL).orElse(0L)) {
            damage *= 1.50F;
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
        boolean hurt = hurtWithPureMateria(level, target, magicSrc, Math.max(0.5F, damage));
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
        float totalScore = defenderAffs.isEmpty() ? 0.0F : calculateSpellElementalAdvantageScore(EssenceType.UNDEAD, defenderAffs);
        if (creepingTombDust) {
            totalScore += 0.4F;
        }
        if (includeHeldWeaponSynergy && caster != null) {
            totalScore += resolveHeldWeaponAdvantageScore(caster, target);
        }
        if (totalScore != 0.0F) {
            float advMult = Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
            if (creepingTombDust) {
                advMult += SpellFlowModifier.SLOW_HEAVY.getInnateAdvantageBonus();
            }
            damage *= advMult;
        } else if (creepingTombDust) {
            damage *= 1.15F;
        }

        CompoundTag targetData = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL).orElse(0L)) {
            damage *= 1.25F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL).orElse(0L)) {
            damage *= 1.50F;
        }
        if (now <= targetData.getLong(SpellLingeringZoneManager.TAG_RADIANT_SHROUD_UNTIL).orElse(0L)) {
            damage *= 0.50F;
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
        float totalScore = defenderAffs.isEmpty() ? 0.0F : calculateSpellElementalAdvantageScore(EssenceType.VOID, defenderAffs);
        if (includeHeldWeaponSynergy && caster != null) {
            totalScore += resolveHeldWeaponAdvantageScore(caster, target);
        }
        if (totalScore != 0.0F) {
            damage *= Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
        }

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
        float totalScore = defenderAffs.isEmpty() ? 0.0F : calculateSpellElementalAdvantageScore(EssenceType.ARID, defenderAffs);
        if (!defenderAffs.contains(EssenceType.WATER) && SpellLingeringZoneManager.isEntityWetOrSoaked(target)) {
            totalScore += 1.0F; // Volatile desiccation bonus against soaked/wet targets!
        }
        if (includeHeldWeaponSynergy && caster != null) {
            totalScore += resolveHeldWeaponAdvantageScore(caster, target);
        }
        if (totalScore != 0.0F) {
            damage *= Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
        }

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
        float totalScore = defenderAffs.isEmpty() ? 0.0F : calculateSpellElementalAdvantageScore(EssenceType.LIGHTNING, defenderAffs);
        if (includeHeldWeaponSynergy && caster != null) {
            totalScore += resolveHeldWeaponAdvantageScore(caster, target);
        }
        if (totalScore != 0.0F) {
            damage *= Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
        }

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
        float totalScore = defenderAffs.isEmpty() ? 0.0F : calculateSpellElementalAdvantageScore(EssenceType.MAGMA, defenderAffs);
        if (includeHeldWeaponSynergy && caster != null) {
            totalScore += resolveHeldWeaponAdvantageScore(caster, target);
        }
        if (totalScore != 0.0F) {
            damage *= Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
        }

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
        float totalScore = defenderAffs.isEmpty() ? 0.0F : calculateSpellElementalAdvantageScore(EssenceType.STORM, defenderAffs);
        if (includeHeldWeaponSynergy && caster != null) {
            totalScore += resolveHeldWeaponAdvantageScore(caster, target);
        }
        if (totalScore != 0.0F) {
            damage *= Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
        }

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
        float totalScore = defenderAffs.isEmpty() ? 0.0F : calculateSpellElementalAdvantageScore(EssenceType.GLACIAL, defenderAffs);
        if (permafrostCreep) {
            totalScore += 0.40F;
        }
        if (includeHeldWeaponSynergy && caster != null) {
            totalScore += resolveHeldWeaponAdvantageScore(caster, target);
        }
        if (totalScore != 0.0F) {
            float advMult = Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F) + (permafrostCreep ? 0.15F : 0.0F);
            damage *= advMult;
        } else if (permafrostCreep) {
            damage *= 1.15F;
        }

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
        float totalScore = defenderAffs.isEmpty() ? 0.0F : calculateSpellElementalAdvantageScore(EssenceType.OVERGROWTH, defenderAffs);
        if (includeHeldWeaponSynergy && caster != null) {
            totalScore += resolveHeldWeaponAdvantageScore(caster, target);
        }
        if (totalScore != 0.0F) {
            damage *= Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
        }

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
        float totalScore = defenderAffs.isEmpty() ? 0.0F : calculateSpellElementalAdvantageScore(EssenceType.DUST, defenderAffs);
        if (includeHeldWeaponSynergy && caster != null) {
            totalScore += resolveHeldWeaponAdvantageScore(caster, target);
        }
        if (totalScore != 0.0F) {
            damage *= Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
        }

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
        float totalScore = defenderAffs.isEmpty() ? 0.0F : calculateSpellElementalAdvantageScore(EssenceType.SPORE, defenderAffs);
        if (includeHeldWeaponSynergy && caster != null) {
            totalScore += resolveHeldWeaponAdvantageScore(caster, target);
        }
        if (totalScore != 0.0F) {
            damage *= Math.clamp(1.0F + (totalScore * 0.25F), 0.50F, 1.85F);
        }

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
     * Guide Section 4.4 Touch / Melee Strike Weapon Synergy:
     * When a Touch spell is cast through a Materia-attuned weapon ({@code ModDataComponents.ESSENCE_STATS}),
     * the weapon's innate Materia Advantage stacks additively with the spell's Materia!
     */
    private static float resolveHeldWeaponAdvantageScore(LivingEntity caster, LivingEntity target) {
        ItemStack held = caster.getMainHandItem();
        if (held.isEmpty()) return 0.0F;
        EssenceCombatStats stats = held.get(ModDataComponents.ESSENCE_STATS.get());
        if (stats == null || stats.attackBonuses().isEmpty()) return 0.0F;
        Set<EssenceType> defenderAffs = resolveEntityAffinities(target);
        if (defenderAffs.isEmpty()) return 0.0F;
        int score = 0;
        for (EssenceType atkType : stats.attackBonuses().keySet()) {
            if (atkType != null && atkType != EssenceType.REGULAR) {
                score += calculateSpellElementalAdvantageScore(atkType, defenderAffs);
            }
        }
        return Math.clamp(score, -2, 2);
    }

    private static boolean isNecroticLine(EssenceType t) {
        return t == EssenceType.UNDEAD || t == EssenceType.BLIGHT || t == EssenceType.MIASMA
                || t == EssenceType.WRAITH || t == EssenceType.BARROW || t == EssenceType.OBLIVION;
    }
}
