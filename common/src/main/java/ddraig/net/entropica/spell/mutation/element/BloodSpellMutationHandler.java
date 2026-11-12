package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
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
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Blood Materia (Vital Fusion #22)</b>
 * (Spellcrafting System Guide Section 6.23).
 */
public final class BloodSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SANGUINE_HEMOMANCER_HEX_COLOR = 0x8A0303;

    @Override
    public EssenceType materiaType() {
        return EssenceType.BLOOD;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Sanguine Capillary Chains";
            default -> "Sanguine " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Sanguine Coagulation Needle";
            case BEAM -> "Sanguine Exsanguination Tether";
            case RADIAL_BURST -> "Boiling Blood Mist Detonation";
            case TOUCH_STRIKE -> "Vampiric Exsanguination Strike";
            case AURA -> "Coagulated Crimson Carapace";
            case GLYPH -> "Sanguine Spike Snare";
            case CONE -> "Sanguine Shrapnel Deluge";
            case SKYFALL -> "Sanguine Deluge Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Sanguine Dash Blink";
            case TRANSMUTE, BREAK -> "Flesh Grafting Transmute";
            case AFFLICTION -> "Sanguine Exsanguination Hex";
            default -> "Sanguine " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Sanguine Capillary Chains: Hemorrhage, siphons 20% healing to caster, pulls chained enemies 2m closer!
        inflictHemorrhage(level, toTarget, 80, 0);
        SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, spell.getEffectiveTier(), 0.20F, 1.0F);

        Vec3 mid = fromTarget.position().add(toTarget.position()).scale(0.5D);
        Vec3 pA = mid.subtract(fromTarget.position()).normalize().scale(0.55D);
        Vec3 pB = mid.subtract(toTarget.position()).normalize().scale(0.55D);
        fromTarget.setDeltaMovement(pA.x, 0.16D, pA.z);
        toTarget.setDeltaMovement(pB.x, 0.16D, pB.z);
        fromTarget.hasImpulse = true;
        toTarget.hasImpulse = true;
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Sanguine Coagulation Needle: pierces 2, Hemorrhage for 4s (80t), leaches 30% damage to heal caster!
        if (!(hitEntity instanceof LivingEntity living)) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) return;

        inflictHemorrhage(level, living, 80, 0);
        SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, spell.getEffectiveTier(), 0.30F, 1.0F);
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Sanguine Exsanguination Tether: 35% lifesteal, stacks Hemorrhage & Slowness (up to 45%), slowing blood puddles underfoot!
        int tier = spell.getEffectiveTier();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            inflictHemorrhage(level, hit, 100, 1);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
            SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, tier, 0.35F, apparatusPotency);

            Vec3 puddleCenter = hit.position();
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, puddleCenter, 2.0D, 60, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        if (elapsed % 10 == 0) {
                            LivingEntity owner = zone.resolveCaster(srv);
                            AABB box = new AABB(zone.center.x - 2.0D, zone.center.y - 1.0D, zone.center.z - 2.0D,
                                    zone.center.x + 2.0D, zone.center.y + 1.5D, zone.center.z + 2.0D);
                            for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                                v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 1));
                            }
                        }
                        return true;
                    }
            ));
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Boiling Blood Mist Detonation: 6m 3s Blindness, melts incoming arrows, +2 Absorption hearts (4.0F) per enemy hit!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        int enemiesHit = 0;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            inflictHemorrhage(level, foe, 60, 0);
            enemiesHit++;
        }
        for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, box)) {
            if (arrow.getOwner() != caster) {
                arrow.discard();
            }
        }
        if (enemiesHit > 0) {
            float absGain = enemiesHit * 4.0F; // +2 Absorption hearts per enemy hit!
            caster.setAbsorptionAmount(Math.min(24.0F, caster.getAbsorptionAmount() + absGain));
        }
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, center.x, center.y + 1.0D, center.z, 45, 2.2D, 0.8D, 2.2D, 0.06D);
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Vampiric Exsanguination Strike: 6s (120t) severe Hemorrhage, 40% lifesteal, kill grants +2 hunger shanks & Speed II 5s!
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            inflictHemorrhage(level, hit, 120, 1);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_BLOOD_FEAST_UNTIL, now + 60L);
            SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, tier, 0.40F, apparatusPotency);
        }
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Coagulated Crimson Carapace: +6 Armor, +2 Armor Toughness, slowly consumes hunger saturation over time!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_BLOOD_CARAPACE_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 2.0D, 0.0D, 0.0D, dur);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, caster.getX(), caster.getY(0.8D), caster.getZ(), 24, 0.45D, 0.5D, 0.45D, 0.05D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Sanguine Spike Snare: erupts coagulated crimson spikes rooting trapped enemies for 3.5s (70t), severe Hemorrhage, leaches 30% healing!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                            zone.center.x + 3.0D, zone.center.y + 2.0D, zone.center.z + 3.0D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner);
                    if (stepped.isEmpty()) return true;

                    int hitCount = 0;
                    for (LivingEntity v : stepped) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 6));
                        v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 70, 0));
                        inflictHemorrhage(srv, v, 80, 1);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                        hitCount++;
                    }
                    if (hitCount > 0) {
                        SpellCombatReactionEngine.applyTierScaledLifesteal(srv, owner, zone.spell.getEffectiveTier(), 0.30F, zone.apparatusPotency * hitCount);
                    }
                    // Erupt temporary coagulated crimson spikes in empty air around snare!
                    List<BlockPos> spikes = new ArrayList<>();
                    BlockPos c = BlockPos.containing(zone.center);
                    for (BlockPos p : BlockPos.betweenClosed(c.offset(-1, 0, -1), c.offset(1, 0, 1))) {
                        if (srv.getBlockState(p).isAir() && srv.getBlockState(p.below()).isSolidRender()) {
                            srv.setBlockAndUpdate(p, Blocks.CRIMSON_ROOTS.defaultBlockState());
                            spikes.add(p.immutable());
                        }
                    }
                    if (!spikes.isEmpty()) {
                        SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                                owner, zone.center, 2.0D, 70, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                                (ssrv, szone, selapsed) -> {
                                    if (selapsed < szone.maxDurationTicks) return true;
                                    for (BlockPos sp : spikes) {
                                        if (ssrv.getBlockState(sp).is(Blocks.CRIMSON_ROOTS)) {
                                            ssrv.removeBlock(sp, false);
                                        }
                                    }
                                    return false;
                                }
                        ));
                    }
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Sanguine Shrapnel Deluge: -25% armor toughness 5s (100t), 3m knockback, Hemorrhage, 20% lifesteal!
        int hits = 0;
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.25D, 100L);
            hit.setDeltaMovement(direction.x * 0.85D, 0.28D, direction.z * 0.85D);
            hit.hasImpulse = true;
            inflictHemorrhage(level, hit, 80, 0);
            hits++;
        }
        if (hits > 0) {
            SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, spell.getEffectiveTier(), 0.20F, apparatusPotency * hits);
        }
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Sanguine Deluge Downpour: 16m crimson bloodstorm for 12s (240t) stacking Hemorrhage & Slowness (up to 50%), 15% lifesteal, caster +30% sprint speed!
        double r = Math.max(8.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 240, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.CRIMSON_SPORE, zone.center.x, zone.center.y + 4.5D, zone.center.z, 24, zone.radius * 0.55D, 1.4D, zone.radius * 0.55D, 0.05D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long now = srv.getGameTime();
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        int hits = 0;
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (e == owner) {
                                e.addEffect(new MobEffectInstance(MobEffects.SPEED, 35, 1, true, false));
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                CompoundTag ed = EntityHelper.getPersistentData(e);
                                long lastRain = ed.getLong("BloodRainLastTick").orElse(0L);
                                int stacks = (now - lastRain <= 35L) ? Math.min(2, ed.getInt("BloodRainStacks").orElse(0) + 1) : 0;
                                ed.putLong("BloodRainLastTick", now);
                                ed.putInt("BloodRainStacks", stacks);
                                inflictHemorrhage(srv, e, 50, Math.min(1, stacks));
                                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, stacks)); // Escalates Slowness I -> II -> III (up to 50%+)!
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, e, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
                                hits++;
                            }
                        }
                        if (hits > 0 && owner != null && owner.isAlive()) {
                            SpellCombatReactionEngine.applyTierScaledLifesteal(srv, owner, zone.spell.getEffectiveTier(), 0.15F, zone.apparatusPotency * 0.35F * hits);
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.BLOOD.name());
        tag.putInt("SpellEidolonTintRGB", SANGUINE_HEMOMANCER_HEX_COLOR);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Sanguine Dash Blink: surges forward 16m in a jet of crimson spray, knocking aside mobs along the path!
        AABB line = new AABB(departurePos, arrivalPos).inflate(1.5D);
        Vec3 surgeDir = arrivalPos.subtract(departurePos).normalize();
        Vec3 perp = new Vec3(-surgeDir.z, 0.25D, surgeDir.x);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, line)) {
            if (foe == caster || !foe.isAlive() || foe.isAlliedTo(caster)) continue;
            foe.setDeltaMovement(perp.scale(0.85D));
            foe.hasImpulse = true;
            inflictHemorrhage(level, foe, 60, 0);
        }
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 22, 0.45D, 0.45D, 0.45D, 0.05D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Flesh Grafting Transmute
        if (state.is(Blocks.SOUL_SAND) || state.is(Blocks.NETHERRACK)) {
            level.setBlockAndUpdate(pos, Blocks.CRIMSON_NYLIUM.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.WITHER_ROSE) || state.is(Blocks.DEAD_BUSH)) {
            level.setBlockAndUpdate(pos, ModBlocks.SANGUINE_LILY.get().defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        // Converts leather and rotten flesh into fresh raw porkchops!
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.ROTTEN_FLESH) || in.is(Items.LEATHER)) {
            itemEntity.setItem(new ItemStack(Items.PORKCHOP, in.getCount()));
            level.sendParticles(ParticleTypes.CRIMSON_SPORE, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.03D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Sanguine Exsanguination Hex: siphons HP directly to caster every 3s, severe Hemorrhage on movement, regen lock!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        inflictHemorrhage(level, target, 35, 0);

        float drain = (1.6F + potencyTier * 0.55F) * mult;
        target.hurtServer(level, level.damageSources().magic(), drain);

        if ((level.getGameTime() / 20L) % 3L == 0L) {
            LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
            if (hexCaster != null && hexCaster.isAlive()) {
                SpellCombatReactionEngine.notifyMagicalHeal(hexCaster, drain);
                hexCaster.heal(drain);
            }
        }
        return true;
    }

    private static void inflictHemorrhage(ServerLevel level, LivingEntity target, int ticks, int amp) {
        target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BLEEDING.get()), ticks, amp));
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, level.getGameTime() + ticks);
    }
}
