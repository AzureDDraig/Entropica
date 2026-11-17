package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.ScribedChalkBlock;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellEidolonSpecs;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Spore Materia (Tier 1 Fusion #18 — Nature + Air)</b>
 * (Spellcrafting System Guide Section 6.19).
 *
 * <p>Dual-tone palette: Nature Green ({@code #1A5923}) &amp; Buff Air Gold ({@code #A88654}).</p>
 */
public final class SporeSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int NATURE_GREEN_HEX = 0x1A5923;
    public static final int BUFF_AIR_GOLD_HEX = 0xA88654;
    public static final int SYMBIOTIC_FUNGAL_HEX_COLOR = 0x1A5923;

    private static final String TAG_INOCULATION_LAST_TICK = "SpellSporeInoculationLastTick";
    private static final String TAG_INOCULATION_STACKS = "SpellSporeInoculationStacks";
    private static final String TAG_INFESTED_CASTER = "SpellSporeInfestedCaster";

    @Override
    public EssenceType materiaType() {
        return EssenceType.SPORE;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case ORBIT_SATELLITE -> "Drifting Spore-Pollen Orbit Flow";
            default -> "Mycelial Spore " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Aerodynamic Fungal Puffball";
            case BEAM -> "Mycelial Inoculation Stream";
            case RADIAL_BURST -> "Fungal Bloom Nova";
            case TOUCH_STRIKE -> "Mycelial Infestation Strike";
            case AURA -> "Mycelial Spore-Cloud Mantle";
            case GLYPH -> "Silencing Mycelium Web";
            case CONE -> "Billowing Windborne Spore Blast";
            case SKYFALL -> "Choking Fungal Fog Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Hyphal Network Blink";
            case TRANSMUTE, BREAK -> "Nether Spore Transmutation";
            case AFFLICTION -> "Hallucinatory Mycelial Hex";
            case RESTORATION -> "Spore Carapace Restoration";
            default -> "Mycelial " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // PART 1 — FLOW MUTATIONS (DRIFTING SPORE-POLLEN ORBIT FLOW)
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        // Section 6.19 Drifting Spore-Pollen Orbit Flow:
        // Spore projectiles spiral tightly around the caster in a dense protective toxic shield,
        // poisoning any hostile attacker that enters melee range before flinging outward toward nearby targets!
        if (projectile.tickCount % 2 == 0) {
            int rgb = (projectile.tickCount % 4 == 0) ? NATURE_GREEN_HEX : BUFF_AIR_GOLD_HEX;
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), rgb, 20, 0.85F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.12D, 0.12D, 0.12D, 0.015D
            );
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.12D, 0.12D, 0.12D, 0.015D);
        }
        if (projectile.tickCount % 5 == 0) {
            Entity owner = projectile.getOwner();
            LivingEntity caster = owner instanceof LivingEntity liv ? liv : null;
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(1.6D))) {
                if (foe == owner || !foe.isAlive()) continue;
                if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                foe.addEffect(new MobEffectInstance(MobEffects.POISON, 70, 1));
                foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
            }
        }
    }

    // =========================================================================
    // PART 2 — FORM & DELIVERY MUTATIONS
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.19 Aerodynamic Fungal Puffball: floats with zero-gravity buoyant drift (#1A5923 / #A88654)!
        projectile.setNoGravity(true);
        projectile.setDeltaMovement(projectile.getDeltaMovement().scale(0.76D));
        level.playSound(null, projectile.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.65F, 1.18F);
        level.playSound(null, projectile.blockPosition(), SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 0.30F, 1.50F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        if (projectile.tickCount % 2 == 0) {
            int rgb = (projectile.tickCount % 4 == 0) ? NATURE_GREEN_HEX : BUFF_AIR_GOLD_HEX;
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), rgb, 22, 0.90F, 0.06F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.10D, 0.10D, 0.10D, 0.015D
            );
            level.sendParticles(ParticleTypes.MYCELIUM,
                    projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.12D, 0.12D, 0.12D, 0.015D);
        }
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
        burstPuffballCloud(level, caster, projectile.position(), projectile.getDeltaMovement(), spell, slot);
    }

    @Override
    public void onProjectileHitBlockFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        Vec3 nrm = Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i());
        burstPuffballCloud(level, caster, blockHit.getLocation().add(nrm.scale(0.15D)), nrm, spell, slot);
    }

    private static void burstPuffballCloud(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            Vec3 impactDir,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.19 Aerodynamic Fungal Puffball:
        // Bursts on contact into a 3-block fungal spore cloud that inflicts Poison II and Nausea for 3 seconds (60t)
        // while sprouting small edible mushrooms on fertile blocks!
        int tier = spell.getEffectiveTier();
        boolean wardSafeguard = spell.hasWardFriendlyFireSafeguard();
        boolean dealDamage = !spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb();
        float puffDmg = (2.8F + tier * 0.80F) * spell.flawProfile().getPotencyMultiplier();

        Vec3 dir = impactDir.lengthSqr() > 1.0E-4D ? impactDir.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
        SpellWorldVfxManager.broadcastProjectileImpact(level, center, dir, EssenceType.SPORE, EssenceType.AIR, 1.55F, tier);
        SpellWorldVfxManager.broadcastRadialNova(level, center, 3.0D, EssenceType.SPORE, EssenceType.AIR, tier);

        double cloudRad = 3.0D;
        double cloudRadSq = (cloudRad + 0.35D) * (cloudRad + 0.35D);
        AABB box = new AABB(
                center.x - cloudRad, center.y - 1.75D, center.z - cloudRad,
                center.x + cloudRad, center.y + 2.25D, center.z + cloudRad
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (foe.position().distanceToSqr(center) > cloudRadSq) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafeguard)) continue;
            if (dealDamage) {
                SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(level, foe, caster, puffDmg, false, true);
            }
            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
            foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
        }

        sproutMushroomsPublic(level, BlockPos.containing(center), 3, false);
        emitDualToneSporeBurst(level, center.add(0.0D, 0.35D, 0.0D), 22, 1.35D, 0.65D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.SPORE_BLOSSOM_BREAK, SoundSource.PLAYERS, 0.85F, 1.05F);
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
        // Section 6.19 Mycelial Inoculation Stream:
        // Continuous channeled beam of living mycelial spores; inflicts sustained Poison and Nausea on caught entities,
        // converts dirt and grass into Mycelium, and accelerates small mushrooms into Huge Mushrooms
        // after 3 seconds (15 consecutive 4-tick pulses = 60t) of sustained channeling!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean wardSafeguard = spell.hasWardFriendlyFireSafeguard();
        boolean dealDamage = !spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb();
        float rayDmg = (1.20F + tier * 0.36F) * Math.max(0.35F, apparatusPotency) * spell.flawProfile().getPotencyMultiplier();

        // Track continuous 3.0-second (60 ticks = 15 x 4t pulses) sustained beam channeling on the caster!
        CompoundTag cData = EntityHelper.getPersistentData(caster);
        long lastPulse = cData.getLong(TAG_INOCULATION_LAST_TICK).orElse(0L);
        int stacks = (now - lastPulse <= 16L) ? (cData.getInt(TAG_INOCULATION_STACKS).orElse(0) + 1) : 1;
        cData.putLong(TAG_INOCULATION_LAST_TICK, now);
        cData.putInt(TAG_INOCULATION_STACKS, stacks);
        boolean hugeMushroomReady = (stacks >= 15); // 15 x 4t = 60 ticks = exact 3.0 seconds!

        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafeguard)) continue;
            if (dealDamage) {
                SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(level, hit, caster, rayDmg, false, true);
            }
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
            hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));
        }

        Vec3 eye = caster.getEyePosition();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        double effectiveReach = maxReach;
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            effectiveReach = Math.min(maxReach, eye.distanceTo(blockHit.getLocation()) + 0.75D);
        }

        boolean grewHuge = false;
        for (double d = 1.0D; d <= effectiveReach; d += 1.0D) {
            BlockPos bp = BlockPos.containing(eye.add(normDir.scale(d)));
            for (BlockPos p : BlockPos.betweenClosed(bp.offset(-1, -1, -1), bp.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() instanceof ScribedChalkBlock || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                    continue;
                }
                if (bs.is(Blocks.DIRT) || bs.is(Blocks.GRASS_BLOCK) || bs.is(Blocks.COARSE_DIRT)
                        || bs.is(Blocks.ROOTED_DIRT) || bs.is(Blocks.PODZOL)) {
                    if (!(level.getBlockState(p.above()).getBlock() instanceof ScribedChalkBlock)) {
                        level.setBlockAndUpdate(p, Blocks.MYCELIUM.defaultBlockState());
                        if (level.getBlockState(p.above()).isAir() && level.random.nextFloat() < 0.08F) {
                            Block shroom = level.random.nextBoolean() ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM;
                            level.setBlockAndUpdate(p.above(), shroom.defaultBlockState());
                        }
                    }
                } else if (bs.getBlock() instanceof MushroomBlock || bs.getBlock() instanceof FungusBlock) {
                    if (!hugeMushroomReady) {
                        // Swelling mycelial inoculation cue during the 3-second channel build-up!
                        if (now % 8L == 0L) {
                            level.sendParticles(
                                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), BUFF_AIR_GOLD_HEX, 18, 0.90F, 0.06F),
                                    p.getX() + 0.5D, p.getY() + 0.45D, p.getZ() + 0.5D, 3, 0.22D, 0.25D, 0.22D, 0.02D
                            );
                        }
                    } else {
                        // Sustained 3.0s reached -> accelerate small mushrooms into Huge Mushrooms!
                        BlockPos soilPos = p.below();
                        BlockState soil = level.getBlockState(soilPos);
                        if (bs.getBlock() instanceof MushroomBlock
                                && (soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(Blocks.COARSE_DIRT) || soil.is(Blocks.ROOTED_DIRT))) {
                            level.setBlockAndUpdate(soilPos, Blocks.MYCELIUM.defaultBlockState());
                        }
                        BlockPos imm = p.immutable();
                        for (int attempt = 0; attempt < 5; attempt++) {
                            BlockState cur = level.getBlockState(imm);
                            if (cur.getBlock() instanceof BonemealableBlock grow && grow.isValidBonemealTarget(level, imm, cur)) {
                                grow.performBonemeal(level, level.random, imm, cur);
                            } else {
                                break;
                            }
                        }
                        if (!(level.getBlockState(imm).getBlock() instanceof MushroomBlock)
                                && !(level.getBlockState(imm).getBlock() instanceof FungusBlock)) {
                            grewHuge = true;
                            emitDualToneSporeBurst(level, Vec3.atCenterOf(imm).add(0.0D, 1.5D, 0.0D), 18, 1.4D, 1.2D);
                        }
                    }
                }
            }
        }
        if (grewHuge) {
            level.playSound(null, caster.blockPosition(), SoundEvents.FUNGUS_PLACE, SoundSource.PLAYERS, 0.95F, 0.80F);
            level.playSound(null, caster.blockPosition(), SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 0.85F, 0.95F);
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
        // Section 6.19 Fungal Bloom Nova:
        // Explodes in an outward 360-degree spore ring with wind pressure knockback;
        // inflicts Poison II and Nausea for 4 seconds (80t), and carpets surrounding ground in mushrooms & mycelium turf!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        double rSq = (r + 0.5D) * (r + 0.5D);
        boolean wardSafeguard = spell.hasWardFriendlyFireSafeguard();
        boolean dealDamage = !spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb();
        float novaDmg = (3.4F + tier * 0.95F) * apparatusPotency * spell.flawProfile().getPotencyMultiplier();

        SpellWorldVfxManager.broadcastRadialNova(level, center.add(0.0D, 0.08D, 0.0D), r, EssenceType.SPORE, EssenceType.AIR, tier);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.8D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (foe.position().distanceToSqr(center) > rSq) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafeguard)) continue;

            if (dealDamage) {
                SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(level, foe, caster, novaDmg, false, true);
            }
            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
            foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));

            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 delta = foe.position().subtract(center);
                Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(away.x * 1.18D, 0.36D, away.z * 1.18D));
            }
        }

        sproutMushroomsPublic(level, BlockPos.containing(center), Math.min(6, (int) Math.ceil(r * 0.80D)), true);
        emitDualToneSporeBurst(level, center.add(0.0D, 0.75D, 0.0D), 36, 2.4D, 0.75D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.90F, 0.96F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.SPORE_BLOSSOM_BREAK, SoundSource.PLAYERS, 0.95F, 0.90F);
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
        // Section 6.19 Mycelial Infestation Strike:
        // Implants virulent fungal spores into the target on melee contact; inflicts Poison II and Nausea for 5 seconds (100t),
        // reduces movement speed by an exact 40%, and if the target dies while infected, their body bursts into a
        // mini-spore pop that infects adjacent enemies and sprouts mushrooms!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean wardSafeguard = spell.hasWardFriendlyFireSafeguard();
        boolean dealDamage = !spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb();
        float strikeDmg = (3.1F + tier * 0.88F) * apparatusPotency * spell.flawProfile().getPotencyMultiplier();

        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.32D, 0.0D), normDir, 2.35D, EssenceType.SPORE, EssenceType.AIR, tier
        );

        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafeguard)) continue;

            // Stamp infestation BEFORE dealing damage so even one-shot kills reliably trigger the infectious death spore pop!
            CompoundTag hData = EntityHelper.getPersistentData(hit);
            hData.putLong(SpellLingeringZoneManager.TAG_SPORE_INFESTED_UNTIL, now + 120L);
            hData.putString(TAG_INFESTED_CASTER, caster.getUUID().toString());

            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.40D, 100L);

            if (dealDamage) {
                SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(level, hit, caster, strikeDmg, true, true);
            }
            emitDualToneSporeBurst(level, hit.position().add(0.0D, hit.getBbHeight() * 0.55D, 0.0D), 14, 0.36D, 0.38D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.85F, 0.88F);
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
        // Section 6.19 Mycelial Spore-Cloud Mantle:
        // Surrounds caster in an active protective spore cloud; grants immunity to Poison and Nausea,
        // increases movement speed by +30% on Mycelium, retaliates against melee attackers with Poison II + Nausea,
        // and leaves a trail of mushrooms underfoot!
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 40;
        long now = level.getGameTime();

        caster.removeEffect(MobEffects.POISON);
        caster.removeEffect(MobEffects.NAUSEA);

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_SPORE_MANTLE_UNTIL, now + dur);
        data.putInt("SpellWardTier", tier);

        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.72F, null);
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, caster.position().add(0.0D, 0.04D, 0.0D), 2.5D, EssenceType.SPORE, EssenceType.AIR, 48, tier
        );
        emitDualToneSporeBurst(level, caster.position().add(0.0D, 0.80D, 0.0D), 22, 0.48D, 0.50D);
        level.playSound(null, caster.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.90F, 1.02F);
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
        // Section 4.6 & 6.19 Silencing Mycelium Web:
        // - Surface-snapped disarmable trap (1.0s arming, Single or Repeating mode, 50% Sneak-Right-Click disarm refund)
        // - Triggers when an enemy steps on it, trapping them in sticky hyphal fungal tendrils for 4 seconds (80t),
        //   completely immobilizing them and silencing their abilities and spellcasting!
        int tier = spell.getEffectiveTier();
        double webRad = Math.max(3.5D, radius);
        int maxWaitTicks = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        BlockPos surfaceFloor = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snappedCenter = new Vec3(surfaceFloor.getX() + 0.5D, surfaceFloor.getY() + 1.02D, surfaceFloor.getZ() + 0.5D);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxCharges = repeating ? 3 : 1;
        int[] chargesUsed = {0};
        int[] rearmAtElapsed = {20}; // 1.0s arming delay

        SpellWorldVfxManager.broadcastArcaneSeal(level, snappedCenter, webRad, EssenceType.SPORE, EssenceType.NATURE, 60, tier);
        level.playSound(null, surfaceFloor.above(), SoundEvents.FUNGUS_PLACE, SoundSource.PLAYERS, 0.85F, 0.92F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, webRad, maxWaitTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 24 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NATURE_GREEN_HEX, 24, 0.80F, 0.06F),
                                zone.center.x, zone.center.y + 0.08D, zone.center.z, 3, webRad * 0.40D, 0.05D, webRad * 0.40D, 0.01D
                        );
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), BUFF_AIR_GOLD_HEX, 24, 0.78F, 0.06F),
                                zone.center.x, zone.center.y + 0.10D, zone.center.z, 2, webRad * 0.40D, 0.05D, webRad * 0.40D, 0.01D
                        );
                    }
                    if (elapsed < rearmAtElapsed[0]) return true;

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - webRad, zone.center.y - 1.0D, zone.center.z - webRad,
                            zone.center.x + webRad, zone.center.y + 2.2D, zone.center.z + webRad
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive()
                                    && e != owner
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    if (stepped.isEmpty()) return true;

                    float chargeScale = chargesUsed[0] == 0 ? 1.0F : 0.75F;
                    long trigNow = srv.getGameTime();
                    long webUntil = trigNow + 80L; // Full 4.0 seconds!

                    SpellWorldVfxManager.broadcastArcaneSeal(srv, zone.center, webRad * 1.12D, EssenceType.SPORE, EssenceType.NATURE, 84, tier);
                    emitDualToneSporeBurst(srv, zone.center.add(0.0D, 0.45D, 0.0D), 28, webRad * 0.48D, 0.45D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.PLAYERS, 0.95F, 0.78F);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.SPORE_BLOSSOM_BREAK, SoundSource.PLAYERS, 0.90F, 0.88F);

                    for (LivingEntity v : stepped) {
                        CompoundTag vData = EntityHelper.getPersistentData(v);
                        vData.putLong(SpellLingeringZoneManager.TAG_SPORE_HYPHAL_WEB_UNTIL, webUntil);
                        vData.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, webUntil);
                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
                        v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                        SpellLingeringZoneManager.applyArcaneSilence(v, 80);

                        if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm() && zone.slot != null && !zone.slot.hasHarmVerb()) {
                            float webDmg = (2.6F + tier * 0.75F) * zone.apparatusPotency * chargeScale;
                            SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(srv, v, owner, webDmg, false, true);
                        }
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * chargeScale
                            );
                        }
                    }

                    // Sustain the sticky 4-second hyphal web zone so anything inside stays immobilized & silenced for the full 4s!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, webRad, 80, trigNow, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (wsrv, wzone, welapsed) -> {
                                if (welapsed > wzone.maxDurationTicks) return false;
                                if (welapsed % 10 == 0) {
                                    emitDualToneSporeBurst(wsrv, wzone.center.add(0.0D, 0.25D, 0.0D), 8, webRad * 0.42D, 0.25D);
                                    LivingEntity wOwner = wzone.resolveCaster(wsrv);
                                    long wNow = wsrv.getGameTime();
                                    for (LivingEntity v : wsrv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> e.isAlive() && e != wOwner)) {
                                        if (wOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, wOwner, wardSafe)) continue;
                                        EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_SPORE_HYPHAL_WEB_UNTIL, wNow + 18L);
                                        EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, wNow + 18L);
                                        SpellLingeringZoneManager.applyArcaneSilence(v, 20);
                                    }
                                }
                                return true;
                            }
                    ));

                    chargesUsed[0]++;
                    if (chargesUsed[0] >= maxCharges) {
                        return false;
                    }
                    rearmAtElapsed[0] = elapsed + 100;
                    return true;
                }
        ).markDisarmableGlyph());
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
        // Section 6.19 Billowing Windborne Spore Blast:
        // Unleashes a wide 60-degree conical blast of fungal spores carried on wind currents;
        // deals Spore Materia magic damage, inflicts Poison II and Nausea for 5 seconds (100t),
        // carpets exposed fertile ground with mushrooms, and accelerates nearby crop growth!
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        boolean wardSafeguard = spell.hasWardFriendlyFireSafeguard();
        boolean dealDamage = !spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb();
        float blastDmg = (3.3F + tier * 0.92F) * apparatusPotency * spell.flawProfile().getPotencyMultiplier();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), normDir, coneRange, EssenceType.SPORE, EssenceType.AIR, tier
        );

        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafeguard)) continue;
            if (dealDamage) {
                SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(level, hit, caster, blastDmg, false, true);
            }
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(normDir.x * 1.08D, 0.30D, normDir.z * 1.08D));
            }
        }

        // Sweep full 60-degree cone across terrain: carpet exposed fertile ground in mushrooms & accelerate crop growth!
        Vec3 origin = caster.position().add(0.0D, 0.65D, 0.0D);
        int maxR = Math.min(11, Math.max(5, (int) Math.ceil(coneRange)));
        double maxRSq = maxR * maxR;
        BlockPos originBlock = BlockPos.containing(origin);
        for (BlockPos p : BlockPos.betweenClosed(originBlock.offset(-maxR, -2, -maxR), originBlock.offset(maxR, 3, maxR))) {
            Vec3 toBlock = Vec3.atCenterOf(p).subtract(origin);
            double distSq = toBlock.lengthSqr();
            if (distSq < 0.5D || distSq > maxRSq) continue;
            if (toBlock.normalize().dot(normDir) < 0.84D) continue;

            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof ScribedChalkBlock || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                continue;
            }
            if (bs.getBlock() instanceof BonemealableBlock grow
                    && !(bs.getBlock() instanceof MushroomBlock)
                    && grow.isValidBonemealTarget(level, p, bs)
                    && level.random.nextFloat() < 0.48F) {
                grow.performBonemeal(level, level.random, p.immutable(), bs);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 3, 0.25D, 0.25D, 0.25D, 0.02D);
            } else if (bs.isAir() && level.random.nextFloat() < 0.24F) {
                trySproutSingleFootstepMushroomPublic(level, p);
            }
        }

        level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.85F, 1.05F);
        level.playSound(null, caster.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.90F, 0.95F);
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
        // Section 6.19 Choking Fungal Fog Downpour:
        // Summons a persistent cave-ready choking fungal fog that blankets an area for 15 seconds (300t),
        // blinding and slowing non-arthropod mobs by an exact 50% while slowly decaying organic drops into compost!
        int tier = spell.getEffectiveTier();
        double fogRad = Math.max(7.0D, radius);
        int fogTicks = 300; // Full 15.0 seconds!

        BlockPos baseBlock = BlockPos.containing(impactCenter);
        double canopyH = 8.4D;
        for (int dy = 4; dy <= 10; dy++) {
            BlockPos check = baseBlock.above(dy);
            if (!level.getBlockState(check).getCollisionShape(level, check).isEmpty()) {
                canopyH = Math.max(3.5D, dy - 0.55D);
                break;
            }
        }
        final double finalCanopyH = canopyH;

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, fogRad, EssenceType.SPORE, EssenceType.AIR, 68, tier);
        level.playSound(null, baseBlock, SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 1.0F, 0.78F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, fogRad, fogTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;

                    if (elapsed > 0 && elapsed % 48 == 0) {
                        SpellWorldVfxManager.broadcastSkyfallPillar(srv, zone.center, zone.radius, EssenceType.SPORE, EssenceType.AIR, 56, tier);
                    }
                    if (elapsed % 5 == 0) {
                        int rgb = (elapsed % 10 == 0) ? NATURE_GREEN_HEX : BUFF_AIR_GOLD_HEX;
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), rgb, 30, 1.05F, 0.08F),
                                zone.center.x, zone.center.y + Math.min(3.4D, finalCanopyH * 0.55D), zone.center.z,
                                18, zone.radius * 0.55D, 1.1D, zone.radius * 0.55D, 0.03D
                        );
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                                zone.center.x, zone.center.y + Math.min(3.2D, finalCanopyH * 0.50D), zone.center.z,
                                14, zone.radius * 0.55D, 1.1D, zone.radius * 0.55D, 0.03D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        boolean dealDamage = zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm()
                                && zone.slot != null && !zone.slot.hasHarmVerb();
                        float fogDmg = (1.35F + tier * 0.40F) * zone.apparatusPotency;
                        double rSq = (zone.radius + 0.5D) * (zone.radius + 0.5D);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + finalCanopyH, zone.center.z + zone.radius
                        );
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            double dx = v.getX() - zone.center.x;
                            double dz = v.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > rSq) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            if (!v.getType().is(EntityTypeTags.ARTHROPOD)) {
                                v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                                v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 2));
                                // Exact 50% movement speed reduction on non-arthropod mobs!
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.50D, 30L);
                            }
                            if (dealDamage) {
                                SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(srv, v, owner, fogDmg, false, true);
                            }
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.30F
                                );
                            }
                        }

                        // Slowly decay organic dropped items into bone meal compost!
                        if (elapsed > 0 && elapsed % 40 == 0) {
                            for (ItemEntity item : srv.getEntitiesOfClass(ItemEntity.class, area)) {
                                ItemStack st = item.getItem();
                                if (isCompostableOrganicItem(st)) {
                                    item.setItem(new ItemStack(Items.BONE_MEAL, st.getCount()));
                                    emitDualToneSporeBurst(srv, item.position().add(0.0D, 0.25D, 0.0D), 8, 0.22D, 0.22D);
                                    srv.playSound(null, item.blockPosition(), SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 0.55F, 1.08F);
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
    public void onSummonFormSpawned(
            ServerLevel level,
            LivingEntity caster,
            Entity summonEntity,
            Vec3 spawnPos,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.SPORE.name());
        tag.putInt("SpellEidolonTintRGB", SYMBIOTIC_FUNGAL_HEX_COLOR);
        emitDualToneSporeBurst(level, spawnPos.add(0.0D, 0.85D, 0.0D), 22, 0.45D, 0.55D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.85F, 1.0F);
    }

    // =========================================================================
    // PART 3 — CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.19 Hyphal Network Blink:
        // Instant teleportation between nearby mycelium or mushroom blocks within range,
        // traveling silently through the underground fungal network!
        EntityHelper.getPersistentData(caster).putLong(
                SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 100L
        );

        sproutMushroomsPublic(level, BlockPos.containing(departurePos), 1, false);
        sproutMushroomsPublic(level, BlockPos.containing(arrivalPos), 1, false);

        // Subterranean hyphal network spore filament connecting departure and arrival
        Vec3 span = arrivalPos.subtract(departurePos);
        int samples = Math.min(24, Math.max(6, (int) Math.ceil(span.length() * 1.4D)));
        for (int i = 0; i <= samples; i++) {
            double t = i / (double) samples;
            Vec3 pt = departurePos.add(span.scale(t)).add(0.0D, 0.15D, 0.0D);
            int rgb = (i % 2 == 0) ? NATURE_GREEN_HEX : BUFF_AIR_GOLD_HEX;
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), rgb, 22, 0.80F, 0.05F),
                    pt.x, pt.y, pt.z, 2, 0.14D, 0.08D, 0.14D, 0.01D
            );
        }
        emitDualToneSporeBurst(level, departurePos.add(0.0D, 0.4D, 0.0D), 14, 0.35D, 0.30D);
        emitDualToneSporeBurst(level, arrivalPos.add(0.0D, 0.4D, 0.0D), 16, 0.38D, 0.32D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.19 Nether Spore Transmutation:
        // Converts Overworld flora and mushrooms into Crimson and Warped fungal vegetation, and vice versa!
        if (state.getBlock() instanceof ScribedChalkBlock || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }

        BlockState next = resolveNetherSporeBlockSwap(level, pos, state);
        if (next == null) {
            return false;
        }
        // Ensure fungal sprouts have valid support beneath when converting in-world!
        if ((next.is(Blocks.CRIMSON_FUNGUS) || next.is(Blocks.WARPED_FUNGUS)
                || next.is(Blocks.CRIMSON_ROOTS) || next.is(Blocks.WARPED_ROOTS) || next.is(Blocks.NETHER_SPROUTS))
                && !next.canSurvive(level, pos)) {
            BlockPos belowPos = pos.below();
            BlockState below = level.getBlockState(belowPos);
            if ((below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT) || below.is(Blocks.PODZOL) || below.is(Blocks.MYCELIUM))
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, belowPos, below)) {
                level.setBlockAndUpdate(belowPos, next.is(Blocks.CRIMSON_FUNGUS) || next.is(Blocks.CRIMSON_ROOTS)
                        ? Blocks.CRIMSON_NYLIUM.defaultBlockState()
                        : Blocks.WARPED_NYLIUM.defaultBlockState());
            }
        } else if ((next.is(Blocks.RED_MUSHROOM) || next.is(Blocks.BROWN_MUSHROOM)
                || next.is(Blocks.SHORT_GRASS) || next.is(Blocks.FERN))
                && !next.canSurvive(level, pos)) {
            BlockPos belowPos = pos.below();
            BlockState below = level.getBlockState(belowPos);
            if ((below.is(Blocks.CRIMSON_NYLIUM) || below.is(Blocks.WARPED_NYLIUM) || below.is(Blocks.NETHERRACK))
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, belowPos, below)) {
                level.setBlockAndUpdate(belowPos, (next.is(Blocks.RED_MUSHROOM) || next.is(Blocks.BROWN_MUSHROOM))
                        ? Blocks.MYCELIUM.defaultBlockState()
                        : Blocks.PODZOL.defaultBlockState());
            }
        }

        level.setBlockAndUpdate(pos, next);
        emitDualToneSporeBurst(level, Vec3.atCenterOf(pos), 10, 0.32D, 0.32D);
        level.playSound(null, pos, SoundEvents.FUNGUS_PLACE, SoundSource.BLOCKS, 0.70F, 1.05F);
        return true;
    }

    @Nullable
    private static BlockState resolveNetherSporeBlockSwap(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.is(Blocks.RED_MUSHROOM)) return Blocks.CRIMSON_FUNGUS.defaultBlockState();
        if (state.is(Blocks.CRIMSON_FUNGUS)) return Blocks.RED_MUSHROOM.defaultBlockState();
        if (state.is(Blocks.BROWN_MUSHROOM)) return Blocks.WARPED_FUNGUS.defaultBlockState();
        if (state.is(Blocks.WARPED_FUNGUS)) return Blocks.BROWN_MUSHROOM.defaultBlockState();

        if (state.is(Blocks.RED_MUSHROOM_BLOCK)) return Blocks.NETHER_WART_BLOCK.defaultBlockState();
        if (state.is(Blocks.NETHER_WART_BLOCK)) return Blocks.RED_MUSHROOM_BLOCK.defaultBlockState();
        if (state.is(Blocks.BROWN_MUSHROOM_BLOCK)) return Blocks.WARPED_WART_BLOCK.defaultBlockState();
        if (state.is(Blocks.WARPED_WART_BLOCK)) return Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState();

        if (state.is(Blocks.MUSHROOM_STEM)) {
            Block stem = level.random.nextBoolean() ? Blocks.CRIMSON_STEM : Blocks.WARPED_STEM;
            return stem.defaultBlockState();
        }
        if (state.is(Blocks.CRIMSON_STEM) || state.is(Blocks.WARPED_STEM)) {
            return Blocks.MUSHROOM_STEM.defaultBlockState();
        }
        if (state.is(BlockTags.OVERWORLD_NATURAL_LOGS)) {
            Direction.Axis axis = state.hasProperty(RotatedPillarBlock.AXIS)
                    ? state.getValue(RotatedPillarBlock.AXIS)
                    : Direction.Axis.Y;
            Block stem = level.random.nextBoolean() ? Blocks.CRIMSON_STEM : Blocks.WARPED_STEM;
            return stem.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        }
        if (state.is(Blocks.MYCELIUM) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL) || state.is(Blocks.MOSS_BLOCK)) {
            return (level.random.nextBoolean() ? Blocks.CRIMSON_NYLIUM : Blocks.WARPED_NYLIUM).defaultBlockState();
        }
        if (state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.WARPED_NYLIUM)) {
            return Blocks.MYCELIUM.defaultBlockState();
        }
        if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN) || state.is(BlockTags.SMALL_FLOWERS)) {
            Block netherFlora = switch (level.random.nextInt(3)) {
                case 0 -> Blocks.CRIMSON_ROOTS;
                case 1 -> Blocks.WARPED_ROOTS;
                default -> Blocks.NETHER_SPROUTS;
            };
            return netherFlora.defaultBlockState();
        }
        if (state.is(Blocks.CRIMSON_ROOTS) || state.is(Blocks.WARPED_ROOTS) || state.is(Blocks.NETHER_SPROUTS)) {
            return (level.random.nextBoolean() ? Blocks.SHORT_GRASS : Blocks.FERN).defaultBlockState();
        }
        if (state.is(Blocks.SPORE_BLOSSOM)) return Blocks.SHROOMLIGHT.defaultBlockState();
        if (state.is(Blocks.SHROOMLIGHT)) return Blocks.SPORE_BLOSSOM.defaultBlockState();
        return null;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        // Section 5.5 & 6.19 Nether Spore Transmutation on dropped item stacks (receives up to 30% bulk stack fuel discount automatically in SpellExecutionEngine!)
        ItemStack in = itemEntity.getItem();
        ItemStack out = resolveNetherSporeItemSwap(level, in);
        if (out.isEmpty()) {
            return false;
        }
        itemEntity.setItem(out);
        emitDualToneSporeBurst(level, itemEntity.position().add(0.0D, 0.25D, 0.0D), 8, 0.22D, 0.22D);
        return true;
    }

    private static ItemStack resolveNetherSporeItemSwap(ServerLevel level, ItemStack in) {
        int count = in.getCount();
        if (in.is(Items.RED_MUSHROOM)) return new ItemStack(Items.CRIMSON_FUNGUS, count);
        if (in.is(Items.CRIMSON_FUNGUS)) return new ItemStack(Items.RED_MUSHROOM, count);
        if (in.is(Items.BROWN_MUSHROOM)) return new ItemStack(Items.WARPED_FUNGUS, count);
        if (in.is(Items.WARPED_FUNGUS)) return new ItemStack(Items.BROWN_MUSHROOM, count);
        if (in.is(Items.RED_MUSHROOM_BLOCK)) return new ItemStack(Items.NETHER_WART_BLOCK, count);
        if (in.is(Items.NETHER_WART_BLOCK)) return new ItemStack(Items.RED_MUSHROOM_BLOCK, count);
        if (in.is(Items.BROWN_MUSHROOM_BLOCK)) return new ItemStack(Items.WARPED_WART_BLOCK, count);
        if (in.is(Items.WARPED_WART_BLOCK)) return new ItemStack(Items.BROWN_MUSHROOM_BLOCK, count);
        if (in.is(Items.MUSHROOM_STEM)) {
            return new ItemStack(level.random.nextBoolean() ? Items.CRIMSON_STEM : Items.WARPED_STEM, count);
        }
        if (in.is(Items.CRIMSON_STEM) || in.is(Items.WARPED_STEM)) return new ItemStack(Items.MUSHROOM_STEM, count);
        if (in.is(Items.MYCELIUM) || in.is(Items.PODZOL) || in.is(Items.MOSS_BLOCK)) {
            return new ItemStack(level.random.nextBoolean() ? Items.CRIMSON_NYLIUM : Items.WARPED_NYLIUM, count);
        }
        if (in.is(Items.CRIMSON_NYLIUM) || in.is(Items.WARPED_NYLIUM)) return new ItemStack(Items.MYCELIUM, count);
        if (in.is(Items.SHORT_GRASS) || in.is(Items.FERN) || in.is(ItemTags.SMALL_FLOWERS)) {
            return new ItemStack(level.random.nextBoolean() ? Items.CRIMSON_ROOTS : Items.WARPED_ROOTS, count);
        }
        if (in.is(Items.CRIMSON_ROOTS) || in.is(Items.WARPED_ROOTS) || in.is(Items.NETHER_SPROUTS)) {
            return new ItemStack(Items.SHORT_GRASS, count);
        }
        if (in.is(Items.VINE)) {
            return new ItemStack(level.random.nextBoolean() ? Items.WEEPING_VINES : Items.TWISTING_VINES, count);
        }
        if (in.is(Items.WEEPING_VINES) || in.is(Items.TWISTING_VINES)) return new ItemStack(Items.VINE, count);
        if (in.is(Items.SPORE_BLOSSOM)) return new ItemStack(Items.SHROOMLIGHT, count);
        if (in.is(Items.SHROOMLIGHT)) return new ItemStack(Items.SPORE_BLOSSOM, count);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.19 Hallucinatory Mycelial Hex (milk-immune):
        // Continuously confuses victim's allegiance so they violently attack the nearest entity (ally or foe),
        // inflicts permanent Nausea, and periodically coughs up toxic poison spores that harm adjacent allies!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0, true, true));

        LivingEntity caster = resolveHexCaster(level, target);
        confuseAllegianceAndCoughSporesPublic(level, target, caster, potencyTier);

        float rotDmg = (1.25F + potencyTier * 0.42F) * mult;
        SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(level, target, caster, rotDmg, false, true);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            Vec3 direction,
            SpellCoreEffect effect,
            int potencyTier,
            SpellAssembly spell
    ) {
        if (effect == SpellCoreEffect.RESTORATION) {
            // Section 6.19 Spore Carapace Restoration:
            // Coats the recipient in a hardened chitinous fungal shell, granting temporary absorption hearts
            // and resistance against incoming physical blows!
            boolean ownedSummon = target instanceof SpellEidolonEntity eid && caster != null && eid.isOwnedBy(caster);
            boolean allied = target == caster || ownedSummon || (caster != null && target.isAlliedTo(caster));
            if (!allied && target.getType().is(EntityTypeTags.UNDEAD)) {
                return;
            }

            int dur = 200 + potencyTier * 30;
            float abs = (6.0F + potencyTier * 2.0F) * spell.flawProfile().getPotencyMultiplier();
            target.setAbsorptionAmount(Math.max(target.getAbsorptionAmount(), abs));
            target.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, dur, potencyTier >= 4 ? 1 : 0));

            // Hardened chitinous fungal shell armor & toughness reinforcement + 3D carapace sync!
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(
                    target, level, 4.0D + potencyTier, 2.0D + potencyTier * 0.5D, 0.20D, 0.0D, dur
            );
            CompoundTag tData = EntityHelper.getPersistentData(target);
            tData.putLong(SpellLingeringZoneManager.TAG_SPORE_CARAPACE_UNTIL, level.getGameTime() + dur);
            tData.putInt("SpellWardTier", potencyTier);
            SpellCombatReactionEngine.syncEntityBarrierState(level, target, 0.80F, null);

            emitDualToneSporeBurst(level, target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D), 16, 0.42D, 0.45D);
            level.playSound(null, target.blockPosition(), SoundEvents.ARMOR_EQUIP_TURTLE.value(), SoundSource.PLAYERS, 0.90F, 1.08F);
        }
    }

    // =========================================================================
    // SHARED ENGINE HELPERS
    // =========================================================================

    public static void confuseAllegianceAndCoughSporesPublic(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            int potencyTier
    ) {
        if (target instanceof Mob mob) {
            List<LivingEntity> nearby = level.getEntitiesOfClass(
                    LivingEntity.class,
                    target.getBoundingBox().inflate(12.0D),
                    e -> e != target && e.isAlive() && !e.isSpectator()
                            && !(e instanceof Player pl && pl.isCreative())
            );
            if (!nearby.isEmpty()) {
                nearby.sort(Comparator.comparingDouble(target::distanceToSqr));
                LivingEntity nearest = nearby.getFirst();
                mob.setTarget(nearest);
                mob.setLastHurtByMob(nearest);
                mob.setAggressive(true);
                mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, nearest);
                mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, nearest.getUUID(), 100L);
            }
        }

        // Periodically cough toxic poison spores onto adjacent entities (harming the host's nearby allies!)
        float coughDmg = 1.15F + potencyTier * 0.35F;
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5D))) {
            if (adj == target || !adj.isAlive()) continue;
            if (caster != null && (adj == caster || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, true))) {
                continue;
            }
            adj.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
            SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(level, adj, caster, coughDmg, false, true);
        }
        emitDualToneSporeBurst(level, target.position().add(0.0D, target.getBbHeight() * 0.72D, 0.0D), 10, 0.40D, 0.35D);
    }

    public static void detonateInfestedCorpseSporePopPublic(ServerLevel level, LivingEntity deceased, long now) {
        Vec3 center = deceased.position().add(0.0D, deceased.getBbHeight() * 0.5D, 0.0D);
        CompoundTag dData = EntityHelper.getPersistentData(deceased);
        LivingEntity caster = null;
        String casterUuidStr = dData.getString(TAG_INFESTED_CASTER).orElse("");
        if (!casterUuidStr.isEmpty()) {
            try {
                Entity found = level.getEntity(UUID.fromString(casterUuidStr));
                if (found instanceof LivingEntity liv) caster = liv;
            } catch (IllegalArgumentException ignored) {
            }
        }

        SpellWorldVfxManager.broadcastRadialNova(level, deceased.position().add(0.0D, 0.08D, 0.0D), 3.6D, EssenceType.SPORE, EssenceType.AIR, 2);
        emitDualToneSporeBurst(level, center, 28, 0.95D, 0.55D);
        level.playSound(null, deceased.blockPosition(), SoundEvents.SPORE_BLOSSOM_BREAK, SoundSource.PLAYERS, 0.95F, 0.92F);
        level.playSound(null, deceased.blockPosition(), SoundEvents.FUNGUS_BREAK, SoundSource.PLAYERS, 0.85F, 1.05F);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, deceased.getBoundingBox().inflate(4.0D))) {
            if (foe == deceased || !foe.isAlive()) continue;
            if (caster != null) {
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            } else if (!(foe instanceof Monster)) {
                continue;
            }
            // Chain-infect adjacent enemies with Poison II, Nausea, -40% speed & infestation tag!
            CompoundTag fData = EntityHelper.getPersistentData(foe);
            fData.putLong(SpellLingeringZoneManager.TAG_SPORE_INFESTED_UNTIL, now + 100L);
            if (!casterUuidStr.isEmpty()) {
                fData.putString(TAG_INFESTED_CASTER, casterUuidStr);
            }
            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, level, 0.40D, 100L);
            SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(level, foe, caster, 2.8F, false, true);
        }

        sproutMushroomsPublic(level, deceased.blockPosition(), 2, false);
    }

    public static void sproutMushroomsPublic(ServerLevel level, BlockPos center, int r, boolean convertTurf) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof ScribedChalkBlock || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                continue;
            }
            if (convertTurf
                    && (bs.is(Blocks.DIRT) || bs.is(Blocks.GRASS_BLOCK) || bs.is(Blocks.COARSE_DIRT) || bs.is(Blocks.PODZOL))
                    && !(level.getBlockState(p.above()).getBlock() instanceof ScribedChalkBlock)
                    && level.random.nextFloat() < 0.42F) {
                level.setBlockAndUpdate(p, Blocks.MYCELIUM.defaultBlockState());
                if (level.getBlockState(p.above()).isAir() && level.random.nextFloat() < 0.36F) {
                    Block shroom = level.random.nextBoolean() ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM;
                    level.setBlockAndUpdate(p.above(), shroom.defaultBlockState());
                }
            } else if (bs.isAir() && level.random.nextFloat() < 0.28F) {
                trySproutSingleFootstepMushroomPublic(level, p);
            }
        }
    }

    public static boolean trySproutSingleFootstepMushroomPublic(ServerLevel level, BlockPos airPos) {
        BlockState cur = level.getBlockState(airPos);
        if (!cur.isAir()) return false;

        BlockPos soilPos = airPos.below();
        BlockState soil = level.getBlockState(soilPos);
        if (soil.getBlock() instanceof ScribedChalkBlock
                || soil.getBlock() instanceof FarmBlock
                || SpellExecutionEngine.isProtectedBlockOrContainer(level, soilPos, soil)) {
            return false;
        }
        boolean fertile = soil.is(Blocks.MYCELIUM)
                || soil.is(Blocks.PODZOL)
                || soil.is(Blocks.GRASS_BLOCK)
                || soil.is(Blocks.DIRT)
                || soil.is(Blocks.COARSE_DIRT)
                || soil.is(Blocks.ROOTED_DIRT)
                || soil.is(Blocks.MOSS_BLOCK);
        if (!fertile) {
            return false;
        }

        Block shroom = level.random.nextBoolean() ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM;
        BlockState shroomState = shroom.defaultBlockState();
        // In sunlit Overworld biomes (raw brightness >= 13), vanilla mushrooms require Mycelium or Podzol underfoot to survive!
        if (!shroomState.canSurvive(level, airPos)) {
            level.setBlockAndUpdate(soilPos, Blocks.PODZOL.defaultBlockState());
        }
        if (shroomState.canSurvive(level, airPos)) {
            level.setBlockAndUpdate(airPos, shroomState);
            return true;
        }
        return false;
    }

    private static void emitDualToneSporeBurst(ServerLevel level, Vec3 pos, int totalCount, double spreadXZ, double spreadY) {
        int greenCount = Math.max(1, totalCount / 2);
        int goldCount = Math.max(1, totalCount - greenCount);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NATURE_GREEN_HEX, 28, 0.95F, 0.08F),
                pos.x, pos.y, pos.z, greenCount, spreadXZ, spreadY, spreadXZ, 0.03D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), BUFF_AIR_GOLD_HEX, 28, 0.90F, 0.08F),
                pos.x, pos.y, pos.z, goldCount, spreadXZ, spreadY, spreadXZ, 0.03D
        );
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                pos.x, pos.y, pos.z, Math.max(4, totalCount / 2), spreadXZ, spreadY, spreadXZ, 0.04D);
    }

    private static boolean isCompostableOrganicItem(ItemStack stack) {
        if (stack.isEmpty() || stack.is(Items.BONE_MEAL)) return false;
        if (stack.isEnchanted() || stack.has(DataComponents.CUSTOM_NAME)) return false;
        if (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE) || stack.is(Items.GOLDEN_CARROT)) {
            return false;
        }
        if (ComposterBlock.COMPOSTABLES.containsKey(stack.getItem())) {
            return true;
        }
        return stack.is(ItemTags.LEAVES)
                || stack.is(ItemTags.SAPLINGS)
                || stack.is(ItemTags.SMALL_FLOWERS)
                || stack.is(Items.ROTTEN_FLESH)
                || stack.is(Items.SPIDER_EYE)
                || stack.is(Items.POISONOUS_POTATO)
                || stack.has(DataComponents.FOOD);
    }

    @Nullable
    private static LivingEntity resolveHexCaster(ServerLevel level, LivingEntity target) {
        String uuidStr = EntityHelper.getPersistentData(target).getString("SpellHexCasterUUID").orElse("");
        if (!uuidStr.isEmpty()) {
            try {
                Entity found = level.getEntity(UUID.fromString(uuidStr));
                if (found instanceof LivingEntity liv) return liv;
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos approx) {
        for (int dy = 2; dy >= -4; dy--) {
            BlockPos check = approx.offset(0, dy, 0);
            BlockState floor = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());
            if (!floor.getCollisionShape(level, check).isEmpty() && above.getCollisionShape(level, check.above()).isEmpty()) {
                return check;
            }
        }
        return approx.below();
    }
}
