package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellEidolonSpecs;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 * Complete per-part Signature Mutation implementation for <b>Pyre Materia (Tier 3 Trinity Fusion #39)</b>
 * (Spellcrafting System Guide Section 6.40 — Trinity Earth + Nether + Nature harmonic matrix):
 * <ul>
 *     <li><b>Palette</b>: Charred Cedar / Granite Coal Brown ({@code #773214}) + Roaring Wildfire Red-Orange ({@code #FF4500})</li>
 *     <li><b>Advantage</b>: Strong vs <b>VOID (+1)</b>, <b>FROZEN (+1)</b>, <b>NATURE (+1)</b>, <b>GLACIAL (+1)</b>,
 *         and <b>RIME (+1)</b> (plus conditional <b>WATER</b>, <b>EARTH</b>) | Weak vs <b>AIR (-1)</b></li>
 * </ul>
 */
public final class PyreSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int PYRE_GRANITE_COAL_HEX_COLOR = 0x773214;
    public static final int PYRE_HEARTH_HEX_COLOR = 0xFF4500;

    private static final String TAG_PYRE_BEAM_LAST_TICK = "PyreBeamLastTick";
    private static final String TAG_PYRE_BEAM_TICKS = "PyreBeamContactTicks";
    private static final String TAG_PYRE_CONE_LAST_TICK = "PyreConeLastTick";
    private static final String TAG_PYRE_CONE_STACKS = "PyreConeArmorStacks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.PYRE;
    }

    // =========================================================================
    // TITLES & CODEX / TOOLTIP DESCRIPTIONS
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE ? "Cremating Wildfire Orbit Flow" : null;
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE
                ? "Swirling rings of incandescent coal embers and burning cedar chunks (#773214 / #FF4500) grant +3 Armor, burn melee attackers for 4s, melt incoming wooden arrows into ash, and launch burning coal darts on melee hits."
                : null;
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Combustion Hearth Orb";
            case BEAM -> "Cremating Wildfire Jetstream Beam";
            case RADIAL_BURST -> "Raging Wildfire Pyre Nova";
            case TOUCH_STRIKE -> "Cremating Timber-Pyre Cleave";
            case AURA -> "Pyre Hearth Hearthstone Shroud";
            case GLYPH -> "Cremation Pyre-Pitfall Glyph";
            case CONE -> "Combustion Hearth Flame-Thrower";
            case SKYFALL -> "Cremating Pyre Cataclysm Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Hurls a 3D compressed volcanic coal & wildfire orb (#773214 / #FF4500) that detonates into a thermal firestorm, dealing Pyre Materia damage, igniting foes for 5.0s, knocking them 5m backward, and melting nearby ice/snow into steam.";
            case BEAM ->
                    "Channels a superheated coal-fire jetstream that disintegrates non-flameproof projectiles and melts enemy armor (-1 per 1.5s); 3.0s sustained contact triggers Cataclysmic Flashover, detonating caught foes and stunning adjacent mobs for 1.5s.";
            case RADIAL_BURST ->
                    "Detonates a 6m+ wildfire nova knocking enemies 7m back, igniting them for 6.0s, charring nearby plant matter into charcoal, melting ice/snow, and boosting caster melee damage by +25% for 8.0s.";
            case TOUCH_STRIKE ->
                    "Sweeping blazing petrified-timber cleave stacking weapon Materia Advantage, igniting victims for 6.0s, melting 3 armor points, knocking 5m back, and leaving a burning ember crescent on the ground.";
            case AURA ->
                    "Envelops caster (or anchored area) in glowing coals & burning cedar (+8 Armor, +2 Toughness, -10% speed), melting ice/snow underfoot and granting complete Fire, Lava, and Freezing immunity.";
            case GLYPH ->
                    "Disarmable Single/Repeating smoldering sigil that erupts into a 4m bonfire pitfall for 4.0s, stripping 3 armor points, locking jumping/escape, and continuously searing trapped foes.";
            case CONE ->
                    "Continuous flamethrower cone melting ice/snow, shredding enemy armor (-1 per 2s), dealing Pyre Materia damage, cooking dropped meat, and smelting sand into glass.";
            case SKYFALL ->
                    "Calls down a 10s meteoritic rain of 3D blazing petrified cedar logs & volcanic coal boulders across 8m+, melting -2 armor per hit, igniting mobs in Wildfire, and leaving temporary fiery craters once complete.";
            case SUMMON_AGENT ->
                    "Summons a Pyre-Juggernaut forged of burning petrified logs and molten coal that delivers sweeping wildfire cleaves.";
            case SUMMON_SENTRY ->
                    "Anchors a Cremation Pyre Spire that sprays 8m wildfire jets at hostiles and radiates +4 Armor to nearby summons.";
            case SUMMON_DECOY ->
                    "Conjures a Smoldering Hearth Effigy that taunts hostiles and detonates into a 6m Raging Wildfire Nova upon destruction.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Wildfire Flashover Step";
            case TRANSMUTE, BREAK -> "Pyre Ash-Charcoal Transmute";
            case AFFLICTION -> "Unquenchable Wildfire Cremation Hex";
            default -> "Cremating Pyre " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Teleports up to 14m in a blazing flash, leaving a 3.0s taunting burning decoy at departure and granting Fire Immunity and +30% sprint speed for 4.0s on arrival.";
            case TRANSMUTE ->
                    "5.0s catalytic thermal carbonization: converts wood logs into coal blocks (+20% charcoal yield), smelts sand into glass, cooks raw foods, enriches dirt/grass into fertile ash-soil, and turns cobble into smooth stone or magma.";
            case BREAK ->
                    "Thermal excavation auto-smelting mined blocks in-world while vaporizing exposed water pockets into steam.";
            case AFFLICTION ->
                    "Milk-immune subterranean coal-fire curse burning even underwater, shredding 40% Armor Toughness, blocking sprinting and natural health regen, and showering nearby allies in incendiary embers when struck.";
            default -> "Infuses " + effect.getDisplayName() + " with Tier 3 Trinity Pyre Materia.";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — CREMATING WILDFIRE ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D);
        level.sendParticles(ParticleTypes.ASH, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
        if (flow == SpellFlowModifier.ORBIT_SATELLITE && projectile.tickCount % 5 == 0) {
            LivingEntity owner = projectile.getOwner() instanceof LivingEntity liv ? liv : null;
            for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, projectile.getBoundingBox().inflate(1.6D))) {
                if (owner != null && arrow.getOwner() == owner) continue;
                level.sendParticles(ParticleTypes.ASH, arrow.getX(), arrow.getY(), arrow.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.02D);
                level.sendParticles(ParticleTypes.FLAME, arrow.getX(), arrow.getY(), arrow.getZ(), 4, 0.08D, 0.08D, 0.08D, 0.02D);
                arrow.discard();
            }
        }
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.LAVA, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.01D);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 1 — COMBUSTION HEARTH ORB (PROJECTILE)
    // -------------------------------------------------------------------------

    @Override
    public void onProjectileHitEntityFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.40 Combustion Hearth Orb:
        // Detonates into a thermal firestorm on impact, dealing Pyre Materia damage, setting targets ablaze with
        // intense Wildfire for 5.0s, knocking enemies 5 blocks backward, and instantly melting nearby ice & snow into steam!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        Vec3 dir = projectile.getDeltaMovement().lengthSqr() > 1.0E-4D
                ? projectile.getDeltaMovement().normalize()
                : (caster != null ? caster.getLookAngle() : new Vec3(0.0D, 0.0D, 1.0D));

        if (hitEntity instanceof LivingEntity victim && victim.isAlive()
                && !SpellExecutionEngine.isFriendlyAllyForOasis(caster, victim)
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, wardSafe))) {
            victim.igniteForSeconds(5);
            EntityHelper.getPersistentData(victim).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 100L);
            Vec3 kb = dir.scale(1.25D); // ~5m knockback!
            victim.setDeltaMovement(kb.x, 0.34D, kb.z);
            victim.hasImpulse = true;
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(level, victim, caster, 3.40F + tier * 0.90F, false, true);
            }
        }

        detonateThermalFirestorm(level, caster, hitEntity.position(), hitEntity, spell, slot, tier, wardSafe);
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
        detonateThermalFirestorm(level, caster, blockHit.getLocation(), null, spell, slot,
                spell.getEffectiveTier(), spell.hasWardFriendlyFireSafeguard());
    }

    private static void detonateThermalFirestorm(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            @Nullable Entity directTarget,
            SpellAssembly spell,
            SubSpellSlot slot,
            int tier,
            boolean wardSafe
    ) {
        long now = level.getGameTime();
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, center.add(0.0D, 0.2D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.PYRE, EssenceType.NETHER, 1.85F, tier
        );
        float splashDmg = 2.45F + tier * 0.65F;
        AABB stormBox = new AABB(
                center.x - 3.5D, center.y - 1.5D, center.z - 3.5D,
                center.x + 3.5D, center.y + 2.8D, center.z + 3.5D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, stormBox, LivingEntity::isAlive)) {
            if (foe == caster || foe == directTarget || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            foe.igniteForSeconds(5);
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 100L);
            Vec3 away = foe.position().subtract(center);
            Vec3 push = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(1.10D) : new Vec3(0.0D, 0.30D, 0.0D);
            foe.setDeltaMovement(push.x, 0.30D, push.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(level, foe, caster, splashDmg, false, true);
        }

        meltIceAndSnowToSteam(level, BlockPos.containing(center), 3);
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.4D, center.z, 26, 1.2D, 0.5D, 1.2D, 0.06D);
        level.sendParticles(ParticleTypes.LAVA, center.x, center.y + 0.4D, center.z, 10, 0.8D, 0.4D, 0.8D, 0.04D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.95F, 0.82F);
    }

    public static void meltIceAndSnowToSteam(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 2, r))) {
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.PACKED_ICE) || bs.is(Blocks.BLUE_ICE)
                    || bs.is(Blocks.SNOW) || bs.is(Blocks.SNOW_BLOCK) || bs.is(Blocks.POWDER_SNOW)) {
                level.removeBlock(p, false);
                level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 3, 0.2D, 0.2D, 0.2D, 0.02D);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 2 — CREMATING WILDFIRE JETSTREAM BEAM (BEAM)
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
        // Section 6.40 Cremating Wildfire Jetstream Beam:
        // Continuously cremates caught targets with Pyre Materia damage, melts enemy armor (-1 armor per 1.5s),
        // and disintegrates non-flameproof incoming projectiles; sustained channeling for 3.0s (60t) triggers
        // Cataclysmic Flashover detonating caught enemies in a fiery blast, igniting adjacent mobs & stunning 1.5s (30t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 eye = caster.getEyePosition();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        AABB corridor = new AABB(eye, eye.add(dirNorm.scale(maxReach))).inflate(1.25D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, corridor,
                p -> p.getOwner() != caster && !(p instanceof SpellProjectileEntity))) {
            if (!proj.fireImmune()) {
                level.sendParticles(ParticleTypes.ASH, proj.getX(), proj.getY(), proj.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.02D);
                level.sendParticles(ParticleTypes.FLAME, proj.getX(), proj.getY(), proj.getZ(), 4, 0.08D, 0.08D, 0.08D, 0.02D);
                proj.discard();
            }
        }

        if (blockHit.getType() == BlockHitResult.Type.BLOCK) {
            meltIceAndSnowToSteam(level, blockHit.getBlockPos(), 2);
        }

        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.igniteForSeconds(4);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 80L);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_PYRE_BEAM_LAST_TICK).orElse(-100L);
            if (!slot.hasHarmVerb()) {
                float jetDmg = (1.50F + tier * 0.42F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(level, hit, caster, jetDmg, false, true);
            }
            if (last == now) {
                continue;
            }
            long delta = now - last;
            int contactTicks = heldTicks <= 0
                    ? 0
                    : ((delta > 0L && delta <= 16L) ? data.getInt(TAG_PYRE_BEAM_TICKS).orElse(0) + (int) delta : 4);
            data.putLong(TAG_PYRE_BEAM_LAST_TICK, now);
            data.putInt(TAG_PYRE_BEAM_TICKS, contactTicks);

            // -1 armor per 1.5s (30t) of sustained beam crematory heat!
            double meltedArmor = Math.min(6.0D, Math.max(1.0D, contactTicks / 30.0D));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, meltedArmor, 0.0D, 100L);

            if (heldTicks > 0 && contactTicks >= 60) {
                data.putInt(TAG_PYRE_BEAM_TICKS, 0);
                SpellWorldVfxManager.broadcastRadialNova(
                        level, hit.position(), 4.5D, EssenceType.PYRE, EssenceType.NETHER, tier
                );
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                float flashoverDmg = (6.80F + tier * 1.85F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(level, hit, caster, flashoverDmg, false, true);

                for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(4.5D),
                        e -> e.isAlive() && e != caster && e != hit)) {
                    if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, adj)) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, wardSafe)) continue;
                    adj.igniteForSeconds(6);
                    EntityHelper.getPersistentData(adj).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
                    adj.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                    SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(level, adj, caster, flashoverDmg * 0.85F, false, true);
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.35F, 0.05F),
                        hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.LAVA, hit.getX(), hit.getY(0.8D), hit.getZ(), 20, 1.4D, 0.6D, 1.4D, 0.08D);
                level.playSound(null, hit.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.95F, 1.12F);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 3 — RAGING WILDFIRE PYRE NOVA (RADIAL BURST)
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
        // Section 6.40 Raging Wildfire Pyre Nova:
        // 6m+ radius — knocks enemies 7m backward, sets struck hostiles ablaze for 6.0s, incinerates nearby plant matter
        // into ash/charcoal, smothers/melts ice & snow, and bolsters caster melee damage by +25% for 8.0s (160t)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.PYRE, EssenceType.NETHER, tier);

        float novaDmg = (3.40F + tier * 0.88F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 delta = foe.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.68D) : new Vec3(0.0D, 0.40D, 0.0D);
            foe.setDeltaMovement(away.x, 0.40D, away.z); // ~7m knockback!
            foe.hasImpulse = true;
            foe.igniteForSeconds(6);
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(level, foe, caster, novaDmg, false, true);
            }
        }

        BlockPos cp = BlockPos.containing(center);
        meltIceAndSnowToSteam(level, cp, 5);
        for (BlockPos p : BlockPos.betweenClosed(cp.offset(-4, -1, -4), cp.offset(4, 2, 4))) {
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(BlockTags.SMALL_FLOWERS) || bs.is(Blocks.SHORT_GRASS) || bs.is(Blocks.FERN)
                    || bs.is(Blocks.TALL_GRASS) || bs.is(Blocks.LARGE_FERN) || bs.is(Blocks.DEAD_BUSH) || bs.is(Blocks.VINE)) {
                level.removeBlock(p, false);
                level.sendParticles(ParticleTypes.ASH, p.getX() + 0.5D, p.getY() + 0.4D, p.getZ() + 0.5D, 4, 0.2D, 0.2D, 0.2D, 0.02D);
                if (level.random.nextFloat() < 0.25F) {
                    level.addFreshEntity(new ItemEntity(level, p.getX() + 0.5D, p.getY() + 0.3D, p.getZ() + 0.5D, new ItemStack(Items.CHARCOAL)));
                }
            }
        }

        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_PYRE_MELEE_BOOST_UNTIL, now + 160L);
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.8D, center.z, 48, 2.3D, 0.6D, 2.3D, 0.08D);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.x, center.y + 1.0D, center.z, 22, 1.8D, 0.5D, 1.8D, 0.03D);
        level.playSound(null, cp, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.05F, 0.78F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 4 — CREMATING TIMBER-PYRE CLEAVE (TOUCH / STRIKE)
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
        // Section 6.40 Cremating Timber-Pyre Cleave:
        // Sweeping horizontal cleave of blazing petrified wood & roaring coal flames (#773214 / #FF4500);
        // deals devastating Pyre Materia damage, ignites for 6.0s, melts 3 armor points, knocks 5m backward,
        // and leaves a crescent arc of burning embers on the ground that damages enemies walking across it!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), push, 3.2D,
                EssenceType.PYRE, EssenceType.NETHER, tier
        );

        float cleaveDmg = (3.85F + tier * 0.98F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.igniteForSeconds(6);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            hit.setDeltaMovement(push.x * 1.25D, 0.34D, push.z * 1.25D); // ~5m knockback!
            hit.hasImpulse = true;
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(level, hit, caster, cleaveDmg, true, true);
            }
        }

        Vec3 arcCenter = caster.position().add(push.scale(2.4D));
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, arcCenter.add(0.0D, 0.04D, 0.0D), 2.6D, EssenceType.PYRE, EssenceType.NETHER, 80, tier
        );
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, arcCenter, 2.6D, 80, now, spell, slot, EssenceType.PYRE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                        AABB box = new AABB(
                                zone.center.x - 2.6D, zone.center.y - 1.0D, zone.center.z - 2.6D,
                                zone.center.x + 2.6D, zone.center.y + 1.8D, zone.center.z + 2.6D
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zSafe)) continue;
                            foe.igniteForSeconds(3);
                            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, srv.getGameTime() + 60L);
                            SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(srv, foe, owner, 2.15F + zTier * 0.45F, false, true);
                        }
                        srv.sendParticles(ParticleTypes.FLAME, zone.center.x, zone.center.y + 0.15D, zone.center.z, 10, 1.1D, 0.12D, 1.1D, 0.02D);
                        srv.sendParticles(ParticleTypes.LAVA, zone.center.x, zone.center.y + 0.15D, zone.center.z, 3, 0.9D, 0.1D, 0.9D, 0.02D);
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 5 — PYRE HEARTH HEARTHSTONE SHROUD (AURA / SELF)
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
        // Section 6.40 Pyre Hearth Hearthstone Shroud:
        // Heavy armor plates of glowing hot coals & burning cedar timber (#773214 / #FF4500);
        // grants +8 Armor, +2 Toughness, -10% move speed, melts ice/snow underfoot, & complete Fire/Lava/Freezing immunity!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_PYRE_SHROUD_UNTIL, level.getGameTime() + dur);
        caster.clearFire();
        caster.setTicksFrozen(0);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 8.0D, 2.0D, 0.0D, -0.10D, dur);
        meltIceAndSnowToSteam(level, caster.blockPosition(), 2);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
        level.sendParticles(ParticleTypes.FLAME, caster.getX(), caster.getY(0.8D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.04D);
        level.playSound(null, caster.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.90F, 0.85F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 6 — CREMATION PYRE-PITFALL GLYPH (GLYPH / TRAP)
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
        // Section 6.40 Cremation Pyre-Pitfall Glyph:
        // Disarmable Single/Repeating hidden sigil of smoldering embers & cracked earth (#773214 / #FF4500);
        // erupts into a 4m bonfire pitfall of roaring wildfire for 4.0s (80t), trapping caught mobs inside with
        // continuous Pyre Materia damage, stripping 3 armor points, and preventing jumping or escape!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.5D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.PYRE, EssenceType.NETHER, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.PYRE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.PYRE, EssenceType.NETHER, rem, tier
                        );
                    }
                    if (elapsed % 12 == 0) {
                        srv.sendParticles(ParticleTypes.SMOKE, zone.center.x, zone.center.y + 0.12D, zone.center.z, 4, 0.85D, 0.1D, 0.85D, 0.01D);
                    }

                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    AABB trigger = new AABB(
                            zone.center.x - 2.4D, zone.center.y - 1.0D, zone.center.z - 2.4D,
                            zone.center.x + 2.4D, zone.center.y + 2.0D, zone.center.z + 2.4D
                    );
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, trigger, e ->
                            e.isAlive() && e != owner
                                    && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    if (victims.isEmpty()) return true;

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 4.0D, EssenceType.PYRE, EssenceType.NETHER, tier);
                    SpellWorldVfxManager.broadcastArcaneSeal(srv, zone.center.add(0.0D, 0.04D, 0.0D), 4.0D, EssenceType.PYRE, EssenceType.NETHER, 80, tier);
                    meltIceAndSnowToSteam(srv, BlockPos.containing(zone.center), 4);

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.0D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                boolean sSafe = sZone.spell != null && sZone.spell.hasWardFriendlyFireSafeguard();
                                int sTier = sZone.spell != null ? sZone.spell.getEffectiveTier() : tier;
                                AABB pitBox = new AABB(
                                        sZone.center.x - 4.0D, sZone.center.y - 1.8D, sZone.center.z - 4.0D,
                                        sZone.center.x + 4.0D, sZone.center.y + 2.5D, sZone.center.z + 4.0D
                                );
                                long now = sLvl.getGameTime();
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, pitBox, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellExecutionEngine.isFriendlyAllyForOasis(sOwner, v)) continue;
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sSafe)) continue;

                                    v.igniteForSeconds(4);
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 80L);
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 25L);
                                    Vec3 inward = sZone.center.subtract(v.position()).scale(0.26D);
                                    v.setDeltaMovement(inward.x, Math.min(-0.14D, v.getDeltaMovement().y), inward.z);
                                    v.hasImpulse = true;
                                    if (sEl == 0) {
                                        SpellCombatReactionEngine.applyTimedAttributeShred(v, sLvl, 3.0D, 0.0D, 160L);
                                    }
                                    if (sEl % 20 == 0) {
                                        float pitDmg = (2.60F + sTier * 0.65F) * Math.max(0.5F, sZone.apparatusPotency);
                                        SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(sLvl, v, sOwner, pitDmg, false, true);
                                        if (sZone.spell != null && sZone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    sLvl, sOwner, v, Vec3.ZERO,
                                                    sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.55F
                                            );
                                        }
                                    }
                                }
                                if (sEl % 5 == 0) {
                                    sLvl.sendParticles(ParticleTypes.FLAME, sZone.center.x, sZone.center.y + 0.3D, sZone.center.z, 18, 1.6D, 0.3D, 1.6D, 0.04D);
                                    sLvl.sendParticles(ParticleTypes.LAVA, sZone.center.x, sZone.center.y + 0.25D, sZone.center.z, 4, 1.2D, 0.2D, 1.2D, 0.03D);
                                }
                                return true;
                            }
                    ));
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 0.82F);
                    triggersLeft[0]--;
                    rearmCooldown[0] = 95;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 7 — COMBUSTION HEARTH FLAME-THROWER (CONE / SPRAY)
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
        // Section 6.40 Combustion Hearth Flame-Thrower:
        // Melts ice, shreds enemy armor (-1 armor per 2s), deals continuous Pyre Materia damage,
        // and instantly cooks dropped meat & smelts sand into glass!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), dirNorm, coneRange,
                EssenceType.PYRE, EssenceType.NETHER, tier
        );

        float coneDmg = (2.85F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.igniteForSeconds(5);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 100L);

            CompoundTag hData = EntityHelper.getPersistentData(hit);
            long last = hData.getLong(TAG_PYRE_CONE_LAST_TICK).orElse(-100L);
            int stacks = (now - last <= 60L) ? Math.min(6, hData.getInt(TAG_PYRE_CONE_STACKS).orElse(0) + 1) : 1;
            hData.putLong(TAG_PYRE_CONE_LAST_TICK, now);
            hData.putInt(TAG_PYRE_CONE_STACKS, stacks);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, stacks, 0.0D, 110L);

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }
        }

        Vec3 eye = caster.getEyePosition();
        AABB itemBox = new AABB(eye, eye.add(dirNorm.scale(coneRange))).inflate(2.0D);
        for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, itemBox, ItemEntity::isAlive)) {
            if (cookOrSmeltDroppedItem(ie)) {
                level.sendParticles(ParticleTypes.FLAME, ie.getX(), ie.getY() + 0.2D, ie.getZ(), 5, 0.15D, 0.15D, 0.15D, 0.02D);
            }
        }
        for (double d = 1.5D; d <= coneRange; d += 1.6D) {
            BlockPos step = BlockPos.containing(eye.add(dirNorm.scale(d)));
            meltIceAndSnowToSteam(level, step, 1);
            for (BlockPos p : BlockPos.betweenClosed(step.offset(-1, -1, -1), step.offset(1, 0, 1))) {
                BlockState bs = level.getBlockState(p);
                if ((bs.is(Blocks.SAND) || bs.is(Blocks.RED_SAND)) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                    level.setBlockAndUpdate(p, Blocks.GLASS.defaultBlockState());
                }
            }
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.95F, 0.88F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 8 — CREMATING PYRE CATACLYSM DOWNPOUR (SKYFALL)
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
        // Section 6.40 Cremating Pyre Cataclysm Downpour:
        // Calls down a 10-second (200t) meteoritic rain of blazing petrified timber logs & roaring volcanic coal boulders
        // across an 8-block+ radius (#773214 / #FF4500) using 3D Skyfall Pseudoblock Columns while active, melting -2 armor
        // per impact, igniting caught mobs in roaring Wildfire, and materializing temporary fiery craters once complete!
        int tier = spell.getEffectiveTier();
        double r = Math.max(8.0D, radius);
        BlockPos centerPos = BlockPos.containing(impactCenter);
        double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.PYRE, EssenceType.NETHER, 210, tier
        );

        final BlockPos[] meteorTargets = new BlockPos[8];
        meteorTargets[0] = findGroundSurfaceNear(level, centerPos).above();
        for (int i = 1; i < 8; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / 7.0D) + level.random.nextDouble() * 0.45D;
            double dist = 0.24D * r + level.random.nextDouble() * (0.58D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            meteorTargets[i] = findGroundSurfaceNear(level, raw).above();
        }

        final boolean[] landed = new boolean[8];
        final boolean[] materialized = {false};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 280, level.getGameTime(), spell, slot, EssenceType.PYRE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int mIdx = 0; mIdx < 8; mIdx++) {
                                boolean timberMeteor = (mIdx % 2 == 0);
                                BlockState pseudoState = timberMeteor
                                        ? ModBlocks.PYRE_ASH_CEDAR_LOG.get().defaultBlockState()
                                        : Blocks.COAL_BLOCK.defaultBlockState();
                                int colHeight = timberMeteor ? 2 : 1;
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, meteorTargets[mIdx], pseudoState, colHeight);
                            }
                        }
                        return false;
                    }

                    if (elapsed < 200) {
                        // Launch 8 falling 3D Blazing Petrified Timber & Volcanic Coal pseudoblocks across the 10.0s (200t) storm!
                        for (int mIdx = 0; mIdx < 8; mIdx++) {
                            int impactTick = 22 + mIdx * 24;
                            BlockPos strikePos = meteorTargets[mIdx];
                            boolean timberMeteor = (mIdx % 2 == 0);
                            BlockState pseudoState = timberMeteor
                                    ? ModBlocks.PYRE_ASH_CEDAR_LOG.get().defaultBlockState()
                                    : Blocks.COAL_BLOCK.defaultBlockState();
                            int colHeight = timberMeteor ? 2 : 1;

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
                                        EssenceType.PYRE,
                                        tier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                LivingEntity owner = zone.resolveCaster(srv);
                                boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                                int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                                float meteorDmg = (4.60F + zTier * 1.15F) * Math.max(0.5F, zone.apparatusPotency);
                                long now = srv.getGameTime();

                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.PYRE, EssenceType.NETHER, 2.25F, zTier
                                );
                                meltIceAndSnowToSteam(srv, strikePos, 3);

                                AABB blastBox = new AABB(
                                        hitVec.x - 4.5D, hitVec.y - 2.0D, hitVec.z - 4.5D,
                                        hitVec.x + 4.5D, hitVec.y + 4.5D, hitVec.z + 4.5D
                                );
                                for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
                                    if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                                    if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                                    foe.igniteForSeconds(6);
                                    EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
                                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 2.0D, 0.0D, 140L);
                                    if (zone.spell != null && zone.slot != null) {
                                        SpellExecutionEngine.deliverSubSpellOnEntity(
                                                srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                                zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.50F
                                        );
                                    }
                                    SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(srv, foe, owner, meteorDmg, false, true);
                                }

                                srv.sendParticles(ParticleTypes.LAVA, hitVec.x, hitVec.y + 0.7D, hitVec.z, 14, 0.9D, 0.5D, 0.9D, 0.06D);
                                srv.sendParticles(ParticleTypes.FLAME, hitVec.x, hitVec.y + 0.9D, hitVec.z, 22, 1.2D, 0.7D, 1.2D, 0.05D);
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 20, 1.0D, 0.5D, 1.0D, 0.09D
                                );
                                srv.playSound(null, strikePos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.85F, 0.92F);
                            }
                        }
                    }
                    for (int mIdx = 0; mIdx < 8; mIdx++) {
                        if (landed[mIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, meteorTargets[mIdx], (mIdx % 2 == 0) ? 2 : 1, false);
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 9 — PYRE-HEARTH COLOSSUS SUITE (SUMMON / EIDOLON)
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
        tag.putString("SpellEidolonMateria", EssenceType.PYRE.name());
        tag.putInt("SpellEidolonTintRGB", PYRE_HEARTH_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.4D, radius), EssenceType.PYRE, EssenceType.NETHER, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.FLAME, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 26, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.40 Wildfire Flashover Step:
        // Teleports up to 14m instantly in a blazing flash, leaving behind a flaming decoy at departure that taunts
        // hostiles and burns enemies for 3.0s (60t), granting Fire Immunity & +30% sprint speed for 4.0s (80t) on arrival!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.PYRE, potencyTier);

        SpellEidolonEntity decoy = SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.PYRE,
                18.0F, 60, "Smoldering Pyre Decoy", null
        );
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, departurePos).inflate(10.0D), Mob::isAlive)) {
            if (mob != decoy && mob.getTarget() == caster && decoy != null) {
                mob.setTarget(decoy);
            }
        }
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 3.0D, 60, level.getGameTime(), null, null, EssenceType.PYRE, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB box = new AABB(zone.center, zone.center).inflate(3.0D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner && e != decoy)) {
                            if (owner != null && SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true)) continue;
                            foe.igniteForSeconds(3);
                            SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(srv, foe, owner, 1.65F + potencyTier * 0.35F, false, true);
                        }
                        srv.sendParticles(ParticleTypes.FLAME, zone.center.x, zone.center.y + 0.8D, zone.center.z, 10, 0.45D, 0.5D, 0.45D, 0.03D);
                    }
                    return true;
                }
        ));

        caster.clearFire();
        caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 80, 0));
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 1));
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 0.0D, 0.0D, 0.0D, 0.30D, 80L);
        level.sendParticles(ParticleTypes.FLAME, departurePos.x, departurePos.y + 0.8D, departurePos.z, 24, 0.35D, 0.45D, 0.35D, 0.05D);
        level.sendParticles(ParticleTypes.LAVA, arrivalPos.x, arrivalPos.y + 0.6D, arrivalPos.z, 12, 0.3D, 0.3D, 0.3D, 0.04D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.95F, 1.08F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.40 Pyre Ash-Charcoal Transmute [5.0s Continuous Catalytic Channel]:
        // Converts wood logs directly into coal blocks (+20% bonus charcoal yield), smelts sand into glass,
        // cooks raw foods, transforms dirt/grass into fertile ash-soil, and converts cobblestone into smooth stone or magma!
        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 5, potencyTier, EssenceType.PYRE, "Pyre Ash-Charcoal Transmute", null)) {
            return true;
        }

        boolean didItem = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D), ItemEntity::isAlive)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didItem = true;
            }
        }
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return didItem;
        }
        if (state.is(BlockTags.LOGS)) {
            level.setBlockAndUpdate(pos, Blocks.COAL_BLOCK.defaultBlockState());
            if (level.random.nextFloat() < 0.20F) {
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, new ItemStack(Items.CHARCOAL, 2)));
            }
            level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 8, 0.25D, 0.25D, 0.25D, 0.03D);
            return true;
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT)) {
            level.setBlockAndUpdate(pos, level.random.nextFloat() < 0.75F
                    ? Blocks.PODZOL.defaultBlockState()
                    : Blocks.ROOTED_DIRT.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.COBBLED_DEEPSLATE)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean()
                    ? Blocks.SMOOTH_STONE.defaultBlockState()
                    : Blocks.MAGMA_BLOCK.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.SAPLINGS) && !state.is(ModBlocks.PYRE_ASH_CEDAR_SAPLING.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.PYRE_ASH_CEDAR_SAPLING.get().defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.SMALL_FLOWERS) && !state.is(ModBlocks.PYRE_SPROUT.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.PYRE_SPROUT.get().defaultBlockState());
            return true;
        }
        return didItem;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(ItemTags.LOGS)) {
            int boosted = Math.min(64, Math.max(stack.getCount() + 1, Math.round(stack.getCount() * 1.20F)));
            itemEntity.setItem(new ItemStack(Items.CHARCOAL, boosted));
            return true;
        }
        if (cookOrSmeltDroppedItem(itemEntity)) {
            return true;
        }
        if (stack.is(Items.COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(level.random.nextBoolean() ? Items.SMOOTH_STONE : Items.MAGMA_BLOCK, stack.getCount()));
            return true;
        }
        if (stack.is(Items.DIRT) || stack.is(Items.GRASS_BLOCK) || stack.is(Items.COARSE_DIRT)) {
            itemEntity.setItem(new ItemStack(Items.PODZOL, stack.getCount()));
            return true;
        }
        return false;
    }

    private static boolean cookOrSmeltDroppedItem(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (stack.is(Items.BEEF)) { itemEntity.setItem(new ItemStack(Items.COOKED_BEEF, stack.getCount())); return true; }
        if (stack.is(Items.PORKCHOP)) { itemEntity.setItem(new ItemStack(Items.COOKED_PORKCHOP, stack.getCount())); return true; }
        if (stack.is(Items.CHICKEN)) { itemEntity.setItem(new ItemStack(Items.COOKED_CHICKEN, stack.getCount())); return true; }
        if (stack.is(Items.MUTTON)) { itemEntity.setItem(new ItemStack(Items.COOKED_MUTTON, stack.getCount())); return true; }
        if (stack.is(Items.RABBIT)) { itemEntity.setItem(new ItemStack(Items.COOKED_RABBIT, stack.getCount())); return true; }
        if (stack.is(Items.COD)) { itemEntity.setItem(new ItemStack(Items.COOKED_COD, stack.getCount())); return true; }
        if (stack.is(Items.SALMON)) { itemEntity.setItem(new ItemStack(Items.COOKED_SALMON, stack.getCount())); return true; }
        if (stack.is(Items.POTATO)) { itemEntity.setItem(new ItemStack(Items.BAKED_POTATO, stack.getCount())); return true; }
        if (stack.is(Items.KELP)) { itemEntity.setItem(new ItemStack(Items.DRIED_KELP, stack.getCount())); return true; }
        if (stack.is(Items.SAND) || stack.is(Items.RED_SAND)) { itemEntity.setItem(new ItemStack(Items.GLASS, stack.getCount())); return true; }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.40 Unquenchable Wildfire Cremation Hex:
        // Continuous searing Pyre fire ticks even underwater, burns away 40% of target armor toughness,
        // blocks sprinting & natural health regeneration, and emits incendiary embers damaging nearby allies!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 45L);
        target.igniteForSeconds(3);
        target.setSprinting(false);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, true));
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);

        LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float pyreTickDmg = (1.65F + potencyTier * 0.50F) * mult;
        SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(level, target, caster, pyreTickDmg, false, true);

        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.0D),
                e -> e.isAlive() && e != target && e != caster)) {
            if (caster != null && SpellExecutionEngine.isFriendlyAllyForOasis(caster, adj)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, true)) continue;
            if ((target instanceof Monster && adj instanceof Monster) || adj.isAlliedTo(target)) {
                adj.igniteForSeconds(2);
                EntityHelper.getPersistentData(adj).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 45L);
                SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(level, adj, caster, 1.30F * mult, false, true);
            }
        }
        level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY(0.85D), target.getZ(), 7, 0.25D, 0.35D, 0.25D, 0.03D);
        level.sendParticles(ParticleTypes.ASH, target.getX(), target.getY(0.95D), target.getZ(), 4, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
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
