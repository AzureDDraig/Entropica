package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
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
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Amber Materia (Vital Fusion #24)</b>
 * (Spellcrafting System Guide Section 6.25).
 */
public final class AmberSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int FOSSIL_WARDEN_HEX_COLOR = 0xBB4F58;

    @Override
    public EssenceType materiaType() {
        return EssenceType.AMBER;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Resinite Viscous Linger Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Amber Stasis Bubble Orb";
            case BEAM -> "Fossilizing Stasis Torrent";
            case RADIAL_BURST -> "Amber Crystallization Nova";
            case TOUCH_STRIKE -> "Fossilizing Stasis Strike";
            case AURA -> "Petrified Amber Bastion Mantle";
            case GLYPH -> "Amber Preservation Vault Glyph";
            case CONE -> "Molten Resin Deluge";
            case SKYFALL -> "Prehistoric Resinite Monsoon";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Amber Chrysalis Blink";
            case TRANSMUTE, BREAK -> "Petrification & Fossilization Transmute";
            case AFFLICTION -> "Petrifying Amber Stasis Hex";
            default -> "Resinite " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---
    // Note: +50% duration & escalating 10%/s up to 50% slow on all Amber ground zones/traps is also enforced directly
    // inside SpellLingeringZoneManager.registerZone!

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.DRIPPING_HONEY, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.DRIPPING_HONEY, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        spawnStickyAmberPool(level, caster, projectile.position(), spell, slot);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        spawnStickyAmberPool(level, caster, projectile.position(), spell, slot);
    }

    private static void spawnStickyAmberPool(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell, SubSpellSlot slot) {
        // Amber Stasis Bubble Orb: 3m sticky amber pool rooting caught enemies for 3s (60t) & permanently preserving dropped items from despawning!
        AABB box = new AABB(center.x - 3.0D, center.y - 1.5D, center.z - 3.0D, center.x + 3.0D, center.y + 2.0D, center.z + 3.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            applyAmberRoot(foe, 60);
        }
        preserveDroppedItems(level, box, false);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, 3.0D, 80, level.getGameTime(), spell, slot, EssenceType.AMBER, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.DRIPPING_HONEY, zone.center.x, zone.center.y + 0.2D, zone.center.z, 8, 1.4D, 0.1D, 1.4D, 0.01D);
                        preserveDroppedItems(srv, box, false);
                    }
                    return true;
                }
        ));
        level.playSound(null, BlockPos.containing(center), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.PLAYERS, 0.9F, 0.9F);
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Fossilizing Stasis Torrent: stacks Slowness -> 2.5s (50t) solid Amber Stasis freeze, fire/lava item protection, Silk Touch fragile blocks!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("AmberBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("AmberBeamStacks").orElse(0) + 1 : 1;
            data.putLong("AmberBeamLastTick", now);
            data.putInt("AmberBeamStacks", stacks);

            if (stacks >= 3) {
                data.putInt("AmberBeamStacks", 0);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
                applyAmberRoot(hit, 50);
                level.playSound(null, hit.blockPosition(), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.PLAYERS, 1.0F, 0.75F);
            } else {
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, Math.min(3, stacks)));
            }
        }

        AABB corridor = caster.getBoundingBox().expandTowards(direction.scale(maxReach)).inflate(1.5D);
        preserveDroppedItems(level, corridor, true);

        int steps = Math.min(16, (int) Math.ceil(maxReach));
        for (int s = 1; s <= steps; s++) {
            BlockPos bp = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            BlockState bs = level.getBlockState(bp);
            silkHarvestFragileBlock(level, bp, bs);
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Amber Crystallization Nova: 6m 4-block knockback, roots 3s (60t), resin veneer (+20% ally speed / -40% hostile slow), converts plants (minus grass_block) into amber!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.05D);
            foe.setDeltaMovement(away.x, 0.30D, away.z);
            foe.hasImpulse = true;
            applyAmberRoot(foe, 60);
        }

        // Convert surface plants (strictly excluding grass_block & dirt turf variants!) into Amber Nectar Blossoms!
        BlockPos cPos = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-5, -1, -5), cPos.offset(5, 2, 5))) {
            BlockState bs = level.getBlockState(p);
            if (isConvertibleSurfacePlant(bs)) {
                level.setBlockAndUpdate(p, ModBlocks.AMBER_NECTAR_BLOSSOM.get().defaultBlockState());
            }
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (e == owner || (owner != null && e.isAlliedTo(owner)) || e instanceof Player) {
                                e.addEffect(new MobEffectInstance(MobEffects.SPEED, 30, 0, true, false));
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1));
                            }
                        }
                    }
                    return true;
                }
        ));
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEY_BLOCK.defaultBlockState()),
                center.x, center.y + 0.8D, center.z, 36, 2.2D, 0.5D, 2.2D, 0.08D);
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Fossilizing Stasis Strike: 60% Slowness & -40% attack speed for 4s (80t); lethal blow crystallizes corpse into fragile shattering amber statue!
        long until = level.getGameTime() + 80L;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            hit.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 80, 1));
            SpellCombatReactionEngine.applyTimedAttackDebuff(hit, level, 0.0D, 0.40D, 80L);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_AMBER_FRAGILE_STATUE_UNTIL, until);
        }
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Petrified Amber Bastion Mantle: +8 Armor, +3 Toughness, 50% KB resistance, armor durability protection, melee attacker slow splash!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_AMBER_MANTLE_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 8.0D, 3.0D, 0.50D, 0.0D, dur);
        level.sendParticles(ParticleTypes.DRIPPING_HONEY, caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.45D, 0.5D, 0.45D, 0.03D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Amber Preservation Vault Glyph: encapsulates touching dropped items against despawning, fire, and lava!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.5D, 600, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    AABB vaultBox = new AABB(zone.center.x - 3.5D, zone.center.y - 1.5D, zone.center.z - 3.5D,
                            zone.center.x + 3.5D, zone.center.y + 2.5D, zone.center.z + 3.5D);
                    if (elapsed % 10 == 0) {
                        preserveDroppedItems(srv, vaultBox, true);
                        srv.sendParticles(ParticleTypes.DRIPPING_HONEY, zone.center.x, zone.center.y + 0.3D, zone.center.z, 5, 1.5D, 0.2D, 1.5D, 0.01D);
                    }
                    if (elapsed >= 10 && elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, vaultBox, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            if (foe instanceof Player) continue;
                            applyAmberRoot(foe, 40);
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Molten Resin Deluge: 3m knockback, 60% Slowness & -30% armor toughness for 4s (80t), sticky ground slick preventing jumping, Silk Touch fragile blocks!
        long now = level.getGameTime();
        long until = now + 80L;
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 0.85D, 0.22D, direction.z * 0.85D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.30D, 80L);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, until);
        }
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(stepPos.offset(-2, -1, -2), stepPos.offset(2, 1, 2))) {
                silkHarvestFragileBlock(level, p, level.getBlockState(p));
            }
        }
        // Register actual 4s (80t) sticky molten resin ground slick preventing jumping & applying 60% Slowness!
        Vec3 slickCenter = caster.position().add(direction.scale(Math.max(2.5D, coneRange * 0.5D)));
        double slickR = Math.max(3.5D, coneRange * 0.55D);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, slickCenter, slickR, 80, now, spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.DRIPPING_HONEY, zone.center.x, zone.center.y + 0.2D, zone.center.z, 10, zone.radius * 0.45D, 0.1D, zone.radius * 0.45D, 0.01D);
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    long lockUntil = srv.getGameTime() + 20L;
                    AABB slickBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 2.0D, zone.center.z + zone.radius);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, slickBox, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 2, false, false));
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, lockUntil);
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Prehistoric Resinite Monsoon: 10s (200t) sap downpour blinding enemies & encasing dying non-boss mobs inside amber monuments!
        double r = Math.max(7.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.DRIPPING_HONEY, zone.center.x, zone.center.y + 4.5D, zone.center.z, 22, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.04D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long markUntil = srv.getGameTime() + 45L;
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                            v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                            EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_AMBER_FOSSILIZE_UNTIL, markUntil);
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
        tag.putString("SpellEidolonMateria", EssenceType.AMBER.name());
        tag.putInt("SpellEidolonTintRGB", FOSSIL_WARDEN_HEX_COLOR);
        level.sendParticles(ParticleTypes.DRIPPING_HONEY, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Amber Chrysalis Blink: 10-HP damage-absorbing amber decoy shell at departure popping into slow resin + caster +4 Armor for 4s (80t)!
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.0D, 0.0D, 80L);
        BlockPos dep = BlockPos.containing(departurePos);
        boolean placedFeet = false;
        boolean placedHead = false;
        if (level.getBlockState(dep).isAir()) {
            level.setBlockAndUpdate(dep, Blocks.HONEY_BLOCK.defaultBlockState());
            placedFeet = true;
        }
        if (level.getBlockState(dep.above()).isAir()) {
            level.setBlockAndUpdate(dep.above(), Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
            placedHead = true;
        }
        final boolean fFeet = placedFeet;
        final boolean fHead = placedHead;
        final BlockPos immDep = dep.immutable();
        final float[] absorbedDamage = new float[]{0.0F};

        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 4.0D, 80, level.getGameTime(), dummy, dummy.subSpells().get(0), EssenceType.AMBER, 1.0F,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB shellBox = new AABB(departurePos.x - 1.5D, departurePos.y - 0.5D, departurePos.z - 1.5D,
                            departurePos.x + 1.5D, departurePos.y + 2.5D, departurePos.z + 1.5D);
                    AABB lureBox = new AABB(departurePos.x - 5.0D, departurePos.y - 2.0D, departurePos.z - 5.0D,
                            departurePos.x + 5.0D, departurePos.y + 3.0D, departurePos.z + 5.0D);

                    if (elapsed < zone.maxDurationTicks) {
                        boolean broken = (fFeet && !srv.getBlockState(immDep).is(Blocks.HONEY_BLOCK))
                                || (fHead && !srv.getBlockState(immDep.above()).is(Blocks.ORANGE_STAINED_GLASS));
                        // Absorb incoming enemy projectiles hitting the chrysalis shell!
                        for (Projectile proj : srv.getEntitiesOfClass(Projectile.class, shellBox)) {
                            if (proj.getOwner() != owner) {
                                absorbedDamage[0] += 4.0F;
                                proj.discard();
                            }
                        }
                        // Taunt nearby monsters to strike the decoy shell, absorbing up to 10 HP of damage!
                        if (!broken && elapsed >= 6) {
                            for (Mob mob : srv.getEntitiesOfClass(Mob.class, lureBox)) {
                                if (mob instanceof Monster && mob.isAlive()) {
                                    if (elapsed % 10 == 0) {
                                        mob.getNavigation().moveTo(departurePos.x, departurePos.y, departurePos.z, 1.15D);
                                    }
                                    if (elapsed % 10 == 0 && mob.position().distanceToSqr(departurePos) <= 3.5D) {
                                        absorbedDamage[0] += 3.5F;
                                        srv.playSound(null, immDep, SoundEvents.HONEY_BLOCK_HIT, SoundSource.PLAYERS, 0.8F, 1.0F);
                                    }
                                }
                            }
                        }
                        if (!broken && absorbedDamage[0] < 10.0F) {
                            return true;
                        }
                    }

                    if (fFeet && srv.getBlockState(immDep).is(Blocks.HONEY_BLOCK)) srv.removeBlock(immDep, false);
                    if (fHead && srv.getBlockState(immDep.above()).is(Blocks.ORANGE_STAINED_GLASS)) srv.removeBlock(immDep.above(), false);
                    AABB splash = new AABB(departurePos.x - 4.0D, departurePos.y - 1.5D, departurePos.z - 4.0D,
                            departurePos.x + 4.0D, departurePos.y + 2.5D, departurePos.z + 4.0D);
                    long noJump = srv.getGameTime() + 60L;
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, splash)) {
                        if (foe == owner || !foe.isAlive() || (owner != null && foe.isAlliedTo(owner))) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, noJump);
                    }
                    srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEY_BLOCK.defaultBlockState()),
                            departurePos.x, departurePos.y + 1.0D, departurePos.z, 26, 1.0D, 0.6D, 1.0D, 0.06D);
                    srv.playSound(null, immDep, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.90F, 1.15F);
                    return false;
                }
        ));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Petrification & Fossilization Transmute: logs -> Amber Log, flora (minus grass_block) -> Amber Nectar Blossom, Silk Touch fragile blocks!
        if (state.is(BlockTags.LOGS) && !state.is(ModBlocks.AMBER_LOG.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AMBER_LOG.get().withPropertiesOf(state));
            return true;
        }
        if (state.is(BlockTags.PLANKS) && !state.is(ModBlocks.AMBER_PLANKS.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AMBER_PLANKS.get().defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.LEAVES) && !state.is(ModBlocks.AMBER_LEAVES.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AMBER_LEAVES.get().defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.SAPLINGS) && !state.is(ModBlocks.AMBER_SAPLING.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AMBER_SAPLING.get().defaultBlockState());
            return true;
        }
        if (isConvertibleSurfacePlant(state)) {
            level.setBlockAndUpdate(pos, ModBlocks.AMBER_NECTAR_BLOSSOM.get().defaultBlockState());
            return true;
        }
        return silkHarvestFragileBlock(level, pos, state);
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(ItemTags.LOGS)) {
            itemEntity.setItem(new ItemStack(ModBlocks.AMBER_LOG.get().asItem(), in.getCount()));
            return true;
        }
        if (in.is(ItemTags.PLANKS)) {
            itemEntity.setItem(new ItemStack(ModBlocks.AMBER_PLANKS.get().asItem(), in.getCount()));
            return true;
        }
        if (in.is(ItemTags.SAPLINGS)) {
            itemEntity.setItem(new ItemStack(ModBlocks.AMBER_SAPLING.get().asItem(), in.getCount()));
            return true;
        }
        if (in.is(ItemTags.SMALL_FLOWERS)) {
            itemEntity.setItem(new ItemStack(ModBlocks.AMBER_NECTAR_BLOSSOM.get().asItem(), in.getCount()));
            return true;
        }
        if (in.is(Items.HONEYCOMB) || in.is(Items.SLIME_BALL)) {
            itemEntity.setItem(new ItemStack(ModItems.AMBER_CHUNK.get(), in.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Petrifying Amber Stasis Hex: 70% Slowness (Slowness IV), blocks sprinting & jumping, shreds 40% toughness, suppresses all positive potion buffs!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 3, true, true));
        target.setSprinting(false);
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 35L);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, 0.40D * mult, 35L);

        // Suppress all positive potion buffs!
        List<Holder<MobEffect>> beneficial = new ArrayList<>();
        for (MobEffectInstance eff : target.getActiveEffects()) {
            if (eff.getEffect().value().isBeneficial()) {
                beneficial.add(eff.getEffect());
            }
        }
        for (Holder<MobEffect> h : beneficial) {
            target.removeEffect(h);
        }

        target.hurtServer(level, level.damageSources().magic(), (1.3F + potencyTier * 0.45F) * mult);
        level.sendParticles(ParticleTypes.DRIPPING_HONEY, target.getX(), target.getY(0.8D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.01D);
        return true;
    }

    private static void applyAmberRoot(LivingEntity target, int ticks) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6));
        target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), ticks, 0));
        target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
        target.hasImpulse = true;
    }

    private static void preserveDroppedItems(ServerLevel level, AABB box, boolean fireImmune) {
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (!item.isAlive()) continue;
            item.setUnlimitedLifetime();
            if (fireImmune) {
                item.clearFire();
                item.setInvulnerable(true);
                item.setGlowingTag(true);
            }
        }
    }

    private static boolean isConvertibleSurfacePlant(BlockState bs) {
        if (bs.isAir() || bs.is(Blocks.GRASS_BLOCK) || bs.is(BlockTags.DIRT) || bs.is(ModBlocks.AMBER_NECTAR_BLOSSOM.get())) {
            return false;
        }
        return bs.is(BlockTags.SMALL_FLOWERS) || bs.is(Blocks.SHORT_GRASS) || bs.is(Blocks.FERN) || bs.is(Blocks.DEAD_BUSH)
                || (bs.getBlock() instanceof BushBlock && !bs.is(BlockTags.CROPS));
    }

    private static boolean silkHarvestFragileBlock(ServerLevel level, BlockPos pos, BlockState bs) {
        if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, bs)) return false;
        Item blockItem = bs.getBlock().asItem();
        if (blockItem == Items.AIR) return false;
        if (bs.is(BlockTags.ICE) || bs.is(BlockTags.IMPERMEABLE) || bs.is(Blocks.GLOWSTONE) || bs.is(Blocks.SEA_LANTERN) || bs.is(Blocks.AMETHYST_CLUSTER)) {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new ItemStack(blockItem, 1));
            return true;
        }
        return false;
    }
}
