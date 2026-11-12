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
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
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
 * Complete per-part Signature Mutation implementation for <b>Umbral Materia (Base Type #8)</b>
 * (Spellcrafting System Guide Section 6.9).
 */
public final class UmbralSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SHADOW_STALKER_HEX_COLOR = 0x211A21;

    @Override
    public EssenceType materiaType() {
        return EssenceType.UMBRAL;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Silent Shadow Chains";
            default -> "Shadow Eclipse " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Shadow Snare Missile";
            case BEAM -> "Abyssal Shadow Lance";
            case RADIAL_BURST -> "Smoke Veil Shockwave";
            case TOUCH_STRIKE -> "Umbral Shroud Cloaking";
            case AURA -> "Shroud of Silence & Stealth";
            case GLYPH -> "Void-Blind Mine";
            case CONE -> "Shadow Dart Fan";
            case SKYFALL -> "Black Rain of Obfuscation";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Shadow-Step";
            case TRANSMUTE, BREAK -> "Nocturnal Darkening";
            case AFFLICTION -> "Sensory Void Hex";
            default -> "Umbral " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Silent Shadow Chains: binds victims together so 25% of damage taken echoes across chained mobs!
        long now = level.getGameTime();
        CompoundTag fromData = EntityHelper.getPersistentData(fromTarget);
        long groupId = fromData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_UNTIL).orElse(0L) > now
                ? fromData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_GROUP).orElse(now)
                : (now ^ caster.getUUID().getLeastSignificantBits());

        fromData.putLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_GROUP, groupId);
        fromData.putLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_UNTIL, now + 160L);

        CompoundTag toData = EntityHelper.getPersistentData(toTarget);
        toData.putLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_GROUP, groupId);
        toData.putLong(SpellLingeringZoneManager.TAG_UMBRAL_CHAIN_UNTIL, now + 160L);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        // Shadow Snare Missile: pierces through targets
        projectile.addBonusPierces(2);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SMOKE, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.0D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Tether target's shadow to the floor for 2 seconds (40t)
        if (hitEntity instanceof LivingEntity living && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
            living.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
        }
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Abyssal Shadow Lance: blinds, silences, & drains attack strength by 30%
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
            EntityHelper.getPersistentData(hit).putLong("SpellSilencedUntil", level.getGameTime() + 60L);
        }
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Umbral Shroud Cloaking: 2s Invisibility & drops active mob aggro
        if (!hitEntities.isEmpty()) {
            caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
            dropMobAggroAround(level, caster.position(), 10.0D);
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Smoke Veil Shockwave: silent dark mist, blinds foes, drops mob aggro, renders caster Invisible for 4s (80t)
        AABB box = new AABB(center.x - radius, center.y - 2.0D, center.z - radius, center.x + radius, center.y + 2.5D, center.z + radius);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == caster || !e.isAlive()) continue;
            if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())) {
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            }
        }
        dropMobAggroAround(level, center, Math.max(8.0D, radius));
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, false, false));
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 0.8D, center.z, 35, radius * 0.45D, 0.5D, radius * 0.45D, 0.04D);
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Shroud of Silence & Stealth
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL, level.getGameTime() + dur);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Void-Blind Mine: wipes aggro table of all mobs in an 8-block radius when tripped!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB trig = new AABB(zone.center.x - 2.0D, zone.center.y - 1.0D, zone.center.z - 2.0D,
                            zone.center.x + 2.0D, zone.center.y + 2.0D, zone.center.z + 2.0D);
                    if (srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive() && e != owner).isEmpty()) {
                        return true;
                    }
                    dropMobAggroAround(srv, zone.center, 8.0D);
                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, new AABB(BlockPos.containing(zone.center)).inflate(8.0D))) {
                        if (v != owner && v instanceof Monster) {
                            v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                        }
                    }
                    srv.sendParticles(ParticleTypes.LARGE_SMOKE, zone.center.x, zone.center.y + 0.8D, zone.center.z, 30, 1.5D, 0.6D, 1.5D, 0.05D);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Shadow Dart Fan: reduces movement speed by ~50% (Slowness III)
        for (LivingEntity hit : hitTargets) {
            if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            }
        }
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Black Rain of Obfuscation: 6s (120t) dark droplets blinding foes & dropping mob aggro across a 16-block radius
        double r = Math.max(16.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.SMOKE, zone.center.x, zone.center.y + 5.0D, zone.center.z, 26, zone.radius * 0.5D, 1.0D, zone.radius * 0.5D, 0.04D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        dropMobAggroAround(srv, zone.center, zone.radius);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
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
        tag.putString("SpellEidolonMateria", EssenceType.UMBRAL.name());
        tag.putInt("SpellEidolonTintRGB", SHADOW_STALKER_HEX_COLOR);
        dropMobAggroAround(level, spawnPos, Math.max(6.0D, radius));
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Shadow-Step: silent stealth blink granting brief cloaking & dropping aggro without sound or arrival particles!
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
        dropMobAggroAround(level, departurePos, 8.0D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Nocturnal Darkening: Stone/Cobble -> Blackstone, Logs -> Dark Oak Log (or Charcoal on second pass), Glass -> Tinted Glass
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)) {
            level.setBlockAndUpdate(pos, Blocks.BLACKSTONE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.GLASS)) {
            level.setBlockAndUpdate(pos, Blocks.TINTED_GLASS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.TORCH)) {
            level.setBlockAndUpdate(pos, Blocks.SOUL_TORCH.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.LANTERN)) {
            level.setBlockAndUpdate(pos, Blocks.SOUL_LANTERN.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.LOGS) && !state.is(Blocks.DARK_OAK_LOG)) {
            level.setBlockAndUpdate(pos, Blocks.DARK_OAK_LOG.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DARK_OAK_LOG)) {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new ItemStack(Items.CHARCOAL, 2));
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        int count = stack.getCount();

        if (stack.is(Items.TORCH)) {
            itemEntity.setItem(new ItemStack(Items.SOUL_TORCH, count));
            return true;
        }
        if (stack.is(Items.LANTERN)) {
            itemEntity.setItem(new ItemStack(Items.SOUL_LANTERN, count));
            return true;
        }
        if (stack.is(Items.CAMPFIRE)) {
            itemEntity.setItem(new ItemStack(Items.SOUL_CAMPFIRE, count));
            return true;
        }
        if (stack.is(Items.GLASS)) {
            itemEntity.setItem(new ItemStack(Items.TINTED_GLASS, count));
            return true;
        }
        if (stack.is(Items.STONE) || stack.is(Items.COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(Items.BLACKSTONE, count));
            return true;
        }
        if (stack.is(Items.GLOW_BERRIES)) {
            itemEntity.setItem(new ItemStack(Items.SWEET_BERRIES, count));
            return true;
        }
        if (stack.is(ItemTags.LOGS)) {
            itemEntity.setItem(new ItemStack(Items.CHARCOAL, count));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Sensory Void Hex: Blindness, Slowness II, silences alerts; empowers surrounding hostiles (+25% dmg & Speed I) if target is a player!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        int slowAmp = Math.min(3, 1 + (SpellCombatReactionEngine.getHexStackLevel(target) - 1));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, false));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, slowAmp, true, false));
        EntityHelper.getPersistentData(target).putLong("SpellSilencedUntil", level.getGameTime() + 35L);
        target.hurtServer(level, level.damageSources().magic(), (1.0F + potencyTier * 0.45F) * mult);

        if (target instanceof Player) {
            for (Monster m : level.getEntitiesOfClass(Monster.class, target.getBoundingBox().inflate(16.0D))) {
                m.addEffect(new MobEffectInstance(MobEffects.SPEED, 35, 0, true, false));
            }
        }
        return true;
    }

    private static void dropMobAggroAround(ServerLevel level, Vec3 center, double radius) {
        AABB box = new AABB(center.x - radius, center.y - 3.0D, center.z - radius, center.x + radius, center.y + 4.0D, center.z + radius);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box)) {
            mob.setTarget(null);
        }
    }
}
