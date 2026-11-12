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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Nether Materia (Base Type #6)</b>
 * (Spellcrafting System Guide Section 6.7).
 */
public final class NetherSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int INFERNAL_EIDOLON_HEX_COLOR = 0xB83C08;

    @Override
    public EssenceType materiaType() {
        return EssenceType.NETHER;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Arcing Fire-Whip Chains";
            default -> "Hellfire Brimstone " + flow.getDisplayName();
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
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
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

    // --- Part 1: Flow Mutations ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Arcing Fire-Whip Chains: leaves a line of burning fire blocks between chained victims!
        Vec3 start = fromTarget.position();
        Vec3 end = toTarget.position();
        double dist = start.distanceTo(end);
        Vec3 step = end.subtract(start).normalize();
        for (double d = 0.5D; d <= dist; d += 1.0D) {
            BlockPos bp = BlockPos.containing(start.add(step.scale(d)));
            if (level.getBlockState(bp).isAir() && level.getBlockState(bp.below()).isSolidRender()) {
                level.setBlockAndUpdate(bp, Blocks.FIRE.defaultBlockState());
            }
        }
        toTarget.igniteForSeconds(5);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(),
                2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        if (hitEntity instanceof LivingEntity living && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            living.igniteForSeconds(5);
        }
        autoSmeltDroppedItems(level, projectile.getBoundingBox().inflate(3.0D));
        level.sendParticles(ParticleTypes.LAVA, projectile.getX(), projectile.getY(), projectile.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        BlockPos firePos = blockHit.getBlockPos().relative(blockHit.getDirection());
        if (level.getBlockState(firePos).isAir() && level.getBlockState(firePos.below()).isSolidRender()) {
            level.setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
        }
        autoSmeltDroppedItems(level, new AABB(firePos).inflate(3.0D));
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Nether Tendril Tether: ignites foes & drains thermal energy to heal caster AND recharge Orbis Cell Nether Materia!
        int tier = spell.getEffectiveTier();
        long fuelPerHit = 12L * ddraig.net.entropica.spell.fuel.MateriaStageTable.getMfumPerNativeUnit(Math.max(1, tier - 1));
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.igniteForSeconds(5);
            float healAmt = 1.5F + tier * 0.4F;
            SpellCombatReactionEngine.notifyMagicalHeal(caster, healAmt);
            caster.heal(healAmt);
            if (caster instanceof net.minecraft.world.entity.player.Player player) {
                ddraig.net.entropica.spell.fuel.SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.NETHER, fuelPerHit);
            }
        }
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Cauterizing Cleave & Auto-Smelt: 6s unquenchable Netherfire + auto-smelts dying mob drops in-place!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.igniteForSeconds(6);
            CompoundTag data = EntityHelper.getPersistentData(hit);
            data.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
            data.putLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL, now + 40L);
            autoSmeltDroppedItems(level, hit.getBoundingBox().inflate(2.5D));
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Hellfire Ring Shockwave: knockback, ignite hostiles, auto-smelt ground drops, leave ring of fire blocks
        AABB box = new AABB(center.x - radius, center.y - 2.0D, center.z - radius, center.x + radius, center.y + 2.5D, center.z + radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity == caster || !entity.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            entity.igniteForSeconds(5);
            Vec3 away = entity.position().subtract(center).normalize().scale(1.15D);
            entity.setDeltaMovement(away.x, 0.35D, away.z);
            entity.hasImpulse = true;
        }
        autoSmeltDroppedItems(level, box);
        BlockPos cPos = BlockPos.containing(center);
        for (int deg = 0; deg < 360; deg += 45) {
            double rad = Math.toRadians(deg);
            BlockPos fp = cPos.offset((int) Math.round(Math.cos(rad) * 3.0D), 0, (int) Math.round(Math.sin(rad) * 3.0D));
            if (level.getBlockState(fp).isAir() && level.getBlockState(fp.below()).isSolidRender()) {
                level.setBlockAndUpdate(fp, Blocks.FIRE.defaultBlockState());
            }
        }
        level.playSound(null, cPos, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 0.9F);
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Immolation Shroud
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_NETHER_SHROUD_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.FLAME, caster.getX(), caster.getY(0.8D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Magma Puddle Trap: 3x3 molten magma/lava hazard for 4s (80t) before cooling into obsidian!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 2.5D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - 2.0D, zone.center.y - 1.0D, zone.center.z - 2.0D,
                            zone.center.x + 2.0D, zone.center.y + 2.0D, zone.center.z + 2.0D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner);
                    if (stepped.isEmpty()) return true;

                    BlockPos floor = BlockPos.containing(zone.center).below();
                    for (BlockPos p : BlockPos.betweenClosed(floor.offset(-1, 0, -1), floor.offset(1, 0, 1))) {
                        if (!SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, srv.getBlockState(p))) {
                            srv.setBlockAndUpdate(p, Blocks.MAGMA_BLOCK.defaultBlockState());
                        }
                    }
                    for (LivingEntity v : stepped) {
                        v.igniteForSeconds(5);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 2.0D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (msrv, mzone, melapsed) -> {
                                if (melapsed < 78) return true;
                                for (BlockPos p : BlockPos.betweenClosed(floor.offset(-1, 0, -1), floor.offset(1, 0, 1))) {
                                    if (msrv.getBlockState(p).is(Blocks.MAGMA_BLOCK)) {
                                        msrv.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
                                    }
                                }
                                return false;
                            }
                    ));
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Combustion Backdraft: blasts enemies forward while pushing caster safely backward with recoil!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.igniteForSeconds(5);
            hit.setDeltaMovement(direction.x * 1.45D, 0.35D, direction.z * 1.45D);
            hit.hasImpulse = true;
        }
        caster.setDeltaMovement(-direction.x * 0.85D, Math.max(0.25D, -direction.y * 0.45D), -direction.z * 0.85D);
        caster.hasImpulse = true;
        // Section 4.7 Nether Environmental Coating: leave scorched fire patches on ground blocks in the cone
        int steps = Math.min(6, (int) Math.ceil(coneRange));
        for (int s = 2; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(stepPos.offset(-1, -1, -1), stepPos.offset(1, 0, 1))) {
                if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isSolidRender() && level.random.nextFloat() < 0.30F) {
                    level.setBlockAndUpdate(p, Blocks.FIRE.defaultBlockState());
                }
            }
        }
        level.sendParticles(ParticleTypes.FLAME, caster.getX() + direction.x * 2.0D, caster.getEyeY() + direction.y * 2.0D, caster.getZ() + direction.z * 2.0D, 30, 0.7D, 0.4D, 0.7D, 0.15D);
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Brimstone Meteor Rain: 6s (120t) exploding netherfire meteors & water vaporization
        double r = Math.max(5.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.LAVA, zone.center.x, zone.center.y + 5.0D, zone.center.z, 12, zone.radius * 0.5D, 1.0D, zone.radius * 0.5D, 0.1D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        BlockPos cPos = BlockPos.containing(zone.center);
                        int br = Math.min(5, (int) zone.radius);
                        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-br, -1, -br), cPos.offset(br, 1, br))) {
                            if (srv.getBlockState(p).is(Blocks.WATER)) {
                                srv.removeBlock(p, false);
                                srv.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 3, 0.1D, 0.2D, 0.1D, 0.02D);
                            }
                        }
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            v.igniteForSeconds(4);
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
        tag.putString("SpellEidolonMateria", EssenceType.NETHER.name());
        tag.putInt("SpellEidolonTintRGB", INFERNAL_EIDOLON_HEX_COLOR);
        level.sendParticles(ParticleTypes.FLAME, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 25, 0.45D, 0.6D, 0.45D, 0.08D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Thermal Pop: pillar of flame at departure + explosive arrival shockwave pop!
        BlockPos dep = BlockPos.containing(departurePos);
        if (level.getBlockState(dep).isAir() && level.getBlockState(dep.below()).isSolidRender()) {
            level.setBlockAndUpdate(dep, Blocks.FIRE.defaultBlockState());
        }
        level.sendParticles(ParticleTypes.LAVA, departurePos.x, departurePos.y + 1.0D, departurePos.z, 16, 0.3D, 0.8D, 0.3D, 0.05D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(3.0D))) {
            if (foe == caster || foe.isAlliedTo(caster)) continue;
            foe.igniteForSeconds(4);
            Vec3 away = foe.position().subtract(arrivalPos).normalize().scale(0.95D);
            foe.setDeltaMovement(away.x, 0.35D, away.z);
            foe.hasImpulse = true;
        }
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7F, 1.35F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Thermal Pyrolysis
        if (state.is(Blocks.WATER)) {
            level.removeBlock(pos, false);
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 10, 0.2D, 0.3D, 0.2D, 0.03D);
            return true;
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.CLAY)) {
            level.setBlockAndUpdate(pos, Blocks.BRICKS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.COBBLESTONE)) {
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
            return true;
        }
        // Auto-smelt dropped resources when breaking ores/stone
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness >= 0.0F && hardness <= 2.0F + potencyTier * 5.0F) {
            List<ItemStack> drops = Block.getDrops(state, level, pos, null, caster, ItemStack.EMPTY);
            boolean smeltedAny = false;
            for (ItemStack d : drops) {
                ItemStack res = trySmeltStack(level, d);
                if (res != d) smeltedAny = true;
                Block.popResource(level, pos, res);
            }
            if (smeltedAny) {
                level.removeBlock(pos, false);
                level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8, 0.2D, 0.2D, 0.2D, 0.02D);
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
        ItemStack smelted = trySmeltStack(level, in);
        if (smelted != in) {
            itemEntity.setItem(smelted);
            level.sendParticles(ParticleTypes.FLAME, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(), 6, 0.15D, 0.15D, 0.15D, 0.02D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Hellfire Sear Hex: burns perpetually even underwater, blocks natural regen & melts 30% physical Armor!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.igniteForSeconds(3);
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 35L);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.30D * mult, 0.0D, 35L);
        target.hurtServer(level, level.damageSources().onFire(), (1.5F + potencyTier * 0.55F) * mult);
        level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY(0.7D), target.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }

    public static void autoSmeltDroppedItems(ServerLevel level, AABB box) {
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, box)) {
            ItemStack in = itemEntity.getItem();
            ItemStack smelted = trySmeltStack(level, in);
            if (smelted != in) {
                itemEntity.setItem(smelted);
                level.sendParticles(ParticleTypes.FLAME, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.02D);
            }
        }
    }

    public static ItemStack trySmeltStack(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) return stack;
        SingleRecipeInput input = new SingleRecipeInput(stack);
        var match = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level);
        if (match.isPresent()) {
            ItemStack out = match.get().value().assemble(input, level.registryAccess());
            if (!out.isEmpty()) {
                out.setCount(out.getCount() * stack.getCount());
                return out;
            }
        }
        return stack;
    }
}
