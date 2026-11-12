package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 * Complete per-part Signature Mutation implementation for <b>Barrow Materia (Tier 1 Fusion #38)</b>
 * (Spellcrafting System Guide Section 6.39 — Earth + Undead [Leans: Undead]).
 */
public final class BarrowSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int GRAVE_WARDEN_HEX_COLOR = 0x773214;

    @Override
    public EssenceType materiaType() {
        return EssenceType.BARROW;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Ossuary Bone-Shield Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Tombstone Ossuary Boulder-Slug";
            case BEAM -> "Barrow-Tomb Necrotic Tether Beam";
            case RADIAL_BURST -> "Catacomb Miasma Eruption";
            case TOUCH_STRIKE -> "Barrow-Cairn Tombstone Slam";
            case AURA -> "Catacomb Miasma Shroud";
            case GLYPH -> "Cairn Collapse Mine";
            case CONE -> "Ossuary Bone-Grit & Tomb-Dust Deluge";
            case SKYFALL -> "Megalithic Ossuary Cataclysm Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Subterranean Crypt-Tunnel Step";
            case TRANSMUTE, BREAK -> "Megalithic Grave Transmute";
            case AFFLICTION -> "Petrifying Sepulchral Entombment Hex";
            default -> "Sepulchral Barrow " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Ossuary Bone-Shield Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D);
        level.sendParticles(ParticleTypes.ASH, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CRACKED_STONE_BRICKS.defaultBlockState()),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.03D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Tombstone Ossuary Boulder-Slug: 6m KB, roots in grave mud 2.5s (50t), buries feet in 2m coarse burial earth!
        if (hitEntity instanceof LivingEntity victim && victim.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, spell.hasWardFriendlyFireSafeguard()))) {
            Vec3 kb = projectile.getDeltaMovement().normalize().scale(1.45D);
            victim.setDeltaMovement(kb.x, 0.35D, kb.z);
            victim.hasImpulse = true;
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
            victim.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
            EntityHelper.getPersistentData(victim).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 50L);
        }
        spreadBurialEarthPatch(level, caster, hitEntity.position(), spell);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        spreadBurialEarthPatch(level, caster, blockHit.getLocation(), spell);
    }

    private static void spreadBurialEarthPatch(ServerLevel level, @Nullable LivingEntity caster, Vec3 hitPos, SpellAssembly spell) {
        BlockPos center = BlockPos.containing(hitPos);
        Map<BlockPos, BlockState> saved = new HashMap<>();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 0, 1))) {
            BlockState gs = level.getBlockState(p);
            if ((gs.is(BlockTags.DIRT) || gs.is(Blocks.STONE)) && !gs.is(Blocks.COARSE_DIRT)
                    && level.getBlockState(p.above()).isAir()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, gs)) {
                saved.put(p.immutable(), gs);
                level.setBlockAndUpdate(p, Blocks.COARSE_DIRT.defaultBlockState());
            }
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CRACKED_STONE_BRICKS.defaultBlockState()),
                hitPos.x, hitPos.y + 0.4D, hitPos.z, 22, 0.9D, 0.4D, 0.9D, 0.08D);
        level.playSound(null, center, SoundEvents.DEEPSLATE_BRICKS_BREAK, SoundSource.PLAYERS, 1.0F, 0.75F);
        if (!saved.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, hitPos, 2.0D, 100, level.getGameTime(), spell, null, EssenceType.BARROW, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        for (Map.Entry<BlockPos, BlockState> e : saved.entrySet()) {
                            if (srv.getBlockState(e.getKey()).is(Blocks.COARSE_DIRT)) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        return false;
                    }
            ));
        }
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
        // Barrow-Tomb Necrotic Tether Beam: drags downward, -60% speed, -1 armor/s;
        // 3.0s sustained triggers Sepulchral Entombment (2-block temporary stone sarcophagus for 2.0s / 40t + heavy crush)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(hit.getDeltaMovement().x * 0.35D, Math.min(-0.22D, hit.getDeltaMovement().y), hit.getDeltaMovement().z * 0.35D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 3));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 1.0D, 0.0D, 80L);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("BarrowBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("BarrowBeamStacks").orElse(0) + 1 : 1;
            data.putLong("BarrowBeamLastTick", now);
            data.putInt("BarrowBeamStacks", stacks);

            if (stacks >= 3) {
                data.putInt("BarrowBeamStacks", 0);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                hit.hurtServer(level, level.damageSources().inWall(), 7.5F + spell.getEffectiveTier() * 2.0F);
                entombTargetInSarcophagus(level, caster, hit, spell);
            }
        }
    }

    private static void entombTargetInSarcophagus(ServerLevel level, LivingEntity caster, LivingEntity victim, SpellAssembly spell) {
        BlockPos base = victim.blockPosition();
        List<BlockPos> placed = new ArrayList<>(2);
        if (level.getBlockState(base).isAir()) {
            level.setBlockAndUpdate(base, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            placed.add(base.immutable());
        }
        if (level.getBlockState(base.above()).isAir()) {
            level.setBlockAndUpdate(base.above(), Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
            placed.add(base.above().immutable());
        }
        level.playSound(null, base, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.7F, 0.65F);
        if (!placed.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, Vec3.atCenterOf(base), 1.5D, 40, level.getGameTime(), spell, null, EssenceType.BARROW, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        for (BlockPos bp : placed) {
                            if (srv.getBlockState(bp).is(Blocks.MOSSY_STONE_BRICKS) || srv.getBlockState(bp).is(Blocks.CRACKED_STONE_BRICKS)) {
                                srv.removeBlock(bp, false);
                            }
                        }
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CRACKED_STONE_BRICKS.defaultBlockState()),
                                zone.center.x, zone.center.y + 0.8D, zone.center.z, 20, 0.4D, 0.5D, 0.4D, 0.08D);
                        return false;
                    }
            ));
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
        // Catacomb Miasma Eruption: 6m cloud — Wither + Poison II, Blindness 5.0s (100t), heals allied undead/summons, +4 Armor to caster!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (!ent.isAlive() || ent == caster) continue;
            if ((ent instanceof SpellEidolonEntity eid && caster.getUUID().equals(eid.getOwnerUuid()))
                    || (ent.isAlliedTo(caster) && ent.getType().is(EntityTypeTags.UNDEAD))) {
                ent.heal(6.0F);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            ent.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
            ent.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            ent.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
        }
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.0D, 0.0D, 160L);
        level.sendParticles(ParticleTypes.ASH, center.x, center.y + 0.9D, center.z, 40, 2.3D, 0.6D, 2.3D, 0.05D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()),
                center.x, center.y + 0.8D, center.z, 26, 2.0D, 0.5D, 2.0D, 0.08D);
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
        // Barrow-Cairn Tombstone Slam: 6m KB, 1.5s (30t) stun, Slowness IV 3.0s (60t), strips 3 armor, +3 Armor to caster 5.0s (100t)!
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(push.x * 1.45D, 0.35D, push.z * 1.45D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSSY_STONE_BRICKS.defaultBlockState()),
                    hit.getX(), hit.getY(0.8D), hit.getZ(), 20, 0.35D, 0.4D, 0.35D, 0.08D);
        }
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 3.0D, 0.0D, 0.0D, 0.0D, 100L);
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
        // Catacomb Miasma Shroud: -40% mob detection, Wither & Slowness within 4m, poison/wither immunity, Night Vision!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_BARROW_SHROUD_UNTIL, level.getGameTime() + dur);
        caster.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, dur + 40, 0, false, false));
        level.sendParticles(ParticleTypes.ASH, caster.getX(), caster.getY(0.8D), caster.getZ(), 24, 0.5D, 0.55D, 0.5D, 0.03D);
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
        // Cairn Collapse Mine: collapses into an explosive pitfall stripping 3 armor points and launching caught mobs 6m airborne!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 12 == 0) {
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.COARSE_DIRT.defaultBlockState()),
                                zone.center.x, zone.center.y + 0.15D, zone.center.z, 4, 0.7D, 0.1D, 0.7D, 0.02D);
                    }
                    if (elapsed < 10) return true;

                    AABB trigger = new AABB(zone.center.x - 2.3D, zone.center.y - 1.0D, zone.center.z - 2.3D,
                            zone.center.x + 2.3D, zone.center.y + 2.0D, zone.center.z + 2.3D);
                    List<LivingEntity> foes = srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (foes.isEmpty()) return true;

                    AABB blast = new AABB(zone.center.x - 3.8D, zone.center.y - 1.5D, zone.center.z - 3.8D,
                            zone.center.x + 3.8D, zone.center.y + 3.5D, zone.center.z + 3.8D);
                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, blast, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        SpellCombatReactionEngine.applyTimedAttributeShred(v, srv, 3.0D, 0.0D, 160L);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 1.15F);
                        v.setDeltaMovement(v.getDeltaMovement().x * 0.4D, 0.98D, v.getDeltaMovement().z * 0.4D);
                        v.hasImpulse = true;
                    }
                    srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                            zone.center.x, zone.center.y + 0.5D, zone.center.z, 34, 1.3D, 0.5D, 1.3D, 0.12D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
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
        // Ossuary Bone-Grit & Tomb-Dust Deluge: 6m KB, shreds 3 armor, Blindness + Slowness III 3.5s (70t),
        // smothers torches & surface fires, turns dirt into coarse dirt or gravel!
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(push.x * 1.45D, 0.35D, push.z * 1.45D);
            hit.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 2));
        }

        Vec3 eye = caster.getEyePosition();
        for (double d = 1.5D; d <= coneRange; d += 1.5D) {
            BlockPos step = BlockPos.containing(eye.add(push.scale(d)));
            for (BlockPos p : BlockPos.betweenClosed(step.offset(-1, -2, -1), step.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (bs.is(Blocks.TORCH) || bs.is(Blocks.WALL_TORCH) || bs.is(Blocks.SOUL_TORCH) || bs.is(Blocks.SOUL_WALL_TORCH)) {
                    level.destroyBlock(p, true);
                } else if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                } else if ((bs.is(Blocks.GRASS_BLOCK) || bs.is(Blocks.DIRT)) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                    level.setBlockAndUpdate(p, level.random.nextBoolean() ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRAVEL.defaultBlockState());
                }
            }
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
        // Megalithic Ossuary Cataclysm Downpour: 10m radius over 6s (120t) — falling tomb megaliths knock mobs flat,
        // shred 2 armor per hit, and leave temporary petrified stone markers & coarse grave dirt!
        double r = Math.max(10.0D, radius);
        Map<BlockPos, BlockState> savedGround = new HashMap<>();
        List<BlockPos> markers = new ArrayList<>();

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (BlockPos mp : markers) {
                            if (srv.getBlockState(mp).is(Blocks.MOSSY_STONE_BRICK_WALL)) {
                                srv.removeBlock(mp, false);
                            }
                        }
                        for (Map.Entry<BlockPos, BlockState> e : savedGround.entrySet()) {
                            if (srv.getBlockState(e.getKey()).is(Blocks.COARSE_DIRT)) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        return false;
                    }
                    if (elapsed % 12 == 0) {
                        double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                        double dist = srv.random.nextDouble() * (zone.radius - 1.5D);
                        Vec3 strike = zone.center.add(Math.cos(ang) * dist, 0.0D, Math.sin(ang) * dist);
                        BlockPos gp = BlockPos.containing(strike).below();
                        BlockState gs = srv.getBlockState(gp);
                        if ((gs.is(BlockTags.DIRT) || gs.is(Blocks.STONE)) && !savedGround.containsKey(gp)
                                && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, gp, gs)) {
                            savedGround.put(gp.immutable(), gs);
                            srv.setBlockAndUpdate(gp, Blocks.COARSE_DIRT.defaultBlockState());
                        }
                        BlockPos markerPos = gp.above();
                        if (markers.size() < 6 && srv.getBlockState(markerPos).isAir() && srv.getBlockState(gp).isSolidRender()) {
                            srv.setBlockAndUpdate(markerPos, Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState());
                            markers.add(markerPos.immutable());
                        }
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSSY_STONE_BRICKS.defaultBlockState()),
                                strike.x, strike.y + 0.6D, strike.z, 24, 1.1D, 0.6D, 1.1D, 0.1D);
                        srv.playSound(null, markerPos, SoundEvents.DEEPSLATE_BRICKS_BREAK, SoundSource.PLAYERS, 0.85F, 0.7F);

                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 2.0D, 0.0D, 120L);
                            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.3D, -0.35D, foe.getDeltaMovement().z * 0.3D);
                            foe.hasImpulse = true;
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F);
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
        tag.putString("SpellEidolonMateria", EssenceType.BARROW.name());
        tag.putInt("SpellEidolonTintRGB", GRAVE_WARDEN_HEX_COLOR);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 22, 0.45D, 0.5D, 0.45D, 0.06D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Subterranean Crypt-Tunnel Step: 14m step raising a 1-block burial mound at departure that taunts hostiles,
        // and granting caster +4 Armor & 50% KB resistance for 4.0s (80t) on arrival!
        BlockPos moundPos = BlockPos.containing(departurePos).immutable();
        boolean placed = false;
        if (level.getBlockState(moundPos).isAir() && !SpellExecutionEngine.isProtectedBlockOrContainer(level, moundPos, level.getBlockState(moundPos))) {
            level.setBlockAndUpdate(moundPos, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            placed = true;
        }
        boolean finalPlaced = placed;
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 8.0D, 80, level.getGameTime(), null, null, EssenceType.BARROW, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        if (finalPlaced && srv.getBlockState(moundPos).is(Blocks.MOSSY_COBBLESTONE)) {
                            srv.removeBlock(moundPos, false);
                        }
                        return false;
                    }
                    if (elapsed % 10 == 0) {
                        AABB lure = new AABB(departurePos.x - 8.0D, departurePos.y - 2.0D, departurePos.z - 8.0D,
                                departurePos.x + 8.0D, departurePos.y + 3.0D, departurePos.z + 8.0D);
                        for (Mob mob : srv.getEntitiesOfClass(Mob.class, lure, m -> m.isAlive() && m instanceof Monster)) {
                            mob.setTarget(null);
                            mob.getNavigation().moveTo(departurePos.x, departurePos.y, departurePos.z, 1.15D);
                        }
                    }
                    return true;
                }
        ));
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.50D, 0.0D, 80L);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COARSE_DIRT.defaultBlockState()),
                arrivalPos.x, arrivalPos.y + 0.5D, arrivalPos.z, 18, 0.4D, 0.35D, 0.4D, 0.05D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Megalithic Grave Transmute: dirt/grass/mud -> coarse dirt, mossy cobblestone, or cracked stone bricks;
        // animal corpses/rotten flesh -> bone blocks; cobblestone -> mossy cobblestone; dead wood -> petrified/amber wood!
        boolean didItem = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) didItem = true;
        }
        if (state.is(BlockTags.DIRT) || state.is(Blocks.MUD)) {
            int pick = level.random.nextInt(3);
            BlockState out = switch (pick) {
                case 0 -> Blocks.COARSE_DIRT.defaultBlockState();
                case 1 -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                default -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            return true;
        }
        if (state.is(Blocks.COBBLESTONE)) {
            level.setBlockAndUpdate(pos, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.LOGS) && !state.is(ModBlocks.AMBER_LOG.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AMBER_LOG.get().withPropertiesOf(state));
            return true;
        }
        return didItem;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.BEEF) || stack.is(Items.PORKCHOP) || stack.is(Items.MUTTON)) {
            itemEntity.setItem(new ItemStack(Items.BONE_BLOCK, stack.getCount()));
            return true;
        }
        if (stack.is(Items.COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(Items.MOSSY_COBBLESTONE, stack.getCount()));
            return true;
        }
        if (stack.is(ItemTags.LOGS)) {
            itemEntity.setItem(new ItemStack(Items.PETRIFIED_OAK_SLAB, Math.min(64, stack.getCount() * 2)));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Petrifying Sepulchral Entombment Hex: -40% move & attack speed, blocks jumping, -40% armor toughness,
        // +25%/+35% physical vulnerability, & Catacomb Awakening spawn-lure on players!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 2, true, true));
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 35L);
        data.putLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL, now + 35L);
        target.hurtServer(level, level.damageSources().inWall(), (1.40F + potencyTier * 0.45F) * mult);
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                target.getX(), target.getY(0.75D), target.getZ(), 5, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }
}
