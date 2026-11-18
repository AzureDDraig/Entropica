package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Oasis Materia (Tier 1 Fusion #20 — Nature + Arid)</b>
 * (Spellcrafting System Guide Section 6.21):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Dual Nature + Arid harmonic matrix — Strong vs <b>WATER (+1)</b>, <b>EARTH (+1)</b>,
 *     &amp; <b>DUST (+1)</b> (Dominance {@code oasis_arid_dom}: Strong vs Water &amp; Void) | Weak vs <b>NETHER (-1)</b> &amp; <b>UMBRAL (-1)</b>.</li>
 *     <li><b>Flow (Split)</b>: <i>Succulent Bloom Split</i> — splits into 3 smaller succulent seed blossoms upon impact or reaching
 *     maximum range, expanding area coverage and scattering healing golden pollen.</li>
 *     <li><b>Projectile</b>: <i>Miracle Desert Bloom Seed</i> — vibrant succulent seed ({@code #1A5923} / {@code #C2B28F}) dealing
 *     Oasis Materia damage, siphoning exact 20% of damage dealt as healing to the caster, and converting struck sand into fertile
 *     grass or moist farmland while blooming desert flowers.</li>
 *     <li><b>Beam</b>: <i>Subterranean Aquifer Stream</i> — sustained jet of nutrient-rich desert springwater &amp; floral pollen dealing
 *     Oasis Materia damage, healing the caster for exact 15% of damage dealt, hydrating dry soil/farmland, and accelerating crops,
 *     cactus, and sugarcane.</li>
 *     <li><b>Radial Burst</b>: <i>Miracle Oasis Bloom Nova</i> — 6m+ 360-degree surge of desert springwater &amp; flourishing flora dealing
 *     Oasis Materia damage to hostiles, knocking enemies back 4 blocks, healing caster &amp; allies, extinguishing fire, hydrating farmland,
 *     and blooming grass and flowers across sand.</li>
 *     <li><b>Touch / Strike</b>: <i>Desert Blessing Touch</i> — versatile touch strike dealing Oasis Materia damage and pacifying hostile
 *     mobs for 2.5s, or when targeting allies, restoring a chunk of health and clearing negative status effects &amp; hexes.</li>
 *     <li><b>Aura / Self</b>: <i>Succulent Thorn Carapace</i> — encases the caster in thick cactus flesh and sharp spines (-20% incoming
 *     physical damage and Oasis Materia thorn retaliation against melee attackers, mobile or sneak-anchored).</li>
 *     <li><b>Glyph / Trap</b>: <i>Aquifer Geyser Mine</i> — disarmable Single/Repeating desert spring rune launching a pressurized burst
 *     of groundwater when triggered, blasting victims 8 blocks into the air with Oasis Materia damage and hydrating nearby farmland.</li>
 *     <li><b>Cone / Spray</b>: <i>Desert Greening Mist</i> — gentle warm botanical mist dealing mild Oasis Materia damage, pacifying caught
 *     mobs for 4.0s, hydrating dry soil, and accelerating all crops, sugarcane, and cactuses in the cone.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Miracle Desert Downpour</i> — tier-scaled 16m+ restorative rainstorm of sparkling springwater &amp;
 *     golden pollen for 12.0s dealing Oasis Materia damage to hostiles, healing players &amp; pets, hydrating farmland, accelerating
 *     crop/cactus/sugarcane growth, and steadily converting sand into fertile grass &amp; flowers.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Oasis Guardian Eidolon Suite</i> — dual-tone {@code #1A5923} / {@code #C2B28F} Oasis-Warden Agent
 *     (20% ally healing siphon), Aquifer Spring Spire Sentry (area heal + hydration/growth + springwater bolts), and Mirage Blossom Effigy
 *     Decoy (14m taunt + 5m pacifying water/pollen burst).</li>
 *     <li><b>Blink / Step</b>: <i>Mirage-Spring Blink</i> — 14m+ desert heat-mirage teleport arriving in a soothing splash of springwater
 *     and leaving a blooming flower decoy at departure.</li>
 *     <li><b>Transmute</b>: <i>Desert Greening Transmute</i> — converts sand &amp; red sand into fertile grass or farmland, dead bushes
 *     into flowers, and coarse dirt into podzol.</li>
 *     <li><b>Affliction</b>: <i>Dehydrating Sunstroke Hex</i> — milk-immune parching mirage curse draining hunger/saturation, cutting
 *     attack speed by 35%, and dealing Oasis Materia damage whenever the victim attempts to sprint or jump until cleansed by Water/Restoration.</li>
 *     <li><b>Restoration</b>: <i>Miracle Spring Mending Restoration</i> — cleanses negative status effects &amp; hexes, heals HP, and refills hunger &amp; saturation.</li>
 * </ul>
 */
public final class OasisSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int OASIS_GUARDIAN_HEX_COLOR = 0x1A5923;
    public static final int LUSH_OASIS_GREEN_HEX_COLOR = 0x1A5923;
    public static final int SUNLIT_DESERT_GOLD_HEX_COLOR = 0xC2B28F;
    public static final int AQUIFER_SPRINGWATER_HEX_COLOR = 0x38C6F4;

    private static final Block[] OASIS_DESERT_FLOWERS = {
            Blocks.POPPY,
            Blocks.DANDELION,
            Blocks.ORANGE_TULIP,
            Blocks.AZURE_BLUET,
            Blocks.OXEYE_DAISY
    };

    @Override
    public EssenceType materiaType() {
        return EssenceType.OASIS;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case DOUBLE_BIFURCATE, TRIPLE_TRIFURCATE, OCTUPLE_DOME -> "Succulent Bloom Split";
            default -> "Succulent Bloom Split (" + flow.getDisplayName() + ")";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Miracle Desert Bloom Seed";
            case BEAM -> "Subterranean Aquifer Stream";
            case RADIAL_BURST -> "Miracle Oasis Bloom Nova";
            case TOUCH_STRIKE -> "Desert Blessing Touch";
            case AURA -> "Succulent Thorn Carapace";
            case GLYPH -> "Aquifer Geyser Mine";
            case CONE -> "Desert Greening Mist";
            case SKYFALL -> "Miracle Desert Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Mirage-Spring Blink";
            case TRANSMUTE, BREAK -> "Desert Greening Transmute";
            case AFFLICTION -> "Dehydrating Sunstroke Hex";
            case RESTORATION -> "Miracle Spring Mending Restoration";
            default -> "Miracle Oasis " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // FLOW MUTATION — SUCCULENT BLOOM SPLIT
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), SUNLIT_DESERT_GOLD_HEX_COLOR, 18, 0.85F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.07D, 0.07D, 0.07D, 0.01D
            );
        }
    }

    @Override
    public void onProjectileImpactOrExpireFromFlow(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Vec3 splitOrigin,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.21 Succulent Bloom Split: splits into 3 smaller succulent seed blossoms on impact or max range,
        // expanding area coverage and scattering a 3.5m cloud of healing golden pollen!
        projectile.spawnSplitChild(level, splitOrigin, -24.0F, false, 0.62F, 1.35F);
        projectile.spawnSplitChild(level, splitOrigin, 0.0F, false, 0.62F, 1.35F);
        projectile.spawnSplitChild(level, splitOrigin, 24.0F, false, 0.62F, 1.35F);

        int tier = spell.getEffectiveTier();
        float pollenHeal = 1.6F + tier * 0.35F;
        AABB pollenBox = new AABB(
                splitOrigin.x - 3.5D, splitOrigin.y - 1.8D, splitOrigin.z - 3.5D,
                splitOrigin.x + 3.5D, splitOrigin.y + 2.2D, splitOrigin.z + 3.5D
        );
        for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, pollenBox, LivingEntity::isAlive)) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, ally)) {
                ally.clearFire();
                SpellCombatReactionEngine.notifyMagicalHeal(ally, pollenHeal);
                ally.heal(pollenHeal);
            }
        }

        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                splitOrigin.x, splitOrigin.y, splitOrigin.z, 14, 0.42D, 0.35D, 0.42D, 0.04D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), LUSH_OASIS_GREEN_HEX_COLOR, 24, 1.00F, 0.06F),
                splitOrigin.x, splitOrigin.y, splitOrigin.z, 10, 0.28D, 0.25D, 0.28D, 0.04D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), SUNLIT_DESERT_GOLD_HEX_COLOR, 24, 1.00F, 0.06F),
                splitOrigin.x, splitOrigin.y, splitOrigin.z, 12, 0.30D, 0.26D, 0.30D, 0.04D
        );
        level.playSound(null, BlockPos.containing(splitOrigin), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.80F, 1.25F);
    }

    // =========================================================================
    // FORM MUTATION 1 — MIRACLE DESERT BLOOM SEED (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        Vec3 vel = projectile.getDeltaMovement();
        if (vel.lengthSqr() > 1.0E-4D && vel.length() < 1.72D) {
            projectile.setDeltaMovement(vel.normalize().scale(1.75D));
            projectile.hasImpulse = true;
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.75F, 1.30F);
        level.playSound(null, caster.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.45F, 1.48F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SPLASH,
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.07D, 0.07D, 0.07D, 0.01D);
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), LUSH_OASIS_GREEN_HEX_COLOR, 20, 0.88F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), SUNLIT_DESERT_GOLD_HEX_COLOR, 20, 0.88F, 0.05F),
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
        // Section 6.21 Miracle Desert Bloom Seed:
        // Deals Oasis Materia damage, siphons exact 20% of damage dealt as healing to the caster,
        // and converts struck sand into fertile grass or moist farmland while blooming desert flowers!
        BlockPos impactPos = BlockPos.containing(projectile.position());
        hydrateAndGrowFlora(level, impactPos, 2);
        bloomOasisPatch(level, impactPos, 2, true);

        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) return;
        if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, living)) {
            living.clearFire();
            return;
        }
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        // Note: When slot.hasHarmVerb() is true, SpellCombatReactionEngine.executeHarmVerb already heals caster
        // for exact 20% of actual damage dealt via oasisProjectileLifesteal — only deal fallback damage + 20% heal when !hasHarmVerb()!
        if (!slot.hasHarmVerb()) {
            int tier = spell.getEffectiveTier();
            float seedDmg = (2.8F + tier * 0.85F) * spell.flawProfile().getPotencyMultiplier();
            float dealt = SpellCombatReactionEngine.hurtWithOasisMateriaAdvantage(level, living, caster, seedDmg, false, true);
            if (dealt > 0.0F && caster != null && caster.isAlive()) {
                float siphonHeal = dealt * 0.20F;
                SpellCombatReactionEngine.notifyMagicalHeal(caster, siphonHeal);
                caster.heal(siphonHeal);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        caster.getX(), caster.getY(0.85D), caster.getZ(), 4, 0.25D, 0.30D, 0.25D, 0.02D);
            }
        }
        SpellLingeringZoneManager.applySoaked(living, 80);
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
        hydrateAndGrowFlora(level, hitPos, 2);
        bloomOasisPatch(level, hitPos, 2, true);
        level.sendParticles(ParticleTypes.SPLASH,
                hitPos.getX() + 0.5D, hitPos.getY() + 1.05D, hitPos.getZ() + 0.5D, 14, 0.45D, 0.25D, 0.45D, 0.06D);
    }

    // =========================================================================
    // FORM MUTATION 2 — SUBTERRANEAN AQUIFER STREAM (BEAM)
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
        // Section 6.21 Subterranean Aquifer Stream:
        // Sustained jet of nutrient-rich desert springwater & floral pollen dealing Oasis Materia damage,
        // healing the caster for exact 15% of damage dealt, hydrating dry soil/farmland, and accelerating crops, cactus & sugarcane!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) {
                hit.clearFire();
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            SpellLingeringZoneManager.applySoaked(hit, 80);
            if (!slot.hasHarmVerb()) {
                float beamDmg = (1.20F + tier * 0.34F) * Math.max(0.5F, apparatusPotency);
                float dealt = SpellCombatReactionEngine.hurtWithOasisMateriaAdvantage(level, hit, caster, beamDmg, false, true);
                if (dealt > 0.0F && caster.isAlive()) {
                    float siphonHeal = dealt * 0.15F;
                    SpellCombatReactionEngine.notifyMagicalHeal(caster, siphonHeal);
                    caster.heal(siphonHeal);
                }
            }
        }

        double actualReach = blockHit != null ? caster.getEyePosition().distanceTo(blockHit.getLocation()) : maxReach;
        int steps = Math.clamp((int) Math.ceil(actualReach), 1, 24);
        Vec3 eye = caster.getEyePosition();
        for (int s = 1; s <= steps; s++) {
            BlockPos bp = BlockPos.containing(eye.add(direction.scale(s)));
            BlockPos surf = findGroundSurfaceNear(level, bp);
            hydrateAndGrowFlora(level, surf, 1);
            if ((level.getBlockState(surf).is(Blocks.SAND) || level.getBlockState(surf).is(Blocks.RED_SAND))
                    && level.random.nextFloat() < 0.28F) {
                bloomOasisPatch(level, surf, 1, false);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — MIRACLE OASIS BLOOM NOVA (RADIAL BURST)
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
        // Section 6.21 Miracle Oasis Bloom Nova:
        // Explosive 360-degree surge of desert springwater and flourishing flora (6-block+ radius) dealing Oasis Materia damage
        // to hostiles, knocking enemies back 4 blocks, healing caster & allies, extinguishing fire, hydrating farmland,
        // and blooming grass and flowers across sand!
        double r = Math.max(6.0D, radius);
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);

        BlockPos cPos = BlockPos.containing(center);
        int br = Math.min(8, (int) Math.ceil(r));
        hydrateAndGrowFlora(level, cPos, br);
        bloomOasisPatch(level, cPos, br, false);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double dx = e.getX() - center.x;
            double dz = e.getZ() - center.z;
            if (dx * dx + dz * dz > (r + 0.6D) * (r + 0.6D)) continue;

            e.clearFire();
            EntityHelper.getPersistentData(e).remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);

            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, e)) {
                float heal = (4.5F + tier * 1.25F) * potency;
                SpellCombatReactionEngine.notifyMagicalHeal(e, heal);
                e.heal(heal);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        e.getX(), e.getY(0.8D), e.getZ(), 8, 0.35D, 0.40D, 0.35D, 0.03D);
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, wardSafe)) {
                if (!slot.hasHarmVerb()) {
                    float novaDmg = (3.3F + tier * 0.90F) * potency;
                    SpellCombatReactionEngine.hurtWithOasisMateriaAdvantage(level, e, caster, novaDmg, false, true);
                }
                SpellLingeringZoneManager.applySoaked(e, 100);
                if (!SpellCombatReactionEngine.isBossEntity(level, e)) {
                    // Exact 4-block horizontal knockback wave!
                    Vec3 away = new Vec3(dx, 0.0D, dz);
                    Vec3 norm = away.lengthSqr() > 1.0E-4D ? away.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                    SpellExecutionEngine.applySyncedMotion(e, new Vec3(norm.x * 1.12D, 0.32D, norm.z * 1.12D));
                }
            }
        }

        level.sendParticles(ParticleTypes.SPLASH,
                center.x, center.y + 0.8D, center.z, 38, r * 0.45D, 0.60D, r * 0.45D, 0.08D);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                center.x, center.y + 1.0D, center.z, 24, r * 0.45D, 0.65D, r * 0.45D, 0.05D);
        level.playSound(null, cPos, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 0.95F);
        level.playSound(null, cPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.28F);
    }

    // =========================================================================
    // FORM MUTATION 4 — DESERT BLESSING TOUCH (TOUCH / STRIKE)
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
        // Section 6.21 Desert Blessing Touch:
        // Versatile touch strike that deals Oasis Materia damage and pacifies hostile mobs for 2.5 seconds (50t),
        // OR when targeting allies, restores a chunk of health and clears negative status effects & hexes!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.position().add(0.0D, caster.getBbHeight() * 0.58D, 0.0D),
                direction, 3.4D, EssenceType.OASIS, EssenceType.WATER, tier
        );

        for (int i = 0; i < hitEntities.size(); i++) {
            LivingEntity hit = hitEntities.get(i);
            if (!hit.isAlive()) continue;
            float cleaveScale = (i == 0) ? 1.0F : 0.55F;

            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) {
                hit.clearFire();
                EntityHelper.getPersistentData(hit).remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                float heal = (6.0F + tier * 1.80F) * cleaveScale * potency;
                SpellCombatReactionEngine.notifyMagicalHeal(hit, heal);
                hit.heal(heal);

                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        hit.getX(), hit.getY(0.85D), hit.getZ(), 12, 0.35D, 0.45D, 0.35D, 0.04D);
                level.sendParticles(ParticleTypes.SPLASH,
                        hit.getX(), hit.getY(0.75D), hit.getZ(), 10, 0.35D, 0.40D, 0.35D, 0.04D);
                level.playSound(null, hit.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.35F);
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) {
                if (!slot.hasHarmVerb()) {
                    float touchDmg = (3.4F + tier * 1.00F) * cleaveScale * potency;
                    SpellCombatReactionEngine.hurtWithOasisMateriaAdvantage(level, hit, caster, touchDmg, true, true);
                }
                // Pacify hostile mob for exact 2.5 seconds (50 ticks)!
                SpellLingeringZoneManager.applyPacified(hit, 50);
                DustSpellMutationHandler.wipeMobAggroPublic(level, hit, 50L);
                hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 1));
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                        hit.getX(), hit.getY(0.85D), hit.getZ(), 10, 0.32D, 0.40D, 0.32D, 0.03D);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — SUCCULENT THORN CARAPACE (AURA / SELF)
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
        // Section 6.21 Succulent Thorn Carapace:
        // Encases the caster in thick cactus flesh and sharp spines (-20% incoming physical damage + Oasis Materia thorn retaliation)!
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 40;
        long until = level.getGameTime() + dur;

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_OASIS_CARAPACE_UNTIL, until);
        data.putInt("SpellOasisCarapaceTier", tier);
        data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
        caster.clearFire();

        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.0F, null);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), LUSH_OASIS_GREEN_HEX_COLOR, 28, 1.05F, 0.06F),
                caster.getX(), caster.getY(0.85D), caster.getZ(), 18, 0.45D, 0.50D, 0.45D, 0.04D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), SUNLIT_DESERT_GOLD_HEX_COLOR, 28, 1.00F, 0.06F),
                caster.getX(), caster.getY(0.90D), caster.getZ(), 18, 0.45D, 0.50D, 0.45D, 0.04D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.THORNS_HIT, SoundSource.PLAYERS, 0.75F, 1.18F);
        level.playSound(null, caster.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.65F, 1.32F);
    }

    // =========================================================================
    // FORM MUTATION 6 — AQUIFER GEYSER MINE (GLYPH / TRAP)
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
        // Section 6.21 Aquifer Geyser Mine:
        // Disarmable (Sneak + Right-Click for 50% refund) desert spring trap (1s arming delay, Single/Repeating)
        // that launches a pressurized burst of groundwater when triggered, blasting victims 8 blocks into the air
        // with Oasis Materia damage and hydrating nearby farmland!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.0D, radius);
        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.OASIS, EssenceType.WATER, maxDuration, tier
        );
        level.playSound(null, surface.above(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.80F, 1.15F);

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    // Canonical 1.0s (20t) Glyph arming delay
                    if (elapsed < 20) return true;
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    if (elapsed % 12 == 0) {
                        srv.sendParticles(ParticleTypes.SPLASH,
                                zone.center.x, zone.center.y + 0.12D, zone.center.z, 4, zone.radius * 0.42D, 0.05D, zone.radius * 0.42D, 0.02D);
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 1.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 2.2D, zone.center.z + zone.radius
                    );
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive() && e != owner && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)));
                    if (victims.isEmpty()) return true;

                    for (LivingEntity v : victims) {
                        SpellLingeringZoneManager.applySoaked(v, 160);
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                            );
                        }
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            float geyserDmg = (3.4F + tier * 0.92F) * Math.max(0.5F, zone.apparatusPotency);
                            SpellCombatReactionEngine.hurtWithOasisMateriaAdvantage(srv, v, owner, geyserDmg, false, true);
                        }
                        if (!SpellCombatReactionEngine.isBossEntity(srv, v)) {
                            // Pressurized groundwater burst blasting the victim 8 blocks into the air (vy = 1.22D -> ~8.2m apex)!
                            Vec3 cur = v.getDeltaMovement();
                            SpellExecutionEngine.applySyncedMotion(v, new Vec3(cur.x * 0.25D, 1.22D, cur.z * 0.25D));
                        }
                    }

                    BlockPos gPos = BlockPos.containing(zone.center);
                    hydrateAndGrowFlora(srv, gPos, 4);
                    bloomOasisPatch(srv, gPos, 3, false);

                    SpellWorldVfxManager.broadcastGeyserPlume(
                            srv, zone.center, Math.max(1.4D, zone.radius * 0.72D), 8.5D,
                            EssenceType.OASIS, EssenceType.WATER, 48, tier
                    );
                    srv.sendParticles(ParticleTypes.SPLASH,
                            zone.center.x, zone.center.y + 1.4D, zone.center.z, 36, 0.65D, 1.8D, 0.65D, 0.10D);
                    srv.playSound(null, gPos, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.10F, 0.85F);
                    srv.playSound(null, gPos, SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.85F, 1.15F);

                    triggersLeft[0]--;
                    rearmCooldown[0] = 35;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — DESERT GREENING MIST (CONE / SPRAY)
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
        // Section 6.21 Desert Greening Mist:
        // Sprays a gentle warm botanical mist that deals mild Oasis Materia damage, pacifies caught mobs for 4.0 seconds (80t),
        // hydrates dry soil, and accelerates the growth of all crops, sugarcane, and cactuses in the cone!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.22D, 0.0D),
                direction, coneRange, EssenceType.OASIS, EssenceType.NATURE, tier
        );

        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) {
                hit.clearFire();
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float mistDmg = (1.80F + tier * 0.50F) * potency;
                SpellCombatReactionEngine.hurtWithOasisMateriaAdvantage(level, hit, caster, mistDmg, false, true);
            }
            // Pacify caught mobs for exact 4.0 seconds (80 ticks)!
            SpellLingeringZoneManager.applyPacified(hit, 80);
            DustSpellMutationHandler.wipeMobAggroPublic(level, hit, 80L);
            hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
        }

        // Hydrate dry soil & accelerate crops, sugarcane, and cactuses across the full 60-degree cone fan!
        Vec3 flatFwd = new Vec3(direction.x, 0.0D, direction.z);
        if (flatFwd.lengthSqr() < 1.0E-4D) {
            flatFwd = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            flatFwd = flatFwd.normalize();
        }
        Vec3 flatRight = new Vec3(-flatFwd.z, 0.0D, flatFwd.x);
        int maxDist = Math.clamp((int) Math.ceil(coneRange), 2, 12);
        for (int s = 1; s <= maxDist; s++) {
            int halfSpan = Math.max(1, (int) Math.ceil(s * 0.577D)); // tan(30°) = 0.577 for full 60° cone!
            for (int lat = -halfSpan; lat <= halfSpan; lat += 2) {
                Vec3 sample = caster.position().add(flatFwd.scale(s)).add(flatRight.scale(lat));
                BlockPos surf = findGroundSurfaceNear(level, BlockPos.containing(sample));
                hydrateAndGrowFlora(level, surf, 1);
                if ((level.getBlockState(surf).is(Blocks.SAND) || level.getBlockState(surf).is(Blocks.RED_SAND))
                        && level.random.nextFloat() < 0.25F) {
                    bloomOasisPatch(level, surf, 1, false);
                }
            }
        }

        level.playSound(null, caster.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.90F, 1.15F);
        level.playSound(null, caster.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.70F, 1.35F);
    }

    // =========================================================================
    // FORM MUTATION 8 — MIRACLE DESERT DOWNPOUR (RAIN / SKYFALL)
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
        // Section 6.21 Miracle Desert Downpour:
        // Summons a tier-scaled 16m+ restorative rainstorm of sparkling springwater & golden pollen for 12.0 seconds (240t);
        // deals continuous Oasis Materia damage to hostiles, heals players/allies/pets, hydrates all farmland,
        // accelerates crop/cactus/sugarcane growth, and slowly converts sand blocks into fertile grass & flowers!
        int tier = spell.getEffectiveTier();
        double r = Math.max(8.0D, radius);
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        BlockPos baseAir = surface.above();
        Vec3 snappedCenter = new Vec3(surface.getX() + 0.5D, baseAir.getY(), surface.getZ() + 0.5D);
        double canopyHeight = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, baseAir, tier);
        int totalTicks = 240; // Canonical 12.0 seconds

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, snappedCenter, r, EssenceType.OASIS, EssenceType.WATER, totalTicks, tier
        );
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snappedCenter.add(0.0D, 0.03D, 0.0D), r, EssenceType.OASIS, EssenceType.WATER, totalTicks, tier
        );
        level.playSound(null, baseAir, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.PLAYERS, 0.85F, 1.12F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, r, totalTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;

                    if (elapsed % 5 == 0) {
                        double rainY = zone.center.y + Math.min(canopyHeight * 0.68D, 11.0D);
                        srv.sendParticles(ParticleTypes.RAIN,
                                zone.center.x, rainY, zone.center.z, 24 + tier * 2, zone.radius * 0.52D, 1.2D, zone.radius * 0.52D, 0.08D);
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                                zone.center.x, rainY * 0.90D, zone.center.z, 10 + tier, zone.radius * 0.48D, 1.0D, zone.radius * 0.48D, 0.03D);
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float potency = Math.max(0.5F, zone.apparatusPotency);
                        BlockPos cPos = BlockPos.containing(zone.center);
                        int br = Math.min(14, (int) Math.ceil(zone.radius));

                        // Hydrate all farmland, snuff fires, accelerate crops/cactus/sugarcane, and gradually convert sand into fertile grass!
                        hydrateAndGrowFloraCircular(srv, cPos, zone.radius);
                        bloomOasisPatchGradual(srv, cPos, zone.radius, 10 + tier * 2);

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + Math.max(5.0D, canopyHeight * 0.60D), zone.center.z + zone.radius
                        );
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            double dx = e.getX() - zone.center.x;
                            double dz = e.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > (zone.radius + 0.6D) * (zone.radius + 0.6D)) continue;

                            e.clearFire();
                            if (e instanceof Player || SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)) {
                                float heal = (2.2F + tier * 0.45F) * potency;
                                SpellCombatReactionEngine.notifyMagicalHeal(e, heal);
                                e.heal(heal);
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)) {
                                SpellLingeringZoneManager.applySoaked(e, 60);
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, e, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                                    );
                                }
                                if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                    float rainDmg = (1.85F + tier * 0.55F) * potency;
                                    SpellCombatReactionEngine.hurtWithOasisMateriaAdvantage(srv, e, owner, rainDmg, false, true);
                                }
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — OASIS GUARDIAN EIDOLON SUITE (AGENT / SENTRY / DECOY)
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
        tag.putString("SpellEidolonMateria", EssenceType.OASIS.name());
        tag.putInt("SpellEidolonTintRGB", OASIS_GUARDIAN_HEX_COLOR);

        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.65D, radius * 0.35D), EssenceType.OASIS, EssenceType.ARID, tier
        );
        level.sendParticles(ParticleTypes.SPLASH,
                spawnPos.x, spawnPos.y + 0.85D, spawnPos.z, 22, 0.45D, 0.55D, 0.45D, 0.06D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), SUNLIT_DESERT_GOLD_HEX_COLOR, 30, 1.05F, 0.06F),
                spawnPos.x, spawnPos.y + 0.90D, spawnPos.z, 18, 0.45D, 0.55D, 0.45D, 0.04D
        );
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.25F);
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
        // Section 6.21 Mirage-Spring Blink:
        // Dissolves into a shimmering desert heat mirage and teleports 14+ blocks away in a splash of springwater,
        // leaving a blooming flower decoy at the point of departure!
        caster.clearFire();
        EntityHelper.getPersistentData(caster).remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);

        BlockPos depGround = findGroundSurfaceNear(level, BlockPos.containing(departurePos));
        BlockPos flowerPos = depGround.above().immutable();
        Block decoyFlower = OASIS_DESERT_FLOWERS[level.random.nextInt(OASIS_DESERT_FLOWERS.length)];
        boolean placedFlower = false;
        if (level.getBlockState(flowerPos).isAir()
                && !SpellExecutionEngine.isProtectedBlockOrContainer(level, flowerPos, level.getBlockState(flowerPos))
                && decoyFlower.defaultBlockState().canSurvive(level, flowerPos)) {
            level.setBlockAndUpdate(flowerPos, decoyFlower.defaultBlockState());
            placedFlower = true;
        }
        final boolean fPlaced = placedFlower;

        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.OASIS, potencyTier);
        // Shimmering desert heat mirage at departure + soothing springwater splash at arrival!
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), SUNLIT_DESERT_GOLD_HEX_COLOR, 28, 1.08F, 0.06F),
                departurePos.x, departurePos.y + 0.95D, departurePos.z, 24, 0.42D, 0.55D, 0.42D, 0.05D
        );
        level.sendParticles(ParticleTypes.SPLASH,
                arrivalPos.x, arrivalPos.y + 0.85D, arrivalPos.z, 28, 0.45D, 0.55D, 0.45D, 0.08D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), LUSH_OASIS_GREEN_HEX_COLOR, 26, 1.00F, 0.06F),
                arrivalPos.x, arrivalPos.y + 0.90D, arrivalPos.z, 18, 0.42D, 0.50D, 0.42D, 0.05D
        );
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.85F, 1.18F);

        AABB lureBox = new AABB(
                departurePos.x - 5.5D, departurePos.y - 2.0D, departurePos.z - 5.5D,
                departurePos.x + 5.5D, departurePos.y + 2.8D, departurePos.z + 5.5D
        );
        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 5.5D, 80, level.getGameTime(), dummy, dummy.subSpells().getFirst(), EssenceType.OASIS, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (fPlaced && srv.getBlockState(flowerPos).is(decoyFlower)) {
                            srv.removeBlock(flowerPos, false);
                        }
                        return false;
                    }
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                                flowerPos.getX() + 0.5D, flowerPos.getY() + 0.55D, flowerPos.getZ() + 0.5D, 4, 0.25D, 0.25D, 0.25D, 0.02D);
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        for (Mob mob : srv.getEntitiesOfClass(Mob.class, lureBox, LivingEntity::isAlive)) {
                            if (mob instanceof Monster && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, mob)) {
                                mob.setTarget(null);
                                mob.getNavigation().moveTo(flowerPos.getX() + 0.5D, flowerPos.getY(), flowerPos.getZ() + 0.5D, 1.12D);
                                if (mob.position().distanceToSqr(departurePos) <= 4.0D) {
                                    SpellLingeringZoneManager.applyPacified(mob, 40);
                                }
                            }
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
        // Section 6.21 Desert Greening Transmute:
        // Converts sand & red sand into fertile grass or farmland, dead bushes into flowers, and coarse dirt into podzol!
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        BlockState out = null;
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            boolean tillFarmland = (caster != null && caster.isShiftKeyDown() && level.getBlockState(pos.above()).isAir());
            out = tillFarmland
                    ? Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7)
                    : Blocks.GRASS_BLOCK.defaultBlockState();
        } else if (state.is(Blocks.DIRT)) {
            out = level.getBlockState(pos.above()).isAir()
                    ? Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7)
                    : Blocks.GRASS_BLOCK.defaultBlockState();
        } else if (state.is(Blocks.FARMLAND) && state.getValue(FarmBlock.MOISTURE) < 7) {
            out = state.setValue(FarmBlock.MOISTURE, 7);
        } else if (state.is(Blocks.DEAD_BUSH)) {
            Block flower = OASIS_DESERT_FLOWERS[level.random.nextInt(OASIS_DESERT_FLOWERS.length)];
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);
            if ((belowState.is(Blocks.SAND) || belowState.is(Blocks.RED_SAND))
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, below, belowState)) {
                level.setBlockAndUpdate(below, Blocks.GRASS_BLOCK.defaultBlockState());
            }
            out = flower.defaultBlockState();
        } else if (state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)) {
            out = Blocks.PODZOL.defaultBlockState();
        }

        if (out == null) {
            return false;
        }
        level.setBlockAndUpdate(pos, out);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5D, pos.getY() + 0.75D, pos.getZ() + 0.5D, 5, 0.25D, 0.25D, 0.25D, 0.02D);
        maybeGrantOasisBatchTransmuteDiscount(level, caster, potencyTier);
        return true;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        ItemStack in = itemEntity.getItem();
        int c = in.getCount();
        ItemStack out = ItemStack.EMPTY;
        if (in.is(Items.SAND) || in.is(Items.RED_SAND)) {
            out = new ItemStack((caster != null && caster.isShiftKeyDown()) ? Items.FARMLAND : Items.GRASS_BLOCK, c);
        } else if (in.is(Items.DIRT)) {
            out = new ItemStack(Items.FARMLAND, c);
        } else if (in.is(Items.DEAD_BUSH)) {
            out = new ItemStack(level.random.nextBoolean() ? Items.POPPY : Items.ORANGE_TULIP, c);
        } else if (in.is(Items.COARSE_DIRT) || in.is(Items.ROOTED_DIRT)) {
            out = new ItemStack(Items.PODZOL, c);
        }

        if (out.isEmpty()) {
            return false;
        }
        itemEntity.setItem(out);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                itemEntity.getX(), itemEntity.getY() + 0.25D, itemEntity.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.02D);
        maybeGrantOasisBatchTransmuteDiscount(level, caster, potencyTier);
        return true;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.21 Dehydrating Sunstroke Hex:
        // Persistent parching mirage curse (milk-immune; cleansed via Water/Rain or Restoration magic, or caster defeat):
        // permanently drains hunger and saturation, cuts attack speed by 35%, and deals Oasis Materia damage
        // whenever the afflicted entity attempts to sprint or jump (also enforced every-tick in SpellLingeringZoneManager)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 1, true, true));
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, 0.35D * mult, 35L);
        if (target instanceof Player player) {
            player.causeFoodExhaustion(1.8F * mult);
        }

        float parchDmg = (1.10F + potencyTier * 0.38F) * mult;
        SpellCombatReactionEngine.hurtWithOasisMateriaAdvantage(level, target, null, parchDmg, false, true);

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), SUNLIT_DESERT_GOLD_HEX_COLOR, 20, 0.90F, 0.04F),
                target.getX(), target.getY(0.80D), target.getZ(), 5, 0.28D, 0.35D, 0.28D, 0.02D
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
            // Section 6.21 Miracle Spring Mending Restoration:
            // Cleanses negative status effects & hexes, heals health points, and refills hunger and saturation!
            target.clearFire();
            EntityHelper.getPersistentData(target).remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(target);

            float bonusHeal = 4.0F + potencyTier * 1.40F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, bonusHeal);
            target.heal(bonusHeal);
            if (target instanceof Player player) {
                player.getFoodData().eat(5 + potencyTier, 0.85F);
            }

            level.sendParticles(ParticleTypes.SPLASH,
                    target.getX(), target.getY(0.80D), target.getZ(), 16, 0.38D, 0.45D, 0.38D, 0.05D);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY(0.85D), target.getZ(), 14, 0.38D, 0.45D, 0.38D, 0.04D);
            level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.32F);
        }
    }

    // =========================================================================
    // PUBLIC & PRIVATE HELPERS
    // =========================================================================

    public static void hydrateAndGrowFloraPublic(ServerLevel level, BlockPos center, int r) {
        hydrateAndGrowFlora(level, center, r);
    }

    private static void hydrateAndGrowFlora(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 2, r))) {
            hydrateAndGrowAt(level, p);
        }
    }

    private static void hydrateAndGrowFloraCircular(ServerLevel level, BlockPos center, double radius) {
        int br = Math.min(14, (int) Math.ceil(radius));
        double rSq = (radius + 0.5D) * (radius + 0.5D);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-br, -1, -br), center.offset(br, 2, br))) {
            double dx = (p.getX() + 0.5D) - (center.getX() + 0.5D);
            double dz = (p.getZ() + 0.5D) - (center.getZ() + 0.5D);
            if (dx * dx + dz * dz <= rSq) {
                hydrateAndGrowAt(level, p);
            }
        }
    }

    private static void hydrateAndGrowAt(ServerLevel level, BlockPos p) {
        BlockState bs = level.getBlockState(p);
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
            return;
        }
        if (bs.getBlock() instanceof BaseFireBlock) {
            level.removeBlock(p, false);
        } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
            level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
        } else if (bs.getBlock() instanceof AbstractCandleBlock && bs.getValue(AbstractCandleBlock.LIT)) {
            AbstractCandleBlock.extinguish(null, bs, level, p);
        } else if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
            level.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
        } else if (bs.getBlock() instanceof BonemealableBlock grow
                && grow.isValidBonemealTarget(level, p, bs)
                && level.random.nextFloat() < 0.38F) {
            grow.performBonemeal(level, level.random, p.immutable(), bs);
        } else if ((bs.getBlock() instanceof CactusBlock || bs.getBlock() instanceof SugarCaneBlock)
                && level.random.nextFloat() < 0.42F) {
            BlockPos up = p.above();
            if (level.getBlockState(up).isAir() && !level.getBlockState(p.below(2)).is(bs.getBlock())) {
                level.setBlockAndUpdate(up, bs.getBlock().defaultBlockState());
            }
        }
    }

    private static void bloomOasisPatch(ServerLevel level, BlockPos center, int r, boolean guaranteeCenter) {
        double rSq = (r + 0.45D) * (r + 0.45D);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
            if (p.distSqr(center) > rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;

            boolean isCenter = p.getX() == center.getX() && p.getZ() == center.getZ();
            if ((bs.is(Blocks.SAND) || bs.is(Blocks.RED_SAND))
                    && ((guaranteeCenter && isCenter) || level.random.nextFloat() < 0.45F)) {
                BlockPos up = p.above();
                BlockState upState = level.getBlockState(up);
                boolean clearAbove = upState.isAir() || upState.is(Blocks.DEAD_BUSH);
                if (!clearAbove) continue;

                if (upState.is(Blocks.DEAD_BUSH)) {
                    level.removeBlock(up, false);
                }
                if (level.random.nextFloat() < 0.32F) {
                    level.setBlockAndUpdate(p, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
                } else {
                    level.setBlockAndUpdate(p, Blocks.GRASS_BLOCK.defaultBlockState());
                    if (level.getBlockState(up).isAir() && level.random.nextFloat() < 0.42F) {
                        Block flower = OASIS_DESERT_FLOWERS[level.random.nextInt(OASIS_DESERT_FLOWERS.length)];
                        if (flower.defaultBlockState().canSurvive(level, up)) {
                            level.setBlockAndUpdate(up, flower.defaultBlockState());
                        }
                    }
                }
            } else if (bs.is(Blocks.DEAD_BUSH) && level.random.nextFloat() < 0.55F) {
                BlockPos below = p.below();
                BlockState belowState = level.getBlockState(below);
                if ((belowState.is(Blocks.SAND) || belowState.is(Blocks.RED_SAND))
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, below, belowState)) {
                    level.setBlockAndUpdate(below, Blocks.GRASS_BLOCK.defaultBlockState());
                }
                Block flower = OASIS_DESERT_FLOWERS[level.random.nextInt(OASIS_DESERT_FLOWERS.length)];
                if (flower.defaultBlockState().canSurvive(level, p)) {
                    level.setBlockAndUpdate(p, flower.defaultBlockState());
                }
            }
        }
    }

    private static void bloomOasisPatchGradual(ServerLevel level, BlockPos center, double radius, int samplesPerSecond) {
        for (int i = 0; i < samplesPerSecond; i++) {
            double ang = level.random.nextDouble() * Math.PI * 2.0D;
            double dist = Math.sqrt(level.random.nextDouble()) * radius;
            BlockPos sample = BlockPos.containing(
                    center.getX() + 0.5D + Math.cos(ang) * dist,
                    center.getY(),
                    center.getZ() + 0.5D + Math.sin(ang) * dist
            );
            BlockPos surf = findGroundSurfaceNear(level, sample);
            BlockState bs = level.getBlockState(surf);
            if ((bs.is(Blocks.SAND) || bs.is(Blocks.RED_SAND))
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, surf, bs)) {
                BlockPos up = surf.above();
                BlockState upState = level.getBlockState(up);
                if (upState.isAir() || upState.is(Blocks.DEAD_BUSH)) {
                    if (upState.is(Blocks.DEAD_BUSH)) {
                        level.removeBlock(up, false);
                    }
                    level.setBlockAndUpdate(surf, Blocks.GRASS_BLOCK.defaultBlockState());
                    if (level.getBlockState(up).isAir() && level.random.nextFloat() < 0.35F) {
                        Block flower = OASIS_DESERT_FLOWERS[level.random.nextInt(OASIS_DESERT_FLOWERS.length)];
                        if (flower.defaultBlockState().canSurvive(level, up)) {
                            level.setBlockAndUpdate(up, flower.defaultBlockState());
                        }
                    }
                }
            }
        }
    }

    private static void maybeGrantOasisBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellOasisBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellOasisBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellOasisBatchTick", now);
        pData.putInt("SpellOasisBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.OASIS, refund);
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
