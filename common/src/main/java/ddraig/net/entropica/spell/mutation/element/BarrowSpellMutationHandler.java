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
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Barrow Materia (Ethereal &amp; Paradox Fusion #38)</b>
 * (Spellcrafting System Guide Section 6.39 — Dual Earth {@code #773214} + Undead {@code #4D6453} [Leans: Undead]).
 */
public final class BarrowSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Granite Brown (#773214) primary tint. */
    public static final int GRAVE_WARDEN_HEX_COLOR = 0x773214;

    /** Canonical Necrotic Moss / Tombstone Lichen (#4D6453) secondary tint. */
    public static final int BARROW_MOSS_HEX_COLOR = 0x4D6453;

    private static final String TAG_BARROW_BEAM_LAST_TICK = "SpellBarrowBeamLastTick";
    private static final String TAG_BARROW_BEAM_TICKS = "SpellBarrowBeamTicks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.BARROW;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Ossuary Bone-Shield Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Tombstone Ossuary Boulder-Slug";
            case BEAM -> "Barrow-Tomb Necrotic Tether Beam";
            case RADIAL_BURST -> "Catacomb Miasma Eruption";
            case TOUCH_STRIKE -> "Barrow-Cairn Tombstone Slam";
            case AURA -> "Catacomb Miasma Shroud";
            case GLYPH -> "Cairn Collapse Mine";
            case CONE -> "Ossuary Bone-Grit & Tomb-Dust Deluge";
            case SKYFALL -> "Megalithic Ossuary Cataclysm Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Subterranean Crypt-Tunnel Step";
            case TRANSMUTE, BREAK -> "Megalithic Grave Transmute";
            case AFFLICTION -> "Petrifying Sepulchral Entombment Hex";
            default -> "Sepulchral Barrow " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return "Circling ring of petrified bone plates and tomb slabs (#773214 / #4D6453) absorbing 25% of physical damage and retaliating with bone shrapnel against melee attackers.";
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Hurls a heavy lichen-crusted megalithic gravestone slab (#773214 / #4D6453) dealing Barrow Materia damage, knocking targets 6 blocks back, rooting them in grave mud for 2.5s, and burying feet in a 2-block coarse burial earth patch.";
            case BEAM -> "Channels grasping skeletal roots and churning grave dirt (#773214 / #4D6453), dragging mobs downward (-60% speed) and siphoning -1 armor/s; 3.0s channeling triggers a Sepulchral Entombment stone sarcophagus for 2.0s with heavy crush damage.";
            case RADIAL_BURST -> "Detonates a 6-block cloud of toxic burial gases and petrified bone shards (#773214 / #4D6453), inflicting Wither, Poison II, and 5.0s Blindness, dealing Barrow Materia damage, healing allied undead/summons, and granting +4 Armor.";
            case TOUCH_STRIKE -> "Slams the target with a summoned megalithic headstone (#773214 / #4D6453) dealing crushing Barrow Materia damage, knocking 6 blocks back, stunning flat for 1.5s, inflicting 3.0s Slowness IV, stripping 3 armor, and granting +3 Armor for 5.0s.";
            case AURA -> "Surrounds the caster in dense ancient tomb fog (#773214 / #4D6453) reducing mob detection range by 40%, inflicting Wither and Slowness on hostiles within 4 blocks, granting complete poison and wither immunity, and Night Vision.";
            case GLYPH -> "Inscribes a disarmable pile of cursed megalithic stones (#773214 / #4D6453) that violently collapses into an explosive pitfall dealing massive Barrow Materia damage, stripping 3 armor points, and launching mobs 6 blocks airborne.";
            case CONE -> "Sprays a 60-degree cone of petrified bone shrapnel and tomb dust (#773214 / #4D6453), knocking foes 6 blocks back, shredding 3 armor points, inflicting 3.5s Blindness and Slowness III, smothering torches/fires, and turning dirt into coarse dirt or gravel.";
            case SKYFALL -> "Calls down a 6-second bombardment of massive lichen-covered tomb megaliths across 10+ blocks (#773214 / #4D6453), knocking mobs flat, shredding 2 armor per hit, and leaving temporary petrified stone markers and coarse grave dirt.";
            case SUMMON_AGENT -> "Summons an armored Barrow Skeleton (#773214 / #4D6453) wielding a heavy petrified tower shield and stone flail that taunts and stuns enemies.";
            case SUMMON_SENTRY -> "Anchors a Cairn Beacon (#773214 / #4D6453) pulsing necrotic burial dust across 8 blocks to slow enemies by 40% and grant allied undead/summons +4 Armor.";
            case SUMMON_DECOY -> "Manifests a Hollow Bone Effigy (#773214 / #4D6453) that taunts hostiles and shatters into petrified bone shrapnel upon destruction.";
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Sinks into the earth and re-emerges up to 14 blocks away, raising a 1-block taunting burial mound at departure and granting +4 Armor and 50% knockback resistance for 4.0s upon arrival.";
            case TRANSMUTE, BREAK -> "Ancient subterranean petrification converting dirt, grass, and mud into coarse dirt, mossy cobblestone, or cracked stone bricks; rotten flesh/raw meat into bone blocks; cobblestone into mossy cobblestone; and wood into petrified wood.";
            case AFFLICTION -> "Persistent milk-immune burial curse cutting move and attack speed by 40%, blocking jumping, shredding 40% armor toughness, boosting incoming physical/blunt damage by +35%, and awakening Catacomb skeletons until cleansed by Vitae/Restoration or Radiant light.";
            default -> null;
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — OSSUARY BONE-SHIELD ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SHIELD.get(), GRAVE_WARDEN_HEX_COLOR, 14, 0.72F, 0.03F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
        );
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), BARROW_MOSS_HEX_COLOR, 16, 0.68F, 0.04F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
            );
        }
        if (flow == SpellFlowModifier.ORBIT_SATELLITE && projectile.getOwner() instanceof LivingEntity owner && owner.isAlive()) {
            long now = level.getGameTime();
            CompoundTag data = EntityHelper.getPersistentData(owner);
            if (data.getLong(SpellLingeringZoneManager.TAG_BARROW_ORBIT_UNTIL).orElse(0L) < now + 30L) {
                data.putLong(SpellLingeringZoneManager.TAG_BARROW_ORBIT_UNTIL, now + 35L);
            }
        }
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (ALL 9 FORMS)
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CRACKED_STONE_BRICKS.defaultBlockState()),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.03D
        );
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
        // Section 6.39 Tombstone Ossuary Boulder-Slug:
        // Detonates into jagged tombstone shrapnel & cursed grave dirt, dealing Barrow Materia damage
        // (+1 vs Frozen, +1 vs Amber, +1 vs Void), knocking targets 6 blocks backward, rooting them in grave mud
        // for 2.5s (50t), and burying their feet in a 2-block patch of coarse burial earth!
        if (hitEntity instanceof LivingEntity victim && victim.isAlive()) {
            boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
            if (caster == null || (!SpellExecutionEngine.isFriendlyAllyForOasis(caster, victim)
                    && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, wardSafe))) {
                int tier = spell.getEffectiveTier();
                Vec3 vel = projectile.getDeltaMovement();
                Vec3 kb = vel.lengthSqr() > 1.0E-4D ? vel.normalize().scale(1.48D) : new Vec3(0.0D, 0.36D, 0.0D);
                victim.setDeltaMovement(kb.x, 0.36D, kb.z);
                victim.hasImpulse = true;
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                victim.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                EntityHelper.getPersistentData(victim).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 50L);

                if (!slot.hasHarmVerb()) {
                    float boulderDmg = 2.95F + tier * 0.74F;
                    SpellCombatReactionEngine.hurtWithBarrowMateriaAdvantage(level, victim, caster, boulderDmg, false, true);
                }

                SpellWorldVfxManager.broadcastProjectileImpact(
                        level, victim.position().add(0.0D, victim.getBbHeight() * 0.55D, 0.0D),
                        new Vec3(0.0D, 1.0D, 0.0D), EssenceType.BARROW, EssenceType.UNDEAD, 1.35F, tier
                );
            }
        }
        spreadBurialEarthPatch(level, caster, hitEntity.position(), spell);
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
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, blockHit.getLocation(), Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i()),
                EssenceType.BARROW, EssenceType.UNDEAD, 1.25F, spell.getEffectiveTier()
        );
        spreadBurialEarthPatch(level, caster, blockHit.getLocation(), spell);
    }

    private static void spreadBurialEarthPatch(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 hitPos,
            SpellAssembly spell
    ) {
        BlockPos center = BlockPos.containing(hitPos);
        Map<BlockPos, BlockState> saved = new HashMap<>();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 0, 1))) {
            if (Math.abs(p.getX() - center.getX()) + Math.abs(p.getZ() - center.getZ()) > 2) continue;
            BlockState gs = level.getBlockState(p);
            if ((gs.is(BlockTags.DIRT) || gs.is(Blocks.STONE) || gs.is(Blocks.COBBLESTONE)) && !gs.is(Blocks.COARSE_DIRT)
                    && level.getBlockState(p.above()).isAir()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, gs)) {
                saved.put(p.immutable(), gs);
                level.setBlock(p, Blocks.COARSE_DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CRACKED_STONE_BRICKS.defaultBlockState()),
                hitPos.x, hitPos.y + 0.4D, hitPos.z, 22, 0.9D, 0.4D, 0.9D, 0.08D
        );
        level.playSound(null, center, SoundEvents.DEEPSLATE_BRICKS_BREAK, SoundSource.PLAYERS, 1.0F, 0.75F);
        if (!saved.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, hitPos, 2.0D, 100, level.getGameTime(), spell, null, EssenceType.BARROW, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        for (Map.Entry<BlockPos, BlockState> e : saved.entrySet()) {
                            if (srv.getBlockState(e.getKey()).is(Blocks.COARSE_DIRT)) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        return false;
                    }
            ));
        }
    }

    // =========================================================================
    // FORM MUTATION 2 — BARROW-TOMB NECROTIC TETHER BEAM (BEAM)
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
        // Section 6.39 Barrow-Tomb Necrotic Tether Beam:
        // Drags caught mobs downward into the floor, reducing movement speed by 60% and continuously siphoning
        // armor points (-1 armor/s); sustained channeling for 3.0s (60 ticks) triggers Sepulchral Entombment
        // encasing the target in a temporary 2-block stone sarcophagus for 2.0s (40t), stunning completely & crushing!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(hit.getDeltaMovement().x * 0.35D, Math.min(-0.24D, hit.getDeltaMovement().y), hit.getDeltaMovement().z * 0.35D);
            hit.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.60D, 45L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 3));

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_BARROW_BEAM_LAST_TICK).orElse(-100L);
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_BARROW_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_BARROW_BEAM_LAST_TICK, now);
            data.putInt(TAG_BARROW_BEAM_TICKS, contactTicks);

            double armorSiphon = Math.min(6.0D, Math.max(1.0D, contactTicks / 20.0D)); // -1 armor/s!
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, armorSiphon, 0.0D, 90L);

            if (!slot.hasHarmVerb()) {
                float beamDmg = (1.32F + tier * 0.36F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithBarrowMateriaAdvantage(level, hit, caster, beamDmg, false, true);
            }

            if (contactTicks >= 60) {
                data.putInt(TAG_BARROW_BEAM_TICKS, 0);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));

                float crushDmg = (6.20F + tier * 1.75F) * Math.max(0.5F, apparatusPotency);
                hit.hurtServer(level, level.damageSources().inWall(), 2.0F + tier * 0.45F);
                SpellCombatReactionEngine.hurtWithBarrowMateriaAdvantage(level, hit, caster, crushDmg, false, true);
                entombTargetInSarcophagus(level, caster, hit, spell);
            }
        }
    }

    private static void entombTargetInSarcophagus(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity victim,
            SpellAssembly spell
    ) {
        BlockPos base = victim.blockPosition();
        Map<BlockPos, BlockState> saved = new HashMap<>();
        BlockState curBase = level.getBlockState(base);
        if (curBase.isAir() && !SpellExecutionEngine.isProtectedBlockOrContainer(level, base, curBase)) {
            saved.put(base.immutable(), curBase);
            level.setBlock(base, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        BlockPos top = base.above();
        BlockState curTop = level.getBlockState(top);
        if (curTop.isAir() && !SpellExecutionEngine.isProtectedBlockOrContainer(level, top, curTop)) {
            saved.put(top.immutable(), curTop);
            level.setBlock(top, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        level.playSound(null, base, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.72F, 0.65F);
        if (!saved.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, Vec3.atCenterOf(base), 1.5D, 40, level.getGameTime(), spell, null, EssenceType.BARROW, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        for (Map.Entry<BlockPos, BlockState> e : saved.entrySet()) {
                            BlockState bs = srv.getBlockState(e.getKey());
                            if (bs.is(Blocks.MOSSY_STONE_BRICKS) || bs.is(Blocks.CRACKED_STONE_BRICKS)) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        srv.sendParticles(
                                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CRACKED_STONE_BRICKS.defaultBlockState()),
                                zone.center.x, zone.center.y + 0.8D, zone.center.z, 20, 0.4D, 0.5D, 0.4D, 0.08D
                        );
                        return false;
                    }
            ));
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — CATACOMB MIASMA ERUPTION (RADIAL BURST)
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
        // Section 6.39 Catacomb Miasma Eruption:
        // 360° cloud of toxic burial gases & jagged petrified bone shards (6m+ radius, #773214 / #4D6453);
        // poisons living mobs with Wither & Poison II, blinds them for 5.0s (100t), deals Barrow Materia damage,
        // heals allied undead/summons, and bolsters the caster with +4 Armor!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.BARROW, EssenceType.UNDEAD, tier);

        float eruptionDmg = (3.05F + tier * 0.78F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);

        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ent == caster) continue;
            if ((ent instanceof SpellEidolonEntity eid && caster.getUUID().equals(eid.getOwnerUuid()))
                    || (SpellExecutionEngine.isFriendlyAllyForOasis(caster, ent) && ent.getType().is(EntityTypeTags.UNDEAD))) {
                float allyHeal = 7.0F + tier * 1.1F;
                SpellCombatReactionEngine.notifyMagicalHeal(ent, allyHeal);
                ent.heal(allyHeal);
                continue;
            }
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, ent)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) continue;

            ent.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
            ent.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            ent.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithBarrowMateriaAdvantage(level, ent, caster, eruptionDmg, false, true);
            }
        }

        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.0D, 0.0D, 160L);
        level.sendParticles(ParticleTypes.ASH, center.x, center.y + 0.9D, center.z, 40, 2.3D, 0.6D, 2.3D, 0.05D);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()),
                center.x, center.y + 0.8D, center.z, 26, 2.0D, 0.5D, 2.0D, 0.08D
        );
        level.playSound(null, BlockPos.containing(center), SoundEvents.DEEPSLATE_BRICKS_BREAK, SoundSource.PLAYERS, 1.1F, 0.72F);
    }

    // =========================================================================
    // FORM MUTATION 4 — BARROW-CAIRN TOMBSTONE SLAM (TOUCH / STRIKE)
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
        // Section 6.39 Barrow-Cairn Tombstone Slam:
        // Slams the target with a summoned megalithic headstone (#773214 / #4D6453); deals crushing Barrow Materia
        // damage, knocks the victim 6 blocks backward, stuns them flat for 1.5s (30t), buries legs in grave mud
        // (Slowness IV for 3.0s / 60t), strips 3 armor points, and bolsters the caster with +3 Armor for 5.0s (100t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), push, 2.8D,
                EssenceType.BARROW, EssenceType.UNDEAD, tier
        );

        float slamDmg = (3.20F + tier * 0.82F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(push.x * 1.48D, 0.36D, push.z * 1.48D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithBarrowMateriaAdvantage(level, hit, caster, slamDmg, true, true);
            }

            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSSY_STONE_BRICKS.defaultBlockState()),
                    hit.getX(), hit.getY(0.8D), hit.getZ(), 20, 0.35D, 0.4D, 0.35D, 0.08D
            );
        }

        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 3.0D, 0.0D, 0.0D, 0.0D, 100L);
        level.playSound(null, caster.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.78F, 0.72F);
    }

    // =========================================================================
    // FORM MUTATION 5 — CATACOMB MIASMA SHROUD (AURA / SELF)
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
        // Section 6.39 Catacomb Miasma Shroud:
        // Reduces mob detection range by 40%, inflicts continuous Wither & Slowness on nearby hostiles within 4m,
        // provides complete poison & wither immunity, and grants permanent Night Vision!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(
                SpellLingeringZoneManager.TAG_BARROW_SHROUD_UNTIL, level.getGameTime() + dur
        );
        caster.removeEffect(MobEffects.POISON);
        caster.removeEffect(MobEffects.WITHER);
        caster.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, dur + 40, 0, false, false));
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.50F, null);
        level.sendParticles(ParticleTypes.ASH, caster.getX(), caster.getY(0.8D), caster.getZ(), 24, 0.5D, 0.55D, 0.5D, 0.03D);
    }

    // =========================================================================
    // FORM MUTATION 6 — CAIRN COLLAPSE MINE (GLYPH / TRAP)
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
        // Section 6.39 Cairn Collapse Mine:
        // Disarmable Single/Repeating pile of cursed megalithic stones (#773214 / #4D6453, 1.0s arming delay) that
        // violently collapses into an explosive pitfall when triggered, dealing massive area Barrow Materia damage,
        // stripping 3 armor points, and launching caught mobs 6 blocks airborne!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.2D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.BARROW, EssenceType.UNDEAD, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.BARROW, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.BARROW, EssenceType.UNDEAD, rem, tier
                        );
                    }
                    if (elapsed % 12 == 0) {
                        srv.sendParticles(
                                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.COARSE_DIRT.defaultBlockState()),
                                zone.center.x, zone.center.y + 0.15D, zone.center.z, 4, 0.75D, 0.1D, 0.75D, 0.02D
                        );
                    }

                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    AABB trigger = new AABB(
                            zone.center.x - 2.4D, zone.center.y - 1.0D, zone.center.z - 2.4D,
                            zone.center.x + 2.4D, zone.center.y + 2.0D, zone.center.z + 2.4D
                    );
                    List<LivingEntity> foes = srv.getEntitiesOfClass(LivingEntity.class, trigger, e ->
                            e.isAlive() && e != owner
                                    && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    if (foes.isEmpty()) return true;

                    SpellWorldVfxManager.broadcastRadialNova(
                            srv, zone.center, 3.8D, EssenceType.BARROW, EssenceType.UNDEAD, tier
                    );

                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                    float mineDmg = (4.80F + zTier * 1.25F) * Math.max(0.5F, zone.apparatusPotency);
                    AABB blast = new AABB(
                            zone.center.x - 3.8D, zone.center.y - 1.5D, zone.center.z - 3.8D,
                            zone.center.x + 3.8D, zone.center.y + 3.5D, zone.center.z + 3.8D
                    );
                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, blast, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                        SpellCombatReactionEngine.applyTimedAttributeShred(v, srv, 3.0D, 0.0D, 160L);
                        SpellCombatReactionEngine.hurtWithBarrowMateriaAdvantage(srv, v, owner, mineDmg, false, true);
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 1.15F
                            );
                        }
                        v.setDeltaMovement(v.getDeltaMovement().x * 0.4D, 1.02D, v.getDeltaMovement().z * 0.4D); // ~6m airborne launch!
                        v.hasImpulse = true;
                    }

                    srv.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                            zone.center.x, zone.center.y + 0.5D, zone.center.z, 34, 1.3D, 0.5D, 1.3D, 0.12D
                    );
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.80F);
                    triggersLeft[0]--;
                    rearmCooldown[0] = 85;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — OSSUARY BONE-GRIT & TOMB-DUST DELUGE (CONE / SPRAY)
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
        // Section 6.39 Ossuary Bone-Grit & Tomb-Dust Deluge:
        // Wide 60° cone of jagged petrified bone shrapnel, coarse grave dirt, & suffocating tomb dust (#773214 / #4D6453);
        // knocks enemies 6 blocks back, shreds 3 physical armor points, inflicts Blindness & Slowness III for 3.5s (70t),
        // smothers torches and surface fires, and turns dirt into coarse dirt or gravel!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), push, coneRange,
                EssenceType.BARROW, EssenceType.UNDEAD, tier
        );

        float coneDmg = (2.95F + tier * 0.74F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(push.x * 1.48D, 0.35D, push.z * 1.48D);
            hit.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 2));

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithBarrowMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }
        }

        Vec3 eye = caster.getEyePosition();
        for (double d = 1.5D; d <= coneRange; d += 1.5D) {
            BlockPos step = BlockPos.containing(eye.add(push.scale(d)));
            for (BlockPos p : BlockPos.betweenClosed(step.offset(-1, -2, -1), step.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
                if (bs.is(Blocks.TORCH) || bs.is(Blocks.WALL_TORCH) || bs.is(Blocks.SOUL_TORCH) || bs.is(Blocks.SOUL_WALL_TORCH)) {
                    level.destroyBlock(p, true);
                } else if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                } else if (bs.getBlock() instanceof CampfireBlock && bs.hasProperty(CampfireBlock.LIT) && bs.getValue(CampfireBlock.LIT)) {
                    level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
                } else if ((bs.is(Blocks.GRASS_BLOCK) || bs.is(Blocks.DIRT)) && level.getBlockState(p.above()).isAir()) {
                    level.setBlockAndUpdate(p, level.random.nextBoolean()
                            ? Blocks.COARSE_DIRT.defaultBlockState()
                            : Blocks.GRAVEL.defaultBlockState());
                }
            }
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 1.0F, 0.78F);
    }

    // =========================================================================
    // FORM MUTATION 8 — MEGALITHIC OSSUARY CATACLYSM DOWNPOUR (SKYFALL)
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
        // Section 6.39 Megalithic Ossuary Cataclysm Downpour:
        // Calls down a 6-second (120t) bombardment of massive lichen-covered tomb megaliths across a 10-block+ radius
        // (#773214 / #4D6453) using 3D Skyfall Pseudoblock Columns while active, then materializing temporary petrified
        // stone markers & coarse grave dirt once the bombardment finishes!
        int tier = spell.getEffectiveTier();
        double r = Math.max(10.0D, radius);
        BlockPos centerPos = BlockPos.containing(impactCenter);
        double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.BARROW, EssenceType.UNDEAD, 130, tier
        );

        // Pre-plan 6 tomb megalith strike coordinates across the 10m+ zone (1st center, 5 around the ring)
        final BlockPos[] megalithTargets = new BlockPos[6];
        megalithTargets[0] = findGroundSurfaceNear(level, centerPos).above();
        for (int i = 1; i < 6; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / 5.0D) + level.random.nextDouble() * 0.55D;
            double dist = 0.28D * r + level.random.nextDouble() * (0.50D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            megalithTargets[i] = findGroundSurfaceNear(level, raw).above();
        }

        final boolean[] landed = new boolean[6];
        final boolean[] materialized = {false};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, EssenceType.BARROW, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int mIdx = 0; mIdx < 6; mIdx++) {
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(
                                        srv, megalithTargets[mIdx], Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 2
                                );
                            }
                        }
                        return false;
                    }

                    if (elapsed < 120) {
                        // Launch 6 falling 3D Mossy Tomb Megalith pseudoblock columns 14t before impacts at 18, 38, 58, 78, 98, 118!
                        for (int mIdx = 0; mIdx < 6; mIdx++) {
                            int impactTick = 18 + mIdx * 20;
                            BlockPos strikePos = megalithTargets[mIdx];
                            if (elapsed == impactTick - 14) {
                                int holdTicks = Math.max(4, zone.maxDurationTicks - impactTick);
                                SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                                        srv,
                                        new Vec3(strikePos.getX() + 0.5D, strikePos.getY() + skyH, strikePos.getZ() + 0.5D),
                                        strikePos,
                                        Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
                                        2,
                                        14,
                                        holdTicks,
                                        EssenceType.BARROW,
                                        tier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                LivingEntity owner = zone.resolveCaster(srv);
                                boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                                int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                                float slabDmg = (4.35F + zTier * 1.05F) * Math.max(0.5F, zone.apparatusPotency);

                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.BARROW, EssenceType.UNDEAD, 2.20F, zTier
                                );

                                AABB shockBox = new AABB(
                                        hitVec.x - 4.8D, hitVec.y - 2.0D, hitVec.z - 4.8D,
                                        hitVec.x + 4.8D, hitVec.y + 4.0D, hitVec.z + 4.8D
                                );
                                for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, shockBox, LivingEntity::isAlive)) {
                                    if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                                    if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 2.0D, 0.0D, 120L);
                                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
                                    foe.setDeltaMovement(foe.getDeltaMovement().x * 0.3D, -0.36D, foe.getDeltaMovement().z * 0.3D);
                                    foe.hasImpulse = true;

                                    if (zone.spell != null && zone.slot != null) {
                                        SpellExecutionEngine.deliverSubSpellOnEntity(
                                                srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                                zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.55F
                                        );
                                    }
                                    SpellCombatReactionEngine.hurtWithBarrowMateriaAdvantage(srv, foe, owner, slabDmg, false, true);
                                }

                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSSY_STONE_BRICKS.defaultBlockState()),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 26, 1.2D, 0.6D, 1.2D, 0.10D
                                );
                                srv.playSound(null, strikePos, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.80F, 0.72F);
                            }
                        }
                    }
                    for (int mIdx = 0; mIdx < 6; mIdx++) {
                        if (landed[mIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, megalithTargets[mIdx], 2, false);
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — GRAVE-WARDEN SKELETON SUITE (SUMMON / EIDOLON)
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
        tag.putString("SpellEidolonMateria", EssenceType.BARROW.name());
        tag.putInt("SpellEidolonTintRGB", GRAVE_WARDEN_HEX_COLOR);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 22, 0.45D, 0.5D, 0.45D, 0.06D
        );
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.39 Subterranean Crypt-Tunnel Step:
        // Sinks into the earth with heavy kinetic weight and re-emerges up to 14 blocks away, raising a 1-block
        // burial mound at departure that taunts hostiles, and granting +4 Armor & 50% KB resistance for 4.0s (80t)!
        BlockPos moundPos = BlockPos.containing(departurePos).immutable();
        BlockState prevState = level.getBlockState(moundPos);
        boolean placed = false;
        if (prevState.isAir() && !SpellExecutionEngine.isProtectedBlockOrContainer(level, moundPos, prevState)) {
            level.setBlockAndUpdate(moundPos, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            placed = true;
        }
        boolean finalPlaced = placed;
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 8.0D, 80, level.getGameTime(), null, null, EssenceType.BARROW, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        if (finalPlaced && srv.getBlockState(moundPos).is(Blocks.MOSSY_COBBLESTONE)) {
                            srv.setBlockAndUpdate(moundPos, prevState);
                        }
                        return false;
                    }
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB lure = new AABB(
                                departurePos.x - 8.0D, departurePos.y - 2.0D, departurePos.z - 8.0D,
                                departurePos.x + 8.0D, departurePos.y + 3.0D, departurePos.z + 8.0D
                        );
                        for (Mob mob : srv.getEntitiesOfClass(Mob.class, lure, m -> m.isAlive() && m instanceof Monster)) {
                            if (owner != null && SpellExecutionEngine.isFriendlyAllyForOasis(owner, mob)) continue;
                            mob.setTarget(null);
                            mob.getNavigation().moveTo(departurePos.x, departurePos.y, departurePos.z, 1.18D);
                        }
                    }
                    return true;
                }
        ));
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.50D, 0.0D, 80L);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COARSE_DIRT.defaultBlockState()),
                arrivalPos.x, arrivalPos.y + 0.5D, arrivalPos.z, 18, 0.4D, 0.35D, 0.4D, 0.05D
        );
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.39 Megalithic Grave Transmute:
        // Converts dirt, grass, and mud into coarse dirt, mossy cobblestone, or cracked stone bricks; fossilizes
        // rotten flesh/raw meat into bone blocks; turns cobblestone into mossy cobblestone; & wood into petrified wood!
        boolean didItem = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didItem = true;
            }
        }
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return didItem;
        }

        if (state.is(BlockTags.DIRT) || state.is(Blocks.MUD)) {
            int pick = level.random.nextInt(3);
            BlockState out = switch (pick) {
                case 0 -> Blocks.COARSE_DIRT.defaultBlockState();
                case 1 -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                default -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            maybeGrantBarrowBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.COBBLESTONE)) {
            level.setBlockAndUpdate(pos, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            maybeGrantBarrowBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.STONE_BRICKS)) {
            level.setBlockAndUpdate(pos, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
            maybeGrantBarrowBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.LOGS) && !state.is(ModBlocks.AMBER_LOG.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AMBER_LOG.get().withPropertiesOf(state));
            maybeGrantBarrowBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return didItem;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.BEEF) || stack.is(Items.PORKCHOP)
                || stack.is(Items.MUTTON) || stack.is(Items.CHICKEN) || stack.is(Items.RABBIT)) {
            itemEntity.setItem(new ItemStack(Items.BONE_BLOCK, stack.getCount()));
            maybeGrantBarrowBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (stack.is(Items.COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(Items.MOSSY_COBBLESTONE, stack.getCount()));
            maybeGrantBarrowBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (stack.is(ItemTags.DIRT) || stack.is(Items.MUD)) {
            itemEntity.setItem(new ItemStack(Items.COARSE_DIRT, stack.getCount()));
            maybeGrantBarrowBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (stack.is(ItemTags.LOGS)) {
            itemEntity.setItem(new ItemStack(Items.PETRIFIED_OAK_SLAB, Math.min(64, stack.getCount() * 2)));
            maybeGrantBarrowBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.39 Petrifying Sepulchral Entombment Hex:
        // Persistent milk-immune burial curse (cleansed via Restoration/Vitae magic, Radiant light, or caster defeat):
        // cuts movement & attack speed by 40%, blocks jumping, shreds 40% armor toughness, +35% physical/blunt damage,
        // and triggers Catacomb Awakening (+100% subterranean monster & armored skeleton spawn-lure on players)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.40D, 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1, true, true));

        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 40L);

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.40F + potencyTier * 0.45F) * mult;
        SpellCombatReactionEngine.hurtWithBarrowMateriaAdvantage(level, target, hexCaster, hexDmg, false, true);

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                target.getX(), target.getY(0.75D), target.getZ(), 5, 0.25D, 0.35D, 0.25D, 0.02D
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
            float heal = 3.0F + potencyTier * 0.90F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, heal);
            target.heal(heal);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static void maybeGrantBarrowBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellBarrowBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellBarrowBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellBarrowBatchTick", now);
        pData.putInt("SpellBarrowBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.BARROW, refund);
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
