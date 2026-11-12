package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Complete per-part Signature Mutation implementation for <b>Arid Materia (Base Type #11)</b>
 * (Spellcrafting System Guide Section 6.12).
 */
public final class AridSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int DUNE_DERVISH_HEX_COLOR = 0xC2B28F;

    @Override
    public EssenceType materiaType() {
        return EssenceType.ARID;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Sirocco Leaping Chains";
            default -> "Sirocco Dust " + flow.getDisplayName();
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
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
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

    // --- Part 1: Flow Mutations ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Sirocco Leaping Chains: blinds struck mob for 1.5s (30t) and sweeps loose items along the chain path to caster
        toTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0));
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, toTarget.getX(), toTarget.getY(0.7D), toTarget.getZ(), 14, 0.3D, 0.4D, 0.3D, 0.05D);

        AABB pathBox = new AABB(fromTarget.position(), toTarget.position()).inflate(2.0D);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, pathBox)) {
            Vec3 toCaster = caster.position().add(0.0D, 0.5D, 0.0D).subtract(item.position());
            if (toCaster.lengthSqr() > 0.01D) {
                item.setDeltaMovement(toCaster.normalize().scale(0.65D));
                item.hasImpulse = true;
            }
        }
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, projectile.getX(), projectile.getY(), projectile.getZ(), 3, 0.14D, 0.14D, 0.14D, 0.02D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Dust Devil Missile: launches struck mobs ~3 blocks backward upon collision
        if (hitEntity instanceof LivingEntity living) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
                return;
            }
            Vec3 knock = projectile.getDeltaMovement();
            Vec3 horiz = new Vec3(knock.x, 0.0D, knock.z);
            if (horiz.lengthSqr() < 1.0E-4D && caster != null) {
                horiz = living.position().subtract(caster.position());
            }
            Vec3 push = horiz.normalize().scale(1.25D);
            living.setDeltaMovement(push.x, 0.38D, push.z);
            living.hasImpulse = true;
        }
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, projectile.getX(), projectile.getY(0.5D), projectile.getZ(), 20, 0.35D, 0.35D, 0.35D, 0.08D);
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Sirocco Sandstream: steady pushback velocity, Blindness while channeled, douses surface fire
        Vec3 push = new Vec3(direction.x, 0.0D, direction.z).normalize().scale(0.85D);
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(push.x, Math.max(0.2D, hit.getDeltaMovement().y), push.z);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            hit.clearFire();
        }
        Vec3 start = caster.getEyePosition();
        for (double d = 1.0D; d <= Math.min(maxReach, 20.0D); d += 1.0D) {
            BlockPos bp = BlockPos.containing(start.add(direction.scale(d)));
            extinguishFireAround(level, bp, 1);
        }
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Desiccating Mummification Strike: Slowness II & Hunger for 4s (80t); converts dying raw meat drops into cured meat + sand!
        long mummifyUntil = level.getGameTime() + 40L;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_ARID_MUMMIFY_DROPS_UNTIL, mummifyUntil);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
            hit.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0));
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Haboob Sandstorm Nova: 6-block radius knockback, blinds hostiles for 3s (60t), extinguishes fires, dries water pools
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == caster || !e.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            e.clearFire();
            Vec3 away = e.position().subtract(center).normalize().scale(1.15D);
            e.setDeltaMovement(away.x, 0.35D, away.z);
            e.hasImpulse = true;
        }

        BlockPos cPos = BlockPos.containing(center);
        int br = Math.min(5, (int) r);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-br, -1, -br), cPos.offset(br, 2, br))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(BlockTags.FIRE)) {
                level.removeBlock(p, false);
            } else if (bs.is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, level.random.nextBoolean() ? Blocks.SAND.defaultBlockState() : Blocks.PACKED_MUD.defaultBlockState());
            }
        }
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, center.x, center.y + 0.8D, center.z, 45, r * 0.45D, 0.6D, r * 0.45D, 0.12D);
        level.playSound(null, cPos, SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 1.0F, 0.75F);
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Sandstorm Veil: 50% mob detection cut, deflects hostile arrows, forms dry sandstone crusts over water
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ARID_VEIL_UNTIL, level.getGameTime() + dur);
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, caster.getX(), caster.getY(0.8D), caster.getZ(), 28, 0.5D, 0.6D, 0.5D, 0.08D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Desiccation Siphon Sigil: 4-block parched mineral dust burst inflicting severe Slowness & draining hunger
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB trig = new AABB(zone.center.x - 2.0D, zone.center.y - 1.0D, zone.center.z - 2.0D,
                            zone.center.x + 2.0D, zone.center.y + 2.0D, zone.center.z + 2.0D);
                    if (srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive() && e != owner).isEmpty()) return true;

                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, new AABB(BlockPos.containing(zone.center)).inflate(4.0D))) {
                        if (v == owner || !v.isAlive()) continue;
                        if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
                        v.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 2));
                        if (v instanceof Player player) {
                            FoodData food = player.getFoodData();
                            food.setFoodLevel(Math.max(0, food.getFoodLevel() - 6));
                        }
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState());
                    srv.sendParticles(sandDust, zone.center.x, zone.center.y + 0.5D, zone.center.z, 36, 1.2D, 0.5D, 1.2D, 0.1D);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Sandblast Blunderbuss Wave: heavy knockback, Blindness 2s (40t), scours grass -> sand/dirt, strips/petrifies logs
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            hit.setDeltaMovement(direction.x * 1.35D, 0.35D, direction.z * 1.35D);
            hit.hasImpulse = true;
        }

        BlockPos origin = BlockPos.containing(caster.position().add(direction.scale(2.5D)));
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-2, -1, -2), origin.offset(2, 2, 2))) {
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, level.getBlockState(p))) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.GRASS_BLOCK)) {
                level.setBlockAndUpdate(p, level.random.nextBoolean() ? Blocks.SAND.defaultBlockState() : Blocks.DIRT.defaultBlockState());
            } else if (bs.is(BlockTags.LOGS)) {
                stripOrPetrifyLog(level, p, bs);
            }
        }
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState());
        level.sendParticles(sandDust, caster.getX() + direction.x * 2.5D, caster.getEyeY() + direction.y * 2.5D, caster.getZ() + direction.z * 2.5D, 35, 0.8D, 0.4D, 0.8D, 0.15D);
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Haboob Sandstorm Downpour: 12-block dust storm for 6 seconds (120t); Blindness + Slowness, dries water, breaks leaves into sticks
        double r = Math.max(6.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 4 == 0) {
                        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
                        srv.sendParticles(sandDust, zone.center.x, zone.center.y + 4.5D, zone.center.z, 28, zone.radius * 0.55D, 1.5D, zone.radius * 0.55D, 0.1D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        BlockPos cPos = BlockPos.containing(zone.center);
                        int br = Math.min(6, (int) zone.radius);
                        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-br, -1, -br), cPos.offset(br, 4, br))) {
                            BlockState bs = srv.getBlockState(p);
                            if (bs.is(Blocks.WATER)) {
                                srv.removeBlock(p, false);
                            } else if (bs.is(BlockTags.LEAVES) && srv.random.nextFloat() < 0.35F) {
                                srv.removeBlock(p, false);
                                Block.popResource(srv, p, new ItemStack(Items.STICK, 1));
                            }
                        }
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                            v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.ARID.name());
        tag.putInt("SpellEidolonTintRGB", DUNE_DERVISH_HEX_COLOR);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(BlockPos.containing(spawnPos)).inflate(3.0D))) {
            if (foe == caster || foe == summonEntity || foe.isAlliedTo(caster)) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        }
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState());
        level.sendParticles(sandDust, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 24, 0.45D, 0.6D, 0.45D, 0.08D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Mirage Step: desert dust swirl & shimmering heat mirage decoy at departure point that briefly distracts mobs
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, departurePos.x, departurePos.y + 0.9D, departurePos.z, 22, 0.35D, 0.65D, 0.35D, 0.08D);
        level.sendParticles(sandDust, arrivalPos.x, arrivalPos.y + 0.9D, arrivalPos.z, 18, 0.35D, 0.65D, 0.35D, 0.08D);

        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 6.0D, 60, level.getGameTime(), dummy, dummy.subSpells().get(0), EssenceType.ARID, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.WHITE_ASH, departurePos.x, departurePos.y + 1.0D, departurePos.z, 10, 0.25D, 0.55D, 0.25D, 0.02D);
                        LivingEntity owner = zone.resolveCaster(srv);
                        for (Mob mob : srv.getEntitiesOfClass(Mob.class, new AABB(BlockPos.containing(departurePos)).inflate(7.0D))) {
                            if (mob instanceof Monster && mob.getTarget() == owner) {
                                mob.setTarget(null);
                                mob.getNavigation().moveTo(departurePos.x, departurePos.y, departurePos.z, 1.0D);
                            }
                        }
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Petrofaction & Desiccation: dries mud, sponges, sand -> sandstone, farmland -> dirt, and strips/petrifies logs
        if (state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.WET_SPONGE)) {
            level.setBlockAndUpdate(pos, Blocks.SPONGE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.SAND)) {
            level.setBlockAndUpdate(pos, Blocks.SANDSTONE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, Blocks.RED_SANDSTONE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.FARMLAND)) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.WATER)) {
            level.removeBlock(pos, false);
            return true;
        }
        if (state.is(BlockTags.LOGS)) {
            return stripOrPetrifyLog(level, pos, state);
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        int count = stack.getCount();

        if (stack.is(Items.WET_SPONGE)) {
            itemEntity.setItem(new ItemStack(Items.SPONGE, count));
            return true;
        }
        if (stack.is(Items.MUD)) {
            itemEntity.setItem(new ItemStack(Items.CLAY, count));
            return true;
        }
        if (stack.is(Items.CLAY)) {
            itemEntity.setItem(new ItemStack(Items.TERRACOTTA, count));
            return true;
        }
        if (stack.is(Items.SAND)) {
            itemEntity.setItem(new ItemStack(Items.SANDSTONE, count));
            return true;
        }
        if (stack.is(Items.RED_SAND)) {
            itemEntity.setItem(new ItemStack(Items.RED_SANDSTONE, count));
            return true;
        }
        if (stack.is(Items.ROTTEN_FLESH)) {
            itemEntity.setItem(new ItemStack(Items.LEATHER, count));
            return true;
        }
        if (stack.is(Items.WATER_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.BUCKET, count));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Desiccation Mummification Hex: Hunger III, Slowness II, disables natural regen, double fire damage & Desiccated Fragility
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 35, 2, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 1, true, true));
        if (target instanceof Player player) {
            player.getFoodData().setSaturation(0.0F);
        }
        target.hurtServer(level, level.damageSources().dryOut(), (1.0F + potencyTier * 0.45F) * mult);
        BlockParticleOption sandDust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        level.sendParticles(sandDust, target.getX(), target.getY(0.7D), target.getZ(), 5, 0.25D, 0.35D, 0.25D, 0.04D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target, Vec3 direction, SpellCoreEffect effect, int potencyTier, SpellAssembly spell) {
        if (effect == SpellCoreEffect.RESTORATION) {
            // Mineral Cauterization: rapid burst healing & +2.0 Armor Toughness at the cost of 16 hunger points!
            if (target instanceof Player player) {
                FoodData food = player.getFoodData();
                food.setFoodLevel(Math.max(0, food.getFoodLevel() - 16));
            }
            float healAmt = 6.0F + potencyTier * 1.5F;
            int buffDur = 160 + potencyTier * 20;
            SpellCombatReactionEngine.notifyMagicalHeal(target, healAmt);
            target.heal(healAmt);
            SpellCombatReactionEngine.applyTimedDefenseBuff(target, level, 2.0D, 0.0D, buffDur);
            target.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, buffDur, 0));
        }
    }

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

        // Already stripped or no stripped variant exists -> petrify into mineral pillar!
        BlockState petrified = Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        level.setBlockAndUpdate(pos, petrified);
        return true;
    }

    private static void extinguishFireAround(ServerLevel level, BlockPos center, int rad) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-rad, -1, -rad), center.offset(rad, 1, rad))) {
            if (level.getBlockState(p).is(BlockTags.FIRE)) {
                level.removeBlock(p, false);
            }
        }
    }
}
