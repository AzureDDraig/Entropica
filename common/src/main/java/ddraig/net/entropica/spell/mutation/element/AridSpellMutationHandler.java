package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.network.SpellVfxEventPayload;
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
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Arid Materia (Base Type #11)</b>
 * (Spellcrafting System Guide Sections 5.6 &amp; 6.12).
 *
 * <p>Canonical Advantage: <b>Strong against WATER (+1)</b> | <b>Weak against WATER (-1)</b>
 * (Mutually volatile clash: Arid attacks deal +1 bonus to Water/soaked targets while flash-drying wet status,
 * and incoming Water spells deal +1 bonus against Arid targets and {@code Sandstorm Veil} casters).</p>
 */
public final class AridSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int DUNE_DERVISH_HEX_COLOR = 0xC2B28F;
    public static final int ARID_SUNLIT_IVORY_HEX = 0xF5E4BE;
    public static final int ARID_TERRACOTTA_GRIT_HEX = 0x9C7A4C;

    private static final String TAG_REPEATING_PULSES_LEFT = "SpellAridGlyphPulsesLeft";
    private static final String TAG_REPEATING_COOLDOWN_UNTIL = "SpellAridGlyphCooldownUntil";
    private static final String TAG_LOOT_SAND_DROPPED = "SpellAridMummifySandDropped";

    @Override
    public EssenceType materiaType() {
        return EssenceType.ARID;
    }

    // =========================================================================
    // CANONICAL TITLES & DESCRIPTIONS (GUIDE SECTION 6.12)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Sirocco Leaping Chains";
            default -> "Sirocco Dust " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Chains jump between targets along swirling desert dust threads; each jump kicks up a mini-sand plume that blinds the struck mob for 1.5s and sweeps loose items along the flight path straight to the caster.";
            default -> "Attunes projectile flow with swirling bleached dune-sandstone (#C2B28F) slipstreams that kick up blinding sand grit and sweep loose items.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Dust Devil Missile";
            case BEAM -> "Sirocco Sandstream";
            case RADIAL_BURST -> "Haboob Sandstorm Nova";
            case TOUCH_STRIKE -> "Desiccating Mummification Strike";
            case AURA -> "Sandstorm Veil";
            case GLYPH -> "Desiccation Siphon Sigil";
            case CONE -> "Sandblast Blunderbuss Wave";
            case SKYFALL -> "Haboob Sandstorm Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Fires a miniature swirling desert vortex (#C2B28F) that launches struck mobs 3 blocks backward upon collision and leaves a swirling sand particle trail.";
            case BEAM -> "Channels a continuous high-pressure jet of abrasive desert sand (#C2B28F) that steadily repels enemies with pushback velocity, inflicts Blindness while channeled, and douses surface fire.";
            case RADIAL_BURST -> "Detonates in an explosive 360-degree desert dust burst (6-block radius) that knocks mobs backward, blinds hostiles for 3s, extinguishes fires, and rapidly dries adjacent water pools into packed sand or dry mud.";
            case TOUCH_STRIKE -> "Melee strike discharges parched arid energy, inflicting Slowness II and Hunger for 4s; killing an animal or mob with this strike causes it to drop dried cured meat and sand rather than raw items.";
            case AURA -> "Cloaks the caster in a swirling dust cloud (#C2B28F); cuts mob detection range by 50%, blows incoming hostile arrows off target, and forms temporary dry sandstone crusts underfoot over water.";
            case GLYPH -> "Surface-snapped desert trap (1.0s arming, 50% Sneak-Right-Click disarm refund) releasing a 4-block burst of parched mineral dust that inflicts severe Slowness and drains hunger points.";
            case CONE -> "Sprays a wide 60-degree cone of coarse abrasive sand and sharp sandstone shrapnel; knocks enemies back, inflicts Blindness for 2s, scours grass into dry sand/dirt, and strips bark off logs (or petrifies logs without stripped variants).";
            case SKYFALL -> "Conjures a swirling 12-block desert dust storm for 6s; pelts enemies with abrasive grit, inflicts Blindness and 30% Slowness, rapidly dries water source blocks, and breaks leaf blocks into sticks.";
            case SUMMON_AGENT -> "Summons a bleached dune-sandstone (#C2B28F) Dune-Dervish Agent that attacks rapidly with twin sandblades, kicking up blinding dust clouds.";
            case SUMMON_SENTRY -> "Anchors a carved Sandstone Obelisk Sentry (#C2B28F) that fires high-velocity sand slugs with heavy knockback and blinding grit.";
            case SUMMON_DECOY -> "Conjures a shimmering Heat Mirage Decoy (#C2B28F) that taunts hostiles and explodes into a blinding 5-block dust blast upon destruction.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Mirage Step";
            case TRANSMUTE, BREAK -> "Petrofaction & Desiccation";
            case AFFLICTION -> "Desiccation Mummification Hex";
            case RESTORATION -> "Mineral Cauterization";
            default -> "Desiccated " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Dissolves the caster into a swirl of desert dust, reforming at the target location and leaving a shimmering Heat Mirage Decoy at the departure point that distracts mobs.";
            case TRANSMUTE, BREAK -> "Dries mud into clay/dirt, dries wet sponges, compacts sand into sandstone, dries tilled farmland, evaporates water, and petrifies logs without stripped variants into Petrified Wood.";
            case AFFLICTION -> "Persistent milk-immune dehydrating curse (cleansed by Water/Frozen/Restoration magic or caster defeat): inflicts Hunger III, Slowness II, disables natural health regen, doubles fire damage, and applies Desiccated Fragility (25% armor bypass & +30% knockback) on players.";
            case RESTORATION -> "Cauterizes open wounds using desert minerals, granting rapid burst healing and +2.0 Armor Toughness at the cost of 16 hunger points.";
            default -> "Infuses the spell effect with parched Arid Materia (+1 vs Water, mutually volatile against Water).";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATIONS — SIROCCO LEAPING CHAINS
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
        // Section 6.12 Sirocco Leaping Chains:
        // - Jumps between targets along 3D swirling #C2B28F dust threads
        // - Kicks up a mini-sand plume blinding struck mob for 1.5s (30t)
        // - Collects loose items along the flight path straight to the caster!
        int tier = spell.getEffectiveTier();
        Vec3 fromCenter = fromTarget.getBoundingBox().getCenter();
        Vec3 toCenter = toTarget.getBoundingBox().getCenter();

        SpellWorldVfxManager.broadcastChainArc(
                level, fromCenter, toCenter, EssenceType.ARID, null, tier
        );

        if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(toTarget, caster, spell.hasWardFriendlyFireSafeguard())) {
            toTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0));
        }

        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, toTarget.getX(), toTarget.getY(0.65D), toTarget.getZ(), 18, 0.34D, 0.42D, 0.34D, 0.06D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), DUNE_DERVISH_HEX_COLOR, 22, 0.90F, 0.14F),
                toTarget.getX(), toTarget.getY(0.55D), toTarget.getZ(), 6, 0.28D, 0.32D, 0.28D, 0.02D
        );
        level.playSound(null, toTarget.blockPosition(), SoundEvents.SAND_STEP, SoundSource.PLAYERS, 0.75F, 1.15F);

        Vec3 segSpan = toCenter.subtract(fromCenter);
        double segLen = segSpan.length();
        Vec3 segDir = segLen > 1.0E-4D ? segSpan.scale(1.0D / segLen) : new Vec3(0.0D, 0.0D, 1.0D);
        AABB pathBox = new AABB(fromCenter, toCenter).inflate(2.25D);
        Vec3 casterGoal = caster.position().add(0.0D, 0.55D, 0.0D);

        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, pathBox, Entity::isAlive)) {
            if (distanceToRaySegment(item.position(), fromCenter, segDir, segLen) <= 2.25D) {
                Vec3 toCaster = casterGoal.subtract(item.position());
                double dist = toCaster.length();
                item.setNoPickUpDelay();
                if (dist <= 3.5D) {
                    item.teleportTo(casterGoal.x, casterGoal.y, casterGoal.z);
                } else {
                    item.setDeltaMovement(toCaster.scale(1.0D / dist).scale(Math.min(1.15D, 0.55D + dist * 0.08D)));
                    item.hasImpulse = true;
                }
            }
        }
    }

    @Override
    public void onFlowMultiCastOrigin(
            ServerLevel level,
            LivingEntity caster,
            SpellFlowModifier flow,
            int branchCount,
            SpellAssembly spell
    ) {
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, caster.getX(), caster.getEyeY() - 0.2D, caster.getZ(),
                8 + branchCount * 2, 0.30D, 0.22D, 0.30D, 0.05D);
    }

    @Override
    public void onProjectileHitEntityFromFlow(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        if (hitEntity instanceof LivingEntity living && living.isAlive()) {
            if (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
                living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0));
            }
        }
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (ALL 9 ARID FORMS)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, projectile.getX(), projectile.getY(), projectile.getZ(), 8, 0.18D, 0.18D, 0.18D, 0.05D);
        level.playSound(null, caster.blockPosition(), SoundEvents.SAND_PLACE, SoundSource.PLAYERS, 0.65F, 1.30F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, projectile.getX(), projectile.getY(), projectile.getZ(), 3, 0.14D, 0.14D, 0.14D, 0.02D);
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), DUNE_DERVISH_HEX_COLOR, 18, 0.72F, 0.14F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.01D
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
        // Section 6.12 Dust Devil Missile:
        // Miniature swirling desert vortex (#C2B28F) launching struck mobs 3 blocks backward upon collision!
        int tier = spell.getEffectiveTier();
        Vec3 impactPos = hitEntity.getBoundingBox().getCenter();
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.ARID, null, 1.20F, tier
        );

        if (hitEntity instanceof LivingEntity living && living.isAlive()) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
                return;
            }
            if (!SpellCombatReactionEngine.isBossEntity(level, living)) {
                Vec3 knock = projectile.getDeltaMovement();
                Vec3 horiz = new Vec3(knock.x, 0.0D, knock.z);
                if (horiz.lengthSqr() < 1.0E-4D && caster != null) {
                    horiz = living.position().subtract(caster.position());
                    horiz = new Vec3(horiz.x, 0.0D, horiz.z);
                }
                if (horiz.lengthSqr() > 1.0E-6D) {
                    // ~1.30 horizontal + 0.38 vertical reliably launches mobs ~3 blocks backward!
                    Vec3 push = horiz.normalize().scale(1.30D);
                    SpellExecutionEngine.applySyncedMotion(living, new Vec3(push.x, 0.38D, push.z));
                }
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !slotExecutesHarmVerb(slot)) {
                float vortexDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * 0.45F * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithAridMateriaAdvantage(level, living, caster, vortexDmg, false, true);
            }
        }

        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, projectile.getX(), projectile.getY(0.5D), projectile.getZ(), 24, 0.38D, 0.38D, 0.38D, 0.09D);
        level.playSound(null, BlockPos.containing(impactPos), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.85F, 0.90F);
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
        BlockPos hitPos = blockHit.getBlockPos();
        extinguishFireAround(level, hitPos, 1);
        Vec3 loc = blockHit.getLocation();
        Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i());
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, loc, normal, EssenceType.ARID, null, 1.05F, spell.getEffectiveTier()
        );
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState());
        level.sendParticles(sandDust, loc.x, loc.y + 0.1D, loc.z, 18, 0.28D, 0.22D, 0.28D, 0.07D);
        level.playSound(null, hitPos, SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.75F, 0.95F);
    }

    // =========================================================================
    // FORM MUTATION 2 — SIROCCO SANDSTREAM (BEAM)
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
        // Section 6.12 Sirocco Sandstream (#C2B28F):
        // Continuous high-pressure jet of abrasive desert sand steadily repelling enemies with pushback velocity,
        // inflicting Blindness while channeled, and dousing surface fire!
        int tier = spell.getEffectiveTier();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float beamDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.25F, apparatusPotency) * 0.65F * flawPot;

        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 muzzle = caster.getEyePosition().subtract(0.0D, 0.18D, 0.0D).add(normDir.scale(0.35D));
        Vec3 endPos = blockHit.getType() == HitResult.Type.BLOCK
                ? blockHit.getLocation()
                : muzzle.add(normDir.scale(maxReach));

        SpellWorldVfxManager.broadcastChanneledBeamSlice(
                level, muzzle, endPos, EssenceType.ARID, null, tier, 8
        );

        Vec3 horizDir = new Vec3(normDir.x, 0.0D, normDir.z);
        Vec3 push = horizDir.lengthSqr() > 1.0E-4D
                ? horizDir.normalize().scale(0.88D)
                : normDir.scale(0.88D);

        Set<UUID> seen = new HashSet<>();
        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster || !seen.add(hit.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            hit.clearFire();
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(push.x, Math.max(0.20D, hit.getDeltaMovement().y), push.z));
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithAridMateriaAdvantage(level, hit, caster, beamDmg, false, false);
            }
        }

        double effectiveLen = Math.min(maxReach, muzzle.distanceTo(endPos) + 0.75D);
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        for (double d = 1.0D; d <= Math.min(effectiveLen, 24.0D); d += 1.25D) {
            Vec3 pt = muzzle.add(normDir.scale(d));
            extinguishFireAround(level, BlockPos.containing(pt), 1);
            if (((int) d & 1) == 0) {
                level.sendParticles(sandDust, pt.x, pt.y, pt.z, 2, 0.14D, 0.14D, 0.14D, 0.03D);
            }
        }
        if (level.getGameTime() % 6L == 0L) {
            level.playSound(null, caster.blockPosition(), SoundEvents.SAND_STEP, SoundSource.PLAYERS, 0.65F, 0.80F);
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — HABOOB SANDSTORM NOVA (RADIAL BURST)
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
        // Section 6.12 Haboob Sandstorm Nova:
        // Explosive 360° desert dust burst (6-block radius) knocking mobs backward, blinding hostiles for 3s (60t),
        // extinguishing fires, and rapidly drying adjacent water pools into packed sand or dry mud!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float novaDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.45F, apparatusPotency) * 0.60F * spell.flawProfile().getPotencyMultiplier();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.ARID, null, tier);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (e == caster || e.position().distanceTo(center) > r + 0.5D) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            e.clearFire();
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            if (!SpellCombatReactionEngine.isBossEntity(level, e)) {
                Vec3 away = e.position().subtract(center);
                Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                Vec3 dir = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                SpellExecutionEngine.applySyncedMotion(e, new Vec3(dir.x * 1.22D, 0.38D, dir.z * 1.22D));
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithAridMateriaAdvantage(level, e, caster, novaDmg, false, true);
            }
        }

        BlockPos cPos = BlockPos.containing(center);
        int br = Math.min(6, (int) Math.ceil(r));
        double rSq = (r + 0.45D) * (r + 0.45D);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-br, -2, -br), cPos.offset(br, 2, br))) {
            if (p.distToCenterSqr(center) > rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(BlockTags.FIRE)) {
                level.removeBlock(p, false);
            } else if (bs.is(Blocks.WATER) || bs.is(Blocks.BUBBLE_COLUMN)) {
                // Dry shallow/bed water blocks into packed sand or dry mud, and evaporate upper water layers cleanly!
                BlockState below = level.getBlockState(p.below());
                if (!below.isAir() && !below.is(Blocks.WATER) && !below.is(Blocks.BUBBLE_COLUMN)) {
                    level.setBlockAndUpdate(p, level.random.nextBoolean()
                            ? Blocks.SAND.defaultBlockState()
                            : Blocks.PACKED_MUD.defaultBlockState());
                } else {
                    level.removeBlock(p, false);
                }
            } else if (bs.is(Blocks.WET_SPONGE)) {
                level.setBlockAndUpdate(p, Blocks.SPONGE.defaultBlockState());
            } else if (bs.is(Blocks.MUD)) {
                level.setBlockAndUpdate(p, Blocks.PACKED_MUD.defaultBlockState());
            }
        }

        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, center.x, center.y + 0.85D, center.z, 52, r * 0.45D, 0.65D, r * 0.45D, 0.12D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), DUNE_DERVISH_HEX_COLOR, 28, 1.15F, 0.18F),
                center.x, center.y + 0.65D, center.z, 18, r * 0.35D, 0.45D, r * 0.35D, 0.03D
        );
        level.playSound(null, cPos, SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 1.0F, 0.72F);
        level.playSound(null, cPos, SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.75F, 0.85F);
    }

    // =========================================================================
    // FORM MUTATION 4 — DESICCATING MUMMIFICATION STRIKE (TOUCH / STRIKE)
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
        // Section 6.12 Desiccating Mummification Strike:
        // Inflicts Slowness II and Hunger for 4s (80t); killing an animal or mob with this strike causes it
        // to drop dried cured meat and sand rather than raw items!
        int tier = spell.getEffectiveTier();
        long mummifyUntil = level.getGameTime() + 100L;
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float strikeDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.45F, apparatusPotency) * spell.flawProfile().getPotencyMultiplier();
        Vec3 slashDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.28D, 0.0D), slashDir, 2.85D, EssenceType.ARID, null, tier
        );

        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState());
        for (LivingEntity hit : hitEntities) {
            if (hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_ARID_MUMMIFY_DROPS_UNTIL, mummifyUntil);

            if (hit.isAlive() && !spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithAridMateriaAdvantage(level, hit, caster, strikeDmg, true, true);
            }

            if (hit.isAlive()) {
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                hit.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0));
            } else {
                desiccateFreshNearbyLoot(level, hit);
            }

            level.sendParticles(sandDust, hit.getX(), hit.getY(0.7D), hit.getZ(), 18, 0.30D, 0.40D, 0.30D, 0.07D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.90F, 0.85F);
    }

    /**
     * Converts raw animal/mob drops into dried cured meat equivalents for <i>Desiccating Mummification Strike</i>
     * (invoked both by {@code MobDropHandler} during loot generation and by {@link #desiccateFreshNearbyLoot}).
     */
    public static ItemStack desiccateRawDropToCuredMeat(ServerLevel level, ItemStack raw) {
        if (raw.isEmpty()) return raw;
        int count = raw.getCount();

        if (raw.is(Items.BEEF)) return new ItemStack(Items.COOKED_BEEF, count);
        if (raw.is(Items.PORKCHOP)) return new ItemStack(Items.COOKED_PORKCHOP, count);
        if (raw.is(Items.CHICKEN)) return new ItemStack(Items.COOKED_CHICKEN, count);
        if (raw.is(Items.MUTTON)) return new ItemStack(Items.COOKED_MUTTON, count);
        if (raw.is(Items.RABBIT)) return new ItemStack(Items.COOKED_RABBIT, count);
        if (raw.is(Items.COD)) return new ItemStack(Items.COOKED_COD, count);
        if (raw.is(Items.SALMON)) return new ItemStack(Items.COOKED_SALMON, count);
        if (raw.is(ModItems.RAW_AURORAFOWL.get())) return new ItemStack(ModItems.COOKED_AURORAFOWL.get(), count);
        if (raw.is(Items.ROTTEN_FLESH)) return new ItemStack(Items.COOKED_BEEF, count);
        if (raw.is(Items.KELP)) return new ItemStack(Items.DRIED_KELP, count);
        if (raw.is(Items.WET_SPONGE)) return new ItemStack(Items.SPONGE, count);

        if (raw.is(ItemTags.MEAT) || raw.is(ItemTags.FISHES) || raw.has(DataComponents.FOOD)) {
            Optional<RecipeHolder<SmeltingRecipe>> smelted = level.recipeAccess()
                    .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(raw.copyWithCount(1)), level);
            if (smelted.isPresent()) {
                ItemStack out = smelted.get().value().assemble(new SingleRecipeInput(raw.copyWithCount(1)), level.registryAccess());
                if (!out.isEmpty()) {
                    out.setCount(Math.min(out.getMaxStackSize(), count * Math.max(1, out.getCount())));
                    return out;
                }
            }
        }
        return raw;
    }

    /**
     * Sweeps freshly spawned loot entities around {@code slainEntity} when a direct lethal hit kills an animal/mob,
     * converting raw drops into cured meat and spawning 1–2 desiccated sand blocks once per slain creature.
     */
    public static void desiccateFreshNearbyLoot(ServerLevel level, LivingEntity slainEntity) {
        AABB dropBox = slainEntity.getBoundingBox().inflate(2.4D);
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, dropBox, Entity::isAlive)) {
            if (itemEntity.getAge() <= 5) {
                ItemStack stack = itemEntity.getItem();
                ItemStack cured = desiccateRawDropToCuredMeat(level, stack);
                if (cured != stack) {
                    itemEntity.setItem(cured);
                }
            }
        }

        CompoundTag data = EntityHelper.getPersistentData(slainEntity);
        if (!data.getBoolean(TAG_LOOT_SAND_DROPPED).orElse(false)) {
            data.putBoolean(TAG_LOOT_SAND_DROPPED, true);
            ItemEntity sandDrop = new ItemEntity(
                    level,
                    slainEntity.getX(),
                    slainEntity.getY() + 0.25D,
                    slainEntity.getZ(),
                    new ItemStack(Items.SAND, 1 + level.random.nextInt(2))
            );
            sandDrop.setDefaultPickUpDelay();
            level.addFreshEntity(sandDrop);
        }

        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, slainEntity.getX(), slainEntity.getY(0.5D), slainEntity.getZ(),
                20, 0.35D, 0.35D, 0.35D, 0.06D);
    }

    // =========================================================================
    // FORM MUTATION 5 — SANDSTORM VEIL (AURA / SELF)
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
        // Section 6.12 Sandstorm Veil (#C2B28F):
        // - Cuts hostile mob AI detection range by an exact 50% (CommonLivingEntityMixin)
        // - Blows incoming hostile arrows off target & forms temporary dry sandstone crusts over water (SpellLingeringZoneManager)
        // - Registers Arid defensive affinity (mutually volatile clash: 1.50x incoming Water damage)
        int tier = spell.getEffectiveTier();
        int dur = 200 + tier * 40;
        long now = level.getGameTime();

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_ARID_VEIL_UNTIL, now + dur);
        data.putInt("SpellWardTier", tier);

        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.75F, null);
        SpellWorldVfxManager.broadcastWardShell(
                level, caster.position(), Math.max(1.35D, caster.getBbWidth() * 1.35D),
                EssenceType.ARID, null, Math.min(dur, 120), tier
        );
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, caster.position().add(0.0D, 0.04D, 0.0D), 2.6D, EssenceType.ARID, null, 52, tier
        );

        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, caster.getX(), caster.getY(0.8D), caster.getZ(), 30, 0.55D, 0.65D, 0.55D, 0.08D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), DUNE_DERVISH_HEX_COLOR, 32, 1.05F, 0.10F),
                caster.getX(), caster.getY(0.85D), caster.getZ(), 14, 0.55D, 0.55D, 0.55D, 0.02D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.SAND_PLACE, SoundSource.PLAYERS, 0.85F, 0.90F);
    }

    // =========================================================================
    // FORM MUTATION 6 — DESICCATION SIPHON SIGIL (GLYPH / TRAP)
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
        // Section 4.6 & 6.12 Desiccation Siphon Sigil:
        // - Surface-snapped desert trap (1.0s arming, 10m or Tier 5+ indefinite lifetime, 50% Sneak-Right-Click disarm refund)
        // - Supports Single-Use Detonator (100%) and Repeating Glyphic Sentry (3 pulses @ 60%, 2.0s re-arm)
        // - Releases a 4-block burst of parched mineral dust inflicting severe Slowness & draining hunger points!
        int tier = spell.getEffectiveTier();
        int maxWaitTicks = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        BlockPos surfaceBlock = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snappedCenter = new Vec3(glyphCenter.x, surfaceBlock.getY() + 1.02D, glyphCenter.z);
        double padRad = Math.max(2.4D, Math.min(4.0D, radius));

        String subMode = slot.primaryEffect().subMode();
        boolean repeatingSentry = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        CompoundTag casterTag = EntityHelper.getPersistentData(caster);
        String pulseKey = TAG_REPEATING_PULSES_LEFT + "_" + level.getGameTime();
        String cdKey = TAG_REPEATING_COOLDOWN_UNTIL + "_" + level.getGameTime();
        if (repeatingSentry) {
            casterTag.putInt(pulseKey, 3);
            casterTag.putLong(cdKey, 0L);
        }

        SpellWorldVfxManager.broadcastArcaneSeal(level, snappedCenter, 2.2D, EssenceType.ARID, null, 52, tier);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_FEHU.get(), DUNE_DERVISH_HEX_COLOR, 32, 0.90F, 0.04F),
                snappedCenter.x, snappedCenter.y + 0.08D, snappedCenter.z, 5, 0.45D, 0.05D, 0.45D, 0.005D
        );
        level.playSound(null, BlockPos.containing(snappedCenter), SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 0.80F, 0.95F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, 4.0D, maxWaitTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 24 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.RUNE_FEHU.get(), DUNE_DERVISH_HEX_COLOR, 26, 0.82F, 0.03F),
                                zone.center.x, zone.center.y + 0.06D, zone.center.z, 2, 0.55D, 0.03D, 0.55D, 0.002D
                        );
                    }
                    if (elapsed < 20) return true; // 1.0s arming delay (Section 4.6)

                    LivingEntity owner = zone.resolveCaster(srv);
                    long now = srv.getGameTime();
                    if (repeatingSentry && owner != null) {
                        long cdUntil = EntityHelper.getPersistentData(owner).getLong(cdKey).orElse(0L);
                        if (now < cdUntil) return true;
                    }

                    AABB triggerBox = new AABB(
                            zone.center.x - padRad, zone.center.y - 1.0D, zone.center.z - padRad,
                            zone.center.x + padRad, zone.center.y + 2.2D, zone.center.z + padRad
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive()
                                    && e != owner
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                            e, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard()))
                    );
                    if (stepped.isEmpty()) return true;

                    float pulseScale = repeatingSentry ? 0.60F : 1.0F;
                    int debuffDur = Math.max(60, Math.round((100 + tier * 15) * pulseScale));
                    int hungerDrain = Math.max(3, Math.round((6 + tier / 2.0F) * pulseScale));
                    double siphonRad = 4.0D;
                    boolean hasHarm = slotExecutesHarmVerb(zone.slot);
                    float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                    float sigilDmg = Math.max(3.2F + tier * 1.05F,
                            MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.45F, zone.apparatusPotency) * 0.70F)
                            * flawPot * pulseScale;

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, siphonRad, EssenceType.ARID, null, tier);
                    SpellWorldVfxManager.broadcastTornadoVortex(srv, zone.center, 2.2D, 4.5D, EssenceType.ARID, null, 60, tier);
                    BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState());
                    srv.sendParticles(sandDust, zone.center.x, zone.center.y + 0.65D, zone.center.z, 44, 1.45D, 0.55D, 1.45D, 0.12D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 1.0F, 0.70F);

                    AABB siphonBox = new AABB(
                            zone.center.x - siphonRad, zone.center.y - 2.0D, zone.center.z - siphonRad,
                            zone.center.x + siphonRad, zone.center.y + 3.2D, zone.center.z + siphonRad
                    );
                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, siphonBox, LivingEntity::isAlive)) {
                        if (v == owner || v.position().distanceTo(zone.center) > siphonRad + 0.45D) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                v, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }

                        // Severe Slowness (Slowness III + 45% transient speed sap) & Hunger point drain!
                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, debuffDur, 2));
                        v.addEffect(new MobEffectInstance(MobEffects.HUNGER, debuffDur, 2));
                        SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.45D * pulseScale, debuffDur);

                        if (v instanceof Player player && !player.getAbilities().invulnerable) {
                            FoodData food = player.getFoodData();
                            food.setFoodLevel(Math.max(0, food.getFoodLevel() - hungerDrain));
                            food.setSaturation(Math.max(0.0F, food.getSaturationLevel() - hungerDrain));
                        } else {
                            // Living non-player mobs suffer parched metabolic exhaustion (Weakness)!
                            v.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, debuffDur, 0));
                        }

                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * pulseScale
                            );
                        }
                        if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm() && !hasHarm && v.isAlive()) {
                            SpellCombatReactionEngine.hurtWithAridMateriaAdvantage(srv, v, owner, sigilDmg, false, true);
                        }
                    }

                    if (repeatingSentry && owner != null) {
                        CompoundTag oTag = EntityHelper.getPersistentData(owner);
                        int rem = oTag.getInt(pulseKey).orElse(1) - 1;
                        if (rem > 0) {
                            oTag.putInt(pulseKey, rem);
                            oTag.putLong(cdKey, now + 40L); // 2.0s re-arm delay (Section 4.6)
                            return true;
                        }
                        oTag.remove(pulseKey);
                        oTag.remove(cdKey);
                    }
                    return false;
                }
        ).markDisarmableGlyph());
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — SANDBLAST BLUNDERBUSS WAVE (CONE / SPRAY)
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
        // Section 6.12 Sandblast Blunderbuss Wave:
        // Wide 60-degree cone of coarse abrasive sand & sharp sandstone shrapnel:
        // - Heavy pushback knockback & Blindness for 2s (40t)
        // - Scours grass off dirt into dry sand/dirt
        // - Strips bark off logs if a stripped variant exists; if no stripped variant exists, transmutes into Petrified Wood!
        int tier = spell.getEffectiveTier();
        double range = Math.max(5.5D, coneRange);
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 originEye = caster.getEyePosition().subtract(0.0D, 0.20D, 0.0D);
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float shrapnelDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.45F, apparatusPotency) * 0.65F * spell.flawProfile().getPotencyMultiplier();

        SpellWorldVfxManager.broadcastVortexCone(level, originEye, normDir, range, EssenceType.ARID, null, tier);

        // Sweep full 60-degree cone (+/- 30 deg -> cos(30 deg) = 0.866025) for entities & terrain blocks!
        double cosHalfCone = 0.866D;
        Set<UUID> seen = new HashSet<>();
        AABB entityBox = caster.getBoundingBox().expandTowards(normDir.scale(range)).inflate(range * 0.65D);
        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, entityBox, LivingEntity::isAlive)) {
            if (cand == caster) continue;
            Vec3 toCand = cand.getBoundingBox().getCenter().subtract(originEye);
            double dist = toCand.length();
            boolean inPassedList = hitTargets.contains(cand);
            if (!inPassedList && (dist > range + 0.6D || dist < 1.0E-4D || toCand.scale(1.0D / dist).dot(normDir) < cosHalfCone)) {
                continue;
            }
            if (!seen.add(cand.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }

            cand.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            cand.clearFire();
            if (!SpellCombatReactionEngine.isBossEntity(level, cand)) {
                SpellExecutionEngine.applySyncedMotion(cand, new Vec3(normDir.x * 1.40D, 0.38D, normDir.z * 1.40D));
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithAridMateriaAdvantage(level, cand, caster, shrapnelDmg, false, true);
            }
        }

        int blockReach = Math.min(9, (int) Math.ceil(range));
        BlockPos startBp = BlockPos.containing(originEye);
        double rangeSq = (range + 0.5D) * (range + 0.5D);
        for (BlockPos p : BlockPos.betweenClosed(
                startBp.offset(-blockReach, -2, -blockReach),
                startBp.offset(blockReach, 3, blockReach))) {
            Vec3 toBlock = Vec3.atCenterOf(p).subtract(originEye);
            double dSq = toBlock.lengthSqr();
            if (dSq < 0.5D || dSq > rangeSq) continue;
            if (toBlock.scale(1.0D / Math.sqrt(dSq)).dot(normDir) < cosHalfCone) continue;

            BlockState bs = level.getBlockState(p);
            if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;

            if (bs.is(BlockTags.FIRE)) {
                level.removeBlock(p, false);
            } else if (bs.is(Blocks.GRASS_BLOCK) || bs.is(Blocks.PODZOL) || bs.is(Blocks.MYCELIUM)
                    || bs.is(Blocks.MOSS_BLOCK) || bs.is(Blocks.ROOTED_DIRT) || bs.is(Blocks.FARMLAND)) {
                level.setBlockAndUpdate(p, level.random.nextBoolean()
                        ? Blocks.SAND.defaultBlockState()
                        : Blocks.DIRT.defaultBlockState());
            } else if (bs.is(BlockTags.LOGS)) {
                stripOrPetrifyLog(level, p, bs);
            } else if (bs.is(BlockTags.WOODEN_SLABS)) {
                petrifyWoodenSlab(level, p, bs);
            }
        }

        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState());
        level.sendParticles(sandDust,
                originEye.x + normDir.x * 2.4D,
                originEye.y + normDir.y * 2.4D,
                originEye.z + normDir.z * 2.4D,
                42, 0.95D, 0.55D, 0.95D, 0.16D);
        level.playSound(null, caster.blockPosition(), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 1.0F, 0.80F);
        level.playSound(null, caster.blockPosition(), SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 0.80F, 0.75F);
    }

    // =========================================================================
    // FORM MUTATION 8 — HABOOB SANDSTORM DOWNPOUR (RAIN / SKYFALL)
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
        // Section 6.12 Haboob Sandstorm Downpour:
        // Swirling 12-block desert dust storm (radius >= 6.0m -> 12-block diameter) for 6 seconds (120t);
        // pelts enemies with abrasive grit, inflicts Blindness and 30% Slowness, rapidly dries up water source blocks,
        // and breaks leaf blocks into sticks!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        int stormTicks = 120; // Canonical 6.0s (120t)

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.ARID, null, stormTicks, tier);
        level.playSound(null, BlockPos.containing(impactCenter), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 0.68F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, stormTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    BlockPos groundFloor = BlockPos.containing(zone.center);
                    double ceilingH = probeOverheadClearance(srv, groundFloor, 9);

                    if (elapsed % 4 == 0) {
                        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
                        srv.sendParticles(sandDust,
                                zone.center.x, zone.center.y + ceilingH * 0.55D, zone.center.z,
                                30, zone.radius * 0.55D, ceilingH * 0.35D, zone.radius * 0.55D, 0.10D);
                    }

                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        int br = Math.min(7, (int) Math.ceil(zone.radius));
                        int topY = Math.max(2, (int) Math.ceil(ceilingH));
                        double rSq = (zone.radius + 0.5D) * (zone.radius + 0.5D);

                        for (BlockPos p : BlockPos.betweenClosed(groundFloor.offset(-br, -1, -br), groundFloor.offset(br, topY, br))) {
                            double dx = (p.getX() + 0.5D) - zone.center.x;
                            double dz = (p.getZ() + 0.5D) - zone.center.z;
                            if (dx * dx + dz * dz > rSq) continue;

                            BlockState bs = srv.getBlockState(p);
                            if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, bs)) continue;
                            if (bs.is(BlockTags.FIRE)) {
                                srv.removeBlock(p, false);
                            } else if (bs.is(Blocks.WATER) || bs.is(Blocks.BUBBLE_COLUMN)) {
                                srv.removeBlock(p, false);
                            } else if (bs.is(BlockTags.LEAVES) && srv.random.nextFloat() < 0.45F) {
                                srv.removeBlock(p, false);
                                Block.popResource(srv, p, new ItemStack(Items.STICK, 1 + srv.random.nextInt(2)));
                            }
                        }

                        boolean hasHarm = slotExecutesHarmVerb(zone.slot);
                        float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                        float gritDmg = Math.max(1.5F,
                                MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.35F, zone.apparatusPotency) * 0.32F * flawPot);

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + ceilingH + 0.5D, zone.center.z + zone.radius
                        );
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            double dx = v.getX() - zone.center.x;
                            double dz = v.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > rSq) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                    v, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                                continue;
                            }

                            v.clearFire();
                            v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                            v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 1));
                            // Exact 30% movement speed sap (Guide Section 6.12)!
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.30D, 25L);

                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                                );
                            }
                            if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm() && !hasHarm && v.isAlive()) {
                                SpellCombatReactionEngine.hurtWithAridMateriaAdvantage(srv, v, owner, gritDmg, false, true);
                            }
                        }
                        srv.playSound(null, groundFloor, SoundEvents.SAND_STEP, SoundSource.PLAYERS, 0.70F, 0.75F);
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — DUNE-DERVISH EIDOLON SUITE (SUMMON TRIAD)
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
        tag.putString("SpellEidolonMateria", EssenceType.ARID.name());
        tag.putInt("SpellEidolonTintRGB", DUNE_DERVISH_HEX_COLOR);

        SpellWorldVfxManager.broadcastSummonPillar(level, spawnPos, 1.35D, EssenceType.ARID, null, tier);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(BlockPos.containing(spawnPos)).inflate(3.2D), LivingEntity::isAlive)) {
            if (foe == caster || foe == summonEntity) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        }

        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState());
        level.sendParticles(sandDust, spawnPos.x, spawnPos.y + 0.85D, spawnPos.z, 28, 0.48D, 0.65D, 0.48D, 0.08D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 0.90F, 0.90F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — MIRAGE STEP, PETROFACTION, HEX, CAUTERIZE
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.12 Mirage Step:
        // Dissolves the caster into a swirl of desert dust, reforming at the target location and leaving
        // a shimmering 3D Heat Mirage Decoy at the departure point that distracts mobs!
        int tier = Math.max(1, potencyTier);
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.ARID, tier);
        SpellWorldVfxManager.broadcastTornadoVortex(level, departurePos, 1.85D, 4.5D, EssenceType.ARID, null, 60, tier);
        SpellWorldVfxManager.broadcastTornadoVortex(level, arrivalPos, 1.85D, 4.5D, EssenceType.ARID, null, 60, tier);

        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, departurePos.x, departurePos.y + 0.95D, departurePos.z, 28, 0.38D, 0.65D, 0.38D, 0.08D);
        level.sendParticles(sandDust, arrivalPos.x, arrivalPos.y + 0.95D, arrivalPos.z, 24, 0.38D, 0.65D, 0.38D, 0.08D);
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.85F, 0.95F);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.SAND_PLACE, SoundSource.PLAYERS, 0.85F, 1.10F);

        // Spawn a real shimmering 3D Heat Mirage Decoy (mirroring caster equipment) for 4.0s (80t) at departurePos!
        float mirageHp = 10.0F + tier * 3.0F;
        SpellEidolonEntity mirageDecoy = SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.ARID, mirageHp, 80, "Heat Mirage Decoy", null
        );
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(BlockPos.containing(departurePos)).inflate(9.0D), LivingEntity::isAlive)) {
            if (mob == mirageDecoy || mob.isAlliedTo(caster)) continue;
            if (mob instanceof Monster || mob.getTarget() == caster) {
                mob.setTarget(mirageDecoy);
            }
        }
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.12 Petrofaction & Desiccation:
        // Dries mud into clay/dirt, dries wet sponges, compacts sand into sandstone, dries tilled farmland,
        // evaporates water, and petrifies logs without stripped variants into Petrified Wood!
        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.25D), Entity::isAlive)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }

        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return didAny;
        }

        if (state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            emitDesiccationVfx(level, pos);
            return true;
        }
        if (state.is(Blocks.MUDDY_MANGROVE_ROOTS)) {
            level.setBlockAndUpdate(pos, Blocks.MANGROVE_ROOTS.defaultBlockState());
            emitDesiccationVfx(level, pos);
            return true;
        }
        if (state.is(Blocks.PACKED_MUD) || state.is(Blocks.CLAY)) {
            level.setBlockAndUpdate(pos, Blocks.TERRACOTTA.defaultBlockState());
            emitDesiccationVfx(level, pos);
            return true;
        }
        if (state.is(Blocks.WET_SPONGE)) {
            level.setBlockAndUpdate(pos, Blocks.SPONGE.defaultBlockState());
            emitDesiccationVfx(level, pos);
            return true;
        }
        if (state.is(Blocks.SAND)) {
            level.setBlockAndUpdate(pos, Blocks.SANDSTONE.defaultBlockState());
            emitDesiccationVfx(level, pos);
            return true;
        }
        if (state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, Blocks.RED_SANDSTONE.defaultBlockState());
            emitDesiccationVfx(level, pos);
            return true;
        }
        if (state.is(Blocks.FARMLAND) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.ROOTED_DIRT)) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            emitDesiccationVfx(level, pos);
            return true;
        }
        if (state.is(Blocks.WATER) || state.is(Blocks.BUBBLE_COLUMN)) {
            level.removeBlock(pos, false);
            emitDesiccationVfx(level, pos);
            return true;
        }
        if (state.is(BlockTags.WOODEN_SLABS)) {
            petrifyWoodenSlab(level, pos, state);
            emitDesiccationVfx(level, pos);
            return true;
        }
        if (state.is(BlockTags.LOGS)) {
            boolean changed = stripOrPetrifyLog(level, pos, state);
            if (changed) {
                emitDesiccationVfx(level, pos);
            }
            return changed || didAny;
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        int count = stack.getCount();
        ItemStack result = ItemStack.EMPTY;

        if (stack.is(Items.WET_SPONGE)) {
            result = new ItemStack(Items.SPONGE, count);
        } else if (stack.is(Items.MUD)) {
            result = new ItemStack(Items.CLAY, count);
        } else if (stack.is(Items.CLAY_BALL)) {
            result = new ItemStack(Items.BRICK, count);
        } else if (stack.is(Items.CLAY) || stack.is(Items.PACKED_MUD)) {
            result = new ItemStack(Items.TERRACOTTA, count);
        } else if (stack.is(Items.SAND)) {
            result = new ItemStack(Items.SANDSTONE, count);
        } else if (stack.is(Items.RED_SAND)) {
            result = new ItemStack(Items.RED_SANDSTONE, count);
        } else if (stack.is(Items.ROTTEN_FLESH)) {
            result = new ItemStack(Items.LEATHER, count);
        } else if (stack.is(Items.KELP)) {
            result = new ItemStack(Items.DRIED_KELP, count);
        } else if (stack.is(Items.WATER_BUCKET)) {
            result = new ItemStack(Items.BUCKET, count);
        } else if (stack.is(ItemTags.WOODEN_SLABS)) {
            result = new ItemStack(Items.PETRIFIED_OAK_SLAB, count);
        } else if (stack.is(ItemTags.LOGS)) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            Optional<net.minecraft.world.item.Item> strippedOpt = id.getPath().startsWith("stripped_")
                    ? Optional.empty()
                    : BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "stripped_" + id.getPath()));
            result = strippedOpt.map(item -> new ItemStack(item, count))
                    .orElseGet(() -> new ItemStack(Items.POLISHED_BASALT, count));
        } else {
            ItemStack cured = desiccateRawDropToCuredMeat(level, stack);
            if (cured != stack) {
                result = cured;
            }
        }

        if (!result.isEmpty()) {
            itemEntity.setItem(result);
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState());
            level.sendParticles(sandDust, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 10, 0.20D, 0.20D, 0.20D, 0.05D);
            level.playSound(null, itemEntity.blockPosition(), SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 0.55F, 1.15F);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 5.6 & 6.12 Desiccation Mummification Hex:
        // - Persistent milk-immune dehydrating curse clinging until cleansed via Water/Frozen/Restoration magic or caster defeat
        // - Permanently inflicts Hunger III, Slowness II, and disables natural health regeneration (TAG_HEX_MAX_ALLOWED_HP)
        // - Doubles incoming Fire damage (2.0x in SpellCombatReactionEngine)
        // - Player Hex Vector (Desiccated Fragility): 25% physical melee armor bypass & +30% knockback!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 38, 2, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 38, 1, true, true));
        if (target instanceof Player player) {
            player.getFoodData().setSaturation(0.0F);
        }

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float parchDamage = (1.05F + potencyTier * 0.42F) * mult;
        SpellCombatReactionEngine.hurtWithAridMateriaAdvantage(level, target, hexCaster, parchDamage, false, false);

        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, target.getX(), target.getY(0.7D), target.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.04D);
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
            // Section 6.12 Mineral Cauterization:
            // Cauterizes open wounds using desert minerals, granting rapid burst healing and +2.0 Armor Toughness
            // at the cost of 16 hunger points!
            Player hungerPayer = target instanceof Player tp ? tp : (caster instanceof Player cp ? cp : null);
            float metabolicScale = 1.0F;
            if (hungerPayer != null && !hungerPayer.getAbilities().invulnerable) {
                FoodData food = hungerPayer.getFoodData();
                int currentFood = food.getFoodLevel();
                if (currentFood <= 0) {
                    hungerPayer.displayClientMessage(
                            Component.literal("§6[Mineral Cauterization] §7Too parched and starving (0 hunger) to cauterize wounds!"),
                            true
                    );
                    return;
                }
                int drained = Math.min(16, currentFood);
                metabolicScale = drained / 16.0F;
                food.setFoodLevel(currentFood - drained);
                food.setSaturation(Math.max(0.0F, food.getSaturationLevel() - 6.0F));
            }

            float healAmt = (7.0F + potencyTier * 1.75F) * metabolicScale;
            int buffDur = Math.max(80, Math.round((200 + potencyTier * 30) * metabolicScale));

            SpellCombatReactionEngine.notifyMagicalHeal(target, healAmt);
            target.heal(healAmt);
            // Canonical +2.0 Armor Toughness mineral crust!
            SpellCombatReactionEngine.applyTimedDefenseBuff(target, level, 2.0D, 0.0D, buffDur);

            BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState());
            level.sendParticles(sandDust, target.getX(), target.getY(0.8D), target.getZ(), 20, 0.32D, 0.45D, 0.32D, 0.06D);
            level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY(1.05D), target.getZ(),
                    Math.min(6, 2 + potencyTier / 2), 0.30D, 0.25D, 0.30D, 0.02D);
            level.playSound(null, target.blockPosition(), SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 0.85F, 1.20F);
        }
    }

    // =========================================================================
    // SHARED BLOCK & FUEL HELPERS
    // =========================================================================

    /**
     * Strips bark off {@code state} if a {@code stripped_<id>} block exists in the registry;
     * if no stripped variant exists (or the log is already stripped), transmutes it into mineral Petrified Wood
     * ({@link Blocks#POLISHED_BASALT} preserving {@link RotatedPillarBlock#AXIS}).
     */
    public static boolean stripOrPetrifyLog(ServerLevel level, BlockPos pos, BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        Direction.Axis axis = state.hasProperty(RotatedPillarBlock.AXIS)
                ? state.getValue(RotatedPillarBlock.AXIS)
                : Direction.Axis.Y;

        if (!id.getPath().startsWith("stripped_")) {
            ResourceLocation strippedId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "stripped_" + id.getPath());
            Optional<Block> strippedOpt = BuiltInRegistries.BLOCK.getOptional(strippedId);
            if (strippedOpt.isPresent()) {
                BlockState next = strippedOpt.get().defaultBlockState();
                if (next.hasProperty(RotatedPillarBlock.AXIS)) {
                    next = next.setValue(RotatedPillarBlock.AXIS, axis);
                }
                level.setBlockAndUpdate(pos, next);
                return true;
            }
        }

        // No stripped variant exists (or already stripped) -> petrify into mineral Petrified Wood pillar!
        BlockState petrified = Blocks.POLISHED_BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        level.setBlockAndUpdate(pos, petrified);
        return true;
    }

    private static void petrifyWoodenSlab(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState petrifiedSlab = Blocks.PETRIFIED_OAK_SLAB.defaultBlockState();
        if (state.hasProperty(SlabBlock.TYPE) && petrifiedSlab.hasProperty(SlabBlock.TYPE)) {
            SlabType slabType = state.getValue(SlabBlock.TYPE);
            petrifiedSlab = petrifiedSlab.setValue(SlabBlock.TYPE, slabType);
        }
        if (petrifiedSlab.hasProperty(SlabBlock.WATERLOGGED)) {
            petrifiedSlab = petrifiedSlab.setValue(SlabBlock.WATERLOGGED, false);
        }
        level.setBlockAndUpdate(pos, petrifiedSlab);
    }

    private static void emitDesiccationVfx(ServerLevel level, BlockPos pos) {
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState());
        level.sendParticles(sandDust, pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                12, 0.28D, 0.28D, 0.28D, 0.05D);
        level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 0.55F, 1.05F);
    }

    private static void extinguishFireAround(ServerLevel level, BlockPos center, int rad) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-rad, -1, -rad), center.offset(rad, 1, rad))) {
            if (level.getBlockState(p).is(BlockTags.FIRE)) {
                level.removeBlock(p, false);
            }
        }
    }

    private static boolean slotExecutesHarmVerb(@Nullable SubSpellSlot slot) {
        if (slot == null) return false;
        if (slot.primaryEffect().effect() == SpellCoreEffect.HARM) return true;
        return slot.secondaryEffect().map(s -> s.effect() == SpellCoreEffect.HARM).orElse(false);
    }

    private static void refundSubLinearBatchFuelDiscount(@Nullable LivingEntity caster, int tier, int stackCount) {
        if (!(caster instanceof Player player) || stackCount <= 1) return;
        float discountFrac = Math.min(0.30F, 0.04F * (stackCount - 1));
        long refundMfum = Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * discountFrac);
        if (refundMfum > 0L) {
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.ARID, refundMfum);
        }
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        if (!level.getBlockState(start).getCollisionShape(level, start).isEmpty()) {
            for (int up = 1; up <= 4; up++) {
                BlockPos above = start.above(up);
                if (level.getBlockState(above).getCollisionShape(level, above).isEmpty()) {
                    return above.below();
                }
            }
            return start;
        }
        for (int down = 1; down <= 8; down++) {
            BlockPos below = start.below(down);
            if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
                return below;
            }
        }
        return start.below();
    }

    private static double probeOverheadClearance(ServerLevel level, BlockPos groundFloor, int maxProbe) {
        for (int dy = 4; dy <= maxProbe; dy++) {
            BlockPos check = groundFloor.above(dy);
            if (!level.getBlockState(check).getCollisionShape(level, check).isEmpty()) {
                return Math.max(3.6D, dy - 0.55D);
            }
        }
        return maxProbe;
    }

    private static double distanceToRaySegment(Vec3 point, Vec3 segStart, Vec3 normDir, double maxLen) {
        Vec3 rel = point.subtract(segStart);
        double proj = Mth.clamp(rel.dot(normDir), 0.0D, maxLen);
        return point.distanceTo(segStart.add(normDir.scale(proj)));
    }
}
