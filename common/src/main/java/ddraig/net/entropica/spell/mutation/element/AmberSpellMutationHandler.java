package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
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
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Amber Materia (Vital Fusion #24)</b>
 * (Spellcrafting System Guide Section 6.25):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Dual Vitae + Earth harmonic matrix — Strong against
 *     <b>VOID (+1)</b> and <b>BARROW (+1)</b> (Dominance {@code amber_vitae_dom}: Strong vs Undead) |
 *     Weak against <b>NETHER (-1)</b>, <b>FROZEN (-1)</b>, and <b>UMBRAL (-1)</b>, computed dynamically
 *     at runtime by {@code VisInteractionManager}.</li>
 *     <li><b>Flow (Linger)</b>: <i>Resinite Viscous Linger Flow</i> — all Amber ground zones, resin slicks,
 *     and stasis traps persist for +50% longer duration, continuously stacking an escalating 10% slow per
 *     second (up to 50% max) on any hostile standing inside them.</li>
 *     <li><b>Projectile</b>: <i>Amber Stasis Bubble Orb</i> — aerodynamic sphere of viscous golden resin
 *     ({@code #FF6B9D} / {@code #773214}) detonating on impact into a 3-block sticky amber pool that roots
 *     caught enemies for 3.0s and permanently preserves dropped items from despawning.</li>
 *     <li><b>Beam</b>: <i>Fossilizing Stasis Torrent</i> — sustained thick stream of molten golden tree resin
 *     stacking progressive Slowness into a 2.5s solid Amber Stasis freeze after 3.0s of continuous contact,
 *     protecting dropped items from burning in fire or lava, and hardening fragile blocks for Silk Touch.</li>
 *     <li><b>Radial Burst</b>: <i>Amber Crystallization Nova</i> — 6m+ shockwave of golden resin and amber
 *     shards knocking hostiles back 4 blocks, rooting them in sticky amber for 3.0s, leaving a resin veneer
 *     (+20% ally speed / -40% hostile slow), and converting surface plants into amber (minus {@code grass_block} &amp; dirt variants).</li>
 *     <li><b>Touch / Strike</b>: <i>Fossilizing Stasis Strike</i> — glues joints in hardening resin (60% Slowness
 *     &amp; -40% attack speed for 4.0s); lethal blows crystallize the corpse into a fragile amber effigy statue
 *     that shatters for bonus amber shards.</li>
 *     <li><b>Aura / Self</b>: <i>Petrified Amber Bastion Mantle</i> — translucent hardened amber carapace granting
 *     +8 Armor, +3 Armor Toughness, 50% knockback resistance, zero equipped armor durability wear, and a
 *     40% 3.0s Slowness splash on melee attackers.</li>
 *     <li><b>Glyph / Trap</b>: <i>Amber Preservation Vault Glyph</i> — item-safekeeping rune encapsulating
 *     touching dropped items in an invulnerable amber shell preventing despawning and protecting against fire &amp; lava.</li>
 *     <li><b>Cone / Spray</b>: <i>Molten Resin Deluge</i> — 60° cone of molten golden resin &amp; amber grit knocking
 *     enemies back 3 blocks, inflicting 60% Slowness &amp; -30% armor toughness for 4.0s, leaving a jump-locking
 *     sticky ground slick, and Silk-Touch harvesting fragile blocks.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Prehistoric Resinite Monsoon</i> — tier-scaled cave-safe 10.0s scalding sap
 *     downpour blinding enemies and permanently encasing dying non-boss mobs inside translucent amber monuments.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Fossil-Warden Eidolon Suite</i> — {@code #FF6B9D} / {@code #773214}
 *     Amber-Clad Sentinel Agent (50% Slowness &amp; -30% Toughness hammer blows), Resinite Stasis Spire Sentry
 *     (8m +4 Armor &amp; item-preservation aura + slowing resin slugs), and Petrified Amber Effigy Decoy
 *     (5m shatter burst rooting hostiles for 3.0s upon defeat).</li>
 *     <li><b>Blink / Step</b>: <i>Amber Chrysalis Blink</i> — teleports up to 14+ blocks, granting +4 Armor for
 *     4.0s and leaving a 10-HP damage-absorbing amber decoy shell at departure that pops into slow resin.</li>
 *     <li><b>Transmute</b>: <i>Petrification &amp; Fossilization Transmute</i> — converts wood logs to petrified
 *     amber wood, turns flora into collectible amber plant blocks, and grants guaranteed Silk Touch on fragile blocks.</li>
 *     <li><b>Affliction</b>: <i>Petrifying Amber Stasis Hex</i> — milk-immune fossilization curse inflicting
 *     70% Slowness, blocking sprinting &amp; jumping, shredding 40% armor toughness, and suppressing all positive
 *     potion buffs until cleansed via Restoration/Fire magic or caster defeat.</li>
 * </ul>
 */
public final class AmberSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int FOSSIL_WARDEN_HEX_COLOR = 0xF29B2E;
    public static final int DEEP_EARTH_AMBER_HEX_COLOR = 0x773214;
    public static final int VITAL_ROSE_HEX_COLOR = 0xFF6B9D;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

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
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Amber Chrysalis Blink";
            case TRANSMUTE, BREAK -> "Petrification & Fossilization Transmute";
            case AFFLICTION -> "Petrifying Amber Stasis Hex";
            case RESTORATION -> "Amber Nectar Mending";
            default -> "Resinite " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // FLOW MUTATION — RESINITE VISCOUS LINGER FLOW (+50% DURATION & 10%/S -> 50% SLOW)
    // =========================================================================
    // Note: +50% duration & escalating 10%/s (up to 50% max) hostile slow on all Amber ground zones,
    // resin slicks, and stasis traps is enforced directly inside SpellLingeringZoneManager!

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.DRIPPING_HONEY,
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), VITAL_ROSE_HEX_COLOR, 18, 0.85F, 0.04F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.0D
            );
        }
    }

    // =========================================================================
    // FORM MUTATION 1 — AMBER STASIS BUBBLE ORB (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Calibrated viscous golden-resin aerodynamic sphere!
        boolean heavyViscous = spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY && spell.flowPart().mutationEnabled();
        projectile.enableSlightFluidArc(heavyViscous);
        level.playSound(null, caster.blockPosition(), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.PLAYERS, 0.85F, 1.08F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.DRIPPING_HONEY,
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), VITAL_ROSE_HEX_COLOR, 16, 0.86F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.0D
        );
        // Any dropped items brushed in flight are immediately coated against despawning!
        preserveDroppedItems(level, projectile.getBoundingBox().inflate(1.25D), false);
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
        if (hitEntity instanceof LivingEntity living && living.isAlive() && living != caster) {
            if (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
                if (!slot.hasHarmVerb()) {
                    int tier = spell.getEffectiveTier();
                    float orbDmg = (2.75F + tier * 0.82F) * spell.flawProfile().getPotencyMultiplier();
                    SpellCombatReactionEngine.hurtWithAmberMateriaAdvantage(level, living, caster, orbDmg, false, true);
                }
            }
        }
        spawnStickyAmberPool(level, caster, projectile.position(), spell, slot);
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
        spawnStickyAmberPool(level, caster, blockHit.getLocation(), spell, slot);
    }

    private static void spawnStickyAmberPool(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 impactPos,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.25 Amber Stasis Bubble Orb:
        // Detonates on impact into a 3-block sticky amber pool that roots caught enemies for 3.0s (60t)
        // and permanently preserves dropped items from despawning!
        int tier = spell.getEffectiveTier();
        BlockPos surf = findGroundSurfaceNear(level, BlockPos.containing(impactPos));
        Vec3 poolCenter = new Vec3(impactPos.x, surf.above().getY() + 0.03D, impactPos.z);

        SpellWorldVfxManager.broadcastRadialNova(level, poolCenter, 3.0D, EssenceType.AMBER, null, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, poolCenter, 3.0D, EssenceType.AMBER, null, 80, tier);

        AABB box = new AABB(
                poolCenter.x - 3.0D, poolCenter.y - 1.5D, poolCenter.z - 3.0D,
                poolCenter.x + 3.0D, poolCenter.y + 2.2D, poolCenter.z + 3.0D
        );
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
            applyAmberRoot(level, foe, 60);
        }
        preserveDroppedItems(level, box, false);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, poolCenter, 3.0D, 80, level.getGameTime(), spell, slot, EssenceType.AMBER, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    AABB zBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 2.2D, zone.center.z + zone.radius
                    );
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.DRIPPING_HONEY,
                                zone.center.x, zone.center.y + 0.2D, zone.center.z, 8, 1.45D, 0.12D, 1.45D, 0.01D);
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), VITAL_ROSE_HEX_COLOR, 16, 0.85F, 0.04F),
                                zone.center.x, zone.center.y + 0.25D, zone.center.z, 3, 1.35D, 0.15D, 1.35D, 0.01D
                        );
                        preserveDroppedItems(srv, zBox, false);
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    long jumpLockUntil = srv.getGameTime() + 18L;
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, zBox, LivingEntity::isAlive)) {
                        if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 22, 1, false, false));
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, jumpLockUntil);
                    }
                    return true;
                }
        ));
        level.playSound(null, BlockPos.containing(poolCenter), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.PLAYERS, 0.92F, 0.90F);
    }

    // =========================================================================
    // FORM MUTATION 2 — FOSSILIZING STASIS TORRENT (BEAM)
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
        // Section 6.25 Fossilizing Stasis Torrent:
        // Sustained thick stream of molten golden tree resin; stacks progressive Slowness into a 2.5s (50t)
        // solid Amber Stasis freeze after 3.0s (60 contact ticks = 15 x 4-tick pulses) of continuous contact,
        // protects dropped items from burning in fire or lava, and hardens fragile blocks for Silk Touch!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (hit == caster || !hit.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float beamDmg = (1.35F + tier * 0.42F) * spell.flawProfile().getPotencyMultiplier() * apparatusPotency;
                SpellCombatReactionEngine.hurtWithAmberMateriaAdvantage(level, hit, caster, beamDmg, false, true);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("SpellAmberBeamLastTick").orElse(-100L);
            int contactTicks = (now - last <= 16L) ? data.getInt("SpellAmberBeamTicks").orElse(0) + 4 : 4;
            data.putLong("SpellAmberBeamLastTick", now);

            if (contactTicks >= 60) {
                // 3.0 seconds of continuous resin contact -> 2.5-second (50t) solid Amber Stasis freeze!
                data.putInt("SpellAmberBeamTicks", 0);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
                SpellLingeringZoneManager.applyArcaneSilence(hit, 50);
                applyAmberRoot(level, hit, 50);
                SpellWorldVfxManager.broadcastWardShell(
                        level, hit.position().add(0.0D, hit.getBbHeight() * 0.5D, 0.0D),
                        Math.max(0.95D, hit.getBbWidth() * 1.15D), EssenceType.AMBER, null, 50, tier
                );
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEY_BLOCK.defaultBlockState()),
                        hit.getX(), hit.getY(0.6D), hit.getZ(), 18, 0.35D, 0.45D, 0.35D, 0.05D);
                level.playSound(null, hit.blockPosition(), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.PLAYERS, 1.0F, 0.72F);
                level.playSound(null, hit.blockPosition(), SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.PLAYERS, 0.85F, 1.15F);
            } else {
                data.putInt("SpellAmberBeamTicks", contactTicks);
                double slowFrac = Math.min(0.68D, 0.18D + (contactTicks / 60.0D) * 0.50D);
                SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, slowFrac, 30L);
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, Math.min(3, contactTicks / 16)));
            }
        }

        // Protect dropped items along the molten resin beam corridor against fire, lava, and despawning!
        AABB corridor = caster.getBoundingBox().expandTowards(direction.scale(maxReach)).inflate(1.65D);
        preserveDroppedItems(level, corridor, true);

        // Harden & Silk-Touch harvest fragile blocks along the beam ray and at the endpoint!
        int steps = Math.min(20, (int) Math.ceil(maxReach));
        for (int s = 1; s <= steps; s++) {
            BlockPos bp = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            silkHarvestFragileBlock(level, bp, level.getBlockState(bp));
        }
        if (blockHit != null) {
            BlockPos hitPos = blockHit.getBlockPos();
            silkHarvestFragileBlock(level, hitPos, level.getBlockState(hitPos));
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — AMBER CRYSTALLIZATION NOVA (RADIAL BURST)
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
        // Section 6.25 Amber Crystallization Nova:
        // Explosive 360° shockwave of golden resin and amber shards (6-block+ radius, #FF6B9D / #773214);
        // knocks hostiles back 4 blocks, roots them in sticky amber for 3.0s (60t), leaves a resin veneer
        // (+20% ally speed / -40% hostile slow), and converts all surface plants in radius into amber
        // (strictly excluding minecraft:grass_block and dirt/turf variants)!
        double r = Math.max(6.0D, radius);
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.AMBER, null, tier);

        Set<UUID> knockedForLandingRoot = new HashSet<>();
        AABB blastBox = new AABB(
                center.x - r, center.y - 2.0D, center.z - r,
                center.x + r, center.y + 2.8D, center.z + r
        );
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
            if (ent == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, ent)) {
                SpellCombatReactionEngine.applyTimedMovementSpeedBoost(ent, level, 0.20D, 80L);
                ent.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 0, true, false));
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float novaDmg = (3.30F + tier * 0.90F) * spell.flawProfile().getPotencyMultiplier() * apparatusPotency;
                SpellCombatReactionEngine.hurtWithAmberMateriaAdvantage(level, ent, caster, novaDmg, false, true);
            }

            if (SpellCombatReactionEngine.isBossEntity(level, ent)) {
                applyAmberRoot(level, ent, 60);
            } else {
                Vec3 diff = ent.position().subtract(center);
                Vec3 flat = new Vec3(diff.x, 0.0D, diff.z);
                Vec3 out = flat.lengthSqr() > 1.0E-4D ? flat.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                // 4-block outward knockback impulse; sticky 3.0s amber root locks onto them right as they land!
                SpellExecutionEngine.applySyncedMotion(ent, new Vec3(out.x * 1.12D, 0.32D, out.z * 1.12D));
                knockedForLandingRoot.add(ent.getUUID());
            }
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(ent, level, 0.40D, 60L);
        }

        // Convert surface plants across the full radius (strictly excluding grass_block & dirt/turf/crop variants) into Amber!
        BlockPos cPos = BlockPos.containing(center);
        int rScan = (int) Math.ceil(r);
        double rSq = r * r;
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-rScan, -2, -rScan), cPos.offset(rScan, 2, rScan))) {
            double dx = (p.getX() + 0.5D) - center.x;
            double dz = (p.getZ() + 0.5D) - center.z;
            if (dx * dx + dz * dz > rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (isConvertibleSurfacePlant(bs) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                convertPlantToAmberBlossom(level, p, bs);
            }
        }

        double veneerSearchR = r + 4.5D;
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, r, 120, level.getGameTime(), spell, slot, EssenceType.AMBER, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);

                    // Lock knocked-back hostiles into 3.0s (60t) sticky amber stasis as soon as the 4-block shove lands!
                    if (elapsed == 8 && !knockedForLandingRoot.isEmpty()) {
                        AABB landBox = new AABB(
                                zone.center.x - veneerSearchR, zone.center.y - 3.0D, zone.center.z - veneerSearchR,
                                zone.center.x + veneerSearchR, zone.center.y + 4.0D, zone.center.z + veneerSearchR
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, landBox, LivingEntity::isAlive)) {
                            if (knockedForLandingRoot.remove(foe.getUUID())) {
                                applyAmberRoot(srv, foe, 60);
                                srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEY_BLOCK.defaultBlockState()),
                                        foe.getX(), foe.getY(0.25D), foe.getZ(), 10, 0.25D, 0.15D, 0.25D, 0.04D);
                            }
                        }
                    }

                    if (elapsed % 10 == 0) {
                        AABB vBox = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 2.5D, zone.center.z + zone.radius
                        );
                        srv.sendParticles(ParticleTypes.DRIPPING_HONEY,
                                zone.center.x, zone.center.y + 0.2D, zone.center.z, 10, zone.radius * 0.45D, 0.1D, zone.radius * 0.45D, 0.01D);
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, vBox, LivingEntity::isAlive)) {
                            if (e == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)) {
                                // +20% ally movement speed on the golden resin veneer!
                                SpellCombatReactionEngine.applyTimedMovementSpeedBoost(e, srv, 0.20D, 25L);
                                e.addEffect(new MobEffectInstance(MobEffects.SPEED, 25, 0, true, false));
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)) {
                                // -40% hostile movement speed on the golden resin veneer!
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(e, srv, 0.40D, 25L);
                                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 1, false, false));
                            }
                        }
                    }
                    return true;
                }
        ));

        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEY_BLOCK.defaultBlockState()),
                center.x, center.y + 0.8D, center.z, 36, 2.2D, 0.5D, 2.2D, 0.08D);
        level.playSound(null, cPos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.PLAYERS, 1.0F, 0.85F);
        level.playSound(null, cPos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.90F, 0.92F);
    }

    // =========================================================================
    // FORM MUTATION 4 — FOSSILIZING STASIS STRIKE (TOUCH / STRIKE)
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
        // Section 6.25 Fossilizing Stasis Strike:
        // Glues the victim's joints in hardening resin (inflicting 60% Slowness and -40% attack speed for 4.0s),
        // and if lethal, crystallizes the corpse into a fragile amber effigy statue that shatters for bonus amber shards!
        int tier = spell.getEffectiveTier();
        long until = level.getGameTime() + 80L;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.22D, 0.0D), direction, 3.3D, EssenceType.AMBER, null, tier
        );

        for (LivingEntity hit : hitEntities) {
            if (hit == caster || !hit.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            // Stamp fragile amber statue marker BEFORE damage so instant one-shot kills still crystallize & shatter!
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_AMBER_FRAGILE_STATUE_UNTIL, until);
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.60D, 80L);
            SpellCombatReactionEngine.applyTimedAttackDebuff(hit, level, 0.0D, 0.40D, 80L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            hit.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 80, 1));

            if (!slot.hasHarmVerb()) {
                float strikeDmg = (3.45F + tier * 0.95F) * spell.flawProfile().getPotencyMultiplier() * apparatusPotency;
                SpellCombatReactionEngine.hurtWithAmberMateriaAdvantage(level, hit, caster, strikeDmg, true, true);
            }

            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEY_BLOCK.defaultBlockState()),
                    hit.getX(), hit.getY(0.7D), hit.getZ(), 14, 0.30D, 0.35D, 0.30D, 0.05D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.HONEY_BLOCK_HIT, SoundSource.PLAYERS, 0.95F, 0.84F);
    }

    // =========================================================================
    // FORM MUTATION 5 — PETRIFIED AMBER BASTION MANTLE (AURA / SELF)
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
        // Section 6.25 Petrified Amber Bastion Mantle:
        // Translucent carapace of hardened amber (#FF6B9D / #773214): grants +8 Armor, +3 Armor Toughness,
        // 50% knockback resistance, protects equipped armor durability from taking wear, and splashes melee
        // attackers with 40% Slowness for 3.0s!
        int dur = 200 + spell.getEffectiveTier() * 40;
        CompoundTag cData = EntityHelper.getPersistentData(caster);
        cData.putLong(SpellLingeringZoneManager.TAG_AMBER_MANTLE_UNTIL, level.getGameTime() + dur);
        snapshotArmorDurability(caster, cData);

        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 8.0D, 3.0D, 0.50D, 0.0D, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.45F, null);

        level.sendParticles(ParticleTypes.DRIPPING_HONEY,
                caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.45D, 0.50D, 0.45D, 0.03D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), VITAL_ROSE_HEX_COLOR, 26, 1.00F, 0.05F),
                caster.getX(), caster.getY(0.95D), caster.getZ(), 14, 0.45D, 0.50D, 0.45D, 0.03D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.PLAYERS, 0.90F, 0.95F);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.18F);
    }

    // =========================================================================
    // FORM MUTATION 6 — AMBER PRESERVATION VAULT GLYPH (GLYPH / TRAP)
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
        // Section 6.25 Amber Preservation Vault Glyph:
        // Inscribes an item-safekeeping rune; any dropped items that touch the glyph are encapsulated in an
        // invulnerable amber shell that prevents despawning and protects against fire and lava until collected!
        int tier = spell.getEffectiveTier();
        double vaultR = Math.max(3.5D, radius);
        BlockPos surf = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(glyphCenter.x, surf.above().getY() + 0.03D, glyphCenter.z);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        boolean hasOffensivePayload = slot.hasHarmVerb()
                || slot.primaryEffect().effect() == SpellCoreEffect.AFFLICTION;
        int maxTicks = tier >= 5 ? Integer.MAX_VALUE / 4 : (600 + tier * 120);

        SpellWorldVfxManager.broadcastArcaneSeal(level, snapped, vaultR, EssenceType.AMBER, null, Math.min(200, maxTicks), tier);

        AABB initialVaultBox = new AABB(
                snapped.x - vaultR, snapped.y - 1.5D, snapped.z - vaultR,
                snapped.x + vaultR, snapped.y + 2.6D, snapped.z + vaultR
        );
        preserveDroppedItems(level, initialVaultBox, true);

        final long[] nextPulseTick = new long[]{level.getGameTime() + 20L};
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, vaultR, maxTicks, level.getGameTime(), spell, slot, EssenceType.AMBER, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed > 0 && elapsed % 160 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 200 : (int) Math.min(200L, zone.maxDurationTicks - elapsed);
                        if (rem > 10) {
                            SpellWorldVfxManager.broadcastArcaneSeal(srv, zone.center, zone.radius, EssenceType.AMBER, null, rem, tier);
                        }
                    }

                    AABB vaultBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 2.6D, zone.center.z + zone.radius
                    );
                    if (elapsed % 5 == 0) {
                        preserveDroppedItems(srv, vaultBox, true);
                    }
                    if (elapsed % 12 == 0) {
                        srv.sendParticles(ParticleTypes.DRIPPING_HONEY,
                                zone.center.x, zone.center.y + 0.25D, zone.center.z, 5, zone.radius * 0.45D, 0.20D, zone.radius * 0.45D, 0.01D);
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), VITAL_ROSE_HEX_COLOR, 18, 0.88F, 0.04F),
                                zone.center.x, zone.center.y + 0.30D, zone.center.z, 3, zone.radius * 0.40D, 0.20D, zone.radius * 0.40D, 0.01D
                        );
                    }

                    // Armed stasis snare trigger after 1.0s (20t) arming delay!
                    if (elapsed >= 20 && srv.getGameTime() >= nextPulseTick[0]) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        List<LivingEntity> trespassers = srv.getEntitiesOfClass(LivingEntity.class, vaultBox, e ->
                                e.isAlive() && e != owner && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                        && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)));
                        if (!trespassers.isEmpty()) {
                            for (LivingEntity foe : trespassers) {
                                applyAmberRoot(srv, foe, 50);
                                if (hasOffensivePayload && zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, foe, new Vec3(0.0D, 1.0D, 0.0D),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                                    );
                                }
                            }
                            srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.PLAYERS, 0.85F, 0.95F);
                            if (hasOffensivePayload && !repeating) {
                                return false;
                            }
                            nextPulseTick[0] = srv.getGameTime() + 40L;
                        }
                    }
                    return true;
                }
        ).markDisarmableGlyph());

        level.playSound(null, BlockPos.containing(snapped), SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.PLAYERS, 0.90F, 1.05F);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — MOLTEN RESIN DELUGE (CONE / SPRAY)
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
        // Section 6.25 Molten Resin Deluge:
        // Wide 60° cone of molten golden resin and amber grit; knocks enemies back 3 blocks, inflicts
        // 60% Slowness and -30% armor toughness for 4.0s (80t), creates a sticky ground slick preventing
        // mob jumping, and hardens fragile blocks for Silk Touch!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        long until = now + 80L;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.20D, 0.0D), direction, coneRange, EssenceType.AMBER, null, tier
        );

        Vec3 flatDir = new Vec3(direction.x, 0.0D, direction.z);
        Vec3 pushDir = flatDir.lengthSqr() > 1.0E-4D ? flatDir.normalize() : new Vec3(0.0D, 0.0D, 1.0D);

        for (LivingEntity hit : hitTargets) {
            if (hit == caster || !hit.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float coneDmg = (2.85F + tier * 0.80F) * spell.flawProfile().getPotencyMultiplier() * apparatusPotency;
                SpellCombatReactionEngine.hurtWithAmberMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }

            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(pushDir.x * 0.88D, 0.24D, pushDir.z * 0.88D)); // 3-block knockback!
            }
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.60D, 80L);
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.30D, 80L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, until);
        }

        // Harden & Silk-Touch harvest fragile blocks across the 60° spray fan!
        int steps = Math.min(9, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            int halfSpan = Math.max(1, (int) Math.ceil(s * 0.55D));
            for (BlockPos p : BlockPos.betweenClosed(stepPos.offset(-halfSpan, -1, -halfSpan), stepPos.offset(halfSpan, 2, halfSpan))) {
                silkHarvestFragileBlock(level, p, level.getBlockState(p));
            }
        }

        // Register 4.0s (80t) sticky molten resin ground slick preventing jumping & maintaining 60% Slowness!
        Vec3 slickRaw = caster.position().add(pushDir.scale(Math.max(2.5D, coneRange * 0.50D)));
        BlockPos slickSurf = findGroundSurfaceNear(level, BlockPos.containing(slickRaw));
        Vec3 slickCenter = new Vec3(slickRaw.x, slickSurf.above().getY() + 0.03D, slickRaw.z);
        double slickR = Math.max(3.5D, coneRange * 0.55D);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, slickCenter, slickR, 80, now, spell, slot, EssenceType.AMBER, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.DRIPPING_HONEY,
                                zone.center.x, zone.center.y + 0.2D, zone.center.z, 10, zone.radius * 0.45D, 0.1D, zone.radius * 0.45D, 0.01D);
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    long lockUntil = srv.getGameTime() + 22L;
                    AABB slickBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 2.2D, zone.center.z + zone.radius
                    );
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, slickBox, LivingEntity::isAlive)) {
                        if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                        SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.60D, 25L);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 2, false, false));
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, lockUntil);
                    }
                    return true;
                }
        ));
        level.playSound(null, caster.blockPosition(), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.PLAYERS, 1.0F, 0.78F);
    }

    // =========================================================================
    // FORM MUTATION 8 — PREHISTORIC RESINITE MONSOON (SKYFALL / RAIN)
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
        // Section 6.25 Prehistoric Resinite Monsoon:
        // Summons a cave-safe, tier-scaled downpour of scalding liquid sap for 10.0s (200t);
        // blinds enemies and permanently encases any non-boss mob that dies in the storm inside
        // a translucent amber statue monument!
        int tier = spell.getEffectiveTier();
        double r = Math.max(8.0D, radius);
        BlockPos groundSurf = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        Vec3 groundCenter = new Vec3(impactCenter.x, groundSurf.above().getY() + 0.03D, impactCenter.z);
        double canopyH = resolveServerSkyfallHeight(level, groundCenter, tier, 9.2D);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, groundCenter, r, EssenceType.AMBER, null, 200, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, groundCenter, r, EssenceType.AMBER, null, 200, tier);
        level.playSound(null, BlockPos.containing(groundCenter), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.PLAYERS, 1.1F, 0.75F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, groundCenter, r, 200, level.getGameTime(), spell, slot, EssenceType.AMBER, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    // Refresh 3D skyfall VFX if Resinite Viscous Linger Flow extends duration beyond 200 ticks!
                    if (elapsed == 195 && zone.maxDurationTicks > 205) {
                        int extra = (int) Math.min(200L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastSkyfallPillar(srv, zone.center, zone.radius, EssenceType.AMBER, null, extra, tier);
                        SpellWorldVfxManager.broadcastArcaneSeal(srv, zone.center, zone.radius, EssenceType.AMBER, null, extra, tier);
                    }

                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.DRIPPING_HONEY,
                                zone.center.x, zone.center.y + Math.min(5.5D, canopyH - 0.5D), zone.center.z,
                                24, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.04D);
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float potency = (zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F) * zone.apparatusPotency;
                        long markUntil = srv.getGameTime() + 50L;

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + canopyH + 0.5D, zone.center.z + zone.radius
                        );
                        preserveDroppedItems(srv, area, true);

                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            // Stamp permanent amber monument tag BEFORE dealing monsoon damage so lethal sap ticks fossilize immediately!
                            EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_AMBER_FOSSILIZE_UNTIL, markUntil);
                            v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 55, 0));
                            v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.35D, 30L);

                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F
                                );
                            }
                            if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                float sapDmg = (1.85F + tier * 0.52F) * potency;
                                SpellCombatReactionEngine.hurtWithAmberMateriaAdvantage(srv, v, owner, sapDmg, false, true);
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — FOSSIL-WARDEN EIDOLON SUITE (AGENT / SENTRY / DECOY)
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
        tag.putString("SpellEidolonMateria", EssenceType.AMBER.name());
        tag.putInt("SpellEidolonTintRGB", FOSSIL_WARDEN_HEX_COLOR);

        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.65D, radius * 0.35D), EssenceType.AMBER, null, tier
        );
        level.sendParticles(ParticleTypes.DRIPPING_HONEY,
                spawnPos.x, spawnPos.y + 0.85D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.04D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), VITAL_ROSE_HEX_COLOR, 28, 1.05F, 0.05F),
                spawnPos.x, spawnPos.y + 0.95D, spawnPos.z, 16, 0.45D, 0.55D, 0.45D, 0.03D
        );
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.PLAYERS, 0.92F, 0.95F);
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
        // Section 6.25 Amber Chrysalis Blink:
        // Teleports up to 14+ blocks away, leaving an amber decoy shell at departure that absorbs 10 damage
        // before popping into slow resin, while granting the caster +4 Armor for 4.0s (80t)!
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.0D, 0.0D, 80L);

        BlockPos dep = BlockPos.containing(departurePos);
        boolean placedFeet = false;
        boolean placedHead = false;
        if (level.getBlockState(dep).isAir() && !SpellExecutionEngine.isProtectedBlockOrContainer(level, dep, level.getBlockState(dep))) {
            level.setBlockAndUpdate(dep, Blocks.HONEY_BLOCK.defaultBlockState());
            placedFeet = true;
        }
        if (level.getBlockState(dep.above()).isAir() && !SpellExecutionEngine.isProtectedBlockOrContainer(level, dep.above(), level.getBlockState(dep.above()))) {
            level.setBlockAndUpdate(dep.above(), Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
            placedHead = true;
        }

        SpellWorldVfxManager.broadcastWardShell(
                level, departurePos.add(0.0D, 1.0D, 0.0D), 1.05D, EssenceType.AMBER, null, 80, potencyTier
        );
        level.playSound(null, dep, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.PLAYERS, 0.90F, 1.05F);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), VITAL_ROSE_HEX_COLOR, 20, 0.92F, 0.04F),
                arrivalPos.x, arrivalPos.y + 0.85D, arrivalPos.z, 12, 0.35D, 0.45D, 0.35D, 0.02D
        );

        final boolean fFeet = placedFeet;
        final boolean fHead = placedHead;
        final BlockPos immDep = dep.immutable();
        final float[] absorbedDamage = new float[]{0.0F};

        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 4.0D, 80, level.getGameTime(), dummy, dummy.subSpells().get(0), EssenceType.AMBER, 1.0F,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB shellBox = new AABB(
                            departurePos.x - 1.5D, departurePos.y - 0.5D, departurePos.z - 1.5D,
                            departurePos.x + 1.5D, departurePos.y + 2.5D, departurePos.z + 1.5D
                    );
                    AABB lureBox = new AABB(
                            departurePos.x - 6.0D, departurePos.y - 2.0D, departurePos.z - 6.0D,
                            departurePos.x + 6.0D, departurePos.y + 3.0D, departurePos.z + 6.0D
                    );

                    if (elapsed < zone.maxDurationTicks) {
                        boolean broken = (fFeet && !srv.getBlockState(immDep).is(Blocks.HONEY_BLOCK))
                                || (fHead && !srv.getBlockState(immDep.above()).is(Blocks.ORANGE_STAINED_GLASS));

                        // Absorb incoming enemy projectiles striking the 10-HP chrysalis decoy shell!
                        for (Projectile proj : srv.getEntitiesOfClass(Projectile.class, shellBox, Entity::isAlive)) {
                            Entity projOwner = proj.getOwner();
                            if (projOwner != owner && !(projOwner instanceof LivingEntity liv && SpellExecutionEngine.isFriendlyAllyForOasis(owner, liv))) {
                                absorbedDamage[0] += 4.0F;
                                proj.discard();
                                srv.playSound(null, immDep, SoundEvents.HONEY_BLOCK_HIT, SoundSource.PLAYERS, 0.85F, 1.05F);
                            }
                        }

                        // Lure nearby hostile monsters to strike the decoy chrysalis, absorbing up to 10.0 HP!
                        if (!broken && elapsed >= 4) {
                            for (Mob mob : srv.getEntitiesOfClass(Mob.class, lureBox, Mob::isAlive)) {
                                if (mob instanceof Monster && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, mob)) {
                                    if (elapsed % 8 == 0) {
                                        mob.getNavigation().moveTo(departurePos.x, departurePos.y, departurePos.z, 1.15D);
                                    }
                                    if (elapsed % 10 == 0 && mob.position().distanceToSqr(departurePos) <= 4.0D) {
                                        absorbedDamage[0] += 3.5F;
                                        srv.playSound(null, immDep, SoundEvents.HONEY_BLOCK_HIT, SoundSource.PLAYERS, 0.80F, 0.95F);
                                    }
                                }
                            }
                        }

                        if (!broken && absorbedDamage[0] < 10.0F) {
                            return true;
                        }
                    }

                    // Pop the chrysalis into a 4m sticky slow resin burst!
                    if (fFeet && srv.getBlockState(immDep).is(Blocks.HONEY_BLOCK)) srv.removeBlock(immDep, false);
                    if (fHead && srv.getBlockState(immDep.above()).is(Blocks.ORANGE_STAINED_GLASS)) srv.removeBlock(immDep.above(), false);

                    AABB splash = new AABB(
                            departurePos.x - 4.0D, departurePos.y - 1.5D, departurePos.z - 4.0D,
                            departurePos.x + 4.0D, departurePos.y + 2.5D, departurePos.z + 4.0D
                    );
                    long noJump = srv.getGameTime() + 60L;
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, splash, LivingEntity::isAlive)) {
                        if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                        SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.60D, 60L);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, noJump);
                    }
                    srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEY_BLOCK.defaultBlockState()),
                            departurePos.x, departurePos.y + 1.0D, departurePos.z, 26, 1.0D, 0.6D, 1.0D, 0.06D);
                    srv.playSound(null, immDep, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.90F, 1.15F);
                    srv.playSound(null, immDep, SoundEvents.HONEY_BLOCK_BREAK, SoundSource.PLAYERS, 0.95F, 0.88F);
                    return false;
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
        // Section 6.25 Petrification & Fossilization Transmute:
        // Converts wood logs to petrified wood, turns flora into collectible amber plant blocks,
        // and grants guaranteed Silk Touch on fragile blocks!
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }

        // 1. Logs & wood -> Petrified Amber-Wood blocks (preserving pillar axis)!
        if (state.is(BlockTags.LOGS)
                && !state.is(ModBlocks.AMBER_LOG.get())
                && !state.is(ModBlocks.AMBER_WOOD.get())
                && !state.is(ModBlocks.STRIPPED_AMBER_LOG.get())
                && !state.is(ModBlocks.STRIPPED_AMBER_WOOD.get())) {
            String key = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
            Block targetBlock;
            if (key.contains("stripped") && (key.endsWith("wood") || key.endsWith("hyphae"))) {
                targetBlock = ModBlocks.STRIPPED_AMBER_WOOD.get();
            } else if (key.contains("stripped")) {
                targetBlock = ModBlocks.STRIPPED_AMBER_LOG.get();
            } else if (key.endsWith("wood") || key.endsWith("hyphae")) {
                targetBlock = ModBlocks.AMBER_WOOD.get();
            } else {
                targetBlock = ModBlocks.AMBER_LOG.get();
            }
            BlockState next = targetBlock.defaultBlockState();
            if (state.hasProperty(RotatedPillarBlock.AXIS) && next.hasProperty(RotatedPillarBlock.AXIS)) {
                next = next.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
            }
            level.setBlockAndUpdate(pos, next);
            maybeGrantAmberBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }

        // 2. Planks, wooden slabs, leaves & saplings -> Petrified / Amber variants!
        if (state.is(BlockTags.WOODEN_SLABS) && !state.is(Blocks.PETRIFIED_OAK_SLAB) && !state.is(ModBlocks.AMBER_SLAB.get())) {
            BlockState petrifiedSlab = Blocks.PETRIFIED_OAK_SLAB.defaultBlockState();
            if (state.hasProperty(SlabBlock.TYPE)) {
                petrifiedSlab = petrifiedSlab.setValue(SlabBlock.TYPE, state.getValue(SlabBlock.TYPE));
            }
            if (state.hasProperty(SlabBlock.WATERLOGGED)) {
                petrifiedSlab = petrifiedSlab.setValue(SlabBlock.WATERLOGGED, state.getValue(SlabBlock.WATERLOGGED));
            }
            level.setBlockAndUpdate(pos, petrifiedSlab);
            maybeGrantAmberBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.PLANKS) && !state.is(ModBlocks.AMBER_PLANKS.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AMBER_PLANKS.get().defaultBlockState());
            maybeGrantAmberBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.LEAVES) && !state.is(ModBlocks.AMBER_LEAVES.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AMBER_LEAVES.get().defaultBlockState());
            maybeGrantAmberBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.SAPLINGS) && !state.is(ModBlocks.AMBER_SAPLING.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AMBER_SAPLING.get().defaultBlockState());
            maybeGrantAmberBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }

        // 3. Surface flora (strictly excluding grass_block & dirt/turf variants) -> collectible Amber Nectar Blossom!
        if (isConvertibleSurfacePlant(state)) {
            convertPlantToAmberBlossom(level, pos, state);
            maybeGrantAmberBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }

        // 4. Fragile blocks -> guaranteed Silk Touch harvest!
        if (silkHarvestFragileBlock(level, pos, state)) {
            maybeGrantAmberBatchTransmuteDiscount(level, caster, potencyTier);
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
        int count = in.getCount();
        ItemStack out = ItemStack.EMPTY;

        if (in.is(ItemTags.LOGS) && !in.is(ModBlocks.AMBER_LOG.get().asItem())) {
            out = new ItemStack(ModBlocks.AMBER_LOG.get().asItem(), count);
        } else if (in.is(ItemTags.PLANKS) && !in.is(ModBlocks.AMBER_PLANKS.get().asItem())) {
            out = new ItemStack(ModBlocks.AMBER_PLANKS.get().asItem(), count);
        } else if (in.is(ItemTags.LEAVES) && !in.is(ModBlocks.AMBER_LEAVES.get().asItem())) {
            out = new ItemStack(ModBlocks.AMBER_LEAVES.get().asItem(), count);
        } else if (in.is(ItemTags.SAPLINGS) && !in.is(ModBlocks.AMBER_SAPLING.get().asItem())) {
            out = new ItemStack(ModBlocks.AMBER_SAPLING.get().asItem(), count);
        } else if (in.is(ItemTags.SMALL_FLOWERS) && !in.is(ModBlocks.AMBER_NECTAR_BLOSSOM.get().asItem())) {
            out = new ItemStack(ModBlocks.AMBER_NECTAR_BLOSSOM.get().asItem(), count);
        } else if (in.is(Items.HONEYCOMB) || in.is(Items.SLIME_BALL) || in.is(Items.RESIN_CLUMP)) {
            out = new ItemStack(ModItems.AMBER_CHUNK.get(), count);
        }

        if (out.isEmpty()) {
            return false;
        }
        itemEntity.setItem(out);
        itemEntity.setUnlimitedLifetime();
        level.sendParticles(ParticleTypes.DRIPPING_HONEY,
                itemEntity.getX(), itemEntity.getY(0.35D), itemEntity.getZ(), 8, 0.20D, 0.20D, 0.20D, 0.02D);
        maybeGrantAmberBatchTransmuteDiscount(level, caster, potencyTier);
        return true;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.25 Petrifying Amber Stasis Hex:
        // Persistent milk-immune fossilization curse (cleansed strictly via Restoration/Fire magic or caster defeat):
        // permanently inflicts 70% Slowness, blocks sprinting and jumping, shreds 40% armor toughness,
        // and suppresses all positive potion buffs!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.70D, 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 3, true, true));
        target.setSprinting(false);
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 45L);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);

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

        float hexDmg = (1.35F + potencyTier * 0.45F) * mult;
        SpellCombatReactionEngine.hurtWithAmberMateriaAdvantage(level, target, null, hexDmg, false, true);

        level.sendParticles(ParticleTypes.DRIPPING_HONEY,
                target.getX(), target.getY(0.8D), target.getZ(), 5, 0.25D, 0.30D, 0.25D, 0.01D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), VITAL_ROSE_HEX_COLOR, 18, 0.88F, 0.04F),
                target.getX(), target.getY(0.9D), target.getZ(), 3, 0.25D, 0.30D, 0.25D, 0.01D
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
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(target, level, 4.0D, 2.0D, 0.25D, 0.0D, 160L);
            float mend = 3.2F + potencyTier * 1.0F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, mend);
            target.heal(mend);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static void snapshotArmorDurability(LivingEntity entity, CompoundTag data) {
        for (EquipmentSlot eq : ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(eq);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                data.putInt("SpellAmberArmorSnap_" + eq.getName(), stack.getDamageValue());
            }
        }
    }

    private static void applyAmberRoot(ServerLevel level, LivingEntity target, int ticks) {
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.95D, ticks);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6));
        target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), ticks, 0));
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + ticks);
        SpellExecutionEngine.applySyncedMotion(target, new Vec3(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D));
    }

    private static void preserveDroppedItems(ServerLevel level, AABB box, boolean fireImmune) {
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box, ItemEntity::isAlive)) {
            item.setUnlimitedLifetime();
            if (fireImmune) {
                item.clearFire();
                item.setInvulnerable(true);
                item.setGlowingTag(true);
                BlockPos ip = item.blockPosition();
                if (level.getFluidState(ip).is(FluidTags.LAVA)) {
                    Vec3 cur = item.getDeltaMovement();
                    item.setDeltaMovement(cur.x * 0.5D, Math.max(0.12D, cur.y), cur.z * 0.5D);
                    item.hasImpulse = true;
                }
            }
        }
    }

    private static boolean isConvertibleSurfacePlant(BlockState bs) {
        if (bs.isAir()
                || bs.is(Blocks.GRASS_BLOCK)
                || bs.is(BlockTags.DIRT)
                || bs.is(Blocks.PODZOL)
                || bs.is(Blocks.MYCELIUM)
                || bs.is(Blocks.MOSS_BLOCK)
                || bs.is(Blocks.FARMLAND)
                || bs.is(Blocks.DIRT_PATH)
                || bs.is(ModBlocks.AMBER_NECTAR_BLOSSOM.get())) {
            return false;
        }
        if (bs.getBlock() instanceof DoublePlantBlock && bs.hasProperty(DoublePlantBlock.HALF)
                && bs.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            return false;
        }
        return bs.is(BlockTags.SMALL_FLOWERS)
                || bs.is(BlockTags.FLOWERS)
                || bs.is(Blocks.SHORT_GRASS)
                || bs.is(Blocks.TALL_GRASS)
                || bs.is(Blocks.FERN)
                || bs.is(Blocks.LARGE_FERN)
                || bs.is(Blocks.DEAD_BUSH)
                || (bs.getBlock() instanceof BushBlock && !bs.is(BlockTags.CROPS) && !bs.is(BlockTags.SAPLINGS));
    }

    private static void convertPlantToAmberBlossom(ServerLevel level, BlockPos pos, BlockState bs) {
        if (bs.getBlock() instanceof DoublePlantBlock && level.getBlockState(pos.above()).is(bs.getBlock())) {
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 18);
        }
        level.setBlockAndUpdate(pos, ModBlocks.AMBER_NECTAR_BLOSSOM.get().defaultBlockState());
    }

    private static boolean silkHarvestFragileBlock(ServerLevel level, BlockPos pos, BlockState bs) {
        if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, bs)) {
            return false;
        }
        Item blockItem = bs.getBlock().asItem();
        if (blockItem == Items.AIR) {
            return false;
        }
        if (bs.is(BlockTags.ICE)
                || bs.is(BlockTags.IMPERMEABLE)
                || bs.is(Blocks.GLOWSTONE)
                || bs.is(Blocks.SEA_LANTERN)
                || bs.is(Blocks.AMETHYST_CLUSTER)
                || bs.is(Blocks.LARGE_AMETHYST_BUD)
                || bs.is(Blocks.MEDIUM_AMETHYST_BUD)
                || bs.is(Blocks.SMALL_AMETHYST_BUD)
                || bs.is(BlockTags.CORALS)
                || bs.is(BlockTags.CORAL_BLOCKS)
                || bs.is(Blocks.TURTLE_EGG)
                || bs.is(Blocks.GLOW_LICHEN)) {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new ItemStack(blockItem, 1));
            level.sendParticles(ParticleTypes.DRIPPING_HONEY,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5, 0.25D, 0.25D, 0.25D, 0.01D);
            return true;
        }
        return false;
    }

    private static void maybeGrantAmberBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellAmberBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellAmberBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellAmberBatchTick", now);
        pData.putInt("SpellAmberBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.AMBER, refund);
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

    private static double resolveServerSkyfallHeight(ServerLevel level, Vec3 ground, int tier, double tier1BaseHeight) {
        double desired = tier1BaseHeight + (Math.clamp(tier, 1, 10) - 1) * 2.2D;
        BlockPos baseBlock = BlockPos.containing(ground.x, ground.y + 0.2D, ground.z);
        int maxScan = (int) Math.ceil(desired) + 1;
        for (int dy = 4; dy <= maxScan; dy++) {
            BlockPos check = baseBlock.above(dy);
            BlockState bs = level.getBlockState(check);
            if (!bs.isAir() && !bs.getCollisionShape(level, check).isEmpty()) {
                return Math.max(3.6D, dy - 0.50D);
            }
        }
        return desired;
    }
}
