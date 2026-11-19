package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModEffects;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Spring Materia (Tier 3 Trinity Fusion #42)</b>
 * (Spellcrafting System Guide Section 6.43 — Trinity Vitae + Nature + Water harmonic matrix):
 * <ul>
 *     <li><b>Palette</b>: Blossom Pink ({@code #FF6B9D}) + Aquifer Cyan ({@code #64C8FF})</li>
 *     <li><b>Advantage</b>: Strong vs <b>UNDEAD (+1)</b>, <b>WATER (+1)</b>, <b>EARTH (+1)</b>, <b>SOULFIRE (+1)</b>,
 *         and <b>BLIGHT (+1)</b> (plus <b>NETHER</b> &amp; <b>ARID</b> under Water Dominance) | Weak vs <b>UMBRAL (-1)</b></li>
 * </ul>
 */
public final class SpringSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int VERNAL_SPRING_HEX_COLOR = 0xFF6B9D;
    public static final int AQUIFER_CYAN_HEX_COLOR = 0x64C8FF;

    private static final String TAG_SPRING_BEAM_LAST_TICK = "SpringBeamLastTick";
    private static final String TAG_SPRING_BEAM_TICKS = "SpringBeamContactTicks";
    private static final String TAG_SPRING_TRANSMUTE_BATCH_TICK = "SpringTransmuteBatchTick";

    private static final Block[] VERNAL_FLORA_BLOCKS = {
            Blocks.PINK_PETALS, Blocks.DANDELION, Blocks.POPPY, Blocks.AZURE_BLUET
    };

    @Override
    public EssenceType materiaType() {
        return EssenceType.SPRING;
    }

    // =========================================================================
    // TITLES & CODEX / TOOLTIP DESCRIPTIONS
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Vernal Blossom-Aquifer Spiral Flow";
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return "4 swirling droplets of pressurized springwater & cherry blossom petals (#FF6B9D / #64C8FF) grant +5 Armor, +2 Toughness, Drowning/Poison/Wither immunity, fire quenching, projectile-to-1-HP healing mist conversion, melee health siphon, and 2.0s-rooting / ally-mending vernal bolts on melee or spell hit.";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Vernal Blossom Aquifer-Orb";
            case BEAM -> "Photosynthetic Life-Tether";
            case RADIAL_BURST -> "Vernal Rebirth Blossom-Nova";
            case TOUCH_STRIKE -> "Vernal Blossom-Cleave";
            case AURA -> "Vernal Fountainhead Mantle";
            case GLYPH -> "Bubbling Vernal Aquifer Well Glyph";
            case CONE -> "Vernal Rejuvenation Torrent Cone";
            case SKYFALL -> "Vernal Miracle Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Hurls a 3D swirling aquifer orb & cherry blossom spiral (#FF6B9D / #64C8FF) that heals allies 3 hearts + Regeneration II (4.0s), or knocks foes 5m back, smites & roots Undead for 3.0s (+50% damage), and blooms temporary vernal flora.";
            case BEAM ->
                    "Dual-stream floral-aquifer tether siphoning 3 HP/s & Slowness III from hostiles, or healing/cleansing allies (+30% attack speed); 5.0s sustained channeling triggers Vernal Bloom Supercharge (8m knockback + +4 Absorption Hearts to allies).";
            case RADIAL_BURST ->
                    "6m+ zero-light-block springwater & rose-petal shockwave hurling foes 7m back, smiting Undead, maturing crops & saplings, and granting caster & allies Vernal Vigor (+20% speed, +3 Armor & Regeneration II for 8.0s).";
            case TOUCH_STRIKE ->
                    "Sweeping aquifer & blossom-vine cleave smiting Undead (+50% damage & -3 armor), knocking foes 5m back, and leaving a 2.5s-rooting / 2-heart-healing vernal arc; cast on an ally as Touch of Spring to purge debuffs & grant +3 Absorption Hearts.";
            case AURA ->
                    "Envelops caster (or anchored area) in bubbling springwater & cherry petals (+6 Armor, +2 Toughness, Drowning/Poison/Wither/Fire immunity, Water Breathing, +30% swim speed, 1 heart/3s self-regen, and 5m 1-heart/4s ally heal & cleanse).";
            case GLYPH ->
                    "Disarmable Single/Repeating dual-purpose font: heals allies 5 hearts + debuff cleanse + Regeneration II (6.0s), or erupts into a 4m Geyser Chasm for 4.0s launching hostiles upward, stripping 3 armor, and drowning Undead.";
            case CONE ->
                    "60° pressurized springwater & blossom cone healing allies 1.5 hearts/s & extinguishing fire while knocking foes 3m back, smiting Undead, and inflicting Vernal Intoxication (-50% attack speed & 25.0s AI disruption).";
            case SKYFALL ->
                    "Extended 18.0s miracle downpour + 8.0s verdant sanctuary across 12m+: pins foes down, strips 5 armor & smites Undead while healing allies 1 heart/s, restoring hunger, granting Regeneration II + Resistance I, and maturing crops.";
            case SUMMON_AGENT ->
                    "45s / 65 HP refreshing Vernal Bloom-Bladewarden delivering 35%-armor-bypassing springwater rapier thrusts that smite Undead and splash 2 hearts of healing pollen to allies.";
            case SUMMON_SENTRY ->
                    "45s / 85 HP refreshing Aquifer Springwater Font Spire firing piercing water lances up to 16m, pulsing an 8m 3-heart heal & cleanse every 4s, and granting allies +6 Armor, +3 Toughness & Water Breathing.";
            case SUMMON_DECOY ->
                    "12s Blossoming Vernal Nectar Effigy projecting a 14m taunt before detonating into a 10m Vernal Rebirth Nova (8m knockback, -4 armor, full ally health restore & Regeneration III for 6.0s).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Petal-Warp Blink";
            case TRANSMUTE, BREAK -> "Vernal Purification & Greening Transmutation";
            case AFFLICTION -> "Uncontrolled Vernal Overgrowth Hex";
            default -> "Vernal Spring " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Dissolves into fragrant cherry blossom petals and re-materializes up to 14m away, shedding all hostile mob aggro and rendering the caster undetectable to Undead for 4.0s.";
            case TRANSMUTE, BREAK ->
                    "Catalytic vernal restoration: cures Netherrack/Soul Sand/Sculk into Grass/Moss/Farmland, hydrates Farmland across 6m, cures Zombie Villagers, refines Rotten Flesh into Leather/Beef/Pork, matures crops, and purifies fluids.";
            case AFFLICTION ->
                    "Milk-immune hyper-vegetative curse rooting & stunning victims 1.0s every 4.0s, cutting movement speed by 50%, attack speed by 40%, and Armor Toughness by 35%, smiting Undead, and dropping 1-heart healing dew-orbs for allies.";
            default -> "Infuses " + effect.getDisplayName() + " with Tier 3 Trinity Spring Materia.";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — VERNAL BLOSSOM-AQUIFER SPIRAL FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.02D);
        if (flow == SpellFlowModifier.ORBIT_SATELLITE && projectile.tickCount % 4 == 0) {
            LivingEntity owner = projectile.getOwner() instanceof LivingEntity liv ? liv : null;
            for (Projectile inc : level.getEntitiesOfClass(Projectile.class, projectile.getBoundingBox().inflate(1.65D),
                    p -> !(p instanceof SpellProjectileEntity))) {
                if (owner != null && inc.getOwner() == owner) continue;
                level.sendParticles(ParticleTypes.CHERRY_LEAVES, inc.getX(), inc.getY(), inc.getZ(), 6, 0.12D, 0.12D, 0.12D, 0.03D);
                level.sendParticles(ParticleTypes.SPLASH, inc.getX(), inc.getY(), inc.getZ(), 5, 0.12D, 0.12D, 0.12D, 0.03D);
                inc.discard();
                if (owner != null && owner.isAlive()) {
                    SpellCombatReactionEngine.notifyMagicalHeal(owner, 1.0F);
                    owner.heal(1.0F);
                }
            }
        }
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
        level.sendParticles(ParticleTypes.DRIPPING_WATER, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 1 — VERNAL BLOSSOM AQUIFER-ORB (PROJECTILE)
    // -------------------------------------------------------------------------

    @Override
    public void onProjectileHitEntityFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.43 Vernal Blossom Aquifer-Orb:
        // On allies -> restores 3 hearts (6.0 HP) + Regeneration II for 4.0s (80t);
        // On hostiles -> heavy Spring Materia damage, +50% Undead smite + 3.0s (60t) root, 5m knockback,
        // and coats terrain in temporary blooming vernal flora!
        if (!(hitEntity instanceof LivingEntity target) || !target.isAlive()) return;
        int tier = spell.getEffectiveTier();

        SpellWorldVfxManager.broadcastProjectileImpact(
                level, target.position().add(0.0D, 0.4D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.SPRING, EssenceType.WATER, 1.75F, tier
        );

        if (caster != null && isFriendlyTarget(target, caster)) {
            target.clearFire();
            SpellCombatReactionEngine.notifyMagicalHeal(target, 6.0F);
            target.heal(6.0F);
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 1));
            level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY(1.0D), target.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.03D);
            level.sendParticles(ParticleTypes.CHERRY_LEAVES, target.getX(), target.getY(0.9D), target.getZ(), 16, 0.5D, 0.4D, 0.5D, 0.04D);
            level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.30F);
            return;
        }

        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(target, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        boolean isUndead = target.getType().is(EntityTypeTags.UNDEAD);
        if (isUndead) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
        } else {
            Vec3 vel = projectile.getDeltaMovement();
            Vec3 kb = vel.lengthSqr() > 1.0E-4D ? vel.normalize().scale(1.25D) : new Vec3(0.0D, 0.32D, 0.0D);
            target.setDeltaMovement(kb.x, 0.32D, kb.z); // ~5m knockback!
            target.hasImpulse = true;
        }
        if (!slot.hasHarmVerb()) {
            float baseDmg = (3.70F + tier * 0.95F) * (isUndead ? 1.50F : 1.0F);
            SpellCombatReactionEngine.hurtWithSpringMateriaAdvantage(level, target, caster, baseDmg, false, true);
        }
        placeTemporaryVernalFlora(level, caster, target.position(), 2, 120);
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
        Vec3 loc = blockHit.getLocation();
        int tier = spell.getEffectiveTier();
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, loc.add(0.0D, 0.2D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.SPRING, EssenceType.WATER, 1.75F, tier
        );
        placeTemporaryVernalFlora(level, caster, loc, 2, 120);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, loc.x, loc.y + 0.4D, loc.z, 18, 0.8D, 0.3D, 0.8D, 0.04D);
        level.sendParticles(ParticleTypes.SPLASH, loc.x, loc.y + 0.3D, loc.z, 16, 0.8D, 0.3D, 0.8D, 0.06D);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 2 — PHOTOSYNTHETIC LIFE-TETHER (BEAM)
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
        // Section 6.43 Photosynthetic Life-Tether:
        // Against hostiles: siphons 3 HP/s & inflicts Slowness III;
        // Against allies: rapidly transfers vitality, cures Poison/Wither/debuffs & grants +30% attack speed;
        // REQUIRES 5.0s (100t / 5 pulses) sustained channeling to trigger Vernal Bloom Supercharge (8m knockback
        // on hostiles + +4 Absorption Hearts / 8.0F to caster & all allies)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 3.0F);
                hit.heal(3.0F);
                SpellCombatReactionEngine.applyTimedAttackSpeedBuff(hit, level, 0.30D, 80L);

                CompoundTag aData = EntityHelper.getPersistentData(hit);
                long aLast = aData.getLong(TAG_SPRING_BEAM_LAST_TICK).orElse(-100L);
                long aDelta = now - aLast;
                int aStep = (aDelta > 0L && aDelta <= 8L) ? (int) Math.max(4L, aDelta) : 20;
                int aTicks = (aDelta <= 45L) ? aData.getInt(TAG_SPRING_BEAM_TICKS).orElse(0) + aStep : aStep;
                aData.putLong(TAG_SPRING_BEAM_LAST_TICK, now);
                aData.putInt(TAG_SPRING_BEAM_TICKS, aTicks);
                if (aTicks >= 100) {
                    aData.putInt(TAG_SPRING_BEAM_TICKS, 0);
                    triggerVernalBloomSupercharge(level, caster, hit.position(), dirNorm, tier, wardSafe, apparatusPotency);
                }
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2)); // Slowness III!
            float siphonHp = 3.0F;
            boolean isUndead = hit.getType().is(EntityTypeTags.UNDEAD);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithSpringMateriaAdvantage(
                        level, hit, caster, siphonHp * (isUndead ? 1.50F : 1.0F), false, true
                );
            }
            if (caster.isAlive()) {
                SpellCombatReactionEngine.notifyMagicalHeal(caster, siphonHp);
                caster.heal(siphonHp);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_SPRING_BEAM_LAST_TICK).orElse(-100L);
            long delta = now - last;
            int stepTicks = (delta > 0L && delta <= 8L) ? (int) Math.max(4L, delta) : 20;
            int contactTicks = (delta <= 45L) ? data.getInt(TAG_SPRING_BEAM_TICKS).orElse(0) + stepTicks : stepTicks;
            data.putLong(TAG_SPRING_BEAM_LAST_TICK, now);
            data.putInt(TAG_SPRING_BEAM_TICKS, contactTicks);

            if (contactTicks >= 100) {
                data.putInt(TAG_SPRING_BEAM_TICKS, 0);
                triggerVernalBloomSupercharge(level, caster, hit.position(), dirNorm, tier, wardSafe, apparatusPotency);
            }
        }
    }

    private static void triggerVernalBloomSupercharge(
            ServerLevel level,
            LivingEntity caster,
            Vec3 burstPos,
            Vec3 dirNorm,
            int tier,
            boolean wardSafe,
            float apparatusPotency
    ) {
        SpellWorldVfxManager.broadcastRadialNova(level, burstPos, 4.5D, EssenceType.SPRING, EssenceType.WATER, tier);
        float burstDmg = (10.0F + tier * 2.45F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(burstPos, burstPos).inflate(4.5D), LivingEntity::isAlive)) {
            if (isFriendlyTarget(foe, caster)) {
                foe.setAbsorptionAmount(Math.max(foe.getAbsorptionAmount(), 8.0F));
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
            Vec3 away = dirNorm.scale(1.88D);
            foe.setDeltaMovement(away.x, 0.44D, away.z); // 8m knockback!
            foe.hasImpulse = true;
            float smiteMult = foe.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
            SpellCombatReactionEngine.hurtWithSpringMateriaAdvantage(level, foe, caster, burstDmg * smiteMult, false, true);
        }
        caster.setAbsorptionAmount(Math.max(caster.getAbsorptionAmount(), 8.0F));
        for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(8.0D), e -> isFriendlyTarget(e, caster))) {
            ally.setAbsorptionAmount(Math.max(ally.getAbsorptionAmount(), 8.0F)); // +4 Absorption Hearts!
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.42F, 0.62F),
                burstPos.x, burstPos.y + 1.0D, burstPos.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, burstPos.x, burstPos.y + 0.9D, burstPos.z, 32, 1.6D, 0.5D, 1.6D, 0.05D);
        level.playSound(null, BlockPos.containing(burstPos), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.05F, 1.25F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 3 — VERNAL REBIRTH BLOSSOM-NOVA (RADIAL BURST)
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
        // Section 6.43 Vernal Rebirth Blossom-Nova:
        // 6m+ radius, strictly ZERO physical light blocks — hurls hostiles 7m backward, deals heavy Spring Materia
        // damage, smites Undead, instantly matures all crops & saplings in radius, and grants caster & allies
        // Vernal Vigor (+20% movement speed, +3 Armor, and Regeneration II for 8.0s / 160t)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        grantVernalVigor(caster, level);

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.SPRING, EssenceType.WATER, tier);

        float novaDmg = (3.60F + tier * 0.92F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (isFriendlyTarget(ent, caster)) {
                grantVernalVigor(ent, level);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) continue;

            Vec3 delta = ent.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.68D) : new Vec3(0.0D, 0.38D, 0.0D);
            ent.setDeltaMovement(away.x, 0.38D, away.z); // ~7m knockback!
            ent.hasImpulse = true;
            boolean isUndead = ent.getType().is(EntityTypeTags.UNDEAD);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithSpringMateriaAdvantage(
                        level, ent, caster, novaDmg * (isUndead ? 1.50F : 1.0F), false, true
                );
            }
        }
        matureCropsInRadius(level, BlockPos.containing(center), (int) Math.min(8, Math.ceil(r)));
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.42F, 0.62F),
                center.x, center.y + 1.0D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, center.x, center.y + 1.0D, center.z, 38, 2.2D, 0.6D, 2.2D, 0.05D);
        level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.8D, center.z, 30, 2.2D, 0.4D, 2.2D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.18F);
    }

    private static void grantVernalVigor(LivingEntity ally, ServerLevel level) {
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 3.0D, 0.0D, 0.0D, 0.20D, 160L);
        ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
    }

    public static void matureCropsInRadius(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 2, r))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof BonemealableBlock growable && !(bs.is(Blocks.GRASS_BLOCK) || bs.is(Blocks.MOSS_BLOCK))) {
                if (growable.isValidBonemealTarget(level, p, bs)) {
                    growable.performBonemeal(level, level.random, p.immutable(), bs);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 4 — VERNAL BLOSSOM-CLEAVE (TOUCH / STRIKE)
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
        // Section 6.43 Vernal Blossom-Cleave:
        // Direct ally cast -> Touch of Spring (purges all debuffs & grants 3 Absorption Hearts / 6.0F);
        // Hostiles -> heavy Spring Materia damage stacking weapon synergy, smites Undead (+50% damage & -3 armor),
        // knocks enemies 5m back, and leaves a 4.0s (80t) blooming vernal water arc rooting enemies 2.5s (50t) &
        // healing allies who step across it for 2 hearts (4.0 HP)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), push, 3.3D,
                EssenceType.SPRING, EssenceType.WATER, tier
        );

        float cleaveDmg = (3.90F + tier * 1.00F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                hit.setAbsorptionAmount(Math.max(hit.getAbsorptionAmount(), 6.0F));
                level.sendParticles(ParticleTypes.HEART, hit.getX(), hit.getY(1.0D), hit.getZ(), 5, 0.25D, 0.25D, 0.25D, 0.03D);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            boolean isUndead = hit.getType().is(EntityTypeTags.UNDEAD);
            if (isUndead) {
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            }
            hit.setDeltaMovement(push.x * 1.25D, 0.32D, push.z * 1.25D); // ~5m knockback!
            hit.hasImpulse = true;
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithSpringMateriaAdvantage(
                        level, hit, caster, cleaveDmg * (isUndead ? 1.50F : 1.0F), true, true
                );
            }
        }

        Vec3 arcPos = caster.position().add(push.scale(2.4D));
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, arcPos.add(0.0D, 0.04D, 0.0D), 2.5D, EssenceType.SPRING, EssenceType.WATER, 80, tier
        );
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, arcPos, 2.5D, 80, level.getGameTime(), spell, slot, EssenceType.SPRING, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB box = new AABB(
                                zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                                zone.center.x + 2.5D, zone.center.y + 1.8D, zone.center.z + 2.5D
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (owner != null && isFriendlyTarget(ent, owner)) {
                                SpellCombatReactionEngine.notifyMagicalHeal(ent, 4.0F);
                                ent.heal(4.0F); // 2 hearts!
                                srv.sendParticles(ParticleTypes.HEART, ent.getX(), ent.getY(1.0D), ent.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.02D);
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zSafe)) {
                                ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                                ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                            }
                        }
                        srv.sendParticles(ParticleTypes.CHERRY_LEAVES, zone.center.x, zone.center.y + 0.2D, zone.center.z, 8, 1.0D, 0.1D, 1.0D, 0.02D);
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 5 — VERNAL FOUNTAINHEAD MANTLE (AURA / SELF)
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
        // Section 6.43 Vernal Fountainhead Mantle:
        // +6 Armor, +2 Toughness, complete immunity to Drowning, Poison, Wither & fire ticks, Water Breathing,
        // +30% swim speed, 1 heart/3s self-regen, and 5m restorative aura (1 heart/4s + debuff cleanse)!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SPRING_MANTLE_UNTIL, level.getGameTime() + dur);
        caster.clearFire();
        caster.setAirSupply(caster.getMaxAirSupply());
        caster.removeEffect(MobEffects.POISON);
        caster.removeEffect(MobEffects.WITHER);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 2.0D, 0.0D, 0.0D, dur);
        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur + 40, 0, false, false));
        caster.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, dur, 0, false, false));
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, caster.getX(), caster.getY(0.9D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.03D);
        level.sendParticles(ParticleTypes.SPLASH, caster.getX(), caster.getY(0.7D), caster.getZ(), 18, 0.4D, 0.4D, 0.4D, 0.04D);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.32F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 6 — BUBBLING VERNAL AQUIFER WELL GLYPH (GLYPH / TRAP)
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
        // Section 6.43 Bubbling Vernal Aquifer Well Glyph:
        // Disarmable Single/Repeating dual-purpose active font (#FF6B9D / #64C8FF):
        // Ally step -> Restorative Spring (cleanses negative debuffs, restores 5 hearts / 10 HP, Regen II 6.0s);
        // Hostile step -> 4m Geyser Chasm for 4.0s (80t), blasting hostiles into the air, stripping 3 armor,
        // dealing heavy Spring Materia damage, and drowning Undead in holy water!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.5D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.SPRING, EssenceType.WATER, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.SPRING, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.SPRING, EssenceType.WATER, rem, tier
                        );
                    }
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 0.15D, zone.center.z, 5, 0.7D, 0.1D, 0.7D, 0.02D);
                        srv.sendParticles(ParticleTypes.CHERRY_LEAVES, zone.center.x, zone.center.y + 0.25D, zone.center.z, 2, 0.6D, 0.1D, 0.6D, 0.01D);
                    }

                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    AABB trigger = new AABB(
                            zone.center.x - 2.3D, zone.center.y - 1.0D, zone.center.z - 2.3D,
                            zone.center.x + 2.3D, zone.center.y + 2.0D, zone.center.z + 2.3D
                    );
                    List<LivingEntity> steppers = srv.getEntitiesOfClass(LivingEntity.class, trigger, LivingEntity::isAlive);
                    if (steppers.isEmpty()) return true;

                    // Mode A: Friendly Ally Step -> Restorative Spring!
                    for (LivingEntity st : steppers) {
                        if (owner != null && isFriendlyTarget(st, owner)) {
                            // Only consume on caster themselves if injured or carrying a debuff so planting at feet doesn't waste it instantly
                            if (st == owner && st.getHealth() >= st.getMaxHealth() - 0.5F
                                    && st.getActiveEffects().stream().allMatch(e -> e.getEffect().value().isBeneficial())) {
                                continue;
                            }
                            SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 3.5D, EssenceType.SPRING, EssenceType.WATER, tier);
                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(st);
                            SpellCombatReactionEngine.notifyMagicalHeal(st, 10.0F);
                            st.heal(10.0F); // 5 full hearts!
                            st.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
                            srv.sendParticles(ParticleTypes.HEART, st.getX(), st.getY(1.0D), st.getZ(), 8, 0.35D, 0.4D, 0.35D, 0.03D);
                            srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.25F);
                            triggersLeft[0]--;
                            rearmCooldown[0] = 95;
                            return triggersLeft[0] > 0;
                        }
                    }

                    // Mode B: Hostile Step -> 4.0s (80t) 4m Geyser Chasm!
                    List<LivingEntity> foes = steppers.stream()
                            .filter(e -> (owner == null || !isFriendlyTarget(e, owner))
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)))
                            .toList();
                    if (foes.isEmpty()) return true;

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 4.0D, EssenceType.SPRING, EssenceType.WATER, tier);
                    SpellWorldVfxManager.broadcastArcaneSeal(srv, zone.center.add(0.0D, 0.04D, 0.0D), 4.0D, EssenceType.SPRING, EssenceType.WATER, 80, tier);

                    for (LivingEntity foe : foes) {
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 3.0D, 0.0D, 160L);
                        foe.setDeltaMovement(0.0D, 0.95D, 0.0D);
                        foe.hasImpulse = true;
                        boolean isUndead = foe.getType().is(EntityTypeTags.UNDEAD);
                        if (isUndead) {
                            SpellLingeringZoneManager.applyAbyssLandDrowning(foe, 80);
                        }
                        float geyserDmg = (4.10F + tier * 1.05F) * Math.max(0.5F, zone.apparatusPotency) * (isUndead ? 1.50F : 1.0F);
                        SpellCombatReactionEngine.hurtWithSpringMateriaAdvantage(srv, foe, owner, geyserDmg, false, true);
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, foe, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                            );
                        }
                    }

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.0D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                if (sEl > 0 && sEl % 20 == 0) {
                                    LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                    boolean sSafe = sZone.spell != null && sZone.spell.hasWardFriendlyFireSafeguard();
                                    int sTier = sZone.spell != null ? sZone.spell.getEffectiveTier() : tier;
                                    AABB chasm = new AABB(
                                            sZone.center.x - 4.0D, sZone.center.y - 2.0D, sZone.center.z - 4.0D,
                                            sZone.center.x + 4.0D, sZone.center.y + 3.5D, sZone.center.z + 4.0D
                                    );
                                    for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, chasm, LivingEntity::isAlive)) {
                                        if (sOwner != null && isFriendlyTarget(v, sOwner)) continue;
                                        if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sSafe)) continue;
                                        v.setDeltaMovement(v.getDeltaMovement().x * 0.4D, 0.52D, v.getDeltaMovement().z * 0.4D);
                                        v.hasImpulse = true;
                                        boolean isUnd = v.getType().is(EntityTypeTags.UNDEAD);
                                        if (isUnd) {
                                            SpellLingeringZoneManager.applyAbyssLandDrowning(v, 40);
                                        }
                                        SpellCombatReactionEngine.hurtWithSpringMateriaAdvantage(
                                                sLvl, v, sOwner, (2.40F + sTier * 0.62F) * (isUnd ? 1.50F : 1.0F), false, true
                                        );
                                    }
                                }
                                if (sEl % 5 == 0) {
                                    sLvl.sendParticles(ParticleTypes.SPLASH, sZone.center.x, sZone.center.y + 0.5D, sZone.center.z, 20, 1.5D, 0.6D, 1.5D, 0.10D);
                                    sLvl.sendParticles(ParticleTypes.CHERRY_LEAVES, sZone.center.x, sZone.center.y + 0.6D, sZone.center.z, 6, 1.4D, 0.5D, 1.4D, 0.03D);
                                }
                                return true;
                            }
                    ));

                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 0.92F);
                    triggersLeft[0]--;
                    rearmCooldown[0] = 95;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 7 — VERNAL REJUVENATION TORRENT CONE (CONE / SPRAY)
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
        // Section 6.43 Vernal Rejuvenation Torrent Cone:
        // Drenches allies (1.5 hearts / 3.0 HP heal & extinguishes fire);
        // On hostiles -> continuous Spring Materia damage, smites Undead, 3m knockback, & Vernal Intoxication
        // (-50% attack speed & 25.0s / 500t AI pathfinding disruption)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), dirNorm, coneRange,
                EssenceType.SPRING, EssenceType.WATER, tier
        );

        float coneDmg = (2.95F + tier * 0.75F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (isFriendlyTarget(hit, caster)) {
                hit.clearFire();
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 3.0F);
                hit.heal(3.0F); // 1.5 hearts!
                level.sendParticles(ParticleTypes.HEART, hit.getX(), hit.getY(1.0D), hit.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.02D);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(dirNorm.x * 0.85D, 0.22D, dirNorm.z * 0.85D); // ~3m knockback!
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));
            SpellCombatReactionEngine.applyTimedAttackDebuff(hit, level, 0.0D, 0.50D, 100L);
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500); // 25.0s AI pathfinding disruption!
            boolean isUndead = hit.getType().is(EntityTypeTags.UNDEAD);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithSpringMateriaAdvantage(
                        level, hit, caster, coneDmg * (isUndead ? 1.50F : 1.0F), false, true
                );
            }
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.88F, 1.22F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 8 — VERNAL MIRACLE DOWNPOUR (SKYFALL)
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
        // Section 6.43 Vernal Miracle Downpour (Extended 18s Duration, Tier 3 Trinity Power):
        // 18.0s (360t) skyfall of radiant springwater torrents & giant lotus/cherry petals across 12m+ radius
        // + 8.0s (160t) verdant sanctuary (520t total); pounds hostiles with Spring Materia damage, pins downward,
        // strips 5 armor & smites Undead; heals friendly players & summons 1 heart/s, satisfies hunger, grants
        // Regeneration II & Resistance I across all 26.0s, and rapidly matures farmland crops & saplings!
        int tier = spell.getEffectiveTier();
        double r = Math.max(12.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.SPRING, EssenceType.WATER, 365, tier
        );

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, EssenceType.SPRING, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean stormActive = elapsed <= 360;
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (elapsed == 361) {
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.04D, 0.0D), zone.radius,
                                EssenceType.SPRING, EssenceType.WATER, 160, zTier
                        );
                    }
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.CHERRY_LEAVES, zone.center.x, zone.center.y + (stormActive ? 3.8D : 0.4D), zone.center.z,
                                22, zone.radius * 0.55D, stormActive ? 1.8D : 0.2D, zone.radius * 0.55D, 0.04D);
                        if (stormActive) {
                            srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 1.2D, zone.center.z,
                                    26, zone.radius * 0.55D, 1.0D, zone.radius * 0.55D, 0.08D);
                        }
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float downpourDmg = (4.20F + zTier * 1.08F) * Math.max(0.5F, zone.apparatusPotency);

                        if (stormActive) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = srv.random.nextDouble() * (zone.radius * 0.72D);
                            Vec3 torrentPos = zone.center.add(Math.cos(ang) * dist, 0.2D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, torrentPos, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.SPRING, EssenceType.WATER, 2.25F, zTier
                            );
                        }

                        matureCropsInRadius(srv, BlockPos.containing(zone.center), 6);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (owner != null && isFriendlyTarget(ent, owner)) {
                                SpellCombatReactionEngine.notifyMagicalHeal(ent, 2.0F);
                                ent.heal(2.0F); // 1 heart per second!
                                ent.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 45, 1));
                                ent.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 45, 0));
                                if (ent instanceof Player p) {
                                    p.getFoodData().eat(1, 0.35F);
                                }
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, wardSafe)) continue;

                            ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 2));
                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 5.0D, 0.0D, 160L);
                                ent.setDeltaMovement(ent.getDeltaMovement().x * 0.3D, -0.36D, ent.getDeltaMovement().z * 0.3D);
                                ent.hasImpulse = true;
                                boolean isUndead = ent.getType().is(EntityTypeTags.UNDEAD);
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, ent, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F
                                    );
                                }
                                SpellCombatReactionEngine.hurtWithSpringMateriaAdvantage(
                                        srv, ent, owner, downpourDmg * (isUndead ? 1.50F : 1.0F), false, true
                                );
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 9 — VERNAL SPRING APEX EIDOLON SUITE (SUMMON / EIDOLON)
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
        tag.putString("SpellEidolonMateria", EssenceType.SPRING.name());
        tag.putInt("SpellEidolonTintRGB", VERNAL_SPRING_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.5D, radius), EssenceType.SPRING, EssenceType.WATER, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 26, 0.45D, 0.55D, 0.45D, 0.04D);
        level.sendParticles(ParticleTypes.SPLASH, spawnPos.x, spawnPos.y + 0.6D, spawnPos.z, 18, 0.4D, 0.4D, 0.4D, 0.05D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.43 Petal-Warp Blink:
        // Dissolves into fragrant cherry blossom petals & re-materializes up to 14m away (#FF6B9D / #64C8FF),
        // completely shedding all hostile mob aggro and rendering caster undetectable to Undead for 4.0s (80t)!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.SPRING, potencyTier);

        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, arrivalPos).inflate(18.0D), Mob::isAlive)) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
        }
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SPRING_UNDEAD_STEALTH_UNTIL, level.getGameTime() + 80L);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, departurePos.x, departurePos.y + 0.8D, departurePos.z, 24, 0.35D, 0.45D, 0.35D, 0.03D);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 24, 0.35D, 0.45D, 0.35D, 0.03D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.CHERRY_WOOD_STEP, SoundSource.PLAYERS, 0.95F, 1.35F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.43 Vernal Purification & Greening Transmutation:
        // Cures Netherrack/Soul Sand/Sculk -> Grass/Moss/Farmland; hydrates Farmland across 6m; cures Zombie Villagers
        // into regular Villagers; Rotten Flesh -> Leather or Beef/Pork; matures crops/saplings; purifies toxic fluids!
        boolean didAny = cureNearbyZombieVillagers(level, pos);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D), ItemEntity::isAlive)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-6, -1, -6), pos.offset(6, 1, 6))) {
            BlockState fs = level.getBlockState(p);
            if (fs.is(Blocks.FARMLAND) && fs.getValue(FarmBlock.MOISTURE) < 7) {
                level.setBlockAndUpdate(p, fs.setValue(FarmBlock.MOISTURE, 7));
                didAny = true;
            }
        }
        matureCropsInRadius(level, pos, 3);

        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            if (didAny) refundBatchDiscount(level, caster, potencyTier);
            return didAny;
        }
        if (state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL) || state.is(Blocks.SCULK)) {
            int pick = level.random.nextInt(3);
            BlockState out = switch (pick) {
                case 0 -> Blocks.GRASS_BLOCK.defaultBlockState();
                case 1 -> Blocks.MOSS_BLOCK.defaultBlockState();
                default -> Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);
            };
            level.setBlockAndUpdate(pos, out);
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.LAVA) || state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            level.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 12, 0.3D, 0.2D, 0.3D, 0.04D);
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.WITHER_ROSE)) {
            level.setBlockAndUpdate(pos, Blocks.PINK_TULIP.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (didAny) {
            refundBatchDiscount(level, caster, potencyTier);
        }
        return didAny;
    }

    public static boolean cureNearbyZombieVillagers(ServerLevel level, BlockPos pos) {
        boolean cured = false;
        for (ZombieVillager zv : level.getEntitiesOfClass(ZombieVillager.class, new AABB(pos).inflate(3.5D), LivingEntity::isAlive)) {
            Villager villager = EntityType.VILLAGER.create(level, EntitySpawnReason.CONVERSION);
            if (villager != null) {
                villager.setPos(zv.getX(), zv.getY(), zv.getZ());
                villager.setYRot(zv.getYRot());
                villager.setXRot(zv.getXRot());
                villager.setVillagerData(zv.getVillagerData());
                if (zv.isBaby()) villager.setBaby(true);
                level.addFreshEntity(villager);
                zv.discard();
                level.sendParticles(ParticleTypes.CHERRY_LEAVES, villager.getX(), villager.getY(1.0D), villager.getZ(), 18, 0.4D, 0.5D, 0.4D, 0.03D);
                level.playSound(null, pos, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 1.0F, 1.0F);
                cured = true;
            }
        }
        return cured;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(Items.ROTTEN_FLESH)) {
            int pick = level.random.nextInt(3);
            ItemStack out = switch (pick) {
                case 0 -> new ItemStack(Items.LEATHER, stack.getCount());
                case 1 -> new ItemStack(Items.BEEF, stack.getCount());
                default -> new ItemStack(Items.PORKCHOP, stack.getCount());
            };
            itemEntity.setItem(out);
            return true;
        }
        if (stack.is(Items.POISONOUS_POTATO)) {
            itemEntity.setItem(new ItemStack(Items.POTATO, stack.getCount()));
            return true;
        }
        if (stack.is(Items.WITHER_ROSE)) {
            itemEntity.setItem(new ItemStack(Items.PINK_TULIP, stack.getCount()));
            return true;
        }
        if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            boolean hasHarmful = false;
            if (contents != null) {
                for (MobEffectInstance eff : contents.getAllEffects()) {
                    if (!eff.getEffect().value().isBeneficial()) {
                        hasHarmful = true;
                        break;
                    }
                }
            }
            if (hasHarmful) {
                ItemStack pureWater = new ItemStack(Items.POTION, stack.getCount());
                pureWater.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
                itemEntity.setItem(pureWater);
                return true;
            }
        }
        if (stack.is(Items.NETHERRACK) || stack.is(Items.SOUL_SAND) || stack.is(Items.SCULK)) {
            itemEntity.setItem(new ItemStack(Items.MOSS_BLOCK, stack.getCount()));
            return true;
        }
        return false;
    }

    private static void refundBatchDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (caster instanceof Player player) {
            CompoundTag pData = EntityHelper.getPersistentData(player);
            long now = level.getGameTime();
            if (pData.getLong(TAG_SPRING_TRANSMUTE_BATCH_TICK).orElse(-10L) != now) {
                pData.putLong(TAG_SPRING_TRANSMUTE_BATCH_TICK, now);
                long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
                SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.SPRING, refund);
            }
        }
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.43 Uncontrolled Vernal Overgrowth Hex:
        // Rooting & stunning 1.0s (20t) every 4.0s, -50% movement speed, -40% attack speed, -35% armor toughness,
        // continuous biological parasite Spring Materia damage (extra damage to Undead), and drops collectable
        // 1-heart (2.0 HP) healing dew-orbs for allies!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, Math.min(0.80D, 0.50D * mult), 45L);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 2, true, true));

        LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        if ((now / 20L) % 4L == 0L) {
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
            SpellLingeringZoneManager.spawnCollectableHealingOrb(level, target.position(), caster, 2.0F, false);
        }

        float smiteMult = target.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F;
        float hexDmg = (1.50F + potencyTier * 0.50F) * mult * smiteMult;
        SpellCombatReactionEngine.hurtWithSpringMateriaAdvantage(level, target, caster, hexDmg, false, true);

        level.sendParticles(ParticleTypes.CHERRY_LEAVES, target.getX(), target.getY(0.85D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }

    private static void placeTemporaryVernalFlora(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            int halfSpan,
            int durationTicks
    ) {
        BlockPos cp = BlockPos.containing(center);
        Map<BlockPos, BlockState> placed = new HashMap<>();
        for (BlockPos p : BlockPos.betweenClosed(cp.offset(-halfSpan, -1, -halfSpan), cp.offset(halfSpan, 1, halfSpan))) {
            BlockState cur = level.getBlockState(p);
            BlockState below = level.getBlockState(p.below());
            if (cur.isAir()
                    && (below.is(BlockTags.DIRT) || below.is(Blocks.MOSS_BLOCK))
                    && level.random.nextFloat() < 0.55F
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, cur)) {
                Block flora = VERNAL_FLORA_BLOCKS[level.random.nextInt(VERNAL_FLORA_BLOCKS.length)];
                BlockState floraState = flora.defaultBlockState();
                if (floraState.canSurvive(level, p)) {
                    placed.put(p.immutable(), cur);
                    level.setBlockAndUpdate(p, floraState);
                }
            }
        }
        if (!placed.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, center, halfSpan + 1.0D, durationTicks, level.getGameTime(), null, null, EssenceType.SPRING, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        for (Map.Entry<BlockPos, BlockState> e : placed.entrySet()) {
                            Block curBlock = srv.getBlockState(e.getKey()).getBlock();
                            for (Block f : VERNAL_FLORA_BLOCKS) {
                                if (curBlock == f) {
                                    srv.setBlockAndUpdate(e.getKey(), e.getValue());
                                    break;
                                }
                            }
                        }
                        return false;
                    }
            ));
        }
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
