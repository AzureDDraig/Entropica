package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
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
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Eclipse Materia (Ethereal &amp; Paradox Fusion #25)</b>
 * (Spellcrafting System Guide Section 6.26 — Dual Radiant + Umbral [Leans: Umbral]):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Dual Radiant + Umbral paradox matrix [Leans: Umbral] — Strong against
 *     <b>RADIANT (+1)</b> and <b>NATURE (+1)</b> (Dominance {@code eclipse_radiant_dom}: Strong vs Undead) |
 *     Weak against <b>VOID (-1)</b>, computed dynamically at runtime by {@code VisInteractionManager}.</li>
 *     <li><b>Flow (Split)</b>: <i>Coronal Flare Bifurcation Split Flow</i> — Eclipse projectiles split into
 *     3 paradox shadow-bolts upon impact or apex range, curving around terrain obstacles to strike secondary
 *     enemies from behind.</li>
 *     <li><b>Projectile</b>: <i>Penumbra Crescent Scythe</i> — spinning crescent of fused shadow and light
 *     ({@code #FFEA78} / {@code #211A21}) curving around physical shields and cover to strike targets from
 *     behind and silencing mob special abilities for 2.0s.</li>
 *     <li><b>Beam</b>: <i>Umbral Inversion Jet</i> — stream of paradox shadow-light shrinking mob aggro range
 *     to 2 blocks and dissolving enemy armor by -1 point per second of continuous channeling (up to -4 armor).</li>
 *     <li><b>Radial Burst</b>: <i>Total Eclipse Nova</i> — 6m+ blast of pitch-black darkness encircled by
 *     blazing coronal gold knocking hostiles back 4 blocks, inflicting simultaneous Blindness &amp; Glowing for
 *     4.0s, snuffing out torches &amp; fires, and resetting mob aggro.</li>
 *     <li><b>Touch / Strike</b>: <i>Umbral Siphon Palm</i> — inverted light palm cutting enemy melee attack
 *     damage by 40% for 6.0s and siphoning active positive potion effects directly to the caster.</li>
 *     <li><b>Aura / Self</b>: <i>Solar Corona Bastion</i> — radiant golden flares ({@code #FFEA78} / {@code #211A21})
 *     granting +6 Armor, total immunity to Blindness &amp; Darkness, and setting melee attackers ablaze with celestial fire.</li>
 *     <li><b>Glyph / Trap</b>: <i>Total Eclipse Singularity Mine</i> — disarmable Single/Repeating rune detonating
 *     into a 4-block black hole encircled by a coronal ring, pulling hostiles to the center, rooting them for 3.0s,
 *     purging positive buffs, blinding them for 4.0s, and snuffing nearby torches.</li>
 *     <li><b>Cone / Spray</b>: <i>Coronal Flare Deluge</i> — 60° cone of paradox shadow-flame &amp; golden corona
 *     knocking enemies 6 blocks back, inflicting simultaneous Blindness &amp; Glowing for 4.0s, snuffing torches
 *     &amp; campfires, and dissolving 3 points of enemy armor for 6.0s.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Penumbral Shadow Monsoon</i> — tier-scaled cave-safe 10.0s downpour of dark
 *     solar radiation stripping all active potion buffs &amp; armor enchantments from hostiles while silencing abilities.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Coronal Eclipse Eidolon Suite</i> — {@code #FFEA78} / {@code #211A21}
 *     Eclipse-Knight Agent (shield-piercing strikes inflicting Blindness, Glowing &amp; armor shred), Black Sun
 *     Corona Spire Sentry (blinding twilight aura + buff-stripping shadow bolts), and Penumbra Silhouette Effigy
 *     Decoy (5m pulling, blinding nova upon defeat).</li>
 *     <li><b>Blink / Step</b>: <i>Coronal Flare Dash</i> — dashes 16 blocks forward in a streak of celestial fire,
 *     knocking aside mobs along the path.</li>
 *     <li><b>Transmute</b>: <i>Umbral Smelting Transmute</i> — smelts raw ores in complete darkness with +20% bonus yields.</li>
 *     <li><b>Affliction</b>: <i>Coronal Blind-Rot Hex</i> — milk-immune twilight paradox curse permanently inflicting
 *     simultaneous Blindness &amp; Glowing, dissolving 3 physical armor points, and preventing mob health regeneration
 *     until cleansed via Restoration magic or caster defeat.</li>
 * </ul>
 */
public final class EclipseSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int CORONAL_GOLD_HEX_COLOR = 0xFFEA78;
    public static final int ABYSSAL_ECLIPSE_HEX_COLOR = 0x211A21;
    public static final int CORONAL_ECLIPSE_HEX_COLOR = 0x211A21;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    @Override
    public EssenceType materiaType() {
        return EssenceType.ECLIPSE;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Coronal Flare Bifurcation Split Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Penumbra Crescent Scythe";
            case BEAM -> "Umbral Inversion Jet";
            case RADIAL_BURST -> "Total Eclipse Nova";
            case TOUCH_STRIKE -> "Umbral Siphon Palm";
            case AURA -> "Solar Corona Bastion";
            case GLYPH -> "Total Eclipse Singularity Mine";
            case CONE -> "Coronal Flare Deluge";
            case SKYFALL -> "Penumbral Shadow Monsoon";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Coronal Flare Dash";
            case TRANSMUTE, BREAK -> "Umbral Smelting Transmute";
            case AFFLICTION -> "Coronal Blind-Rot Hex";
            case RESTORATION -> "Coronal Twilight Restoration";
            default -> "Coronal Eclipse " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // FLOW MUTATION — CORONAL FLARE BIFURCATION SPLIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), ABYSSAL_ECLIPSE_HEX_COLOR, 16, 0.88F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
        );
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), CORONAL_GOLD_HEX_COLOR, 16, 0.90F, 0.04F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
            );
        }
        // Secondary split shadow-bolts curve around terrain obstacles to strike secondary enemies from behind!
        if (projectile.tickCount >= 2) {
            steerFlankingBackStrike(level, projectile, 0.24D);
        }
    }

    @Override
    public void onProjectileImpactOrExpireFromFlow(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Vec3 splitOrigin,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.26 Coronal Flare Bifurcation Split Flow:
        // Splits into 3 paradox shadow-bolts upon impact or apex range, curving around terrain obstacles
        // to strike secondary enemies from behind!
        float[] angles = new float[]{-28.0F, 0.0F, 28.0F};
        for (float yaw : angles) {
            SpellProjectileEntity child = projectile.spawnSplitChild(level, splitOrigin, yaw, true, 0.65F, 1.35F);
            child.setWallPhasing(true);
            child.setHomingGuidance(true);
            child.setNoGravity(true);
        }
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), CORONAL_GOLD_HEX_COLOR, 22, 1.00F, 0.05F),
                splitOrigin.x, splitOrigin.y + 0.3D, splitOrigin.z, 12, 0.25D, 0.25D, 0.25D, 0.05D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), ABYSSAL_ECLIPSE_HEX_COLOR, 22, 0.95F, 0.05F),
                splitOrigin.x, splitOrigin.y + 0.3D, splitOrigin.z, 10, 0.25D, 0.25D, 0.25D, 0.03D
        );
        level.playSound(null, BlockPos.containing(splitOrigin), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 0.72F);
    }

    // =========================================================================
    // FORM MUTATION 1 — PENUMBRA CRESCENT SCYTHE (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Zero-drop spinning crescent scythe curving around physical shields and cover!
        projectile.setNoGravity(true);
        projectile.setHomingGuidance(true);
        projectile.setWallPhasing(true);
        level.playSound(null, caster.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.85F, 0.78F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), ABYSSAL_ECLIPSE_HEX_COLOR, 16, 0.90F, 0.05F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.07D, 0.07D, 0.07D, 0.01D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), CORONAL_GOLD_HEX_COLOR, 16, 0.90F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
        );
        if (projectile.tickCount >= 2) {
            steerFlankingBackStrike(level, projectile, 0.28D);
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
        // Section 6.26 Penumbra Crescent Scythe:
        // Curves around physical shields and cover to strike targets from behind, silencing mob special abilities for 2.0s (40t)!
        if (!(hitEntity instanceof LivingEntity target) || !target.isAlive() || target == caster) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(target, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        int tier = spell.getEffectiveTier();
        boolean brokeShield = false;
        if (target.isBlocking()) {
            brokeShield = true;
            if (target instanceof Player blockingPlayer && !blockingPlayer.getUseItem().isEmpty()) {
                blockingPlayer.getCooldowns().addCooldown(blockingPlayer.getUseItem(), 60);
            }
            target.stopUsingItem();
            level.playSound(null, target.blockPosition(), SoundEvents.SHIELD_BREAK.value(), SoundSource.PLAYERS, 0.95F, 0.95F);
        }

        if (!slot.hasHarmVerb() || brokeShield) {
            float scytheDmg = (2.85F + tier * 0.85F) * spell.flawProfile().getPotencyMultiplier();
            SpellCombatReactionEngine.hurtWithEclipseMateriaAdvantage(level, target, caster, scytheDmg, false, true);
        }

        // Strike lands from behind the victim's back, shoving them forward & silencing special abilities for 2.0s (40t)!
        if (!SpellCombatReactionEngine.isBossEntity(level, target)) {
            Vec3 look = target.getLookAngle();
            Vec3 flatLook = new Vec3(look.x, 0.0D, look.z);
            Vec3 fwdFromBehind = flatLook.lengthSqr() > 1.0E-4D ? flatLook.normalize().scale(0.46D) : new Vec3(0.0D, 0.0D, 0.46D);
            Vec3 cur = target.getDeltaMovement();
            SpellExecutionEngine.applySyncedMotion(target, new Vec3(cur.x * 0.4D + fwdFromBehind.x, 0.18D, cur.z * 0.4D + fwdFromBehind.z));
        }

        SpellLingeringZoneManager.applyArcaneSilence(target, 40);
        Vec3 backPos = target.position().subtract(target.getLookAngle().scale(0.65D));
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), CORONAL_GOLD_HEX_COLOR, 18, 0.95F, 0.05F),
                backPos.x, target.getY(0.85D), backPos.z, 10, 0.22D, 0.25D, 0.22D, 0.04D
        );
    }

    // =========================================================================
    // FORM MUTATION 2 — UMBRAL INVERSION JET (BEAM)
    // =========================================================================

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
        // Section 6.26 Umbral Inversion Jet:
        // Stream of paradox shadow-light; shrinks mob aggro range to 2 blocks and dissolves enemy armor
        // by -1 point per second of continuous channeling (up to -4 armor)!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        long myopiaUntil = now + 80L;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (hit == caster || !hit.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float beamDmg = (1.40F + tier * 0.45F) * spell.flawProfile().getPotencyMultiplier() * apparatusPotency;
                SpellCombatReactionEngine.hurtWithEclipseMateriaAdvantage(level, hit, caster, beamDmg, false, true);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            // Enforce genuine 2-block aggro range ceiling!
            data.putLong(SpellLingeringZoneManager.TAG_ECLIPSE_MYOPIA_UNTIL, myopiaUntil);
            if (hit instanceof Mob mob && mob.getTarget() != null && mob.distanceToSqr(mob.getTarget()) > 4.0D) {
                clearMobAggro(mob);
            }

            // Track continuous beam contact ticks (4 ticks per pulse -> 20 ticks = 1.0s per -1 armor point, up to -4 armor)!
            long last = data.getLong("SpellEclipseBeamLastTick").orElse(-100L);
            int contactTicks = (now - last <= 16L) ? data.getInt("SpellEclipseBeamTicks").orElse(0) + 4 : 4;
            data.putLong("SpellEclipseBeamLastTick", now);
            data.putInt("SpellEclipseBeamTicks", contactTicks);

            int dissolvedArmor = Math.min(4, Math.max(1, contactTicks / 20));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, dissolvedArmor, 0.0D, 100L);
            hit.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 45, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0));
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — TOTAL ECLIPSE NOVA (RADIAL BURST)
    // =========================================================================

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
        // Section 6.26 Total Eclipse Nova:
        // Explosive 360° blast of pitch-black darkness encircled by blazing coronal gold (6-block+ radius);
        // knocks hostiles back 4 blocks, inflicts simultaneous Blindness and Glowing for 4.0s (80t),
        // snuffs out torches and fires, and resets mob aggro!
        double r = Math.max(6.0D, radius);
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.ECLIPSE, null, tier);

        AABB box = new AABB(
                center.x - r, center.y - 2.0D, center.z - r,
                center.x + r, center.y + 2.8D, center.z + r
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float novaDmg = (3.35F + tier * 0.92F) * spell.flawProfile().getPotencyMultiplier() * apparatusPotency;
                SpellCombatReactionEngine.hurtWithEclipseMateriaAdvantage(level, foe, caster, novaDmg, false, true);
            }

            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 diff = foe.position().subtract(center);
                Vec3 flat = new Vec3(diff.x, 0.0D, diff.z);
                Vec3 out = flat.lengthSqr() > 1.0E-4D ? flat.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(out.x * 1.12D, 0.32D, out.z * 1.12D)); // 4-block knockback!
            }
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            if (foe instanceof Mob mob) {
                clearMobAggro(mob);
            }
        }

        snuffTorchesAndFires(level, BlockPos.containing(center), (int) Math.ceil(r));
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), ABYSSAL_ECLIPSE_HEX_COLOR, 26, 1.15F, 0.06F),
                center.x, center.y + 0.8D, center.z, 32, 2.2D, 0.5D, 2.2D, 0.05D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), CORONAL_GOLD_HEX_COLOR, 26, 1.10F, 0.05F),
                center.x, center.y + 0.9D, center.z, 28, 2.4D, 0.4D, 2.4D, 0.05D
        );
        level.playSound(null, BlockPos.containing(center), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.0F, 0.80F);
    }

    // =========================================================================
    // FORM MUTATION 4 — UMBRAL SIPHON PALM (TOUCH / STRIKE)
    // =========================================================================

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
        // Section 6.26 Umbral Siphon Palm:
        // Strikes with an inverted light palm; cuts enemy melee attack damage by 40% for 6.0s (120t)
        // and siphons their positive potion effects directly to the caster!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.22D, 0.0D), direction, 3.3D, EssenceType.ECLIPSE, null, tier
        );

        for (LivingEntity hit : hitEntities) {
            if (hit == caster || !hit.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            SpellCombatReactionEngine.applyTimedAttackDebuff(hit, level, 0.40D, 0.0D, 120L);
            hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));

            List<MobEffectInstance> stolen = new ArrayList<>();
            for (MobEffectInstance inst : hit.getActiveEffects()) {
                if (inst.getEffect().value().isBeneficial()) {
                    stolen.add(new MobEffectInstance(inst));
                }
            }
            for (MobEffectInstance inst : stolen) {
                hit.removeEffect(inst.getEffect());
                caster.addEffect(new MobEffectInstance(inst.getEffect(), Math.min(240, inst.getDuration()), inst.getAmplifier()));
            }

            if (!slot.hasHarmVerb()) {
                float palmDmg = (3.45F + tier * 0.95F) * spell.flawProfile().getPotencyMultiplier() * apparatusPotency;
                SpellCombatReactionEngine.hurtWithEclipseMateriaAdvantage(level, hit, caster, palmDmg, true, true);
            }

            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), CORONAL_GOLD_HEX_COLOR, 20, 0.95F, 0.04F),
                    hit.getX(), hit.getY(0.8D), hit.getZ(), 10, 0.30D, 0.30D, 0.30D, 0.04D
            );
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.85F, 1.35F);
    }

    // =========================================================================
    // FORM MUTATION 5 — SOLAR CORONA BASTION (AURA / SELF)
    // =========================================================================

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
        // Section 6.26 Solar Corona Bastion:
        // Encases the caster in radiant golden flares (#FFEA78 / #211A21); grants +6 Armor, total immunity
        // to Blindness and Darkness effects, and sets melee attackers ablaze with celestial fire!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ECLIPSE_BASTION_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 0.0D, 0.0D, 0.0D, dur);
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.48F, null);

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), CORONAL_GOLD_HEX_COLOR, 24, 1.00F, 0.05F),
                caster.getX(), caster.getY(0.9D), caster.getZ(), 18, 0.45D, 0.50D, 0.45D, 0.04D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), ABYSSAL_ECLIPSE_HEX_COLOR, 24, 0.95F, 0.05F),
                caster.getX(), caster.getY(0.9D), caster.getZ(), 14, 0.40D, 0.50D, 0.40D, 0.02D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.85F, 1.25F);
    }

    // =========================================================================
    // FORM MUTATION 6 — TOTAL ECLIPSE SINGULARITY MINE (GLYPH / TRAP)
    // =========================================================================

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
        // Section 6.26 Total Eclipse Singularity Mine:
        // Disarmable Single/Repeating rune (1.0s arming delay) detonating into a 4-block black hole
        // encircled by a coronal ring; pulls hostiles to the center, roots them for 3.0s (60t),
        // purges positive potion buffs, and blinds them for 4.0s (80t) while snuffing nearby torches!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double mineRad = Math.max(4.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), mineRad, EssenceType.ECLIPSE, null, Math.min(240, maxDuration), tier
        );
        level.playSound(null, surface.above(), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 0.85F, 1.25F);

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, mineRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.ECLIPSE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.ECLIPSE, null, rem, tier
                        );
                    }
                    if (elapsed < 20) return true;
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                            zone.center.x + 3.0D, zone.center.y + 2.2D, zone.center.z + 3.0D
                    );
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive() && e != owner && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)));
                    if (triggers.isEmpty()) return true;

                    // Detonate 4-block black hole encircled by coronal ring!
                    SpellWorldVfxManager.broadcastVacuumSphere(
                            srv, zone.center.add(0.0D, 0.45D, 0.0D), zone.radius, true, EssenceType.ECLIPSE, null, 36, tier
                    );
                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, zone.radius, EssenceType.ECLIPSE, null, tier);
                    snuffTorchesAndFires(srv, BlockPos.containing(zone.center), (int) Math.ceil(zone.radius));

                    Set<UUID> pulledHostiles = new HashSet<>();
                    AABB blastBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 3.0D, zone.center.z + zone.radius
                    );
                    for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
                        if (victim == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, victim)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, wardSafe)) continue;

                        purgeBeneficialEffects(victim);
                        victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                        victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));

                        if (SpellCombatReactionEngine.isBossEntity(srv, victim)) {
                            applyEclipseSingularityRoot(srv, victim, 60);
                        } else {
                            Vec3 toCenter = zone.center.subtract(victim.position());
                            Vec3 pull = toCenter.lengthSqr() > 1.0E-4D ? toCenter.normalize().scale(0.95D) : Vec3.ZERO;
                            SpellExecutionEngine.applySyncedMotion(victim, new Vec3(pull.x, 0.16D, pull.z));
                            pulledHostiles.add(victim.getUUID());
                        }

                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, victim, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                            );
                        }
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            float mineDmg = (3.40F + tier * 0.90F) * Math.max(0.5F, zone.apparatusPotency);
                            SpellCombatReactionEngine.hurtWithEclipseMateriaAdvantage(srv, victim, owner, mineDmg, false, true);
                        }
                    }

                    // Anchor pulled victims at the singularity center for 3.0s (60t) right after the inward pull!
                    if (!pulledHostiles.isEmpty()) {
                        Vec3 holeCenter = zone.center;
                        double rLock = zone.radius + 1.5D;
                        SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                                owner, holeCenter, zone.radius, 10, srv.getGameTime(), zone.spell, zone.slot, EssenceType.ECLIPSE, 1.0F,
                                (sLvl, subZone, subEl) -> {
                                    if (subEl == 6) {
                                        AABB lockBox = new AABB(
                                                holeCenter.x - rLock, holeCenter.y - 2.0D, holeCenter.z - rLock,
                                                holeCenter.x + rLock, holeCenter.y + 3.0D, holeCenter.z + rLock
                                        );
                                        for (LivingEntity foe : sLvl.getEntitiesOfClass(LivingEntity.class, lockBox, LivingEntity::isAlive)) {
                                            if (pulledHostiles.remove(foe.getUUID())) {
                                                applyEclipseSingularityRoot(sLvl, foe, 60);
                                            }
                                        }
                                        return false;
                                    }
                                    return subEl < 10;
                                }
                        ));
                    }

                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.0F, 0.85F);
                    triggersLeft[0]--;
                    rearmCooldown[0] = 35;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — CORONAL FLARE DELUGE (CONE / SPRAY)
    // =========================================================================

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
        // Section 6.26 Coronal Flare Deluge:
        // Wide 60° cone of paradox shadow-flame and golden corona particles; knocks enemies 6 blocks back,
        // inflicts simultaneous Blindness and Glowing for 4.0s (80t), snuffs out torches and campfires,
        // and dissolves 3 points of enemy armor for 6.0s (120t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.20D, 0.0D), direction, coneRange, EssenceType.ECLIPSE, null, tier
        );

        Vec3 flatDir = new Vec3(direction.x, 0.0D, direction.z);
        Vec3 pushDir = flatDir.lengthSqr() > 1.0E-4D ? flatDir.normalize() : new Vec3(0.0D, 0.0D, 1.0D);

        for (LivingEntity hit : hitTargets) {
            if (hit == caster || !hit.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float delugeDmg = (2.95F + tier * 0.84F) * spell.flawProfile().getPotencyMultiplier() * apparatusPotency;
                SpellCombatReactionEngine.hurtWithEclipseMateriaAdvantage(level, hit, caster, delugeDmg, false, true);
            }

            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(pushDir.x * 1.48D, 0.36D, pushDir.z * 1.48D)); // 6-block knockback!
            }
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 120L);
        }

        int steps = Math.min(9, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            int span = Math.max(2, (int) Math.ceil(s * 0.55D));
            snuffTorchesAndFires(level, BlockPos.containing(caster.position().add(direction.scale(s))), span);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.90F, 0.72F);
    }

    // =========================================================================
    // FORM MUTATION 8 — PENUMBRAL SHADOW MONSOON (SKYFALL / RAIN)
    // =========================================================================

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
        // Section 6.26 Penumbral Shadow Monsoon:
        // Summons a cave-safe, tier-scaled downpour of dark solar radiation for 10.0s (200t);
        // strips all active potion buffs and armor enchantments from hostiles while silencing their abilities!
        int tier = spell.getEffectiveTier();
        double r = Math.max(8.0D, radius);
        BlockPos surf = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        Vec3 groundCenter = new Vec3(impactCenter.x, surf.above().getY() + 0.03D, impactCenter.z);
        double eclipseH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, surf.above(), tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, groundCenter, r, EssenceType.ECLIPSE, null, 200, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, groundCenter, r, EssenceType.ECLIPSE, null, 200, tier);
        level.playSound(null, BlockPos.containing(groundCenter), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.1F, 0.68F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, groundCenter, r, 200, level.getGameTime(), spell, slot, EssenceType.ECLIPSE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), ABYSSAL_ECLIPSE_HEX_COLOR, 20, 1.05F, 0.05F),
                                zone.center.x, zone.center.y + Math.min(5.0D, eclipseH - 0.5D), zone.center.z,
                                16, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.03D
                        );
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), CORONAL_GOLD_HEX_COLOR, 20, 1.00F, 0.05F),
                                zone.center.x, zone.center.y + Math.min(4.8D, eclipseH - 0.5D), zone.center.z,
                                10, zone.radius * 0.50D, 1.0D, zone.radius * 0.50D, 0.04D
                        );
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float potency = (zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F) * zone.apparatusPotency;

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + eclipseH + 0.5D, zone.center.z + zone.radius
                        );
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            purgeBeneficialEffects(v);
                            SpellLingeringZoneManager.applyArcaneSilence(v, 45);
                            SpellCombatReactionEngine.applyPercentageArmorShred(v, srv, 0.40D, 0.40D, 45L);

                            if (v instanceof Monster) {
                                for (EquipmentSlot armorSlot : ARMOR_SLOTS) {
                                    ItemStack worn = v.getItemBySlot(armorSlot);
                                    if (!worn.isEmpty() && worn.isEnchanted()) {
                                        EnchantmentHelper.updateEnchantments(worn, mutable -> mutable.removeIf(h -> !h.is(EnchantmentTags.CURSE)));
                                    }
                                }
                            }

                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F
                                );
                            }
                            if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                float radDmg = (1.95F + tier * 0.56F) * potency;
                                SpellCombatReactionEngine.hurtWithEclipseMateriaAdvantage(srv, v, owner, radDmg, false, true);
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — CORONAL ECLIPSE EIDOLON SUITE (AGENT / SENTRY / DECOY)
    // =========================================================================

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
        int tier = spell.getEffectiveTier();
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.ECLIPSE.name());
        tag.putInt("SpellEidolonTintRGB", CORONAL_ECLIPSE_HEX_COLOR);

        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.65D, radius * 0.35D), EssenceType.ECLIPSE, null, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), CORONAL_GOLD_HEX_COLOR, 28, 1.05F, 0.05F),
                spawnPos.x, spawnPos.y + 0.95D, spawnPos.z, 18, 0.45D, 0.55D, 0.45D, 0.04D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), ABYSSAL_ECLIPSE_HEX_COLOR, 28, 1.00F, 0.05F),
                spawnPos.x, spawnPos.y + 0.85D, spawnPos.z, 16, 0.45D, 0.55D, 0.45D, 0.03D
        );
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.90F, 0.85F);
    }

    // =========================================================================
    // CORE EFFECT SYNERGIES — BLINK, TRANSMUTE, AFFLICTION & RESTORATION
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        // Section 6.26 Coronal Flare Dash:
        // Dashes 16 blocks forward in a streak of celestial fire, knocking aside mobs along the path!
        Vec3 delta = arrivalPos.subtract(departurePos);
        double dist = delta.length();
        Vec3 dashDir = dist > 1.0E-4D ? delta.normalize() : caster.getLookAngle();
        Vec3 right = new Vec3(-dashDir.z, 0.0D, dashDir.x);

        SpellWorldVfxManager.broadcastChanneledBeamSlice(
                level, departurePos.add(0.0D, 0.90D, 0.0D), arrivalPos.add(0.0D, 0.90D, 0.0D),
                EssenceType.ECLIPSE, null, potencyTier, 18
        );

        int steps = Math.max(2, (int) Math.ceil(dist));
        Vec3 stepVec = delta.scale(1.0D / steps);
        long burnUntil = level.getGameTime() + 80L;
        Set<UUID> shoved = new HashSet<>();

        for (int i = 0; i <= steps; i++) {
            Vec3 p = departurePos.add(stepVec.scale(i));
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), CORONAL_GOLD_HEX_COLOR, 18, 0.95F, 0.04F),
                    p.x, p.y + 0.8D, p.z, 3, 0.22D, 0.30D, 0.22D, 0.02D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), ABYSSAL_ECLIPSE_HEX_COLOR, 18, 0.90F, 0.04F),
                    p.x, p.y + 0.8D, p.z, 2, 0.22D, 0.30D, 0.22D, 0.01D
            );

            AABB laneBox = new AABB(p.x - 1.65D, p.y - 0.6D, p.z - 1.65D, p.x + 1.65D, p.y + 2.2D, p.z + 1.65D);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, laneBox, LivingEntity::isAlive)) {
                if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                if (!shoved.add(foe.getUUID())) continue;

                if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                    Vec3 rel = foe.position().subtract(p);
                    double sideSign = rel.dot(right) >= 0.0D ? 1.0D : -1.0D;
                    Vec3 lateral = right.scale(sideSign * 0.92D);
                    SpellExecutionEngine.applySyncedMotion(foe, new Vec3(lateral.x, 0.34D, lateral.z));
                }
                foe.igniteForSeconds(4);
                EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_ECLIPSE_CORONA_BURN_UNTIL, burnUntil);
                SpellCombatReactionEngine.hurtWithEclipseMateriaAdvantage(level, foe, caster, 2.0F + potencyTier * 0.55F, false, true);
            }
        }
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.90F, 1.15F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.26 Umbral Smelting Transmute:
        // Smelts raw ores in complete darkness with +20% bonus yields!
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F || hardness > 2.0F + potencyTier * 5.0F) {
            return false;
        }

        List<ItemStack> drops = Block.getDrops(state, level, pos, null, caster, ItemStack.EMPTY);
        boolean smeltedAny = false;
        boolean inCompleteDarkness = level.getMaxLocalRawBrightness(pos) <= 4;

        List<ItemStack> finalOutputs = new ArrayList<>();
        for (ItemStack d : drops) {
            ItemStack smelted = NetherSpellMutationHandler.trySmeltStack(level, d);
            if (smelted != d) {
                smeltedAny = true;
                ItemStack outCopy = smelted.copy();
                if (inCompleteDarkness) {
                    int bonus = (outCopy.getCount() / 5) + (level.random.nextFloat() < (outCopy.getCount() % 5) * 0.20F ? 1 : 0);
                    if (bonus > 0) {
                        outCopy.setCount(Math.min(outCopy.getMaxStackSize(), outCopy.getCount() + bonus));
                    }
                }
                finalOutputs.add(outCopy);
            } else {
                finalOutputs.add(d);
            }
        }

        if (smeltedAny) {
            level.removeBlock(pos, false);
            for (ItemStack out : finalOutputs) {
                Block.popResource(level, pos, out);
            }
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), ABYSSAL_ECLIPSE_HEX_COLOR, 18, 0.90F, 0.04F),
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8, 0.22D, 0.22D, 0.22D, 0.02D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), CORONAL_GOLD_HEX_COLOR, 18, 0.92F, 0.04F),
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 6, 0.22D, 0.22D, 0.22D, 0.03D
            );
            maybeGrantEclipseBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        ItemStack in = itemEntity.getItem();
        ItemStack smelted = NetherSpellMutationHandler.trySmeltStack(level, in);
        if (smelted != in) {
            ItemStack outCopy = smelted.copy();
            if (level.getMaxLocalRawBrightness(itemEntity.blockPosition()) <= 4) {
                int bonus = (outCopy.getCount() / 5) + (level.random.nextFloat() < (outCopy.getCount() % 5) * 0.20F ? 1 : 0);
                if (bonus > 0) {
                    outCopy.setCount(Math.min(outCopy.getMaxStackSize(), outCopy.getCount() + bonus));
                }
            }
            itemEntity.setItem(outCopy);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), CORONAL_GOLD_HEX_COLOR, 18, 0.92F, 0.04F),
                    itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 6, 0.18D, 0.18D, 0.18D, 0.03D
            );
            maybeGrantEclipseBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.26 Coronal Blind-Rot Hex:
        // Persistent milk-immune twilight paradox curse (cleansed strictly via Restoration magic or caster defeat):
        // permanently inflicts simultaneous Blindness and Glowing, dissolves 3 physical armor points,
        // and prevents mob health regeneration!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, true, true));
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D * mult, 0.0D, 45L);

        float hexDmg = (1.45F + potencyTier * 0.50F) * mult;
        SpellCombatReactionEngine.hurtWithEclipseMateriaAdvantage(level, target, null, hexDmg, false, true);

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), ABYSSAL_ECLIPSE_HEX_COLOR, 18, 0.90F, 0.04F),
                target.getX(), target.getY(0.8D), target.getZ(), 4, 0.25D, 0.30D, 0.25D, 0.01D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), CORONAL_GOLD_HEX_COLOR, 18, 0.90F, 0.04F),
                target.getX(), target.getY(0.9D), target.getZ(), 3, 0.22D, 0.28D, 0.22D, 0.02D
        );
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
        if (effect == SpellCoreEffect.RESTORATION) {
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(target);
            target.removeEffect(MobEffects.BLINDNESS);
            target.removeEffect(MobEffects.DARKNESS);
            float heal = 3.2F + potencyTier * 0.95F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, heal);
            target.heal(heal);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    /**
     * Curves spinning Eclipse scythes &amp; split shadow-bolts laterally around shields and cover
     * toward the target's unprotected back!
     */
    private static void steerFlankingBackStrike(ServerLevel level, SpellProjectileEntity projectile, double turnRate) {
        Vec3 vel = projectile.getDeltaMovement();
        double speed = vel.length();
        if (speed < 0.08D) return;
        Entity owner = projectile.getOwner();
        LivingEntity livOwner = owner instanceof LivingEntity l ? l : null;

        LivingEntity bestTarget = null;
        double bestDistSq = Double.MAX_VALUE;
        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(9.5D), LivingEntity::isAlive)) {
            if (cand == owner || SpellExecutionEngine.isFriendlyAllyForOasis(livOwner, cand)) continue;
            if (!(cand instanceof Monster) && (livOwner == null || cand != livOwner.getLastHurtMob())) continue;
            double dSq = cand.distanceToSqr(projectile);
            if (dSq < bestDistSq) {
                bestDistSq = dSq;
                bestTarget = cand;
            }
        }
        if (bestTarget == null) return;

        Vec3 targetCenter = bestTarget.position().add(0.0D, bestTarget.getBbHeight() * 0.58D, 0.0D);
        Vec3 targetLook = bestTarget.getLookAngle();
        Vec3 flatLook = new Vec3(targetLook.x, 0.0D, targetLook.z);
        Vec3 normLook = flatLook.lengthSqr() > 1.0E-4D ? flatLook.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 toScythe = projectile.position().subtract(bestTarget.position());

        Vec3 aimPoint;
        if (toScythe.dot(normLook) > 0.0D && bestDistSq > 2.25D) {
            // Projectile is still in front of target/shield -> curve wide around their flank toward their back!
            Vec3 side = new Vec3(-normLook.z, 0.0D, normLook.x).scale((projectile.getId() & 1) == 0 ? 1.35D : -1.35D);
            aimPoint = targetCenter.subtract(normLook.scale(1.05D)).add(side);
        } else {
            aimPoint = targetCenter.subtract(normLook.scale(0.35D));
        }

        Vec3 desiredDir = aimPoint.subtract(projectile.position());
        if (desiredDir.lengthSqr() < 1.0E-4D) return;
        Vec3 steered = vel.normalize().scale(1.0D - turnRate).add(desiredDir.normalize().scale(turnRate));
        if (steered.lengthSqr() > 1.0E-4D) {
            projectile.setDeltaMovement(steered.normalize().scale(speed));
            projectile.hasImpulse = true;
        }
    }

    private static void clearMobAggro(Mob mob) {
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        mob.setAggressive(false);
        mob.getNavigation().stop();
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
    }

    private static void applyEclipseSingularityRoot(ServerLevel level, LivingEntity target, int ticks) {
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.95D, ticks);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6));
        target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), ticks, 0));
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + ticks);
        SpellExecutionEngine.applySyncedMotion(target, new Vec3(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D));
    }

    public static void purgeBeneficialEffects(LivingEntity target) {
        List<Holder<MobEffect>> toRemove = new ArrayList<>();
        for (MobEffectInstance eff : target.getActiveEffects()) {
            if (eff.getEffect().value().isBeneficial()) {
                toRemove.add(eff.getEffect());
            }
        }
        for (Holder<MobEffect> h : toRemove) {
            target.removeEffect(h);
        }
    }

    public static void snuffTorchesAndFires(ServerLevel level, BlockPos center, int rad) {
        double rSq = (rad + 0.5D) * (rad + 0.5D);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-rad, -1, -rad), center.offset(rad, 2, rad))) {
            if (p.distSqr(center) > rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (bs.getBlock() instanceof TorchBlock || bs.getBlock() instanceof WallTorchBlock) {
                level.destroyBlock(p, true);
            } else if (bs.getBlock() instanceof CampfireBlock && bs.hasProperty(CampfireBlock.LIT) && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            } else if (bs.getBlock() instanceof CandleBlock && bs.hasProperty(CandleBlock.LIT) && bs.getValue(CandleBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CandleBlock.LIT, false));
            }
        }
    }

    private static void maybeGrantEclipseBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellEclipseBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellEclipseBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellEclipseBatchTick", now);
        pData.putInt("SpellEclipseBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.ECLIPSE, refund);
        }
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        for (int dy = 2; dy >= -5; dy--) {
            BlockPos check = start.offset(0, dy, 0);
            BlockState bs = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());
            if ((!bs.isAir() && !bs.getCollisionShape(level, check).isEmpty())
                    && (above.isAir() || above.getCollisionShape(level, check.above()).isEmpty())) {
                return check;
            }
        }
        return start.below();
    }
}
