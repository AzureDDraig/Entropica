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
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Vitae Materia (Vital Fusion #21)</b>
 * (Spellcrafting System Guide Section 6.22).
 */
public final class VitaeSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int VITAE_GUARDIAN_HEX_COLOR = 0xFF6B9D;

    @Override
    public EssenceType materiaType() {
        return EssenceType.VITAE;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case DOUBLE_BIFURCATE, TRIPLE_TRIFURCATE, DELAYED_BLOOM -> "Cellular Mitosis Split Flow";
            default -> "Cellular Vitality " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Spiraling Vitality Helix";
            case BEAM -> "Cardiovascular Life-Tether";
            case RADIAL_BURST -> "Cellular Overcharge Bloom";
            case TOUCH_STRIKE -> "Cellular Overcharge Strike";
            case AURA -> "Cellular Overdrive Shroud";
            case GLYPH -> "Cellular Overcharge Mine";
            case CONE -> "Cellular Rejuvenation Spray";
            case SKYFALL -> "Font of Vitality Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Cardiovascular Shock Blink";
            case TRANSMUTE, BREAK -> "Vitality Synthesis Transmute";
            case AFFLICTION -> "Hyper-Metabolic Arrhythmia Hex";
            default -> "Vitality " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onProjectileImpactOrExpireFromFlow(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Vec3 splitOrigin, SpellAssembly spell, SubSpellSlot slot) {
        // Cellular Mitosis Split Flow: splits into 2 smaller pulsating heart-orbs seeking lowest-HP ally or hostile!
        projectile.spawnSplitChild(level, splitOrigin, -25.0F, true, 0.65F, 1.20F);
        projectile.spawnSplitChild(level, splitOrigin, 25.0F, true, 0.65F, 1.20F);
        level.sendParticles(ParticleTypes.HEART, splitOrigin.x, splitOrigin.y, splitOrigin.z, 6, 0.25D, 0.25D, 0.25D, 0.04D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        double phase = projectile.tickCount * 0.7D;
        double ox = Math.cos(phase) * 0.22D;
        double oz = Math.sin(phase) * 0.22D;
        level.sendParticles(ParticleTypes.HEART, projectile.getX() + ox, projectile.getY(), projectile.getZ() + oz, 1, 0.02D, 0.02D, 0.02D, 0.0D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Spiraling Vitality Helix: siphons positive status buffs from struck enemies directly onto the caster!
        if (caster == null || !caster.isAlive() || !(hitEntity instanceof LivingEntity living) || living == caster) return;
        if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) return;

        List<Holder<MobEffect>> toStrip = new ArrayList<>();
        for (MobEffectInstance eff : living.getActiveEffects()) {
            if (eff.getEffect().value().isBeneficial()) {
                caster.addEffect(new MobEffectInstance(eff));
                toStrip.add(eff.getEffect());
            }
        }
        for (Holder<MobEffect> h : toStrip) {
            living.removeEffect(h);
        }
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Cardiovascular Life-Tether: heartbeat sound, 25% lifesteal, cleanses allies, triggers spontaneous breeding on animals!
        level.playSound(null, caster.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.7F, 1.15F);
        int tier = spell.getEffectiveTier();
        Player playerCaster = caster instanceof Player p ? p : null;

        for (LivingEntity hit : hitEntities) {
            if (hit instanceof Animal animal && animal.canFallInLove()) {
                animal.setInLove(playerCaster);
                level.sendParticles(ParticleTypes.HEART, animal.getX(), animal.getY(0.8D), animal.getZ(), 5, 0.25D, 0.25D, 0.25D, 0.03D);
                continue;
            }
            if (hit == caster || hit.isAlliedTo(caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                float allyHeal = 2.5F + tier * 0.6F;
                SpellCombatReactionEngine.notifyMagicalHeal(hit, allyHeal);
                hit.heal(allyHeal);
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, tier, 0.25F, apparatusPotency);
            }
        }

        // Also sweep passive animals along the beam corridor for spontaneous breeding
        AABB corridor = caster.getBoundingBox().expandTowards(direction.scale(maxReach)).inflate(1.25D);
        for (Animal animal : level.getEntitiesOfClass(Animal.class, corridor)) {
            if (animal.isAlive() && animal.canFallInLove()) {
                animal.setInLove(playerCaster);
            }
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Cellular Overcharge Bloom: 6m allies gain Speed II & Jump Boost II for 8s (160t); struck enemies drop glowing life-essence healing pickup orbs!
        double r = Math.max(6.0D, radius);
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (!e.isAlive()) continue;
            if (e == caster || e.isAlliedTo(caster) || e instanceof Player) {
                e.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 1));
                e.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 160, 1));
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())) {
                EntityHelper.getPersistentData(e).putLong(SpellLingeringZoneManager.TAG_VITAE_DEATH_BLOOM_UNTIL, now + 120L);
                Vec3 orbOrigin = e.position().add(0.0D, 0.4D, 0.0D);
                float orbHeal = 3.0F + tier * 0.85F;
                level.sendParticles(ParticleTypes.HEART, orbOrigin.x, orbOrigin.y + 0.4D, orbOrigin.z, 4, 0.2D, 0.2D, 0.2D, 0.03D);

                // Spawn a 5-second (100t) glowing life-essence pickup orb at each struck enemy's position!
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        caster, orbOrigin, 2.2D, 100, now, spell, slot, fallbackMateria, apparatusPotency,
                        (srv, zone, elapsed) -> {
                            if (elapsed > zone.maxDurationTicks) return false;
                            if (elapsed % 4 == 0) {
                                srv.sendParticles(ParticleTypes.HEART, zone.center.x, zone.center.y + 0.25D, zone.center.z, 1, 0.1D, 0.1D, 0.1D, 0.01D);
                                srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.25D, zone.center.z, 2, 0.12D, 0.12D, 0.12D, 0.01D);
                            }
                            if (elapsed < 4) return true;
                            LivingEntity owner = zone.resolveCaster(srv);
                            AABB pickupBox = new AABB(zone.center.x - 2.2D, zone.center.y - 1.2D, zone.center.z - 2.2D,
                                    zone.center.x + 2.2D, zone.center.y + 2.0D, zone.center.z + 2.2D);
                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, pickupBox, LivingEntity::isAlive)) {
                                if (ally instanceof Player || ally == owner || (owner != null && ally.isAlliedTo(owner))) {
                                    SpellCombatReactionEngine.notifyMagicalHeal(ally, orbHeal);
                                    ally.heal(orbHeal);
                                    srv.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(0.85D), ally.getZ(), 5, 0.25D, 0.25D, 0.25D, 0.03D);
                                    srv.playSound(null, ally.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.35F);
                                    return false;
                                }
                            }
                            return true;
                        }
                ));
            }
        }
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Cellular Overcharge Strike: victim emits healing pulses to nearby allies whenever taking damage over the next 5s (100t)!
        long until = level.getGameTime() + 100L;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_VITAE_OVERCHARGE_UNTIL, until);
            level.sendParticles(ParticleTypes.HEART, hit.getX(), hit.getY(0.9D), hit.getZ(), 6, 0.25D, 0.3D, 0.25D, 0.04D);
        }
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Cellular Overdrive Shroud: 2x natural health regen, -50% food exhaustion, +20% sprint speed!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_VITAE_SHROUD_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.HEART, caster.getX(), caster.getY(1.0D), caster.getZ(), 12, 0.45D, 0.5D, 0.45D, 0.04D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Cellular Overcharge Mine: 35% Slowness + marks target to detonate into a healing explosion if killed within 5s (100t)!
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

                    long markUntil = srv.getGameTime() + 100L;
                    for (LivingEntity v : stepped) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
                        EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_VITAE_DEATH_BLOOM_UNTIL, markUntil);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.sendParticles(ParticleTypes.HEART, zone.center.x, zone.center.y + 0.8D, zone.center.z, 18, 1.0D, 0.4D, 1.0D, 0.05D);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Cellular Rejuvenation Spray: disorients enemies (35% miss chance) & knocks back 3m, while cleansing & granting +20% speed to caught allies!
        long disorientUntil = level.getGameTime() + 80L;
        for (LivingEntity hit : hitTargets) {
            if (hit == caster || hit.isAlliedTo(caster) || hit instanceof Player) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                hit.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 0));
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_VITAE_DISORIENTED_UNTIL, disorientUntil);
                hit.setDeltaMovement(direction.x * 0.85D, 0.25D, direction.z * 0.85D);
                hit.hasImpulse = true;
            }
        }
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Font of Vitality Downpour: 16m rain of rose life essence for 12s (240t) restoring HP/hunger, cleansing every 2s, sprouting roses!
        double r = Math.max(8.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 240, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.HEART, zone.center.x, zone.center.y + 4.0D, zone.center.z, 10, zone.radius * 0.5D, 1.2D, zone.radius * 0.5D, 0.03D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (e instanceof Player || (e instanceof TamableAnimal t && t.isTame()) || (e instanceof AbstractHorse h && h.isTamed())
                                    || (owner != null && (e == owner || e.isAlliedTo(owner)))) {
                                SpellCombatReactionEngine.notifyMagicalHeal(e, 2.0F);
                                e.heal(2.0F);
                                if (e instanceof Player p) {
                                    p.getFoodData().eat(1, 0.4F);
                                }
                                if (elapsed % 40 == 0) {
                                    SpellCombatReactionEngine.purifyHexesAndNegativeEffects(e);
                                }
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, e, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
                            }
                        }
                        // Sprout vibrant roses & Vitae Orchids on grass/dirt
                        BlockPos cPos = BlockPos.containing(zone.center);
                        for (int i = 0; i < 2; i++) {
                            BlockPos rp = cPos.offset(srv.random.nextInt(9) - 4, 0, srv.random.nextInt(9) - 4);
                            if (srv.getBlockState(rp).isAir()) {
                                Block flora = srv.random.nextBoolean() ? ModBlocks.VITAE_ORCHID.get() : Blocks.POPPY;
                                if (flora.defaultBlockState().canSurvive(srv, rp)) {
                                    srv.setBlockAndUpdate(rp, flora.defaultBlockState());
                                }
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
        tag.putString("SpellEidolonMateria", EssenceType.VITAE.name());
        tag.putInt("SpellEidolonTintRGB", VITAE_GUARDIAN_HEX_COLOR);
        level.sendParticles(ParticleTypes.HEART, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 16, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Cardiovascular Shock Blink: heartbeat boom stunning adjacent hostiles for 1s (20t) at both departure and arrival!
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F, 0.9F);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F, 0.9F);
        var paralyzed = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get());
        AABB depBox = new AABB(departurePos.x - 3.5D, departurePos.y - 1.5D, departurePos.z - 3.5D,
                departurePos.x + 3.5D, departurePos.y + 2.5D, departurePos.z + 3.5D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox)) {
            if (foe == caster || foe.isAlliedTo(caster)) continue;
            foe.addEffect(new MobEffectInstance(paralyzed, 20, 0));
        }
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(3.5D))) {
            if (foe == caster || foe.isAlliedTo(caster)) continue;
            foe.addEffect(new MobEffectInstance(paralyzed, 20, 0));
        }
        level.sendParticles(ParticleTypes.HEART, departurePos.x, departurePos.y + 0.9D, departurePos.z, 8, 0.5D, 0.4D, 0.5D, 0.04D);
        level.sendParticles(ParticleTypes.HEART, arrivalPos.x, arrivalPos.y + 0.9D, arrivalPos.z, 12, 0.5D, 0.4D, 0.5D, 0.04D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Vitality Synthesis Transmute: cures zombie villagers, rotten flesh -> raw meat, dead bushes -> rose bushes / Vitae Orchids!
        boolean curedAny = false;
        for (ZombieVillager zv : level.getEntitiesOfClass(ZombieVillager.class, new AABB(pos).inflate(2.5D))) {
            if (zv.convertTo(EntityType.VILLAGER, ConversionParams.single(zv, false, false), v -> {}) != null) {
                curedAny = true;
            }
        }
        if (state.is(Blocks.DEAD_BUSH)) {
            if (Blocks.ROSE_BUSH.defaultBlockState().canSurvive(level, pos) && level.getBlockState(pos.above()).isAir()) {
                DoublePlantBlock.placeAt(level, Blocks.ROSE_BUSH.defaultBlockState(), pos, Block.UPDATE_ALL);
            } else {
                level.setBlockAndUpdate(pos, ModBlocks.VITAE_ORCHID.get().defaultBlockState());
            }
            return true;
        }
        if (state.is(Blocks.WITHER_ROSE)) {
            level.setBlockAndUpdate(pos, ModBlocks.VITAE_ORCHID.get().defaultBlockState());
            return true;
        }
        return curedAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.ROTTEN_FLESH)) {
            Item rawMeat = switch (level.random.nextInt(4)) {
                case 0 -> Items.BEEF;
                case 1 -> Items.PORKCHOP;
                case 2 -> Items.MUTTON;
                default -> Items.CHICKEN;
            };
            itemEntity.setItem(new ItemStack(rawMeat, in.getCount()));
            level.sendParticles(ParticleTypes.HEART, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 5, 0.15D, 0.15D, 0.15D, 0.02D);
            return true;
        }
        if (in.is(Items.DEAD_BUSH)) {
            itemEntity.setItem(new ItemStack(Items.ROSE_BUSH, in.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Hyper-Metabolic Arrhythmia Hex: disables health regen, cuts attack damage by 40%, self-damaging recoil when attacking (in interceptWardDamage)
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, true, true));
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.40D * mult, 0.0D, 35L);
        target.hurtServer(level, level.damageSources().magic(), (1.2F + potencyTier * 0.45F) * mult);
        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.85D), target.getZ(), 3, 0.2D, 0.25D, 0.2D, 0.02D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target, Vec3 direction, SpellCoreEffect effect, int potencyTier, SpellAssembly spell) {
        if (effect == SpellCoreEffect.TRANSMUTE && target instanceof ZombieVillager zv) {
            zv.convertTo(EntityType.VILLAGER, ConversionParams.single(zv, false, false), v -> {});
            level.playSound(null, target.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 0.9F, 1.1F);
        }
    }
}
