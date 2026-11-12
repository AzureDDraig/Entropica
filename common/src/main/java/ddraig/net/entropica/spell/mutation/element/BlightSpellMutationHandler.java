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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Blight Materia (Ethereal &amp; Paradox Fusion #26)</b>
 * (Spellcrafting System Guide Section 6.27 — Undead + Nature [Leans: Undead]).
 */
public final class BlightSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int ROT_WARDEN_HEX_COLOR = 0x40984D;

    @Override
    public EssenceType materiaType() {
        return EssenceType.BLIGHT;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Cadaverous Contagion Chains Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Contagious Necro-Spore Pod";
            case BEAM -> "Necrotic Bramble Tether Beam";
            case RADIAL_BURST -> "Cadaverous Briar Nova";
            case TOUCH_STRIKE -> "Necrotic Thorn Gouge";
            case AURA -> "Cadaverous Briar-Carapace";
            case GLYPH -> "Strangling Rot-Briar Snare";
            case CONE -> "Virulent Necro-Spore Flurry";
            case SKYFALL -> "Cadaverous Rot Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Cadaverous Root-Step";
            case TRANSMUTE, BREAK -> "Necrotic Decomposition Transmute";
            case AFFLICTION -> "Virulent Wither-Blight Hex";
            default -> "Cadaverous Blight " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Cadaverous Contagion Chains Flow) ---

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromTarget,
            LivingEntity toTarget,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Each leap roots victim 1.5s (30t), shreds 1 armor point, and marks for 3s (60t) secondary corpse contagion leap!
        applyBlightRoot(toTarget, 30);
        SpellCombatReactionEngine.applyTimedAttributeShred(toTarget, level, 1.0D, 0.0D, 100L);
        EntityHelper.getPersistentData(toTarget).putLong(SpellLingeringZoneManager.TAG_BLIGHT_CHAIN_MARK_UNTIL, level.getGameTime() + 60L);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, toTarget.getX(), toTarget.getY(0.8D), toTarget.getZ(), 10, 0.25D, 0.3D, 0.25D, 0.03D);
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Contagious Necro-Spore Pod: roots target 2.5s (50t), marks for death contagion, bursts into 3m Poison II cloud!
        if (hitEntity instanceof LivingEntity victim && victim.isAlive()) {
            if (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, spell.hasWardFriendlyFireSafeguard())) {
                applyBlightRoot(victim, 50);
                EntityHelper.getPersistentData(victim).putLong(SpellLingeringZoneManager.TAG_BLIGHT_CHAIN_MARK_UNTIL, level.getGameTime() + 60L);
            }
        }
        spawnToxicNecroCloud(level, caster, projectile.position(), spell, slot);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        spawnToxicNecroCloud(level, caster, projectile.position(), spell, slot);
    }

    private static void spawnToxicNecroCloud(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell, SubSpellSlot slot) {
        AABB box = new AABB(center.x - 3.0D, center.y - 1.5D, center.z - 3.0D, center.x + 3.0D, center.y + 2.0D, center.z + 3.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
        }
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, center.x, center.y + 0.5D, center.z, 22, 1.3D, 0.5D, 1.3D, 0.04D);
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
        // Necrotic Bramble Tether Beam: pulls enemy toward caster + 50% slow; 3s continuous triggers Blight Bloom (-3 armor + 20% caster heal)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 pull = caster.position().subtract(hit.position()).normalize().scale(0.45D);
            hit.setDeltaMovement(pull.x, 0.15D, pull.z);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("BlightBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("BlightBeamStacks").orElse(0) + 1 : 1;
            data.putLong("BlightBeamLastTick", now);
            data.putInt("BlightBeamStacks", stacks);
            if (stacks >= 3) {
                data.putInt("BlightBeamStacks", 0);
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
                SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, spell.getEffectiveTier(), 0.20F, apparatusPotency);
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, hit.getX(), hit.getY(0.8D), hit.getZ(), 18, 0.4D, 0.4D, 0.4D, 0.05D);
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
        // Cadaverous Briar Nova: 6m ring roots caught enemies 2.5s (50t), Poison II 4s (80t), wilts nearby flowers into Wither Roses!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            applyBlightRoot(foe, 50);
            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
        }
        BlockPos cPos = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-5, -1, -5), cPos.offset(5, 2, 5))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(BlockTags.SMALL_FLOWERS) && !bs.is(Blocks.WITHER_ROSE)) {
                level.setBlockAndUpdate(p, Blocks.WITHER_ROSE.defaultBlockState());
            }
        }
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, center.x, center.y + 0.7D, center.z, 36, 2.3D, 0.5D, 2.3D, 0.05D);
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
        // Necrotic Thorn Gouge: Poison II & 40% Slowness 4s (80t), shreds 2 armor, corpse bursts into 3s root snare on death!
        long snareMark = level.getGameTime() + 100L;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 100L);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_BLIGHT_CORPSE_SNARE_UNTIL, snareMark);
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
        // Cadaverous Briar-Carapace: +6 Armor, Poison/Hunger immunity, undead neutrality, 30% thorns + Poison II melee retaliation!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_BLIGHT_CARAPACE_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 0.0D, 0.0D, 0.0D, dur);
        caster.removeEffect(MobEffects.POISON);
        caster.removeEffect(MobEffects.HUNGER);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.45D, 0.5D, 0.45D, 0.03D);
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
        // Strangling Rot-Briar Snare: 4m grasping necrotic vine cage — roots 3.5s (70t), shreds 3 armor, plants Wither Roses on death!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 12 == 0) {
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, zone.center.x, zone.center.y + 0.2D, zone.center.z, 5, 1.2D, 0.1D, 1.2D, 0.01D);
                    }
                    if (elapsed < 10) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB triggerBox = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                            zone.center.x + 2.5D, zone.center.y + 2.0D, zone.center.z + 2.5D);
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (triggers.isEmpty()) return true;

                    long mark = srv.getGameTime() + 120L;
                    AABB cageBox = new AABB(zone.center.x - 4.0D, zone.center.y - 1.5D, zone.center.z - 4.0D,
                            zone.center.x + 4.0D, zone.center.y + 2.5D, zone.center.z + 4.0D);
                    for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, cageBox, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        applyBlightRoot(victim, 70);
                        SpellCombatReactionEngine.applyTimedAttributeShred(victim, srv, 3.0D, 0.0D, 140L);
                        EntityHelper.getPersistentData(victim).putLong(SpellLingeringZoneManager.TAG_BLIGHT_CORPSE_SNARE_UNTIL, mark);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, victim, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 1.0F, 0.7F);
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
        // Virulent Necro-Spore Flurry: 3m knockback, Poison II & 40% Slowness 4s, shreds 2 armor, strips logs to decayed, wilts flora!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 0.85D, 0.25D, direction.z * 0.85D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 100L);
        }
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(stepPos.offset(-2, -1, -2), stepPos.offset(2, 1, 2))) {
                BlockState bs = level.getBlockState(p);
                if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
                if (bs.is(BlockTags.LOGS) && !bs.is(ModBlocks.STRIPPED_VOID_BLIGHT_MANGROVE_LOG.get())) {
                    level.setBlockAndUpdate(p, ModBlocks.STRIPPED_VOID_BLIGHT_MANGROVE_LOG.get().withPropertiesOf(bs));
                } else if (bs.is(BlockTags.SMALL_FLOWERS) || bs.is(Blocks.SHORT_GRASS) || bs.is(Blocks.TALL_GRASS) || bs.is(Blocks.FERN)) {
                    level.setBlockAndUpdate(p, Blocks.DEAD_BUSH.defaultBlockState());
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
        // Cadaverous Rot Tempest: 10s (200t), 14m radius raining fungal bile — Poison II, 50% Slowness, decays raw meat -> rotten flesh, roots foes!
        double r = Math.max(10.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, zone.center.x, zone.center.y + 4.5D, zone.center.z, 24, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.04D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (ItemEntity item : srv.getEntitiesOfClass(ItemEntity.class, area)) {
                            if (item.isAlive() && isRawMeat(item.getItem())) {
                                item.setItem(new ItemStack(Items.ROTTEN_FLESH, item.getItem().getCount()));
                            }
                        }
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            v.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                            v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
                            if (elapsed % 40 == 0) {
                                applyBlightRoot(v, 25);
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
        tag.putString("SpellEidolonMateria", EssenceType.BLIGHT.name());
        tag.putInt("SpellEidolonTintRGB", ROT_WARDEN_HEX_COLOR);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 24, 0.45D, 0.5D, 0.45D, 0.04D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Cadaverous Root-Step: leaves a necrotic bone-thorn cage at departure rooting pursuers for 3s (60t) and poisoning them!
        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 3.0D, 60, level.getGameTime(), dummy, dummy.subSpells().getFirst(), EssenceType.BLIGHT, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, zone.center.x, zone.center.y + 0.4D, zone.center.z, 8, 1.1D, 0.3D, 1.1D, 0.02D);
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB box = new AABB(zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                                zone.center.x + 3.0D, zone.center.y + 2.2D, zone.center.z + 3.0D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box)) {
                            if (foe == owner || !foe.isAlive() || (owner != null && foe.isAlliedTo(owner))) continue;
                            applyBlightRoot(foe, 60);
                            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                        }
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Necrotic Decomposition Transmute: rots foliage/wood into dead/blight variants, grass -> coarse dirt/podzol!
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.PODZOL.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.LOGS) && !state.is(ModBlocks.VOID_BLIGHT_MANGROVE_LOG.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.VOID_BLIGHT_MANGROVE_LOG.get().withPropertiesOf(state));
            return true;
        }
        if (state.is(BlockTags.PLANKS) && !state.is(ModBlocks.VOID_BLIGHT_MANGROVE_PLANKS.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.VOID_BLIGHT_MANGROVE_PLANKS.get().defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.SMALL_FLOWERS) && !state.is(Blocks.WITHER_ROSE)) {
            level.setBlockAndUpdate(pos, Blocks.WITHER_ROSE.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.LEAVES) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN)) {
            level.setBlockAndUpdate(pos, Blocks.DEAD_BUSH.defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.ROTTEN_FLESH) || in.is(Items.BONE)) {
            int baseCount = in.is(Items.BONE) ? in.getCount() * 3 : in.getCount();
            int boosted = Math.min(64, Math.max(baseCount + 1, Math.round(baseCount * 1.25F)));
            itemEntity.setItem(new ItemStack(Items.BONE_MEAL, boosted));
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.03D);
            return true;
        }
        if (isRawMeat(in)) {
            itemEntity.setItem(new ItemStack(Items.ROTTEN_FLESH, in.getCount()));
            return true;
        }
        if (in.is(ItemTags.LOGS)) {
            itemEntity.setItem(new ItemStack(ModBlocks.VOID_BLIGHT_MANGROVE_LOG.get().asItem(), in.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Virulent Wither-Blight Hex: continuous Blight damage, disables health regen, shreds 40% armor toughness, contagious death spread!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, 0.40D * mult, 35L);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 35, 1, true, true));
        target.hurtServer(level, level.damageSources().magic(), (1.45F + potencyTier * 0.50F) * mult);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY(0.8D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }

    private static void applyBlightRoot(LivingEntity target, int ticks) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6));
        target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), ticks, 0));
        target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
        target.hasImpulse = true;
    }

    private static boolean isRawMeat(ItemStack stack) {
        return stack.is(Items.BEEF) || stack.is(Items.PORKCHOP) || stack.is(Items.CHICKEN)
                || stack.is(Items.MUTTON) || stack.is(Items.RABBIT) || stack.is(Items.COD) || stack.is(Items.SALMON);
    }
}
