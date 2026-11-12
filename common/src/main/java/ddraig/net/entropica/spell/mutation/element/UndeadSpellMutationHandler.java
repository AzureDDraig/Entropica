package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
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
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Undead Materia (Base Type #9)</b>
 * (Spellcrafting System Guide Section 6.10).
 */
public final class UndeadSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int CRYPTGUARD_EIDOLON_HEX_COLOR = 0x4D6453;

    @Override
    public EssenceType materiaType() {
        return EssenceType.UNDEAD;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Creeping Tomb-Dust";
            default -> "Necrotic Ossuary " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Necrotic Bone-Shard";
            case BEAM -> "Siphon Life";
            case RADIAL_BURST -> "Corpse Explosion (Bone Nova)";
            case TOUCH_STRIKE -> "Vampiric Touch";
            case AURA -> "Necrotic Shroud";
            case GLYPH -> "Corpse Bloom Mine";
            case CONE -> "Screaming Skull Swarm";
            case SKYFALL -> "Spectral Soul Swarm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Grave-Phasing";
            case TRANSMUTE, BREAK -> "Necrotic Fossilization";
            case AFFLICTION -> "Cadaverous Wither-Blight Hex";
            case RESTORATION -> "Metabolic Transfusion";
            default -> "Necrotic " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public float getFlowBonusAdvantageScore(SpellFlowModifier flow, LivingEntity target) {
        return flow == SpellFlowModifier.SLOW_HEAVY ? 0.4F : 0.0F;
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow == SpellFlowModifier.SLOW_HEAVY) {
            level.sendParticles(ParticleTypes.ASH, projectile.getX(), projectile.getY(), projectile.getZ(), 4, 0.25D, 0.25D, 0.25D, 0.01D);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(1.8D))) {
                if (e != projectile.getOwner() && e.isAlive()) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
                }
            }
        }
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SOUL, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Necrotic Bone-Shard: Wither I for 4s (80t) & bone shrapnel splash
        if (hitEntity instanceof LivingEntity living && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
            for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(2.5D))) {
                if (adj != caster && adj != living && adj.isAlive()
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, spell.hasWardFriendlyFireSafeguard()))) {
                    adj.hurtServer(level, level.damageSources().magic(), 2.0F + spell.getEffectiveTier());
                }
            }
            level.playSound(null, living.blockPosition(), SoundEvents.SKELETON_HURT, SoundSource.PLAYERS, 0.85F, 0.85F);
        }
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Siphon Life: siphons 25% of base combat damage back into caster health AND recharges caster's Orbis Cell!
        int tier = spell.getEffectiveTier();
        float siphonHeal = 0.25F * MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.4F, apparatusPotency);
        long fuelRestore = Math.max(8L, MateriaStageTable.getBaseMfumCastCost(tier) / 4L);
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
            SpellCombatReactionEngine.notifyMagicalHeal(caster, siphonHeal);
            caster.heal(siphonHeal);
            if (caster instanceof Player player) {
                SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.UNDEAD, fuelRestore);
            }
        }
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Vampiric Touch: siphons 30% of base combat damage directly into caster health upon landing the blow!
        boolean landed = false;
        for (LivingEntity hit : hitEntities) {
            if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                hit.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                landed = true;
            }
        }
        if (landed) {
            float vampHeal = 0.30F * MateriaStageTable.getBaseCombatDamageHp(spell.getEffectiveTier()) * Math.max(0.5F, apparatusPotency);
            SpellCombatReactionEngine.notifyMagicalHeal(caster, vampHeal);
            caster.heal(vampHeal);
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Corpse Explosion / Bone Nova: Wither II for 4s (80t) & knockback
        AABB box = new AABB(center.x - radius, center.y - 2.0D, center.z - radius, center.x + radius, center.y + 2.5D, center.z + radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity == caster || !entity.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
            Vec3 away = entity.position().subtract(center).normalize().scale(1.1D);
            entity.setDeltaMovement(away.x, 0.35D, away.z);
            entity.hasImpulse = true;
        }
        level.playSound(null, BlockPos.containing(center), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8F, 1.2F);
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Necrotic Shroud
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.SOUL, caster.getX(), caster.getY(0.8D), caster.getZ(), 18, 0.45D, 0.5D, 0.45D, 0.04D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Corpse Bloom Mine: Wither II skull blast + converts slain mobs directly into harvestable bone meal
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - 2.2D, zone.center.y - 1.0D, zone.center.z - 2.2D,
                            zone.center.x + 2.2D, zone.center.y + 2.0D, zone.center.z + 2.2D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner);
                    if (stepped.isEmpty()) return true;

                    for (LivingEntity v : stepped) {
                        v.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                        if (!v.isAlive()) {
                            Block.popResource(srv, v.blockPosition(), new ItemStack(Items.BONE_MEAL, 3));
                        }
                    }
                    srv.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, zone.center.x, zone.center.y + 0.6D, zone.center.z, 25, 0.8D, 0.5D, 0.8D, 0.06D);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Screaming Skull Swarm (Haunting Banshee Wave): pushes & panics struck entities to turn and flee in terror for 2 seconds (40t)!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.25D, 0.3D, direction.z * 1.25D);
            hit.hasImpulse = true;
            SpellLingeringZoneManager.applyFearFlee(hit, caster.position(), 40);
            if (hit instanceof Mob mob) {
                mob.setAggressive(false);
            }
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.GHAST_SCREAM, SoundSource.PLAYERS, 0.65F, 1.35F);
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Spectral Soul Swarm: 6s (120t) wraiths across 12m area ignoring physical shields, sapping speed by 40% & dealing Undead damage
        double r = Math.max(6.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.SOUL, zone.center.x, zone.center.y + 4.0D, zone.center.z, 18, zone.radius * 0.5D, 1.2D, zone.radius * 0.5D, 0.05D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            if (v instanceof Player p && p.isBlocking()) {
                                ItemStack shield = p.getUseItem();
                                p.stopUsingItem();
                                if (!shield.isEmpty()) {
                                    p.getCooldowns().addCooldown(shield, 80);
                                }
                            }
                            v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
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
        tag.putString("SpellEidolonMateria", EssenceType.UNDEAD.name());
        tag.putInt("SpellEidolonTintRGB", CRYPTGUARD_EIDOLON_HEX_COLOR);
        level.sendParticles(ParticleTypes.SOUL, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 22, 0.5D, 0.6D, 0.5D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Grave-Phasing: skeletal rib cage at departure rooting pursuing enemies!
        AABB box = new AABB(departurePos.x - 3.0D, departurePos.y - 1.5D, departurePos.z - 3.0D,
                departurePos.x + 3.0D, departurePos.y + 2.5D, departurePos.z + 3.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || foe.isAlliedTo(caster)) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
        }
        level.sendParticles(ParticleTypes.SOUL, departurePos.x, departurePos.y + 0.6D, departurePos.z, 18, 0.5D, 0.5D, 0.5D, 0.04D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Necrotic Fossilization: wood -> fossilized stone/bone, foliage -> dead bush, coral -> dead/bone
        if (state.is(BlockTags.LOGS) || state.is(BlockTags.CORAL_BLOCKS)) {
            level.setBlockAndUpdate(pos, Blocks.BONE_BLOCK.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.PLANKS)) {
            level.setBlockAndUpdate(pos, Blocks.TUFF.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)) {
            level.setBlockAndUpdate(pos, Blocks.DEAD_BUSH.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL)) {
            level.setBlockAndUpdate(pos, Blocks.COARSE_DIRT.defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        // Necrotic Fossilization: transmutes meat and organic items directly into solid bone blocks / bones!
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        int count = stack.getCount();

        if (stack.is(ItemTags.LOGS) || stack.is(Items.BEEF) || stack.is(Items.PORKCHOP)
                || stack.is(Items.MUTTON) || stack.is(Items.CHICKEN) || stack.is(Items.RABBIT)
                || stack.is(Items.COOKED_BEEF) || stack.is(Items.COOKED_PORKCHOP) || stack.is(Items.ROTTEN_FLESH)) {
            itemEntity.setItem(new ItemStack(Items.BONE_BLOCK, count));
            level.sendParticles(ParticleTypes.SOUL, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.03D);
            return true;
        }
        if (stack.is(ItemTags.LEAVES) || stack.is(ItemTags.FLOWERS) || stack.is(Items.LEATHER) || stack.is(Items.FEATHER)) {
            itemEntity.setItem(new ItemStack(Items.BONE, count));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Cadaverous Wither-Blight Hex: necrotic damage & real 30% physical Armor Toughness shred (scaled by stack)
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 35, 1, true, false));
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, 0.30D * mult, 35L);
        target.hurtServer(level, level.damageSources().magic(), (1.25F + potencyTier * 0.5F) * mult);
        level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY(0.7D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.03D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target, Vec3 direction, SpellCoreEffect effect, int potencyTier, SpellAssembly spell) {
        // Metabolic Transfusion: consumes caster food saturation first, then hunger points, to rapidly heal mortal wounds!
        if (effect == SpellCoreEffect.RESTORATION && caster instanceof Player player) {
            FoodData food = player.getFoodData();
            float sat = food.getSaturationLevel();
            float bonusHeal = 0.0F;
            if (sat >= 1.0F) {
                float consumedSat = Math.min(6.0F, sat);
                food.setSaturation(sat - consumedSat);
                bonusHeal += consumedSat * 1.5F;
            }
            if (food.getFoodLevel() >= 4) {
                food.setFoodLevel(food.getFoodLevel() - 4);
                bonusHeal += 6.0F + potencyTier * 1.5F;
            }
            if (bonusHeal > 0.0F) {
                SpellCombatReactionEngine.notifyMagicalHeal(target, bonusHeal);
                target.heal(bonusHeal);
            }
        }
    }
}
