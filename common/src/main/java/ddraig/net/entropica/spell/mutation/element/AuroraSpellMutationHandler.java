package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
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
import net.minecraft.core.particles.ColorParticleOption;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Aurora Materia (Ethereal &amp; Paradox Fusion #35)</b>
 * (Spellcrafting System Guide Section 6.36 — Dual Radiant {@code #FFEA78} + Frozen {@code #98DCF2} [Leans: Radiant]).
 */
public final class AuroraSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Radiant Solar Gold (#FFEA78) primary tint. */
    public static final int BOREALIS_ARCHON_HEX_COLOR = 0xFFEA78;

    /** Canonical Crystalline Cryo Cyan (#98DCF2) secondary tint. */
    public static final int CRYO_PRISM_HEX_COLOR = 0x98DCF2;

    private static final String TAG_AURORA_BEAM_LAST_TICK = "SpellAuroraBeamLastTick";
    private static final String TAG_AURORA_BEAM_TICKS = "SpellAuroraBeamTicks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.AURORA;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Glacial Aurora Spiral Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Prismatic Aurora Ribbon-Lance";
            case BEAM -> "Borealis Prismatic Spectrum Ray";
            case RADIAL_BURST -> "Borealis Prismatic Curtain Nova";
            case TOUCH_STRIKE -> "Prismatic Aurora Polar Cleave";
            case AURA -> "Borealis Prismatic Veil Mantle";
            case GLYPH -> "Cryo-Refractive Mirror Trap";
            case CONE -> "Borealis Prismatic Fan Deluge";
            case SKYFALL -> "Cryo-Solar Comet Barrage";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Borealis Prism-Flash Step";
            case TRANSMUTE, BREAK -> "Polar Cryo-Refraction Transmute";
            case AFFLICTION -> "Sub-Zero Polar Stasis Hex";
            default -> "Prismatic Borealis " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return "Spiraling northern light helix of iridescent ribbons and cold vapor (#FFEA78 / #98DCF2) pulling dropped items toward the caster while chilling adjacent hostiles with Slowness II and extinguishing fire.";
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Hurls a piercing zero-drop ribbon of crystalline polar light (#FFEA78 / #98DCF2) through up to 3 targets, refracting into prismatic shards, freezing foes for 2.0s, and inflicting Glowing for 5s.";
            case BEAM -> "Channels a dancing iridescent ribbon of northern light and freezing air (#FFEA78 / #98DCF2), stacking Slowness up to IV and Glowing; 3.0s channeling triggers a 1.5s-stunning Prismatic Flashbang Shatter.";
            case RADIAL_BURST -> "Detonates a 6-block 360-degree curtain of dancing aurora ribbons and sub-zero hoarfrost (#FFEA78 / #98DCF2), knocking hostiles 6 blocks back, freezing for 2.0s, inflicting 4s Blindness/Glowing, and smiting undead (+50%).";
            case TOUCH_STRIKE -> "Sweeping crescent of sub-zero aurora light (#FFEA78 / #98DCF2) dealing Aurora Materia damage, freezing solid for 1.5s, smiting undead (+50%), inflicting 6s Glowing, and stripping 2 armor points.";
            case AURA -> "Envelops the caster in dancing polar ribbons (#FFEA78 / #98DCF2) granting Frost Walker, freeze/powder-snow immunity, Blindness/Darkness immunity, +4 Armor, Level-12 light, and melee Blindness + 2s Frostbite retaliation.";
            case GLYPH -> "Inscribes a disarmable mirrored polar glyph (#FFEA78 / #98DCF2) that deflects hostile projectiles back at shooters while rooting entering mobs in sub-zero ice crystals for 3s with continuous Aurora damage and Glowing.";
            case CONE -> "Sprays a 60-degree cone of sub-zero light ribbons and ice needles (#FFEA78 / #98DCF2), knocking hostiles 5 blocks back, freezing for 2.0s, inflicting 3s Glowing/Blindness, smiting undead (+50%), and coating ground in frosted ice.";
            case SKYFALL -> "Rains down 5 massive glowing ice meteors over 6 seconds (#FFEA78 / #98DCF2); each impact flashes light and frost, strips 2 armor points, deals heavy Aurora Materia damage, and coats the zone in blue ice.";
            case SUMMON_AGENT -> "Summons a Borealis-Archon biped (#FFEA78 / #98DCF2) draped in aurora light veils that hurls freezing ribbon-lances.";
            case SUMMON_SENTRY -> "Anchors a Prismatic Aurora Spire (#FFEA78 / #98DCF2) that grants allies Frost Walker and deflects hostile projectiles across 8 blocks.";
            case SUMMON_DECOY -> "Manifests a Cryo-Prism Effigy (#FFEA78 / #98DCF2) that taunts hostiles and detonates into a Borealis Curtain Nova upon destruction.";
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Teleports up to 14 blocks instantly in northern lights, leaving a 2.0s taunting frozen light hologram that shatters while granting Frost Walker and +30% sprint speed for 4s.";
            case TRANSMUTE, BREAK -> "Sub-zero light crystallization converting water directly into packed/blue ice without flow, lava into obsidian/crying obsidian, glass into tinted glass, and flowers into Auroral Buttercups.";
            case AFFLICTION -> "Persistent milk-immune polar chill curse cutting attack speed by 50%, movement speed by 40%, dealing continuous frostbite ticks, and inflicting Glowing until cleansed by Vitae/Restoration magic, Nether fire, or caster defeat.";
            default -> null;
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — GLACIAL AURORA SPIRAL FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), BOREALIS_ARCHON_HEX_COLOR, 14, 0.72F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
        );
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), CRYO_PRISM_HEX_COLOR, 16, 0.68F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
            );
        }
        if (flow == SpellFlowModifier.ORBIT_SATELLITE && projectile.getOwner() instanceof LivingEntity owner && owner.isAlive()) {
            owner.clearFire();
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, projectile.getBoundingBox().inflate(4.5D))) {
                Vec3 pull = owner.position().add(0.0D, 0.5D, 0.0D).subtract(item.position());
                if (pull.lengthSqr() > 0.25D) {
                    Vec3 step = pull.normalize().scale(0.18D);
                    item.setDeltaMovement(item.getDeltaMovement().scale(0.6D).add(step));
                    item.hasImpulse = true;
                }
            }
        }
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (ALL 9 FORMS)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.36 Prismatic Aurora Ribbon-Lance: straight-line zero-drop flight!
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
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
        // Section 6.36 Prismatic Aurora Ribbon-Lance:
        // Cuts through up to 3 targets in a straight line, refracting on impact into prismatic light shards,
        // dealing Aurora Materia damage, freezing targets in place for 2.0s (40t) with Frostbite, and Glowing for 5.0s (100t)!
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) return;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        if (caster != null && SpellExecutionEngine.isFriendlyAllyForOasis(caster, living)) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, wardSafe)) return;

        int tier = spell.getEffectiveTier();
        living.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
        living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
        living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 100, living.getTicksFrozen() + 90));
        living.removeEffect(MobEffects.INVISIBILITY);
        living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));

        if (!slot.hasHarmVerb()) {
            float lanceDmg = 2.70F + tier * 0.68F;
            SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(level, living, caster, lanceDmg, false, true);
        } else if (living.getType().is(EntityTypeTags.UNDEAD)) {
            float smiteBonus = 1.60F + tier * 0.45F;
            SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(level, living, caster, smiteBonus, false, true);
        }

        refractPrismaticLightShards(level, living.position().add(0.0D, living.getBbHeight() * 0.55D, 0.0D), caster, living, tier, wardSafe);
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
        refractPrismaticLightShards(
                level, blockHit.getLocation(), caster, null, spell.getEffectiveTier(), spell.hasWardFriendlyFireSafeguard()
        );
    }

    private static void refractPrismaticLightShards(
            ServerLevel level,
            Vec3 origin,
            @Nullable LivingEntity caster,
            @Nullable LivingEntity directHit,
            int tier,
            boolean wardSafe
    ) {
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, origin, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.AURORA, EssenceType.FROZEN, 1.35F, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), CRYO_PRISM_HEX_COLOR, 18, 0.85F, 0.08F),
                origin.x, origin.y, origin.z, 10, 0.35D, 0.35D, 0.35D, 0.06D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), BOREALIS_ARCHON_HEX_COLOR, 18, 0.85F, 0.08F),
                origin.x, origin.y, origin.z, 8, 0.35D, 0.35D, 0.35D, 0.06D
        );
        float shardDmg = 1.45F + tier * 0.38F;
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(origin, 3.6D, 3.0D, 3.6D), LivingEntity::isAlive)) {
            if (adj == caster || adj == directHit) continue;
            if (caster != null && SpellExecutionEngine.isFriendlyAllyForOasis(caster, adj)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, wardSafe)) continue;
            adj.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            adj.setTicksFrozen(Math.min(adj.getTicksRequiredToFreeze() + 40, adj.getTicksFrozen() + 35));
            SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(level, adj, caster, shardDmg, false, true);
        }
    }

    // =========================================================================
    // FORM MUTATION 2 — BOREALIS PRISMATIC SPECTRUM RAY (BEAM)
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
        // Section 6.36 Borealis Prismatic Spectrum Ray:
        // Continually stacks Slowness up to Slowness IV and inflicts Glowing; sustained channeling for 3.0 seconds
        // (60 ticks) triggers a Prismatic Flashbang Shatter that stuns caught mobs for 1.5s (30t) and deals bonus
        // frost-shatter Aurora Materia damage!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_AURORA_BEAM_LAST_TICK).orElse(-100L);
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_AURORA_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_AURORA_BEAM_LAST_TICK, now);
            data.putInt(TAG_AURORA_BEAM_TICKS, contactTicks);

            int slowAmp = Math.min(3, contactTicks / 15); // Stacks Slowness I -> II -> III -> IV!
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, slowAmp));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            hit.setTicksFrozen(Math.min(hit.getTicksRequiredToFreeze() + 100, hit.getTicksFrozen() + 28));

            if (!slot.hasHarmVerb()) {
                float rayDmg = (1.28F + tier * 0.35F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(level, hit, caster, rayDmg, false, true);
            }

            if (contactTicks >= 60) {
                data.putInt(TAG_AURORA_BEAM_TICKS, 0);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 6));
                hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));

                float shatterDmg = (4.80F + tier * 1.35F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(level, hit, caster, shatterDmg, false, true);

                SpellWorldVfxManager.broadcastRadialNova(
                        level, hit.position().add(0.0D, 0.2D, 0.0D), 2.8D, EssenceType.AURORA, EssenceType.FROZEN, tier
                );
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.00F, 0.92F, 0.47F),
                        hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                level.sendParticles(ParticleTypes.SNOWFLAKE, hit.getX(), hit.getY(0.9D), hit.getZ(), 24, 0.45D, 0.5D, 0.45D, 0.08D);
                level.playSound(null, hit.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.45F);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — BOREALIS PRISMATIC CURTAIN NOVA (RADIAL BURST)
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
        // Section 6.36 Borealis Prismatic Curtain Nova:
        // 360° shimmering curtain of dancing aurora ribbons & sub-zero hoarfrost (6m+ radius, #FFEA78 / #98DCF2);
        // knocks hostiles 6 blocks backward, freezes them in stasis for 2.0s (40t), inflicts Blindness & Glowing
        // for 4.0s (80t), and smites undead (+50% bonus damage)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.AURORA, EssenceType.FROZEN, tier);

        float smiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.5F, apparatusPotency);
        float baseNovaDmg = (3.05F + tier * 0.76F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 delta = foe.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.48D) : new Vec3(0.0D, 0.38D, 0.0D);
            foe.setDeltaMovement(away.x, 0.38D, away.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
            foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 120, foe.getTicksFrozen() + 100));
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));

            boolean undead = foe.getType().is(EntityTypeTags.UNDEAD);
            if (slot.hasHarmVerb()) {
                if (undead) {
                    SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(
                            level, foe, caster, Math.max(3.5F, smiteBonus), false, true
                    );
                }
            } else {
                SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(
                        level, foe, caster, baseNovaDmg * (undead ? 1.50F : 1.0F), false, true
                );
            }
        }

        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.9D, center.z, 36, 2.2D, 0.6D, 2.2D, 0.07D);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.9D, center.z, 30, 2.2D, 0.6D, 2.2D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.15F, 1.55F);
    }

    // =========================================================================
    // FORM MUTATION 4 — PRISMATIC AURORA POLAR CLEAVE (TOUCH / STRIKE)
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
        // Section 6.36 Prismatic Aurora Polar Cleave:
        // Sweeping horizontal crescent of shimmering sub-zero aurora light (#FFEA78 / #98DCF2); deals Aurora Materia
        // damage, freezes the struck target solid for 1.5s (30t), smites undead (+50%), inflicts Glowing for 6.0s (120t),
        // and shatters brittle armor (stripping 2 physical armor points)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), direction, 2.8D,
                EssenceType.AURORA, EssenceType.FROZEN, tier
        );

        float smiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.5F, apparatusPotency);
        float baseCleaveDmg = (2.85F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);
        long brittleUntil = level.getGameTime() + 120L;

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 6));
            hit.setTicksFrozen(Math.min(hit.getTicksRequiredToFreeze() + 100, hit.getTicksFrozen() + 90));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 120L);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_AURORA_BRITTLE_UNTIL, brittleUntil);

            boolean undead = hit.getType().is(EntityTypeTags.UNDEAD);
            if (slot.hasHarmVerb()) {
                if (undead) {
                    SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(
                            level, hit, caster, Math.max(3.5F, smiteBonus), true, true
                    );
                }
            } else {
                SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(
                        level, hit, caster, baseCleaveDmg * (undead ? 1.50F : 1.0F), true, true
                );
            }
            level.sendParticles(ParticleTypes.SNOWFLAKE, hit.getX(), hit.getY(0.85D), hit.getZ(), 16, 0.3D, 0.4D, 0.3D, 0.05D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.85F, 1.45F);
    }

    // =========================================================================
    // FORM MUTATION 5 — BOREALIS PRISMATIC VEIL MANTLE (AURA / SELF)
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
        // Section 6.36 Borealis Prismatic Veil Mantle:
        // Envelops the caster in dancing iridescent polar ribbons and protective hoarfrost (#FFEA78 / #98DCF2);
        // grants Frost Walker, complete freezing & powder snow immunity, Blindness & Darkness immunity, +4 Armor,
        // Level-12 illumination, and retaliates against melee attackers with Blindness & 2s Frostbite!
        int dur = 200 + spell.getEffectiveTier() * 40;
        long until = level.getGameTime() + dur;
        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_AURORA_MANTLE_UNTIL, until);
        data.putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, until);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.0D, 0.0D, dur);
        caster.setTicksFrozen(0);
        caster.setIsInPowderSnow(false);
        caster.clearFire();
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.50F, null);
        level.sendParticles(ParticleTypes.SNOWFLAKE, caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.45D, 0.55D, 0.45D, 0.04D);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 16, 0.45D, 0.55D, 0.45D, 0.04D);
    }

    // =========================================================================
    // FORM MUTATION 6 — CRYO-REFRACTIVE MIRROR TRAP (GLYPH / TRAP)
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
        // Section 6.36 Cryo-Refractive Mirror Trap:
        // Disarmable Single/Repeating mirrored polar glyph (#FFEA78 / #98DCF2, 1.0s arming delay); deflects enemy
        // projectiles back at the shooter while rooting entering mobs in jagged sub-zero ice crystals for 3.0s (60t),
        // dealing continuous Aurora Materia damage, and inflicting Glowing!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.2D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.AURORA, EssenceType.FROZEN, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.AURORA, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.AURORA, EssenceType.FROZEN, rem, tier
                        );
                    }
                    if (elapsed % 12 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                zone.center.x, zone.center.y + 0.15D, zone.center.z, 4, 0.9D, 0.1D, 0.9D, 0.02D);
                    }

                    // Active projectile mirror deflection back toward the shooter!
                    deflectHostileProjectilesAtMirror(srv, zone.center, zone.radius, owner);

                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - 2.6D, zone.center.y - 1.0D, zone.center.z - 2.6D,
                            zone.center.x + 2.6D, zone.center.y + 2.2D, zone.center.z + 2.6D
                    );
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive() && e != owner
                                    && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    if (triggers.isEmpty()) return true;

                    SpellWorldVfxManager.broadcastRadialNova(
                            srv, zone.center, zone.radius, EssenceType.AURORA, EssenceType.FROZEN, tier
                    );

                    // 3.0s (60t) sub-zero ice crystal root + continuous Aurora Materia damage + projectile mirror!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, zone.radius, 60, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                deflectHostileProjectilesAtMirror(sLvl, sZone.center, sZone.radius, sOwner);

                                boolean sWard = sZone.spell != null && sZone.spell.hasWardFriendlyFireSafeguard();
                                int zTier = sZone.spell != null ? sZone.spell.getEffectiveTier() : tier;
                                float tickDmg = (1.35F + zTier * 0.36F) * Math.max(0.5F, sZone.apparatusPotency);
                                AABB box = new AABB(
                                        sZone.center.x - sZone.radius, sZone.center.y - 1.0D, sZone.center.z - sZone.radius,
                                        sZone.center.x + sZone.radius, sZone.center.y + 2.5D, sZone.center.z + sZone.radius
                                );
                                long now = sLvl.getGameTime();
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                                    if (v == sOwner || SpellExecutionEngine.isFriendlyAllyForOasis(sOwner, v)) continue;
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sWard)) continue;

                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 6));
                                    v.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                                    v.setTicksFrozen(Math.min(v.getTicksRequiredToFreeze() + 100, v.getTicksFrozen() + 35));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 25L);

                                    if (sEl % 20 == 0) {
                                        if (sZone.spell != null && sZone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    sLvl, sOwner, v, Vec3.ZERO, sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.50F
                                            );
                                        }
                                        SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(sLvl, v, sOwner, tickDmg, false, true);
                                    }
                                }
                                return true;
                            }
                    ));

                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.95F, 1.40F);
                    triggersLeft[0]--;
                    rearmCooldown[0] = 80;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    private static void deflectHostileProjectilesAtMirror(
            ServerLevel level,
            Vec3 center,
            double radius,
            @Nullable LivingEntity owner
    ) {
        AABB projBox = new AABB(
                center.x - radius, center.y - 0.5D, center.z - radius,
                center.x + radius, center.y + 3.0D, center.z + radius
        );
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, projBox)) {
            Entity shooter = proj.getOwner();
            if (owner != null && (shooter == owner || (shooter instanceof LivingEntity l && SpellExecutionEngine.isFriendlyAllyForOasis(owner, l)))) {
                continue;
            }
            Vec3 rev;
            if (shooter != null && shooter.isAlive()) {
                Vec3 toShooter = shooter.getEyePosition().subtract(proj.position());
                rev = toShooter.lengthSqr() > 1.0E-4D
                        ? toShooter.normalize().scale(Math.max(1.25D, proj.getDeltaMovement().length()))
                        : proj.getDeltaMovement().scale(-1.05D);
            } else {
                rev = proj.getDeltaMovement().scale(-1.05D);
            }
            proj.setDeltaMovement(rev);
            if (owner != null) {
                proj.setOwner(owner);
            }
            proj.hasImpulse = true;
            level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 6, 0.15D, 0.15D, 0.15D, 0.04D);
        }
    }

    // =========================================================================
    // FORM MUTATION 7 — BOREALIS PRISMATIC FAN DELUGE (CONE / SPRAY)
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
        // Section 6.36 Borealis Prismatic Fan Deluge:
        // Wide 60° cone of sparkling sub-zero light ribbons & crystalline ice needles (#FFEA78 / #98DCF2);
        // knocks hostiles 5 blocks backward, freezes them for 2.0s (40t), inflicts Glowing & Blindness for 3.0s (60t),
        // smites undead (+50%), and covers the ground in frosted ice!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), direction, coneRange,
                EssenceType.AURORA, EssenceType.FROZEN, tier
        );

        float smiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.5F, apparatusPotency);
        float coneDmg = (2.80F + tier * 0.70F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(direction.x * 1.26D, 0.32D, direction.z * 1.26D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
            hit.setTicksFrozen(Math.min(hit.getTicksRequiredToFreeze() + 100, hit.getTicksFrozen() + 90));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));

            boolean undead = hit.getType().is(EntityTypeTags.UNDEAD);
            if (slot.hasHarmVerb()) {
                if (undead) {
                    SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(
                            level, hit, caster, Math.max(3.5F, smiteBonus), false, true
                    );
                }
            } else {
                SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(
                        level, hit, caster, coneDmg * (undead ? 1.50F : 1.0F), false, true
                );
            }
        }

        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        BlockPos cPos = BlockPos.containing(mid);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-3, -2, -3), cPos.offset(3, 1, 3))) {
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(Blocks.WATER) && level.getBlockState(p.above()).isAir()) {
                level.setBlockAndUpdate(p, Blocks.FROSTED_ICE.defaultBlockState());
            } else if (bs.isAir() && level.getBlockState(p.below()).isSolidRender()
                    && Blocks.SNOW.defaultBlockState().canSurvive(level, p) && level.random.nextFloat() < 0.50F) {
                level.setBlockAndUpdate(p, Blocks.SNOW.defaultBlockState());
            }
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, mid.x, mid.y, mid.z, 26, 1.5D, 0.5D, 1.5D, 0.05D);
        level.sendParticles(ParticleTypes.END_ROD, mid.x, mid.y, mid.z, 20, 1.5D, 0.5D, 1.5D, 0.05D);
    }

    // =========================================================================
    // FORM MUTATION 8 — CRYO-SOLAR COMET BARRAGE (SKYFALL)
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
        // Section 6.36 Cryo-Solar Comet Barrage:
        // Rains down 5 massive glowing ice meteors over 6.0 seconds (120 ticks, #FFEA78 / #98DCF2); each impact
        // detonates in a flash of light and frost, shatters enemy armor (stripping 2 armor points), deals heavy
        // Aurora Materia damage, and coats the impact zone in blue ice!
        int tier = spell.getEffectiveTier();
        double r = Math.max(8.0D, radius);
        BlockPos centerPos = BlockPos.containing(impactCenter);
        double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.AURORA, EssenceType.FROZEN, 140, tier
        );

        // Pre-plan 5 comet strike coordinates across the 8m+ target zone (1st dead-center, 4 around the ring)
        final BlockPos[] cometTargets = new BlockPos[5];
        cometTargets[0] = findGroundSurfaceNear(level, centerPos).above();
        for (int i = 1; i < 5; i++) {
            double ang = (Math.PI * 0.5D * (i - 1)) + level.random.nextDouble() * 0.75D;
            double dist = 0.30D * r + level.random.nextDouble() * (0.48D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            cometTargets[i] = findGroundSurfaceNear(level, raw).above();
        }

        final boolean[] landed = new boolean[5];
        final boolean[] materialized = {false};
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 150, level.getGameTime(), spell, slot, EssenceType.AURORA, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int cIdx = 0; cIdx < 5; cIdx++) {
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(
                                        srv, cometTargets[cIdx], Blocks.BLUE_ICE.defaultBlockState(), 2
                                );
                            }
                        }
                        return false;
                    }

                    // Launch each 3D falling BLUE_ICE meteor pseudoblock 16 ticks before touchdown (impacts at 20, 44, 68, 92, 116)!
                    for (int cIdx = 0; cIdx < 5; cIdx++) {
                        int impactTick = 20 + cIdx * 24;
                        BlockPos strikePos = cometTargets[cIdx];
                        if (elapsed == impactTick - 16) {
                            int holdTicks = Math.max(4, zone.maxDurationTicks - impactTick);
                            SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                                    srv,
                                    new Vec3(strikePos.getX() + 0.5D, strikePos.getY() + skyH, strikePos.getZ() + 0.5D),
                                    strikePos,
                                    Blocks.BLUE_ICE.defaultBlockState(),
                                    2,
                                    16,
                                    holdTicks,
                                    EssenceType.AURORA,
                                    tier
                            );
                        } else if (elapsed == impactTick) {
                            landed[cIdx] = true;
                            Vec3 cometHit = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                            LivingEntity owner = zone.resolveCaster(srv);
                            boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                            int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                            float cometDmg = (4.10F + zTier * 0.98F) * Math.max(0.5F, zone.apparatusPotency);

                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, cometHit.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.AURORA, EssenceType.FROZEN, 2.25F, zTier
                            );

                            AABB hitBox = new AABB(
                                    cometHit.x - 4.5D, cometHit.y - 2.0D, cometHit.z - 4.5D,
                                    cometHit.x + 4.5D, cometHit.y + 4.0D, cometHit.z + 4.5D
                            );
                            for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, hitBox, LivingEntity::isAlive)) {
                                if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 2.0D, 0.0D, 120L);
                                foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 100, foe.getTicksFrozen() + 75));
                                foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));

                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.65F
                                    );
                                }
                                boolean undead = foe.getType().is(EntityTypeTags.UNDEAD);
                                SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(
                                        srv, foe, owner, cometDmg * (undead ? 1.50F : 1.0F), false, true
                                );
                            }

                            // Freeze surface water & dust surrounding ground in snow around the comet impact!
                            for (BlockPos p : BlockPos.betweenClosed(strikePos.offset(-2, -1, -2), strikePos.offset(2, 0, 2))) {
                                if (p.distSqr(strikePos) > 5.0D || p.equals(strikePos)) continue;
                                BlockState cur = srv.getBlockState(p);
                                if (SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, cur)) continue;
                                if (cur.is(Blocks.WATER)) {
                                    srv.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
                                } else if (p.getY() == strikePos.getY() && cur.isAir()
                                        && srv.getBlockState(p.below()).isSolidRender()
                                        && Blocks.SNOW.defaultBlockState().canSurvive(srv, p)) {
                                    srv.setBlockAndUpdate(p, Blocks.SNOW.defaultBlockState());
                                }
                            }

                            srv.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.00F, 0.92F, 0.47F),
                                    cometHit.x, cometHit.y + 0.8D, cometHit.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                            srv.sendParticles(ParticleTypes.SNOWFLAKE, cometHit.x, cometHit.y + 0.8D, cometHit.z, 26, 1.4D, 0.5D, 1.4D, 0.08D);
                            srv.sendParticles(ParticleTypes.END_ROD, cometHit.x, cometHit.y + 0.8D, cometHit.z, 20, 1.2D, 0.5D, 1.2D, 0.08D);
                            srv.playSound(null, strikePos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.35F);
                        }
                        if (landed[cIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, strikePos, 2, false);
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — BOREALIS ARCHON-CONSTRUCT SUITE (SUMMON / EIDOLON)
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
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.AURORA.name());
        tag.putInt("SpellEidolonTintRGB", BOREALIS_ARCHON_HEX_COLOR);
        level.sendParticles(ParticleTypes.SNOWFLAKE, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 22, 0.45D, 0.55D, 0.45D, 0.05D);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 18, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.36 Borealis Prism-Flash Step:
        // Teleports up to 14 blocks instantly in northern lights; leaves behind a shimmering frozen light hologram
        // that taunts enemies for 2.0s (40t) before shattering, granting Frost Walker & +30% sprint speed for 4.0s (80t)!
        SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.AURORA,
                10.0F, 40, "Borealis Frozen Hologram", null
        );
        EntityHelper.getPersistentData(caster).putLong(
                SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, level.getGameTime() + 80L
        );
        SpellCombatReactionEngine.applyTimedMovementSpeedBoost(caster, level, 0.30D, 80L);
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 1, false, true));
        level.sendParticles(ParticleTypes.SNOWFLAKE, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 16, 0.35D, 0.45D, 0.35D, 0.04D);
        level.sendParticles(ParticleTypes.END_ROD, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 14, 0.35D, 0.45D, 0.35D, 0.05D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.36 Polar Cryo-Refraction Transmute:
        // Converts water blocks directly into packed ice or blue ice WITHOUT flow, turns lava into obsidian or
        // crying obsidian, transmutes glass/stained glass into tinted crystal glass, & flowers into Auroral Buttercup!
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        if (state.is(Blocks.WATER)) {
            BlockState iceState = (potencyTier >= 5 || level.random.nextFloat() < 0.30F)
                    ? Blocks.BLUE_ICE.defaultBlockState()
                    : Blocks.PACKED_ICE.defaultBlockState();
            level.setBlock(pos, iceState, Block.UPDATE_CLIENTS);
            maybeGrantAuroraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE) || state.is(Blocks.PACKED_ICE)) {
            level.setBlock(pos, Blocks.BLUE_ICE.defaultBlockState(), Block.UPDATE_CLIENTS);
            maybeGrantAuroraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.LAVA)) {
            BlockState obsState = level.random.nextBoolean()
                    ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                    : Blocks.OBSIDIAN.defaultBlockState();
            level.setBlock(pos, obsState, Block.UPDATE_CLIENTS);
            maybeGrantAuroraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        String key = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (state.is(Blocks.GLASS) || (key.endsWith("_stained_glass") && !state.is(Blocks.TINTED_GLASS))) {
            level.setBlockAndUpdate(pos, Blocks.TINTED_GLASS.defaultBlockState());
            maybeGrantAuroraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.SMALL_FLOWERS) && !state.is(ModBlocks.AURORAL_BUTTERCUP.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AURORAL_BUTTERCUP.get().defaultBlockState());
            maybeGrantAuroraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.isEmpty()) return false;
        String key = BuiltInRegistries.ITEM.getKey(in.getItem()).getPath();
        if (in.is(Items.ICE) || in.is(Items.PACKED_ICE)) {
            itemEntity.setItem(new ItemStack(Items.BLUE_ICE, in.getCount()));
            maybeGrantAuroraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (in.is(Items.WATER_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.BLUE_ICE, in.getCount()));
            maybeGrantAuroraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (in.is(Items.LAVA_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.CRYING_OBSIDIAN, in.getCount()));
            maybeGrantAuroraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (in.is(Items.GLASS) || key.endsWith("_stained_glass")) {
            itemEntity.setItem(new ItemStack(Items.TINTED_GLASS, in.getCount()));
            maybeGrantAuroraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (in.is(ItemTags.SMALL_FLOWERS) && !in.is(ModBlocks.AURORAL_BUTTERCUP.get().asItem())) {
            itemEntity.setItem(new ItemStack(ModBlocks.AURORAL_BUTTERCUP.get().asItem(), in.getCount()));
            maybeGrantAuroraBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.36 Sub-Zero Polar Stasis Hex:
        // Persistent milk-immune frost curse (cleansed via Restoration/Vitae magic, Nether fire, or caster defeat):
        // permanently cuts attack speed by 50%, slashes movement speed by 40%, deals continuous freezing frostbite
        // ticks, and inflicts Glowing!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.50D * mult), 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.40D, 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, Math.min(4, Math.max(1, Math.round(1.5F * mult))), true, true));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, true, true));
        target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 120, target.getTicksFrozen() + Math.round(55 * mult)));

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.40F + potencyTier * 0.45F) * mult;
        SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(level, target, hexCaster, hexDmg, false, true);

        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.85D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.03D);
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
            target.setTicksFrozen(0);
            float heal = 3.2F + potencyTier * 0.95F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, heal);
            target.heal(heal);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static void maybeGrantAuroraBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellAuroraBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellAuroraBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellAuroraBatchTick", now);
        pData.putInt("SpellAuroraBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.AURORA, refund);
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
