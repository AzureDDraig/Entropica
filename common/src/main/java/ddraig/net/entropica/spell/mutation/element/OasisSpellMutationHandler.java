package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Oasis Materia (Tier 1 Fusion #20)</b>
 * (Spellcrafting System Guide Section 6.21).
 */
public final class OasisSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int OASIS_GUARDIAN_HEX_COLOR = 0x5E9B62;

    @Override
    public EssenceType materiaType() {
        return EssenceType.OASIS;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case DOUBLE_BIFURCATE, TRIPLE_TRIFURCATE, DELAYED_BLOOM -> "Succulent Bloom Split";
            default -> "Miracle Oasis " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Miracle Desert Bloom Seed";
            case BEAM -> "Subterranean Aquifer Stream";
            case RADIAL_BURST -> "Miracle Oasis Bloom Nova";
            case TOUCH_STRIKE -> "Desert Blessing Touch";
            case AURA -> "Succulent Thorn Carapace";
            case GLYPH -> "Aquifer Geyser Mine";
            case CONE -> "Desert Greening Mist";
            case SKYFALL -> "Miracle Desert Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Mirage-Spring Blink";
            case TRANSMUTE, BREAK -> "Desert Greening Transmute";
            case AFFLICTION -> "Dehydrating Sunstroke Hex";
            case RESTORATION -> "Miracle Spring Mending";
            default -> "Oasis " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onProjectileImpactOrExpireFromFlow(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Vec3 splitOrigin, SpellAssembly spell, SubSpellSlot slot) {
        // Succulent Bloom Split: splits into 3 smaller succulent seed blossoms + scatters healing pollen!
        projectile.spawnSplitChild(level, splitOrigin, -25.0F, false, 0.58F, 1.25F);
        projectile.spawnSplitChild(level, splitOrigin, 0.0F, false, 0.58F, 1.25F);
        projectile.spawnSplitChild(level, splitOrigin, 25.0F, false, 0.58F, 1.25F);
        AABB pollen = new AABB(splitOrigin.x - 3.0D, splitOrigin.y - 1.5D, splitOrigin.z - 3.0D,
                splitOrigin.x + 3.0D, splitOrigin.y + 2.0D, splitOrigin.z + 3.0D);
        for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, pollen)) {
            if (ally.isAlive() && (ally == caster || (caster != null && ally.isAlliedTo(caster)))) {
                SpellCombatReactionEngine.notifyMagicalHeal(ally, 1.5F);
                ally.heal(1.5F);
            }
        }
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, splitOrigin.x, splitOrigin.y, splitOrigin.z, 12, 0.35D, 0.3D, 0.35D, 0.05D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.01D);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Miracle Desert Bloom Seed: siphons 20% lifesteal healing to caster + greens struck sand into grass or hydrated farmland!
        if (caster != null && caster.isAlive() && hitEntity instanceof LivingEntity living && living != caster) {
            SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, spell.getEffectiveTier(), 0.20F, 1.0F);
        }
        bloomOasisPatch(level, BlockPos.containing(projectile.position()), 2);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        bloomOasisPatch(level, blockHit.getBlockPos(), 2);
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Subterranean Aquifer Stream: 15% lifesteal heal, hydrates farmland, accelerates crops/cactus/sugarcane!
        int tier = spell.getEffectiveTier();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, tier, 0.15F, apparatusPotency);
        }
        int steps = Math.min(16, (int) Math.ceil(maxReach));
        for (int s = 1; s <= steps; s++) {
            BlockPos bp = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            hydrateAndGrowFlora(level, bp, 1);
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Miracle Oasis Bloom Nova: 6m knockback 4 blocks, heals caster & allies, extinguishes fire, hydrates farmland, blooms sand!
        double r = Math.max(6.0D, radius);
        int tier = spell.getEffectiveTier();
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (!e.isAlive()) continue;
            e.clearFire();
            if (e == caster || e.isAlliedTo(caster)) {
                float heal = 4.0F + tier;
                SpellCombatReactionEngine.notifyMagicalHeal(e, heal);
                e.heal(heal);
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())) {
                Vec3 away = e.position().subtract(center).normalize().scale(1.05D);
                e.setDeltaMovement(away.x, 0.30D, away.z);
                e.hasImpulse = true;
            }
        }
        BlockPos cPos = BlockPos.containing(center);
        hydrateAndGrowFlora(level, cPos, 4);
        bloomOasisPatch(level, cPos, 4);
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Desert Blessing Touch: pacifies hostiles for 2.5s (50t), OR heals & cleanses allies!
        int tier = spell.getEffectiveTier();
        for (LivingEntity hit : hitEntities) {
            if (hit == caster || hit.isAlliedTo(caster)) {
                float heal = 5.0F + tier * 1.2F;
                SpellCombatReactionEngine.notifyMagicalHeal(hit, heal);
                hit.heal(heal);
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                SpellLingeringZoneManager.applyPacified(hit, 50);
                hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 2));
            }
        }
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Succulent Thorn Carapace: -20% physical damage taken + Oasis thorn retaliation!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_OASIS_CARAPACE_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, caster.getX(), caster.getY(0.8D), caster.getZ(), 20, 0.45D, 0.5D, 0.45D, 0.05D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Aquifer Geyser Mine: blasts victim 8 blocks into the air + hydrates nearby farmland!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                            zone.center.x + 2.5D, zone.center.y + 2.0D, zone.center.z + 2.5D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner);
                    if (stepped.isEmpty()) return true;

                    for (LivingEntity v : stepped) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        v.setDeltaMovement(v.getDeltaMovement().x * 0.3D, 1.18D, v.getDeltaMovement().z * 0.3D);
                        v.hasImpulse = true;
                        SpellLingeringZoneManager.applySoaked(v, 140);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    hydrateAndGrowFlora(srv, BlockPos.containing(zone.center), 4);
                    srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 1.2D, zone.center.z, 32, 0.5D, 1.5D, 0.5D, 0.1D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 0.9F);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Desert Greening Mist: pacifies caught mobs for 4s (80t), hydrates dry soil, accelerates crops/sugarcane/cactus!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellLingeringZoneManager.applyPacified(hit, 80);
            hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
        }
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            hydrateAndGrowFlora(level, BlockPos.containing(caster.position().add(direction.scale(s))), 2);
        }
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Miracle Desert Downpour: 16m restorative rainstorm for 12s (240t) harming hostiles, healing players/pets, hydrating & greening sand!
        double r = Math.max(8.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 240, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.RAIN, zone.center.x, zone.center.y + 4.5D, zone.center.z, 25, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.1D);
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, zone.center.x, zone.center.y + 4.0D, zone.center.z, 10, zone.radius * 0.5D, 1.0D, zone.radius * 0.5D, 0.03D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        BlockPos cPos = BlockPos.containing(zone.center);
                        int br = Math.min(8, (int) Math.ceil(zone.radius));
                        hydrateAndGrowFlora(srv, cPos, Math.min(5, br));
                        bloomOasisPatch(srv, cPos, Math.min(4, br));

                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (e instanceof Player || (e instanceof TamableAnimal t && t.isTame()) || (e instanceof AbstractHorse h && h.isTamed())
                                    || (owner != null && (e == owner || e.isAlliedTo(owner)))) {
                                SpellCombatReactionEngine.notifyMagicalHeal(e, 2.0F);
                                e.heal(2.0F);
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, e, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
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
        tag.putString("SpellEidolonMateria", EssenceType.OASIS.name());
        tag.putInt("SpellEidolonTintRGB", OASIS_GUARDIAN_HEX_COLOR);
        level.sendParticles(ParticleTypes.SPLASH, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 20, 0.45D, 0.55D, 0.45D, 0.06D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Mirage-Spring Blink: splash of soothing springwater on arrival + 3s (60t) blooming flower decoy at departure!
        caster.clearFire();
        BlockPos dep = BlockPos.containing(departurePos).immutable();
        boolean placedFlower = false;
        if (level.getBlockState(dep).isAir() && Blocks.POPPY.defaultBlockState().canSurvive(level, dep)) {
            level.setBlockAndUpdate(dep, Blocks.POPPY.defaultBlockState());
            placedFlower = true;
        }
        final boolean fPlaced = placedFlower;
        AABB lureBox = new AABB(departurePos.x - 4.5D, departurePos.y - 2.0D, departurePos.z - 4.5D,
                departurePos.x + 4.5D, departurePos.y + 2.5D, departurePos.z + 4.5D);
        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 4.5D, 60, level.getGameTime(), dummy, dummy.subSpells().get(0), EssenceType.OASIS, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (fPlaced && srv.getBlockState(dep).is(Blocks.POPPY)) {
                            srv.removeBlock(dep, false);
                        }
                        return false;
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        for (Mob mob : srv.getEntitiesOfClass(Mob.class, lureBox)) {
                            if (mob instanceof Monster && mob.isAlive() && (owner == null || !mob.isAlliedTo(owner))) {
                                mob.getNavigation().moveTo(departurePos.x, departurePos.y, departurePos.z, 1.1D);
                            }
                        }
                    }
                    return true;
                }
        ));
        level.sendParticles(ParticleTypes.SPLASH, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 24, 0.45D, 0.5D, 0.45D, 0.08D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Desert Greening Transmute: sand/red_sand -> grass_block, dead_bush -> flowers, coarse_dirt -> podzol!
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DIRT)) {
            level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
            return true;
        }
        if (state.is(Blocks.DEAD_BUSH)) {
            level.setBlockAndUpdate(pos, (level.random.nextBoolean() ? Blocks.POPPY : Blocks.DANDELION).defaultBlockState());
            return true;
        }
        if (state.is(Blocks.COARSE_DIRT)) {
            level.setBlockAndUpdate(pos, Blocks.PODZOL.defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.SAND) || in.is(Items.RED_SAND)) {
            itemEntity.setItem(new ItemStack(Items.GRASS_BLOCK, in.getCount()));
            return true;
        }
        if (in.is(Items.DEAD_BUSH)) {
            itemEntity.setItem(new ItemStack(Items.POPPY, in.getCount()));
            return true;
        }
        if (in.is(Items.COARSE_DIRT)) {
            itemEntity.setItem(new ItemStack(Items.PODZOL, in.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Dehydrating Sunstroke Hex: drains hunger/saturation, cuts attack speed by 35%, deals Oasis damage when sprinting/jumping!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 1, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 0, true, true));
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, 0.35D * mult, 35L);
        if (target instanceof Player player) {
            player.causeFoodExhaustion(1.5F * mult);
        }
        boolean exerted = target.isSprinting() || (!target.onGround() && target.getDeltaMovement().y > 0.1D);
        float dmg = ((exerted ? 2.5F : 1.0F) + potencyTier * 0.45F) * mult;
        target.hurtServer(level, level.damageSources().magic(), dmg);
        level.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY(0.8D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target, Vec3 direction, SpellCoreEffect effect, int potencyTier, SpellAssembly spell) {
        if (effect == SpellCoreEffect.RESTORATION) {
            // Miracle Spring Mending: cleanses negative effects, heals HP, refills hunger & saturation!
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(target);
            float bonus = 3.0F + potencyTier;
            SpellCombatReactionEngine.notifyMagicalHeal(target, bonus);
            target.heal(bonus);
            if (target instanceof Player player) {
                player.getFoodData().eat(4 + potencyTier, 0.8F);
            }
        }
    }

    private static void bloomOasisPatch(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
            BlockState bs = level.getBlockState(p);
            if ((bs.is(Blocks.SAND) || bs.is(Blocks.RED_SAND)) && level.random.nextFloat() < 0.40F) {
                BlockPos up = p.above();
                if (level.getBlockState(up).isAir() && level.random.nextFloat() < 0.35F) {
                    level.setBlockAndUpdate(p, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
                } else {
                    level.setBlockAndUpdate(p, Blocks.GRASS_BLOCK.defaultBlockState());
                    if (level.getBlockState(up).isAir() && level.random.nextFloat() < 0.45F) {
                        Block flower = level.random.nextBoolean() ? Blocks.POPPY : Blocks.DANDELION;
                        level.setBlockAndUpdate(up, flower.defaultBlockState());
                    }
                }
            }
        }
    }

    private static void hydrateAndGrowFlora(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                level.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
            } else if (bs.getBlock() instanceof BonemealableBlock grow && grow.isValidBonemealTarget(level, p, bs) && level.random.nextFloat() < 0.35F) {
                grow.performBonemeal(level, level.random, p.immutable(), bs);
            } else if ((bs.getBlock() instanceof CactusBlock || bs.getBlock() instanceof SugarCaneBlock) && level.random.nextFloat() < 0.35F) {
                BlockPos up = p.above();
                if (level.getBlockState(up).isAir() && !level.getBlockState(p.below(2)).is(bs.getBlock())) {
                    level.setBlockAndUpdate(up, bs.getBlock().defaultBlockState());
                }
            }
        }
    }
}
