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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Frozen Materia (Base Type #5)</b>
 * (Spellcrafting System Guide Section 6.6).
 */
public final class FrozenSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int GLACIAL_EIDOLON_HEX_COLOR = 0x98DCF2;

    @Override
    public EssenceType materiaType() {
        return EssenceType.FROZEN;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Permafrost Creep";
            default -> "Cryo-Frost " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Glacial Ice-Shard";
            case BEAM -> "Absolute-Zero Cryo-Ray";
            case RADIAL_BURST -> "Frost Nova Burst";
            case TOUCH_STRIKE -> "Flash-Freeze Stasis";
            case AURA -> "Glacial Spikes Retaliation";
            case GLYPH -> "Rime Pitfall";
            case CONE -> "Hoarfrost Flash-Freeze";
            case SKYFALL -> "Colossal Glacial Monolith";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Frostwalker Stasis";
            case TRANSMUTE, BREAK -> "Silk Touch & Permafrost";
            case AFFLICTION -> "Permafrost Stasis Hex";
            default -> "Cryo-Frost " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public float getFlowBonusAdvantageScore(SpellFlowModifier flow, LivingEntity target) {
        if (flow == SpellFlowModifier.SLOW_HEAVY && SpellLingeringZoneManager.isEntityWetOrSoaked(target)) {
            return 0.4F;
        }
        return 0.0F;
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow == SpellFlowModifier.SLOW_HEAVY) {
            BlockPos below = projectile.blockPosition().below();
            if (level.getBlockState(below).is(Blocks.WATER)) {
                level.setBlockAndUpdate(below, Blocks.BLUE_ICE.defaultBlockState());
            }
        }
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(),
                2, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        if (hitEntity instanceof LivingEntity living && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
            living.setTicksFrozen(living.getTicksRequiredToFreeze() + 60);
            level.playSound(null, living.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 1.4F);
        }
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        BlockPos hitPos = blockHit.getBlockPos();
        for (BlockPos p : BlockPos.betweenClosed(hitPos.offset(-1, -1, -1), hitPos.offset(1, 1, 1))) {
            if (level.getBlockState(p).is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
            }
        }
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Absolute-Zero Cryo-Ray: progressive freeze-stasis (full 3s ice encasement on consecutive hits) & walkable ice bridges over water
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 80);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 4));
            CompoundTag data = EntityHelper.getPersistentData(hit);
            int stacks = (now <= data.getLong("SpellCryoRayWindowUntil").orElse(0L))
                    ? data.getInt("SpellCryoRayStacks").orElse(0) + 1 : 1;
            data.putInt("SpellCryoRayStacks", stacks);
            data.putLong("SpellCryoRayWindowUntil", now + 80L);
            if (stacks >= 2) {
                data.putInt("SpellCryoRayStacks", 0);
                applyIceStasis(level, caster, hit, 60, spell, slot);
            }
        }
        int steps = Math.min(20, (int) Math.ceil(maxReach));
        for (int i = 1; i <= steps; i++) {
            BlockPos c = BlockPos.containing(caster.getEyePosition().add(direction.scale(i)));
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-1, -1, -1), c.offset(1, 0, 1))) {
                if (level.getBlockState(p).is(Blocks.WATER)) {
                    level.setBlockAndUpdate(p, Blocks.ICE.defaultBlockState());
                }
            }
        }
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Flash-Freeze Stasis: 1.5s (30t) complete ice stasis
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            applyIceStasis(level, caster, hit, 30, spell, slot);
        }
        if (!hitEntities.isEmpty()) {
            level.playSound(null, caster.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.0F, 1.3F);
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Frost Nova Burst: encase grounded foes' feet in temporary non-flooding ice for 2s (40t) & freeze surface liquids
        AABB box = new AABB(center.x - radius, center.y - 2.0D, center.z - radius, center.x + radius, center.y + 2.5D, center.z + radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity == caster || !entity.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = entity.position().subtract(center).normalize().scale(0.9D);
            entity.setDeltaMovement(away.x, 0.25D, away.z);
            entity.hasImpulse = true;
            applyIceStasis(level, caster, entity, 40, spell, slot);
        }
        freezeLiquidsInRadius(level, BlockPos.containing(center), Math.min(6, (int) Math.ceil(radius)));
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.6D, center.z, 35, radius * 0.45D, 0.3D, radius * 0.45D, 0.08D);
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Glacial Spikes Retaliation
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.SNOWFLAKE, caster.getX(), caster.getY(0.8D), caster.getZ(), 20, 0.4D, 0.5D, 0.4D, 0.05D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Rime Pitfall: hollow frosted ice floor that collapses when stepped on!
        BlockPos floorCenter = BlockPos.containing(glyphCenter).below();
        for (BlockPos p : BlockPos.betweenClosed(floorCenter.offset(-1, 0, -1), floorCenter.offset(1, 0, 1))) {
            BlockState bs = level.getBlockState(p);
            if (!SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs) && bs.getDestroySpeed(level, p) <= 4.0F) {
                level.setBlockAndUpdate(p, Blocks.FROSTED_ICE.defaultBlockState());
            }
        }
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 2.5D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - 1.8D, zone.center.y - 1.0D, zone.center.z - 1.8D,
                            zone.center.x + 1.8D, zone.center.y + 2.0D, zone.center.z + 1.8D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner);
                    if (stepped.isEmpty()) return true;

                    for (BlockPos p : BlockPos.betweenClosed(floorCenter.offset(-1, -2, -1), floorCenter.offset(1, 0, 1))) {
                        BlockState bs = srv.getBlockState(p);
                        if (!SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, bs) && bs.getDestroySpeed(srv, p) <= 3.5F) {
                            srv.destroyBlock(p, false);
                        }
                    }
                    for (LivingEntity v : stepped) {
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.playSound(null, floorCenter, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.85F);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Hoarfrost Flash-Freeze: freezes water -> ice & lava -> obsidian in cone
        for (LivingEntity hit : hitTargets) {
            if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 60);
            }
        }
        int steps = Math.min(10, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            freezeLiquidsInRadius(level, stepPos, 2);
            // Section 4.7 Frost Environmental Coating: lay slick snow/frost surface patches over solid ground
            for (BlockPos p : BlockPos.betweenClosed(stepPos.offset(-1, -1, -1), stepPos.offset(1, 0, 1))) {
                if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isSolidRender() && level.random.nextFloat() < 0.35F) {
                    level.setBlockAndUpdate(p, Blocks.SNOW.defaultBlockState());
                }
            }
        }
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Colossal Glacial Monolith: plummets after 2 seconds (40t), crushes impact zone & leaves a Blue Ice monument!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, Math.max(4.0D, radius), 42, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed < 40) {
                        if (elapsed % 4 == 0) {
                            srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + 6.0D - (elapsed * 0.12D), zone.center.z,
                                    16, 0.8D, 0.8D, 0.8D, 0.03D);
                        }
                        return true;
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB crush = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 4.0D, zone.center.z + zone.radius);
                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, crush, e -> e.isAlive() && e != owner)) {
                        applyIceStasis(srv, owner, v, 50, zone.spell, zone.slot);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 1.25F);
                    }
                    BlockPos base = BlockPos.containing(zone.center);
                    for (int dy = 0; dy < 3; dy++) {
                        BlockPos spire = base.above(dy);
                        if (srv.getBlockState(spire).isAir()) {
                            srv.setBlockAndUpdate(spire, Blocks.BLUE_ICE.defaultBlockState());
                        }
                    }
                    srv.playSound(null, base, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 0.65F);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.FROZEN.name());
        tag.putInt("SpellEidolonTintRGB", GLACIAL_EIDOLON_HEX_COLOR);
        level.sendParticles(ParticleTypes.SNOWFLAKE, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 24, 0.5D, 0.6D, 0.5D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Frostwalker Stasis: leaves a 2-block-tall taunting ice statue at departure that shatters into a cryo burst after 3s (60t)!
        BlockPos dep = BlockPos.containing(departurePos);
        List<BlockPos> statueBlocks = new java.util.ArrayList<>();
        for (int dy = 0; dy <= 1; dy++) {
            BlockPos p = dep.above(dy);
            if (level.getBlockState(p).isAir()) {
                level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                statueBlocks.add(p.immutable());
            }
        }
        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 4.0D, 60, level.getGameTime(), dummy, dummy.subSpells().get(0), EssenceType.FROZEN, 1.0F,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed < 58) {
                        if (elapsed % 12 == 0) {
                            for (net.minecraft.world.entity.Mob mob : srv.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
                                    new AABB(zone.center.x - 6.0D, zone.center.y - 2.0D, zone.center.z - 6.0D,
                                            zone.center.x + 6.0D, zone.center.y + 3.0D, zone.center.z + 6.0D))) {
                                if (mob.isAlive() && mob.getTarget() == owner) {
                                    mob.setTarget(null);
                                    mob.getNavigation().moveTo(zone.center.x, zone.center.y, zone.center.z, 1.15D);
                                }
                            }
                        }
                        return true;
                    }
                    for (BlockPos bp : statueBlocks) {
                        if (srv.getBlockState(bp).is(Blocks.PACKED_ICE)) {
                            srv.removeBlock(bp, false); // Clean shatter — zero water flooding!
                        }
                    }
                    AABB shatterBox = new AABB(zone.center.x - 3.5D, zone.center.y - 1.5D, zone.center.z - 3.5D,
                            zone.center.x + 3.5D, zone.center.y + 3.0D, zone.center.z + 3.5D);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, shatterBox, e -> e.isAlive() && e != owner)) {
                        if (owner != null && foe.isAlliedTo(owner)) continue;
                        foe.hurtServer(srv, srv.damageSources().freeze(), 3.0F + potencyTier * 1.25F);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                        foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 60);
                    }
                    srv.sendParticles(ParticleTypes.ITEM_SNOWBALL, zone.center.x, zone.center.y + 1.0D, zone.center.z, 24, 0.5D, 0.6D, 0.5D, 0.08D);
                    srv.playSound(null, dep, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.2F);
                    return false;
                }
        ));
        level.sendParticles(ParticleTypes.SNOWFLAKE, departurePos.x, departurePos.y + 0.8D, departurePos.z, 20, 0.4D, 0.6D, 0.4D, 0.05D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Silk Touch & Permafrost
        if (state.is(Blocks.LAVA)) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.SNOW_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.PACKED_ICE) || state.is(Blocks.ICE)) {
            level.setBlockAndUpdate(pos, Blocks.BLUE_ICE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
            return true;
        }
        // Silk Touch clean block drop for breakable blocks
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness >= 0.0F && hardness <= 2.0F + potencyTier * 5.0F) {
            ItemStack silkTool = new ItemStack(Items.NETHERITE_PICKAXE);
            level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .get(Enchantments.SILK_TOUCH)
                    .ifPresent(holder -> silkTool.enchant(holder, 1));
            Block.dropResources(state, level, pos, null, caster, silkTool);
            level.removeBlock(pos, false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            net.minecraft.world.entity.item.ItemEntity itemEntity,
            int potencyTier
    ) {
        ItemStack stack = itemEntity.getItem();
        if (stack.is(Items.SNOWBALL) || stack.is(Items.SNOW_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.ICE, stack.getCount()));
            return true;
        }
        if (stack.is(Items.ICE)) {
            itemEntity.setItem(new ItemStack(Items.PACKED_ICE, stack.getCount()));
            return true;
        }
        if (stack.is(Items.PACKED_ICE)) {
            itemEntity.setItem(new ItemStack(Items.BLUE_ICE, stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Permafrost Stasis / Deep-Freeze Shatter Hex: 60% slowness, locks sprinting, +50% physical shatter amplification, cryogenic DoT
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.setSprinting(false);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3, true, false));
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + 40);
        target.hurtServer(level, level.damageSources().freeze(), (1.25F + potencyTier * 0.5F) * mult);
        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.7D), target.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }

    private static void applyIceStasis(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            int ticks,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), ticks, 0));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6));
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + ticks);

        // Encase grounded target's feet in temporary non-flooding Packed Ice that shatters cleanly back to air!
        BlockPos feet = target.blockPosition();
        if (target.onGround() && level.getBlockState(feet).isAir()) {
            level.setBlockAndUpdate(feet, Blocks.PACKED_ICE.defaultBlockState());
            final BlockPos cleanupPos = feet.immutable();
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, Vec3.atCenterOf(cleanupPos), 1.0D, ticks, level.getGameTime(), spell, slot, EssenceType.FROZEN, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < ticks - 2) return true;
                        if (srv.getBlockState(cleanupPos).is(Blocks.PACKED_ICE)) {
                            srv.removeBlock(cleanupPos, false);
                        }
                        return false;
                    }
            ));
        }
    }

    private static void freezeLiquidsInRadius(ServerLevel level, BlockPos center, int rad) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-rad, -1, -rad), center.offset(rad, 1, rad))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, Blocks.ICE.defaultBlockState());
            } else if (bs.is(Blocks.LAVA)) {
                level.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
            }
        }
    }
}
