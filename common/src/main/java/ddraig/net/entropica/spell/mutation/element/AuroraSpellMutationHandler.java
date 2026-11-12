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
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.item.ItemEntity;
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
 * Complete per-part Signature Mutation implementation for <b>Aurora Materia (Ethereal &amp; Paradox Fusion #35)</b>
 * (Spellcrafting System Guide Section 6.36 — Radiant + Frozen [Leans: Radiant]).
 */
public final class AuroraSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int BOREALIS_ARCHON_HEX_COLOR = 0xBCE3B5;

    @Override
    public EssenceType materiaType() {
        return EssenceType.AURORA;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Glacial Aurora Spiral Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Prismatic Aurora Ribbon-Lance";
            case BEAM -> "Borealis Prismatic Spectrum Ray";
            case RADIAL_BURST -> "Borealis Prismatic Curtain Nova";
            case TOUCH_STRIKE -> "Prismatic Aurora Polar Cleave";
            case AURA -> "Borealis Prismatic Veil Mantle";
            case GLYPH -> "Cryo-Refractive Mirror Trap";
            case CONE -> "Borealis Prismatic Fan Deluge";
            case SKYFALL -> "Cryo-Solar Comet Barrage";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Borealis Prism-Flash Step";
            case TRANSMUTE, BREAK -> "Polar Cryo-Refraction Transmute";
            case AFFLICTION -> "Sub-Zero Polar Stasis Hex";
            default -> "Prismatic Borealis " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Glacial Aurora Spiral Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Prismatic Aurora Ribbon-Lance: pierces up to 3 targets, freezes in place 2.0s (40t), & applies Glowing 5.0s (100t)!
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) return;

        living.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
        living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 100, living.getTicksFrozen() + 90));
        living.removeEffect(MobEffects.INVISIBILITY);
        living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
        if (living.getType().is(EntityTypeTags.UNDEAD)) {
            living.hurtServer(level, level.damageSources().magic(), 3.5F + spell.getEffectiveTier() * 1.15F);
        }
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
        // Borealis Prismatic Spectrum Ray: stacks Slowness up to Slowness IV + Glowing;
        // 3.0s continuous channeling triggers Prismatic Flashbang Shatter stunning 1.5s (30t) + bonus frost-shatter damage!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("AuroraBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("AuroraBeamStacks").orElse(0) + 1 : 1;
            data.putLong("AuroraBeamLastTick", now);
            data.putInt("AuroraBeamStacks", stacks);

            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, Math.min(3, stacks)));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            hit.setTicksFrozen(Math.min(hit.getTicksRequiredToFreeze() + 100, hit.getTicksFrozen() + 50));

            if (stacks >= 3) {
                data.putInt("AuroraBeamStacks", 0);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                hit.hurtServer(level, level.damageSources().freeze(), 5.0F + spell.getEffectiveTier() * 1.4F);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.55F, 0.96F, 0.85F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.SNOWFLAKE, hit.getX(), hit.getY(0.9D), hit.getZ(), 22, 0.45D, 0.5D, 0.45D, 0.08D);
                level.playSound(null, hit.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.45F);
            }
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
        // Borealis Prismatic Curtain Nova: 6m curtain — 6m knockback, freezes in stasis 2.0s (40t),
        // 4.0s (80t) Blindness + Glowing, and smites undead (+50%)!
        double r = Math.max(6.0D, radius);
        float smiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(spell.getEffectiveTier()) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.45D);
            foe.setDeltaMovement(away.x, 0.36D, away.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
            foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 120, foe.getTicksFrozen() + 100));
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            if (foe.getType().is(EntityTypeTags.UNDEAD)) {
                foe.invulnerableTime = 0;
                foe.hurtServer(level, level.damageSources().indirectMagic(caster, caster), Math.max(3.5F, smiteBonus));
            }
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.9D, center.z, 36, 2.2D, 0.6D, 2.2D, 0.07D);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.9D, center.z, 30, 2.2D, 0.6D, 2.2D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.15F, 1.55F);
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
        // Prismatic Aurora Polar Cleave: freezes struck target solid 1.5s (30t), smites undead (+50%),
        // Glowing 6.0s (120t), strips 2 physical armor points, & marks brittle (+40% shatter bonus on next hit)!
        float smiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(spell.getEffectiveTier()) * Math.max(0.5F, apparatusPotency);
        long brittleUntil = level.getGameTime() + 120L;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
            hit.setTicksFrozen(Math.min(hit.getTicksRequiredToFreeze() + 100, hit.getTicksFrozen() + 90));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 120L);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_AURORA_BRITTLE_UNTIL, brittleUntil);
            if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                hit.invulnerableTime = 0;
                hit.hurtServer(level, level.damageSources().indirectMagic(caster, caster), Math.max(3.5F, smiteBonus));
            }
            level.sendParticles(ParticleTypes.SNOWFLAKE, hit.getX(), hit.getY(0.85D), hit.getZ(), 16, 0.3D, 0.4D, 0.3D, 0.05D);
        }
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
        // Borealis Prismatic Veil Mantle: +4 Armor, Frost Walker water-freezing, complete freeze/Blindness/Darkness immunity,
        // and melee attacker Blindness + 2s Frostbite!
        int dur = 200 + spell.getEffectiveTier() * 40;
        long until = level.getGameTime() + dur;
        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_AURORA_MANTLE_UNTIL, until);
        data.putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, until);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 1.0D, 0.0D, 0.0D, dur);
        caster.setTicksFrozen(0);
        caster.clearFire();
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        level.sendParticles(ParticleTypes.SNOWFLAKE, caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.45D, 0.55D, 0.45D, 0.04D);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 16, 0.45D, 0.55D, 0.45D, 0.04D);
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
        // Cryo-Refractive Mirror Trap: reflects enemy projectiles back at shooter while rooting entering mobs
        // in sub-zero ice crystals for 3.0s (60t), continuous Aurora damage, & Glowing!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.2D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);

                    AABB projBox = new AABB(zone.center.x - 2.8D, zone.center.y - 0.5D, zone.center.z - 2.8D,
                            zone.center.x + 2.8D, zone.center.y + 2.8D, zone.center.z + 2.8D);
                    for (Projectile proj : srv.getEntitiesOfClass(Projectile.class, projBox)) {
                        if (owner != null && proj.getOwner() == owner) continue;
                        Vec3 rev = proj.getOwner() != null
                                ? proj.getOwner().getEyePosition().subtract(proj.position()).normalize().scale(Math.max(1.2D, proj.getDeltaMovement().length()))
                                : proj.getDeltaMovement().scale(-1.05D);
                        proj.setDeltaMovement(rev);
                        if (owner != null) proj.setOwner(owner);
                        proj.hasImpulse = true;
                        srv.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 6, 0.15D, 0.15D, 0.15D, 0.04D);
                    }

                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + 0.15D, zone.center.z, 5, 0.9D, 0.1D, 0.9D, 0.02D);
                    }
                    if (elapsed < 10) return true;

                    AABB triggerBox = new AABB(zone.center.x - 2.4D, zone.center.y - 1.0D, zone.center.z - 2.4D,
                            zone.center.x + 2.4D, zone.center.y + 2.0D, zone.center.z + 2.4D);
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (triggers.isEmpty()) return true;

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 3.2D, 60, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB box = new AABB(sZone.center.x - 3.2D, sZone.center.y - 1.0D, sZone.center.z - 3.2D,
                                        sZone.center.x + 3.2D, sZone.center.y + 2.5D, sZone.center.z + 3.2D);
                                long now = sLvl.getGameTime();
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sZone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 5));
                                    v.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                                    v.setTicksFrozen(Math.min(v.getTicksRequiredToFreeze() + 100, v.getTicksFrozen() + 35));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 25L);
                                    if (sEl % 20 == 0) {
                                        SpellExecutionEngine.deliverSubSpellOnEntity(sLvl, sOwner, v, Vec3.ZERO, sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.45F);
                                    }
                                }
                                return true;
                            }
                    ));
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.9F, 1.4F);
                    return false;
                }
        ));
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
        // Borealis Prismatic Fan Deluge: 5m knockback, freezes 2.0s (40t), Glowing + Blindness 3.0s (60t),
        // smites undead (+50%), and covers water/ground in frosted ice & snow!
        float smiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(spell.getEffectiveTier()) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.25D, 0.32D, direction.z * 1.25D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
            hit.setTicksFrozen(Math.min(hit.getTicksRequiredToFreeze() + 100, hit.getTicksFrozen() + 90));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                hit.invulnerableTime = 0;
                hit.hurtServer(level, level.damageSources().indirectMagic(caster, caster), Math.max(3.5F, smiteBonus));
            }
        }

        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        BlockPos cPos = BlockPos.containing(mid);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-3, -2, -3), cPos.offset(3, 1, 3))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.WATER) && level.getBlockState(p.above()).isAir()) {
                level.setBlockAndUpdate(p, Blocks.FROSTED_ICE.defaultBlockState());
            } else if (bs.isAir() && level.getBlockState(p.below()).isSolidRender()
                    && Blocks.SNOW.defaultBlockState().canSurvive(level, p) && level.random.nextFloat() < 0.45F) {
                level.setBlockAndUpdate(p, Blocks.SNOW.defaultBlockState());
            }
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, mid.x, mid.y, mid.z, 26, 1.5D, 0.5D, 1.5D, 0.05D);
        level.sendParticles(ParticleTypes.END_ROD, mid.x, mid.y, mid.z, 20, 1.5D, 0.5D, 1.5D, 0.05D);
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
        // Cryo-Solar Comet Barrage: 6.0s (120t) barrage of 5 glowing ice meteors — each impact flashes light & frost,
        // strips 2 armor points, deals heavy Aurora damage, and coats water/ground with temporary blue/frosted ice!
        double r = Math.max(8.0D, radius);
        Map<BlockPos, BlockState> savedBlocks = new HashMap<>();
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 140, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (Map.Entry<BlockPos, BlockState> e : savedBlocks.entrySet()) {
                            BlockState cur = srv.getBlockState(e.getKey());
                            if (cur.is(Blocks.BLUE_ICE) || cur.is(Blocks.FROSTED_ICE) || cur.is(Blocks.SNOW)) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        return false;
                    }
                    if (elapsed <= 108 && elapsed % 24 == 12) {
                        double ox = (srv.random.nextDouble() - 0.5D) * zone.radius * 0.9D;
                        double oz = (srv.random.nextDouble() - 0.5D) * zone.radius * 0.9D;
                        Vec3 cometHit = zone.center.add(ox, 0.0D, oz);
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB hitBox = new AABB(cometHit.x - 4.5D, cometHit.y - 2.0D, cometHit.z - 4.5D,
                                cometHit.x + 4.5D, cometHit.y + 4.0D, cometHit.z + 4.5D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 2.0D, 0.0D, 100L);
                            foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 100, foe.getTicksFrozen() + 75));
                            foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                            if (foe.getType().is(EntityTypeTags.UNDEAD)) {
                                foe.hurtServer(srv, srv.damageSources().magic(), 3.5F + zone.spell.getEffectiveTier() * 1.0F);
                            }
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.65F);
                        }
                        BlockPos cPos = BlockPos.containing(cometHit);
                        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-2, -1, -2), cPos.offset(2, 0, 2))) {
                            BlockState cur = srv.getBlockState(p);
                            if (cur.is(Blocks.WATER) && !savedBlocks.containsKey(p)) {
                                savedBlocks.put(p.immutable(), cur);
                                srv.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
                            } else if (cur.isAir() && srv.getBlockState(p.below()).isSolidRender()
                                    && !savedBlocks.containsKey(p) && Blocks.SNOW.defaultBlockState().canSurvive(srv, p)) {
                                savedBlocks.put(p.immutable(), cur);
                                srv.setBlockAndUpdate(p, Blocks.SNOW.defaultBlockState());
                            }
                        }
                        srv.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.55F, 0.96F, 0.85F), cometHit.x, cometHit.y + 0.8D, cometHit.z, 1, 0, 0, 0, 0);
                        srv.sendParticles(ParticleTypes.SNOWFLAKE, cometHit.x, cometHit.y + 0.8D, cometHit.z, 26, 1.4D, 0.5D, 1.4D, 0.08D);
                        srv.sendParticles(ParticleTypes.END_ROD, cometHit.x, cometHit.y + 0.8D, cometHit.z, 20, 1.2D, 0.5D, 1.2D, 0.08D);
                        srv.playSound(null, cPos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.35F);
                    }
                    return true;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.AURORA.name());
        tag.putInt("SpellEidolonTintRGB", BOREALIS_ARCHON_HEX_COLOR);
        level.sendParticles(ParticleTypes.SNOWFLAKE, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 22, 0.45D, 0.55D, 0.45D, 0.05D);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 18, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Borealis Prism-Flash Step: 14m flash leaving a shimmering frozen hologram decoy at departure taunting 2.0s (40t)
        // before shattering in a cryo-light burst, while granting Frost Walker + 30% sprint speed for 4.0s (80t)!
        SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.AURORA,
                10.0F, 40, "Borealis Frozen Hologram", null
        );
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, level.getGameTime() + 80L);
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 1, false, true));
        level.sendParticles(ParticleTypes.SNOWFLAKE, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 16, 0.35D, 0.45D, 0.35D, 0.04D);
        level.sendParticles(ParticleTypes.END_ROD, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 14, 0.35D, 0.45D, 0.35D, 0.05D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Polar Cryo-Refraction Transmute: water -> packed/blue ice, lava -> obsidian/crying obsidian,
        // glass -> tinted glass, small flowers -> Auroral Buttercup!
        if (state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, potencyTier >= 5 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
            level.setBlockAndUpdate(pos, Blocks.BLUE_ICE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.LAVA)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState());
            return true;
        }
        String key = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (state.is(Blocks.GLASS) || (key.endsWith("_stained_glass") && !state.is(Blocks.TINTED_GLASS))) {
            level.setBlockAndUpdate(pos, Blocks.TINTED_GLASS.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.SMALL_FLOWERS) && !state.is(ModBlocks.AURORAL_BUTTERCUP.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AURORAL_BUTTERCUP.get().defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.isEmpty()) return false;
        String key = BuiltInRegistries.ITEM.getKey(in.getItem()).getPath();
        if (in.is(Items.ICE) || in.is(Items.PACKED_ICE)) {
            itemEntity.setItem(new ItemStack(Items.BLUE_ICE, in.getCount()));
            return true;
        }
        if (in.is(Items.GLASS) || key.endsWith("_stained_glass")) {
            itemEntity.setItem(new ItemStack(Items.TINTED_GLASS, in.getCount()));
            return true;
        }
        if (in.is(ItemTags.SMALL_FLOWERS) && !in.is(ModBlocks.AURORAL_BUTTERCUP.get().asItem())) {
            itemEntity.setItem(new ItemStack(ModBlocks.AURORAL_BUTTERCUP.get().asItem(), in.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Sub-Zero Polar Stasis Hex: cuts attack speed by 50%, slashes movement speed by 40% (Slowness II),
        // continuous freezing frostbite ticks, and Glowing!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.50D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, Math.min(4, Math.max(1, Math.round(1.5F * mult))), true, true));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, true, true));
        target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 120, target.getTicksFrozen() + Math.round(55 * mult)));
        target.hurtServer(level, level.damageSources().freeze(), (1.40F + potencyTier * 0.45F) * mult);
        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.85D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.03D);
        return true;
    }
}
