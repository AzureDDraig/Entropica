package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Prismatic Materia (#51 — Refined Chimera + Radiant)</b>
 * (Spellcrafting System Guide Section 6.53).
 */
public final class PrismaticSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int PRISMATIC_DIAMOND_HEX_COLOR = 0x72F6FF;
    private static final ResourceLocation STAR_SHOWER_ATK_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath("entropica", "prismatic_star_shower_atk_speed");

    @Override
    public EssenceType materiaType() {
        return EssenceType.PRISMATIC;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Prismatic Kaleidoscope Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Prismatic Oscillating Light-Disc";
            case BEAM -> "Bifurcated Rainbow Spectrum Ray & Prismatic Meltdown";
            case RADIAL_BURST -> "Rainbow Bloom Shockwave";
            case TOUCH_STRIKE -> "Prismatic Crystalline Palm-Shatter";
            case AURA -> "Chromatic Dispersion Aura";
            case GLYPH -> "Refractive Prism-Vault Glyph";
            case CONE -> "Chromatic Prismatic Deluge Cone";
            case SKYFALL -> "Chromatic Star-Shower";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Prismatic Refractive Phase-Step";
            case TRANSMUTE, BREAK -> "Prismatic Ore Metamorphosis Transmutation";
            case AFFLICTION -> "Prismatic Refractive Dyschromia / Chromatic Overload Hex";
            default -> "Prismatic Refractive " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Prismatic Kaleidoscope Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        ApexEschatonSingularityHelper.deflectHostileProjectiles(level,
                projectile.getOwner() instanceof LivingEntity l ? l : null,
                projectile.getBoundingBox().inflate(2.0D));
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        // Prismatic Oscillating Light-Disc: 6 ricochets off solid blocks & entities handled in SpellProjectileEntity!
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        // Leaves luminous refractive color filaments that deflect hostile magic projectiles!
        ApexEschatonSingularityHelper.deflectHostileProjectiles(level,
                projectile.getOwner() instanceof LivingEntity l ? l : null,
                projectile.getBoundingBox().inflate(1.85D));
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.01D);
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
        // Bifurcated Rainbow Spectrum Ray & Prismatic Meltdown:
        // Splits into 3 secondary tracking beams upon contact, siphons 1.5 hearts/s (3.0 HP), melts 2 armor/s;
        // 5.0s continuous -> Prismatic Meltdown (-5 armor, 4.0s paralysis, party radiant absorption shields)!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();

        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 80L);
            SpellCombatReactionEngine.notifyMagicalHeal(caster, 3.0F);
            caster.heal(3.0F);

            // Split into up to 3 secondary tracking beams on adjacent hostiles!
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(6.5D),
                    e -> e.isAlive() && e != hit && e != caster && !hitEntities.contains(e)
                            && (e instanceof Monster || e == caster.getLastHurtMob())
                            && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, safeguard));
            int splits = Math.min(3, nearby.size());
            for (int i = 0; i < splits; i++) {
                LivingEntity sec = nearby.get(i);
                SpellCombatReactionEngine.applyTimedAttributeShred(sec, level, 2.0D, 0.0D, 80L);
                sec.hurtServer(level, level.damageSources().magic(), 4.5F + spell.getEffectiveTier() * 1.1F);
                level.sendParticles(ParticleTypes.END_ROD, sec.getX(), sec.getY(0.9D), sec.getZ(), 6, 0.2D, 0.25D, 0.2D, 0.03D);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("PrismaticBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("PrismaticBeamStacks").orElse(0) + 1 : 1;
            data.putLong("PrismaticBeamLastTick", now);
            data.putInt("PrismaticBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("PrismaticBeamStacks", 0);
                AABB meltdown = hit.getBoundingBox().inflate(6.0D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, meltdown, e -> e.isAlive() && e != caster)) {
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 180L);
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 80, 0)); // 4.0s!
                    foe.hurtServer(level, level.damageSources().magic(), 12.0F + spell.getEffectiveTier() * 2.2F);
                }
                SpellLingeringZoneManager.grantPartyAbsorption(level, caster, 12.0F, 24.0F, 12.0D);
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
        // Rainbow Bloom Shockwave: 10m shockwave (ZERO light blocks!), purges terrain hazards (fire, cobwebs, potion clouds),
        // shatters 5 armor, restores 8 hearts (16.0 HP) to all party members!
        double r = Math.max(10.0D, radius);
        BlockPos cPos = BlockPos.containing(center);
        int ir = (int) Math.min(10, Math.ceil(r));
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-ir, -2, -ir), cPos.offset(ir, 3, ir))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof BaseFireBlock || bs.is(Blocks.COBWEB)) {
                level.removeBlock(p, false);
            }
        }
        AABB box = new AABB(center.x - r, center.y - 2.5D, center.z - r, center.x + r, center.y + 4.5D, center.z + r);
        for (AreaEffectCloud cloud : level.getEntitiesOfClass(AreaEffectCloud.class, box)) {
            cloud.discard();
        }
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                SpellCombatReactionEngine.notifyMagicalHeal(ent, 16.0F); // 8 hearts!
                ent.heal(16.0F);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 5.0D, 0.0D, 160L);
        }
        SpellCombatReactionEngine.notifyMagicalHeal(caster, 16.0F);
        caster.heal(16.0F);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0D, center.z, 36, 2.5D, 0.5D, 2.5D, 0.06D);
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
        // Prismatic Crystalline Palm-Shatter: blinding flashbang, disarms 4.0s (80t), -5 armor, Glowing 10.0s (200t)!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 160L);
            ApexEschatonSingularityHelper.disableShield(hit, 80);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 3)); // 4.0s disarm!
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0)); // 10.0s chromatic outline!
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
        // Chromatic Dispersion Aura (ZERO physical light blocks!)
        int dur = 240 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_PRISMATIC_SHROUD_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(1.0D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.04D);
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
        // Refractive Prism-Vault Glyph: 4m trigger -> encases trapped mobs in solid prismatic crystal blocks for 5.0s (100t),
        // shredding 4 armor and forcing mobs to drop/disarm their held weapons upon breaking free!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 360, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.15D, zone.center.z, 4, 0.8D, 0.05D, 0.8D, 0.01D);
                    }
                    if (elapsed < 10) return true;

                    AABB trig = new AABB(zone.center.x - 4.0D, zone.center.y - 1.5D, zone.center.z - 4.0D,
                            zone.center.x + 4.0D, zone.center.y + 2.5D, zone.center.z + 4.0D);
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (victims.isEmpty()) return true;

                    Map<BlockPos, BlockState> prismShell = new HashMap<>();
                    List<LivingEntity> trapped = new ArrayList<>(victims);
                    for (LivingEntity foe : trapped) {
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 4.0D, 0.0D, 180L);
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 100, 0));
                        BlockPos feet = foe.blockPosition();
                        for (BlockPos p : List.of(feet, feet.above())) {
                            if (srv.getBlockState(p).isAir() && !prismShell.containsKey(p)) {
                                prismShell.put(p.immutable(), Blocks.AIR.defaultBlockState());
                                srv.setBlockAndUpdate(p, Blocks.CYAN_STAINED_GLASS.defaultBlockState());
                            }
                        }
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 5.0D, 100, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl < 100) return true;
                                for (BlockPos p : prismShell.keySet()) {
                                    if (sLvl.getBlockState(p).is(Blocks.CYAN_STAINED_GLASS)) {
                                        sLvl.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                                    }
                                }
                                for (LivingEntity foe : trapped) {
                                    if (!foe.isAlive()) continue;
                                    if (foe instanceof Monster mob && !mob.getMainHandItem().isEmpty()) {
                                        ItemStack weapon = mob.getMainHandItem().copy();
                                        mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                                        sLvl.addFreshEntity(new ItemEntity(sLvl, mob.getX(), mob.getY(0.5D), mob.getZ(), weapon));
                                    } else {
                                        foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 3));
                                    }
                                }
                                sLvl.playSound(null, BlockPos.containing(sZone.center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.2F);
                                return false;
                            }
                    ));
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
        // Chromatic Prismatic Deluge Cone: -5 armor, deflects projectiles, Chromatic Sensory Overload (25s AI disruption);
        // allies cleansed of negative effects + +30% move speed (Speed II) for 12.0s (240t)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        ApexEschatonSingularityHelper.deflectHostileProjectiles(level, caster,
                new AABB(caster.getEyePosition(), caster.getEyePosition().add(dir.scale(coneRange))).inflate(3.0D));

        for (LivingEntity hit : hitTargets) {
            if (ApexEschatonSingularityHelper.isFriendly(hit, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                hit.addEffect(new MobEffectInstance(MobEffects.SPEED, 240, 1));
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 180L);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500); // 25.0s!
        }
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
        // Chromatic Star-Shower: 14m radius, 20.0s (400t) + 10.0s (200t) Crystalline Resonance Field (600t total):
        // shatters 6 armor, knocks down, lingering field doubles ally attack speed (+100%), Jump Boost III, burns corrupt mobs!
        double r = Math.max(14.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 600, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        AABB clean = new AABB(zone.center.x - zone.radius - 4.0D, zone.center.y - 4.0D, zone.center.z - zone.radius - 4.0D,
                                zone.center.x + zone.radius + 4.0D, zone.center.y + 8.0D, zone.center.z + zone.radius + 4.0D);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, clean)) {
                            AttributeInstance inst = ent.getAttribute(Attributes.ATTACK_SPEED);
                            if (inst != null) inst.removeModifier(STAR_SHOWER_ATK_SPEED_ID);
                        }
                        return false;
                    }
                    boolean shower = elapsed <= 400;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + (shower ? 4.5D : 0.4D), zone.center.z,
                                24, zone.radius * 0.55D, shower ? 2.2D : 0.3D, zone.radius * 0.55D, 0.05D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.0D, zone.center.z + zone.radius);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner)) {
                                if (!shower) {
                                    ent.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 35, 2)); // Jump Boost III
                                    AttributeInstance inst = ent.getAttribute(Attributes.ATTACK_SPEED);
                                    if (inst != null) {
                                        inst.removeModifier(STAR_SHOWER_ATK_SPEED_ID);
                                        inst.addTransientModifier(new AttributeModifier(STAR_SHOWER_ATK_SPEED_ID, 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                                    }
                                }
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            if (shower) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 6.0D, 0.0D, 180L);
                                ent.setDeltaMovement(ent.getDeltaMovement().x * 0.25D, -0.45D, ent.getDeltaMovement().z * 0.25D);
                                ent.hasImpulse = true;
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, ent, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.50F);
                            } else if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(ent)) {
                                ent.igniteForSeconds(3);
                                ent.hurtServer(srv, srv.damageSources().magic(), 5.0F);
                            }
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
        tag.putString("SpellEidolonMateria", EssenceType.PRISMATIC.name());
        tag.putInt("SpellEidolonTintRGB", PRISMATIC_DIAMOND_HEX_COLOR);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 28, 0.5D, 0.6D, 0.5D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Prismatic Refractive Phase-Step: 18m through walls; sheds 100% aggro, Invisible 2.0s (40t),
        // leaves Prismatic Mirror Decoy at departure!
        SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.PRISMATIC,
                20.0F, 100, "Prismatic Mirror Decoy", null
        );
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0));
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 22.0D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.PLAYERS, 1.0F, 1.4F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Prismatic Ore Metamorphosis Transmutation [5.0s Channel]:
        // Smelts all raw ores in an 8-block radius without fuel, with 10% chance to transmute into a different ore up to Diamond (no bonus yield)!
        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 8, potencyTier, EssenceType.PRISMATIC,
                "Prismatic Ore Metamorphosis Transmutation", null)) {
            return true;
        }

        Item smelted = ApexEschatonSingularityHelper.resolveSmeltedBlockOreOutput(state.getBlock());
        if (smelted != null) {
            Item out = ApexEschatonSingularityHelper.rollPrismaticMetamorphosisSmeltOutput(level.random, smelted);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, new ItemStack(out, 1));
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        Item smelted = ApexEschatonSingularityHelper.resolveSmeltedOreOutput(stack.getItem());
        if (smelted != null) {
            // Strictly 1x yield (no bonus yield), 10% chance per item stack roll to metamorphose up to Diamond!
            Item out = ApexEschatonSingularityHelper.rollPrismaticMetamorphosisSmeltOutput(level.random, smelted);
            itemEntity.setItem(new ItemStack(out, stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Prismatic Refractive Dyschromia / Chromatic Overload Hex: -40% toughness, 50% fumble miss,
        // inverts positive buffs into damage, continuous radiant-prismatic burn (2x vs Undead/Demonic/Blight/Blood)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);

        int invertedBuffs = 0;
        for (MobEffectInstance eff : new ArrayList<>(target.getActiveEffects())) {
            if (eff.getEffect().value().isBeneficial()) {
                target.removeEffect(eff.getEffect());
                invertedBuffs++;
            }
        }
        float dmg = (1.75F + potencyTier * 0.50F + invertedBuffs * 2.5F) * mult;
        if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(target)) {
            dmg *= 2.0F;
        }
        target.hurtServer(level, level.damageSources().magic(), dmg);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.9D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.03D);
        return true;
    }
}
