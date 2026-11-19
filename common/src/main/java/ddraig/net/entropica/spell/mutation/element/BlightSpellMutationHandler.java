package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModBlocks;
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
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Blight Materia (Ethereal &amp; Paradox Fusion #26)</b>
 * (Spellcrafting System Guide Section 6.27 — Dual Undead + Nature [Leans: Undead]):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Dual Undead + Nature harmonic matrix [Leans: Undead] — Strong against
 *     <b>FROZEN (+1)</b> (Dominance {@code blight_nature_dom}: Strong vs Water &amp; Earth) | Weak against
 *     <b>RADIANT (-1)</b>, <b>NETHER (-1)</b>, <b>FROZEN (-1)</b>, and <b>UMBRAL (-1)</b>, computed dynamically
 *     at runtime by {@code VisInteractionManager}.</li>
 *     <li><b>Flow (Chains)</b>: <i>Cadaverous Contagion Chains Flow</i> — thorny bone vines leaping between
 *     targets; each leap roots the victim for 1.5s, shreds 1 armor point, and if any chained enemy dies
 *     within 3.0s, immediately triggers a secondary leap to 2 additional nearby hostiles.</li>
 *     <li><b>Projectile</b>: <i>Contagious Necro-Spore Pod</i> — pulsating pod of rotting vines &amp; necrotic
 *     mold ({@code #4D6453} / {@code #34CC48}) rooting the target for 2.5s, dealing Blight Materia damage,
 *     and bursting into a 3-block Poison II cloud that spreads if the target dies while rooted.</li>
 *     <li><b>Beam</b>: <i>Necrotic Bramble Tether Beam</i> — pulls enemies toward the caster &amp; slows them
 *     by 50%; 3.0s continuous channeling triggers a <i>Blight Bloom</i> shredding 3 armor points and healing
 *     the caster for 20% of channeled damage.</li>
 *     <li><b>Radial Burst</b>: <i>Cadaverous Briar Nova</i> — 6m+ ring of rotting bone thorns &amp; toxic miasma
 *     spores rooting caught enemies for 2.5s, inflicting Poison II for 4.0s, and wilting nearby flowers into Wither Roses.</li>
 *     <li><b>Touch / Strike</b>: <i>Necrotic Thorn Gouge</i> — deals Blight Materia damage, inflicts Poison II
 *     &amp; 40% Slowness for 4.0s, shreds 2 armor points, and makes the victim's corpse burst into a 3.0s root snare on death.</li>
 *     <li><b>Aura / Self</b>: <i>Cadaverous Briar-Carapace</i> — grants +6 Armor, Poison &amp; Hunger immunity,
 *     unprovoked vanilla undead neutrality, and 30% damage + 3.0s Poison II melee retaliation.</li>
 *     <li><b>Glyph / Trap</b>: <i>Strangling Rot-Briar Snare</i> — disarmable Single/Repeating 4m vine cage
 *     rooting trapped enemies for 3.5s, dealing Blight Materia damage, shredding 3 armor points, and planting
 *     Wither Roses if an entity dies inside.</li>
 *     <li><b>Cone / Spray</b>: <i>Virulent Necro-Spore Flurry</i> — 60° cone knocking enemies back 3 blocks,
 *     inflicting Poison II &amp; 40% Slowness for 4.0s, shredding 2 armor points, stripping wood logs into
 *     decayed variants, and wilting flowers/grass into dead bushes.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Cadaverous Rot Tempest</i> — cave-safe 14m+ weeping aerial blight-rift for
 *     10.0s dealing Blight Materia damage, inflicting Poison II &amp; 50% Slowness, decaying dropped raw meat
 *     into rotten flesh, and sprouting grasping ground thorns that repeatedly root enemies.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Rot-Warden Eidolon Suite</i> — {@code #4D6453} / {@code #34CC48}
 *     Blight-Revenant Agent (Poison II &amp; armor-shredding briar claws), Necro-Briar Spire Sentry (8m 30%
 *     slow aura + piercing skeletal thorn darts), and Cadaverous Effigy Decoy (5m 3.0s-rooting Cadaverous
 *     Briar Nova &amp; Wither Roses upon defeat).</li>
 *     <li><b>Blink / Step</b>: <i>Cadaverous Root-Step</i> — teleports up to 14+ blocks, leaving a necrotic
 *     bone-thorn cage at departure rooting pursuers for 3.0s and poisoning them.</li>
 *     <li><b>Transmute</b>: <i>Necrotic Decomposition Transmute</i> — rots foliage/wood into dead/blight variants,
 *     turns grass into coarse dirt or podzol, and decomposes rotten flesh &amp; bones into bone meal with +25% bonus yields.</li>
 *     <li><b>Affliction</b>: <i>Virulent Wither-Blight Hex</i> — milk-immune rotting curse dealing continuous
 *     Blight Materia damage, disabling all health regeneration, shredding 40% armor toughness, spreading on
 *     victim death, and triggering <i>Virulent Blood-Tasting</i> on cursed players.</li>
 * </ul>
 */
public final class BlightSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int TOMBSTONE_BLIGHT_HEX_COLOR = 0x4D6453;
    public static final int NECROTIC_GREEN_HEX_COLOR = 0x34CC48;
    public static final int ROT_WARDEN_HEX_COLOR = 0x4D6453;

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
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Cadaverous Root-Step";
            case TRANSMUTE, BREAK -> "Necrotic Decomposition Transmute";
            case AFFLICTION -> "Virulent Wither-Blight Hex";
            case RESTORATION -> "Symbiotic Rot Purge";
            default -> "Cadaverous Blight " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // FLOW MUTATION — CADAVEROUS CONTAGION CHAINS FLOW
    // =========================================================================

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromTarget,
            LivingEntity toTarget,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.27 Cadaverous Contagion Chains Flow:
        // Leaping threads of thorny bone vines jump between consecutive targets; each leap roots the victim
        // for 1.5s (30t), shreds 1 armor point, and if any chained enemy dies within 3.0s (60t), immediately
        // triggers a secondary leap to 2 additional nearby hostiles!
        int tier = spell.getEffectiveTier();
        Vec3 fromEye = fromTarget.position().add(0.0D, fromTarget.getBbHeight() * 0.58D, 0.0D);
        Vec3 toEye = toTarget.position().add(0.0D, toTarget.getBbHeight() * 0.58D, 0.0D);
        SpellWorldVfxManager.broadcastChainArc(level, fromEye, toEye, EssenceType.BLIGHT, null, tier);

        // Stamp 3.0s (60t) secondary death-contagion mark BEFORE damage so instant kills still leap!
        EntityHelper.getPersistentData(toTarget).putLong(
                SpellLingeringZoneManager.TAG_BLIGHT_CHAIN_MARK_UNTIL, level.getGameTime() + 60L
        );
        applyBlightRoot(level, toTarget, 30);
        SpellCombatReactionEngine.applyTimedAttributeShred(toTarget, level, 1.0D, 0.0D, 100L);

        if (!slot.hasHarmVerb()) {
            float leapDmg = (2.3F + tier * 0.68F) * spell.flawProfile().getPotencyMultiplier();
            SpellCombatReactionEngine.hurtWithBlightMateriaAdvantage(level, toTarget, caster, leapDmg, false, true);
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NECROTIC_GREEN_HEX_COLOR, 18, 0.90F, 0.04F),
                toTarget.getX(), toTarget.getY(0.8D), toTarget.getZ(), 8, 0.25D, 0.30D, 0.25D, 0.02D
        );
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NECROTIC_GREEN_HEX_COLOR, 16, 0.86F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D
        );
    }

    // =========================================================================
    // FORM MUTATION 1 — CONTAGIOUS NECRO-SPORE POD (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        boolean heavyArc = spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY && spell.flowPart().mutationEnabled();
        projectile.enableSlightFluidArc(heavyArc);
        level.playSound(null, caster.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.90F, 0.78F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NECROTIC_GREEN_HEX_COLOR, 16, 0.88F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D
        );
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_ACID.get(), TOMBSTONE_BLIGHT_HEX_COLOR, 16, 0.85F, 0.04F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
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
        // Section 6.27 Contagious Necro-Spore Pod:
        // Roots the target in place for 2.5s (50t), deals Blight Materia damage, and bursts into a 3-block
        // toxic cloud inflicting Poison II and spreading to nearby enemies if the target dies while rooted!
        if (hitEntity instanceof LivingEntity victim && victim.isAlive() && victim != caster) {
            if (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, spell.hasWardFriendlyFireSafeguard())) {
                EntityHelper.getPersistentData(victim).putLong(
                        SpellLingeringZoneManager.TAG_BLIGHT_POD_CONTAGION_UNTIL, level.getGameTime() + 50L
                );
                applyBlightRoot(level, victim, 50);
                if (!slot.hasHarmVerb()) {
                    int tier = spell.getEffectiveTier();
                    float podDmg = (2.85F + tier * 0.84F) * spell.flawProfile().getPotencyMultiplier();
                    SpellCombatReactionEngine.hurtWithBlightMateriaAdvantage(level, victim, caster, podDmg, false, true);
                }
            }
        }
        spawnToxicNecroCloud(level, caster, projectile.position(), spell, slot);
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
        spawnToxicNecroCloud(level, caster, blockHit.getLocation(), spell, slot);
    }

    private static void spawnToxicNecroCloud(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 impactPos,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        int tier = spell.getEffectiveTier();
        BlockPos surf = findGroundSurfaceNear(level, BlockPos.containing(impactPos));
        Vec3 cloudCenter = new Vec3(impactPos.x, surf.above().getY() + 0.03D, impactPos.z);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, cloudCenter, 3.0D, EssenceType.BLIGHT, null, tier);

        AABB box = new AABB(
                cloudCenter.x - 3.0D, cloudCenter.y - 1.5D, cloudCenter.z - 3.0D,
                cloudCenter.x + 3.0D, cloudCenter.y + 2.2D, cloudCenter.z + 3.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, cloudCenter, 3.0D, 60, level.getGameTime(), spell, slot, EssenceType.BLIGHT, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NECROTIC_GREEN_HEX_COLOR, 18, 0.90F, 0.04F),
                                zone.center.x, zone.center.y + 0.45D, zone.center.z, 10, 1.35D, 0.35D, 1.35D, 0.02D
                        );
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB cBox = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 2.2D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, cBox, LivingEntity::isAlive)) {
                            if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                        }
                    }
                    return true;
                }
        ));
        level.playSound(null, BlockPos.containing(cloudCenter), SoundEvents.SPORE_BLOSSOM_BREAK, SoundSource.PLAYERS, 0.90F, 0.72F);
    }

    // =========================================================================
    // FORM MUTATION 2 — NECROTIC BRAMBLE TETHER BEAM (BEAM)
    // =========================================================================

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
        // Section 6.27 Necrotic Bramble Tether Beam:
        // Writhing tendril of dark thorns and rotting mold; pulls the enemy toward the caster and slows them
        // by 50%; channeling continuously for 3.0s (60 contact ticks = 15 x 4-tick pulses) triggers a
        // Blight Bloom that shreds 3 armor points and heals the caster for 20% of the channeled damage!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = spell.flawProfile().getPotencyMultiplier() * apparatusPotency;

        for (LivingEntity hit : hitEntities) {
            if (hit == caster || !hit.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            float pulseDealt = (1.40F + tier * 0.44F) * potency;
            if (!slot.hasHarmVerb()) {
                pulseDealt = SpellCombatReactionEngine.hurtWithBlightMateriaAdvantage(level, hit, caster, pulseDealt, false, true);
            }

            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                Vec3 toCaster = caster.position().subtract(hit.position());
                Vec3 pull = toCaster.lengthSqr() > 1.0E-4D ? toCaster.normalize().scale(0.42D) : Vec3.ZERO;
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(pull.x, 0.14D, pull.z));
            }
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.50D, 30L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2));

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("SpellBlightBeamLastTick").orElse(-100L);
            boolean contiguous = (now - last <= 16L);
            int contactTicks = contiguous ? data.getInt("SpellBlightBeamTicks").orElse(0) + 4 : 4;
            float accumDmg = (contiguous ? data.getFloat("SpellBlightBeamAccumDmg").orElse(0.0F) : 0.0F) + Math.max(0.5F, pulseDealt);
            data.putLong("SpellBlightBeamLastTick", now);

            if (contactTicks >= 60) {
                // 3.0s continuous channeling -> Blight Bloom (-3 armor + 20% of channeled damage healed)!
                data.putInt("SpellBlightBeamTicks", 0);
                data.putFloat("SpellBlightBeamAccumDmg", 0.0F);
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 160L);
                if (caster.isAlive()) {
                    float bloomHeal = Math.max(1.5F, accumDmg * 0.20F);
                    SpellCombatReactionEngine.notifyMagicalHeal(caster, bloomHeal);
                    caster.heal(bloomHeal);
                }
                SpellWorldVfxManager.broadcastRadialNova(level, hit.position().add(0.0D, 0.1D, 0.0D), 2.8D, EssenceType.BLIGHT, null, tier);
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NECROTIC_GREEN_HEX_COLOR, 22, 1.00F, 0.05F),
                        hit.getX(), hit.getY(0.8D), hit.getZ(), 18, 0.40D, 0.40D, 0.40D, 0.04D
                );
                level.playSound(null, hit.blockPosition(), SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 1.0F, 0.70F);
            } else {
                data.putInt("SpellBlightBeamTicks", contactTicks);
                data.putFloat("SpellBlightBeamAccumDmg", accumDmg);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — CADAVEROUS BRIAR NOVA (RADIAL BURST)
    // =========================================================================

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
        // Section 6.27 Cadaverous Briar Nova:
        // Explosive 360° ring of rotting bone thorns and toxic miasma spores (6-block+ radius);
        // roots all caught enemies in place for 2.5s (50t), inflicts Poison II for 4.0s (80t),
        // and wilts nearby flowers into Wither Roses!
        double r = Math.max(6.0D, radius);
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.BLIGHT, null, tier);

        AABB box = new AABB(
                center.x - r, center.y - 2.0D, center.z - r,
                center.x + r, center.y + 2.8D, center.z + r
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float novaDmg = (3.30F + tier * 0.90F) * spell.flawProfile().getPotencyMultiplier() * apparatusPotency;
                SpellCombatReactionEngine.hurtWithBlightMateriaAdvantage(level, foe, caster, novaDmg, false, true);
            }
            applyBlightRoot(level, foe, 50);
            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
        }

        BlockPos cPos = BlockPos.containing(center);
        int rScan = (int) Math.ceil(r);
        double rSq = r * r;
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-rScan, -2, -rScan), cPos.offset(rScan, 2, rScan))) {
            double dx = (p.getX() + 0.5D) - center.x;
            double dz = (p.getZ() + 0.5D) - center.z;
            if (dx * dx + dz * dz > rSq) continue;
            wiltFlowerIntoWitherRose(level, p, level.getBlockState(p));
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NECROTIC_GREEN_HEX_COLOR, 26, 1.10F, 0.05F),
                center.x, center.y + 0.7D, center.z, 34, 2.3D, 0.5D, 2.3D, 0.04D
        );
        level.playSound(null, cPos, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 1.0F, 0.68F);
    }

    // =========================================================================
    // FORM MUTATION 4 — NECROTIC THORN GOUGE (TOUCH / STRIKE)
    // =========================================================================

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
        // Section 6.27 Necrotic Thorn Gouge:
        // Strikes with festering briar thorns; deals Blight Materia damage, inflicts Poison II and
        // 40% Slowness for 4.0s (80t), shreds 2 armor points, and causes the victim's corpse on death
        // to burst into a root snare that traps adjacent enemies for 3.0s (60t)!
        int tier = spell.getEffectiveTier();
        long snareMark = level.getGameTime() + 100L;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.22D, 0.0D), direction, 3.3D, EssenceType.BLIGHT, null, tier
        );

        for (LivingEntity hit : hitEntities) {
            if (hit == caster || !hit.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            // Stamp corpse root-snare marker BEFORE damage so instant one-shot kills still trigger!
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_BLIGHT_CORPSE_SNARE_UNTIL, snareMark);
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.40D, 80L);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 100L);
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));

            if (!slot.hasHarmVerb()) {
                float gougeDmg = (3.40F + tier * 0.94F) * spell.flawProfile().getPotencyMultiplier() * apparatusPotency;
                SpellCombatReactionEngine.hurtWithBlightMateriaAdvantage(level, hit, caster, gougeDmg, true, true);
            }

            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NECROTIC_GREEN_HEX_COLOR, 18, 0.95F, 0.04F),
                    hit.getX(), hit.getY(0.8D), hit.getZ(), 10, 0.30D, 0.30D, 0.30D, 0.03D
            );
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.THORNS_HIT, SoundSource.PLAYERS, 0.95F, 0.78F);
    }

    // =========================================================================
    // FORM MUTATION 5 — CADAVEROUS BRIAR-CARAPACE (AURA / SELF)
    // =========================================================================

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
        // Section 6.27 Cadaverous Briar-Carapace:
        // Covers the caster in rotting bone-bark and thorny vines (#4D6453 / #34CC48); grants +6 Armor,
        // total immunity to Poison and Hunger, makes vanilla undead treat the caster as neutral unless
        // attacked first, and retaliates against melee attackers with 30% damage & Poison II for 3.0s!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_BLIGHT_CARAPACE_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 0.0D, 0.0D, 0.0D, dur);
        caster.removeEffect(MobEffects.POISON);
        caster.removeEffect(MobEffects.HUNGER);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.45F, null);

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NECROTIC_GREEN_HEX_COLOR, 24, 1.00F, 0.05F),
                caster.getX(), caster.getY(0.9D), caster.getZ(), 18, 0.45D, 0.50D, 0.45D, 0.03D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 0.90F, 0.78F);
    }

    // =========================================================================
    // FORM MUTATION 6 — STRANGLING ROT-BRIAR SNARE (GLYPH / TRAP)
    // =========================================================================

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
        // Section 6.27 Strangling Rot-Briar Snare:
        // Disarmable Single/Repeating floor glyph (1.0s arming delay) erupting into a 4-block cage of
        // grasping necrotic vines when stepped on; roots trapped enemies for 3.5s (70t), deals Blight
        // Materia damage, shreds 3 armor points, and leaves behind Wither Roses if an entity dies inside!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double snareRad = Math.max(4.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), snareRad, EssenceType.BLIGHT, null, Math.min(240, maxDuration), tier
        );
        level.playSound(null, surface.above(), SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 0.85F, 0.82F);

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, snareRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.BLIGHT, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.BLIGHT, null, rem, tier
                        );
                    }
                    if (elapsed < 20) return true;
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                            zone.center.x + 3.0D, zone.center.y + 2.2D, zone.center.z + 3.0D
                    );
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive() && e != owner && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)));
                    if (triggers.isEmpty()) return true;

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, zone.radius, EssenceType.BLIGHT, null, tier);
                    long roseMark = srv.getGameTime() + 120L;
                    AABB cageBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 2.6D, zone.center.z + zone.radius
                    );
                    for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, cageBox, LivingEntity::isAlive)) {
                        if (victim == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, victim)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, wardSafe)) continue;

                        // Stamp Wither Rose death-bloom tag BEFORE damage!
                        EntityHelper.getPersistentData(victim).putLong(SpellLingeringZoneManager.TAG_BLIGHT_WITHER_ROSE_SNARE_UNTIL, roseMark);
                        applyBlightRoot(srv, victim, 70);
                        SpellCombatReactionEngine.applyTimedAttributeShred(victim, srv, 3.0D, 0.0D, 140L);

                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, victim, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                            );
                        }
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            float snareDmg = (3.35F + tier * 0.88F) * Math.max(0.5F, zone.apparatusPotency);
                            SpellCombatReactionEngine.hurtWithBlightMateriaAdvantage(srv, victim, owner, snareDmg, false, true);
                        }
                    }

                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 1.0F, 0.68F);
                    triggersLeft[0]--;
                    rearmCooldown[0] = 35;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — VIRULENT NECRO-SPORE FLURRY (CONE / SPRAY)
    // =========================================================================

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
        // Section 6.27 Virulent Necro-Spore Flurry:
        // Wide 60° cone of rotting bone splinters and toxic spores; knocks enemies back 3 blocks,
        // inflicts Poison II and 40% Slowness for 4.0s (80t), shreds 2 armor points, strips bark off
        // wood logs into decayed variants, and wilts flowers and tall grass into dead bushes!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.20D, 0.0D), direction, coneRange, EssenceType.BLIGHT, null, tier
        );

        Vec3 flatDir = new Vec3(direction.x, 0.0D, direction.z);
        Vec3 pushDir = flatDir.lengthSqr() > 1.0E-4D ? flatDir.normalize() : new Vec3(0.0D, 0.0D, 1.0D);

        for (LivingEntity hit : hitTargets) {
            if (hit == caster || !hit.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float coneDmg = (2.85F + tier * 0.82F) * spell.flawProfile().getPotencyMultiplier() * apparatusPotency;
                SpellCombatReactionEngine.hurtWithBlightMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }

            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(pushDir.x * 0.88D, 0.25D, pushDir.z * 0.88D)); // 3-block knockback!
            }
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.40D, 80L);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 100L);
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
        }

        int steps = Math.min(9, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            int span = Math.max(2, (int) Math.ceil(s * 0.55D));
            for (BlockPos p : BlockPos.betweenClosed(stepPos.offset(-span, -1, -span), stepPos.offset(span, 2, span))) {
                BlockState bs = level.getBlockState(p);
                if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
                if (bs.is(BlockTags.LOGS)
                        && !bs.is(ModBlocks.STRIPPED_VOID_BLIGHT_MANGROVE_LOG.get())
                        && !bs.is(ModBlocks.STRIPPED_VOID_BLIGHT_MANGROVE_WOOD.get())) {
                    String path = BuiltInRegistries.BLOCK.getKey(bs.getBlock()).getPath();
                    Block decayed = (path.endsWith("wood") || path.endsWith("hyphae"))
                            ? ModBlocks.STRIPPED_VOID_BLIGHT_MANGROVE_WOOD.get()
                            : ModBlocks.STRIPPED_VOID_BLIGHT_MANGROVE_LOG.get();
                    BlockState next = decayed.defaultBlockState();
                    if (bs.hasProperty(RotatedPillarBlock.AXIS) && next.hasProperty(RotatedPillarBlock.AXIS)) {
                        next = next.setValue(RotatedPillarBlock.AXIS, bs.getValue(RotatedPillarBlock.AXIS));
                    }
                    level.setBlockAndUpdate(p, next);
                } else {
                    wiltFloraIntoDeadBush(level, p, bs);
                }
            }
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.AXE_STRIP, SoundSource.PLAYERS, 0.85F, 0.72F);
    }

    // =========================================================================
    // FORM MUTATION 8 — CADAVEROUS ROT TEMPEST (SKYFALL / RAIN)
    // =========================================================================

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
        // Section 6.27 Cadaverous Rot Tempest:
        // Opens a weeping aerial blight-rift for 10.0s (200t, 14-block+ radius) raining putrid fungal bile
        // and skeletal thorns; continuously deals Blight Materia damage, inflicts Poison II and 50% Slowness,
        // decays dropped raw meat into rotten flesh, and sprouts grasping ground thorns that repeatedly root enemies!
        int tier = spell.getEffectiveTier();
        double r = Math.max(14.0D, radius);
        BlockPos surf = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        Vec3 groundCenter = new Vec3(impactCenter.x, surf.above().getY() + 0.03D, impactCenter.z);
        double riftH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, surf.above(), tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, groundCenter, r, EssenceType.BLIGHT, null, 200, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, groundCenter, r, EssenceType.BLIGHT, null, 200, tier);
        level.playSound(null, BlockPos.containing(groundCenter), SoundEvents.SPORE_BLOSSOM_BREAK, SoundSource.PLAYERS, 1.1F, 0.65F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, groundCenter, r, 200, level.getGameTime(), spell, slot, EssenceType.BLIGHT, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NECROTIC_GREEN_HEX_COLOR, 20, 1.05F, 0.05F),
                                zone.center.x, zone.center.y + Math.min(5.0D, riftH - 0.5D), zone.center.z,
                                20, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.03D
                        );
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float potency = (zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F) * zone.apparatusPotency;

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + riftH + 0.5D, zone.center.z + zone.radius
                        );
                        for (ItemEntity item : srv.getEntitiesOfClass(ItemEntity.class, area, ItemEntity::isAlive)) {
                            if (isRawMeat(item.getItem())) {
                                item.setItem(new ItemStack(Items.ROTTEN_FLESH, item.getItem().getCount()));
                            }
                        }

                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            v.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                            v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 2));
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.50D, 35L);

                            // Grasping ground thorns repeatedly root enemies!
                            if (elapsed % 40 == 0) {
                                applyBlightRoot(srv, v, 30);
                            }

                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F
                                );
                            }
                            if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                float rotDmg = (1.90F + tier * 0.55F) * potency;
                                SpellCombatReactionEngine.hurtWithBlightMateriaAdvantage(srv, v, owner, rotDmg, false, true);
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — ROT-WARDEN EIDOLON SUITE (AGENT / SENTRY / DECOY)
    // =========================================================================

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
        int tier = spell.getEffectiveTier();
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.BLIGHT.name());
        tag.putInt("SpellEidolonTintRGB", ROT_WARDEN_HEX_COLOR);

        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.65D, radius * 0.35D), EssenceType.BLIGHT, null, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NECROTIC_GREEN_HEX_COLOR, 28, 1.05F, 0.05F),
                spawnPos.x, spawnPos.y + 0.85D, spawnPos.z, 20, 0.45D, 0.55D, 0.45D, 0.03D
        );
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 0.90F, 0.72F);
    }

    // =========================================================================
    // CORE EFFECT SYNERGIES — BLINK, TRANSMUTE, AFFLICTION & RESTORATION
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        // Section 6.27 Cadaverous Root-Step:
        // Teleports up to 14+ blocks away, leaving a cage of necrotic bone-thorns at departure
        // that roots pursuers for 3.0s (60t) and poisons them!
        BlockPos depSurf = findGroundSurfaceNear(level, BlockPos.containing(departurePos));
        Vec3 cagePos = new Vec3(departurePos.x, depSurf.above().getY() + 0.03D, departurePos.z);

        SpellWorldVfxManager.broadcastArcaneSeal(level, cagePos, 3.0D, EssenceType.BLIGHT, null, 60, potencyTier);
        level.playSound(null, BlockPos.containing(cagePos), SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 0.90F, 0.75F);

        AABB initialBox = new AABB(
                cagePos.x - 3.0D, cagePos.y - 1.0D, cagePos.z - 3.0D,
                cagePos.x + 3.0D, cagePos.y + 2.2D, cagePos.z + 3.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, initialBox, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            applyBlightRoot(level, foe, 60);
            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
        }

        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, cagePos, 3.0D, 60, level.getGameTime(), dummy, dummy.subSpells().getFirst(), EssenceType.BLIGHT, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NECROTIC_GREEN_HEX_COLOR, 18, 0.90F, 0.04F),
                                zone.center.x, zone.center.y + 0.4D, zone.center.z, 8, 1.1D, 0.3D, 1.1D, 0.02D
                        );
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB box = new AABB(
                                zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                                zone.center.x + 3.0D, zone.center.y + 2.2D, zone.center.z + 3.0D
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            applyBlightRoot(srv, foe, 60);
                            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                        }
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.27 Necrotic Decomposition Transmute:
        // Rapidly rots foliage and wood into dead variants, turns grass into coarse dirt or podzol,
        // and decomposes rotten flesh and bones into bone meal with +25% bonus yields!
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }

        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean()
                    ? Blocks.COARSE_DIRT.defaultBlockState()
                    : Blocks.PODZOL.defaultBlockState());
            maybeGrantBlightBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }

        if (state.is(BlockTags.LOGS)
                && !state.is(ModBlocks.VOID_BLIGHT_MANGROVE_LOG.get())
                && !state.is(ModBlocks.VOID_BLIGHT_MANGROVE_WOOD.get())
                && !state.is(ModBlocks.STRIPPED_VOID_BLIGHT_MANGROVE_LOG.get())
                && !state.is(ModBlocks.STRIPPED_VOID_BLIGHT_MANGROVE_WOOD.get())) {
            String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
            Block targetBlock;
            if (path.contains("stripped") && (path.endsWith("wood") || path.endsWith("hyphae"))) {
                targetBlock = ModBlocks.STRIPPED_VOID_BLIGHT_MANGROVE_WOOD.get();
            } else if (path.contains("stripped")) {
                targetBlock = ModBlocks.STRIPPED_VOID_BLIGHT_MANGROVE_LOG.get();
            } else if (path.endsWith("wood") || path.endsWith("hyphae")) {
                targetBlock = ModBlocks.VOID_BLIGHT_MANGROVE_WOOD.get();
            } else {
                targetBlock = ModBlocks.VOID_BLIGHT_MANGROVE_LOG.get();
            }
            BlockState next = targetBlock.defaultBlockState();
            if (state.hasProperty(RotatedPillarBlock.AXIS) && next.hasProperty(RotatedPillarBlock.AXIS)) {
                next = next.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
            }
            level.setBlockAndUpdate(pos, next);
            maybeGrantBlightBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }

        if (state.is(BlockTags.PLANKS) && !state.is(ModBlocks.VOID_BLIGHT_MANGROVE_PLANKS.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.VOID_BLIGHT_MANGROVE_PLANKS.get().defaultBlockState());
            maybeGrantBlightBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.SAPLINGS) && !state.is(ModBlocks.VOID_BLIGHT_MANGROVE_SAPLING.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.VOID_BLIGHT_MANGROVE_SAPLING.get().defaultBlockState());
            maybeGrantBlightBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (wiltFlowerIntoWitherRose(level, pos, state)) {
            maybeGrantBlightBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.LEAVES)) {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new ItemStack(Items.DEAD_BUSH, 1));
            maybeGrantBlightBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (wiltFloraIntoDeadBush(level, pos, state)) {
            maybeGrantBlightBatchTransmuteDiscount(level, caster, potencyTier);
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
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.ROTTEN_FLESH) || in.is(Items.BONE)) {
            // Decomposes rotten flesh & bones into Bone Meal with +25% bonus yields!
            int baseCount = in.is(Items.BONE) ? in.getCount() * 3 : in.getCount();
            int bonus = (baseCount / 4) + (level.random.nextFloat() < (baseCount % 4) * 0.25F ? 1 : 0);
            int total = Math.max(baseCount + 1, baseCount + bonus);
            itemEntity.setItem(new ItemStack(Items.BONE_MEAL, Math.min(64, total)));
            if (total > 64) {
                Block.popResource(level, itemEntity.blockPosition(), new ItemStack(Items.BONE_MEAL, total - 64));
            }
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NECROTIC_GREEN_HEX_COLOR, 18, 0.90F, 0.04F),
                    itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 8, 0.20D, 0.20D, 0.20D, 0.02D
            );
            maybeGrantBlightBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (isRawMeat(in)) {
            itemEntity.setItem(new ItemStack(Items.ROTTEN_FLESH, in.getCount()));
            maybeGrantBlightBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (in.is(ItemTags.LOGS) && !in.is(ModBlocks.VOID_BLIGHT_MANGROVE_LOG.get().asItem())) {
            itemEntity.setItem(new ItemStack(ModBlocks.VOID_BLIGHT_MANGROVE_LOG.get().asItem(), in.getCount()));
            maybeGrantBlightBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.27 Virulent Wither-Blight Hex:
        // Persistent milk-immune rotting briar curse (cleansed via Restoration/Radiant magic or caster defeat):
        // continuously deals Blight Materia damage, permanently disables all health regeneration,
        // shreds 40% armor toughness, and spreads contagiously to nearby enemies upon victim death!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 45, 1, true, true));

        float hexDmg = (1.50F + potencyTier * 0.52F) * mult;
        SpellCombatReactionEngine.hurtWithBlightMateriaAdvantage(level, target, null, hexDmg, false, true);

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), NECROTIC_GREEN_HEX_COLOR, 18, 0.90F, 0.04F),
                target.getX(), target.getY(0.8D), target.getZ(), 5, 0.25D, 0.30D, 0.25D, 0.02D
        );
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
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(target);
            target.removeEffect(MobEffects.POISON);
            target.removeEffect(MobEffects.WITHER);
            target.removeEffect(MobEffects.HUNGER);
            float heal = 3.1F + potencyTier * 0.95F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, heal);
            target.heal(heal);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static void applyBlightRoot(ServerLevel level, LivingEntity target, int ticks) {
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.95D, ticks);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6));
        target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), ticks, 0));
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + ticks);
        SpellExecutionEngine.applySyncedMotion(target, new Vec3(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D));
    }

    private static boolean wiltFlowerIntoWitherRose(ServerLevel level, BlockPos pos, BlockState bs) {
        if (bs.isAir() || bs.is(Blocks.WITHER_ROSE) || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, bs)) {
            return false;
        }
        if (bs.getBlock() instanceof DoublePlantBlock && bs.hasProperty(DoublePlantBlock.HALF)
                && bs.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            return false;
        }
        if (bs.is(BlockTags.SMALL_FLOWERS) || bs.is(BlockTags.FLOWERS)) {
            if (bs.getBlock() instanceof DoublePlantBlock && level.getBlockState(pos.above()).is(bs.getBlock())) {
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 18);
            }
            if (Blocks.WITHER_ROSE.defaultBlockState().canSurvive(level, pos)) {
                level.setBlockAndUpdate(pos, Blocks.WITHER_ROSE.defaultBlockState());
                return true;
            }
        }
        return false;
    }

    private static boolean wiltFloraIntoDeadBush(ServerLevel level, BlockPos pos, BlockState bs) {
        if (bs.isAir() || bs.is(Blocks.DEAD_BUSH) || bs.is(Blocks.WITHER_ROSE)
                || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, bs)) {
            return false;
        }
        if (bs.getBlock() instanceof DoublePlantBlock && bs.hasProperty(DoublePlantBlock.HALF)
                && bs.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            return false;
        }
        if (bs.is(BlockTags.SMALL_FLOWERS)
                || bs.is(BlockTags.FLOWERS)
                || bs.is(Blocks.SHORT_GRASS)
                || bs.is(Blocks.TALL_GRASS)
                || bs.is(Blocks.FERN)
                || bs.is(Blocks.LARGE_FERN)) {
            if (bs.getBlock() instanceof DoublePlantBlock && level.getBlockState(pos.above()).is(bs.getBlock())) {
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 18);
            }
            if (Blocks.DEAD_BUSH.defaultBlockState().canSurvive(level, pos)) {
                level.setBlockAndUpdate(pos, Blocks.DEAD_BUSH.defaultBlockState());
            } else {
                level.removeBlock(pos, false);
                Block.popResource(level, pos, new ItemStack(Items.DEAD_BUSH, 1));
            }
            return true;
        }
        return false;
    }

    private static boolean isRawMeat(ItemStack stack) {
        return stack.is(Items.BEEF) || stack.is(Items.PORKCHOP) || stack.is(Items.CHICKEN)
                || stack.is(Items.MUTTON) || stack.is(Items.RABBIT) || stack.is(Items.COD) || stack.is(Items.SALMON);
    }

    private static void maybeGrantBlightBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellBlightBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellBlightBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellBlightBatchTick", now);
        pData.putInt("SpellBlightBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.BLIGHT, refund);
        }
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        for (int dy = 2; dy >= -5; dy--) {
            BlockPos check = start.offset(0, dy, 0);
            BlockState bs = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());
            if ((!bs.isAir() && !bs.getCollisionShape(level, check).isEmpty())
                    && (above.isAir() || above.getCollisionShape(level, check.above()).isEmpty())) {
                return check;
            }
        }
        return start.below();
    }
}
