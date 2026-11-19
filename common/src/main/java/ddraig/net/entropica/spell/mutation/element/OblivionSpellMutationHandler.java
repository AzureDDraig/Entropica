package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
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
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Oblivion Materia (Tier 3 Trinity Fusion #47)</b>
 * (Spellcrafting System Guide Section 6.48 — Trinity Nether + Umbral + Undead harmonic matrix):
 * <ul>
 *     <li><b>Palette</b>: Nether Blazing Magma Rust ({@code #B83C08}) + Stygian Obsidian Black ({@code #211A21}) + Necrotic Grave Slate ({@code #4D6453})</li>
 *     <li><b>Advantage</b>: Strong vs <b>RADIANT (+1)</b>, <b>NATURE (+1)</b>, <b>FROZEN (+1)</b>, <b>GENESIS (+1)</b>,
 *         and <b>AURA (+1)</b> (or <b>FROZEN</b>, <b>NATURE</b>, <b>GENESIS</b>, <b>AURA</b> under Nether Dominance) |
 *         Weak vs <b>WATER (-1)</b> and <b>VOID (-1)</b></li>
 * </ul>
 */
public final class OblivionSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int STYGIAN_OBLIVION_HEX_COLOR = 0xB83C08;
    public static final int STYGIAN_OBSIDIAN_BLACK_HEX_COLOR = 0x211A21;
    public static final int NECROTIC_GRAVE_SLATE_HEX_COLOR = 0x4D6453;

    private static final String TAG_OBLIVION_BEAM_LAST_TICK = "OblivionBeamLastTick";
    private static final String TAG_OBLIVION_BEAM_TICKS = "OblivionBeamContactTicks";
    private static final String TAG_OBLIVION_BEAM_ARMOR_STACKS = "OblivionBeamArmorStacks";
    private static final String TAG_OBLIVION_TRANSMUTE_BATCH_TICK = "OblivionTransmuteBatchTick";

    @Override
    public EssenceType materiaType() {
        return EssenceType.OBLIVION;
    }

    // =========================================================================
    // TITLES & CODEX / TOOLTIP DESCRIPTIONS
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Entropic Ash-Chain Flow";
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return "Modifies projectile and strike spells so tendrils of black soulfire (#B83C08 / #211A21 / #4D6453) leap to up to 4 nearby hostiles within 6m; each chained target takes 60% initial damage, loses 25% armor toughness, and burns in stygian fire for 5.0s.";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Stygian Hellfire Annihilation Skull";
            case BEAM -> "Stygian Oblivion Disintegration Ray & Void-Flare";
            case RADIAL_BURST -> "Stygian Hellfire Nova";
            case TOUCH_STRIKE -> "Stygian Extinction Palm-Cleave";
            case AURA -> "Oblivion Void-Shroud";
            case GLYPH -> "Oblivion Singularity Mine";
            case CONE -> "Stygian Extinction-Flame Deluge";
            case SKYFALL -> "Cataclysmic Stygian Meteor Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Hurls a screaming flaming skull of obsidian shadows, necrotic bone & nether embers (#B83C08 / #211A21 / #4D6453) dealing heavy Oblivion Materia damage, shattering 4 armor points, and exploding in a 4m black fire shockwave that burns underwater (1.5 hearts/s) with a 50%-slowing wither-ash crater.";
            case BEAM ->
                    "Channels an ultra-dense beam of black-and-rust netherfire (#B83C08 / #211A21 / #4D6453) melting 1 armor/s and vaporizing incoming arrows; after 5.0s sustained triggers a Cataclysmic Extinction Burst (8m knockback, -5 armor & Wither III + Blindness for 6.0s).";
            case RADIAL_BURST ->
                    "6m Stygian Hellfire Nova (#B83C08 / #211A21 / #4D6453, zero physical light blocks): hurls hostiles 8m outward, shatters 3 armor, ignites foes in 6.0s black stygian fire, and grants Obsidian Extinction Armor (+6 Armor, +3 Toughness, Fire/Lava/Wither immunity & 40% melee reflect for 10.0s).";
            case TOUCH_STRIKE ->
                    "Strikes with netherfire, obsidian shadow & necrotic bone-ash (#B83C08 / #211A21 / #4D6453) dealing massive Oblivion Materia damage, shattering 4 armor, disabling shields for 4.0s, inflicting Stygian Conflagration (1 heart/s burn, hunger drain & 6.0s heal lock), and leaving a slowing black fire-arc.";
            case AURA ->
                    "Oblivion Void-Shroud (#B83C08 / #211A21 / #4D6453): snuffs out torches, reduces hostile mob detection range by 70%, vaporizes incoming arrows and tridents within 3m into ash, and continuously drains 1 heart/s of wither damage from nearby foes.";
            case GLYPH ->
                    "Disarmable Single/Repeating Oblivion Singularity Mine (#B83C08 / #211A21 / #4D6453): pulls nearby mobs inward with gravitational suction when approached within 3m before violently exploding outward, launching enemies 8m high, shredding 3 armor, and inflicting Blindness & Wither II for 5.0s.";
            case CONE ->
                    "Stygian Extinction-Flame Deluge (#B83C08 / #211A21 / #4D6453): melts up to 4 armor points, incinerates foliage & wooden blocks, boils water into steam, and inflicts Stygian Dread (25.0s AI pathfinding disruption, -45% movement speed & blind panic flee).";
            case SKYFALL ->
                    "Extended 18.0s Cataclysmic Stygian Meteor Downpour across a 12m zone (#B83C08 / #211A21 / #4D6453): rains 3D obsidian-encrusted nether meteors & black hellfire knocking hostiles flat, shattering 5 armor, and leaving an 8.0s molten obsidian crater (60% slow, Wither II & underwater black burn).";
            case SUMMON_AGENT ->
                    "Stygian Dread-Juggernaut (45s lifecycle / 75 HP): massive armored colossus delivering earth-shaking cleaves that bypass 35% armor, deal heavy Oblivion damage, knock foes 6m back, and ignite targets in 5.0s black stygian fire.";
            case SUMMON_SENTRY ->
                    "Pyre of Nonexistence Obelisk (45s lifecycle / 85 HP): anchored monolithic spire bombarding enemies up to 16m away with explosive skull mortars (-2 armor/hit) while pulsing an 8m Extinction Aura (-1 armor/s & 1 heart/s burn).";
            case SUMMON_DECOY ->
                    "Smoldering Wither-Effigy (12s duration): 14m taunt bone-and-darkfire effigy reflecting 35% melee damage and detonating into a 10m burst of black netherfire (-4 armor, Wither II & Blindness for 5.0s).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Stygian Ash-Warp Step";
            case TRANSMUTE, BREAK -> "Stygian Desolation & Ash-Slag Smelting";
            case AFFLICTION -> "Stygian Oblivion-Extinction Hex";
            default -> "Stygian Oblivion " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Dissolves into black smoke, rust embers & charred ash (#B83C08 / #211A21 / #4D6453), reforming up to 16m away; departure blasts foes 6m back (-2 armor & 3.0s Blindness), while arrival grants Fire & Fall damage immunity for 6.0s and sheds all mob aggro.";
            case TRANSMUTE, BREAK ->
                    "Requires 5.0s continuous channeling: converts wood/organics into Crying Obsidian, Blackstone, or Coal Blocks (+25% yield), smelts raw metal blocks into ingots without fuel (+30% furnace speed), turns cobblestone into Deepslate/Basalt, and corrupts dirt into Soul Soil/Soul Sand.";
            case AFFLICTION ->
                    "Apocalyptic curse cutting Max Health by 25%, blocking all natural & potion healing, shredding 40% armor toughness, cutting attack damage by 35%, and dealing 1.5 hearts of underwater-burning true fire damage every 3.5s; Stygian Extinction Scent lures nether beasts & wither skeletons.";
            default -> "Infuses the effect with stygian Oblivion Materia resonance (#B83C08 / #211A21 / #4D6453).";
        };
    }

    // =========================================================================
    // PART 1 — FLOW MUTATIONS (ENTROPIC ASH-CHAIN FLOW)
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
        // Section 6.48 Entropic Ash-Chain Flow:
        // Leaps to up to 4 nearby hostiles within 6m (60% initial damage handled in SpellExecutionEngine),
        // cutting armor toughness by 25% and igniting in underwater-burning stygian fire for 5.0s (100t)!
        SpellCombatReactionEngine.applyPercentageArmorShred(toTarget, level, 0.0D, 0.25D, 100L);
        EntityHelper.getPersistentData(toTarget).putLong(
                SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L
        );
        toTarget.igniteForSeconds(5);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), STYGIAN_OBLIVION_HEX_COLOR, 20, 0.90F, 0.06F),
                toTarget.getX(), toTarget.getY(0.8D), toTarget.getZ(), 8, 0.25D, 0.30D, 0.25D, 0.03D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), STYGIAN_OBSIDIAN_BLACK_HEX_COLOR, 20, 0.88F, 0.06F),
                toTarget.getX(), toTarget.getY(0.65D), toTarget.getZ(), 6, 0.22D, 0.25D, 0.22D, 0.03D
        );
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), STYGIAN_OBLIVION_HEX_COLOR, 16, 0.85F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), STYGIAN_OBSIDIAN_BLACK_HEX_COLOR, 16, 0.82F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
        }
    }

    // =========================================================================
    // PART 2 — DELIVERY FORM MUTATIONS
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.02D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
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
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        if (hitEntity instanceof LivingEntity directVictim && directVictim.isAlive()
                && !slot.hasHarmVerb()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(directVictim, caster, wardSafe))) {
            SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(
                    level, directVictim, caster, 4.15F + tier * 1.05F, false, true
            );
        }
        detonateStygianSkull(level, caster, hitEntity.position(), hitEntity, spell, slot);
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
        detonateStygianSkull(level, caster, blockHit.getLocation(), null, spell, slot);
    }

    private static void detonateStygianSkull(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            @Nullable Entity directHit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.48 Stygian Hellfire Annihilation Skull:
        // Heavy Oblivion Materia damage, shatters 4 armor points, and explodes in a 4m shockwave of black fire
        // that burns underwater, drains 1.5 hearts/s (3.0 HP/s), and leaves a lingering wither-ash crater slowing foes by 50%!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long burnUntil = level.getGameTime() + 100L;

        SpellWorldVfxManager.broadcastProjectileImpact(
                level, center.add(0.0D, 0.2D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.OBLIVION, EssenceType.NETHER, 1.85F, tier
        );
        SpellWorldVfxManager.broadcastRadialNova(level, center, 4.0D, EssenceType.OBLIVION, EssenceType.UMBRAL, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.04D, 0.0D), 4.0D, EssenceType.OBLIVION, EssenceType.UNDEAD, 80, tier);

        float splashDmg = 3.45F + tier * 0.85F;
        AABB box = new AABB(
                center.x - 4.0D, center.y - 1.5D, center.z - 4.0D,
                center.x + 4.0D, center.y + 2.5D, center.z + 4.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (caster != null && isFriendlyTarget(foe, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 160L);
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, burnUntil);
            foe.igniteForSeconds(5);
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, level, 0.50D, 80L);
            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2)); // 50% slow
            if (foe != directHit) {
                SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(level, foe, caster, splashDmg, false, true);
            }
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 0.6D, center.z, 26, 1.5D, 0.5D, 1.5D, 0.05D);
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.6D, center.z, 22, 1.4D, 0.5D, 1.4D, 0.06D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.85F, 0.72F);

        // Lingering 4.0s (80t) wither-ash crater draining 1.5 hearts/s (3.0 HP/s) & slowing enemies by 50%!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, 4.0D, 80, level.getGameTime(), spell, slot, EssenceType.OBLIVION, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed > 0 && elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        long now = srv.getGameTime();
                        AABB crater = new AABB(
                                zone.center.x - 4.0D, zone.center.y - 1.2D, zone.center.z - 4.0D,
                                zone.center.x + 4.0D, zone.center.y + 2.2D, zone.center.z + 4.0D
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, crater, e -> e.isAlive() && e != owner)) {
                            if (owner != null && isFriendlyTarget(foe, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zSafe)) continue;
                            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 40L);
                            foe.igniteForSeconds(2);
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.50D, 30L);
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 2));
                            SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(srv, foe, owner, 3.0F, false, true); // 1.5 hearts/s!
                        }
                        srv.sendParticles(ParticleTypes.ASH, zone.center.x, zone.center.y + 0.25D, zone.center.z, 14, 1.6D, 0.2D, 1.6D, 0.02D);
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 2 — STYGIAN OBLIVION DISINTEGRATION RAY & VOID-FLARE (BEAM)
    // -------------------------------------------------------------------------

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
        // Section 6.48 Stygian Oblivion Disintegration Ray & Void-Flare:
        // Melts 1 armor point per second and vaporizes incoming arrows in mid-air;
        // REQUIRES 5.0s (100 contact ticks) continuous channeling to trigger Cataclysmic Extinction Burst
        // (8m knockback, melts 5 armor points, and inflicts Wither III & Blindness for 6.0s / 120t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 eye = caster.getEyePosition();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        AABB corridor = new AABB(eye, eye.add(dirNorm.scale(maxReach))).inflate(1.5D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, corridor, p -> p.getOwner() != caster && !(p instanceof SpellProjectileEntity))) {
            if (proj.getOwner() instanceof LivingEntity pOwner && isFriendlyTarget(pOwner, caster)) continue;
            level.sendParticles(ParticleTypes.ASH, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.02D);
            proj.discard();
        }

        long now = level.getGameTime();
        float beamDmg = (2.65F + tier * 0.70F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(level, hit, caster, beamDmg, false, true);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_OBLIVION_BEAM_LAST_TICK).orElse(-100L);
            long delta = now - last;
            int stepTicks = (delta > 0L && delta <= 8L) ? (int) Math.max(4L, delta) : 20;
            int contactTicks = (delta <= 45L) ? data.getInt(TAG_OBLIVION_BEAM_TICKS).orElse(0) + stepTicks : stepTicks;
            int armorStacks = (delta <= 45L) ? Math.min(6, Math.max(1, contactTicks / 20)) : 1;
            data.putLong(TAG_OBLIVION_BEAM_LAST_TICK, now);
            data.putInt(TAG_OBLIVION_BEAM_TICKS, contactTicks);
            data.putInt(TAG_OBLIVION_BEAM_ARMOR_STACKS, armorStacks);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, armorStacks, 0.0D, 80L);

            if (contactTicks >= 100) {
                data.putInt(TAG_OBLIVION_BEAM_TICKS, 0);
                SpellWorldVfxManager.broadcastRadialNova(level, hit.position(), 4.8D, EssenceType.OBLIVION, EssenceType.NETHER, tier);
                float burstDmg = (11.20F + tier * 2.55F) * Math.max(0.5F, apparatusPotency);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(4.8D), e -> e.isAlive() && e != caster)) {
                    if (isFriendlyTarget(foe, caster)) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
                    Vec3 dVec = foe.position().subtract(caster.position());
                    Vec3 away = dVec.lengthSqr() > 1.0E-4D ? dVec.normalize().scale(1.85D) : dirNorm.scale(1.85D);
                    foe.setDeltaMovement(away.x, 0.44D, away.z); // 8m knockback!
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 200L);
                    foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 2)); // Wither III 6.0s!
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0)); // Blindness 6.0s!
                    SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(level, foe, caster, burstDmg, false, true);
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.722F, 0.235F, 0.031F),
                        hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, hit.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.68F);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 3 — STYGIAN HELLFIRE NOVA (RADIAL BURST)
    // -------------------------------------------------------------------------

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
        // Section 6.48 Stygian Hellfire Nova:
        // 6m radius, shaders/bloom only without chunk light recalculations (zero physical light blocks);
        // hurls hostiles 8m outward, shatters 3 armor points, ignites enemies in black stygian fire for 6.0s (120t),
        // and grants caster Obsidian Extinction Armor (+6 Armor, +3 Toughness, Fire/Lava/Wither immunity & 40% melee reflect for 10.0s / 200t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        double r = Math.max(6.0D, radius);
        long now = level.getGameTime();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.OBLIVION, EssenceType.NETHER, tier);
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_OBLIVION_EXTINCTION_ARMOR_UNTIL, now + 200L);
        caster.clearFire();
        caster.removeEffect(MobEffects.WITHER);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 3.0D, 0.0D, 0.0D, 200L);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);

        float novaDmg = (4.45F + tier * 1.10F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(
                center.x - r, center.y - 2.0D, center.z - r,
                center.x + r, center.y + 3.5D, center.z + r
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (isFriendlyTarget(foe, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
            Vec3 delta = foe.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.85D) : new Vec3(0.0D, 0.42D, 0.0D);
            foe.setDeltaMovement(away.x, 0.42D, away.z); // 8m outward hurl!
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 160L);
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
            foe.igniteForSeconds(6);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(level, foe, caster, novaDmg, false, true);
            }
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.722F, 0.235F, 0.031F),
                center.x, center.y + 0.9D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 0.8D, center.z, 36, 2.3D, 0.6D, 2.3D, 0.05D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 0.68F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 4 — STYGIAN EXTINCTION PALM-CLEAVE (TOUCH / STRIKE)
    // -------------------------------------------------------------------------

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
        // Section 6.48 Stygian Extinction Palm-Cleave:
        // Massive Oblivion Materia damage, shatters 4 armor points, disables shields for 4.0s (80t),
        // inflicts Stygian Conflagration (1 heart/s burn, drains hunger & blocks natural healing for 6.0s / 120t),
        // and leaves a slowing black fire-arc on the ground that damages enemies attempting to cross!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.3D, 0.0D), push, 3.0D,
                EssenceType.OBLIVION, EssenceType.NETHER, tier
        );

        float cleaveDmg = (4.55F + tier * 1.12F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 4.0D, 0.0D, 160L);
            if (hit.isBlocking()) {
                hit.stopUsingItem();
            }
            if (hit instanceof Player p) {
                p.getCooldowns().addCooldown(new ItemStack(Items.SHIELD), 80);
            }
            CompoundTag hData = EntityHelper.getPersistentData(hit);
            hData.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
            hData.putLong(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_UNTIL, now + 120L);
            hit.igniteForSeconds(6);
            hit.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 1));
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(level, hit, caster, cleaveDmg, true, true);
            }
            hData.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, hit.getHealth());
        }

        Vec3 arcPos = caster.position().add(push.scale(2.4D));
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, arcPos.add(0.0D, 0.04D, 0.0D), 2.6D, EssenceType.OBLIVION, EssenceType.UMBRAL, 80, tier
        );
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, arcPos, 2.6D, 80, now, spell, slot, EssenceType.OBLIVION, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB box = new AABB(
                                zone.center.x - 2.6D, zone.center.y - 1.0D, zone.center.z - 2.6D,
                                zone.center.x + 2.6D, zone.center.y + 1.8D, zone.center.z + 2.6D
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && isFriendlyTarget(foe, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zSafe)) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 2));
                            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, srv.getGameTime() + 40L);
                            foe.igniteForSeconds(3);
                            SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(srv, foe, owner, 2.20F + tier * 0.50F, false, true);
                        }
                        srv.sendParticles(ParticleTypes.LARGE_SMOKE, zone.center.x, zone.center.y + 0.15D, zone.center.z, 8, 1.0D, 0.1D, 1.0D, 0.02D);
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 5 — OBLIVION VOID-SHROUD (AURA / SELF)
    // -------------------------------------------------------------------------

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
        // Section 6.48 Oblivion Void-Shroud:
        // Snuffs out torches, reduces hostile mob detection range by 70%, vaporizes incoming arrows & tridents
        // within 3m into ash, and continuously drains 1 heart/s (2.0 HP) of wither damage from nearby foes!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_OBLIVION_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.52F, null);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, caster.getX(), caster.getY(0.9D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.04D);
        level.playSound(null, caster.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.85F, 0.72F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 6 — OBLIVION SINGULARITY MINE (GLYPH / TRAP)
    // -------------------------------------------------------------------------

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
        // Section 6.48 Oblivion Singularity Mine:
        // Disarmable Single/Repeating concealed shadow-mine (#B83C08 / #211A21 / #4D6453);
        // when hostiles approach within 3m, pulls all nearby mobs into a central point with gravitational suction
        // before violently exploding outward, launching enemies 8m high, shredding 3 armor, and inflicting Blindness & Wither II 5.0s!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.05D, 0.0D), trapRad, EssenceType.OBLIVION, EssenceType.UMBRAL, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};
        final int[] suctionCountdown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.OBLIVION, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.05D, 0.0D), zone.radius, EssenceType.OBLIVION, EssenceType.UMBRAL, rem, tier
                        );
                    }

                    AABB suctionBox = new AABB(
                            zone.center.x - 5.0D, zone.center.y - 2.0D, zone.center.z - 5.0D,
                            zone.center.x + 5.0D, zone.center.y + 3.5D, zone.center.z + 5.0D
                    );

                    // Active gravitational suction phase (12 ticks) pulling nearby mobs into the central point before detonation!
                    if (suctionCountdown[0] > 0) {
                        suctionCountdown[0]--;
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, suctionBox, e -> e.isAlive() && e != owner)) {
                            if (owner != null && isFriendlyTarget(v, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;
                            Vec3 toCenter = zone.center.subtract(v.position());
                            if (toCenter.lengthSqr() > 0.04D) {
                                Vec3 pull = toCenter.normalize().scale(0.46D);
                                v.setDeltaMovement(pull.x, 0.08D, pull.z);
                                v.hasImpulse = true;
                            }
                        }
                        srv.sendParticles(ParticleTypes.SMOKE, zone.center.x, zone.center.y + 0.4D, zone.center.z, 8, 1.4D, 0.3D, 1.4D, -0.05D);
                        if (suctionCountdown[0] == 0) {
                            triggersLeft[0]--;
                            rearmCooldown[0] = 50;
                            SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 4.2D, EssenceType.OBLIVION, EssenceType.NETHER, tier);
                            float mineDmg = (4.65F + tier * 1.15F) * Math.max(0.5F, zone.apparatusPotency);
                            for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, suctionBox, e -> e.isAlive() && e != owner)) {
                                if (owner != null && isFriendlyTarget(v, owner)) continue;
                                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;
                                SpellCombatReactionEngine.applyTimedAttributeShred(v, srv, 3.0D, 0.0D, 160L);
                                v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                                v.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                                v.setDeltaMovement(v.getDeltaMovement().x * 0.25D, 1.22D, v.getDeltaMovement().z * 0.25D); // 8m vertical launch!
                                v.hasImpulse = true;
                                SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(srv, v, owner, mineDmg, false, true);
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.OBLIVION, zone.apparatusPotency
                                    );
                                }
                            }
                            srv.sendParticles(ParticleTypes.LARGE_SMOKE, zone.center.x, zone.center.y + 0.6D, zone.center.z, 32, 1.5D, 0.6D, 1.5D, 0.08D);
                            srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.68F);
                            return triggersLeft[0] > 0;
                        }
                        return true;
                    }

                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SMOKE, zone.center.x, zone.center.y + 0.1D, zone.center.z, 4, 0.7D, 0.05D, 0.7D, 0.01D);
                    }
                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    AABB trigger = new AABB(
                            zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                            zone.center.x + 3.0D, zone.center.y + 2.4D, zone.center.z + 3.0D
                    );
                    boolean hasFoe = !srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner
                            && (owner == null || !isFriendlyTarget(e, owner))
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))).isEmpty();
                    if (hasFoe) {
                        suctionCountdown[0] = 12;
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.85F, 0.65F);
                    }
                    return true;
                }
        ).markDisarmableGlyph();

        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 7 — STYGIAN EXTINCTION-FLAME DELUGE (CONE / SPRAY)
    // -------------------------------------------------------------------------

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
        // Section 6.48 Stygian Extinction-Flame Deluge:
        // Melts up to 4 armor points, incinerates foliage & wooden blocks, boils water into steam,
        // and inflicts Stygian Dread (25.0s / 500t AI pathfinding disruption, -45% movement speed & blind panic flee)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), dirNorm, coneRange,
                EssenceType.OBLIVION, EssenceType.NETHER, tier
        );

        float coneDmg = (3.45F + tier * 0.88F) * Math.max(0.5F, apparatusPotency);
        long unquenchUntil = level.getGameTime() + 100L;
        for (LivingEntity hit : hitTargets) {
            if (isFriendlyTarget(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 4.0D, 0.0D, 160L);
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.45D, 500L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 500, 2)); // ~45% slow for 25s
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, unquenchUntil);
            hit.igniteForSeconds(5);
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
            SpellLingeringZoneManager.applyFearFlee(hit, caster.position(), 140);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }
        }

        Vec3 eye = caster.getEyePosition();
        for (double d = 1.5D; d <= coneRange; d += 1.6D) {
            BlockPos step = BlockPos.containing(eye.add(dirNorm.scale(d)));
            for (BlockPos p : BlockPos.betweenClosed(step.offset(-1, -1, -1), step.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
                if (bs.is(Blocks.WATER)) {
                    level.removeBlock(p, false);
                    level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 3, 0.2D, 0.2D, 0.2D, 0.02D);
                } else if (bs.is(BlockTags.SMALL_FLOWERS) || bs.is(Blocks.SHORT_GRASS) || bs.is(Blocks.FERN)
                        || bs.is(BlockTags.LEAVES)) {
                    level.removeBlock(p, false);
                    level.sendParticles(ParticleTypes.ASH, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 4, 0.2D, 0.2D, 0.2D, 0.02D);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 8 — CATACLYSMIC STYGIAN METEOR DOWNPOUR (SKYFALL)
    // -------------------------------------------------------------------------

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
        // Section 6.48 Cataclysmic Stygian Meteor Downpour:
        // Extended 18.0s (360t) bombardment across a 12m+ zone + 8.0s (160t) molten obsidian crater (520t total);
        // rains 8 falling 3D Skyfall Pseudoblock massive obsidian-encrusted nether meteors alongside black hellfire bolts
        // and necrotic bone-slag — knocking hostiles flat, shattering 5 armor points, slowing enemies by 60%,
        // inflicting Wither II, and continuously burning any hostile inside across all 520t!
        int tier = spell.getEffectiveTier();
        double r = Math.max(12.0D, radius);
        BlockPos centerPos = BlockPos.containing(impactCenter);
        double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.OBLIVION, EssenceType.NETHER, 365, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.05D, 0.0D), r, EssenceType.OBLIVION, EssenceType.UMBRAL, 520, tier);

        final int boulderCount = 8;
        final BlockPos[] boulderTargets = new BlockPos[boulderCount];
        boulderTargets[0] = findGroundSurfaceNear(level, centerPos).above();
        for (int i = 1; i < boulderCount; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / (boulderCount - 1.0D)) + level.random.nextDouble() * 0.40D;
            double dist = 0.20D * r + level.random.nextDouble() * (0.60D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            boulderTargets[i] = findGroundSurfaceNear(level, raw).above();
        }

        final boolean[] landed = new boolean[boulderCount];
        Map<BlockPos, BlockState> savedCraterBlocks = new HashMap<>();

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, EssenceType.OBLIVION, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (Map.Entry<BlockPos, BlockState> e : savedCraterBlocks.entrySet()) {
                            BlockState cur = srv.getBlockState(e.getKey());
                            if (cur.is(Blocks.CRYING_OBSIDIAN) || cur.is(Blocks.BLACKSTONE) || cur.is(Blocks.MAGMA_BLOCK)) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        return false;
                    }

                    boolean stormActive = elapsed < 360;
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (stormActive) {
                        // 1. 8 falling 3D obsidian-encrusted nether meteor pseudoblock columns!
                        for (int mIdx = 0; mIdx < boulderCount; mIdx++) {
                            int impactTick = 20 + mIdx * 42;
                            BlockPos strikePos = boulderTargets[mIdx];
                            boolean cryingCore = (mIdx % 2 == 0);
                            BlockState pseudoState = cryingCore
                                    ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                                    : Blocks.BLACKSTONE.defaultBlockState();
                            int colHeight = cryingCore ? 2 : 1;

                            if (elapsed == impactTick - 14) {
                                int holdTicks = Math.max(8, 360 - impactTick);
                                SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                                        srv,
                                        new Vec3(strikePos.getX() + 0.5D, strikePos.getY() + skyH, strikePos.getZ() + 0.5D),
                                        strikePos,
                                        pseudoState,
                                        colHeight,
                                        14,
                                        holdTicks,
                                        EssenceType.OBLIVION,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.OBLIVION, EssenceType.NETHER, 2.35F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 22, 1.0D, 0.5D, 1.0D, 0.09D
                                );
                                srv.playSound(null, strikePos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.85F, 0.72F);
                            }
                            if (landed[mIdx]) {
                                SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, strikePos, colHeight, false);
                            }
                        }

                        // 2. Black hellfire bolts & necrotic bone-slag impacts!
                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 strike = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, strike, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.OBLIVION, EssenceType.UNDEAD, 1.50F, zTier
                            );
                            srv.sendParticles(ParticleTypes.ASH, strike.x, strike.y + 0.8D, strike.z, 10, 0.7D, 0.6D, 0.7D, 0.04D);
                        }
                    } else if (elapsed == 360) {
                        // Bombardment complete: materialize real temporary molten obsidian & blackstone crater for 8.0s (160t)!
                        for (int mIdx = 0; mIdx < boulderCount; mIdx++) {
                            BlockPos strikePos = boulderTargets[mIdx];
                            BlockPos ground = strikePos.below();
                            BlockState gs = srv.getBlockState(ground);
                            if ((gs.is(BlockTags.DIRT) || gs.is(Blocks.STONE) || gs.is(Blocks.COBBLESTONE))
                                    && !savedCraterBlocks.containsKey(ground)
                                    && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, ground, gs)) {
                                savedCraterBlocks.put(ground.immutable(), gs);
                                srv.setBlockAndUpdate(ground, (mIdx % 2 == 0)
                                        ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                                        : Blocks.MAGMA_BLOCK.defaultBlockState());
                            }
                        }
                    }

                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.LARGE_SMOKE,
                                zone.center.x, zone.center.y + (stormActive ? 3.8D : 0.4D), zone.center.z,
                                22, zone.radius * 0.55D, stormActive ? 2.0D : 0.2D, zone.radius * 0.55D, 0.05D);
                        srv.sendParticles(ParticleTypes.FLAME,
                                zone.center.x, zone.center.y + (stormActive ? 3.5D : 0.3D), zone.center.z,
                                14, zone.radius * 0.55D, stormActive ? 1.5D : 0.2D, zone.radius * 0.55D, 0.05D);
                    }

                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        long now = srv.getGameTime();
                        float stormDmg = (3.85F + zTier * 0.95F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && isFriendlyTarget(foe, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                            // 60% slow, Wither II & underwater black burn across all 520t (18s + 8s)!
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.60D, 35L);
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3));
                            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
                            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 45L);
                            foe.igniteForSeconds(3);
                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 160L);
                                foe.setDeltaMovement(foe.getDeltaMovement().x * 0.3D, -0.38D, foe.getDeltaMovement().z * 0.3D);
                                foe.hasImpulse = true;
                                SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(srv, foe, owner, stormDmg, false, true);
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.OBLIVION, zone.apparatusPotency * 0.45F
                                    );
                                }
                            } else {
                                SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(srv, foe, owner, 2.25F, false, true);
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 9 — STYGIAN OBLIVION-COLOSSUS SUITE (SUMMON / EIDOLON)
    // -------------------------------------------------------------------------

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
        tag.putString("SpellEidolonMateria", EssenceType.OBLIVION.name());
        tag.putInt("SpellEidolonTintRGB", STYGIAN_OBLIVION_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.8D, radius * 0.55D), EssenceType.OBLIVION, EssenceType.NETHER, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.LARGE_SMOKE, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // =========================================================================
    // PART 3 — CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.48 Stygian Ash-Warp Step:
        // Dissolves into black smoke, rust embers & charred ash (#B83C08 / #211A21 / #4D6453), reforming up to 16m away;
        // departure blasts foes 6m back, shatters 2 armor, and blinds 3.0s (60t);
        // arrival grants Fire & Fall damage immunity for 6.0s (120t) while completely shedding all hostile mob aggro!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.OBLIVION, potencyTier);
        SpellWorldVfxManager.broadcastRadialNova(level, departurePos, 4.0D, EssenceType.OBLIVION, EssenceType.NETHER, potencyTier);

        AABB depBox = new AABB(
                departurePos.x - 4.0D, departurePos.y - 1.5D, departurePos.z - 4.0D,
                departurePos.x + 4.0D, departurePos.y + 2.5D, departurePos.z + 4.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox, e -> e.isAlive() && e != caster)) {
            if (isFriendlyTarget(foe, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            Vec3 delta = foe.position().subtract(departurePos);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.45D) : new Vec3(0.0D, 0.36D, 0.0D);
            foe.setDeltaMovement(away.x, 0.36D, away.z); // 6m departure blast!
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 120L);
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(level, foe, caster, 3.20F + potencyTier * 0.85F, false, true);
        }

        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, arrivalPos).inflate(18.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
        }
        EntityHelper.getPersistentData(caster).putLong(
                SpellLingeringZoneManager.TAG_OBLIVION_FIRE_FALL_IMMUNE_UNTIL, level.getGameTime() + 120L
        );
        caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 120, 0));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0));
        caster.resetFallDistance();
        level.sendParticles(ParticleTypes.LARGE_SMOKE, departurePos.x, departurePos.y + 0.8D, departurePos.z, 22, 0.5D, 0.5D, 0.5D, 0.05D);
        level.sendParticles(ParticleTypes.ASH, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 20, 0.4D, 0.4D, 0.4D, 0.03D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.85F, 0.72F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.48 Stygian Desolation & Ash-Slag Smelting [5.0s Continuous Catalytic Channel]:
        // Converts wood/organic blocks into Crying Obsidian, Blackstone, or Coal Blocks (+25% bonus yield);
        // smelts raw metal blocks into ingots without fuel with +30% speed acceleration on nearby furnaces;
        // turns cobblestone into Deepslate / Basalt; and corrupts dirt into Soul Soil / Soul Sand!
        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 5, potencyTier, EssenceType.OBLIVION, "Stygian Desolation & Ash-Slag Smelting", null)) {
            return true;
        }

        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D), ItemEntity::isAlive)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }

        // +30% smelting speed acceleration for furnaces / blast furnaces / smokers at or adjacent to pos!
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AbstractFurnaceBlockEntity) {
            registerStygianFurnaceAccelerationZone(level, caster, pos.immutable());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }

        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            if (didAny) refundBatchDiscount(level, caster, potencyTier);
            return didAny;
        }

        if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.HAY_BLOCK)) {
            BlockState out = switch (level.random.nextInt(3)) {
                case 0 -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
                case 1 -> Blocks.BLACKSTONE.defaultBlockState();
                default -> Blocks.COAL_BLOCK.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            if (level.random.nextFloat() < 0.25F) {
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                        new ItemStack(Items.CHARCOAL, 2)));
            }
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.RAW_IRON_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, new ItemStack(Items.IRON_INGOT, 9)));
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.RAW_COPPER_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, new ItemStack(Items.COPPER_INGOT, 9)));
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.RAW_GOLD_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, new ItemStack(Items.GOLD_INGOT, 9)));
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.COBBLED_DEEPSLATE)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean()
                    ? Blocks.DEEPSLATE.defaultBlockState()
                    : Blocks.BASALT.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.DIRT)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean()
                    ? Blocks.SOUL_SOIL.defaultBlockState()
                    : Blocks.SOUL_SAND.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (didAny) {
            refundBatchDiscount(level, caster, potencyTier);
        }
        return didAny;
    }

    @SuppressWarnings("unchecked")
    private static void registerStygianFurnaceAccelerationZone(ServerLevel level, @Nullable LivingEntity caster, BlockPos furnacePos) {
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, Vec3.atCenterOf(furnacePos), 1.5D, 600, level.getGameTime(), null, null, EssenceType.OBLIVION, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    BlockEntity curBe = srv.getBlockEntity(furnacePos);
                    if (!(curBe instanceof AbstractFurnaceBlockEntity furnace)) return false;
                    // +30% extra ticks (3 extra ticks every 10 ticks)!
                    if (elapsed % 10 < 3) {
                        BlockState bs = srv.getBlockState(furnacePos);
                        BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) bs.getTicker(
                                srv, (BlockEntityType<BlockEntity>) furnace.getType()
                        );
                        if (ticker != null) {
                            ticker.tick(srv, furnacePos, bs, furnace);
                        }
                    }
                    if (elapsed % 20 == 0) {
                        srv.sendParticles(ParticleTypes.FLAME, furnacePos.getX() + 0.5D, furnacePos.getY() + 0.6D, furnacePos.getZ() + 0.5D,
                                3, 0.3D, 0.2D, 0.3D, 0.01D);
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(ItemTags.LOGS)) {
            int boosted = Math.min(64, Math.max(stack.getCount() + 1, Math.round(stack.getCount() * 1.25F)));
            ItemStack out = switch (level.random.nextInt(3)) {
                case 0 -> new ItemStack(Items.CRYING_OBSIDIAN, boosted);
                case 1 -> new ItemStack(Items.BLACKSTONE, boosted);
                default -> new ItemStack(Items.COAL_BLOCK, boosted);
            };
            itemEntity.setItem(out);
            return true;
        }
        if (stack.is(Items.RAW_IRON_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.IRON_INGOT, Math.min(64, stack.getCount() * 9)));
            return true;
        }
        if (stack.is(Items.RAW_GOLD_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.GOLD_INGOT, Math.min(64, stack.getCount() * 9)));
            return true;
        }
        if (stack.is(Items.RAW_COPPER_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.COPPER_INGOT, Math.min(64, stack.getCount() * 9)));
            return true;
        }
        if (stack.is(Items.RAW_IRON)) {
            itemEntity.setItem(new ItemStack(Items.IRON_INGOT, stack.getCount()));
            return true;
        }
        if (stack.is(Items.RAW_GOLD)) {
            itemEntity.setItem(new ItemStack(Items.GOLD_INGOT, stack.getCount()));
            return true;
        }
        if (stack.is(Items.RAW_COPPER)) {
            itemEntity.setItem(new ItemStack(Items.COPPER_INGOT, stack.getCount()));
            return true;
        }
        if (stack.is(Items.COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(level.random.nextBoolean() ? Items.DEEPSLATE : Items.BASALT, stack.getCount()));
            return true;
        }
        return false;
    }

    private static void refundBatchDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (caster instanceof Player player) {
            CompoundTag pData = EntityHelper.getPersistentData(player);
            long now = level.getGameTime();
            if (pData.getLong(TAG_OBLIVION_TRANSMUTE_BATCH_TICK).orElse(-10L) != now) {
                pData.putLong(TAG_OBLIVION_TRANSMUTE_BATCH_TICK, now);
                long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
                SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.OBLIVION, refund);
            }
        }
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.48 Stygian Oblivion-Extinction Hex:
        // Cuts maximum health by 25% & blocks all natural and potion healing (enforced in SpellCombatReactionEngine),
        // shreds 40% armor toughness, cuts attack damage by 35%, and deals 1.5 hearts (3.0 HP) of true fire damage
        // every 3.5s (70t) burning underwater!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 45L);
        target.igniteForSeconds(3);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, Math.min(0.80D, 0.35D * mult), 0.0D, 45L);

        LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        SpellCombatReactionEngine.hurtWithOblivionMateriaAdvantage(
                level, target, caster, (1.60F + potencyTier * 0.50F) * mult, false, true
        );

        if ((now % 70L) < 20L && (now % 20L) == 0L) {
            target.invulnerableTime = 0;
            target.hurtServer(level, level.damageSources().fellOutOfWorld(), 3.0F * mult); // 1.5 hearts true fire damage!
            level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY(0.85D), target.getZ(), 10, 0.3D, 0.35D, 0.3D, 0.03D);
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY(0.85D), target.getZ(), 5, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }

    private static boolean isFriendlyTarget(LivingEntity entity, LivingEntity caster) {
        return SpellExecutionEngine.isFriendlyAllyForOasis(caster, entity)
                || (entity instanceof SpellEidolonEntity eid && caster.getUUID().equals(eid.getOwnerUuid()));
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos origin) {
        for (int dy = 2; dy >= -6; dy--) {
            BlockPos check = origin.offset(0, dy, 0);
            BlockState ground = level.getBlockState(check);
            if (!ground.getCollisionShape(level, check).isEmpty() && level.getBlockState(check.above()).isAir()) {
                return check;
            }
        }
        return origin.below();
    }
}
