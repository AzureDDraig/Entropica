package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.AutocastPedestalBlockEntity;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.network.SpellVfxEventPayload;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModItems;
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
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Nether Materia (Base Type #6)</b>
 * (Spellcrafting System Guide Sections 1.2A, 1.2B, 1.7, 2.2, 2.3, 4.1–4.9, 5.1–5.6 &amp; 6.7):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Strong against <b>FROZEN (+1)</b> | Weak against <b>WATER (-1)</b>.</li>
 *     <li><b>Projectile</b>: <i>Netherfire Comet</i> — roaring molten magma orb wrapped in basalt-slag plates that
 *     flashes water into steam and explodes on impact (or max-range airburst), dealing Nether Materia damage
 *     (+1 vs Frozen, -1 vs Water) with linear distance falloff, igniting entities for 5.0s, alerting dungeon hostiles
 *     (Section 1.7), auto-smelting dropped items, and leaving a temporary pool of fire on hit ground.</li>
 *     <li><b>Beam</b>: <i>Nether Tendril Tether</i> — burning braided chain of hellfire that latches onto the target
 *     within a flexible 45-degree leash cone while moving, draining their life force as thermal energy to recharge
 *     Nether Materia in the caster's Orbis Cell proportional to actual damage dealt.</li>
 *     <li><b>Radial Burst</b>: <i>Hellfire Ring Shockwave</i> — 6-block 360-degree wave of explosive netherfire,
 *     knocking hostiles backward, igniting all hostiles, auto-smelting dropped item stacks on the ground, melting
 *     ice/snow, and leaving a dense 24-point perimeter ring of blazing fire blocks.</li>
 *     <li><b>Touch / Strike</b>: <i>Cauterizing Cleave &amp; Auto-Smelt</i> — converts melee strike to pure Nether
 *     Materia damage (stacking held weapon Materia Advantage) with a blazing 3D crescent cleave; ignites primary and
 *     cleave targets with unquenchable Netherfire for 6.0s; strikes that directly kill the creature cook meat drops
 *     and pre-smelt raw ores across equipment and inventories.</li>
 *     <li><b>Aura / Self</b>: <i>Immolation Shroud</i> — registers defense as Nether (+1 vs Frozen -> 50% damage,
 *     -1 vs Water -> 150% damage), grants 100% immunity to fire, lava, and magma blocks, enables smooth every-tick
 *     water-like swimming buoyancy in lava, sets melee attackers ablaze with unquenchable fire, and melts nearby
 *     ice and snow automatically (mobile or Sneak-Cast anchored field).</li>
 *     <li><b>Glyph / Trap</b>: <i>Magma Puddle Trap</i> — 1.0s arming delay, visible volcanic caldera seal, 10m
 *     (or Tier 5+ indefinite) persistence, 50% Sneak-Right-Click disarm refund, and Repeating Sentry support; turns
 *     the floor under the trap into a contained non-spilling 3x3 pool of real Lava for 4.0 seconds (65% movement slow)
 *     before permanently solidifying into Obsidian around trapped mobs' legs.</li>
 *     <li><b>Cone / Spray</b>: <i>Combustion Backdraft</i> — short-range 60-degree explosive cone blasting hostiles
 *     backward with heavy knockback, incinerating incoming arrows, leaving scorched fire patches, and pushing the
 *     caster safely backward with smooth fall-resetting recoil.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Brimstone Meteor Rain</i> — opens a cave-ceiling-adaptive volcanic sky-rift scaling
 *     from 6.0s (120t) at Tier 1 up to 13.2s (264t) at Tier 10 alongside circular tier-scaled radius, raining 3D
 *     exploding netherfire meteors that stagger enemies with Nether Materia damage (+1 vs Frozen), set the area ablaze,
 *     auto-smelt ground loot, and vaporize standing water pools into steam.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Infernal Eidolon Suite</i> — volcanic red-orange (#B83C08) triad: dashing
 *     burning-claw Agent that explodes into fire on death, ballistic Magma Mortar Sentry lobbing explosive fireballs,
 *     and Blazing Wicker Effigy Decoy that sears attackers and detonates in a fiery burst on destruction.</li>
 *     <li><b>Flow (Chains)</b>: <i>Arcing Fire-Whip Chains</i> — roaring 3D flame whips leaving a continuous 6.0s line
 *     of burning fire blocks on the ground between chained victims, igniting and searing hostiles crossing the path.</li>
 *     <li><b>Blink / Step</b>: <i>Thermal Pop</i> — roaring 3D pillar of flame and 5.0s 3x3 fire pool at departure
 *     plus circular 3.5-block explosive arrival shockwave pop and ice/snow melting.</li>
 *     <li><b>Transmute &amp; Break</b>: <i>Thermal Pyrolysis</i> — auto-smelts raw ores (with virtual Fortune I–IV and
 *     up to 30% batch fuel discount), fuses Sand into Glass and Clay into Bricks, solidifies lava rims into Magma Blocks,
 *     chars wooden planks and invalid organic targets into Cinder Ash Flakes, primes TNT, smelts Logistics raw ores in
 *     transit, and vaporizes Water into high-pressure steam.</li>
 *     <li><b>Affliction</b>: <i>Hellfire Sear Hex</i> — unquenchable Nether curse burning perpetually (even boiling
 *     underwater) until extinguished via Water/Restoration magic or caster defeat; melts 30% / 45% / 60% of target
 *     physical armor points across Hex Levels I–III and disables natural health regeneration (immune to milk buckets).</li>
 * </ul>
 */
public final class NetherSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int INFERNAL_EIDOLON_HEX_COLOR = 0xB83C08;
    private static final int MOLTEN_GOLD_HEX_COLOR = 0xFFD15C;
    private static final int BASALT_SLAG_HEX_COLOR = 0x2A1712;

    /** Persistent guard tag so held raw ores on a dying creature are only pre-smelted once. */
    private static final String TAG_HELD_ORES_SMELTED = "SpellNetherHeldOresSmelted";

    // Generous 3D world-space VFX durations so players can admire volcanic & hellfire formations
    private static final int VFX_FIRE_WHIP_CHAIN_TICKS = 58;
    private static final int VFX_COMET_IMPACT_TICKS = 62;
    private static final int VFX_HELLFIRE_NOVA_TICKS = 68;
    private static final int VFX_HELLFIRE_SEAL_TICKS = 64;
    private static final int VFX_CAUTERIZING_SLASH_TICKS = 54;
    private static final int VFX_BACKDRAFT_CONE_TICKS = 56;
    private static final int VFX_MAGMA_GEYSER_TICKS = 58;
    private static final int VFX_BRIMSTONE_STORM_TICKS = 120;
    private static final int VFX_SUMMON_PILLAR_TICKS = 60;
    private static final int VFX_THERMAL_POP_TICKS = 60;

    @Override
    public EssenceType materiaType() {
        return EssenceType.NETHER;
    }

    // =========================================================================
    // CANONICAL SIGNATURE TITLES & GUIDE DESCRIPTIONS (SECTION 6.7)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Arcing Fire-Whip Chains";
            default -> "Hellfire Brimstone " + flow.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Arcing Fire-Whip Chains: Leaping chains manifest as roaring 3D flame whips that leave a blazing line of fire blocks on the ground between chained victims, igniting and searing hostiles that cross the path.";
            default -> null;
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Netherfire Comet";
            case BEAM -> "Nether Tendril Tether";
            case RADIAL_BURST -> "Hellfire Ring Shockwave";
            case TOUCH_STRIKE -> "Cauterizing Cleave & Auto-Smelt";
            case AURA -> "Immolation Shroud";
            case GLYPH -> "Magma Puddle Trap";
            case CONE -> "Combustion Backdraft";
            case SKYFALL -> "Brimstone Meteor Rain";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public @Nullable String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Netherfire Comet: Fires a roaring molten magma orb that explodes on impact, dealing Nether Materia damage (+1 vs Frozen, -1 vs Water), igniting entities for 5s, auto-smelting dropped items, and leaving a temporary pool of fire.";
            case BEAM -> "Nether Tendril Tether: Latches a flexible 45° burning chain of hellfire onto the target, draining their life force as thermal energy to recharge the caster's Orbis Cell proportional to actual damage dealt.";
            case RADIAL_BURST -> "Hellfire Ring Shockwave: Detonates a 6-block 360° wave of explosive netherfire, knocking hostiles back, igniting hostiles, auto-smelting ground item stacks, and leaving a ring of blazing fire blocks.";
            case TOUCH_STRIKE -> "Cauterizing Cleave & Auto-Smelt: Converts melee strike to Nether Materia damage and ignites primary and cleave foes in unquenchable Netherfire for 6s; kills from the strike cook meat drops and pre-smelt held raw ores.";
            case AURA -> "Immolation Shroud: Registers defense as Nether (+1 vs Frozen, -1 vs Water); grants 100% fire/lava immunity, smooth water-like lava swimming buoyancy, melee attacker ignition, and automatic melting of nearby ice and snow.";
            case GLYPH -> "Magma Puddle Trap: Turns the floor under the trap into a contained non-spilling 3x3 pool of molten lava for 4 seconds (65% slow), permanently solidifying into Obsidian around trapped mobs' legs.";
            case CONE -> "Combustion Backdraft: Unleashes a short-range explosive cone that blasts hostiles backward with heavy knockback while pushing the caster safely backward with recoil.";
            case SKYFALL -> "Brimstone Meteor Rain: Rains exploding netherfire meteors from a cave-viable rift over 6.0s–13.2s across Tiers 1–10, staggering enemies with Nether Materia damage (+1 vs Frozen), setting the area ablaze, and vaporizing standing water pools.";
            case SUMMON_AGENT -> "Infernal Eidolon (Agent): Volcanic red-orange (#B83C08) striker dashing at targets with burning claws, igniting them, and exploding into fire upon death.";
            case SUMMON_SENTRY -> "Infernal Magma Mortar (Sentry): Anchored basalt cauldron (#B83C08) lobbing ballistic explosive magma fireballs at distant hostiles.";
            case SUMMON_DECOY -> "Blazing Wicker Effigy (Decoy): Burning wicker effigy (#B83C08) that sears attackers who strike it and detonates into a violent fiery burst upon destruction.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Thermal Pop";
            case TRANSMUTE, BREAK -> "Thermal Pyrolysis";
            case AFFLICTION -> "Hellfire Sear Hex";
            default -> "Hellfire " + effect.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Thermal Pop: Teleport leaves a roaring 3D pillar of flame and 5s 3x3 fire pool at departure and detonates a circular 3.5-block explosive arrival shockwave pop.";
            case TRANSMUTE, BREAK -> "Thermal Pyrolysis: Auto-smelts raw ores (with virtual Fortune I–IV and up to 30% batch fuel discount), turns Sand into Glass and Clay into Bricks, solidifies lava rims, chars wooden planks and invalid organics to Ash, primes TNT, and vaporizes Water into high-pressure steam.";
            case AFFLICTION -> "Hellfire Sear Hex: Unquenchable Nether curse burning perpetually (even underwater) until extinguished via Water/Restoration magic or caster defeat; melts 30% / 45% / 60% of physical Armor across Hex Levels I–III and locks natural health regeneration (milk-immune).";
            default -> null;
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — ARCING FIRE-WHIP CHAINS (CHAIN_LEAP)
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
        Vec3 startEye = fromTarget.position().add(0.0D, fromTarget.getBbHeight() * 0.55D, 0.0D);
        Vec3 endEye = toTarget.position().add(0.0D, toTarget.getBbHeight() * 0.55D, 0.0D);

        // Generous 58-tick 3D braided flame whip with serrated hellfire barbs!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_CHAIN_ARC,
                startEye, endEye, EssenceType.NETHER, null,
                1.0F, VFX_FIRE_WHIP_CHAIN_TICKS, tier
        );

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), MOLTEN_GOLD_HEX_COLOR, 38, 1.08F, 0.08F),
                endEye.x, endEye.y, endEye.z, 14, 0.32D, 0.32D, 0.32D, 0.05D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_KENAZ.get(), MOLTEN_GOLD_HEX_COLOR, 28, 1.05F, 0.05F),
                endEye.x, endEye.y + 0.15D, endEye.z, 4, 0.25D, 0.25D, 0.25D, 0.02D
        );
        level.playSound(null, toTarget.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.80F, 1.16F);
        level.playSound(null, toTarget.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.65F, 1.28F);

        if (spell.prefix().enforcesZeroEntityHarm()) {
            return;
        }

        boolean safeWard = spell.hasWardFriendlyFireSafeguard();
        Vec3 start = fromTarget.position();
        Vec3 end = toTarget.position();
        double dist = start.distanceTo(end);
        Vec3 step = dist > 1.0E-4D ? end.subtract(start).normalize() : new Vec3(1.0D, 0.0D, 0.0D);

        List<BlockPos> placedFireLine = new ArrayList<>();
        List<Vec3> pathCheckpoints = new ArrayList<>();
        for (double d = 0.35D; d <= dist; d += 0.72D) {
            Vec3 sample = start.add(step.scale(d));
            pathCheckpoints.add(sample);
            BlockPos sampleBlock = BlockPos.containing(sample);
            // Melt thin snow/ice crusts along the fire-whip's ground path before igniting!
            meltSnowOrThinIceAt(level, sampleBlock);
            meltSnowOrThinIceAt(level, sampleBlock.below());
            BlockPos bp = findGroundFireSpot(level, sampleBlock);
            if (bp != null) {
                level.setBlockAndUpdate(bp, Blocks.FIRE.defaultBlockState());
                placedFireLine.add(bp.immutable());
            }
        }

        if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(toTarget, caster, safeWard)
                && !isPassiveOrFriendlyEntity(toTarget, caster)) {
            toTarget.igniteForSeconds(5);
            EntityHelper.getPersistentData(toTarget).putLong(
                    SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L
            );
        }

        // Maintain the blazing fire-whip ground trail for 6.0s (120t), igniting & searing any hostile crossing the path!
        Vec3 midpoint = start.add(end).scale(0.5D);
        double boundRad = Math.max(2.5D, dist * 0.55D + 1.25D);
        float whipCrossDmg = 1.4F + tier * 0.45F;

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, midpoint, boundRad, 120, level.getGameTime(), spell, slot, EssenceType.NETHER, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        for (BlockPos fp : placedFireLine) {
                            if (srv.getBlockState(fp).is(Blocks.FIRE)) {
                                srv.removeBlock(fp, false);
                            }
                        }
                        return false;
                    }
                    if (elapsed % 8 == 0 && !pathCheckpoints.isEmpty()) {
                        if (zone.spell != null && zone.spell.prefix().enforcesZeroEntityHarm()) {
                            return true;
                        }
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zoneWard = (zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard()) || safeWard;
                        Set<UUID> searedThisPulse = new HashSet<>();
                        for (Vec3 cp : pathCheckpoints) {
                            if (srv.random.nextFloat() < 0.28F) {
                                srv.sendParticles(ParticleTypes.FLAME, cp.x, cp.y + 0.22D, cp.z,
                                        2, 0.18D, 0.12D, 0.18D, 0.015D);
                            }
                            AABB segBox = new AABB(cp.x - 0.88D, cp.y - 1.25D, cp.z - 0.88D,
                                    cp.x + 0.88D, cp.y + 1.80D, cp.z + 0.88D);
                            for (LivingEntity crosser : srv.getEntitiesOfClass(LivingEntity.class, segBox, LivingEntity::isAlive)) {
                                if (crosser == owner || !searedThisPulse.add(crosser.getUUID())) continue;
                                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(crosser, owner, zoneWard)) {
                                    continue;
                                }
                                if (isPassiveOrFriendlyEntity(crosser, owner)) {
                                    continue;
                                }
                                crosser.igniteForSeconds(5);
                                if (elapsed % 16 == 0) {
                                    SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(srv, crosser, owner, whipCrossDmg, false);
                                }
                            }
                        }
                    }
                    return true;
                }
        ));
    }

    // =========================================================================
    // FORM MUTATION 1 — NETHERFIRE COMET (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        Vec3 pos = projectile.position();
        level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z,
                2, 0.09D, 0.09D, 0.09D, 0.015D);
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), INFERNAL_EIDOLON_HEX_COLOR, 26, 0.95F, 0.06F),
                    pos.x, pos.y, pos.z, 2, 0.10D, 0.10D, 0.10D, 0.02D
            );
            level.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z,
                    1, 0.06D, 0.06D, 0.06D, 0.01D);
        }
        // Sublimate snow layers grazed in flight
        BlockPos curPos = projectile.blockPosition();
        BlockState curState = level.getBlockState(curPos);
        if (curState.is(Blocks.SNOW) || curState.is(Blocks.POWDER_SNOW)) {
            level.removeBlock(curPos, false);
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
        int tier = spell.getEffectiveTier();
        Vec3 impactPos = hitEntity.position().add(0.0D, hitEntity.getBbHeight() * 0.45D, 0.0D);
        detonateNetherfireComet(level, caster, impactPos, hitEntity, spell, slot, tier);
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
        int tier = spell.getEffectiveTier();
        Vec3 impactPos = blockHit.getLocation();
        detonateNetherfireComet(level, caster, impactPos, null, spell, slot, tier);
    }

    @Override
    public void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Detonate cleanly on Timed Fuse / On-Proximity / Outer-Rim max travel distance stops (0m..128m)!
        int tier = spell.getEffectiveTier();
        detonateNetherfireComet(level, caster, projectile.position(), null, spell, slot, tier);
    }

    private static void detonateNetherfireComet(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 impactPos,
            @Nullable Entity directHit,
            SpellAssembly spell,
            SubSpellSlot slot,
            int tier
    ) {
        // Generous 62-tick 3D erupting molten magma crown & 8 outward-arcing lava splash plumes!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                impactPos, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.NETHER, null, 2.55F, VFX_COMET_IMPACT_TICKS, tier
        );
        level.sendParticles(ParticleTypes.LAVA, impactPos.x, impactPos.y + 0.2D, impactPos.z, 16, 0.50D, 0.35D, 0.50D, 0.06D);
        level.sendParticles(ParticleTypes.FLAME, impactPos.x, impactPos.y + 0.2D, impactPos.z, 30, 0.95D, 0.45D, 0.95D, 0.08D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), MOLTEN_GOLD_HEX_COLOR, 36, 1.15F, 0.09F),
                impactPos.x, impactPos.y + 0.35D, impactPos.z, 16, 0.65D, 0.40D, 0.65D, 0.05D
        );
        level.playSound(null, BlockPos.containing(impactPos), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.88F, 1.12F);
        level.playSound(null, BlockPos.containing(impactPos), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.85F, 0.95F);

        // Section 1.7 Sonic & Luminescent Threat Emission: loud Nether fireballs alert nearby dungeon hostiles!
        emitInfernalThreatPulse(level, caster, impactPos, spell, 26.0D);

        double blastRadius = 3.2D;
        AABB blastBox = new AABB(
                impactPos.x - blastRadius, impactPos.y - 1.8D, impactPos.z - blastRadius,
                impactPos.x + blastRadius, impactPos.y + 2.5D, impactPos.z + blastRadius
        );

        if (!spell.prefix().enforcesZeroEntityHarm()) {
            boolean hasHarm = hasHarmEffect(spell, slot);
            float flawPot = spell.flawProfile().getPotencyMultiplier();
            float cometBaseDmg = Math.max(3.8F + tier * 1.20F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.58F) * flawPot;

            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
                if (victim == caster) continue;
                if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                        victim, caster, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                double dist = victim.position().distanceTo(impactPos);
                if (dist > blastRadius + 0.45D) continue;

                // Section 5.1 Linear Distance Falloff: 100% at epicenter -> 33% at outermost perimeter!
                float distFactor = (victim == directHit)
                        ? 1.0F
                        : (float) Mth.clamp(1.0D - (dist / blastRadius) * 0.67D, 0.33D, 1.0D);

                victim.igniteForSeconds(5);
                if (!hasHarm) {
                    SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(
                            level, victim, caster, cometBaseDmg * distFactor, true
                    );
                }
                if (victim != directHit) {
                    SpellExecutionEngine.deliverSubSpellOnEntity(
                            level, caster, victim, victim.position().subtract(impactPos),
                            spell, slot, EssenceType.NETHER, distFactor * 0.65F
                    );
                }
            }
        }

        // Boil shallow water pockets & melt snow/ice at impact site before placing temporary fire pool
        BlockPos centerBlock = BlockPos.containing(impactPos);
        vaporizeWaterAndMeltIceInRadius(level, centerBlock, 2);
        autoSmeltDroppedItems(level, blastBox.inflate(0.85D));
        spawnTemporaryFirePool(level, caster, centerBlock, 1, 100, 0.78F);
    }

    // =========================================================================
    // FORM MUTATION 2 — NETHER TENDRIL TETHER (BEAM)
    // =========================================================================

    /**
     * Resolves any active 45-degree flexible hellfire tether target for {@code caster} during continuous beam channeling
     * (Guide Sections 4.2 &amp; 6.7: stays anchored to the latched target within a 45° angle while moving!).
     */
    @Nullable
    public static LivingEntity resolveLatchedTetherTarget(
            ServerLevel level,
            LivingEntity caster,
            Vec3 rayStart,
            Vec3 direction,
            double maxReach
    ) {
        CompoundTag casterData = EntityHelper.getPersistentData(caster);
        long now = level.getGameTime();
        if (now > casterData.getLong(SpellLingeringZoneManager.TAG_NETHER_TETHER_UNTIL).orElse(0L)) {
            return null;
        }
        String uuidStr = casterData.getString(SpellLingeringZoneManager.TAG_NETHER_TETHER_TARGET_UUID).orElse("");
        if (uuidStr.isEmpty()) {
            return null;
        }
        try {
            Entity found = level.getEntity(UUID.fromString(uuidStr));
            if (!(found instanceof LivingEntity living) || !living.isAlive() || living == caster) {
                return null;
            }
            Vec3 targetCenter = living.position().add(0.0D, living.getBbHeight() * 0.55D, 0.0D);
            Vec3 toTarget = targetCenter.subtract(rayStart);
            double dist = toTarget.length();
            if (dist < 0.2D || dist > maxReach + 1.5D) {
                return null;
            }
            Vec3 normLook = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
            // cos(45°) = 0.7071 -> flexible 45-degree locking leash cone!
            if (toTarget.scale(1.0D / dist).dot(normLook) < 0.707D || !caster.hasLineOfSight(living)) {
                return null;
            }
            return living;
        } catch (IllegalArgumentException ignored) {
            return null;
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
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean hasHarm = hasHarmEffect(spell, slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float tetherDrainDmg = (1.85F + tier * 0.58F) * Math.max(0.25F, apparatusPotency) * flawPot;

        Vec3 eye = caster.getEyePosition();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 casterHand = eye.subtract(0.0D, 0.25D, 0.0D);

        // Vaporize grazed water pockets, melt ice/snow, and light unlit campfires/candles along the beam path
        double envReach = blockHit.getType() == HitResult.Type.BLOCK
                ? Math.min(maxReach, eye.distanceTo(blockHit.getLocation()) + 0.5D)
                : maxReach;
        for (double d = 1.4D; d <= envReach; d += 1.25D) {
            BlockPos bp = BlockPos.containing(eye.add(normDir.scale(d)));
            BlockState bs = level.getBlockState(bp);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, bp, bs)) continue;
            if (bs.is(Blocks.WATER) || bs.is(Blocks.BUBBLE_COLUMN)) {
                level.removeBlock(bp, false);
                level.sendParticles(ParticleTypes.CLOUD, bp.getX() + 0.5D, bp.getY() + 0.5D, bp.getZ() + 0.5D,
                        5, 0.2D, 0.2D, 0.2D, 0.03D);
            } else if (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.SNOW) || bs.is(Blocks.POWDER_SNOW)) {
                level.removeBlock(bp, false);
            } else if (bs.getBlock() instanceof CampfireBlock && !bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(bp, bs.setValue(CampfireBlock.LIT, true));
            } else if (bs.getBlock() instanceof CandleBlock && !bs.getValue(CandleBlock.LIT)) {
                level.setBlockAndUpdate(bp, bs.setValue(CandleBlock.LIT, true));
            }
        }

        if (spell.prefix().enforcesZeroEntityHarm()) {
            return;
        }

        // Refresh or acquire flexible 45° hellfire tether lock on primary struck target!
        List<LivingEntity> tetheredTargets = new ArrayList<>(hitEntities);
        if (tetheredTargets.isEmpty()) {
            LivingEntity latched = resolveLatchedTetherTarget(level, caster, eye, normDir, maxReach);
            if (latched != null) {
                tetheredTargets.add(latched);
            }
        }

        boolean latchedPrimary = false;
        float baseHp = Math.max(1.0F, MateriaStageTable.getBaseCombatDamageHp(tier));
        long baseCastCost = MateriaStageTable.getBaseMfumCastCost(tier);

        for (LivingEntity hit : tetheredTargets) {
            if (hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (!latchedPrimary) {
                latchedPrimary = true;
                CompoundTag cTag = EntityHelper.getPersistentData(caster);
                cTag.putString(SpellLingeringZoneManager.TAG_NETHER_TETHER_TARGET_UUID, hit.getUUID().toString());
                cTag.putLong(SpellLingeringZoneManager.TAG_NETHER_TETHER_UNTIL, now + 18L);
            }

            boolean alreadyStruckBySlotHarm = hasHarm && hitEntities.contains(hit);
            float actualDamageDealt = 0.0F;

            if (!alreadyStruckBySlotHarm) {
                float prePool = hit.getHealth() + hit.getAbsorptionAmount();
                float preWard = EntityHelper.getPersistentData(hit).getFloat("SpellWardHp").orElse(0.0F);
                boolean hurt = SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(level, hit, caster, tetherDrainDmg, false);
                if (hurt) {
                    float postPool = hit.isAlive() ? (hit.getHealth() + hit.getAbsorptionAmount()) : 0.0F;
                    float postWard = EntityHelper.getPersistentData(hit).getFloat("SpellWardHp").orElse(0.0F);
                    actualDamageDealt = Math.max(0.0F, (prePool - postPool) + Math.max(0.0F, preWard - postWard));
                }
            } else {
                boolean damagedThisTick = (!hit.isAlive() || hit.isDeadOrDying()
                        || (hit.hurtTime > 0 && hit.hurtTime == hit.hurtDuration))
                        && !hit.isInvulnerable();
                if (damagedThisTick) {
                    float advMult = SpellCombatReactionEngine.calculateSpellAdvantageDamageMultiplier(
                            EssenceType.NETHER, caster, hit, SpellCombatReactionEngine.resolveEntityAffinities(hit), 0.0F
                    );
                    if (advMult > 0.0F) {
                        actualDamageDealt = Math.max(0.0F, baseHp * Math.max(0.25F, apparatusPotency) * flawPot * advMult);
                    }
                }
            }

            hit.igniteForSeconds(5);

            // Broadcast coiling 3D hellfire tendril chain latching from target back into caster!
            Vec3 hitChest = hit.position().add(0.0D, hit.getBbHeight() * 0.55D, 0.0D);
            SpellWorldVfxManager.broadcast(
                    level, SpellVfxEventPayload.VFX_CHAIN_ARC,
                    casterHand, hitChest, EssenceType.NETHER, null, 0.95F, 22, tier
            );

            // Recharge ONLY the caster's Orbis Cell fuel (no direct player HP heal) strictly proportional to > 0 damage dealt!
            if (actualDamageDealt > 0.0F) {
                Vec3 backVec = casterHand.subtract(hitChest);
                for (int m = 1; m <= 3; m++) {
                    Vec3 orbPos = hitChest.add(backVec.scale(m * 0.26D));
                    level.sendParticles(
                            TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), MOLTEN_GOLD_HEX_COLOR, 18, 1.05F, 0.04F),
                            orbPos.x, orbPos.y, orbPos.z, 2, 0.08D, 0.08D, 0.08D, 0.01D
                    );
                }

                if (caster instanceof Player player && player.isAlive()) {
                    double dmgRatio = Math.min(3.0D, actualDamageDealt / baseHp);
                    long restoredFuel = Math.max(1L, Math.round(baseCastCost * 0.35D * dmgRatio));
                    SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.NETHER, restoredFuel);
                }
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), MOLTEN_GOLD_HEX_COLOR, 28, 1.0F, 0.06F),
                        caster.getX(), caster.getY(0.85D), caster.getZ(), 5, 0.25D, 0.30D, 0.25D, 0.03D
                );
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — HELLFIRE RING SHOCKWAVE (RADIAL_BURST)
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
        double blastRadius = Math.max(6.0D, radius); // Canonical 6-block minimum radius!
        int tier = spell.getEffectiveTier();

        // Generous 68-tick 3D roaring 20-tongue hellfire ring nova & volcanic caldera floor seal!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                center, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.NETHER, null, (float) blastRadius, VFX_HELLFIRE_NOVA_TICKS, tier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                center.add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.NETHER, null, (float) (blastRadius * 0.92D), VFX_HELLFIRE_SEAL_TICKS, tier
        );

        emitInfernalThreatPulse(level, caster, center, spell, 28.0D);

        AABB box = new AABB(
                center.x - blastRadius, center.y - 2.2D, center.z - blastRadius,
                center.x + blastRadius, center.y + 3.0D, center.z + blastRadius
        );

        if (!spell.prefix().enforcesZeroEntityHarm()) {
            boolean hasHarm = hasHarmEffect(spell, slot);
            boolean safeWard = spell.hasWardFriendlyFireSafeguard();
            float flawPot = spell.flawProfile().getPotencyMultiplier();
            double blastRadiusSq = blastRadius * blastRadius;

            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                if (entity == caster) continue;
                double dx = entity.getX() - center.x;
                double dz = entity.getZ() - center.z;
                double distSq = dx * dx + dz * dz;
                if (distSq > blastRadiusSq) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, safeWard)) {
                    continue;
                }
                if (isPassiveOrFriendlyEntity(entity, caster)) {
                    continue;
                }

                double dist = Math.sqrt(distSq);
                // Section 5.1 Linear Distance Falloff: 100% at epicenter -> 33% at outermost perimeter!
                float distFactor = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, blastRadius)) * 0.67D, 0.33D, 1.0D);

                entity.igniteForSeconds(5);
                if (!hasHarm) {
                    float baseDmg = Math.max(4.2F + tier * 1.30F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.58F)
                            * distFactor * apparatusPotency;
                    SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(level, entity, caster, baseDmg * flawPot, true);
                }
                if (dist > radius) {
                    SpellExecutionEngine.deliverSubSpellOnEntity(
                            level, caster, entity, direction, spell, slot, fallbackMateria, apparatusPotency * distFactor
                    );
                }
                if (!SpellCombatReactionEngine.isBossEntity(level, entity)) {
                    Vec3 delta = entity.position().subtract(center);
                    Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
                    Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                    SpellExecutionEngine.applySyncedMotion(entity, new Vec3(push.x * 1.32D, 0.40D, push.z * 1.32D));
                }
            }
        }

        // Melt snow/ice & auto-smelt all dropped item stacks on the ground within the 6m+ wave!
        BlockPos cPos = BlockPos.containing(center);
        meltIceAndSnowInRadius(level, cPos, (int) Math.ceil(blastRadius));
        autoSmeltDroppedItems(level, box);

        // Leave a dense 24-point perimeter ring of blazing fire blocks around the shockwave (auto-cleaned after 6.0s)
        double fireRingRad = Math.max(3.5D, blastRadius - 0.75D);
        List<BlockPos> ringFires = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            double rad = (Math.PI * 2.0D * i) / 24.0D;
            BlockPos sample = cPos.offset(
                    (int) Math.round(Math.cos(rad) * fireRingRad),
                    0,
                    (int) Math.round(Math.sin(rad) * fireRingRad)
            );
            BlockPos fp = findGroundFireSpot(level, sample);
            if (fp != null) {
                level.setBlockAndUpdate(fp, Blocks.FIRE.defaultBlockState());
                ringFires.add(fp.immutable());
            }
        }
        if (!ringFires.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, center, blastRadius, 120, level.getGameTime(), null, null, EssenceType.NETHER, 1.0F,
                    (srv, z, el) -> {
                        if (el >= z.maxDurationTicks) {
                            for (BlockPos fp : ringFires) {
                                if (srv.getBlockState(fp).is(Blocks.FIRE)) {
                                    srv.removeBlock(fp, false);
                                }
                            }
                            return false;
                        }
                        return true;
                    }
            ));
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), INFERNAL_EIDOLON_HEX_COLOR, 44, 1.18F, 0.10F),
                center.x, center.y + 0.55D, center.z, 38, blastRadius * 0.48D, 0.45D, blastRadius * 0.48D, 0.08D
        );
        level.sendParticles(ParticleTypes.LAVA, center.x, center.y + 0.35D, center.z,
                22, blastRadius * 0.40D, 0.35D, blastRadius * 0.40D, 0.06D);
        level.playSound(null, cPos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.95F, 0.95F);
        level.playSound(null, cPos, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 0.85F);
    }

    // =========================================================================
    // FORM MUTATION 4 — CAUTERIZING CLEAVE & AUTO-SMELT (TOUCH_STRIKE)
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
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 slashOrigin = caster.getEyePosition().subtract(0.0D, 0.28D, 0.0D).add(normDir.scale(0.95D));

        // Generous 54-tick 3D blazing infernal crescent cleave!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_CRESCENT_SLASH,
                slashOrigin, normDir, EssenceType.NETHER, null,
                2.65F, VFX_CAUTERIZING_SLASH_TICKS, tier
        );

        if (spell.prefix().enforcesZeroEntityHarm()) {
            for (LivingEntity hit : hitEntities) {
                EntityHelper.getPersistentData(hit).remove(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL);
            }
            return;
        }

        boolean safeWard = spell.hasWardFriendlyFireSafeguard();
        for (int idx = 0; idx < hitEntities.size(); idx++) {
            LivingEntity hit = hitEntities.get(idx);
            CompoundTag data = EntityHelper.getPersistentData(hit);
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeWard)) {
                data.remove(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL);
                continue;
            }
            boolean isPrimary = (idx == 0);

            // Both primary and secondary cleave targets receive the full 6.0s (120t) of unquenchable Netherfire!
            data.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL,
                    Math.max(now + 120L, data.getLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL).orElse(0L)));
            // Stamp auto-smelt ONLY for the immediate strike resolution (never a 6.5s unrelated-death window)!
            data.putLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL, now + 2L);
            hit.igniteForSeconds(6);

            // Cook meat drops & pre-smelt held/inventory raw ores strictly when the strike itself kills the creature!
            if (!hit.isAlive() || hit.isDeadOrDying()) {
                dropHeldRawOresPreSmelted(level, hit);
                autoSmeltDroppedItems(level, hit.getBoundingBox().inflate(3.2D));
            } else if (!isPrimary || !(caster instanceof Player)) {
                data.remove(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL);
            } else {
                // Allow SpellCombatReactionEngine's immediate same-tick melee weapon conversion to resolve, then clear if still alive!
                final LivingEntity trackedTarget = hit;
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        caster, trackedTarget.position(), 0.5D, 1, now, null, null, EssenceType.NETHER, 1.0F,
                        (srv, z, el) -> {
                            if (el >= 1) {
                                if (trackedTarget.isAlive() && !trackedTarget.isDeadOrDying()) {
                                    EntityHelper.getPersistentData(trackedTarget).remove(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL);
                                }
                                return false;
                            }
                            return true;
                        }
                ));
            }

            SpellWorldVfxManager.broadcast(
                    level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                    hit.position().add(0.0D, hit.getBbHeight() * 0.5D, 0.0D),
                    normDir, EssenceType.NETHER, null, isPrimary ? 1.45F : 1.05F, 48, tier
            );
            level.sendParticles(ParticleTypes.LAVA, hit.getX(), hit.getY(0.55D), hit.getZ(),
                    isPrimary ? 10 : 5, 0.25D, 0.30D, 0.25D, 0.04D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.92F, 1.08F);
        level.playSound(null, caster.blockPosition(), SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 0.45F, 1.32F);
    }

    // =========================================================================
    // FORM MUTATION 5 — IMMOLATION SHROUD (AURA)
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
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 50;
        long until = level.getGameTime() + dur;

        caster.clearFire();
        caster.setTicksFrozen(0);
        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL,
                Math.max(until, data.getLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL).orElse(0L)));
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.65F, null);

        // Immediately melt nearby snow & ice upon igniting Immolation Shroud
        meltIceAndSnowInRadius(level, caster.blockPosition(), 3);

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                caster.position().add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.NETHER, null, (float) Math.max(1.8D, Math.min(3.5D, radius)), 60, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), MOLTEN_GOLD_HEX_COLOR, 40, 1.12F, 0.10F),
                caster.getX(), caster.getY(0.85D), caster.getZ(), 26, 0.48D, 0.55D, 0.48D, 0.05D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_KENAZ.get(), MOLTEN_GOLD_HEX_COLOR, 34, 1.10F, 0.06F),
                caster.getX(), caster.getY(0.95D), caster.getZ(), 8, 0.42D, 0.45D, 0.42D, 0.02D
        );
        level.sendParticles(ParticleTypes.FLAME, caster.getX(), caster.getY(0.8D), caster.getZ(),
                24, 0.45D, 0.55D, 0.45D, 0.05D);
        level.playSound(null, caster.blockPosition(), SoundEvents.BLAZE_AMBIENT, SoundSource.PLAYERS, 0.85F, 1.15F);
        level.playSound(null, caster.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.75F, 0.95F);
    }

    // =========================================================================
    // FORM MUTATION 6 — MAGMA PUDDLE TRAP (GLYPH)
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
        int tier = spell.getEffectiveTier();
        double trapRad = Math.max(2.6D, radius);
        BlockPos surfaceCenter = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 anchoredCenter = new Vec3(surfaceCenter.getX() + 0.5D, surfaceCenter.getY() + 1.02D, surfaceCenter.getZ() + 0.5D);

        // Broadcast visible volcanic basalt-caldera runic floor sigil (Section 4.6)
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                anchoredCenter, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.NETHER, null, (float) (trapRad * 0.85D), 220, tier
        );
        level.playSound(null, surfaceCenter, SoundEvents.BASALT_PLACE, SoundSource.PLAYERS, 0.90F, 0.85F);

        String formSub = slot.formPart().subMode().toLowerCase(Locale.ROOT);
        final boolean repeatingSentry = formSub.contains("repeat") || formSub.contains("sentry");
        final int maxCharges = repeatingSentry ? 3 : 1;
        final float chargePotencyScale = repeatingSentry ? 0.60F : 1.0F;
        int glyphLifetime = tier >= 5 ? (Integer.MAX_VALUE / 4) : 12_000;
        final int[] chargesUsed = {0};
        final int[] rearmUntil = {20}; // Section 4.6: 1.0-second (20-tick) arming delay!
        final Map<BlockPos, BlockState> originalBasinFloor = new HashMap<>();

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchoredCenter, trapRad, glyphLifetime, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < rearmUntil[0]) return true;
                    if (elapsed % 4 != 0) return true;

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean safeWard = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB box = new AABB(
                            zone.center.x - 1.85D, zone.center.y - 0.85D, zone.center.z - 1.85D,
                            zone.center.x + 1.85D, zone.center.y + 2.15D, zone.center.z + 1.85D
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box,
                            e -> e.isAlive() && e != owner
                                    && !(e instanceof SpellEidolonEntity eid && owner != null && (eid.isOwnedBy(owner) || eid.isFriendlyTo(owner)))
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, safeWard))
                                    && !isPassiveOrFriendlyEntity(e, owner));
                    if (stepped.isEmpty()) return true;

                    // Erupt contained non-spilling 3x3 real Lava pool across the trap floor!
                    Set<BlockPos> puddleCells = new HashSet<>(9);
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            puddleCells.add(surfaceCenter.offset(dx, 0, dz).immutable());
                        }
                    }

                    // Snapshot 5x5 outer border & sub-floor neighbors so outward-flowing lava is contained every tick
                    Map<BlockPos, BlockState> containmentNeighbors = new HashMap<>();
                    for (int dx = -2; dx <= 2; dx++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            BlockPos rim = surfaceCenter.offset(dx, 0, dz).immutable();
                            if (!puddleCells.contains(rim)) {
                                BlockState rimState = srv.getBlockState(rim);
                                if (!rimState.is(Blocks.LAVA)) {
                                    containmentNeighbors.putIfAbsent(rim, rimState);
                                }
                            }
                            BlockPos under = surfaceCenter.offset(dx, -1, dz).immutable();
                            BlockState underState = srv.getBlockState(under);
                            if (!underState.is(Blocks.LAVA)) {
                                containmentNeighbors.putIfAbsent(under, underState);
                            }
                        }
                    }

                    List<BlockPos> placedCells = new ArrayList<>(9);
                    for (BlockPos p : puddleCells) {
                        BlockState cur = srv.getBlockState(p);
                        if (SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, cur) || cur.getDestroySpeed(srv, p) < 0.0F) {
                            continue;
                        }
                        originalBasinFloor.putIfAbsent(p, cur);
                        // Clear snow/fire directly above the molten pool
                        BlockPos aboveP = p.above();
                        BlockState aboveState = srv.getBlockState(aboveP);
                        if (aboveState.is(Blocks.SNOW) || aboveState.is(Blocks.FIRE)) {
                            srv.removeBlock(aboveP, false);
                        }
                        srv.setBlockAndUpdate(p, Blocks.LAVA.defaultBlockState());
                        placedCells.add(p);
                    }

                    SpellWorldVfxManager.broadcast(
                            srv, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                            zone.center, new Vec3(0.0D, 1.0D, 0.0D),
                            EssenceType.NETHER, null, 2.9F, 60, tier
                    );
                    SpellWorldVfxManager.broadcastGeyserPlume(
                            srv, zone.center, 1.6D, 2.9D, EssenceType.NETHER, null, VFX_MAGMA_GEYSER_TICKS, tier
                    );
                    srv.sendParticles(ParticleTypes.LAVA, zone.center.x, zone.center.y + 0.3D, zone.center.z,
                            26, 1.0D, 0.35D, 1.0D, 0.08D);
                    srv.playSound(null, surfaceCenter, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.PLAYERS, 1.15F, 0.85F);

                    float effPot = zone.apparatusPotency * chargePotencyScale;
                    boolean zeroHarm = zone.spell != null && zone.spell.prefix().enforcesZeroEntityHarm();
                    for (LivingEntity v : stepped) {
                        if (!zeroHarm) {
                            v.igniteForSeconds(5);
                            // Exact 65% movement slow without compounding Slowness IV!
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.65D, 80L);
                        }
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, effPot
                            );
                        }
                    }

                    // Hold the contained 3x3 real Lava pool for 4.0s (80t), then permanently solidify into Obsidian around trapped mobs' legs!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 2.4D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, effPot,
                            (msrv, mzone, melapsed) -> {
                                // Every tick: contain the 3x3 Lava pool so it never spills outward or downward!
                                for (Map.Entry<BlockPos, BlockState> nb : containmentNeighbors.entrySet()) {
                                    BlockState curNb = msrv.getBlockState(nb.getKey());
                                    if (curNb.is(Blocks.LAVA)
                                            || (curNb.is(Blocks.COBBLESTONE) && !nb.getValue().is(Blocks.COBBLESTONE))) {
                                        msrv.setBlockAndUpdate(nb.getKey(), nb.getValue());
                                    }
                                }

                                if (melapsed < 80) {
                                    if (melapsed % 5 == 0) {
                                        boolean mZeroHarm = mzone.spell != null && mzone.spell.prefix().enforcesZeroEntityHarm();
                                        if (!mZeroHarm) {
                                            LivingEntity mOwner = mzone.resolveCaster(msrv);
                                            boolean mWard = mzone.spell != null && mzone.spell.hasWardFriendlyFireSafeguard();
                                            AABB poolBox = new AABB(
                                                    mzone.center.x - 1.85D, mzone.center.y - 1.35D, mzone.center.z - 1.85D,
                                                    mzone.center.x + 1.85D, mzone.center.y + 1.50D, mzone.center.z + 1.85D
                                            );
                                            for (LivingEntity trapped : msrv.getEntitiesOfClass(LivingEntity.class, poolBox, LivingEntity::isAlive)) {
                                                if (trapped == mOwner) continue;
                                                if (mOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(trapped, mOwner, mWard)) {
                                                    continue;
                                                }
                                                if (isPassiveOrFriendlyEntity(trapped, mOwner)) {
                                                    continue;
                                                }
                                                trapped.igniteForSeconds(4);
                                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(trapped, msrv, 0.65D, 25L);
                                                if (trapped.getDeltaMovement().y > 0.02D) {
                                                    Vec3 curVel = trapped.getDeltaMovement();
                                                    trapped.setDeltaMovement(curVel.x, -0.04D, curVel.z);
                                                    trapped.hasImpulse = true;
                                                }
                                                if (melapsed % 20 == 0) {
                                                    SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(
                                                            msrv, trapped, mOwner, (2.4F + tier * 0.60F) * chargePotencyScale, false
                                                    );
                                                }
                                            }
                                        }
                                        msrv.sendParticles(ParticleTypes.LAVA, mzone.center.x, mzone.center.y + 0.05D, mzone.center.z,
                                                5, 0.95D, 0.10D, 0.95D, 0.02D);
                                    }
                                    return true;
                                }

                                // Permanently solidify the 3x3 Lava pool into Obsidian right around trapped mobs' legs!
                                for (BlockPos p : placedCells) {
                                    BlockState s = msrv.getBlockState(p);
                                    if (s.is(Blocks.LAVA) || s.isAir()) {
                                        msrv.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
                                        msrv.sendParticles(ParticleTypes.LARGE_SMOKE, p.getX() + 0.5D, p.getY() + 1.05D, p.getZ() + 0.5D,
                                                4, 0.2D, 0.15D, 0.2D, 0.02D);
                                    }
                                }
                                originalBasinFloor.keySet().removeAll(placedCells);
                                msrv.playSound(null, surfaceCenter, SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.95F);
                                return false;
                            }
                    ));

                    chargesUsed[0]++;
                    if (chargesUsed[0] >= maxCharges) {
                        return false;
                    }
                    // Repeating Glyphic Sentry: 4.0s pool + 2.0s re-arm = 120 ticks before next eruption!
                    rearmUntil[0] = elapsed + 120;
                    return true;
                }
        ).markDisarmableGlyph()
                .withOnDisarm((srv, z) -> {
                    for (Map.Entry<BlockPos, BlockState> entry : originalBasinFloor.entrySet()) {
                        BlockState cur = srv.getBlockState(entry.getKey());
                        if (cur.is(Blocks.LAVA)) {
                            srv.setBlockAndUpdate(entry.getKey(), entry.getValue());
                        }
                    }
                });

        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — COMBUSTION BACKDRAFT (CONE)
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
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        double effRange = Math.clamp(coneRange, 5.5D, 8.0D);
        boolean zeroHarm = spell.prefix().enforcesZeroEntityHarm();
        boolean hasHarm = hasHarmEffect(spell, slot);
        boolean safeWard = spell.hasWardFriendlyFireSafeguard();
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float blastDmg = Math.max(4.0F + tier * 1.20F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.52F) * apparatusPotency * flawPot;

        Vec3 eye = caster.getEyePosition();
        Vec3 muzzlePos = eye.subtract(0.0D, 0.22D, 0.0D);

        // Generous 56-tick 3D roaring Combustion Backdraft hellfire cone & muzzle blast ring!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_VORTEX_CONE,
                muzzlePos, normDir, EssenceType.NETHER, null,
                (float) effRange, VFX_BACKDRAFT_CONE_TICKS, tier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                muzzlePos.add(normDir.scale(0.65D)), normDir,
                EssenceType.NETHER, null, 1.95F, 48, tier
        );

        emitInfernalThreatPulse(level, caster, muzzlePos, spell, 24.0D);

        AABB coneBox = caster.getBoundingBox().expandTowards(normDir.scale(effRange)).inflate(effRange * 0.58D);
        if (!zeroHarm) {
            // Sweep full 60-degree fan (cos(30°) = 0.866, using 0.82 for edge forgiveness) for both hitTargets & flanking hostiles
            Set<UUID> blasted = new HashSet<>();
            for (LivingEntity hit : hitTargets) {
                if (!hit.isAlive() || hit == caster) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeWard)) {
                    continue;
                }
                if (isPassiveOrFriendlyEntity(hit, caster)) {
                    continue;
                }
                blasted.add(hit.getUUID());
                applyBackdraftBlastToTarget(level, caster, hit, normDir, blastDmg, !hasHarm);
            }

            for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, coneBox, LivingEntity::isAlive)) {
                if (cand == caster || blasted.contains(cand.getUUID())) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, safeWard)) {
                    continue;
                }
                if (isPassiveOrFriendlyEntity(cand, caster)) {
                    continue;
                }
                Vec3 toCand = cand.getBoundingBox().getCenter().subtract(eye);
                double dist = toCand.length();
                if (dist > 0.1D && dist <= effRange + 0.65D && toCand.scale(1.0D / dist).dot(normDir) >= 0.82D) {
                    blasted.add(cand.getUUID());
                    SpellExecutionEngine.deliverSubSpellOnEntity(level, caster, cand, normDir, spell, slot, fallbackMateria, apparatusPotency);
                    applyBackdraftBlastToTarget(level, caster, cand, normDir, blastDmg, !hasHarm);
                }
            }
        }

        // Incinerate / blast back incoming hostile arrows & projectiles caught in the explosive backdraft!
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, coneBox, Entity::isAlive)) {
            if (proj.getOwner() == caster || proj instanceof SpellProjectileEntity sp && sp.getOwner() == caster) continue;
            Vec3 toProj = proj.position().subtract(eye);
            double pDist = toProj.length();
            if (pDist > 0.1D && pDist <= effRange + 0.5D && toProj.scale(1.0D / pDist).dot(normDir) >= 0.78D) {
                if (!zeroHarm && proj instanceof AbstractArrow arrow) {
                    arrow.igniteForSeconds(5);
                }
                SpellExecutionEngine.applySyncedMotion(proj, new Vec3(normDir.x * 1.85D, 0.30D, normDir.z * 1.85D));
            }
        }

        // Safe client-synced caster backward recoil vault (preserved even for Utility-prefixed spells)!
        caster.resetFallDistance();
        SpellExecutionEngine.applySyncedMotion(
                caster,
                new Vec3(-normDir.x * 0.98D, Math.max(0.30D, -normDir.y * 0.48D), -normDir.z * 0.98D)
        );

        // Vaporize shallow water, melt snow/ice, and leave temporary scorched fire patches along the 60° blast cone (auto-cleaned after 5.0s)
        List<BlockPos> coneFires = new ArrayList<>();
        int steps = Math.min(7, (int) Math.ceil(effRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(normDir.scale(s)));
            int halfSpan = Math.max(1, (int) Math.ceil(s * 0.52D));
            for (BlockPos p : BlockPos.betweenClosed(stepPos.offset(-halfSpan, -1, -halfSpan), stepPos.offset(halfSpan, 1, halfSpan))) {
                BlockState bs = level.getBlockState(p);
                if (bs.is(Blocks.WATER)) {
                    level.removeBlock(p, false);
                } else if (bs.is(Blocks.SNOW) || bs.is(Blocks.POWDER_SNOW) || bs.is(Blocks.FROSTED_ICE)) {
                    level.removeBlock(p, false);
                } else if (!zeroHarm && s >= 2 && bs.isAir() && level.getBlockState(p.below()).isSolidRender() && level.random.nextFloat() < 0.26F) {
                    level.setBlockAndUpdate(p, Blocks.FIRE.defaultBlockState());
                    coneFires.add(p.immutable());
                }
            }
        }
        if (!coneFires.isEmpty()) {
            Vec3 mid = caster.position().add(normDir.scale(effRange * 0.5D));
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, mid, effRange, 100, level.getGameTime(), null, null, EssenceType.NETHER, 1.0F,
                    (srv, z, el) -> {
                        if (el >= z.maxDurationTicks) {
                            for (BlockPos fp : coneFires) {
                                if (srv.getBlockState(fp).is(Blocks.FIRE)) {
                                    srv.removeBlock(fp, false);
                                }
                            }
                            return false;
                        }
                        return true;
                    }
            ));
        }

        level.sendParticles(ParticleTypes.FLAME, muzzlePos.x + normDir.x * 1.4D, muzzlePos.y, muzzlePos.z + normDir.z * 1.4D,
                36, 0.80D, 0.45D, 0.80D, 0.16D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), MOLTEN_GOLD_HEX_COLOR, 34, 1.15F, 0.08F),
                muzzlePos.x + normDir.x * 1.6D, muzzlePos.y, muzzlePos.z + normDir.z * 1.6D,
                18, 0.70D, 0.40D, 0.70D, 0.06D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.88F, 1.20F);
        level.playSound(null, caster.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.90F, 0.92F);
    }

    private static void applyBackdraftBlastToTarget(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity target,
            Vec3 normDir,
            float blastDmg,
            boolean applyFormDamage
    ) {
        target.igniteForSeconds(5);
        if (applyFormDamage) {
            SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(level, target, caster, blastDmg, true);
        }
        if (!SpellCombatReactionEngine.isBossEntity(level, target)) {
            SpellExecutionEngine.applySyncedMotion(target, new Vec3(normDir.x * 1.62D, 0.44D, normDir.z * 1.62D));
        }
    }

    // =========================================================================
    // FORM MUTATION 8 — BRIMSTONE METEOR RAIN (SKYFALL)
    // =========================================================================

    /**
     * Computes canonical <i>Brimstone Meteor Rain</i> duration across Tiers 1–10:
     * starts at <b>6.0s (120 ticks) at Tier 1</b> and scales smoothly up to <b>13.2s (264 ticks) at Tier 10</b>.
     */
    public static int computeBrimstoneStormDurationTicks(int potencyTier) {
        int clampedTier = Math.clamp(potencyTier, 1, 10);
        return VFX_BRIMSTONE_STORM_TICKS + (clampedTier - 1) * 16;
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
        int tier = Math.clamp(spell.getEffectiveTier(), 1, 10);
        double r = Math.max(radius, 5.5D + (tier - 1) * 0.65D);
        int stormDurationTicks = computeBrimstoneStormDurationTicks(tier);
        BlockPos surfacePos = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        Vec3 anchoredCenter = new Vec3(impactCenter.x, surfacePos.getY() + 1.0D, impactCenter.z);
        double riftClearance = probeOverheadClearance(level, BlockPos.containing(anchoredCenter), 10);

        // Broadcast tier-scaled (120t..264t) cave-viable volcanic sky-rift, hurtling 3D brimstone meteors & caldera ground seal!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SKYFALL_PILLAR,
                anchoredCenter, new Vec3(0.0D, -1.0D, 0.0D),
                EssenceType.NETHER, null, (float) r, stormDurationTicks, tier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                anchoredCenter.add(0.0D, 0.03D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.NETHER, null, (float) r, stormDurationTicks, tier
        );
        level.playSound(null, BlockPos.containing(anchoredCenter), SoundEvents.BLAZE_AMBIENT, SoundSource.PLAYERS, 0.95F, 0.78F);
        emitInfernalThreatPulse(level, caster, anchoredCenter, spell, 30.0D);

        List<BlockPos> stormFires = new ArrayList<>();

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchoredCenter, r, stormDurationTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (BlockPos fp : stormFires) {
                            if (srv.getBlockState(fp).is(Blocks.FIRE)) {
                                srv.removeBlock(fp, false);
                            }
                        }
                        return false;
                    }

                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.LAVA,
                                zone.center.x, zone.center.y + riftClearance * 0.72D, zone.center.z,
                                10, zone.radius * 0.55D, 1.1D, zone.radius * 0.55D, 0.08D);
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), INFERNAL_EIDOLON_HEX_COLOR, 36, 1.15F, 0.10F),
                                zone.center.x, zone.center.y + riftClearance * 0.45D, zone.center.z,
                                12, zone.radius * 0.55D, 1.4D, zone.radius * 0.55D, 0.06D
                        );
                    }

                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        BlockPos cPos = BlockPos.containing(zone.center);
                        int br = (int) Math.ceil(zone.radius);
                        double zRadSq = zone.radius * zone.radius;
                        boolean zeroHarm = zone.spell != null && zone.spell.prefix().enforcesZeroEntityHarm();
                        boolean evaporatedAny = false;

                        // Vaporize standing water pools, bubble columns & waterlogged blocks into steam, melt ice/snow & scatter fire patches
                        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-br, -2, -br), cPos.offset(br, 2, br))) {
                            double dx = (p.getX() + 0.5D) - zone.center.x;
                            double dz = (p.getZ() + 0.5D) - zone.center.z;
                            if ((dx * dx + dz * dz) > zRadSq) continue;

                            BlockState bs = srv.getBlockState(p);
                            if (SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, bs)) continue;
                            if (bs.is(Blocks.WATER) || bs.is(Blocks.BUBBLE_COLUMN)) {
                                srv.removeBlock(p, false);
                                evaporatedAny = true;
                                srv.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.getX() + 0.5D, p.getY() + 0.6D, p.getZ() + 0.5D,
                                        4, 0.2D, 0.25D, 0.2D, 0.03D);
                            } else if (bs.hasProperty(BlockStateProperties.WATERLOGGED) && bs.getValue(BlockStateProperties.WATERLOGGED)) {
                                srv.setBlockAndUpdate(p, bs.setValue(BlockStateProperties.WATERLOGGED, false));
                                evaporatedAny = true;
                            } else if (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.SNOW) || bs.is(Blocks.POWDER_SNOW)) {
                                srv.removeBlock(p, false);
                                evaporatedAny = true;
                            } else if (!zeroHarm && bs.isAir() && srv.getBlockState(p.below()).isSolidRender() && srv.random.nextFloat() < 0.08F) {
                                srv.setBlockAndUpdate(p, Blocks.FIRE.defaultBlockState());
                                stormFires.add(p.immutable());
                            }
                        }
                        if (evaporatedAny) {
                            srv.playSound(null, cPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.85F, 1.15F);
                        }

                        // Crash 2–3 localized 3D meteor impacts across the bombardment zone
                        int strikes = 2 + (tier >= 5 ? 1 : 0);
                        for (int m = 0; m < strikes; m++) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.84D);
                            Vec3 strikePos = zone.center.add(Math.cos(ang) * dist, 0.08D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcast(
                                    srv, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                                    strikePos, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.NETHER, null, 1.90F, 48, tier
                            );
                        }
                        srv.playSound(null, cPos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.74F, 1.12F);

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius
                        );
                        if (!zeroHarm) {
                            // Repeatedly stagger & burn enemies with Nether Materia damage (+1 vs Frozen, linear distance falloff)
                            boolean hasHarm = hasHarmEffect(zone.spell, zone.slot);
                            boolean safeWard = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                            float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                            float meteorBaseDmg = (3.0F + tier * 0.90F) * Math.max(0.4F, zone.apparatusPotency) * flawPot;

                            for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, safeWard)) {
                                    continue;
                                }
                                double dx = v.getX() - zone.center.x;
                                double dz = v.getZ() - zone.center.z;
                                double hDistSq = dx * dx + dz * dz;
                                if (hDistSq > zRadSq) continue;
                                double hDist = Math.sqrt(hDistSq);
                                float falloff = (float) Mth.clamp(1.0D - (hDist / Math.max(1.0D, zone.radius)) * 0.67D, 0.33D, 1.0D);

                                v.igniteForSeconds(5);
                                if (!hasHarm) {
                                    SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(srv, v, owner, meteorBaseDmg * falloff, false);
                                }
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.42F * falloff
                                    );
                                }
                                if (!SpellCombatReactionEngine.isBossEntity(srv, v)) {
                                    double jx = (srv.random.nextDouble() - 0.5D) * 0.58D;
                                    double jz = (srv.random.nextDouble() - 0.5D) * 0.58D;
                                    SpellExecutionEngine.applySyncedMotion(v, new Vec3(jx, 0.25D, jz));
                                    SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.35D, 22L);
                                }
                            }
                        }
                        autoSmeltDroppedItems(srv, area);
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — INFERNAL EIDOLON SUITE (AGENT / SENTRY / DECOY)
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
        tag.putString("SpellEidolonMateria", EssenceType.NETHER.name());
        tag.putInt("SpellEidolonTintRGB", INFERNAL_EIDOLON_HEX_COLOR);

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SUMMON_PILLAR,
                spawnPos, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.NETHER, null, (float) Math.max(1.75D, radius * 0.65D), VFX_SUMMON_PILLAR_TICKS, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), INFERNAL_EIDOLON_HEX_COLOR, 42, 1.18F, 0.09F),
                spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 26, 0.45D, 0.65D, 0.45D, 0.06D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_KENAZ.get(), MOLTEN_GOLD_HEX_COLOR, 36, 1.15F, 0.05F),
                spawnPos.x, spawnPos.y + 1.1D, spawnPos.z, 8, 0.40D, 0.50D, 0.40D, 0.02D
        );
        level.sendParticles(ParticleTypes.FLAME, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 25, 0.45D, 0.6D, 0.45D, 0.08D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.88F, 0.92F);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.BASALT_PLACE, SoundSource.PLAYERS, 0.80F, 0.82F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT SYNERGIES — BLINK, TRANSMUTE / BREAK & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        if (caster.isOnFire()) {
            caster.clearFire();
        }

        SpellAssembly activeSpell = resolveActiveCasterSpell(level, caster, departurePos);
        boolean zeroHarm = activeSpell != null && activeSpell.prefix().enforcesZeroEntityHarm();
        boolean safeWard = resolveActiveCasterWardSafeguard(caster, activeSpell);

        // 1. Departure Pillar of Flame + 5.0s (100t) 3x3 temporary departure fire pool!
        SpellWorldVfxManager.broadcastGeyserPlume(
                level, departurePos, 1.40D, 4.4D, EssenceType.NETHER, null, VFX_THERMAL_POP_TICKS, potencyTier
        );
        spawnTemporaryFirePool(level, caster, BlockPos.containing(departurePos), 1, 100, 0.78F);
        if (!zeroHarm) {
            float depDmg = 2.4F + potencyTier * 0.75F;
            double depRadSq = 2.2D * 2.2D;
            for (LivingEntity chaser : level.getEntitiesOfClass(LivingEntity.class, new AABB(departurePos, departurePos).inflate(2.2D), LivingEntity::isAlive)) {
                if (chaser == caster || chaser.position().distanceToSqr(departurePos) > depRadSq) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(chaser, caster, safeWard)) continue;
                if (isPassiveOrFriendlyEntity(chaser, caster)) continue;
                chaser.igniteForSeconds(5);
                SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(level, chaser, caster, depDmg, true);
            }
        }
        level.sendParticles(ParticleTypes.LAVA, departurePos.x, departurePos.y + 1.0D, departurePos.z,
                18, 0.35D, 0.9D, 0.35D, 0.06D);
        level.sendParticles(ParticleTypes.FLAME, departurePos.x, departurePos.y + 1.2D, departurePos.z,
                28, 0.4D, 1.1D, 0.4D, 0.08D);

        // 2. Arrival Explosive Shockwave Pop (exact circular 3.5-block radius + ice/snow melting)!
        double popRadius = 3.5D;
        double popRadiusSq = popRadius * popRadius;
        meltIceAndSnowInRadius(level, BlockPos.containing(arrivalPos), 2);
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                arrivalPos, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.NETHER, null, (float) popRadius, VFX_THERMAL_POP_TICKS, potencyTier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                arrivalPos.add(0.0D, 0.85D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.NETHER, null, 2.25F, 54, potencyTier
        );
        if (!zeroHarm) {
            float popDmg = 3.2F + potencyTier * 1.00F;
            AABB arrivalBox = new AABB(
                    arrivalPos.x - popRadius, arrivalPos.y - 2.0D, arrivalPos.z - popRadius,
                    arrivalPos.x + popRadius, arrivalPos.y + 3.0D, arrivalPos.z + popRadius
            );
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, arrivalBox, LivingEntity::isAlive)) {
                if (foe == caster || foe.position().distanceToSqr(arrivalPos) > popRadiusSq) {
                    continue;
                }
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeWard)) {
                    continue;
                }
                if (isPassiveOrFriendlyEntity(foe, caster)) {
                    continue;
                }
                foe.igniteForSeconds(5);
                SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(level, foe, caster, popDmg, true);
                if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                    Vec3 delta = foe.position().subtract(arrivalPos);
                    Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
                    Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                    SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x * 1.15D, 0.38D, push.z * 1.15D));
                }
            }
        }
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_KENAZ.get(), MOLTEN_GOLD_HEX_COLOR, 32, 1.15F, 0.05F),
                arrivalPos.x, arrivalPos.y + 1.0D, arrivalPos.z, 8, 0.45D, 0.50D, 0.45D, 0.02D
        );
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.88F, 0.95F);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.82F, 1.25F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }

        // 1. Instantly vaporize water / bubble columns / waterlogged blocks into high-pressure steam!
        if (state.is(Blocks.WATER) || state.is(Blocks.BUBBLE_COLUMN)) {
            level.removeBlock(pos, false);
            emitHighPressureSteamBurst(level, caster, pos);
            return true;
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.WATERLOGGED, false));
            emitHighPressureSteamBurst(level, caster, pos);
            return true;
        }

        // 2. Section 5.5 Fluid Source Transmutation: solidify flowing lava rims into Magma Blocks!
        if (state.is(Blocks.LAVA) && !state.getFluidState().isSource()) {
            level.setBlockAndUpdate(pos, Blocks.MAGMA_BLOCK.defaultBlockState());
            level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5D, pos.getY() + 0.85D, pos.getZ() + 0.5D,
                    6, 0.25D, 0.20D, 0.25D, 0.02D);
            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.70F, 1.15F);
            return true;
        }

        // 3. Melt ice & snow into steam/water
        if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
            level.removeBlock(pos, false);
            emitHighPressureSteamBurst(level, caster, pos);
            return true;
        }

        // 4. Ignite unlit campfires, candles & TNT
        if (state.getBlock() instanceof CampfireBlock && !state.getValue(CampfireBlock.LIT)) {
            level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, true));
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.80F, 1.10F);
            return true;
        }
        if (state.getBlock() instanceof CandleBlock && !state.getValue(CandleBlock.LIT)) {
            level.setBlockAndUpdate(pos, state.setValue(CandleBlock.LIT, true));
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.70F, 1.20F);
            return true;
        }
        if (state.getBlock() instanceof TntBlock) {
            level.removeBlock(pos, false);
            PrimedTnt primedTnt = new PrimedTnt(level, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, caster);
            level.addFreshEntity(primedTnt);
            level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        // 5. Direct in-place thermal block conversions (Sand -> Glass, Clay -> Bricks, Raw Ore Blocks -> Metal Blocks)
        BlockState directConverted = resolveDirectThermalBlockConversion(state);
        if (directConverted != null) {
            level.setBlockAndUpdate(pos, directConverted);
            level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    10, 0.25D, 0.25D, 0.25D, 0.03D);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), MOLTEN_GOLD_HEX_COLOR, 26, 1.0F, 0.06F),
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 6, 0.22D, 0.22D, 0.22D, 0.03D
            );
            level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.70F, 1.20F);
            return true;
        }

        // 6. Auto-smelt raw ores & smeltable blocks (vanilla & Entropica!) with tier Fortune!
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness >= 0.0F && hardness <= 4.0F + potencyTier * 5.5F) {
            ItemStack virtualPickaxe = createTierFortunePickaxe(level, potencyTier);
            List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), caster, virtualPickaxe);
            List<ItemStack> smeltedDrops = new ArrayList<>(drops.size());
            boolean smeltedAny = false;
            int totalSmeltedCount = 0;
            for (ItemStack d : drops) {
                ItemStack res = trySmeltStack(level, d);
                if (res != d) {
                    smeltedAny = true;
                    totalSmeltedCount += res.getCount();
                }
                smeltedDrops.add(res);
            }
            if (smeltedAny) {
                level.removeBlock(pos, false);
                for (ItemStack out : smeltedDrops) {
                    Block.popResource(level, pos, out);
                }
                refundSubLinearBatchFuelDiscount(caster, potencyTier, totalSmeltedCount, EssenceType.NETHER);
                level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        12, 0.25D, 0.25D, 0.25D, 0.03D);
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), MOLTEN_GOLD_HEX_COLOR, 28, 1.0F, 0.06F),
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8, 0.22D, 0.22D, 0.22D, 0.03D
                );
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.75F, 1.15F);
                return true;
            }

            // 7. Section 5.5 Despawn / Ash Conversion (Invalid Organic Targets -> Cinder Ash Flakes!)
            if (isCharableOrganicBlock(state)) {
                level.removeBlock(pos, false);
                Block.popResource(level, pos, new ItemStack(ModItems.CINDER_ASH_FLAKES.get(), 1));
                level.sendParticles(ParticleTypes.ASH, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        18, 0.30D, 0.30D, 0.30D, 0.04D);
                level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        8, 0.22D, 0.22D, 0.22D, 0.02D);
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.65F, 1.35F);
                return true;
            }
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
        if (in.isEmpty()) return false;

        // Water bucket -> Empty bucket + high-pressure steam burst!
        if (in.is(Items.WATER_BUCKET) || in.is(Items.POWDER_SNOW_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.BUCKET, in.getCount()));
            emitHighPressureSteamBurst(level, caster, itemEntity.blockPosition());
            return true;
        }

        ItemStack smelted = trySmeltStack(level, in);
        if (smelted != in) {
            itemEntity.setItem(smelted);
            // Section 5.5 Sub-Linear Batch Scaling: award up to a 30% bulk fuel discount on large dropped stacks!
            refundSubLinearBatchFuelDiscount(caster, potencyTier, in.getCount(), EssenceType.NETHER);
            level.sendParticles(ParticleTypes.FLAME, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    10, 0.18D, 0.18D, 0.18D, 0.025D);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), MOLTEN_GOLD_HEX_COLOR, 24, 0.95F, 0.05F),
                    itemEntity.getX(), itemEntity.getY() + 0.25D, itemEntity.getZ(),
                    5, 0.15D, 0.15D, 0.15D, 0.02D
            );
            level.playSound(null, itemEntity.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.68F, 1.30F);
            return true;
        }

        // Section 5.5 Invalid Organic Dropped Items -> char into Cinder Ash Flakes!
        if (isCharableOrganicItem(in)) {
            itemEntity.setItem(new ItemStack(ModItems.CINDER_ASH_FLAKES.get(), in.getCount()));
            level.sendParticles(ParticleTypes.ASH, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    14, 0.20D, 0.20D, 0.20D, 0.03D);
            level.sendParticles(ParticleTypes.FLAME, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    6, 0.14D, 0.14D, 0.14D, 0.02D);
            level.playSound(null, itemEntity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.65F, 1.38F);
            return true;
        }
        return false;
    }

    /**
     * Resolves canonical <i>Hellfire Sear Hex</i> physical armor melt percentage across Hex Levels I–III:
     * {@code 30%} ({@code Level I}), {@code 45%} ({@code Level II}), {@code 60%} ({@code Level III}).
     */
    public static double resolveHellfireSearArmorMeltFraction(int stackCount) {
        return switch (Math.clamp(stackCount, 1, 3)) {
            case 2 -> 0.45D;
            case 3 -> 0.60D;
            default -> 0.30D;
        };
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Hellfire Sear Hex (Section 6.7):
        // Burns perpetually even underwater, melts 30% / 45% / 60% of physical Armor across Hex Levels I–III,
        // and disables natural health regeneration (enforced by SpellCombatReactionEngine.isRegenLockingHex)!
        int stack = Math.clamp(SpellCombatReactionEngine.getHexStackLevel(target), 1, 3);
        double armorMeltPct = resolveHellfireSearArmorMeltFraction(stack);
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);

        target.removeEffect(MobEffects.REGENERATION);
        target.igniteForSeconds(3);
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 45L);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, armorMeltPct, 0.0D, 45L);
        SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(
                level, target, null, (1.65F + potencyTier * 0.62F) * mult, false
        );

        level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY(0.7D), target.getZ(),
                7, 0.25D, 0.35D, 0.25D, 0.025D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), INFERNAL_EIDOLON_HEX_COLOR, 26, 0.98F, 0.06F),
                target.getX(), target.getY(0.75D), target.getZ(), 5, 0.25D, 0.35D, 0.25D, 0.02D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_KENAZ.get(), MOLTEN_GOLD_HEX_COLOR, 24, 0.92F, 0.04F),
                target.getX(), target.getY(0.92D), target.getZ(), 2, 0.20D, 0.25D, 0.20D, 0.01D
        );
        if (target.isInWaterOrRain()) {
            // Boiling underwater hellfire steam & bubble feedback!
            level.sendParticles(ParticleTypes.BUBBLE_POP, target.getX(), target.getY(0.6D), target.getZ(),
                    6, 0.25D, 0.30D, 0.25D, 0.03D);
            level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(0.85D), target.getZ(),
                    4, 0.22D, 0.25D, 0.22D, 0.02D);
        }
        return true;
    }

    // =========================================================================
    // PUBLIC SHARED HELPERS (EXCAVATION, LOGISTICS, HELD-ORE SMELT & THERMAL)
    // =========================================================================

    /**
     * Harvests {@code pos} for Nether / Pyre-lineage Excavation ({@code BREAK}), automatically smelting broken
     * block drops in-world, awarding a batch fuel discount, AND vaporizing targeted or adjacent exposed water pockets
     * into high-pressure steam (Sections 2.2, 2.3 &amp; 6.7).
     */
    public static boolean harvestBlockWithAutoSmeltAndVaporizeWater(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            float maxHardness,
            int potencyTier,
            EssenceType fuelMateria
    ) {
        if (state.is(Blocks.WATER) || state.is(Blocks.BUBBLE_COLUMN)) {
            level.removeBlock(pos, false);
            emitHighPressureSteamBurst(level, caster, pos);
            return true;
        }
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F || hardness > maxHardness) {
            return false;
        }

        ItemStack fortuneTool = createTierFortunePickaxe(level, potencyTier);
        List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), caster, fortuneTool);
        level.removeBlock(pos, false);

        int smeltedItems = 0;
        for (ItemStack raw : drops) {
            ItemStack out = trySmeltStack(level, raw);
            if (out != raw) {
                smeltedItems += out.getCount();
            }
            Block.popResource(level, pos, out);
        }

        // Vaporize any exposed adjacent water pockets so aquifers don't flood the tunnel!
        boolean vaporizedPocket = false;
        for (Direction dir : Direction.values()) {
            BlockPos adj = pos.relative(dir);
            BlockState adjState = level.getBlockState(adj);
            if (adjState.is(Blocks.WATER) || adjState.is(Blocks.BUBBLE_COLUMN)) {
                level.removeBlock(adj, false);
                level.sendParticles(ParticleTypes.CLOUD, adj.getX() + 0.5D, adj.getY() + 0.5D, adj.getZ() + 0.5D,
                        6, 0.25D, 0.25D, 0.25D, 0.03D);
                vaporizedPocket = true;
            }
        }

        if (smeltedItems > 0) {
            refundSubLinearBatchFuelDiscount(caster, potencyTier, smeltedItems, fuelMateria);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), MOLTEN_GOLD_HEX_COLOR, 26, 1.0F, 0.06F),
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8, 0.24D, 0.24D, 0.24D, 0.03D
            );
            level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.65F, 1.22F);
        }
        if (vaporizedPocket) {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.75F, 1.25F);
        }
        return true;
    }

    /**
     * Guide Sections 2.2.3 &amp; 2.3 Nether-Lineage Logistics Perk:
     * Automatically smelts vacuumed raw ores (and ore blocks) into refined ingots while in transit!
     */
    public static ItemStack trySmeltRawOreInTransit(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty() || !isRawOreOrOreBlockItem(stack)) {
            return stack;
        }
        return trySmeltStack(level, stack);
    }

    /**
     * Guide Section 6.7 <i>Cauterizing Cleave &amp; Auto-Smelt</i>:
     * Ensures any raw ores held across ALL equipment slots, {@link InventoryCarrier} mob storage slots
     * (e.g., Piglins, Villagers, Allays), and {@link Player} inventory slots are pre-smelted cleanly on kill!
     */
    public static void dropHeldRawOresPreSmelted(ServerLevel level, LivingEntity deceased) {
        CompoundTag tag = EntityHelper.getPersistentData(deceased);
        if (tag.getBoolean(TAG_HELD_ORES_SMELTED).orElse(false)) {
            return;
        }
        tag.putBoolean(TAG_HELD_ORES_SMELTED, true);

        if (deceased instanceof Player player) {
            boolean keepInv = level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
            Container playerInv = player.getInventory();
            for (int i = 0; i < playerInv.getContainerSize(); i++) {
                ItemStack slotStack = playerInv.getItem(i);
                if (!slotStack.isEmpty() && isRawOreOrOreBlockItem(slotStack)) {
                    ItemStack smelted = trySmeltStack(level, slotStack);
                    if (smelted != slotStack && !smelted.isEmpty()) {
                        if (keepInv) {
                            playerInv.setItem(i, smelted.copy());
                        } else {
                            playerInv.setItem(i, ItemStack.EMPTY);
                            spawnSmeltedDrops(level, player, smelted);
                        }
                    }
                }
            }
            playerInv.setChanged();

            for (EquipmentSlot eqSlot : EquipmentSlot.values()) {
                ItemStack equipped = player.getItemBySlot(eqSlot);
                if (!equipped.isEmpty() && isRawOreOrOreBlockItem(equipped)) {
                    ItemStack smelted = trySmeltStack(level, equipped);
                    if (smelted != equipped && !smelted.isEmpty()) {
                        if (keepInv) {
                            player.setItemSlot(eqSlot, smelted.copy());
                        } else {
                            player.setItemSlot(eqSlot, ItemStack.EMPTY);
                            spawnSmeltedDrops(level, player, smelted);
                        }
                    }
                }
            }
            return;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack held = deceased.getItemBySlot(slot);
            if (!held.isEmpty() && isRawOreOrOreBlockItem(held)) {
                ItemStack smelted = trySmeltStack(level, held);
                if (smelted != held && !smelted.isEmpty()) {
                    deceased.setItemSlot(slot, ItemStack.EMPTY);
                    spawnSmeltedDrops(level, deceased, smelted);
                }
            }
        }

        if (deceased instanceof InventoryCarrier carrier) {
            SimpleContainer mobInv = carrier.getInventory();
            for (int i = 0; i < mobInv.getContainerSize(); i++) {
                ItemStack stored = mobInv.getItem(i);
                if (!stored.isEmpty() && isRawOreOrOreBlockItem(stored)) {
                    ItemStack smelted = trySmeltStack(level, stored);
                    if (smelted != stored && !smelted.isEmpty()) {
                        mobInv.setItem(i, ItemStack.EMPTY);
                        spawnSmeltedDrops(level, deceased, smelted);
                    }
                }
            }
            mobInv.setChanged();
        } else if (deceased instanceof Container extraContainer) {
            for (int i = 0; i < extraContainer.getContainerSize(); i++) {
                ItemStack stored = extraContainer.getItem(i);
                if (!stored.isEmpty() && isRawOreOrOreBlockItem(stored)) {
                    ItemStack smelted = trySmeltStack(level, stored);
                    if (smelted != stored && !smelted.isEmpty()) {
                        extraContainer.setItem(i, ItemStack.EMPTY);
                        spawnSmeltedDrops(level, deceased, smelted);
                    }
                }
            }
            extraContainer.setChanged();
        }
    }

    private static void spawnSmeltedDrops(ServerLevel level, LivingEntity deceased, ItemStack smelted) {
        int remaining = smelted.getCount();
        int maxStack = Math.max(1, smelted.getMaxStackSize());
        while (remaining > 0) {
            int batch = Math.min(maxStack, remaining);
            ItemStack dropStack = smelted.copyWithCount(batch);
            ItemEntity drop = new ItemEntity(level, deceased.getX(), deceased.getY() + 0.45D, deceased.getZ(), dropStack);
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
            remaining -= batch;
        }
    }

    public static void meltIceAndSnowInRadius(ServerLevel level, BlockPos center, int radius) {
        boolean ultraWarm = level.dimensionType().ultraWarm();
        double rSq = (radius + 0.5D) * (radius + 0.5D);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-radius, -1, -radius), center.offset(radius, 2, radius))) {
            if (p.distSqr(center) > rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.PACKED_ICE) || bs.is(Blocks.BLUE_ICE)) {
                if (ultraWarm) {
                    level.removeBlock(p, false);
                } else {
                    level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
                }
            } else if (bs.is(Blocks.SNOW) || bs.is(Blocks.SNOW_BLOCK) || bs.is(Blocks.POWDER_SNOW)) {
                level.removeBlock(p, false);
            }
        }
    }

    public static void spawnTemporaryFirePoolPublic(
            ServerLevel level,
            @Nullable LivingEntity owner,
            BlockPos center,
            int radiusBlocks,
            int durationTicks,
            float chance
    ) {
        spawnTemporaryFirePool(level, owner, center, radiusBlocks, durationTicks, chance);
    }

    public static void autoSmeltDroppedItems(ServerLevel level, AABB box) {
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (!itemEntity.isAlive()) continue;
            ItemStack in = itemEntity.getItem();
            ItemStack smelted = trySmeltStack(level, in);
            if (smelted != in) {
                itemEntity.setItem(smelted);
                level.sendParticles(ParticleTypes.FLAME, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                        6, 0.12D, 0.12D, 0.12D, 0.02D);
            }
        }
    }

    public static ItemStack trySmeltStack(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) return stack;
        if (stack.is(ModItems.RAW_AURORAFOWL.get())) {
            return new ItemStack(ModItems.COOKED_AURORAFOWL.get(), stack.getCount());
        }
        if (stack.is(Blocks.CLAY.asItem())) {
            return new ItemStack(Blocks.BRICKS.asItem(), stack.getCount());
        }
        if (stack.is(Items.CLAY_BALL)) {
            return new ItemStack(Items.BRICK, stack.getCount());
        }
        if (stack.is(Items.RAW_IRON_BLOCK)) {
            return new ItemStack(Items.IRON_BLOCK, stack.getCount());
        }
        if (stack.is(Items.RAW_COPPER_BLOCK)) {
            return new ItemStack(Items.COPPER_BLOCK, stack.getCount());
        }
        if (stack.is(Items.RAW_GOLD_BLOCK)) {
            return new ItemStack(Items.GOLD_BLOCK, stack.getCount());
        }

        SingleRecipeInput input = new SingleRecipeInput(stack);
        var match = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level);
        if (match.isPresent()) {
            ItemStack out = match.get().value().assemble(input, level.registryAccess());
            if (!out.isEmpty()) {
                out.setCount(out.getCount() * stack.getCount());
                return out;
            }
        }

        // Dynamic Entropica raw/ore -> ingot fallback (e.g. raw_arcanite / arcanite_ore -> arcanite_ingot)
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = key.getPath();
        String metalBase = null;
        if (path.startsWith("raw_") && !path.equals("raw_rubber") && !path.equals("raw_materia_readout")) {
            metalBase = path.substring(4);
        } else if (path.endsWith("_ore") && !path.equals("entropic_ore")) {
            metalBase = path.substring(0, path.length() - 4);
            if (metalBase.startsWith("deepslate_")) {
                metalBase = metalBase.substring(10);
            }
        }
        if (metalBase != null) {
            ResourceLocation ingotLoc = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), metalBase + "_ingot");
            if (BuiltInRegistries.ITEM.containsKey(ingotLoc)) {
                Item ingot = BuiltInRegistries.ITEM.getValue(ingotLoc);
                if (ingot != Items.AIR) {
                    return new ItemStack(ingot, stack.getCount());
                }
            }
        }
        return stack;
    }

    // =========================================================================
    // INTERNAL THERMAL, THREAT & CONTAINMENT HELPERS
    // =========================================================================

    private static boolean hasHarmEffect(@Nullable SpellAssembly spell, @Nullable SubSpellSlot slot) {
        if (slot != null && slot.hasHarmVerb()) {
            return true;
        }
        if (spell != null) {
            for (SubSpellSlot s : spell.subSpells()) {
                if (s.hasHarmVerb()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Identifies passive farm animals, villagers, peaceful golems/helpers, tamed allies, and friendly eidolons
     * so hostile-targeted Nether fire waves/whips/pops spare peaceful creatures even when Ward is not slotted,
     * while still striking Monsters/Enemies, angry mobs targeting the caster, and non-allied PvP players (when Ward is off).
     */
    public static boolean isPassiveOrFriendlyEntity(LivingEntity ent, @Nullable LivingEntity caster) {
        if (ent == null || !ent.isAlive()) {
            return false;
        }
        if (ent instanceof SpellEidolonEntity eidolon) {
            return caster != null && (eidolon.isOwnedBy(caster) || eidolon.isFriendlyTo(caster));
        }
        if (ent instanceof Monster || ent instanceof Enemy) {
            return false;
        }
        if (ent instanceof Mob m && m.getTarget() != null
                && (caster == null
                    || m.getTarget() == caster
                    || ApexEschatonSingularityHelper.isFriendly(m.getTarget(), caster)
                    || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(m.getTarget(), caster, true))) {
            return false;
        }
        if (ent instanceof NeutralMob nm && (nm.isAngry()
                || (caster != null && caster.getUUID().equals(nm.getPersistentAngerTarget())))) {
            return false;
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
        return ent instanceof Animal
                || ent instanceof AbstractVillager
                || ent instanceof AbstractGolem
                || ent instanceof WaterAnimal
                || ent instanceof AmbientCreature
                || ent.getType().getCategory().isFriendly();
    }

    @Nullable
    private static SpellAssembly resolveActiveCasterSpell(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 origin
    ) {
        if (caster != null) {
            SpellAssembly fallbackFound = null;
            for (ItemStack held : List.of(caster.getMainHandItem(), caster.getOffhandItem())) {
                if (held.isEmpty()) continue;
                CastingApparatusState appState = held.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
                if (appState != null) {
                    Optional<SpellAssembly> active = appState.getActiveSpell();
                    if (active.isPresent()) {
                        SpellAssembly asm = active.get();
                        if (assemblyHasBlink(asm)) {
                            return asm;
                        }
                        if (fallbackFound == null) {
                            fallbackFound = asm;
                        }
                    }
                }
                SpellAssembly directSpell = held.get(ModDataComponents.SPELL_ASSEMBLY.get());
                if (directSpell != null) {
                    if (assemblyHasBlink(directSpell)) {
                        return directSpell;
                    }
                    if (fallbackFound == null) {
                        fallbackFound = directSpell;
                    }
                }
            }
            if (fallbackFound != null) {
                return fallbackFound;
            }
        }
        BlockPos originBlock = BlockPos.containing(origin);
        for (BlockPos check : List.of(originBlock, originBlock.below())) {
            if (level.getBlockEntity(check) instanceof AutocastPedestalBlockEntity pedestal) {
                Optional<SpellAssembly> pedSpell = pedestal.getActiveSpellAssembly();
                if (pedSpell.isPresent()) {
                    return pedSpell.get();
                }
            }
        }
        return null;
    }

    private static boolean assemblyHasBlink(SpellAssembly spell) {
        for (SubSpellSlot s : spell.subSpells()) {
            if (s.primaryEffect().effect() == SpellCoreEffect.BLINK
                    || s.secondaryEffect().map(e -> e.effect() == SpellCoreEffect.BLINK).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    private static boolean resolveActiveCasterWardSafeguard(
            @Nullable LivingEntity caster,
            @Nullable SpellAssembly activeSpell
    ) {
        if (activeSpell != null && activeSpell.hasWardFriendlyFireSafeguard()) {
            return true;
        }
        if (caster == null) {
            return false;
        }
        CompoundTag data = EntityHelper.getPersistentData(caster);
        if (data.contains(SpellLingeringZoneManager.TAG_AIR_BLINK_WARD_SAFE)) {
            return data.getBoolean(SpellLingeringZoneManager.TAG_AIR_BLINK_WARD_SAFE).orElse(false);
        }
        return false;
    }

    private static void emitInfernalThreatPulse(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 origin,
            SpellAssembly spell,
            double alertRadius
    ) {
        if (caster == null || spell.prefix().enforcesZeroEntityHarm()) {
            return;
        }
        // Respect Section 1.7 Umbral / Silence stealth suppression!
        EssenceType flowMat = spell.flowPart().materiaType();
        if (flowMat == EssenceType.UMBRAL || flowMat == EssenceType.NULL_U || flowMat == EssenceType.WRAITH) {
            return;
        }
        for (SubSpellSlot s : spell.subSpells()) {
            if (s.primaryEffect().materiaType() == EssenceType.UMBRAL
                    || s.primaryEffect().materiaType() == EssenceType.NULL_U) {
                return;
            }
        }
        level.gameEvent(caster, GameEvent.EXPLODE, BlockPos.containing(origin));
        if (caster instanceof Player || caster instanceof Mob) {
            AABB alertBox = new AABB(origin, origin).inflate(alertRadius);
            for (Monster monster : level.getEntitiesOfClass(Monster.class, alertBox, LivingEntity::isAlive)) {
                if (monster.getTarget() == null && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(monster, caster, true)) {
                    monster.setTarget(caster);
                }
            }
        }
    }

    private static void refundSubLinearBatchFuelDiscount(
            @Nullable LivingEntity caster,
            int potencyTier,
            int batchItemCount,
            EssenceType materia
    ) {
        if (!(caster instanceof Player player) || batchItemCount <= 1) {
            return;
        }
        // Section 5.5 Sub-Linear Batch Scaling: smooth curve up to 30% fuel discount on full 64-item stacks!
        double discountFrac = Math.min(0.30D, 0.06D + (Math.min(64, batchItemCount) / 64.0D) * 0.24D);
        long baseCastMfum = MateriaStageTable.getBaseMfumCastCost(Math.clamp(potencyTier, 1, 10));
        long refund = Math.max(1L, Math.round(baseCastMfum * discountFrac));
        SpellFuelService.restorePlayerMateriaFuel(player, materia, refund);
    }

    private static ItemStack createTierFortunePickaxe(ServerLevel level, int potencyTier) {
        ItemStack pick = new ItemStack(Items.NETHERITE_PICKAXE);
        int fortuneLvl = potencyTier >= 10 ? 4 : (potencyTier >= 7 ? 3 : (potencyTier >= 4 ? 2 : (potencyTier >= 3 ? 1 : 0)));
        if (fortuneLvl > 0) {
            final int lvl = fortuneLvl;
            level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                    .get(Enchantments.FORTUNE)
                    .ifPresent(holder -> pick.enchant(holder, lvl));
        }
        return pick;
    }

    @Nullable
    private static BlockState resolveDirectThermalBlockConversion(BlockState state) {
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            return Blocks.GLASS.defaultBlockState();
        }
        if (state.is(Blocks.CLAY)) {
            return Blocks.BRICKS.defaultBlockState();
        }
        if (state.is(Blocks.RAW_IRON_BLOCK)) {
            return Blocks.IRON_BLOCK.defaultBlockState();
        }
        if (state.is(Blocks.RAW_COPPER_BLOCK)) {
            return Blocks.COPPER_BLOCK.defaultBlockState();
        }
        if (state.is(Blocks.RAW_GOLD_BLOCK)) {
            return Blocks.GOLD_BLOCK.defaultBlockState();
        }
        if (state.is(Blocks.COBBLESTONE)) {
            return Blocks.STONE.defaultBlockState();
        }
        if (state.is(Blocks.STONE)) {
            return Blocks.SMOOTH_STONE.defaultBlockState();
        }
        if (state.is(Blocks.COBBLED_DEEPSLATE)) {
            return Blocks.DEEPSLATE.defaultBlockState();
        }
        if (state.is(Blocks.NETHERRACK)) {
            return Blocks.NETHER_BRICKS.defaultBlockState();
        }
        if (state.is(Blocks.WET_SPONGE)) {
            return Blocks.SPONGE.defaultBlockState();
        }
        if (state.is(Blocks.MUD)) {
            return Blocks.TERRACOTTA.defaultBlockState();
        }
        return null;
    }

    private static boolean isRawOreOrOreBlockItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(Items.RAW_IRON) || stack.is(Items.RAW_COPPER) || stack.is(Items.RAW_GOLD)
                || stack.is(Items.RAW_IRON_BLOCK) || stack.is(Items.RAW_COPPER_BLOCK) || stack.is(Items.RAW_GOLD_BLOCK)
                || stack.is(Items.ANCIENT_DEBRIS)) {
            return true;
        }
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);
        if (path.equals("raw_rubber") || path.equals("raw_materia_readout") || path.equals("raw_aurorafowl")) {
            return false;
        }
        return path.startsWith("raw_") || path.endsWith("_ore");
    }

    public static boolean isCharableOrganicBlock(BlockState state) {
        return state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOODEN_STAIRS)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_FENCES)
                || state.is(BlockTags.FENCE_GATES)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.CROPS)
                || state.is(BlockTags.WOOL)
                || state.is(BlockTags.WOOL_CARPETS)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.VINE)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.MOSS_CARPET)
                || state.is(Blocks.HAY_BLOCK)
                || state.is(Blocks.COBWEB)
                || state.is(Blocks.DEAD_BUSH);
    }

    public static boolean isCharableOrganicItem(ItemStack stack) {
        if (stack.is(ModItems.CINDER_ASH_FLAKES.get()) || stack.is(Items.CHARCOAL) || stack.is(Items.COAL)) {
            return false;
        }
        if (stack.is(ItemTags.LEAVES) || stack.is(ItemTags.FLOWERS) || stack.is(ItemTags.SAPLINGS)
                || stack.is(ItemTags.WOOL) || stack.is(ItemTags.PLANKS)
                || stack.is(ItemTags.WOODEN_STAIRS) || stack.is(ItemTags.WOODEN_SLABS)
                || stack.is(ItemTags.WOODEN_FENCES) || stack.is(ItemTags.FENCE_GATES)) {
            return true;
        }
        return stack.is(Items.ROTTEN_FLESH)
                || stack.is(Items.SPIDER_EYE)
                || stack.is(Items.STRING)
                || stack.is(Items.FEATHER)
                || stack.is(Items.WHEAT)
                || stack.is(Items.WHEAT_SEEDS)
                || stack.is(Items.BEETROOT_SEEDS)
                || stack.is(Items.MELON_SEEDS)
                || stack.is(Items.PUMPKIN_SEEDS)
                || stack.is(Items.STICK)
                || stack.is(Items.PAPER)
                || stack.is(Items.SUGAR_CANE)
                || stack.is(Items.BAMBOO)
                || stack.is(Items.VINE)
                || stack.is(Items.DEAD_BUSH);
    }

    private static void vaporizeWaterAndMeltIceInRadius(ServerLevel level, BlockPos center, int radius) {
        boolean evaporated = false;
        double rSq = (radius + 0.5D) * (radius + 0.5D);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-radius, -1, -radius), center.offset(radius, 1, radius))) {
            if (p.distSqr(center) > rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(Blocks.WATER) || bs.is(Blocks.BUBBLE_COLUMN)
                    || bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE)
                    || bs.is(Blocks.SNOW) || bs.is(Blocks.POWDER_SNOW)) {
                level.removeBlock(p, false);
                level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D,
                        4, 0.2D, 0.2D, 0.2D, 0.03D);
                evaporated = true;
            }
        }
        if (evaporated) {
            level.playSound(null, center, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.75F, 1.25F);
        }
    }

    private static void meltSnowOrThinIceAt(ServerLevel level, BlockPos pos) {
        BlockState bs = level.getBlockState(pos);
        if (bs.is(Blocks.SNOW) || bs.is(Blocks.POWDER_SNOW) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.ICE)) {
            level.removeBlock(pos, false);
        }
    }

    private static void emitHighPressureSteamBurst(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos) {
        Vec3 c = Vec3.atCenterOf(pos);
        level.sendParticles(ParticleTypes.CLOUD, c.x, c.y + 0.15D, c.z, 18, 0.38D, 0.48D, 0.38D, 0.08D);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, c.x, c.y + 0.25D, c.z, 8, 0.25D, 0.35D, 0.25D, 0.03D);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.85F, 1.25F);
        for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(2.2D), LivingEntity::isAlive)) {
            if (near == caster) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(near, caster, true)) continue;
            if (!SpellCombatReactionEngine.isBossEntity(level, near)) {
                Vec3 away = near.position().subtract(c);
                Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : Vec3.ZERO;
                SpellExecutionEngine.applySyncedMotion(near, new Vec3(push.x * 0.45D, 0.28D, push.z * 0.45D));
            }
        }
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        for (int dy = 1; dy >= -4; dy--) {
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

    private static double probeOverheadClearance(ServerLevel level, BlockPos groundAir, int maxScan) {
        for (int dy = 3; dy <= maxScan; dy++) {
            BlockPos p = groundAir.above(dy);
            BlockState bs = level.getBlockState(p);
            if (!bs.isAir() && !bs.getCollisionShape(level, p).isEmpty()) {
                return Math.max(3.5D, dy - 0.35D);
            }
        }
        return maxScan;
    }

    @Nullable
    private static BlockPos findGroundFireSpot(ServerLevel level, BlockPos approx) {
        for (int dy = 1; dy >= -2; dy--) {
            BlockPos p = approx.offset(0, dy, 0);
            BlockState cur = level.getBlockState(p);
            if ((cur.isAir() || cur.is(Blocks.SNOW)) && level.getBlockState(p.below()).isSolidRender()) {
                return p;
            }
        }
        return null;
    }

    private static void spawnTemporaryFirePool(
            ServerLevel level,
            @Nullable LivingEntity owner,
            BlockPos center,
            int radiusBlocks,
            int durationTicks,
            float chance
    ) {
        List<BlockPos> placed = new ArrayList<>();
        for (int dx = -radiusBlocks; dx <= radiusBlocks; dx++) {
            for (int dz = -radiusBlocks; dz <= radiusBlocks; dz++) {
                if ((dx != 0 || dz != 0) && level.random.nextFloat() > chance) continue;
                BlockPos fp = findGroundFireSpot(level, center.offset(dx, 0, dz));
                if (fp != null) {
                    level.setBlockAndUpdate(fp, Blocks.FIRE.defaultBlockState());
                    placed.add(fp.immutable());
                }
            }
        }
        if (!placed.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    owner, Vec3.atCenterOf(center), radiusBlocks + 1.5D, durationTicks, level.getGameTime(),
                    null, null, EssenceType.NETHER, 1.0F,
                    (srv, z, el) -> {
                        if (el >= z.maxDurationTicks) {
                            for (BlockPos fp : placed) {
                                if (srv.getBlockState(fp).is(Blocks.FIRE)) {
                                    srv.removeBlock(fp, false);
                                }
                            }
                            return false;
                        }
                        return true;
                    }
            ));
        }
    }
}
