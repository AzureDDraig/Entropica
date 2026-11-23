package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.network.SpellVfxEventPayload;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellEidolonSpecs;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Nature Materia (Base Type #3)</b>
 * (Spellcrafting System Guide Section 6.4):
 * <ul>
 *     <li><b>Projectile</b>: <i>Thorned Briar Seed</i> — spiked botanical seed that burrows into the target on impact,
 *     dealing Nature Materia puncture damage (+1 vs Water/Earth, -1 vs Nether/Frozen/Umbral) and erupting 3D thorny vines
 *     that root non-boss targets to the spot for 2.0s (40t).</li>
 *     <li><b>Beam</b>: <i>Verdant Growth Ray</i> — piercing (+8 bonus pierces) continuous living emerald beam wrapped in
 *     coiling ivy &amp; blossoms that accelerates crop/sapling growth, harmlessly knits friendly ally wounds, and tangles
 *     non-boss enemies in progressive dragging ivy creepers.</li>
 *     <li><b>Radial Burst</b>: <i>Bramble Bloom Explosion</i> — rapid circular ring of jagged 3D wooden spikes &amp; thorny
 *     briars knocking non-boss foes backward first, rooting them for 1.5s (30t) after a 0.4s (8t) launch flight, and leaving
 *     uncapped temporary damaging ground briars.</li>
 *     <li><b>Touch / Strike</b>: <i>Poison Ivy Slash</i> — thorny crescent cleave coating the primary struck target in
 *     virulent botanical sap that inflicts Poison II for 8s (160t), shreds Armor Toughness, and corrodes Undead tissue
 *     with Nature Materia damage (while secondary cleave targets take a light 2.5s Poison I splash).</li>
 *     <li><b>Aura / Self</b>: <i>Sylvan Mantle</i> — wildflowers &amp; tall/short grass bloom underfoot strictly while walking,
 *     grants exact +30% speed in true forests/foliage, keeps unprovoked passive animals calm, and confers clear-sky sunlight
 *     health regeneration while registering Nature defending affinity.</li>
 *     <li><b>Glyph / Trap</b>: <i>Overgrowth Snare</i> — 1.0s arm, 10m (or Tier 5+ indefinite) disarmable circular root trap;
 *     violently erupts dense 3D wooden roots from the floor, trapping non-boss hostiles inside an entangled dome cage for 4.0s
 *     with continuous Nature Materia crush damage.</li>
 *     <li><b>Cone / Spray</b>: <i>Toxic Pollen Cone</i> — exact 60-degree billowing cloud of golden-yellow botanical pollen
 *     inflicting Nausea and Poison II on cast and refreshing Poison II + Nausea every 1.0s inside a 4.5s lingering pollen cloud.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Spore Tempest</i> — overhead mycelial canopy scaling across Tiers 1–10 from 6.0m / 6.0s
 *     up to 14.0m / 30.0s, carpeting the full circular zone in temporary poison moss patches and raining toxic fungal spores.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Sylvan Eidolon</i> — lush forest green (#1A5923) triad with clean zero-root spawning:
 *     Agent lunges &amp; roots non-boss foes for 2.0s; Sentry fires Thorn Darts (1.0s non-boss root + 3.0s Poison I);
 *     Decoy taunts hostiles, reflects 35% melee thorns damage + Slowness II, and bursts into toxic spores on death/expiry.</li>
 *     <li><b>Flow (Chains)</b>: <i>Entangling Briar Chains</i> — CHAIN_LEAP-exclusive 3D thorny creepers enforcing a hard
 *     5.0-block tether clamp between consecutive non-boss chained targets for 5.0s (100t).</li>
 *     <li><b>Blink / Step</b>: <i>Tree-Stepping</i> — phases cleanly through logs and leaf canopies across all tiers (with
 *     boosted range near living tree foliage) while leaving a temporary 30.0s blooming Flowering Azalea bush at departure.</li>
 *     <li><b>Transmute</b>: <i>Botanical Synthesis</i> — transmutes Stone -> Mossy Cobblestone, Stone Bricks -> Mossy Stone Bricks,
 *     Dirt -> Podzol, Dead Bushes -> random 6-species Saplings, and Cobblestone -> all-bark living Oak Wood.</li>
 *     <li><b>Affliction</b>: <i>Silencing Spore Hex</i> — persistent anti-milk fungal curse (cleansed by Restoration/Fire magic
 *     or caster defeat) permanently silencing spellcasting &amp; mob special abilities while shredding 20% / 35% / 50% Armor
 *     Toughness across Stacks I–III with zero continuous tick damage.</li>
 * </ul>
 */
public final class NatureSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SYLVAN_EIDOLON_HEX_COLOR = 0x1A5923;
    private static final String TAG_IVY_DRAG_STACKS = "SpellNatureIvyDragStacks";
    private static final String TAG_IVY_DRAG_UNTIL = "SpellNatureIvyDragUntil";

    /** Exact 60-degree cone half-angle (30°): cos(30°) = sqrt(3)/2 = 0.8660254; tan(30°) = 0.5773503. */
    private static final double CONE_HALF_ANGLE_COS = 0.8660254D;
    private static final double CONE_HALF_ANGLE_TAN = 0.5773503D;

    private static final Block[] SAPLING_BLOCKS = new Block[]{
            Blocks.OAK_SAPLING, Blocks.BIRCH_SAPLING, Blocks.SPRUCE_SAPLING,
            Blocks.JUNGLE_SAPLING, Blocks.CHERRY_SAPLING, Blocks.DARK_OAK_SAPLING
    };
    private static final Item[] SAPLING_ITEMS = new Item[]{
            Items.OAK_SAPLING, Items.BIRCH_SAPLING, Items.SPRUCE_SAPLING,
            Items.JUNGLE_SAPLING, Items.CHERRY_SAPLING, Items.DARK_OAK_SAPLING
    };

    @Override
    public EssenceType materiaType() {
        return EssenceType.NATURE;
    }

    // =========================================================================
    // CANONICAL SIGNATURE TITLES & GUIDE DESCRIPTIONS (SECTION 6.4)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Entangling Briar Chains";
            default -> "Verdant Briar " + flow.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Entangling Briar Chains: Leaping chains manifest as thorny creepers that wrap around each chained target, enforcing a hard 5-block tether constraint on non-boss victims for 5 seconds.";
            default -> null;
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Thorned Briar Seed";
            case BEAM -> "Verdant Growth Ray";
            case RADIAL_BURST -> "Bramble Bloom Explosion";
            case TOUCH_STRIKE -> "Poison Ivy Slash";
            case AURA -> "Sylvan Mantle";
            case GLYPH -> "Overgrowth Snare";
            case CONE -> "Toxic Pollen Cone";
            case SKYFALL -> "Spore Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public @Nullable String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Thorned Briar Seed: Spiked botanical seed that burrows into the target on impact, dealing Nature Materia puncture damage and erupting thorny vines that root non-boss targets for 2 seconds.";
            case BEAM -> "Verdant Growth Ray: Piercing (+8) living emerald beam that accelerates crop/sapling growth, harmlessly knits friendly ally wounds, and tangles enemies in speed-draining ivy creepers.";
            case RADIAL_BURST -> "Bramble Bloom Explosion: Detonates in a circular ring of jagged wooden spikes and thorny briars, flinging non-boss foes backward before rooting them for 1.5s and leaving ground briars.";
            case TOUCH_STRIKE -> "Poison Ivy Slash: Coats the primary struck target in virulent botanical sap inflicting 8s Poison II, Armor Toughness shred, and Undead sap corrosion (with a light 2.5s Poison I splash on secondary targets).";
            case AURA -> "Sylvan Mantle: Flowers and tall grass bloom underfoot while walking; grants +30% speed in forests/foliage, calms unprovoked wildlife, and confers clear-sky sunlight health regeneration.";
            case GLYPH -> "Overgrowth Snare: Circular disarmable root trap that violently erupts dense wooden roots from the floor, trapping non-boss hostiles inside an entangled cage for 4s with continuous crush damage.";
            case CONE -> "Toxic Pollen Cone: Sprays an exact 60-degree cloud of golden botanical pollen inflicting Nausea and Poison II on hit and refreshing both inside a 4.5s lingering pollen cloud.";
            case SKYFALL -> "Spore Tempest: Calls down toxic fungal spores across a 6m–14m circular canopy for 6s–30s (Tiers 1–10), carpeting the ground in poison moss patches and dealing Nature Materia spore damage.";
            case SUMMON_AGENT -> "Sylvan Eidolon: Forest green (#1A5923) woodland hunter that lunges across mid-range gaps and lashes/roots non-boss foes for 2.0s with thorny briar tendrils.";
            case SUMMON_SENTRY -> "Sylvan Thorn Sentry: Ground-anchored forest green (#1A5923) root spire that fires high-speed Thorn Darts inflicting a 1.0s non-boss briar root and 3.0s Poison I.";
            case SUMMON_DECOY -> "Sylvan Pollen Effigy: Woven forest green (#1A5923) floral effigy emitting sweet golden pollen to taunt hostiles, reflecting 35% melee thorns damage + Slowness II before bursting into toxic spores.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Tree-Stepping";
            case TRANSMUTE, BREAK -> "Botanical Synthesis";
            case AFFLICTION -> "Silencing Spore Hex";
            case HARVEST -> "Verdant Overgrowth Harvest";
            case RESTORATION -> "Photosynthetic Regrowth";
            default -> "Sylvan " + effect.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Tree-Stepping: Phases cleanly through tree trunks and leaf canopies with boosted range near living trees, leaving a temporary 30s Flowering Azalea bush at departure.";
            case TRANSMUTE, BREAK -> "Botanical Synthesis: Transmutes stone into mossy cobblestone, stone bricks into mossy stone bricks, dirt into podzol, dead bushes into 6-species saplings, and cobblestone into living oak wood.";
            case AFFLICTION -> "Silencing Spore Hex: Persistent anti-milk fungal curse (cleansed by Restoration/Fire magic or caster defeat) silencing spellcasting & mob abilities while shredding 20% / 35% / 50% Armor Toughness (0 tick damage).";
            default -> null;
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATIONS — ENTANGLING BRIAR CHAINS (SECTION 6.4)
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
        if (spell.flowPart().flow() != SpellFlowModifier.CHAIN_LEAP) {
            return;
        }
        int tier = spell.getEffectiveTier();
        Vec3 midpoint = fromTarget.position().add(toTarget.position()).scale(0.5D);

        // Initial briar snag on non-bosses only (without locking velocity before clamping within 5.0m!)
        if (!SpellCombatReactionEngine.isBossEntity(level, fromTarget)) {
            fromTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 2, false, true));
        }
        if (!SpellCombatReactionEngine.isBossEntity(level, toTarget)) {
            toTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 2, false, true));
        }

        // Immediately enforce the hard 5.0-block tether constraint on tick 0
        enforceHardBriarTether(level, fromTarget, toTarget);

        // Broadcast immediate 3D thorny creeper vine bridge between chained targets
        SpellWorldVfxManager.broadcastChainArc(
                level,
                fromTarget.position().add(0.0D, fromTarget.getBbHeight() * 0.55D, 0.0D),
                toTarget.position().add(0.0D, toTarget.getBbHeight() * 0.55D, 0.0D),
                EssenceType.NATURE, null, tier
        );
        spawnEmeraldLeafBurst(level, midpoint.add(0.0D, 0.75D, 0.0D), 14, 0.55D);
        level.playSound(null, BlockPos.containing(midpoint), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 0.85F, 0.90F);

        final UUID idA = fromTarget.getUUID();
        final UUID idB = toTarget.getUUID();
        int tetherDuration = 100; // 5.0s (100-tick) hard 5.0-block briar tether constraint!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, midpoint, 1.0D, tetherDuration, level.getGameTime(), null, null, EssenceType.NATURE, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    Entity eA = srv.getEntity(idA);
                    Entity eB = srv.getEntity(idB);
                    if (!(eA instanceof LivingEntity lA) || !(eB instanceof LivingEntity lB) || !lA.isAlive() || !lB.isAlive()) {
                        return false;
                    }

                    boolean clamped = enforceHardBriarTether(srv, lA, lB);

                    Vec3 chestA = lA.position().add(0.0D, lA.getBbHeight() * 0.55D, 0.0D);
                    Vec3 chestB = lB.position().add(0.0D, lB.getBbHeight() * 0.55D, 0.0D);
                    zone.center = chestA.add(chestB).scale(0.5D);
                    if (elapsed % 12 == 0) {
                        SpellWorldVfxManager.broadcast(
                                srv, SpellVfxEventPayload.VFX_CHAIN_ARC, chestA, chestB,
                                EssenceType.NATURE, null, 0.14F, 16, tier
                        );
                    }
                    if (elapsed % 6 == 0) {
                        Vec3 span = chestB.subtract(chestA);
                        for (double f = 0.2D; f <= 0.8D; f += 0.3D) {
                            Vec3 pt = chestA.add(span.scale(f));
                            srv.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x1A5923, 22, 1.0F, 0.08F),
                                    pt.x, pt.y, pt.z, 1, 0.06D, 0.06D, 0.06D, 0.01D);
                        }
                    }
                    if (clamped && elapsed % 10 == 0) {
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 0.55F, 0.85F);
                    }
                    return true;
                }
        ));
    }

    private static boolean enforceHardBriarTether(ServerLevel srv, LivingEntity lA, LivingEntity lB) {
        Vec3 posA = lA.position();
        Vec3 posB = lB.position();
        Vec3 delta = posB.subtract(posA);
        double dist = delta.length();
        if (dist <= 5.0D || dist < 1.0E-4D) {
            return false;
        }

        boolean bossA = SpellCombatReactionEngine.isBossEntity(srv, lA);
        boolean bossB = SpellCombatReactionEngine.isBossEntity(srv, lB);
        if (bossA && bossB) {
            return false;
        }

        Vec3 unitAB = delta.scale(1.0D / dist);
        double excess = dist - 4.95D;
        if (!bossA && !bossB) {
            Vec3 targetA = posA.add(unitAB.scale(excess * 0.5D));
            Vec3 targetB = posB.subtract(unitAB.scale(excess * 0.5D));
            clampOrPullEntityToTetherPos(srv, lA, targetA, unitAB);
            clampOrPullEntityToTetherPos(srv, lB, targetB, unitAB.scale(-1.0D));
        } else if (bossA) {
            Vec3 targetB = posA.add(unitAB.scale(4.95D));
            clampOrPullEntityToTetherPos(srv, lB, targetB, unitAB.scale(-1.0D));
        } else {
            Vec3 targetA = posB.subtract(unitAB.scale(4.95D));
            clampOrPullEntityToTetherPos(srv, lA, targetA, unitAB);
        }
        return true;
    }

    private static void clampOrPullEntityToTetherPos(ServerLevel srv, LivingEntity ent, Vec3 targetPos, Vec3 inwardDir) {
        AABB movedBox = ent.getBoundingBox().move(targetPos.subtract(ent.position()));
        if (srv.noCollision(ent, movedBox)) {
            ent.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        }
        Vec3 vel = ent.getDeltaMovement();
        double outwardSpeed = -vel.dot(inwardDir);
        Vec3 adjusted = outwardSpeed > 0.0D
                ? vel.add(inwardDir.scale(outwardSpeed + 0.24D))
                : vel.add(inwardDir.scale(0.18D));
        SpellExecutionEngine.applySyncedMotion(ent, new Vec3(adjusted.x, Math.max(vel.y, 0.06D), adjusted.z));
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (SECTION 6.4)
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x2CE668, 24, 1.0F, 0.10F),
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    2, 0.07D, 0.07D, 0.07D, 0.01D);
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
        // Thorned Briar Seed: burrows into target dealing Nature Materia puncture damage (+1/-1 advantage) & boss-guarded 2.0s (40t) briar root!
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) return;
        boolean safeguard = resolveDynamicSafeguard(spell, caster);
        if (caster != null && (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, safeguard)
                || (safeguard && isFriendlyOrWoodlandAlly(living, caster)))) {
            return;
        }
        int tier = spell.getEffectiveTier();
        float punctureDmg = Math.max(2.5F + tier * 0.75F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.35F);
        SpellCombatReactionEngine.hurtWithNatureMateriaAdvantage(level, living, caster, punctureDmg);
        applyBriarRoot(living, 40);

        // 46-tick 3D thorny vine snare eruption wrapping the rooted target!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                living.position().add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.NATURE, null, 1.25F, 46, tier
        );
        spawnEmeraldLeafBurst(level, living.position().add(0.0D, 0.55D, 0.0D), 16, 0.40D);
        level.playSound(null, living.blockPosition(), SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 0.85F, 0.85F);
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
        // Ground-impact Thorned Briar Seed burrows into soil and erupts a 1.85m circular thorny vine snare!
        int tier = spell.getEffectiveTier();
        Vec3 pos = blockHit.getLocation();
        boolean safeguard = resolveDynamicSafeguard(spell, caster);
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                pos, Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i()),
                EssenceType.NATURE, null, 1.15F, 44, tier
        );
        spawnEmeraldLeafBurst(level, pos.add(0.0D, 0.25D, 0.0D), 14, 0.45D);
        level.playSound(null, blockHit.getBlockPos(), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 0.75F, 0.90F);

        double radSq = 1.85D * 1.85D;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(1.85D))) {
            if (!foe.isAlive() || foe == caster) continue;
            double dx = foe.getX() - pos.x;
            double dz = foe.getZ() - pos.z;
            if (dx * dx + dz * dz > radSq) continue;
            if (caster != null && (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)
                    || isFriendlyOrWoodlandAlly(foe, caster))) {
                continue;
            }
            applyBriarRoot(foe, 40);
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
        // Verdant Growth Ray (+8 pierces): accelerates crop/sapling growth, harmlessly knits ally wounds,
        // and tangles non-boss foes in progressive dragging ivy creepers!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean safeguard = resolveDynamicSafeguard(spell, caster);
        float allyHeal = (2.5F + tier * 0.85F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster) continue;
            if (isFriendlyOrWoodlandAlly(hit, caster)) {
                SpellCombatReactionEngine.notifyMagicalHeal(hit, allyHeal);
                hit.heal(allyHeal);
                level.sendParticles(ParticleTypes.HEART, hit.getX(), hit.getY(1.05D), hit.getZ(), 2, 0.25D, 0.25D, 0.25D, 0.02D);
                spawnEmeraldLeafBurst(level, hit.position().add(0.0D, 0.7D, 0.0D), 6, 0.30D);
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) {
                if (SpellCombatReactionEngine.isBossEntity(level, hit)) {
                    // Boss tenacity guard: light Slowness II only — never Inertial Anchor or velocity drag!
                    hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, true));
                } else {
                    CompoundTag hTag = EntityHelper.getPersistentData(hit);
                    int prevStacks = (hTag.getLong(TAG_IVY_DRAG_UNTIL).orElse(0L) > now)
                            ? hTag.getInt(TAG_IVY_DRAG_STACKS).orElse(0) : 0;
                    int stacks = Math.min(5, prevStacks + 1);
                    hTag.putInt(TAG_IVY_DRAG_STACKS, stacks);
                    hTag.putLong(TAG_IVY_DRAG_UNTIL, now + 50L);

                    // Progressive speed-draining ivy creepers (Slowness II -> VI + ground drag)!
                    hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, Math.min(5, stacks), false, true));
                    if (stacks >= 3) {
                        hit.addEffect(new MobEffectInstance(
                                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 35, 0, false, false));
                    }
                    Vec3 curVel = hit.getDeltaMovement();
                    double dragFactor = Math.max(0.12D, 0.55D - stacks * 0.09D);
                    hit.setDeltaMovement(curVel.x * dragFactor, Math.min(0.02D, curVel.y), curVel.z * dragFactor);
                    hit.hasImpulse = true;
                }

                if (now % 10L == 0L) {
                    SpellWorldVfxManager.broadcast(
                            level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                            hit.position().add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                            EssenceType.NATURE, null, 0.95F, 22, tier
                    );
                }
            }
        }

        // Accelerate crop, sapling, berry & flora growth along the Verdant Growth Ray
        double effectiveStop = blockHit.getLocation().distanceTo(caster.getEyePosition());
        int steps = Math.min(24, (int) Math.ceil(Math.min(maxReach, effectiveStop + 0.5D)));
        int grownCount = 0;
        for (int s = 1; s <= steps && grownCount < 6; s++) {
            BlockPos p = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            for (BlockPos bp : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) {
                if (level.random.nextFloat() > 0.42F) continue;
                BlockState bs = level.getBlockState(bp);
                if (bs.getBlock() instanceof BonemealableBlock growable
                        && growable.isValidBonemealTarget(level, bp, bs)) {
                    BlockPos imm = bp.immutable();
                    if (growable.isBonemealSuccess(level, level.random, imm, bs)) {
                        growable.performBonemeal(level, level.random, imm, bs);
                    }
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.RUNE_BERKANO.get(), 0xB4FF78, 26, 1.05F, 0.05F),
                            imm.getX() + 0.5D, imm.getY() + 0.65D, imm.getZ() + 0.5D, 2, 0.2D, 0.2D, 0.2D, 0.01D);
                    if (++grownCount >= 6) break;
                }
            }
        }
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
        // Poison Ivy Slash (User A1): primary struck target (i == 0) receives full 8.0s (160t) Poison II,
        // Armor Toughness shred, and Undead sap corrosion (routed through hurtWithNatureMateriaAdvantage);
        // secondary cleave bystanders (i > 0) receive only a light 2.5s (50t) Poison I splash!
        int tier = spell.getEffectiveTier();
        boolean safeguard = resolveDynamicSafeguard(spell, caster);
        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), direction, 3.0D,
                EssenceType.NATURE, null, tier
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.VINE_STEP, SoundSource.PLAYERS, 0.90F, 1.20F);

        if (hitEntities.isEmpty()) {
            return;
        }

        LivingEntity primaryTarget = hitEntities.getFirst();
        for (int i = 0; i < hitEntities.size(); i++) {
            LivingEntity hit = hitEntities.get(i);
            if (hit == null || !hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)
                    || (safeguard && isFriendlyOrWoodlandAlly(hit, caster))) {
                continue;
            }

            if (i == 0 && hit == primaryTarget) {
                hit.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 1));
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 0.0D, 4.0D + tier * 0.5D, 160L);
                SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.20D, 160L);

                if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                    SpellCombatReactionEngine.hurtWithNatureMateriaAdvantage(
                            level,
                            hit,
                            caster,
                            (3.0F + tier * 0.85F) * Math.max(0.5F, apparatusPotency)
                    );
                }

                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_ACID.get(), 0x8AFF3B, 36, 1.05F, 0.06F),
                        hit.getX(), hit.getY(0.65D), hit.getZ(), 14, 0.32D, 0.38D, 0.32D, 0.04D);
                spawnEmeraldLeafBurst(level, hit.position().add(0.0D, 0.6D, 0.0D), 10, 0.35D);
            } else {
                // Secondary cleave splash (i > 0): light 2.5s (50t) Poison I only!
                hit.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0));
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_ACID.get(), 0x8AFF3B, 24, 0.90F, 0.05F),
                        hit.getX(), hit.getY(0.60D), hit.getZ(), 5, 0.22D, 0.25D, 0.22D, 0.03D);
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
        // Bramble Bloom Explosion: circular blast flinging non-boss foes outward immediately (1.15 horiz, 0.36 up),
        // followed 8 ticks (0.4s) later by a 1.5s (30t) briar root where they land, plus uncapped ground briars!
        int tier = spell.getEffectiveTier();
        double effectiveRadius = Math.max(6.0D, radius);
        double radSq = effectiveRadius * effectiveRadius;
        boolean safeguard = resolveDynamicSafeguard(spell, caster);

        AABB box = new AABB(
                center.x - effectiveRadius, center.y - 2.0D, center.z - effectiveRadius,
                center.x + effectiveRadius, center.y + 2.8D, center.z + effectiveRadius
        );
        List<UUID> flungVictims = new ArrayList<>();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity == caster || !entity.isAlive()) continue;
            double dx = entity.getX() - center.x;
            double dz = entity.getZ() - center.z;
            if (dx * dx + dz * dz > radSq) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, safeguard)
                    || (safeguard && isFriendlyOrWoodlandAlly(entity, caster))) {
                continue;
            }
            if (!SpellCombatReactionEngine.isBossEntity(level, entity)) {
                Vec3 horiz = new Vec3(dx, 0.0D, dz);
                Vec3 away = (horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D)).scale(1.15D);
                SpellExecutionEngine.applySyncedMotion(entity, new Vec3(away.x, 0.36D, away.z));
            }
            flungVictims.add(entity.getUUID());
            SpellWorldVfxManager.broadcast(
                    level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                    entity.position().add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    EssenceType.NATURE, null, 1.05F, 34, tier
            );
        }

        // Short 8-tick (0.4s) delayed follow-up so outward knockback flings targets backward BEFORE briar vines root them!
        if (!flungVictims.isEmpty()) {
            boolean[] appliedDelayedRoot = new boolean[]{false};
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, center, 0.5D, 8, level.getGameTime(), null, null, EssenceType.NATURE, 1.0F,
                    (srv, z, elapsed) -> {
                        if (elapsed < 8) return true;
                        if (appliedDelayedRoot[0]) return false;
                        appliedDelayedRoot[0] = true;
                        for (UUID uid : flungVictims) {
                            if (srv.getEntity(uid) instanceof LivingEntity victim && victim.isAlive()) {
                                applyBriarRoot(victim, 30);
                                SpellWorldVfxManager.broadcast(
                                        srv, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                                        victim.position().add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.NATURE, null, 1.05F, 32, tier
                                );
                                spawnEmeraldLeafBurst(srv, victim.position().add(0.0D, 0.35D, 0.0D), 8, 0.30D);
                            }
                        }
                        return false;
                    }
            ));
        }

        int briarDuration = 160; // 8.0s ground briar patch duration
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, center, effectiveRadius, EssenceType.NATURE, null, briarDuration, tier
        );

        // Uncapped circular temporary Moss Carpet ground briars across full effectiveRadius!
        List<BlockPos> placedBriars = new ArrayList<>();
        BlockPos cPos = BlockPos.containing(center);
        int bRad = (int) Math.ceil(effectiveRadius);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-bRad, -1, -bRad), cPos.offset(bRad, 1, bRad))) {
            double bx = (p.getX() + 0.5D) - center.x;
            double bz = (p.getZ() + 0.5D) - center.z;
            if (bx * bx + bz * bz > radSq) continue;
            if (level.getBlockState(p).isAir()
                    && level.getBlockState(p.below()).isSolidRender()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, level.getBlockState(p))
                    && level.random.nextFloat() < 0.38F) {
                level.setBlockAndUpdate(p, Blocks.MOSS_CARPET.defaultBlockState());
                placedBriars.add(p.immutable());
            }
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, effectiveRadius, briarDuration, level.getGameTime(), spell, slot, EssenceType.NATURE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        for (BlockPos bp : placedBriars) {
                            if (srv.getBlockState(bp).is(Blocks.MOSS_CARPET)) {
                                srv.removeBlock(bp, false);
                            }
                        }
                        return false;
                    }
                    if (elapsed > 0 && elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = resolveDynamicSafeguard(zone.spell, owner);
                        double zRadSq = zone.radius * zone.radius;
                        AABB patchBox = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 1.2D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 1.6D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, patchBox, e -> e.isAlive() && e != owner)) {
                            double fdx = foe.getX() - zone.center.x;
                            double fdz = foe.getZ() - zone.center.z;
                            if (fdx * fdx + fdz * fdz > zRadSq) continue;
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zSafe)
                                    || isFriendlyOrWoodlandAlly(foe, owner)) {
                                continue;
                            }
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 1));
                            SpellCombatReactionEngine.hurtWithNatureMateriaAdvantage(
                                    srv, foe, owner, 1.5F + tier * 0.35F
                            );
                        }
                    }
                    return true;
                }
        ));
        spawnEmeraldLeafBurst(level, center.add(0.0D, 0.45D, 0.0D), 28, effectiveRadius * 0.45D);
        level.playSound(null, cPos, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 1.0F, 0.80F);
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
        // Sylvan Mantle (User A1): 3D twining leafy-vine & blossom garland + +30% true foliage speed + walking wildflower/tall-grass trail
        // + unprovoked wildlife calm + clear-sky sunlight regen + Nature defending affinity!
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 50;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.65F, null);
        spawnEmeraldLeafBurst(level, caster.position().add(0.0D, 0.9D, 0.0D), 22, 0.55D);
        level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xFFDC47, 36, 1.05F, 0.05F),
                caster.getX(), caster.getY(1.0D), caster.getZ(), 14, 0.45D, 0.5D, 0.45D, 0.02D);
        level.playSound(null, caster.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.85F, 1.15F);
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
        // Overgrowth Snare: canonical 1.0s (20t) arming delay, 10m (12,000t) or Tier 5+ indefinite persistence, 50% Sneak-Right-Click disarm!
        // Circular trigger & circular 4.0s (80t) 3D wooden root dome cage with boss root/clamp guards and Nature Materia crush damage!
        int tier = spell.getEffectiveTier();
        double cageRadius = Math.max(4.0D, radius);
        double triggerRad = cageRadius * 0.85D;
        double triggerRadSq = triggerRad * triggerRad;
        double cageRadSq = cageRadius * cageRadius;
        int glyphLifetime = tier >= 5 ? (Integer.MAX_VALUE / 4) : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(level, glyphCenter, cageRadius * 0.72D, EssenceType.NATURE, null, 220, tier);
        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 0.75F, 0.95F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, cageRadius, glyphLifetime, level.getGameTime(), spell, slot, EssenceType.NATURE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed > 0 && elapsed % 180 == 0) {
                        SpellWorldVfxManager.broadcastArcaneSeal(srv, zone.center, cageRadius * 0.72D, EssenceType.NATURE, null, 200, tier);
                    }
                    // Canonical 1.0s (20-tick) arming delay before proximity trigger activates
                    if (elapsed < 20) return true;
                    if (elapsed % 4 != 0) return true;

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = resolveDynamicSafeguard(zone.spell, owner);
                    AABB triggerBox = new AABB(
                            zone.center.x - triggerRad, zone.center.y - 1.0D, zone.center.z - triggerRad,
                            zone.center.x + triggerRad, zone.center.y + 2.4D, zone.center.z + triggerRad
                    );
                    List<LivingEntity> intruders = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> {
                        if (!e.isAlive() || e == owner) return false;
                        double dx = e.getX() - zone.center.x;
                        double dz = e.getZ() - zone.center.z;
                        if (dx * dx + dz * dz > triggerRadSq) return false;
                        return !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)
                                && !isFriendlyOrWoodlandAlly(e, owner);
                    });
                    if (intruders.isEmpty()) return true;

                    // Erupt the circular 3D Entangled Wooden Root Dome Cage for 4.0s (80t)!
                    SpellWorldVfxManager.broadcastWardShell(srv, zone.center, cageRadius, EssenceType.NATURE, null, 88, tier);
                    SpellWorldVfxManager.broadcastArcaneSeal(srv, zone.center, cageRadius, EssenceType.NATURE, null, 92, tier);
                    spawnEmeraldLeafBurst(srv, zone.center.add(0.0D, 1.0D, 0.0D), 32, cageRadius * 0.55D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 1.15F, 0.65F);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 1.0F, 0.75F);

                    AABB cageBox = new AABB(
                            zone.center.x - cageRadius, zone.center.y - 1.2D, zone.center.z - cageRadius,
                            zone.center.x + cageRadius, zone.center.y + 3.4D, zone.center.z + cageRadius
                    );
                    double innerClampSq = (cageRadius * 0.78D) * (cageRadius * 0.78D);
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, cageRadius, 80, srv.getGameTime(), zone.spell, zone.slot, EssenceType.NATURE, zone.apparatusPotency,
                            (csrv, czone, celapsed) -> {
                                if (celapsed > czone.maxDurationTicks) return false;
                                LivingEntity cOwner = czone.resolveCaster(csrv);
                                boolean cWard = resolveDynamicSafeguard(czone.spell, cOwner);
                                for (LivingEntity victim : csrv.getEntitiesOfClass(LivingEntity.class, cageBox,
                                        e -> e.isAlive() && e != cOwner
                                                && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, cOwner, cWard)
                                                && !isFriendlyOrWoodlandAlly(e, cOwner))) {
                                    double dx = victim.getX() - czone.center.x;
                                    double dz = victim.getZ() - czone.center.z;
                                    double horizSq = dx * dx + dz * dz;
                                    if (horizSq > cageRadSq) continue;

                                    if (!SpellCombatReactionEngine.isBossEntity(csrv, victim)) {
                                        if (horizSq > innerClampSq && horizSq > 1.0E-4D) {
                                            Vec3 inward = new Vec3(-dx, 0.0D, -dz).normalize().scale(0.52D);
                                            SpellExecutionEngine.applySyncedMotion(victim, new Vec3(inward.x, 0.05D, inward.z));
                                        }
                                        applyBriarRoot(victim, 22);
                                    } else {
                                        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 22, 1, false, true));
                                    }

                                    if (celapsed % 16 == 0) {
                                        float crushDmg = (2.2F + tier * 0.65F) * Math.max(0.5F, czone.apparatusPotency);
                                        SpellCombatReactionEngine.hurtWithNatureMateriaAdvantage(csrv, victim, cOwner, crushDmg);
                                        if (czone.spell != null && czone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    csrv, cOwner, victim, Vec3.ZERO,
                                                    czone.spell, czone.slot, czone.fallbackMateria, czone.apparatusPotency * 0.35F
                                            );
                                        }
                                        spawnEmeraldLeafBurst(csrv, victim.position().add(0.0D, 0.7D, 0.0D), 6, 0.30D);
                                    }
                                }
                                return true;
                            }
                    ));
                    return false;
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
        // Toxic Pollen Cone: exact 60-degree cone (dot >= 0.8660254D) with consistent ally/safeguard filtering across both loops,
        // plus an active 4.5s (90-tick) lingering pollen cloud inflicting Poison II (80t) + Nausea (100t) every 20 ticks!
        int tier = spell.getEffectiveTier();
        double reach = Math.max(8.0D, coneRange);
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 coneOrigin = caster.getEyePosition();
        boolean safeguard = resolveDynamicSafeguard(spell, caster);

        SpellWorldVfxManager.broadcastVortexCone(
                level, coneOrigin.subtract(0.0D, 0.2D, 0.0D), normDir, reach,
                EssenceType.NATURE, null, tier
        );

        Set<UUID> afflicted = new HashSet<>();
        for (LivingEntity hit : hitTargets) {
            if (hit == null || !hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)
                    || isFriendlyOrWoodlandAlly(hit, caster)) {
                continue;
            }
            if (!isInsidePollenCone(coneOrigin, normDir, reach, hit)) {
                continue;
            }
            afflicted.add(hit.getUUID());
            hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 160, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 1));
        }

        double maxConeWidth = reach * CONE_HALF_ANGLE_TAN + 1.25D;
        AABB coneBox = new AABB(coneOrigin, coneOrigin.add(normDir.scale(reach))).inflate(maxConeWidth);
        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, coneBox, LivingEntity::isAlive)) {
            if (cand == caster || afflicted.contains(cand.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, safeguard)
                    || isFriendlyOrWoodlandAlly(cand, caster)) {
                continue;
            }
            if (isInsidePollenCone(coneOrigin, normDir, reach, cand)) {
                afflicted.add(cand.getUUID());
                cand.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 160, 0));
                cand.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 1));
            }
        }

        // Dense billowing golden-yellow pollen clouds + active 4.5s (90-tick) lingering pollen cloud!
        for (double d = 1.5D; d <= reach * 0.90D; d += 2.0D) {
            Vec3 cloudPt = coneOrigin.add(normDir.scale(d));
            double spread = 0.25D + d * CONE_HALF_ANGLE_TAN * 0.45D;
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xFFDC38, 48, 1.15F, 0.05F),
                    cloudPt.x, cloudPt.y, cloudPt.z, 12, spread, spread * 0.65D, spread, 0.025D);
        }

        Vec3 hazeCenter = coneOrigin.add(normDir.scale(reach * 0.50D));
        double cloudRadius = Math.max(2.25D, reach * CONE_HALF_ANGLE_TAN * 0.65D);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, hazeCenter, cloudRadius, 90, level.getGameTime(), spell, slot, EssenceType.NATURE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        for (double d = 2.0D; d <= reach * 0.90D; d += 2.5D) {
                            Vec3 pt = coneOrigin.add(normDir.scale(d));
                            double sp = 0.22D + d * CONE_HALF_ANGLE_TAN * 0.42D;
                            srv.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xFFDC38, 34, 1.10F, 0.05F),
                                    pt.x, pt.y, pt.z, 6, sp, sp * 0.60D, sp, 0.015D);
                        }
                    }
                    if (elapsed > 0 && elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = resolveDynamicSafeguard(zone.spell, owner);
                        double zRadSq = zone.radius * zone.radius;
                        for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, coneBox, LivingEntity::isAlive)) {
                            if (victim == owner) continue;
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, zSafe)
                                    || isFriendlyOrWoodlandAlly(victim, owner)) {
                                continue;
                            }
                            double dx = victim.getX() - zone.center.x;
                            double dz = victim.getZ() - zone.center.z;
                            boolean inCloud = isInsidePollenCone(coneOrigin, normDir, reach, victim)
                                    || (dx * dx + dz * dz <= zRadSq && Math.abs(victim.getY() - zone.center.y) <= 2.25D);
                            if (inCloud) {
                                victim.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                                victim.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
                            }
                        }
                    }
                    return true;
                }
        ));
        level.playSound(null, caster.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.90F, 0.90F);
    }

    private static boolean isInsidePollenCone(Vec3 origin, Vec3 normDir, double reach, LivingEntity candidate) {
        double reachSq = reach * reach;
        Vec3 toCenter = candidate.getBoundingBox().getCenter().subtract(origin);
        Vec3 toEye = candidate.getEyePosition().subtract(origin);
        Vec3 toBase = candidate.position().subtract(origin);
        return isPointInSixtyDegreeCone(toCenter, normDir, reachSq)
                || isPointInSixtyDegreeCone(toEye, normDir, reachSq)
                || isPointInSixtyDegreeCone(toBase, normDir, reachSq);
    }

    private static boolean isPointInSixtyDegreeCone(Vec3 offset, Vec3 normDir, double reachSq) {
        double lenSq = offset.lengthSqr();
        if (lenSq <= 1.0E-6D || lenSq > reachSq) {
            return false;
        }
        return offset.scale(1.0D / Math.sqrt(lenSq)).dot(normDir) >= CONE_HALF_ANGLE_COS;
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
        // Spore Tempest (User A2): scales BOTH circular radius (6.0m -> 14.0m) AND duration (6.0s / 120t -> 30.0s / 600t)
        // across Tiers 1–10, uncapping temporary Moss Carpet patches across the full circle & routing spore damage through Nature advantage!
        int tier = Math.clamp(spell.getEffectiveTier(), 1, 10);
        double r = Math.max(radius, 6.0D + (tier - 1) * (8.0D / 9.0D));
        double rSq = r * r;
        int tempestDuration = Math.round(120.0F + (tier - 1) * (480.0F / 9.0F));

        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter, r, EssenceType.NATURE, null, tempestDuration, tier);

        List<BlockPos> sporeMoss = new ArrayList<>();
        BlockPos cPos = BlockPos.containing(impactCenter);
        int mr = (int) Math.ceil(r);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-mr, -1, -mr), cPos.offset(mr, 1, mr))) {
            double dx = (p.getX() + 0.5D) - impactCenter.x;
            double dz = (p.getZ() + 0.5D) - impactCenter.z;
            if (dx * dx + dz * dz > rSq) continue;
            if (level.getBlockState(p).isAir()
                    && level.getBlockState(p.below()).isSolidRender()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, level.getBlockState(p))
                    && level.random.nextFloat() < 0.36F) {
                level.setBlockAndUpdate(p, Blocks.MOSS_CARPET.defaultBlockState());
                sporeMoss.add(p.immutable());
            }
        }
        level.playSound(null, cPos, SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 1.0F, 0.75F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, tempestDuration, level.getGameTime(), spell, slot, EssenceType.NATURE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        for (BlockPos mp : sporeMoss) {
                            if (srv.getBlockState(mp).is(Blocks.MOSS_CARPET)) {
                                srv.removeBlock(mp, false);
                            }
                        }
                        return false;
                    }
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xB8FF52, 42, 1.15F, 0.05F),
                                zone.center.x, zone.center.y + 4.8D, zone.center.z,
                                16, zone.radius * 0.52D, 1.6D, zone.radius * 0.52D, 0.025D);
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                                zone.center.x, zone.center.y + 4.2D, zone.center.z,
                                10, zone.radius * 0.50D, 1.4D, zone.radius * 0.50D, 0.02D);
                    }
                    if (elapsed == 1 || (elapsed > 0 && elapsed % 20 == 0)) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = resolveDynamicSafeguard(zone.spell, owner);
                        double zRadSq = zone.radius * zone.radius;
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.5D, zone.center.z + zone.radius
                        );
                        for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            double dx = victim.getX() - zone.center.x;
                            double dz = victim.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > zRadSq) continue;
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, wardSafe)
                                    || isFriendlyOrWoodlandAlly(victim, owner)) {
                                continue;
                            }
                            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                            float sporeDmg = (1.8F + tier * 0.45F) * Math.max(0.5F, zone.apparatusPotency);
                            SpellCombatReactionEngine.hurtWithNatureMateriaAdvantage(srv, victim, owner, sporeDmg);
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, victim, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.30F
                                );
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
        // Sylvan Eidolon (User A3): clean botanical coalescence with zero spawn root!
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.NATURE.name());
        tag.putInt("SpellEidolonTintRGB", SYLVAN_EIDOLON_HEX_COLOR);
        spawnEmeraldLeafBurst(level, spawnPos.add(0.0D, 0.85D, 0.0D), 24, 0.65D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS (SECTION 6.4)
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Tree-Stepping (User A4): leaves a temporary 30.0s (600t) Flowering Azalea bush at departure + lush emerald vine transit!
        BlockPos dep = BlockPos.containing(departurePos);
        BlockPos placePos = null;
        for (BlockPos cand : List.of(dep, dep.below(), dep.above())) {
            BlockState at = level.getBlockState(cand);
            BlockState under = level.getBlockState(cand.below());
            if ((at.isAir() || at.canBeReplaced())
                    && !under.getCollisionShape(level, cand.below()).isEmpty()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, cand, at)) {
                placePos = cand.immutable();
                break;
            }
        }

        if (placePos != null) {
            final BlockPos azaleaPos = placePos;
            level.setBlockAndUpdate(azaleaPos, Blocks.FLOWERING_AZALEA.defaultBlockState());
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, Vec3.atCenterOf(azaleaPos), 1.0D, 600, level.getGameTime(),
                    null, null, EssenceType.NATURE, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed >= zone.maxDurationTicks) {
                            if (srv.getBlockState(azaleaPos).is(Blocks.FLOWERING_AZALEA)) {
                                srv.removeBlock(azaleaPos, false);
                                srv.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0xFF94C2, 34, 1.05F, 0.08F),
                                        azaleaPos.getX() + 0.5D, azaleaPos.getY() + 0.5D, azaleaPos.getZ() + 0.5D,
                                        12, 0.3D, 0.3D, 0.3D, 0.03D);
                            }
                            return false;
                        }
                        return srv.getBlockState(azaleaPos).is(Blocks.FLOWERING_AZALEA);
                    }
            ));
        }

        SpellWorldVfxManager.broadcastChainArc(
                level, departurePos.add(0.0D, 0.85D, 0.0D), arrivalPos.add(0.0D, 0.85D, 0.0D),
                EssenceType.NATURE, null, potencyTier
        );
        spawnEmeraldLeafBurst(level, departurePos.add(0.0D, 0.8D, 0.0D), 22, 0.45D);
        spawnEmeraldLeafBurst(level, arrivalPos.add(0.0D, 0.8D, 0.0D), 22, 0.45D);
        level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0xFF94C2, 38, 1.10F, 0.08F),
                departurePos.x, departurePos.y + 0.75D, departurePos.z, 14, 0.35D, 0.4D, 0.35D, 0.04D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.AZALEA_PLACE, SoundSource.PLAYERS, 0.90F, 1.15F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Botanical Synthesis: Stone -> Mossy Cobblestone, Stone Bricks -> Mossy Stone Bricks,
        // Dirt -> Podzol, Dead Bush -> 6-species Saplings, Cobblestone -> all-bark living Oak Wood!
        BlockState result = null;
        if (state.is(Blocks.STONE) || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)) {
            result = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        } else if (state.is(Blocks.STONE_BRICKS) || state.is(Blocks.INFESTED_STONE_BRICKS)) {
            result = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        } else if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)) {
            result = Blocks.PODZOL.defaultBlockState();
        } else if (state.is(Blocks.DEAD_BUSH)) {
            Block chosen = SAPLING_BLOCKS[level.random.nextInt(SAPLING_BLOCKS.length)];
            BlockState cand = chosen.defaultBlockState();
            if (!cand.canSurvive(level, pos) && level.getBlockState(pos.below()).is(Blocks.SAND)) {
                level.setBlockAndUpdate(pos.below(), Blocks.PODZOL.defaultBlockState());
            }
            result = cand;
        } else if (state.is(Blocks.COBBLESTONE)) {
            result = Blocks.OAK_WOOD.defaultBlockState();
        }

        if (result != null) {
            level.setBlockAndUpdate(pos, result);
            spawnEmeraldLeafBurst(level, Vec3.atCenterOf(pos), 10, 0.35D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.RUNE_BERKANO.get(), 0xB4FF78, 28, 1.05F, 0.05F),
                    pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D, 3, 0.25D, 0.25D, 0.25D, 0.01D);
            level.playSound(null, pos, SoundEvents.MOSS_PLACE, SoundSource.PLAYERS, 0.75F, 1.10F);
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
        ItemStack stack = itemEntity.getItem();
        int count = stack.getCount();
        ItemStack transmuted = null;
        if (stack.is(Items.STONE) || stack.is(Items.ANDESITE) || stack.is(Items.DIORITE) || stack.is(Items.GRANITE)) {
            transmuted = new ItemStack(Items.MOSSY_COBBLESTONE, count);
        } else if (stack.is(Items.STONE_BRICKS)) {
            transmuted = new ItemStack(Items.MOSSY_STONE_BRICKS, count);
        } else if (stack.is(Items.DIRT) || stack.is(Items.COARSE_DIRT) || stack.is(Items.ROOTED_DIRT)) {
            transmuted = new ItemStack(Items.PODZOL, count);
        } else if (stack.is(Items.DEAD_BUSH)) {
            Item chosenSapling = SAPLING_ITEMS[level.random.nextInt(SAPLING_ITEMS.length)];
            transmuted = new ItemStack(chosenSapling, count);
        } else if (stack.is(Items.COBBLESTONE)) {
            transmuted = new ItemStack(Items.OAK_WOOD, count);
        }

        if (transmuted != null) {
            itemEntity.setItem(transmuted);
            spawnEmeraldLeafBurst(level, itemEntity.position().add(0.0D, 0.25D, 0.0D), 8, 0.25D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Silencing Spore Hex (User A5): persistent anti-milk fungal curse (cleansed by Restoration/Fire magic or caster defeat);
        // permanently silences enemy spellcasting & mob special abilities while shredding 20% / 35% / 50% Armor Toughness across
        // Hex Stacks I–III with ZERO continuous HP tick damage!
        int stack = Math.clamp(SpellCombatReactionEngine.getHexStackLevel(target), 1, 3);
        double toughnessShred = switch (stack) {
            case 2 -> 0.35D;
            case 3 -> 0.50D;
            default -> 0.20D;
        };

        SpellLingeringZoneManager.applyArcaneSilence(target, 45);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, toughnessShred, 45L);
        if (target instanceof Mob mob) {
            mob.setAggressive(false);
        }

        level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xB8FF52, 32, 1.0F, 0.05F),
                target.getX(), target.getY(0.85D), target.getZ(), 6, 0.28D, 0.35D, 0.28D, 0.015D);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                target.getX(), target.getY(0.95D), target.getZ(), 4, 0.25D, 0.30D, 0.25D, 0.015D);
        return true;
    }

    // =========================================================================
    // BOTANICAL HELPERS
    // =========================================================================

    /**
     * Applies thorny briar immobilization to non-boss targets ({@code Slowness VII}, {@code Inertial Anchor},
     * jump-lock, and zeroed horizontal/upward velocity). On bosses, applies only brief {@code Slowness II}
     * without locking jumps or overwriting velocity.
     */
    public static void applyBriarRoot(LivingEntity target, int ticks) {
        if (target == null || !target.isAlive()) return;
        if (SpellCombatReactionEngine.isBossEntity(target)) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, Math.min(ticks, 30), 1, false, true));
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6, false, true));
        target.addEffect(new MobEffectInstance(
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), ticks, 0, false, false));
        EntityHelper.getPersistentData(target).putLong(
                SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, target.level().getGameTime() + ticks);
        target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
        target.hasImpulse = true;
    }

    public static void applyBriarRoot(ServerLevel level, LivingEntity target, int ticks) {
        applyBriarRoot(target, ticks);
    }

    /**
     * Returns {@code true} only for genuine allies and peaceful unprovoked woodland/village creatures.
     * Never classifies an entity as an ally if it is a {@link Monster}/{@link Enemy}, if it is actively targeting
     * {@code caster} or a friendly ally, if it is an angry {@link NeutralMob}, or if it is owned by a hostile/non-allied player.
     */
    public static boolean isFriendlyOrWoodlandAlly(LivingEntity ent, @Nullable LivingEntity caster) {
        if (ent == null || !ent.isAlive()) {
            return false;
        }
        if (ent instanceof Monster || ent instanceof Enemy) {
            return false;
        }
        if (ent instanceof Mob m && m.getTarget() != null
                && (m.getTarget() == caster
                    || (caster != null && (ApexEschatonSingularityHelper.isFriendly(m.getTarget(), caster)
                        || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(m.getTarget(), caster, true))))) {
            return false;
        }
        if (ent instanceof NeutralMob nm && (nm.isAngry()
                || (caster != null && caster.getUUID().equals(nm.getPersistentAngerTarget())))) {
            return false;
        }
        if (ent instanceof SpellEidolonEntity eidolon) {
            return caster != null && (eidolon.isOwnedBy(caster) || eidolon.isFriendlyTo(caster));
        }
        if (ent instanceof OwnableEntity ownable) {
            LivingEntity owner = ownable.getOwner();
            if (owner != null) {
                if (caster == null) {
                    return false;
                }
                return owner == caster || owner.isAlliedTo(caster);
            }
        }
        if (caster != null && (ent == caster || ent.isAlliedTo(caster))) {
            return true;
        }
        return ent instanceof Animal || ent instanceof AbstractVillager || ent instanceof AbstractGolem;
    }

    private static boolean resolveDynamicSafeguard(@Nullable SpellAssembly spell, @Nullable LivingEntity caster) {
        if (spell != null && spell.hasWardFriendlyFireSafeguard()) {
            return true;
        }
        if (caster == null) {
            return false;
        }
        for (ItemStack held : List.of(caster.getMainHandItem(), caster.getOffhandItem())) {
            if (held.isEmpty()) continue;
            CastingApparatusState appState = held.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
            if (appState != null) {
                Optional<SpellAssembly> active = appState.getActiveSpell();
                if (active.isPresent() && active.get().hasWardFriendlyFireSafeguard()) {
                    return true;
                }
            }
            SpellAssembly directSpell = held.get(ModDataComponents.SPELL_ASSEMBLY.get());
            if (directSpell != null && directSpell.hasWardFriendlyFireSafeguard()) {
                return true;
            }
        }
        return false;
    }

    private static void spawnEmeraldLeafBurst(ServerLevel level, Vec3 pos, int count, double spread) {
        level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x2CE668, 34, 1.05F, 0.09F),
                pos.x, pos.y, pos.z, count, spread, spread * 0.65D, spread, 0.035D);
    }
}
