package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModItems;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Radiant Materia (Base Type #7)</b>
 * (Spellcrafting System Guide Section 6.8).
 */
public final class RadiantSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SOLAR_SERAPH_HEX_COLOR = 0xFFE278;

    @Override
    public EssenceType materiaType() {
        return EssenceType.RADIANT;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Prismatic Starlight Chains";
            default -> "Solar Starlight " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Homing Dawn Star";
            case BEAM -> "Solar Thermal Lance";
            case RADIAL_BURST -> "Supernova Flashbang";
            case TOUCH_STRIKE -> "Holy Smite & Daylight Ignition";
            case AURA -> "Pacification Shroud";
            case GLYPH -> "Sunburst Confinement Sigil";
            case CONE -> "Cleansing Mist Cone";
            case SKYFALL -> "Daylight Megalith Pillar";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Luminous Cleanse";
            case TRANSMUTE, BREAK -> "Holy Purification";
            case AFFLICTION -> "Sun-Scorched Smite Hex";
            default -> "Solar " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Prismatic Starlight Chains: reveals victims for 5s (100t) & strips Invisibility
        fromTarget.removeEffect(MobEffects.INVISIBILITY);
        toTarget.removeEffect(MobEffects.INVISIBILITY);
        fromTarget.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
        toTarget.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));

        Vec3 p1 = fromTarget.getEyePosition();
        Vec3 p2 = toTarget.getEyePosition();
        double dist = p1.distanceTo(p2);
        Vec3 step = p2.subtract(p1).normalize();
        for (double d = 0.0D; d <= dist; d += 0.5D) {
            Vec3 pt = p1.add(step.scale(d));
            level.sendParticles(ParticleTypes.END_ROD, pt.x, pt.y, pt.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        // Homing Dawn Star: seeks out the lowest-health Undead enemy within 16 blocks!
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        if (projectile.tickCount % 2 == 0) {
            List<LivingEntity> undead = level.getEntitiesOfClass(
                    LivingEntity.class,
                    projectile.getBoundingBox().inflate(16.0D),
                    e -> e.isAlive() && e != projectile.getOwner() && e.getType().is(EntityTypeTags.UNDEAD)
            );
            undead.stream().min(Comparator.comparingDouble(LivingEntity::getHealth)).ifPresent(target -> {
                Vec3 cur = projectile.getDeltaMovement();
                double spd = Math.max(0.8D, cur.length());
                Vec3 desired = target.getEyePosition().subtract(projectile.position()).normalize().scale(spd);
                projectile.setDeltaMovement(cur.scale(0.65D).add(desired.scale(0.35D)).normalize().scale(spd));
                projectile.hasImpulse = true;
            });
        }
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        if (hitEntity instanceof LivingEntity living && living.getType().is(EntityTypeTags.UNDEAD)) {
            living.igniteForSeconds(5);
            living.invulnerableTime = 0;
            living.hurtServer(level, level.damageSources().indirectMagic(caster, caster), 4.0F + spell.getEffectiveTier() * 2.0F);
            level.sendParticles(ParticleTypes.END_ROD, living.getX(), living.getY(0.8D), living.getZ(), 18, 0.3D, 0.4D, 0.3D, 0.12D);
        }
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Solar Thermal Lance: smites undead, ignites Phantoms, melts ice blocks along beam
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            if (hit.getType().is(EntityTypeTags.UNDEAD) || hit instanceof Phantom) {
                hit.igniteForSeconds(6);
            }
        }
        int steps = Math.min(20, (int) Math.ceil(maxReach));
        for (int i = 1; i <= steps; i++) {
            BlockPos p = BlockPos.containing(caster.getEyePosition().add(direction.scale(i)));
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.SNOW_BLOCK)) {
                level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
            }
        }
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Holy Smite & Daylight Ignition: +50% bonus smite damage vs Undead, radiant fire, and stardust drop purification!
        float smiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(spell.getEffectiveTier()) * Math.max(0.5F, apparatusPotency);
        long purifyUntil = level.getGameTime() + 40L;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL, purifyUntil);
                hit.igniteForSeconds(6);
                hit.invulnerableTime = 0;
                hit.hurtServer(level, level.damageSources().indirectMagic(caster, caster), Math.max(4.0F, smiteBonus));
                level.sendParticles(ParticleTypes.END_ROD, hit.getX(), hit.getY(0.7D), hit.getZ(), 20, 0.35D, 0.45D, 0.35D, 0.1D);
            }
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Supernova Flashbang: 4s (80t) Blindness & Weakness on hostile monsters
        AABB box = new AABB(center.x - radius, center.y - 2.0D, center.z - radius, center.x + radius, center.y + 2.5D, center.z + radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity == caster || !entity.isAlive()) continue;
            if (entity instanceof Monster) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                Vec3 away = entity.position().subtract(center).normalize().scale(1.0D);
                entity.setDeltaMovement(away.x, 0.3D, away.z);
                entity.hasImpulse = true;
            }
        }
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0D, center.z, 36, radius * 0.4D, 0.5D, radius * 0.4D, 0.15D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.5F);
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Pacification Shroud
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_RADIANT_SHROUD_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.4D, 0.5D, 0.4D, 0.05D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Sunburst Confinement Sigil: 3s (60t) blinding cage & heavy Radiant smite on undead
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 2.8D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - 2.2D, zone.center.y - 1.0D, zone.center.z - 2.2D,
                            zone.center.x + 2.2D, zone.center.y + 2.5D, zone.center.z + 2.2D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner);
                    if (stepped.isEmpty()) return true;

                    for (LivingEntity v : stepped) {
                        v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 5));
                        if (v.getType().is(EntityTypeTags.UNDEAD)) {
                            v.igniteForSeconds(5);
                        }
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 1.2F);
                    }
                    srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 1.0D, zone.center.z, 30, 0.8D, 1.2D, 0.8D, 0.08D);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Cleansing Mist Cone: heals friendly allies & cleanses negative status effects
        float healAmt = 3.0F + spell.getEffectiveTier() * 1.5F;
        for (LivingEntity hit : hitTargets) {
            if (hit.isAlliedTo(caster) || !(hit instanceof Monster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                SpellCombatReactionEngine.notifyMagicalHeal(hit, healAmt);
                hit.heal(healAmt);
            }
        }
        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(caster);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX() + direction.x * 2.0D, caster.getEyeY() + direction.y * 2.0D, caster.getZ() + direction.z * 2.0D, 22, 0.7D, 0.4D, 0.7D, 0.06D);
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Daylight Megalith Pillar: crashes down after 2 seconds (40t) with holy shockwaves
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, Math.max(4.5D, radius), 42, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 40) {
                        if (elapsed % 4 == 0) {
                            srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 6.0D - (elapsed * 0.12D), zone.center.z, 14, 0.6D, 0.6D, 0.6D, 0.02D);
                        }
                        return true;
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 4.0D, zone.center.z + zone.radius);
                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                        if (v.getType().is(EntityTypeTags.UNDEAD)) v.igniteForSeconds(6);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 1.25F);
                    }
                    srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 1.5D, zone.center.z, 45, 1.0D, 2.0D, 1.0D, 0.15D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.1F, 1.4F);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.RADIANT.name());
        tag.putInt("SpellEidolonTintRGB", SOLAR_SERAPH_HEX_COLOR);
        SpellCombatReactionEngine.notifyMagicalHeal(caster, 2.5F);
        caster.heal(2.5F);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 24, 0.5D, 0.6D, 0.5D, 0.06D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Luminous Cleanse: blinding flash at departure + cleanses negative status effects on arrival!
        AABB depBox = new AABB(departurePos.x - 3.5D, departurePos.y - 2.0D, departurePos.z - 3.5D,
                departurePos.x + 3.5D, departurePos.y + 3.0D, departurePos.z + 3.5D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox)) {
            if (foe != caster && foe instanceof Monster) {
                foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            }
        }
        level.sendParticles(ParticleTypes.END_ROD, departurePos.x, departurePos.y + 0.9D, departurePos.z, 22, 0.4D, 0.5D, 0.4D, 0.12D);
        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(caster);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Holy Purification: purifies rotten flesh -> leather, strips curses on dropped gear, soul sand -> sand, cures ZombieVillager
        boolean didAny = false;
        AABB area = new AABB(pos).inflate(2.5D);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        for (ZombieVillager zv : level.getEntitiesOfClass(ZombieVillager.class, area)) {
            var villager = zv.convertTo(EntityType.VILLAGER, net.minecraft.world.entity.ConversionParams.single(zv, false, false), v -> {});
            if (villager != null) didAny = true;
        }
        if (state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)) {
            level.setBlockAndUpdate(pos, Blocks.SAND.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.MYCELIUM) || state.is(Blocks.PODZOL)) {
            level.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.NETHERRACK)) {
            level.setBlockAndUpdate(pos, Blocks.QUARTZ_BLOCK.defaultBlockState());
            return true;
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;

        if (stack.is(Items.ROTTEN_FLESH)) {
            itemEntity.setItem(new ItemStack(Items.LEATHER, stack.getCount()));
            level.sendParticles(ParticleTypes.END_ROD, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.04D);
            return true;
        }
        if (stack.is(Items.SOUL_SAND) || stack.is(Items.SOUL_SOIL)) {
            itemEntity.setItem(new ItemStack(Items.SAND, stack.getCount()));
            return true;
        }
        if (stack.is(Items.WITHER_ROSE)) {
            itemEntity.setItem(new ItemStack(ModItems.STARLIGHT_FLAKES.get(), stack.getCount()));
            return true;
        }

        // Cleanse curses (Binding, Vanishing, etc.) from dropped equipment!
        boolean[] strippedCurse = {false};
        EnchantmentHelper.updateEnchantments(stack, mutable ->
                mutable.removeIf(holder -> {
                    if (holder.is(EnchantmentTags.CURSE)) {
                        strippedCurse[0] = true;
                        return true;
                    }
                    return false;
                })
        );
        if (strippedCurse[0]) {
            itemEntity.setItem(stack);
            level.sendParticles(ParticleTypes.END_ROD, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 14, 0.25D, 0.25D, 0.25D, 0.06D);
            level.playSound(null, itemEntity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.5F);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Sun-Scorched Smite Hex: melts 4 physical armor points (scaled by stack), continuous radiant smite (boosted vs Undead)
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 4.0D * mult, 0.0D, 35L);
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 35, 0, true, false));
        float dmg = (1.5F + potencyTier * 0.55F) * mult * (target.getType().is(EntityTypeTags.UNDEAD) ? 1.5F : 1.0F);
        target.hurtServer(level, level.damageSources().magic(), dmg);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.8D), target.getZ(), 5, 0.25D, 0.35D, 0.25D, 0.03D);
        return true;
    }
}
