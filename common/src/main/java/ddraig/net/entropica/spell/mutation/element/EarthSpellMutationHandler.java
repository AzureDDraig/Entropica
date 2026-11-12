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
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Earth Materia (Base Type #4)</b>
 * (Spellcrafting System Guide Section 6.5).
 */
public final class EarthSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int TECTONIC_EIDOLON_HEX_COLOR = 0x773214;

    @Override
    public EssenceType materiaType() {
        return EssenceType.EARTH;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Dense Tectonic Mass";
            default -> "Seismic Tectonic " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Quake Spire";
            case BEAM -> "Magnetite Tractor Beam";
            case RADIAL_BURST -> "Stone Ring Rampart";
            case TOUCH_STRIKE -> "Seismic Impact";
            case AURA -> "Earth Defensive Attunement";
            case GLYPH -> "Tectonic Impalement Spike";
            case CONE -> "Sand & Gravel Flurry";
            case SKYFALL -> "Meteorite Shrapnel Barrage";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Burrow Step";
            case TRANSMUTE, BREAK -> "Lithic Compaction";
            case AFFLICTION -> "Sundering Hex";
            case CONSTRUCT -> "Tectonic Bastion Wall";
            default -> "Tectonic " + effect.getDisplayName();
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
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 3, 0.1D, 0.1D, 0.1D, 0.05D);
        }
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        // Quake Spire: skims closely along ground terrain when fired horizontally near the earth!
        BlockPos below = projectile.blockPosition().below();
        if (level.getBlockState(below).isAir() && level.getBlockState(below.below()).isSolidRender()) {
            projectile.setPos(projectile.getX(), Math.max(below.getY() + 0.35D, projectile.getY() - 0.12D), projectile.getZ());
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLESTONE.defaultBlockState()),
                projectile.getX(), projectile.getY() - 0.15D, projectile.getZ(), 2, 0.1D, 0.05D, 0.1D, 0.03D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Quake Spire: temporary stone spike erupts beneath target's feet, launching them upward!
        if (hitEntity instanceof LivingEntity living && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            Vec3 v = living.getDeltaMovement();
            living.setDeltaMovement(v.x * 0.4D, 0.92D, v.z * 0.4D);
            living.hasImpulse = true;
            shredArmor(level, living, 2, 140L);
            BlockPos spikePos = living.blockPosition();
            if (level.getBlockState(spikePos).isAir() && level.getBlockState(spikePos.below()).isSolidRender()) {
                level.setBlockAndUpdate(spikePos, Blocks.POINTED_DRIPSTONE.defaultBlockState());
                final BlockPos cleanupPos = spikePos.immutable();
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        caster, Vec3.atCenterOf(cleanupPos), 1.0D, 60, level.getGameTime(), spell, slot, EssenceType.EARTH, 1.0F,
                        (srv, zone, elapsed) -> {
                            if (elapsed < 58) return true;
                            if (srv.getBlockState(cleanupPos).is(Blocks.POINTED_DRIPSTONE)) {
                                srv.removeBlock(cleanupPos, false);
                            }
                            return false;
                        }
                ));
            }
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()),
                    living.getX(), living.getY() + 0.3D, living.getZ(), 18, 0.25D, 0.5D, 0.25D, 0.08D);
            level.playSound(null, living.blockPosition(), SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 0.9F, 0.8F);
        }
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Magnetite Tractor Beam: pulls dropped items & armored mobs steadily toward the caster
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            double mult = hit.getArmorValue() > 0 ? 1.15D : 0.65D;
            Vec3 pull = caster.position().subtract(hit.position()).normalize().scale(mult);
            hit.setDeltaMovement(pull.x, 0.22D, pull.z);
            hit.hasImpulse = true;
        }
        AABB itemBox = caster.getBoundingBox().expandTowards(direction.scale(maxReach)).inflate(1.5D);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, itemBox)) {
            Vec3 pull = caster.getEyePosition().subtract(item.position()).normalize().scale(0.85D);
            item.setDeltaMovement(pull);
            item.hasImpulse = true;
        }
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Seismic Impact: shreds armor points & knocks target flat
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            shredArmor(level, hit, 3, 180L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 3));
            hit.setDeltaMovement(direction.x * 0.85D, -0.35D, direction.z * 0.85D);
            hit.hasImpulse = true;
        }
        if (!hitEntities.isEmpty()) {
            level.playSound(null, caster.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.55F, 0.75F);
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Stone Ring Rampart: raises a circular ring of temporary stone barrier pillars around the caster for 8 seconds!
        List<BlockPos> placedPillars = new ArrayList<>();
        BlockPos cPos = BlockPos.containing(center);
        int ringRad = 3;
        for (int deg = 0; deg < 360; deg += 45) {
            double rad = Math.toRadians(deg);
            int dx = (int) Math.round(Math.cos(rad) * ringRad);
            int dz = (int) Math.round(Math.sin(rad) * ringRad);
            for (int dy = 0; dy <= 1; dy++) {
                BlockPos pillarPos = cPos.offset(dx, dy, dz);
                if (level.getBlockState(pillarPos).isAir()) {
                    level.setBlockAndUpdate(pillarPos, Blocks.COBBLESTONE.defaultBlockState());
                    placedPillars.add(pillarPos.immutable());
                }
            }
        }
        if (!placedPillars.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, center, ringRad, 160, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed < 158) return true;
                        for (BlockPos p : placedPillars) {
                            if (srv.getBlockState(p).is(Blocks.COBBLESTONE)) {
                                srv.destroyBlock(p, false);
                            }
                        }
                        return false;
                    }
            ));
        }
        level.playSound(null, cPos, SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 1.0F, 0.75F);
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Earth Defensive Attunement: +2.5 Armor Toughness, +55% Knockback Resistance, +1 vs Void (50% dmg), -1 vs Air (150% dmg)
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL, level.getGameTime() + dur);
        caster.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, dur, 0, false, true));
        SpellCombatReactionEngine.applyTimedDefenseBuff(caster, level, 2.5D, 0.55D, dur);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Tectonic Impalement Spike
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 2.5D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - 2.0D, zone.center.y - 1.0D, zone.center.z - 2.0D,
                            zone.center.x + 2.0D, zone.center.y + 2.5D, zone.center.z + 2.0D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner);
                    if (stepped.isEmpty()) return true;

                    for (LivingEntity v : stepped) {
                        v.setDeltaMovement(v.getDeltaMovement().x * 0.3D, 1.15D, v.getDeltaMovement().z * 0.3D);
                        v.hasImpulse = true;
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()),
                            zone.center.x, zone.center.y + 0.6D, zone.center.z, 24, 0.5D, 0.8D, 0.5D, 0.1D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 0.7F);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Sand & Gravel Flurry: pushes mobs 6 blocks back & excavates loose dirt/sand/gravel
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.35D, 0.3D, direction.z * 1.35D);
            hit.hasImpulse = true;
        }
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos c = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-1, -1, -1), c.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (!SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)
                        && (bs.is(BlockTags.DIRT) || bs.is(BlockTags.SAND) || bs.is(Blocks.GRAVEL))) {
                    level.destroyBlock(p, true, caster);
                }
            }
        }
        // Section 4.7 Earth Cone Recoil Impulse: heavy physical recoil pushing caster backward
        caster.push(-direction.x * 0.50D, 0.12D, -direction.z * 0.50D);
        caster.hasImpulse = true;
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Meteorite Shrapnel Barrage: 6s (120t) stone meteor rain battering foes & knocking down airborne mobs
        double r = Math.max(5.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()),
                                zone.center.x, zone.center.y + 5.0D, zone.center.z, 18, zone.radius * 0.5D, 1.0D, zone.radius * 0.5D, 0.2D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            if (!v.onGround()) {
                                v.setDeltaMovement(v.getDeltaMovement().x * 0.3D, -0.95D, v.getDeltaMovement().z * 0.3D);
                                v.hasImpulse = true;
                            }
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
        tag.putString("SpellEidolonMateria", EssenceType.EARTH.name());
        tag.putInt("SpellEidolonTintRGB", TECTONIC_EIDOLON_HEX_COLOR);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                spawnPos.x, spawnPos.y + 0.5D, spawnPos.z, 24, 0.5D, 0.5D, 0.5D, 0.1D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Burrow Step
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                departurePos.x, departurePos.y + 0.3D, departurePos.z, 20, 0.4D, 0.3D, 0.4D, 0.08D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                arrivalPos.x, arrivalPos.y + 0.3D, arrivalPos.z, 20, 0.4D, 0.3D, 0.4D, 0.08D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 0.9F, 0.8F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Lithic Compaction: Cobblestone -> Stone -> Deepslate, Sand/Gravel -> Sandstone
        if (state.is(Blocks.COBBLESTONE)) {
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.STONE)) {
            level.setBlockAndUpdate(pos, Blocks.DEEPSLATE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.GRAVEL)) {
            level.setBlockAndUpdate(pos, Blocks.SANDSTONE.defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        var stack = itemEntity.getItem();
        if (stack.is(net.minecraft.world.item.Items.COBBLESTONE)) {
            itemEntity.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE, stack.getCount()));
            return true;
        }
        if (stack.is(net.minecraft.world.item.Items.STONE)) {
            itemEntity.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DEEPSLATE, stack.getCount()));
            return true;
        }
        if (stack.is(net.minecraft.world.item.Items.SAND) || stack.is(net.minecraft.world.item.Items.GRAVEL)) {
            itemEntity.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SANDSTONE, stack.getCount()));
            return true;
        }
        if (potencyTier >= 5 && stack.is(net.minecraft.world.item.Items.COAL)) {
            itemEntity.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.AMETHYST_SHARD, stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Sundering Hex: shreds 5 points of real physical Armor (scaled by stack) & doubles incoming Earth spell damage!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 5.0D * mult, 0.0D, 35L);
        target.hurtServer(level, level.damageSources().magic(), (1.2F + potencyTier * 0.5F) * mult);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                target.getX(), target.getY(0.6D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.05D);
        return true;
    }

    private static void shredArmor(ServerLevel level, LivingEntity target, int pts, long durationTicks) {
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, pts, 0.0D, durationTicks);
    }
}
